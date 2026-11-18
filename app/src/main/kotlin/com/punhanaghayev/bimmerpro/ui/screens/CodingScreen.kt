package com.punhanaghayev.bimmerpro.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.bmw.CafdParameter
import com.punhanaghayev.bimmerpro.core.bmw.CodingPreset
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.data.BimmerRepository
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import com.punhanaghayev.bimmerpro.ui.theme.BimmerCard
import com.punhanaghayev.bimmerpro.ui.theme.BimmerCardElevated
import com.punhanaghayev.bimmerpro.ui.theme.BimmerOutline
import com.punhanaghayev.bimmerpro.ui.theme.BmwBlue
import com.punhanaghayev.bimmerpro.ui.theme.BmwBlueBright
import com.punhanaghayev.bimmerpro.ui.theme.StatusAmber
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed
import com.punhanaghayev.bimmerpro.ui.theme.TextPrimary
import com.punhanaghayev.bimmerpro.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CodingScreen(
    session: BimmerSessionState,
    onApplyPreset: (String, Int) -> Unit,
    onApplyCafd: (String, String) -> Unit,
    onRestoreBackup: (String) -> Unit,
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
    var newVoCode by remember { mutableStateOf("") }
    var newVoDesc by remember { mutableStateOf("") }

    val voltageSafe = profile.batteryVoltage >= BimmerRepository.MIN_SAFE_CODING_VOLTAGE
    val strings = LocalAppStrings.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Safety Banner
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
            containerColor = BimmerCard,
            contentColor = BmwBlueBright
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
                    items(session.presets, key = { it.id }) { preset ->
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
                            label = { Text("CAFD / Fonksiyon Ara (örn. BMW_LOGO, MSA)") },
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
                            colors = CardDefaults.cardColors(containerColor = BimmerCard),
                            border = BorderStroke(1.dp, BimmerOutline)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Araç Sipariş Kodları (FA / VO)", fontWeight = FontWeight.Bold, color = TextPrimary)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    profile.faOptions.forEach { opt ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(BimmerCardElevated)
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("${opt.code} • ${opt.description}", color = TextPrimary, fontSize = 11.sp)
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = newVoCode,
                                        onValueChange = { newVoCode = it },
                                        label = { Text("Kod (6NS)") },
                                        singleLine = true,
                                        modifier = Modifier.weight(0.35f)
                                    )
                                    OutlinedTextField(
                                        value = newVoDesc,
                                        onValueChange = { newVoDesc = it },
                                        label = { Text("Açıklama") },
                                        singleLine = true,
                                        modifier = Modifier.weight(0.65f)
                                    )
                                }
                                Button(
                                    onClick = {
                                        onAddVoOption(newVoCode, newVoDesc)
                                        newVoCode = ""
                                        newVoDesc = ""
                                    },
                                    enabled = voltageSafe && newVoCode.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = BmwBlue)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("VO Opsiyonu Ekle & ECU Kodla")
                                }
                            }
                        }
                    }

                    item {
                        Text("Otomatik Kodlama Yedekleri (${session.backups.size})", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }

                    if (session.backups.isEmpty()) {
                        item {
                            Text("Henüz kodlama yapılmadı. Her yazma işleminde buraya geri yükleme noktası eklenir.", color = TextSecondary, fontSize = 12.sp)
                        }
                    } else {
                        items(session.backups, key = { it.id }) { backup ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BimmerCard),
                                border = BorderStroke(1.dp, BimmerOutline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${backup.ecuName} • ${backup.timestamp}", color = BmwBlueBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(backup.summary, color = TextPrimary, fontSize = 12.sp)
                                    }
                                    TextButton(onClick = { onRestoreBackup(backup.id) }) {
                                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Geri Al")
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
    Card(
        colors = CardDefaults.cardColors(containerColor = BimmerCard),
        border = BorderStroke(1.dp, if (preset.isLockedBySecureCoding) StatusAmber.copy(alpha = 0.5f) else BimmerOutline),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${preset.targetEcu} • ${preset.category.label}", color = BmwBlueBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                if (preset.isLockedBySecureCoding) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Secure Coding 2.0", color = StatusAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(preset.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(preset.description, color = TextSecondary, fontSize = 12.sp)
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
    Card(
        colors = CardDefaults.cardColors(containerColor = BimmerCard),
        border = BorderStroke(1.dp, BimmerOutline)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${param.ecuName} • ${param.cafdFile}", color = BmwBlueBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Offset 0x%02X | Mask 0x%02X".format(param.byteOffset, param.bitMask), color = TextSecondary, fontSize = 10.sp)
            }
            Text("${param.section} -> ${param.functionName}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text("Mevcut Değer: ${param.selectedOptionLabel}", color = StatusGreen, fontSize = 12.sp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                param.availableOptions.keys.forEach { optionLabel ->
                    OutlinedButton(
                        onClick = { if (enabled) onApply(optionLabel) },
                        border = BorderStroke(
                            1.dp,
                            if (param.selectedOptionLabel == optionLabel) BmwBlueBright else BimmerOutline
                        )
                    ) {
                        Text(optionLabel, fontSize = 11.sp, color = TextPrimary)
                    }
                }
            }
        }
    }
}
