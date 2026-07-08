package com.daming.fushengzhi3.v3.data

import kotlinx.serialization.Serializable

const val V3_SAVE_VERSION = 1

@Serializable
enum class V3Screen { County, Clan, People, Strategy }

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
enum class V3GoalMetric(val label: String) {
    SilverStock("银两储备"),
    GrainStock("粮食储备"),
    Militia("乡勇规模"),
    Cohesion("宗族凝聚"),
    Influence("族望声名"),
    ControlledSites("控制地点"),
    SafeSites("安定地点"),
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
data class V3UpgradeCost(
    val silver: Int,
    val grain: Int,
    val desc: String
)

@Serializable
data class V3GameState(
    val clanName: String = "李氏宗族",
    val root: String = "没落士族",
    val county: String = "江南水乡",
    val creed: String = "耕读传家",
    val crisis: String = "官府催税",
    val year: Int = 1601,
    val month: Int = 1,
    val silver: Int = 160,
    val grain: Int = 260,
    val influence: Int = 35,
    val cohesion: Int = 60,
    val militia: Int = 20,
    val people: List<V3Person> = V3Content.initialPeople,
    val branches: List<V3Branch> = V3Content.initialBranches,
    val sites: List<V3CountySite> = V3Content.initialSites,
    val relations: V3Relations = V3Relations(),
    val routeScores: Map<V3Route, Int> = V3Content.initialRouteScores,
    val annualGoals: List<V3AnnualGoal> = V3Content.initialAnnualGoals,
    val finalEnding: V3FinalEnding? = null,
    val activeEvent: V3ActiveEvent? = null,
    val pendingReports: List<String> = listOf("县域初定，族老请你先审视祠堂、田庄、集市与县衙。"),
    val eventLog: List<String> = listOf("万历二十九年，宗族重立谱牒，县域沙盘由此展开。")
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
    val merit: Int = 0,
    val fatigue: Int = 0,
    val currentTask: V3TaskType? = null,
    val assignedSiteId: String? = null
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
    val route: V3Route,
    val routeDelta: Int = 4,
    val branchImpacts: List<V3BranchImpact> = emptyList()
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
    val nextState: V3GameState
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
    val stats: List<String>
)
