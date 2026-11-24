package com.punhanaghayev.bimmerpro.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import com.punhanaghayev.bimmerpro.ui.components.BmwDangerButton
import com.punhanaghayev.bimmerpro.ui.components.BmwOutlinedButton
import com.punhanaghayev.bimmerpro.ui.components.BmwSecondaryButton
import com.punhanaghayev.bimmerpro.ui.components.BmwStatusBadge
import com.punhanaghayev.bimmerpro.ui.components.BadgeStatusType
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.bmw.BmwDtcDictionary
import com.punhanaghayev.bimmerpro.core.bmw.DtcFault
import com.punhanaghayev.bimmerpro.core.bmw.DtcMemoryType
import com.punhanaghayev.bimmerpro.core.bmw.DtcSeverity
import com.punhanaghayev.bimmerpro.core.bmw.EcuModule
import com.punhanaghayev.bimmerpro.core.bmw.TamperStatus
import com.punhanaghayev.bimmerpro.core.i18n.GlobalTextLocalizer
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusAmber
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiagnosticsScreen(
    session: BimmerSessionState,
    onRunScan: () -> Unit,
    onClearAllDtcs: () -> Unit,
    onClearEcuDtcs: (Int, String) -> Unit,
    onSendEcuReset: (Int, String, Int) -> Unit,
    onFetchFreezeFrame: (DtcFault) -> Unit,
    onExportPdf: () -> Unit,
    onGoToGarage: () -> Unit
) {
    val profile = session.vehicleProfile
    val strings = LocalAppStrings.current
    val theme = LocalThemeStyle.current
    if (profile == null) {
        DisconnectedPlaceholder(
            title = strings.disconnectedDiagTitle,
            subtitle = strings.disconnectedDiagSub,
            onGoToGarage = onGoToGarage
        )
        return
    }

    var filterMode by remember { mutableStateOf(DtcFilterMode.ALL) }
    var selectedMemoryTab by remember { mutableStateOf<DtcMemoryType?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }
    var dtcLookupQuery by remember { mutableStateOf("") }
    var showOfflineHealth by remember { mutableStateOf(true) }
    var showRootCause by remember { mutableStateOf(true) }
    var showMileageCheck by remember { mutableStateOf(true) }
    val showLabel = when (strings.language) {
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Show"
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Göstər"
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Показать"
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> "Göster"
    }
    val hideLabel = when (strings.language) {
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Hide"
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Gizlət"
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Скрыть"
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> "Gizle"
    }
    val lookupResults = remember(dtcLookupQuery) { BmwDtcDictionary.search(dtcLookupQuery) }

    val filteredDtcs = remember(session.dtcs, filterMode, selectedMemoryTab) {
        session.dtcs.filter { dtc ->
            val memOk = selectedMemoryTab == null || dtc.memoryType == selectedMemoryTab
            val sevOk = when (filterMode) {
                DtcFilterMode.ALL -> true
                DtcFilterMode.ACTIVE_AND_WARN -> dtc.severity != DtcSeverity.SHADOW
                DtcFilterMode.SHADOW_ONLY -> dtc.severity == DtcSeverity.SHADOW
            }
            memOk && sevOk
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(strings.clearConfirmTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.clearConfirmBody) },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    onClearAllDtcs()
                }) {
                    Text(strings.clearConfirmAction, color = StatusRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text(strings.cancelBtn) }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (session.isScanning || session.scanStatusText.isNotBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, theme.outline)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(session.scanStatusText, color = theme.textPrimary, fontSize = 12.sp)
                        LinearProgressIndicator(
                            progress = { session.scanProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = theme.primaryBright
                        )
                    }
                }
            }
        }

        if (session.isPhysicalSafeMode) {
            item {
                val safeColor = if (session.isPhysicalWriteUnlocked) StatusRed else StatusAmber
                Card(
                    colors = CardDefaults.cardColors(containerColor = safeColor.copy(alpha = 0.14f)),
                    border = BorderStroke(1.dp, safeColor.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (session.isPhysicalWriteUnlocked) Icons.Default.Warning else Icons.Default.Shield,
                            contentDescription = null,
                            tint = safeColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (session.isPhysicalWriteUnlocked) {
                                strings.safeModeBannerUnlocked
                            } else {
                                strings.safeModeBannerLocked
                            },
                            color = safeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        val healthReport = session.offlineReport
        if (healthReport != null) {
            item {
                val overallColor = when {
                    healthReport.overallScore >= 85 -> StatusGreen
                    healthReport.overallScore >= 65 -> StatusAmber
                    else -> StatusRed
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, overallColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showOfflineHealth = !showOfflineHealth },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.offlineHealthTitle,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(overallColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${healthReport.overallScore} / 100",
                                    color = overallColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false
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
                                    text = if (showOfflineHealth) hideLabel else showLabel,
                                    color = theme.primaryBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    softWrap = false
                                )
                                Icon(
                                    imageVector = if (showOfflineHealth) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = theme.primaryBright,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        AnimatedVisibility(visible = showOfflineHealth) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                healthReport.subsystemScores.forEach { sub ->
                                    val subColor = when {
                                        sub.scorePercent >= 85 -> StatusGreen
                                        sub.scorePercent >= 65 -> StatusAmber
                                        else -> StatusRed
                                    }
                                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(
                                            text = sub.localizedName(strings),
                                            color = theme.textPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${sub.scorePercent}% • ${sub.localizedNote(strings)}",
                                            color = subColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        LinearProgressIndicator(
                                            progress = { (sub.scorePercent / 100f).coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp),
                                            color = subColor,
                                            trackColor = theme.outline.copy(alpha = 0.35f)
                                        )
                                    }
                                }
                                if (healthReport.recommendationItems.isNotEmpty()) {
                                    healthReport.recommendationItems.forEach { recItem ->
                                        Text("• ${recItem.localized(strings)}", color = theme.textSecondary, fontSize = 11.sp)
                                    }
                                } else {
                                    healthReport.recommendations.forEach { rec ->
                                        Text("• $rec", color = theme.textSecondary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (healthReport.rootCauseCorrelations.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusAmber.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, StatusAmber.copy(alpha = 0.55f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showRootCause = !showRootCause },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(18.dp))
                                    Text(strings.rootCauseTitle, color = StatusAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(Modifier.width(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(theme.card)
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (showRootCause) hideLabel else showLabel,
                                        color = StatusAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        softWrap = false
                                    )
                                    Icon(
                                        imageVector = if (showRootCause) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = StatusAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            AnimatedVisibility(visible = showRootCause) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    healthReport.rootCauseCorrelations.forEach { rc ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(theme.card)
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(rc.localizedTitle(strings), color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(
                                                text = rc.involvedCodes.joinToString(" + "),
                                                color = theme.primaryBright,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(rc.localizedSummary(strings), color = theme.textSecondary, fontSize = 11.sp)
                                            Text("${strings.rootCauseActionPrefix}: ${rc.localizedAction(strings)}", color = StatusGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val compDiff = session.comparativeDiff
        if (compDiff != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, theme.primaryBright.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(strings.comparativeDiffTitle, color = theme.primaryBright, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${compDiff.previousTimestamp} → ${compDiff.currentTimestamp}", color = theme.textSecondary, fontSize = 11.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${strings.comparativeKmDeltaLabel}: ${if (compDiff.kmDelta >= 0) "+" else ""}${compDiff.kmDelta} km", color = theme.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            val healthColor = if (compDiff.healthScoreDelta >= 0) StatusGreen else StatusRed
                            Text("${strings.comparativeHealthDeltaLabel}: ${if (compDiff.healthScoreDelta >= 0) "+" else ""}${compDiff.healthScoreDelta}", color = healthColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (compDiff.newlyAppearedDtcs.isNotEmpty()) {
                            Text("${strings.comparativeNewDtcLabel}: ${compDiff.newlyAppearedDtcs.joinToString(", ")}", color = StatusRed, fontSize = 11.sp)
                        }
                        if (compDiff.resolvedDtcs.isNotEmpty()) {
                            Text("${strings.comparativeResolvedDtcLabel}: ${compDiff.resolvedDtcs.joinToString(", ")}", color = StatusGreen, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            val mv = profile.mileageVerification
            val isTampered = mv.status == TamperStatus.SUSPICIOUS_MISMATCH
            val badgeColor = if (isTampered) StatusRed else StatusGreen
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMileageCheck = !showMileageCheck },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isTampered) Icons.Default.Warning else Icons.Default.Shield,
                                contentDescription = null,
                                tint = badgeColor
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(strings.mileageCheckTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 14.sp)
                                Text(mv.status.localized(strings), color = badgeColor, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
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
                                text = if (showMileageCheck) hideLabel else showLabel,
                                color = theme.primaryBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                softWrap = false
                            )
                            Icon(
                                imageVector = if (showMileageCheck) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = theme.primaryBright,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    AnimatedVisibility(visible = showMileageCheck) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(mv.localizedSummary(strings), color = theme.textSecondary, fontSize = 12.sp)
                            mv.readings.forEach { reading ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(reading.localizedModuleName(strings), color = theme.textSecondary, fontSize = 12.sp)
                                    Text(
                                        text = "%,d km".format(reading.mileageKm) + (reading.engineHours?.let { " • ${it}h" } ?: ""),
                                        color = theme.textPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            val errCount = session.dtcs.count { it.memoryType == DtcMemoryType.ERROR_MEMORY }
            val infoCount = session.dtcs.count { it.memoryType == DtcMemoryType.INFO_MEMORY }
            val permCount = session.dtcs.count { it.memoryType == DtcMemoryType.PERMANENT_MEMORY }
            var memoryDropdownExpanded by remember { mutableStateOf(false) }
            val currentMemoryLabel = when (selectedMemoryTab) {
                null -> "${strings.filterAll} (${session.dtcs.size})"
                DtcMemoryType.ERROR_MEMORY -> "${strings.dtcMemoryErrorLabel} ($errCount)"
                DtcMemoryType.INFO_MEMORY -> "${strings.dtcMemoryInfoLabel} ($infoCount)"
                DtcMemoryType.PERMANENT_MEMORY -> "${strings.dtcMemoryPermanentLabel} ($permCount)"
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { memoryDropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(currentMemoryLabel, fontSize = 12.sp, maxLines = 1, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(
                    expanded = memoryDropdownExpanded,
                    onDismissRequest = { memoryDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("${strings.filterAll} (${session.dtcs.size})", fontSize = 12.sp) },
                        onClick = {
                            selectedMemoryTab = null
                            memoryDropdownExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("${strings.dtcMemoryErrorLabel} ($errCount)", fontSize = 12.sp) },
                        onClick = {
                            selectedMemoryTab = DtcMemoryType.ERROR_MEMORY
                            memoryDropdownExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("${strings.dtcMemoryInfoLabel} ($infoCount)", fontSize = 12.sp) },
                        onClick = {
                            selectedMemoryTab = DtcMemoryType.INFO_MEMORY
                            memoryDropdownExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("${strings.dtcMemoryPermanentLabel} ($permCount)", fontSize = 12.sp) },
                        onClick = {
                            selectedMemoryTab = DtcMemoryType.PERMANENT_MEMORY
                            memoryDropdownExpanded = false
                        }
                    )
                }
            }
        }

        item {
            var filterDropdownExpanded by remember { mutableStateOf(false) }
            val currentFilterLabel = when (filterMode) {
                DtcFilterMode.ALL -> strings.filterAll
                DtcFilterMode.ACTIVE_AND_WARN -> strings.filterWarn
                DtcFilterMode.SHADOW_ONLY -> strings.filterShadow
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    OutlinedButton(
                        onClick = { filterDropdownExpanded = true },
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(currentFilterLabel, fontSize = 12.sp, maxLines = 1)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = filterDropdownExpanded,
                        onDismissRequest = { filterDropdownExpanded = false }
                    ) {
                        DtcFilterMode.entries.forEach { mode ->
                            val label = when (mode) {
                                DtcFilterMode.ALL -> strings.filterAll
                                DtcFilterMode.ACTIVE_AND_WARN -> strings.filterWarn
                                DtcFilterMode.SHADOW_ONLY -> strings.filterShadow
                            }
                            DropdownMenuItem(
                                text = { Text(label, fontSize = 12.sp) },
                                onClick = {
                                    filterMode = mode
                                    filterDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "${filteredDtcs.size} ${strings.statusFault}",
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (filteredDtcs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = strings.noDtcInFilter,
                        modifier = Modifier.padding(18.dp),
                        color = StatusGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            items(filteredDtcs, key = { it.id }) { dtc ->
                ExpandableDtcCard(
                    dtc = dtc,
                    onExpand = { onFetchFreezeFrame(dtc) }
                )
            }
        }

        item {
            Text(
                text = "${strings.scannedEcusTitle} (${session.ecus.size})",
                style = MaterialTheme.typography.titleSmall,
                color = theme.textSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(session.ecus, key = { it.address }) { ecu ->
            ExpandableEcuCard(
                ecu = ecu,
                onClearDtc = { onClearEcuDtcs(ecu.address, ecu.shortName) },
                onResetEcu = { onSendEcuReset(ecu.address, ecu.shortName, 0x01) }
            )
        }

        // BimmerTool-style Trouble Code Lookup
        item {
            Spacer(Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.dtcDictionaryTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 13.sp)
                    androidx.compose.material3.OutlinedTextField(
                        value = dtcLookupQuery,
                        onValueChange = { dtcLookupQuery = it },
                        placeholder = { Text(strings.dtcDictionaryHint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        textStyle = TextStyle(fontSize = 12.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    lookupResults.forEach { (code, entry) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.cardElevated)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text("$code • ${entry.title}", color = theme.primaryBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(entry.detail, color = theme.textSecondary, fontSize = 11.sp)
                            Text(entry.guidance, color = theme.textPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Action Buttons (Template style from Image 1)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BmwOutlinedButton(
                    text = strings.scanAllEcusBtn,
                    icon = Icons.Default.Refresh,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRunScan
                )
                BmwSecondaryButton(
                    text = strings.expertisePdfBtn,
                    icon = Icons.Default.PictureAsPdf,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onExportPdf
                )
                if (session.dtcs.isNotEmpty()) {
                    BmwDangerButton(
                        text = strings.clearAllDtcBtn,
                        icon = Icons.Default.DeleteSweep,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showClearDialog = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableEcuCard(
    ecu: EcuModule,
    onClearDtc: () -> Unit,
    onResetEcu: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val theme = LocalThemeStyle.current
    val strings = LocalAppStrings.current
    val total = ecu.activeDtcCount + ecu.shadowDtcCount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = theme.card),
        border = BorderStroke(1.dp, theme.outline),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${ecu.shortName} (${ecu.addressHex}) • ${GlobalTextLocalizer.localize(ecu.fullName, strings.language)}",
                        color = theme.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${ecu.softwareVersion}" +
                            (ecu.variantName?.let { " • $it" } ?: "") +
                            " | ${GlobalTextLocalizer.localize(ecu.codingCapability.label, strings.language)}",
                        color = theme.textSecondary,
                        fontSize = 11.sp
                    )
                }
                val isOffline = total == 0 && (ecu.softwareVersion.contains("error", ignoreCase = true) || ecu.hardwareVersion.contains("error", ignoreCase = true))
                if (isOffline) {
                    BmwStatusBadge(statusType = BadgeStatusType.ERROR, text = "link error —")
                } else if (total > 0) {
                    BmwStatusBadge(statusType = BadgeStatusType.WARNING, count = total)
                } else {
                    BmwStatusBadge(statusType = BadgeStatusType.OK, count = 0)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "HW: ${ecu.hardwareVersion} • CAFD: ${ecu.cafdId ?: "-"}" +
                            (ecu.reportedMileageKm?.let { " • %,d km".format(it) } ?: ""),
                        color = theme.textSecondary,
                        fontSize = 11.sp
                    )
                    if (!ecu.svkSummary.isNullOrBlank()) {
                        Text(
                            text = "SVK: ${ecu.svkSummary}",
                            color = theme.primaryBright,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onClearDtc,
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = StatusRed, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.clearEcuDtcBtn, fontSize = 11.sp, color = StatusRed, maxLines = 1, softWrap = false)
                        }
                        OutlinedButton(
                            onClick = onResetEcu,
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.ecuResetBtn, fontSize = 11.sp, color = theme.primaryBright, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableDtcCard(
    dtc: DtcFault,
    onExpand: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val theme = LocalThemeStyle.current
    val strings = LocalAppStrings.current
    val severityColor = when (dtc.severity) {
        DtcSeverity.CRITICAL -> StatusRed
        DtcSeverity.WARNING -> StatusAmber
        DtcSeverity.INFO -> theme.primaryBright
        DtcSeverity.SHADOW -> theme.textSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val next = !expanded
                expanded = next
                if (next) onExpand()
            },
        colors = CardDefaults.cardColors(containerColor = theme.card),
        border = BorderStroke(1.dp, severityColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(severityColor.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(dtc.code, color = severityColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    val memLabel = when (dtc.memoryType) {
                        DtcMemoryType.ERROR_MEMORY -> strings.dtcMemoryErrorLabel
                        DtcMemoryType.INFO_MEMORY -> strings.dtcMemoryInfoLabel
                        DtcMemoryType.PERMANENT_MEMORY -> strings.dtcMemoryPermanentLabel
                    }
                    Text("${dtc.ecuName} • ${dtc.status.localized(strings)} • $memLabel", color = theme.textSecondary, fontSize = 10.sp)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = theme.textSecondary
                )
            }
            Text(
                GlobalTextLocalizer.localize(dtc.localizedTitle(strings), strings.language),
                color = theme.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    Text(
                        GlobalTextLocalizer.localize(dtc.technicalDetail, strings.language),
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.cardElevated)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Freeze-Frame: %,d km | %.1f V | %d°C | %d rpm (${strings.freezeFrameOccurrences.format(dtc.freezeFrame.occurrenceCount)})".format(
                                dtc.freezeFrame.mileageKm,
                                dtc.freezeFrame.batteryVoltage,
                                dtc.freezeFrame.coolantTempC,
                                dtc.freezeFrame.engineRpm
                            ),
                            color = theme.textPrimary,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.primary.copy(alpha = 0.16f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(strings.istaControlPlan, color = theme.primaryBright, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(GlobalTextLocalizer.localize(dtc.istaGuidance, strings.language), color = theme.textPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisconnectedPlaceholder(
    title: String,
    subtitle: String,
    onGoToGarage: () -> Unit
) {
    val theme = LocalThemeStyle.current
    val strings = LocalAppStrings.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.card),
            border = BorderStroke(1.dp, theme.outline),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                Text(subtitle, color = theme.textSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Button(onClick = onGoToGarage, colors = ButtonDefaults.buttonColors(containerColor = theme.primary)) {
                    Text(strings.goToGarageBtn, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}

private enum class DtcFilterMode(val label: String) {
    ALL("Tümü"),
    ACTIVE_AND_WARN("Kritik / Uyarı"),
    SHADOW_ONLY("Gölge")
}
