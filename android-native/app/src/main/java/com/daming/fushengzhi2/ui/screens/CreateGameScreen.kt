package com.daming.fushengzhi2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daming.fushengzhi2.data.GameContent
import com.daming.fushengzhi2.data.GameImages
import com.daming.fushengzhi2.logic.GameController
import com.daming.fushengzhi2.ui.components.AssetBackground
import com.daming.fushengzhi2.ui.components.AssetImage
import com.daming.fushengzhi2.ui.theme.MingColors

@Composable
fun CreateGameScreen(controller: GameController) {
    var surname by remember { mutableStateOf(GameContent.surnames.first()) }
    var customSurname by remember { mutableStateOf("") }
    var useCustomSurname by remember { mutableStateOf(false) }
    var originId by remember { mutableStateOf(GameContent.origins.first().id) }
    var regionId by remember { mutableStateOf("jiangnan") }
    var mottoId by remember { mutableStateOf(GameContent.familyMottos.first().id) }
    var difficultyId by remember { mutableStateOf("normal") }

    val finalSurname = if (useCustomSurname && customSurname.isNotBlank()) customSurname.take(2) else surname
    val origin = GameContent.origin(originId)
    val region = GameContent.region(regionId)
    val motto = GameContent.motto(mottoId)
    val difficulty = GameContent.difficulty(difficultyId)

    AssetBackground(
        path = GameImages.MenuBg,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    ) {
        Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color(0x55FFFCF5)))
        Column(Modifier.fillMaxSize()) {
            CreateTitleBar { controller.backToMenu() }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .widthIn(max = 760.dp)
                    .align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionLabel("宗族姓氏")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    GameContent.surnames.forEach { name ->
                        SealChip(
                            name = name,
                            selected = !useCustomSurname && surname == name,
                            onClick = {
                                controller.audio.select()
                                surname = name
                                useCustomSurname = false
                            }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("自定义:", color = MingColors.TextSecondary, fontSize = 14.sp)
                    OutlinedTextField(
                        value = customSurname,
                        onValueChange = { value ->
                            customSurname = value.take(2)
                            useCustomSurname = value.isNotBlank()
                            controller.audio.select()
                        },
                        modifier = Modifier.width(76.dp).height(48.dp),
                        singleLine = true,
                        placeholder = { Text("输入", fontSize = 13.sp) },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = MingColors.TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MingColors.Gold,
                            unfocusedBorderColor = MingColors.Border,
                            focusedContainerColor = MingColors.BgWhite.copy(alpha = 0.85f),
                            unfocusedContainerColor = MingColors.BgWhite.copy(alpha = 0.75f)
                        )
                    )
                }

                SectionLabel("开局出身")
                ImageButtonRow(GameContent.origins, selectedId = originId, image = { GameImages.OriginCards[it.id] }, height = 58.dp) {
                    controller.audio.select()
                    originId = it.id
                }

                SectionLabel("开局地域")
                ImageButtonRow(GameContent.regions, selectedId = regionId, image = { GameImages.RegionCards[it.id] }, height = 48.dp) {
                    controller.audio.select()
                    regionId = it.id
                }

                SectionLabel("家训(永久增益)")
                ImageButtonRow(GameContent.familyMottos, selectedId = mottoId, image = { GameImages.MottoCards[it.id] }, height = 48.dp) {
                    controller.audio.select()
                    mottoId = it.id
                }

                SectionLabel("游戏难度")
                ImageButtonRow(GameContent.difficulties, selectedId = difficultyId, image = { GameImages.DifficultyCards[it.id] }, height = 58.dp) {
                    controller.audio.select()
                    difficultyId = it.id
                }

                InfoPanel(
                    text = "【${finalSurname}氏宗族】\n" +
                        "出身：${origin.name} — ${origin.desc}\n" +
                        "银${origin.silver}  粮${origin.grain}  布${origin.cloth}\n" +
                        "地域：${region.name} — ${region.desc}\n" +
                        "家训：${motto.name} — ${motto.desc}\n" +
                        "难度：${difficulty.name} — ${difficulty.desc}"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp)
                        .clickable {
                            controller.newGame(finalSurname.ifBlank { "李" }, originId, regionId, mottoId, difficultyId)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AssetImage(GameImages.ButtonStartJourney, "开始征程", Modifier.fillMaxSize(), ContentScale.Fit)
                }
            }
        }
    }
}

@Composable
private fun CreateTitleBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(androidx.compose.ui.graphics.Color(0xDDFFFFFF))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("‹", modifier = Modifier.clickable(onClick = onBack).padding(horizontal = 6.dp), color = MingColors.Gold, fontSize = 30.sp)
        Text("开局创建", color = MingColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = MingColors.Gold, fontSize = 15.sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun SealChip(name: String, selected: Boolean, onClick: () -> Unit) {
    val image = GameImages.SurnameSeals[name]
    Box(
        modifier = Modifier
            .size(41.dp)
            .alpha(if (selected) 1f else 0.72f)
            .clip(RoundedCornerShape(7.dp))
            .background(MingColors.BgWhite.copy(alpha = 0.2f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (image != null) AssetImage(image, name, Modifier.fillMaxSize(), ContentScale.Crop)
        Box(
            Modifier
                .matchParentSize()
                .background(androidx.compose.ui.graphics.Color.Transparent)
        )
        if (selected) {
            Box(Modifier.matchParentSize().border(3.dp, MingColors.Gold, RoundedCornerShape(7.dp)))
        }
    }
}

@Composable
private fun <T> ImageButtonRow(items: List<T>, selectedId: String, image: (T) -> String?, height: androidx.compose.ui.unit.Dp, onClick: (T) -> Unit) where T : Any {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            val id = itemId(item)
            val selected = id == selectedId
            val path = image(item)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .alpha(if (selected) 1f else 0.72f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onClick(item) },
                contentAlignment = Alignment.Center
            ) {
                if (path != null) AssetImage(path, id, Modifier.fillMaxSize(), ContentScale.Crop)
                if (selected) Box(Modifier.matchParentSize().border(3.dp, if (id == "jiangnan" || id == "huguang" || id == "henan" || id == "shaanbei") MingColors.Primary else MingColors.Gold, RoundedCornerShape(10.dp)))
            }
        }
    }
}

private fun itemId(item: Any): String = when (item) {
    is com.daming.fushengzhi2.data.Origin -> item.id
    is com.daming.fushengzhi2.data.Region -> item.id
    is com.daming.fushengzhi2.data.FamilyMotto -> item.id
    is com.daming.fushengzhi2.data.Difficulty -> item.id
    else -> item.toString()
}

@Composable
private fun InfoPanel(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MingColors.BgWhite.copy(alpha = 0.78f)),
        border = BorderStroke(1.dp, MingColors.Border),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            color = MingColors.TextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}
