package dev.nik.vaultcam

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavGraphTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun start_screen_navigates_to_camera_and_vault() {
        composeTestRule.onNodeWithTag("btn_camera").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_vault").assertIsDisplayed()

        composeTestRule.onNodeWithTag("btn_camera").performClick()
        composeTestRule.onNodeWithTag("screen_camera").assertIsDisplayed()

        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.onNodeWithTag("btn_camera").assertIsDisplayed()

        composeTestRule.onNodeWithTag("btn_vault").performClick()
        composeTestRule.onNodeWithTag("screen_vault").assertIsDisplayed()
    }
}
