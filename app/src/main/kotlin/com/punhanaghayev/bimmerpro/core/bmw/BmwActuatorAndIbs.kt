package com.punhanaghayev.bimmerpro.core.bmw

/**
 * UDS 0x2F InputOutputControlByIdentifier actuator tests & IBS Battery Health telemetry.
 */
data class ActuatorTestItem(
    val id: String,
    val title: String,
    val targetEcu: String,
    val targetAddress: Int,
    val did: Int,
    val description: String
) {
    fun localizedTitle(strings: com.punhanaghayev.bimmerpro.core.i18n.AppStrings): String = when (id) {
        "act_fan" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Radiator Cooling Fan (80% Test)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Radiator Soyutma Pəri (80% Test)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Вентилятор охлаждения радиатора (80% тест)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        "act_fuel" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Low Pressure Fuel Pump (EKPS)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Aşağı Təzyiqli Yanacaq Nasosu (EKPS)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Топливный насос низкого давления (EKPS)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        "act_exhaust" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Exhaust Flap Open/Close Test"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Qaz Qapağının Aç/Bağla Testi"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Тест заслонки выхлопа (Открыть/Закрыть)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        "act_kombi" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Instrument Cluster Gauge & LED Sweep"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Cihazlar Paneli Əqrəb və LED Testi (Sweep)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Тест стрелок и индикаторов приборной панели (Sweep)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        "act_angel" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Angel Eyes & Headlight Welcome Test"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Angel Eyes və Fara Qarşılama Testi"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Тест ангельских глазок и фар (Приветствие)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        "act_kafas_heater" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "KAFAS Camera Lens Heater (Defrost)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "KAFAS Kamera Lensi Qızdırıcısı (Defrost)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Обогрев объектива камеры KAFAS (Defrost)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        "act_acc_alignment" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "ACC Radar Angle & Deviation Live Measurement"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "ACC Radar Bucağı və Meyletmə Canlı Ölçümü"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Измерение угла и отклонения радара ACC"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        "act_hv_pyro" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "HV Pyro-fuse Disconnect Resistance Check"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "HV Piro-sigorta Qoruyucu Müqavimət Yoxlaması"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Проверка пиропатрона высоковольтного предохранителя"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        else -> title
    }

    fun localizedDescription(strings: com.punhanaghayev.bimmerpro.core.i18n.AppStrings): String = when (id) {
        "act_fan" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Runs electric cooling fan at high speed for 5 seconds."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Elektrikli pəri 5 saniyə yüksək sürətdə işə salır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Запуск вентилятора на высоких оборотах на 5 секунд."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        "act_fuel" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Triggers fuel feed pump for pressure line test."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Təzyiq testi üçün yanacaq nasosunu işə salır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Активация топливного насоса для проверки давления."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        "act_exhaust" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Actuates rear muffler flap to verify mechanical movement."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Arxa səsboğucu qapağını açıb-bağlayaraq mexanizmi yoxlayır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Проверка работоспособности заслонки глушителя."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        "act_kombi" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Sweeps tachometer/speedo needles and lights all warning LEDs."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Dövr və sürət əqrəblərini tam fırladır, bütün xəbərdarlıq LED-lərini yandırır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Полный прогон стрелок тахометра/спидометра и тест всех LED."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        "act_angel" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Tests front DRL rings at full brightness."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Ön gündüz LED halqalarını tam parlaqlıqda yoxlayır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Проверка светодиодных колец DRL на максимальной яркости."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        "act_kafas_heater" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Actuates windshield camera heating element to clear frost and condensation."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Kamera lensi üzərindəki buzlanma və dumanlanmanı aradan qaldırmaq üçün qızdırıcını işə salır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Включение обогрева камеры лобового стекла для устранения запотевания и льда."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        "act_acc_alignment" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Reads live angular yaw and pitch mounting deviations via UDS 0x22 DID 0x3240."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "UDS 0x22 DID 0x3240 ilə radarda üfüqi və şaquli meyletmə bucaqlarını canlı oxuyur."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Чтение угловых отклонений монтажа радара по горизонтали и вертикали."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        "act_hv_pyro" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Verifies resistance and trigger circuit readiness of the battery safety terminal."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Yüksək gərginlik pirotexniki təhlükəsizlik xəttinin müqavimətini yoxlayır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Проверка сопротивления цепи пиропатрона аварийного отключения HV."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        else -> description
    }
}

data class IbsBatteryHealth(
    val stateOfChargePercent: Int,
    val stateOfHealthPercent: Int,
    val capacityAh: Int,
    val isAgm: Boolean,
    val last5DaysSoc: List<Int>
)

object BmwActuatorCatalog {
    fun defaultTests(): List<ActuatorTestItem> = listOf(
        ActuatorTestItem("act_fan", "Radyatör Soğutma Fanı (%80 Test)", "DME/DDE", EcuAddress.DME_DDE, 0x4101, "Elektrikli fanı 5 saniye yüksek devirde çalıştırır."),
        ActuatorTestItem("act_fuel", "Düşük Basınç Yakıt Pompası (EKPS)", "DME/DDE", EcuAddress.DME_DDE, 0x4105, "Besleme pompasını basınç testi için tetikler."),
        ActuatorTestItem("act_exhaust", "Egzoz Flapı Aç/Kapa Testi", "DME/DDE", EcuAddress.DME_DDE, 0x4112, "Arka susturucu valfini açıp kapatarak mekanizmayı doğrular."),
        ActuatorTestItem("act_kombi", "Gösterge İbre & LED Testi (Sweep)", "KOMBI", EcuAddress.KOMBI, 0x4201, "Devir ve hız ibrelerini tam tur döndürüp tüm ikaz LED'lerini yakar."),
        ActuatorTestItem("act_angel", "Angel Eyes & Far Selamlama Testi", "CAS4/BDC", EcuAddress.ZGW_BDC, 0x4302, "Ön gündüz LED halkalarını tam parlaklıkta test eder."),
        ActuatorTestItem("act_kafas_heater", "KAFAS Kamera Isıtıcısı (Defrost)", "KAFAS", EcuAddress.KAFAS_CAMERA, 0x4120, "Ön cam kamera lensi rezistansını 30 saniye boyunca çalıştırır."),
        ActuatorTestItem("act_acc_alignment", "ACC Radar Sensörü Açı & Sapma Ölçümü", "ACC/FRR", EcuAddress.ACC_RADAR, 0x3240, "Radar sensörü yatay/dikey mekanik montaj açısını canlı okur."),
        ActuatorTestItem("act_hv_pyro", "HV Piromekanik Güvenlik Sigortası Kontrolü", "SME", EcuAddress.SME_HV_BATTERY, 0x4130, "Kaza anında HV hattını kesen piroteknik sigortanın direncini doğrular.")
    )

    fun defaultIbsHealth(): IbsBatteryHealth = IbsBatteryHealth(
        stateOfChargePercent = 82,
        stateOfHealthPercent = 88,
        capacityAh = 90,
        isAgm = true,
        last5DaysSoc = listOf(86, 84, 79, 81, 82)
    )

    fun defaultShortCircuitChannels(ecuName: String = "FRM/FEM", ecuAddress: Int = EcuAddress.FRM): List<ShortCircuitChannel> = listOf(
        ShortCircuitChannel("sc_low_beam_l", "Sol Kısa Far (Low Beam L)", ecuName, ecuAddress, 0x01, 0, false),
        ShortCircuitChannel("sc_low_beam_r", "Sağ Kısa Far (Low Beam R)", ecuName, ecuAddress, 0x02, 2, false),
        ShortCircuitChannel("sc_angel_l", "Sol Angel Eyes / DRL Çıkışı", ecuName, ecuAddress, 0x03, 50, true),
        ShortCircuitChannel("sc_plate", "Arka Plaka Aydınlatması", ecuName, ecuAddress, 0x04, 12, false),
        ShortCircuitChannel("sc_fog_r", "Sağ Ön Sis Farı Çıkışı", ecuName, ecuAddress, 0x05, 0, false)
    )
}
