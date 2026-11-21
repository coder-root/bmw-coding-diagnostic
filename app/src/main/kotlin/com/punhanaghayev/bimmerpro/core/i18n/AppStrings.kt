package com.punhanaghayev.bimmerpro.core.i18n

import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import java.util.Locale

enum class AppLanguage(val code: String, val nativeName: String, val flagCode: String) {
    EN("en", "English", "EN"),
    TR("tr", "Türkçe", "TR"),
    AZ("az", "Azərbaycan", "AZ"),
    RU("ru", "Русский", "RU");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: EN

        fun detectSystemLanguage(): AppLanguage =
            fromCode(Locale.getDefault().language)
    }
}

data class AppStrings(
    val tabGarage: String,
    val tabDiagnostics: String,
    val tabCoding: String,
    val tabLiveService: String,
    val tabDeveloper: String,

    val heroSubtitle: String,
    val heroSubtitleLine2: String,
    val disconnectedHint: String,
    val connectionHubTitle: String,
    val scanHardwareBtn: String,
    val adapterDropdownLabel: String,
    val connectVehicleBtn: String,
    val reconnectBtn: String,
    val disconnectBtn: String,
    val connectingText: String,
    val discoveredDevicesTitle: String,
    val quickDiagnostics: String,
    val quickCoding: String,
    val quickLiveService: String,

    val scanAllEcusBtn: String,
    val scanningText: String,
    val expertisePdfBtn: String,
    val mileageCheckTitle: String,
    val clearAllDtcBtn: String,
    val noDtcInFilter: String,
    val scannedEcusTitle: String,

    val codingEasyMode: String,
    val codingExpertCafd: String,
    val codingFaVoBackup: String,
    val batterySafeBanner: String,
    val batteryLockedBanner: String,

    val liveTelemetryTitle: String,
    val startBtn: String,
    val stopBtn: String,
    val serviceRoutinesTitle: String,
    val runRoutineBtn: String,
    val serviceHistoryTitle: String,
    val writeRecordBtn: String,

    val languageTitle: String,
    val otaTitle: String,
    val checkUpdatesBtn: String,
    val changelogTitle: String,
    val supportedArchTitle: String,
    val packetInspectorTitle: String,
    val packetInspectorSub: String,
    val clearBtn: String,
    val themeTitle: String,
    val savedVehiclesTitle: String,
    val exportJsonBtn: String,
    val importJsonBtn: String,
    val ecuResetTitle: String,
    val gatewaySleepBtn: String,
    val hardResetBtn: String,
    val disconnectedDiagTitle: String,
    val disconnectedDiagSub: String,
    val disconnectedCodingTitle: String,
    val disconnectedCodingSub: String,
    val disconnectedLiveTitle: String,
    val disconnectedLiveSub: String,
    val goToGarageBtn: String,
    val filterAll: String,
    val filterWarn: String,
    val filterShadow: String,
    val clearEcuDtcBtn: String,
    val ecuResetBtn: String,
    val statusClean: String,
    val statusFault: String,
    val searchCodingHint: String,
    val searchCafdHint: String,
    val faVoTitle: String,
    val codeLabel: String,
    val descLabel: String,
    val addVoBtn: String,
    val autoBackupsTitle: String,
    val noBackupsHint: String,
    val rollbackBtn: String,
    val currentValueLabel: String,
    val perfTimerTitle: String,
    val sportGaugeTitle: String,
    val zfAdaptTitle: String,
    val resetBtn: String,
    val injectorTitle: String,
    val codeBtn: String,
    val selectVersionLabel: String,

    // v1.1.0 Extended Full Localization Strings
    val connectModeLabel: String,
    val simulatorModeLabel: String,
    val startSimulatorBtn: String,
    val connectionPurposeTitle: String,
    val purposeDiagnosticsLabel: String,
    val purposeCodingLabel: String,
    val purposeDiagnosticsSub: String,
    val purposeCodingSub: String,
    val chassisLabel: String,
    val engineLabel: String,
    val currentILevelLabel: String,
    val factoryILevelLabel: String,
    val mileageLabel: String,
    val typeProdLabel: String,
    val paintLabel: String,
    val upholsteryLabel: String,
    val showBtn: String,
    val hideBtn: String,
    val vehicleEquipmentLabel: String,
    val codeEquipmentBtn: String,

    val safeModeBannerLocked: String,
    val safeModeBannerUnlocked: String,
    val offlineHealthTitle: String,
    val dtcDictionaryTitle: String,
    val dtcDictionaryHint: String,

    val categoryFilterLabel: String,
    val selectModuleLabel: String,
    val allModulesLabel: String,
    val codingModulesHint: String,
    val backToModulesBtn: String,
    val diffPreviewTitle: String,
    val diffBeforeLabel: String,
    val diffAfterLabel: String,
    val applyBtn: String,
    val cancelBtn: String,

    val customizeSensorsBtn: String,
    val customizeSensorsTitle: String,
    val reorderHintLabel: String,
    val editOrderBtn: String,
    val doneBtn: String,
    val ibsHealthTitle: String,
    val actuatorTestsTitle: String,
    val testBtn: String,
    val notePrefix: String,

    val selectSeriesLabel: String,
    val supportedModelsLabel: String,
    val dataPackTitle: String,
    val dataPackDownloadBtn: String,
    val dataPackDownloading: String,
    val dataPackUpToDate: String,
    val dataPackApplied: String,
    val physicalSafeUnlockTitle: String,
    val noSavedVehiclesHint: String,
    val noPacketLogsHint: String,
    val traceExportBtn: String,

    val welcomeLangTitle: String,
    val welcomeLangSub: String,
    val continueBtn: String,
    val disclaimerTitle: String,
    val disclaimerBullet1: String,
    val disclaimerBullet2: String,
    val disclaimerBullet3: String,
    val disclaimerAcceptBtn: String,
    val updateAvailableTitle: String,
    val updateNowBtn: String,
    val updateLaterBtn: String,

    val vehicleTechnicalDetailsTitle: String,
    val supportedBmwHeader: String,
    val architectureProtocolSubtitle: String,
    val operationProgressTitle: String,
    val operationCompletedTitle: String,
    val operationFailedTitle: String,
    val operationDoneBtn: String,

    val tabLive: String,
    val tabService: String,
    val quickLive: String,
    val quickService: String,
    val exportCsvBtn: String,
    val stagedChangesTitle: String,
    val codeVehicleBatchBtn: String,
    val confirmCodingSafetyTitle: String,
    val confirmCodingSafetyWarning: String,
    val confirmCodingCheckbox: String,
    val discardStagedBtn: String,
    val simVoltageTestLabel: String,
    val tamperVerified: String,
    val tamperMinorDrift: String,
    val tamperSuspicious: String,
    val dtcFilterDropdownLabel: String,
    val subsystemEngineTurbo: String,
    val subsystemDpfExhaust: String,
    val subsystemZf8: String,
    val subsystemBatteryIbs: String,
    val healthNominal: String,
    val healthFaultCheck: String,
    val healthDpfClean: String,
    val healthDpfHigh: String,
    val selectBatteryCapacityTitle: String,
    val ibsCapacityResetPrompt: String,
    val injectorImaDialogTitle: String,
    val injectorImaInputLabel: String,
    val writeDmeBtn: String,
    val writeServiceHistoryTitle: String,
    val dealerCodeLabel: String,
    val socLabel: String,
    val sohLabel: String,
    val terminalVoltageLabel: String,
    val leadAcidLabel: String,
    val cylinderPrefix: String,
    val stagedOptionPending: String,
    val zfClutchDevLabel: String,
    val recCriticalDtcs: String,
    val recWarningDtcs: String,
    val recDpfSootHigh: String,
    val recBatteryLow: String,
    val recHealthy: String,
    val dpfMonitorTitle: String,
    val dpfSootLabel: String,
    val dpfAshLabel: String,
    val dpfBackpressureLabel: String,
    val dpfLastRegenLabel: String,
    val dpfPreTempLabel: String,
    val dpfPostTempLabel: String,
    val dpfStatusActive: String,
    val dpfStatusIdle: String,
    val dpfTriggerBtn: String,
    val zfHealthScoreLabel: String,
    val zfOilTempLabel: String,
    val zfOilConditionLabel: String,
    val zfClutchOptimal: String,
    val zfClutchWarn: String,

    val dtcStatusActive: String,
    val dtcStatusStored: String,
    val dtcStatusPending: String,
    val dtcStatusIntermittent: String,
    val dtcStatusShadow: String,

    val routineConfirmTitle: String,
    val routineConfirmWarning: String,
    val routineConfirmPreconditions: String,
    val rememberChoiceSession: String,

    val zfResetConfirmTitle: String,
    val zfResetConfirmText: String,

    val istaControlPlan: String,
    val freezeFrameOccurrences: String,

    val batteryVoltageLabel: String,
    val batterySafeLabel: String,
    val batteryLowLabel: String,
    val batteryLockedLabel: String,

    val bestTimerLabel: String,

    val secureCodingWarningTitle: String,
    val secureCodingWarningText: String,
    val kLineWarningTitle: String,
    val kLineWarningText: String,

    val moduleKombi: String,
    val moduleDmePetrol: String,
    val moduleDdeDiesel: String,
    val moduleZf8: String,

    val language: AppLanguage
) {
    var hvBatteryTitle: String = ""
    var hvSohLabel: String = ""
    var hvCapacityLabel: String = ""
    var cellDeltaLabel: String = ""
    var insulationResistanceLabel: String = ""
    var contactorStatusLabel: String = ""
    var adasTitle: String = ""
    var kafasStatusLabel: String = ""
    var accOffsetLabel: String = ""
    var pdfQrVerificationLabel: String = ""
    var pdfServiceHistoryTitle: String = ""
    var pdfEcuSwVersionTitle: String = ""

    var clearConfirmTitle: String = ""
    var clearConfirmBody: String = ""
    var clearConfirmAction: String = ""
    var hvCellDistributionLabel: String = ""
    var contactorClosedLabel: String = ""
    var contactorOpenLabel: String = ""
    var moduleTempsLabel: String = ""
    var radarOkLabel: String = ""
    var radarAlignRequiredLabel: String = ""
    var toleranceLabel: String = ""

    // BimmerLink-inspired Extended Diagnostic, Live Chart/Dashboard & Service Strings
    var dtcMemoryErrorLabel: String = "Error Memory"
    var dtcMemoryInfoLabel: String = "Info Memory"
    var dtcMemoryPermanentLabel: String = "Permanent"
    var liveModeListLabel: String = "List"
    var liveModeChartLabel: String = "Chart"
    var liveModeDashboardLabel: String = "Dashboard"
    var liveChartSelectHint: String = "Tap sensors to plot live telemetry curves (up to 4 channels)"
    var shortCircuitTitle: String = "FRM / FEM / BDC Short-Circuit Lock Reset"
    var shortCircuitSub: String = "Resets output shutdown counters (50 faults) after bulb/wiring repair."
    var shortCircuitLockedLabel: String = "LOCKED"
    var shortCircuitOkLabel: String = "OK"
    var shortCircuitResetBtn: String = "Reset Lock"
    var soundTuningTitle: String = "Exhaust Flap & Active Sound Design (ASD)"
    var exhaustFlapLabel: String = "Exhaust Flap Control"
    var exhaustAutoLabel: String = "OEM Auto"
    var exhaustOpenLabel: String = "Always Open"
    var exhaustClosedLabel: String = "Closed"
    var asdEnableLabel: String = "Active Sound Design (ASD)"
    var asdVolumeLabel: String = "ASD Acoustic Gain"

    // Extended Service, Android Auto Config & Support Bundle Strings
    var dpfPreconditionsTitle: String = "Regeneration Preconditions"
    var dpfCondCoolant: String = "Coolant Temp > 75°C"
    var dpfCondExhaust: String = "Exhaust Temp > 240°C"
    var dpfCondVoltage: String = "Battery >= 12.2 V"
    var dpfCondNoCriticalDtc: String = "No Critical Engine Faults"
    var epbCardTitle: String = "Electronic Parking Brake (EPB) Service Mode"
    var epbStatusNormal: String = "NORMAL"
    var epbStatusOpen: String = "SERVICE MODE OPEN"
    var epbEnterBtn: String = "Open (Retract)"
    var epbExitBtn: String = "Close & Calibrate"
    var ibs5DayHistoryLabel: String = "5-Day SoC History"
    var androidAutoSensorsBtn: String = "Android Auto"
    var androidAutoSensorsTitle: String = "Android Auto Display Sensors (Max 4)"
    var supportBundleTitle: String = "Diagnostics Support Bundle"
    var supportBundleBtn: String = "Create Support Bundle"
    var supportBundleSub: String = "Exports vehicle VIN, ECU SVT tree, 3-layer DTCs, and UDS/AT logs."
    fun adapterTitle(type: AdapterType): String = when (language) {
        AppLanguage.AZ -> when (type) {
            AdapterType.ENET_USB -> "USB-C to ENET Kabel"
            AdapterType.USB_KDCAN -> "USB OTG K+DCAN Kabel"
            AdapterType.SIMULATOR_F10 -> "BMW F10 520d Simulyasiyası"
            AdapterType.SIMULATOR_F10_528I -> "BMW F10 528i (N20) Simulyasiyası"
            AdapterType.SIMULATOR_F30 -> "BMW F30 328i Simulyasiyası"
            AdapterType.SIMULATOR_G30 -> "BMW G30 530i Simulyasiyası"
            AdapterType.SIMULATOR_G20 -> "BMW G20 M340i Simulyasiyası"
            AdapterType.SIMULATOR_U11 -> "BMW U11 iX1 / X1 Simulyasiyası"
            AdapterType.SIMULATOR_E90 -> "BMW E90 335i Simulyasiyası"
            else -> type.displayName
        }
        AppLanguage.TR -> when (type) {
            AdapterType.ENET_USB -> "USB-C to ENET Kablo"
            AdapterType.USB_KDCAN -> "USB OTG K+DCAN Kablo"
            AdapterType.SIMULATOR_F10 -> "BMW F10 520d Simülasyonu"
            AdapterType.SIMULATOR_F10_528I -> "BMW F10 528i (N20) Simülasyonu"
            AdapterType.SIMULATOR_F30 -> "BMW F30 328i Simülasyonu"
            AdapterType.SIMULATOR_G30 -> "BMW G30 530i Simülasyonu"
            AdapterType.SIMULATOR_G20 -> "BMW G20 M340i Simülasyonu"
            AdapterType.SIMULATOR_U11 -> "BMW U11 iX1 / X1 Simülasyonu"
            AdapterType.SIMULATOR_E90 -> "BMW E90 335i Simülasyonu"
            else -> type.displayName
        }
        AppLanguage.EN -> when (type) {
            AdapterType.SIMULATOR_F10 -> "BMW F10 520d Simulation"
            AdapterType.SIMULATOR_F10_528I -> "BMW F10 528i (N20 Petrol) Simulation"
            AdapterType.SIMULATOR_F30 -> "BMW F30 328i Simulation"
            AdapterType.SIMULATOR_G30 -> "BMW G30 530i Simulation"
            AdapterType.SIMULATOR_G20 -> "BMW G20 M340i Simulation"
            AdapterType.SIMULATOR_U11 -> "BMW U11 iX1 / X1 Simulation"
            AdapterType.SIMULATOR_E90 -> "BMW E90 335i Simulation"
            else -> type.displayName
        }
        AppLanguage.RU -> when (type) {
            AdapterType.ENET_USB -> "Кабель USB-C — ENET"
            AdapterType.USB_KDCAN -> "Кабель USB OTG K+DCAN"
            AdapterType.SIMULATOR_F10 -> "Симуляция BMW F10 520d"
            AdapterType.SIMULATOR_F10_528I -> "Симуляция BMW F10 528i (N20)"
            AdapterType.SIMULATOR_F30 -> "Симуляция BMW F30 328i"
            AdapterType.SIMULATOR_G30 -> "Симуляция BMW G30 530i"
            AdapterType.SIMULATOR_G20 -> "Симуляция BMW G20 M340i"
            AdapterType.SIMULATOR_U11 -> "Симуляция BMW U11 iX1 / X1"
            AdapterType.SIMULATOR_E90 -> "Симуляция BMW E90 335i"
            else -> type.displayName
        }
    }

    fun adapterDesc(type: AdapterType): String = when (language) {
        AppLanguage.AZ -> when (type) {
            AdapterType.SIMULATOR_F10 -> "Virtual HSFZ / ENET + UDS"
            AdapterType.SIMULATOR_F10_528I -> "Virtual HSFZ / ENET + N20 Benzin"
            AdapterType.SIMULATOR_F30 -> "Virtual HSFZ / ENET + N20"
            AdapterType.SIMULATOR_G30 -> "Virtual DoIP / HSFZ + Secure Coding"
            AdapterType.SIMULATOR_G20 -> "Virtual DoIP / B58 + iDrive 7"
            AdapterType.SIMULATOR_U11 -> "Virtual DoIP / OS 9 + U-Seriyası"
            AdapterType.SIMULATOR_E90 -> "Virtual K+DCAN / KM Yoxlaması Demo"
            else -> type.protocolSummary
        }
        AppLanguage.TR -> type.protocolSummary
        AppLanguage.EN -> type.protocolSummary
        AppLanguage.RU -> when (type) {
            AdapterType.SIMULATOR_F10 -> "Виртуальный HSFZ / ENET + UDS"
            AdapterType.SIMULATOR_F10_528I -> "Виртуальный HSFZ / ENET + N20 Бензин"
            AdapterType.SIMULATOR_F30 -> "Виртуальный HSFZ / ENET + N20"
            AdapterType.SIMULATOR_G30 -> "Виртуальный DoIP / HSFZ + Secure Coding"
            AdapterType.SIMULATOR_G20 -> "Виртуальный DoIP / B58 + iDrive 7"
            AdapterType.SIMULATOR_U11 -> "Виртуальный DoIP / OS 9 + U-Серия"
            AdapterType.SIMULATOR_E90 -> "Виртуальный K+DCAN / Тест пробега"
            else -> type.protocolSummary
        }
    }
}
