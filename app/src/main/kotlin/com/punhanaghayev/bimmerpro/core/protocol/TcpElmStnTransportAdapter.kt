package com.punhanaghayev.bimmerpro.core.protocol

import kotlinx.coroutines.Dispatchers
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
 * Default endpoint for vLinker BM Wi-Fi is 192.168.0.10:35000.
 *
 * Uses BMW 500kbps CAN (ATSP6) with extended addressing (ATCEA / 0x6F1) and STN long-frame support.
 */
class TcpElmStnTransportAdapter(
    override val adapterType: AdapterType = AdapterType.VLINKER_BM_WIFI,
    private val host: String = DEFAULT_VLINKER_HOST,
    private val port: Int = DEFAULT_VLINKER_PORT,
    private val timeoutMs: Int = 4000
) : TransportAdapter {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var socket: Socket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null

    var lastReadVoltage: Double? = null
        private set

    override suspend fun connect() = withContext(Dispatchers.IO) {
        _connectionState.value = ConnectionState.Connecting("${adapterType.displayName} ($host:$port)")
        try {
            val tcp = Socket().apply {
                tcpNoDelay = true
                soTimeout = timeoutMs
                connect(InetSocketAddress(host, port), timeoutMs)
            }
            socket = tcp
            input = tcp.getInputStream()
            output = tcp.getOutputStream()

            // Initialize STN2255 / ELM327 for BMW F-Series / E-Series D-CAN (500kbps)
            sendAtCommand("ATZ")
            sendAtCommand("ATE0")
            sendAtCommand("ATL0")
            sendAtCommand("ATS0")
            sendAtCommand("ATH1")
            sendAtCommand("ATSP6")
            sendAtCommand("ATCAF1")
            sendAtCommand("ATAL")

            val rv = sendAtCommand("ATRV")
            lastReadVoltage = parseVoltage(rv)

            _connectionState.value = ConnectionState.Connected(adapterType, "$host:$port")
        } catch (ex: Exception) {
            runCatching { socket?.close() }
            socket = null
            _connectionState.value = ConnectionState.Error(
                "vLinker BM Wi-Fi bağlantısı kurulamadı ($host:$port): ${ex.localizedMessage ?: "Ağ zaman aşımı"}. Telefonun vLinker Wi-Fi ağına bağlı olduğundan emin olun."
            )
            throw ex
        }
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
        // BMW F/E-Series functional/physical CAN addressing via 0x6F1 + extended target byte
        sendAtCommand("ATSH6F1")
        sendAtCommand("ATCEA%02X".format(targetEcu))
        sendAtCommand("ATCRA6F1")

        val hexPayload = hsfz.payload.joinToString("") { "%02X".format(it) }
        val rawResponse = sendAtCommand(hexPayload)
        val udsBytes = parseStnUdsResponse(rawResponse)

        HsfzFrame(
            controlWord = HsfzFrame.CONTROL_DIAGNOSTIC_DATA,
            sourceAddress = targetEcu,
            targetAddress = HsfzFrame.TESTER_ADDRESS,
            payload = udsBytes
        ).encode()
    }

    private fun sendAtCommand(command: String): String {
        val out = output ?: error("vLinker Wi-Fi socket bağlı değil")
        val inp = input ?: error("vLinker Wi-Fi input stream yok")
        out.write("$command\r".toByteArray(StandardCharsets.US_ASCII))
        out.flush()

        val sb = StringBuilder()
        val buf = ByteArray(512)
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val read = inp.read(buf)
            if (read <= 0) break
            sb.append(String(buf, 0, read, StandardCharsets.US_ASCII))
            if (sb.contains(">")) break
        }
        return sb.toString()
    }

    companion object {
        const val DEFAULT_VLINKER_HOST = "192.168.0.10"
        const val DEFAULT_VLINKER_PORT = 35000

        internal fun parseVoltage(raw: String): Double? {
            val match = Regex("(\\d{1,2}\\.\\d{1,2})").find(raw) ?: return null
            return match.groupValues[1].toDoubleOrNull()
        }

        internal fun parseStnUdsResponse(raw: String): ByteArray {
            val clean = raw
                .replace(">", "")
                .replace("\r", " ")
                .replace("\n", " ")
                .trim()
            if (clean.contains("NO DATA", ignoreCase = true) || clean.contains("ERROR", ignoreCase = true)) {
                return byteArrayOf(0x7F, 0x22, 0x31)
            }
            val tokens = clean.split(Regex("\\s+"))
                .filter { it.length == 2 && it.all { c -> c.isDigit() || c.uppercaseChar() in 'A'..'F' } }
            // If raw CAN frame includes header (e.g. 6F1 F4 ...), strip first 2/3 header bytes when appropriate
            val bytes = tokens.map { it.toInt(16).toByte() }.toByteArray()
            return if (bytes.size > 3 && (bytes[0].toInt() and 0xFF) == 0x06 && (bytes[1].toInt() and 0xFF) == 0xF1) {
                bytes.copyOfRange(2, bytes.size)
            } else {
                bytes
            }
        }
    }
}
