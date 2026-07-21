package com.arktools.daming.ads.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 激励广告加载全屏遮罩。
 *
 * 设计原则：
 *  - 静态（不做旋转 / 动画），避免低端机或部分厂商 ROM 下动画导致的闪烁、卡死；
 *  - 不可点击穿透、不可返回键关闭（直到广告加载成功/失败/超时后由状态位驱动自动关闭）；
 *  - 配色沿用大明浮生志 V3 主题（暗底 + 金边 + 浅米字），不使用系统默认白色对话框。
 */
@Composable
fun AdLoadingOverlay(visible: Boolean, label: String = "广告加载中…") {
    if (!visible) return
    Dialog(
        onDismissRequest = { /* 不可关闭，由 onLoadingChanged(false) 驱动消失 */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { /* 吞掉点击，避免误触底层 */ }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 静态圆形徽记：金边 + 暗底，无动画
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF171511))
                        .border(3.dp, Color(0xFFE2C17E), CircleShape)
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = label,
                    color = Color(0xFFFFF1D2),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "请稍候，完整观看后奖励即刻发放",
                    color = Color(0xFFD1C4AD),
                    fontSize = 12.sp
                )
            }
        }
    }
}
