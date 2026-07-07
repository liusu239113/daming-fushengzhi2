-- ============================================================================
-- 大明浮生志2 - 核心游戏数据模块
-- 管理所有游戏状态：资源、族人、产业、年份等
-- ============================================================================

local EraSystem = require("Data.EraSystem")
local IndustryData = require("Data.IndustryData")
local MemberData = require("Data.MemberData")

local GameData = {}

-- 姓氏列表（开局选择用，保持精简）
GameData.SURNAMES = { "李", "王", "张", "刘", "陈", "赵", "朱", "杨", "黄", "周" }

-- 出身选项
GameData.ORIGINS = {
    { id = "farmer", name = "寒门农户", desc = "资源少、无负债，白手起家", silver = 30, grain = 80, cloth = 20, fame = 5 },
    { id = "landlord", name = "小地主", desc = "有田产积蓄，但树大招风", silver = 120, grain = 200, cloth = 60, fame = 20 },
    { id = "military", name = "退役军户", desc = "族长有武艺，结寨防御加成", silver = 60, grain = 120, cloth = 30, fame = 15 },
}

-- 地域选项
GameData.REGIONS = {
    { id = "shaanbei", name = "陕北", desc = "流寇频繁、天灾多、民风彪悍", disasterRate = 0.3, banditRate = 0.25, taxRate = 0.1 },
    { id = "henan", name = "河南", desc = "四战之地、灾荒严重", disasterRate = 0.25, banditRate = 0.2, taxRate = 0.12 },
    { id = "jiangnan", name = "江南", desc = "富庶但赋税重、后期清军压力", disasterRate = 0.1, banditRate = 0.08, taxRate = 0.2 },
    { id = "huguang", name = "湖广", desc = "鱼米之乡、但张献忠之乱将至", disasterRate = 0.12, banditRate = 0.15, taxRate = 0.12 },
}

-- 宗族品级
GameData.CLAN_RANKS = { "寒门", "农户", "乡绅", "望族", "世家", "勋贵", "名门", "豪阀", "国柱" }

--- 品级升级需求表（索引 = 目标品级）
--- silver: 银两, fame: 声望, population: 最低族人数
GameData.RANK_UP_REQUIREMENTS = {
    -- [1] 寒门是初始品级，无需升级
    [2] = { silver = 500,   fame = 300,   grain = 300,  population = 6  },  -- 寒门 → 农户
    [3] = { silver = 1500,  fame = 800,   grain = 600,  cloth = 100, population = 10 },  -- 农户 → 乡绅
    [4] = { silver = 4000,  fame = 2000,  grain = 1200, cloth = 300, population = 15 },  -- 乡绅 → 望族
    [5] = { silver = 10000, fame = 5000,  grain = 3000, cloth = 800, population = 22 },  -- 望族 → 世家
    [6] = { silver = 25000, fame = 12000, grain = 6000, cloth = 2000, population = 30 },  -- 世家 → 勋贵
    [7] = { silver = 60000, fame = 30000, grain = 15000, cloth = 5000, population = 40 },  -- 勋贵 → 名门
    [8] = { silver = 150000,fame = 80000, grain = 40000, cloth = 12000, population = 55 },  -- 名门 → 豪阀
    [9] = { silver = 400000,fame = 200000,grain = 100000,cloth = 30000, population = 75 },  -- 豪阀 → 国柱
}

--- 每个品级允许拥有的最大产业数量
GameData.INDUSTRY_LIMIT_BY_RANK = {
    [1] = 4,   -- 寒门
    [2] = 6,   -- 农户
    [3] = 9,   -- 乡绅
    [4] = 12,  -- 望族
    [5] = 16,  -- 世家
    [6] = 20,  -- 勋贵
    [7] = 25,  -- 名门
    [8] = 30,  -- 豪阀
    [9] = 40,  -- 国柱
}

-- ============================================================================
-- 从 IndustryData / MemberData 模块回挂（向后兼容）
-- 外部代码仍可通过 GameData.XXX 访问，内部数据源在独立模块中
-- ============================================================================
GameData.INDUSTRY_TYPES = IndustryData.INDUSTRY_TYPES
GameData.INDUSTRY_EVOLUTION = IndustryData.INDUSTRY_EVOLUTION
GameData.EVOLVED_INDUSTRY_TYPES = IndustryData.EVOLVED_INDUSTRY_TYPES
GameData.INDUSTRY_UNLOCK = IndustryData.INDUSTRY_UNLOCK
GameData.GetIndustryType = IndustryData.GetIndustryType

GameData.IDENTITIES = MemberData.IDENTITIES
GameData.STATES = MemberData.STATES
GameData.TALENTS = MemberData.TALENTS
GameData.AGE_STAGES = MemberData.AGE_STAGES
GameData.EXAM_LEVELS = MemberData.EXAM_LEVELS
GameData.MILITARY_RANKS = MemberData.MILITARY_RANKS
GameData.MARRIAGE_TIERS = MemberData.MARRIAGE_TIERS
GameData.MARRIAGE_UNLOCK = MemberData.MARRIAGE_UNLOCK
GameData.DONATION_COST = MemberData.DONATION_COST
GameData.HORSE_COST = MemberData.HORSE_COST
GameData.GetAgeStage = MemberData.GetAgeStage
GameData.RandomGivenName = MemberData.RandomGivenName

-- 新增：装备和技能系统回挂
GameData.EQUIPMENT_SLOTS = MemberData.EQUIPMENT_SLOTS
GameData.SLOT_NAMES = MemberData.SLOT_NAMES
GameData.EQUIPMENT_RARITIES = MemberData.EQUIPMENT_RARITIES
GameData.EQUIPMENT_LIST = MemberData.EQUIPMENT_LIST
GameData.GetEquipment = MemberData.GetEquipment
GameData.GetRarityConfig = MemberData.GetRarityConfig
GameData.GetEquipmentBySlot = MemberData.GetEquipmentBySlot
GameData.SKILL_PATHS = MemberData.SKILL_PATHS
GameData.CanUnlockSkillPath = MemberData.CanUnlockSkillPath
GameData.GetActiveSkillPath = MemberData.GetActiveSkillPath
GameData.TRAINING_OPTIONS = MemberData.TRAINING_OPTIONS

-- 族规系统
GameData.CLAN_RULES = {
    { id = "store_grain", name = "储粮备荒", desc = "粮食消耗-20%，但产出也-10%", icon = "仓",
      effects = { grainConsumeMul = -0.2, grainOutputMul = -0.1 } },
    { id = "martial_train", name = "习武自卫", desc = "族人武艺成长+50%，流寇抵抗+20%", icon = "武",
      effects = { martialGrowthMul = 0.5, banditResist = 0.2 } },
    { id = "frugal", name = "节衣缩食", desc = "布匹消耗-40%，但声望每月-1", icon = "俭",
      effects = { clothConsumeMul = -0.4, fameDrain = -1 } },
    { id = "study_first", name = "耕读传家", desc = "学识成长+30%，但需额外粮食供给", icon = "读",
      effects = { studyGrowthMul = 0.3, grainConsumeMul = 0.1 } },
    { id = "merchant_focus", name = "重商兴族", desc = "经商收益+25%，但声望-2/月", icon = "商",
      effects = { tradeIncomeMul = 0.25, fameDrain = -2 } },
    { id = "fortify", name = "全民筑寨", desc = "筑寨费用-30%，但产业产出-10%", icon = "堡",
      effects = { fortCostMul = -0.3, allOutputMul = -0.1 } },
}

-- ============================================================================
-- 家训系统（替代修仙世家的图腾系统）
-- 开局选择，给予全族永久被动buff
-- ============================================================================

GameData.FAMILY_MOTTOS = {
    { id = "study",    name = "诗书传家", icon = "书",
      desc = "全族学识成长+20%，科举通过率+10%",
      effects = { studyGrowthMul = 0.2, examPassBonus = 0.1 } },
    { id = "martial",  name = "武德充沛", icon = "武",
      desc = "全族武艺成长+25%，从军存活率+15%",
      effects = { martialGrowthMul = 0.25, militarySurvival = 0.15 } },
    { id = "trade",    name = "货殖兴家", icon = "商",
      desc = "经商收益+20%，初始银两+30",
      effects = { tradeIncomeMul = 0.2, initSilverBonus = 30 } },
    { id = "farm",     name = "耕读为本", icon = "田",
      desc = "粮食产出+20%，消耗-10%",
      effects = { grainOutputMul = 0.2, grainConsumeMul = -0.1 } },
    { id = "fortify",  name = "聚族自保", icon = "堡",
      desc = "流寇抵抗+30%，筑寨费用-20%",
      effects = { banditResist = 0.3, fortCostMul = -0.2 } },
}

-- ============================================================================
-- 难度选项
-- ============================================================================

GameData.DIFFICULTIES = {
    { id = "easy",   name = "太平盛世", desc = "灾害概率-50%，税率-30%", disasterMul = 0.5, taxMul = 0.7 },
    { id = "normal", name = "风雨飘摇", desc = "标准难度", disasterMul = 1.0, taxMul = 1.0 },
    { id = "hard",   name = "末世浩劫", desc = "灾害概率+30%，税率+20%", disasterMul = 1.3, taxMul = 1.2 },
}

-- ============================================================================
-- 书院/武馆系统（替代修仙世家的仙峰系统）
-- 设施有等级和席位，族人分配到席位上获得加速成长
-- ============================================================================

GameData.ACADEMY_TYPES = {
    { id = "school",    name = "族学",   icon = "学", attribute = "study",
      desc = "提升族人学识成长速度", baseSlotsPerLevel = 2, upgradeCost = 250,
      baseBonus = 0.5 },  -- 额外50%学识成长
    { id = "wuguan",    name = "武馆",   icon = "武", attribute = "martial",
      desc = "提升族人武艺成长速度", baseSlotsPerLevel = 2, upgradeCost = 300,
      baseBonus = 0.6 },  -- 额外60%武艺成长
}

-- ============================================================================
-- 探索历练系统（替代修仙世家的外出历练）
-- 选择族人 + 目的地，消耗资源和时间，归来获得奖励
-- ============================================================================

GameData.EXPEDITION_TYPES = {
    { id = "market",    name = "赶集贸易", icon = "集",
      desc = "去集市贸易，获取银两和布匹", duration = 1,
      cost = { silver = 40, grain = 40 },
      rewards = { silver = { 60, 150 }, cloth = { 10, 30 } },
      riskRate = 0.05, riskDesc = "遭遇劫匪" },
    { id = "trade_route", name = "行商远途", icon = "商",
      desc = "沿商路远行贸易，收益丰厚但风险也大", duration = 3,
      cost = { silver = 120, grain = 80 },
      rewards = { silver = { 200, 500 }, fame = { 3, 8 } },
      riskRate = 0.15, riskDesc = "路遇山贼" },
    { id = "study_trip", name = "游学求教", icon = "游",
      desc = "拜访名士，增长学识见闻", duration = 2,
      cost = { silver = 80, grain = 60 },
      rewards = { study = { 3, 8 }, fame = { 3, 8 } },
      riskRate = 0.05, riskDesc = "水土不服" },
    { id = "explore",   name = "深山探秘", icon = "探",
      desc = "进山探寻宝物，可能有意外收获", duration = 2,
      cost = { silver = 60, grain = 80 },
      rewards = { silver = { 30, 250 }, item_chance = 0.3 },
      riskRate = 0.20, riskDesc = "遭遇猛兽" },
    { id = "recruit",   name = "招贤纳士", icon = "招",
      desc = "四处访求能人异士加入宗族", duration = 2,
      cost = { silver = 150, grain = 80 },
      rewards = { recruit_chance = 0.4, fame = { 3, 10 } },
      riskRate = 0.08, riskDesc = "遇到骗子" },

    -- === 勋贵（6级）===
    { id = "sea_trade",  name = "海上通商", icon = "帆",
      desc = "扬帆出海，与番邦贸易，利润极高", duration = 4,
      cost = { silver = 300, grain = 150 },
      rewards = { silver = { 500, 1200 }, fame = { 8, 20 } },
      riskRate = 0.25, riskDesc = "遭遇海寇" },
    { id = "court_visit", name = "进京面圣", icon = "朝",
      desc = "上京朝觐，结交朝中权贵", duration = 3,
      cost = { silver = 500, grain = 200 },
      rewards = { fame = { 20, 50 }, silver = { 100, 300 } },
      riskRate = 0.10, riskDesc = "卷入党争" },

    -- === 名门（7级）===
    { id = "border_patrol", name = "巡边戍守", icon = "戍",
      desc = "率族中精锐巡视边关，军功换声望", duration = 3,
      cost = { silver = 200, grain = 200 },
      rewards = { fame = { 15, 40 }, martial_boost = { 3, 8 } },
      riskRate = 0.18, riskDesc = "遭遇敌袭" },
    { id = "treasure_hunt", name = "寻访古迹", icon = "古",
      desc = "探访前朝遗迹，寻觅传世珍宝", duration = 3,
      cost = { silver = 250, grain = 120 },
      rewards = { silver = { 100, 800 }, item_chance = 0.5, fame = { 5, 15 } },
      riskRate = 0.20, riskDesc = "机关陷阱" },

    -- === 豪阀（8级）===
    { id = "silk_road",  name = "丝路远征", icon = "驼",
      desc = "沿丝绸之路远赴西域，带回奇珍异宝", duration = 6,
      cost = { silver = 800, grain = 400 },
      rewards = { silver = { 1000, 3000 }, fame = { 20, 50 }, item_chance = 0.6 },
      riskRate = 0.30, riskDesc = "沙匪劫掠" },

    -- === 国柱（9级）===
    { id = "tributary_mission", name = "万邦来朝", icon = "使",
      desc = "代天子出使四方，宣扬国威；声望名利双收", duration = 5,
      cost = { silver = 1500, grain = 600 },
      rewards = { fame = { 50, 120 }, silver = { 800, 2000 } },
      riskRate = 0.15, riskDesc = "外邦刁难" },
}

-- ============================================================================
-- 库房物品定义
-- ============================================================================

GameData.ITEM_TYPES = {
    { id = "herb",      name = "上等药材", icon = "药", rarity = "common",
      desc = "可治疗族人伤病", useEffect = "heal", value = 80 },
    { id = "book",      name = "经史典籍", icon = "典", rarity = "uncommon",
      desc = "使用后学识+3", useEffect = "study_boost", value = 120 },
    { id = "weapon",    name = "精钢兵器", icon = "兵", rarity = "uncommon",
      desc = "使用后武艺+3", useEffect = "martial_boost", value = 150 },
    { id = "jade",      name = "玉石珍玩", icon = "玉", rarity = "rare",
      desc = "可变卖换取银两250", useEffect = "sell", value = 250 },
    { id = "scroll",    name = "兵法残卷", icon = "卷", rarity = "rare",
      desc = "使用后武艺+5", useEffect = "martial_boost_large", value = 200 },
    { id = "seal",      name = "官府印信", icon = "印", rarity = "epic",
      desc = "声望+15", useEffect = "fame_boost", value = 300 },
    { id = "heirloom",  name = "传家宝", icon = "宝", rarity = "epic",
      desc = "全族士气提升，声望+20", useEffect = "morale_boost", value = 400 },
}

GameData.RARITY_COLORS = {
    common   = { 180, 180, 180, 255 },
    uncommon = { 100, 200, 100, 255 },
    rare     = { 80, 140, 255, 255 },
    epic     = { 200, 120, 255, 255 },
}

-- 明朝历史大事件时间线（引用 EraSystem）
GameData.HISTORY_EVENTS = EraSystem.HISTORY_EVENTS

-- 年号系统（对外暴露便捷函数）
GameData.EraSystem = EraSystem

-- ============================================================================
-- 成就/里程碑系统
-- ============================================================================

GameData.ACHIEVEMENTS = {
    -- 人口类
    { id = "pop_10",      name = "人丁兴旺",     desc = "宗族存活人口达到10人",      icon = "丁", reward = { fame = 10 },
      check = function(s) return #GameData.GetAliveMembers() >= 10 end },
    { id = "pop_20",      name = "枝繁叶茂",     desc = "宗族存活人口达到20人",      icon = "族", reward = { fame = 20 },
      check = function(s) return #GameData.GetAliveMembers() >= 20 end },
    { id = "pop_30",      name = "百口之家",     desc = "宗族存活人口达到30人",      icon = "百", reward = { fame = 30, silver = 20 },
      check = function(s) return #GameData.GetAliveMembers() >= 30 end },
    -- 财富类
    { id = "silver_100",  name = "小有积蓄",     desc = "银两达到100",              icon = "银", reward = { fame = 5 },
      check = function(s) return s.silver >= 100 end },
    { id = "silver_500",  name = "富甲一方",     desc = "银两达到500",              icon = "富", reward = { fame = 15 },
      check = function(s) return s.silver >= 500 end },
    { id = "silver_1000", name = "万贯家财",     desc = "银两达到1000",             icon = "财", reward = { fame = 25, grain = 50 },
      check = function(s) return s.silver >= 1000 end },
    -- 声望类
    { id = "fame_50",     name = "声名鹊起",     desc = "声望达到50",              icon = "名", reward = { silver = 15 },
      check = function(s) return s.fame >= 50 end },
    { id = "fame_100",    name = "名震一方",     desc = "声望达到100",             icon = "望", reward = { silver = 30 },
      check = function(s) return s.fame >= 100 end },
    { id = "fame_200",    name = "天下闻名",     desc = "声望达到200",             icon = "天", reward = { silver = 50, grain = 30 },
      check = function(s) return s.fame >= 200 end },
    -- 科举类
    { id = "first_xiucai", name = "首中秀才",    desc = "族中首次出现秀才",         icon = "秀", reward = { fame = 10, silver = 10 },
      check = function(s)
          for _, m in ipairs(GameData.GetAliveMembers()) do
              if m.identity == "秀才" or m.identity == "举人" or m.identity == "进士" then return true end
          end
          return false
      end },
    { id = "first_juren",  name = "金榜题名",    desc = "族中首次出现举人",         icon = "举", reward = { fame = 20, silver = 30 },
      check = function(s)
          for _, m in ipairs(GameData.GetAliveMembers()) do
              if m.identity == "举人" or m.identity == "进士" then return true end
          end
          return false
      end },
    { id = "first_jinshi", name = "天子门生",    desc = "族中首次出现进士",         icon = "进", reward = { fame = 40, silver = 60 },
      check = function(s)
          for _, m in ipairs(GameData.GetAliveMembers()) do
              if m.identity == "进士" then return true end
          end
          return false
      end },
    -- 军事类
    { id = "first_bazong",  name = "从军立功",    desc = "族中首次出现把总",         icon = "将", reward = { fame = 10, silver = 15 },
      check = function(s)
          for _, m in ipairs(GameData.GetAliveMembers()) do
              if m.militaryRank == "把总" or m.militaryRank == "守备" then return true end
          end
          return false
      end },
    { id = "first_shoubei", name = "武将之家",    desc = "族中首次出现守备",         icon = "帅", reward = { fame = 25, silver = 40 },
      check = function(s)
          for _, m in ipairs(GameData.GetAliveMembers()) do
              if m.militaryRank == "守备" then return true end
          end
          return false
      end },
    { id = "fort_3",        name = "铜墙铁壁",   desc = "拥有3座寨堡",             icon = "堡", reward = { fame = 15 },
      check = function(s) return s.fortCount >= 3 end },
    -- 产业类
    { id = "ind_5",       name = "家业兴旺",     desc = "拥有5个以上产业",          icon = "业", reward = { fame = 8, silver = 10 },
      check = function(s) return #s.industries >= 5 end },
    { id = "ind_10",      name = "田连阡陌",     desc = "拥有10个以上产业",         icon = "田", reward = { fame = 15, silver = 25 },
      check = function(s) return #s.industries >= 10 end },
    -- 品级类
    { id = "rank_xiangsh", name = "乡绅门第",    desc = "宗族品级提升至乡绅",       icon = "绅", reward = { silver = 20 },
      check = function(s) return s.clanRank >= 3 end },
    { id = "rank_wangzu",  name = "望族崛起",    desc = "宗族品级提升至望族",       icon = "族", reward = { silver = 40 },
      check = function(s) return s.clanRank >= 4 end },
    { id = "rank_shijia",  name = "世家大族",    desc = "宗族品级提升至世家",       icon = "世", reward = { silver = 60, grain = 40 },
      check = function(s) return s.clanRank >= 5 end },
    -- 时间类
    { id = "survive_5y",  name = "五年生存",     desc = "宗族延续5年以上",          icon = "年", reward = { fame = 5, grain = 20 },
      check = function(s) return s.totalMonths >= 60 end },
    { id = "survive_10y", name = "十年磨剑",     desc = "宗族延续10年以上",         icon = "剑", reward = { fame = 15, silver = 30 },
      check = function(s) return s.totalMonths >= 120 end },
    { id = "survive_end", name = "乱世生存者",   desc = "坚持到1644年甲申之变",     icon = "存", reward = { fame = 30, silver = 50 },
      check = function(s) return s.year >= 1644 end },
    -- 特殊类
    { id = "births_10",   name = "多子多孙",     desc = "累计出生10名族人",         icon = "孙", reward = { fame = 10 },
      check = function(s) return s.totalBirths >= 10 end },
    { id = "births_20",   name = "子孙满堂",     desc = "累计出生20名族人",         icon = "满", reward = { fame = 20 },
      check = function(s) return s.totalBirths >= 20 end },
    { id = "exams_3",     name = "书香世家",     desc = "累计3人通过科举",          icon = "书", reward = { fame = 15, silver = 20 },
      check = function(s) return s.totalExamPasses >= 3 end },
    { id = "merits_5",    name = "将门虎子",     desc = "累计5次军功",             icon = "虎", reward = { fame = 20, silver = 25 },
      check = function(s) return s.totalMilitaryMerits >= 5 end },
}

-- ============================================================================
-- 年度目标池（每年初从中随机抽取2-3个作为可选目标）
-- ============================================================================

GameData.YEARLY_GOAL_POOL = {
    -- === 经济目标 ===
    { id = "earn_silver_30",  name = "积银三十", desc = "本年银两净增30以上",
      icon = "银", reward = { fame = 5, grain = 15 },
      check = function(s, snapshot) return s.silver - snapshot.silver >= 30 end,
      condition = function(s) return true end },
    { id = "earn_silver_80",  name = "家财万贯", desc = "本年银两净增80以上",
      icon = "财", reward = { fame = 10, grain = 25 },
      check = function(s, snapshot) return s.silver - snapshot.silver >= 80 end,
      condition = function(s) return s.silver >= 30 end },
    { id = "earn_grain_50",   name = "五谷丰登", desc = "本年粮食净增50以上",
      icon = "谷", reward = { silver = 15, fame = 5 },
      check = function(s, snapshot) return s.grain - snapshot.grain >= 50 end,
      condition = function(s) return #s.industries >= 2 end },
    { id = "save_silver_200", name = "富甲乡里", desc = "年末银两存量达到200",
      icon = "富", reward = { fame = 12, grain = 20 },
      check = function(s, snapshot) return s.silver >= 200 end,
      condition = function(s) return s.silver >= 80 end },

    -- === 人口目标 ===
    { id = "birth_2",   name = "添丁进口", desc = "本年至少新增2名族人",
      icon = "丁", reward = { grain = 20, fame = 5 },
      check = function(s, snapshot) return s.totalBirths - snapshot.totalBirths >= 2 end,
      condition = function(s)
          for _, m in ipairs(GameData.GetAliveMembers()) do
              if m.gender == "female" and m.age >= 18 and m.age <= 38 and m.spouseId then return true end
          end
          return false
      end },
    { id = "recruit_1", name = "招贤纳士", desc = "本年至少通过历练或事件招募1名新族人",
      icon = "招", reward = { silver = 10, fame = 5 },
      check = function(s, snapshot) return #GameData.GetAliveMembers() > snapshot.aliveCount end,
      condition = function(s) return #GameData.GetAdultMembers() >= 3 end },
    { id = "pop_15",    name = "人丁兴旺", desc = "年末存活人口达到15人",
      icon = "旺", reward = { fame = 10, silver = 10 },
      check = function(s, snapshot) return #GameData.GetAliveMembers() >= 15 end,
      condition = function(s) return #GameData.GetAliveMembers() >= 8 end },

    -- === 军事目标 ===
    { id = "build_fort",   name = "筑寨一座", desc = "本年建造至少1座新寨堡",
      icon = "堡", reward = { fame = 8, silver = 10 },
      check = function(s, snapshot) return s.fortCount > snapshot.fortCount end,
      condition = function(s) return s.silver >= 80 end },
    { id = "martial_40",   name = "武艺精进", desc = "年末至少1名族人武艺达到40",
      icon = "武", reward = { fame = 8, silver = 8 },
      check = function(s, snapshot)
          for _, m in ipairs(GameData.GetAliveMembers()) do
              if m.martial >= 40 then return true end
          end
          return false
      end,
      condition = function(s)
          for _, m in ipairs(GameData.GetAliveMembers()) do
              if m.martial >= 20 then return true end
          end
          return false
      end },
    { id = "enlist_1",     name = "投军报国", desc = "本年至少送1名族人从军",
      icon = "军", reward = { fame = 8, silver = 5 },
      check = function(s, snapshot)
          local count = 0
          for _, m in ipairs(GameData.GetAliveMembers()) do
              if m.state == "从军" then count = count + 1 end
          end
          return count > snapshot.soldierCount
      end,
      condition = function(s)
          for _, m in ipairs(GameData.GetAdultMembers()) do
              if m.state == "在家" and m.gender == "male" then return true end
          end
          return false
      end },

    -- === 文化目标 ===
    { id = "study_50",   name = "学识渊博", desc = "年末至少1名族人学识达到50",
      icon = "学", reward = { fame = 8, silver = 10 },
      check = function(s, snapshot)
          for _, m in ipairs(GameData.GetAliveMembers()) do
              if m.study >= 50 then return true end
          end
          return false
      end,
      condition = function(s)
          for _, m in ipairs(GameData.GetAliveMembers()) do
              if m.study >= 25 then return true end
          end
          return false
      end },
    { id = "exam_pass",  name = "金榜题名", desc = "本年至少1名族人通过科举",
      icon = "榜", reward = { fame = 15, silver = 20 },
      check = function(s, snapshot) return s.totalExamPasses > snapshot.totalExamPasses end,
      condition = function(s)
          for _, m in ipairs(GameData.GetAliveMembers()) do
              if m.state == "读书" and m.study >= 25 then return true end
          end
          return false
      end },
    { id = "readers_3",  name = "书声琅琅", desc = "年末至少3名族人在读书",
      icon = "读", reward = { fame = 5, grain = 15 },
      check = function(s, snapshot) return #GameData.GetStudyingMembers() >= 3 end,
      condition = function(s) return #GameData.GetAliveMembers() >= 6 end },

    -- === 产业目标 ===
    { id = "build_ind_2", name = "开拓产业", desc = "本年至少新建2个产业",
      icon = "业", reward = { fame = 8, silver = 10 },
      check = function(s, snapshot) return #s.industries - snapshot.industryCount >= 2 end,
      condition = function(s) return s.silver >= 40 end },
    { id = "upgrade_ind", name = "精益求精", desc = "本年至少升级1个产业至3级",
      icon = "升", reward = { fame = 5, silver = 15 },
      check = function(s, snapshot)
          for _, ind in ipairs(s.industries) do
              if ind.level >= 3 then return true end
          end
          return false
      end,
      condition = function(s)
          for _, ind in ipairs(s.industries) do
              if ind.level >= 2 then return true end
          end
          return false
      end },

    -- === 声望目标 ===
    { id = "fame_plus_20", name = "声名远播", desc = "本年声望净增20以上",
      icon = "名", reward = { silver = 15, grain = 10 },
      check = function(s, snapshot) return s.fame - snapshot.fame >= 20 end,
      condition = function(s) return true end },
    { id = "rank_up",      name = "光宗耀祖", desc = "本年宗族品级提升一次",
      icon = "族", reward = { silver = 20, grain = 20, fame = 10 },
      check = function(s, snapshot) return s.clanRank > snapshot.clanRank end,
      condition = function(s) return s.clanRank < 9 end },

    -- === 生存目标（后期） ===
    { id = "no_death",   name = "全族平安", desc = "本年无一族人死亡",
      icon = "安", reward = { fame = 10, silver = 10, grain = 10 },
      check = function(s, snapshot) return s.totalDeaths == snapshot.totalDeaths end,
      condition = function(s) return #GameData.GetAliveMembers() >= 5 end },
    { id = "survive_low", name = "逆境求存", desc = "年末银两和粮食均大于0",
      icon = "存", reward = { fame = 8, grain = 20 },
      check = function(s, snapshot) return s.silver > 0 and s.grain > 0 end,
      condition = function(s) return s.year >= 1638 end },
}

-- ============================================================================
-- 品级解锁系统 - 决定每个品级可使用的功能
-- 设计理念：从寒门一步步做成勋贵，逐级解锁新玩法，拉长游玩周期
-- ============================================================================

--- 底部导航栏主Tab解锁要求（合并后，取子页签最低要求）
GameData.TAB_UNLOCK = {
    tree      = 1,  -- 寒门即可：族谱
    clan      = 1,  -- 寒门即可：宗族（宗祠+族规）
    members   = 1,  -- 寒门即可：族人管理
    industry  = 1,  -- 寒门即可：经营（产业+库房）
    career    = 2,  -- 农户解锁：功业（仕途+书院+历练，取最低=书院2）
    events    = 1,  -- 寒门即可：事件
}

--- 子页签解锁要求
GameData.SUB_TAB_UNLOCK = {
    clan_main      = 1,  -- 寒门：宗祠
    clan_rules     = 2,  -- 农户：族规
    clan_chronicle = 1,  -- 寒门：家族志
    ind_main       = 1,  -- 寒门：产业
    ind_market     = 3,  -- 乡绅：集市
    ind_store      = 4,  -- 望族：库房
    car_career     = 3,  -- 乡绅：仕途
    car_academy    = 2,  -- 农户：书院
    car_expedition = 3,  -- 乡绅：历练
}

--- 检查子页签是否解锁
function GameData.IsSubTabUnlocked(subTabId)
    local s = GameData.state
    if not s then return false end
    local req = GameData.SUB_TAB_UNLOCK[subTabId]
    if not req then return true end
    return s.clanRank >= req
end

--- 书院/武馆解锁要求
GameData.ACADEMY_UNLOCK = {
    school = 2,  -- 农户解锁：族学
    wuguan = 3,  -- 乡绅解锁：武馆
}

--- 历练类型解锁要求
GameData.EXPEDITION_UNLOCK = {
    market      = 3,  -- 乡绅解锁：赶集贸易
    study_trip  = 3,  -- 乡绅解锁：游学求教
    trade_route = 5,  -- 世家解锁：行商远途
    explore     = 4,  -- 望族解锁：深山探秘
    recruit     = 4,  -- 望族解锁：招贤纳士
    sea_trade   = 6,  -- 勋贵解锁：海上通商
    court_visit = 6,  -- 勋贵解锁：进京面圣
    border_patrol = 7, -- 名门解锁：巡边戍守
    treasure_hunt = 7, -- 名门解锁：寻访古迹
    silk_road     = 8, -- 豪阀解锁：丝路远征
    tributary_mission = 9, -- 国柱解锁：万邦来朝
}

--- 族规可启用上限
GameData.RULES_MAX_BY_RANK = {
    [1] = 0,  -- 寒门：无族规
    [2] = 2,  -- 农户：2条
    [3] = 2,  -- 乡绅：2条
    [4] = 3,  -- 望族：3条
    [5] = 3,  -- 世家：3条
    [6] = 3,  -- 勋贵：3条
    [7] = 4,  -- 名门：4条
    [8] = 4,  -- 豪阀：4条
    [9] = 5,  -- 国柱：5条
}

--- 仕途子功能解锁
GameData.CAREER_UNLOCK = {
    exam     = 3,  -- 乡绅解锁：科举
    military = 3,  -- 乡绅解锁：从军（但世家前从军死亡率+20%）
    donate   = 4,  -- 望族解锁：纳捐
}

--- 品级提升时的解锁提示文本
GameData.RANK_UNLOCK_DESC = {
    [2] = {  -- 农户
        "族学（书院页面）：可安排族人读书深造",
        "水田产业：粮食产量更高（可从旱田进化）",
        "鱼塘产业：产出稳定，不受天灾影响",
        "畜栏产业：产粮食兼出少量布匹",
        "族规系统：可启用2条族规",
        "经商安排：族人可外出经商",
    },
    [3] = {  -- 乡绅
        "集市交易：买卖粮布药材铁器书籍马匹",
        "仕途系统：科举取士、从军报国",
        "武馆（书院页面）：习练武艺",
        "历练系统：赶集贸易、游学求教",
        "祭天祈福：占卜卦象、抉择命运，获取物品与资源",
        "商铺产业：经营店铺赚取银两（可进化为商号）",
        "茶园产业：银两兼声望双收",
        "客栈产业：稳定银两收益",
        "药铺产业：赚银两且降低全族生病概率",
        "书香门第联姻：配偶学识加成",
    },
    [4] = {  -- 望族
        "库房系统：收纳珍贵物品",
        "织坊产业：生产布匹（可进化为绸缎庄）",
        "寨堡建筑：防御流寇",
        "酒坊产业：以粮酿酒，利润丰厚",
        "铁匠铺产业：银两兼声望，提升武艺成长",
        "深山探秘、招贤纳士历练",
        "官宦世家联姻：声望大增",
        "纳捐功能：银两买监生身份",
        "族规上限提升至3条",
    },
    [5] = {  -- 世家
        "花魁大赛：选派族中女子参赛，三轮竞技争夺桂冠",
        "马场产业：声望收益，从军族人战力提升",
        "镖局产业：高收益但有折损风险",
        "书坊产业：声望银两双收，提升学识成长",
        "盐场产业：暴利但有官府稽查风险",
        "行商远途历练：高风险高收益远途贸易",
        "军户世家联姻：武艺加成",
        "产业进化：商铺→商号、织坊→绸缎庄",
    },
    [6] = {  -- 勋贵
        "天子诰封：承办朝廷差事，积累皇恩兑换封赏",
        "钱庄产业：放贷收息，按总资产额外生利",
        "船队产业：海上贸易，利润极高",
        "庄园产业：粮银名三收的终极产业",
    },
    [7] = {  -- 名门
        "当铺产业：典当质押，乱世暴利，按银两存量+1%收益",
        "染坊产业：染制锦缎彩绸，布匹银两双收",
        "私塾产业：开馆授徒，声名远播，全族学识+10%",
        "漕运码头：扼守漕运要道，南北货物中转抽成",
        "皇亲国戚联姻：皇室姻亲，声望飞涨，配偶全属性+20",
        "巡边戍守历练：率精锐巡视边关，军功换声望",
        "寻访古迹历练：探访前朝遗迹，寻觅传世珍宝",
        "名师授业/名将指点：高级培养，成长×2.5",
        "商队领队打工：率领商队远行，收入丰厚",
        "镖局可进化为漕运码头",
        "族规上限提升至4条，产业上限提升至25个",
    },
    [8] = {  -- 豪阀
        "军械坊产业：铸造兵器甲胄，从军族人战力+25%",
        "织造局产业：承接官府织造，布匹银两声望三收",
        "钱庄/当铺可进化为票号：汇通天下，总资产额外生利2%",
        "藩镇将门联姻：配偶武艺+35，赠送精兵50",
        "丝路远征历练：远赴西域，带回奇珍异宝",
        "矿监打工：监管矿山开采，收入极高",
        "高阶天灾应对：需调动更多资源赈灾",
        "敌对势力进攻：组织防御抵御外敌入侵",
        "产业上限提升至30个",
    },
    [9] = {  -- 国柱
        "皇商行产业：皇家特许经营，垄断盐铁茶叶，暴利产业",
        "海关行产业：把持海关贸易，坐收厘金，银两声望双收",
        "万亩良田产业：终极农业，粮银双收且不受天灾",
        "宰辅门第联姻：宰相之家，全族声望月产+5",
        "万邦来朝历练：代天子出使四方，声望名利双收",
        "全才培养：文武兼修，全属性成长×2.0",
        "税使打工：代征赋税，权势滔天",
        "盐场可进化为海关行，庄园可进化为万亩良田",
        "族规上限提升至5条，产业上限提升至40个",
        "恭喜！已达最高品级——国柱！",
    },
}

--- 检查某个标签页是否已解锁
function GameData.IsTabUnlocked(tabId)
    local s = GameData.state
    if not s then return false end
    local req = GameData.TAB_UNLOCK[tabId]
    if not req then return true end
    return s.clanRank >= req
end

--- 检查某个产业类型是否已解锁
function GameData.IsIndustryUnlocked(typeId)
    local s = GameData.state
    if not s then return false end
    local req = GameData.INDUSTRY_UNLOCK[typeId]
    if not req then return true end
    return s.clanRank >= req
end

--- 检查某个书院类型是否已解锁
function GameData.IsAcademyUnlocked(typeId)
    local s = GameData.state
    if not s then return false end
    local req = GameData.ACADEMY_UNLOCK[typeId]
    if not req then return true end
    return s.clanRank >= req
end

--- 检查某个历练类型是否已解锁
function GameData.IsExpeditionUnlocked(typeId)
    local s = GameData.state
    if not s then return false end
    local req = GameData.EXPEDITION_UNLOCK[typeId]
    if not req then return true end
    return s.clanRank >= req
end

--- 检查某个联姻门第是否已解锁
function GameData.IsMarriageUnlocked(tierId)
    local s = GameData.state
    if not s then return false end
    local req = GameData.MARRIAGE_UNLOCK[tierId]
    if not req then return true end
    return s.clanRank >= req
end

--- 获取当前品级允许的族规上限
function GameData.GetMaxClanRules()
    local s = GameData.state
    if not s then return 0 end
    return GameData.RULES_MAX_BY_RANK[s.clanRank] or 0
end

--- 获取解锁某功能所需的品级名称
function GameData.GetUnlockRankName(reqRank)
    return GameData.CLAN_RANKS[reqRank] or "?"
end

-- ============================================================================
-- 游戏状态
-- ============================================================================

---@class GameState
local DefaultState = {
    -- 基本信息
    surname = "李",
    originId = "farmer",
    regionId = "shaanbei",
    clanName = "",
    clanRank = 1,       -- 宗族品级索引

    -- 时间
    year = 1368,
    month = 1,
    totalMonths = 0,

    -- 四大资源
    silver = 30,
    grain = 80,
    cloth = 20,
    fame = 5,

    -- 族人列表（树形结构扁平化存储）
    members = {},
    nextMemberId = 1,
    patriarchId = nil,  -- 当前族长ID

    -- 产业列表
    industries = {},
    nextIndustryId = 1,

    -- 寨堡数量
    fortCount = 0,

    -- 族规
    clanRules = {},

    -- 家训（开局选择，永久buff）
    familyMottoId = nil,

    -- 难度
    difficultyId = "normal",

    -- 书院/武馆
    academies = {},  -- { { typeId, level, memberIds={} }, ... }

    -- 探索历练
    expeditions = {},  -- { { typeId, memberId, monthsLeft, startYear, startMonth }, ... }

    -- 集市交易
    market = nil,  -- MarketSystem.InitState 会初始化

    -- 库房物品
    inventory = {},  -- { { itemId, count }, ... }

    -- 事件日志
    eventLog = {},

    -- 已触发的历史事件年份
    triggeredHistoryYears = {},

    -- 待处理事件
    pendingEvents = {},

    -- 连锁事件（上月某事件触发的后续，下月自动处理）
    pendingChainEvent = nil,

    -- 已解锁成就ID列表
    unlockedAchievements = {},

    -- 年度目标系统
    yearlyGoals = {},           -- 当年可选目标 { {goalId, desc, reward, check, completed}, ... }
    completedGoalIds = {},      -- 历史已完成目标ID列表
    goalYear = 0,               -- 当前目标对应的年份（避免重复生成）

    -- 统计
    totalBirths = 0,
    totalDeaths = 0,
    totalExamPasses = 0,
    totalMilitaryMerits = 0,

    -- 年度统计（用于年终总结）
    yearStats = nil,            -- 当年累积数据 { incomes, expenses, births, deaths, examPasses, militaryMerits, majorEvents }
    yearStartSnapshot = nil,    -- 年初快照 { silver, grain, cloth, fame, aliveCount, clanRank, industryCount, fortCount }

    -- 终局
    gameEnded = false,
    endingChoice = nil,
    hiddenEnding = nil,         -- 隐藏结局ID（如果触发了隐藏结局）
    triggeredHiddenEndings = {},-- 已触发过的隐藏结局ID集合（选择继续后不再重复触发）

    -- 军队系统（征伐专用，独立于从军族人）
    army = {
        infantry = 0,       -- 步兵数量
        archers = 0,        -- 弓兵数量
        trainingLevel = 0,  -- 训练等级 0-5
    },
    conqueredRegions = {},  -- 已征服的区域ID列表（旧版兼容）
    conqueredStages = {},   -- 已征服的关卡ID列表（新版：stageId = areaId*100+stageNum）

    -- 宠物（单只，纯装饰，消耗粮食）
    pet = nil,  -- { type="dog", name="大黄", adoptYear=1370, adoptMonth=3, lifespan=12, alive=true, deathYear=nil, deathMonth=nil }

    -- 祭祀记录（每年只能祭祀一次）
    lastSacrificeYear = 0,
}

-- 当前游戏状态
GameData.state = nil

--- 获取默认状态的深拷贝（用于存档迁移补齐缺失字段）
function GameData.GetDefaultState()
    local function deepCopy(orig)
        if type(orig) ~= "table" then return orig end
        local copy = {}
        for k, v in pairs(orig) do
            copy[k] = deepCopy(v)
        end
        return copy
    end
    return deepCopy(DefaultState)
end

-- ============================================================================
-- 初始化
-- ============================================================================

function GameData.NewGame(surname, originId, regionId, mottoId, difficultyId)
    local origin = nil
    for _, o in ipairs(GameData.ORIGINS) do
        if o.id == originId then origin = o; break end
    end
    if not origin then origin = GameData.ORIGINS[1] end

    local function deepCopy(orig)
        if type(orig) ~= "table" then return orig end
        local copy = {}
        for k2, v2 in pairs(orig) do copy[k2] = deepCopy(v2) end
        return copy
    end
    local state = {}
    for k, v in pairs(DefaultState) do
        state[k] = deepCopy(v)
    end

    state.surname = surname
    state.originId = originId
    state.regionId = regionId
    state.clanName = surname .. "氏宗族"
    state.silver = origin.silver
    state.grain = origin.grain
    state.cloth = origin.cloth
    state.fame = origin.fame or 5  -- 初始声望来自出身

    -- 家训
    state.familyMottoId = mottoId or "study"
    -- 家训初始银两加成
    local motto = GameData.GetMottoById(mottoId or "study")
    if motto and motto.effects.initSilverBonus then
        state.silver = state.silver + motto.effects.initSilverBonus
    end

    -- 难度
    state.difficultyId = difficultyId or "normal"

    -- 初始化书院（农户以上才有族学）
    state.academies = {}
    if origin.id == "landlord" then
        -- 小地主出身起步高，给族学
        state.academies = { { typeId = "school", level = 1, memberIds = {} } }
    end

    GameData.state = state

    -- 生成初始族人
    GameData.GenerateInitialMembers(origin)

    -- 生成初始产业
    GameData.GenerateInitialIndustries(originId)

    return state
end

-- ============================================================================
-- 族人管理
-- ============================================================================

function GameData.CreateMember(opts)
    local s = GameData.state
    local id = s.nextMemberId
    s.nextMemberId = id + 1

    local member = {
        id = id,
        name = opts.name or (s.surname .. GameData.RandomGivenName(opts.gender)),
        gender = opts.gender or (math.random() > 0.5 and "male" or "female"),
        age = opts.age or 0,
        generation = opts.generation or 1,
        parentId = opts.parentId or nil,
        spouseId = opts.spouseId or nil,
        childrenIds = {},
        identity = opts.identity or "白丁",
        state = opts.state or "在家",
        talent = opts.talent or nil,
        study = opts.study or 1,          -- 学识 0-100（新生儿默认1）
        martial = opts.martial or 1,      -- 武艺 0-100（新生儿默认1）
        health = opts.health or 100,      -- 健康 0-100
        militaryRank = opts.militaryRank or nil,
        assignment = opts.assignment or nil, -- 当前分配的产业id
        aptitude = opts.aptitude or MemberData.GenerateAptitude(), -- 资质（终身属性上限）
        alive = true,
    }

    -- 随机天赋（30%概率）
    if not member.talent and math.random() < 0.3 then
        member.talent = GameData.TALENTS[math.random(1, #GameData.TALENTS)]
    end

    s.members[#s.members + 1] = member
    return member
end

function GameData.GetMember(id)
    if not GameData.state then return nil end
    for _, m in ipairs(GameData.state.members) do
        if m.id == id then return m end
    end
    return nil
end

function GameData.GetAliveMembers()
    local result = {}
    if not GameData.state then return result end
    for _, m in ipairs(GameData.state.members) do
        if m.alive then result[#result + 1] = m end
    end
    return result
end

function GameData.GetAdultMembers()
    local result = {}
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.age >= 15 then result[#result + 1] = m end
    end
    return result
end

function GameData.GetIdleMembers()
    local result = {}
    for _, m in ipairs(GameData.GetAdultMembers()) do
        if m.state == "在家" then result[#result + 1] = m end
    end
    return result
end

--- 获取可分配管理产业的族人（在家/读书/从军/经商均可兼职管理）
function GameData.GetAssignableMembers()
    local assignable = { ["在家"] = true, ["读书"] = true, ["从军"] = true, ["经商"] = true }
    local result = {}
    for _, m in ipairs(GameData.GetAdultMembers()) do
        if assignable[m.state] then result[#result + 1] = m end
    end
    return result
end

function GameData.GetMembersByState(stateName)
    local result = {}
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.state == stateName then result[#result + 1] = m end
    end
    return result
end

function GameData.GenerateInitialMembers(origin)
    local s = GameData.state

    -- 族长（创始人）- 单人开局，娶妻生子靠后续发展
    -- 属性随机 10-30，出身给予小幅偏向加成
    local baseStudy = 10 + math.random(0, 20)
    local baseMartial = 10 + math.random(0, 20)
    if origin.id == "military" then
        baseMartial = math.min(30, baseMartial + 5)  -- 军户武艺略高
    elseif origin.id == "landlord" then
        baseStudy = math.min(30, baseStudy + 5)      -- 地主学识略高
    end
    local patriarch = GameData.CreateMember({
        gender = "male",
        age = 20 + math.random(0, 5),
        generation = 1,
        identity = origin.id == "military" and "士兵" or "白丁",
        state = "在家",
        martial = baseMartial,
        study = baseStudy,
        health = 85 + math.random(0, 15),
    })
    s.patriarchId = patriarch.id
end

-- ============================================================================
-- 产业管理
-- ============================================================================

function GameData.GenerateInitialIndustries(originId)
    if originId == "farmer" then
        GameData.AddIndustry("dry_field")
    elseif originId == "landlord" then
        GameData.AddIndustry("dry_field")
        GameData.AddIndustry("paddy_field")
        GameData.AddIndustry("shop")
    elseif originId == "military" then
        GameData.AddIndustry("dry_field")
        GameData.AddIndustry("dry_field")
    end
end

function GameData.AddIndustry(typeId)
    local s = GameData.state
    local indType = nil
    for _, t in ipairs(GameData.INDUSTRY_TYPES) do
        if t.id == typeId then indType = t; break end
    end
    if not indType then return nil end

    -- A1: 产业数量上限检查
    local limit = GameData.INDUSTRY_LIMIT_BY_RANK[s.clanRank] or 4
    if #s.industries >= limit then
        return nil, "产业已达上限（" .. limit .. "个），需提升品级"
    end

    local id = s.nextIndustryId
    s.nextIndustryId = id + 1

    local industry = {
        id = id,
        typeId = typeId,
        level = 1,
        assignedMemberId = nil,
    }
    s.industries[#s.industries + 1] = industry

    if typeId == "fort" then
        s.fortCount = s.fortCount + 1
    end

    return industry
end

--- 变卖产业，回收建造成本的50%
--- @return boolean success, string message
function GameData.SellIndustry(industryId)
    local s = GameData.state
    if not s then return false, "无存档" end

    local idx = nil
    local ind = nil
    for i, v in ipairs(s.industries) do
        if v.id == industryId then
            idx = i
            ind = v
            break
        end
    end
    if not ind then return false, "产业不存在" end

    local indType = GameData.GetIndustryType(ind.typeId)
    if not indType then return false, "产业类型未知" end

    -- 回收价 = (建造成本 + 累计升级费) * 50%
    local totalInvest = indType.cost
    for lv = 1, ind.level - 1 do totalInvest = totalInvest + indType.cost * lv end
    local refund = math.floor(totalInvest * 0.5)

    -- 寨堡计数
    if ind.typeId == "fort" then
        s.fortCount = math.max(0, s.fortCount - 1)
    end

    -- 移除产业
    table.remove(s.industries, idx)

    -- 回收银两
    GameData.AddResource("silver", refund)
    GameData.AddLog("变卖" .. indType.name .. "，回收银两" .. refund)

    return true, "变卖" .. indType.name .. "，获得银两" .. refund
end

--- 检查产业是否可以进化
--- @return boolean canEvolve, string|nil reason
function GameData.CanEvolveIndustry(industryId)
    local s = GameData.state
    if not s then return false, "无存档" end
    local ind = nil
    for _, i in ipairs(s.industries) do
        if i.id == industryId then ind = i; break end
    end
    if not ind then return false, "产业不存在" end

    local evo = GameData.INDUSTRY_EVOLUTION[ind.typeId]
    if not evo then return false, "该产业无法进化" end
    if ind.level < evo.reqLevel then
        return false, "需要产业等级" .. evo.reqLevel .. "（当前" .. ind.level .. "级）"
    end
    if s.clanRank < evo.reqRank then
        return false, "需要品级【" .. GameData.CLAN_RANKS[evo.reqRank] .. "】"
    end
    if s.silver < evo.cost then
        return false, "需要银两" .. evo.cost .. "（当前" .. s.silver .. "）"
    end
    return true, nil
end

--- 执行产业进化（原地替换为高级产业，保留等级和管理人）
function GameData.EvolveIndustry(industryId)
    local canEvolve, reason = GameData.CanEvolveIndustry(industryId)
    if not canEvolve then return false, reason end

    local s = GameData.state
    local ind = nil
    for _, i in ipairs(s.industries) do
        if i.id == industryId then ind = i; break end
    end

    local evo = GameData.INDUSTRY_EVOLUTION[ind.typeId]
    local oldType = GameData.GetIndustryType(ind.typeId)
    local newType = GameData.GetIndustryType(evo.to)

    -- 扣费
    s.silver = s.silver - evo.cost

    -- 原地进化：替换typeId，保留等级和管理人
    local oldName = oldType and oldType.name or ind.typeId
    ind.typeId = evo.to

    local newName = newType and newType.name or evo.to
    GameData.AddLog(oldName .. "进化为【" .. newName .. "】！")

    return true, oldName .. " → " .. newName
end

-- ============================================================================
-- 资源操作
-- ============================================================================

function GameData.AddResource(name, amount)
    local s = GameData.state
    if s[name] ~= nil then
        s[name] = s[name] + amount
    end
end

function GameData.CanAfford(silver, grain, cloth, fame)
    local s = GameData.state
    silver = silver or 0
    grain = grain or 0
    cloth = cloth or 0
    fame = fame or 0
    return (silver <= 0 or s.silver >= silver)
        and (grain <= 0 or s.grain >= grain)
        and (cloth <= 0 or s.cloth >= cloth)
        and (fame <= 0 or s.fame >= fame)
end

function GameData.SpendResources(silver, grain, cloth, fame)
    if not GameData.CanAfford(silver, grain, cloth, fame) then return false end
    local s = GameData.state
    s.silver = s.silver - (silver or 0)
    s.grain = s.grain - (grain or 0)
    s.cloth = s.cloth - (cloth or 0)
    s.fame = s.fame - (fame or 0)
    return true
end

--- 强制花费资源，允许扣成负数（用于紧急事件等不可避免的场景）
---@param silver number|nil
---@param grain number|nil
---@param cloth number|nil
---@param fame number|nil
function GameData.ForceSpend(silver, grain, cloth, fame)
    local s = GameData.state
    s.silver = s.silver - (silver or 0)
    s.grain = s.grain - (grain or 0)
    s.cloth = s.cloth - (cloth or 0)
    s.fame = s.fame - (fame or 0)
end

--- 尝试花费资源，不足时自动弹出 Toast 提示并返回 false
--- 用于事件选项中的可选消费（如"花银两做某事"）
---@param silver number|nil
---@param grain number|nil
---@param cloth number|nil
---@param fame number|nil
---@return boolean success
function GameData.TrySpend(silver, grain, cloth, fame)
    silver = silver or 0
    grain = grain or 0
    cloth = cloth or 0
    fame = fame or 0
    if GameData.CanAfford(silver, grain, cloth, fame) then
        local s = GameData.state
        if silver > 0 then s.silver = s.silver - silver end
        if grain > 0 then s.grain = s.grain - grain end
        if cloth > 0 then s.cloth = s.cloth - cloth end
        if fame > 0 then s.fame = s.fame - fame end
        return true
    end
    -- 提示最缺的资源
    local Toast = require("Systems.Toast")
    local s = GameData.state
    if silver > 0 and s.silver < silver then Toast.NotEnough("银两")
    elseif grain > 0 and s.grain < grain then Toast.NotEnough("粮食")
    elseif cloth > 0 and s.cloth < cloth then Toast.NotEnough("布匹")
    elseif fame > 0 and s.fame < fame then Toast.NotEnough("声望")
    end
    return false
end

-- ============================================================================
-- 获取当前地域信息
-- ============================================================================

function GameData.GetRegion()
    if not GameData.state then return GameData.REGIONS[1] end
    for _, r in ipairs(GameData.REGIONS) do
        if r.id == GameData.state.regionId then return r end
    end
    return GameData.REGIONS[1]
end

function GameData.GetClanRankName()
    if not GameData.state then return "寒门" end
    return GameData.CLAN_RANKS[GameData.state.clanRank] or "寒门"
end

-- ============================================================================
-- 事件日志
-- ============================================================================

function GameData.AddLog(text)
    local s = GameData.state
    local entry = {
        year = s.year,
        month = s.month,
        text = text,
    }
    table.insert(s.eventLog, 1, entry) -- 最新的在最前
    if #s.eventLog > 100 then
        s.eventLog[#s.eventLog] = nil
    end
end

-- ============================================================================
-- 族规管理
-- ============================================================================

function GameData.IsClanRuleActive(ruleId)
    local s = GameData.state
    if not s or not s.clanRules then return false end
    for _, id in ipairs(s.clanRules) do
        if id == ruleId then return true end
    end
    return false
end

function GameData.ToggleClanRule(ruleId)
    local s = GameData.state
    if not s then return end
    if not s.clanRules then s.clanRules = {} end

    for i, id in ipairs(s.clanRules) do
        if id == ruleId then
            table.remove(s.clanRules, i)
            return false  -- 已关闭
        end
    end
    -- 最多启用品级允许的族规数
    local maxRules = GameData.GetMaxClanRules()
    if #s.clanRules >= maxRules then return nil end  -- 超出上限
    s.clanRules[#s.clanRules + 1] = ruleId
    return true  -- 已启用
end

--- 获取所有激活族规的效果合集
function GameData.GetClanRuleEffects()
    local combined = {}
    local s = GameData.state
    if not s or not s.clanRules then return combined end
    for _, ruleId in ipairs(s.clanRules) do
        for _, rule in ipairs(GameData.CLAN_RULES) do
            if rule.id == ruleId then
                for k, v in pairs(rule.effects) do
                    combined[k] = (combined[k] or 0) + v
                end
                break
            end
        end
    end
    return combined
end

-- ============================================================================
-- 死亡通知系统：推送弹窗让玩家知晓族人去世
-- ============================================================================

function GameData.NotifyDeath(member, cause, detail)
    local s = GameData.state
    if not s or not member then return end
    -- 弹窗队列满时不推送（避免堆积过多）
    if #s.pendingEvents >= 5 then return end
    -- 婴儿夭折用简短通知
    local ageText = member.age <= 2
        and ("年仅" .. member.age .. "岁")
        or ("享年" .. member.age .. "岁")
    local desc = member.name .. "（" .. ageText .. "）" .. cause .. "。"
    if detail then desc = desc .. "\n" .. detail end
    s.pendingEvents[#s.pendingEvents + 1] = {
        title = "族人讣告",
        desc = desc,
        type = "notification",
        choices = {
            { text = "节哀顺变", effect = function() end },
        },
    }
    -- 紧接着推送葬礼事件（让玩家在讣告后选择是否举办葬礼）
    s.pendingEvents[#s.pendingEvents + 1] = {
        title  = "葬礼",
        desc   = "",
        type   = "funeral",
        member = member,  -- 保存逝者引用
    }
end

-- ============================================================================
-- 族长系统
-- ============================================================================

--- 获取当前族长
---@return table|nil
function GameData.GetPatriarch()
    local s = GameData.state
    if not s then return nil end
    -- 优先使用 patriarchId
    if s.patriarchId then
        local p = GameData.GetMember(s.patriarchId)
        if p and p.alive then return p end
    end
    -- 兼容旧存档：找第一代男性
    for _, m in ipairs(s.members) do
        if m.alive and m.generation == 1 and m.gender == "male" then
            s.patriarchId = m.id
            return m
        end
    end
    -- 找第一代任意活人
    for _, m in ipairs(s.members) do
        if m.alive and m.generation == 1 then
            s.patriarchId = m.id
            return m
        end
    end
    -- 最后兜底：任意活人
    local alive = GameData.GetAliveMembers()
    if #alive > 0 then
        s.patriarchId = alive[1].id
        return alive[1]
    end
    return nil
end

--- 判断某成员是否是族长
function GameData.IsPatriarch(memberId)
    local p = GameData.GetPatriarch()
    return p and p.id == memberId
end

--- 获取可继承族长之位的候选人列表（年满16岁，按优先级排序）
--- 优先级：族长子女男性 > 族长子女女性 > 其他男性 > 其他女性
---@param deadPatriarchId number 去世族长的ID
---@return table[] 候选人列表
function GameData.GetSuccessionCandidates(deadPatriarchId)
    local alive = GameData.GetAliveMembers()
    local children = {}
    local others = {}

    for _, m in ipairs(alive) do
        if m.age >= 16 then
            if m.parentId == deadPatriarchId then
                children[#children + 1] = m
            else
                others[#others + 1] = m
            end
        end
    end

    -- 排序：男性优先，同性别按年龄降序（年长优先）
    local function sortByPriority(a, b)
        if a.gender ~= b.gender then
            return a.gender == "male"
        end
        return a.age > b.age
    end
    table.sort(children, sortByPriority)
    table.sort(others, sortByPriority)

    -- 合并：子女优先
    local result = {}
    for _, m in ipairs(children) do result[#result + 1] = m end
    for _, m in ipairs(others) do result[#result + 1] = m end
    return result
end

--- 设置新族长
function GameData.SetPatriarch(memberId)
    local s = GameData.state
    if not s then return end
    s.patriarchId = memberId
    local m = GameData.GetMember(memberId)
    if m then
        GameData.AddLog(m.name .. "继任族长之位。")
    end
end

--- 检查死者是否是族长，如果是则推送继承事件到 pendingEvents
--- 返回 true 表示需要继承
function GameData.CheckPatriarchDeath(deadMember)
    local s = GameData.state
    if not s then return false end
    if s.patriarchId ~= deadMember.id then return false end

    local candidates = GameData.GetSuccessionCandidates(deadMember.id)
    if #candidates == 0 then
        -- 无继承人（全族灭亡边缘）
        s.patriarchId = nil
        return false
    end

    if #candidates == 1 then
        -- 只有一个候选人，自动继承
        GameData.SetPatriarch(candidates[1].id)
        return false
    end

    -- 多个候选人，推送继承事件让玩家选择
    s.pendingEvents[#s.pendingEvents + 1] = {
        title = "族长继承",
        desc = deadMember.name .. "已故，族长之位空悬。\n请从族中选择新任族长。",
        type = "succession",
        deadPatriarchId = deadMember.id,
        choices = {},  -- 继承弹窗由 ShowSuccessionDialog 处理
    }
    return true
end

-- ============================================================================
-- 统计辅助
-- ============================================================================

function GameData.GetMerchantMembers()
    local result = {}
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.state == "经商" then result[#result + 1] = m end
    end
    return result
end

function GameData.GetStudyingMembers()
    local result = {}
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.state == "读书" then result[#result + 1] = m end
    end
    return result
end

-- ============================================================================
-- 家训系统
-- ============================================================================

function GameData.GetMottoById(mottoId)
    for _, m in ipairs(GameData.FAMILY_MOTTOS) do
        if m.id == mottoId then return m end
    end
    return nil
end

function GameData.GetMottoEffects()
    local s = GameData.state
    if not s or not s.familyMottoId then return {} end
    local motto = GameData.GetMottoById(s.familyMottoId)
    if not motto then return {} end
    return motto.effects
end

function GameData.GetDifficulty()
    local s = GameData.state
    if not s then return GameData.DIFFICULTIES[2] end  -- normal
    for _, d in ipairs(GameData.DIFFICULTIES) do
        if d.id == s.difficultyId then return d end
    end
    return GameData.DIFFICULTIES[2]
end

-- ============================================================================
-- 书院/武馆管理
-- ============================================================================

function GameData.GetAcademy(typeId)
    local s = GameData.state
    if not s then return nil end
    for _, a in ipairs(s.academies) do
        if a.typeId == typeId then return a end
    end
    return nil
end

function GameData.GetAcademyType(typeId)
    for _, t in ipairs(GameData.ACADEMY_TYPES) do
        if t.id == typeId then return t end
    end
    return nil
end

function GameData.GetAcademySlots(academy)
    local aType = GameData.GetAcademyType(academy.typeId)
    if not aType then return 0 end
    return aType.baseSlotsPerLevel * academy.level
end

function GameData.AddAcademy(typeId)
    local s = GameData.state
    if not s then return nil end
    -- 检查是否已存在
    for _, a in ipairs(s.academies) do
        if a.typeId == typeId then return a end
    end
    local academy = { typeId = typeId, level = 1, memberIds = {} }
    s.academies[#s.academies + 1] = academy
    return academy
end

function GameData.AssignToAcademy(typeId, memberId)
    local academy = GameData.GetAcademy(typeId)
    if not academy then return false end
    local maxSlots = GameData.GetAcademySlots(academy)
    -- 清理已死亡或不存在的成员
    local validIds = {}
    for _, id in ipairs(academy.memberIds) do
        local m = GameData.GetMember(id)
        if m and m.alive then validIds[#validIds + 1] = id end
    end
    academy.memberIds = validIds
    if #academy.memberIds >= maxSlots then return false end
    -- 检查是否已在该设施
    for _, id in ipairs(academy.memberIds) do
        if id == memberId then return false end
    end
    academy.memberIds[#academy.memberIds + 1] = memberId
    return true
end

function GameData.RemoveFromAcademy(typeId, memberId)
    local academy = GameData.GetAcademy(typeId)
    if not academy then return end
    for i, id in ipairs(academy.memberIds) do
        if id == memberId then
            table.remove(academy.memberIds, i)
            return
        end
    end
end

function GameData.IsInAcademy(memberId)
    local s = GameData.state
    if not s then return nil end
    for _, a in ipairs(s.academies) do
        for _, id in ipairs(a.memberIds) do
            if id == memberId then return a.typeId end
        end
    end
    return nil
end

-- ============================================================================
-- 探索历练管理
-- ============================================================================

function GameData.StartExpedition(typeId, memberId)
    local s = GameData.state
    if not s then return false end
    local expType = nil
    for _, t in ipairs(GameData.EXPEDITION_TYPES) do
        if t.id == typeId then expType = t; break end
    end
    if not expType then return false end
    -- 检查资源
    local cost = expType.cost
    if not GameData.CanAfford(cost.silver or 0, cost.grain or 0, 0, 0) then return false end
    GameData.SpendResources(cost.silver or 0, cost.grain or 0, 0, 0)
    -- 设置族人状态
    local member = GameData.GetMember(memberId)
    if member then member.state = "历练" end
    -- 记录探索
    s.expeditions[#s.expeditions + 1] = {
        typeId = typeId,
        memberId = memberId,
        monthsLeft = expType.duration,
        startYear = s.year,
        startMonth = s.month,
    }
    return true
end

function GameData.GetMemberExpedition(memberId)
    local s = GameData.state
    if not s then return nil end
    for _, exp in ipairs(s.expeditions) do
        if exp.memberId == memberId then return exp end
    end
    return nil
end

-- ============================================================================
-- 库房管理
-- ============================================================================

function GameData.AddItem(itemId, count)
    local s = GameData.state
    if not s then return end
    count = count or 1
    for _, slot in ipairs(s.inventory) do
        if slot.itemId == itemId then
            slot.count = slot.count + count
            return
        end
    end
    s.inventory[#s.inventory + 1] = { itemId = itemId, count = count }
end

function GameData.RemoveItem(itemId, count)
    local s = GameData.state
    if not s then return false end
    count = count or 1
    for i, slot in ipairs(s.inventory) do
        if slot.itemId == itemId then
            if slot.count < count then return false end
            slot.count = slot.count - count
            if slot.count <= 0 then table.remove(s.inventory, i) end
            return true
        end
    end
    return false
end

function GameData.GetItemCount(itemId)
    local s = GameData.state
    if not s then return 0 end
    for _, slot in ipairs(s.inventory) do
        if slot.itemId == itemId then return slot.count end
    end
    return 0
end

function GameData.GetItemType(itemId)
    for _, t in ipairs(GameData.ITEM_TYPES) do
        if t.id == itemId then return t end
    end
    return nil
end

-- ============================================================================
-- 马匹系统
-- 族人可购买战马，出征时使用骑马模型，移速加成
-- ============================================================================

--- 给族人购买马匹
function GameData.BuyHorse(memberId)
    local s = GameData.state
    if not s then return false, "无存档" end
    local member = GameData.GetMember(memberId)
    if not member then return false, "族人不存在" end
    if not member.alive then return false, "族人已故" end
    if member.hasHorse then return false, "已有战马" end
    if not GameData.CanAfford(GameData.HORSE_COST.silver, 0, 0, 0) then
        return false, "银两不足（需" .. GameData.HORSE_COST.silver .. "两）"
    end
    GameData.SpendResources(GameData.HORSE_COST.silver, 0, 0, 0)
    member.hasHorse = true
    GameData.AddLog(member.name .. "购得战马一匹")
    return true, member.name .. "购得战马"
end

--- 出售族人马匹
function GameData.SellHorse(memberId)
    local s = GameData.state
    if not s then return false end
    local member = GameData.GetMember(memberId)
    if not member or not member.hasHorse then return false end
    member.hasHorse = false
    local refund = math.floor(GameData.HORSE_COST.silver * 0.5)
    GameData.AddResource("silver", refund)
    GameData.AddLog(member.name .. "出售战马，回收银两" .. refund)
    return true
end

function GameData.UseItem(itemId, member)
    local iType = GameData.GetItemType(itemId)
    if not iType then return false, "物品不存在" end
    if not GameData.RemoveItem(itemId, 1) then return false, "库存不足" end

    if iType.useEffect == "heal" then
        if member then
            member.health = math.min(100, member.health + 30)
            if member.state == "生病" then member.state = member.prevState or "在家"; member.prevState = nil end
            return true, member.name .. "服药后康复"
        end
    elseif iType.useEffect == "study_boost" then
        if member then
            member.study = math.min(100, member.study + 3)
            return true, member.name .. "研读典籍，学识+3"
        end
    elseif iType.useEffect == "martial_boost" then
        if member then
            member.martial = math.min(100, member.martial + 3)
            return true, member.name .. "习练兵器，武艺+3"
        end
    elseif iType.useEffect == "martial_boost_large" then
        if member then
            member.martial = math.min(100, member.martial + 5)
            return true, member.name .. "研读兵法，武艺+5"
        end
    elseif iType.useEffect == "sell" then
        GameData.AddResource("silver", 250)
        return true, "变卖珍玩，获银两250"
    elseif iType.useEffect == "fame_boost" then
        GameData.AddResource("fame", 15)
        return true, "展示印信，声望+15"
    elseif iType.useEffect == "morale_boost" then
        GameData.AddResource("fame", 20)
        return true, "传家宝鼓舞全族，声望+20"
    end
    return false, "无法使用"
end

return GameData
