package com.daming.fushengzhi2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daming.fushengzhi2.logic.GameController
import com.daming.fushengzhi2.ui.components.MingButton
import com.daming.fushengzhi2.ui.theme.MingColors

@Composable
fun MainMenuScreen(controller: GameController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MingColors.BgLight)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("大明浮生志 · 贰", color = MingColors.GoldDark, fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("大明风华", color = MingColors.TextSecondary, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(48.dp))
        MingButton("新游戏", Modifier.fillMaxWidth(), onClick = controller::openCreate)
        Spacer(Modifier.height(12.dp))
        MingButton("继续游戏", Modifier.fillMaxWidth(), enabled = controller.hasAnySave(), onClick = controller::continueLatest)
        Spacer(Modifier.height(12.dp))
        MingButton("删除本地存档", Modifier.fillMaxWidth(), danger = true, enabled = controller.hasAnySave(), onClick = controller::deleteSaves)
        Spacer(Modifier.height(30.dp))
        Text(
            "原生 Kotlin 迁移版：保留宗族模拟、经营、月度结算与本地存档；原 Lua 脚本和资源已随项目归档。",
            color = MingColors.TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }

    controller.message?.let { msg ->
        AlertDialog(
            onDismissRequest = controller::clearMessage,
            confirmButton = { TextButton(onClick = controller::clearMessage) { Text("知道了") } },
            title = { Text("提示") },
            text = { Text(msg) }
        )
    }
}
