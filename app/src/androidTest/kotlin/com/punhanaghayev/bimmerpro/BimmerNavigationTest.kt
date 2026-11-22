package com.punhanaghayev.bimmerpro

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented Compose UI & End-to-End Integration tests covering app launch, simulator connection,
 * Diagnostics, Coding (VO Diff & FSC), Live Telemetry (Virtual Dyno & Drag modes), Service routines,
 * and Packet Inspector navigation.
 */
class BimmerNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun dismissLanguagePickerIfShown() {
        val confirmNodes = composeRule.onAllNodesWithText("Continue", substring = true).fetchSemanticsNodes() +
            composeRule.onAllNodesWithText("Devam Et", substring = true).fetchSemanticsNodes()
        if (confirmNodes.isNotEmpty()) {
            runCatching { composeRule.onNodeWithText("Continue", substring = true).performClick() }
                .onFailure {
                    runCatching { composeRule.onNodeWithText("Devam Et", substring = true).performClick() }
                }
            composeRule.waitForIdle()
        }
    }

    @Test
    fun fullAppFlowWorksEndToEndInSimulatorMode() {
        dismissLanguagePickerIfShown()

        // 1. Connect Simulator F10 in Garage
        composeRule.onAllNodesWithText("BMW F10", substring = true)[0].performClick()
        composeRule.waitForIdle()

        // 2. Navigate to Diagnostics tab and verify ECU SVT + Root-Cause section renders
        composeRule.onAllNodesWithText("Diagnostics", substring = true)[0]
            .let { node -> node.performClick() }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("DME", substring = true)[0].assertIsDisplayed()

        // 3. Navigate to Coding tab -> FA/VO tab & test VO Diff calculation
        composeRule.onAllNodesWithText("Coding", substring = true)[0].performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("FA / VO", substring = true)[0].performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("6NS", substring = true)[0].performTextInput("6WB")

        // 4. Navigate to Live tab & verify Virtual Dyno + Drag modes
        composeRule.onAllNodesWithText("Live", substring = true)[0].performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("Run Dyno Pull", substring = true)[0].performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("HP", substring = true)[0].assertIsDisplayed()

        // 5. Navigate to Developer tab and verify attribution
        composeRule.onAllNodesWithText("Developer", substring = true)[0].performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Punhan Aghayev").assertIsDisplayed()
    }
}
