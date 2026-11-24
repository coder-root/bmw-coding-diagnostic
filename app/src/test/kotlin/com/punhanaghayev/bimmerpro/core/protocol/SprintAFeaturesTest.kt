package com.punhanaghayev.bimmerpro.core.protocol

import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage
import com.punhanaghayev.bimmerpro.core.report.ReportLocalization
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SprintAFeaturesTest {

    @Test
    fun `multi-DID UDS 0x22 builder and parser round-trip accurately`() {
        val dids = listOf(0xF40C, 0xF40D, 0x5814)
        val req = UdsCodec.buildReadMultipleDids(dids)
        assertEquals(7, req.size)
        assertEquals(0x22, req[0].toInt() and 0xFF)
        assertEquals(0xF4, req[1].toInt() and 0xFF)
        assertEquals(0x0C, req[2].toInt() and 0xFF)
        assertEquals(0x58, req[5].toInt() and 0xFF)
        assertEquals(0x14, req[6].toInt() and 0xFF)

        val responsePayload = byteArrayOf(
            0xF4.toByte(), 0x0C, 0x1A, 0x2B,
            0xF4.toByte(), 0x0D, 0x55,
            0x58, 0x14, 0x04, 0xB0.toByte()
        )
        val parsed = UdsCodec.parseMultiDidResponse(
            responsePayload,
            mapOf(0xF40C to 2, 0xF40D to 1, 0x5814 to 2)
        )
        assertEquals(3, parsed.size)
        assertArrayEquals(byteArrayOf(0x1A, 0x2B), parsed[0xF40C])
        assertArrayEquals(byteArrayOf(0x55), parsed[0xF40D])
        assertArrayEquals(byteArrayOf(0x04, 0xB0.toByte()), parsed[0x5814])
    }

    @Test
    fun `adapter health diagnostics grades latency, loss and voltage accurately`() {
        val excellent = AdapterHealthDiagnostics.evaluate(
            adapterName = "ENET Cable / Wi-Fi",
            protocolStack = "HSFZ / DoIP",
            latenciesMs = listOf(12L, 15L, 14L, 18L, 16L),
            totalPings = 5,
            batteryVoltage = 13.4
        )
        assertEquals(AdapterReadinessGrade.EXCELLENT, excellent.grade)
        assertEquals(0, excellent.packetLossPercent)
        assertTrue(excellent.localizedSummary(AppLanguage.EN).contains("Ready"))

        val lowVoltage = AdapterHealthDiagnostics.evaluate(
            adapterName = "vLinker Wi-Fi",
            protocolStack = "STN2255",
            latenciesMs = listOf(20L, 22L, 25L, 21L, 24L),
            totalPings = 5,
            batteryVoltage = 11.7
        )
        assertEquals(AdapterReadinessGrade.CRITICAL, lowVoltage.grade)
    }

    @Test
    fun `multilingual PDF report labels cover all four languages`() {
        AppLanguage.entries.forEach { lang ->
            val labels = ReportLocalization.labelsFor(lang)
            assertTrue(labels.docTitle.isNotBlank())
            assertTrue(labels.sec1Identity.isNotBlank())
            assertTrue(labels.sec8Dtcs.isNotBlank())
            assertTrue(labels.noDtcsClean.isNotBlank())
        }
        assertEquals("en", ReportLocalization.labelsFor(AppLanguage.EN).langCode)
        assertEquals("ru", ReportLocalization.labelsFor(AppLanguage.RU).langCode)
    }
}
