package com.arktools.daming.ads

import android.content.Context

class AutomationQuotaStore(context: Context) {
    private val preferences = context.getSharedPreferences("automation_daily_quota", Context.MODE_PRIVATE)

    fun remaining(action: String, now: Long = System.currentTimeMillis()): Int {
        refreshDay(now)
        return (BASE_USES + preferences.getInt("${action}_bonus", 0) - preferences.getInt("${action}_used", 0))
            .coerceAtLeast(0)
    }

    fun consume(action: String, now: Long = System.currentTimeMillis()): Boolean {
        if (remaining(action, now) <= 0) return false
        val usedKey = "${action}_used"
        preferences.edit().putInt(usedKey, preferences.getInt(usedKey, 0) + 1).apply()
        return true
    }

    fun grantFive(action: String, now: Long = System.currentTimeMillis()) {
        refreshDay(now)
        val bonusKey = "${action}_bonus"
        val batchKey = "${action}_batches"
        preferences.edit()
            .putInt(bonusKey, preferences.getInt(bonusKey, 0) + BONUS_USES)
            .putInt(batchKey, preferences.getInt(batchKey, 0) + 1)
            .apply()
    }

    fun dayToken(now: Long = System.currentTimeMillis()): Long {
        refreshDay(now)
        return preferences.getLong(KEY_DAY, 0L)
    }

    fun nextBatch(action: String, now: Long = System.currentTimeMillis()): Int {
        refreshDay(now)
        return preferences.getInt("${action}_batches", 0) + 1
    }

    private fun refreshDay(now: Long) {
        val currentDay = (now.coerceAtLeast(0L) / DAY_MS).coerceAtLeast(0L)
        val savedDay = preferences.getLong(KEY_DAY, currentDay)
        val effectiveDay = maxOf(savedDay, currentDay)
        if (effectiveDay != savedDay || !preferences.contains(KEY_DAY)) {
            preferences.edit()
                .clear()
                .putLong(KEY_DAY, effectiveDay)
                .apply()
        }
    }

    private companion object {
        const val KEY_DAY = "last_seen_epoch_day"
        const val DAY_MS = 86_400_000L
        const val BASE_USES = 5
        const val BONUS_USES = 5
    }
}
