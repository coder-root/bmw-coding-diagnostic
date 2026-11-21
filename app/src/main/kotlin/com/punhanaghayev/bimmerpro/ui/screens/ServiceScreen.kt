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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import com.punhanaghayev.bimmerpro.ui.components.BmwOutlinedButton
import com.punhanaghayev.bimmerpro.ui.components.BmwPrimaryButton
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
import com.punhanaghayev.bimmerpro.core.bmw.AdasCalibrationStatus
import com.punhanaghayev.bimmerpro.core.bmw.ClutchWearLevel
import com.punhanaghayev.bimmerpro.core.bmw.DpfHealthStatus
import com.punhanaghayev.bimmerpro.core.bmw.DtcSeverity
import com.punhanaghayev.bimmerpro.core.bmw.EcuAddress
import com.punhanaghayev.bimmerpro.core.bmw.EpbServiceState
import com.punhanaghayev.bimmerpro.core.bmw.ExhaustFlapMode
import com.punhanaghayev.bimmerpro.core.bmw.HvBatteryHealth
import com.punhanaghayev.bimmerpro.core.bmw.HvCellVoltage
import com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutine
import com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutineType
import com.punhanaghayev.bimmerpro.core.bmw.ShortCircuitChannel
import com.punhanaghayev.bimmerpro.core.bmw.ZfGearboxHealth
import com.punhanaghayev.bimmerpro.core.i18n.GlobalTextLocalizer
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
    onResetShortCircuitChannel: (ShortCircuitChannel) -> Unit,
    onSetExhaustFlapMode: (ExhaustFlapMode) -> Unit,
    onSetAsdState: (Boolean, Int) -> Unit,
    onSetEpbServiceMode: (Boolean) -> Unit,
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
    var pendingRoutineToExecute by remember { mutableStateOf<Pair<ServiceRoutine, Byte>?>(null) }
    var rememberRoutineConfirmation by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var editingInjectorCyl by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var showZfResetConfirm by remember { mutableStateOf(false) }

    pendingRoutineToExecute?.let { (routine, optionByte) ->
        AlertDialog(
            onDismissRequest = { pendingRoutineToExecute = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(20.dp))
                    Text(strings.routineConfirmTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = strings.routineConfirmWarning.format(routine.localizedTitle(strings), routine.targetEcu),
                        color = theme.textPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = strings.routineConfirmPreconditions,
                        color = StatusAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.cardElevated)
                            .clickable { rememberRoutineConfirmation = !rememberRoutineConfirmation }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberRoutineConfirmation,
                            onCheckedChange = { rememberRoutineConfirmation = it }
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = strings.rememberChoiceSession,
                            color = theme.textPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val r = routine
                        val opt = optionByte
                        pendingRoutineToExecute = null
                        onExecuteRoutine(r, opt)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                ) {
                    Text(strings.runRoutineBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRoutineToExecute = null }) { Text(strings.cancelBtn) }
            }
        )
    }

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
            title = { Text(strings.zfResetConfirmTitle, fontWeight = FontWeight.Bold) },
            text = {
                Text(strings.zfResetConfirmText)
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
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = socColor, modifier = Modifier.size(18.dp))
                            Text(
                                text = strings.ibsHealthTitle,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.primaryBright.copy(alpha = 0.16f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$capacity Ah ${if (isAgm) "AGM" else strings.leadAcidLabel}",
                                color = theme.primaryBright,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                softWrap = false
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(strings.socLabel, color = theme.textSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(2.dp))
                            Text("$soc%", color = socColor, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(strings.sohLabel, color = theme.textSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(2.dp))
                            Text("$soh%", color = StatusGreen, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(strings.terminalVoltageLabel, color = theme.textSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(2.dp))
                            Text("%.1f V".format(profile.batteryVoltage), color = theme.textPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                    }

                    // IBS 5-Day SoC History Mini Bar Chart
                    val socHistory = ibs?.last5DaysSoc ?: listOf(86, 84, 79, 81, soc)
                    if (socHistory.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(strings.ibs5DayHistoryLabel, color = theme.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.cardElevated)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                socHistory.forEachIndexed { idx, daySoc ->
                                    val barColor = if (daySoc >= 75) StatusGreen else if (daySoc >= 60) StatusAmber else StatusRed
                                    val barHeight = (12 + (daySoc / 100f) * 24).dp
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text("$daySoc%", color = barColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.55f)
                                                .height(barHeight)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(barColor.copy(alpha = 0.85f))
                                        )
                                        Text(
                                            text = if (idx == socHistory.lastIndex) "D0" else "D-${socHistory.lastIndex - idx}",
                                            color = theme.textSecondary,
                                            fontSize = 8.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // High-Voltage (HV) Battery Health (SME)
        session.hvBatteryHealth?.let { hv ->
            item {
                val deltaColor = when {
                    hv.cellDeltaMv < 25 -> StatusGreen
                    hv.cellDeltaMv < 50 -> StatusAmber
                    else -> StatusRed
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, deltaColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(18.dp))
                                Text(
                                    text = strings.hvBatteryTitle,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusGreen.copy(alpha = 0.16f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "SOH %${hv.stateOfHealthPercent}",
                                    color = StatusGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    softWrap = false
                                )
                            }
                        }

                        // Metrics row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(strings.hvCapacityLabel, color = theme.textSecondary, fontSize = 10.sp, maxLines = 1)
                                Text("${hv.netAvailableCapacityKwh} / ${hv.grossCapacityKwh} kWh", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(strings.cellDeltaLabel, color = theme.textSecondary, fontSize = 10.sp, maxLines = 1)
                                Text("${hv.cellDeltaMv} mV", color = deltaColor, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(strings.insulationResistanceLabel, color = theme.textSecondary, fontSize = 10.sp, maxLines = 1)
                                Text("${hv.insulationResistanceKOhm} kΩ", color = StatusGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        // 96-Cell distribution sample visualization
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = strings.hvCellDistributionLabel.format(hv.minCellVoltageMv, hv.avgCellVoltageMv, hv.maxCellVoltageMv),
                                    color = theme.textSecondary,
                                    fontSize = 10.sp
                                )
                                Text("Delta: ${hv.cellDeltaMv} mV", color = deltaColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(theme.cardElevated)
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                for (cell in hv.cellVoltages.take(24)) {
                                    val hFrac = ((cell.voltageMv - (hv.minCellVoltageMv - 5)) / (hv.cellDeltaMv + 10f).coerceAtLeast(1f)).coerceIn(0.2f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(hFrac)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(deltaColor.copy(alpha = 0.85f))
                                    )
                                }
                            }
                        }

                        // Temp & Health Assessment
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = strings.moduleTempsLabel.format(hv.minModuleTempC, hv.maxModuleTempC, hv.avgModuleTempC),
                                color = theme.textSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = if (hv.contactorClosed) strings.contactorClosedLabel else strings.contactorOpenLabel,
                                color = if (hv.contactorClosed) StatusGreen else StatusAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = GlobalTextLocalizer.localize(hv.healthAssessment, strings.language),
                            color = theme.primaryBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Service Routines (UDS 0x31)
        item {
            Text(
                text = strings.serviceRoutinesTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
        }

        session.lastServiceResult?.let { res ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = (if (res.success) StatusGreen else StatusRed).copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, if (res.success) StatusGreen else StatusRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (res.success) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (res.success) StatusGreen else StatusRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${res.title} • ${res.timestamp}",
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = res.message,
                                color = if (res.success) StatusGreen else StatusRed,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
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
                    BmwPrimaryButton(
                        text = strings.runRoutineBtn,
                        icon = Icons.Default.Build,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (routine.type == ServiceRoutineType.BATTERY_REGISTRATION) {
                                batteryDialogRoutine = routine
                            } else if (rememberRoutineConfirmation) {
                                onExecuteRoutine(routine, 0x01)
                            } else {
                                pendingRoutineToExecute = routine to 0x01
                            }
                        }
                    )
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
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = sootColor, modifier = Modifier.size(18.dp))
                                Text(
                                    text = strings.dpfMonitorTitle,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (dpf.isRegenActive) StatusGreen.copy(alpha = 0.2f) else theme.cardElevated)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (dpf.isRegenActive) strings.dpfStatusActive else strings.dpfStatusIdle,
                                    color = if (dpf.isRegenActive) StatusGreen else theme.textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    softWrap = false,
                                    maxLines = 1
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

                        // DPF Stats 2x2 Grid (clean spacing, never collides)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(strings.dpfAshLabel, color = theme.textSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("%.1f g".format(dpf.ashMassGrams), color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(strings.dpfBackpressureLabel, color = theme.textSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("%.1f mbar".format(dpf.backpressureMbar), color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(strings.dpfLastRegenLabel, color = theme.textSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("%,d km".format(dpf.distanceSinceLastRegenKm), color = theme.primaryBright, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(strings.dpfPreTempLabel, color = theme.textSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("%d°C".format(dpf.exhaustTempPreDpfC), color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        // Live DPF Precondition Checklist
                        val coolantC = session.liveChannels.firstOrNull { it.id == "coolant" }?.currentValue ?: 88.0
                        val hasCriticalEngineDtc = session.dtcs.any {
                            it.ecuAddress == EcuAddress.DME_DDE && it.severity == DtcSeverity.CRITICAL
                        }
                        val preconditions = listOf(
                            "${strings.dpfCondCoolant} (${coolantC.toInt()}°C)" to (coolantC > 75.0),
                            "${strings.dpfCondExhaust} (${dpf.exhaustTempPreDpfC}°C)" to (dpf.exhaustTempPreDpfC > 240),
                            "${strings.dpfCondVoltage} (${"%.1f".format(profile.batteryVoltage)} V)" to (profile.batteryVoltage >= 12.2),
                            strings.dpfCondNoCriticalDtc to !hasCriticalEngineDtc
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.cardElevated)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(strings.dpfPreconditionsTitle, color = theme.primaryBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            preconditions.forEach { (label, met) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(label, color = theme.textPrimary, fontSize = 11.sp)
                                    Text(
                                        text = if (met) "✓ OK" else "✕",
                                        color = if (met) StatusGreen else StatusRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Trigger Regen Button
                        val regenRoutine = session.serviceRoutines.firstOrNull { it.type == ServiceRoutineType.DPF_REGENERATION }
                        if (regenRoutine != null) {
                            BmwPrimaryButton(
                                text = strings.dpfTriggerBtn,
                                icon = Icons.Default.PlayArrow,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    if (rememberRoutineConfirmation) {
                                        onExecuteRoutine(regenRoutine, 0x01)
                                    } else {
                                        pendingRoutineToExecute = regenRoutine to 0x01
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // EPB Two-Step Service Mode Card (Retract / Clamp & Calibrate)
        item {
            val isEpbOpen = session.epbServiceState == EpbServiceState.SERVICE_MODE_OPEN
            val epbColor = if (isEpbOpen) StatusAmber else StatusGreen
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, epbColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.epbCardTitle,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(epbColor.copy(alpha = 0.16f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isEpbOpen) strings.epbStatusOpen else strings.epbStatusNormal,
                                color = epbColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onSetEpbServiceMode(true) },
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(strings.epbEnterBtn, fontSize = 11.sp, color = StatusAmber, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { onSetEpbServiceMode(false) },
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(strings.epbExitBtn, fontSize = 11.sp, color = StatusGreen, maxLines = 1)
                        }
                    }
                }
            }
        }

        // ADAS Camera & Radar Calibration (KAFAS & ACC)
        session.adasStatus?.let { adas ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, theme.outline),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(18.dp))
                                Text(
                                    text = strings.adasTitle,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusGreen.copy(alpha = 0.16f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (adas.isAccWithinTolerance) strings.radarOkLabel else strings.radarAlignRequiredLabel,
                                    color = if (adas.isAccWithinTolerance) StatusGreen else StatusAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    softWrap = false,
                                    maxLines = 1
                                )
                            }
                        }

                        // KAFAS Camera Progress
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(strings.kafasStatusLabel, color = theme.textSecondary, fontSize = 11.sp)
                                Text("${adas.kafasProgressPercent}%", color = theme.primaryBright, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            LinearProgressIndicator(
                                progress = { (adas.kafasProgressPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(5.dp),
                                color = theme.primaryBright,
                                trackColor = theme.outline.copy(alpha = 0.3f)
                            )
                            Text(GlobalTextLocalizer.localize(adas.kafasStatus, strings.language), color = theme.textSecondary, fontSize = 10.sp)
                        }

                        // ACC Radar Offsets
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(strings.accOffsetLabel, color = theme.textSecondary, fontSize = 10.sp)
                                Text("Yaw: %+1.1f mrad  |  Pitch: %+1.1f mrad".format(adas.accYawOffsetMrad, adas.accPitchOffsetMrad), color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Text(strings.toleranceLabel, color = StatusGreen, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }

                        // Action Buttons
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val kafasRoutine = session.serviceRoutines.firstOrNull { it.type == ServiceRoutineType.KAFAS_CAMERA_CALIBRATION }
                            if (kafasRoutine != null) {
                                OutlinedButton(
                                    onClick = { onExecuteRoutine(kafasRoutine, 0x01) },
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(kafasRoutine.localizedTitle(strings), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                }
                            }
                            val accRoutine = session.serviceRoutines.firstOrNull { it.type == ServiceRoutineType.ACC_RADAR_CALIBRATION }
                            if (accRoutine != null) {
                                OutlinedButton(
                                    onClick = { onExecuteRoutine(accRoutine, 0x01) },
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(accRoutine.localizedTitle(strings), fontSize = 11.sp, maxLines = 1, softWrap = false)
                                }
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
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = scoreColor, modifier = Modifier.size(18.dp))
                            Text(
                                text = strings.zfAdaptTitle,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(scoreColor.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$overallScore%",
                                color = scoreColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                softWrap = false,
                                maxLines = 1
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
                            Text(GlobalTextLocalizer.localize(c.clutchName, strings.language), color = theme.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
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
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(18.dp))
                        Text(strings.serviceHistoryTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                    }

                    BmwOutlinedButton(
                        text = strings.writeRecordBtn,
                        icon = Icons.Default.Add,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showHistoryDialog = true }
                    )
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
                            Text(
                                rec.performedItems.joinToString(" • ") { GlobalTextLocalizer.localize(it, strings.language) },
                                color = theme.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Exhaust Flap & Active Sound Design (ASD) Tuning Panel
        item {
            val sound = session.soundTuning
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(18.dp))
                        Text(strings.soundTuningTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                    }
                    Text(strings.exhaustFlapLabel, color = theme.textSecondary, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            ExhaustFlapMode.OEM_AUTO to strings.exhaustAutoLabel,
                            ExhaustFlapMode.FORCE_OPEN to strings.exhaustOpenLabel,
                            ExhaustFlapMode.FORCE_CLOSED to strings.exhaustClosedLabel
                        ).forEach { (mode, label) ->
                            FilterChip(
                                selected = sound.exhaustFlapMode == mode,
                                onClick = { onSetExhaustFlapMode(mode) },
                                label = { Text(label, fontSize = 11.sp, maxLines = 1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (sound.asdSupported) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.cardElevated)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.asdEnableLabel, color = theme.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("${strings.asdVolumeLabel}: ${sound.asdVolumePercent}%", color = theme.textSecondary, fontSize = 10.sp)
                            }
                            Switch(
                                checked = sound.asdEnabled,
                                onCheckedChange = { checked -> onSetAsdState(checked, sound.asdVolumePercent) }
                            )
                        }
                        if (sound.asdEnabled) {
                            Slider(
                                value = sound.asdVolumePercent.toFloat(),
                                onValueChange = { onSetAsdState(true, it.toInt()) },
                                valueRange = 20f..100f
                            )
                        }
                    }
                }
            }
        }

        // FRM / FEM / BDC Short-Circuit Lock Reset
        if (session.shortCircuitChannels.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, theme.outline),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(18.dp))
                            Text(strings.shortCircuitTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                        }
                        Text(strings.shortCircuitSub, color = theme.textSecondary, fontSize = 11.sp)
                        session.shortCircuitChannels.forEach { ch ->
                            val statusColor = if (ch.isLocked) StatusRed else if (ch.faultCounter > 0) StatusAmber else StatusGreen
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.cardElevated)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ch.channelName, color = theme.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "${ch.ecuName} • ${ch.faultCounter}/50 • ${if (ch.isLocked) strings.shortCircuitLockedLabel else strings.shortCircuitOkLabel}",
                                        color = statusColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (ch.isLocked || ch.faultCounter > 0) {
                                    OutlinedButton(
                                        onClick = { onResetShortCircuitChannel(ch) },
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(12.dp), tint = statusColor)
                                        Spacer(Modifier.width(4.dp))
                                        Text(strings.shortCircuitResetBtn, fontSize = 10.sp, color = statusColor)
                                    }
                                }
                            }
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
