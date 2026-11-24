package com.punhanaghayev.bimmerpro.core.bmw

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SprintCFeaturesTest {

    @Test
    fun `csv datalog parser extracts timestamps, channels and min-max bounds accurately`() {
        val sampleCsv = """
            # BMW Expert PRO Telemetry Log
            Timestamp,Engine Speed (rpm),Boost Pressure (bar),Coolant Temp (C)
            12:00:01.000,820,0.12,88.5
            12:00:01.500,2450,0.85,89.0
            12:00:02.000,4800,1.42,90.2
            12:00:02.500,3200,0.60,90.5
        """.trimIndent()

        val session = CsvDatalogParser.parse(sampleCsv)
        assertEquals(4, session.frameCount)
        assertEquals(3, session.channels.size)

        val rpm = session.channels[0]
        assertEquals("Engine Speed (rpm)", rpm.name)
        assertEquals(820.0, rpm.minValue, 0.01)
        assertEquals(4800.0, rpm.maxValue, 0.01)

        val boostPeak = session.valueAt(1, 2)
        assertNotNull(boostPeak)
        assertEquals(1.42, boostPeak!!, 0.001)
    }

    @Test
    fun `csv datalog parser handles semicolon delimited European CSV files`() {
        val euCsv = "Time;RPM;Boost\n00:01;1500;0,95\n00:02;3000;1,35"
        val session = CsvDatalogParser.parse(euCsv)
        assertEquals(2, session.frameCount)
        assertEquals(1.35, session.valueAt(1, 1)!!, 0.001)
    }
}
