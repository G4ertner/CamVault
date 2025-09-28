package dev.nik.vaultcam.data

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import com.google.crypto.tink.Aead
import java.io.File
import java.util.UUID

object VaultRepository {
    private const val VAULT_DIR = "secure_media"
    private const val TAG = "VaultRepository"

    fun getVaultDir(context: Context): File {
        val root = ContextCompat.getNoBackupFilesDir(context)
        return File(root, VAULT_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    fun listItems(context: Context): List<VaultItem> {
        return getVaultDir(context)
            .listFiles()
            ?.asSequence()
            ?.filter { it.isFile && it.name.endsWith(".enc") }
            ?.map { file ->
                val id = file.name.removeSuffix(".enc")
                VaultItem(id = id, file = file, createdAt = file.lastModified())
            }
            ?.sortedByDescending { it.createdAt }
            ?.toList()
            ?: emptyList()
    }

    fun saveEncrypted(context: Context, imageBytes: ByteArray, aead: Aead): String {
        val uuid = UUID.randomUUID().toString()
        val associatedData = uuid.toByteArray()
        val cipher = aead.encrypt(imageBytes, associatedData)
        val outFile = File(getVaultDir(context), "$uuid.enc")
        outFile.outputStream().use { it.write(cipher) }
        Log.d(TAG, "Saved encrypted media to ${outFile.absolutePath}")
        return uuid
    }

    fun decrypt(context: Context, uuid: String, aead: Aead): ByteArray {
        val file = File(getVaultDir(context), "$uuid.enc")
        val cipher = file.readBytes()
        val associatedData = uuid.toByteArray()
        return aead.decrypt(cipher, associatedData)
    }

    fun delete(context: Context, id: String): Boolean {
        val file = File(getVaultDir(context), "$id.enc")
        return file.delete()
    }

    @VisibleForTesting
    internal fun clearVault(context: Context) {
        val dir = getVaultDir(context)
        dir.listFiles()?.forEach { it.deleteRecursively() }
    }
}
