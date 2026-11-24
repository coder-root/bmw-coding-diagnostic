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
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.BuildConfig
import com.punhanaghayev.bimmerpro.core.bmw.BmwChassis
import com.punhanaghayev.bimmerpro.core.bmw.BmwModelSupportCatalog
import com.punhanaghayev.bimmerpro.core.bmw.ModelSupportLevel
import com.punhanaghayev.bimmerpro.core.bmw.SeriesFamily
import com.punhanaghayev.bimmerpro.core.bmw.SupportedBmwModelEntry
import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.core.update.AppReleaseInfo
import com.punhanaghayev.bimmerpro.core.update.DataPackStatus
import com.punhanaghayev.bimmerpro.core.update.OtaCheckState
import com.punhanaghayev.bimmerpro.core.update.OtaUpdateManager
import com.punhanaghayev.bimmerpro.ui.components.BmwRoundelLogo
import com.punhanaghayev.bimmerpro.ui.components.MSportAccentStrip
import com.punhanaghayev.bimmerpro.ui.theme.AppThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusAmber
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(
    otaState: OtaCheckState,
    dataPackStatus: DataPackStatus,
    currentLanguage: AppLanguage,
    currentTheme: AppThemeStyle,
    isPhysicalSafeMode: Boolean,
    isPhysicalWriteUnlocked: Boolean,
    dtcDictionaryCount: Int,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectTheme: (AppThemeStyle) -> Unit,
    onCheckOta: () -> Unit,
    onDownloadOta: (AppReleaseInfo) -> Unit,
    onTogglePhysicalWriteUnlock: (Boolean) -> Unit,
    onDownloadDataPack: () -> Unit,
    onExportSupportBundle: () -> Unit,
    workshopProfile: com.punhanaghayev.bimmerpro.data.WorkshopProfile = com.punhanaghayev.bimmerpro.data.WorkshopProfile(),
    onSaveWorkshopProfile: (com.punhanaghayev.bimmerpro.data.WorkshopProfile) -> Unit = {}
) {
    val strings = LocalAppStrings.current
    val theme = LocalThemeStyle.current
    var langExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }
    var changelogExpanded by remember { mutableStateOf(false) }
    var archExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("TÃ¼mÃ¼") }

    val changelogList = when (otaState) {
        is OtaCheckState.UpToDate -> otaState.releases.ifEmpty { OtaUpdateManager.builtinChangelog }
        is OtaCheckState.UpdateAvailable -> otaState.releases.ifEmpty { OtaUpdateManager.builtinChangelog }
        else -> OtaUpdateManager.builtinChangelog
    }
    var selectedReleaseTag by remember(changelogList) {
        mutableStateOf(changelogList.firstOrNull()?.tagName ?: "v1.0.0")
    }
    val selectedRelease = changelogList.firstOrNull { it.tagName == selectedReleaseTag } ?: changelogList.first()

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

        // Physical Safe Mode & On-Demand DTC DataPack Card
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
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = strings.physicalSafeUnlockTitle,
                                color = if (isPhysicalWriteUnlocked) StatusRed else theme.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isPhysicalSafeMode && !isPhysicalWriteUnlocked) {
                                    strings.safeModeBannerLocked
                                } else if (isPhysicalWriteUnlocked) {
                                    strings.safeModeBannerUnlocked
                                } else {
                                    strings.disclaimerBullet1
                                },
                                color = theme.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = isPhysicalWriteUnlocked,
                            onCheckedChange = onTogglePhysicalWriteUnlock,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StatusRed,
                                uncheckedThumbColor = StatusAmber
                            )
                        )
                    }
                    HorizontalDivider(color = theme.outline.copy(alpha = 0.4f))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${strings.dataPackTitle} ($dtcDictionaryCount)",
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedButton(
                            onClick = onDownloadDataPack,
                            enabled = dataPackStatus !is DataPackStatus.Downloading,
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (dataPackStatus is DataPackStatus.Downloading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(strings.dataPackDownloadBtn, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                        }
                        val (statusText, statusColor) = when (dataPackStatus) {
                            DataPackStatus.NotInstalled -> strings.dataPackDownloadBtn to theme.textSecondary
                            DataPackStatus.Downloading -> strings.dataPackDownloading to theme.primaryBright
                            is DataPackStatus.Applied -> {
                                val dateClean = dataPackStatus.updatedAt.substringBefore(" ")
                                strings.dataPackApplied.format(dataPackStatus.packVersion, dataPackStatus.addedCount, dateClean) to StatusGreen
                            }
                            is DataPackStatus.UpToDate -> {
                                val dateClean = dataPackStatus.updatedAt.substringBefore(" ")
                                strings.dataPackUpToDate.format(dataPackStatus.packVersion, dateClean, dataPackStatus.totalCount) to StatusGreen
                            }
                            is DataPackStatus.Error -> dataPackStatus.message to StatusRed
                        }
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    HorizontalDivider(color = theme.outline.copy(alpha = 0.4f))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(strings.supportBundleTitle, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(strings.supportBundleSub, color = theme.textSecondary, fontSize = 11.sp)
                        OutlinedButton(
                            onClick = onExportSupportBundle,
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.supportBundleBtn, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }


        // Release History / Changelog Dropdown Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = strings.changelogTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                    ExposedDropdownMenuBox(
                        expanded = changelogExpanded,
                        onExpandedChange = { changelogExpanded = !changelogExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${selectedRelease.tagName} • ${selectedRelease.publishedDate.substringBefore(" ")}",
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            label = { Text(strings.selectVersionLabel) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = changelogExpanded) },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = changelogExpanded,
                            onDismissRequest = { changelogExpanded = false },
                            modifier = Modifier.background(theme.cardElevated)
                        ) {
                            changelogList.forEach { rel ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${rel.tagName} • ${rel.publishedDate.substringBefore(" ")}", color = theme.textPrimary)
                                    },
                                    onClick = {
                                        selectedReleaseTag = rel.tagName
                                        changelogExpanded = false
                                    }
                                )
                            }
                        }
                    }

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
                            Text(selectedRelease.tagName, color = theme.primaryBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(selectedRelease.publishedDate.substringBefore(" "), color = theme.textSecondary, fontSize = 11.sp)
                    }
                    Text(selectedRelease.title, color = theme.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(selectedRelease.changelog, color = theme.textSecondary, fontSize = 12.sp, lineHeight = 18.sp)
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
