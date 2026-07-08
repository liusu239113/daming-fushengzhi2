package com.daming.fushengzhi2.v3.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.daming.fushengzhi2.data.GameImages
import com.daming.fushengzhi2.ui.components.AssetImage
import com.daming.fushengzhi2.v3.data.V3ActiveEvent
import com.daming.fushengzhi2.v3.data.V3AnnualGoal
import com.daming.fushengzhi2.v3.data.V3Content
import com.daming.fushengzhi2.v3.data.V3CountySite
import com.daming.fushengzhi2.v3.data.V3EventChoice
import com.daming.fushengzhi2.v3.data.V3FinalEnding
import com.daming.fushengzhi2.v3.data.V3GameState
import com.daming.fushengzhi2.v3.data.V3Person
import com.daming.fushengzhi2.v3.data.V3Route
import com.daming.fushengzhi2.v3.data.V3Screen
import com.daming.fushengzhi2.v3.data.V3TaskType
import com.daming.fushengzhi2.v3.logic.V3GameController
import com.daming.fushengzhi2.v3.logic.V3GameEngine

private val V3Ink = Color(0xFF1F1712)
private val V3Paper = Color(0xFFF3E2C2)
private val V3PaperDeep = Color(0xFFE1C99E)
private val V3Red = Color(0xFF9A2E24)
private val V3Gold = Color(0xFFC59A45)
private val V3Muted = Color(0xFF8F806A)
private val V3Green = Color(0xFF3E7A4C)
private val V3Blue = Color(0xFF445D7A)

@Composable
fun V3CreateScreen(controller: V3GameController, onBack: () -> Unit, onStart: () -> Unit) {
    LaunchedEffect(Unit) { controller.ensureV3Bgm() }
    var root by remember { mutableStateOf("没落士族") }
    var county by remember { mutableStateOf("江南水乡") }
    var creed by remember { mutableStateOf("耕读传家") }
    var crisis by remember { mutableStateOf("官府催税") }

    V3Background {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            V3Title("大明浮生志3", "明末县域宗族沙盘")
            V3Selector("宗族根基", V3Content.roots, root) { root = it }
            V3Selector("县域位置", V3Content.counties, county) { county = it }
            V3Selector("家族信条", V3Content.creeds, creed) { creed = it }
            V3Selector("初始危机", V3Content.crises, crisis) { crisis = it }
            V3Panel {
                Text("三代上架版已接入专属案牍美术、县域地图、人物成长、房支政治、营建升级、年度目标与正式终局。", color = V3Ink, fontSize = 15.sp)
                Text("开局：$root / $county / $creed / $crisis", color = V3Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                V3Button("返回主菜单", Modifier.weight(1f), onClick = onBack)
                V3Button("立族开局", Modifier.weight(1f)) {
                    controller.newGame(root, county, creed, crisis)
                    onStart()
                }
            }
        }
    }
}

@Composable
fun V3GameScreen(controller: V3GameController, onBackToV2: () -> Unit) {
    LaunchedEffect(Unit) { controller.ensureV3Bgm() }
    val state = controller.state
    V3Background {
        Column(Modifier.fillMaxSize()) {
            V3TopBar(state, controller, onBackToV2)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
                    .widthIn(max = 920.dp)
                    .align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val ending = state.finalEnding
                if (ending != null) {
                    V3EndingPage(ending, controller, onBackToV2)
                } else {
                    when (controller.screen) {
                        V3Screen.County -> V3CountyPage(state, controller)
                        V3Screen.Clan -> V3ClanPage(state)
                        V3Screen.People -> V3PeoplePage(state, controller)
                        V3Screen.Strategy -> V3StrategyPage(state, controller)
                    }
                }
            }
            if (state.finalEnding == null) V3BottomNav(controller)
        }
    }

    val activeEvent = controller.state.activeEvent
    if (controller.state.finalEnding == null && activeEvent != null) {
        V3EventDialog(event = activeEvent, controller = controller)
    } else if (controller.state.finalEnding == null) {
        controller.latestReport?.let { report ->
            V3Dialog(title = report.title, onDismiss = controller::clearReport) {
                report.lines.forEach { Text(it, color = V3Ink, fontSize = 14.sp) }
            }
        }
    }
    controller.message?.let { message ->
        V3Dialog(title = "案牍提示", onDismiss = controller::clearMessage) {
            Text(message, color = V3Ink, fontSize = 15.sp)
        }
    }
}

@Composable
private fun V3EndingPage(ending: V3FinalEnding, controller: V3GameController, onBackToV2: () -> Unit) {
    V3Section("终局家乘", "${ending.route.label} · ${ending.tier.label} · 终局评分 ${ending.score}")
    V3Panel {
        Text(ending.title, color = V3Red, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(ending.body, color = V3Ink, fontSize = 16.sp)
        ending.stats.forEach { stat -> Text(stat, color = V3Muted, fontSize = 14.sp) }
    }
    V3Panel {
        Text("上架版终局说明", color = V3Red, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text("终局会根据路线倾向、地点稳定、资源、关系、族人功绩和房支状态综合评分。", color = V3Ink, fontSize = 14.sp)
        Text("评分等级：根基未稳 / 可成一线 / 大势已成 / 足载家乘。", color = V3Ink, fontSize = 14.sp)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        V3Button("重新开局", Modifier.weight(1f), onClick = controller::restartAfterEnding)
        V3Button("返回主菜单", Modifier.weight(1f), onClick = onBackToV2)
    }
}

@Composable
private fun V3CountyPage(state: V3GameState, controller: V3GameController) {
    V3Section("县域地图", "${state.county} · 当前路线：${V3GameEngine.dominantRoute(state).label}")
    V3Panel {
        Text("旧地图上标记着祠堂、田庄、集市、县衙、寨堡和码头。地点风险会随时间扩散，必须派族人处理。", color = V3Ink, fontSize = 15.sp)
        AssetImage(
            path = GameImages.V3CountyMap,
            contentDescription = "三代县域旧地图",
            modifier = Modifier.fillMaxWidth().height(180.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.92f
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            V3Metric("银", state.silver, V3Gold, Modifier.weight(1f))
            V3Metric("粮", state.grain, V3Green, Modifier.weight(1f))
            V3Metric("望", state.influence, V3Red, Modifier.weight(1f))
            V3Metric("勇", state.militia, V3Blue, Modifier.weight(1f))
        }
    }
    V3Panel {
        Text("年度目标", color = V3Red, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        state.annualGoals.forEach { goal -> V3GoalRow(state, goal) }
    }
    state.sites.forEach { site -> V3SiteCard(site, state, controller) }
    V3Panel {
        Text("任务手册", color = V3Red, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        V3Content.taskPlans.forEach { plan ->
            Text("${plan.task.label} · ${plan.primaryStat} · ${plan.effect}", color = V3Ink, fontSize = 13.sp)
            Text("风险：${plan.risk}    路线：${plan.route.label}", color = V3Muted, fontSize = 12.sp)
        }
    }
    V3Button("推进一月", Modifier.fillMaxWidth(), onClick = controller::advanceMonth)
}

@Composable
private fun V3ClanPage(state: V3GameState) {
    V3Section("宗族", "房支、凝聚、继承与内斗框架")
    V3Panel {
        Text(state.clanName, color = V3Red, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("根基：${state.root}    信条：${state.creed}", color = V3Ink, fontSize = 15.sp)
        Text("凝聚 ${state.cohesion}/100    族内威望 ${state.influence}/100", color = V3Muted, fontSize = 14.sp)
        val angryBranch = state.branches.maxByOrNull { it.grievance }
        if (angryBranch != null && angryBranch.grievance >= 42) {
            Text("议事预警：${angryBranch.name}怨气已达${angryBranch.grievance}，下月可能入祠逼议。", color = V3Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text("宗族内部不再只是人口列表，而是由房支利益、忠诚、财富和怨气共同驱动。", color = V3Ink, fontSize = 13.sp)
    }
    state.branches.forEach { branch ->
        V3Panel {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(branch.name, color = V3Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("支主：${branch.leaderName} · 倾向：${branch.focus.label}", color = V3Red, fontSize = 13.sp)
                }
                Text(if (branch.grievance >= 35) "将争" else if (branch.grievance >= 25) "有怨" else "安定", color = if (branch.grievance >= 25) V3Red else V3Green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Text(branch.desc, color = V3Muted, fontSize = 14.sp)
            Text("房支压力：忠诚越低、怨气越高，越容易触发争产、分家、违令等事件。", color = V3Ink, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                V3Metric("忠", branch.loyalty, V3Green, Modifier.weight(1f))
                V3Metric("财", branch.wealth, V3Gold, Modifier.weight(1f))
                V3Metric("势", branch.influence, V3Blue, Modifier.weight(1f))
                V3Metric("怨", branch.grievance, V3Red, Modifier.weight(1f))
            }
        }
    }
    V3Panel {
        Text("后续要做满的宗族系统", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("1. 族长继承争端：嫡长、贤能、军功、财富四种继承逻辑。", color = V3Ink, fontSize = 13.sp)
        Text("2. 房支议事：每年由各房提出诉求，玩家裁决。", color = V3Ink, fontSize = 13.sp)
        Text("3. 族产分配：田庄、商号、书院、寨堡都将影响房支利益。", color = V3Ink, fontSize = 13.sp)
    }
}

@Composable
private fun V3PeoplePage(state: V3GameState, controller: V3GameController) {
    V3Section("人物派遣", "族人不再只是状态，而是每月任务执行者")
    V3Panel {
        Text("本月派遣", color = V3Red, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        val assignedCount = state.people.count { it.currentTask != null }
        Text("已派遣 $assignedCount / ${state.people.size} 人。月结时每个任务会影响地点控制、风险、资源、关系和路线倾向。", color = V3Ink, fontSize = 14.sp)
    }
    state.people.forEach { person -> V3PersonCard(person, state, controller) }
}

@Composable
private fun V3StrategyPage(state: V3GameState, controller: V3GameController) {
    V3Section("政略", "地方关系、天下大势与多结局路线")
    V3Panel {
        Text("地方势力", color = V3Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        V3RelationRow("官府", state.relations.yamen)
        V3RelationRow("士绅", state.relations.gentry)
        V3RelationRow("乡民", state.relations.villagers)
        V3RelationRow("流寇", state.relations.bandits)
        V3RelationRow("商帮", state.relations.merchants)
        V3RelationRow("军镇", state.relations.garrison)
    }
    V3Panel {
        val ending = V3GameEngine.endingPreview(state)
        Text("结局预判：${ending.title}", color = V3Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("${ending.route.label} · ${ending.tier.label} · 评估 ${ending.score}", color = V3Ink, fontSize = 14.sp)
        Text(ending.desc, color = V3Muted, fontSize = 13.sp)
        V3Button("以当前局势写入家乘终局", Modifier.fillMaxWidth(), onClick = controller::finalizeGame)
    }
    V3Panel {
        Text("当前路线：${V3GameEngine.dominantRoute(state).label}", color = V3Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        V3Content.routePlans.sortedByDescending { state.routeScores[it.route] ?: 0 }.forEach { plan ->
            val score = state.routeScores[plan.route] ?: 0
            Card(
                colors = CardDefaults.cardColors(containerColor = if (plan.route == V3GameEngine.dominantRoute(state)) V3PaperDeep else Color(0x44FFFFFF)),
                border = BorderStroke(1.dp, if (plan.route == V3GameEngine.dominantRoute(state)) V3Red else V3Gold),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(plan.route.label, color = V3Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(score.toString(), color = V3Red, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(plan.goal, color = V3Ink, fontSize = 13.sp)
                    Text("核心：${plan.coreStats.joinToString(" / ")}", color = V3Muted, fontSize = 12.sp)
                    Text("结局：${plan.endingName} · ${plan.unlockHint}", color = V3Muted, fontSize = 12.sp)
                }
            }
        }
    }
    V3Panel {
        Text("事件池种子", color = V3Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        V3Content.eventSeeds.forEach { seed ->
            Text("${seed.title} · ${seed.trigger}", color = V3Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("选择：${seed.choices.joinToString(" / ")}", color = V3Muted, fontSize = 12.sp)
        }
    }
    V3Panel {
        Text("急报与日志", color = V3Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        state.eventLog.take(10).forEach { Text(it, color = V3Ink, fontSize = 13.sp) }
    }
    V3Panel {
        Text("开发路线", color = V3Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("第一阶段：县域地图、人物派遣、房支、关系、事件、独立存档。", color = V3Ink, fontSize = 13.sp)
        Text("第二阶段：补 30+ 地点事件、60+ 人物事件、20+ 房支矛盾事件。", color = V3Ink, fontSize = 13.sp)
        Text("第三阶段：替换三代专属美术、动态音乐、官衙/军报/商路音效。", color = V3Ink, fontSize = 13.sp)
        Text("第四阶段：做满七条结局线，并加入结局统计。", color = V3Ink, fontSize = 13.sp)
    }
    V3Button("查看设计文档位置", Modifier.fillMaxWidth(), onClick = controller::openDesignHint)
}

@Composable
private fun V3SiteCard(site: V3CountySite, state: V3GameState, controller: V3GameController) {
    V3Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(site.name, color = V3Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${site.type.label} · ${site.status.label} · 控制${site.control} / 风险${site.risk}", color = V3Muted, fontSize = 13.sp)
            }
            Text(site.level.takeIf { it > 0 }?.let { "Lv.$it" } ?: "未建", color = V3Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Text(site.desc, color = V3Ink, fontSize = 14.sp)
        val cost = V3GameEngine.upgradeCost(site)
        if (cost != null) {
            Text("营建：银${cost.silver} / 粮${cost.grain} · ${cost.desc}", color = V3Muted, fontSize = 12.sp)
            V3SmallButton(
                "营建升级",
                Modifier.fillMaxWidth(),
                enabled = V3GameEngine.canUpgrade(state, site.id)
            ) { controller.upgradeSite(site.id) }
        } else {
            Text("营建：已达最高等级", color = V3Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        val assigned = site.assignedPersonId?.let { id -> state.people.firstOrNull { it.id == id } }
        Text("当前派遣：${assigned?.name ?: "无人"}", color = if (assigned == null) V3Red else V3Green, fontSize = 13.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            site.taskTypes.take(3).forEach { task ->
                val person = bestPersonFor(state.people, task)
                V3SmallButton(task.label, Modifier.weight(1f), enabled = person != null) {
                    if (person != null) controller.assignTask(person.id, site.id, task)
                }
            }
        }
    }
}

@Composable
private fun V3PersonCard(person: V3Person, state: V3GameState, controller: V3GameController) {
    V3Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(person.name, color = V3Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${person.branch} · ${person.identity} · ${person.age}岁 · ${person.trait.label}", color = V3Red, fontSize = 14.sp)
                Text(person.trait.desc, color = V3Muted, fontSize = 13.sp)
            }
            val assignedSite = person.assignedSiteId?.let { id -> state.sites.firstOrNull { it.id == id } }
            Text(
                if (person.currentTask == null) "待命" else "${person.currentTask.label}@${assignedSite?.name ?: "未知"}",
                color = if (person.currentTask == null) V3Muted else V3Green,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            V3Metric("学", person.study, V3Blue, Modifier.weight(1f))
            V3Metric("武", person.martial, V3Red, Modifier.weight(1f))
            V3Metric("商", person.commerce, V3Gold, Modifier.weight(1f))
            V3Metric("谋", person.diplomacy, V3Green, Modifier.weight(1f))
            V3Metric("忠", person.loyalty, V3Muted, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            V3Metric("绩", person.merit, V3Gold, Modifier.weight(1f))
            V3Metric("劳", person.fatigue, if (person.fatigue >= 60) V3Red else V3Muted, Modifier.weight(1f))
        }
        if (person.fatigue >= 60) {
            Text("疲劳偏高：继续派遣会推动成长，但也会降低后续事件中的稳定性。", color = V3Red, fontSize = 12.sp)
        }
        Text("派遣建议：${recommendedTask(person).label} · ${taskDescription(recommendedTask(person))}", color = V3Ink, fontSize = 13.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            V3TaskType.entries.take(5).forEach { task ->
                val site = state.sites.firstOrNull { it.taskTypes.contains(task) }
                V3SmallButton(task.label, Modifier.weight(1f), enabled = site != null) {
                    if (site != null) controller.assignTask(person.id, site.id, task)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            V3TaskType.entries.drop(5).forEach { task ->
                val site = state.sites.firstOrNull { it.taskTypes.contains(task) }
                V3SmallButton(task.label, Modifier.weight(1f), enabled = site != null) {
                    if (site != null) controller.assignTask(person.id, site.id, task)
                }
            }
        }
    }
}

@Composable
private fun V3TopBar(state: V3GameState, controller: V3GameController, onBackToV2: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xEE1F1712))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("大明浮生志3", color = V3Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("${state.year}年${state.month}月 · ${state.crisis}", color = V3Paper, fontSize = 13.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            V3SmallButton("文档", Modifier.width(76.dp)) { controller.openDesignHint() }
            V3SmallButton("返回2代", Modifier.width(76.dp)) { onBackToV2() }
        }
    }
}

@Composable
private fun V3BottomNav(controller: V3GameController) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xF01F1712))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        V3Screen.entries.forEach { screen ->
            V3SmallButton(
                V3GameEngine.screenTitle(screen),
                Modifier.weight(1f),
                selected = controller.screen == screen
            ) { controller.switchScreen(screen) }
        }
    }
}

@Composable
private fun V3Background(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(V3Ink)
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
                .background(Color(0xAA120D0A))
        )
        content()
    }
}

@Composable
private fun V3Title(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, color = V3Gold, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = V3Paper, fontSize = 16.sp)
    }
}

@Composable
private fun V3Section(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, color = V3Gold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = V3Paper, fontSize = 14.sp)
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(V3Red))
    }
}

@Composable
private fun V3Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = V3Paper),
        border = BorderStroke(1.dp, V3Gold),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun V3Selector(title: String, values: List<String>, selected: String, onSelect: (String) -> Unit) {
    V3Panel {
        Text(title, color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        values.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { value ->
                    V3SmallButton(value, Modifier.weight(1f), selected = value == selected) { onSelect(value) }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun V3Metric(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Color(0x44FFFFFF), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = V3Muted, fontSize = 12.sp)
        Text(value.toString(), color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun V3RelationRow(label: String, value: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = V3Ink, fontSize = 15.sp)
        Text(value.toString(), color = if (value >= 0) V3Green else V3Red, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun V3GoalRow(state: V3GameState, goal: V3AnnualGoal) {
    val progress = V3GameEngine.goalProgress(state, goal)
    val reached = progress >= goal.target || goal.completed
    Column(
        Modifier
            .fillMaxWidth()
            .background(if (reached) V3Green.copy(alpha = 0.12f) else Color(0x44FFFFFF), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(goal.title, color = V3Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(if (reached) "已成" else "${progress}/${goal.target}", color = if (reached) V3Green else V3Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text("${goal.metric.label} · ${goal.route.label}", color = V3Muted, fontSize = 12.sp)
        Text(goal.desc, color = V3Ink, fontSize = 13.sp)
    }
}

@Composable
private fun V3Button(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(
        text,
        modifier = modifier
            .background(V3Red, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        color = V3Paper,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun V3SmallButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, selected: Boolean = false, onClick: () -> Unit) {
    Text(
        text,
        modifier = modifier
            .background(
                when {
                    selected -> V3Red
                    enabled -> Color(0xFFE8D1A6)
                    else -> V3Muted.copy(alpha = 0.4f)
                },
                RoundedCornerShape(7.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        color = if (selected) V3Paper else V3Ink,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun V3EventDialog(event: V3ActiveEvent, controller: V3GameController) {
    Dialog(onDismissRequest = {}) {
        V3Panel(Modifier.widthIn(max = 520.dp)) {
            Text(event.title, color = V3Red, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(event.body, color = V3Ink, fontSize = 15.sp)
            event.choices.forEach { choice ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { controller.chooseEvent(choice) },
                    colors = CardDefaults.cardColors(containerColor = V3PaperDeep),
                    border = BorderStroke(1.dp, V3Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(choice.label, color = V3Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(choice.route.label, color = V3Red, fontSize = 12.sp)
                        }
                        Text(choice.desc, color = V3Muted, fontSize = 13.sp)
                        Text(choiceImpactSummary(choice), color = V3Ink, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun V3Dialog(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        V3Panel(Modifier.widthIn(max = 440.dp)) {
            Text(title, color = V3Red, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            content()
            V3Button("知道了", Modifier.fillMaxWidth(), onClick = onDismiss)
        }
    }
}

private fun choiceImpactSummary(choice: V3EventChoice): String {
    val parts = mutableListOf<String>()
    fun add(label: String, value: Int) {
        if (value > 0) parts += "$label+$value"
        if (value < 0) parts += "$label$value"
    }
    add("银", choice.silverDelta)
    add("粮", choice.grainDelta)
    add("凝", choice.cohesionDelta)
    add("望", choice.influenceDelta)
    add("官", choice.yamenDelta)
    add("绅", choice.gentryDelta)
    add("民", choice.villagersDelta)
    add("寇", choice.banditsDelta)
    add("商", choice.merchantsDelta)
    add("军", choice.garrisonDelta)
    if (choice.routeDelta != 0) parts += "${choice.route.label}+${choice.routeDelta}"
    if (choice.branchImpacts.isNotEmpty()) {
        val branchCount = choice.branchImpacts.size
        val grievance = choice.branchImpacts.sumOf { it.grievanceDelta }
        val influence = choice.branchImpacts.sumOf { it.influenceDelta }
        val branchText = buildString {
            append("房支$branchCount项")
            if (grievance > 0) append(" 怨+$grievance")
            if (grievance < 0) append(" 怨$grievance")
            if (influence > 0) append(" 势+$influence")
            if (influence < 0) append(" 势$influence")
        }
        parts += branchText
    }
    return if (parts.isEmpty()) "后果：局势小幅变化" else "后果：${parts.joinToString(" · ")}"
}

private fun recommendedTask(person: V3Person): V3TaskType = listOf(
    V3TaskType.Study to person.study,
    V3TaskType.Recruit to person.martial,
    V3TaskType.Trade to person.commerce,
    V3TaskType.Diplomacy to person.diplomacy,
    V3TaskType.Govern to ((person.study + person.diplomacy) / 2)
).maxByOrNull { it.second }?.first ?: V3TaskType.Govern

private fun taskDescription(task: V3TaskType): String = when (task) {
    V3TaskType.Govern -> "提升地点控制和宗族凝聚。"
    V3TaskType.Farm -> "增加粮食，压低田庄风险。"
    V3TaskType.Trade -> "获取银两并提升商帮关系。"
    V3TaskType.Study -> "推动耕读路线，为科举做准备。"
    V3TaskType.Diplomacy -> "改善官府与士绅关系。"
    V3TaskType.Relief -> "消耗粮银换取民心与凝聚。"
    V3TaskType.Fortify -> "降低寨堡风险，推动自保路线。"
    V3TaskType.Scout -> "侦察流寇和山道风险。"
    V3TaskType.Recruit -> "增加乡勇，强化军事路线。"
}

private fun bestPersonFor(people: List<V3Person>, task: V3TaskType): V3Person? {
    return people.filter { it.currentTask == null }.maxByOrNull { person ->
        when (task) {
            V3TaskType.Govern -> person.study + person.diplomacy
            V3TaskType.Farm -> person.study + person.commerce
            V3TaskType.Trade -> person.commerce
            V3TaskType.Study -> person.study
            V3TaskType.Diplomacy -> person.diplomacy
            V3TaskType.Relief -> person.study + person.diplomacy
            V3TaskType.Fortify -> person.martial
            V3TaskType.Scout -> person.martial + person.diplomacy
            V3TaskType.Recruit -> person.martial
        }
    }
}
