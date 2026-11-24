package com.punhanaghayev.bimmerpro.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SprintDFeaturesTest {

    @Test
    fun `workshop profile formats header line and technician signature cleanly`() {
        val empty = WorkshopProfile()
        assertFalse(empty.isConfigured)
        assertEquals("", empty.headerLine())

        val branded = WorkshopProfile(
            workshopName = "Bavaria Master Garage",
            phoneNumber = "+994 50 555 0101",
            address = "Baku, AZ",
            technicianName = "Punhan Aghayev"
        )
        assertTrue(branded.isConfigured)
        assertEquals("Bavaria Master Garage  •  +994 50 555 0101  •  Baku, AZ", branded.headerLine())
        assertEquals("Bavaria Master Garage  |  Tech: Punhan Aghayev", branded.footerSignature())
    }
}
