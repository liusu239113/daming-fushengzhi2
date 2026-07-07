-- ============================================================================
-- 大明浮生志2 - 敌对宗族数据模块
-- AI生成的敌方宗族：名称、兵力、成员、实力等级
-- 讨伐系统在宗族品级达到"世家"(rank 5)时解锁
-- ============================================================================

local GameData = require("Data.GameData")
local EquipmentSystem = require("Systems.EquipmentSystem")

local RivalClans = {}

-- ============================================================================
-- 敌族姓氏和名称池
-- ============================================================================

local RIVAL_SURNAMES = {
    "马", "孙", "韩", "徐", "曹", "魏", "吕", "沈", "冯", "郑",
    "蒋", "唐", "贺", "邓", "萧", "田", "程", "袁", "于", "董",
    "霍", "范", "彭", "鲁", "任", "姜", "柳", "尹", "秦", "顾",
    "苏", "严", "薛", "闫", "白", "孟", "侯", "龚", "段", "雷",
    "武", "康", "贺", "乔", "赖", "庞", "樊", "甘", "柏", "阎",
}

local CLAN_SUFFIXES = { "氏", "家", "堡", "寨", "庄" }

local RIVAL_GIVEN_NAMES_MALE = {
    -- 武勇
    "虎", "豹", "彪", "龙", "猛", "刚", "坚", "毅", "烈", "威",
    "勇", "强", "铁", "钢", "石", "山", "峰", "岩", "崖", "雷",
    -- 扩充
    "霆", "鸿", "昆", "熊", "飞", "锋", "剑", "戟", "骁", "悍",
    "豪", "壮", "魁", "硕", "洪", "泽", "凯", "啸", "震", "翔",
    "弘", "义", "忠", "信", "杰", "俊", "良", "才", "栋", "柱",
}

local RIVAL_GIVEN_NAMES_FEMALE = {
    "英", "娘", "翠", "凤", "鸾", "霜", "燕", "莺", "珠", "环",
    "娥", "姑", "嫂", "妹", "姐", "婶", "素", "云", "月", "花",
    "春", "秋", "红", "绿", "碧", "玲", "珑", "香", "惠", "贞",
}

local RIVAL_TITLES = {
    "寨主", "庄主", "族长", "堡主", "当家",
}

local RIVAL_TITLES_FEMALE = {
    "女寨主", "当家娘子", "女族长", "堡主夫人", "大当家",
}

-- ============================================================================
-- 品阶缩放表（兵力和属性随品阶指数增长）
-- 征伐在品阶5解锁，品阶5为基准倍率1.0
-- ============================================================================

RivalClans.RANK_SCALING = {
    [5] = { soldierMul = 1.0,  statBonus = 0,  memberExtra = 0, rewardMul = 1.0 },
    [6] = { soldierMul = 3.0,  statBonus = 8,  memberExtra = 1, rewardMul = 2.0 },
    [7] = { soldierMul = 6.0,  statBonus = 10, memberExtra = 2, rewardMul = 4.0 },
    [8] = { soldierMul = 12.0, statBonus = 15, memberExtra = 3, rewardMul = 7.0 },
    [9] = { soldierMul = 22.0, statBonus = 20, memberExtra = 4, rewardMul = 12.0 },
}

-- ============================================================================
-- 敌族难度等级
-- ============================================================================

RivalClans.DIFFICULTY_TIERS = {
    { id = "easy",   name = "弱敌",   color = { 100, 200, 100, 255 },
      memberRange = { 2, 3 }, soldierRange = { 100, 300 },
      martialRange = { 15, 35 }, healthRange = { 50, 70 },
      rewardMul = 0.8, desc = "乌合之众，不堪一击" },
    { id = "normal", name = "劲敌",   color = { 220, 180, 50, 255 },
      memberRange = { 3, 5 }, soldierRange = { 300, 600 },
      martialRange = { 30, 55 }, healthRange = { 60, 80 },
      rewardMul = 1.0, desc = "旗鼓相当，需谨慎应对" },
    { id = "hard",   name = "强敌",   color = { 220, 80, 60, 255 },
      memberRange = { 4, 6 }, soldierRange = { 500, 1000 },
      martialRange = { 45, 75 }, healthRange = { 70, 90 },
      rewardMul = 1.5, desc = "实力强劲，胜负难料" },
    { id = "elite",  name = "宿敌",   color = { 180, 80, 220, 255 },
      memberRange = { 5, 6 }, soldierRange = { 800, 1500 },
      martialRange = { 60, 90 }, healthRange = { 80, 95 },
      rewardMul = 2.0, desc = "称霸一方的豪强" },
}

-- ============================================================================
-- 战斗奖励模板
-- ============================================================================

RivalClans.BASE_REWARDS = {
    silver = { 20, 40 },
    grain  = { 15, 30 },
    fame   = { 5, 12 },
}

-- ============================================================================
-- 生成敌族
-- ============================================================================

--- 随机生成一个敌族成员
---@param surname string 姓氏
---@param tier table 难度等级
---@param isLeader boolean
---@param memberIndex number 成员序号（用于头像分配）
---@param rankScale table|nil 品阶缩放数据
---@return table member
local function GenerateRivalMember(surname, tier, isLeader, memberIndex, rankScale)
    -- 征伐限定男性（敌方出战成员均为男性）
    local gender = "male"

    local givenPool = RIVAL_GIVEN_NAMES_MALE
    local givenName = givenPool[math.random(1, #givenPool)]
    -- 40%概率双字名
    if math.random() < 0.4 then
        local n2 = givenPool[math.random(1, #givenPool)]
        if n2 ~= givenName then givenName = givenName .. n2 end
    end
    local name = surname .. givenName

    local martial = math.random(tier.martialRange[1], tier.martialRange[2])
    local health = math.random(tier.healthRange[1], tier.healthRange[2])

    -- 品阶属性加成
    local statBonus = rankScale and rankScale.statBonus or 0
    martial = math.min(100, martial + statBonus)
    health = math.min(100, health + statBonus)

    -- 首领属性更高
    if isLeader then
        martial = math.min(100, martial + 15)
        health = math.min(100, health + 10)
    end

    -- 年龄：首领偏大，其余随机
    local age
    if isLeader then
        age = math.random(35, 55)
    else
        age = math.random(18, 50)
    end

    local titlePool = RIVAL_TITLES
    return {
        name = name,
        gender = gender,
        age = age,
        title = isLeader and titlePool[math.random(1, #titlePool)] or nil,
        martial = martial,
        health = health,
        isLeader = isLeader or false,
        memberIndex = memberIndex or 1,
    }
end

--- 生成一组敌族（每次讨伐刷新3个可选目标）
---@return table[] rivalClans
function RivalClans.Generate()
    local clans = {}
    local usedSurnames = {}

    -- 玩家姓氏不能出现
    local playerSurname = GameData.state and GameData.state.surname or ""

    -- 获取品阶缩放
    local s = GameData.state
    local clanRank = s and s.clanRank or 5
    local rankScale = RivalClans.RANK_SCALING[clanRank] or RivalClans.RANK_SCALING[5]

    -- 生成3个不同难度的敌族
    local tierIndices = { 1, 2, 3 }
    -- 世家以上额外出现精英敌族
    if clanRank >= 6 then
        tierIndices = { 2, 3, 4 }
    end

    for i = 1, 3 do
        local tier = RivalClans.DIFFICULTY_TIERS[tierIndices[i]]

        -- 选不重复的姓氏
        local surname
        repeat
            surname = RIVAL_SURNAMES[math.random(1, #RIVAL_SURNAMES)]
        until not usedSurnames[surname] and surname ~= playerSurname
        usedSurnames[surname] = true

        -- 宗族名
        local suffix = CLAN_SUFFIXES[math.random(1, #CLAN_SUFFIXES)]
        local clanName = surname .. suffix

        -- 成员数（品阶越高成员越多）
        local memberCount = math.random(tier.memberRange[1], tier.memberRange[2])
            + (rankScale.memberExtra or 0)
        local members = {}
        for j = 1, memberCount do
            members[j] = GenerateRivalMember(surname, tier, j == 1, j, rankScale)
        end

        -- 兵力（总士兵数）= 基础范围 × 品阶倍率
        local baseSoldiers = math.random(tier.soldierRange[1], tier.soldierRange[2])
        local soldiers = math.floor(baseSoldiers * rankScale.soldierMul)
        -- 取整到100
        soldiers = math.floor(soldiers / 100) * 100
        if soldiers < 100 then soldiers = 100 end

        -- 奖励 = 基础奖励 × 难度倍率 × 品阶奖励倍率
        local rewardMul = tier.rewardMul * rankScale.rewardMul
        local rewards = {
            silver = math.floor(math.random(RivalClans.BASE_REWARDS.silver[1], RivalClans.BASE_REWARDS.silver[2]) * rewardMul),
            grain  = math.floor(math.random(RivalClans.BASE_REWARDS.grain[1], RivalClans.BASE_REWARDS.grain[2]) * rewardMul),
            fame   = math.floor(math.random(RivalClans.BASE_REWARDS.fame[1], RivalClans.BASE_REWARDS.fame[2]) * rewardMul),
        }

        clans[i] = {
            id = i,
            name = clanName,
            surname = surname,
            tierId = tier.id,
            tierName = tier.name,
            tierColor = tier.color,
            tierDesc = tier.desc,
            members = members,
            soldiers = soldiers,
            rewards = rewards,
        }
    end

    return clans
end

-- ============================================================================
-- 战斗数据转换
-- ============================================================================

--- 将我方族人转换为战斗单位数据
---@param member table GameData member
---@return table battleUnit
function RivalClans.MemberToBattleUnit(member)
    -- 装备加成
    local eqBonus = EquipmentSystem.GetBonus(member)
    local totalMartial = member.martial + (eqBonus.martial or 0)
    local totalHealth  = member.health  + (eqBonus.health or 0)
    -- 攻击力 = (武艺+装备武艺) * 0.8 + 5
    local atk = math.floor(totalMartial * 0.8 + 5)
    -- 生命值 = (健康+装备健康) * 2 + 50
    local hp = math.floor(totalHealth * 2 + 50)
    -- 速度 = 武艺 * 0.3 + 20（影响移动和攻击间隔）
    local spd = math.floor(totalMartial * 0.3 + 20)
    -- 骑马加成：移速 +40%
    local mounted = member.hasHorse == true
    if mounted then
        spd = math.floor(spd * 1.4)
    end
    -- 防御 = 军衔加成 + 装备防御（armor提供额外防御）
    local def = 0
    if member.militaryRank == "把总" then def = 5
    elseif member.militaryRank == "守备" then def = 10
    end
    -- 护甲装备提供额外防御
    if member.equipment and member.equipment.armor then
        local armor = GameData.GetEquipment(member.equipment.armor)
        if armor then
            local rarity = GameData.GetRarityConfig(armor.rarity)
            local mul = rarity and rarity.multiplier or 1.0
            def = def + math.floor((armor.health or 0) * mul * 0.3)
        end
    end

    return {
        name = member.name,
        memberId = member.id,
        isSoldier = false,
        mounted = mounted,
        atk = atk,
        hp = hp,
        maxHp = hp,
        spd = spd,
        def = def,
        side = "player",
    }
end

--- 将小兵（每100个为一个单位）转换为战斗单位
---@param unitIndex number 第几个小兵单位
---@param side string "player" 或 "enemy"
---@param clanRank number|nil 品阶（仅敌方使用，用于缩放属性）
---@return table battleUnit
function RivalClans.SoldierToBattleUnit(unitIndex, side, clanRank)
    -- 小兵基础属性
    local atk, hp, spd, def = 12, 80, 18, 2

    -- 敌方小兵按品阶强化
    if side == "enemy" and clanRank then
        local scale = RivalClans.RANK_SCALING[clanRank]
        if scale then
            local bonus = scale.statBonus
            atk = atk + math.floor(bonus * 0.4)
            hp  = hp  + bonus * 3
            def = def + math.floor(bonus * 0.2)
        end
    end

    -- 注意：我方小兵的训练加成在 BattleScene 中统一应用（含筑寨、弓兵等修正），
    -- 此处不再重复叠加，避免双重加成 bug。

    return {
        name = (side == "player" and "我军" or "敌军") .. "第" .. unitIndex .. "营",
        memberId = nil,
        isSoldier = true,
        atk = atk,
        hp = hp,
        maxHp = hp,
        spd = spd,
        def = def,
        side = side,
    }
end

--- 将敌族成员转换为战斗单位
---@param rivalMember table
---@return table battleUnit
function RivalClans.RivalMemberToBattleUnit(rivalMember)
    local atk = math.floor(rivalMember.martial * 0.8 + 5)
    local hp = math.floor(rivalMember.health * 2 + 50)
    local spd = math.floor(rivalMember.martial * 0.3 + 20)
    local def = rivalMember.isLeader and 8 or 3

    return {
        name = rivalMember.name,
        memberId = nil,
        isSoldier = false,
        atk = atk,
        hp = hp,
        maxHp = hp,
        spd = spd,
        def = def,
        side = "enemy",
        isLeader = rivalMember.isLeader,
        title = rivalMember.title,
    }
end

-- ============================================================================
-- 解锁检查
-- ============================================================================

--- 讨伐系统是否已解锁（需品级达到世家=5）
function RivalClans.IsUnlocked()
    local s = GameData.state
    if not s then return false end
    return s.clanRank >= 5
end

--- 获取我方可出战的族人（必须是从军状态的男性，健康>30）
function RivalClans.GetDeployableMembers()
    local result = {}
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.gender == "male" and m.state == "从军"
           and m.health > 30 and m.martial > 0 then
            result[#result + 1] = m
        end
    end
    -- 按武艺排序
    table.sort(result, function(a, b) return a.martial > b.martial end)
    return result
end

--- 获取我方可用兵力总数（步兵 + 弓兵）
function RivalClans.GetPlayerSoldierCount()
    local s = GameData.state
    if not s or not s.army then return 0 end
    return (s.army.infantry or 0) + (s.army.archers or 0)
end

--- 获取我方步兵数量
function RivalClans.GetPlayerInfantry()
    local s = GameData.state
    if not s or not s.army then return 0 end
    return s.army.infantry or 0
end

--- 获取我方弓兵数量
function RivalClans.GetPlayerArchers()
    local s = GameData.state
    if not s or not s.army then return 0 end
    return s.army.archers or 0
end

--- 获取训练等级
function RivalClans.GetTrainingLevel()
    local s = GameData.state
    if not s or not s.army then return 0 end
    return s.army.trainingLevel or 0
end

-- ============================================================================
-- 征兵系统
-- ============================================================================

-- 征兵费用：每100人
RivalClans.CONSCRIPT_COST = {
    silver = 800,  -- 银两（每100人）
    grain = 600,   -- 粮食（每100人）
}

-- 兵力上限
RivalClans.MAX_ARMY_SIZE = 50000

--- 征兵
---@param unitType string "infantry" 或 "archers"
---@param count number 征兵数量（应为100的整数倍）
---@return boolean success
---@return string message
function RivalClans.Conscript(unitType, count)
    local s = GameData.state
    if not s or not s.army then return false, "数据异常" end

    -- 规范化为100的整数倍
    count = math.floor(count / 100) * 100
    if count <= 0 then return false, "数量不足100" end

    -- 检查上限
    local current = (s.army.infantry or 0) + (s.army.archers or 0)
    if current + count > RivalClans.MAX_ARMY_SIZE then
        local canRecruit = RivalClans.MAX_ARMY_SIZE - current
        canRecruit = math.floor(canRecruit / 100) * 100
        if canRecruit <= 0 then
            return false, "兵力已达上限（" .. RivalClans.MAX_ARMY_SIZE .. "）"
        end
        count = canRecruit
    end

    -- 计算费用
    local batches = count / 100
    local silverCost = RivalClans.CONSCRIPT_COST.silver * batches
    local grainCost = RivalClans.CONSCRIPT_COST.grain * batches

    -- 检查资源
    if not GameData.CanAfford(silverCost, grainCost, 0, 0) then
        return false, "资源不足（需银两" .. silverCost .. "、粮食" .. grainCost .. "）"
    end

    -- 扣费
    GameData.AddResource("silver", -silverCost)
    GameData.AddResource("grain", -grainCost)

    -- 增兵
    if unitType == "archers" then
        s.army.archers = (s.army.archers or 0) + count
    else
        s.army.infantry = (s.army.infantry or 0) + count
    end

    GameData.AddLog("征募" .. (unitType == "archers" and "弓兵" or "步兵") .. count .. "人，花费银两" .. silverCost .. "、粮食" .. grainCost .. "。")
    return true, "成功征募" .. (unitType == "archers" and "弓兵" or "步兵") .. count .. "人"
end

-- ============================================================================
-- 训练系统
-- ============================================================================

-- 训练费用：每级
RivalClans.TRAINING_COST = {
    silver = 3000,
    grain = 2000,
}

-- 最高训练等级
RivalClans.MAX_TRAINING_LEVEL = 5

-- 训练等级属性加成（加到 SoldierToBattleUnit 的基础属性上）
RivalClans.TRAINING_BONUS = {
    -- { atk_bonus, hp_bonus, def_bonus }
    [0] = { 0, 0, 0 },
    [1] = { 2, 10, 1 },
    [2] = { 4, 20, 2 },
    [3] = { 7, 35, 3 },
    [4] = { 10, 50, 4 },
    [5] = { 14, 70, 6 },
}

--- 训练军队（提升一级）
---@param free boolean 是否免费（看广告）
---@return boolean success
---@return string message
function RivalClans.TrainArmy(free)
    local s = GameData.state
    if not s or not s.army then return false, "数据异常" end

    local currentLevel = s.army.trainingLevel or 0
    if currentLevel >= RivalClans.MAX_TRAINING_LEVEL then
        return false, "已达最高训练等级"
    end

    local totalSoldiers = (s.army.infantry or 0) + (s.army.archers or 0)
    if totalSoldiers <= 0 then
        return false, "没有士兵可训练"
    end

    if not free then
        local silverCost = RivalClans.TRAINING_COST.silver
        local grainCost = RivalClans.TRAINING_COST.grain
        if not GameData.CanAfford(silverCost, grainCost, 0, 0) then
            return false, "资源不足（需银两" .. silverCost .. "、粮食" .. grainCost .. "）"
        end
        GameData.AddResource("silver", -silverCost)
        GameData.AddResource("grain", -grainCost)
    end

    s.army.trainingLevel = currentLevel + 1
    local levelName = { "新兵", "正卒", "精锐", "老兵", "虎贲", "铁军" }
    GameData.AddLog("军队训练提升至" .. (levelName[s.army.trainingLevel + 1] or ("Lv" .. s.army.trainingLevel)) .. "。")
    return true, "训练等级提升至 " .. s.army.trainingLevel
end

--- 获取训练等级名称
function RivalClans.GetTrainingLevelName(level)
    local names = { [0] = "新兵", "正卒", "精锐", "老兵", "虎贲", "铁军" }
    return names[level] or "未知"
end

return RivalClans
