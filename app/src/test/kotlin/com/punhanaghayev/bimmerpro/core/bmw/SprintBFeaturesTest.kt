package com.punhanaghayev.bimmerpro.core.bmw

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SprintBFeaturesTest {

    @Test
    fun `extended coding catalog includes new high-demand presets`() {
        val presets = BmwCodingCatalog.buildPresets(isGSeries = false)
        val ids = presets.map { it.id }.toSet()
        assertTrue("asd_disable" in ids)
        assertTrue("seat_heat_temp_l3" in ids)
        assertTrue("cornering_fog_lights" in ids)
        assertTrue("blind_spot_speed" in ids)
        assertTrue("exhaust_flap_sport" in ids)
    }

    @Test
    fun `ncd diff engine computes byte offset and before-after hex for staged changes`() {
        val presets = BmwCodingCatalog.buildPresets(isGSeries = false)
        val cafds = BmwCodingCatalog.buildCafdParameters(isGSeries = false)

        val diffs = NcdDiffEngine.computeStagedDiff(
            presets = presets,
            cafds = cafds,
            stagedPresets = mapOf("start_stop_mem" to 1, "asd_disable" to 1),
            stagedCafds = mapOf("cafd_kombi_logo" to "alpina (0x03)")
        )

        assertEquals(3, diffs.size)
        val kombiDiff = diffs.first { it.parameterName.contains("BMW_LOGO") }
        assertEquals("0x08", kombiDiff.byteOffsetHex)
        assertEquals("0x07", kombiDiff.bitMaskHex)
        assertEquals("0x00", kombiDiff.beforeHex)
        assertEquals("0x03", kombiDiff.afterHex)
    }
}
