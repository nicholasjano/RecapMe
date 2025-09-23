package com.nicholasjano.recapme

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule
import org.junit.Assert.*

/**
 * Instrumented tests for MainActivity and app functionality.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.nicholasjano.recapme", appContext.packageName)
    }

    @Test
    fun mainActivity_launches_successfully() {
        // Verify the app launches without crashing and shows expected content
        composeTestRule.onNodeWithText("Home").assertExists()
        composeTestRule.onNodeWithText("Settings").assertExists()
    }

    @Test
    fun navigation_betweenScreens_works() {
        // Test navigation between Home and Settings
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Summarization Settings").assertExists()

        // Navigate back to Home
        composeTestRule.onNodeWithText("Home").performClick()
        composeTestRule.onNodeWithText("Home").assertExists()
    }
}