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

enum class AdapterTransportKind {
    WIFI_ELM_STN,
    ENET_TCP,
    BLE_OBD,
    CLASSIC_BT_OBD,
    USB_KDCAN,
    SIMULATOR
}

enum class AdapterType(
    val displayName: String,
    val protocolSummary: String,
    val supportsEnetspeed: Boolean,
    val transportKind: AdapterTransportKind
) {
    ENET_WIFI("ENET Cable / Wi-Fi", "HSFZ / DoIP TCP:6801 (100 Mbps)", true, AdapterTransportKind.ENET_TCP),
    UNICARSCAN_3000("UniCarScan - 3000", "BLE / BT Dual-Mode ISO-TP (E/F/G/I)", false, AdapterTransportKind.BLE_OBD),
    UNICARSCAN_2100("UniCarScan - 2100", "BLE ISO-TP over CAN (E/F/G)", false, AdapterTransportKind.BLE_OBD),
    BLE_OBD("OBDLink CX / CX+", "STN BLE GATT ISO-TP (F/G/I)", false, AdapterTransportKind.BLE_OBD),
    OBDLINK_MX_PLUS("OBDLink MX+", "Classic BT SPP High-Speed STN ISO-TP", false, AdapterTransportKind.CLASSIC_BT_OBD),
    VLINKER_BM_WIFI("vLinker Wi-Fi", "STN2255 Wi-Fi TCP 192.168.0.10:35000", false, AdapterTransportKind.WIFI_ELM_STN),
    VLINKER_BT("vLinker Bluetooth", "STN2255 BLE / BT ISO-TP (BM+ / MC+)", false, AdapterTransportKind.BLE_OBD),
    VGATE_ICAR_BT("Vgate iCar Bluetooth", "Classic BT SPP / ELM327 ISO-TP", false, AdapterTransportKind.CLASSIC_BT_OBD),
    VEEPEAK_BT("Veepeak Bluetooth", "OBDCheck BLE+ ISO-TP", false, AdapterTransportKind.BLE_OBD),
    KONNWEI("Konnwei", "KW902 / KW903 Classic BT SPP ELM327", false, AdapterTransportKind.CLASSIC_BT_OBD),
    MODBM_ENET("modBM ENET", "Wi-Fi ENET HSFZ / DoIP TCP:6801", true, AdapterTransportKind.ENET_TCP),
    THOR("THOR", "THOR Wi-Fi ENET HSFZ / DoIP", true, AdapterTransportKind.ENET_TCP),
    KIES_WIFI("KIES Wi-Fi", "KIES Wi-Fi ENET HSFZ / DoIP", true, AdapterTransportKind.ENET_TCP),
    MG_FLASHER("MG Flasher", "MG Flasher Wi-Fi ENET / DoIP", true, AdapterTransportKind.ENET_TCP),
    BM3("bm3", "bootmod3 Wi-Fi ENET / DoIP", true, AdapterTransportKind.ENET_TCP),
    MHD_UNIVERSAL("MHD Universal", "MHD Universal Wi-Fi ENET / DoIP", true, AdapterTransportKind.ENET_TCP),
    XHP("xHP", "xHP Wi-Fi ENET / DoIP", true, AdapterTransportKind.ENET_TCP),
    ENET_USB("USB-C to ENET Cable", "HSFZ / DoIP 100Mbps Ethernet", true, AdapterTransportKind.ENET_TCP),
    USB_KDCAN("USB OTG K+DCAN Cable", "E-Series K-Line / D-CAN (Pin 7+8)", false, AdapterTransportKind.USB_KDCAN),
    SIMULATOR_F10("BMW F10 520d", "Virtual HSFZ / ENET + UDS", true, AdapterTransportKind.SIMULATOR),
    SIMULATOR_F10_528I("BMW F10 528i", "Virtual HSFZ / ENET + N20 Petrol", true, AdapterTransportKind.SIMULATOR),
    SIMULATOR_F30("BMW F30 328i", "Virtual HSFZ / ENET + N20", true, AdapterTransportKind.SIMULATOR),
    SIMULATOR_G30("BMW G30 530i", "Virtual DoIP / HSFZ + Secure Coding", true, AdapterTransportKind.SIMULATOR),
    SIMULATOR_G20("BMW G20 M340i", "Virtual DoIP / B58 + iDrive 7", true, AdapterTransportKind.SIMULATOR),
    SIMULATOR_U11("BMW U11 iX1 / X1", "Virtual DoIP / OS 9 + U-Series", true, AdapterTransportKind.SIMULATOR),
    SIMULATOR_E90("BMW E90 335i", "Virtual K+DCAN / KWP2000 + Tamper Demo", false, AdapterTransportKind.SIMULATOR);

    val isSimulator: Boolean
        get() = transportKind == AdapterTransportKind.SIMULATOR
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
