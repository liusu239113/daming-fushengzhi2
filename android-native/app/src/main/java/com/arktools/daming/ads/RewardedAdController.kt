package com.arktools.daming.ads

import android.app.Activity
import android.os.Handler
import android.os.Looper

object RewardedAdController {
    private const val LOAD_TIMEOUT_MS = 15_000L
    private const val COOLDOWN_MS = 5_000L
    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    private var loading = false

    @Volatile
    private var lastShownAt = 0L

    fun show(
        activity: Activity,
        onLoadingChanged: (Boolean) -> Unit,
        onRewarded: () -> Unit,
        onError: (String) -> Unit,
        onClosed: () -> Unit
    ) {
        if (loading) {
            onError("广告正在加载中，请稍候")
            return
        }
        val remaining = (COOLDOWN_MS - (System.currentTimeMillis() - lastShownAt)).coerceAtLeast(0L)
        if (lastShownAt > 0L && remaining > 0L) {
            onError("广告冷却中，请 ${((remaining + 999L) / 1000L)} 秒后再试")
            return
        }
        if (activity.isFinishing || activity.isDestroyed) {
            onError("当前页面不可用")
            return
        }

        loading = true
        onLoadingChanged(true)
        var finished = false
        var rewarded = false

        fun finish(error: String? = null) {
            if (finished) return
            finished = true
            loading = false
            onLoadingChanged(false)
            if (error != null) onError(error)
        }

        val timeout = Runnable {
            if (!finished) {
                RewardedAdManager.destroy()
                finish("广告加载超时，请稍后重试")
            }
        }
        handler.postDelayed(timeout, LOAD_TIMEOUT_MS)

        RewardedAdManager.load(activity, object : RewardedAdManager.Callback {
            override fun onLoaded() {
                if (finished || activity.isFinishing || activity.isDestroyed) {
                    finish("页面已关闭")
                    return
                }
                handler.removeCallbacks(timeout)
                lastShownAt = System.currentTimeMillis()
                onLoadingChanged(false)
                RewardedAdManager.show()
            }

            override fun onRewarded() {
                if (!rewarded) {
                    rewarded = true
                    onRewarded()
                }
            }

            override fun onClosed() {
                handler.removeCallbacks(timeout)
                finish()
                RewardedAdManager.destroy()
                onClosed()
            }

            override fun onFailed(message: String) {
                handler.removeCallbacks(timeout)
                RewardedAdManager.destroy()
                finish(message)
            }
        })
    }

    fun destroy() {
        loading = false
        RewardedAdManager.destroy()
    }
}
