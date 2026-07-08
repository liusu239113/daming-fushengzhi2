package com.daming.fushengzhi3.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.daming.fushengzhi3.data.GameImages
import com.daming.fushengzhi3.ui.components.AssetImage
import com.daming.fushengzhi3.v3.logic.V3GameController

private val MenuInk = Color(0xFF1F1712)
private val MenuPaper = Color(0xFFF3E2C2)
private val MenuRed = Color(0xFF9A2E24)
private val MenuGold = Color(0xFFC59A45)
private val MenuMuted = Color(0xFFD3BE91)

@Composable
fun MainMenuScreen(
    v3Controller: V3GameController,
    onNewGame: () -> Unit,
    onContinue: () -> Unit
) {
    LaunchedEffect(Unit) { v3Controller.ensureV3Bgm() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MenuInk)
    ) {
        AssetImage(
            path = GameImages.V3DossierBg,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.72f
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xB0120D0A))
        )
        Text(
            "v1.1.0",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 10.dp),
            color = MenuMuted.copy(alpha = 0.75f),
            fontSize = 13.sp
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.92f)
                .widthIn(max = 540.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AssetImage(
                path = GameImages.V3Icon,
                contentDescription = "大明浮生志3",
                modifier = Modifier.size(96.dp),
                contentScale = ContentScale.Crop
            )
            Text("大明浮生志3", color = MenuGold, fontSize = 38.sp, fontWeight = FontWeight.Bold)
            Text("明末县域宗族沙盘", color = MenuPaper, fontSize = 17.sp)
            V3MenuDossierCard()
            V3MenuButton("开始新局", "重立谱牒，选择宗族根基、县域与初始危机。", onClick = onNewGame)
            V3MenuButton(
                "继续案卷",
                if (v3Controller.hasSave()) "读取县域案卷，继续书写家乘。" else "暂无案卷，可先开始新局。",
                enabled = v3Controller.hasSave()
            ) {
                v3Controller.continueGame()
                onContinue()
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                V3MenuSmallButton("玩法说明", Modifier.weight(1f)) { v3Controller.openPlayGuide() }
                V3MenuSmallButton("音画说明", Modifier.weight(1f)) { v3Controller.openAudioVisualGuide() }
            }
        }
    }

    v3Controller.message?.let { msg ->
        AlertDialog(
            onDismissRequest = v3Controller::clearMessage,
            confirmButton = { TextButton(onClick = v3Controller::clearMessage) { Text("知道了") } },
            title = { Text("案牍提示") },
            text = { Text(msg, textAlign = TextAlign.Start) }
        )
    }
}

@Composable
private fun V3MenuDossierCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MenuPaper.copy(alpha = 0.94f)),
        border = BorderStroke(1.dp, MenuGold),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("县域案卷", color = MenuRed, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("经营田庄、周旋官府、安抚乡民、抵御流寇、培养族人，并在王朝崩塌前为宗族选择命运。", color = MenuInk, fontSize = 14.sp)
            AssetImage(
                path = GameImages.V3CountyMap,
                contentDescription = "县域旧地图",
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.95f
            )
        }
    }
}

@Composable
private fun V3MenuButton(text: String, subtitle: String, enabled: Boolean = true, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(containerColor = if (enabled) MenuRed else Color(0xFF5B5148)),
        border = BorderStroke(1.dp, if (enabled) MenuGold else Color(0xFF8F806A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text, color = MenuPaper, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MenuMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun V3MenuSmallButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(
        text,
        modifier = modifier
            .background(MenuPaper, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        color = MenuInk,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}
