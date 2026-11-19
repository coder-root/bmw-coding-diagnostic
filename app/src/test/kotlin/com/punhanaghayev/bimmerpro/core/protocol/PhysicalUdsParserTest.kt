package com.punhanaghayev.bimmerpro.core.protocol

import com.punhanaghayev.bimmerpro.core.bmw.BmwChassis
import com.punhanaghayev.bimmerpro.core.bmw.DtcSeverity
import org.junit.Assert.assertEquals
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
        assertEquals("CD9304", dtcs[1].code)
        assertEquals(DtcSeverity.SHADOW, dtcs[1].severity)
    }

    @Test
    fun `parses 3-byte big endian odometer km accurately`() {
        // DID 0x100C + 148420 km (0x0243C4)
        val payload = byteArrayOf(0x10, 0x0C, 0x02, 0x43, 0xC4.toByte())
        assertEquals(148_420, PhysicalUdsParser.parseOdometerKm(payload))
    }
}
