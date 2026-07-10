package com.daming.fushengzhi3.v3.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.daming.fushengzhi3.data.GameImages
import com.daming.fushengzhi3.ui.components.AssetImage
import com.daming.fushengzhi3.v3.data.V3ActiveEvent
import com.daming.fushengzhi3.v3.data.V3AnnualGoal
import com.daming.fushengzhi3.v3.data.V3Content
import com.daming.fushengzhi3.v3.data.V3CountySite
import com.daming.fushengzhi3.v3.data.V3EventChoice
import com.daming.fushengzhi3.v3.data.V3FinalEnding
import com.daming.fushengzhi3.v3.data.V3GameState
import com.daming.fushengzhi3.v3.data.V3Person
import com.daming.fushengzhi3.v3.data.V3Route
import com.daming.fushengzhi3.v3.data.V3Screen
import com.daming.fushengzhi3.v3.data.V3SiteYield
import com.daming.fushengzhi3.v3.data.V3TaskType
import com.daming.fushengzhi3.v3.data.V3TrainingType
import com.daming.fushengzhi3.v3.logic.V3GameController
import com.daming.fushengzhi3.v3.logic.V3GameEngine

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
fun V3GameScreen(controller: V3GameController, onBackToMenu: () -> Unit) {
    LaunchedEffect(Unit) { controller.ensureV3Bgm() }
    val state = controller.state
    V3Background {
        Column(Modifier.fillMaxSize()) {
            V3TopBar(state, controller, onBackToMenu)
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
        V3SettingsDialog(controller = controller, onBackToMenu = onBackToMenu)
    }
    controller.state.examSession?.let { session ->
        V3ExamDialog(session = session, controller = controller)
    }
    controller.state.battleState?.let { battle ->
        V3BattleDialog(target = battle.target, enemyPower = battle.enemyPower, risk = battle.risk, controller = controller)
    }
}

@Composable
private fun V3HomePage(state: V3GameState, controller: V3GameController) {
    var selectedSiteId by remember { mutableStateOf(state.sites.firstOrNull { it.level > 0 }?.id ?: state.sites.first().id) }
    val selectedSite = state.sites.firstOrNull { it.id == selectedSiteId } ?: state.sites.first()
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
        Text("眼前目标", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        state.annualGoals.take(3).forEach { goal -> V3GoalRow(state, goal) }
    }
    V3CountyMapView(state, selectedSiteId) { selectedSiteId = it }
    V3SelectorChips("产业", state.sites.map { it.id to siteChipText(it) }, selectedSiteId) { selectedSiteId = it }
    V3SiteCard(selectedSite, state, controller)
    V3Button("推进一月", Modifier.fillMaxWidth(), onClick = controller::advanceMonth)
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
                Card(colors = CardDefaults.cardColors(containerColor = V3PaperDeep), border = BorderStroke(1.dp, V3Gold), shape = RoundedCornerShape(8.dp)) {
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
    var selectedPersonId by remember { mutableStateOf(people.firstOrNull()?.id ?: 1) }
    var page by remember { mutableStateOf(0) }
    val pageSize = 6
    val totalPages = maxOf(1, (people.size + pageSize - 1) / pageSize)
    if (page >= totalPages) page = totalPages - 1
    val pagePeople = people.drop(page * pageSize).take(pageSize)
    val person = people.firstOrNull { it.id == selectedPersonId } ?: people.firstOrNull()
    V3Section("族谱", "书册翻页查看家族脉络，再点族人安排培养、科举、经营或军务")
    V3GenealogyBook(pagePeople, page, totalPages, selectedPersonId, onSelect = { selectedPersonId = it }, onPrev = {
        if (page > 0) {
            page -= 1
            controller.pageTurn()
        }
    }, onNext = {
        if (page < totalPages - 1) {
            page += 1
            controller.pageTurn()
        }
    })
    if (person != null) V3PersonCard(person, state, controller)
}

@Composable
private fun V3GenealogyBook(
    people: List<V3Person>,
    page: Int,
    totalPages: Int,
    selectedPersonId: Int,
    onSelect: (Int) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Box(Modifier.fillMaxWidth().height(330.dp)) {
        AssetImage(GameImages.V3UiGenealogyBook, null, Modifier.matchParentSize(), ContentScale.Crop)
        Row(Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 34.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            V3GenealogyPageColumn(people.take(3), selectedPersonId, Modifier.weight(1f), onSelect)
            V3GenealogyPageColumn(people.drop(3), selectedPersonId, Modifier.weight(1f), onSelect)
        }
        if (page > 0) {
            AssetImage(GameImages.V3UiPageLeft, "上一页", Modifier.align(Alignment.CenterStart).size(54.dp).clickable(onClick = onPrev), ContentScale.Fit)
        }
        if (page < totalPages - 1) {
            AssetImage(GameImages.V3UiPageRight, "下一页", Modifier.align(Alignment.CenterEnd).size(54.dp).clickable(onClick = onNext), ContentScale.Fit)
        }
        Text("第 ${page + 1} / $totalPages 页", color = V3Muted, fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomCenter).background(V3Paper.copy(alpha = 0.74f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Composable
private fun V3GenealogyPageColumn(people: List<V3Person>, selectedPersonId: Int, modifier: Modifier, onSelect: (Int) -> Unit) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        people.forEach { person ->
            Column(
                Modifier.fillMaxWidth().background(if (person.id == selectedPersonId) V3Gold.copy(alpha = 0.22f) else Color(0x22FFFFFF), RoundedCornerShape(8.dp)).clickable { onSelect(person.id) }.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(person.name, color = V3Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("${person.gender.label} · ${person.age}岁 · ${person.identity}", color = V3Red, fontSize = 11.sp)
                Text("学${person.study} 武${person.martial} 商${person.commerce} 谋${person.diplomacy}", color = V3Muted, fontSize = 10.sp)
            }
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
private fun V3CountyMapView(state: V3GameState, selectedSiteId: String, onSelectSite: (String) -> Unit) {
    var pan by remember { mutableStateOf(Offset.Zero) }
    V3Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("县域地图", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("可拖动预览 · 点建筑经营", color = V3Muted, fontSize = 12.sp)
        }
        Box(
            Modifier.fillMaxWidth().height(420.dp).background(Color(0xFF2A2018), RoundedCornerShape(10.dp)).pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    pan = Offset((pan.x + dragAmount.x).coerceIn(-360f, 120f), (pan.y + dragAmount.y).coerceIn(-420f, 120f))
                }
            }
        ) {
            Box(Modifier.size(width = 720.dp, height = 960.dp).graphicsLayer { translationX = pan.x; translationY = pan.y }) {
                AssetImage(GameImages.V3MapBgPlain, null, Modifier.fillMaxSize(), ContentScale.Crop)
                state.sites.forEach { site ->
                    V3MapSitePin(site, selectedSiteId == site.id) { onSelectSite(site.id) }
                }
            }
        }
    }
}

@Composable
private fun V3MapSitePin(site: V3CountySite, selected: Boolean, onClick: () -> Unit) {
    val point = siteMapPoint(site.id)
    val icon = GameImages.v3SiteIcons[site.id] ?: return
    Column(
        Modifier.graphicsLayer { translationX = point.x; translationY = point.y }.width(96.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(Modifier.size(if (selected) 86.dp else 74.dp).background(if (selected) V3Gold.copy(alpha = 0.28f) else Color.Transparent, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            AssetImage(icon, site.name, Modifier.size(if (selected) 78.dp else 68.dp), ContentScale.Fit)
        }
        Text(site.name, color = if (selected) V3Red else V3Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.background(V3Paper.copy(alpha = 0.86f), RoundedCornerShape(5.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
    }
}

private fun siteMapPoint(siteId: String): Offset = when (siteId) {
    "shrine" -> Offset(300f, 390f)
    "farmland" -> Offset(150f, 520f)
    "market" -> Offset(430f, 500f)
    "yamen" -> Offset(320f, 250f)
    "fort" -> Offset(105f, 190f)
    "dock" -> Offset(500f, 700f)
    "academy" -> Offset(480f, 330f)
    "clinic" -> Offset(220f, 670f)
    "mountain_pass" -> Offset(120f, 760f)
    else -> Offset(320f, 450f)
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
private fun V3TopBar(state: V3GameState, controller: V3GameController, onBackToMenu: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(Color(0xEE1F1712)).padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(state.clanName, color = V3Gold, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("${state.year}年${state.month}月 · ${V3GameEngine.clanRankName(state)} · ${state.crisis}", color = V3Paper, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                V3SmallButton("设置", Modifier.width(66.dp)) { controller.openSettings() }
                V3SmallButton("菜单", Modifier.width(66.dp)) { onBackToMenu() }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            V3ResourceMetric(GameImages.V3IconSilver, "银", state.silver, V3Gold, Modifier.weight(1f))
            V3ResourceMetric(GameImages.V3IconGrain, "粮", state.grain, V3Green, Modifier.weight(1f))
            V3ResourceMetric(GameImages.V3IconPopulation, "人", V3GameEngine.alivePeople(state).size, V3Blue, Modifier.weight(1f))
            V3ResourceMetric(GameImages.V3IconIndustry, "产", V3GameEngine.builtSiteCount(state), V3Red, Modifier.weight(1f))
        }
    }
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
    Box(Modifier.fillMaxSize().background(V3Ink)) {
        AssetImage(GameImages.V3DossierBg, null, Modifier.fillMaxSize(), ContentScale.Crop, alpha = 0.72f)
        Box(Modifier.fillMaxSize().background(Color(0xAA120D0A)))
        content()
    }
}

@Composable
private fun V3Title(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, color = V3Gold, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = V3Paper, fontSize = 15.sp)
    }
}

@Composable
private fun V3Section(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, color = V3Gold, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = V3Paper, fontSize = 13.sp, lineHeight = 19.sp)
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(V3Red))
    }
}

@Composable
private fun V3Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = V3Paper), border = BorderStroke(1.dp, V3Gold), shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(7.dp), content = content)
    }
}

@Composable
private fun V3ImagePanel(imagePath: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier.fillMaxWidth()) {
        AssetImage(imagePath, null, Modifier.matchParentSize(), ContentScale.Crop)
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
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
    Column(modifier.background(Color(0x44FFFFFF), RoundedCornerShape(8.dp)).padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = V3Muted, fontSize = 11.sp)
        Text(value.toString(), color = color, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun V3ResourceMetric(iconPath: String, label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier.background(Color(0x44FFFFFF), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AssetImage(iconPath, label, Modifier.size(28.dp), ContentScale.Fit)
        Column(horizontalAlignment = Alignment.Start) {
            Text(label, color = V3Muted, fontSize = 10.sp)
            Text(value.toString(), color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
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
    Column(Modifier.fillMaxWidth().background(if (reached) V3Green.copy(alpha = 0.12f) else Color(0x44FFFFFF), RoundedCornerShape(8.dp)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(goal.title, color = V3Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(if (reached) "已成" else "${progress}/${goal.target}", color = if (reached) V3Green else V3Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text(goal.desc, color = V3Muted, fontSize = 12.sp)
    }
}

@Composable
private fun V3Button(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(text, modifier = modifier.background(V3Red, RoundedCornerShape(9.dp)).clickable(onClick = onClick).padding(horizontal = 13.dp, vertical = 11.dp), color = V3Paper, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
}

@Composable
private fun V3SmallButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, selected: Boolean = false, onClick: () -> Unit) {
    Text(
        text,
        modifier = modifier.background(when { selected -> V3Red; enabled -> Color(0xFFE8D1A6); else -> V3Muted.copy(alpha = 0.4f) }, RoundedCornerShape(7.dp)).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 7.dp, vertical = 8.dp),
        color = if (selected) V3Paper else V3Ink,
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
                        Card(modifier = Modifier.fillMaxWidth().clickable { controller.chooseEvent(choice) }, colors = CardDefaults.cardColors(containerColor = V3PaperDeep), border = BorderStroke(1.dp, V3Red), shape = RoundedCornerShape(8.dp)) {
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
private fun V3SettingsDialog(controller: V3GameController, onBackToMenu: () -> Unit) {
    Dialog(onDismissRequest = controller::closeSettings) {
        V3ImagePanel(GameImages.V3UiSettingsScroll, Modifier.widthIn(max = 460.dp)) {
            Text("游戏设置", color = V3Red, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text("声音", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            V3VolumeRow("背景音乐", controller.bgmVolume, controller::setBgmVolume)
            V3VolumeRow("音效", controller.sfxVolume, controller::setSfxVolume)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                V3SmallButton("返回主菜单", Modifier.weight(1f)) {
                    controller.closeSettings()
                    onBackToMenu()
                }
                V3SmallButton("关闭", Modifier.weight(1f), selected = true) { controller.closeSettings() }
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
    if (yield.silver != 0) parts += "银+${yield.silver}"
    if (yield.grain != 0) parts += "粮+${yield.grain}"
    if (yield.influence != 0) parts += "望+${yield.influence}"
    if (yield.cohesion != 0) parts += "凝+${yield.cohesion}"
    if (yield.militia != 0) parts += "勇+${yield.militia}"
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
