package dev.nik.vaultcam.crypto

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TinkModuleTest {

    private val context: Context = ApplicationProvider.getApplicationContext<Application>()

    @Before
    fun setUp() {
        TinkModule.deleteKeyset(context)
        TinkModule.clearCachedAead()
        KeyStoreWrapper.clearTestState()
    }

    @After
    fun tearDown() {
        TinkModule.deleteKeyset(context)
        TinkModule.clearCachedAead()
        KeyStoreWrapper.clearTestState()
    }

    @Test
    fun tink_roundTripEncryption() {
        val aead = TinkModule.getAead(context)
        val plaintext = "Hello VaultCam".toByteArray()
        val associatedData = byteArrayOf(0x01, 0x02)

        val ciphertext = aead.encrypt(plaintext, associatedData)
        val decrypted = aead.decrypt(ciphertext, associatedData)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun tink_keysetPersistsAcrossInstances() {
        val aeadFirst = TinkModule.getAead(context)
        val ciphertext = aeadFirst.encrypt(byteArrayOf(0x10), null)

        TinkModule.clearCachedAead()

        val aeadSecond = TinkModule.getAead(context)
        val decrypted = aeadSecond.decrypt(ciphertext, null)

        assertArrayEquals(byteArrayOf(0x10), decrypted)
    }
}
