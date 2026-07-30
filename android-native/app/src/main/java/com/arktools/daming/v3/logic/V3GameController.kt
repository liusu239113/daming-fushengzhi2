package com.arktools.daming.v3.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arktools.daming.ads.AutomationQuotaStore
import com.arktools.daming.audio.GameAudio
import com.arktools.daming.data.BgmKey
import com.arktools.daming.data.SfxKey
import com.arktools.daming.persistence.V3SaveStore
import com.arktools.daming.v3.data.V3CardPool
import com.arktools.daming.v3.data.V3Content
import com.arktools.daming.v3.data.V3GameState
import com.arktools.daming.v3.data.V3MonthlyReport
import com.arktools.daming.v3.data.V3Screen
import com.arktools.daming.v3.data.V3TaskType
import com.arktools.daming.v3.data.V3TrainingType
import com.arktools.daming.v3.data.V3Person
import com.arktools.daming.v3.data.V3EventChoice
import com.arktools.daming.v3.data.V3EstateType
import com.arktools.daming.v3.data.V3TroopType
import com.arktools.daming.v3.data.V3CrisisAd
import com.arktools.daming.v3.data.V3CardRequire

class V3GameController(
    private val saveStore: V3SaveStore,
    private val audio: GameAudio,
    private val automationQuotaStore: AutomationQuotaStore
) {
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

    var pendingCrisisAd by mutableStateOf<V3CrisisAd?>(null)
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
        pendingCrisisAd = null
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
        pendingCrisisAd = null
    }

    companion object {
        const val AUTOMATION_ARRANGE = "arrange"
        const val AUTOMATION_ESTATE = "estate"

        // 完整教程包含界面导览、地点/族人弹窗教学和首月经营闭环。
        const val TUTORIAL_STEP_COUNT = 32

        // 只有会真实改变玩法状态或打开下一层操作界面的步骤才要求点击。
        // 其余步骤属于说明导览，由明确的“下一步”按钮推进。
        val TUTORIAL_ACTION_STEPS: Set<Int> = setOf(
            5, 8, 11, 14, 15, 16, 17, 18, 21, 27, 28, 29
        )

        fun tutorialStepRequiresAction(step: Int): Boolean =
            step in TUTORIAL_ACTION_STEPS
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

    fun timeBlockReason(): String? = when {
        latestReport != null -> "请先阅读月报"
        message != null -> "请先关闭提示"
        pendingCrisisAd != null -> "请先处理临危援手"
        settingsVisible -> "设置界面已打开"
        state.finalEnding != null -> "本局已经结束"
        state.activeEvent != null -> "请先处理月度事件"
        state.examSession != null -> "请先完成科举"
        state.miniGameSession != null -> "请先完成情境小游戏"
        state.battleState != null -> "请先完成地点讨伐"
        state.hexBattleState != null -> "请先完成守庄战"
        state.pendingSuccession -> "请先推举继任族长"
        state.activeCards.isNotEmpty() -> "请先处理月事或访客"
        state.pendingDice != null -> "请先完成当前判定"
        state.conquestState != null -> "请先完成地域征伐"
        else -> null
    }

    fun shouldAutoTick(): Boolean =
        timeSpeed > 0 && timeBlockReason() == null

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
        val before = state
        val target = before.people.firstOrNull { it.id == (targetPersonId ?: 1) && it.alive }
        val candidate = target?.let { V3GameEngine.marriageCandidatesFor(it, before).firstOrNull { candidate -> candidate.id == candidateId } }
        state = V3GameEngine.marry(before, candidateId, targetPersonId)
        message = state.pendingReports.firstOrNull()
        if (candidate != null && before.influence >= candidate.influenceReq) {
            offerResourceAid(
                keySuffix = "marry-${target.id}-$candidateId",
                actionTitle = "婚事周转",
                actionDescription = "这门亲事只差银粮便可落定",
                silverMissing = (candidate.silverCost - before.silver).coerceAtLeast(0),
                grainMissing = (candidate.grainCost - before.grain).coerceAtLeast(0)
            )
        }
        saveStore.save(state)
    }

    fun takeConcubine(personId: Int, candidateId: String) {
        audio.playSfx(SfxKey.V3Edict)
        val before = state
        val person = before.people.firstOrNull { it.id == personId && it.alive }
        val candidate = person?.let { V3GameEngine.concubineCandidatesFor(it, before).firstOrNull { candidate -> candidate.id == candidateId } }
        state = V3GameEngine.takeConcubine(before, personId, candidateId)
        message = state.pendingReports.firstOrNull()
        if (candidate != null) {
            offerResourceAid(
                keySuffix = "concubine-$personId-$candidateId",
                actionTitle = "婚事周转",
                actionDescription = "纳妾礼资只差银粮便可备齐",
                silverMissing = (candidate.silverCost * 60 / 100 - before.silver).coerceAtLeast(0),
                grainMissing = (candidate.grainCost * 60 / 100 - before.grain).coerceAtLeast(0)
            )
        }
        saveStore.save(state)
    }

    fun rankUp() {
        audio.playSfx(SfxKey.V3Build)
        val before = state
        val cost = V3GameEngine.nextRankCost(before)
        state = V3GameEngine.rankUp(before)
        message = state.pendingReports.firstOrNull()
        if (cost != null) {
            val resourceReadyState = before.copy(
                silver = maxOf(before.silver, cost.silver),
                grain = maxOf(before.grain, cost.grain)
            )
            if (V3GameEngine.canRankUp(resourceReadyState)) {
                offerResourceAid(
                    keySuffix = "rank-${before.clanRank + 1}",
                    actionTitle = "晋升筹资",
                    actionDescription = "宗族晋升的其他条件均已具备，只差银粮",
                    silverMissing = (cost.silver - before.silver).coerceAtLeast(0),
                    grainMissing = (cost.grain - before.grain).coerceAtLeast(0)
                )
            }
        }
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

    fun automationRemaining(action: String): Int = automationQuotaStore.remaining(action)

    fun autoArrangeMonth() {
        audio.playSfx(SfxKey.V3Edict)
        val actionable = state.people.any {
            it.alive && it.illness == null && it.currentTask == null && it.trainingFocus == null && it.fatigue < 70
        }
        if (!actionable) {
            message = "当前没有可安排的待命族人，不消耗今日一键次数。"
            return
        }
        if (!automationQuotaStore.consume(AUTOMATION_ARRANGE)) {
            offerAutomationQuota(AUTOMATION_ARRANGE, "一键安排")
            return
        }
        val beforeAssigned = state.people.count { it.alive && (it.currentTask != null || it.trainingFocus != null) }
        state = V3GameEngine.autoArrangeMonth(state)
        val afterAssigned = state.people.count { it.alive && (it.currentTask != null || it.trainingFocus != null) }
        if (afterAssigned > 0) {
            completeTutorialAction(15)
        }
        val arrangedCount = (afterAssigned - beforeAssigned).coerceAtLeast(0)
        message = when {
            arrangedCount > 0 -> "一键安排已完成：本次为 $arrangedCount 名待命族人安排了差事或培养。月结后兑现经营与成长结果。"
            afterAssigned > 0 -> "当前可用族人均已有差事或培养安排，无需重复安排。"
            else -> "当前没有可安排的成年待命族人，或可用地点尚未解锁。请先查看族人状态与县域地点。"
        }
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
        val before = state
        val site = before.sites.firstOrNull { it.id == siteId }
        val cost = site?.let(V3GameEngine::upgradeCost)
        state = V3GameEngine.upgradeSite(before, siteId)
        message = state.pendingReports.firstOrNull()
        if (site != null && cost != null && V3GameEngine.isSiteUnlocked(before, site.type)) {
            offerResourceAid(
                keySuffix = "site-$siteId-${site.level + 1}",
                actionTitle = "营建周转",
                actionDescription = "营建${site.name}只差银粮即可动工",
                silverMissing = (cost.silver - before.silver).coerceAtLeast(0),
                grainMissing = (cost.grain - before.grain).coerceAtLeast(0)
            )
        }
        saveStore.save(state)
    }

    fun autoManageEstates() {
        audio.playSfx(SfxKey.V3Build)
        val actionable = V3EstateType.entries.any { type ->
            if (!V3GameEngine.isEstateUnlocked(state, type)) return@any false
            val level = state.estateAssets.firstOrNull { it.type == type }?.level ?: 0
            if (level >= 5) return@any false
            val requiredPopulation = when (type) {
                V3EstateType.Workshop -> 4
                V3EstateType.Caravan -> 5
                V3EstateType.Barracks -> 6
                else -> 1
            }
            val cost = V3GameEngine.estateUpgradeCost(state, type)
            V3GameEngine.alivePeople(state).size >= requiredPopulation && state.silver >= cost.silver && state.grain >= cost.grain
        }
        if (!actionable) {
            message = "当前没有可负担的家产升级，不消耗今日一键次数。"
            return
        }
        if (!automationQuotaStore.consume(AUTOMATION_ESTATE)) {
            offerAutomationQuota(AUTOMATION_ESTATE, "一键营建")
            return
        }
        state = V3GameEngine.autoManageEstates(state)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun upgradeEstate(type: V3EstateType) {
        audio.playSfx(SfxKey.V3Build)
        val before = state
        val cost = V3GameEngine.estateUpgradeCost(before, type)
        val requiredPopulation = when (type) {
            V3EstateType.Workshop -> 4
            V3EstateType.Caravan -> 5
            V3EstateType.Barracks -> 6
            else -> 1
        }
        state = V3GameEngine.upgradeEstate(before, type)
        message = state.pendingReports.firstOrNull()
        if (
            V3GameEngine.isEstateUnlocked(before, type) &&
            V3GameEngine.alivePeople(before).size >= requiredPopulation &&
            (before.estateAssets.firstOrNull { it.type == type }?.level ?: 0) < 5
        ) {
            offerResourceAid(
                keySuffix = "estate-${type.name}-${(before.estateAssets.firstOrNull { it.type == type }?.level ?: 0) + 1}",
                actionTitle = "家产周转",
                actionDescription = "扩建${type.label}只差银粮即可开工",
                silverMissing = (cost.silver - before.silver).coerceAtLeast(0),
                grainMissing = (cost.grain - before.grain).coerceAtLeast(0)
            )
        }
        saveStore.save(state)
    }

    fun assignClinicHealer(personId: Int?) {
        audio.select()
        state = V3GameEngine.assignClinicHealer(state, personId)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun treatPersonAtClinic(personId: Int) {
        audio.playSfx(SfxKey.V3SpecialAction)
        val before = state
        state = V3GameEngine.treatPersonAtClinic(before, personId)
        message = state.pendingReports.firstOrNull()
        val person = before.people.firstOrNull { it.id == personId && it.alive }
        if (person != null) {
            val cost = V3GameEngine.treatmentCost(person.age)
            offerResourceAid(
                keySuffix = "clinic-person-$personId",
                actionTitle = "医药周转",
                actionDescription = "为${person.name}延医只差诊金",
                silverMissing = (cost - before.silver).coerceAtLeast(0),
                grainMissing = 0
            )
        }
        saveStore.save(state)
    }

    fun treatPatriarchAtClinic() {
        audio.playSfx(SfxKey.V3SpecialAction)
        val before = state
        state = V3GameEngine.treatPatriarchAtClinic(before)
        message = state.pendingReports.firstOrNull()
        val holder = before.people.firstOrNull { it.id == before.patriarch.personId && it.alive }
        val age = holder?.age ?: 60
        val cost = V3GameEngine.treatmentCost(age, patriarch = true)
        offerResourceAid(
            keySuffix = "clinic-patriarch",
            actionTitle = "族长求医",
            actionDescription = "族长身板告急，只差诊金便可入馆调治",
            silverMissing = (cost - before.silver).coerceAtLeast(0),
            grainMissing = 0
        )
        saveStore.save(state)
    }

    fun requestClinicAutoTreatmentAd() {
        if (state.clinicAutoTreatmentMonths > 0) {
            message = "两年自动治疗尚余${state.clinicAutoTreatmentMonths}个月，无需重复领取。"
            return
        }
        pendingCrisisAd = V3CrisisAd(
            key = "clinic-auto-treatment-${state.year}-${state.month}",
            title = "药商两年义约",
            subtitle = "当前确有治疗需求。完整观看后，药商将连续24个月承担族长与所有患病族人的自动调治；期间无需安排医师。",
            grantedMessage = "药商两年义约已生效：未来24个月族长和所有患病族人每月自动调治，医馆会持续显示剩余期限。",
            clinicAutoTreatmentMonths = 24
        )
        pauseForModal()
    }

    fun siteSpecialAction(siteId: String) {
        audio.playSfx(SfxKey.V3SpecialAction)
        val before = state
        val site = before.sites.firstOrNull { it.id == siteId }
        val cost = site?.let { V3GameEngine.siteSpecialActionCost(it.type) }
        state = V3GameEngine.siteSpecialAction(before, siteId)
        message = state.pendingReports.firstOrNull()
        val currentMonth = before.year * 12 + before.month
        if (
            site != null &&
            cost != null &&
            site.level > 0 &&
            before.siteSpecialActionMonths[site.id] != currentMonth &&
            V3GameEngine.isSiteUnlocked(before, site.type)
        ) {
            offerResourceAid(
                keySuffix = "site-action-$siteId-${before.year}-${before.month}",
                actionTitle = "专属事务周转",
                actionDescription = "执行${site.name}专属事务只差银粮即可安排",
                silverMissing = (cost.silver - before.silver).coerceAtLeast(0),
                grainMissing = (cost.grain - before.grain).coerceAtLeast(0)
            )
        }
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

    fun upgradeRegionalTradePost(regionId: String) {
        audio.playSfx(SfxKey.V3Build)
        state = V3GameEngine.upgradeRegionalTradePost(state, regionId)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun upgradeRegionalGarrison(regionId: String) {
        audio.playSfx(SfxKey.V3Build)
        state = V3GameEngine.upgradeRegionalGarrison(state, regionId)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun startConquest(regionId: String) {
        audio.playSfx(SfxKey.V3Dispute)
        val hadConquest = state.conquestState != null
        state = V3GameEngine.startConquest(state, regionId)
        if (!hadConquest && state.conquestState != null) pauseForModal()
        message = if (state.conquestState == null) state.pendingReports.firstOrNull() else null
        saveStore.save(state)
    }

    fun requestConquestTacticalAid() {
        val conquest = state.conquestState ?: return
        val assessment = V3GameEngine.conquestAssessment(state)
        if (assessment.total >= conquest.enemyPower) {
            message = "当前综合战力已不低于敌势，无需临时整军。"
            return
        }
        if (state.conquestTacticalAid > 0) {
            message = "军师整军增益已备妥：下一场地域征伐战力+${state.conquestTacticalAid}。"
            return
        }
        val aid = ((conquest.enemyPower - assessment.total) * 60 / 100).coerceIn(35, 120)
        pendingCrisisAd = V3CrisisAd(
            key = "conquest-tactics-${state.year}-${state.month}-${conquest.regionId}",
            title = "军师整军",
            subtitle = "当前综合战力${assessment.total}，敌势${conquest.enemyPower}。完整观看后获得下一场地域征伐战力+$aid；仍需依靠族将、装备和兵种，不保证必胜。",
            grantedMessage = "军师已完成阵图与粮道整备：下一场地域征伐战力+$aid，开战结算后消耗。",
            conquestTacticalAid = aid
        )
        pauseForModal()
    }

    fun resolveConquest() {
        val before = state
        val assessment = V3GameEngine.conquestAssessment(before)
        val enemyPower = before.conquestState?.enemyPower ?: 0
        state = V3GameEngine.resolveConquest(before)
        val result = state.pendingReports.firstOrNull().orEmpty()
        audio.playSfx(if (result.contains("得胜")) SfxKey.V3Success else SfxKey.V3Failure)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
        if (result.contains("失利") && pendingCrisisAd == null && state.finalEnding == null) {
            val aid = ((enemyPower - assessment.total) * 50 / 100).coerceIn(35, 120)
            pendingCrisisAd = V3CrisisAd(
                key = "conquest-recovery-${state.year}-${state.month}-${before.conquestState?.regionId.orEmpty()}",
                title = "败军复盘",
                subtitle = "此战失利。完整观看后，军师复盘阵图并联络乡绅筹措粮道，下一场地域征伐战力+$aid。",
                grantedMessage = "败军复盘完成：下一场地域征伐战力+$aid。请先补专业兵、修装备、培养族将，再择机出征。",
                conquestTacticalAid = aid
            )
            pauseForModal()
        }
        resumeAfterModalIfClear()
    }

    fun cancelConquest() {
        audio.click()
        state = V3GameEngine.cancelConquest(state)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
        resumeAfterModalIfClear()
    }

    fun proclaimUnification() {
        audio.playSfx(SfxKey.V3Finale)
        state = V3GameEngine.proclaimUnification(state)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun trainPerson(personId: Int, training: V3TrainingType) {
        audio.select()
        val before = state
        state = V3GameEngine.trainPerson(before, personId, training)
        message = state.pendingReports.firstOrNull()
        val person = before.people.firstOrNull { it.id == personId && it.alive }
        val costSilver = if ((person?.age ?: 12) < 12) 2 else 5
        val costGrain = if ((person?.age ?: 12) < 12) 1 else 2
        if (person != null && (before.silver < costSilver || before.grain < costGrain)) {
            offerResourceAid(
                keySuffix = "training-$personId-${training.name}",
                actionTitle = "塾师留课",
                actionDescription = "为${person.name}安排${V3GameEngine.trainingLabel(person, training)}只差束脩与口粮",
                silverMissing = (costSilver - before.silver).coerceAtLeast(0),
                grainMissing = (costGrain - before.grain).coerceAtLeast(0)
            )
        }
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
        // 科举落第时弹出"族中长辈点拨学业"援手
        if (result.contains("落第") && pendingCrisisAd == null && state.finalEnding == null) {
            val y = state.year
            val m = state.month
            pendingCrisisAd = V3CrisisAd(
                key = "crisis-exam-$y-$m",
                title = "长辈点拨",
                subtitle = "科场失意，族中致仕长辈愿开小灶指点学问。",
                grantedMessage = "长辈倾囊相授，赠银八十两、米六十石以资助下次赴考。",
                silver = 80,
                grain = 60
            )
            pauseForModal()
        }
    }

    fun cancelExam(reason: String = "科举已取消。") {
        state = state.copy(examSession = null)
        message = reason
        saveStore.save(state)
        resumeAfterModalIfClear()
    }

    fun recruitTroops(type: V3TroopType, amount: Int = 5) {
        audio.playSfx(SfxKey.V3Build)
        val before = state
        val count = amount.coerceIn(1, 20).coerceAtMost((999 - before.army.total()).coerceAtLeast(0))
        state = V3GameEngine.recruitTroops(before, type, amount)
        message = state.pendingReports.firstOrNull()
        val troopUnlocked =
            count > 0 &&
                V3GameEngine.isUnlocked(before, "Recruit") &&
                (type == V3TroopType.Militia || V3GameEngine.isUnlocked(before, "AdvancedTroops")) &&
                (type != V3TroopType.Cavalry || before.clanRank >= 4)
        if (troopUnlocked) {
            offerResourceAid(
                keySuffix = "recruit-${type.name}-$count",
                actionTitle = "募兵筹粮",
                actionDescription = "募${type.label}${count}名只差军饷与口粮",
                silverMissing = (type.silverCost * count - before.silver).coerceAtLeast(0),
                grainMissing = (type.grainCost * count - before.grain).coerceAtLeast(0)
            )
        }
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
        val hadBattle = state.battleState != null
        state = V3GameEngine.startBattle(state)
        if (!hadBattle && state.battleState != null) pauseForModal()
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
        settleTimelineEndingIfReady()
        val result = state.pendingReports.firstOrNull().orEmpty()
        audio.playSfx(if (result.contains("得胜")) SfxKey.V3Success else SfxKey.V3Failure)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
        // 战败时弹出"乡勇战后整补"援手
        if (result.contains("失利") && pendingCrisisAd == null && state.finalEnding == null) {
            val y = state.year
            val m = state.month
            pendingCrisisAd = V3CrisisAd(
                key = "crisis-battle-$y-$m",
                title = "乡勇整补",
                subtitle = "出师不利，武库损毁、乡勇带伤。城中铁匠与乡绅愿助一臂之力。",
                grantedMessage = "铁匠连夜修械，乡绅捐银劳军，武库耐久与银两皆得补充。",
                silver = 50,
                repairDurability = 35
            )
            pauseForModal()
        }
        resumeAfterModalIfClear()
    }

    fun resolveBattle() {
        state = V3GameEngine.resolveBattle(state)
        settleTimelineEndingIfReady()
        val result = state.pendingReports.firstOrNull().orEmpty()
        audio.playSfx(if (result.contains("得胜")) SfxKey.V3Success else SfxKey.V3Failure)
        message = if (state.battleState == null) state.pendingReports.firstOrNull() else null
        saveStore.save(state)
        resumeAfterModalIfClear()
    }

    fun cancelBattle() {
        audio.click()
        state = V3GameEngine.cancelBattle(state)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
        resumeAfterModalIfClear()
    }

    fun raiseBanner() {
        audio.playSfx(SfxKey.V3Finale)
        state = V3GameEngine.raiseBanner(state)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
    }

    fun succeedPatriarch(personId: Int) {
        audio.playSfx(SfxKey.V3Success)
        state = V3GameEngine.succeedPatriarch(state, personId)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
        resumeAfterModalIfClear()
    }

    fun chooseCard(cardId: String, choiceId: String) {
        audio.playSfx(SfxKey.V3Edict)
        val card = state.activeCards.firstOrNull { it.id == cardId }
        val requestedChoice = card?.choices?.firstOrNull { it.id == choiceId }
        if (requestedChoice != null && !V3CardEngine.meets(requestedChoice.require, state)) {
            offerPatriarchStatAid(cardId, requestedChoice.require)
            offerCardResourceAid(cardId, requestedChoice.id, requestedChoice.require)
            message = requestedChoice.require?.label() ?: "此项暂不可行"
            return
        }
        val resolution = V3CardEngine.choose(state, cardId, choiceId)
        if (resolution == null) {
            message = "此项家务尚不能处置，或本月议事名额已用尽。"
            return
        }
        state = resolution.state
        val visitorId = resolution.choice.effects.visitorId
        val remainingVisitorChapters = visitorId?.let { id ->
            V3Content.visitors
                .firstOrNull { visitor -> visitor.id == id }
                ?.chapters
                ?.size
                ?.minus(state.visitorProgress[id] ?: 0)
                ?.coerceAtLeast(0)
        }
        message = if (resolution.card.pool == V3CardPool.Visitor) {
            buildString {
                append(resolution.message)
                if (remainingVisitorChapters != null && remainingVisitorChapters > 0) {
                    append("\n\n这段来访已写入家乘。尚有")
                    append(remainingVisitorChapters)
                    append("章后续，将在家族阶段和来访条件满足后继续。")
                } else {
                    append("\n\n这位访客的故事已经收束，相关物品、关系与履历均已写入家业。")
                }
            }
        } else {
            resolution.message
        }
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
            val enemy = when (Math.floorMod(tile.q * 7 + tile.r * 11 + battle.turn, 3)) {
                0 -> com.arktools.daming.v3.data.V3HexArms.Spear
                1 -> com.arktools.daming.v3.data.V3HexArms.Archer
                else -> com.arktools.daming.v3.data.V3HexArms.Cavalry
            }
            val advantage = if (selected.counters(enemy)) 13 else if (enemy.counters(selected)) -13 else 0
            val baseLoss = (tile.enemyWave / 4 - advantage / 4).coerceIn(0, 12)
            val loss = if (tile.stable) baseLoss else (baseLoss + 3).coerceAtMost(12)
            tile.copy(arms = selected, garrison = (tile.garrison - loss).coerceAtLeast(0), breached = tile.garrison - loss <= 0, stable = tile.garrison - loss > 0)
        }
        val nextTurn = battle.turn + 1
        val supplyAfter = (battle.supply - 8 - nextTiles.count { it.breached } * 3).coerceAtLeast(0)
        val momentumAfter = (battle.enemyMomentum + nextTiles.count { it.breached } * 8 + if (supplyAfter == 0) 15 else -4).coerceIn(0, 100)
        val supplyFailure = supplyAfter == 0
        val momentumFailure = momentumAfter >= 100
        val victory = nextTiles.none { it.breached } && nextTurn > battle.maxTurns && !supplyFailure && !momentumFailure
        val finished = victory || nextTiles.any { it.breached } || nextTurn > battle.maxTurns || supplyFailure || momentumFailure
        state = state.copy(
            hexBattleState = battle.copy(
                turn = nextTurn,
                tiles = nextTiles,
                supply = supplyAfter,
                enemyMomentum = momentumAfter,
                selectedArms = emptyMap(),
                log = (battle.log + "第${battle.turn}轮守庄结算，${nextTiles.count { it.breached }}处庄门失守。").takeLast(20),
                finished = finished,
                victory = victory
            )
        )
        saveStore.save(state)
    }

    private fun hexBattleInitialState(): com.arktools.daming.v3.data.V3HexBattleState {
        val initial = com.arktools.daming.v3.data.V3HexBattleState.initial()
        val originBonus = if (state.originTraits.any { it.startsWith("边堡军籍") }) 3 else 0
        val letterBonus = if ("military_letter" in state.inventory) 2 else 0
        val moraleBonus = ((state.garrisonMorale - 60) / 10).coerceIn(-3, 4)
        val totalBonus = originBonus + letterBonus + moraleBonus
        return initial.copy(
            tiles = initial.tiles.map { tile ->
                tile.copy(garrison = (tile.garrison + totalBonus).coerceAtLeast(8))
            },
            log = listOf(
                "终章守庄整备：六处庄门依照出身、军书与守望士气配置驻守。"
            )
        )
    }

    fun startHexBattle() {
        if (state.year < 1643) {
            message = "六门守庄只在甲申前夕的最终守庄玩法中开启。"
            return
        }
        if (state.hexBattleCompleted) {
            message = "六门守庄已经结算过，本局不会重复开启。"
            return
        }
        if (V3GameEngine.hasBlockingEncounter(state)) {
            message = "当前已有待处理的战事、考试或终局事务，六门守庄不会与其他玩法重叠。"
            return
        }
        state = state.copy(
            hexBattleState = hexBattleInitialState()
        )
        pauseForModal()
        saveStore.save(state)
    }

    fun closeHexBattle() {
        val battle = state.hexBattleState ?: return
        if (!battle.finished) {
            message = "守庄战尚未结束，请先结算当前轮次。"
            return
        }
        state = if (battle.victory) {
            state.copy(hexBattleState = null, hexBattleCompleted = true, garrisonMorale = (state.garrisonMorale + 8).coerceIn(0, 100), influence = (state.influence + 6).coerceIn(0, 100), pendingReports = listOf("六处庄门守住，族谱记下这一夜。"))
        } else {
            state.copy(hexBattleState = null, hexBattleCompleted = true, garrisonMorale = (state.garrisonMorale - 10).coerceIn(0, 100), cohesion = (state.cohesion - 8).coerceIn(0, 100), pendingReports = listOf("庄门有失，族内需要重新整顿。"))
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
        // 每月结算后检测族人患病等月报未覆盖的危机，主动弹出援手
        maybeTriggerMonthlyCrisisAd()
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

    fun answerMiniGame(answerIndex: Int) {
        audio.playSfx(SfxKey.V3Success)
        state = V3EventEngine.answerMiniGame(state, answerIndex)
        message = state.pendingReports.firstOrNull()
        saveStore.save(state)
        if (message == null) resumeAfterModalIfClear()
    }

    fun chooseEvent(choice: V3EventChoice) {
        audio.playSfx(SfxKey.V3Edict)
        pauseForModal()
        val eventTitle = state.activeEvent?.title.orEmpty()
        val resolved = V3EventEngine.choose(state, choice)
        val hostileBattleTarget = if (eventTitle.endsWith("敌对来书") && choice.label == "暂不回应") {
            when {
                eventTitle.startsWith("县衙") -> "官差围庄"
                eventTitle.startsWith("山贼") -> "山贼来袭"
                eventTitle.startsWith("军镇") -> "军镇问罪"
                else -> "敌对势力来袭"
            }
        } else null
        val resolvedWithBattle = if (hostileBattleTarget != null) {
            V3GameEngine.startBattle(resolved, hostileBattleTarget)
        } else {
            resolved
        }
        state = if (
            V3GameEngine.isTimelineEnding(resolvedWithBattle) &&
                "final_eve" in resolvedWithBattle.seenChapterMilestones &&
                resolvedWithBattle.battleState == null &&
                resolvedWithBattle.hexBattleState == null &&
                resolvedWithBattle.conquestState == null
        ) {
            resolvedWithBattle.copy(
                finalEnding = V3GameEngine.finalizeEnding(resolvedWithBattle),
                activeEvent = null
            )
        } else {
            resolvedWithBattle
        }
        completeTutorialAction(18)
        message = if (
            state.finalEnding == null &&
            state.tutorialCompleted &&
            state.miniGameSession == null
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
        showInfo(description)
    }

    private fun settleTimelineEndingIfReady() {
        if (
            state.finalEnding == null &&
            state.battleState == null &&
            state.hexBattleState == null &&
            state.conquestState == null &&
            V3GameEngine.isTimelineEnding(state) &&
            "final_eve" in state.seenChapterMilestones
        ) {
            state = state.copy(
                finalEnding = V3GameEngine.finalizeEnding(state),
                activeEvent = null
            )
        }
    }

    private fun offerAutomationQuota(action: String, label: String) {
        if (pendingCrisisAd != null || state.finalEnding != null) return
        val batch = automationQuotaStore.nextBatch(action)
        pendingCrisisAd = V3CrisisAd(
            key = "automation-$action-${automationQuotaStore.dayToken()}-$batch",
            title = "${label}次数已用完",
            subtitle = "今日5次免费额度已用完。仅在你再次需要时出现：完整观看一次，立即追加5次本功能额度。",
            grantedMessage = "${label}已追加5次今日额度。",
            automationQuotaAction = action
        )
        message = null
        pauseForModal()
    }

    private fun offerCardResourceAid(cardId: String, choiceId: String, require: V3CardRequire?) {
        if (pendingCrisisAd != null || state.finalEnding != null || require == null) return
        val silverMissing = ((require.minSilver ?: 0) - state.silver).coerceAtLeast(0)
        val grainMissing = ((require.minGrain ?: 0) - state.grain).coerceAtLeast(0)
        val cohesionMissing = ((require.minCohesion ?: 0) - state.cohesion).coerceAtLeast(0)
        if (silverMissing <= 0 && grainMissing <= 0 && cohesionMissing <= 0) return
        val rewardParts = buildList {
            if (silverMissing > 0) add("银${silverMissing}两")
            if (grainMissing > 0) add("粮${grainMissing}石")
            if (cohesionMissing > 0) add("凝聚+$cohesionMissing")
        }
        pendingCrisisAd = V3CrisisAd(
            key = "crisis-card-resource-${state.year}-${state.month}-$cardId-$choiceId",
            title = "亲友周转",
            subtitle = "此项家务只差${rewardParts.joinToString("、")}。完整观看后可补足本次缺口；领取后请再次选择此项。",
            grantedMessage = "亲友援手已到：${rewardParts.joinToString("、")}已补足，可重新处置刚才的家务。",
            silver = silverMissing,
            grain = grainMissing,
            cohesion = cohesionMissing
        )
        message = null
        pauseForModal()
    }

    private fun offerPatriarchStatAid(cardId: String, require: V3CardRequire?) {
        if (pendingCrisisAd != null || state.finalEnding != null) return
        val stat = require?.minPatriarchStat ?: return
        val target = require.minPatriarchStatValue ?: return
        val current = when (stat) {
            "conduct" -> state.patriarch.conduct
            "stewardship" -> state.patriarch.stewardship
            "prestige" -> state.patriarch.prestige
            "health" -> state.patriarch.health
            else -> return
        }
        val gain = (target - current).coerceIn(1, 10)
        val label = when (stat) {
            "conduct" -> "处世（品行）"
            "stewardship" -> "经营（治家）"
            "prestige" -> "威望"
            "health" -> "身板"
            else -> return
        }
        val title = when (stat) {
            "conduct" -> "族老训诫"
            "stewardship" -> "账房留卷"
            "prestige" -> "乡绅举荐"
            else -> "名医调理"
        }
        pendingCrisisAd = V3CrisisAd(
            key = "crisis-card-stat-${state.year}-${state.month}-$cardId-$stat-$current",
            title = title,
            subtitle = "此事要求家主$label≥$target，当前$current。完整观看后可获得$label +$gain；领取后请再次选择此项。",
            grantedMessage = "${title}已成：家主$label +$gain，现为${current + gain}，可重新处置刚才的家务。",
            patriarchConduct = if (stat == "conduct") gain else 0,
            patriarchStewardship = if (stat == "stewardship") gain else 0,
            patriarchPrestige = if (stat == "prestige") gain else 0,
            patriarchHealth = if (stat == "health") gain else 0
        )
        message = null
        pauseForModal()
    }

    private fun offerResourceAid(
        keySuffix: String,
        actionTitle: String,
        actionDescription: String,
        silverMissing: Int,
        grainMissing: Int
    ) {
        if (pendingCrisisAd != null || state.finalEnding != null) return
        if (silverMissing <= 0 && grainMissing <= 0) return
        val aidParts = buildList {
            if (silverMissing > 0) add("银$silverMissing 两")
            if (grainMissing > 0) add("粮$grainMissing 石")
        }
        pendingCrisisAd = V3CrisisAd(
            key = "crisis-action-${state.year}-${state.month}-$keySuffix",
            title = actionTitle,
            subtitle = "$actionDescription。完整观看后可获得本次缺口所需的${aidParts.joinToString("、")}；领取后请再次执行该操作。",
            grantedMessage = "族中亲友送来周转物资：${aidParts.joinToString("、")}已入账，可重新执行刚才的操作。",
            silver = silverMissing,
            grain = grainMissing
        )
        message = null
        pauseForModal()
    }

    /** 关闭危机弹窗（点击"暂不需要"）。 */
    fun dismissCrisisAd() {
        audio.click()
        pendingCrisisAd = null
        resumeAfterModalIfClear()
    }

    /** 观看广告后发放危机援手奖励，随后关闭弹窗。 */
    fun grantCrisisAd() {
        val ad = pendingCrisisAd ?: return
        ad.automationQuotaAction?.let { automationQuotaStore.grantFive(it) }
        val repairedEquipment = if (ad.repairDurability > 0) {
            state.equipment.map { item ->
                item.copy(durability = (item.durability + ad.repairDurability).coerceAtMost(item.maxDurability))
            }
        } else {
            state.equipment
        }
        val curedPeople = if (ad.cureIllness) {
            val sick = state.people.firstOrNull { it.alive && it.illness != null }
            if (sick != null) {
                state.people.map {
                    if (it.id == sick.id) it.copy(illness = null, illnessMonths = 0, fatigue = (it.fatigue - 15).coerceAtLeast(0))
                    else it
                }
            } else {
                state.people
            }
        } else {
            state.people
        }
        state = state.copy(
            silver = if (ad.settleDeficit) state.silver.coerceAtLeast(0) + ad.silver else (state.silver + ad.silver).coerceIn(-999, 999_999),
            grain = if (ad.settleDeficit) state.grain.coerceAtLeast(0) + ad.grain else (state.grain + ad.grain).coerceIn(-999, 999_999),
            cohesion = (state.cohesion + ad.cohesion).coerceIn(0, 100),
            equipment = repairedEquipment,
            people = curedPeople,
            patriarch = state.patriarch.copy(
                conduct = (state.patriarch.conduct + ad.patriarchConduct).coerceIn(0, 100),
                stewardship = (state.patriarch.stewardship + ad.patriarchStewardship).coerceIn(0, 100),
                prestige = (state.patriarch.prestige + ad.patriarchPrestige).coerceIn(0, 100),
                health = (state.patriarch.health + ad.patriarchHealth).coerceIn(0, 100)
            ),
            consecutiveDeficitMonths = if (ad.settleDeficit) 0 else state.consecutiveDeficitMonths,
            clinicAutoTreatmentMonths = maxOf(state.clinicAutoTreatmentMonths, ad.clinicAutoTreatmentMonths),
            conquestTacticalAid = maxOf(state.conquestTacticalAid, ad.conquestTacticalAid)
        )
        saveStore.save(state)
        pendingCrisisAd = null
        showInfo(ad.grantedMessage)
    }

    /**
     * 每月结算后检查必须由当前困境触发的援助，不提供常驻广告入口。
     * 优先处理负债平账，其次处理患病。
     */
    private fun maybeTriggerMonthlyCrisisAd() {
        if (pendingCrisisAd != null) return
        if (state.finalEnding != null) return
        val y = state.year
        val m = state.month
        if ((state.silver < 0 || state.grain < 0) && state.consecutiveDeficitMonths in 1..11) {
            pendingCrisisAd = V3CrisisAd(
                key = "crisis-deficit-$y-$m",
                title = "家计告急",
                subtitle = "银粮已出现亏空，继续拖欠将累计破产月份。完整观看后由乡绅与义仓出面平账，并另补银粮各100。",
                grantedMessage = "乡绅与义仓协力平账：负银负粮已归零，并补银100两、粮100石。连续亏空月份已清零。",
                silver = 100,
                grain = 100,
                settleDeficit = true
            )
            pauseForModal()
            return
        }
        val absoluteMonth = state.year * 12 + state.month
        if (
            state.patriarch.health <= 20 &&
            state.regencyHeirId == null &&
            state.patriarchCriticalWarningMonth != absoluteMonth
        ) {
            state = state.copy(patriarchCriticalWarningMonth = absoluteMonth)
            saveStore.save(state)
            message = "族长身板仅余${state.patriarch.health}，已至病危。请前往【县域地图 → 仁心医馆】，安排一名成年族人（女性亦可）担任医师，或付银立即治疗；进入医馆后也可自愿观看广告，解锁连续24个月自动治疗。"
            pauseForModal()
            return
        }
        val sick = state.people.any { it.alive && it.illness != null }
        if (!sick) return
        pendingCrisisAd = V3CrisisAd(
            key = "crisis-ill-$y-$m",
            title = "医者上门",
            subtitle = "族中有人缠绵病榻，乡间游医愿施针赠药。",
            grantedMessage = "医者施治，族人病情已愈，另赠口粮百斤以养元气。",
            grain = 100,
            cureIllness = true
        )
        pauseForModal()
    }

    private fun pauseForModal() {
        if (timeSpeed > 0 && resumeSpeedAfterModal == null) resumeSpeedAfterModal = timeSpeed
        timeSpeed = 0
    }

    private fun resumeAfterModalIfClear() {
        val blocked = latestReport != null || message != null || pendingCrisisAd != null || settingsVisible || state.activeEvent != null ||
            state.examSession != null || state.miniGameSession != null || state.battleState != null || state.hexBattleState != null ||
            state.conquestState != null || state.pendingSuccession || state.activeCards.isNotEmpty() || state.pendingDice != null ||
            state.finalEnding != null
        if (blocked) return
        resumeSpeedAfterModal?.let { speed ->
            timeSpeed = speed
            lastActiveSpeed = speed
        }
        resumeSpeedAfterModal = null
    }

    private fun shouldGenerateEventThisMonth(nextState: V3GameState): Boolean {
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

    fun genealogyPreface(): String = V3GameEngine.genealogyPreface(state)

    fun endingChronicle(): List<String> = V3GameEngine.endingChronicle(state)

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
