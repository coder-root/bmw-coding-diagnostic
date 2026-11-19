package com.punhanaghayev.bimmerpro.core.bmw

/**
 * Comprehensive BimmerCode / E-Sys style BMW FDL & CAFD coding catalog for F-Series (F10/F30) and G-Series (G30/G20).
 */
object BmwCodingCatalog {

    fun buildPresets(isGSeries: Boolean): List<CodingPreset> {
        val bodyEcu = if (isGSeries) "BDC_BODY" else "CAS4 / FEM"
        val bodyAddr = if (isGSeries) EcuAddress.ZGW_BDC else EcuAddress.FEM_CAS
        val kombiEcu = if (isGSeries) "DKOMBI" else "KOMBI"
        val huEcu = if (isGSeries) "HU_MGU" else "HU_NBT"

        return listOf(
            // DRIVETRAIN / START-STOP
            CodingPreset("start_stop_mem", "Auto Start-Stop Son Durum Hafızası", "Motor kapatıldığında Start-Stop tercihini hatırlar.", CodingCategory.DRIVETRAIN, bodyEcu, bodyAddr, "TCM_MSA_MEMORY", listOf("Kapalı (Fabrika)", "Son Durumu Hatırla"), 0),
            CodingPreset("start_stop_off", "Auto Start-Stop Varsayılan Kapalı", "Her çalıştırmada Start-Stop kapalı başlar.", CodingCategory.DRIVETRAIN, bodyEcu, bodyAddr, "TCM_MSA_DEFAULT_OFF", listOf("Fabrika (Açık)", "Varsayılan Kapalı"), 0),
            CodingPreset("icm_drive_mode", "Başlangıç Sürüş Modu (Eco/Comfort/Sport)", "Kontak açıldığında aracın başlayacağı sürüş modunu seçer.", CodingCategory.DRIVETRAIN, "ICM", EcuAddress.DSC, "ICM_SPORT_INDIVIDUAL", listOf("Comfort (Fabrika)", "Eco Pro", "Sport"), 0),

            // COMFORT & BODY
            CodingPreset("mirror_fold", "Kumandadan Tek Basışta Ayna Katlama", "Konfor kapatma gecikmesini düşürür.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "KOMFORT_SCHLIESSEN", listOf("1.5 sn Gecikme", "0.5 sn Hızlı", "Anında (0.0 sn)"), 0),
            CodingPreset("mirror_fold_lock", "Kilitlemede Otomatik Ayna Katlama", "Kumandadan kilitleyince yan aynaları otomatik katlar.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "ASP_BEIKLAPPEN_BEI_KOMFORTSCHLIESSEN", listOf("Pasif", "Aktif"), 1),
            CodingPreset("reverse_mirror_tilt", "Geri Viteste Sağ Ayna Eğilme Açısı", "Kaldırım parkında sağ aynanın aşağı eğilme oranını ayarlar.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "ASP_BORDSTEINAUTOMATIK_DELTA", listOf("Fabrika (%70)", "Hafif (%40)", "Tam Eğilme (%90)"), 0),
            CodingPreset("window_door_interrupt", "Kapı Açılınca Cam Kapanmasını Durdurma", "Kapı açıldığında tek dokunuş cam kalkışını kesmez.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "FH_TUERAUF_STOP_MAUT", listOf("Durdur (Fabrika)", "Kesintisiz Kapat"), 0),
            CodingPreset("trunk_keyfob_close", "Kumanda & İç Düğmeden Bagaj Kapatma", "Elektrikli bagajı kumandadan basılı tutmadan tek basışla kapatır.", CodingCategory.COMFORT, "HKFM", bodyAddr, "HKFM_SCHALTEINSATZ_FBD", listOf("Basılı Tut (Fabrika)", "Tek Dokunuş Kapat"), 0),
            CodingPreset("unlock_engine_off", "Motor Stop Edince Kapıları Otomatik Aç", "Start-Stop düğmesiyle motor kapatılınca tüm kilitleri açar.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "CLM_UNLOCK_AT_ENGINE_OFF", listOf("Pasif", "Aktif"), 0),
            CodingPreset("radio_off_door", "Kapı Açılınca iDrive & Radyoyu Kapat", "Motor stop edildikten sonra sürücü kapısı açılınca multimedyayı kapatır.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "TC_LOGIC_R_OFF_DOOR", listOf("Pasif", "Aktif"), 0),
            CodingPreset("seatbelt_chime", "Emniyet Kemeri İkaz Sesi (Sürücü)", "ACSM sürücü emniyet kemeri sesli uyarısını yönetir.", CodingCategory.COMFORT, "ACSM", EcuAddress.ACSM, "SBR_PREWARNING_FAHRER", listOf("Aktif (Fabrika)", "Kapalı"), 0),
            CodingPreset("seatbelt_chime_pass", "Emniyet Kemeri İkaz Sesi (Yolcu)", "ACSM ön yolcu emniyet kemeri sesli uyarısını yönetir.", CodingCategory.COMFORT, "ACSM", EcuAddress.ACSM, "SBR_PREWARNING_BEIFAHRER", listOf("Aktif (Fabrika)", "Kapalı"), 0),
            CodingPreset("ihka_ac_memory", "Klima (IHKA) Son Durum Hafızası", "Klima ve A/C kompresör durumunu kontak sonrası hatırlar.", CodingCategory.COMFORT, "IHKA", EcuAddress.IHKA, "MEMORY_OFF", listOf("Fabrika", "Son Durumu Hatırla"), 1),

            // LIGHTING
            CodingPreset("angel_eyes_drl", "Angel Eyes / DRL Gündüz Parlaklığı", "Kısalar açıkken LED halkaların parlaklık oranını ayarlar.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "TFL_MODUS_BRIGHTNESS", listOf("%50 Fabrika", "%80 Dengeli", "%100 Tam Parlaklık"), 0),
            CodingPreset("rear_drl", "Arka Stop LED'leri Gündüz Aktif", "Gündüz farlarıyla birlikte arka stopların da yanmasını sağlar.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "TAGFAHRLICHT_ECE_HINTEN", listOf("Kapalı (Fabrika)", "Aktif"), 0),
            CodingPreset("turn_signal_count", "Tek Dokunuş Sinyal Sayısı (3x / 5x / 7x)", "Sinyal koluna hafif dokunuşta yanıp sönme sayısını değiştirir.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "BLINKZYKLEN_ANZAHL_TIPP", listOf("3 Kez (Fabrika)", "5 Kez", "7 Kez"), 0),
            CodingPreset("welcome_lights_time", "Karşılama Işıkları (Welcome Lights) Süresi", "Kilit açıldığında far ve kapı kolu LED'lerinin yanma süresini seçer.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "WL_TIMEOUT", listOf("20 sn (Fabrika)", "40 sn", "60 sn"), 0),
            CodingPreset("door_leds_reverse", "Geri Viteste Kapı Kolu LED'leri", "Geri vitese takıldığında dış kapı kolu aydınlatmalarını yakar.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "OVT_BEI_RUECKFAHRT", listOf("Kapalı (Fabrika)", "Aktif"), 0),

            // INSTRUMENT CLUSTER & HUD
            CodingPreset("kombi_logo", "Gösterge Açılış & Tema Logosu", "Gösterge paneli açılış logosunu ve yazı tipini değiştirir.", CodingCategory.INSTRUMENT, kombiEcu, EcuAddress.KOMBI, "BMW_LOGO", listOf("Standart AG", "M Performance", "Alpina Tema", "M5 / Motorsport"), 0),
            CodingPreset("digital_speed", "Kadranda Gerçek Dijital Hız (km/h)", "Yol bilgisayarı (BC) döngüsüne büyük dijital hız ekler.", CodingCategory.INSTRUMENT, kombiEcu, EcuAddress.KOMBI, "BC_DIGITAL_V", listOf("Pasif", "Aktif"), 1),
            CodingPreset("true_speed_corr", "Gerçek Hız Sapma Düzeltmesi", "Kadran hızının GPS hızına yakın gerçek değer göstermesini sağlar.", CodingCategory.INSTRUMENT, kombiEcu, EcuAddress.KOMBI, "BC_DIGITAL_V_KORREKTUR", listOf("Fabrika (+4 km/h)", "Gerçek Hız (0 Sapma)"), 0),
            CodingPreset("hud_turn_signals", "Head-Up Display (HUD) Sinyal Gösterimi", "Sinyal oklarını ön cam HUD yansıtmasına ekler.", CodingCategory.INSTRUMENT, kombiEcu, EcuAddress.KOMBI, "HMI_HUD_TURN_INDICATOR", listOf("Kapalı", "Aktif"), 0),
            CodingPreset("hud_entertainment", "HUD Medya & Telefon Listesi", "Direksiyon tekerleğinden değişen şarkı/rehber listesini HUD'a yansıtır.", CodingCategory.INSTRUMENT, kombiEcu, EcuAddress.KOMBI, "HUD_ENTERTAINMENT_ENABLE", listOf("Kapalı", "Aktif"), 1),

            // MULTIMEDIA & IDRIVE
            CodingPreset("video_in_motion", "Hareket Halinde Video (Video in Motion)", "iDrive ekranında sürüş esnasında USB video oynatım kilidini açar.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "SPEEDLOCK_X_KMH_MAX", listOf("Fabrika Kilidi (3 km/h)", "Limitsiz (255 km/h)"), 0, isLockedBySecureCoding = isGSeries),
            CodingPreset("sport_displays", "iDrive Spor Göstergeler (HP & Nm)", "iDrive ekranında anlık beygir gücü ve tork kadranlarını aktif eder.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "EFF_DYN_SPORT_CID", listOf("Pasif", "Aktif (Turuncu/Kırmızı)"), 1, isLockedBySecureCoding = isGSeries),
            CodingPreset("legal_disclaimer", "iDrive Açılış Yasal Uyarı Ekranını Kapat", "Her çalıştırmada çıkan güvenlik uyarısını kaldırır.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "LEGAL_DISCLAIMER_TIME", listOf("Aktif (Fabrika)", "Kapalı (0 sn)"), 0, isLockedBySecureCoding = isGSeries),
            CodingPreset("chime_sound_set", "iDrive Uyarı Sesi Paketi (BMW / Rolls-Royce)", "Park sensörü ve sistem ikaz seslerinin tonunu değiştirir.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "SOUND_SIGNAL_SET", listOf("BMW Standart", "BMW i", "Rolls-Royce", "Mini"), 0, isLockedBySecureCoding = isGSeries),
            CodingPreset("usb_video_codecs", "USB Video Formatları (XviD / MP4 / MKV)", "USB bellekten ek video kodeklerinin oynatılmasını açar.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "ENT_CODEC_XVID", listOf("Fabrika", "Tüm Kodekler Aktif"), 0, isLockedBySecureCoding = isGSeries),
            CodingPreset("idrive_boot_logo", "iDrive Başlangıç Animasyonu", "Ekran açılış logosunu M Performance veya Alpina yapar.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "STARTUP_EMBLEM", listOf("ConnectedDrive", "M Performance", "Alpina", "BMW i"), 0, isLockedBySecureCoding = isGSeries)
        )
    }

    fun buildCafdParameters(isGSeries: Boolean): List<CafdParameter> {
        val bodyEcu = if (isGSeries) "BDC_BODY" else "CAS4"
        val bodyAddr = if (isGSeries) EcuAddress.ZGW_BDC else EcuAddress.FEM_CAS
        val bodyCafd = if (isGSeries) "CAFD_00007083_031_020" else "CAFD_0000000F_005_025"
        val kombiEcu = if (isGSeries) "DKOMBI" else "KOMBI"
        val kombiCafd = if (isGSeries) "CAFD_00004508_009_012" else "CAFD_00000069_009_002"
        val huEcu = if (isGSeries) "HU_MGU" else "HU_NBT"
        val huCafd = if (isGSeries) "CAFD_00003E52_007_018" else "CAFD_00000DED_003_015"

        return listOf(
            CafdParameter("cafd_msa_mem", bodyEcu, bodyAddr, bodyCafd, "3023 TcMaster2", "TCM_MSA_MEMORY", 0x1A, 0x01, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x01)" to "0x01"), "nicht_aktiv (0x00)"),
            CafdParameter("cafd_msa_off", bodyEcu, bodyAddr, bodyCafd, "3023 TcMaster2", "TCM_MSA_DEFAULT_OFF", 0x1A, 0x02, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x01)" to "0x01"), "nicht_aktiv (0x00)"),
            CafdParameter("cafd_comfort_delay", bodyEcu, bodyAddr, bodyCafd, "3053 PwMaster", "KOMFORT_SCHLIESSEN", 0x42, 0xFF, "0x0F", mapOf("1.5s (0x0F)" to "0x0F", "0.5s (0x05)" to "0x05", "0.0s (0x00)" to "0x00"), "1.5s (0x0F)"),
            CafdParameter("cafd_blink_count", bodyEcu, bodyAddr, bodyCafd, "3068 LceMaster", "BLINKZYKLEN_ANZAHL_TIPP", 0x24, 0x07, "0x02", mapOf("3x (0x02)" to "0x02", "5x (0x04)" to "0x04", "7x (0x06)" to "0x06"), "3x (0x02)"),
            CafdParameter("cafd_rear_drl", bodyEcu, bodyAddr, bodyCafd, "3062 LceLampMapping", "TAGFAHRLICHT_ECE_HINTEN", 0x31, 0x01, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x01)" to "0x01"), "nicht_aktiv (0x00)"),
            CafdParameter("cafd_door_win", bodyEcu, bodyAddr, bodyCafd, "3050 PwClient", "FH_TUERAUF_STOP_MAUT", 0x12, 0x01, "0x01", mapOf("aktiv (0x01)" to "0x01", "nicht_aktiv (0x00)" to "0x00"), "aktiv (0x01)"),
            CafdParameter("cafd_kombi_logo", kombiEcu, EcuAddress.KOMBI, kombiCafd, "3000 Anzeige_Konfiguration", "BMW_LOGO", 0x08, 0x07, "0x00", mapOf("ag (0x00)" to "0x00", "m_performance (0x01)" to "0x01", "alpina (0x03)" to "0x03"), "ag (0x00)"),
            CafdParameter("cafd_dig_speed", kombiEcu, EcuAddress.KOMBI, kombiCafd, "3000 Anzeige_Konfiguration", "BC_DIGITAL_V", 0x0C, 0x01, "0x01", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x01)" to "0x01"), "aktiv (0x01)"),
            CafdParameter("cafd_hud_turn", kombiEcu, EcuAddress.KOMBI, kombiCafd, "3008 HUD_Konfiguration", "HMI_HUD_TURN_INDICATOR", 0x19, 0x01, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x01)" to "0x01"), "nicht_aktiv (0x00)"),
            CafdParameter("cafd_vim_lock", huEcu, EcuAddress.HU_MEDIA, huCafd, "3000 HMI", "SPEEDLOCK_X_KMH_MAX", 0x61, 0xFF, "0x03", mapOf("3_kmh (0x03)" to "0x03", "werte_FF (0xFF)" to "0xFF"), "3_kmh (0x03)", isLockedBySecureCoding = isGSeries),
            CafdParameter("cafd_sport_cid", huEcu, EcuAddress.HU_MEDIA, huCafd, "3000 HMI", "EFF_DYN_SPORT_CID", 0x44, 0x01, "0x01", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x01)" to "0x01"), "aktiv (0x01)", isLockedBySecureCoding = isGSeries),
            CafdParameter("cafd_legal_disc", huEcu, EcuAddress.HU_MEDIA, huCafd, "3001 EXBOX", "LEGAL_DISCLAIMER_TIME", 0x20, 0xFF, "0x0A", mapOf("ld_mit_timeout (0x0A)" to "0x0A", "kein_ld (0x00)" to "0x00"), "ld_mit_timeout (0x0A)", isLockedBySecureCoding = isGSeries),
            CafdParameter("cafd_sbr_driver", "ACSM", EcuAddress.ACSM, "CAFD_00000909_004_003", "3000 Ausstattungsflags", "SBR_PREWARNING_FAHRER", 0x05, 0x01, "0x01", mapOf("aktiv (0x01)" to "0x01", "nicht_aktiv (0x00)" to "0x00"), "aktiv (0x01)")
        )
    }
}
