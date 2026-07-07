package com.daming.fushengzhi2.logic

import com.daming.fushengzhi2.data.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object GameEngine {
    private val seasonFactors = mapOf(
        ResourceKind.Grain to listOf(0.6, 0.8, 1.0, 1.2, 1.5, 1.3, 1.2, 1.0, 1.4, 1.0, 0.7, 0.5),
        ResourceKind.Silver to listOf(0.9, 1.0, 1.0, 1.1, 1.0, 1.0, 1.0, 1.1, 1.0, 1.2, 1.1, 1.3),
        ResourceKind.Cloth to listOf(0.8, 0.9, 1.0, 1.0, 1.0, 1.0, 1.0, 1.1, 1.2, 1.1, 1.0, 0.9),
        ResourceKind.Fame to List(12) { 1.0 }
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
            aptitude = randomAptitudeSet()
        )
        val industries = when (origin.id) {
            "landlord" -> listOf("dry_field", "paddy_field", "shop")
            "military" -> listOf("dry_field", "dry_field")
            else -> listOf("dry_field")
        }.mapIndexed { index, typeId -> ClanIndustry(index + 1, typeId) }

        return GameState(
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
            eventLog = listOf(EventLogEntry(1368, 1, "${surname}氏宗族立宗，乱世浮生由此开始。"))
        )
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

    fun assignIndustry(state: GameState, industryId: Int, memberId: Int?): GameState {
        val industries = state.industries.map { if (it.id == industryId) it.copy(assignedMemberId = memberId) else it }
        return state.copy(industries = industries)
    }

    fun setMemberState(state: GameState, memberId: Int, memberState: MemberState): GameState {
        val members = state.members.map { member ->
            if (member.id == memberId && member.alive) member.copy(state = memberState) else member
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



    fun buyMarketItem(state: GameState, itemId: String, count: Int): GameState {
        val price = state.market.prices[itemId] ?: return state
        val total = price * count
        if (count <= 0 || state.silver < total) return state
        return when (itemId) {
            "grain" -> addLog(state.copy(silver = state.silver - total, grain = state.grain + count), "集市买入粮食$count，花费银两$total。")
            "cloth" -> addLog(state.copy(silver = state.silver - total, cloth = state.cloth + count), "集市买入布匹$count，花费银两$total。")
            else -> addLog(state.copy(silver = state.silver - total, inventory = addItem(state.inventory, itemId, count)), "集市买入${GameContent.item(itemId)?.name ?: itemId}$count，花费银两$total。")
        }
    }

    fun sellMarketItem(state: GameState, itemId: String, count: Int): GameState {
        val price = state.market.prices[itemId] ?: return state
        if (count <= 0) return state
        return when (itemId) {
            "grain" -> if (state.grain >= count) addLog(state.copy(grain = state.grain - count, silver = state.silver + price * count), "集市卖出粮食$count，获得银两${price * count}。") else state
            "cloth" -> if (state.cloth >= count) addLog(state.copy(cloth = state.cloth - count, silver = state.silver + price * count), "集市卖出布匹$count，获得银两${price * count}。") else state
            else -> {
                val have = state.inventory.firstOrNull { it.itemId == itemId }?.count ?: 0
                if (have < count) state else addLog(state.copy(inventory = addItem(state.inventory, itemId, -count), silver = state.silver + price * count), "集市卖出${GameContent.item(itemId)?.name ?: itemId}$count，获得银两${price * count}。")
            }
        }
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
            "heal" -> memberId?.let { id -> next.copy(members = next.members.map { if (it.id == id) it.copy(health = min(100, it.health + 35), state = MemberState.Home) else it }) } ?: next
            "study_boost" -> memberId?.let { id -> next.copy(members = next.members.map { if (it.id == id) it.copy(study = min(100, it.study + 3)) else it }) } ?: next
            "martial_boost" -> memberId?.let { id -> next.copy(members = next.members.map { if (it.id == id) it.copy(martial = min(100, it.martial + 3)) else it }) } ?: next
            "martial_boost_large" -> memberId?.let { id -> next.copy(members = next.members.map { if (it.id == id) it.copy(martial = min(100, it.martial + 5)) else it }) } ?: next
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
        if (state.clanRank < type.minRank || state.silver < type.costSilver || state.grain < type.costGrain || member.state \!= MemberState.Home) return state
        val expedition = Expedition(typeId, memberId, type.duration, state.year, state.month)
        val members = state.members.map { if (it.id == memberId) it.copy(state = MemberState.Expedition) else it }
        return addLog(state.copy(silver = state.silver - type.costSilver, grain = state.grain - type.costGrain, members = members, expeditions = state.expeditions + expedition), "${member.name}出发进行【${type.name}】。")
    }

    fun takeExam(state: GameState, memberId: Int, examId: String): GameState {
        val exam = GameContent.exam(examId) ?: return state
        val member = state.members.firstOrNull { it.id == memberId && it.alive } ?: return state
        if (member.study < exam.reqStudy) return state
        val pass = Random.nextDouble() < exam.passRate + combinedEffects(state).examPassBonus
        val members = if (pass) state.members.map { if (it.id == memberId) it.copy(identity = exam.result, state = MemberState.Home) else it } else state.members
        val next = if (pass) state.copy(members = members, fame = state.fame + exam.famePlus, totalExamPasses = state.totalExamPasses + 1) else state
        return addLog(next, if (pass) "${member.name}参加${exam.name}高中【${exam.result}】！" else "${member.name}参加${exam.name}落榜。")
    }

    fun recruitArmy(state: GameState, infantry: Int, archers: Int): GameState {
        val cost = infantry * 2 + archers * 3
        val grainCost = infantry + archers
        if (infantry < 0 || archers < 0 || state.silver < cost || state.grain < grainCost) return state
        return addLog(state.copy(silver = state.silver - cost, grain = state.grain - grainCost, army = state.army.copy(infantry = state.army.infantry + infantry, archers = state.army.archers + archers)), "招募步兵${infantry}、弓兵${archers}。")
    }

    fun attackStage(state: GameState, stageId: Int): GameState {
        val stage = GameContent.campaignStages.firstOrNull { it.id == stageId } ?: return state
        if (state.conqueredStages.contains(stageId)) return state
        val power = state.army.infantry + state.army.archers * 2 + state.army.trainingLevel * 50 + adults(state).filter { it.state == MemberState.Military }.sumOf { it.martial }
        val win = power >= stage.enemyPower || Random.nextDouble() < (power.toDouble() / (stage.enemyPower * 1.5)).coerceIn(0.05, 0.85)
        val lossRatio = if (win) 0.15 else 0.35
        val nextArmy = state.army.copy(infantry = max(0, (state.army.infantry * (1 - lossRatio)).toInt()), archers = max(0, (state.army.archers * (1 - lossRatio)).toInt()))
        val next = if (win) state.copy(army = nextArmy, silver = state.silver + stage.rewardSilver, fame = state.fame + stage.rewardFame, conqueredStages = state.conqueredStages + stageId) else state.copy(army = nextArmy)
        return addLog(next, if (win) "征伐【${stage.name}】获胜，银两+${stage.rewardSilver}，声望+${stage.rewardFame}。" else "征伐【${stage.name}】失利，士卒折损。")
    }

    fun advanceMonth(state: GameState): Pair<GameState, MonthlyReport> {
        var working = state
        val events = mutableListOf<String>()
        val effects = combinedEffects(working)
        var incomeSilver = 0
        var incomeGrain = 0
        var incomeCloth = 0
        var incomeFame = 0
        var expenseSilver = 0
        var expenseGrain = 0
        var expenseCloth = 0
        var expenseFame = 0

        fun addIncome(kind: ResourceKind, amount: Int) {
            when (kind) {
                ResourceKind.Silver -> incomeSilver += amount
                ResourceKind.Grain -> incomeGrain += amount
                ResourceKind.Cloth -> incomeCloth += amount
                ResourceKind.Fame -> incomeFame += amount
                ResourceKind.None -> Unit
            }
        }

        working.industries.forEach { industry ->
            val type = GameContent.industry(industry.typeId) ?: return@forEach
            if (type.resource == ResourceKind.None) return@forEach
            val output = calcIndustryOutput(working, industry, type, type.resource, type.baseOutput, effects)
            addIncome(type.resource, output)
            type.resource2?.let { addIncome(it, calcIndustryOutput(working, industry, type, it, type.baseOutput2, effects)) }
            type.resource3?.let { addIncome(it, calcIndustryOutput(working, industry, type, it, type.baseOutput3, effects)) }
            when (type.specialEffect) {
                "consume_grain_3" -> expenseGrain += 3
                "consume_fame_3" -> expenseFame += 3
                "interest_1pct" -> incomeSilver += min(150, floor(working.silver * 0.01).toInt())
                "interest_2pct" -> incomeSilver += min(250, floor(working.silver * 0.02).toInt())
                "risk_loss_20" -> if (Random.nextDouble() < 0.20) {
                    val lost = max(1, output / 2)
                    incomeSilver -= lost
                    events += "${type.name}押镖途中遭劫，损失银两$lost。"
                }
                "risk_tax_30" -> if (Random.nextDouble() < 0.10) {
                    val fine = max(1, output / 2)
                    incomeSilver -= fine
                    events += "${type.name}被官府稽查，罚没银两$fine。"
                }
            }
        }

        adults(working).filter { it.state == MemberState.Trade }.forEach { member ->
            val income = floor(Random.nextInt(5, 13) * (1.0 + effects.tradeIncomeMul)).toInt()
            incomeSilver += income
            if (Random.nextDouble() < 0.06) {
                val fameGain = Random.nextInt(2, 6)
                incomeFame += fameGain
                events += "${member.name}经商结交豪客，声望+$fameGain。"
            }
        }

        adults(working).filter { it.state == MemberState.Labor }.forEach { incomeSilver += 5 + working.clanRank * 2 }

        aliveMembers(working).forEach { member ->
            expenseGrain += when {
                member.age <= 5 -> 1
                member.age <= 14 -> 2
                else -> 3
            }
        }
        expenseGrain = max(1, floor(expenseGrain * (1.0 + effects.grainConsumeMul)).toInt())
        if (working.month % 3 == 0) {
            expenseCloth = max(0, floor(ceil(aliveMembers(working).size * 0.5) * (1.0 + effects.clothConsumeMul)).toInt())
            val taxRate = region(working).taxRate * difficulty(working).taxMul
            expenseSilver += ceil(working.silver * taxRate * 0.3).toInt()
            expenseGrain += ceil(incomeGrain * taxRate).toInt()
            events += "官府征税：银两$expenseSilver、粮食$expenseGrain。"
        }
        if (effects.fameDrain < 0) expenseFame += -effects.fameDrain

        var members = working.members.map { member ->
            if (!member.alive) return@map member
            var next = member
            if (working.month == 12) next = next.copy(age = next.age + 1)
            next = when (next.state) {
                MemberState.Study -> next.copy(study = min(100, next.study + diminishedGain(next.study, 1 + Random.nextInt(0, 3), effects.studyGrowthMul)))
                MemberState.Military -> next.copy(martial = min(100, next.martial + diminishedGain(next.martial, Random.nextInt(0, 3), effects.martialGrowthMul)))
                MemberState.Sick -> next.copy(health = min(100, next.health + Random.nextInt(1, 5)))
                else -> next
            }
            next
        }

        working.academies.forEach { academy ->
            val type = GameContent.academy(academy.typeId) ?: return@forEach
            academy.memberIds.forEach { mid ->
                members = members.map { member ->
                    if (member.id \!= mid || \!member.alive) member else {
                        if (type.attribute == "study") member.copy(study = min(100, member.study + max(1, (2 + academy.level))))
                        else member.copy(martial = min(100, member.martial + max(1, (2 + academy.level))))
                    }
                }
            }
        }

        val remainingExpeditions = mutableListOf<Expedition>()
        working.expeditions.forEach { exp ->
            val type = GameContent.expedition(exp.typeId)
            val nextExp = exp.copy(monthsLeft = exp.monthsLeft - 1)
            if (type == null || nextExp.monthsLeft > 0) {
                remainingExpeditions += nextExp
            } else {
                val member = members.firstOrNull { it.id == exp.memberId }
                if (member \!= null && member.alive) {
                    val failed = Random.nextDouble() < type.riskRate
                    if (failed) {
                        events += "${member.name}历练遭遇${type.riskDesc}，负伤归来。"
                        members = members.map { if (it.id == member.id) it.copy(health = max(10, it.health - Random.nextInt(10, 31)), state = MemberState.Sick) else it }
                    } else {
                        val silverReward = type.rewardSilver?.let { Random.nextInt(it.first, it.last + 1) } ?: 0
                        val clothReward = type.rewardCloth?.let { Random.nextInt(it.first, it.last + 1) } ?: 0
                        val fameReward = type.rewardFame?.let { Random.nextInt(it.first, it.last + 1) } ?: 0
                        val studyReward = type.rewardStudy?.let { Random.nextInt(it.first, it.last + 1) } ?: 0
                        incomeSilver += silverReward
                        incomeCloth += clothReward
                        incomeFame += fameReward
                        members = members.map { if (it.id == member.id) it.copy(state = MemberState.Home, study = min(100, it.study + studyReward)) else it }
                        if (type.itemChance > 0 && Random.nextDouble() < type.itemChance) working = working.copy(inventory = addItem(working.inventory, GameContent.itemTypes.random().id, 1))
                        if (type.recruitChance > 0 && Random.nextDouble() < type.recruitChance) {
                            val recruit = randomMember(working, 1)
                            members = members + recruit
                            working = working.copy(nextMemberId = working.nextMemberId + 1)
                        }
                        events += "${member.name}完成【${type.name}】，满载而归。"
                    }
                }
            }
        }
        working = working.copy(expeditions = remainingExpeditions)

        if (working.month == 1 && aliveMembers(working).size < 6 && Random.nextDouble() < 0.35) {
            val newMember = randomMember(working, generation = 1)
            members = members + newMember
            working = working.copy(nextMemberId = working.nextMemberId + 1, totalBirths = working.totalBirths + 1)
            events += "流民${newMember.name}投奔宗族。"
        }

        val disasterChance = region(working).disasterRate * difficulty(working).disasterMul / 12.0
        if (Random.nextDouble() < disasterChance) {
            val loss = max(5, floor(working.grain * Random.nextDouble(0.08, 0.18)).toInt())
            expenseGrain += loss
            events += "本月遭遇灾荒，粮食损失$loss。"
        }
        val banditChance = region(working).banditRate * difficulty(working).disasterMul / 12.0 * (1.0 - min(0.5, effects.banditResist + working.fortCount * 0.1))
        if (Random.nextDouble() < banditChance) {
            val loss = max(3, floor(working.silver * Random.nextDouble(0.05, 0.15)).toInt())
            expenseSilver += loss
            events += "流寇劫掠乡里，宗族损失银两$loss。"
        }

        var silver = working.silver + incomeSilver - expenseSilver
        var grain = working.grain + incomeGrain - expenseGrain
        var cloth = working.cloth + incomeCloth - expenseCloth
        var fame = working.fame + incomeFame - expenseFame
        var totalDeaths = working.totalDeaths
        if (grain <= 0) {
            events += "粮食耗尽，族人健康下降。"
            members = members.map { member ->
                if (!member.alive) member else {
                    val health = member.health - Random.nextInt(3, 10)
                    if (health <= 0) {
                        totalDeaths += 1
                        events += "${member.name}因饥饿病逝。"
                        member.copy(health = 0, alive = false, state = MemberState.Dead)
                    } else member.copy(health = health)
                }
            }
        }
        silver = max(-999999, silver)
        grain = max(-999999, grain)
        cloth = max(-999999, cloth)
        fame = max(-999999, fame)

        val nextMonth = if (working.month == 12) 1 else working.month + 1
        val nextYear = if (working.month == 12) working.year + 1 else working.year
        if (working.month == 12) events += "${working.year}年终结，宗族延续至${nextYear}年。"

        working = working.copy(
            year = nextYear,
            month = nextMonth,
            totalMonths = working.totalMonths + 1,
            silver = silver,
            grain = grain,
            cloth = cloth,
            fame = fame,
            members = members,
            expeditions = working.expeditions,
            inventory = working.inventory,
            totalDeaths = totalDeaths
        )

        val report = MonthlyReport(
            year = state.year,
            month = state.month,
            incomeSilver = incomeSilver,
            incomeGrain = incomeGrain,
            incomeCloth = incomeCloth,
            incomeFame = incomeFame,
            expenseSilver = expenseSilver,
            expenseGrain = expenseGrain,
            expenseCloth = expenseCloth,
            expenseFame = expenseFame,
            events = events.ifEmpty { listOf("本月平安无事。") }
        )
        report.events.forEach { working = addLog(working, it) }
        return working to report
    }

    private fun calcIndustryOutput(
        state: GameState,
        industry: ClanIndustry,
        type: IndustryType,
        resource: ResourceKind,
        baseOutput: Int,
        effects: RuleEffects
    ): Int {
        if (baseOutput <= 0) return 0
        val manager = industry.assignedMemberId?.let { id -> state.members.firstOrNull { it.id == id && it.alive } }
        val manageMul = when {
            manager == null -> 1.0
            manager.state == MemberState.Home || manager.state == MemberState.Sick -> 1.30
            else -> 1.15
        }
        var mul = 1.0 + effects.allOutputMul
        if (resource == ResourceKind.Grain) mul += effects.grainOutputMul
        seasonFactors[resource]?.let { mul *= it[state.month - 1] }
        if (type.specialEffect == "season_amplify" && resource == ResourceKind.Silver && state.month in listOf(4, 8, 10, 11, 12)) mul *= 1.25
        val output = baseOutput * (1 + ln(industry.level.toDouble()) * 1.5) * manageMul * mul
        return max(1, floor(output).toInt())
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
        val raw = floor(base * (1.0 + mul)).toInt()
        val factor = when {
            current >= 90 -> 0.25
            current >= 75 -> 0.45
            current >= 50 -> 0.70
            else -> 1.0
        }
        return max(0, floor(raw * factor).toInt())
    }



    private fun addItem(items: List<InventoryItem>, itemId: String, delta: Int): List<InventoryItem> {
        val current = items.firstOrNull { it.itemId == itemId }?.count ?: 0
        val next = current + delta
        val filtered = items.filterNot { it.itemId == itemId }
        return if (next > 0) filtered + InventoryItem(itemId, next) else filtered
    }

    private fun addLog(state: GameState, text: String): GameState {
        val log = listOf(EventLogEntry(state.year, state.month, text)) + state.eventLog
        return state.copy(eventLog = log.take(120))
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
            aptitude = randomAptitudeSet()
        )
    }
}
