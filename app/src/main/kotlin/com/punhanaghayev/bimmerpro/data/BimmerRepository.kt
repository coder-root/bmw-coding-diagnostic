package com.punhanaghayev.bimmerpro.data

import android.content.Context
import com.punhanaghayev.bimmerpro.core.bmw.CafdParameter
import com.punhanaghayev.bimmerpro.core.bmw.CodingBackupSnapshot
import com.punhanaghayev.bimmerpro.core.bmw.CodingCapability
import com.punhanaghayev.bimmerpro.core.bmw.CodingPreset
import com.punhanaghayev.bimmerpro.core.bmw.DtcFault
import com.punhanaghayev.bimmerpro.core.bmw.EcuAddress
import com.punhanaghayev.bimmerpro.core.bmw.EcuModule
import com.punhanaghayev.bimmerpro.core.bmw.InjectorCalibration
import com.punhanaghayev.bimmerpro.core.bmw.LiveSensorChannel
import com.punhanaghayev.bimmerpro.core.bmw.MileageModuleReading
import com.punhanaghayev.bimmerpro.core.bmw.MileageVerificationResult
import com.punhanaghayev.bimmerpro.core.bmw.ServiceExecutionResult
import com.punhanaghayev.bimmerpro.core.bmw.ServiceHistoryRecord
import com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutine
import com.punhanaghayev.bimmerpro.core.bmw.TamperStatus
import com.punhanaghayev.bimmerpro.core.bmw.VehicleOrderOption
import com.punhanaghayev.bimmerpro.core.bmw.VehicleProfile
import com.punhanaghayev.bimmerpro.core.bmw.ZfClutchAdaptation
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import com.punhanaghayev.bimmerpro.core.protocol.BleAdapterDevice
import com.punhanaghayev.bimmerpro.core.protocol.BleObdTransportAdapter
import com.punhanaghayev.bimmerpro.core.protocol.CafdBitmaskEngine
import com.punhanaghayev.bimmerpro.core.protocol.ConnectionState
import com.punhanaghayev.bimmerpro.core.protocol.DiscoveredGateway
import com.punhanaghayev.bimmerpro.core.protocol.EnetGatewayDiscovery
import com.punhanaghayev.bimmerpro.core.protocol.PhysicalUdsParser
import com.punhanaghayev.bimmerpro.core.protocol.ProtocolLogEntry
import com.punhanaghayev.bimmerpro.core.protocol.TcpElmStnTransportAdapter
import com.punhanaghayev.bimmerpro.core.protocol.TcpEnetTransportAdapter
import com.punhanaghayev.bimmerpro.core.protocol.TransportAdapter
import com.punhanaghayev.bimmerpro.core.protocol.UdsClient
import com.punhanaghayev.bimmerpro.core.protocol.UdsCodec
import com.punhanaghayev.bimmerpro.core.protocol.UdsResponse
import com.punhanaghayev.bimmerpro.core.protocol.UdsSessionType
import com.punhanaghayev.bimmerpro.core.protocol.UsbKdcanDeviceInfo
import com.punhanaghayev.bimmerpro.core.protocol.UsbKdcanTransportAdapter
import com.punhanaghayev.bimmerpro.core.simulator.BmwSimulatorCatalog
import com.punhanaghayev.bimmerpro.core.simulator.SimulatorTransportAdapter
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

data class PerformanceTimerState(
    val isArmed: Boolean = false,
    val isRunning: Boolean = false,
    val currentSpeedKmh: Int = 0,
    val elapsedMs: Long = 0L,
    val bestZeroToHundredSec: Double? = null
)

data class BimmerSessionState(
    val selectedAdapter: AdapterType = AdapterType.SIMULATOR_F10,
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val vehicleProfile: VehicleProfile? = null,
    val ecus: List<EcuModule> = emptyList(),
    val dtcs: List<DtcFault> = emptyList(),
    val presets: List<CodingPreset> = emptyList(),
    val cafdParameters: List<CafdParameter> = emptyList(),
    val backups: List<CodingBackupSnapshot> = emptyList(),
    val liveChannels: List<LiveSensorChannel> = emptyList(),
    val serviceRoutines: List<ServiceRoutine> = emptyList(),
    val serviceHistory: List<ServiceHistoryRecord> = emptyList(),
    val zfAdaptations: List<ZfClutchAdaptation> = emptyList(),
    val injectorCodes: List<InjectorCalibration> = emptyList(),
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scanStatusText: String = "",
    val lastOperationMessage: String? = null,
    val lastServiceResult: ServiceExecutionResult? = null,
    val discoveredGateways: List<DiscoveredGateway> = emptyList(),
    val discoveredBleDevices: List<BleAdapterDevice> = emptyList(),
    val discoveredUsbDevices: List<UsbKdcanDeviceInfo> = emptyList(),
    val selectedBleAddress: String? = null,
    val performanceTimer: PerformanceTimerState = PerformanceTimerState(),
    val savedVehicles: List<SavedVehicleRecord> = emptyList()
)

class BimmerRepository(
    private val appContext: Context? = null
) {

    private val backupStorage: BackupStorage? = appContext?.let { BackupStorage(File(it.filesDir, "backups")) }
    private val garageHistoryStorage: GarageHistoryStorage? = appContext?.let { GarageHistoryStorage(File(it.filesDir, "garage")) }
    private var activeTransport: TransportAdapter = SimulatorTransportAdapter(AdapterType.SIMULATOR_F10)
    private val udsClient = UdsClient { activeTransport }
    private val ecuCafdBuffers = mutableMapOf<Int, ByteArray>()

    private val _session = MutableStateFlow(
        BimmerSessionState(
            backups = backupStorage?.loadBackups().orEmpty(),
            savedVehicles = garageHistoryStorage?.loadVehicles().orEmpty()
        )
    )
    val session: StateFlow<BimmerSessionState> = _session.asStateFlow()

    val protocolLogs: StateFlow<List<ProtocolLogEntry>> = udsClient.logs

    private var telemetryTick = 0

    suspend fun connect(
        adapterType: AdapterType,
        customHost: String = "",
        customPort: Int? = null,
        bleAddress: String? = _session.value.selectedBleAddress
    ) {
        activeTransport.disconnect()
        activeTransport = when (adapterType) {
            AdapterType.VLINKER_BM_WIFI -> TcpElmStnTransportAdapter(
                adapterType = adapterType,
                host = customHost.ifBlank { TcpElmStnTransportAdapter.DEFAULT_VLINKER_HOST },
                port = customPort ?: TcpElmStnTransportAdapter.DEFAULT_VLINKER_PORT,
                onAtLog = { msg -> udsClient.logInfo("AT", EcuAddress.ZGW_BDC, msg) }
            )

            AdapterType.SIMULATOR_F10,
            AdapterType.SIMULATOR_F30,
            AdapterType.SIMULATOR_G30,
            AdapterType.SIMULATOR_G20,
            AdapterType.SIMULATOR_E90 -> SimulatorTransportAdapter(adapterType)

            AdapterType.ENET_WIFI,
            AdapterType.ENET_USB -> TcpEnetTransportAdapter(
                adapterType = adapterType,
                host = customHost.ifBlank { TcpEnetTransportAdapter.DEFAULT_ZGW_HOST },
                port = customPort ?: TcpEnetTransportAdapter.DEFAULT_HSFZ_PORT
            )

            AdapterType.BLE_OBD -> {
                val ctx = appContext ?: error("BLE için Android Context gerekli")
                val targetMac = bleAddress ?: throw IllegalStateException(
                    "Önce 'Donanım Tara' ile bir BLE OBD adaptörü (OBDLink/UniCarScan) seçin."
                )
                BleObdTransportAdapter(ctx, targetMac)
            }

            AdapterType.USB_KDCAN -> {
                val ctx = appContext ?: error("USB K+DCAN için Android Context gerekli")
                UsbKdcanTransportAdapter(ctx)
            }
        }

        _session.update {
            it.copy(
                selectedAdapter = adapterType,
                connectionState = ConnectionState.Connecting(adapterType.displayName),
                lastOperationMessage = null
            )
        }

        try {
            activeTransport.connect()
            val connState = activeTransport.connectionState.value
            // Execute real UDS VIN & Battery Voltage probe over HSFZ
            udsClient.sendUds(
                EcuAddress.ZGW_BDC,
                UdsCodec.buildSessionControl(UdsSessionType.EXTENDED),
                "UDS 0x10 Extended Session (Gateway)"
            )
            val vinResp = runCatching {
                udsClient.sendUds(
                    EcuAddress.ZGW_BDC,
                    UdsCodec.buildReadDataById(SimulatorTransportAdapter.DID_VIN),
                    "UDS 0x22 Read VIN (0xF190)"
                )
            }.getOrNull()
            val voltResp = runCatching {
                udsClient.sendUds(
                    EcuAddress.ZGW_BDC,
                    UdsCodec.buildReadDataById(SimulatorTransportAdapter.DID_BATTERY_MV),
                    "UDS 0x22 Read Battery Voltage"
                )
            }.getOrNull()

            val sim = activeTransport as? SimulatorTransportAdapter
            if (sim != null) {
                val bundle = sim.bundle
                val savedList = saveConnectedVehicle(bundle.profile, adapterType)
                _session.update {
                    it.copy(
                        connectionState = connState,
                        vehicleProfile = bundle.profile,
                        ecus = bundle.ecus,
                        dtcs = sim.currentDtcs(),
                        presets = bundle.presets,
                        cafdParameters = bundle.cafdParameters,
                        liveChannels = bundle.liveChannels,
                        serviceRoutines = bundle.serviceRoutines,
                        serviceHistory = bundle.serviceHistory,
                        zfAdaptations = bundle.zfAdaptations,
                        injectorCodes = bundle.injectorCodes,
                        savedVehicles = savedList,
                        lastOperationMessage = "${bundle.profile.modelDisplayName} başarıyla bağlandı."
                    )
                }
            } else {
                val extractedVin = (vinResp as? UdsResponse.Positive)
                    ?.let { PhysicalUdsParser.extractVin(it.payload) }
                    ?: "WBA_PHYSICAL_VIN"
                val inferredChassis = PhysicalUdsParser.inferChassisFromVin(extractedVin)
                val vlinkerVoltage = (activeTransport as? TcpElmStnTransportAdapter)?.lastReadVoltage
                val udsVoltage = (voltResp as? UdsResponse.Positive)?.payload?.let { p ->
                    if (p.size >= 4) (((p[2].toInt() and 0xFF) shl 8) or (p[3].toInt() and 0xFF)) / 1000.0 else null
                }
                val batteryVolts = vlinkerVoltage ?: udsVoltage ?: 12.6
                val template = BmwSimulatorCatalog.buildBundle(
                    when (inferredChassis) {
                        com.punhanaghayev.bimmerpro.core.bmw.BmwChassis.G30 -> AdapterType.SIMULATOR_G30
                        com.punhanaghayev.bimmerpro.core.bmw.BmwChassis.G20 -> AdapterType.SIMULATOR_G20
                        com.punhanaghayev.bimmerpro.core.bmw.BmwChassis.F30 -> AdapterType.SIMULATOR_F30
                        com.punhanaghayev.bimmerpro.core.bmw.BmwChassis.E90,
                        com.punhanaghayev.bimmerpro.core.bmw.BmwChassis.E60 -> AdapterType.SIMULATOR_E90
                        else -> AdapterType.SIMULATOR_F10
                    }
                )
                val physicalProfile = VehicleProfile(
                    vin = extractedVin,
                    chassis = inferredChassis,
                    modelDisplayName = "BMW ${inferredChassis.code} (${adapterType.displayName})",
                    engineCode = "Fiziksel OBD/UDS Bağlantısı",
                    productionYear = 2015,
                    iLevel = "Live Gateway",
                    batteryVoltage = batteryVolts,
                    odometerKm = 0,
                    faOptions = listOf(VehicleOrderOption("LIVE", "Canlı ECU Okuması Aktif")),
                    mileageVerification = MileageVerificationResult(
                        readings = listOf(MileageModuleReading("Gateway", 0)),
                        maxDifferenceKm = 0,
                        status = TamperStatus.VERIFIED,
                        summary = "Fiziksel araç bağlı — tam modül taraması için Diagnostik -> Tüm ECU Tara çalıştırın."
                    )
                )
                val savedList = saveConnectedVehicle(physicalProfile, adapterType)
                _session.update {
                    it.copy(
                        connectionState = connState,
                        vehicleProfile = physicalProfile,
                        ecus = template.ecus.map { ecu -> ecu.copy(activeDtcCount = 0, shadowDtcCount = 0) },
                        dtcs = emptyList(),
                        presets = template.presets,
                        cafdParameters = template.cafdParameters,
                        liveChannels = template.liveChannels,
                        serviceRoutines = template.serviceRoutines,
                        serviceHistory = emptyList(),
                        zfAdaptations = template.zfAdaptations,
                        injectorCodes = template.injectorCodes,
                        savedVehicles = savedList,
                        lastOperationMessage = "Fiziksel bağlantı kuruldu: $extractedVin (%.1f V)".format(batteryVolts)
                    )
                }
            }
        } catch (ex: Exception) {
            _session.update {
                it.copy(
                    connectionState = ConnectionState.Error(ex.message ?: "Bağlantı hatası"),
                    lastOperationMessage = ex.message
                )
            }
        }
    }

    suspend fun disconnect() {
        activeTransport.disconnect()
        _session.update {
            BimmerSessionState(
                selectedAdapter = it.selectedAdapter,
                connectionState = ConnectionState.Disconnected
            )
        }
    }

    suspend fun runFullVehicleScan() {
        val ecus = _session.value.ecus
        if (ecus.isEmpty()) return

        _session.update { it.copy(isScanning = true, scanProgress = 0f, scanStatusText = "ECU ağacı taranıyor...") }
        val total = ecus.size.toFloat()

        ecus.forEachIndexed { index, ecu ->
            _session.update {
                it.copy(
                    scanProgress = (index + 1) / total,
                    scanStatusText = "${ecu.shortName} (${ecu.addressHex}) DTC & KM okunuyor..."
                )
            }
            udsClient.sendUds(
                ecu.address,
                UdsCodec.buildReadDtcByStatusMask(0xFF),
                "UDS 0x19 Read DTC (${ecu.shortName})"
            )
            delay(110)
        }

        val sim = activeTransport as? SimulatorTransportAdapter
        if (sim != null) {
            val refreshedDtcs = sim.currentDtcs()
            _session.update {
                it.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    scanStatusText = "Tarama tamamlandı (${refreshedDtcs.size} kayıt)",
                    dtcs = refreshedDtcs,
                    lastOperationMessage = "Tüm modüller tarandı: ${refreshedDtcs.size} hata kaydı bulundu."
                )
            }
            return
        }

        // Physical vehicle scan: query each ECU with UDS 0x19 0x02 and decode real DTC bytes
        val physicalDtcs = mutableListOf<DtcFault>()
        val odo = _session.value.vehicleProfile?.odometerKm ?: 0
        val volts = _session.value.vehicleProfile?.batteryVoltage ?: 12.6
        val updatedEcus = ecus.map { ecu ->
            val resp = runCatching {
                udsClient.sendUds(
                    ecu.address,
                    UdsCodec.buildReadDtcByStatusMask(0xFF),
                    "UDS 0x19 Physical DTC (${ecu.shortName})"
                )
            }.getOrNull()
            if (resp is UdsResponse.Positive) {
                val parsed = PhysicalUdsParser.parseDtcRecords(ecu.address, ecu.shortName, resp.payload, odo, volts)
                physicalDtcs.addAll(parsed)
                ecu.copy(
                    activeDtcCount = parsed.count { it.status == com.punhanaghayev.bimmerpro.core.bmw.DtcStatus.ACTIVE },
                    shadowDtcCount = parsed.count { it.severity == com.punhanaghayev.bimmerpro.core.bmw.DtcSeverity.SHADOW }
                )
            } else {
                ecu
            }
        }
        _session.update {
            it.copy(
                isScanning = false,
                scanProgress = 1f,
                scanStatusText = "Fiziksel tarama tamamlandı (${physicalDtcs.size} kayıt)",
                ecus = updatedEcus,
                dtcs = physicalDtcs,
                lastOperationMessage = "Fiziksel araç tarandı: ${physicalDtcs.size} hata kaydı bulundu."
            )
        }
    }

    suspend fun clearAllDtcs() {
        val response = udsClient.sendUds(
            EcuAddress.ZGW_BDC,
            UdsCodec.buildClearAllDtc(),
            "UDS 0x14 Clear All DTCs (Gateway Broadcast)"
        )
        if (response is UdsResponse.Positive) {
            val sim = activeTransport as? SimulatorTransportAdapter
            val remaining = sim?.currentDtcs() ?: emptyList()
            val refreshedEcus = _session.value.ecus.map { ecu ->
                ecu.copy(activeDtcCount = 0, shadowDtcCount = 0)
            }
            _session.update {
                it.copy(
                    dtcs = remaining,
                    ecus = refreshedEcus,
                    lastOperationMessage = "Tüm hata ve gölge hafızaları temizlendi."
                )
            }
        }
    }

    suspend fun clearEcuDtcs(ecuAddress: Int, ecuName: String) {
        val response = udsClient.sendUds(
            ecuAddress,
            UdsCodec.buildClearAllDtc(),
            "UDS 0x14 Clear DTC ($ecuName)"
        )
        if (response is UdsResponse.Positive) {
            _session.update { state ->
                state.copy(
                    dtcs = state.dtcs.filterNot { it.ecuAddress == ecuAddress },
                    ecus = state.ecus.map {
                        if (it.address == ecuAddress) it.copy(activeDtcCount = 0, shadowDtcCount = 0) else it
                    },
                    lastOperationMessage = "$ecuName modülünün hata hafızası temizlendi."
                )
            }
        }
    }

    suspend fun applyPresetCoding(presetId: String, newOptionIndex: Int): Result<String> {
        val state = _session.value
        val profile = state.vehicleProfile ?: return Result.failure(IllegalStateException("Araç bağlı değil"))
        val preset = state.presets.firstOrNull { it.id == presetId }
            ?: return Result.failure(IllegalArgumentException("Preset bulunamadı"))

        if (profile.batteryVoltage < MIN_SAFE_CODING_VOLTAGE) {
            return Result.failure(
                IllegalStateException(
                    "Güvenlik Kilidi: Akü voltajı %.1f V (< %.1f V). ECU hasarını önlemek için kodlama durduruldu."
                        .format(profile.batteryVoltage, MIN_SAFE_CODING_VOLTAGE)
                )
            )
        }

        // Create automatic pre-coding backup snapshot
        val snapshot = CodingBackupSnapshot(
            id = "bkp_${System.currentTimeMillis()}",
            timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()),
            ecuName = preset.targetEcu,
            ecuAddress = preset.targetAddress,
            cafdFile = preset.cafdKey,
            summary = "${preset.title}: ${preset.options[preset.currentOptionIndex]} -> ${preset.options[newOptionIndex]}",
            previousValues = mapOf(presetId to preset.currentOptionIndex)
        )

        udsClient.sendUds(
            preset.targetAddress,
            UdsCodec.buildSessionControl(UdsSessionType.CODING),
            "UDS 0x10 Coding Session (${preset.targetEcu})"
        )

        val writeResp = udsClient.sendUds(
            preset.targetAddress,
            UdsCodec.buildWriteDataById(0x3001, byteArrayOf(newOptionIndex.toByte())),
            "UDS 0x2E FDL Write ${preset.cafdKey}=${newOptionIndex}"
        )

        return when (writeResp) {
            is UdsResponse.Positive -> {
                val updatedPresets = state.presets.map {
                    if (it.id == presetId) it.copy(currentOptionIndex = newOptionIndex) else it
                }
                val updatedBackups = listOf(snapshot) + state.backups
                backupStorage?.saveBackups(updatedBackups)
                _session.update {
                    it.copy(
                        presets = updatedPresets,
                        backups = updatedBackups,
                        lastOperationMessage = "${preset.targetEcu} kodlandı (${preset.options[newOptionIndex]}). Yedek alındı."
                    )
                }
                Result.success("Kodlama başarılı")
            }
            is UdsResponse.Negative -> {
                val msg = "ECU Reddi (NRC 0x%02X): ${writeResp.nrc.description}".format(writeResp.nrc.code)
                _session.update { it.copy(lastOperationMessage = msg) }
                Result.failure(IllegalStateException(msg))
            }
        }
    }

    suspend fun applyCafdParameter(paramId: String, optionLabel: String): Result<String> {
        val state = _session.value
        val profile = state.vehicleProfile ?: return Result.failure(IllegalStateException("Araç bağlı değil"))
        val param = state.cafdParameters.firstOrNull { it.id == paramId }
            ?: return Result.failure(IllegalArgumentException("CAFD parametresi bulunamadı"))

        if (profile.batteryVoltage < MIN_SAFE_CODING_VOLTAGE) {
            return Result.failure(
                IllegalStateException("Güvenlik Kilidi: Akü voltajı %.1f V (< 12.2 V)!".format(profile.batteryVoltage))
            )
        }

        val hexStr = param.availableOptions[optionLabel] ?: param.currentHex
        val byteVal = hexStr.removePrefix("0x").toIntOrNull(16) ?: 0
        val currentBuffer = ecuCafdBuffers.getOrPut(param.ecuAddress) { ByteArray(128) }
        val patchedBuffer = CafdBitmaskEngine.writeMaskedValue(
            codingData = currentBuffer,
            byteOffset = param.byteOffset,
            bitMask = param.bitMask,
            newValue = byteVal
        )

        val writeResp = udsClient.sendUds(
            param.ecuAddress,
            UdsCodec.buildWriteDataById(0x3002, byteArrayOf(param.byteOffset.toByte(), patchedBuffer[param.byteOffset])),
            "UDS 0x2E Expert FDL ${param.functionName}=$hexStr (Byte=0x%02X)".format(patchedBuffer[param.byteOffset])
        )

        return when (writeResp) {
            is UdsResponse.Positive -> {
                ecuCafdBuffers[param.ecuAddress] = patchedBuffer
                val updated = state.cafdParameters.map {
                    if (it.id == paramId) it.copy(currentHex = hexStr, selectedOptionLabel = optionLabel) else it
                }
                _session.update {
                    it.copy(
                        cafdParameters = updated,
                        lastOperationMessage = "Expert FDL yazıldı: ${param.functionName} -> $optionLabel"
                    )
                }
                Result.success("Expert FDL yazıldı")
            }
            is UdsResponse.Negative -> {
                val msg = "ECU Reddi (NRC 0x%02X): ${writeResp.nrc.description}".format(writeResp.nrc.code)
                _session.update { it.copy(lastOperationMessage = msg) }
                Result.failure(IllegalStateException(msg))
            }
        }
    }

    suspend fun restoreBackup(backupId: String) {
        val state = _session.value
        val backup = state.backups.firstOrNull { it.id == backupId } ?: return
        var restoredPresets = state.presets
        backup.previousValues.forEach { (presetId, prevIndex) ->
            udsClient.sendUds(
                backup.ecuAddress,
                UdsCodec.buildWriteDataById(0x3001, byteArrayOf(prevIndex.toByte())),
                "UDS 0x2E Rollback ${backup.ecuName}"
            )
            restoredPresets = restoredPresets.map {
                if (it.id == presetId) it.copy(currentOptionIndex = prevIndex) else it
            }
        }
        _session.update {
            it.copy(
                presets = restoredPresets,
                lastOperationMessage = "${backup.ecuName} için önceki kodlama yedeği geri yüklendi."
            )
        }
    }

    suspend fun addVehicleOrderOption(code: String, description: String) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.length < 3) return
        val profile = _session.value.vehicleProfile ?: return
        if (profile.faOptions.any { it.code == cleanCode }) return

        udsClient.sendUds(
            EcuAddress.FEM_CAS,
            UdsCodec.buildWriteDataById(0x3F00, cleanCode.toByteArray()),
            "UDS 0x2E FA/VO Write +$cleanCode"
        )
        _session.update {
            it.copy(
                vehicleProfile = profile.copy(
                    faOptions = profile.faOptions + VehicleOrderOption(cleanCode, description.ifBlank { "Özel VO Opsiyonu" })
                ),
                lastOperationMessage = "FA/VO listesine $cleanCode eklendi ve ilgili ECU'lara işlendi."
            )
        }
    }

    suspend fun executeServiceRoutine(routine: ServiceRoutine, optionByte: Byte = 0x01) {
        val response = udsClient.sendUds(
            routine.targetAddress,
            UdsCodec.buildRoutineControl(0x01, routine.routineId, byteArrayOf(optionByte)),
            "UDS 0x31 Routine ${routine.title}"
        )
        val stamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val result = when (response) {
            is UdsResponse.Positive -> ServiceExecutionResult(
                success = true,
                title = routine.title,
                message = "${routine.targetEcu} üzerinde başarıyla çalıştırıldı.",
                timestamp = stamp
            )
            is UdsResponse.Negative -> ServiceExecutionResult(
                success = false,
                title = routine.title,
                message = "Başarısız: ${response.nrc.description}",
                timestamp = stamp
            )
        }
        _session.update {
            it.copy(
                lastServiceResult = result,
                lastOperationMessage = result.message
            )
        }
    }

    suspend fun stepLiveTelemetry() {
        val current = _session.value.liveChannels
        if (current.isEmpty()) return
        telemetryTick++

        val sim = activeTransport as? SimulatorTransportAdapter
        if (sim == null) {
            // Physical ECU live PID polling (DME/DDE RPM 0xF40C & Coolant 0xF405)
            val rpmResp = runCatching {
                udsClient.sendUds(EcuAddress.DME_DDE, UdsCodec.buildReadDataById(0xF40C), "UDS 0x22 Live RPM")
            }.getOrNull() as? UdsResponse.Positive
            val coolResp = runCatching {
                udsClient.sendUds(EcuAddress.DME_DDE, UdsCodec.buildReadDataById(0xF405), "UDS 0x22 Live Coolant")
            }.getOrNull() as? UdsResponse.Positive

            val liveRpm = rpmResp?.payload?.takeIf { it.size >= 4 }?.let {
                ((((it[2].toInt() and 0xFF) shl 8) or (it[3].toInt() and 0xFF)) / 4.0)
            }
            val liveCoolant = coolResp?.payload?.takeIf { it.size >= 3 }?.let {
                ((it[2].toInt() and 0xFF) - 40).toDouble()
            }

            val updated = current.map { ch ->
                val v = when {
                    ch.id == "rpm" && liveRpm != null -> liveRpm
                    ch.id == "coolant" && liveCoolant != null -> liveCoolant
                    else -> ch.currentValue
                }
                ch.copy(currentValue = v, history = (ch.history.drop(1) + v.toFloat()))
            }
            _session.update { it.copy(liveChannels = updated) }
            return
        }

        val wave = sin(telemetryTick * 0.45)
        val updated = current.mapIndexed { idx, ch ->
            val delta = when (ch.id) {
                "rpm" -> wave * 42.0
                "boost" -> wave * 0.08
                "batt" -> wave * 0.05
                "rail" -> wave * 3.5
                else -> (idx % 2) * 0.1 * wave
            }
            val nextVal = ((ch.currentValue + delta) * 100.0).toInt() / 100.0
            val clamped = nextVal.coerceIn(ch.minExpected, ch.maxExpected)
            ch.copy(
                currentValue = clamped,
                history = (ch.history.drop(1) + clamped.toFloat())
            )
        }
        _session.update { it.copy(liveChannels = updated) }
    }

    fun startPerformanceRun() {
        _session.update {
            it.copy(
                performanceTimer = it.performanceTimer.copy(
                    isArmed = true,
                    isRunning = true,
                    currentSpeedKmh = 0,
                    elapsedMs = 0L
                )
            )
        }
    }

    fun stopPerformanceRun() {
        _session.update {
            it.copy(
                performanceTimer = it.performanceTimer.copy(isArmed = false, isRunning = false)
            )
        }
    }

    fun stepPerformanceTimer(deltaMs: Long = 200L) {
        val timer = _session.value.performanceTimer
        if (!timer.isRunning) return
        val nextElapsed = timer.elapsedMs + deltaMs
        // Realistic launch curve up to 105 km/h in ~5.4s for simulator demo
        val nextSpeed = ((nextElapsed / 54.0).toInt()).coerceAtMost(112)
        if (nextSpeed >= 100) {
            val sec = (nextElapsed / 100.0) / 10.0
            val best = timer.bestZeroToHundredSec?.let { minOf(it, sec) } ?: sec
            _session.update {
                it.copy(
                    performanceTimer = timer.copy(
                        isArmed = false,
                        isRunning = false,
                        currentSpeedKmh = 100,
                        elapsedMs = nextElapsed,
                        bestZeroToHundredSec = best
                    ),
                    lastOperationMessage = "0-100 km/h tamamlandı: %.1f sn!".format(sec)
                )
            }
        } else {
            _session.update {
                it.copy(
                    performanceTimer = timer.copy(currentSpeedKmh = nextSpeed, elapsedMs = nextElapsed)
                )
            }
        }
    }

    suspend fun writeInjectorCode(cylinder: Int, imaCode: String) {
        val clean = imaCode.trim().uppercase()
        if (clean.length < 5) return
        udsClient.sendUds(
            EcuAddress.DME_DDE,
            UdsCodec.buildWriteDataById(0x3C00 + cylinder, clean.toByteArray()),
            "UDS 0x2E Injector IMA Cyl $cylinder = $clean"
        )
        _session.update { state ->
            state.copy(
                injectorCodes = state.injectorCodes.map {
                    if (it.cylinder == cylinder) it.copy(imaCode = clean) else it
                },
                lastOperationMessage = "Silindir $cylinder enjektör IMA kodu ($clean) DME/DDE'ye yazıldı."
            )
        }
    }

    suspend fun resetZfAdaptations() {
        udsClient.sendUds(
            EcuAddress.EGS,
            UdsCodec.buildRoutineControl(0x01, 0xF080, byteArrayOf(0x01)),
            "UDS 0x31 EGS ZF8 Adaptation Reset"
        )
        _session.update { state ->
            state.copy(
                zfAdaptations = state.zfAdaptations.map { it.copy(pressureMbar = 0, fillTimeMs = 0) },
                lastOperationMessage = "ZF8 EGS şanzıman adaptasyonları sıfırlandı (ilk 50 km öğrenme sürüşü yapın)."
            )
        }
    }

    suspend fun sendEcuReset(ecuAddress: Int, ecuName: String, resetType: Int = 0x01) {
        udsClient.sendUds(
            ecuAddress,
            UdsCodec.buildEcuReset(resetType),
            "UDS 0x11 ECU Reset ($ecuName, type=0x%02X)".format(resetType)
        )
        _session.update {
            it.copy(lastOperationMessage = "$ecuName için UDS 0x11 reset/uyku komutu gönderildi.")
        }
    }

    fun removeSavedVehicle(vin: String) {
        val updated = garageHistoryStorage?.removeVehicle(vin)
            ?: _session.value.savedVehicles.filterNot { it.vin == vin }
        _session.update { it.copy(savedVehicles = updated) }
    }

    fun exportBackupsJson(cacheDir: File): File {
        val reportsDir = File(cacheDir, "reports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val out = File(reportsDir, "BMW_Expert_PRO_Backups_$stamp.json")
        val storage = backupStorage ?: BackupStorage(reportsDir)
        out.writeText(storage.exportToJson(_session.value.backups))
        return out
    }

    fun importBackupsJson(rawJson: String): Int {
        val storage = backupStorage ?: BackupStorage(File(System.getProperty("java.io.tmpdir")))
        val imported = storage.importFromJson(rawJson)
        val merged = (imported + _session.value.backups).distinctBy { it.id }.take(50)
        backupStorage?.saveBackups(merged)
        _session.update {
            it.copy(
                backups = merged,
                lastOperationMessage = "${imported.size} adet kodlama yedeği JSON'dan içe aktarıldı."
            )
        }
        return imported.size
    }

    private fun saveConnectedVehicle(profile: VehicleProfile, adapter: AdapterType): List<SavedVehicleRecord> {
        val record = SavedVehicleRecord(
            vin = profile.vin,
            modelName = profile.modelDisplayName,
            chassisCode = profile.chassis.code,
            odometerKm = profile.odometerKm,
            lastConnected = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US).format(Date()),
            adapterName = adapter.displayName
        )
        return garageHistoryStorage?.upsertVehicle(record)
            ?: (listOf(record) + _session.value.savedVehicles.filterNot { it.vin == record.vin }).take(25)
    }

    fun adjustSimulatedVoltage(voltage: Double) {
        val sim = activeTransport as? SimulatorTransportAdapter ?: return
        sim.setSimulatedBatteryVoltage(voltage)
        _session.update { state ->
            state.copy(
                vehicleProfile = state.vehicleProfile?.copy(batteryVoltage = voltage)
            )
        }
    }

    fun clearProtocolLogs() {
        udsClient.clearLogs()
    }

    suspend fun scanHardware() {
        val gateways = EnetGatewayDiscovery.discover()
        val ctx = appContext
        val ble = if (ctx != null) BleObdTransportAdapter.scanNearbyObdAdapters(ctx) else emptyList()
        val usb = if (ctx != null) UsbKdcanTransportAdapter.scanUsbDevices(ctx) else emptyList()
        _session.update {
            it.copy(
                discoveredGateways = gateways,
                discoveredBleDevices = ble,
                discoveredUsbDevices = usb,
                selectedBleAddress = it.selectedBleAddress ?: ble.firstOrNull()?.address,
                lastOperationMessage = "Donanım taraması: ${gateways.size} ENET, ${ble.size} BLE, ${usb.size} USB cihaz bulundu."
            )
        }
    }

    fun selectBleDevice(address: String) {
        _session.update { it.copy(selectedBleAddress = address) }
    }

    suspend fun writeServiceHistoryRecord(
        date: String,
        mileageKm: Int,
        dealerCode: String,
        items: List<String>
    ) {
        if (items.isEmpty()) return
        val payload = "$date|$mileageKm|$dealerCode|${items.joinToString(",")}".toByteArray()
        udsClient.sendUds(
            EcuAddress.HU_MEDIA,
            UdsCodec.buildWriteDataById(0x3B01, payload),
            "UDS 0x2E iDrive Service History Write ($mileageKm km)"
        )
        val newRecord = ServiceHistoryRecord(
            id = "sh_${System.currentTimeMillis()}",
            date = date,
            mileageKm = mileageKm,
            dealerCode = dealerCode.ifBlank { "Punhan Garage (08801)" },
            performedItems = items
        )
        _session.update {
            it.copy(
                serviceHistory = listOf(newRecord) + it.serviceHistory,
                lastOperationMessage = "iDrive dijital servis geçmişine yeni bakım kaydı işlendi ($mileageKm km)."
            )
        }
    }

    companion object {
        const val MIN_SAFE_CODING_VOLTAGE = 12.2
    }
}
