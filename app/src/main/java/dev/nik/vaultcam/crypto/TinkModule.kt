package dev.nik.vaultcam.crypto

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.google.crypto.tink.Aead
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.JsonKeysetReader
import com.google.crypto.tink.JsonKeysetWriter
import java.io.File

object TinkModule {
    private const val KEYSET_DIR = "keys"
    private const val KEYSET_FILE_NAME = "keyset.json"
    private const val MASTER_KEY_URI_PREFIX = "android-keystore://"

    @Volatile
    private var aead: Aead? = null

    fun getAead(context: Context): Aead {
        aead?.let { return it }
        return synchronized(this) {
            aead?.let { return it }
            val created = createAead(context.applicationContext)
            aead = created
            created
        }
    }

    private fun createAead(context: Context): Aead {
        runCatching { AeadConfig.register() }
        val keysDir = File(context.filesDir, KEYSET_DIR)
        if (!keysDir.exists()) {
            keysDir.mkdirs()
        }
        val keysetFile = File(keysDir, KEYSET_FILE_NAME)

        val keysetHandle: KeysetHandle = if (KeyStoreWrapper.isAndroidKeyStoreAvailable()) {
            KeyStoreWrapper.getOrCreateSecretKey()
            AndroidKeysetManager.Builder()
                .withSharedPref(context, KEYSET_FILE_NAME, "vaultcam_tink_prefs")
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri(masterKeyUri())
                .build()
                .keysetHandle
        } else {
            loadKeysetWithoutKeystore(keysetFile)
        }
        return keysetHandle.getPrimitive(Aead::class.java)
    }

    private fun loadKeysetWithoutKeystore(keysetFile: File): KeysetHandle {
        return if (keysetFile.exists()) {
            CleartextKeysetHandle.read(JsonKeysetReader.withFile(keysetFile))
        } else {
            val handle = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
            CleartextKeysetHandle.write(handle, JsonKeysetWriter.withFile(keysetFile))
            handle
        }
    }

    private fun masterKeyUri(): String = MASTER_KEY_URI_PREFIX + KeyStoreWrapper.MASTER_KEY_ALIAS

    @VisibleForTesting
    internal fun clearCachedAead() {
        aead = null
    }

    @VisibleForTesting
    internal fun deleteKeyset(context: Context) {
        val keysetFile = File(File(context.filesDir, KEYSET_DIR), KEYSET_FILE_NAME)
        if (keysetFile.exists()) {
            keysetFile.delete()
        }
        context.getSharedPreferences("vaultcam_tink_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove(KEYSET_FILE_NAME)
            .apply()
    }
}
