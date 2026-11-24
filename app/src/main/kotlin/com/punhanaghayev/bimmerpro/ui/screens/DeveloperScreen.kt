package com.punhanaghayev.bimmerpro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage
import com.punhanaghayev.bimmerpro.core.i18n.GlobalTextLocalizer
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.core.update.AppReleaseInfo
import com.punhanaghayev.bimmerpro.core.update.DataPackStatus
import com.punhanaghayev.bimmerpro.core.update.OtaCheckState
import com.punhanaghayev.bimmerpro.ui.components.BmwOutlinedButton
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
    onOpenUpdatesAndReleases: () -> Unit = {},
    onOpenDeveloperInfo: () -> Unit = {},
    workshopProfile: com.punhanaghayev.bimmerpro.data.WorkshopProfile = com.punhanaghayev.bimmerpro.data.WorkshopProfile(),
    onSaveWorkshopProfile: (com.punhanaghayev.bimmerpro.data.WorkshopProfile) -> Unit = {}
) {
    val strings = LocalAppStrings.current
    val theme = LocalThemeStyle.current
    var themeExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Modern App Language Selector Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = theme.primaryBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = strings.languageTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                        }

                        val languagePairs = AppLanguage.entries.chunked(2)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            languagePairs.forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    pair.forEach { lang ->
                                        val isSelected = lang == currentLanguage
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { onSelectLanguage(lang) },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) {
                                                    theme.primary.copy(alpha = 0.16f)
                                                } else {
                                                    theme.cardElevated
                                                }
                                            ),
                                            border = BorderStroke(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) theme.primaryBright else theme.outline.copy(alpha = 0.6f)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(
                                                                if (isSelected) theme.primary else theme.card
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                                    ) {
                                                        Text(
                                                            text = lang.flagCode,
                                                            color = if (isSelected) Color.White else theme.primaryBright,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.ExtraBold
                                                        )
                                                    }
                                                    Text(
                                                        text = lang.nativeName,
                                                        color = theme.textPrimary,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = StatusGreen,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (pair.size == 1) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                    MSportAccentStrip()
                }
            }
        }

        // Compact App Theme Selector Card (Standard BmwCompactDropdown template)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    com.punhanaghayev.bimmerpro.ui.components.BmwCompactDropdown(
                        label = strings.themeTitle,
                        selectedLabel = currentTheme.displayName,
                        leadingIcon = Icons.Default.Palette,
                        items = AppThemeStyle.entries,
                        itemLabel = { it.displayName },
                        itemLeadingContent = { style ->
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(style.primaryBright)
                            )
                        },
                        onSelect = onSelectTheme
                    )
                }
            }
        }

        // Physical Safe Mode, Cloud DTC DataPack & Support Bundle Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        Text(strings.supportBundleTitle, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(strings.supportBundleSub, color = theme.textSecondary, fontSize = 11.sp)
                        BmwOutlinedButton(
                            text = strings.supportBundleBtn,
                            onClick = onExportSupportBundle,
                            icon = Icons.Default.Share,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
