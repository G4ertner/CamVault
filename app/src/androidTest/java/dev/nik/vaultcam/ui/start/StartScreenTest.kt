package dev.nik.vaultcam.ui.start

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.nik.vaultcam.ui.theme.VaultCamTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun startScreen_showsPrimaryActions() {
        composeTestRule.setContent {
            VaultCamTheme {
                StartScreen(
                    onSecureCameraClick = {},
                    onPhotoVaultClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Secure Camera").assertIsDisplayed()
        composeTestRule.onNodeWithText("Photo Vault").assertIsDisplayed()
    }
}
