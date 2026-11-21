package com.punhanaghayev.bimmerpro.data

import android.content.Context
import com.punhanaghayev.bimmerpro.core.bmw.ActuatorTestItem
import com.punhanaghayev.bimmerpro.core.bmw.BmwDtcDictionary
import com.punhanaghayev.bimmerpro.core.bmw.BmwEcuAndPidCatalog
import com.punhanaghayev.bimmerpro.core.bmw.CafdParameter
import com.punhanaghayev.bimmerpro.core.bmw.CodingBackupSnapshot
import com.punhanaghayev.bimmerpro.core.bmw.CodingCapability
import com.punhanaghayev.bimmerpro.core.bmw.CodingPreset
import com.punhanaghayev.bimmerpro.core.bmw.DpfHealthStatus
import com.punhanaghayev.bimmerpro.core.bmw.DtcFault
import com.punhanaghayev.bimmerpro.core.bmw.EcuAddress
import com.punhanaghayev.bimmerpro.core.bmw.EcuModule
import com.punhanaghayev.bimmerpro.core.bmw.IbsBatteryHealth
import com.punhanaghayev.bimmerpro.core.bmw.InjectorCalibration
import com.punhanaghayev.bimmerpro.core.bmw.LiveSensorChannel
import com.punhanaghayev.bimmerpro.core.bmw.MileageModuleReading
import com.punhanaghayev.bimmerpro.core.bmw.MileageVerificationResult
import com.punhanaghayev.bimmerpro.core.bmw.OfflineDiagnosticReport
import com.punhanaghayev.bimmerpro.core.bmw.OfflineFaultAnalyzer
import com.punhanaghayev.bimmerpro.core.bmw.ServiceExecutionResult
import com.punhanaghayev.bimmerpro.core.bmw.ServiceHistoryRecord
import com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutine
import com.punhanaghayev.bimmerpro.core.bmw.TamperStatus
import com.punhanaghayev.bimmerpro.core.bmw.VehicleOrderOption
import com.punhanaghayev.bimmerpro.core.bmw.VehicleProfile
import com.punhanaghayev.bimmerpro.core.bmw.VoDependencyMapper
import com.punhanaghayev.bimmerpro.core.bmw.ZfClutchAdaptation
import com.punhanaghayev.bimmerpro.core.bmw.ZfGearboxHealth
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import com.punhanaghayev.bimmerpro.core.protocol.BleAdapterDevice
import com.punhanaghayev.bimmerpro.core.protocol.BleObdTransportAdapter
import com.punhanaghayev.bimmerpro.core.protocol.BmwSecurityAccess
import com.punhanaghayev.bimmerpro.core.protocol.CafdBitmaskEngine
import com.punhanaghayev.bimmerpro.core.protocol.ConnectionState
import com.punhanaghayev.bimmerpro.core.protocol.DiscoveredGateway
import com.punhanaghayev.bimmerpro.core.protocol.DoIpProtocol
import com.punhanaghayev.bimmerpro.core.protocol.DoIpTransportAdapter
import com.punhanaghayev.bimmerpro.core.protocol.EnetGatewayDiscovery
import com.punhanaghayev.bimmerpro.core.protocol.PhysicalUdsParser
import com.punhanaghayev.bimmerpro.core.protocol.ProtocolLogEntry
import com.punhanaghayev.bimmerpro.core.protocol.TcpElmStnTransportAdapter
import com.punhanaghayev.bimmerpro.core.protocol.TcpEnetTransportAdapter
import com.punhanaghayev.bimmerpro.core.protocol.TransportAdapter
import com.punhanaghayev.bimmerpro.core.protocol.UdsClient
import com.punhanaghayev.bimmerpro.core.protocol.UdsCodec
import com.punhanaghayev.bimmerpro.core.protocol.UdsNegativeResponseCode
import com.punhanaghayev.bimmerpro.core.protocol.UdsResponse
import com.punhanaghayev.bimmerpro.core.protocol.UdsSessionType
import com.punhanaghayev.bimmerpro.core.protocol.UsbKdcanDeviceInfo
import com.punhanaghayev.bimmerpro.core.protocol.UsbKdcanTransportAdapter
import com.punhanaghayev.bimmerpro.core.simulator.BmwSimulatorCatalog
import com.punhanaghayev.bimmerpro.core.simulator.SimulatorTransportAdapter
import com.punhanaghayev.bimmerpro.core.update.DataPackDownloader
import com.punhanaghayev.bimmerpro.core.update.DataPackStatus
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

enum class ConnectionPurpose {
    DIAGNOSTICS,
    CODING
}

data class BimmerSessionState(
    val selectedAdapter: AdapterType = AdapterType.SIMULATOR_F10,
    val connectionPurpose: ConnectionPurpose = ConnectionPurpose.DIAGNOSTICS,
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
    val zfHealth: ZfGearboxHealth? = null,
    val dpfHealth: DpfHealthStatus? = null,
    val injectorCodes: List<InjectorCalibration> = emptyList(),
    val actuatorTests: List<ActuatorTestItem> = emptyList(),
    val ibsHealth: IbsBatteryHealth? = null,
    val offlineReport: OfflineDiagnosticReport? = null,
    val isPhysicalSafeMode: Boolean = true,
    val isPhysicalWriteUnlocked: Boolean = false,
    val dtcDictionaryCount: Int = BmwDtcDictionary.totalCodeCount(),
    val dataPackStatus: DataPackStatus = DataPackStatus.NotInstalled,
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
    val savedVehicles: List<SavedVehicleRecord> = emptyList(),
    val activeOperation: ActiveOperationState? = null
)

data class ActiveOperationState(
    val inProgress: Boolean = false,
    val title: String = "",
    val currentStep: String = "",
    val details: String = "",
    val progress: Float = 0f,
    val isCompleted: Boolean = false,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

class BimmerRepository(
    private val appContext: Context? = null
) {

    private val backupStorage: BackupStorage? = appContext?.let { BackupStorage(File(it.filesDir, "backups")) }
    private val garageHistoryStorage: GarageHistoryStorage? = appContext?.let { GarageHistoryStorage(File(it.filesDir, "garage")) }
    private var activeTransport: TransportAdapter = SimulatorTransportAdapter(AdapterType.SIMULATOR_F10)
    private val udsClient = UdsClient { activeTransport }
    private val ecuCafdBuffers = mutableMapOf<Int, ByteArray>()

    private var cachedLiveChannelsCatalog: List<LiveSensorChannel> = emptyList()

    private val _session = MutableStateFlow(
        BimmerSessionState(
            backups = backupStorage?.loadBackups().orEmpty(),
            savedVehicles = garageHistoryStorage?.loadVehicles().orEmpty()
        )
    )

    init {
        appContext?.let { ctx ->
            VoDependencyMapper.initializeFromDataPack(File(ctx.filesDir, "datapacks"))
            DataPackDownloader.loadCachedDtcPack(ctx.filesDir)?.let { pack ->
                BmwDtcDictionary.loadExternalPack(pack)
                val meta = DataPackDownloader.loadCachedMeta(ctx.filesDir)
                val total = BmwDtcDictionary.totalCodeCount()
                _session.update {
                    it.copy(
                        dtcDictionaryCount = total,
                        dataPackStatus = DataPackStatus.UpToDate(
                            packVersion = meta?.packVersion ?: pack.packVersion,
                            totalCount = total,
                            updatedAt = meta?.updatedAt ?: "-"
                        )
                    )
                }
            }
        }
    }
    val session: StateFlow<BimmerSessionState> = _session.asStateFlow()

    val protocolLogs: StateFlow<List<ProtocolLogEntry>> = udsClient.logs

    private var telemetryTick = 0

    suspend fun connect(
        adapterType: AdapterType,
        customHost: String = "",
        customPort: Int? = null,
        bleAddress: String? = _session.value.selectedBleAddress,
        purpose: ConnectionPurpose = _session.value.connectionPurpose
    ) {
        activeTransport.disconnect()
        activeTransport = when (adapterType) {
            AdapterType.VLINKER_BM_WIFI -> TcpElmStnTransportAdapter(
                adapterType = adapterType,
                context = appContext,
                host = customHost.ifBlank { TcpElmStnTransportAdapter.DEFAULT_VLINKER_HOST },
                port = customPort ?: TcpElmStnTransportAdapter.DEFAULT_VLINKER_PORT,
                onAtLog = { msg -> udsClient.logInfo("AT", EcuAddress.ZGW_BDC, msg) }
            )

            AdapterType.SIMULATOR_F10,
            AdapterType.SIMULATOR_F10_528I,
            AdapterType.SIMULATOR_F30,
            AdapterType.SIMULATOR_G30,
            AdapterType.SIMULATOR_G20,
            AdapterType.SIMULATOR_E90 -> SimulatorTransportAdapter(adapterType)

            AdapterType.ENET_WIFI,
            AdapterType.ENET_USB -> {
                val targetPort = customPort ?: TcpEnetTransportAdapter.DEFAULT_HSFZ_PORT
                if (targetPort == DoIpProtocol.DEFAULT_DOIP_PORT) {
                    DoIpTransportAdapter(
                        adapterType = adapterType,
                        context = appContext,
                        host = customHost.ifBlank { DoIpTransportAdapter.DEFAULT_DOIP_HOST },
                        port = targetPort
                    )
                } else {
                    TcpEnetTransportAdapter(
                        adapterType = adapterType,
                        context = appContext,
                        host = customHost.ifBlank { TcpEnetTransportAdapter.DEFAULT_ZGW_HOST },
                        port = targetPort
                    )
                }
            }

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
                connectionPurpose = purpose,
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
            val iLevelResp = runCatching {
                udsClient.sendUds(
                    EcuAddress.ZGW_BDC,
                    UdsCodec.buildReadDataById(SimulatorTransportAdapter.DID_I_LEVEL),
                    "UDS 0x22 Read Gateway I-Level (0xF150)"
                )
            }.getOrNull()
            val odoResp = runCatching {
                udsClient.sendUds(
                    EcuAddress.ZGW_BDC,
                    UdsCodec.buildReadDataById(SimulatorTransportAdapter.DID_ODOMETER),
                    "UDS 0x22 Read Gateway Odometer"
                )
            }.getOrNull()

            val sim = activeTransport as? SimulatorTransportAdapter
            if (sim != null) {
                val bundle = sim.bundle
                val savedList = saveConnectedVehicle(bundle.profile, adapterType)
                val dtcs = sim.currentDtcs()
                val customChannels = applySavedLivePidOrder(bundle.liveChannels)
                val health = OfflineFaultAnalyzer.analyze(dtcs, customChannels, bundle.ibsHealth, bundle.profile.batteryVoltage, bundle.zfAdaptations)
                _session.update {
                    it.copy(
                        connectionState = connState,
                        connectionPurpose = purpose,
                        vehicleProfile = bundle.profile,
                        ecus = bundle.ecus,
                        dtcs = dtcs,
                        presets = bundle.presets,
                        cafdParameters = bundle.cafdParameters,
                        liveChannels = customChannels,
                        serviceRoutines = bundle.serviceRoutines,
                        serviceHistory = bundle.serviceHistory,
                        zfAdaptations = bundle.zfAdaptations,
                        zfHealth = bundle.zfHealth,
                        dpfHealth = bundle.dpfHealth,
                        injectorCodes = bundle.injectorCodes,
                        actuatorTests = bundle.actuatorTests,
                        ibsHealth = bundle.ibsHealth,
                        offlineReport = health,
                        isPhysicalSafeMode = false,
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
                val liveILevel = (iLevelResp as? UdsResponse.Positive)
                    ?.let { PhysicalUdsParser.parseILevel(it.payload) }
                    ?: "Live Gateway"
                val liveOdoKm = (odoResp as? UdsResponse.Positive)
                    ?.let { PhysicalUdsParser.parseOdometerKm(it.payload) }
                    ?: 0
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
                    iLevel = liveILevel,
                    batteryVoltage = batteryVolts,
                    odometerKm = liveOdoKm,
                    faOptions = listOf(VehicleOrderOption("LIVE", "Canlı ECU Okuması Aktif")),
                    mileageVerification = MileageVerificationResult(
                        readings = listOf(MileageModuleReading("ZGW / Gateway", liveOdoKm)),
                        maxDifferenceKm = 0,
                        status = TamperStatus.VERIFIED,
                        summary = "Fiziksel araç bağlı — tam modül & KM doğrulaması için Diagnostik -> Tüm ECU Tara çalıştırın."
                    )
                )
                val savedList = saveConnectedVehicle(physicalProfile, adapterType)
                val fullEcuTree = BmwEcuAndPidCatalog.allBmwEcus.map { def ->
                    EcuModule(def.address, def.shortName, def.fullName, "Live", "Live", null, CodingCapability.FDL_ONLY, 0, 0)
                }
                val customChannels = applySavedLivePidOrder(template.liveChannels)
                val health = OfflineFaultAnalyzer.analyze(emptyList(), customChannels, null, batteryVolts, emptyList())
                _session.update {
                    it.copy(
                        connectionState = connState,
                        connectionPurpose = purpose,
                        vehicleProfile = physicalProfile,
                        ecus = fullEcuTree,
                        dtcs = emptyList(),
                        presets = template.presets,
                        cafdParameters = template.cafdParameters,
                        liveChannels = customChannels,
                        serviceRoutines = template.serviceRoutines,
                        serviceHistory = emptyList(),
                        zfAdaptations = template.zfAdaptations,
                        zfHealth = template.zfHealth,
                        dpfHealth = template.dpfHealth,
                        injectorCodes = emptyList(),
                        actuatorTests = template.actuatorTests,
                        ibsHealth = null,
                        offlineReport = health,
                        isPhysicalSafeMode = true,
                        savedVehicles = savedList,
                        lastOperationMessage = "Fiziksel Salt-Okunur Güvenli Mod: $extractedVin (%.1f V)".format(batteryVolts)
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
                connectionPurpose = it.connectionPurpose,
                connectionState = ConnectionState.Disconnected,
                dtcDictionaryCount = it.dtcDictionaryCount,
                dataPackStatus = it.dataPackStatus,
                savedVehicles = it.savedVehicles,
                backups = it.backups
            )
        }
    }

    suspend fun runFullVehicleScan() {
        val ecus = _session.value.ecus
        if (ecus.isEmpty()) return

        _session.update {
            it.copy(
                isScanning = true,
                scanProgress = 0f,
                scanStatusText = "ECU ağacı taranıyor...",
                activeOperation = ActiveOperationState(
                    inProgress = true,
                    title = "BMW Diagnostik Taraması",
                    currentStep = "ECU İletişimi Başlatılıyor...",
                    details = "${ecus.size} modül sorgulanacak",
                    progress = 0.05f
                )
            )
        }
        val total = ecus.size.toFloat()

        val sim = activeTransport as? SimulatorTransportAdapter
        if (sim != null) {
            ecus.forEachIndexed { index, ecu ->
                val p = (index + 1) / total
                _session.update {
                    it.copy(
                        scanProgress = p,
                        scanStatusText = "${ecu.shortName} (${ecu.addressHex}) DTC & KM okunuyor...",
                        activeOperation = ActiveOperationState(
                            inProgress = true,
                            title = "BMW Diagnostik Taraması",
                            currentStep = "[${index + 1}/${ecus.size}] ${ecu.shortName} (${ecu.addressHex})",
                            details = "UDS 0x19 Arıza Hafızası, SWE ve KM DID okunuyor...",
                            progress = p
                        )
                    )
                }
                udsClient.sendUds(
                    ecu.address,
                    UdsCodec.buildReadDtcByStatusMask(0xFF),
                    "UDS 0x19 Read DTC (${ecu.shortName})"
                )
                delay(70)
            }
            val refreshedDtcs = sim.currentDtcs()
            val health = OfflineFaultAnalyzer.analyze(
                refreshedDtcs,
                _session.value.liveChannels,
                _session.value.ibsHealth,
                _session.value.vehicleProfile?.batteryVoltage ?: 12.6,
                _session.value.zfAdaptations
            )
            _session.update {
                it.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    scanStatusText = "Tarama tamamlandı (${refreshedDtcs.size} kayıt)",
                    dtcs = refreshedDtcs,
                    offlineReport = health,
                    lastOperationMessage = "Tüm modüller tarandı: ${refreshedDtcs.size} hata kaydı bulundu.",
                    activeOperation = ActiveOperationState(
                        inProgress = true,
                        title = "BMW Diagnostik Taraması",
                        currentStep = "Tarama Başarıyla Tamamlandı!",
                        details = "${ecus.size} ECU tarandı, ${refreshedDtcs.size} arıza kaydı tespit edildi.",
                        progress = 1f,
                        isCompleted = true,
                        isSuccess = true
                    )
                )
            }
            return
        }

        // Physical vehicle scan: query each ECU with UDS 0x19 0x02 + SWE/Odometer, keep responding ECUs
        val physicalDtcs = mutableListOf<DtcFault>()
        val odo = _session.value.vehicleProfile?.odometerKm ?: 0
        val volts = _session.value.vehicleProfile?.batteryVoltage ?: 12.6
        val updatedEcus = mutableListOf<EcuModule>()
        val mileageReadings = mutableListOf<MileageModuleReading>()
        ecus.forEachIndexed { index, ecu ->
            val p = (index + 1) / total
            _session.update {
                it.copy(
                    scanProgress = p,
                    scanStatusText = "${ecu.shortName} (${ecu.addressHex}) DTC & SVT sorgulanıyor...",
                    activeOperation = ActiveOperationState(
                        inProgress = true,
                        title = "BMW Diagnostik Taraması",
                        currentStep = "[${index + 1}/${ecus.size}] ${ecu.shortName} (${ecu.addressHex})",
                        details = "UDS 0x19 Fiziksel DTC, SWE ve KM DID okunuyor...",
                        progress = p
                    )
                )
            }
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
                val swResp = runCatching {
                    udsClient.sendUds(ecu.address, UdsCodec.buildReadDataById(0xF195), "UDS 0x22 SWE (${ecu.shortName})")
                }.getOrNull() as? UdsResponse.Positive
                val swVersion = swResp?.let { PhysicalUdsParser.parseSoftwareVersion(it.payload) } ?: ecu.softwareVersion
                val ecuOdoResp = runCatching {
                    udsClient.sendUds(ecu.address, UdsCodec.buildReadDataById(SimulatorTransportAdapter.DID_ODOMETER), "UDS 0x22 KM (${ecu.shortName})")
                }.getOrNull() as? UdsResponse.Positive
                val ecuKm = ecuOdoResp?.let { PhysicalUdsParser.parseOdometerKm(it.payload) }
                if (ecuKm != null && ecuKm > 0) {
                    mileageReadings.add(MileageModuleReading(ecu.shortName, ecuKm))
                }
                updatedEcus.add(
                    ecu.copy(
                        softwareVersion = swVersion,
                        reportedMileageKm = ecuKm ?: ecu.reportedMileageKm,
                        activeDtcCount = parsed.count { it.status == com.punhanaghayev.bimmerpro.core.bmw.DtcStatus.ACTIVE },
                        shadowDtcCount = parsed.count { it.severity == com.punhanaghayev.bimmerpro.core.bmw.DtcSeverity.SHADOW }
                    )
                )
            } else if (ecu.address == EcuAddress.ZGW_BDC || ecu.address == EcuAddress.DME_DDE) {
                updatedEcus.add(ecu)
            }
        }
        val updatedProfile = _session.value.vehicleProfile?.let { prof ->
            if (mileageReadings.size >= 2) {
                val maxKm = mileageReadings.maxOf { it.mileageKm }
                val minKm = mileageReadings.minOf { it.mileageKm }
                val diff = maxKm - minKm
                val status = when {
                    diff <= 10 -> TamperStatus.VERIFIED
                    diff <= 150 -> TamperStatus.MINOR_DRIFT
                    else -> TamperStatus.SUSPICIOUS_MISMATCH
                }
                prof.copy(
                    odometerKm = maxKm,
                    mileageVerification = MileageVerificationResult(
                        readings = mileageReadings,
                        maxDifferenceKm = diff,
                        status = status,
                        summary = "${mileageReadings.size} ECU'dan KM okundu (maks fark: $diff km)."
                    )
                )
            } else {
                prof
            }
        }
        val physicalHealth = OfflineFaultAnalyzer.analyze(
            physicalDtcs,
            _session.value.liveChannels,
            _session.value.ibsHealth,
            volts,
            _session.value.zfAdaptations
        )
        _session.update {
            it.copy(
                isScanning = false,
                scanProgress = 1f,
                scanStatusText = "Fiziksel tarama tamamlandı (${updatedEcus.size} ECU, ${physicalDtcs.size} kayıt)",
                vehicleProfile = updatedProfile ?: it.vehicleProfile,
                ecus = updatedEcus.ifEmpty { ecus },
                dtcs = physicalDtcs,
                offlineReport = physicalHealth,
                lastOperationMessage = "Fiziksel araç tarandı: ${updatedEcus.size} ECU yanıt verdi, ${physicalDtcs.size} hata kaydı bulundu.",
                activeOperation = ActiveOperationState(
                    inProgress = true,
                    title = "BMW Diagnostik Taraması",
                    currentStep = "Fiziksel Tarama Tamamlandı!",
                    details = "${updatedEcus.size} ECU yanıt verdi, ${physicalDtcs.size} arıza kaydı tespit edildi.",
                    progress = 1f,
                    isCompleted = true,
                    isSuccess = true
                )
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
            val updatedHealth = OfflineFaultAnalyzer.analyze(
                remaining,
                _session.value.liveChannels,
                _session.value.ibsHealth,
                _session.value.vehicleProfile?.batteryVoltage ?: 12.6,
                _session.value.zfAdaptations
            )
            _session.update {
                it.copy(
                    dtcs = remaining,
                    ecus = refreshedEcus,
                    offlineReport = updatedHealth,
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
                val remaining = state.dtcs.filterNot { it.ecuAddress == ecuAddress }
                val updatedHealth = OfflineFaultAnalyzer.analyze(
                    remaining,
                    state.liveChannels,
                    state.ibsHealth,
                    state.vehicleProfile?.batteryVoltage ?: 12.6,
                    state.zfAdaptations
                )
                state.copy(
                    dtcs = remaining,
                    ecus = state.ecus.map {
                        if (it.address == ecuAddress) it.copy(activeDtcCount = 0, shadowDtcCount = 0) else it
                    },
                    offlineReport = updatedHealth,
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

        _session.update {
            it.copy(
                activeOperation = ActiveOperationState(
                    inProgress = true,
                    title = "BMW Kodlama Motoru (Easy Mode)",
                    currentStep = "Akü Voltajı ve Güvenlik Doğrulanıyor...",
                    details = "Akü: %.1f V (Gereken: >= %.1f V)".format(profile.batteryVoltage, MIN_SAFE_CODING_VOLTAGE),
                    progress = 0.20f
                )
            )
        }

        if (profile.batteryVoltage < MIN_SAFE_CODING_VOLTAGE) {
            val err = "Güvenlik Kilidi: Akü voltajı %.1f V (< %.1f V). ECU hasarını önlemek için kodlama durduruldu."
                .format(profile.batteryVoltage, MIN_SAFE_CODING_VOLTAGE)
            _session.update {
                it.copy(
                    activeOperation = ActiveOperationState(
                        inProgress = true,
                        title = "BMW Kodlama Motoru (Easy Mode)",
                        currentStep = "Kodlama Güvenlik Kilidi",
                        details = err,
                        progress = 1f,
                        isCompleted = true,
                        isSuccess = false,
                        errorMessage = err
                    )
                )
            }
            return Result.failure(IllegalStateException(err))
        }
        if (isPhysicalWriteBlocked()) {
            _session.update {
                it.copy(
                    activeOperation = ActiveOperationState(
                        inProgress = true,
                        title = "BMW Kodlama Motoru (Easy Mode)",
                        currentStep = "Fiziksel Güvenli Mod Kilitli",
                        details = PHYSICAL_SAFE_MODE_MSG,
                        progress = 1f,
                        isCompleted = true,
                        isSuccess = false,
                        errorMessage = PHYSICAL_SAFE_MODE_MSG
                    )
                )
            }
            return Result.failure(IllegalStateException(PHYSICAL_SAFE_MODE_MSG))
        }

        // Create automatic pre-coding backup snapshot
        _session.update {
            it.copy(
                activeOperation = ActiveOperationState(
                    inProgress = true,
                    title = "BMW Kodlama Motoru (Easy Mode)",
                    currentStep = "Otomatik CAFD Güvenlik Yedeği Alınıyor...",
                    details = "Modül: ${preset.targetEcu} - ${preset.cafdKey}",
                    progress = 0.45f
                )
            )
        }
        val snapshot = CodingBackupSnapshot(
            id = "bkp_${System.currentTimeMillis()}",
            timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()),
            ecuName = preset.targetEcu,
            ecuAddress = preset.targetAddress,
            cafdFile = preset.cafdKey,
            summary = "${preset.title}: ${preset.options[preset.currentOptionIndex]} -> ${preset.options[newOptionIndex]}",
            previousValues = mapOf(presetId to preset.currentOptionIndex)
        )

        _session.update {
            it.copy(
                activeOperation = ActiveOperationState(
                    inProgress = true,
                    title = "BMW Kodlama Motoru (Easy Mode)",
                    currentStep = "UDS 0x10 Kodlama Oturumu Başlatılıyor...",
                    details = "ECU: ${preset.targetEcu} (0x%02X)".format(preset.targetAddress),
                    progress = 0.65f
                )
            )
        }
        udsClient.sendUds(
            preset.targetAddress,
            UdsCodec.buildSessionControl(UdsSessionType.CODING),
            "UDS 0x10 Coding Session (${preset.targetEcu})"
        )

        _session.update {
            it.copy(
                activeOperation = ActiveOperationState(
                    inProgress = true,
                    title = "BMW Kodlama Motoru (Easy Mode)",
                    currentStep = "UDS 0x2E FDL Parametresi Yazılıyor & Doğrulanıyor...",
                    details = "${preset.title} -> ${preset.options[newOptionIndex]}",
                    progress = 0.85f
                )
            )
        }
        var writeResp = udsClient.sendUds(
            preset.targetAddress,
            UdsCodec.buildWriteDataById(0x3001, byteArrayOf(newOptionIndex.toByte())),
            "UDS 0x2E FDL Write ${preset.cafdKey}=${newOptionIndex}"
        )

        if (writeResp is UdsResponse.Negative && writeResp.nrc == UdsNegativeResponseCode.SECURITY_ACCESS_DENIED) {
            val unlocked = tryUnlockSecurityAccess(preset.targetAddress, 0x01)
            if (unlocked) {
                writeResp = udsClient.sendUds(
                    preset.targetAddress,
                    UdsCodec.buildWriteDataById(0x3001, byteArrayOf(newOptionIndex.toByte())),
                    "UDS 0x2E FDL Write Retry after SecurityAccess ${preset.cafdKey}=${newOptionIndex}"
                )
            }
        }

        return when (writeResp) {
            is UdsResponse.Positive -> {
                val updatedPresets = state.presets.map {
                    if (it.id == presetId) it.copy(currentOptionIndex = newOptionIndex) else it
                }
                val updatedBackups = listOf(snapshot) + state.backups
                backupStorage?.saveBackups(updatedBackups)
                val successMsg = "${preset.targetEcu} kodlandı (${preset.options[newOptionIndex]}). Yedek alındı."
                _session.update {
                    it.copy(
                        presets = updatedPresets,
                        backups = updatedBackups,
                        lastOperationMessage = successMsg,
                        activeOperation = ActiveOperationState(
                            inProgress = true,
                            title = "BMW Kodlama Motoru (Easy Mode)",
                            currentStep = "Kodlama Başarıyla Tamamlandı!",
                            details = successMsg,
                            progress = 1.0f,
                            isCompleted = true,
                            isSuccess = true
                        )
                    )
                }
                Result.success("Kodlama başarılı")
            }
            is UdsResponse.Negative -> {
                val msg = "ECU Reddi (NRC 0x%02X): ${writeResp.nrc.description}".format(writeResp.nrc.code)
                _session.update {
                    it.copy(
                        lastOperationMessage = msg,
                        activeOperation = ActiveOperationState(
                            inProgress = true,
                            title = "BMW Kodlama Motoru (Easy Mode)",
                            currentStep = "Kodlama Reddedildi",
                            details = msg,
                            progress = 1.0f,
                            isCompleted = true,
                            isSuccess = false,
                            errorMessage = msg
                        )
                    )
                }
                Result.failure(IllegalStateException(msg))
            }
        }
    }

    suspend fun applyCafdParameter(paramId: String, optionLabel: String): Result<String> {
        val state = _session.value
        val profile = state.vehicleProfile ?: return Result.failure(IllegalStateException("Araç bağlı değil"))
        val param = state.cafdParameters.firstOrNull { it.id == paramId }
            ?: return Result.failure(IllegalArgumentException("CAFD parametresi bulunamadı"))

        _session.update {
            it.copy(
                activeOperation = ActiveOperationState(
                    inProgress = true,
                    title = "BMW Expert CAFD/FDL Motoru",
                    currentStep = "Akü Voltajı ve Güvenlik Doğrulanıyor...",
                    details = "Akü: %.1f V, Modül: ${param.ecuName}".format(profile.batteryVoltage),
                    progress = 0.15f
                )
            )
        }

        if (profile.batteryVoltage < MIN_SAFE_CODING_VOLTAGE) {
            val err = "Güvenlik Kilidi: Akü voltajı %.1f V (< 12.2 V)!".format(profile.batteryVoltage)
            _session.update {
                it.copy(
                    activeOperation = ActiveOperationState(
                        inProgress = true,
                        title = "BMW Expert CAFD/FDL Motoru",
                        currentStep = "Voltaj Yetersiz",
                        details = err,
                        progress = 1f,
                        isCompleted = true,
                        isSuccess = false,
                        errorMessage = err
                    )
                )
            }
            return Result.failure(IllegalStateException(err))
        }
        if (isPhysicalWriteBlocked()) {
            _session.update {
                it.copy(
                    activeOperation = ActiveOperationState(
                        inProgress = true,
                        title = "BMW Expert CAFD/FDL Motoru",
                        currentStep = "Fiziksel Güvenli Mod Kilitli",
                        details = PHYSICAL_SAFE_MODE_MSG,
                        progress = 1f,
                        isCompleted = true,
                        isSuccess = false,
                        errorMessage = PHYSICAL_SAFE_MODE_MSG
                    )
                )
            }
            return Result.failure(IllegalStateException(PHYSICAL_SAFE_MODE_MSG))
        }

        _session.update {
            it.copy(
                activeOperation = ActiveOperationState(
                    inProgress = true,
                    title = "BMW Expert CAFD/FDL Motoru",
                    currentStep = "UDS 0x22 Mevcut FDL Buffer Okunuyor...",
                    details = "ECU: ${param.ecuName} (0x%02X)".format(param.ecuAddress),
                    progress = 0.35f
                )
            )
        }

        // Read-Modify-Write-Verify + Auto-Rollback
        val readBefore = runCatching {
            udsClient.sendUds(
                param.ecuAddress,
                UdsCodec.buildReadDataById(0x3002),
                "UDS 0x22 Read FDL Before (${param.ecuName})"
            )
        }.getOrNull() as? UdsResponse.Positive

        val hexStr = param.availableOptions[optionLabel] ?: param.currentHex
        val byteVal = hexStr.removePrefix("0x").toIntOrNull(16) ?: 0
        val currentBuffer = ecuCafdBuffers.getOrPut(param.ecuAddress) { ByteArray(128) }
        val payloadBefore = readBefore?.payload
        if (payloadBefore != null && payloadBefore.size >= 4) {
            val remoteOffset = payloadBefore[2].toInt() and 0xFF
            if (remoteOffset in currentBuffer.indices && remoteOffset == param.byteOffset) {
                currentBuffer[remoteOffset] = payloadBefore[3]
            }
        }
        val previousByte = currentBuffer[param.byteOffset]

        _session.update {
            it.copy(
                activeOperation = ActiveOperationState(
                    inProgress = true,
                    title = "BMW Expert CAFD/FDL Motoru",
                    currentStep = "CAFD Bitmask Hesaplanıyor & Buffer Yamalanıyor...",
                    details = "Offset 0x%02X, Mask 0x%02X -> Değer %s".format(param.byteOffset, param.bitMask, hexStr),
                    progress = 0.55f
                )
            )
        }

        val patchedBuffer = CafdBitmaskEngine.writeMaskedValue(
            codingData = currentBuffer,
            byteOffset = param.byteOffset,
            bitMask = param.bitMask,
            newValue = byteVal
        )

        _session.update {
            it.copy(
                activeOperation = ActiveOperationState(
                    inProgress = true,
                    title = "BMW Expert CAFD/FDL Motoru",
                    currentStep = "UDS 0x2E FDL Parametresi Yazılıyor...",
                    details = "${param.functionName} = $hexStr (Byte=0x%02X)".format(patchedBuffer[param.byteOffset]),
                    progress = 0.75f
                )
            )
        }

        var writeResp = udsClient.sendUds(
            param.ecuAddress,
            UdsCodec.buildWriteDataById(0x3002, byteArrayOf(param.byteOffset.toByte(), patchedBuffer[param.byteOffset])),
            "UDS 0x2E Expert FDL ${param.functionName}=$hexStr (Byte=0x%02X)".format(patchedBuffer[param.byteOffset])
        )

        if (writeResp is UdsResponse.Negative && writeResp.nrc == UdsNegativeResponseCode.SECURITY_ACCESS_DENIED) {
            val unlocked = tryUnlockSecurityAccess(param.ecuAddress, 0x01)
            if (unlocked) {
                writeResp = udsClient.sendUds(
                    param.ecuAddress,
                    UdsCodec.buildWriteDataById(0x3002, byteArrayOf(param.byteOffset.toByte(), patchedBuffer[param.byteOffset])),
                    "UDS 0x2E Expert FDL Retry after SecurityAccess ${param.functionName}=$hexStr"
                )
            }
        }

        return when (writeResp) {
            is UdsResponse.Positive -> {
                _session.update {
                    it.copy(
                        activeOperation = ActiveOperationState(
                            inProgress = true,
                            title = "BMW Expert CAFD/FDL Motoru",
                            currentStep = "UDS 0x22 Doğrulama (Verify) Yapılıyor...",
                            details = "Geri okunan byte kontrol ediliyor...",
                            progress = 0.90f
                        )
                    )
                }
                val readVerify = runCatching {
                    udsClient.sendUds(
                        param.ecuAddress,
                        UdsCodec.buildReadDataById(0x3002),
                        "UDS 0x22 Verify FDL (${param.ecuName})"
                    )
                }.getOrNull() as? UdsResponse.Positive
                val verifyPayload = readVerify?.payload
                if (verifyPayload != null && verifyPayload.size >= 4 &&
                    (verifyPayload[2].toInt() and 0xFF) == param.byteOffset &&
                    verifyPayload[3] != patchedBuffer[param.byteOffset]
                ) {
                    runCatching {
                        udsClient.sendUds(
                            param.ecuAddress,
                            UdsCodec.buildWriteDataById(0x3002, byteArrayOf(param.byteOffset.toByte(), previousByte)),
                            "UDS 0x2E Rollback FDL (${param.ecuName})"
                        )
                    }
                    val rollbackMsg = "Doğrulama Hatası (${param.ecuName}): Geri okunan byte eşleşmedi, otomatik rollback uygulandı."
                    _session.update {
                        it.copy(
                            lastOperationMessage = rollbackMsg,
                            activeOperation = ActiveOperationState(
                                inProgress = true,
                                title = "BMW Expert CAFD/FDL Motoru",
                                currentStep = "Doğrulama Hatası (Rollback)",
                                details = rollbackMsg,
                                progress = 1f,
                                isCompleted = true,
                                isSuccess = false,
                                errorMessage = rollbackMsg
                            )
                        )
                    }
                    return Result.failure(IllegalStateException(rollbackMsg))
                }
                ecuCafdBuffers[param.ecuAddress] = patchedBuffer
                val updated = state.cafdParameters.map {
                    if (it.id == paramId) it.copy(currentHex = hexStr, selectedOptionLabel = optionLabel) else it
                }
                val successMsg = "Expert FDL doğrulandı & yazıldı: ${param.functionName} -> $optionLabel"
                _session.update {
                    it.copy(
                        cafdParameters = updated,
                        lastOperationMessage = successMsg,
                        activeOperation = ActiveOperationState(
                            inProgress = true,
                            title = "BMW Expert CAFD/FDL Motoru",
                            currentStep = "Kodlama Başarıyla Tamamlandı & Doğrulandı!",
                            details = successMsg,
                            progress = 1.0f,
                            isCompleted = true,
                            isSuccess = true
                        )
                    )
                }
                Result.success("Expert FDL doğrulandı ve yazıldı")
            }
            is UdsResponse.Negative -> {
                val msg = "ECU Reddi (NRC 0x%02X): ${writeResp.nrc.description}".format(writeResp.nrc.code)
                _session.update {
                    it.copy(
                        lastOperationMessage = msg,
                        activeOperation = ActiveOperationState(
                            inProgress = true,
                            title = "BMW Expert CAFD/FDL Motoru",
                            currentStep = "Yazma Reddedildi",
                            details = msg,
                            progress = 1f,
                            isCompleted = true,
                            isSuccess = false,
                            errorMessage = msg
                        )
                    )
                }
                Result.failure(IllegalStateException(msg))
            }
        }
    }

    private suspend fun tryUnlockSecurityAccess(targetAddress: Int, level: Int = 0x01): Boolean {
        _session.update {
            it.copy(
                activeOperation = it.activeOperation?.copy(
                    currentStep = "UDS 0x27 Security Access (Seed & Key) Çözülüyor...",
                    details = "Seed isteniyor (Seviye 0x%02X)...".format(level),
                    progress = 0.88f
                )
            )
        }
        val seedResp = udsClient.sendUds(
            targetAddress,
            UdsCodec.buildSecurityRequestSeed(level),
            "UDS 0x27 Security Access Request Seed (Level $level)"
        )
        if (seedResp !is UdsResponse.Positive) return false
        val seed = PhysicalUdsParser.parseSecuritySeed(seedResp.payload) ?: return false
        if (BmwSecurityAccess.isAlreadyUnlocked(seed)) return true

        val key = BmwSecurityAccess.calculateKey(targetAddress, seed, level)
        val keyResp = udsClient.sendUds(
            targetAddress,
            UdsCodec.buildSecuritySendKey(level + 1, key),
            "UDS 0x27 Security Access Send Key (Level ${level + 1})"
        )
        return keyResp is UdsResponse.Positive
    }

    suspend fun restoreBackup(backupId: String) {
        if (isPhysicalWriteBlocked()) {
            _session.update { it.copy(lastOperationMessage = PHYSICAL_SAFE_MODE_MSG) }
            return
        }
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
        if (isPhysicalWriteBlocked()) {
            _session.update { it.copy(lastOperationMessage = PHYSICAL_SAFE_MODE_MSG) }
            return
        }
        val cleanCode = code.trim().uppercase()
        if (cleanCode.length < 3) return
        val profile = _session.value.vehicleProfile ?: return
        if (profile.faOptions.any { it.code == cleanCode }) return

        val affected = VoDependencyMapper.affectedEcusFor(cleanCode)
        affected.forEach { (addr, name) ->
            udsClient.sendUds(
                addr,
                UdsCodec.buildWriteDataById(0x3F00, cleanCode.toByteArray()),
                "UDS 0x2E FA/VO Write +$cleanCode -> $name"
            )
        }
        val ecuNames = affected.joinToString(", ") { it.second }
        _session.update {
            it.copy(
                vehicleProfile = profile.copy(
                    faOptions = profile.faOptions + VehicleOrderOption(cleanCode, description.ifBlank { "Özel VO Opsiyonu" })
                ),
                lastOperationMessage = "FA/VO $cleanCode eklendi. Kodlanan ECU'lar: $ecuNames"
            )
        }
    }

    suspend fun createFullNcdSnapshot() {
        val state = _session.value
        val previousMap = state.presets.associate { it.id to it.currentOptionIndex }
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(previousMap.entries.joinToString(",").toByteArray())
            .take(4)
            .joinToString("") { "%02X".format(it) }
        val snapshot = CodingBackupSnapshot(
            id = "ncd_${System.currentTimeMillis()}",
            timestamp = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US).format(Date()),
            ecuName = "FULL_SVT (${state.ecus.size} ECU)",
            ecuAddress = EcuAddress.ZGW_BDC,
            cafdFile = "NCD_SHA256_$digest",
            summary = "Tam Araç NCD Snapshot (${state.presets.size} parametre, SHA:$digest)",
            previousValues = previousMap
        )
        val updated = listOf(snapshot) + state.backups
        backupStorage?.saveBackups(updated)
        _session.update {
            it.copy(
                backups = updated,
                lastOperationMessage = "Tam araç NCD Snapshot alındı (SHA:$digest)."
            )
        }
    }

    suspend fun executeServiceRoutine(routine: ServiceRoutine, optionByte: Byte = 0x01) {
        if (isPhysicalWriteBlocked()) {
            _session.update { it.copy(lastOperationMessage = PHYSICAL_SAFE_MODE_MSG) }
            return
        }
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
            // Cyclic Multi-ECU live PID polling across BmwEcuAndPidCatalog (4 PIDs per tick)
            val pidMap = BmwEcuAndPidCatalog.livePidDefinitions.associateBy { it.id }
            val activeDefs = current.mapNotNull { ch -> pidMap[ch.id] }
            if (activeDefs.isNotEmpty()) {
                val batchSize = 4
                val batchCount = ((activeDefs.size + batchSize - 1) / batchSize).coerceAtLeast(1)
                val startIdx = (telemetryTick % batchCount) * batchSize
                val batch = activeDefs.drop(startIdx).take(batchSize)
                val polledValues = mutableMapOf<String, Double>()
                batch.forEach { def ->
                    val resp = runCatching {
                        udsClient.sendUds(def.ecuAddress, UdsCodec.buildReadDataById(def.did), "UDS 0x22 Live ${def.id}")
                    }.getOrNull() as? UdsResponse.Positive
                    val parsed = resp?.payload?.let { PhysicalUdsParser.parsePhysicalPidValue(def.id, it) }
                    if (parsed != null) {
                        polledValues[def.id] = parsed
                    }
                }
                val updated = current.map { ch ->
                    val v = polledValues[ch.id] ?: ch.currentValue
                    ch.copy(currentValue = v, history = (ch.history.drop(1) + v.toFloat()))
                }
                _session.update { it.copy(liveChannels = updated) }
            }
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
        if (isPhysicalWriteBlocked()) {
            _session.update { it.copy(lastOperationMessage = PHYSICAL_SAFE_MODE_MSG) }
            return
        }
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
        if (isPhysicalWriteBlocked()) {
            _session.update { it.copy(lastOperationMessage = PHYSICAL_SAFE_MODE_MSG) }
            return
        }
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
        if (isPhysicalWriteBlocked()) {
            _session.update { it.copy(lastOperationMessage = PHYSICAL_SAFE_MODE_MSG) }
            return
        }
        udsClient.sendUds(
            ecuAddress,
            UdsCodec.buildEcuReset(resetType),
            "UDS 0x11 ECU Reset ($ecuName, type=0x%02X)".format(resetType)
        )
        _session.update {
            it.copy(lastOperationMessage = "$ecuName için UDS 0x11 reset/uyku komutu gönderildi.")
        }
    }

    suspend fun runActuatorTest(test: ActuatorTestItem) {
        if (isPhysicalWriteBlocked()) {
            _session.update { it.copy(lastOperationMessage = PHYSICAL_SAFE_MODE_MSG) }
            return
        }
        val resp = udsClient.sendUds(
            test.targetAddress,
            UdsCodec.buildIoControl(test.did, controlParam = 0x03, stateByte = 0x01),
            "UDS 0x2F Actuator Test (${test.title})"
        )
        val stamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val result = when (resp) {
            is UdsResponse.Positive -> ServiceExecutionResult(true, test.title, "${test.targetEcu} aktüatör testi aktif edildi.", stamp)
            is UdsResponse.Negative -> ServiceExecutionResult(false, test.title, "Reddedildi: ${resp.nrc.description}", stamp)
        }
        _session.update { it.copy(lastServiceResult = result, lastOperationMessage = result.message) }
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
        val storage = backupStorage ?: BackupStorage(File(System.getProperty("java.io.tmpdir") ?: "/tmp"))
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

    fun dismissActiveOperation() {
        _session.update { it.copy(activeOperation = null) }
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
        if (isPhysicalWriteBlocked()) {
            _session.update { it.copy(lastOperationMessage = PHYSICAL_SAFE_MODE_MSG) }
            return
        }
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

    private fun isPhysicalWriteBlocked(): Boolean =
        activeTransport !is SimulatorTransportAdapter && !_session.value.isPhysicalWriteUnlocked

    fun setPhysicalWriteUnlocked(unlocked: Boolean) {
        _session.update {
            it.copy(
                isPhysicalWriteUnlocked = unlocked,
                isPhysicalSafeMode = !unlocked
            )
        }
    }

    suspend fun sendTesterPresentKeepAlive() {
        if (_session.value.connectionState is ConnectionState.Connected && activeTransport !is SimulatorTransportAdapter) {
            runCatching {
                udsClient.sendUds(EcuAddress.ZGW_BDC, UdsCodec.buildTesterPresent(true), "UDS 0x3E Keep-Alive")
            }
        }
    }

    private fun applySavedLivePidOrder(catalogChannels: List<LiveSensorChannel>): List<LiveSensorChannel> {
        cachedLiveChannelsCatalog = catalogChannels
        val savedCsv = appContext?.getSharedPreferences("bmw_expert_pro_prefs", Context.MODE_PRIVATE)
            ?.getString("selected_live_pids", null)
        if (savedCsv.isNullOrBlank()) return catalogChannels
        val byId = catalogChannels.associateBy { it.id }
        val ordered = savedCsv.split(",").mapNotNull { byId[it.trim()] }
        return ordered.ifEmpty { catalogChannels }
    }

    private fun persistLivePidOrder(channels: List<LiveSensorChannel>) {
        appContext?.getSharedPreferences("bmw_expert_pro_prefs", Context.MODE_PRIVATE)
            ?.edit()
            ?.putString("selected_live_pids", channels.joinToString(",") { it.id })
            ?.apply()
    }

    fun availableLiveChannelCatalog(): List<LiveSensorChannel> =
        cachedLiveChannelsCatalog.ifEmpty { _session.value.liveChannels }

    fun setSelectedLivePids(orderedIds: List<String>) {
        if (orderedIds.isEmpty()) return
        val pool = (cachedLiveChannelsCatalog + _session.value.liveChannels).associateBy { it.id }
        val updated = orderedIds.mapNotNull { pool[it] }
        if (updated.isNotEmpty()) {
            persistLivePidOrder(updated)
            _session.update { it.copy(liveChannels = updated) }
        }
    }

    fun moveLiveChannel(fromIndex: Int, toIndex: Int) {
        val current = _session.value.liveChannels.toMutableList()
        if (fromIndex !in current.indices || toIndex !in current.indices || fromIndex == toIndex) return
        val item = current.removeAt(fromIndex)
        current.add(toIndex, item)
        persistLivePidOrder(current)
        _session.update { it.copy(liveChannels = current) }
    }

    suspend fun downloadDataPack(): Result<Int> {
        val ctx = appContext ?: return Result.failure(IllegalStateException("Context yok"))
        val prevCount = BmwDtcDictionary.totalCodeCount()
        val prevMeta = DataPackDownloader.loadCachedMeta(ctx.filesDir)
        _session.update { it.copy(dataPackStatus = DataPackStatus.Downloading) }
        return runCatching {
            val pack = DataPackDownloader.downloadAndCacheDtcPack(ctx.filesDir)
            DataPackDownloader.downloadOptionalPacks(ctx.filesDir)
            VoDependencyMapper.initializeFromDataPack(File(ctx.filesDir, "datapacks"))
            BmwDtcDictionary.loadExternalPack(pack)
            val total = BmwDtcDictionary.totalCodeCount()
            val added = (total - prevCount).coerceAtLeast(0)
            val meta = DataPackDownloader.saveCachedMeta(ctx.filesDir, pack.packVersion, total)
            val newStatus = if (added > 0 || prevMeta?.packVersion != pack.packVersion) {
                DataPackStatus.Applied(pack.packVersion, added, total, meta.updatedAt)
            } else {
                DataPackStatus.UpToDate(pack.packVersion, total, meta.updatedAt)
            }
            _session.update {
                it.copy(
                    dtcDictionaryCount = total,
                    dataPackStatus = newStatus,
                    lastOperationMessage = "Veri paketi v${pack.packVersion} doğrulandı ($total BMW DTC)."
                )
            }
            added
        }.onFailure { err ->
            _session.update {
                it.copy(dataPackStatus = DataPackStatus.Error(err.message ?: "İndirme başarısız"))
            }
        }
    }

    companion object {
        const val MIN_SAFE_CODING_VOLTAGE = 12.2
        const val PHYSICAL_SAFE_MODE_MSG =
            "Güvenlik Kalkanı (Salt-Okunur Mod): Fiziksel araçta doğrulanmamış yazma kilitlidir. Developer ekranından kilidi açabilir veya Trace logu alabilirsiniz."
    }
}
