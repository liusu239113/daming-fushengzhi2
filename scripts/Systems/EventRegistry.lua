-- ============================================================================
-- 大明浮生志2 - 事件注册中心
-- 统一管理6级事件池，提供按品阶过滤、冷却去重、身份匹配的事件选取
-- ============================================================================

local GameData = require("Data.GameData")
local EraSystem = require("Data.EraSystem")

local EventRegistry = {}

-- 所有注册的扩展事件（按等级分池加载）
local allEvents = {}
-- 事件冷却记录 { [eventId] = lastTriggeredMonth }
-- 持久化存储在 GameData.state.eventCooldowns 中

-- ============================================================================
-- 初始化：加载9级事件池
-- ============================================================================

function EventRegistry.Init()
    allEvents = {}

    local pools = {
        require("Data.Events.Events_Rank1"),  -- 寒门
        require("Data.Events.Events_Rank2"),  -- 农户
        require("Data.Events.Events_Rank3"),  -- 乡绅
        require("Data.Events.Events_Rank4"),  -- 望族
        require("Data.Events.Events_Rank5"),  -- 世家
        require("Data.Events.Events_Rank6"),  -- 勋贵
        require("Data.Events.Events_Rank7"),  -- 名门
        require("Data.Events.Events_Rank8"),  -- 豪阀
        require("Data.Events.Events_Rank9"),  -- 国柱
        require("Data.Events.Events_CourtMissions"),  -- 朝廷差事（C2）
        require("Data.Events.Events_FamilyConflict"),  -- 家族内斗（C3）
        require("Data.Events.Events_RivalAttack"),     -- 敌对势力进攻（C4）
    }

    for _, pool in ipairs(pools) do
        for _, evt in ipairs(pool) do
            allEvents[#allEvents + 1] = evt
        end
    end
end

-- ============================================================================
-- 获取当前可触发的扩展事件（已过滤品阶、冷却、身份等条件）
-- ============================================================================

---@param s table GameData.state
---@param report table 月度报告
---@return table[] 可触发事件列表（含计算后的weight）
function EventRegistry.GetAvailableEvents(s, report)
    if #allEvents == 0 then
        EventRegistry.Init()
    end

    -- 确保冷却表存在
    if not s.eventCooldowns then
        s.eventCooldowns = {}
    end

    local currentMonth = (s.year - 1368) * 12 + s.month  -- 绝对月份数
    local difficulty = GameData.GetDifficulty()
    local aliveMembers = GameData.GetAliveMembers()

    -- 预计算族人身份集合（用于 identityMatch）
    local identitySet = {}
    for _, m in ipairs(aliveMembers) do
        if m.identity and m.identity ~= "平民" then
            identitySet[m.identity] = true
        end
        -- 通用标签
        if m.state == "读书" then identitySet["scholar"] = true end
        if m.state == "习武" then identitySet["warrior"] = true end
        if m.state == "经商" then identitySet["merchant"] = true end
        if m.state == "从军" then identitySet["soldier"] = true end
        if m.spouseId then identitySet["married"] = true end
        if m.gender == "female" then identitySet["female"] = true end
        if m.gender == "male" then identitySet["male"] = true end
    end

    -- 灾害类事件标签（受难度影响权重）
    local available = {}

    for _, evt in ipairs(allEvents) do
        local canTrigger = true

        -- 1. 品阶范围过滤
        if evt.rankRange then
            local minRank = evt.rankRange[1] or 1
            local maxRank = evt.rankRange[2] or 9
            if s.clanRank < minRank or s.clanRank > maxRank then
                canTrigger = false
            end
        end

        -- 2. 冷却检查
        if canTrigger and evt.cooldownMonths and evt.cooldownMonths > 0 then
            local lastTriggered = s.eventCooldowns[evt.id]
            if lastTriggered then
                if currentMonth - lastTriggered < evt.cooldownMonths then
                    canTrigger = false
                end
            end
        end

        -- 3. 身份匹配
        if canTrigger and evt.identityMatch then
            local matched = false
            for _, tag in ipairs(evt.identityMatch) do
                if identitySet[tag] then
                    matched = true
                    break
                end
            end
            if not matched then canTrigger = false end
        end

        -- 4. 年代限制
        if canTrigger and evt.era then
            local eraMin = evt.era[1] or 1368
            local eraMax = evt.era[2] or 1644
            if s.year < eraMin or s.year > eraMax then
                canTrigger = false
            end
        end

        -- 5. 自定义条件检查
        if canTrigger and evt.check then
            canTrigger = evt.check(s)
        end

        -- 6. 连锁事件排除（只能由连锁触发）
        if evt.isChainEvent then
            canTrigger = false
        end

        -- 通过全部检查，加入可用列表
        if canTrigger then
            local w = evt.weight or 5
            -- 灾害事件权重受难度影响
            if evt.isDisaster then
                w = math.ceil(w * difficulty.disasterMul)
            end
            available[#available + 1] = { evt = evt, weight = w }
        end
    end

    return available
end

-- ============================================================================
-- 从扩展事件池中选取一个事件
-- ============================================================================

---@param s table GameData.state
---@param report table 月度报告
---@return table|nil 事件执行结果（title/desc/choices），或nil
function EventRegistry.SelectAndExecute(s, report)
    local available = EventRegistry.GetAvailableEvents(s, report)
    if #available == 0 then return nil end

    -- 加权随机选择
    local totalWeight = 0
    for _, entry in ipairs(available) do
        totalWeight = totalWeight + entry.weight
    end

    local roll = math.random() * totalWeight
    local cumulative = 0
    local chosen = available[1]
    for _, entry in ipairs(available) do
        cumulative = cumulative + entry.weight
        if roll <= cumulative then
            chosen = entry
            break
        end
    end

    local evt = chosen.evt

    -- 记录冷却
    if evt.id and evt.cooldownMonths and evt.cooldownMonths > 0 then
        if not s.eventCooldowns then s.eventCooldowns = {} end
        local currentMonth = (s.year - 1368) * 12 + s.month
        s.eventCooldowns[evt.id] = currentMonth
    end

    -- 执行事件
    local result = evt.execute(s, report)
    return result
end

-- ============================================================================
-- 处理连锁事件（从扩展事件池查找）
-- ============================================================================

---@param chainId string 连锁事件ID
---@param s table GameData.state
---@param report table 月度报告
---@return table|nil 事件执行结果
function EventRegistry.ExecuteChainEvent(chainId, s, report)
    if #allEvents == 0 then
        EventRegistry.Init()
    end

    for _, evt in ipairs(allEvents) do
        if evt.id == chainId and evt.isChainEvent then
            return evt.execute(s, report)
        end
    end
    return nil
end

-- ============================================================================
-- 获取统计信息
-- ============================================================================

function EventRegistry.GetStats()
    if #allEvents == 0 then
        EventRegistry.Init()
    end

    local stats = { total = #allEvents, byRank = {} }
    for _, evt in ipairs(allEvents) do
        if evt.rankRange then
            local key = evt.rankRange[1] .. "-" .. evt.rankRange[2]
            stats.byRank[key] = (stats.byRank[key] or 0) + 1
        end
    end
    return stats
end

return EventRegistry
