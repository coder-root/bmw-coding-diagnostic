package com.punhanaghayev.bimmerpro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.bmw.VirtualDynoResult
import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import com.punhanaghayev.bimmerpro.data.DragMode
import com.punhanaghayev.bimmerpro.ui.components.BmwOutlinedButton
import com.punhanaghayev.bimmerpro.ui.components.BmwPrimaryButton
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusAmber
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen

@Composable
fun PerformanceDynoScreen(
    session: BimmerSessionState,
    onTogglePerformanceRun: () -> Unit,
    onSelectDragMode: (DragMode) -> Unit,
    onRunVirtualDynoPull: () -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val theme = LocalThemeStyle.current

    val screenTitle = when (strings.language) {
        AppLanguage.EN -> "Performance & Dyno"
        AppLanguage.AZ -> "Performans və Dyno"
        AppLanguage.RU -> "Динамика и Dyno"
        AppLanguage.TR -> "Performans & Dyno"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.textPrimary)
                }
                Icon(Icons.Default.Speed, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(screenTitle, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // 1. 0-100 / 100-200 / 0-400m Performance Timer
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
                            Text(strings.perfTimerTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
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

        // 2. Pro-Tuner Virtual Dyno (HP & Nm Curve)
        item {
            PerformanceVirtualDynoCard(
                dyno = session.virtualDyno,
                onRunDynoPull = onRunVirtualDynoPull
            )
        }

        // 3. M-Sport Live Gauges & Power Estimation
        item {
            val rpmVal = session.liveChannels.firstOrNull { it.id == "rpm" }?.currentValue ?: 780.0
            val boostVal = session.liveChannels.firstOrNull { it.id == "boost_press" || it.id == "boost" }?.currentValue ?: 0.25
            val estHp = ((rpmVal / 6500.0) * 245.0 * (1.0 + boostVal.coerceAtLeast(0.0) * 0.45)).toInt().coerceAtLeast(20)
            val estNm = (260 + (boostVal.coerceAtLeast(0.0) * 180)).toInt()
            val powerLabel = when (strings.language) {
                AppLanguage.EN -> "Power"
                AppLanguage.AZ -> "Güc"
                AppLanguage.RU -> "Мощность"
                AppLanguage.TR -> "Güç"
            }
            val torqueLabel = when (strings.language) {
                AppLanguage.EN -> "Torque"
                AppLanguage.AZ -> "Fırlanma anı"
                AppLanguage.RU -> "Крутящий момент"
                AppLanguage.TR -> "Tork"
            }

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
                        PerformanceSportGauge(title = powerLabel, value = "$estHp", unit = "HP", fraction = (estHp / 320f).coerceIn(0f, 1f), modifier = Modifier.weight(1f))
                        PerformanceSportGauge(title = torqueLabel, value = "$estNm", unit = "Nm", fraction = (estNm / 450f).coerceIn(0f, 1f), modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceSportGauge(
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
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = theme.primaryBright,
                trackColor = theme.outline.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun PerformanceVirtualDynoCard(
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
            Text(strings.virtualDynoTitle, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)

            if (dyno != null && dyno.points.size >= 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${dyno.peakHp} HP @ ${dyno.peakHpRpm} rpm", color = theme.primaryBright, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    Text("${dyno.peakTorqueNm} Nm @ ${dyno.peakTorqueRpm} rpm", color = StatusAmber, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
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
                    val maxVal = maxOf(pts.maxOf { it.horsepower }, pts.maxOf { it.torqueNm }, 100.0)
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
                Text("${dyno.summary} • ${dyno.vehicleMassKg} kg", color = theme.textSecondary, fontSize = 11.sp)
            } else {
                val dynoPrompt = when (strings.language) {
                    AppLanguage.EN -> "Run a full-throttle 3rd/4th gear pull to plot HP & Nm dyno curve."
                    AppLanguage.AZ -> "HP və Nm qrafikini çəkmək üçün 3-cü/4-cü pillədə tam qaz ölçümü başladın."
                    AppLanguage.RU -> "Выполните разгон на 3/4 передаче для построения кривой мощности и момента."
                    AppLanguage.TR -> "HP ve Nm eğrisini çizmek için 3./4. viteste tam gaz ölçümü başlatın."
                }
                Text(dynoPrompt, color = theme.textSecondary, fontSize = 11.sp)
            }

            BmwOutlinedButton(
                text = strings.virtualDynoPullBtn,
                icon = Icons.Default.Speed,
                modifier = Modifier.fillMaxWidth(),
                onClick = onRunDynoPull
            )
        }
    }
}
