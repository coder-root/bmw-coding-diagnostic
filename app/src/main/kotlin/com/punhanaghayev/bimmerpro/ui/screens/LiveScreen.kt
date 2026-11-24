package com.punhanaghayev.bimmerpro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.FilterChip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.punhanaghayev.bimmerpro.ui.components.MSportAccentStrip
import com.punhanaghayev.bimmerpro.ui.components.BmwDangerButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.bmw.BmwEcuAndPidCatalog
import com.punhanaghayev.bimmerpro.core.bmw.LiveSensorChannel
import com.punhanaghayev.bimmerpro.core.bmw.VirtualDynoResult
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import com.punhanaghayev.bimmerpro.data.DragMode
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusAmber
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveScreen(
    session: BimmerSessionState,
    isLiveStreamRunning: Boolean,
    availableLiveChannels: List<LiveSensorChannel>,
    onToggleLiveStream: () -> Unit,
    onTogglePerformanceRun: () -> Unit,
    onExportCsv: () -> Unit,
    onSetSelectedPids: (List<String>) -> Unit,
    onSetAndroidAutoPids: (List<String>) -> Unit,
    onMoveLiveChannel: (Int, Int) -> Unit,
    onSelectDragMode: (DragMode) -> Unit = {},
    onRunVirtualDynoPull: () -> Unit = {},
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
    var showAndroidAutoDialog by remember { mutableStateOf(false) }
    var draftSelectedPids by remember { mutableStateOf<Set<String>>(emptySet()) }
    var draftAutoPids by remember { mutableStateOf<List<String>>(emptyList()) }
    var isReorderMode by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(LiveViewMode.LIST) }
    var chartSelectedIds by remember(session.liveChannels.map { it.id }) {
        mutableStateOf(session.liveChannels.take(3).map { it.id }.toSet())
    }

    if (showAndroidAutoDialog) {
        if (draftAutoPids.isEmpty()) {
            draftAutoPids = session.androidAutoPidIds
        }
        AlertDialog(
            onDismissRequest = { showAndroidAutoDialog = false },
            title = {
                Text(strings.androidAutoSensorsTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = theme.textPrimary)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BmwEcuAndPidCatalog.livePidDefinitions.forEach { def ->
                        val selected = def.id in draftAutoPids
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    draftAutoPids = when {
                                        selected && draftAutoPids.size > 1 -> draftAutoPids - def.id
                                        !selected && draftAutoPids.size < 4 -> draftAutoPids + def.id
                                        !selected -> draftAutoPids.drop(1) + def.id
                                        else -> draftAutoPids
                                    }
                                }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selected,
                                onCheckedChange = { checked ->
                                    draftAutoPids = when {
                                        !checked && draftAutoPids.size > 1 -> draftAutoPids - def.id
                                        checked && draftAutoPids.size < 4 -> draftAutoPids + def.id
                                        checked -> draftAutoPids.drop(1) + def.id
                                        else -> draftAutoPids
                                    }
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(def.name, fontSize = 12.sp, color = theme.textPrimary, fontWeight = FontWeight.Medium)
                                Text(def.unit, fontSize = 10.sp, color = theme.textSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (draftAutoPids.isNotEmpty()) onSetAndroidAutoPids(draftAutoPids)
                        showAndroidAutoDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                ) {
                    Text(strings.applyBtn, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAndroidAutoDialog = false }) { Text(strings.cancelBtn) }
            }
        )
    }

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
                                        Text("DID 0x%04X • %s".format(def.did, def.unit), fontSize = 10.sp, color = theme.textSecondary)
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
                            text = "UDS 0x22 • ${session.engineDidProfile.ecuFamilyTag} (${session.engineDidProfile.displayName})",
                            color = theme.primaryBright,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isLiveStreamRunning) {
                            BmwDangerButton(
                                text = strings.stopBtn,
                                icon = Icons.Default.Stop,
                                modifier = Modifier.weight(1f),
                                onClick = onToggleLiveStream
                            )
                        } else {
                            BmwPrimaryButton(
                                text = strings.startBtn,
                                icon = Icons.Default.PlayArrow,
                                modifier = Modifier.weight(1f),
                                onClick = onToggleLiveStream
                            )
                        }

                        BmwOutlinedButton(
                            text = strings.exportCsvBtn,
                            icon = Icons.Default.Download,
                            modifier = Modifier.weight(1f),
                            onClick = onExportCsv
                        )
                    }
                }
            }
        }

        // Multi-Channel Live Oscilloscope (Sprint C)
        if (session.liveChannels.isNotEmpty()) {
            item {
                com.punhanaghayev.bimmerpro.ui.components.LiveOscilloscopeCard(
                    channels = session.liveChannels
                )
            }
        }

        // Extended Drag Performance Timer + Virtual Dyno (Pro-Tuner)
        item {
            val timer = session.performanceTimer
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, if (timer.isArmed) theme.primaryBright else theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(20.dp))
                            Text(
                                text = strings.perfTimerTitle,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "${timer.currentSpeedKmh} km/h" + if (timer.dragMode == DragMode.QUARTER_MILE) " • ${timer.distanceMeters.toInt()}m" else "",
                            color = theme.primaryBright,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(DragMode.ZERO_TO_100, DragMode.HUNDRED_TO_200).forEach { mode ->
                                FilterChip(
                                    selected = timer.dragMode == mode,
                                    onClick = { onSelectDragMode(mode) },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    label = {
                                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            Text(mode.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                                        }
                                    }
                                )
                            }
                        }
                        FilterChip(
                            selected = timer.dragMode == DragMode.QUARTER_MILE,
                            onClick = { onSelectDragMode(DragMode.QUARTER_MILE) },
                            modifier = Modifier.fillMaxWidth().height(38.dp),
                            label = {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text(DragMode.QUARTER_MILE.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(timer.dragMode.label, color = theme.textSecondary, fontSize = 11.sp)
                            val timeStr = if (timer.isRunning) "%.2fs".format(timer.elapsedMs / 1000.0)
                                else if (timer.isArmed) "..." else "--.--s"
                            Text(timeStr, color = theme.primaryBright, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(strings.bestTimerLabel, color = theme.textSecondary, fontSize = 11.sp)
                            val modeBest = when (timer.dragMode) {
                                DragMode.ZERO_TO_100 -> timer.bestZeroToHundredSec
                                DragMode.HUNDRED_TO_200 -> timer.best100To200Sec
                                DragMode.SIXTY_TO_130_MPH -> timer.best60To130MphSec
                                DragMode.QUARTER_MILE -> timer.bestQuarterMileSec
                            }
                            val bestStr = modeBest?.let { "%.2fs".format(it) } ?: "--.--s"
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

        // Pro-Tuner Virtual Dyno (HP & Nm Curve)
        item {
            VirtualDynoCard(
                dyno = session.virtualDyno,
                onRunDynoPull = onRunVirtualDynoPull
            )
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
                        val powerLabel = when (strings.language) {
                            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Power"
                            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Güc"
                            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Мощность"
                            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> "Güç"
                        }
                        val torqueLabel = when (strings.language) {
                            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Torque"
                            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Fırlanma anı"
                            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Крутящий момент"
                            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> "Tork"
                        }
                        LiveSportGauge(title = powerLabel, value = "$estHp", unit = "HP", fraction = (estHp / 320f).coerceIn(0f, 1f), modifier = Modifier.weight(1f))
                        LiveSportGauge(title = torqueLabel, value = "$estNm", unit = "Nm", fraction = (estNm / 450f).coerceIn(0f, 1f), modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Live Sensors Toolbar (Android Auto & Customize Modules below title with equal width)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${strings.tabLive} (${session.liveChannels.size})",
                        color = theme.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BmwOutlinedButton(
                        text = strings.androidAutoSensorsBtn,
                        icon = Icons.Default.DirectionsCar,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            draftAutoPids = session.androidAutoPidIds
                            showAndroidAutoDialog = true
                        }
                    )
                    BmwOutlinedButton(
                        text = strings.customizeSensorsBtn,
                        icon = Icons.Default.DashboardCustomize,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            draftSelectedPids = session.liveChannels.map { it.id }.toSet()
                            showCustomizeSensorsDialog = true
                        }
                    )
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

        // View Mode Switcher: List / Chart / Dashboard
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = viewMode == LiveViewMode.LIST,
                    onClick = { viewMode = LiveViewMode.LIST },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = null, modifier = Modifier.size(15.dp)) },
                    label = { Text(strings.liveModeListLabel, fontSize = 11.sp, maxLines = 1) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = viewMode == LiveViewMode.CHART,
                    onClick = { viewMode = LiveViewMode.CHART },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, modifier = Modifier.size(15.dp)) },
                    label = { Text(strings.liveModeChartLabel, fontSize = 11.sp, maxLines = 1) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = viewMode == LiveViewMode.DASHBOARD,
                    onClick = { viewMode = LiveViewMode.DASHBOARD },
                    leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(15.dp)) },
                    label = { Text(strings.liveModeDashboardLabel, fontSize = 11.sp, maxLines = 1) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when (viewMode) {
            LiveViewMode.CHART -> {
                item {
                    LiveTelemetryChartCard(
                        channels = session.liveChannels,
                        selectedIds = chartSelectedIds,
                        onToggleChannel = { id ->
                            chartSelectedIds = if (id in chartSelectedIds && chartSelectedIds.size > 1) {
                                chartSelectedIds - id
                            } else if (chartSelectedIds.size < 4) {
                                chartSelectedIds + id
                            } else {
                                chartSelectedIds.drop(1).toSet() + id
                            }
                        }
                    )
                }
            }

            LiveViewMode.DASHBOARD -> {
                val rows = session.liveChannels.chunked(2)
                itemsIndexed(rows, key = { rowIdx, pair -> "dash-$rowIdx-${pair.first().id}" }) { _, pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { ch ->
                            DashboardArcGaugeCard(
                                channel = ch,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            LiveViewMode.LIST -> {
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
    }
}

private enum class LiveViewMode {
    LIST,
    CHART,
    DASHBOARD
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LiveTelemetryChartCard(
    channels: List<LiveSensorChannel>,
    selectedIds: Set<String>,
    onToggleChannel: (String) -> Unit
) {
    val theme = LocalThemeStyle.current
    val strings = LocalAppStrings.current
    val palette = listOf(theme.primaryBright, StatusGreen, StatusAmber, StatusRed)
    val plotted = channels.filter { it.id in selectedIds }.take(4)

    Card(
        colors = CardDefaults.cardColors(containerColor = theme.card),
        border = BorderStroke(1.dp, theme.outline),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = strings.liveChartSelectHint,
                color = theme.textSecondary,
                fontSize = 11.sp
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.cardElevated)
                    .padding(12.dp)
            ) {
                val w = size.width
                val h = size.height
                val gridColor = theme.outline.copy(alpha = 0.35f)
                for (i in 0..4) {
                    val y = h * (i / 4f)
                    drawLine(gridColor, start = Offset(0f, y), end = Offset(w, y), strokeWidth = 1f)
                }

                plotted.forEachIndexed { idx, channel ->
                    val pts = channel.history
                    if (pts.size < 2) return@forEachIndexed
                    val span = (channel.maxExpected - channel.minExpected).takeIf { it > 0.0 } ?: 1.0
                    val lineColor = palette[idx % palette.size]
                    val path = Path()
                    pts.forEachIndexed { pIdx, rawVal ->
                        val x = w * (pIdx.toFloat() / (pts.size - 1).coerceAtLeast(1))
                        val norm = ((rawVal - channel.minExpected) / span).toFloat().coerceIn(0.02f, 0.98f)
                        val y = h * (1f - norm)
                        if (pIdx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, color = lineColor, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
                }
            }

            // Legend for active plotted channels
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                plotted.forEachIndexed { idx, ch ->
                    val color = palette[idx % palette.size]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(color)
                            )
                            Text(ch.localizedName(strings), color = theme.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Text(
                            text = "%.1f %s".format(ch.currentValue, ch.unit),
                            color = color,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Channel Selector List (stacked vertically one below the other)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                channels.forEach { ch ->
                    val active = ch.id in selectedIds
                    FilterChip(
                        selected = active,
                        onClick = { onToggleChannel(ch.id) },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(ch.localizedName(strings), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("%.1f %s".format(ch.currentValue, ch.unit), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardArcGaugeCard(
    channel: LiveSensorChannel,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeStyle.current
    val strings = LocalAppStrings.current
    val span = (channel.maxExpected - channel.minExpected).takeIf { it > 0.0 } ?: 1.0
    val fraction = ((channel.currentValue - channel.minExpected) / span).toFloat().coerceIn(0f, 1f)
    val gaugeColor = when {
        fraction >= 0.9f -> StatusRed
        fraction >= 0.75f -> StatusAmber
        else -> theme.primaryBright
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = theme.card),
        border = BorderStroke(1.dp, theme.outline),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.size(108.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                    val strokePx = 10.dp.toPx()
                    val arcSize = Size(size.width, size.height)
                    drawArc(
                        color = theme.outline.copy(alpha = 0.35f),
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round),
                        size = arcSize
                    )
                    drawArc(
                        color = gaugeColor,
                        startAngle = 150f,
                        sweepAngle = 240f * fraction,
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round),
                        size = arcSize
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.1f".format(channel.currentValue),
                        color = theme.textPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp
                    )
                    Text(
                        text = channel.unit,
                        color = theme.textSecondary,
                        fontSize = 10.sp
                    )
                }
            }
            Text(
                text = channel.localizedName(strings),
                color = theme.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                maxLines = 1
            )
            Text(
                text = "%.0f – %.0f %s".format(channel.minExpected, channel.maxExpected, channel.unit),
                color = theme.textSecondary,
                fontSize = 9.sp
            )
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

@Composable
private fun VirtualDynoCard(
    dyno: VirtualDynoResult?,
    onRunDynoPull: () -> Unit
) {
    val theme = LocalThemeStyle.current
    val strings = LocalAppStrings.current

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
                Text(strings.virtualDynoTitle, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = onRunDynoPull,
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(strings.virtualDynoPullBtn, fontSize = 11.sp, color = theme.primaryBright, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            if (dyno != null && dyno.points.size >= 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${dyno.peakHp} HP @ ${dyno.peakHpRpm} rpm",
                        color = theme.primaryBright,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${dyno.peakTorqueNm} Nm @ ${dyno.peakTorqueRpm} rpm",
                        color = StatusAmber,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.cardElevated)
                        .padding(10.dp)
                ) {
                    val pts = dyno.points
                    val maxVal = maxOf(
                        pts.maxOf { it.horsepower },
                        pts.maxOf { it.torqueNm },
                        100.0
                    )
                    val gridColor = theme.outline.copy(alpha = 0.25f)
                    for (i in 1..3) {
                        val y = size.height * i / 4f
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    }

                    val hpPath = Path()
                    val nmPath = Path()
                    val denom = (pts.size - 1).coerceAtLeast(1)
                    pts.forEachIndexed { idx, pt ->
                        val x = size.width * idx / denom.toFloat()
                        val hpY = size.height - ((pt.horsepower / maxVal).toFloat().coerceIn(0f, 1f) * size.height)
                        val nmY = size.height - ((pt.torqueNm / maxVal).toFloat().coerceIn(0f, 1f) * size.height)
                        if (idx == 0) {
                            hpPath.moveTo(x, hpY)
                            nmPath.moveTo(x, nmY)
                        } else {
                            hpPath.lineTo(x, hpY)
                            nmPath.lineTo(x, nmY)
                        }
                    }
                    drawPath(hpPath, color = theme.primaryBright, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
                    drawPath(nmPath, color = StatusAmber, style = Stroke(width = 3f, cap = StrokeCap.Round))
                }
                Text(
                    text = "${dyno.summary} • ${dyno.vehicleMassKg} kg",
                    color = theme.textSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
