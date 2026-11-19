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
)

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
        ActuatorTestItem("act_angel", "Angel Eyes & Far Selamlama Testi", "CAS4/BDC", EcuAddress.ZGW_BDC, 0x4302, "Ön gündüz LED halkalarını tam parlaklıkta test eder.")
    )

    fun defaultIbsHealth(): IbsBatteryHealth = IbsBatteryHealth(
        stateOfChargePercent = 82,
        stateOfHealthPercent = 88,
        capacityAh = 90,
        isAgm = true,
        last5DaysSoc = listOf(86, 84, 79, 81, 82)
    )
}
