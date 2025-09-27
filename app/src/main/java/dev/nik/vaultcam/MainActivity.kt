package dev.nik.vaultcam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.nik.vaultcam.ui.start.StartScreen
import dev.nik.vaultcam.ui.theme.VaultCamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VaultCamApp()
        }
    }
}

@Composable
fun VaultCamApp() {
    VaultCamTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = VaultCamDestination.Start.route
        ) {
            composable(VaultCamDestination.Start.route) {
                StartScreen(
                    onSecureCameraClick = {
                        navController.navigate(VaultCamDestination.Camera.route)
                    },
                    onPhotoVaultClick = {
                        navController.navigate(VaultCamDestination.Vault.route)
                    }
                )
            }
            composable(VaultCamDestination.Camera.route) {
                PlaceholderScreen(title = "Camera Screen")
            }
            composable(VaultCamDestination.Vault.route) {
                PlaceholderScreen(title = "Vault Screen")
            }
            composable(
                route = VaultCamDestination.Viewer.route,
                arguments = listOf(
                    navArgument(VaultCamDestination.Viewer.ARG_ID) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString(VaultCamDestination.Viewer.ARG_ID).orEmpty()
                PlaceholderScreen(title = "Viewer Screen: $id")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}

sealed class VaultCamDestination(val route: String) {
    data object Start : VaultCamDestination("start")
    data object Camera : VaultCamDestination("camera")
    data object Vault : VaultCamDestination("vault")
    data object Viewer : VaultCamDestination("viewer/{id}") {
        const val ARG_ID = "id"
        fun routeFor(id: String): String = "viewer/$id"
    }
}

@Preview(showBackground = true)
@Composable
private fun VaultCamAppPreview() {
    VaultCamApp()
}
