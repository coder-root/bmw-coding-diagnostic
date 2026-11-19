package com.punhanaghayev.bimmerpro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.bmw.ActuatorTestItem
import com.punhanaghayev.bimmerpro.core.bmw.EcuAddress
import com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutine
import com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutineType
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import com.punhanaghayev.bimmerpro.ui.components.SportArcGauge
import com.punhanaghayev.bimmerpro.ui.components.TelemetrySparkline
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusAmber
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveServiceScreen(
    session: BimmerSessionState,
    isLiveStreamRunning: Boolean,
    onToggleLiveStream: () -> Unit,
    onTogglePerformanceRun: () -> Unit,
    onExportCsv: () -> Unit,
    onExecuteRoutine: (ServiceRoutine, Byte) -> Unit,
    onRunActuatorTest: (ActuatorTestItem) -> Unit,
    onWriteServiceHistory: (String, Int, String, List<String>) -> Unit,
    onWriteInjectorCode: (Int, String) -> Unit,
    onResetZfAdaptations: () -> Unit,
    onSendEcuReset: (Int, String, Int) -> Unit,
    onGoToGarage: () -> Unit
) {
    val profile = session.vehicleProfile
    val strings = LocalAppStrings.current
    val theme = LocalThemeStyle.current
    if (profile == null) {
        DisconnectedPlaceholder(
            title = strings.disconnectedLiveTitle,
            subtitle = strings.disconnectedLiveSub,
            onGoToGarage = onGoToGarage
        )
        return
    }

    var batteryDialogRoutine by remember { mutableStateOf<ServiceRoutine?>(null) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var editingInjectorCyl by remember { mutableStateOf<Pair<Int, String>?>(null) }

    editingInjectorCyl?.let { (cyl, currentCode) ->
        var codeInput by remember { mutableStateOf(currentCode) }
        AlertDialog(
            onDismissRequest = { editingInjectorCyl = null },
            title = { Text("Silindir $cyl Enjektör IMA Kodu") },
            text = {
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it.uppercase().take(8) },
                    label = { Text("6-7 Haneli IMA / IQA Kodu") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onWriteInjectorCode(cyl, codeInput)
                        editingInjectorCyl = null
                    },
                    enabled = codeInput.length >= 5
                ) {
                    Text("DME/DDE'ye Yaz")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingInjectorCyl = null }) { Text("İptal") }
            }
        )
    }

    if (showHistoryDialog) {
        var dealerCode by remember { mutableStateOf("Punhan Garage (08801)") }
        var mileageInput by remember { mutableStateOf(profile.odometerKm.toString()) }
        val allItems = listOf("Motor Yağı", "Mikrofiltre", "Hava Filtresi", "Fren Hidroliği", "Buji", "Araç Kontrol")
        var selectedItems by remember { mutableStateOf(setOf("Motor Yağı", "Mikrofiltre")) }

        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text("iDrive Dijital Servis Geçmişi Yaz") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dealerCode,
                        onValueChange = { dealerCode = it },
                        label = { Text("Servis / Bayi Kodu") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mileageInput,
                        onValueChange = { mileageInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Bakım Kilometresi (km)") },
                        singleLine = true
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        allItems.forEach { item ->
                            val isSel = item in selectedItems
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    selectedItems = if (isSel) selectedItems - item else selectedItems + item
                                },
                                label = { Text(item, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val km = mileageInput.toIntOrNull() ?: profile.odometerKm
                        val todayStr = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.US).format(java.util.Date())
                        onWriteServiceHistory(todayStr, km, dealerCode, selectedItems.toList())
                        showHistoryDialog = false
                    },
                    enabled = selectedItems.isNotEmpty()
                ) {
                    Text("HU Modülüne Yaz")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHistoryDialog = false }) { Text("İptal") }
            }
        )
    }

    batteryDialogRoutine?.let { routine ->
        val capacities = listOf(
            "70 Ah AGM" to 0x70.toByte(),
            "80 Ah AGM" to 0x80.toByte(),
            "90 Ah AGM" to 0x90.toByte(),
            "105 Ah AGM" to 0xA5.toByte(),
            "80 Ah Sulu (Lead-Acid)" to 0x40.toByte()
        )
        AlertDialog(
            onDismissRequest = { batteryDialogRoutine = null },
            title = { Text("Yeni Akü Kapasitesi Seçin") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("IBS şarj profili seçilen kapasiteye göre sıfırlanacak:", fontSize = 12.sp)
                    capacities.forEach { (label, code) ->
                        OutlinedButton(
                            onClick = {
                                batteryDialogRoutine = null
                                onExecuteRoutine(routine, code)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { batteryDialogRoutine = null }) { Text("İptal") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.liveTelemetryTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                    Text("UDS 0x22 DID", color = theme.textSecondary, fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onExportCsv,
                        modifier = Modifier.height(38.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("CSV", fontSize = 11.sp, maxLines = 1, softWrap = false)
                    }
                    Button(
                        onClick = onToggleLiveStream,
                        modifier = Modifier.height(38.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLiveStreamRunning) StatusRed else theme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isLiveStreamRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (isLiveStreamRunning) strings.stopBtn else strings.startBtn, fontSize = 12.sp, maxLines = 1, softWrap = false)
                    }
                }
            }
        }

        item {
            val rpmVal = session.liveChannels.firstOrNull { it.id == "rpm" }?.currentValue ?: 780.0
            val boostVal = session.liveChannels.firstOrNull { it.id == "boost" }?.currentValue ?: 0.25
            val estHp = ((rpmVal / 6500.0) * 184.0 * (1.0 + boostVal.coerceAtLeast(0.0) * 0.45)).toInt().coerceAtLeast(18)
            val estNm = (240 + (boostVal.coerceAtLeast(0.0) * 160)).toInt()

            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.sportGaugeTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SportArcGauge(
                            label = "RPM",
                            valueText = "${rpmVal.toInt()}",
                            progress = (rpmVal / 7000.0).toFloat(),
                            accentColor = theme.primaryBright
                        )
                        SportArcGauge(
                            label = "BOOST",
                            valueText = "%.2f b".format(boostVal),
                            progress = ((boostVal + 0.5) / 2.3).toFloat(),
                            accentColor = StatusGreen
                        )
                        SportArcGauge(
                            label = "$estNm Nm",
                            valueText = "$estHp HP",
                            progress = (estHp / 350.0).toFloat(),
                            accentColor = theme.accentRed
                        )
                    }
                }
            }
        }

        items(session.liveChannels.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { channel ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = theme.card),
                        border = BorderStroke(1.dp, theme.outline),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(channel.name, color = theme.textSecondary, fontSize = 11.sp, maxLines = 1)
                            Text(
                                text = "${channel.currentValue} ${channel.unit}",
                                color = theme.textPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            TelemetrySparkline(
                                points = channel.history,
                                lineColor = theme.primaryBright,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                            )
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        item {
            val perf = session.performanceTimer
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.primary.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(strings.perfTimerTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                        Text(
                            text = "%d km/h • %.1f sn%s".format(
                                perf.currentSpeedKmh,
                                perf.elapsedMs / 1000.0,
                                perf.bestZeroToHundredSec?.let { " | En İyi: %.1f sn".format(it) } ?: ""
                            ),
                            color = theme.primaryBright,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = onTogglePerformanceRun,
                        modifier = Modifier.height(38.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (perf.isRunning) StatusRed else theme.primary
                        )
                    ) {
                        Text(if (perf.isRunning) strings.stopBtn else strings.startBtn, maxLines = 1, softWrap = false, fontSize = 12.sp)
                    }
                }
            }
        }

        // ECU Reset & Sleep Mode Commands (UDS 0x11)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.ecuResetTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onSendEcuReset(EcuAddress.ZGW_BDC, "ZGW/BDC", 0x01) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.hardResetBtn, fontSize = 11.sp, maxLines = 1, softWrap = false)
                        }
                        OutlinedButton(
                            onClick = { onSendEcuReset(EcuAddress.ZGW_BDC, "Gateway Sleep", 0x03) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Bedtime, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.gatewaySleepBtn, fontSize = 11.sp, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text(strings.serviceRoutinesTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = theme.textPrimary)
        }

        session.lastServiceResult?.let { res ->
            item {
                val color = if (res.success) StatusGreen else StatusRed
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.14f))
                        .padding(12.dp)
                ) {
                    Text("${res.timestamp} • ${res.title}: ${res.message}", color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        items(session.serviceRoutines, key = { it.routineId }) { routine ->
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${routine.targetEcu} • RID 0x%04X".format(routine.routineId), color = theme.primaryBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(routine.title, color = theme.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(routine.description, color = theme.textSecondary, fontSize = 12.sp)
                    Text("Not: ${routine.warningNote}", color = StatusAmber, fontSize = 11.sp)
                    Button(
                        onClick = {
                            if (routine.type == ServiceRoutineType.BATTERY_REGISTRATION) {
                                batteryDialogRoutine = routine
                            } else {
                                onExecuteRoutine(routine, 0x01)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        modifier = Modifier.align(Alignment.End).height(38.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.runRoutineBtn, fontSize = 12.sp, maxLines = 1, softWrap = false)
                    }
                }
            }
        }

        // Fixed iDrive Digital Service History Header + Compact Write Button
        item {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.serviceHistoryTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = { showHistoryDialog = true },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, theme.primaryBright.copy(alpha = 0.7f))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.writeRecordBtn, fontSize = 11.sp, color = theme.primaryBright, maxLines = 1, softWrap = false)
                }
            }
        }

        items(session.serviceHistory, key = { it.id }) { entry ->
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${entry.date} • %,d km".format(entry.mileageKm), color = theme.primaryBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(entry.dealerCode, color = theme.textSecondary, fontSize = 11.sp)
                    }
                    Text(entry.performedItems.joinToString(" • "), color = theme.textPrimary, fontSize = 12.sp)
                }
            }
        }

        if (session.zfAdaptations.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.zfAdaptTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = theme.textPrimary, modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = onResetZfAdaptations,
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Text(strings.resetBtn, fontSize = 11.sp, color = StatusAmber, maxLines = 1, softWrap = false)
                    }
                }
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, theme.outline),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        session.zfAdaptations.forEach { clutch ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(clutch.clutchName, color = theme.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${clutch.pressureMbar} mbar  |  ${clutch.fillTimeMs} ms",
                                    color = theme.primaryBright,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (session.injectorCodes.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Text(strings.injectorTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = theme.textPrimary)
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, theme.outline),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        session.injectorCodes.forEach { inj ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sil. ${inj.cylinder}: ${inj.imaCode}", color = theme.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                TextButton(onClick = { editingInjectorCyl = inj.cylinder to inj.imaCode }) {
                                    Text(strings.codeBtn, color = theme.primaryBright, fontSize = 12.sp, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }
            }
        }

        // IBS Battery Health Card
        session.ibsHealth?.let { ibs ->
            item {
                Spacer(Modifier.height(4.dp))
                Text("IBS Akü Sağlığı & Son 5 Gün (%)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = theme.textPrimary)
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, theme.outline),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Şarj (SOC): %${ibs.stateOfChargePercent}", color = StatusGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Sağlık (SOH): %${ibs.stateOfHealthPercent}", color = theme.primaryBright, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${ibs.capacityAh}Ah ${if (ibs.isAgm) "AGM" else "Sulu"}", color = theme.textSecondary, fontSize = 12.sp)
                        }
                        TelemetrySparkline(
                            points = ibs.last5DaysSoc.map { it.toFloat() },
                            lineColor = StatusGreen,
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        )
                    }
                }
            }
        }

        // UDS 0x2F Actuator / Component Tests
        if (session.actuatorTests.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Text("Aktüatör & Parça Testleri (UDS 0x2F)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = theme.textPrimary)
            }
            items(session.actuatorTests, key = { it.id }) { test ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, theme.outline),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("${test.targetEcu} • 0x%04X".format(test.did), color = theme.primaryBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(test.title, color = theme.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(test.description, color = theme.textSecondary, fontSize = 11.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { onRunActuatorTest(test) },
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Text("Test Et", fontSize = 11.sp, color = theme.primaryBright, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }
    }
}
