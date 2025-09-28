package dev.nik.vaultcam

import android.app.Application
import android.util.Log
import com.google.crypto.tink.aead.AeadConfig
import java.security.GeneralSecurityException

class VaultCamApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            AeadConfig.register()
        } catch (se: GeneralSecurityException) {
            Log.e(TAG, "Failed to register Tink AEAD config", se)
        }
    }

    companion object {
        private const val TAG = "VaultCamApp"
    }
}
