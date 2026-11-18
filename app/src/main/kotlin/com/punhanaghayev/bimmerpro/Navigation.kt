package com.punhanaghayev.bimmerpro

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.punhanaghayev.bimmerpro.ui.theme.BmwBlueBright
import com.punhanaghayev.bimmerpro.ui.theme.TextPrimary
import com.punhanaghayev.bimmerpro.ui.theme.TextSecondary

private enum class BimmerTab(val label: String, val icon: ImageVector) {
    GARAGE("Garaj", Icons.Default.DirectionsCar),
    DIAGNOSTICS("Diagnostik", Icons.Default.Memory),
    CODING("Kodlama", Icons.Default.Build),
    LIVE_SERVICE("Canlı/Servis", Icons.Default.Speed),
    DEVELOPER("Developer", Icons.Default.Code)
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

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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

    Scaffold(
        containerColor = BimmerBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BimmerSurface,
                        titleContentColor = TextPrimary
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BmwRoundelLogo(size = 28.dp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("BimmerPro", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                val sub = session.vehicleProfile?.let {
                                    "${it.chassis.code} • ${it.vin.takeLast(7)}"
                                } ?: BimmerTab.entries[selectedTabIndex].label
                                Text(sub, color = BmwBlueBright, fontSize = 11.sp)
                            }
                        }
                    }
                )
                MSportAccentStrip()
            }
        },
        bottomBar = {
            NavigationBar(containerColor = BimmerSurface) {
                BimmerTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BmwBlueBright,
                            selectedTextColor = BmwBlueBright,
                            indicatorColor = BimmerCard,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
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
                    onConnect = { adapter, ip ->
                        if (adapter == AdapterType.USB_KDCAN) {
                            HardwarePermissionHelper.requestUsbKdcanPermissionIfNeeded(context)
                        }
                        viewModel.connect(adapter, ip)
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
                    onNavigateToTab = { selectedTabIndex = it }
                )

                BimmerTab.DIAGNOSTICS -> DiagnosticsScreen(
                    session = session,
                    onRunScan = viewModel::runFullScan,
                    onClearAllDtcs = viewModel::clearAllDtcs,
                    onExportPdf = { viewModel.generateInspectionPdf(context) },
                    onGoToGarage = { selectedTabIndex = 0 }
                )

                BimmerTab.CODING -> CodingScreen(
                    session = session,
                    onApplyPreset = viewModel::applyPresetCoding,
                    onApplyCafd = viewModel::applyCafdParameter,
                    onRestoreBackup = viewModel::restoreBackup,
                    onAddVoOption = viewModel::addVehicleOrderOption,
                    onGoToGarage = { selectedTabIndex = 0 }
                )

                BimmerTab.LIVE_SERVICE -> LiveServiceScreen(
                    session = session,
                    isLiveStreamRunning = isLiveRunning,
                    onToggleLiveStream = viewModel::toggleLiveTelemetry,
                    onExportCsv = { viewModel.exportTelemetryCsv(context) },
                    onExecuteRoutine = viewModel::executeServiceRoutine,
                    onWriteServiceHistory = viewModel::writeServiceHistory,
                    onGoToGarage = { selectedTabIndex = 0 }
                )

                BimmerTab.DEVELOPER -> DeveloperScreen(
                    protocolLogs = protocolLogs,
                    otaState = otaState,
                    onCheckOta = viewModel::checkForOtaUpdates,
                    onDownloadOta = { release -> viewModel.downloadAndInstallOta(context, release) },
                    onClearLogs = viewModel::clearProtocolLogs
                )
            }
        }
    }
}
