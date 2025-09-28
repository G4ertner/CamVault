package dev.nik.vaultcam.data

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.crypto.tink.Aead
import dev.nik.vaultcam.crypto.KeyStoreWrapper
import dev.nik.vaultcam.crypto.TinkModule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VaultRepositoryDeleteTest {

    private lateinit var context: Context
    private lateinit var aead: Aead

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Application>()
        VaultRepository.clearVault(context)
        TinkModule.deleteKeyset(context)
        TinkModule.clearCachedAead()
        KeyStoreWrapper.clearTestState()
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
    fun saveAndDeleteRemovesFile() {
        val data = "test".toByteArray()
        val id = VaultRepository.saveEncrypted(context, data, aead)
        assertEquals(1, VaultRepository.listItems(context).size)
        assertTrue(VaultRepository.delete(context, id))
        assertEquals(0, VaultRepository.listItems(context).size)
    }
}
