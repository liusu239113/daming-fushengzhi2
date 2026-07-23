package com.arktools.daming.v3.data

import kotlinx.serialization.Serializable

const val V3_SAVE_VERSION = 2
const val V3_TUTORIAL_VERSION = 3

@Serializable
enum class V3Screen { County, Clan, People, Strategy }

@Serializable
enum class V3Gender(val label: String) { Male("男"), Female("女") }

@Serializable
enum class V3CountySiteType(val label: String) {
    Shrine("祠堂"),
    Farmland("田庄"),
    Market("集市"),
    Yamen("县衙"),
    Academy("书院"),
    Clinic("医馆"),
    Fort("寨堡"),
    Dock("码头"),
    MountainPass("山道")
}

@Serializable
enum class V3SiteStatus(val label: String) {
    Stable("平稳"),
    Prosperous("繁荣"),
    Strained("紧张"),
    Threatened("受威胁"),
    Blighted("荒废")
}

@Serializable
enum class V3Trait(val label: String, val desc: String) {
    Honest("忠厚", "族内威望更稳，但商战手腕不足"),
    Greedy("贪财", "经商收益提高，但可能引发族内不满"),
    Studious("好学", "读书和科举任务更稳"),
    Martial("尚武", "军务与防御任务更强，但冲突概率提高"),
    Timid("怯懦", "风险任务较弱，但更少惹祸"),
    Ambitious("野心", "仕途成长更快，但房支争权风险提高"),
    Benevolent("仁善", "赈济和民心任务更强，但资源消耗更大"),
    Cunning("狡黠", "斡旋和暗线任务更强"),
    Fierce("刚烈", "抗压和战斗更强，但外交更差"),
    Smooth("圆滑", "官府和士绅周旋更强")
}

@Serializable
enum class V3TaskType(val label: String) {
    Govern("治理"),
    Farm("管田"),
    Trade("行商"),
    Study("读书"),
    Diplomacy("拜访"),
    Relief("赈济"),
    Fortify("筑寨"),
    Scout("刺探"),
    Recruit("募勇")
}

@Serializable
enum class V3TrainingType(val label: String, val desc: String) {
    Enlighten("启蒙读书", "提升学识，适合儿童和科举路线"),
    MartialDrill("习武演练", "提升武艺，适合从军与防务路线"),
    Abacus("账房学算", "提升经商，适合产业和商路路线"),
    Etiquette("礼法应对", "提升谋略，适合官府士绅周旋")
}

@Serializable
enum class V3ExamStage(val label: String, val title: String) {
    County("县试", "童生"),
    Prefecture("府试", "秀才"),
    Provincial("乡试", "举人")
}

@Serializable
data class V3ExamQuestion(
    val id: String,
    val stage: V3ExamStage,
    val question: String,
    val options: List<String>,
    val answerIndex: Int,
    val note: String
)

@Serializable
data class V3ExamSession(
    val personId: Int,
    val stage: V3ExamStage,
    val questionId: String
)

@Serializable
enum class V3TroopType(val label: String, val desc: String, val silverCost: Int, val grainCost: Int, val power: Int) {
    Militia("乡勇", "便宜耐用，前期守寨讨寇主力", 5, 8, 8),
    Spear("枪兵", "克制骑冲，适合稳阵", 10, 14, 14),
    Archer("弓手", "先手消耗，适合压低敌方血线", 12, 10, 13),
    Shield("盾手", "护住文官与弓手，降低伤损", 14, 16, 15),
    Cavalry("骑兵", "后期冲阵强，但费用高", 28, 22, 28)
}

@Serializable
data class V3ArmyRoster(
    val militia: Int = 0,
    val spear: Int = 0,
    val archer: Int = 0,
    val shield: Int = 0,
    val cavalry: Int = 0
) {
    fun count(type: V3TroopType): Int = when (type) {
        V3TroopType.Militia -> militia
        V3TroopType.Spear -> spear
        V3TroopType.Archer -> archer
        V3TroopType.Shield -> shield
        V3TroopType.Cavalry -> cavalry
    }

    fun add(type: V3TroopType, amount: Int): V3ArmyRoster {
        val accepted = amount.coerceAtLeast(0).coerceAtMost((999 - total()).coerceAtLeast(0))
        return when (type) {
            V3TroopType.Militia -> copy(militia = militia + accepted)
            V3TroopType.Spear -> copy(spear = spear + accepted)
            V3TroopType.Archer -> copy(archer = archer + accepted)
            V3TroopType.Shield -> copy(shield = shield + accepted)
            V3TroopType.Cavalry -> copy(cavalry = cavalry + accepted)
        }
    }

    fun total(): Int = militia + spear + archer + shield + cavalry

    fun lose(amount: Int): V3ArmyRoster {
        var remaining = amount.coerceAtLeast(0)
        fun take(current: Int): Int {
            val lost = minOf(current, remaining)
            remaining -= lost
            return current - lost
        }
        val nextMilitia = take(militia)
        val nextSpear = take(spear)
        val nextArcher = take(archer)
        val nextShield = take(shield)
        val nextCavalry = take(cavalry)
        return copy(militia = nextMilitia, spear = nextSpear, archer = nextArcher, shield = nextShield, cavalry = nextCavalry)
    }

    /** Deduct troops from the militia pool only; never touches professional troops. */
    fun loseMilitia(amount: Int): V3ArmyRoster {
        val loss = amount.coerceAtLeast(0).coerceAtMost(militia)
        return copy(militia = militia - loss)
    }

    fun battlePower(): Int = militia * V3TroopType.Militia.power + spear * V3TroopType.Spear.power + archer * V3TroopType.Archer.power + shield * V3TroopType.Shield.power + cavalry * V3TroopType.Cavalry.power
}

@Serializable
data class V3Combatant(
    val name: String,
    val hp: Int,
    val maxHp: Int,
    val power: Int,
    val role: String,
    val defense: Int = 0,
    val personId: Int? = null,
    val troopType: V3TroopType? = null,
    val troopCount: Int = 0
)

@Serializable
data class V3BattleRound(
    val attacker: String,
    val defender: String,
    val damage: Int,
    val text: String
)

@Serializable
enum class V3BattlePhase { Draft, Fighting, Finished }

@Serializable
data class V3BattleState(
    val target: String,
    val enemyPower: Int,
    val rewardInfluence: Int,
    val rewardSilver: Int,
    val risk: String,
    val phase: V3BattlePhase = V3BattlePhase.Draft,
    val selectedPersonIds: List<Int> = emptyList(),
    val selectedTroops: Map<Int, V3TroopType> = emptyMap(),
    val allies: List<V3Combatant> = emptyList(),
    val enemies: List<V3Combatant> = emptyList(),
    val roundLog: List<V3BattleRound> = emptyList(),
    val turn: Int = 0,
    val finished: Boolean = false,
    val victory: Boolean = false
)

@Serializable
enum class V3Route(val label: String) {
    Scholar("耕读传家"),
    Merchant("富甲江南"),
    Fortress("聚族自保"),
    Loyalist("从龙勤王"),
    Warlord("割据一方"),
    Overseas("远渡海外"),
    Hermit("隐世避祸")
}

@Serializable
enum class V3EstateType(val label: String, val desc: String) {
    TenantLand("佃田", "稳定产粮，是一户起家的根基"),
    Shop("铺面", "每月产银，支撑婚配、打点和扩张"),
    Workshop("作坊", "产银较高，但需要人口和集市"),
    Warehouse("粮仓", "提高存粮与灾荒承受力"),
    Caravan("商队", "联通集市和码头，提升商路收益"),
    Barracks("团练营", "持续训练乡勇，是征伐和造反根基")
}

@Serializable
data class V3EstateAsset(
    val id: String,
    val type: V3EstateType,
    val level: Int,
    val workers: Int = 0,
    val desc: String = ""
)

@Serializable
enum class V3RegionStatus(val label: String) {
    Unknown("未涉足"),
    Contacted("已结交"),
    Influenced("有声望"),
    Controlled("已控制"),
    Pacified("已归附")
}

@Serializable
data class V3WorldRegion(
    val id: String,
    val name: String,
    val tier: Int,
    val status: V3RegionStatus,
    val control: Int,
    val enemyPower: Int,
    val wealth: Int,
    val desc: String,
    val accordRoute: V3Route? = null
)

@Serializable
data class V3ConquestState(
    val regionId: String,
    val enemyPower: Int,
    val rewardSilver: Int,
    val rewardGrain: Int,
    val rewardInfluence: Int,
    val targetName: String,
    val scale: String
)

@Serializable
enum class V3GoalMetric(val label: String) {
    SilverStock("银两储备"),
    GrainStock("粮食储备"),
    Militia("乡勇规模"),
    Cohesion("宗族凝聚"),
    Influence("族望声名"),
    ControlledSites("控制地点"),
    SafeSites("安定地点"),
    BuiltSites("已建产业"),
    EstateLevel("家产等级"),
    ControlledRegions("控制地域"),
    Unification("统一进度"),
    Population("家族人口"),
    ClanRank("宗族品第"),
    RouteScore("路线倾向"),
    RelationTotal("地方关系")
}

@Serializable
enum class V3EndingTier(val label: String) {
    Fragile("根基未稳"),
    Viable("可成一线"),
    Strong("大势已成"),
    Historic("足载家乘")
}

@Serializable
data class V3AnnualGoal(
    val id: String,
    val title: String,
    val desc: String,
    val metric: V3GoalMetric,
    val target: Int,
    val route: V3Route,
    val rewardSilver: Int = 0,
    val rewardGrain: Int = 0,
    val rewardInfluence: Int = 0,
    val rewardCohesion: Int = 0,
    val completed: Boolean = false
)

@Serializable
enum class V3EquipmentSlot(val label: String) { Weapon("武器"), Armor("甲胄"), Mount("坐骑"), Shield("盾牌") }

@Serializable
enum class V3EquipmentQuality(val label: String, val multiplier: Int) { Common("凡品", 1), Fine("良造", 2), Masterwork("名工", 3) }

@Serializable
data class V3EquipmentItem(
    val id: String,
    val name: String,
    val slot: V3EquipmentSlot,
    val quality: V3EquipmentQuality,
    val attack: Int,
    val defense: Int,
    val price: Int,
    val ownerId: Int? = null,
    val durability: Int = 100,
    val maxDurability: Int = 100
)

@Serializable
data class V3SpouseCandidate(
    val id: String,
    val name: String,
    val desc: String,
    val silverCost: Int,
    val grainCost: Int,
    val influenceReq: Int,
    val studyBonus: Int = 0,
    val martialBonus: Int = 0,
    val commerceBonus: Int = 0,
    val diplomacyBonus: Int = 0,
    val route: V3Route,
    val gender: V3Gender = V3Gender.Female,
    val age: Int = 19,
    val avatarKey: String = "female_youth",
    val surname: String = "",
    val prototypeId: String = ""
)

@Serializable
data class V3UpgradeCost(
    val silver: Int,
    val grain: Int,
    val desc: String
)

@Serializable
data class V3RankCost(
    val silver: Int,
    val grain: Int,
    val population: Int,
    val builtSites: Int,
    val influence: Int,
    val title: String
)

@Serializable
data class V3SiteYield(
    val silver: Int = 0,
    val grain: Int = 0,
    val influence: Int = 0,
    val cohesion: Int = 0,
    val militia: Int = 0,
    val desc: String = ""
)

@Serializable
data class V3MonthlyForecast(
    val silverIncome: Int = 0,
    val grainIncome: Int = 0,
    val influenceIncome: Int = 0,
    val cohesionIncome: Int = 0,
    val militiaIncome: Int = 0,
    val silverExpense: Int = 0,
    val grainExpense: Int = 0,
    val dangerSites: Int = 0,
    val summary: String = ""
)

@Serializable
data class V3CountySite(
    val id: String,
    val name: String,
    val type: V3CountySiteType,
    val level: Int,
    val control: Int,
    val risk: Int,
    val status: V3SiteStatus,
    val desc: String,
    val taskTypes: List<V3TaskType>,
    val assignedPersonId: Int? = null
)

@Serializable
data class V3Person(
    val id: Int,
    val name: String,
    val age: Int,
    val branch: String,
    val identity: String,
    val trait: V3Trait,
    val study: Int,
    val martial: Int,
    val commerce: Int,
    val diplomacy: Int,
    val loyalty: Int,
    val gender: V3Gender = V3Gender.Male,
    val generation: Int = 1,
    val spouseId: Int? = null,
    val parentId: Int? = null,
    val motherId: Int? = null,
    val childrenIds: List<Int> = emptyList(),
    val alive: Boolean = true,
    val merit: Int = 0,
    val officeRank: String? = null,
    val militaryRank: String? = null,
    val examStage: V3ExamStage? = null,
    val trainingFocus: V3TrainingType? = null,
    val fatigue: Int = 0,
    val currentTask: V3TaskType? = null,
    val assignedSiteId: String? = null,
    val spouseSinceMonth: Int? = null,
    val lastBirthMonth: Int? = null,
    val ageMonths: Int = -1,
    val surname: String = "",
    val spouseCandidateId: String? = null,
    val pregnancyDueMonth: Int? = null,
    val illness: String? = null,
    val illnessMonths: Int = 0,
    val deathYear: Int? = null,
    val deathMonth: Int? = null,
    val deathCause: String? = null
)

@Serializable
data class V3Branch(
    val id: String,
    val name: String,
    val leaderName: String,
    val focus: V3Route,
    val loyalty: Int,
    val wealth: Int,
    val influence: Int,
    val grievance: Int,
    val desc: String
)

@Serializable
data class V3RoutePlan(
    val route: V3Route,
    val goal: String,
    val coreStats: List<String>,
    val unlockHint: String,
    val endingName: String
)

@Serializable
data class V3EventSeed(
    val title: String,
    val trigger: String,
    val choices: List<String>,
    val route: V3Route
)

@Serializable
data class V3TaskPlan(
    val task: V3TaskType,
    val primaryStat: String,
    val effect: String,
    val risk: String,
    val route: V3Route
)

@Serializable
data class V3ActiveEvent(
    val title: String,
    val body: String,
    val choices: List<V3EventChoice>
)

@Serializable
data class V3EventChoice(
    val label: String,
    val desc: String,
    val silverDelta: Int = 0,
    val grainDelta: Int = 0,
    val cohesionDelta: Int = 0,
    val influenceDelta: Int = 0,
    val militiaDelta: Int = 0,
    val yamenDelta: Int = 0,
    val gentryDelta: Int = 0,
    val villagersDelta: Int = 0,
    val banditsDelta: Int = 0,
    val merchantsDelta: Int = 0,
    val garrisonDelta: Int = 0,
    val siteId: String? = null,
    val siteControlDelta: Int = 0,
    val siteRiskDelta: Int = 0,
    val personId: Int? = null,
    val personFatigueDelta: Int = 0,
    val personMeritDelta: Int = 0,
    val personLoyaltyDelta: Int = 0,
    val route: V3Route? = null,
    val routeDelta: Int = 0,
    val branchImpacts: List<V3BranchImpact> = emptyList(),
    val storyFlag: String? = null,
    val removeFlag: String? = null,
    val createBranch: V3Branch? = null,
    val regionId: String? = null,
    val regionControlDelta: Int = 0,
    val regionStatus: V3RegionStatus? = null,
    val refugeesDelta: Int = 0,
    val garrisonMoraleDelta: Int = 0,
    val unrestDelta: Int = 0,
    val rebelHeatDelta: Int = 0,
    val patriarchConductDelta: Int = 0,
    val patriarchStewardshipDelta: Int = 0,
    val patriarchPrestigeDelta: Int = 0,
    val patriarchHealthDelta: Int = 0,
    val plaqueId: String? = null,
    val itemId: String? = null,
    val biographicalNote: String? = null,
    val visitorId: String? = null,
    val visitorProgressDelta: Int? = null,
    val followUpEventId: String? = null
)

@Serializable
data class V3BranchImpact(
    val branchId: String,
    val loyaltyDelta: Int = 0,
    val wealthDelta: Int = 0,
    val influenceDelta: Int = 0,
    val grievanceDelta: Int = 0,
    val note: String = ""
)

@Serializable
enum class V3RelationBand(val label: String) {
    Hostile("敌对"),
    Estranged("交恶"),
    Distant("冷淡"),
    Friendly("友善"),
    Close("深交"),
    Allied("同盟")
}

@Serializable
data class V3Relations(
    val yamen: Int = 0,
    val gentry: Int = 0,
    val villagers: Int = 0,
    val bandits: Int = -20,
    val merchants: Int = 5,
    val garrison: Int = 0
)

@Serializable
data class V3SaveEnvelope(
    val version: Int = V3_SAVE_VERSION,
    val timestamp: Long = System.currentTimeMillis(),
    val state: V3GameState
)

@Serializable
data class V3MonthlyReport(
    val title: String,
    val lines: List<String>,
    val nextState: V3GameState,
    val conclusion: String = "",
    val resourceLines: List<String> = emptyList(),
    val assignmentLines: List<String> = emptyList(),
    val goalLines: List<String> = emptyList(),
    val alertLines: List<String> = emptyList(),
    val nextActionTitle: String = "",
    val nextActionReason: String = "",
    val nextActionLabel: String = "",
    val nextActionDestination: V3Screen = V3Screen.County
)

@Serializable
data class V3EndingPreview(
    val route: V3Route,
    val tier: V3EndingTier,
    val score: Int,
    val title: String,
    val desc: String
)

@Serializable
data class V3FinalEnding(
    val route: V3Route,
    val tier: V3EndingTier,
    val score: Int,
    val title: String,
    val body: String,
    val stats: List<String>,
    val failureKind: String? = null
)

// ============================================================
// P0+P1+P2 融合扩展：族长、月度卡牌、访客、物品、骰子、六角守庄战
// ============================================================

/** 族长四围，参考崇祯制品的 personal stats，但改为族老视角：处世/经营/威望/身板。0-100，任一到 100 触发封翁匾。 */
@Serializable
data class V3Patriarch(
    val personId: Int = 1,
    val name: String = "",                        // LOW #19: 由 newGame() 填充 founderName，避免硬编码
    val generation: Int = 1,
    val conduct: Int = 30,   // 处世 — 应对官绅乡邻的手腕
    val stewardship: Int = 28, // 经营 — 田庄银粮调度
    val prestige: Int = 22,   // 威望 — 族内与乡间声望
    val health: Int = 55,     // 身板 — 健康/寿元
    val capstones: List<String> = emptyList(), // 已解锁封翁匾 id
    val term: Int = 0         // 继任以来的月份
)

@Serializable
enum class V3CardPool { Clan, Trade, Estate, Field, Rumor, Crisis, Visitor, Chain, Exam, Annual }

/** 卡牌选项的需求锁定：visible=false 时未满足条件的选项直接隐藏，否则显示为灰色不可点。 */
@Serializable
data class V3RouteRequirement(val route: V3Route, val score: Int)

@Serializable
data class V3RouteDelta(val route: V3Route, val delta: Int)

@Serializable
data class V3CardRequire(
    val minSilver: Int? = null,
    val minGrain: Int? = null,
    val minInfluence: Int? = null,
    val minCohesion: Int? = null,
    val minMilitia: Int? = null,
    val minRelationYamen: Int? = null,
    val minRelationGentry: Int? = null,
    val minRelationVillagers: Int? = null,
    val minRelationMerchants: Int? = null,
    val minRelationBandits: Int? = null,
    val minRelationGarrison: Int? = null,
    val minRouteScore: V3RouteRequirement? = null,
    val minClanRank: Int? = null,
    val minChapter: Int? = null,
    val flagRequired: String? = null,
    val flagBlocked: String? = null,
    val minPatriarchStat: String? = null, // conduct/stewardship/prestige/health
    val minPatriarchStatValue: Int? = null,
    // 人丁前提：配偶/子女/支脉
    val hasSpouse: Boolean? = null,        // 族长已娶
    val minChildren: Int? = null,          // 族中未成年子女数（<16岁）
    val minAliveAdults: Int? = null,       // 活着的成年族人（≥16岁）
    val minAlivePeople: Int? = null,       // 活着的总人数
    val requiredBranch: String? = null,    // 需已有此支脉（scholar/martial/merchant/sea/second）
    val minBranchCount: Int? = null,       // 已有支脉数
    val minPatriarchAge: Int? = null       // 族长最低年龄
) {
    fun label(): String? {
        val parts = mutableListOf<String>()
        if (minSilver != null) parts += "银两≥$minSilver"
        if (minGrain != null) parts += "粮食≥$minGrain"
        if (minInfluence != null) parts += "族望≥$minInfluence"
        if (minCohesion != null) parts += "凝聚≥$minCohesion"
        if (minMilitia != null) parts += "乡勇≥$minMilitia"
        if (minRelationYamen != null) parts += "官府≥$minRelationYamen"
        if (minRelationGentry != null) parts += "士绅≥$minRelationGentry"
        if (minRelationVillagers != null) parts += "乡民≥$minRelationVillagers"
        if (minRelationMerchants != null) parts += "商帮≥$minRelationMerchants"
        if (minRelationGarrison != null) parts += "军镇≥$minRelationGarrison"
        if (minRouteScore != null) parts += "${minRouteScore.route.label}≥${minRouteScore.score}"
        if (minClanRank != null) parts += "品第≥$minClanRank"
        if (minChapter != null) parts += "第${minChapter}章后"
        if (hasSpouse == true) parts += "已娶妻室"
        if (minChildren != null) parts += "子嗣≥${minChildren}"
        if (minAliveAdults != null) parts += "成丁≥${minAliveAdults}"
        if (minAlivePeople != null) parts += "族人≥${minAlivePeople}"
        if (requiredBranch != null) parts += "需立${requiredBranch}脉"
        if (minBranchCount != null) parts += "支脉≥${minBranchCount}"
        if (minPatriarchAge != null) parts += "家主≥${minPatriarchAge}岁"
        if (flagRequired != null) parts += "需$flagRequired"
        if (flagBlocked != null) parts += "未$flagBlocked"
        if (minPatriarchStat != null && minPatriarchStatValue != null) parts += "${minPatriarchStat}≥$minPatriarchStatValue"
        return if (parts.isEmpty()) null else parts.joinToString(" · ")
    }
}

/** 通用效果增量映射。空字段不修改。 */
@Serializable
data class V3EffectDelta(
    val silver: Int = 0,
    val grain: Int = 0,
    val influence: Int = 0,
    val cohesion: Int = 0,
    val militia: Int = 0,
    val refugees: Int = 0,
    val garrisonMorale: Int = 0,
    val unrest: Int = 0,
    val yamen: Int = 0,
    val gentry: Int = 0,
    val villagers: Int = 0,
    val bandits: Int = 0,
    val merchants: Int = 0,
    val garrison: Int = 0,
    val patriarchConduct: Int = 0,
    val patriarchStewardship: Int = 0,
    val patriarchPrestige: Int = 0,
    val patriarchHealth: Int = 0,
    val rebelHeat: Int = 0,
    val storyFlag: String? = null,
    val removeFlag: String? = null,
    val plaqueId: String? = null,
    val itemId: String? = null,
    val biographicalNote: String? = null,
    val visitorId: String? = null,
    val visitorProgress: Int? = null,
    val routeDelta: V3RouteDelta? = null
)

@Serializable
data class V3CardChoice(
    val id: String,
    val label: String,
    val desc: String = "",
    val require: V3CardRequire? = null,
    val hiddenIfLocked: Boolean = false,
    val dice: Boolean = false,                 // 吉凶判定
    val successRate: Int = 60,                 // 成功率 0-100
    val successText: String = "事遂人意",
    val failureText: String = "事与愿违",
    val effects: V3EffectDelta = V3EffectDelta(),
    val successEffects: V3EffectDelta? = null, // 骰子成功时额外
    val failureEffects: V3EffectDelta? = null, // 骰子失败时额外
    val nextCardId: String? = null             // 链状后续卡
)

@Serializable
data class V3MonthlyCard(
    val id: String,
    val pool: V3CardPool,
    val title: String,
    val body: String,
    val tag: String = "",                 // 右上角标签：族务/商旅/内宅/乡野/风闻/讼案/危局/访客
    val weight: Int = 10,
    val once: Boolean = false,            // 本局只出现一次
    val oncePerGeneration: Boolean = false,
    val choices: List<V3CardChoice>,
    val minChapter: Int = 1,
    val maxChapter: Int = 6,
    val crisisLevel: Int = 0, // 1粮荒、2民怨、3庄乱；0表示普通卡
    val require: V3CardRequire? = null // 卡级前置条件：不满足则整卡不出现
)

@Serializable
data class V3Visitor(
    val id: String,
    val name: String,
    val title: String,
    val portraitTag: String = "scholar",
    val chapters: List<V3VisitorChapter>
)

@Serializable
data class V3VisitorChapter(
    val chapter: Int,           // 1..6, 家族阶段
    val title: String,
    val body: String,
    val giftItemId: String? = null,
    val effects: V3EffectDelta = V3EffectDelta()
)

@Serializable
data class V3Item(
    val id: String,
    val name: String,
    val kind: String,    // book/weapon/deed/relic/western
    val desc: String,
    val effects: V3EffectDelta = V3EffectDelta(),
    val recurring: Boolean = false
)

@Serializable
data class V3DiceRoll(
    val cardId: String,
    val choiceId: String,
    val successRate: Int,
    val roll: Int            // 0..99
) {
    val success: Boolean get() = roll < successRate
    val verdict: String get() = if (success) "吉" else "凶"
}

// ---- 六角守庄战（第六章）：参考崇祯制品 6 阵位剪刀石头布，简化为 3 兵种相克 ----

@Serializable
enum class V3HexArms(val label: String) {
    Spear("枪阵"), Archer("弓矢"), Cavalry("骑突");
    infix fun counters(other: V3HexArms): Boolean = when (this) {
        Spear -> other == Cavalry
        Archer -> other == Spear
        Cavalry -> other == Archer
    }
}

@Serializable
data class V3HexTile(
    val q: Int,
    val r: Int,
    val name: String = "",
    val garrison: Int = 0,        // 我方驻守兵
    val arms: V3HexArms = V3HexArms.Spear,
    val enemyWave: Int = 0,       // 敌军冲击数
    val stable: Boolean = true,
    val breached: Boolean = false
)

@Serializable
data class V3HexBattleState(
    val turn: Int = 1,
    val maxTurns: Int = 6,
    val tiles: List<V3HexTile> = emptyList(),
    val supply: Int = 80,
    val enemyMomentum: Int = 40,
    val selectedArms: Map<String, V3HexArms> = emptyMap(), // tile key "$q,$r"
    val log: List<String> = emptyList(),
    val finished: Boolean = false,
    val victory: Boolean = false
) {
    companion object {
        fun initial(): V3HexBattleState {
            // 六个庄门点位，围绕祖祠呈环形
            val base = listOf(
                Triple(-1, 0, "西寨门"), Triple(0, -1, "北寨门"), Triple(1, -1, "东寨门"),
                Triple(1, 0, "东南田道"), Triple(0, 1, "南塘埂"), Triple(-1, 1, "西麓哨卡")
            )
            return V3HexBattleState(
                tiles = base.mapIndexed { index, (q, r, n) ->
                    V3HexTile(
                        q = q,
                        r = r,
                        name = n,
                        garrison = 18,
                        enemyWave = 14 + index * 2   // MED #14: 原 22/26/30/34/38/42 对 18 驻守严重失衡，改为 14/16/18/20/22/24 渐增
                    )
                },
                supply = 80,
                enemyMomentum = 40
            )
        }
    }
}

// ---- 扩展 GameState：所有新字段都给默认值，旧存档直接兼容 ----

@Serializable
data class V3GameState(
    val surname: String = "李",
    val founderName: String = "李慎行",
    val clanName: String = "李氏宗族",
    val root: String = "没落士族",
    val county: String = "江南水乡",
    val creed: String = "耕读传家",
    val crisis: String = "官府催税",
    val originTraits: List<String> = emptyList(),
    val year: Int = 1601,
    val month: Int = 1,
    val clanRank: Int = 1,
    val nextPersonId: Int = 2,
    val silver: Int = 160,
    val grain: Int = 260,
    val influence: Int = 35,
    val cohesion: Int = 60,
    val militia: Int = 0,                            // HIGH #4: 派生自 army.militia，默认 0 由 newGame()/反序列化填充
    val army: V3ArmyRoster = V3ArmyRoster(),
    val equipment: List<V3EquipmentItem> = emptyList(),
    val people: List<V3Person> = emptyList(),        // HIGH #1: 不再引用 V3Content 单例
    val branches: List<V3Branch> = emptyList(),
    val sites: List<V3CountySite> = emptyList(),
    val estateAssets: List<V3EstateAsset> = emptyList(),
    val worldRegions: List<V3WorldRegion> = emptyList(),
    val conquestState: V3ConquestState? = null,
    val unificationProgress: Int = 20,
    val relations: V3Relations = V3Relations(),
    val routeScores: Map<V3Route, Int> = emptyMap(),
    val annualGoals: List<V3AnnualGoal> = emptyList(),
    val examSession: V3ExamSession? = null,
    val battleState: V3BattleState? = null,
    val rebelHeat: Int = 0,
    val finalEnding: V3FinalEnding? = null,
    val activeEvent: V3ActiveEvent? = null,
    val tutorialVersion: Int = V3_TUTORIAL_VERSION,
    val tutorialStep: Int = 0,
    val tutorialCompleted: Boolean = false,
    val claimedChapterRewards: List<String> = emptyList(),
    val seenChapterMilestones: List<String> = emptyList(),
    val completedStoryFlags: List<String> = emptyList(),
    val pendingReports: List<String> = listOf("县域初定，族老请你先审视祠堂、田庄、集市与县衙。"),
    val eventLog: List<String> = listOf("万历二十九年，宗族重立谱牒，县域沙盘由此展开。"),

    // —— P0 融合字段 ——
    val patriarch: V3Patriarch = V3Patriarch(),
    val pendingSuccession: Boolean = false,
    val refugees: Int = 0,                 // 流民
    val garrisonMorale: Int = 60,          // 守望士气 0-100
    val unrestLevel: Int = 0,              // 庄内怨气象 0-100（过高触发庄乱）
    val activeCards: List<V3MonthlyCard> = emptyList(),
    val playedCardsThisMonth: Int = 0,
    val cardBudget: Int = 3,               // 本月可出牌数
    val seenCardIds: List<String> = emptyList(),
    val seenCardGenerations: Map<Int, List<String>> = emptyMap(),
    val seenVisitors: List<String> = emptyList(),
    val visitorProgress: Map<String, Int> = emptyMap(),
    val plaques: List<String> = emptyList(), // 族望匾 id 列表
    val inventory: List<String> = emptyList(), // 物品 id 列表
    val biography: List<String> = emptyList(), // 族谱·履历条目
    val pendingDice: V3DiceRoll? = null,
    val hexBattleState: V3HexBattleState? = null,
    val hexBattleCompleted: Boolean = false,
    val currentCrisisStage: String? = null // grain_shortage / unrest / mutiny，便于级联
)
