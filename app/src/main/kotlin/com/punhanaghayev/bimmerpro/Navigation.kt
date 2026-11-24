package com.punhanaghayev.bimmerpro

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage
import com.punhanaghayev.bimmerpro.core.i18n.AppLocalization
import com.punhanaghayev.bimmerpro.core.i18n.AppStrings
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import com.punhanaghayev.bimmerpro.core.protocol.ConnectionState
import com.punhanaghayev.bimmerpro.core.protocol.HardwarePermissionHelper
import com.punhanaghayev.bimmerpro.core.update.OtaCheckState
import com.punhanaghayev.bimmerpro.data.ActiveOperationState
import com.punhanaghayev.bimmerpro.data.ConnectionPurpose
import com.punhanaghayev.bimmerpro.ui.BimmerViewModel
import com.punhanaghayev.bimmerpro.ui.UiEvent
import com.punhanaghayev.bimmerpro.ui.components.BmwRoundelLogo
import com.punhanaghayev.bimmerpro.ui.components.MSportAccentStrip
import com.punhanaghayev.bimmerpro.ui.screens.CodingScreen
import com.punhanaghayev.bimmerpro.ui.screens.DeveloperScreen
import com.punhanaghayev.bimmerpro.ui.screens.DiagnosticsScreen
import com.punhanaghayev.bimmerpro.ui.screens.GarageScreen
import com.punhanaghayev.bimmerpro.ui.screens.LiveScreen
import com.punhanaghayev.bimmerpro.ui.screens.ServiceScreen
import com.punhanaghayev.bimmerpro.ui.screens.PacketInspectorScreen
import com.punhanaghayev.bimmerpro.ui.screens.SavedVehiclesScreen
import com.punhanaghayev.bimmerpro.ui.theme.AppThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.BimmerProTheme
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed

enum class BimmerTab(val icon: ImageVector) {
    GARAGE(Icons.Default.DirectionsCar),
    DIAGNOSTICS(Icons.Default.Build),
    CODING(Icons.Default.Memory),
    LIVE(Icons.Default.Speed),
    SERVICE(Icons.Default.Tune),
    DEVELOPER(Icons.Default.Settings);

    fun label(strings: AppStrings): String = when (this) {
        GARAGE -> strings.tabGarage
        DIAGNOSTICS -> strings.tabDiagnostics
        CODING -> strings.tabCoding
        LIVE -> strings.tabLive
        SERVICE -> strings.tabService
        DEVELOPER -> strings.tabDeveloper
    }
}

private enum class OverlayRoute {
    SAVED_VEHICLES,
    PACKET_INSPECTOR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    viewModel: BimmerViewModel = viewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val protocolLogs by viewModel.protocolLogs.collectAsStateWithLifecycle()
    val isLiveRunning by viewModel.isLiveStreamRunning.collectAsStateWithLifecycle()
    val otaState by viewModel.otaState.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val themeStyle by viewModel.themeStyle.collectAsStateWithLifecycle()
    val isLanguageConfirmed by viewModel.isLanguageConfirmed.collectAsStateWithLifecycle()
    val isDisclaimerAccepted by viewModel.isDisclaimerAccepted.collectAsStateWithLifecycle()
    val otaPromptDismissed by viewModel.otaPromptDismissed.collectAsStateWithLifecycle()
    val strings = remember(appLanguage) { AppLocalization.stringsFor(appLanguage) }

    val isConnected = session.vehicleProfile != null && session.connectionState is ConnectionState.Connected
    val visibleTabs = remember(isConnected, session.connectionPurpose) {
        if (!isConnected) {
            listOf(BimmerTab.GARAGE, BimmerTab.DEVELOPER)
        } else if (session.connectionPurpose == ConnectionPurpose.CODING) {
            listOf(BimmerTab.GARAGE, BimmerTab.CODING, BimmerTab.DEVELOPER)
        } else {
            listOf(BimmerTab.GARAGE, BimmerTab.DIAGNOSTICS, BimmerTab.LIVE, BimmerTab.SERVICE, BimmerTab.DEVELOPER)
        }
    }

    var activeTab by remember { mutableStateOf(BimmerTab.GARAGE) }
    var activeOverlay by remember { mutableStateOf<OverlayRoute?>(null) }
    var langMenuExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Keep activeTab valid when visibleTabs changes (e.g. on disconnect or purpose change)
    LaunchedEffect(visibleTabs) {
        if (activeTab !in visibleTabs) {
            activeTab = BimmerTab.GARAGE
        }
    }

    // Automatic silent OTA check on startup once onboarding dialogs are done
    LaunchedEffect(isLanguageConfirmed, isDisclaimerAccepted) {
        if (isLanguageConfirmed && isDisclaimerAccepted) {
            viewModel.checkForOtaUpdates(silent = true)
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (text != null) viewModel.importBackupsJson(text)
        }
    }

    val importTraceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (text != null) viewModel.connectWithTraceReplay(text)
        }
    }

    val btPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.scanHardware()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.Snackbar -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.ShareFile -> {
                    runCatching {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            event.file
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = event.mimeType
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, "BMW Expert PRO: ${event.file.name}")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, event.chooserTitle))
                    }.onFailure { err ->
                        snackbarHostState.showSnackbar("Saved: ${event.file.name} (${err.message ?: ""})")
                    }
                }
                is UiEvent.InstallApk -> {
                    val apkUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        event.apkFile
                    )
                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(apkUri, "application/vnd.android.package-archive")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    runCatching {
                        context.startActivity(installIntent)
                    }.onFailure {
                        snackbarHostState.showSnackbar(event.apkFile.name)
                    }
                }
            }
        }
    }

    BimmerProTheme(themeStyle = themeStyle) {
        val theme = LocalThemeStyle.current
        CompositionLocalProvider(LocalAppStrings provides strings) {
            // 1. First-launch Language Picker (auto-detected, EN default)
            if (!isLanguageConfirmed) {
                var draftLang by remember { mutableStateOf(appLanguage) }
                val draftStrings = remember(draftLang) { AppLocalization.stringsFor(draftLang) }
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(draftStrings.welcomeLangTitle, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(draftStrings.welcomeLangSub, fontSize = 12.sp)
                            AppLanguage.entries.forEach { lang ->
                                val selected = lang == draftLang
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { draftLang = lang },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) theme.primary.copy(alpha = 0.22f) else theme.card
                                    ),
                                    border = BorderStroke(1.dp, if (selected) theme.primaryBright else theme.outline),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${lang.flagCode} • ${lang.nativeName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        if (selected) Text("✓", color = theme.primaryBright, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { viewModel.confirmInitialLanguage(draftLang) }) {
                            Text(draftStrings.continueBtn)
                        }
                    }
                )
            } else if (!isDisclaimerAccepted) {
                // 2. Legal & Vehicle Safety Disclaimer
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(strings.disclaimerTitle, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(strings.disclaimerBullet1, fontSize = 12.sp)
                            Text(strings.disclaimerBullet2, fontSize = 12.sp)
                            Text(strings.disclaimerBullet3, fontSize = 12.sp)
                        }
                    },
                    confirmButton = {
                        Button(onClick = viewModel::acceptDisclaimer) {
                            Text(strings.disclaimerAcceptBtn)
                        }
                    }
                )
            } else if (otaState is OtaCheckState.UpdateAvailable && !otaPromptDismissed) {
                // 3. Startup Automatic Update Prompt
                val latest = (otaState as OtaCheckState.UpdateAvailable).latest
                AlertDialog(
                    onDismissRequest = viewModel::dismissOtaPrompt,
                    title = { Text("${strings.updateAvailableTitle} (${latest.tagName})", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(latest.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(latest.changelog.take(280), fontSize = 12.sp)
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.dismissOtaPrompt()
                            viewModel.downloadAndInstallOta(context, latest)
                        }) {
                            Text(strings.updateNowBtn)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::dismissOtaPrompt) {
                            Text(strings.updateLaterBtn)
                        }
                    }
                )
            }

            val activeOp = session.activeOperation
            if (activeOp != null && activeOp.inProgress) {
                OperationProgressDialog(
                    operation = activeOp,
                    strings = strings,
                    theme = theme,
                    onDismiss = viewModel::dismissActiveOperation
                )
            }

            Scaffold(
                containerColor = theme.bg,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    Column {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = theme.surface,
                                titleContentColor = theme.textPrimary
                            ),
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BmwRoundelLogo(size = 28.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text("BMW Expert PRO", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                        val sub = session.vehicleProfile?.let {
                                            "${it.chassis.code} • ${it.vin.takeLast(7)}"
                                        } ?: activeTab.label(strings)
                                        Text(sub, color = theme.primaryBright, fontSize = 11.sp)
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        activeOverlay = if (activeOverlay == OverlayRoute.SAVED_VEHICLES) null else OverlayRoute.SAVED_VEHICLES
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = strings.savedVehiclesTitle,
                                        tint = if (activeOverlay == OverlayRoute.SAVED_VEHICLES) theme.accentRed else theme.primaryBright
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        activeOverlay = if (activeOverlay == OverlayRoute.PACKET_INSPECTOR) null else OverlayRoute.PACKET_INSPECTOR
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Code,
                                        contentDescription = strings.packetInspectorTitle,
                                        tint = if (activeOverlay == OverlayRoute.PACKET_INSPECTOR) theme.accentRed else theme.primaryBright
                                    )
                                }
                                Box {
                                    IconButton(onClick = { langMenuExpanded = true }) {
                                        Icon(Icons.Default.Language, contentDescription = strings.languageTitle, tint = theme.primaryBright)
                                    }
                                    DropdownMenu(
                                        expanded = langMenuExpanded,
                                        onDismissRequest = { langMenuExpanded = false }
                                    ) {
                                        AppLanguage.entries.forEach { lang ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = "${lang.flagCode} • ${lang.nativeName}",
                                                        fontWeight = if (lang == appLanguage) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (lang == appLanguage) theme.primaryBright else theme.textPrimary
                                                    )
                                                },
                                                onClick = {
                                                    viewModel.setAppLanguage(lang)
                                                    langMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                        MSportAccentStrip()
                    }
                },
                bottomBar = {
                    NavigationBar(containerColor = theme.surface) {
                        visibleTabs.forEach { tab ->
                            val tabLabel = tab.label(strings)
                            NavigationBarItem(
                                selected = activeOverlay == null && activeTab == tab,
                                onClick = {
                                    activeOverlay = null
                                    activeTab = tab
                                },
                                icon = { Icon(tab.icon, contentDescription = tabLabel) },
                                label = { Text(tabLabel, fontSize = 10.sp, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = theme.primaryBright,
                                    selectedTextColor = theme.primaryBright,
                                    indicatorColor = theme.card,
                                    unselectedIconColor = theme.textSecondary,
                                    unselectedTextColor = theme.textSecondary
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (activeOverlay) {
                        OverlayRoute.SAVED_VEHICLES -> SavedVehiclesScreen(
                            savedVehicles = session.savedVehicles,
                            onConnectSaved = { adapter ->
                                viewModel.connect(adapter, "", null, session.connectionPurpose)
                            },
                            onRemoveSavedVehicle = viewModel::removeSavedVehicle,
                            onBack = { activeOverlay = null }
                        )

                        OverlayRoute.PACKET_INSPECTOR -> PacketInspectorScreen(
                            protocolLogs = protocolLogs,
                            onExportTrace = { viewModel.exportSessionTrace(context) },
                            onImportAndReplayTrace = { importTraceLauncher.launch(arrayOf("text/*", "*/*")) },
                            onClearLogs = viewModel::clearProtocolLogs,
                            onBack = { activeOverlay = null }
                        )

                        null -> when (activeTab) {
                            BimmerTab.GARAGE -> GarageScreen(
                                session = session,
                                onConnect = { adapter, host, port, purpose ->
                                    if (adapter == AdapterType.USB_KDCAN) {
                                        HardwarePermissionHelper.requestUsbKdcanPermissionIfNeeded(context)
                                    }
                                    viewModel.connect(adapter, host, port, purpose)
                                },
                                onDisconnect = viewModel::disconnect,
                                onAdjustSimVoltage = viewModel::adjustSimulatedVoltage,
                                onScanHardware = {
                                    val missingBt = HardwarePermissionHelper.requiredBluetoothPermissions()
                                    if (missingBt.isNotEmpty() && !HardwarePermissionHelper.hasAllBluetoothPermissions(context)) {
                                        btPermissionLauncher.launch(missingBt)
                                    } else {
                                        HardwarePermissionHelper.requestUsbKdcanPermissionIfNeeded(context)
                                        viewModel.scanHardware()
                                    }
                                },
                                onSelectBleDevice = viewModel::selectBleDevice,
                                onNavigateToTab = { tabIndex ->
                                    activeTab = when (tabIndex) {
                                        1 -> BimmerTab.DIAGNOSTICS
                                        2 -> BimmerTab.CODING
                                        3 -> BimmerTab.LIVE
                                        4 -> BimmerTab.SERVICE
                                        5 -> BimmerTab.DEVELOPER
                                        else -> BimmerTab.GARAGE
                                    }
                                },
                                onRunAdapterHealthCheck = viewModel::runAdapterHealthCheck
                            )

                            BimmerTab.DIAGNOSTICS -> DiagnosticsScreen(
                                session = session,
                                onRunScan = viewModel::runFullScan,
                                onClearAllDtcs = viewModel::clearAllDtcs,
                                onClearEcuDtcs = viewModel::clearEcuDtcs,
                                onSendEcuReset = viewModel::sendEcuReset,
                                onFetchFreezeFrame = viewModel::fetchDtcFreezeFrame,
                                onExportPdf = { viewModel.generateInspectionPdf(context) },
                                onGoToGarage = { activeTab = BimmerTab.GARAGE }
                            )

                            BimmerTab.CODING -> CodingScreen(
                                session = session,
                                onApplyPreset = viewModel::applyPresetCoding,
                                onApplyCafd = viewModel::applyCafdParameter,
                                onApplyBatchCoding = viewModel::applyBatchCoding,
                                onRestoreBackup = viewModel::restoreBackup,
                                onCreateFullNcdSnapshot = viewModel::createFullNcdSnapshot,
                                onExportBackupsJson = { viewModel.exportBackupsJson(context) },
                                onImportBackupsJson = { importJsonLauncher.launch(arrayOf("application/json", "*/*")) },
                                onAddVoOption = viewModel::addVehicleOrderOption,
                                onPreviewVoCoding = viewModel::previewVoCodingPlan,
                                onDismissVoPlan = viewModel::dismissVoCodingPlan,
                                onApplyVoPlan = viewModel::applyVoCodingPlan,
                                onReadFsc = viewModel::readFscCertificates,
                                onGoToGarage = { activeTab = BimmerTab.GARAGE }
                            )

                            BimmerTab.LIVE -> LiveScreen(
                                session = session,
                                isLiveStreamRunning = isLiveRunning,
                                availableLiveChannels = viewModel.availableLiveChannels(),
                                onToggleLiveStream = viewModel::toggleLiveTelemetry,
                                onTogglePerformanceRun = viewModel::togglePerformanceRun,
                                onExportCsv = { viewModel.exportTelemetryCsv(context) },
                                onSetSelectedPids = viewModel::setSelectedLivePids,
                                onSetAndroidAutoPids = viewModel::setAndroidAutoPids,
                                onMoveLiveChannel = viewModel::moveLiveChannel,
                                onSelectDragMode = viewModel::setDragMode,
                                onRunVirtualDynoPull = viewModel::runVirtualDynoPull,
                                onGoToGarage = { activeTab = BimmerTab.GARAGE }
                            )

                            BimmerTab.SERVICE -> ServiceScreen(
                                session = session,
                                onExecuteRoutine = viewModel::executeServiceRoutine,
                                onRunActuatorTest = viewModel::runActuatorTest,
                                onResetShortCircuitChannel = viewModel::resetShortCircuitChannel,
                                onSetExhaustFlapMode = viewModel::setExhaustFlapMode,
                                onSetAsdState = viewModel::setAsdState,
                                onSetEpbServiceMode = viewModel::setEpbServiceMode,
                                onWriteServiceHistory = viewModel::writeServiceHistory,
                                onWriteInjectorCode = viewModel::writeInjectorCode,
                                onResetZfAdaptations = viewModel::resetZfAdaptations,
                                onSendEcuReset = viewModel::sendEcuReset,
                                onGoToGarage = { activeTab = BimmerTab.GARAGE }
                            )

                            BimmerTab.DEVELOPER -> DeveloperScreen(
                                otaState = otaState,
                                dataPackStatus = session.dataPackStatus,
                                currentLanguage = appLanguage,
                                currentTheme = themeStyle,
                                isPhysicalSafeMode = session.isPhysicalSafeMode,
                                isPhysicalWriteUnlocked = session.isPhysicalWriteUnlocked,
                                dtcDictionaryCount = session.dtcDictionaryCount,
                                onSelectLanguage = viewModel::setAppLanguage,
                                onSelectTheme = viewModel::setThemeStyle,
                                onCheckOta = { viewModel.checkForOtaUpdates(silent = false) },
                                onDownloadOta = { release -> viewModel.downloadAndInstallOta(context, release) },
                                onTogglePhysicalWriteUnlock = viewModel::setPhysicalWriteUnlocked,
                                onDownloadDataPack = viewModel::downloadDataPack,
                                onExportSupportBundle = { viewModel.exportSupportBundle(context) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OperationProgressDialog(
    operation: ActiveOperationState,
    strings: AppStrings,
    theme: AppThemeStyle,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (operation.isCompleted) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = operation.isCompleted,
            dismissOnClickOutside = operation.isCompleted
        ),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = com.punhanaghayev.bimmerpro.core.i18n.GlobalTextLocalizer.localize(
                        operation.title.ifBlank { strings.operationProgressTitle },
                        strings.language
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = theme.textPrimary
                )
                MSportAccentStrip()
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!operation.isCompleted) {
                    CircularProgressIndicator(
                        progress = { operation.progress.coerceIn(0f, 1f) },
                        modifier = Modifier.size(54.dp),
                        color = theme.primaryBright,
                        strokeWidth = 4.dp,
                        trackColor = theme.outline.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "${(operation.progress.coerceIn(0f, 1f) * 100).toInt()}%",
                        color = theme.primaryBright,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                } else {
                    Icon(
                        imageVector = if (operation.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (operation.isSuccess) StatusGreen else StatusRed,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = com.punhanaghayev.bimmerpro.core.i18n.GlobalTextLocalizer.localize(operation.currentStep, strings.language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (operation.isCompleted && !operation.isSuccess) StatusRed else theme.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    if (operation.details.isNotBlank()) {
                        Text(
                            text = com.punhanaghayev.bimmerpro.core.i18n.GlobalTextLocalizer.localize(operation.details, strings.language),
                            fontSize = 12.sp,
                            color = theme.textSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                    if (operation.errorMessage != null) {
                        Text(
                            text = com.punhanaghayev.bimmerpro.core.i18n.GlobalTextLocalizer.localize(operation.errorMessage, strings.language),
                            fontSize = 11.sp,
                            color = StatusRed,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (operation.isCompleted) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (operation.isSuccess) theme.primary else StatusRed
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(strings.operationDoneBtn)
                }
            }
        }
    )
}
