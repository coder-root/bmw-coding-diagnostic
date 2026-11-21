package com.punhanaghayev.bimmerpro.core.protocol

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.nio.charset.StandardCharsets
import java.util.UUID

data class BleAdapterDevice(
    val name: String,
    val address: String,
    val rssi: Int
)

/**
 * Physical BLE OBD-II transport for OBDLink CX / MX+, UniCarScan UCSI-2100, and STN2120/ELM327 BLE adapters.
 * Translates HSFZ-wrapped UDS frames into AT/ISO-TP commands over BLE GATT UART characteristics.
 */
class BleObdTransportAdapter(
    private val context: Context,
    private val deviceAddress: String,
    override val adapterType: AdapterType = AdapterType.BLE_OBD
) : TransportAdapter {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var gatt: BluetoothGatt? = null
    private var writeChar: BluetoothGattCharacteristic? = null
    private var responseDeferred: CompletableDeferred<String>? = null
    private val rxAccumulator = StringBuilder()

    @SuppressLint("MissingPermission")
    override suspend fun connect() = withContext(Dispatchers.IO) {
        _connectionState.value = ConnectionState.Connecting("BLE OBD ($deviceAddress)")
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter: BluetoothAdapter? = manager?.adapter
        if (adapter == null || !adapter.isEnabled) {
            val msg = "Bluetooth kapalı veya cihaz desteklemiyor."
            _connectionState.value = ConnectionState.Error(msg)
            throw IllegalStateException(msg)
        }

        val device: BluetoothDevice = adapter.getRemoteDevice(deviceAddress)
        val readyDeferred = CompletableDeferred<Unit>()

        val callback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                    g.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (!readyDeferred.isCompleted) {
                        readyDeferred.completeExceptionally(IllegalStateException("BLE bağlantısı kesildi (status=$status)"))
                    }
                    _connectionState.value = ConnectionState.Disconnected
                }
            }

            override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    readyDeferred.completeExceptionally(IllegalStateException("GATT servis keşfi başarısız ($status)"))
                    return
                }
                val uartPair = findUartCharacteristics(g)
                if (uartPair == null) {
                    readyDeferred.completeExceptionally(IllegalStateException("Uyumlu OBD BLE UART servisi bulunamadı."))
                    return
                }
                writeChar = uartPair.first
                val notifyChar = uartPair.second
                g.setCharacteristicNotification(notifyChar, true)
                val cccd = notifyChar.getDescriptor(CCCD_UUID)
                if (cccd != null) {
                    @Suppress("DEPRECATION")
                    cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    g.writeDescriptor(cccd)
                }
                readyDeferred.complete(Unit)
            }

            @Deprecated("Deprecated in Android 13, kept for minSdk 26 compatibility")
            override fun onCharacteristicChanged(g: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                @Suppress("DEPRECATION")
                val chunk = characteristic.value?.toString(StandardCharsets.US_ASCII) ?: return
                rxAccumulator.append(chunk)
                if (rxAccumulator.contains(">")) {
                    val full = rxAccumulator.toString()
                    rxAccumulator.clear()
                    responseDeferred?.complete(full)
                }
            }
        }

        gatt = device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
        withTimeout(8000) { readyDeferred.await() }

        // Initialize STN/ELM327 for BMW 500kbps CAN + ISO 15765-4 with ISO-TP framing
        sendAtCommand("ATZ")
        val chipId = sendAtCommand("STI")
        val isStn = chipId.contains("STN", ignoreCase = true) || chipId.contains("UniCar", ignoreCase = true)
        sendAtCommand("ATE0")
        sendAtCommand("ATL0")
        sendAtCommand("ATS0")
        sendAtCommand("ATH1")
        sendAtCommand("ATSP6")
        sendAtCommand("ATCAF1")
        sendAtCommand("ATAL")
        sendAtCommand("ATAT2")
        if (isStn) {
            sendAtCommand("STCSEGR 1")
        }

        _connectionState.value = ConnectionState.Connected(adapterType, deviceAddress)
    }

    @SuppressLint("MissingPermission")
    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        runCatching { gatt?.disconnect() }
        runCatching { gatt?.close() }
        gatt = null
        writeChar = null
        _connectionState.value = ConnectionState.Disconnected
    }

    override suspend fun transceive(frame: ByteArray): ByteArray = withContext(Dispatchers.IO) {
        val hsfz = HsfzFrame.decode(frame)
        val targetEcu = hsfz.targetAddress and 0xFF
        sendAtCommand("ATSH6F1")
        sendAtCommand("ATCEA%02X".format(targetEcu))
        sendAtCommand("ATCRA6%02X".format(targetEcu))

        val rawCombined = if (hsfz.payload.size <= 7) {
            val hexPayload = hsfz.payload.joinToString("") { "%02X".format(it) }
            val firstResp = sendAtCommand(hexPayload)
            if (ElmIsoTpHelper.isFirstFrameResponse(firstResp)) {
                val fcHex = IsoTpCodec.flowControlFrame().take(3).joinToString("") { "%02X".format(it) }
                val cfResp = sendAtCommand(fcHex)
                "$firstResp\n$cfResp"
            } else {
                firstResp
            }
        } else {
            // Long UDS write / coding payload segmented via IsoTpCodec (FF + CF)
            sendAtCommand("ATCAF0")
            var lastResp = ""
            try {
                IsoTpCodec.segment(hsfz.payload).forEach { seg ->
                    val segHex = seg.joinToString("") { "%02X".format(it) }
                    lastResp = sendAtCommand(segHex)
                }
            } finally {
                sendAtCommand("ATCAF1")
            }
            lastResp
        }

        val parsedUds = ElmIsoTpHelper.parseUdsResponse(rawCombined)
        HsfzFrame(
            controlWord = HsfzFrame.CONTROL_DIAGNOSTIC_DATA,
            sourceAddress = targetEcu,
            targetAddress = HsfzFrame.TESTER_ADDRESS,
            payload = parsedUds
        ).encode()
    }

    @SuppressLint("MissingPermission")
    private suspend fun sendAtCommand(cmd: String): String {
        val g = gatt ?: error("BLE GATT bağlı değil")
        val ch = writeChar ?: error("BLE Write karakteristiği yok")
        val deferred = CompletableDeferred<String>()
        responseDeferred = deferred
        rxAccumulator.clear()
        val bytes = "$cmd\r".toByteArray(StandardCharsets.US_ASCII)
        @Suppress("DEPRECATION")
        ch.value = bytes
        @Suppress("DEPRECATION")
        g.writeCharacteristic(ch)
        return withTimeout(3500) { deferred.await() }
    }

    private fun findUartCharacteristics(g: BluetoothGatt): Pair<BluetoothGattCharacteristic, BluetoothGattCharacteristic>? {
        for ((serviceUuid, txUuid, rxUuid) in KNOWN_BLE_PROFILES) {
            val service = g.getService(serviceUuid) ?: continue
            val write = service.getCharacteristic(txUuid)
            val notify = service.getCharacteristic(rxUuid)
            if (write != null && notify != null) return write to notify
        }
        return null
    }

    companion object {
        private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        private val KNOWN_BLE_PROFILES = listOf(
            // Standard ELM327 / OBDLink / UniCarScan FFF0 profile
            Triple(
                UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"),
                UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb"),
                UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")
            ),
            // Nordic UART (NUS) profile
            Triple(
                UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"),
                UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e"),
                UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
            )
        )

        internal fun parseElmHexPayload(raw: String): ByteArray =
            ElmIsoTpHelper.parseUdsResponse(raw)

        @SuppressLint("MissingPermission")
        suspend fun scanNearbyObdAdapters(context: Context, durationMs: Long = 2500): List<BleAdapterDevice> =
            withContext(Dispatchers.IO) {
                val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                val scanner = manager?.adapter?.bluetoothLeScanner ?: return@withContext emptyList()
                val found = linkedMapOf<String, BleAdapterDevice>()
                val cb = object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult) {
                        val name = result.device.name ?: return
                        if (name.contains("OBD", true) || name.contains("UniCar", true) || name.contains("Vgate", true) || name.contains("Bimmer", true)) {
                            found[result.device.address] = BleAdapterDevice(name, result.device.address, result.rssi)
                        }
                    }
                }
                runCatching {
                    scanner.startScan(cb)
                    delay(durationMs)
                    scanner.stopScan(cb)
                }
                found.values.toList()
            }
    }
}
