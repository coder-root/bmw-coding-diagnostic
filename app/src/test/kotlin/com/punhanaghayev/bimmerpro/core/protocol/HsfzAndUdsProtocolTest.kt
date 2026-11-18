package com.punhanaghayev.bimmerpro.core.protocol

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HsfzAndUdsProtocolTest {

    @Test
    fun `hsfz diagnostic frame encodes and decodes symmetrically`() {
        val uds = UdsCodec.buildReadDataById(0xF190)
        val original = HsfzFrame.diagnosticRequest(targetAddress = 0x10, udsPayload = uds)

        val encoded = original.encode()
        val decoded = HsfzFrame.decode(encoded)

        assertEquals(HsfzFrame.CONTROL_DIAGNOSTIC_DATA, decoded.controlWord)
        assertEquals(HsfzFrame.TESTER_ADDRESS, decoded.sourceAddress)
        assertEquals(0x10, decoded.targetAddress)
        assertArrayEquals(uds, decoded.payload)
    }

    @Test
    fun `uds codec parses positive and negative responses accurately`() {
        val posRaw = byteArrayOf(0x62, 0xF1.toByte(), 0x90.toByte(), 0x57, 0x42, 0x41)
        val pos = UdsCodec.parseResponse(posRaw)
        assertTrue(pos is UdsResponse.Positive)
        pos as UdsResponse.Positive
        assertEquals(UdsServiceId.READ_DATA_BY_ID, pos.serviceId)
        assertEquals(5, pos.payload.size)

        val negRaw = byteArrayOf(0x7F, 0x2E, 0x33)
        val neg = UdsCodec.parseResponse(negRaw)
        assertTrue(neg is UdsResponse.Negative)
        neg as UdsResponse.Negative
        assertEquals(UdsServiceId.WRITE_DATA_BY_ID, neg.requestedServiceId)
        assertEquals(UdsNegativeResponseCode.SECURITY_ACCESS_DENIED, neg.nrc)
    }
}
