package com.punhanaghayev.bimmerpro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.bmw.LiveSensorChannel
import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusAmber
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed

/**
 * Multi-channel real-time oscilloscope card overlaying up to 4 live telemetry channels
 * with normalized auto-scaling, gridlines, peak/min stats and interactive cursor scrubber.
 */
@Composable
fun LiveOscilloscopeCard(
    channels: List<LiveSensorChannel>,
    modifier: Modifier = Modifier
) {
    val active = channels.take(4)
    if (active.isEmpty()) return

    val theme = LocalThemeStyle.current
    val strings = LocalAppStrings.current
    val palette = listOf(theme.primaryBright, StatusGreen, StatusAmber, StatusRed)

    val maxSamples = active.maxOfOrNull { it.history.size } ?: 0
    var cursorFraction by remember { mutableFloatStateOf(1f) }
    var showOscilloscope by remember { mutableStateOf(true) }
    val cursorIndex = if (maxSamples > 1) ((maxSamples - 1) * cursorFraction).toInt().coerceIn(0, maxSamples - 1) else 0
    val showLabel = when (strings.language) {
        AppLanguage.EN -> "Show"
        AppLanguage.AZ -> "Göstər"
        AppLanguage.RU -> "Показать"
        AppLanguage.TR -> "Göster"
    }
    val hideLabel = when (strings.language) {
        AppLanguage.EN -> "Hide"
        AppLanguage.AZ -> "Gizlət"
        AppLanguage.RU -> "Скрыть"
        AppLanguage.TR -> "Gizle"
    }

    val mainTitle = when (strings.language) {
        AppLanguage.EN -> "Multi-Channel Oscilloscope"
        AppLanguage.AZ -> "Çoxkanallı Osiloskop"
        AppLanguage.RU -> "Многоканальный осциллограф"
        AppLanguage.TR -> "Çoklu Sensör Osiloskop Grafiği"
    }
    val subOverlayLabel = when (strings.language) {
        AppLanguage.EN -> "4x Overlay"
        AppLanguage.AZ -> "4x Qrafik"
        AppLanguage.RU -> "4 канала"
        AppLanguage.TR -> "4x Overlay"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.card),
        border = BorderStroke(1.dp, theme.outline),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showOscilloscope = !showOscilloscope },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = mainTitle,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "$subOverlayLabel • $maxSamples pts",
                        fontWeight = FontWeight.SemiBold,
                        color = theme.primaryBright,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.width(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(theme.cardElevated)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (showOscilloscope) hideLabel else showLabel,
                        color = theme.primaryBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        softWrap = false
                    )
                    Icon(
                        imageVector = if (showOscilloscope) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = theme.primaryBright,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(visible = showOscilloscope) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Oscilloscope Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.cardElevated)
                    .padding(6.dp)
            ) {
                val w = size.width
                val h = size.height
                val gridColor = Color.Gray.copy(alpha = 0.22f)

                // Horizontal & vertical grid lines
                for (i in 1..3) {
                    val y = h * i / 4f
                    drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                }
                for (i in 1..4) {
                    val x = w * i / 5f
                    drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                }

                // Draw each channel normalized to its min..max
                active.forEachIndexed { idx, ch ->
                    val pts = ch.history
                    if (pts.size < 2) return@forEachIndexed
                    val minV = ch.minExpected.coerceAtMost(pts.minOrNull()?.toDouble() ?: 0.0)
                    val maxV = ch.maxExpected.coerceAtLeast(pts.maxOrNull()?.toDouble() ?: 1.0)
                    val span = (maxV - minV).coerceAtLeast(0.01)
                    val path = Path()
                    pts.forEachIndexed { pIdx, v ->
                        val x = w * pIdx / (pts.size - 1).coerceAtLeast(1)
                        val norm = ((v.toDouble() - minV) / span).toFloat().coerceIn(0f, 1f)
                        val y = h - (norm * h)
                        if (pIdx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, color = palette[idx % palette.size], style = Stroke(width = 2.4f))
                }

                // Vertical cursor line
                if (maxSamples > 1) {
                    val cx = w * cursorFraction.coerceIn(0f, 1f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.55f),
                        start = Offset(cx, 0f),
                        end = Offset(cx, h),
                        strokeWidth = 1.5f
                    )
                }
            }

            // Cursor scrubber slider when history has multiple frames
            if (maxSamples > 2) {
                Slider(
                    value = cursorFraction,
                    onValueChange = { cursorFraction = it },
                    modifier = Modifier.fillMaxWidth().height(24.dp)
                )
            }

            // Color-coded legend showing channel name on top and value + peak cleanly underneath
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                active.forEachIndexed { idx, ch ->
                    val valueAtCursor = ch.history.getOrNull(cursorIndex)?.toDouble() ?: ch.currentValue
                    val peak = ch.history.maxOrNull()?.toDouble() ?: ch.currentValue
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.cardElevated)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(palette[idx % palette.size])
                            )
                            Text(
                                text = ch.localizedName(strings),
                                color = theme.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "%.1f %s (Max %.1f)".format(valueAtCursor, ch.unit, peak),
                            color = palette[idx % palette.size],
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 14.dp)
                        )
                    }
                }
            }
            }
            }
        }
    }
}
