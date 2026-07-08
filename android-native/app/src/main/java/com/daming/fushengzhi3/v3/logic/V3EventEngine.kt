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
        if (V3GameEngine.alivePeople(state).size < 2 && state.year == 1601 && state.month <= 6) return null
        val totalRisk = state.sites.sumOf { it.risk }
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

        val weightedPool = V3EventContent.allEvents.filter { event ->
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
        val pool = if (weightedPool.isNotEmpty()) weightedPool else V3EventContent.allEvents
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
