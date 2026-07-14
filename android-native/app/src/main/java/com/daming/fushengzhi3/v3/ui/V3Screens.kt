package com.daming.fushengzhi3.v3.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.daming.fushengzhi3.data.GameImages
import com.daming.fushengzhi3.ui.components.AssetImage
import com.daming.fushengzhi3.ui.theme.FontPreference
import com.daming.fushengzhi3.ui.theme.FontStyleKey
import com.daming.fushengzhi3.v3.data.V3ActiveEvent
import com.daming.fushengzhi3.v3.data.V3AnnualGoal
import com.daming.fushengzhi3.v3.data.V3Content
import com.daming.fushengzhi3.v3.data.V3CountySite
import com.daming.fushengzhi3.v3.data.V3EventChoice
import com.daming.fushengzhi3.v3.data.V3EstateType
import com.daming.fushengzhi3.v3.data.V3FinalEnding
import com.daming.fushengzhi3.v3.data.V3GameState
import com.daming.fushengzhi3.v3.data.V3Gender
import com.daming.fushengzhi3.v3.data.V3Person
import com.daming.fushengzhi3.v3.data.V3Route
import com.daming.fushengzhi3.v3.data.V3RegionStatus
import com.daming.fushengzhi3.v3.data.V3Screen
import com.daming.fushengzhi3.v3.data.V3SiteYield
import com.daming.fushengzhi3.v3.data.V3TaskType
import com.daming.fushengzhi3.v3.data.V3TrainingType
import com.daming.fushengzhi3.v3.data.V3WorldRegion
import com.daming.fushengzhi3.v3.logic.V3GameController
import com.daming.fushengzhi3.v3.logic.V3GameEngine
import kotlinx.coroutines.delay

private val V3Ink = Color(0xFFF4E8C6)
private val V3Paper = Color(0xFF1B1B3A)
private val V3PaperDeep = Color(0xFF252550)
private val V3Red = Color(0xFFB9352B)
private val V3Gold = Color(0xFFE0B85A)
private val V3Muted = Color(0xFFAFA6C8)
private val V3Green = Color(0xFF50C878)
private val V3Blue = Color(0xFF21BDAE)
private val V3Bg = Color(0xFF0F0F23)
private val V3Border = Color(0xFF6F5930)

@Composable
fun V3CreateScreen(controller: V3GameController, onBack: () -> Unit, onStart: () -> Unit) {
    LaunchedEffect(Unit) { controller.ensureV3Bgm() }
    var root by remember { mutableStateOf("没落士族") }
    var county by remember { mutableStateOf("江南水乡") }
    var creed by remember { mutableStateOf("耕读传家") }
    var crisis by remember { mutableStateOf("官府催税") }

    V3Background {
        Column(
            Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            V3Title("大明浮生志3", "一户起家 · 成婚育子 · 经营产业")
            V3Selector("出身", V3Content.roots, root) { root = it }
            V3Selector("县域", V3Content.counties, county) { county = it }
            V3Selector("家训", V3Content.creeds, creed) { creed = it }
            V3Selector("开局危机", V3Content.crises, crisis) { crisis = it }
            V3Panel {
                Text("开局只有家主一人和一处薄田。你的第一步不是管全县，而是成家、置产、养子、把一个小户慢慢经营成宗族。", color = V3Ink, fontSize = 15.sp, lineHeight = 22.sp)
                Text("$root / $county / $creed / $crisis", color = V3Red, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                V3Button("返回", Modifier.weight(1f), onClick = onBack)
                V3Button("一户起家", Modifier.weight(1f)) {
                    controller.newGame(root, county, creed, crisis)
                    onStart()
                }
            }
        }
    }
}

@Composable
fun V3GameScreen(controller: V3GameController, fontPreference: FontPreference, onBackToMenu: () -> Unit) {
    LaunchedEffect(Unit) { controller.ensureV3Bgm() }
    LaunchedEffect(controller.timeSpeed, controller.state.year, controller.state.month, controller.latestReport, controller.message, controller.state.activeEvent, controller.settingsVisible, controller.state.examSession, controller.state.battleState, controller.state.conquestState) {
        if (controller.shouldAutoTick()) {
            val interval = when (controller.timeSpeed) {
                3 -> 850L
                2 -> 1400L
                else -> 2400L
            }
            delay(interval)
            controller.autoAdvanceTime()
        }
    }
    val state = controller.state
    var confirmBackToMenu by remember { mutableStateOf(false) }
    V3Background {
        Column(Modifier.fillMaxSize()) {
            V3TopBar(state, controller, onRequestBackToMenu = { confirmBackToMenu = true })
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(10.dp).widthIn(max = 760.dp).align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val ending = state.finalEnding
                if (ending != null) {
                    V3EndingPage(ending, controller, onBackToMenu)
                } else {
                    when (controller.screen) {
                        V3Screen.County -> V3HomePage(state, controller)
                        V3Screen.Clan -> V3ClanPage(state, controller)
                        V3Screen.People -> V3PeoplePage(state, controller)
                        V3Screen.Strategy -> V3StrategyPage(state, controller)
                    }
                }
            }
            if (state.finalEnding == null) V3BottomNav(controller)
        }
    }

    val activeEvent = controller.state.activeEvent
    if (controller.state.finalEnding == null) {
        controller.latestReport?.let { report ->
            V3Dialog(title = monthlyReportTitle(report.title, report.lines), onDismiss = controller::clearReport) {
                report.lines.forEach { Text("· $it", color = V3Ink, fontSize = 14.sp, lineHeight = 21.sp) }
            }
        } ?: activeEvent?.let { event ->
            V3EventDialog(event = event, controller = controller)
        }
    }
    controller.message?.let { message ->
        V3Dialog(title = "家书提示", onDismiss = controller::clearMessage) {
            Text(message, color = V3Ink, fontSize = 15.sp, lineHeight = 23.sp)
        }
    }
    if (controller.settingsVisible) {
        V3SettingsDialog(controller = controller, fontPreference = fontPreference, onRequestBackToMenu = { confirmBackToMenu = true })
    }
    if (confirmBackToMenu) {
        V3ConfirmBackToMenuDialog(
            onConfirm = {
                confirmBackToMenu = false
                controller.closeSettings()
                onBackToMenu()
            },
            onCancel = { confirmBackToMenu = false }
        )
    }
    controller.state.examSession?.let { session ->
        V3ExamDialog(session = session, controller = controller)
    }
    controller.state.battleState?.let { battle ->
        V3BattleDialog(target = battle.target, enemyPower = battle.enemyPower, risk = battle.risk, controller = controller)
    }
    controller.state.conquestState?.let { conquest ->
        V3ConquestDialog(target = conquest.targetName, enemyPower = conquest.enemyPower, scale = conquest.scale, controller = controller)
    }
}

@Composable
private fun V3HomePage(state: V3GameState, controller: V3GameController) {
    var selectedSiteId by remember { mutableStateOf<String?>(null) }
    val selectedSite = selectedSiteId?.let { id -> state.sites.firstOrNull { it.id == id } }
    val forecast = V3GameEngine.monthlyForecast(state)

    V3Section("家业", nextAdvice(state))
    V3Panel {
        Text("本月账本", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(forecast.summary, color = V3Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            V3Metric("入银", forecast.silverIncome, V3Gold, Modifier.weight(1f))
            V3Metric("出银", forecast.silverExpense, V3Red, Modifier.weight(1f))
            V3Metric("入粮", forecast.grainIncome, V3Green, Modifier.weight(1f))
            V3Metric("出粮", forecast.grainExpense, V3Red, Modifier.weight(1f))
        }
    }
    V3Panel {
        Text("时局脉络", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(mingSituationText(state), color = V3Ink, fontSize = 14.sp, lineHeight = 21.sp)
    }
    V3Panel {
        Text("眼前目标", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        state.annualGoals.take(3).forEach { goal -> V3GoalRow(state, goal) }
    }
    V3CountyMapView(state) { selectedSiteId = it }
    V3EstatePanel(state, controller)
    selectedSite?.let { site ->
        V3SiteManageDialog(site = site, state = state, controller = controller, onDismiss = { selectedSiteId = null })
    }
}

@Composable
private fun V3ClanPage(state: V3GameState, controller: V3GameController) {
    V3Section("宗族", "${V3GameEngine.clanRankName(state)} · 人口 ${V3GameEngine.alivePeople(state).size} · 产业 ${V3GameEngine.builtSiteCount(state)}")
    V3Panel {
        Text(state.clanName, color = V3Red, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("根基：${state.root}    家训：${state.creed}", color = V3Ink, fontSize = 14.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            V3Metric("凝", state.cohesion, V3Green, Modifier.weight(1f))
            V3Metric("望", state.influence, V3Red, Modifier.weight(1f))
            V3Metric("勇", state.militia, V3Blue, Modifier.weight(1f))
            V3Metric("品", state.clanRank, V3Gold, Modifier.weight(1f))
        }
    }
    val options = V3GameEngine.marriageOptions(state)
    if (options.isNotEmpty()) {
        V3Panel {
            Text("婚配", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("娶妻后才会有添丁机会，这是家族模拟的第一步。", color = V3Ink, fontSize = 13.sp)
            options.take(3).forEach { option ->
                Card(colors = CardDefaults.cardColors(containerColor = V3PaperDeep), border = BorderStroke(2.dp, V3Gold), shape = RoundedCornerShape(0.dp)) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(option.name, color = V3Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("银${option.silverCost}/粮${option.grainCost}", color = V3Red, fontSize = 12.sp)
                        }
                        Text(option.desc, color = V3Muted, fontSize = 13.sp)
                        V3SmallButton("迎娶", Modifier.fillMaxWidth(), enabled = V3GameEngine.canMarry(state, option.id)) { controller.marry(option.id) }
                    }
                }
            }
        }
    }
    V3Panel {
        Text("宗族晋升", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        val cost = V3GameEngine.nextRankCost(state)
        if (cost == null) {
            Text("已达最高品第。", color = V3Green, fontSize = 14.sp)
        } else {
            Text("下一品第：${cost.title}", color = V3Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("需要：银${cost.silver} / 粮${cost.grain} / 人口${cost.population} / 产业${cost.builtSites} / 族望${cost.influence}", color = V3Muted, fontSize = 13.sp)
            Text("当前：银${state.silver} / 粮${state.grain} / 人口${V3GameEngine.alivePeople(state).size} / 产业${V3GameEngine.builtSiteCount(state)} / 族望${state.influence}", color = V3Ink, fontSize = 13.sp)
            V3SmallButton("晋升宗族", Modifier.fillMaxWidth(), enabled = V3GameEngine.canRankUp(state), onClick = controller::rankUp)
        }
    }
    V3Panel {
        Text("房支", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        state.branches.take(3).forEach { branch ->
            Text("${branch.name}：忠${branch.loyalty} 财${branch.wealth} 势${branch.influence} 怨${branch.grievance}", color = V3Ink, fontSize = 13.sp)
            Text(branch.desc, color = V3Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun V3PeoplePage(state: V3GameState, controller: V3GameController) {
    val people = V3GameEngine.alivePeople(state)
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    val person = selectedPersonId?.let { id -> people.firstOrNull { it.id == id } }
    V3Section("族谱", "可拖动查看大族树状图；点小卡片弹出族人详情与培养安排。")
    V3GenealogyTree(people, onSelect = { selectedPersonId = it })
    person?.let { selected ->
        V3PersonDetailDialog(person = selected, state = state, controller = controller, onDismiss = { selectedPersonId = null })
    }
}

@Composable
private fun V3GenealogyTree(people: List<V3Person>, onSelect: (Int) -> Unit) {
    var pan by remember { mutableStateOf(Offset.Zero) }
    val roots = people.filter { it.parentId == null }.ifEmpty { people.take(1) }
    val generations = people.groupBy { it.generation }.toSortedMap()
    V3Panel {
        Text("李氏族谱树", color = V3Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("族人多时拖动画布查看；节点只保留头像、姓名、年龄性别，详情点开弹窗。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
        Box(Modifier.fillMaxWidth().height(430.dp).background(V3PaperDeep, RoundedCornerShape(0.dp)).pointerInput(Unit) {
            detectDragGestures { _, dragAmount -> pan = Offset((pan.x + dragAmount.x).coerceIn(-1400f, 180f), (pan.y + dragAmount.y).coerceIn(-1400f, 180f)) }
        }) {
            Box(Modifier.size(width = 1500.dp, height = 1500.dp).graphicsLayer { translationX = pan.x; translationY = pan.y }) {
                roots.take(12).forEachIndexed { index, person ->
                    V3FamilyMiniNode(person, x = 640 + index * 104, y = 28, onSelect = onSelect)
                }
                generations.filterKeys { it > 1 }.forEach { (gen, members) ->
                    val y = 28 + (gen - 1) * 138
                    members.take(80).forEachIndexed { index, person ->
                        val x = 80 + (index % 12) * 112
                        val row = index / 12
                        V3FamilyMiniNode(person, x = x, y = y + row * 122, onSelect = onSelect)
                    }
                }
            }
        }
    }
}

@Composable
private fun V3FamilyMiniNode(person: V3Person, x: Int, y: Int, onSelect: (Int) -> Unit) {
    Column(
        Modifier.graphicsLayer { translationX = x.toFloat(); translationY = y.toFloat() }.width(96.dp).background(V3Paper, RoundedCornerShape(0.dp)).clickable { onSelect(person.id) }.padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(if (person.gender == V3Gender.Male) "男" else "女", color = V3Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(person.name, color = V3Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text("${person.age}岁 · ${person.gender.label}", color = V3Muted, fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
private fun V3PersonDetailDialog(person: V3Person, state: V3GameState, controller: V3GameController, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        V3ImagePanel(GameImages.V3UiEventPanel, Modifier.widthIn(max = 480.dp)) {
            V3PersonCard(person, state, controller)
            V3SmallButton("关闭", Modifier.fillMaxWidth(), selected = true, onClick = onDismiss)
        }
    }
}

@Composable
private fun V3StrategyPage(state: V3GameState, controller: V3GameController) {
    val ending = V3GameEngine.endingPreview(state)
    V3Section("大势", "当前路线：${V3GameEngine.dominantRoute(state).label}")
    V3Panel {
        Text("路线评估：${ending.title}", color = V3Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("${ending.route.label} · ${ending.tier.label} · 评估 ${ending.score}", color = V3Ink, fontSize = 14.sp)
        Text(ending.desc, color = V3Muted, fontSize = 13.sp)
        Text("这里只保留局势预览，不再提供手动写入终局。真正结局会在家族败落、资源崩盘或历史终点时自然触发。", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
    }
    V3Panel {
        Text("地方关系", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        V3RelationRow("官府", state.relations.yamen)
        V3RelationRow("士绅", state.relations.gentry)
        V3RelationRow("乡民", state.relations.villagers)
        V3RelationRow("流寇", state.relations.bandits)
        V3RelationRow("商帮", state.relations.merchants)
        V3RelationRow("军镇", state.relations.garrison)
    }
    V3Panel {
        Text("路线", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        V3Content.routePlans.sortedByDescending { state.routeScores[it.route] ?: 0 }.take(4).forEach { plan ->
            Text("${plan.route.label} ${state.routeScores[plan.route] ?: 0} · ${plan.goal}", color = if (plan.route == ending.route) V3Red else V3Ink, fontSize = 13.sp, fontWeight = if (plan.route == ending.route) FontWeight.Bold else FontWeight.Normal)
        }
    }
    V3WorldPanel(state, controller)
    V3Panel {
        Text("近事", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        state.eventLog.take(5).forEach { Text("· $it", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp) }
    }
    V3Panel {
        Text("军务与举旗", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("乡勇 ${state.militia} · 举旗热度 ${state.rebelHeat}。战斗会根据乡勇、武艺最高族人、寨堡等级和军镇关系结算。", color = V3Ink, fontSize = 13.sp, lineHeight = 19.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            V3SmallButton("讨伐流寇", Modifier.weight(1f), enabled = state.battleState == null) { controller.startBattle() }
            V3SmallButton("举旗造反", Modifier.weight(1f)) { controller.raiseBanner() }
        }
    }
    V3Button("设置", Modifier.fillMaxWidth(), onClick = controller::openSettings)
    V3Button("玩法说明", Modifier.fillMaxWidth(), onClick = controller::openPlayGuide)
}

@Composable
private fun V3WorldPanel(state: V3GameState, controller: V3GameController) {
    var selectedRegionId by remember { mutableStateOf<String?>(null) }
    val selectedRegion = selectedRegionId?.let { id -> state.worldRegions.firstOrNull { it.id == id } }
    V3Panel {
        Text("天下地图", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("统一进度 ${state.unificationProgress} · 控制地域 ${V3GameEngine.controlledRegionCount(state)}/${state.worldRegions.size}。点地图节点弹出结交、经营、征伐。", color = V3Ink, fontSize = 13.sp, lineHeight = 19.sp)
        V3WorldVisualMap(state) { selectedRegionId = it }
        V3SmallButton("统一天下", Modifier.fillMaxWidth()) { controller.proclaimUnification() }
    }
    selectedRegion?.let { region ->
        V3RegionManageDialog(region = region, state = state, controller = controller, onDismiss = { selectedRegionId = null })
    }
}

@Composable
private fun V3RegionManageDialog(region: V3WorldRegion, state: V3GameState, controller: V3GameController, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        V3ImagePanel(GameImages.V3UiBattleReport, Modifier.widthIn(max = 470.dp)) {
            Text("${region.name} · ${region.status.label}", color = V3Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text("控制 ${region.control} · 敌势 ${region.enemyPower} · 财富 ${region.wealth}", color = V3Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(region.desc, color = V3Muted, fontSize = 13.sp, lineHeight = 20.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                V3SmallButton("结交", Modifier.weight(1f), enabled = region.status == V3RegionStatus.Unknown) { controller.contactRegion(region.id) }
                V3SmallButton("经营", Modifier.weight(1f)) { controller.influenceRegion(region.id) }
                V3SmallButton("征伐", Modifier.weight(1f), enabled = state.conquestState == null) { controller.startConquest(region.id) }
            }
            V3SmallButton("关闭", Modifier.fillMaxWidth(), selected = true, onClick = onDismiss)
        }
    }
}

@Composable
private fun V3WorldVisualMap(state: V3GameState, onSelect: (String) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth().height(360.dp).background(V3PaperDeep, RoundedCornerShape(0.dp)).clip(RoundedCornerShape(0.dp))) {
        val worldWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val worldHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
        AssetImage(GameImages.V3CountyMap, null, Modifier.matchParentSize(), ContentScale.Crop, alpha = 0.82f)
        Canvas(Modifier.matchParentSize()) {
            drawLine(V3Gold.copy(alpha = 0.55f), Offset(size.width * 0.18f, size.height * 0.70f), Offset(size.width * 0.42f, size.height * 0.52f), strokeWidth = 4f, cap = StrokeCap.Square)
            drawLine(V3Gold.copy(alpha = 0.55f), Offset(size.width * 0.42f, size.height * 0.52f), Offset(size.width * 0.66f, size.height * 0.38f), strokeWidth = 4f, cap = StrokeCap.Square)
            drawLine(V3Gold.copy(alpha = 0.55f), Offset(size.width * 0.66f, size.height * 0.38f), Offset(size.width * 0.78f, size.height * 0.18f), strokeWidth = 4f, cap = StrokeCap.Square)
            drawLine(V3Gold.copy(alpha = 0.55f), Offset(size.width * 0.42f, size.height * 0.52f), Offset(size.width * 0.28f, size.height * 0.26f), strokeWidth = 4f, cap = StrokeCap.Square)
            drawLine(V3Gold.copy(alpha = 0.55f), Offset(size.width * 0.78f, size.height * 0.18f), Offset(size.width * 0.50f, size.height * 0.10f), strokeWidth = 4f, cap = StrokeCap.Square)
        }
        state.worldRegions.forEach { region ->
            V3WorldRegionPin(region, worldWidthPx = worldWidthPx, worldHeightPx = worldHeightPx, onClick = { onSelect(region.id) })
        }
    }
}

@Composable
private fun V3WorldRegionPin(region: V3WorldRegion, worldWidthPx: Float, worldHeightPx: Float, onClick: () -> Unit) {
    val point = worldMapPoint(region.id)
    val icon = GameImages.v3WorldRegionIcons[region.id]
    val density = LocalDensity.current
    val pinWidthPx = with(density) { 108.dp.toPx() }
    val pinHeightPx = with(density) { 118.dp.toPx() }
    val x = (worldWidthPx * point.x - pinWidthPx * 0.5f).coerceIn(4f, worldWidthPx - pinWidthPx - 4f)
    val y = (worldHeightPx * point.y - pinHeightPx * 0.45f).coerceIn(4f, worldHeightPx - pinHeightPx - 4f)
    val color = when (region.status) {
        V3RegionStatus.Unknown -> V3Muted
        V3RegionStatus.Contacted -> V3Blue
        V3RegionStatus.Influenced -> V3Gold
        V3RegionStatus.Controlled -> V3Green
        V3RegionStatus.Pacified -> V3Red
    }
    Column(
        Modifier.graphicsLayer { translationX = x; translationY = y }.width(108.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(Modifier.size(72.dp).background(V3Paper.copy(alpha = 0.24f), RoundedCornerShape(0.dp)), contentAlignment = Alignment.Center) {
            if (icon != null) {
                AssetImage(icon, region.name, Modifier.size(68.dp), ContentScale.Fit)
            }
        }
        Column(
            Modifier.fillMaxWidth().background(V3Paper.copy(alpha = 0.92f), RoundedCornerShape(0.dp)).padding(horizontal = 4.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(region.name, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
            Text(region.status.label, color = V3Ink, fontSize = 9.sp, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

private fun worldMapPoint(regionId: String): Offset = when (regionId) {
    "home_county" -> Offset(0.18f, 0.68f)
    "river_prefecture" -> Offset(0.42f, 0.52f)
    "mountain_prefecture" -> Offset(0.28f, 0.25f)
    "south_province" -> Offset(0.66f, 0.38f)
    "north_capital" -> Offset(0.78f, 0.16f)
    "all_realm" -> Offset(0.50f, 0.08f)
    else -> Offset(0.50f, 0.50f)
}

@Composable
private fun V3CountyMapView(state: V3GameState, onSelectSite: (String) -> Unit) {
    var pan by remember { mutableStateOf(Offset.Zero) }
    val frameHeight = 560.dp
    val frameShape = RoundedCornerShape(0.dp)
    val density = LocalDensity.current
    V3Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("县域地图", color = V3Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("点建筑弹出管理", color = V3Muted, fontSize = 12.sp)
        }
        BoxWithConstraints(
            Modifier.fillMaxWidth().height(frameHeight).clip(frameShape).background(V3PaperDeep, frameShape)
        ) {
            val frameWidthPx = with(density) { maxWidth.toPx() }
            val frameHeightPx = with(density) { maxHeight.toPx() }
            val mapWidthPx = frameWidthPx
            val mapHeightPx = maxOf(frameHeightPx + 1f, frameWidthPx * 1180f / 760f)
            val mapHeight = with(density) { mapHeightPx.toDp() }
            val minPanY = (frameHeightPx - mapHeightPx).coerceAtMost(0f)
            val boundedPan = Offset(0f, pan.y.coerceIn(minPanY, 0f))
            Box(
                Modifier.fillMaxSize().pointerInput(minPanY) {
                    detectDragGestures { _, dragAmount ->
                        pan = Offset(0f, (pan.y + dragAmount.y).coerceIn(minPanY, 0f))
                    }
                }
            ) {
                Box(Modifier.fillMaxWidth().height(mapHeight).graphicsLayer { translationX = boundedPan.x; translationY = boundedPan.y }) {
                    AssetImage(GameImages.V3MapBgPlain, null, Modifier.fillMaxSize(), ContentScale.Crop)
                    state.sites.forEach { site ->
                        V3MapSitePin(site, mapWidthPx = mapWidthPx, mapHeightPx = mapHeightPx) { onSelectSite(site.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun V3MapSitePin(site: V3CountySite, mapWidthPx: Float, mapHeightPx: Float, onClick: () -> Unit) {
    val point = siteMapPoint(site.id)
    val icon = GameImages.v3SiteIcons[site.id] ?: return
    val density = LocalDensity.current
    val pinWidthPx = with(density) { 92.dp.toPx() }
    val pinHeightPx = with(density) { 110.dp.toPx() }
    val safeMarginPx = with(density) { 10.dp.toPx() }
    val x = (mapWidthPx * point.x - pinWidthPx * 0.5f).coerceIn(safeMarginPx, mapWidthPx - pinWidthPx - safeMarginPx)
    val y = (mapHeightPx * point.y - pinHeightPx * 0.35f).coerceIn(safeMarginPx, mapHeightPx - pinHeightPx - safeMarginPx)
    Column(
        Modifier.graphicsLayer {
            translationX = x
            translationY = y
        }.width(92.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(Modifier.size(66.dp).background(V3PaperDeep.copy(alpha = 0.38f), RoundedCornerShape(0.dp)), contentAlignment = Alignment.Center) {
            AssetImage(icon, site.name, Modifier.size(58.dp), ContentScale.Fit)
        }
        Text(site.name, color = V3Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.background(V3PaperDeep, RoundedCornerShape(0.dp)).padding(horizontal = 5.dp, vertical = 3.dp))
    }
}

private fun siteMapPoint(siteId: String): Offset = when (siteId) {
    "fort" -> Offset(0.25f, 0.14f)
    "yamen" -> Offset(0.54f, 0.20f)
    "academy" -> Offset(0.76f, 0.32f)
    "shrine" -> Offset(0.40f, 0.43f)
    "farmland" -> Offset(0.22f, 0.54f)
    "market" -> Offset(0.64f, 0.62f)
    "clinic" -> Offset(0.36f, 0.74f)
    "dock" -> Offset(0.72f, 0.84f)
    "mountain_pass" -> Offset(0.24f, 0.92f)
    else -> Offset(0.50f, 0.50f)
}

@Composable
private fun V3SiteManageDialog(site: V3CountySite, state: V3GameState, controller: V3GameController, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        V3ImagePanel(GameImages.V3UiEventPanel, Modifier.widthIn(max = 470.dp)) {
            Text("${site.name} 管理", color = V3Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            V3SiteCard(site, state, controller)
            V3SmallButton("关闭", Modifier.fillMaxWidth(), selected = true, onClick = onDismiss)
        }
    }
}

@Composable
private fun V3SiteCard(site: V3CountySite, state: V3GameState, controller: V3GameController) {
    val yield = V3GameEngine.siteYield(site)
    V3Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(site.name, color = V3Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${site.type.label} · ${site.status.label} · 控${site.control} / 险${site.risk}", color = V3Muted, fontSize = 13.sp)
            }
            Text(site.level.takeIf { it > 0 }?.let { "Lv.$it" } ?: "未建", color = V3Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Text(site.desc, color = V3Ink, fontSize = 14.sp, lineHeight = 21.sp)
        Text("月产：${siteYieldSummary(yield)} · ${yield.desc}", color = if (site.level > 0) V3Green else V3Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        val cost = V3GameEngine.upgradeCost(site)
        if (cost != null) {
            Text("营建：银${cost.silver} / 粮${cost.grain} · ${cost.desc}", color = V3Muted, fontSize = 12.sp)
            V3SmallButton("营建/升级", Modifier.fillMaxWidth(), enabled = V3GameEngine.canUpgrade(state, site.id)) { controller.upgradeSite(site.id) }
        } else {
            Text("营建：已达最高等级", color = V3Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        val assigned = site.assignedPersonId?.let { id -> state.people.firstOrNull { it.id == id } }
        Text("本月派遣：${assigned?.name ?: "无人"}", color = if (assigned == null) V3Muted else V3Green, fontSize = 13.sp)
        Text("经营动作已统一放到【族人】页：先选族人，再决定去哪个地点做事，避免同一件事在多个界面重复操作。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
        site.taskTypes.take(3).forEach { task ->
            val person = bestPersonFor(state.people, task)
            val suggestion = if (person == null) "暂无合适族人" else "建议 ${person.name} · ${task.label} · ${V3GameEngine.assignmentPreview(person, site, task)}"
            Text("· $suggestion", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun V3EstatePanel(state: V3GameState, controller: V3GameController) {
    V3Panel {
        Text("家产管理", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("家产是从小户到大族的底盘：田产养人，铺面生银，团练营支撑征伐。", color = V3Ink, fontSize = 13.sp, lineHeight = 19.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            V3Metric("家产", V3GameEngine.estateLevelTotal(state), V3Gold, Modifier.weight(1f))
            V3Metric("地域", V3GameEngine.controlledRegionCount(state), V3Blue, Modifier.weight(1f))
            V3Metric("统一", state.unificationProgress, V3Red, Modifier.weight(1f))
        }
        V3EstateType.entries.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                row.forEach { type ->
                    val asset = state.estateAssets.firstOrNull { it.type == type }
                    val cost = V3GameEngine.estateUpgradeCost(state, type)
                    val yield = asset?.let { V3GameEngine.estateYield(it) }
                    V3SmallButton(
                        "${type.label}${asset?.level?.let { " Lv.$it" } ?: ""}\n银${cost.silver}/粮${cost.grain}${yield?.let { " · ${siteYieldSummary(it)}" } ?: ""}",
                        Modifier.weight(1f)
                    ) { controller.upgradeEstate(type) }
                }
                repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun V3PersonCard(person: V3Person, state: V3GameState, controller: V3GameController) {
    V3Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(person.name, color = V3Ink, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("${person.gender.label} · ${person.branch} · ${person.identity} · ${person.age}岁 · ${person.trait.label}", color = V3Red, fontSize = 13.sp)
                Text(person.trait.desc, color = V3Muted, fontSize = 12.sp)
            }
            val assignedSite = person.assignedSiteId?.let { id -> state.sites.firstOrNull { it.id == id } }
            val titleBits = listOfNotNull(person.officeRank, person.militaryRank).joinToString(" · ")
            Text(if (person.currentTask == null && person.trainingFocus == null) "待命${if (titleBits.isBlank()) "" else " · $titleBits"}" else "${person.currentTask?.label ?: person.trainingFocus?.label}@${assignedSite?.name ?: "家中"}", color = if (person.currentTask == null && person.trainingFocus == null) V3Muted else V3Green, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            V3Metric("学", person.study, V3Blue, Modifier.weight(1f))
            V3Metric("武", person.martial, V3Red, Modifier.weight(1f))
            V3Metric("商", person.commerce, V3Gold, Modifier.weight(1f))
            V3Metric("谋", person.diplomacy, V3Green, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            V3Metric("忠", person.loyalty, V3Muted, Modifier.weight(1f))
            V3Metric("绩", person.merit, V3Gold, Modifier.weight(1f))
            V3Metric("劳", person.fatigue, if (person.fatigue >= 60) V3Red else V3Muted, Modifier.weight(1f))
        }
        Text("培养", color = V3Red, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        V3TrainingButtons(person, controller)
        if (person.age < 12) {
            Text("尚年幼，不能外出办事，但可以每月培养。儿童培养成长更快。", color = V3Ink, fontSize = 13.sp)
        } else {
            Text("建议：${recommendedTask(person).label} · ${taskDescription(recommendedTask(person))}", color = V3Ink, fontSize = 13.sp)
            V3TaskButtons(person, state, controller)
            val stage = V3GameEngine.nextExamStage(person)
            val examLabel = stage?.let { "参加${it.label}" } ?: "科举已达当前上限"
            V3SmallButton(examLabel, Modifier.fillMaxWidth(), enabled = stage != null && V3GameEngine.canStartExam(state, person)) { controller.startExam(person.id) }
        }
    }
}

@Composable
private fun V3TrainingButtons(person: V3Person, controller: V3GameController) {
    V3TrainingType.entries.chunked(2).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row.forEach { training ->
                V3SmallButton(training.label, Modifier.weight(1f), enabled = person.currentTask == null && person.trainingFocus == null) {
                    controller.trainPerson(person.id, training)
                }
            }
            repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun V3TaskButtons(person: V3Person, state: V3GameState, controller: V3GameController) {
    val tasks = V3TaskType.entries
    tasks.chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row.forEach { task ->
                val site = targetSiteFor(state, task)
                V3SmallButton(task.label, Modifier.weight(1f), enabled = site != null && person.currentTask == null && person.trainingFocus == null) {
                    if (site != null) controller.assignTask(person.id, site.id, task)
                }
            }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun V3EndingPage(ending: V3FinalEnding, controller: V3GameController, onBackToMenu: () -> Unit) {
    V3Section("终局家乘", "${ending.route.label} · ${ending.tier.label} · 终局评分 ${ending.score}")
    V3Panel {
        Text(ending.title, color = V3Red, fontSize = 27.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(ending.body, color = V3Ink, fontSize = 15.sp, lineHeight = 23.sp)
        ending.stats.forEach { stat -> Text("· $stat", color = V3Muted, fontSize = 13.sp) }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        V3Button("重新开局", Modifier.weight(1f), onClick = controller::restartAfterEnding)
        V3Button("主菜单", Modifier.weight(1f), onClick = onBackToMenu)
    }
}

@Composable
private fun V3TopBar(state: V3GameState, controller: V3GameController, onRequestBackToMenu: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(Color(0xEE1F1712)).padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(state.clanName, color = V3Gold, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("${mingEraLabel(state.year)}${state.month}月 · ${V3GameEngine.clanRankName(state)} · ${state.crisis}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                V3SmallButton("设置", Modifier.width(66.dp)) { controller.openSettings() }
                V3SmallButton("菜单", Modifier.width(66.dp)) { onRequestBackToMenu() }
            }
        }
        V3TimeControls(controller)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            V3ResourceMetric(GameImages.V3IconSilver, "银两", state.silver, V3Gold, Modifier.weight(1f))
            V3ResourceMetric(GameImages.V3IconGrain, "粮食", state.grain, V3Green, Modifier.weight(1f))
            V3ResourceMetric(GameImages.V3IconPopulation, "人口", V3GameEngine.alivePeople(state).size, V3Blue, Modifier.weight(1f))
            V3ResourceMetric(GameImages.V3IconIndustry, "产业", V3GameEngine.builtSiteCount(state), V3Red, Modifier.weight(1f))
        }
    }
}

@Composable
private fun V3TimeControls(controller: V3GameController) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        V3SmallButton(if (controller.timeSpeed == 0) "继续" else "暂停", Modifier.weight(1f), selected = controller.timeSpeed == 0) { controller.togglePause() }
        listOf(1, 2, 3).forEach { speed ->
            V3SmallButton("${speed}倍", Modifier.weight(1f), selected = controller.timeSpeed == speed) { controller.updateTimeSpeed(speed) }
        }
        Text(if (controller.shouldAutoTick()) "时序流动中" else "等待抉择", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1.4f))
    }
}

private fun mingEraLabel(year: Int): String = when {
    year <= 1619 -> "万历${year - 1572}年 · ${year}年"
    year <= 1627 -> "天启${year - 1620}年 · ${year}年"
    else -> "崇祯${year - 1627}年 · ${year}年"
}

private fun mingSituationText(state: V3GameState): String = when {
    state.year < 1619 -> "万历末年，矿税、徭役和地方积弊仍在。李氏先要在清河县稳住婚配、田粮、祠产和县衙关系。"
    state.year < 1628 -> "辽事已急，天启年间党争、边饷、军镇催粮渐入县中。宗族不能只看家账，还要决定勤王、自保或通商。"
    state.year < 1636 -> "崇祯新政催饷更紧，饥荒与流民开始外溢。李氏若无粮、无勇、无族望，乱世会自然吞没家业。"
    state.year < 1642 -> "关外势大，流寇四起，地方豪族各谋退路。此时应在书院、商路、寨堡、码头之间定下真正路线。"
    else -> "甲申将近，天下土崩。李氏必须在勤王、割据、保族、南迁之间作终局选择。"
}

@Composable
private fun V3BottomNav(controller: V3GameController) {
    Row(Modifier.fillMaxWidth().background(Color(0xF01F1712)).padding(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        V3Screen.entries.forEach { screen ->
            V3SmallButton(V3GameEngine.screenTitle(screen), Modifier.weight(1f), selected = controller.screen == screen) { controller.switchScreen(screen) }
        }
    }
}

@Composable
private fun V3Background(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(V3Bg)) {
        AssetImage(GameImages.V3DossierBg, null, Modifier.fillMaxSize(), ContentScale.Crop, alpha = 0.20f)
        Box(Modifier.fillMaxSize().background(Color(0xE00B0B18)))
        content()
    }
}

@Composable
private fun V3Title(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, color = V3Gold, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color.White, fontSize = 15.sp)
    }
}

@Composable
private fun V3Section(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, color = V3Gold, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color.White, fontSize = 13.sp, lineHeight = 19.sp)
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(V3Red))
    }
}

@Composable
private fun V3Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = V3Paper), border = BorderStroke(2.dp, V3Border), shape = RoundedCornerShape(0.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun V3ImagePanel(imagePath: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = V3Paper), border = BorderStroke(2.dp, V3Gold), shape = RoundedCornerShape(0.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp), content = content)
    }
}

@Composable
private fun V3Selector(title: String, values: List<String>, selected: String, onSelect: (String) -> Unit) {
    V3Panel {
        Text(title, color = V3Red, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        values.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                row.forEach { value -> V3SmallButton(value, Modifier.weight(1f), selected = value == selected) { onSelect(value) } }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun V3SelectorChips(title: String, values: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    V3Panel {
        Text(title, color = V3Red, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        values.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                row.forEach { (id, label) -> V3SmallButton(label, Modifier.weight(1f), selected = id == selected) { onSelect(id) } }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun V3Metric(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Column(modifier.background(V3PaperDeep, RoundedCornerShape(0.dp)).padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = V3Muted, fontSize = 11.sp)
        Text(value.toString(), color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun V3ResourceMetric(iconPath: String, label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.background(V3PaperDeep, RoundedCornerShape(0.dp)).padding(horizontal = 4.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        AssetImage(iconPath, label, Modifier.size(26.dp), ContentScale.Fit)
        Text(value.toString(), color = color, fontSize = 19.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(label, color = V3Muted, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun V3RelationRow(label: String, value: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = V3Ink, fontSize = 14.sp)
        Text(value.toString(), color = if (value >= 0) V3Green else V3Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun V3GoalRow(state: V3GameState, goal: V3AnnualGoal) {
    val progress = V3GameEngine.goalProgress(state, goal)
    val reached = progress >= goal.target || goal.completed
    Column(Modifier.fillMaxWidth().background(if (reached) V3Green.copy(alpha = 0.16f) else V3PaperDeep, RoundedCornerShape(0.dp)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(goal.title, color = V3Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(if (reached) "已成" else "${progress}/${goal.target}", color = if (reached) V3Green else V3Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text(goal.desc, color = V3Muted, fontSize = 12.sp)
    }
}

@Composable
private fun V3Button(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(text, modifier = modifier.background(V3Red, RoundedCornerShape(0.dp)).clickable(onClick = onClick).padding(horizontal = 13.dp, vertical = 12.dp), color = V3Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
}

@Composable
private fun V3SmallButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, selected: Boolean = false, onClick: () -> Unit) {
    Text(
        text,
        modifier = modifier.background(when { selected -> V3Red; enabled -> V3PaperDeep; else -> Color(0xFF33334B) }, RoundedCornerShape(0.dp)).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 7.dp, vertical = 9.dp),
        color = if (enabled || selected) V3Ink else V3Muted.copy(alpha = 0.55f),
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun V3EventDialog(event: V3ActiveEvent, controller: V3GameController) {
    Dialog(onDismissRequest = {}) {
        V3ImagePanel(GameImages.V3UiEventPanel, Modifier.widthIn(max = 500.dp)) {
            Text(event.title, color = V3Red, fontSize = 21.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Box(Modifier.fillMaxWidth().heightIn(max = 430.dp).verticalScroll(rememberScrollState())) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(event.body, color = V3Ink, fontSize = 15.sp, lineHeight = 23.sp)
                    event.choices.forEach { choice ->
                        Card(modifier = Modifier.fillMaxWidth().clickable { controller.chooseEvent(choice) }, colors = CardDefaults.cardColors(containerColor = V3PaperDeep), border = BorderStroke(2.dp, V3Red), shape = RoundedCornerShape(0.dp)) {
                            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(choice.label, color = V3Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text(choice.route.label, color = V3Red, fontSize = 12.sp)
                                }
                                Text(choice.desc, color = V3Muted, fontSize = 12.sp)
                                Text(choiceImpactSummary(choice), color = V3Ink, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun V3Dialog(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        V3ImagePanel(GameImages.V3UiEventPanel, Modifier.widthIn(max = 460.dp)) {
            Text(title, color = V3Red, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Box(Modifier.fillMaxWidth().heightIn(max = 390.dp).verticalScroll(rememberScrollState())) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp), content = content)
            }
            V3Button("知道了", Modifier.fillMaxWidth(), onClick = onDismiss)
        }
    }
}

@Composable
private fun V3ExamDialog(session: com.daming.fushengzhi3.v3.data.V3ExamSession, controller: V3GameController) {
    val question = V3GameEngine.examQuestion(session)
    if (question != null) {
        Dialog(onDismissRequest = {}) {
            V3ImagePanel(GameImages.V3UiExamPaper, Modifier.widthIn(max = 500.dp)) {
                Text("${session.stage.label}考题", color = V3Red, fontSize = 21.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text(question.question, color = V3Ink, fontSize = 16.sp, lineHeight = 24.sp)
                question.options.forEachIndexed { index, option ->
                    V3SmallButton(option, Modifier.fillMaxWidth()) { controller.answerExam(index) }
                }
                Text("提示：学识和谋略越高，答错时也越可能靠底子补救。", color = V3Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun V3BattleDialog(target: String, enemyPower: Int, risk: String, controller: V3GameController) {
    Dialog(onDismissRequest = {}) {
        V3ImagePanel(GameImages.V3UiBattleReport, Modifier.widthIn(max = 460.dp)) {
            Text("军务出征", color = V3Red, fontSize = 21.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text("目标：$target", color = V3Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("敌势：$enemyPower · 风险：$risk", color = V3Muted, fontSize = 13.sp)
            Text("结算会参考乡勇数量、武艺最高族人、寨堡等级和军镇关系。胜利后降低地点风险，推动从军、自保和割据路线。", color = V3Ink, fontSize = 13.sp, lineHeight = 20.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                V3SmallButton("出战", Modifier.weight(1f), selected = true) { controller.resolveBattle() }
                V3SmallButton("暂缓", Modifier.weight(1f)) { controller.cancelBattle() }
            }
        }
    }
}

@Composable
private fun V3ConquestDialog(target: String, enemyPower: Int, scale: String, controller: V3GameController) {
    Dialog(onDismissRequest = {}) {
        V3ImagePanel(GameImages.V3UiBattleReport, Modifier.widthIn(max = 460.dp)) {
            Text(scale, color = V3Red, fontSize = 21.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text("目标：$target", color = V3Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("敌势：$enemyPower", color = V3Muted, fontSize = 13.sp)
            Text("这是从县域经营走向州府、京畿和天下统一的战役。结算会参考乡勇、团练营、寨堡、族望、武艺最高族人和统一进度。", color = V3Ink, fontSize = 13.sp, lineHeight = 20.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                V3SmallButton("开战", Modifier.weight(1f), selected = true) { controller.resolveConquest() }
                V3SmallButton("暂缓", Modifier.weight(1f)) { controller.cancelConquest() }
            }
        }
    }
}

@Composable
private fun V3SettingsDialog(controller: V3GameController, fontPreference: FontPreference, onRequestBackToMenu: () -> Unit) {
    Dialog(onDismissRequest = controller::closeSettings) {
        V3ImagePanel(GameImages.V3UiSettingsScroll, Modifier.widthIn(max = 460.dp)) {
            Text("游戏设置", color = V3Red, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text("字体", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            FontStyleKey.entries.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { style ->
                        V3SmallButton(
                            "${style.label}\n${style.desc}",
                            Modifier.weight(1f),
                            selected = fontPreference.style == style
                        ) { fontPreference.updateStyle(style) }
                    }
                    repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Text("声音", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            V3VolumeRow("背景音乐", controller.bgmVolume, controller::updateBgmVolume)
            V3VolumeRow("音效", controller.sfxVolume, controller::updateSfxVolume)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                V3SmallButton("返回主菜单", Modifier.weight(1f)) {
                    onRequestBackToMenu()
                }
                V3SmallButton("关闭", Modifier.weight(1f), selected = true) { controller.closeSettings() }
            }
        }
    }
}

@Composable
private fun V3ConfirmBackToMenuDialog(onConfirm: () -> Unit, onCancel: () -> Unit) {
    Dialog(onDismissRequest = onCancel) {
        V3ImagePanel(GameImages.V3UiSettingsScroll, Modifier.widthIn(max = 430.dp)) {
            Text("返回主菜单？", color = V3Red, fontSize = 21.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text("当前宗族进度已自动存档。返回主菜单会中断当前查看流程，但不会删除存档。", color = V3Ink, fontSize = 14.sp, lineHeight = 21.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                V3SmallButton("继续游戏", Modifier.weight(1f), selected = true, onClick = onCancel)
                V3SmallButton("返回菜单", Modifier.weight(1f), onClick = onConfirm)
            }
        }
    }
}

@Composable
private fun V3VolumeRow(label: String, value: Float, onChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = V3Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("${(value * 100).toInt()}%", color = V3Muted, fontSize = 13.sp)
        }
        Slider(value = value, onValueChange = onChange, valueRange = 0f..1f)
    }
}

private fun monthlyReportTitle(baseTitle: String, lines: List<String>): String = when {
    lines.any { it.contains("添丁") || it.contains("出生") } -> "添丁喜报"
    lines.any { it.contains("目标【") && it.contains("完成") } -> "目标达成"
    else -> baseTitle
}

private fun nextAdvice(state: V3GameState): String = when {
    V3GameEngine.marriageOptions(state).isNotEmpty() -> "先到【宗族】迎娶妻子，家族才会添丁。"
    V3GameEngine.builtSiteCount(state) < 2 -> "建第二处产业，形成银粮双收入。"
    V3GameEngine.canRankUp(state) -> "条件已够，到【宗族】晋升品第。"
    V3GameEngine.alivePeople(state).none { it.age >= 12 && it.currentTask == null && it.trainingFocus == null } -> "等待子嗣成长，或让空闲族人先培养属性。"
    else -> "选择产业派人经营，再推进月结。"
}

private fun siteChipText(site: V3CountySite): String = "${site.name.takeLast(2)}${if (site.level > 0) "Lv.${site.level}" else "未建"}"

private fun siteYieldSummary(yield: V3SiteYield): String {
    val parts = mutableListOf<String>()
    fun add(label: String, value: Int) {
        if (value > 0) parts += "$label+$value"
        if (value < 0) parts += "$label$value"
    }
    add("银", yield.silver)
    add("粮", yield.grain)
    add("望", yield.influence)
    add("凝", yield.cohesion)
    add("勇", yield.militia)
    return if (parts.isEmpty()) "无" else parts.joinToString(" / ")
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
    add("勇", choice.militiaDelta)
    add("官", choice.yamenDelta)
    add("绅", choice.gentryDelta)
    add("民", choice.villagersDelta)
    add("寇", choice.banditsDelta)
    add("商", choice.merchantsDelta)
    add("军", choice.garrisonDelta)
    add("控", choice.siteControlDelta)
    add("险", choice.siteRiskDelta)
    add("功", choice.personMeritDelta)
    add("劳", choice.personFatigueDelta)
    add("忠", choice.personLoyaltyDelta)
    if (choice.routeDelta != 0) parts += "${choice.route.label}+${choice.routeDelta}"
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
    V3TaskType.Govern -> "提升控制和凝聚。"
    V3TaskType.Farm -> "增加粮食，适合早期活命。"
    V3TaskType.Trade -> "增加银两，适合扩产业。"
    V3TaskType.Study -> "培养读书路线和族望。"
    V3TaskType.Diplomacy -> "改善官府与士绅。"
    V3TaskType.Relief -> "花粮银换民心。"
    V3TaskType.Fortify -> "压风险，强防务。"
    V3TaskType.Scout -> "查山道和流寇。"
    V3TaskType.Recruit -> "增加乡勇。"
}

private fun bestPersonFor(people: List<V3Person>, task: V3TaskType): V3Person? {
    return people.filter { it.alive && it.age >= 12 && it.currentTask == null && it.trainingFocus == null }.maxByOrNull { person ->
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

private fun targetSiteFor(state: V3GameState, task: V3TaskType): V3CountySite? {
    return state.sites.filter { it.taskTypes.contains(task) }.maxWithOrNull(compareBy<V3CountySite> { it.level }.thenBy { it.risk })
}
