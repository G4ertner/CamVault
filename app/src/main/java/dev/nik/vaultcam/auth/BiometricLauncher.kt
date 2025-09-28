package dev.nik.vaultcam.auth

import androidx.biometric.BiometricManager
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

fun interface BiometricLauncher {
    fun launch(onResult: (Boolean) -> Unit)
}

class RealBiometricLauncher(private val activity: FragmentActivity) : BiometricLauncher {
    override fun launch(onResult: (Boolean) -> Unit) {
        if (activity.isFinishing || activity.isDestroyed) {
            onResult(false)
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = androidx.biometric.BiometricPrompt(
            activity,
            executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    onResult(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onResult(false)
                }
            }
        )

        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Vault")
            .setSubtitle("Authenticate to access your private media")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        prompt.authenticate(promptInfo)
    }
}

private object AlwaysAllowBiometricLauncher : BiometricLauncher {
    override fun launch(onResult: (Boolean) -> Unit) {
        onResult(true)
    }
}

val LocalBiometricLauncher = staticCompositionLocalOf<BiometricLauncher> { AlwaysAllowBiometricLauncher }
