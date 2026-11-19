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
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
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
    onConnect: (AdapterType, String, Int?) -> Unit,
    onDisconnect: () -> Unit,
    onAdjustSimVoltage: (Double) -> Unit,
    onScanHardware: () -> Unit,
    onSelectBleDevice: (String) -> Unit,
    onRemoveSavedVehicle: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    val physicalAdapters = remember { AdapterType.entries.filter { !it.isSimulator } }
    val simulatorAdapters = remember { AdapterType.entries.filter { it.isSimulator } }

    var isSimulatorMode by remember(session.selectedAdapter) {
        mutableStateOf(
            if (session.connectionState is ConnectionState.Connected) session.selectedAdapter.isSimulator else false
        )
    }
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
                    title = "Connect",
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
                    title = "Simulator",
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = profile.modelDisplayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.width(8.dp))
                            BatterySafetyPill(voltage = profile.batteryVoltage)
                        }

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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            VehicleStatChip("Şasi / Seri", "${profile.chassis.code} (${profile.chassis.family.label})")
                            VehicleStatChip("Motor", profile.engineCode)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            VehicleStatChip("I-Level", profile.iLevel)
                            VehicleStatChip("Kilometre", "%,d km".format(profile.odometerKm))
                        }

                        profile.chassis.secureCodingNotice?.let { notice ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(StatusAmber.copy(alpha = 0.14f))
                                    .padding(10.dp)
                            ) {
                                Text(text = notice, color = StatusAmber, fontSize = 12.sp)
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
                                    text = "Simülatör Akü Voltajı Testi: %.1f V (<12.2V kodlamayı kilitler)".format(profile.batteryVoltage),
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickActionTile(
                                icon = Icons.Default.Memory,
                                title = strings.quickDiagnostics,
                                badge = "${session.dtcs.size} DTC",
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToTab(1) }
                            )
                            QuickActionTile(
                                icon = Icons.Default.Build,
                                title = strings.quickCoding,
                                badge = "${session.presets.size} Preset",
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToTab(2) }
                            )
                            QuickActionTile(
                                icon = Icons.Default.Speed,
                                title = strings.quickLiveService,
                                badge = "${session.liveChannels.size} PID",
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToTab(3) }
                            )
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

        // Adapter or Simulator Selection Hub
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isSimulatorMode) "BMW Araç Simülatörü" else strings.connectionHubTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = theme.textPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = strings.adapterTitle(activeAdapter),
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            label = { Text(strings.adapterDropdownLabel, maxLines = 1) },
                            supportingText = {
                                Text(strings.adapterDesc(activeAdapter), color = theme.primaryBright, fontSize = 11.sp)
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when {
                                        activeAdapter == AdapterType.VLINKER_BM_WIFI -> Icons.Default.Wifi
                                        activeAdapter.isSimulator -> Icons.Default.DirectionsCar
                                        else -> Icons.Default.Cable
                                    },
                                    contentDescription = null,
                                    tint = theme.primaryBright
                                )
                            },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(theme.cardElevated)
                        ) {
                            activeChoices.forEach { adapter ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(strings.adapterTitle(adapter), color = theme.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text(strings.adapterDesc(adapter), color = theme.textSecondary, fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        if (isSimulatorMode) selectedSimulator = adapter else selectedPhysical = adapter
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (!isSimulatorMode) {
                        when (activeAdapter) {
                            AdapterType.VLINKER_BM_WIFI -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(theme.primary.copy(alpha = 0.14f))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Vgate vLinker BM Wi-Fi: 'vLinker-BM' Wi-Fi ağına bağlanın (192.168.0.10:35000). Otomatik Wi-Fi soket kilidi aktiftir.",
                                        color = theme.primaryBright,
                                        fontSize = 11.sp
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = vlinkerHost,
                                        onValueChange = { vlinkerHost = it },
                                        label = { Text("IP") },
                                        singleLine = true,
                                        modifier = Modifier.weight(0.65f)
                                    )
                                    OutlinedTextField(
                                        value = vlinkerPort,
                                        onValueChange = { vlinkerPort = it.filter { c -> c.isDigit() } },
                                        label = { Text("Port") },
                                        singleLine = true,
                                        modifier = Modifier.weight(0.35f)
                                    )
                                }
                            }

                            AdapterType.ENET_WIFI, AdapterType.ENET_USB -> {
                                OutlinedTextField(
                                    value = enetHost,
                                    onValueChange = { enetHost = it },
                                    label = { Text("Gateway IP (169.254.0.1)") },
                                    singleLine = true,
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
                            when (activeAdapter) {
                                AdapterType.VLINKER_BM_WIFI -> onConnect(
                                    activeAdapter,
                                    vlinkerHost.trim(),
                                    vlinkerPort.toIntOrNull() ?: TcpElmStnTransportAdapter.DEFAULT_VLINKER_PORT
                                )
                                AdapterType.ENET_WIFI, AdapterType.ENET_USB -> onConnect(
                                    activeAdapter,
                                    enetHost.trim(),
                                    TcpEnetTransportAdapter.DEFAULT_HSFZ_PORT
                                )
                                else -> onConnect(activeAdapter, "", null)
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
                                text = if (isSimulatorMode) "Simülatörü Başlat" else strings.connectVehicleBtn,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        if (session.savedVehicles.isNotEmpty()) {
            item {
                Text(
                    text = strings.savedVehiclesTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
            }
            items(session.savedVehicles, key = { it.vin }) { saved ->
                Card(
                    modifier = Modifier.clickable {
                        val matched = AdapterType.entries.firstOrNull { it.displayName == saved.adapterName }
                            ?: AdapterType.SIMULATOR_F10
                        if (matched.isSimulator) {
                            isSimulatorMode = true
                            selectedSimulator = matched
                        } else {
                            isSimulatorMode = false
                            selectedPhysical = matched
                        }
                        onConnect(matched, "", null)
                    },
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, theme.outline),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(saved.modelName, color = theme.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("VIN: ${saved.vin}", color = theme.primaryBright, fontSize = 11.sp, fontFamily = FontFamily.Monospace, maxLines = 1, softWrap = false)
                            Text("${saved.lastConnected} • %,d km".format(saved.odometerKm), color = theme.textSecondary, fontSize = 11.sp)
                        }
                        IconButton(onClick = { onRemoveSavedVehicle(saved.vin) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSelectorCard(
    title: String,
    subtitle: String,
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
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) theme.primaryBright else theme.textSecondary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(title, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text(subtitle, color = theme.textSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
private fun VehicleStatChip(label: String, value: String) {
    val theme = LocalThemeStyle.current
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = label, color = theme.textSecondary, fontSize = 11.sp)
        Text(text = value, color = theme.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    title: String,
    badge: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = LocalThemeStyle.current
    Column(
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(theme.cardElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            color = theme.textPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
        Text(badge, color = theme.textSecondary, fontSize = 10.sp, maxLines = 1)
    }
}
