package dev.nik.vaultcam.ui.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun CameraScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_camera"),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Camera Screen")
    }
}
