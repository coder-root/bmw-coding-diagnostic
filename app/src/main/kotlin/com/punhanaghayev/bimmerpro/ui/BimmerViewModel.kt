package com.punhanaghayev.bimmerpro.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.punhanaghayev.bimmerpro.BuildConfig
import com.punhanaghayev.bimmerpro.core.bmw.ActuatorTestItem
import com.punhanaghayev.bimmerpro.core.bmw.DtcFault
import com.punhanaghayev.bimmerpro.core.bmw.ExhaustFlapMode
import com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutine
import com.punhanaghayev.bimmerpro.core.bmw.ShortCircuitChannel
import com.punhanaghayev.bimmerpro.core.bmw.VoCodingPlan
import com.punhanaghayev.bimmerpro.data.DragMode
import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage
import com.punhanaghayev.bimmerpro.core.i18n.AppLocalization
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import com.punhanaghayev.bimmerpro.core.protocol.ConnectionState
import com.punhanaghayev.bimmerpro.core.protocol.ProtocolLogEntry
import com.punhanaghayev.bimmerpro.core.protocol.SessionTraceRecorder
import com.punhanaghayev.bimmerpro.core.protocol.TcpEnetTransportAdapter
import com.punhanaghayev.bimmerpro.core.report.InspectionReportGenerator
import com.punhanaghayev.bimmerpro.core.report.TelemetryCsvExporter
import com.punhanaghayev.bimmerpro.core.service.CodingSessionService
import com.punhanaghayev.bimmerpro.core.update.AppReleaseInfo
import com.punhanaghayev.bimmerpro.core.update.OtaCheckState
import com.punhanaghayev.bimmerpro.core.update.OtaUpdateManager
import com.punhanaghayev.bimmerpro.core.update.SemVerComparator
import com.punhanaghayev.bimmerpro.data.BimmerRepository
import com.punhanaghayev.bimmerpro.data.BimmerRepositoryProvider
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import com.punhanaghayev.bimmerpro.data.ConnectionPurpose
import com.punhanaghayev.bimmerpro.ui.theme.AppThemeStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class BimmerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: BimmerRepository = BimmerRepositoryProvider.get(application.applicationContext)

    val session: StateFlow<BimmerSessionState> = repository.session
    val protocolLogs: StateFlow<List<ProtocolLogEntry>> = repository.protocolLogs

    private val _isLiveStreamRunning = MutableStateFlow(false)
    val isLiveStreamRunning: StateFlow<Boolean> = _isLiveStreamRunning.asStateFlow()

    private val _otaState = MutableStateFlow<OtaCheckState>(OtaCheckState.Idle)
    val otaState: StateFlow<OtaCheckState> = _otaState.asStateFlow()

    private val _appLanguage = MutableStateFlow(AppLocalization.loadSavedLanguage(application))
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _themeStyle = MutableStateFlow(AppThemeStyle.loadSaved(application))
    val themeStyle: StateFlow<AppThemeStyle> = _themeStyle.asStateFlow()

    private val prefs = application.getSharedPreferences("bimmerpro_prefs", Context.MODE_PRIVATE)

    private val _isDisclaimerAccepted = MutableStateFlow(prefs.getBoolean("disclaimer_accepted_v1", false))
    val isDisclaimerAccepted: StateFlow<Boolean> = _isDisclaimerAccepted.asStateFlow()

    private val _isLanguageConfirmed = MutableStateFlow(AppLocalization.hasConfirmedLanguage(application))
    val isLanguageConfirmed: StateFlow<Boolean> = _isLanguageConfirmed.asStateFlow()

    private val _otaPromptDismissed = MutableStateFlow(false)
    val otaPromptDismissed: StateFlow<Boolean> = _otaPromptDismissed.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    private var telemetryJob: Job? = null
    private var perfJob: Job? = null
    private var keepAliveJob: Job? = null

    fun confirmInitialLanguage(language: AppLanguage) {
        AppLocalization.saveLanguage(getApplication(), language, markConfirmed = true)
        _appLanguage.value = language
        _isLanguageConfirmed.value = true
    }

    fun dismissOtaPrompt() {
        _otaPromptDismissed.value = true
    }

    fun acceptDisclaimer() {
        prefs.edit().putBoolean("disclaimer_accepted_v1", true).apply()
        _isDisclaimerAccepted.value = true
    }

    fun connect(
        adapterType: AdapterType,
        customHost: String = "",
        customPort: Int? = null,
        purpose: ConnectionPurpose = ConnectionPurpose.DIAGNOSTICS
    ) {
        keepAliveJob?.cancel()
        keepAliveJob = null
        viewModelScope.launch {
            repository.connect(adapterType, customHost, customPort, purpose = purpose)
            if (!adapterType.isSimulator && session.value.connectionState is ConnectionState.Connected) {
                keepAliveJob = viewModelScope.launch {
                    while (isActive && session.value.connectionState is ConnectionState.Connected) {
                        delay(2500)
                        repository.sendTesterPresentKeepAlive()
                    }
                }
            }
        }
    }

    fun disconnect() {
        stopLiveTelemetry()
        keepAliveJob?.cancel()
        keepAliveJob = null
        viewModelScope.launch {
            repository.disconnect()
        }
    }

    fun runFullScan() {
        viewModelScope.launch {
            repository.runFullVehicleScan()
        }
    }

    fun clearAllDtcs() {
        viewModelScope.launch {
            repository.clearAllDtcs()
            _uiEvents.emit(UiEvent.Snackbar("Tüm hata kodları temizlendi."))
        }
    }

    fun clearEcuDtcs(ecuAddress: Int, ecuName: String) {
        viewModelScope.launch {
            repository.clearEcuDtcs(ecuAddress, ecuName)
            _uiEvents.emit(UiEvent.Snackbar("$ecuName hata kodları temizlendi."))
        }
    }

    fun applyPresetCoding(presetId: String, newIndex: Int) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            CodingSessionService.start(ctx, "ECU FDL Kodlama Yazılıyor...")
            try {
                val res = repository.applyPresetCoding(presetId, newIndex)
                res.onSuccess {
                    _uiEvents.emit(UiEvent.Snackbar("Kodlama tamamlandı ve yedek oluşturuldu."))
                }.onFailure { err ->
                    _uiEvents.emit(UiEvent.Snackbar(err.message ?: "Kodlama başarısız"))
                }
            } finally {
                CodingSessionService.stop(ctx)
            }
        }
    }

    fun applyBatchCoding(presets: Map<String, Int>, cafds: Map<String, String>) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val total = presets.size + cafds.size
            if (total == 0) return@launch
            CodingSessionService.start(ctx, "Araca $total adet kodlama yazılıyor...")
            var successCount = 0
            try {
                for ((presetId, newIndex) in presets) {
                    val res = repository.applyPresetCoding(presetId, newIndex)
                    if (res.isSuccess) successCount++
                }
                for ((paramId, optLabel) in cafds) {
                    val res = repository.applyCafdParameter(paramId, optLabel)
                    if (res.isSuccess) successCount++
                }
                _uiEvents.emit(UiEvent.Snackbar("$successCount / $total kodlama başarıyla uygulandı."))
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.Snackbar("Kodlama sırasında hata: ${e.message}"))
            } finally {
                CodingSessionService.stop(ctx)
            }
        }
    }

    fun applyCafdParameter(paramId: String, optionLabel: String) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            CodingSessionService.start(ctx, "Expert CAFD Parametresi Yazılıyor...")
            try {
                val res = repository.applyCafdParameter(paramId, optionLabel)
                res.onSuccess {
                    _uiEvents.emit(UiEvent.Snackbar("Expert FDL parametresi yazıldı."))
                }.onFailure { err ->
                    _uiEvents.emit(UiEvent.Snackbar(err.message ?: "FDL yazma hatası"))
                }
            } finally {
                CodingSessionService.stop(ctx)
            }
        }
    }

    fun restoreBackup(backupId: String) {
        viewModelScope.launch {
            repository.restoreBackup(backupId)
            _uiEvents.emit(UiEvent.Snackbar("Önceki ECU kodlama yedeği geri yüklendi."))
        }
    }

    fun addVehicleOrderOption(code: String, description: String) {
        viewModelScope.launch {
            repository.addVehicleOrderOption(code, description)
            _uiEvents.emit(UiEvent.Snackbar("FA/VO opsiyonu eklendi: ${code.uppercase()}"))
        }
    }

    fun createFullNcdSnapshot() {
        viewModelScope.launch {
            repository.createFullNcdSnapshot()
            _uiEvents.emit(UiEvent.Snackbar("Tam ECU NCD Snapshot kaydedildi."))
        }
    }

    fun executeServiceRoutine(routine: ServiceRoutine, optionByte: Byte = 0x01) {
        viewModelScope.launch {
            repository.executeServiceRoutine(routine, optionByte)
        }
    }

    fun writeServiceHistory(date: String, mileageKm: Int, dealerCode: String, items: List<String>) {
        viewModelScope.launch {
            repository.writeServiceHistoryRecord(date, mileageKm, dealerCode, items)
            _uiEvents.emit(UiEvent.Snackbar("iDrive servis geçmişi kaydı yazıldı."))
        }
    }

    fun writeInjectorCode(cylinder: Int, imaCode: String) {
        viewModelScope.launch {
            repository.writeInjectorCode(cylinder, imaCode)
            _uiEvents.emit(UiEvent.Snackbar("Silindir $cylinder IMA kodu yazıldı."))
        }
    }

    fun resetZfAdaptations() {
        viewModelScope.launch {
            repository.resetZfAdaptations()
            _uiEvents.emit(UiEvent.Snackbar("ZF8 EGS adaptasyonları sıfırlandı."))
        }
    }

    fun runActuatorTest(test: ActuatorTestItem) {
        viewModelScope.launch {
            repository.runActuatorTest(test)
        }
    }

    fun resetShortCircuitChannel(channel: ShortCircuitChannel) {
        viewModelScope.launch {
            repository.resetShortCircuitChannel(channel)
            _uiEvents.emit(UiEvent.Snackbar(repository.session.value.lastOperationMessage ?: channel.channelName))
        }
    }

    fun setExhaustFlapMode(mode: ExhaustFlapMode) {
        viewModelScope.launch {
            repository.setExhaustFlapMode(mode)
            _uiEvents.emit(UiEvent.Snackbar(repository.session.value.lastOperationMessage ?: mode.name))
        }
    }

    fun setAsdState(enabled: Boolean, volumePercent: Int) {
        viewModelScope.launch {
            repository.setAsdState(enabled, volumePercent)
            _uiEvents.emit(UiEvent.Snackbar(repository.session.value.lastOperationMessage ?: "ASD"))
        }
    }

    fun setEpbServiceMode(enterServiceMode: Boolean) {
        viewModelScope.launch {
            repository.setEpbServiceMode(enterServiceMode)
            _uiEvents.emit(UiEvent.Snackbar(repository.session.value.lastOperationMessage ?: "EPB"))
        }
    }

    fun fetchDtcFreezeFrame(dtc: DtcFault) {
        viewModelScope.launch {
            repository.fetchDtcFreezeFrame(dtc)
        }
    }

    fun toggleLiveTelemetry() {
        if (_isLiveStreamRunning.value) {
            stopLiveTelemetry()
        } else {
            startLiveTelemetry()
        }
    }

    private fun startLiveTelemetry() {
        if (telemetryJob?.isActive == true) return
        _isLiveStreamRunning.value = true
        telemetryJob = viewModelScope.launch {
            while (isActive) {
                repository.stepLiveTelemetry()
                delay(550)
            }
        }
    }

    private fun stopLiveTelemetry() {
        telemetryJob?.cancel()
        telemetryJob = null
        _isLiveStreamRunning.value = false
    }

    fun adjustSimulatedVoltage(voltage: Double) {
        repository.adjustSimulatedVoltage(voltage)
    }

    fun generateInspectionPdf(context: Context) {
        val state = session.value
        val profile = state.vehicleProfile ?: return
        viewModelScope.launch {
            runCatching {
                val file = withContext(Dispatchers.IO) {
                    InspectionReportGenerator.generatePdf(
                        context = context,
                        profile = profile,
                        ecus = state.ecus,
                        dtcs = state.dtcs,
                        ibsHealth = state.ibsHealth,
                        dpfHealth = state.dpfHealth,
                        zfHealth = state.zfHealth,
                        serviceHistory = state.serviceHistory,
                        hvBatteryHealth = state.hvBatteryHealth
                    )
                }
                _uiEvents.emit(UiEvent.ShareFile(file, "application/pdf", "BMW Expert PRO PDF Report"))
            }.onFailure { err ->
                _uiEvents.emit(UiEvent.Snackbar("PDF Error: ${err.message ?: "Unknown"}"))
            }
        }
    }

    fun generateInspectionHtml(context: Context) {
        val state = session.value
        val profile = state.vehicleProfile ?: return
        viewModelScope.launch {
            runCatching {
                val file = withContext(Dispatchers.IO) {
                    InspectionReportGenerator.generateHtmlReport(
                        context = context,
                        profile = profile,
                        ecus = state.ecus,
                        dtcs = state.dtcs,
                        ibsHealth = state.ibsHealth,
                        dpfHealth = state.dpfHealth,
                        zfHealth = state.zfHealth,
                        serviceHistory = state.serviceHistory,
                        hvBatteryHealth = state.hvBatteryHealth
                    )
                }
                _uiEvents.emit(UiEvent.ShareFile(file, "text/html", "BMW Expert PRO HTML Report"))
            }.onFailure { err ->
                _uiEvents.emit(UiEvent.Snackbar("HTML Error: ${err.message ?: "Unknown"}"))
            }
        }
    }

    fun clearProtocolLogs() {
        repository.clearProtocolLogs()
    }

    fun dismissActiveOperation() {
        repository.dismissActiveOperation()
    }

    fun scanHardware() {
        viewModelScope.launch {
            repository.scanHardware()
        }
    }

    fun selectBleDevice(address: String) {
        repository.selectBleDevice(address)
    }

    fun setAppLanguage(language: AppLanguage) {
        AppLocalization.saveLanguage(getApplication(), language)
        _appLanguage.value = language
    }

    fun setThemeStyle(style: AppThemeStyle) {
        AppThemeStyle.save(getApplication(), style)
        _themeStyle.value = style
    }

    fun removeSavedVehicle(vin: String) {
        repository.removeSavedVehicle(vin)
    }

    fun sendEcuReset(ecuAddress: Int, ecuName: String, resetType: Int = 0x01) {
        viewModelScope.launch {
            repository.sendEcuReset(ecuAddress, ecuName, resetType)
            _uiEvents.emit(UiEvent.Snackbar("$ecuName reset/uyku komutu gönderildi."))
        }
    }

    fun exportBackupsJson(context: Context) {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                repository.exportBackupsJson(context.cacheDir)
            }
            _uiEvents.emit(UiEvent.ShareFile(file, "application/json", "Kodlama Yedeklerini JSON Paylaş"))
        }
    }

    fun importBackupsJson(rawJson: String) {
        viewModelScope.launch {
            runCatching { repository.importBackupsJson(rawJson) }
                .onSuccess { count -> _uiEvents.emit(UiEvent.Snackbar("$count yedek içe aktarıldı.")) }
                .onFailure { _uiEvents.emit(UiEvent.Snackbar("Geçersiz JSON yedek dosyası.")) }
        }
    }

    fun exportTelemetryCsv(context: Context) {
        val state = session.value
        val chassisCode = state.vehicleProfile?.chassis?.code ?: return
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                TelemetryCsvExporter.exportCsv(context, chassisCode, state.liveChannels)
            }
            _uiEvents.emit(UiEvent.ShareFile(file, "text/csv", "Canlı Telemetri CSV Paylaş"))
        }
    }

    fun checkForOtaUpdates(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _otaState.value = OtaCheckState.Checking
            runCatching {
                OtaUpdateManager.fetchReleases()
            }.onSuccess { remoteReleases ->
                val combined = (remoteReleases + OtaUpdateManager.builtinChangelog)
                    .distinctBy { it.versionName }
                val latest = remoteReleases.firstOrNull()
                if (latest != null && SemVerComparator.isNewer(latest.versionName, BuildConfig.VERSION_NAME)) {
                    _otaState.value = OtaCheckState.UpdateAvailable(latest, combined)
                } else if (!silent) {
                    _otaState.value = OtaCheckState.UpToDate(BuildConfig.VERSION_NAME, combined)
                }
            }.onFailure { err ->
                if (!silent) {
                    _otaState.value = OtaCheckState.Error(err.message ?: "OTA server unreachable")
                }
            }
        }
    }

    fun downloadAndInstallOta(context: Context, release: AppReleaseInfo) {
        val url = release.apkDownloadUrl ?: run {
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.Snackbar("Bu sürümde indirilebilir APK dosyası bulunamadı."))
            }
            return
        }
        viewModelScope.launch {
            _otaState.value = OtaCheckState.Downloading(0f, release.versionName)
            runCatching {
                OtaUpdateManager.downloadApk(url, context.cacheDir, release.versionName) { p ->
                    _otaState.value = OtaCheckState.Downloading(p, release.versionName)
                }
            }.onSuccess { apkFile ->
                _otaState.value = OtaCheckState.ReadyToInstall(apkFile, release.versionName)
                _uiEvents.emit(UiEvent.InstallApk(apkFile))
            }.onFailure { err ->
                _otaState.value = OtaCheckState.Error("İndirme hatası: ${err.message}")
            }
        }
    }

    fun togglePerformanceRun() {
        if (session.value.performanceTimer.isRunning) {
            perfJob?.cancel()
            perfJob = null
            repository.stopPerformanceRun()
        } else {
            repository.startPerformanceRun()
            perfJob?.cancel()
            perfJob = viewModelScope.launch {
                while (isActive && session.value.performanceTimer.isRunning) {
                    delay(200)
                    repository.stepPerformanceTimer(200L)
                }
            }
        }
    }

    fun setPhysicalWriteUnlocked(unlocked: Boolean) {
        repository.setPhysicalWriteUnlocked(unlocked)
    }

    fun availableLiveChannels() = repository.availableLiveChannelCatalog()

    fun setSelectedLivePids(orderedIds: List<String>) {
        repository.setSelectedLivePids(orderedIds)
    }

    fun moveLiveChannel(fromIndex: Int, toIndex: Int) {
        repository.moveLiveChannel(fromIndex, toIndex)
    }

    fun setAndroidAutoPids(orderedIds: List<String>) {
        repository.setAndroidAutoPids(orderedIds)
        viewModelScope.launch {
            _uiEvents.emit(UiEvent.Snackbar("Android Auto sensörleri güncellendi."))
        }
    }

    fun exportSupportBundle(context: Context) {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                repository.exportSupportBundle(context.cacheDir)
            }
            _uiEvents.emit(UiEvent.ShareFile(file, "text/plain", "Destek & Teşhis Paketini Paylaş"))
        }
    }

    fun downloadDataPack() {
        viewModelScope.launch {
            repository.downloadDataPack()
                .onSuccess { added ->
                    _uiEvents.emit(
                        UiEvent.Snackbar(
                            if (added > 0) "Data pack updated: +$added codes (${session.value.dtcDictionaryCount} total)."
                            else "Data pack is up to date (${session.value.dtcDictionaryCount} codes)."
                        )
                    )
                }
                .onFailure { err ->
                    _uiEvents.emit(UiEvent.Snackbar("Data pack error: ${err.message ?: "Network error"}"))
                }
        }
    }

    fun exportSessionTrace(context: Context) {
        val state = session.value
        val logs = protocolLogs.value
        if (logs.isEmpty()) {
            viewModelScope.launch { _uiEvents.emit(UiEvent.Snackbar("Dışa aktarılacak paket logu yok.")) }
            return
        }
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                SessionTraceRecorder.exportTraceFile(
                    cacheDir = context.cacheDir,
                    vin = state.vehicleProfile?.vin ?: "NO_VIN",
                    adapterName = state.selectedAdapter.displayName,
                    logs = logs
                )
            }
            _uiEvents.emit(UiEvent.ShareFile(file, "text/plain", "Protokol Trace Dosyasını Paylaş"))
        }
    }

    fun previewVoCodingPlan(code: String, description: String) {
        val plan = repository.previewVoCodingPlan(code, description)
        if (plan == null) {
            viewModelScope.launch { _uiEvents.emit(UiEvent.Snackbar("Geçerli bir VO kodu girin (örn: 6WB veya 2TB).")) }
        }
    }

    fun dismissVoCodingPlan() {
        repository.dismissVoCodingPlan()
    }

    fun applyVoCodingPlan(plan: VoCodingPlan) {
        viewModelScope.launch {
            repository.applyVoCodingPlan(plan)
            session.value.lastOperationMessage?.let { _uiEvents.emit(UiEvent.Snackbar(it)) }
        }
    }

    fun readFscCertificates() {
        viewModelScope.launch {
            repository.readFscCertificates()
            session.value.lastOperationMessage?.let { _uiEvents.emit(UiEvent.Snackbar(it)) }
        }
    }

    fun setDragMode(mode: DragMode) {
        perfJob?.cancel()
        repository.setDragMode(mode)
    }

    fun runVirtualDynoPull() {
        val result = repository.runVirtualDynoPull()
        viewModelScope.launch {
            _uiEvents.emit(UiEvent.Snackbar("Virtual Dyno: ${result.summary}"))
        }
    }

    fun connectWithTraceReplay(traceText: String) {
        viewModelScope.launch {
            repository.connectWithTraceReplay(traceText)
                .onSuccess { count ->
                    _uiEvents.emit(UiEvent.Snackbar("Trace Playback: $count UDS frame yüklendi."))
                }
                .onFailure { err ->
                    _uiEvents.emit(UiEvent.Snackbar(err.message ?: "Trace dosyası okunamadı."))
                }
        }
    }

    override fun onCleared() {
        stopLiveTelemetry()
        keepAliveJob?.cancel()
        perfJob?.cancel()
        super.onCleared()
    }
}

sealed interface UiEvent {
    data class Snackbar(val message: String) : UiEvent
    data class ShareFile(val file: File, val mimeType: String, val chooserTitle: String) : UiEvent
    data class InstallApk(val apkFile: File) : UiEvent
}
