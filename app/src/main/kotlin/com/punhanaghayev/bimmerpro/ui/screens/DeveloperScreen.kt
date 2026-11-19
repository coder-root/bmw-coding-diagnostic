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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.punhanaghayev.bimmerpro.ui.theme.AppThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(
    protocolLogs: List<ProtocolLogEntry>,
    otaState: OtaCheckState,
    currentLanguage: AppLanguage,
    currentTheme: AppThemeStyle,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectTheme: (AppThemeStyle) -> Unit,
    onCheckOta: () -> Unit,
    onDownloadOta: (AppReleaseInfo) -> Unit,
    onClearLogs: () -> Unit
) {
    val strings = LocalAppStrings.current
    val theme = LocalThemeStyle.current
    var langExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }

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
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.primary.copy(alpha = 0.7f)),
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
                            color = theme.textPrimary
                        )
                        Text(
                            text = "Lead Architect",
                            color = theme.primaryBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "BMW Diagnostics Developer",
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                        Row(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(theme.cardElevated)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = theme.primaryBright,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "cwghost@hotmail.com",
                                color = theme.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "BMW Expert PRO v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                            color = theme.textSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    MSportAccentStrip()
                }
            }
        }

        // Language & Multi-Theme Dropdown Settings Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = langExpanded,
                        onExpandedChange = { langExpanded = !langExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${currentLanguage.flagCode} • ${currentLanguage.nativeName}",
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            label = { Text(strings.languageTitle) },
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = theme.primaryBright) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = langExpanded,
                            onDismissRequest = { langExpanded = false },
                            modifier = Modifier.background(theme.cardElevated)
                        ) {
                            AppLanguage.entries.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text("${lang.flagCode} • ${lang.nativeName}", color = theme.textPrimary) },
                                    onClick = {
                                        onSelectLanguage(lang)
                                        langExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = themeExpanded,
                        onExpandedChange = { themeExpanded = !themeExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentTheme.displayName,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            label = { Text(strings.themeTitle) },
                            leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, tint = theme.primaryBright) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = themeExpanded,
                            onDismissRequest = { themeExpanded = false },
                            modifier = Modifier.background(theme.cardElevated)
                        ) {
                            AppThemeStyle.entries.forEach { style ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(style.primaryBright)
                                            )
                                            Text(style.displayName, color = theme.textPrimary)
                                        }
                                    },
                                    onClick = {
                                        onSelectTheme(style)
                                        themeExpanded = false
                                    }
                                )
                            }
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

        item {
            Text(
                text = strings.changelogTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
        }

        items(changelogList, key = { it.tagName }) { release ->
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
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
                                .background(theme.primary.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(release.tagName, color = theme.primaryBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(release.publishedDate, color = theme.textSecondary, fontSize = 11.sp)
                    }
                    Text(release.title, color = theme.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(release.changelog, color = theme.textSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.supportedArchTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                    BmwChassis.entries.forEachIndexed { idx, chassis ->
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(theme.primary.copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(chassis.code, color = theme.primaryBright, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Text(chassis.modelRange, color = theme.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(
                                text = chassis.family.protocolStack,
                                color = theme.textSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                        if (idx < BmwChassis.entries.lastIndex) {
                            HorizontalDivider(color = theme.outline.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Clean Live HSFZ / UDS Packet Inspector Header & Logs
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.packetInspectorTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary, fontSize = 14.sp)
                            Text(strings.packetInspectorSub, color = theme.textSecondary, fontSize = 11.sp)
                        }
                        if (protocolLogs.isNotEmpty()) {
                            OutlinedButton(
                                onClick = onClearLogs,
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(strings.clearBtn, color = theme.primaryBright, fontSize = 11.sp, maxLines = 1, softWrap = false)
                            }
                        }
                    }

                    if (protocolLogs.isEmpty()) {
                        Text(
                            text = "Henüz paket trafiği yok. Garaj sekmesinden bir araç simülasyonu veya vLinker / ENET bağlantısı başlatın.",
                            color = theme.textSecondary,
                            fontSize = 12.sp
                        )
                    } else {
                        protocolLogs.take(30).forEach { entry ->
                            val dirColor = when {
                                entry.isError -> StatusRed
                                entry.direction == "TX" -> theme.primaryBright
                                else -> StatusGreen
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.cardElevated)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "[${entry.direction}] 0x%02X".format(entry.ecuAddress),
                                        color = dirColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(entry.timestamp, color = theme.textSecondary, fontSize = 10.sp)
                                }
                                Text(entry.summary, color = theme.textPrimary, fontSize = 11.sp)
                                if (entry.rawHex.isNotBlank()) {
                                    Text(
                                        text = entry.rawHex,
                                        color = theme.textSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
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
    val theme = LocalThemeStyle.current
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.card),
        border = BorderStroke(1.dp, theme.outline),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(strings.otaTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                Text(
                    text = "Repo: github.com/${OtaUpdateManager.GITHUB_OWNER}/${OtaUpdateManager.GITHUB_REPO}",
                    color = theme.textSecondary,
                    fontSize = 11.sp
                )
            }

            when (otaState) {
                OtaCheckState.Idle -> {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    )
                }
                OtaCheckState.Checking -> {
                    Text("GitHub Releases...", color = theme.primaryBright, fontSize = 12.sp)
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
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
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
                        Text("v${otaState.version}: ${(otaState.progress * 100).toInt()}%", color = theme.primaryBright, fontSize = 12.sp)
                        LinearProgressIndicator(progress = { otaState.progress }, modifier = Modifier.fillMaxWidth(), color = theme.primaryBright)
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
