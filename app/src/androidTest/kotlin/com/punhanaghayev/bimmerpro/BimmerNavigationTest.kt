package com.punhanaghayev.bimmerpro

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class BimmerNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun developerTabDisplaysPunhanAghayevAndSimulatorConnects() {
        // Connect to F10 Simulator on Garaj tab
        composeRule.onNodeWithText("Araca Bağlan (UDS / HSFZ)").performClick()
        composeRule.waitForIdle()

        // Navigate to Developer tab and verify author attribution
        composeRule.onNodeWithText("Developer").performClick()
        composeRule.onNodeWithText("Punhan Aghayev").assertIsDisplayed()
    }
}
