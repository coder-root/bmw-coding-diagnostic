package com.punhanaghayev.bimmerpro.core.bmw

import com.punhanaghayev.bimmerpro.core.protocol.CafdBitmaskEngine
import com.punhanaghayev.bimmerpro.core.protocol.HsfzFrame
import com.punhanaghayev.bimmerpro.core.simulator.SimulatorTraceCatalog
import com.punhanaghayev.bimmerpro.core.simulator.SimulatorTransportAdapter
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * End-to-end verification test suite validating that all 5 pillars of the BimmerCode
 * automotive integration are fully active, compiled, and functional in BMW Expert PRO.
 */
class BimmerCodeIntegrationVerificationTest {

    @Test
    fun `pillar A - authentic UDS HSFZ trace catalog returns genuine BMW vehicle responses`() = runBlocking {
        // Test genuine CAS4 request: UDS 0x22 DID 0x3111
        val reqHex = "000000050001F440223111"
        val expectedRespPrefix = "00000021000140F4623111"
        
        val actualResp = SimulatorTraceCatalog.findResponse(reqHex)
        assertNotNull("Trace response should not be null for genuine CAS4 0x3111 request", actualResp)
        assertTrue(
            "Response should begin with positive response 0x62 to DID 0x3111",
            actualResp?.startsWith(expectedRespPrefix) == true
        )

        // Test SimulatorTransportAdapter integration
        val sim = SimulatorTransportAdapter(AdapterType.SIMULATOR_F10)
        sim.connect()
        val reqBytes = ByteArray(reqHex.length / 2) { i -> reqHex.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
        val respBytes = sim.transceive(reqBytes)
        val respHex = respBytes.joinToString("") { "%02X".format(it) }
        assertTrue("Simulated transceive must return genuine HSFZ trace response", respHex.startsWith(expectedRespPrefix))
        sim.disconnect()
    }

    @Test
    fun `pillar B - ECU map and module addresses are fully registered`() {
        val ecus = BmwEcuAndPidCatalog.allBmwEcus
        assertTrue("Should contain all 28 core BMW physical ECU definitions", ecus.size >= 28)
        
        val addresses = ecus.map { it.address }
        assertTrue("Must include CAS4/FEM (0x40)", addresses.contains(0x40))
        assertTrue("Must include HU_NBT/MGU (0x63)", addresses.contains(0x63))
        assertTrue("Must include KOMBI (0x60)", addresses.contains(0x60))
        assertTrue("Must include HKFM (0x6D)", addresses.contains(0x6D))
        assertTrue("Must include IHKA (0x78)", addresses.contains(0x78))
        assertTrue("Must include ACSM (0x01)", addresses.contains(0x01))
    }

    @Test
    fun `pillar C - Easy Mode catalog includes authentic BimmerCode presets`() {
        val presets = BmwCodingCatalog.buildPresets(isGSeries = false)
        assertTrue("Easy Mode presets must exceed 35 options", presets.size >= 35)

        val keys = presets.map { it.cafdKey }
        // Verify key BimmerCode features
        assertTrue("Must include Auto Start-Stop memory", keys.contains("TCM_MSA_MEMORY"))
        assertTrue("Must include Auto Start-Stop default off", keys.contains("TCM_MSA_DEFAULT_OFF"))
        assertTrue("Must include Remote Tailgate close", keys.contains("HKFM_SCHALTEINSATZ_FBD"))
        assertTrue("Must include Automatic mirror fold on lock", keys.contains("ASP_BEIKLAPPEN_BEI_KOMFORTSCHLIESSEN"))
        assertTrue("Must include Reverse mirror curb tilt", keys.contains("ASP_BORDSTEINAUTOMATIK_DELTA"))
        assertTrue("Must include Welcome Light activation", keys.contains("WL_AKTIVIERUNG"))
        assertTrue("Must include Angel Eyes PWM brightness", keys.contains("MAPPING_STANDL_V_R_PWM_LEVEL_1"))
        assertTrue("Must include LED conversion for fog lights", keys.contains("LED_CONVERSION_NSW"))
        assertTrue("Must include Battery class registration", keys.contains("VCM_AEP_BATTERY_CLASS"))
        assertTrue("Must include Paddle shift retrofit", keys.contains("PADDLES_VERBAUT"))
        assertTrue("Must include Video in Motion 255 kmh", keys.contains("SPEEDLOCK_X_KMH_MAX"))
        assertTrue("Must include Digital speed in BC", keys.contains("BC_DIGITAL_V"))
    }

    @Test
    fun `pillar D - Expert CAFD parameters and bitmask engine match exact byte offsets`() {
        val params = BmwCodingCatalog.buildCafdParameters(isGSeries = false)
        assertTrue("CAFD parameters must be defined", params.isNotEmpty())

        val msaMem = params.first { it.functionName == "TCM_MSA_MEMORY" }
        assertEquals("CAS4", msaMem.ecuName)
        assertEquals(0x00, msaMem.byteOffset)
        assertEquals(0x20, msaMem.bitMask)

        // Test CafdBitmaskEngine write & verify
        val buffer = ByteArray(128)
        buffer[0] = 0x00
        val patched = CafdBitmaskEngine.writeMaskedValue(buffer, byteOffset = 0, bitMask = 0x20, newValue = 1)
        assertEquals(0x20.toByte(), patched[0])
        val readBack = CafdBitmaskEngine.readMaskedValue(patched, byteOffset = 0, bitMask = 0x20)
        assertEquals(1, readBack)
    }

    @Test
    fun `pillar E - SALAPA FA-VO catalog resolves option retrofits and descriptions`() {
        val desc2tb = VoDependencyMapper.lookupOptionDescription("2TB")
        assertTrue("2TB description should mention Sport Otomatik", desc2tb.contains("Sport Otomatik"))

        val desc6wb = VoDependencyMapper.lookupOptionDescription("6WB")
        assertTrue("6WB description should mention Hayalet", desc6wb.contains("Hayalet"))

        val ecus2tb = VoDependencyMapper.affectedEcusFor("2TB")
        assertEquals(4, ecus2tb.size)
        assertTrue(ecus2tb.any { it.second.contains("EGS") })
    }

    @Test
    fun `datapacks directory contains all 5 extracted databases`() {
        val dpDir = if (File("datapacks").exists()) File("datapacks") else File("../datapacks")
        assertTrue("datapacks directory must exist", dpDir.exists())

        val dtc = File(dpDir, "bmw_dtc_pack.json")
        val salapa = File(dpDir, "bmw_salapa_pack.json")
        val coding = File(dpDir, "bmw_coding_pack.json")
        val ecus = File(dpDir, "bmw_ecu_catalog.json")
        val traces = File(dpDir, "bmw_enet_sim_traces.json")

        assertTrue("bmw_dtc_pack.json must exist", dtc.exists() && dtc.length() > 5000)
        assertTrue("bmw_salapa_pack.json must exist", salapa.exists() && salapa.length() > 2_000_000)
        assertTrue("bmw_coding_pack.json must exist", coding.exists() && coding.length() > 10_000_000)
        assertTrue("bmw_ecu_catalog.json must exist", ecus.exists() && ecus.length() > 50_000)
        assertTrue("bmw_enet_sim_traces.json must exist", traces.exists() && traces.length() > 50_000)
    }
}
