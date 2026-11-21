package com.punhanaghayev.bimmerpro.core.i18n

import com.punhanaghayev.bimmerpro.core.bmw.BmwEcuAndPidCatalog
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLocalizationTest {

    @Test
    fun `all four languages provide non-empty translations and valid format strings`() {
        AppLanguage.entries.forEach { lang ->
            val s = AppLocalization.stringsFor(lang)
            assertTrue(s.tabGarage.isNotBlank())
            assertTrue(s.scanHardwareBtn.isNotBlank())
            assertTrue(s.supportedArchTitle.isNotBlank())

            // Verify HV & Service format specifiers never throw IllegalFormatConversionException
            val tempFormatted = s.moduleTempsLabel.format(22.4, 28.6, 25.1)
            assertTrue(tempFormatted.isNotBlank())
            val cellFormatted = s.hvCellDistributionLabel.format(3980, 3992, 4004)
            assertTrue(cellFormatted.isNotBlank())
            val routineConfirm = s.routineConfirmWarning.format("Battery Registration", "DME")
            assertTrue(routineConfirm.isNotBlank())
        }
        assertEquals(AppLanguage.AZ, AppLanguage.fromCode("az"))
        assertEquals(AppLanguage.RU, AppLanguage.fromCode("ru"))
        assertEquals(AppLanguage.EN, AppLanguage.fromCode("en"))
        assertEquals(AppLanguage.EN, AppLanguage.fromCode("unknown"))
    }

    @Test
    fun `all live PIDs including percentage units format safely without crash`() {
        BmwEcuAndPidCatalog.livePidDefinitions.forEach { def ->
            val formatted = "DID 0x%04X • %s".format(def.did, def.unit)
            assertTrue(formatted.startsWith("DID 0x"))
        }
    }

    @Test
    fun `global text localizer translates dynamic domain strings to English`() {
        assertEquals("Clutch A", GlobalTextLocalizer.localize("Kavrama A", AppLanguage.EN))
        assertEquals("Scan Completed Successfully!", GlobalTextLocalizer.localize("Tarama Başarıyla Tamamlandı!", AppLanguage.EN))
        assertEquals("10 ECUs scanned, 3 fault(s) detected.", GlobalTextLocalizer.localize("10 ECU tarandı, 3 arıza kaydı tespit edildi.", AppLanguage.EN))
        assertEquals("Full Support", GlobalTextLocalizer.localize("Tam Destek", AppLanguage.EN))
    }

    @Test
    fun `all 17 requested physical adapters are registered`() {
        val names = AdapterType.entries.map { it.displayName }.toSet()
        val required = listOf(
            "ENET Cable / Wi-Fi",
            "UniCarScan - 3000",
            "UniCarScan - 2100",
            "OBDLink CX / CX+",
            "OBDLink MX+",
            "vLinker Wi-Fi",
            "vLinker Bluetooth",
            "Vgate iCar Bluetooth",
            "Veepeak Bluetooth",
            "Konnwei",
            "modBM ENET",
            "THOR",
            "KIES Wi-Fi",
            "MG Flasher",
            "bm3",
            "MHD Universal",
            "xHP"
        )
        required.forEach { expected ->
            assertTrue("Missing adapter: $expected", expected in names)
        }
    }
}
