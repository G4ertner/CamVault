package dev.nik.vaultcam.ui.camera

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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val cameraPermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @Test
    fun camera_controls_are_visible_and_clickable() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_camera").performClick()

        composeTestRule.onAllNodesWithTag("screen_camera_preview").assertCountEquals(1)
        composeTestRule.onNodeWithTag("btn_flip_camera").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("btn_shutter").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("btn_flash_toggle").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("btn_flash_toggle").performClick()
    }
}
