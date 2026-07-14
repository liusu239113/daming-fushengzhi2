package com.daming.fushengzhi3.v3.logic

import com.daming.fushengzhi3.v3.data.V3AnnualGoal
import com.daming.fushengzhi3.v3.data.V3Branch
import com.daming.fushengzhi3.v3.data.V3Content
import com.daming.fushengzhi3.v3.data.V3CountySite
import com.daming.fushengzhi3.v3.data.V3CountySiteType
import com.daming.fushengzhi3.v3.data.V3EndingPreview
import com.daming.fushengzhi3.v3.data.V3EstateAsset
import com.daming.fushengzhi3.v3.data.V3EstateType
import com.daming.fushengzhi3.v3.data.V3EndingTier
import com.daming.fushengzhi3.v3.data.V3FinalEnding
import com.daming.fushengzhi3.v3.data.V3GameState
import com.daming.fushengzhi3.v3.data.V3Gender
import com.daming.fushengzhi3.v3.data.V3GoalMetric
import com.daming.fushengzhi3.v3.data.V3MonthlyForecast
import com.daming.fushengzhi3.v3.data.V3MonthlyReport
import com.daming.fushengzhi3.v3.data.V3Person
import com.daming.fushengzhi3.v3.data.V3RankCost
import com.daming.fushengzhi3.v3.data.V3RegionStatus
import com.daming.fushengzhi3.v3.data.V3Relations
import com.daming.fushengzhi3.v3.data.V3Route
import com.daming.fushengzhi3.v3.data.V3Screen
import com.daming.fushengzhi3.v3.data.V3SiteStatus
import com.daming.fushengzhi3.v3.data.V3SiteYield
import com.daming.fushengzhi3.v3.data.V3SpouseCandidate
import com.daming.fushengzhi3.v3.data.V3TaskType
import com.daming.fushengzhi3.v3.data.V3TrainingType
import com.daming.fushengzhi3.v3.data.V3Trait
import com.daming.fushengzhi3.v3.data.V3UpgradeCost
import com.daming.fushengzhi3.v3.data.V3BattleState
import com.daming.fushengzhi3.v3.data.V3ConquestState
import com.daming.fushengzhi3.v3.data.V3ExamQuestion
import com.daming.fushengzhi3.v3.data.V3ExamSession
import com.daming.fushengzhi3.v3.data.V3ExamStage
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

    fun estateLevelTotal(state: V3GameState): Int = state.estateAssets.sumOf { it.level }

    fun controlledRegionCount(state: V3GameState): Int = state.worldRegions.count { it.status == V3RegionStatus.Controlled || it.status == V3RegionStatus.Pacified }

    fun estateYield(asset: V3EstateAsset): V3SiteYield = when (asset.type) {
        V3EstateType.TenantLand -> V3SiteYield(silver = 2 * asset.level, grain = 22 * asset.level, desc = "佃田租谷")
        V3EstateType.Shop -> V3SiteYield(silver = 18 * asset.level, desc = "铺面营收")
        V3EstateType.Workshop -> V3SiteYield(silver = 26 * asset.level, grain = -2 * asset.level, desc = "作坊利润")
        V3EstateType.Warehouse -> V3SiteYield(grain = 10 * asset.level, cohesion = asset.level, desc = "粮仓周转")
        V3EstateType.Caravan -> V3SiteYield(silver = 24 * asset.level, influence = asset.level, desc = "商队通路")
        V3EstateType.Barracks -> V3SiteYield(silver = -4 * asset.level, grain = -5 * asset.level, militia = 8 * asset.level, desc = "团练养勇")
    }

    fun estateUpgradeCost(state: V3GameState, type: V3EstateType): V3UpgradeCost {
        val current = state.estateAssets.firstOrNull { it.type == type }
        val nextLevel = (current?.level ?: 0) + 1
        val baseSilver = when (type) {
            V3EstateType.TenantLand -> 28
            V3EstateType.Shop -> 42
            V3EstateType.Workshop -> 70
            V3EstateType.Warehouse -> 55
            V3EstateType.Caravan -> 90
            V3EstateType.Barracks -> 85
        }
        val baseGrain = when (type) {
            V3EstateType.TenantLand -> 12
            V3EstateType.Shop -> 8
            V3EstateType.Workshop -> 22
            V3EstateType.Warehouse -> 40
            V3EstateType.Caravan -> 28
            V3EstateType.Barracks -> 55
        }
        return V3UpgradeCost(baseSilver * nextLevel, baseGrain * nextLevel, type.desc)
    }

    fun upgradeEstate(state: V3GameState, type: V3EstateType): V3GameState {
        val current = state.estateAssets.firstOrNull { it.type == type }
        if ((current?.level ?: 0) >= 5) return state.copy(pendingReports = listOf("${type.label}已达最高等级。"))
        val cost = estateUpgradeCost(state, type)
        val requiredPopulation = when (type) {
            V3EstateType.Workshop -> 4
            V3EstateType.Caravan -> 5
            V3EstateType.Barracks -> 6
            else -> 1
        }
        if (alivePeople(state).size < requiredPopulation) return state.copy(pendingReports = listOf("经营${type.label}需要人口至少$requiredPopulation。先成婚育子、扩大家族。"))
        if (state.silver < cost.silver || state.grain < cost.grain) return state.copy(pendingReports = listOf("扩建${type.label}需要银${cost.silver}、粮${cost.grain}。"))
        val nextLevel = (current?.level ?: 0) + 1
        val nextAsset = V3EstateAsset(type.name.lowercase(), type, nextLevel, workers = requiredPopulation, desc = type.desc)
        val assets = if (current == null) state.estateAssets + nextAsset else state.estateAssets.map { if (it.type == type) nextAsset else it }
        val route = when (type) {
            V3EstateType.Barracks -> V3Route.Fortress
            V3EstateType.Caravan, V3EstateType.Shop, V3EstateType.Workshop -> V3Route.Merchant
            else -> V3Route.Hermit
        }
        val message = "家产扩建【${type.label}】至 Lv.$nextLevel：${yieldText(estateYield(nextAsset))}。${type.desc}"
        return state.copy(
            silver = state.silver - cost.silver,
            grain = state.grain - cost.grain,
            estateAssets = assets,
            routeScores = state.routeScores + (route to ((state.routeScores[route] ?: 0) + 5)),
            pendingReports = listOf(message),
            eventLog = (listOf("${state.year}年${state.month}月 · $message") + state.eventLog).take(100)
        )
    }

    fun contactRegion(state: V3GameState, regionId: String): V3GameState {
        val region = state.worldRegions.firstOrNull { it.id == regionId } ?: return state
        if (region.status != V3RegionStatus.Unknown) return state.copy(pendingReports = listOf("${region.name}已经进入家族视野，可继续经营或征伐。"))
        val costSilver = 30 + region.tier * 24
        val costInfluence = region.tier * 6
        if (state.silver < costSilver || state.influence < costInfluence) return state.copy(pendingReports = listOf("结交${region.name}需要银$costSilver、族望$costInfluence。"))
        val regions = state.worldRegions.map { if (it.id == regionId) it.copy(status = V3RegionStatus.Contacted, control = 12) else it }
        val message = "派人结交【${region.name}】：商路、士绅和乡勇开始进入此地。后续可经营声望或征伐控制。"
        return state.copy(
            silver = state.silver - costSilver,
            worldRegions = regions,
            routeScores = state.routeScores + (V3Route.Merchant to ((state.routeScores[V3Route.Merchant] ?: 0) + 3)),
            pendingReports = listOf(message),
            eventLog = (listOf("${state.year}年${state.month}月 · $message") + state.eventLog).take(100)
        )
    }

    fun influenceRegion(state: V3GameState, regionId: String): V3GameState {
        val region = state.worldRegions.firstOrNull { it.id == regionId } ?: return state
        if (region.status == V3RegionStatus.Unknown) return contactRegion(state, regionId)
        if (region.status == V3RegionStatus.Pacified) return state.copy(pendingReports = listOf("${region.name}已经归附，无需再经营。"))
        val costSilver = 18 + region.tier * 16
        val costGrain = 10 + region.tier * 12
        if (state.silver < costSilver || state.grain < costGrain) return state.copy(pendingReports = listOf("经营${region.name}需要银$costSilver、粮$costGrain。"))
        val gain = 12 + state.influence / 10 + estateLevelTotal(state) / 3
        val nextControl = (region.control + gain).coerceAtMost(100)
        val nextStatus = when {
            nextControl >= 80 -> V3RegionStatus.Pacified
            nextControl >= 45 -> V3RegionStatus.Influenced
            else -> V3RegionStatus.Contacted
        }
        val regions = state.worldRegions.map { if (it.id == regionId) it.copy(control = nextControl, status = nextStatus) else it }
        val message = "经营【${region.name}】：控制+$gain，当前$nextControl。通过商路、婚盟、士绅和族产渗入地方。"
        return state.copy(
            silver = state.silver - costSilver,
            grain = state.grain - costGrain,
            worldRegions = regions,
            influence = (state.influence + (if (nextStatus == V3RegionStatus.Pacified) 4 else 1)).coerceIn(0, 100),
            unificationProgress = calculateUnification(regions),
            routeScores = state.routeScores + (V3Route.Merchant to ((state.routeScores[V3Route.Merchant] ?: 0) + 4)),
            pendingReports = listOf(message),
            eventLog = (listOf("${state.year}年${state.month}月 · $message") + state.eventLog).take(100)
        )
    }

    fun startConquest(state: V3GameState, regionId: String): V3GameState {
        if (state.conquestState != null) return state
        val region = state.worldRegions.firstOrNull { it.id == regionId } ?: return state
        if (region.status == V3RegionStatus.Pacified || region.status == V3RegionStatus.Controlled) return state.copy(pendingReports = listOf("${region.name}已在掌中，继续经营即可。"))
        val prerequisite = if (region.tier <= 2) 1 else region.tier
        if (controlledRegionCount(state) < prerequisite) return state.copy(pendingReports = listOf("征伐${region.name}前，至少要控制$prerequisite 个地域。先稳住县域和府县。"))
        if (state.militia < 40 + region.tier * 25) return state.copy(pendingReports = listOf("乡勇不足，征伐${region.name}至少需要${40 + region.tier * 25}。"))
        val enemy = region.enemyPower + state.rebelHeat / 2 - region.control / 3
        val conquest = V3ConquestState(region.id, enemy, region.wealth / 2, region.wealth / 3, 5 + region.tier * 3, region.name, if (region.tier >= 4) "天下大战" else "地域征伐")
        return state.copy(conquestState = conquest, pendingReports = listOf("已准备${conquest.scale}【${region.name}】：敌势${conquest.enemyPower}。胜则控制地域，败则折损乡勇。"))
    }

    fun cancelConquest(state: V3GameState): V3GameState = state.copy(conquestState = null, pendingReports = listOf("已暂缓征伐，粮草和乡勇留待后用。"))

    fun resolveConquest(state: V3GameState): V3GameState {
        val conquest = state.conquestState ?: return state
        val region = state.worldRegions.firstOrNull { it.id == conquest.regionId } ?: return state.copy(conquestState = null)
        val bestWarrior = alivePeople(state).maxByOrNull { it.martial + it.merit / 4 }
        val barracks = state.estateAssets.firstOrNull { it.type == V3EstateType.Barracks }?.level ?: 0
        val fortLevel = state.sites.firstOrNull { it.type == V3CountySiteType.Fort }?.level ?: 0
        val power = state.militia + (bestWarrior?.martial ?: 0) + barracks * 24 + fortLevel * 14 + state.influence / 2 + state.unificationProgress / 2
        val victory = power >= conquest.enemyPower
        val loss = if (victory) max(12, conquest.enemyPower / 16) else max(28, conquest.enemyPower / 8)
        val nextRegions = state.worldRegions.map {
            if (it.id == region.id) {
                val control = (it.control + (if (victory) 65 else 18)).coerceAtMost(100)
                it.copy(control = control, status = if (victory) V3RegionStatus.Controlled else V3RegionStatus.Influenced)
            } else it
        }
        val nextProgress = calculateUnification(nextRegions)
        val nextPeople = state.people.map {
            if (it.id == bestWarrior?.id) it.copy(martial = (it.martial + (if (victory) 3 else 1)).coerceAtMost(100), merit = (it.merit + (if (victory) 18 else 6)).coerceAtMost(999), militaryRank = if (victory && it.militaryRank == null) "统兵族将" else it.militaryRank, fatigue = (it.fatigue + (if (victory) 18 else 32)).coerceIn(0, 100)) else it
        }
        val message = if (victory) "征伐【${region.name}】得胜，家族势力跨出县域，统一进度推进到$nextProgress。" else "征伐【${region.name}】失利，虽未控制地域，但地方已知李氏兵威。"
        return state.copy(
            people = nextPeople,
            worldRegions = nextRegions,
            unificationProgress = nextProgress,
            conquestState = null,
            militia = (state.militia - loss).coerceAtLeast(0),
            silver = state.silver + (if (victory) conquest.rewardSilver else 0),
            grain = state.grain + (if (victory) conquest.rewardGrain else 0),
            influence = (state.influence + (if (victory) conquest.rewardInfluence else 1)).coerceIn(0, 100),
            rebelHeat = (state.rebelHeat + (if (victory) 8 + region.tier * 4 else 4)).coerceAtMost(100),
            routeScores = state.routeScores + (V3Route.Warlord to ((state.routeScores[V3Route.Warlord] ?: 0) + (if (victory) 16 else 4))),
            pendingReports = listOf(message),
            eventLog = (listOf("${state.year}年${state.month}月 · $message") + state.eventLog).take(100)
        )
    }

    fun proclaimUnification(state: V3GameState): V3GameState {
        val controlled = controlledRegionCount(state)
        val realmControlled = state.worldRegions.any { it.id == "all_realm" && (it.status == V3RegionStatus.Controlled || it.status == V3RegionStatus.Pacified) }
        val power = state.unificationProgress + state.militia / 6 + state.influence / 2 + estateLevelTotal(state) * 2 + alivePeople(state).size * 2
        if (!realmControlled || controlled < 6 || power < 150) return state.copy(pendingReports = listOf("统一条件不足：需控制清河、府县、南直隶、京畿与天下节点，且综合国力达到150。当前地域$controlled/6，国力$power/150。"))
        val ending = V3FinalEnding(
            route = V3Route.Warlord,
            tier = V3EndingTier.Historic,
            score = power + state.unificationProgress,
            title = "李氏定鼎天下",
            body = "李氏由一人起家，娶妻生子、置田开铺、修祠建学、团练征伐，最终从清河县扩张到州府、京畿与天下。旧朝崩裂之际，家族不再只是求活，而是以宗族、产业与兵权重塑秩序。",
            stats = listOf("统一进度：${state.unificationProgress}", "控制地域：$controlled / ${state.worldRegions.size}", "家产总等级：${estateLevelTotal(state)}", "家族人口：${alivePeople(state).size}", "乡勇：${state.militia}", "族望：${state.influence}")
        )
        return state.copy(finalEnding = ending, activeEvent = null, conquestState = null, pendingReports = listOf("天下归一，李氏家乘写入新朝开篇。"))
    }

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
        val silver = 16 + nextLevel * 16 + site.risk / 8
        val grain = 8 + nextLevel * 10
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
        val training = person.trainingFocus
        if (training != null) return state.copy(pendingReports = listOf("${person.name}本月正在${training.label}，不能同时外出办事。"))
        val previousPersonId = site.assignedPersonId
        val previousSiteId = person.assignedSiteId
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
                it.id == previousSiteId -> it.copy(assignedPersonId = null)
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

    fun trainPerson(state: V3GameState, personId: Int, training: V3TrainingType): V3GameState {
        val person = state.people.firstOrNull { it.id == personId && it.alive } ?: return state
        if (person.currentTask != null || person.trainingFocus != null) {
            return state.copy(pendingReports = listOf("${person.name}本月已有安排，推进月结后才能重新培养。"))
        }
        val costSilver = if (person.age < 12) 2 else 5
        val costGrain = if (person.age < 12) 1 else 2
        if (state.silver < costSilver || state.grain < costGrain) {
            return state.copy(pendingReports = listOf("培养【${person.name}】需要银$costSilver、粮$costGrain。"))
        }
        val people = state.people.map {
            if (it.id == personId) it.copy(trainingFocus = training, assignedSiteId = null, currentTask = null) else it
        }
        val sites = state.sites.map { if (it.assignedPersonId == personId) it.copy(assignedPersonId = null) else it }
        return state.copy(
            silver = state.silver - costSilver,
            grain = state.grain - costGrain,
            people = people,
            sites = sites,
            pendingReports = listOf("已安排${person.name}本月【${training.label}】。${training.desc}，月结时增长属性。")
        )
    }

    fun nextExamStage(person: V3Person): V3ExamStage? = when (person.examStage) {
        null -> V3ExamStage.County
        V3ExamStage.County -> V3ExamStage.Prefecture
        V3ExamStage.Prefecture -> V3ExamStage.Provincial
        V3ExamStage.Provincial -> null
    }

    fun canStartExam(state: V3GameState, person: V3Person): Boolean {
        val stage = nextExamStage(person) ?: return false
        val academy = state.sites.firstOrNull { it.type == V3CountySiteType.Academy }
        val requiredStudy = when (stage) {
            V3ExamStage.County -> 18
            V3ExamStage.Prefecture -> 36
            V3ExamStage.Provincial -> 58
        }
        return person.alive && person.age >= 12 && person.study >= requiredStudy && state.examSession == null && (academy?.level ?: 0) > 0
    }

    fun examQuestion(session: V3ExamSession): V3ExamQuestion? = V3Content.examQuestions.firstOrNull { it.id == session.questionId }

    fun startExam(state: V3GameState, personId: Int): V3GameState {
        val person = state.people.firstOrNull { it.id == personId && it.alive } ?: return state
        val stage = nextExamStage(person) ?: return state.copy(pendingReports = listOf("${person.name}已过乡试，暂不需要继续考试。"))
        val academy = state.sites.firstOrNull { it.type == V3CountySiteType.Academy }
        if ((academy?.level ?: 0) <= 0) return state.copy(pendingReports = listOf("需要先营建书院，才能送族人参加科举。"))
        if (!canStartExam(state, person)) {
            return state.copy(pendingReports = listOf("${person.name}暂不适合参加${stage.label}：需年龄12岁以上、学识达标，且当前没有正在进行的考试。"))
        }
        val costSilver = when (stage) {
            V3ExamStage.County -> 8
            V3ExamStage.Prefecture -> 18
            V3ExamStage.Provincial -> 36
        }
        if (state.silver < costSilver) return state.copy(pendingReports = listOf("参加${stage.label}需要银$costSilver，用于束脩、盘缠与打点。"))
        val pool = V3Content.examQuestions.filter { it.stage == stage }
        val question = pool[(person.id + state.year + state.month) % pool.size]
        return state.copy(
            silver = state.silver - costSilver,
            examSession = V3ExamSession(person.id, stage, question.id),
            pendingReports = listOf("${person.name}入场参加${stage.label}。学识越高，答错时也越可能凭底子补救。")
        )
    }

    fun answerExam(state: V3GameState, answerIndex: Int): V3GameState {
        val session = state.examSession ?: return state
        val question = examQuestion(session) ?: return state.copy(examSession = null)
        val person = state.people.firstOrNull { it.id == session.personId && it.alive } ?: return state.copy(examSession = null)
        val difficulty = when (session.stage) {
            V3ExamStage.County -> 34
            V3ExamStage.Prefecture -> 58
            V3ExamStage.Provincial -> 82
        }
        val examPower = person.study + person.diplomacy / 3 + traitExamBonus(person.trait)
        val answeredCorrectly = answerIndex == question.answerIndex
        val passed = answeredCorrectly || examPower >= difficulty
        val nextPeople = state.people.map {
            if (it.id == person.id) {
                if (passed) {
                    it.copy(
                        examStage = session.stage,
                        officeRank = session.stage.title,
                        merit = (it.merit + 8 + session.stage.ordinal * 5).coerceAtMost(999),
                        fatigue = (it.fatigue + 12).coerceIn(0, 100),
                        study = (it.study + 2).coerceAtMost(100),
                        diplomacy = (it.diplomacy + 1).coerceAtMost(100)
                    )
                } else {
                    it.copy(fatigue = (it.fatigue + 16).coerceIn(0, 100), study = (it.study + 1).coerceAtMost(100))
                }
            } else it
        }
        val routeGain = if (passed) 10 + session.stage.ordinal * 4 else 2
        val influenceGain = if (passed) 5 + session.stage.ordinal * 3 else 0
        val relationGain = if (passed) 4 + session.stage.ordinal * 2 else 0
        val result = if (passed) {
            val reason = if (answeredCorrectly) "答中考题" else "虽答偏一字，但学识底子扎实，阅卷官仍予录取"
            "${person.name}${reason}，通过${session.stage.label}，取得【${session.stage.title}】身份。${question.note}"
        } else {
            "${person.name}${session.stage.label}落第。正确答案是【${question.options[question.answerIndex]}】。${question.note}"
        }
        return state.copy(
            people = nextPeople,
            influence = (state.influence + influenceGain).coerceIn(0, 100),
            relations = state.relations.copy(gentry = clamp(state.relations.gentry + relationGain), yamen = clamp(state.relations.yamen + (if (passed) 2 else 0))),
            routeScores = state.routeScores + (V3Route.Scholar to ((state.routeScores[V3Route.Scholar] ?: 0) + routeGain)),
            examSession = null,
            pendingReports = listOf(result),
            eventLog = (listOf("${state.year}年${state.month}月 · $result") + state.eventLog).take(100)
        )
    }

    fun startBattle(state: V3GameState): V3GameState {
        if (state.battleState != null) return state
        if (state.militia < 15) return state.copy(pendingReports = listOf("乡勇不足15，不宜出兵。先在寨堡或山道募勇、筑寨。"))
        val riskySite = state.sites.maxByOrNull { it.risk } ?: return state
        val enemyPower = 35 + riskySite.risk + state.rebelHeat / 3
        val battle = V3BattleState(
            target = riskySite.name,
            enemyPower = enemyPower,
            rewardInfluence = 4 + riskySite.risk / 12,
            rewardSilver = 12 + riskySite.risk / 5,
            risk = if (enemyPower > state.militia + 40) "凶险" else "可战"
        )
        return state.copy(battleState = battle, pendingReports = listOf("已集结乡勇，准备讨伐【${battle.target}】。敌势${battle.enemyPower}，风险：${battle.risk}。"))
    }

    fun cancelBattle(state: V3GameState): V3GameState = state.copy(battleState = null, pendingReports = listOf("已暂缓出兵，乡勇回寨待命。"))

    fun resolveBattle(state: V3GameState): V3GameState {
        val battle = state.battleState ?: return state
        val bestWarrior = alivePeople(state).maxByOrNull { it.martial + it.merit / 4 }
        val fortLevel = state.sites.firstOrNull { it.type == V3CountySiteType.Fort }?.level ?: 0
        val power = state.militia + (bestWarrior?.martial ?: 0) + fortLevel * 12 + max(0, state.relations.garrison) / 2
        val victory = power >= battle.enemyPower
        val loss = if (victory) max(3, battle.enemyPower / 18) else max(8, battle.enemyPower / 10)
        val targetSite = state.sites.maxByOrNull { it.risk }
        val nextSites = state.sites.map { site ->
            if (site.id == targetSite?.id) {
                val risk = (site.risk - (if (victory) 24 else 8)).coerceAtLeast(0)
                val control = (site.control + (if (victory) 12 else 3)).coerceAtMost(100)
                site.copy(risk = risk, control = control, status = statusFor(control, risk))
            } else site
        }
        val nextPeople = state.people.map {
            if (it.id == bestWarrior?.id) {
                it.copy(
                    merit = (it.merit + (if (victory) 10 else 4)).coerceAtMost(999),
                    martial = (it.martial + (if (victory) 2 else 1)).coerceAtMost(100),
                    fatigue = (it.fatigue + (if (victory) 14 else 24)).coerceIn(0, 100),
                    militaryRank = if (victory && it.militaryRank == null) "乡勇头目" else it.militaryRank
                )
            } else it
        }
        val message = if (victory) {
            "讨伐【${battle.target}】得胜。${bestWarrior?.name ?: "族中勇丁"}立下军功，地点风险下降，割据与自保路线推进。"
        } else {
            "讨伐【${battle.target}】失利。乡勇折损较重，但摸清了敌势。建议先筑寨募勇再战。"
        }
        return state.copy(
            sites = nextSites,
            people = nextPeople,
            militia = (state.militia - loss).coerceAtLeast(0),
            silver = state.silver + (if (victory) battle.rewardSilver else 0),
            influence = (state.influence + (if (victory) battle.rewardInfluence else 1)).coerceIn(0, 100),
            relations = state.relations.copy(bandits = clamp(state.relations.bandits - (if (victory) 12 else 4)), garrison = clamp(state.relations.garrison + (if (victory) 3 else 0))),
            routeScores = state.routeScores + mapOf(
                V3Route.Fortress to ((state.routeScores[V3Route.Fortress] ?: 0) + (if (victory) 7 else 2)),
                V3Route.Warlord to ((state.routeScores[V3Route.Warlord] ?: 0) + (if (victory) 5 else 1))
            ),
            battleState = null,
            pendingReports = listOf(message),
            eventLog = (listOf("${state.year}年${state.month}月 · $message") + state.eventLog).take(100)
        )
    }

    fun raiseBanner(state: V3GameState): V3GameState {
        val controlled = state.sites.count { it.control >= 60 && it.risk <= 45 }
        val power = state.militia + state.influence + controlled * 18 + state.rebelHeat
        if (power < 140) {
            return state.copy(pendingReports = listOf("举旗条件不足：需乡勇、族望、稳定据点共同支撑。当前举旗评估 $power / 140。"))
        }
        val message = "李氏在县中举起义旗，接管粮仓与城门。官府关系大降，割据路线大幅推进，后续会引来官军与周边家族压力。"
        return state.copy(
            rebelHeat = (state.rebelHeat + 35).coerceAtMost(100),
            influence = (state.influence + 8).coerceIn(0, 100),
            cohesion = (state.cohesion - 6).coerceIn(0, 100),
            relations = state.relations.copy(yamen = clamp(state.relations.yamen - 25), garrison = clamp(state.relations.garrison - 8), villagers = clamp(state.relations.villagers + 4)),
            routeScores = state.routeScores + (V3Route.Warlord to ((state.routeScores[V3Route.Warlord] ?: 0) + 24)),
            pendingReports = listOf(message),
            eventLog = (listOf("${state.year}年${state.month}月 · $message") + state.eventLog).take(100)
        )
    }

    fun assignmentPreview(person: V3Person, site: V3CountySite, task: V3TaskType): String {
        val power = taskPower(person, task)
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
            V3CountySiteType.Farmland -> V3SiteYield(silver = scale(4), grain = scale(34), cohesion = scale(1), desc = "粮食主产，兼有租银")
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
        var taskSilverExpense = 0
        var taskGrainExpense = 0
        state.sites.forEach { site ->
            val yield = siteYield(site)
            silverIncome += yield.silver
            grainIncome += yield.grain
            influenceIncome += yield.influence
            cohesionIncome += yield.cohesion
            militiaIncome += yield.militia
        }
        state.estateAssets.forEach { asset ->
            val yield = estateYield(asset)
            silverIncome += yield.silver
            grainIncome += yield.grain
            influenceIncome += yield.influence
            cohesionIncome += yield.cohesion
            militiaIncome += yield.militia
        }
        state.worldRegions.filter { it.status == V3RegionStatus.Controlled || it.status == V3RegionStatus.Pacified }.forEach { region ->
            silverIncome += region.wealth / 12
            grainIncome += region.wealth / 18
            influenceIncome += region.tier
        }
        state.sites.forEach { site ->
            val person = site.assignedPersonId?.let { id -> state.people.firstOrNull { it.id == id } } ?: return@forEach
            val task = person.currentTask ?: return@forEach
            val power = taskPower(person, task)
            val silver = taskSilver(task, power)
            val grain = taskGrain(task, power)
            if (silver > 0) silverIncome += silver else taskSilverExpense += -silver
            if (grain > 0) grainIncome += grain else taskGrainExpense += -grain
        }
        val silverExpense = monthlySilverExpense(state) + taskSilverExpense
        val grainExpense = monthlyGrainExpense(state) + taskGrainExpense
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
        state.estateAssets.forEach { asset ->
            val yield = estateYield(asset)
            silverDelta += yield.silver
            grainDelta += yield.grain
            influenceDelta += yield.influence
            cohesionDelta += yield.cohesion
            militiaDelta += yield.militia
            incomeParts += "${asset.type.label}${yieldText(yield)}"
        }
        state.worldRegions.filter { it.status == V3RegionStatus.Controlled || it.status == V3RegionStatus.Pacified }.forEach { region ->
            val silver = region.wealth / 12
            val grain = region.wealth / 18
            silverDelta += silver
            grainDelta += grain
            influenceDelta += region.tier
            incomeParts += "${region.name}银+$silver/粮+$grain/望+${region.tier}"
        }

        val sites = state.sites.map { site ->
            val assigned = site.assignedPersonId?.let { id -> state.people.firstOrNull { it.id == id && it.alive } }
            if (assigned == null) {
                val riskRise = if (site.level > 0 || site.risk >= 50) 2 else 1
                val risk = (site.risk + riskRise).coerceAtMost(95)
                site.copy(risk = risk, status = statusFor(site.control, risk))
            } else {
                val task = assigned.currentTask ?: V3TaskType.Govern
                val power = taskPower(assigned, task)
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
        V3GoalMetric.EstateLevel -> estateLevelTotal(state)
        V3GoalMetric.ControlledRegions -> controlledRegionCount(state)
        V3GoalMetric.Unification -> state.unificationProgress
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
        val score = routeScore + stableSites * 4 + resourceScore + familyScore + relationScore + state.cohesion / 5 + state.influence / 4 + estateLevelTotal(state) * 3 + controlledRegionCount(state) * 12 + state.unificationProgress
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

    fun shouldAutoEnd(state: V3GameState): Boolean = state.year > 1644 || (state.year == 1644 && state.month >= 5) || state.cohesion <= 0 || state.silver <= -300 || state.grain <= -300

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
                "家产总等级：${estateLevelTotal(state)}",
                "控制地域：${controlledRegionCount(state)} / ${state.worldRegions.size}",
                "统一进度：${state.unificationProgress}",
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
                val training = person.trainingFocus
                if (training == null) {
                    person.copy(age = baseAge, fatigue = (person.fatigue - 8).coerceAtLeast(0), currentTask = null, assignedSiteId = null)
                } else {
                    val grow = trainingGrowth(training, person.trait, person.age)
                    val next = person.copy(
                        age = baseAge,
                        study = (person.study + grow.study).coerceAtMost(100),
                        martial = (person.martial + grow.martial).coerceAtMost(100),
                        commerce = (person.commerce + grow.commerce).coerceAtMost(100),
                        diplomacy = (person.diplomacy + grow.diplomacy).coerceAtMost(100),
                        loyalty = (person.loyalty + grow.loyalty).coerceIn(0, 100),
                        merit = (person.merit + grow.merit).coerceAtMost(999),
                        fatigue = (person.fatigue + grow.fatigue).coerceIn(0, 100),
                        trainingFocus = null,
                        currentTask = null,
                        assignedSiteId = null
                    )
                    lines += "${person.name}完成${training.label}，${trainingResultText(grow)}。"
                    next
                }
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
                    trainingFocus = null,
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
                loyalty = (branch.loyalty + (if (active > 0) 1 else 0)).coerceIn(0, 100),
                wealth = (branch.wealth + wealthShift).coerceIn(0, 100),
                influence = (branch.influence + (if (active > 0) 1 else 0)).coerceIn(0, 100),
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

    private fun trainingGrowth(training: V3TrainingType, trait: V3Trait, age: Int): GrowthDelta {
        val childBonus = if (age < 12) 1 else 0
        val base = when (training) {
            V3TrainingType.Enlighten -> GrowthDelta(study = 2 + childBonus, diplomacy = 1, fatigue = 3)
            V3TrainingType.MartialDrill -> GrowthDelta(martial = 2 + childBonus, loyalty = 1, fatigue = 5)
            V3TrainingType.Abacus -> GrowthDelta(commerce = 2 + childBonus, study = 1, fatigue = 4)
            V3TrainingType.Etiquette -> GrowthDelta(diplomacy = 2 + childBonus, study = 1, fatigue = 3)
        }
        return when (trait) {
            V3Trait.Studious -> if (training == V3TrainingType.Enlighten) base.copy(study = base.study + 1) else base
            V3Trait.Martial, V3Trait.Fierce -> if (training == V3TrainingType.MartialDrill) base.copy(martial = base.martial + 1) else base
            V3Trait.Greedy -> if (training == V3TrainingType.Abacus) base.copy(commerce = base.commerce + 1) else base
            V3Trait.Smooth, V3Trait.Cunning -> if (training == V3TrainingType.Etiquette) base.copy(diplomacy = base.diplomacy + 1) else base
            else -> base
        }
    }

    private fun trainingResultText(grow: GrowthDelta): String {
        val parts = mutableListOf<String>()
        if (grow.study > 0) parts += "学+${grow.study}"
        if (grow.martial > 0) parts += "武+${grow.martial}"
        if (grow.commerce > 0) parts += "商+${grow.commerce}"
        if (grow.diplomacy > 0) parts += "谋+${grow.diplomacy}"
        if (grow.loyalty > 0) parts += "忠+${grow.loyalty}"
        if (grow.fatigue > 0) parts += "劳+${grow.fatigue}"
        return parts.joinToString("，")
    }

    private fun traitTaskBonus(trait: V3Trait, task: V3TaskType): Int = when (trait) {
        V3Trait.Studious -> if (task == V3TaskType.Study) 8 else 0
        V3Trait.Martial -> if (task == V3TaskType.Fortify || task == V3TaskType.Recruit || task == V3TaskType.Scout) 8 else 0
        V3Trait.Fierce -> if (task == V3TaskType.Fortify || task == V3TaskType.Recruit) 6 else if (task == V3TaskType.Diplomacy) -4 else 0
        V3Trait.Greedy -> if (task == V3TaskType.Trade || task == V3TaskType.Farm) 6 else 0
        V3Trait.Benevolent -> if (task == V3TaskType.Relief || task == V3TaskType.Govern) 7 else 0
        V3Trait.Cunning -> if (task == V3TaskType.Scout || task == V3TaskType.Diplomacy || task == V3TaskType.Trade) 6 else 0
        V3Trait.Smooth -> if (task == V3TaskType.Diplomacy || task == V3TaskType.Govern) 7 else 0
        V3Trait.Timid -> if (task == V3TaskType.Scout || task == V3TaskType.Recruit) -5 else 2
        V3Trait.Ambitious -> if (task == V3TaskType.Study || task == V3TaskType.Diplomacy || task == V3TaskType.Recruit) 4 else 0
        V3Trait.Honest -> if (task == V3TaskType.Govern || task == V3TaskType.Farm) 4 else 0
    }

    private fun traitExamBonus(trait: V3Trait): Int = when (trait) {
        V3Trait.Studious -> 10
        V3Trait.Smooth -> 4
        V3Trait.Ambitious -> 3
        V3Trait.Timid -> -3
        else -> 0
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
            estateLevelTotal(state) < 5 -> pool.firstOrNull { it.id == "estate_5" }
            controlledRegionCount(state) < 2 && state.clanRank >= 3 -> pool.firstOrNull { it.id == "region_2" }
            state.unificationProgress < 30 && controlledRegionCount(state) >= 2 -> pool.firstOrNull { it.id == "unify_30" }
            !canRankUp(state) -> pool.firstOrNull { it.id == "rank_2" }
            else -> pool.firstOrNull { it.route == route } ?: pool.firstOrNull()
        }
    }

    private fun taskPower(person: V3Person, task: V3TaskType): Int {
        val base = when (task) {
            V3TaskType.Govern -> (person.study + person.diplomacy) / 2
            V3TaskType.Farm -> (person.study + person.commerce) / 2
            V3TaskType.Trade -> person.commerce
            V3TaskType.Study -> person.study
            V3TaskType.Diplomacy -> person.diplomacy
            V3TaskType.Relief -> (person.study + person.diplomacy) / 2
            V3TaskType.Fortify -> person.martial
            V3TaskType.Scout -> (person.martial + person.diplomacy) / 2
            V3TaskType.Recruit -> person.martial
        }
        return (base + traitTaskBonus(person.trait, task) + person.merit / 20 - person.fatigue / 12).coerceAtLeast(1)
    }

    private fun taskSilver(task: V3TaskType, power: Int): Int = when (task) {
        V3TaskType.Trade -> max(8, power / 3)
        V3TaskType.Diplomacy -> -3
        V3TaskType.Farm -> max(2, power / 10)
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

    private fun monthlySilverExpense(state: V3GameState): Int = 2 + state.clanRank + state.militia / 50

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
        if (yield.silver != 0) parts += "银${signed(yield.silver)}"
        if (yield.grain != 0) parts += "粮${signed(yield.grain)}"
        if (yield.influence != 0) parts += "望${signed(yield.influence)}"
        if (yield.cohesion != 0) parts += "凝${signed(yield.cohesion)}"
        if (yield.militia != 0) parts += "勇${signed(yield.militia)}"
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

    private fun calculateUnification(regions: List<com.daming.fushengzhi3.v3.data.V3WorldRegion>): Int {
        val score = regions.sumOf { region ->
            val base = when (region.status) {
                V3RegionStatus.Unknown -> 0
                V3RegionStatus.Contacted -> region.control / 10
                V3RegionStatus.Influenced -> 6 + region.control / 8
                V3RegionStatus.Controlled -> 12 + region.tier * 8
                V3RegionStatus.Pacified -> 16 + region.tier * 9
            }
            base
        }
        return score.coerceIn(0, 100)
    }

    private fun clamp(value: Int): Int = min(100, max(-100, value))

    private val boyNames = listOf("守成", "承祖", "怀远", "景行", "念安", "修齐", "启明", "存义")
    private val girlNames = listOf("采薇", "若兰", "念慈", "静姝", "安禾", "清婉", "素心", "云娘")
}
