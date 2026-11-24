package com.punhanaghayev.bimmerpro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import com.punhanaghayev.bimmerpro.core.protocol.ConnectionState
import com.punhanaghayev.bimmerpro.core.protocol.TcpElmStnTransportAdapter
import com.punhanaghayev.bimmerpro.core.protocol.TcpEnetTransportAdapter
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import com.punhanaghayev.bimmerpro.data.ConnectionPurpose
import com.punhanaghayev.bimmerpro.ui.components.BatterySafetyPill
import com.punhanaghayev.bimmerpro.ui.components.BmwRoundelLogo
import com.punhanaghayev.bimmerpro.ui.components.MSportAccentStrip
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusAmber
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(
    session: BimmerSessionState,
    onConnect: (AdapterType, String, Int?, ConnectionPurpose) -> Unit,
    onDisconnect: () -> Unit,
    onAdjustSimVoltage: (Double) -> Unit,
    onScanHardware: () -> Unit,
    onSelectBleDevice: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onRunAdapterHealthCheck: () -> Unit = {}
) {
    val physicalAdapters = remember { AdapterType.entries.filter { !it.isSimulator } }
    val simulatorAdapters = remember { AdapterType.entries.filter { it.isSimulator } }

    var isSimulatorMode by remember(session.selectedAdapter) {
        mutableStateOf(
            if (session.connectionState is ConnectionState.Connected) session.selectedAdapter.isSimulator else false
        )
    }
    var selectedPurpose by remember(session.connectionPurpose) { mutableStateOf(session.connectionPurpose) }
    var selectedPhysical by remember { mutableStateOf(AdapterType.VLINKER_BM_WIFI) }
    var selectedSimulator by remember { mutableStateOf(AdapterType.SIMULATOR_F10) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var vlinkerHost by remember { mutableStateOf(TcpElmStnTransportAdapter.DEFAULT_VLINKER_HOST) }
    var vlinkerPort by remember { mutableStateOf(TcpElmStnTransportAdapter.DEFAULT_VLINKER_PORT.toString()) }
    var enetHost by remember { mutableStateOf(TcpEnetTransportAdapter.DEFAULT_ZGW_HOST) }

    val strings = LocalAppStrings.current
    val theme = LocalThemeStyle.current

    val activeChoices = if (isSimulatorMode) simulatorAdapters else physicalAdapters
    val activeAdapter = if (isSimulatorMode) selectedSimulator else selectedPhysical

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GarageHeroHeader(connectionState = session.connectionState)
        }

        // BimmerCode-style Connect vs Simulator Primary Mode Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModeSelectorCard(
                    title = strings.connectModeLabel,
                    subtitle = "vLinker / ENET / BLE",
                    icon = Icons.Default.Wifi,
                    selected = !isSimulatorMode,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        isSimulatorMode = false
                        dropdownExpanded = false
                    }
                )
                ModeSelectorCard(
                    title = strings.simulatorModeLabel,
                    subtitle = "F10 / F30 / G30 / E90",
                    icon = Icons.Default.DirectionsCar,
                    selected = isSimulatorMode,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        isSimulatorMode = true
                        dropdownExpanded = false
                    }
                )
            }
        }

        if (session.vehicleProfile != null && session.connectionState is ConnectionState.Connected) {
            val profile = session.vehicleProfile
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, theme.primary.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = profile.modelDisplayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )

                        Text(
                            text = "VIN: ${profile.vin}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = theme.primaryBright,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )

                        BatterySafetyPill(voltage = profile.batteryVoltage)

                        // Vehicle Technical Information & I-Level (Expandable Show/Hide)
                        var showVehicleInfo by remember { mutableStateOf(false) }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.cardElevated)
                                .clickable { showVehicleInfo = !showVehicleInfo }
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = theme.primaryBright,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = strings.vehicleTechnicalDetailsTitle,
                                        color = theme.primaryBright,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (showVehicleInfo) strings.hideBtn else strings.showBtn,
                                    color = theme.textSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            if (showVehicleInfo) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val techRows = listOf(
                                        strings.chassisLabel to "${profile.chassis.code} (${profile.chassis.family.label})",
                                        strings.engineLabel to profile.engineCode,
                                        strings.currentILevelLabel to profile.iLevel,
                                        strings.factoryILevelLabel to profile.factoryILevel,
                                        strings.mileageLabel to "%,d km".format(profile.odometerKm),
                                        strings.typeProdLabel to "${profile.typeCode} • ${profile.productionDate}",
                                        strings.paintLabel to profile.paintCode,
                                        strings.upholsteryLabel to profile.upholsteryCode
                                    )
                                    techRows.forEach { (label, value) ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(theme.card)
                                                .padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                color = theme.textSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = value,
                                                color = theme.textPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // BimmerCode-style Vehicle Equipment (FA/VO) Overview
                        var showEquipment by remember { mutableStateOf(false) }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.cardElevated)
                                .clickable { showEquipment = !showEquipment }
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.vehicleEquipmentLabel.format(profile.faOptions.size),
                                    color = theme.primaryBright,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (showEquipment) strings.hideBtn else strings.showBtn,
                                    color = theme.textSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            if (showEquipment) {
                                profile.faOptions.forEach { opt ->
                                    Text(
                                        text = "• ${opt.code} — ${com.punhanaghayev.bimmerpro.core.i18n.GlobalTextLocalizer.localize(opt.description, strings.language)}",
                                        color = theme.textPrimary,
                                        fontSize = 11.sp
                                    )
                                }
                                if (session.connectionPurpose == ConnectionPurpose.CODING) {
                                    Spacer(Modifier.height(4.dp))
                                    com.punhanaghayev.bimmerpro.ui.components.BmwOutlinedButton(
                                        text = strings.codeEquipmentBtn,
                                        icon = Icons.Default.Memory,
                                        onClick = { onNavigateToTab(2) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        profile.chassis.secureCodingNotice?.let { notice ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(StatusAmber.copy(alpha = 0.14f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = com.punhanaghayev.bimmerpro.core.i18n.GlobalTextLocalizer.localize(notice, strings.language),
                                    color = StatusAmber,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (session.selectedAdapter.isSimulator) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.cardElevated)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = strings.simVoltageTestLabel.format(profile.batteryVoltage),
                                    color = theme.textSecondary,
                                    fontSize = 11.sp
                                )
                                Slider(
                                    value = profile.batteryVoltage.toFloat(),
                                    onValueChange = { onAdjustSimVoltage(((it * 10).toInt()) / 10.0) },
                                    valueRange = 11.4f..14.4f
                                )
                            }
                        }

                        if (session.connectionPurpose == ConnectionPurpose.CODING) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickActionTile(
                                    icon = Icons.Default.Memory,
                                    title = strings.quickCoding,
                                    badge = null,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onNavigateToTab(2) }
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onDisconnect,
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                            border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.disconnectBtn, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }

        // Adapter or Simulator Selection Hub (hidden while connected; reappears on Disconnect)
        if (session.connectionState !is ConnectionState.Connected) {
            item {
                Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isSimulatorMode) strings.simulatorModeLabel else strings.connectionHubTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = theme.textPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = strings.connectionPurposeTitle,
                        color = theme.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModeSelectorCard(
                            title = strings.purposeDiagnosticsLabel,
                            subtitle = null,
                            icon = Icons.Default.Build,
                            selected = selectedPurpose == ConnectionPurpose.DIAGNOSTICS,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedPurpose = ConnectionPurpose.DIAGNOSTICS }
                        )
                        ModeSelectorCard(
                            title = strings.purposeCodingLabel,
                            subtitle = null,
                            icon = Icons.Default.Memory,
                            selected = selectedPurpose == ConnectionPurpose.CODING,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedPurpose = ConnectionPurpose.CODING }
                        )
                    }

                    com.punhanaghayev.bimmerpro.ui.components.BmwCompactDropdown(
                        label = strings.adapterDropdownLabel,
                        selectedLabel = strings.adapterTitle(activeAdapter),
                        supportingText = strings.adapterDesc(activeAdapter),
                        leadingIcon = when {
                            activeAdapter == AdapterType.VLINKER_BM_WIFI -> Icons.Default.Wifi
                            activeAdapter.isSimulator -> Icons.Default.DirectionsCar
                            else -> Icons.Default.Cable
                        },
                        items = activeChoices,
                        itemLabel = { strings.adapterTitle(it) },
                        onSelect = { adapter ->
                            if (isSimulatorMode) selectedSimulator = adapter else selectedPhysical = adapter
                        }
                    )

                    if (!isSimulatorMode) {
                        when (activeAdapter.transportKind) {
                            com.punhanaghayev.bimmerpro.core.protocol.AdapterTransportKind.WIFI_ELM_STN -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    com.punhanaghayev.bimmerpro.ui.components.BmwCompactTextField(
                                        value = vlinkerHost,
                                        onValueChange = { vlinkerHost = it },
                                        label = "IP",
                                        showClearButton = false,
                                        modifier = Modifier.weight(0.65f)
                                    )
                                    com.punhanaghayev.bimmerpro.ui.components.BmwCompactTextField(
                                        value = vlinkerPort,
                                        onValueChange = { vlinkerPort = it.filter { c -> c.isDigit() } },
                                        label = "Port",
                                        showClearButton = false,
                                        modifier = Modifier.weight(0.35f)
                                    )
                                }
                            }

                            com.punhanaghayev.bimmerpro.core.protocol.AdapterTransportKind.ENET_TCP -> {
                                com.punhanaghayev.bimmerpro.ui.components.BmwCompactTextField(
                                    value = enetHost,
                                    onValueChange = { enetHost = it },
                                    label = "Gateway IP (169.254.0.1)",
                                    showClearButton = false,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            else -> Unit
                        }

                        if (session.discoveredGateways.isNotEmpty() || session.discoveredBleDevices.isNotEmpty() || session.discoveredUsbDevices.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.cardElevated)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(strings.discoveredDevicesTitle, color = theme.primaryBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                session.discoveredGateways.forEach { gw ->
                                    Text(
                                        text = "• ENET Gateway: ${gw.ipAddress} ${gw.vin?.let { "($it)" } ?: ""}",
                                        color = theme.textPrimary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.clickable {
                                            enetHost = gw.ipAddress
                                            selectedPhysical = AdapterType.ENET_WIFI
                                        }
                                    )
                                }
                                session.discoveredBleDevices.forEach { ble ->
                                    val active = session.selectedBleAddress == ble.address
                                    Text(
                                        text = "• BLE: ${ble.name} (${ble.address}) ${if (active) "[OK]" else ""}",
                                        color = if (active) StatusGreen else theme.textPrimary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.clickable {
                                            onSelectBleDevice(ble.address)
                                            selectedPhysical = AdapterType.BLE_OBD
                                        }
                                    )
                                }
                                session.discoveredUsbDevices.forEach { usb ->
                                    Text(
                                        text = "• USB: ${usb.productName} (VID:0x%04X)".format(usb.vendorId),
                                        color = theme.textPrimary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.clickable { selectedPhysical = AdapterType.USB_KDCAN }
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onScanHardware,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, theme.primaryBright.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = strings.scanHardwareBtn,
                                fontSize = 13.sp,
                                color = theme.primaryBright,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    if (session.connectionState is ConnectionState.Error) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StatusRed.copy(alpha = 0.14f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusRed)
                            Spacer(Modifier.width(8.dp))
                            Text(session.connectionState.message, color = StatusRed, fontSize = 12.sp)
                        }
                    }

                    val isConnecting = session.connectionState is ConnectionState.Connecting
                    Button(
                        onClick = {
                            when (activeAdapter.transportKind) {
                                com.punhanaghayev.bimmerpro.core.protocol.AdapterTransportKind.WIFI_ELM_STN -> onConnect(
                                    activeAdapter,
                                    vlinkerHost.trim(),
                                    vlinkerPort.toIntOrNull() ?: TcpElmStnTransportAdapter.DEFAULT_VLINKER_PORT,
                                    selectedPurpose
                                )
                                com.punhanaghayev.bimmerpro.core.protocol.AdapterTransportKind.ENET_TCP -> onConnect(
                                    activeAdapter,
                                    enetHost.trim(),
                                    TcpEnetTransportAdapter.DEFAULT_HSFZ_PORT,
                                    selectedPurpose
                                )
                                else -> onConnect(activeAdapter, "", null, selectedPurpose)
                            }
                        },
                        enabled = !isConnecting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(strings.connectingText, maxLines = 1, softWrap = false)
                        } else {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isSimulatorMode) strings.startSimulatorBtn else strings.connectVehicleBtn,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
            }
        }

        // Adapter Health & Latency Diagnostics Card (Sprint A)
        if (session.connectionState is ConnectionState.Connected) {
            item {
                val report = session.adapterHealthReport
                val healthTitle = when (strings.language) {
                    com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Adapter Health & Latency Test"
                    com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Adapter Sağlamlığı və Gecikmə Testi"
                    com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Диагностика адаптера и задержка"
                    com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> "Adaptör Sağlık & Gecikme Testi"
                }
                val runTestBtn = when (strings.language) {
                    com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Run 5x Ping & Latency Test"
                    com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "5x Ping və Gecikmə Testini Başlat"
                    com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Запустить 5x Ping тест"
                    com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> "5x Ping & Gecikme Testi Başlat"
                }
                val gradeColor = when (report?.grade) {
                    com.punhanaghayev.bimmerpro.core.protocol.AdapterReadinessGrade.EXCELLENT -> StatusGreen
                    com.punhanaghayev.bimmerpro.core.protocol.AdapterReadinessGrade.GOOD -> theme.primaryBright
                    com.punhanaghayev.bimmerpro.core.protocol.AdapterReadinessGrade.UNSTABLE -> StatusAmber
                    com.punhanaghayev.bimmerpro.core.protocol.AdapterReadinessGrade.CRITICAL -> StatusRed
                    null -> theme.outline
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, gradeColor.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(healthTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                            if (report != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(gradeColor.copy(alpha = 0.18f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(report.grade.name, color = gradeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, softWrap = false)
                                }
                            }
                        }

                        if (report != null) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                VehicleStatChip(label = "Avg RTT", value = "${report.avgLatencyMs} ms", modifier = Modifier.weight(1f))
                                VehicleStatChip(label = "Min / Max", value = "${report.minLatencyMs}/${report.maxLatencyMs} ms", modifier = Modifier.weight(1f))
                                VehicleStatChip(label = "Loss", value = "${report.packetLossPercent}%", modifier = Modifier.weight(1f))
                            }
                            Text(
                                text = report.localizedSummary(strings.language),
                                color = gradeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        com.punhanaghayev.bimmerpro.ui.components.BmwOutlinedButton(
                            text = runTestBtn,
                            icon = Icons.Default.Wifi,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onRunAdapterHealthCheck
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSelectorCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = LocalThemeStyle.current
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) theme.primary.copy(alpha = 0.22f) else theme.card
        ),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) theme.primaryBright else theme.outline
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = if (subtitle != null) 12.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) theme.primaryBright else theme.textSecondary,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = title,
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = theme.textSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun GarageHeroHeader(connectionState: ConnectionState) {
    val strings = LocalAppStrings.current
    val theme = LocalThemeStyle.current
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.card),
        border = BorderStroke(1.dp, theme.outline),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BmwRoundelLogo(size = 54.dp)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BMW Expert PRO",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.textPrimary
                    )
                    Text(
                        text = strings.heroSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.textSecondary
                    )
                    Text(
                        text = strings.heroSubtitleLine2,
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.primaryBright,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(6.dp))
                    val (statusText, statusColor) = when (connectionState) {
                        is ConnectionState.Connected -> connectionState.endpoint to StatusGreen
                        is ConnectionState.Connecting -> strings.connectingText to StatusAmber
                        is ConnectionState.Error -> "Error" to StatusRed
                        ConnectionState.Disconnected -> strings.disconnectedHint to theme.textSecondary
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(text = statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            MSportAccentStrip()
        }
    }
}

@Composable
private fun VehicleStatChip(label: String, value: String, modifier: Modifier = Modifier) {
    val theme = LocalThemeStyle.current
    Column(modifier = modifier.padding(vertical = 2.dp)) {
        Text(text = label, color = theme.textSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = value, color = theme.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    title: String,
    badge: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = LocalThemeStyle.current
    Column(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(theme.cardElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            color = theme.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
        if (!badge.isNullOrBlank()) {
            Text(badge, color = theme.textSecondary, fontSize = 10.sp, maxLines = 1)
        }
    }
}
