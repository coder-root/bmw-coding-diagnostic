package com.punhanaghayev.bimmerpro.core.protocol

import com.punhanaghayev.bimmerpro.core.bmw.BmwChassis
import com.punhanaghayev.bimmerpro.core.bmw.BmwDtcDictionary
import com.punhanaghayev.bimmerpro.core.bmw.DtcMemoryType
import com.punhanaghayev.bimmerpro.core.bmw.DtcSeverity
import com.punhanaghayev.bimmerpro.core.bmw.FreezeFrameData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhysicalUdsParserTest {

    @Test
    fun `extracts 17 char vin and infers F10 chassis`() {
        val raw = byteArrayOf(0xF1.toByte(), 0x90.toByte()) + "WBA5C31040D842910".toByteArray()
        val vin = PhysicalUdsParser.extractVin(raw)
        assertEquals("WBA5C31040D842910", vin)
        assertEquals(BmwChassis.F10, PhysicalUdsParser.inferChassisFromVin(vin!!))
    }

    @Test
    fun `parses UDS 0x19 0x02 DTC records accurately`() {
        // subfn=0x02, mask=0xFF, DTC1=245700 status=0x01 (Active), DTC2=CD9304 status=0x08 (Confirmed/Info)
        val payload = byteArrayOf(
            0x02, 0xFF.toByte(),
            0x24, 0x57, 0x00, 0x01,
            0xCD.toByte(), 0x93.toByte(), 0x04, 0x08
        )
        val dtcs = PhysicalUdsParser.parseDtcRecords(0x12, "DDE7", payload, 150_000, 12.6)
        assertEquals(2, dtcs.size)
        assertEquals("245700", dtcs[0].code)
        assertEquals("DPF Partikül Filtresi: Kurum Yükü Sınırda", dtcs[0].title)
        assertEquals(DtcSeverity.WARNING, dtcs[0].severity)
        assertEquals(DtcMemoryType.ERROR_MEMORY, dtcs[0].memoryType)
        assertEquals("CD9304", dtcs[1].code)
        assertEquals(DtcSeverity.SHADOW, dtcs[1].severity)

        val permPayload = byteArrayOf(0x15, 0xFF.toByte(), 0x24, 0x57, 0x00, 0x08)
        val permDtcs = PhysicalUdsParser.parseDtcRecords(
            0x12, "DDE7", permPayload, 150_000, 12.6, DtcMemoryType.PERMANENT_MEMORY
        )
        assertEquals(1, permDtcs.size)
        assertEquals(DtcMemoryType.PERMANENT_MEMORY, permDtcs[0].memoryType)
    }

    @Test
    fun `parses 3-byte big endian odometer km accurately`() {
        // DID 0x100C + 148420 km (0x0243C4)
        val payload = byteArrayOf(0x10, 0x0C, 0x02, 0x43, 0xC4.toByte())
        assertEquals(148_420, PhysicalUdsParser.parseOdometerKm(payload))
        assertEquals(true, BmwDtcDictionary.search("Turbo").isNotEmpty())
    }

    @Test
    fun `parses kwp2000 dtc, freeze frame snapshot, ibs, short circuit and elm isotp multi frame`() {
        val kwpRaw = byteArrayOf(0x58, 0x02, 0x30, 0xFF.toByte(), 0x20, 0x29, 0xD0.toByte(), 0x04)
        val kwpDtcs = PhysicalUdsParser.parseKwpDtcRecords(0x12, "MSD80", kwpRaw, 180_000, 12.5)
        assertEquals(2, kwpDtcs.size)
        assertEquals("30FF", kwpDtcs[0].code)
        assertEquals(DtcMemoryType.ERROR_MEMORY, kwpDtcs[0].memoryType)
        assertEquals(DtcMemoryType.INFO_MEMORY, kwpDtcs[1].memoryType)

        val ffPayload = byteArrayOf(
            0x04, 0x24, 0x57, 0x00, 0x2F, 0x03, 0x02,
            0x10, 0x0C, 0x02, 0x43, 0xC4.toByte(),
            0xF4.toByte(), 0x05, 0x82.toByte()
        )
        val ff = PhysicalUdsParser.parseDtcSnapshotFreezeFrame(ffPayload, FreezeFrameData(0, 12.0, 0, 0, 1))
        assertEquals(148_420, ff.mileageKm)
        assertEquals(90, ff.coolantTempC)
        assertEquals(3, ff.occurrenceCount)

        val ibs = PhysicalUdsParser.parseIbsHealth(byteArrayOf(0x40, 0x10, 84, 91, 90, 1))
        assertNotNull(ibs)
        assertEquals(84, ibs?.stateOfChargePercent)
        assertEquals(5, ibs?.last5DaysSoc?.size)

        val sc = PhysicalUdsParser.parseShortCircuitCounters("FRM", 0x72, byteArrayOf(0x45, 0x01, 0, 50))
        assertEquals(2, sc.size)
        assertTrue(sc[1].isLocked)

        val rawElm = "612 F1 10 14 62 F1 90 57 42 41\n612 F1 21 35 43 33 31 30 34 30\n612 F1 22 44 38 34 32 39 31 30"
        val uds = ElmIsoTpHelper.parseUdsResponse(rawElm)
        assertEquals("WBA5C31040D842910", PhysicalUdsParser.extractVin(uds))
    }
}
