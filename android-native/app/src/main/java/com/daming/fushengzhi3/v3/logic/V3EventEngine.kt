package com.daming.fushengzhi3.v3.logic

import com.daming.fushengzhi3.v3.data.V3ActiveEvent
import com.daming.fushengzhi3.v3.data.V3BranchImpact
import com.daming.fushengzhi3.v3.data.V3EventContent
import com.daming.fushengzhi3.v3.data.V3EventChoice
import com.daming.fushengzhi3.v3.data.V3GameState
import com.daming.fushengzhi3.v3.data.V3RegionStatus
import com.daming.fushengzhi3.v3.data.V3Route
import kotlin.math.max
import kotlin.math.min

object V3EventEngine {
    fun personalizeEvent(event: V3ActiveEvent, state: V3GameState): V3ActiveEvent {
        val clanLabel = "${state.surname}氏"
        fun personalize(text: String): String =
            text
                .replace("李慎行", state.founderName)
                .replace("李氏", clanLabel)
        return event.copy(
            title = personalize(event.title),
            body = personalize(event.body),
            choices = event.choices.map { choice ->
                choice.copy(label = personalize(choice.label), desc = personalize(choice.desc))
            }
        )
    }

    fun generateEvent(state: V3GameState): V3ActiveEvent? {
        if (state.activeEvent != null) return state.activeEvent
        if (V3GameEngine.alivePeople(state).size < 2 && state.year == 1601 && state.month <= 3) return null
        val totalRisk = state.sites.sumOf { it.risk }
        historicalEvent(state)?.let { return it }
        val angryBranch = state.branches.maxByOrNull { it.grievance }
        val criticalEvent = when {
            angryBranch != null && angryBranch.grievance >= 48 -> branchDemandEvent(angryBranch.id, angryBranch.name, angryBranch.focus)
            state.cohesion < 38 -> branchDisputeEvent()
            state.silver < 40 && state.month % 2 == 0 -> countyRumorEvent()
            state.grain < 95 -> refugeesEvent()
            totalRisk > 420 -> banditShadowEvent()
            else -> null
        }
        if (criticalEvent != null) return criticalEvent
        followUpEvent(state)?.let { return it }
        if (!shouldRoutineEvent(state, totalRisk)) return null

        val staticCandidates = V3EventContent.allEvents
            .map { event -> personalizeEvent(event, state) }
            .filter { event ->
                eventMatchesState(event, state) && state.eventLog.take(10).none { it.contains(event.title) }
            }
        val dynamicCandidates = dynamicProgressEvents(state, totalRisk)
        val ranked = (staticCandidates + dynamicCandidates)
            .map { it to eventFitScore(it, state, totalRisk) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
        if (ranked.isEmpty()) return null
        val topScore = ranked.first().second
        val pool = ranked
            .takeWhile { topScore - it.second <= 12 }
            .take(12)
            .map { it.first }
        if (pool.isEmpty()) return ranked.first().first
        val index = ((state.year * 12 + state.month + totalRisk + state.routeScores.values.sum()) % pool.size).coerceAtLeast(0)
        return pool[index]
    }

    fun choose(state: V3GameState, choice: V3EventChoice): V3GameState {
        val routeScore = (state.routeScores[choice.route] ?: 0) + choice.routeDelta
        val nextRelations = state.relations.copy(
            yamen = clamp(state.relations.yamen + choice.yamenDelta),
            gentry = clamp(state.relations.gentry + choice.gentryDelta),
            villagers = clamp(state.relations.villagers + choice.villagersDelta),
            bandits = clamp(state.relations.bandits + choice.banditsDelta),
            merchants = clamp(state.relations.merchants + choice.merchantsDelta),
            garrison = clamp(state.relations.garrison + choice.garrisonDelta)
        )
        val nextBranches = applyBranchImpacts(state, choice.branchImpacts)
        val nextSites = state.sites.map { site ->
            if (choice.siteId == site.id) {
                val nextControl = (site.control + choice.siteControlDelta).coerceIn(0, 100)
                val nextRisk = (site.risk + choice.siteRiskDelta).coerceIn(0, 100)
                site.copy(control = nextControl, risk = nextRisk, status = V3GameEngine.siteStatusFor(nextControl, nextRisk))
            } else {
                site
            }
        }
        val nextPeople = state.people.map { person ->
            if (choice.personId == person.id) {
                person.copy(
                    fatigue = (person.fatigue + choice.personFatigueDelta).coerceIn(0, 100),
                    merit = (person.merit + choice.personMeritDelta).coerceIn(0, 999),
                    loyalty = (person.loyalty + choice.personLoyaltyDelta).coerceIn(0, 100)
                )
            } else {
                person
            }
        }
        val branchNotes = choice.branchImpacts.mapNotNull { it.note.takeIf { note -> note.isNotBlank() } }
        val eventTitle = state.activeEvent?.title ?: "县域抉择"
        val impactLines = eventChoiceImpactLines(state, choice)
        val report = (listOf(
            "【抉择结算】",
            "事件【$eventTitle】",
            "选择：${choice.label}",
            choice.desc
        ) + impactLines + branchNotes).joinToString("\n")
        val logLine = "事件【$eventTitle】选择：${choice.label}。${impactLines.joinToString("；")}"
        val nextArmy = if (choice.militiaDelta >= 0) {
            state.army.add(com.daming.fushengzhi3.v3.data.V3TroopType.Militia, choice.militiaDelta)
        } else {
            state.army.lose(-choice.militiaDelta)
        }
        return state.copy(
            silver = (state.silver + choice.silverDelta).coerceAtLeast(-999),
            grain = (state.grain + choice.grainDelta).coerceAtLeast(-999),
            militia = nextArmy.total(),
            army = nextArmy,
            cohesion = (state.cohesion + choice.cohesionDelta).coerceIn(0, 100),
            influence = (state.influence + choice.influenceDelta).coerceIn(0, 100),
            sites = nextSites,
            people = nextPeople,
            relations = nextRelations,
            branches = nextBranches,
            routeScores = state.routeScores + (choice.route to routeScore),
            activeEvent = null,
            pendingReports = listOf(report),
            eventLog = (listOf("${state.year}年${state.month}月 · $logLine") + state.eventLog).take(100)
        )
    }

    private fun eventChoiceImpactLines(state: V3GameState, choice: V3EventChoice): List<String> {
        val parts = mutableListOf<String>()
        fun add(label: String, value: Int) {
            if (value > 0) parts += "$label+$value"
            if (value < 0) parts += "$label$value"
        }
        add("银两", choice.silverDelta)
        add("粮食", choice.grainDelta)
        add("凝聚", choice.cohesionDelta)
        add("族望", choice.influenceDelta)
        add("乡勇", choice.militiaDelta)
        add("官府", choice.yamenDelta)
        add("士绅", choice.gentryDelta)
        add("乡民", choice.villagersDelta)
        add("流寇", choice.banditsDelta)
        add("商帮", choice.merchantsDelta)
        add("军镇", choice.garrisonDelta)
        val siteName = choice.siteId?.let { id -> state.sites.firstOrNull { it.id == id }?.name }
        if (siteName != null) {
            if (choice.siteControlDelta != 0) parts += "$siteName 控制${signed(choice.siteControlDelta)}"
            if (choice.siteRiskDelta != 0) parts += "$siteName 风险${signed(choice.siteRiskDelta)}"
        } else {
            add("地点控制", choice.siteControlDelta)
            add("地点风险", choice.siteRiskDelta)
        }
        val personName = choice.personId?.let { id -> state.people.firstOrNull { it.id == id }?.name }
        if (personName != null) {
            if (choice.personFatigueDelta != 0) parts += "$personName 疲劳${signed(choice.personFatigueDelta)}"
            if (choice.personMeritDelta != 0) parts += "$personName 功绩${signed(choice.personMeritDelta)}"
            if (choice.personLoyaltyDelta != 0) parts += "$personName 忠诚${signed(choice.personLoyaltyDelta)}"
        } else {
            add("疲劳", choice.personFatigueDelta)
            add("功绩", choice.personMeritDelta)
            add("忠诚", choice.personLoyaltyDelta)
        }
        if (choice.routeDelta != 0) parts += "路线【${choice.route.label}】${signed(choice.routeDelta)}"
        return if (parts.isEmpty()) listOf("结算：局势小幅变化，已记入近事。") else listOf("结算：${parts.joinToString("；")}")
    }

    private fun signed(value: Int): String = if (value >= 0) "+$value" else value.toString()

    private fun shouldRoutineEvent(state: V3GameState, totalRisk: Int): Boolean {
        if (state.month % 4 == 0) return true
        if (state.month % 3 == 0 && (totalRisk >= 260 || state.silver < 70 || state.grain < 120)) return true
        if (state.month == 12) return true
        return false
    }

    private fun followUpEvent(state: V3GameState): V3ActiveEvent? {
        val pendingChoice = state.eventLog.take(40).firstOrNull { log ->
            log.contains("事件【") && log.contains("选择：")
        } ?: return null
        val recent = pendingChoice
        if ((recent.contains("开仓") || recent.contains("赈") || recent.contains("义诊")) && !recent.contains("赈济余波")) {
            return V3ActiveEvent(
                "赈济余波",
                "前番赈济之后，乡民口碑传开，也有外乡饥民闻讯而来。族老提醒：善名能聚人，也会继续消耗粮仓。",
                listOf(
                    V3EventChoice("续开粥棚", "继续换民心与凝聚。", grainDelta = -55, cohesionDelta = 6, villagersDelta = 9, route = V3Route.Hermit, routeDelta = 7),
                    V3EventChoice("登记为佃户", "把善名转为劳力。", grainDelta = -25, influenceDelta = 3, villagersDelta = 5, siteId = "farmland", siteControlDelta = 6, route = V3Route.Hermit, routeDelta = 5),
                    V3EventChoice("限额施救", "稳住粮仓，但善名放缓。", grainDelta = -10, cohesionDelta = 2, villagersDelta = 2, route = V3Route.Hermit, routeDelta = 3)
                )
            )
        }
        if ((recent.contains("商") || recent.contains("牙行") || recent.contains("海货") || recent.contains("码头")) && !recent.contains("商路回响")) {
            return V3ActiveEvent(
                "商路回响",
                "前番商路动作让西河商帮重新估量李氏。有人愿意入股，也有人担心官府追查。",
                listOf(
                    V3EventChoice("开联合账房", "银两和商帮关系上升。", silverDelta = 90, merchantsDelta = 8, yamenDelta = -3, route = V3Route.Merchant, routeDelta = 8),
                    V3EventChoice("分润给官差", "降低追查风险。", silverDelta = -35, yamenDelta = 6, merchantsDelta = 3, route = V3Route.Loyalist, routeDelta = 4),
                    V3EventChoice("暗投海路", "海外路线增强。", silverDelta = -25, merchantsDelta = 6, siteId = "dock", siteControlDelta = 6, route = V3Route.Overseas, routeDelta = 8)
                )
            )
        }
        if ((recent.contains("讨伐") || recent.contains("乡勇") || recent.contains("寨堡") || recent.contains("山道")) && !recent.contains("军务余震")) {
            return V3ActiveEvent(
                "军务余震",
                "乡勇操练和山道冲突之后，县中豪族都开始打听李氏兵力。若处理不好，官府疑心和流寇仇怨都会加深。",
                listOf(
                    V3EventChoice("整编乡勇", "兵力更稳，官府略疑。", silverDelta = -28, grainDelta = -24, militiaDelta = 20, yamenDelta = -3, route = V3Route.Fortress, routeDelta = 8),
                    V3EventChoice("递名册报备", "官府军镇关系回升。", silverDelta = -18, yamenDelta = 7, garrisonDelta = 5, route = V3Route.Loyalist, routeDelta = 5),
                    V3EventChoice("暗招山民", "割据路线加深。", silverDelta = -35, militiaDelta = 18, banditsDelta = 4, route = V3Route.Warlord, routeDelta = 9)
                )
            )
        }
        if ((recent.contains("书院") || recent.contains("科举") || recent.contains("讲会") || recent.contains("士绅")) && !recent.contains("士林回函")) {
            return V3ActiveEvent(
                "士林回函",
                "书院讲会与科举消息传开，邻县士子送来名帖。接还是不接，都会决定李氏是否真正走进士林。",
                listOf(
                    V3EventChoice("设席延请", "士绅和耕读路线提升。", silverDelta = -40, gentryDelta = 9, influenceDelta = 5, route = V3Route.Scholar, routeDelta = 9),
                    V3EventChoice("只收名帖", "低成本维持名声。", influenceDelta = 2, gentryDelta = 3, route = V3Route.Scholar, routeDelta = 4),
                    V3EventChoice("避开党争", "凝聚上升，士林热度放缓。", cohesionDelta = 4, gentryDelta = -2, route = V3Route.Hermit, routeDelta = 4)
                )
            )
        }
        return null
    }

    private fun eventFitScore(event: V3ActiveEvent, state: V3GameState, totalRisk: Int): Int {
        if (!eventMatchesState(event, state)) return 0
        var score = 8
        val title = event.title
        val body = event.body
        val bestRoute = state.routeScores.maxByOrNull { it.value }?.key
        val peopleCount = V3GameEngine.alivePeople(state).size
        val builtSites = V3GameEngine.builtSiteCount(state)
        val estateLevel = V3GameEngine.estateLevelTotal(state)
        val controlledRegions = V3GameEngine.controlledRegionCount(state)
        val highRiskSites = state.sites.filter { it.risk >= 45 }.map { it.id }.toSet()
        val lowControlSites = state.sites.filter { it.control < 35 }.map { it.id }.toSet()

        event.choices.forEach { choice ->
            if (choice.route == bestRoute) score += 11
            if ((state.routeScores[choice.route] ?: 0) >= 35) score += 6
            if (choice.siteId != null && choice.siteId in highRiskSites) score += 14
            if (choice.siteId != null && choice.siteId in lowControlSites) score += 8
            if (choice.siteId != null && state.sites.firstOrNull { it.id == choice.siteId }?.level == 0) score += 4
            if (choice.branchImpacts.isNotEmpty() && state.branches.any { it.grievance >= 30 }) score += 8
            if (choice.personId != null && peopleCount >= 3) score += 7
        }

        if (state.crisis == "官府催税" && anyText(title, body, "县", "衙", "税", "饷", "徭")) score += 18
        if (state.crisis == "流寇逼近" && anyText(title, body, "山", "寇", "寨", "勇", "兵")) score += 18
        if (state.crisis == "饥荒将至" && anyText(title, body, "粮", "田", "民", "仓", "荒")) score += 18
        if (state.crisis == "族产争端" && anyText(title, body, "房", "祠", "账", "谱", "族")) score += 18
        if (state.crisis == "商路断绝" && anyText(title, body, "商", "市", "码头", "船", "货")) score += 18
        if (state.crisis == "瘟疫初起" && anyText(title, body, "医", "药", "疫", "病")) score += 18

        if (state.year >= 1619 && anyText(title, body, "辽", "勤王", "军", "饷")) score += 12
        if (state.year >= 1628 && anyText(title, body, "崇祯", "流民", "饥", "税", "灾")) score += 12
        if (state.year >= 1636 && anyText(title, body, "关外", "清军", "寨", "海", "割据")) score += 12
        if (state.year >= 1642 && anyText(title, body, "甲申", "天下", "城", "南迁", "勤王")) score += 14

        if (peopleCount <= 2 && anyText(title, body, "婚", "添丁", "子", "嗣", "族谱")) score += 16
        if (peopleCount >= 6 && anyText(title, body, "房", "分粮", "私塾", "席位", "族规")) score += 12
        if (builtSites <= 1 && anyText(title, body, "田", "铺", "仓", "田契", "集市")) score += 12
        if (builtSites >= 4 && anyText(title, body, "总号", "审计", "扩铺", "合股", "商号")) score += 12
        if (estateLevel >= 5 && anyText(title, body, "家产", "账", "分润", "祠产", "铺")) score += 11
        if (state.militia >= 80 && anyText(title, body, "乡勇", "寨", "兵", "军械", "点验")) score += 12
        if (state.silver < 80 && anyText(title, body, "借", "账", "税", "商", "银")) score += 12
        if (state.grain < 140 && anyText(title, body, "粮", "仓", "荒", "民", "赈")) score += 12
        if (totalRisk >= 280 && anyText(title, body, "风险", "盗", "寇", "疫", "戒严", "塌方")) score += 10
        if (controlledRegions >= 2 && anyText(title, body, "府", "省", "会盟", "天下", "军议")) score += 14
        if (state.unificationProgress >= 45 && anyText(title, body, "统一", "京畿", "城门", "盟主", "勤王")) score += 16
        return score
    }

    private fun anyText(title: String, body: String, vararg words: String): Boolean = words.any { title.contains(it) || body.contains(it) }

    private fun dynamicProgressEvents(state: V3GameState, totalRisk: Int): List<V3ActiveEvent> {
        val events = mutableListOf<V3ActiveEvent>()
        events += dynamicResourceEvents(state)
        events += dynamicSiteEvents(state)
        events += dynamicClanEvents(state)
        events += dynamicPersonEvents(state)
        events += dynamicRouteEvents(state)
        events += dynamicWorldEvents(state)
        events += dynamicEraEvents(state, totalRisk)
        return events.filter { state.eventLog.take(10).none { log -> log.contains(it.title) } }
    }

    private fun dynamicResourceEvents(state: V3GameState): List<V3ActiveEvent> {
        val events = mutableListOf<V3ActiveEvent>()
        if (state.silver < 80) events += V3ActiveEvent("银库见底", "账房报称银库将空，婚配、修产、打点县衙都可能停滞。商支建议借贷，族老主张缩支。", listOf(
            V3EventChoice("向商帮借银", "立刻补银，但商帮影响扩大。", silverDelta = 120, merchantsDelta = 8, cohesionDelta = -2, route = V3Route.Merchant, routeDelta = 7),
            V3EventChoice("削减用度", "凝聚略伤，保住自主。", silverDelta = 45, cohesionDelta = -3, route = V3Route.Hermit, routeDelta = 5)
        ))
        if (state.grain < 140) events += V3ActiveEvent("粮仓告急", "粮仓余粮不足，佃户与族人都在看宗祠如何分配口粮。", listOf(
            V3EventChoice("开仓均粜", "粮更少，但民心与凝聚上升。", grainDelta = -35, villagersDelta = 10, cohesionDelta = 5, route = V3Route.Hermit, routeDelta = 6),
            V3EventChoice("高价购粮", "用银换粮。", silverDelta = -70, grainDelta = 120, merchantsDelta = 4, route = V3Route.Merchant, routeDelta = 5)
        ))
        if (state.silver > 700) events += V3ActiveEvent("巨银入库", "族中银两充盈，诸房都盯着这笔钱：修祠、买田、扩铺、练勇，各有主张。", listOf(
            V3EventChoice("扩买族田", "粮食根基增强。", silverDelta = -180, grainDelta = 130, cohesionDelta = 4, route = V3Route.Hermit, routeDelta = 6),
            V3EventChoice("投向商号", "商业路线推进。", silverDelta = -140, merchantsDelta = 10, influenceDelta = 5, route = V3Route.Merchant, routeDelta = 8)
        ))
        if (state.grain > 900) events += V3ActiveEvent("积谷成仓", "粮仓充盈，邻村与县衙都想借李氏粮力。", listOf(
            V3EventChoice("赈济邻里", "民心与声望提升。", grainDelta = -160, villagersDelta = 14, influenceDelta = 7, route = V3Route.Hermit, routeDelta = 6),
            V3EventChoice("换取兵械", "以粮换军备。", grainDelta = -150, militiaDelta = 25, garrisonDelta = 5, route = V3Route.Fortress, routeDelta = 8)
        ))
        return events
    }

    private fun dynamicSiteEvents(state: V3GameState): List<V3ActiveEvent> {
        val events = mutableListOf<V3ActiveEvent>()
        state.sites.filter { it.risk >= 55 }.forEach { site ->
            events += V3ActiveEvent("${site.name}危局", "${site.name}风险已高，地方传言、差役盘剥、流寇窥伺都可能由此爆发。", listOf(
                V3EventChoice("派人整顿", "压低风险，花费银粮。", silverDelta = -28, grainDelta = -12, siteId = site.id, siteControlDelta = 7, siteRiskDelta = -18, route = routeForSite(site.id), routeDelta = 7),
                V3EventChoice("借势立威", "控制上升，但关系更紧。", influenceDelta = 4, siteId = site.id, siteControlDelta = 12, siteRiskDelta = 4, route = V3Route.Warlord, routeDelta = 7)
            ))
        }
        state.sites.filter { it.control < 28 }.forEach { site ->
            events += V3ActiveEvent("${site.name}旁落", "${site.name}仍不在李氏掌握中，地方小吏、商帮或外族正填补空位。", listOf(
                V3EventChoice("出资经营", "提高控制。", silverDelta = -42, siteId = site.id, siteControlDelta = 15, siteRiskDelta = -4, route = routeForSite(site.id), routeDelta = 6),
                V3EventChoice("扶植本地人", "花费较少，推进较慢。", silverDelta = -18, villagersDelta = 4, siteId = site.id, siteControlDelta = 8, route = V3Route.Hermit, routeDelta = 4)
            ))
        }
        state.sites.filter { it.level >= 2 && it.control >= 65 }.forEach { site ->
            events += V3ActiveEvent("${site.name}成势", "${site.name}已成李氏根基，族人提议借此向外扩张影响。", listOf(
                V3EventChoice("借势扩名", "声望上升。", silverDelta = -25, influenceDelta = 8, siteId = site.id, siteControlDelta = 4, route = routeForSite(site.id), routeDelta = 7),
                V3EventChoice("厚积不张", "凝聚稳定，风险下降。", cohesionDelta = 4, siteId = site.id, siteRiskDelta = -8, route = V3Route.Hermit, routeDelta = 5)
            ))
        }
        return events
    }

    private fun dynamicClanEvents(state: V3GameState): List<V3ActiveEvent> {
        val events = mutableListOf<V3ActiveEvent>()
        val people = V3GameEngine.alivePeople(state)
        if (people.size <= 2) events += V3ActiveEvent("香火单薄", "族谱上人丁仍少，族老催促早定婚配与子嗣，否则家业虽起也无人承继。", listOf(
            V3EventChoice("重礼求亲", "花费银粮，提升声望。", silverDelta = -55, grainDelta = -25, influenceDelta = 5, cohesionDelta = 3, route = V3Route.Hermit, routeDelta = 6),
            V3EventChoice("先稳家产", "暂缓婚育，家业优先。", silverDelta = 25, cohesionDelta = -2, route = V3Route.Merchant, routeDelta = 4)
        ))
        if (people.size >= 6) events += V3ActiveEvent("人丁渐盛", "族人增多后，学业、婚配、分房、职司都要重新安排。", listOf(
            V3EventChoice("分设房支职司", "凝聚和治理上升。", silverDelta = -25, cohesionDelta = 7, influenceDelta = 4, route = V3Route.Hermit, routeDelta = 7),
            V3EventChoice("择优重点培养", "路线推进，但部分族人不满。", silverDelta = -35, cohesionDelta = -2, influenceDelta = 8, route = bestRoute(state), routeDelta = 8)
        ))
        state.branches.filter { it.grievance >= 30 }.forEach { branch ->
            events += V3ActiveEvent("${branch.name}怨言", "${branch.name}近来怨气渐重，认为宗祠分配不公。若不处理，日后会牵动族产争端。", listOf(
                V3EventChoice("开祠安抚", "消怨但耗资源。", silverDelta = -30, grainDelta = -20, cohesionDelta = 5, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact(branch.id, loyaltyDelta = 4, grievanceDelta = -10))),
                V3EventChoice("以规压下", "主房权威上升，怨气未消。", influenceDelta = 4, cohesionDelta = -3, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact(branch.id, grievanceDelta = 5), V3BranchImpact("main", influenceDelta = 3)))
            ))
        }
        people.filter { it.fatigue >= 60 }.forEach { person ->
            events += V3ActiveEvent("${person.name}积劳", "${person.name}连月奔走，疲惫已深。若继续压担，或伤身体与忠心。", listOf(
                V3EventChoice("令其休养", "疲劳下降，进度放慢。", cohesionDelta = 2, personId = person.id, personFatigueDelta = -24, personLoyaltyDelta = 3, route = V3Route.Hermit, routeDelta = 4),
                V3EventChoice("加派帮手", "花费银两保住进度。", silverDelta = -25, personId = person.id, personFatigueDelta = -10, personMeritDelta = 2, route = bestRoute(state), routeDelta = 5)
            ))
        }
        return events
    }

    private fun dynamicPersonEvents(state: V3GameState): List<V3ActiveEvent> {
        val founder = state.people.firstOrNull { it.id == 1 && it.alive }
        val mostTired = V3GameEngine.alivePeople(state).filter { it.fatigue >= 55 }.maxByOrNull { it.fatigue }
        val mostMeritorious = V3GameEngine.alivePeople(state).filter { it.merit >= 10 }.maxByOrNull { it.merit }
        return buildList {
            if (founder != null) {
                add(
                    V3ActiveEvent(
                        "${founder.name}夜定家法",
                        "${founder.name}夜召族老，眼下各项产业渐开，若仍各行其是，日后房支必争。",
                        listOf(
                            V3EventChoice("定下家法", "主房威望上升，凝聚稳定。", cohesionDelta = 5, influenceDelta = 3, personId = founder.id, personFatigueDelta = 5, personMeritDelta = 2, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 3))),
                            V3EventChoice("广听族议", "凝聚提升，路线更偏耕读。", cohesionDelta = 4, personId = founder.id, personFatigueDelta = 3, route = V3Route.Scholar)
                        )
                    )
                )
            }
            if (mostTired != null) {
                add(
                    V3ActiveEvent(
                        "${mostTired.name}积劳成忧",
                        "${mostTired.name}连月奔走，脸色已差。若继续压担，可能染病；若让其休养，本月事务会放慢。",
                        listOf(
                            V3EventChoice("令其休养", "疲劳下降，忠诚回升。", cohesionDelta = 2, personId = mostTired.id, personFatigueDelta = -25, personLoyaltyDelta = 3, route = V3Route.Hermit, routeDelta = 4),
                            V3EventChoice("请医馆调理", "花银换健康与功绩。", silverDelta = -20, personId = mostTired.id, personFatigueDelta = -15, personMeritDelta = 2, route = V3Route.Hermit, routeDelta = 3)
                        )
                    )
                )
            }
            if (mostMeritorious != null && mostMeritorious.id != founder?.id) {
                add(
                    V3ActiveEvent(
                        "${mostMeritorious.name}功高请命",
                        "${mostMeritorious.name}已在族中积下${mostMeritorious.merit}点功绩，请求独当一面。答应会壮大其房支，不答应则可能伤忠心。",
                        listOf(
                            V3EventChoice("授予职司", "功绩与忠诚提高，家族路线随其所长推进。", influenceDelta = 3, personId = mostMeritorious.id, personMeritDelta = 4, personLoyaltyDelta = 3, route = bestRoute(state), routeDelta = 5),
                            V3EventChoice("暂缓任命", "主房权威稳定，但本人略有失望。", cohesionDelta = 2, personId = mostMeritorious.id, personLoyaltyDelta = -2, route = V3Route.Hermit, routeDelta = 3)
                        )
                    )
                )
            }
        }
    }

    private fun dynamicRouteEvents(state: V3GameState): List<V3ActiveEvent> {
        val route = bestRoute(state)
        return when (route) {
            V3Route.Scholar -> listOf(V3ActiveEvent("士林邀约", "李氏耕读名声渐起，邻县士林邀族人讲学。", listOf(
                V3EventChoice("赴会讲学", "士绅与声望上升。", silverDelta = -35, gentryDelta = 10, influenceDelta = 7, route = V3Route.Scholar, routeDelta = 9),
                V3EventChoice("闭门读书", "避开党争。", cohesionDelta = 4, yamenDelta = 2, route = V3Route.Hermit, routeDelta = 5)
            )))
            V3Route.Merchant -> listOf(V3ActiveEvent("商路分润", "商支声势日盛，商帮要求重新议定集市和码头分润。", listOf(
                V3EventChoice("扩大合股", "银两与商帮关系提升。", silverDelta = 90, merchantsDelta = 9, cohesionDelta = -2, route = V3Route.Merchant, routeDelta = 9),
                V3EventChoice("收回宗祠账权", "凝聚上升，商支不满。", influenceDelta = 5, merchantsDelta = -5, route = V3Route.Hermit, routeDelta = 5)
            )))
            V3Route.Fortress -> listOf(V3ActiveEvent("堡寨军议", "北山寨堡已成自保根基，邻村请求共立守望盟。", listOf(
                V3EventChoice("共立守望", "乡勇与民心上升。", silverDelta = -45, grainDelta = -30, militiaDelta = 22, villagersDelta = 8, route = V3Route.Fortress, routeDelta = 10),
                V3EventChoice("只守本族", "凝聚稳定，地方声望有限。", cohesionDelta = 5, villagersDelta = -2, route = V3Route.Hermit, routeDelta = 5)
            )))
            V3Route.Loyalist -> listOf(V3ActiveEvent("勤王名帖", "军镇与县衙联名送帖，希望李氏输粮募勇入册。", listOf(
                V3EventChoice("入册勤王", "勤王路线推进。", grainDelta = -80, militiaDelta = 15, yamenDelta = 10, garrisonDelta = 10, route = V3Route.Loyalist, routeDelta = 10),
                V3EventChoice("只献粮草", "少涉兵事。", grainDelta = -45, yamenDelta = 5, route = V3Route.Hermit, routeDelta = 5)
            )))
            V3Route.Warlord -> listOf(V3ActiveEvent("地方推戴", "县中小族见官府式微，私下请李氏主持城防与粮价。", listOf(
                V3EventChoice("接掌城防", "割据路线推进，官府关系下降。", silverDelta = -55, militiaDelta = 28, yamenDelta = -12, influenceDelta = 10, route = V3Route.Warlord, routeDelta = 12),
                V3EventChoice("仍尊县印", "保留名义秩序。", yamenDelta = 8, cohesionDelta = 4, route = V3Route.Loyalist, routeDelta = 6)
            )))
            V3Route.Overseas -> listOf(V3ActiveEvent("海路暗约", "码头商人愿替李氏安排南洋船位，但索价极高。", listOf(
                V3EventChoice("预订船位", "海外路线推进。", silverDelta = -120, merchantsDelta = 10, route = V3Route.Overseas, routeDelta = 12),
                V3EventChoice("只走货不迁人", "商利提升，风险较低。", silverDelta = 55, merchantsDelta = 5, route = V3Route.Merchant, routeDelta = 5)
            )))
            V3Route.Hermit -> listOf(V3ActiveEvent("闭乡族约", "族老提议重申闭乡、屯粮、禁奢、互保四约。", listOf(
                V3EventChoice("颁行族约", "凝聚与粮食安全提升。", grainDelta = 35, cohesionDelta = 9, villagersDelta = 5, route = V3Route.Hermit, routeDelta = 9),
                V3EventChoice("从宽执行", "各房自在，凝聚提升较少。", cohesionDelta = 3, route = V3Route.Hermit, routeDelta = 4)
            )))
        }
    }

    private fun dynamicWorldEvents(state: V3GameState): List<V3ActiveEvent> {
        val events = mutableListOf<V3ActiveEvent>()
        fun regionVisible(id: String): Boolean = state.worldRegions.firstOrNull { it.id == id }?.status?.let { it != V3RegionStatus.Unknown } == true
        if (regionVisible("lake_province")) events += V3ActiveEvent("湖广粮约", "湖广粮商愿按年供米，但要求李氏替他们护住运船与仓栈。", listOf(
            V3EventChoice("签下粮约", "获得大批粮食，承担护运成本。", silverDelta = -65, grainDelta = 150, merchantsDelta = 6, garrisonDelta = 2, route = V3Route.Merchant, routeDelta = 8),
            V3EventChoice("设义仓分粮", "以粮区声望换民心。", grainDelta = -45, villagersDelta = 12, influenceDelta = 6, route = V3Route.Hermit, routeDelta = 7)
        ))
        if (regionVisible("coast_province")) events += V3ActiveEvent("闽粤船契", "闽粤船主送来船契，可入股远海，也可替官府缉查走私。", listOf(
            V3EventChoice("入股远海", "海外路线和商利提升。", silverDelta = -90, merchantsDelta = 10, route = V3Route.Overseas, routeDelta = 12),
            V3EventChoice("协助巡海", "官府军镇关系提升。", silverDelta = -35, yamenDelta = 8, garrisonDelta = 6, route = V3Route.Loyalist, routeDelta = 7)
        ))
        if (regionVisible("shandong_corridor")) events += V3ActiveEvent("运河漕争", "山东运河数家豪强争夺漕船泊位，李氏必须决定以商契、官帖还是兵威入局。", listOf(
            V3EventChoice("合股漕运", "银两和商帮关系提升。", silverDelta = 85, merchantsDelta = 8, route = V3Route.Merchant, routeDelta = 9),
            V3EventChoice("持官帖调停", "官府与士绅关系提升。", silverDelta = -30, yamenDelta = 7, gentryDelta = 6, route = V3Route.Loyalist, routeDelta = 8),
            V3EventChoice("派兵占泊", "乡勇与割据路线增强。", grainDelta = -35, militiaDelta = 18, yamenDelta = -6, route = V3Route.Warlord, routeDelta = 10)
        ))
        if (regionVisible("liaodong_front")) events += V3ActiveEvent("辽东残军", "一队辽东残军求粮求械，愿受李氏节制，也可能把边祸带回族中。", listOf(
            V3EventChoice("收编残军", "兵力和军镇关系大增。", silverDelta = -55, grainDelta = -65, militiaDelta = 28, garrisonDelta = 10, route = V3Route.Loyalist, routeDelta = 11),
            V3EventChoice("资粮遣返", "勤王声名提升但不扩军。", grainDelta = -45, influenceDelta = 7, yamenDelta = 5, route = V3Route.Loyalist, routeDelta = 8)
        ))
        if (regionVisible("jiangsea_gate")) events += V3ActiveEvent("江海迁族", "江海门户聚集许多南迁宗族，有人求依附李氏，有人只想搭船远走。", listOf(
            V3EventChoice("接纳入族", "凝聚、声望和商帮关系提升。", silverDelta = -45, grainDelta = -55, cohesionDelta = 6, influenceDelta = 6, merchantsDelta = 5, route = V3Route.Hermit, routeDelta = 8),
            V3EventChoice("资助出海", "海外路线大幅推进。", silverDelta = -100, merchantsDelta = 8, route = V3Route.Overseas, routeDelta = 12)
        ))
        state.worldRegions.filter { it.status == V3RegionStatus.Contacted || it.status == V3RegionStatus.Influenced }.forEach { region ->
            events += V3ActiveEvent("${region.name}来使", "${region.name}已有李氏声望，对方遣人来谈粮、兵、商路与归附条件。", listOf(
                V3EventChoice("厚礼结交", "提升天下路线声望。", silverDelta = -80, influenceDelta = 8, merchantsDelta = 4, route = V3Route.Merchant, routeDelta = 7),
                V3EventChoice("示以兵威", "割据和统一路线推进。", grainDelta = -40, militiaDelta = 18, influenceDelta = 6, route = V3Route.Warlord, routeDelta = 9)
            ))
        }
        if (V3GameEngine.controlledRegionCount(state) >= 2) events += V3ActiveEvent("跨府声势", "李氏已不止一县之族，府县士绅开始权衡是否依附。", listOf(
            V3EventChoice("设跨府公议", "声望与士绅关系上升。", silverDelta = -90, gentryDelta = 12, influenceDelta = 12, route = V3Route.Scholar, routeDelta = 10),
            V3EventChoice("立盟主旗号", "统一路线推进。", silverDelta = -70, grainDelta = -50, militiaDelta = 30, influenceDelta = 10, route = V3Route.Warlord, routeDelta = 12)
        ))
        return events
    }

    private fun dynamicEraEvents(state: V3GameState, totalRisk: Int): List<V3ActiveEvent> {
        val events = mutableListOf<V3ActiveEvent>()
        if (state.year >= 1619) events += V3ActiveEvent("辽饷阴影", "辽东战事之后，县中催饷越来越急。李氏的粮银账本难再只为族内服务。", listOf(
            V3EventChoice("先输小饷", "官府关系提升。", silverDelta = -45, grainDelta = -25, yamenDelta = 8, route = V3Route.Loyalist, routeDelta = 7),
            V3EventChoice("称灾缓缴", "保住资源，官府不满。", grainDelta = -10, yamenDelta = -8, villagersDelta = 5, route = V3Route.Hermit, routeDelta = 5)
        ))
        if (state.year >= 1628) events += V3ActiveEvent("崇祯催科", "新政要清积弊，地方催科反而更急。差役到村，乡民怨声载道。", listOf(
            V3EventChoice("代民出银", "民心上升，银两下降。", silverDelta = -75, villagersDelta = 12, yamenDelta = 3, route = V3Route.Hermit, routeDelta = 7),
            V3EventChoice("严按户籍", "官府满意，民心下降。", yamenDelta = 10, villagersDelta = -8, influenceDelta = 3, route = V3Route.Loyalist, routeDelta = 7)
        ))
        if (state.year >= 1636 || totalRisk >= 360) events += V3ActiveEvent("乱世逼近", "关外、流寇、饥荒与苛派一齐压来，县中人人都在问李氏到底站哪边。", listOf(
            V3EventChoice("扩寨练勇", "武备和割据路线推进。", silverDelta = -60, grainDelta = -45, militiaDelta = 30, route = V3Route.Warlord, routeDelta = 12),
            V3EventChoice("修谱保族", "保族路线推进。", grainDelta = -25, cohesionDelta = 10, route = V3Route.Hermit, routeDelta = 9)
        ))
        return events
    }

    private fun routeForSite(siteId: String): V3Route = when (siteId) {
        "academy" -> V3Route.Scholar
        "market" -> V3Route.Merchant
        "dock" -> V3Route.Overseas
        "fort", "mountain_pass" -> V3Route.Fortress
        "yamen" -> V3Route.Loyalist
        else -> V3Route.Hermit
    }

    private fun bestRoute(state: V3GameState): V3Route = state.routeScores.maxByOrNull { it.value }?.key ?: V3Route.Hermit

    private fun historicalEvent(state: V3GameState): V3ActiveEvent? {
        val key = state.year to state.month
        if (state.eventLog.take(30).any { it.contains("史事") && it.contains(state.year.toString()) }) return null
        fun personalize(text: String): String = text.replace("李慎行", state.founderName).replace("李氏", "${state.surname}氏")
        fun event(title: String, body: String, choices: List<V3EventChoice>) = V3ActiveEvent(personalize(title), personalize(body), choices.map { choice -> choice.copy(label = personalize(choice.label), desc = personalize(choice.desc)) })
        return when (key) {
            1601 to 9 -> event(
                "史事：矿税余波",
                "万历末年，矿监税使之弊虽稍退，地方仍以旧账追索。清河县衙重翻商税与田税册，李氏若不表态，集市、田庄都会被牵动。",
                listOf(
                    V3EventChoice("清账输税", "以银换安，官府关系回暖。", silverDelta = -45, yamenDelta = 10, merchantsDelta = -2, influenceDelta = 2, siteId = "yamen", siteRiskDelta = -8, route = V3Route.Loyalist),
                    V3EventChoice("联络士绅缓征", "借士绅之力抗税，声名上升但县衙不悦。", silverDelta = -20, gentryDelta = 9, yamenDelta = -6, influenceDelta = 5, route = V3Route.Scholar),
                    V3EventChoice("暗改账册", "短期保财，但商路和官府风险变高。", silverDelta = 35, yamenDelta = -8, merchantsDelta = 5, siteId = "market", siteRiskDelta = 10, route = V3Route.Merchant)
                )
            )
            1619 to 4 -> event(
                "史事：萨尔浒败闻",
                "辽东大败的消息顺江而下，军需、辽饷、募兵风声一齐压到县中。宗祠议事不再只是家产，已开始牵连边事。",
                listOf(
                    V3EventChoice("输粮勤王", "勤王名声上升，粮仓承压。", grainDelta = -70, yamenDelta = 8, garrisonDelta = 9, influenceDelta = 4, route = V3Route.Loyalist, routeDelta = 9),
                    V3EventChoice("修寨屯粮", "转向乱世自保。", silverDelta = -35, grainDelta = -25, militiaDelta = 12, siteId = "fort", siteControlDelta = 8, siteRiskDelta = -10, route = V3Route.Fortress, routeDelta = 9),
                    V3EventChoice("扩大商路备银", "以财应变，商路加强。", silverDelta = 60, merchantsDelta = 7, yamenDelta = -3, route = V3Route.Merchant, routeDelta = 8)
                )
            )
            1621 to 7 -> event(
                "史事：辽事再急",
                "天启初年，辽东战报频仍，县中军户与商帮都在囤粮避祸。李氏的选择会让家族路线更偏向勤王、自保或逐利。",
                listOf(
                    V3EventChoice("募勇入册", "乡勇增加，军镇关系改善。", silverDelta = -40, grainDelta = -25, militiaDelta = 18, garrisonDelta = 10, route = V3Route.Loyalist, routeDelta = 8),
                    V3EventChoice("堡寨盟约", "自保路线增强，乡民更安。", grainDelta = -35, militiaDelta = 10, villagersDelta = 7, siteId = "fort", siteControlDelta = 10, route = V3Route.Fortress, routeDelta = 9),
                    V3EventChoice("囤货等价", "银两上涨但民心下降。", silverDelta = 90, villagersDelta = -8, merchantsDelta = 8, route = V3Route.Merchant, routeDelta = 8)
                )
            )
            1627 to 10 -> event(
                "史事：天启崩，崇祯立",
                "京中传来大行皇帝崩逝、新君即位。清流称新政可期，县衙却催各族重新表忠。李氏该押注朝局，还是守住家业？",
                listOf(
                    V3EventChoice("递表称贺", "官府关系提升，花费银两。", silverDelta = -35, yamenDelta = 10, influenceDelta = 3, route = V3Route.Loyalist, routeDelta = 8),
                    V3EventChoice("资助书院清议", "士林声望上升，党争风险加深。", silverDelta = -45, gentryDelta = 10, yamenDelta = -5, influenceDelta = 6, siteId = "academy", siteRiskDelta = 6, route = V3Route.Scholar, routeDelta = 9),
                    V3EventChoice("闭祠修谱", "避开朝局，凝聚上升。", grainDelta = -20, cohesionDelta = 7, route = V3Route.Hermit, routeDelta = 7)
                )
            )
            1628 to 6 -> event(
                "史事：崇祯清饷",
                "新政要清理积弊，却也让地方催饷更急。差役登门，士绅观望，商帮怕税，乡民怕役。",
                listOf(
                    V3EventChoice("替民缓饷", "民心大升，县衙不悦。", silverDelta = -30, villagersDelta = 12, yamenDelta = -6, influenceDelta = 5, route = V3Route.Scholar),
                    V3EventChoice("足额输饷", "官府满意，资源受损。", silverDelta = -75, grainDelta = -35, yamenDelta = 12, garrisonDelta = 4, route = V3Route.Loyalist),
                    V3EventChoice("以商税抵饷", "商路承压但家业保住。", silverDelta = -35, merchantsDelta = -4, yamenDelta = 5, siteId = "market", siteRiskDelta = 6, route = V3Route.Merchant)
                )
            )
            1630 to 3 -> event(
                "史事：陕北流寇",
                "西北饥荒与流寇的传闻传入江南，逃户、募兵、粮价一起动荡。李氏需要决定是赈济、练兵，还是趁乱扩财。",
                listOf(
                    V3EventChoice("开粥棚收流民", "人口与民心机会增加，粮食下降。", grainDelta = -85, villagersDelta = 14, cohesionDelta = 5, route = V3Route.Hermit, routeDelta = 7),
                    V3EventChoice("募勇守庄", "乡勇上升，割据伏笔加深。", silverDelta = -45, grainDelta = -25, militiaDelta = 22, siteId = "fort", siteControlDelta = 8, route = V3Route.Fortress, routeDelta = 10),
                    V3EventChoice("囤粮抬价", "银两大增，民心大跌。", silverDelta = 120, grainDelta = -25, villagersDelta = -14, merchantsDelta = 8, route = V3Route.Merchant, routeDelta = 9)
                )
            )
            1636 to 5 -> event(
                "史事：关外称帝",
                "关外改号称帝的消息震动南北。朝廷催兵催饷，地方豪族开始各谋退路。李氏已不能只做县中小族。",
                listOf(
                    V3EventChoice("响应勤王", "勤王路线大进，军镇关系提升。", silverDelta = -70, grainDelta = -50, militiaDelta = 15, garrisonDelta = 14, influenceDelta = 6, route = V3Route.Loyalist, routeDelta = 12),
                    V3EventChoice("扩寨藏兵", "割据和自保路线大进，官府猜忌。", silverDelta = -55, grainDelta = -35, militiaDelta = 26, yamenDelta = -8, siteId = "fort", siteControlDelta = 10, route = V3Route.Warlord, routeDelta = 12),
                    V3EventChoice("筹船留后路", "海外路线增强。", silverDelta = -80, merchantsDelta = 9, siteId = "dock", siteControlDelta = 9, route = V3Route.Overseas, routeDelta = 12)
                )
            )
            1642 to 8 -> event(
                "史事：天下土崩",
                "北方城池屡陷，逃官、饥民、败兵接连入境。若李氏已有兵粮，此时可争一方；若根基不足，只能求保香火。",
                listOf(
                    V3EventChoice("接管县防", "举旗割据的前奏，风险与声望同升。", silverDelta = -60, grainDelta = -60, militiaDelta = 35, yamenDelta = -15, influenceDelta = 12, siteId = "yamen", siteControlDelta = 18, route = V3Route.Warlord, routeDelta = 16),
                    V3EventChoice("闭境保族", "隐忍自保，凝聚上升。", grainDelta = -45, cohesionDelta = 10, villagersDelta = 6, siteId = "fort", siteRiskDelta = -15, route = V3Route.Hermit, routeDelta = 10),
                    V3EventChoice("南迁海路", "海外路线强推，家族撕裂。", silverDelta = -120, cohesionDelta = -5, merchantsDelta = 12, route = V3Route.Overseas, routeDelta = 16)
                )
            )
            1644 to 3 -> event(
                "史事：甲申国变",
                "京师陷落的消息尚未传实，县中已人心惶惶。大明气数将尽，李氏必须选择最后路线：勤王、割据、保族，或远走。",
                listOf(
                    V3EventChoice("举族勤王", "忠烈路线终极选择。", silverDelta = -90, grainDelta = -90, militiaDelta = 30, garrisonDelta = 16, influenceDelta = 14, route = V3Route.Loyalist, routeDelta = 20),
                    V3EventChoice("据县自立", "割据路线终极选择。", silverDelta = -70, grainDelta = -70, militiaDelta = 45, yamenDelta = -20, influenceDelta = 12, route = V3Route.Warlord, routeDelta = 22),
                    V3EventChoice("保谱南迁", "保住香火，另寻出路。", silverDelta = -120, grainDelta = -60, cohesionDelta = 8, merchantsDelta = 8, route = V3Route.Overseas, routeDelta = 18)
                )
            )
            else -> null
        }
    }

    private fun taxDemandEvent() = V3ActiveEvent(
        title = "县令催粮",
        body = "县衙急札送至宗祠，称军需紧急，命各族三日内出粮。族老忧粮仓不足，商支主张花钱打点，书香支则劝与士绅共议。",
        choices = listOf(
            V3EventChoice("出粮助官", "官府暂安，粮仓见底。", grainDelta = -60, yamenDelta = 10, influenceDelta = 3, route = V3Route.Loyalist, branchImpacts = listOf(V3BranchImpact("main", loyaltyDelta = 2, influenceDelta = 2, note = "主房因奉公出粮而威望上升。"), V3BranchImpact("second", grievanceDelta = 2, note = "二房忧心乡民粮荒，怨气略升。"))),
            V3EventChoice("买通书吏", "花银消灾，但商支更有话语权。", silverDelta = -45, yamenDelta = 4, merchantsDelta = 3, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", influenceDelta = 3, grievanceDelta = -2, note = "商支因出面打点而话语权提高。"))),
            V3EventChoice("联合士绅议价", "士绅关系改善，官府不满。", gentryDelta = 8, yamenDelta = -3, influenceDelta = 2, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("scholar", influenceDelta = 3, loyaltyDelta = 1, note = "书香支因联络士绅而声势增长。"))),
            V3EventChoice("拖延不办", "保住资源，但官府记恨。", yamenDelta = -8, cohesionDelta = -2, route = V3Route.Hermit)
        )
    )

    private fun banditShadowEvent() = V3ActiveEvent(
        title = "山道匪影",
        body = "北山脚夫传言，有流寇斥候在山道出没。武支请命修寨募勇，族长若处置失当，商路和田庄都会受扰。",
        choices = listOf(
            V3EventChoice("修寨防守", "消耗银粮，强化自保路线。", silverDelta = -30, grainDelta = -20, garrisonDelta = 3, banditsDelta = -5, route = V3Route.Fortress, branchImpacts = listOf(V3BranchImpact("martial", influenceDelta = 4, loyaltyDelta = 2, grievanceDelta = -3, note = "武支得以主事修寨，怨气下降。"), V3BranchImpact("merchant", grievanceDelta = 1, note = "商支担心修寨耗银，略有不满。"))),
            V3EventChoice("派人刺探", "掌握匪情，风险暂缓。", silverDelta = -10, banditsDelta = -8, route = V3Route.Warlord),
            V3EventChoice("上报官府", "官府出面，但日后会索取更多供给。", yamenDelta = 6, garrisonDelta = 3, route = V3Route.Loyalist),
            V3EventChoice("暗中招安", "危险但可能转化为武力。", silverDelta = -20, banditsDelta = 5, route = V3Route.Warlord, routeDelta = 7)
        )
    )

    private fun refugeesEvent() = V3ActiveEvent(
        title = "流民入境",
        body = "饥民沿河而来，跪在田庄外求一口粮。二房主张赈济，商支担心粮价上涨，武支则怕流民混入盗匪。",
        choices = listOf(
            V3EventChoice("开仓赈济", "民心大涨，粮食下降。", grainDelta = -70, villagersDelta = 12, cohesionDelta = 6, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("second", loyaltyDelta = 3, influenceDelta = 2, grievanceDelta = -3, note = "二房因赈济得民心，宗族内声望增长。"), V3BranchImpact("merchant", grievanceDelta = 2, note = "商支不满粮价机会被压下。"))),
            V3EventChoice("招为佃户", "田庄劳力增加，但短期消耗更高。", grainDelta = -35, villagersDelta = 6, influenceDelta = 2, route = V3Route.Fortress, branchImpacts = listOf(V3BranchImpact("second", influenceDelta = 1, note = "二房负责安置流民，声望略增。"), V3BranchImpact("martial", loyaltyDelta = 1, note = "武支认为收编流民有利于后续募勇。"))),
            V3EventChoice("驱逐流民", "保资源，失民心。", villagersDelta = -10, cohesionDelta = -4, banditsDelta = 4, route = V3Route.Warlord),
            V3EventChoice("交由县衙", "官府满意，乡民寒心。", yamenDelta = 6, villagersDelta = -6, route = V3Route.Loyalist)
        )
    )

    private fun plagueRumorEvent() = V3ActiveEvent(
        title = "瘟疫初闻",
        body = "医馆外忽有病者聚集，郎中称此症传得极快。若不早治，乡民离散，田庄也会误工。",
        choices = listOf(
            V3EventChoice("开设义诊", "消耗银两，乡民与凝聚上升。", silverDelta = -35, villagersDelta = 9, cohesionDelta = 5, route = V3Route.Hermit),
            V3EventChoice("封闭村口", "风险降低，但民心受损。", villagersDelta = -5, banditsDelta = -2, route = V3Route.Fortress),
            V3EventChoice("请县衙出面", "官府关系提高，乡民未必领情。", yamenDelta = 5, villagersDelta = -2, route = V3Route.Loyalist),
            V3EventChoice("购买药材", "花银换稳定。", silverDelta = -50, cohesionDelta = 4, villagersDelta = 4, route = V3Route.Hermit)
        )
    )

    private fun academyDebateEvent() = V3ActiveEvent(
        title = "书院清议",
        body = "东林书院诸生议论辽饷与党争，书香支认为这是扬名机会，主房却担心卷入朝局。",
        choices = listOf(
            V3EventChoice("资助讲会", "士林声望提升。", silverDelta = -30, gentryDelta = 8, influenceDelta = 5, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("scholar", influenceDelta = 5, loyaltyDelta = 2, grievanceDelta = -3, note = "书香支因讲会大振声名。"), V3BranchImpact("martial", grievanceDelta = 1, note = "武支认为银两不该尽投书院。"))),
            V3EventChoice("约束族人", "避开党争，宗族更稳。", cohesionDelta = 4, gentryDelta = -2, route = V3Route.Hermit),
            V3EventChoice("结交名士", "提高仕途路线倾向。", silverDelta = -20, gentryDelta = 5, yamenDelta = 2, route = V3Route.Loyalist),
            V3EventChoice("刊印文集", "声望和商路同时增长。", silverDelta = -25, influenceDelta = 4, merchantsDelta = 3, route = V3Route.Scholar)
        )
    )

    private fun seaMerchantEvent() = V3ActiveEvent(
        title = "海商密约",
        body = "三江码头有海商夜访，愿以海外货路相托。此事利润丰厚，也可能触怒官府。",
        choices = listOf(
            V3EventChoice("暗中合作", "海贸路线大幅推进。", silverDelta = 45, merchantsDelta = 8, yamenDelta = -4, route = V3Route.Overseas, routeDelta = 8, branchImpacts = listOf(V3BranchImpact("sea", wealthDelta = 5, influenceDelta = 4, loyaltyDelta = 2, grievanceDelta = -3, note = "海路支掌握新货路，迅速坐大。"), V3BranchImpact("main", grievanceDelta = 1, note = "主房担心海禁风险。"))),
            V3EventChoice("上报县衙", "官府关系提升，商帮失望。", yamenDelta = 8, merchantsDelta = -6, route = V3Route.Loyalist),
            V3EventChoice("只收过路费", "稳妥得银，不深涉海路。", silverDelta = 25, merchantsDelta = 2, route = V3Route.Merchant),
            V3EventChoice("拒绝往来", "避祸保身。", cohesionDelta = 2, merchantsDelta = -3, route = V3Route.Hermit)
        )
    )

    private fun merchantEscortEvent() = V3ActiveEvent(
        title = "商帮求护",
        body = "西河商帮愿以厚利相托，请宗族派人护送货队过山道。若成，可开商路；若败，武支与商支都将受挫。",
        choices = listOf(
            V3EventChoice("派武支护商", "商帮感激，武支威望增长。", silverDelta = 35, merchantsDelta = 10, garrisonDelta = 3, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("martial", influenceDelta = 3, loyaltyDelta = 2, grievanceDelta = -2, note = "武支因护商立功，威望提高。"), V3BranchImpact("merchant", wealthDelta = 4, grievanceDelta = -2, note = "商支货路得保，财富增长。"))),
            V3EventChoice("提高抽成", "收益更高，但商帮不满。", silverDelta = 60, merchantsDelta = -2, route = V3Route.Merchant, routeDelta = 5),
            V3EventChoice("拒绝牵连", "保持低调，错失商机。", cohesionDelta = 1, merchantsDelta = -4, route = V3Route.Hermit),
            V3EventChoice("借机入股", "推动海贸伏笔。", silverDelta = -30, merchantsDelta = 7, route = V3Route.Overseas, routeDelta = 7)
        )
    )

    private fun branchDisputeEvent() = V3ActiveEvent(
        title = "房支争产",
        body = "商支称田庄收益分配不公，武支也抱怨修寨无银。族老请你当众裁断，稍有偏颇就会伤及宗族凝聚。",
        choices = listOf(
            V3EventChoice("按族规裁断", "凝聚回升，但怨气仍存。", cohesionDelta = 8, influenceDelta = 2, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 3, loyaltyDelta = 2, note = "主房以族规裁断，权威回升。"), V3BranchImpact("merchant", grievanceDelta = -2, note = "商支虽未尽得利益，但认可族规程序。"), V3BranchImpact("martial", grievanceDelta = -2, note = "武支接受公议，怨气稍平。"))),
            V3EventChoice("让利商支", "商路更稳，其他房支不满。", silverDelta = -20, merchantsDelta = 6, cohesionDelta = -2, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", wealthDelta = 5, loyaltyDelta = 2, grievanceDelta = -4, note = "商支得利，怨气明显下降。"), V3BranchImpact("martial", grievanceDelta = 2, note = "武支认为分配偏向商支。"), V3BranchImpact("scholar", grievanceDelta = 1, note = "书香支也担心族产失衡。"))),
            V3EventChoice("补贴武支", "乡勇自保路线增强。", silverDelta = -20, garrisonDelta = 5, cohesionDelta = -1, route = V3Route.Fortress, branchImpacts = listOf(V3BranchImpact("martial", wealthDelta = 3, loyaltyDelta = 2, grievanceDelta = -4, note = "武支得补贴，愿意继续守寨。"), V3BranchImpact("merchant", grievanceDelta = 2, note = "商支不满银两转投武备。"), V3BranchImpact("sea", grievanceDelta = 1, note = "海路支担心码头投入被挤占。"))),
            V3EventChoice("严惩争执者", "威望上升，凝聚下降。", influenceDelta = 5, cohesionDelta = -6, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 4, loyaltyDelta = -1, note = "主房威权增强，但人心转冷。"), V3BranchImpact("merchant", grievanceDelta = 3, loyaltyDelta = -2, note = "商支因受压而暗生怨望。"), V3BranchImpact("martial", grievanceDelta = 3, loyaltyDelta = -2, note = "武支也因重罚而心有不服。")))
        )
    )

    private fun branchDemandEvent(branchId: String, branchName: String, focus: V3Route) = V3ActiveEvent(
        title = "$branchName 议事逼宫",
        body = "$branchName 怨气已高，支主带人入祠，要求重新分配族产与人手。若压不住，宗族凝聚会继续下滑。",
        choices = listOf(
            V3EventChoice("当众安抚", "以族义压住纷争，花费资源换取暂时稳定。", silverDelta = -25, grainDelta = -20, cohesionDelta = 5, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact(branchId, loyaltyDelta = 2, grievanceDelta = -8, note = "$branchName 得到安抚，怨气明显下降。"))),
            V3EventChoice("顺其诉求", "让该房支获得更多资源与权柄。", silverDelta = -35, cohesionDelta = -1, route = focus, routeDelta = 6, branchImpacts = listOf(V3BranchImpact(branchId, wealthDelta = 6, influenceDelta = 5, loyaltyDelta = 3, grievanceDelta = -10, note = "$branchName 获得让步，势力增强。"), V3BranchImpact("main", grievanceDelta = 2, note = "主房对权柄旁落不满。"))),
            V3EventChoice("严词申饬", "维护族长权威，但该房支会更不服。", influenceDelta = 4, cohesionDelta = -5, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact(branchId, loyaltyDelta = -5, grievanceDelta = 7, note = "$branchName 被压下，怨气反而加深。"), V3BranchImpact("main", influenceDelta = 3, note = "主房威权上升。"))),
            V3EventChoice("许来年再议", "暂缓冲突，不解决根源。", cohesionDelta = 1, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact(branchId, grievanceDelta = -3, note = "$branchName 暂时退让，但仍会观望。")))
        )
    )

    private fun countyRumorEvent() = V3ActiveEvent(
        title = "县中流言",
        body = "茶肆传言朝廷将再加派辽饷，县中士绅暗自观望。是否提前布局，会影响未来路线。",
        choices = listOf(
            V3EventChoice("拜访士绅", "士绅关系改善。", silverDelta = -15, gentryDelta = 8, route = V3Route.Scholar),
            V3EventChoice("囤粮观望", "粮食压力缓解。", silverDelta = -20, grainDelta = 45, route = V3Route.Hermit),
            V3EventChoice("扩张商路", "商帮关系改善。", silverDelta = -25, merchantsDelta = 8, route = V3Route.Merchant),
            V3EventChoice("募练乡勇", "军镇关系提升。", silverDelta = -25, garrisonDelta = 6, route = V3Route.Fortress)
        )
    )

    private fun eventMatchesState(event: V3ActiveEvent, state: V3GameState): Boolean {
        val personOk = event.choices.all { choice ->
            choice.personId == null || state.people.any { it.id == choice.personId && it.alive && event.body.contains(it.name) }
        }
        val siteOk = event.choices.all { choice -> choice.siteId == null || state.sites.any { it.id == choice.siteId } }
        val branchIds = state.branches.map { it.id }.toSet()
        val branchOk = event.choices.flatMap { it.branchImpacts }.all { impact ->
            impact.branchId == "main" ||
                impact.branchId in branchIds ||
                state.branches.any { branch -> branchMatchesImpact(branch.id, branch.focus, impact.branchId) }
        }
        return personOk && siteOk && branchOk && eventTimeMatches(event, state) && eventProgressMatches(event, state)
    }

    private fun eventTimeMatches(event: V3ActiveEvent, state: V3GameState): Boolean {
        val title = event.title
        val monthWords = mapOf(
            1 to listOf("正月"),
            2 to listOf("二月"),
            3 to listOf("三月", "清明"),
            4 to listOf("四月"),
            5 to listOf("五月", "端午"),
            6 to listOf("六月"),
            7 to listOf("七月", "鬼节"),
            8 to listOf("八月", "秋税", "商会"),
            9 to listOf("九月", "重阳"),
            10 to listOf("十月"),
            11 to listOf("冬月"),
            12 to listOf("腊月")
        )
        val allMonthWords = monthWords.values.flatten()
        if (allMonthWords.any { title.contains(it) }) {
            return monthWords[state.month].orEmpty().any { title.contains(it) }
        }
        if (title.contains("万历末") && state.year > 1620) return false
        if (title.contains("天启") && state.year !in 1621..1627) return false
        if (title.contains("崇祯") && state.year < 1628) return false
        if ((title.contains("关外") || title.contains("流寇转战")) && state.year < 1630) return false
        if ((title.contains("甲申") || title.contains("南迁")) && state.year < 1640) return false
        return true
    }

    private fun eventProgressMatches(event: V3ActiveEvent, state: V3GameState): Boolean {
        val title = event.title
        val builtSites = V3GameEngine.builtSiteCount(state)
        val people = V3GameEngine.alivePeople(state).size
        val controlledRegions = V3GameEngine.controlledRegionCount(state)
        val routeScore = event.choices.maxOfOrNull { state.routeScores[it.route] ?: 0 } ?: 0
        if ((title.contains("成局") || title.contains("声名") || title.contains("荐才") || title.contains("议纲") || title.contains("据点") || title.contains("分歧")) && routeScore < 18) return false
        if ((title.contains("府城") || title.contains("跨县") || title.contains("省城")) && state.clanRank < 2) return false
        if ((title.contains("一方豪强") || title.contains("京畿") || title.contains("天下")) && (state.clanRank < 3 || controlledRegions < 1)) return false
        if ((title.contains("家丁成队") || title.contains("堡寨") || title.contains("练勇")) && state.militia < 35) return false
        if ((title.contains("人丁") || title.contains("添役") || title.contains("分房")) && people < 3) return false
        if ((title.contains("产业") || title.contains("商号") || title.contains("铺")) && builtSites < 1) return false
        return true
    }

    private fun applyBranchImpacts(state: V3GameState, impacts: List<V3BranchImpact>) = if (impacts.isEmpty()) {
        state.branches
    } else {
        state.branches.map { branch ->
            val related = impacts.filter { impact -> branchMatchesImpact(branch.id, branch.focus, impact.branchId) }
            if (related.isEmpty()) {
                branch
            } else {
                branch.copy(
                    loyalty = clamp01(branch.loyalty + related.sumOf { it.loyaltyDelta }),
                    wealth = clamp01(branch.wealth + related.sumOf { it.wealthDelta }),
                    influence = clamp01(branch.influence + related.sumOf { it.influenceDelta }),
                    grievance = clamp01(branch.grievance + related.sumOf { it.grievanceDelta })
                )
            }
        }
    }

    private fun branchMatchesImpact(branchId: String, focus: V3Route, impactId: String): Boolean =
        branchId == impactId || when (impactId) {
            "main" -> branchId == "main"
            "merchant" -> focus == V3Route.Merchant
            "martial" -> focus == V3Route.Fortress || focus == V3Route.Warlord
            "scholar" -> focus == V3Route.Scholar || focus == V3Route.Loyalist
            "sea" -> focus == V3Route.Overseas
            "second" -> branchId != "main"
            else -> false
        }

    private fun clamp(value: Int): Int = min(100, max(-100, value))

    private fun clamp01(value: Int): Int = min(100, max(0, value))
}
