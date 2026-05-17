-- ============================================================================
-- 大明浮生志2 - 讨伐准备页面（两级地图系统重写版）
-- 四阶段：1. 区域选择+征兵训练  2. 关卡选择  3. 族人选择  4. 兵力分配
-- 世家(rank 5)解锁
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")
local RivalClans = require("Data.RivalClans")
local CampaignRegions = require("Data.CampaignRegions")
local AudioManager = require("Systems.AudioManager")
local Toast = require("Systems.Toast")

local BattlePrepPage = {}

-- ============================================================================
-- 状态
-- ============================================================================

local selectedArea_ = nil         -- 选中的区域数据
local selectedStage_ = nil        -- 选中的关卡数据
local selectedRival_ = nil        -- 生成的敌族数据
local selectedMembers_ = {}       -- 选中的出战族人ID集合 { [memberId] = true }
local phase_ = "area_select"      -- "area_select" | "stage_select" | "member_select" | "troop_assign"
local gameScreen_ = nil           -- GameScreen 引用
local MAX_DEPLOY = 6              -- 最多出战族人数

-- 兵力分配
local deployInfantry_ = 0
local deployArchers_ = 0
local skipReset_ = false  -- Refresh时跳过状态重置

-- ============================================================================
-- 辅助工具
-- ============================================================================

local function GetSelectedCount()
    local count = 0
    for _ in pairs(selectedMembers_) do count = count + 1 end
    return count
end

local function FormatNumber(n)
    if n >= 10000 then
        return string.format("%.1fw", n / 10000)
    end
    return tostring(n)
end

-- ============================================================================
-- 征兵弹窗
-- ============================================================================

local function ShowConscriptModal(unitType)
    local typeName = unitType == "archers" and "弓兵" or "步兵"
    local current = (unitType == "archers") and RivalClans.GetPlayerArchers() or RivalClans.GetPlayerInfantry()
    local total = RivalClans.GetPlayerSoldierCount()
    local canRecruit = math.max(0, RivalClans.MAX_ARMY_SIZE - total)
    canRecruit = math.floor(canRecruit / 100) * 100

    local amount = math.min(500, canRecruit)
    if amount <= 0 then
        Toast.Show("兵力已达上限（" .. RivalClans.MAX_ARMY_SIZE .. "）")
        return
    end

    local ModalDialogs = require("UI.ModalDialogs")
    if ModalDialogs and ModalDialogs.ShowChoiceDialog then
        local options = {}
        local amounts = { 100, 300, 500, 1000, 2000, 5000 }
        for _, amt in ipairs(amounts) do
            if amt <= canRecruit then
                local b = amt / 100
                local sc = RivalClans.CONSCRIPT_COST.silver * b
                local gc = RivalClans.CONSCRIPT_COST.grain * b
                local affordable = GameData.CanAfford(sc, gc, 0, 0)
                options[#options + 1] = {
                    text = typeName .. " +" .. amt .. "人（银" .. sc .. " 粮" .. gc .. "）" .. (affordable and "" or " [不足]"),
                    effect = function()
                        local ok, msg = RivalClans.Conscript(unitType, amt)
                        Toast.Show(msg)
                        BattlePrepPage.Refresh()
                    end,
                }
            end
        end
        options[#options + 1] = { text = "取消", effect = function() end }

        ModalDialogs.ShowChoiceDialog(
            "征募" .. typeName,
            "当前" .. typeName .. "：" .. current .. "人\n可征上限：" .. canRecruit .. "人\n每100人费用：银两" .. RivalClans.CONSCRIPT_COST.silver .. "、粮食" .. RivalClans.CONSCRIPT_COST.grain,
            options
        )
    else
        local ok, msg = RivalClans.Conscript(unitType, 500)
        Toast.Show(msg)
        BattlePrepPage.Refresh()
    end
end

-- ============================================================================
-- 阶段1：区域选择 + 军队管理
-- ============================================================================

local function CreateAreaCard(area)
    local isConquered = CampaignRegions.IsAreaConquered(area.id)
    local isUnlocked = CampaignRegions.IsAreaUnlocked(area.id)
    local conquered, total = CampaignRegions.GetAreaProgress(area.id)

    -- 颜色
    local borderColor, bgColor, labelColor
    if isConquered then
        borderColor = { 100, 200, 100, 180 }
        bgColor = { 100, 200, 100, 15 }
        labelColor = { 100, 200, 100, 255 }
    elseif isUnlocked then
        borderColor = Theme.GOLD_BORDER or { 200, 160, 50, 200 }
        bgColor = { 200, 160, 50, 15 }
        labelColor = Theme.GOLD or { 200, 160, 50, 255 }
    else
        borderColor = { 120, 120, 120, 100 }
        bgColor = { 120, 120, 120, 10 }
        labelColor = { 120, 120, 120, 200 }
    end

    local statusText
    if isConquered then
        statusText = "已征服"
    elseif isUnlocked then
        statusText = conquered .. "/" .. total
    else
        statusText = "未解锁"
    end

    -- 区域内关卡的兵力范围
    local minSoldier = area.stages[1].soldierRange[1]
    local maxSoldier = area.stages[#area.stages].soldierRange[2]

    return UI.Panel {
        width = "100%", padding = 10, borderRadius = 8,
        backgroundColor = bgColor,
        borderWidth = 1, borderColor = borderColor,
        flexDirection = "row", justifyContent = "space-between", alignItems = "center",
        opacity = (not isUnlocked) and 0.5 or 1.0,
        onTap = function()
            if not isUnlocked then
                Toast.Show("需先征服前一区域")
                return
            end
            AudioManager.Select()
            selectedArea_ = area
            phase_ = "stage_select"
            BattlePrepPage.Refresh()
        end,
        children = {
            -- 左侧信息
            UI.Panel {
                gap = 2, flexShrink = 1,
                children = {
                    UI.Panel {
                        flexDirection = "row", gap = 6, alignItems = "center",
                        children = {
                            UI.Label { text = area.id .. ".", fontSize = 11, fontColor = labelColor },
                            UI.Label { text = area.name, fontSize = 14, fontColor = labelColor, fontWeight = "bold" },
                            isConquered and UI.Label { text = "V", fontSize = 11, fontColor = { 100, 200, 100, 255 } } or nil,
                        },
                    },
                    UI.Label { text = area.desc, fontSize = 10, fontColor = Theme.TEXT_MUTED },
                    UI.Panel {
                        flexDirection = "row", gap = 8,
                        children = {
                            UI.Label { text = "敌兵" .. FormatNumber(minSoldier) .. "~" .. FormatNumber(maxSoldier), fontSize = 10, fontColor = Theme.TEXT_SECONDARY },
                            UI.Label { text = total .. "关", fontSize = 10, fontColor = Theme.TEXT_SECONDARY },
                        },
                    },
                },
            },
            -- 右侧状态
            UI.Panel {
                paddingHorizontal = 8, paddingVertical = 4, borderRadius = 4,
                backgroundColor = isConquered and { 100, 200, 100, 30 } or (isUnlocked and { 200, 160, 50, 30 } or { 120, 120, 120, 20 }),
                children = {
                    UI.Label { text = statusText, fontSize = 11, fontColor = labelColor, fontWeight = "bold" },
                },
            },
        },
    }
end

local function CreateAreaSelectView()
    local s = GameData.state
    local infantry = RivalClans.GetPlayerInfantry()
    local archers = RivalClans.GetPlayerArchers()
    local total = infantry + archers
    local trainingLevel = RivalClans.GetTrainingLevel()
    local levelName = RivalClans.GetTrainingLevelName(trainingLevel)

    -- 总进度
    local totalConquered = CampaignRegions.GetConqueredStageCount()
    local totalStages = CampaignRegions.GetTotalStageCount()

    -- 区域列表
    local areas = CampaignRegions.GetAllAreas()
    local areaCards = {}
    for _, a in ipairs(areas) do
        areaCards[#areaCards + 1] = CreateAreaCard(a)
    end

    return UI.ScrollView {
        width = "100%", flexGrow = 1, flexBasis = 0,
        backgroundColor = { 0, 0, 0, 0 },
        children = {
            UI.Panel {
                width = "100%", gap = 10, padding = 12, paddingBottom = 20,
                children = {
                    -- 标题
                    UI.Panel {
                        width = "100%", padding = { 12, 16, 8, 16 },
                        borderBottomWidth = 1, borderBottomColor = Theme.BORDER,
                        backgroundColor = Theme.NAV_BG,
                        children = {
                            UI.Panel {
                                flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                                children = {
                                    UI.Label { text = "征伐天下", fontSize = 18, fontColor = Theme.GOLD, letterSpacing = 2 },
                                    UI.Label { text = totalConquered .. "/" .. totalStages .. " 关", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                                },
                            },
                            UI.Label { text = "选择区域，逐步征服天下", fontSize = 11, fontColor = Theme.TEXT_MUTED, marginTop = 2 },
                        },
                    },

                    -- 军队概况
                    UI.Panel {
                        width = "100%", padding = 10, borderRadius = 8,
                        backgroundColor = { 56, 168, 120, 15 },
                        borderWidth = 1, borderColor = Theme.PRIMARY,
                        gap = 8,
                        children = {
                            UI.Label { text = "我军兵力", fontSize = 13, fontColor = Theme.PRIMARY, fontWeight = "bold" },
                            UI.Panel {
                                flexDirection = "row", justifyContent = "space-around",
                                children = {
                                    UI.Panel { alignItems = "center", gap = 2, children = {
                                        UI.Label { text = "步兵", fontSize = 10, fontColor = Theme.TEXT_MUTED },
                                        UI.Label { text = FormatNumber(infantry), fontSize = 15, fontColor = Theme.PRIMARY, fontWeight = "bold" },
                                    }},
                                    UI.Panel { alignItems = "center", gap = 2, children = {
                                        UI.Label { text = "弓兵", fontSize = 10, fontColor = Theme.TEXT_MUTED },
                                        UI.Label { text = FormatNumber(archers), fontSize = 15, fontColor = Theme.PRIMARY, fontWeight = "bold" },
                                    }},
                                    UI.Panel { alignItems = "center", gap = 2, children = {
                                        UI.Label { text = "总兵力", fontSize = 10, fontColor = Theme.TEXT_MUTED },
                                        UI.Label { text = FormatNumber(total) .. "/" .. FormatNumber(RivalClans.MAX_ARMY_SIZE), fontSize = 13, fontColor = Theme.TEXT_PRIMARY },
                                    }},
                                    UI.Panel { alignItems = "center", gap = 2, children = {
                                        UI.Label { text = "训练", fontSize = 10, fontColor = Theme.TEXT_MUTED },
                                        UI.Label { text = "Lv" .. trainingLevel .. " " .. levelName, fontSize = 13, fontColor = Theme.GOLD, fontWeight = "bold" },
                                    }},
                                },
                            },
                            -- 月耗提示
                            total > 0 and UI.Label {
                                text = "月耗：银两" .. math.ceil(total / 1000 * 1200) .. " 粮食" .. math.ceil(total / 1000 * 1500),
                                fontSize = 10, fontColor = Theme.TEXT_MUTED,
                            } or nil,
                        },
                    },

                    -- 操作按钮行
                    UI.Panel {
                        width = "100%", flexDirection = "row", gap = 8, justifyContent = "center",
                        children = {
                            UI.Panel {
                                flexGrow = 1, flexBasis = 0,
                                paddingVertical = 8, borderRadius = 6,
                                backgroundColor = Theme.PRIMARY, justifyContent = "center", alignItems = "center",
                                onTap = function()
                                    AudioManager.Click()
                                    ShowConscriptModal("infantry")
                                end,
                                children = { UI.Label { text = "征步兵", fontSize = 13, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" } },
                            },
                            UI.Panel {
                                flexGrow = 1, flexBasis = 0,
                                paddingVertical = 8, borderRadius = 6,
                                backgroundColor = { 66, 133, 244, 255 }, justifyContent = "center", alignItems = "center",
                                onTap = function()
                                    AudioManager.Click()
                                    ShowConscriptModal("archers")
                                end,
                                children = { UI.Label { text = "征弓兵", fontSize = 13, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" } },
                            },
                            UI.Panel {
                                flexGrow = 1, flexBasis = 0,
                                paddingVertical = 8, borderRadius = 6,
                                backgroundColor = trainingLevel < RivalClans.MAX_TRAINING_LEVEL and { 200, 160, 50, 255 } or Theme.BG_INPUT,
                                justifyContent = "center", alignItems = "center",
                                onTap = function()
                                    if trainingLevel >= RivalClans.MAX_TRAINING_LEVEL then
                                        Toast.Show("已达最高训练等级")
                                        return
                                    end
                                    if total <= 0 then
                                        Toast.Show("没有士兵可训练")
                                        return
                                    end
                                    AudioManager.Click()
                                    local ok, msg = RivalClans.TrainArmy(false)
                                    Toast.Show(msg)
                                    BattlePrepPage.Refresh()
                                end,
                                children = {
                                    UI.Label {
                                        text = trainingLevel < RivalClans.MAX_TRAINING_LEVEL
                                            and ("训练 银" .. RivalClans.TRAINING_COST.silver .. " 粮" .. RivalClans.TRAINING_COST.grain)
                                            or "训练已满",
                                        fontSize = 11, fontColor = Theme.TEXT_WHITE, fontWeight = "bold",
                                    },
                                },
                            },
                        },
                    },

                    -- 区域列表标题
                    UI.Panel {
                        width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                        marginTop = 4,
                        children = {
                            UI.Label { text = "战役区域", fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                            UI.Label { text = "逐步征服解锁下一区域", fontSize = 10, fontColor = Theme.TEXT_MUTED },
                        },
                    },

                    -- 区域卡片
                    table.unpack(areaCards),
                },
            },
        },
    }
end

-- ============================================================================
-- 阶段2：关卡选择
-- ============================================================================

local function CreateStageCard(stage)
    local isConquered = CampaignRegions.IsStageConquered(stage.id)
    local isUnlocked = CampaignRegions.IsStageUnlocked(stage.id)

    local borderColor, bgColor, labelColor
    if isConquered then
        borderColor = { 100, 200, 100, 180 }
        bgColor = { 100, 200, 100, 15 }
        labelColor = { 100, 200, 100, 255 }
    elseif isUnlocked then
        borderColor = Theme.GOLD_BORDER or { 200, 160, 50, 200 }
        bgColor = { 200, 160, 50, 15 }
        labelColor = Theme.GOLD or { 200, 160, 50, 255 }
    else
        borderColor = { 120, 120, 120, 100 }
        bgColor = { 120, 120, 120, 10 }
        labelColor = { 120, 120, 120, 200 }
    end

    local statusText = isConquered and "已通关" or (isUnlocked and "可挑战" or "未解锁")

    -- Boss关卡特殊标识
    local namePrefix = stage.isBoss and "BOSS " or ""

    return UI.Panel {
        width = "100%", padding = 10, borderRadius = 8,
        backgroundColor = bgColor,
        borderWidth = stage.isBoss and 2 or 1, borderColor = borderColor,
        flexDirection = "row", justifyContent = "space-between", alignItems = "center",
        opacity = (not isUnlocked) and 0.5 or 1.0,
        onTap = function()
            if not isUnlocked then
                Toast.Show("需先通过前一关卡")
                return
            end
            if isConquered then
                Toast.Show("此关卡已通关")
                return
            end
            AudioManager.Select()
            selectedStage_ = stage
            selectedRival_ = CampaignRegions.GenerateStageEnemy(stage.id)
            selectedMembers_ = {}
            phase_ = "member_select"
            BattlePrepPage.Refresh()
        end,
        children = {
            -- 左侧信息
            UI.Panel {
                gap = 2, flexShrink = 1,
                children = {
                    UI.Panel {
                        flexDirection = "row", gap = 6, alignItems = "center",
                        children = {
                            stage.isBoss and UI.Panel {
                                paddingHorizontal = 4, paddingVertical = 1, borderRadius = 3,
                                backgroundColor = { 220, 80, 60, 40 },
                                children = { UI.Label { text = "BOSS", fontSize = 9, fontColor = { 220, 80, 60, 255 }, fontWeight = "bold" } },
                            } or nil,
                            UI.Label { text = stage.name, fontSize = 14, fontColor = labelColor, fontWeight = "bold" },
                        },
                    },
                    UI.Label { text = stage.desc, fontSize = 10, fontColor = Theme.TEXT_MUTED },
                    UI.Panel {
                        flexDirection = "row", gap = 8,
                        children = {
                            UI.Label { text = "敌兵" .. FormatNumber(stage.soldierRange[1]) .. "~" .. FormatNumber(stage.soldierRange[2]), fontSize = 10, fontColor = Theme.TEXT_SECONDARY },
                            UI.Label { text = "敌将" .. stage.memberRange[1] .. "~" .. stage.memberRange[2] .. "人", fontSize = 10, fontColor = Theme.TEXT_SECONDARY },
                        },
                    },
                },
            },
            -- 右侧状态
            UI.Panel {
                paddingHorizontal = 8, paddingVertical = 4, borderRadius = 4,
                backgroundColor = isConquered and { 100, 200, 100, 30 } or (isUnlocked and { 200, 160, 50, 30 } or { 120, 120, 120, 20 }),
                children = {
                    UI.Label { text = statusText, fontSize = 11, fontColor = labelColor, fontWeight = "bold" },
                },
            },
        },
    }
end

local function CreateStageSelectView()
    local area = selectedArea_
    if not area then return UI.Panel {} end

    local conquered, total = CampaignRegions.GetAreaProgress(area.id)

    local stageCards = {}
    for _, stage in ipairs(area.stages) do
        stageCards[#stageCards + 1] = CreateStageCard(stage)
    end

    return UI.ScrollView {
        width = "100%", flexGrow = 1, flexBasis = 0,
        backgroundColor = { 0, 0, 0, 0 },
        children = {
            UI.Panel {
                width = "100%", gap = 8, padding = 12, paddingBottom = 20,
                children = {
                    -- 标题 + 返回
                    UI.Panel {
                        width = "100%", flexDirection = "row", alignItems = "center", gap = 8,
                        padding = { 8, 12, 8, 12 },
                        borderBottomWidth = 1, borderBottomColor = Theme.BORDER,
                        children = {
                            UI.Panel {
                                paddingHorizontal = 8, paddingVertical = 4, borderRadius = 4,
                                backgroundColor = Theme.BG_INPUT,
                                onTap = function()
                                    AudioManager.Click()
                                    phase_ = "area_select"
                                    selectedArea_ = nil
                                    BattlePrepPage.Refresh()
                                end,
                                children = { UI.Label { text = "< 返回", fontSize = 12, fontColor = Theme.TEXT_SECONDARY } },
                            },
                            UI.Panel {
                                gap = 1, flexShrink = 1,
                                children = {
                                    UI.Label { text = area.name, fontSize = 16, fontColor = Theme.GOLD, fontWeight = "bold" },
                                    UI.Label { text = area.desc .. "  进度 " .. conquered .. "/" .. total, fontSize = 10, fontColor = Theme.TEXT_MUTED },
                                },
                            },
                        },
                    },

                    -- 关卡列表
                    table.unpack(stageCards),
                },
            },
        },
    }
end

-- ============================================================================
-- 阶段3：族人选择
-- ============================================================================

local function CreateDeployMemberCard(member)
    local isSelected = selectedMembers_[member.id] == true

    return UI.Panel {
        width = "100%", padding = 10, borderRadius = 8,
        backgroundColor = isSelected and { 56, 168, 120, 20 } or Theme.BG_WHITE,
        borderWidth = isSelected and 2 or 1,
        borderColor = isSelected and Theme.PRIMARY or Theme.BORDER,
        flexDirection = "row", justifyContent = "space-between", alignItems = "center",
        onTap = function()
            AudioManager.Click()
            if isSelected then
                selectedMembers_[member.id] = nil
            else
                if GetSelectedCount() >= MAX_DEPLOY then
                    Toast.Show("最多选择" .. MAX_DEPLOY .. "名族人出战")
                    return
                end
                selectedMembers_[member.id] = true
            end
            BattlePrepPage.Refresh()
        end,
        children = {
            -- 左侧：信息
            UI.Panel {
                gap = 3, flexShrink = 1,
                children = {
                    UI.Panel {
                        flexDirection = "row", gap = 4, alignItems = "center",
                        children = {
                            UI.Label { text = member.name, fontSize = 13, fontColor = Theme.TEXT_PRIMARY },
                            UI.Label { text = member.age .. "岁", fontSize = 10, fontColor = Theme.TEXT_MUTED },
                            member.militaryRank and UI.Panel {
                                paddingHorizontal = 4, paddingVertical = 1, borderRadius = 3,
                                backgroundColor = { 66, 133, 244, 25 },
                                children = { UI.Label { text = member.militaryRank, fontSize = 9, fontColor = Theme.BLUE } },
                            } or nil,
                        },
                    },
                    UI.Panel {
                        flexDirection = "row", gap = 8,
                        children = {
                            UI.Label { text = "武" .. member.martial, fontSize = 11, fontColor = Theme.RED },
                            UI.Label { text = "健" .. member.health, fontSize = 11, fontColor = Theme.GREEN },
                            member.talent and UI.Label { text = member.talent.name, fontSize = 10, fontColor = Theme.GOLD_DARK } or nil,
                        },
                    },
                },
            },
            -- 右侧：选中标记
            UI.Panel {
                width = 24, height = 24, borderRadius = 12,
                backgroundColor = isSelected and Theme.PRIMARY or Theme.BG_INPUT,
                borderWidth = 1, borderColor = isSelected and Theme.PRIMARY or Theme.BORDER,
                justifyContent = "center", alignItems = "center",
                children = {
                    isSelected and UI.Label { text = "V", fontSize = 12, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" } or nil,
                },
            },
        },
    }
end

local function CreateMemberSelectView()
    local deployable = RivalClans.GetDeployableMembers()
    local selectedCount = GetSelectedCount()
    local rival = selectedRival_
    local stage = selectedStage_

    local memberCards = {}
    for _, m in ipairs(deployable) do
        memberCards[#memberCards + 1] = CreateDeployMemberCard(m)
    end

    if #memberCards == 0 then
        memberCards[#memberCards + 1] = UI.Panel {
            width = "100%", padding = 20, alignItems = "center",
            children = {
                UI.Label { text = "没有可出战的族人", fontSize = 14, fontColor = Theme.TEXT_MUTED },
                UI.Label { text = "需要从军状态的男性族人、武艺>0、健康>30", fontSize = 10, fontColor = Theme.TEXT_MUTED, marginTop = 4 },
            },
        }
    end

    return UI.ScrollView {
        width = "100%", flexGrow = 1, flexBasis = 0,
        backgroundColor = { 0, 0, 0, 0 },
        children = {
            UI.Panel {
                width = "100%", gap = 8, padding = 12, paddingBottom = 80,
                children = {
                    -- 标题 + 返回
                    UI.Panel {
                        width = "100%", flexDirection = "row", alignItems = "center", gap = 8,
                        padding = { 8, 12, 8, 12 },
                        borderBottomWidth = 1, borderBottomColor = Theme.BORDER,
                        children = {
                            UI.Panel {
                                paddingHorizontal = 8, paddingVertical = 4, borderRadius = 4,
                                backgroundColor = Theme.BG_INPUT,
                                onTap = function()
                                    AudioManager.Click()
                                    phase_ = "stage_select"
                                    selectedStage_ = nil
                                    selectedRival_ = nil
                                    selectedMembers_ = {}
                                    BattlePrepPage.Refresh()
                                end,
                                children = { UI.Label { text = "< 返回", fontSize = 12, fontColor = Theme.TEXT_SECONDARY } },
                            },
                            UI.Panel {
                                gap = 1,
                                children = {
                                    UI.Label { text = "征伐 " .. (stage and stage.name or ""), fontSize = 16, fontColor = Theme.GOLD, fontWeight = "bold" },
                                    UI.Label { text = "选择出战将领（最多" .. MAX_DEPLOY .. "人）", fontSize = 10, fontColor = Theme.TEXT_MUTED },
                                },
                            },
                        },
                    },

                    -- 敌方简报
                    rival and UI.Panel {
                        width = "100%", padding = 8, borderRadius = 6,
                        backgroundColor = { 220, 80, 60, 15 },
                        borderWidth = 1, borderColor = { 220, 80, 60, 100 },
                        flexDirection = "row", justifyContent = "space-around",
                        children = {
                            UI.Panel { alignItems = "center", children = {
                                UI.Label { text = "敌军", fontSize = 9, fontColor = Theme.TEXT_MUTED },
                                UI.Label { text = rival.name, fontSize = 13, fontColor = Theme.RED, fontWeight = "bold" },
                            }},
                            UI.Panel { alignItems = "center", children = {
                                UI.Label { text = "敌将", fontSize = 9, fontColor = Theme.TEXT_MUTED },
                                UI.Label { text = #rival.members .. "人", fontSize = 13, fontColor = Theme.RED, fontWeight = "bold" },
                            }},
                            UI.Panel { alignItems = "center", children = {
                                UI.Label { text = "敌兵", fontSize = 9, fontColor = Theme.TEXT_MUTED },
                                UI.Label { text = FormatNumber(rival.soldiers), fontSize = 13, fontColor = Theme.RED, fontWeight = "bold" },
                            }},
                        },
                    } or nil,

                    -- 已选统计
                    UI.Panel {
                        width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                        children = {
                            UI.Label { text = "可出战将领", fontSize = 13, fontColor = Theme.TEXT_PRIMARY },
                            UI.Label { text = "已选 " .. selectedCount .. "/" .. MAX_DEPLOY, fontSize = 12, fontColor = Theme.PRIMARY, fontWeight = "bold" },
                        },
                    },

                    -- 族人卡片
                    table.unpack(memberCards),
                },
            },
        },
    }
end

-- ============================================================================
-- 阶段4：兵力分配
-- ============================================================================

local function CreateAdjustRow(label, value, maxValue, onDecrease, onIncrease)
    local ratio = maxValue > 0 and (value / maxValue) or 0

    return UI.Panel {
        width = "100%", gap = 4,
        children = {
            -- 标签行
            UI.Panel {
                flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                children = {
                    UI.Label { text = label, fontSize = 13, fontColor = Theme.TEXT_PRIMARY },
                    UI.Label { text = FormatNumber(value) .. " / " .. FormatNumber(maxValue), fontSize = 12, fontColor = Theme.TEXT_SECONDARY },
                },
            },
            -- 按钮 + 进度条
            UI.Panel {
                flexDirection = "row", gap = 6, alignItems = "center",
                children = {
                    -- 减
                    UI.Panel {
                        width = 36, height = 36, borderRadius = 6,
                        backgroundColor = value > 0 and { 220, 80, 60, 255 } or Theme.BG_INPUT,
                        justifyContent = "center", alignItems = "center",
                        onTap = function()
                            if value > 0 then
                                AudioManager.Click()
                                onDecrease()
                            end
                        end,
                        children = { UI.Label { text = "-", fontSize = 18, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" } },
                    },
                    -- 进度条
                    UI.Panel {
                        flexGrow = 1, flexBasis = 0, height = 20, borderRadius = 10,
                        backgroundColor = Theme.BG_INPUT, overflow = "hidden",
                        onTap = function()
                            AudioManager.Click()
                            local half = math.floor(maxValue / 200) * 100
                            if value ~= half then
                                onIncrease(half)
                            end
                        end,
                        children = {
                            UI.Panel {
                                width = math.max(0, math.floor(ratio * 100)) .. "%",
                                height = "100%", borderRadius = 10,
                                backgroundColor = Theme.PRIMARY,
                            },
                        },
                    },
                    -- 加
                    UI.Panel {
                        width = 36, height = 36, borderRadius = 6,
                        backgroundColor = value < maxValue and Theme.PRIMARY or Theme.BG_INPUT,
                        justifyContent = "center", alignItems = "center",
                        onTap = function()
                            if value < maxValue then
                                AudioManager.Click()
                                onIncrease()
                            end
                        end,
                        children = { UI.Label { text = "+", fontSize = 18, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" } },
                    },
                },
            },
            -- 快捷按钮
            UI.Panel {
                flexDirection = "row", gap = 4, justifyContent = "flex-end",
                children = {
                    UI.Panel {
                        paddingHorizontal = 8, paddingVertical = 3, borderRadius = 4,
                        backgroundColor = Theme.BG_INPUT,
                        onTap = function()
                            AudioManager.Click()
                            onIncrease(0)
                        end,
                        children = { UI.Label { text = "清零", fontSize = 10, fontColor = Theme.TEXT_MUTED } },
                    },
                    UI.Panel {
                        paddingHorizontal = 8, paddingVertical = 3, borderRadius = 4,
                        backgroundColor = Theme.BG_INPUT,
                        onTap = function()
                            AudioManager.Click()
                            local half = math.floor(maxValue / 200) * 100
                            onIncrease(half)
                        end,
                        children = { UI.Label { text = "半数", fontSize = 10, fontColor = Theme.TEXT_MUTED } },
                    },
                    UI.Panel {
                        paddingHorizontal = 8, paddingVertical = 3, borderRadius = 4,
                        backgroundColor = Theme.BG_INPUT,
                        onTap = function()
                            AudioManager.Click()
                            onIncrease(maxValue)
                        end,
                        children = { UI.Label { text = "全部", fontSize = 10, fontColor = Theme.TEXT_MUTED } },
                    },
                },
            },
        },
    }
end

local function CreateTroopAssignView()
    local rival = selectedRival_
    local stage = selectedStage_
    local selectedCount = GetSelectedCount()

    local maxInfantry = RivalClans.GetPlayerInfantry()
    local maxArchers = RivalClans.GetPlayerArchers()

    -- 确保分配值不超过当前拥有的
    deployInfantry_ = math.min(deployInfantry_, maxInfantry)
    deployArchers_ = math.min(deployArchers_, maxArchers)

    local totalDeploy = deployInfantry_ + deployArchers_
    local totalDeployUnits = math.floor(totalDeploy / 100)

    local enemyUnits = rival and math.floor(rival.soldiers / 100) or 0

    -- 训练等级
    local trainingLevel = RivalClans.GetTrainingLevel()
    local levelName = RivalClans.GetTrainingLevelName(trainingLevel)

    return UI.ScrollView {
        width = "100%", flexGrow = 1, flexBasis = 0,
        backgroundColor = { 0, 0, 0, 0 },
        children = {
            UI.Panel {
                width = "100%", gap = 12, padding = 12, paddingBottom = 80,
                children = {
                    -- 标题 + 返回
                    UI.Panel {
                        width = "100%", flexDirection = "row", alignItems = "center", gap = 8,
                        padding = { 8, 12, 8, 12 },
                        borderBottomWidth = 1, borderBottomColor = Theme.BORDER,
                        children = {
                            UI.Panel {
                                paddingHorizontal = 8, paddingVertical = 4, borderRadius = 4,
                                backgroundColor = Theme.BG_INPUT,
                                onTap = function()
                                    AudioManager.Click()
                                    phase_ = "member_select"
                                    BattlePrepPage.Refresh()
                                end,
                                children = { UI.Label { text = "< 返回", fontSize = 12, fontColor = Theme.TEXT_SECONDARY } },
                            },
                            UI.Panel {
                                gap = 1,
                                children = {
                                    UI.Label { text = "兵力部署", fontSize = 16, fontColor = Theme.GOLD, fontWeight = "bold" },
                                    UI.Label { text = "分配步兵和弓兵出战", fontSize = 10, fontColor = Theme.TEXT_MUTED },
                                },
                            },
                        },
                    },

                    -- 对阵概览
                    UI.Panel {
                        width = "100%", padding = 10, borderRadius = 8,
                        backgroundColor = { 60, 60, 80, 20 },
                        borderWidth = 1, borderColor = Theme.BORDER,
                        gap = 6,
                        children = {
                            UI.Label { text = "征伐 " .. (stage and stage.name or ""), fontSize = 14, fontColor = Theme.GOLD, fontWeight = "bold" },
                            UI.Panel {
                                flexDirection = "row", justifyContent = "space-around", flexWrap = "wrap", gap = 4,
                                children = {
                                    UI.Panel { alignItems = "center", children = {
                                        UI.Label { text = "我将", fontSize = 9, fontColor = Theme.TEXT_MUTED },
                                        UI.Label { text = selectedCount .. "人", fontSize = 14, fontColor = Theme.PRIMARY, fontWeight = "bold" },
                                    }},
                                    UI.Panel { alignItems = "center", children = {
                                        UI.Label { text = "出征兵力", fontSize = 9, fontColor = Theme.TEXT_MUTED },
                                        UI.Label { text = FormatNumber(totalDeploy) .. "(" .. totalDeployUnits .. "营)", fontSize = 14, fontColor = Theme.PRIMARY, fontWeight = "bold" },
                                    }},
                                    UI.Panel { alignItems = "center", children = {
                                        UI.Label { text = "训练", fontSize = 9, fontColor = Theme.TEXT_MUTED },
                                        UI.Label { text = levelName, fontSize = 14, fontColor = Theme.GOLD, fontWeight = "bold" },
                                    }},
                                    UI.Panel { alignItems = "center", children = {
                                        UI.Label { text = "寨防", fontSize = 9, fontColor = Theme.TEXT_MUTED },
                                        UI.Label {
                                            text = (GameData.state.fortCount or 0) > 0
                                                and ("+" .. math.min(GameData.state.fortCount, 5) * 2 .. "防")
                                                or "无",
                                            fontSize = 14,
                                            fontColor = (GameData.state.fortCount or 0) > 0 and Theme.GREEN or Theme.TEXT_MUTED,
                                            fontWeight = "bold",
                                        },
                                    }},
                                    UI.Panel { alignItems = "center", children = {
                                        UI.Label { text = "敌兵", fontSize = 9, fontColor = Theme.TEXT_MUTED },
                                        UI.Label { text = enemyUnits .. "营", fontSize = 14, fontColor = Theme.RED, fontWeight = "bold" },
                                    }},
                                },
                            },
                        },
                    },

                    -- 步兵分配
                    CreateAdjustRow(
                        "步兵（近战）", deployInfantry_, maxInfantry,
                        function()
                            deployInfantry_ = math.max(0, deployInfantry_ - 100)
                            BattlePrepPage.Refresh()
                        end,
                        function(setTo)
                            if setTo ~= nil then
                                deployInfantry_ = math.max(0, math.min(maxInfantry, math.floor(setTo / 100) * 100))
                            else
                                deployInfantry_ = math.min(maxInfantry, deployInfantry_ + 100)
                            end
                            BattlePrepPage.Refresh()
                        end
                    ),

                    -- 弓兵分配
                    CreateAdjustRow(
                        "弓兵（远程）", deployArchers_, maxArchers,
                        function()
                            deployArchers_ = math.max(0, deployArchers_ - 100)
                            BattlePrepPage.Refresh()
                        end,
                        function(setTo)
                            if setTo ~= nil then
                                deployArchers_ = math.max(0, math.min(maxArchers, math.floor(setTo / 100) * 100))
                            else
                                deployArchers_ = math.min(maxArchers, deployArchers_ + 100)
                            end
                            BattlePrepPage.Refresh()
                        end
                    ),

                    -- 战斗加持广告
                    (function()
                        local AdSystem = require("Systems.AdSystem")
                        local s = GameData.state
                        if s.adBattleBoost then
                            return UI.Panel {
                                width = "100%", padding = 8, borderRadius = 6,
                                backgroundColor = { 56, 168, 120, 20 },
                                borderWidth = 1, borderColor = Theme.PRIMARY,
                                flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 6,
                                children = {
                                    UI.Label { text = "战力加持已激活", fontSize = 12, fontColor = Theme.PRIMARY, fontWeight = "bold" },
                                    UI.Label { text = "从军族人属性+10%", fontSize = 10, fontColor = Theme.TEXT_SECONDARY },
                                },
                            }
                        elseif AdSystem.IsAvailable("battle_boost") then
                            local adRemain = AdSystem.GetRemaining("battle_boost")
                            return UI.Panel {
                                width = "100%", height = 38, borderRadius = 6,
                                backgroundGradient = { direction = "to-right", from = { 180, 130, 50, 255 }, to = { 160, 110, 30, 255 } },
                                flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 6,
                                onTap = function()
                                    AudioManager.Click()
                                    AdSystem.BattleBoost(function(success)
                                        if success then
                                            Toast.Show("战力加持已激活！从军族人本月属性+10%")
                                            BattlePrepPage.Refresh()
                                        end
                                    end)
                                end,
                                children = {
                                    UI.Label { text = "▶ 看广告·战力加持+10%", fontSize = 11, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                                    UI.Label { text = "(" .. adRemain .. "次)", fontSize = 9, fontColor = { 255, 255, 255, 160 } },
                                },
                            }
                        end
                        return nil
                    end)(),

                    -- 奖励预览
                    rival and UI.Panel {
                        width = "100%", padding = 8, borderRadius = 6,
                        backgroundColor = { 200, 160, 50, 15 },
                        borderWidth = 1, borderColor = { 200, 160, 50, 60 },
                        children = {
                            UI.Panel {
                                flexDirection = "row", gap = 10,
                                children = {
                                    UI.Label { text = "胜利奖励:", fontSize = 11, fontColor = Theme.TEXT_MUTED },
                                    UI.Label { text = "银" .. rival.rewards.silver, fontSize = 11, fontColor = Theme.SILVER_COLOR },
                                    UI.Label { text = "粮" .. rival.rewards.grain, fontSize = 11, fontColor = Theme.GRAIN_COLOR },
                                    UI.Label { text = "望" .. rival.rewards.fame, fontSize = 11, fontColor = Theme.FAME_COLOR },
                                },
                            },
                        },
                    } or nil,
                },
            },
        },
    }
end

-- ============================================================================
-- 底部操作按钮
-- ============================================================================

local function CreateBottomButton()
    -- 阶段3：下一步按钮
    if phase_ == "member_select" then
        local selectedCount = GetSelectedCount()
        local total = RivalClans.GetPlayerSoldierCount()
        local canProceed = selectedCount > 0 or total > 0

        return UI.Panel {
            position = "absolute", bottom = 0, left = 0, width = "100%",
            padding = 12, backgroundColor = { 255, 255, 255, 240 },
            borderTopWidth = 1, borderTopColor = Theme.BORDER,
            children = {
                UI.Panel {
                    width = "100%", height = 44, borderRadius = 8,
                    backgroundColor = canProceed and Theme.PRIMARY or Theme.BG_INPUT,
                    justifyContent = "center", alignItems = "center",
                    opacity = canProceed and 1.0 or 0.5,
                    onTap = function()
                        if not canProceed then
                            Toast.Show("至少选择1名将领或拥有兵力")
                            return
                        end
                        AudioManager.Select()
                        -- 默认带全部兵力
                        deployInfantry_ = RivalClans.GetPlayerInfantry()
                        deployArchers_ = RivalClans.GetPlayerArchers()
                        phase_ = "troop_assign"
                        BattlePrepPage.Refresh()
                    end,
                    children = {
                        UI.Label {
                            text = canProceed and "下一步 - 分配兵力" or "请选择出战人员",
                            fontSize = 15, fontColor = canProceed and Theme.TEXT_WHITE or Theme.TEXT_MUTED, fontWeight = "bold",
                        },
                    },
                },
            },
        }
    end

    -- 阶段4：出征按钮
    if phase_ == "troop_assign" then
        local totalDeploy = deployInfantry_ + deployArchers_
        local selectedCount = GetSelectedCount()
        local canFight = selectedCount > 0 or totalDeploy > 0

        return UI.Panel {
            position = "absolute", bottom = 0, left = 0, width = "100%",
            padding = 12, backgroundColor = { 255, 255, 255, 240 },
            borderTopWidth = 1, borderTopColor = Theme.BORDER,
            children = {
                UI.Panel {
                    width = "100%", height = 44, borderRadius = 8,
                    backgroundGradient = canFight and Theme.GRADIENT_RED or nil,
                    backgroundColor = canFight and nil or Theme.BG_INPUT,
                    justifyContent = "center", alignItems = "center",
                    opacity = canFight and 1.0 or 0.5,
                    onTap = function()
                        if not canFight then
                            Toast.Show("至少选择1名将领或分配兵力才能出战")
                            return
                        end
                        AudioManager.Select()

                        -- 收集出战数据
                        local deployedMemberIds = {}
                        for id in pairs(selectedMembers_) do
                            deployedMemberIds[#deployedMemberIds + 1] = id
                        end

                        -- 设置出战族人状态为"出征"
                        for _, id in ipairs(deployedMemberIds) do
                            local m = GameData.GetMember(id)
                            if m then m.state = "出征" end
                        end

                        -- 通知 GameScreen 进入战斗
                        if gameScreen_ and gameScreen_.EnterBattle then
                            local archerRatio = totalDeploy > 0 and (deployArchers_ / totalDeploy) or 0
                            gameScreen_.EnterBattle(selectedRival_, deployedMemberIds, {
                                soldierCount = totalDeploy,
                                archerRatio = archerRatio,
                                infantry = deployInfantry_,
                                archers = deployArchers_,
                                trainingLevel = RivalClans.GetTrainingLevel(),
                                stageId = selectedStage_ and selectedStage_.id or nil,
                                regionId = selectedArea_ and selectedArea_.id or nil,
                            })
                        end
                    end,
                    children = {
                        UI.Label {
                            text = canFight
                                and ("出征讨伐 " .. (selectedRival_ and selectedRival_.name or "") .. "（" .. FormatNumber(totalDeploy) .. "兵）")
                                or "请分配兵力",
                            fontSize = 14, fontColor = canFight and Theme.TEXT_WHITE or Theme.TEXT_MUTED, fontWeight = "bold",
                        },
                    },
                },
            },
        }
    end

    return nil
end

-- ============================================================================
-- 公共接口
-- ============================================================================

--- 创建讨伐准备页面
---@param pageTitle function 页面标题组件（兼容 GameScreen 风格）
---@param gs table GameScreen 引用
function BattlePrepPage.Create(pageTitle, gs)
    gameScreen_ = gs
    if not skipReset_ then
        phase_ = "area_select"
        selectedArea_ = nil
        selectedStage_ = nil
        selectedRival_ = nil
        selectedMembers_ = {}
        deployInfantry_ = 0
        deployArchers_ = 0
    end
    skipReset_ = false

    return BattlePrepPage.BuildView()
end

--- 构建当前视图
function BattlePrepPage.BuildView()
    local content
    if phase_ == "area_select" then
        content = CreateAreaSelectView()
    elseif phase_ == "stage_select" then
        content = CreateStageSelectView()
    elseif phase_ == "member_select" then
        content = CreateMemberSelectView()
    else
        content = CreateTroopAssignView()
    end

    return UI.Panel {
        id = "battlePrepRoot",
        width = "100%", flexGrow = 1, flexBasis = 0,
        position = "relative",
        children = {
            content,
            CreateBottomButton(),
        },
    }
end

--- 刷新页面
function BattlePrepPage.Refresh()
    if not gameScreen_ then return end
    skipReset_ = true  -- 内部刷新时保留当前状态（phase/选区/选人等）
    if gameScreen_.RefreshContent then
        gameScreen_.RefreshContent()
    end
end

--- 重置状态
function BattlePrepPage.Reset()
    selectedArea_ = nil
    selectedStage_ = nil
    selectedRival_ = nil
    selectedMembers_ = {}
    phase_ = "area_select"
    deployInfantry_ = 0
    deployArchers_ = 0
end

return BattlePrepPage
