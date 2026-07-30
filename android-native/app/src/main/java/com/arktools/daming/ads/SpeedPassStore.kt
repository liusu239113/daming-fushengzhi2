package com.arktools.daming.ads

import android.content.Context

class SpeedPassStore(context: Context) {
    private val preferences = context.getSharedPreferences("rewarded_speed_pass", Context.MODE_PRIVATE)

    fun unlockForTwentyMinutes(now: Long = System.currentTimeMillis()): Long {
        val effectiveNow = effectiveNow(now)
        val expiresAt = effectiveNow + DURATION_MS
        preferences.edit()
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .putLong(KEY_LAST_SEEN_AT, effectiveNow)
            .apply()
        return expiresAt
    }

    fun expiresAt(): Long = preferences.getLong(KEY_EXPIRES_AT, 0L)

    fun remainingMillis(now: Long = System.currentTimeMillis()): Long =
        (expiresAt() - effectiveNow(now)).coerceAtLeast(0L)

    fun isActive(now: Long = System.currentTimeMillis()): Boolean = remainingMillis(now) > 0L

    private fun effectiveNow(now: Long): Long {
        val safeNow = now.coerceAtLeast(0L)
        val lastSeenAt = preferences.getLong(KEY_LAST_SEEN_AT, safeNow)
        val effectiveNow = maxOf(safeNow, lastSeenAt)
        if (!preferences.contains(KEY_LAST_SEEN_AT) || effectiveNow != lastSeenAt) {
            preferences.edit().putLong(KEY_LAST_SEEN_AT, effectiveNow).apply()
        }
        return effectiveNow
    }

    private companion object {
        const val KEY_EXPIRES_AT = "expires_at"
        const val KEY_LAST_SEEN_AT = "last_seen_at"
        const val DURATION_MS = 20L * 60L * 1000L
    }
}
