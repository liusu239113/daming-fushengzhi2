package com.daming.fushengzhi2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daming.fushengzhi2.data.GameContent
import com.daming.fushengzhi2.data.GameState
import com.daming.fushengzhi2.data.GameTab
import com.daming.fushengzhi2.data.MemberState
import com.daming.fushengzhi2.logic.GameController
import com.daming.fushengzhi2.logic.GameEngine
import com.daming.fushengzhi2.ui.components.DividerLine
import com.daming.fushengzhi2.ui.components.EmptyHint
import com.daming.fushengzhi2.ui.components.MingButton
import com.daming.fushengzhi2.ui.components.MingCard
import com.daming.fushengzhi2.ui.components.ResourcePill
import com.daming.fushengzhi2.ui.components.SectionTitle
import com.daming.fushengzhi2.ui.theme.MingColors

@Composable
fun GameScreen(controller: GameController) {
    val state = controller.state ?: return
    Column(
        Modifier
            .fillMaxSize()
            .background(MingColors.BgLight)
    ) {
        TopBar(state, controller)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (controller.tab) {
                GameTab.Tree -> FamilyTreePage(state, controller)
                GameTab.Clan -> ClanPage(state, controller)
                GameTab.Members -> MembersPage(state, controller)
                GameTab.Industry -> IndustryPage(state, controller)
                GameTab.Career -> CareerPage(state, controller)
                GameTab.Academy -> AcademyPage(state, controller)
                GameTab.Expedition -> ExpeditionPage(state, controller)
                GameTab.Inventory -> InventoryPage(state, controller)
                GameTab.Market -> MarketPage(state, controller)
                GameTab.Battle -> BattlePage(state, controller)
            }
        }
        NavigationBar(containerColor = MingColors.BgPanel) {
            GameTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = controller.tab == tab,
                    onClick = { controller.switchTab(tab) },
                    icon = { Text(tab.label.take(1), fontWeight = FontWeight.Bold) },
                    label = { Text(tab.label) }
                )
            }
        }
    }

    controller.latestReport?.let { report ->
        AlertDialog(
            onDismissRequest = controller::clearReport,
            confirmButton = { TextButton(onClick = controller::clearReport) { Text("继续") } },
            title = { Text("${report.year}年${report.month}月结算") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("收入：银${report.incomeSilver} 粮${report.incomeGrain} 布${report.incomeCloth} 名${report.incomeFame}")
                    Text("支出：银${report.expenseSilver} 粮${report.expenseGrain} 布${report.expenseCloth} 名${report.expenseFame}")
                    DividerLine()
                    report.events.take(8).forEach { Text("· $it", fontSize = 13.sp) }
                }
            }
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

@Composable
private fun TopBar(state: GameState, controller: GameController) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MingColors.BgPanel)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(state.clanName, color = MingColors.GoldDark, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("${GameEngine.clanRankName(state)} · ${GameEngine.region(state).name}", color = MingColors.TextMuted, fontSize = 12.sp)
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text("${state.year}年${state.month}月", color = MingColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text("传承${state.totalMonths}月", color = MingColors.TextMuted, fontSize = 12.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            ResourcePill("银", state.silver, MingColors.Silver, Modifier.weight(1f))
            ResourcePill("粮", state.grain, MingColors.Grain, Modifier.weight(1f))
            ResourcePill("布", state.cloth, MingColors.Cloth, Modifier.weight(1f))
            ResourcePill("名", state.fame, MingColors.Fame, Modifier.weight(1f))
            ResourcePill("人", GameEngine.aliveMembers(state).size, MingColors.Green, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MingButton("推进一月", Modifier.weight(1f), onClick = controller::advanceMonth)
            MingButton("手动存档", Modifier.weight(1f), onClick = controller::saveManual)
            MingButton("主菜单", Modifier.weight(1f), danger = true, onClick = controller::backToMenu)
        }
    }
}

@Composable
private fun FamilyTreePage(state: GameState, controller: GameController) {
    SectionTitle("族谱", "以族人为中心查看宗族传承")
    GameEngine.aliveMembers(state).forEach { member ->
        MingCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(member.name, color = MingColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("${if (member.gender.name == "Male") "男" else "女"} · ${member.age}岁 · 第${member.generation}代 · ${member.identity}", color = MingColors.TextSecondary, fontSize = 12.sp)
                    Text("状态：${member.state.label}  学识${member.study}  武艺${member.martial}  健康${member.health}", color = MingColors.TextMuted, fontSize = 12.sp)
                }
                Text(if (member.id == state.patriarchId) "族长" else "族人", color = MingColors.Gold, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ClanPage(state: GameState, controller: GameController) {
    SectionTitle("宗族", "宗祠、族规、家族志")
    MingCard {
        Text("宗族信息", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        Text("门第：${GameEngine.clanRankName(state)}", color = MingColors.TextPrimary)
        Text("族人：${GameEngine.aliveMembers(state).size} 人，产业：${state.industries.size} 个，寨堡：${state.fortCount} 座", color = MingColors.TextSecondary)
        val req = GameContent.rankRequirements[state.clanRank + 1]
        if (req != null) {
            DividerLine()
            Text("晋升 ${GameContent.rankName(state.clanRank + 1)} 条件", color = MingColors.Gold)
            Text("银${state.silver}/${req.silver}  粮${state.grain}/${req.grain}  布${state.cloth}/${req.cloth}  名${state.fame}/${req.fame}  人${GameEngine.aliveMembers(state).size}/${req.population}", color = MingColors.TextMuted, fontSize = 12.sp)
            MingButton("晋升宗族", Modifier.fillMaxWidth(), enabled = GameEngine.canRankUp(state), onClick = controller::rankUp)
        } else {
            Text("已达最高品级。", color = MingColors.Gold)
        }
    }
    MingCard {
        Text("族规", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        Text("当前可启用 ${GameContent.maxRulesByRank[state.clanRank] ?: 0} 条", color = MingColors.TextMuted, fontSize = 12.sp)
        GameContent.clanRules.forEach { rule ->
            val active = state.clanRules.contains(rule.id)
            Card(
                modifier = Modifier.fillMaxWidth().clickable { controller.toggleRule(rule.id) },
                colors = CardDefaults.cardColors(containerColor = if (active) MingColors.BgPanel else MingColors.BgWhite),
                border = BorderStroke(1.dp, if (active) MingColors.Gold else MingColors.Border)
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text("${rule.icon} ${rule.name}${if (active) " · 已启用" else ""}", color = if (active) MingColors.GoldDark else MingColors.TextPrimary, fontWeight = FontWeight.Bold)
                    Text(rule.desc, color = MingColors.TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
    MingCard {
        Text("家族志", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        state.eventLog.take(20).forEach { Text("${it.year}年${it.month}月 · ${it.text}", color = MingColors.TextSecondary, fontSize = 12.sp) }
    }
}

@Composable
private fun MembersPage(state: GameState, controller: GameController) {
    SectionTitle("族人", "安排读书、经商、从军或打工")
    GameEngine.aliveMembers(state).forEach { member ->
        MingCard {
            Text(member.name, color = MingColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text("${member.age}岁 · ${member.identity} · 当前：${member.state.label}", color = MingColors.TextSecondary)
            Text("学识 ${member.study}/${member.aptitude.study.cap}  武艺 ${member.martial}/${member.aptitude.martial.cap}  健康 ${member.health}/${member.aptitude.health.cap}", color = MingColors.TextMuted, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(MemberState.Home, MemberState.Study, MemberState.Trade, MemberState.Military, MemberState.Labor).forEach { target ->
                    Text(
                        target.label,
                        modifier = Modifier
                            .background(if (member.state == target) MingColors.Primary else MingColors.BgPanel)
                            .clickable { controller.setMemberState(member.id, target) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        color = if (member.state == target) MingColors.BgWhite else MingColors.TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun IndustryPage(state: GameState, controller: GameController) {
    SectionTitle("经营", "产业、库房与集市")
    MingCard {
        Text("已有产业", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        if (state.industries.isEmpty()) EmptyHint("暂无产业")
        state.industries.forEach { ind ->
            val type = GameContent.industry(ind.typeId)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("${type?.name ?: ind.typeId} · ${ind.level}级", color = MingColors.TextPrimary, fontWeight = FontWeight.Bold)
                    Text(type?.desc ?: "", color = MingColors.TextMuted, fontSize = 12.sp)
                }
                MingButton("升级", Modifier.width(86.dp)) { controller.upgradeIndustry(ind.id) }
            }
            DividerLine()
        }
    }
    MingCard {
        Text("可建产业", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        GameContent.industryTypes.filter { !it.evolved && state.clanRank >= it.unlockRank }.forEach { type ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("${type.name} · ${type.cost}两", color = MingColors.TextPrimary, fontWeight = FontWeight.Bold)
                    Text("${type.resource.label}+${type.baseOutput}  ${type.desc}", color = MingColors.TextMuted, fontSize = 12.sp)
                }
                MingButton("新建", Modifier.width(86.dp), enabled = state.silver >= type.cost) { controller.addIndustry(type.id) }
            }
            DividerLine()
        }
    }
}

@Composable
private fun CareerPage(state: GameState, controller: GameController) {
    SectionTitle("功业", "仕途、书院、历练、战斗的原生迁移入口")
    MingCard {
        Text("仕途概览", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        Text("读书族人：${state.members.count { it.alive && it.state == MemberState.Study }}", color = MingColors.TextSecondary)
        Text("从军族人：${state.members.count { it.alive && it.state == MemberState.Military }}", color = MingColors.TextSecondary)
        Text("经商族人：${state.members.count { it.alive && it.state == MemberState.Trade }}", color = MingColors.TextSecondary)
        Text("打工族人：${state.members.count { it.alive && it.state == MemberState.Labor }}", color = MingColors.TextSecondary)
    }
    MingCard {
        Text("原 3D 战斗迁移说明", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        Text("原项目 BattleScene.lua 依赖 UrhoX 场景、MDL 模型、材质和 FSM。此 Kotlin 原生版先保留战略入口与资源归档，3D 战斗可后续接入 Filament、SceneView 或其他 Android 3D 渲染方案。", color = MingColors.TextSecondary, fontSize = 13.sp)
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
            if (academy \!= null) {
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MingButton("使用", Modifier.weight(1f)) { controller.useItem(inv.itemId, GameEngine.aliveMembers(state).firstOrNull()?.id) }
                MingButton("卖出", Modifier.weight(1f), danger = true) { controller.sellMarketItem(inv.itemId, 1) }
            }
        }
    }
}

@Composable
private fun MarketPage(state: GameState, controller: GameController) {
    SectionTitle("集市", "买卖粮布与常用物品")
    val goods = listOf("grain", "cloth", "herb", "book", "weapon", "horse")
    goods.forEach { id ->
        val price = state.market.prices[id] ?: 0
        val name = when (id) {
            "grain" -> "粮食"
            "cloth" -> "布匹"
            "horse" -> "马匹"
            else -> GameContent.item(id)?.name ?: id
        }
        val have = when (id) {
            "grain" -> state.grain
            "cloth" -> state.cloth
            else -> state.inventory.firstOrNull { it.itemId == id }?.count ?: 0
        }
        MingCard {
            Text("$name · 单价 $price", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
            Text("持有：$have", color = MingColors.TextMuted, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MingButton("买1", Modifier.weight(1f), enabled = state.silver >= price) { controller.buyMarketItem(id, 1) }
                MingButton("卖1", Modifier.weight(1f), danger = true, enabled = have > 0) { controller.sellMarketItem(id, 1) }
            }
        }
    }
}

@Composable
private fun BattlePage(state: GameState, controller: GameController) {
    SectionTitle("征伐", "原 3D 战斗的战略层原生迁移")
    MingCard {
        Text("军队", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        Text("步兵 ${state.army.infantry} · 弓兵 ${state.army.archers} · 训练 ${state.army.trainingLevel}", color = MingColors.TextSecondary)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MingButton("募步兵10", Modifier.weight(1f)) { controller.recruitArmy(10, 0) }
            MingButton("募弓兵10", Modifier.weight(1f)) { controller.recruitArmy(0, 10) }
        }
    }
    GameContent.campaignStages.forEach { stage ->
        val done = state.conqueredStages.contains(stage.id)
        MingCard {
            Text("${stage.name}${if (done) " · 已征服" else ""}", color = if (done) MingColors.Green else MingColors.GoldDark, fontWeight = FontWeight.Bold)
            Text("敌军战力 ${stage.enemyPower} · 奖励银${stage.rewardSilver} 名${stage.rewardFame}", color = MingColors.TextSecondary, fontSize = 12.sp)
            MingButton("出征", Modifier.fillMaxWidth(), enabled = \!done) { controller.attackStage(stage.id) }
        }
    }
}
