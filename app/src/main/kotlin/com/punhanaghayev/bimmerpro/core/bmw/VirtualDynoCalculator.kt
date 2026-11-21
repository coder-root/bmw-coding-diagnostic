package com.punhanaghayev.bimmerpro.core.bmw

import kotlin.math.pow
import kotlin.math.sin

data class DynoPoint(
    val rpm: Int,
    val horsepower: Double,
    val torqueNm: Double
)

data class VirtualDynoResult(
    val points: List<DynoPoint>,
    val peakHp: Int,
    val peakHpRpm: Int,
    val peakTorqueNm: Int,
    val peakTorqueRpm: Int,
    val vehicleMassKg: Int,
    val summary: String
)

/**
 * Physics-based Virtual Dyno calculator computing wheel/crank power (HP) and torque (Nm)
 * from live OBD/UDS acceleration (dV/dt), RPM, aerodynamic drag, and BMW chassis mass.
 */
object VirtualDynoCalculator {

    fun defaultMassKgFor(chassis: BmwChassis): Int = when (chassis) {
        BmwChassis.E87, BmwChassis.F20, BmwChassis.F22 -> 1480
        BmwChassis.E90, BmwChassis.F30, BmwChassis.F32, BmwChassis.G20 -> 1595
        BmwChassis.E60, BmwChassis.F10, BmwChassis.G30, BmwChassis.G60 -> 1740
        BmwChassis.G11, BmwChassis.G70 -> 1960
        BmwChassis.F25, BmwChassis.F48, BmwChassis.G01, BmwChassis.U06, BmwChassis.U11 -> 1790
        BmwChassis.E70, BmwChassis.F15, BmwChassis.G05 -> 2120
        BmwChassis.I01 -> 1365
        BmwChassis.I12 -> 1560
        BmwChassis.I20 -> 2510
    }

    fun calculateSample(
        speedKmh: Double,
        prevSpeedKmh: Double,
        deltaSeconds: Double,
        rpm: Double,
        massKg: Int
    ): DynoPoint? {
        if (deltaSeconds <= 0.05 || rpm < 1200.0) return null
        val v1 = prevSpeedKmh / 3.6
        val v2 = speedKmh / 3.6
        val vAvg = ((v1 + v2) * 0.5).coerceAtLeast(1.0)
        val accel = ((v2 - v1) / deltaSeconds).coerceAtLeast(0.0)

        // F = m*a + F_aero + F_roll (CdA ~ 0.68 m^2, Crr ~ 0.012, drivetrain loss factor ~ 1.15)
        val fAccel = massKg * accel
        val fAero = 0.5 * 1.225 * 0.68 * vAvg.pow(2)
        val fRoll = massKg * 9.81 * 0.012
        val wheelWatts = (fAccel + fAero + fRoll) * vAvg
        val crankHp = ((wheelWatts * 1.15) / 745.7).coerceIn(10.0, 950.0)
        val torqueNm = ((crankHp * 7127.0) / rpm).coerceIn(20.0, 1200.0)

        return DynoPoint(
            rpm = rpm.toInt(),
            horsepower = (crankHp * 10).toInt() / 10.0,
            torqueNm = (torqueNm * 10).toInt() / 10.0
        )
    }

    /**
     * Generates a calibrated WOT dyno pull curve for simulator validation or aggregates live points.
     */
    fun generateSimulatedPull(chassis: BmwChassis, engineCode: String): VirtualDynoResult {
        val mass = defaultMassKgFor(chassis)
        val targetPeakHp = when {
            engineCode.contains("374") || engineCode.contains("B58") -> 378.0
            engineCode.contains("306") || engineCode.contains("N54") -> 312.0
            engineCode.contains("313") || engineCode.contains("eDrive") -> 315.0
            engineCode.contains("252") || engineCode.contains("B48") -> 255.0
            engineCode.contains("245") || engineCode.contains("N20") -> 248.0
            else -> 188.0 // 520d N47
        }
        val isDiesel = engineCode.contains("N47") || engineCode.contains("B47") || engineCode.contains("520d")
        val maxRpm = if (isDiesel) 4600 else 6600
        val step = if (isDiesel) 250 else 350

        val points = (1500..maxRpm step step).map { rpm ->
            val norm = (rpm - 1500).toDouble() / (maxRpm - 1500).coerceAtLeast(1)
            val torqueShape = (0.68 + 0.34 * sin(norm * Math.PI * 0.85)).coerceIn(0.55, 1.02)
            val hp = (targetPeakHp * (0.35 + 0.65 * norm) * torqueShape).coerceAtMost(targetPeakHp)
            val nm = ((hp * 7127.0) / rpm).coerceIn(180.0, 620.0)
            DynoPoint(rpm, (hp * 10).toInt() / 10.0, (nm * 10).toInt() / 10.0)
        }
        return summarize(points, mass)
    }

    fun summarize(points: List<DynoPoint>, massKg: Int): VirtualDynoResult {
        if (points.isEmpty()) {
            return VirtualDynoResult(emptyList(), 0, 0, 0, 0, massKg, "Dyno verisi yok")
        }
        val maxHpPt = points.maxBy { it.horsepower }
        val maxTqPt = points.maxBy { it.torqueNm }
        return VirtualDynoResult(
            points = points,
            peakHp = maxHpPt.horsepower.toInt(),
            peakHpRpm = maxHpPt.rpm,
            peakTorqueNm = maxTqPt.torqueNm.toInt(),
            peakTorqueRpm = maxTqPt.rpm,
            vehicleMassKg = massKg,
            summary = "Peak: ${maxHpPt.horsepower.toInt()} HP @ ${maxHpPt.rpm} rpm | ${maxTqPt.torqueNm.toInt()} Nm @ ${maxTqPt.rpm} rpm"
        )
    }
}
