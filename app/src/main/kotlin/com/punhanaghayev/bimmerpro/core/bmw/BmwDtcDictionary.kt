package com.punhanaghayev.bimmerpro.core.bmw

data class DtcCatalogEntry(
    val title: String,
    val detail: String,
    val severity: DtcSeverity,
    val guidance: String
)

/**
 * Built-in BMW Hex DTC dictionary for F10/F30/G30/E90 physical ECU scans.
 */
object BmwDtcDictionary {

    private val entries: Map<String, DtcCatalogEntry> = mapOf(
        "245700" to DtcCatalogEntry(
            title = "DPF Partikül Filtresi: Kurum Yükü Sınırda",
            detail = "Diferansiyel basınç veya hesaplanan kurum kütlesi eşik değerine ulaştı.",
            severity = DtcSeverity.WARNING,
            guidance = "Canlı veriden DPF kurum miktarını kontrol edin; uygunsa zorunlu rejenerasyon başlatın."
        ),
        "244800" to DtcCatalogEntry(
            title = "DPF Partikül Filtresi: Kalan Mesafe Sınırlı",
            detail = "DPF kül birikimi nedeniyle filtre ömrü azaldı.",
            severity = DtcSeverity.WARNING,
            guidance = "DPF egzoz karşı basınç testi yapın ve gerekirse kimyasal/profesyonel temizlik uygulayın."
        ),
        "290900" to DtcCatalogEntry(
            title = "Hava Kütlesi Sistemi: Ölçülen Hava Kütle Akışı Çok Yüksek (EGR)",
            detail = "EGR valfi kapalı kalmış veya emme manifoldunda kaçak olabilir.",
            severity = DtcSeverity.WARNING,
            guidance = "EGR valfi hareket testi yapın ve emme hattı sızdırmazlığını kontrol edin."
        ),
        "255000" to DtcCatalogEntry(
            title = "Yakıt Rail Basınç Kontrolü: Sapma Çok Yüksek",
            detail = "Yüksek basınç yakıt pompası veya rail basınç regülatöründe sapma.",
            severity = DtcSeverity.CRITICAL,
            guidance = "Yakıt filtresi, düşük basınç beslemesi ve rail basınç sensörü soketini kontrol edin."
        ),
        "26CF00" to DtcCatalogEntry(
            title = "Kızdırma Bujisi Silindir 1-4 Aktivasyon Hatası",
            detail = "Bir veya birden fazla kızdırma bujisi veya kızdırma rölesi (GSE) arızalı.",
            severity = DtcSeverity.INFO,
            guidance = "Kızdırma bujisi dirençlerini (0.5–1.5 Ohm) ve kızdırma kontrol ünitesini ölçün."
        ),
        "8040B9" to DtcCatalogEntry(
            title = "Akü Deşarj / Uyku Akımı İhlali",
            detail = "Araç kilitliyken 80 mA üzeri parazit akım çekimi algılandı.",
            severity = DtcSeverity.INFO,
            guidance = "Comfort Access kapı kolları ve sonradan takılan aksesuarları kontrol edin."
        ),
        "8041A8" to DtcCatalogEntry(
            title = "Hoş Geldiniz / İç Aydınlatma Çıkışı Kısa Devre Koruması",
            detail = "FEM/CAS aydınlatma devresinde aşırı akım algılandı.",
            severity = DtcSeverity.INFO,
            guidance = "LED ampul değişimi sonrası direnç/kısa devre sayacını kontrol edin."
        ),
        "CD9304" to DtcCatalogEntry(
            title = "LIN Bus: Aktif Radyatör Panjuru Haberleşme Gecikmesi",
            detail = "Gölge hafıza — geçici LIN veri yolu cevapsızlığı.",
            severity = DtcSeverity.SHADOW,
            guidance = "Mekanik takılma yoksa bilgi amaçlı gölge kaydıdır; temizlenebilir."
        ),
        "E1C447" to DtcCatalogEntry(
            title = "MOST Ring Geçici Senkronizasyon Kaybı",
            detail = "Fiber optik MOST halkasında kısa süreli senkronizasyon kesintisi.",
            severity = DtcSeverity.SHADOW,
            guidance = "Ses/iDrive kesintisi yoksa temizlenip izlenebilir."
        ),
        "D35A53" to DtcCatalogEntry(
            title = "DSC: Eksik / Hatalı Motor Torku Sinyali",
            detail = "PT-CAN üzerinden DME/DDE tork verisi geçici olarak alınamadı.",
            severity = DtcSeverity.WARNING,
            guidance = "Önce DME/DDE arıza kodlarını okuyun; motor hatası çözüldüğünde DSC kodu silinecektir."
        ),
        "480A11" to DtcCatalogEntry(
            title = "Fren Balatası Aşınma Sensörü (Ön Aks) Sınırda",
            detail = "Ön fren balata kalınlığı uyarı eşiğine ulaştı.",
            severity = DtcSeverity.WARNING,
            guidance = "Balata ve aşınma sensörünü kontrol edip değişim sonrası CBS sıfırlaması yapın."
        ),
        "420311" to DtcCatalogEntry(
            title = "EGS ZF8 Şanzıman: Yağ Ömrü / Adaptasyon Uyarısı",
            detail = "Şanzıman yağı ısıl yaşlanma sayacı yüksek.",
            severity = DtcSeverity.INFO,
            guidance = "80.000-100.000 km aralığında ZF Lifeguard 8 yağı ve karter filtresi değişimi önerilir."
        ),
        "30FF" to DtcCatalogEntry(
            title = "Turbo Takviye Basıncı Hedefin Altında (Underboost)",
            detail = "İstenen boost basıncına ulaşılamadı.",
            severity = DtcSeverity.CRITICAL,
            guidance = "Vakum hatları, boost selenoidleri ve charge-pipe bağlantısını kontrol edin."
        ),
        "B7F8C3" to DtcCatalogEntry(
            title = "RAM / TCB Acil Çağrı Anteni Yüksek Empedans",
            detail = "Telematik anten devresinde açık devre veya yüksek direnç.",
            severity = DtcSeverity.WARNING,
            guidance = "Tavan anteni su sızdırmazlığını ve TCB yedek bataryasını kontrol edin."
        ),
        "278F00" to DtcCatalogEntry(
            title = "Alternatör Haberleşme / BSD Veri Yolu Hatası",
            detail = "DME ile alternatör arasındaki BSD tek telli haberleşme kesildi.",
            severity = DtcSeverity.WARNING,
            guidance = "BSD hattı soketini, alternatör regülatörünü ve IBS sensörünü kontrol edin."
        ),
        "28A000" to DtcCatalogEntry(
            title = "Gaz Kelebeği Açısı - Emme Basıncı Korelasyonu",
            detail = "Ölçülen emme manifoldu basıncı hesaplanan değerden sapıyor (hava kaçağı).",
            severity = DtcSeverity.WARNING,
            guidance = "Charge-pipe, intercooler hortumları ve PCV valfini duman testi ile kontrol edin."
        ),
        "120308" to DtcCatalogEntry(
            title = "Turbo Takviye Basıncı Kontrolü: Basınç Çok Düşük",
            detail = "N20/B48/B58 turboşarj basınç devresinde hedef basınç yakalanamadı.",
            severity = DtcSeverity.CRITICAL,
            guidance = "Elektrikli wastegate aktüatörü, diverter valf ve intercooler borularını kontrol edin."
        ),
        "140010" to DtcCatalogEntry(
            title = "Ateşleme Teklemesi (Misfire) Çoklu Silindir",
            detail = "Birden fazla silindirde yanma kesintisi tespit edildi.",
            severity = DtcSeverity.CRITICAL,
            guidance = "Bujiler, ateşleme bobinleri ve enjektör yakıt düzeltme değerlerini kontrol edin."
        ),
        "8040B7" to DtcCatalogEntry(
            title = "CAS4 / FEM Terminal 15N Röle Aktivasyon Hatası",
            detail = "Kontak besleme hattında voltaj düşümü veya röle yapışması.",
            severity = DtcSeverity.INFO,
            guidance = "Sigorta kutusu ve akü kutup başı sıkılığını kontrol edin."
        ),
        "930910" to DtcCatalogEntry(
            title = "ACSM Sürücü Hava Yastığı Direnç Çok Yüksek",
            detail = "Direksiyon zembereği (SZL) veya koltuk altı soketinde yüksek direnç.",
            severity = DtcSeverity.CRITICAL,
            guidance = "Aküyü söküp 10 dk bekledikten sonra koltuk/direksiyon sarı soketlerini kontrol edin."
        ),
        "E71420" to DtcCatalogEntry(
            title = "IHKA AUC / Hava Kalite Sensörü Arızası",
            detail = "Dış hava kirlilik sensörü yanıt vermiyor.",
            severity = DtcSeverity.SHADOW,
            guidance = "Klima performansını etkilemez; sensör soketi kontrol edilebilir."
        )
    )

    fun lookup(codeHex: String): DtcCatalogEntry? =
        entries[codeHex.trim().uppercase()]

    fun search(query: String): List<Pair<String, DtcCatalogEntry>> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()
        return entries.entries
            .filter { (code, entry) ->
                code.contains(q, ignoreCase = true) ||
                    entry.title.contains(q, ignoreCase = true) ||
                    entry.detail.contains(q, ignoreCase = true)
            }
            .map { it.key to it.value }
            .take(8)
    }
}
