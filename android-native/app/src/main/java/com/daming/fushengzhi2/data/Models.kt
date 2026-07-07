package com.daming.fushengzhi2.data

import kotlinx.serialization.Serializable

const val SAVE_VERSION = 5

@Serializable
data class SaveEnvelope(
    val version: Int = SAVE_VERSION,
    val timestamp: Long = System.currentTimeMillis(),
    val state: GameState
)

@Serializable
data class GameState(
    val surname: String = "李",
    val originId: String = "farmer",
    val regionId: String = "shaanbei",
    val clanName: String = "李氏宗族",
    val clanRank: Int = 1,
    val year: Int = 1368,
    val month: Int = 1,
    val totalMonths: Int = 0,
    val silver: Int = 30,
    val grain: Int = 80,
    val cloth: Int = 20,
    val fame: Int = 5,
    val members: List<ClanMember> = emptyList(),
    val nextMemberId: Int = 1,
    val patriarchId: Int? = null,
    val industries: List<ClanIndustry> = emptyList(),
    val nextIndustryId: Int = 1,
    val fortCount: Int = 0,
    val clanRules: List<String> = emptyList(),
    val familyMottoId: String = "study",
    val difficultyId: String = "normal",
    val academies: List<Academy> = emptyList(),
    val expeditions: List<Expedition> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(),
    val market: MarketState = MarketState(),
    val conqueredStages: List<Int> = emptyList(),
    val eventLog: List<EventLogEntry> = emptyList(),
    val triggeredHistoryYears: List<Int> = emptyList(),
    val unlockedAchievements: List<String> = emptyList(),
    val yearlyGoals: List<YearlyGoalState> = emptyList(),
    val completedGoalIds: List<String> = emptyList(),
    val goalYear: Int = 0,
    val yearStartSnapshot: YearSnapshot? = null,
    val totalBirths: Int = 0,
    val totalDeaths: Int = 0,
    val totalExamPasses: Int = 0,
    val totalMilitaryMerits: Int = 0,
    val army: ArmyState = ArmyState(),
    val pet: PetState? = null,
    val lastSacrificeYear: Int = 0,
    val clinicHealsThisYear: Int = 0,
    val gameEnded: Boolean = false,
    val endingChoice: String? = null,
    val hiddenEnding: String? = null,
    val triggeredHiddenEndings: List<String> = emptyList()
)

@Serializable
data class ClanMember(
    val id: Int,
    val name: String,
    val gender: Gender,
    val age: Int,
    val generation: Int = 1,
    val parentId: Int? = null,
    val spouseId: Int? = null,
    val childrenIds: List<Int> = emptyList(),
    val identity: String = "白丁",
    val state: MemberState = MemberState.Home,
    val prevState: MemberState? = null,
    val talentId: String? = null,
    val study: Int = 1,
    val martial: Int = 1,
    val health: Int = 100,
    val militaryRank: String? = null,
    val officialRank: String? = null,
    val officialTenure: Int = 0,
    val assignmentId: Int? = null,
    val aptitude: AptitudeSet = AptitudeSet(),
    val laborJob: String? = null,
    val skillPath: String? = null,
    val equipment: Map<String, String> = emptyMap(),
    val alive: Boolean = true,
    val deathYear: Int? = null,
    val deathAge: Int? = null,
    val deathCause: String? = null
)

@Serializable
data class AptitudeSet(
    val study: Aptitude = Aptitude(),
    val martial: Aptitude = Aptitude(),
    val health: Aptitude = Aptitude()
)

@Serializable
data class Aptitude(val cap: Int = 60, val stars: Int = 2)

@Serializable
enum class Gender { Male, Female }

@Serializable
enum class MemberState(val label: String) {
    Home("在家"),
    Study("读书"),
    Exam("赶考"),
    Trade("经商"),
    Military("从军"),
    Battle("出征"),
    Sick("生病"),
    Expedition("历练"),
    Labor("打工"),
    Official("为官"),
    Left("离族"),
    Dead("亡故")
}

@Serializable
data class ClanIndustry(
    val id: Int,
    val typeId: String,
    val level: Int = 1,
    val assignedMemberId: Int? = null
)

@Serializable
data class Academy(
    val typeId: String,
    val level: Int = 1,
    val memberIds: List<Int> = emptyList()
)

@Serializable
data class Expedition(
    val typeId: String,
    val memberId: Int,
    val monthsLeft: Int,
    val startYear: Int,
    val startMonth: Int
)

@Serializable
data class InventoryItem(val itemId: String, val count: Int)

@Serializable
data class MarketState(
    val prices: Map<String, Int> = GameContent.defaultMarketPrices(),
    val stock: Map<String, Int> = emptyMap(),
    val history: Map<String, List<Int>> = emptyMap(),
    val lastUpdatedMonth: Int = 0,
    val totalTradeProfit: Int = 0,
    val tradeCount: Int = 0,
    val monthlySpent: Int = 0
)

@Serializable
data class EventLogEntry(val year: Int, val month: Int, val text: String)

@Serializable
data class ArmyState(
    val infantry: Int = 0,
    val archers: Int = 0,
    val trainingLevel: Int = 0
)

@Serializable
data class PetState(
    val type: String,
    val name: String,
    val adoptYear: Int,
    val adoptMonth: Int,
    val lifespan: Int,
    val alive: Boolean = true,
    val deathYear: Int? = null,
    val deathMonth: Int? = null
)

@Serializable
data class YearlyGoalState(
    val goalId: String,
    val completed: Boolean = false
)

@Serializable
data class YearSnapshot(
    val silver: Int,
    val grain: Int,
    val cloth: Int,
    val fame: Int,
    val aliveCount: Int,
    val clanRank: Int,
    val industryCount: Int,
    val fortCount: Int,
    val totalBirths: Int,
    val totalDeaths: Int,
    val totalExamPasses: Int,
    val totalMilitaryMerits: Int
)

data class Origin(
    val id: String,
    val name: String,
    val desc: String,
    val silver: Int,
    val grain: Int,
    val cloth: Int,
    val fame: Int
)

data class Region(
    val id: String,
    val name: String,
    val desc: String,
    val disasterRate: Double,
    val banditRate: Double,
    val taxRate: Double
)

data class Difficulty(
    val id: String,
    val name: String,
    val desc: String,
    val disasterMul: Double,
    val taxMul: Double
)

data class Talent(
    val id: String,
    val name: String,
    val effect: String,
    val studyBonus: Double = 0.0,
    val tradeBonus: Double = 0.0,
    val militaryBonus: Double = 0.0,
    val sickRate: Double = 0.0,
    val outputBonus: Double = 0.0,
    val marriageBonus: Double = 0.0,
    val fertilityBonus: Double = 0.0
)

data class ClanRule(
    val id: String,
    val name: String,
    val desc: String,
    val icon: String,
    val effects: RuleEffects
)

data class FamilyMotto(
    val id: String,
    val name: String,
    val icon: String,
    val desc: String,
    val effects: RuleEffects
)

data class RuleEffects(
    val grainConsumeMul: Double = 0.0,
    val grainOutputMul: Double = 0.0,
    val clothConsumeMul: Double = 0.0,
    val fameDrain: Int = 0,
    val studyGrowthMul: Double = 0.0,
    val martialGrowthMul: Double = 0.0,
    val tradeIncomeMul: Double = 0.0,
    val allOutputMul: Double = 0.0,
    val banditResist: Double = 0.0,
    val examPassBonus: Double = 0.0,
    val militarySurvival: Double = 0.0,
    val fortCostMul: Double = 0.0,
    val initSilverBonus: Int = 0
)

data class ItemType(
    val id: String,
    val name: String,
    val icon: String,
    val rarity: String,
    val desc: String,
    val value: Int,
    val effect: String
)

data class AcademyType(
    val id: String,
    val name: String,
    val icon: String,
    val attribute: String,
    val desc: String,
    val baseSlotsPerLevel: Int,
    val upgradeCost: Int,
    val baseBonus: Double,
    val unlockRank: Int
)

data class ExpeditionType(
    val id: String,
    val name: String,
    val icon: String,
    val desc: String,
    val duration: Int,
    val costSilver: Int,
    val costGrain: Int,
    val minRank: Int,
    val rewardSilver: IntRange? = null,
    val rewardCloth: IntRange? = null,
    val rewardFame: IntRange? = null,
    val rewardStudy: IntRange? = null,
    val rewardMartial: IntRange? = null,
    val itemChance: Double = 0.0,
    val recruitChance: Double = 0.0,
    val riskRate: Double,
    val riskDesc: String
)

data class ExamLevel(
    val id: String,
    val name: String,
    val reqStudy: Int,
    val passRate: Double,
    val result: String,
    val famePlus: Int
)

data class MilitaryRankDef(
    val id: String,
    val name: String,
    val deathRate: Double,
    val silverPay: Int,
    val famePlus: Int
)

data class OfficialRankDef(
    val id: String,
    val name: String,
    val reqIdentity: String,
    val silver: Int,
    val fame: Int,
    val taxReduce: Double,
    val demotionRate: Double,
    val famePerMonth: Int = 0,
    val desc: String
)

data class MarriageTier(
    val id: String,
    val name: String,
    val silverCost: Int,
    val grainCost: Int,
    val fameReq: Int,
    val famePlus: Int,
    val desc: String,
    val bonusType: String? = null,
    val bonusValue: Int = 0,
    val unlockRank: Int
)

data class LaborJob(
    val id: String,
    val name: String,
    val rank: Int,
    val wage: Int,
    val desc: String
)

data class MarketCommodity(
    val id: String,
    val name: String,
    val icon: String,
    val unit: String,
    val basePrice: Int,
    val batchSize: Int,
    val fluctRange: Double,
    val desc: String,
    val resourceKey: String? = null,
    val itemId: String? = null
)

data class CampaignStage(
    val id: Int,
    val name: String,
    val enemyPower: Int,
    val rewardSilver: Int,
    val rewardFame: Int
)

data class IndustryType(
    val id: String,
    val name: String,
    val resource: ResourceKind,
    val baseOutput: Int,
    val cost: Int,
    val desc: String,
    val unlockRank: Int,
    val resource2: ResourceKind? = null,
    val baseOutput2: Int = 0,
    val resource3: ResourceKind? = null,
    val baseOutput3: Int = 0,
    val specialEffect: String? = null,
    val evolvesTo: String? = null,
    val evolved: Boolean = false
)

enum class ResourceKind(val label: String) {
    Silver("银两"), Grain("粮食"), Cloth("布匹"), Fame("声望"), None("无")
}

data class RankRequirement(
    val silver: Int,
    val fame: Int,
    val grain: Int = 0,
    val cloth: Int = 0,
    val population: Int
)

data class IndustryEvolution(
    val from: String,
    val to: String,
    val reqLevel: Int,
    val reqRank: Int,
    val cost: Int,
    val desc: String
)

data class HistoryEvent(
    val year: Int,
    val title: String,
    val desc: String,
    val effect: String
)

data class AchievementDef(
    val id: String,
    val name: String,
    val desc: String,
    val icon: String,
    val rewardSilver: Int = 0,
    val rewardGrain: Int = 0,
    val rewardCloth: Int = 0,
    val rewardFame: Int = 0
)

data class YearlyGoalDef(
    val id: String,
    val name: String,
    val desc: String,
    val icon: String,
    val rewardSilver: Int = 0,
    val rewardGrain: Int = 0,
    val rewardCloth: Int = 0,
    val rewardFame: Int = 0,
    val minRank: Int = 1
)

data class MonthlyReport(
    val year: Int,
    val month: Int,
    val incomeSilver: Int,
    val incomeGrain: Int,
    val incomeCloth: Int,
    val incomeFame: Int,
    val expenseSilver: Int,
    val expenseGrain: Int,
    val expenseCloth: Int,
    val expenseFame: Int,
    val events: List<String>
)

enum class GameTab(val label: String) {
    Tree("族谱"),
    Clan("宗族"),
    Members("族人"),
    Industry("经营"),
    Career("功业"),
    Academy("书院"),
    Expedition("历练"),
    Inventory("库房"),
    Market("集市"),
    Battle("征伐")
}
