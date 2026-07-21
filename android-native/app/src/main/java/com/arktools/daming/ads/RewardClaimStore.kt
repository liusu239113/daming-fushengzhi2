package com.arktools.daming.ads

import android.content.Context

class RewardClaimStore(context: Context) {
    private val preferences = context.getSharedPreferences("rewarded_claims", Context.MODE_PRIVATE)

    fun hasClaimed(key: String): Boolean = preferences.getBoolean(key, false)

    fun markClaimed(key: String) {
        preferences.edit().putBoolean(key, true).apply()
    }
}
