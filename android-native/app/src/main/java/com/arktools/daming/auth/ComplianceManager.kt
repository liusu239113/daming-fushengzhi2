package com.arktools.daming.auth

import android.app.Activity
import com.taptap.sdk.compliance.TapTapCompliance
import com.taptap.sdk.compliance.TapTapComplianceCallback
import com.taptap.sdk.compliance.constants.ComplianceMessage

object ComplianceManager {
    sealed interface Result {
        data object Allowed : Result
        data object Exited : Result
        data object SwitchAccount : Result
        data class Blocked(val message: String) : Result
    }

    fun register(onResult: (Result) -> Unit) {
        TapTapCompliance.registerComplianceCallback(
            callback = object : TapTapComplianceCallback {
                override fun onComplianceResult(code: Int, extra: Map<String, Any>?) {
                    val result = when (code) {
                        ComplianceMessage.LOGIN_SUCCESS -> Result.Allowed
                        ComplianceMessage.EXITED -> Result.Exited
                        ComplianceMessage.SWITCH_ACCOUNT -> Result.SwitchAccount
                        ComplianceMessage.PERIOD_RESTRICT -> Result.Blocked("当前时段暂不可进入游戏")
                        ComplianceMessage.DURATION_LIMIT -> Result.Blocked("今日可玩时长已用完")
                        ComplianceMessage.INVALID_CLIENT_OR_NETWORK_ERROR -> Result.Blocked("防沉迷认证失败，请检查网络")
                        ComplianceMessage.REAL_NAME_STOP -> Result.Blocked("需要完成实名认证才能进入游戏")
                        1100 -> Result.Blocked("当前年龄段暂不可进入游戏")
                        else -> Result.Blocked("防沉迷认证返回未知状态：$code")
                    }
                    onResult(result)
                }
            }
        )
    }

    fun start(activity: Activity, userId: String) {
        TapTapCompliance.startup(activity, userId)
    }

    fun exit() {
        TapTapCompliance.exit()
    }
}
