package com.punhanaghayev.bimmerpro.data

import com.punhanaghayev.bimmerpro.core.bmw.BmwChassis
import com.punhanaghayev.bimmerpro.core.bmw.DtcMemoryType
import com.punhanaghayev.bimmerpro.core.bmw.EcuAddress
import com.punhanaghayev.bimmerpro.core.bmw.EpbServiceState
import com.punhanaghayev.bimmerpro.core.bmw.ExhaustFlapMode
import com.punhanaghayev.bimmerpro.core.bmw.TamperStatus
import java.io.File
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import com.punhanaghayev.bimmerpro.core.protocol.ConnectionState
import com.punhanaghayev.bimmerpro.core.protocol.PhysicalUdsParser
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BimmerRepositoryTest {

    @Test
    fun `f10 simulator connects, codes preset, creates backup, and restores cleanly`() = runTest {
        val repo = BimmerRepository()
        repo.connect(AdapterType.SIMULATOR_F10)

        val state = repo.session.value
        assertTrue(state.connectionState is ConnectionState.Connected)
        assertEquals(BmwChassis.F10, state.vehicleProfile?.chassis)
        assertTrue(repo.protocolLogs.value.isNotEmpty())

        val result = repo.applyPresetCoding("start_stop_mem", 1)
        assertTrue(result.isSuccess)
        assertEquals(1, repo.session.value.presets.first { it.id == "start_stop_mem" }.currentOptionIndex)
        assertEquals(1, repo.session.value.backups.size)

        val backupId = repo.session.value.backups.first().id
        repo.restoreBackup(backupId)
        assertEquals(0, repo.session.value.presets.first { it.id == "start_stop_mem" }.currentOptionIndex)
    }

    @Test
    fun `coding is blocked when battery voltage drops below safety threshold`() = runTest {
        val repo = BimmerRepository()
        repo.connect(AdapterType.SIMULATOR_F10)
        repo.adjustSimulatedVoltage(11.8)

        val result = repo.applyPresetCoding("mirror_fold", 2)
        assertTrue(result.isFailure)
        assertEquals(0, repo.session.value.presets.first { it.id == "mirror_fold" }.currentOptionIndex)
    }

    @Test
    fun `g30 secure coding locked ecu returns security access denied`() = runTest {
        val repo = BimmerRepository()
        repo.connect(AdapterType.SIMULATOR_G30)

        val result = repo.applyPresetCoding("video_in_motion", 1)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("0x33") == true)
    }

    @Test
    fun `e90 simulator detects odometer mismatch and clears dtcs`() = runTest {
        val repo = BimmerRepository()
        repo.connect(AdapterType.SIMULATOR_E90)

        val profile = repo.session.value.vehicleProfile
        assertNotNull(profile)
        assertEquals(TamperStatus.SUSPICIOUS_MISMATCH, profile?.mileageVerification?.status)
        assertTrue(repo.session.value.dtcs.isNotEmpty())

        repo.clearAllDtcs()
        assertTrue(repo.session.value.dtcs.isEmpty())
    }

    @Test
    fun `f30 and g20 simulators connect and write idrive service history`() = runTest {
        val repo = BimmerRepository()
        repo.connect(AdapterType.SIMULATOR_F30)
        assertEquals(BmwChassis.F30, repo.session.value.vehicleProfile?.chassis)

        repo.connect(AdapterType.SIMULATOR_G20)
        assertEquals(BmwChassis.G20, repo.session.value.vehicleProfile?.chassis)

        val beforeCount = repo.session.value.serviceHistory.size
        repo.writeServiceHistoryRecord("2026-11-18", 40_000, "Punhan Garage", listOf("Motor Yağı", "Buji"))
        assertEquals(beforeCount + 1, repo.session.value.serviceHistory.size)
        assertEquals(40_000, repo.session.value.serviceHistory.first().mileageKm)

        repo.writeInjectorCode(1, "9A7C5E")
        assertEquals("9A7C5E", repo.session.value.injectorCodes.first { it.cylinder == 1 }.imaCode)

        repo.resetZfAdaptations()
        assertTrue(repo.session.value.zfAdaptations.all { it.pressureMbar == 0 && it.fillTimeMs == 0 })

        repo.connect(AdapterType.SIMULATOR_F10)
        assertTrue(repo.session.value.presets.size >= 25)
        assertTrue(repo.session.value.cafdParameters.size >= 10)
        val firstActuator = repo.session.value.actuatorTests.first()
        repo.runActuatorTest(firstActuator)
        assertTrue(repo.session.value.lastServiceResult?.success == true)

        val initialDtcs = repo.session.value.dtcs.size
        repo.clearEcuDtcs(EcuAddress.DME_DDE, "DDE7")
        assertTrue(repo.session.value.dtcs.size < initialDtcs)
    }

    @Test
    fun `u11 simulator connects with 3-layer dtc memory, short-circuit reset, exhaust flap and asd control`() = runTest {
        val repo = BimmerRepository()
        repo.connect(AdapterType.SIMULATOR_U11)

        val state = repo.session.value
        assertEquals(BmwChassis.U11, state.vehicleProfile?.chassis)
        assertTrue(state.dtcs.any { it.memoryType == DtcMemoryType.ERROR_MEMORY })
        assertTrue(state.dtcs.any { it.memoryType == DtcMemoryType.INFO_MEMORY })
        assertTrue(state.liveChannels.all { it.history.size >= 20 })

        // Short-circuit lock reset
        val lockedChannel = state.shortCircuitChannels.firstOrNull { it.isLocked }
        assertNotNull(lockedChannel)
        repo.resetShortCircuitChannel(lockedChannel!!)
        val updatedChannel = repo.session.value.shortCircuitChannels.first { it.id == lockedChannel.id }
        assertFalse(updatedChannel.isLocked)
        assertEquals(0, updatedChannel.faultCounter)

        // Exhaust flap & ASD tuning
        repo.setExhaustFlapMode(ExhaustFlapMode.FORCE_OPEN)
        assertEquals(ExhaustFlapMode.FORCE_OPEN, repo.session.value.soundTuning.exhaustFlapMode)

        repo.setAsdState(enabled = false, volumePercent = 40)
        assertFalse(repo.session.value.soundTuning.asdEnabled)
        assertEquals(40, repo.session.value.soundTuning.asdVolumePercent)

        // F10 permanent + info + error DTC check & U-series VIN inference
        repo.connect(AdapterType.SIMULATOR_F10)
        assertTrue(repo.session.value.dtcs.any { it.memoryType == DtcMemoryType.PERMANENT_MEMORY })
        assertEquals(BmwChassis.U11, PhysicalUdsParser.inferChassisFromVin("WBA71EF0905X18420"))

        // EPB two-step service mode
        repo.setEpbServiceMode(true)
        assertEquals(EpbServiceState.SERVICE_MODE_OPEN, repo.session.value.epbServiceState)
        repo.setEpbServiceMode(false)
        assertEquals(EpbServiceState.NORMAL, repo.session.value.epbServiceState)

        // Freeze Frame fetch & Android Auto sensor selection
        val firstDtc = repo.session.value.dtcs.first()
        repo.fetchDtcFreezeFrame(firstDtc)
        repo.setAndroidAutoPids(listOf("rpm", "oil_temp"))
        assertEquals(listOf("rpm", "oil_temp"), repo.session.value.androidAutoPidIds)

        // Support bundle export
        val tmpDir = File(System.getProperty("java.io.tmpdir") ?: ".", "bimmerpro_test_support")
        val bundleFile = repo.exportSupportBundle(tmpDir)
        assertTrue(bundleFile.exists() && bundleFile.readText().contains("WBA5C31040D842910"))
        bundleFile.delete()
    }
}
