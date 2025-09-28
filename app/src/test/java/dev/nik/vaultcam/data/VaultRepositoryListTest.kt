package dev.nik.vaultcam.data

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class VaultRepositoryListTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Application>()
        VaultRepository.clearVault(context)
        val dir = VaultRepository.getVaultDir(context)
        dir.mkdirs()
        val first = File(dir, "a.enc")
        first.writeBytes(byteArrayOf(1))
        first.setLastModified(System.currentTimeMillis() - 1_000)
        Thread.sleep(5)
        val second = File(dir, "b.enc")
        second.writeBytes(byteArrayOf(2))
    }

    @After
    fun tearDown() {
        VaultRepository.clearVault(context)
    }

    @Test
    fun listItemsReturnsSortedByCreatedAtDescending() {
        val items = VaultRepository.listItems(context)
        assertEquals(2, items.size)
        assertEquals("b", items[0].id)
        assertEquals("a", items[1].id)
    }
}
