package com.punhanaghayev.bimmerpro.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.bmw.DtcFault
import com.punhanaghayev.bimmerpro.core.bmw.DtcSeverity
import com.punhanaghayev.bimmerpro.core.bmw.TamperStatus
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
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

@Composable
fun DiagnosticsScreen(
    session: BimmerSessionState,
    onRunScan: () -> Unit,
    onClearAllDtcs: () -> Unit,
    onExportPdf: () -> Unit,
    onGoToGarage: () -> Unit
) {
    val profile = session.vehicleProfile
    if (profile == null) {
        DisconnectedPlaceholder(
            title = "Diagnostik Hazır Değil",
            subtitle = "Tüm ECU'ları taramak ve arıza/ekspertiz raporu almak için önce araç bağlantısı kurun.",
            onGoToGarage = onGoToGarage
        )
        return
    }

    var filterMode by remember { mutableStateOf(DtcFilterMode.ALL) }
    var showClearDialog by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    val filteredDtcs = remember(session.dtcs, filterMode) {
        when (filterMode) {
            DtcFilterMode.ALL -> session.dtcs
            DtcFilterMode.ACTIVE_AND_WARN -> session.dtcs.filter { it.severity != DtcSeverity.SHADOW }
            DtcFilterMode.SHADOW_ONLY -> session.dtcs.filter { it.severity == DtcSeverity.SHADOW }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Tüm Hata Hafızaları Silinsin mi?") },
            text = { Text("Gateway üzerinden tüm ECU'lara UDS 0x14 ClearDiagnosticInfo komutu gönderilecek.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    onClearAllDtcs()
                }) {
                    Text("Evet, Temizle", color = StatusRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("İptal") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRunScan,
                    enabled = !session.isScanning,
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BmwBlue)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (session.isScanning) strings.scanningText else strings.scanAllEcusBtn, maxLines = 1, softWrap = false, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onExportPdf,
                    modifier = Modifier.weight(1f).height(44.dp),
                    border = BorderStroke(1.dp, BmwBlueBright.copy(alpha = 0.7f))
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = BmwBlueBright, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.expertisePdfBtn, color = BmwBlueBright, maxLines = 1, softWrap = false, fontSize = 12.sp)
                }
            }
        }

        if (session.isScanning || session.scanStatusText.isNotBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BimmerCard),
                    border = BorderStroke(1.dp, BimmerOutline)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(session.scanStatusText, color = TextPrimary, fontSize = 12.sp)
                        LinearProgressIndicator(
                            progress = { session.scanProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = BmwBlueBright
                        )
                    }
                }
            }
        }

        // Cross-ECU Mileage Verification Card
        item {
            val mv = profile.mileageVerification
            val isTampered = mv.status == TamperStatus.SUSPICIOUS_MISMATCH
            val badgeColor = if (isTampered) StatusRed else StatusGreen
            Card(
                colors = CardDefaults.cardColors(containerColor = BimmerCard),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isTampered) Icons.Default.Warning else Icons.Default.Shield,
                            contentDescription = null,
                            tint = badgeColor
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Çoklu Modül KM Doğrulama", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text(mv.status.label, color = badgeColor, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                    Text(mv.summary, color = TextSecondary, fontSize = 12.sp)
                    mv.readings.forEach { reading ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(reading.moduleName, color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = "%,d km".format(reading.mileageKm) + (reading.engineHours?.let { " • ${it}h" } ?: ""),
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
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
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DtcFilterMode.entries.forEach { mode ->
                        FilterChip(
                            selected = filterMode == mode,
                            onClick = { filterMode = mode },
                            label = { Text(mode.label, fontSize = 11.sp) }
                        )
                    }
                }
                if (session.dtcs.isNotEmpty()) {
                    TextButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = StatusRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Sil", color = StatusRed, fontSize = 12.sp)
                    }
                }
            }
        }

        if (filteredDtcs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BimmerCard),
                    border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Seçili filtrede arıza kaydı bulunmuyor.",
                        modifier = Modifier.padding(18.dp),
                        color = StatusGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            items(filteredDtcs, key = { it.id }) { dtc ->
                ExpandableDtcCard(dtc = dtc)
            }
        }

        item {
            Text(
                text = "Tarama Yapılan ECU Modülleri (${session.ecus.size})",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(session.ecus, key = { it.address }) { ecu ->
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
                        Text("${ecu.shortName} (${ecu.addressHex}) • ${ecu.fullName}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("${ecu.softwareVersion} | ${ecu.codingCapability.label}", color = TextSecondary, fontSize = 11.sp)
                    }
                    val total = ecu.activeDtcCount + ecu.shadowDtcCount
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background((if (total > 0) StatusAmber else StatusGreen).copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (total == 0) "TEMİZ" else "$total HATA",
                            color = if (total > 0) StatusAmber else StatusGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableDtcCard(dtc: DtcFault) {
    var expanded by remember { mutableStateOf(false) }
    val severityColor = when (dtc.severity) {
        DtcSeverity.CRITICAL -> StatusRed
        DtcSeverity.WARNING -> StatusAmber
        DtcSeverity.INFO -> BmwBlueBright
        DtcSeverity.SHADOW -> TextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = BimmerCard),
        border = BorderStroke(1.dp, severityColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(severityColor.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(dtc.code, color = severityColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text("${dtc.ecuName} • ${dtc.status.label}", color = TextSecondary, fontSize = 11.sp)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
            Text(dtc.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    Text(dtc.technicalDetail, color = TextSecondary, fontSize = 12.sp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BimmerCardElevated)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Freeze-Frame: %,d km | %.1f V | %d°C | %d rpm (Tekrar: %dx)".format(
                                dtc.freezeFrame.mileageKm,
                                dtc.freezeFrame.batteryVoltage,
                                dtc.freezeFrame.coolantTempC,
                                dtc.freezeFrame.engineRpm,
                                dtc.freezeFrame.occurrenceCount
                            ),
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BmwBlue.copy(alpha = 0.16f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("ISTA Yönlendirmeli Kontrol Planı", color = BmwBlueBright, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(dtc.istaGuidance, color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisconnectedPlaceholder(
    title: String,
    subtitle: String,
    onGoToGarage: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BimmerCard),
            border = BorderStroke(1.dp, BimmerOutline),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(subtitle, color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Button(onClick = onGoToGarage, colors = ButtonDefaults.buttonColors(containerColor = BmwBlue)) {
                    Text("Garaj & Bağlantı Ekranına Git")
                }
            }
        }
    }
}

private enum class DtcFilterMode(val label: String) {
    ALL("Tümü"),
    ACTIVE_AND_WARN("Kritik / Uyarı"),
    SHADOW_ONLY("Gölge")
}
