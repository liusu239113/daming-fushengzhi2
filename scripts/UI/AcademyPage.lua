-- ============================================================================
-- 大明浮生志2 - 书院武馆培养页面
-- 管理族学/武馆设施，分配族人到席位，升级设施
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")
local AudioManager = require("Systems.AudioManager")

local AcademyPage = {}

--- 创建书院武馆页面
---@param pageTitle fun(title: string, subtitle: string): table
---@param gameScreen table
function AcademyPage.Create(pageTitle, gameScreen)
    local s = GameData.state
    local children = {
        pageTitle("书院武馆", "修文习武，培育英才"),
    }

    -- 遍历所有设施
    for _, academy in ipairs(s.academies) do
        local aType = GameData.GetAcademyType(academy.typeId)
        if not aType then goto continueAcad end

        local maxSlots = GameData.GetAcademySlots(academy)
        local upgradeCost = aType.upgradeCost * academy.level
        local canUpgrade = GameData.CanAfford(upgradeCost, 0, 0, 0)

        -- 设施标题
        children[#children + 1] = UI.Panel {
            width = "100%", padding = 12, borderRadius = 10,
            backgroundColor = Theme.BG_WHITE,
            borderWidth = 1, borderColor = Theme.BORDER,
            gap = 8,
            children = {
                -- 头部：名称 + 等级 + 升级按钮
                UI.Panel {
                    width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                    children = {
                        UI.Panel {
                            flexDirection = "row", gap = 8, alignItems = "center",
                            children = {
                                UI.Panel {
                                    width = 36, height = 36, borderRadius = 18,
                                    backgroundGradient = Theme.GRADIENT_GOLD,
                                    justifyContent = "center", alignItems = "center",
                                    children = { UI.Label { text = aType.icon, fontSize = 20 } },
                                },
                                UI.Panel {
                                    gap = 2,
                                    children = {
                                        UI.Label { text = aType.name .. " Lv." .. academy.level, fontSize = 17, fontColor = Theme.TEXT_TITLE },
                                        UI.Label { text = "席位 " .. #academy.memberIds .. "/" .. maxSlots, fontSize = 13, fontColor = Theme.TEXT_SECONDARY },
                                    },
                                },
                            },
                        },
                        UI.Panel {
                            paddingHorizontal = 10, paddingVertical = 5, borderRadius = 6,
                            backgroundGradient = canUpgrade and Theme.GRADIENT_PRIMARY or nil,
                            backgroundColor = canUpgrade and nil or Theme.BG_INPUT,
                            opacity = canUpgrade and 1.0 or 0.5,
                            onTap = function(self)
                                if not canUpgrade then return end
                                AudioManager.Click()
                                if GameData.SpendResources(upgradeCost, 0, 0, 0) then
                                    academy.level = academy.level + 1
                                    GameData.AddLog(aType.name .. "升至" .. academy.level .. "级。")
                                    gameScreen.RefreshAll()
                                end
                            end,
                            children = {
                                UI.Label {
                                    text = "升级(-" .. upgradeCost .. "银)",
                                    fontSize = 13,
                                    fontColor = canUpgrade and Theme.TEXT_WHITE or Theme.TEXT_MUTED,
                                },
                            },
                        },
                    },
                },

                -- 描述
                UI.Label { text = aType.desc .. "（每级" .. aType.baseSlotsPerLevel .. "席位，额外" .. math.floor(aType.baseBonus * 100) .. "%" .. (aType.attribute == "study" and "学识" or "武艺") .. "成长/等级）",
                    fontSize = 12, fontColor = Theme.TEXT_MUTED, whiteSpace = "normal" },

                -- 已分配成员列表
                AcademyPage.BuildMemberList(academy, aType, gameScreen),

                -- 分配新成员按钮
                AcademyPage.BuildAssignSection(academy, aType, maxSlots, gameScreen),
            },
        }

        ::continueAcad::
    end

    -- 新建设施按钮
    children[#children + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 8 }
    children[#children + 1] = UI.Label { text = "新建设施", fontSize = 17, fontColor = Theme.GOLD, marginTop = 4 }

    for _, aType in ipairs(GameData.ACADEMY_TYPES) do
        local existing = GameData.GetAcademy(aType.id)
        if not existing then
            local unlocked = GameData.IsAcademyUnlocked(aType.id)
            local reqRank = GameData.ACADEMY_UNLOCK[aType.id] or 1
            local buildCost = aType.upgradeCost
            local canBuild = unlocked and GameData.CanAfford(buildCost, 0, 0, 0)
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 10, borderRadius = 8,
                backgroundColor = Theme.BG_INPUT,
                flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                opacity = (unlocked and canBuild) and 1.0 or (unlocked and 0.5 or 0.35),
                children = {
                    UI.Panel {
                        flexDirection = "row", gap = 6, alignItems = "center",
                        children = {
                            UI.Label { text = unlocked and aType.icon or "[锁]", fontSize = 18 },
                            UI.Panel {
                                gap = 2,
                                children = {
                                    UI.Label { text = aType.name, fontSize = 15, fontColor = unlocked and Theme.TEXT_PRIMARY or Theme.TEXT_MUTED },
                                    UI.Label {
                                        text = unlocked and aType.desc or ("需品级【" .. GameData.GetUnlockRankName(reqRank) .. "】解锁"),
                                        fontSize = 12, fontColor = unlocked and Theme.TEXT_SECONDARY or Theme.TEXT_MUTED,
                                    },
                                },
                            },
                        },
                    },
                    UI.Panel {
                        paddingHorizontal = 10, paddingVertical = 5, borderRadius = 6,
                        backgroundGradient = canBuild and Theme.GRADIENT_PRIMARY or nil,
                        backgroundColor = canBuild and nil or Theme.BG_INPUT,
                        onTap = function(self)
                            AudioManager.Click()
                            if not unlocked then
                                gameScreen.ShowResultPopup("尚未解锁", "提升品级至【" .. GameData.GetUnlockRankName(reqRank) .. "】后可建造" .. aType.name .. "。")
                                return
                            end
                            if not canBuild then return end
                            if GameData.SpendResources(buildCost, 0, 0, 0) then
                                GameData.AddAcademy(aType.id)
                                GameData.AddLog("建成" .. aType.name .. "。")
                                gameScreen.RefreshAll()
                            end
                        end,
                        children = {
                            UI.Label {
                                text = unlocked and ("建造(-" .. buildCost .. "银)") or "[锁] 未解锁",
                                fontSize = 13,
                                fontColor = canBuild and Theme.TEXT_WHITE or Theme.TEXT_MUTED,
                            },
                        },
                    },
                },
            }
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

--- 构建已分配成员列表
function AcademyPage.BuildMemberList(academy, aType, gameScreen)
    local items = {}
    for _, mid in ipairs(academy.memberIds) do
        local m = GameData.GetMember(mid)
        if m and m.alive then
            local attrVal = m[aType.attribute] or 0
            items[#items + 1] = UI.Panel {
                width = "100%", paddingVertical = 6, paddingHorizontal = 10,
                borderRadius = 6, backgroundColor = Theme.BG_INPUT,
                flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                children = {
                    UI.Panel {
                        flexDirection = "row", gap = 6, alignItems = "center",
                        children = {
                            UI.Label { text = m.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY },
                            UI.Label { text = m.age .. "岁", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                            UI.Label { text = (aType.attribute == "study" and "学" or "武") .. attrVal, fontSize = 12, fontColor = Theme.BLUE },
                            m.talent and UI.Label { text = m.talent.name, fontSize = 11, fontColor = Theme.GOLD } or nil,
                        },
                    },
                    UI.Panel {
                        paddingHorizontal = 8, paddingVertical = 3, borderRadius = 4,
                        backgroundColor = { 180, 60, 45, 40 }, borderWidth = 1, borderColor = Theme.RED_DARK,
                        onTap = function(self)
                            AudioManager.Click()
                            GameData.RemoveFromAcademy(academy.typeId, mid)
                            GameData.AddLog(m.name .. "离开" .. aType.name .. "。")
                            gameScreen.RefreshAll()
                        end,
                        children = { UI.Label { text = "撤回", fontSize = 12, fontColor = Theme.RED } },
                    },
                },
            }
        end
    end

    if #items == 0 then
        return UI.Label { text = "暂无族人在此学习", fontSize = 13, fontColor = Theme.TEXT_MUTED, marginTop = 4 }
    end

    return UI.Panel { width = "100%", gap = 4, children = items }
end

--- 构建分配新成员区域
function AcademyPage.BuildAssignSection(academy, aType, maxSlots, gameScreen)
    if #academy.memberIds >= maxSlots then
        return UI.Label { text = "席位已满，请升级设施", fontSize = 12, fontColor = Theme.TEXT_MUTED }
    end

    -- 筛选可分配的族人（在家、年龄合适、不在其他书院）
    local candidates = {}
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.age >= 6 and m.state == "在家" and not GameData.IsInAcademy(m.id) then
            candidates[#candidates + 1] = m
        end
    end

    if #candidates == 0 then
        return UI.Label { text = "无可分配族人", fontSize = 12, fontColor = Theme.TEXT_MUTED }
    end

    local items = {}
    items[#items + 1] = UI.Label { text = "── 可分配 ──", fontSize = 12, fontColor = Theme.TEXT_MUTED, textAlign = "center" }

    for _, m in ipairs(candidates) do
        local attrVal = m[aType.attribute] or 0
        items[#items + 1] = UI.Panel {
            width = "100%", paddingVertical = 5, paddingHorizontal = 10,
            borderRadius = 6, backgroundColor = { 66, 133, 244, 18 },
            flexDirection = "row", justifyContent = "space-between", alignItems = "center",
            children = {
                UI.Panel {
                    flexDirection = "row", gap = 6, alignItems = "center",
                    children = {
                        UI.Label { text = m.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY },
                        UI.Label { text = m.age .. "岁", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                        UI.Label { text = (aType.attribute == "study" and "学" or "武") .. attrVal, fontSize = 12, fontColor = Theme.BLUE },
                    },
                },
                UI.Panel {
                    paddingHorizontal = 8, paddingVertical = 3, borderRadius = 4,
                    backgroundColor = { 66, 133, 244, 35 }, borderWidth = 1, borderColor = Theme.BLUE,
                    onTap = function(self)
                        AudioManager.Click()
                        if GameData.AssignToAcademy(academy.typeId, m.id) then
                            GameData.AddLog(m.name .. "进入" .. aType.name .. "学习。")
                            gameScreen.RefreshAll()
                        end
                    end,
                    children = { UI.Label { text = "分配", fontSize = 12, fontColor = Theme.BLUE } },
                },
            },
        }
    end

    return UI.Panel { width = "100%", gap = 4, marginTop = 4, children = items }
end

return AcademyPage
