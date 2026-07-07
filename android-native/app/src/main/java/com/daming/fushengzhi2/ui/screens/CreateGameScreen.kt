package com.daming.fushengzhi2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.daming.fushengzhi2.data.GameContent
import com.daming.fushengzhi2.logic.GameController
import com.daming.fushengzhi2.ui.components.MingButton
import com.daming.fushengzhi2.ui.components.SectionTitle
import com.daming.fushengzhi2.ui.theme.MingColors

@Composable
fun CreateGameScreen(controller: GameController) {
    var surname by remember { mutableStateOf(GameContent.surnames.first()) }
    var originId by remember { mutableStateOf(GameContent.origins.first().id) }
    var regionId by remember { mutableStateOf(GameContent.regions.first().id) }
    var mottoId by remember { mutableStateOf(GameContent.familyMottos.first().id) }
    var difficultyId by remember { mutableStateOf("normal") }

    Column(
        Modifier
            .fillMaxSize()
            .background(MingColors.BgLight)
    ) {
        SectionTitle("开宗立族", "选择姓氏、出身、地域、家训与难度")
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OptionSection("姓氏") {
                WrapRow(GameContent.surnames) { item ->
                    SelectChip(item, surname == item) { surname = item }
                }
            }
            OptionSection("出身") {
                GameContent.origins.forEach { origin ->
                    SelectCard(origin.name, origin.desc, originId == origin.id) { originId = origin.id }
                }
            }
            OptionSection("地域") {
                GameContent.regions.forEach { region ->
                    SelectCard(region.name, region.desc, regionId == region.id) { regionId = region.id }
                }
            }
            OptionSection("家训") {
                GameContent.familyMottos.forEach { motto ->
                    SelectCard("${motto.icon} ${motto.name}", motto.desc, mottoId == motto.id) { mottoId = motto.id }
                }
            }
            OptionSection("难度") {
                GameContent.difficulties.forEach { diff ->
                    SelectCard(diff.name, diff.desc, difficultyId == diff.id) { difficultyId = diff.id }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MingButton("返回", Modifier.weight(1f), danger = true, onClick = controller::backToMenu)
            MingButton("开始浮生", Modifier.weight(2f)) {
                controller.newGame(surname, originId, regionId, mottoId, difficultyId)
            }
        }
    }
}

@Composable
private fun OptionSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = MingColors.GoldDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun SelectCard(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) MingColors.BgPanel else MingColors.BgWhite),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MingColors.Gold else MingColors.Border)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = if (selected) MingColors.GoldDark else MingColors.TextPrimary, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MingColors.TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SelectChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) MingColors.Primary else MingColors.BgWhite),
        border = BorderStroke(1.dp, if (selected) MingColors.PrimaryDark else MingColors.Border)
    ) {
        Text(text, Modifier.padding(horizontal = 14.dp, vertical = 9.dp), color = if (selected) MingColors.BgWhite else MingColors.TextPrimary)
    }
}

@Composable
private fun <T> WrapRow(items: List<T>, itemContent: @Composable (T) -> Unit) {
    val rows = items.chunked(5)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { itemContent(it) } }
        }
    }
}
