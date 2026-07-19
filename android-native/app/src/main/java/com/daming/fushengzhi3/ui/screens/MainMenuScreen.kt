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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

private val MenuBg = Color(0xFFB98E59)
private val MenuSurface = Color(0xFFF4E7C7)
private val MenuSurface2 = Color(0xFFE6D2A4)
private val MenuInk = Color(0xFF2B2016)
private val MenuMuted = Color(0xFF6E5D46)
private val MenuGold = Color(0xFF8A5A19)
private val MenuRed = Color(0xFFA83224)
private val MenuCyan = Color(0xFF426B67)
private val MenuShape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
private val MenuButtonShape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)

@Composable
fun MainMenuScreen(
    v3Controller: V3GameController,
    fontPreference: FontPreference,
    onNewGame: () -> Unit,
    onContinue: () -> Unit
) {
    LaunchedEffect(Unit) { v3Controller.ensureV3Bgm() }

    Box(Modifier.fillMaxSize().background(MenuBg)) {
        AssetImage(GameImages.V3MainMenuBg, null, Modifier.fillMaxSize(), ContentScale.Crop, alpha = 1f)
        Box(Modifier.fillMaxSize().background(Color(0x88F4E0B5)))
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .widthIn(max = 560.dp)
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            AssetImage(GameImages.V3MainLogo, "大明浮生志3", Modifier.fillMaxWidth().height(156.dp), ContentScale.Fit)
            Text("一户起家 · 娶妻生子 · 经营宗族 · 举旗定鼎", color = MenuInk, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(22.dp))
            MenuButton("开始新局", "从一人一户重立族谱", primary = true) {
                v3Controller.pauseForPlayerAction()
                v3Controller.pageTurn()
                onNewGame()
            }
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
    Box(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AssetImage(
            when {
                !enabled -> GameImages.MingyunSmallButtonDisabled
                primary -> GameImages.MingyunPrimaryButton
                else -> GameImages.MingyunSmallButton
            },
            null,
            Modifier.matchParentSize(),
            ContentScale.FillBounds
        )
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text, color = if (enabled) MenuInk else MenuMuted, fontSize = 21.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            if (subtitle.isNotBlank()) Text(subtitle, color = if (enabled) MenuMuted else MenuMuted.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun MenuPanel(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxWidth().widthIn(max = 460.dp)) {
        AssetImage(GameImages.MingyunDialog, null, Modifier.matchParentSize(), ContentScale.FillBounds)
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
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
    Box(
        modifier
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AssetImage(
            if (selected) GameImages.MingyunSmallButtonSelected else GameImages.MingyunSmallButton,
            null,
            Modifier.matchParentSize(),
            ContentScale.FillBounds
        )
        Text(
            text,
            color = if (selected) Color(0xFFFFF4D8) else MenuInk,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }
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
