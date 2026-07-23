package com.arktools.daming.ads

import android.content.Context
import java.util.Calendar

class RewardClaimStore(context: Context) {
    private val preferences = context.getSharedPreferences("rewarded_claims", Context.MODE_PRIVATE)

    fun hasClaimed(key: String): Boolean = preferences.getBoolean(key, false)

    fun markClaimed(key: String) {
        preferences.edit().putBoolean(key, true).apply()
    }

    fun markClaimedAndRecordDaily(key: String) {
        val countKey = dailyCountKey()
        val nextCount = preferences.getInt(countKey, 0) + 1
        preferences.edit()
            .putBoolean(key, true)
            .putInt(countKey, nextCount)
            .apply()
    }

    fun recordDailyClaim() {
        val countKey = dailyCountKey()
        preferences.edit().putInt(countKey, preferences.getInt(countKey, 0) + 1).apply()
    }

    fun dailyClaimCount(): Int = preferences.getInt(dailyCountKey(), 0)

    fun remainingDailyClaims(limit: Int = DAILY_REWARD_LIMIT): Int =
        (limit - dailyClaimCount()).coerceAtLeast(0)

    fun canClaimToday(limit: Int = DAILY_REWARD_LIMIT): Boolean = remainingDailyClaims(limit) > 0

    fun dailyRewardKey(rewardId: String): String = "daily-${dailyToken()}-$rewardId"

    private fun dailyCountKey(now: Long = System.currentTimeMillis()): String =
        "daily-count-${dailyToken(now)}"

    private fun dailyToken(now: Long = System.currentTimeMillis()): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = now }
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
    }

    companion object {
        const val DAILY_REWARD_LIMIT = 8
    }
}
