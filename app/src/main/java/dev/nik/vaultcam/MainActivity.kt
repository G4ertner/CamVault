package dev.nik.vaultcam

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.nik.vaultcam.auth.BiometricLauncher
import dev.nik.vaultcam.auth.LocalBiometricLauncher
import dev.nik.vaultcam.auth.RealBiometricLauncher
import dev.nik.vaultcam.navigation.AppNav
import dev.nik.vaultcam.ui.theme.VaultCamTheme
import dev.nik.vaultcam.util.SecureScreens

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VaultCamApp()
        }
    }
}

@Composable
fun VaultCamApp(biometricLauncher: BiometricLauncher? = null) {
    VaultCamTheme {
        val activity = LocalContext.current as? FragmentActivity
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        val realLauncher = remember(activity) {
            activity?.let { RealBiometricLauncher(it) }
        }
        val launcher = biometricLauncher ?: realLauncher ?: LocalBiometricLauncher.current

        LaunchedEffect(activity, currentRoute) {
            val secure = SecureScreens.isSecureRoute(currentRoute)
            activity?.let { SecureScreens.applySecureWindow(it, secure) }
        }

        CompositionLocalProvider(LocalBiometricLauncher provides launcher) {
            AppNav(navController = navController)
        }
    }
}
