-- ============================================================================
-- 大明浮生志2 - 探索历练页面
-- 选择族人外出历练，消耗资源换取奖励
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")
local AudioManager = require("Systems.AudioManager")

local ExpeditionPage = {}

--- 创建探索历练页面
---@param pageTitle fun(title: string, subtitle: string): table
---@param gameScreen table
function ExpeditionPage.Create(pageTitle, gameScreen)
    local s = GameData.state
    local children = {
        pageTitle("探索历练", "行万里路，历练族人"),
    }

    -- 正在进行的历练
    if #s.expeditions > 0 then
        children[#children + 1] = UI.Label { text = "进行中的历练", fontSize = 17, fontColor = Theme.GOLD, marginTop = 4 }
        for _, exp in ipairs(s.expeditions) do
            local member = GameData.GetMember(exp.memberId)
            local expType = nil
            for _, t in ipairs(GameData.EXPEDITION_TYPES) do
                if t.id == exp.typeId then expType = t; break end
            end
            if member and expType then
                children[#children + 1] = UI.Panel {
                    width = "100%", padding = 10, borderRadius = 8,
                    backgroundColor = Theme.BG_WHITE,
                    borderWidth = 1, borderColor = Theme.BORDER,
                    gap = 4,
                    children = {
                        UI.Panel {
                            width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                            children = {
                                UI.Panel {
                                    flexDirection = "row", gap = 6, alignItems = "center",
                                    children = {
                                        UI.Label { text = expType.icon, fontSize = 18 },
                                        UI.Label { text = expType.name, fontSize = 16, fontColor = Theme.TEXT_TITLE },
                                    },
                                },
                                UI.Panel {
                                    paddingHorizontal = 8, paddingVertical = 3, borderRadius = 4,
                                    backgroundColor = { 66, 133, 244, 35 },
                                    children = { UI.Label { text = "剩余" .. exp.monthsLeft .. "月", fontSize = 13, fontColor = Theme.BLUE } },
                                },
                            },
                        },
                        UI.Label {
                            text = member.name .. "（" .. member.age .. "岁，武" .. member.martial .. "）正在外出历练",
                            fontSize = 13, fontColor = Theme.TEXT_SECONDARY, whiteSpace = "normal",
                        },
                    },
                }
            end
        end
        children[#children + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 8 }
    end

    -- 派遣新的历练
    children[#children + 1] = UI.Label { text = "选择历练类型", fontSize = 17, fontColor = Theme.GOLD, marginTop = 4 }

    -- 可派遣族人列表
    local availableMembers = {}
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.age >= 15 and m.state == "在家" and not GameData.GetMemberExpedition(m.id) then
            availableMembers[#availableMembers + 1] = m
        end
    end

    if #availableMembers == 0 then
        children[#children + 1] = UI.Label { text = "没有可派遣的族人（需年满15岁且在家）", fontSize = 13, fontColor = Theme.TEXT_MUTED }
    end

    -- 每种历练类型展示为卡片
    for _, expType in ipairs(GameData.EXPEDITION_TYPES) do
        local unlocked = GameData.IsExpeditionUnlocked(expType.id)
        local reqRank = GameData.EXPEDITION_UNLOCK[expType.id] or 1

        local costText = {}
        if expType.cost.silver and expType.cost.silver > 0 then costText[#costText + 1] = "银" .. expType.cost.silver end
        if expType.cost.grain and expType.cost.grain > 0 then costText[#costText + 1] = "粮" .. expType.cost.grain end
        local costStr = table.concat(costText, " ")
        local canAfford = unlocked and GameData.CanAfford(expType.cost.silver or 0, expType.cost.grain or 0, 0, 0)

        -- 奖励描述
        local rewardParts = {}
        if expType.rewards.silver then rewardParts[#rewardParts + 1] = "银" .. expType.rewards.silver[1] .. "~" .. expType.rewards.silver[2] end
        if expType.rewards.cloth then rewardParts[#rewardParts + 1] = "布" .. expType.rewards.cloth[1] .. "~" .. expType.rewards.cloth[2] end
        if expType.rewards.fame then rewardParts[#rewardParts + 1] = "望" .. expType.rewards.fame[1] .. "~" .. expType.rewards.fame[2] end
        if expType.rewards.study then rewardParts[#rewardParts + 1] = "学识" .. expType.rewards.study[1] .. "~" .. expType.rewards.study[2] end
        if expType.rewards.item_chance then rewardParts[#rewardParts + 1] = math.floor(expType.rewards.item_chance * 100) .. "%获物品" end
        if expType.rewards.recruit_chance then rewardParts[#rewardParts + 1] = math.floor(expType.rewards.recruit_chance * 100) .. "%招人" end
        local rewardStr = table.concat(rewardParts, "、")

        children[#children + 1] = UI.Panel {
            width = "100%", padding = 12, borderRadius = 10,
            backgroundColor = unlocked and Theme.BG_WHITE or Theme.BG_INPUT,
            borderWidth = 1, borderColor = Theme.BORDER,
            gap = 6,
            opacity = unlocked and 1.0 or 0.35,
            onTap = (not unlocked) and function(self)
                AudioManager.Click()
                gameScreen.ShowResultPopup("尚未解锁", "提升品级至【" .. GameData.GetUnlockRankName(reqRank) .. "】后可进行" .. expType.name .. "。")
            end or nil,
            children = {
                -- 标题行
                UI.Panel {
                    width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                    children = {
                        UI.Panel {
                            flexDirection = "row", gap = 6, alignItems = "center",
                            children = {
                                UI.Panel {
                                    width = 32, height = 32, borderRadius = 16,
                                    backgroundGradient = unlocked and Theme.GRADIENT_GOLD or nil,
                                    backgroundColor = unlocked and nil or Theme.BG_INPUT,
                                    justifyContent = "center", alignItems = "center",
                                    children = { UI.Label { text = unlocked and expType.icon or "[锁]", fontSize = 18 } },
                                },
                                UI.Panel {
                                    gap = 2,
                                    children = {
                                        UI.Label { text = expType.name, fontSize = 16, fontColor = unlocked and Theme.TEXT_TITLE or Theme.TEXT_MUTED },
                                        UI.Label {
                                            text = unlocked and ("耗时" .. expType.duration .. "月 | 费用：" .. costStr) or ("需品级【" .. GameData.GetUnlockRankName(reqRank) .. "】"),
                                            fontSize = 12, fontColor = Theme.TEXT_MUTED,
                                        },
                                    },
                                },
                            },
                        },
                        unlocked and UI.Panel {
                            paddingHorizontal = 6, paddingVertical = 2, borderRadius = 4,
                            backgroundColor = { 180, 60, 45, 40 },
                            children = { UI.Label { text = "风险" .. math.floor(expType.riskRate * 100) .. "%", fontSize = 11, fontColor = Theme.RED } },
                        } or nil,
                    },
                },

                -- 描述
                UI.Label { text = expType.desc, fontSize = 13, fontColor = Theme.TEXT_SECONDARY, whiteSpace = "normal" },
                unlocked and UI.Label { text = "奖励：" .. rewardStr, fontSize = 12, fontColor = Theme.GREEN, whiteSpace = "normal" } or nil,

                -- 派遣族人列表（仅解锁后显示）
                unlocked and ExpeditionPage.BuildDispatchList(expType, availableMembers, canAfford, gameScreen) or nil,
            },
        }
    end

    -- 历练策略提示
    children[#children + 1] = UI.Panel {
        width = "100%", padding = 10, borderRadius = 8, marginTop = 4,
        backgroundColor = { 66, 133, 200, 12 },
        borderWidth = 1, borderColor = { 66, 133, 200, 40 },
        gap = 4,
        children = {
            UI.Label { text = "历练须知", fontSize = 14, fontColor = Theme.BLUE },
            UI.Label {
                text = "· 武艺≥30减少30%风险，武艺≥60再减50%风险\n· 族规「习武自卫」额外降低20%遇险概率\n· 派遣前备好药材，以防族人负伤归来\n· 「深山探秘」风险最高但可获得珍稀物品",
                fontSize = 11, fontColor = Theme.TEXT_SECONDARY, whiteSpace = "normal",
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

--- 构建派遣族人选项
function ExpeditionPage.BuildDispatchList(expType, members, canAfford, gameScreen)
    if #members == 0 then
        return UI.Label { text = "无可用族人", fontSize = 12, fontColor = Theme.TEXT_MUTED }
    end

    local items = {}
    -- 最多显示5个候选人
    local showCount = math.min(5, #members)
    for i = 1, showCount do
        local m = members[i]
        local enabled = canAfford and m.state == "在家"
        items[#items + 1] = UI.Panel {
            width = "100%", paddingVertical = 4, paddingHorizontal = 8,
            borderRadius = 6, backgroundColor = { 66, 133, 244, 12 },
            flexDirection = "row", justifyContent = "space-between", alignItems = "center",
            children = {
                UI.Panel {
                    flexDirection = "row", gap = 6, alignItems = "center",
                    children = {
                        UI.Label { text = m.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY },
                        UI.Label { text = m.age .. "岁", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                        UI.Label { text = "武" .. m.martial .. " 学" .. m.study, fontSize = 12, fontColor = Theme.BLUE },
                    },
                },
                UI.Panel {
                    paddingHorizontal = 10, paddingVertical = 4, borderRadius = 5,
                    backgroundGradient = enabled and Theme.GRADIENT_PRIMARY or nil,
                    backgroundColor = enabled and nil or Theme.BG_INPUT,
                    opacity = enabled and 1.0 or 0.4,
                    onTap = function(self)
                        if not enabled then return end
                        AudioManager.Click()
                        if GameData.StartExpedition(expType.id, m.id) then
                            GameData.AddLog(m.name .. "出发前往" .. expType.name .. "。")
                            gameScreen.ShowResultPopup("出发！", m.name .. "踏上" .. expType.name .. "之路。\n预计" .. expType.duration .. "月后归来。")
                            gameScreen.RefreshAll()
                        else
                            gameScreen.ShowResultPopup("资源不足", "没有足够的资源派遣族人。")
                        end
                    end,
                    children = { UI.Label { text = "派遣", fontSize = 13, fontColor = enabled and Theme.TEXT_WHITE or Theme.TEXT_MUTED } },
                },
            },
        }
    end

    if #members > showCount then
        items[#items + 1] = UI.Label { text = "还有" .. (#members - showCount) .. "人可选...", fontSize = 12, fontColor = Theme.TEXT_MUTED }
    end

    return UI.Panel { width = "100%", gap = 3, marginTop = 4, children = items }
end

return ExpeditionPage
