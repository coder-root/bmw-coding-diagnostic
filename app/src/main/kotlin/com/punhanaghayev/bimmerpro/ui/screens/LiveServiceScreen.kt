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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutine
import com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutineType
import com.punhanaghayev.bimmerpro.core.i18n.LocalAppStrings
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import com.punhanaghayev.bimmerpro.ui.components.TelemetrySparkline
import com.punhanaghayev.bimmerpro.ui.theme.BimmerCard
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
fun LiveServiceScreen(
    session: BimmerSessionState,
    isLiveStreamRunning: Boolean,
    onToggleLiveStream: () -> Unit,
    onExportCsv: () -> Unit,
    onExecuteRoutine: (ServiceRoutine, Byte) -> Unit,
    onWriteServiceHistory: (String, Int, String, List<String>) -> Unit,
    onGoToGarage: () -> Unit
) {
    val profile = session.vehicleProfile
    if (profile == null) {
        DisconnectedPlaceholder(
            title = "Canlı Veri & Servis Beklemede",
            subtitle = "Sensör grafikleri, DPF rejenerasyonu ve akü tanıtma işlemleri için önce araca bağlanın.",
            onGoToGarage = onGoToGarage
        )
        return
    }

    var batteryDialogRoutine by remember { mutableStateOf<ServiceRoutine?>(null) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    if (showHistoryDialog) {
        var dealerCode by remember { mutableStateOf("Punhan Garage (08801)") }
        var mileageInput by remember { mutableStateOf(profile.odometerKm.toString()) }
        val allItems = listOf("Motor Yağı", "Mikrofiltre", "Hava Filtresi", "Fren Hidroliği", "Buji", "Araç Kontrol")
        var selectedItems by remember { mutableStateOf(setOf("Motor Yağı", "Mikrofiltre")) }

        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text("iDrive Dijital Servis Geçmişi Yaz") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dealerCode,
                        onValueChange = { dealerCode = it },
                        label = { Text("Servis / Bayi Kodu") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mileageInput,
                        onValueChange = { mileageInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Bakım Kilometresi (km)") },
                        singleLine = true
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        allItems.forEach { item ->
                            val isSel = item in selectedItems
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    selectedItems = if (isSel) selectedItems - item else selectedItems + item
                                },
                                label = { Text(item, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val km = mileageInput.toIntOrNull() ?: profile.odometerKm
                        onWriteServiceHistory("2026-11-18", km, dealerCode, selectedItems.toList())
                        showHistoryDialog = false
                    },
                    enabled = selectedItems.isNotEmpty()
                ) {
                    Text("HU Modülüne Yaz")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHistoryDialog = false }) { Text("İptal") }
            }
        )
    }

    batteryDialogRoutine?.let { routine ->
        val capacities = listOf(
            "70 Ah AGM" to 0x70.toByte(),
            "80 Ah AGM" to 0x80.toByte(),
            "90 Ah AGM" to 0x90.toByte(),
            "105 Ah AGM" to 0xA5.toByte(),
            "80 Ah Sulu (Lead-Acid)" to 0x40.toByte()
        )
        AlertDialog(
            onDismissRequest = { batteryDialogRoutine = null },
            title = { Text("Yeni Akü Kapasitesi Seçin") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("IBS şarj profili seçilen kapasiteye göre sıfırlanacak:", fontSize = 12.sp)
                    capacities.forEach { (label, code) ->
                        OutlinedButton(
                            onClick = {
                                batteryDialogRoutine = null
                                onExecuteRoutine(routine, code)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { batteryDialogRoutine = null }) { Text("İptal") }
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.liveTelemetryTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("UDS 0x22 DID", color = TextSecondary, fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = onExportCsv) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("CSV", fontSize = 11.sp, maxLines = 1, softWrap = false)
                    }
                    Button(
                        onClick = onToggleLiveStream,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLiveStreamRunning) StatusRed else BmwBlue
                        )
                    ) {
                        Icon(
                            imageVector = if (isLiveStreamRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (isLiveStreamRunning) strings.stopBtn else strings.startBtn, fontSize = 12.sp, maxLines = 1, softWrap = false)
                    }
                }
            }
        }

        items(session.liveChannels.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { channel ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = BimmerCard),
                        border = BorderStroke(1.dp, BimmerOutline),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(channel.name, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
                            Text(
                                text = "${channel.currentValue} ${channel.unit}",
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            TelemetrySparkline(
                                points = channel.history,
                                lineColor = BmwBlueBright,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                            )
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text("Servis & Bakım Fonksiyonları (UDS 0x31)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        session.lastServiceResult?.let { res ->
            item {
                val color = if (res.success) StatusGreen else StatusRed
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.14f))
                        .padding(12.dp)
                ) {
                    Text("${res.timestamp} • ${res.title}: ${res.message}", color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        items(session.serviceRoutines, key = { it.routineId }) { routine ->
            Card(
                colors = CardDefaults.cardColors(containerColor = BimmerCard),
                border = BorderStroke(1.dp, BimmerOutline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${routine.targetEcu} • RID 0x%04X".format(routine.routineId), color = BmwBlueBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(routine.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(routine.description, color = TextSecondary, fontSize = 12.sp)
                    Text("Not: ${routine.warningNote}", color = StatusAmber, fontSize = 11.sp)
                    Button(
                        onClick = {
                            if (routine.type == ServiceRoutineType.BATTERY_REGISTRATION) {
                                batteryDialogRoutine = routine
                            } else {
                                onExecuteRoutine(routine, 0x01)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BmwBlue),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Rutini Çalıştır")
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("iDrive Dijital Servis Geçmişi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                OutlinedButton(onClick = { showHistoryDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Kayıt Yaz", fontSize = 11.sp)
                }
            }
        }

        items(session.serviceHistory, key = { it.id }) { entry ->
            Card(
                colors = CardDefaults.cardColors(containerColor = BimmerCard),
                border = BorderStroke(1.dp, BimmerOutline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${entry.date} • %,d km".format(entry.mileageKm), color = BmwBlueBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(entry.dealerCode, color = TextSecondary, fontSize = 11.sp)
                    }
                    Text(entry.performedItems.joinToString(" • "), color = TextPrimary, fontSize = 12.sp)
                }
            }
        }
    }
}
