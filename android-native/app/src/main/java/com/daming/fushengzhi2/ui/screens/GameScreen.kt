package com.daming.fushengzhi2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.daming.fushengzhi2.data.GameContent
import com.daming.fushengzhi2.data.GameImages
import com.daming.fushengzhi2.data.GameState
import com.daming.fushengzhi2.data.GameTab
import com.daming.fushengzhi2.data.MemberState
import com.daming.fushengzhi2.logic.GameController
import com.daming.fushengzhi2.logic.GameEngine
import com.daming.fushengzhi2.ui.components.AssetBackground
import com.daming.fushengzhi2.ui.components.AssetImage
import com.daming.fushengzhi2.ui.components.DividerLine
import com.daming.fushengzhi2.ui.components.EmptyHint
import com.daming.fushengzhi2.ui.components.MingButton
import com.daming.fushengzhi2.ui.components.MingCard
import com.daming.fushengzhi2.ui.components.SectionTitle
import com.daming.fushengzhi2.ui.theme.MingColors
import kotlinx.coroutines.delay

private data class MainNavItem(val tab: GameTab, val label: String, val imagePath: String)

private val mainNavItems = listOf(
    MainNavItem(GameTab.Tree, "族谱", GameImages.NavTree),
    MainNavItem(GameTab.Clan, "宗族", GameImages.NavClan),
    MainNavItem(GameTab.Members, "族人", GameImages.NavMembers),
    MainNavItem(GameTab.Industry, "经营", GameImages.NavIndustry)
)

@Composable
fun GameScreen(controller: GameController) {
    val state = controller.state ?: return
    LaunchedEffect(controller.speed, controller.latestReport, controller.screen) {
        while (controller.speed > 0 && controller.latestReport == null && controller.screen == GameController.Screen.Game) {
            delay(when (controller.speed) { 1 -> 4000L; 2 -> 2500L; else -> 1500L })
            if (controller.speed > 0 && controller.latestReport == null && controller.screen == GameController.Screen.Game) {
                controller.advanceMonth()
            }
        }
    }

    AssetBackground(
        path = if (controller.tab == GameTab.Tree) GameImages.BgFamilyTree else GameImages.BgMainLandscape,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                TopBar(state, controller)
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .widthIn(max = 820.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GameContentArea(state, controller)
                }
                BottomNav(controller)
            }
            if (controller.tab == GameTab.Tree) SideActionRail(controller, Modifier.align(Alignment.CenterEnd))
            TimeControl(controller, Modifier.align(Alignment.BottomEnd).padding(end = 10.dp, bottom = 92.dp))
        }
    }

    controller.latestReport?.let { report ->
        MingDialog(onDismiss = controller::clearReport, title = "${report.year}年${report.month}月结算") {
            Text("收入：银${report.incomeSilver} 粮${report.incomeGrain} 布${report.incomeCloth} 名${report.incomeFame}", color = MingColors.TextPrimary, fontSize = 14.sp)
            Text("支出：银${report.expenseSilver} 粮${report.expenseGrain} 布${report.expenseCloth} 名${report.expenseFame}", color = MingColors.TextPrimary, fontSize = 14.sp)
            DividerLine()
            report.events.take(8).forEach { Text("· $it", color = MingColors.TextSecondary, fontSize = 13.sp) }
        }
    }

    controller.message?.let { msg ->
        MingDialog(onDismiss = controller::clearMessage, title = "⚠") {
            Text(msg, color = MingColors.TextPrimary, fontSize = 16.sp, lineHeight = 23.sp)
        }
    }
}

@Composable
private fun TopBar(state: GameState, controller: GameController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(124.dp)
    ) {
        AssetImage(GameImages.BgTopBar, null, Modifier.fillMaxSize(), ContentScale.Crop)
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 54.dp, top = 8.dp, bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(state.clanName, color = MingColors.GoldDark, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        GameEngine.clanRankName(state),
                        modifier = Modifier.background(MingColors.BgWhite.copy(alpha = 0.65f), RoundedCornerShape(5.dp)).padding(horizontal = 7.dp, vertical = 3.dp),
                        color = MingColors.GoldDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${mingEraLabel(state.year)} ${state.month}月", color = MingColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("● 春 · 多云", color = MingColors.TextSecondary, fontSize = 12.sp)
                    Text(
                        "⚙ 设置",
                        modifier = Modifier.background(MingColors.BgWhite.copy(alpha = 0.72f), RoundedCornerShape(5.dp)).clickable { controller.openSettingsHint() }.padding(horizontal = 7.dp, vertical = 4.dp),
                        color = MingColors.TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ResourceIconPill(GameImages.ResourceSilver, state.silver, MingColors.Silver, Modifier.weight(1f))
                ResourceIconPill(GameImages.ResourceGrain, state.grain, MingColors.Grain, Modifier.weight(1f))
                ResourceIconPill(GameImages.ResourceCloth, state.cloth, MingColors.Cloth, Modifier.weight(1f))
                ResourceIconPill(GameImages.ResourceFame, state.fame, MingColors.Fame, Modifier.weight(1f))
                ResourceIconPill(GameImages.ResourcePop, GameEngine.aliveMembers(state).size, MingColors.Green, Modifier.weight(1f))
            }
            val debtHint = if (state.silver < 50) "贷 银两不足？点击钱庄借贷 ›" else ""
            if (debtHint.isNotEmpty()) {
                Text(
                    debtHint,
                    modifier = Modifier.fillMaxWidth().clickable { controller.switchTab(GameTab.Career) }.padding(top = 1.dp),
                    color = MingColors.Green,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ResourceIconPill(icon: String, value: Int, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(androidx.compose.ui.graphics.Color(0xCCFFFCF0), RoundedCornerShape(9.dp))
            .padding(horizontal = 5.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssetImage(icon, null, Modifier.size(20.dp), ContentScale.Fit)
        Text(com.daming.fushengzhi2.ui.components.formatNumber(value), color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SmallTopButton(text: String, danger: Boolean = false, onClick: () -> Unit) {
    Text(
        text,
        modifier = Modifier
            .background(if (danger) MingColors.Red.copy(alpha = 0.9f) else MingColors.Primary.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = MingColors.BgWhite,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun TimeControl(controller: GameController, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(96.dp)
            .width(244.dp),
        contentAlignment = Alignment.Center
    ) {
        AssetImage(GameImages.BgControlPanel, null, Modifier.fillMaxSize(), ContentScale.Crop)
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("●", color = MingColors.Green, fontSize = 12.sp)
                Text("${controller.state?.let { mingEraLabel(it.year) + it.month + "月" } ?: "时光"}", color = MingColors.Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Text(if (controller.speed == 0) "已暂停" else "${controller.speed}x 时光流转中", color = if (controller.speed == 0) MingColors.Red else MingColors.Green, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                AssetImage(GameImages.IconPause, "暂停", Modifier.size(34.dp).alpha(if (controller.speed == 0) 1f else 0.42f).clickable { controller.pauseSpeed() }, ContentScale.Fit)
                AssetImage(GameImages.IconPlay1x, "1x", Modifier.size(38.dp).alpha(if (controller.speed == 1) 1f else 0.42f).clickable { controller.cycleSpeed() }, ContentScale.Fit)
                AssetImage(GameImages.IconPlay2x, "2x", Modifier.size(34.dp).alpha(if (controller.speed == 2) 1f else 0.36f).clickable { controller.cycleSpeed() }, ContentScale.Fit)
                AssetImage(GameImages.IconPlay3x, "3x", Modifier.size(34.dp).alpha(if (controller.speed == 3) 1f else 0.36f).clickable { controller.cycleSpeed() }, ContentScale.Fit)
            }
        }
    }
}

@Composable
private fun GameContentArea(state: GameState, controller: GameController) {
    when (controller.tab) {
        GameTab.Tree -> FamilyTreePage(state, controller)
        GameTab.Clan -> {
            SubTabBar(GameController.ClanSubTab.entries, controller.clanSubTab, { it.label }, controller::switchClanSubTab)
            when (controller.clanSubTab) {
                GameController.ClanSubTab.Main -> ClanMainPage(state, controller)
                GameController.ClanSubTab.Rules -> ClanRulesPage(state, controller)
                GameController.ClanSubTab.Chronicle -> ClanChroniclePage(state)
            }
        }
        GameTab.Members -> MembersPage(state, controller)
        GameTab.Industry -> {
            SubTabBar(GameController.IndustrySubTab.entries, controller.industrySubTab, { it.label }, controller::switchIndustrySubTab)
            when (controller.industrySubTab) {
                GameController.IndustrySubTab.Main -> IndustryPage(state, controller)
                GameController.IndustrySubTab.Market -> MarketPage(state, controller)
                GameController.IndustrySubTab.Store -> InventoryPage(state, controller)
            }
        }
        GameTab.Career -> {
            SubTabBar(GameController.CareerSubTab.entries, controller.careerSubTab, { it.label }, controller::switchCareerSubTab)
            when (controller.careerSubTab) {
                GameController.CareerSubTab.Career -> CareerPage(state, controller)
                GameController.CareerSubTab.Academy -> AcademyPage(state, controller)
                GameController.CareerSubTab.Expedition -> ExpeditionPage(state, controller)
            }
        }
        GameTab.Battle -> BattlePage(state, controller)
        GameTab.Academy -> AcademyPage(state, controller)
        GameTab.Expedition -> ExpeditionPage(state, controller)
        GameTab.Inventory -> InventoryPage(state, controller)
        GameTab.Market -> MarketPage(state, controller)
    }
}

@Composable
private fun <T> SubTabBar(items: List<T>, selected: T, label: (T) -> String, onClick: (T) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(MingColors.BgWhite.copy(alpha = 0.72f)),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        items.forEach { item ->
            val isSelected = item == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClick(item) }
                    .padding(top = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    label(item),
                    color = if (isSelected) MingColors.GoldDark else MingColors.TextMuted.copy(alpha = 0.62f),
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(if (isSelected) MingColors.Gold else androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun BottomNav(controller: GameController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        contentAlignment = Alignment.Center
    ) {
        AssetImage(GameImages.BgBottomNav, null, Modifier.fillMaxSize(), ContentScale.Crop)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            mainNavItems.forEach { item ->
                BottomNavButton(item, selected = controller.tab == item.tab) { controller.switchTab(item.tab) }
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavButton(item: MainNavItem, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(78.dp)
            .alpha(if (selected) 1f else 0.78f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AssetImage(item.imagePath, item.label, Modifier.fillMaxSize(), ContentScale.Fit)
        if (selected) {
            Text(
                "●",
                modifier = Modifier.align(Alignment.TopCenter),
                color = MingColors.Gold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SideActionRail(controller: GameController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(end = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SideActionButton(GameImages.NavLoan, "钱庄") { controller.switchTab(GameTab.Career) }
        SideActionButton(GameImages.NavClinic, "医馆") { controller.openSettingsHint() }
        SideActionButton(GameImages.NavLabor, "打工") { controller.switchTab(GameTab.Members) }
    }
}

@Composable
private fun SideActionButton(image: String, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        AssetImage(image, label, Modifier.size(58.dp), ContentScale.Fit)
        Text(label, color = MingColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MingDialog(onDismiss: () -> Unit, title: String, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MingColors.BgWhite),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(2.dp, MingColors.Gold)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(title, color = MingColors.GoldDark, fontSize = if (title == "⚠") 30.sp else 18.sp, fontWeight = FontWeight.Bold)
                DividerLine()
                content()
                Text(
                    "知道了",
                    modifier = Modifier.background(MingColors.Primary, RoundedCornerShape(7.dp)).clickable(onClick = onDismiss).padding(horizontal = 30.dp, vertical = 9.dp),
                    color = MingColors.BgWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun mingEraLabel(year: Int): String = when {
    year < 1399 -> "洪武元年"
    year < 1403 -> "建文元年"
    year < 1425 -> "永乐元年"
    year < 1436 -> "宣德元年"
    year < 1450 -> "正统元年"
    year < 1457 -> "景泰元年"
    year < 1465 -> "天顺元年"
    year < 1488 -> "成化元年"
    year < 1506 -> "弘治元年"
    year < 1522 -> "正德元年"
    year < 1567 -> "嘉靖元年"
    year < 1573 -> "隆庆元年"
    year < 1621 -> "万历元年"
    year < 1628 -> "天启元年"
    else -> "崇祯元年"
}

@Composable
private fun PageHeader(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = MingColors.GoldDark, fontSize = 28.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, color = MingColors.TextSecondary, fontSize = 16.sp)
            }
            AssetImage(GameImages.DecoTitleBanner, null, Modifier.width(150.dp).height(38.dp), ContentScale.Fit, alpha = 0.72f)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(MingColors.Gold.copy(alpha = 0.78f)))
    }
}

@Composable
private fun PaperCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MingColors.BgWhite.copy(alpha = 0.82f)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MingColors.BorderGold.copy(alpha = 0.8f))
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun TextChip(text: String, selected: Boolean = false, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Text(
        text,
        modifier = modifier
            .background(if (selected) MingColors.GoldDark else MingColors.BgWhite.copy(alpha = 0.78f), RoundedCornerShape(10.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 13.dp, vertical = 8.dp),
        color = if (selected) MingColors.BgWhite else MingColors.TextSecondary,
        fontSize = 15.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
    )
}

@Composable
private fun MiniTextButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, danger: Boolean = false, onClick: () -> Unit) {
    Text(
        text,
        modifier = modifier
            .background(
                when {
                    !enabled -> MingColors.Border.copy(alpha = 0.45f)
                    danger -> MingColors.Red.copy(alpha = 0.12f)
                    else -> MingColors.BgPanel.copy(alpha = 0.9f)
                },
                RoundedCornerShape(7.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        color = when {
            !enabled -> MingColors.TextMuted.copy(alpha = 0.45f)
            danger -> MingColors.Red
            else -> MingColors.GoldDark
        },
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

@Composable
private fun RequirementLine(icon: String, label: String, current: Int, required: Int) {
    val ok = current >= required
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("$icon $label", color = MingColors.TextSecondary, fontSize = 15.sp)
        Text("$current / $required ${if (ok) "✓" else "×"}", color = if (ok) MingColors.Green else MingColors.Red.copy(alpha = 0.75f), fontSize = 14.sp)
    }
}

private fun memberGenderText(member: com.daming.fushengzhi2.data.ClanMember): String = if (member.gender.name == "Male") "男" else "女"

private fun memberGenderColor(member: com.daming.fushengzhi2.data.ClanMember): androidx.compose.ui.graphics.Color =
    if (member.gender.name == "Male") androidx.compose.ui.graphics.Color(0xFF2F75C8) else androidx.compose.ui.graphics.Color(0xFFB84372)

@Composable
private fun MemberPortraitCard(member: com.daming.fushengzhi2.data.ClanMember, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        AssetImage(GameImages.CardFrameMember, null, Modifier.fillMaxSize(), ContentScale.Fit)
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AssetImage(GameImages.FamilyMemberFront, member.name, Modifier.size(44.dp), ContentScale.Fit)
            Text(member.name, color = MingColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("${member.age}岁", color = MingColors.TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun IndustryThumb(typeId: String, modifier: Modifier = Modifier) {
    val image = GameImages.IndustryImages[typeId]
    Box(
        modifier = modifier.background(MingColors.BgPanel.copy(alpha = 0.85f), RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (image != null) AssetImage(image, typeId, Modifier.fillMaxSize(), ContentScale.Crop) else Text("业", color = MingColors.GoldDark, fontSize = 24.sp)
    }
}

@Composable
private fun ClanMainPage(state: GameState, controller: GameController) {
    val alive = GameEngine.aliveMembers(state)
    val nextRank = state.clanRank + 1
    val req = GameContent.rankRequirements[nextRank]
    PageHeader("宗祠管理", "${state.clanName} · ${GameEngine.clanRankName(state)}")
    PaperCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(state.clanName, color = MingColors.GoldDark, fontSize = 23.sp, fontWeight = FontWeight.Medium)
                Text("族人 ${alive.size} 人    传承 ${state.totalMonths} 月    寨堡 ${state.fortCount} 座", color = MingColors.TextPrimary, fontSize = 16.sp)
            }
            Text(GameContent.region(state.regionId).name, color = MingColors.TextSecondary, fontSize = 17.sp)
        }
    }
    PaperCard {
        Text("晋升条件 → ${GameContent.rankName(nextRank)}", color = MingColors.GoldDark, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        if (req != null) {
            RequirementLine("◉", "银两", state.silver, req.silver)
            RequirementLine("◎", "声望", state.fame, req.fame)
            RequirementLine("▣", "粮食", state.grain, req.grain)
            if (req.cloth > 0) RequirementLine("◇", "布匹", state.cloth, req.cloth)
            RequirementLine("●", "族人", alive.size, req.population)
            MiniTextButton("晋升宗族", Modifier.fillMaxWidth(), enabled = GameEngine.canRankUp(state), onClick = controller::rankUp)
        } else {
            Text("已达最高品级。", color = MingColors.Gold, fontSize = 16.sp)
        }
    }
    PaperCard {
        Text("年度目标", color = MingColors.GoldDark, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        if (state.yearlyGoals.isEmpty()) Text("暂无年度目标", color = MingColors.TextMuted, fontSize = 15.sp)
        state.yearlyGoals.forEach { goalState ->
            val goal = GameContent.yearlyGoal(goalState.goalId)
            Text("${goal?.icon ?: "目"} ${goal?.name ?: goalState.goalId}    ${if (goalState.completed) "已完成" else goal?.desc ?: ""}", color = if (goalState.completed) MingColors.Green else MingColors.TextSecondary, fontSize = 14.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniTextButton("祭天祈福", Modifier.weight(1f), enabled = state.lastSacrificeYear != state.year && state.grain >= 50, onClick = controller::sacrifice)
            MiniTextButton("收养黄犬", Modifier.weight(1f), enabled = state.pet?.alive != true && state.grain >= 20) { controller.adoptPet("dog") }
        }
    }
    PaperCard {
        Text("宗族大事记", color = MingColors.GoldDark, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        if (state.eventLog.isEmpty()) Text("暂无记录", color = MingColors.TextMuted, fontSize = 15.sp)
        state.eventLog.take(12).forEach { Text("${it.year}年${it.month}月 · ${it.text}", color = MingColors.TextSecondary, fontSize = 13.sp) }
    }
}

@Composable
private fun ClanRulesPage(state: GameState, controller: GameController) {
    PageHeader("族规", "启用家法宗规，改变长期经营节奏")
    PaperCard {
        Text("当前可启用 ${GameContent.maxRulesByRank[state.clanRank] ?: 0} 条", color = MingColors.TextMuted, fontSize = 14.sp)
        GameContent.clanRules.forEach { rule ->
            val active = state.clanRules.contains(rule.id)
            Card(
                modifier = Modifier.fillMaxWidth().clickable { controller.toggleRule(rule.id) },
                colors = CardDefaults.cardColors(containerColor = if (active) MingColors.BgPanel else MingColors.BgWhite.copy(alpha = 0.72f)),
                border = BorderStroke(1.dp, if (active) MingColors.Gold else MingColors.Border)
            ) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${rule.icon} ${rule.name}${if (active) " · 已启用" else ""}", color = if (active) MingColors.GoldDark else MingColors.TextPrimary, fontWeight = FontWeight.Bold)
                    Text(rule.desc, color = MingColors.TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ClanChroniclePage(state: GameState) {
    PageHeader("家族志", "记录宗族每月兴衰与历史回响")
    PaperCard {
        if (state.eventLog.isEmpty()) EmptyHint("尚无家族志记录")
        state.eventLog.take(60).forEach { Text("${it.year}年${it.month}月 · ${it.text}", color = MingColors.TextSecondary, fontSize = 13.sp) }
    }
}

@Composable
private fun FamilyTreePage(state: GameState, controller: GameController) {
    val alive = GameEngine.aliveMembers(state)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("● 男", color = androidx.compose.ui.graphics.Color(0xFF2F75C8), fontSize = 14.sp)
        Text("   ● 女", color = androidx.compose.ui.graphics.Color(0xFFB84372), fontSize = 14.sp)
        Text("   ◆ 故   第1代", color = MingColors.TextSecondary, fontSize = 14.sp)
    }
    if (alive.isEmpty()) {
        EmptyHint("暂无族人")
        return
    }
    val patriarch = alive.firstOrNull { it.id == state.patriarchId } ?: alive.first()
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MemberPortraitCard(patriarch, Modifier.width(108.dp).height(148.dp))
        val descendants = alive.filter { it.id != patriarch.id }.take(8)
        if (descendants.isNotEmpty()) {
            Box(Modifier.width(1.dp).height(24.dp).background(MingColors.BorderGold.copy(alpha = 0.7f)))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                descendants.chunked(4).first().forEach { member ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.width(42.dp).height(1.dp).background(MingColors.BorderGold.copy(alpha = 0.55f)))
                        MemberPortraitCard(member, Modifier.width(88.dp).height(120.dp))
                    }
                }
            }
        }
        PaperCard(Modifier.padding(top = 6.dp)) {
            Text("族谱详情", color = MingColors.GoldDark, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            alive.take(4).forEach { member ->
                val spouse = member.spouseId?.let { id -> state.members.firstOrNull { it.id == id }?.name } ?: "未婚"
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(member.name, color = MingColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("${memberGenderText(member)} · ${member.age}岁 · 第${member.generation}代 · ${member.identity} · 配偶：$spouse", color = MingColors.TextMuted, fontSize = 12.sp)
                    }
                    if (member.spouseId == null && member.age in 16..40) {
                        MiniTextButton("联姻", enabled = GameContent.marriageTiers.any { state.clanRank >= it.unlockRank }) {
                            GameContent.marriageTiers.firstOrNull { state.clanRank >= it.unlockRank }?.let { tier -> controller.arrangeMarriage(member.id, tier.id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MembersPage(state: GameState, controller: GameController) {
    val alive = GameEngine.aliveMembers(state)
    PageHeader("族人管理", "共 ${alive.size} 人")
    Text(
        "搜索族人姓名...",
        modifier = Modifier
            .fillMaxWidth()
            .background(MingColors.BgWhite.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        color = MingColors.TextMuted,
        fontSize = 19.sp
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        TextChip("全部", true)
        TextChip("在家")
        TextChip("读书")
        TextChip("经商")
        TextChip("从军")
        TextChip("生病")
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("排序:", color = MingColors.TextSecondary, fontSize = 16.sp)
            Text("⇅ 年龄", color = MingColors.Green, fontSize = 16.sp)
            Text("健康", color = MingColors.TextMuted, fontSize = 16.sp)
            Text("学识", color = MingColors.TextMuted, fontSize = 16.sp)
            Text("武力", color = MingColors.TextMuted, fontSize = 16.sp)
        }
        Text(
            "批量安排",
            modifier = Modifier.background(MingColors.BgWhite.copy(alpha = 0.75f), RoundedCornerShape(7.dp)).padding(horizontal = 14.dp, vertical = 9.dp),
            color = MingColors.GoldDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
    alive.forEach { member ->
        PaperCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(member.name, color = MingColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        Text(memberGenderText(member), color = memberGenderColor(member), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("${member.age}岁  ${member.identity}  ${GameContent.talent(member.talentId)?.name ?: "好吃懒做"}", color = MingColors.TextSecondary, fontSize = 15.sp)
                    Text("健${member.health}  学${member.study}  武${member.martial}", color = MingColors.TextMuted, fontSize = 14.sp)
                }
                Text(
                    member.state.label,
                    modifier = Modifier.background(MingColors.BgPanel.copy(alpha = 0.9f), RoundedCornerShape(3.dp)).padding(horizontal = 10.dp, vertical = 7.dp),
                    color = MingColors.TextSecondary,
                    fontSize = 14.sp
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(MemberState.Home, MemberState.Study, MemberState.Trade, MemberState.Military, MemberState.Labor).forEach { target ->
                    MiniTextButton(target.label, Modifier.weight(1f), enabled = member.state != target) { controller.setMemberState(member.id, target) }
                }
            }
            if (member.state == MemberState.Labor) {
                GameEngine.availableLaborJobs(state).takeLast(4).forEach { job ->
                    Text("工种：${job.name} 月银${job.wage} · ${job.desc}", modifier = Modifier.fillMaxWidth().clickable { controller.setMemberState(member.id, MemberState.Labor, job.id) }.padding(4.dp), color = MingColors.Primary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun IndustryPage(state: GameState, controller: GameController) {
    PageHeader("田庄产业", "管理家族产业")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Text("${state.industries.size}/${GameContent.industryLimitByRank[state.clanRank] ?: 4}", color = MingColors.TextSecondary, fontSize = 16.sp)
    }
    Text("现有产业", color = MingColors.GoldDark, fontSize = 22.sp, fontWeight = FontWeight.Medium)
    if (state.industries.isEmpty()) {
        PaperCard { Text("暂无产业", color = MingColors.TextMuted, fontSize = 15.sp) }
    }
    state.industries.forEach { ind ->
        val type = GameContent.industry(ind.typeId)
        val manager = ind.assignedMemberId?.let { id -> state.members.firstOrNull { it.id == id }?.name } ?: "无人管理"
        PaperCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IndustryThumb(ind.typeId, Modifier.size(92.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${type?.name ?: ind.typeId}(Lv.${ind.level})", color = MingColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("维护  银-${ind.level * 3}", color = MingColors.TextSecondary, fontSize = 13.sp)
                    }
                    Text("月产  ${type?.resource?.label ?: "资源"}+${(type?.baseOutput ?: 0) * ind.level}", color = MingColors.Green, fontSize = 15.sp)
                    Text("管理：$manager", color = MingColors.TextSecondary, fontSize = 15.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val evo = GameContent.evolution(ind.typeId)
                        MiniTextButton("分配", Modifier.weight(1f)) {
                            GameEngine.adults(state).firstOrNull()?.let { controller.assignIndustry(ind.id, it.id) }
                        }
                        MiniTextButton("进化·${evo?.to?.let { GameContent.industry(it)?.name } ?: "无"}", Modifier.weight(1f), enabled = evo != null && ind.level >= (evo?.reqLevel ?: 99) && state.clanRank >= (evo?.reqRank ?: 99) && state.silver >= (evo?.cost ?: 999999)) { controller.evolveIndustry(ind.id) }
                        MiniTextButton("升级Lv${ind.level + 1}(-${ind.level * 150}银)", Modifier.weight(1f), enabled = state.silver >= ind.level * 150) { controller.upgradeIndustry(ind.id) }
                        MiniTextButton("变卖(+${type?.cost?.div(2) ?: 0}银)", Modifier.weight(1f), danger = true) { controller.sellIndustry(ind.id) }
                    }
                }
            }
        }
    }
    Text("购买产业", color = MingColors.GoldDark, fontSize = 22.sp, fontWeight = FontWeight.Medium)
    GameContent.industryTypes.filter { !it.evolved }.take(12).forEach { type ->
        val locked = state.clanRank < type.unlockRank
        PaperCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IndustryThumb(type.id, Modifier.size(82.dp).alpha(if (locked) 0.35f else 1f))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (locked) "[锁] ${type.name}" else "新建 ${type.name}", color = if (locked) MingColors.TextMuted else MingColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Text(if (locked) "品级${GameContent.rankName(type.unlockRank)}解锁" else "产${type.resource.label} · 受地域与管理加成", color = MingColors.TextMuted, fontSize = 13.sp)
                    Text(type.desc, color = MingColors.TextMuted.copy(alpha = 0.88f), fontSize = 12.sp)
                }
                Text("${type.cost}银", color = if (state.silver >= type.cost && !locked) MingColors.GoldDark else MingColors.TextMuted.copy(alpha = 0.55f), fontSize = 15.sp)
                MiniTextButton("新建", enabled = !locked && state.silver >= type.cost) { controller.addIndustry(type.id) }
            }
        }
    }
}

@Composable
private fun CareerPage(state: GameState, controller: GameController) {
    PageHeader("功业", "科举入仕、纳捐授官、军功与历史大势")
    PaperCard {
        Text("仕途概览", color = MingColors.GoldDark, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextChip("读书 ${state.members.count { it.alive && it.state == MemberState.Study }}", true, Modifier.weight(1f))
            TextChip("从军 ${state.members.count { it.alive && it.state == MemberState.Military }}", false, Modifier.weight(1f))
            TextChip("经商 ${state.members.count { it.alive && it.state == MemberState.Trade }}", false, Modifier.weight(1f))
            TextChip("打工 ${state.members.count { it.alive && it.state == MemberState.Labor }}", false, Modifier.weight(1f))
        }
    }
    PaperCard {
        Text("科举取士", color = MingColors.GoldDark, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        GameEngine.aliveMembers(state).filter { it.age >= 12 }.take(6).forEach { member ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("${member.name} · 学识${member.study} · ${member.identity}", color = MingColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("童试 / 乡试 / 会试 / 殿试逐级晋身", color = MingColors.TextMuted, fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    GameContent.examLevels.take(4).forEach { exam ->
                        MiniTextButton(exam.name, enabled = member.study >= exam.reqStudy) { controller.takeExam(member.id, exam.id) }
                    }
                }
            }
            DividerLine()
        }
    }
    PaperCard {
        Text("纳捐与入仕", color = MingColors.GoldDark, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        GameEngine.aliveMembers(state).filter { it.age >= 16 }.take(6).forEach { member ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("${member.name} · ${member.identity}", color = MingColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("望族可纳捐监生；进士可授知县，知县可升知府。", color = MingColors.TextMuted, fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    MiniTextButton("纳捐", enabled = state.clanRank >= 4 && state.silver >= 500 && state.fame >= 15) { controller.donateIdentity(member.id) }
                    GameContent.officialRanks.firstOrNull { it.reqIdentity == member.identity }?.let { rank ->
                        MiniTextButton("任${rank.name}") { controller.appointOfficial(member.id, rank.id) }
                    }
                }
            }
            DividerLine()
        }
    }
    PaperCard {
        Text("历史大势", color = MingColors.GoldDark, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        GameContent.historyEvents.filter { it.year >= state.year }.take(5).forEach { evt ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(evt.title, color = MingColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(evt.year.toString(), color = MingColors.GoldDark, fontSize = 14.sp)
            }
            Text(evt.desc, color = MingColors.TextSecondary, fontSize = 12.sp)
            DividerLine()
        }
    }
}

@Composable
private fun AcademyPage(state: GameState, controller: GameController) {
    SectionTitle("书院", "族学与武馆培养")
    GameContent.academyTypes.forEach { type ->
        val academy = state.academies.firstOrNull { it.typeId == type.id }
        MingCard {
            Text("${type.icon} ${type.name}", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
            Text(type.desc, color = MingColors.TextSecondary, fontSize = 12.sp)
            Text("等级：${academy?.level ?: 0}  席位：${academy?.memberIds?.size ?: 0}/${(academy?.level ?: 0) * type.baseSlotsPerLevel}  解锁品级：${GameContent.rankName(type.unlockRank)}", color = MingColors.TextMuted, fontSize = 12.sp)
            MingButton(if (academy == null) "修建" else "升级", Modifier.fillMaxWidth(), enabled = state.clanRank >= type.unlockRank) { controller.upgradeAcademy(type.id) }
            if (academy != null) {
                GameEngine.aliveMembers(state).filter { it.age >= 6 }.forEach { member ->
                    Text(
                        "${if (academy.memberIds.contains(member.id)) "[在席]" else "[安排]"} ${member.name} 学${member.study} 武${member.martial}",
                        modifier = Modifier.fillMaxWidth().clickable { controller.assignAcademy(type.id, member.id) }.padding(6.dp),
                        color = if (academy.memberIds.contains(member.id)) MingColors.Primary else MingColors.TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpeditionPage(state: GameState, controller: GameController) {
    SectionTitle("历练", "派遣族人外出获取资源、物品和人才")
    if (state.expeditions.isNotEmpty()) {
        MingCard {
            Text("进行中", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
            state.expeditions.forEach { exp ->
                val type = GameContent.expedition(exp.typeId)
                val member = state.members.firstOrNull { it.id == exp.memberId }
                Text("${member?.name ?: "族人"} · ${type?.name ?: exp.typeId} · 剩余${exp.monthsLeft}月", color = MingColors.TextSecondary)
            }
        }
    }
    val idle = GameEngine.adults(state).filter { it.state == MemberState.Home }
    GameContent.expeditionTypes.filter { state.clanRank >= it.minRank }.forEach { type ->
        MingCard {
            Text("${type.icon} ${type.name}", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
            Text(type.desc, color = MingColors.TextSecondary, fontSize = 12.sp)
            Text("耗时${type.duration}月 · 银${type.costSilver} 粮${type.costGrain} · 风险${(type.riskRate * 100).toInt()}%", color = MingColors.TextMuted, fontSize = 12.sp)
            if (idle.isEmpty()) Text("暂无在家成人可派遣", color = MingColors.TextMuted, fontSize = 12.sp)
            idle.take(5).forEach { member ->
                Text(
                    "派遣 ${member.name}（学${member.study} 武${member.martial}）",
                    modifier = Modifier.fillMaxWidth().clickable { controller.startExpedition(type.id, member.id) }.padding(6.dp),
                    color = MingColors.Primary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun InventoryPage(state: GameState, controller: GameController) {
    SectionTitle("库房", "珍贵物品与消耗品")
    if (state.inventory.isEmpty()) EmptyHint("库房暂无物品")
    state.inventory.forEach { inv ->
        val item = GameContent.item(inv.itemId)
        MingCard {
            Text("${item?.icon ?: "物"} ${item?.name ?: inv.itemId} × ${inv.count}", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
            Text(item?.desc ?: "", color = MingColors.TextSecondary, fontSize = 12.sp)
            val firstMember = GameEngine.aliveMembers(state).firstOrNull()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MingButton("使用", Modifier.weight(1f), enabled = firstMember != null) { controller.useItem(inv.itemId, firstMember?.id) }
                MingButton("卖出", Modifier.weight(1f), danger = true) { controller.sellMarketItem(inv.itemId, 1) }
            }
        }
    }
}

@Composable
private fun MarketPage(state: GameState, controller: GameController) {
    SectionTitle("集市", "买卖粮布与常用物品，价格随季节与战乱波动")
    if (state.clanRank < 3) {
        EmptyHint("乡绅品级解锁集市")
        return
    }
    GameContent.marketCommodities.forEach { commodity ->
        val price = state.market.prices[commodity.id] ?: commodity.basePrice
        val have = when (commodity.resourceKey) {
            "grain" -> state.grain
            "cloth" -> state.cloth
            else -> commodity.itemId?.let { id -> state.inventory.firstOrNull { it.itemId == id }?.count } ?: (state.market.stock[commodity.id] ?: 0)
        }
        MingCard {
            Text("${commodity.icon} ${commodity.name} · 单价 $price", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
            Text("${commodity.desc} · 持有：$have", color = MingColors.TextMuted, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MingButton("买1", Modifier.weight(1f), enabled = state.silver >= price) { controller.buyMarketItem(commodity.id, 1) }
                MingButton("卖1", Modifier.weight(1f), danger = true, enabled = have > 0) { controller.sellMarketItem(commodity.id, 1) }
            }
        }
    }
}

@Composable
private fun BattlePage(state: GameState, controller: GameController) {
    SectionTitle("征伐", "战略层征伐、军队训练与军功结算")
    MingCard {
        Text("军队", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        Text("步兵 ${state.army.infantry} · 弓兵 ${state.army.archers} · 训练 ${state.army.trainingLevel}/5", color = MingColors.TextSecondary)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MingButton("募步兵10", Modifier.weight(1f)) { controller.recruitArmy(10, 0) }
            MingButton("募弓兵10", Modifier.weight(1f)) { controller.recruitArmy(0, 10) }
            MingButton("训练", Modifier.weight(1f), enabled = state.army.trainingLevel < 5) { controller.trainArmy() }
        }
    }
    GameContent.campaignStages.forEach { stage ->
        val done = state.conqueredStages.contains(stage.id)
        MingCard {
            Text("${stage.name}${if (done) " · 已征服" else ""}", color = if (done) MingColors.Green else MingColors.GoldDark, fontWeight = FontWeight.Bold)
            Text("敌军战力 ${stage.enemyPower} · 奖励银${stage.rewardSilver} 名${stage.rewardFame}", color = MingColors.TextSecondary, fontSize = 12.sp)
            MingButton("出征", Modifier.fillMaxWidth(), enabled = !done) { controller.attackStage(stage.id) }
        }
    }
}
