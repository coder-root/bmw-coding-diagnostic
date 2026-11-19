package com.punhanaghayev.bimmerpro.data

import com.punhanaghayev.bimmerpro.core.bmw.BmwChassis
import com.punhanaghayev.bimmerpro.core.bmw.EcuAddress
import com.punhanaghayev.bimmerpro.core.bmw.TamperStatus
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import com.punhanaghayev.bimmerpro.core.protocol.ConnectionState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
        val initialDtcs = repo.session.value.dtcs.size
        repo.clearEcuDtcs(EcuAddress.DME_DDE, "DDE7")
        assertTrue(repo.session.value.dtcs.size < initialDtcs)
    }
}
