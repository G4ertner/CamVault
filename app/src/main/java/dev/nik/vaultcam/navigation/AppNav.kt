package dev.nik.vaultcam.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.nik.vaultcam.ui.camera.CameraScreen
import dev.nik.vaultcam.ui.start.StartScreen
import dev.nik.vaultcam.ui.vault.VaultScreen
import dev.nik.vaultcam.ui.viewer.ViewerScreen

sealed class VaultCamDestination(val route: String) {
    data object Start : VaultCamDestination("start")
    data object Camera : VaultCamDestination("camera")
    data object Vault : VaultCamDestination("vault")
    data object Viewer : VaultCamDestination("viewer/{id}") {
        const val ARG_ID: String = "id"
        fun routeFor(id: String): String = "viewer/$id"
    }
}

@Composable
fun AppNav(navController: NavHostController) {
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
            CameraScreen()
        }
        composable(VaultCamDestination.Vault.route) {
            VaultScreen()
        }
        composable(
            route = VaultCamDestination.Viewer.route,
            arguments = listOf(
                navArgument(VaultCamDestination.Viewer.ARG_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(VaultCamDestination.Viewer.ARG_ID).orEmpty()
            ViewerScreen(id = id)
        }
    }
}
