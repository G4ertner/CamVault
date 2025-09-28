package dev.nik.vaultcam.data

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.crypto.tink.Aead
import dev.nik.vaultcam.crypto.KeyStoreWrapper
import dev.nik.vaultcam.crypto.TinkModule
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VaultRepositoryTest {

    private lateinit var context: Context
    private lateinit var aead: Aead

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Application>()
        TinkModule.deleteKeyset(context)
        TinkModule.clearCachedAead()
        KeyStoreWrapper.clearTestState()
        VaultRepository.clearVault(context)
        aead = TinkModule.getAead(context)
    }

    @After
    fun tearDown() {
        VaultRepository.clearVault(context)
        TinkModule.deleteKeyset(context)
        TinkModule.clearCachedAead()
        KeyStoreWrapper.clearTestState()
    }

    @Test
    fun encryptAndDecryptRoundTrip() {
        val data = "Sample".toByteArray()

        val uuid = VaultRepository.saveEncrypted(context, data, aead)
        val decrypted = VaultRepository.decrypt(context, uuid, aead)

        assertArrayEquals(data, decrypted)

        val files = VaultRepository.getVaultDir(context).listFiles() ?: emptyArray()
        assertEquals(1, files.size)
        assertTrue(files.first().name.endsWith(".enc"))
    }
}
