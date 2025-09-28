package dev.nik.vaultcam.auth

import android.Manifest
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import dev.nik.vaultcam.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BiometricGateTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val cameraPermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @Before
    fun setUp() {
        VaultSession.clear()
        MainActivity.setBiometricLauncherOverride(FakeBiometricLauncher(result = false))
    }

    @After
    fun tearDown() {
        VaultSession.clear()
        MainActivity.setBiometricLauncherOverride(null)
    }

    @Test
    fun vault_unlocks_on_success() {
        val fakeLauncher = FakeBiometricLauncher(result = true)
        MainActivity.setBiometricLauncherOverride(fakeLauncher)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("btn_vault").performClick()
        composeTestRule.onNodeWithTag("screen_vault").assertIsDisplayed()
        composeTestRule.runOnIdle { assert(fakeLauncher.called) }
    }

    @Test
    fun vault_stays_locked_on_cancel() {
        val fakeLauncher = FakeBiometricLauncher(result = false)
        MainActivity.setBiometricLauncherOverride(fakeLauncher)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("btn_vault").performClick()
        composeTestRule.onNodeWithTag("btn_camera").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_vault").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("screen_vault").assertCountEquals(0)
        composeTestRule.runOnIdle { assert(fakeLauncher.called) }
    }

    @Test
    fun camera_does_not_trigger_biometric() {
        val fakeLauncher = FakeBiometricLauncher(result = false)
        MainActivity.setBiometricLauncherOverride(fakeLauncher)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("btn_camera").performClick()
        composeTestRule.onAllNodesWithTag("screen_camera_preview").assertCountEquals(1)
        composeTestRule.runOnIdle { assert(!fakeLauncher.called) }
    }

    private class FakeBiometricLauncher(private val result: Boolean) : BiometricLauncher {
        var called: Boolean = false
            private set

        override fun launch(onResult: (Boolean) -> Unit) {
            called = true
            onResult(result)
        }
    }
}
