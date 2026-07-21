package com.arktools.daming.ads

import android.app.Activity
import com.tosin.sdk.loadAd.model.AdError
import com.tosin.sdk.loadAd.rewardvideo.RewardVideoAd
import com.tosin.sdk.loadAd.rewardvideo.RewardVideoListener
import com.tosin.sdk.loadAd.rewardvideo.config.RewardVideoConfig
import com.tosin.sdk.initsdk.init.InitListener

object RewardedAdManager {
    private var rewardedAd: RewardVideoAd? = null
    private var listener: RewardVideoListener? = null

    interface Callback {
        fun onLoaded()
        fun onRewarded()
        fun onClosed()
        fun onFailed(message: String)
    }

    fun load(activity: Activity, callback: Callback) {
        if (activity.isFinishing || activity.isDestroyed) {
            callback.onFailed("当前页面不可用")
            return
        }
        AdSdkInitializer.initialize(activity.application, object : InitListener {
            override fun onInitFail(fail: String?) {
                callback.onFailed(fail ?: "广告 SDK 初始化失败")
            }

            override fun onInitSuccess() {
                createAndLoad(activity, callback)
            }
        })
    }

    private fun createAndLoad(activity: Activity, callback: Callback) {
        destroy()
        val placementId = AdConfig.rewardedPlacementId
        if (placementId.isBlank()) {
            callback.onFailed("激励广告位 ID 未配置")
            return
        }

        val config = RewardVideoConfig.Builder()
            .codeId(placementId)
            .build()
        rewardedAd = RewardVideoAd(activity, config)
        val rewardListener = object : RewardVideoListener {
            override fun onRewardVerify() = callback.onRewarded()
            override fun onVideoComplete() = Unit
            override fun onAdClose() = callback.onClosed()
            override fun onExposure() = Unit
            override fun onADShowError(fail: String) = callback.onFailed(fail.ifBlank { "广告展示失败" })
            override fun onADShow() = Unit
            override fun onLoadSuccess() = callback.onLoaded()
            override fun onLoadFail(adError: AdError?) = callback.onFailed(adError?.errorMsg ?: "广告加载失败")
            override fun onADClick() = Unit
        }
        listener = rewardListener
        rewardedAd?.loadRewardVideo(rewardListener)
    }

    fun show() {
        rewardedAd?.showAd()
    }

    fun destroy() {
        rewardedAd?.destory()
        rewardedAd = null
        listener = null
    }
}
