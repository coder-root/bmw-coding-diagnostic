package com.punhanaghayev.bimmerpro.core.bmw

import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SalapaOptionDto(
    val name: String,
    val en: String? = null,
    val de: String? = null,
    val fr: String? = null,
    val es: String? = null,
    val it: String? = null
)

/**
 * Authentic BMW FA/VO (SALAPA) dependency & retrofit mapping engine (E-Sys standard).
 * Resolves which ECUs must be VO-coded when adding or removing factory equipment codes.
 */
object VoDependencyMapper {

    private val json = Json { ignoreUnknownKeys = true }
    private var cachedSalapaMap: Map<String, SalapaOptionDto>? = null

    // Comprehensive BMW factory option to affected ECUs mapping
    private val optionToEcus: Map<String, List<Pair<Int, String>>> = mapOf(
        // Transmission & Drivetrain
        "2TB" to listOf(EcuAddress.EGS to "EGS (ZF8)", EcuAddress.DSC to "DSC/ICM", EcuAddress.KOMBI to "KOMBI", EcuAddress.DME_DDE to "DME/DDE"),
        "2TE" to listOf(EcuAddress.EGS to "EGS", EcuAddress.FEM_CAS to "FEM/CAS"),
        "2VF" to listOf(EcuAddress.DSC to "DSC", EcuAddress.ZGW_BDC to "ICM/VDC"),
        "2VL" to listOf(EcuAddress.DSC to "DSC", EcuAddress.ZGW_BDC to "EPS"),

        // Lighting
        "552" to listOf(EcuAddress.FEM_CAS to "FEM/BDC", EcuAddress.ZGW_BDC to "ZGW", EcuAddress.KOMBI to "KOMBI"),
        "5A1" to listOf(EcuAddress.FEM_CAS to "FEM/FRM"),
        "5AC" to listOf(EcuAddress.FEM_CAS to "FEM/FRM", EcuAddress.KOMBI to "KAFAS/KOMBI"),

        // Multimedia & Navigation
        "609" to listOf(EcuAddress.HU_MEDIA to "HU_NBT/MGU", EcuAddress.KOMBI to "KOMBI", EcuAddress.ZGW_BDC to "ZGW"),
        "606" to listOf(EcuAddress.HU_MEDIA to "HU_ENTRYNAV", EcuAddress.KOMBI to "KOMBI"),
        "6WB" to listOf(EcuAddress.KOMBI to "KOMBI (6WB)", EcuAddress.HU_MEDIA to "HU_NBT/MGU"),
        "6WA" to listOf(EcuAddress.KOMBI to "KOMBI (6WA)", EcuAddress.HU_MEDIA to "HU_NBT"),
        "6NS" to listOf(EcuAddress.HU_MEDIA to "HU_NBT", EcuAddress.FEM_CAS to "FEM/BDC"),
        "6CP" to listOf(EcuAddress.HU_MEDIA to "HU_NBT_EVO/MGU (Apple CarPlay)"),
        "688" to listOf(EcuAddress.HU_MEDIA to "HU_NBT/MGU", EcuAddress.ZGW_BDC to "AMPT (Harman Kardon)"),
        "676" to listOf(EcuAddress.HU_MEDIA to "HU_NBT/MGU (HiFi Audio)"),
        "610" to listOf(EcuAddress.KOMBI to "KOMBI", EcuAddress.HU_MEDIA to "HU_NBT (HUD)"),

        // Driver Assistance & Cameras
        "3AG" to listOf(EcuAddress.HU_MEDIA to "HU_NBT/MGU", EcuAddress.FEM_CAS to "REM/TRSVC"),
        "508" to listOf(EcuAddress.FEM_CAS to "REM/PDC", EcuAddress.HU_MEDIA to "HU_NBT/MGU"),
        "5AS" to listOf(EcuAddress.KOMBI to "KOMBI", EcuAddress.DSC to "DSC", EcuAddress.HU_MEDIA to "HU_NBT/MGU", EcuAddress.ZGW_BDC to "KAFAS"),
        "5AT" to listOf(EcuAddress.KOMBI to "KOMBI", EcuAddress.DSC to "DSC", EcuAddress.HU_MEDIA to "HU_MGU", EcuAddress.ZGW_BDC to "KAFAS/SAS"),
        "5DF" to listOf(EcuAddress.DSC to "DSC", EcuAddress.KOMBI to "KOMBI", EcuAddress.DME_DDE to "DME/DDE", EcuAddress.ZGW_BDC to "ACC/ICM"),
        "5DL" to listOf(EcuAddress.HU_MEDIA to "HU_NBT", EcuAddress.FEM_CAS to "TRSVC Surround View"),

        // Comfort & Access
        "322" to listOf(EcuAddress.FEM_CAS to "CAS4/BDC (Comfort Access)"),
        "316" to listOf(EcuAddress.FEM_CAS to "HKFM (Elektrikli Bagaj)", EcuAddress.ZGW_BDC to "CAS4/BDC"),
        "248" to listOf(EcuAddress.FEM_CAS to "FEM/SZL (Direksiyon Isıtma)"),
        "403" to listOf(EcuAddress.FEM_CAS to "FZD (Cam Sunroof)", EcuAddress.ZGW_BDC to "CAS4/BDC"),
        "456" to listOf(EcuAddress.FEM_CAS to "SMFA/SMBF (Konfor Koltuk)", EcuAddress.ZGW_BDC to "BDC"),
        "481" to listOf(EcuAddress.FEM_CAS to "SMFA/SMBF (Spor Koltuk)"),
        "4UR" to listOf(EcuAddress.FEM_CAS to "FEM/BDC (Ambiyans Aydınlatma)"),
        "534" to listOf(EcuAddress.IHKA to "IHKA (2-Bölge Otomatik Klima)")
    )

    private val builtinDescriptions: Map<String, String> = mapOf(
        "2TB" to "Sport Otomatik Şanzıman (Steptronic Sport / Kulakçık & Hızlı Vites)",
        "2TE" to "Direksiyondan Vites Değiştirme Kulakçıkları",
        "2VF" to "Adaptif M Süspansiyon (EDC)",
        "2VL" to "Değişken Spor Direksiyon (Variable Sport Steering)",
        "3AG" to "Geri Görüş Kamerası (Reversing Camera)",
        "316" to "Otomatik Bagaj Kapağı (Automatic Tailgate)",
        "322" to "Konfor Erişim Sistemi (Comfort Access / Anahtarsız Giriş)",
        "403" to "Elektrikli Cam Tavan / Sunroof",
        "456" to "Hafızalı Elektrikli Konfor Koltuklar (Comfort Seats)",
        "481" to "Sürücü & Yolcu Spor Koltuklar (Sport Seats)",
        "4UR" to "Gelişmiş Ortam Ambiyans Aydınlatması (Ambient Light)",
        "508" to "Ön ve Arka Park Mesafe Kontrolü (PDC)",
        "534" to "Gelişmiş Otomatik Klima (2-Bölge IHKA)",
        "552" to "Adaptif LED Farlar (Adaptive Full LED Headlights)",
        "5A1" to "LED Sis Farları (LED Fog Lights)",
        "5AC" to "Otomatik Uzun Far Asistanı (High-Beam Assistant)",
        "5AS" to "Sürüş Asistanı (Driving Assistant - Şerit Takip / Çarpışma Önleme)",
        "5AT" to "Sürüş Asistanı Plus (Driving Assistant Plus / Stop&Go ACC)",
        "5DF" to "Stop&Go Fonksiyonlu Aktif Hız Sabitleyici (Active Cruise Control)",
        "5DL" to "Çevre Görüş Kamera Sistemi (Surround View)",
        "606" to "Navigasyon Sistemi Business (Navigation Business)",
        "609" to "Navigasyon Sistemi Professional (NBT / MGU Professional)",
        "610" to "Head-Up Display (Ön Cama Bilgi Yansıtma)",
        "676" to "HiFi Hoparlör Sistemi (HiFi Audio System)",
        "688" to "Harman Kardon Surround Ses Sistemi (Harman Kardon Top HiFi)",
        "6CP" to "Apple CarPlay Hazırlığı & Entegrasyonu",
        "6NS" to "Gelişmiş Akıllı Telefon Bağlantısı & Bluetooth Audio",
        "6WA" to "Genişletilmiş Gösterge Paneli (Extended Black Panel)",
        "6WB" to "Çok Fonksiyonlu Dijital Hayalet Gösterge (Multifunctional Digital Display)"
    )

    fun initializeFromDataPack(datapacksDir: File) {
        val file = File(datapacksDir, "bmw_salapa_pack.json")
        if (!file.exists()) return
        runCatching {
            val list = json.decodeFromString<List<SalapaOptionDto>>(file.readText())
            cachedSalapaMap = list.associateBy { it.name.trim().uppercase() }
        }
    }

    fun lookupOptionDescription(voCode: String, lang: String = "en"): String {
        val clean = voCode.trim().uppercase().removePrefix("S")
        val builtin = builtinDescriptions[clean]
        if (builtin != null) return builtin

        val cached = cachedSalapaMap?.get(clean)
        if (cached != null) {
            val translated = when (lang) {
                "de" -> cached.de
                "fr" -> cached.fr
                "es" -> cached.es
                "it" -> cached.it
                else -> cached.en
            }
            if (!translated.isNullOrBlank()) return translated
        }
        return "BMW Fabrika Opsiyonu ($clean)"
    }

    fun affectedEcusFor(voCode: String): List<Pair<Int, String>> {
        val clean = voCode.trim().uppercase().removePrefix("S")
        return optionToEcus[clean] ?: listOf(EcuAddress.FEM_CAS to "CAS4/FEM/BDC", EcuAddress.HU_MEDIA to "HU_MEDIA")
    }

    fun isKnownRetrofit(voCode: String): Boolean {
        val clean = voCode.trim().uppercase().removePrefix("S")
        return optionToEcus.containsKey(clean)
    }
}
