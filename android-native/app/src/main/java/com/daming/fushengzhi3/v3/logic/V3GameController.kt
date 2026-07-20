package com.daming.fushengzhi3.v3.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.daming.fushengzhi3.audio.GameAudio
import com.daming.fushengzhi3.data.BgmKey
import com.daming.fushengzhi3.data.SfxKey
import com.daming.fushengzhi3.persistence.V3SaveStore
import com.daming.fushengzhi3.v3.data.V3Content
import com.daming.fushengzhi3.v3.data.V3GameState
import com.daming.fushengzhi3.v3.data.V3MonthlyReport
import com.daming.fushengzhi3.v3.data.V3Screen
import com.daming.fushengzhi3.v3.data.V3TaskType
import com.daming.fushengzhi3.v3.data.V3TrainingType
import com.daming.fushengzhi3.v3.data.V3EventChoice
import com.daming.fushengzhi3.v3.data.V3EstateType
import com.daming.fushengzhi3.v3.data.V3TroopType

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
        message = "你从一户起家。先娶妻成家，再置产业、养子嗣、派族人经营。时间默认暂停，处理完家业后再点继续。"
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

    fun holdCouncil(agenda: String) {
        audio.playSfx(SfxKey.V3Edict)
        state = V3GameEngine.holdCouncil(state, agenda)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun autoArrangeMonth() {
        audio.playSfx(SfxKey.V3Edict)
        state = V3GameEngine.autoArrangeMonth(state)
        if (state.people.any { it.alive && (it.currentTask != null || it.trainingFocus != null) }) completeTutorialAction(2)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun assignTask(personId: Int, siteId: String, task: V3TaskType) {
        audio.select()
        state = V3GameEngine.assignTask(state, personId, siteId, task)
        val assigned = state.people.firstOrNull { it.id == personId }
        if (assigned?.assignedSiteId == siteId && assigned.currentTask == task) completeTutorialAction(2)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun upgradeSite(siteId: String) {
        audio.playSfx(SfxKey.V3Build)
        state = V3GameEngine.upgradeSite(state, siteId)
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

    fun buyEquipment(slot: com.daming.fushengzhi3.v3.data.V3EquipmentSlot, quality: com.daming.fushengzhi3.v3.data.V3EquipmentQuality) {
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
        message = if (state.battleState?.phase == com.daming.fushengzhi3.v3.data.V3BattlePhase.Fighting) null else state.pendingReports.firstOrNull()
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

    fun advanceMonth(showReport: Boolean = true) {
        audio.playSfx(SfxKey.V3ResourceSettle)
        val report = V3GameEngine.advanceMonth(state)
        val generatedEvent = if (shouldGenerateEventThisMonth(report.nextState)) {
            V3EventEngine.generateEvent(report.nextState)?.let { event ->
                V3EventEngine.personalizeEvent(event, report.nextState)
            }
        } else {
            null
        }
        val withEnding = if (V3GameEngine.shouldAutoEnd(report.nextState)) {
            report.nextState.copy(finalEnding = V3GameEngine.finalizeEnding(report.nextState), activeEvent = null)
        } else {
            report.nextState.copy(activeEvent = generatedEvent)
        }
        state = withEnding
        saveStore.save(state)
        val shouldShowReport = showReport || report.nextState.month == 1 || report.lines.any { it.contains("目标达成") || it.contains("添丁") || it.contains("终局") || it.contains("岁末") }
        latestReport = if (shouldShowReport) report.copy(nextState = withEnding) else null
        completeTutorialAction(3)
        if (withEnding.activeEvent != null || latestReport != null) pauseForModal()
    }

    fun chooseEvent(choice: V3EventChoice) {
        audio.playSfx(SfxKey.V3Edict)
        pauseForModal()
        state = V3EventEngine.choose(state, choice)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun finalizeGame() {
        audio.playSfx(SfxKey.V3Finale)
        state = state.copy(finalEnding = V3GameEngine.finalizeEnding(state), activeEvent = null)
        saveStore.save(state)
    }

    fun restartAfterEnding() {
        audio.click()
        val founder = state.people.firstOrNull { it.id == 1 }?.name.orEmpty()
        val givenName = founder.removePrefix(state.surname).ifBlank { "慎行" }
        state = V3Content.newGame(state.root, state.county, state.creed, state.crisis, state.surname, givenName)
        saveStore.save(state)
        latestReport = null
        message = "新一轮县域宗族沙盘已重开。"
        timeSpeed = 0
        lastActiveSpeed = 1
        resumeSpeedAfterModal = null
        settingsVisible = false
        screen = V3Screen.County
    }

    fun clearReport() {
        audio.click()
        latestReport = null
        resumeAfterModalIfClear()
    }

    fun clearMessage() {
        audio.click()
        message = null
        resumeAfterModalIfClear()
    }

    fun showInfo(text: String) {
        audio.click()
        pauseForModal()
        message = text
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

    fun observeTutorialLedger() {
        completeTutorialAction(0)
    }

    fun observeTutorialSite() {
        completeTutorialAction(1)
    }

    fun finishTutorial() {
        if (state.tutorialStep < 4) return
        state = state.copy(tutorialStep = 5, tutorialCompleted = true)
        saveStore.save(state)
    }

    fun skipTutorial() {
        state = state.copy(tutorialStep = 5, tutorialCompleted = true)
        saveStore.save(state)
    }

    fun reopenTutorial() {
        state = state.copy(tutorialStep = 0, tutorialCompleted = false)
        screen = V3Screen.County
        saveStore.save(state)
    }

    private fun completeTutorialAction(requiredStep: Int) {
        if (state.tutorialCompleted || state.tutorialStep != requiredStep) return
        val nextStep = requiredStep + 1
        state = state.copy(
            tutorialStep = nextStep,
            tutorialCompleted = nextStep >= 5
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
}
