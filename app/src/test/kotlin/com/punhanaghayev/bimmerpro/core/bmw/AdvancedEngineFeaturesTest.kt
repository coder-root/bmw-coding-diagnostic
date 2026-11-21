package com.punhanaghayev.bimmerpro.core.bmw

import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import com.punhanaghayev.bimmerpro.data.BackupStorage
import com.punhanaghayev.bimmerpro.data.BimmerRepository
import com.punhanaghayev.bimmerpro.data.DragMode
import com.punhanaghayev.bimmerpro.data.GarageHistoryStorage
import com.punhanaghayev.bimmerpro.data.VehicleScanSnapshot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class AdvancedEngineFeaturesTest {

    @Test
    fun `ecu variant resolver parses ascii and binary svk payloads`() {
        val asciiPayload = byteArrayOf(0xF1.toByte(), 0x01) +
            "HWEL_00001A2B|BTLD_00001001|SWFL_000024C1|CAFD_000001A7".toByteArray()
        val svk = EcuVariantResolver.parseSvk(EcuAddress.DME_DDE, BmwChassis.F10, asciiPayload)
        assertNotNull(svk)
        assertEquals("HWEL_00001A2B", svk!!.hwel)
        assertEquals("SWFL_000024C1", svk.swfl)
        assertEquals("CAFD_000001A7", svk.cafd)
        assertTrue(svk.resolvedVariant.isNotBlank())

        val binPayload = byteArrayOf(
            0xF1.toByte(), 0x01,
            0x12, 0x34, 0x56, 0x78,
            0x9A.toByte(), 0xBC.toByte(), 0xDE.toByte(), 0xF0.toByte()
        )
        val binSvk = EcuVariantResolver.parseSvk(EcuAddress.ZGW_BDC, BmwChassis.G05, binPayload)
        assertNotNull(binSvk)
        assertEquals("HWEL_12345678", binSvk!!.hwel)
        assertEquals("SWFL_9ABCDEF0", binSvk.swfl)

        val catalogFile = listOf(
            File("src/main/assets/datapacks/bmw_ecu_catalog.json"),
            File("../datapacks/bmw_ecu_catalog.json"),
            File("datapacks/bmw_ecu_catalog.json")
        ).first { it.exists() }
        catalogFile.inputStream().use { EcuVariantResolver.initializeFromAssetStream(it) }
        assertEquals(616, EcuVariantResolver.catalogSize())
        assertEquals(0x19, EcuAddress.VTG_TRANSFER)
    }

    @Test
    fun `vo coding engine builds hex byte diff plan and parses fsc statuses`() {
        val buffers = mapOf(
            EcuAddress.KOMBI to ByteArray(32) { 0x00 },
            EcuAddress.HU_MEDIA to ByteArray(32) { 0x00 }
        )
        val plan = VoCodingEngine.buildCodingPlan("6WB", "Multifunctional Instrument Display", buffers)
        assertEquals("6WB", plan.voCode)
        assertTrue(plan.diffs.isNotEmpty())
        assertTrue(plan.diffs.any { it.beforeByteHex != it.afterByteHex })

        val fscPayload = byteArrayOf(
            0x30, 0x08,
            0x00, 0xDE.toByte(), 0x01, 0x00, // Nav Accepted
            0x01, 0x43, 0x01, 0x01,          // CarPlay Rejected
            0x00, 0x7E, 0x01, 0x02           // SLI Not Loaded
        )
        val certs = VoCodingEngine.parseFscStatusPayload("HU_MGU", EcuAddress.HU_MEDIA, fscPayload)
        assertEquals(3, certs.size)
        assertEquals(FscCertificateStatus.ACCEPTED, certs[0].status)
        assertEquals(FscCertificateStatus.REJECTED, certs[1].status)
        assertEquals(FscCertificateStatus.NOT_LOADED, certs[2].status)
    }

    @Test
    fun `virtual dyno calculator produces monotonic rpm sweep and realistic peak power`() {
        val sample = VirtualDynoCalculator.calculateSample(
            speedKmh = 95.0,
            prevSpeedKmh = 75.0,
            deltaSeconds = 1.0,
            rpm = 5200.0,
            massKg = 1740
        )
        assertNotNull(sample)
        assertTrue(sample!!.horsepower > 100.0)
        assertTrue(sample.torqueNm > 100.0)

        val simPull = VirtualDynoCalculator.generateSimulatedPull(BmwChassis.G20, "B58B30 374 HP")
        assertTrue(simPull.points.size >= 10)
        assertTrue(simPull.peakHp >= 300)
        assertTrue(simPull.peakTorqueNm >= 300)
    }

    @Test
    fun `offline fault analyzer correlates multi-fault root causes`() {
        val dtcs = listOf(
            DtcFault(
                id = "d1",
                code = "290900",
                ecuAddress = EcuAddress.DME_DDE,
                ecuName = "DDE7",
                title = "EGR Air Mass",
                technicalDetail = "Air mass too high",
                severity = DtcSeverity.WARNING,
                status = DtcStatus.ACTIVE,
                freezeFrame = FreezeFrameData(145000, 12.0, 88, 1200, 2),
                istaGuidance = "Check EGR"
            ),
            DtcFault(
                id = "d2",
                code = "D35A53",
                ecuAddress = EcuAddress.DSC,
                ecuName = "DSC",
                title = "Missing Engine Torque Signal",
                technicalDetail = "Torque intervention active",
                severity = DtcSeverity.WARNING,
                status = DtcStatus.ACTIVE,
                freezeFrame = FreezeFrameData(145000, 12.0, 88, 1200, 1),
                istaGuidance = "Check DME first"
            )
        )
        val report = OfflineFaultAnalyzer.analyze(dtcs, emptyList(), null, 12.4, emptyList())
        assertTrue(report.rootCauseCorrelations.any { it.id == "rc_dpf_dsc_torque" })
    }

    @Test
    fun `garage history storage computes comparative inspection diff across scans`() {
        val dir = Files.createTempDirectory("garage_hist_test").toFile()
        try {
            val storage = GarageHistoryStorage(dir)
            val snap1 = VehicleScanSnapshot("01.01.2026 10:00", 120000, "F010-20-07", 18, listOf("245700", "8040B9"), 74)
            val snap2 = VehicleScanSnapshot("01.04.2026 14:30", 124500, "F010-21-03", 18, listOf("8040B9", "120308"), 81)

            val (_, firstDiff) = storage.appendScanSnapshot("WBA5C31000TEST001", "520d", "F10", 120000, "ENET", snap1)
            assertEquals(null, firstDiff)

            val (records, secondDiff) = storage.appendScanSnapshot("WBA5C31000TEST001", "520d", "F10", 124500, "ENET", snap2)
            assertEquals(1, records.size)
            assertEquals(2, records.first().scanSnapshots.size)
            assertNotNull(secondDiff)
            assertEquals(4500, secondDiff!!.kmDelta)
            assertEquals(listOf("120308"), secondDiff.newlyAppearedDtcs)
            assertEquals(listOf("245700"), secondDiff.resolvedDtcs)
            assertEquals(listOf("8040B9"), secondDiff.persistentDtcs)
            assertEquals(7, secondDiff.healthScoreDelta)
            assertTrue(secondDiff.iLevelChanged)

            val reloaded = storage.loadVehicles()
            assertEquals(2, reloaded.first().scanSnapshots.size)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `backup storage persists cafd byte snapshots in dat and json`() {
        val dir = Files.createTempDirectory("bkp_bytes_test").toFile()
        try {
            val storage = BackupStorage(dir)
            val snap = CodingBackupSnapshot(
                id = "bkp_1",
                timestamp = "12:00:00",
                ecuName = "KOMBI",
                ecuAddress = EcuAddress.KOMBI,
                cafdFile = "CAFD_00000069",
                summary = "Digital Speed",
                previousValues = mapOf("p1" to 0),
                cafdByteSnapshot = mapOf(EcuAddress.KOMBI to "00AA11BB")
            )
            storage.saveBackups(listOf(snap))
            val loaded = storage.loadBackups()
            assertEquals(1, loaded.size)
            assertEquals("00AA11BB", loaded.first().cafdByteSnapshot[EcuAddress.KOMBI])

            val jsonText = storage.exportToJson(loaded)
            val fromJson = storage.importFromJson(jsonText)
            assertEquals("00AA11BB", fromJson.first().cafdByteSnapshot[EcuAddress.KOMBI])
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `repository supports trace replay vo byte diff apply rollback drag modes and new ista routines`() = runBlocking {
        val repo = BimmerRepository(appContext = null)
        repo.connect(AdapterType.SIMULATOR_F10)

        val simSession = repo.session.value
        assertTrue(simSession.fscCertificates.isNotEmpty())
        assertTrue(simSession.serviceRoutines.any { it.type == ServiceRoutineType.COOLANT_BLEEDING })
        assertTrue(simSession.serviceRoutines.any { it.type == ServiceRoutineType.DSC_ABS_BLEED })
        assertTrue(simSession.serviceRoutines.any { it.type == ServiceRoutineType.VTG_TRANSFER_CASE_RESET })
        assertTrue(simSession.serviceRoutines.any { it.type == ServiceRoutineType.VALVETRONIC_LIMIT_LEARN })
        assertTrue(simSession.serviceRoutines.any { it.type == ServiceRoutineType.EHC_AIR_SUSPENSION_CAL })

        // Preview & Apply VO Coding plan + atomic byte rollback
        val voPlan = repo.previewVoCodingPlan("6WB", "Digital Cluster")
        assertNotNull(voPlan)
        repo.applyVoCodingPlan(voPlan!!)
        val afterApply = repo.session.value
        assertTrue(afterApply.vehicleProfile!!.faOptions.any { it.code == "6WB" })
        val latestBackup = afterApply.backups.firstOrNull { it.cafdByteSnapshot.isNotEmpty() }
        assertNotNull(latestBackup)
        repo.restoreBackup(latestBackup!!.id)
        assertTrue(repo.session.value.lastOperationMessage!!.contains("geri yüklendi", ignoreCase = true))

        // Execute new ISTA Coolant Bleeding routine
        val coolantRoutine = afterApply.serviceRoutines.first { it.type == ServiceRoutineType.COOLANT_BLEEDING }
        repo.executeServiceRoutine(coolantRoutine)
        assertTrue(repo.session.value.lastServiceResult?.success == true)

        // Extended Drag Mode (100-200 km/h) & Virtual Dyno Pull
        repo.setDragMode(DragMode.HUNDRED_TO_200)
        repo.startPerformanceRun()
        assertTrue(repo.session.value.performanceTimer.isRunning)
        repeat(60) { repo.stepPerformanceTimer(200L) }
        assertFalse(repo.session.value.performanceTimer.isRunning)
        assertNotNull(repo.session.value.performanceTimer.best100To200Sec)
        val dyno = repo.runVirtualDynoPull()
        assertTrue(dyno.peakHp > 100)

        // Trace Playback Mode (both inline Trace and Support Bundle 2-line format)
        val trace = """
            [RX] ECU=0x12 | UDS 0x19 Read DTC | HEX: 59 02 FF 12 03 08 2F
            12:05:10.123 [RX] 0x29 | UDS 0x19 Read DTC (DSC)
              HEX: 59 02 FF D3 5A 53 2F
        """.trimIndent()
        val replayResult = repo.connectWithTraceReplay(trace)
        assertTrue(replayResult.isSuccess)
        assertTrue(repo.session.value.dtcs.any { it.code == "120308" })
        assertTrue(repo.session.value.dtcs.any { it.code == "D35A53" })
    }
}
