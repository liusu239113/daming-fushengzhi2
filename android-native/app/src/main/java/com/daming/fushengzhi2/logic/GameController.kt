package com.daming.fushengzhi2.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.daming.fushengzhi2.audio.GameAudio
import com.daming.fushengzhi2.data.BgmKey
import com.daming.fushengzhi2.data.GameState
import com.daming.fushengzhi2.data.GameTab
import com.daming.fushengzhi2.data.MemberState
import com.daming.fushengzhi2.data.MonthlyReport
import com.daming.fushengzhi2.data.SfxKey
import com.daming.fushengzhi2.persistence.SaveStore

class GameController(private val saveStore: SaveStore, val audio: GameAudio) {
    enum class Screen { Menu, Create, Game, V3Create, V3Game }
    enum class ClanSubTab(val label: String) { Main("宗祠"), Rules("[锁]族规"), Chronicle("家族志") }
    enum class IndustrySubTab(val label: String) { Main("产业"), Market("[锁]集市"), Store("[锁]库房") }
    enum class CareerSubTab(val label: String) { Career("仕途"), Academy("书院"), Expedition("历练") }

    var screen by mutableStateOf(Screen.Menu)
        private set
    var state by mutableStateOf<GameState?>(null)
        private set
    var tab by mutableStateOf(GameTab.Tree)
        private set
    var clanSubTab by mutableStateOf(ClanSubTab.Main)
        private set
    var industrySubTab by mutableStateOf(IndustrySubTab.Main)
        private set
    var careerSubTab by mutableStateOf(CareerSubTab.Career)
        private set
    var speed by mutableStateOf(0)
        private set
    var latestReport by mutableStateOf<MonthlyReport?>(null)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    fun newGame(surname: String, originId: String, regionId: String, mottoId: String, difficultyId: String) {
        audio.click()
        state = GameEngine.newGame(surname, originId, regionId, mottoId, difficultyId)
        tab = GameTab.Tree
        clanSubTab = ClanSubTab.Main
        industrySubTab = IndustrySubTab.Main
        careerSubTab = CareerSubTab.Career
        speed = 0
        latestReport = null
        saveStore.save(requireState(), SaveStore.Slot.Auto)
        screen = Screen.Game
        audio.playGameBgm(requireState().year)
    }

    fun openCreate() {
        audio.click()
        screen = Screen.Create
    }

    fun openV3Create() {
        audio.click()
        screen = Screen.V3Create
    }

    fun openV3Game() {
        audio.click()
        screen = Screen.V3Game
    }

    fun backToMenu() {
        audio.back()
        state?.let { saveStore.save(it, SaveStore.Slot.Auto) }
        screen = Screen.Menu
        audio.playBgm(BgmKey.Menu)
    }

    fun continueLatest() {
        audio.click()
        val loaded = saveStore.loadLatest()
        if (loaded == null) {
            message = "暂无可继续的存档"
        } else {
            state = loaded
            tab = GameTab.Tree
            clanSubTab = ClanSubTab.Main
            industrySubTab = IndustrySubTab.Main
            careerSubTab = CareerSubTab.Career
            speed = 0
            latestReport = null
            screen = Screen.Game
            audio.playGameBgm(loaded.year)
        }
    }

    fun hasAnySave(): Boolean = saveStore.latestSlot() != null

    fun openArchiveHint() {
        audio.click()
        message = if (hasAnySave()) "已发现本地存档，可点击继续游戏读取最近进度。" else "暂无本地存档"
    }

    fun openSettingsHint() {
        audio.click()
        message = "已启用原作 BGM 与点击、切页、结算等音效。"
    }

    fun saveManual() {
        audio.click()
        saveStore.save(requireState(), SaveStore.Slot.Manual)
        message = "已保存到手动存档"
    }

    fun deleteSaves() {
        audio.playSfx(SfxKey.UiBack)
        SaveStore.Slot.entries.forEach { saveStore.delete(it) }
        message = "本地存档已删除"
    }

    fun switchTab(next: GameTab) {
        if (tab != next) audio.tabSwitch()
        tab = next
    }

    fun switchClanSubTab(next: ClanSubTab) {
        if (clanSubTab != next) audio.select()
        clanSubTab = next
    }

    fun switchIndustrySubTab(next: IndustrySubTab) {
        if (industrySubTab != next) audio.select()
        industrySubTab = next
    }

    fun switchCareerSubTab(next: CareerSubTab) {
        if (careerSubTab != next) audio.select()
        careerSubTab = next
    }

    fun cycleSpeed() {
        audio.click()
        speed = (speed + 1) % 4
    }

    fun pauseSpeed() {
        audio.click()
        speed = 0
    }

    fun advanceMonth() {
        audio.monthTick()
        val (next, report) = GameEngine.advanceMonth(requireState())
        state = next
        latestReport = report
        saveStore.save(next, SaveStore.Slot.Auto)
        audio.playGameBgm(next.year)
        if (report.incomeSilver + report.incomeGrain + report.incomeCloth + report.incomeFame >= report.expenseSilver + report.expenseGrain + report.expenseCloth + report.expenseFame) {
            audio.gain()
        } else {
            audio.loss()
        }
    }

    fun rankUp() {
        audio.celebrate()
        val before = requireState().clanRank
        val next = GameEngine.rankUp(requireState())
        state = next
        message = if (next.clanRank > before) "宗族品级提升" else "晋升条件尚未满足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun addIndustry(typeId: String) {
        audio.click()
        val before = requireState()
        val next = GameEngine.addIndustry(before, typeId)
        state = next
        message = if (next != before) "产业已新建" else "资源不足、未解锁或已达上限"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun upgradeIndustry(industryId: Int) {
        audio.click()
        val before = requireState()
        val next = GameEngine.upgradeIndustry(before, industryId)
        state = next
        message = if (next != before) "产业已升级" else "银两不足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun sellIndustry(industryId: Int) {
        audio.playSfx(SfxKey.UiBack)
        val before = requireState()
        val next = GameEngine.sellIndustry(before, industryId)
        state = next
        message = if (next != before) "产业已变卖" else "产业不存在"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun evolveIndustry(industryId: Int) {
        audio.celebrate()
        val before = requireState()
        val next = GameEngine.evolveIndustry(before, industryId)
        state = next
        message = if (next != before) "产业已进化" else "等级、品级或银两不足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun assignIndustry(industryId: Int, memberId: Int?) {
        state = GameEngine.assignIndustry(requireState(), industryId, memberId)
        saveStore.save(requireState(), SaveStore.Slot.Auto)
    }

    fun setMemberState(memberId: Int, memberState: MemberState, laborJobId: String? = null) {
        audio.select()
        state = GameEngine.setMemberState(requireState(), memberId, memberState, laborJobId)
        saveStore.save(requireState(), SaveStore.Slot.Auto)
    }

    fun toggleRule(ruleId: String) {
        audio.select()
        state = GameEngine.toggleClanRule(requireState(), ruleId)
        saveStore.save(requireState(), SaveStore.Slot.Auto)
    }

    fun buyMarketItem(itemId: String, count: Int = 1) {
        audio.click()
        val before = requireState()
        val next = GameEngine.buyMarketItem(before, itemId, count)
        state = next
        message = if (next != before) "交易完成" else "银两不足或商品无效"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun sellMarketItem(itemId: String, count: Int = 1) {
        audio.playSfx(SfxKey.UiBack)
        val before = requireState()
        val next = GameEngine.sellMarketItem(before, itemId, count)
        state = next
        message = if (next != before) "交易完成" else "库存不足或商品无效"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun useItem(itemId: String, memberId: Int? = null) {
        audio.select()
        val before = requireState()
        val next = GameEngine.useItem(before, itemId, memberId)
        state = next
        message = if (next != before) "物品已使用" else "物品不足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun upgradeAcademy(typeId: String) {
        audio.click()
        val before = requireState()
        val next = GameEngine.upgradeAcademy(before, typeId)
        state = next
        message = if (next != before) "设施已升级" else "未解锁或银两不足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun assignAcademy(typeId: String, memberId: Int) {
        audio.select()
        state = GameEngine.assignAcademy(requireState(), typeId, memberId)
        saveStore.save(requireState(), SaveStore.Slot.Auto)
    }

    fun startExpedition(typeId: String, memberId: Int) {
        audio.select()
        val before = requireState()
        val next = GameEngine.startExpedition(before, typeId, memberId)
        state = next
        message = if (next != before) "历练已开始" else "条件不足或族人不可用"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun takeExam(memberId: Int, examId: String) {
        audio.examPass()
        val before = requireState()
        val next = GameEngine.takeExam(before, memberId, examId)
        state = next
        message = if (next != before) "科举已结算" else "学识不足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun donateIdentity(memberId: Int) {
        audio.click()
        val before = requireState()
        val next = GameEngine.donateIdentity(before, memberId)
        state = next
        message = if (next != before) "已纳捐监生" else "需要望族、银500、声望15"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun appointOfficial(memberId: Int, rankId: String) {
        audio.celebrate()
        val before = requireState()
        val next = GameEngine.appointOfficial(before, memberId, rankId)
        state = next
        message = if (next != before) "已入仕为官" else "身份条件不足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun arrangeMarriage(memberId: Int, tierId: String) {
        audio.celebrate()
        val before = requireState()
        val next = GameEngine.arrangeMarriage(before, memberId, tierId)
        state = next
        message = if (next != before) "联姻完成" else "聘礼、声望、品级或族人状态不足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun recruitArmy(infantry: Int, archers: Int) {
        audio.battle()
        val before = requireState()
        val next = GameEngine.recruitArmy(before, infantry, archers)
        state = next
        message = if (next != before) "士兵已招募" else "资源不足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun trainArmy() {
        audio.battle()
        val before = requireState()
        val next = GameEngine.trainArmy(before)
        state = next
        message = if (next != before) "训练完成" else "训练已满或银两不足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun attackStage(stageId: Int) {
        audio.battle()
        val before = requireState()
        val next = GameEngine.attackStage(before, stageId)
        state = next
        message = if (next != before) "征伐已结算" else "关卡无效或已征服"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun adoptPet(type: String) {
        audio.celebrate()
        val before = requireState()
        val next = GameEngine.adoptPet(before, type)
        state = next
        message = if (next != before) "宠物已收养" else "已有宠物或粮食不足"
        saveStore.save(next, SaveStore.Slot.Auto)
    }

    fun sacrifice() {
        audio.celebrate()
        val before = requireState()
        val next = GameEngine.sacrifice(before)
        state = next
        message = if (next != before) "祭祀完成" else "本年已祭祀或粮食不足"
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
