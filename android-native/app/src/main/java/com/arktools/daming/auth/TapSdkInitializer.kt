package com.arktools.daming.auth

import android.content.Context
import android.util.Log
import com.arktools.daming.BuildConfig
import com.taptap.sdk.compliance.option.TapTapComplianceOptions
import com.taptap.sdk.core.TapTapRegion
import com.taptap.sdk.core.TapTapSdk
import com.taptap.sdk.core.TapTapSdkOptions

object TapSdkInitializer {
    @Volatile
    private var initialized = false

    @Synchronized
    fun initialize(context: Context): Result<Unit> {
        if (initialized) return Result.success(Unit)
        if (BuildConfig.TAPTAP_CLIENT_ID.isBlank() || BuildConfig.TAPTAP_CLIENT_TOKEN.isBlank()) {
            return Result.failure(IllegalStateException("TapTap 登录配置不完整"))
        }
        return runCatching {
            val options = TapTapSdkOptions(
                clientId = BuildConfig.TAPTAP_CLIENT_ID,
                clientToken = BuildConfig.TAPTAP_CLIENT_TOKEN,
                region = TapTapRegion.CN,
                enableLog = BuildConfig.DEBUG
            )
            TapTapSdk.init(
                context.applicationContext,
                options,
                options = arrayOf(
                    TapTapComplianceOptions(
                        showSwitchAccount = true,
                        useAgeRange = false
                    )
                )
            )
            initialized = true
            Log.i("TapSdkInitializer", "TapTap SDK initialized")
        }
    }

    fun isInitialized(): Boolean = initialized
}
