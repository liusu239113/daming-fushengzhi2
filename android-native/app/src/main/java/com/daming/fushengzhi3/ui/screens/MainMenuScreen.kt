package com.daming.fushengzhi3.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.daming.fushengzhi3.data.GameImages
import com.daming.fushengzhi3.ui.components.AssetImage
import com.daming.fushengzhi3.ui.theme.FontPreference
import com.daming.fushengzhi3.ui.theme.FontStyleKey
import com.daming.fushengzhi3.v3.logic.V3GameController

private val MenuBg = Color(0xFF0F0F23)
private val MenuSurface = Color(0xFF1B1B3A)
private val MenuSurface2 = Color(0xFF252550)
private val MenuInk = Color(0xFFF3E7C7)
private val MenuMuted = Color(0xFFAFA6C8)
private val MenuGold = Color(0xFFE0B85A)
private val MenuRed = Color(0xFFB9352B)
private val MenuCyan = Color(0xFF21BDAE)

@Composable
fun MainMenuScreen(
    v3Controller: V3GameController,
    fontPreference: FontPreference,
    onNewGame: () -> Unit,
    onContinue: () -> Unit
) {
    LaunchedEffect(Unit) { v3Controller.ensureV3Bgm() }

    Box(Modifier.fillMaxSize().background(MenuBg)) {
        AssetImage(GameImages.V3DossierBg, null, Modifier.fillMaxSize(), ContentScale.Crop, alpha = 0.28f)
        Box(Modifier.fillMaxSize().background(Color(0xD50B0B18)))
        Column(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(0.9f).widthIn(max = 560.dp).padding(top = 78.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("大明浮生志3", color = MenuGold, fontSize = 42.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("一户起家 · 娶妻生子 · 经营宗族 · 举旗定鼎", color = MenuInk, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))
            MenuButton("开始新局", "从一人一户重立族谱", primary = true, onClick = onNewGame)
            MenuButton(
                "继续游戏",
                if (v3Controller.hasSave()) "读取已有宗族案卷" else "暂无存档",
                enabled = v3Controller.hasSave()
            ) {
                v3Controller.continueGame()
                onContinue()
            }
            MenuButton("设置", "字体、音乐和音效", onClick = v3Controller::openSettings)
        }
        Text("v1.0.0", Modifier.align(Alignment.BottomStart).padding(12.dp), color = MenuMuted, fontSize = 12.sp)
    }

    if (v3Controller.settingsVisible) {
        MenuSettingsDialog(v3Controller, fontPreference)
    }
    v3Controller.message?.let { msg ->
        Dialog(onDismissRequest = v3Controller::clearMessage) {
            MenuPanel {
                Text("提示", color = MenuGold, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text(msg, color = MenuInk, fontSize = 15.sp, lineHeight = 22.sp)
                MenuButton("知道了", onClick = v3Controller::clearMessage)
            }
        }
    }
}

@Composable
private fun MenuButton(text: String, subtitle: String = "", enabled: Boolean = true, primary: Boolean = false, onClick: () -> Unit) {
    val bg = when {
        !enabled -> Color(0xFF34344F)
        primary -> MenuRed
        else -> MenuSurface2
    }
    val border = if (primary) MenuGold else MenuCyan
    Card(
        modifier = Modifier.fillMaxWidth().then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(2.dp, border),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 15.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text, color = if (enabled) MenuInk else MenuMuted, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            if (subtitle.isNotBlank()) Text(subtitle, color = if (enabled) MenuMuted else MenuMuted.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun MenuPanel(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MenuSurface),
        border = BorderStroke(2.dp, MenuGold),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
        modifier = Modifier.fillMaxWidth().widthIn(max = 460.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
private fun MenuSettingsDialog(controller: V3GameController, fontPreference: FontPreference) {
    Dialog(onDismissRequest = controller::closeSettings) {
        MenuPanel {
            Text("设置", color = MenuGold, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text("字体", color = MenuCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            FontStyleKey.entries.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { style ->
                        MenuChoice(style.label, selected = fontPreference.style == style, modifier = Modifier.weight(1f)) { fontPreference.updateStyle(style) }
                    }
                    repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            MenuVolume("背景音乐", controller.bgmVolume, controller::updateBgmVolume)
            MenuVolume("音效", controller.sfxVolume, controller::updateSfxVolume)
            MenuButton("关闭", primary = true, onClick = controller::closeSettings)
        }
    }
}

@Composable
private fun MenuChoice(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Text(
        text,
        modifier = modifier.background(if (selected) MenuRed else MenuSurface2).clickable(onClick = onClick).padding(vertical = 10.dp),
        color = MenuInk,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun MenuVolume(label: String, value: Float, onChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = MenuInk, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("${(value * 100).toInt()}%", color = MenuMuted, fontSize = 13.sp)
        }
        Slider(value = value, onValueChange = onChange, valueRange = 0f..1f)
    }
}
