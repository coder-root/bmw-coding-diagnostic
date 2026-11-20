package com.punhanaghayev.bimmerpro.core.protocol

import com.punhanaghayev.bimmerpro.core.bmw.CodingBackupSnapshot
import com.punhanaghayev.bimmerpro.core.bmw.VoDependencyMapper
import com.punhanaghayev.bimmerpro.data.BackupStorage
import com.punhanaghayev.bimmerpro.data.GarageHistoryStorage
import com.punhanaghayev.bimmerpro.data.SavedVehicleRecord
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.nio.file.Files

class CafdAndTransportUnitTest {

    @Test
    fun `cafd bitmask engine preserves neighboring bits on write`() {
        val initial = byteArrayOf(0b1010_0000.toByte(), 0x55)
        // Mask 0x07 (lower 3 bits): write 0b101 -> should keep upper 1010_0000 intact -> 0b1010_0101
        val updated = CafdBitmaskEngine.writeMaskedValue(initial, byteOffset = 0, bitMask = 0x07, newValue = 0b101)
        assertEquals(0b1010_0101.toByte(), updated[0])
        assertEquals(0x55.toByte(), updated[1])
        assertEquals(0b101, CafdBitmaskEngine.readMaskedValue(updated, byteOffset = 0, bitMask = 0x07))
    }

    @Test
    fun `enet discovery parses vin from gateway announcement`() {
        val raw = byteArrayOf(0x00, 0x00, 0x00, 0x17, 0x00, 0x12) + "WBA5C31040D842910".toByteArray()
        val gw = EnetGatewayDiscovery.parseAnnouncement("169.254.12.34", raw)
        assertNotNull(gw)
        assertEquals("169.254.12.34", gw?.ipAddress)
        assertEquals("WBA5C31040D842910", gw?.vin)
    }

    @Test
    fun `ble elm hex parser and kwp encoder produce valid bytes`() {
        val parsed = BleObdTransportAdapter.parseElmHexPayload("62 F1 90 57 42 41\r\n>")
        assertArrayEquals(byteArrayOf(0x62, 0xF1.toByte(), 0x90.toByte(), 0x57, 0x42, 0x41), parsed)

        val kwp = UsbKdcanTransportAdapter.encodeKwpFrame(0x12, byteArrayOf(0x22, 0x10, 0x0A))
        assertEquals(0x83.toByte(), kwp[0])
        assertEquals(0x12.toByte(), kwp[1])
        assertEquals(0xF1.toByte(), kwp[2])
    }

    @Test
    fun `backup storage persists and reloads coding snapshots`() {
        val tempDir = Files.createTempDirectory("bimmerpro_bkp_test").toFile()
        try {
            val storage = BackupStorage(tempDir)
            val snap = CodingBackupSnapshot(
                id = "bkp_1",
                timestamp = "16:20:00",
                ecuName = "CAS4",
                ecuAddress = 0x40,
                cafdFile = "TCM_MSA_MEMORY",
                summary = "Kapalı -> Aktif",
                previousValues = mapOf("start_stop_mem" to 0)
            )
            storage.saveBackups(listOf(snap))
            val loaded = storage.loadBackups()
            assertEquals(1, loaded.size)
            assertEquals("CAS4", loaded.first().ecuName)
            assertEquals(0, loaded.first().previousValues["start_stop_mem"])
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `vlinker wifi stn parser extracts voltage and uds payload`() {
        val volts = TcpElmStnTransportAdapter.parseVoltage("12.6V\r\n>")
        assertEquals(12.6, volts ?: 0.0, 0.01)

        val uds = TcpElmStnTransportAdapter.parseStnUdsResponse("06 F1 62 F1 90 57 42 41\r\n>")
        assertArrayEquals(byteArrayOf(0x62, 0xF1.toByte(), 0x90.toByte(), 0x57, 0x42, 0x41), uds)
    }

    @Test
    fun `backup json export import and garage multi-vin history work accurately`() {
        val tempDir = Files.createTempDirectory("bimmerpro_v170_test").toFile()
        try {
            val storage = BackupStorage(tempDir)
            val snap = CodingBackupSnapshot("id1", "10:00", "CAS4", 0x40, "MSA", "Test", mapOf("k" to 1))
            val json = storage.exportToJson(listOf(snap))
            val imported = storage.importFromJson(json)
            assertEquals(1, imported.size)
            assertEquals("CAS4", imported.first().ecuName)

            val garage = GarageHistoryStorage(tempDir)
            garage.upsertVehicle(SavedVehicleRecord("WBA5C31040D842910", "BMW 520d", "F10", 148000, "19.11", "vLinker"))
            assertEquals(1, garage.loadVehicles().size)
            garage.removeVehicle("WBA5C31040D842910")
            assertEquals(0, garage.loadVehicles().size)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `kwp2000 codec and vo dependency mapper work accurately`() {
        val rawKwp = byteArrayOf(0x58, 0x02, 0x30, 0xFF.toByte(), 0x21, 0x29, 0xF4.toByte(), 0x41)
        val dtcs = Kwp2000Codec.parseDtcResponse(rawKwp)
        assertEquals(2, dtcs.size)
        assertEquals("30FF", dtcs[0].first)
        assertEquals("29F4", dtcs[1].first)

        val affected = VoDependencyMapper.affectedEcusFor("2TB")
        assertEquals(4, affected.size)
    }
}
