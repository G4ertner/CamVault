package dev.nik.vaultcam.auth

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.nik.vaultcam.VaultCamApp
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BiometricGateTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        VaultSession.clear()
    }

    @After
    fun tearDown() {
        VaultSession.clear()
    }

    @Test
    fun vault_unlocks_on_success() {
        val fakeLauncher = FakeBiometricLauncher(result = true)
        composeTestRule.activity.setContent {
            VaultCamApp(biometricLauncher = fakeLauncher)
        }

        composeTestRule.onNodeWithTag("btn_vault").performClick()
        composeTestRule.onNodeWithTag("screen_vault").assertIsDisplayed()
        composeTestRule.runOnIdle { assert(fakeLauncher.called) }
    }

    @Test
    fun vault_stays_locked_on_cancel() {
        val fakeLauncher = FakeBiometricLauncher(result = false)
        composeTestRule.activity.setContent {
            VaultCamApp(biometricLauncher = fakeLauncher)
        }

        composeTestRule.onNodeWithTag("btn_vault").performClick()
        composeTestRule.onNodeWithTag("btn_camera").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_vault").assertIsDisplayed()
        composeTestRule.onNodeWithTag("screen_vault").assertDoesNotExist()
        composeTestRule.runOnIdle { assert(fakeLauncher.called) }
    }

    @Test
    fun camera_does_not_trigger_biometric() {
        val fakeLauncher = FakeBiometricLauncher(result = false)
        composeTestRule.activity.setContent {
            VaultCamApp(biometricLauncher = fakeLauncher)
        }

        composeTestRule.onNodeWithTag("btn_camera").performClick()
        composeTestRule.onNodeWithTag("screen_camera").assertIsDisplayed()
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
