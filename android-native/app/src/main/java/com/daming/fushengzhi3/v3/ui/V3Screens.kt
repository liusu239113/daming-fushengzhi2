package com.daming.fushengzhi3.v3.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.daming.fushengzhi3.v3.data.V3BattleState
import com.daming.fushengzhi3.v3.data.V3BattlePhase
import com.daming.fushengzhi3.v3.data.V3Combatant
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
import com.daming.fushengzhi3.v3.data.V3TroopType
import com.daming.fushengzhi3.v3.data.V3WorldRegion
import com.daming.fushengzhi3.v3.logic.V3GameController
import com.daming.fushengzhi3.v3.logic.V3GameEngine
import kotlinx.coroutines.delay

private val V3Ink = Color(0xFF2B2016)
private val V3Paper = Color(0xFFF4E7C7)
private val V3PaperDeep = Color(0xFFE6D2A4)
private val V3Red = Color(0xFFA83224)
private val V3Gold = Color(0xFF8A5A19)
private val V3Muted = Color(0xFF6E5D46)
private val V3Green = Color(0xFF2F7D55)
private val V3Blue = Color(0xFF426B67)
private val V3Bg = Color(0xFFB98E59)
private val V3Border = Color(0xFF8A5A19)
private val V3SealRed = Color(0xFFB33A2F)
private val V3Rice = Color(0xFFFFF4D8)
private val V3SoftShape = RoundedCornerShape(16.dp)
private val V3PanelShape = RoundedCornerShape(20.dp)
private val V3ButtonShape = RoundedCornerShape(14.dp)

@Composable
fun V3CreateScreen(controller: V3GameController, onBack: () -> Unit, onStart: () -> Unit) {
    LaunchedEffect(Unit) { controller.ensureV3Bgm() }
    var clanName by remember { mutableStateOf("李氏宗族") }
    var root by remember { mutableStateOf("没落士族") }
    var county by remember { mutableStateOf("江南水乡") }
    var creed by remember { mutableStateOf("耕读传家") }
    var crisis by remember { mutableStateOf("官府催税") }

    val profile = V3Content.startProfile(root, county, creed, crisis)

    V3Background {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .widthIn(max = 760.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                V3Title("大明浮生志3", "一户起家 · 成婚育子 · 经营宗族")
                V3Panel {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("宗族", color = V3Red, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(54.dp))
                        TextField(
                            value = clanName,
                            onValueChange = { clanName = it.take(8) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = V3Rice,
                                unfocusedContainerColor = V3Rice,
                                focusedTextColor = V3Ink,
                                unfocusedTextColor = V3Ink,
                                focusedIndicatorColor = V3Red,
                                unfocusedIndicatorColor = V3Border
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    V3CompactSelector("出身", V3Content.roots, root, ::createRootEffect) { root = it }
                    V3CompactSelector("县域", V3Content.counties, county, {
                        V3Content.startProfile(root, it, creed, crisis).countyEffect
                    }) { county = it }
                    V3CompactSelector("家训", V3Content.creeds, creed, ::createCreedEffect) { creed = it }
                    V3CompactSelector("危机", V3Content.crises, crisis, {
                        V3Content.startProfile(root, county, creed, it).crisisEffect
                    }) { crisis = it }
                }
                V3Panel {
                    Text("开局实得", color = V3Red, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "银两 ${profile.silver} · 粮食 ${profile.grain} · " +
                            "族望 ${profile.influence} · 凝聚 ${profile.cohesion} · " +
                            "乡勇 ${profile.militia}",
                        color = V3Ink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    )
                    Text(
                        "出身：${createRootEffect(root)}",
                        color = V3Muted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Text(
                        "县域：${profile.countyEffect}",
                        color = V3Muted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Text(
                        "家训：${createCreedEffect(creed)}",
                        color = V3Muted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Text(
                        "危机：${profile.crisisEffect}",
                        color = V3Muted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Text(
                        "首年目标：${profile.annualGoals.joinToString("、") { it.title }}",
                        color = V3Gold,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                V3Button("返回", Modifier.weight(1f), onClick = onBack)
                V3Button("开宗立户 · 进入游戏", Modifier.weight(1f)) {
                    controller.newGame(root, county, creed, crisis, clanName)
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
                3 -> 7300L
                2 -> 11000L
                else -> 22000L
            }
            delay(interval)
            controller.autoAdvanceTime()
        }
    }
    val state = controller.state
    var confirmBackToMenu by remember { mutableStateOf(false) }
    var elderGuideVisible by remember { mutableStateOf(state.year == 1601 && state.month <= 3) }
    var elderGuideStep by remember { mutableStateOf(0) }
    var guideStrategyPage by remember { mutableStateOf<String?>(null) }
    V3Background {
        Box(Modifier.fillMaxSize()) {
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
                            V3Screen.Strategy -> V3StrategyPage(state, controller, forcedPage = guideStrategyPage, openGuide = {
                                elderGuideStep = 0
                                guideStrategyPage = null
                                elderGuideVisible = true
                            })
                        }
                    }
                }
                if (state.finalEnding == null) V3BottomNav(controller)
            }
            if (
                elderGuideVisible &&
                    state.finalEnding == null &&
                    controller.latestReport == null &&
                    controller.message == null &&
                    !controller.settingsVisible &&
                    state.activeEvent == null &&
                    state.examSession == null &&
                    state.battleState == null &&
                    state.conquestState == null
            ) {
                V3ElderGuideOverlay(
                    state = state,
                    controller = controller,
                    stepIndex = elderGuideStep,
                    onStepChange = { elderGuideStep = it },
                    onStrategyPageChange = { guideStrategyPage = it },
                    onDismiss = {
                        guideStrategyPage = null
                        elderGuideVisible = false
                    }
                )
            }
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
        V3BattleDialog(state = controller.state, battle = battle, controller = controller)
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
    V3ClanLedgerPanel(state)
    V3RouteOverviewPanel(state)
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
    V3CountyMapView(state) {
        controller.pauseForPlayerAction()
        selectedSiteId = it
    }
    V3EstatePanel(state, controller)
    selectedSite?.let { site ->
        V3SiteManageDialog(site = site, state = state, controller = controller, onDismiss = { selectedSiteId = null })
    }
}

@Composable
private fun V3ClanLedgerPanel(state: V3GameState) {
    val peopleFood = V3GameEngine.alivePeople(state).map { if (it.age < 12) 1 else 3 }.sum()
    val militiaFood = state.militia / 8
    V3Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("族中月账", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("开门七事，先算粮银", color = V3Muted, fontSize = 12.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            V3Metric("人丁耗粮", peopleFood, V3Red, Modifier.weight(1f))
            V3Metric("乡勇耗粮", militiaFood, V3Red, Modifier.weight(1f))
            V3Metric("险地", forecastDangerCount(state), V3Red, Modifier.weight(1f))
        }
        Text("族人越多，办事越快，粮仓也越紧；先安家口，再扩产业。", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
    }
}

private fun forecastDangerCount(state: V3GameState): Int = state.sites.count { it.risk >= 55 }

private fun expandedMapPoint(point: Offset): Offset = Offset(0.18f + point.x * 0.64f, 0.18f + point.y * 0.64f)

private enum class V3GuideFocus {
    TopBar,
    TimeControls,
    Resources,
    MainPanel,
    MapArea,
    BottomNav
}

private data class V3ElderGuideStep(
    val tab: V3Screen,
    val title: String,
    val words: String,
    val action: String,
    val focus: V3GuideFocus,
    val strategyPage: String? = null
)

private fun elderGuideSteps(state: V3GameState): List<V3ElderGuideStep> = listOf(
    V3ElderGuideStep(
        V3Screen.County,
        "雨夜启卷",
        "慎行，今晚族祠只点一盏灯。你接过的是${state.clanName}的家乘，不是一张空账。先看最上方的案头：银两能办事，粮食能养人，人口是根，产业是枝。时间默认暂停，别急着让月份流走。",
        "看案头",
        V3GuideFocus.TopBar
    ),
    V3ElderGuideStep(
        V3Screen.County,
        "别让年月偷跑",
        "这里是时间控制。点【继续】或 1倍、2倍、3倍，月份才会流动。每次切页面都会自动暂停，给你时间看账、派人、营建。经营游戏不是拼手速，是先做决定。",
        "看时间",
        V3GuideFocus.TimeControls
    ),
    V3ElderGuideStep(
        V3Screen.County,
        "四样家底",
        "这四个小胶囊是眼前命脉：银两用来婚配、营建、升级；粮食养人养勇；人口决定族谱能不能延续；产业决定每月是否有稳定进项。少看一个，账就会歪。",
        "看资源",
        V3GuideFocus.Resources
    ),
    V3ElderGuideStep(
        V3Screen.County,
        "先读月账",
        "家业页第一眼看【族中月账】和【本月账本】。入银、出银、入粮、出粮决定你能不能撑过一年。人丁越多，消耗越大；乡勇越多，打仗越稳，粮仓也越紧。",
        "看账本",
        V3GuideFocus.MainPanel
    ),
    V3ElderGuideStep(
        V3Screen.County,
        "县图可拖",
        "这块县域地图可以拖动。宗祠管凝聚，田庄管粮，集市管银，书院管科举，寨堡管军务，码头管商路。点任意建筑，会弹出管理窗口。",
        "看地图",
        V3GuideFocus.MapArea
    ),
    V3ElderGuideStep(
        V3Screen.County,
        "营建根基",
        "建筑不是摆设。先造田庄保粮，再开集市补银；若流寇逼近，寨堡要早修；若想走读书入仕，书院不能拖。每个地点都有升级成本和特殊动作。",
        "看营建",
        V3GuideFocus.MapArea
    ),
    V3ElderGuideStep(
        V3Screen.Clan,
        "一户成家",
        "现在自动切到宗族页。乱世里，一人不是宗族。先看【婚配】，迎娶妻子后，后面才有添丁、子嗣、房支、族谱传承。没有家口，李氏再有银粮也只是独户。",
        "看婚配",
        V3GuideFocus.MainPanel
    ),
    V3ElderGuideStep(
        V3Screen.Clan,
        "宗族晋升",
        "宗族页还有【晋升宗族】。晋升要银、粮、人口、产业和族望。品第越高，家族影响越大，路线推进也更稳。别只攒钱，人口和族望同样要经营。",
        "看晋升",
        V3GuideFocus.MainPanel
    ),
    V3ElderGuideStep(
        V3Screen.People,
        "翻开族谱",
        "现在自动切到族人页。这里是活的族谱，不是名单。拖动画布看族人，点头像看详情。孩童先培养，成年后派差；老人虽少外出，也能撑起房支名望。",
        "看族谱",
        V3GuideFocus.MapArea
    ),
    V3ElderGuideStep(
        V3Screen.People,
        "因材派差",
        "点开族人后看四项：学、武、商、谋。学高走书院和科举，武高守寨讨寇，商高跑集市码头，谋高适合交涉经营。派错人不是不能做，但收益会低。",
        "点族人",
        V3GuideFocus.MainPanel
    ),
    V3ElderGuideStep(
        V3Screen.People,
        "培养与疲劳",
        "每个族人都可以培养。孩童培养成长更快，成年族人办事会积累功绩，也会疲劳。疲劳高了要收手，否则家里账面好看，人却会被拖垮。",
        "看培养",
        V3GuideFocus.MainPanel
    ),
    V3ElderGuideStep(
        V3Screen.Strategy,
        "看清大势",
        "现在自动切到大势页。这里不是结算页，是路线盘。耕读、重商、自保、勤王、割据、海路，每个月的选择都会悄悄给路线加分，最后决定李氏写进怎样的家乘。",
        "看大势",
        V3GuideFocus.MainPanel,
        strategyPage = "声势"
    ),
    V3ElderGuideStep(
        V3Screen.Strategy,
        "四个分卷",
        "大势页上方有【声势、天下、军务、近事】四个按钮。声势看路线，天下看地域，军务管战争，近事看日志。后面玩不懂，就先回这四个分卷找线索。",
        "看分卷",
        V3GuideFocus.MainPanel,
        strategyPage = "声势"
    ),
    V3ElderGuideStep(
        V3Screen.Strategy,
        "天下地图",
        "【天下】是一张更大的局。地图可以拖动，点清河县、州府、南直隶、京畿、天下这些节点，可以结交、经营、征伐。县里稳住后，李氏才有资格看天下。",
        "看天下",
        V3GuideFocus.MapArea,
        strategyPage = "天下"
    ),
    V3ElderGuideStep(
        V3Screen.Strategy,
        "军务与举旗",
        "【军务】里能讨伐流寇，也能举旗。别早早举旗，乡勇、寨堡、族望、武艺人才都不够时，旗一竖就是灭门。先自保，再谈征伐。",
        "看军务",
        V3GuideFocus.MainPanel,
        strategyPage = "军务"
    ),
    V3ElderGuideStep(
        V3Screen.County,
        "底部四页",
        "最后看底部四个主按钮：家业、宗族、族人、大势。家业管账和地图，宗族管婚配晋升，族人管培养派差，大势管路线天下。四页都会用，才是真正在经营宗族。",
        "看底栏",
        V3GuideFocus.BottomNav
    ),
    V3ElderGuideStep(
        V3Screen.County,
        "族老退席",
        "记住顺序：先成家，再造田；先养人，再派差；先稳县，再问天下。若哪一步忘了，到大势页点【族老札记】，我会重新把这卷家乘讲一遍。",
        "开始经营",
        V3GuideFocus.MainPanel
    )
)

@Composable
private fun V3ElderGuideOverlay(state: V3GameState, controller: V3GameController, stepIndex: Int, onStepChange: (Int) -> Unit, onStrategyPageChange: (String?) -> Unit, onDismiss: () -> Unit) {
    val steps = elderGuideSteps(state)
    val safeIndex = stepIndex.coerceIn(0, steps.lastIndex)
    val step = steps[safeIndex]
    LaunchedEffect(safeIndex) {
        onStrategyPageChange(step.strategyPage)
        controller.switchScreen(step.tab)
        controller.playGuideTick()
    }
    Box(Modifier.fillMaxSize().background(Color(0x77000000))) {
        V3GuideFocusFrame(step.focus)
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .fillMaxWidth()
                .background(V3Rice, V3PanelShape)
                .border(2.dp, V3Gold, V3PanelShape)
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AssetImage(GameImages.v3AvatarPortraits.getValue("male_elder"), "族老", Modifier.size(52.dp), ContentScale.Fit)
                        Column {
                            Text(step.title, color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("族老 · ${state.clanName}", color = V3Muted, fontSize = 11.sp)
                        }
                    }
                    Text("${safeIndex + 1}/${steps.size}", color = V3Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(step.words, color = V3Ink, fontSize = 14.sp, lineHeight = 21.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    V3SmallButton("跳过", Modifier.weight(1f), onClick = onDismiss)
                    V3SmallButton(step.action, Modifier.weight(1f)) { controller.switchScreen(step.tab) }
                    V3SmallButton(if (safeIndex == steps.lastIndex) "知道了" else "下一句", Modifier.weight(1f), selected = true) {
                        if (safeIndex == steps.lastIndex) onDismiss() else onStepChange(safeIndex + 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun V3GuideFocusFrame(focus: V3GuideFocus) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val modifier = when (focus) {
            V3GuideFocus.TopBar -> Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 8.dp, vertical = 5.dp)
            V3GuideFocus.TimeControls -> Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 54.dp)
                .height(48.dp)
            V3GuideFocus.Resources -> Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 102.dp)
                .height(62.dp)
            V3GuideFocus.MainPanel -> Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(maxHeight * 0.42f)
                .padding(horizontal = 12.dp)
            V3GuideFocus.MapArea -> Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(maxHeight * 0.52f)
                .padding(horizontal = 14.dp)
            V3GuideFocus.BottomNav -> Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        }
        Box(
            modifier
                .background(Color(0x22FFF4D8), V3PanelShape)
                .border(3.dp, V3Gold, V3PanelShape)
        )
    }
}

@Composable
private fun V3RouteOverviewPanel(state: V3GameState) {
    val dominant = V3GameEngine.dominantRoute(state)
    V3Panel {
        Text("当前主路线：${dominant.label}", color = V3Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        V3Content.routePlans.sortedByDescending { state.routeScores[it.route] ?: 0 }.take(4).forEach { plan ->
            V3RouteProgressRow(plan.route.label, state.routeScores[plan.route] ?: 0, selected = plan.route == dominant)
        }
    }
}

@Composable
private fun V3RouteProgressRow(label: String, score: Int, selected: Boolean) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = if (selected) V3Gold else V3Ink, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            Text(routeStage(score), color = if (selected) V3Gold else V3Muted, fontSize = 12.sp)
        }
        Box(Modifier.fillMaxWidth().height(7.dp).background(V3PaperDeep, V3SoftShape)) {
            Box(Modifier.fillMaxWidth((score.coerceIn(0, 100) / 100f).coerceAtLeast(0.05f)).height(7.dp).background(if (selected) V3Gold else V3Blue, V3SoftShape))
        }
    }
}

private fun routeStage(score: Int): String = when {
    score >= 80 -> "成局"
    score >= 55 -> "成势"
    score >= 32 -> "入路"
    score >= 15 -> "萌芽"
    else -> "未定"
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
    val eligible = V3GameEngine.marriageEligiblePeople(state)
    if (options.isNotEmpty() || eligible.any { it.id != 1 && V3GameEngine.marriageCandidatesFor(it, state).isNotEmpty() }) {
        V3Panel {
            Text("婚配", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("适龄未婚男女都可以婚配：男族人迎娶，女族人可按入赘规则成家。儿童每月成长，成年后才可婚配和出战。", color = V3Ink, fontSize = 13.sp, lineHeight = 19.sp)
            val target = eligible.firstOrNull { V3GameEngine.marriageCandidatesFor(it, state).isNotEmpty() } ?: eligible.firstOrNull()
            target?.let { person ->
                Text("当前婚配人选：${person.name} · ${person.gender.label} · ${person.age}岁 · ${V3GameEngine.lifeStage(person)}", color = V3Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                V3GameEngine.marriageCandidatesFor(person, state).take(8).forEach { option ->
                    Card(colors = CardDefaults.cardColors(containerColor = V3PaperDeep), border = BorderStroke(2.dp, V3Gold), shape = V3SoftShape) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
                            AssetImage(GameImages.v3AvatarPortraits[option.avatarKey] ?: GameImages.v3AvatarPortraits.getValue("female_youth"), option.name, Modifier.size(58.dp).background(V3Rice, CircleShape).border(2.dp, V3Gold, CircleShape).padding(3.dp), ContentScale.Fit)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(option.name, color = V3Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("${option.gender.label} · ${option.age}岁", color = V3Red, fontSize = 12.sp)
                                }
                                Text("银${option.silverCost}/粮${option.grainCost} · ${option.route.label}", color = V3Red, fontSize = 12.sp)
                                Text(option.desc, color = V3Muted, fontSize = 12.sp, lineHeight = 17.sp)
                                V3SmallButton("${person.name}与其婚配", Modifier.fillMaxWidth(), enabled = V3GameEngine.canMarry(state, option.id)) { controller.marry(option.id, person.id) }
                            }
                        }
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
    V3GenealogyTree(state.clanName, people, onSelect = {
        controller.pauseForPlayerAction()
        selectedPersonId = it
    })
    person?.let { selected ->
        V3PersonDetailDialog(person = selected, state = state, controller = controller, onDismiss = { selectedPersonId = null })
    }
}

@Composable
private fun V3GenealogyTree(clanName: String, people: List<V3Person>, onSelect: (Int) -> Unit) {
    var pan by remember { mutableStateOf(Offset.Zero) }
    val roots = people.filter { it.parentId == null }.ifEmpty { people.take(1) }
    val generations = people.groupBy { it.generation }.toSortedMap()
    V3Panel {
        Text("${clanName}谱系", color = V3Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("拖动画布查看族人；点头像可看详情、培养和派差。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
        Box(Modifier.fillMaxWidth().height(460.dp).clip(V3SoftShape).background(V3Rice, V3SoftShape).pointerInput(Unit) {
            detectDragGestures { _, dragAmount -> pan = Offset((pan.x + dragAmount.x).coerceIn(-880f, 80f), (pan.y + dragAmount.y).coerceIn(-980f, 80f)) }
        }) {
            AssetImage(GameImages.V3GenealogyBg, null, Modifier.matchParentSize(), ContentScale.Crop, alpha = 0.72f)
            Box(Modifier.size(width = 1120.dp, height = 1180.dp).graphicsLayer { translationX = pan.x; translationY = pan.y }) {
                roots.take(12).forEachIndexed { index, person ->
                    V3FamilyMiniNode(person, x = 480 + index * 104, y = 32, onSelect = onSelect)
                }
                generations.filterKeys { it > 1 }.forEach { (gen, members) ->
                    val y = 28 + (gen - 1) * 138
                    members.take(80).forEachIndexed { index, person ->
                        val x = 44 + (index % 9) * 116
                        val row = index / 9
                        V3FamilyMiniNode(person, x = x, y = y + row * 124, onSelect = onSelect)
                    }
                }
            }
        }
    }
}

@Composable
private fun V3FamilyMiniNode(person: V3Person, x: Int, y: Int, onSelect: (Int) -> Unit) {
    val statusColor = when {
        person.currentTask != null -> V3Green
        person.trainingFocus != null -> V3Blue
        person.fatigue >= 65 -> V3Red
        person.age < 12 -> V3Gold
        else -> V3Muted
    }
    val statusText = when {
        person.currentTask != null -> person.currentTask.label
        person.trainingFocus != null -> person.trainingFocus.label
        person.fatigue >= 65 -> "疲惫"
        person.age < 12 -> "幼学"
        else -> "待命"
    }
    Box(
        Modifier
            .graphicsLayer { translationX = x.toFloat(); translationY = y.toFloat() }
            .width(112.dp)
            .background(V3Paper.copy(alpha = 0.96f), V3SoftShape)
            .border(2.dp, V3Gold.copy(alpha = 0.88f), V3SoftShape)
            .clickable { onSelect(person.id) }
            .padding(4.dp)
    ) {
        Box(Modifier.matchParentSize().border(1.dp, V3Red.copy(alpha = 0.38f), V3SoftShape))
        Column(
            Modifier.fillMaxWidth().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text("第${person.generation}世", color = V3Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Box(Modifier.size(54.dp).background(V3Rice, CircleShape).border(2.dp, statusColor, CircleShape).padding(2.dp), contentAlignment = Alignment.Center) {
                AssetImage(v3AvatarFor(person), person.name, Modifier.size(49.dp), ContentScale.Fit)
            }
            Text(person.name, color = V3Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("${person.age}岁 · ${person.trait.label}", color = V3Muted, fontSize = 9.sp, maxLines = 1)
            Box(Modifier.fillMaxWidth().background(statusColor.copy(alpha = 0.14f), CircleShape).border(1.dp, statusColor.copy(alpha = 0.45f), CircleShape).padding(vertical = 2.dp), contentAlignment = Alignment.Center) {
                Text(statusText, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

private fun v3AvatarFor(person: V3Person): String {
    val key = when {
        person.age < 4 -> "baby"
        person.gender == V3Gender.Male && person.age >= 55 -> "male_elder"
        person.gender == V3Gender.Male && person.age >= 32 -> "male_middle"
        person.gender == V3Gender.Male -> "male_youth"
        person.gender == V3Gender.Female && person.age >= 55 -> "female_elder"
        person.gender == V3Gender.Female && person.age >= 32 -> "female_middle"
        else -> "female_youth"
    }
    return GameImages.v3AvatarPortraits[key] ?: GameImages.v3AvatarPortraits.getValue("male_youth")
}

@Composable
private fun V3PersonDetailDialog(person: V3Person, state: V3GameState, controller: V3GameController, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        V3ImagePanel(
            GameImages.V3UiEventPanel,
            Modifier
                .widthIn(max = 480.dp)
                .heightIn(max = 680.dp)
                .verticalScroll(rememberScrollState())
        ) {
            V3PersonCard(person, state, controller)
            V3SmallButton("关闭", Modifier.fillMaxWidth(), selected = true, onClick = onDismiss)
        }
    }
}

@Composable
private fun V3StrategyPage(state: V3GameState, controller: V3GameController, forcedPage: String?, openGuide: () -> Unit) {
    val ending = V3GameEngine.endingPreview(state)
    var page by remember { mutableStateOf(forcedPage ?: "声势") }
    LaunchedEffect(forcedPage) {
        if (forcedPage != null) page = forcedPage
    }
    V3Section("大势", "当前路线：${V3GameEngine.dominantRoute(state).label}")
    V3Panel {
        Text("路线评估：${ending.title}", color = V3Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("${ending.route.label} · ${ending.tier.label} · 评估 ${ending.score}", color = V3Ink, fontSize = 14.sp)
        Text(ending.desc, color = V3Muted, fontSize = 13.sp, lineHeight = 19.sp)
        Text("家乘所记，不止县中一亩三分地。婚配、产业、科举、军务和商路都会推着李氏走向不同结局。", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("声势", "天下", "军务", "近事").forEach { label ->
                V3SmallButton(label, Modifier.weight(1f), selected = page == label) { page = label }
            }
        }
    }
    when (page) {
        "声势" -> {
            V3Panel {
                Text("地方关系", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                V3RelationRow("官府", state.relations.yamen)
                V3RelationRow("士绅", state.relations.gentry)
                V3RelationRow("乡民", state.relations.villagers)
                V3RelationRow("流寇", state.relations.bandits)
                V3RelationRow("商帮", state.relations.merchants)
                V3RelationRow("军镇", state.relations.garrison)
            }
            V3CouncilPanel(state, controller)
            V3Panel {
                Text("路线", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                V3Content.routePlans.sortedByDescending { state.routeScores[it.route] ?: 0 }.take(4).forEach { plan ->
                    V3RouteProgressRow(plan.route.label, state.routeScores[plan.route] ?: 0, selected = plan.route == ending.route)
                    Text("· ${plan.goal}", color = V3Muted, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
        }
        "天下" -> V3WorldPanel(state, controller)
        "军务" -> V3Panel {
            Text("军务与举旗", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            val recruitUnlocked = V3GameEngine.isUnlocked(state, "Recruit")
            val advancedUnlocked = V3GameEngine.isUnlocked(state, "AdvancedTroops")
            val conquestUnlocked = V3GameEngine.isUnlocked(state, "Conquest")
            val bannerUnlocked = V3GameEngine.isUnlocked(state, "RaiseBanner")
            Text("兵册 ${state.army.total()} · 乡勇 ${state.army.militia} · 枪${state.army.spear} 弓${state.army.archer} 盾${state.army.shield} 骑${state.army.cavalry}。", color = V3Ink, fontSize = 13.sp, lineHeight = 19.sp)
            Text("解锁：募兵 ${if (recruitUnlocked) "已开" else "小族/寨堡"} · 精兵 ${if (advancedUnlocked) "已开" else "望族+团练营"} · 征伐 ${if (conquestUnlocked) "已开" else "望族+控制地域"} · 举旗 ${if (bannerUnlocked) "已开" else "县中大姓+兵80"}", color = V3Muted, fontSize = 11.sp, lineHeight = 16.sp)
            V3TroopType.entries.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { type ->
                        val enabled = type == V3TroopType.Militia || advancedUnlocked || (type == V3TroopType.Cavalry && state.clanRank >= 4)
                        V3SmallButton("募${type.label}×5", Modifier.weight(1f), enabled = recruitUnlocked && enabled) { controller.recruitTroops(type, 5) }
                    }
                    repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Text("装备与兵册", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("兵册决定每名将领能带的部曲规模；装备只对已穿戴的族人计入战攻与防御。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
            Text("库存：${state.equipment.count { it.ownerId == null }} 件 · 已装备：${state.equipment.count { it.ownerId != null }} 件", color = V3Ink, fontSize = 13.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                com.daming.fushengzhi3.v3.data.V3EquipmentSlot.entries.forEach { slot ->
                    V3SmallButton("购${slot.label}", Modifier.weight(1f)) { controller.buyEquipment(slot, com.daming.fushengzhi3.v3.data.V3EquipmentQuality.Common) }
                }
            }
            state.equipment.filter { it.ownerId == null }.take(6).forEach { item ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${item.name} · 攻${item.attack}/防${item.defense}", color = V3Ink, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    V3SmallButton("修复", Modifier.width(58.dp), enabled = item.durability < item.maxDurability) { controller.repairEquipment(item.id) }
                    V3SmallButton("给武将", Modifier.width(72.dp), enabled = V3GameEngine.adultPeople(state).isNotEmpty()) { controller.equipEquipment(item.id, V3GameEngine.adultPeople(state).first().id) }
                }
            }
        }
        else -> V3Panel {
            Text("近事", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            state.eventLog.take(6).ifEmpty { listOf("族谱新启，尚无大事入册。") }.forEach { Text("· $it", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp) }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        V3SmallButton("设置", Modifier.weight(1f), onClick = controller::openSettings)
        V3SmallButton("族老札记", Modifier.weight(1f), onClick = openGuide)
    }
}

@Composable
private fun V3CouncilPanel(state: V3GameState, controller: V3GameController) {
    val usedThisMonth = state.eventLog.any { it.startsWith("${state.year}年${state.month}月 · 宗族议事") }
    V3Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("宗族议事", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(if (usedThisMonth) "本月已议" else "每月一议", color = if (usedThisMonth) V3Muted else V3Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text("不是只看数值：每月在祠堂议定一项家政方略，会真实改变银粮、族望、凝聚、地方关系、路线倾向和兵册。", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
        val agendas = listOf(
            Triple("granary", "开仓平粜", "银18 → 粮+42 / 民心"),
            Triple("trade", "重整商账", "粮18 → 银+46 / 商帮"),
            Triple("ritual", "修谱祭祖", "银24粮12 → 凝聚+6"),
            Triple("drill", "团练巡夜", "银28粮34 → 乡勇+12"),
            Triple("study", "延师讲学", "银30粮8 → 族望+4"),
            Triple("relief", "赈济乡邻", "银12粮32 → 民心+6")
        )
        agendas.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                row.forEach { (id, label, desc) ->
                    V3SmallButton("$label\n$desc", Modifier.weight(1f), enabled = !usedThisMonth) { controller.holdCouncil(id) }
                }
                repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun V3WorldPanel(state: V3GameState, controller: V3GameController) {
    var selectedRegionId by remember { mutableStateOf<String?>(null) }
    val selectedRegion = selectedRegionId?.let { id -> state.worldRegions.firstOrNull { it.id == id } }
    V3Panel {
        Text("天下地图", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("统一进度 ${state.unificationProgress} · 控制地域 ${V3GameEngine.controlledRegionCount(state)}/${state.worldRegions.size}。点地图节点弹出结交、经营、征伐。", color = V3Ink, fontSize = 13.sp, lineHeight = 19.sp)
        V3WorldVisualMap(state) {
            controller.pauseForPlayerAction()
            selectedRegionId = it
        }
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
                V3SmallButton("征伐", Modifier.weight(1f), enabled = state.conquestState == null && V3GameEngine.isUnlocked(state, "Conquest")) { controller.startConquest(region.id) }
            }
            V3SmallButton("关闭", Modifier.fillMaxWidth(), selected = true, onClick = onDismiss)
        }
    }
}

@Composable
private fun V3WorldVisualMap(state: V3GameState, onSelect: (String) -> Unit) {
    var pan by remember { mutableStateOf(Offset.Zero) }
    BoxWithConstraints(Modifier.fillMaxWidth().height(430.dp).background(V3Rice, V3SoftShape).clip(V3SoftShape)) {
        val density = LocalDensity.current
        val frameWidthPx = with(density) { maxWidth.toPx() }
        val frameHeightPx = with(density) { maxHeight.toPx() }
        val mapWidthPx = frameWidthPx * 1.55f
        val mapHeightPx = frameHeightPx * 1.70f
        val mapWidth = with(density) { mapWidthPx.toDp() }
        val mapHeight = with(density) { mapHeightPx.toDp() }
        val minPanX = frameWidthPx - mapWidthPx
        val minPanY = frameHeightPx - mapHeightPx
        val boundedPan = Offset(pan.x.coerceIn(minPanX, 0f), pan.y.coerceIn(minPanY, 0f))
        Box(Modifier.fillMaxSize().pointerInput(mapWidthPx, mapHeightPx) {
            detectDragGestures { _, dragAmount ->
                pan = Offset((pan.x + dragAmount.x).coerceIn(minPanX, 0f), (pan.y + dragAmount.y).coerceIn(minPanY, 0f))
            }
        }) {
            Box(Modifier.size(width = mapWidth, height = mapHeight).graphicsLayer { translationX = boundedPan.x; translationY = boundedPan.y }) {
                AssetImage(GameImages.V3WorldMap, null, Modifier.fillMaxSize(), ContentScale.FillBounds, alpha = 0.96f)
                Canvas(Modifier.matchParentSize()) {
                    fun point(x: Float, y: Float) = expandedMapPoint(Offset(x, y)).let { Offset(size.width * it.x, size.height * it.y) }
                    fun route(ax: Float, ay: Float, bx: Float, by: Float, alpha: Float = 0.48f) {
                        drawLine(V3Red.copy(alpha = alpha), point(ax, ay), point(bx, by), strokeWidth = 4f, cap = StrokeCap.Square)
                    }
                    route(0.10f, 0.73f, 0.26f, 0.72f)
                    route(0.26f, 0.72f, 0.39f, 0.58f)
                    route(0.26f, 0.72f, 0.18f, 0.43f)
                    route(0.39f, 0.58f, 0.43f, 0.39f)
                    route(0.39f, 0.58f, 0.66f, 0.62f)
                    route(0.43f, 0.39f, 0.63f, 0.40f)
                    route(0.63f, 0.40f, 0.67f, 0.25f)
                    route(0.66f, 0.62f, 0.83f, 0.52f)
                    route(0.67f, 0.25f, 0.79f, 0.25f)
                    route(0.79f, 0.25f, 0.88f, 0.12f)
                    route(0.79f, 0.25f, 0.48f, 0.10f, 0.58f)
                    route(0.83f, 0.52f, 0.48f, 0.10f, 0.40f)
                }
                state.worldRegions.forEach { region ->
                    V3WorldRegionPin(region, worldWidthPx = mapWidthPx, worldHeightPx = mapHeightPx, onClick = { onSelect(region.id) })
                }
            }
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
        Box(Modifier.size(72.dp).background(V3Paper.copy(alpha = 0.24f), V3SoftShape).padding(2.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.matchParentSize().background(color.copy(alpha = 0.32f), V3SoftShape))
            if (icon != null) {
                AssetImage(icon, region.name, Modifier.size(68.dp), ContentScale.Fit)
            }
        }
        Column(
            Modifier.fillMaxWidth().background(V3Paper.copy(alpha = 0.92f), V3SoftShape).padding(horizontal = 4.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(region.name, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
            Text(region.status.label, color = V3Ink, fontSize = 9.sp, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

private fun worldMapPoint(regionId: String): Offset = expandedMapPoint(when (regionId) {
    "home_county" -> Offset(0.10f, 0.73f)
    "neighbor_county" -> Offset(0.26f, 0.72f)
    "river_prefecture" -> Offset(0.39f, 0.58f)
    "mountain_prefecture" -> Offset(0.18f, 0.43f)
    "lake_province" -> Offset(0.43f, 0.39f)
    "coast_province" -> Offset(0.66f, 0.62f)
    "south_province" -> Offset(0.63f, 0.40f)
    "shandong_corridor" -> Offset(0.67f, 0.25f)
    "liaodong_front" -> Offset(0.88f, 0.12f)
    "north_capital" -> Offset(0.79f, 0.25f)
    "jiangsea_gate" -> Offset(0.83f, 0.52f)
    "all_realm" -> Offset(0.48f, 0.10f)
    else -> Offset(0.50f, 0.50f)
})

@Composable
private fun V3CountyMapView(state: V3GameState, onSelectSite: (String) -> Unit) {
    var pan by remember { mutableStateOf(Offset.Zero) }
    val frameHeight = 560.dp
    val frameShape = V3SoftShape
    val density = LocalDensity.current
    V3Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("县域地图", color = V3Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("点建筑弹出管理", color = V3Muted, fontSize = 12.sp)
        }
        BoxWithConstraints(
            Modifier.fillMaxWidth().height(frameHeight).clip(frameShape).background(V3Rice, frameShape)
        ) {
            val frameWidthPx = with(density) { maxWidth.toPx() }
            val frameHeightPx = with(density) { maxHeight.toPx() }
            val mapWidthPx = frameWidthPx * 1.48f
            val mapHeightPx = maxOf(frameHeightPx * 1.60f, mapWidthPx * 1840f / 1184f)
            val mapWidth = with(density) { mapWidthPx.toDp() }
            val mapHeight = with(density) { mapHeightPx.toDp() }
            val minPanX = frameWidthPx - mapWidthPx
            val minPanY = frameHeightPx - mapHeightPx
            val boundedPan = Offset(pan.x.coerceIn(minPanX, 0f), pan.y.coerceIn(minPanY, 0f))
            Box(
                Modifier.fillMaxSize().pointerInput(minPanX, minPanY) {
                    detectDragGestures { _, dragAmount ->
                        pan = Offset((pan.x + dragAmount.x).coerceIn(minPanX, 0f), (pan.y + dragAmount.y).coerceIn(minPanY, 0f))
                    }
                }
            ) {
                Box(Modifier.size(width = mapWidth, height = mapHeight).graphicsLayer { translationX = boundedPan.x; translationY = boundedPan.y }) {
                    AssetImage(GameImages.V3MapBgPlain, null, Modifier.fillMaxSize(), ContentScale.FillBounds)
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
    val markerColor = when {
        site.risk >= 60 -> V3Red
        site.control >= 55 -> V3Green
        site.level > 0 -> V3Gold
        else -> V3Muted
    }
    Column(
        Modifier.graphicsLayer {
            translationX = x
            translationY = y
        }.width(92.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(Modifier.size(70.dp), contentAlignment = Alignment.Center) {
            AssetImage(icon, site.name, Modifier.size(66.dp), ContentScale.Fit)
        }
        Text(site.name, color = markerColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.background(V3Rice.copy(alpha = 0.88f), V3SoftShape).padding(horizontal = 5.dp, vertical = 3.dp))
    }
}

private fun siteMapPoint(siteId: String): Offset = expandedMapPoint(when (siteId) {
    "fort" -> Offset(0.18f, 0.10f)
    "yamen" -> Offset(0.58f, 0.18f)
    "academy" -> Offset(0.82f, 0.30f)
    "shrine" -> Offset(0.42f, 0.42f)
    "farmland" -> Offset(0.18f, 0.56f)
    "market" -> Offset(0.72f, 0.60f)
    "clinic" -> Offset(0.34f, 0.74f)
    "dock" -> Offset(0.80f, 0.86f)
    "mountain_pass" -> Offset(0.18f, 0.94f)
    else -> Offset(0.50f, 0.50f)
})

@Composable
private fun V3SiteManageDialog(site: V3CountySite, state: V3GameState, controller: V3GameController, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        V3ImagePanel(
            GameImages.V3UiEventPanel,
            Modifier
                .widthIn(max = 470.dp)
                .heightIn(max = 680.dp)
                .verticalScroll(rememberScrollState())
        ) {
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
        Text(siteSpecialHint(site), color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
        V3SmallButton(siteSpecialButtonLabel(site), Modifier.fillMaxWidth(), enabled = site.level > 0) { controller.siteSpecialAction(site.id) }
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Box(Modifier.size(78.dp).background(V3Rice, CircleShape).border(3.dp, V3Gold, CircleShape).padding(4.dp), contentAlignment = Alignment.Center) {
                AssetImage(v3AvatarFor(person), person.name, Modifier.size(70.dp), ContentScale.Fit)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(person.name, color = V3Ink, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Text("第${person.generation}世 · ${person.gender.label} · ${person.branch} · ${person.identity} · ${person.age}岁", color = V3Red, fontSize = 12.sp)
                    }
                    val assignedSite = person.assignedSiteId?.let { id -> state.sites.firstOrNull { it.id == id } }
                    val titleBits = listOfNotNull(person.officeRank, person.militaryRank).joinToString(" · ")
                    Text(if (person.currentTask == null && person.trainingFocus == null) "待命${if (titleBits.isBlank()) "" else " · $titleBits"}" else "${person.currentTask?.label ?: person.trainingFocus?.label}@${assignedSite?.name ?: "家中"}", color = if (person.currentTask == null && person.trainingFocus == null) V3Muted else V3Green, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                }
                Text("${person.trait.label}：${person.trait.desc}", color = V3Muted, fontSize = 12.sp, lineHeight = 17.sp)
                Text("阶段：${V3GameEngine.lifeStage(person)} · ${V3GameEngine.marriageStatus(person, state)}", color = V3Red, fontSize = 12.sp, lineHeight = 17.sp)
                if (person.age < 16) {
                    Text("距离成年约 ${V3GameEngine.monthsUntilAdult(person)} 个月；1倍速每月推进1个月。", color = V3Muted, fontSize = 11.sp)
                }
            }
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
    Column(Modifier.fillMaxWidth().background(V3Rice.copy(alpha = 0.96f)).padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(state.clanName, color = V3Gold, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("${mingEraLabel(state.year)}${state.month}月 · ${V3GameEngine.clanRankName(state)} · ${state.crisis}", color = V3Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
        Text(if (controller.shouldAutoTick()) "时序流动中" else "已暂停", color = V3Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1.4f))
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
    Row(Modifier.fillMaxWidth().background(V3Rice.copy(alpha = 0.97f)).padding(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        V3Screen.entries.forEach { screen ->
            V3SmallButton(V3GameEngine.screenTitle(screen), Modifier.weight(1f), selected = controller.screen == screen) { controller.switchScreen(screen) }
        }
    }
}

@Composable
private fun V3Background(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(V3Bg)) {
        AssetImage(GameImages.V3DossierBg, null, Modifier.fillMaxSize(), ContentScale.Crop, alpha = 0.32f)
        Box(Modifier.fillMaxSize().background(Color(0x99F4E0B5)))
        content()
    }
}

@Composable
private fun V3Title(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, color = V3Gold, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = V3Muted, fontSize = 15.sp)
    }
}

@Composable
private fun V3Section(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, color = V3Gold, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = V3Muted, fontSize = 13.sp, lineHeight = 19.sp)
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(V3Red))
    }
}

@Composable
private fun V3Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = V3Paper), border = BorderStroke(2.dp, V3Gold), shape = V3PanelShape) {
        Box(Modifier.background(V3Paper.copy(alpha = 0.98f))) {
            V3CornerOrnaments()
            Box(Modifier.matchParentSize().padding(5.dp).border(1.dp, V3Red.copy(alpha = 0.34f), V3SoftShape))
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
        }
    }
}

@Composable
private fun V3ImagePanel(imagePath: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = V3Paper), border = BorderStroke(2.dp, V3Gold), shape = V3PanelShape) {
        Box {
            AssetImage(imagePath, null, Modifier.matchParentSize(), ContentScale.Crop, alpha = 0.55f)
            Box(Modifier.matchParentSize().background(Color(0xDDF8EBCB)))
            V3CornerOrnaments()
            Box(Modifier.matchParentSize().padding(6.dp).border(1.dp, V3Red.copy(alpha = 0.35f), V3SoftShape))
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(9.dp), content = content)
        }
    }
}

@Composable
private fun V3CornerOrnaments() {
    Box(Modifier.fillMaxSize()) {
        Text("◆", color = V3Gold.copy(alpha = 0.82f), fontSize = 11.sp, modifier = Modifier.align(Alignment.TopStart).padding(7.dp))
        Text("◆", color = V3Gold.copy(alpha = 0.82f), fontSize = 11.sp, modifier = Modifier.align(Alignment.TopEnd).padding(7.dp))
        Text("◆", color = V3Gold.copy(alpha = 0.82f), fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomStart).padding(7.dp))
        Text("◆", color = V3Gold.copy(alpha = 0.82f), fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(7.dp))
    }
}

@Composable
private fun V3CompactSelector(title: String, values: List<String>, selected: String, effect: (String) -> String, onSelect: (String) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = V3Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(effect(selected), color = V3Muted, fontSize = 10.sp, maxLines = 1)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            values.take(6).forEach { value ->
                V3SmallButton(value, Modifier.weight(1f), selected = value == selected) { onSelect(value) }
            }
        }
    }
}

private fun createRootEffect(value: String): String = when (value) {
    "寒门佃户" -> "基础银 58 · 粮 120 · 族望 6 · 乡勇 3"
    "没落士族" -> "基础银 76 · 粮 95 · 族望 14 · 乡勇 3"
    "边地军户" -> "基础银 68 · 粮 100 · 族望 8 · 乡勇 12"
    "江南商族" -> "基础银 110 · 粮 80 · 族望 10 · 乡勇 3"
    "山中堡寨" -> "基础银 74 · 粮 110 · 族望 6 · 乡勇 12"
    else -> "基础银 70 · 粮 95 · 族望 6 · 乡勇 3"
}

private fun createCreedEffect(value: String): String = when (value) {
    "耕读传家" -> "耕读路线 +12"
    "重商逐利" -> "富商路线 +12"
    "聚族自保" -> "自保路线 +12"
    "忠君报国" -> "勤王路线 +12"
    "开海远行" -> "海外路线 +12"
    else -> "避祸路线 +12"
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
    Column(modifier.background(V3PaperDeep, V3SoftShape).padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = V3Muted, fontSize = 11.sp)
        Text(value.toString(), color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun V3ResourceMetric(iconPath: String, label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier.background(V3PaperDeep.copy(alpha = 0.78f), CircleShape).padding(horizontal = 7.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AssetImage(iconPath, label, Modifier.size(20.dp), ContentScale.Fit)
        Column(horizontalAlignment = Alignment.Start) {
            Text(value.toString(), color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(label, color = V3Muted, fontSize = 8.sp, maxLines = 1)
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
    Column(Modifier.fillMaxWidth().background(if (reached) V3Green.copy(alpha = 0.16f) else V3PaperDeep, V3SoftShape).padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(goal.title, color = V3Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(if (reached) "已成" else "${progress}/${goal.target}", color = if (reached) V3Green else V3Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text(goal.desc, color = V3Muted, fontSize = 12.sp)
    }
}

@Composable
private fun V3Button(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(V3Red, V3ButtonShape)
            .border(2.dp, V3Gold, V3ButtonShape)
            .clickable(onClick = onClick)
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.matchParentSize().border(1.dp, V3Rice.copy(alpha = 0.52f), V3SoftShape))
        Text("❖ $text ❖", color = V3Rice, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp))
    }
}

@Composable
private fun V3SmallButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, selected: Boolean = false, onClick: () -> Unit) {
    val bg = when {
        selected -> V3Red
        enabled -> V3PaperDeep
        else -> V3Muted.copy(alpha = 0.28f)
    }
    val fg = when {
        selected -> V3Rice
        enabled -> V3Ink
        else -> V3Muted.copy(alpha = 0.75f)
    }
    val edge = when {
        selected -> V3Gold
        enabled -> V3Border.copy(alpha = 0.72f)
        else -> V3Muted.copy(alpha = 0.35f)
    }
    Box(
        modifier = modifier
            .background(bg, V3SoftShape)
            .border(1.dp, edge, V3SoftShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.matchParentSize().border(1.dp, if (selected) V3Rice.copy(alpha = 0.35f) else V3Gold.copy(alpha = 0.25f), V3SoftShape))
        Text(
            text,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
            color = fg,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp
        )
    }
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
                        Card(modifier = Modifier.fillMaxWidth().clickable { controller.chooseEvent(choice) }, colors = CardDefaults.cardColors(containerColor = V3PaperDeep), border = BorderStroke(2.dp, V3Red), shape = V3SoftShape) {
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
private fun V3BattleDialog(state: V3GameState, battle: V3BattleState, controller: V3GameController) {
    Dialog(onDismissRequest = {}) {
        V3ImagePanel(GameImages.V3UiBattleReport, Modifier.widthIn(max = 540.dp)) {
            Text("军务出征 · ${battle.target}", color = V3Red, fontSize = 21.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text("敌势 ${battle.enemyPower} · 风险 ${battle.risk} · 第${battle.turn + 1}阵。${if (battle.turn % 2 == 0) "我方先手" else "敌方先手"}", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
            if (battle.phase == V3BattlePhase.Draft) {
                Text("先点选最多6名成年族人，确认后进入战斗。战斗界面只显示上下两阵，不再显示候选人。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
                    Text("已选 ${battle.selectedPersonIds.size}/6 · 每名族人必须配置一个兵种；兵册只是总部曲，出战时按6名将领均分所选兵种编制。", color = V3Red, fontSize = 12.sp, lineHeight = 18.sp)
                    battle.selectedPersonIds.forEach { id ->
                        val person = state.people.firstOrNull { it.id == id } ?: return@forEach
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                            AssetImage(v3AvatarFor(person), person.name, Modifier.size(34.dp).background(V3Rice, CircleShape).border(1.dp, V3Gold, CircleShape).padding(2.dp), ContentScale.Fit)
                            Text(person.name, color = V3Ink, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            V3TroopType.entries.forEach { troop ->
                                V3SmallButton(troop.label, Modifier.weight(1f), selected = battle.selectedTroops[id] == troop, enabled = state.army.count(troop) > 0) { controller.selectBattleTroop(id, troop) }
                            }
                        }
                    }
                Box(Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        V3GameEngine.adultPeople(state).chunked(2).forEach { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                row.forEach { person ->
                                    val selectedPerson = battle.selectedPersonIds.contains(person.id)
                                    V3SmallButton("${person.name} 武${person.martial} 谋${person.diplomacy} 功${person.merit}", Modifier.weight(1f), selected = selectedPerson) { controller.selectBattlePerson(person.id) }
                                }
                                repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    V3SmallButton("确认出战", Modifier.weight(1f), selected = true, enabled = battle.selectedPersonIds.isNotEmpty()) { controller.confirmBattleLineup() }
                    V3SmallButton("暂缓", Modifier.weight(1f)) { controller.cancelBattle() }
                }
            } else {
                val latestHit = battle.roundLog.firstOrNull()
                Text("敌阵", color = V3Red, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                V3BattleGrid(battle.enemies, state, enemy = true, recentHitName = latestHit?.defender, recentHitDamage = latestHit?.damage)
                latestHit?.let { latest ->
                    Text("-${latest.damage}  ${latest.defender}", color = V3Red, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                Text("我阵", color = V3Green, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                V3BattleGrid(battle.allies, state, enemy = false, recentHitName = latestHit?.defender, recentHitDamage = latestHit?.damage)
                if (battle.roundLog.isNotEmpty()) {
                    Text("战报", color = V3Red, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    battle.roundLog.take(5).forEach { round -> Text("· ${round.text}", color = V3Ink, fontSize = 11.sp, lineHeight = 16.sp) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    V3SmallButton(if (battle.finished) "收兵结算" else if (battle.turn % 2 == 0) "我方先手" else "敌方先手", Modifier.weight(1f), selected = true) {
                        if (battle.finished) controller.finalizeBattle() else controller.advanceBattleRound()
                    }
                    V3SmallButton("自动打完", Modifier.weight(1f), enabled = !battle.finished) { controller.resolveBattle() }
                    V3SmallButton("撤出", Modifier.weight(1f), enabled = !battle.finished) { controller.cancelBattle() }
                }
            }
        }
    }
}

@Composable
private fun V3BattleGrid(fighters: List<V3Combatant>, state: V3GameState, enemy: Boolean, recentHitName: String? = null, recentHitDamage: Int? = null) {
    fighters.chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row.forEach { fighter ->
                val person = fighter.personId?.let { id -> state.people.firstOrNull { it.id == id } }
                val enemyIndex = ((fighter.name.hashCode() and Int.MAX_VALUE) % GameImages.v3EnemyPortraits.size)
                val enemyAvatar = GameImages.v3EnemyPortraits.getOrNull(enemyIndex)
                V3CombatantCard(
                    fighter = fighter,
                    modifier = Modifier.weight(1f),
                    enemy = enemy,
                    avatarPath = if (enemy) enemyAvatar else person?.let { v3AvatarFor(it) },
                    highlighted = fighter.name == recentHitName,
                    floatingDamage = if (fighter.name == recentHitName) recentHitDamage else null
                )
            }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun V3CombatantCard(
    fighter: V3Combatant,
    modifier: Modifier = Modifier,
    enemy: Boolean,
    avatarPath: String? = null,
    highlighted: Boolean = false,
    floatingDamage: Int? = null
) {
    val hpRatio = if (fighter.maxHp <= 0) 0f else fighter.hp.toFloat() / fighter.maxHp.toFloat()
    val shake = remember { Animatable(0f) }
    val lift = remember { Animatable(0f) }
    LaunchedEffect(highlighted, floatingDamage, fighter.hp) {
        if (highlighted) {
            shake.snapTo(0f)
            lift.snapTo(0f)
            shake.animateTo(
                0f,
                keyframes {
                    durationMillis = 420
                    0f at 0
                    -7f at 50
                    7f at 100
                    -5f at 150
                    5f at 210
                    0f at 300
                }
            )
            lift.animateTo(
                1f,
                keyframes {
                    durationMillis = 520
                    0f at 0
                    1f at 520
                }
            )
        }
    }
    Box(modifier.graphicsLayer { translationX = shake.value }) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(if (highlighted) V3Red.copy(alpha = 0.16f) else if (enemy) V3PaperDeep else V3Rice, V3SoftShape)
                .border(if (highlighted) 2.dp else 1.dp, if (highlighted) V3SealRed else if (enemy) V3Red else V3Green, V3SoftShape)
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (avatarPath != null) {
                AssetImage(
                    avatarPath,
                    fighter.name,
                    Modifier
                        .size(34.dp)
                        .graphicsLayer {
                            if (highlighted) {
                                scaleX = 1.08f
                                scaleY = 1.08f
                            }
                        },
                    ContentScale.Fit
                )
            } else {
                Box(Modifier.size(34.dp).background(if (enemy) V3Red.copy(alpha = 0.18f) else V3Green.copy(alpha = 0.18f), CircleShape), contentAlignment = Alignment.Center) {
                    Text(if (enemy) "敌" else "兵", color = if (enemy) V3Red else V3Green, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(fighter.name, color = if (enemy) V3Red else V3Green, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("${fighter.role} · ${fighter.troopType?.label ?: "敌军"} ${fighter.troopCount}人 · 战${fighter.power} 防${fighter.defense}", color = V3Ink, fontSize = 10.sp, maxLines = 1)
            Box(Modifier.fillMaxWidth().height(5.dp).background(V3Border.copy(alpha = 0.25f), V3SoftShape)) {
                Box(Modifier.fillMaxWidth(hpRatio.coerceIn(0.03f, 1f)).height(5.dp).background(if (enemy) V3Red else V3Green, V3SoftShape))
            }
            Text("血 ${fighter.hp}/${fighter.maxHp}", color = V3Muted, fontSize = 9.sp, maxLines = 1)
        }
        floatingDamage?.let { damage ->
            Text(
                "-$damage",
                color = V3SealRed,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer {
                        translationY = -18f * lift.value
                        alpha = 1f - lift.value * 0.45f
                    }
                    .background(V3Rice.copy(alpha = 0.82f), CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
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

private fun siteSpecialButtonLabel(site: V3CountySite): String = when (site.type) {
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Shrine -> "开祠修谱"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Farmland -> "抢修水渠"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Market -> "开设牙行"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Yamen -> "打点税册"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Academy -> "举行讲会"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Clinic -> "开设义诊"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Fort -> "点验乡勇"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Dock -> "开走海货"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.MountainPass -> "山道设卡"
}

private fun siteSpecialHint(site: V3CountySite): String = when (site.type) {
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Shrine -> "专属事务：消耗粮食，提升凝聚和族望。"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Farmland -> "专属事务：花银修渠，换取大量粮食。"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Market -> "专属事务：消耗粮食换银两和商帮关系。"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Yamen -> "专属事务：花银打点官府，缓冲税役压力。"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Academy -> "专属事务：开讲会提升士绅、族望和耕读路线。"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Clinic -> "专属事务：义诊压疫病，提高乡民与凝聚。"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Fort -> "专属事务：消耗银粮，快速增加乡勇。"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.Dock -> "专属事务：走海货获银，推进海外路线但损官府。"
    com.daming.fushengzhi3.v3.data.V3CountySiteType.MountainPass -> "专属事务：设卡压流寇，推进割据路线。"
}

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
