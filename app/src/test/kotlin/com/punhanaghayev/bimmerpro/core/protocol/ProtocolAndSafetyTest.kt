package com.punhanaghayev.bimmerpro.core.protocol

import com.punhanaghayev.bimmerpro.core.bmw.BmwDtcDictionary
import com.punhanaghayev.bimmerpro.core.bmw.BmwEcuAndPidCatalog
import com.punhanaghayev.bimmerpro.core.bmw.DtcFault
import com.punhanaghayev.bimmerpro.core.bmw.DtcSeverity
import com.punhanaghayev.bimmerpro.core.bmw.DtcStatus
import com.punhanaghayev.bimmerpro.core.bmw.EcuAddress
import com.punhanaghayev.bimmerpro.core.bmw.FreezeFrameData
import com.punhanaghayev.bimmerpro.core.bmw.IbsBatteryHealth
import com.punhanaghayev.bimmerpro.core.bmw.LiveSensorChannel
import com.punhanaghayev.bimmerpro.core.bmw.OfflineFaultAnalyzer
import com.punhanaghayev.bimmerpro.core.update.ExternalDtcDto
import com.punhanaghayev.bimmerpro.core.update.ExternalDtcPackDto
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocolAndSafetyTest {

    @Test
    fun `isotp segments and reassembles single frame and multi frame payloads`() {
        val singlePayload = byteArrayOf(0x62, 0xF1.toByte(), 0x90.toByte(), 0x41, 0x42)
        val sfFrames = IsoTpCodec.segment(singlePayload)
        assertEquals(1, sfFrames.size)
        assertArrayEquals(singlePayload, IsoTpCodec.reassemble(sfFrames))

        val multiPayload = "WBA5A510X0D981245_LONG_SVT_PAYLOAD".toByteArray()
        val mfFrames = IsoTpCodec.segment(multiPayload)
        assertTrue(mfFrames.size >= 4)
        assertEquals(0x1, (mfFrames.first()[0].toInt() and 0xF0) ushr 4)
        assertArrayEquals(multiPayload, IsoTpCodec.reassemble(mfFrames))
    }

    @Test
    fun `uds codec builds tester present security access and parses 0x78`() {
        val tp = UdsCodec.buildTesterPresent(suppressResponse = true)
        assertArrayEquals(byteArrayOf(0x3E, 0x80.toByte()), tp)

        val seedReq = UdsCodec.buildSecurityRequestSeed(0x01)
        assertArrayEquals(byteArrayOf(0x27, 0x01), seedReq)

        val sendKey = UdsCodec.buildSecuritySendKey(0x02, byteArrayOf(0xAA.toByte(), 0xBB.toByte()))
        assertArrayEquals(byteArrayOf(0x27, 0x02, 0xAA.toByte(), 0xBB.toByte()), sendKey)

        val pending = UdsCodec.parseResponse(byteArrayOf(0x7F, 0x31, 0x78))
        assertTrue(pending is UdsResponse.Negative)
        assertEquals(UdsNegativeResponseCode.RESPONSE_PENDING, (pending as UdsResponse.Negative).nrc)
    }

    @Test
    fun `bmw ecu and pid catalog exposes 28 ecus and 20 unique live pids`() {
        assertEquals(28, BmwEcuAndPidCatalog.allBmwEcus.size)
        assertEquals(28, BmwEcuAndPidCatalog.allBmwEcus.map { it.address }.distinct().size)

        assertEquals(20, BmwEcuAndPidCatalog.livePidDefinitions.size)
        assertEquals(20, BmwEcuAndPidCatalog.livePidDefinitions.map { it.id }.distinct().size)
    }

    @Test
    fun `offline fault analyzer penalizes low voltage critical dtcs and high dpf soot`() {
        val sampleDtc = DtcFault(
            id = "d1",
            code = "244C00",
            ecuAddress = EcuAddress.DME_DDE,
            ecuName = "DME/DDE",
            title = "Charge-air pressure too low",
            technicalDetail = "Boost deviation",
            severity = DtcSeverity.CRITICAL,
            status = DtcStatus.ACTIVE,
            freezeFrame = FreezeFrameData(185200, 11.7, 92, 2100, 1),
            istaGuidance = "Check boost hoses"
        )
        val report = OfflineFaultAnalyzer.analyze(
            dtcs = listOf(sampleDtc),
            liveChannels = listOf(
                LiveSensorChannel("dpf_soot", "DPF Soot", "g", 38.0, 0.0, 45.0, listOf(38f))
            ),
            ibsHealth = IbsBatteryHealth(64, 58, 90, true, listOf(70, 68, 66, 65, 64)),
            batteryVoltage = 11.7,
            zfAdaptations = emptyList()
        )
        assertTrue(report.overallScore < 70)
        assertEquals(4, report.subsystemScores.size)
        assertTrue(report.recommendations.any { it.contains("DPF") })
        assertTrue(report.recommendations.any { it.contains("11.7") })
    }

    @Test
    fun `session state defaults physical write unlock to false and dtc dictionary loads external pack`() {
        val state = BimmerSessionState()
        assertFalse(state.isPhysicalWriteUnlocked)
        val initialCount = BmwDtcDictionary.totalCodeCount()
        assertTrue(initialCount >= 20)
        BmwDtcDictionary.loadExternalPack(
            ExternalDtcPackDto(
                packVersion = "1.0",
                entries = listOf(
                    ExternalDtcDto("9999AA", "Test External Fault", "Detail", "WARNING", "Guidance")
                )
            )
        )
        assertTrue(BmwDtcDictionary.totalCodeCount() >= initialCount + 1)
    }
}
