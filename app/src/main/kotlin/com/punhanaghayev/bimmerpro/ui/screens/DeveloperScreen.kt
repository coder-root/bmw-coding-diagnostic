package com.punhanaghayev.bimmerpro.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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

@Composable
fun DeveloperScreen(
    protocolLogs: List<ProtocolLogEntry>,
    otaState: OtaCheckState,
    onCheckOta: () -> Unit,
    onDownloadOta: (AppReleaseInfo) -> Unit,
    onClearLogs: () -> Unit
) {
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            text = "Lead Architect & BMW Diagnostics Developer",
                            color = BmwBlueBright,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "BimmerPro v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE}) • ${BuildConfig.APPLICATION_ID}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    MSportAccentStrip()
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
                text = "Sürüm Geçmişi (Changelog)",
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

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BimmerCard),
                border = BorderStroke(1.dp, BimmerOutline)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Desteklenen BMW Mimarisi & Protokol Yığını", fontWeight = FontWeight.Bold, color = TextPrimary)
                    BmwChassis.entries.forEach { chassis ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${chassis.code} • ${chassis.modelRange}", color = TextPrimary, fontSize = 12.sp)
                            Text(chassis.family.protocolStack, color = BmwBlueBright, fontSize = 11.sp)
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
                Column {
                    Text("Canlı HSFZ / UDS Paket İnceleyici", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Gerçek zamanlı TX / RX hex çerçeveleri", color = TextSecondary, fontSize = 11.sp)
                }
                if (protocolLogs.isNotEmpty()) {
                    TextButton(onClick = onClearLogs) {
                        Text("Temizle", color = BmwBlueBright)
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
                        text = "Henüz paket trafiği yok. Garaj sekmesinden bir araç simülasyonu veya ENET bağlantısı başlatın.",
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
    Card(
        colors = CardDefaults.cardColors(containerColor = BimmerCard),
        border = BorderStroke(1.dp, BimmerOutline),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Versiyon Takibi & OTA Güncelleme", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        text = "Repo: github.com/${OtaUpdateManager.GITHUB_OWNER}/${OtaUpdateManager.GITHUB_REPO}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                OutlinedButton(
                    onClick = onCheckOta,
                    enabled = otaState !is OtaCheckState.Checking && otaState !is OtaCheckState.Downloading
                ) {
                    if (otaState is OtaCheckState.Checking) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("Kontrol Et", fontSize = 12.sp)
                }
            }

            when (otaState) {
                OtaCheckState.Idle -> {
                    Text(
                        text = "Mevcut sürüm: v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                OtaCheckState.Checking -> {
                    Text("GitHub Releases üzerinden yeni sürüm kontrol ediliyor...", color = BmwBlueBright, fontSize = 12.sp)
                }
                is OtaCheckState.UpToDate -> {
                    Text("Güncelsiniz: v${otaState.currentVersion} en son yayınlanan sürümdür.", color = StatusGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                is OtaCheckState.UpdateAvailable -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NewReleases, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Yeni Sürüm Mevcut: ${otaState.latest.tagName}",
                                color = StatusGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Button(
                            onClick = { onDownloadOta(otaState.latest) },
                            colors = ButtonDefaults.buttonColors(containerColor = BmwBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("OTA İndir & Yükle (${otaState.latest.tagName})", color = Color.White)
                        }
                    }
                }
                is OtaCheckState.Downloading -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("v${otaState.version} indiriliyor: ${(otaState.progress * 100).toInt()}%", color = BmwBlueBright, fontSize = 12.sp)
                        LinearProgressIndicator(progress = { otaState.progress }, modifier = Modifier.fillMaxWidth(), color = BmwBlueBright)
                    }
                }
                is OtaCheckState.ReadyToInstall -> {
                    Text("v${otaState.version} indirildi ve yükleme ekranı açıldı.", color = StatusGreen, fontSize = 12.sp)
                }
                is OtaCheckState.Error -> {
                    Text(otaState.message, color = StatusRed, fontSize = 12.sp)
                }
            }
        }
    }
}
