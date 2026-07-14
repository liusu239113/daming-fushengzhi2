package com.daming.fushengzhi3.v3.logic

import com.daming.fushengzhi3.v3.data.V3ActiveEvent
import com.daming.fushengzhi3.v3.data.V3BranchImpact
import com.daming.fushengzhi3.v3.data.V3EventContent
import com.daming.fushengzhi3.v3.data.V3EventChoice
import com.daming.fushengzhi3.v3.data.V3GameState
import com.daming.fushengzhi3.v3.data.V3Route
import kotlin.math.max
import kotlin.math.min

object V3EventEngine {
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
        if (!shouldRoutineEvent(state, totalRisk)) return null

        val weightedPool = V3EventContent.allEvents.filter { event ->
            eventMatchesState(event, state) && run {
                val titleNotRecent = state.eventLog.take(8).none { it.contains(event.title) }
                val crisisMatched = when (state.crisis) {
                    "官府催税" -> event.title.contains("县") || event.title.contains("衙") || event.title.contains("辽饷") || event.title.contains("徭役")
                    "流寇逼近" -> event.title.contains("山") || event.title.contains("寇") || event.title.contains("寨") || event.title.contains("勇")
                    "饥荒将至" -> event.title.contains("粮") || event.title.contains("田") || event.title.contains("民") || event.title.contains("仓")
                    "族产争端" -> event.title.contains("房") || event.title.contains("祠") || event.title.contains("账") || event.title.contains("议")
                    "商路断绝" -> event.title.contains("商") || event.title.contains("市") || event.title.contains("码头") || event.title.contains("船")
                    "瘟疫初起" -> event.title.contains("医") || event.title.contains("药") || event.title.contains("疫") || event.title.contains("病")
                    else -> false
                }
                titleNotRecent && (crisisMatched || state.month % 3 == 0 || event.choices.any { it.siteId != null && (state.sites.firstOrNull { site -> site.id == it.siteId }?.risk ?: 0) >= 35 })
            }
        }
        val fallbackPool = V3EventContent.allEvents.filter { eventMatchesState(it, state) }
        val pool = if (weightedPool.isNotEmpty()) weightedPool else fallbackPool
        if (pool.isEmpty()) return null
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
        val log = (listOf("事件【${state.activeEvent?.title ?: "县域抉择"}】选择：${choice.label}。${choice.desc}") + branchNotes).joinToString(" ")
        return state.copy(
            silver = state.silver + choice.silverDelta,
            grain = state.grain + choice.grainDelta,
            militia = (state.militia + choice.militiaDelta).coerceIn(0, 999),
            cohesion = (state.cohesion + choice.cohesionDelta).coerceIn(0, 100),
            influence = (state.influence + choice.influenceDelta).coerceIn(0, 100),
            sites = nextSites,
            people = nextPeople,
            relations = nextRelations,
            branches = nextBranches,
            routeScores = state.routeScores + (choice.route to routeScore),
            activeEvent = null,
            pendingReports = listOf(log),
            eventLog = (listOf("${state.year}年${state.month}月 · $log") + state.eventLog).take(100)
        )
    }

    private fun shouldRoutineEvent(state: V3GameState, totalRisk: Int): Boolean {
        if (state.month % 4 == 0) return true
        if (state.month % 3 == 0 && (totalRisk >= 260 || state.silver < 70 || state.grain < 120)) return true
        if (state.month == 12) return true
        return false
    }

    private fun historicalEvent(state: V3GameState): V3ActiveEvent? {
        val key = state.year to state.month
        if (state.eventLog.take(30).any { it.contains("史事") && it.contains(state.year.toString()) }) return null
        return when (key) {
            1601 to 9 -> V3ActiveEvent(
                "史事：矿税余波",
                "万历末年，矿监税使之弊虽稍退，地方仍以旧账追索。清河县衙重翻商税与田税册，李氏若不表态，集市、田庄都会被牵动。",
                listOf(
                    V3EventChoice("清账输税", "以银换安，官府关系回暖。", silverDelta = -45, yamenDelta = 10, merchantsDelta = -2, influenceDelta = 2, siteId = "yamen", siteRiskDelta = -8, route = V3Route.Loyalist),
                    V3EventChoice("联络士绅缓征", "借士绅之力抗税，声名上升但县衙不悦。", silverDelta = -20, gentryDelta = 9, yamenDelta = -6, influenceDelta = 5, route = V3Route.Scholar),
                    V3EventChoice("暗改账册", "短期保财，但商路和官府风险变高。", silverDelta = 35, yamenDelta = -8, merchantsDelta = 5, siteId = "market", siteRiskDelta = 10, route = V3Route.Merchant)
                )
            )
            1619 to 4 -> V3ActiveEvent(
                "史事：萨尔浒败闻",
                "辽东大败的消息顺江而下，军需、辽饷、募兵风声一齐压到县中。宗祠议事不再只是家产，已开始牵连边事。",
                listOf(
                    V3EventChoice("输粮勤王", "勤王名声上升，粮仓承压。", grainDelta = -70, yamenDelta = 8, garrisonDelta = 9, influenceDelta = 4, route = V3Route.Loyalist, routeDelta = 9),
                    V3EventChoice("修寨屯粮", "转向乱世自保。", silverDelta = -35, grainDelta = -25, militiaDelta = 12, siteId = "fort", siteControlDelta = 8, siteRiskDelta = -10, route = V3Route.Fortress, routeDelta = 9),
                    V3EventChoice("扩大商路备银", "以财应变，商路加强。", silverDelta = 60, merchantsDelta = 7, yamenDelta = -3, route = V3Route.Merchant, routeDelta = 8)
                )
            )
            1621 to 7 -> V3ActiveEvent(
                "史事：辽事再急",
                "天启初年，辽东战报频仍，县中军户与商帮都在囤粮避祸。李氏的选择会让家族路线更偏向勤王、自保或逐利。",
                listOf(
                    V3EventChoice("募勇入册", "乡勇增加，军镇关系改善。", silverDelta = -40, grainDelta = -25, militiaDelta = 18, garrisonDelta = 10, route = V3Route.Loyalist, routeDelta = 8),
                    V3EventChoice("堡寨盟约", "自保路线增强，乡民更安。", grainDelta = -35, militiaDelta = 10, villagersDelta = 7, siteId = "fort", siteControlDelta = 10, route = V3Route.Fortress, routeDelta = 9),
                    V3EventChoice("囤货等价", "银两上涨但民心下降。", silverDelta = 90, villagersDelta = -8, merchantsDelta = 8, route = V3Route.Merchant, routeDelta = 8)
                )
            )
            1627 to 10 -> V3ActiveEvent(
                "史事：天启崩，崇祯立",
                "京中传来大行皇帝崩逝、新君即位。清流称新政可期，县衙却催各族重新表忠。李氏该押注朝局，还是守住家业？",
                listOf(
                    V3EventChoice("递表称贺", "官府关系提升，花费银两。", silverDelta = -35, yamenDelta = 10, influenceDelta = 3, route = V3Route.Loyalist, routeDelta = 8),
                    V3EventChoice("资助书院清议", "士林声望上升，党争风险加深。", silverDelta = -45, gentryDelta = 10, yamenDelta = -5, influenceDelta = 6, siteId = "academy", siteRiskDelta = 6, route = V3Route.Scholar, routeDelta = 9),
                    V3EventChoice("闭祠修谱", "避开朝局，凝聚上升。", grainDelta = -20, cohesionDelta = 7, route = V3Route.Hermit, routeDelta = 7)
                )
            )
            1628 to 6 -> V3ActiveEvent(
                "史事：崇祯清饷",
                "新政要清理积弊，却也让地方催饷更急。差役登门，士绅观望，商帮怕税，乡民怕役。",
                listOf(
                    V3EventChoice("替民缓饷", "民心大升，县衙不悦。", silverDelta = -30, villagersDelta = 12, yamenDelta = -6, influenceDelta = 5, route = V3Route.Scholar),
                    V3EventChoice("足额输饷", "官府满意，资源受损。", silverDelta = -75, grainDelta = -35, yamenDelta = 12, garrisonDelta = 4, route = V3Route.Loyalist),
                    V3EventChoice("以商税抵饷", "商路承压但家业保住。", silverDelta = -35, merchantsDelta = -4, yamenDelta = 5, siteId = "market", siteRiskDelta = 6, route = V3Route.Merchant)
                )
            )
            1630 to 3 -> V3ActiveEvent(
                "史事：陕北流寇",
                "西北饥荒与流寇的传闻传入江南，逃户、募兵、粮价一起动荡。李氏需要决定是赈济、练兵，还是趁乱扩财。",
                listOf(
                    V3EventChoice("开粥棚收流民", "人口与民心机会增加，粮食下降。", grainDelta = -85, villagersDelta = 14, cohesionDelta = 5, route = V3Route.Hermit, routeDelta = 7),
                    V3EventChoice("募勇守庄", "乡勇上升，割据伏笔加深。", silverDelta = -45, grainDelta = -25, militiaDelta = 22, siteId = "fort", siteControlDelta = 8, route = V3Route.Fortress, routeDelta = 10),
                    V3EventChoice("囤粮抬价", "银两大增，民心大跌。", silverDelta = 120, grainDelta = -25, villagersDelta = -14, merchantsDelta = 8, route = V3Route.Merchant, routeDelta = 9)
                )
            )
            1636 to 5 -> V3ActiveEvent(
                "史事：关外称帝",
                "关外改号称帝的消息震动南北。朝廷催兵催饷，地方豪族开始各谋退路。李氏已不能只做县中小族。",
                listOf(
                    V3EventChoice("响应勤王", "勤王路线大进，军镇关系提升。", silverDelta = -70, grainDelta = -50, militiaDelta = 15, garrisonDelta = 14, influenceDelta = 6, route = V3Route.Loyalist, routeDelta = 12),
                    V3EventChoice("扩寨藏兵", "割据和自保路线大进，官府猜忌。", silverDelta = -55, grainDelta = -35, militiaDelta = 26, yamenDelta = -8, siteId = "fort", siteControlDelta = 10, route = V3Route.Warlord, routeDelta = 12),
                    V3EventChoice("筹船留后路", "海外路线增强。", silverDelta = -80, merchantsDelta = 9, siteId = "dock", siteControlDelta = 9, route = V3Route.Overseas, routeDelta = 12)
                )
            )
            1642 to 8 -> V3ActiveEvent(
                "史事：天下土崩",
                "北方城池屡陷，逃官、饥民、败兵接连入境。若李氏已有兵粮，此时可争一方；若根基不足，只能求保香火。",
                listOf(
                    V3EventChoice("接管县防", "举旗割据的前奏，风险与声望同升。", silverDelta = -60, grainDelta = -60, militiaDelta = 35, yamenDelta = -15, influenceDelta = 12, siteId = "yamen", siteControlDelta = 18, route = V3Route.Warlord, routeDelta = 16),
                    V3EventChoice("闭境保族", "隐忍自保，凝聚上升。", grainDelta = -45, cohesionDelta = 10, villagersDelta = 6, siteId = "fort", siteRiskDelta = -15, route = V3Route.Hermit, routeDelta = 10),
                    V3EventChoice("南迁海路", "海外路线强推，家族撕裂。", silverDelta = -120, cohesionDelta = -5, merchantsDelta = 12, route = V3Route.Overseas, routeDelta = 16)
                )
            )
            1644 to 3 -> V3ActiveEvent(
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
        val branchOk = event.choices.flatMap { it.branchImpacts }.all { impact -> impact.branchId == "main" || impact.branchId in branchIds }
        return personOk && siteOk && branchOk
    }

    private fun applyBranchImpacts(state: V3GameState, impacts: List<V3BranchImpact>) = if (impacts.isEmpty()) {
        state.branches
    } else {
        state.branches.map { branch ->
            val related = impacts.filter { it.branchId == branch.id }
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

    private fun clamp(value: Int): Int = min(100, max(-100, value))

    private fun clamp01(value: Int): Int = min(100, max(0, value))
}
