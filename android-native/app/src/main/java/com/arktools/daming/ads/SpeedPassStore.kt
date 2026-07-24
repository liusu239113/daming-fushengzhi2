package com.arktools.daming.ads

import android.content.Context

class SpeedPassStore(context: Context) {
    private val preferences = context.getSharedPreferences("rewarded_speed_pass", Context.MODE_PRIVATE)

    fun unlockForTwentyMinutes(now: Long = System.currentTimeMillis()): Long {
        val expiresAt = now + DURATION_MS
        preferences.edit().putLong(KEY_EXPIRES_AT, expiresAt).apply()
        return expiresAt
    }

    fun expiresAt(): Long = preferences.getLong(KEY_EXPIRES_AT, 0L)

    fun remainingMillis(now: Long = System.currentTimeMillis()): Long =
        (expiresAt() - now).coerceAtLeast(0L)

    fun isActive(now: Long = System.currentTimeMillis()): Boolean = remainingMillis(now) > 0L

    private companion object {
        const val KEY_EXPIRES_AT = "expires_at"
        const val DURATION_MS = 20L * 60L * 1000L
    }
}
