-- ============================================================================
-- 大明浮生志2 - 模态对话框集合
-- 从 GameScreen.lua 提取，包含所有弹窗 UI
-- 使用注入模式：Init(GameScreen) 将 Show* 方法挂载到 GameScreen 上
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")
local AudioManager = require("Systems.AudioManager")
local EquipmentSystem = require("Systems.EquipmentSystem")
local SkillSystem = require("Systems.SkillSystem")
local GrowthSystem = require("Systems.GrowthSystem")
local AdSystem = require("Systems.AdSystem")
local AvatarSystem = require("UI.AvatarSystem")
local MemberData = require("Data.MemberData")
local Toast = require("Systems.Toast")

local ModalDialogs = {}

-- ============================================================================
-- 通用属性条组件（供多个弹窗复用）
-- ============================================================================

-- 创建左侧关闭按钮的标题行（替代 Modal 内置的右侧关闭按钮）
local function CreateLeftCloseHeader(modal, title)
    return UI.Panel {
        width = "100%",
        flexDirection = "row",
        alignItems = "center",
        paddingBottom = 8,
        marginBottom = 4,
        borderBottomWidth = 1,
        borderColor = Theme.BORDER,
        children = {
            -- 左侧关闭按钮
            UI.Panel {
                width = 30, height = 30,
                borderRadius = 15,
                justifyContent = "center",
                alignItems = "center",
                backgroundColor = { 0, 0, 0, 0 },
                onClick = function(self)
                    AudioManager.Click()
                    modal:Close()
                end,
                children = {
                    UI.Label { text = "X", fontSize = 16, fontColor = Theme.TEXT_SECONDARY, fontWeight = "bold" },
                },
            },
            -- 标题居中
            UI.Label {
                text = title,
                fontSize = 16,
                fontColor = Theme.TEXT_PRIMARY,
                fontWeight = "bold",
                flexGrow = 1,
                textAlign = "center",
            },
            -- 右侧占位（保持标题居中）
            UI.Panel { width = 30, height = 30 },
        },
    }
end

local function statColor(val)
    if val >= 60 then return Theme.GREEN
    elseif val >= 30 then return Theme.GOLD
    else return Theme.TEXT_MUTED end
end

local function StatBar(label, val, maxVal, aptitudeInfo)
    maxVal = maxVal or 100
    local pct = math.min(1.0, val / maxVal)
    local barChildren = {
        UI.Label { text = label, fontSize = 12, fontColor = Theme.TEXT_SECONDARY, width = 28 },
        UI.Panel {
            flex = 1, height = 6, borderRadius = 3, backgroundColor = { 230, 225, 218, 180 },
            children = {
                UI.Panel {
                    width = tostring(math.floor(pct * 100)) .. "%", height = "100%",
                    borderRadius = 4, backgroundColor = statColor(val),
                },
            },
        },
        UI.Label { text = tostring(val), fontSize = 12, fontColor = statColor(val), width = 22, textAlign = "right" },
    }
    -- 资质星级显示（紧跟数值后面）
    if aptitudeInfo then
        local starsColor = MemberData.GetStarsColor(aptitudeInfo.stars)
        barChildren[#barChildren + 1] = UI.Label {
            text = MemberData.GetStarsLabel(aptitudeInfo.stars),
            fontSize = 10, fontColor = starsColor, width = 38, textAlign = "right",
        }
    end
    return UI.Panel {
        flexDirection = "row", alignItems = "center", gap = 4, width = "100%",
        children = barChildren,
    }
end

--- 初始化：将所有弹窗方法挂载到 GameScreen
--- @param GameScreen table 游戏主界面模块
function ModalDialogs.Init(GameScreen)

-- ============================================================================
-- 弹窗：族人详情
-- ============================================================================

function GameScreen.ShowMemberDetail(member)
    local isDead = not member.alive

    local modal = UI.Modal {
        title = member.name .. "（" .. member.identity .. "）",
        size = "fullscreen", showCloseButton = true, closeOnOverlay = true,
        contentBgColor = isDead and { 235, 230, 225, 255 } or { 245, 235, 218, 255 },
        headerBgColor = isDead and { 220, 215, 210, 255 } or { 238, 228, 208, 255 },
    }
    local spouse = member.spouseId and GameData.GetMember(member.spouseId) or nil

    -- 死因映射
    local deathCauseMap = {
        ["亡故"]  = { icon = "殁", desc = "寿终正寝", detail = "因年迈体衰或饥病交加而离世" },
        ["夭折"]  = { icon = "夭", desc = "幼年夭折", detail = "年幼体弱，不幸早逝" },
        ["病逝"]  = { icon = "疾", desc = "久病不愈", detail = "身患重疾，药石无灵" },
        ["阵亡"]  = { icon = "戎", desc = "马革裹尸", detail = "在战场上英勇牺牲" },
        ["离族"]  = { icon = "去", desc = "离族出走", detail = "离开家族，不知所踪" },
    }

    local detailChildren = {}

    if isDead then
        -- 已故成员：显示死因卡片
        local causeInfo = deathCauseMap[member.state] or { icon = "亡", desc = member.state, detail = "已离世" }
        detailChildren[#detailChildren + 1] = UI.Panel {
            width = "100%", padding = 10, borderRadius = 8, marginBottom = 4,
            backgroundColor = { 60, 55, 50, 30 }, borderWidth = 1, borderColor = { 150, 140, 130, 80 },
            alignItems = "center", gap = 4,
            children = {
                UI.Label { text = causeInfo.icon, fontSize = 30 },
                UI.Label { text = causeInfo.desc, fontSize = 17, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                UI.Label { text = causeInfo.detail, fontSize = 13, fontColor = Theme.TEXT_MUTED },
                UI.Panel { width = "60%", height = 1, backgroundColor = { 150, 140, 130, 60 }, marginVertical = 2 },
                UI.Label {
                    text = (member.gender == "male" and "男" or "女") .. " · 享年" .. member.age .. "岁",
                    fontSize = 14, fontColor = Theme.TEXT_SECONDARY,
                },
            },
        }
        -- 生前属性
        local apt = member.aptitude
        detailChildren[#detailChildren + 1] = UI.Label { text = "— 生平 —", fontSize = 13, fontColor = Theme.TEXT_MUTED, textAlign = "center", width = "100%", marginTop = 2 }
        detailChildren[#detailChildren + 1] = UI.Panel {
            width = "100%", gap = 3,
            children = {
                StatBar("学识", member.study, 100, apt and apt.study),
                StatBar("武艺", member.martial, 100, apt and apt.martial),
            },
        }
    else
        -- 存活成员：原有信息
        local apt = member.aptitude
        detailChildren[#detailChildren + 1] = UI.Label { text = (member.gender == "male" and "男" or "女") .. " · " .. member.age .. "岁 · " .. GameData.GetAgeStage(member.age), fontSize = 15, fontColor = Theme.TEXT_PRIMARY }
        detailChildren[#detailChildren + 1] = UI.Label { text = "状态：" .. member.state, fontSize = 14, fontColor = Theme.TEXT_SECONDARY }
        detailChildren[#detailChildren + 1] = UI.Panel {
            width = "100%", gap = 3,
            children = {
                StatBar("健康", member.health, 100, apt and apt.health),
                StatBar("学识", member.study, 100, apt and apt.study),
                StatBar("武艺", member.martial, 100, apt and apt.martial),
            },
        }

        -- 神医诊治按钮（生病或健康<=40时显示）
        if (member.state == "生病" or member.health <= 40) and AdSystem.IsAvailable("health_cure") then
            local cureRemain = AdSystem.GetRemaining("health_cure")
            detailChildren[#detailChildren + 1] = UI.Panel {
                width = "100%", height = 32, borderRadius = 6, marginTop = 4,
                backgroundGradient = { direction = "to-right", from = { 60, 160, 120, 255 }, to = { 40, 140, 100, 255 } },
                flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 4,
                onTap = function(self)
                    AdSystem.HealthCure(member.id, function(success, msg)
                        modal:Close()
                        GameScreen.ShowResultPopup(success and "妙手回春" or "诊治失败", msg or "")
                        GameScreen.RefreshAll()
                    end)
                end,
                children = {
                    UI.Label { text = "▶ 看广告·神医诊治", fontSize = 13, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                    UI.Label { text = "(" .. cureRemain .. "次)", fontSize = 11, fontColor = { 255, 255, 255, 160 } },
                },
            }
        end
    end

    if member.talent then
        detailChildren[#detailChildren + 1] = UI.Label { text = "天赋：" .. member.talent.name .. "（" .. member.talent.effect .. "）", fontSize = 14, fontColor = Theme.GOLD_DARK }
    end

    -- 身份效果说明（功名/军衔带来的月收益）
    local IDENTITY_EFFECTS = {
        ["秀才"]  = { color = { 80, 100, 180, 255 }, desc = "功名在身，每月+2声望", icon = "文" },
        ["举人"]  = { color = { 80, 100, 180, 255 }, desc = "举人功名，每月+5声望 +2银两", icon = "文" },
        ["进士"]  = { color = { 80, 100, 180, 255 }, desc = "进士及第，每月+10声望 +5银两", icon = "文" },
        ["监生"]  = { color = { 80, 100, 180, 255 }, desc = "监生资格，每月+2声望", icon = "文" },
        ["士兵"]  = { color = { 180, 60, 60, 255 }, desc = "军中效力，每月领取军饷", icon = "武" },
        ["把总"]  = { color = { 180, 60, 60, 255 }, desc = "把总军衔，每月+8银两军饷", icon = "武" },
        ["守备"]  = { color = { 180, 60, 60, 255 }, desc = "守备军衔，每月+15银两军饷", icon = "武" },
    }
    local idEffect = IDENTITY_EFFECTS[member.identity]
    if idEffect then
        detailChildren[#detailChildren + 1] = UI.Panel {
            width = "100%", flexDirection = "row", alignItems = "center", gap = 6,
            paddingVertical = 4, paddingHorizontal = 8, borderRadius = 6, marginTop = 2,
            backgroundColor = { idEffect.color[1], idEffect.color[2], idEffect.color[3], 20 },
            borderWidth = 1, borderColor = { idEffect.color[1], idEffect.color[2], idEffect.color[3], 60 },
            children = {
                UI.Label { text = idEffect.icon, fontSize = 16, fontColor = idEffect.color, fontWeight = "bold" },
                UI.Panel {
                    flexShrink = 1, gap = 1,
                    children = {
                        UI.Label { text = member.identity, fontSize = 14, fontColor = idEffect.color, fontWeight = "bold" },
                        UI.Label { text = idEffect.desc, fontSize = 12, fontColor = Theme.TEXT_MUTED },
                    },
                },
            },
        }
    end

    if spouse then
        detailChildren[#detailChildren + 1] = UI.Label { text = "配偶：" .. spouse.name .. (spouse.alive and "" or "（已故）"), fontSize = 14, fontColor = Theme.TEXT_SECONDARY }
    end

    -- ===== 培养系统区域 =====
    local s = GameData.state
    if not isDead and s.clanRank >= 2 then
        if not s.inventory then s.inventory = {} end

        detailChildren[#detailChildren + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 6 }

        -- === 培养训练 (品级>=2) ===
        detailChildren[#detailChildren + 1] = UI.Label { text = "— 培养 —", fontSize = 14, fontColor = Theme.GOLD, textAlign = "center", width = "100%", marginTop = 2 }
        local trainBtns = {}
        for _, t in ipairs(GameData.TRAINING_OPTIONS) do
            local costText = ""
            if (t.cost.silver or 0) > 0 then costText = costText .. "银" .. t.cost.silver end
            if (t.cost.grain or 0) > 0 then costText = costText .. (costText ~= "" and " " or "") .. "粮" .. t.cost.grain end
            local canAfford = GameData.CanAfford(t.cost.silver or 0, t.cost.grain or 0, 0, 0)
            trainBtns[#trainBtns + 1] = UI.Panel {
                width = "31%", padding = 6, borderRadius = 6,
                backgroundColor = canAfford and Theme.BG_WHITE or Theme.BG_INPUT,
                borderWidth = 1, borderColor = canAfford and Theme.BORDER_GOLD or Theme.BORDER,
                alignItems = "center", gap = 2, opacity = canAfford and 1.0 or 0.5,
                onTap = function()
                    if not canAfford then return end
                    AudioManager.Click()
                    local ok, msg = SkillSystem.Train(member.id, t.id)
                    modal:Close()
                    GameScreen.ShowResultPopup(ok and "培养成功" or "培养失败", msg)
                    GameScreen.RefreshAll()
                end,
                children = {
                    UI.Label { text = t.icon, fontSize = 18, fontColor = canAfford and Theme.PRIMARY or Theme.TEXT_MUTED },
                    UI.Label { text = t.name, fontSize = 12, fontColor = Theme.TEXT_PRIMARY },
                    UI.Label { text = costText, fontSize = 11, fontColor = Theme.TEXT_MUTED },
                },
            }
        end
        detailChildren[#detailChildren + 1] = UI.Panel {
            width = "100%", flexDirection = "row", justifyContent = "space-between", gap = 4,
            children = trainBtns,
        }
        -- 看广告免费培养按钮
        if AdSystem.IsAvailable("free_training") then
            local adRemain = AdSystem.GetRemaining("free_training")
            detailChildren[#detailChildren + 1] = UI.Panel {
                width = "100%", height = 30, borderRadius = 6,
                backgroundGradient = { direction = "to-right", from = { 218, 165, 32, 255 }, to = { 200, 140, 20, 255 } },
                flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 4,
                onTap = function(self)
                    -- 默认选第一个训练项
                    local defaultTraining = GameData.TRAINING_OPTIONS[1]
                    if not defaultTraining then return end
                    AdSystem.FreeTraining(member.id, defaultTraining.id, function(success, msg)
                        modal:Close()
                        GameScreen.ShowResultPopup(success and "免费培养" or "培养失败", msg or "")
                        GameScreen.RefreshAll()
                    end)
                end,
                children = {
                    UI.Label { text = "看广告免费培养", fontSize = 13, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                    UI.Label { text = "(" .. adRemain .. "次)", fontSize = 11, fontColor = { 255, 255, 255, 160 } },
                },
            }
        end

        -- === 装备系统 (品级>=3) ===
        if s.clanRank >= 3 then
            detailChildren[#detailChildren + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 4 }
            detailChildren[#detailChildren + 1] = UI.Panel {
                width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                children = {
                    UI.Label { text = "— 装备 —", fontSize = 14, fontColor = Theme.GOLD },
                    UI.Panel {
                        paddingHorizontal = 8, paddingVertical = 3, borderRadius = 4,
                        backgroundColor = Theme.PRIMARY_LIGHT, borderWidth = 1, borderColor = Theme.PRIMARY,
                        onTap = function(self)
                            AudioManager.Click()
                            modal:Close()
                            GameScreen.ShowEquipShop(member)
                        end,
                        children = { UI.Label { text = "购买装备", fontSize = 12, fontColor = Theme.PRIMARY_DARK } },
                    },
                },
            }
            local slotCards = {}
            for _, slot in ipairs(GameData.EQUIPMENT_SLOTS) do
                local slotName = GameData.SLOT_NAMES and GameData.SLOT_NAMES[slot] or slot
                local equipped = EquipmentSystem.GetEquipped(member, slot)
                local rarityConf = equipped and GameData.GetRarityConfig(equipped.rarity) or nil
                local nameColor = rarityConf and rarityConf.color or Theme.TEXT_MUTED
                slotCards[#slotCards + 1] = UI.Panel {
                    width = "31%", padding = 6, borderRadius = 6,
                    backgroundColor = equipped and Theme.BG_WHITE or Theme.BG_INPUT,
                    borderWidth = 1, borderColor = equipped and (rarityConf and rarityConf.color or Theme.BORDER_GOLD) or Theme.BORDER,
                    alignItems = "center", gap = 2,
                    onTap = function()
                        AudioManager.Click()
                        if equipped then
                            -- 卸下装备
                            GameScreen.ShowConfirm("卸下装备", "是否卸下【" .. equipped.name .. "】？\n卸下后将放回库房。", "卸下", function()
                                EquipmentSystem.Unequip(member.id, slot)
                                modal:Close()
                                GameScreen.ShowMemberDetail(member)
                            end)
                        else
                            -- 选择装备
                            modal:Close()
                            GameScreen.ShowEquipSelect(member, slot)
                        end
                    end,
                    children = {
                        UI.Label { text = slotName, fontSize = 11, fontColor = Theme.TEXT_MUTED },
                        UI.Label {
                            text = equipped and equipped.name or "空",
                            fontSize = 13, fontColor = nameColor,
                        },
                        equipped and UI.Label {
                            text = (equipped.martial > 0 and ("武+" .. equipped.martial) or "") ..
                                   (equipped.study > 0 and (" 文+" .. equipped.study) or "") ..
                                   (equipped.health > 0 and (" 体+" .. equipped.health) or ""),
                            fontSize = 10, fontColor = Theme.TEXT_MUTED,
                        } or UI.Label { text = "点击装备", fontSize = 10, fontColor = Theme.TEXT_MUTED },
                    },
                }
            end
            detailChildren[#detailChildren + 1] = UI.Panel {
                width = "100%", flexDirection = "row", justifyContent = "space-between", gap = 4,
                children = slotCards,
            }
            -- 装备加成汇总
            local bonus = EquipmentSystem.GetBonus(member)
            if bonus.martial > 0 or bonus.study > 0 or bonus.health > 0 then
                local bonusText = "装备加成："
                if bonus.martial > 0 then bonusText = bonusText .. "武+" .. bonus.martial .. " " end
                if bonus.study > 0 then bonusText = bonusText .. "文+" .. bonus.study .. " " end
                if bonus.health > 0 then bonusText = bonusText .. "体+" .. bonus.health end
                detailChildren[#detailChildren + 1] = UI.Label { text = bonusText, fontSize = 12, fontColor = Theme.GREEN, textAlign = "center", width = "100%" }
            end

            -- === 技能专精 (品级>=3, 年龄>=18) ===
            if member.age >= 18 then
                detailChildren[#detailChildren + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 4 }
                detailChildren[#detailChildren + 1] = UI.Label { text = "— 专精 —", fontSize = 14, fontColor = Theme.GOLD, textAlign = "center", width = "100%", marginTop = 2 }
                local activePath = GameData.GetActiveSkillPath(member)
                if activePath then
                    -- 已有专精：显示路径和效果
                    detailChildren[#detailChildren + 1] = UI.Panel {
                        width = "100%", padding = 8, borderRadius = 6,
                        backgroundGradient = { direction = "to-bottom", from = { 200, 165, 60, 30 }, to = { 200, 165, 60, 10 } },
                        borderWidth = 1, borderColor = Theme.GOLD_DARK, alignItems = "center", gap = 3,
                        children = {
                            UI.Label { text = activePath.icon .. " " .. activePath.name, fontSize = 16, fontColor = Theme.GOLD, fontWeight = "bold" },
                            UI.Label { text = activePath.desc, fontSize = 12, fontColor = Theme.TEXT_SECONDARY, whiteSpace = "normal", textAlign = "center" },
                        },
                    }
                else
                    -- 未选择专精：显示可选路径
                    local pathBtns = {}
                    for _, path in ipairs(GameData.SKILL_PATHS) do
                        local canUnlock, reason = GameData.CanUnlockSkillPath(member, path.id)
                        pathBtns[#pathBtns + 1] = UI.Panel {
                            width = "31%", padding = 6, borderRadius = 6,
                            backgroundColor = canUnlock and Theme.BG_WHITE or Theme.BG_INPUT,
                            borderWidth = 1, borderColor = canUnlock and Theme.BORDER_GOLD or Theme.BORDER,
                            alignItems = "center", gap = 2, opacity = canUnlock and 1.0 or 0.5,
                            onTap = function()
                                AudioManager.Click()
                                if not canUnlock then
                                    GameScreen.ShowResultPopup("条件不足", reason or "无法解锁")
                                    return
                                end
                                GameScreen.ShowConfirm("选择专精", "确定专精为【" .. path.name .. "】？\n" .. path.desc .. "\n\n专精一旦选择不可更改！", "确认专精", function()
                                    local ok, msg = SkillSystem.UnlockPath(member.id, path.id)
                                    modal:Close()
                                    GameScreen.ShowResultPopup(ok and "专精成功" or "专精失败", msg)
                                    GameScreen.RefreshAll()
                                end)
                            end,
                            children = {
                                UI.Label { text = path.icon, fontSize = 18, fontColor = canUnlock and Theme.GOLD or Theme.TEXT_MUTED },
                                UI.Label { text = path.name, fontSize = 13, fontColor = Theme.TEXT_PRIMARY },
                                UI.Label { text = path.reqAttr .. "≥" .. path.reqValue, fontSize = 10, fontColor = canUnlock and Theme.GREEN or Theme.RED },
                            },
                        }
                    end
                    detailChildren[#detailChildren + 1] = UI.Panel {
                        width = "100%", flexDirection = "row", justifyContent = "space-between", gap = 4,
                        children = pathBtns,
                    }
                end
            end
        end
    end

    -- 操作按钮（仅存活成员显示）
    -- onClick 代替 onClick：手机端触摸兼容性更好
    local actionDone = false
    if isDead then
        -- 已故成员：广告复活按钮
        if AdSystem.IsAvailable("revive_member") then
            detailChildren[#detailChildren + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 8 }
            local reviveRemain = AdSystem.GetRemaining("revive_member")
            detailChildren[#detailChildren + 1] = UI.Panel {
                width = "100%", height = 36, borderRadius = 8, marginTop = 4,
                backgroundGradient = { direction = "horizontal", from = {218, 165, 32, 255}, to = {255, 200, 50, 255} },
                justifyContent = "center", alignItems = "center",
                flexDirection = "row", gap = 6,
                onTap = function(self, event)
                    if actionDone then return end
                    AudioManager.Click()
                    actionDone = true
                    AdSystem.ReviveMember(member.id, function(success, msg)
                        actionDone = false
                        if success then
                            modal:Close()
                            GameScreen.ShowResultPopup("起死回生", member.name .. "奇迹般苏醒，体力恢复部分。")
                            GameScreen.RefreshAll()
                        else
                            GameScreen.ShowResultPopup("复活失败", msg or "广告播放失败")
                        end
                    end)
                end,
                children = {
                    UI.Label { text = "▶ 看广告复活族人", fontSize = 15, fontColor = {80, 40, 0, 255} },
                    UI.Label { text = "(今日" .. reviveRemain .. "次)", fontSize = 12, fontColor = {120, 80, 20, 255} },
                },
            }
        end
    elseif member.state == "在家" and member.age >= 15 then
        detailChildren[#detailChildren + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 8 }
        detailChildren[#detailChildren + 1] = UI.Label { text = "指派工作", fontSize = 15, fontColor = Theme.GOLD, marginTop = 2 }
        if member.age >= 6 then
            detailChildren[#detailChildren + 1] = UI.Panel {
                width = 180, height = 75, alignSelf = "center", borderRadius = 6, overflow = "hidden",
                backgroundImage = Theme.IMG.BTN_STUDY, backgroundFit = "cover",
                onTap = function(self, event)
                    if actionDone then return end
                    AudioManager.Click()
                    actionDone = true
                    member.state = "读书"; GameData.AddLog(member.name .. "开始在族学读书。"); modal:Close(); GameScreen.RefreshAll()
                end,
            }
        end
        -- 经商需农户(品级2)
        local tradeUnlocked = GameData.IsTabUnlocked("industry") and (GameData.state.clanRank >= 2)
        detailChildren[#detailChildren + 1] = UI.Panel {
            width = 180, height = 75, alignSelf = "center", borderRadius = 6, overflow = "hidden",
            backgroundImage = Theme.IMG.BTN_TRADE, backgroundFit = "cover",
            opacity = tradeUnlocked and 1.0 or 0.4,
            onTap = function(self, event)
                if actionDone then return end
                AudioManager.Click()
                if not tradeUnlocked then
                    GameScreen.ShowResultPopup("功能未解锁", "经商需要品级【农户】以上。")
                    return
                end
                actionDone = true
                member.state = "经商"; GameData.AddLog(member.name .. "开始经商。"); modal:Close(); GameScreen.RefreshAll()
            end,
        }
        -- 从武：仅男性可用，需品级农户(2)以上
        if member.gender == "male" then
            local militaryUnlocked = (GameData.state.clanRank >= 2)
            detailChildren[#detailChildren + 1] = UI.Panel {
                width = 180, height = 75, alignSelf = "center", borderRadius = 6, overflow = "hidden",
                backgroundImage = Theme.IMG.BTN_MILITARY, backgroundFit = "cover",
                opacity = militaryUnlocked and 1.0 or 0.4,
                onTap = function(self, event)
                    if actionDone then return end
                    AudioManager.Click()
                    if not militaryUnlocked then
                        GameScreen.ShowResultPopup("功能未解锁", "习武需要品级【农户】以上。")
                        return
                    end
                    actionDone = true
                    member.state = "习武"; GameData.AddLog(member.name .. "开始习武练功。"); modal:Close(); GameScreen.RefreshAll()
                end,
            }
        end
    elseif member.state ~= "在家" and member.state ~= "从军" and member.state ~= "出征" and member.state ~= "阵亡" then
        detailChildren[#detailChildren + 1] = UI.Panel {
            width = 180, height = 75, alignSelf = "center", borderRadius = 6, overflow = "hidden", marginTop = 4,
            backgroundImage = Theme.IMG.BTN_RECALL, backgroundFit = "cover",
            onTap = function(self, event)
                if actionDone then return end
                AudioManager.Click()
                actionDone = true
                member.laborJob = nil  -- 清除打工工种
                member.state = "在家"; modal:Close(); GameScreen.RefreshAll()
            end,
        }
    end

    modal:AddContent(UI.ScrollView {
        width = "100%", maxHeight = 500, scrollY = true, bounces = true, showScrollbar = true,
        children = { UI.Panel { width = "100%", gap = 4, paddingBottom = 16, children = detailChildren } },
    })
    modal:Open()
end

-- ============================================================================
-- 弹窗：分配族人管理产业
-- ============================================================================

function GameScreen.ShowAssignMember(industry)
    local candidates = GameData.GetAssignableMembers()
    -- 过滤掉已分配到其他产业的族人（每人只能管理一个产业）
    local filtered = {}
    local assignedIds = {}
    for _, ind in ipairs(GameData.state.industries or {}) do
        if ind.assignedMemberId and ind ~= industry then
            assignedIds[ind.assignedMemberId] = true
        end
    end
    for _, m in ipairs(candidates) do
        if not assignedIds[m.id] then
            filtered[#filtered + 1] = m
        end
    end
    candidates = filtered
    local modal = UI.Modal { title = "分配族人管理", size = "md", showCloseButton = true, closeOnOverlay = true }
    local items = {}
    items[#items + 1] = UI.Panel {
        width = "100%", height = 36, borderRadius = 6, backgroundColor = Theme.BG_INPUT, borderWidth = 1, borderColor = Theme.BORDER,
        justifyContent = "center", alignItems = "center",
        onTap = function()
            AudioManager.Click()
            industry.assignedMemberId = nil; modal:Close(); GameScreen.RefreshAll()
        end,
        children = { UI.Label { text = "无人管理", fontSize = 14, fontColor = Theme.TEXT_MUTED } },
    }
    -- 状态标签颜色
    local stateColors = {
        ["在家"] = Theme.TEXT_MUTED,
        ["读书"] = { 100, 180, 120, 255 },
        ["从军"] = Theme.BLUE,
        ["经商"] = Theme.GOLD_DARK,
    }
    for _, m in ipairs(candidates) do
        local stateLabel = m.state ~= "在家" and m.state or ""
        items[#items + 1] = UI.Panel {
            width = "100%", height = 36, borderRadius = 6, backgroundColor = Theme.BG_INPUT, borderWidth = 1, borderColor = Theme.BORDER,
            flexDirection = "row", justifyContent = "space-between", alignItems = "center", paddingHorizontal = 10,
            onTap = function()
                AudioManager.Click()
                industry.assignedMemberId = m.id; modal:Close(); GameScreen.RefreshAll()
            end,
            children = {
                UI.Panel {
                    flexDirection = "row", alignItems = "center", gap = 4,
                    children = {
                        UI.Label { text = m.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY },
                        stateLabel ~= "" and UI.Label { text = stateLabel, fontSize = 11, fontColor = stateColors[m.state] or Theme.TEXT_MUTED } or nil,
                    },
                },
                UI.Label { text = m.age .. "岁", fontSize = 12, fontColor = Theme.TEXT_MUTED },
            },
        }
    end
    if #candidates == 0 then
        items[#items + 1] = UI.Label { text = "无可分配族人", fontSize = 14, fontColor = Theme.TEXT_MUTED, textAlign = "center", marginTop = 8 }
    end
    modal:AddContent(UI.ScrollView {
        width = "100%", maxHeight = 400, scrollY = true, bounces = true, showScrollbar = true,
        children = { UI.Panel { width = "100%", gap = 6, children = items } },
    })
    modal:Open()
end

-- ============================================================================
-- 弹窗：结果提示
-- ============================================================================

function GameScreen.ShowResultPopup(title, desc)
    local modal = UI.Modal { title = title, size = "sm", showCloseButton = true, closeOnOverlay = true }
    modal:AddContent(UI.Panel {
        width = "100%", alignItems = "center", gap = 12, padding = 8,
        children = { UI.Label { text = desc, fontSize = 16, fontColor = Theme.TEXT_PRIMARY, textAlign = "center", whiteSpace = "normal" } },
    })
    modal:SetFooter(UI.Panel {
        flexDirection = "row", justifyContent = "center",
        children = {
            UI.Panel {
                width = 100, height = 36, borderRadius = 6,
                backgroundGradient = Theme.GRADIENT_PRIMARY,
                justifyContent = "center", alignItems = "center",
                onClick = function(self) AudioManager.Click() modal:Close() end,
                children = { UI.Label { text = "知道了", fontSize = 15, fontColor = Theme.TEXT_WHITE } },
            },
        },
    })
    modal:Open()
end

--- 二次确认弹窗（高代价操作）
--- @param title string 标题
--- @param desc string 描述/警告文字
--- @param confirmText string 确认按钮文字（如"确认花费"）
--- @param onConfirm function 点击确认后的回调
--- @param onCancel function|nil 取消回调（可选）
function GameScreen.ShowConfirm(title, desc, confirmText, onConfirm, onCancel)
    local modal = UI.Modal { title = title, size = "sm", showCloseButton = true, closeOnOverlay = true }
    modal:AddContent(UI.Panel {
        width = "100%", alignItems = "center", gap = 8, padding = 8,
        children = {
            UI.Label { text = "警", fontSize = 30, fontColor = Theme.GOLD },
            UI.Label { text = desc, fontSize = 15, fontColor = Theme.TEXT_PRIMARY, textAlign = "center", whiteSpace = "normal" },
        },
    })
    modal:SetFooter(UI.Panel {
        flexDirection = "row", justifyContent = "center", gap = 12,
        children = {
            -- 取消按钮
            UI.Panel {
                width = 90, height = 36, borderRadius = 6,
                backgroundColor = Theme.BG_CARD,
                borderWidth = 1, borderColor = Theme.BORDER,
                justifyContent = "center", alignItems = "center",
                onClick = function(self)
                    AudioManager.Click()
                    modal:Close()
                    if onCancel then onCancel() end
                end,
                children = { UI.Label { text = "取消", fontSize = 15, fontColor = Theme.TEXT_MUTED } },
            },
            -- 确认按钮
            UI.Panel {
                width = 110, height = 36, borderRadius = 6,
                backgroundGradient = Theme.GRADIENT_PRIMARY,
                justifyContent = "center", alignItems = "center",
                onClick = function(self)
                    AudioManager.Click()
                    modal:Close()
                    if onConfirm then onConfirm() end
                end,
                children = { UI.Label { text = confirmText or "确认", fontSize = 15, fontColor = Theme.TEXT_WHITE } },
            },
        },
    })
    modal:Open()
end

-- ============================================================================
-- 弹窗：月报
-- ============================================================================

function GameScreen.ShowMonthlyReport(report)
    local desc = ""
    for _, evt in ipairs(report.events) do desc = desc .. "· " .. evt .. "\n" end
    if #desc == 0 then desc = "本月平安无事。" end
    local netSilver = report.incomes.silver - report.expenses.silver
    local netGrain = report.incomes.grain - report.expenses.grain
    local title = "月报 · " .. report.year .. "年" .. report.month .. "月"
    local summary = "银两" .. (netSilver >= 0 and "+" or "") .. netSilver .. "  粮食" .. (netGrain >= 0 and "+" or "") .. netGrain

    local contentChildren = {
        UI.Label { text = summary, fontSize = 15, fontColor = Theme.GOLD },
        UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER },
        UI.Label { text = desc, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, whiteSpace = "normal" },
    }

    -- 品级提升解锁通知
    if report.rankUpUnlocks then
        local ru = report.rankUpUnlocks
        contentChildren[#contentChildren + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.GOLD_DARK, marginTop = 4 }
        contentChildren[#contentChildren + 1] = UI.Panel {
            width = "100%", padding = 10, borderRadius = 8,
            backgroundGradient = { direction = "to-bottom", from = { 200, 165, 60, 40 }, to = { 200, 165, 60, 15 } },
            borderWidth = 1, borderColor = Theme.GOLD_DARK,
            gap = 6,
            children = (function()
                local items = {
                    UI.Label { text = "品级提升为【" .. ru.newRank .. "】！", fontSize = 17, fontColor = Theme.GOLD, textAlign = "center" },
                    UI.Label { text = "解锁新功能：", fontSize = 14, fontColor = Theme.TEXT_TITLE },
                }
                for _, unlockDesc in ipairs(ru.unlocks) do
                    items[#items + 1] = UI.Label { text = "  · " .. unlockDesc, fontSize = 13, fontColor = Theme.GREEN, whiteSpace = "normal" }
                end
                return items
            end)(),
        }
    end

    local modal = UI.Modal {
        title = title, size = "md", showCloseButton = true, closeOnOverlay = true,
        onClose = function() GameScreen.ResumeSpeed() end,
    }
    modal:AddContent(UI.ScrollView {
        width = "100%", maxHeight = 400, scrollY = true, bounces = true, showScrollbar = true,
        children = {
            UI.Panel { width = "100%", gap = 8, children = contentChildren },
        },
    })
    -- 广告按钮：双倍产出
    local adBtnChildren = {}
    if AdSystem.IsAvailable("double_income") then
        local remaining = AdSystem.GetRemaining("double_income")
        adBtnChildren[#adBtnChildren + 1] = UI.Panel {
            width = 130, height = 36, borderRadius = 6,
            backgroundGradient = { direction = "to-right", from = { 218, 165, 32, 255 }, to = { 200, 140, 20, 255 } },
            justifyContent = "center", alignItems = "center",
            flexDirection = "row", gap = 4,
            onClick = function(self)
                AdSystem.DoubleMonthlyIncome(report, function(success, a, b)
                    modal:Close()
                    if success then
                        GameScreen.ShowConfirm("双倍产出", "广告奖励已发放！\n额外获得银两" .. a .. "、粮食" .. b, "好的", function() end)
                    end
                    GameScreen.RefreshAll()
                end)
            end,
            children = {
                UI.Label { text = "看广告双倍", fontSize = 14, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                UI.Label { text = "(" .. remaining .. ")", fontSize = 12, fontColor = { 255, 255, 255, 180 } },
            },
        }
    end

    modal:SetFooter(UI.Panel {
        flexDirection = "row", justifyContent = "center", gap = 12,
        children = {
            UI.Panel {
                width = 100, height = 36, borderRadius = 6,
                backgroundGradient = Theme.GRADIENT_PRIMARY,
                justifyContent = "center", alignItems = "center",
                onClick = function(self) AudioManager.Click() modal:Close() end,
                children = { UI.Label { text = "朕知道了", fontSize = 15, fontColor = Theme.TEXT_WHITE } },
            },
            table.unpack(adBtnChildren),
        },
    })
    modal:Open()
end

-- ============================================================================
-- 弹窗：年终总结
-- ============================================================================

function GameScreen.ShowYearEndSummary(summary)
    if not summary then return end

    local root = UI.GetRoot()
    if not root then return end

    -- 移除已有的年终总结覆盖层
    local old = root:FindById("yearEndOverlay")
    if old then root:RemoveChild(old) end

    -- 辅助函数
    local function SignText(val, suffix)
        suffix = suffix or ""
        local v = math.floor(val)
        if v > 0 then return "+" .. v .. suffix
        elseif v < 0 then return tostring(v) .. suffix
        else return "±0" .. suffix end
    end
    local function SignColor(val)
        if val > 0 then return Theme.GREEN
        elseif val < 0 then return Theme.RED
        else return Theme.TEXT_MUTED end
    end

    local function ResIcon(img, size)
        size = size or 18
        return UI.Panel {
            width = size, height = size,
            backgroundImage = img, backgroundFit = "contain",
        }
    end

    local function StockCard(img, label, value, color)
        return UI.Panel {
            width = "48%", padding = 6, borderRadius = 8,
            backgroundColor = Theme.BG_INPUT, alignItems = "center", gap = 2,
            children = {
                ResIcon(img, 22),
                UI.Label { text = tostring(math.floor(value)), fontSize = 18, fontColor = color or Theme.TEXT_PRIMARY, fontWeight = "bold" },
                UI.Label { text = label, fontSize = 11, fontColor = Theme.TEXT_MUTED },
            },
        }
    end

    local function FinanceRow(img, label, income, expense, net)
        return UI.Panel {
            width = "100%", flexDirection = "row", alignItems = "center",
            justifyContent = "space-between", paddingVertical = 3,
            children = {
                UI.Panel {
                    flexDirection = "row", alignItems = "center", gap = 4, width = 60,
                    children = {
                        ResIcon(img, 14),
                        UI.Label { text = label, fontSize = 13, fontColor = Theme.TEXT_SECONDARY },
                    },
                },
                UI.Label { text = "+" .. math.floor(income), fontSize = 13, fontColor = Theme.GREEN },
                UI.Label { text = "-" .. math.floor(expense), fontSize = 13, fontColor = Theme.RED },
                UI.Label { text = SignText(net), fontSize = 14, fontColor = SignColor(net), fontWeight = "bold", textAlign = "right", width = 50 },
            },
        }
    end

    local function SectionTitle(text)
        return UI.Panel {
            width = "100%", paddingVertical = 2, marginTop = 6,
            borderBottomWidth = 1, borderColor = Theme.BORDER,
            children = {
                UI.Label { text = text, fontSize = 15, fontColor = Theme.GOLD, fontWeight = "bold" },
            },
        }
    end

    local function StatRow(icon, label, value, color)
        return UI.Panel {
            flexDirection = "row", alignItems = "center", justifyContent = "space-between",
            width = "100%", paddingVertical = 2,
            children = {
                UI.Panel {
                    flexDirection = "row", alignItems = "center", gap = 4,
                    children = {
                        UI.Label { text = icon, fontSize = 14 },
                        UI.Label { text = label, fontSize = 13, fontColor = Theme.TEXT_SECONDARY },
                    },
                },
                UI.Label { text = tostring(value), fontSize = 14, fontColor = color or Theme.TEXT_PRIMARY, fontWeight = "bold" },
            },
        }
    end

    -- 构建内容
    local contentChildren = {}

    -- 总评
    contentChildren[#contentChildren + 1] = UI.Panel {
        width = "100%", padding = 10, borderRadius = 8,
        backgroundGradient = { direction = "to-bottom", from = { 200, 165, 60, 50 }, to = { 200, 165, 60, 15 } },
        borderWidth = 1, borderColor = Theme.GOLD_DARK,
        flexDirection = "row", alignItems = "center", justifyContent = "center", gap = 8,
        children = {
            UI.Label { text = summary.ratingIcon, fontSize = 30 },
            UI.Panel {
                alignItems = "center", gap = 2,
                children = {
                    UI.Label { text = summary.rating, fontSize = 18, fontColor = Theme.GOLD, fontWeight = "bold" },
                    UI.Label { text = summary.clanRank .. " · " .. summary.aliveCount .. "口人", fontSize = 12, fontColor = Theme.TEXT_SECONDARY },
                },
            },
        },
    }

    -- 人丁
    contentChildren[#contentChildren + 1] = SectionTitle("人丁兴衰")
    contentChildren[#contentChildren + 1] = UI.Panel {
        width = "100%", flexDirection = "row", justifyContent = "space-between", paddingVertical = 3,
        children = {
            UI.Label { text = "族人 " .. summary.aliveCount .. "人", fontSize = 14, fontColor = Theme.TEXT_PRIMARY },
            UI.Label { text = "生 " .. summary.births, fontSize = 14, fontColor = summary.births > 0 and Theme.GREEN or Theme.TEXT_MUTED },
            UI.Label { text = "亡 " .. summary.deaths, fontSize = 14, fontColor = summary.deaths > 0 and Theme.RED or Theme.TEXT_MUTED },
            UI.Label { text = SignText(summary.popChange, "人"), fontSize = 14, fontColor = SignColor(summary.popChange), fontWeight = "bold" },
        },
    }

    -- 年末库存
    contentChildren[#contentChildren + 1] = SectionTitle("年末库存")
    contentChildren[#contentChildren + 1] = UI.Panel {
        width = "100%", flexDirection = "row", flexWrap = "wrap",
        justifyContent = "space-between", gap = 6,
        children = {
            StockCard(Theme.IMG.RES_SILVER, "银两", summary.currentSilver or 0, Theme.SILVER_COLOR or Theme.TEXT_PRIMARY),
            StockCard(Theme.IMG.RES_GRAIN, "粮食", summary.currentGrain or 0, Theme.GRAIN_COLOR or Theme.TEXT_PRIMARY),
            StockCard(Theme.IMG.RES_CLOTH, "布匹", summary.currentCloth or 0, Theme.CLOTH_COLOR or Theme.TEXT_PRIMARY),
            StockCard(Theme.IMG.RES_FAME, "声望", summary.currentFame or 0, Theme.FAME_COLOR or Theme.GOLD),
        },
    }

    -- 年度收支
    contentChildren[#contentChildren + 1] = SectionTitle("年度收支")
    contentChildren[#contentChildren + 1] = UI.Panel {
        width = "100%", flexDirection = "row", alignItems = "center",
        justifyContent = "space-between", paddingVertical = 1,
        children = {
            UI.Label { text = "", fontSize = 11, fontColor = Theme.TEXT_MUTED, width = 60 },
            UI.Label { text = "收入", fontSize = 11, fontColor = Theme.TEXT_MUTED },
            UI.Label { text = "支出", fontSize = 11, fontColor = Theme.TEXT_MUTED },
            UI.Label { text = "净值", fontSize = 11, fontColor = Theme.TEXT_MUTED, textAlign = "right", width = 50 },
        },
    }
    contentChildren[#contentChildren + 1] = FinanceRow(Theme.IMG.RES_SILVER, "银两", summary.incomes.silver, summary.expenses.silver, summary.netSilver)
    contentChildren[#contentChildren + 1] = FinanceRow(Theme.IMG.RES_GRAIN, "粮食", summary.incomes.grain, summary.expenses.grain, summary.netGrain)
    contentChildren[#contentChildren + 1] = FinanceRow(Theme.IMG.RES_CLOTH, "布匹", summary.incomes.cloth, summary.expenses.cloth, summary.netCloth)
    contentChildren[#contentChildren + 1] = FinanceRow(Theme.IMG.RES_FAME, "声望", summary.incomes.fame, summary.expenses.fame, summary.netFame)

    -- 功业
    local devItems = {}
    devItems[#devItems + 1] = StatRow("坊", "产业", summary.industryCount .. "处" .. (summary.industryChange ~= 0 and ("(" .. SignText(summary.industryChange) .. ")") or ""), summary.industryChange > 0 and Theme.GREEN or Theme.TEXT_PRIMARY)
    if summary.fortChange > 0 then
        devItems[#devItems + 1] = StatRow("寨", "新建寨堡", "+" .. summary.fortChange .. "座", Theme.GREEN)
    end
    if summary.examPasses > 0 then
        devItems[#devItems + 1] = StatRow("榜", "科举登第", summary.examPasses .. "人", Theme.FAME_COLOR or Theme.GOLD)
    end
    if summary.militaryMerits > 0 then
        devItems[#devItems + 1] = StatRow("勋", "军功立勋", summary.militaryMerits .. "次", Theme.RED)
    end
    if summary.rankChanged then
        local oldName = GameData.CLAN_RANKS[summary.oldRank] or "?"
        devItems[#devItems + 1] = StatRow("爵", "品级晋升", oldName .. " → " .. summary.clanRank, Theme.GOLD)
    end
    if #devItems > 0 then
        contentChildren[#contentChildren + 1] = SectionTitle("发展功业")
        for _, item in ipairs(devItems) do
            contentChildren[#contentChildren + 1] = item
        end
    end

    -- 关闭函数
    local function closeOverlay()
        AudioManager.Click()
        local o = root:FindById("yearEndOverlay")
        if o then root:RemoveChild(o) end
        -- 恢复游戏速度
        GameScreen.ResumeSpeed()
    end

    -- 构建全屏覆盖层
    local overlay = UI.Panel {
        id = "yearEndOverlay",
        width = "100%", height = "100%",
        position = "absolute", left = 0, top = 0, zIndex = 950,
        backgroundColor = { 0, 0, 0, 160 },
        justifyContent = "center", alignItems = "center",
        children = {
            -- 弹窗卡片
            UI.Panel {
                width = "92%", maxHeight = "90%",
                backgroundColor = Theme.BG_WHITE,
                borderRadius = 12, borderWidth = 1, borderColor = Theme.BORDER_GOLD,
                overflow = "hidden",
                children = {
                    -- 标题栏
                    UI.Panel {
                        width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                        padding = 12, borderBottomWidth = 1, borderColor = Theme.BORDER,
                        children = {
                            UI.Label { text = summary.yearLabel .. " · 年终总结", fontSize = 17, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                            UI.Panel {
                                width = 28, height = 28, borderRadius = 14,
                                backgroundColor = Theme.BG_INPUT,
                                justifyContent = "center", alignItems = "center",
                                onClick = function(self) closeOverlay() end,
                                children = { UI.Label { text = "X", fontSize = 16, fontColor = Theme.TEXT_MUTED } },
                            },
                        },
                    },
                    -- 可滚动内容区
                    UI.ScrollView {
                        width = "100%", flexGrow = 1, flexBasis = 0,
                        scrollY = true, showScrollbar = true, bounces = true,
                        children = {
                            UI.Panel {
                                width = "100%", padding = 12, gap = 2,
                                children = contentChildren,
                            },
                        },
                    },
                    -- 底部按钮
                    UI.Panel {
                        width = "100%", padding = 10, gap = 8,
                        justifyContent = "center", alignItems = "center",
                        borderTopWidth = 1, borderColor = Theme.BORDER,
                        children = {
                            -- 年终奖励翻倍广告按钮（每年1次）
                            (function()
                                -- 只有净收入为正时才显示翻倍按钮
                                local hasPositiveNet = (summary.netSilver > 0) or (summary.netGrain > 0) or (summary.netCloth > 0) or (summary.netFame > 0)
                                if hasPositiveNet and AdSystem.IsAvailable("year_bonus") then
                                    local rewards = {}
                                    if summary.netSilver > 0 then rewards.silver = math.floor(summary.netSilver) end
                                    if summary.netGrain > 0 then rewards.grain = math.floor(summary.netGrain) end
                                    if summary.netCloth > 0 then rewards.cloth = math.floor(summary.netCloth) end
                                    if summary.netFame > 0 then rewards.fame = math.floor(summary.netFame) end
                                    return UI.Panel {
                                        width = 200, height = 36, borderRadius = 8,
                                        backgroundGradient = { direction = "to-right", from = { 200, 160, 40, 255 }, to = { 180, 120, 20, 255 } },
                                        flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 4,
                                        onClick = function(self)
                                            AdSystem.DoubleYearBonus(rewards, function(success, msg)
                                                if success then
                                                    self:SetStyle { opacity = 0.3 }
                                                    GameScreen.ShowResultPopup("年终奖励翻倍", msg or "奖励已翻倍发放！")
                                                else
                                                    GameScreen.ShowResultPopup("翻倍失败", msg or "")
                                                end
                                            end)
                                        end,
                                        children = {
                                            UI.Label { text = "▶ 看广告·年终奖励翻倍", fontSize = 14, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                                        },
                                    }
                                else
                                    return UI.Panel { width = 0, height = 0 }
                                end
                            end)(),
                            UI.Panel {
                                width = 140, height = 38, borderRadius = 8,
                                backgroundGradient = Theme.GRADIENT_PRIMARY,
                                justifyContent = "center", alignItems = "center",
                                onClick = function(self) closeOverlay() end,
                                children = { UI.Label { text = "迎接新年", fontSize = 16, fontColor = Theme.TEXT_WHITE } },
                            },
                        },
                    },
                },
            },
        },
    }

    root:AddChild(overlay)
    AudioManager.Celebrate()
end

-- ============================================================================
-- 弹窗：联姻选配（单步流程：直接展示候选人，左头像右属性）
-- ============================================================================

function GameScreen.ShowMarriageTierSelect(member)
    local s = GameData.state
    local modal = UI.Modal { title = member.name .. " · 选择佳偶", size = "sm", showCloseButton = true, closeOnOverlay = true }

    local spouseGender = member.gender == "male" and "female" or "male"

    -- 候选配偶姓氏池
    local SPOUSE_SURNAMES = { "赵", "钱", "孙", "周", "吴", "郑", "冯", "褚", "卫", "蒋", "沈", "韩", "秦", "许", "何", "吕" }

    -- 聘礼：根据品级自动决定（品级越高花费越多、属性越好）
    local function getTierByRank()
        local rank = s.clanRank or 1
        for i = #GameData.MARRIAGE_TIERS, 1, -1 do
            local tier = GameData.MARRIAGE_TIERS[i]
            local req = GameData.MARRIAGE_UNLOCK[tier.id] or 1
            if rank >= req then return tier end
        end
        return GameData.MARRIAGE_TIERS[1]
    end

    local currentTier = getTierByRank()

    -- 随机生成一位候选配偶
    local function generateCandidate()
        local surname = SPOUSE_SURNAMES[math.random(1, #SPOUSE_SURNAMES)]
        local name = surname .. GameData.RandomGivenName(spouseGender)
        local age = math.max(16, member.age + math.random(-4, 3))
        local baseH = 55 + math.random(0, 40)
        -- 候选人属性按年龄模拟自然成长：16-20岁约 8-25
        local baseS = 5 + math.random(3, math.min(20, math.floor(age * 0.8)))
        local baseM = 5 + math.random(2, math.min(18, math.floor(age * 0.7)))
        -- 品级加成
        if currentTier.bonusType == "study" then baseS = baseS + (currentTier.bonusValue or 0)
        elseif currentTier.bonusType == "martial" then baseM = baseM + (currentTier.bonusValue or 0)
        end
        -- 随机天赋
        local talent = nil
        if math.random() < 0.25 then
            talent = GameData.TALENTS[math.random(1, #GameData.TALENTS)]
        end
        return {
            name = name, gender = spouseGender, age = age,
            health = math.min(100, baseH), study = math.min(100, baseS), martial = math.min(100, baseM),
            talent = talent,
        }
    end

    -- 根据属性计算聘礼（属性越好花费越高）
    local function calcBridePrice(c)
        local total = c.study + c.martial + c.health  -- 满分 300
        -- 银两：基础 5，每 30 点属性总和 +5，有天赋额外 +10
        local silver = 5 + math.floor(total / 30) * 5
        if c.talent then silver = silver + 10 end
        -- 布匹：属性总和 ≥80 才需要，每 40 点 +3，有天赋额外 +5
        local cloth = 0
        if total >= 80 then
            cloth = math.floor((total - 80) / 40) * 3 + 3
            if c.talent then cloth = cloth + 5 end
        end
        -- 声望奖励：基础 2，每 50 点 +2
        local fame = 2 + math.floor(total / 50) * 2
        return silver, cloth, fame
    end

    -- 展示单个候选人
    local function showCandidate(c)
        modal:ClearContent()
        local genderIcon = c.gender == "female" and "♀" or "♂"
        local genderColor = c.gender == "female" and { 200, 100, 120, 255 } or { 80, 120, 180, 255 }
        local avatarPath = AvatarSystem.GetAvatar({ id = "candidate_" .. c.name, gender = c.gender, age = c.age })
        local talentName = c.talent and c.talent.name or nil
        local adRemaining = AdSystem.GetRemaining("marriage_refresh")

        local silverCost, clothCost, famePlus = calcBridePrice(c)

        -- 确认选亲
        local function confirmMarriage()
            AudioManager.Click()
            if not GameData.CanAfford(silverCost, 0, clothCost, 0) then
                local need = "需要银" .. silverCost
                if clothCost > 0 then need = need .. "、布匹" .. clothCost end
                need = need .. "作为聘礼。"
                modal:Close()
                GameScreen.ShowResultPopup("资源不足", need)
                return
            end
            GameData.SpendResources(silverCost, 0, clothCost, 0)
            local newSpouse = GameData.CreateMember({
                name = c.name, gender = c.gender, age = c.age,
                generation = member.generation,
                spouseId = member.id, state = "在家",
                health = c.health, study = c.study, martial = c.martial,
                talent = c.talent,
            })
            member.spouseId = newSpouse.id
            -- 将候选人临时头像转移到正式成员ID，保持前后形象一致
            AvatarSystem.TransferAvatar("candidate_" .. c.name, newSpouse.id)
            GameData.AddResource("fame", famePlus)
            AudioManager.Celebrate()
            GameData.AddLog(member.name .. "与" .. c.name .. "喜结良缘。")
            modal:Close()
            local talentStr = talentName and ("天赋：" .. talentName) or ""
            GameScreen.ShowResultPopup("喜结良缘",
                member.name .. "与" .. c.name .. "联姻成功！\n"
                .. "声望+" .. famePlus .. "\n"
                .. "学识" .. c.study .. " 武艺" .. c.martial .. " 健康" .. c.health
                .. (talentStr ~= "" and ("\n" .. talentStr) or ""))
            GameScreen.RefreshAll()
        end

        local content = UI.Panel {
            width = "100%", gap = 8,
            children = {
                -- 主体：左头像 + 右属性
                UI.Panel {
                    width = "100%", flexDirection = "row", gap = 10, padding = 10,
                    borderRadius = 10, backgroundColor = Theme.BG_WHITE,
                    borderWidth = 1, borderColor = Theme.BORDER_GOLD,
                    children = {
                        -- 左侧：头像 + 名字 + 看广告换人
                        UI.Panel {
                            width = 80, alignItems = "center", gap = 4,
                            children = {
                                -- 头像
                                UI.Panel {
                                    width = 72, height = 72, borderRadius = 36,
                                    borderWidth = 2, borderColor = Theme.GOLD_LIGHT,
                                    overflow = "hidden",
                                    children = {
                                        UI.Panel { width = 72, height = 72, borderRadius = 36, backgroundImage = avatarPath, backgroundFit = "cover" },
                                    },
                                },
                                -- 性别 + 名字
                                UI.Panel {
                                    flexDirection = "row", alignItems = "center", gap = 2,
                                    children = {
                                        UI.Label { text = genderIcon, fontSize = 14, fontColor = genderColor },
                                        UI.Label { text = c.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                                    },
                                },
                                UI.Label { text = c.age .. "岁", fontSize = 12, fontColor = Theme.TEXT_SECONDARY },
                                -- 看广告换人按钮
                                UI.Panel {
                                    width = 80, paddingVertical = 5, borderRadius = 6, marginTop = 2,
                                    backgroundColor = adRemaining > 0 and Theme.BG_WHITE or Theme.BG_INPUT,
                                    borderWidth = 1, borderColor = adRemaining > 0 and Theme.PRIMARY or Theme.BORDER,
                                    opacity = adRemaining > 0 and 1.0 or 0.4,
                                    justifyContent = "center", alignItems = "center",
                                    onClick = function(self)
                                        if adRemaining <= 0 then return end
                                        AudioManager.Click()
                                        AdSystem.ShowRewardAd("marriage_refresh", function()
                                            showCandidate(generateCandidate())
                                        end, function(msg)
                                            GameScreen.ShowResultPopup("提示", msg or "广告播放失败")
                                        end)
                                    end,
                                    children = {
                                        UI.Label {
                                            text = "▶ 换一位",
                                            fontSize = 12, fontColor = adRemaining > 0 and Theme.PRIMARY or Theme.TEXT_MUTED,
                                        },
                                        UI.Label {
                                            text = "看广告(" .. adRemaining .. "次)",
                                            fontSize = 10, fontColor = Theme.TEXT_MUTED, marginTop = 1,
                                        },
                                    },
                                },
                            },
                        },
                        -- 右侧：属性面板 + 确定按钮
                        UI.Panel {
                            flexGrow = 1, flexShrink = 1, gap = 5, justifyContent = "space-between",
                            children = (function()
                                local items = {}
                                -- 天赋标签（有天赋才显示）
                                if talentName then
                                    items[#items + 1] = UI.Panel {
                                        paddingHorizontal = 6, paddingVertical = 2,
                                        borderRadius = 4, backgroundColor = { 255, 200, 50, 40 },
                                        alignSelf = "flex-start",
                                        children = {
                                            UI.Label { text = "天赋：" .. talentName, fontSize = 12, fontColor = Theme.GOLD_DARK },
                                        },
                                    }
                                end
                                -- 属性条
                                items[#items + 1] = StatBar("学识", c.study)
                                items[#items + 1] = StatBar("武艺", c.martial)
                                items[#items + 1] = StatBar("健康", c.health)
                                -- 聘礼提示
                                items[#items + 1] = UI.Label {
                                    text = "聘礼：银" .. silverCost .. (clothCost > 0 and ("  布" .. clothCost) or ""),
                                    fontSize = 12, fontColor = Theme.TEXT_MUTED, marginTop = 2,
                                }
                                -- 确定按钮（右下角）
                                items[#items + 1] = UI.Panel {
                                    alignSelf = "flex-end", marginTop = 4,
                                    paddingHorizontal = 20, paddingVertical = 7, borderRadius = 8,
                                    backgroundGradient = { direction = "to-right", from = { 180, 50, 50, 255 }, to = { 200, 70, 70, 255 } },
                                    onClick = function(self) confirmMarriage() end,
                                    children = {
                                        UI.Label { text = "确定", fontSize = 15, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                                    },
                                }
                                return items
                            end)(),
                        },
                    },
                },
            },
        }
        modal:AddContent(content)
    end

    -- 直接展示第一个候选人
    showCandidate(generateCandidate())
    modal:Open()
end

-- ============================================================================
-- 弹窗：装备选择（从库房选择装备穿戴）
-- ============================================================================

function GameScreen.ShowEquipSelect(member, slot)
    local slotName = GameData.SLOT_NAMES and GameData.SLOT_NAMES[slot] or slot
    local modal = UI.Modal { title = member.name .. " · 装备" .. slotName, size = "md", showCloseButton = true, closeOnOverlay = true }
    local items = {}

    -- 收集库房中该槽位的可用装备
    local s = GameData.state
    if not s.inventory then s.inventory = {} end
    local hasItems = false
    for _, invItem in ipairs(s.inventory) do
        if invItem.count > 0 then
            local equip = GameData.GetEquipment(invItem.itemId)
            if equip and equip.slot == slot then
                hasItems = true
                local rarityConf = GameData.GetRarityConfig(equip.rarity)
                local nameColor = rarityConf and rarityConf.color or Theme.TEXT_PRIMARY
                local statsText = ""
                if equip.martial > 0 then statsText = statsText .. "武+" .. equip.martial .. " " end
                if equip.study > 0 then statsText = statsText .. "文+" .. equip.study .. " " end
                if equip.health > 0 then statsText = statsText .. "体+" .. equip.health end
                items[#items + 1] = UI.Panel {
                    width = "100%", padding = 10, borderRadius = 8,
                    backgroundColor = Theme.BG_WHITE,
                    borderWidth = 1, borderColor = rarityConf and rarityConf.color or Theme.BORDER,
                    flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                    onTap = function(self)
                        AudioManager.Click()
                        local ok, msg = EquipmentSystem.Equip(member.id, equip.id)
                        modal:Close()
                        if ok then
                            GameScreen.ShowMemberDetail(member)
                        else
                            GameScreen.ShowResultPopup("装备失败", msg)
                        end
                    end,
                    children = {
                        UI.Panel {
                            gap = 2, flexShrink = 1,
                            children = {
                                UI.Panel {
                                    flexDirection = "row", gap = 4, alignItems = "center",
                                    children = {
                                        UI.Label { text = equip.name, fontSize = 15, fontColor = nameColor, fontWeight = "bold" },
                                        UI.Label { text = "(" .. rarityConf.name .. ")", fontSize = 12, fontColor = nameColor },
                                        equip.heirloom and UI.Label { text = "传世", fontSize = 11, fontColor = Theme.GOLD } or nil,
                                    },
                                },
                                UI.Label { text = statsText, fontSize = 12, fontColor = Theme.TEXT_SECONDARY },
                                UI.Label { text = equip.desc, fontSize = 11, fontColor = Theme.TEXT_MUTED },
                            },
                        },
                        UI.Label { text = "×" .. invItem.count, fontSize = 14, fontColor = Theme.TEXT_SECONDARY },
                    },
                }
            end
        end
    end

    if not hasItems then
        items[#items + 1] = UI.Panel {
            width = "100%", padding = 16, alignItems = "center", gap = 6,
            children = {
                UI.Label { text = "库房中没有" .. slotName, fontSize = 15, fontColor = Theme.TEXT_MUTED },
                UI.Panel {
                    paddingHorizontal = 12, paddingVertical = 6, borderRadius = 6,
                    backgroundGradient = Theme.GRADIENT_PRIMARY,
                    onTap = function(self)
                        AudioManager.Click()
                        modal:Close()
                        GameScreen.ShowEquipShop(member)
                    end,
                    children = { UI.Label { text = "前往购买", fontSize = 14, fontColor = Theme.TEXT_WHITE } },
                },
            },
        }
    end

    modal:AddContent(UI.ScrollView {
        width = "100%", maxHeight = 400, scrollY = true, bounces = true, showScrollbar = true,
        children = { UI.Panel { width = "100%", gap = 6, children = items } },
    })
    modal:Open()
end

-- ============================================================================
-- 弹窗：装备商店（购买装备到库房）
-- ============================================================================

function GameScreen.ShowEquipShop(member)
    local modal = UI.Modal { title = "购买装备", size = "md", showCloseButton = true, closeOnOverlay = true }
    local s = GameData.state

    -- 按槽位分组显示
    local items = {}
    for _, slot in ipairs(GameData.EQUIPMENT_SLOTS) do
        local slotName = GameData.SLOT_NAMES and GameData.SLOT_NAMES[slot] or slot
        items[#items + 1] = UI.Label { text = slotName, fontSize = 15, fontColor = Theme.GOLD, marginTop = 4 }

        local equipList = GameData.GetEquipmentBySlot(slot)
        for _, equip in ipairs(equipList) do
            local rarityConf = GameData.GetRarityConfig(equip.rarity)
            local nameColor = rarityConf and rarityConf.color or Theme.TEXT_PRIMARY
            local canBuy = s.silver >= equip.cost
            local inStock = EquipmentSystem.GetInventoryCount(equip.id)
            local statsText = ""
            if equip.martial > 0 then statsText = statsText .. "武+" .. equip.martial .. " " end
            if equip.study > 0 then statsText = statsText .. "文+" .. equip.study .. " " end
            if equip.health > 0 then statsText = statsText .. "体+" .. equip.health end

            items[#items + 1] = UI.Panel {
                width = "100%", padding = 8, borderRadius = 6,
                backgroundColor = canBuy and Theme.BG_WHITE or Theme.BG_INPUT,
                borderWidth = 1, borderColor = canBuy and (rarityConf and rarityConf.color or Theme.BORDER) or Theme.BORDER,
                flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                opacity = canBuy and 1.0 or 0.5,
                children = {
                    UI.Panel {
                        gap = 1, flexShrink = 1,
                        children = {
                            UI.Panel {
                                flexDirection = "row", gap = 4, alignItems = "center",
                                children = {
                                    UI.Label { text = equip.name, fontSize = 14, fontColor = nameColor },
                                    UI.Label { text = rarityConf.name, fontSize = 11, fontColor = nameColor },
                                    inStock > 0 and UI.Label { text = "库存" .. inStock, fontSize = 11, fontColor = Theme.GREEN } or nil,
                                },
                            },
                            UI.Label { text = statsText, fontSize = 11, fontColor = Theme.TEXT_MUTED },
                        },
                    },
                    UI.Panel {
                        paddingHorizontal = 8, paddingVertical = 4, borderRadius = 4,
                        backgroundColor = canBuy and Theme.PRIMARY or Theme.BG_INPUT,
                        onTap = function(self)
                            if not canBuy then return end
                            AudioManager.Click()
                            local ok, msg = EquipmentSystem.BuyEquipment(equip.id)
                            modal:Close()
                            GameScreen.ShowResultPopup(ok and "购买成功" or "购买失败", msg)
                            if ok then
                                GameScreen.ShowEquipShop(member)
                            end
                        end,
                        children = { UI.Label { text = "银" .. equip.cost, fontSize = 12, fontColor = canBuy and Theme.TEXT_WHITE or Theme.TEXT_MUTED } },
                    },
                },
            }
        end
    end

    modal:AddContent(UI.ScrollView {
        width = "100%", height = 350, scrollY = true, showScrollbar = true,
        children = { UI.Panel { width = "100%", gap = 4, children = items } },
    })
    modal:SetFooter(UI.Panel {
        flexDirection = "row", justifyContent = "space-between", alignItems = "center", width = "100%",
        children = {
            UI.Label { text = "当前银两：" .. s.silver, fontSize = 14, fontColor = Theme.SILVER_COLOR },
            UI.Panel {
                paddingHorizontal = 12, paddingVertical = 6, borderRadius = 6,
                backgroundColor = Theme.BG_CARD, borderWidth = 1, borderColor = Theme.BORDER,
                onClick = function(self) AudioManager.Click() modal:Close(); GameScreen.ShowMemberDetail(member) end,
                children = { UI.Label { text = "返回详情", fontSize = 14, fontColor = Theme.TEXT_PRIMARY } },
            },
        },
    })
    modal:Open()
end

-- ============================================================================
-- 弹窗：批量安排
-- ============================================================================

function GameScreen.ShowBatchAssign()
    local idle = GameData.GetIdleMembers()
    if #idle == 0 then GameScreen.ShowResultPopup("无闲人", "没有空闲族人可安排。"); return end

    local modal = UI.Modal { title = "批量安排工作", size = "md", showCloseButton = true, closeOnOverlay = true }
    local tradeUnlocked = GameData.state.clanRank >= 2
    local militaryUnlocked = GameData.state.clanRank >= 3
    local jobs = {
        { id = "读书", label = "全部读书", icon = "读", color = Theme.BLUE, unlocked = true, filter = function(m) return m.age >= 6 and m.age <= 50 end },
        { id = "经商", label = "全部经商", icon = "商", color = Theme.GOLD_DARK, unlocked = tradeUnlocked, lockMsg = "需品级【农户】", filter = function(m) return m.age >= 15 end },
        { id = "从军", label = "全部从军", icon = "军", color = Theme.RED, unlocked = militaryUnlocked, lockMsg = "需品级【乡绅】", filter = function(m) return m.age >= 16 and m.age <= 45 and m.gender == "male" end },
    }
    local jobBtns = {}
    for _, job in ipairs(jobs) do
        local eligible = {}
        if job.unlocked then
            for _, m in ipairs(idle) do if job.filter(m) then eligible[#eligible + 1] = m end end
        end
        local isLocked = not job.unlocked
        jobBtns[#jobBtns + 1] = UI.Panel {
            width = "100%", padding = 12, borderRadius = 8,
            backgroundColor = Theme.BG_WHITE,
            borderWidth = 1, borderColor = Theme.BORDER,
            flexDirection = "row", justifyContent = "space-between", alignItems = "center",
            opacity = isLocked and 0.35 or (#eligible > 0 and 1.0 or 0.4),
            onTap = function(self)
                AudioManager.Click()
                if isLocked then
                    GameScreen.ShowResultPopup("功能未解锁", job.label .. job.lockMsg)
                    return
                end
                if #eligible == 0 then return end
                for _, m in ipairs(eligible) do
                    m.state = job.id
                    if job.id == "从军" then m.identity = "士兵"; m.militaryRank = "士兵" end
                end
                GameData.AddLog("批量安排" .. #eligible .. "人" .. job.id .. "。")
                modal:Close(); GameScreen.RefreshAll()
            end,
            children = {
                UI.Panel {
                    flexDirection = "row", gap = 6, alignItems = "center",
                    children = {
                        UI.Label { text = isLocked and "[锁]" or job.icon, fontSize = 20, fontColor = isLocked and Theme.TEXT_MUTED or job.color },
                        UI.Panel { gap = 2, children = {
                            UI.Label { text = job.label, fontSize = 15, fontColor = isLocked and Theme.TEXT_MUTED or Theme.TEXT_PRIMARY },
                            UI.Label { text = isLocked and job.lockMsg or ("可安排" .. #eligible .. "人"), fontSize = 12, fontColor = Theme.TEXT_MUTED },
                        }},
                    },
                },
                UI.Label { text = isLocked and "" or (#eligible .. "人 →"), fontSize = 14, fontColor = job.color },
            },
        }
    end
    jobBtns[#jobBtns + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER }
    jobBtns[#jobBtns + 1] = UI.Panel {
        width = "100%", padding = 12, borderRadius = 8, backgroundColor = Theme.BG_INPUT,
        borderWidth = 1, borderColor = Theme.BORDER, justifyContent = "center", alignItems = "center",
        onTap = function(self)
            AudioManager.Click()
            local count = 0
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.state ~= "在家" and m.state ~= "从军" and m.state ~= "出征" and m.state ~= "阵亡" then
                    m.state = "在家"; count = count + 1
                end
            end
            if count > 0 then GameData.AddLog("召回" .. count .. "名族人回家。") end
            modal:Close(); GameScreen.RefreshAll()
        end,
        children = { UI.Label { text = "全部召回在家", fontSize = 15, fontColor = Theme.TEXT_PRIMARY } },
    }
    modal:AddContent(UI.ScrollView {
        width = "100%", maxHeight = 400, scrollY = true, bounces = true, showScrollbar = true,
        children = { UI.Panel { width = "100%", gap = 8, children = jobBtns } },
    })
    modal:Open()
end

-- ============================================================================
-- 弹窗：终局结算
-- ============================================================================

function GameScreen.ShowEndingScreen()
    local s = GameData.state
    if not s.gameEnded then return end
    local aliveCount = #GameData.GetAliveMembers()
    local totalBorn = #s.members
    local score = aliveCount * 10 + s.fame * 2 + (s.totalExamPasses or 0) * 20 + s.silver + math.floor(s.grain / 2) + s.fortCount * 30 + (s.clanRank - 1) * 50

    -- 隐藏结局额外加分
    local hiddenEndingTitle = nil
    if s.hiddenEnding then
        local EndingSystem = require("Systems.EndingSystem")
        local info = EndingSystem.GetEndingInfo(s.hiddenEnding)
        if info then hiddenEndingTitle = info.title end
        -- 隐藏结局加分
        if s.hiddenEnding == "conquest_complete" then score = score + 600
        elseif s.hiddenEnding == "scholar_dynasty" then score = score + 300
        elseif s.hiddenEnding == "warlord" then score = score + 250
        elseif s.hiddenEnding == "merchant_empire" then score = score + 200
        elseif s.hiddenEnding == "utopia" then score = score + 500
        elseif s.hiddenEnding == "executed" then score = math.max(0, score - 200)
        elseif s.hiddenEnding == "extinction" then score = 0
        end
    end

    local grade, gradeColor = "庶民", Theme.TEXT_MUTED
    if score >= 1000 then grade = "名门望族"; gradeColor = Theme.GOLD
    elseif score >= 700 then grade = "乡绅世家"; gradeColor = { 200, 160, 60, 255 }
    elseif score >= 400 then grade = "耕读人家"; gradeColor = Theme.GREEN
    elseif score >= 200 then grade = "小康之家"; gradeColor = Theme.BLUE
    end

    -- 结局描述
    local endDesc, modalTitle = "", "大明覆灭 · 终局"
    if hiddenEndingTitle then
        modalTitle = hiddenEndingTitle .. " · 隐藏结局"
        endDesc = "你的宗族在大明的风云变幻中走出了一条独特的道路。"
    elseif s.endingChoice == "resist" then endDesc = "甲申之变，天崩地裂。你的宗族选择了抵抗，以血肉之躯捍卫家园。"
    elseif s.endingChoice == "flee" then endDesc = "甲申之变，天崩地裂。你的宗族选择了南迁避难，颠沛流离，但血脉得以延续。"
    elseif s.endingChoice == "surrender" then endDesc = "甲申之变，天崩地裂。你的宗族选择了归顺新朝，保全了眼前的性命与财产。"
    else endDesc = "甲申之变，大明覆亡。你的宗族在这二百七十六年间留下了自己的痕迹。"
    end

    -- 已解锁隐藏结局列表
    local unlockedChildren = {}
    local triggered = s.triggeredHiddenEndings or {}
    local EndingSys = require("Systems.EndingSystem")
    for _, ending in ipairs(EndingSys.HIDDEN_ENDINGS) do
        local unlocked = triggered[ending.id]
        local label = unlocked and ending.title or "???"
        local color = unlocked and Theme.GOLD or Theme.TEXT_MUTED
        unlockedChildren[#unlockedChildren + 1] = UI.Label {
            text = (unlocked and "[达成] " or "[未知] ") .. label,
            fontSize = 13, fontColor = color,
        }
    end

    local modal = UI.Modal { title = modalTitle, size = "fullscreen", showCloseButton = false, closeOnOverlay = false }
    -- 隐藏结局收集：安全构建children数组（避免table.unpack不在尾部的问题）
    local endingCollectChildren = {
        UI.Label { text = "隐藏结局收集", fontSize = 16, fontColor = Theme.GOLD },
    }
    for _, child in ipairs(unlockedChildren) do
        endingCollectChildren[#endingCollectChildren + 1] = child
    end
    modal:AddContent(UI.ScrollView {
        width = "100%", maxHeight = 500, scrollY = true, bounces = true, showScrollbar = true,
        children = {
            UI.Panel {
                width = "100%", gap = 10, padding = 4,
                children = {
                    UI.Label { text = endDesc, fontSize = 15, fontColor = Theme.TEXT_PRIMARY, whiteSpace = "normal" },
                    UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER },
                    UI.Panel {
                        width = "100%", alignItems = "center", gap = 4,
                        children = {
                            UI.Label { text = "宗族评定", fontSize = 18, fontColor = Theme.GOLD },
                            UI.Label { text = grade, fontSize = 24, fontColor = gradeColor },
                            UI.Label { text = "总分：" .. score, fontSize = 16, fontColor = Theme.TEXT_PRIMARY },
                        },
                    },
                    UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER },
                    UI.Panel {
                        width = "100%", gap = 4,
                        children = {
                            UI.Label { text = "传承：" .. s.totalMonths .. "个月", fontSize = 14, fontColor = Theme.TEXT_SECONDARY },
                            UI.Label { text = "存活族人：" .. aliveCount .. "人（共" .. totalBorn .. "人）", fontSize = 14, fontColor = Theme.TEXT_SECONDARY },
                            UI.Label { text = "宗族品级：" .. GameData.GetClanRankName(), fontSize = 14, fontColor = Theme.TEXT_SECONDARY },
                            UI.Label { text = "科举通过：" .. (s.totalExamPasses or 0) .. "次", fontSize = 14, fontColor = Theme.TEXT_SECONDARY },
                            UI.Label { text = "寨堡：" .. s.fortCount .. "座", fontSize = 14, fontColor = Theme.TEXT_SECONDARY },
                            UI.Label { text = "剩余银两：" .. s.silver, fontSize = 14, fontColor = Theme.TEXT_SECONDARY },
                        },
                    },
                    -- 隐藏结局收集展示
                    UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER },
                    UI.Panel { width = "100%", gap = 4, children = endingCollectChildren },
                },
            },
        },
    })
    modal:SetFooter(UI.Panel {
        flexDirection = "row", justifyContent = "center", gap = 16,
        children = {
            UI.Panel {
                width = 120, height = 40, borderRadius = 6,
                backgroundGradient = Theme.GRADIENT_PRIMARY,
                justifyContent = "center", alignItems = "center",
                onClick = function(self)
                    AudioManager.Click()
                    modal:Close()
                    -- 返回主菜单
                    local SaveSystem = require("Systems.SaveSystem")
                    if GameData.state then SaveSystem.AutoSave() end
                    if ShowScreen then ShowScreen("menu") end
                end,
                children = { UI.Label { text = "返回主菜单", fontSize = 16, fontColor = Theme.TEXT_WHITE } },
            },
        },
    })
    modal:Open()
end

-- ============================================================================
-- 弹窗：钱庄贷款
-- ============================================================================

function GameScreen.ShowLoanDialog()
    local s = GameData.state
    local loans = AdSystem.GetLoans()
    local totalDebt, monthlyInterest = AdSystem.GetTotalDebt()
    local canBorrow = AdSystem.CanBorrow()

    local modal = UI.Modal {
        size = "md",
        showCloseButton = false,
        closeOnOverlay = true,
    }
    modal:AddContent(CreateLeftCloseHeader(modal, "钱庄 · 借贷"))

    local children = {}

    -- 当前负债概览
    children[#children + 1] = UI.Panel {
        width = "100%", padding = 10, borderRadius = 8,
        backgroundColor = Theme.BG_INPUT, gap = 4,
        children = {
            UI.Label { text = "当前负债", fontSize = 15, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
            UI.Panel {
                width = "100%", flexDirection = "row", justifyContent = "space-between",
                children = {
                    UI.Label { text = "贷款笔数", fontSize = 13, fontColor = Theme.TEXT_SECONDARY },
                    UI.Label { text = #loans .. "/3", fontSize = 13, fontColor = #loans >= 3 and Theme.RED or Theme.TEXT_PRIMARY, fontWeight = "bold" },
                },
            },
            UI.Panel {
                width = "100%", flexDirection = "row", justifyContent = "space-between",
                children = {
                    UI.Label { text = "总欠款本金", fontSize = 13, fontColor = Theme.TEXT_SECONDARY },
                    UI.Label { text = totalDebt .. "两", fontSize = 13, fontColor = totalDebt > 0 and Theme.RED or Theme.TEXT_MUTED, fontWeight = "bold" },
                },
            },
            UI.Panel {
                width = "100%", flexDirection = "row", justifyContent = "space-between",
                children = {
                    UI.Label { text = "每月利息支出", fontSize = 13, fontColor = Theme.TEXT_SECONDARY },
                    UI.Label { text = monthlyInterest .. "两/月", fontSize = 13, fontColor = monthlyInterest > 0 and Theme.RED or Theme.TEXT_MUTED, fontWeight = "bold" },
                },
            },
        },
    }

    -- 当前贷款明细
    if #loans > 0 then
        children[#children + 1] = UI.Label { text = "在贷明细", fontSize = 15, fontColor = Theme.GOLD, fontWeight = "bold", marginTop = 8 }
        for idx, loan in ipairs(loans) do
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 8, borderRadius = 6,
                backgroundColor = Theme.BG_WHITE, borderWidth = 1, borderColor = Theme.BORDER,
                flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                children = {
                    UI.Panel {
                        gap = 2,
                        children = {
                            UI.Label { text = loan.name, fontSize = 13, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                            UI.Label { text = "本金" .. loan.principal .. "两 · 月息" .. loan.interest .. "两 · 剩余" .. loan.monthsLeft .. "月", fontSize = 11, fontColor = Theme.TEXT_MUTED },
                        },
                    },
                    (function()
                        if s.silver >= loan.principal then
                            return UI.Panel {
                                width = 60, height = 28, borderRadius = 6,
                                backgroundGradient = { direction = "to-right", from = { 60, 160, 120, 255 }, to = { 40, 140, 100, 255 } },
                                justifyContent = "center", alignItems = "center",
                                onTap = function(self)
                                    AudioManager.Click()
                                    AdSystem.RepayLoan(idx, function(success, msg)
                                        modal:Close()
                                        GameScreen.ShowResultPopup(success and "还款成功" or "还款失败", msg or "")
                                        GameScreen.RefreshAll()
                                    end)
                                end,
                                children = { UI.Label { text = "还清", fontSize = 12, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" } },
                            }
                        else
                            return UI.Label { text = "银两不足", fontSize = 11, fontColor = Theme.RED }
                        end
                    end)(),
                },
            }
        end
    end

    -- ====== 看广告领银两（白送，不用还）======
    local adGrantRemain = AdSystem.GetRemaining("ad_grant")
    children[#children + 1] = UI.Panel {
        width = "100%", flexDirection = "row", alignItems = "center", gap = 4, marginTop = 8,
        children = {
            UI.Label { text = "看广告领银", fontSize = 15, fontColor = { 40, 160, 80, 255 }, fontWeight = "bold" },
            UI.Label { text = "（白送不用还）", fontSize = 12, fontColor = { 40, 160, 80, 180 } },
        },
    }
    if adGrantRemain <= 0 then
        children[#children + 1] = UI.Label { text = "本年领取次数已用完，明年再来", fontSize = 13, fontColor = Theme.TEXT_MUTED }
    else
        children[#children + 1] = UI.Label { text = "今年剩余" .. adGrantRemain .. "次", fontSize = 12, fontColor = Theme.TEXT_MUTED }
        for _, opt in ipairs(AdSystem.AD_GRANT_OPTIONS) do
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 10, borderRadius = 8,
                backgroundColor = { 230, 250, 235, 255 }, borderWidth = 1, borderColor = { 40, 160, 80, 80 },
                gap = 4,
                children = {
                    UI.Panel {
                        width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                        children = {
                            UI.Label { text = opt.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                            UI.Label { text = "领银" .. opt.amount .. "两", fontSize = 14, fontColor = { 40, 160, 80, 255 }, fontWeight = "bold" },
                        },
                    },
                    UI.Label { text = opt.desc .. "（不用还）", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                    UI.Panel {
                        width = "100%", height = 32, borderRadius = 6, marginTop = 4,
                        backgroundGradient = { direction = "to-right", from = { 60, 180, 100, 255 }, to = { 40, 150, 80, 255 } },
                        flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 4,
                        onTap = function(self)
                            AudioManager.Click()
                            AdSystem.TakeAdGrant(opt.id, function(success, msg)
                                modal:Close()
                                GameScreen.ShowResultPopup(success and "领取成功" or "领取失败", msg or "")
                                GameScreen.RefreshAll()
                            end)
                        end,
                        children = {
                            UI.Label { text = "▶ 看广告·免费领取", fontSize = 13, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                        },
                    },
                },
            }
        end
    end

    -- ====== 普通借贷（有利息要还，不看广告）======
    children[#children + 1] = UI.Panel {
        width = "100%", flexDirection = "row", alignItems = "center", gap = 4, marginTop = 10,
        children = {
            UI.Label { text = "普通借贷", fontSize = 15, fontColor = Theme.GOLD, fontWeight = "bold" },
            UI.Label { text = "（有利息，需按月还息到期还本）", fontSize = 12, fontColor = Theme.TEXT_MUTED },
        },
    }
    if not canBorrow then
        children[#children + 1] = UI.Label { text = "已达最大贷款笔数（3笔），请还清后再借", fontSize = 13, fontColor = Theme.RED }
    else
        for _, opt in ipairs(AdSystem.LOAN_OPTIONS) do
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 10, borderRadius = 8,
                backgroundColor = Theme.BG_WHITE, borderWidth = 1, borderColor = Theme.BORDER_GOLD,
                gap = 4,
                children = {
                    UI.Panel {
                        width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                        children = {
                            UI.Label { text = opt.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                            UI.Label { text = "借银" .. opt.amount .. "两", fontSize = 14, fontColor = Theme.GOLD, fontWeight = "bold" },
                        },
                    },
                    UI.Label { text = opt.desc, fontSize = 12, fontColor = Theme.TEXT_MUTED },
                    UI.Panel {
                        width = "100%", height = 32, borderRadius = 6, marginTop = 4,
                        backgroundGradient = { direction = "to-right", from = { 200, 160, 40, 255 }, to = { 180, 120, 20, 255 } },
                        justifyContent = "center", alignItems = "center",
                        onTap = function(self)
                            AudioManager.Click()
                            AdSystem.TakeLoan(opt.id, function(success, msg)
                                modal:Close()
                                GameScreen.ShowResultPopup(success and "借款成功" or "借款失败", msg or "")
                                GameScreen.RefreshAll()
                            end)
                        end,
                        children = {
                            UI.Label { text = "立即借款", fontSize = 13, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                        },
                    },
                },
            }
        end
    end

    -- 风险提示
    children[#children + 1] = UI.Panel {
        width = "100%", padding = 6, borderRadius = 4, marginTop = 6,
        backgroundColor = { 200, 60, 60, 20 },
        children = {
            UI.Label { text = "提示：看广告领的银两白送不用还；普通借贷到期未还将扣除声望", fontSize = 11, fontColor = Theme.RED },
        },
    }

    modal:AddContent(UI.ScrollView {
        width = "100%", maxHeight = 400,
        scrollY = true, showScrollbar = true, bounces = true,
        children = {
            UI.Panel { width = "100%", padding = 8, gap = 6, children = children },
        },
    })
    modal:Open()
end

-- ============================================================================
-- 祭天祈福对话框（乡绅/品级3解锁）
-- 三步占卜玩法：选祭品→抽卦象→抉择命运
-- ============================================================================

function GameScreen.ShowPrayDialog()
    local s = GameData.state
    if not s then return end

    -- 冷却检查：每季一次（每3个月）
    if s.lastPrayMonth and (s.totalMonths - s.lastPrayMonth) < 3 then
        local remain = 3 - (s.totalMonths - s.lastPrayMonth)
        GameScreen.ShowResultPopup("祭天祈福", "距下次祈福尚需" .. remain .. "个月\n天地有常，不可频繁叨扰神灵")
        return
    end

    local modal = UI.Modal { title = "祭天祈福", size = "md" }

    -- 祭品等级
    local tiers = {
        { name = "素果清香", silver = 10, grain = 15, luck = 0, desc = "心诚则灵，简朴感天" },
        { name = "三牲大礼", silver = 30, grain = 40, luck = 1, desc = "礼敬鬼神，虔诚祷告" },
        { name = "金帛祭典", silver = 80, grain = 80, luck = 2, desc = "告天祭地，歌舞升平" },
    }

    -- 卦象库：每个卦象包含一个情境和两个选择
    local allHexagrams = {
        { gua = "乾", symbol = "天", name = "天行健",
          color = { 200, 160, 40, 255 },
          scene = "卦象示大旱之兆，田地将荒。\n族长面前有两条路：",
          choices = {
              { text = "开渠引水，以人力胜天",
                good = { desc = "渠成水到，粮食丰收！粮+60，全族健康+5", apply = function()
                    GameData.AddResource("grain", 60)
                    for _, m in ipairs(GameData.GetAliveMembers()) do m.health = math.min(100, m.health + 5) end
                end },
                bad = { desc = "劳民伤财，渠未成而银耗尽。银-15", apply = function()
                    GameData.AddResource("silver", -15)
                end },
                base = 55 },
              { text = "焚香祈雨，求苍天怜悯",
                good = { desc = "甘霖普降！粮+40，声望+10", apply = function()
                    GameData.AddResource("grain", 40); GameData.AddResource("fame", 10)
                end },
                bad = { desc = "苍天无情，大旱依旧。粮-10", apply = function()
                    GameData.AddResource("grain", -10)
                end },
                base = 40 },
          },
        },
        { gua = "坤", symbol = "地", name = "地势坤",
          color = { 139, 90, 43, 255 },
          scene = "卦象示有贵人经过，可结善缘。\n如何待客？",
          choices = {
              { text = "盛情款待，倾囊设宴",
                good = { desc = "贵人感动，赠经史典籍一部！获得【经史典籍】", apply = function()
                    GameData.AddItem("book", 1)
                end },
                bad = { desc = "贵人乃骗子，席卷银两而去。银-20", apply = function()
                    GameData.AddResource("silver", -20)
                end },
                base = 60 },
              { text = "礼节周到，不卑不亢",
                good = { desc = "贵人赞许，赐银相助。银+30", apply = function()
                    GameData.AddResource("silver", 30)
                end },
                bad = { desc = "贵人觉得冷淡，匆匆离去。无事发生", apply = function() end },
                base = 70 },
          },
        },
        { gua = "震", symbol = "雷", name = "震为雷",
          color = { 100, 80, 200, 255 },
          scene = "卦象示变动之兆。一支流寇靠近村庄，\n如何应对？",
          choices = {
              { text = "组织族人奋起抵抗",
                good = { desc = "击退流寇，缴获兵器！获得【精钢兵器】，全族武艺+", apply = function()
                    GameData.AddItem("weapon", 1)
                    for _, m in ipairs(GameData.GetAliveMembers()) do
                        local g = GrowthSystem.DiminishedGain(m.martial, 2)
                        m.martial = math.min(100, m.martial + g)
                    end
                end },
                bad = { desc = "虽然击退，但族人受伤。全族健康-8", apply = function()
                    for _, m in ipairs(GameData.GetAliveMembers()) do m.health = math.max(1, m.health - 8) end
                end },
                base = 45 },
              { text = "以财消灾，赠粮退敌",
                good = { desc = "流寇退去，且传扬族长仁义。声望+15", apply = function()
                    GameData.AddResource("fame", 15)
                end },
                bad = { desc = "流寇得寸进尺，粮仓被洗劫。粮-30", apply = function()
                    GameData.AddResource("grain", -30)
                end },
                base = 55 },
          },
        },
        { gua = "巽", symbol = "风", name = "巽为风",
          color = { 60, 170, 120, 255 },
          scene = "卦象示远方有机缘。\n有商队途经，邀请族人同行。",
          choices = {
              { text = "派族人随行，远途经商",
                good = { desc = "满载而归！银+50，布匹+20", apply = function()
                    GameData.AddResource("silver", 50); GameData.AddResource("cloth", 20)
                end },
                bad = { desc = "途遇山贼，损失货物。布-10", apply = function()
                    GameData.AddResource("cloth", -10)
                end },
                base = 50 },
              { text = "婉拒邀请，在家深耕",
                good = { desc = "安心农事，五谷丰登。粮+35，全族健康+3", apply = function()
                    GameData.AddResource("grain", 35)
                    for _, m in ipairs(GameData.GetAliveMembers()) do m.health = math.min(100, m.health + 3) end
                end },
                bad = { desc = "平淡度日，无甚收获。", apply = function() end },
                base = 75 },
          },
        },
        { gua = "坎", symbol = "水", name = "坎为水",
          color = { 50, 120, 200, 255 },
          scene = "卦象示暗藏危机。\n族中有人染疫，恐波及全族。",
          choices = {
              { text = "重金请名医，全力救治",
                good = { desc = "名医妙手回春！治愈全族病患，获得【上等药材】", apply = function()
                    GameData.AddItem("herb", 2)
                    for _, m in ipairs(GameData.GetAliveMembers()) do
                        if m.state == "生病" then m.state = m.prevState or "在家"; m.prevState = nil; m.health = math.max(m.health, 60) end
                    end
                end },
                bad = { desc = "名医路途遥远，赶到时疫情已扩散。银-25", apply = function()
                    GameData.AddResource("silver", -25)
                end },
                base = 50 },
              { text = "隔离病患，以草药自救",
                good = { desc = "疫情控制住了，族人学会了医术。全族学识+", apply = function()
                    for _, m in ipairs(GameData.GetAliveMembers()) do
                        local g = GrowthSystem.DiminishedGain(m.study, 2)
                        m.study = math.min(100, m.study + g)
                    end
                end },
                bad = { desc = "隔离不力，更多人感染。全族健康-5", apply = function()
                    for _, m in ipairs(GameData.GetAliveMembers()) do m.health = math.max(1, m.health - 5) end
                end },
                base = 55 },
          },
        },
        { gua = "离", symbol = "火", name = "离为火",
          color = { 220, 80, 40, 255 },
          scene = "卦象示文昌高照。\n县令举办文会，各族可派人参加。",
          choices = {
              { text = "选派才学最高者赴会",
                good = { desc = "妙笔生花，拔得头筹！声望+15，获得【官府印信】", apply = function()
                    GameData.AddResource("fame", 15); GameData.AddItem("seal", 1)
                end },
                bad = { desc = "文章虽好，但得罪权贵。声望-5", apply = function()
                    GameData.AddResource("fame", -5)
                end },
                base = 50 },
              { text = "低调参与，重在交友",
                good = { desc = "结交文士，声名渐起。声望+8，全族学识+", apply = function()
                    GameData.AddResource("fame", 8)
                    for _, m in ipairs(GameData.GetAliveMembers()) do
                        local g = GrowthSystem.DiminishedGain(m.study, 1)
                        m.study = math.min(100, m.study + g)
                    end
                end },
                bad = { desc = "无人问津，略有遗憾。", apply = function() end },
                base = 65 },
          },
        },
        { gua = "艮", symbol = "山", name = "艮为山",
          color = { 100, 100, 100, 255 },
          scene = "卦象示守成之相。\n后山发现一处古墓入口。",
          choices = {
              { text = "组织族人探墓寻宝",
                good = { desc = "发现先人遗藏！获得【传家宝】，银+30", apply = function()
                    GameData.AddItem("heirloom", 1); GameData.AddResource("silver", 30)
                end },
                bad = { desc = "墓中机关重重，族人受伤。全族健康-6", apply = function()
                    for _, m in ipairs(GameData.GetAliveMembers()) do m.health = math.max(1, m.health - 6) end
                end },
                base = 35 },
              { text = "封土填穴，不扰先人",
                good = { desc = "积善之举，天道酬勤。声望+10，全族健康+5", apply = function()
                    GameData.AddResource("fame", 10)
                    for _, m in ipairs(GameData.GetAliveMembers()) do m.health = math.min(100, m.health + 5) end
                end },
                bad = { desc = "平静度日，无事发生。", apply = function() end },
                base = 80 },
          },
        },
        { gua = "兑", symbol = "泽", name = "兑为泽",
          color = { 80, 180, 220, 255 },
          scene = "卦象示喜悦之兆。\n邻族来使，邀请联合举办秋收庆典。",
          choices = {
              { text = "欣然应允，出资合办",
                good = { desc = "庆典盛况空前！声望+12，获得【玉石珍玩】", apply = function()
                    GameData.AddResource("fame", 12); GameData.AddItem("jade", 1)
                end },
                bad = { desc = "花费超支，略有亏损。银-15", apply = function()
                    GameData.AddResource("silver", -15)
                end },
                base = 60 },
              { text = "婉言谢绝，自家庆贺",
                good = { desc = "族人欢聚，和乐融融。全族健康+5，粮+15", apply = function()
                    for _, m in ipairs(GameData.GetAliveMembers()) do m.health = math.min(100, m.health + 5) end
                    GameData.AddResource("grain", 15)
                end },
                bad = { desc = "邻族不悦，关系疏远。声望-3", apply = function()
                    GameData.AddResource("fame", -3)
                end },
                base = 65 },
          },
        },
    }

    -- ========== Phase 1: 选择祭品 ==========
    local function showPhase1()
        local children = {
            UI.Panel {
                width = "100%", padding = 8, borderRadius = 6,
                backgroundGradient = { direction = "to-right", from = { 180, 140, 220, 40 }, to = { 0, 0, 0, 0 } },
                children = {
                    UI.Label { text = "第一步：备好祭品，焚香告天", fontSize = 14, fontColor = { 140, 100, 200, 255 }, fontWeight = "bold" },
                    UI.Label { text = "祭品越丰厚，卦象越灵验", fontSize = 12, fontColor = Theme.TEXT_MUTED, marginTop = 2 },
                },
            },
        }
        for _, t in ipairs(tiers) do
            local canAfford = s.silver >= t.silver and s.grain >= t.grain
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 10, borderRadius = 8,
                backgroundColor = canAfford and Theme.BG_CARD or { 230, 225, 218, 180 },
                borderWidth = 1, borderColor = canAfford and Theme.BORDER_GOLD or Theme.BORDER_LIGHT,
                gap = 4, opacity = canAfford and 1.0 or 0.5,
                onTap = canAfford and function(self)
                    AudioManager.Click()
                    GameData.SpendResources(t.silver, t.grain, 0, 0)
                    s.lastPrayMonth = s.totalMonths
                    showPhase2(t)
                end or nil,
                children = {
                    UI.Panel {
                        width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                        children = {
                            UI.Label { text = t.name, fontSize = 15, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                            UI.Panel {
                                flexDirection = "row", gap = 6,
                                children = {
                                    UI.Label { text = "银" .. t.silver, fontSize = 12, fontColor = Theme.GOLD_DARK },
                                    UI.Label { text = "粮" .. t.grain, fontSize = 12, fontColor = { 100, 160, 60, 255 } },
                                },
                            },
                        },
                    },
                    UI.Label { text = t.desc, fontSize = 12, fontColor = Theme.TEXT_MUTED },
                },
            }
        end
        children[#children + 1] = UI.Label { text = "每季（3个月）可祈福一次", fontSize = 11, fontColor = Theme.TEXT_MUTED, marginTop = 4 }
        modal:AddContent(UI.ScrollView {
            width = "100%", maxHeight = 450, scrollY = true, bounces = true, showScrollbar = true,
            children = { UI.Panel { width = "100%", padding = 8, gap = 8, children = children } },
        })
    end

    -- ========== Phase 2: 抽卦象（选一张） ==========
    function showPhase2(tier)
        modal:ClearContent()
        -- 随机抽3个不重复的卦象
        local pool = {}
        for i = 1, #allHexagrams do pool[i] = i end
        local drawn = {}
        for _ = 1, 3 do
            local idx = math.random(1, #pool)
            drawn[#drawn + 1] = allHexagrams[pool[idx]]
            table.remove(pool, idx)
        end

        modal.title = "祭天祈福 · 请卦"
        local children = {
            UI.Panel {
                width = "100%", padding = 8, borderRadius = 6,
                backgroundGradient = { direction = "to-right", from = { 180, 140, 220, 40 }, to = { 0, 0, 0, 0 } },
                children = {
                    UI.Label { text = "第二步：天降三卦，择其一而观之", fontSize = 14, fontColor = { 140, 100, 200, 255 }, fontWeight = "bold" },
                    UI.Label { text = "每卦蕴含不同机缘，慎重抉择", fontSize = 12, fontColor = Theme.TEXT_MUTED, marginTop = 2 },
                },
            },
        }

        for _, hex in ipairs(drawn) do
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 12, borderRadius = 10,
                backgroundColor = Theme.BG_CARD,
                borderWidth = 2, borderColor = hex.color,
                gap = 4,
                onTap = function(self) AudioManager.Click() showPhase3(tier, hex) end,
                children = {
                    UI.Panel {
                        width = "100%", flexDirection = "row", alignItems = "center", gap = 8,
                        children = {
                            UI.Panel {
                                width = 40, height = 40, borderRadius = 20,
                                backgroundColor = hex.color,
                                justifyContent = "center", alignItems = "center",
                                children = {
                                    UI.Label { text = hex.symbol, fontSize = 20, fontColor = { 255, 255, 255, 255 }, fontWeight = "bold" },
                                },
                            },
                            UI.Panel {
                                flexShrink = 1, gap = 2,
                                children = {
                                    UI.Label { text = hex.gua .. "卦 · " .. hex.name, fontSize = 16, fontColor = hex.color, fontWeight = "bold" },
                                    UI.Label { text = "点击翻开此卦", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                                },
                            },
                        },
                    },
                },
            }
        end
        modal:AddContent(UI.ScrollView {
            width = "100%", maxHeight = 450, scrollY = true, bounces = true, showScrollbar = true,
            children = { UI.Panel { width = "100%", padding = 8, gap = 10, children = children } },
        })
    end

    -- ========== Phase 3: 情境抉择 ==========
    function showPhase3(tier, hex)
        modal:ClearContent()
        modal.title = "祭天祈福 · " .. hex.gua .. "卦"
        local children = {
            -- 卦象标题
            UI.Panel {
                width = "100%", padding = 10, borderRadius = 8,
                backgroundColor = { hex.color[1], hex.color[2], hex.color[3], 30 },
                borderWidth = 1, borderColor = hex.color,
                alignItems = "center", gap = 4,
                children = {
                    UI.Label { text = hex.symbol, fontSize = 30, fontColor = hex.color },
                    UI.Label { text = hex.gua .. "卦 · " .. hex.name, fontSize = 18, fontColor = hex.color, fontWeight = "bold" },
                },
            },
            -- 情境描述
            UI.Panel {
                width = "100%", padding = 10, borderRadius = 6,
                backgroundColor = Theme.BG_WHITE,
                children = {
                    UI.Label { text = hex.scene, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, whiteSpace = "normal", lineHeight = 1.5 },
                },
            },
        }

        -- 两个选择
        for i, ch in ipairs(hex.choices) do
            local btnColors = i == 1
                and { bg = { 180, 140, 60, 255 }, border = { 200, 160, 40, 255 } }
                or  { bg = { 100, 140, 180, 255 }, border = { 80, 120, 180, 255 } }
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 10, borderRadius = 8,
                backgroundColor = btnColors.bg,
                borderWidth = 1, borderColor = btnColors.border,
                onTap = function(self) AudioManager.Click() showResult(tier, hex, ch) end,
                children = {
                    UI.Label { text = ch.text, fontSize = 15, fontColor = { 255, 255, 255, 255 }, fontWeight = "bold", whiteSpace = "normal" },
                },
            }
        end

        children[#children + 1] = UI.Label {
            text = "祭品：" .. tier.name .. "（灵验加成+" .. (tier.luck * 10) .. "%）",
            fontSize = 11, fontColor = Theme.TEXT_MUTED, textAlign = "center", width = "100%", marginTop = 2,
        }
        modal:AddContent(UI.ScrollView {
            width = "100%", maxHeight = 450, scrollY = true, bounces = true, showScrollbar = true,
            children = { UI.Panel { width = "100%", padding = 8, gap = 8, children = children } },
        })
    end

    -- ========== Phase 4: 展示结果 ==========
    function showResult(tier, hex, choice)
        modal:ClearContent()
        -- 计算成功率：基础概率 + 祭品加成（每级+10%）
        local chance = choice.base + tier.luck * 10
        local success = math.random(1, 100) <= chance
        local outcome = success and choice.good or choice.bad
        outcome.apply()
        local logText = "祭天祈福（" .. hex.gua .. "卦/" .. tier.name .. "）：" .. outcome.desc
        GameData.AddLog(logText)

        modal.title = "祭天祈福 · 天意"
        local resultColor = success and { 60, 160, 80, 255 } or { 200, 80, 60, 255 }
        local resultIcon = success and "吉" or "凶"
        local children = {
            UI.Panel {
                width = "100%", padding = 16, borderRadius = 10,
                backgroundColor = { resultColor[1], resultColor[2], resultColor[3], 25 },
                borderWidth = 2, borderColor = resultColor,
                alignItems = "center", gap = 8,
                children = {
                    UI.Label { text = resultIcon, fontSize = 38, fontColor = resultColor, fontWeight = "bold" },
                    UI.Label { text = hex.gua .. "卦 · " .. hex.name, fontSize = 16, fontColor = hex.color },
                    UI.Panel { width = "80%", height = 1, backgroundColor = { 200, 200, 200, 100 } },
                    UI.Label { text = outcome.desc, fontSize = 16, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold", textAlign = "center", whiteSpace = "normal" },
                },
            },
            UI.Panel {
                width = "100%", height = 36, borderRadius = 8, marginTop = 4,
                backgroundGradient = { direction = "to-right", from = { 140, 100, 200, 255 }, to = { 100, 60, 180, 255 } },
                justifyContent = "center", alignItems = "center",
                onTap = function(self)
                    AudioManager.Click()
                    modal:Close()
                    GameScreen.RefreshAll()
                end,
                children = { UI.Label { text = "天意已定", fontSize = 15, fontColor = { 255, 255, 255, 255 }, fontWeight = "bold" } },
            },
        }
        modal:AddContent(UI.ScrollView {
            width = "100%", maxHeight = 450, scrollY = true, bounces = true, showScrollbar = true,
            children = { UI.Panel { width = "100%", padding = 8, gap = 10, children = children } },
        })
    end

    showPhase1()
    modal:Open()
end

-- ============================================================================
-- 教坊司·花魁大赛（世家/品级5解锁）
-- 选派族中女子参赛，三轮竞技对决，争夺花魁桂冠
-- ============================================================================

function GameScreen.ShowCourtesanDialog()
    local s = GameData.state
    if not s then return end

    -- 冷却：每半年一次
    if s.lastCourtesanMonth and (s.totalMonths - s.lastCourtesanMonth) < 6 then
        local remain = 6 - (s.totalMonths - s.lastCourtesanMonth)
        GameScreen.ShowResultPopup("教坊司·花魁", "距下次花魁大赛尚需" .. remain .. "个月\n盛事不可仓促")
        return
    end

    local modal = UI.Modal { title = "教坊司 · 花魁大赛", size = "md" }
    local entryFee = 50 -- 报名费

    -- AI选手名字库
    local aiNames = {
        "柳如烟", "苏小小", "李师师", "陈圆圆", "董小宛",
        "顾横波", "卞玉京", "马湘兰", "寇白门", "柳如是",
        "赵飞燕", "花蕊夫人", "鱼玄机", "薛涛", "关盼盼",
    }

    -- 生成AI选手
    local function genAI()
        local name = aiNames[math.random(1, #aiNames)]
        return {
            name = name,
            study = 20 + math.random(0, 40),    -- 才学
            health = 50 + math.random(0, 40),    -- 仪态（用health代替）
            charm = 30 + math.random(0, 50),     -- 机敏（随机值）
            isAI = true,
        }
    end

    -- 比赛三个环节
    local rounds = {
        { name = "才艺比拼", attr = "study", icon = "文",
          desc = "琴棋书画，以才学高下论胜负",
          strategies = {
              { name = "稳扎稳打", desc = "发挥正常水平", bonus = 0, risk = 0 },
              { name = "出奇制胜", desc = "冒险创新，可能大成或大败", bonus = 15, risk = 15 },
              { name = "以柔克刚", desc = "柔美风格，加小幅分", bonus = 8, risk = 3 },
          },
        },
        { name = "仪态风姿", attr = "health", icon = "姿",
          desc = "身段容貌，仪态万千",
          strategies = {
              { name = "端庄大方", desc = "稳重得体，不出差错", bonus = 0, risk = 0 },
              { name = "艳压群芳", desc = "华服盛装，博取高分", bonus = 12, risk = 10 },
              { name = "清新脱俗", desc = "素雅出尘，别具一格", bonus = 8, risk = 5 },
          },
        },
        { name = "机智应对", attr = "charm", icon = "辩",
          desc = "名士出题，考验急智应变",
          strategies = {
              { name = "从容应答", desc = "镇定自若，中规中矩", bonus = 0, risk = 0 },
              { name = "妙语连珠", desc = "才思敏捷，语惊四座", bonus = 15, risk = 12 },
              { name = "含蓄婉约", desc = "言辞含蓄，留有余韵", bonus = 5, risk = 2 },
          },
        },
    }

    -- ========== Phase 1: 选择参赛族人 ==========
    local function showPhase1()
        -- 找出可参赛的女性族人（在家、年龄16-35）
        local candidates = {}
        for _, m in ipairs(GameData.GetAliveMembers()) do
            if m.gender == "female" and m.state == "在家" and m.age >= 16 and m.age <= 35 then
                candidates[#candidates + 1] = m
            end
        end

        local children = {
            UI.Panel {
                width = "100%", padding = 8, borderRadius = 6,
                backgroundGradient = { direction = "to-right", from = { 230, 120, 160, 40 }, to = { 0, 0, 0, 0 } },
                children = {
                    UI.Label { text = "教坊司举办花魁大赛，四方佳丽齐聚", fontSize = 14, fontColor = { 200, 100, 140, 255 }, fontWeight = "bold" },
                    UI.Label { text = "选派族中女子参赛，报名费 银" .. entryFee, fontSize = 12, fontColor = Theme.TEXT_MUTED, marginTop = 2 },
                },
            },
        }

        if #candidates == 0 then
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 16, borderRadius = 8,
                backgroundColor = Theme.BG_WHITE, alignItems = "center", gap = 6,
                children = {
                    UI.Label { text = "族中暂无适龄女子参赛", fontSize = 15, fontColor = Theme.TEXT_MUTED },
                    UI.Label { text = "（需16-35岁、状态为在家的女性族人）", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                },
            }
        else
            children[#children + 1] = UI.Label { text = "选择参赛者：", fontSize = 13, fontColor = Theme.TEXT_SECONDARY }
            for _, c in ipairs(candidates) do
                local canAfford = s.silver >= entryFee
                children[#children + 1] = UI.Panel {
                    width = "100%", padding = 10, borderRadius = 8,
                    backgroundColor = canAfford and Theme.BG_CARD or { 230, 225, 218, 180 },
                    borderWidth = 1, borderColor = canAfford and { 230, 120, 160, 200 } or Theme.BORDER_LIGHT,
                    opacity = canAfford and 1.0 or 0.5, gap = 4,
                    onTap = canAfford and function(self)
                        AudioManager.Click()
                        GameData.SpendResources(entryFee, 0, 0, 0)
                        s.lastCourtesanMonth = s.totalMonths
                        -- 构建参赛者数据
                        local player = {
                            name = c.name, study = c.study, health = c.health,
                            charm = math.floor((c.study + c.health) / 2) + math.random(-5, 10),
                            isAI = false, memberId = c.id,
                        }
                        -- 生成3个AI对手
                        local contestants = { player, genAI(), genAI(), genAI() }
                        showPhase2(player, contestants, 1, {})
                    end or nil,
                    children = {
                        UI.Panel {
                            width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                            children = {
                                UI.Label { text = c.name, fontSize = 15, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                                UI.Label { text = c.age .. "岁", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                            },
                        },
                        UI.Panel {
                            flexDirection = "row", gap = 8,
                            children = {
                                UI.Label { text = "才学" .. c.study, fontSize = 12, fontColor = { 100, 120, 200, 255 } },
                                UI.Label { text = "仪态" .. c.health, fontSize = 12, fontColor = { 200, 120, 100, 255 } },
                                c.talent and UI.Label { text = "天赋:" .. c.talent.name, fontSize = 11, fontColor = Theme.GOLD_DARK } or nil,
                            },
                        },
                    },
                }
            end
        end
        children[#children + 1] = UI.Label { text = "每半年（6个月）可参赛一次", fontSize = 11, fontColor = Theme.TEXT_MUTED, marginTop = 4 }
        modal:AddContent(UI.ScrollView {
            width = "100%", maxHeight = 380, scrollY = true, showScrollbar = true, bounces = true,
            children = { UI.Panel { width = "100%", padding = 8, gap = 8, children = children } },
        })
    end

    -- ========== Phase 2: 比赛环节（选策略） ==========
    function showPhase2(player, contestants, roundIdx, allScores)
        modal:ClearContent()
        if roundIdx > 3 then
            showResult(player, contestants, allScores)
            return
        end
        local round = rounds[roundIdx]
        modal.title = "花魁大赛 · " .. round.name .. "（第" .. roundIdx .. "/3轮）"

        -- 初始化本轮得分
        if not allScores[roundIdx] then allScores[roundIdx] = {} end

        local children = {
            UI.Panel {
                width = "100%", padding = 8, borderRadius = 6,
                backgroundGradient = { direction = "to-right", from = { 230, 120, 160, 40 }, to = { 0, 0, 0, 0 } },
                children = {
                    UI.Label { text = "第" .. roundIdx .. "轮：" .. round.name, fontSize = 16, fontColor = { 200, 100, 140, 255 }, fontWeight = "bold" },
                    UI.Label { text = round.desc, fontSize = 12, fontColor = Theme.TEXT_MUTED, marginTop = 2 },
                },
            },
            -- 选手状态
            UI.Label { text = "参赛选手", fontSize = 13, fontColor = Theme.TEXT_SECONDARY, marginTop = 4 },
        }

        -- 显示所有选手当前总分
        for i, c in ipairs(contestants) do
            local totalSoFar = 0
            for r = 1, roundIdx - 1 do
                totalSoFar = totalSoFar + (allScores[r][i] or 0)
            end
            local isPlayer = not c.isAI
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 6, borderRadius = 6,
                backgroundColor = isPlayer and { 230, 120, 160, 30 } or Theme.BG_WHITE,
                borderWidth = isPlayer and 1 or 0, borderColor = { 230, 120, 160, 150 },
                flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                children = {
                    UI.Panel {
                        flexDirection = "row", gap = 6, alignItems = "center",
                        children = {
                            UI.Label { text = isPlayer and "我方" or "对手", fontSize = 11,
                                fontColor = isPlayer and { 200, 100, 140, 255 } or Theme.TEXT_MUTED },
                            UI.Label { text = c.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = isPlayer and "bold" or "normal" },
                        },
                    },
                    UI.Panel {
                        flexDirection = "row", gap = 6,
                        children = {
                            UI.Label { text = round.icon .. c[round.attr], fontSize = 12, fontColor = { 100, 120, 200, 255 } },
                            roundIdx > 1 and UI.Label { text = "累计" .. totalSoFar, fontSize = 11, fontColor = Theme.GOLD_DARK } or nil,
                        },
                    },
                },
            }
        end

        -- 策略选择（仅玩家选择）
        children[#children + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 6 }
        children[#children + 1] = UI.Label { text = "选择策略：", fontSize = 14, fontColor = { 200, 100, 140, 255 }, fontWeight = "bold", marginTop = 4 }

        for _, strat in ipairs(round.strategies) do
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 10, borderRadius = 8,
                backgroundColor = Theme.BG_CARD,
                borderWidth = 1, borderColor = { 230, 120, 160, 150 },
                gap = 2,
                onTap = function(self)
                    AudioManager.Click()
                    -- 计算本轮所有人得分
                    for i, c in ipairs(contestants) do
                        local base = c[round.attr] or 30
                        local roll = math.random(-8, 8)
                        if not c.isAI then
                            -- 玩家：应用策略加成
                            local stratRoll = math.random(-strat.risk, strat.bonus)
                            allScores[roundIdx][i] = base + roll + stratRoll
                        else
                            -- AI：随机策略
                            allScores[roundIdx][i] = base + roll + math.random(-5, 8)
                        end
                    end
                    showRoundResult(player, contestants, roundIdx, allScores)
                end,
                children = {
                    UI.Label { text = strat.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                    UI.Label { text = strat.desc, fontSize = 12, fontColor = Theme.TEXT_MUTED },
                    UI.Panel {
                        flexDirection = "row", gap = 8,
                        children = (function()
                            local tags = {}
                            if strat.bonus > 0 then tags[#tags+1] = UI.Label { text = "加成+" .. strat.bonus, fontSize = 11, fontColor = Theme.GREEN } end
                            if strat.risk > 0 then tags[#tags+1] = UI.Label { text = "风险-" .. strat.risk, fontSize = 11, fontColor = Theme.RED } end
                            if strat.bonus == 0 and strat.risk == 0 then tags[#tags+1] = UI.Label { text = "稳定发挥", fontSize = 11, fontColor = Theme.TEXT_MUTED } end
                            return tags
                        end)(),
                    },
                },
            }
        end
        modal:AddContent(UI.ScrollView {
            width = "100%", maxHeight = 420, scrollY = true, showScrollbar = true, bounces = true,
            children = { UI.Panel { width = "100%", padding = 8, gap = 6, children = children } },
        })
    end

    -- ========== 单轮结果展示 ==========
    function showRoundResult(player, contestants, roundIdx, allScores)
        modal:ClearContent()
        local round = rounds[roundIdx]
        modal.title = "花魁大赛 · " .. round.name .. " 结果"

        -- 本轮排名
        local rankList = {}
        for i, c in ipairs(contestants) do
            rankList[#rankList + 1] = { idx = i, name = c.name, score = allScores[roundIdx][i], isAI = c.isAI }
        end
        table.sort(rankList, function(a, b) return a.score > b.score end)

        local children = {
            UI.Panel {
                width = "100%", padding = 8, borderRadius = 6,
                backgroundColor = { 230, 120, 160, 25 },
                alignItems = "center",
                children = {
                    UI.Label { text = round.name .. " · 得分揭晓", fontSize = 16, fontColor = { 200, 100, 140, 255 }, fontWeight = "bold" },
                },
            },
        }

        local rankIcons = { "冠", "亚", "季", "殿" }
        for rank, entry in ipairs(rankList) do
            local isPlayer = not entry.isAI
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 8, borderRadius = 6,
                backgroundColor = isPlayer and { 255, 240, 245, 255 } or Theme.BG_WHITE,
                borderWidth = isPlayer and 1 or 0, borderColor = { 230, 120, 160, 200 },
                flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                children = {
                    UI.Panel {
                        flexDirection = "row", gap = 6, alignItems = "center",
                        children = {
                            UI.Label { text = rankIcons[rank] or rank, fontSize = 16,
                                fontColor = rank == 1 and Theme.GOLD or (rank == 2 and Theme.SILVER_COLOR or Theme.TEXT_MUTED),
                                fontWeight = "bold" },
                            UI.Label { text = entry.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = isPlayer and "bold" or "normal" },
                            isPlayer and UI.Label { text = "(我方)", fontSize = 11, fontColor = { 200, 100, 140, 255 } } or nil,
                        },
                    },
                    UI.Label { text = entry.score .. "分", fontSize = 14, fontColor = Theme.GOLD_DARK, fontWeight = "bold" },
                },
            }
        end

        -- 继续下一轮或查看最终结果
        local btnText = roundIdx < 3 and ("进入第" .. (roundIdx + 1) .. "轮：" .. rounds[roundIdx + 1].name) or "查看最终排名"
        children[#children + 1] = UI.Panel {
            width = "100%", height = 36, borderRadius = 8, marginTop = 8,
            backgroundGradient = { direction = "to-right", from = { 230, 120, 160, 255 }, to = { 200, 80, 140, 255 } },
            justifyContent = "center", alignItems = "center",
            onTap = function(self)
                AudioManager.Click()
                showPhase2(player, contestants, roundIdx + 1, allScores)
            end,
            children = { UI.Label { text = btnText, fontSize = 15, fontColor = { 255, 255, 255, 255 }, fontWeight = "bold" } },
        }
        modal:AddContent(UI.ScrollView {
            width = "100%", maxHeight = 400, scrollY = true, showScrollbar = true, bounces = true,
            children = { UI.Panel { width = "100%", padding = 8, gap = 6, children = children } },
        })
    end

    -- ========== 最终结果 ==========
    function showResult(player, contestants, allScores)
        modal:ClearContent()
        modal.title = "花魁大赛 · 最终结果"

        -- 计算总分
        local totals = {}
        for i, c in ipairs(contestants) do
            local total = 0
            for r = 1, 3 do total = total + (allScores[r] and allScores[r][i] or 0) end
            totals[#totals + 1] = { idx = i, name = c.name, score = total, isAI = c.isAI }
        end
        table.sort(totals, function(a, b) return a.score > b.score end)

        -- 确定玩家排名
        local playerRank = 4
        for rank, entry in ipairs(totals) do
            if not entry.isAI then playerRank = rank; break end
        end

        -- 奖励配置
        local rewards = {
            [1] = { title = "花魁桂冠", desc = "银+80，获得【玉石珍玩】、【经史典籍】，参赛者学识+8",
                color = { 220, 180, 50, 255 },
                apply = function()
                    GameData.AddResource("silver", 80)
                    GameData.AddItem("jade", 1); GameData.AddItem("book", 1)
                    for _, m in ipairs(GameData.GetAliveMembers()) do
                        if m.id == player.memberId then
                            local g = GrowthSystem.DiminishedGain(m.study, 8)  -- 原+15
                            m.study = math.min(100, m.study + g); break
                        end
                    end
                end },
            [2] = { title = "榜眼佳人", desc = "银+40，获得【经史典籍】，参赛者学识+5",
                color = { 180, 180, 200, 255 },
                apply = function()
                    GameData.AddResource("silver", 40); GameData.AddItem("book", 1)
                    for _, m in ipairs(GameData.GetAliveMembers()) do
                        if m.id == player.memberId then
                            local g = GrowthSystem.DiminishedGain(m.study, 5)  -- 原+8
                            m.study = math.min(100, m.study + g); break
                        end
                    end
                end },
            [3] = { title = "探花风采", desc = "银+20，参赛者学识+3",
                color = { 180, 120, 80, 255 },
                apply = function()
                    GameData.AddResource("silver", 20)
                    for _, m in ipairs(GameData.GetAliveMembers()) do
                        if m.id == player.memberId then
                            local g = GrowthSystem.DiminishedGain(m.study, 3)  -- 原+5
                            m.study = math.min(100, m.study + g); break
                        end
                    end
                end },
            [4] = { title = "参赛留念", desc = "虽未夺魁，但长了见识。参赛者学识+2",
                color = Theme.TEXT_MUTED,
                apply = function()
                    for _, m in ipairs(GameData.GetAliveMembers()) do
                        if m.id == player.memberId then
                            local g = GrowthSystem.DiminishedGain(m.study, 2)  -- 原+3
                            m.study = math.min(100, m.study + g); break
                        end
                    end
                end },
        }
        local reward = rewards[playerRank]
        reward.apply()
        GameData.AddLog("花魁大赛：" .. player.name .. "荣获" .. reward.title .. " - " .. reward.desc)

        local rankIcons = { "冠", "亚", "季", "殿" }
        local children = {
            UI.Panel {
                width = "100%", padding = 12, borderRadius = 10,
                backgroundColor = { reward.color[1], reward.color[2], reward.color[3], 30 },
                borderWidth = 2, borderColor = reward.color,
                alignItems = "center", gap = 6,
                children = {
                    UI.Label { text = playerRank == 1 and "夺魁" or rankIcons[playerRank], fontSize = 34, fontColor = reward.color, fontWeight = "bold" },
                    UI.Label { text = player.name .. " · " .. reward.title, fontSize = 18, fontColor = reward.color, fontWeight = "bold" },
                    UI.Panel { width = "80%", height = 1, backgroundColor = { 200, 200, 200, 100 } },
                    UI.Label { text = reward.desc, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, textAlign = "center", whiteSpace = "normal" },
                },
            },
            UI.Label { text = "最终排名", fontSize = 14, fontColor = Theme.TEXT_SECONDARY, marginTop = 6 },
        }
        for rank, entry in ipairs(totals) do
            local isPlayer = not entry.isAI
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 6, borderRadius = 6,
                backgroundColor = isPlayer and { 255, 240, 245, 255 } or Theme.BG_WHITE,
                flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                children = {
                    UI.Panel {
                        flexDirection = "row", gap = 6, alignItems = "center",
                        children = {
                            UI.Label { text = rankIcons[rank], fontSize = 15,
                                fontColor = rank == 1 and Theme.GOLD or Theme.TEXT_MUTED, fontWeight = "bold" },
                            UI.Label { text = entry.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY },
                        },
                    },
                    UI.Label { text = "总分 " .. entry.score, fontSize = 13, fontColor = Theme.GOLD_DARK },
                },
            }
        end
        children[#children + 1] = UI.Panel {
            width = "100%", height = 36, borderRadius = 8, marginTop = 8,
            backgroundGradient = { direction = "to-right", from = { 230, 120, 160, 255 }, to = { 200, 80, 140, 255 } },
            justifyContent = "center", alignItems = "center",
            onTap = function(self) AudioManager.Click() modal:Close(); GameScreen.RefreshAll() end,
            children = { UI.Label { text = "曲终人散", fontSize = 15, fontColor = { 255, 255, 255, 255 }, fontWeight = "bold" } },
        }
        modal:AddContent(UI.ScrollView {
            width = "100%", maxHeight = 420, scrollY = true, showScrollbar = true, bounces = true,
            children = { UI.Panel { width = "100%", padding = 8, gap = 6, children = children } },
        })
    end

    showPhase1()
    modal:Open()
end

-- ============================================================================
-- 天子诰封对话框（勋贵/品级6解锁）
-- 朝廷差事任务链：接受朝廷委派，完成差事换取皇恩
-- ============================================================================

function GameScreen.ShowImperialSealDialog()
    local s = GameData.state
    if not s then return end

    -- 初始化皇恩值
    if not s.imperialFavor then s.imperialFavor = 0 end

    -- 冷却：每6个月一次（比之前12个月缩短，因为现在需要积累）
    if s.lastSealMonth and (s.totalMonths - s.lastSealMonth) < 6 then
        local remain = 6 - (s.totalMonths - s.lastSealMonth)
        GameScreen.ShowResultPopup("天子诰封", "距下次朝廷差事尚需" .. remain .. "个月\n圣意已达，不可频求")
        return
    end

    local modal = UI.Modal { title = "天子诰封", size = "md" }

    -- 皇恩封赏阶梯（累计皇恩值兑换永久奖励）
    local sealTiers = {
        { favor = 30,  name = "义民旌表", desc = "赐匾入祠，乡里敬仰",
          reward = "银+50，全族健康+3", claimed = false,
          apply = function()
              GameData.AddResource("silver", 50)
              for _, m in ipairs(GameData.GetAliveMembers()) do
                  m.health = math.min(100, m.health + GrowthSystem.DiminishedGain(m.health, 3))
              end
          end },
        { favor = 80,  name = "乡贤敕封", desc = "敕封乡贤，建坊立碑",
          reward = "银+100，获得【官府印信】×2，全族学识+2", claimed = false,
          apply = function()
              GameData.AddResource("silver", 100); GameData.AddItem("seal", 2)
              for _, m in ipairs(GameData.GetAliveMembers()) do
                  local gain = GrowthSystem.DiminishedGain(m.study, 2)
                  m.study = math.min(100, m.study + gain)
              end
          end },
        { favor = 150, name = "恩荣诰命", desc = "诰命加身，荫及子孙",
          reward = "银+200，获得【传家宝】，全族属性+4", claimed = false,
          apply = function()
              GameData.AddResource("silver", 200); GameData.AddItem("heirloom", 1)
              for _, m in ipairs(GameData.GetAliveMembers()) do
                  m.study = math.min(100, m.study + GrowthSystem.DiminishedGain(m.study, 4))
                  m.martial = math.min(100, m.martial + GrowthSystem.DiminishedGain(m.martial, 4))
                  m.health = math.min(100, m.health + 4)
              end
          end },
        { favor = 250, name = "世袭罔替", desc = "世代承袭，与国同休",
          reward = "银+500，获得【传家宝】×2、【兵法残卷】×2，全族属性+6", claimed = false,
          apply = function()
              GameData.AddResource("silver", 500)
              GameData.AddItem("heirloom", 2); GameData.AddItem("scroll", 2)
              for _, m in ipairs(GameData.GetAliveMembers()) do
                  m.study = math.min(100, m.study + GrowthSystem.DiminishedGain(m.study, 6))
                  m.martial = math.min(100, m.martial + GrowthSystem.DiminishedGain(m.martial, 6))
                  m.health = math.min(100, m.health + 6)
              end
          end },
    }

    -- 检查已领取状态
    if not s.claimedSeals then s.claimedSeals = {} end
    for _, tier in ipairs(sealTiers) do
        tier.claimed = s.claimedSeals[tier.name] == true
    end

    -- 差事任务池（根据当前资源动态生成可完成的任务）
    local allMissions = {
        { name = "运粮赈灾", desc = "北方水患，朝廷急调粮草赈济灾民",
          require = "粮食≥60", check = function() return s.grain >= 60 end,
          cost = function() GameData.SpendResources(0, 60, 0, 0) end,
          favor = 12, bonusDesc = "银+25，声望+8",
          bonus = function() GameData.AddResource("silver", 25); GameData.AddResource("fame", 8) end },
        { name = "筹银修城", desc = "边关城墙年久失修，朝廷令各族捐资",
          require = "银两≥80", check = function() return s.silver >= 80 end,
          cost = function() GameData.SpendResources(80, 0, 0, 0) end,
          favor = 15, bonusDesc = "粮+40，声望+10",
          bonus = function() GameData.AddResource("grain", 40); GameData.AddResource("fame", 10) end },
        { name = "献布纳贡", desc = "宫廷大典在即，征调各地精美布匹",
          require = "布匹≥30", check = function() return s.cloth >= 30 end,
          cost = function() GameData.SpendResources(0, 0, 30, 0) end,
          favor = 10, bonusDesc = "银+35，获得【玉石珍玩】",
          bonus = function() GameData.AddResource("silver", 35); GameData.AddItem("jade", 1) end },
        { name = "练兵报国", desc = "朝廷遴选壮士充实禁军",
          require = "族中有武艺≥40的族人3名",
          check = function()
              local count = 0
              for _, m in ipairs(GameData.GetAliveMembers()) do
                  if m.martial >= 40 then count = count + 1 end
              end
              return count >= 3
          end,
          cost = function()
              -- 武艺最高的3人各消耗10武艺
              local sorted = {}
              for _, m in ipairs(GameData.GetAliveMembers()) do
                  if m.martial >= 40 then sorted[#sorted + 1] = m end
              end
              table.sort(sorted, function(a, b) return a.martial > b.martial end)
              for i = 1, math.min(3, #sorted) do sorted[i].martial = sorted[i].martial - 10 end
          end,
          favor = 18, bonusDesc = "银+40，获得【精钢兵器】、【兵法残卷】",
          bonus = function()
              GameData.AddResource("silver", 40)
              GameData.AddItem("weapon", 1); GameData.AddItem("scroll", 1)
          end },
        { name = "呈献才子", desc = "翰林院选拔文才，荐举各地饱学之士",
          require = "族中有学识≥50的族人2名",
          check = function()
              local count = 0
              for _, m in ipairs(GameData.GetAliveMembers()) do
                  if m.study >= 50 then count = count + 1 end
              end
              return count >= 2
          end,
          cost = function()
              local sorted = {}
              for _, m in ipairs(GameData.GetAliveMembers()) do
                  if m.study >= 50 then sorted[#sorted + 1] = m end
              end
              table.sort(sorted, function(a, b) return a.study > b.study end)
              for i = 1, math.min(2, #sorted) do sorted[i].study = sorted[i].study - 8 end
          end,
          favor = 16, bonusDesc = "银+30，获得【经史典籍】×2",
          bonus = function()
              GameData.AddResource("silver", 30); GameData.AddItem("book", 2)
          end },
        { name = "供奉药材", desc = "太医院急需各地药材入库",
          require = "拥有上等药材≥2",
          check = function() return GameData.GetItemCount("herb") >= 2 end,
          cost = function() GameData.AddItem("herb", -2) end,
          favor = 14, bonusDesc = "银+50，全族健康+4",
          bonus = function()
              GameData.AddResource("silver", 50)
              for _, m in ipairs(GameData.GetAliveMembers()) do
                  m.health = math.min(100, m.health + GrowthSystem.DiminishedGain(m.health, 4))
              end
          end },
        { name = "捐资修庙", desc = "皇帝下旨修缮京师大庙，广征善款",
          require = "银两≥50且粮食≥30", check = function() return s.silver >= 50 and s.grain >= 30 end,
          cost = function() GameData.SpendResources(50, 30, 0, 0) end,
          favor = 13, bonusDesc = "声望+12，全族学识+1",
          bonus = function()
              GameData.AddResource("fame", 12)
              for _, m in ipairs(GameData.GetAliveMembers()) do
                  m.study = math.min(100, m.study + GrowthSystem.DiminishedGain(m.study, 1))
              end
          end },
    }

    -- 根据月份种子选3个不重复的任务
    local seed = s.totalMonths * 7 + 13
    local shuffled = {}
    for i = 1, #allMissions do shuffled[i] = i end
    -- 简单洗牌（基于种子）
    for i = #shuffled, 2, -1 do
        seed = (seed * 1103515245 + 12345) % 2147483648
        local j = (seed % i) + 1
        shuffled[i], shuffled[j] = shuffled[j], shuffled[i]
    end
    local missions = {}
    for i = 1, math.min(3, #shuffled) do
        missions[#missions + 1] = allMissions[shuffled[i]]
    end

    -- ========== 主界面 ==========
    local function showMain()
        local children = {}

        -- 皇恩进度条
        local nextTier = nil
        for _, tier in ipairs(sealTiers) do
            if not tier.claimed then nextTier = tier; break end
        end
        children[#children + 1] = UI.Panel {
            width = "100%", padding = 10, borderRadius = 8,
            backgroundGradient = { direction = "to-right", from = { 220, 180, 50, 40 }, to = { 180, 140, 30, 20 } },
            gap = 4,
            children = {
                UI.Panel {
                    width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                    children = {
                        UI.Label { text = "皇恩值", fontSize = 14, fontColor = Theme.GOLD, fontWeight = "bold" },
                        UI.Label { text = s.imperialFavor .. (nextTier and ("/" .. nextTier.favor) or " (已满)"),
                            fontSize = 16, fontColor = Theme.GOLD, fontWeight = "bold" },
                    },
                },
                -- 进度条
                UI.Panel {
                    width = "100%", height = 8, borderRadius = 4,
                    backgroundColor = { 200, 180, 120, 80 },
                    children = {
                        UI.Panel {
                            width = nextTier and (math.min(100, math.floor(s.imperialFavor / nextTier.favor * 100)) .. "%") or "100%",
                            height = "100%", borderRadius = 4,
                            backgroundGradient = { direction = "to-right", from = { 220, 180, 50, 255 }, to = { 200, 140, 30, 255 } },
                        },
                    },
                },
                nextTier and UI.Label { text = "下一封赏：" .. nextTier.name .. "（需" .. nextTier.favor .. "）", fontSize = 11, fontColor = Theme.TEXT_MUTED }
                    or UI.Label { text = "已获最高封赏！", fontSize = 11, fontColor = Theme.GOLD },
            },
        }

        -- 可领取的封赏
        local hasClaimable = false
        for _, tier in ipairs(sealTiers) do
            if not tier.claimed and s.imperialFavor >= tier.favor then
                hasClaimable = true
                children[#children + 1] = UI.Panel {
                    width = "100%", padding = 10, borderRadius = 8,
                    borderWidth = 2, borderColor = { 220, 180, 50, 255 },
                    backgroundGradient = { direction = "to-right", from = { 255, 245, 200, 255 }, to = { 255, 235, 180, 255 } },
                    gap = 4,
                    children = {
                        UI.Panel {
                            width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                            children = {
                                UI.Label { text = "可领取：" .. tier.name, fontSize = 15, fontColor = Theme.GOLD, fontWeight = "bold" },
                                UI.Label { text = "皇恩" .. tier.favor, fontSize = 12, fontColor = Theme.GOLD_DARK },
                            },
                        },
                        UI.Label { text = tier.desc, fontSize = 12, fontColor = Theme.TEXT_SECONDARY },
                        UI.Label { text = "奖励：" .. tier.reward, fontSize = 12, fontColor = { 180, 120, 30, 255 }, fontWeight = "bold" },
                        UI.Panel {
                            width = "100%", height = 32, borderRadius = 6, marginTop = 2,
                            backgroundGradient = { direction = "to-right", from = { 220, 180, 50, 255 }, to = { 200, 140, 30, 255 } },
                            justifyContent = "center", alignItems = "center",
                            onTap = function(self)
                                AudioManager.Click()
                                tier.apply()
                                s.claimedSeals[tier.name] = true
                                tier.claimed = true
                                GameData.AddLog("天子诰封：荣获" .. tier.name .. " - " .. tier.reward)
                                GameScreen.ShowResultPopup("天子诰封 · " .. tier.name, tier.desc .. "\n\n" .. tier.reward)
                                GameScreen.RefreshAll()
                            end,
                            children = { UI.Label { text = "领取封赏", fontSize = 14, fontColor = { 255, 255, 255, 255 }, fontWeight = "bold" } },
                        },
                    },
                }
                break  -- 只显示第一个可领取的
            end
        end

        -- 差事列表
        children[#children + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = hasClaimable and 2 or 6 }
        children[#children + 1] = UI.Panel {
            width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
            children = {
                UI.Label { text = "朝廷差事", fontSize = 15, fontColor = Theme.GOLD, fontWeight = "bold" },
                UI.Label { text = "完成差事积累皇恩", fontSize = 11, fontColor = Theme.TEXT_MUTED },
            },
        }

        for _, mission in ipairs(missions) do
            local canDo = mission.check()
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 10, borderRadius = 8,
                backgroundColor = canDo and Theme.BG_CARD or { 230, 225, 218, 180 },
                borderWidth = 1, borderColor = canDo and { 220, 180, 50, 150 } or Theme.BORDER_LIGHT,
                gap = 4, opacity = canDo and 1.0 or 0.6,
                children = {
                    UI.Panel {
                        width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                        children = {
                            UI.Label { text = mission.name, fontSize = 15, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                            UI.Label { text = "皇恩+" .. mission.favor, fontSize = 13, fontColor = Theme.GOLD, fontWeight = "bold" },
                        },
                    },
                    UI.Label { text = mission.desc, fontSize = 12, fontColor = Theme.TEXT_MUTED, whiteSpace = "normal" },
                    UI.Panel {
                        width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                        children = {
                            UI.Label { text = "需：" .. mission.require, fontSize = 12, fontColor = canDo and Theme.GREEN or Theme.RED },
                            UI.Label { text = "得：" .. mission.bonusDesc, fontSize = 11, fontColor = Theme.GOLD_DARK },
                        },
                    },
                    canDo and UI.Panel {
                        width = "100%", height = 30, borderRadius = 6, marginTop = 2,
                        backgroundGradient = { direction = "to-right", from = { 180, 140, 40, 255 }, to = { 160, 120, 20, 255 } },
                        justifyContent = "center", alignItems = "center",
                        onTap = function(self)
                            AudioManager.Click()
                            mission.cost()
                            mission.bonus()
                            s.imperialFavor = s.imperialFavor + mission.favor
                            s.lastSealMonth = s.totalMonths
                            GameData.AddLog("天子诰封·" .. mission.name .. "：皇恩+" .. mission.favor .. "，" .. mission.bonusDesc)
                            modal:Close()
                            GameScreen.ShowResultPopup("差事完成 · " .. mission.name,
                                "皇恩+" .. mission.favor .. "\n" .. mission.bonusDesc .. "\n\n累计皇恩：" .. s.imperialFavor)
                            GameScreen.RefreshAll()
                        end,
                        children = { UI.Label { text = "承办差事", fontSize = 14, fontColor = { 255, 255, 255, 255 }, fontWeight = "bold" } },
                    } or UI.Label { text = "条件不足", fontSize = 12, fontColor = Theme.RED, textAlign = "center", width = "100%", marginTop = 2 },
                },
            }
        end

        -- 封赏进度一览
        children[#children + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 4 }
        children[#children + 1] = UI.Label { text = "封赏阶梯", fontSize = 13, fontColor = Theme.TEXT_SECONDARY }
        for _, tier in ipairs(sealTiers) do
            local reached = s.imperialFavor >= tier.favor
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 6, borderRadius = 4,
                backgroundColor = tier.claimed and { 240, 235, 220, 255 } or Theme.BG_WHITE,
                flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                children = {
                    UI.Panel {
                        flexDirection = "row", gap = 4, alignItems = "center",
                        children = {
                            UI.Label { text = tier.claimed and "已领" or (reached and "可领" or "未达"),
                                fontSize = 11, fontColor = tier.claimed and Theme.GREEN or (reached and Theme.GOLD or Theme.TEXT_MUTED) },
                            UI.Label { text = tier.name, fontSize = 13,
                                fontColor = tier.claimed and Theme.TEXT_MUTED or (reached and Theme.GOLD or Theme.TEXT_PRIMARY) },
                        },
                    },
                    UI.Label { text = "皇恩" .. tier.favor, fontSize = 11, fontColor = Theme.TEXT_MUTED },
                },
            }
        end

        children[#children + 1] = UI.Label { text = "每半年（6个月）可承办一次差事", fontSize = 11, fontColor = Theme.TEXT_MUTED, marginTop = 4 }

        modal:AddContent(UI.ScrollView {
            width = "100%", maxHeight = 440, scrollY = true, showScrollbar = true, bounces = true,
            children = { UI.Panel { width = "100%", padding = 8, gap = 6, children = children } },
        })
    end

    showMain()
    modal:Open()
end

-- ============================================================================
-- 弹窗：族长继承
-- ============================================================================

function GameScreen.ShowSuccessionDialog(deadPatriarchId)
    local candidates = GameData.GetSuccessionCandidates(deadPatriarchId)
    local deadP = GameData.GetMember(deadPatriarchId)
    local deadName = deadP and deadP.name or "族长"

    -- 如果在弹窗前候选人已不足（极端情况），自动选第一个
    if #candidates == 0 then
        GameScreen.ShowPendingEventPopup()
        return
    end
    if #candidates == 1 then
        GameData.SetPatriarch(candidates[1].id)
        GameScreen.ShowPendingEventPopup()
        return
    end

    local modal = UI.Modal {
        title = "族长继承",
        size = "md", showCloseButton = false, closeOnOverlay = false,
    }

    -- 渲染候选人列表
    local function renderCandidates()
        local children = {}

        -- 提示文字
        children[#children + 1] = UI.Label {
            text = deadName .. "已故，请选择新任族长。",
            fontSize = 14, fontColor = Theme.TEXT_PRIMARY, textAlign = "center",
            whiteSpace = "normal",
        }
        children[#children + 1] = UI.Label {
            text = "年满16岁的族人方可继承族长之位",
            fontSize = 12, fontColor = Theme.TEXT_MUTED, textAlign = "center",
        }
        children[#children + 1] = UI.Panel { width = "80%", height = 1, backgroundColor = Theme.BORDER_GOLD, alignSelf = "center" }

        -- 最多显示6个候选人
        local showCount = math.min(#candidates, 6)
        for i = 1, showCount do
            local c = candidates[i]
            local genderText = c.gender == "male" and "男" or "女"
            local isChild = c.parentId == deadPatriarchId
            local relationTag = isChild and "子嗣" or "族人"
            local relationColor = isChild and Theme.GOLD or Theme.TEXT_MUTED

            -- 天赋
            local talentText = ""
            if c.talent then
                talentText = "【" .. c.talent.name .. "】"
            end

            local card = UI.Panel {
                width = "100%", padding = 10, borderRadius = 8,
                backgroundColor = Theme.BG_INPUT,
                borderWidth = 1, borderColor = Theme.BORDER_LIGHT,
                gap = 6,
                onTap = function(self)
                    AudioManager.Select()
                    GameData.SetPatriarch(c.id)
                    modal:Close()
                    -- 继续处理其他 pending 事件
                    GameScreen.ShowPendingEventPopup()
                end,
                children = {
                    -- 第一行：姓名 + 标签
                    UI.Panel {
                        flexDirection = "row", justifyContent = "space-between", alignItems = "center", width = "100%",
                        children = {
                            UI.Panel {
                                flexDirection = "row", gap = 4, alignItems = "center",
                                children = {
                                    UI.Label { text = c.name, fontSize = 16, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                                    UI.Label { text = genderText .. " " .. c.age .. "岁",
                                        fontSize = 13, fontColor = Theme.TEXT_MUTED },
                                    UI.Label {
                                        text = relationTag, fontSize = 11,
                                        fontColor = { 255, 255, 255, 255 },
                                        backgroundColor = relationColor,
                                        paddingLeft = 4, paddingRight = 4, paddingTop = 1, paddingBottom = 1,
                                        borderRadius = 3,
                                    },
                                },
                            },
                            UI.Label { text = c.identity .. (talentText ~= "" and " " .. talentText or ""),
                                fontSize = 12, fontColor = Theme.GOLD },
                        },
                    },
                    -- 属性条
                    StatBar("学识", c.study),
                    StatBar("武艺", c.martial),
                    StatBar("健康", c.health),
                    -- 提示
                    UI.Label { text = "点击选择此人为新族长", fontSize = 11, fontColor = Theme.TEXT_MUTED, textAlign = "center" },
                },
            }

            children[#children + 1] = card
        end

        if #candidates > showCount then
            children[#children + 1] = UI.Label {
                text = "（还有" .. (#candidates - showCount) .. "名候选人未显示）",
                fontSize = 12, fontColor = Theme.TEXT_MUTED, textAlign = "center",
            }
        end

        modal:AddContent(UI.ScrollView {
            width = "100%", maxHeight = 420, scrollY = true, showScrollbar = true, bounces = true,
            children = {
                UI.Panel { width = "100%", padding = 8, gap = 8, alignItems = "center", children = children },
            },
        })
    end

    renderCandidates()
    modal:Open()
end

-- ============================================================================
-- 弹窗：医馆（主动治疗生病族人）
-- ============================================================================

function GameScreen.ShowClinicDialog()
    local s = GameData.state

    -- 治疗费用：基础 8 两，品级越高费用越高（名医坐诊）
    local baseCost = 8
    local costPerRank = { [1] = 8, [2] = 10, [3] = 12, [4] = 15, [5] = 20, [6] = 25, [7] = 30 }
    local healCost = costPerRank[s.clanRank] or baseCost
    -- 治疗效果：恢复 25~40 点健康
    local healMin, healMax = 25, 40

    local modal = UI.Modal {
        size = "md",
        showCloseButton = false,
        closeOnOverlay = true,
    }
    modal:AddContent(CreateLeftCloseHeader(modal, "医馆 · 问诊"))

    local function renderContent()
        local children = {}

        -- 说明区
        children[#children + 1] = UI.Panel {
            width = "100%", padding = 8, borderRadius = 8,
            backgroundColor = Theme.BG_INPUT, gap = 4,
            children = {
                UI.Label { text = "延请郎中，诊治族人", fontSize = 15, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                UI.Label { text = "诊金 " .. healCost .. " 两/人 · 恢复健康 " .. healMin .. "~" .. healMax, fontSize = 13, fontColor = Theme.TEXT_SECONDARY },
                UI.Label { text = "当前银两：" .. s.silver .. " 两", fontSize = 13, fontColor = s.silver >= healCost and Theme.GOLD_DARK or Theme.RED },
            },
        }

        -- 收集生病或低健康的族人
        local sickMembers = {}
        for _, m in ipairs(GameData.GetAliveMembers()) do
            if m.state == "生病" or m.health <= 40 then
                sickMembers[#sickMembers + 1] = m
            end
        end

        if #sickMembers == 0 then
            -- 无人需要治疗
            children[#children + 1] = UI.Panel {
                width = "100%", paddingTop = 30, paddingBottom = 30,
                justifyContent = "center", alignItems = "center", gap = 6,
                children = {
                    UI.Label { text = "阖族安康", fontSize = 18, fontColor = Theme.GREEN, fontWeight = "bold" },
                    UI.Label { text = "当前无人需要诊治", fontSize = 14, fontColor = Theme.TEXT_MUTED },
                },
            }
        else
            -- 全部治疗按钮
            if #sickMembers > 1 then
                local totalCost = healCost * #sickMembers
                local canHealAll = s.silver >= totalCost
                children[#children + 1] = UI.Panel {
                    width = "100%", height = 36, borderRadius = 8, marginTop = 6,
                    backgroundGradient = canHealAll
                        and { direction = "to-right", from = { 60, 160, 120, 255 }, to = { 40, 140, 100, 255 } }
                        or nil,
                    backgroundColor = (not canHealAll) and Theme.BG_INPUT or nil,
                    justifyContent = "center", alignItems = "center",
                    onTap = canHealAll and function(self)
                        AudioManager.Click()
                        local msgs = {}
                        for _, m in ipairs(sickMembers) do
                            s.silver = s.silver - healCost
                            local heal = math.random(healMin, healMax)
                            m.health = math.min(100, m.health + heal)
                            if m.health >= 60 and m.state == "生病" then
                                m.state = m.prevState or "在家"
                                m.prevState = nil
                                msgs[#msgs + 1] = m.name .. "康复（健康+" .. heal .. "）"
                            else
                                msgs[#msgs + 1] = m.name .. "好转（健康+" .. heal .. "）"
                            end
                        end
                        GameData.AddLog("医馆诊治：" .. table.concat(msgs, "、"))
                        modal:Close()
                        GameScreen.ShowResultPopup("医馆 · 诊治完毕", table.concat(msgs, "\n"))
                        GameScreen.RefreshAll()
                    end or nil,
                    children = {
                        UI.Label {
                            text = canHealAll
                                and ("全部诊治（" .. #sickMembers .. "人 · " .. totalCost .. "两）")
                                or ("银两不足（需 " .. totalCost .. " 两）"),
                            fontSize = 14,
                            fontColor = canHealAll and Theme.TEXT_WHITE or Theme.TEXT_MUTED,
                            fontWeight = "bold",
                        },
                    },
                }
            end

            -- 逐人列表
            children[#children + 1] = UI.Label { text = "需诊治族人", fontSize = 15, fontColor = Theme.GOLD, fontWeight = "bold", marginTop = 8 }

            for _, m in ipairs(sickMembers) do
                local canHeal = s.silver >= healCost
                local healthColor = statColor(m.health)
                local stateDesc = m.state == "生病" and "染病" or "体弱"

                children[#children + 1] = UI.Panel {
                    width = "100%", padding = 8, borderRadius = 8,
                    backgroundColor = Theme.BG_WHITE, borderWidth = 1, borderColor = Theme.BORDER,
                    flexDirection = "row", alignItems = "center", gap = 8, marginTop = 4,
                    children = {
                        -- 族人信息
                        UI.Panel {
                            flex = 1, gap = 2,
                            children = {
                                UI.Panel {
                                    flexDirection = "row", alignItems = "center", gap = 4,
                                    children = {
                                        UI.Label { text = m.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                                        UI.Label { text = stateDesc, fontSize = 12, fontColor = Theme.RED },
                                        UI.Label { text = m.age .. "岁", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                                    },
                                },
                                StatBar("健康", m.health),
                            },
                        },
                        -- 治疗按钮
                        UI.Panel {
                            width = 68, height = 30, borderRadius = 6,
                            backgroundGradient = canHeal
                                and { direction = "to-right", from = { 60, 160, 120, 255 }, to = { 40, 140, 100, 255 } }
                                or nil,
                            backgroundColor = (not canHeal) and Theme.BG_INPUT or nil,
                            justifyContent = "center", alignItems = "center",
                            onTap = canHeal and function(self)
                                AudioManager.Click()
                                s.silver = s.silver - healCost
                                local heal = math.random(healMin, healMax)
                                m.health = math.min(100, m.health + heal)
                                local msg
                                if m.health >= 60 and m.state == "生病" then
                                    m.state = m.prevState or "在家"
                                    m.prevState = nil
                                    msg = m.name .. "经郎中诊治后康复，健康+" .. heal
                                else
                                    msg = m.name .. "经郎中诊治后好转，健康+" .. heal
                                end
                                GameData.AddLog("医馆：" .. msg)
                                modal:Close()
                                GameScreen.ShowResultPopup("医馆 · 问诊", msg)
                                GameScreen.RefreshAll()
                            end or nil,
                            children = {
                                UI.Label {
                                    text = canHeal and (healCost .. "两") or "银不足",
                                    fontSize = 13,
                                    fontColor = canHeal and Theme.TEXT_WHITE or Theme.TEXT_MUTED,
                                    fontWeight = "bold",
                                },
                            },
                        },
                    },
                }
            end
        end

        -- ========================================
        -- 坐堂郎中（女性族人，学识>60，家族限1人）
        -- ========================================
        children[#children + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 10 }
        children[#children + 1] = UI.Label { text = "坐堂郎中", fontSize = 16, fontColor = Theme.GOLD, fontWeight = "bold", marginTop = 6 }
        local clinicYearlyLimit = AdSystem.GetClinicYearlyLimit()
        local clinicBoosted = AdSystem.IsClinicBoosted()
        children[#children + 1] = UI.Label {
            text = "指派一名女性族人（学识60以上）坐堂行医，每年自动诊治最多" .. clinicYearlyLimit .. "名体弱族人",
            fontSize = 12, fontColor = Theme.TEXT_SECONDARY,
        }

        local doctorId = s.clinicDoctorId
        local doctor = doctorId and GameData.GetMember(doctorId) or nil
        -- 清理无效分配（死亡/不再符合）
        if doctor and (not doctor.alive or doctor.gender ~= "female" or (doctor.study or 0) < 60) then
            s.clinicDoctorId = nil
            doctor = nil
        end

        local healsThisYear = s._clinicHealsThisYear or 0

        if doctor then
            -- 已有坐堂郎中：显示信息 + 操作按钮
            local doctorActionChildren = {}
            -- 看广告增加次数按钮
            if not clinicBoosted and AdSystem.IsAvailable("clinic_boost") then
                local boostRemain = AdSystem.GetRemaining("clinic_boost")
                doctorActionChildren[#doctorActionChildren + 1] = UI.Panel {
                    height = 28, borderRadius = 6, paddingHorizontal = 8,
                    backgroundGradient = { direction = "to-right", from = { 60, 160, 120, 255 }, to = { 40, 140, 100, 255 } },
                    flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 3,
                    onTap = function(self)
                        AdSystem.BoostClinicDoctor(function(success, msg)
                            modal:Close()
                            if success then
                                Toast.Success("郎中加持生效，今年可诊治6人")
                            else
                                GameScreen.ShowResultPopup("加持失败", msg or "")
                            end
                            GameScreen.ShowClinicDialog()
                            GameScreen.RefreshAll()
                        end)
                    end,
                    children = {
                        UI.Label { text = "▶广告·加持", fontSize = 12, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                        UI.Label { text = "(" .. boostRemain .. ")", fontSize = 11, fontColor = { 255, 255, 255, 160 } },
                    },
                }
            elseif clinicBoosted then
                local remainSec = AdSystem.GetClinicBoostRemainingSeconds()
                local remainH = math.ceil(remainSec / 3600)
                doctorActionChildren[#doctorActionChildren + 1] = UI.Label {
                    text = "加持中·" .. remainH .. "h", fontSize = 12, fontColor = Theme.GREEN,
                }
            end
            -- 卸任按钮
            doctorActionChildren[#doctorActionChildren + 1] = UI.Panel {
                width = 58, height = 28, borderRadius = 6,
                backgroundColor = { 180, 80, 60, 255 },
                justifyContent = "center", alignItems = "center",
                onTap = function(self)
                    AudioManager.Click()
                    s.clinicDoctorId = nil
                    GameData.AddLog(doctor.name .. "卸任坐堂郎中，回到家中。")
                    Toast.Info(doctor.name .. "已卸任")
                    modal:Close()
                    GameScreen.ShowClinicDialog()
                    GameScreen.RefreshAll()
                end,
                children = {
                    UI.Label { text = "卸任", fontSize = 13, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                },
            }

            children[#children + 1] = UI.Panel {
                width = "100%", padding = 10, borderRadius = 8, marginTop = 4,
                backgroundColor = { 240, 255, 245, 255 }, borderWidth = 1, borderColor = { 120, 200, 140, 255 },
                gap = 4,
                children = {
                    UI.Panel {
                        flexDirection = "row", alignItems = "center", gap = 6,
                        children = {
                            UI.Label { text = doctor.name, fontSize = 15, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                            UI.Label { text = "女 · " .. doctor.age .. "岁", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                            UI.Label { text = "学识" .. (doctor.study or 0), fontSize = 12, fontColor = Theme.BLUE },
                        },
                    },
                    UI.Label { text = "今年已诊治 " .. healsThisYear .. "/" .. clinicYearlyLimit .. " 人", fontSize = 13, fontColor = healsThisYear >= clinicYearlyLimit and Theme.RED or Theme.GREEN },
                    UI.Panel {
                        flexDirection = "row", justifyContent = "flex-end", alignItems = "center", gap = 6, marginTop = 2,
                        children = doctorActionChildren,
                    },
                },
            }
        else
            -- 未分配坐堂郎中：显示可选女性族人
            local candidates = {}
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.gender == "female" and (m.study or 0) >= 60 and m.age >= 16 and m.state == "在家" then
                    candidates[#candidates + 1] = m
                end
            end

            if #candidates == 0 then
                children[#children + 1] = UI.Panel {
                    width = "100%", padding = 10, borderRadius = 8, marginTop = 4,
                    backgroundColor = Theme.BG_INPUT,
                    justifyContent = "center", alignItems = "center", gap = 4,
                    children = {
                        UI.Label { text = "暂无合适人选", fontSize = 14, fontColor = Theme.TEXT_MUTED },
                        UI.Label { text = "需成年女性族人且学识达60以上", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                    },
                }
            else
                for _, m in ipairs(candidates) do
                    children[#children + 1] = UI.Panel {
                        width = "100%", padding = 8, borderRadius = 8, marginTop = 4,
                        backgroundColor = Theme.BG_WHITE, borderWidth = 1, borderColor = Theme.BORDER,
                        flexDirection = "row", alignItems = "center", gap = 8,
                        children = {
                            UI.Panel {
                                flex = 1, gap = 2,
                                children = {
                                    UI.Panel {
                                        flexDirection = "row", alignItems = "center", gap = 4,
                                        children = {
                                            UI.Label { text = m.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                                            UI.Label { text = m.age .. "岁", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                                        },
                                    },
                                    UI.Panel {
                                        flexDirection = "row", gap = 6,
                                        children = {
                                            UI.Label { text = "学识" .. (m.study or 0), fontSize = 12, fontColor = Theme.BLUE },
                                            UI.Label { text = "健康" .. (m.health or 0), fontSize = 12, fontColor = Theme.TEXT_SECONDARY },
                                        },
                                    },
                                },
                            },
                            UI.Panel {
                                width = 56, height = 28, borderRadius = 6,
                                backgroundGradient = { direction = "to-right", from = { 60, 160, 120, 255 }, to = { 40, 140, 100, 255 } },
                                justifyContent = "center", alignItems = "center",
                                onTap = function(self)
                                    AudioManager.Click()
                                    s.clinicDoctorId = m.id
                                    GameData.AddLog(m.name .. "被指派为坐堂郎中，在医馆行医济世。")
                                    Toast.Success(m.name .. "就任坐堂郎中")
                                    modal:Close()
                                    GameScreen.ShowClinicDialog()
                                    GameScreen.RefreshAll()
                                end,
                                children = {
                                    UI.Label { text = "指派", fontSize = 13, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                                },
                            },
                        },
                    }
                end
            end
        end

        -- ========================================
        -- 临时郎中（看广告增加，1小时有效，最多3人）
        -- ========================================
        children[#children + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 10 }
        children[#children + 1] = UI.Label { text = "临时郎中", fontSize = 16, fontColor = Theme.GOLD, fontWeight = "bold", marginTop = 6 }
        children[#children + 1] = UI.Label {
            text = "看广告增派临时郎中（1小时有效），最多同时3名",
            fontSize = 12, fontColor = Theme.TEXT_SECONDARY,
        }

        local extraDoctors = AdSystem.GetExtraDoctors()
        local extraCount = #extraDoctors

        -- 显示已有的临时医生
        if extraCount > 0 then
            for idx, ed in ipairs(extraDoctors) do
                local edMember = GameData.GetMember(ed.memberId)
                if edMember then
                    local remainSec = math.max(0, ed.expiresAt - os.time())
                    local remainMin = math.ceil(remainSec / 60)
                    children[#children + 1] = UI.Panel {
                        width = "100%", padding = 8, borderRadius = 8, marginTop = 4,
                        backgroundColor = { 245, 250, 255, 255 }, borderWidth = 1, borderColor = { 100, 160, 220, 255 },
                        flexDirection = "row", alignItems = "center", gap = 8,
                        children = {
                            UI.Panel {
                                flex = 1, gap = 2,
                                children = {
                                    UI.Panel {
                                        flexDirection = "row", alignItems = "center", gap = 4,
                                        children = {
                                            UI.Label { text = edMember.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                                            UI.Label { text = "临时", fontSize = 11, fontColor = Theme.BLUE },
                                            UI.Label { text = edMember.age .. "岁", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                                        },
                                    },
                                    UI.Label { text = "学识" .. (edMember.study or 0) .. " · 剩余" .. remainMin .. "分钟", fontSize = 12, fontColor = Theme.TEXT_SECONDARY },
                                },
                            },
                            UI.Panel {
                                width = 18, height = 18, borderRadius = 9,
                                backgroundColor = { 100, 160, 220, 200 },
                                justifyContent = "center", alignItems = "center",
                                children = {
                                    UI.Label { text = tostring(idx), fontSize = 11, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                                },
                            },
                        },
                    }
                end
            end
        end

        -- 增派临时郎中按钮（未满3人时显示候选列表）
        if extraCount < 3 then
            -- 收集可指派为临时郎中的候选人
            local extraCandidates = {}
            for _, m in ipairs(GameData.GetAliveMembers()) do
                -- 需年满16岁，排除正式郎中
                if m.age >= 16 and m.id ~= s.clinicDoctorId then
                    -- 排除已在临时坐诊的
                    local alreadyExtra = false
                    for _, ed in ipairs(extraDoctors) do
                        if ed.memberId == m.id then alreadyExtra = true; break end
                    end
                    if not alreadyExtra then
                        extraCandidates[#extraCandidates + 1] = m
                    end
                end
            end

            if #extraCandidates > 0 then
                children[#children + 1] = UI.Label { text = "可增派（" .. (3 - extraCount) .. "个名额）", fontSize = 13, fontColor = Theme.TEXT_SECONDARY, marginTop = 6 }
                for _, m in ipairs(extraCandidates) do
                    children[#children + 1] = UI.Panel {
                        width = "100%", padding = 8, borderRadius = 8, marginTop = 4,
                        backgroundColor = Theme.BG_WHITE, borderWidth = 1, borderColor = Theme.BORDER,
                        flexDirection = "row", alignItems = "center", gap = 8,
                        children = {
                            UI.Panel {
                                flex = 1, gap = 2,
                                children = {
                                    UI.Panel {
                                        flexDirection = "row", alignItems = "center", gap = 4,
                                        children = {
                                            UI.Label { text = m.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                                            UI.Label { text = m.age .. "岁", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                                        },
                                    },
                                    UI.Panel {
                                        flexDirection = "row", gap = 6,
                                        children = {
                                            UI.Label { text = "学识" .. (m.study or 0), fontSize = 12, fontColor = Theme.BLUE },
                                            UI.Label { text = "健康" .. (m.health or 0), fontSize = 12, fontColor = Theme.TEXT_SECONDARY },
                                        },
                                    },
                                },
                            },
                            UI.Panel {
                                height = 28, borderRadius = 6, paddingHorizontal = 8,
                                backgroundGradient = { direction = "to-right", from = { 60, 140, 200, 255 }, to = { 40, 120, 180, 255 } },
                                flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 3,
                                onTap = function(self)
                                    AudioManager.Click()
                                    AdSystem.AddExtraDoctor(m.id, function(success, msg)
                                        modal:Close()
                                        if success then
                                            Toast.Success(msg or "已增派")
                                        else
                                            GameScreen.ShowResultPopup("增派失败", msg or "")
                                        end
                                        GameScreen.ShowClinicDialog()
                                        GameScreen.RefreshAll()
                                    end)
                                end,
                                children = {
                                    UI.Label { text = "▶广告·增派", fontSize = 12, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                                },
                            },
                        },
                    }
                end
            elseif extraCount == 0 then
                children[#children + 1] = UI.Panel {
                    width = "100%", padding = 8, borderRadius = 8, marginTop = 4,
                    backgroundColor = Theme.BG_INPUT,
                    justifyContent = "center", alignItems = "center",
                    children = {
                        UI.Label { text = "暂无可指派族人", fontSize = 13, fontColor = Theme.TEXT_MUTED },
                    },
                }
            end
        else
            children[#children + 1] = UI.Label {
                text = "临时郎中已满（3/3）",
                fontSize = 13, fontColor = Theme.BLUE, marginTop = 4,
            }
        end

        -- 药铺提示
        local hasHerbShop = false
        if s.industries then
            for _, ind in ipairs(s.industries) do
                if ind.id == "herb_shop" then hasHerbShop = true; break end
            end
        end
        if not hasHerbShop then
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 6, borderRadius = 6, marginTop = 8,
                backgroundColor = { 255, 248, 220, 255 }, gap = 2,
                children = {
                    UI.Label { text = "提示：开设药铺可降低全族生病概率15%", fontSize = 12, fontColor = Theme.TEXT_SECONDARY },
                },
            }
        end

        modal:AddContent(UI.ScrollView {
            width = "100%", maxHeight = 420, scrollY = true, showScrollbar = true, bounces = true,
            children = {
                UI.Panel { width = "100%", padding = 8, gap = 6, children = children },
            },
        })
    end

    renderContent()
    modal:Open()
end

-- ============================================================================
-- 弹窗：打工（派遣族人外出做工赚银两）
-- ============================================================================

function GameScreen.ShowLaborDialog()
    local s = GameData.state
    local clanRank = s.clanRank or 1

    local modal = UI.Modal {
        size = "md",
        showCloseButton = false,
        closeOnOverlay = true,
    }
    modal:AddContent(CreateLeftCloseHeader(modal, "打工 · 外出做工"))

    local function renderContent()
        local children = {}

        -- 当前打工中的族人
        local laborers = GameData.GetMembersByState("打工")

        -- 说明区
        children[#children + 1] = UI.Panel {
            width = "100%", padding = 8, borderRadius = 8,
            backgroundColor = Theme.BG_INPUT, gap = 4,
            children = {
                UI.Label { text = "派遣族人外出做工", fontSize = 15, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                UI.Label { text = "打工族人无法读书、经商、从军或管理产业", fontSize = 12, fontColor = Theme.RED },
                UI.Label { text = "当前打工：" .. #laborers .. " 人", fontSize = 13, fontColor = Theme.TEXT_SECONDARY },
            },
        }

        -- 已在打工的族人列表
        if #laborers > 0 then
            children[#children + 1] = UI.Label { text = "打工中", fontSize = 15, fontColor = Theme.GOLD, fontWeight = "bold", marginTop = 8 }
            for _, m in ipairs(laborers) do
                local job = MemberData.GetLaborJob(m.laborJob)
                local jobName = job and job.name or "杂工"
                local jobWage = job and job.wage or 3
                children[#children + 1] = UI.Panel {
                    width = "100%", padding = 8, borderRadius = 8,
                    backgroundColor = Theme.BG_WHITE, borderWidth = 1, borderColor = Theme.BORDER,
                    flexDirection = "row", alignItems = "center", gap = 8, marginTop = 4,
                    children = {
                        UI.Panel {
                            flex = 1, gap = 2,
                            children = {
                                UI.Panel {
                                    flexDirection = "row", alignItems = "center", gap = 4,
                                    children = {
                                        UI.Label { text = m.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                                        UI.Label { text = jobName, fontSize = 12, fontColor = {180, 140, 60, 255} },
                                        UI.Label { text = m.age .. "岁", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                                    },
                                },
                                UI.Label { text = "月入 " .. jobWage .. " 银两", fontSize = 12, fontColor = Theme.GREEN },
                            },
                        },
                        -- 召回按钮
                        UI.Panel {
                            width = 56, height = 28, borderRadius = 6,
                            backgroundColor = {180, 80, 60, 255},
                            justifyContent = "center", alignItems = "center",
                            onTap = function(self)
                                AudioManager.Click()
                                m.state = "在家"
                                m.laborJob = nil
                                GameData.AddLog(m.name .. "停止打工，回到家中。")
                                modal:Close()
                                GameScreen.ShowLaborDialog()  -- 重新打开刷新
                                GameScreen.RefreshAll()
                            end,
                            children = {
                                UI.Label { text = "召回", fontSize = 13, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                            },
                        },
                    },
                }
            end
        end

        -- 可用工种列表
        local availableJobs = MemberData.GetAvailableLaborJobs(clanRank)
        children[#children + 1] = UI.Label { text = "可用工种", fontSize = 15, fontColor = Theme.GOLD, fontWeight = "bold", marginTop = 10 }

        -- 所有工种（包括未解锁的，灰显）
        for _, job in ipairs(MemberData.LABOR_JOBS) do
            local unlocked = clanRank >= job.rank
            local rankName = GameData.CLAN_RANKS[job.rank] or "?"

            children[#children + 1] = UI.Panel {
                width = "100%", padding = 8, borderRadius = 8, marginTop = 4,
                backgroundColor = unlocked and Theme.BG_WHITE or {240, 240, 240, 255},
                borderWidth = 1, borderColor = unlocked and Theme.BORDER or {220, 220, 220, 255},
                opacity = unlocked and 1.0 or 0.5,
                flexDirection = "row", alignItems = "center", gap = 8,
                children = {
                    -- 工种信息
                    UI.Panel {
                        flex = 1, gap = 2,
                        children = {
                            UI.Panel {
                                flexDirection = "row", alignItems = "center", gap = 4,
                                children = {
                                    UI.Label { text = job.name, fontSize = 14, fontColor = unlocked and Theme.TEXT_PRIMARY or Theme.TEXT_MUTED, fontWeight = "bold" },
                                    UI.Label { text = "月薪 " .. job.wage .. " 两", fontSize = 12, fontColor = unlocked and Theme.GREEN or Theme.TEXT_MUTED },
                                },
                            },
                            UI.Label { text = job.desc, fontSize = 12, fontColor = Theme.TEXT_SECONDARY },
                            (not unlocked) and UI.Label { text = "需品级【" .. rankName .. "】解锁", fontSize = 11, fontColor = Theme.RED } or nil,
                        },
                    },
                    -- 派遣按钮
                    unlocked and UI.Panel {
                        width = 56, height = 28, borderRadius = 6,
                        backgroundGradient = { direction = "to-right", from = {180, 140, 60, 255}, to = {160, 120, 40, 255} },
                        justifyContent = "center", alignItems = "center",
                        onTap = function(self)
                            AudioManager.Click()
                            -- 弹出选人界面
                            modal:Close()
                            GameScreen.ShowLaborAssign(job)
                        end,
                        children = {
                            UI.Label { text = "派遣", fontSize = 13, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                        },
                    } or nil,
                },
            }
        end

        modal:AddContent(UI.ScrollView {
            width = "100%", maxHeight = 400, scrollY = true, showScrollbar = true, bounces = true,
            children = {
                UI.Panel { width = "100%", padding = 8, gap = 4, children = children },
            },
        })
    end

    renderContent()
    modal:Open()
end

-- ============================================================================
-- 弹窗：打工派遣选人
-- ============================================================================

function GameScreen.ShowLaborAssign(job)
    -- 可派遣的族人：成年 + 在家状态
    local candidates = {}
    for _, m in ipairs(GameData.GetAdultMembers()) do
        if m.state == "在家" then
            candidates[#candidates + 1] = m
        end
    end

    local modal = UI.Modal {
        title = "派遣 · " .. job.name,
        size = "md",
        showCloseButton = true,
        closeOnOverlay = true,
    }

    local children = {}

    children[#children + 1] = UI.Panel {
        width = "100%", padding = 8, borderRadius = 8,
        backgroundColor = Theme.BG_INPUT, gap = 4,
        children = {
            UI.Label { text = job.name .. " · " .. job.desc, fontSize = 14, fontColor = Theme.TEXT_PRIMARY },
            UI.Label { text = "月薪 " .. job.wage .. " 银两", fontSize = 13, fontColor = Theme.GREEN },
            UI.Label { text = "选择一名族人外出打工", fontSize = 12, fontColor = Theme.TEXT_MUTED },
        },
    }

    if #candidates == 0 then
        children[#children + 1] = UI.Panel {
            width = "100%", paddingTop = 30, paddingBottom = 30,
            justifyContent = "center", alignItems = "center", gap = 6,
            children = {
                UI.Label { text = "无人可用", fontSize = 16, fontColor = Theme.TEXT_MUTED, fontWeight = "bold" },
                UI.Label { text = "没有闲在家中的成年族人", fontSize = 13, fontColor = Theme.TEXT_MUTED },
            },
        }
    else
        for _, m in ipairs(candidates) do
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 8, borderRadius = 8, marginTop = 4,
                backgroundColor = Theme.BG_WHITE, borderWidth = 1, borderColor = Theme.BORDER,
                flexDirection = "row", alignItems = "center", gap = 8,
                children = {
                    UI.Panel {
                        flex = 1, gap = 2,
                        children = {
                            UI.Panel {
                                flexDirection = "row", alignItems = "center", gap = 4,
                                children = {
                                    UI.Label { text = m.name, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                                    UI.Label { text = (m.gender == "male" and "男" or "女") .. " " .. m.age .. "岁", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                                },
                            },
                            UI.Panel {
                                flexDirection = "row", gap = 6,
                                children = {
                                    UI.Label { text = "学" .. (m.study or 0), fontSize = 11, fontColor = Theme.TEXT_SECONDARY },
                                    UI.Label { text = "武" .. (m.martial or 0), fontSize = 11, fontColor = Theme.TEXT_SECONDARY },
                                    UI.Label { text = "健" .. (m.health or 0), fontSize = 11, fontColor = Theme.TEXT_SECONDARY },
                                },
                            },
                        },
                    },
                    UI.Panel {
                        width = 56, height = 28, borderRadius = 6,
                        backgroundGradient = { direction = "to-right", from = {180, 140, 60, 255}, to = {160, 120, 40, 255} },
                        justifyContent = "center", alignItems = "center",
                        onTap = function(self)
                            AudioManager.Click()
                            m.state = "打工"
                            m.laborJob = job.id
                            GameData.AddLog(m.name .. "外出做" .. job.name .. "，月入" .. job.wage .. "两。")
                            modal:Close()
                            GameScreen.RefreshAll()
                            Toast.Success(m.name .. "开始打工")
                        end,
                        children = {
                            UI.Label { text = "派遣", fontSize = 13, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                        },
                    },
                },
            }
        end
    end

    -- 返回按钮
    children[#children + 1] = UI.Panel {
        width = "100%", height = 32, borderRadius = 6, marginTop = 8,
        backgroundColor = Theme.BG_INPUT,
        justifyContent = "center", alignItems = "center",
        onTap = function(self)
            AudioManager.Click()
            modal:Close()
            GameScreen.ShowLaborDialog()  -- 返回打工主界面
        end,
        children = {
            UI.Label { text = "← 返回打工列表", fontSize = 13, fontColor = Theme.TEXT_SECONDARY },
        },
    }

    modal:AddContent(UI.ScrollView {
        width = "100%", maxHeight = 400, scrollY = true, showScrollbar = true, bounces = true,
        children = {
            UI.Panel { width = "100%", padding = 8, gap = 4, children = children },
        },
    })
    modal:Open()
end

end -- ModalDialogs.Init 结束

return ModalDialogs
