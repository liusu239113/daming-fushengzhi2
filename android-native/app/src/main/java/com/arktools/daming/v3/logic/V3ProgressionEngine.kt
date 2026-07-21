package com.arktools.daming.v3.logic

import com.arktools.daming.v3.data.V3CountySiteType
import com.arktools.daming.v3.data.V3GameState
import com.arktools.daming.v3.data.V3RegionStatus
import com.arktools.daming.v3.data.V3Route
import com.arktools.daming.v3.data.V3Screen

enum class V3Chapter(
    val number: Int,
    val title: String,
    val theme: String,
    val unlockPreview: String
) {
    Founding(1, "立户求生", "先让一人之家拥有婚姻、收入与下一代。", "宗族议事、县衙、书院、医馆与基础军务"),
    Rooting(2, "小族扎根", "稳定银粮、扩充家口，在县中建立可靠根基。", "天下经营、高级产业、精兵与跨域扩张"),
    Expansion(3, "望族扩张", "把县内家业转化为跨县影响力与武备。", "县中大姓、举旗资格与更大地域容量"),
    CountyRivalry(4, "县中争锋", "整合数个地域，在乱世来临前形成一方势力。", "郡望世家与完整终局路线"),
    ChoosingPath(5, "乱世择路", "让长期经营汇成明确路线，为甲申终局作准备。", "终局章与路线决断"),
    FinalLegacy(6, "终局立业", "在天下崩解前完成家族最后的选择。", "路线结局或统一天下")
}

enum class V3QuestCategory(val label: String) {
    Main("主线"),
    Chapter("章节"),
    Annual("年务"),
    Urgent("急务")
}

enum class V3ActionPriority(val rank: Int, val label: String) {
    Critical(0, "紧急"),
    MainQuest(1, "主线"),
    AnnualGoal(2, "年务"),
    Efficiency(3, "经营")
}

data class V3QuestCondition(
    val label: String,
    val current: Int,
    val target: Int,
    val satisfied: Boolean = current >= target
) {
    val progressText: String
        get() = if (satisfied) "已完成" else "$current/$target"
}

data class V3QuestCard(
    val id: String,
    val category: V3QuestCategory,
    val title: String,
    val description: String,
    val conditions: List<V3QuestCondition>,
    val rewardText: String,
    val actionLabel: String,
    val destination: V3Screen,
    val blockers: List<String> = emptyList()
) {
    val completedCount: Int
        get() = conditions.count { it.satisfied }

    val totalCount: Int
        get() = conditions.size.coerceAtLeast(1)

    val completed: Boolean
        get() = conditions.isNotEmpty() && conditions.all { it.satisfied }
}

data class V3RecommendedAction(
    val id: String,
    val priority: V3ActionPriority,
    val title: String,
    val reason: String,
    val actionLabel: String,
    val destination: V3Screen,
    val expectedImpact: String,
    val canExecute: Boolean = true,
    val blockers: List<String> = emptyList()
)

data class V3ChapterReward(
    val chapter: V3Chapter,
    val silver: Int = 0,
    val grain: Int = 0,
    val influence: Int = 0,
    val cohesion: Int = 0,
    val militia: Int = 0
) {
    val text: String
        get() = listOfNotNull(
            silver.takeIf { it > 0 }?.let { "银+$it" },
            grain.takeIf { it > 0 }?.let { "粮+$it" },
            influence.takeIf { it > 0 }?.let { "族望+$it" },
            cohesion.takeIf { it > 0 }?.let { "凝聚+$it" },
            militia.takeIf { it > 0 }?.let { "乡勇+$it" }
        ).joinToString(" · ")
}

data class V3ProgressionSnapshot(
    val chapter: V3Chapter,
    val chapterProgress: Int,
    val mainQuest: V3QuestCard,
    val sideQuests: List<V3QuestCard>,
    val recommendedActions: List<V3RecommendedAction>,
    val claimableReward: V3ChapterReward?,
    val nextUnlock: String
) {
    val primaryAction: V3RecommendedAction
        get() = recommendedActions.first()
}

object V3ProgressionEngine {
    private val rewards = mapOf(
        V3Chapter.Founding to V3ChapterReward(V3Chapter.Founding, silver = 40, grain = 60, cohesion = 4),
        V3Chapter.Rooting to V3ChapterReward(V3Chapter.Rooting, silver = 80, grain = 100, influence = 5, cohesion = 3),
        V3Chapter.Expansion to V3ChapterReward(V3Chapter.Expansion, silver = 140, grain = 160, influence = 7, militia = 15),
        V3Chapter.CountyRivalry to V3ChapterReward(V3Chapter.CountyRivalry, silver = 220, grain = 240, influence = 8, militia = 25),
        V3Chapter.ChoosingPath to V3ChapterReward(V3Chapter.ChoosingPath, silver = 300, grain = 300, influence = 8, cohesion = 6)
    )

    fun snapshot(state: V3GameState): V3ProgressionSnapshot {
        val chapter = currentChapter(state)
        val mainQuest = mainQuest(state, chapter)
        val actions = recommendedActions(state, chapter, mainQuest)
        return V3ProgressionSnapshot(
            chapter = chapter,
            chapterProgress = if (mainQuest.conditions.isEmpty()) 100 else (mainQuest.completedCount * 100 / mainQuest.totalCount),
            mainQuest = mainQuest,
            sideQuests = annualQuestCards(state),
            recommendedActions = actions,
            claimableReward = claimableReward(state),
            nextUnlock = chapter.unlockPreview
        )
    }

    fun currentChapter(state: V3GameState): V3Chapter {
        val dominantRoute = V3GameEngine.dominantRoute(state)
        return when {
            state.finalEnding != null ||
                state.year >= 1642 ||
                (
                    dominantRoute == V3Route.Warlord &&
                        state.unificationProgress >= 70 &&
                        routeQuest(state).completed
                    ) ->
                V3Chapter.FinalLegacy

            state.clanRank >= 5 ||
                (state.clanRank >= 4 && (state.year >= 1636 || (state.routeScores.values.maxOrNull() ?: 0) >= 60)) ->
                V3Chapter.ChoosingPath

            state.clanRank >= 4 -> V3Chapter.CountyRivalry
            state.clanRank >= 3 -> V3Chapter.Expansion
            state.clanRank >= 2 -> V3Chapter.Rooting
            else -> V3Chapter.Founding
        }
    }

    fun chapterCompleted(state: V3GameState, chapter: V3Chapter): Boolean = when (chapter) {
        V3Chapter.Founding -> state.clanRank >= 2
        V3Chapter.Rooting -> state.clanRank >= 3
        V3Chapter.Expansion -> state.clanRank >= 4
        V3Chapter.CountyRivalry -> state.clanRank >= 5
        V3Chapter.ChoosingPath -> routeQuest(state).completed
        V3Chapter.FinalLegacy -> state.finalEnding != null
    }

    fun claimableReward(state: V3GameState): V3ChapterReward? =
        rewards.values
            .sortedBy { it.chapter.number }
            .firstOrNull {
                chapterCompleted(state, it.chapter) &&
                    it.chapter.name !in state.claimedChapterRewards
            }

    fun claimChapterReward(state: V3GameState, chapter: V3Chapter): V3GameState {
        val reward = rewards[chapter] ?: return state.copy(pendingReports = listOf("终局章节没有额外领取奖励。"))
        if (!chapterCompleted(state, chapter)) {
            return state.copy(pendingReports = listOf("【${chapter.title}】尚未完成，暂不能领取章节奖励。"))
        }
        if (chapter.name in state.claimedChapterRewards) {
            return state.copy(pendingReports = listOf("【${chapter.title}】章节奖励已经领取。"))
        }
        val nextArmy = state.army.add(com.arktools.daming.v3.data.V3TroopType.Militia, reward.militia)
        val message = "完成第${chapter.number}章【${chapter.title}】，领取章节奖励：${reward.text}。"
        return state.copy(
            silver = (state.silver + reward.silver).coerceAtMost(999_999),
            grain = (state.grain + reward.grain).coerceAtMost(999_999),
            influence = (state.influence + reward.influence).coerceIn(0, 100),
            cohesion = (state.cohesion + reward.cohesion).coerceIn(0, 100),
            militia = nextArmy.total(),
            army = nextArmy,
            claimedChapterRewards = state.claimedChapterRewards + chapter.name,
            pendingReports = listOf(message),
            eventLog = (listOf("${state.year}年${state.month}月 · $message") + state.eventLog).take(100)
        )
    }

    fun eventChoiceContext(state: V3GameState, choice: com.arktools.daming.v3.data.V3EventChoice): String {
        val snapshot = snapshot(state)
        val quest = snapshot.mainQuest
        val helpsMainQuest = when {
            quest.conditions.any { it.label == "银两" && !it.satisfied } && choice.silverDelta > 0 -> "有利于主线银两缺口"
            quest.conditions.any { it.label == "粮食" && !it.satisfied } && choice.grainDelta > 0 -> "有利于主线粮食缺口"
            quest.conditions.any { it.label == "族望" && !it.satisfied } && choice.influenceDelta > 0 -> "有利于主线族望缺口"
            quest.conditions.any { it.label.contains("路线") && !it.satisfied } && choice.route == V3GameEngine.dominantRoute(state) && choice.routeDelta > 0 -> "推进当前主路线"
            quest.conditions.any { it.label.contains("控制") && !it.satisfied } && choice.siteControlDelta > 0 -> "有利于控制目标"
            else -> "对当前主线无直接推进"
        }
        val warnings = buildList {
            if (state.grain < 120 && choice.grainDelta < 0) add("粮仓偏低仍将消耗${-choice.grainDelta}")
            if (state.silver < 80 && choice.silverDelta < 0) add("银库偏低仍将消耗${-choice.silverDelta}")
            if (choice.cohesionDelta < 0) add("凝聚${choice.cohesionDelta}")
            if (choice.siteRiskDelta > 0) add("地点风险+${choice.siteRiskDelta}")
        }
        return if (warnings.isEmpty()) {
            "主线判断：$helpsMainQuest"
        } else {
            "主线判断：$helpsMainQuest；风险：${warnings.joinToString("、")}"
        }
    }

    private fun mainQuest(state: V3GameState, chapter: V3Chapter): V3QuestCard = when (chapter) {
        V3Chapter.Founding -> rankQuest(
            state,
            chapter,
            "成家立业，晋升小族",
            "完成婚育、产业与首阶段积累，让一人之家真正成为宗族。"
        )

        V3Chapter.Rooting -> rankQuest(
            state,
            chapter,
            "稳住县业，晋升望族",
            "扩大第二代、产业和储备，并通过议事与县域经营建立小族根基。"
        )

        V3Chapter.Expansion -> rankQuest(
            state,
            chapter,
            "跨出本县，晋升县中大姓",
            "接触并控制第一个县外地域，把家业、人才和兵册转化为地方影响。"
        )

        V3Chapter.CountyRivalry -> rankQuest(
            state,
            chapter,
            "整合一方，晋升郡望世家",
            "控制多个战略地域，扩大家口与产业，形成能参与乱世角逐的一方势力。"
        )

        V3Chapter.ChoosingPath -> routeQuest(state)
        V3Chapter.FinalLegacy -> finalQuest(state)
    }

    private fun rankQuest(
        state: V3GameState,
        chapter: V3Chapter,
        title: String,
        description: String
    ): V3QuestCard {
        val cost = V3GameEngine.nextRankCost(state)
        if (cost == null) return routeQuest(state)
        val elapsedMonths = elapsedMonths(state)
        val secondGeneration = state.people.count { it.alive && it.generation >= 2 }
        val externalRegions = externalControlledRegions(state)
        val stageCondition = when (state.clanRank) {
            1 -> V3QuestCondition("经营满8个月", elapsedMonths, 8)
            2 -> V3QuestCondition("经营满42个月", elapsedMonths, 42)
            3 -> V3QuestCondition("控制县外地域", externalRegions, 1)
            4 -> V3QuestCondition("控制战略地域", V3GameEngine.controlledRegionCount(state), 4)
            else -> V3QuestCondition("阶段历练", 1, 1)
        }
        val generationCondition = when (state.clanRank) {
            1 -> V3QuestCondition("第二代子嗣", secondGeneration, 1)
            2 -> V3QuestCondition("第二代子嗣", secondGeneration, 2)
            else -> null
        }
        val conditions = listOfNotNull(
            stageCondition,
            generationCondition,
            V3QuestCondition("银两", state.silver, cost.silver),
            V3QuestCondition("粮食", state.grain, cost.grain),
            V3QuestCondition("人口", V3GameEngine.alivePeople(state).size, cost.population),
            V3QuestCondition("产业", V3GameEngine.builtSiteCount(state), cost.builtSites),
            V3QuestCondition("族望", state.influence, cost.influence)
        )
        return V3QuestCard(
            id = "rank_${state.clanRank + 1}",
            category = V3QuestCategory.Main,
            title = title,
            description = description,
            conditions = conditions,
            rewardText = rewards[chapter]?.text.orEmpty(),
            actionLabel = if (V3GameEngine.canRankUp(state)) "前往宗族晋升" else nextRankActionLabel(state),
            destination = if (V3GameEngine.canRankUp(state) || needsMarriage(state)) V3Screen.Clan else nextRankDestination(state),
            blockers = conditions.filterNot { it.satisfied }.map { "${it.label}还差${(it.target - it.current).coerceAtLeast(0)}" }
        )
    }

    private fun routeQuest(state: V3GameState): V3QuestCard {
        val route = V3GameEngine.dominantRoute(state)
        val routeScore = state.routeScores[route] ?: 0
        val conditions = when (route) {
            V3Route.Scholar -> listOf(
                V3QuestCondition("耕读路线", routeScore, 80),
                V3QuestCondition("高学识族人", state.people.count { it.alive && it.study >= 70 }, 2),
                V3QuestCondition("士绅关系", state.relations.gentry, 50),
                V3QuestCondition("族望", state.influence, 85)
            )

            V3Route.Merchant -> listOf(
                V3QuestCondition("富商路线", routeScore, 80),
                V3QuestCondition("银两", state.silver, 1800),
                V3QuestCondition("商帮关系", state.relations.merchants, 60),
                V3QuestCondition("家产等级", V3GameEngine.estateLevelTotal(state), 12)
            )

            V3Route.Fortress -> listOf(
                V3QuestCondition("自保路线", routeScore, 80),
                V3QuestCondition("乡勇", state.militia, 160),
                V3QuestCondition("安定地点", state.sites.count { it.risk < 30 }, 6),
                V3QuestCondition("粮食", state.grain, 1200)
            )

            V3Route.Loyalist -> listOf(
                V3QuestCondition("勤王路线", routeScore, 80),
                V3QuestCondition("官府关系", state.relations.yamen, 60),
                V3QuestCondition("军镇关系", state.relations.garrison, 50),
                V3QuestCondition("族望", state.influence, 85)
            )

            V3Route.Warlord -> listOf(
                V3QuestCondition("割据路线", routeScore, 80),
                V3QuestCondition("控制地域", V3GameEngine.controlledRegionCount(state), 6),
                V3QuestCondition("乡勇", state.militia, 220),
                V3QuestCondition("统一进度", state.unificationProgress, 60)
            )

            V3Route.Overseas -> listOf(
                V3QuestCondition("海外路线", routeScore, 80),
                V3QuestCondition("银两", state.silver, 1800),
                V3QuestCondition("商帮关系", state.relations.merchants, 60),
                V3QuestCondition("码头等级", state.sites.firstOrNull { it.type == V3CountySiteType.Dock }?.level ?: 0, 2)
            )

            V3Route.Hermit -> listOf(
                V3QuestCondition("避祸路线", routeScore, 80),
                V3QuestCondition("宗族凝聚", state.cohesion, 80),
                V3QuestCondition("粮食", state.grain, 1200),
                V3QuestCondition("安定地点", state.sites.count { it.risk < 30 }, 6)
            )
        }
        return V3QuestCard(
            id = "route_${route.name.lowercase()}",
            category = V3QuestCategory.Main,
            title = "定下${route.label}之路",
            description = routeObjective(route),
            conditions = conditions,
            rewardText = rewards[V3Chapter.ChoosingPath]?.text.orEmpty(),
            actionLabel = routeActionLabel(route, conditions),
            destination = routeDestination(route, conditions),
            blockers = conditions.filterNot { it.satisfied }.map { "${it.label}还差${(it.target - it.current).coerceAtLeast(0)}" }
        )
    }

    private fun finalQuest(state: V3GameState): V3QuestCard {
        val controlled = V3GameEngine.controlledRegionCount(state)
        val route = V3GameEngine.dominantRoute(state)
        val finalActMonths = (((state.year - 1642) * 12) + state.month - 1).coerceIn(0, 28)
        val monthsToEnding = (28 - finalActMonths).coerceAtLeast(0)
        val routePreparation = routeQuest(state)
        val realmControlled = state.worldRegions.any {
            it.id == "all_realm" &&
                it.status in setOf(V3RegionStatus.Controlled, V3RegionStatus.Pacified)
        }
        val nationalPower = state.unificationProgress +
            state.militia / 6 +
            state.influence / 2 +
            V3GameEngine.estateLevelTotal(state) * 2 +
            V3GameEngine.alivePeople(state).size * 2
        val conditions = if (route == V3Route.Warlord) {
            listOf(
                V3QuestCondition("路线准备", routePreparation.completedCount, routePreparation.totalCount),
                V3QuestCondition("控制地域", controlled, 8),
                V3QuestCondition("统一进度", state.unificationProgress, 100),
                V3QuestCondition("天下节点", if (realmControlled) 1 else 0, 1),
                V3QuestCondition("综合国力", nationalPower, 150)
            )
        } else {
            listOf(
                V3QuestCondition("路线准备", routePreparation.completedCount, routePreparation.totalCount),
                V3QuestCondition("甲申前夜决断", if ("final_eve" in state.seenChapterMilestones) 1 else 0, 1),
                V3QuestCondition("终章历程（月）", finalActMonths, 28)
            )
        }
        return V3QuestCard(
            id = "final_${route.name.lowercase()}",
            category = V3QuestCategory.Main,
            title = if (route == V3Route.Warlord) "定鼎天下" else "写下${route.label}终局",
            description = if (route == V3Route.Warlord) {
                "控制天下节点和八个战略地域，以综合国力完成统一。"
            } else {
                "守住家业并完成甲申前夜与国变抉择。距离1644年5月终局还剩${monthsToEnding}个月，其间仍可补足路线与凝聚。"
            },
            conditions = conditions,
            rewardText = "解锁专属终局家乘",
            actionLabel = if (route == V3Route.Warlord) "前往天下" else "回到家业推进月结（余${monthsToEnding}月）",
            destination = if (route == V3Route.Warlord) V3Screen.Strategy else V3Screen.County,
            blockers = conditions.filterNot { it.satisfied }.map { "${it.label}还差${(it.target - it.current).coerceAtLeast(0)}" }
        )
    }

    private fun annualQuestCards(state: V3GameState): List<V3QuestCard> = state.annualGoals.take(3).map { goal ->
        val progress = V3GameEngine.goalProgress(state, goal)
        V3QuestCard(
            id = goal.id,
            category = V3QuestCategory.Annual,
            title = goal.title,
            description = goal.desc,
            conditions = listOf(V3QuestCondition(goal.metric.label, progress, goal.target, goal.completed || progress >= goal.target)),
            rewardText = annualRewardText(goal),
            actionLabel = annualActionLabel(goal.metric),
            destination = annualDestination(goal.metric),
            blockers = if (progress >= goal.target || goal.completed) emptyList() else listOf("${goal.metric.label}还差${goal.target - progress}")
        )
    }

    private fun recommendedActions(
        state: V3GameState,
        chapter: V3Chapter,
        mainQuest: V3QuestCard
    ): List<V3RecommendedAction> {
        val actions = mutableListOf<V3RecommendedAction>()
        val forecast = V3GameEngine.monthlyForecast(state)
        if (state.grain < 80 || state.grain + forecast.grainIncome - forecast.grainExpense < 40) {
            actions += V3RecommendedAction(
                "grain_crisis",
                V3ActionPriority.Critical,
                "先保粮仓",
                "预计下月粮食仅余${state.grain + forecast.grainIncome - forecast.grainExpense}，继续扩人口或募兵会加速断粮。",
                "处理田庄与粮仓",
                V3Screen.County,
                "安排管田、升级田庄或扩建粮仓"
            )
        }
        if (state.silver < 45 || state.silver + forecast.silverIncome - forecast.silverExpense < 20) {
            actions += V3RecommendedAction(
                "silver_crisis",
                V3ActionPriority.Critical,
                "补足现银",
                "银库不足会阻断婚配、营建、议事与军务。",
                "处理集市与铺面",
                V3Screen.County,
                "安排行商、升级集市或营建铺面"
            )
        }
        val danger = state.sites.maxByOrNull { it.risk }
        if (danger != null && danger.risk >= 55) {
            actions += V3RecommendedAction(
                "danger_${danger.id}",
                V3ActionPriority.Critical,
                "治理${danger.name}",
                "风险${danger.risk}已进入险地区间，会压低产出并提高坏事件概率。",
                "前往县域治理",
                V3Screen.County,
                "派人治理、筑寨或执行专属事务"
            )
        }
        if (actions.none { it.priority == V3ActionPriority.Critical }) {
            actions += mainQuestAction(state, chapter, mainQuest)
        }
        state.annualGoals
            .filterNot { it.completed || V3GameEngine.goalProgress(state, it) >= it.target }
            .minByOrNull { goal -> (goal.target - V3GameEngine.goalProgress(state, goal)).coerceAtLeast(0) }
            ?.let { goal ->
                val progress = V3GameEngine.goalProgress(state, goal)
                actions += V3RecommendedAction(
                    "annual_${goal.id}",
                    V3ActionPriority.AnnualGoal,
                    "年务：${goal.title}",
                    "当前${progress}/${goal.target}，完成可得${annualRewardText(goal)}。",
                    annualActionLabel(goal.metric),
                    annualDestination(goal.metric),
                    "推进${goal.metric.label}"
                )
            }
        if (state.people.none { it.alive && (it.currentTask != null || it.trainingFocus != null) }) {
            actions += V3RecommendedAction(
                "arrange_people",
                V3ActionPriority.Efficiency,
                "安排本月人手",
                "当前没有族人派差或培养，直接月结会浪费一个月的人才成长。",
                "前往族人安排",
                V3Screen.People,
                "按所长派差，或使用一键安排"
            )
        }
        return actions
            .distinctBy { it.id }
            .sortedBy { it.priority.rank }
            .take(4)
            .ifEmpty { listOf(mainQuestAction(state, chapter, mainQuest)) }
    }

    private fun mainQuestAction(state: V3GameState, chapter: V3Chapter, quest: V3QuestCard): V3RecommendedAction {
        val canClaim = claimableReward(state)
        if (canClaim != null) {
            return V3RecommendedAction(
                "claim_${canClaim.chapter.name}",
                V3ActionPriority.MainQuest,
                "领取第${canClaim.chapter.number}章奖励",
                "【${canClaim.chapter.title}】已经完成。领取${canClaim.text}，再继续下一章。",
                "领取章节奖励",
                V3Screen.County,
                canClaim.text
            )
        }
        return V3RecommendedAction(
            "main_${chapter.name}",
            V3ActionPriority.MainQuest,
            quest.title,
            quest.blockers.firstOrNull() ?: quest.description,
            quest.actionLabel,
            quest.destination,
            if (quest.rewardText.isBlank()) "推进章节" else "章节奖励：${quest.rewardText}",
            canExecute = true,
            blockers = quest.blockers
        )
    }

    private fun nextRankDestination(state: V3GameState): V3Screen = when {
        needsMarriage(state) -> V3Screen.Clan
        V3GameEngine.builtSiteCount(state) < (V3GameEngine.nextRankCost(state)?.builtSites ?: 0) -> V3Screen.County
        state.people.none { it.alive && (it.currentTask != null || it.trainingFocus != null) } -> V3Screen.People
        state.clanRank >= 3 && externalControlledRegions(state) < 1 -> V3Screen.Strategy
        else -> V3Screen.County
    }

    private fun nextRankActionLabel(state: V3GameState): String = when {
        needsMarriage(state) -> "前往婚配与传承"
        V3GameEngine.builtSiteCount(state) < (V3GameEngine.nextRankCost(state)?.builtSites ?: 0) -> "前往营建产业"
        state.clanRank >= 3 && externalControlledRegions(state) < 1 -> "前往天下经营"
        state.people.none { it.alive && (it.currentTask != null || it.trainingFocus != null) } -> "前往安排族人"
        else -> "继续本月经营"
    }

    private fun needsMarriage(state: V3GameState): Boolean =
        state.clanRank <= 2 && state.people.none { it.alive && it.spouseId != null }

    private fun elapsedMonths(state: V3GameState): Int = (state.year - 1601) * 12 + state.month - 1

    private fun externalControlledRegions(state: V3GameState): Int =
        V3GameEngine.externalControlledRegionCount(state)

    private fun routeObjective(route: V3Route): String = when (route) {
        V3Route.Scholar -> "以书院、科举和士绅关系把家族塑成士林门第。"
        V3Route.Merchant -> "以集市、商队和跨县货路积累足以渡过乱世的财富。"
        V3Route.Fortress -> "以寨堡、粮仓和乡勇建立保境安民的宗族共同体。"
        V3Route.Loyalist -> "以官府、军镇与仕途关系积累勤王资本。"
        V3Route.Warlord -> "以乡勇、据点和地域控制形成割据一方的实力。"
        V3Route.Overseas -> "以码头、商帮和船队为宗族准备远海退路。"
        V3Route.Hermit -> "以凝聚、低风险和充足粮仓保存宗族香火。"
    }

    private fun routeActionLabel(route: V3Route, conditions: List<V3QuestCondition>): String {
        val missing = conditions.firstOrNull { !it.satisfied }?.label.orEmpty()
        return when {
            missing.contains("学识") -> "培养高学识族人"
            missing.contains("关系") || missing.contains("路线") && route == V3Route.Loyalist -> "议事并经营地方关系"
            missing.contains("银两") || missing.contains("家产") || missing.contains("码头") -> "经营产业与商路"
            missing.contains("粮食") || missing.contains("安定") || missing.contains("凝聚") -> "稳住粮仓与县域"
            missing.contains("乡勇") || missing.contains("控制") || missing.contains("统一") -> "整备军务与地域"
            else -> when (route) {
                V3Route.Scholar -> "培养科举与士绅关系"
                V3Route.Loyalist -> "议事并经营官军关系"
                V3Route.Merchant, V3Route.Overseas -> "经营产业与商路"
                V3Route.Fortress, V3Route.Warlord -> "整备军务与地域"
                V3Route.Hermit -> "稳住粮仓与宗族"
            }
        }
    }

    private fun routeDestination(route: V3Route, conditions: List<V3QuestCondition>): V3Screen {
        val missing = conditions.firstOrNull { !it.satisfied }?.label.orEmpty()
        return when {
            missing.contains("学识") -> V3Screen.People
            missing.contains("关系") || route == V3Route.Loyalist && missing.contains("路线") -> V3Screen.Strategy
            missing.contains("银两") || missing.contains("家产") || missing.contains("码头") ||
                missing.contains("粮食") || missing.contains("安定") || missing.contains("凝聚") -> V3Screen.County
            missing.contains("乡勇") || missing.contains("控制") || missing.contains("统一") -> V3Screen.Strategy
            route == V3Route.Scholar -> V3Screen.People
            route == V3Route.Loyalist || route == V3Route.Fortress || route == V3Route.Warlord -> V3Screen.Strategy
            else -> V3Screen.County
        }
    }

    private fun annualRewardText(goal: com.arktools.daming.v3.data.V3AnnualGoal): String = listOfNotNull(
        goal.rewardSilver.takeIf { it > 0 }?.let { "银+$it" },
        goal.rewardGrain.takeIf { it > 0 }?.let { "粮+$it" },
        goal.rewardInfluence.takeIf { it > 0 }?.let { "族望+$it" },
        goal.rewardCohesion.takeIf { it > 0 }?.let { "凝聚+$it" }
    ).joinToString(" · ").ifBlank { "路线推进" }

    private fun annualActionLabel(metric: com.arktools.daming.v3.data.V3GoalMetric): String = when (metric) {
        com.arktools.daming.v3.data.V3GoalMetric.Population,
        com.arktools.daming.v3.data.V3GoalMetric.ClanRank,
        com.arktools.daming.v3.data.V3GoalMetric.Cohesion,
        com.arktools.daming.v3.data.V3GoalMetric.Influence -> "前往宗族"

        com.arktools.daming.v3.data.V3GoalMetric.RouteScore,
        com.arktools.daming.v3.data.V3GoalMetric.RelationTotal,
        com.arktools.daming.v3.data.V3GoalMetric.ControlledRegions,
        com.arktools.daming.v3.data.V3GoalMetric.Unification,
        com.arktools.daming.v3.data.V3GoalMetric.Militia -> "前往大势"

        else -> "前往家业"
    }

    private fun annualDestination(metric: com.arktools.daming.v3.data.V3GoalMetric): V3Screen = when (metric) {
        com.arktools.daming.v3.data.V3GoalMetric.Population,
        com.arktools.daming.v3.data.V3GoalMetric.ClanRank,
        com.arktools.daming.v3.data.V3GoalMetric.Cohesion,
        com.arktools.daming.v3.data.V3GoalMetric.Influence -> V3Screen.Clan

        com.arktools.daming.v3.data.V3GoalMetric.RouteScore,
        com.arktools.daming.v3.data.V3GoalMetric.RelationTotal,
        com.arktools.daming.v3.data.V3GoalMetric.ControlledRegions,
        com.arktools.daming.v3.data.V3GoalMetric.Unification,
        com.arktools.daming.v3.data.V3GoalMetric.Militia -> V3Screen.Strategy

        else -> V3Screen.County
    }
}
