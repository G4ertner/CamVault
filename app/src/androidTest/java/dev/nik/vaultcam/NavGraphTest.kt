package dev.nik.vaultcam

import android.Manifest
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
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

    @get:Rule
    val cameraPermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @Before
    fun setUp() {
        VaultSession.clear()
        MainActivity.setBiometricLauncherOverride(BiometricLauncher { onResult -> onResult(true) })
    }

    @After
    fun tearDown() {
        VaultSession.clear()
        MainActivity.setBiometricLauncherOverride(null)
    }

    @Test
    fun start_screen_navigates_to_camera_and_vault() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_camera").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_vault").assertIsDisplayed()

        composeTestRule.onNodeWithTag("btn_camera").performClick()
        composeTestRule.onAllNodesWithTag("screen_camera_preview").assertCountEquals(1)

        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_camera").assertIsDisplayed()

        composeTestRule.onNodeWithTag("btn_vault").performClick()
        composeTestRule.onNodeWithTag("screen_vault").assertIsDisplayed()
    }
}
