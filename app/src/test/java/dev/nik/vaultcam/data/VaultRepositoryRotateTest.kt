package dev.nik.vaultcam.data

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.google.crypto.tink.Aead
import dev.nik.vaultcam.crypto.KeyStoreWrapper
import dev.nik.vaultcam.crypto.TinkModule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
class VaultRepositoryRotateTest {

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
    fun rotateSwapsDimensions() {
        val bitmap = Bitmap.createBitmap(2, 3, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.RED)
        }
        val bytes = ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.toByteArray()
        }
        val id = VaultRepository.saveEncrypted(context, bytes, aead)

        val rotated = VaultRepository.rotate(context, id, aead)
        assertEquals(true, rotated)

        val decrypted = VaultRepository.decrypt(context, id, aead)
        val rotatedBitmap = BitmapFactory.decodeByteArray(decrypted, 0, decrypted.size)!!
        assertEquals(3, rotatedBitmap.width)
        assertEquals(2, rotatedBitmap.height)
    }
}
