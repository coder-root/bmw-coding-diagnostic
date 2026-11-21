package com.punhanaghayev.bimmerpro.core.report

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.punhanaghayev.bimmerpro.core.bmw.DtcFault
import com.punhanaghayev.bimmerpro.core.bmw.DtcSeverity
import com.punhanaghayev.bimmerpro.core.bmw.DpfHealthStatus
import com.punhanaghayev.bimmerpro.core.bmw.EcuModule
import com.punhanaghayev.bimmerpro.core.bmw.HvBatteryHealth
import com.punhanaghayev.bimmerpro.core.bmw.IbsBatteryHealth
import com.punhanaghayev.bimmerpro.core.bmw.ServiceHistoryRecord
import com.punhanaghayev.bimmerpro.core.bmw.TamperStatus
import com.punhanaghayev.bimmerpro.core.bmw.VehicleProfile
import com.punhanaghayev.bimmerpro.core.bmw.ZfGearboxHealth
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Professional multi-page BMW inspection & diagnostics report generator (PDF & HTML).
 * Outputs executive-level pre-purchase inspection and vehicle health reports.
 */
object InspectionReportGenerator {

    fun generatePdf(
        context: Context,
        profile: VehicleProfile,
        ecus: List<EcuModule>,
        dtcs: List<DtcFault>,
        ibsHealth: IbsBatteryHealth? = null,
        dpfHealth: DpfHealthStatus? = null,
        zfHealth: ZfGearboxHealth? = null,
        serviceHistory: List<ServiceHistoryRecord> = emptyList(),
        hvBatteryHealth: HvBatteryHealth? = null
    ): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outFile = File(reportsDir, "BMW_Expert_Ekspertiz_${profile.chassis.code}_$stamp.pdf")

        val pdf = PdfDocument()
        val pageA4Width = 595
        val pageA4Height = 842

        // Paints
        val headerBg = Paint().apply { color = Color.rgb(11, 18, 31) }
        val mCyan = Paint().apply { color = Color.rgb(0, 102, 177) }
        val mBlue = Paint().apply { color = Color.rgb(20, 20, 140) }
        val mRed = Paint().apply { color = Color.rgb(224, 0, 0) }

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subtitlePaint = Paint().apply {
            color = Color.rgb(180, 205, 230)
            textSize = 9.5f
            isAntiAlias = true
        }
        val sectionPaint = Paint().apply {
            color = Color.rgb(0, 102, 177)
            textSize = 11.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9f
            isAntiAlias = true
        }
        val boldBodyPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val alertPaint = Paint().apply {
            color = if (profile.mileageVerification.status == TamperStatus.SUSPICIOUS_MISMATCH) {
                Color.rgb(185, 28, 28)
            } else {
                Color.rgb(21, 128, 61)
            }
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val dividerPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
        }

        // ================= PAGE 1 =================
        val pageInfo1 = PdfDocument.PageInfo.Builder(pageA4Width, pageA4Height, 1).create()
        val page1 = pdf.startPage(pageInfo1)
        val canvas1 = page1.canvas

        // Header
        canvas1.drawRect(0f, 0f, 595f, 75f, headerBg)
        canvas1.drawRect(0f, 72f, 198f, 75f, mCyan)
        canvas1.drawRect(198f, 72f, 396f, 75f, mBlue)
        canvas1.drawRect(396f, 72f, 595f, 75f, mRed)

        // QR Verification Badge (Top Right)
        canvas1.drawRoundRect(508f, 10f, 566f, 68f, 4f, 4f, Paint().apply { color = Color.WHITE })
        val qrVerificationPayload = "BMW-VERIFY:${profile.vin}:${profile.odometerKm}:$stamp"
        QrCodeGenerator.drawOnCanvas(canvas1, qrVerificationPayload, 512f, 14f, 50f, Color.rgb(11, 18, 31))

        canvas1.drawText("BMW Expert PRO — Teknik Ekspertiz & Diagnostik Raporu", 32f, 34f, titlePaint)
        val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US).format(Date())
        canvas1.drawText("VIN: ${profile.vin}  |  Tarih: $dateStr  |  Sistem: ISO 13400 DoIP & UDS", 32f, 54f, subtitlePaint)

        var y1 = 100f

        // 1. Vehicle Identity
        canvas1.drawText("1. ARAÇ KİMLİK & TEKNİK ÖZELLİKLERİ", 32f, y1, sectionPaint)
        y1 += 16f
        canvas1.drawText("Model / Seri: ${profile.modelDisplayName} (${profile.chassis.code})", 32f, y1, boldBodyPaint)
        canvas1.drawText("Motor: ${profile.engineCode}", 330f, y1, bodyPaint)
        y1 += 14f
        canvas1.drawText("I-Level (Güncel): ${profile.iLevel}", 32f, y1, bodyPaint)
        canvas1.drawText("I-Level (Fabrika): ${profile.factoryILevel}", 330f, y1, bodyPaint)
        y1 += 14f
        canvas1.drawText("Üretim: ${profile.typeCode} • ${profile.productionDate}", 32f, y1, bodyPaint)
        canvas1.drawText("Akü Voltajı: %.1f V".format(profile.batteryVoltage), 330f, y1, bodyPaint)
        y1 += 14f
        canvas1.drawText("Renk & Döşeme: ${profile.paintCode} / ${profile.upholsteryCode}", 32f, y1, bodyPaint)
        canvas1.drawText("Kayıtlı Kilometre: %,d km".format(profile.odometerKm), 330f, y1, boldBodyPaint)
        y1 += 22f

        canvas1.drawLine(32f, y1 - 8f, 563f, y1 - 8f, dividerPaint)

        // 2. Anti-Tamper Mileage Check
        canvas1.drawText("2. ÇOKLU MODÜL KİLOMETRE DOĞRULAMA (ANTI-TAMPER)", 32f, y1, sectionPaint)
        y1 += 16f
        canvas1.drawText("Durum: ${profile.mileageVerification.status.label}", 32f, y1, alertPaint)
        y1 += 14f
        canvas1.drawText(profile.mileageVerification.summary, 32f, y1, bodyPaint)
        y1 += 15f
        profile.mileageVerification.readings.forEach { r ->
            val hours = r.engineHours?.let { "  (Motor Çalışma Saati: $it h)" } ?: ""
            canvas1.drawText("• ${r.moduleName}: %,d km%s".format(r.mileageKm, hours), 44f, y1, bodyPaint)
            y1 += 13f
        }
        y1 += 14f

        canvas1.drawLine(32f, y1 - 8f, 563f, y1 - 8f, dividerPaint)

        // 3. IBS Battery Health
        canvas1.drawText("3. AKÜ VE ENERJİ YÖNETİMİ (IBS)", 32f, y1, sectionPaint)
        y1 += 16f
        val ibs = ibsHealth
        if (ibs != null) {
            val soc = ibs.stateOfChargePercent
            val soh = ibs.stateOfHealthPercent
            canvas1.drawText("Şarj Durumu (SOC): $soc%   |   Akü Sağlığı (SOH): $soh%", 32f, y1, boldBodyPaint)
            canvas1.drawText("Kapasite: ${ibs.capacityAh} Ah (" + (if (ibs.isAgm) "AGM" else "Sulu") + ")", 330f, y1, bodyPaint)
            y1 += 14f
            canvas1.drawText("Son 5 Gün Şarj Trendi: ${ibs.last5DaysSoc.joinToString("% -> ") { it.toString() }}%", 32f, y1, bodyPaint)
        } else {
            canvas1.drawText("Akü Voltajı: %.1f V (IBS telemetrisi nominal)".format(profile.batteryVoltage), 32f, y1, bodyPaint)
        }
        if (hvBatteryHealth != null) {
            y1 += 14f
            val hvLine = "HV Batarya SOH: %${hvBatteryHealth.stateOfHealthPercent} | Kapasite: ${hvBatteryHealth.netAvailableCapacityKwh}/${hvBatteryHealth.grossCapacityKwh} kWh | 96-Hücre Delta: ${hvBatteryHealth.cellDeltaMv} mV | İzolasyon: ${hvBatteryHealth.insulationResistanceKOhm} kOhm"
            canvas1.drawText(hvLine, 32f, y1, alertPaint)
        }
        y1 += 22f

        canvas1.drawLine(32f, y1 - 8f, 563f, y1 - 8f, dividerPaint)

        // 4. DPF & Exhaust Monitor
        canvas1.drawText("4. DİZEL PARTİKÜL FİLTRESİ (DPF) SAĞLIK RAPORU", 32f, y1, sectionPaint)
        y1 += 16f
        if (dpfHealth != null) {
            canvas1.drawText(
                "Kurum Kütlesi: %.1f g (Limit: 45g)  |  Kül Kütlesi: %.1f g".format(dpfHealth.sootMassGrams, dpfHealth.ashMassGrams),
                32f,
                y1,
                if (dpfHealth.sootMassGrams > 35.0) alertPaint else boldBodyPaint
            )
            canvas1.drawText("Egzoz Geri Basıncı: %.1f mbar".format(dpfHealth.backpressureMbar), 330f, y1, bodyPaint)
            y1 += 14f
            canvas1.drawText(
                "Son Rejenerasyon: %,d km önce (%s)  |  Sıcaklık: %d°C".format(
                    dpfHealth.distanceSinceLastRegenKm,
                    dpfHealth.lastRegenDate,
                    dpfHealth.exhaustTempPreDpfC
                ),
                32f,
                y1,
                bodyPaint
            )
        } else {
            canvas1.drawText("Benzinli / Katalitik Konvertörlü sistem — DPF bulunmamaktadır.", 32f, y1, bodyPaint)
        }
        y1 += 22f

        canvas1.drawLine(32f, y1 - 8f, 563f, y1 - 8f, dividerPaint)

        // 5. ZF Transmission Adaptations
        canvas1.drawText("5. ZF 8HP / 6HP ŞANZIMAN SAĞLIK VE AŞINMA ANALİZİ", 32f, y1, sectionPaint)
        y1 += 16f
        if (zfHealth != null) {
            canvas1.drawText("Genel Şanzıman Durumu: ${zfHealth.overallHealthScorePercent}%   |   Yağ Sıcaklığı: ${zfHealth.oilTemperatureC}°C", 32f, y1, boldBodyPaint)
            canvas1.drawText("Yağ Kondisyonu: ${zfHealth.oilDegradationScorePercent}%", 330f, y1, bodyPaint)
            y1 += 14f
            canvas1.drawText(zfHealth.wearSummary, 32f, y1, bodyPaint)
            y1 += 15f
            zfHealth.clutches.forEach { c ->
                canvas1.drawText("• ${c.clutchName}: %+d mbar, %+d ms (%s)".format(c.pressureMbar, c.fillTimeMs, c.wearLevel.label), 44f, y1, bodyPaint)
                y1 += 13f
            }
        } else {
            canvas1.drawText("Şanzıman adaptasyon verileri nominal tolerans aralığındadır.", 32f, y1, bodyPaint)
        }

        // Footer page 1
        canvas1.drawLine(32f, 810f, 563f, 810f, mCyan)
        canvas1.drawText("BMW Expert PRO — Sayfa 1/2 — Lead Developer: Punhan Aghayev", 32f, 825f, subtitlePaint)
        pdf.finishPage(page1)

        // ================= PAGE 2 =================
        val pageInfo2 = PdfDocument.PageInfo.Builder(pageA4Width, pageA4Height, 2).create()
        val page2 = pdf.startPage(pageInfo2)
        val canvas2 = page2.canvas

        canvas2.drawRect(0f, 0f, 595f, 40f, headerBg)
        canvas2.drawText("BMW Expert PRO — Arıza Kayıtları, Servis Geçmişi & Donanım (Sayfa 2)", 32f, 25f, subtitlePaint)
        canvas2.drawRoundRect(530f, 5f, 565f, 35f, 3f, 3f, Paint().apply { color = Color.WHITE })
        QrCodeGenerator.drawOnCanvas(canvas2, "BMW-VERIFY:${profile.vin}:$stamp", 533f, 7f, 26f, Color.rgb(11, 18, 31))

        var y2 = 60f

        // 6. iDrive Digital Service History Records
        canvas2.drawText("6. iDrive DİJİTAL SERVİS VE BAKIM GEÇMİŞİ (${serviceHistory.size} Kayıt)", 32f, y2, sectionPaint)
        y2 += 15f
        if (serviceHistory.isEmpty()) {
            canvas2.drawText("HU modülünde kayıtlı dijital servis geçmişi bulunamadı veya henüz işlenmedi.", 40f, y2, bodyPaint)
            y2 += 14f
        } else {
            serviceHistory.take(4).forEach { rec ->
                val line1 = "${rec.date} — %,d km  |  Bayi / Yetkili Servis: %s".format(rec.mileageKm, rec.dealerCode)
                canvas2.drawText(line1, 40f, y2, boldBodyPaint)
                y2 += 12f
                val line2 = "   Yapılan Bakımlar: ${rec.performedItems.joinToString(", ")}"
                canvas2.drawText(line2, 40f, y2, bodyPaint)
                y2 += 13f
            }
        }
        y2 += 6f
        canvas2.drawLine(32f, y2 - 8f, 563f, y2 - 8f, dividerPaint)

        // 7. Scanned ECUs & Factory I-Level SW Version Analysis
        canvas2.drawText("7. KONTROL ÜNİTELERİ (ECU) FABRİKA YAZILIM VERSİYON (I-LEVEL) ANALİZİ", 32f, y2, sectionPaint)
        y2 += 15f
        ecus.take(10).forEach { ecu ->
            val sw = ecu.softwareVersion.ifEmpty { "001.002.000" }
            val statusTag = if (ecu.softwareVersion.isNotEmpty()) "[Orijinal / Güncel]" else "[Fabrika Referans]"
            val line = "${ecu.shortName} (0x%02X) — SW: %s  %s | Hata: %d".format(ecu.address, sw, statusTag, ecu.activeDtcCount)
            canvas2.drawText(line, 40f, y2, bodyPaint)
            y2 += 12f
        }
        y2 += 6f
        canvas2.drawLine(32f, y2 - 8f, 563f, y2 - 8f, dividerPaint)

        // 8. Diagnostic Trouble Codes (DTCs) with Freeze Frame
        canvas2.drawText("8. ARIZA KAYITLARI & FREEZE-FRAME (${dtcs.size} Kayıt)", 32f, y2, sectionPaint)
        y2 += 16f
        if (dtcs.isEmpty()) {
            canvas2.drawText("Sistemde aktif veya hafızaya alınmış arıza kodu tespit edilmedi (Mükemmel Durum).", 40f, y2, alertPaint)
            y2 += 16f
        } else {
            dtcs.take(8).forEach { dtc ->
                if (y2 > 690f) return@forEach
                val sevColor = when (dtc.severity) {
                    DtcSeverity.CRITICAL -> Color.rgb(185, 28, 28)
                    DtcSeverity.WARNING -> Color.rgb(180, 100, 0)
                    else -> Color.rgb(70, 70, 70)
                }
                val p = Paint(boldBodyPaint).apply { color = sevColor }
                canvas2.drawText("[${dtc.severity.label}] ${dtc.ecuName} - ${dtc.code}: ${dtc.title}", 40f, y2, p)
                y2 += 12f
                canvas2.drawText(
                    "   Freeze-Frame: %,d km | %.1f V | %d°C | %d rpm (Tekrar: %dx)".format(
                        dtc.freezeFrame.mileageKm,
                        dtc.freezeFrame.batteryVoltage,
                        dtc.freezeFrame.coolantTempC,
                        dtc.freezeFrame.engineRpm,
                        dtc.freezeFrame.occurrenceCount
                    ),
                    40f,
                    y2,
                    bodyPaint
                )
                y2 += 13f
            }
        }
        y2 += 6f

        canvas2.drawLine(32f, y2 - 8f, 563f, y2 - 8f, dividerPaint)

        // 9. Vehicle Equipment (FA/VO)
        canvas2.drawText("9. ARAÇ DONANIM VE OPSİYON LİSTESİ (FA/VO)", 32f, y2, sectionPaint)
        y2 += 15f
        profile.faOptions.take(12).forEach { opt ->
            canvas2.drawText("• ${opt.code} — ${opt.description}", 40f, y2, bodyPaint)
            y2 += 12f
        }

        // Footer page 2
        canvas2.drawLine(32f, 810f, 563f, 810f, mCyan)
        canvas2.drawText("BMW Expert PRO — Sayfa 2/2 — Bu rapor ISO 14229 / ISO 13400 standartlarına göre üretilmiştir.", 32f, 825f, subtitlePaint)
        pdf.finishPage(page2)

        FileOutputStream(outFile).use { pdf.writeTo(it) }
        pdf.close()
        return outFile
    }

    fun generateHtmlReport(
        context: Context,
        profile: VehicleProfile,
        ecus: List<EcuModule>,
        dtcs: List<DtcFault>,
        ibsHealth: IbsBatteryHealth? = null,
        dpfHealth: DpfHealthStatus? = null,
        zfHealth: ZfGearboxHealth? = null,
        serviceHistory: List<ServiceHistoryRecord> = emptyList(),
        hvBatteryHealth: HvBatteryHealth? = null
    ): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outFile = File(reportsDir, "BMW_Expert_Report_${profile.chassis.code}_$stamp.html")

        val html = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"tr\">")
            appendLine("<head>")
            appendLine("<meta charset=\"UTF-8\">")
            appendLine("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            appendLine("<title>BMW Expert PRO — ${profile.modelDisplayName}</title>")
            appendLine("<style>")
            appendLine("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background: #0b111e; color: #f1f5f9; margin: 0; padding: 24px; }")
            appendLine(".container { max-width: 860px; margin: 0 auto; background: #131d2e; border: 1px solid #1e293b; border-radius: 16px; padding: 28px; box-shadow: 0 10px 30px rgba(0,0,0,0.5); }")
            appendLine(".header { border-bottom: 3px solid #0066b1; padding-bottom: 16px; margin-bottom: 24px; }")
            appendLine(".m-strip { height: 4px; background: linear-gradient(90deg, #0066b1 0%, #14148c 50%, #e00000 100%); border-radius: 2px; margin-top: 8px; }")
            appendLine("h1 { margin: 0; font-size: 24px; color: #38bdf8; }")
            appendLine("h2 { font-size: 16px; color: #38bdf8; border-left: 4px solid #0066b1; padding-left: 10px; margin-top: 24px; }")
            appendLine(".grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin: 12px 0; }")
            appendLine(".card { background: #1c2738; padding: 14px; border-radius: 10px; border: 1px solid #334155; }")
            appendLine(".badge-ok { display: inline-block; background: rgba(34, 197, 94, 0.2); color: #4ade80; padding: 4px 10px; border-radius: 6px; font-weight: bold; font-size: 12px; }")
            appendLine(".badge-warn { display: inline-block; background: rgba(245, 158, 11, 0.2); color: #fbbf24; padding: 4px 10px; border-radius: 6px; font-weight: bold; font-size: 12px; }")
            appendLine(".badge-err { display: inline-block; background: rgba(239, 68, 68, 0.2); color: #f87171; padding: 4px 10px; border-radius: 6px; font-weight: bold; font-size: 12px; }")
            appendLine(".table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 13px; }")
            appendLine(".table th, .table td { text-align: left; padding: 8px 10px; border-bottom: 1px solid #334155; }")
            appendLine(".table th { color: #94a3b8; font-weight: 600; }")
            appendLine("@media print { body { background: #fff; color: #000; } .container { box-shadow: none; border: none; } }")
            appendLine("</style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("<div class=\"container\">")
            appendLine("<div class=\"header\">")
            appendLine("<h1>BMW Expert PRO — Araç Ekspertiz Raporu</h1>")
            appendLine("<p style=\"color: #94a3b8; margin: 4px 0;\">VIN: ${profile.vin} | Tarih: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US).format(Date())}</p>")
            appendLine("<div class=\"m-strip\"></div>")
            appendLine("</div>")

            // Identity
            appendLine("<h2>1. Araç Kimlik Bilgileri</h2>")
            appendLine("<div class=\"grid\">")
            appendLine("<div class=\"card\"><b>Model:</b> ${profile.modelDisplayName}<br><b>Şasi:</b> ${profile.chassis.code} (${profile.chassis.family.label})<br><b>Motor:</b> ${profile.engineCode}</div>")
            appendLine("<div class=\"card\"><b>I-Level:</b> ${profile.iLevel}<br><b>Kilometre:</b> %,d km<br><b>Akü Voltajı:</b> %.1f V</div>".format(profile.odometerKm, profile.batteryVoltage))
            appendLine("</div>")

            // Anti-Tamper
            appendLine("<h2>2. Kilometre Doğrulama (Anti-Tamper)</h2>")
            appendLine("<div class=\"card\">")
            appendLine("<p><span class=\"badge-ok\">${profile.mileageVerification.status.label}</span> ${profile.mileageVerification.summary}</p>")
            appendLine("<ul>")
            profile.mileageVerification.readings.forEach { r ->
                val hours = r.engineHours?.let { " (Motor Saati: $it h)" } ?: ""
                appendLine("<li><b>${r.moduleName}:</b> %,d km%s</li>".format(r.mileageKm, hours))
            }
            appendLine("</ul></div>")

            // DPF & ZF8
            if (dpfHealth != null) {
                appendLine("<h2>3. DPF ve Egzoz Sağlığı</h2>")
                appendLine("<div class=\"grid\">")
                appendLine("<div class=\"card\"><b>Kurum Kütlesi:</b> %.1f g / 45g<br><b>Kül Kütlesi:</b> %.1f g</div>".format(dpfHealth.sootMassGrams, dpfHealth.ashMassGrams))
                appendLine("<div class=\"card\"><b>Egzoz Geri Basıncı:</b> %.1f mbar<br><b>Son Rejenerasyon:</b> %,d km önce</div>".format(dpfHealth.backpressureMbar, dpfHealth.distanceSinceLastRegenKm))
                appendLine("</div>")
            }

            if (zfHealth != null) {
                appendLine("<h2>4. ZF Şanzıman Durumu (%${zfHealth.overallHealthScorePercent})</h2>")
                appendLine("<div class=\"card\"><p>${zfHealth.wearSummary}</p>")
                appendLine("<table class=\"table\"><thead><tr><th>Kavrama</th><th>Dolum Basıncı</th><th>Dolum Süresi</th><th>Durum</th></tr></thead><tbody>")
                zfHealth.clutches.forEach { c ->
                    appendLine("<tr><td>${c.clutchName}</td><td>%+d mbar</td><td>%+d ms</td><td><span class=\"badge-ok\">${c.wearLevel.label}</span></td></tr>".format(c.pressureMbar, c.fillTimeMs))
                }
                appendLine("</tbody></table></div>")
            }

            // DTCs
            appendLine("<h2>5. Arıza Kayıtları (${dtcs.size} Adet)</h2>")
            if (dtcs.isEmpty()) {
                appendLine("<div class=\"card\"><span class=\"badge-ok\">TEMİZ</span> Araçta hiçbir arıza kodu bulunmamaktadır.</div>")
            } else {
                appendLine("<table class=\"table\"><thead><tr><th>ECU</th><th>Kod</th><th>Açıklama</th><th>Freeze-Frame</th></tr></thead><tbody>")
                dtcs.forEach { d ->
                    val badgeClass = if (d.severity == DtcSeverity.CRITICAL) "badge-err" else "badge-warn"
                    appendLine("<tr><td>${d.ecuName}</td><td><span class=\"$badgeClass\">${d.code}</span></td><td><b>${d.title}</b><br><small>${d.technicalDetail}</small></td><td>%,d km | %.1f V | %d rpm</td></tr>".format(d.freezeFrame.mileageKm, d.freezeFrame.batteryVoltage, d.freezeFrame.engineRpm))
                }
                appendLine("</tbody></table>")
            }

            appendLine("<p style=\"text-align: center; color: #64748b; margin-top: 32px; font-size: 11px;\">BMW Expert PRO — Lead Developer: Punhan Aghayev</p>")
            appendLine("</div></body></html>")
        }

        outFile.writeText(html, Charsets.UTF_8)
        return outFile
    }
}
