package dev.nik.vaultcam.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.VisibleForTesting
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object KeyStoreWrapper {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    internal const val MASTER_KEY_ALIAS = "VaultCamMasterKey"

    @Volatile
    private var inMemorySecretKey: SecretKey? = null

    private val inMemoryLock = Any()

    fun getOrCreateSecretKey(): SecretKey {
        return if (isAndroidKeyStoreAvailable()) {
            createOrLoadFromAndroidKeyStore()
        } else {
            createOrLoadInMemoryKey()
        }
    }

    internal fun isAndroidKeyStoreAvailable(): Boolean {
        return runCatching {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            true
        }.getOrDefault(false)
    }

    private fun createOrLoadFromAndroidKeyStore(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getKey(MASTER_KEY_ALIAS, null) as? SecretKey
        if (existing != null) {
            return existing
        }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun createOrLoadInMemoryKey(): SecretKey {
        inMemorySecretKey?.let { return it }
        synchronized(inMemoryLock) {
            inMemorySecretKey?.let { return it }
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256)
            val key = keyGenerator.generateKey()
            inMemorySecretKey = key
            return key
        }
    }

    @VisibleForTesting
    internal fun clearTestState() {
        synchronized(inMemoryLock) {
            inMemorySecretKey = null
        }
    }
}
