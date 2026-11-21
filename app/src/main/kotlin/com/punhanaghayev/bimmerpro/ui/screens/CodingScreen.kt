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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.bmw.CafdParameter
import com.punhanaghayev.bimmerpro.core.bmw.CodingCategory
import com.punhanaghayev.bimmerpro.core.bmw.CodingPreset
import com.punhanaghayev.bimmerpro.core.bmw.VoDependencyMapper
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.data.BimmerRepository
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusAmber
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen
import com.punhanaghayev.bimmerpro.ui.theme.StatusRed

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CodingScreen(
    session: BimmerSessionState,
    onApplyPreset: (String, Int) -> Unit,
    onApplyCafd: (String, String) -> Unit,
    onApplyBatchCoding: (Map<String, Int>, Map<String, String>) -> Unit = { _, _ -> },
    onRestoreBackup: (String) -> Unit,
    onCreateFullNcdSnapshot: () -> Unit,
    onExportBackupsJson: () -> Unit,
    onImportBackupsJson: () -> Unit,
    onAddVoOption: (String, String) -> Unit,
    onGoToGarage: () -> Unit
) {
    val profile = session.vehicleProfile
    val strings = LocalAppStrings.current
    val theme = LocalThemeStyle.current
    if (profile == null) {
        DisconnectedPlaceholder(
            title = strings.disconnectedCodingTitle,
            subtitle = strings.disconnectedCodingSub,
            onGoToGarage = onGoToGarage
        )
        return
    }

    var selectedMode by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var easySearchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CodingCategory?>(null) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var selectedEasyModule by remember { mutableStateOf<String?>(null) }
    var selectedCafdModule by remember { mutableStateOf<String?>(null) }
    var newVoCode by remember { mutableStateOf("") }
    var newVoDesc by remember { mutableStateOf("") }
    var pendingPresetDiff by remember { mutableStateOf<Pair<CodingPreset, Int>?>(null) }
    var pendingCafdDiff by remember { mutableStateOf<Pair<CafdParameter, String>?>(null) }

    var stagedPresets by remember { mutableStateOf(mapOf<String, Int>()) }
    var stagedCafds by remember { mutableStateOf(mapOf<String, String>()) }
    var showSafetyDialog by remember { mutableStateOf(false) }

    pendingPresetDiff?.let { (preset, targetIdx) ->
        val beforeLabel = preset.options.getOrNull(preset.currentOptionIndex) ?: "-"
        val afterLabel = preset.options.getOrNull(targetIdx) ?: "-"
        AlertDialog(
            onDismissRequest = { pendingPresetDiff = null },
            title = { Text(strings.diffPreviewTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(preset.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("ECU: ${preset.targetEcu} (0x%02X)".format(preset.targetAddress), fontSize = 12.sp)
                    Text("CAFD: ${preset.cafdKey}", fontSize = 12.sp)
                    Text("${strings.diffBeforeLabel} $beforeLabel", color = StatusAmber, fontSize = 12.sp)
                    Text("${strings.diffAfterLabel} $afterLabel", color = StatusGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    if (session.isPhysicalSafeMode && !session.isPhysicalWriteUnlocked) {
                        Text(
                            text = strings.safeModeBannerLocked,
                            color = StatusRed,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val p = preset
                    val idx = targetIdx
                    pendingPresetDiff = null
                    onApplyPreset(p.id, idx)
                }) {
                    Text(strings.applyBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingPresetDiff = null }) {
                    Text(strings.cancelBtn)
                }
            }
        )
    }

    pendingCafdDiff?.let { (param, afterOption) ->
        val beforeBytes = param.availableOptions[param.selectedOptionLabel]?.let { "0x$it" } ?: "--"
        val afterBytes = param.availableOptions[afterOption]?.let { "0x$it" } ?: "--"
        AlertDialog(
            onDismissRequest = { pendingCafdDiff = null },
            title = { Text(strings.diffPreviewTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${param.section} -> ${param.functionName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("ECU: ${param.ecuName} • ${param.cafdFile}", fontSize = 12.sp)
                    Text("Offset: 0x%02X | Bitmask: 0x%02X".format(param.byteOffset, param.bitMask), fontSize = 12.sp)
                    Text("${strings.diffBeforeLabel} ${param.selectedOptionLabel} ($beforeBytes)", color = StatusAmber, fontSize = 12.sp)
                    Text("${strings.diffAfterLabel} $afterOption ($afterBytes)", color = StatusGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    if (session.isPhysicalSafeMode && !session.isPhysicalWriteUnlocked) {
                        Text(
                            text = strings.safeModeBannerLocked,
                            color = StatusRed,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val p = param
                    val opt = afterOption
                    pendingCafdDiff = null
                    onApplyCafd(p.id, opt)
                }) {
                    Text(strings.applyBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingCafdDiff = null }) {
                    Text(strings.cancelBtn)
                }
            }
        )
    }

    val voltageSafe = profile.batteryVoltage >= BimmerRepository.MIN_SAFE_CODING_VOLTAGE

    if (showSafetyDialog) {
        var safetyConfirmed by remember { mutableStateOf(false) }
        val totalStaged = stagedPresets.size + stagedCafds.size
        AlertDialog(
            onDismissRequest = { showSafetyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = if (voltageSafe) theme.primaryBright else StatusRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(strings.confirmCodingSafetyTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!voltageSafe) {
                        Text(
                            text = strings.batteryLockedBanner.format(profile.batteryVoltage),
                            color = StatusRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "${strings.batteryVoltageLabel}: %.1f V (${strings.batterySafeLabel})".format(profile.batteryVoltage),
                            color = StatusGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = strings.confirmCodingSafetyWarning,
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.cardElevated)
                            .clickable { safetyConfirmed = !safetyConfirmed }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = safetyConfirmed,
                            onCheckedChange = { safetyConfirmed = it }
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = strings.confirmCodingCheckbox,
                            color = theme.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pCopy = stagedPresets
                        val cCopy = stagedCafds
                        stagedPresets = emptyMap()
                        stagedCafds = emptyMap()
                        showSafetyDialog = false
                        onApplyBatchCoding(pCopy, cCopy)
                    },
                    enabled = voltageSafe && safetyConfirmed && totalStaged > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                ) {
                    Text(strings.codeVehicleBatchBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSafetyDialog = false }) { Text(strings.cancelBtn) }
            }
        )
    }

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

        val isSecureCodingVehicle = profile.chassis.family == com.punhanaghayev.bimmerpro.core.bmw.SeriesFamily.G_SERIES ||
            profile.chassis.name.contains("G", ignoreCase = true) ||
            profile.chassis.name.contains("I20", ignoreCase = true) ||
            profile.chassis.name.contains("U06", ignoreCase = true) ||
            profile.chassis.name.contains("U11", ignoreCase = true) ||
            profile.chassis.name.contains("U10", ignoreCase = true) ||
            profile.chassis.name.contains("F70", ignoreCase = true) ||
            profile.chassis.name.contains("F74", ignoreCase = true)

        if (isSecureCodingVehicle) {
            Card(
                colors = CardDefaults.cardColors(containerColor = StatusAmber.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, StatusAmber.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = StatusAmber,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.secureCodingWarningTitle,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = strings.secureCodingWarningText,
                            color = theme.textSecondary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        val isKLineVehicle = profile.chassis.family == com.punhanaghayev.bimmerpro.core.bmw.SeriesFamily.E_SERIES &&
            (profile.productionYear in 2001..2007 || profile.chassis.name.contains("E85", ignoreCase = true) || profile.chassis.name.contains("E65", ignoreCase = true))

        if (isKLineVehicle) {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.primaryBright.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, theme.primaryBright.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = theme.primaryBright,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.kLineWarningTitle,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = strings.kLineWarningText,
                            color = theme.textSecondary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
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
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedMode) {
                0 -> {
                    item {
                        OutlinedTextField(
                            value = easySearchQuery,
                            onValueChange = { easySearchQuery = it },
                            placeholder = { Text(strings.searchCodingHint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            textStyle = TextStyle(fontSize = 13.sp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory?.label ?: strings.filterAll,
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                label = { Text(strings.categoryFilterLabel, fontSize = 11.sp) },
                                textStyle = TextStyle(fontSize = 13.sp),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false },
                                modifier = Modifier.background(theme.cardElevated)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(strings.filterAll, color = theme.textPrimary) },
                                    onClick = {
                                        selectedCategory = null
                                        categoryDropdownExpanded = false
                                    }
                                )
                                CodingCategory.entries.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.label, color = theme.textPrimary) },
                                        onClick = {
                                            selectedCategory = cat
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    val showModuleList = selectedEasyModule == null && easySearchQuery.isBlank() && selectedCategory == null
                    if (showModuleList) {
                        val easyModules = session.presets.groupBy { it.targetEcu }
                        item {
                            Text(strings.codingModulesHint, color = theme.textSecondary, fontSize = 12.sp)
                        }
                        items(easyModules.keys.toList(), key = { "easy_$it" }) { ecuName ->
                            val list = easyModules[ecuName].orEmpty()
                            val addr = list.firstOrNull()?.targetAddress ?: 0
                            CodingModuleSelectorCard(
                                moduleName = ecuName,
                                addressHex = "0x%02X".format(addr),
                                itemCount = list.size,
                                onClick = { selectedEasyModule = ecuName }
                            )
                        }
                    } else {
                        if (selectedEasyModule != null) {
                            item {
                                OutlinedButton(
                                    onClick = { selectedEasyModule = null },
                                    modifier = Modifier.height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("${strings.backToModulesBtn} ($selectedEasyModule)", fontSize = 11.sp, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                        val filteredPresets = session.presets.filter { preset ->
                            (selectedEasyModule == null || preset.targetEcu == selectedEasyModule) &&
                                (selectedCategory == null || preset.category == selectedCategory) &&
                                (easySearchQuery.isBlank() ||
                                    preset.title.contains(easySearchQuery, ignoreCase = true) ||
                                    preset.description.contains(easySearchQuery, ignoreCase = true) ||
                                    preset.targetEcu.contains(easySearchQuery, ignoreCase = true))
                        }
                        items(filteredPresets, key = { it.id }) { preset ->
                            PresetCodingCard(
                                preset = preset,
                                stagedIndex = stagedPresets[preset.id],
                                enabled = voltageSafe,
                                onSelectOption = { idx ->
                                    stagedPresets = if (idx == preset.currentOptionIndex) {
                                        stagedPresets - preset.id
                                    } else {
                                        stagedPresets + (preset.id to idx)
                                    }
                                }
                            )
                        }
                    }
                }

                1 -> {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(strings.searchCafdHint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            textStyle = TextStyle(fontSize = 13.sp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    val showCafdModules = selectedCafdModule == null && searchQuery.isBlank()
                    if (showCafdModules) {
                        val cafdModules = session.cafdParameters.groupBy { it.ecuName }
                        item {
                            Text(strings.codingModulesHint, color = theme.textSecondary, fontSize = 12.sp)
                        }
                        items(cafdModules.keys.toList(), key = { "cafd_$it" }) { ecuName ->
                            val list = cafdModules[ecuName].orEmpty()
                            val addr = list.firstOrNull()?.ecuAddress ?: 0
                            CodingModuleSelectorCard(
                                moduleName = ecuName,
                                addressHex = "0x%02X".format(addr),
                                itemCount = list.size,
                                onClick = { selectedCafdModule = ecuName }
                            )
                        }
                    } else {
                        if (selectedCafdModule != null) {
                            item {
                                OutlinedButton(
                                    onClick = { selectedCafdModule = null },
                                    modifier = Modifier.height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("${strings.backToModulesBtn} ($selectedCafdModule)", fontSize = 11.sp, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                        val filtered = session.cafdParameters.filter { param ->
                            (selectedCafdModule == null || param.ecuName == selectedCafdModule) &&
                                (searchQuery.isBlank() ||
                                    param.functionName.contains(searchQuery, ignoreCase = true) ||
                                    param.ecuName.contains(searchQuery, ignoreCase = true) ||
                                    param.cafdFile.contains(searchQuery, ignoreCase = true))
                        }
                        items(filtered, key = { it.id }) { param ->
                            ExpertCafdCard(
                                param = param,
                                stagedOption = stagedCafds[param.id],
                                enabled = voltageSafe,
                                onApply = { optLabel ->
                                    stagedCafds = if (optLabel == param.selectedOptionLabel) {
                                        stagedCafds - param.id
                                    } else {
                                        stagedCafds + (param.id to optLabel)
                                    }
                                }
                            )
                        }
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
                                Text(strings.faVoTitle, fontWeight = FontWeight.Bold, color = theme.textPrimary)
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
                                        onValueChange = { input ->
                                            newVoCode = input.uppercase().take(6)
                                            val autoDesc = VoDependencyMapper.lookupOptionDescription(newVoCode)
                                            if (!autoDesc.startsWith("BMW Fabrika Opsiyonu") && autoDesc.isNotBlank()) {
                                                newVoDesc = autoDesc
                                            }
                                        },
                                        label = { Text(strings.codeLabel, fontSize = 12.sp, maxLines = 1) },
                                        placeholder = { Text("6NS", fontSize = 12.sp) },
                                        textStyle = TextStyle(fontSize = 13.sp),
                                        singleLine = true,
                                        modifier = Modifier.weight(0.36f)
                                    )
                                    OutlinedTextField(
                                        value = newVoDesc,
                                        onValueChange = { newVoDesc = it },
                                        label = { Text(strings.descLabel, fontSize = 12.sp, maxLines = 1) },
                                        placeholder = { Text("Enhanced BT", fontSize = 12.sp) },
                                        textStyle = TextStyle(fontSize = 13.sp),
                                        singleLine = true,
                                        modifier = Modifier.weight(0.64f)
                                    )
                                }
                                if (newVoCode.length >= 3) {
                                    val affectedNames = VoDependencyMapper.affectedEcusFor(newVoCode).joinToString(", ") { it.second }
                                    Text("Etkilenecek ECU'lar: $affectedNames", color = theme.primaryBright, fontSize = 11.sp)
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
                                    Text(strings.addVoBtn, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = onCreateFullNcdSnapshot,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Tam ECU NCD Snapshot Al", fontSize = 12.sp, maxLines = 1, softWrap = false)
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
                        Text("${strings.autoBackupsTitle} (${session.backups.size})", color = theme.textSecondary, fontWeight = FontWeight.SemiBold)
                    }

                    if (session.backups.isEmpty()) {
                        item {
                            Text(strings.noBackupsHint, color = theme.textSecondary, fontSize = 12.sp)
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
                                        Text(strings.rollbackBtn, maxLines = 1, softWrap = false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val totalStaged = stagedPresets.size + stagedCafds.size
        if (totalStaged > 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.cardElevated),
                border = BorderStroke(1.5.dp, theme.primaryBright),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.stagedChangesTitle.format(totalStaged),
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryBright,
                            fontSize = 13.sp
                        )
                        Text(
                            text = strings.discardStagedBtn,
                            color = StatusRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                stagedPresets = emptyMap()
                                stagedCafds = emptyMap()
                            }
                        )
                    }
                    Button(
                        onClick = { showSafetyDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.codeVehicleBatchBtn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
    stagedIndex: Int?,
    enabled: Boolean,
    onSelectOption: (Int) -> Unit
) {
    val theme = LocalThemeStyle.current
    val strings = LocalAppStrings.current
    val effectiveIndex = stagedIndex ?: preset.currentOptionIndex
    val isStaged = stagedIndex != null && stagedIndex != preset.currentOptionIndex
    val selectedBadgeText = when (strings.language) {
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "STAGED"
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "SEÇİLDİ"
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "ВЫБРАНО"
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> "SEÇİLDİ"
    }
    val pendingSuffix = when (strings.language) {
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "(Pending)"
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "(Gözləyir)"
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "(Ожидает)"
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> "(Bekliyor)"
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.card),
        border = BorderStroke(
            1.dp,
            if (isStaged) theme.primaryBright
            else if (preset.isLockedBySecureCoding) StatusAmber.copy(alpha = 0.5f)
            else theme.outline
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${preset.targetEcu} • ${preset.category.label}", color = theme.primaryBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                if (isStaged) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.primaryBright.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(selectedBadgeText, color = theme.primaryBright, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (preset.isLockedBySecureCoding) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Secure Coding 2.0", color = StatusAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                com.punhanaghayev.bimmerpro.core.i18n.GlobalTextLocalizer.localize(preset.title, strings.language),
                color = theme.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                com.punhanaghayev.bimmerpro.core.i18n.GlobalTextLocalizer.localize(preset.description, strings.language),
                color = theme.textSecondary,
                fontSize = 12.sp
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                preset.options.forEachIndexed { idx, label ->
                    val selected = effectiveIndex == idx
                    val localizedOpt = com.punhanaghayev.bimmerpro.core.i18n.GlobalTextLocalizer.localize(label, strings.language)
                    FilterChip(
                        selected = selected,
                        onClick = { if (enabled) onSelectOption(idx) },
                        label = {
                            Text(
                                text = if (selected && isStaged) "$localizedOpt $pendingSuffix" else localizedOpt,
                                fontSize = 11.sp
                            )
                        }
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
    stagedOption: String?,
    enabled: Boolean,
    onApply: (String) -> Unit
) {
    val theme = LocalThemeStyle.current
    val strings = LocalAppStrings.current
    val effectiveOption = stagedOption ?: param.selectedOptionLabel
    val isStaged = stagedOption != null && stagedOption != param.selectedOptionLabel
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.card),
        border = BorderStroke(1.dp, if (isStaged) theme.primaryBright else theme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${param.ecuName} • ${param.cafdFile}",
                    color = theme.primaryBright,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isStaged) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.primaryBright.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("SEÇİLDİ", color = theme.primaryBright, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                text = "Offset 0x%02X | Mask 0x%02X".format(param.byteOffset, param.bitMask),
                color = theme.textSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
            Text("${param.section} -> ${param.functionName}", color = theme.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text("${strings.currentValueLabel} ${param.selectedOptionLabel}", color = StatusGreen, fontSize = 12.sp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                param.availableOptions.keys.forEach { optionLabel ->
                    val isSelected = effectiveOption == optionLabel
                    OutlinedButton(
                        onClick = { if (enabled) onApply(optionLabel) },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) theme.primaryBright else theme.outline
                        )
                    ) {
                        Text(
                            text = if (isSelected && isStaged) "$optionLabel ${strings.stagedOptionPending}" else optionLabel,
                            fontSize = 11.sp,
                            color = if (isSelected) theme.primaryBright else theme.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CodingModuleSelectorCard(
    moduleName: String,
    addressHex: String,
    itemCount: Int,
    onClick: () -> Unit
) {
    val theme = LocalThemeStyle.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = theme.card),
        border = BorderStroke(1.dp, theme.outline),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Memory, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(20.dp))
                Column {
                    Text(moduleName, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("ECU $addressHex", color = theme.textSecondary, fontSize = 11.sp)
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.primary.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("$itemCount", color = theme.primaryBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
