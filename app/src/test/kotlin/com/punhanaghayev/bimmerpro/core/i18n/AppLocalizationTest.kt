package com.punhanaghayev.bimmerpro.core.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLocalizationTest {

    @Test
    fun `all four languages provide non-empty translations`() {
        AppLanguage.entries.forEach { lang ->
            val s = AppLocalization.stringsFor(lang)
            assertTrue(s.tabGarage.isNotBlank())
            assertTrue(s.scanHardwareBtn.isNotBlank())
            assertTrue(s.supportedArchTitle.isNotBlank())
        }
        assertEquals(AppLanguage.AZ, AppLanguage.fromCode("az"))
        assertEquals(AppLanguage.RU, AppLanguage.fromCode("ru"))
        assertEquals(AppLanguage.EN, AppLanguage.fromCode("en"))
        assertEquals(AppLanguage.EN, AppLanguage.fromCode("unknown"))
    }
}
