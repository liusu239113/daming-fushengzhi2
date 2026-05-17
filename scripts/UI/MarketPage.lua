-- ============================================================================
-- 大明浮生志2 - 集市交易页面
-- 展示商品列表、实时价格、涨跌趋势、买卖操作
-- 解锁条件: 乡绅(品级3)
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")
local MarketSystem = require("Systems.MarketSystem")
local Toast = require("Systems.Toast")
local AudioManager = require("Systems.AudioManager")
local AdSystem = require("Systems.AdSystem")

local MarketPage = {}

-- 趋势图标和颜色
local TREND_CONFIG = {
    up     = { icon = "↑", color = Theme.RED,   label = "涨" },
    down   = { icon = "↓", color = Theme.GREEN, label = "跌" },
    stable = { icon = "→", color = Theme.TEXT_MUTED, label = "平" },
}

-- ============================================================================
-- 商品卡片
-- ============================================================================

---@param commodity table
---@param pageTitle fun(title: string, subtitle: string): table
---@param gameScreen table
local function CreateCommodityCard(commodity, pageTitle, gameScreen)
    local s = GameData.state
    local market = s.market
    local cid = commodity.id
    local currentPrice = market.prices[cid] or commodity.basePrice
    local trend = MarketSystem.GetPriceTrend(cid)
    local tc = TREND_CONFIG[trend]

    -- 持有数量
    local stockDisplay = MarketSystem.GetStockDisplay(cid)

    -- 价格显示（带单位换算说明）
    local priceText
    if commodity.batchSize > 1 then
        priceText = currentPrice .. "两/" .. commodity.batchSize .. commodity.unit
    else
        priceText = currentPrice .. "两/" .. commodity.unit
    end

    -- 卖出价
    local sellPrice = MarketSystem.CalcSellIncome(cid, 1)

    -- 价格历史简要（最近3个月）
    local hist = market.history[cid] or {}
    local histParts = {}
    local histStart = math.max(1, #hist - 2)
    for i = histStart, #hist do
        histParts[#histParts + 1] = string.format("%.1f", hist[i])
    end
    local histText = #histParts > 0 and ("近期: " .. table.concat(histParts, "→")) or ""

    -- 计算玩家可买的最大数量
    local maxBuy = math.floor(s.silver / math.max(1, currentPrice))
    -- 计算玩家可卖的最大数量
    local maxSell = 0
    if commodity.resourceKey then
        maxSell = math.floor((s[commodity.resourceKey] or 0) / commodity.batchSize)
    else
        maxSell = market.stock[cid] or 0
    end

    -- 购买按钮组
    local buyButtons = {}
    local buyAmounts = { 1, 5, 10 }
    for _, amt in ipairs(buyAmounts) do
        local cost = MarketSystem.CalcBuyCost(cid, amt)
        local canBuy = s.silver >= cost
        buyButtons[#buyButtons + 1] = UI.Button {
            text = "买" .. amt,
            fontSize = 11,
            paddingHorizontal = 8, paddingVertical = 4,
            variant = canBuy and "primary" or "outline",
            disabled = not canBuy,
            onClick = function()
                local ok, msg = MarketSystem.Buy(cid, amt)
                if ok then
                    AudioManager.PlaySFX("coin")
                    Toast.Show(msg)
                else
                    Toast.Show(msg)
                end
                if gameScreen and gameScreen.RefreshAll then
                    gameScreen.RefreshAll()
                end
            end,
        }
    end

    -- 出售按钮组
    local sellButtons = {}
    local sellAmounts = { 1, 5, 10 }
    for _, amt in ipairs(sellAmounts) do
        local canSell = maxSell >= amt
        sellButtons[#sellButtons + 1] = UI.Button {
            text = "卖" .. amt,
            fontSize = 11,
            paddingHorizontal = 8, paddingVertical = 4,
            variant = canSell and "outline" or "outline",
            disabled = not canSell,
            onClick = function()
                local ok, msg = MarketSystem.Sell(cid, amt)
                if ok then
                    AudioManager.PlaySFX("coin")
                    Toast.Show(msg)
                else
                    Toast.Show(msg)
                end
                if gameScreen and gameScreen.RefreshAll then
                    gameScreen.RefreshAll()
                end
            end,
        }
    end

    return UI.Panel {
        width = "100%", padding = 12, borderRadius = 10,
        backgroundColor = Theme.BG_WHITE,
        borderWidth = 1, borderColor = Theme.BORDER,
        gap = 8,
        children = {
            -- 第一行: 商品名 + 价格 + 趋势
            UI.Panel {
                width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                children = {
                    -- 左侧: 图标+名称
                    UI.Panel {
                        flexDirection = "row", gap = 6, alignItems = "center",
                        children = {
                            UI.Panel {
                                width = 32, height = 32, borderRadius = 6,
                                backgroundColor = Theme.PRIMARY_LIGHT,
                                justifyContent = "center", alignItems = "center",
                                children = {
                                    UI.Label { text = commodity.icon, fontSize = 16, fontColor = Theme.PRIMARY },
                                },
                            },
                            UI.Panel {
                                gap = 1,
                                children = {
                                    UI.Label { text = commodity.name, fontSize = 14, fontColor = Theme.TEXT_TITLE },
                                    UI.Label { text = commodity.desc, fontSize = 9, fontColor = Theme.TEXT_MUTED },
                                },
                            },
                        },
                    },
                    -- 右侧: 价格+趋势
                    UI.Panel {
                        alignItems = "flex-end", gap = 2,
                        children = {
                            UI.Panel {
                                flexDirection = "row", alignItems = "center", gap = 3,
                                children = {
                                    UI.Label { text = priceText, fontSize = 14, fontColor = Theme.GOLD_DARK },
                                    UI.Label { text = tc.icon, fontSize = 13, fontColor = tc.color },
                                },
                            },
                            UI.Label { text = "卖出价: " .. sellPrice .. "两", fontSize = 9, fontColor = Theme.TEXT_MUTED },
                        },
                    },
                },
            },

            -- 第二行: 持有量 + 近期价格走势
            UI.Panel {
                width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                paddingHorizontal = 2,
                children = {
                    UI.Label { text = "持有: " .. stockDisplay, fontSize = 11, fontColor = Theme.TEXT_SECONDARY },
                    UI.Label { text = histText, fontSize = 10, fontColor = Theme.TEXT_MUTED },
                },
            },

            -- 分隔线
            UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER_LIGHT },

            -- 第三行: 买卖按钮
            UI.Panel {
                width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                children = {
                    -- 购买按钮组
                    UI.Panel {
                        flexDirection = "row", gap = 4, alignItems = "center",
                        children = buyButtons,
                    },
                    -- 出售按钮组
                    UI.Panel {
                        flexDirection = "row", gap = 4, alignItems = "center",
                        children = sellButtons,
                    },
                },
            },
        },
    }
end

-- ============================================================================
-- 集市统计面板
-- ============================================================================

local function CreateStatsPanel()
    local stats = MarketSystem.GetStats()
    local s = GameData.state
    local month = s.month
    local MONTH_NAMES = { "正月", "二月", "三月", "四月", "五月", "六月",
                          "七月", "八月", "九月", "十月", "十一月", "腊月" }
    local seasonName
    if month >= 1 and month <= 3 then seasonName = "春季"
    elseif month >= 4 and month <= 6 then seasonName = "夏季"
    elseif month >= 7 and month <= 9 then seasonName = "秋季"
    else seasonName = "冬季" end

    return UI.Panel {
        width = "100%", padding = 12, borderRadius = 8,
        backgroundColor = { 255, 248, 230, 255 },
        borderWidth = 1, borderColor = Theme.BORDER_GOLD,
        gap = 6,
        children = {
            UI.Panel {
                width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                children = {
                    UI.Panel {
                        flexDirection = "row", gap = 6, alignItems = "center",
                        children = {
                            UI.Label { text = "集", fontSize = 16, fontColor = Theme.GOLD },
                            UI.Panel {
                                gap = 1,
                                children = {
                                    UI.Label { text = (MONTH_NAMES[month] or "") .. "集市", fontSize = 14, fontColor = Theme.TEXT_TITLE },
                                    UI.Label { text = seasonName .. " · 价格随季节波动", fontSize = 10, fontColor = Theme.TEXT_MUTED },
                                },
                            },
                        },
                    },
                    UI.Panel {
                        alignItems = "flex-end", gap = 1,
                        children = {
                            UI.Label { text = "银两: " .. s.silver, fontSize = 12, fontColor = Theme.SILVER_COLOR },
                            UI.Label { text = "交易" .. stats.tradeCount .. "次", fontSize = 9, fontColor = Theme.TEXT_MUTED },
                        },
                    },
                },
            },
        },
    }
end

-- ============================================================================
-- 页面入口
-- ============================================================================

--- 创建集市交易页面
---@param pageTitle fun(title: string, subtitle: string): table
---@param gameScreen table
function MarketPage.Create(pageTitle, gameScreen)
    local s = GameData.state

    -- 确保集市状态存在
    MarketSystem.EnsureState()

    local children = {
        pageTitle("集市交易", "买卖有道，兴家立业"),
        CreateStatsPanel(),
    }

    -- 广告优惠入口
    if AdSystem.IsAvailable("market_discount") then
        local discountRemain = AdSystem.GetRemaining("market_discount")
        local stats = MarketSystem.GetStats()
        local lastSpent = stats.totalSpent or 0
        children[#children + 1] = UI.Panel {
            width = "100%", padding = 10, borderRadius = 8,
            backgroundGradient = { direction = "horizontal", from = {218, 165, 32, 255}, to = {255, 200, 50, 255} },
            flexDirection = "row", justifyContent = "space-between", alignItems = "center",
            onClick = function(self)
                -- 以本月已消费总额的20%作为返还基础
                local spent = (GameData.state.market and GameData.state.market.monthlySpent) or 0
                if spent <= 0 then
                    Toast.Show("本月尚未交易，先购买商品再来领取优惠")
                    return
                end
                AdSystem.MarketDiscount(spent, function(success, refundOrMsg)
                    if success then
                        -- 重置月度消费记录，避免重复领取
                        if GameData.state.market then
                            GameData.state.market.monthlySpent = 0
                        end
                        Toast.Show("获得返还银两 " .. refundOrMsg .. " 两！")
                        if gameScreen and gameScreen.RefreshAll then
                            gameScreen.RefreshAll()
                        end
                    else
                        Toast.Show(refundOrMsg or "广告播放失败")
                    end
                end)
            end,
            children = {
                UI.Panel {
                    flexDirection = "row", gap = 6, alignItems = "center",
                    children = {
                        UI.Label { text = "▶", fontSize = 14, fontColor = {80, 40, 0, 255} },
                        UI.Panel {
                            gap = 1,
                            children = {
                                UI.Label { text = "看广告领交易返利", fontSize = 13, fontColor = {80, 40, 0, 255} },
                                UI.Label { text = "购物后观看广告返还20%银两", fontSize = 9, fontColor = {120, 80, 20, 255} },
                            },
                        },
                    },
                },
                UI.Label { text = "剩" .. discountRemain .. "次", fontSize = 11, fontColor = {120, 80, 20, 255} },
            },
        }
    end

    -- 商品列表
    for _, commodity in ipairs(MarketSystem.COMMODITIES) do
        children[#children + 1] = CreateCommodityCard(commodity, pageTitle, gameScreen)
    end

    -- 底部提示
    children[#children + 1] = UI.Panel {
        width = "100%", padding = 10, marginTop = 4, marginBottom = 20,
        children = {
            UI.Label {
                text = "提示: 卖出价为买入价的八折。善于观察价格趋势，低买高卖可获厚利。",
                fontSize = 10, fontColor = Theme.TEXT_MUTED, whiteSpace = "normal",
            },
        },
    }

    return UI.ScrollView {
        width = "100%", flexGrow = 1, flexBasis = 0,
        backgroundColor = { 0, 0, 0, 0 },
        children = {
            UI.Panel {
                width = "100%", gap = 8, padding = 12, paddingBottom = 20,
                children = children,
            },
        },
    }
end

return MarketPage
