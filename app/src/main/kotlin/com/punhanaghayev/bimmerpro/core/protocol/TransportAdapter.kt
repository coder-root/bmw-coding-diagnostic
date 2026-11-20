package com.punhanaghayev.bimmerpro.core.protocol

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

enum class AdapterType(
    val displayName: String,
    val protocolSummary: String,
    val supportsEnetspeed: Boolean
) {
    VLINKER_BM_WIFI("Vgate vLinker BM Wi-Fi", "STN2255 Wi-Fi TCP 192.168.0.10:35000 (F/G/E)", false),
    ENET_WIFI("Wi-Fi ENET (Thor / MHD / BM3)", "HSFZ / DoIP TCP:6801", true),
    ENET_USB("USB-C to ENET Kablo", "HSFZ / DoIP 100Mbps Ethernet", true),
    BLE_OBD("OBDLink CX / UniCarScan BLE", "ISO-TP over CAN (BLE GATT)", false),
    USB_KDCAN("USB OTG K+DCAN Kablo", "E-Series K-Line / D-CAN", false),
    SIMULATOR_F10("BMW F10 520d Simülasyonu", "Virtual HSFZ / ENET + UDS", true),
    SIMULATOR_F10_528I("BMW F10 528i Simülasyonu", "Virtual HSFZ / ENET + N20 Benzin", true),
    SIMULATOR_F30("BMW F30 328i Simülasyonu", "Virtual HSFZ / ENET + N20", true),
    SIMULATOR_G30("BMW G30 530i Simülasyonu", "Virtual DoIP / HSFZ + Secure Coding Check", true),
    SIMULATOR_G20("BMW G20 M340i Simülasyonu", "Virtual DoIP / B58 + iDrive 7", true),
    SIMULATOR_E90("BMW E90 335i Simülasyonu", "Virtual K+DCAN / KWP2000 + Tamper Alert Demo", false);

    val isSimulator: Boolean
        get() = name.startsWith("SIMULATOR")
}

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data class Connecting(val adapterName: String) : ConnectionState
    data class Connected(val adapterType: AdapterType, val endpoint: String) : ConnectionState
    data class Error(val message: String) : ConnectionState
}

interface TransportAdapter {
    val adapterType: AdapterType
    val connectionState: StateFlow<ConnectionState>
    suspend fun connect()
    suspend fun disconnect()
    suspend fun transceive(frame: ByteArray): ByteArray
}

/**
 * Physical TCP ENET transport for F/G/I-Series vehicles via ZGW/BDC gateway on port 6801.
 */
class TcpEnetTransportAdapter(
    override val adapterType: AdapterType,
    private val context: Context? = null,
    private val host: String = DEFAULT_ZGW_HOST,
    private val port: Int = DEFAULT_HSFZ_PORT,
    private val timeoutMs: Int = 3500
) : TransportAdapter {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var socket: Socket? = null
    private var input: DataInputStream? = null
    private var output: OutputStream? = null

    override suspend fun connect() = withContext(Dispatchers.IO) {
        _connectionState.value = ConnectionState.Connecting(adapterType.displayName)
        try {
            val tcpSocket = WifiNetworkBinder.createSocket(context)
            tcpSocket.tcpNoDelay = true
            tcpSocket.soTimeout = timeoutMs
            tcpSocket.connect(InetSocketAddress(host, port), timeoutMs)
            socket = tcpSocket
            input = DataInputStream(tcpSocket.getInputStream())
            output = tcpSocket.getOutputStream()
            _connectionState.value = ConnectionState.Connected(adapterType, "$host:$port")
        } catch (ex: Exception) {
            _connectionState.value = ConnectionState.Error(
                "ENET bağlantısı kurulamadı ($host:$port): ${ex.localizedMessage ?: "Ağ zaman aşımı"}"
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
        val out = output ?: error("ENET socket is not connected")
        val inp = input ?: error("ENET input stream is not available")
        out.write(frame)
        out.flush()

        val bodyLength = inp.readInt()
        val controlWord = inp.readShort().toInt() and 0xFFFF
        val body = ByteArray(bodyLength)
        inp.readFully(body)

        val responseFrame = ByteArray(HsfzFrame.HEADER_SIZE + bodyLength)
        responseFrame[0] = ((bodyLength ushr 24) and 0xFF).toByte()
        responseFrame[1] = ((bodyLength ushr 16) and 0xFF).toByte()
        responseFrame[2] = ((bodyLength ushr 8) and 0xFF).toByte()
        responseFrame[3] = (bodyLength and 0xFF).toByte()
        responseFrame[4] = ((controlWord ushr 8) and 0xFF).toByte()
        responseFrame[5] = (controlWord and 0xFF).toByte()
        System.arraycopy(body, 0, responseFrame, HsfzFrame.HEADER_SIZE, bodyLength)
        responseFrame
    }

    companion object {
        const val DEFAULT_ZGW_HOST = "169.254.0.1"
        const val DEFAULT_HSFZ_PORT = 6801
    }
}
