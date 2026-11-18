package com.punhanaghayev.bimmerpro.core.protocol

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class UsbKdcanDeviceInfo(
    val deviceName: String,
    val vendorId: Int,
    val productId: Int,
    val productName: String
)

/**
 * Android USB Host transport for BMW E-Series K+DCAN cables (FTDI FT232R / CH340).
 * Wraps UDS / KWP2000 diagnostic frames for E60/E90/E70 vehicles.
 */
class UsbKdcanTransportAdapter(
    private val context: Context,
    override val adapterType: AdapterType = AdapterType.USB_KDCAN
) : TransportAdapter {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var usbConnection: UsbDeviceConnection? = null
    private var inEndpoint: UsbEndpoint? = null
    private var outEndpoint: UsbEndpoint? = null

    override suspend fun connect() = withContext(Dispatchers.IO) {
        _connectionState.value = ConnectionState.Connecting("USB K+DCAN Kablo")
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
            ?: error("USB Host servisi bulunamadı")

        val device = findSupportedKdcanDevice(usbManager)
            ?: throw IllegalStateException("Takılı K+DCAN USB kablosu bulunamadı (FTDI/CH340).")

        if (!usbManager.hasPermission(device)) {
            throw IllegalStateException("USB cihaz erişim izni verilmedi: ${device.productName ?: device.deviceName}")
        }

        val usbInterface = device.getInterface(0)
        val conn = usbManager.openDevice(device)
            ?: throw IllegalStateException("USB cihaz açılamadı.")

        conn.claimInterface(usbInterface, true)

        for (i in 0 until usbInterface.endpointCount) {
            val ep = usbInterface.getEndpoint(i)
            if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.direction == UsbConstants.USB_DIR_IN) inEndpoint = ep else outEndpoint = ep
            }
        }

        // FTDI 115200 baud configuration control transfer (for FT232R K+DCAN)
        if (device.vendorId == VENDOR_FTDI) {
            conn.controlTransfer(0x40, 0x00, 0x0000, 0, null, 0, 500) // Reset
            conn.controlTransfer(0x40, 0x03, 0x001A, 0, null, 0, 500) // 115200 baud
        }

        usbConnection = conn
        _connectionState.value = ConnectionState.Connected(
            adapterType,
            "USB ${device.productName ?: device.deviceName}"
        )
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        runCatching { usbConnection?.close() }
        usbConnection = null
        inEndpoint = null
        outEndpoint = null
        _connectionState.value = ConnectionState.Disconnected
    }

    override suspend fun transceive(frame: ByteArray): ByteArray = withContext(Dispatchers.IO) {
        val conn = usbConnection ?: error("USB K+DCAN bağlı değil")
        val outEp = outEndpoint ?: error("USB OUT endpoint yok")
        val inEp = inEndpoint ?: error("USB IN endpoint yok")

        val hsfz = HsfzFrame.decode(frame)
        val kwpPacket = encodeKwpFrame(hsfz.targetAddress, hsfz.payload)
        val sent = conn.bulkTransfer(outEp, kwpPacket, kwpPacket.size, 2000)
        if (sent < 0) error("USB K+DCAN gönderim hatası")

        val rxBuf = ByteArray(256)
        val read = conn.bulkTransfer(inEp, rxBuf, rxBuf.size, 2500)
        if (read <= 2) error("ECU cevap vermedi (USB K+DCAN timeout)")

        // Strip FTDI 2-byte modem status header if present
        val payloadStart = if (read > 4) 2 else 0
        val udsPayload = rxBuf.copyOfRange(payloadStart, read)

        HsfzFrame(
            controlWord = HsfzFrame.CONTROL_DIAGNOSTIC_DATA,
            sourceAddress = hsfz.targetAddress,
            targetAddress = HsfzFrame.TESTER_ADDRESS,
            payload = udsPayload
        ).encode()
    }

    companion object {
        private const val VENDOR_FTDI = 0x0403
        private const val VENDOR_WCH_CH340 = 0x1A86

        internal fun encodeKwpFrame(targetEcu: Int, udsPayload: ByteArray): ByteArray {
            val header = byteArrayOf(
                (0x80 or udsPayload.size).toByte(),
                targetEcu.toByte(),
                0xF1.toByte()
            )
            val body = header + udsPayload
            val checksum = body.sumOf { it.toInt() and 0xFF } and 0xFF
            return body + byteArrayOf(checksum.toByte())
        }

        fun scanUsbDevices(context: Context): List<UsbKdcanDeviceInfo> {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return emptyList()
            return usbManager.deviceList.values.map { dev ->
                UsbKdcanDeviceInfo(
                    deviceName = dev.deviceName,
                    vendorId = dev.vendorId,
                    productId = dev.productId,
                    productName = dev.productName ?: "USB Serial (${dev.vendorId})"
                )
            }
        }

        private fun findSupportedKdcanDevice(usbManager: UsbManager): UsbDevice? =
            usbManager.deviceList.values.firstOrNull {
                it.vendorId == VENDOR_FTDI || it.vendorId == VENDOR_WCH_CH340
            } ?: usbManager.deviceList.values.firstOrNull()
    }
}
