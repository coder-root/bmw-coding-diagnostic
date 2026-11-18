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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.BuildConfig
import com.punhanaghayev.bimmerpro.core.bmw.BmwChassis
import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.core.protocol.ProtocolLogEntry
import com.punhanaghayev.bimmerpro.core.update.AppReleaseInfo
import com.punhanaghayev.bimmerpro.core.update.OtaCheckState
import com.punhanaghayev.bimmerpro.core.update.OtaUpdateManager
import com.punhanaghayev.bimmerpro.ui.components.BmwRoundelLogo
import com.punhanaghayev.bimmerpro.ui.components.MSportAccentStrip
import com.punhanaghayev.bimmerpro.ui.theme.BimmerCard
import com.punhanaghayev.bimmerpro.ui.theme.BimmerCardElevated
import com.punhanaghayev.bimmerpro.ui.theme.BimmerOutline
import com.punhanaghayev.bimmerpro.ui.theme.BmwBlue
import com.punhanaghayev.bimmerpro.ui.theme.BmwBlueBright
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed
import com.punhanaghayev.bimmerpro.ui.theme.TextPrimary
import com.punhanaghayev.bimmerpro.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeveloperScreen(
    protocolLogs: List<ProtocolLogEntry>,
    otaState: OtaCheckState,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onCheckOta: () -> Unit,
    onDownloadOta: (AppReleaseInfo) -> Unit,
    onClearLogs: () -> Unit
) {
    val strings = LocalAppStrings.current
    val changelogList = when (otaState) {
        is OtaCheckState.UpToDate -> otaState.releases
        is OtaCheckState.UpdateAvailable -> otaState.releases
        else -> OtaUpdateManager.builtinChangelog
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BimmerCard),
                border = BorderStroke(1.dp, BmwBlue.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BmwRoundelLogo(size = 76.dp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Punhan Aghayev",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Lead Architect",
                            color = BmwBlueBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "BMW Diagnostics Developer",
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                        Row(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(BimmerCardElevated)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = BmwBlueBright,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "cwghost@hotmail.com",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "BMW Expert PRO v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    MSportAccentStrip()
                }
            }
        }

        // Language Selection Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BimmerCard),
                border = BorderStroke(1.dp, BimmerOutline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = BmwBlueBright, modifier = Modifier.size(18.dp))
                        Text(strings.languageTitle, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = currentLanguage == lang,
                                onClick = { onSelectLanguage(lang) },
                                label = { Text("${lang.flagCode} • ${lang.nativeName}", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // OTA Update & Version Tracking Card
        item {
            OtaUpdateCard(
                otaState = otaState,
                onCheckOta = onCheckOta,
                onDownloadOta = onDownloadOta
            )
        }

        // Changelog Section
        item {
            Text(
                text = strings.changelogTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(changelogList, key = { it.tagName }) { release ->
            Card(
                colors = CardDefaults.cardColors(containerColor = BimmerCard),
                border = BorderStroke(1.dp, BimmerOutline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BmwBlue.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(release.tagName, color = BmwBlueBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(release.publishedDate, color = TextSecondary, fontSize = 11.sp)
                    }
                    Text(release.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(release.changelog, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }

        // Supported BMW Architecture (Stacked vertical layout - clean & readable)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BimmerCard),
                border = BorderStroke(1.dp, BimmerOutline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.supportedArchTitle, fontWeight = FontWeight.Bold, color = TextPrimary)
                    BmwChassis.entries.forEachIndexed { idx, chassis ->
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BmwBlue.copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(chassis.code, color = BmwBlueBright, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Text(chassis.modelRange, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(
                                text = chassis.family.protocolStack,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                        if (idx < BmwChassis.entries.lastIndex) {
                            HorizontalDivider(color = BimmerOutline.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.packetInspectorTitle, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(strings.packetInspectorSub, color = TextSecondary, fontSize = 11.sp)
                }
                if (protocolLogs.isNotEmpty()) {
                    TextButton(onClick = onClearLogs) {
                        Text(strings.clearBtn, color = BmwBlueBright, maxLines = 1, softWrap = false)
                    }
                }
            }
        }

        if (protocolLogs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BimmerCard),
                    border = BorderStroke(1.dp, BimmerOutline)
                ) {
                    Text(
                        text = "Henüz paket trafiği yok. Garaj sekmesinden bir araç simülasyonu veya vLinker / ENET bağlantısı başlatın.",
                        modifier = Modifier.padding(14.dp),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(protocolLogs) { entry ->
                val dirColor = when {
                    entry.isError -> StatusRed
                    entry.direction == "TX" -> BmwBlueBright
                    else -> StatusGreen
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BimmerCardElevated)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "[${entry.direction}] ECU 0x%02X • ${entry.summary}".format(entry.ecuAddress),
                                color = dirColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(entry.timestamp, color = TextSecondary, fontSize = 10.sp)
                        }
                        Text(
                            text = entry.rawHex,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OtaUpdateCard(
    otaState: OtaCheckState,
    onCheckOta: () -> Unit,
    onDownloadOta: (AppReleaseInfo) -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        colors = CardDefaults.cardColors(containerColor = BimmerCard),
        border = BorderStroke(1.dp, BimmerOutline),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(strings.otaTitle, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    text = "Repo: github.com/${OtaUpdateManager.GITHUB_OWNER}/${OtaUpdateManager.GITHUB_REPO}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            when (otaState) {
                OtaCheckState.Idle -> {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                OtaCheckState.Checking -> {
                    Text("GitHub Releases...", color = BmwBlueBright, fontSize = 12.sp)
                }
                is OtaCheckState.UpToDate -> {
                    Text("v${otaState.currentVersion} OK", color = StatusGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                is OtaCheckState.UpdateAvailable -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NewReleases, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${otaState.latest.tagName} Ready",
                                color = StatusGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Button(
                            onClick = { onDownloadOta(otaState.latest) },
                            colors = ButtonDefaults.buttonColors(containerColor = BmwBlue),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("OTA (${otaState.latest.tagName})", color = Color.White, maxLines = 1, softWrap = false)
                        }
                    }
                }
                is OtaCheckState.Downloading -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("v${otaState.version}: ${(otaState.progress * 100).toInt()}%", color = BmwBlueBright, fontSize = 12.sp)
                        LinearProgressIndicator(progress = { otaState.progress }, modifier = Modifier.fillMaxWidth(), color = BmwBlueBright)
                    }
                }
                is OtaCheckState.ReadyToInstall -> {
                    Text("v${otaState.version} ready to install.", color = StatusGreen, fontSize = 12.sp)
                }
                is OtaCheckState.Error -> {
                    Text(otaState.message, color = StatusRed, fontSize = 12.sp)
                }
            }

            OutlinedButton(
                onClick = onCheckOta,
                enabled = otaState !is OtaCheckState.Checking && otaState !is OtaCheckState.Downloading,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (otaState is OtaCheckState.Checking) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(strings.checkUpdatesBtn, fontSize = 13.sp, maxLines = 1, softWrap = false)
            }
        }
    }
}
