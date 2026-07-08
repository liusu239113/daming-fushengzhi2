package com.daming.fushengzhi3.v3.logic

import com.daming.fushengzhi3.v3.data.V3AnnualGoal
import com.daming.fushengzhi3.v3.data.V3Branch
import com.daming.fushengzhi3.v3.data.V3Content
import com.daming.fushengzhi3.v3.data.V3CountySite
import com.daming.fushengzhi3.v3.data.V3CountySiteType
import com.daming.fushengzhi3.v3.data.V3EndingPreview
import com.daming.fushengzhi3.v3.data.V3EndingTier
import com.daming.fushengzhi3.v3.data.V3FinalEnding
import com.daming.fushengzhi3.v3.data.V3GameState
import com.daming.fushengzhi3.v3.data.V3Gender
import com.daming.fushengzhi3.v3.data.V3GoalMetric
import com.daming.fushengzhi3.v3.data.V3MonthlyForecast
import com.daming.fushengzhi3.v3.data.V3MonthlyReport
import com.daming.fushengzhi3.v3.data.V3Person
import com.daming.fushengzhi3.v3.data.V3RankCost
import com.daming.fushengzhi3.v3.data.V3Relations
import com.daming.fushengzhi3.v3.data.V3Route
import com.daming.fushengzhi3.v3.data.V3Screen
import com.daming.fushengzhi3.v3.data.V3SiteStatus
import com.daming.fushengzhi3.v3.data.V3SiteYield
import com.daming.fushengzhi3.v3.data.V3SpouseCandidate
import com.daming.fushengzhi3.v3.data.V3TaskType
import com.daming.fushengzhi3.v3.data.V3UpgradeCost
import kotlin.math.max
import kotlin.math.min

object V3GameEngine {
    fun alivePeople(state: V3GameState): List<V3Person> = state.people.filter { it.alive }

    fun adultPeople(state: V3GameState): List<V3Person> = alivePeople(state).filter { it.age >= 15 }

    fun clanRankName(state: V3GameState): String = when (state.clanRank) {
        1 -> "立户"
        2 -> "小族"
        3 -> "望族"
        4 -> "县中大姓"
        else -> "郡望世家"
    }

    fun nextRankCost(state: V3GameState): V3RankCost? = when (state.clanRank + 1) {
        2 -> V3RankCost(90, 130, 2, 2, 10, "小族")
        3 -> V3RankCost(260, 320, 5, 4, 28, "望族")
        4 -> V3RankCost(650, 760, 10, 6, 52, "县中大姓")
        5 -> V3RankCost(1400, 1500, 18, 8, 80, "郡望世家")
        else -> null
    }

    fun builtSiteCount(state: V3GameState): Int = state.sites.count { it.level > 0 && it.type != V3CountySiteType.Shrine }

    fun canRankUp(state: V3GameState): Boolean {
        val cost = nextRankCost(state) ?: return false
        return state.silver >= cost.silver &&
            state.grain >= cost.grain &&
            alivePeople(state).size >= cost.population &&
            builtSiteCount(state) >= cost.builtSites &&
            state.influence >= cost.influence
    }

    fun rankUp(state: V3GameState): V3GameState {
        val cost = nextRankCost(state) ?: return state.copy(pendingReports = listOf("宗族已达最高品第。"))
        if (!canRankUp(state)) {
            return state.copy(pendingReports = listOf("晋升【${cost.title}】不足：需银${cost.silver}、粮${cost.grain}、人口${cost.population}、产业${cost.builtSites}、族望${cost.influence}。"))
        }
        val message = "宗族晋升为【${cost.title}】：可容纳更多产业与人口，家业进入下一阶段。"
        return state.copy(
            clanRank = state.clanRank + 1,
            silver = state.silver - cost.silver,
            grain = state.grain - cost.grain,
            influence = (state.influence + 4).coerceAtMost(100),
            cohesion = (state.cohesion + 3).coerceAtMost(100),
            pendingReports = listOf(message),
            eventLog = (listOf("${state.year}年${state.month}月 · $message") + state.eventLog).take(100)
        )
    }

    fun marriageOptions(state: V3GameState): List<V3SpouseCandidate> {
        if (hasSpouse(state)) return emptyList()
        return V3Content.spouseCandidates.filter { state.influence >= it.influenceReq }
    }

    fun canMarry(state: V3GameState, candidateId: String): Boolean {
        val candidate = V3Content.spouseCandidates.firstOrNull { it.id == candidateId } ?: return false
        return !hasSpouse(state) && state.silver >= candidate.silverCost && state.grain >= candidate.grainCost && state.influence >= candidate.influenceReq
    }

    fun marry(state: V3GameState, candidateId: String): V3GameState {
        val candidate = V3Content.spouseCandidates.firstOrNull { it.id == candidateId } ?: return state
        if (hasSpouse(state)) return state.copy(pendingReports = listOf("家主已成婚，后续重点应放在添丁、产业与子嗣培养。"))
        if (!canMarry(state, candidateId)) {
            return state.copy(pendingReports = listOf("迎娶【${candidate.name}】不足：需银${candidate.silverCost}、粮${candidate.grainCost}、族望${candidate.influenceReq}。"))
        }
        val patriarch = state.people.firstOrNull { it.id == 1 } ?: state.people.first()
        val spouseId = state.nextPersonId
        val spouse = V3Person(
            id = spouseId,
            name = candidate.name,
            age = 19,
            branch = "主房",
            identity = "主母",
            trait = when (candidate.route) {
                V3Route.Merchant -> com.daming.fushengzhi3.v3.data.V3Trait.Cunning
                V3Route.Scholar -> com.daming.fushengzhi3.v3.data.V3Trait.Studious
                V3Route.Fortress -> com.daming.fushengzhi3.v3.data.V3Trait.Martial
                else -> com.daming.fushengzhi3.v3.data.V3Trait.Honest
            },
            study = 18 + candidate.studyBonus,
            martial = 10 + candidate.martialBonus,
            commerce = 18 + candidate.commerceBonus,
            diplomacy = 18 + candidate.diplomacyBonus,
            loyalty = 88,
            gender = V3Gender.Female,
            spouseId = patriarch.id
        )
        val people = state.people.map { if (it.id == patriarch.id) it.copy(spouseId = spouseId) else it } + spouse
        val message = "${patriarch.name}迎娶【${candidate.name}】：${candidate.desc} 家族人口+1，后续月结有机会添丁。"
        val routeScore = (state.routeScores[candidate.route] ?: 0) + 5
        return state.copy(
            silver = state.silver - candidate.silverCost,
            grain = state.grain - candidate.grainCost,
            people = people,
            nextPersonId = spouseId + 1,
            cohesion = (state.cohesion + 5).coerceAtMost(100),
            routeScores = state.routeScores + (candidate.route to routeScore),
            pendingReports = listOf(message),
            eventLog = (listOf("${state.year}年${state.month}月 · $message") + state.eventLog).take(100)
        )
    }

    fun upgradeCost(site: V3CountySite): V3UpgradeCost? {
        if (site.level >= 3) return null
        val nextLevel = site.level + 1
        val silver = 28 + nextLevel * 24 + site.risk / 5
        val grain = 12 + nextLevel * 14
        val desc = when (site.type) {
            V3CountySiteType.Shrine -> "修谱立规，提升凝聚与年度评分。"
            V3CountySiteType.Farmland -> "开垦修渠，显著增加每月粮食。"
            V3CountySiteType.Market -> "置摊铺账，增加每月银两。"
            V3CountySiteType.Yamen -> "打点差役，降低赋役压力。"
            V3CountySiteType.Academy -> "建义塾，培养子嗣读书。"
            V3CountySiteType.Clinic -> "置药柜，降低灾病损耗。"
            V3CountySiteType.Fort -> "筑围墙，产出乡勇并压风险。"
            V3CountySiteType.Dock -> "修小埠，打开商路和远行路线。"
            V3CountySiteType.MountainPass -> "设茶棚和卡哨，得小利并防流寇。"
        }
        return V3UpgradeCost(silver, grain, desc)
    }

    fun canUpgrade(state: V3GameState, siteId: String): Boolean {
        val site = state.sites.firstOrNull { it.id == siteId } ?: return false
        val cost = upgradeCost(site) ?: return false
        val limit = 1 + state.clanRank * 2
        val wouldAddBuiltSite = site.level == 0 && site.type != V3CountySiteType.Shrine
        if (wouldAddBuiltSite && builtSiteCount(state) >= limit) return false
        return state.silver >= cost.silver && state.grain >= cost.grain
    }

    fun upgradeSite(state: V3GameState, siteId: String): V3GameState {
        val site = state.sites.firstOrNull { it.id == siteId } ?: return state
        val cost = upgradeCost(site) ?: return state.copy(pendingReports = listOf("${site.name}已达最高等级。"))
        val limit = 1 + state.clanRank * 2
        if (site.level == 0 && site.type != V3CountySiteType.Shrine && builtSiteCount(state) >= limit) {
            return state.copy(pendingReports = listOf("当前品第最多经营 $limit 处产业。先积累人口、族望与资源，再晋升宗族。"))
        }
        if (state.silver < cost.silver || state.grain < cost.grain) {
            return state.copy(pendingReports = listOf("资源不足：营建${site.name}需要银${cost.silver}、粮${cost.grain}。"))
        }
        val before = siteYield(site)
        val nextSite = site.copy(
            level = site.level + 1,
            control = (site.control + 14 + site.level * 4).coerceAtMost(100),
            risk = (site.risk - 10 - site.level * 3).coerceAtLeast(0)
        )
        val after = siteYield(nextSite)
        val route = routeForSite(site)
        val routes = state.routeScores + (route to ((state.routeScores[route] ?: 0) + 4))
        val message = "营建【${site.name}】至 Lv.${nextSite.level}：${yieldDiffText(before, after)}。${cost.desc}"
        return state.copy(
            silver = state.silver - cost.silver,
            grain = state.grain - cost.grain,
            sites = state.sites.map { if (it.id == siteId) nextSite.copy(status = statusFor(nextSite.control, nextSite.risk)) else it },
            routeScores = routes,
            pendingReports = listOf(message),
            eventLog = (listOf("${state.year}年${state.month}月 · $message") + state.eventLog).take(100)
        )
    }

    fun assignTask(state: V3GameState, personId: Int, siteId: String, task: V3TaskType): V3GameState {
        val person = state.people.firstOrNull { it.id == personId && it.alive } ?: return state
        val site = state.sites.firstOrNull { it.id == siteId && it.taskTypes.contains(task) } ?: return state
        if (person.age < 12) return state.copy(pendingReports = listOf("${person.name}尚年幼，不能外出办事。"))
        val previousPersonId = site.assignedPersonId
        val people = state.people.map {
            when (it.id) {
                personId -> it.copy(currentTask = task, assignedSiteId = siteId)
                previousPersonId -> it.copy(currentTask = null, assignedSiteId = null)
                else -> it
            }
        }
        val sites = state.sites.map {
            when {
                it.id == siteId -> it.copy(assignedPersonId = personId)
                it.assignedPersonId == personId -> it.copy(assignedPersonId = null)
                else -> it
            }
        }
        val preview = assignmentPreview(person, site, task)
        return state.copy(
            people = people,
            sites = sites,
            pendingReports = listOf("已安排${person.name}去【${site.name}】${task.label}。$preview")
        )
    }

    fun assignmentPreview(person: V3Person, site: V3CountySite, task: V3TaskType): String {
        val power = taskPower(person.study, person.martial, person.commerce, person.diplomacy, task)
        val controlGain = max(2, power / 16)
        val riskDrop = max(1, power / 20)
        val silver = taskSilver(task, power)
        val grain = taskGrain(task, power)
        val parts = mutableListOf("控+$controlGain", "险-$riskDrop")
        if (silver != 0) parts += if (silver > 0) "银+$silver" else "银$silver"
        if (grain != 0) parts += if (grain > 0) "粮+$grain" else "粮$grain"
        if (site.level == 0) parts += "可先压风险，建成后才有月产"
        return "预计月结：${parts.joinToString(" · ")}。"
    }

    fun siteYield(site: V3CountySite): V3SiteYield {
        if (site.level <= 0) return V3SiteYield(desc = "未建成，无固定月产")
        val quality = (70 + site.control / 3 - site.risk / 3).coerceIn(35, 120)
        fun scale(value: Int): Int = max(0, value * site.level * quality / 100)
        return when (site.type) {
            V3CountySiteType.Shrine -> V3SiteYield(influence = scale(1), cohesion = scale(2), desc = "凝聚与族望")
            V3CountySiteType.Farmland -> V3SiteYield(grain = scale(34), cohesion = scale(1), desc = "粮食主产")
            V3CountySiteType.Market -> V3SiteYield(silver = scale(24), desc = "银两主产")
            V3CountySiteType.Yamen -> V3SiteYield(silver = scale(5), influence = scale(2), desc = "赋役缓冲")
            V3CountySiteType.Academy -> V3SiteYield(influence = scale(3), cohesion = scale(1), desc = "读书声望")
            V3CountySiteType.Clinic -> V3SiteYield(cohesion = scale(3), grain = scale(4), desc = "医药民心")
            V3CountySiteType.Fort -> V3SiteYield(militia = scale(5), cohesion = scale(1), desc = "乡勇防务")
            V3CountySiteType.Dock -> V3SiteYield(silver = scale(18), influence = scale(1), desc = "码头商路")
            V3CountySiteType.MountainPass -> V3SiteYield(silver = scale(8), militia = scale(2), desc = "山道小利")
        }
    }

    fun monthlyForecast(state: V3GameState): V3MonthlyForecast {
        var silverIncome = 0
        var grainIncome = 0
        var influenceIncome = 0
        var cohesionIncome = 0
        var militiaIncome = 0
        state.sites.forEach { site ->
            val yield = siteYield(site)
            silverIncome += yield.silver
            grainIncome += yield.grain
            influenceIncome += yield.influence
            cohesionIncome += yield.cohesion
            militiaIncome += yield.militia
        }
        state.sites.forEach { site ->
            val person = site.assignedPersonId?.let { id -> state.people.firstOrNull { it.id == id } } ?: return@forEach
            val task = person.currentTask ?: return@forEach
            val power = taskPower(person.study, person.martial, person.commerce, person.diplomacy, task)
            val silver = taskSilver(task, power)
            val grain = taskGrain(task, power)
            if (silver > 0) silverIncome += silver
            if (grain > 0) grainIncome += grain
        }
        val silverExpense = monthlySilverExpense(state)
        val grainExpense = monthlyGrainExpense(state)
        val netSilver = silverIncome - silverExpense
        val netGrain = grainIncome - grainExpense
        val dangerSites = state.sites.count { it.risk >= 55 }
        val summary = "预计本月 银${signed(netSilver)} / 粮${signed(netGrain)}，危险地点 $dangerSites 处。"
        return V3MonthlyForecast(silverIncome, grainIncome, influenceIncome, cohesionIncome, militiaIncome, silverExpense, grainExpense, dangerSites, summary)
    }

    fun advanceMonth(state: V3GameState): V3MonthlyReport {
        var silverDelta = 0
        var grainDelta = 0
        var influenceDelta = 0
        var cohesionDelta = 0
        var militiaDelta = 0
        var relations = state.relations
        val assignmentResults = mutableMapOf<Int, V3TaskType>()
        val routeDelta = mutableMapOf<V3Route, Int>()
        val detailLines = mutableListOf<String>()
        val incomeParts = mutableListOf<String>()

        state.sites.forEach { site ->
            val yield = siteYield(site)
            silverDelta += yield.silver
            grainDelta += yield.grain
            influenceDelta += yield.influence
            cohesionDelta += yield.cohesion
            militiaDelta += yield.militia
            if (site.level > 0 && (yield.silver + yield.grain + yield.influence + yield.cohesion + yield.militia) > 0) {
                incomeParts += "${site.name}${yieldText(yield)}"
            }
        }

        val sites = state.sites.map { site ->
            val assigned = site.assignedPersonId?.let { id -> state.people.firstOrNull { it.id == id && it.alive } }
            if (assigned == null) {
                val riskRise = if (site.level > 0 || site.risk >= 50) 2 else 1
                val risk = (site.risk + riskRise).coerceAtMost(95)
                site.copy(risk = risk, status = statusFor(site.control, risk))
            } else {
                val task = assigned.currentTask ?: V3TaskType.Govern
                val power = taskPower(assigned.study, assigned.martial, assigned.commerce, assigned.diplomacy, task)
                val controlGain = max(2, power / 16)
                val riskDrop = max(1, power / 20)
                silverDelta += taskSilver(task, power)
                grainDelta += taskGrain(task, power)
                influenceDelta += taskInfluence(task)
                cohesionDelta += taskCohesion(task)
                militiaDelta += taskMilitia(task, power)
                relations = applyRelation(relations, task)
                routeDelta[routeFor(task)] = (routeDelta[routeFor(task)] ?: 0) + 2
                assignmentResults[assigned.id] = task
                val nextControl = (site.control + controlGain).coerceAtMost(100)
                val nextRisk = (site.risk - riskDrop).coerceAtLeast(0)
                detailLines += "${assigned.name}在${site.name}${task.label}：控+$controlGain，险-$riskDrop。"
                site.copy(control = nextControl, risk = nextRisk, status = statusFor(nextControl, nextRisk))
            }
        }

        val silverExpense = monthlySilverExpense(state)
        val grainExpense = monthlyGrainExpense(state)
        silverDelta -= silverExpense
        grainDelta -= grainExpense

        val nextMonth = if (state.month == 12) 1 else state.month + 1
        val nextYear = if (state.month == 12) state.year + 1 else state.year
        if (state.month == 12) {
            influenceDelta += 2
            detailLines += "岁末修谱，族望+2，所有族人年长一岁。"
        }

        val nextRoutes = state.routeScores.mapValues { (route, value) -> value + (routeDelta[route] ?: 0) }
        val grownPeople = growPeople(state.people, assignmentResults, detailLines, state.month == 12)
        val nextBranches = updateBranches(state.branches, grownPeople, assignmentResults, silverDelta, grainDelta, detailLines)
        val nextSites = sites.map { it.copy(assignedPersonId = null) }
        var settledState = state.copy(
            year = nextYear,
            month = nextMonth,
            silver = (state.silver + silverDelta).coerceAtLeast(-999),
            grain = (state.grain + grainDelta).coerceAtLeast(-999),
            influence = (state.influence + influenceDelta).coerceIn(0, 100),
            cohesion = (state.cohesion + cohesionDelta).coerceIn(0, 100),
            militia = (state.militia + militiaDelta).coerceIn(0, 999),
            sites = nextSites,
            people = grownPeople,
            branches = nextBranches,
            relations = relations,
            routeScores = nextRoutes,
            pendingReports = emptyList()
        )
        settledState = maybeAddChild(settledState, detailLines)

        val summary = mutableListOf<String>()
        summary += "本月收支：银${signed(silverDelta)}，粮${signed(grainDelta)}，族望${signed(influenceDelta)}，凝聚${signed(cohesionDelta)}，乡勇${signed(militiaDelta)}。"
        if (incomeParts.isNotEmpty()) summary += "产业进项：${incomeParts.take(4).joinToString("；")}${if (incomeParts.size > 4) "等" else ""}。"
        summary += "固定消耗：赋役银-$silverExpense，人丁与乡勇粮-$grainExpense。"
        summary += detailLines.take(6)

        val nextState = evaluateAnnualGoals(
            settledState.copy(
                pendingReports = summary,
                eventLog = (summary.map { "${state.year}年${state.month}月 · $it" } + state.eventLog).take(80)
            ),
            summary,
            state.month == 12
        )
        return V3MonthlyReport("${state.year}年${state.month}月家业结算", summary, nextState)
    }

    fun dominantRoute(state: V3GameState): V3Route = state.routeScores.maxByOrNull { it.value }?.key ?: V3Route.Hermit

    fun screenTitle(screen: V3Screen): String = when (screen) {
        V3Screen.County -> "家业"
        V3Screen.Clan -> "宗族"
        V3Screen.People -> "族人"
        V3Screen.Strategy -> "大势"
    }

    fun goalProgress(state: V3GameState, goal: V3AnnualGoal): Int = when (goal.metric) {
        V3GoalMetric.SilverStock -> state.silver
        V3GoalMetric.GrainStock -> state.grain
        V3GoalMetric.Militia -> state.militia
        V3GoalMetric.Cohesion -> state.cohesion
        V3GoalMetric.Influence -> state.influence
        V3GoalMetric.ControlledSites -> state.sites.count { it.control >= 50 }
        V3GoalMetric.SafeSites -> state.sites.count { it.risk < 30 }
        V3GoalMetric.BuiltSites -> builtSiteCount(state)
        V3GoalMetric.Population -> alivePeople(state).size
        V3GoalMetric.ClanRank -> state.clanRank
        V3GoalMetric.RouteScore -> state.routeScores[goal.route] ?: 0
        V3GoalMetric.RelationTotal -> relationTotal(state.relations)
    }

    fun endingPreview(state: V3GameState): V3EndingPreview {
        val route = dominantRoute(state)
        val routeScore = state.routeScores[route] ?: 0
        val stableSites = state.sites.count { it.risk < 35 && it.control >= 40 }
        val resourceScore = (state.silver / 22) + (state.grain / 30) + (state.militia / 10)
        val familyScore = alivePeople(state).size * 3 + state.clanRank * 12 + builtSiteCount(state) * 5
        val relationScore = relationTotal(state.relations) / 8
        val score = routeScore + stableSites * 4 + resourceScore + familyScore + relationScore + state.cohesion / 5 + state.influence / 4
        val tier = when {
            score >= 150 -> V3EndingTier.Historic
            score >= 105 -> V3EndingTier.Strong
            score >= 65 -> V3EndingTier.Viable
            else -> V3EndingTier.Fragile
        }
        val plan = V3Content.routePlans.firstOrNull { it.route == route }
        return V3EndingPreview(
            route = route,
            tier = tier,
            score = score,
            title = plan?.endingName ?: route.label,
            desc = plan?.goal ?: "宗族仍在乱世中寻找自己的归宿。"
        )
    }

    fun siteStatusFor(control: Int, risk: Int): V3SiteStatus = statusFor(control, risk)

    fun shouldAutoEnd(state: V3GameState): Boolean = state.year >= 1644 || state.cohesion <= 0 || state.silver <= -300 || state.grain <= -300

    fun finalizeEnding(state: V3GameState): V3FinalEnding {
        val preview = endingPreview(state)
        val bestPerson = alivePeople(state).maxByOrNull { it.merit }
        val stableSites = state.sites.count { it.risk < 35 && it.control >= 40 }
        val heirCount = alivePeople(state).count { it.generation >= 2 }
        val body = when (preview.route) {
            V3Route.Scholar -> "${state.clanName}由一户起家，终以书院、族学与士绅网络立身。子嗣中读书者渐多，家乘从草纸变成可传后世的谱牒。"
            V3Route.Merchant -> "从一处田产、一间铺面开始，${state.clanName}把集市、码头与账房连成家业。乱世中不只求活，还求财源不断。"
            V3Route.Fortress -> "${state.clanName}先成家，再置产，后筑寨募勇。香火与围墙一起长成，在兵荒马乱中守住一方烟火。"
            V3Route.Loyalist -> "家业渐成后，${state.clanName}选择与县衙、士绅和军需绑定，以名义换庇护，也承受赋役之重。"
            V3Route.Warlord -> "当法度崩坏，家族人口、产业与乡勇逐渐合成地方力量。${state.clanName}已不只是小户，而是县中不可忽视的大姓。"
            V3Route.Overseas -> "从田庄到码头，${state.clanName}把一部分家业押向海路。即便王朝风雨飘摇，香火仍可在远方延续。"
            V3Route.Hermit -> "不争一时显赫，只求成家、育子、置产、修祠。${state.clanName}在乱世缝隙里守住了大明浮生志最本质的一条路：活下去，传下去。"
        }
        return V3FinalEnding(
            route = preview.route,
            tier = preview.tier,
            score = preview.score,
            title = preview.title,
            body = body,
            stats = listOf(
                "终局时间：${state.year}年${state.month}月",
                "宗族品第：${clanRankName(state)}",
                "家族人口：${alivePeople(state).size}（子嗣 $heirCount）",
                "经营产业：${builtSiteCount(state)} 处",
                "稳定地点：$stableSites / ${state.sites.size}",
                "最有功绩族人：${bestPerson?.name ?: "无"}（${bestPerson?.merit ?: 0}）"
            )
        )
    }

    private fun growPeople(people: List<V3Person>, assignments: Map<Int, V3TaskType>, lines: MutableList<String>, yearEnded: Boolean): List<V3Person> {
        return people.map { person ->
            val baseAge = if (yearEnded) person.age + 1 else person.age
            val task = assignments[person.id]
            if (task == null) {
                person.copy(age = baseAge, fatigue = (person.fatigue - 8).coerceAtLeast(0), currentTask = null, assignedSiteId = null)
            } else {
                val grow = growthFor(task)
                val next = person.copy(
                    age = baseAge,
                    study = (person.study + grow.study).coerceAtMost(100),
                    martial = (person.martial + grow.martial).coerceAtMost(100),
                    commerce = (person.commerce + grow.commerce).coerceAtMost(100),
                    diplomacy = (person.diplomacy + grow.diplomacy).coerceAtMost(100),
                    loyalty = (person.loyalty + grow.loyalty).coerceIn(0, 100),
                    merit = (person.merit + grow.merit).coerceAtMost(999),
                    fatigue = (person.fatigue + grow.fatigue).coerceIn(0, 100),
                    currentTask = null,
                    assignedSiteId = null
                )
                if (grow.merit > 0) lines += "${person.name}历练有进，功绩+${grow.merit}，疲劳${next.fatigue}。"
                next
            }
        }
    }

    private fun maybeAddChild(state: V3GameState, lines: MutableList<String>): V3GameState {
        val husband = state.people.firstOrNull { it.gender == V3Gender.Male && it.spouseId != null && it.age in 16..55 } ?: return state
        val wife = state.people.firstOrNull { it.id == husband.spouseId && it.age in 16..45 } ?: return state
        val limit = 2 + state.clanRank * 4
        if (alivePeople(state).size >= limit) return state
        val tick = state.year * 12 + state.month + state.people.size
        if (tick % 8 != 0) return state
        val childId = state.nextPersonId
        val gender = if (childId % 2 == 0) V3Gender.Male else V3Gender.Female
        val childName = "李${if (gender == V3Gender.Male) boyNames[(childId + state.year) % boyNames.size] else girlNames[(childId + state.year) % girlNames.size]}"
        val child = V3Person(
            id = childId,
            name = childName,
            age = 0,
            branch = "主房",
            identity = "幼子",
            trait = if (gender == V3Gender.Male) com.daming.fushengzhi3.v3.data.V3Trait.Ambitious else com.daming.fushengzhi3.v3.data.V3Trait.Studious,
            study = 1,
            martial = 1,
            commerce = 1,
            diplomacy = 1,
            loyalty = 100,
            gender = gender,
            generation = max(husband.generation, wife.generation) + 1,
            parentId = husband.id
        )
        val people = state.people.map {
            when (it.id) {
                husband.id, wife.id -> it.copy(childrenIds = it.childrenIds + childId)
                else -> it
            }
        } + child
        lines += "${husband.name}与${wife.name}添丁，${child.name}出生，香火+1。"
        return state.copy(
            people = people,
            nextPersonId = childId + 1,
            cohesion = (state.cohesion + 3).coerceAtMost(100),
            influence = (state.influence + 1).coerceAtMost(100)
        )
    }

    private fun updateBranches(
        branches: List<V3Branch>,
        people: List<V3Person>,
        assignments: Map<Int, V3TaskType>,
        silverDelta: Int,
        grainDelta: Int,
        lines: MutableList<String>
    ): List<V3Branch> {
        val activeByBranch = people.filter { assignments.containsKey(it.id) }.groupingBy { it.branch }.eachCount()
        return branches.map { branch ->
            val active = activeByBranch[branch.name] ?: 0
            val wealthShift = when {
                active > 0 && silverDelta > 0 -> 1
                silverDelta < -35 -> -1
                else -> 0
            }
            val grievanceShift = when {
                active == 0 && people.size >= 5 -> 1
                grainDelta < -45 -> 1
                else -> -1
            }
            val next = branch.copy(
                loyalty = (branch.loyalty + if (active > 0) 1 else 0).coerceIn(0, 100),
                wealth = (branch.wealth + wealthShift).coerceIn(0, 100),
                influence = (branch.influence + if (active > 0) 1 else 0).coerceIn(0, 100),
                grievance = (branch.grievance + grievanceShift).coerceIn(0, 100)
            )
            if (next.grievance >= 35 && branch.grievance < 35) lines += "${branch.name}怨气渐重，需在宗族页留意。"
            next
        }
    }

    private data class GrowthDelta(
        val study: Int = 0,
        val martial: Int = 0,
        val commerce: Int = 0,
        val diplomacy: Int = 0,
        val loyalty: Int = 0,
        val merit: Int = 1,
        val fatigue: Int = 8
    )

    private fun growthFor(task: V3TaskType): GrowthDelta = when (task) {
        V3TaskType.Govern -> GrowthDelta(study = 1, diplomacy = 1, loyalty = 1, merit = 2, fatigue = 7)
        V3TaskType.Farm -> GrowthDelta(study = 1, commerce = 1, loyalty = 1, merit = 1, fatigue = 6)
        V3TaskType.Trade -> GrowthDelta(commerce = 2, diplomacy = 1, merit = 2, fatigue = 8)
        V3TaskType.Study -> GrowthDelta(study = 2, diplomacy = 1, merit = 2, fatigue = 5)
        V3TaskType.Diplomacy -> GrowthDelta(diplomacy = 2, study = 1, merit = 2, fatigue = 7)
        V3TaskType.Relief -> GrowthDelta(study = 1, diplomacy = 1, loyalty = 2, merit = 2, fatigue = 9)
        V3TaskType.Fortify -> GrowthDelta(martial = 2, loyalty = 1, merit = 2, fatigue = 10)
        V3TaskType.Scout -> GrowthDelta(martial = 1, diplomacy = 1, merit = 2, fatigue = 10)
        V3TaskType.Recruit -> GrowthDelta(martial = 2, merit = 2, fatigue = 9)
    }

    private fun evaluateAnnualGoals(state: V3GameState, lines: MutableList<String>, yearEnded: Boolean): V3GameState {
        var silver = state.silver
        var grain = state.grain
        var influence = state.influence
        var cohesion = state.cohesion
        val goalLines = mutableListOf<String>()
        var goals = state.annualGoals.map { goal ->
            if (!goal.completed && goalProgress(state, goal) >= goal.target) {
                silver += goal.rewardSilver
                grain += goal.rewardGrain
                influence += goal.rewardInfluence
                cohesion += goal.rewardCohesion
                val line = "目标【${goal.title}】完成，奖励已入账。"
                lines += line
                goalLines += line
                goal.copy(completed = true)
            } else goal
        }
        if (yearEnded) {
            val unfinished = goals.filter { !it.completed }
            unfinished.take(2).forEach { goal ->
                val line = "目标【${goal.title}】未竟：${goalProgress(state, goal)}/${goal.target}。"
                lines += line
                goalLines += line
            }
            val activeGoals = unfinished.takeLast(2).toMutableList()
            val replacement = nextAnnualGoal(state, activeGoals)
            if (replacement != null && activeGoals.none { it.id == replacement.id }) {
                activeGoals += replacement.copy(completed = false)
                val line = "新目标【${replacement.title}】列入家业计划。"
                lines += line
                goalLines += line
            }
            goals = activeGoals.take(3)
        }
        if (goals.isEmpty()) goals = V3Content.goalsFor(state.creed, state.crisis)
        return state.copy(
            silver = silver.coerceAtLeast(-999),
            grain = grain.coerceAtLeast(-999),
            influence = influence.coerceIn(0, 100),
            cohesion = cohesion.coerceIn(0, 100),
            annualGoals = goals,
            pendingReports = lines,
            eventLog = (goalLines.map { "${state.year}年${state.month}月 · $it" } + state.eventLog).take(80)
        )
    }

    private fun nextAnnualGoal(state: V3GameState, activeGoals: List<V3AnnualGoal>): V3AnnualGoal? {
        val used = activeGoals.map { it.id }.toSet()
        val route = dominantRoute(state)
        val pool = V3Content.initialAnnualGoals.filter { it.id !in used }
        return when {
            !hasSpouse(state) -> pool.firstOrNull { it.id == "marry" }
            alivePeople(state).size < 3 -> pool.firstOrNull { it.id == "child_3" }
            builtSiteCount(state) < 2 -> pool.firstOrNull { it.id == "build_2" }
            !canRankUp(state) -> pool.firstOrNull { it.id == "rank_2" }
            else -> pool.firstOrNull { it.route == route } ?: pool.firstOrNull()
        }
    }

    private fun taskPower(study: Int, martial: Int, commerce: Int, diplomacy: Int, task: V3TaskType): Int = when (task) {
        V3TaskType.Govern -> (study + diplomacy) / 2
        V3TaskType.Farm -> (study + commerce) / 2
        V3TaskType.Trade -> commerce
        V3TaskType.Study -> study
        V3TaskType.Diplomacy -> diplomacy
        V3TaskType.Relief -> (study + diplomacy) / 2
        V3TaskType.Fortify -> martial
        V3TaskType.Scout -> (martial + diplomacy) / 2
        V3TaskType.Recruit -> martial
    }

    private fun taskSilver(task: V3TaskType, power: Int): Int = when (task) {
        V3TaskType.Trade -> max(4, power / 4)
        V3TaskType.Diplomacy -> -4
        V3TaskType.Farm -> 1
        V3TaskType.Relief -> -8
        V3TaskType.Fortify -> -10
        V3TaskType.Recruit -> -12
        else -> 0
    }

    private fun taskGrain(task: V3TaskType, power: Int): Int = when (task) {
        V3TaskType.Farm -> max(8, power / 3)
        V3TaskType.Relief -> -16
        V3TaskType.Recruit -> -8
        else -> 0
    }

    private fun taskInfluence(task: V3TaskType): Int = when (task) {
        V3TaskType.Study, V3TaskType.Diplomacy, V3TaskType.Govern -> 1
        else -> 0
    }

    private fun taskCohesion(task: V3TaskType): Int = when (task) {
        V3TaskType.Relief, V3TaskType.Govern -> 2
        V3TaskType.Trade -> -1
        else -> 0
    }

    private fun taskMilitia(task: V3TaskType, power: Int): Int = when (task) {
        V3TaskType.Recruit -> max(2, power / 8)
        V3TaskType.Fortify -> 1
        else -> 0
    }

    private fun monthlySilverExpense(state: V3GameState): Int = 3 + state.clanRank * 2 + state.militia / 35

    private fun monthlyGrainExpense(state: V3GameState): Int {
        var peopleCost = 0
        alivePeople(state).forEach { person ->
            peopleCost += if (person.age < 12) 1 else 3
        }
        return peopleCost + state.militia / 8
    }

    private fun routeFor(task: V3TaskType): V3Route = when (task) {
        V3TaskType.Study -> V3Route.Scholar
        V3TaskType.Trade -> V3Route.Merchant
        V3TaskType.Fortify, V3TaskType.Recruit -> V3Route.Fortress
        V3TaskType.Diplomacy -> V3Route.Loyalist
        V3TaskType.Scout -> V3Route.Warlord
        V3TaskType.Relief, V3TaskType.Govern, V3TaskType.Farm -> V3Route.Hermit
    }

    private fun routeForSite(site: V3CountySite): V3Route = when (site.type) {
        V3CountySiteType.Shrine, V3CountySiteType.Farmland, V3CountySiteType.Clinic -> V3Route.Hermit
        V3CountySiteType.Market -> V3Route.Merchant
        V3CountySiteType.Yamen -> V3Route.Loyalist
        V3CountySiteType.Academy -> V3Route.Scholar
        V3CountySiteType.Fort -> V3Route.Fortress
        V3CountySiteType.Dock -> V3Route.Overseas
        V3CountySiteType.MountainPass -> V3Route.Warlord
    }

    private fun applyRelation(relations: V3Relations, task: V3TaskType): V3Relations = when (task) {
        V3TaskType.Diplomacy -> relations.copy(yamen = clamp(relations.yamen + 4), gentry = clamp(relations.gentry + 2))
        V3TaskType.Relief -> relations.copy(villagers = clamp(relations.villagers + 5), gentry = clamp(relations.gentry - 1))
        V3TaskType.Trade -> relations.copy(merchants = clamp(relations.merchants + 4))
        V3TaskType.Recruit -> relations.copy(garrison = clamp(relations.garrison + 2), bandits = clamp(relations.bandits - 2))
        V3TaskType.Scout -> relations.copy(bandits = clamp(relations.bandits - 4))
        else -> relations
    }

    private fun statusFor(control: Int, risk: Int): V3SiteStatus = when {
        risk >= 70 -> V3SiteStatus.Threatened
        control <= 20 -> V3SiteStatus.Blighted
        control >= 80 && risk <= 20 -> V3SiteStatus.Prosperous
        risk >= 35 -> V3SiteStatus.Strained
        else -> V3SiteStatus.Stable
    }

    private fun hasSpouse(state: V3GameState): Boolean = state.people.any { it.spouseId != null }

    private fun yieldText(yield: V3SiteYield): String {
        val parts = mutableListOf<String>()
        if (yield.silver != 0) parts += "银+${yield.silver}"
        if (yield.grain != 0) parts += "粮+${yield.grain}"
        if (yield.influence != 0) parts += "望+${yield.influence}"
        if (yield.cohesion != 0) parts += "凝+${yield.cohesion}"
        if (yield.militia != 0) parts += "勇+${yield.militia}"
        return if (parts.isEmpty()) "无月产" else parts.joinToString("/")
    }

    private fun yieldDiffText(before: V3SiteYield, after: V3SiteYield): String {
        val diff = V3SiteYield(
            silver = after.silver - before.silver,
            grain = after.grain - before.grain,
            influence = after.influence - before.influence,
            cohesion = after.cohesion - before.cohesion,
            militia = after.militia - before.militia
        )
        return "月产提升 ${yieldText(diff)}"
    }

    private fun signed(value: Int): String = if (value >= 0) "+$value" else value.toString()

    private fun relationTotal(relations: V3Relations): Int {
        return relations.yamen + relations.gentry + relations.villagers + relations.merchants + relations.garrison - relations.bandits
    }

    private fun clamp(value: Int): Int = min(100, max(-100, value))

    private val boyNames = listOf("守成", "承祖", "怀远", "景行", "念安", "修齐", "启明", "存义")
    private val girlNames = listOf("采薇", "若兰", "念慈", "静姝", "安禾", "清婉", "素心", "云娘")
}
