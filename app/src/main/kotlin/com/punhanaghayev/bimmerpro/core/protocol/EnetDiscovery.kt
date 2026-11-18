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
    private val IDENTIFY_VEHICLE_REQUEST = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x11)

    suspend fun discover(timeoutMs: Int = 1800): List<DiscoveredGateway> = withContext(Dispatchers.IO) {
        val found = mutableListOf<DiscoveredGateway>()
        runCatching {
            DatagramSocket().use { socket ->
                socket.broadcast = true
                socket.soTimeout = timeoutMs
                val packet = DatagramPacket(
                    IDENTIFY_VEHICLE_REQUEST,
                    IDENTIFY_VEHICLE_REQUEST.size,
                    InetAddress.getByName("255.255.255.255"),
                    DISCOVERY_UDP_PORT
                )
                socket.send(packet)

                val rxBuffer = ByteArray(512)
                val rxPacket = DatagramPacket(rxBuffer, rxBuffer.size)
                val deadline = System.currentTimeMillis() + timeoutMs
                while (System.currentTimeMillis() < deadline) {
                    try {
                        socket.receive(rxPacket)
                        val parsed = parseAnnouncement(
                            ipAddress = rxPacket.address.hostAddress ?: continue,
                            payload = rxPacket.data.copyOf(rxPacket.length)
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

    internal fun parseAnnouncement(ipAddress: String, payload: ByteArray): DiscoveredGateway? {
        if (payload.size < 6) return null
        val ascii = String(payload, StandardCharsets.US_ASCII)
        val vinRegex = Regex("[A-HJ-NPR-Z0-9]{17}")
        val vin = vinRegex.find(ascii)?.value
        return DiscoveredGateway(ipAddress = ipAddress, vin = vin)
    }
}
