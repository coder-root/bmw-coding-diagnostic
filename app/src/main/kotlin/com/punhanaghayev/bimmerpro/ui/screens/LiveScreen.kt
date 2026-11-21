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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.punhanaghayev.bimmerpro.ui.components.MSportAccentStrip
import com.punhanaghayev.bimmerpro.ui.components.BmwPrimaryButton
import com.punhanaghayev.bimmerpro.ui.components.BmwOutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.bmw.BmwEcuAndPidCatalog
import com.punhanaghayev.bimmerpro.core.bmw.LiveSensorChannel
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusAmber
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed

@Composable
fun LiveScreen(
    session: BimmerSessionState,
    isLiveStreamRunning: Boolean,
    availableLiveChannels: List<LiveSensorChannel>,
    onToggleLiveStream: () -> Unit,
    onTogglePerformanceRun: () -> Unit,
    onExportCsv: () -> Unit,
    onSetSelectedPids: (List<String>) -> Unit,
    onMoveLiveChannel: (Int, Int) -> Unit,
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

    var showCustomizeSensorsDialog by remember { mutableStateOf(false) }
    var draftSelectedPids by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isReorderMode by remember { mutableStateOf(false) }

    if (showCustomizeSensorsDialog) {
        if (draftSelectedPids.isEmpty()) {
            draftSelectedPids = session.liveChannels.map { it.id }.toSet().ifEmpty {
                setOf("rpm", "speed", "boost", "coolant", "oil_temp", "batt")
            }
        }
        val ecuNames = remember { BmwEcuAndPidCatalog.allBmwEcus.associateBy { it.address } }
        val catalogIds = remember(availableLiveChannels) { availableLiveChannels.map { it.id }.toSet() }
        val groupedPids = remember(catalogIds) {
            BmwEcuAndPidCatalog.livePidDefinitions
                .filter { catalogIds.isEmpty() || it.id in catalogIds }
                .groupBy { it.ecuAddress }
        }

        AlertDialog(
            onDismissRequest = { showCustomizeSensorsDialog = false },
            title = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.customizeSensorsTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = theme.textPrimary
                        )
                        IconButton(onClick = { showCustomizeSensorsDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textSecondary)
                        }
                    }
                    Text(
                        text = "${draftSelectedPids.size} / ${BmwEcuAndPidCatalog.livePidDefinitions.size} selected",
                        fontSize = 11.sp,
                        color = theme.textSecondary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MSportAccentStrip()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                draftSelectedPids = BmwEcuAndPidCatalog.livePidDefinitions.map { it.id }.toSet()
                            },
                            modifier = Modifier.weight(1f).height(32.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(strings.allModulesLabel, fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                draftSelectedPids = setOf("rpm", "speed", "boost", "coolant", "oil_temp", "batt")
                            },
                            modifier = Modifier.weight(1f).height(32.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Default (6)", fontSize = 11.sp)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groupedPids.forEach { (ecuAddr, defs) ->
                            val ecuTitle = ecuNames[ecuAddr]?.let { "${it.shortName} (0x%02X)".format(it.address) }
                                ?: "ECU 0x%02X".format(ecuAddr)
                            Text(ecuTitle, color = theme.primaryBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            defs.forEach { def ->
                                val checked = def.id in draftSelectedPids
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            draftSelectedPids = if (checked && draftSelectedPids.size > 1) {
                                                draftSelectedPids - def.id
                                            } else {
                                                draftSelectedPids + def.id
                                            }
                                        }
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { isChecked ->
                                            draftSelectedPids = if (!isChecked && draftSelectedPids.size > 1) {
                                                draftSelectedPids - def.id
                                            } else if (isChecked) {
                                                draftSelectedPids + def.id
                                            } else draftSelectedPids
                                        }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(def.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = theme.textPrimary)
                                        Text("DID 0x%04X • ${def.unit}".format(def.did), fontSize = 10.sp, color = theme.textSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentIds = session.liveChannels.map { it.id }
                        val preservedOrder = currentIds.filter { it in draftSelectedPids } +
                            BmwEcuAndPidCatalog.livePidDefinitions.map { it.id }.filter { it in draftSelectedPids && it !in currentIds }
                        if (preservedOrder.isNotEmpty()) {
                            onSetSelectedPids(preservedOrder)
                        }
                        showCustomizeSensorsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                ) {
                    Text(strings.applyBtn, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCustomizeSensorsDialog = false }) {
                    Text(strings.cancelBtn)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Telemetry Stream Header + Start & CSV Buttons below UDS 0x22 DID
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column {
                        Text(
                            text = strings.liveTelemetryTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = "UDS 0x22 DID Telemetry Stream",
                            color = theme.textSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onToggleLiveStream,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLiveStreamRunning) StatusRed else theme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if (isLiveStreamRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isLiveStreamRunning) strings.stopBtn else strings.startBtn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        OutlinedButton(
                            onClick = onExportCsv,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            border = BorderStroke(1.dp, theme.primaryBright.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                tint = theme.primaryBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = strings.exportCsvBtn,
                                color = theme.primaryBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        // 0-100 km/h Performance Timer
        item {
            val timer = session.performanceTimer
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, if (timer.isArmed) theme.primaryBright else theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(20.dp))
                        Text(
                            text = strings.perfTimerTitle,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary,
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("0-100 km/h", color = theme.textSecondary, fontSize = 11.sp)
                            val timeStr = if (timer.isRunning) "%.2fs".format(timer.elapsedMs / 1000.0)
                                else if (timer.isArmed) "..." else "--.--s"
                            Text(timeStr, color = theme.primaryBright, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(strings.bestTimerLabel, color = theme.textSecondary, fontSize = 11.sp)
                            val bestStr = timer.bestZeroToHundredSec?.let { "%.2fs".format(it) } ?: "--.--s"
                            Text(bestStr, color = StatusGreen, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        }
                    }

                    BmwPrimaryButton(
                        text = if (timer.isArmed) strings.stopBtn else strings.startBtn,
                        icon = if (timer.isArmed) Icons.Default.Stop else Icons.Default.PlayArrow,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onTogglePerformanceRun
                    )
                }
            }
        }

        // Sport Displays Gauges (Power & Torque)
        item {
            val rpmVal = session.liveChannels.firstOrNull { it.id == "rpm" }?.currentValue ?: 780.0
            val boostVal = session.liveChannels.firstOrNull { it.id == "boost_press" || it.id == "boost" }?.currentValue ?: 0.25
            val estHp = ((rpmVal / 6500.0) * 245.0 * (1.0 + boostVal.coerceAtLeast(0.0) * 0.45)).toInt().coerceAtLeast(20)
            val estNm = (260 + (boostVal.coerceAtLeast(0.0) * 180)).toInt()

            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(18.dp))
                        Text(strings.sportGaugeTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LiveSportGauge(title = "Güç / Power", value = "$estHp", unit = "HP", fraction = (estHp / 320f).coerceIn(0f, 1f), modifier = Modifier.weight(1f))
                        LiveSportGauge(title = "Tork / Torque", value = "$estNm", unit = "Nm", fraction = (estNm / 450f).coerceIn(0f, 1f), modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Live Sensors Toolbar (Customize & Reorder Mode)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${strings.tabLive} (${session.liveChannels.size})",
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            draftSelectedPids = session.liveChannels.map { it.id }.toSet()
                            showCustomizeSensorsDialog = true
                        },
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.DashboardCustomize, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(strings.customizeSensorsBtn, fontSize = 11.sp, maxLines = 1, softWrap = false)
                    }
                    OutlinedButton(
                        onClick = { isReorderMode = !isReorderMode },
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        border = BorderStroke(1.dp, if (isReorderMode) theme.primaryBright else theme.outline)
                    ) {
                        Icon(
                            if (isReorderMode) Icons.Default.Check else Icons.Default.SwapVert,
                            contentDescription = null,
                            tint = if (isReorderMode) theme.primaryBright else theme.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isReorderMode) strings.doneBtn else strings.editOrderBtn,
                            fontSize = 11.sp,
                            color = if (isReorderMode) theme.primaryBright else theme.textPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        if (isReorderMode) {
            item {
                Text(
                    text = strings.reorderHintLabel,
                    color = theme.primaryBright,
                    fontSize = 11.sp
                )
            }
        }

        // Sensor Cards List
        itemsIndexed(session.liveChannels, key = { _, ch -> ch.id }) { index, channel ->
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(channel.localizedName(strings), color = theme.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${channel.id.uppercase()} • ${channel.unit}", color = theme.textSecondary, fontSize = 10.sp)
                        }
                        if (isReorderMode) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (index > 0) onMoveLiveChannel(index, index - 1) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { if (index < session.liveChannels.size - 1) onMoveLiveChannel(index, index + 1) },
                                    enabled = index < session.liveChannels.size - 1,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", modifier = Modifier.size(18.dp))
                                }
                            }
                        } else {
                            Text(
                                text = "%.1f %s".format(channel.currentValue, channel.unit),
                                color = theme.primaryBright,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp
                            )
                        }
                    }
                    val fraction = if (channel.maxExpected > channel.minExpected) {
                        ((channel.currentValue - channel.minExpected) / (channel.maxExpected - channel.minExpected)).toFloat().coerceIn(0f, 1f)
                    } else 0f
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp),
                        color = theme.primaryBright,
                        trackColor = theme.outline.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveSportGauge(
    title: String,
    value: String,
    unit: String,
    fraction: Float,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeStyle.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(theme.cardElevated)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = theme.textSecondary, fontSize = 11.sp)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(value, color = theme.primaryBright, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                Text(unit, color = theme.textSecondary, fontSize = 11.sp, modifier = Modifier.padding(bottom = 2.dp))
            }
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = theme.primaryBright,
                trackColor = theme.outline.copy(alpha = 0.3f)
            )
        }
    }
}
