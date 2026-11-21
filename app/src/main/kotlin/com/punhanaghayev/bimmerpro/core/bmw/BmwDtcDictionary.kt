package com.punhanaghayev.bimmerpro.core.bmw

import com.punhanaghayev.bimmerpro.core.update.ExternalDtcPackDto

data class DtcCatalogEntry(
    val title: String,
    val detail: String,
    val severity: DtcSeverity,
    val guidance: String
)

/**
 * Built-in + downloadable BMW Hex DTC dictionary for F10/F30/G30/E90 physical ECU scans.
 */
object BmwDtcDictionary {

    private val externalEntries = mutableMapOf<String, DtcCatalogEntry>()

    fun loadExternalPack(pack: ExternalDtcPackDto) {
        pack.entries.forEach { dto ->
            val sev = runCatching { DtcSeverity.valueOf(dto.severity.uppercase()) }.getOrDefault(DtcSeverity.WARNING)
            externalEntries[dto.code.trim().uppercase()] = DtcCatalogEntry(
                title = dto.title,
                detail = dto.detail,
                severity = sev,
                guidance = dto.guidance
            )
        }
    }

    fun totalCodeCount(): Int = (entries.keys + externalEntries.keys).size

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
        ),
        "8041C3" to DtcCatalogEntry(
            title = "FRM / FEM: Sağ Gündüz Farı (Angel Eyes) Kısa Devre Sayacı",
            detail = "Kısa devre koruması nedeniyle çıkış sürücüsü kilitlendi.",
            severity = DtcSeverity.WARNING,
            guidance = "Ampul/LED modül tesisatını onarın ve Kısa Devre Kilidi Sıfırlama aracını çalıştırın."
        ),
        "8041B6" to DtcCatalogEntry(
            title = "FRM / FEM: Sol Kısa Far Çıkışı Kısa Devre Kilidi Aktif",
            detail = "50 kısa devre döngüsü aşıldığı için çıkış devre dışı bırakıldı.",
            severity = DtcSeverity.CRITICAL,
            guidance = "Tesisatı onardıktan sonra Servis sekmesinden Kısa Devre Kilidini sıfırlayın."
        ),
        "118001" to DtcCatalogEntry(
            title = "Yakıt Karışım Kontrolü: Karışım Çok Fakir (Bank 1)",
            detail = "Uzun dönem yakıt düzeltmesi (%LTFT) +20% üst sınırına ulaştı.",
            severity = DtcSeverity.CRITICAL,
            guidance = "Vakum kaçağı, düşük yakıt basıncı veya arızalı O2 sensörü için test yapın."
        ),
        "130308" to DtcCatalogEntry(
            title = "VANOS Egzoz Eksantrik Mili Pozisyon Ulaşım Hatası",
            detail = "Egzoz VANOS solenoidi yağ basıncı veya mekanik dişli yanıtı yavaş.",
            severity = DtcSeverity.WARNING,
            guidance = "Motor yağ seviyesini, yağ filtresi kafesini ve VANOS selenoid süzgeçlerini kontrol edin."
        ),
        "240500" to DtcCatalogEntry(
            title = "EGR Valfi Pozisyon Sapması: Valf Açık Takılı",
            detail = "Egzoz gazı devridaim valfi hedeflenen kapanma açısına ulaşamıyor.",
            severity = DtcSeverity.CRITICAL,
            guidance = "EGR valfi kurum temizliği veya değişimi yapın; adaptasyonu sıfırlayın."
        ),
        "480A12" to DtcCatalogEntry(
            title = "DSC: Sağ Arka Tekerlek Hız Sensörü Sinyal Kesintisi",
            detail = "ABS/DSC ünitesi sağ arka porya hız halkasından sinyal alamıyor.",
            severity = DtcSeverity.CRITICAL,
            guidance = "Sağ arka ABS sensörünü, kablo soketini ve aks/porya manyetik halkasını kontrol edin."
        ),
        "48279B" to DtcCatalogEntry(
            title = "EMF (Elektronik Park Freni): Sağ Kaliper Aktüatör Hatası",
            detail = "Arka sağ el freni motoru yüksek akım veya açık devre.",
            severity = DtcSeverity.CRITICAL,
            guidance = "Arka sağ kaliper EPB soketini ve kablodaki kırığı kontrol edin."
        ),
        "21F06E" to DtcCatalogEntry(
            title = "SME (HV Batarya): Hücre Voltaj Dengesizliği Uyarısı",
            detail = "Yüksek voltaj batarya paketinde hücre farkı > 50 mV.",
            severity = DtcSeverity.WARNING,
            guidance = "Tam şarj döngüsü sonrası hücre dengeleme ve SOH değerlerini kontrol edin."
        ),
        "21F139" to DtcCatalogEntry(
            title = "SME (HV Batarya): Yüksek Voltaj İzolasyon Direnci Düşük",
            detail = "HV hat ile şasi arası izolasyon < 500 kOhm eşiğine yaklaştı.",
            severity = DtcSeverity.CRITICAL,
            guidance = "HV turuncu kablolarda ve klima kompresörü izolasyonunda yetkili servis testi yapın."
        )
    )

    fun lookup(codeHex: String): DtcCatalogEntry? {
        val key = codeHex.trim().uppercase()
        return externalEntries[key] ?: entries[key]
    }

    fun search(query: String): List<Pair<String, DtcCatalogEntry>> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()
        val combined = entries + externalEntries
        return combined.entries
            .filter { (code, entry) ->
                code.contains(q, ignoreCase = true) ||
                    entry.title.contains(q, ignoreCase = true) ||
                    entry.detail.contains(q, ignoreCase = true)
            }
            .map { it.key to it.value }
            .take(10)
    }
}
