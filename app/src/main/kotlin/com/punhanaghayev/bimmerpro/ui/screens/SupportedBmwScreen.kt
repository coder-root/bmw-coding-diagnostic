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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.punhanaghayev.bimmerpro.core.bmw.BmwModelSupportCatalog
import com.punhanaghayev.bimmerpro.core.bmw.ModelSupportLevel
import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage
import com.punhanaghayev.bimmerpro.core.i18n.GlobalTextLocalizer
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle
import com.punhanaghayev.bimmerpro.ui.theme.StatusAmber
import com.punhanaghayev.bimmerpro.ui.theme.StatusGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportedBmwScreen(
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val theme = LocalThemeStyle.current
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(BmwModelSupportCatalog.allCategories.first()) }
    val filteredModels = remember(selectedCategory) { BmwModelSupportCatalog.filterByCategory(selectedCategory) }

    val screenTitle = when (strings.language) {
        AppLanguage.EN -> "Supported BMW Models"
        AppLanguage.AZ -> "Dəstəklənən BMW Modelləri"
        AppLanguage.RU -> "Поддерживаемые модели BMW"
        AppLanguage.TR -> "Desteklenen BMW Modelleri"
    }
    val seriesLabel = when (strings.language) {
        AppLanguage.EN -> "Select BMW Series"
        AppLanguage.AZ -> "BMW Seriyasını Seçin"
        AppLanguage.RU -> "Выберите серию BMW"
        AppLanguage.TR -> "BMW Serisi Seçin"
    }
    val modelsCountLabel = when (strings.language) {
        AppLanguage.EN -> "${filteredModels.size} Supported Models & Chassis"
        AppLanguage.AZ -> "${filteredModels.size} Dəstəklənən Model və Kuzov"
        AppLanguage.RU -> "${filteredModels.size} поддерживаемых моделей и кузовов"
        AppLanguage.TR -> "${filteredModels.size} Desteklenen Model & Kasa"
    }
    val diagPrefix = when (strings.language) {
        AppLanguage.EN -> "• Diagnostics:"
        AppLanguage.AZ -> "• Diaqnostika:"
        AppLanguage.RU -> "• Диагностика:"
        AppLanguage.TR -> "• Teşhis:"
    }
    val codingPrefix = when (strings.language) {
        AppLanguage.EN -> "• Coding:"
        AppLanguage.AZ -> "• Kodlaşdırma:"
        AppLanguage.RU -> "• Кодирование:"
        AppLanguage.TR -> "• Kodlama:"
    }
    val adapterPrefix = when (strings.language) {
        AppLanguage.EN -> "• Required Adapter:"
        AppLanguage.AZ -> "• Tələb olunan Adapter:"
        AppLanguage.RU -> "• Необходимый адаптер:"
        AppLanguage.TR -> "• Gerekli Adaptör:"
    }
    val limitPrefix = when (strings.language) {
        AppLanguage.EN -> "⚠️ Limitation:"
        AppLanguage.AZ -> "⚠️ Məhdudiyyət:"
        AppLanguage.RU -> "⚠️ Ограничение:"
        AppLanguage.TR -> "⚠️ Kısıtlama Nedeni:"
    }
    val solutionPrefix = when (strings.language) {
        AppLanguage.EN -> "💡 Solution:"
        AppLanguage.AZ -> "💡 Həll yolu:"
        AppLanguage.RU -> "💡 Решение:"
        AppLanguage.TR -> "💡 Çözüm:"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.textPrimary)
                }
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = theme.primaryBright, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(screenTitle, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            label = { Text(seriesLabel) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier.background(theme.cardElevated)
                        ) {
                            BmwModelSupportCatalog.allCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, fontSize = 13.sp, color = theme.textPrimary) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text(modelsCountLabel, color = theme.primaryBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(filteredModels) { model ->
            val badgeColor = when (model.supportLevel) {
                ModelSupportLevel.FULL -> StatusGreen
                ModelSupportLevel.PARTIAL_SECURE_CODING -> StatusAmber
                ModelSupportLevel.K_LINE_HARDWARE -> theme.primaryBright
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.card),
                border = BorderStroke(1.dp, theme.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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
                            Text(
                                text = model.chassisCodes,
                                color = theme.primaryBright,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Text(
                            text = model.yearRange,
                            color = theme.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = GlobalTextLocalizer.localize(model.supportLevel.label, strings.language),
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text("$diagPrefix ${GlobalTextLocalizer.localize(model.diagnosticSummary, strings.language)}", color = theme.textPrimary, fontSize = 11.sp)
                    Text("$codingPrefix ${GlobalTextLocalizer.localize(model.codingSummary, strings.language)}", color = theme.textSecondary, fontSize = 11.sp)
                    Text("$adapterPrefix ${GlobalTextLocalizer.localize(model.requiredAdapter, strings.language)}", color = theme.primaryBright, fontSize = 11.sp)

                    if (model.limitationNote != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusAmber.copy(alpha = 0.12f))
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "$limitPrefix ${GlobalTextLocalizer.localize(model.limitationNote, strings.language)}",
                                    color = StatusAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (model.howToSupportSolution != null) {
                                    Text(
                                        "$solutionPrefix ${GlobalTextLocalizer.localize(model.howToSupportSolution, strings.language)}",
                                        color = theme.textPrimary,
                                        fontSize = 10.sp
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
