package dev.nik.vaultcam.ui.viewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun ViewerScreen(id: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_viewer"),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Viewer Screen: $id")
    }
}
