-- ============================================================================
-- 大明浮生志2 - 集市交易系统
-- 管理商品定义、价格波动、买卖交易
-- 解锁条件: 乡绅(品级3)
-- ============================================================================

local GameData = require("Data.GameData")
local EraSystem = require("Data.EraSystem")

local MarketSystem = {}

-- ============================================================================
-- 商品定义
-- ============================================================================

MarketSystem.COMMODITIES = {
    { id = "grain",    name = "粮食",  icon = "粮", unit = "石",
      basePrice = 8, batchSize = 10,  -- 8银/10粮
      fluctRange = 0.40,
      desc = "民以食为天，灾年涨价、丰收跌价",
      resourceKey = "grain" },  -- 可与资源互转

    { id = "cloth",    name = "布匹",  icon = "布", unit = "匹",
      basePrice = 16, batchSize = 5,   -- 16银/5布
      fluctRange = 0.30,
      desc = "四季皆需，冬季尤贵",
      resourceKey = "cloth" },

    { id = "medicine", name = "药材",  icon = "药", unit = "份",
      basePrice = 40, batchSize = 1,
      fluctRange = 0.20,
      desc = "济世良药，瘟疫时价格飞涨" },

    { id = "iron",     name = "铁器",  icon = "铁", unit = "件",
      basePrice = 64, batchSize = 1,
      fluctRange = 0.15,
      desc = "农具兵器皆赖之，战时涨价" },

    { id = "books",    name = "书籍",  icon = "书", unit = "册",
      basePrice = 80, batchSize = 1,
      fluctRange = 0.10,
      desc = "圣贤之书，科举季需求旺盛" },

    { id = "horse",    name = "马匹",  icon = "马", unit = "匹",
      basePrice = 240, batchSize = 1,
      fluctRange = 0.25,
      desc = "行军征战之需，军需时价格高涨" },
}

-- 商品id快速查找
MarketSystem.COMMODITY_MAP = {}
for _, c in ipairs(MarketSystem.COMMODITIES) do
    MarketSystem.COMMODITY_MAP[c.id] = c
end

-- ============================================================================
-- 季节因子（月份 -> 各商品价格修正）
-- ============================================================================

local SEASON_FACTORS = {
    -- 春耕(2-4): 粮种子需求大涨; 布匹平稳
    grain    = { 1.0,  1.15, 1.20, 1.10, 0.95, 0.90, 0.85, 0.80, 0.70, 0.75, 0.85, 0.95 },
    cloth    = { 1.05, 1.00, 0.95, 0.90, 0.90, 0.95, 1.00, 1.00, 1.05, 1.10, 1.15, 1.20 },
    medicine = { 1.00, 1.00, 1.00, 1.05, 1.10, 1.15, 1.15, 1.10, 1.00, 1.00, 0.95, 0.95 },
    iron     = { 1.00, 1.05, 1.05, 1.00, 1.00, 0.95, 0.95, 1.00, 1.05, 1.05, 1.00, 1.00 },
    books    = { 1.00, 1.15, 1.10, 1.00, 0.95, 0.90, 0.90, 1.10, 1.15, 1.00, 0.95, 0.95 },
    horse    = { 1.05, 1.05, 1.10, 1.10, 1.00, 0.95, 0.90, 0.90, 0.95, 1.00, 1.05, 1.10 },
}

-- ============================================================================
-- 初始化集市状态
-- ============================================================================

function MarketSystem.InitState(state)
    if state.market then return end  -- 已有则不覆盖（存档恢复）
    state.market = {
        prices = {},       -- { [commodityId] = currentPrice }
        stock = {},        -- { [commodityId] = quantity }  玩家持有的商品库存
        history = {},      -- { [commodityId] = {price1, price2, ...} } 最近12月价格记录
        lastUpdateMonth = 0,
        totalTradeProfit = 0,  -- 累计贸易利润（统计用）
        tradeCount = 0,        -- 交易次数
    }
    -- 初始化各商品价格和库存
    for _, c in ipairs(MarketSystem.COMMODITIES) do
        state.market.prices[c.id] = c.basePrice
        state.market.stock[c.id] = 0
        state.market.history[c.id] = {}
    end
end

--- 确保 market 状态存在（兼容旧存档）
function MarketSystem.EnsureState()
    local s = GameData.state
    if not s then return end
    MarketSystem.InitState(s)
end

-- ============================================================================
-- 价格波动计算（每月调用）
-- ============================================================================

function MarketSystem.UpdatePrices()
    local s = GameData.state
    if not s then return end
    MarketSystem.EnsureState()

    local market = s.market
    local month = s.month
    local year = s.year

    for _, commodity in ipairs(MarketSystem.COMMODITIES) do
        local cid = commodity.id
        local base = commodity.basePrice

        -- 1) 季节因子
        local seasonFactor = 1.0
        local sf = SEASON_FACTORS[cid]
        if sf then
            seasonFactor = sf[month] or 1.0
        end

        -- 2) 时代/事件因子
        local eventFactor = MarketSystem.CalcEventFactor(cid, year, month)

        -- 3) 随机扰动（+-10%）
        local randomFactor = 0.90 + math.random() * 0.20

        -- 4) 综合价格
        local rawPrice = base * seasonFactor * eventFactor * randomFactor

        -- 5) 限制波动范围
        local minPrice = base * (1.0 - commodity.fluctRange)
        local maxPrice = base * (1.0 + commodity.fluctRange)
        local finalPrice = math.max(minPrice, math.min(maxPrice, rawPrice))

        -- 取整（最低1银）
        finalPrice = math.max(1, math.floor(finalPrice * 100 + 0.5) / 100)

        market.prices[cid] = finalPrice

        -- 记录历史（保留最近12个月）
        local hist = market.history[cid]
        if not hist then
            hist = {}
            market.history[cid] = hist
        end
        hist[#hist + 1] = finalPrice
        if #hist > 12 then
            table.remove(hist, 1)
        end
    end

    market.lastUpdateMonth = s.totalMonths
    -- 重置本月消费统计（供广告返利用）
    market.monthlySpent = 0
end

--- 计算时代/事件引发的价格因子
---@param commodityId string
---@param year number
---@param month number
---@return number factor
function MarketSystem.CalcEventFactor(commodityId, year, month)
    local factor = 1.0
    local s = GameData.state
    local region = GameData.GetRegion()

    -- 战乱年代: 铁器、马匹涨价，书籍跌价
    local isWartime = false
    if year >= 1399 and year <= 1402 then isWartime = true end  -- 靖难之役
    if year >= 1449 and year <= 1450 then isWartime = true end  -- 土木堡之变
    if year >= 1592 and year <= 1598 then isWartime = true end  -- 万历三大征
    if year >= 1627 then isWartime = true end                    -- 明末动荡

    if isWartime then
        if commodityId == "iron" then factor = factor * 1.25 end
        if commodityId == "horse" then factor = factor * 1.30 end
        if commodityId == "grain" then factor = factor * 1.15 end
        if commodityId == "books" then factor = factor * 0.85 end
    end

    -- 郑和下西洋(1405-1433): 布匹、药材有外贸需求
    if year >= 1405 and year <= 1433 then
        if commodityId == "cloth" then factor = factor * 1.10 end
        if commodityId == "medicine" then factor = factor * 1.08 end
    end

    -- 瘟疫/灾荒: 药材暴涨、粮食涨价
    -- 检查上个月是否有灾害事件（简化：用地域灾害率随机判定）
    if region and region.disasterRate then
        local disasterChance = region.disasterRate * 0.3  -- 集市层面影响概率较低
        if math.random() < disasterChance then
            if commodityId == "medicine" then factor = factor * 1.20 end
            if commodityId == "grain" then factor = factor * 1.10 end
        end
    end

    -- 科举年份(逢三年一科: 乡试八月, 会试次年二月)
    if year % 3 == 0 and (month >= 6 and month <= 9) then
        if commodityId == "books" then factor = factor * 1.15 end
    end

    return factor
end

-- ============================================================================
-- 买卖交易
-- ============================================================================

--- 计算购买某商品的花费
---@param commodityId string
---@param quantity number 购买批次数
---@return number cost 总花费银两
function MarketSystem.CalcBuyCost(commodityId, quantity)
    local s = GameData.state
    if not s or not s.market then return 0 end
    local price = s.market.prices[commodityId] or 0
    return math.floor(price * quantity + 0.5)
end

--- 购买商品
---@param commodityId string
---@param quantity number 购买批次数
---@return boolean success
---@return string message
function MarketSystem.Buy(commodityId, quantity)
    local s = GameData.state
    if not s then return false, "游戏未开始" end
    MarketSystem.EnsureState()

    local commodity = MarketSystem.COMMODITY_MAP[commodityId]
    if not commodity then return false, "未知商品" end
    if quantity <= 0 then return false, "数量无效" end

    local cost = MarketSystem.CalcBuyCost(commodityId, quantity)
    if s.silver < cost then
        return false, "银两不足（需" .. cost .. "两）"
    end

    -- 扣银两
    s.silver = s.silver - cost
    -- 记录本月消费总额（供广告返利用）
    s.market.monthlySpent = (s.market.monthlySpent or 0) + cost

    -- 如果是可转资源的商品（粮食、布匹），直接加到资源
    if commodity.resourceKey then
        local addAmount = quantity * commodity.batchSize
        GameData.AddResource(commodity.resourceKey, addAmount)
        s.market.tradeCount = s.market.tradeCount + 1
        GameData.AddLog("集市购入" .. commodity.name .. addAmount .. commodity.unit .. "，花费" .. cost .. "两银子")
        return true, "购入" .. commodity.name .. " " .. addAmount .. commodity.unit
    else
        -- 非资源商品存入集市库存
        s.market.stock[commodityId] = (s.market.stock[commodityId] or 0) + quantity
        s.market.tradeCount = s.market.tradeCount + 1
        GameData.AddLog("集市购入" .. commodity.name .. quantity .. commodity.unit .. "，花费" .. cost .. "两银子")
        return true, "购入" .. commodity.name .. " " .. quantity .. commodity.unit
    end
end

--- 计算出售某商品的收入（比买入价低20%作为买卖差价）
---@param commodityId string
---@param quantity number
---@return number income
function MarketSystem.CalcSellIncome(commodityId, quantity)
    local s = GameData.state
    if not s or not s.market then return 0 end
    local price = s.market.prices[commodityId] or 0
    -- 卖出价 = 买入价 * 0.8（20%差价）
    return math.max(1, math.floor(price * 0.8 * quantity + 0.5))
end

--- 出售商品
---@param commodityId string
---@param quantity number
---@return boolean success
---@return string message
function MarketSystem.Sell(commodityId, quantity)
    local s = GameData.state
    if not s then return false, "游戏未开始" end
    MarketSystem.EnsureState()

    local commodity = MarketSystem.COMMODITY_MAP[commodityId]
    if not commodity then return false, "未知商品" end
    if quantity <= 0 then return false, "数量无效" end

    -- 检查库存
    if commodity.resourceKey then
        -- 资源类商品从资源池扣除
        local needAmount = quantity * commodity.batchSize
        local currentAmount = s[commodity.resourceKey] or 0
        if currentAmount < needAmount then
            return false, commodity.name .. "不足（需" .. needAmount .. commodity.unit .. "）"
        end
        GameData.AddResource(commodity.resourceKey, -needAmount)
    else
        -- 非资源商品从集市库存扣除
        local stock = s.market.stock[commodityId] or 0
        if stock < quantity then
            return false, commodity.name .. "库存不足（仅" .. stock .. commodity.unit .. "）"
        end
        s.market.stock[commodityId] = stock - quantity
    end

    local income = MarketSystem.CalcSellIncome(commodityId, quantity)
    s.silver = s.silver + income
    s.market.tradeCount = s.market.tradeCount + 1
    s.market.totalTradeProfit = s.market.totalTradeProfit + income

    local sellDesc = commodity.resourceKey
        and (quantity * commodity.batchSize .. commodity.unit)
        or (quantity .. commodity.unit)
    GameData.AddLog("集市售出" .. commodity.name .. sellDesc .. "，获得" .. income .. "两银子")

    return true, "售出" .. commodity.name .. "，获" .. income .. "两"
end

--- 获取玩家持有的某商品数量（用于UI显示）
---@param commodityId string
---@return number
function MarketSystem.GetStock(commodityId)
    local s = GameData.state
    if not s or not s.market then return 0 end
    local commodity = MarketSystem.COMMODITY_MAP[commodityId]
    if not commodity then return 0 end

    if commodity.resourceKey then
        return s[commodity.resourceKey] or 0
    else
        return s.market.stock[commodityId] or 0
    end
end

--- 获取商品单位显示名
---@param commodityId string
---@return string
function MarketSystem.GetStockDisplay(commodityId)
    local commodity = MarketSystem.COMMODITY_MAP[commodityId]
    if not commodity then return "0" end
    local stock = MarketSystem.GetStock(commodityId)
    return stock .. commodity.unit
end

--- 获取价格趋势（与上月比较）
---@param commodityId string
---@return string trend "up"|"down"|"stable"
function MarketSystem.GetPriceTrend(commodityId)
    local s = GameData.state
    if not s or not s.market then return "stable" end
    local hist = s.market.history[commodityId]
    if not hist or #hist < 2 then return "stable" end
    local prev = hist[#hist - 1]
    local curr = hist[#hist]
    if curr > prev * 1.03 then return "up"
    elseif curr < prev * 0.97 then return "down"
    else return "stable" end
end

--- 获取集市统计信息
---@return table
function MarketSystem.GetStats()
    local s = GameData.state
    if not s or not s.market then
        return { tradeCount = 0, totalProfit = 0 }
    end
    return {
        tradeCount = s.market.tradeCount,
        totalProfit = s.market.totalTradeProfit,
    }
end

return MarketSystem
