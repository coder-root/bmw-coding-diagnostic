package com.punhanaghayev.bimmerpro.core.report

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.punhanaghayev.bimmerpro.core.bmw.DtcFault
import com.punhanaghayev.bimmerpro.core.bmw.EcuModule
import com.punhanaghayev.bimmerpro.core.bmw.TamperStatus
import com.punhanaghayev.bimmerpro.core.bmw.VehicleProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InspectionReportGenerator {

    fun generatePdf(
        context: Context,
        profile: VehicleProfile,
        ecus: List<EcuModule>,
        dtcs: List<DtcFault>
    ): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outFile = File(reportsDir, "BimmerPro_Ekspertiz_${profile.chassis.code}_$stamp.pdf")

        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 72dpi
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val headerBg = Paint().apply { color = Color.rgb(11, 18, 31) }
        val accentPaint = Paint().apply { color = Color.rgb(0, 102, 177) }
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subtitlePaint = Paint().apply {
            color = Color.rgb(180, 200, 224)
            textSize = 10f
            isAntiAlias = true
        }
        val sectionPaint = Paint().apply {
            color = Color.rgb(0, 102, 177)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9.5f
            isAntiAlias = true
        }
        val boldBodyPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val alertPaint = Paint().apply {
            color = if (profile.mileageVerification.status == TamperStatus.SUSPICIOUS_MISMATCH) {
                Color.rgb(185, 28, 28)
            } else {
                Color.rgb(21, 128, 61)
            }
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // Top Header Banner
        canvas.drawRect(0f, 0f, 595f, 84f, headerBg)
        canvas.drawRect(0f, 80f, 595f, 84f, accentPaint)
        canvas.drawText("BMW Expert PRO — BMW Teknik Ekspertiz & Diagnostik Raporu", 32f, 36f, titlePaint)
        val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US).format(Date())
        canvas.drawText("VIN: ${profile.vin}  |  Tarih: $dateStr  |  Developer: Punhan Aghayev", 32f, 58f, subtitlePaint)

        var y = 112f

        // Vehicle Info
        canvas.drawText("1. ARAÇ KİMLİK BİLGİLERİ", 32f, y, sectionPaint)
        y += 18f
        canvas.drawText("Model / Şasi: ${profile.modelDisplayName} (${profile.chassis.code})", 32f, y, boldBodyPaint)
        canvas.drawText("Motor: ${profile.engineCode}", 340f, y, bodyPaint)
        y += 15f
        canvas.drawText("Üretim Yılı: ${profile.productionYear}   |   I-Level: ${profile.iLevel}", 32f, y, bodyPaint)
        canvas.drawText("Akü Voltajı: %.1f V".format(profile.batteryVoltage), 340f, y, bodyPaint)
        y += 24f

        // Cross-ECU Mileage Check
        canvas.drawText("2. ÇOKLU MODÜL KİLOMETRE DOĞRULAMA", 32f, y, sectionPaint)
        y += 16f
        canvas.drawText("Durum: ${profile.mileageVerification.status.label}", 32f, y, alertPaint)
        y += 14f
        canvas.drawText(profile.mileageVerification.summary, 32f, y, bodyPaint)
        y += 16f
        profile.mileageVerification.readings.forEach { reading ->
            val hours = reading.engineHours?.let { "  (Motor Saati: $it h)" } ?: ""
            canvas.drawText("• ${reading.moduleName}: %,d km%s".format(reading.mileageKm, hours), 44f, y, bodyPaint)
            y += 13f
        }
        y += 14f

        // ECU Summary
        canvas.drawText("3. TARANAN ECU MODÜLLERİ (${ecus.size} Adet)", 32f, y, sectionPaint)
        y += 16f
        ecus.take(10).forEach { ecu ->
            val line = "${ecu.shortName} (${ecu.addressHex}) — SW: ${ecu.softwareVersion} | Aktif Hata: ${ecu.activeDtcCount} | Gölge: ${ecu.shadowDtcCount}"
            canvas.drawText(line, 40f, y, bodyPaint)
            y += 13f
        }
        y += 14f

        // DTCs
        canvas.drawText("4. ARIZA KAYITLARI (${dtcs.size} Adet)", 32f, y, sectionPaint)
        y += 16f
        if (dtcs.isEmpty()) {
            canvas.drawText("Araçta aktif veya kayıtlı arıza kodu bulunmamaktadır.", 40f, y, bodyPaint)
            y += 15f
        } else {
            dtcs.take(12).forEach { dtc ->
                if (y > 775f) return@forEach
                canvas.drawText(
                    "[${dtc.severity.label.uppercase()}] ${dtc.ecuName} - ${dtc.code}: ${dtc.title}",
                    40f,
                    y,
                    boldBodyPaint
                )
                y += 13f
                canvas.drawText(
                    "   Freeze-Frame: %,d km | %.1f V | %d°C | %d rpm".format(
                        dtc.freezeFrame.mileageKm,
                        dtc.freezeFrame.batteryVoltage,
                        dtc.freezeFrame.coolantTempC,
                        dtc.freezeFrame.engineRpm
                    ),
                    40f,
                    y,
                    bodyPaint
                )
                y += 16f
            }
        }

        // Footer
        canvas.drawLine(32f, 808f, 563f, 808f, accentPaint)
        canvas.drawText(
            "BMW Expert PRO Diagnostics & Coding Suite — Lead Developer: Punhan Aghayev",
            32f,
            824f,
            bodyPaint
        )

        pdf.finishPage(page)
        FileOutputStream(outFile).use { pdf.writeTo(it) }
        pdf.close()
        return outFile
    }
}
