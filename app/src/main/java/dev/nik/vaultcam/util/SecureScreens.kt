package dev.nik.vaultcam.util

import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity

object SecureScreens {
    private val secureRoutes = setOf(
        "vault",
        "viewer/{id}"
    )

    fun isSecureRoute(route: String?): Boolean = route != null && route in secureRoutes

    fun applySecureWindow(activity: ComponentActivity, secure: Boolean) {
        val window: Window = activity.window
        if (secure) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.setRecentsScreenshotEnabled(!secure)
        }
    }
}

