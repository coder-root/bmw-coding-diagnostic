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
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * ISO 13400 Diagnostic over IP (DoIP) transport adapter for high-speed BMW Ethernet / Wi-Fi ENET communication.
 * Connects on TCP port 13400, negotiates routing activation, and encapsulates UDS messages.
 */
class DoIpTransportAdapter(
    override val adapterType: AdapterType = AdapterType.ENET_WIFI,
    private val context: Context? = null,
    private val host: String = DEFAULT_DOIP_HOST,
    private val port: Int = DoIpProtocol.DEFAULT_DOIP_PORT,
    private val testerLogicalAddress: Int = DoIpProtocol.BMW_TESTER_LOGICAL_ADDRESS,
    private val timeoutMs: Int = 4000
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
            val inp = DataInputStream(tcpSocket.getInputStream())
            val out = tcpSocket.getOutputStream()
            input = inp
            output = out

            // Step 1: Perform Routing Activation Handshake (0x0005)
            val req = DoIpProtocol.createRoutingActivationRequest(sourceAddress = testerLogicalAddress)
            out.write(req.encode())
            out.flush()

            val resp = readDoIpMessage(inp)
            if (resp.payloadType == DoIpProtocol.TYPE_ROUTING_ACTIVATION_RESPONSE) {
                val respCode = if (resp.payload.size >= 5) resp.payload[4].toInt() and 0xFF else -1
                if (respCode == DoIpProtocol.ROUTING_SUCCESS || respCode == DoIpProtocol.ROUTING_ALREADY_ACTIVE) {
                    _connectionState.value = ConnectionState.Connected(adapterType, "$host:$port (DoIP Active)")
                } else {
                    throw IllegalStateException("DoIP Routing Activation Denied by BMW Gateway: code 0x%02X".format(respCode))
                }
            } else {
                // If the gateway accepted connection but returned different initial frame
                _connectionState.value = ConnectionState.Connected(adapterType, "$host:$port (DoIP Active)")
            }
        } catch (ex: Exception) {
            _connectionState.value = ConnectionState.Error(
                "DoIP bağlantısı kurulamadı ($host:$port): ${ex.localizedMessage ?: "Bağlantı hatası"}"
            )
            disconnect()
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
        val out = output ?: error("DoIP socket is not connected")
        val inp = input ?: error("DoIP input stream is not available")

        // In our architecture, the input frame can be either a raw DoIP message,
        // or an HSFZ-wrapped diagnostic request which we convert to DoIP,
        // or a raw UDS payload where target address is in the header.
        val doIpFrame = if (frame.size >= DoIpProtocol.HEADER_SIZE && frame[0].toInt() == DoIpProtocol.PROTOCOL_VERSION) {
            frame
        } else if (frame.size >= HsfzFrame.HEADER_SIZE) {
            val hsfz = HsfzFrame.decode(frame)
            DoIpProtocol.createDiagnosticMessage(
                sourceAddress = testerLogicalAddress,
                targetAddress = hsfz.targetAddress,
                udsPayload = hsfz.payload
            ).encode()
        } else {
            // Raw UDS payload defaulted to BDC/ZGW (0x0010)
            DoIpProtocol.createDiagnosticMessage(
                sourceAddress = testerLogicalAddress,
                targetAddress = 0x0010,
                udsPayload = frame
            ).encode()
        }

        out.write(doIpFrame)
        out.flush()

        // Read DoIP responses. Note: DoIP gateways typically send a Diagnostic Message ACK (0x8002)
        // followed immediately by the Diagnostic Message response (0x8001).
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val msg = readDoIpMessage(inp)
            when (msg.payloadType) {
                DoIpProtocol.TYPE_DIAGNOSTIC_POS_ACK -> {
                    // Gateway acknowledged receipt, continue waiting for actual 0x8001 response
                    continue
                }
                DoIpProtocol.TYPE_ALIVE_CHECK_REQUEST -> {
                    // Gateway heartbeat check: reply immediately and keep waiting
                    val aliveResp = DoIpProtocol.createAliveCheckResponse(testerLogicalAddress)
                    out.write(aliveResp.encode())
                    out.flush()
                    continue
                }
                DoIpProtocol.TYPE_DIAGNOSTIC_MESSAGE -> {
                    // Extract payload: [0..1] sourceAddress, [2..3] targetAddress, [4..] UDS response
                    if (msg.payload.size >= 4) {
                        val buffer = ByteBuffer.wrap(msg.payload).order(ByteOrder.BIG_ENDIAN)
                        val source = buffer.short.toInt() and 0xFFFF
                        val target = buffer.short.toInt() and 0xFFFF
                        val uds = ByteArray(msg.payload.size - 4)
                        buffer.get(uds)
                        // Package into HsfzFrame so the rest of the app's UDS parser can process transparently
                        return@withContext HsfzFrame.diagnosticRequest(targetAddress = source, udsPayload = uds).encode()
                    }
                    return@withContext msg.encode()
                }
                DoIpProtocol.TYPE_DIAGNOSTIC_NEG_ACK -> {
                    val nackCode = if (msg.payload.isNotEmpty()) msg.payload[0].toInt() and 0xFF else 0xFF
                    throw IllegalStateException("DoIP Diagnostic Negative ACK from gateway: 0x%02X".format(nackCode))
                }
                else -> {
                    // Other messages
                    if (msg.payload.isNotEmpty()) {
                        return@withContext msg.encode()
                    }
                }
            }
        }
        throw IllegalStateException("DoIP diagnostic response timed out ($timeoutMs ms)")
    }

    private fun readDoIpMessage(inp: DataInputStream): DoIpMessage {
        val version = inp.readUnsignedByte()
        val invVersion = inp.readUnsignedByte()
        val payloadType = inp.readUnsignedShort()
        val payloadLength = inp.readInt()
        require(payloadLength >= 0 && payloadLength <= 65536) {
            "Invalid DoIP payload length: $payloadLength"
        }
        val payload = ByteArray(payloadLength)
        inp.readFully(payload)
        return DoIpMessage(protocolVersion = version, payloadType = payloadType, payload = payload)
    }

    companion object {
        const val DEFAULT_DOIP_HOST = "169.254.0.1"
    }
}
