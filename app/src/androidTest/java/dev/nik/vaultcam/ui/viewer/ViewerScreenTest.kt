package dev.nik.vaultcam.ui.viewer

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import dev.nik.vaultcam.MainActivity
import dev.nik.vaultcam.auth.BiometricLauncher
import dev.nik.vaultcam.auth.VaultSession
import dev.nik.vaultcam.crypto.KeyStoreWrapper
import dev.nik.vaultcam.crypto.TinkModule
import dev.nik.vaultcam.data.VaultRepository
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewerScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val cameraPermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    private lateinit var savedId: String

    @Before
    fun setUp() {
        val context = composeRule.activity
        MainActivity.setBiometricLauncherOverride(BiometricLauncher { callback -> callback(true) })
        VaultSession.unlock()
        VaultRepository.clearVault(context)
        TinkModule.deleteKeyset(context)
        TinkModule.clearCachedAead()
        KeyStoreWrapper.clearTestState()
        val aead = TinkModule.getAead(context)
        savedId = VaultRepository.saveEncrypted(context, "viewer-test".toByteArray(), aead)
    }

    @After
    fun tearDown() {
        val context = composeRule.activity
        VaultRepository.clearVault(context)
        TinkModule.deleteKeyset(context)
        TinkModule.clearCachedAead()
        KeyStoreWrapper.clearTestState()
        VaultSession.clear()
        MainActivity.setBiometricLauncherOverride(null)
    }

    @Test
    fun viewerDisplaysImageAndDeletes() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("btn_vault").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("vault_item_$savedId").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("vault_item_$savedId").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("viewer_image").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("viewer_image").assertIsDisplayed()
        composeRule.onNodeWithTag("btn_delete").assertIsDisplayed().performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("vault_empty").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("vault_empty").assertIsDisplayed()
    }
}
