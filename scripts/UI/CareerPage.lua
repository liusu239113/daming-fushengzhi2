-- ============================================================================
-- 大明浮生志2 - 仕途科举 / 从军 页面
-- 从 GameScreen.lua 拆分出来的全屏页面模块
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")
local AudioManager = require("Systems.AudioManager")
local ExamQuestions = require("Data.ExamQuestions")

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
        -- 确定当前身份对应的已通过等级，取下一级考试（顺序递进：童试→乡试→会试→殿试）
        local passedLevel = 0  -- 0=白丁，1=童生，2=秀才，3=举人，4=进士
        for j, e in ipairs(GameData.EXAM_LEVELS) do
            if e.result == m.identity then passedLevel = j; break end
        end
        local nextLevel = passedLevel + 1
        if nextLevel <= #GameData.EXAM_LEVELS then
            local exam = GameData.EXAM_LEVELS[nextLevel]
            if m.study >= exam.reqStudy then
                availExam = exam
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
                            UI.Label { text = "可参加：" .. availExam.name .. "（答题考试）", fontSize = 10, fontColor = Theme.TEXT_SECONDARY },
                        },
                    },
                    UI.Panel {
                        paddingHorizontal = 12, paddingVertical = 5, borderRadius = 6,
                        backgroundGradient = Theme.GRADIENT_PRIMARY,
                        onClick = function(self)
                            AudioManager.Click()
                            log:Write(LOG_INFO, "[赴考] 点击赴考按钮: " .. m.name .. ", 考试=" .. availExam.name .. ", gameScreen=" .. tostring(gameScreen))
                            local ok, err = pcall(CareerPage.ShowExamStrategyModal, m, availExam, gameScreen)
                            if not ok then
                                log:Write(LOG_ERROR, "[赴考] ShowExamStrategyModal 报错: " .. tostring(err))
                            end
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

    -- ================================================================
    -- 入仕为官
    -- ================================================================
    children[#children + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 12 }

    local MemberData = require("Data.MemberData")
    local officialUnlocked = s.clanRank >= (GameData.CAREER_UNLOCK.exam or 3)
    if not officialUnlocked then
        children[#children + 1] = UI.Panel {
            width = "100%", padding = 12, borderRadius = 8,
            backgroundColor = Theme.BG_INPUT, opacity = 0.6,
            gap = 6, alignItems = "center",
            children = {
                UI.Label { text = "入仕为官 · 需先通过科举", fontSize = 12, fontColor = Theme.GOLD },
                UI.Label { text = "进士可授知县，逐级升迁至布政使", fontSize = 10, fontColor = Theme.TEXT_SECONDARY },
            },
        }
    else
        children[#children + 1] = UI.Label { text = "入仕为官", fontSize = 15, fontColor = Theme.GOLD, marginTop = 4 }
        children[#children + 1] = UI.Label {
            text = "进士可授官，官员月产俸禄与声望，并减免税赋",
            fontSize = 10, fontColor = Theme.TEXT_SECONDARY, whiteSpace = "normal",
        }

        -- 已在任官员
        local currentOfficials = {}
        for _, m in ipairs(GameData.GetAliveMembers()) do
            if m.state == "为官" and m.officialRank then
                currentOfficials[#currentOfficials + 1] = m
            end
        end
        if #currentOfficials > 0 then
            children[#children + 1] = UI.Label { text = "在任官员", fontSize = 12, fontColor = Theme.TEXT_MUTED, marginTop = 4 }
            for _, m in ipairs(currentOfficials) do
                local rankInfo = nil
                for _, r in ipairs(MemberData.OFFICIAL_RANKS) do
                    if r.id == m.officialRank then rankInfo = r; break end
                end
                local rankName = rankInfo and rankInfo.name or m.officialRank
                local tenure = m.officialTenure or 0

                -- 检查是否可以晋升
                local nextRank = nil
                for _, r in ipairs(MemberData.OFFICIAL_RANKS) do
                    if r.reqIdentity == rankName then nextRank = r; break end
                end

                local cardChildren = {
                    UI.Panel {
                        flexDirection = "row", gap = 6, alignItems = "center", flexShrink = 1,
                        children = {
                            UI.Label { text = m.name, fontSize = 13, fontColor = Theme.TEXT_PRIMARY },
                            UI.Panel {
                                paddingHorizontal = 5, paddingVertical = 1, borderRadius = 4,
                                backgroundColor = { 180, 50, 50, 30 },
                                children = { UI.Label { text = rankName, fontSize = 10, fontColor = Theme.RED } },
                            },
                            UI.Label { text = "任期" .. tenure .. "月", fontSize = 10, fontColor = Theme.TEXT_MUTED },
                        },
                    },
                }
                if rankInfo then
                    cardChildren[#cardChildren + 1] = UI.Label {
                        text = "月俸银" .. rankInfo.silver .. " 望+" .. rankInfo.fame
                            .. " 税减" .. math.floor(rankInfo.taxReduce * 100) .. "%",
                        fontSize = 10, fontColor = Theme.TEXT_SECONDARY,
                    }
                end
                -- 晋升按钮
                if nextRank then
                    cardChildren[#cardChildren + 1] = UI.Panel {
                        alignSelf = "flex-end",
                        paddingHorizontal = 12, paddingVertical = 5, borderRadius = 6,
                        backgroundGradient = Theme.GRADIENT_PRIMARY,
                        onClick = function(self)
                            AudioManager.Click()
                            m.identity = nextRank.name
                            m.officialRank = nextRank.id
                            m.officialTenure = 0
                            GameData.AddResource("fame", nextRank.fame)
                            GameData.AddLog(m.name .. "晋升为" .. nextRank.name .. "！")
                            gameScreen.ShowResultPopup("官职晋升",
                                m.name .. "晋升为" .. nextRank.name .. "！\n" .. nextRank.desc)
                            gameScreen.RefreshAll()
                        end,
                        children = { UI.Label { text = "晋升" .. nextRank.name, fontSize = 11, fontColor = Theme.TEXT_WHITE } },
                    }
                end

                children[#children + 1] = UI.Panel {
                    width = "100%", padding = 10, borderRadius = 8,
                    backgroundColor = Theme.BG_INPUT, gap = 4,
                    children = cardChildren,
                }
            end
        end

        -- 可授官的族人（进士/知县/知府 在家状态）
        local appointable = {}
        for _, m in ipairs(GameData.GetAliveMembers()) do
            if m.state == "在家" then
                for _, r in ipairs(MemberData.OFFICIAL_RANKS) do
                    if r.reqIdentity == m.identity then
                        appointable[#appointable + 1] = { member = m, rank = r }
                        break
                    end
                end
            end
        end

        if #appointable > 0 then
            children[#children + 1] = UI.Label { text = "可授官族人", fontSize = 12, fontColor = Theme.TEXT_MUTED, marginTop = 4 }
            for _, ap in ipairs(appointable) do
                local m = ap.member
                local r = ap.rank
                children[#children + 1] = UI.Panel {
                    width = "100%", padding = 10, borderRadius = 8,
                    backgroundColor = Theme.BG_INPUT,
                    flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                    children = {
                        UI.Panel {
                            gap = 2, flexShrink = 1,
                            children = {
                                UI.Panel {
                                    flexDirection = "row", gap = 6, alignItems = "center",
                                    children = {
                                        UI.Label { text = m.name, fontSize = 13, fontColor = Theme.TEXT_PRIMARY },
                                        UI.Label { text = m.identity, fontSize = 10, fontColor = Theme.BLUE },
                                        UI.Label { text = m.age .. "岁", fontSize = 10, fontColor = Theme.TEXT_MUTED },
                                    },
                                },
                                UI.Label {
                                    text = r.desc,
                                    fontSize = 9, fontColor = Theme.TEXT_MUTED, whiteSpace = "normal",
                                },
                            },
                        },
                        UI.Panel {
                            paddingHorizontal = 12, paddingVertical = 5, borderRadius = 6,
                            backgroundGradient = Theme.GRADIENT_PRIMARY,
                            onClick = function(self)
                                AudioManager.Click()
                                m.state = "为官"
                                m.officialRank = r.id
                                m.identity = r.name
                                m.officialTenure = 0
                                GameData.AddResource("fame", r.fame)
                                GameData.AddLog(m.name .. "入仕为官，授" .. r.name .. "。")
                                gameScreen.ShowResultPopup("入仕为官",
                                    m.name .. "被授" .. r.name .. "！\n" .. r.desc)
                                gameScreen.RefreshAll()
                            end,
                            children = { UI.Label { text = "授" .. r.name, fontSize = 12, fontColor = Theme.TEXT_WHITE } },
                        },
                    },
                }
            end
        end

        if #currentOfficials == 0 and #appointable == 0 then
            children[#children + 1] = UI.Label {
                text = "暂无可授官族人（需进士及以上在家状态）",
                fontSize = 11, fontColor = Theme.TEXT_MUTED, marginTop = 2,
            }
        end
    end -- officialUnlocked else end

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
    log:Write(LOG_INFO, "[赴考] ShowExamStrategyModal 进入: member=" .. tostring(member and member.name) .. ", exam=" .. tostring(exam and exam.name))
    local s = GameData.state

    -- 保留基础通过率用于日志记录
    local baseRate = exam.passRate

    -- 广告加持：赠送额外1次免费提示
    local hasAdBoost = s.adExamBoost == true
    local adBoostHints = hasAdBoost and 1 or 0

    -- 三种备考策略（影响答题过程而非通过率）
    local strategies = {
        {
            name = "闭门苦读",
            icon = "书",
            desc = "稳扎稳打，全靠真才实学。",
            rateBonus = 0,
            cost = nil,
            penalty = nil,
            freeHints = 0 + adBoostHints,
            eliminateOne = false,
            color = Theme.TEXT_SECONDARY,
        },
        {
            name = "名师指点",
            icon = "师",
            desc = "延请名师指点，获得1次免费提示机会。消耗银两20。",
            rateBonus = 0,
            cost = { silver = 20 },
            penalty = nil,
            freeHints = 1 + adBoostHints,
            eliminateOne = false,
            color = Theme.BLUE,
        },
        {
            name = "揣摩考题",
            icon = "靶",
            desc = "冒险押题，每题可排除一个错误选项。若落榜学识-5。",
            rateBonus = 0,
            cost = nil,
            penalty = { studyLoss = 5 },
            freeHints = 0 + adBoostHints,
            eliminateOne = true,
            color = Theme.GOLD,
        },
    }

    local modal = UI.Modal {
        title = exam.name .. " · 备考策略",
        size = "md",
    }

    -- 获取考试配置用于顶部展示
    local examCfg = ExamQuestions.GetConfig(exam.id)
    local examTotalQ = examCfg and examCfg.total or 3
    local examPassQ = examCfg and examCfg.pass or 2

    -- 顶部信息
    local infoChildren = {
        UI.Label { text = member.name .. " · 学识" .. member.study, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
        UI.Label { text = "考试形式：共" .. examTotalQ .. "题，答对" .. examPassQ .. "题即可通过", fontSize = 11, fontColor = Theme.TEXT_SECONDARY },
    }
    if hasAdBoost then
        infoChildren[#infoChildren + 1] = UI.Label { text = "科举加持已生效：额外+1次免费提示", fontSize = 10, fontColor = Theme.GOLD }
    end

    -- 广告加持按钮（若尚未激活）—— 改为赠送免费提示
    local AdSystem = require("Systems.AdSystem")
    if not hasAdBoost and AdSystem.IsAvailable("exam_boost") then
        local adRemain = AdSystem.GetRemaining("exam_boost")
        infoChildren[#infoChildren + 1] = UI.Panel {
            width = "100%", height = 30, borderRadius = 6, marginTop = 4,
            backgroundGradient = { direction = "to-right", from = { 180, 130, 50, 255 }, to = { 160, 110, 30, 255 } },
            flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 4,
            onClick = function(self)
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
                UI.Label { text = "▶ 看广告·获得额外提示机会", fontSize = 10, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                UI.Label { text = "(" .. adRemain .. "次)", fontSize = 9, fontColor = { 255, 255, 255, 160 } },
            },
        }
    end

    -- 获取考试配置
    local examConfig = ExamQuestions.GetConfig(exam.id)
    local totalQ = examConfig and examConfig.total or 3
    local passQ = examConfig and examConfig.pass or 2

    -- 策略选项卡片
    local strategyCards = {}
    for _, strat in ipairs(strategies) do
        local canAfford = true
        if strat.cost and strat.cost.silver then
            canAfford = GameData.CanAfford(strat.cost.silver, 0, 0, 0)
        end

        local detailParts = {}
        if strat.freeHints > 0 then detailParts[#detailParts + 1] = "免费提示x" .. strat.freeHints end
        if strat.eliminateOne then detailParts[#detailParts + 1] = "每题排除1个错项" end
        if strat.cost then detailParts[#detailParts + 1] = "银两-" .. strat.cost.silver end
        if strat.penalty then detailParts[#detailParts + 1] = "落榜学识-" .. strat.penalty.studyLoss end
        local detailText = #detailParts > 0 and table.concat(detailParts, "  ") or "无额外效果"

        strategyCards[#strategyCards + 1] = UI.Panel {
            width = "100%", padding = 10, borderRadius = 8,
            backgroundColor = canAfford and Theme.BG_INPUT or { 200, 195, 185, 120 },
            borderWidth = 1, borderColor = canAfford and strat.color or Theme.BORDER,
            opacity = canAfford and 1.0 or 0.5, gap = 4,
            onClick = canAfford and function(self)
                AudioManager.Click()
                modal:Close()
                -- 扣除费用
                if strat.cost and strat.cost.silver then
                    GameData.SpendResources(strat.cost.silver, 0, 0, 0)
                end
                -- 清除广告加持标记（一次性）
                s.adExamBoost = nil
                -- 进入答题考试
                CareerPage.DoExam(member, exam, baseRate, strat, gameScreen)
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
                        UI.Label { text = "共" .. totalQ .. "题·答对" .. passQ .. "题过", fontSize = 12, fontColor = Theme.GOLD, fontWeight = "bold" },
                    },
                },
            },
        }
    end

    local contentChildren = {
        UI.Panel { width = "100%", gap = 4, children = infoChildren },
        UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER },
        UI.Label { text = "选择备考策略", fontSize = 12, fontColor = Theme.TEXT_MUTED },
    }
    for _, card in ipairs(strategyCards) do
        contentChildren[#contentChildren + 1] = card
    end
    modal:AddContent(UI.ScrollView {
        width = "100%", maxHeight = 400, scrollY = true, bounces = true, showScrollbar = true,
        children = {
            UI.Panel { width = "100%", gap = 8, padding = 4, children = contentChildren },
        },
    })
    modal:Open()
end

--- 执行考试（答题模式）
---@param member table
---@param exam table
---@param passRate number 基础通过率（保留用于日志）
---@param strategy table 选择的策略
---@param gameScreen table
function CareerPage.DoExam(member, exam, passRate, strategy, gameScreen)
    local config = ExamQuestions.GetConfig(exam.id)
    if not config then
        log:Write(LOG_ERROR, "[科举] 未找到考试配置: " .. tostring(exam.id))
        return
    end

    local questions = ExamQuestions.Pick(exam.id, config.total)
    if #questions < config.total then
        log:Write(LOG_WARNING, "[科举] 题库不足: 需要" .. config.total .. "题，只有" .. #questions .. "题")
    end

    -- 答题状态
    local state = {
        questions = questions,
        current = 1,          -- 当前题号（1开始）
        correct = 0,          -- 答对数
        total = #questions,
        passReq = config.pass,
        freeHints = strategy.freeHints or 0,  -- 免费提示次数
        eliminateOne = strategy.eliminateOne or false,  -- 是否排除一个错项
        hintUsed = {},        -- 已用提示的题号
        eliminated = {},      -- 已排除选项的题号 { [qIndex] = eliminatedOptIndex }
        isRetry = false,      -- 是否是重考
    }

    CareerPage._showQuestion(state, member, exam, strategy, gameScreen)
end

--- 显示当前题目（内部函数）
function CareerPage._showQuestion(state, member, exam, strategy, gameScreen)
    local q = state.questions[state.current]
    if not q then return end

    local AdSystem = require("Systems.AdSystem")

    local modal = UI.Modal {
        title = exam.name .. " · 第" .. state.current .. "/" .. state.total .. "题",
        size = "md",
        closeOnOverlay = false,
    }

    -- 进度条信息
    local progressText = "答对 " .. state.correct .. "/" .. state.passReq .. "（还需 " .. math.max(0, state.passReq - state.correct) .. " 题）"
    local remaining = state.total - state.current + 1
    local canStillPass = (state.correct + remaining) >= state.passReq

    -- 已排除的选项索引
    local eliminatedOpt = state.eliminated[state.current]

    -- 如果策略是"揣摩考题"且尚未排除，自动排除一个错误选项
    if state.eliminateOne and not eliminatedOpt then
        local wrongOpts = {}
        for i = 1, 4 do
            if i ~= q.answer then wrongOpts[#wrongOpts + 1] = i end
        end
        if #wrongOpts > 0 then
            eliminatedOpt = wrongOpts[math.random(1, #wrongOpts)]
            state.eliminated[state.current] = eliminatedOpt
        end
    end

    -- 构建选项按钮
    local optionLabels = { "甲", "乙", "丙", "丁" }
    local optChildren = {}
    for i = 1, 4 do
        local isEliminated = (eliminatedOpt == i)
        local optText = optionLabels[i] .. ". " .. q.opts[i]

        if isEliminated then
            -- 被排除的选项：灰色删除线样式
            optChildren[#optChildren + 1] = UI.Panel {
                width = "100%", padding = 10, borderRadius = 8,
                backgroundColor = { 100, 100, 100, 20 },
                opacity = 0.4,
                children = {
                    UI.Label { text = optText .. "（已排除）", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                },
            }
        else
            -- 正常选项
            optChildren[#optChildren + 1] = UI.Panel {
                width = "100%", padding = 10, borderRadius = 8,
                backgroundColor = Theme.BG_INPUT,
                borderWidth = 1, borderColor = Theme.BORDER,
                onClick = function(self)
                    AudioManager.Click()
                    modal:Close()
                    -- 判断对错
                    local isCorrect = (i == q.answer)
                    if isCorrect then
                        state.correct = state.correct + 1
                    end
                    -- 显示答题结果
                    CareerPage._showAnswerResult(state, isCorrect, q, i, member, exam, strategy, gameScreen)
                end,
                children = {
                    UI.Label { text = optText, fontSize = 12, fontColor = Theme.TEXT_PRIMARY, whiteSpace = "normal" },
                },
            }
        end
    end

    -- 提示按钮区域
    local hintChildren = {}
    local hintShowing = state.hintUsed[state.current]

    if hintShowing then
        -- 已经显示了提示
        hintChildren[#hintChildren + 1] = UI.Panel {
            width = "100%", padding = 8, borderRadius = 6,
            backgroundColor = { 255, 200, 50, 25 },
            borderWidth = 1, borderColor = { 255, 200, 50, 80 },
            children = {
                UI.Label { text = "提示：" .. q.hint, fontSize = 11, fontColor = Theme.GOLD, whiteSpace = "normal" },
            },
        }
    else
        -- 免费提示
        if state.freeHints > 0 then
            hintChildren[#hintChildren + 1] = UI.Panel {
                height = 28, paddingHorizontal = 12, borderRadius = 6,
                backgroundColor = { 66, 133, 244, 30 },
                borderWidth = 1, borderColor = Theme.BLUE,
                flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 4,
                onClick = function(self)
                    AudioManager.Click()
                    state.hintUsed[state.current] = true
                    state.freeHints = state.freeHints - 1
                    modal:Close()
                    CareerPage._showQuestion(state, member, exam, strategy, gameScreen)
                end,
                children = {
                    UI.Label { text = "名师提示（免费x" .. state.freeHints .. "）", fontSize = 10, fontColor = Theme.BLUE },
                },
            }
        end
        -- 广告提示
        if AdSystem.IsAvailable("exam_hint") then
            local adRemain = AdSystem.GetRemaining("exam_hint")
            hintChildren[#hintChildren + 1] = UI.Panel {
                height = 28, paddingHorizontal = 12, borderRadius = 6,
                backgroundGradient = { direction = "to-right", from = { 180, 130, 50, 255 }, to = { 160, 110, 30, 255 } },
                flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 4,
                onClick = function(self)
                    AudioManager.Click()
                    AdSystem.ShowRewardAd("exam_hint", function()
                        state.hintUsed[state.current] = true
                        modal:Close()
                        CareerPage._showQuestion(state, member, exam, strategy, gameScreen)
                    end, function(msg)
                        log:Write(LOG_WARNING, "[科举] 提示广告失败: " .. tostring(msg))
                    end)
                end,
                children = {
                    UI.Label { text = "▶ 看广告获取提示", fontSize = 10, fontColor = Theme.TEXT_WHITE },
                    UI.Label { text = "(" .. adRemain .. "次)", fontSize = 9, fontColor = { 255, 255, 255, 160 } },
                },
            }
        end
    end

    -- 组装内容
    local contentItems = {
        -- 进度
        UI.Panel {
            width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
            children = {
                UI.Label { text = progressText, fontSize = 11, fontColor = canStillPass and Theme.TEXT_SECONDARY or Theme.RED },
                UI.Label { text = member.name .. " · " .. exam.name, fontSize = 10, fontColor = Theme.TEXT_MUTED },
            },
        },
        -- 进度条
        UI.Panel {
            width = "100%", height = 4, borderRadius = 2, backgroundColor = { 255, 255, 255, 30 },
            children = {
                UI.Panel {
                    width = math.floor((state.current - 1) / state.total * 100) .. "%",
                    height = "100%", borderRadius = 2,
                    backgroundGradient = Theme.GRADIENT_PRIMARY,
                },
            },
        },
        -- 题目
        UI.Panel {
            width = "100%", padding = 12, borderRadius = 8,
            backgroundColor = { 255, 255, 255, 8 },
            children = {
                UI.Label { text = q.q, fontSize = 14, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold", whiteSpace = "normal" },
            },
        },
    }

    -- 提示区
    if #hintChildren > 0 then
        for _, hc in ipairs(hintChildren) do
            contentItems[#contentItems + 1] = hc
        end
    end

    -- 选项
    for _, oc in ipairs(optChildren) do
        contentItems[#contentItems + 1] = oc
    end

    modal:AddContent(UI.ScrollView {
        width = "100%", maxHeight = 420, scrollY = true, bounces = true, showScrollbar = true,
        children = {
            UI.Panel { width = "100%", gap = 8, padding = 4, children = contentItems },
        },
    })
    modal:Open()
end

--- 显示单题答案结果（内部函数）
function CareerPage._showAnswerResult(state, isCorrect, question, chosenIdx, member, exam, strategy, gameScreen)
    local optionLabels = { "甲", "乙", "丙", "丁" }

    local modal = UI.Modal {
        title = isCorrect and "答对了！" or "答错了",
        size = "sm",
        closeOnOverlay = false,
    }

    local resultChildren = {}

    if isCorrect then
        resultChildren[#resultChildren + 1] = UI.Label {
            text = "回答正确！",
            fontSize = 16, fontColor = Theme.GREEN, fontWeight = "bold",
        }
    else
        resultChildren[#resultChildren + 1] = UI.Label {
            text = "回答错误",
            fontSize = 16, fontColor = Theme.RED, fontWeight = "bold",
        }
        resultChildren[#resultChildren + 1] = UI.Label {
            text = "你选了：" .. optionLabels[chosenIdx] .. ". " .. question.opts[chosenIdx],
            fontSize = 11, fontColor = Theme.TEXT_MUTED, whiteSpace = "normal",
        }
        resultChildren[#resultChildren + 1] = UI.Label {
            text = "正确答案：" .. optionLabels[question.answer] .. ". " .. question.opts[question.answer],
            fontSize = 11, fontColor = Theme.GREEN, whiteSpace = "normal",
        }
    end

    -- 当前进度
    resultChildren[#resultChildren + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 4 }
    resultChildren[#resultChildren + 1] = UI.Label {
        text = "已答对 " .. state.correct .. "/" .. state.passReq .. "  剩余 " .. (state.total - state.current) .. " 题",
        fontSize = 11, fontColor = Theme.TEXT_SECONDARY,
    }

    modal:AddContent(UI.Panel {
        width = "100%", gap = 6, padding = 8, alignItems = "center",
        children = resultChildren,
    })

    -- 判断是否还需要继续答题
    local remaining = state.total - state.current
    local canStillPass = (state.correct + remaining) >= state.passReq
    local alreadyPassed = state.correct >= state.passReq

    if state.current >= state.total or alreadyPassed or not canStillPass then
        -- 考试结束
        modal:SetFooter(UI.Panel {
            width = "100%", justifyContent = "center", alignItems = "center",
            children = {
                UI.Panel {
                    paddingHorizontal = 24, paddingVertical = 8, borderRadius = 8,
                    backgroundGradient = Theme.GRADIENT_PRIMARY,
                    onClick = function(self)
                        AudioManager.Click()
                        modal:Close()
                        CareerPage._showExamResult(state, member, exam, strategy, gameScreen)
                    end,
                    children = { UI.Label { text = "查看结果", fontSize = 13, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" } },
                },
            },
        })
    else
        -- 继续下一题
        modal:SetFooter(UI.Panel {
            width = "100%", justifyContent = "center", alignItems = "center",
            children = {
                UI.Panel {
                    paddingHorizontal = 24, paddingVertical = 8, borderRadius = 8,
                    backgroundGradient = Theme.GRADIENT_PRIMARY,
                    onClick = function(self)
                        AudioManager.Click()
                        modal:Close()
                        state.current = state.current + 1
                        CareerPage._showQuestion(state, member, exam, strategy, gameScreen)
                    end,
                    children = { UI.Label { text = "下一题", fontSize = 13, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" } },
                },
            },
        })
    end

    modal:Open()
end

--- 显示考试最终结果（内部函数）
function CareerPage._showExamResult(state, member, exam, strategy, gameScreen)
    local passed = state.correct >= state.passReq
    local AdSystem = require("Systems.AdSystem")

    if passed then
        -- 高中！
        member.identity = exam.result
        GameData.AddResource("fame", exam.famePlus)
        GameData.state.totalExamPasses = (GameData.state.totalExamPasses or 0) + 1
        GameData.AddLog(member.name .. "以「" .. strategy.name .. "」之法备考，答对" .. state.correct .. "/" .. state.total .. "题，高中" .. exam.result .. "！光宗耀祖！")
        gameScreen.ShowResultPopup("金榜题名",
            member.name .. "高中" .. exam.result .. "！\n\n"
            .. "答题成绩：" .. state.correct .. "/" .. state.total .. "（需" .. state.passReq .. "题）\n"
            .. "备考策略：" .. strategy.name .. "\n"
            .. "声望+" .. exam.famePlus)
    else
        -- 落榜
        local extraMsg = ""
        if strategy.penalty and strategy.penalty.studyLoss then
            member.study = math.max(0, member.study - strategy.penalty.studyLoss)
            extraMsg = "\n揣摩考题失败，学识-" .. strategy.penalty.studyLoss
        end
        GameData.AddLog(member.name .. "参加" .. exam.name .. "，答对" .. state.correct .. "/" .. state.total .. "题，落榜。")

        -- 显示落榜结果 + 广告重考选项
        local modal = UI.Modal {
            title = "名落孙山",
            size = "md",
            closeOnOverlay = false,
        }

        local resultChildren = {
            UI.Label { text = member.name .. " " .. exam.name .. " 落榜", fontSize = 16, fontColor = Theme.RED, fontWeight = "bold" },
            UI.Label {
                text = "答题成绩：" .. state.correct .. "/" .. state.total .. "（需" .. state.passReq .. "题）" .. extraMsg,
                fontSize = 12, fontColor = Theme.TEXT_SECONDARY, whiteSpace = "normal",
            },
        }

        -- 广告重考按钮
        if not state.isRetry and AdSystem.IsAvailable("exam_retry") then
            local adRemain = AdSystem.GetRemaining("exam_retry")
            resultChildren[#resultChildren + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 4 }
            resultChildren[#resultChildren + 1] = UI.Label { text = "再给一次机会？", fontSize = 12, fontColor = Theme.TEXT_MUTED }
            resultChildren[#resultChildren + 1] = UI.Panel {
                width = "100%", height = 36, borderRadius = 8,
                backgroundGradient = { direction = "to-right", from = { 180, 130, 50, 255 }, to = { 160, 110, 30, 255 } },
                flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 6,
                onClick = function(self)
                    AudioManager.Click()
                    AdSystem.ShowRewardAd("exam_retry", function()
                        modal:Close()
                        -- 重新抽题重考
                        local config = ExamQuestions.GetConfig(exam.id)
                        local newQuestions = ExamQuestions.Pick(exam.id, config.total)
                        local newState = {
                            questions = newQuestions,
                            current = 1,
                            correct = 0,
                            total = #newQuestions,
                            passReq = config.pass,
                            freeHints = strategy.freeHints or 0,
                            eliminateOne = strategy.eliminateOne or false,
                            hintUsed = {},
                            eliminated = {},
                            isRetry = true,  -- 标记为重考，不再允许第二次重考
                        }
                        GameData.AddLog(member.name .. "观看广告获得" .. exam.name .. "重考机会！")
                        CareerPage._showQuestion(newState, member, exam, strategy, gameScreen)
                    end, function(msg)
                        log:Write(LOG_WARNING, "[科举] 重考广告失败: " .. tostring(msg))
                    end)
                end,
                children = {
                    UI.Label { text = "▶ 看广告·重考一次", fontSize = 12, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                    UI.Label { text = "（重新出题 " .. adRemain .. "次）", fontSize = 10, fontColor = { 255, 255, 255, 160 } },
                },
            }
        end

        modal:AddContent(UI.Panel {
            width = "100%", gap = 8, padding = 8, alignItems = "center",
            children = resultChildren,
        })

        modal:SetFooter(UI.Panel {
            width = "100%", justifyContent = "center", alignItems = "center",
            children = {
                UI.Panel {
                    paddingHorizontal = 24, paddingVertical = 8, borderRadius = 8,
                    backgroundColor = Theme.BG_INPUT,
                    borderWidth = 1, borderColor = Theme.BORDER,
                    onClick = function(self)
                        AudioManager.Click()
                        modal:Close()
                        gameScreen.RefreshAll()
                    end,
                    children = { UI.Label { text = "接受结果", fontSize = 13, fontColor = Theme.TEXT_SECONDARY } },
                },
            },
        })

        modal:Open()
        return  -- 落榜弹窗自带刷新，不走下面的 RefreshAll
    end

    gameScreen.RefreshAll()
end

return CareerPage
