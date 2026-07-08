package com.daming.fushengzhi2.v3.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.daming.fushengzhi2.audio.GameAudio
import com.daming.fushengzhi2.data.BgmKey
import com.daming.fushengzhi2.data.SfxKey
import com.daming.fushengzhi2.persistence.V3SaveStore
import com.daming.fushengzhi2.v3.data.V3Content
import com.daming.fushengzhi2.v3.data.V3GameState
import com.daming.fushengzhi2.v3.data.V3MonthlyReport
import com.daming.fushengzhi2.v3.data.V3Screen
import com.daming.fushengzhi2.v3.data.V3TaskType
import com.daming.fushengzhi2.v3.data.V3EventChoice

class V3GameController(private val saveStore: V3SaveStore, private val audio: GameAudio) {
    var state by mutableStateOf(saveStore.load() ?: V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税"))
        private set

    var screen by mutableStateOf(V3Screen.County)
        private set

    var latestReport by mutableStateOf<V3MonthlyReport?>(null)
        private set

    var message by mutableStateOf<String?>(null)
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
        message = "大明浮生志3框架已建立：县域地图、人物派遣、政略关系与路线倾向已接入。"
    }

    fun switchScreen(next: V3Screen) {
        if (screen != next) audio.tabSwitch()
        screen = next
    }

    fun assignTask(personId: Int, siteId: String, task: V3TaskType) {
        audio.select()
        state = V3GameEngine.assignTask(state, personId, siteId, task)
        saveStore.save(state)
    }

    fun upgradeSite(siteId: String) {
        audio.playSfx(SfxKey.V3Build)
        state = V3GameEngine.upgradeSite(state, siteId)
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

    fun openDesignHint() {
        audio.playSfx(SfxKey.UiSelect)
        message = "设计文档已写入 docs/DA_MING_FU_SHENG_ZHI_3_GDD.md。当前游戏内先接入三代可运行框架，后续逐步做满内容与资源。"
    }
}
