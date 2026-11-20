package com.punhanaghayev.bimmerpro

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage
import com.punhanaghayev.bimmerpro.core.i18n.AppLocalization
import com.punhanaghayev.bimmerpro.core.i18n.AppStrings
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import com.punhanaghayev.bimmerpro.core.protocol.HardwarePermissionHelper
import com.punhanaghayev.bimmerpro.ui.BimmerViewModel
import com.punhanaghayev.bimmerpro.ui.UiEvent
import com.punhanaghayev.bimmerpro.ui.components.BmwRoundelLogo
import com.punhanaghayev.bimmerpro.ui.components.MSportAccentStrip
import com.punhanaghayev.bimmerpro.ui.screens.CodingScreen
import com.punhanaghayev.bimmerpro.ui.screens.DeveloperScreen
import com.punhanaghayev.bimmerpro.ui.screens.DiagnosticsScreen
import com.punhanaghayev.bimmerpro.ui.screens.GarageScreen
import com.punhanaghayev.bimmerpro.ui.screens.LiveServiceScreen
import com.punhanaghayev.bimmerpro.ui.theme.BimmerBg
import com.punhanaghayev.bimmerpro.ui.theme.BimmerCard
import com.punhanaghayev.bimmerpro.ui.theme.BimmerSurface
import com.punhanaghayev.bimmerpro.ui.theme.BimmerProTheme
import com.punhanaghayev.bimmerpro.ui.theme.BmwBlueBright
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.TextPrimary
import com.punhanaghayev.bimmerpro.ui.theme.TextSecondary

private enum class BimmerTab(val icon: ImageVector) {
    GARAGE(Icons.Default.DirectionsCar),
    DIAGNOSTICS(Icons.Default.Memory),
    CODING(Icons.Default.Build),
    LIVE_SERVICE(Icons.Default.Speed),
    DEVELOPER(Icons.Default.Code);

    fun label(strings: AppStrings): String = when (this) {
        GARAGE -> strings.tabGarage
        DIAGNOSTICS -> strings.tabDiagnostics
        CODING -> strings.tabCoding
        LIVE_SERVICE -> strings.tabLiveService
        DEVELOPER -> strings.tabDeveloper
    }
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
    val isDisclaimerAccepted by viewModel.isDisclaimerAccepted.collectAsStateWithLifecycle()
    val strings = remember(appLanguage) { AppLocalization.stringsFor(appLanguage) }

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var langMenuExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (text != null) viewModel.importBackupsJson(text)
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
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        event.file
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = event.mimeType
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "BimmerPro Çıktısı: ${event.file.name}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    runCatching {
                        context.startActivity(Intent.createChooser(shareIntent, event.chooserTitle))
                    }.onFailure {
                        snackbarHostState.showSnackbar("Dosya kaydedildi: ${event.file.name}")
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
                        snackbarHostState.showSnackbar("APK indirildi: ${event.apkFile.name}")
                    }
                }
            }
        }
    }

    BimmerProTheme(themeStyle = themeStyle) {
        val theme = LocalThemeStyle.current
        CompositionLocalProvider(LocalAppStrings provides strings) {
            if (!isDisclaimerAccepted) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Yasal Uyarı & Araç Güvenlik Sözleşmesi", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "• Fiziksel araç bağlantılarında uygulama varsayılan olarak Salt-Okunur Güvenli Modda (Read-Only Safe Mode) açılır.",
                                fontSize = 12.sp
                            )
                            Text(
                                text = "• Kilometre sıfırlama/değiştirme (odometer rollback) ve ECU chiptuning/flash işlemleri güvenlik ve yasal nedenlerle kapsam dışıdır.",
                                fontSize = 12.sp
                            )
                            Text(
                                text = "• Yazma kilidi açıldığında oluşabilecek ECU/kodlama riskleri tamamen kullanıcının sorumluluğundadır. Kodlama öncesi akünün >= 12.2V olduğundan emin olun.",
                                fontSize = 12.sp
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = viewModel::acceptDisclaimer) {
                            Text("Okudum, Anladım ve Kabul Ediyorum")
                        }
                    }
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
                                        } ?: BimmerTab.entries[selectedTabIndex].label(strings)
                                        Text(sub, color = theme.primaryBright, fontSize = 11.sp)
                                    }
                                }
                            },
                            actions = {
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
                        BimmerTab.entries.forEachIndexed { index, tab ->
                            val tabLabel = tab.label(strings)
                            NavigationBarItem(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
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
                    when (BimmerTab.entries[selectedTabIndex]) {
                        BimmerTab.GARAGE -> GarageScreen(
                            session = session,
                            onConnect = { adapter, host, port ->
                                if (adapter == AdapterType.USB_KDCAN) {
                                    HardwarePermissionHelper.requestUsbKdcanPermissionIfNeeded(context)
                                }
                                viewModel.connect(adapter, host, port)
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
                            onRemoveSavedVehicle = viewModel::removeSavedVehicle,
                            onNavigateToTab = { selectedTabIndex = it }
                        )

                        BimmerTab.DIAGNOSTICS -> DiagnosticsScreen(
                            session = session,
                            onRunScan = viewModel::runFullScan,
                            onClearAllDtcs = viewModel::clearAllDtcs,
                            onClearEcuDtcs = viewModel::clearEcuDtcs,
                            onSendEcuReset = viewModel::sendEcuReset,
                            onExportPdf = { viewModel.generateInspectionPdf(context) },
                            onGoToGarage = { selectedTabIndex = 0 }
                        )

                        BimmerTab.CODING -> CodingScreen(
                            session = session,
                            onApplyPreset = viewModel::applyPresetCoding,
                            onApplyCafd = viewModel::applyCafdParameter,
                            onRestoreBackup = viewModel::restoreBackup,
                            onCreateFullNcdSnapshot = viewModel::createFullNcdSnapshot,
                            onExportBackupsJson = { viewModel.exportBackupsJson(context) },
                            onImportBackupsJson = { importJsonLauncher.launch(arrayOf("application/json", "*/*")) },
                            onAddVoOption = viewModel::addVehicleOrderOption,
                            onGoToGarage = { selectedTabIndex = 0 }
                        )

                        BimmerTab.LIVE_SERVICE -> LiveServiceScreen(
                            session = session,
                            isLiveStreamRunning = isLiveRunning,
                            onToggleLiveStream = viewModel::toggleLiveTelemetry,
                            onTogglePerformanceRun = viewModel::togglePerformanceRun,
                            onExportCsv = { viewModel.exportTelemetryCsv(context) },
                            onExecuteRoutine = viewModel::executeServiceRoutine,
                            onRunActuatorTest = viewModel::runActuatorTest,
                            onWriteServiceHistory = viewModel::writeServiceHistory,
                            onWriteInjectorCode = viewModel::writeInjectorCode,
                            onResetZfAdaptations = viewModel::resetZfAdaptations,
                            onSendEcuReset = viewModel::sendEcuReset,
                            onGoToGarage = { selectedTabIndex = 0 }
                        )

                        BimmerTab.DEVELOPER -> DeveloperScreen(
                            protocolLogs = protocolLogs,
                            otaState = otaState,
                            currentLanguage = appLanguage,
                            currentTheme = themeStyle,
                            isPhysicalSafeMode = session.isPhysicalSafeMode,
                            isPhysicalWriteUnlocked = session.isPhysicalWriteUnlocked,
                            dtcDictionaryCount = session.dtcDictionaryCount,
                            onSelectLanguage = viewModel::setAppLanguage,
                            onSelectTheme = viewModel::setThemeStyle,
                            onCheckOta = viewModel::checkForOtaUpdates,
                            onDownloadOta = { release -> viewModel.downloadAndInstallOta(context, release) },
                            onTogglePhysicalWriteUnlock = viewModel::setPhysicalWriteUnlocked,
                            onDownloadDataPack = viewModel::downloadDataPack,
                            onExportTrace = { viewModel.exportSessionTrace(context) },
                            onClearLogs = viewModel::clearProtocolLogs
                        )
                    }
                }
            }
        }
    }
}
