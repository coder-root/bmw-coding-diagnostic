package com.punhanaghayev.bimmerpro.core.bmw

/**
 * Comprehensive BimmerCode / E-Sys style BMW FDL & CAFD coding catalog for F-Series (F10/F30) and G-Series (G30/G20).
 * Extracted and calibrated directly against authentic BMW CAFD & FDL definition files.
 */
object BmwCodingCatalog {

    fun buildPresets(isGSeries: Boolean): List<CodingPreset> {
        val bodyEcu = if (isGSeries) "BDC_BODY" else "CAS4 / FEM"
        val bodyAddr = if (isGSeries) EcuAddress.ZGW_BDC else EcuAddress.FEM_CAS
        val kombiEcu = if (isGSeries) "DKOMBI" else "KOMBI"
        val huEcu = if (isGSeries) "HU_MGU" else "HU_NBT"

        return listOf(
            // ==================== DRIVETRAIN & START-STOP ====================
            CodingPreset("start_stop_mem", "Auto Start-Stop Son Durum Hafızası", "Motor kapatıldığında Start-Stop tercihini hatırlar.", CodingCategory.DRIVETRAIN, bodyEcu, bodyAddr, "TCM_MSA_MEMORY", listOf("Kapalı (Fabrika)", "Son Durumu Hatırla"), 0),
            CodingPreset("start_stop_off", "Auto Start-Stop Varsayılan Kapalı", "Her çalıştırmada Start-Stop otomatik kapalı başlar.", CodingCategory.DRIVETRAIN, bodyEcu, bodyAddr, "TCM_MSA_DEFAULT_OFF", listOf("Fabrika (Açık)", "Varsayılan Kapalı"), 0),
            CodingPreset("icm_drive_mode", "Başlangıç Sürüş Modu (Eco/Comfort/Sport)", "Kontak açıldığında aracın başlayacağı sürüş modunu seçer.", CodingCategory.DRIVETRAIN, "ICM", EcuAddress.DSC, "ICM_SPORT_INDIVIDUAL", listOf("Comfort (Fabrika)", "Eco Pro", "Sport"), 0),
            CodingPreset("paddles_retrofit", "Direksiyon Vites Kulakçıkları (Paddle Shift)", "Direksiyondaki F1 vites kulakçığı fonksiyonunu aktif eder.", CodingCategory.DRIVETRAIN, bodyEcu, bodyAddr, "PADDLES_VERBAUT", listOf("Pasif", "Aktif"), 0),
            CodingPreset("sport_transmission_2tb", "2TB Sport Otomatik Şanzıman & Launch Control", "Daha hızlı vites geçişleri, sport vites algoritması ve kalkış kontrolü (LC) açar.", CodingCategory.DRIVETRAIN, "EGS", EcuAddress.EGS, "SPORTSCHALTER", listOf("Standart Otomatik (205)", "2TB Sport Otomatik & Launch Control"), 0),
            CodingPreset("battery_class", "Akü Tipi & Kapasitesi Tanıtımı", "Araç enerji yönetimine yeni akü sınıfını kaydeder.", CodingCategory.DRIVETRAIN, bodyEcu, bodyAddr, "VCM_AEP_BATTERY_CLASS", listOf("80Ah AGM (Fabrika)", "90Ah AGM", "105Ah AGM", "70Ah AGM", "90Ah Kurşun-Asit"), 0),

            // ==================== COMFORT & BODY ====================
            CodingPreset("mirror_fold", "Kumandadan Tek Basışta Ayna Katlama", "Konfor kapatma gecikmesini sıfıra indirir.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "KOMFORT_SCHLIESSEN", listOf("1.5 sn Gecikme (Fabrika)", "0.5 sn Hızlı", "Anında (0.0 sn)"), 0),
            CodingPreset("mirror_fold_lock", "Kilitlemede Otomatik Ayna Katlama", "Kumandadan kilitleyince yan aynaları otomatik katlar.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "ASP_BEIKLAPPEN_BEI_KOMFORTSCHLIESSEN", listOf("Pasif", "Aktif"), 1),
            CodingPreset("mirror_unfold_unlock", "Kilit Açmada Otomatik Ayna Açma", "Araç kilidi açıldığında yan aynaları otomatik açar.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "ASP_AUSKLAPPEN_NACH_KOMFORTSCHLIESSEN", listOf("Pasif", "Aktif"), 1),
            CodingPreset("acoustic_lock_chirp", "Akustik Kilitleme / Kilit Açma Bip Sesi", "DWA sireninden kilitlemede 1 bip, açmada 2 bip onayı verir.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "QUIT_AKUST_SCHAERF", listOf("Sessiz (Fabrika)", "Akustik Onay Aktif"), 0),
            CodingPreset("reverse_mirror_tilt", "Geri Viteste Sağ Ayna Eğilme Açısı", "Kaldırım parkında sağ aynanın aşağı eğilme oranını ayarlar.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "ASP_BORDSTEINAUTOMATIK_DELTA", listOf("Fabrika (%70)", "Hafif (%40)", "Tam Eğilme (%90)"), 0),
            CodingPreset("window_door_interrupt", "Kapı Açılınca Cam Kapanmasını Durdurma", "Kapı açıldığında tek dokunuş cam kalkışını kesmez.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "FH_TUERAUF_STOP_MAUT", listOf("Durdur (Fabrika)", "Kesintisiz Kapat"), 0),
            CodingPreset("headlight_washer_disable", "Far Yıkama Fıskiyesini Kapat", "Ön far yıkama motorunu devre dışı bırakıp cam suyu tasarrufu sağlar.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "SCHEINWERFERREINIGUNG", listOf("Aktif (Fabrika)", "Devre Dışı (Tasarruf)"), 0),
            CodingPreset("trunk_keyfob_close", "Kumandadan Bagaj Kapatma", "Elektrikli bagajı kumandadan basılı tutmadan tek basışla kapatır.", CodingCategory.COMFORT, "HKFM", bodyAddr, "HKFM_SCHALTEINSATZ_FBD", listOf("Basılı Tut (Fabrika)", "Tek Dokunuş Kapat"), 0),
            CodingPreset("trunk_button_close", "Ayak Bölmesi Düğmesinden Bagaj Kapatma", "İç konsol düğmesinden elektrikli bagajı kapatır.", CodingCategory.COMFORT, "HKFM", bodyAddr, "HKL_ROCKERSWITCHPOS1_LONGPRESS_CLOSE", listOf("Pasif", "Aktif"), 0),
            CodingPreset("unlock_engine_off", "Motor Stop Edince Kapıları Otomatik Aç", "Start-Stop düğmesiyle motor kapatılınca tüm kilitleri açar.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "CLM_UNLOCK_AT_ENGINE_OFF", listOf("Pasif", "Aktif"), 0),
            CodingPreset("radio_off_door", "Kapı Açılınca iDrive & Radyoyu Kapat", "Motor stop edildikten sonra sürücü kapısı açılınca multimedyayı kapatır.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "TC_LOGIC_R_OFF_DOOR", listOf("Pasif", "Aktif"), 0),
            CodingPreset("easy_entry_seat", "Easy Entry Koltuk Geri Çekilme", "İniş ve binişi kolaylaştırmak için koltuğu otomatik geriye alır.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "EASY_ENTRY_SEAT", listOf("Pasif (Fabrika)", "Aktif"), 0),
            CodingPreset("seat_heating_memory", "Koltuk Isıtma Son Seviye Hafızası", "Araçtan inildikten sonra koltuk ısıtma kademesini hatırlar.", CodingCategory.COMFORT, bodyEcu, bodyAddr, "SA_MEMORY_ZEIT", listOf("15 Dakika (Fabrika)", "24 Saat", "Sürekli Hatırla"), 0),
            CodingPreset("seatbelt_chime", "Emniyet Kemeri İkaz Sesi (Sürücü)", "ACSM sürücü emniyet kemeri sesli uyarısını yönetir.", CodingCategory.COMFORT, "ACSM", EcuAddress.ACSM, "SBR_PREWARNING_FAHRER", listOf("Aktif (Fabrika)", "Kapalı"), 0),
            CodingPreset("seatbelt_chime_pass", "Emniyet Kemeri İkaz Sesi (Yolcu)", "ACSM ön yolcu emniyet kemeri sesli uyarısını yönetir.", CodingCategory.COMFORT, "ACSM", EcuAddress.ACSM, "SBR_PREWARNING_BEIFAHRER", listOf("Aktif (Fabrika)", "Kapalı"), 0),
            CodingPreset("ihka_ac_memory", "Klima (IHKA) Son Durum Hafızası", "Klima ve A/C kompresör durumunu kontak sonrası hatırlar.", CodingCategory.COMFORT, "IHKA", EcuAddress.IHKA, "MEMORY_OFF", listOf("Fabrika", "Son Durumu Hatırla"), 1),
            CodingPreset("ihka_recirc_memory", "İç Hava Sirkülasyon Hafızası", "İç hava sirkülasyon modunun kontak sonrası korunmasını sağlar.", CodingCategory.COMFORT, "IHKA", EcuAddress.IHKA, "MEMORY_UMLUFT", listOf("Fabrika (Sıfırla)", "Son Durumu Hatırla"), 0),

            // ==================== LIGHTING & WELCOME LIGHT ====================
            CodingPreset("angel_eyes_drl", "Angel Eyes / DRL Gündüz Parlaklığı", "Kısalar açıkken LED halkaların parlaklık oranını ayarlar.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "MAPPING_STANDL_V_R_PWM_LEVEL_1", listOf("%8 Fabrika", "%12 Belirgin", "%50 Çok Parlak", "%100 Maksimum"), 0),
            CodingPreset("welcome_lights", "Karşılama Işıkları (Welcome Lights)", "Kilit açıldığında karşılama ışıklarının yanmasını kontrol eder.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "WL_AKTIVIERUNG", listOf("Kapalı", "Far Anahtarı Otomatikte Açık", "Sürekli Açık"), 1),
            CodingPreset("welcome_lights_time", "Karşılama Işıkları Süresi", "Kilit açıldığında LED halkaların yanma süresini belirler.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "WL_TIMEOUT", listOf("20 sn (Fabrika)", "40 sn", "60 sn", "10 sn"), 0),
            CodingPreset("welcome_drl_soft", "Gündüz Farı Karşılama Yumuşak Açılış", "Gündüz farlarının yumuşak bir parlama animasyonuyla açılmasını sağlar.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "MAPPING_TAGFAHRL_V_L_PART_OF_WL", listOf("Kapalı", "Yumuşak Açılış (Soft On)"), 1),
            CodingPreset("welcome_fog_soft", "Sis Farı Karşılama Aydınlatması", "Kilit açıldığında ön sis farlarının da karşılama ışığına katılmasını sağlar.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "MAPPING_NEBELSCHW_L_PART_OF_WL", listOf("Kapalı (Fabrika)", "Yumuşak Açılış (Soft On)"), 0),
            CodingPreset("rear_drl", "Arka Stop LED'leri Gündüz Aktif", "Gündüz farlarıyla birlikte arka stopların da yanmasını sağlar.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "TAGFAHRLICHT_ECE_HINTEN", listOf("Kapalı (Fabrika)", "Aktif"), 0),
            CodingPreset("ambient_lighting_custom", "Ambiyans Aydınlatması Özel Renkleri", "İç konsol ve kapı LED'leri için fabrika dışı özel renk profillerini aktif eder.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "AMBIENTE_FARBE_ENSEMBLE", listOf("Turuncu / Beyaz (Fabrika)", "Turkuaz / Cyan", "M Sport Mavisi", "Lila / Mor", "Saf Kırmızı", "Buz Beyazı"), 0),
            CodingPreset("brake_force_display", "Acil Frende Çakar Stop Lambaları (BFD)", "Ani ve sert frenlemede arka fren lambalarını hızlı çakar moduna geçirir.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "ESS_AKTIVIERUNG_GESCHW", listOf("Sabit Yan (Fabrika)", "Çakar Flaşör Aktif (5 Hz)"), 0),
            CodingPreset("fog_off_with_highbeam", "Uzunlar Açılınca Sis Farını Kapat", "Uzun far selektöründe sis farlarının otomatik sönmesini sağlar.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "NSW_AUS_BEI_FL", listOf("Kapalı (Fabrika)", "Aktif (Sisi Söndür)"), 0),
            CodingPreset("turn_signal_count", "Tek Dokunuş Sinyal Sayısı (3x / 5x / 7x)", "Sinyal koluna hafif dokunuşta yanıp sönme sayısını değiştirir.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "BLINKZYKLEN_ANZAHL_TIPP", listOf("3 Kez (Fabrika)", "5 Kez", "7 Kez", "1 Kez"), 0),
            CodingPreset("door_leds_reverse", "Geri Viteste Kapı Kolu LED'leri", "Geri vitese takıldığında dış kapı kolu aydınlatmalarını yakar.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "OVT_BEI_RUECKFAHRT", listOf("Kapalı (Fabrika)", "Aktif"), 0),

            // ==================== LED CONVERSION (AMPUL ARIZA SÖNDÜRME) ====================
            CodingPreset("led_conv_fog", "Sis Farı LED Dönüşümü & Arıza Lambası", "Sis farlarına LED ampul takıldığında soğuk/sıcak kontrolü kapatır.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "LED_CONVERSION_NSW", listOf("Fabrika Halojen Kontrolü", "LED Aktif (Arıza İptal)"), 0),
            CodingPreset("led_conv_lowbeam", "Kısa Far LED Dönüşümü & Arıza İptali", "Kısa farlara takılan LED ampullerin titremesini ve arızasını önler.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "LED_CONVERSION_AL", listOf("Fabrika Halojen Kontrolü", "LED Aktif (Arıza İptal)"), 0),
            CodingPreset("led_conv_highbeam", "Uzun Far LED Dönüşümü & Arıza İptali", "Uzun farlara LED ampul takıldığında uyarı lambasını engeller.", CodingCategory.LIGHTING, bodyEcu, bodyAddr, "LED_CONVERSION_FL", listOf("Fabrika Halojen Kontrolü", "LED Aktif (Arıza İptal)"), 0),

            // ==================== INSTRUMENT CLUSTER & HUD ====================
            CodingPreset("kombi_logo", "Gösterge Açılış & Tema Logosu", "Gösterge paneli açılış logosunu ve yazı tipini değiştirir.", CodingCategory.INSTRUMENT, kombiEcu, EcuAddress.KOMBI, "BMW_LOGO", listOf("Standart AG", "M Performance", "Alpina Tema", "M5 / Motorsport"), 0),
            CodingPreset("digital_speed", "Kadranda Gerçek Dijital Hız (km/h)", "Yol bilgisayarı (BC) döngüsüne büyük dijital hız ekler.", CodingCategory.INSTRUMENT, kombiEcu, EcuAddress.KOMBI, "BC_DIGITAL_V", listOf("Pasif", "Aktif"), 1),
            CodingPreset("true_speed_corr", "Gerçek Hız Sapma Düzeltmesi", "Kadran hızının GPS hızına yakın gerçek değer göstermesini sağlar.", CodingCategory.INSTRUMENT, kombiEcu, EcuAddress.KOMBI, "BC_DIGITAL_V_KORREKTUR", listOf("Fabrika (+4 km/h)", "Gerçek Hız (0 Sapma)"), 0),
            CodingPreset("bc_date", "Yol Bilgisayarı Tarih Gösterimi", "Yol bilgisayarı menü döngüsüne güncel tarihi ekler.", CodingCategory.INSTRUMENT, kombiEcu, EcuAddress.KOMBI, "BC_DATUM", listOf("Pasif (Fabrika)", "Aktif"), 0),
            CodingPreset("hud_turn_signals", "Head-Up Display (HUD) Sinyal Gösterimi", "Sinyal oklarını ön cam HUD yansıtmasına ekler.", CodingCategory.INSTRUMENT, kombiEcu, EcuAddress.KOMBI, "HMI_HUD_TURN_INDICATOR", listOf("Kapalı", "Aktif"), 0),
            CodingPreset("hud_entertainment", "HUD Medya & Telefon Listesi", "Direksiyon tekerleğinden değişen şarkı/rehber listesini HUD'a yansıtır.", CodingCategory.INSTRUMENT, kombiEcu, EcuAddress.KOMBI, "HUD_ENTERTAINMENT_ENABLE", listOf("Kapalı", "Aktif"), 1),

            // ==================== MULTIMEDIA & IDRIVE ====================
            CodingPreset("video_in_motion", "Hareket Halinde Video (Video in Motion)", "iDrive ekranında sürüş esnasında USB video oynatım kilidini açar.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "SPEEDLOCK_X_KMH_MAX", listOf("Fabrika Kilidi (3 km/h)", "Limitsiz (255 km/h)"), 0, isLockedBySecureCoding = isGSeries),
            CodingPreset("usb_video_playback", "USB Bellekten Video Oynatma", "iDrive multimedya menüsünden USB video seçeneğini aktif eder.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "API_USB_VIDEO", listOf("Pasif", "Aktif"), 1, isLockedBySecureCoding = isGSeries),
            CodingPreset("usb_video_codecs", "USB Video Formatları (XviD / MP4 / MKV)", "USB bellekten ek video kodeklerinin oynatılmasını açar.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "VIDEO_CODECS_ACTIVE", listOf("Fabrika", "Tüm Kodekler Aktif (XviD/MKV/VCD)"), 0, isLockedBySecureCoding = isGSeries),
            CodingPreset("sport_displays", "iDrive Spor Göstergeler (HP & Nm)", "iDrive ekranında anlık beygir gücü ve tork kadranlarını aktif eder.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "SPORT_DISPLAY", listOf("Pasif", "Aktif (Turuncu/Kırmızı)"), 1, isLockedBySecureCoding = isGSeries),
            CodingPreset("sport_displays_color", "Spor Gösterge Renk Teması", "Spor kadranların rengini M Gümüş veya BMW Turuncu yapar.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "SPORT_DISPLAY_COLOR", listOf("Klasik Turuncu/Kırmızı", "M Gümüş/Gri Kadran"), 0, isLockedBySecureCoding = isGSeries),
            CodingPreset("legal_disclaimer", "iDrive Açılış Yasal Uyarı Ekranını Kapat", "Her çalıştırmada çıkan güvenlik uyarısını kaldırır.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "LEGAL_DISCLAIMER_TIME", listOf("Aktif (Fabrika)", "Kapalı (0 sn)"), 0, isLockedBySecureCoding = isGSeries),
            CodingPreset("chime_sound_set", "iDrive Uyarı Sesi Paketi (BMW / Rolls-Royce)", "Park sensörü ve sistem ikaz seslerinin tonunu değiştirir.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "SOUND_SIGNAL_SET", listOf("BMW Standart", "Rolls-Royce", "BMW i", "Mini"), 0, isLockedBySecureCoding = isGSeries),
            CodingPreset("idrive_boot_logo", "iDrive Başlangıç Animasyonu", "Ekran açılış logosunu M Performance veya Alpina yapar.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "STARTUP_EMBLEM", listOf("ConnectedDrive", "M Performance", "Alpina", "BMW i", "Rolls-Royce"), 0, isLockedBySecureCoding = isGSeries),
            CodingPreset("rdc_safety_temp", "Lastik Basıncı ve Sıcaklığı Gösterimi", "iDrive araç durumu menüsünde lastik basıncı yanında sıcaklık değerini (°C) de gösterir.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "RDC_SAFETY", listOf("Yalnızca Basınç (Fabrika)", "Basınç ve Sıcaklık Aktif (°C)"), 0, isLockedBySecureCoding = isGSeries),
            CodingPreset("volume_popup", "Ses Seviyesi Değişim Çubuğu (Volume Popup)", "Ses açıp kaparken ekranda anlık ses seviyesi çubuğunu gösterir.", CodingCategory.MULTIMEDIA, huEcu, EcuAddress.HU_MEDIA, "VOLUME_POPUP", listOf("Kapalı", "Aktif"), 1, isLockedBySecureCoding = isGSeries)
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
            // CAS4 / BDC_BODY
            CafdParameter("cafd_msa_mem", bodyEcu, bodyAddr, bodyCafd, "3023 TcMaster2", "TCM_MSA_MEMORY", 0x00, 0x20, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x20)" to "0x20"), "nicht_aktiv (0x00)"),
            CafdParameter("cafd_msa_off", bodyEcu, bodyAddr, bodyCafd, "3023 TcMaster2", "TCM_MSA_DEFAULT_OFF", 0x00, 0x10, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x10)" to "0x10"), "nicht_aktiv (0x00)"),
            CafdParameter("cafd_comfort_close_fb", bodyEcu, bodyAddr, bodyCafd, "3053 PwMaster", "KOMFORTSCHLIESSUNG_FB", 0x00, 0x80, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x80)" to "0x80"), "nicht_aktiv (0x00)"),
            CafdParameter("cafd_comfort_open_fb", bodyEcu, bodyAddr, bodyCafd, "3053 PwMaster", "KOMFORTOEFFNUNG_FB", 0x00, 0x40, "0x40", mapOf("aktiv (0x40)" to "0x40", "nicht_aktiv (0x00)" to "0x00"), "aktiv (0x40)"),
            CafdParameter("cafd_comfort_delay", bodyEcu, bodyAddr, bodyCafd, "3053 PwMaster", "KOMFORT_SCHLIESSEN", 0x09, 0xFF, "0x0F", mapOf("1.5s (0x0F)" to "0x0F", "0.5s (0x05)" to "0x05", "0.0s (0x00)" to "0x00"), "1.5s (0x0F)"),
            CafdParameter("cafd_sa_mem_time", bodyEcu, bodyAddr, bodyCafd, "3030 SeatMemory", "SA_MEMORY_ZEIT", 0x04, 0x03, "0x00", mapOf("15_min (0x00)" to "0x00", "dauerhaft (0x01)" to "0x01"), "15_min (0x00)"),
            CafdParameter("cafd_door_win", bodyEcu, bodyAddr, bodyCafd, "3050 PwClient", "FH_TUERAUF_STOP_MAUT", 0x12, 0x01, "0x01", mapOf("aktiv (0x01)" to "0x01", "nicht_aktiv (0x00)" to "0x00"), "aktiv (0x01)"),

            // FEM_01 Lighting & Convenience
            CafdParameter("cafd_wl_active", "FEM_01", EcuAddress.FEM_CAS, "CAFD_00000794_014_040", "3060 LceMaster", "WL_AKTIVIERUNG", 0x0E, 0xC0, "0x80", mapOf("nicht_aktiv (0x00)" to "0x00", "LDS_in_A (0x80)" to "0x80", "LDS_in_A_oder_2 (0xC0)" to "0xC0"), "LDS_in_A (0x80)"),
            CafdParameter("cafd_wl_time", "FEM_01", EcuAddress.FEM_CAS, "CAFD_00000794_014_040", "3060 LceMaster", "WL_TIMEOUT", 0x0F, 0xFF, "0x14", mapOf("20s (0x14)" to "0x14", "40s (0x28)" to "0x28", "60s (0x3C)" to "0x3C", "10s (0x0A)" to "0x0A"), "20s (0x14)"),
            CafdParameter("cafd_nsw_fl", "FEM_01", EcuAddress.FEM_CAS, "CAFD_00000794_014_040", "3060 LceMaster", "NSW_AUS_BEI_FL", 0x14, 0x20, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x20)" to "0x20"), "nicht_aktiv (0x00)"),
            CafdParameter("cafd_asp_fold", "FEM_01", EcuAddress.FEM_CAS, "CAFD_00000794_014_040", "3110 PfExteriorMirrorParam", "ASP_BEIKLAPPEN", 0x12, 0x01, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x01)" to "0x01"), "nicht_aktiv (0x00)"),
            CafdParameter("cafd_paddles", "FEM_01", EcuAddress.FEM_CAS, "CAFD_00000794_014_040", "3160 PfLinSteeringWheel", "PADDLES_VERBAUT", 0x18, 0x01, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x01)" to "0x01"), "nicht_aktiv (0x00)"),
            CafdParameter("cafd_led_nsw_warm", "FEM_01", EcuAddress.FEM_CAS, "CAFD_00000794_014_040", "3061 LceLampMapping", "NSW_L_WARMUEBERWACHUNG", 0x30, 0x01, "0x01", mapOf("aktiv (0x01)" to "0x01", "nicht_aktiv (0x00)" to "0x00"), "aktiv (0x01)"),
            CafdParameter("cafd_led_nsw_is_led", "FEM_01", EcuAddress.FEM_CAS, "CAFD_00000794_014_040", "3061 LceLampMapping", "NSW_L_IS_LED", 0x30, 0x04, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x04)" to "0x04"), "nicht_aktiv (0x00)"),
            CafdParameter("cafd_battery_class", "FEM_01", EcuAddress.FEM_CAS, "CAFD_00000794_014_040", "3040 PfcAep", "VCM_AEP_BATTERY_CLASS", 0x22, 0xFF, "0x5A", mapOf("80ah_agm (0x50)" to "0x50", "90ah_agm (0x5A)" to "0x5A", "105ah_agm (0x69)" to "0x69", "70ah_agm (0x46)" to "0x46"), "90ah_agm (0x5A)"),

            // KOMBI Instrument Cluster
            CafdParameter("cafd_kombi_logo", kombiEcu, EcuAddress.KOMBI, kombiCafd, "3000 Anzeige_Konfiguration", "BMW_LOGO", 0x08, 0x07, "0x00", mapOf("ag (0x00)" to "0x00", "m_performance (0x01)" to "0x01", "alpina (0x03)" to "0x03"), "ag (0x00)"),
            CafdParameter("cafd_dig_speed", kombiEcu, EcuAddress.KOMBI, kombiCafd, "3000 Anzeige_Konfiguration", "BC_DIGITAL_V", 0x0C, 0x01, "0x01", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x01)" to "0x01"), "aktiv (0x01)"),
            CafdParameter("cafd_speed_corr", kombiEcu, EcuAddress.KOMBI, kombiCafd, "3000 Anzeige_Konfiguration", "BC_DIGITAL_V_KORREKTUR", 0x0C, 0x02, "0x00", mapOf("tacho (0x00)" to "0x00", "gps_echt (0x02)" to "0x02"), "tacho (0x00)"),
            CafdParameter("cafd_bc_datum", kombiEcu, EcuAddress.KOMBI, kombiCafd, "3000 Anzeige_Konfiguration", "BC_DATUM", 0x0D, 0x01, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x01)" to "0x01"), "nicht_aktiv (0x00)"),
            CafdParameter("cafd_hud_turn", kombiEcu, EcuAddress.KOMBI, kombiCafd, "3008 HUD_Konfiguration", "HMI_HUD_TURN_INDICATOR", 0x19, 0x01, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x01)" to "0x01"), "nicht_aktiv (0x00)"),

            // HU_NBT Multimedia & iDrive
            CafdParameter("cafd_vim_lock", huEcu, EcuAddress.HU_MEDIA, huCafd, "3000 HMI", "SPEEDLOCK_X_KMH_MAX", 0x61, 0xFF, "0x03", mapOf("3_kmh (0x03)" to "0x03", "werte_FF (0xFF)" to "0xFF"), "3_kmh (0x03)", isLockedBySecureCoding = isGSeries),
            CafdParameter("cafd_usb_video", huEcu, EcuAddress.HU_MEDIA, huCafd, "3000 HMI", "API_USB_VIDEO", 0x38, 0x01, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "both (0x01)" to "0x01"), "nicht_aktiv (0x00)", isLockedBySecureCoding = isGSeries),
            CafdParameter("cafd_codecs", huEcu, EcuAddress.HU_MEDIA, huCafd, "3000 HMI", "VIDEO_CODECS_ACTIVE", 0x38, 0x02, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x02)" to "0x02"), "nicht_aktiv (0x00)", isLockedBySecureCoding = isGSeries),
            CafdParameter("cafd_sport_cid", huEcu, EcuAddress.HU_MEDIA, huCafd, "3000 HMI", "EFF_DYN_SPORT_CID", 0x44, 0x01, "0x01", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x01)" to "0x01"), "aktiv (0x01)", isLockedBySecureCoding = isGSeries),
            CafdParameter("cafd_legal_disc", huEcu, EcuAddress.HU_MEDIA, huCafd, "3001 EXBOX", "LEGAL_DISCLAIMER_TIME", 0x20, 0xFF, "0x0A", mapOf("ld_mit_timeout (0x0A)" to "0x0A", "kein_ld (0x00)" to "0x00"), "ld_mit_timeout (0x0A)", isLockedBySecureCoding = isGSeries),
            CafdParameter("cafd_sound_set", huEcu, EcuAddress.HU_MEDIA, huCafd, "3002 AUDIO_TUNER", "SOUND_SIGNAL_SET", 0x48, 0x07, "0x00", mapOf("bmw (0x00)" to "0x00", "rolls_royce (0x01)" to "0x01", "bmw_i (0x02)" to "0x02", "mini (0x03)" to "0x03"), "bmw (0x00)", isLockedBySecureCoding = isGSeries),

            // HKFM Tailgate
            CafdParameter("cafd_hkfm_remote_close", "HKFM", EcuAddress.FEM_CAS, "CAFD_00005710_001_000", "3010 HklGeneral", "HKL_REMOTECONTROLLIFTGATEBUTTON_LONGPRESS_CLOSE", 0x00, 0x08, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x08)" to "0x08"), "nicht_aktiv (0x00)"),
            CafdParameter("cafd_hkfm_rocker_close", "HKFM", EcuAddress.FEM_CAS, "CAFD_00005710_001_000", "3010 HklGeneral", "HKL_ROCKERSWITCHPOS1_LONGPRESS_CLOSE", 0x00, 0x08, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x08)" to "0x08"), "nicht_aktiv (0x00)"),

            // IHKA Climate Control
            CafdParameter("cafd_ihka_mem_off", "IHKA", EcuAddress.IHKA, "CAFD_000016EE_113_112", "3000 General", "MEMORY_OFF", 0x05, 0x01, "0x01", mapOf("aktiv (0x01)" to "0x01", "nicht_aktiv (0x00)" to "0x00"), "aktiv (0x01)"),
            CafdParameter("cafd_ihka_mem_recirc", "IHKA", EcuAddress.IHKA, "CAFD_000016EE_113_112", "3000 General", "MEMORY_UMLUFT", 0x05, 0x02, "0x00", mapOf("nicht_aktiv (0x00)" to "0x00", "aktiv (0x02)" to "0x02"), "nicht_aktiv (0x00)"),

            // ACSM Crash Safety Module
            CafdParameter("cafd_sbr_driver", "ACSM", EcuAddress.ACSM, "CAFD_00000911_004_000", "3000 Ausstattungsflags", "SBR_PREWARNING_FAHRER", 0x05, 0x01, "0x01", mapOf("aktiv (0x01)" to "0x01", "nicht_aktiv (0x00)" to "0x00"), "aktiv (0x01)"),
            CafdParameter("cafd_sbr_pass", "ACSM", EcuAddress.ACSM, "CAFD_00000911_004_000", "3000 Ausstattungsflags", "SBR_PREWARNING_BEIFAHRER", 0x05, 0x02, "0x02", mapOf("aktiv (0x02)" to "0x02", "nicht_aktiv (0x00)" to "0x00"), "aktiv (0x02)")
        )
    }
}
