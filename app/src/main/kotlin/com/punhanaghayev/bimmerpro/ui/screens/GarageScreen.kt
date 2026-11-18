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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Memory
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import com.punhanaghayev.bimmerpro.core.protocol.ConnectionState
import com.punhanaghayev.bimmerpro.core.protocol.TcpElmStnTransportAdapter
import com.punhanaghayev.bimmerpro.core.protocol.TcpEnetTransportAdapter
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import com.punhanaghayev.bimmerpro.ui.components.BatterySafetyPill
import com.punhanaghayev.bimmerpro.ui.components.BmwRoundelLogo
import com.punhanaghayev.bimmerpro.ui.components.MSportAccentStrip
import com.punhanaghayev.bimmerpro.ui.theme.BimmerCard
import com.punhanaghayev.bimmerpro.ui.theme.BimmerCardElevated
import com.punhanaghayev.bimmerpro.ui.theme.BimmerOutline
import com.punhanaghayev.bimmerpro.ui.theme.BmwBlue
import com.punhanaghayev.bimmerpro.ui.theme.BmwBlueBright
import com.punhanaghayev.bimmerpro.ui.theme.StatusAmber
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed
import com.punhanaghayev.bimmerpro.ui.theme.TextPrimary
import com.punhanaghayev.bimmerpro.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(
    session: BimmerSessionState,
    onConnect: (AdapterType, String, Int?) -> Unit,
    onDisconnect: () -> Unit,
    onAdjustSimVoltage: (Double) -> Unit,
    onScanHardware: () -> Unit,
    onSelectBleDevice: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    var selectedAdapter by remember(session.selectedAdapter) { mutableStateOf(session.selectedAdapter) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var vlinkerHost by remember { mutableStateOf(TcpElmStnTransportAdapter.DEFAULT_VLINKER_HOST) }
    var vlinkerPort by remember { mutableStateOf(TcpElmStnTransportAdapter.DEFAULT_VLINKER_PORT.toString()) }
    var enetHost by remember { mutableStateOf(TcpEnetTransportAdapter.DEFAULT_ZGW_HOST) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GarageHeroHeader(connectionState = session.connectionState)
        }

        if (session.vehicleProfile != null && session.connectionState is ConnectionState.Connected) {
            val profile = session.vehicleProfile
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BimmerCard),
                    border = BorderStroke(1.dp, BmwBlue.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.modelDisplayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "VIN: ${profile.vin}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BmwBlueBright
                                )
                            }
                            BatterySafetyPill(voltage = profile.batteryVoltage)
                        }

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

                        if (session.selectedAdapter.name.startsWith("SIMULATOR")) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BimmerCardElevated)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "Simülatör Akü Voltajı Testi: %.1f V (<12.2V kodlamayı kilitler)".format(profile.batteryVoltage),
                                    color = TextSecondary,
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
                                title = "Diagnostik",
                                badge = "${session.dtcs.size} DTC",
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToTab(1) }
                            )
                            QuickActionTile(
                                icon = Icons.Default.Build,
                                title = "Kodlama",
                                badge = "${session.presets.size} Preset",
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToTab(2) }
                            )
                            QuickActionTile(
                                icon = Icons.Default.Speed,
                                title = "Canlı & Servis",
                                badge = "${session.liveChannels.size} Sensör",
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToTab(3) }
                            )
                        }

                        OutlinedButton(
                            onClick = onDisconnect,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                            border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Araç Bağlantısını Kes")
                        }
                    }
                }
            }
        }

        // Sleek Connection Hub Card with Dropdown Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BimmerCard),
                border = BorderStroke(1.dp, BimmerOutline),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OBD / ENET Bağlantı Merkezi",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(onClick = onScanHardware) {
                            Text("Donanım Tara", fontSize = 11.sp, color = BmwBlueBright)
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedAdapter.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Bağlantı Adaptörü / Simülatör Seçin") },
                            supportingText = { Text(selectedAdapter.protocolSummary, color = BmwBlueBright, fontSize = 11.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when {
                                        selectedAdapter == AdapterType.VLINKER_BM_WIFI -> Icons.Default.Wifi
                                        selectedAdapter.name.startsWith("SIMULATOR") -> Icons.Default.DirectionsCar
                                        else -> Icons.Default.Cable
                                    },
                                    contentDescription = null,
                                    tint = BmwBlueBright
                                )
                            },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(BimmerCardElevated)
                        ) {
                            AdapterType.entries.forEachIndexed { idx, adapter ->
                                val isSim = adapter.name.startsWith("SIMULATOR")
                                if (idx > 0 && isSim && !AdapterType.entries[idx - 1].name.startsWith("SIMULATOR")) {
                                    HorizontalDivider(color = BimmerOutline)
                                }
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(adapter.displayName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text(adapter.protocolSummary, color = TextSecondary, fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedAdapter = adapter
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Dynamic adapter configuration fields
                    when (selectedAdapter) {
                        AdapterType.VLINKER_BM_WIFI -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BmwBlue.copy(alpha = 0.14f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "Vgate vLinker BM Wi-Fi: Telefonunuzu 'vLinker-BM' Wi-Fi ağına bağlayın (Varsayılan: 192.168.0.10:35000). BMW F10 2015 D-CAN / UDS uyumludur.",
                                    color = BmwBlueBright,
                                    fontSize = 11.sp
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = vlinkerHost,
                                    onValueChange = { vlinkerHost = it },
                                    label = { Text("vLinker IP") },
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
                                label = { Text("ZGW / BDC Gateway IP (Varsayılan: 169.254.0.1)") },
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
                                .background(BimmerCardElevated)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Bulunan Fiziksel Aygıtlar", color = BmwBlueBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            session.discoveredGateways.forEach { gw ->
                                Text(
                                    text = "• ENET Gateway: ${gw.ipAddress} ${gw.vin?.let { "($it)" } ?: ""}",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable {
                                        enetHost = gw.ipAddress
                                        selectedAdapter = AdapterType.ENET_WIFI
                                    }
                                )
                            }
                            session.discoveredBleDevices.forEach { ble ->
                                val active = session.selectedBleAddress == ble.address
                                Text(
                                    text = "• BLE: ${ble.name} (${ble.address}) ${if (active) "[Seçili]" else ""}",
                                    color = if (active) StatusGreen else TextPrimary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable {
                                        onSelectBleDevice(ble.address)
                                        selectedAdapter = AdapterType.BLE_OBD
                                    }
                                )
                            }
                            session.discoveredUsbDevices.forEach { usb ->
                                Text(
                                    text = "• USB: ${usb.productName} (VID:0x%04X)".format(usb.vendorId),
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable { selectedAdapter = AdapterType.USB_KDCAN }
                                )
                            }
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
                            when (selectedAdapter) {
                                AdapterType.VLINKER_BM_WIFI -> onConnect(
                                    selectedAdapter,
                                    vlinkerHost.trim(),
                                    vlinkerPort.toIntOrNull() ?: TcpElmStnTransportAdapter.DEFAULT_VLINKER_PORT
                                )
                                AdapterType.ENET_WIFI, AdapterType.ENET_USB -> onConnect(
                                    selectedAdapter,
                                    enetHost.trim(),
                                    TcpEnetTransportAdapter.DEFAULT_HSFZ_PORT
                                )
                                else -> onConnect(selectedAdapter, "", null)
                            }
                        },
                        enabled = !isConnecting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BmwBlue)
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Bağlanıyor & VIN Okunuyor...")
                        } else {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (session.connectionState is ConnectionState.Connected) {
                                    "Seçili Adaptörle Yeniden Bağlan"
                                } else {
                                    "Araca Bağlan (${selectedAdapter.displayName.substringBefore(" ")})"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GarageHeroHeader(connectionState: ConnectionState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BimmerCard),
        border = BorderStroke(1.dp, BimmerOutline),
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
                        color = TextPrimary
                    )
                    Text(
                        text = "Full BMW Kodlama, Diagnostik & Ekspertiz",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    val (statusText, statusColor) = when (connectionState) {
                        is ConnectionState.Connected -> "Bağlı: ${connectionState.endpoint}" to StatusGreen
                        is ConnectionState.Connecting -> "Bağlanıyor..." to StatusAmber
                        is ConnectionState.Error -> "Bağlantı Hatası" to StatusRed
                        ConnectionState.Disconnected -> "Bağlı Değil — Dropdown'dan adaptör seçin" to TextSecondary
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(text = statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            MSportAccentStrip()
        }
    }
}

@Composable
private fun VehicleStatChip(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BimmerCardElevated)
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = BmwBlueBright, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(badge, color = TextSecondary, fontSize = 10.sp)
    }
}
