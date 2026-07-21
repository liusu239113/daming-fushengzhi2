package com.arktools.daming.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taptap.sdk.kit.internal.callback.TapTapCallback
import com.taptap.sdk.kit.internal.exception.TapTapException
import com.taptap.sdk.login.Scopes
import com.taptap.sdk.login.TapTapAccount
import com.taptap.sdk.login.TapTapLogin

@Composable
fun TapTapLoginGate(onLoggedIn: (TapTapAccount) -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    var loggingIn by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        TapTapLogin.getCurrentTapAccount()?.let(onLoggedIn)
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFB98E59)), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth(0.88f).widthIn(max = 520.dp).background(Color(0xFFF4E7C7), RoundedCornerShape(18.dp)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("大明浮生志3", color = Color(0xFFA83224), fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("登录 TapTap 账号后进入宗族沙盘", color = Color(0xFF2B2016), fontSize = 15.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val host = activity ?: return@Button
                    loggingIn = true
                    error = null
                    try {
                        TapTapLogin.loginWithScopes(
                            host,
                            arrayOf(Scopes.SCOPE_PUBLIC_PROFILE),
                            object : TapTapCallback<TapTapAccount> {
                                override fun onSuccess(result: TapTapAccount) {
                                    loggingIn = false
                                    onLoggedIn(result)
                                }

                                override fun onCancel() {
                                    loggingIn = false
                                    error = "登录已取消"
                                }

                                override fun onFail(exception: TapTapException) {
                                    loggingIn = false
                                    error = "登录失败：${exception.message ?: "请稍后重试"}"
                                }
                            }
                        )
                    } catch (_: Exception) {
                        loggingIn = false
                        error = "登录服务不可用，请安装或更新 TapTap 客户端"
                    }
                },
                enabled = !loggingIn && activity != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA83224)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loggingIn) "正在登录…" else "TapTap 登录")
            }
            error?.let { Text(it, color = Color(0xFFB71C1C), fontSize = 13.sp, textAlign = TextAlign.Center) }
        }
    }
}

@Composable
fun ComplianceGate(
    activity: Activity,
    userId: String,
    onAllowed: () -> Unit,
    onReturnToLogin: () -> Unit
) {
    var message by remember { mutableStateOf("正在进行实名认证与防沉迷校验…") }
    var allowed by remember { mutableStateOf(false) }
    var canRetry by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        ComplianceManager.register { result ->
            when (result) {
                ComplianceManager.Result.Allowed -> {
                    allowed = true
                    message = "认证通过，正在进入游戏…"
                }
                ComplianceManager.Result.Exited -> {
                    canRetry = true
                    message = "认证已退出，请重新登录"
                }
                ComplianceManager.Result.SwitchAccount -> {
                    canRetry = true
                    message = "账号已切换，请重新登录"
                }
                is ComplianceManager.Result.Blocked -> {
                    canRetry = true
                    message = result.message
                }
            }
        }
        ComplianceManager.start(activity, userId)
    }
    LaunchedEffect(allowed) {
        if (allowed) onAllowed()
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFB98E59)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(message, color = Color(0xFF2B2016), fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
            if (canRetry) {
                Button(onClick = onReturnToLogin, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA83224))) {
                    Text("返回登录")
                }
            }
        }
    }
}
