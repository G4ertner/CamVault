package dev.nik.vaultcam

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.nik.vaultcam.auth.BiometricLauncher
import dev.nik.vaultcam.auth.VaultSession
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavGraphTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        VaultSession.clear()
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.setContent {
                VaultCamApp(biometricLauncher = BiometricLauncher { onResult -> onResult(true) })
            }
        }
    }

    @After
    fun tearDown() {
        VaultSession.clear()
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.setContent { VaultCamApp() }
        }
    }

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
