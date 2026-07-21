package com.arktools.daming.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RewardedSilverButton(onReward: () -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivity()
    var loading by remember { mutableStateOf(false) }

    Button(
        onClick = {
            val host = activity
            if (host == null) {
                Toast.makeText(context, "广告暂时不可用", Toast.LENGTH_SHORT).show()
                return@Button
            }
            RewardedAdController.show(
                activity = host,
                onLoadingChanged = { loading = it },
                onRewarded = onReward,
                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() },
                onClosed = {}
            )
        },
        enabled = !loading,
        modifier = Modifier.fillMaxWidth().height(58.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA83224))
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("看激励广告领取 50 银两", color = Color.White, textAlign = TextAlign.Center)
                Text("完整观看并通过奖励校验后到账", color = Color(0xFFFFD7B8), textAlign = TextAlign.Center)
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
