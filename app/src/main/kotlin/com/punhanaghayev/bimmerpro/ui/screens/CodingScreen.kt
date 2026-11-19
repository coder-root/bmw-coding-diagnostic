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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.bmw.CafdParameter
import com.punhanaghayev.bimmerpro.core.bmw.CodingCategory
import com.punhanaghayev.bimmerpro.core.bmw.CodingPreset
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.data.BimmerRepository
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusAmber
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CodingScreen(
    session: BimmerSessionState,
    onApplyPreset: (String, Int) -> Unit,
    onApplyCafd: (String, String) -> Unit,
    onRestoreBackup: (String) -> Unit,
    onExportBackupsJson: () -> Unit,
    onImportBackupsJson: () -> Unit,
    onAddVoOption: (String, String) -> Unit,
    onGoToGarage: () -> Unit
) {
    val profile = session.vehicleProfile
    if (profile == null) {
        DisconnectedPlaceholder(
            title = "Kodlama Motoru Beklemede",
            subtitle = "Easy Mode, Expert CAFD/FDL ve FA/VO kodlama için önce araca bağlanın.",
            onGoToGarage = onGoToGarage
        )
        return
    }

    var selectedMode by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var easySearchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CodingCategory?>(null) }
    var newVoCode by remember { mutableStateOf("") }
    var newVoDesc by remember { mutableStateOf("") }

    val voltageSafe = profile.batteryVoltage >= BimmerRepository.MIN_SAFE_CODING_VOLTAGE
    val strings = LocalAppStrings.current
    val theme = LocalThemeStyle.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background((if (voltageSafe) StatusGreen else StatusRed).copy(alpha = 0.14f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                tint = if (voltageSafe) StatusGreen else StatusRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (voltageSafe) {
                    strings.batterySafeBanner.format(profile.batteryVoltage)
                } else {
                    strings.batteryLockedBanner.format(profile.batteryVoltage)
                },
                color = if (voltageSafe) StatusGreen else StatusRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        PrimaryTabRow(
            selectedTabIndex = selectedMode,
            containerColor = theme.card,
            contentColor = theme.primaryBright
        ) {
            Tab(selected = selectedMode == 0, onClick = { selectedMode = 0 }, text = { Text(strings.codingEasyMode, fontSize = 12.sp, maxLines = 1) })
            Tab(selected = selectedMode == 1, onClick = { selectedMode = 1 }, text = { Text(strings.codingExpertCafd, fontSize = 12.sp, maxLines = 1) })
            Tab(selected = selectedMode == 2, onClick = { selectedMode = 2 }, text = { Text(strings.codingFaVoBackup, fontSize = 12.sp, maxLines = 1) })
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedMode) {
                0 -> {
                    item {
                        OutlinedTextField(
                            value = easySearchQuery,
                            onValueChange = { easySearchQuery = it },
                            placeholder = { Text("Kodlama ara...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            textStyle = TextStyle(fontSize = 13.sp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { selectedCategory = null },
                                    label = { Text("Tümü", fontSize = 11.sp) }
                                )
                            }
                            items(CodingCategory.entries) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                                    label = { Text(cat.label, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                    val filteredPresets = session.presets.filter { preset ->
                        (selectedCategory == null || preset.category == selectedCategory) &&
                            (easySearchQuery.isBlank() ||
                                preset.title.contains(easySearchQuery, ignoreCase = true) ||
                                preset.description.contains(easySearchQuery, ignoreCase = true) ||
                                preset.targetEcu.contains(easySearchQuery, ignoreCase = true))
                    }
                    items(filteredPresets, key = { it.id }) { preset ->
                        PresetCodingCard(preset = preset, enabled = voltageSafe, onSelectOption = { idx ->
                            onApplyPreset(preset.id, idx)
                        })
                    }
                }

                1 -> {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("CAFD / Fonksiyon ara (BMW_LOGO, MSA)", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            textStyle = TextStyle(fontSize = 13.sp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    val filtered = session.cafdParameters.filter {
                        searchQuery.isBlank() ||
                            it.functionName.contains(searchQuery, ignoreCase = true) ||
                            it.ecuName.contains(searchQuery, ignoreCase = true) ||
                            it.cafdFile.contains(searchQuery, ignoreCase = true)
                    }
                    items(filtered, key = { it.id }) { param ->
                        ExpertCafdCard(param = param, enabled = voltageSafe, onApply = { optLabel ->
                            onApplyCafd(param.id, optLabel)
                        })
                    }
                }

                2 -> {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = theme.card),
                            border = BorderStroke(1.dp, theme.outline),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Araç Sipariş Kodları (FA / VO)", fontWeight = FontWeight.Bold, color = theme.textPrimary)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    profile.faOptions.forEach { opt ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(theme.cardElevated)
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("${opt.code} • ${opt.description}", color = theme.textPrimary, fontSize = 11.sp)
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = newVoCode,
                                        onValueChange = { newVoCode = it.uppercase().take(6) },
                                        label = { Text("Kod", fontSize = 12.sp, maxLines = 1) },
                                        placeholder = { Text("6NS", fontSize = 12.sp) },
                                        textStyle = TextStyle(fontSize = 13.sp),
                                        singleLine = true,
                                        modifier = Modifier.weight(0.36f)
                                    )
                                    OutlinedTextField(
                                        value = newVoDesc,
                                        onValueChange = { newVoDesc = it },
                                        label = { Text("Açıklama", fontSize = 12.sp, maxLines = 1) },
                                        placeholder = { Text("Enhanced BT", fontSize = 12.sp) },
                                        textStyle = TextStyle(fontSize = 13.sp),
                                        singleLine = true,
                                        modifier = Modifier.weight(0.64f)
                                    )
                                }
                                Button(
                                    onClick = {
                                        onAddVoOption(newVoCode, newVoDesc)
                                        newVoCode = ""
                                        newVoDesc = ""
                                    },
                                    enabled = voltageSafe && newVoCode.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("VO Opsiyonu Ekle & Kodla", maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onExportBackupsJson,
                                enabled = session.backups.isNotEmpty(),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(strings.exportJsonBtn, fontSize = 12.sp, maxLines = 1, softWrap = false)
                            }
                            OutlinedButton(
                                onClick = onImportBackupsJson,
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(strings.importJsonBtn, fontSize = 12.sp, maxLines = 1, softWrap = false)
                            }
                        }
                    }

                    item {
                        Text("Otomatik Kodlama Yedekleri (${session.backups.size})", color = theme.textSecondary, fontWeight = FontWeight.SemiBold)
                    }

                    if (session.backups.isEmpty()) {
                        item {
                            Text("Henüz kodlama yapılmadı. Her yazma işleminde buraya geri yükleme noktası eklenir.", color = theme.textSecondary, fontSize = 12.sp)
                        }
                    } else {
                        items(session.backups, key = { it.id }) { backup ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = theme.card),
                                border = BorderStroke(1.dp, theme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${backup.ecuName} • ${backup.timestamp}", color = theme.primaryBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(backup.summary, color = theme.textPrimary, fontSize = 12.sp)
                                    }
                                    TextButton(onClick = { onRestoreBackup(backup.id) }) {
                                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Geri Al", maxLines = 1, softWrap = false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetCodingCard(
    preset: CodingPreset,
    enabled: Boolean,
    onSelectOption: (Int) -> Unit
) {
    val theme = LocalThemeStyle.current
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.card),
        border = BorderStroke(1.dp, if (preset.isLockedBySecureCoding) StatusAmber.copy(alpha = 0.5f) else theme.outline),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${preset.targetEcu} • ${preset.category.label}", color = theme.primaryBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                if (preset.isLockedBySecureCoding) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Secure Coding 2.0", color = StatusAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(preset.title, color = theme.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(preset.description, color = theme.textSecondary, fontSize = 12.sp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                preset.options.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = preset.currentOptionIndex == idx,
                        onClick = { if (enabled) onSelectOption(idx) },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpertCafdCard(
    param: CafdParameter,
    enabled: Boolean,
    onApply: (String) -> Unit
) {
    val theme = LocalThemeStyle.current
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.card),
        border = BorderStroke(1.dp, theme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${param.ecuName} • ${param.cafdFile}", color = theme.primaryBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Offset 0x%02X | Mask 0x%02X".format(param.byteOffset, param.bitMask), color = theme.textSecondary, fontSize = 10.sp)
            }
            Text("${param.section} -> ${param.functionName}", color = theme.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text("Mevcut Değer: ${param.selectedOptionLabel}", color = StatusGreen, fontSize = 12.sp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                param.availableOptions.keys.forEach { optionLabel ->
                    OutlinedButton(
                        onClick = { if (enabled) onApply(optionLabel) },
                        border = BorderStroke(
                            1.dp,
                            if (param.selectedOptionLabel == optionLabel) theme.primaryBright else theme.outline
                        )
                    ) {
                        Text(optionLabel, fontSize = 11.sp, color = theme.textPrimary)
                    }
                }
            }
        }
    }
}
