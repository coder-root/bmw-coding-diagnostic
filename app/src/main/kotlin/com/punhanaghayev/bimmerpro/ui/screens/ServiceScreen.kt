package com.punhanaghayev.bimmerpro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.bmw.ActuatorTestItem
import com.punhanaghayev.bimmerpro.core.bmw.ClutchWearLevel
import com.punhanaghayev.bimmerpro.core.bmw.DpfHealthStatus
import com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutine
import com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutineType
import com.punhanaghayev.bimmerpro.core.bmw.ZfGearboxHealth
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusAmber
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceScreen(
    session: BimmerSessionState,
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
    var showZfResetConfirm by remember { mutableStateOf(false) }

    editingInjectorCyl?.let { (cyl, currentCode) ->
        var codeInput by remember { mutableStateOf(currentCode) }
        AlertDialog(
            onDismissRequest = { editingInjectorCyl = null },
            title = { Text(strings.injectorImaDialogTitle.format(cyl), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it.uppercase().take(8) },
                    label = { Text(strings.injectorImaInputLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
                    Text(strings.writeDmeBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingInjectorCyl = null }) { Text(strings.cancelBtn) }
            }
        )
    }

    if (showHistoryDialog) {
        var dealerCode by remember { mutableStateOf("Punhan Garage (08801)") }
        var mileageInput by remember { mutableStateOf(profile.odometerKm.toString()) }
        val allItems = listOf("Motor Yağı", "Mikrofiltre", "Hava Filtresi", "Fren Hidroliği", "Buji Seti", "Araç Kontrolü")
        var selectedItems by remember { mutableStateOf(setOf("Motor Yağı", "Mikrofiltre")) }

        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text(strings.writeServiceHistoryTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dealerCode,
                        onValueChange = { dealerCode = it },
                        label = { Text(strings.dealerCodeLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = mileageInput,
                        onValueChange = { mileageInput = it.filter { c -> c.isDigit() } },
                        label = { Text(strings.mileageLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Yapılan Bakım Kalemleri:", fontSize = 12.sp, color = theme.textSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                TextButton(onClick = { showHistoryDialog = false }) { Text(strings.cancelBtn) }
            }
        )
    }

    if (showZfResetConfirm) {
        AlertDialog(
            onDismissRequest = { showZfResetConfirm = false },
            title = { Text("ZF 8HP Adaptasyonları Sıfırlansın mı?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Kavrama dolum süreleri ve basınç değerleri fabrika ayarlarına döndürülecektir. Sıfırlama sonrası ilk 50 km boyunca vites geçişleri öğrenme sürecinde olacaktır.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showZfResetConfirm = false
                        onResetZfAdaptations()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAmber)
                ) {
                    Text(strings.resetBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { showZfResetConfirm = false }) { Text(strings.cancelBtn) }
            }
        )
    }

    batteryDialogRoutine?.let { routine ->
        val capacities = listOf(
            "70 Ah AGM" to 0x70.toByte(),
            "80 Ah AGM" to 0x80.toByte(),
            "90 Ah AGM" to 0x90.toByte(),
            "105 Ah AGM" to 0xA5.toByte(),
            "80 Ah ${strings.leadAcidLabel}" to 0x40.toByte()
        )
        AlertDialog(
            onDismissRequest = { batteryDialogRoutine = null },
            title = { Text(strings.selectBatteryCapacityTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.ibsCapacityResetPrompt, fontSize = 12.sp, color = theme.textSecondary)
                    capacities.forEach { (label, code) ->
                        OutlinedButton(
                            onClick = {
                                batteryDialogRoutine = null
                                onExecuteRoutine(routine, code)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label, color = theme.textPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { batteryDialogRoutine = null }) { Text(strings.cancelBtn) }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // IBS Battery Health Overview
        item {
            val ibs = session.ibsHealth
            val soc = ibs?.stateOfChargePercent ?: 88
            val soh = ibs?.stateOfHealthPercent ?: 88
            val capacity = ibs?.capacityAh ?: 90
            val isAgm = ibs?.isAgm ?: true
            val socColor = if (soc >= 70) StatusGreen else StatusAmber
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, socColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = socColor, modifier = Modifier.size(18.dp))
                            Text(strings.ibsHealthTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                        }
                        Text(
                            text = "$capacity Ah " + (if (isAgm) "AGM" else strings.leadAcidLabel),
                            color = theme.primaryBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(strings.socLabel, color = theme.textSecondary, fontSize = 11.sp)
                            Text("$soc%", color = socColor, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                        Column {
                            Text(strings.sohLabel, color = theme.textSecondary, fontSize = 11.sp)
                            Text("$soh%", color = StatusGreen, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                        Column {
                            Text(strings.terminalVoltageLabel, color = theme.textSecondary, fontSize = 11.sp)
                            Text("%.1f V".format(profile.batteryVoltage), color = theme.textPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        // Service Routines (UDS 0x31)
        item {
            Text(
                text = "${strings.serviceRoutinesTitle} (UDS 0x31)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
        }

        items(session.serviceRoutines) { routine ->
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${routine.targetEcu} • RID 0x%04X".format(routine.routineId),
                        color = theme.primaryBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = routine.localizedTitle(strings),
                        color = theme.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = routine.localizedDescription(strings),
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    )
                    val warn = routine.localizedWarning(strings)
                    if (warn.isNotBlank()) {
                        Text(
                            text = "${strings.notePrefix} $warn",
                            color = StatusAmber,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = {
                            if (routine.type == ServiceRoutineType.BATTERY_REGISTRATION) {
                                batteryDialogRoutine = routine
                            } else {
                                onExecuteRoutine(routine, 0x01)
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.runRoutineBtn, fontSize = 12.sp)
                    }
                }
            }
        }

        // Diesel Particulate Filter (DPF) Live Monitor (BimmerLink / BimmerTool style)
        session.dpfHealth?.let { dpf ->
            item {
                val sootColor = when {
                    dpf.sootMassGrams > 35.0 -> StatusRed
                    dpf.sootMassGrams > 22.0 -> StatusAmber
                    else -> StatusGreen
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, sootColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = sootColor, modifier = Modifier.size(18.dp))
                                Text(strings.dpfMonitorTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (dpf.isRegenActive) StatusGreen.copy(alpha = 0.2f) else theme.cardElevated)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (dpf.isRegenActive) strings.dpfStatusActive else strings.dpfStatusIdle,
                                    color = if (dpf.isRegenActive) StatusGreen else theme.textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Soot gauge progress
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(strings.dpfSootLabel, color = theme.textSecondary, fontSize = 11.sp)
                                Text("%.1f g / 45.0 g".format(dpf.sootMassGrams), color = sootColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            LinearProgressIndicator(
                                progress = { (dpf.sootMassGrams / 45.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = sootColor,
                                trackColor = theme.outline.copy(alpha = 0.3f)
                            )
                        }

                        // DPF Stats Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(strings.dpfAshLabel, color = theme.textSecondary, fontSize = 10.sp)
                                Text("%.1f g".format(dpf.ashMassGrams), color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Column {
                                Text(strings.dpfBackpressureLabel, color = theme.textSecondary, fontSize = 10.sp)
                                Text("%.1f mbar".format(dpf.backpressureMbar), color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Column {
                                Text(strings.dpfLastRegenLabel, color = theme.textSecondary, fontSize = 10.sp)
                                Text("%,d km".format(dpf.distanceSinceLastRegenKm), color = theme.primaryBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Column {
                                Text(strings.dpfPreTempLabel, color = theme.textSecondary, fontSize = 10.sp)
                                Text("%d°C".format(dpf.exhaustTempPreDpfC), color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        // Trigger Regen Button
                        val regenRoutine = session.serviceRoutines.firstOrNull { it.type == ServiceRoutineType.DPF_REGENERATION }
                        if (regenRoutine != null) {
                            Button(
                                onClick = { onExecuteRoutine(regenRoutine, 0x01) },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(strings.dpfTriggerBtn, fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // ZF 8HP Transmission Adaptations & Health
        item {
            val zf = session.zfHealth
            val overallScore = zf?.overallHealthScorePercent ?: 95
            val scoreColor = if (overallScore >= 85) StatusGreen else StatusAmber
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = scoreColor, modifier = Modifier.size(18.dp))
                            Text(strings.zfAdaptTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(scoreColor.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$overallScore% ${strings.zfHealthScoreLabel}",
                                color = scoreColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Oil Temp & Degradation
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(strings.zfOilTempLabel, color = theme.textSecondary, fontSize = 10.sp)
                            Text("${zf?.oilTemperatureC ?: 84}°C", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Column {
                            Text(strings.zfOilConditionLabel, color = theme.textSecondary, fontSize = 10.sp)
                            Text("${zf?.oilDegradationScorePercent ?: 91}%", color = StatusGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { showZfResetConfirm = true },
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.resetBtn, fontSize = 11.sp)
                        }
                    }

                    // Clutches A to E
                    val details = zf?.clutches ?: session.zfAdaptations.map {
                        com.punhanaghayev.bimmerpro.core.bmw.ZfClutchDetail(
                            it.clutchName,
                            it.pressureMbar,
                            it.fillTimeMs,
                            com.punhanaghayev.bimmerpro.core.bmw.ClutchWearLevel.OPTIMAL,
                            "Nominal"
                        )
                    }
                    details.forEach { c ->
                        val wearColor = when (c.wearLevel) {
                            com.punhanaghayev.bimmerpro.core.bmw.ClutchWearLevel.OPTIMAL -> StatusGreen
                            com.punhanaghayev.bimmerpro.core.bmw.ClutchWearLevel.MODERATE_WEAR -> StatusAmber
                            com.punhanaghayev.bimmerpro.core.bmw.ClutchWearLevel.CRITICAL_WEAR -> StatusRed
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(c.clutchName, color = theme.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "%+d mbar • %+d ms".format(c.pressureMbar, c.fillTimeMs),
                                    color = theme.textSecondary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(wearColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (c.wearLevel == com.punhanaghayev.bimmerpro.core.bmw.ClutchWearLevel.OPTIMAL) strings.zfClutchOptimal else strings.zfClutchWarn,
                                        color = wearColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Injector IMA Calibration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.injectorTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                    session.injectorCodes.forEach { inj ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.cylinderPrefix.format(inj.cylinder), color = theme.textPrimary, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    inj.imaCode,
                                    color = theme.primaryBright,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                OutlinedButton(
                                    onClick = { editingInjectorCyl = inj.cylinder to inj.imaCode },
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(strings.codeBtn, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // CBS Digital Service History
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.History, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(18.dp))
                            Text(strings.serviceHistoryTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                        }
                        Button(
                            onClick = { showHistoryDialog = true },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.writeRecordBtn, fontSize = 11.sp)
                        }
                    }
                    session.serviceHistory.forEach { rec ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.cardElevated)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(rec.date, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 12.sp)
                                Text("%,d km".format(rec.mileageKm), color = theme.primaryBright, fontSize = 12.sp)
                            }
                            Text(rec.dealerCode, color = theme.textSecondary, fontSize = 11.sp)
                            Text(rec.performedItems.joinToString(" • "), color = theme.textSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Actuator Tests (UDS 0x2F)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(18.dp))
                        Text(strings.actuatorTestsTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                    }
                    session.actuatorTests.forEach { test ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(test.localizedTitle(strings), color = theme.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("${test.targetEcu} • DID 0x%04X".format(test.did), color = theme.textSecondary, fontSize = 10.sp)
                            }
                            OutlinedButton(
                                onClick = { onRunActuatorTest(test) },
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(strings.testBtn, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // ECU Diagnostic Reset Controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(18.dp))
                        Text(strings.ecuResetTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onSendEcuReset(0x10, "ZGW Gateway", 0x04) },
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text(strings.gatewaySleepBtn, fontSize = 11.sp, maxLines = 1, softWrap = false)
                        }
                        OutlinedButton(
                            onClick = { onSendEcuReset(0x40, "CAS/BDC", 0x01) },
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text(strings.hardResetBtn, fontSize = 11.sp, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }
    }
}
