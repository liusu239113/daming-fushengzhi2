package com.arktools.daming.v3.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.arktools.daming.ads.RewardedAdController
import com.arktools.daming.ads.SpeedPassStore
import com.arktools.daming.data.GameImages
import com.arktools.daming.ui.components.AssetImage
import com.arktools.daming.ui.theme.FontPreference
import com.arktools.daming.ui.theme.FontStyleKey
import com.arktools.daming.v3.data.V3ActiveEvent
import com.arktools.daming.v3.data.V3BattleState
import com.arktools.daming.v3.data.V3BattlePhase
import com.arktools.daming.v3.data.V3Combatant
import com.arktools.daming.v3.data.V3AnnualGoal
import com.arktools.daming.v3.data.V3Content
import com.arktools.daming.v3.data.V3CountySite
import com.arktools.daming.v3.data.V3CountySiteType
import com.arktools.daming.v3.data.V3EventChoice
import com.arktools.daming.v3.data.V3EquipmentQuality
import com.arktools.daming.v3.data.V3EquipmentSlot
import com.arktools.daming.v3.data.V3EstateType
import com.arktools.daming.v3.data.V3FinalEnding
import com.arktools.daming.v3.data.V3GameState
import com.arktools.daming.v3.data.V3Gender
import com.arktools.daming.v3.data.V3Person
import com.arktools.daming.v3.data.V3Route
import com.arktools.daming.v3.data.V3RegionStatus
import com.arktools.daming.v3.data.V3Screen
import com.arktools.daming.v3.data.V3SiteYield
import com.arktools.daming.v3.data.V3TaskType
import com.arktools.daming.v3.data.V3TrainingType
import com.arktools.daming.v3.data.V3TroopType
import com.arktools.daming.v3.data.V3WorldRegion
import com.arktools.daming.v3.logic.V3GameController
import com.arktools.daming.v3.logic.V3GameEngine
import kotlinx.coroutines.delay

private val V3Ink = Color(0xFFFFF1D2)
private val V3Paper = Color(0xF0181511)
private val V3PaperDeep = Color(0xF229241D)
private val V3Red = Color(0xFFE06B55)
private val V3Gold = Color(0xFFE2C17E)
private val V3Muted = Color(0xFFD1C4AD)
private val V3Green = Color(0xFFA8D29C)
private val V3Blue = Color(0xFFA5C9CA)
private val V3Bg = Color(0xFF0E0D0B)
private val V3Border = Color(0xFFB89A62)
private val V3SealRed = Color(0xFFF07A61)
private val V3Rice = Color(0xFF171511)
private val V3SoftShape = RoundedCornerShape(16.dp)
private val V3PanelShape = RoundedCornerShape(20.dp)
private val V3ButtonShape = RoundedCornerShape(14.dp)

@Composable
fun V3CreateScreen(controller: V3GameController, onBack: () -> Unit, onStart: () -> Unit) {
    LaunchedEffect(Unit) { controller.ensureV3Bgm() }
    var surname by remember { mutableStateOf("李") }
    var givenName by remember { mutableStateOf("慎行") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var root by remember { mutableStateOf("没落士族") }
    var county by remember { mutableStateOf("江南水乡") }
    var creed by remember { mutableStateOf("耕读传家") }
    var crisis by remember { mutableStateOf("官府催税") }

    val profile = V3Content.startProfile(root, county, creed, crisis)

    V3Background(GameImages.MingyunClanBg) {
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
                        Text("姓氏", color = V3Red, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(54.dp))
                        TextField(
                            value = surname,
                            onValueChange = {
                                surname = it.trim().filter { char -> char in '\u3400'..'\u9FFF' }.take(2)
                                nameError = when {
                                    surname.isBlank() -> "请填写一至二字姓氏。"
                                    givenName.isBlank() -> "请填写一至四字名字。"
                                    V3Content.isBlockedName(surname + givenName) -> "这个名字涉及近现代人物或历史英雄称谓，请换一个。"
                                    else -> null
                                }
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = V3Rice,
                                unfocusedContainerColor = V3Rice,
                                focusedTextColor = V3Ink,
                                unfocusedTextColor = V3Ink,
                                focusedIndicatorColor = V3Gold,
                                unfocusedIndicatorColor = V3Border,
                                cursorColor = V3Ink
                            ),
                            modifier = Modifier.weight(1f).height(56.dp)
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("名字", color = V3Red, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(54.dp))
                        TextField(
                            value = givenName,
                            onValueChange = {
                                givenName = it.trim().filter { char -> char in '\u3400'..'\u9FFF' }.take(4)
                                nameError = when {
                                    surname.isBlank() -> "请填写一至二字姓氏。"
                                    givenName.isBlank() -> "请填写一至四字名字。"
                                    V3Content.isBlockedName(surname + givenName) -> "这个名字涉及近现代人物或历史英雄称谓，请换一个。"
                                    else -> null
                                }
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = V3Rice,
                                unfocusedContainerColor = V3Rice,
                                focusedTextColor = V3Ink,
                                unfocusedTextColor = V3Ink,
                                focusedIndicatorColor = V3Gold,
                                unfocusedIndicatorColor = V3Border,
                                cursorColor = V3Ink
                            ),
                            modifier = Modifier.weight(1f).height(56.dp)
                        )
                    }
                    nameError?.let { Text(it, color = V3Red, fontSize = 12.sp, lineHeight = 17.sp) }
                    Text(
                        "宗族：${V3Content.clanName(surname)} · 开族祖：${V3Content.founderName(surname, givenName)}",
                        color = V3Gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 19.sp
                    )
                    Text(
                        "只填写一至二字姓氏，宗族名与开族祖姓名由系统生成；复姓会完整传给后代。",
                        color = V3Muted,
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )
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
                V3Button("开宗立户", Modifier.weight(1f), enabled = nameError == null) {
                    controller.newGame(root, county, creed, crisis, surname, givenName)
                    onStart()
                }
            }
        }
    }
}

private fun Modifier.horizontalMapDrag(onDrag: (Offset) -> Unit): Modifier =
    pointerInput(onDrag) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var previous = down.position
            var accumulated = Offset.Zero
            var draggingMap: Boolean? = null
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) break
                val delta = change.position - previous
                previous = change.position
                accumulated += delta
                if (draggingMap == null && accumulated.getDistance() >= viewConfiguration.touchSlop) {
                    draggingMap = kotlin.math.abs(accumulated.x) > kotlin.math.abs(accumulated.y)
                }
                if (draggingMap == true) {
                    change.consume()
                    onDrag(delta)
                } else if (draggingMap == false) {
                    break
                }
            }
        }
    }

private fun Modifier.freeMapDrag(onDrag: (Offset) -> Unit): Modifier =
    pointerInput(onDrag) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var previous = down.position
            var accumulated = Offset.Zero
            var dragging = false
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) break
                val delta = change.position - previous
                previous = change.position
                accumulated += delta
                if (!dragging && accumulated.getDistance() >= viewConfiguration.touchSlop) dragging = true
                if (dragging) {
                    change.consume()
                    onDrag(delta)
                }
            }
        }
    }

private fun Modifier.guideTarget(
    focus: V3GuideFocus,
    targets: MutableMap<V3GuideFocus, Rect>
): Modifier = onGloballyPositioned { coordinates ->
    targets[focus] = coordinates.boundsInRoot()
}

@Composable
fun V3GameScreen(controller: V3GameController, fontPreference: FontPreference, onBackToMenu: () -> Unit) {
    LaunchedEffect(Unit) { controller.ensureV3Bgm() }
    var secondsToNextMonth by remember { mutableStateOf(0) }
    LaunchedEffect(controller.timeSpeed, controller.state.year, controller.state.month, controller.latestReport, controller.message, controller.state.activeEvent, controller.settingsVisible, controller.state.examSession, controller.state.battleState, controller.state.conquestState) {
        if (controller.shouldAutoTick()) {
            var remainingMillis = monthIntervalMillis(controller.timeSpeed)
            secondsToNextMonth = ((remainingMillis + 999L) / 1000L).toInt()
            while (remainingMillis > 0L && controller.shouldAutoTick()) {
                val step = minOf(250L, remainingMillis)
                delay(step)
                remainingMillis -= step
                secondsToNextMonth = ((remainingMillis + 999L) / 1000L).toInt()
            }
            if (controller.shouldAutoTick()) controller.autoAdvanceTime()
        } else {
            secondsToNextMonth = 0
        }
    }
    val state = controller.state
    var confirmBackToMenu by remember { mutableStateOf(false) }
    var elderGuideVisible by remember { mutableStateOf(!state.tutorialCompleted) }
    var guideStrategyPage by remember { mutableStateOf<String?>(null) }
    val guideTargets = remember { mutableStateMapOf<V3GuideFocus, Rect>() }
    val contentScroll = rememberScrollState()
    val tutorialStep = state.tutorialStep.coerceIn(0, elderGuideSteps(state).lastIndex)
    val tutorialFocus = elderGuideSteps(state)[tutorialStep].focus
    LaunchedEffect(elderGuideVisible, tutorialStep, controller.screen, guideStrategyPage, guideTargets[tutorialFocus], contentScroll.maxValue) {
        if (!elderGuideVisible || state.tutorialCompleted) return@LaunchedEffect
        val bounds = guideTargets[tutorialFocus] ?: return@LaunchedEffect
        val targetScroll = (contentScroll.value + bounds.top - 190f)
            .toInt()
            .coerceIn(0, contentScroll.maxValue)
        contentScroll.animateScrollTo(targetScroll)
    }
    V3Background(controller.screen.backgroundAsset()) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                V3TopBar(
                    state,
                    controller,
                    onRequestBackToMenu = { confirmBackToMenu = true },
                    guideTargets = guideTargets,
                    secondsToNextMonth = secondsToNextMonth
                )
                Column(
                    Modifier.weight(1f).verticalScroll(contentScroll).padding(10.dp).widthIn(max = 760.dp).align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val ending = state.finalEnding
                    if (ending != null) {
                        V3EndingPage(ending, controller, onBackToMenu)
                    } else {
                        when (controller.screen) {
                            V3Screen.County -> V3HomePage(state, controller, guideTargets)
                            V3Screen.Clan -> V3ClanPage(state, controller, guideTargets)
                            V3Screen.People -> V3PeoplePage(state, controller, guideTargets)
                            V3Screen.Strategy -> V3StrategyPage(state, controller, forcedPage = guideStrategyPage, guideTargets = guideTargets, openGuide = {
                                guideStrategyPage = null
                                controller.reopenTutorial()
                                elderGuideVisible = true
                            })
                            V3Screen.Military -> V3MilitaryPage(state, controller)
                        }
                    }
                }
                if (state.finalEnding == null) {
                    V3BottomNav(controller, guideTargets)
                }
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
                    targetBounds = guideTargets[tutorialFocus],
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
private fun V3HomePage(
    state: V3GameState,
    controller: V3GameController,
    guideTargets: MutableMap<V3GuideFocus, Rect>
) {
    var selectedSiteId by remember { mutableStateOf<String?>(null) }
    val selectedSite = selectedSiteId?.let { id -> state.sites.firstOrNull { it.id == id } }
    val forecast = V3GameEngine.monthlyForecast(state)

    V3Section("家业", nextAdvice(state))
    V3ClanLedgerPanel(
        state,
        Modifier.guideTarget(V3GuideFocus.MonthlyLedger, guideTargets),
        onClick = { controller.observeTutorialLedger(); controller.showInfo("族中月账：人丁耗粮是所有活着的族人每月口粮；乡勇耗粮是兵册维护费；险地是风险达到55以上的地点。银粮收支会在每月结算时真正改变库存。") }
    )
    V3RouteOverviewPanel(state, controller)
    V3Panel {
        Text("本月账本", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(forecast.summary, color = V3Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            V3Metric("入银", forecast.silverIncome, V3Gold, Modifier.weight(1f))
            V3Metric("出银", forecast.silverExpense, V3Red, Modifier.weight(1f))
            V3Metric("入粮", forecast.grainIncome, V3Green, Modifier.weight(1f))
            V3Metric("出粮", forecast.grainExpense, V3Red, Modifier.weight(1f))
        }
        Text(
            "经营说明：田庄与佃田主产粮，集市、铺面和商队主产银；地点控制越高、风险越低，固定月产越多。人口和乡勇每月消耗粮食。",
            color = V3Muted,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
        V3SmallButton(
            "一键安排本月派遣与培养",
            Modifier
                .fillMaxWidth()
                .guideTarget(V3GuideFocus.AutoArrange, guideTargets),
            selected = true
        ) {
            controller.autoArrangeMonth()
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
    V3CountyMapView(
        state,
        Modifier.guideTarget(V3GuideFocus.CountyMap, guideTargets)
    ) {
        selectedSiteId = it
        controller.observeTutorialSite()
    }
    V3EstatePanel(state, controller)
    selectedSite?.let { site ->
        V3SiteManageDialog(site = site, state = state, controller = controller, onDismiss = { selectedSiteId = null })
    }
}

@Composable
private fun V3ClanLedgerPanel(state: V3GameState, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val peopleFood = V3GameEngine.alivePeople(state).map { if (it.age < 12) 1 else 3 }.sum()
    val militiaFood = state.militia / 8
    V3Panel(modifier.clickable(enabled = onClick != null) { onClick?.invoke() }) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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

private enum class V3GuideFocus {
    TopBar,
    TimeControls,
    Resources,
    MonthlyLedger,
    CountyMap,
    AutoArrange,
    Marriage,
    ClanPromotion,
    Genealogy,
    StrategyTabs,
    StrategyContent,
    WorldMap,
    BottomNav
}

private data class V3ElderGuideStep(
    val tab: V3Screen,
    val speaker: String,
    val portrait: String,
    val title: String,
    val words: String,
    val action: String,
    val focus: V3GuideFocus,
    val strategyPage: String? = null
)

private fun elderGuideSteps(state: V3GameState): List<V3ElderGuideStep> = listOf(
    V3ElderGuideStep(
        V3Screen.County,
        "沈账房",
        "male_scholar",
        "第一课 · 亲手核账",
        "家主，${state.crisis}当前，先别急着点继续。请点亮上方【族中月账】，看清人丁、乡勇与险地的消耗。只有亲手点过，这一课才算完成。",
        "请点击高亮的月账",
        V3GuideFocus.MonthlyLedger
    ),
    V3ElderGuideStep(
        V3Screen.County,
        "周管事",
        "male_middle",
        "第二课 · 打开田庄",
        "账看明白了，还要认得产业。请向下查看县域地图，亲手点开田庄或任意地点，再关闭管理窗口。地图会自动滚到眼前，不必盲找。",
        "请点击高亮的县域地图地点",
        V3GuideFocus.CountyMap
    ),
    V3ElderGuideStep(
        V3Screen.County,
        "周管事",
        "male_middle",
        "第三课 · 安排人手",
        "地点不会自己经营。请点【一键安排本月派遣与培养】，系统会按风险、银粮缺口和族人所长分配；你也可以去族人页手动安排。",
        "请完成一次派遣或培养",
        V3GuideFocus.AutoArrange
    ),
    V3ElderGuideStep(
        V3Screen.County,
        "族老",
        "male_elder",
        "第四课 · 推进月结",
        "差事已经排下，结果要到月结才兑现。请点顶栏【继续】或任意倍速，真正推进一个月。若弹出月报或事件，处理完后会恢复原来的倍率。",
        "请推进一个月",
        V3GuideFocus.TimeControls
    ),
    V3ElderGuideStep(
        V3Screen.County,
        "族老",
        "male_elder",
        "家业开卷",
        "你已亲手核账、开地图、派人和过月。往后记住：田庄与佃田养粮，集市、铺面与商队生银；先成家育人，再扩产业与宗族品第。等根基稳了，才轮得到县外天下。",
        "完成引导",
        V3GuideFocus.MonthlyLedger
    )
)

@Composable
private fun V3ElderGuideOverlay(
    state: V3GameState,
    controller: V3GameController,
    targetBounds: Rect?,
    onStrategyPageChange: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val steps = elderGuideSteps(state)
    val safeIndex = state.tutorialStep.coerceIn(0, steps.lastIndex)
    val step = steps[safeIndex]
    LaunchedEffect(safeIndex) {
        onStrategyPageChange(step.strategyPage)
        controller.switchScreen(step.tab)
        controller.playGuideTick()
    }
    Box(Modifier.fillMaxSize()) {
        V3GuideFocusFrame(targetBounds)
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
                        AssetImage(
                            GameImages.v3AvatarPortraits[step.portrait] ?: GameImages.v3AvatarPortraits.getValue("male_elder"),
                            step.speaker,
                            Modifier.size(52.dp),
                            ContentScale.Fit
                        )
                        Column {
                            Text(step.title, color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("${step.speaker} · ${state.clanName}", color = V3Muted, fontSize = 11.sp)
                        }
                    }
                    Text("任务 ${safeIndex + 1}/${steps.size}", color = V3Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(step.words, color = V3Ink, fontSize = 14.sp, lineHeight = 21.sp)
                Text(
                    if (safeIndex == steps.lastIndex) "前四项操作已完成，可收下族老札记。" else step.action,
                    color = V3Gold,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    V3SmallButton("跳过引导", Modifier.weight(1f)) {
                        controller.skipTutorial()
                        onDismiss()
                    }
                    if (safeIndex == steps.lastIndex) {
                        V3SmallButton("完成引导", Modifier.weight(2f), selected = true) {
                            controller.finishTutorial()
                            onDismiss()
                        }
                    } else {
                        V3SmallButton("等待完成当前操作", Modifier.weight(2f), enabled = false, selected = true) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun V3GuideFocusFrame(targetBounds: Rect?) {
    if (targetBounds == null) return
    val density = LocalDensity.current
    val paddingPx = with(density) { 6.dp.toPx() }
    val left = (targetBounds.left - paddingPx).coerceAtLeast(0f)
    val top = (targetBounds.top - paddingPx).coerceAtLeast(0f)
    val width = targetBounds.width + paddingPx * 2f
    val height = targetBounds.height + paddingPx * 2f
    Box(
        Modifier
            .graphicsLayer {
                translationX = left
                translationY = top
            }
            .size(
                width = with(density) { width.toDp() },
                height = with(density) { height.toDp() }
            )
            .background(Color(0x22FFF4D8), V3PanelShape)
            .border(3.dp, V3Gold, V3PanelShape)
    )
}

@Composable
private fun V3RouteOverviewPanel(state: V3GameState, controller: V3GameController? = null) {
    val dominant = V3GameEngine.dominantRoute(state)
    V3Panel {
        Text("当前主路线：${dominant.label}", color = V3Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("主路线不是一次性选择，而是你在议事、产业、培养、军务和事件中长期累积的倾向；最高分路线会影响建议与终局评价。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
        V3Content.routePlans.sortedByDescending { state.routeScores[it.route] ?: 0 }.take(4).forEach { plan ->
            V3RouteProgressRow(plan.route.label, state.routeScores[plan.route] ?: 0, selected = plan.route == dominant)
        }
        controller?.let { c ->
            V3SmallButton("查看路线规则", Modifier.fillMaxWidth()) { c.showInfo("路线分数来自地点经营、族人差事、宗族议事和随机事件。耕读偏书院与讲学，富商偏集市与商路，聚族自保偏祠堂与寨堡，勤王偏县衙与军镇，割据偏募勇与征伐，海外偏码头。分数最高者就是当前主路线。") }
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
private fun V3ClanPage(
    state: V3GameState,
    controller: V3GameController,
    guideTargets: MutableMap<V3GuideFocus, Rect>
) {
    V3Section("宗族", "${V3GameEngine.clanRankName(state)} · 人口 ${V3GameEngine.alivePeople(state).size} · 产业 ${V3GameEngine.builtSiteCount(state)}")
    V3Panel {
        Text(state.clanName, color = V3Red, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("根基：${state.root}    家训：${state.creed}", color = V3Ink, fontSize = 14.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                V3SmallButton("凝聚", Modifier.weight(1f)) { controller.showInfo("凝聚：宗族内部的团结程度。高凝聚会提高议事、育子和危机承受能力；过低时房支更容易争执，甚至影响结局。当前 ${state.cohesion}/100。") }
                V3SmallButton("族望", Modifier.weight(1f)) { controller.showInfo("族望：外部社会对本族的声名与认可。它影响婚配对象、宗族晋升、士绅交往和部分事件。当前 ${state.influence}/100。") }
                V3SmallButton("品第", Modifier.weight(1f)) { controller.showInfo("宗族品第：家族阶段，从立户、小族、望族到县中大姓、郡望世家。品第决定人口、产业、军务与天下经营的解锁。当前 ${V3GameEngine.clanRankName(state)}。") }
                V3SmallButton("乡勇", Modifier.weight(1f)) { controller.showInfo("乡勇：基础地方武装规模。它参与防御、出征和举旗评估；每月也会消耗粮食。当前 ${state.militia}。") }
        }
    }
    val eligible = V3GameEngine.marriageEligiblePeople(state)
    var selectedMarriagePersonId by remember { mutableStateOf<Int?>(null) }
    val target = eligible.firstOrNull { it.id == selectedMarriagePersonId } ?: eligible.firstOrNull()
    V3Panel(Modifier.guideTarget(V3GuideFocus.Marriage, guideTargets)) {
        Text("婚配与提亲", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("先选择具体待婚族人，再查看媒人送来的对象。每桩婚事都绑定到所选族人，需支付对应银粮；男族人迎娶，女族人按招赘规则成家。", color = V3Ink, fontSize = 13.sp, lineHeight = 19.sp)
        if (eligible.isEmpty()) {
            Text("当前没有18—55岁的适龄未婚族人。子女年满18岁后会自动进入待婚名单。", color = V3Muted, fontSize = 13.sp, lineHeight = 19.sp)
        } else {
            Text("待婚族人（${eligible.size}人）", color = V3Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            eligible.chunked(2).forEach { rowPeople ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowPeople.forEach { person ->
                        V3SmallButton(
                            "${person.name} · ${person.gender.label} · ${person.age}岁",
                            Modifier.weight(1f),
                            selected = target?.id == person.id
                        ) { selectedMarriagePersonId = person.id }
                    }
                    repeat(2 - rowPeople.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            target?.let { person ->
                val candidates = V3GameEngine.marriageCandidatesFor(person, state)
                Text("正在为 ${person.name}（${person.branch} · ${person.age}岁）议亲", color = V3Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (candidates.isEmpty()) {
                    Text("当前没有符合性别、年龄与族望条件的提亲对象。可提升族望后再来查看。", color = V3Muted, fontSize = 13.sp, lineHeight = 19.sp)
                }
                candidates.forEach { option ->
                    Card(colors = CardDefaults.cardColors(containerColor = V3PaperDeep), border = BorderStroke(2.dp, V3Gold), shape = V3SoftShape) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(V3Rice)
                                    .border(2.dp, V3Gold, CircleShape)
                                    .padding(5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AssetImage(
                                    GameImages.v3SpousePortraits[option.prototypeId]
                                        ?: GameImages.v3AvatarPortraits[option.avatarKey]
                                        ?: GameImages.v3AvatarPortraits.getValue("female_youth"),
                                    option.name,
                                    Modifier
                                        .matchParentSize()
                                        .clip(CircleShape),
                                    ContentScale.Fit
                                )
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(option.name, color = V3Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("${option.gender.label} · ${option.age}岁", color = V3Red, fontSize = 12.sp)
                                }
                                Text("礼金：银${option.silverCost} / 粮${option.grainCost} · 族望${option.influenceReq}", color = V3Red, fontSize = 12.sp)
                                Text(option.desc, color = V3Muted, fontSize = 12.sp, lineHeight = 17.sp)
                                V3SmallButton(
                                    "确认：${person.name}与${option.name}成婚",
                                    Modifier.fillMaxWidth(),
                                    enabled = true
                                ) { controller.marry(option.id, person.id) }
                            }
                        }
                    }
                }
            }
        }
    }
    V3Panel(Modifier.guideTarget(V3GuideFocus.ClanPromotion, guideTargets)) {
        Text("宗族晋升", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        val cost = V3GameEngine.nextRankCost(state)
        if (cost == null) {
            Text("已达最高品第。", color = V3Green, fontSize = 14.sp)
        } else {
            Text("下一品第：${cost.title}", color = V3Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("需要：银${cost.silver} / 粮${cost.grain} / 人口${cost.population} / 产业${cost.builtSites} / 族望${cost.influence}", color = V3Muted, fontSize = 13.sp)
            Text("当前：银${state.silver} / 粮${state.grain} / 人口${V3GameEngine.alivePeople(state).size} / 产业${V3GameEngine.builtSiteCount(state)} / 族望${state.influence}", color = V3Ink, fontSize = 13.sp)
            Text(V3GameEngine.rankProgressHint(state), color = if (V3GameEngine.canRankUp(state)) V3Green else V3Red, fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold)
            Text("晋升解锁：${rankUnlockPreview(state.clanRank + 1)}", color = V3Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 17.sp)
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
private fun V3PeoplePage(
    state: V3GameState,
    controller: V3GameController,
    guideTargets: MutableMap<V3GuideFocus, Rect>
) {
    val people = V3GameEngine.alivePeople(state)
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    val person = selectedPersonId?.let { id -> people.firstOrNull { it.id == id } }
    V3Section("族谱", "可拖动查看大族树状图；点小卡片弹出族人详情与培养安排。")
    V3GenealogyTree(
        state.clanName,
        people,
        Modifier.guideTarget(V3GuideFocus.Genealogy, guideTargets),
        onSelect = {
            selectedPersonId = it
        }
    )
    person?.let { selected ->
        V3PersonDetailDialog(person = selected, state = state, controller = controller, onDismiss = { selectedPersonId = null })
    }
}

private data class GenealogyNodePosition(val x: Int, val y: Int)

private fun genealogyPositions(people: List<V3Person>): Map<Int, GenealogyNodePosition> {
    val byParent = people.groupBy { it.parentId }
    val roots = people.filter { it.parentId == null }.sortedBy { it.id }
    val positions = linkedMapOf<Int, GenealogyNodePosition>()
    var nextX = 0
    fun place(person: V3Person, depth: Int): Int {
        val children = byParent[person.id].orEmpty().sortedBy { it.id }
        val center = if (children.isEmpty()) {
            val leafX = nextX * 132
            nextX += 1
            leafX
        } else {
            val childCenters = children.map { place(it, depth + 1) }
            (childCenters.first() + childCenters.last()) / 2
        }
        positions[person.id] = GenealogyNodePosition(center, depth * 164)
        return center
    }
    roots.forEach { place(it, 0) }
    people.filter { it.id !in positions }.forEach { person ->
        positions[person.id] = GenealogyNodePosition(nextX++ * 132, person.generation * 164)
    }
    val minX = positions.values.minOfOrNull { it.x } ?: 0
    val maxX = positions.values.maxOfOrNull { it.x } ?: 0
    val span = maxX - minX
    val nodeWidth = 112
    val contentWidth = maxOf(760, span + nodeWidth + 80)
    val centerPadding = ((contentWidth - span - nodeWidth) / 2).coerceAtLeast(40)
    return positions.mapValues { (_, position) -> position.copy(x = position.x - minX + centerPadding) }
}

@Composable
private fun V3GenealogyTree(
    clanName: String,
    people: List<V3Person>,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    var pan by remember { mutableStateOf(Offset.Zero) }
    val nodeWidth = 112
    val nodeHeight = 150
    val positions = genealogyPositions(people)
    val minX = positions.values.minOfOrNull { it.x } ?: 0
    val maxX = positions.values.maxOfOrNull { it.x } ?: 0
    val contentWidth = maxOf(760, maxX - minX + nodeWidth + 80)
    val contentHeight = maxOf(700, (positions.values.maxOfOrNull { it.y } ?: 0) + nodeHeight + 40)
    val density = LocalDensity.current
    val contentWidthPx = with(density) { contentWidth.dp.toPx() }
    val contentHeightPx = with(density) { contentHeight.dp.toPx() }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    V3Panel(modifier) {
        Text("${clanName}谱系", color = V3Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("以开族祖为中心，按亲子关系向下展开；拖动画布查看完整家谱，点卡片查看详情。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(460.dp)
                .clip(V3SoftShape)
                .background(V3Rice, V3SoftShape)
                .onSizeChanged { viewportSize = it }
                .freeMapDrag { dragAmount ->
                    val viewportWidth = viewportSize.width.toFloat()
                    val viewportHeight = viewportSize.height.toFloat()
                    val centeredX = (viewportWidth - contentWidthPx) / 2f
                    val minXPan = minOf(0f, viewportWidth - contentWidthPx - centeredX)
                    val maxXPan = maxOf(0f, -centeredX)
                    val minYPan = minOf(0f, viewportHeight - contentHeightPx)
                    pan = Offset(
                        (pan.x + dragAmount.x).coerceIn(minXPan, maxXPan),
                        (pan.y + dragAmount.y).coerceIn(minYPan, 0f)
                    )
                }
        ) {
            val centeredX = (viewportSize.width.toFloat() - contentWidthPx) / 2f
            Box(
                Modifier
                    .wrapContentSize(Alignment.TopStart, unbounded = true)
                    .requiredSize(contentWidth.dp, contentHeight.dp)
                    .graphicsLayer {
                        translationX = pan.x + centeredX
                        translationY = pan.y
                    }
            ) {
                AssetImage(GameImages.V3GenealogyBg, null, Modifier.fillMaxSize(), ContentScale.FillBounds, alpha = 0.72f)
                Canvas(Modifier.fillMaxSize()) {
                    people.forEach { child ->
                        val parent = child.parentId?.let { positions[it] } ?: return@forEach
                        val childPosition = positions[child.id] ?: return@forEach
                        val scale = density.density
                        val parentCenter = Offset((parent.x + 56f) * scale, (parent.y + nodeHeight.toFloat()) * scale)
                        val childCenter = Offset((childPosition.x + 56f) * scale, childPosition.y.toFloat() * scale)
                        val branchY = parentCenter.y + (childCenter.y - parentCenter.y) * 0.45f
                        drawLine(V3Gold.copy(alpha = 0.68f), parentCenter, Offset(parentCenter.x, branchY), strokeWidth = 3f)
                        drawLine(V3Gold.copy(alpha = 0.68f), Offset(parentCenter.x, branchY), Offset(childCenter.x, branchY), strokeWidth = 3f)
                        drawLine(V3Gold.copy(alpha = 0.68f), Offset(childCenter.x, branchY), childCenter, strokeWidth = 3f)
                    }
                }
                people.sortedWith(compareBy<V3Person> { positions[it.id]?.y ?: 0 }.thenBy { positions[it.id]?.x ?: 0 }).forEach { person ->
                    val position = positions[person.id] ?: GenealogyNodePosition(0, 0)
                    V3FamilyMiniNode(person, x = position.x, y = position.y, onSelect = onSelect)
                }
            }
        }
    }
}

@Composable
private fun V3FamilyMiniNode(person: V3Person, x: Int, y: Int, onSelect: (Int) -> Unit) {
    val nodeHeight = 150
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
            .offset(x = x.dp, y = y.dp)
            .width(112.dp)
            .height(nodeHeight.dp)
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
            Box(Modifier.size(54.dp).clip(CircleShape).background(V3Rice).border(2.dp, statusColor, CircleShape).padding(4.dp), contentAlignment = Alignment.Center) {
                AssetImage(v3AvatarFor(person), person.name, Modifier.matchParentSize().clip(CircleShape), ContentScale.Fit)
            }
            Text(person.name, color = V3Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("${person.age}岁${person.ageMonths.coerceAtLeast(person.age * 12) % 12}个月 · ${person.trait.label}", color = V3Muted, fontSize = 9.sp, maxLines = 1)
            Box(Modifier.fillMaxWidth().background(statusColor.copy(alpha = 0.14f), CircleShape).border(1.dp, statusColor.copy(alpha = 0.45f), CircleShape).padding(vertical = 2.dp), contentAlignment = Alignment.Center) {
                Text(statusText, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

private fun v3AvatarFor(person: V3Person): String {
    val spousePrototypeId = person.spouseCandidateId?.substringBefore('@')
    spousePrototypeId?.let { id -> GameImages.v3SpousePortraits[id] }?.let { return it }
    val stageKey = when {
        person.age <= 2 -> "baby"
        person.age <= 12 -> if (person.gender == V3Gender.Male) "male_child" else "female_child"
        person.age <= 20 -> if (person.gender == V3Gender.Male) "male_youth" else "female_youth"
        person.age <= 35 -> if (person.gender == V3Gender.Male) "male_adult" else "female_adult"
        person.age <= 55 -> if (person.gender == V3Gender.Male) "male_middle" else "female_middle"
        else -> if (person.gender == V3Gender.Male) "male_elder" else "female_elder"
    }
    val variants = GameImages.v3AvatarVariants[stageKey]
        ?: GameImages.v3AvatarVariants.getValue("baby")
    val stableIndex = Math.floorMod(person.id * 31 + stageKey.hashCode(), variants.size)
    return variants[stableIndex]
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
            V3PersonCard(person, state, controller, framed = false)
            V3SmallButton("关闭", Modifier.fillMaxWidth(), selected = true, onClick = onDismiss)
        }
    }
}

@Composable
private fun V3StrategyPage(
    state: V3GameState,
    controller: V3GameController,
    forcedPage: String?,
    guideTargets: MutableMap<V3GuideFocus, Rect>,
    openGuide: () -> Unit
) {
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
        Text("家乘所记，不止县中一亩三分地。婚配、产业、科举、军务和商路都会推着${state.surname}氏走向不同结局。", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
        Row(
            Modifier
                .fillMaxWidth()
                .guideTarget(V3GuideFocus.StrategyTabs, guideTargets),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("声势", "天下", "军务", "近事").forEach { label ->
                V3SmallButton(label, Modifier.weight(1f), selected = page == label) { page = label }
            }
        }
    }
    when (page) {
        "声势" -> {
            Column(Modifier.guideTarget(V3GuideFocus.StrategyContent, guideTargets)) {
            V3Panel {
                Text("地方关系", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                V3RelationRow("官府", state.relations.yamen)
                V3RelationRow("士绅", state.relations.gentry)
                V3RelationRow("乡民", state.relations.villagers)
                V3RelationRow("流寇", state.relations.bandits)
                V3RelationRow("商帮", state.relations.merchants)
                V3RelationRow("军镇", state.relations.garrison)
            }
            if (V3GameEngine.isUnlocked(state, "Council")) {
                V3CouncilPanel(state, controller)
            } else {
                V3LockedFeaturePanel("宗族议事", "升为小族（2级）后，每月可议定一次家政方略。")
            }
            V3Panel {
                Text("路线", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                V3Content.routePlans.sortedByDescending { state.routeScores[it.route] ?: 0 }.take(4).forEach { plan ->
                    V3RouteProgressRow(plan.route.label, state.routeScores[plan.route] ?: 0, selected = plan.route == ending.route)
                    Text("· ${plan.goal}", color = V3Muted, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
            }
        }
        "天下" -> if (V3GameEngine.isUnlocked(state, "World")) {
            V3WorldPanel(state, controller, guideTargets)
        } else {
            V3LockedFeaturePanel("天下经营", "升为望族（3级）后开放跨县结交、经营和征伐。")
        }
        "军务" -> if (!V3GameEngine.isUnlocked(state, "Recruit")) {
            V3LockedFeaturePanel("军务", "升为小族（2级）后开放基础募兵；望族后开放精兵与征伐。")
        } else V3Panel(Modifier.guideTarget(V3GuideFocus.StrategyContent, guideTargets)) {
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
                com.arktools.daming.v3.data.V3EquipmentSlot.entries.forEach { slot ->
                    V3SmallButton("购${slot.label}", Modifier.weight(1f)) { controller.buyEquipment(slot, com.arktools.daming.v3.data.V3EquipmentQuality.Common) }
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
private fun V3MilitaryPage(state: V3GameState, controller: V3GameController) {
    val recruitUnlocked = V3GameEngine.isUnlocked(state, "Recruit")
    val advancedUnlocked = V3GameEngine.isUnlocked(state, "AdvancedTroops")
    val conquestUnlocked = V3GameEngine.isUnlocked(state, "Conquest")
    val bannerUnlocked = V3GameEngine.isUnlocked(state, "RaiseBanner")
    V3Section("军务", "独立管理兵册、军械与出征，不再把长列表塞进大势页面。")
    V3Panel {
        Text("军务总览", color = V3Red, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text("兵册 ${state.army.total()} · 乡勇 ${state.army.militia} · 枪兵 ${state.army.spear} · 弓手 ${state.army.archer} · 盾手 ${state.army.shield} · 骑兵 ${state.army.cavalry}", color = V3Ink, fontSize = 13.sp, lineHeight = 19.sp)
        Text("募兵消耗银两与粮食；兵种会影响出征攻击、防守和损耗。按钮不可用时也会在点击后说明缺口。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
        Text("解锁状态：基础募兵 ${if (recruitUnlocked) "已开" else "需小族或寨堡"} · 精兵 ${if (advancedUnlocked) "已开" else "需望族与团练营"} · 征伐 ${if (conquestUnlocked) "已开" else "需望族并控制地域"} · 举旗 ${if (bannerUnlocked) "已开" else "需县中大姓与80兵"}", color = V3Gold, fontSize = 12.sp, lineHeight = 18.sp)
    }
    V3Panel {
        Text("募兵", color = V3Red, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text("点击一次募5名。若品第、团练营、价格或粮银不满足，会弹出明确原因。", color = V3Muted, fontSize = 12.sp)
        V3TroopType.entries.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { type ->
                    val enabledByRank = type == V3TroopType.Militia || advancedUnlocked || (type == V3TroopType.Cavalry && state.clanRank >= 4)
                    V3SmallButton("募${type.label}×5\n银${type.silverCost * 5} / 粮${type.grainCost * 5}", Modifier.weight(1f), enabled = recruitUnlocked && enabledByRank) {
                        controller.recruitTroops(type, 5)
                    }
                }
                repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
    V3Panel {
        Text("军械库", color = V3Red, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text("武器提高攻击，甲胄与盾牌提高防御，坐骑提高机动攻击。品质越高，基础属性越高，价格也越高。装备后会显示在对应族人详情。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
        V3EquipmentSlot.entries.forEach { slot ->
            val basePrice = when (slot) {
                V3EquipmentSlot.Weapon -> 32
                V3EquipmentSlot.Armor -> 32
                V3EquipmentSlot.Mount -> 28
                V3EquipmentSlot.Shield -> 24
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(slot.label, color = V3Ink, fontSize = 14.sp, modifier = Modifier.weight(0.8f))
                V3EquipmentQuality.entries.forEach { quality ->
                    val price = basePrice * quality.multiplier
                    V3SmallButton("${quality.label}\n银$price", Modifier.weight(1f)) {
                        controller.buyEquipment(slot, quality)
                    }
                }
            }
        }
        Text("库存与穿戴", color = V3Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text("库存 ${state.equipment.count { it.ownerId == null }} 件 · 已装备 ${state.equipment.count { it.ownerId != null }} 件。点击‘装备’后，装备会进入族人详情的装备栏，并计入出战属性。", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
        if (state.equipment.isEmpty()) {
            Text("军械库还是空的。先购买一件军械，再给成年族人装备。", color = V3Muted, fontSize = 12.sp)
        } else {
            state.equipment.forEach { item ->
                val owner = item.ownerId?.let { id -> state.people.firstOrNull { it.id == id } }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${item.name} · ${item.quality.label} · 攻${item.attack}/防${item.defense} · 银${item.price} · 耐久${item.durability}%${owner?.let { " · ${it.name}" } ?: " · 库存"}", color = V3Ink, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    if (owner == null) {
                        val target = V3GameEngine.adultPeople(state).firstOrNull()
                        V3SmallButton("装备", Modifier.width(58.dp), enabled = target != null) {
                            if (target != null) controller.equipEquipment(item.id, target.id) else controller.showInfo("当前没有16岁以上的成年族人，装备暂时没有可用的穿戴者。")
                        }
                    } else {
                        V3SmallButton("已装备", Modifier.width(64.dp), enabled = false, onClick = {})
                    }
                }
            }
        }
    }
    V3Panel {
        Text("出征与军令", color = V3Red, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text("出征会打开独立战斗面板：选择成年族人、分配兵种、逐回合推进。兵器与甲胄会直接计入族人的战斗属性。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            V3SmallButton("讨伐高风险地点", Modifier.weight(1f), enabled = recruitUnlocked) { controller.startBattle() }
            V3SmallButton("举旗", Modifier.weight(1f), enabled = bannerUnlocked) { controller.raiseBanner() }
        }
    }
}

@Composable
private fun V3LockedFeaturePanel(title: String, hint: String) {
    V3Panel {
        Text("$title · 尚未解锁", color = V3Muted, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(hint, color = V3Ink, fontSize = 13.sp, lineHeight = 19.sp)
        Text("先完成顶栏当前目标并提升宗族品第，高阶内容会逐步开放。", color = V3Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun V3CouncilPanel(state: V3GameState, controller: V3GameController) {
    val usedThisMonth = state.eventLog.any { it.startsWith("${state.year}年${state.month}月 · 宗族议事") }
    val (recommendedAgenda, recommendation) = V3GameEngine.recommendedCouncilAgenda(state)
    V3Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("宗族议事", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(if (usedThisMonth) "本月已议" else "每月一议", color = if (usedThisMonth) V3Muted else V3Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text("不是只看数值：每月在祠堂议定一项家政方略，会真实改变银粮、族望、凝聚、地方关系、路线倾向和兵册。", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
        Text(recommendation, color = V3Gold, fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold)
        V3SmallButton(
            "采纳本月建议",
            Modifier.fillMaxWidth(),
            enabled = !usedThisMonth,
            selected = true
        ) {
            controller.holdCouncil(recommendedAgenda)
        }
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
private fun V3WorldPanel(
    state: V3GameState,
    controller: V3GameController,
    guideTargets: MutableMap<V3GuideFocus, Rect>
) {
    var selectedRegionId by remember { mutableStateOf<String?>(null) }
    val selectedRegion = selectedRegionId?.let { id -> state.worldRegions.firstOrNull { it.id == id } }
    V3Panel {
        Text("天下地图", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Text("统一进度：${state.unificationProgress}/100", color = V3Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    Text("统一不是家产等级，也不是当前路线；它表示已控制的县外地域和天下影响力，只有跨域经营、结交、征伐逐步推进后才会增加。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
        V3WorldVisualMap(
            state,
            Modifier.guideTarget(V3GuideFocus.WorldMap, guideTargets)
        ) {
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
            Text("控制 ${region.control}/100 · 敌势 ${region.enemyPower} · 财富 ${region.wealth}", color = V3Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("控制值越高，县外经营收益越稳定；敌势越高，征伐风险越大。财富决定该地域每月提供的银粮潜力。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
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
private fun V3WorldVisualMap(
    state: V3GameState,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    var pan by remember { mutableStateOf(Offset.Zero) }
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(430.dp)
            .background(V3Rice, V3SoftShape)
            .clip(V3SoftShape)
    ) {
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
        Box(Modifier.fillMaxSize().horizontalMapDrag { dragAmount ->
            pan = Offset(
                (pan.x + dragAmount.x).coerceIn(minPanX, 0f),
                (pan.y + dragAmount.y).coerceIn(minPanY, 0f)
            )
        }) {
            Box(
                Modifier
                    .wrapContentSize(Alignment.TopStart, unbounded = true)
                    .requiredSize(width = mapWidth, height = mapHeight)
                    .graphicsLayer {
                        translationX = boundedPan.x
                        translationY = boundedPan.y
                    }
            ) {
                AssetImage(GameImages.V3WorldMap, null, Modifier.fillMaxSize(), ContentScale.FillBounds, alpha = 0.96f)
                Canvas(Modifier.matchParentSize()) {
                    fun point(x: Float, y: Float) = Offset(size.width * x, size.height * y)
                    fun route(ax: Float, ay: Float, bx: Float, by: Float, alpha: Float = 0.48f) {
                        drawLine(V3Red.copy(alpha = alpha), point(ax, ay), point(bx, by), strokeWidth = 4f, cap = StrokeCap.Square)
                    }
                    route(0.10f, 0.82f, 0.29f, 0.80f)
                    route(0.29f, 0.80f, 0.43f, 0.63f)
                    route(0.29f, 0.80f, 0.12f, 0.52f)
                    route(0.43f, 0.63f, 0.36f, 0.40f)
                    route(0.43f, 0.63f, 0.67f, 0.67f)
                    route(0.36f, 0.40f, 0.61f, 0.43f)
                    route(0.61f, 0.43f, 0.66f, 0.24f)
                    route(0.67f, 0.67f, 0.86f, 0.53f)
                    route(0.66f, 0.24f, 0.80f, 0.27f)
                    route(0.80f, 0.27f, 0.88f, 0.09f)
                    route(0.80f, 0.27f, 0.42f, 0.10f, 0.58f)
                    route(0.86f, 0.53f, 0.42f, 0.10f, 0.40f)
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

private fun worldMapPoint(regionId: String): Offset = when (regionId) {
    "home_county" -> Offset(0.10f, 0.82f)
    "neighbor_county" -> Offset(0.29f, 0.80f)
    "river_prefecture" -> Offset(0.43f, 0.63f)
    "mountain_prefecture" -> Offset(0.12f, 0.52f)
    "lake_province" -> Offset(0.36f, 0.40f)
    "coast_province" -> Offset(0.67f, 0.67f)
    "south_province" -> Offset(0.61f, 0.43f)
    "shandong_corridor" -> Offset(0.66f, 0.24f)
    "liaodong_front" -> Offset(0.88f, 0.09f)
    "north_capital" -> Offset(0.80f, 0.27f)
    "jiangsea_gate" -> Offset(0.86f, 0.53f)
    "all_realm" -> Offset(0.42f, 0.10f)
    else -> Offset(0.50f, 0.50f)
}

@Composable
private fun V3CountyMapView(
    state: V3GameState,
    modifier: Modifier = Modifier,
    onSelectSite: (String) -> Unit
) {
    var pan by remember { mutableStateOf(Offset.Zero) }
    val frameHeight = 560.dp
    val frameShape = V3SoftShape
    val density = LocalDensity.current
    V3Panel(modifier) {
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
                Modifier.fillMaxSize().horizontalMapDrag { dragAmount ->
                    pan = Offset(
                        (pan.x + dragAmount.x).coerceIn(minPanX, 0f),
                        (pan.y + dragAmount.y).coerceIn(minPanY, 0f)
                    )
                }
            ) {
                Box(
                    Modifier
                        .wrapContentSize(Alignment.TopStart, unbounded = true)
                        .requiredSize(width = mapWidth, height = mapHeight)
                        .graphicsLayer {
                            translationX = boundedPan.x
                            translationY = boundedPan.y
                        }
                ) {
                    AssetImage(GameImages.V3MapBgPlain, null, Modifier.fillMaxSize(), ContentScale.FillBounds)
                    state.sites.forEach { site ->
                        V3MapSitePin(
                            site,
                            mapWidthPx = mapWidthPx,
                            mapHeightPx = mapHeightPx,
                            unlocked = V3GameEngine.isSiteUnlocked(state, site.type),
                            requiredRank = V3GameEngine.siteRequiredRank(site.type)
                        ) { onSelectSite(site.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun V3MapSitePin(
    site: V3CountySite,
    mapWidthPx: Float,
    mapHeightPx: Float,
    unlocked: Boolean,
    requiredRank: Int,
    onClick: () -> Unit
) {
    val point = siteMapPoint(site.id)
    val icon = GameImages.v3SiteIcons[site.id] ?: return
    val density = LocalDensity.current
    val pinWidthPx = with(density) { 92.dp.toPx() }
    val pinHeightPx = with(density) { 110.dp.toPx() }
    val safeMarginPx = with(density) { 10.dp.toPx() }
    val x = (mapWidthPx * point.x - pinWidthPx * 0.5f).coerceIn(safeMarginPx, mapWidthPx - pinWidthPx - safeMarginPx)
    val y = (mapHeightPx * point.y - pinHeightPx * 0.35f).coerceIn(safeMarginPx, mapHeightPx - pinHeightPx - safeMarginPx)
    val markerColor = when {
        !unlocked -> V3Muted
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
        Text(
            if (unlocked) site.name else "${site.name} · ${requiredRank}级",
            color = markerColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.background(V3Rice.copy(alpha = 0.88f), V3SoftShape).padding(horizontal = 5.dp, vertical = 3.dp)
        )
    }
}

private fun siteMapPoint(siteId: String): Offset = when (siteId) {
    "fort" -> Offset(0.12f, 0.10f)
    "yamen" -> Offset(0.55f, 0.16f)
    "academy" -> Offset(0.85f, 0.28f)
    "shrine" -> Offset(0.40f, 0.38f)
    "farmland" -> Offset(0.13f, 0.55f)
    "market" -> Offset(0.77f, 0.58f)
    "clinic" -> Offset(0.32f, 0.72f)
    "dock" -> Offset(0.82f, 0.82f)
    "mountain_pass" -> Offset(0.14f, 0.90f)
    else -> Offset(0.50f, 0.50f)
}

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
            Text("${site.name} 管理", color = V3Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
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
        Text("控制：${site.control}/100（越高产出越稳定） · 风险：${site.risk}/100（越高越容易减产或触发事件） · 等级：${site.level}", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
        Text(siteSpecialHint(site), color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
        V3SmallButton(siteSpecialButtonLabel(site), Modifier.fillMaxWidth(), enabled = site.level > 0) { controller.siteSpecialAction(site.id) }
        val cost = V3GameEngine.upgradeCost(site)
        if (cost != null) {
            Text("营建：银${cost.silver} / 粮${cost.grain} · ${cost.desc}", color = V3Muted, fontSize = 12.sp)
            Text("当前：银${state.silver}、粮${state.grain}；点击按钮查看具体缺口或解锁条件。", color = V3Muted, fontSize = 11.sp, lineHeight = 16.sp)
            V3SmallButton("营建/升级", Modifier.fillMaxWidth()) { controller.upgradeSite(site.id) }
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
            V3SmallButton("家产总级", Modifier.weight(1f)) { controller.showInfo("家产总级：所有家产项目等级之和，不是银两，也不是产业数量。它反映家族经济底盘，并影响终局评价。当前 ${V3GameEngine.estateLevelTotal(state)}。") }
            V3SmallButton("控制地域", Modifier.weight(1f)) { controller.showInfo("控制地域：当前已控制的县外区域数量。控制地域会带来额外银粮，也会提高统一进度。当前 ${V3GameEngine.controlledRegionCount(state)}。") }
            V3SmallButton("统一进度", Modifier.weight(1f)) { controller.showInfo("统一进度：跨县经营和征伐的长期进度，不等于家产等级。需要逐步控制战略地域，满足条件后才能宣告统一。当前 ${state.unificationProgress}/100。") }
        }
        V3EstateType.entries.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                row.forEach { type ->
                    val asset = state.estateAssets.firstOrNull { it.type == type }
                    val cost = V3GameEngine.estateUpgradeCost(state, type)
                    val yield = asset?.let { V3GameEngine.estateYield(it) }
                    val unlocked = V3GameEngine.isEstateUnlocked(state, type)
                    V3SmallButton("${type.label}\n银${cost.silver}/粮${cost.grain}${yield?.let { " · ${siteYieldSummary(it)}" } ?: ""}", Modifier.weight(1f)) {
                        controller.upgradeEstate(type)
                    }
                }
                repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun V3PersonCard(
    person: V3Person,
    state: V3GameState,
    controller: V3GameController,
    framed: Boolean = true
) {
    val content: @Composable ColumnScope.() -> Unit = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(72.dp).clip(CircleShape).background(V3Rice).border(3.dp, V3Gold, CircleShape).padding(5.dp), contentAlignment = Alignment.Center) {
                        AssetImage(v3AvatarFor(person), person.name, Modifier.matchParentSize().clip(CircleShape), ContentScale.Fit)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(person.name, color = V3Ink, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "第${person.generation}世 · ${person.gender.label} · ${person.branch} · ${person.identity}",
                            color = V3Red,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                        Text("${person.age}岁${person.ageMonths.coerceAtLeast(person.age * 12) % 12}个月 · ${V3GameEngine.lifeStage(person)} · ${V3GameEngine.marriageStatus(person, state)}", color = V3Muted, fontSize = 12.sp)
                        V3GameEngine.birthStatus(person, state)?.let { status ->
                            Text(status, color = V3Gold, fontSize = 11.sp, lineHeight = 16.sp)
                        }
                        V3GameEngine.equipmentSummary(person, state)?.let { equipmentText ->
                            Text(equipmentText, color = V3Blue, fontSize = 11.sp, lineHeight = 16.sp)
                        }
                        person.illness?.let { illness ->
                            Text(
                                "患病：$illness · 病程${person.illnessMonths}个月${if ((state.sites.firstOrNull { it.type == V3CountySiteType.Clinic }?.level ?: 0) > 0) " · 医馆诊治中" else " · 尚无医馆"}",
                                color = V3Red,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!person.alive) {
                            Text(
                                "卒于${person.deathYear ?: "?"}年${person.deathMonth ?: "?"}月 · ${person.deathCause ?: "未详"}",
                                color = V3Muted,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                        val assignedSite = person.assignedSiteId?.let { id -> state.sites.firstOrNull { it.id == id } }
                        val titleBits = listOfNotNull(person.officeRank, person.militaryRank).joinToString(" · ")
                        Text(
                            if (person.currentTask == null && person.trainingFocus == null) "待命${if (titleBits.isBlank()) "" else " · $titleBits"}" else "${person.currentTask?.label ?: person.trainingFocus?.label} · ${assignedSite?.name ?: "家中"}",
                            color = if (person.currentTask == null && person.trainingFocus == null) V3Muted else V3Green,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 17.sp
                        )
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
    if (framed) V3Panel(content = content) else Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
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
private fun V3TopBar(
    state: V3GameState,
    controller: V3GameController,
    onRequestBackToMenu: () -> Unit,
    guideTargets: MutableMap<V3GuideFocus, Rect>,
    secondsToNextMonth: Int
) {
    Box(Modifier.fillMaxWidth().guideTarget(V3GuideFocus.TopBar, guideTargets)) {
        AssetImage(GameImages.MingyunPanel, null, Modifier.matchParentSize(), ContentScale.FillBounds)
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
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
            V3TimeControls(controller, guideTargets, secondsToNextMonth)
            Text(
                "当前目标：${nextAdvice(state)}",
                color = V3Red,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 17.sp
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .guideTarget(V3GuideFocus.Resources, guideTargets),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                V3ResourceMetric(GameImages.V3IconSilver, "银两", state.silver, V3Gold, Modifier.weight(1f), onClick = { controller.showInfo("银两：家族的现金储备，用于婚配礼金、产业营建、议事、募兵、购买与修复军械。当前 ${state.silver} 两。") })
                V3ResourceMetric(GameImages.V3IconGrain, "粮食", state.grain, V3Green, Modifier.weight(1f), onClick = { controller.showInfo("粮食：人口、乡勇和部分产业的每月消耗品。田庄、佃田、粮仓与赈济会改变粮食。当前 ${state.grain} 石。") })
                V3ResourceMetric(GameImages.V3IconPopulation, "人口", V3GameEngine.alivePeople(state).size, V3Blue, Modifier.weight(1f), onClick = { controller.showInfo("人口：当前活着的族人数量。16岁成年，18岁进入婚配名单；婚姻会触发备孕、孕期、出生和下一代成长。当前 ${V3GameEngine.alivePeople(state).size} 人。") })
                V3ResourceMetric(GameImages.V3IconIndustry, "产业", V3GameEngine.builtSiteCount(state), V3Red, Modifier.weight(1f), onClick = { controller.showInfo("产业：已建成的县域地点数量。等级、控制和风险共同决定稳定产出；点击县域地图中的地点可查看详情、升级和专属事务。当前 ${V3GameEngine.builtSiteCount(state)} 处。") })
            }
        }
    }
}

@Composable
private fun V3TimeControls(
    controller: V3GameController,
    guideTargets: MutableMap<V3GuideFocus, Rect>,
    secondsToNextMonth: Int
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val speedPassStore = remember { SpeedPassStore(context) }
    var remainingPassMillis by remember { mutableStateOf(speedPassStore.remainingMillis()) }
    var adLoading by remember { mutableStateOf(false) }

    LaunchedEffect(remainingPassMillis > 0L) {
        while (remainingPassMillis > 0L) {
            delay(1_000L)
            remainingPassMillis = speedPassStore.remainingMillis()
        }
        if (remainingPassMillis <= 0L && controller.timeSpeed > 1) {
            controller.updateTimeSpeed(1)
            controller.showInfo("广告倍速权益已到期，时间流速已自动恢复为 1 倍。")
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .guideTarget(V3GuideFocus.TimeControls, guideTargets),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            V3SmallButton(if (controller.timeSpeed == 0) "继续" else "暂停", Modifier.weight(1f), selected = controller.timeSpeed == 0) { controller.togglePause() }
            listOf(1, 2, 3, 4, 5).forEach { speed ->
                val unlocked = speed == 1 || remainingPassMillis > 0L
                val label = if (unlocked) "${speed}倍" else "${speed}倍锁定"
                V3SmallButton(label, Modifier.weight(1f), selected = controller.timeSpeed == speed) {
                    when {
                        speed == 1 || remainingPassMillis > 0L -> controller.updateTimeSpeed(speed)
                        adLoading -> controller.showInfo("激励广告正在加载，请稍候。")
                        activity == null -> controller.showInfo("当前页面无法打开激励广告。")
                        else -> RewardedAdController.show(
                            activity = activity,
                            onLoadingChanged = { adLoading = it },
                            onRewarded = {
                                val expiresAt = speedPassStore.unlockForTwentyMinutes()
                                remainingPassMillis = (expiresAt - System.currentTimeMillis()).coerceAtLeast(0L)
                                controller.updateTimeSpeed(speed)
                                controller.showInfo("激励广告奖励已生效：2–5 倍速度全部解锁 20 分钟，当前切换为 ${speed} 倍。")
                            },
                            onError = controller::showInfo,
                            onClosed = {}
                        )
                    }
                }
            }
            Text(
                if (controller.shouldAutoTick()) "下月 ${secondsToNextMonth}秒" else "时序暂停",
                color = if (controller.shouldAutoTick()) V3Green else V3Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1.4f)
            )
        }
        Text(
            if (remainingPassMillis > 0L) {
                val totalSeconds = (remainingPassMillis / 1000L).coerceAtLeast(0L)
                val minutes = totalSeconds / 60L
                val seconds = totalSeconds % 60L
                "广告倍速已解锁，剩余 ${minutes}分${seconds.toString().padStart(2, '0')}秒。1倍永久免费；权益到期自动恢复1倍。"
            } else {
                "1倍永久免费；点击2–5倍可观看一次激励广告，完整观看并通过奖励校验后全部解锁20分钟。"
            },
            color = if (remainingPassMillis > 0L) V3Green else V3Gold,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )
        Text(
            "现实耗时：1倍22秒/月（4分24秒/年）· 2倍11秒/月 · 3倍约7秒/月 · 4倍约5秒/月 · 5倍约4秒/月；人物每月增长1个月。",
            color = V3Muted,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )
    }
}

private fun monthIntervalMillis(speed: Int): Long = when (speed) {
    5 -> 4400L
    4 -> 5500L
    3 -> 7300L
    2 -> 11000L
    else -> 22000L
}

private fun mingEraLabel(year: Int): String = when {
    year <= 1619 -> "万历${year - 1572}年 · ${year}年"
    year <= 1627 -> "天启${year - 1620}年 · ${year}年"
    else -> "崇祯${year - 1627}年 · ${year}年"
}

private fun mingSituationText(state: V3GameState): String = when {
    state.year < 1619 -> "万历末年，矿税、徭役和地方积弊仍在。${state.surname}氏先要在清河县稳住婚配、田粮、祠产和县衙关系。"
    state.year < 1628 -> "辽事已急，天启年间党争、边饷、军镇催粮渐入县中。宗族不能只看家账，还要决定勤王、自保或通商。"
    state.year < 1636 -> "崇祯新政催饷更紧，饥荒与流民开始外溢。${state.surname}氏若无粮、无勇、无族望，乱世会自然吞没家业。"
    state.year < 1642 -> "关外势大，流寇四起，地方豪族各谋退路。此时应在书院、商路、寨堡、码头之间定下真正路线。"
    else -> "甲申将近，天下土崩。${state.surname}氏必须在勤王、割据、保族、南迁之间作终局选择。"
}

@Composable
private fun V3BottomNav(
    controller: V3GameController,
    guideTargets: MutableMap<V3GuideFocus, Rect>
) {
    Box(
        Modifier
            .fillMaxWidth()
            .guideTarget(V3GuideFocus.BottomNav, guideTargets)
    ) {
        AssetImage(GameImages.MingyunPanel, null, Modifier.matchParentSize(), ContentScale.FillBounds)
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            V3Screen.entries.forEach { screen ->
                V3SmallButton(V3GameEngine.screenTitle(screen), Modifier.weight(1f), selected = controller.screen == screen) { controller.switchScreen(screen) }
            }
        }
    }
}

private fun V3Screen.backgroundAsset(): String = when (this) {
    V3Screen.County -> GameImages.MingyunHomeBg
    V3Screen.Clan -> GameImages.MingyunClanBg
    V3Screen.People -> GameImages.MingyunPeopleBg
    V3Screen.Strategy -> GameImages.MingyunStrategyBg
    V3Screen.Military -> GameImages.MingyunStrategyBg
}

@Composable
private fun V3Background(backgroundAsset: String, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(V3Bg)) {
        AssetImage(backgroundAsset, null, Modifier.fillMaxSize(), ContentScale.Crop, alpha = 1f)
        Box(Modifier.fillMaxSize().background(Color(0x55100E0B)))
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
    Box(modifier = modifier.fillMaxWidth()) {
        AssetImage(GameImages.MingyunPanel, null, Modifier.matchParentSize(), ContentScale.FillBounds)
        Column(
            Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
private fun V3ImagePanel(imagePath: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier.fillMaxWidth()) {
        AssetImage(imagePath, null, Modifier.matchParentSize(), ContentScale.FillBounds)
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .background(V3Rice.copy(alpha = 0.94f), V3SoftShape)
                .border(1.dp, V3Border.copy(alpha = 0.85f), V3SoftShape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
            content = content
        )
    }
}

@Composable
private fun BoxScope.V3CornerOrnaments() {
    Box(Modifier.matchParentSize()) {
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
private fun V3ResourceMetric(
    iconPath: String,
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(modifier.clickable(enabled = onClick != null) { onClick?.invoke() }) {
        AssetImage(GameImages.MingyunResourcePlaque, null, Modifier.matchParentSize(), ContentScale.FillBounds)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            AssetImage(iconPath, null, Modifier.size(21.dp), ContentScale.Fit)
            Text(value.toString(), color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
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
private fun V3Button(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AssetImage(
            if (enabled) GameImages.MingyunPrimaryButton else GameImages.MingyunPrimaryButtonDisabled,
            null,
            Modifier.matchParentSize(),
            ContentScale.FillBounds
        )
        Text(
            text,
            color = if (enabled) V3Ink else V3Muted,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun V3SmallButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, selected: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AssetImage(
            when {
                !enabled -> GameImages.MingyunSmallButtonDisabled
                selected -> GameImages.MingyunSmallButtonSelected
                else -> GameImages.MingyunSmallButton
            },
            null,
            Modifier.matchParentSize(),
            ContentScale.FillBounds
        )
        Text(
            text,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
            color = when {
                !enabled -> V3Muted
                selected -> V3Ink
                else -> V3Ink
            },
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
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = V3PaperDeep), border = BorderStroke(2.dp, V3Red), shape = V3SoftShape) {
                            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(choice.label, color = V3Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text(choice.route.label, color = V3Red, fontSize = 12.sp)
                                }
                                Text(choice.desc, color = V3Muted, fontSize = 12.sp)
                                Text(choiceImpactSummary(choice), color = V3Ink, fontSize = 12.sp)
                                V3SmallButton("选择此方案", Modifier.fillMaxWidth()) { controller.chooseEvent(choice) }
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
private fun V3ExamDialog(session: com.arktools.daming.v3.data.V3ExamSession, controller: V3GameController) {
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
                            style.label,
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

private fun nextAdvice(state: V3GameState): String {
    val founder = state.people.firstOrNull { it.id == 1 }
    val hasSpouse = founder?.spouseId != null
    val waitingMarriage = V3GameEngine.marriageEligiblePeople(state).firstOrNull()
    val builtSites = V3GameEngine.builtSiteCount(state)
    val hasAssignment = state.people.any { it.currentTask != null || it.trainingFocus != null }
    val advancedFromOpeningMonth = state.year > 1601 || state.month > 1
    return when {
        !hasSpouse -> "前往【宗族】为${state.founderName}完成婚配；完成后解锁添丁传承。"
        waitingMarriage != null -> "${waitingMarriage.name}已到适婚年龄，可前往【宗族】选择此人并查看提亲对象。"
        builtSites < 2 -> "在【家业】点县域集市并营建第二处产业；完成后形成银粮双收入。"
        !hasAssignment -> "前往【族人】点${state.founderName}，安排培养或派差；月结时获得成长与收益。"
        !advancedFromOpeningMonth -> "返回【家业】点继续或倍速，推进首月结算；倒计时会显示距下月秒数。"
        state.clanRank == 1 && V3GameEngine.canRankUp(state) -> "前往【宗族】晋升小族；解锁议事、书院医馆、县衙与基础募兵。"
        state.clanRank == 1 -> "完成首年目标并积累银90、粮130、人口2、产业2、族望10，准备晋升小族。"
        state.clanRank == 2 && V3GameEngine.canRankUp(state) -> "前往【宗族】晋升望族；解锁天下经营、军务产业和跨域征伐。"
        state.clanRank == 2 -> "经营县域、培养族人并扩大家口，满足望族晋升条件。"
        state.clanRank == 3 && V3GameEngine.canRankUp(state) -> "晋升县中大姓，筹备80兵册后可举旗。"
        else -> "按【眼前目标】经营产业、派遣族人并推进月结，逐步完成路线与宗族晋升。"
    }
}

private fun rankUnlockPreview(rank: Int): String = when (rank) {
    2 -> "宗族议事、县衙、书院、医馆、铺面粮仓、基础募兵"
    3 -> "寨堡、码头、山道、作坊商队团练营、天下经营与征伐"
    4 -> "举旗割据、骑兵与县域统合"
    5 -> "郡望世家与统一终局"
    else -> "已达最高品第"
}

private fun siteChipText(site: V3CountySite): String = "${site.name.takeLast(2)}${if (site.level > 0) "Lv.${site.level}" else "未建"}"

private fun siteSpecialButtonLabel(site: V3CountySite): String = when (site.type) {
    com.arktools.daming.v3.data.V3CountySiteType.Shrine -> "开祠修谱"
    com.arktools.daming.v3.data.V3CountySiteType.Farmland -> "抢修水渠"
    com.arktools.daming.v3.data.V3CountySiteType.Market -> "开设牙行"
    com.arktools.daming.v3.data.V3CountySiteType.Yamen -> "打点税册"
    com.arktools.daming.v3.data.V3CountySiteType.Academy -> "举行讲会"
    com.arktools.daming.v3.data.V3CountySiteType.Clinic -> "开设义诊"
    com.arktools.daming.v3.data.V3CountySiteType.Fort -> "点验乡勇"
    com.arktools.daming.v3.data.V3CountySiteType.Dock -> "开走海货"
    com.arktools.daming.v3.data.V3CountySiteType.MountainPass -> "山道设卡"
}

private fun siteSpecialHint(site: V3CountySite): String = when (site.type) {
    com.arktools.daming.v3.data.V3CountySiteType.Shrine -> "专属事务：消耗粮食，提升凝聚和族望。"
    com.arktools.daming.v3.data.V3CountySiteType.Farmland -> "专属事务：花银修渠，换取大量粮食。"
    com.arktools.daming.v3.data.V3CountySiteType.Market -> "专属事务：消耗粮食换银两和商帮关系。"
    com.arktools.daming.v3.data.V3CountySiteType.Yamen -> "专属事务：花银打点官府，缓冲税役压力。"
    com.arktools.daming.v3.data.V3CountySiteType.Academy -> "专属事务：开讲会提升士绅、族望和耕读路线。"
    com.arktools.daming.v3.data.V3CountySiteType.Clinic -> "专属事务：义诊压疫病，提高乡民与凝聚。"
    com.arktools.daming.v3.data.V3CountySiteType.Fort -> "专属事务：消耗银粮，快速增加乡勇。"
    com.arktools.daming.v3.data.V3CountySiteType.Dock -> "专属事务：走海货获银，推进海外路线但损官府。"
    com.arktools.daming.v3.data.V3CountySiteType.MountainPass -> "专属事务：设卡压流寇，推进割据路线。"
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
    return state.sites
        .filter {
            it.taskTypes.contains(task) &&
                V3GameEngine.isSiteUnlocked(state, it.type)
        }
        .maxWithOrNull(compareBy<V3CountySite> { it.level }.thenBy { it.risk })
}
