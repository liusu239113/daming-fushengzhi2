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

class V3GameController(private val saveStore: V3SaveStore, private val audio: GameAudio) {
    var state by mutableStateOf(saveStore.load() ?: V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税"))
        private set

    var screen by mutableStateOf(V3Screen.County)
        private set

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

    fun newGame(root: String, county: String, creed: String, crisis: String) {
        audio.click()
        ensureV3Bgm()
        state = V3Content.newGame(root, county, creed, crisis)
        saveStore.save(state)
        screen = V3Screen.County
        latestReport = null
        message = "你从一户起家。先娶妻成家，再置产业、养子嗣、派族人经营，逐步把小户养成大族。"
    }

    fun hasSave(): Boolean = saveStore.hasSave()

    fun continueGame() {
        audio.click()
        ensureV3Bgm()
        state = saveStore.load() ?: state
        screen = V3Screen.County
        latestReport = null
        message = "案卷已启封，旧日县域局势重归案前。"
    }

    fun switchScreen(next: V3Screen) {
        if (screen != next) audio.tabSwitch()
        screen = next
    }

    fun openSettings() {
        audio.click()
        settingsVisible = true
    }

    fun closeSettings() {
        audio.click()
        settingsVisible = false
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

    fun marry(candidateId: String) {
        audio.playSfx(SfxKey.V3Edict)
        state = V3GameEngine.marry(state, candidateId)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun rankUp() {
        audio.playSfx(SfxKey.V3Build)
        state = V3GameEngine.rankUp(state)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun assignTask(personId: Int, siteId: String, task: V3TaskType) {
        audio.select()
        state = V3GameEngine.assignTask(state, personId, siteId, task)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun upgradeSite(siteId: String) {
        audio.playSfx(SfxKey.V3Build)
        state = V3GameEngine.upgradeSite(state, siteId)
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
        audio.playSfx(SfxKey.V3Edict)
        state = V3GameEngine.answerExam(state, answerIndex)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun startBattle() {
        audio.playSfx(SfxKey.V3Dispute)
        state = V3GameEngine.startBattle(state)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun resolveBattle() {
        audio.playSfx(SfxKey.V3Dispute)
        state = V3GameEngine.resolveBattle(state)
        message = state.pendingReports.firstOrNull()
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

    fun advanceMonth() {
        audio.monthTick()
        val report = V3GameEngine.advanceMonth(state)
        val withEnding = if (V3GameEngine.shouldAutoEnd(report.nextState)) {
            report.nextState.copy(finalEnding = V3GameEngine.finalizeEnding(report.nextState), activeEvent = null)
        } else {
            report.nextState.copy(activeEvent = V3EventEngine.generateEvent(report.nextState))
        }
        state = withEnding
        saveStore.save(state)
        latestReport = report.copy(nextState = withEnding)
    }

    fun chooseEvent(choice: V3EventChoice) {
        audio.playSfx(SfxKey.V3Edict)
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
        state = V3Content.newGame(state.root, state.county, state.creed, state.crisis)
        saveStore.save(state)
        latestReport = null
        message = "新一轮县域宗族沙盘已重开。"
        screen = V3Screen.County
    }

    fun clearReport() {
        latestReport = null
    }

    fun clearMessage() {
        message = null
    }

    fun openPlayGuide() {
        audio.playSfx(SfxKey.UiSelect)
        message = "玩法核心：从一户起家，先通过婚配形成家庭，再置办田庄、集市、书院、寨堡等产业。族人可以每月培养属性；成年后可派去经营地点。学识高的族人可参加科举答题，武艺和乡勇足够后可讨伐流寇；控制据点、积累乡勇和族望后可以尝试举旗造反。"
    }

    fun openAudioVisualGuide() {
        audio.playSfx(SfxKey.UiSelect)
        message = "三代采用案牍卷轴、县域旧地图与宗祠议事风格界面，并配有专属县域主题音乐、印信、落笔、营建与终局音效。"
    }
}
