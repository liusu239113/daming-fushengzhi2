package com.daming.fushengzhi2.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daming.fushengzhi2.data.GameContent
import com.daming.fushengzhi2.data.GameImages
import com.daming.fushengzhi2.logic.GameController
import com.daming.fushengzhi2.ui.components.AssetBackground
import com.daming.fushengzhi2.ui.components.AssetImage
import com.daming.fushengzhi2.ui.components.MingButton
import com.daming.fushengzhi2.ui.theme.MingColors

@Composable
fun CreateGameScreen(controller: GameController) {
    var surname by remember { mutableStateOf(GameContent.surnames.first()) }
    var originId by remember { mutableStateOf(GameContent.origins.first().id) }
    var regionId by remember { mutableStateOf(GameContent.regions.first().id) }
    var mottoId by remember { mutableStateOf(GameContent.familyMottos.first().id) }
    var difficultyId by remember { mutableStateOf("normal") }

    AssetBackground(
        path = GameImages.BgTexture,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    ) {
        Column(Modifier.fillMaxSize()) {
            CreateHeader()
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 720.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OptionSection("姓氏", "开宗立族之名") {
                    WrapRow(GameContent.surnames, 6) { item ->
                        SurnameChip(item, surname == item) {
                            controller.audio.select()
                            surname = item
                        }
                    }
                }
                ImageOptionSection("出身", "决定初始资源与家族起点") {
                    GameContent.origins.forEach { origin ->
                        ImageSelectCard(
                            title = origin.name,
                            subtitle = origin.desc,
                            imagePath = GameImages.OriginCards[origin.id],
                            selected = originId == origin.id
                        ) {
                            controller.audio.select()
                            originId = origin.id
                        }
                    }
                }
                ImageOptionSection("地域", "影响税赋、匪患与灾害") {
                    GameContent.regions.forEach { region ->
                        ImageSelectCard(
                            title = region.name,
                            subtitle = region.desc,
                            imagePath = GameImages.RegionCards[region.id],
                            selected = regionId == region.id
                        ) {
                            controller.audio.select()
                            regionId = region.id
                        }
                    }
                }
                ImageOptionSection("家训", "延续族风，影响长期玩法") {
                    GameContent.familyMottos.forEach { motto ->
                        ImageSelectCard(
                            title = "${motto.icon} ${motto.name}",
                            subtitle = motto.desc,
                            imagePath = GameImages.MottoCards[motto.id],
                            selected = mottoId == motto.id
                        ) {
                            controller.audio.select()
                            mottoId = motto.id
                        }
                    }
                }
                ImageOptionSection("难度", "选择浮生路上的风浪") {
                    GameContent.difficulties.forEach { diff ->
                        ImageSelectCard(
                            title = diff.name,
                            subtitle = diff.desc,
                            imagePath = GameImages.DifficultyCards[diff.id],
                            selected = difficultyId == diff.id
                        ) {
                            controller.audio.select()
                            difficultyId = diff.id
                        }
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MingColors.BgPanel.copy(alpha = 0.88f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MingButton("返回", Modifier.weight(1f), danger = true, onClick = controller::backToMenu)
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(58.dp)
                        .clickable {
                            controller.newGame(surname, originId, regionId, mottoId, difficultyId)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AssetImage(
                        path = GameImages.ButtonStartJourney,
                        contentDescription = "开始浮生",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp),
        contentAlignment = Alignment.Center
    ) {
        AssetImage(
            path = GameImages.BgTopBar,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        AssetImage(
            path = GameImages.DecoTitleBanner,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(0.84f).height(54.dp),
            contentScale = ContentScale.Fit
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("开宗立族", color = MingColors.GoldDark, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("选择姓氏、出身、地域、家训与难度", color = MingColors.TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun OptionSection(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = MingColors.GoldDark, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = MingColors.TextMuted, fontSize = 12.sp)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun ImageOptionSection(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    OptionSection(title, subtitle, content)
}

@Composable
private fun ImageSelectCard(title: String, subtitle: String, imagePath: String?, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) MingColors.BgPanel else MingColors.BgWhite.copy(alpha = 0.94f)),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MingColors.Gold else MingColors.Border)
    ) {
        Row(
            Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imagePath != null) {
                AssetImage(
                    path = imagePath,
                    contentDescription = null,
                    modifier = Modifier.height(70.dp).fillMaxWidth(0.34f),
                    contentScale = ContentScale.Crop,
                    alpha = if (selected) 1f else 0.88f
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = if (selected) MingColors.GoldDark else MingColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MingColors.TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Text(if (selected) "已选" else "", color = MingColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SurnameChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .alpha(if (selected) 1f else 0.92f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) MingColors.Primary else MingColors.BgWhite.copy(alpha = 0.92f)),
        border = BorderStroke(1.dp, if (selected) MingColors.PrimaryDark else MingColors.Border),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text,
            Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
            color = if (selected) MingColors.BgWhite else MingColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun <T> WrapRow(items: List<T>, perRow: Int, itemContent: @Composable (T) -> Unit) {
    val rows = items.chunked(perRow)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { itemContent(it) } }
        }
    }
}
