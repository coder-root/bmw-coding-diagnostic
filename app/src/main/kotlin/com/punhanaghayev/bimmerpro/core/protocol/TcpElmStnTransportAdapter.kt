package com.punhanaghayev.bimmerpro.core.protocol

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets

/**
 * Physical Wi-Fi TCP transport for Vgate vLinker BM Wi-Fi (STN2255) and ELM327 Wi-Fi OBD-II adapters.
 * Default endpoint for vLinker BM Wi-Fi is 192.168.0.10:35000 (fallback port 23).
 *
 * Explicitly binds to the Android Wi-Fi network so traffic is never routed over 4G/5G cellular data.
 */
class TcpElmStnTransportAdapter(
    override val adapterType: AdapterType = AdapterType.VLINKER_BM_WIFI,
    private val context: Context? = null,
    private val host: String = DEFAULT_VLINKER_HOST,
    private val port: Int = DEFAULT_VLINKER_PORT,
    private val timeoutMs: Int = 5000,
    private val onAtLog: (String) -> Unit = {}
) : TransportAdapter {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var socket: Socket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null

    var lastReadVoltage: Double? = null
        private set

    var isStnChip: Boolean = false
        private set

    override suspend fun connect() = withContext(Dispatchers.IO) {
        _connectionState.value = ConnectionState.Connecting("${adapterType.displayName} ($host:$port)")
        val candidatePorts = listOf(port, 35000, 23).distinct()
        var connectedSocket: Socket? = null
        var connectedPort = port
        var lastError: Exception? = null

        val wifiBound = context != null && WifiNetworkBinder.findWifiNetwork(context) != null
        onAtLog("WIFI-BIND: ${if (wifiBound) "Wi-Fi ağına kilitlendi" else "Varsayılan ağ kullanılıyor"}")

        for (tryPort in candidatePorts) {
            try {
                val s = WifiNetworkBinder.createSocket(context).apply {
                    tcpNoDelay = true
                    soTimeout = timeoutMs
                    connect(InetSocketAddress(host, tryPort), timeoutMs)
                }
                connectedSocket = s
                connectedPort = tryPort
                break
            } catch (ex: Exception) {
                lastError = ex
                onAtLog("TCP $host:$tryPort başarısız: ${ex.localizedMessage ?: "Bağlantı hatası"}")
            }
        }

        if (connectedSocket == null) {
            val msg = "vLinker BM Wi-Fi ($host:$port) bağlanamadı. " +
                "1) Telefonu vLinker Wi-Fi ağına bağlayın. " +
                "2) Gerekirse Mobil Veriyi geçici kapatın. " +
                "(${lastError?.localizedMessage ?: "Zaman aşımı"})"
            _connectionState.value = ConnectionState.Error(msg)
            throw lastError ?: IllegalStateException(msg)
        }

        try {
            socket = connectedSocket
            input = connectedSocket.getInputStream()
            output = connectedSocket.getOutputStream()
            onAtLog("TCP bağlı: $host:$connectedPort")

            // Drain any welcome banner
            delay(150)
            drainInput()

            // Initialize STN2255 / ELM327 for BMW D-CAN (500kbps) + adaptive timing
            sendAtCommand("ATZ", waitMs = 1200)
            val chipId = sendAtCommand("STI", waitMs = 500)
            isStnChip = chipId.contains("STN", ignoreCase = true) || chipId.contains("vLinker", ignoreCase = true)
            onAtLog("CHIP: ${if (isStnChip) "STN Fast ISO-TP (STPX) Aktif ($chipId)" else "Standart ELM327 ($chipId)"}")

            sendAtCommand("ATE0")
            sendAtCommand("ATL0")
            sendAtCommand("ATS0")
            sendAtCommand("ATH1")
            sendAtCommand("ATSP6")
            sendAtCommand("ATCAF1")
            sendAtCommand("ATAL")
            sendAtCommand("ATAT2")
            sendAtCommand("ATST64")
            // Enable STN2255 ISO-TP long-frame segmentation receive when supported
            if (isStnChip) {
                sendAtCommand("STCSEGR 1", waitMs = 400)
            }

            val rv = sendAtCommand("ATRV")
            lastReadVoltage = parseVoltage(rv)

            _connectionState.value = ConnectionState.Connected(adapterType, "$host:$connectedPort")
        } catch (ex: Exception) {
            runCatching { connectedSocket.close() }
            socket = null
            val msg = "vLinker AT başlatma hatası: ${ex.localizedMessage ?: "Cevap yok"}"
            _connectionState.value = ConnectionState.Error(msg)
            throw ex
        }
    }

    suspend fun configureKLineKwp2000() = withContext(Dispatchers.IO) {
        sendAtCommand("ATSPB") // ISO 14230-4 KWP fast init (10.4 kbps)
        sendAtCommand("ATAT1")
        sendAtCommand("ATST96")
        onAtLog("K-LINE: ATSPB 10.4 kbps KWP2000 yapılandırıldı")
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        runCatching { socket?.close() }
        socket = null
        input = null
        output = null
        _connectionState.value = ConnectionState.Disconnected
    }

    override suspend fun transceive(frame: ByteArray): ByteArray = withContext(Dispatchers.IO) {
        val hsfz = HsfzFrame.decode(frame)
        val targetEcu = hsfz.targetAddress and 0xFF
        val hexPayload = hsfz.payload.joinToString("") { "%02X".format(it) }

        val udsBytes = if (isStnChip) {
            // STN high-speed direct ISO-TP single command: STPX h:6F1, d:<hexPayload>, r:1, t:350
            val stpxCmd = "STPX h:6F1, d:$hexPayload, r:1, t:350"
            val rawResponse = sendAtCommand(stpxCmd)
            parseStnUdsResponse(rawResponse)
        } else {
            sendAtCommand("ATSH6F1")
            sendAtCommand("ATCEA%02X".format(targetEcu))
            sendAtCommand("ATCRA6F1")
            val rawResponse = sendAtCommand(hexPayload)
            parseStnUdsResponse(rawResponse)
        }

        HsfzFrame(
            controlWord = HsfzFrame.CONTROL_DIAGNOSTIC_DATA,
            sourceAddress = targetEcu,
            targetAddress = HsfzFrame.TESTER_ADDRESS,
            payload = udsBytes
        ).encode()
    }

    private fun drainInput() {
        val inp = input ?: return
        runCatching {
            while (inp.available() > 0) {
                val skipBuf = ByteArray(inp.available().coerceAtMost(512))
                inp.read(skipBuf)
            }
        }
    }

    private fun sendAtCommand(command: String, waitMs: Int = timeoutMs): String {
        val out = output ?: error("vLinker Wi-Fi socket bağlı değil")
        val inp = input ?: error("vLinker Wi-Fi input stream yok")
        drainInput()
        out.write("$command\r".toByteArray(StandardCharsets.US_ASCII))
        out.flush()

        val sb = StringBuilder()
        val buf = ByteArray(512)
        val deadline = System.currentTimeMillis() + waitMs
        while (System.currentTimeMillis() < deadline) {
            val read = runCatching { inp.read(buf) }.getOrDefault(-1)
            if (read <= 0) break
            sb.append(String(buf, 0, read, StandardCharsets.US_ASCII))
            if (sb.contains(">")) break
        }
        val response = sb.toString()
        onAtLog("AT [$command] -> ${response.replace("\r", " ").replace("\n", " ").trim()}")
        return response
    }

    companion object {
        const val DEFAULT_VLINKER_HOST = "192.168.0.10"
        const val DEFAULT_VLINKER_PORT = 35000

        internal fun parseVoltage(raw: String): Double? =
            ElmIsoTpHelper.parseVoltage(raw)

        internal fun parseStnUdsResponse(raw: String): ByteArray =
            ElmIsoTpHelper.parseUdsResponse(raw)
    }
}
