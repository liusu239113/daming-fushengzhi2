package com.arktools.daming

import android.app.Application
import com.arktools.daming.ads.AdConfig

class DamingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdConfig.configure(
            appId = BuildConfig.AD_APP_ID,
            rewardedPlacementId = BuildConfig.REWARDED_AD_PLACEMENT_ID,
            privacyPolicyUrl = BuildConfig.PRIVACY_POLICY_URL,
            debug = BuildConfig.DEBUG
        )
    }
}
