package com.punhanaghayev.bimmerpro.core.i18n

/**
 * Global domain text localizer ensuring that dynamic vehicle data, simulator records,
 * DTC descriptions, ECU long names, service history items, ZF8 clutch labels,
 * coding presets, and chassis compatibility notes render in the user's selected language.
 */
object GlobalTextLocalizer {

    fun localize(text: String, language: AppLanguage): String {
        if (text.isBlank() || language == AppLanguage.TR) return text

        // 1. Exact match lookup
        val exact = when (language) {
            AppLanguage.EN -> exactEn[text]
            AppLanguage.AZ -> exactAz[text]
            AppLanguage.RU -> exactRu[text]
            AppLanguage.TR -> text
        }
        if (exact != null) return exact

        // 2. Dynamic pattern translations (e.g. "10 ECU tarandı, 3 arıza kaydı tespit edildi.")
        val scanSummaryMatch = Regex("""(\d+) ECU tarandı, (\d+) arıza kaydı tespit edildi\.""").find(text)
        if (scanSummaryMatch != null) {
            val ecuCount = scanSummaryMatch.groupValues[1]
            val dtcCount = scanSummaryMatch.groupValues[2]
            return when (language) {
                AppLanguage.EN -> "$ecuCount ECUs scanned, $dtcCount fault(s) detected."
                AppLanguage.AZ -> "$ecuCount ECU skan edildi, $dtcCount xəta qeydi aşkarlandı."
                AppLanguage.RU -> "Просканировано $ecuCount ЭБУ, обнаружено $dtcCount ошибок."
                AppLanguage.TR -> text
            }
        }

        // 3. Phrase-based deterministic translation for compound strings
        var result = text
        val replacements = when (language) {
            AppLanguage.EN -> phrasesEn
            AppLanguage.AZ -> phrasesAz
            AppLanguage.RU -> phrasesRu
            AppLanguage.TR -> emptyList()
        }
        for ((src, dst) in replacements) {
            if (result.contains(src)) {
                result = result.replace(src, dst)
            }
        }
        return result
    }

    private val exactEn = mapOf(
        // Scan & modal status
        "BMW Diagnostik Taraması" to "BMW Diagnostic Scan",
        "Tarama Başarıyla Tamamlandı!" to "Scan Completed Successfully!",
        "Tüm kontrol üniteleri taranıyor..." to "Scanning all control units...",
        "Bağlantı koptu. Otomatik kurtarma devrede..." to "Connection lost. Auto-recovery active...",
        "Bağlantı kesildi." to "Disconnected.",
        // DTC Status & Severity
        "Aktif" to "Active",
        "Kayıtlı" to "Stored",
        "Gölge" to "Shadow",
        "Kritik" to "Critical",
        "Uyarı" to "Warning",
        "Pasif" to "Inactive",
        // ZF8 Clutches
        "Kavrama A" to "Clutch A",
        "Kavrama B" to "Clutch B",
        "Kavrama C" to "Clutch C",
        "Kavrama D" to "Clutch D",
        "Kavrama E" to "Clutch E",
        // Model Support Levels
        "Tam Destek" to "Full Support",
        "Kısmi Destek (Secure Coding 2.0)" to "Partial Support (Secure Coding 2.0)",
        "K-Line Donanım Gerekli (2001-2007)" to "K-Line Hardware Required (2001-2007)",
        // HV / ADAS
        "HV Kontaktör & İzolasyon Sağlam (>500 kOhm)" to "HV Contactor & Insulation OK (>500 kOhm)",
        "Dinamik Sürüş Kalibrasyonu Aktif (40-70 km/h)" to "Dynamic Driving Calibration Active (40-70 km/h)",
        "Kalibre Edildi (Aktif)" to "Calibrated (Active)",
        "Yüksek voltaj batarya hücre dengesi sağlıklı (Delta < 25 mV)" to "HV battery cell balance healthy (Delta < 25 mV)",
        // Service History Items
        "Motor Yağı" to "Engine Oil",
        "Motor Yağı 5W-30" to "Engine Oil 5W-30",
        "Buji Seti (4x)" to "Spark Plug Set (4x)",
        "Buji Seti" to "Spark Plug Set",
        "Mikrofiltre" to "Microfilter",
        "Fren Hidroliği" to "Brake Fluid",
        "Hava Filtresi" to "Air Filter",
        "Araç Kontrolü" to "Vehicle Check",
        // F10 528i & Common DTCs
        "Şarj Basıncı Kontrolü: Basınç Çok Düşük (Boost Pressure Low)" to "Charge-Air Pressure Control: Pressure Too Low (Boost Pressure Low)",
        "Gaz Kelebeği Açısı — Emme Manifoldu Mutlak Basıncı Tutarsızlığı" to "Throttle Valve Angle — Intake Manifold Pressure Plausibility",
        "USB Bağlantı Soketi 1 İletişim Hatası" to "USB Connection Port 1 Communication Fault",
        "İstenen basınç ile ölçülen basınç sapması > 350 mbar (N20 Diverter valf kontrolü)." to "Target vs. actual boost pressure deviation > 350 mbar (Check N20 diverter valve).",
        // ECU Long Names
        "DME Motor Yönetimi (N20 TwinPower Turbo)" to "DME Engine Management (N20 TwinPower Turbo)",
        "Elektronik Şanzıman Yönetimi (ZF 8HP45)" to "Electronic Transmission Control (ZF 8HP45)",
        "Dinamik Denge Kontrolü" to "Dynamic Stability Control",
        "Elektronik Güç Destekli Direksiyon" to "Electric Power Steering",
        "Gösterge Tablosu" to "Instrument Cluster",
        "Entegre Otomatik Isıtma/Klima" to "Integrated Automatic Heating / A/C",
        "Ayak Bölmesi Modülü (Footwell Module 3)" to "Footwell Module 3 (FRM)",
        "Merkezi Ağ Geçidi (Central Gateway)" to "Central Gateway Module (ZGW)",
        // Coding Capabilities
        "Tam FDL + VO Kodlama" to "Full FDL + VO Coding",
        "Sadece FDL Kodlama" to "FDL Coding Only",
        "Salt Okunur (Secure Coding 2.0)" to "Read-Only (Secure Coding 2.0)",
        "Salt Okunur" to "Read-Only",
        // Extended Coding Presets & Options
        "Auto Start-Stop Son Durum Hafızası" to "Auto Start-Stop Last State Memory",
        "Motor kapatıldığında Start-Stop tercihini hatırlar." to "Remembers Auto Start-Stop setting across ignition cycles.",
        "Auto Start-Stop Varsayılan Kapalı" to "Auto Start-Stop Default Off",
        "Her çalıştırmada Start-Stop otomatik kapalı başlar." to "Starts with Auto Start-Stop turned off by default.",
        "Başlangıç Sürüş Modu (Eco/Comfort/Sport)" to "Default Driving Mode (Eco/Comfort/Sport)",
        "Kontak açıldığında aracın başlayacağı sürüş modunu seçer." to "Selects default driving mode on vehicle startup.",
        "Direksiyon Vites Kulakçıkları (Paddle Shift)" to "Steering Wheel Paddle Shifters",
        "Direksiyondaki F1 vites kulakçığı fonksiyonunu aktif eder." to "Activates F1 paddle shift retrofit functionality.",
        "2TB Sport Otomatik Şanzıman & Launch Control" to "2TB Sport Automatic Transmission & Launch Control",
        "Daha hızlı vites geçişleri, sport vites algoritması ve kalkış kontrolü (LC) açar." to "Enables faster shifts, sport shift map and Launch Control.",
        "Akü Tipi & Kapasitesi Tanıtımı" to "Battery Type & Capacity Registration",
        "Araç enerji yönetimine yeni akü sınıfını kaydeder." to "Codes new battery capacity & chemistry into power management.",
        "Kumandadan Tek Basışta Ayna Katlama" to "One-Touch Mirror Fold via Keyfob",
        "Konfor kapatma gecikmesini sıfıra indirir." to "Reduces comfort mirror fold delay to zero.",
        "Kilitlemede Otomatik Ayna Katlama" to "Auto Fold Mirrors on Lock",
        "Kumandadan kilitleyince yan aynaları otomatik katlar." to "Automatically folds side mirrors when locking via keyfob.",
        "Akustik Kilitleme / Kilit Açma Bip Sesi" to "Acoustic Lock / Unlock Confirmation Beep",
        "DWA sireninden kilitlemede 1 bip, açmada 2 bip onayı verir." to "Enables 1 beep on lock and 2 beeps on unlock via alarm siren.",
        "Geri Viteste Sağ Ayna Eğilme Açısı" to "Passenger Mirror Tilt Angle in Reverse",
        "Kaldırım parkında sağ aynanın aşağı eğilme oranını ayarlar." to "Adjusts downward tilt angle of passenger mirror in reverse.",
        "Kapı Açılınca Cam Kapanmasını Durdurma" to "Disable Window Roll-Up Interrupt on Door Open",
        "Kapı açıldığında tek dokunuş cam kalkışını kesmez." to "Prevents one-touch window closing from stopping when opening door.",
        "Far Yıkama Fıskiyesini Kapat" to "Disable Headlight Washers",
        "Ön far yıkama motorunu devre dışı bırakıp cam suyu tasarrufu sağlar." to "Disables headlight washer jets to save washer fluid.",
        "Kumandadan Bagaj Kapatma" to "Close Trunk via Keyfob (One-Touch)",
        "Elektrikli bagajı kumandadan basılı tutmadan tek basışla kapatır." to "Closes power tailgate with one click without holding button.",
        "Motor Stop Edince Kapıları Otomatik Aç" to "Auto Unlock Doors on Engine Stop",
        "Start-Stop düğmesiyle motor kapatılınca tüm kilitleri açar." to "Unlocks all doors automatically when stopping the engine.",
        "Kapı Açılınca iDrive & Radyoyu Kapat" to "Turn Off iDrive & Radio When Door Opens",
        "Motor stop edildikten sonra sürücü kapısı açılınca multimedyayı kapatır." to "Shuts down iDrive and audio when driver door opens after engine off.",
        "Easy Entry Koltuk Geri Çekilme" to "Easy Entry Driver Seat Retract",
        "İniş ve binişi kolaylaştırmak için koltuğu otomatik geriye alır." to "Moves seat back automatically for easier entry and exit.",
        "Koltuk Isıtma Son Seviye Hafızası" to "Seat Heating Last Level Memory",
        "Araçtan inildikten sonra koltuk ısıtma kademesini hatırlar." to "Remembers last seat heating stage after parking.",
        "Emniyet Kemeri İkaz Sesi (Sürücü)" to "Seatbelt Reminder Chime (Driver)",
        "ACSM sürücü emniyet kemeri sesli uyarısını yönetir." to "Controls ACSM driver seatbelt audible warning.",
        "Emniyet Kemeri İkaz Sesi (Yolcu)" to "Seatbelt Reminder Chime (Passenger)",
        "ACSM ön yolcu emniyet kemeri sesli uyarısını yönetir." to "Controls ACSM front passenger seatbelt audible warning.",
        "Klima (IHKA) Son Durum Hafızası" to "Climate Control (IHKA) Last State Memory",
        "Klima ve A/C kompresör durumunu kontak sonrası hatırlar." to "Remembers A/C and climate state across ignition cycles.",
        "İç Hava Sirkülasyon Hafızası" to "Air Recirculation Memory",
        "İç hava sirkülasyon modunun kontak sonrası korunmasını sağlar." to "Preserves cabin air recirculation setting after restart.",
        "Angel Eyes / DRL Gündüz Parlaklığı" to "Angel Eyes / DRL Brightness",
        "Kısalar açıkken LED halkaların parlaklık oranını ayarlar." to "Adjusts LED halo brightness when low beams are active.",
        "Karşılama Işıkları (Welcome Lights)" to "Welcome Lights Activation",
        "Kilit açıldığında karşılama ışıklarının yanmasını kontrol eder." to "Controls welcome lights when unlocking the car.",
        "Karşılama Işıkları Süresi" to "Welcome Lights Duration",
        "Kilit açıldığında LED halkaların yanma süresini belirler." to "Sets how long welcome lights stay illuminated.",
        "Arka Stop LED'leri Gündüz Aktif" to "Rear Tail Lights Active with DRL",
        "Gündüz farlarıyla birlikte arka stopların da yanmasını sağlar." to "Enables rear LED tail lights alongside front DRLs.",
        "Ambiyans Aydınlatması Özel Renkleri" to "Custom Interior Ambient Lighting Colors",
        "İç konsol ve kapı LED'leri için fabrika dışı özel renk profillerini aktif eder." to "Unlocks custom interior ambient LED color presets.",
        "Acil Frende Çakar Stop Lambaları (BFD)" to "Brake Force Display (Flashing Brake Lights)",
        "Ani ve sert frenlemede arka fren lambalarını hızlı çakar moduna geçirir." to "Flashes rear brake lights rapidly during hard emergency braking.",
        "Tek Dokunuş Sinyal Sayısı (3x / 5x / 7x)" to "One-Touch Turn Signal Blinks (3x / 5x / 7x)",
        "Sinyal koluna hafif dokunuşta yanıp sönme sayısını değiştirir." to "Changes blink count for one-touch lane change signal.",
        "Geri Viteste Kapı Kolu LED'leri" to "Door Handle LEDs in Reverse",
        "Geri vitese takıldığında dış kapı kolu aydınlatmalarını yakar." to "Illuminates exterior door handle LEDs when reversing.",
        "Gösterge Açılış & Tema Logosu" to "Instrument Cluster Startup Logo & Theme",
        "Gösterge paneli açılış logosunu ve yazı tipini değiştirir." to "Changes instrument cluster boot logo and gauge styling.",
        "Kadranda Gerçek Dijital Hız (km/h)" to "Digital Speedometer in Cluster (km/h)",
        "Yol bilgisayarı (BC) döngüsüne büyük dijital hız ekler." to "Adds digital speed display to the instrument cluster BC cycle.",
        "Head-Up Display (HUD) Sinyal Gösterimi" to "Head-Up Display (HUD) Turn Signals",
        "Sinyal oklarını ön cam HUD yansıtmasına ekler." to "Shows turn signal indicators on the windshield HUD.",
        "iDrive Spor Göstergeler (HP & Nm)" to "iDrive Sport Displays (HP & Nm)",
        "iDrive ekranında anlık beygir gücü ve tork kadranlarını aktif eder." to "Activates live horsepower and torque gauges in iDrive.",
        "iDrive Açılış Yasal Uyarı Ekranını Kapat" to "Disable iDrive Startup Legal Disclaimer",
        "Her çalıştırmada çıkan güvenlik uyarısını kaldırır." to "Removes the startup safety disclaimer screen.",
        "Aktif Ses Tasarımı (ASD) Yapay Motor Sesi" to "Active Sound Design (ASD) Cabin Engine Sound",
        "Hoparlörlerden verilen yapay motor sesini kapatır veya M-Power profiline alır." to "Disables artificial speaker engine sound or sets M-Power profile.",
        "Koltuk Isıtma 3. Kademe Sıcaklık Limiti" to "Seat Heating Stage 3 Temperature Limit",
        "Ön koltuk ısıtmasının en yüksek (3. kademe) hedef sıcaklığını ayarlar." to "Sets target temperature for stage 3 front seat heating.",
        "Viraj Aydınlatması (Sis Farları)" to "Cornering Fog Lights",
        "Düşük hızda direksiyon çevrildiğinde ilgili taraf sis farını viraj içi yakar." to "Turns on inner fog light when cornering at low speeds.",
        "Kör Nokta Uyarı Aktivasyon Hızı" to "Blind Spot Detection Activation Speed",
        "Yan ayna kör nokta uyarısının (SWW) devreye girme hız eşiğini düşürür." to "Lowers activation speed threshold for blind spot warnings.",
        "Egzoz Valfi Sport Modda Tam Açık" to "Exhaust Flap Permanently Open in Sport",
        "Sport sürüş modunda egzoz susturucu klapesini sürekli açık tutar." to "Keeps exhaust flap open continuously in Sport mode.",
        // Common coding options
        "Kapalı (Fabrika)" to "Off (Factory)",
        "Aktif (Fabrika)" to "Active (Factory)",
        "Pasif (Fabrika)" to "Inactive (Factory)",
        "Kapalı" to "Off",
        "Son Durumu Hatırla" to "Remember Last State",
        "Varsayılan Kapalı" to "Default Off",
        "Fabrika (Açık)" to "Factory (On)",
        "Tamamen Kapalı" to "Completely Off",
        "Sport Modda Tam Açık" to "Fully Open in Sport",
        "Fabrika Haritası" to "Factory Map",
        "Başarılı" to "Success",
        "Başarısız" to "Failed"
    )

    private val exactAz = mapOf(
        "BMW Diagnostik Taraması" to "BMW Diaqnostik Skanı",
        "Tarama Başarıyla Tamamlandı!" to "Skan Uğurla Tamamlandı!",
        "Tüm kontrol üniteleri taranıyor..." to "Bütün idarəetmə blokları skan edilir...",
        "Aktif" to "Aktiv",
        "Kayıtlı" to "Yaddaşda",
        "Gölge" to "Kölgə",
        "Kritik" to "Kritik",
        "Uyarı" to "Xəbərdarlıq",
        "Pasif" to "Passiv",
        "Kavrama A" to "Mufta A",
        "Kavrama B" to "Mufta B",
        "Kavrama C" to "Mufta C",
        "Kavrama D" to "Mufta D",
        "Kavrama E" to "Mufta E",
        "Tam Destek" to "Tam Dəstək",
        "Kısmi Destek (Secure Coding 2.0)" to "Qismən Dəstək (Secure Coding 2.0)",
        "K-Line Donanım Gerekli (2001-2007)" to "K-Line Avadanlıq Tələb Olunur (2001-2007)",
        "HV Kontaktör & İzolasyon Sağlam (>500 kOhm)" to "HV Kontaktor və İzolyasiya Sağlam (>500 kOhm)",
        "Dinamik Sürüş Kalibrasyonu Aktif (40-70 km/h)" to "Dinamik Sürüş Kalibrasiyası Aktiv (40-70 km/saat)",
        "Motor Yağı" to "Mühərrik Yağı",
        "Motor Yağı 5W-30" to "Mühərrik Yağı 5W-30",
        "Buji Seti (4x)" to "Şam Dəsti (4x)",
        "Mikrofiltre" to "Mikrofiltr",
        "Fren Hidroliği" to "Əyləc Mayesi",
        "Hava Filtresi" to "Hava Filtri",
        "Araç Kontrolü" to "Avtomobil Yoxlanışı",
        "Tam FDL + VO Kodlama" to "Tam FDL + VO Kodlaşdırma",
        "Sadece FDL Kodlama" to "Yalnız FDL Kodlaşdırma",
        "Salt Okunur (Secure Coding 2.0)" to "Yalnız Oxuma (Secure Coding 2.0)",
        "Şarj Basıncı Kontrolü: Basınç Çok Düşük (Boost Pressure Low)" to "Turbo Təzyiq Nəzarəti: Təzyiq Çox Aşağıdır (Boost Pressure Low)",
        "Gaz Kelebeği Açısı — Emme Manifoldu Mutlak Basıncı Tutarsızlığı" to "Qaz Kəpənəyi Bucağı — Giriş Kollektoru Təzyiq Uyğunsuzluğu",
        "USB Bağlantı Soketi 1 İletişim Hatası" to "USB Bağlantı Portu 1 Əlaqə Xətası"
    )

    private val exactRu = mapOf(
        "BMW Diagnostik Taraması" to "Диагностическое сканирование BMW",
        "Tarama Başarıyla Tamamlandı!" to "Сканирование успешно завершено!",
        "Tüm kontrol üniteleri taranıyor..." to "Сканирование всех блоков управления...",
        "Aktif" to "Активная",
        "Kayıtlı" to "Сохраненная",
        "Gölge" to "Теневая",
        "Kritik" to "Критическая",
        "Uyarı" to "Предупреждение",
        "Pasif" to "Выкл.",
        "Kavrama A" to "Муфта A",
        "Kavrama B" to "Муфта B",
        "Kavrama C" to "Муфта C",
        "Kavrama D" to "Муфта D",
        "Kavrama E" to "Муфта E",
        "Tam Destek" to "Полная поддержка",
        "Kısmi Destek (Secure Coding 2.0)" to "Частичная поддержка (Secure Coding 2.0)",
        "K-Line Donanım Gerekli (2001-2007)" to "Требуется K-Line адаптер (2001-2007)",
        "HV Kontaktör & İzolasyon Sağlam (>500 kOhm)" to "Контактор и изоляция HV в норме (>500 кОм)",
        "Dinamik Sürüş Kalibrasyonu Aktif (40-70 km/h)" to "Активна динамическая калибровка (40-70 км/ч)",
        "Motor Yağı" to "Моторное масло",
        "Motor Yağı 5W-30" to "Моторное масло 5W-30",
        "Buji Seti (4x)" to "Комплект свечей (4x)",
        "Mikrofiltre" to "Микрофильтр",
        "Fren Hidroliği" to "Тормозная жидкость",
        "Hava Filtresi" to "Воздушный фильтр",
        "Araç Kontrolü" to "Проверка автомобиля",
        "Tam FDL + VO Kodlama" to "Полное FDL + VO кодирование",
        "Sadece FDL Kodlama" to "Только FDL кодирование",
        "Salt Okunur (Secure Coding 2.0)" to "Только чтение (Secure Coding 2.0)",
        "Şarj Basıncı Kontrolü: Basınç Çok Düşük (Boost Pressure Low)" to "Контроль давления наддува: слишком низкое давление",
        "Gaz Kelebeği Açısı — Emme Manifoldu Mutlak Basıncı Tutarsızlığı" to "Угол дроссельной заслонки — несоответствие давления во впуске",
        "USB Bağlantı Soketi 1 İletişim Hatası" to "Ошибка связи USB-порта 1"
    )

    private val phrasesEn = listOf(
        "Tam Destek (K-Line / KWP2000)" to "Full Support (K-Line / KWP2000)",
        "Tam Destek (D-CAN / KWP2000)" to "Full Support (D-CAN / KWP2000)",
        "Tam Destek (ENET / DoIP / UDS)" to "Full Support (ENET / DoIP / UDS)",
        "Tam Destek (DoIP / UDS)" to "Full Support (DoIP / UDS)",
        "Tam Destek" to "Full Support",
        "Tam FDL & TRC Kodlama" to "Full FDL & TRC Coding",
        "Tam FDL & VO Kodlama" to "Full FDL & VO Coding",
        "Tam FDL Kodlama" to "Full FDL Coding",
        "Kısıtlı (Secure Coding 2.0 — X.509 Sertifika Gerekli)" to "Restricted (Secure Coding 2.0 — X.509 Certificate Required)",
        "Pin 7+8 Köprülü Anahtarlı Adaptör" to "Pin 7+8 Bridged Switch Adapter",
        "2007 öncesi araçlarda OBD soketinde D-CAN hattı yoktur; teşhis Pin 7 ve 8 üzerinden K-Line ile yürütülür." to "Pre-2007 vehicles lack D-CAN on OBD pins 6/14; diagnostics run over dual K-Line on Pins 7 & 8.",
        "Standart ENET çalışmaz. Üzerinde switch bulunan ve Pin 7+8 köprüleyen K+DCAN USB adaptörü kullanılmalıdır." to "Standard ENET will not work. Use a K+DCAN USB adapter with Pin 7+8 bridged switch.",
        "2023 sonrası iDrive 8/8.5 MGU ekran ve BDC3 modülü kodlama kilidine sahiptir." to "2023+ iDrive 8/8.5 MGU & BDC3 modules are protected by BMW Secure Coding 2.0.",
        "iDrive 8.5 ekran ve BDC3 modülü kodlama kilidi içerir." to "iDrive 8.5 headunit & BDC3 module use BMW Secure Coding 2.0 lock.",
        "BMW AOS imzalı JWT token gereklidir; ancak teşhis, arıza silme, canlı veri ve servis rutinleri %100 çalışır." to "Requires BMW AOS signed JWT token for FDL coding; diagnostics, fault clearing, live data & service routines work 100%.",
        "Kilit Açmada Otomatik Ayna Açma" to "Auto Unfold Mirrors on Unlock",
        "Araç kilidi açıldığında yan aynaları otomatik açar." to "Automatically unfolds side mirrors when unlocking the vehicle.",
        "Hareket Halinde Video (Video in Motion)" to "Video in Motion (VIM)",
        "iDrive ekranında sürüş esnasında USB video oynatım kilidini açar." to "Unlocks USB video playback on iDrive screen while driving.",
        "Fabrika Kilidi (3 km/h)" to "Factory Lock (3 km/h)",
        "Limitsiz (255 km/h)" to "Unlimited (255 km/h)",
        "Otomatik Start/Stop Hafızası" to "Auto Start/Stop Memory",
        "Son Start/Stop durumunu kontak kapatıldığında hafızada tutar." to "Remembers the last Auto Start/Stop setting across ignition cycles.",
        "Dijital Hız Göstergesi (KOMBI)" to "Digital Speedometer (KOMBI)",
        "Gösterge panelinde anlık dijital hız bilgisini etkinleştirir." to "Enables real-time digital speed readout on the instrument cluster.",
        "M Performance Açılış Animasyonu" to "M Performance Startup Animation",
        "iDrive açılışında M Performance animasyonunu gösterir." to "Displays M Performance boot animation on iDrive startup.",
        "Sinyal Sayısı (Tek Dokunuş)" to "One-Touch Turn Signal Count",
        "Tek dokunuşta yanıp sönen sinyal sayısını ayarlar." to "Configures blink count for one-touch lane change signal.",
        "3 Sinyal" to "3 Blinks",
        "5 Sinyal" to "5 Blinks",
        "Ayak Bölmesi Modülü" to "Footwell Module",
        "Motor Yönetimi" to "Engine Management",
        "Elektronik Şanzıman Yönetimi" to "Electronic Transmission Control",
        "Dinamik Denge Kontrolü" to "Dynamic Stability Control",
        "Gösterge Tablosu" to "Instrument Cluster",
        "Merkezi Ağ Geçidi" to "Central Gateway",
        "Kavrama " to "Clutch ",
        "Motor Yağı" to "Engine Oil",
        "Buji Seti" to "Spark Plug Set",
        "Mikrofiltre" to "Microfilter",
        "Fren Hidroliği" to "Brake Fluid",
        "Hava Filtresi" to "Air Filter",
        "Kayıtlı" to "Stored",
        "Aktif" to "Active",
        "Pasif" to "Inactive"
    )

    private val phrasesAz = listOf(
        "Tam Destek" to "Tam Dəstək",
        "Tam FDL & TRC Kodlama" to "Tam FDL və TRC Kodlaşdırma",
        "Tam FDL & VO Kodlama" to "Tam FDL və VO Kodlaşdırma",
        "Pin 7+8 Köprülü Anahtarlı Adaptör" to "Pin 7+8 Körpülü Açarlı Adapter",
        "2007 öncesi araçlarda OBD soketinde D-CAN hattı yoktur; teşhis Pin 7 ve 8 üzerinden K-Line ile yürütülür." to "2007-dən əvvəlki avtomobillərdə D-CAN xətti yoxdur; diaqnostika Pin 7 və 8 üzərindən K-Line ilə aparılır.",
        "Standart ENET çalışmaz. Üzerinde switch bulunan ve Pin 7+8 köprüleyen K+DCAN USB adaptörü kullanılmalıdır." to "Standart ENET işləmir. Pin 7+8 açarı körpülənmiş K+DCAN USB adapteri istifadə olunmalıdır.",
        "Kavrama " to "Mufta ",
        "Motor Yağı" to "Mühərrik Yağı",
        "Buji Seti" to "Şam Dəsti",
        "Mikrofiltre" to "Mikrofiltr",
        "Fren Hidroliği" to "Əyləc Mayesi",
        "Hava Filtresi" to "Hava Filtri",
        "Kayıtlı" to "Yaddaşda",
        "Aktif" to "Aktiv",
        "Pasif" to "Passiv"
    )

    private val phrasesRu = listOf(
        "Tam Destek" to "Полная поддержка",
        "Tam FDL & TRC Kodlama" to "Полное кодирование FDL и TRC",
        "Tam FDL & VO Kodlama" to "Полное кодирование FDL и VO",
        "Pin 7+8 Köprülü Anahtarlı Adaptör" to "Адаптер с перемычкой Pin 7+8",
        "2007 öncesi araçlarda OBD soketinde D-CAN hattı yoktur; teşhis Pin 7 ve 8 üzerinden K-Line ile yürütülür." to "Автомобили до 2007 года не имеют шины D-CAN; диагностика выполняется по K-Line (контакты 7 и 8).",
        "Standart ENET çalışmaz. Üzerinde switch bulunan ve Pin 7+8 köprüleyen K+DCAN USB adaptörü kullanılmalıdır." to "Стандартный ENET не работает. Используйте K+DCAN USB адаптер с переключателем контактов 7+8.",
        "Kavrama " to "Муфта ",
        "Motor Yağı" to "Моторное масло",
        "Buji Seti" to "Комплект свечей",
        "Mikrofiltre" to "Микрофильтр",
        "Fren Hidroliği" to "Тормозная жидкость",
        "Hava Filtresi" to "Воздушный фильтр",
        "Kayıtlı" to "Сохраненная",
        "Aktif" to "Активная",
        "Pasif" to "Выкл."
    )
}
