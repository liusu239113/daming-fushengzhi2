package com.daming.fushengzhi2.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.daming.fushengzhi2.data.GameState
import com.daming.fushengzhi2.data.GameTab
import com.daming.fushengzhi2.data.MemberState
import com.daming.fushengzhi2.data.MonthlyReport
import com.daming.fushengzhi2.persistence.SaveStore

class GameController(private val saveStore: SaveStore) {
    enum class Screen { Menu, Create, Game }

    var screen by mutableStateOf(Screen.Menu)
        private set
    var state by mutableStateOf<GameState?>(null)
        private set
    var tab by mutableStateOf(GameTab.Tree)
        private set
    var latestReport by mutableStateOf<MonthlyReport?>(null)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    fun newGame(surname: String, originId: String, regionId: String, mottoId: String, difficultyId: String) {
        state = GameEngine.newGame(surname, originId, regionId, mottoId, difficultyId)
        tab = GameTab.Tree
        latestReport = null
        saveStore.save(requireState(), SaveStore.Slot.Auto)
        screen = Screen.Game
    }

    fun openCreate() {
        screen = Screen.Create
    }

    fun backToMenu() {
        state?.let { saveStore.save(it, SaveStore.Slot.Auto) }
        screen = Screen.Menu
    }

    fun continueLatest() {
        val loaded = saveStore.loadLatest()
        if (loaded == null) {
            message = "暂无可继续的存档"
        } else {
            state = loaded
            tab = GameTab.Tree
            latestReport = null
            screen = Screen.Game
        }
    }

    fun hasAnySave(): Boolean = saveStore.latestSlot() != null

    fun saveManual() {
        saveStore.save(requireState(), SaveStore.Slot.Manual)
        message = "已保存到手动存档"
    }

    fun deleteSaves() {
        SaveStore.Slot.entries.forEach { saveStore.delete(it) }
        message = "本地存档已删除"
    }

    fun switchTab(next: GameTab) {
        tab = next
    }

    fun advanceMonth() {
        val (next, report) = GameEngine.advanceMonth(requireState())
        state = next
        latestReport = report
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun rankUp() {
        val before = requireState().clanRank
        val next = GameEngine.rankUp(requireState())
        state = next
        message = if (next.clanRank > before) "宗族品级提升" else "晋升条件尚未满足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun addIndustry(typeId: String) {
        val before = requireState()
        val next = GameEngine.addIndustry(before, typeId)
        state = next
        message = if (next != before) "产业已新建" else "资源不足、未解锁或已达上限"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun upgradeIndustry(industryId: Int) {
        val before = requireState()
        val next = GameEngine.upgradeIndustry(before, industryId)
        state = next
        message = if (next != before) "产业已升级" else "银两不足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun assignIndustry(industryId: Int, memberId: Int?) {
        state = GameEngine.assignIndustry(requireState(), industryId, memberId)
        saveStore.save(requireState(), SaveStore.Slot.Auto)
    }

    fun setMemberState(memberId: Int, memberState: MemberState) {
        state = GameEngine.setMemberState(requireState(), memberId, memberState)
        saveStore.save(requireState(), SaveStore.Slot.Auto)
    }

    fun toggleRule(ruleId: String) {
        state = GameEngine.toggleClanRule(requireState(), ruleId)
        saveStore.save(requireState(), SaveStore.Slot.Auto)
    }



    fun buyMarketItem(itemId: String, count: Int = 1) {
        val before = requireState()
        val next = GameEngine.buyMarketItem(before, itemId, count)
        state = next
        message = if (next \!= before) "交易完成" else "银两不足或商品无效"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun sellMarketItem(itemId: String, count: Int = 1) {
        val before = requireState()
        val next = GameEngine.sellMarketItem(before, itemId, count)
        state = next
        message = if (next \!= before) "交易完成" else "库存不足或商品无效"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun useItem(itemId: String, memberId: Int? = null) {
        val before = requireState()
        val next = GameEngine.useItem(before, itemId, memberId)
        state = next
        message = if (next \!= before) "物品已使用" else "物品不足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun upgradeAcademy(typeId: String) {
        val before = requireState()
        val next = GameEngine.upgradeAcademy(before, typeId)
        state = next
        message = if (next \!= before) "设施已升级" else "未解锁或银两不足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun assignAcademy(typeId: String, memberId: Int) {
        state = GameEngine.assignAcademy(requireState(), typeId, memberId)
        saveStore.save(requireState(), SaveStore.Slot.Auto)
    }

    fun startExpedition(typeId: String, memberId: Int) {
        val before = requireState()
        val next = GameEngine.startExpedition(before, typeId, memberId)
        state = next
        message = if (next \!= before) "历练已开始" else "条件不足或族人不可用"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun takeExam(memberId: Int, examId: String) {
        val before = requireState()
        val next = GameEngine.takeExam(before, memberId, examId)
        state = next
        message = if (next \!= before) "科举已结算" else "学识不足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun recruitArmy(infantry: Int, archers: Int) {
        val before = requireState()
        val next = GameEngine.recruitArmy(before, infantry, archers)
        state = next
        message = if (next \!= before) "士兵已招募" else "资源不足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun attackStage(stageId: Int) {
        val before = requireState()
        val next = GameEngine.attackStage(before, stageId)
        state = next
        message = if (next \!= before) "征伐已结算" else "关卡无效或已征服"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun clearMessage() {
        message = null
    }

    fun clearReport() {
        latestReport = null
    }

    private fun requireState(): GameState = requireNotNull(state) { "Game state is not initialized" }
}
