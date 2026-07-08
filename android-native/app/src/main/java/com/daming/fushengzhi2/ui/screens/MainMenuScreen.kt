package com.daming.fushengzhi2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daming.fushengzhi2.data.GameImages
import com.daming.fushengzhi2.logic.GameController
import com.daming.fushengzhi2.ui.components.AssetBackground
import com.daming.fushengzhi2.ui.components.AssetImage
import com.daming.fushengzhi2.ui.theme.MingColors

@Composable
fun MainMenuScreen(controller: GameController) {
    AssetBackground(
        path = GameImages.MenuBg,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    ) {
        Text(
            "v1.0.51",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 8.dp),
            color = MingColors.TextMuted.copy(alpha = 0.65f),
            fontSize = 13.sp
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.92f)
                .widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AssetImage(
                path = GameImages.Logo,
                contentDescription = "大明浮生志贰",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(2.dp))
            V3MenuButton { controller.openV3Create() }
            MenuImageButton(GameImages.ButtonStart, enabled = true) { controller.openCreate() }
            MenuImageButton(GameImages.ButtonContinue, enabled = controller.hasAnySave()) { controller.continueLatest() }
            MenuImageButton(GameImages.ButtonSaves, enabled = true) { controller.openArchiveHint() }
            MenuImageButton(GameImages.ButtonSettings, enabled = true) { controller.openSettingsHint() }
        }
    }

    controller.message?.let { msg ->
        AlertDialog(
            onDismissRequest = controller::clearMessage,
            confirmButton = { TextButton(onClick = controller::clearMessage) { Text("知道了") } },
            title = { Text("提示") },
            text = { Text(msg, textAlign = TextAlign.Start) }
        )
    }
}

@Composable
private fun V3MenuButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(MingColors.TextPrimary.copy(alpha = 0.92f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            AssetImage(
                path = GameImages.V3Icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Crop
            )
            Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("大明浮生志3 · 县域宗族沙盘", color = MingColors.Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("上架版方向：案牍美术 / 人物派遣 / 房支政治 / 多结局", color = MingColors.BgLight, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun MenuImageButton(path: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .alpha(if (enabled) 1f else 0.42f)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        AssetImage(
            path = path,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
