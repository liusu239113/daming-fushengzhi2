package com.arktools.daming.ads

import android.app.Application
import com.tosin.sdk.initsdk.init.CustomController
import com.tosin.sdk.initsdk.init.InitListener
import com.tosin.sdk.initsdk.init.TosinInitConfig
import com.tosin.sdk.initsdk.init.TosinSDK

object AdSdkInitializer {
    @Volatile
    private var initialized = false

    @Volatile
    private var initializing = false

    @Synchronized
    fun initialize(application: Application, listener: InitListener? = null) {
        if (initialized) {
            listener?.onInitSuccess()
            return
        }
        if (initializing) {
            listener?.onInitFail("广告 SDK 正在初始化")
            return
        }
        require(AdConfig.appId != 0L) { "广告应用 ID 未配置" }

        initializing = true
        val config = TosinInitConfig.Builder()
            .appId(AdConfig.appId)
            .isDebug(AdConfig.debug)
            .customController(object : CustomController() {
                override fun canUsePhoneState(): Boolean = false
                override fun canUseMacAddress(): Boolean = false
                override fun canReadLocation(): Boolean = false
                override fun canGetInstallPackages(): Boolean = false
                override fun canUsePermissionRecordAudio(): Boolean = false
                override fun canUseOaid(): Boolean = true
                override fun canUseAndroidId(): Boolean = true
                override fun canUseWifiState(): Boolean = true
            })
            .build()

        TosinSDK.instance.init(application, config, object : InitListener {
            override fun onInitFail(fail: String?) {
                initializing = false
                initialized = false
                listener?.onInitFail(fail ?: "广告 SDK 初始化失败")
            }

            override fun onInitSuccess() {
                initializing = false
                initialized = true
                listener?.onInitSuccess()
            }
        })
    }

    fun isInitialized(): Boolean = initialized
}
