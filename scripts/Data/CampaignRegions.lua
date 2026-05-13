-- ============================================================================
-- 大明浮生志2 - 区域战役数据模块
-- 12个战役区域，难度递增，线性解锁
-- ============================================================================

local GameData = require("Data.GameData")

local CampaignRegions = {}

-- ============================================================================
-- 区域数据（难度递增，线性解锁）
-- ============================================================================

CampaignRegions.REGIONS = {
    {
        id = 1, name = "清剿本地匪寨", difficulty = 1,
        soldierRange = { 200, 500 }, memberRange = { 2, 3 },
        martialRange = { 15, 30 }, healthRange = { 50, 70 },
        rewards = { silver = { 15, 30 }, grain = { 10, 25 }, fame = { 3, 8 } },
        desc = "盘踞附近山林的匪寨，为祸乡里已久。",
    },
    {
        id = 2, name = "邻村恶霸", difficulty = 2,
        soldierRange = { 400, 800 }, memberRange = { 2, 3 },
        martialRange = { 20, 35 }, healthRange = { 55, 75 },
        rewards = { silver = { 20, 40 }, grain = { 15, 30 }, fame = { 5, 10 } },
        desc = "邻村豪强横行霸道，屡次侵占田产。",
    },
    {
        id = 3, name = "县城豪强", difficulty = 3,
        soldierRange = { 600, 1200 }, memberRange = { 3, 4 },
        martialRange = { 25, 40 }, healthRange = { 60, 80 },
        rewards = { silver = { 30, 55 }, grain = { 20, 40 }, fame = { 8, 15 } },
        desc = "县城中势力庞大的豪强家族。",
    },
    {
        id = 4, name = "邻县争锋", difficulty = 4,
        soldierRange = { 1000, 2000 }, memberRange = { 3, 4 },
        martialRange = { 30, 45 }, healthRange = { 65, 85 },
        rewards = { silver = { 40, 70 }, grain = { 30, 50 }, fame = { 10, 18 } },
        desc = "邻县实力雄厚的世家，对我方虎视眈眈。",
    },
    {
        id = 5, name = "府城争霸", difficulty = 5,
        soldierRange = { 1500, 3000 }, memberRange = { 3, 5 },
        martialRange = { 35, 50 }, healthRange = { 65, 85 },
        rewards = { silver = { 50, 90 }, grain = { 40, 65 }, fame = { 12, 22 } },
        desc = "府城中称霸一方的望族，兵强马壮。",
    },
    {
        id = 6, name = "省城攻略", difficulty = 6,
        soldierRange = { 2500, 5000 }, memberRange = { 4, 5 },
        martialRange = { 40, 55 }, healthRange = { 70, 90 },
        rewards = { silver = { 65, 110 }, grain = { 50, 80 }, fame = { 15, 28 } },
        desc = "省城豪门大族，盘根错节，根基深厚。",
    },
    {
        id = 7, name = "中原逐鹿", difficulty = 7,
        soldierRange = { 4000, 7000 }, memberRange = { 4, 6 },
        martialRange = { 45, 60 }, healthRange = { 70, 90 },
        rewards = { silver = { 80, 140 }, grain = { 60, 100 }, fame = { 20, 35 } },
        desc = "中原腹地，群雄割据，逐鹿中原。",
    },
    {
        id = 8, name = "江南征伐", difficulty = 8,
        soldierRange = { 5000, 9000 }, memberRange = { 4, 6 },
        martialRange = { 50, 65 }, healthRange = { 75, 90 },
        rewards = { silver = { 100, 170 }, grain = { 75, 120 }, fame = { 25, 40 } },
        desc = "鱼米之乡，富甲天下，却也兵力雄厚。",
    },
    {
        id = 9, name = "巴蜀平定", difficulty = 9,
        soldierRange = { 7000, 11000 }, memberRange = { 5, 6 },
        martialRange = { 55, 70 }, healthRange = { 75, 95 },
        rewards = { silver = { 120, 200 }, grain = { 90, 140 }, fame = { 30, 50 } },
        desc = "蜀道之难难于上青天，更兼雄兵把守。",
    },
    {
        id = 10, name = "两广统一", difficulty = 10,
        soldierRange = { 9000, 13000 }, memberRange = { 5, 6 },
        martialRange = { 60, 75 }, healthRange = { 80, 95 },
        rewards = { silver = { 150, 240 }, grain = { 100, 160 }, fame = { 35, 55 } },
        desc = "两广之地，民风彪悍，土司林立。",
    },
    {
        id = 11, name = "塞北征讨", difficulty = 11,
        soldierRange = { 12000, 16000 }, memberRange = { 5, 6 },
        martialRange = { 70, 85 }, healthRange = { 80, 95 },
        rewards = { silver = { 180, 280 }, grain = { 120, 190 }, fame = { 40, 65 } },
        desc = "塞外铁骑纵横，苦寒之地，劲敌如云。",
    },
    {
        id = 12, name = "问鼎天下", difficulty = 12,
        soldierRange = { 15000, 20000 }, memberRange = { 6, 6 },
        martialRange = { 80, 95 }, healthRange = { 85, 100 },
        rewards = { silver = { 250, 400 }, grain = { 150, 250 }, fame = { 50, 80 } },
        desc = "天下大势，分久必合。最终的决战！",
    },
}

-- ============================================================================
-- 查询函数
-- ============================================================================

--- 获取所有区域列表
function CampaignRegions.GetAll()
    return CampaignRegions.REGIONS
end

--- 获取指定区域
---@param regionId number
---@return table|nil
function CampaignRegions.GetById(regionId)
    for _, r in ipairs(CampaignRegions.REGIONS) do
        if r.id == regionId then return r end
    end
    return nil
end

--- 区域是否已征服
---@param regionId number
---@return boolean
function CampaignRegions.IsConquered(regionId)
    local s = GameData.state
    if not s or not s.conqueredRegions then return false end
    for _, id in ipairs(s.conqueredRegions) do
        if id == regionId then return true end
    end
    return false
end

--- 区域是否已解锁（前一个区域已征服，或是第一个区域）
---@param regionId number
---@return boolean
function CampaignRegions.IsUnlocked(regionId)
    if regionId == 1 then return true end
    return CampaignRegions.IsConquered(regionId - 1)
end

--- 标记区域为已征服
---@param regionId number
function CampaignRegions.MarkConquered(regionId)
    local s = GameData.state
    if not s then return end
    if not s.conqueredRegions then s.conqueredRegions = {} end
    -- 避免重复
    for _, id in ipairs(s.conqueredRegions) do
        if id == regionId then return end
    end
    s.conqueredRegions[#s.conqueredRegions + 1] = regionId
end

-- ============================================================================
-- 敌族姓氏（用于区域战役）
-- ============================================================================

local REGION_SURNAMES = {
    "马", "孙", "韩", "徐", "曹", "魏", "吕", "沈", "冯", "郑",
    "蒋", "唐", "贺", "邓", "萧", "田", "程", "袁", "于", "董",
}

local GIVEN_NAMES = {
    "虎", "豹", "龙", "猛", "刚", "坚", "毅", "烈", "威", "勇",
    "强", "铁", "钢", "峰", "岩", "雷", "霆", "鸿", "熊", "飞",
    "锋", "剑", "骁", "悍", "豪", "壮", "魁", "洪", "凯", "震",
}

local LEADER_TITLES = { "大将军", "都督", "总兵", "统帅", "首领", "寨主", "城主" }

--- 生成区域战役的敌族数据（兼容 BattleScene.Enter 所需的 rival 格式）
---@param regionId number
---@return table rival
function CampaignRegions.GenerateEnemy(regionId)
    local region = CampaignRegions.GetById(regionId)
    if not region then return nil end

    local playerSurname = GameData.state and GameData.state.surname or ""

    -- 随机姓氏（不与玩家重复）
    local surname
    repeat
        surname = REGION_SURNAMES[math.random(1, #REGION_SURNAMES)]
    until surname ~= playerSurname

    -- 成员数
    local memberCount = math.random(region.memberRange[1], region.memberRange[2])
    local members = {}
    local usedNames = {}

    for j = 1, memberCount do
        local isLeader = (j == 1)
        -- 随机名字（不重复）
        local givenName
        repeat
            givenName = GIVEN_NAMES[math.random(1, #GIVEN_NAMES)]
            if math.random() < 0.4 then
                local n2 = GIVEN_NAMES[math.random(1, #GIVEN_NAMES)]
                if n2 ~= givenName then givenName = givenName .. n2 end
            end
        until not usedNames[givenName]
        usedNames[givenName] = true

        local name = surname .. givenName
        local martial = math.random(region.martialRange[1], region.martialRange[2])
        local health = math.random(region.healthRange[1], region.healthRange[2])

        if isLeader then
            martial = math.min(100, martial + 15)
            health = math.min(100, health + 10)
        end

        members[j] = {
            name = name,
            gender = "male",
            age = isLeader and math.random(35, 55) or math.random(18, 50),
            title = isLeader and LEADER_TITLES[math.random(1, #LEADER_TITLES)] or nil,
            martial = martial,
            health = health,
            isLeader = isLeader,
            memberIndex = j,
        }
    end

    -- 总兵力
    local soldiers = math.random(region.soldierRange[1], region.soldierRange[2])
    soldiers = math.floor(soldiers / 100) * 100
    if soldiers < 100 then soldiers = 100 end

    -- 奖励
    local rewards = {
        silver = math.random(region.rewards.silver[1], region.rewards.silver[2]),
        grain = math.random(region.rewards.grain[1], region.rewards.grain[2]),
        fame = math.random(region.rewards.fame[1], region.rewards.fame[2]),
    }

    -- 难度标签
    local tierNames = { "匪寨", "恶霸", "豪强", "世家", "望族", "豪门", "群雄", "霸主", "枭雄", "雄主", "铁骑", "天下" }
    local tierColors = {
        { 100, 200, 100, 255 },  -- 1 绿
        { 100, 200, 100, 255 },  -- 2 绿
        { 220, 180, 50, 255 },   -- 3 黄
        { 220, 180, 50, 255 },   -- 4 黄
        { 220, 120, 50, 255 },   -- 5 橙
        { 220, 120, 50, 255 },   -- 6 橙
        { 220, 80, 60, 255 },    -- 7 红
        { 220, 80, 60, 255 },    -- 8 红
        { 180, 80, 220, 255 },   -- 9 紫
        { 180, 80, 220, 255 },   -- 10 紫
        { 180, 50, 50, 255 },    -- 11 暗红
        { 200, 180, 50, 255 },   -- 12 金
    }

    local clanName = surname .. "军"

    return {
        id = regionId,
        name = clanName,
        surname = surname,
        tierId = "region_" .. regionId,
        tierName = tierNames[regionId] or "未知",
        tierColor = tierColors[regionId] or { 200, 200, 200, 255 },
        tierDesc = region.desc,
        members = members,
        soldiers = soldiers,
        rewards = rewards,
        regionId = regionId,  -- 标记来源为区域战役
    }
end

return CampaignRegions
