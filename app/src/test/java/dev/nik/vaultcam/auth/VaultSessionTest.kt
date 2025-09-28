package dev.nik.vaultcam.auth

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VaultSessionTest {

    @After
    fun tearDown() {
        VaultSession.clear()
    }

    @Test
    fun unlock_within_window_keeps_session_open() {
        VaultSession.clear()
        VaultSession.unlock(now = 1_000L)

        val stillUnlocked = VaultSession.isUnlocked(now = 1_000L + VaultSession.UNLOCK_WINDOW_MS - 1)
        assertTrue(stillUnlocked)
    }

    @Test
    fun unlock_expires_after_window() {
        VaultSession.clear()
        VaultSession.unlock(now = 0L)

        val unlocked = VaultSession.isUnlocked(now = VaultSession.UNLOCK_WINDOW_MS + 1)
        assertFalse(unlocked)
    }

    @Test
    fun clear_resets_session() {
        VaultSession.unlock(now = 1_000L)
        VaultSession.clear()

        assertFalse(VaultSession.isUnlocked(now = 1_000L))
    }
}
