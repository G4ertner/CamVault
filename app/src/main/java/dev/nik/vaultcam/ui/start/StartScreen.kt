package dev.nik.vaultcam.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.nik.vaultcam.ui.theme.VaultCamTheme

@Composable
fun StartScreen(
    modifier: Modifier = Modifier,
    onSecureCameraClick: () -> Unit,
    onPhotoVaultClick: () -> Unit
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "VaultCam",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(onClick = onSecureCameraClick) {
                Text(text = "Secure Camera")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onPhotoVaultClick) {
                Text(text = "Photo Vault")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StartScreenPreview() {
    VaultCamTheme {
        StartScreen(
            onSecureCameraClick = {},
            onPhotoVaultClick = {}
        )
    }
}
