-- ============================================================================
-- 大明浮生志2 - 仕途科举 / 从军 页面
-- 从 GameScreen.lua 拆分出来的全屏页面模块
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")
local AudioManager = require("Systems.AudioManager")

local CareerPage = {}

--- 创建仕途科举页面
---@param pageTitle fun(title: string, subtitle: string): table  PageTitle 组件构造函数
---@param gameScreen table  GameScreen 模块引用（用于调用 ShowResultPopup, RefreshAll）
function CareerPage.Create(pageTitle, gameScreen)
    local s = GameData.state

    local scholars = {}
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.age >= 6 and m.age <= 50 and m.state == "在家" then scholars[#scholars + 1] = m end
    end
    local soldiers = {}
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.age >= 16 and m.age <= 45 and m.gender == "male" and m.state == "在家" then soldiers[#soldiers + 1] = m end
    end
    local examCandidates = {}
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.age >= 15 and m.state == "读书" and m.study >= 20 then examCandidates[#examCandidates + 1] = m end
    end

    local children = {
        pageTitle("仕途科举", "经文纬武，报效朝廷"),

        -- 安排读书
        UI.Label { text = "安排族人读书", fontSize = 15, fontColor = Theme.GOLD, marginTop = 4 },
        UI.Label { text = "可安排人数：" .. #scholars, fontSize = 11, fontColor = Theme.TEXT_SECONDARY },
    }

    for _, m in ipairs(scholars) do
        children[#children + 1] = UI.Panel {
            width = "100%", padding = 10, borderRadius = 8,
            backgroundColor = Theme.BG_INPUT,
            flexDirection = "row", justifyContent = "space-between", alignItems = "center",
            children = {
                UI.Panel {
                    flexDirection = "row", gap = 6, alignItems = "center",
                    children = {
                        UI.Label { text = m.name, fontSize = 13, fontColor = Theme.TEXT_PRIMARY },
                        UI.Label { text = m.age .. "岁", fontSize = 10, fontColor = Theme.TEXT_MUTED },
                        UI.Label { text = "学" .. m.study, fontSize = 10, fontColor = Theme.BLUE },
                    },
                },
                UI.Panel {
                    paddingHorizontal = 12, paddingVertical = 5, borderRadius = 6,
                    backgroundColor = { 66, 133, 244, 35 }, borderWidth = 1, borderColor = Theme.BLUE,
                    onClick = function(self)
                        AudioManager.Click()
                        m.state = "读书"
                        GameData.AddLog(m.name .. "开始在族学读书。")
                        gameScreen.RefreshAll()
                    end,
                    children = { UI.Label { text = "入学", fontSize = 12, fontColor = Theme.BLUE } },
                },
            },
        }
    end

    -- 科举考试
    children[#children + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 12 }
    local examUnlocked = s.clanRank >= (GameData.CAREER_UNLOCK.exam or 3)
    local examReqRank = GameData.GetUnlockRankName(GameData.CAREER_UNLOCK.exam or 3)
    if not examUnlocked then
        children[#children + 1] = UI.Panel {
            width = "100%", padding = 12, borderRadius = 8,
            backgroundColor = Theme.BG_INPUT, opacity = 0.6,
            gap = 6, alignItems = "center",
            onClick = function(self)
                gameScreen.ShowResultPopup("科举考试（未解锁）",
                    "提升品级至【" .. examReqRank .. "】后可参加科举考试。\n\n"
                    .. "科举路径：\n"
                    .. "  1. 先在书院安排族人读书，提升学识\n"
                    .. "  2. 学识达标后依次参加：\n"
                    .. "     童试（学识30+）→ 乡试（60+）→ 会试（85+）→ 殿试（95+）\n"
                    .. "  3. 中举/中进士可大幅提升声望\n\n"
                    .. "当前品级：" .. GameData.GetClanRankName() .. "，需提升至" .. examReqRank)
            end,
            children = {
                UI.Panel { width = 200, height = 112, alignSelf = "center", borderRadius = 8, overflow = "hidden", backgroundImage = Theme.IMG.BTN_EXAM, backgroundFit = "cover", opacity = 0.4 },
                UI.Label { text = "科举取士 · 需品级【" .. examReqRank .. "】解锁", fontSize = 12, fontColor = Theme.GOLD },
                UI.Label { text = "安排族人读书→参加科举→获取功名声望", fontSize = 10, fontColor = Theme.TEXT_SECONDARY },
                UI.Label { text = "点击查看详情", fontSize = 9, fontColor = Theme.BLUE },
            },
        }
    else
    children[#children + 1] = UI.Panel { width = 200, height = 112, alignSelf = "center", borderRadius = 8, overflow = "hidden", marginTop = 4, backgroundImage = Theme.IMG.BTN_EXAM, backgroundFit = "cover" }

    for _, m in ipairs(examCandidates) do
        local availExam = nil
        for _, exam in ipairs(GameData.EXAM_LEVELS) do
            if m.study >= exam.reqStudy then
                local alreadyPassed = false
                for j, e2 in ipairs(GameData.EXAM_LEVELS) do
                    if e2.result == m.identity then
                        for k = j + 1, #GameData.EXAM_LEVELS do
                            if GameData.EXAM_LEVELS[k].id == exam.id then alreadyPassed = true end
                        end
                        if e2.id == exam.id then alreadyPassed = true end
                    end
                end
                if not alreadyPassed then availExam = exam end
            end
        end

        if availExam then
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 10, borderRadius = 8,
                backgroundColor = Theme.BG_INPUT,
                flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                children = {
                    UI.Panel {
                        gap = 2, flexShrink = 1,
                        children = {
                            UI.Label { text = m.name .. "（学识" .. m.study .. "）", fontSize = 13, fontColor = Theme.TEXT_PRIMARY },
                            UI.Label { text = "可参加：" .. availExam.name .. "（基础" .. math.floor(availExam.passRate * 100) .. "%）", fontSize = 10, fontColor = Theme.TEXT_SECONDARY },
                        },
                    },
                    UI.Panel {
                        paddingHorizontal = 12, paddingVertical = 5, borderRadius = 6,
                        backgroundGradient = Theme.GRADIENT_PRIMARY,
                        onClick = function(self)
                            AudioManager.Click()
                            CareerPage.ShowExamStrategyModal(m, availExam, gameScreen)
                        end,
                        children = { UI.Label { text = "赴考", fontSize = 12, fontColor = Theme.TEXT_WHITE } },
                    },
                },
            }
        end
    end

    end -- examUnlocked else end

    -- 纳捐监生
    children[#children + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 12 }
    local donateUnlocked = s.clanRank >= (GameData.CAREER_UNLOCK.donate or 4)
    local donateReqRank = GameData.GetUnlockRankName(GameData.CAREER_UNLOCK.donate or 4)
    if not donateUnlocked then
        children[#children + 1] = UI.Panel {
            width = "100%", padding = 12, borderRadius = 8,
            backgroundColor = Theme.BG_INPUT, opacity = 0.6,
            gap = 6, alignItems = "center",
            onClick = function(self)
                gameScreen.ShowResultPopup("纳捐监生（未解锁）",
                    "提升品级至【" .. donateReqRank .. "】后可纳捐监生。\n\n"
                    .. "纳捐说明：\n"
                    .. "  · 花费银两为族人购买国子监名额\n"
                    .. "  · 族人获得「监生」身份\n"
                    .. "  · 无需参加科举，直接获得功名\n\n"
                    .. "当前品级：" .. GameData.GetClanRankName() .. "，需提升至" .. donateReqRank)
            end,
            children = {
                UI.Label { text = "纳捐监生 · 需品级【" .. donateReqRank .. "】解锁", fontSize = 12, fontColor = Theme.GOLD },
                UI.Label { text = "花银两为族人买功名（不需科举）", fontSize = 10, fontColor = Theme.TEXT_SECONDARY },
                UI.Label { text = "点击查看详情", fontSize = 9, fontColor = Theme.BLUE },
            },
        }
    else
    children[#children + 1] = UI.Label { text = "纳捐监生", fontSize = 15, fontColor = Theme.GOLD, marginTop = 4 }
    children[#children + 1] = UI.Label {
        text = "花银两" .. GameData.DONATION_COST.silver .. "买国子监名额（声望" .. GameData.DONATION_COST.famePrice .. "）",
        fontSize = 10, fontColor = Theme.TEXT_SECONDARY, whiteSpace = "normal",
    }
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if (m.identity == "白丁" or m.identity == "童生") and m.age >= 15 and m.state ~= "从军" and m.state ~= "出征" then
            local canDonate = GameData.CanAfford(GameData.DONATION_COST.silver, 0, 0, 0)
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 10, borderRadius = 8,
                backgroundColor = Theme.BG_INPUT,
                flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                opacity = canDonate and 1.0 or 0.4,
                children = {
                    UI.Panel {
                        flexDirection = "row", gap = 6, alignItems = "center",
                        children = {
                            UI.Label { text = m.name, fontSize = 13, fontColor = Theme.TEXT_PRIMARY },
                            UI.Label { text = m.identity, fontSize = 10, fontColor = Theme.TEXT_MUTED },
                        },
                    },
                    UI.Panel {
                        paddingHorizontal = 12, paddingVertical = 5, borderRadius = 6,
                        backgroundColor = canDonate and { 180, 140, 50, 40 } or Theme.BG_INPUT,
                        borderWidth = 1, borderColor = Theme.GOLD_DARK,
                        onClick = function(self)
                            if not canDonate then return end
                            AudioManager.Click()
                            if GameData.SpendResources(GameData.DONATION_COST.silver, 0, 0, 0) then
                                m.identity = GameData.DONATION_COST.resultIdentity
                                GameData.AddResource("fame", GameData.DONATION_COST.famePrice)
                                GameData.AddLog(m.name .. "纳捐入国子监，获监生身份。")
                                gameScreen.ShowResultPopup("纳捐成功", m.name .. "已获监生身份。")
                                gameScreen.RefreshAll()
                            end
                        end,
                        children = { UI.Label { text = "纳捐（-" .. GameData.DONATION_COST.silver .. "银）", fontSize = 10, fontColor = Theme.GOLD } },
                    },
                },
            }
        end
    end
    end -- donateUnlocked else end

    -- 从军
    children[#children + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 12 }
    local militaryUnlocked = s.clanRank >= (GameData.CAREER_UNLOCK.military or 3)
    local militaryReqRank = GameData.GetUnlockRankName(GameData.CAREER_UNLOCK.military or 3)
    if not militaryUnlocked then
        children[#children + 1] = UI.Panel {
            width = "100%", padding = 12, borderRadius = 8,
            backgroundColor = Theme.BG_INPUT, opacity = 0.6,
            gap = 6, alignItems = "center",
            onClick = function(self)
                gameScreen.ShowResultPopup("从军报国（未解锁）",
                    "提升品级至【" .. militaryReqRank .. "】后可从军报国。\n\n"
                    .. "从军特点：\n"
                    .. "  · 派遣男性族人（16-45岁）投身军旅\n"
                    .. "  · 每月获取军饷（银两）和声望\n"
                    .. "  · 可逐步晋升军职：士兵→把总→守备\n"
                    .. "  · 风险较高：从军有阵亡概率\n\n"
                    .. "当前品级：" .. GameData.GetClanRankName() .. "，需提升至" .. militaryReqRank)
            end,
            children = {
                UI.Panel { width = 200, height = 112, alignSelf = "center", borderRadius = 8, overflow = "hidden", backgroundImage = Theme.IMG.BTN_MILITARY, backgroundFit = "cover", opacity = 0.4 },
                UI.Label { text = "从军报国 · 需品级【" .. militaryReqRank .. "】解锁", fontSize = 12, fontColor = Theme.GOLD },
                UI.Label { text = "派遣族人从军→获取军饷声望（高风险高回报）", fontSize = 10, fontColor = Theme.TEXT_SECONDARY },
                UI.Label { text = "点击查看详情", fontSize = 9, fontColor = Theme.BLUE },
            },
        }
    else
    children[#children + 1] = UI.Panel { width = 200, height = 112, alignSelf = "center", borderRadius = 8, overflow = "hidden", marginTop = 4, backgroundImage = Theme.IMG.BTN_MILITARY, backgroundFit = "cover" }
    children[#children + 1] = UI.Label { text = "风险极高，但回报快。可用男性" .. #soldiers .. "人", fontSize = 11, fontColor = Theme.TEXT_SECONDARY }

    for _, m in ipairs(soldiers) do
        children[#children + 1] = UI.Panel {
            width = "100%", padding = 10, borderRadius = 8,
            backgroundColor = Theme.BG_INPUT,
            flexDirection = "row", justifyContent = "space-between", alignItems = "center",
            children = {
                UI.Panel {
                    flexDirection = "row", gap = 6, alignItems = "center",
                    children = {
                        UI.Label { text = m.name, fontSize = 13, fontColor = Theme.TEXT_PRIMARY },
                        UI.Label { text = m.age .. "岁", fontSize = 10, fontColor = Theme.TEXT_MUTED },
                        UI.Label { text = "武" .. m.martial, fontSize = 10, fontColor = Theme.RED },
                    },
                },
                UI.Panel {
                    paddingHorizontal = 12, paddingVertical = 5, borderRadius = 6,
                    backgroundGradient = Theme.GRADIENT_PRIMARY,
                    onClick = function(self)
                        AudioManager.Click()
                        m.state = "从军"
                        m.identity = "士兵"
                        m.militaryRank = "士兵"
                        GameData.AddLog(m.name .. "投身军旅。")
                        gameScreen.RefreshAll()
                    end,
                    children = { UI.Label { text = "从军", fontSize = 12, fontColor = Theme.TEXT_WHITE } },
                },
            },
        }
    end
    end -- militaryUnlocked else end

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

--- 科举策略选择弹窗
---@param member table 参加考试的族人
---@param exam table EXAM_LEVELS 中的考试定义
---@param gameScreen table GameScreen 模块引用
function CareerPage.ShowExamStrategyModal(member, exam, gameScreen)
    local s = GameData.state

    -- 计算基础通过率（含天赋、家训、广告加成）
    local baseRate = exam.passRate
    local bonusParts = {}  -- 展示用的加成明细

    if member.talent and member.talent.id == "smart" then
        baseRate = baseRate * 1.3
        bonusParts[#bonusParts + 1] = "天赋+30%"
    end

    local ruleEffects = GameData.GetClanRuleEffects()
    local examBonus = ruleEffects.examPassBonus or 0
    if examBonus > 0 then
        baseRate = baseRate + examBonus
        bonusParts[#bonusParts + 1] = "家训+" .. math.floor(examBonus * 100) .. "%"
    end

    local hasAdBoost = s.adExamBoost == true
    if hasAdBoost then
        baseRate = baseRate + 0.15
        bonusParts[#bonusParts + 1] = "加持+15%"
    end

    -- 三种备考策略
    local strategies = {
        {
            name = "闭门苦读",
            icon = "书",
            desc = "稳扎稳打，按部就班温习功课。",
            rateBonus = 0,
            cost = nil,
            penalty = nil,
            color = Theme.TEXT_SECONDARY,
        },
        {
            name = "名师指点",
            icon = "师",
            desc = "延请名师，指点迷津。消耗银两20。",
            rateBonus = 0.15,
            cost = { silver = 20 },
            penalty = nil,
            color = Theme.BLUE,
        },
        {
            name = "揣摩考题",
            icon = "靶",
            desc = "冒险押题，若落榜则学识-5。",
            rateBonus = 0.25,
            cost = nil,
            penalty = { studyLoss = 5 },
            color = Theme.GOLD,
        },
    }

    local modal = UI.Modal {
        title = exam.name .. " · 备考策略",
        size = "sm",
    }

    -- 顶部信息
    local bonusText = #bonusParts > 0 and ("（" .. table.concat(bonusParts, "、") .. "）") or ""
    local basePercent = math.min(math.floor(baseRate * 100), 95)
    local infoChildren = {
        UI.Label { text = member.name .. " · 学识" .. member.study, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
        UI.Label { text = "基础通过率：" .. basePercent .. "%" .. bonusText, fontSize = 11, fontColor = Theme.TEXT_SECONDARY },
    }

    -- 广告加持按钮（若尚未激活）
    local AdSystem = require("Systems.AdSystem")
    if not hasAdBoost and AdSystem.IsAvailable("exam_boost") then
        local adRemain = AdSystem.GetRemaining("exam_boost")
        infoChildren[#infoChildren + 1] = UI.Panel {
            width = "100%", height = 30, borderRadius = 6, marginTop = 4,
            backgroundGradient = { direction = "to-right", from = { 180, 130, 50, 255 }, to = { 160, 110, 30, 255 } },
            flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 4,
            onPointerDown = function(self)
                AudioManager.Click()
                AdSystem.ExamBoost(function(success)
                    if success then
                        modal:Close()
                        -- 重新打开带加持的弹窗
                        CareerPage.ShowExamStrategyModal(member, exam, gameScreen)
                    end
                end)
            end,
            children = {
                UI.Label { text = "▶ 看广告·科举加持+15%", fontSize = 10, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                UI.Label { text = "(" .. adRemain .. "次)", fontSize = 9, fontColor = { 255, 255, 255, 160 } },
            },
        }
    end

    -- 策略选项卡片
    local strategyCards = {}
    for _, strat in ipairs(strategies) do
        local finalRate = math.min(baseRate + strat.rateBonus, 0.95)
        local finalPercent = math.floor(finalRate * 100)
        local canAfford = true
        if strat.cost and strat.cost.silver then
            canAfford = GameData.CanAfford(strat.cost.silver, 0, 0, 0)
        end

        local detailParts = {}
        if strat.rateBonus > 0 then detailParts[#detailParts + 1] = "通过率+" .. math.floor(strat.rateBonus * 100) .. "%" end
        if strat.cost then detailParts[#detailParts + 1] = "银两-" .. strat.cost.silver end
        if strat.penalty then detailParts[#detailParts + 1] = "落榜学识-" .. strat.penalty.studyLoss end
        local detailText = #detailParts > 0 and table.concat(detailParts, "  ") or "无额外效果"

        strategyCards[#strategyCards + 1] = UI.Panel {
            width = "100%", padding = 10, borderRadius = 8,
            backgroundColor = canAfford and Theme.BG_INPUT or { 200, 195, 185, 120 },
            borderWidth = 1, borderColor = canAfford and strat.color or Theme.BORDER,
            opacity = canAfford and 1.0 or 0.5, gap = 4,
            onPointerDown = canAfford and function(self)
                AudioManager.Click()
                modal:Close()
                -- 扣除费用
                if strat.cost and strat.cost.silver then
                    GameData.SpendResources(strat.cost.silver, 0, 0, 0)
                end
                -- 清除广告加持标记（一次性）
                s.adExamBoost = nil
                -- 执行考试
                CareerPage.DoExam(member, exam, finalRate, strat, gameScreen)
            end or nil,
            children = {
                UI.Panel {
                    flexDirection = "row", alignItems = "center", gap = 6,
                    children = {
                        UI.Label { text = strat.icon, fontSize = 18 },
                        UI.Panel {
                            gap = 1, flexShrink = 1,
                            children = {
                                UI.Label { text = strat.name, fontSize = 13, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                                UI.Label { text = strat.desc, fontSize = 10, fontColor = Theme.TEXT_MUTED, whiteSpace = "normal" },
                            },
                        },
                    },
                },
                UI.Panel {
                    flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                    children = {
                        UI.Label { text = detailText, fontSize = 10, fontColor = strat.color },
                        UI.Label { text = "通过率 " .. finalPercent .. "%", fontSize = 12, fontColor = finalPercent >= 50 and Theme.GREEN or Theme.GOLD, fontWeight = "bold" },
                    },
                },
            },
        }
    end

    modal:SetContent(UI.Panel {
        width = "100%", gap = 8, padding = 4,
        children = {
            UI.Panel { width = "100%", gap = 4, children = infoChildren },
            UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER },
            UI.Label { text = "选择备考策略", fontSize = 12, fontColor = Theme.TEXT_MUTED },
            table.unpack(strategyCards),
        },
    })
end

--- 执行考试（通用逻辑）
---@param member table
---@param exam table
---@param passRate number 最终通过率
---@param strategy table 选择的策略
---@param gameScreen table
function CareerPage.DoExam(member, exam, passRate, strategy, gameScreen)
    if math.random() < passRate then
        -- 高中
        member.identity = exam.result
        GameData.AddResource("fame", exam.famePlus)
        GameData.state.totalExamPasses = GameData.state.totalExamPasses + 1
        GameData.AddLog(member.name .. "以「" .. strategy.name .. "」之法备考，高中" .. exam.result .. "！光宗耀祖！")
        gameScreen.ShowResultPopup("金榜题名",
            member.name .. "高中" .. exam.result .. "！\n备考策略：" .. strategy.name .. "\n声望+" .. exam.famePlus)
    else
        -- 落榜
        local extraMsg = ""
        if strategy.penalty and strategy.penalty.studyLoss then
            member.study = math.max(0, member.study - strategy.penalty.studyLoss)
            extraMsg = "\n押题失败，学识-" .. strategy.penalty.studyLoss
        end
        GameData.AddLog(member.name .. "参加" .. exam.name .. "落榜。")
        gameScreen.ShowResultPopup("名落孙山",
            member.name .. "参加" .. exam.name .. "，不幸落榜。" .. extraMsg .. "\n再接再厉！")
    end
    gameScreen.RefreshAll()
end

return CareerPage
