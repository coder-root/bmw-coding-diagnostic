package com.punhanaghayev.bimmerpro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import com.punhanaghayev.bimmerpro.ui.theme.BmwBlue
import com.punhanaghayev.bimmerpro.ui.theme.BmwBlueBright
import com.punhanaghayev.bimmerpro.ui.theme.BmwMotorsportDarkBlue
import com.punhanaghayev.bimmerpro.ui.theme.BmwMotorsportRed
import com.punhanaghayev.bimmerpro.ui.theme.StatusAmber
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed

@Composable
fun BmwRoundelLogo(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val diameter = this.size.minDimension
        val radius = diameter / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        // Outer metallic chrome bezel
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFF8FAFC), Color(0xFF94A3B8), Color(0xFFE2E8F0)),
                start = Offset.Zero,
                end = Offset(diameter, diameter)
            ),
            radius = radius,
            center = center
        )

        // Classic black outer ring
        drawCircle(
            color = Color(0xFF0D1117),
            radius = radius * 0.94f,
            center = center
        )

        // Inner silver ring border
        drawCircle(
            color = Color(0xFFE2E8F0),
            radius = radius * 0.67f,
            center = center
        )

        val quadRadius = radius * 0.63f
        val topLeft = Offset(center.x - quadRadius, center.y - quadRadius)
        val arcSize = Size(quadRadius * 2f, quadRadius * 2f)

        // Top-Left: Bavarian Blue
        drawArc(
            color = BmwBlue,
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = topLeft,
            size = arcSize
        )
        // Top-Right: White
        drawArc(
            color = Color.White,
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = topLeft,
            size = arcSize
        )
        // Bottom-Right: Bavarian Blue
        drawArc(
            color = BmwBlue,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = topLeft,
            size = arcSize
        )
        // Bottom-Left: White
        drawArc(
            color = Color.White,
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = topLeft,
            size = arcSize
        )

        // Cross-hair dividers
        val lineStroke = (diameter * 0.025f).coerceAtLeast(1.5f)
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(center.x - quadRadius, center.y),
            end = Offset(center.x + quadRadius, center.y),
            strokeWidth = lineStroke
        )
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(center.x, center.y - quadRadius),
            end = Offset(center.x, center.y + quadRadius),
            strokeWidth = lineStroke
        )

        // Minimal 12, 3, 6, 9 metallic accents on black ring
        val tickOuter = radius * 0.89f
        val tickInner = radius * 0.76f
        val tickStroke = (diameter * 0.03f).coerceAtLeast(1.5f)
        drawLine(Color(0xFFE2E8F0), Offset(center.x, center.y - tickOuter), Offset(center.x, center.y - tickInner), tickStroke)
        drawLine(Color(0xFFE2E8F0), Offset(center.x, center.y + tickInner), Offset(center.x, center.y + tickOuter), tickStroke)
        drawLine(Color(0xFFE2E8F0), Offset(center.x - tickOuter, center.y), Offset(center.x - tickInner, center.y), tickStroke)
        drawLine(Color(0xFFE2E8F0), Offset(center.x + tickInner, center.y), Offset(center.x + tickOuter, center.y), tickStroke)
    }
}

@Composable
fun MSportAccentStrip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
    ) {
        Box(modifier = Modifier.weight(1f).height(3.dp).background(BmwBlueBright))
        Box(modifier = Modifier.weight(1f).height(3.dp).background(BmwMotorsportDarkBlue))
        Box(modifier = Modifier.weight(1f).height(3.dp).background(BmwMotorsportRed))
    }
}

@Composable
fun BatterySafetyPill(voltage: Double, modifier: Modifier = Modifier) {
    val color = when {
        voltage >= 12.4 -> StatusGreen
        voltage >= 12.2 -> StatusAmber
        else -> StatusRed
    }
    val statusText = when {
        voltage >= 12.4 -> "GÜVENLİ"
        voltage >= 12.2 -> "DÜŞÜK"
        else -> "KODLAMA KİLİTLİ"
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "%.1f V • %s".format(voltage, statusText),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TelemetrySparkline(
    points: List<Float>,
    lineColor: Color = BmwBlueBright,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val minVal = points.minOrNull() ?: 0f
        val maxVal = points.maxOrNull() ?: 1f
        val range = (maxVal - minVal).takeIf { it > 0.001f } ?: 1f

        val stepX = size.width / (points.size - 1).coerceAtLeast(1)
        val path = Path()
        points.forEachIndexed { idx, value ->
            val x = idx * stepX
            val normalizedY = (value - minVal) / range
            val y = size.height - (normalizedY * (size.height * 0.78f)) - (size.height * 0.11f)
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun SportArcGauge(
    label: String,
    valueText: String,
    progress: Float,
    accentColor: Color = BmwBlueBright,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(104.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(96.dp)) {
            val stroke = 8.dp.toPx()
            drawArc(
                color = Color(0xFF2B394F),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = accentColor,
                startAngle = 140f,
                sweepAngle = 260f * progress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valueText, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, maxLines = 1)
            Text(label, color = Color(0xFF94A3B8), fontSize = 10.sp, maxLines = 1)
        }
    }
}
