package com.arktools.daming.v3.logic

import com.arktools.daming.v3.data.V3CardChoice
import com.arktools.daming.v3.data.V3CardPool
import com.arktools.daming.v3.data.V3CardRequire
import com.arktools.daming.v3.data.V3DiceRoll
import com.arktools.daming.v3.data.V3EffectDelta
import com.arktools.daming.v3.data.V3Content
import com.arktools.daming.v3.data.V3GameState
import com.arktools.daming.v3.data.V3MonthlyCard
import com.arktools.daming.v3.data.V3RouteDelta
import kotlin.math.abs

/**
 * 月度卡牌与家业危机引擎。
 *
 * 卡牌只改变可序列化的 GameState，不持有 UI 或随机对象，便于存档和单测。
 * 抽牌分成 11 层：危机、章节、年务、链案、访客、关系、族长、田庄、风闻、一次性锁、通用池。
 */
object V3CardEngine {
    private const val EARLY_BUDGET = 3
    private const val LATE_BUDGET = 5
    private const val MAX_CARDS = 5
    private const val CRISIS_GRAIN_THRESHOLD = 0
    private const val CRISIS_REFUGEE_THRESHOLD = 18
    private const val CRISIS_UNREST_THRESHOLD = 35
    private const val CRISIS_MUTINY_THRESHOLD = 65

    data class CardResolution(
        val state: V3GameState,
        val card: V3MonthlyCard,
        val choice: V3CardChoice,
        val dice: V3DiceRoll? = null,
        val message: String
    )

    fun budget(state: V3GameState): Int =
        if (V3ProgressionEngine.currentChapter(state).number <= 2) EARLY_BUDGET else LATE_BUDGET

    fun refreshMonth(state: V3GameState, cards: List<V3MonthlyCard> = V3Content.allMonthlyCards): V3GameState {
        val generationSeen = state.seenCardGenerations[state.patriarch.generation].orEmpty()
        val visitorCards = visitorIntroCards(state) + visitorChainCards(state)
        val available = (cards + visitorCards).distinctBy { it.id }.filter { card ->
            val chapter = V3ProgressionEngine.currentChapter(state).number
            chapter in card.minChapter..card.maxChapter &&
                card.id !in state.activeCards.map { it.id } &&
                (card.pool != V3CardPool.Crisis || crisisCardApplies(card, state)) &&
                (card.pool != V3CardPool.Annual || annualCardApplies(card, state)) &&
                (!card.once || card.id !in state.seenCardIds) &&
                (!card.oncePerGeneration || card.id !in generationSeen)
        }
        val selected = selectPriorityCards(state, available, budget(state)).take(MAX_CARDS)
        return state.copy(
            activeCards = selected,
            playedCardsThisMonth = 0,
            cardBudget = budget(state),
            pendingDice = null,
            currentCrisisStage = crisisStage(state),
            pendingReports = if (selected.isEmpty()) {
                listOf("本月暂无待议家务，族人各守其职。")
            } else {
                listOf("本月有${selected.size}件家务待议，族长可择要处置。")
            }
        )
    }

    /** 返回满足当前条件的 11 层优先卡，不满足的卡只会落入通用池。 */
    fun selectPriorityCards(
        state: V3GameState,
        available: List<V3MonthlyCard>,
        limit: Int = budget(state)
    ): List<V3MonthlyCard> {
        if (available.isEmpty() || limit <= 0) return emptyList()
        val chosen = linkedSetOf<String>()
        val prioritizedLayers = listOf(
            available.filter { it.pool == V3CardPool.Crisis },
            available.filter { it.tag.contains("章节") },
            available.filter { it.pool == V3CardPool.Visitor && visitorCardApplies(it, state) },
            available.filter { it.pool == V3CardPool.Annual },
            available.filter { it.pool == V3CardPool.Exam },
            available.filter { it.tag.contains("关系") && relationshipCardApplies(it, state) },
            available.filter { it.tag.contains("族长") && patriarchCardApplies(it, state) }
        )
        val rotatingPool = available.filter {
            it.id !in chosen &&
                it.pool !in setOf(
                    V3CardPool.Crisis,
                    V3CardPool.Visitor,
                    V3CardPool.Annual,
                    V3CardPool.Exam
                ) &&
                !it.tag.contains("章节")
        }
        val seed = stableSeed(state)
        prioritizedLayers.forEachIndexed { index, layer ->
            if (chosen.size >= limit) return@forEachIndexed
            val candidates = orderedCandidates(layer.filter { it.id !in chosen }, seed, index)
            candidates.firstOrNull()?.let { chosen += it.id }
        }
        orderedCandidates(rotatingPool.filter { it.id !in chosen }, seed, prioritizedLayers.size)
            .take(limit - chosen.size)
            .forEach { chosen += it.id }
        return chosen.mapNotNull { id -> available.firstOrNull { it.id == id } }
    }

    private fun orderedCandidates(
        cards: List<V3MonthlyCard>,
        seed: Int,
        layerIndex: Int
    ): List<V3MonthlyCard> =
        cards.sortedByDescending { card ->
            val rotation = Math.floorMod(
                card.id.hashCode() + seed + layerIndex * 31,
                997
            )
            rotation + card.weight * 8
        }

    fun canPlay(state: V3GameState, cardId: String, choiceId: String): Boolean {
        if (state.playedCardsThisMonth >= state.cardBudget) return false
        val card = state.activeCards.firstOrNull { it.id == cardId } ?: return false
        val choice = card.choices.firstOrNull { it.id == choiceId } ?: return false
        return meets(choice.require, state) && state.pendingDice == null
    }

    fun choose(state: V3GameState, cardId: String, choiceId: String): CardResolution? {
        if (state.playedCardsThisMonth >= state.cardBudget || state.pendingDice != null) return null
        val card = state.activeCards.firstOrNull { it.id == cardId } ?: return null
        val choice = card.choices.firstOrNull { it.id == choiceId } ?: return null
        if (!meets(choice.require, state)) return null
        if (choice.dice) {
            val pending = V3DiceRoll(card.id, choice.id, choice.successRate.coerceIn(0, 100), diceRoll(state, card, choice))
            return CardResolution(
                state.copy(pendingDice = pending),
                card,
                choice,
                pending,
                "纸上掷签，待看吉凶。"
            )
        }
        return resolve(state, card, choice, null)
    }

    fun resolveDice(state: V3GameState): CardResolution? {
        val pending = state.pendingDice ?: return null
        val card = state.activeCards.firstOrNull { it.id == pending.cardId } ?: return null
        val choice = card.choices.firstOrNull { it.id == pending.choiceId } ?: return null
        return resolve(state, card, choice, pending)
    }

    fun resolve(
        state: V3GameState,
        card: V3MonthlyCard,
        choice: V3CardChoice,
        dice: V3DiceRoll?
    ): CardResolution {
        val success = dice?.success ?: true
        val delta = if (dice == null) {
            choice.effects
        } else {
            choice.effects.plus(if (success) choice.successEffects else choice.failureEffects)
        }
        var next = applyDelta(state, applyOriginTraitBonus(state, card, delta))
        val nextSeen = if (card.once) {
            (next.seenCardIds + card.id).distinct()
        } else next.seenCardIds
        val nextGenerationSeen = if (card.oncePerGeneration) {
            next.seenCardGenerations + (
                next.patriarch.generation to
                    (next.seenCardGenerations[next.patriarch.generation].orEmpty() + card.id).distinct()
                )
        } else next.seenCardGenerations
        val chainCard = choice.nextCardId?.let { id -> next.activeCards.firstOrNull { it.id == id } }
        val remainingCards = next.activeCards.filterNot { it.id == card.id }.let { cards ->
            if (chainCard != null && chainCard.id !in cards.map { it.id }) listOf(chainCard) + cards else cards
        }
        val text = if (dice == null) choice.successText else if (success) choice.successText else choice.failureText
        val log = "【${card.tag.ifBlank { card.pool.label() }}】${card.title}：${choice.label}。$text"
        next = next.copy(
            activeCards = remainingCards,
            playedCardsThisMonth = next.playedCardsThisMonth + 1,
            seenCardIds = nextSeen,
            seenCardGenerations = nextGenerationSeen,
            seenVisitors = next.seenVisitors,
            pendingDice = null,
            pendingReports = listOf(log),
            biography = delta.biographicalNote?.let { (next.biography + it).take(80) } ?: next.biography,
            eventLog = (listOf("${next.year}年${next.month}月 · $log") + next.eventLog).take(100)
        )
        return CardResolution(next, card, choice, dice, log)
    }

    fun applyCrisisCascade(state: V3GameState, detailLines: MutableList<String>? = null): V3GameState {
        var next = state
        var stage = crisisStage(next)
        if (next.grain < CRISIS_GRAIN_THRESHOLD) {
            val added = ((-next.grain) / 12 + 2).coerceIn(2, 12)
            next = next.copy(
                refugees = (next.refugees + added).coerceAtMost(999),
                unrestLevel = (next.unrestLevel + added * 2).coerceAtMost(100),
                cohesion = (next.cohesion - 2).coerceIn(0, 100),
                currentCrisisStage = "grain_shortage"
            )
            detailLines?.add("粮仓见底，逃荒人群来投：流民+$added，庄内怨气上升。")
            stage = "grain_shortage"
        }
        if (next.refugees >= CRISIS_REFUGEE_THRESHOLD) {
            next = next.copy(
                unrestLevel = (next.unrestLevel + 6).coerceAtMost(100),
                garrisonMorale = (next.garrisonMorale - 3).coerceIn(0, 100),
                currentCrisisStage = "unrest"
            )
            detailLines?.add("流民聚集已越过安置能力，庄内怨气转为明火。")
            stage = "unrest"
        }
        if (next.unrestLevel >= CRISIS_UNREST_THRESHOLD) {
            next = next.copy(
                garrisonMorale = (next.garrisonMorale - 5).coerceIn(0, 100),
                influence = (next.influence - 2).coerceIn(0, 100),
                currentCrisisStage = "unrest"
            )
            detailLines?.add("房支与佃户争执不休，乡勇守望开始松动。")
            stage = "unrest"
        }
        if (next.garrisonMorale < CRISIS_MUTINY_THRESHOLD && next.unrestLevel >= CRISIS_UNREST_THRESHOLD) {
            next = next.copy(
                militia = (next.militia - 4).coerceAtLeast(0),
                army = next.army.lose(4),
                cohesion = (next.cohesion - 4).coerceIn(0, 100),
                currentCrisisStage = "mutiny"
            )
            detailLines?.add("乡勇怨声载道，已有数人弃械，团练折损。")
            stage = "mutiny"
        }
        return next.copy(currentCrisisStage = stage)
    }

    fun crisisStage(state: V3GameState): String? = when {
        state.garrisonMorale < CRISIS_MUTINY_THRESHOLD && state.unrestLevel >= CRISIS_UNREST_THRESHOLD -> "mutiny"
        state.unrestLevel >= CRISIS_UNREST_THRESHOLD || state.refugees >= CRISIS_REFUGEE_THRESHOLD -> "unrest"
        state.grain < CRISIS_GRAIN_THRESHOLD -> "grain_shortage"
        else -> null
    }

    fun meets(require: V3CardRequire?, state: V3GameState): Boolean {
        if (require == null) return true
        val relations = state.relations
        val patriarch = state.patriarch
        val stat = when (require.minPatriarchStat) {
            "conduct" -> patriarch.conduct
            "stewardship" -> patriarch.stewardship
            "prestige" -> patriarch.prestige
            "health" -> patriarch.health
            else -> null
        }
        return (require.minSilver == null || state.silver >= require.minSilver) &&
            (require.minGrain == null || state.grain >= require.minGrain) &&
            (require.minInfluence == null || state.influence >= require.minInfluence) &&
            (require.minCohesion == null || state.cohesion >= require.minCohesion) &&
            (require.minMilitia == null || state.militia >= require.minMilitia) &&
            (require.minRelationYamen == null || relations.yamen >= require.minRelationYamen) &&
            (require.minRelationGentry == null || relations.gentry >= require.minRelationGentry) &&
            (require.minRelationVillagers == null || relations.villagers >= require.minRelationVillagers) &&
            (require.minRelationMerchants == null || relations.merchants >= require.minRelationMerchants) &&
            (require.minRelationBandits == null || relations.bandits >= require.minRelationBandits) &&
            (require.minRelationGarrison == null || relations.garrison >= require.minRelationGarrison) &&
            (require.minRouteScore == null || (state.routeScores[require.minRouteScore.route] ?: 0) >= require.minRouteScore.score) &&
            (require.minClanRank == null || state.clanRank >= require.minClanRank) &&
            (require.minChapter == null || V3ProgressionEngine.currentChapter(state).number >= require.minChapter) &&
            (require.flagRequired == null || require.flagRequired in state.completedStoryFlags) &&
            (require.flagBlocked == null || require.flagBlocked !in state.completedStoryFlags) &&
            (require.minPatriarchStatValue == null || (stat != null && stat >= require.minPatriarchStatValue))
    }

    fun applyInventoryEffects(state: V3GameState, detailLines: MutableList<String>? = null): V3GameState {
        val recurring = state.inventory
            .mapNotNull { id -> V3Content.items.firstOrNull { it.id == id } }
            .filter { it.recurring }
            .map { it.effects }
            .fold(V3EffectDelta()) { total, item -> total.plus(item) }
        if (recurring == V3EffectDelta()) return state
        detailLines?.add("物品生效：${state.inventory.count { id -> V3Content.items.any { it.id == id && it.recurring } }}件家传物品为本月家业添了一笔助益。")
        return applyDelta(state, recurring)
    }

    private fun applyOriginTraitBonus(
        state: V3GameState,
        card: V3MonthlyCard,
        delta: V3EffectDelta
    ): V3EffectDelta {
        if (state.originTraits.isEmpty()) return delta
        var result = delta
        state.originTraits.forEach { trait ->
            result = when {
                trait.startsWith("同乡相护") && card.crisisLevel == 1 ->
                    result.copy(refugees = result.refugees - 2)
                trait.startsWith("账房传家") && card.pool == V3CardPool.Trade ->
                    result.copy(silver = result.silver + 5)
                trait.startsWith("边堡军籍") && card.pool == V3CardPool.Crisis ->
                    result.copy(garrisonMorale = result.garrisonMorale + 3)
                trait.startsWith("旧谱余荫") && (card.tag == "科举" || card.tag == "关系") ->
                    result.copy(influence = result.influence + 2)
                trait.startsWith("山寨旧盟") && card.tag == "灾变" ->
                    result.copy(bandits = result.bandits + 2, yamen = result.yamen - 1)
                trait.startsWith("海路遗契") && (card.pool == V3CardPool.Trade || card.tag == "商旅") ->
                    result.copy(
                        silver = result.silver + 4,
                        routeDelta = result.routeDelta ?: V3RouteDelta(V3Route.Overseas, 1)
                    )
                else -> result
            }
        }
        return result
    }

    private fun crisisLevel(state: V3GameState): Int = when (crisisStage(state)) {
        "mutiny" -> 3
        "unrest" -> 2
        "grain_shortage" -> 1
        else -> 0
    }

    private fun applyDelta(state: V3GameState, delta: V3EffectDelta): V3GameState {
        val relations = state.relations.copy(
            yamen = clamp(state.relations.yamen + delta.yamen),
            gentry = clamp(state.relations.gentry + delta.gentry),
            villagers = clamp(state.relations.villagers + delta.villagers),
            bandits = clamp(state.relations.bandits + delta.bandits),
            merchants = clamp(state.relations.merchants + delta.merchants),
            garrison = clamp(state.relations.garrison + delta.garrison)
        )
        val patriarch = state.patriarch.copy(
            conduct = (state.patriarch.conduct + delta.patriarchConduct).coerceIn(0, 100),
            stewardship = (state.patriarch.stewardship + delta.patriarchStewardship).coerceIn(0, 100),
            prestige = (state.patriarch.prestige + delta.patriarchPrestige).coerceIn(0, 100),
            health = (state.patriarch.health + delta.patriarchHealth).coerceIn(0, 100)
        )
        val flags = buildList {
            addAll(state.completedStoryFlags)
            delta.storyFlag?.let { add(it) }
            delta.removeFlag?.let { remove(it) }
        }.distinct()
        val plaques = delta.plaqueId?.let { (state.plaques + it).distinct() } ?: state.plaques
        val inventory = delta.itemId?.let { (state.inventory + it).distinct() } ?: state.inventory
        val visitorProgress = delta.visitorId?.let { id ->
            state.visitorProgress + (id to (delta.visitorProgress ?: ((state.visitorProgress[id] ?: 0) + 1)))
        } ?: state.visitorProgress
        val seenVisitors = delta.visitorId?.let { (state.seenVisitors + it).distinct() } ?: state.seenVisitors
        val routeScores = delta.routeDelta?.let {
            state.routeScores + (it.route to ((state.routeScores[it.route] ?: 0) + it.delta))
        } ?: state.routeScores
        val nextArmy = if (delta.militia >= 0) {
            state.army.add(com.arktools.daming.v3.data.V3TroopType.Militia, delta.militia)
        } else {
            state.army.lose(-delta.militia)
        }
        val result = state.copy(
            silver = (state.silver + delta.silver).coerceAtLeast(-999),
            grain = (state.grain + delta.grain).coerceAtLeast(-999),
            influence = (state.influence + delta.influence).coerceIn(0, 100),
            cohesion = (state.cohesion + delta.cohesion).coerceIn(0, 100),
            militia = nextArmy.total(),
            army = nextArmy,
            refugees = (state.refugees + delta.refugees).coerceAtLeast(0),
            garrisonMorale = (state.garrisonMorale + delta.garrisonMorale).coerceIn(0, 100),
            unrestLevel = (state.unrestLevel + delta.unrest).coerceIn(0, 100),
            relations = relations,
            patriarch = patriarch,
            rebelHeat = (state.rebelHeat + delta.rebelHeat).coerceAtLeast(0),
            completedStoryFlags = flags,
            plaques = plaques,
            inventory = inventory,
            seenVisitors = seenVisitors,
            visitorProgress = visitorProgress,
            routeScores = routeScores
        )
        val acquiredEffects = delta.itemId
            ?.let { id -> V3Content.items.firstOrNull { it.id == id && !it.recurring }?.effects }
            ?.copy(itemId = null)
        return if (acquiredEffects == null || acquiredEffects == V3EffectDelta()) {
            result
        } else {
            applyDelta(result, acquiredEffects)
        }
    }

    private fun visitorIntroCards(state: V3GameState): List<V3MonthlyCard> =
        V3Content.visitors.mapNotNull { visitor ->
            if (visitor.id in state.seenVisitors || state.visitorProgress.containsKey(visitor.id)) return@mapNotNull null
            val first = visitor.chapters.firstOrNull() ?: return@mapNotNull null
            if (first.chapter > V3ProgressionEngine.currentChapter(state).number) return@mapNotNull null
            V3MonthlyCard(
                id = "visitor_intro_${visitor.id}",
                pool = V3CardPool.Visitor,
                title = "${visitor.name}：${first.title}",
                body = first.body,
                tag = "访客",
                weight = 22,
                once = true,
                minChapter = first.chapter,
                choices = listOf(
                    V3CardChoice(
                        id = "receive",
                        label = "请入庄中",
                        desc = "让远客在族谱上留下姓名，也把这一段见闻留下。",
                        effects = first.effects.plus(
                            V3EffectDelta(
                                itemId = first.giftItemId,
                                visitorId = visitor.id,
                                visitorProgress = 1,
                                biographicalNote = "${visitor.name}初次过庄：${first.title}。"
                            )
                        )
                    ),
                    V3CardChoice(
                        id = "farewell",
                        label = "以礼送行",
                        desc = "不把远客留在门内，但仍结下一段可追溯的善缘。",
                        effects = V3EffectDelta(
                            silver = -6,
                            influence = 2,
                            visitorId = visitor.id,
                            visitorProgress = 1,
                            biographicalNote = "${visitor.name}初次过庄，族人以礼相送。"
                        )
                    )
                )
            )
        }

    private fun visitorChainCards(state: V3GameState): List<V3MonthlyCard> =
        V3Content.visitors.flatMap { visitor ->
            val progress = state.visitorProgress[visitor.id] ?: 0
            if (progress == 0) return@flatMap emptyList()
            val next = visitor.chapters.getOrNull(progress) ?: return@flatMap emptyList()
            if (next.chapter > V3ProgressionEngine.currentChapter(state).number) return@flatMap emptyList()
            listOf(
                V3MonthlyCard(
                    id = "visitor_chain_${visitor.id}_${progress + 1}",
                    pool = V3CardPool.Visitor,
                    title = "${visitor.name}：${next.title}",
                    body = next.body,
                    tag = "访客续章",
                    weight = 24,
                    once = false,
                    minChapter = next.chapter,
                    choices = listOf(
                        V3CardChoice(
                            id = "receive",
                            label = "请入族中",
                            desc = "把这段相逢写进家乘，也把远方的见识留下。",
                            effects = next.effects.plus(
                                V3EffectDelta(
                                    itemId = next.giftItemId,
                                    visitorId = visitor.id,
                                    visitorProgress = progress + 1,
                                    biographicalNote = "${visitor.name}第${progress + 1}次来访：${next.title}。"
                                )
                            )
                        ),
                        V3CardChoice(
                            id = "farewell",
                            label = "以礼相送",
                            desc = "不强留远客，仍以一份薄礼结下善缘。",
                            effects = V3EffectDelta(
                                silver = -6,
                                influence = 2,
                                visitorId = visitor.id,
                                visitorProgress = progress + 1,
                                biographicalNote = "${visitor.name}第${progress + 1}次过庄，族人以礼相送。"
                            )
                        )
                    )
                )
            )
        }

    private fun V3EffectDelta.plus(other: V3EffectDelta?): V3EffectDelta {
        if (other == null) return this
        return copy(
            silver = silver + other.silver, grain = grain + other.grain, influence = influence + other.influence,
            cohesion = cohesion + other.cohesion, militia = militia + other.militia, refugees = refugees + other.refugees,
            garrisonMorale = garrisonMorale + other.garrisonMorale, unrest = unrest + other.unrest,
            yamen = yamen + other.yamen, gentry = gentry + other.gentry, villagers = villagers + other.villagers,
            bandits = bandits + other.bandits, merchants = merchants + other.merchants, garrison = garrison + other.garrison,
            patriarchConduct = patriarchConduct + other.patriarchConduct, patriarchStewardship = patriarchStewardship + other.patriarchStewardship,
            patriarchPrestige = patriarchPrestige + other.patriarchPrestige, patriarchHealth = patriarchHealth + other.patriarchHealth,
            rebelHeat = rebelHeat + other.rebelHeat, storyFlag = other.storyFlag ?: storyFlag,
            removeFlag = other.removeFlag ?: removeFlag, plaqueId = other.plaqueId ?: plaqueId,
            itemId = other.itemId ?: itemId, biographicalNote = other.biographicalNote ?: biographicalNote,
            visitorId = other.visitorId ?: visitorId, visitorProgress = other.visitorProgress ?: visitorProgress,
            routeDelta = other.routeDelta ?: routeDelta
        )
    }

    private fun stableSeed(state: V3GameState): Int = state.year * 37 + state.month * 17 + state.clanRank * 11 + state.people.size

    private fun diceRoll(state: V3GameState, card: V3MonthlyCard, choice: V3CardChoice): Int =
        abs((stableSeed(state) + card.id.hashCode() * 3 + choice.id.hashCode()) % 100)

    private fun crisisCardApplies(card: V3MonthlyCard, state: V3GameState): Boolean =
        if (card.crisisLevel == 0) {
            crisisStage(state) != null
        } else {
            card.crisisLevel <= crisisLevel(state)
        }

    private fun annualCardApplies(card: V3MonthlyCard, state: V3GameState): Boolean =
        when (card.id) {
            "annual_repair_ledger", "annual_03" -> state.month == 12
            "annual_01" -> state.month == 2
            "annual_02" -> state.month == 9
            "annual_04" -> state.month == 4
            "annual_05" -> state.month == 11
            else -> true
        }

    private fun visitorCardApplies(card: V3MonthlyCard, state: V3GameState): Boolean =
        card.id !in state.seenCardIds

    private fun relationshipCardApplies(card: V3MonthlyCard, state: V3GameState): Boolean =
        state.relations.yamen < 20 ||
            state.relations.gentry < 20 ||
            state.relations.villagers < 20 ||
            state.relations.bandits < 20 ||
            state.relations.merchants < 20 ||
            state.relations.garrison < 20

    private fun patriarchCardApplies(card: V3MonthlyCard, state: V3GameState): Boolean =
        state.patriarch.health < 45 || state.patriarch.conduct >= 70 || state.patriarch.stewardship >= 70 || state.patriarch.prestige >= 70

    private fun clamp(value: Int): Int = value.coerceIn(-100, 100)

    private fun V3CardPool.label(): String = when (this) {
        V3CardPool.Clan -> "族务"
        V3CardPool.Trade -> "商旅"
        V3CardPool.Estate -> "产业"
        V3CardPool.Field -> "田野"
        V3CardPool.Rumor -> "风闻"
        V3CardPool.Crisis -> "危局"
        V3CardPool.Visitor -> "访客"
        V3CardPool.Chain -> "旧案"
        V3CardPool.Exam -> "科举"
        V3CardPool.Annual -> "年务"
    }
}
