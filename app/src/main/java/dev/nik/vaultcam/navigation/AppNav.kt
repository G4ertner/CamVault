package dev.nik.vaultcam.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.nik.vaultcam.auth.LocalBiometricLauncher
import dev.nik.vaultcam.auth.VaultSession
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
    val biometricLauncher = LocalBiometricLauncher.current

    val navigateToVault = remember(navController, biometricLauncher) {
        {
            val navigate: () -> Unit = {
                navController.navigate(VaultCamDestination.Vault.route) {
                    popUpTo(VaultCamDestination.Vault.route) { inclusive = true }
                    launchSingleTop = true
                }
            }

            if (VaultSession.isUnlocked()) {
                navigate()
            } else {
                biometricLauncher.launch { success ->
                    if (success) {
                        VaultSession.unlock()
                        navigate()
                    } else {
                        VaultSession.clear()
                        // cancel: pop back to Start (or previous)
                        navController.popBackStack()
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = VaultCamDestination.Start.route
    ) {
        composable(VaultCamDestination.Start.route) {
            StartScreen(
                onSecureCameraClick = {
                    navController.navigate(VaultCamDestination.Camera.route) {
                        launchSingleTop = true
                    }
                },
                onPhotoVaultClick = navigateToVault
            )
        }
        composable(VaultCamDestination.Camera.route) {
            CameraScreen()
        }
        composable(VaultCamDestination.Vault.route) {
            if (VaultSession.isUnlocked()) {
                VaultScreen(onItemClick = { id -> navController.navigate(VaultCamDestination.Viewer.routeFor(id)) })
            } else {
                // Show nothing while waiting; can optionally display a placeholder
                LaunchedEffect(Unit) {
                    VaultSession.clear()
                    navController.popBackStack(VaultCamDestination.Start.route, false)
                }
            }
        }
        composable(
            route = VaultCamDestination.Viewer.route,
            arguments = listOf(
                navArgument(VaultCamDestination.Viewer.ARG_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(VaultCamDestination.Viewer.ARG_ID).orEmpty()
            if (VaultSession.isUnlocked()) {
                ViewerScreen(id = id)
            } else {
                LaunchedEffect(Unit) {
                    VaultSession.clear()
                    navController.popBackStack(VaultCamDestination.Start.route, false)
                }
            }
        }
    }
}
