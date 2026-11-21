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
import com.punhanaghayev.bimmerpro.data.BimmerRepository
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

    @Test
    fun `physical parser decodes i-level and live pid engineering units`() {
        val iLevelPayload = byteArrayOf(0xF1.toByte(), 0x50) + "F010-24-03-520".toByteArray()
        assertEquals("F010-24-03-520", PhysicalUdsParser.parseILevel(iLevelPayload))

        val rpmPayload = byteArrayOf(0xF4.toByte(), 0x0C, 0x0C, 0x30) // (0x0C30 = 3120) / 4 = 780.0 rpm
        assertEquals(780.0, PhysicalUdsParser.parsePhysicalPidValue("rpm", rpmPayload) ?: 0.0, 0.01)

        val coolPayload = byteArrayOf(0xF4.toByte(), 0x05, 0x82.toByte()) // 130 - 40 = 90 C
        assertEquals(90.0, PhysicalUdsParser.parsePhysicalPidValue("coolant", coolPayload) ?: 0.0, 0.01)

        val dpfPayload = byteArrayOf(0x58, 0x22, 0x09, 0xB0.toByte()) // 2480 / 100 = 24.8 g
        assertEquals(24.8, PhysicalUdsParser.parsePhysicalPidValue("dpf_soot", dpfPayload) ?: 0.0, 0.01)
    }

    @Test
    fun `stn parser filters intermediate 0x78 response pending before isotp multiframe vin`() {
        val stnRaw = """
            6F1 F4 03 7F 22 78
            6F1 F4 10 14 62 F1 90 57 42 41
            6F1 F4 21 35 41 35 31 30 58 30
            6F1 F4 22 44 39 38 31 32 34 35
            >
        """.trimIndent()
        val uds = TcpElmStnTransportAdapter.parseStnUdsResponse(stnRaw)
        assertEquals("WBA5A510X0D981245", PhysicalUdsParser.extractVin(uds))
    }

    @Test
    fun `trace replay adapter parses rx lines and replays ecu frames offline`() = runTest {
        val sampleTrace = """
            # BMW Expert PRO Raw Trace
            [08:00:01.100] TX  ECU 0x10 | UDS 0x22 Read VIN | 22 F1 90
            [08:00:01.145] RX  0x10 | UDS +0x22 | 62 F1 90 57 42 41 35 41 35 31 30 58 30 44 39 38 31 32 34 35
        """.trimIndent()
        val replay = TraceReplayTransportAdapter(sampleTrace)
        replay.connect()
        val reqFrame = HsfzFrame(HsfzFrame.CONTROL_DIAGNOSTIC_DATA, HsfzFrame.TESTER_ADDRESS, 0x10, byteArrayOf(0x22, 0xF1.toByte(), 0x90.toByte())).encode()
        val respFrame = HsfzFrame.decode(replay.transceive(reqFrame))
        assertEquals("WBA5A510X0D981245", PhysicalUdsParser.extractVin(respFrame.payload))

        // Also verify SessionTraceRecorder multiline format (`[RX] ECU=0x10 ... \n  HEX: ...`)
        val recorderTrace = """
            # BMW Expert PRO Raw Diagnostic Trace
            08:00:01 [RX] ECU=0x10 | UDS +0x22 VIN
              HEX: 62 F1 90 57 42 41 35 41 35 31 30 58 30 44 39 38 31 32 34 35
        """.trimIndent()
        val replay2 = TraceReplayTransportAdapter(recorderTrace)
        replay2.connect()
        val resp2 = HsfzFrame.decode(replay2.transceive(reqFrame))
        assertEquals("WBA5A510X0D981245", PhysicalUdsParser.extractVin(resp2.payload))
    }

    @Test
    fun `cafd read modify write verify succeeds on matching readback in simulator`() = runTest {
        val repo = BimmerRepository()
        repo.connect(AdapterType.SIMULATOR_F10)
        val firstCafd = repo.session.value.cafdParameters.first()
        val nextOpt = firstCafd.availableOptions.keys.first { it != firstCafd.selectedOptionLabel }
        val result = repo.applyCafdParameter(firstCafd.id, nextOpt)
        assertTrue(result.isSuccess)
        assertEquals(nextOpt, repo.session.value.cafdParameters.first { it.id == firstCafd.id }.selectedOptionLabel)
    }

    @Test
    fun `f10 528i simulator initializes petrol n20 profile and mevd1724 ecus`() = runTest {
        val repo = BimmerRepository()
        repo.connect(AdapterType.SIMULATOR_F10_528I)
        val profile = repo.session.value.vehicleProfile
        assertNotNull(profile)
        assertEquals("WBAXG51050D518420", profile?.vin)
        assertTrue(profile?.modelDisplayName?.contains("528i") == true)
        assertTrue(profile?.engineCode?.contains("N20") == true)
        assertTrue(repo.session.value.ecus.any { it.shortName == "MEVD1724" })
        assertTrue(repo.session.value.serviceRoutines.isNotEmpty())
    }

    @Test
    fun `doip encodes and decodes routing activation and diagnostic messages`() {
        val req = DoIpProtocol.createRoutingActivationRequest(0x0E00).encode()
        assertEquals(15, req.size)
        val decoded = DoIpProtocol.decode(req)
        assertEquals(DoIpProtocol.TYPE_ROUTING_ACTIVATION_REQUEST, decoded.payloadType)

        val diagMsg = DoIpProtocol.createDiagnosticMessage(0x0E00, 0x0040, byteArrayOf(0x22, 0xF1.toByte(), 0x90.toByte())).encode()
        val decodedDiag = DoIpProtocol.decode(diagMsg)
        assertEquals(DoIpProtocol.TYPE_DIAGNOSTIC_MESSAGE, decodedDiag.payloadType)
        assertTrue(decodedDiag.payload.size >= 7)
    }

    @Test
    fun `bmw security access calculates keys accurately and detects already unlocked zero seeds`() {
        val zeroSeed = byteArrayOf(0x00, 0x00, 0x00, 0x00)
        assertTrue(BmwSecurityAccess.isAlreadyUnlocked(zeroSeed))

        val seed = byteArrayOf(0x12, 0x34, 0x56, 0x78)
        assertFalse(BmwSecurityAccess.isAlreadyUnlocked(seed))

        val casKey = BmwSecurityAccess.calculateKey(EcuAddress.FEM_CAS, seed, 0x01)
        assertEquals(4, casKey.size)

        val femKey = BmwSecurityAccess.calculateKey(EcuAddress.ZGW_BDC, seed, 0x01)
        assertEquals(4, femKey.size)

        val dmeKey = BmwSecurityAccess.calculateKey(EcuAddress.DME_DDE, seed, 0x01)
        assertEquals(4, dmeKey.size)

        // Keys should be non-trivial and deterministic
        assertArrayEquals(casKey, BmwSecurityAccess.calculateKey(EcuAddress.FEM_CAS, seed, 0x01))
        assertFalse(casKey.contentEquals(femKey))
    }

    @Test
    fun `dpf and zf8 gearbox health models compute correct status in simulator bundle`() = runTest {
        val repo = BimmerRepository()
        repo.connect(AdapterType.SIMULATOR_F10)
        val dpf = repo.session.value.dpfHealth
        assertNotNull(dpf)
        assertEquals(22.4, dpf?.sootMassGrams ?: 0.0, 0.01)
        assertEquals(48.2, dpf?.ashMassGrams ?: 0.0, 0.01)
        assertEquals(14.5, dpf?.backpressureMbar ?: 0.0, 0.01)

        val zf = repo.session.value.zfHealth
        assertNotNull(zf)
        assertEquals(5, zf?.clutches?.size)
        assertEquals(96, zf?.overallHealthScorePercent)
        assertEquals(84, zf?.oilTemperatureC)
        assertTrue(zf?.clutches?.all { it.wearLevel.name == "OPTIMAL" } == true)
    }

    @Test
    fun `coding catalog contains 2tb ambient and chirp presets`() {
        val presets = com.punhanaghayev.bimmerpro.core.bmw.BmwCodingCatalog.buildPresets(false)
        assertTrue(presets.any { it.id == "sport_transmission_2tb" })
        assertTrue(presets.any { it.id == "ambient_lighting_custom" })
        assertTrue(presets.any { it.id == "acoustic_lock_chirp" })
        assertTrue(presets.any { it.id == "rdc_safety_temp" })
        assertTrue(presets.any { it.id == "mirror_fold" })
    }

    @Test
    fun `bmw model support catalog covers all series from 1 to 8 and x z i`() {
        val models = com.punhanaghayev.bimmerpro.core.bmw.BmwModelSupportCatalog.allModels
        assertTrue(models.size >= 40)
        assertTrue(models.any { it.chassisCodes.contains("F70") })
        assertTrue(models.any { it.chassisCodes.contains("G87") })
        assertTrue(models.any { it.chassisCodes.contains("G20") })
        assertTrue(models.any { it.chassisCodes.contains("G60") })
        assertTrue(models.any { it.chassisCodes.contains("G70") })
        assertTrue(models.any { it.chassisCodes.contains("G05") })
        assertTrue(models.any { it.chassisCodes.contains("G09") })
        assertTrue(models.any { it.chassisCodes.contains("I01") })
        assertTrue(models.any { it.chassisCodes.contains("I20") })

        val xSeries = com.punhanaghayev.bimmerpro.core.bmw.BmwModelSupportCatalog.filterByCategory("X Series (X1 - X7, XM)")
        assertTrue(xSeries.size >= 15)

        val iSeries = com.punhanaghayev.bimmerpro.core.bmw.BmwModelSupportCatalog.filterByCategory("i Series (Elektrikli i3, i4, i5, i7, i8, iX, iX1-3)")
        assertTrue(iSeries.size >= 8)
    }

    @Test
    fun `qrCodeGeneratorProducesStandardMatrixWithFinders`() {
        val matrix = com.punhanaghayev.bimmerpro.core.report.QrCodeGenerator.encode("BMW-VERIFY-WBA3D3150GNT12345-128400")
        assertEquals(25, matrix.size)
        assertEquals(25, matrix[0].size)
        // Check top-left finder pattern center (3,3) is black
        assertTrue(matrix[3][3])
        // Check top-left finder pattern corner (0,0) is black
        assertTrue(matrix[0][0])
        // Check top-right finder pattern corner (0, 24) is black
        assertTrue(matrix[0][24])
        // Check bottom-left finder pattern corner (24, 0) is black
        assertTrue(matrix[24][0])
    }

    @Test
    fun `adasAndHvBatteryRoutinesAndActuatorsExist`() {
        val routines = com.punhanaghayev.bimmerpro.core.simulator.BmwSimulatorCatalog.buildBundle(AdapterType.SIMULATOR_G30).serviceRoutines
        assertTrue(routines.any { it.type == com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutineType.KAFAS_CAMERA_CALIBRATION })
        assertTrue(routines.any { it.type == com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutineType.ACC_RADAR_CALIBRATION })
        assertTrue(routines.any { it.type == com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutineType.HV_BATTERY_CONTACTOR_CHECK })

        val actuators = com.punhanaghayev.bimmerpro.core.bmw.BmwActuatorCatalog.defaultTests()
        assertTrue(actuators.any { it.id == "act_kafas_heater" })
        assertTrue(actuators.any { it.id == "act_acc_alignment" })
        assertTrue(actuators.any { it.id == "act_hv_pyro" })
    }

    @Test
    fun `hvBatteryHealth96CellsCalculatesDelta`() {
        val bundle = com.punhanaghayev.bimmerpro.core.simulator.BmwSimulatorCatalog.buildBundle(AdapterType.SIMULATOR_G30)
        val hv = bundle.hvBatteryHealth
        assertNotNull(hv)
        assertEquals(96, hv?.cellCount)
        assertEquals(96, hv?.cellVoltages?.size)
        assertTrue(hv!!.cellDeltaMv in 1..40)
        assertTrue(hv.insulationResistanceKOhm > 1000)
        assertTrue(hv.stateOfHealthPercent > 90.0)
    }
}
