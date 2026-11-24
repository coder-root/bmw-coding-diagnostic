package com.punhanaghayev.bimmerpro.core.report

import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage
import com.punhanaghayev.bimmerpro.core.i18n.GlobalTextLocalizer

data class ReportLabels(
    val langCode: String,
    val docTitle: String,
    val dateLabel: String,
    val systemLabel: String,
    val sec1Identity: String,
    val sec2Mileage: String,
    val sec3Battery: String,
    val sec4Dpf: String,
    val sec5Transmission: String,
    val sec6ServiceHistory: String,
    val sec7EcuSw: String,
    val sec8Dtcs: String,
    val sec12Fsc: String,
    val modelLabel: String,
    val engineLabel: String,
    val iLevelCurrentLabel: String,
    val iLevelFactoryLabel: String,
    val productionLabel: String,
    val batteryVoltageLabel: String,
    val paintUpholsteryLabel: String,
    val odometerLabel: String,
    val statusLabel: String,
    val noDtcsClean: String,
    val pageLabel: String,
    val noServiceHistory: String
)

object ReportLocalization {

    fun labelsFor(language: AppLanguage): ReportLabels = when (language) {
        AppLanguage.EN -> ReportLabels(
            langCode = "en",
            docTitle = "BMW Expert PRO — Technical Inspection & Diagnostic Report",
            dateLabel = "Date",
            systemLabel = "System",
            sec1Identity = "1. VEHICLE IDENTITY & TECHNICAL SPECIFICATIONS",
            sec2Mileage = "2. CROSS-ECU MILEAGE VERIFICATION (ANTI-TAMPER)",
            sec3Battery = "3. BATTERY & ENERGY MANAGEMENT (IBS / HV)",
            sec4Dpf = "4. DIESEL PARTICULATE FILTER (DPF) HEALTH REPORT",
            sec5Transmission = "5. ZF 8HP / 6HP TRANSMISSION WEAR & ADAPTATIONS",
            sec6ServiceHistory = "6. iDrive DIGITAL SERVICE & MAINTENANCE HISTORY",
            sec7EcuSw = "7. CONTROL UNITS (ECU) FACTORY SOFTWARE (I-LEVEL) ANALYSIS",
            sec8Dtcs = "8. DIAGNOSTIC TROUBLE CODES (DTC) & FREEZE-FRAME",
            sec12Fsc = "12. FSC ACTIVATION CERTIFICATE STATUS",
            modelLabel = "Model / Series",
            engineLabel = "Engine",
            iLevelCurrentLabel = "I-Level (Current)",
            iLevelFactoryLabel = "I-Level (Factory)",
            productionLabel = "Production",
            batteryVoltageLabel = "Battery Voltage",
            paintUpholsteryLabel = "Paint & Upholstery",
            odometerLabel = "Odometer",
            statusLabel = "Status",
            noDtcsClean = "No active or stored fault codes detected (Excellent Condition).",
            pageLabel = "Page",
            noServiceHistory = "No digital service history records found in Headunit."
        )
        AppLanguage.AZ -> ReportLabels(
            langCode = "az",
            docTitle = "BMW Expert PRO — Texniki Ekspertiza və Diaqnostika Hesabatı",
            dateLabel = "Tarix",
            systemLabel = "Sistem",
            sec1Identity = "1. AVTOMOBİLİN KİMLİYİ VƏ TEXNİKİ GÖSTƏRİCİLƏRİ",
            sec2Mileage = "2. ÇOXLU MODUL YÜRÜŞ YOXLAMASI (ANTI-TAMPER)",
            sec3Battery = "3. AKKUMULYATOR VƏ ENERJİ İDARƏETMƏSİ (IBS / HV)",
            sec4Dpf = "4. DİZEL PARTİKÜL FİLTRİ (DPF) SAĞLAMLIQ HESABATI",
            sec5Transmission = "5. ZF 8HP / 6HP SÜRƏTLƏR QUTUSU AŞINMA ANALİZİ",
            sec6ServiceHistory = "6. iDrive RƏQƏMSAL SERVİS VƏ BAXIM TARİXÇƏSİ",
            sec7EcuSw = "7. İDARƏETMƏ BLOKLARI (ECU) ZAVOD PROQRAM (I-LEVEL) ANALİZİ",
            sec8Dtcs = "8. XƏTA KODLARI (DTC) VƏ FREEZE-FRAME",
            sec12Fsc = "12. FSC AKTİVASİYA SERTİFİKATLARI",
            modelLabel = "Model / Seriya",
            engineLabel = "Mühərrik",
            iLevelCurrentLabel = "I-Level (Cari)",
            iLevelFactoryLabel = "I-Level (Zavod)",
            productionLabel = "İstehsal",
            batteryVoltageLabel = "Akkumulyator Gərginliyi",
            paintUpholsteryLabel = "Rəng və Salon",
            odometerLabel = "Qeyd olunmuş Yürüş",
            statusLabel = "Vəziyyət",
            noDtcsClean = "Sistemdə aktiv və ya yaddaşda xəta kodu aşkar edilmədi (Əla Vəziyyət).",
            pageLabel = "Səhifə",
            noServiceHistory = "HU modulunda rəqəmsal servis tarixçəsi tapılmadı."
        )
        AppLanguage.RU -> ReportLabels(
            langCode = "ru",
            docTitle = "BMW Expert PRO — Технический отчет диагностики и экспертизы",
            dateLabel = "Дата",
            systemLabel = "Система",
            sec1Identity = "1. ИДЕНТИФИКАЦИЯ И ТЕХНИЧЕСКИЕ ХАРАКТЕРИСТИКИ",
            sec2Mileage = "2. СВЕРКА ПРОБЕГА ПО БЛОКАМ (ANTI-TAMPER)",
            sec3Battery = "3. СОСТОЯНИЕ АКБ И УПРАВЛЕНИЕ ЭНЕРГИЕЙ (IBS / HV)",
            sec4Dpf = "4. ОТЧЕТ ПО САЖЕВОМУ ФИЛЬТРУ (DPF)",
            sec5Transmission = "5. АНАЛИЗ ИЗНОСА И АДАПТАЦИЙ АКПП ZF 8HP / 6HP",
            sec6ServiceHistory = "6. ЦИФРОВАЯ ИСТОРИЯ ОБСЛУЖИВАНИЯ iDrive",
            sec7EcuSw = "7. АНАЛИЗ ВЕРСИЙ ПО БЛОКОВ УПРАВЛЕНИЯ (I-LEVEL)",
            sec8Dtcs = "8. КОДЫ НЕИСПРАВНОСТЕЙ (DTC) И FREEZE-FRAME",
            sec12Fsc = "12. СТАТУС СЕРТИФИКАТОВ АКТИВАЦИИ FSC",
            modelLabel = "Модель / Серия",
            engineLabel = "Двигатель",
            iLevelCurrentLabel = "I-Level (Текущий)",
            iLevelFactoryLabel = "I-Level (Заводской)",
            productionLabel = "Производство",
            batteryVoltageLabel = "Напряжение АКБ",
            paintUpholsteryLabel = "Цвет и салон",
            odometerLabel = "Пробег",
            statusLabel = "Статус",
            noDtcsClean = "Активных или сохраненных кодов неисправностей не обнаружено.",
            pageLabel = "Страница",
            noServiceHistory = "Цифровые записи обслуживания в головном устройстве не найдены."
        )
        AppLanguage.TR -> ReportLabels(
            langCode = "tr",
            docTitle = "BMW Expert PRO — Teknik Ekspertiz & Diagnostik Raporu",
            dateLabel = "Tarih",
            systemLabel = "Sistem",
            sec1Identity = "1. ARAÇ KİMLİK & TEKNİK ÖZELLİKLERİ",
            sec2Mileage = "2. ÇOKLU MODÜL KİLOMETRE DOĞRULAMA (ANTI-TAMPER)",
            sec3Battery = "3. AKÜ VE ENERJİ YÖNETİMİ (IBS / HV)",
            sec4Dpf = "4. DİZEL PARTİKÜL FİLTRESİ (DPF) SAĞLIK RAPORU",
            sec5Transmission = "5. ZF 8HP / 6HP ŞANZIMAN SAĞLIK VE AŞINMA ANALİZİ",
            sec6ServiceHistory = "6. iDrive DİJİTAL SERVİS VE BAKIM GEÇMİŞİ",
            sec7EcuSw = "7. KONTROL ÜNİTELERİ (ECU) FABRİKA YAZILIM VERSİYON (I-LEVEL) ANALİZİ",
            sec8Dtcs = "8. ARIZA KAYITLARI & FREEZE-FRAME",
            sec12Fsc = "12. FSC AKTİVASYON SERTİFİKA DURUMLARI",
            modelLabel = "Model / Seri",
            engineLabel = "Motor",
            iLevelCurrentLabel = "I-Level (Güncel)",
            iLevelFactoryLabel = "I-Level (Fabrika)",
            productionLabel = "Üretim",
            batteryVoltageLabel = "Akü Voltajı",
            paintUpholsteryLabel = "Renk & Döşeme",
            odometerLabel = "Kayıtlı Kilometre",
            statusLabel = "Durum",
            noDtcsClean = "Sistemde aktif veya hafızaya alınmış arıza kodu tespit edilmedi (Mükemmel Durum).",
            pageLabel = "Sayfa",
            noServiceHistory = "HU modülünde kayıtlı dijital servis geçmişi bulunamadı veya henüz işlenmedi."
        )
    }

    fun loc(text: String, language: AppLanguage): String =
        GlobalTextLocalizer.localize(text, language)
}
