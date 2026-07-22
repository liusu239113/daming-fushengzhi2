package com.arktools.daming.v3.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arktools.daming.audio.GameAudio
import com.arktools.daming.data.BgmKey
import com.arktools.daming.data.SfxKey
import com.arktools.daming.persistence.V3SaveStore
import com.arktools.daming.v3.data.V3Content
import com.arktools.daming.v3.data.V3GameState
import com.arktools.daming.v3.data.V3MonthlyReport
import com.arktools.daming.v3.data.V3Screen
import com.arktools.daming.v3.data.V3TaskType
import com.arktools.daming.v3.data.V3TrainingType
import com.arktools.daming.v3.data.V3EventChoice
import com.arktools.daming.v3.data.V3EstateType
import com.arktools.daming.v3.data.V3TroopType

class V3GameController(private val saveStore: V3SaveStore, private val audio: GameAudio) {
    var state by mutableStateOf(V3GameEngine.normalizeState(saveStore.load() ?: V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税")))
        private set

    var screen by mutableStateOf(V3Screen.County)
        private set

    var timeSpeed by mutableStateOf(0)
        private set

    private var lastActiveSpeed = 1
    private var resumeSpeedAfterModal: Int? = null

    var latestReport by mutableStateOf<V3MonthlyReport?>(null)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    var settingsVisible by mutableStateOf(false)
        private set

    var bgmVolume by mutableStateOf(audio.currentBgmVolume)
        private set

    var sfxVolume by mutableStateOf(audio.currentSfxVolume)
        private set

    fun ensureV3Bgm() {
        audio.playBgm(BgmKey.V3County)
    }

    fun newGame(root: String, county: String, creed: String, crisis: String, surname: String = "李", givenName: String = "慎行") {
        audio.playSfx(SfxKey.V3ScrollOpen)
        ensureV3Bgm()
        val safeGivenName = if (V3Content.isBlockedName(surname + givenName)) "慎行" else V3Content.sanitizeFounderGivenName(givenName)
        state = V3Content.newGame(root, county, creed, crisis, surname, safeGivenName)
        saveStore.save(state)
        screen = V3Screen.County
        timeSpeed = 0
        lastActiveSpeed = 1
        resumeSpeedAfterModal = null
        latestReport = null
        settingsVisible = false
        message = null
    }

    fun hasSave(): Boolean = saveStore.hasSave()

    fun continueGame() {
        audio.click()
        ensureV3Bgm()
        state = V3GameEngine.normalizeState(saveStore.load() ?: state)
        screen = V3Screen.County
        timeSpeed = 0
        lastActiveSpeed = 1
        resumeSpeedAfterModal = null
        latestReport = null
        settingsVisible = false
        message = "案卷已启封，旧日县域局势重归案前。"
    }

    companion object {
        // 完整教程包含界面导览、地点/族人弹窗教学和首月经营闭环。
        const val TUTORIAL_STEP_COUNT = 32
    }

    fun switchScreen(next: V3Screen) {
        if (screen != next) audio.tabSwitch()
        screen = next
    }

    fun updateTimeSpeed(speed: Int) {
        audio.select()
        val nextSpeed = speed.coerceIn(0, 5)
        timeSpeed = nextSpeed
        if (nextSpeed > 0) lastActiveSpeed = nextSpeed
    }

    fun togglePause() {
        audio.click()
        timeSpeed = if (timeSpeed == 0) lastActiveSpeed else 0
    }

    fun pauseForPlayerAction() {
        timeSpeed = 0
    }

    fun shouldAutoTick(): Boolean =
        timeSpeed > 0 &&
            latestReport == null &&
            message == null &&
            !settingsVisible &&
            state.finalEnding == null &&
            state.activeEvent == null &&
            state.examSession == null &&
            state.battleState == null &&
            state.hexBattleState == null &&
            state.activeCards.isEmpty() &&
            state.pendingDice == null &&
            state.conquestState == null

    fun autoAdvanceTime() {
        if (shouldAutoTick()) advanceMonth(showReport = false)
    }

    fun openSettings() {
        audio.click()
        pauseForModal()
        settingsVisible = true
    }

    fun closeSettings() {
        audio.click()
        settingsVisible = false
        resumeAfterModalIfClear()
    }

    fun pageTurn() {
        audio.playSfx(SfxKey.V3PageTurn)
    }

    fun updateBgmVolume(value: Float) {
        audio.setBgmVolume(value)
        bgmVolume = audio.currentBgmVolume
    }

    fun updateSfxVolume(value: Float) {
        audio.setSfxVolume(value)
        sfxVolume = audio.currentSfxVolume
    }

    fun marry(candidateId: String, targetPersonId: Int? = null) {
        audio.playSfx(SfxKey.V3Edict)
        state = V3GameEngine.marry(state, candidateId, targetPersonId)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun rankUp() {
        audio.playSfx(SfxKey.V3Build)
        state = V3GameEngine.rankUp(state)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun claimChapterReward(chapter: V3Chapter) {
        audio.playSfx(SfxKey.V3Success)
        state = V3ProgressionEngine.claimChapterReward(state, chapter)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun holdCouncil(agenda: String) {
        audio.playSfx(SfxKey.V3Edict)
        state = V3GameEngine.holdCouncil(state, agenda)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun autoArrangeMonth() {
        audio.playSfx(SfxKey.V3Edict)
        state = V3GameEngine.autoArrangeMonth(state)
        if (state.people.any { it.alive && (it.currentTask != null || it.trainingFocus != null) }) {
            completeTutorialAction(15)
        }
        message = if (state.tutorialCompleted) state.pendingReports.firstOrNull() else null
        saveStore.save(state)
    }

    fun assignTask(personId: Int, siteId: String, task: V3TaskType) {
        audio.select()
        state = V3GameEngine.assignTask(state, personId, siteId, task)
        val assigned = state.people.firstOrNull { it.id == personId }
        if (assigned?.assignedSiteId == siteId && assigned.currentTask == task) {
            completeTutorialAction(14)
        }
        message = if (state.tutorialCompleted) state.pendingReports.firstOrNull() else null
        saveStore.save(state)
    }

    fun upgradeSite(siteId: String) {
        audio.playSfx(SfxKey.V3Build)
        state = V3GameEngine.upgradeSite(state, siteId)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun autoManageEstates() {
        audio.playSfx(SfxKey.V3Build)
        state = V3GameEngine.autoManageEstates(state)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun upgradeEstate(type: V3EstateType) {
        audio.playSfx(SfxKey.V3Build)
        state = V3GameEngine.upgradeEstate(state, type)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun siteSpecialAction(siteId: String) {
        audio.playSfx(SfxKey.V3SpecialAction)
        state = V3GameEngine.siteSpecialAction(state, siteId)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun contactRegion(regionId: String) {
        audio.select()
        state = V3GameEngine.contactRegion(state, regionId)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun influenceRegion(regionId: String) {
        audio.select()
        state = V3GameEngine.influenceRegion(state, regionId)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun startConquest(regionId: String) {
        audio.playSfx(SfxKey.V3Dispute)
        state = V3GameEngine.startConquest(state, regionId)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun resolveConquest() {
        state = V3GameEngine.resolveConquest(state)
        val result = state.pendingReports.firstOrNull().orEmpty()
        audio.playSfx(if (result.contains("得胜")) SfxKey.V3Success else SfxKey.V3Failure)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun cancelConquest() {
        audio.click()
        state = V3GameEngine.cancelConquest(state)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun proclaimUnification() {
        audio.playSfx(SfxKey.V3Finale)
        state = V3GameEngine.proclaimUnification(state)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun trainPerson(personId: Int, training: V3TrainingType) {
        audio.select()
        state = V3GameEngine.trainPerson(state, personId, training)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun startExam(personId: Int) {
        audio.playSfx(SfxKey.UiSelect)
        state = V3GameEngine.startExam(state, personId)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun answerExam(answerIndex: Int) {
        state = V3GameEngine.answerExam(state, answerIndex)
        val result = state.pendingReports.firstOrNull().orEmpty()
        audio.playSfx(if (result.contains("通过")) SfxKey.V3Success else SfxKey.V3Failure)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun recruitTroops(type: V3TroopType, amount: Int = 5) {
        audio.playSfx(SfxKey.V3Build)
        state = V3GameEngine.recruitTroops(state, type, amount)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun buyEquipment(slot: com.arktools.daming.v3.data.V3EquipmentSlot, quality: com.arktools.daming.v3.data.V3EquipmentQuality) {
        audio.playSfx(SfxKey.V3Build)
        state = V3GameEngine.buyEquipment(state, slot, quality)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun repairEquipment(equipmentId: String) {
        audio.playSfx(SfxKey.V3Build)
        state = V3GameEngine.repairEquipment(state, equipmentId)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }
    fun equipEquipment(equipmentId: String, personId: Int) {
        audio.select()
        state = V3GameEngine.equipEquipment(state, equipmentId, personId)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }
    fun startBattle() {
        audio.playSfx(SfxKey.V3Dispute)
        state = V3GameEngine.startBattle(state)
        message = if (state.battleState == null) state.pendingReports.firstOrNull() else null
        saveStore.save(state)
    }

    fun selectBattlePerson(personId: Int) {
        audio.select()
        state = V3GameEngine.selectBattlePerson(state, personId)
        message = if (state.battleState == null) state.pendingReports.firstOrNull() else null
        saveStore.save(state)
    }

    fun selectBattleTroop(personId: Int, troopType: V3TroopType) {
        audio.select()
        state = V3GameEngine.selectBattleTroop(state, personId, troopType)
        message = if (state.battleState == null) state.pendingReports.firstOrNull() else null
        saveStore.save(state)
    }
    fun confirmBattleLineup() {
        audio.playSfx(SfxKey.V3Edict)
        state = V3GameEngine.confirmBattleLineup(state)
        message = if (state.battleState?.phase == com.arktools.daming.v3.data.V3BattlePhase.Fighting) null else state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun advanceBattleRound() {
        state = V3GameEngine.advanceBattleRound(state)
        val result = state.battleState?.roundLog?.firstOrNull()?.text.orEmpty()
        audio.playSfx(if (result.contains("反扑")) SfxKey.V3Warning else SfxKey.V3Dispute)
        message = if (state.battleState == null) state.pendingReports.firstOrNull() else null
        saveStore.save(state)
    }

    fun finalizeBattle() {
        state = V3GameEngine.finalizeBattle(state)
        val result = state.pendingReports.firstOrNull().orEmpty()
        audio.playSfx(if (result.contains("得胜")) SfxKey.V3Success else SfxKey.V3Failure)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun resolveBattle() {
        state = V3GameEngine.resolveBattle(state)
        val result = state.pendingReports.firstOrNull().orEmpty()
        audio.playSfx(if (result.contains("得胜")) SfxKey.V3Success else SfxKey.V3Failure)
        message = if (state.battleState == null) state.pendingReports.firstOrNull() else null
        saveStore.save(state)
    }

    fun cancelBattle() {
        audio.click()
        state = V3GameEngine.cancelBattle(state)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun raiseBanner() {
        audio.playSfx(SfxKey.V3Finale)
        state = V3GameEngine.raiseBanner(state)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun chooseCard(cardId: String, choiceId: String) {
        audio.playSfx(SfxKey.V3Edict)
        val resolution = V3CardEngine.choose(state, cardId, choiceId)
        if (resolution == null) {
            message = "此项家务尚不能处置，或本月议事名额已用尽。"
            return
        }
        state = resolution.state
        message = resolution.message
        saveStore.save(state)
        if (state.pendingDice == null && state.activeCards.isEmpty()) resumeAfterModalIfClear()
    }

    fun resolveCardDice() {
        val resolution = V3CardEngine.resolveDice(state) ?: return
        audio.playSfx(if (resolution.dice?.success == true) SfxKey.V3Success else SfxKey.V3Failure)
        state = resolution.state
        message = resolution.message
        saveStore.save(state)
        if (state.activeCards.isEmpty()) resumeAfterModalIfClear()
    }

    fun setHexArms(tileKey: String, arms: com.arktools.daming.v3.data.V3HexArms) {
        val battle = state.hexBattleState ?: return
        state = state.copy(hexBattleState = battle.copy(selectedArms = battle.selectedArms + (tileKey to arms)))
        saveStore.save(state)
    }

    fun advanceHexTurn() {
        val battle = state.hexBattleState ?: return
        val nextTiles = battle.tiles.map { tile ->
            val selected = battle.selectedArms["${tile.q},${tile.r}"] ?: tile.arms
            val enemy = when ((tile.q * 7 + tile.r * 11 + battle.turn) % 3) {
                0 -> com.arktools.daming.v3.data.V3HexArms.Spear
                1 -> com.arktools.daming.v3.data.V3HexArms.Archer
                else -> com.arktools.daming.v3.data.V3HexArms.Cavalry
            }
            val advantage = if (selected.counters(enemy)) 13 else if (enemy.counters(selected)) -13 else 0
            val loss = (tile.enemyWave / 4 - advantage / 4).coerceIn(0, 12)
            tile.copy(arms = selected, garrison = (tile.garrison - loss).coerceAtLeast(0), breached = tile.garrison - loss <= 0, stable = tile.garrison - loss > 0)
        }
        val nextTurn = battle.turn + 1
        val victory = nextTiles.none { it.breached } && nextTurn > battle.maxTurns
        val finished = victory || nextTiles.any { it.breached } || nextTurn > battle.maxTurns
        state = state.copy(
            hexBattleState = battle.copy(
                turn = nextTurn,
                tiles = nextTiles,
                supply = (battle.supply - 8).coerceAtLeast(0),
                enemyMomentum = (battle.enemyMomentum + if (victory) -12 else 6).coerceIn(0, 100),
                selectedArms = emptyMap(),
                log = (battle.log + "第${battle.turn}轮守庄结算，${nextTiles.count { it.breached }}处庄门失守。").takeLast(20),
                finished = finished,
                victory = victory
            )
        )
        saveStore.save(state)
    }

    fun startHexBattle() {
        state = state.copy(hexBattleState = com.arktools.daming.v3.data.V3HexBattleState.initial())
        pauseForModal()
        saveStore.save(state)
    }

    fun closeHexBattle() {
        val battle = state.hexBattleState ?: return
        state = if (battle.victory) {
            state.copy(hexBattleState = null, garrisonMorale = (state.garrisonMorale + 8).coerceIn(0, 100), influence = (state.influence + 6).coerceIn(0, 100), pendingReports = listOf("六处庄门守住，族谱记下这一夜。"))
        } else {
            state.copy(hexBattleState = null, garrisonMorale = (state.garrisonMorale - 10).coerceIn(0, 100), cohesion = (state.cohesion - 8).coerceIn(0, 100), pendingReports = listOf("庄门有失，族内需要重新整顿。"))
        }
        saveStore.save(state)
        resumeAfterModalIfClear()
    }

    fun advanceMonth(showReport: Boolean = true) {
        audio.playSfx(SfxKey.V3ResourceSettle)
        val report = V3GameEngine.advanceMonth(state)
        val isFailureEnding = V3GameEngine.isFailureEnding(report.nextState)
        val isTimelineEnding = V3GameEngine.isTimelineEnding(report.nextState)
        val needsFinalDecision =
            isTimelineEnding &&
                "final_eve" !in report.nextState.seenChapterMilestones
        val generatedEvent = when {
            isFailureEnding -> null
            needsFinalDecision ->
                V3EventEngine.finalDecisionEvent(report.nextState)
                    ?.let { event ->
                        V3EventEngine.personalizeEvent(
                            event,
                            report.nextState
                        )
                    }
            !isTimelineEnding &&
                shouldGenerateEventThisMonth(report.nextState) ->
                V3EventEngine.generateEvent(report.nextState)
                    ?.let { event ->
                        V3EventEngine.personalizeEvent(
                            event,
                            report.nextState
                        )
                    }
            else -> null
        }
        val withEnding = when {
            isFailureEnding ->
                report.nextState.copy(
                    finalEnding =
                        V3GameEngine.finalizeEnding(
                            report.nextState
                        ),
                    activeEvent = null
                )
            isTimelineEnding && !needsFinalDecision ->
                report.nextState.copy(
                    finalEnding =
                        V3GameEngine.finalizeEnding(
                            report.nextState
                        ),
                    activeEvent = null
                )
            else ->
                report.nextState.copy(
                    activeEvent = generatedEvent
                )
        }
        state = withEnding
        saveStore.save(state)
        val reportRequested =
            showReport ||
                report.nextState.month == 1 ||
                report.lines.any {
                    it.contains("目标达成") ||
                        it.contains("添丁") ||
                        it.contains("终局") ||
                        it.contains("岁末")
                }
        val terminalModalVisible =
            needsFinalDecision ||
                withEnding.finalEnding != null
        latestReport = if (
            reportRequested && !terminalModalVisible
        ) {
            report.copy(nextState = withEnding)
        } else {
            null
        }
        completeTutorialAction(16)
        if (
            withEnding.activeEvent != null ||
            withEnding.finalEnding != null ||
            latestReport != null
        ) {
            pauseForModal()
        }
    }

    fun chooseEvent(choice: V3EventChoice) {
        audio.playSfx(SfxKey.V3Edict)
        pauseForModal()
        val resolved = V3EventEngine.choose(state, choice)
        state = if (
            V3GameEngine.isTimelineEnding(resolved) &&
                "final_eve" in resolved.seenChapterMilestones
        ) {
            resolved.copy(
                finalEnding = V3GameEngine.finalizeEnding(resolved),
                activeEvent = null
            )
        } else {
            resolved
        }
        completeTutorialAction(18)
        message = if (
            state.finalEnding == null &&
            state.tutorialCompleted
        ) {
            state.pendingReports.firstOrNull()
        } else {
            null
        }
        saveStore.save(state)
        if (message == null && state.finalEnding == null) {
            resumeAfterModalIfClear()
        }
    }

    fun restartAfterEnding() {
        audio.click()
        val founder = state.people.firstOrNull { it.id == 1 }?.name.orEmpty()
        val givenName = founder.removePrefix(state.surname).ifBlank { "慎行" }
        state = V3Content.newGame(state.root, state.county, state.creed, state.crisis, state.surname, givenName)
        saveStore.save(state)
        latestReport = null
        message = null
        timeSpeed = 0
        lastActiveSpeed = 1
        resumeSpeedAfterModal = null
        settingsVisible = false
        screen = V3Screen.County
    }

    fun clearReportAndNavigate(destination: V3Screen) {
        audio.click()
        latestReport = null
        completeTutorialAction(17)
        if (state.tutorialStep == 18 && state.activeEvent == null) {
            completeTutorialAction(18)
        }
        screen = destination
        resumeAfterModalIfClear()
    }

    fun clearReport() {
        audio.click()
        latestReport = null
        completeTutorialAction(17)
        if (state.tutorialStep == 18 && state.activeEvent == null) {
            completeTutorialAction(18)
        }
        resumeAfterModalIfClear()
    }

    fun clearMessage() {
        audio.click()
        message = null
        resumeAfterModalIfClear()
    }

    fun showInfo(text: String) {
        audio.click()
        if (!state.tutorialCompleted) return
        pauseForModal()
        message = text
    }

    fun grantMonthlyReward(
        description: String,
        silver: Int = 0,
        grain: Int = 0,
        cohesion: Int = 0,
        repairDurability: Int = 0
    ) {
        val repairedEquipment = if (repairDurability > 0) {
            state.equipment.map { item ->
                item.copy(durability = (item.durability + repairDurability).coerceAtMost(item.maxDurability))
            }
        } else {
            state.equipment
        }
        state = state.copy(
            silver = (state.silver + silver).coerceIn(-999, 999_999),
            grain = (state.grain + grain).coerceIn(-999, 999_999),
            cohesion = (state.cohesion + cohesion).coerceIn(0, 100),
            equipment = repairedEquipment
        )
        saveStore.save(state)
        latestReport = null
        showInfo(description)
    }

    private fun pauseForModal() {
        if (timeSpeed > 0 && resumeSpeedAfterModal == null) resumeSpeedAfterModal = timeSpeed
        timeSpeed = 0
    }

    private fun resumeAfterModalIfClear() {
        val blocked = latestReport != null || message != null || settingsVisible || state.activeEvent != null ||
            state.examSession != null || state.battleState != null || state.conquestState != null || state.finalEnding != null
        if (blocked) return
        resumeSpeedAfterModal?.let { speed ->
            timeSpeed = speed
            lastActiveSpeed = speed
        }
        resumeSpeedAfterModal = null
    }

    private fun shouldGenerateEventThisMonth(nextState: V3GameState): Boolean {
        if (nextState.month !in listOf(2, 5, 8, 11)) return false
        if (nextState.eventLog.take(2).any { it.contains("事件【") || it.contains("抉择") }) return false
        return true
    }

    fun advanceTutorial(requiredStep: Int) {
        completeTutorialAction(requiredStep)
    }

    fun finishTutorial() {
        if (state.tutorialStep < TUTORIAL_STEP_COUNT - 1) return
        state = state.copy(tutorialStep = TUTORIAL_STEP_COUNT, tutorialCompleted = true)
        saveStore.save(state)
    }

    fun skipTutorial() {
        state = state.copy(tutorialStep = TUTORIAL_STEP_COUNT, tutorialCompleted = true)
        saveStore.save(state)
    }

    fun reopenTutorial() {
        state = state.copy(
            tutorialVersion = com.arktools.daming.v3.data.V3_TUTORIAL_VERSION,
            tutorialStep = 0,
            tutorialCompleted = false
        )
        screen = V3Screen.County
        saveStore.save(state)
    }

    private fun completeTutorialAction(requiredStep: Int) {
        if (state.tutorialCompleted || state.tutorialStep != requiredStep) return
        if (requiredStep >= TUTORIAL_STEP_COUNT) return
        val nextStep = requiredStep + 1
        state = state.copy(
            tutorialStep = nextStep,
            tutorialCompleted = nextStep >= TUTORIAL_STEP_COUNT
        )
        saveStore.save(state)
    }

    fun openPlayGuide() {
        audio.playSfx(SfxKey.UiSelect)
        message = "族老札记：立户之后，先娶妻安家，再置田庄、开集市、修书院、筑寨堡。孩童可培养，成年可派差；学识可入科举，武艺可讨流寇。等族望、乡勇和地域控制足够，${state.surname}氏便能在乱世中择路而行。"
    }

    fun openAudioVisualGuide() {
        audio.playSfx(SfxKey.UiSelect)
        message = "三代采用案牍卷轴、县域旧地图与宗祠议事风格界面，并配有专属县域主题音乐、印信、落笔、营建与终局音效。"
    }

    fun playGuideTick() {
        audio.playSfx(SfxKey.UiSelect)
    }

    fun playUiClick() {
        audio.click()
    }

    fun playUiSelect() {
        audio.select()
    }
}
