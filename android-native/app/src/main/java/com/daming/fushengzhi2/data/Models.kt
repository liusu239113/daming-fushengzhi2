package com.daming.fushengzhi2.data

import kotlinx.serialization.Serializable

const val SAVE_VERSION = 4

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
    val unlockedAchievements: List<String> = emptyList(),
    val totalBirths: Int = 0,
    val totalDeaths: Int = 0,
    val totalExamPasses: Int = 0,
    val totalMilitaryMerits: Int = 0,
    val army: ArmyState = ArmyState(),
    val gameEnded: Boolean = false
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
    val talentId: String? = null,
    val study: Int = 1,
    val martial: Int = 1,
    val health: Int = 100,
    val militaryRank: String? = null,
    val assignmentId: Int? = null,
    val aptitude: AptitudeSet = AptitudeSet(),
    val laborJob: String? = null,
    val alive: Boolean = true
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
    Trade("经商"),
    Military("从军"),
    Sick("生病"),
    Expedition("历练"),
    Labor("打工"),
    Official("为官"),
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
    val prices: Map<String, Int> = mapOf(
        "grain" to 2,
        "cloth" to 5,
        "herb" to 80,
        "book" to 120,
        "weapon" to 150,
        "horse" to 400
    ),
    val lastUpdatedMonth: Int = 0
)

@Serializable
data class EventLogEntry(val year: Int, val month: Int, val text: String)

@Serializable
data class ArmyState(
    val infantry: Int = 0,
    val archers: Int = 0,
    val trainingLevel: Int = 0
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
