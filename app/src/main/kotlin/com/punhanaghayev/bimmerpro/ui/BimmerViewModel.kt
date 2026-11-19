package com.punhanaghayev.bimmerpro.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.punhanaghayev.bimmerpro.BuildConfig
import com.punhanaghayev.bimmerpro.core.bmw.ActuatorTestItem
import com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutine
import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage
import com.punhanaghayev.bimmerpro.core.i18n.AppLocalization
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import com.punhanaghayev.bimmerpro.core.protocol.ProtocolLogEntry
import com.punhanaghayev.bimmerpro.core.protocol.TcpEnetTransportAdapter
import com.punhanaghayev.bimmerpro.core.report.InspectionReportGenerator
import com.punhanaghayev.bimmerpro.core.report.TelemetryCsvExporter
import com.punhanaghayev.bimmerpro.core.service.CodingSessionService
import com.punhanaghayev.bimmerpro.core.update.AppReleaseInfo
import com.punhanaghayev.bimmerpro.core.update.OtaCheckState
import com.punhanaghayev.bimmerpro.core.update.OtaUpdateManager
import com.punhanaghayev.bimmerpro.core.update.SemVerComparator
import com.punhanaghayev.bimmerpro.data.BimmerRepository
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
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

    private val repository: BimmerRepository = BimmerRepository(application.applicationContext)

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

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    private var telemetryJob: Job? = null
    private var perfJob: Job? = null

    fun connect(
        adapterType: AdapterType,
        customHost: String = "",
        customPort: Int? = null
    ) {
        viewModelScope.launch {
            repository.connect(adapterType, customHost, customPort)
        }
    }

    fun disconnect() {
        stopLiveTelemetry()
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
            val file = withContext(Dispatchers.IO) {
                InspectionReportGenerator.generatePdf(
                    context = context,
                    profile = profile,
                    ecus = state.ecus,
                    dtcs = state.dtcs
                )
            }
            _uiEvents.emit(UiEvent.ShareFile(file, "application/pdf", "Ekspertiz PDF Raporunu Paylaş"))
        }
    }

    fun clearProtocolLogs() {
        repository.clearProtocolLogs()
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

    fun checkForOtaUpdates() {
        viewModelScope.launch {
            _otaState.value = OtaCheckState.Checking
            runCatching {
                OtaUpdateManager.fetchReleases()
            }.onSuccess { remoteReleases ->
                val combined = (remoteReleases + OtaUpdateManager.builtinChangelog)
                    .distinctBy { it.versionName }
                val latest = remoteReleases.firstOrNull()
                if (latest != null && SemVerComparator.isNewer(latest.versionName, BuildConfig.VERSION_NAME)) {
                    _otaState.value = OtaCheckState.UpdateAvailable(latest, combined)
                } else {
                    _otaState.value = OtaCheckState.UpToDate(BuildConfig.VERSION_NAME, combined)
                }
            }.onFailure { err ->
                _otaState.value = OtaCheckState.Error(err.message ?: "OTA sunucusuna erişilemedi")
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

    override fun onCleared() {
        stopLiveTelemetry()
        perfJob?.cancel()
        super.onCleared()
    }
}

sealed interface UiEvent {
    data class Snackbar(val message: String) : UiEvent
    data class ShareFile(val file: File, val mimeType: String, val chooserTitle: String) : UiEvent
    data class InstallApk(val apkFile: File) : UiEvent
}
