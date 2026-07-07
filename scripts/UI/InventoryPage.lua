-- ============================================================================
-- 大明浮生志2 - 库房页面
-- 显示物品仓库，支持对族人使用物品
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")

local InventoryPage = {}

--- 创建库房页面
---@param pageTitle fun(title: string, subtitle: string): table
---@param gameScreen table
function InventoryPage.Create(pageTitle, gameScreen)
    local s = GameData.state
    local children = {
        pageTitle("库房", "家族珍藏，物尽其用"),
    }

    -- 统计物品总数
    local totalItems = 0
    for _, slot in ipairs(s.inventory) do
        totalItems = totalItems + slot.count
    end

    children[#children + 1] = UI.Label {
        text = "共有 " .. totalItems .. " 件物品",
        fontSize = 14, fontColor = Theme.TEXT_SECONDARY,
    }

    if totalItems == 0 then
        children[#children + 1] = UI.Panel {
            width = "100%", paddingVertical = 40,
            justifyContent = "center", alignItems = "center",
            children = {
                UI.Label { text = "库房空空如也", fontSize = 18, fontColor = Theme.TEXT_MUTED },
                UI.Label { text = "通过探索历练可以获得物品", fontSize = 13, fontColor = Theme.TEXT_MUTED, marginTop = 8 },
            },
        }
    else
        -- 物品列表
        for _, slot in ipairs(s.inventory) do
            if slot.count > 0 then
                local itemType = GameData.GetItemType(slot.itemId)
                if itemType then
                    local rarityColor = GameData.RARITY_COLORS[itemType.rarity] or Theme.TEXT_PRIMARY
                    children[#children + 1] = InventoryPage.BuildItemCard(slot, itemType, rarityColor, gameScreen)
                end
            end
        end
    end

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

--- 构建单个物品卡片
function InventoryPage.BuildItemCard(slot, itemType, rarityColor, gameScreen)
    -- 判断是否需要选择族人使用
    local needsMember = (itemType.useEffect == "heal" or itemType.useEffect == "study_boost"
        or itemType.useEffect == "martial_boost" or itemType.useEffect == "martial_boost_large")

    local cardChildren = {
        -- 头部：图标+名称+数量
        UI.Panel {
            width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
            children = {
                UI.Panel {
                    flexDirection = "row", gap = 8, alignItems = "center",
                    children = {
                        UI.Panel {
                            width = 36, height = 36, borderRadius = 8,
                            backgroundColor = { rarityColor[1], rarityColor[2], rarityColor[3], 40 },
                            borderWidth = 1, borderColor = rarityColor,
                            justifyContent = "center", alignItems = "center",
                            children = { UI.Label { text = itemType.icon, fontSize = 20 } },
                        },
                        UI.Panel {
                            gap = 2,
                            children = {
                                UI.Label { text = itemType.name, fontSize = 16, fontColor = rarityColor },
                                UI.Label { text = itemType.desc, fontSize = 12, fontColor = Theme.TEXT_SECONDARY },
                            },
                        },
                    },
                },
                UI.Panel {
                    paddingHorizontal = 8, paddingVertical = 3, borderRadius = 6,
                    backgroundColor = Theme.BG_INPUT,
                    children = { UI.Label { text = "x" .. slot.count, fontSize = 15, fontColor = Theme.TEXT_PRIMARY } },
                },
            },
        },
    }

    -- 不需要成员的物品，直接使用按钮
    if not needsMember then
        cardChildren[#cardChildren + 1] = UI.Panel {
            width = "100%", flexDirection = "row", justifyContent = "flex-end",
            children = {
                UI.Panel {
                    paddingHorizontal = 14, paddingVertical = 5, borderRadius = 6,
                    backgroundGradient = Theme.GRADIENT_PRIMARY,
                    onTap = function(self)
                        local ok, msg = GameData.UseItem(slot.itemId, nil)
                        if ok then
                            gameScreen.ShowResultPopup("使用成功", msg)
                        else
                            gameScreen.ShowResultPopup("使用失败", msg)
                        end
                        gameScreen.RefreshAll()
                    end,
                    children = { UI.Label { text = "使用", fontSize = 14, fontColor = Theme.TEXT_WHITE } },
                },
            },
        }
    else
        -- 需要选择族人
        local members = GameData.GetAliveMembers()
        local targets = {}
        for _, m in ipairs(members) do
            if itemType.useEffect == "heal" then
                if m.health < 80 or m.state == "生病" then targets[#targets + 1] = m end
            else
                targets[#targets + 1] = m
            end
        end

        if #targets == 0 then
            cardChildren[#cardChildren + 1] = UI.Label { text = "暂无合适使用对象", fontSize = 12, fontColor = Theme.TEXT_MUTED }
        else
            local memberItems = {}
            local showCount = math.min(4, #targets)
            for i = 1, showCount do
                local m = targets[i]
                local statText = ""
                if itemType.useEffect == "heal" then
                    statText = "血" .. m.health
                elseif itemType.useEffect == "study_boost" then
                    statText = "学" .. m.study
                else
                    statText = "武" .. m.martial
                end

                memberItems[#memberItems + 1] = UI.Panel {
                    width = "100%", paddingVertical = 4, paddingHorizontal = 8,
                    borderRadius = 5, backgroundColor = { 66, 133, 244, 12 },
                    flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                    children = {
                        UI.Panel {
                            flexDirection = "row", gap = 6, alignItems = "center",
                            children = {
                                UI.Label { text = m.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY },
                                UI.Label { text = statText, fontSize = 12, fontColor = Theme.BLUE },
                            },
                        },
                        UI.Panel {
                            paddingHorizontal = 10, paddingVertical = 3, borderRadius = 5,
                            backgroundGradient = Theme.GRADIENT_PRIMARY,
                            onTap = function(self)
                                local ok, msg = GameData.UseItem(slot.itemId, m)
                                if ok then
                                    gameScreen.ShowResultPopup("使用成功", msg)
                                else
                                    gameScreen.ShowResultPopup("使用失败", msg)
                                end
                                gameScreen.RefreshAll()
                            end,
                            children = { UI.Label { text = "使用", fontSize = 12, fontColor = Theme.TEXT_WHITE } },
                        },
                    },
                }
            end
            if #targets > showCount then
                memberItems[#memberItems + 1] = UI.Label { text = "还有" .. (#targets - showCount) .. "人...", fontSize = 12, fontColor = Theme.TEXT_MUTED }
            end
            cardChildren[#cardChildren + 1] = UI.Panel { width = "100%", gap = 3, marginTop = 4, children = memberItems }
        end
    end

    return UI.Panel {
        width = "100%", padding = 12, borderRadius = 10,
        backgroundColor = Theme.BG_WHITE,
        borderWidth = 1, borderColor = Theme.BORDER,
        gap = 6,
        children = cardChildren,
    }
end

return InventoryPage
