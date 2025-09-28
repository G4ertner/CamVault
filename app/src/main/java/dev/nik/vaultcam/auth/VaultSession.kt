package dev.nik.vaultcam.auth

import android.os.SystemClock

object VaultSession {
    const val UNLOCK_WINDOW_MS: Long = 300_000L

    private var unlockedUntil: Long = 0L

    fun unlock(now: Long = SystemClock.elapsedRealtime()) {
        unlockedUntil = now + UNLOCK_WINDOW_MS
    }

    fun isUnlocked(now: Long = SystemClock.elapsedRealtime()): Boolean {
        return now <= unlockedUntil
    }

    fun clear() {
        unlockedUntil = 0L
    }
}
