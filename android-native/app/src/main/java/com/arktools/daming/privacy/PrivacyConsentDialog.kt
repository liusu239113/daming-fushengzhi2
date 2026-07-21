package com.arktools.daming.privacy

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arktools.daming.ads.AdConfig
import kotlinx.coroutines.launch

@Composable
fun PrivacyConsentDialog(
    onAccepted: () -> Unit,
    onRejected: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = PrivacyPreferences(context)
    val paper = Color(0xFFF4E7C7)
    val ink = Color(0xFF2B2016)
    val muted = Color(0xFF6E5D46)
    val red = Color(0xFFA83224)
    val blue = Color(0xFF1565C0)

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(paper, RoundedCornerShape(18.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("隐私政策与用户协议", color = red, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("欢迎使用《大明浮生志3》。请在使用账号登录、实名认证、防沉迷和激励广告服务前阅读以下说明。", color = ink, fontSize = 14.sp)
                Text("我们处理的信息", color = ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("• TapTap 登录可能处理账号公开资料、OpenID/UnionID、设备及网络信息，用于登录、账号安全、实名认证和防沉迷。\n• 激励广告 SDK 可能处理 Android ID、OAID、设备型号、系统版本、网络状态及广告交互记录，用于广告请求、展示、归因、统计和反作弊。\n• 游戏存档和设置默认保存在本机。", color = muted, fontSize = 12.sp, lineHeight = 18.sp)
                Text("第三方 SDK 与权限", color = ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("本应用使用 TapTap SDK 与 Tosin 聚合广告 SDK。我们关闭电话状态、MAC 地址、定位、录音和已安装应用列表等非必要能力，仅保留广告服务所需的网络、Android ID、OAID 与 Wi-Fi 状态能力。", color = muted, fontSize = 12.sp, lineHeight = 18.sp)
                Text("您的权利", color = ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("您可以拒绝并退出，也可以在系统设置中清除应用数据以撤回本地同意记录。拒绝后不会初始化 TapTap 或广告 SDK。完整政策将说明查询、更正、删除、撤回同意、账号注销和联系我们的方式。", color = muted, fontSize = 12.sp, lineHeight = 18.sp)
                if (AdConfig.privacyPolicyUrl.startsWith("http://") || AdConfig.privacyPolicyUrl.startsWith("https://")) {
                    Text(
                        text = "查看《大明浮生志3完整隐私政策》",
                        color = blue,
                        fontSize = 13.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { openWebUrl(context, AdConfig.privacyPolicyUrl) }
                    )
                } else {
                    Text("完整隐私政策网页部署完成后，此处将显示可点击的蓝色 HTTPS 链接。", color = muted, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(2.dp))
            Row(Modifier.fillMaxWidth()) {
                Button(
                    onClick = onRejected,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF786B5A)),
                    modifier = Modifier.weight(1f)
                ) { Text("不同意并退出") }
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = {
                        scope.launch {
                            preferences.accept()
                            onAccepted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = red),
                    modifier = Modifier.weight(1f)
                ) { Text("同意并继续") }
            }
        }
    }
}

private fun openWebUrl(context: Context, url: String) {
    if (!url.startsWith("http://") && !url.startsWith("https://")) return
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: ActivityNotFoundException) {
        // 没有可处理 HTTPS 链接的应用时保持当前页面，不影响用户继续阅读摘要。
    }
}
