package com.punhanaghayev.bimmerpro.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.ui.theme.BmwBlue
import com.punhanaghayev.bimmerpro.ui.theme.BmwBlueBright
import com.punhanaghayev.bimmerpro.ui.theme.BmwMotorsportDarkBlue
import com.punhanaghayev.bimmerpro.ui.theme.BmwMotorsportRed
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
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
    val strings = LocalAppStrings.current
    val color = when {
        voltage >= 12.4 -> StatusGreen
        voltage >= 12.2 -> StatusAmber
        else -> StatusRed
    }
    val statusText = when {
        voltage >= 12.4 -> strings.batterySafeLabel
        voltage >= 12.2 -> strings.batteryLowLabel
        else -> strings.batteryLockedLabel
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

enum class BadgeStatusType { OK, WARNING, ERROR, NEUTRAL }

@Composable
fun BmwStatusBadge(
    statusType: BadgeStatusType = BadgeStatusType.OK,
    count: Int? = null,
    text: String? = null,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeStyle.current
    val (badgeBg, contentColor) = when (statusType) {
        BadgeStatusType.OK -> StatusGreen.copy(alpha = 0.14f) to StatusGreen
        BadgeStatusType.WARNING -> StatusAmber.copy(alpha = 0.16f) to StatusAmber
        BadgeStatusType.ERROR -> StatusRed.copy(alpha = 0.16f) to StatusRed
        BadgeStatusType.NEUTRAL -> theme.cardElevated to theme.textSecondary
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeBg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (statusType) {
            BadgeStatusType.OK -> Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            BadgeStatusType.WARNING -> Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            BadgeStatusType.ERROR -> Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            BadgeStatusType.NEUTRAL -> Unit
        }

        val displayStr = when {
            text != null -> text
            count != null -> "$count"
            else -> ""
        }
        if (displayStr.isNotEmpty()) {
            Text(
                text = displayStr,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun BmwPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val theme = LocalThemeStyle.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = theme.primary,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun BmwOutlinedButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    borderColor: Color? = null,
    contentColor: Color? = null,
    onClick: () -> Unit
) {
    val theme = LocalThemeStyle.current
    val strokeColor = borderColor ?: theme.primaryBright.copy(alpha = 0.85f)
    val color = contentColor ?: theme.primaryBright
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 46.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, strokeColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun BmwSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val theme = LocalThemeStyle.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = theme.cardElevated,
            contentColor = theme.textPrimary
        ),
        border = BorderStroke(1.dp, theme.outline),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = theme.textPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, color = theme.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
fun BmwDangerButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = StatusRed,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun BmwSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val theme = LocalThemeStyle.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = theme.card),
        border = BorderStroke(1.dp, theme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = theme.textPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = theme.textSecondary,
                    lineHeight = 15.sp
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = theme.primaryBright,
                    uncheckedThumbColor = theme.textSecondary,
                    uncheckedTrackColor = theme.cardElevated
                )
            )
        }
    }
}
