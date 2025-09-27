package dev.nik.vaultcam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.nik.vaultcam.navigation.AppNav
import dev.nik.vaultcam.ui.theme.VaultCamTheme
import dev.nik.vaultcam.util.SecureScreens

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
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val activity = LocalContext.current as? ComponentActivity

        LaunchedEffect(activity, currentRoute) {
            val secure = SecureScreens.isSecureRoute(currentRoute)
            activity?.let { SecureScreens.applySecureWindow(it, secure) }
        }

        AppNav(navController = navController)
    }
}

