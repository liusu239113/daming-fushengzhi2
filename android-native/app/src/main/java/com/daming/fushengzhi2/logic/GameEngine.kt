package com.daming.fushengzhi2.logic

import com.daming.fushengzhi2.data.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

object GameEngine {
    private val seasonFactors = mapOf(
        ResourceKind.Grain to listOf(0.6, 0.8, 1.0, 1.2, 1.5, 1.3, 1.2, 1.0, 1.4, 1.0, 0.7, 0.5),
        ResourceKind.Silver to listOf(0.9, 1.0, 1.0, 1.1, 1.0, 1.0, 1.0, 1.1, 1.0, 1.2, 1.1, 1.3),
        ResourceKind.Cloth to listOf(0.8, 0.9, 1.0, 1.0, 1.0, 1.0, 1.0, 1.1, 1.2, 1.1, 1.0, 0.9),
        ResourceKind.Fame to List(12) { 1.0 }
    )

    private val marketSeasonFactors = mapOf(
        "grain" to listOf(1.00, 1.15, 1.20, 1.10, 0.95, 0.90, 0.85, 0.80, 0.70, 0.75, 0.85, 0.95),
        "cloth" to listOf(1.05, 1.00, 0.95, 0.90, 0.90, 0.95, 1.00, 1.00, 1.05, 1.10, 1.15, 1.20),
        "herb" to listOf(1.00, 1.00, 1.00, 1.05, 1.10, 1.15, 1.15, 1.10, 1.00, 1.00, 0.95, 0.95),
        "weapon" to listOf(1.00, 1.05, 1.05, 1.00, 1.00, 0.95, 0.95, 1.00, 1.05, 1.05, 1.00, 1.00),
        "book" to listOf(1.00, 1.15, 1.10, 1.00, 0.95, 0.90, 0.90, 1.10, 1.15, 1.00, 0.95, 0.95),
        "horse" to listOf(1.05, 1.05, 1.10, 1.10, 1.00, 0.95, 0.90, 0.90, 0.95, 1.00, 1.05, 1.10)
    )

    private data class IndustryEffects(
        val reduceSick: Boolean = false,
        val horseRanch: Boolean = false,
        val smithyBonus: Int = 0,
        val bookshopBonus: Int = 0
    )

    private data class MutableReport(
        var incomeSilver: Int = 0,
        var incomeGrain: Int = 0,
        var incomeCloth: Int = 0,
        var incomeFame: Int = 0,
        var expenseSilver: Int = 0,
        var expenseGrain: Int = 0,
        var expenseCloth: Int = 0,
        var expenseFame: Int = 0,
        val events: MutableList<String> = mutableListOf(),
        var industryEffects: IndustryEffects = IndustryEffects()
    )

    fun newGame(
        surname: String,
        originId: String,
        regionId: String,
        mottoId: String,
        difficultyId: String
    ): GameState {
        val origin = GameContent.origin(originId)
        val motto = GameContent.motto(mottoId)
        val patriarchGender = Gender.Male
        val patriarch = ClanMember(
            id = 1,
            name = surname + randomGivenName(patriarchGender),
            gender = patriarchGender,
            age = 20 + Random.nextInt(0, 6),
            generation = 1,
            identity = if (origin.id == "military") "士兵" else "白丁",
            state = MemberState.Home,
            study = (10 + Random.nextInt(0, 21) + if (origin.id == "landlord") 5 else 0).coerceAtMost(30),
            martial = (10 + Random.nextInt(0, 21) + if (origin.id == "military") 5 else 0).coerceAtMost(30),
            health = 85 + Random.nextInt(0, 16),
            aptitude = randomAptitudeSet(),
            talentId = rollTalentId()
        )
        val industries = when (origin.id) {
            "landlord" -> listOf("dry_field", "paddy_field", "shop")
            "military" -> listOf("dry_field", "dry_field")
            else -> listOf("dry_field")
        }.mapIndexed { index, typeId -> ClanIndustry(index + 1, typeId) }

        var state = GameState(
            surname = surname,
            originId = origin.id,
            regionId = regionId,
            clanName = "${surname}氏宗族",
            silver = origin.silver + motto.effects.initSilverBonus,
            grain = origin.grain,
            cloth = origin.cloth,
            fame = origin.fame,
            familyMottoId = motto.id,
            difficultyId = difficultyId,
            members = listOf(patriarch),
            nextMemberId = 2,
            patriarchId = patriarch.id,
            industries = industries,
            nextIndustryId = industries.size + 1,
            academies = if (origin.id == "landlord") listOf(Academy("school")) else emptyList(),
            market = MarketState(prices = GameContent.defaultMarketPrices()),
            eventLog = listOf(EventLogEntry(1368, 1, "${surname}氏宗族立宗，乱世浮生由此开始。"))
        )
        state = generateYearlyGoals(state)
        return state
    }

    fun aliveMembers(state: GameState) = state.members.filter { it.alive }
    fun adults(state: GameState) = aliveMembers(state).filter { it.age >= 15 }
    fun clanRankName(state: GameState) = GameContent.rankName(state.clanRank)
    fun region(state: GameState) = GameContent.region(state.regionId)
    fun difficulty(state: GameState) = GameContent.difficulty(state.difficultyId)

    fun canRankUp(state: GameState): Boolean {
        val next = state.clanRank + 1
        val req = GameContent.rankRequirements[next] ?: return false
        return state.silver >= req.silver &&
            state.fame >= req.fame &&
            state.grain >= req.grain &&
            state.cloth >= req.cloth &&
            aliveMembers(state).size >= req.population
    }

    fun rankUp(state: GameState): GameState {
        if (!canRankUp(state)) return state
        val next = state.clanRank + 1
        val req = GameContent.rankRequirements[next] ?: return state
        val updated = state.copy(
            clanRank = next,
            silver = state.silver - req.silver,
            fame = state.fame - req.fame,
            grain = state.grain - req.grain,
            cloth = state.cloth - req.cloth
        )
        return addLog(updated, "宗族晋升为【${GameContent.rankName(next)}】，门第更进一步。")
    }

    fun addIndustry(state: GameState, typeId: String): GameState {
        val type = GameContent.industry(typeId) ?: return state
        if (type.evolved || state.clanRank < type.unlockRank) return state
        val limit = GameContent.industryLimitByRank[state.clanRank] ?: 4
        if (state.industries.size >= limit || state.silver < type.cost) return state
        val industry = ClanIndustry(state.nextIndustryId, typeId)
        return addLog(
            state.copy(
                silver = state.silver - type.cost,
                industries = state.industries + industry,
                nextIndustryId = state.nextIndustryId + 1,
                fortCount = state.fortCount + if (typeId == "fort") 1 else 0
            ),
            "新建产业【${type.name}】，花费银两${type.cost}。"
        )
    }

    fun upgradeIndustry(state: GameState, industryId: Int): GameState {
        val target = state.industries.firstOrNull { it.id == industryId } ?: return state
        val type = GameContent.industry(target.typeId) ?: return state
        val cost = type.cost * target.level
        if (state.silver < cost) return state
        val industries = state.industries.map { if (it.id == industryId) it.copy(level = it.level + 1) else it }
        return addLog(
            state.copy(silver = state.silver - cost, industries = industries),
            "${type.name}升至${target.level + 1}级，花费银两$cost。"
        )
    }

    fun sellIndustry(state: GameState, industryId: Int): GameState {
        val target = state.industries.firstOrNull { it.id == industryId } ?: return state
        val type = GameContent.industry(target.typeId) ?: return state
        val totalInvest = type.cost + (1 until target.level).sumOf { type.cost * it }
        val refund = floor(totalInvest * 0.5).toInt()
        val industries = state.industries.filterNot { it.id == industryId }
        return addLog(
            state.copy(
                silver = state.silver + refund,
                industries = industries,
                fortCount = state.fortCount - if (target.typeId == "fort") 1 else 0
            ),
            "变卖${type.name}，回收银两$refund。"
        )
    }

    fun evolveIndustry(state: GameState, industryId: Int): GameState {
        val target = state.industries.firstOrNull { it.id == industryId } ?: return state
        val evolution = GameContent.evolution(target.typeId) ?: return state
        if (target.level < evolution.reqLevel || state.clanRank < evolution.reqRank || state.silver < evolution.cost) return state
        val oldType = GameContent.industry(target.typeId)
        val newType = GameContent.industry(evolution.to)
        val industries = state.industries.map { if (it.id == industryId) it.copy(typeId = evolution.to) else it }
        return addLog(
            state.copy(silver = state.silver - evolution.cost, industries = industries),
            "${oldType?.name ?: target.typeId}进化为【${newType?.name ?: evolution.to}】。"
        )
    }

    fun assignIndustry(state: GameState, industryId: Int, memberId: Int?): GameState {
        val members = state.members.map { member ->
            if (member.assignmentId == industryId && member.id != memberId) member.copy(assignmentId = null) else member
        }.map { member ->
            if (member.id == memberId) member.copy(assignmentId = industryId) else member
        }
        val industries = state.industries.map { if (it.id == industryId) it.copy(assignedMemberId = memberId) else it }
        return state.copy(industries = industries, members = members)
    }

    fun setMemberState(state: GameState, memberId: Int, memberState: MemberState, laborJobId: String? = null): GameState {
        val members = state.members.map { member ->
            if (member.id == memberId && member.alive) {
                val rankName = if (memberState == MemberState.Military && member.militaryRank == null) "士兵" else member.militaryRank
                member.copy(
                    state = memberState,
                    militaryRank = rankName,
                    identity = if (memberState == MemberState.Military && member.identity == "白丁") "士兵" else member.identity,
                    laborJob = if (memberState == MemberState.Labor) laborJobId ?: member.laborJob ?: availableLaborJobs(state).firstOrNull()?.id else member.laborJob
                )
            } else member
        }
        val name = members.firstOrNull { it.id == memberId }?.name ?: "族人"
        return addLog(state.copy(members = members), "$name 状态调整为【${memberState.label}】。")
    }

    fun toggleClanRule(state: GameState, ruleId: String): GameState {
        val maxRules = GameContent.maxRulesByRank[state.clanRank] ?: 0
        val exists = state.clanRules.contains(ruleId)
        val newRules = when {
            exists -> state.clanRules - ruleId
            state.clanRules.size < maxRules -> state.clanRules + ruleId
            else -> state.clanRules
        }
        return state.copy(clanRules = newRules)
    }

    fun availableLaborJobs(state: GameState) = GameContent.laborJobs.filter { state.clanRank >= it.rank }

    fun buyMarketItem(state: GameState, itemId: String, count: Int): GameState {
        val commodity = GameContent.commodity(itemId) ?: return state
        if (count <= 0) return state
        val total = (state.market.prices[itemId] ?: commodity.basePrice) * count
        if (state.silver < total) return state
        var next = state.copy(silver = state.silver - total)
        next = if (commodity.resourceKey == "grain") next.copy(grain = next.grain + commodity.batchSize * count)
        else if (commodity.resourceKey == "cloth") next.copy(cloth = next.cloth + commodity.batchSize * count)
        else commodity.itemId?.let { next.copy(inventory = addItem(next.inventory, it, count)) } ?: next.copy(market = next.market.copy(stock = bumpMap(next.market.stock, itemId, count)))
        next = next.copy(market = next.market.copy(tradeCount = next.market.tradeCount + 1, monthlySpent = next.market.monthlySpent + total))
        return addLog(next, "集市买入${commodity.name}$count${commodity.unit}，花费银两$total。")
    }

    fun sellMarketItem(state: GameState, itemId: String, count: Int): GameState {
        val commodity = GameContent.commodity(itemId) ?: return state
        if (count <= 0) return state
        val price = state.market.prices[itemId] ?: commodity.basePrice
        val income = max(1, floor(price * 0.8 * count).toInt())
        var next = state
        next = when (commodity.resourceKey) {
            "grain" -> {
                val amount = commodity.batchSize * count
                if (next.grain < amount) return state
                next.copy(grain = next.grain - amount)
            }
            "cloth" -> {
                val amount = commodity.batchSize * count
                if (next.cloth < amount) return state
                next.copy(cloth = next.cloth - amount)
            }
            else -> {
                val invId = commodity.itemId
                if (invId != null) {
                    val have = next.inventory.firstOrNull { it.itemId == invId }?.count ?: 0
                    if (have < count) return state
                    next.copy(inventory = addItem(next.inventory, invId, -count))
                } else {
                    val have = next.market.stock[itemId] ?: 0
                    if (have < count) return state
                    next.copy(market = next.market.copy(stock = bumpMap(next.market.stock, itemId, -count)))
                }
            }
        }
        next = next.copy(silver = next.silver + income, market = next.market.copy(tradeCount = next.market.tradeCount + 1, totalTradeProfit = next.market.totalTradeProfit + income))
        return addLog(next, "集市卖出${commodity.name}$count${commodity.unit}，获得银两$income。")
    }

    fun useItem(state: GameState, itemId: String, memberId: Int? = null): GameState {
        val item = GameContent.item(itemId) ?: return state
        val have = state.inventory.firstOrNull { it.itemId == itemId }?.count ?: 0
        if (have <= 0) return state
        var next = state.copy(inventory = addItem(state.inventory, itemId, -1))
        next = when (item.effect) {
            "sell" -> next.copy(silver = next.silver + item.value)
            "fame_boost" -> next.copy(fame = next.fame + 15)
            "morale_boost" -> next.copy(fame = next.fame + 20)
            "heal" -> memberId?.let { id -> next.copy(members = next.members.map { if (it.id == id) it.copy(health = min(100, it.health + 35), state = MemberState.Home, prevState = null) else it }) } ?: next
            "study_boost" -> memberId?.let { id -> next.copy(members = next.members.map { if (it.id == id) it.copy(study = min(capFor(next, it, "study"), it.study + 3)) else it }) } ?: next
            "martial_boost" -> memberId?.let { id -> next.copy(members = next.members.map { if (it.id == id) it.copy(martial = min(capFor(next, it, "martial"), it.martial + 3)) else it }) } ?: next
            "martial_boost_large" -> memberId?.let { id -> next.copy(members = next.members.map { if (it.id == id) it.copy(martial = min(capFor(next, it, "martial"), it.martial + 5)) else it }) } ?: next
            else -> next
        }
        return addLog(next, "使用物品【${item.name}】。")
    }

    fun upgradeAcademy(state: GameState, typeId: String): GameState {
        val type = GameContent.academy(typeId) ?: return state
        if (state.clanRank < type.unlockRank) return state
        val existing = state.academies.firstOrNull { it.typeId == typeId }
        val nextLevel = (existing?.level ?: 0) + 1
        val cost = type.upgradeCost * nextLevel
        if (state.silver < cost) return state
        val academies = if (existing == null) state.academies + Academy(typeId, 1) else state.academies.map { if (it.typeId == typeId) it.copy(level = nextLevel) else it }
        return addLog(state.copy(silver = state.silver - cost, academies = academies), "${type.name}提升至${nextLevel}级。")
    }

    fun assignAcademy(state: GameState, typeId: String, memberId: Int): GameState {
        val type = GameContent.academy(typeId) ?: return state
        val academy = state.academies.firstOrNull { it.typeId == typeId } ?: return state
        val slots = type.baseSlotsPerLevel * academy.level
        val updatedIds = if (academy.memberIds.contains(memberId)) academy.memberIds - memberId else {
            if (academy.memberIds.size >= slots) academy.memberIds else academy.memberIds + memberId
        }
        return state.copy(academies = state.academies.map { if (it.typeId == typeId) it.copy(memberIds = updatedIds) else it })
    }

    fun startExpedition(state: GameState, typeId: String, memberId: Int): GameState {
        val type = GameContent.expedition(typeId) ?: return state
        val member = state.members.firstOrNull { it.id == memberId && it.alive } ?: return state
        if (state.clanRank < type.minRank || state.silver < type.costSilver || state.grain < type.costGrain || member.state != MemberState.Home) return state
        val expedition = Expedition(typeId, memberId, type.duration, state.year, state.month)
        val members = state.members.map { if (it.id == memberId) it.copy(state = MemberState.Expedition) else it }
        return addLog(state.copy(silver = state.silver - type.costSilver, grain = state.grain - type.costGrain, members = members, expeditions = state.expeditions + expedition), "${member.name}出发进行【${type.name}】。")
    }

    fun takeExam(state: GameState, memberId: Int, examId: String): GameState {
        val exam = GameContent.exam(examId) ?: return state
        val member = state.members.firstOrNull { it.id == memberId && it.alive } ?: return state
        if (member.study < exam.reqStudy) return state
        val pass = Random.nextDouble() < (exam.passRate + combinedEffects(state).examPassBonus).coerceAtMost(0.95)
        val members = if (pass) state.members.map { if (it.id == memberId) it.copy(identity = exam.result, state = MemberState.Home) else it } else state.members
        val next = if (pass) state.copy(members = members, fame = state.fame + exam.famePlus, totalExamPasses = state.totalExamPasses + 1) else state.copy(members = members)
        return addLog(next, if (pass) "${member.name}参加${exam.name}高中【${exam.result}】！" else "${member.name}参加${exam.name}落榜。")
    }

    fun donateIdentity(state: GameState, memberId: Int): GameState {
        val member = state.members.firstOrNull { it.id == memberId && it.alive } ?: return state
        if (state.clanRank < 4 || state.silver < 500 || state.fame < 15) return state
        val members = state.members.map { if (it.id == member.id) it.copy(identity = "监生") else it }
        return addLog(state.copy(silver = state.silver - 500, fame = state.fame - 15, members = members), "${member.name}纳捐入监，得监生身份。")
    }

    fun appointOfficial(state: GameState, memberId: Int, rankId: String): GameState {
        val rank = GameContent.officialRank(rankId) ?: return state
        val member = state.members.firstOrNull { it.id == memberId && it.alive } ?: return state
        if (member.identity != rank.reqIdentity) return state
        val members = state.members.map { if (it.id == memberId) it.copy(identity = rank.name, state = MemberState.Official, officialRank = rank.id, officialTenure = 0) else it }
        return addLog(state.copy(members = members), "${member.name}入仕为官，授${rank.name}。")
    }

    fun arrangeMarriage(state: GameState, memberId: Int, tierId: String): GameState {
        val person = state.members.firstOrNull { it.id == memberId && it.alive && it.spouseId == null } ?: return state
        val tier = GameContent.marriageTier(tierId) ?: return state
        if (state.clanRank < tier.unlockRank || state.silver < tier.silverCost || state.grain < tier.grainCost || state.fame < tier.fameReq) return state
        val spouseGender = if (person.gender == Gender.Male) Gender.Female else Gender.Male
        val spouseId = state.nextMemberId
        val baseStudy = Random.nextInt(8, 25) + if (tier.bonusType == "study" || tier.bonusType == "all") tier.bonusValue else 0
        val baseMartial = Random.nextInt(8, 25) + if (tier.bonusType == "martial" || tier.bonusType == "all") tier.bonusValue else 0
        val spouse = ClanMember(
            id = spouseId,
            name = state.surname + randomGivenName(spouseGender),
            gender = spouseGender,
            age = (person.age + Random.nextInt(-3, 4)).coerceIn(16, 42),
            generation = person.generation,
            spouseId = person.id,
            study = baseStudy.coerceAtMost(100),
            martial = baseMartial.coerceAtMost(100),
            health = Random.nextInt(70, 101),
            aptitude = randomAptitudeSet(),
            talentId = rollTalentId()
        )
        val members = state.members.map { if (it.id == person.id) it.copy(spouseId = spouseId) else it } + spouse
        var next = state.copy(
            silver = state.silver - tier.silverCost,
            grain = state.grain - tier.grainCost,
            fame = state.fame + tier.famePlus,
            members = members,
            nextMemberId = spouseId + 1
        )
        if (tier.id == "warlord") {
            next = next.copy(army = next.army.copy(infantry = next.army.infantry + 50))
        }
        return addLog(next, "${person.name}与${tier.name}联姻，迎娶/嫁入${spouse.name}。")
    }

    fun recruitArmy(state: GameState, infantry: Int, archers: Int): GameState {
        val cost = infantry * 2 + archers * 3
        val grainCost = infantry + archers
        if (infantry < 0 || archers < 0 || state.silver < cost || state.grain < grainCost) return state
        return addLog(state.copy(silver = state.silver - cost, grain = state.grain - grainCost, army = state.army.copy(infantry = state.army.infantry + infantry, archers = state.army.archers + archers)), "招募步兵${infantry}、弓兵${archers}。")
    }

    fun trainArmy(state: GameState): GameState {
        if (state.army.trainingLevel >= 5) return state
        val cost = 100 * (state.army.trainingLevel + 1)
        if (state.silver < cost) return state
        return addLog(state.copy(silver = state.silver - cost, army = state.army.copy(trainingLevel = state.army.trainingLevel + 1)), "操练军队，训练等级提升至${state.army.trainingLevel + 1}。")
    }

    fun attackStage(state: GameState, stageId: Int): GameState {
        val stage = GameContent.campaignStages.firstOrNull { it.id == stageId } ?: return state
        if (state.conqueredStages.contains(stageId)) return state
        val power = state.army.infantry + state.army.archers * 2 + state.army.trainingLevel * 50 + adults(state).filter { it.state == MemberState.Military }.sumOf { it.martial }
        val win = power >= stage.enemyPower || Random.nextDouble() < (power.toDouble() / (stage.enemyPower * 1.5)).coerceIn(0.05, 0.85)
        val lossRatio = if (win) 0.15 else 0.35
        val nextArmy = state.army.copy(infantry = max(0, (state.army.infantry * (1 - lossRatio)).toInt()), archers = max(0, (state.army.archers * (1 - lossRatio)).toInt()))
        val next = if (win) state.copy(army = nextArmy, silver = state.silver + stage.rewardSilver, fame = state.fame + stage.rewardFame, conqueredStages = state.conqueredStages + stageId, totalMilitaryMerits = state.totalMilitaryMerits + 1) else state.copy(army = nextArmy)
        return addLog(next, if (win) "征伐【${stage.name}】获胜，银两+${stage.rewardSilver}，声望+${stage.rewardFame}。" else "征伐【${stage.name}】失利，士卒折损。")
    }

    fun adoptPet(state: GameState, type: String): GameState {
        if (state.pet?.alive == true || state.grain < 20) return state
        val name = when (type) {
            "cat" -> "狸奴"
            "goose" -> "大白"
            else -> "大黄"
        }
        return addLog(state.copy(grain = state.grain - 20, pet = PetState(type, name, state.year, state.month, 12 + Random.nextInt(0, 6))), "宗族收养了$name，平添几分烟火气。")
    }

    fun sacrifice(state: GameState): GameState {
        if (state.lastSacrificeYear == state.year || state.grain < 50) return state
        val roll = Random.nextInt(100)
        val next = when {
            roll < 35 -> state.copy(grain = state.grain - 50, fame = state.fame + 8)
            roll < 70 -> state.copy(grain = state.grain - 50, silver = state.silver + 40)
            else -> state.copy(grain = state.grain - 50, inventory = addItem(state.inventory, GameContent.itemTypes.random().id, 1))
        }.copy(lastSacrificeYear = state.year)
        return addLog(next, "祭天祈福完成，族人心有所安。")
    }

    fun advanceMonth(state: GameState): Pair<GameState, MonthlyReport> {
        var working = ensureRuntimeState(state)
        val report = MutableReport()
        val effects = combinedEffects(working)
        val startingSnapshot = working.yearStartSnapshot ?: snapshotOf(working)
        if (working.yearStartSnapshot == null) working = working.copy(yearStartSnapshot = startingSnapshot)

        working = processWeather(working, report)
        val industryResult = processIndustries(working, report, effects)
        working = industryResult.first
        report.industryEffects = industryResult.second
        processMerchants(working, report, effects)
        processLabor(working, report)
        processConsumption(working, report, effects)
        working = processArmyMaintenance(working, report)
        processTax(working, report)
        working = processGrowth(working, report, effects)
        working = processGuaranteedMarriage(working, report)
        working = processEncounters(working, report, effects)
        working = processCareer(working, report, effects)
        working = processAcademies(working, report, effects)
        working = processExpeditions(working, report, effects)
        working = processPetAging(working, report)
        working = processClinicDoctor(working, report)
        working = processRandomEvents(working, report, effects)
        working = processMarket(working)

        var silver = working.silver + report.incomeSilver - report.expenseSilver
        var grain = working.grain + report.incomeGrain - report.expenseGrain
        var cloth = working.cloth + report.incomeCloth - report.expenseCloth
        var fame = working.fame + report.incomeFame - report.expenseFame
        var totalDeaths = working.totalDeaths
        var members = working.members

        if (grain <= 0) {
            report.events += "粮食耗尽，族人健康下降。"
            members = members.map { member ->
                if (!member.alive) member else {
                    val health = member.health - Random.nextInt(if (working.totalMonths < 36) 2 else 3, if (working.totalMonths < 36) 6 else 10)
                    if (health <= 0 && aliveMembers(working.copy(members = members)).size > 1) {
                        totalDeaths += 1
                        report.events += "${member.name}因饥饿病逝。"
                        member.copy(health = 0, alive = false, state = MemberState.Dead, deathYear = working.year, deathAge = member.age, deathCause = "饥饿")
                    } else member.copy(health = max(1, health))
                }
            }
        }

        silver = max(-999999, silver)
        grain = max(-999999, grain)
        cloth = max(-999999, cloth)
        fame = max(-999999, fame)

        val reportYear = working.year
        val reportMonth = working.month
        var nextMonth = if (working.month == 12) 1 else working.month + 1
        var nextYear = if (working.month == 12) working.year + 1 else working.year
        val yearEnded = working.month == 12
        if (yearEnded) report.events += "${working.year}年终结，宗族延续至${nextYear}年。"

        working = working.copy(
            year = nextYear,
            month = nextMonth,
            totalMonths = working.totalMonths + 1,
            silver = silver,
            grain = grain,
            cloth = cloth,
            fame = fame,
            members = members,
            totalDeaths = totalDeaths
        )

        if (yearEnded) {
            working = checkYearlyGoals(working, startingSnapshot, report)
            working = generateYearlyGoals(working.copy(yearStartSnapshot = null, clinicHealsThisYear = 0))
            working = processHistoryEvents(working, report)
        }
        working = checkAchievements(working, report)
        working = checkEndings(working, report)

        if (report.events.isEmpty()) report.events += "本月平安无事。"
        report.events.forEach { working = addLog(working, it) }
        return working to MonthlyReport(
            year = reportYear,
            month = reportMonth,
            incomeSilver = report.incomeSilver,
            incomeGrain = report.incomeGrain,
            incomeCloth = report.incomeCloth,
            incomeFame = report.incomeFame,
            expenseSilver = report.expenseSilver,
            expenseGrain = report.expenseGrain,
            expenseCloth = report.expenseCloth,
            expenseFame = report.expenseFame,
            events = report.events
        )
    }

    private fun processWeather(state: GameState, report: MutableReport): GameState {
        val chance = GameContent.region(state.regionId).disasterRate * GameContent.difficulty(state.difficultyId).disasterMul / 18.0
        if (Random.nextDouble() >= chance) return state
        return when (Random.nextInt(3)) {
            0 -> {
                val loss = max(5, floor(state.grain * Random.nextDouble(0.08, 0.18)).toInt())
                report.expenseGrain += loss
                report.events += "本月遭遇灾荒，粮食损失$loss。"
                state
            }
            1 -> {
                val loss = max(5, floor(state.grain * Random.nextDouble(0.10, 0.22)).toInt())
                val silverLoss = max(3, floor(state.silver * 0.04).toInt())
                report.expenseGrain += loss
                report.expenseSilver += silverLoss
                report.events += "水灾泛滥，田产受损，粮食-$loss，银两-$silverLoss。"
                state
            }
            else -> {
                val infected = aliveMembers(state).filter { Random.nextDouble() < 0.10 }
                if (infected.isEmpty()) return state
                val infectedIds = infected.map { it.id }.toSet()
                report.events += "瘟疫蔓延，${infected.size}名族人染病。"
                state.copy(members = state.members.map { member ->
                    if (member.id in infectedIds) {
                        member.copy(
                            health = max(10, member.health - Random.nextInt(15, 36)),
                            prevState = if (member.state == MemberState.Sick) member.prevState else member.state,
                            state = MemberState.Sick
                        )
                    } else member
                })
            }
        }
    }

    private fun processIndustries(state: GameState, report: MutableReport, effects: RuleEffects): Pair<GameState, IndustryEffects> {
        val typeCount = state.industries.groupingBy { it.typeId }.eachCount()
        var reduceSick = false
        var horseRanch = false
        var smithyBonus = 0
        var bookshopBonus = 0
        state.industries.forEach { industry ->
            val type = GameContent.industry(industry.typeId) ?: return@forEach
            when (type.specialEffect) {
                "reduce_sick_15" -> reduceSick = true
                "military_boost_15", "military_boost_25" -> horseRanch = true
                "martial_grow_10" -> smithyBonus += 1
                "study_grow_10" -> bookshopBonus += 1
            }
            if (type.resource != ResourceKind.None) addIncome(report, type.resource, calcIndustryOutput(state, industry, type, type.resource, type.baseOutput, effects, typeCount[industry.typeId] ?: 1))
            type.resource2?.let { addIncome(report, it, calcIndustryOutput(state, industry, type, it, type.baseOutput2, effects, typeCount[industry.typeId] ?: 1)) }
            type.resource3?.let { addIncome(report, it, calcIndustryOutput(state, industry, type, it, type.baseOutput3, effects, typeCount[industry.typeId] ?: 1)) }
            when (type.specialEffect) {
                "consume_grain_3" -> report.expenseGrain += 3
                "consume_fame_3" -> report.expenseFame += 3
                "interest_1pct" -> report.incomeSilver += min(150, floor(state.silver * 0.01).toInt())
                "interest_2pct" -> report.incomeSilver += min(250, floor(state.silver * 0.02).toInt())
                "risk_loss_20" -> if (Random.nextDouble() < 0.20) {
                    val lost = max(1, type.baseOutput * industry.level / 2)
                    report.incomeSilver -= lost
                    report.events += "${type.name}押镖途中遭劫，损失银两$lost。"
                }
                "risk_tax_30" -> if (Random.nextDouble() < 0.10) {
                    val fine = max(1, type.baseOutput * industry.level / 2)
                    report.incomeSilver -= fine
                    report.events += "${type.name}被官府稽查，罚没银两$fine。"
                }
            }
        }
        val maintenance = state.industries.sumOf { industry ->
            val type = GameContent.industry(industry.typeId)
            if (type == null || type.resource == ResourceKind.None) 0 else floor(sqrt(type.cost.toDouble()) * 0.3 * (1 + ln(industry.level.toDouble()) * 0.5)).toInt()
        }
        if (maintenance > 0) report.expenseSilver += maintenance
        return state to IndustryEffects(reduceSick, horseRanch, smithyBonus, bookshopBonus)
    }

    private fun calcIndustryOutput(state: GameState, industry: ClanIndustry, type: IndustryType, resource: ResourceKind, baseOutput: Int, effects: RuleEffects, sameCount: Int): Int {
        if (baseOutput <= 0) return 0
        val manager = industry.assignedMemberId?.let { id -> state.members.firstOrNull { it.id == id && it.alive } }
        var manageMul = when {
            manager == null -> 1.0
            manager.state == MemberState.Home || manager.state == MemberState.Sick -> 1.30
            else -> 1.15
        }
        GameContent.talent(manager?.talentId)?.let { talent ->
            manageMul *= when {
                talent.id == "diligent" -> 1.15
                talent.id == "merchant" && resource == ResourceKind.Silver -> 1.25
                talent.id == "lazy" -> 0.80
                else -> 1.0
            }
        }
        var mul = 1.0 + effects.allOutputMul
        if (sameCount >= 3) mul += (sameCount - 2) * 0.08
        if (industry.level >= 5) mul += 0.30 else if (industry.level >= 3) mul += 0.15
        if (resource == ResourceKind.Grain) mul += effects.grainOutputMul
        seasonFactors[resource]?.let { mul *= it[state.month - 1] }
        if (type.specialEffect == "season_amplify" && resource == ResourceKind.Silver && state.month in listOf(4, 8, 10, 11, 12)) mul *= 1.25
        val output = baseOutput * (1 + ln(industry.level.toDouble()) * 1.5) * manageMul * mul
        return max(1, floor(output).toInt())
    }

    private fun processMerchants(state: GameState, report: MutableReport, effects: RuleEffects) {
        adults(state).filter { it.state == MemberState.Trade }.forEach { member ->
            var income = Random.nextInt(5, 13)
            GameContent.talent(member.talentId)?.let { talent ->
                income = floor(income * (1.0 + talent.tradeBonus + talent.outputBonus)).toInt()
            }
            income = floor(income * (1.0 + effects.tradeIncomeMul)).toInt().coerceAtLeast(0)
            report.incomeSilver += income
            if (Random.nextDouble() < 0.07) {
                val bonus = Random.nextInt(10, 41)
                report.incomeSilver += bonus
                report.events += "${member.name}发现一笔好买卖，额外获银$bonus。"
            }
        }
    }

    private fun processLabor(state: GameState, report: MutableReport) {
        adults(state).filter { it.state == MemberState.Labor }.forEach { member ->
            report.incomeSilver += GameContent.laborJob(member.laborJob)?.wage ?: (5 + state.clanRank * 2)
        }
    }

    private fun processConsumption(state: GameState, report: MutableReport, effects: RuleEffects) {
        var grainCost = aliveMembers(state).sumOf { if (it.age <= 5) 1 else if (it.age <= 14) 2 else 3 }
        grainCost = max(1, floor(grainCost * (1.0 + effects.grainConsumeMul)).toInt())
        if (state.pet?.alive == true) grainCost += 1
        report.expenseGrain += grainCost
        if (state.month % 3 == 0) report.expenseCloth += max(0, floor(ceil(aliveMembers(state).size * 0.5) * (1.0 + effects.clothConsumeMul)).toInt())
        if (effects.fameDrain < 0) report.expenseFame += -effects.fameDrain
    }

    private fun processArmyMaintenance(state: GameState, report: MutableReport): GameState {
        val total = state.army.infantry + state.army.archers
        if (total <= 0) return state
        val silverCost = ceil(total / 1000.0 * 1200).toInt()
        val grainCost = ceil(total / 1000.0 * 1500).toInt()
        if (state.silver >= silverCost && state.grain >= grainCost) {
            report.expenseSilver += silverCost
            report.expenseGrain += grainCost
            return state
        }
        val supportBySilver = if (state.silver > 0) floor(state.silver / 1200.0 * 1000).toInt() else 0
        val supportByGrain = if (state.grain > 0) floor(state.grain / 1500.0 * 1000).toInt() else 0
        val support = min(supportBySilver, supportByGrain).coerceIn(0, total)
        val actualSilver = min(max(0, state.silver), ceil(support / 1000.0 * 1200).toInt())
        val actualGrain = min(max(0, state.grain), ceil(support / 1000.0 * 1500).toInt())
        report.expenseSilver += actualSilver
        report.expenseGrain += actualGrain
        val ratio = if (total == 0) 0.0 else support.toDouble() / total
        val deserted = ((total - support) / 100) * 100
        if (deserted > 0) report.events += "军粮不足，${deserted}名士兵逃散。"
        return state.copy(army = state.army.copy(infantry = floor(state.army.infantry * ratio).toInt(), archers = floor(state.army.archers * ratio).toInt()))
    }

    private fun processTax(state: GameState, report: MutableReport) {
        if (state.month % 3 != 0) return
        var taxRate = region(state).taxRate * difficulty(state).taxMul * taxModifier(state.year)
        val reduce = state.members.mapNotNull { GameContent.officialRank(it.officialRank)?.taxReduce }.maxOrNull() ?: 0.0
        taxRate *= (1.0 - reduce)
        val taxSilver = ceil(state.silver * taxRate * 0.3).toInt()
        val taxGrain = ceil(report.incomeGrain * taxRate).toInt()
        report.expenseSilver += taxSilver
        report.expenseGrain += taxGrain
        if (taxSilver + taxGrain > 0) report.events += "官府征税：银两$taxSilver、粮食$taxGrain。"
    }

    private fun processGrowth(state: GameState, report: MutableReport, effects: RuleEffects): GameState {
        var next = state
        var totalBirths = next.totalBirths
        var totalDeaths = next.totalDeaths
        var members = next.members.map { member ->
            if (!member.alive) return@map member
            var m = member
            if (next.month == 1) {
                m = m.copy(age = m.age + 1)
                if (m.age in 3..30) {
                    val rankCap = rankCap(next.clanRank)
                    val study = if (Random.nextDouble() < ageGainChance(m.age)) min(capFor(next, m, "study", rankCap), m.study + 1) else m.study
                    val martial = if (Random.nextDouble() < ageGainChance(m.age)) min(capFor(next, m, "martial", rankCap), m.martial + 1) else m.martial
                    m = m.copy(study = study, martial = martial)
                }
            }
            if (m.age >= 60) {
                val deathRate = ((m.age - 55) * 0.005) * if (m.health < 30) 2.0 else 1.0
                if (Random.nextDouble() < deathRate && aliveMembers(next).size > 1) {
                    totalDeaths += 1
                    report.events += "${m.name}寿终正寝，享年${m.age}岁。"
                    return@map m.copy(alive = false, state = MemberState.Dead, deathYear = next.year, deathAge = m.age, deathCause = "寿终正寝")
                }
            }
            if (m.age <= 2) {
                val deathRate = if (m.health < 50) 0.015 else 0.008
                if (Random.nextDouble() < deathRate && aliveMembers(next).size > 1) {
                    totalDeaths += 1
                    report.events += "${m.name}不幸夭折。"
                    return@map m.copy(alive = false, state = MemberState.Dead, deathYear = next.year, deathAge = m.age, deathCause = "夭折")
                }
            }
            val talent = GameContent.talent(m.talentId)
            var sickRate = 0.02 + (talent?.sickRate ?: 0.0)
            if (m.age >= 50) sickRate += 0.02
            if (next.totalMonths < 36) sickRate *= 0.5
            if (aliveMembers(next).size <= 1) sickRate *= 0.3
            if (report.industryEffects.reduceSick) sickRate *= 0.85
            if (m.age > 2 && m.state != MemberState.Sick && Random.nextDouble() < sickRate) {
                m = m.copy(health = max(0, m.health - Random.nextInt(3, 11)), prevState = m.state, state = MemberState.Sick)
                report.events += "${m.name}染病，健康下降。"
            }
            if (m.state == MemberState.Sick) {
                val health = min(100, m.health + Random.nextInt(5, 13))
                m = if (health >= 60) m.copy(health = health, state = m.prevState ?: MemberState.Home, prevState = null) else m.copy(health = health)
                if (m.health <= 10 && Random.nextDouble() < if (aliveMembers(next).size <= 1) 0.03 else 0.08) {
                    totalDeaths += 1
                    report.events += "${m.name}久病不愈，抱憾离世。"
                    return@map m.copy(alive = false, state = MemberState.Dead, deathYear = next.year, deathAge = m.age, deathCause = "病逝")
                }
            }
            if (m.state == MemberState.Study && m.age >= 6) {
                val gain = diminishedGain(m.study, if (Random.nextDouble() < 0.10 + (talent?.studyBonus ?: 0.0) * 0.2) 1 else 0, effects.studyGrowthMul)
                m = m.copy(study = min(capFor(next, m, "study"), m.study + gain))
            }
            if (m.gender == Gender.Male && m.age >= 10) {
                val martialMul = effects.martialGrowthMul + report.industryEffects.smithyBonus * 0.10
                val gain = if (martialMul > 0 && Random.nextDouble() < 0.10) diminishedGain(m.martial, 1, martialMul) else 0
                m = m.copy(martial = min(capFor(next, m, "martial"), m.martial + gain))
            }
            m
        }
        next = next.copy(members = members, totalDeaths = totalDeaths)
        val births = mutableListOf<ClanMember>()
        var nextMemberId = next.nextMemberId
        next.members.filter { it.alive && it.gender == Gender.Female && it.age in 18..40 && it.spouseId != null && it.state != MemberState.Sick }.forEach { mother ->
            val spouse = next.members.firstOrNull { it.id == mother.spouseId && it.alive } ?: return@forEach
            if (spouse.state == MemberState.Military || spouse.state == MemberState.Battle) return@forEach
            val childCount = spouse.childrenIds.size
            if (childCount >= 3) return@forEach
            var chance = 0.08
            GameContent.talent(mother.talentId)?.let { chance += it.fertilityBonus }
            GameContent.talent(spouse.talentId)?.let { chance += it.fertilityBonus * 0.6 }
            chance -= childCount * 0.03
            if (mother.age >= 35) chance *= 0.6
            if (Random.nextDouble() < chance.coerceAtLeast(0.02)) {
                val birthCount = if (childCount <= 1 && Random.nextDouble() < 0.05) 2 else 1
                repeat(min(birthCount, 3 - childCount)) {
                    val gender = if (Random.nextBoolean()) Gender.Male else Gender.Female
                    val child = ClanMember(
                        id = nextMemberId++,
                        name = next.surname + randomGivenName(gender),
                        gender = gender,
                        age = 0,
                        generation = max(mother.generation, spouse.generation) + 1,
                        parentId = spouse.id,
                        health = Random.nextInt(60, 101),
                        aptitude = randomAptitudeSet(),
                        talentId = rollTalentId(0.18)
                    )
                    births += child
                    totalBirths += 1
                    report.incomeFame += 1
                    report.events += "${mother.name}诞下${if (gender == Gender.Male) "一子" else "一女"}，取名${child.name}。"
                }
            }
        }
        if (births.isNotEmpty()) {
            val childIdsByParent = births.groupBy { it.parentId }.mapValues { it.value.map { child -> child.id } }
            members = next.members.map { m -> childIdsByParent[m.id]?.let { m.copy(childrenIds = m.childrenIds + it) } ?: m } + births
            next = next.copy(members = members, nextMemberId = nextMemberId, totalBirths = totalBirths)
        }
        if (next.month == 1 && aliveMembers(next).size < 6 && Random.nextDouble() < 0.35) {
            val recruit = randomMember(next, 1)
            next = next.copy(members = next.members + recruit, nextMemberId = next.nextMemberId + 1)
            report.events += "流民${recruit.name}投奔宗族。"
        }
        return next
    }

    private fun processGuaranteedMarriage(state: GameState, report: MutableReport): GameState {
        val members = aliveMembers(state)
        val maxGeneration = members.maxOfOrNull { it.generation } ?: return state
        if (maxGeneration <= 1) return state
        val generationMembers = members.filter { it.generation == maxGeneration }
        if (generationMembers.isEmpty()) return state
        if (generationMembers.any { it.spouseId != null } || generationMembers.any { it.age < 18 }) return state
        val candidate = generationMembers.filter { it.spouseId == null && it.age in 16..40 }.randomOrNull() ?: return state
        val tier = GameContent.marriageTier("common") ?: return state
        if (state.silver < tier.silverCost || state.grain < tier.grainCost || state.fame < tier.fameReq) {
            report.events += "族中长辈催促${candidate.name}的婚事。"
            return state
        }
        val next = arrangeMarriage(state, candidate.id, tier.id)
        if (next != state) report.events += "族中长辈为${candidate.name}张罗婚事，香火得续。"
        return next
    }

    private fun processEncounters(state: GameState, report: MutableReport, effects: RuleEffects): GameState {
        var next = state
        next.members.forEach { member ->
            if (!member.alive) return@forEach
            when {
                member.state == MemberState.Study && member.study >= 30 && Random.nextDouble() < 0.06 -> {
                    val gain = diminishedGain(member.study, Random.nextInt(3, 9), effects.studyGrowthMul)
                    next = next.copy(members = next.members.map { if (it.id == member.id) it.copy(study = min(capFor(next, it, "study"), it.study + gain)) else it })
                    report.events += "${member.name}读书忽有所悟，学识+$gain。"
                }
                member.state == MemberState.Trade && Random.nextDouble() < 0.07 -> {
                    val bonus = Random.nextInt(15, 41)
                    next = next.copy(silver = next.silver + bonus)
                    report.events += "${member.name}结识豪商，额外获银$bonus。"
                }
                member.state == MemberState.Military && Random.nextDouble() < 0.05 -> {
                    val fameGain = Random.nextInt(5, 13)
                    next = next.copy(fame = next.fame + fameGain, totalMilitaryMerits = next.totalMilitaryMerits + 1)
                    report.events += "${member.name}在军中立功，声望+$fameGain。"
                }
            }
        }
        return next
    }

    private fun processCareer(state: GameState, report: MutableReport, effects: RuleEffects): GameState {
        var next = state
        next.members.forEach { member ->
            if (!member.alive) return@forEach
            when (member.identity) {
                "秀才" -> report.incomeFame += 2
                "举人" -> { report.incomeFame += 5; report.incomeSilver += 2 }
                "进士", "监生" -> { report.incomeFame += if (member.identity == "进士") 10 else 2; report.incomeSilver += if (member.identity == "进士") 5 else 0 }
            }
            if (member.state == MemberState.Official) {
                val rank = GameContent.officialRank(member.officialRank)
                if (rank != null) {
                    report.incomeSilver += rank.silver
                    report.incomeFame += rank.fame + rank.famePerMonth
                    if (Random.nextDouble() < rank.demotionRate) {
                        next = next.copy(members = next.members.map { if (it.id == member.id) it.copy(state = MemberState.Home, officialRank = null, officialTenure = 0) else it })
                        report.events += "${member.name}因朝廷变故，被免去${rank.name}之职。"
                    }
                }
            }
            if (member.state == MemberState.Military) {
                val rank = GameContent.militaryRank(member.militaryRank)
                report.incomeSilver += rank.silverPay
                var deathRate = rank.deathRate * deathRateModifier(next.year) * (1.0 - effects.militarySurvival)
                if (GameContent.talent(member.talentId)?.id == "martial") deathRate *= 0.8
                if (report.industryEffects.horseRanch) deathRate *= 0.85
                if (Random.nextDouble() < deathRate && aliveMembers(next).size > 1) {
                    next = next.copy(members = next.members.map { if (it.id == member.id) it.copy(alive = false, state = MemberState.Dead, deathYear = next.year, deathAge = it.age, deathCause = "战死沙场") else it }, totalDeaths = next.totalDeaths + 1, fame = next.fame + 5)
                    report.events += "${member.name}在战场上英勇牺牲。"
                } else if (Random.nextDouble() < 0.03) {
                    val idx = GameContent.militaryRanks.indexOfFirst { it.name == rank.name }
                    if (idx >= 0 && idx < GameContent.militaryRanks.lastIndex) {
                        val nextRank = GameContent.militaryRanks[idx + 1]
                        next = next.copy(members = next.members.map { if (it.id == member.id) it.copy(militaryRank = nextRank.name, identity = nextRank.name) else it }, fame = next.fame + nextRank.famePlus, totalMilitaryMerits = next.totalMilitaryMerits + 1)
                        report.events += "${member.name}军功卓著，升任${nextRank.name}。"
                    }
                }
            }
        }
        return next
    }

    private fun processAcademies(state: GameState, report: MutableReport, effects: RuleEffects): GameState {
        var members = state.members
        state.academies.forEach { academy ->
            val type = GameContent.academy(academy.typeId) ?: return@forEach
            academy.memberIds.forEach { mid ->
                members = members.map { member ->
                    if (member.id != mid || !member.alive) member else {
                        if (type.attribute == "study") member.copy(study = min(capFor(state, member, "study"), member.study + max(1, floor((2 + academy.level) * (1.0 + effects.studyGrowthMul)).toInt())))
                        else member.copy(martial = min(capFor(state, member, "martial"), member.martial + max(1, floor((2 + academy.level) * (1.0 + effects.martialGrowthMul)).toInt())))
                    }
                }
            }
        }
        return state.copy(members = members)
    }

    private fun processExpeditions(state: GameState, report: MutableReport, effects: RuleEffects): GameState {
        var next = state
        val remaining = mutableListOf<Expedition>()
        var members = next.members
        next.expeditions.forEach { exp ->
            val type = GameContent.expedition(exp.typeId)
            val nextExp = exp.copy(monthsLeft = exp.monthsLeft - 1)
            if (type == null || nextExp.monthsLeft > 0) {
                remaining += nextExp
            } else {
                val member = members.firstOrNull { it.id == exp.memberId }
                if (member != null && member.alive) {
                    var risk = type.riskRate
                    if (member.martial >= 30) risk *= 0.7
                    if (member.martial >= 60) risk *= 0.5
                    risk *= (1.0 - effects.militarySurvival)
                    if (Random.nextDouble() < risk) {
                        members = members.map { if (it.id == member.id) it.copy(health = max(10, it.health - Random.nextInt(10, 31)), prevState = it.state, state = MemberState.Sick) else it }
                        report.events += "${member.name}历练遭遇${type.riskDesc}，负伤归来。"
                    } else {
                        report.incomeSilver += type.rewardSilver?.let { Random.nextInt(it.first, it.last + 1) } ?: 0
                        report.incomeCloth += type.rewardCloth?.let { Random.nextInt(it.first, it.last + 1) } ?: 0
                        report.incomeFame += type.rewardFame?.let { Random.nextInt(it.first, it.last + 1) } ?: 0
                        val studyReward = type.rewardStudy?.let { Random.nextInt(it.first, it.last + 1) } ?: 0
                        val martialReward = type.rewardMartial?.let { Random.nextInt(it.first, it.last + 1) } ?: 0
                        members = members.map { if (it.id == member.id) it.copy(state = MemberState.Home, study = min(capFor(next, it, "study"), it.study + studyReward), martial = min(capFor(next, it, "martial"), it.martial + martialReward)) else it }
                        if (type.itemChance > 0 && Random.nextDouble() < type.itemChance) next = next.copy(inventory = addItem(next.inventory, GameContent.itemTypes.random().id, 1))
                        if (type.recruitChance > 0 && Random.nextDouble() < type.recruitChance) {
                            val recruit = randomMember(next, 1)
                            members = members + recruit
                            next = next.copy(nextMemberId = next.nextMemberId + 1)
                        }
                        report.events += "${member.name}完成【${type.name}】，满载而归。"
                    }
                }
            }
        }
        return next.copy(members = members, expeditions = remaining)
    }

    private fun processPetAging(state: GameState, report: MutableReport): GameState {
        val pet = state.pet ?: return state
        if (!pet.alive) return state
        val ageMonths = (state.year - pet.adoptYear) * 12 + (state.month - pet.adoptMonth)
        if (ageMonths < pet.lifespan * 12) return state
        report.events += "家犬「${pet.name}」年老体衰，安详离世。"
        return state.copy(
            pet = pet.copy(alive = false, deathYear = state.year, deathMonth = state.month),
            fame = state.fame + 1
        )
    }

    private fun processClinicDoctor(state: GameState, report: MutableReport): GameState {
        if (state.month % 3 != 0 || state.clinicHealsThisYear >= 3) return state
        val doctor = state.members
            .filter { it.alive && it.gender == Gender.Female && it.study >= 60 && it.state == MemberState.Labor && it.laborJob == "physician" }
            .maxByOrNull { it.study }
            ?: return state
        val patient = state.members
            .filter { it.alive && it.id != doctor.id && it.health < 30 }
            .minByOrNull { it.health }
            ?: return state
        val healAmount = Random.nextInt(20, 36) + if (doctor.study >= 80) Random.nextInt(5, 11) else 0
        val newHealth = min(100, patient.health + healAmount)
        val members = state.members.map { member ->
            if (member.id == patient.id) {
                member.copy(
                    health = newHealth,
                    state = if (member.state == MemberState.Sick && newHealth >= 60) member.prevState ?: MemberState.Home else member.state,
                    prevState = if (member.state == MemberState.Sick && newHealth >= 60) null else member.prevState
                )
            } else member
        }
        report.events += "坐堂郎中${doctor.name}为${patient.name}把脉施药，健康恢复+${newHealth - patient.health}。"
        return state.copy(members = members, clinicHealsThisYear = state.clinicHealsThisYear + 1)
    }

    private fun processRandomEvents(state: GameState, report: MutableReport, effects: RuleEffects): GameState {
        var next = state
        if (Random.nextDouble() > 0.28) return next
        val alive = aliveMembers(next)
        if (alive.isEmpty()) return next
        when (Random.nextInt(10)) {
            0 -> {
                val victim = alive.random()
                next = next.copy(members = next.members.map { if (it.id == victim.id) it.copy(health = max(10, it.health - Random.nextInt(15, 31)), prevState = it.state, state = MemberState.Sick) else it })
                report.events += "族人染疫：${victim.name}卧床不起。"
            }
            1 -> if (alive.size > 6) {
                val loss = Random.nextInt(5, 16)
                next = next.copy(silver = next.silver - loss, fame = next.fame - 2)
                report.events += "分家争产，损失银两$loss，声望受损。"
            }
            2 -> if (adults(next).any { it.spouseId == null }) {
                val person = adults(next).filter { it.spouseId == null }.random()
                report.events += "媒人登门，为${person.name}说亲。"
            }
            3 -> if (next.fame >= 30 && next.industries.size < alive.size) {
                val type = GameContent.industry("dry_field")
                val limit = GameContent.industryLimitByRank[next.clanRank] ?: 4
                if (type != null && next.clanRank >= type.unlockRank && next.industries.size < limit) {
                    next = next.copy(
                        industries = next.industries + ClanIndustry(next.nextIndustryId, "dry_field"),
                        nextIndustryId = next.nextIndustryId + 1
                    )
                    report.events += "乡人仰慕宗族声望，献田一亩。"
                }
            }
            4 -> if (adults(next).any { it.state == MemberState.Trade }) {
                val bonus = Random.nextInt(20, 51)
                next = next.copy(silver = next.silver + bonus)
                report.events += "经商族人做成大买卖，额外获得银两$bonus。"
            }
            5 -> {
                val loss = max(3, floor(next.silver * Random.nextDouble(0.05, 0.15)).toInt())
                val resist = (effects.banditResist + next.fortCount * 0.1).coerceAtMost(0.6)
                if (Random.nextDouble() > resist) {
                    next = next.copy(silver = next.silver - loss)
                    report.events += "流寇劫掠乡里，宗族损失银两$loss。"
                } else report.events += "流寇来犯，被宗族寨堡与乡勇击退。"
            }
            6 -> if (next.fame >= 15) {
                val helpCost = 10
                if (next.silver >= helpCost) {
                    next = next.copy(silver = next.silver - helpCost, fame = next.fame + 5)
                    report.events += "乡邻求助，宗族慷慨相助，声望提升。"
                }
            }
            7 -> if (adults(next).any { it.state == MemberState.Study }) {
                val student = adults(next).filter { it.state == MemberState.Study }.random()
                next = next.copy(members = next.members.map { if (it.id == student.id) it.copy(study = min(capFor(next, it, "study"), it.study + 3)) else it })
                report.events += "${student.name}偶遇名师，学识精进。"
            }
            8 -> if (next.year >= 1627) {
                val tax = Random.nextInt(10, 26)
                next = next.copy(silver = next.silver - tax)
                report.events += "朝廷加征三饷，额外征银$tax。"
            }
            9 -> if (next.year >= 1630 && alive.size < 12) {
                val recruit = randomMember(next, 1)
                next = next.copy(members = next.members + recruit, nextMemberId = next.nextMemberId + 1)
                report.events += "难民${recruit.name}投奔宗族。"
            }
        }
        return next
    }

    private fun processMarket(state: GameState): GameState {
        if (state.clanRank < 3) return state
        val prices = GameContent.marketCommodities.associate { commodity ->
            val season = marketSeasonFactors[commodity.id]?.getOrElse(state.month - 1) { 1.0 } ?: 1.0
            val eventFactor = marketEventFactor(commodity.id, state.year, state.month, state)
            val random = 0.90 + Random.nextDouble() * 0.20
            val raw = commodity.basePrice * season * eventFactor * random
            val minPrice = commodity.basePrice * (1.0 - commodity.fluctRange)
            val maxPrice = commodity.basePrice * (1.0 + commodity.fluctRange)
            commodity.id to max(1, floor(raw.coerceIn(minPrice, maxPrice)).toInt())
        }
        val history = prices.mapValues { (id, price) -> ((state.market.history[id] ?: emptyList()) + price).takeLast(12) }
        return state.copy(market = state.market.copy(prices = prices, history = history, lastUpdatedMonth = state.totalMonths, monthlySpent = 0))
    }

    private fun processHistoryEvents(state: GameState, report: MutableReport): GameState {
        var next = state
        GameContent.historyEvents.filter { it.year == state.year && !state.triggeredHistoryYears.contains(it.year) }.forEach { event ->
            report.events += "【${event.title}】${event.desc}"
            next = next.copy(triggeredHistoryYears = next.triggeredHistoryYears + event.year)
            next = when (event.effect) {
                "era_start" -> next.copy(fame = next.fame + 15)
                "trade_boost" -> next.copy(silver = next.silver + 40)
                "tax_reform" -> next.copy(silver = next.silver + 20, grain = next.grain + 15)
                "political_chaos" -> next.copy(fame = next.fame - 10, silver = next.silver - 15)
                "war_civil" -> next.copy(silver = next.silver - ceil(next.silver * 0.15).toInt(), grain = next.grain - ceil(next.grain * 0.15).toInt())
                "great_famine" -> next.copy(grain = next.grain - ceil(next.grain * 0.4).toInt())
                "ending_year" -> next.copy(hiddenEnding = "survive_end")
                else -> next
            }
        }
        return next
    }

    private fun checkAchievements(state: GameState, report: MutableReport): GameState {
        var next = state
        GameContent.achievements.forEach { achievement ->
            if (next.unlockedAchievements.contains(achievement.id)) return@forEach
            val unlocked = when (achievement.id) {
                "pop_10" -> aliveMembers(next).size >= 10
                "pop_20" -> aliveMembers(next).size >= 20
                "silver_500" -> next.silver >= 500
                "fame_100" -> next.fame >= 100
                "first_xiucai" -> next.members.any { it.alive && it.identity in listOf("秀才", "举人", "进士") }
                "first_juren" -> next.members.any { it.alive && it.identity in listOf("举人", "进士") }
                "first_jinshi" -> next.members.any { it.alive && it.identity == "进士" }
                "first_bazong" -> next.members.any { it.alive && (it.militaryRank == "把总" || it.militaryRank == "守备") }
                "fort_3" -> next.fortCount >= 3
                "ind_10" -> next.industries.size >= 10
                "rank_wangzu" -> next.clanRank >= 4
                "survive_10y" -> next.totalMonths >= 120
                "births_10" -> next.totalBirths >= 10
                "exams_3" -> next.totalExamPasses >= 3
                "merits_5" -> next.totalMilitaryMerits >= 5
                else -> false
            }
            if (unlocked) {
                next = next.copy(
                    unlockedAchievements = next.unlockedAchievements + achievement.id,
                    silver = next.silver + achievement.rewardSilver,
                    grain = next.grain + achievement.rewardGrain,
                    cloth = next.cloth + achievement.rewardCloth,
                    fame = next.fame + achievement.rewardFame
                )
                report.events += "达成成就【${achievement.name}】：${achievement.desc}。"
            }
        }
        return next
    }

    private fun generateYearlyGoals(state: GameState): GameState {
        if (state.goalYear == state.year && state.yearlyGoals.isNotEmpty()) return state
        val candidates = GameContent.yearlyGoalPool.filter { state.clanRank >= it.minRank && !state.completedGoalIds.contains(it.id) }
        val chosen = candidates.shuffled().take(if (candidates.size >= 3) 3 else candidates.size).map { YearlyGoalState(it.id) }
        return state.copy(yearlyGoals = chosen, goalYear = state.year, yearStartSnapshot = snapshotOf(state))
    }

    private fun checkYearlyGoals(state: GameState, snapshot: YearSnapshot, report: MutableReport): GameState {
        var next = state
        next.yearlyGoals.filterNot { it.completed }.forEach { goalState ->
            val completed = when (goalState.goalId) {
                "earn_silver_30" -> next.silver - snapshot.silver >= 30
                "earn_grain_50" -> next.grain - snapshot.grain >= 50
                "birth_2" -> next.totalBirths - snapshot.totalBirths >= 2
                "martial_40" -> next.members.any { it.alive && it.martial >= 40 }
                "study_50" -> next.members.any { it.alive && it.study >= 50 }
                "exam_pass" -> next.totalExamPasses > snapshot.totalExamPasses
                "build_ind_2" -> next.industries.size - snapshot.industryCount >= 2
                "fame_plus_20" -> next.fame - snapshot.fame >= 20
                "rank_up" -> next.clanRank > snapshot.clanRank
                "no_death" -> next.totalDeaths == snapshot.totalDeaths
                else -> false
            }
            if (completed) {
                val def = GameContent.yearlyGoal(goalState.goalId) ?: return@forEach
                next = next.copy(
                    completedGoalIds = next.completedGoalIds + def.id,
                    silver = next.silver + def.rewardSilver,
                    grain = next.grain + def.rewardGrain,
                    cloth = next.cloth + def.rewardCloth,
                    fame = next.fame + def.rewardFame
                )
                report.events += "年度目标【${def.name}】完成，获得奖励。"
            }
        }
        return next
    }

    private fun checkEndings(state: GameState, report: MutableReport): GameState {
        if (state.gameEnded) return state
        return when {
            aliveMembers(state).isEmpty() -> {
                report.events += "宗族无人存续，浮生至此终结。"
                state.copy(gameEnded = true, endingChoice = "clan_extinct")
            }
            state.year >= 1644 && !state.triggeredHiddenEndings.contains("survive_end") -> {
                report.events += "甲申大变已至，宗族熬过乱世，可选择继续经营或结算传承。"
                state.copy(hiddenEnding = "survive_end", triggeredHiddenEndings = state.triggeredHiddenEndings + "survive_end")
            }
            state.silver <= -5000 || state.grain <= -5000 -> {
                report.events += "宗族债台高筑，被迫变卖家业，品级下降。"
                val nextRank = max(1, state.clanRank - 1)
                state.copy(clanRank = nextRank, silver = max(0, state.silver / 2), grain = max(0, state.grain / 2), fame = max(0, state.fame - 50))
            }
            else -> state
        }
    }

    private fun addIncome(report: MutableReport, kind: ResourceKind, amount: Int) {
        when (kind) {
            ResourceKind.Silver -> report.incomeSilver += amount
            ResourceKind.Grain -> report.incomeGrain += amount
            ResourceKind.Cloth -> report.incomeCloth += amount
            ResourceKind.Fame -> report.incomeFame += amount
            ResourceKind.None -> Unit
        }
    }

    private fun combinedEffects(state: GameState): RuleEffects {
        val rules = state.clanRules.mapNotNull { id -> GameContent.clanRules.firstOrNull { it.id == id }?.effects }
        val motto = GameContent.motto(state.familyMottoId).effects
        return (rules + motto).fold(RuleEffects()) { acc, e ->
            RuleEffects(
                grainConsumeMul = acc.grainConsumeMul + e.grainConsumeMul,
                grainOutputMul = acc.grainOutputMul + e.grainOutputMul,
                clothConsumeMul = acc.clothConsumeMul + e.clothConsumeMul,
                fameDrain = acc.fameDrain + e.fameDrain,
                studyGrowthMul = acc.studyGrowthMul + e.studyGrowthMul,
                martialGrowthMul = acc.martialGrowthMul + e.martialGrowthMul,
                tradeIncomeMul = acc.tradeIncomeMul + e.tradeIncomeMul,
                allOutputMul = acc.allOutputMul + e.allOutputMul,
                banditResist = acc.banditResist + e.banditResist,
                examPassBonus = acc.examPassBonus + e.examPassBonus,
                militarySurvival = acc.militarySurvival + e.militarySurvival,
                fortCostMul = acc.fortCostMul + e.fortCostMul,
                initSilverBonus = acc.initSilverBonus + e.initSilverBonus
            )
        }
    }

    private fun diminishedGain(current: Int, base: Int, mul: Double): Int {
        if (base <= 0) return 0
        val raw = base * (1.0 + mul)
        val factor = when {
            current >= 80 -> 0.05
            current >= 70 -> 0.07
            current >= 65 -> 0.10
            current >= 50 -> 0.20
            current >= 35 -> 0.35
            current >= 20 -> 0.60
            else -> 1.0
        }
        val scaled = raw * factor
        val floorValue = floor(scaled).toInt()
        return max(0, floorValue + if (Random.nextDouble() < scaled - floorValue) 1 else 0)
    }

    private fun rankCap(rank: Int) = when (rank) {
        1 -> 35
        2 -> 50
        3 -> 65
        4 -> 80
        5 -> 92
        else -> 100
    }

    private fun capFor(state: GameState, member: ClanMember, attr: String, rankCap: Int = rankCap(state.clanRank)): Int {
        val aptitudeCap = when (attr) {
            "study" -> member.aptitude.study.cap
            "martial" -> member.aptitude.martial.cap
            else -> member.aptitude.health.cap
        }
        return min(rankCap, aptitudeCap)
    }

    private fun ageGainChance(age: Int) = when {
        age <= 6 -> 0.05
        age <= 12 -> 0.08
        age <= 18 -> 0.10
        age <= 30 -> 0.05
        else -> 0.0
    }

    private fun marketEventFactor(commodityId: String, year: Int, month: Int, state: GameState): Double {
        var factor = 1.0
        val wartime = year in 1399..1402 || year in 1449..1450 || year in 1592..1598 || year >= 1627
        if (wartime) {
            if (commodityId == "weapon") factor *= 1.25
            if (commodityId == "horse") factor *= 1.30
            if (commodityId == "grain") factor *= 1.15
            if (commodityId == "book") factor *= 0.85
        }
        if (year in 1405..1433) {
            if (commodityId == "cloth") factor *= 1.10
            if (commodityId == "herb") factor *= 1.08
        }
        if (Random.nextDouble() < region(state).disasterRate * 0.3) {
            if (commodityId == "herb") factor *= 1.20
            if (commodityId == "grain") factor *= 1.10
        }
        if (year % 3 == 0 && month in 6..9 && commodityId == "book") factor *= 1.15
        return factor
    }

    private fun taxModifier(year: Int) = when {
        year >= 1627 -> 1.35
        year >= 1581 -> 1.10
        year in 1400..1435 -> 0.9
        else -> 1.0
    }

    private fun deathRateModifier(year: Int) = when {
        year >= 1630 -> 1.8
        year >= 1620 -> 1.4
        year in 1449..1450 -> 1.6
        else -> 1.0
    }

    private fun snapshotOf(state: GameState) = YearSnapshot(
        silver = state.silver,
        grain = state.grain,
        cloth = state.cloth,
        fame = state.fame,
        aliveCount = aliveMembers(state).size,
        clanRank = state.clanRank,
        industryCount = state.industries.size,
        fortCount = state.fortCount,
        totalBirths = state.totalBirths,
        totalDeaths = state.totalDeaths,
        totalExamPasses = state.totalExamPasses,
        totalMilitaryMerits = state.totalMilitaryMerits
    )

    private fun ensureRuntimeState(state: GameState): GameState {
        val market = if (state.market.prices.isEmpty()) state.market.copy(prices = GameContent.defaultMarketPrices()) else state.market
        return state.copy(market = market)
    }

    private fun bumpMap(map: Map<String, Int>, key: String, delta: Int): Map<String, Int> {
        val next = (map[key] ?: 0) + delta
        return if (next > 0) map + (key to next) else map - key
    }

    private fun addItem(items: List<InventoryItem>, itemId: String, delta: Int): List<InventoryItem> {
        val current = items.firstOrNull { it.itemId == itemId }?.count ?: 0
        val next = current + delta
        val filtered = items.filterNot { it.itemId == itemId }
        return if (next > 0) filtered + InventoryItem(itemId, next) else filtered
    }

    private fun addLog(state: GameState, text: String): GameState {
        val log = listOf(EventLogEntry(state.year, state.month, text)) + state.eventLog
        return state.copy(eventLog = log.take(160))
    }

    private fun randomGivenName(gender: Gender): String {
        val pool = if (gender == Gender.Female) GameContent.femaleNames else GameContent.maleNames
        val first = pool.random()
        return if (Random.nextBoolean()) first + pool.random() else first
    }

    private fun randomAptitudeSet() = AptitudeSet(randomAptitude(), randomAptitude(), randomAptitude())

    private fun randomAptitude(): Aptitude {
        val roll = Random.nextInt(100)
        val stars = when {
            roll < 30 -> 1
            roll < 60 -> 2
            roll < 85 -> 3
            roll < 97 -> 4
            else -> 5
        }
        val cap = when (stars) {
            1 -> Random.nextInt(50, 60)
            2 -> Random.nextInt(60, 70)
            3 -> Random.nextInt(70, 80)
            4 -> Random.nextInt(80, 90)
            else -> Random.nextInt(90, 101)
        }
        return Aptitude(cap, stars)
    }

    private fun rollTalentId(chance: Double = 0.30): String? = if (Random.nextDouble() < chance) GameContent.talents.random().id else null

    private fun randomMember(state: GameState, generation: Int): ClanMember {
        val gender = if (Random.nextBoolean()) Gender.Male else Gender.Female
        return ClanMember(
            id = state.nextMemberId,
            name = state.surname + randomGivenName(gender),
            gender = gender,
            age = Random.nextInt(16, 31),
            generation = generation,
            study = Random.nextInt(5, 25),
            martial = Random.nextInt(5, 25),
            health = Random.nextInt(60, 96),
            aptitude = randomAptitudeSet(),
            talentId = rollTalentId()
        )
    }
}
