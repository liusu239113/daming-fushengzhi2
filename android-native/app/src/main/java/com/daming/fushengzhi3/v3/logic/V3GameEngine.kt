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
import com.daming.fushengzhi3.v3.data.V3GoalMetric
import com.daming.fushengzhi3.v3.data.V3MonthlyReport
import com.daming.fushengzhi3.v3.data.V3Person
import com.daming.fushengzhi3.v3.data.V3Relations
import com.daming.fushengzhi3.v3.data.V3Route
import com.daming.fushengzhi3.v3.data.V3Screen
import com.daming.fushengzhi3.v3.data.V3SiteStatus
import com.daming.fushengzhi3.v3.data.V3TaskType
import com.daming.fushengzhi3.v3.data.V3UpgradeCost
import kotlin.math.max
import kotlin.math.min

object V3GameEngine {
    fun upgradeCost(site: V3CountySite): V3UpgradeCost? {
        if (site.level >= 3) return null
        val nextLevel = site.level + 1
        val silver = 45 + nextLevel * 35 + site.risk / 3
        val grain = 25 + nextLevel * 20
        val desc = when (site.type) {
            V3CountySiteType.Shrine -> "修谱立规，提升宗族凝聚与地点控制。"
            V3CountySiteType.Farmland -> "修渠筑仓，增加粮食根基并降低田庄风险。"
            V3CountySiteType.Market -> "整顿摊税，提升商路收益与商帮关系。"
            V3CountySiteType.Yamen -> "打点差役，降低催税压力并改善官府周旋。"
            V3CountySiteType.Academy -> "扩建讲舍，推动耕读与士绅路线。"
            V3CountySiteType.Clinic -> "购置药材，增强瘟疫与伤病缓冲。"
            V3CountySiteType.Fort -> "修墙备械，增强自保与乡勇路线。"
            V3CountySiteType.Dock -> "修埠置仓，打开海贸与远渡伏笔。"
            V3CountySiteType.MountainPass -> "设卡巡山，压低流寇与私盐风险。"
        }
        return V3UpgradeCost(silver, grain, desc)
    }

    fun canUpgrade(state: V3GameState, siteId: String): Boolean {
        val site = state.sites.firstOrNull { it.id == siteId } ?: return false
        val cost = upgradeCost(site) ?: return false
        return state.silver >= cost.silver && state.grain >= cost.grain
    }

    fun upgradeSite(state: V3GameState, siteId: String): V3GameState {
        val site = state.sites.firstOrNull { it.id == siteId } ?: return state
        val cost = upgradeCost(site) ?: return state.copy(pendingReports = listOf("${site.name}已达最高等级。"))
        if (state.silver < cost.silver || state.grain < cost.grain) {
            return state.copy(pendingReports = listOf("资源不足：升级${site.name}需要银${cost.silver}、粮${cost.grain}。"))
        }
        val nextSite = site.copy(
            level = site.level + 1,
            control = (site.control + 12 + site.level * 3).coerceAtMost(100),
            risk = (site.risk - 10 - site.level * 2).coerceAtLeast(0)
        )
        val route = routeForSite(site)
        val routes = state.routeScores + (route to ((state.routeScores[route] ?: 0) + 4))
        val message = "营建【${site.name}】至 Lv.${nextSite.level}：${cost.desc}"
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
        val person = state.people.firstOrNull { it.id == personId } ?: return state
        val site = state.sites.firstOrNull { it.id == siteId && it.taskTypes.contains(task) } ?: return state
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
        return state.copy(
            people = people,
            sites = sites,
            pendingReports = listOf("${person.name}已前往【${site.name}】执行【${task.label}】。")
        )
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
        val lines = mutableListOf<String>()

        val sites = state.sites.map { site ->
            val assigned = site.assignedPersonId?.let { id -> state.people.firstOrNull { it.id == id } }
            if (assigned == null) {
                val risk = (site.risk + 3).coerceAtMost(95)
                val status = statusFor(site.control, risk)
                lines += "${site.name}无人照看，风险升至$risk。"
                site.copy(risk = risk, status = status)
            } else {
                val task = assigned.currentTask ?: V3TaskType.Govern
                val power = taskPower(assigned.study, assigned.martial, assigned.commerce, assigned.diplomacy, task)
                val controlGain = max(3, power / 14)
                val riskDrop = max(2, power / 18)
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
                lines += "${assigned.name}在${site.name}${task.label}有成，控制+$controlGain，风险-$riskDrop。"
                site.copy(control = nextControl, risk = nextRisk, status = statusFor(nextControl, nextRisk))
            }
        }

        val pressure = state.year - 1600 + state.month / 6
        val taxPressure = 6 + pressure / 3
        val grainConsume = 18 + state.people.size * 2
        silverDelta -= taxPressure
        grainDelta -= grainConsume
        lines += "本月税赋与人丁消耗：银-$taxPressure，粮-$grainConsume。"

        val nextMonth = if (state.month == 12) 1 else state.month + 1
        val nextYear = if (state.month == 12) state.year + 1 else state.year
        if (state.month == 12) {
            influenceDelta += 2
            lines += "岁末修谱，宗族名望略有增长。"
        }

        val nextRoutes = state.routeScores.mapValues { (route, value) -> value + (routeDelta[route] ?: 0) }
        val nextPeople = growPeople(state.people, assignmentResults, lines)
        val nextBranches = updateBranches(state.branches, nextPeople, assignmentResults, silverDelta, grainDelta, lines)
        val nextSites = sites.map { it.copy(assignedPersonId = null) }
        val settledState = state.copy(
            year = nextYear,
            month = nextMonth,
            silver = (state.silver + silverDelta).coerceAtLeast(-999),
            grain = (state.grain + grainDelta).coerceAtLeast(-999),
            influence = (state.influence + influenceDelta).coerceIn(0, 100),
            cohesion = (state.cohesion + cohesionDelta).coerceIn(0, 100),
            militia = (state.militia + militiaDelta).coerceIn(0, 999),
            sites = nextSites,
            people = nextPeople,
            branches = nextBranches,
            relations = relations,
            routeScores = nextRoutes,
            pendingReports = lines,
            eventLog = (lines.map { "${state.year}年${state.month}月 · $it" } + state.eventLog).take(80)
        )
        val nextState = evaluateAnnualGoals(settledState, lines, state.month == 12)
        return V3MonthlyReport("${state.year}年${state.month}月县域结算", lines, nextState)
    }

    fun dominantRoute(state: V3GameState): V3Route = state.routeScores.maxByOrNull { it.value }?.key ?: V3Route.Hermit

    fun screenTitle(screen: V3Screen): String = when (screen) {
        V3Screen.County -> "县域"
        V3Screen.Clan -> "宗族"
        V3Screen.People -> "人物"
        V3Screen.Strategy -> "政略"
    }

    fun goalProgress(state: V3GameState, goal: V3AnnualGoal): Int = when (goal.metric) {
        V3GoalMetric.SilverStock -> state.silver
        V3GoalMetric.GrainStock -> state.grain
        V3GoalMetric.Militia -> state.militia
        V3GoalMetric.Cohesion -> state.cohesion
        V3GoalMetric.Influence -> state.influence
        V3GoalMetric.ControlledSites -> state.sites.count { it.control >= 50 }
        V3GoalMetric.SafeSites -> state.sites.count { it.risk < 30 }
        V3GoalMetric.RouteScore -> state.routeScores[goal.route] ?: 0
        V3GoalMetric.RelationTotal -> relationTotal(state.relations)
    }

    fun endingPreview(state: V3GameState): V3EndingPreview {
        val route = dominantRoute(state)
        val routeScore = state.routeScores[route] ?: 0
        val stableSites = state.sites.count { it.risk < 35 && it.control >= 40 }
        val resourceScore = (state.silver / 25) + (state.grain / 35) + (state.militia / 12)
        val relationScore = relationTotal(state.relations) / 8
        val score = routeScore + stableSites * 5 + resourceScore + relationScore + state.cohesion / 4 + state.influence / 4
        val tier = when {
            score >= 130 -> V3EndingTier.Historic
            score >= 95 -> V3EndingTier.Strong
            score >= 60 -> V3EndingTier.Viable
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
        val bestPerson = state.people.maxByOrNull { it.merit }
        val strongestBranch = state.branches.maxByOrNull { it.influence }
        val stableSites = state.sites.count { it.risk < 35 && it.control >= 40 }
        val body = when (preview.route) {
            V3Route.Scholar -> "乱世之中，${state.clanName}以书院、士绅与族学立身。${bestPerson?.name ?: "族中后进"}成为家乘中最耀眼的一笔，宗族虽历兵火，仍以文脉留名。"
            V3Route.Merchant -> "县域商路重开，银钱、码头与集市连成网络。${state.clanName}不再只靠田庄，而以商号维系族人，成为乱世中的富族。"
            V3Route.Fortress -> "寨堡、乡勇和宗祠合为一体。${state.clanName}未必显赫于朝堂，却能保境安民，在山河破碎时守住一方烟火。"
            V3Route.Loyalist -> "辽饷、军需与县衙文书都曾压到宗祠门前。${state.clanName}终究选择勤王从命，换得名义与官府庇护，也背上沉重代价。"
            V3Route.Warlord -> "当法度崩坏，宗族以武力自立。${strongestBranch?.name ?: "强房"}势力坐大，${state.clanName}在县域内近似一方豪强。"
            V3Route.Overseas -> "码头暗线通向远海。${state.clanName}将部分族产与族人送往海路，在王朝倾覆前留下另一条活路。"
            V3Route.Hermit -> "不争名利，不逐权势。${state.clanName}以祠堂、田庄、医馆和乡约维系人心，乱世中求得一线安稳。"
        }
        return V3FinalEnding(
            route = preview.route,
            tier = preview.tier,
            score = preview.score,
            title = preview.title,
            body = body,
            stats = listOf(
                "终局时间：${state.year}年${state.month}月",
                "宗族凝聚：${state.cohesion}",
                "族望声名：${state.influence}",
                "稳定地点：$stableSites / ${state.sites.size}",
                "最有功绩族人：${bestPerson?.name ?: "无"}（${bestPerson?.merit ?: 0}）",
                "最强房支：${strongestBranch?.name ?: "无"}（势${strongestBranch?.influence ?: 0} / 怨${strongestBranch?.grievance ?: 0}）"
            )
        )
    }

    private fun growPeople(people: List<V3Person>, assignments: Map<Int, V3TaskType>, lines: MutableList<String>): List<V3Person> {
        return people.map { person ->
            val task = assignments[person.id]
            if (task == null) {
                person.copy(
                    fatigue = (person.fatigue - 8).coerceAtLeast(0),
                    currentTask = null,
                    assignedSiteId = null
                )
            } else {
                val grow = growthFor(task)
                val next = person.copy(
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
                if (grow.merit > 0) {
                    lines += "${person.name}历练有进，功绩+${grow.merit}，疲劳升至${next.fatigue}。"
                }
                next
            }
        }
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
                active > 0 && silverDelta > 0 -> 2
                silverDelta < -25 -> -1
                else -> 0
            }
            val grievanceShift = when {
                active == 0 -> 2
                grainDelta < -40 -> 2
                branch.focus == dominantBranchRoute(assignments, people, branch.name) -> -2
                else -> -1
            }
            val loyaltyShift = if (active > 0) 1 else -1
            val influenceShift = if (active > 0) 1 else 0
            val next = branch.copy(
                loyalty = (branch.loyalty + loyaltyShift).coerceIn(0, 100),
                wealth = (branch.wealth + wealthShift).coerceIn(0, 100),
                influence = (branch.influence + influenceShift).coerceIn(0, 100),
                grievance = (branch.grievance + grievanceShift).coerceIn(0, 100)
            )
            if (next.grievance >= 35 && branch.grievance < 35) {
                lines += "${branch.name}怨气渐重，后续可能触发房支争产。"
            }
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

    private fun dominantBranchRoute(assignments: Map<Int, V3TaskType>, people: List<V3Person>, branchName: String): V3Route? {
        val routes = people.filter { it.branch == branchName }.mapNotNull { person -> assignments[person.id]?.let { routeFor(it) } }
        return routes.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
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
                val line = "年度目标【${goal.title}】完成，宗族路线【${goal.route.label}】更稳。"
                lines += line
                goalLines += line
                goal.copy(completed = true)
            } else {
                goal
            }
        }
        if (yearEnded) {
            val unfinished = goals.filter { !it.completed }
            unfinished.forEach { goal ->
                val line = "年度目标【${goal.title}】未竟，当前进度 ${goalProgress(state, goal)}/${goal.target}。"
                lines += line
                goalLines += line
            }
            val activeGoals = unfinished.takeLast(2).toMutableList()
            val replacement = nextAnnualGoal(state, activeGoals)
            if (replacement != null && activeGoals.none { it.id == replacement.id }) {
                activeGoals += replacement.copy(completed = false)
                val line = "新岁目标【${replacement.title}】列入宗祠议程。"
                lines += line
                goalLines += line
            }
            goals = activeGoals.take(3)
        }
        if (goals.isEmpty()) {
            goals = V3Content.goalsFor(state.creed, state.crisis)
        }
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
        return pool.firstOrNull { it.route == route } ?: pool.firstOrNull()
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
        V3TaskType.Trade -> power / 4
        V3TaskType.Diplomacy -> 3
        V3TaskType.Farm -> 1
        V3TaskType.Relief -> -8
        V3TaskType.Fortify -> -10
        V3TaskType.Recruit -> -12
        else -> 0
    }

    private fun taskGrain(task: V3TaskType, power: Int): Int = when (task) {
        V3TaskType.Farm -> power / 3
        V3TaskType.Relief -> -18
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

    private fun relationTotal(relations: V3Relations): Int {
        return relations.yamen + relations.gentry + relations.villagers + relations.merchants + relations.garrison - relations.bandits
    }

    private fun clamp(value: Int): Int = min(100, max(-100, value))
}
