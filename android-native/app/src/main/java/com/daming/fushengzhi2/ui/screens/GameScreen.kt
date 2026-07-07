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
                    report.events.take(10).forEach { Text("· $it", fontSize = 13.sp) }
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
    SectionTitle("族谱", "以族人为中心查看宗族传承、联姻和血脉")
    GameEngine.aliveMembers(state).forEach { member ->
        MingCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(member.name, color = MingColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    val spouse = member.spouseId?.let { id -> state.members.firstOrNull { it.id == id }?.name } ?: "未婚"
                    Text("${if (member.gender.name == "Male") "男" else "女"} · ${member.age}岁 · 第${member.generation}代 · ${member.identity}", color = MingColors.TextSecondary, fontSize = 12.sp)
                    Text("配偶：$spouse  子女：${member.childrenIds.size}  天赋：${GameContent.talent(member.talentId)?.name ?: "无"}", color = MingColors.TextMuted, fontSize = 12.sp)
                    Text("状态：${member.state.label}  学识${member.study}/${member.aptitude.study.cap}  武艺${member.martial}/${member.aptitude.martial.cap}  健康${member.health}/${member.aptitude.health.cap}", color = MingColors.TextMuted, fontSize = 12.sp)
                }
                Text(if (member.id == state.patriarchId) "族长" else "族人", color = MingColors.Gold, fontWeight = FontWeight.Bold)
            }
            if (member.spouseId == null && member.age in 16..40) {
                GameContent.marriageTiers.filter { state.clanRank >= it.unlockRank }.take(3).forEach { tier ->
                    Text(
                        "联姻：${tier.name}（银${tier.silverCost} 粮${tier.grainCost} 名望需${tier.fameReq}）",
                        modifier = Modifier.fillMaxWidth().clickable { controller.arrangeMarriage(member.id, tier.id) }.padding(6.dp),
                        color = MingColors.Primary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ClanPage(state: GameState, controller: GameController) {
    SectionTitle("宗族", "宗祠、族规、年度目标、成就和家族志")
    MingCard {
        Text("宗族信息", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        Text("门第：${GameEngine.clanRankName(state)}", color = MingColors.TextPrimary)
        Text("族人：${GameEngine.aliveMembers(state).size} 人，产业：${state.industries.size} 个，寨堡：${state.fortCount} 座", color = MingColors.TextSecondary)
        state.pet?.let { Text("宠物：${it.name}${if (it.alive) "" else "（已故）"}", color = MingColors.TextSecondary) }
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
        Text("年度目标", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        if (state.yearlyGoals.isEmpty()) EmptyHint("暂无年度目标")
        state.yearlyGoals.forEach { goalState ->
            val goal = GameContent.yearlyGoal(goalState.goalId)
            Text("${goal?.icon ?: "目"} ${goal?.name ?: goalState.goalId}：${goal?.desc ?: ""}${if (goalState.completed) " · 已完成" else ""}", color = if (goalState.completed) MingColors.Green else MingColors.TextSecondary, fontSize = 12.sp)
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
        Text("祭祀与宠物", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MingButton("祭天祈福", Modifier.weight(1f), enabled = state.lastSacrificeYear != state.year && state.grain >= 50) { controller.sacrifice() }
            MingButton("收养黄犬", Modifier.weight(1f), enabled = state.pet?.alive != true && state.grain >= 20) { controller.adoptPet("dog") }
        }
    }
    MingCard {
        Text("成就", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        Text("已达成 ${state.unlockedAchievements.size}/${GameContent.achievements.size}", color = MingColors.TextMuted, fontSize = 12.sp)
        state.unlockedAchievements.take(12).forEach { id ->
            val ach = GameContent.achievement(id)
            Text("${ach?.icon ?: "成"} ${ach?.name ?: id}", color = MingColors.Green, fontSize = 12.sp)
        }
    }
    MingCard {
        Text("家族志", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        state.eventLog.take(30).forEach { Text("${it.year}年${it.month}月 · ${it.text}", color = MingColors.TextSecondary, fontSize = 12.sp) }
    }
}

@Composable
private fun MembersPage(state: GameState, controller: GameController) {
    SectionTitle("族人", "安排读书、经商、从军、打工和管理产业")
    GameEngine.aliveMembers(state).forEach { member ->
        MingCard {
            Text(member.name, color = MingColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text("${member.age}岁 · ${member.identity} · 当前：${member.state.label} · 天赋：${GameContent.talent(member.talentId)?.name ?: "无"}", color = MingColors.TextSecondary)
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
    SectionTitle("经营", "产业建造、升级、进化、变卖和管理分配")
    MingCard {
        Text("已有产业", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        if (state.industries.isEmpty()) EmptyHint("暂无产业")
        state.industries.forEach { ind ->
            val type = GameContent.industry(ind.typeId)
            val manager = ind.assignedMemberId?.let { id -> state.members.firstOrNull { it.id == id }?.name } ?: "无"
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("${type?.name ?: ind.typeId} · ${ind.level}级", color = MingColors.TextPrimary, fontWeight = FontWeight.Bold)
                        Text("管理：$manager · ${type?.desc ?: ""}", color = MingColors.TextMuted, fontSize = 12.sp)
                    }
                    MingButton("升级", Modifier.width(76.dp)) { controller.upgradeIndustry(ind.id) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val evo = GameContent.evolution(ind.typeId)
                    MingButton("进化", Modifier.weight(1f), enabled = evo != null && ind.level >= (evo?.reqLevel ?: 99) && state.clanRank >= (evo?.reqRank ?: 99) && state.silver >= (evo?.cost ?: 999999)) { controller.evolveIndustry(ind.id) }
                    MingButton("变卖", Modifier.weight(1f), danger = true) { controller.sellIndustry(ind.id) }
                }
                val assignable = GameEngine.adults(state).take(4)
                if (assignable.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        assignable.forEach { m ->
                            Text(m.name.takeLast(2), modifier = Modifier.background(if (ind.assignedMemberId == m.id) MingColors.Primary else MingColors.BgPanel).clickable { controller.assignIndustry(ind.id, m.id) }.padding(6.dp), color = if (ind.assignedMemberId == m.id) MingColors.BgWhite else MingColors.TextPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
            DividerLine()
        }
    }
    MingCard {
        Text("可建产业", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        Text("上限：${state.industries.size}/${GameContent.industryLimitByRank[state.clanRank] ?: 4}", color = MingColors.TextMuted, fontSize = 12.sp)
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
    SectionTitle("功业", "科举、纳捐、入仕、军功和历史大势")
    MingCard {
        Text("仕途概览", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        Text("读书族人：${state.members.count { it.alive && it.state == MemberState.Study }}", color = MingColors.TextSecondary)
        Text("从军族人：${state.members.count { it.alive && it.state == MemberState.Military }}", color = MingColors.TextSecondary)
        Text("经商族人：${state.members.count { it.alive && it.state == MemberState.Trade }}", color = MingColors.TextSecondary)
        Text("打工族人：${state.members.count { it.alive && it.state == MemberState.Labor }}", color = MingColors.TextSecondary)
    }
    MingCard {
        Text("科举取士", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        GameEngine.aliveMembers(state).filter { it.age >= 12 }.take(8).forEach { member ->
            Text("${member.name} · 学识${member.study} · ${member.identity}", color = MingColors.TextPrimary, fontSize = 13.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GameContent.examLevels.take(4).forEach { exam ->
                    Text(exam.name, modifier = Modifier.background(if (member.study >= exam.reqStudy) MingColors.BgPanel else MingColors.Border).clickable { controller.takeExam(member.id, exam.id) }.padding(6.dp), color = MingColors.Primary, fontSize = 11.sp)
                }
            }
            DividerLine()
        }
    }
    MingCard {
        Text("纳捐与入仕", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        GameEngine.aliveMembers(state).filter { it.age >= 16 }.take(8).forEach { member ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("${member.name} · ${member.identity}", color = MingColors.TextPrimary, fontSize = 13.sp)
                    Text("望族可纳捐监生；进士可授知县，知县可升知府。", color = MingColors.TextMuted, fontSize = 11.sp)
                }
                Column(Modifier.width(110.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MingButton("纳捐", Modifier.fillMaxWidth(), enabled = state.clanRank >= 4 && state.silver >= 500 && state.fame >= 15) { controller.donateIdentity(member.id) }
                    GameContent.officialRanks.firstOrNull { it.reqIdentity == member.identity }?.let { rank ->
                        MingButton("任${rank.name}", Modifier.fillMaxWidth()) { controller.appointOfficial(member.id, rank.id) }
                    }
                }
            }
            DividerLine()
        }
    }
    MingCard {
        Text("历史大势", color = MingColors.GoldDark, fontWeight = FontWeight.Bold)
        GameContent.historyEvents.filter { it.year >= state.year }.take(5).forEach { evt ->
            Text("${evt.year} · ${evt.title}：${evt.desc}", color = MingColors.TextSecondary, fontSize = 12.sp)
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
