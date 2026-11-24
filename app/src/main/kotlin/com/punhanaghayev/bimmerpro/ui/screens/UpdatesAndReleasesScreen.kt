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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.BuildConfig
import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.core.update.AppReleaseInfo
import com.punhanaghayev.bimmerpro.core.update.DataPackStatus
import com.punhanaghayev.bimmerpro.core.update.OtaCheckState
import com.punhanaghayev.bimmerpro.core.update.OtaUpdateManager
import com.punhanaghayev.bimmerpro.ui.components.BmwOutlinedButton
import com.punhanaghayev.bimmerpro.ui.components.BmwPrimaryButton
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesAndReleasesScreen(
    otaState: OtaCheckState,
    dataPackStatus: DataPackStatus = DataPackStatus.NotInstalled,
    dtcDictionaryCount: Int = 0,
    onCheckOta: () -> Unit,
    onDownloadOta: (AppReleaseInfo) -> Unit,
    onDownloadDataPack: () -> Unit = {},
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val theme = LocalThemeStyle.current

    val title = when (strings.language) {
        AppLanguage.EN -> "Update & Releases"
        AppLanguage.AZ -> "Yeniləmələr və Buraxılışlar"
        AppLanguage.RU -> "Обновления и релизы"
        AppLanguage.TR -> "Güncelleme & Sürümler"
    }

    var changelogExpanded by remember { mutableStateOf(false) }
    val changelogList = remember(otaState) {
        when (otaState) {
            is OtaCheckState.UpToDate -> otaState.releases.ifEmpty { OtaUpdateManager.builtinChangelog }
            is OtaCheckState.UpdateAvailable -> otaState.releases.ifEmpty { OtaUpdateManager.builtinChangelog }
            else -> OtaUpdateManager.builtinChangelog
        }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.textPrimary)
                }
                Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // OTA Update Card
        item {
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
                            Text("v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})", color = theme.textSecondary, fontSize = 12.sp)
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
                                    Text("${otaState.latest.tagName} Ready", color = StatusGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                BmwPrimaryButton(
                                    text = "OTA (${otaState.latest.tagName})",
                                    icon = Icons.Default.CloudDownload,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { onDownloadOta(otaState.latest) }
                                )
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

                    BmwOutlinedButton(
                        text = strings.checkUpdatesBtn,
                        icon = Icons.Default.Refresh,
                        enabled = otaState !is OtaCheckState.Checking && otaState !is OtaCheckState.Downloading,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onCheckOta
                    )
                }
            }
        }

        // Offline DTC Data Pack Card (moved from Settings)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${strings.dataPackTitle} ($dtcDictionaryCount)",
                        color = theme.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    BmwOutlinedButton(
                        text = strings.dataPackDownloadBtn,
                        onClick = onDownloadDataPack,
                        icon = Icons.Default.CloudDownload,
                        enabled = dataPackStatus !is DataPackStatus.Downloading,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                        modifier = Modifier.fillMaxWidth()
                    )
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
                                    text = { Text("${rel.tagName} • ${rel.publishedDate.substringBefore(" ")}", color = theme.textPrimary) },
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
