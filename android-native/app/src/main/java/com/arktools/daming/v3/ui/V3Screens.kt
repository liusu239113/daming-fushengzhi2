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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.arktools.daming.ads.RewardClaimStore
import com.arktools.daming.ads.RewardedAdController
import com.arktools.daming.ads.SpeedPassStore
import com.arktools.daming.ads.ui.AdLoadingOverlay
import com.arktools.daming.data.GameImages
import com.arktools.daming.ui.components.AssetImage
import com.arktools.daming.ui.theme.FontPreference
import com.arktools.daming.ui.theme.FontStyleKey
import com.arktools.daming.v3.data.V3ActiveEvent
import com.arktools.daming.v3.data.V3CardPool
import com.arktools.daming.v3.data.V3MonthlyCard
import com.arktools.daming.v3.data.V3HexArms
import com.arktools.daming.v3.data.V3HexBattleState
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
import com.arktools.daming.v3.data.V3MonthlyReport
import com.arktools.daming.v3.data.V3Person
import com.arktools.daming.v3.data.V3Route
import com.arktools.daming.v3.data.V3RegionStatus
import com.arktools.daming.v3.data.V3Screen
import com.arktools.daming.v3.data.V3SiteYield
import com.arktools.daming.v3.data.V3TaskType
import com.arktools.daming.v3.data.V3TrainingType
import com.arktools.daming.v3.data.V3TroopType
import com.arktools.daming.v3.data.V3WorldRegion
import com.arktools.daming.v3.logic.V3CardEngine
import com.arktools.daming.v3.logic.V3GameController
import com.arktools.daming.v3.logic.V3GameEngine
import com.arktools.daming.v3.logic.V3ProgressionEngine
import com.arktools.daming.v3.logic.V3ProgressionSnapshot
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

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
                    V3CompactSelector("出身", V3Content.roots, root, ::createRootEffect) {
                        controller.playUiSelect()
                        root = it
                    }
                    V3CompactSelector("县域", V3Content.counties, county, {
                        V3Content.startProfile(root, it, creed, crisis).countyEffect
                    }) {
                        controller.playUiSelect()
                        county = it
                    }
                    V3CompactSelector("家训", V3Content.creeds, creed, ::createCreedEffect) {
                        controller.playUiSelect()
                        creed = it
                    }
                    V3CompactSelector("危机", V3Content.crises, crisis, {
                        V3Content.startProfile(root, county, creed, it).crisisEffect
                    }) {
                        controller.playUiSelect()
                        crisis = it
                    }
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
                V3Button("返回", Modifier.weight(1f)) {
                    controller.playUiClick()
                    onBack()
                }
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
    targets: MutableMap<V3GuideFocus, Rect>,
    actions: MutableMap<V3GuideFocus, () -> Unit>? = null,
    onClick: (() -> Unit)? = null
): Modifier = this.onGloballyPositioned { coordinates ->
    targets[focus] = coordinates.boundsInRoot()
    if (actions != null && onClick != null) actions[focus] = onClick
}

@Composable
fun V3GameScreen(controller: V3GameController, fontPreference: FontPreference, onBackToMenu: () -> Unit) {
    LaunchedEffect(Unit) { controller.ensureV3Bgm() }
    LaunchedEffect(controller.timeSpeed) {
        while (controller.timeSpeed > 0) {
            delay(monthIntervalMillis(controller.timeSpeed))
            if (controller.shouldAutoTick()) controller.autoAdvanceTime()
        }
    }
    val state = controller.state
    var confirmBackToMenu by remember { mutableStateOf(false) }
    var elderGuideVisible by remember(state.clanName, state.founderName) { mutableStateOf(!state.tutorialCompleted) }
    LaunchedEffect(state.tutorialCompleted) {
        if (!state.tutorialCompleted) elderGuideVisible = true
    }
    var guideStrategyPage by remember { mutableStateOf<String?>(null) }
    var countyHomePage by remember { mutableStateOf(V3CountyHomePage.Map) }
    val guideTargets = remember { mutableStateMapOf<V3GuideFocus, Rect>() }
    val guideActions = remember { mutableStateMapOf<V3GuideFocus, () -> Unit>() }
    val contentScroll = rememberScrollState()
    val screenDensity = LocalDensity.current
    val screenHeightPx = with(screenDensity) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val tutorialStep = state.tutorialStep.coerceIn(0, elderGuideSteps(state).lastIndex)
    val tutorialFocus = elderGuideSteps(state)[tutorialStep].focus
    val localTutorialSteps = setOf(6, 7, 8, 12, 13, 14, 17, 18)
    val tutorialUsesLocalOverlay = tutorialStep in localTutorialSteps
    var tutorialCardAtTop by remember(tutorialStep) { mutableStateOf(false) }
    var tutorialTargetBounds by remember(tutorialStep) { mutableStateOf<Rect?>(null) }
    val measuredTutorialTarget = guideTargets[tutorialFocus]
    val cardAtTop = tutorialCardAtTop
    LaunchedEffect(elderGuideVisible, tutorialStep) {
        if (!elderGuideVisible || state.tutorialCompleted) return@LaunchedEffect
        val step = elderGuideSteps(state)[tutorialStep]
        if (step.tab == V3Screen.County) {
            countyHomePage = when (step.focus) {
                V3GuideFocus.CountyMap,
                V3GuideFocus.FarmlandPin,
                V3GuideFocus.SiteOverview,
                V3GuideFocus.SiteActions,
                V3GuideFocus.SiteClose,
                V3GuideFocus.EstateOverview -> V3CountyHomePage.Map

                else -> V3CountyHomePage.Overview
            }
        }
        if (controller.screen != step.tab) controller.switchScreen(step.tab)
        guideStrategyPage = step.strategyPage
    }
    LaunchedEffect(
        elderGuideVisible,
        tutorialStep,
        controller.screen,
        guideStrategyPage,
        contentScroll.maxValue,
        countyHomePage,
        measuredTutorialTarget
    ) {
        tutorialTargetBounds = null
        if (!elderGuideVisible || state.tutorialCompleted) return@LaunchedEffect
        val step = elderGuideSteps(state)[tutorialStep]
        // 教程只负责把步骤目标带到正确页面，不再在玩家手动切页后强制拉回。
        // 玩家可以暂时离开当前页处理其他事务，返回目标页后教程会继续显示。
        if (controller.screen != step.tab) return@LaunchedEffect
        guideStrategyPage = step.strategyPage
        val presetFraction = when (tutorialFocus) {
            V3GuideFocus.TopBar,
            V3GuideFocus.Resources,
            V3GuideFocus.MonthlyLedger,
            V3GuideFocus.TimeControls -> 0f

            V3GuideFocus.MonthlyForecast,
            V3GuideFocus.AutoArrange -> 0.16f

            V3GuideFocus.AnnualGoals -> 0.34f
            V3GuideFocus.CountyMap,
            V3GuideFocus.FarmlandPin -> 0.58f

            V3GuideFocus.EstateOverview -> 0.96f
            V3GuideFocus.Marriage,
            V3GuideFocus.MarriagePerson,
            V3GuideFocus.MarriageCandidate -> 0.22f

            V3GuideFocus.ClanPromotion -> 0.72f
            V3GuideFocus.Genealogy -> 0.25f
            V3GuideFocus.StrategyContent,
            V3GuideFocus.StrategyTabs,
            V3GuideFocus.Relations,
            V3GuideFocus.Council,
            V3GuideFocus.WorldTab,
            V3GuideFocus.MilitaryTab,
            V3GuideFocus.RecentTab -> 0f

            else -> 0f
        }
        delay(100)
        contentScroll.scrollTo((contentScroll.maxValue * presetFraction).toInt())
        repeat(6) {
            delay(100)
            val bounds = guideTargets[tutorialFocus] ?: return@repeat
            val shouldPlaceCardTop = bounds.center.y > screenHeightPx * 0.55f
            val safeTop = if (shouldPlaceCardTop) with(screenDensity) { 300.dp.toPx() } else with(screenDensity) { 120.dp.toPx() }
            val safeBottom = if (shouldPlaceCardTop) screenHeightPx - with(screenDensity) { 72.dp.toPx() } else screenHeightPx - with(screenDensity) { 300.dp.toPx() }
            val adjustment = when {
                bounds.top < safeTop -> bounds.top - safeTop
                bounds.bottom > safeBottom -> bounds.bottom - safeBottom
                else -> 0f
            }.toInt()
            if (adjustment != 0) {
                contentScroll.scrollTo((contentScroll.value + adjustment).coerceIn(0, contentScroll.maxValue))
            } else {
                tutorialTargetBounds = bounds
                tutorialCardAtTop = shouldPlaceCardTop
                return@LaunchedEffect
            }
        }
        val finalBounds = guideTargets[tutorialFocus]
        if (finalBounds != null && finalBounds.bottom > 0f && finalBounds.top < screenHeightPx) {
            tutorialTargetBounds = finalBounds
            tutorialCardAtTop = finalBounds.center.y > screenHeightPx * 0.55f
        }
    }
    V3Background(controller.screen.backgroundAsset()) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                V3TopBar(
                    state,
                    controller,
                    onRequestBackToMenu = { confirmBackToMenu = true },
                    guideTargets = guideTargets,
                    tutorialStep = tutorialStep
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
                            V3Screen.County -> V3HomePage(
                                state,
                                controller,
                                guideTargets,
                                guideActions,
                                selectedPage = countyHomePage,
                                onPageChange = { countyHomePage = it }
                            )
                            V3Screen.Clan -> V3ClanPage(state, controller, guideTargets, guideActions)
                            V3Screen.People -> V3PeoplePage(state, controller, guideTargets, guideActions)
                            V3Screen.Strategy -> V3StrategyPage(state, controller, forcedPage = guideStrategyPage, guideTargets = guideTargets, guideActions = guideActions, openGuide = {
                                guideStrategyPage = null
                                controller.reopenTutorial()
                                elderGuideVisible = true
                            })
                        }
                    }
                }
                if (state.finalEnding == null) {
                    V3BottomNav(controller, guideTargets)
                }
            }
            if (
                elderGuideVisible &&
                    !tutorialUsesLocalOverlay &&
                    state.finalEnding == null &&
                    controller.latestReport == null &&
                    controller.message == null &&
                    !controller.settingsVisible &&
                    state.activeEvent == null &&
                    state.examSession == null &&
                    state.battleState == null &&
                    state.hexBattleState == null &&
                    state.conquestState == null
            ) {
                V3ElderGuideOverlay(
                    state = state,
                    controller = controller,
                    targetBounds = tutorialTargetBounds,
                    cardAtTop = cardAtTop,
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
            V3MonthlyReportDialog(report = report, controller = controller)
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
    if (controller.state.finalEnding == null) {
        controller.state.examSession?.let { session ->
            V3ExamDialog(session = session, controller = controller)
        }
        controller.state.battleState?.let { battle ->
            V3BattleDialog(state = controller.state, battle = battle, controller = controller)
        }
        controller.state.hexBattleState?.let { hexBattle ->
            V3HexBattleDialog(battle = hexBattle, controller = controller)
        }
        controller.state.conquestState?.let { conquest ->
            V3ConquestDialog(target = conquest.targetName, enemyPower = conquest.enemyPower, scale = conquest.scale, controller = controller)
        }
        if (
            controller.latestReport == null &&
            controller.message == null &&
            controller.state.activeEvent == null &&
            controller.state.activeCards.any { it.pool == V3CardPool.Visitor }
        ) {
            V3VisitorDialog(
                card = controller.state.activeCards.first { it.pool == V3CardPool.Visitor },
                state = controller.state,
                controller = controller
            )
        }
    }
}

private enum class V3CountyHomePage(val label: String) {
    Overview("总览"),
    Map("县域"),
    Archive("家产档案")
}

@Composable
private fun V3HomePage(
    state: V3GameState,
    controller: V3GameController,
    guideTargets: MutableMap<V3GuideFocus, Rect>,
    guideActions: MutableMap<V3GuideFocus, () -> Unit>,
    selectedPage: V3CountyHomePage,
    onPageChange: (V3CountyHomePage) -> Unit
) {
    var selectedSiteId by remember { mutableStateOf<String?>(null) }
    val selectedSite = selectedSiteId?.let { id -> state.sites.firstOrNull { it.id == id } }
    val forecast = V3GameEngine.monthlyForecast(state)
    val progression = V3ProgressionEngine.snapshot(state)

    V3Section("家业", "第${progression.chapter.number}章 · ${progression.chapter.title} · ${progression.chapter.theme}")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        V3CountyHomePage.entries.forEach { page ->
            V3SmallButton(
                page.label,
                Modifier.weight(1f),
                selected = page == selectedPage
            ) { onPageChange(page) }
        }
    }
    when (selectedPage) {
        V3CountyHomePage.Overview -> {
            V3ActionCenterPanel(progression, controller, onNavigate = { destination ->
                if (destination == V3Screen.County) onPageChange(V3CountyHomePage.Map)
                else controller.switchScreen(destination)
            })
            V3MonthlyCardsPanel(state, controller)
            V3ClanLedgerPanel(
                state,
                Modifier.guideTarget(V3GuideFocus.MonthlyLedger, guideTargets),
                onClick = { controller.showInfo("族中月账：人丁耗粮是所有活着的族人每月口粮；乡勇耗粮是兵册维护费；险地是风险达到55以上的地点。银粮收支会在每月结算时真正改变库存。") }
            )
            V3Panel(Modifier.guideTarget(V3GuideFocus.MonthlyForecast, guideTargets)) {
                Text("本月账本", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(forecast.summary, color = V3Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    V3Metric("入银", forecast.silverIncome, V3Gold, Modifier.weight(1f))
                    V3Metric("出银", forecast.silverExpense, V3Red, Modifier.weight(1f))
                    V3Metric("入粮", forecast.grainIncome, V3Green, Modifier.weight(1f))
                    V3Metric("出粮", forecast.grainExpense, V3Red, Modifier.weight(1f))
                }
                Text("经营说明：田庄与佃田主产粮，集市、铺面和商队主产银；地点控制越高、风险越低，固定月产越多。人口和乡勇每月消耗粮食。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
                V3SmallButton(
                    "一键安排本月派遣与培养",
                    Modifier.fillMaxWidth().guideTarget(V3GuideFocus.AutoArrange, guideTargets),
                    selected = true
                ) {
                    controller.autoArrangeMonth()
                    controller.advanceTutorial(15)
                }
            }
            V3RouteOverviewPanel(state, controller)
            V3Panel {
                Text("时局脉络", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(mingSituationText(state), color = V3Ink, fontSize = 14.sp, lineHeight = 21.sp)
            }
            V3Panel(Modifier.guideTarget(V3GuideFocus.AnnualGoals, guideTargets)) {
                Text("年务支线", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("年务提供额外资源与路线奖励，不会取代上方章节主线。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
                state.annualGoals.take(3).forEach { goal -> V3GoalRow(state, goal, controller) }
            }
        }
        V3CountyHomePage.Map -> {
            V3Panel {
                Text("县域地图", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("点击地点图钉打开管理；风险达到 55 以上的地点会优先显示治理提示。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
            }
            V3CountyMapView(
                state,
                guideModifier = Modifier.guideTarget(V3GuideFocus.CountyMap, guideTargets),
                guideTargets = guideTargets,
                guideActions = guideActions,
                tutorialStep = state.tutorialStep,
                onGuideClick = { controller.advanceTutorial(4) }
            ) { siteId ->
                selectedSiteId = siteId
                if (siteId == "farmland") controller.advanceTutorial(5)
            }
            V3EstatePanel(state, controller, Modifier.guideTarget(V3GuideFocus.EstateOverview, guideTargets))
        }
        V3CountyHomePage.Archive -> {
            V3ArchivePanel(state, controller)
            V3Panel {
                Text("家产档案", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("低频档案集中在这里，不再挤占经营总览。", color = V3Muted, fontSize = 12.sp)
                Text("家产建设与县域地点管理请切换到“县域”页。", color = V3Ink, fontSize = 13.sp)
            }
        }
    }
    selectedSite?.let { site ->
        V3SiteManageDialog(site, state, controller, state.tutorialStep) { selectedSiteId = null }
    }
}

@Composable
private fun V3ActionCenterPanel(
    progression: V3ProgressionSnapshot,
    controller: V3GameController,
    onNavigate: (V3Screen) -> Unit = controller::switchScreen
) {
    val quest = progression.mainQuest
    val primary = progression.primaryAction
    V3Panel {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("当家要务 · 第${progression.chapter.number}章", color = V3Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(progression.chapter.title, color = V3Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Text("${progression.chapterProgress}%", color = V3Green, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Box(Modifier.fillMaxWidth().height(9.dp).background(V3PaperDeep, V3SoftShape)) {
            Box(
                Modifier
                    .fillMaxWidth((progression.chapterProgress.coerceIn(0, 100) / 100f).coerceAtLeast(0.02f))
                    .height(9.dp)
                    .background(V3Gold, V3SoftShape)
            )
        }
        Text(quest.title, color = V3Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(quest.description, color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
        quest.conditions.chunked(2).forEach { rowConditions ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                rowConditions.forEach { condition ->
                    Column(
                        Modifier
                            .weight(1f)
                            .background(if (condition.satisfied) V3Green.copy(alpha = 0.16f) else V3PaperDeep, V3SoftShape)
                            .border(1.dp, if (condition.satisfied) V3Green.copy(alpha = 0.65f) else V3Border.copy(alpha = 0.65f), V3SoftShape)
                            .padding(7.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(condition.label, color = V3Muted, fontSize = 10.sp, maxLines = 1)
                        Text(condition.progressText, color = if (condition.satisfied) V3Green else V3Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                repeat(2 - rowConditions.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        if (quest.rewardText.isNotBlank()) {
            Text("章节奖励：${quest.rewardText}", color = V3Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text("下一解锁：${progression.nextUnlock}", color = V3Blue, fontSize = 11.sp, lineHeight = 16.sp)
        Column(
            Modifier
                .fillMaxWidth()
                .background(if (primary.priority == com.arktools.daming.v3.logic.V3ActionPriority.Critical) V3Red.copy(alpha = 0.14f) else V3PaperDeep, V3SoftShape)
                .padding(9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("${primary.priority.label} · ${primary.title}", color = if (primary.priority == com.arktools.daming.v3.logic.V3ActionPriority.Critical) V3Red else V3Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(primary.reason, color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
            Text("预期：${primary.expectedImpact}", color = V3Muted, fontSize = 11.sp, lineHeight = 16.sp)
        }
        progression.claimableReward?.let { reward ->
            V3SmallButton(
                "领取第${reward.chapter.number}章奖励 · ${reward.text}",
                Modifier.fillMaxWidth(),
                selected = true
            ) { controller.claimChapterReward(reward.chapter) }
        } ?: V3SmallButton(primary.actionLabel, Modifier.fillMaxWidth(), enabled = primary.canExecute, selected = true) {
            onNavigate(primary.destination)
        }
        if (progression.recommendedActions.size > 1) {
            Text("随后可做", color = V3Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            progression.recommendedActions.drop(1).take(2).forEach { action ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                        if (action.destination == V3Screen.County) {
                            onNavigate(V3Screen.County)
                        } else {
                            onNavigate(action.destination)
                        }
                    }
                        .background(V3PaperDeep, V3SoftShape)
                        .padding(horizontal = 9.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${action.priority.label} · ${action.title}", color = V3Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("前往", color = V3Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
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
    MonthlyForecast,
    CountyMap,
    FarmlandPin,
    SiteOverview,
    SiteActions,
    SiteClose,
    EstateOverview,
    AnnualGoals,
    AutoArrange,
    ClanOverview,
    Marriage,
    MarriagePerson,
    MarriageCandidate,
    ClanPromotion,
    Genealogy,
    PersonOverview,
    PersonTraining,
    PersonTask,
    MonthlyReport,
    MonthlyReportDismiss,
    EventChoice,
    StrategyTabs,
    StrategyContent,
    Relations,
    Council,
    WorldTab,
    MilitaryTab,
    RecentTab,
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
    val strategyPage: String? = null,
    val requiresAction: Boolean = false
)

private fun elderGuideSteps(state: V3GameState): List<V3ElderGuideStep> {
    val steps = listOf(
    V3ElderGuideStep(V3Screen.County, "族老", "male_elder", "第一章 · 先认家底", "顶栏记录宗族、年月、危机和当前品第。经营不是盲点按钮：先看年月与危机，再决定这个月保粮、挣钱、育人还是压风险。", "本页只作说明，无需点击顶栏；读完点击下一步", V3GuideFocus.TopBar),
    V3ElderGuideStep(V3Screen.County, "沈账房", "male_scholar", "银、粮、人、业", "银两用于婚配、营建、议事和军务；粮食供养族人与乡勇；人口决定能派多少人；产业决定稳定月产。任何一项见底，家族都会失去周转能力。", "读完后点击下一步；平时可点击资源查看说明", V3GuideFocus.Resources),
    V3ElderGuideStep(V3Screen.County, "沈账房", "male_scholar", "先看固定负担", "族中月账显示每月必付的人丁口粮、乡勇维护和高风险地点。扩人口、养兵之前，必须先确认粮仓能否承受。", "读完后点击下一步", V3GuideFocus.MonthlyLedger),
    V3ElderGuideStep(V3Screen.County, "沈账房", "male_scholar", "再看本月收支", "本月账本预估银粮收支。田庄和佃田补粮，集市、铺面和商队生银；地点控制越高、风险越低，收入越稳定。", "读完后点击下一步", V3GuideFocus.MonthlyForecast),
    V3ElderGuideStep(V3Screen.County, "周管事", "male_middle", "第二章 · 县域产业", "地图上的每个地点都有等级、控制、风险和专属用途。接下来会请你亲手打开南乡田庄。", "先了解地图，读完后点击下一步", V3GuideFocus.CountyMap),
    V3ElderGuideStep(V3Screen.County, "周管事", "male_middle", "打开南乡田庄", "田庄是前期粮食根基。请点击地图中高亮的南乡田庄图钉，打开真实的地点管理。", "点击南乡田庄图钉", V3GuideFocus.FarmlandPin, requiresAction = true),
    V3ElderGuideStep(V3Screen.County, "周管事", "male_middle", "读懂地点详情", "月产说明这个地点每月带来什么；控制越高，收益越稳；风险越高，越容易减产或出事；等级决定基础产量。", "在田庄详情中查看高亮信息，读完点击下一步", V3GuideFocus.SiteOverview),
    V3ElderGuideStep(V3Screen.County, "周管事", "male_middle", "地点如何经营", "专属事务会立即改变地点或关系；营建升级提高长期产量；派遣族人管田、治理或赈济，则在月结时改善控制、风险和收益。", "查看经营按钮和派遣说明", V3GuideFocus.SiteActions),
    V3ElderGuideStep(V3Screen.County, "周管事", "male_middle", "返回家业总览", "地点详情只是查看和建设入口；给谁派什么差事，要到族人详情中决定。现在关闭田庄详情。", "点击关闭，返回家业页", V3GuideFocus.SiteClose, requiresAction = true),
    V3ElderGuideStep(V3Screen.County, "沈账房", "male_scholar", "家产是第二条收入线", "县域地图下方的家产管理包含佃田、铺面、作坊、粮仓、商队和团练营。这里先认识长期建设入口，不要求现在花费资源。", "查看高亮的家产管理区域，读完点击下一步", V3GuideFocus.EstateOverview),
    V3ElderGuideStep(V3Screen.County, "族老", "male_elder", "年务是额外目标", "总览页底部的年务支线提供额外资源与路线奖励，不会取代顶部章节主线。这里先认识它的位置与作用。", "查看高亮的年务支线，读完点击下一步", V3GuideFocus.AnnualGoals),
    V3ElderGuideStep(V3Screen.People, "族老", "male_elder", "第三章 · 人才经营", "产业不会自己运转。族谱展示亲子关系和每个人当前状态；真正的培养、派差和科举都从族人详情开始。", "点击高亮族人卡片，打开详情", V3GuideFocus.Genealogy, requiresAction = true),
    V3ElderGuideStep(V3Screen.People, "族老", "male_elder", "读懂一个族人", "学、武、商、谋决定适合的差事；忠影响宗族稳定；绩记录功劳；劳过高会降低办事效果。先看清人，再安排。", "在族人详情中查看高亮信息，读完点击下一步", V3GuideFocus.PersonOverview),
    V3ElderGuideStep(V3Screen.People, "族老", "male_elder", "培养决定长期成长", "读书、习武、学算、礼法分别提高学、武、商、谋。培养会占用本月行动，所以不能同时派差；儿童不能外出，但培养成长更快。", "查看高亮培养区域，读完点击下一步", V3GuideFocus.PersonTraining),
    V3ElderGuideStep(V3Screen.People, "周管事", "male_middle", "亲手派一次差事", "系统会给出推荐差事。请点击高亮的可用差事，让这名族人本月真正去经营对应地点；月结时才会兑现结果。", "点击高亮差事按钮", V3GuideFocus.PersonTask, requiresAction = true),
    V3ElderGuideStep(V3Screen.County, "周管事", "male_middle", "自动安排其余人手", "手动派差适合精细经营；人多以后可用一键安排，让系统根据缺粮、缺银、地点风险和族人所长处理其余待命者。", "点击一键安排本月派遣与培养", V3GuideFocus.AutoArrange, requiresAction = true),
    V3ElderGuideStep(V3Screen.County, "族老", "male_elder", "第四章 · 完成一个经营月", "安排只是计划，必须推进月结才会获得产出、成长、婚育进度与事件结果。点击继续，真实推进一个月。", "点击继续，推进一个月", V3GuideFocus.TimeControls, requiresAction = true),
    V3ElderGuideStep(V3Screen.County, "沈账房", "male_scholar", "阅读月报", "月报会列出银粮变化、地点经营、族人成长和目标进度。不要只看库存数字，要从月报判断下个月该补什么。", "阅读月报后点击知道了", V3GuideFocus.MonthlyReportDismiss, requiresAction = true),
    V3ElderGuideStep(V3Screen.County, "族老", "male_elder", "处理月度事件", "事件选择会真实改变银粮、关系、族望、凝聚与路线。先看代价和后果，再选择最符合当前家底和长期路线的方案。", "选择一个事件方案；若本月无事件则继续", V3GuideFocus.EventChoice, requiresAction = true),
    V3ElderGuideStep(V3Screen.Clan, "族老", "male_elder", "第五章 · 宗族成长", "宗族概况中的凝聚、族望、品第和乡勇分别代表内部稳定、外部声望、系统解锁与防务规模。这里先认识四项含义。", "查看高亮的宗族概况，读完点击下一步", V3GuideFocus.ClanOverview),
    V3ElderGuideStep(V3Screen.Clan, "媒婆", "female_middle", "婚配是人口循环起点", "婚配区先选择待婚族人，再查看礼金、粮耗、属性与路线倾向。婚后还要经历备孕和孕期，不会立刻增加人口。", "查看高亮的婚配区域，读完点击下一步", V3GuideFocus.Marriage),
    V3ElderGuideStep(V3Screen.Clan, "媒婆", "female_middle", "先选为谁议亲", "待婚名单只包含符合年龄与婚姻状态的族人。选择不同族人，候选对象也会变化。", "点击高亮的待婚族人", V3GuideFocus.MarriagePerson, requiresAction = true),
    V3ElderGuideStep(V3Screen.Clan, "媒婆", "female_middle", "看清对象再成婚", "高亮候选卡会显示对象属性、礼金、粮耗和路线倾向。教程只说明这些信息，不强迫你支付资源成婚。", "查看高亮的候选对象卡，读完点击下一步", V3GuideFocus.MarriageCandidate),
    V3ElderGuideStep(V3Screen.Clan, "族老", "male_elder", "品第是主要解锁线", "高亮的宗族晋升区列出下一品第所需银、粮、人口、产业和族望，并明确晋升后开放的玩法。", "查看高亮的晋升要求，读完点击下一步", V3GuideFocus.ClanPromotion),
    V3ElderGuideStep(V3Screen.Strategy, "军师", "male_scholar", "第六章 · 长期路线", "高亮的路线评估会根据产业、培养、议事、军务和事件选择计算当前主路线与终局倾向。", "查看高亮的路线评估，读完点击下一步", V3GuideFocus.StrategyContent, "声势"),
    V3ElderGuideStep(V3Screen.Strategy, "军师", "male_scholar", "地方关系", "高亮区域列出官府、士绅、乡民、商帮、军镇和流寇关系；极端关系会改变事件、经营和战争难度。", "查看高亮的地方关系，读完点击下一步", V3GuideFocus.Relations, "声势"),
    V3ElderGuideStep(V3Screen.Strategy, "军师", "male_scholar", "宗族议事", "高亮区域是宗族议事。当前未解锁时会显示明确条件；升为小族后，每月可议定一次家政方略。", "查看高亮的议事区域或解锁条件，读完点击下一步", V3GuideFocus.Council, "声势"),
    V3ElderGuideStep(V3Screen.Strategy, "军师", "male_scholar", "天下经营", "升为望族后可跨县结交、经营和征伐。先接触，再提升影响，最后控制地域；不要在县内根基不稳时急着扩张。", "点击天下页签，查看解锁条件", V3GuideFocus.WorldTab, "天下", requiresAction = true),
    V3ElderGuideStep(V3Screen.Strategy, "武教头", "male_middle", "军务与防务", "小族开放基础募兵，望族和团练营开放精兵与征伐。兵越多每月耗粮越高；装备要购买、穿戴和修复，出战还要选择合适将领。", "点击军务页签，查看解锁条件", V3GuideFocus.MilitaryTab, "军务", requiresAction = true),
    V3ElderGuideStep(V3Screen.Strategy, "沈账房", "male_scholar", "近事是经营记录", "近事记录每月大事、选择和结果。忘记上一月发生了什么时，先来这里复盘，再决定下一步。", "点击近事页签", V3GuideFocus.RecentTab, "近事", requiresAction = true),
    V3ElderGuideStep(V3Screen.County, "族老", "male_elder", "完整经营循环", "每月按这个顺序：看危机与目标 → 核银粮收支 → 查看地点风险 → 按族人所长培养或派差 → 决定婚配、营建与议事 → 推进月结 → 阅读月报和事件 → 根据路线调整下月计划。", "点击月账，复习经营循环", V3GuideFocus.MonthlyLedger),
    V3ElderGuideStep(V3Screen.County, "族老", "male_elder", "现在由你当家", "前期先让田庄保粮、集市生银，并为族人安排合适差事；中期靠婚育、产业和族望晋升；后期再进入议事、天下与军务。遇到不懂的数值就点击面板说明，也可在大势页重开族老札记。", "完成完整引导", V3GuideFocus.MonthlyLedger)
    )
    check(steps.size == V3GameController.TUTORIAL_STEP_COUNT)
    check(
        steps.indices.filter { steps[it].requiresAction }.toSet() ==
            V3GameController.TUTORIAL_ACTION_STEPS
    )
    return steps
}

@Composable
private fun V3LocalGuideOverlay(
    state: V3GameState,
    controller: V3GameController,
    targetBounds: Rect?,
    cardAtTop: Boolean
) {
    val steps = elderGuideSteps(state)
    val safeIndex = state.tutorialStep.coerceIn(0, steps.lastIndex)
    val step = steps[safeIndex]
    Box(Modifier.fillMaxSize()) {
        V3GuideFocusFrame(
            targetBounds = targetBounds,
            blockInput = step.requiresAction && targetBounds != null
        )
        Column(
            Modifier
                .align(if (cardAtTop) Alignment.TopCenter else Alignment.BottomCenter)
                .padding(10.dp)
                .fillMaxWidth()
                .background(V3Rice, V3PanelShape)
                .border(2.dp, V3Gold, V3PanelShape)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(step.title, color = V3Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(step.words, color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
            Text(step.action, color = V3Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (step.requiresAction && targetBounds == null) {
                Text(
                    "正在定位可操作目标，请稍候。目标出现前不会要求你盲点。",
                    color = V3Red,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("步骤 ${safeIndex + 1}/${steps.size}", color = V3Muted, fontSize = 10.sp)
            if (step.requiresAction) {
                V3SmallButton(
                    if (targetBounds == null) "正在定位目标" else "请完成高亮操作",
                    Modifier.fillMaxWidth(),
                    enabled = false,
                    selected = true
                ) {}
            } else {
                V3SmallButton("下一步", Modifier.fillMaxWidth(), selected = true) {
                    controller.advanceTutorial(safeIndex)
                }
            }
        }
    }
}

@Composable
private fun V3ElderGuideOverlay(
    state: V3GameState,
    controller: V3GameController,
    targetBounds: Rect?,
    cardAtTop: Boolean,
    onStrategyPageChange: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val steps = elderGuideSteps(state)
    val safeIndex = state.tutorialStep.coerceIn(0, steps.lastIndex)
    val step = steps[safeIndex]
    LaunchedEffect(safeIndex) {
        onStrategyPageChange(step.strategyPage)
        controller.playGuideTick()
    }
    val cardAlignment = if (cardAtTop) Alignment.TopCenter else Alignment.BottomCenter
    Box(Modifier.fillMaxSize()) {
        V3GuideFocusFrame(
            targetBounds = targetBounds,
            blockInput = step.requiresAction && targetBounds != null
        )
        Box(
            Modifier
                .align(cardAlignment)
                .padding(horizontal = 12.dp)
                .padding(
                    top = if (cardAtTop) 18.dp else 0.dp,
                    bottom = if (cardAtTop) 0.dp else 82.dp
                )
                .fillMaxWidth()
                .widthIn(max = 680.dp)
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
                Text(step.words, color = V3Ink, fontSize = 13.sp, lineHeight = 19.sp)
                Text(
                    if (safeIndex == steps.lastIndex) "全部操作已完成，可收下族老札记。" else step.action,
                    color = V3Gold,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (step.requiresAction && targetBounds == null) {
                    Text(
                        "正在定位可操作目标，请稍候。目标出现前不会要求你盲点。",
                        color = V3Red,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                    } else if (!step.requiresAction) {
                        V3SmallButton("下一步", Modifier.weight(2f), selected = true) {
                            controller.advanceTutorial(safeIndex)
                        }
                    } else {
                        V3SmallButton(
                            if (targetBounds == null) "正在定位目标" else "请完成高亮操作",
                            Modifier.weight(2f),
                            enabled = false,
                            selected = true
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun V3GuideFocusFrame(targetBounds: Rect?, blockInput: Boolean) {
    val density = LocalDensity.current
    val paddingPx = with(density) { 8.dp.toPx() }
    val cornerPx = with(density) { 12.dp.toPx() }
    val borderPx = with(density) { 3.dp.toPx() }
    // 遮罩仅负责视觉高亮，不接管指针事件；目标控件保持真实可点击。
    Box(Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val hole = if (targetBounds != null) {
                val left = (targetBounds.left - paddingPx).coerceAtLeast(0f)
                val top = (targetBounds.top - paddingPx).coerceAtLeast(0f)
                val right = (targetBounds.right + paddingPx).coerceAtMost(size.width)
                val bottom = (targetBounds.bottom + paddingPx).coerceAtMost(size.height)
                androidx.compose.ui.geometry.RoundRect(
                    left = left,
                    top = top,
                    right = right,
                    bottom = bottom,
                    radiusX = cornerPx,
                    radiusY = cornerPx
                )
            } else null

            // 离屏合成，用 Clear 混合模式挖洞
            drawIntoCanvas { canvas ->
                canvas.saveLayer(
                    Rect(0f, 0f, size.width, size.height),
                    Paint()
                )
                // 先画满半透明黑色
                drawRect(color = Color(0x99000000))
                if (hole != null) {
                    // 再用 Clear 把目标区域掏空（露出底层控件颜色）
                    val holePath = Path().apply {
                        addRoundRect(hole)
                    }
                    drawPath(
                        path = holePath,
                        color = Color.Transparent,
                        blendMode = BlendMode.Clear
                    )
                }
                canvas.restore()
            }

            // 在洞边画金色描边（在挖洞之后，不受 Clear 影响）
            if (hole != null) {
                val borderPath = Path().apply { addRoundRect(hole) }
                drawPath(
                    path = borderPath,
                    color = V3Gold,
                    style = Stroke(width = borderPx, cap = StrokeCap.Round)
                )
            }
        }
        if (blockInput) V3GuideInputBlockers(targetBounds)
    }
}

@Composable
private fun V3GuideInputBlockers(targetBounds: Rect?) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val padding = 8.dp
    fun Modifier.blockGuideInput(): Modifier = pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                awaitPointerEvent().changes.forEach { it.consume() }
            }
        }
    }
    if (targetBounds == null) {
        Box(Modifier.fillMaxSize().blockGuideInput())
        return
    }
    val left = with(density) { targetBounds.left.toDp() } - padding
    val top = with(density) { targetBounds.top.toDp() } - padding
    val right = with(density) { targetBounds.right.toDp() } + padding
    val bottom = with(density) { targetBounds.bottom.toDp() } + padding
    val safeLeft = left.coerceIn(0.dp, screenWidth)
    val safeTop = top.coerceIn(0.dp, screenHeight)
    val safeRight = right.coerceIn(0.dp, screenWidth)
    val safeBottom = bottom.coerceIn(0.dp, screenHeight)
    Box(Modifier.fillMaxWidth().height(safeTop).blockGuideInput())
    Box(Modifier.offset(y = safeBottom).fillMaxWidth().height((screenHeight - safeBottom).coerceAtLeast(0.dp)).blockGuideInput())
    Box(Modifier.offset(y = safeTop).width(safeLeft).height((safeBottom - safeTop).coerceAtLeast(0.dp)).blockGuideInput())
    Box(
        Modifier
            .offset(x = safeRight, y = safeTop)
            .width((screenWidth - safeRight).coerceAtLeast(0.dp))
            .height((safeBottom - safeTop).coerceAtLeast(0.dp))
            .blockGuideInput()
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
    guideTargets: MutableMap<V3GuideFocus, Rect>,
    guideActions: MutableMap<V3GuideFocus, () -> Unit>
) {
    V3Section("宗族", "${V3GameEngine.clanRankName(state)} · 人口 ${V3GameEngine.alivePeople(state).size} · 产业 ${V3GameEngine.builtSiteCount(state)}")
    // 族长属性和继任属于宗族页，不占用县域经营首页。
    V3PatriarchPanel(state, controller)
    V3Panel {
        Text("族长履历", color = V3Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(state.biography.lastOrNull() ?: "尚无记载", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
    }
    V3Panel(Modifier.guideTarget(V3GuideFocus.ClanOverview, guideTargets)) {
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
    V3Panel {
        Text(
            "婚配与提亲",
            color = V3Red,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .guideTarget(V3GuideFocus.Marriage, guideTargets)
                .clickable { controller.advanceTutorial(20) }
                .padding(vertical = 6.dp)
        )
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
                            Modifier
                                .weight(1f)
                                .then(
                                    if (person.id == eligible.firstOrNull()?.id) {
                                        Modifier.guideTarget(
                                            V3GuideFocus.MarriagePerson,
                                            guideTargets,
                                            guideActions
                                        ) {
                                            selectedMarriagePersonId = person.id
                                            controller.advanceTutorial(21)
                                        }
                                    } else Modifier
                                ),
                            selected = target?.id == person.id
                        ) {
                            selectedMarriagePersonId = person.id
                            controller.advanceTutorial(21)
                        }
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
                    Card(
                        modifier = if (option.id == candidates.firstOrNull()?.id) {
                            Modifier
                                .guideTarget(V3GuideFocus.MarriageCandidate, guideTargets)
                                .clickable { controller.advanceTutorial(22) }
                        } else Modifier,
                        colors = CardDefaults.cardColors(containerColor = V3PaperDeep),
                        border = BorderStroke(2.dp, V3Gold),
                        shape = V3SoftShape
                    ) {
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
    V3Panel {
        Text(
            "宗族晋升",
            color = V3Red,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .guideTarget(V3GuideFocus.ClanPromotion, guideTargets)
                .clickable { controller.advanceTutorial(23) }
                .padding(vertical = 6.dp)
        )
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
    guideTargets: MutableMap<V3GuideFocus, Rect>,
    guideActions: MutableMap<V3GuideFocus, () -> Unit>
) {
    val people = V3GameEngine.alivePeople(state)
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    val person = selectedPersonId?.let { id -> people.firstOrNull { it.id == id } }
    V3Section("族谱", "可拖动查看大族树状图；点小卡片弹出族人详情与培养安排。")
    V3Panel {
        Text("族人总管", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("待命族人较多时，无需逐个点开安排。系统会优先处理险地与收支缺口，再按族人所长培养其余人。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
        V3SmallButton("一键安排全部待命族人", Modifier.fillMaxWidth(), selected = true) {
            controller.autoArrangeMonth()
        }
    }
    V3GenealogyTree(
        state.clanName,
        people,
        guideTargets = guideTargets,
        guideActions = guideActions,
        onSelect = {
            selectedPersonId = it
            controller.advanceTutorial(11)
        }
    )
    person?.let { selected ->
        V3PersonDetailDialog(
            person = selected,
            state = state,
            controller = controller,
            tutorialStep = state.tutorialStep,
            onDismiss = { selectedPersonId = null }
        )
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
    guideTargets: MutableMap<V3GuideFocus, Rect>,
    guideActions: MutableMap<V3GuideFocus, () -> Unit>,
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
    LaunchedEffect(viewportSize, people.firstOrNull()?.id) {
        val firstPerson = people.firstOrNull() ?: return@LaunchedEffect
        val target = positions[firstPerson.id] ?: return@LaunchedEffect
        if (viewportSize != IntSize.Zero) {
            val centeredX = (viewportSize.width.toFloat() - contentWidthPx) / 2f
            pan = Offset(
                viewportSize.width * 0.5f - (target.x + nodeWidth * 0.5f) * density.density - centeredX,
                0f
            )
        }
    }
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
                    .offset {
                        IntOffset(
                            (pan.x + centeredX).roundToInt(),
                            pan.y.roundToInt()
                        )
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
                    V3FamilyMiniNode(
                        person,
                        x = position.x,
                        y = position.y,
                        modifier = if (person.id == people.firstOrNull()?.id) {
                            Modifier.guideTarget(
                                V3GuideFocus.Genealogy,
                                guideTargets,
                                guideActions
                            ) { onSelect(person.id) }
                        } else Modifier,
                        onSelect = onSelect
                    )
                }
            }
        }
    }
}

@Composable
private fun V3FamilyMiniNode(
    person: V3Person,
    x: Int,
    y: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
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
            .then(modifier)
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
private fun V3PersonDetailDialog(
    person: V3Person,
    state: V3GameState,
    controller: V3GameController,
    tutorialStep: Int,
    onDismiss: () -> Unit
) {
    val localTargets = remember { mutableStateMapOf<V3GuideFocus, Rect>() }
    val focus = when (tutorialStep) {
        12 -> V3GuideFocus.PersonOverview
        13 -> V3GuideFocus.PersonTraining
        14 -> V3GuideFocus.PersonTask
        else -> null
    }
    val localScroll = rememberScrollState()
    LaunchedEffect(tutorialStep, localScroll.maxValue) {
        val fraction = when (tutorialStep) {
            12 -> 0f
            13 -> 0.50f
            14 -> 1f
            else -> return@LaunchedEffect
        }
        delay(100)
        localScroll.scrollTo((localScroll.maxValue * fraction).toInt())
    }
    Dialog(onDismissRequest = { if (focus == null) onDismiss() }) {
        Box {
            V3ImagePanel(
                GameImages.V3UiEventPanel,
                Modifier
                    .widthIn(max = 480.dp)
                    .heightIn(max = 680.dp)
                    .verticalScroll(localScroll)
            ) {
                V3PersonCard(
                    person,
                    state,
                    controller,
                    framed = false,
                    guideTargets = localTargets,
                    tutorialStep = tutorialStep,
                    onTaskAssigned = {
                        controller.advanceTutorial(14)
                        onDismiss()
                    }
                )
                V3SmallButton("关闭", Modifier.fillMaxWidth(), selected = true, onClick = onDismiss)
            }
            focus?.let { currentFocus ->
                val bounds = localTargets[currentFocus]
                V3LocalGuideOverlay(
                    state = state,
                    controller = controller,
                    targetBounds = bounds,
                    cardAtTop = (bounds?.center?.y ?: 0f) > 360f
                )
            }
        }
    }
}

@Composable
private fun V3StrategyPage(
    state: V3GameState,
    controller: V3GameController,
    forcedPage: String?,
    guideTargets: MutableMap<V3GuideFocus, Rect>,
    guideActions: MutableMap<V3GuideFocus, () -> Unit>,
    openGuide: () -> Unit
) {
    val ending = V3GameEngine.endingPreview(state)
    var page by remember { mutableStateOf(forcedPage ?: "声势") }
    LaunchedEffect(forcedPage) {
        if (forcedPage != null) page = forcedPage
    }
    // 大势教程也必须由玩家点击高亮内容或页签完成。
    V3Section("大势", "当前路线：${V3GameEngine.dominantRoute(state).label}")
    V3Panel(Modifier.guideTarget(V3GuideFocus.StrategyContent, guideTargets)) {
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
                val focus = when (label) {
                    "天下" -> V3GuideFocus.WorldTab
                    "军务" -> V3GuideFocus.MilitaryTab
                    "近事" -> V3GuideFocus.RecentTab
                    else -> V3GuideFocus.StrategyTabs
                }
                val requiredStep = when (label) {
                    "天下" -> 27
                    "军务" -> 28
                    "近事" -> 29
                    else -> -1
                }
                val action = {
                    page = label
                    if (requiredStep >= 0) controller.advanceTutorial(requiredStep)
                }
                V3SmallButton(
                    label,
                    Modifier
                        .weight(1f)
                        .guideTarget(focus, guideTargets, guideActions, action),
                    selected = page == label,
                    onClick = action
                )
            }
        }
    }
    when (page) {
        "声势" -> {
            Column {
            V3Panel(Modifier.guideTarget(V3GuideFocus.Relations, guideTargets)) {
                Text("地方关系", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                V3RelationRow("官府", state.relations.yamen)
                V3RelationRow("士绅", state.relations.gentry)
                V3RelationRow("乡民", state.relations.villagers)
                V3RelationRow("流寇", state.relations.bandits)
                V3RelationRow("商帮", state.relations.merchants)
                V3RelationRow("军镇", state.relations.garrison)
            }
            Box(Modifier.guideTarget(V3GuideFocus.Council, guideTargets)) {
                if (V3GameEngine.isUnlocked(state, "Council")) {
                    V3CouncilPanel(state, controller)
                } else {
                    V3LockedFeaturePanel("宗族议事", "升为小族（2级）后，每月可议定一次家政方略。")
                }
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
            Text("解锁：募兵 ${if (recruitUnlocked) "已开" else "小族/寨堡"} · 精兵 ${if (advancedUnlocked) "已开" else "望族+团练营"} · 征伐 ${if (conquestUnlocked) "已开" else "望族"} · 举旗 ${if (bannerUnlocked) "已开" else "县中大姓+兵80"}", color = V3Muted, fontSize = 11.sp, lineHeight = 16.sp)
            V3TroopType.entries.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { type ->
                        val enabled = type == V3TroopType.Militia || advancedUnlocked || (type == V3TroopType.Cavalry && state.clanRank >= 4)
                        V3SmallButton("募${type.label}×5", Modifier.weight(1f), enabled = recruitUnlocked && enabled) { controller.recruitTroops(type, 5) }
                    }
                    repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            V3Panel {
                Text("两类战事分工", color = V3Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("地点讨伐用于主动清剿县域高风险地点；六门守庄只在甲申前夕触发一次，是终章家庄防御。两类战事与地域征伐互斥，不会同时开启。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
                V3SmallButton(
                    "讨伐最高风险地点",
                    Modifier.fillMaxWidth(),
                    selected = true,
                    enabled = state.army.total() >= 15 && !V3GameEngine.hasBlockingEncounter(state)
                ) { controller.startBattle() }
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
            region.accordRoute?.let { route ->
                Text(
                    "归附条约：${route.label} · 每月${V3GameEngine.accordBenefitText(route, region.tier)}",
                    color = V3Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp
                )
            }
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
    guideModifier: Modifier = Modifier,
    guideTargets: MutableMap<V3GuideFocus, Rect>,
    guideActions: MutableMap<V3GuideFocus, () -> Unit>,
    tutorialStep: Int,
    onGuideClick: () -> Unit = {},
    onSelectSite: (String) -> Unit
) {
    var pan by remember { mutableStateOf(Offset.Zero) }
    val frameHeight = 560.dp
    val frameShape = V3SoftShape
    val density = LocalDensity.current
    V3Panel(modifier) {
        Row(
            guideModifier
                .fillMaxWidth()
                .clickable(onClick = onGuideClick)
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            LaunchedEffect(tutorialStep, frameWidthPx, frameHeightPx) {
                if (tutorialStep == 5) {
                    val farmland = siteMapPoint("farmland")
                    val pinHeightPx = with(density) { 110.dp.toPx() }
                    pan = Offset(
                        (frameWidthPx * 0.5f - mapWidthPx * farmland.x).coerceIn(minPanX, 0f),
                        (frameHeightPx * 0.42f - mapHeightPx * farmland.y + pinHeightPx * 0.15f).coerceIn(minPanY, 0f)
                    )
                }
            }
            val boundedPan = Offset(pan.x.coerceIn(minPanX, 0f), pan.y.coerceIn(minPanY, 0f))
            Box(
                Modifier.fillMaxSize().freeMapDrag { dragAmount ->
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
                        .offset {
                            IntOffset(
                                boundedPan.x.roundToInt(),
                                boundedPan.y.roundToInt()
                            )
                        }
                ) {
                    AssetImage(GameImages.V3MapBgPlain, null, Modifier.fillMaxSize(), ContentScale.FillBounds)
                    state.sites.forEach { site ->
                        V3MapSitePin(
                            site,
                            mapWidthPx = mapWidthPx,
                            mapHeightPx = mapHeightPx,
                            unlocked = V3GameEngine.isSiteUnlocked(state, site.type),
                            requiredRank = V3GameEngine.siteRequiredRank(site.type),
                            modifier = if (site.id == "farmland") {
                                Modifier.guideTarget(
                                    V3GuideFocus.FarmlandPin,
                                    guideTargets,
                                    guideActions
                                ) { onSelectSite(site.id) }
                            } else Modifier
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
    modifier: Modifier = Modifier,
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
        Modifier
            .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
            .then(modifier)
            .width(92.dp)
            .clickable(onClick = onClick),
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
private fun V3SiteManageDialog(
    site: V3CountySite,
    state: V3GameState,
    controller: V3GameController,
    tutorialStep: Int,
    onDismiss: () -> Unit
) {
    val localTargets = remember { mutableStateMapOf<V3GuideFocus, Rect>() }
    val focus = when (tutorialStep) {
        6 -> V3GuideFocus.SiteOverview
        7 -> V3GuideFocus.SiteActions
        8 -> V3GuideFocus.SiteClose
        else -> null
    }
    val localScroll = rememberScrollState()
    LaunchedEffect(tutorialStep, localScroll.maxValue) {
        val fraction = when (tutorialStep) {
            6 -> 0f
            7 -> 0.72f
            8 -> 1f
            else -> return@LaunchedEffect
        }
        delay(100)
        localScroll.scrollTo((localScroll.maxValue * fraction).toInt())
    }
    Dialog(onDismissRequest = { if (focus == null) onDismiss() }) {
        Box {
            V3ImagePanel(
                GameImages.V3UiEventPanel,
                Modifier
                    .widthIn(max = 470.dp)
                    .heightIn(max = 680.dp)
                    .verticalScroll(localScroll)
            ) {
                Text(
                    "${site.name} 管理",
                    color = V3Ink,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                V3SiteCard(
                    site,
                    state,
                    controller,
                    overviewModifier = Modifier.guideTarget(V3GuideFocus.SiteOverview, localTargets)
                )
                Text(
                    "地点操作包括专属事务、营建升级和派遣建议。派遣需到族人详情选择具体族人与差事。",
                    color = V3Ink,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.guideTarget(V3GuideFocus.SiteActions, localTargets)
                )
                V3SmallButton(
                    "关闭",
                    Modifier
                        .fillMaxWidth()
                        .guideTarget(V3GuideFocus.SiteClose, localTargets),
                    selected = true
                ) {
                    controller.advanceTutorial(8)
                    onDismiss()
                }
            }
            focus?.let { currentFocus ->
                val bounds = localTargets[currentFocus]
                V3LocalGuideOverlay(
                    state = state,
                    controller = controller,
                    targetBounds = bounds,
                    cardAtTop = currentFocus != V3GuideFocus.SiteOverview
                )
            }
        }
    }
}

@Composable
private fun V3SiteCard(
    site: V3CountySite,
    state: V3GameState,
    controller: V3GameController,
    overviewModifier: Modifier = Modifier
) {
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
        Text(
            "月产：${siteYieldSummary(yield)} · ${yield.desc}",
            color = if (site.level > 0) V3Green else V3Muted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = overviewModifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
        )
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
private fun V3EstatePanel(
    state: V3GameState,
    controller: V3GameController,
    modifier: Modifier = Modifier
) {
    V3Panel(modifier) {
        Text("家产管理", color = V3Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("家产是从小户到大族的底盘：田产养人，铺面生银，团练营支撑征伐。", color = V3Ink, fontSize = 13.sp, lineHeight = 19.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            V3SmallButton("家产总级", Modifier.weight(1f)) { controller.showInfo("家产总级：所有家产项目等级之和，不是银两，也不是产业数量。它反映家族经济底盘，并影响终局评价。当前 ${V3GameEngine.estateLevelTotal(state)}。") }
            V3SmallButton("控制地域", Modifier.weight(1f)) { controller.showInfo("控制地域：当前已控制的县外区域数量。控制地域会带来额外银粮，也会提高统一进度。当前 ${V3GameEngine.controlledRegionCount(state)}。") }
            V3SmallButton("统一进度", Modifier.weight(1f)) { controller.showInfo("统一进度：跨县经营和征伐的长期进度，不等于家产等级。需要逐步控制战略地域，满足条件后才能宣告统一。当前 ${state.unificationProgress}/100。") }
        }
        V3SmallButton("一键营建可负担家产", Modifier.fillMaxWidth(), selected = true, onClick = controller::autoManageEstates)
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
    framed: Boolean = true,
    guideTargets: MutableMap<V3GuideFocus, Rect>? = null,
    tutorialStep: Int = -1,
    onTaskAssigned: () -> Unit = {}
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
        Row(
            Modifier
                .fillMaxWidth()
                .then(
                    if (guideTargets != null) {
                        Modifier.guideTarget(V3GuideFocus.PersonOverview, guideTargets)
                    } else Modifier
                ),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
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
        Text("个人五维", color = V3Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        V3AttributeRadar(
            values = listOf(person.study, person.martial, person.commerce, person.diplomacy, person.loyalty),
            accent = V3Blue
        )
        Text(
            "培养",
            color = V3Red,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.then(
                if (guideTargets != null) {
                    Modifier.guideTarget(V3GuideFocus.PersonTraining, guideTargets)
                } else Modifier
            )
        )
        V3TrainingButtons(person, controller)
        if (person.age < 12) {
            Text("尚年幼，不能外出办事，但可以每月培养。儿童培养成长更快。", color = V3Ink, fontSize = 13.sp)
        } else {
            Text("建议：${recommendedTask(person).label} · ${taskDescription(recommendedTask(person))}", color = V3Ink, fontSize = 13.sp)
            V3TaskButtons(
                person,
                state,
                controller,
                guideTargets = guideTargets,
                tutorialStep = tutorialStep,
                onTaskAssigned = onTaskAssigned
            )
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
private fun V3TaskButtons(
    person: V3Person,
    state: V3GameState,
    controller: V3GameController,
    guideTargets: MutableMap<V3GuideFocus, Rect>? = null,
    tutorialStep: Int = -1,
    onTaskAssigned: () -> Unit = {}
) {
    val tasks = V3TaskType.entries
    val recommended = recommendedTask(person)
    tasks.chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row.forEach { task ->
                val site = targetSiteFor(state, task)
                val targetModifier = if (
                    guideTargets != null &&
                    tutorialStep == 14 &&
                    task == recommended &&
                    site != null
                ) {
                    Modifier.guideTarget(V3GuideFocus.PersonTask, guideTargets)
                } else Modifier
                V3SmallButton(
                    task.label,
                    targetModifier.weight(1f),
                    enabled = site != null && person.currentTask == null && person.trainingFocus == null
                ) {
                    if (site != null) {
                        controller.assignTask(person.id, site.id, task)
                        if (tutorialStep == 14) onTaskAssigned()
                    }
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
        ending.failureKind?.let { failure ->
            Text("失败分支：$failure", color = V3Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        V3SmallButton("展开族谱序", Modifier.fillMaxWidth(), selected = true) {
            controller.showInfo(controller.genealogyPreface())
        }
        V3SmallButton("查看终局履历", Modifier.fillMaxWidth()) {
            controller.showInfo(controller.endingChronicle().joinToString("\n"))
        }
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
    tutorialStep: Int
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
            V3TimeControls(controller, guideTargets, tutorialStep)
            val progression = V3ProgressionEngine.snapshot(state)
            Text(
                "主线：${progression.mainQuest.title} · ${progression.mainQuest.completedCount}/${progression.mainQuest.totalCount}项",
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
    tutorialStep: Int
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val speedPassStore = remember { SpeedPassStore(context) }
    var remainingPassMillis by remember { mutableStateOf(speedPassStore.remainingMillis()) }
    var adLoading by remember { mutableStateOf(false) }
    // 点击带锁遮罩的 2–5 倍按钮时先弹确认窗，确认后再拉广告
    var confirmSpeed by remember { mutableStateOf<Int?>(null) }

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

    // 全屏静态遮罩：只有 adLoading=true 时显示；由 onLoadingChanged(false) 可靠关闭
    AdLoadingOverlay(visible = adLoading, label = "倍速权益加载中…")

    // 观看前确认弹窗
    confirmSpeed?.let { speed ->
        Dialog(onDismissRequest = { confirmSpeed = null }) {
            V3ImagePanel(GameImages.V3UiEventPanel, Modifier.widthIn(max = 420.dp)) {
                Text("观看广告解锁倍速", color = V3Red, fontSize = 19.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text("观看后，2–5 倍时序全部解锁 20 分钟。", color = V3Ink, fontSize = 13.sp, lineHeight = 20.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    V3SmallButton("关闭", Modifier.weight(1f)) { confirmSpeed = null }
                    V3SmallButton("观看视频", Modifier.weight(1f), selected = true) {
                        val act = activity
                        confirmSpeed = null
                        if (act == null) {
                            controller.showInfo("当前页面无法打开激励广告。")
                            return@V3SmallButton
                        }
                        RewardedAdController.show(
                            activity = act,
                            onLoadingChanged = { adLoading = it },
                            onRewarded = {
                                val expiresAt = speedPassStore.unlockForTwentyMinutes()
                                remainingPassMillis = (expiresAt - System.currentTimeMillis()).coerceAtLeast(0L)
                                controller.updateTimeSpeed(speed)
                                controller.showInfo("激励广告奖励已生效：2–5 倍速度全部解锁 20 分钟，当前切换为 ${speed} 倍。")
                            },
                            onError = controller::showInfo,
                            onClosed = { /* AdLoadingOverlay 由 onLoadingChanged(false) 关闭 */ }
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .guideTarget(V3GuideFocus.TimeControls, guideTargets),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            V3SmallButton(if (controller.timeSpeed == 0) "继续" else "暂停", Modifier.weight(1f), selected = controller.timeSpeed == 0) {
                if (tutorialStep == 16 && controller.timeSpeed == 0) {
                    controller.advanceMonth()
                    controller.advanceTutorial(16)
                } else {
                    controller.togglePause()
                }
            }
            listOf(1, 2, 3, 4, 5).forEach { speed ->
                val unlocked = speed == 1 || remainingPassMillis > 0L
                V3SpeedButton(
                    speed = speed,
                    unlocked = unlocked,
                    selected = controller.timeSpeed == speed,
                    modifier = Modifier.weight(1f),
                    enabled = !adLoading
                ) {
                    when {
                        unlocked -> controller.updateTimeSpeed(speed)
                        adLoading -> Unit
                        else -> confirmSpeed = speed
                    }
                }
            }
            Text(
                if (controller.shouldAutoTick()) "时序推进中" else "时序暂停",
                color = if (controller.shouldAutoTick()) V3Green else V3Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1.4f)
            )
        }
        if (remainingPassMillis > 0L) {
            val totalSeconds = (remainingPassMillis / 1000L).coerceAtLeast(0L)
            val minutes = totalSeconds / 60L
            val seconds = totalSeconds % 60L
            Text(
                "倍速剩余 ${minutes}:${seconds.toString().padStart(2, '0')}",
                color = V3Green,
                fontSize = 10.sp,
                lineHeight = 15.sp
            )
        }
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
    Column(
        modifier
            .background(V3PaperDeep, V3SoftShape)
            .border(1.dp, V3Border.copy(alpha = 0.82f), V3SoftShape)
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
private fun V3GoalRow(state: V3GameState, goal: V3AnnualGoal, controller: V3GameController) {
    val quest = V3ProgressionEngine.snapshot(state).sideQuests.firstOrNull { it.id == goal.id }
    val progress = V3GameEngine.goalProgress(state, goal)
    val reached = progress >= goal.target || goal.completed
    Column(
        Modifier
            .fillMaxWidth()
            .background(if (reached) V3Green.copy(alpha = 0.16f) else V3PaperDeep, V3SoftShape)
            .border(1.dp, if (reached) V3Green.copy(alpha = 0.55f) else V3Border.copy(alpha = 0.55f), V3SoftShape)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("年务 · ${goal.title}", color = V3Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(if (reached) "已成" else "${progress}/${goal.target}", color = if (reached) V3Green else V3Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text(goal.desc, color = V3Muted, fontSize = 12.sp, lineHeight = 17.sp)
        quest?.let { card ->
            Text("奖励：${card.rewardText}", color = V3Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            if (!reached) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(card.blockers.firstOrNull().orEmpty(), color = V3Muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    V3SmallButton(card.actionLabel, Modifier.widthIn(min = 86.dp)) { controller.switchScreen(card.destination) }
                }
            }
        }
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
private fun V3SpeedButton(
    speed: Int,
    unlocked: Boolean,
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AssetImage(
            when {
                selected -> GameImages.MingyunSmallButtonSelected
                else -> GameImages.MingyunSmallButton
            },
            null,
            Modifier.matchParentSize(),
            ContentScale.FillBounds
        )
        Text("${speed}倍", color = V3Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        if (!unlocked) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color(0x552B2925), V3ButtonShape),
                contentAlignment = Alignment.Center
            ) {
                AssetImage(
                    GameImages.V3IconSpeedLock,
                    "未解锁",
                    Modifier.size(25.dp),
                    ContentScale.Fit
                )
            }
        }
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
    val eventTutorial = controller.state.tutorialStep == 18
    val localTargets = remember { mutableStateMapOf<V3GuideFocus, Rect>() }
    Dialog(onDismissRequest = {}) {
        Box {
            V3ImagePanel(GameImages.V3UiEventPanel, Modifier.widthIn(max = 500.dp)) {
                Text(event.title, color = V3Red, fontSize = 21.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Box(Modifier.fillMaxWidth().heightIn(max = 430.dp).verticalScroll(rememberScrollState())) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(event.body, color = V3Ink, fontSize = 15.sp, lineHeight = 23.sp)
                        event.choices.forEachIndexed { index, choice ->
                            val choiceContext = V3ProgressionEngine.eventChoiceContext(controller.state, choice)
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = V3PaperDeep), border = BorderStroke(2.dp, V3Red), shape = V3SoftShape) {
                                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(choice.label, color = V3Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text(choice.route.label, color = V3Red, fontSize = 12.sp)
                                    }
                                    Text(choice.desc, color = V3Muted, fontSize = 12.sp)
                                    Text(choiceImpactSummary(choice), color = V3Ink, fontSize = 12.sp)
                                    Text(
                                        choiceContext,
                                        color = if (choiceContext.contains("风险：")) V3Red else V3Gold,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    V3SmallButton(
                                        "选择此方案",
                                        Modifier
                                            .fillMaxWidth()
                                            .then(
                                                if (eventTutorial && index == 0) {
                                                    Modifier.guideTarget(V3GuideFocus.EventChoice, localTargets)
                                                } else Modifier
                                            )
                                    ) {
                                        controller.chooseEvent(choice)
                                        if (eventTutorial) controller.advanceTutorial(18)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (eventTutorial) {
                V3LocalGuideOverlay(
                    state = controller.state,
                    controller = controller,
                    targetBounds = localTargets[V3GuideFocus.EventChoice],
                    cardAtTop = true
                )
            }
        }
    }
}

@Composable
private fun V3Dialog(
    title: String,
    onDismiss: () -> Unit,
    tutorialState: V3GameState? = null,
    tutorialController: V3GameController? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val localTargets = remember { mutableStateMapOf<V3GuideFocus, Rect>() }
    val isReportTutorial = tutorialState?.tutorialStep == 17
    Dialog(onDismissRequest = onDismiss) {
        Box {
            V3ImagePanel(GameImages.V3UiEventPanel, Modifier.widthIn(max = 460.dp)) {
                Text(title, color = V3Red, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Box(Modifier.fillMaxWidth().heightIn(max = 390.dp).verticalScroll(rememberScrollState())) {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp), content = content)
                }
                V3Button(
                    "知道了",
                    Modifier
                        .fillMaxWidth()
                        .then(
                            if (isReportTutorial) {
                                Modifier.guideTarget(V3GuideFocus.MonthlyReportDismiss, localTargets)
                            } else Modifier
                        ),
                    onClick = onDismiss
                )
            }
            if (isReportTutorial && tutorialState != null && tutorialController != null) {
                V3LocalGuideOverlay(
                    state = tutorialState,
                    controller = tutorialController,
                    targetBounds = localTargets[V3GuideFocus.MonthlyReportDismiss],
                    cardAtTop = true
                )
            }
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

private data class V3AdRewardOffer(
    val key: String,
    val title: String,
    val subtitle: String,
    val grantedMessage: String,
    val silver: Int = 0,
    val grain: Int = 0,
    val cohesion: Int = 0,
    val repairDurability: Int = 0
)

private fun monthlyAdOffer(state: V3GameState): V3AdRewardOffer {
    val keyPrefix = "month-${state.year}-${state.month}"
    return when {
        state.silver < 80 -> V3AdRewardOffer(
            key = "$keyPrefix-silver",
            title = "接受行商周转",
            subtitle = "可选观看一段激励视频，商队送来 70 两应急银",
            grantedMessage = "行商周转已到账：银两 +70。",
            silver = 70
        )
        state.grain < 120 -> V3AdRewardOffer(
            key = "$keyPrefix-grain",
            title = "开仓筹措赈粮",
            subtitle = "可选观看一段激励视频，乡绅协助筹得 100 石粮",
            grantedMessage = "赈粮已入仓：粮食 +100。",
            grain = 100
        )
        state.equipment.any { it.durability < it.maxDurability } -> V3AdRewardOffer(
            key = "$keyPrefix-repair",
            title = "请军匠维护军械",
            subtitle = "可选观看一段激励视频，全部军械恢复 12 点耐久",
            grantedMessage = "军匠维护完成：全部军械耐久 +12。",
            repairDurability = 12
        )
        state.cohesion < 55 -> V3AdRewardOffer(
            key = "$keyPrefix-cohesion",
            title = "设宴安抚族人",
            subtitle = "可选观看一段激励视频，宗族凝聚 +6",
            grantedMessage = "族宴结束：宗族凝聚 +6。",
            cohesion = 6
        )
        else -> V3AdRewardOffer(
            key = "$keyPrefix-trade",
            title = "追加本月商路分红",
            subtitle = "可选观看一段激励视频，获得 45 两商路分红",
            grantedMessage = "商路分红已到账：银两 +45。",
            silver = 45
        )
    }
}

@Composable
private fun V3MonthlyReportDialog(report: V3MonthlyReport, controller: V3GameController) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val claimStore = remember { RewardClaimStore(context) }
    val offer = monthlyAdOffer(controller.state)
    var loading by remember { mutableStateOf(false) }
    var confirmOffer by remember { mutableStateOf(false) }
    var claimed by remember(offer.key) { mutableStateOf(claimStore.hasClaimed(offer.key)) }

    AdLoadingOverlay(visible = loading, label = "机缘加载中…")

    if (confirmOffer && !claimed) {
        Dialog(onDismissRequest = { confirmOffer = false }) {
            V3ImagePanel(GameImages.V3UiEventPanel, Modifier.widthIn(max = 420.dp)) {
                Text(offer.title, color = V3Red, fontSize = 19.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text(offer.subtitle, color = V3Ink, fontSize = 13.sp, lineHeight = 20.sp)
                Text("完整观看并通过奖励校验后奖励即刻发放；不观看也可直接关闭月报继续游戏。", color = V3Muted, fontSize = 11.sp, lineHeight = 17.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    V3SmallButton("关闭", Modifier.weight(1f)) { confirmOffer = false }
                    V3SmallButton("观看视频", Modifier.weight(1f), selected = true) {
                        val act = activity
                        confirmOffer = false
                        if (act == null) {
                            controller.showInfo("当前页面无法打开激励视频。")
                            return@V3SmallButton
                        }
                        RewardedAdController.show(
                            activity = act,
                            onLoadingChanged = { loading = it },
                            onRewarded = {
                                if (!claimStore.hasClaimed(offer.key)) {
                                    claimStore.markClaimed(offer.key)
                                    claimed = true
                                    controller.grantMonthlyReward(
                                        description = offer.grantedMessage,
                                        silver = offer.silver,
                                        grain = offer.grain,
                                        cohesion = offer.cohesion,
                                        repairDurability = offer.repairDurability
                                    )
                                }
                            },
                            onError = controller::showInfo,
                            onClosed = {}
                        )
                    }
                }
            }
        }
    }

    V3Dialog(
        title = monthlyReportTitle(report.title, report.lines),
        onDismiss = controller::clearReport,
        tutorialState = controller.state,
        tutorialController = controller
    ) {
        if (report.conclusion.isNotBlank()) {
            Text("本月结论", color = V3Red, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(report.conclusion, color = V3Ink, fontSize = 14.sp, lineHeight = 21.sp)
        }
        if (report.resourceLines.isNotEmpty()) {
            Text("资源变化", color = V3Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            report.resourceLines.forEach { Text("· $it", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp) }
        }
        if (report.assignmentLines.isNotEmpty()) {
            Text("计划执行", color = V3Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            report.assignmentLines.forEach { Text("· $it", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp) }
        }
        if (report.goalLines.isNotEmpty()) {
            Text("目标推进", color = V3Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            report.goalLines.forEach { Text("· $it", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp) }
        }
        if (report.alertLines.isNotEmpty()) {
            Text("风险警告", color = V3Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            report.alertLines.forEach { Text("· $it", color = V3Red, fontSize = 12.sp, lineHeight = 18.sp) }
        }
        if (report.nextActionTitle.isNotBlank()) {
            Column(
                Modifier.fillMaxWidth().background(V3PaperDeep, V3SoftShape).padding(9.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("下月首要行动 · ${report.nextActionTitle}", color = V3Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(report.nextActionReason, color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
                V3SmallButton(report.nextActionLabel.ifBlank { "前往处理" }, Modifier.fillMaxWidth(), selected = true) {
                    controller.clearReportAndNavigate(report.nextActionDestination)
                }
            }
        }
        if (report.lines.isNotEmpty()) {
            Text("完整纪要", color = V3Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            report.lines.forEach { line ->
                Text("· $line", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
        if (!claimed) {
            Text("本月可选机缘", color = V3Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(offer.subtitle, color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
            V3SmallButton(offer.title, Modifier.fillMaxWidth(), enabled = !loading) {
                if (!loading) confirmOffer = true
            }
            Text("这是额外奖励，不观看也可直接关闭月报继续游戏。", color = V3Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun V3BattleDialog(state: V3GameState, battle: V3BattleState, controller: V3GameController) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val claimStore = remember { RewardClaimStore(context) }
    val battleRewardKey = "battle-${state.year}-${state.month}-${battle.target}-${battle.enemyPower}-${battle.turn}"
    var adLoading by remember { mutableStateOf(false) }
    var confirmBattleReward by remember { mutableStateOf(false) }
    var battleRewardClaimed by remember(battleRewardKey) { mutableStateOf(claimStore.hasClaimed(battleRewardKey)) }

    AdLoadingOverlay(visible = adLoading, label = "军匠联络中…")

    if (confirmBattleReward && battle.finished && !battleRewardClaimed) {
        Dialog(onDismissRequest = { confirmBattleReward = false }) {
            V3ImagePanel(GameImages.V3UiEventPanel, Modifier.widthIn(max = 420.dp)) {
                Text("请军匠战地维护", color = V3Red, fontSize = 19.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text("是否观看一段激励视频？完整观看并通过奖励校验后，全部军械恢复 18 点耐久。", color = V3Ink, fontSize = 13.sp, lineHeight = 20.sp)
                Text("不观看也可直接收兵结算，不影响战斗结果。", color = V3Muted, fontSize = 11.sp, lineHeight = 17.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    V3SmallButton("关闭", Modifier.weight(1f)) { confirmBattleReward = false }
                    V3SmallButton("观看视频", Modifier.weight(1f), selected = true) {
                        val act = activity
                        confirmBattleReward = false
                        if (act == null) {
                            controller.showInfo("当前页面无法打开激励视频。")
                            return@V3SmallButton
                        }
                        RewardedAdController.show(
                            activity = act,
                            onLoadingChanged = { adLoading = it },
                            onRewarded = {
                                if (!claimStore.hasClaimed(battleRewardKey)) {
                                    claimStore.markClaimed(battleRewardKey)
                                    battleRewardClaimed = true
                                    controller.grantMonthlyReward(
                                        description = "战地军械维护完成：全部军械耐久 +18。",
                                        repairDurability = 18
                                    )
                                }
                            },
                            onError = controller::showInfo,
                            onClosed = {}
                        )
                    }
                }
            }
        }
    }

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
                if (battle.finished && !battleRewardClaimed) {
                    Text("战后可选军匠援助：观看一段激励视频，全部军械恢复 18 点耐久。不观看也可直接收兵。", color = V3Muted, fontSize = 11.sp, lineHeight = 16.sp)
                    V3SmallButton("请军匠战地维护", Modifier.fillMaxWidth(), enabled = !adLoading) {
                        if (!adLoading) confirmBattleReward = true
                    }
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

private fun recommendedAvailableTask(state: V3GameState, person: V3Person): Pair<V3TaskType, V3CountySite>? =
    listOf(
        V3TaskType.Study to person.study,
        V3TaskType.Recruit to person.martial,
        V3TaskType.Trade to person.commerce,
        V3TaskType.Diplomacy to person.diplomacy,
        V3TaskType.Govern to ((person.study + person.diplomacy) / 2),
        V3TaskType.Farm to ((person.study + person.commerce) / 2),
        V3TaskType.Fortify to person.martial,
        V3TaskType.Scout to ((person.martial + person.diplomacy) / 2),
        V3TaskType.Relief to ((person.study + person.diplomacy) / 2)
    ).mapNotNull { (task, score) ->
        targetSiteFor(state, task)?.let { site -> Triple(task, site, score) }
    }.maxByOrNull { it.third }
        ?.let { it.first to it.second }

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

@Composable
private fun V3ArchivePanel(state: V3GameState, controller: V3GameController) {
    var expanded by remember { mutableStateOf(false) }
    V3Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("族谱与家传", color = V3Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            V3SmallButton(if (expanded) "收起" else "展开", selected = false) { expanded = !expanded }
        }
        Text("履历 ${state.biography.size} 条 · 物品 ${state.inventory.size} 件 · 匾额 ${state.plaques.size} 方", color = V3Muted, fontSize = 12.sp)
        if (expanded) {
            if (state.plaques.isNotEmpty()) {
                Text("封翁匾", color = V3Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                state.plaques.forEach { plaque ->
                    Text("【$plaque】${V3Content.plaques[plaque].orEmpty()}", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
            if (state.inventory.isNotEmpty()) {
                Text("家传物品", color = V3Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                state.inventory.mapNotNull { id -> V3Content.items.firstOrNull { it.id == id } }.forEach { item ->
                    Text("${item.name}：${item.desc}", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
            if (state.biography.isNotEmpty()) {
                Text("家乘履历", color = V3Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                state.biography.takeLast(8).reversed().forEach { note ->
                    Text("· $note", color = V3Ink, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
            if (state.pendingSuccession) {
                Text("族长继任", color = V3Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("${state.patriarch.name}已无法继续执掌族印，请从候选人中推举下一代族长。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
                V3GameEngine.patriarchCandidates(state).take(4).forEach { person ->
                    V3SmallButton("推举${person.name} · 功${person.merit}", Modifier.fillMaxWidth(), selected = true) {
                        controller.succeedPatriarch(person.id)
                    }
                }
            }
        }
    }
}

@Composable
private fun V3AttributeRadar(values: List<Int>, accent: Color) {
    Canvas(Modifier.fillMaxWidth().height(150.dp).padding(horizontal = 34.dp, vertical = 6.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) * 0.34f
        val count = values.size.coerceAtLeast(3)
        val angles = values.indices.map { index -> -Math.PI / 2.0 + index * Math.PI * 2.0 / count }
        val points = values.mapIndexed { index, value ->
            Offset(
                center.x + kotlin.math.cos(angles[index]).toFloat() * radius * value.coerceIn(0, 100) / 100f,
                center.y + kotlin.math.sin(angles[index]).toFloat() * radius * value.coerceIn(0, 100) / 100f
            )
        }
        val outline = Path().apply {
            points.forEachIndexed { index, point -> if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y) }
            close()
        }
        drawPath(outline, accent.copy(alpha = 0.28f))
        drawPath(outline, accent.copy(alpha = 0.9f), style = Stroke(width = 2f))
        angles.forEach { angle ->
            drawLine(
                V3Muted.copy(alpha = 0.45f),
                center,
                Offset(center.x + kotlin.math.cos(angle).toFloat() * radius, center.y + kotlin.math.sin(angle).toFloat() * radius),
                strokeWidth = 1f
            )
        }
    }
}

@Composable
private fun V3PatriarchRadar(state: V3GameState) {
    val values = listOf(
        state.patriarch.conduct,
        state.patriarch.stewardship,
        state.patriarch.prestige,
        state.patriarch.health
    )
    Canvas(Modifier.fillMaxWidth().height(150.dp).padding(8.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) * 0.34f
        val points = values.mapIndexed { index, value ->
            val angle = (-Math.PI / 2.0 + index * Math.PI / 2.0)
            Offset(
                center.x + kotlin.math.cos(angle).toFloat() * radius * value / 100f,
                center.y + kotlin.math.sin(angle).toFloat() * radius * value / 100f
            )
        }
        val outline = Path().apply {
            points.forEachIndexed { index, point -> if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y) }
            close()
        }
        drawPath(outline, V3Gold.copy(alpha = 0.28f), style = Stroke(width = 2f))
        drawPath(outline, V3Gold.copy(alpha = 0.22f))
        values.indices.forEach { index ->
            val angle = (-Math.PI / 2.0 + index * Math.PI / 2.0)
            val edge = Offset(
                center.x + kotlin.math.cos(angle).toFloat() * radius,
                center.y + kotlin.math.sin(angle).toFloat() * radius
            )
            drawLine(V3Muted.copy(alpha = 0.45f), center, edge, strokeWidth = 1f)
        }
    }
}

@Composable
private fun V3PatriarchPanel(state: V3GameState, controller: V3GameController) {
    V3Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("族长 · ${state.patriarch.name}", color = V3Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("第${state.patriarch.generation}代 · 任期${state.patriarch.term}月", color = V3Muted, fontSize = 12.sp)
        }
        Text("处世 ${state.patriarch.conduct} · 经营 ${state.patriarch.stewardship} · 威望 ${state.patriarch.prestige} · 身板 ${state.patriarch.health}", color = V3Ink, fontSize = 13.sp)
        V3PatriarchRadar(state)
        if (state.originTraits.isNotEmpty()) {
            Text("出身特性", color = V3Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            state.originTraits.forEach { trait -> Text("· $trait", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp) }
        }
        Text("流民 ${state.refugees} · 庄内怨气 ${state.unrestLevel} · 守望士气 ${state.garrisonMorale}", color = if (state.unrestLevel >= 35) V3Red else V3Muted, fontSize = 12.sp)
        if (state.patriarch.capstones.isNotEmpty()) Text("族望匾：${state.patriarch.capstones.joinToString("、")}", color = V3Gold, fontSize = 12.sp)
        if (state.biography.isNotEmpty()) Text("族谱履历：${state.biography.last()}", color = V3Muted, fontSize = 12.sp, lineHeight = 17.sp)
        if (state.year >= 1643 && state.hexBattleState == null && !state.hexBattleCompleted) {
            V3SmallButton("整备六门守庄", Modifier.fillMaxWidth(), selected = true) { controller.startHexBattle() }
        }
    }
}

@Composable
private fun V3VisitorDialog(
    card: V3MonthlyCard,
    state: V3GameState,
    controller: V3GameController
) {
    Dialog(onDismissRequest = {}) {
        V3ImagePanel(GameImages.V3UiEventPanel, Modifier.widthIn(max = 500.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("远客入庄", color = V3Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(card.title, color = V3Gold, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                }
                Text("${state.year}年${state.month}月", color = V3Muted, fontSize = 12.sp)
            }
            Text("这不是普通家务，而是一段会继续发展的访客故事。选择之后，访客进度、家乘、物品、关系或路线会真实写回；后续章节会在条件满足的月份再次来访。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
            Text(card.body, color = V3Ink, fontSize = 15.sp, lineHeight = 23.sp)
            card.choices.forEach { choice ->
                val unlocked = V3CardEngine.meets(choice.require, state)
                V3SmallButton(
                    if (unlocked) choice.label else "${choice.label}（条件不足）",
                    Modifier.fillMaxWidth(),
                    enabled = unlocked,
                    selected = unlocked
                ) {
                    if (unlocked) controller.chooseCard(card.id, choice.id)
                    else controller.showInfo(choice.require?.label() ?: "此项暂不可行")
                }
                Text(choice.desc, color = V3Muted, fontSize = 11.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun V3MonthlyCardsPanel(state: V3GameState, controller: V3GameController) {
    val regularCards = state.activeCards.filter { it.pool != V3CardPool.Visitor }
    if (regularCards.isEmpty() && state.pendingDice == null) {
        V3Panel {
            Text("本月家务", color = V3Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("案上暂时无急务。下月结算后，新的访客、族务与危局会依条件出现。访客到来时会单独弹出剧情对话。", color = V3Muted, fontSize = 12.sp, lineHeight = 18.sp)
        }
        return
    }
    V3Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("本月家务", color = V3Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("已议 ${state.playedCardsThisMonth}/${state.cardBudget}", color = V3Muted, fontSize = 12.sp)
        }
        Text("银粮之外，真正改变家族走向的，是每月摆在案上的几件事。访客剧情会以独立弹窗呈现。", color = V3Muted, fontSize = 12.sp)
        regularCards.forEach { card ->
            V3CardPanel(card, state, controller)
        }
        state.pendingDice?.let { dice ->
            val shake = remember(dice.cardId, dice.choiceId) { Animatable(0f) }
            LaunchedEffect(dice.cardId, dice.choiceId) {
                shake.animateTo(1f, animationSpec = keyframes { durationMillis = 600 })
            }
            V3Panel(Modifier.fillMaxWidth().graphicsLayer { rotationZ = shake.value * 5f }.background(V3PaperDeep, V3SoftShape)) {
                Text("签筒已摇", color = V3Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("成功率 ${dice.successRate}% · 签数 ${dice.roll}", color = V3Muted, fontSize = 13.sp)
                V3SmallButton("揭签看吉凶", Modifier.fillMaxWidth(), selected = true) { controller.resolveCardDice() }
            }
        }
    }
}

@Composable
private fun V3CardPanel(card: V3MonthlyCard, state: V3GameState, controller: V3GameController) {
    val lift = remember { Animatable(0f) }
    LaunchedEffect(card.id) {
        lift.snapTo(18f)
        lift.animateTo(0f, animationSpec = keyframes { durationMillis = 280 })
    }
    Column(Modifier.fillMaxWidth().graphicsLayer { translationY = lift.value }.background(V3PaperDeep, V3SoftShape).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(card.title, color = V3Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(card.tag.ifBlank { "家务" }, color = V3Gold, fontSize = 12.sp)
        }
        Text(card.body, color = V3Muted, fontSize = 13.sp, lineHeight = 19.sp)
        card.choices.forEach { choice ->
            val unlocked = com.arktools.daming.v3.logic.V3CardEngine.meets(choice.require, state)
            if (!choice.hiddenIfLocked || unlocked) {
                V3SmallButton(
                    if (unlocked) choice.label else "${choice.label}（条件不足）",
                    Modifier.fillMaxWidth(),
                    selected = unlocked
                ) {
                    if (unlocked) controller.chooseCard(card.id, choice.id) else controller.showInfo(choice.require?.label() ?: "此项暂不可行")
                }
            }
        }
    }
}

@Composable
private fun V3HexBattleDialog(battle: V3HexBattleState, controller: V3GameController) {
    V3Dialog(title = "守庄战 · 第${battle.turn}轮", onDismiss = {}) {
        Text("补给 ${battle.supply} · 敌方动量 ${battle.enemyMomentum}", color = if (battle.supply <= 20 || battle.enemyMomentum >= 75) V3Red else V3Muted, fontSize = 13.sp)
        Text("枪阵克骑突，骑突克弓矢，弓矢克枪阵。补给耗尽或敌方动量到100，守庄战即告败。", color = V3Muted, fontSize = 13.sp, lineHeight = 19.sp)
        battle.tiles.forEach { tile ->
            val key = "${tile.q},${tile.r}"
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("${tile.name} · 驻守${tile.garrison}", color = if (tile.breached) V3Red else V3Ink, fontSize = 13.sp)
                    Text(if (tile.stable) "庄门尚稳" else "庄门告急", color = V3Muted, fontSize = 11.sp)
                }
                V3HexArms.entries.forEach { arms ->
                    V3SmallButton(
                        arms.label,
                        Modifier.padding(start = 3.dp),
                        selected = (battle.selectedArms[key] ?: tile.arms) == arms
                    ) {
                        controller.setHexArms(key, arms)
                    }
                }
            }
        }
        if (battle.finished) {
            Text(if (battle.victory) "六处庄门俱在，守住了这一夜。" else "庄门已破，族人退入祖祠。", color = if (battle.victory) V3Green else V3Red, fontWeight = FontWeight.Bold)
            V3SmallButton("收下战果", Modifier.fillMaxWidth(), selected = true) { controller.closeHexBattle() }
        } else {
            V3SmallButton("结算本轮", Modifier.fillMaxWidth(), selected = true) { controller.advanceHexTurn() }
        }
        battle.log.takeLast(3).forEach { Text(it, color = V3Muted, fontSize = 11.sp) }
    }
}
