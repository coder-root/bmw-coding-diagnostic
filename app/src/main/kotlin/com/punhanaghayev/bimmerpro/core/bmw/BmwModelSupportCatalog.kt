package com.punhanaghayev.bimmerpro.core.bmw

enum class ModelSupportLevel(val label: String) {
    FULL("Tam Desteklendi"),
    PARTIAL_SECURE_CODING("Kısmi (2023+ Secure Coding 2.0)"),
    K_LINE_HARDWARE("K-Line Donanım Gerekli (2001-2007)")
}

data class SupportedBmwModelEntry(
    val categoryName: String,
    val chassisCodes: String,
    val yearRange: String,
    val family: SeriesFamily,
    val supportLevel: ModelSupportLevel,
    val diagnosticSummary: String,
    val codingSummary: String,
    val requiredAdapter: String,
    val limitationNote: String? = null,
    val howToSupportSolution: String? = null
)

object BmwModelSupportCatalog {

    val allCategories: List<String> = listOf(
        "Tümü",
        "1 Series & 1M",
        "2 Series (Coupé / Gran Coupé / Active Tourer)",
        "3 Series & M3",
        "4 Series & M4",
        "5 Series & M5",
        "6 Series & M6",
        "7 Series",
        "8 Series & M8",
        "X Series (X1 - X7, XM)",
        "Z Series (Z4)",
        "i Series (Elektrikli i3, i4, i5, i7, i8, iX, iX1-3)"
    )

    val allModels: List<SupportedBmwModelEntry> = listOf(
        // ==================== 1 SERIES ====================
        SupportedBmwModelEntry(
            categoryName = "1 Series & 1M",
            chassisCodes = "F70",
            yearRange = "2024+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400 / UDS)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0 Korumalı)",
            requiredAdapter = "DoIP / ENET Wi-Fi Adaptörü (MHD, BM3, Thor)",
            limitationNote = "BMW OS 9 ve yeni BDC modülleri, FDL kodlama yazımında BMW merkezi sunucu imzası (X.509 token) talep eder.",
            howToSupportSolution = "Diagnostik, DTC silme ve canlı telemetri %100 çalışır. Kodlama için BMW AOS sunucu oturum tokeni veya korumasız alt ECU'lar kodlanabilir."
        ),
        SupportedBmwModelEntry(
            categoryName = "1 Series & 1M",
            chassisCodes = "F40",
            yearRange = "2019-2024",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (DoIP / ENET HSFZ)",
            codingSummary = "Tam FDL + Easy Mode + VO (2019-2022) / 2023+ Kısmi",
            requiredAdapter = "ENET Kablo / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "1 Series & 1M",
            chassisCodes = "F20, F21",
            yearRange = "2011-2019",
            family = SeriesFamily.F_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS ISO 14229)",
            codingSummary = "Tam FDL + Easy Mode + FA/VO Sipariş Kodlama",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM / K+DCAN",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "1 Series & 1M",
            chassisCodes = "E81, E82, E87, E88",
            yearRange = "2008-2013",
            family = SeriesFamily.E_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (K+DCAN D-CAN 500kbps / KWP2000)",
            codingSummary = "Tam FDL & TRC Kodlama (CAS3, FRM2/3, N47/N54)",
            requiredAdapter = "K+DCAN USB (OTG) / Vgate vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "1 Series & 1M",
            chassisCodes = "E81, E82, E87, E88",
            yearRange = "2004-2007",
            family = SeriesFamily.E_SERIES,
            supportLevel = ModelSupportLevel.K_LINE_HARDWARE,
            diagnosticSummary = "Tam Destek (K-Line / KWP2000)",
            codingSummary = "Tam FDL & TRC Kodlama",
            requiredAdapter = "K+DCAN USB (Pin 7+8 Köprülü Anahtarlı Adaptör)",
            limitationNote = "2007 öncesi araçlarda OBD soketinde D-CAN hattı yoktur; teşhis Pin 7 ve 8 üzerinden K-Line ile yürütülür.",
            howToSupportSolution = "Standart ENET çalışmaz. Üzerinde switch bulunan ve Pin 7+8 köprüleyen K+DCAN USB adaptörü kullanılmalıdır."
        ),

        // ==================== 2 SERIES ====================
        SupportedBmwModelEntry(
            categoryName = "2 Series (Coupé / Gran Coupé / Active Tourer)",
            chassisCodes = "G42, G87",
            yearRange = "2021+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "2021-2022 Tam / 2023+ iDrive 8 Secure Coding 2.0",
            requiredAdapter = "DoIP / ENET Wi-Fi Adaptörü",
            limitationNote = "2023 sonrası iDrive 8/8.5 MGU ekran ve BDC3 modülü kodlama kilidine sahiptir.",
            howToSupportSolution = "Tüm ECU arıza tarama, canlı sensörler, CBS sıfırlama %100 açıktır."
        ),
        SupportedBmwModelEntry(
            categoryName = "2 Series (Coupé / Gran Coupé / Active Tourer)",
            chassisCodes = "F22, F23, F87",
            yearRange = "2013-2021",
            family = SeriesFamily.F_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS)",
            codingSummary = "Tam FDL + Easy Mode + FA/VO (M2 Dahil)",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "2 Series (Coupé / Gran Coupé / Active Tourer)",
            chassisCodes = "F74",
            yearRange = "2025+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0)",
            requiredAdapter = "DoIP Wi-Fi / Ethernet Adaptörü",
            limitationNote = "BMW OS 9 bulut güvenlik protokolü devrededir.",
            howToSupportSolution = "Diagnostik %100 desteklenir."
        ),
        SupportedBmwModelEntry(
            categoryName = "2 Series (Coupé / Gran Coupé / Active Tourer)",
            chassisCodes = "F44",
            yearRange = "2020-2024",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (DoIP / ENET HSFZ)",
            codingSummary = "Tam FDL + Easy Mode (2020-2022) / 2023+ Kısmi",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "2 Series (Coupé / Gran Coupé / Active Tourer)",
            chassisCodes = "U06",
            yearRange = "2021+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (iDrive 8/9)",
            requiredAdapter = "DoIP / ENET Wi-Fi Adaptörü",
            limitationNote = "FAAR platformu iDrive 8 eğlence ve gövde kontrol ünitesi imza gerektirir.",
            howToSupportSolution = "Arıza tespit ve servis fonksiyonları tam etkindir."
        ),
        SupportedBmwModelEntry(
            categoryName = "2 Series (Coupé / Gran Coupé / Active Tourer)",
            chassisCodes = "F45",
            yearRange = "2014-2021",
            family = SeriesFamily.F_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "2 Series (Coupé / Gran Coupé / Active Tourer)",
            chassisCodes = "F46",
            yearRange = "2015-2022",
            family = SeriesFamily.F_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),

        // ==================== 3 SERIES ====================
        SupportedBmwModelEntry(
            categoryName = "3 Series & M3",
            chassisCodes = "G20, G21, G80, G81",
            yearRange = "2019+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (DoIP / ENET HSFZ)",
            codingSummary = "2019-2022 Tam Kodlama / 2023+ LCI Secure Coding 2.0",
            requiredAdapter = "DoIP / ENET Wi-Fi / vLinker BM Wi-Fi",
            limitationNote = "LCI (iDrive 8 Curved Display) modellerde BDC3 ve MGU üniteleri Secure Coding 2.0 korumalıdır.",
            howToSupportSolution = "Pre-LCI %100 tam kodlanır. LCI modellerde teşhis ve servis %100 aktiftir."
        ),
        SupportedBmwModelEntry(
            categoryName = "3 Series & M3",
            chassisCodes = "F30, F31, F34, F35, F80",
            yearRange = "2011-2019",
            family = SeriesFamily.F_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS ISO 14229)",
            codingSummary = "Tam FDL + Easy Mode + FA/VO (M3 Dahil)",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "3 Series & M3",
            chassisCodes = "E90, E91, E92, E93",
            yearRange = "2008-2013",
            family = SeriesFamily.E_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (K+DCAN D-CAN / KWP2000)",
            codingSummary = "Tam FDL & TRC Kodlama (CAS3+, FRM2/3, CIC)",
            requiredAdapter = "K+DCAN USB (OTG) / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "3 Series & M3",
            chassisCodes = "E90, E91, E92, E93",
            yearRange = "2005-2007",
            family = SeriesFamily.E_SERIES,
            supportLevel = ModelSupportLevel.K_LINE_HARDWARE,
            diagnosticSummary = "Tam Destek (K-Line / KWP2000)",
            codingSummary = "Tam FDL & TRC Kodlama (CAS3, FRM1, CCC)",
            requiredAdapter = "K+DCAN USB (Pin 7+8 Köprülü)",
            limitationNote = "2007/03 öncesi araçlar K-Line haberleşmesi kullanır.",
            howToSupportSolution = "Pin 7+8 switchli K+DCAN adaptörü gereklidir."
        ),

        // ==================== 4 SERIES ====================
        SupportedBmwModelEntry(
            categoryName = "4 Series & M4",
            chassisCodes = "G22, G23, G26, G82, G83",
            yearRange = "2020+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (DoIP / ENET HSFZ)",
            codingSummary = "2020-2022 Tam / 2023+ LCI Secure Coding 2.0",
            requiredAdapter = "DoIP / ENET Wi-Fi / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "4 Series & M4",
            chassisCodes = "F32, F33, F36, F82, F83",
            yearRange = "2013+",
            family = SeriesFamily.F_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS)",
            codingSummary = "Tam FDL + Easy Mode + FA/VO (M4 Dahil)",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),

        // ==================== 5 SERIES ====================
        SupportedBmwModelEntry(
            categoryName = "5 Series & M5",
            chassisCodes = "G60, G61, G90, G99",
            yearRange = "2023+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0)",
            requiredAdapter = "DoIP Wi-Fi / Ethernet Adaptörü",
            limitationNote = "Yeni 5 Serisi (iDrive 8.5) tüm kontrol ünitelerinde sertifika koruması kullanır.",
            howToSupportSolution = "Tüm arıza kodları, canlı veriler ve servis işlemleri aktiftir."
        ),
        SupportedBmwModelEntry(
            categoryName = "5 Series & M5",
            chassisCodes = "G30, G31, F90",
            yearRange = "2017-2023",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (DoIP / ENET HSFZ)",
            codingSummary = "Tam FDL + Easy Mode + FA/VO (M5 Dahil)",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "5 Series & M5",
            chassisCodes = "F07, F10, F11, F18",
            yearRange = "2010-2017",
            family = SeriesFamily.F_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS ISO 14229)",
            codingSummary = "Tam FDL + Easy Mode + FA/VO (CAS4, NBT, 2TB, vb.)",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi / K+DCAN",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "5 Series & M5",
            chassisCodes = "E60, E61",
            yearRange = "2008-2010",
            family = SeriesFamily.E_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (K+DCAN D-CAN / KWP2000)",
            codingSummary = "Tam FDL & TRC Kodlama (CAS3, LM2, CIC)",
            requiredAdapter = "K+DCAN USB (OTG) / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "5 Series & M5",
            chassisCodes = "E60, E61",
            yearRange = "2003-2007",
            family = SeriesFamily.E_SERIES,
            supportLevel = ModelSupportLevel.K_LINE_HARDWARE,
            diagnosticSummary = "Tam Destek (K-Line / KWP2000)",
            codingSummary = "Tam FDL & TRC Kodlama (CAS2, LM1, CCC)",
            requiredAdapter = "K+DCAN USB (Pin 7+8 Köprülü)",
            limitationNote = "2007/03 öncesi araçlarda D-CAN protokolü bulunmaz.",
            howToSupportSolution = "Pin 7 ve 8'i köprüleyen K+DCAN donanımı gereklidir."
        ),

        // ==================== 6 SERIES ====================
        SupportedBmwModelEntry(
            categoryName = "6 Series & M6",
            chassisCodes = "G32",
            yearRange = "2017-2023",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (DoIP / ENET HSFZ)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "6 Series & M6",
            chassisCodes = "F06, F12, F13",
            yearRange = "2011-2018",
            family = SeriesFamily.F_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "6 Series & M6",
            chassisCodes = "E63, E64",
            yearRange = "2008-2010",
            family = SeriesFamily.E_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (K+DCAN D-CAN)",
            codingSummary = "Tam FDL & TRC Kodlama (LM2, CAS3)",
            requiredAdapter = "K+DCAN USB / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "6 Series & M6",
            chassisCodes = "E63, E64",
            yearRange = "2003-2007",
            family = SeriesFamily.E_SERIES,
            supportLevel = ModelSupportLevel.K_LINE_HARDWARE,
            diagnosticSummary = "Tam Destek (K-Line / KWP2000)",
            codingSummary = "Tam FDL & TRC Kodlama",
            requiredAdapter = "K+DCAN USB (Pin 7+8 Köprülü)",
            limitationNote = "K-Line haberleşmesi gerektirir.",
            howToSupportSolution = "Pin 7+8 köprülü adaptör takılmalıdır."
        ),

        // ==================== 7 SERIES ====================
        SupportedBmwModelEntry(
            categoryName = "7 Series",
            chassisCodes = "G70",
            yearRange = "2022+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0)",
            requiredAdapter = "DoIP Wi-Fi / Ethernet Adaptörü",
            limitationNote = "iDrive 8/8.5 Theatre Screen ve BDC3 modülü imza korumalıdır.",
            howToSupportSolution = "Diagnostik, hata analizi ve servis fonksiyonları tam etkindir."
        ),
        SupportedBmwModelEntry(
            categoryName = "7 Series",
            chassisCodes = "G11, G12",
            yearRange = "2015-2022",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (DoIP / ENET HSFZ)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "7 Series",
            chassisCodes = "F01, F02, F03, F04",
            yearRange = "2008-2015",
            family = SeriesFamily.F_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "7 Series",
            chassisCodes = "E65, E66, E67, E68",
            yearRange = "2001-2008",
            family = SeriesFamily.E_SERIES,
            supportLevel = ModelSupportLevel.K_LINE_HARDWARE,
            diagnosticSummary = "Tam Destek (K-Line / D-CAN / KWP2000)",
            codingSummary = "Tam FDL & TRC Kodlama (ZGM, CAS1/CAS2)",
            requiredAdapter = "K+DCAN USB (Pin 7+8 Köprülü)",
            limitationNote = "Erken E65 modelleri çift K-Line hattı ve ZGM ağ geçidi kullanır.",
            howToSupportSolution = "Pin 7+8 köprülü adaptör ile teşhis ve kodlama yapılabilir."
        ),

        // ==================== 8 SERIES ====================
        SupportedBmwModelEntry(
            categoryName = "8 Series & M8",
            chassisCodes = "G14, G15, G16, F91, F92, F93",
            yearRange = "2018+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (DoIP / ENET HSFZ)",
            codingSummary = "2018-2022 Tam / 2023+ LCI Secure Coding 2.0",
            requiredAdapter = "DoIP / ENET Wi-Fi / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),

        // ==================== X SERIES ====================
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "U11 (X1)",
            yearRange = "2022+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0)",
            requiredAdapter = "DoIP Wi-Fi / Ethernet Adaptörü",
            limitationNote = "iDrive 8/9 FAAR mimarisi imza korumalıdır.",
            howToSupportSolution = "Diagnostik ve canlı sensörler tam açıktır."
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "F48 (X1)",
            yearRange = "2015-2022",
            family = SeriesFamily.F_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "E84 (X1)",
            yearRange = "2009-2015",
            family = SeriesFamily.E_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (K+DCAN D-CAN / KWP2000)",
            codingSummary = "Tam FDL & TRC Kodlama (CAS3, FRM3)",
            requiredAdapter = "K+DCAN USB / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "U10 (X2)",
            yearRange = "2024+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0)",
            requiredAdapter = "DoIP Wi-Fi / Ethernet Adaptörü",
            limitationNote = "OS 9 koruması devrededir.",
            howToSupportSolution = "Teşhis ve arıza tespiti tam açıktır."
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "F39 (X2)",
            yearRange = "2018-2023",
            family = SeriesFamily.F_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "G45 (X3)",
            yearRange = "2024+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0)",
            requiredAdapter = "DoIP Wi-Fi / Ethernet Adaptörü",
            limitationNote = "Yeni X3 OS 9 işletim sistemi kullanır.",
            howToSupportSolution = "DoIP teşhis tam fonksiyoneldir."
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "G01, F97 (X3, X3 M)",
            yearRange = "2017-2024",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (DoIP / ENET HSFZ)",
            codingSummary = "2017-2022 Tam / 2023+ LCI Kısmi",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "F25 (X3)",
            yearRange = "2010-2017",
            family = SeriesFamily.F_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "G02, F98 (X4, X4 M)",
            yearRange = "2018+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (DoIP / ENET HSFZ)",
            codingSummary = "2018-2022 Tam / 2023+ Kısmi",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "F26 (X4)",
            yearRange = "2014-2018",
            family = SeriesFamily.F_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "G05, F95 (X5, X5 M LCI)",
            yearRange = "2023+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0)",
            requiredAdapter = "DoIP Wi-Fi / Ethernet Adaptörü",
            limitationNote = "iDrive 8.5 ekran ve BDC3 modülü kodlama kilidi içerir.",
            howToSupportSolution = "Teşhis, DPF rejenerasyonu ve ZF8 analizi tam desteklenir."
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "G05, F95 (X5, X5 M PRE-LCI)",
            yearRange = "2018-2023",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (DoIP / ENET HSFZ)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "F15, F85 (X5, X5 M)",
            yearRange = "2013-2018",
            family = SeriesFamily.F_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "E70 (X5)",
            yearRange = "2008-2013",
            family = SeriesFamily.E_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (K+DCAN D-CAN)",
            codingSummary = "Tam FDL & TRC Kodlama (CAS3, FRM3)",
            requiredAdapter = "K+DCAN USB / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "E70 (X5)",
            yearRange = "2006-2007",
            family = SeriesFamily.E_SERIES,
            supportLevel = ModelSupportLevel.K_LINE_HARDWARE,
            diagnosticSummary = "Tam Destek (K-Line / KWP2000)",
            codingSummary = "Tam FDL & TRC Kodlama",
            requiredAdapter = "K+DCAN USB (Pin 7+8 Köprülü)",
            limitationNote = "K-Line haberleşmesi gerektirir.",
            howToSupportSolution = "Pin 7+8 köprülü adaptör kullanılmalıdır."
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "G06, F96 (X6, X6 M LCI)",
            yearRange = "2023+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0)",
            requiredAdapter = "DoIP Wi-Fi / Ethernet Adaptörü",
            limitationNote = "iDrive 8.5 imza koruması devrededir.",
            howToSupportSolution = "Teşhis ve servis tam açıktır."
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "G06, F96 (X6, X6 M PRE-LCI)",
            yearRange = "2019-2023",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (DoIP / ENET HSFZ)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "F16, F86 (X6, X6 M)",
            yearRange = "2014-2019",
            family = SeriesFamily.F_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "E71, E72 (X6, X6 M)",
            yearRange = "2008-2014",
            family = SeriesFamily.E_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (K+DCAN D-CAN)",
            codingSummary = "Tam FDL & TRC Kodlama (CAS3, FRM3)",
            requiredAdapter = "K+DCAN USB / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "G07 (X7 LCI)",
            yearRange = "2022+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0)",
            requiredAdapter = "DoIP Wi-Fi / Ethernet Adaptörü",
            limitationNote = "iDrive 8/8.5 imza korumalıdır.",
            howToSupportSolution = "Teşhis ve bakım tam desteklenir."
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "G07 (X7 PRE-LCI)",
            yearRange = "2019-2022",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (DoIP / ENET HSFZ)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "X Series (X1 - X7, XM)",
            chassisCodes = "G09 (XM)",
            yearRange = "2022+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0)",
            requiredAdapter = "DoIP Wi-Fi / Ethernet Adaptörü",
            limitationNote = "MGU ve BDC3 Secure Coding 2.0 korumalıdır.",
            howToSupportSolution = "Teşhis, arıza analizi ve DPF/şebeke sağlık kontrolü tam açıktır."
        ),

        // ==================== Z SERIES ====================
        SupportedBmwModelEntry(
            categoryName = "Z Series (Z4)",
            chassisCodes = "G29",
            yearRange = "2018+",
            family = SeriesFamily.G_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (DoIP / ENET HSFZ)",
            codingSummary = "2018-2022 Tam / 2023+ Kısmi",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "Z Series (Z4)",
            chassisCodes = "E89",
            yearRange = "2009-2016",
            family = SeriesFamily.E_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (K+DCAN D-CAN)",
            codingSummary = "Tam FDL & TRC Kodlama (CAS3, FRM3)",
            requiredAdapter = "K+DCAN USB / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "Z Series (Z4)",
            chassisCodes = "E85",
            yearRange = "2002-2008",
            family = SeriesFamily.E_SERIES,
            supportLevel = ModelSupportLevel.K_LINE_HARDWARE,
            diagnosticSummary = "Tam Destek (K-Line / KWP2000)",
            codingSummary = "Tam FDL & TRC Kodlama",
            requiredAdapter = "K+DCAN USB (Pin 7+8 Köprülü)",
            limitationNote = "E85 Z4 modelleri K-Line üzerinden haberleşir.",
            howToSupportSolution = "Pin 7+8 köprülü adaptör ile bağlanılır."
        ),

        // ==================== i SERIES (ELECTRIC / HYBRID) ====================
        SupportedBmwModelEntry(
            categoryName = "i Series (Elektrikli i3, i4, i5, i7, i8, iX, iX1-3)",
            chassisCodes = "I01 (i3)",
            yearRange = "2013+",
            family = SeriesFamily.I_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS)",
            codingSummary = "Tam FDL + Easy Mode + VO (Rex / BEV Dahil)",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "i Series (Elektrikli i3, i4, i5, i7, i8, iX, iX1-3)",
            chassisCodes = "G26E (i4)",
            yearRange = "2021+",
            family = SeriesFamily.I_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "2021-2022 Tam / 2023+ Secure Coding 2.0",
            requiredAdapter = "DoIP / ENET Wi-Fi Adaptörü",
            limitationNote = "iDrive 8 kavisli ekran modülü imza korumalıdır.",
            howToSupportSolution = "Yüksek voltajlı batarya sağlığı ve teşhis tam etkindir."
        ),
        SupportedBmwModelEntry(
            categoryName = "i Series (Elektrikli i3, i4, i5, i7, i8, iX, iX1-3)",
            chassisCodes = "G60E, G61E (i5)",
            yearRange = "2023+",
            family = SeriesFamily.I_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0)",
            requiredAdapter = "DoIP Wi-Fi / Ethernet Adaptörü",
            limitationNote = "BMW OS 8.5 güvenlik koruması devrededir.",
            howToSupportSolution = "Teşhis ve batarya sağlık analizi tam desteklenir."
        ),
        SupportedBmwModelEntry(
            categoryName = "i Series (Elektrikli i3, i4, i5, i7, i8, iX, iX1-3)",
            chassisCodes = "G70E (i7)",
            yearRange = "2022+",
            family = SeriesFamily.I_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0)",
            requiredAdapter = "DoIP Wi-Fi / Ethernet Adaptörü",
            limitationNote = "iDrive 8/8.5 imza korumalıdır.",
            howToSupportSolution = "Teşhis ve servis tam açıktır."
        ),
        SupportedBmwModelEntry(
            categoryName = "i Series (Elektrikli i3, i4, i5, i7, i8, iX, iX1-3)",
            chassisCodes = "I12, I15 (i8)",
            yearRange = "2013+",
            family = SeriesFamily.I_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (ENET HSFZ / UDS)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "i Series (Elektrikli i3, i4, i5, i7, i8, iX, iX1-3)",
            chassisCodes = "U11E (iX1)",
            yearRange = "2022+",
            family = SeriesFamily.I_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0)",
            requiredAdapter = "DoIP Wi-Fi / Ethernet Adaptörü",
            limitationNote = "FAAR OS 9 imza koruması vardır.",
            howToSupportSolution = "Teşhis tam etkindir."
        ),
        SupportedBmwModelEntry(
            categoryName = "i Series (Elektrikli i3, i4, i5, i7, i8, iX, iX1-3)",
            chassisCodes = "U10E (iX2)",
            yearRange = "2024+",
            family = SeriesFamily.I_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0)",
            requiredAdapter = "DoIP Wi-Fi / Ethernet Adaptörü",
            limitationNote = "OS 9 güvenlik koruması devrededir.",
            howToSupportSolution = "Teşhis tam etkindir."
        ),
        SupportedBmwModelEntry(
            categoryName = "i Series (Elektrikli i3, i4, i5, i7, i8, iX, iX1-3)",
            chassisCodes = "G08 (iX3)",
            yearRange = "2021+",
            family = SeriesFamily.I_SERIES,
            supportLevel = ModelSupportLevel.FULL,
            diagnosticSummary = "Tam Destek (DoIP / ENET HSFZ)",
            codingSummary = "Tam FDL + Easy Mode + VO",
            requiredAdapter = "ENET / Wi-Fi ENET / vLinker BM Wi-Fi",
            limitationNote = null,
            howToSupportSolution = null
        ),
        SupportedBmwModelEntry(
            categoryName = "i Series (Elektrikli i3, i4, i5, i7, i8, iX, iX1-3)",
            chassisCodes = "I20 (iX)",
            yearRange = "2021+",
            family = SeriesFamily.I_SERIES,
            supportLevel = ModelSupportLevel.PARTIAL_SECURE_CODING,
            diagnosticSummary = "Tam Destek (DoIP ISO 13400)",
            codingSummary = "Kısmi Destek (Secure Coding 2.0)",
            requiredAdapter = "DoIP Wi-Fi / Ethernet Adaptörü",
            limitationNote = "BDC3 ve MGU üniteleri imza gerektirir.",
            howToSupportSolution = "Diagnostik ve yüksek voltaj batarya analizi tam çalışır."
        )
    )

    fun filterByCategory(category: String): List<SupportedBmwModelEntry> {
        if (category == "Tümü") return allModels
        return allModels.filter { it.categoryName == category }
    }
}
