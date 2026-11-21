package com.punhanaghayev.bimmerpro.core.protocol

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.charset.StandardCharsets

data class DiscoveredGateway(
    val ipAddress: String,
    val vin: String?,
    val diagnosticPort: Int = TcpEnetTransportAdapter.DEFAULT_HSFZ_PORT
)

/**
 * Discovers BMW ZGW/BDC gateways on local ENET/Wi-Fi via UDP port 6811 (HSFZ/DoIP vehicle announcement).
 */
object EnetGatewayDiscovery {

    const val DISCOVERY_UDP_PORT = 6811
    const val DOIP_UDP_PORT = 13400
    private val IDENTIFY_VEHICLE_REQUEST_HSFZ = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x11)
    private val IDENTIFY_VEHICLE_REQUEST_DOIP = byteArrayOf(
        0x02.toByte(), 0xFD.toByte(), 0x00.toByte(), 0x02.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()
    )

    suspend fun discover(timeoutMs: Int = 1800): List<DiscoveredGateway> = withContext(Dispatchers.IO) {
        val found = mutableListOf<DiscoveredGateway>()
        runCatching {
            DatagramSocket().use { socket ->
                socket.broadcast = true
                socket.soTimeout = timeoutMs
                val bcast = InetAddress.getByName("255.255.255.255")

                // Send both HSFZ (6811) and DoIP (13400) discovery probes
                socket.send(DatagramPacket(IDENTIFY_VEHICLE_REQUEST_HSFZ, IDENTIFY_VEHICLE_REQUEST_HSFZ.size, bcast, DISCOVERY_UDP_PORT))
                socket.send(DatagramPacket(IDENTIFY_VEHICLE_REQUEST_DOIP, IDENTIFY_VEHICLE_REQUEST_DOIP.size, bcast, DOIP_UDP_PORT))

                val rxBuffer = ByteArray(512)
                val rxPacket = DatagramPacket(rxBuffer, rxBuffer.size)
                val deadline = System.currentTimeMillis() + timeoutMs
                while (System.currentTimeMillis() < deadline) {
                    try {
                        socket.receive(rxPacket)
                        val port = rxPacket.port
                        val parsed = parseAnnouncement(
                            ipAddress = rxPacket.address.hostAddress ?: continue,
                            payload = rxPacket.data.copyOf(rxPacket.length),
                            port = if (port == DOIP_UDP_PORT) DoIpProtocol.DEFAULT_DOIP_PORT else TcpEnetTransportAdapter.DEFAULT_HSFZ_PORT
                        )
                        if (parsed != null && found.none { it.ipAddress == parsed.ipAddress }) {
                            found.add(parsed)
                        }
                    } catch (_: Exception) {
                        break
                    }
                }
            }
        }
        found
    }

    internal fun parseAnnouncement(ipAddress: String, payload: ByteArray, port: Int = TcpEnetTransportAdapter.DEFAULT_HSFZ_PORT): DiscoveredGateway? {
        if (payload.size < 6) return null

        // Check if DoIP 0x0004 announcement
        if (payload.size >= DoIpProtocol.HEADER_SIZE && payload[0].toInt() == DoIpProtocol.PROTOCOL_VERSION) {
            val type = ((payload[2].toInt() and 0xFF) shl 8) or (payload[3].toInt() and 0xFF)
            if (type == DoIpProtocol.TYPE_VEHICLE_ANNOUNCEMENT) {
                val body = payload.copyOfRange(DoIpProtocol.HEADER_SIZE, payload.size)
                val (vin, _, _) = DoIpProtocol.parseVehicleAnnouncement(body)
                return DiscoveredGateway(ipAddress = ipAddress, vin = vin, diagnosticPort = DoIpProtocol.DEFAULT_DOIP_PORT)
            }
        }

        val ascii = String(payload, StandardCharsets.US_ASCII)
        val vinRegex = Regex("[A-HJ-NPR-Z0-9]{17}")
        val vin = vinRegex.find(ascii)?.value
        return DiscoveredGateway(ipAddress = ipAddress, vin = vin, diagnosticPort = port)
    }
}
