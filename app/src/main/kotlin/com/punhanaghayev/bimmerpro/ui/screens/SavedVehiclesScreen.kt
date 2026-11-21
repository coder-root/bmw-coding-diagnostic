package com.punhanaghayev.bimmerpro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.data.SavedVehicleRecord
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import com.punhanaghayev.bimmerpro.ui.theme.LocalThemeStyle

@Composable
fun SavedVehiclesScreen(
    savedVehicles: List<SavedVehicleRecord>,
    onConnectSaved: (AdapterType) -> Unit,
    onRemoveSavedVehicle: (String) -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val theme = LocalThemeStyle.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = theme.primaryBright)
                }
                Text(
                    text = strings.savedVehiclesTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (savedVehicles.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, theme.outline),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(36.dp))
                        Text(strings.noSavedVehiclesHint, color = theme.textSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        TextButton(onClick = onBack) {
                            Text(strings.goToGarageBtn, color = theme.primaryBright)
                        }
                    }
                }
            }
        } else {
            items(savedVehicles, key = { it.vin }) { saved ->
                Card(
                    modifier = Modifier.clickable {
                        val matched = AdapterType.entries.firstOrNull { it.displayName == saved.adapterName }
                            ?: AdapterType.SIMULATOR_F10
                        onConnectSaved(matched)
                        onBack()
                    },
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    border = BorderStroke(1.dp, theme.outline),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(saved.modelName, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("VIN: ${saved.vin}", color = theme.primaryBright, fontSize = 11.sp, fontFamily = FontFamily.Monospace, maxLines = 1, softWrap = false)
                            Text("${saved.chassisCode} • ${saved.lastConnected} • %,d km".format(saved.odometerKm), color = theme.textSecondary, fontSize = 11.sp)
                            val latestSnap = saved.scanSnapshots.firstOrNull()
                            if (latestSnap != null) {
                                Text(
                                    text = "${saved.scanSnapshots.size} tarama geçmişi • Son Skor: ${latestSnap.healthScorePercent}/100 (${latestSnap.dtcCodes.size} DTC)",
                                    color = theme.primaryBright,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        IconButton(onClick = { onRemoveSavedVehicle(saved.vin) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
