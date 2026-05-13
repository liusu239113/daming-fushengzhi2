-- ============================================================================
-- 大明浮生志2 - 游戏主界面（族谱中心 + 全屏页面切换）
-- 顶部资源栏 + 中央内容区(族谱/宗祠/族人/产业/仕途/族规/事件) + 浮动时间控制 + 底部导航
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")
local EraSystem = require("Data.EraSystem")
local MonthlyUpdate = require("Systems.MonthlyUpdate")
local SaveSystem = require("Systems.SaveSystem")
local AudioManager = require("Systems.AudioManager")
local Toast = require("Systems.Toast")
local FamilyTree = require("UI.FamilyTree")
local CareerPage = require("UI.CareerPage")
local AcademyPage = require("UI.AcademyPage")
local ExpeditionPage = require("UI.ExpeditionPage")
local InventoryPage = require("UI.InventoryPage")
local ModalDialogs = require("UI.ModalDialogs")
local WeatherSystem = require("Systems.WeatherSystem")
local BattlePrepPage = require("UI.BattlePrepPage")
local BattleScene = require("Battle.BattleScene")
local RivalClans = require("Data.RivalClans")
local IndustryPage = require("UI.IndustryPage")
local MemberPage = require("UI.MemberPage")
local EventPage = require("UI.EventPage")
local MarketPage = require("UI.MarketPage")
local AdSystem = require("Systems.AdSystem")

local GameScreen = {}

-- 注入模态对话框方法到 GameScreen
ModalDialogs.Init(GameScreen)

-- ============================================================================
-- 状态变量
-- ============================================================================

local gameRoot_ = nil
local contentArea_ = nil    -- 中央内容容器
local topBar_ = nil
local currentTab_ = "tree"  -- "tree"|"clan"|"members"|"industry"|"career"|"academy"|"expedition"|"inventory"|"rules"|"events"

-- 时间自动推进
local gameSpeed_ = 0        -- 0=暂停, 1=1x, 2=2x, 3=3x
local prevGameSpeed_ = 1    -- 事件暂停前的速度（用于恢复）
local timeAccum_ = 0        -- 时间累加器
local updateSubscribed_ = false
local eventPopupShown_ = false  -- 事件弹窗是否已显示
local grainReliefShownMonth_ = nil  -- 粮食急救弹窗已显示的月份标记（避免同月重复弹）

local SPEED_INTERVAL = {
    [0] = 999999,   -- 暂停
    [1] = 3.0,      -- 1x: 3秒/月
    [2] = 1.5,      -- 2x: 1.5秒/月
    [3] = 0.75,     -- 3x: 0.75秒/月
}
local SPEED_LABELS = { [0] = "停", [1] = "一速", [2] = "二速", [3] = "三速" }
local SPEED_ICONS = {
    [0] = Theme.IMG.ICON_PAUSE,
    [1] = Theme.IMG.ICON_PLAY1X,
    [2] = Theme.IMG.ICON_PLAY2X,
    [3] = Theme.IMG.ICON_PLAY3X,
}

-- Forward declarations
local RefreshTimeControl
local PageTitle

--- 暂停游戏（设置菜单等场景使用，不自动恢复）
function GameScreen.PauseGame()
    if gameSpeed_ ~= 0 then
        prevGameSpeed_ = gameSpeed_
    end
    gameSpeed_ = 0
    RefreshTimeControl()
end

--- 恢复游戏速度（modal/overlay 关闭时统一调用）
--- 将速度恢复到暂停前的值，至少 1 倍速
function GameScreen.ResumeSpeed()
    if gameSpeed_ == 0 then
        gameSpeed_ = (prevGameSpeed_ > 0) and prevGameSpeed_ or 1
        RefreshTimeControl()
    end
end

-- ============================================================================
-- 顶部资源栏
-- ============================================================================

--- 数字格式化：>=1000 显示为 x.xk
local function FormatNumber(n)
    if n >= 10000 then
        return string.format("%.1fw", n / 10000)
    elseif n >= 1000 then
        return string.format("%.1fk", n / 1000)
    else
        return tostring(n)
    end
end

local function CreateResourceItem(img, value, color, id)
    return UI.Panel {
        flexDirection = "row", alignItems = "center", gap = 2,
        backgroundColor = { 255, 252, 240, 160 },
        borderRadius = 6, paddingHorizontal = 4, paddingVertical = 2,
        children = {
            UI.Panel {
                width = 20, height = 20,
                backgroundImage = img, backgroundFit = "contain",
            },
            UI.Label { id = id, text = FormatNumber(value), fontSize = 13, fontColor = color, fontWeight = "bold" },
        },
    }
end

local function CreateTopBar()
    local s = GameData.state
    return UI.Panel {
        id = "topBar",
        width = "100%",
        backgroundColor = { 248, 243, 228, 255 },
        backgroundImage = Theme.IMG.BG_TOP_BAR, backgroundFit = "fill",
        borderBottomWidth = 1,
        borderBottomColor = Theme.BORDER_GOLD,
        padding = { 8, 50, 6, 12 },  -- 右侧留 50px 避开 TapTap 悬浮窗
        gap = 4,
        overflow = "hidden",
        children = {
            -- 第一行：宗族信息 + 年份
            UI.Panel {
                flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                children = {
                    UI.Panel {
                        flexDirection = "row", alignItems = "center", gap = 6,
                        backgroundColor = { 255, 252, 240, 160 },
                        borderRadius = 6, paddingHorizontal = 6, paddingVertical = 2,
                        children = {
                            UI.Label { id = "clanNameLabel", text = s.clanName, fontSize = 14, fontColor = Theme.GOLD_DARK, fontWeight = "bold" },
                            UI.Panel {
                                paddingHorizontal = 6, paddingVertical = 2,
                                borderRadius = 4, backgroundColor = { 56, 168, 120, 40 },
                                borderWidth = 1, borderColor = Theme.GOLD_DARK,
                                children = {
                                    UI.Label { id = "clanRankLabel", text = GameData.GetClanRankName(), fontSize = 10, fontColor = Theme.GOLD_DARK, fontWeight = "bold" },
                                },
                            },
                        },
                    },
                    UI.Panel {
                        flexDirection = "row", alignItems = "center", gap = 5,
                        backgroundColor = { 255, 252, 240, 160 },
                        borderRadius = 6, paddingHorizontal = 5, paddingVertical = 2,
                        children = {
                            UI.Label { id = "yearLabel", text = EraSystem.GetYearLabel(s.year), fontSize = 12, fontColor = Theme.TEXT_PRIMARY, fontWeight = "bold" },
                            UI.Label { id = "monthLabel", text = s.month .. "月", fontSize = 11, fontColor = Theme.TEXT_SECONDARY, fontWeight = "bold" },
                            UI.Label { id = "weatherLabel", text = WeatherSystem.GetDisplayText(), fontSize = 10, fontColor = Theme.TEXT_SECONDARY },
                            -- 暂停菜单按钮
                            UI.Panel {
                                flexDirection = "row", alignItems = "center", gap = 3,
                                paddingHorizontal = 6, paddingVertical = 3,
                                borderRadius = 5,
                                backgroundColor = { 245, 240, 225, 200 },
                                borderWidth = 1, borderColor = { 180, 160, 120, 120 },
                                onClick = function(self)
                                    AudioManager.Click()
                                    if ShowPauseMenu then ShowPauseMenu() end
                                end,
                                children = {
                                    UI.Panel {
                                        width = 16, height = 16,
                                        backgroundImage = "image/icon_gear_black_20260512150437.png",
                                        backgroundFit = "contain",
                                    },
                                    UI.Label { text = "设置", fontSize = 10, fontColor = Theme.TEXT_PRIMARY },
                                },
                            },
                        },
                    },
                },
            },
            -- 第二行：资源 + 人口（金色徽章图标 + 格式化数字）
            UI.Panel {
                flexDirection = "row", justifyContent = "space-around", alignItems = "center", marginTop = 2,
                children = {
                    CreateResourceItem(Theme.IMG.RES_SILVER, s.silver, Theme.SILVER_COLOR, "resSilver"),
                    CreateResourceItem(Theme.IMG.RES_GRAIN, s.grain, Theme.GRAIN_COLOR, "resGrain"),
                    CreateResourceItem(Theme.IMG.RES_CLOTH, s.cloth, Theme.CLOTH_COLOR, "resCloth"),
                    CreateResourceItem(Theme.IMG.RES_FAME, s.fame, Theme.FAME_COLOR, "resFame"),
                    CreateResourceItem(Theme.IMG.RES_POP, #GameData.GetAliveMembers(), Theme.GREEN, "resPop"),
                },
            },
            -- 第三行：钱庄入口（有贷款或银两不足时显示）
            (function()
                local loans = AdSystem.GetLoans()
                local totalDebt, monthlyInt = AdSystem.GetTotalDebt()
                local showLoanBar = #loans > 0 or s.silver < 30
                if not showLoanBar then return UI.Panel { id = "loanBar", width = 0, height = 0 } end
                local barText = #loans > 0
                    and ("钱庄 · 在贷" .. #loans .. "笔 · 月息" .. monthlyInt .. "两")
                    or "银两不足？点击钱庄借贷"
                return UI.Panel {
                    id = "loanBar",
                    width = "100%", height = 22, marginTop = 2,
                    flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 6,
                    borderRadius = 4,
                    backgroundColor = #loans > 0 and { 200, 160, 40, 30 } or { 60, 160, 120, 25 },
                    onClick = function(self)
                        AudioManager.Click()
                        GameScreen.ShowLoanDialog()
                    end,
                    children = {
                        UI.Label { text = "贷", fontSize = 11 },
                        UI.Label { text = barText, fontSize = 10, fontColor = #loans > 0 and Theme.GOLD_DARK or Theme.GREEN },
                        UI.Label { text = "›", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                    },
                }
            end)(),
        },
    }
end

-- ============================================================================
-- 浮动时间控制（右下角绝对定位）
-- ============================================================================

local function CreateSpeedBtn(speed)
    local isActive = (gameSpeed_ == speed)
    local needAd = (speed >= 2) and not AdSystem.IsSpeedUnlocked()
    local btnOpacity = isActive and 1.0 or 0.4

    return UI.Panel {
        id = "speedBtn_" .. speed,
        width = 34, height = 34,
        justifyContent = "center", alignItems = "center",
        onClick = function(self)
            local s = GameData.state
            if s.gameEnded then return end
            AudioManager.Click()

            -- 安全网：清除可能的阻塞状态
            if AdSystem.isShowingAd then
                AdSystem.isShowingAd = false
                AdSystem._adStartTime = nil
            end
            if eventPopupShown_ then
                local overlay = gameRoot_ and gameRoot_:FindById("eventPopupOverlay")
                if not overlay then eventPopupShown_ = false end
            end

            if speed >= 2 and not AdSystem.IsSpeedUnlocked() then
                -- 需要看广告解锁加速
                AdSystem.UnlockSpeed(function(success, msg)
                    if success then
                        gameSpeed_ = speed
                        if speed > 0 then prevGameSpeed_ = speed end
                        timeAccum_ = 0
                        Toast.Show("加速已领取")
                        RefreshTimeControl()
                    else
                        Toast.Show(msg or "广告播放失败")
                    end
                end)
                return
            end

            gameSpeed_ = speed
            if speed > 0 then prevGameSpeed_ = speed end
            timeAccum_ = 0
            RefreshTimeControl()
        end,
        children = {
            UI.Panel {
                width = 30, height = 30,
                backgroundImage = SPEED_ICONS[speed], backgroundFit = "contain",
                opacity = btnOpacity,
                borderWidth = isActive and 2 or 0,
                borderColor = Theme.GOLD,
                borderRadius = 15,
            },
            -- 未解锁时显示小锁标
            needAd and UI.Panel {
                position = "absolute", top = -2, right = -2,
                width = 14, height = 14, borderRadius = 7,
                backgroundColor = { 255, 180, 0, 230 },
                justifyContent = "center", alignItems = "center",
                children = {
                    UI.Label { text = "AD", fontSize = 7, fontColor = { 0, 0, 0, 255 }, fontWeight = "bold" },
                },
            } or nil,
        },
    }
end

local function CreateTimeControl()
    local s = GameData.state
    local isRunning = (gameSpeed_ > 0)
    local speedText = isRunning and (gameSpeed_ .. "x 时光流转中") or "已暂停"
    local dotColor = isRunning and Theme.GREEN or Theme.RED

    -- 闪烁指示灯（运行时脉冲动画）
    local indicatorDot = UI.Panel {
        id = "timeIndicatorDot",
        width = 8, height = 8, borderRadius = 4,
        backgroundColor = dotColor,
        opacity = isRunning and 1.0 or 0.6,
    }

    local timePanel = UI.Panel {
        id = "timeControl",
        position = "absolute",
        bottom = 64, right = 8,
        backgroundImage = Theme.IMG.BG_CONTROL_PANEL, backgroundFit = "contain",
        borderRadius = 12,
        padding = { 8, 10, 8, 10 },
        gap = 4, alignItems = "center",
        children = {
            -- 状态行：闪烁圆点 + 年月 + 状态文字
            UI.Panel {
                flexDirection = "row", gap = 4, alignItems = "center",
                children = {
                    indicatorDot,
                    UI.Label {
                        id = "timeDisplayLabel",
                        text = EraSystem.GetYearLabel(s.year) .. s.month .. "月",
                        fontSize = 12, fontColor = Theme.GOLD, fontWeight = "bold",
                    },
                },
            },
            -- 流动状态提示
            UI.Label {
                text = speedText,
                fontSize = 9,
                fontColor = isRunning and Theme.GREEN or Theme.RED,
            },
            -- 速度按钮行
            UI.Panel {
                flexDirection = "row", gap = 2, alignItems = "center",
                children = { CreateSpeedBtn(0), CreateSpeedBtn(1), CreateSpeedBtn(2), CreateSpeedBtn(3) },
            },
            -- GM 测试按钮（维护用，发布时隐藏）
            -- UI.Panel {
            --     width = "100%", height = 22, borderRadius = 4, marginTop = 2,
            --     backgroundColor = { 180, 60, 60, 200 },
            --     justifyContent = "center", alignItems = "center",
            --     onClick = function(self)
            --         AudioManager.Click()
            --         GameScreen.ShowGMBattlePanel()
            --     end,
            --     children = {
            --         UI.Label { text = "GM", fontSize = 10, fontColor = { 255, 255, 255, 255 }, fontWeight = "bold" },
            --     },
            -- },
        },
    }

    -- 运行中给指示灯加脉冲动画
    if isRunning then
        indicatorDot:Animate({
            keyframes = {
                { opacity = 1.0, scale = 1.0 },
                { opacity = 0.3, scale = 0.7 },
                { opacity = 1.0, scale = 1.0 },
            },
            duration = 1.2,
            iterations = -1,   -- 无限循环
            easing = "easeInOut",
        })
    end

    return timePanel
end

RefreshTimeControl = function()
    if not gameRoot_ then return end
    -- 完全重建时间控制面板（图片按钮状态通过重建刷新）
    local container = gameRoot_:FindById("middleContainer")
    if not container then return end
    local oldTC = container:FindById("timeControl")
    if oldTC then
        container:RemoveChild(oldTC)
    end
    container:AddChild(CreateTimeControl())
end

-- ============================================================================
-- 事件弹窗（在当前页面上覆盖显示，不切换 tab）
-- ============================================================================

function GameScreen.ShowPendingEventPopup()
    local s = GameData.state

    -- 先清除任何残留的 Toast 弹窗，避免遮挡事件弹窗
    Toast.DismissPopup()

    -- 先移除旧的事件弹窗 overlay（防止重复叠加）
    if gameRoot_ then
        local oldOverlay = gameRoot_:FindById("eventPopupOverlay")
        if oldOverlay then
            gameRoot_:RemoveChild(oldOverlay)
        end
    end

    if #s.pendingEvents == 0 then
        eventPopupShown_ = false
        -- 恢复之前的速度（至少恢复到 1 倍速，防止 prevGameSpeed_=0 导致卡死）
        gameSpeed_ = (prevGameSpeed_ > 0) and prevGameSpeed_ or 1
        RefreshTimeControl()
        GameScreen.RefreshAll()
        return
    end

    eventPopupShown_ = true

    -- 取第一个事件
    local evt = s.pendingEvents[1]

    -- 无效事件保护：缺少title/desc的事件直接跳过
    if not evt or not evt.title or not evt.desc then
        log:Write(LOG_WARNING, "Skipping invalid pending event: " .. tostring(evt and evt.title))
        table.remove(s.pendingEvents, 1)
        GameScreen.ShowPendingEventPopup()
        return
    end

    -- 族长继承事件：使用专用弹窗
    if evt.type == "succession" then
        table.remove(s.pendingEvents, 1)
        GameScreen.ShowSuccessionDialog(evt.deadPatriarchId)
        return
    end

    -- 构建选项按钮
    local choiceChildren = {}
    if evt.choices then
        for ci, choice in ipairs(evt.choices) do
            choiceChildren[#choiceChildren + 1] = UI.Panel {
                width = "100%", height = 40, borderRadius = 6,
                backgroundColor = Theme.BG_INPUT, borderWidth = 1, borderColor = Theme.BORDER_GOLD,
                justifyContent = "center", alignItems = "center",
                onPointerDown = function(self)
                    AudioManager.Select()
                    -- 声明式费用检查：如果选项有 cost 字段，先检查资源是否足够
                    if choice.cost then
                        if not GameData.TrySpend(
                            choice.cost.silver, choice.cost.grain,
                            choice.cost.cloth, choice.cost.fame
                        ) then
                            return -- 资源不足，不关闭事件，不执行效果
                        end
                    end
                    -- 先移除事件和overlay，防止effect()报错导致卡死
                    table.remove(s.pendingEvents, 1)
                    SaveSystem.AutoSave()
                    local overlay = gameRoot_:FindById("eventPopupOverlay")
                    if overlay then gameRoot_:RemoveChild(overlay) end
                    -- 安全执行效果函数
                    local ok, err = pcall(choice.effect)
                    if not ok then
                        log:Write(LOG_WARNING, "Event effect error: " .. tostring(err))
                    end
                    -- 处理下一个事件或恢复
                    GameScreen.ShowPendingEventPopup()
                end,
                children = { UI.Label { text = choice.text, fontSize = 13, fontColor = Theme.GOLD } },
            }
        end
    else
        -- 无选项事件，点击确认即可
        choiceChildren[#choiceChildren + 1] = UI.Panel {
            width = 120, height = 36, borderRadius = 6,
            backgroundGradient = Theme.GRADIENT_PRIMARY,
            justifyContent = "center", alignItems = "center",
            onPointerDown = function(self)
                AudioManager.Click()
                table.remove(s.pendingEvents, 1)
                SaveSystem.AutoSave()
                local overlay = gameRoot_:FindById("eventPopupOverlay")
                if overlay then gameRoot_:RemoveChild(overlay) end
                GameScreen.ShowPendingEventPopup()
            end,
            children = { UI.Label { text = "知道了", fontSize = 13, fontColor = Theme.TEXT_WHITE } },
        }
    end

    -- 弹窗 overlay
    local overlay = UI.Panel {
        id = "eventPopupOverlay",
        width = "100%", height = "100%",
        position = "absolute", left = 0, top = 0, zIndex = 900,
        justifyContent = "center", alignItems = "center",
        backgroundColor = { 0, 0, 0, 120 },
        children = {
            UI.Panel {
                width = 280, borderRadius = 12,
                backgroundColor = Theme.BG_WHITE,
                borderWidth = 2, borderColor = Theme.BORDER_GOLD,
                padding = 16, gap = 10, alignItems = "center",
                children = {
                    -- 标题行
                    UI.Panel {
                        flexDirection = "row", gap = 6, alignItems = "center",
                        children = {
                            UI.Panel { width = 8, height = 8, borderRadius = 4, backgroundColor = Theme.RED },
                            UI.Label { text = "紧急事件", fontSize = 10, fontColor = Theme.RED },
                            UI.Label {
                                text = "（剩余" .. #s.pendingEvents .. "件）",
                                fontSize = 9, fontColor = Theme.TEXT_MUTED,
                            },
                        },
                    },
                    -- 事件标题
                    UI.Label { text = evt.title, fontSize = 16, fontColor = Theme.GOLD, fontWeight = "bold" },
                    -- 分隔线
                    UI.Panel { width = "80%", height = 1, backgroundColor = Theme.BORDER_GOLD },
                    -- 事件描述
                    UI.Label {
                        text = evt.desc, fontSize = 12, fontColor = Theme.TEXT_PRIMARY,
                        textAlign = "center", whiteSpace = "normal",
                    },
                    -- 选项
                    UI.Panel {
                        width = "100%", gap = 6, marginTop = 4,
                        children = choiceChildren,
                    },
                },
            },
        },
    }

    gameRoot_:AddChild(overlay)
end

-- ============================================================================
-- 底部导航栏（全屏页面切换）
-- ============================================================================

local NAV_ITEMS = {
    { id = "tree",       icon = "谱", label = "族谱",  img = Theme.IMG.NAV_TREE },
    { id = "clan",       icon = "祠", label = "宗族",  img = Theme.IMG.NAV_CLAN },       -- 宗祠 + 族规
    { id = "members",    icon = "人", label = "族人",  img = Theme.IMG.NAV_MEMBERS },
    { id = "industry",   icon = "田", label = "经营",  img = Theme.IMG.NAV_INDUSTRY },   -- 产业 + 库房
    { id = "career",     icon = "仕", label = "功业",  img = Theme.IMG.NAV_CAREER },     -- 仕途 + 书院 + 历练
}

-- 子页签配置：合并后的 Tab 内含子页签
local SUB_TABS = {
    clan = {
        { id = "clan_main",  label = "宗祠" },
        { id = "clan_rules", label = "族规" },
    },
    industry = {
        { id = "ind_main",   label = "产业" },
        { id = "ind_market", label = "集市" },
        { id = "ind_store",  label = "库房" },
    },
    career = {
        { id = "car_career",     label = "仕途" },
        { id = "car_academy",    label = "书院" },
        { id = "car_expedition", label = "历练" },
    },
}

-- 当前子页签状态
local currentSubTab_ = {
    clan = "clan_main",
    industry = "ind_main",
    career = "car_career",
}

-- 记录已知的解锁状态，用于检测新解锁
local knownUnlockedTabs_ = {}

local function InitKnownUnlockedTabs()
    knownUnlockedTabs_ = {}
    for _, item in ipairs(NAV_ITEMS) do
        if GameData.IsTabUnlocked(item.id) then
            knownUnlockedTabs_[item.id] = true
        end
    end
end

-- 解锁动画：弹出提示 + 图标出现在导航栏
local function PlayTabUnlockAnimation(item)
    -- 先弹出解锁提示（带图标）
    local overlay = UI.Panel {
        id = "unlockOverlay",
        width = "100%", height = "100%",
        position = "absolute", left = 0, top = 0, zIndex = 999,
        justifyContent = "center", alignItems = "center",
        backgroundColor = { 0, 0, 0, 100 },
        children = {
            UI.Panel {
                width = 200, height = 200,
                justifyContent = "center", alignItems = "center", gap = 12,
                backgroundColor = { 255, 255, 255, 240 },
                borderRadius = 16, borderWidth = 2, borderColor = Theme.GOLD,
                children = {
                    UI.Label { text = "功能解锁", fontSize = 18, fontColor = Theme.GOLD, fontWeight = "bold" },
                    UI.Panel {
                        width = 80, height = 80,
                        backgroundImage = item.img, backgroundFit = "contain",
                    },
                    UI.Label { text = "「" .. item.label .. "」已开启！", fontSize = 14, fontColor = Theme.TEXT_PRIMARY },
                },
            },
        },
        onClick = function(self)
            if gameRoot_ then
                gameRoot_:RemoveChild(self)
            end
            -- 刷新导航栏以显示新图标
            GameScreen.RefreshBottomNav()
        end,
    }

    if gameRoot_ then
        gameRoot_:AddChild(overlay)
    end
end

-- 检查是否有新解锁的 tab，如有则播放动画
function GameScreen.CheckNewUnlocks()
    for _, item in ipairs(NAV_ITEMS) do
        if GameData.IsTabUnlocked(item.id) and not knownUnlockedTabs_[item.id] then
            knownUnlockedTabs_[item.id] = true
            PlayTabUnlockAnimation(item)
            return  -- 一次只播一个
        end
    end
end

local function CreateBottomNav()
    local navChildren = {}
    for _, item in ipairs(NAV_ITEMS) do
        local unlocked = GameData.IsTabUnlocked(item.id)
        if unlocked then
            local isActive = (currentTab_ == item.id)
            navChildren[#navChildren + 1] = UI.Panel {
                id = "nav_" .. item.id,
                flex = 1, height = 56,
                justifyContent = "center", alignItems = "center",
                backgroundColor = isActive and { 56, 168, 120, 15 } or { 0, 0, 0, 0 },
                onClick = function(self)
                    AudioManager.TabSwitch()
                    if currentTab_ == item.id and item.id ~= "tree" then
                        GameScreen.SwitchTab("tree")
                    else
                        GameScreen.SwitchTab(item.id)
                    end
                end,
                children = {
                    UI.Panel {
                        width = 40, height = 40,
                        backgroundImage = item.img, backgroundFit = "contain",
                        opacity = isActive and 1.0 or 0.6,
                        borderWidth = isActive and 2 or 0,
                        borderColor = Theme.NAV_ACTIVE,
                        borderRadius = 20,
                    },
                },
            }
        end
        -- 未解锁的 tab 不渲染，完全隐藏
    end
    return UI.Panel {
        id = "bottomNav",
        width = "100%", height = 60,
        flexDirection = "row", justifyContent = "center",
        backgroundColor = { 248, 243, 228, 255 },
        backgroundImage = Theme.IMG.BG_BOTTOM_NAV, backgroundFit = "fill",
        borderTopWidth = 1, borderTopColor = Theme.BORDER_GOLD,
        overflow = "hidden",
        children = navChildren,
    }
end

-- ============================================================================
-- 页面切换核心逻辑
-- ============================================================================

function GameScreen.SwitchTab(tabId)
    currentTab_ = tabId
    AudioManager.TabSwitch()
    GameScreen.RefreshContent()
    GameScreen.RefreshBottomNav()
end

function GameScreen.RefreshBottomNav()
    if not gameRoot_ then return end
    local oldNav = gameRoot_:FindById("bottomNav")
    if oldNav then
        gameRoot_:RemoveChild(oldNav)
    end
    gameRoot_:AddChild(CreateBottomNav())
end

-- 确保当前子页签已解锁，否则切到第一个已解锁的
local function EnsureSubTabUnlocked(tabId)
    local subs = SUB_TABS[tabId]
    if not subs then return end
    local cur = currentSubTab_[tabId]
    if GameData.IsSubTabUnlocked(cur) then return end
    for _, sub in ipairs(subs) do
        if GameData.IsSubTabUnlocked(sub.id) then
            currentSubTab_[tabId] = sub.id
            return
        end
    end
end

-- 创建子页签切换栏
local function CreateSubTabBar(tabId)
    local subs = SUB_TABS[tabId]
    if not subs then return nil end

    EnsureSubTabUnlocked(tabId)

    local tabChildren = {}
    for _, sub in ipairs(subs) do
        local isActive = (currentSubTab_[tabId] == sub.id)
        local unlocked = GameData.IsSubTabUnlocked(sub.id)
        local reqRank = GameData.SUB_TAB_UNLOCK[sub.id] or 1

        tabChildren[#tabChildren + 1] = UI.Panel {
            flex = 1, height = 32,
            justifyContent = "center", alignItems = "center",
            borderBottomWidth = isActive and 2 or 0,
            borderBottomColor = isActive and Theme.GOLD or { 0, 0, 0, 0 },
            opacity = unlocked and 1.0 or 0.4,
            onClick = function(self)
                if not unlocked then
                    GameScreen.ShowResultPopup("功能未解锁",
                        sub.label .. "需要品级【" .. GameData.GetUnlockRankName(reqRank) .. "】才能解锁。")
                    return
                end
                if currentSubTab_[tabId] ~= sub.id then
                    AudioManager.Click()
                    currentSubTab_[tabId] = sub.id
                    GameScreen.RefreshContent()
                end
            end,
            children = {
                UI.Label {
                    text = unlocked and sub.label or ("[锁]" .. sub.label),
                    fontSize = 12,
                    fontColor = isActive and Theme.GOLD or (unlocked and Theme.TEXT_MUTED or Theme.TEXT_MUTED),
                },
            },
        }
    end

    return UI.Panel {
        width = "100%", flexDirection = "row",
        backgroundColor = Theme.NAV_BG,
        borderBottomWidth = 1, borderBottomColor = Theme.BORDER_LIGHT,
        children = tabChildren,
    }
end

function GameScreen.RefreshContent()
    if not contentArea_ then return end

    -- 保存当前 ScrollView 的滚动位置
    local savedScrollX, savedScrollY = 0, 0
    local savedTab = currentTab_
    local children = contentArea_:GetChildren()
    if children then
        for _, child in ipairs(children) do
            if child.GetScroll then
                local sx, sy = child:GetScroll()
                savedScrollX = sx or 0
                savedScrollY = sy or 0
                break
            end
        end
    end

    contentArea_:ClearChildren()

    if currentTab_ == "tree" then
        local tree = FamilyTree.Create(
            function(member) GameScreen.ShowMemberDetail(member) end,
            function() GameScreen.SwitchTab("battle") end,
            {
                onPray = function() GameScreen.ShowPrayDialog() end,
                onCourtesan = function() GameScreen.ShowCourtesanDialog() end,
                onImperialSeal = function() GameScreen.ShowImperialSealDialog() end,
                onLoan = function() GameScreen.ShowLoanDialog() end,
            }
        )
        contentArea_:AddChild(tree)
    elseif currentTab_ == "battle" then
        contentArea_:AddChild(BattlePrepPage.Create(PageTitle, GameScreen))
    elseif currentTab_ == "clan" then
        -- 子页签栏
        local subBar = CreateSubTabBar("clan")
        if subBar then contentArea_:AddChild(subBar) end
        local subId = currentSubTab_.clan
        if subId == "clan_main" then
            contentArea_:AddChild(GameScreen.CreateClanPage())
        elseif subId == "clan_rules" then
            contentArea_:AddChild(GameScreen.CreateRulesPage())
        end
    elseif currentTab_ == "members" then
        contentArea_:AddChild(MemberPage.Create(PageTitle, GameScreen))
    elseif currentTab_ == "industry" then
        local subBar = CreateSubTabBar("industry")
        if subBar then contentArea_:AddChild(subBar) end
        local subId = currentSubTab_.industry
        if subId == "ind_main" then
            contentArea_:AddChild(IndustryPage.Create(PageTitle, GameScreen))
        elseif subId == "ind_market" then
            contentArea_:AddChild(MarketPage.Create(PageTitle, GameScreen))
        elseif subId == "ind_store" then
            contentArea_:AddChild(InventoryPage.Create(PageTitle, GameScreen))
        end
    elseif currentTab_ == "career" then
        local subBar = CreateSubTabBar("career")
        if subBar then contentArea_:AddChild(subBar) end
        local subId = currentSubTab_.career
        if subId == "car_career" then
            contentArea_:AddChild(CareerPage.Create(PageTitle, GameScreen))
        elseif subId == "car_academy" then
            contentArea_:AddChild(AcademyPage.Create(PageTitle, GameScreen))
        elseif subId == "car_expedition" then
            contentArea_:AddChild(ExpeditionPage.Create(PageTitle, GameScreen))
        end
    end

    -- 恢复滚动位置（仅同一 tab 刷新时恢复，切换 tab 不恢复）
    if savedTab == currentTab_ and (savedScrollX ~= 0 or savedScrollY ~= 0) then
        local newChildren = contentArea_:GetChildren()
        if newChildren then
            for _, child in ipairs(newChildren) do
                if child.SetScroll then
                    child:SetScroll(savedScrollX, savedScrollY)
                    break
                end
            end
        end
    end
end

-- ============================================================================
-- 通用页面标题栏
-- ============================================================================

PageTitle = function(title, subtitle)
    return UI.Panel {
        width = "100%", padding = { 10, 16, 8, 16 },
        borderBottomWidth = 1, borderBottomColor = Theme.BORDER_GOLD,
        backgroundColor = { 248, 243, 228, 200 },
        overflow = "hidden",
        backgroundImage = Theme.IMG.DECO_TITLE_BANNER, backgroundFit = "contain",
        children = {
            UI.Label { text = title, fontSize = 18, fontColor = Theme.GOLD, letterSpacing = 2 },
            subtitle and UI.Label { text = subtitle, fontSize = 11, fontColor = Theme.TEXT_MUTED, marginTop = 2 } or nil,
        },
    }
end

-- ============================================================================
-- 辅助函数：计算筑寨实际费用（受族规"全民筑寨"减免影响）
-- ============================================================================

function GameScreen.GetFortCost()
    local s = GameData.state
    -- 递增费用：第1座100、第2座150、第3座200 ...
    local baseCost = 800 + s.fortCount * 400
    local ruleEffects = GameData.GetClanRuleEffects()
    local discount = ruleEffects.fortCostMul or 0  -- -0.3 表示减30%
    return math.max(10, math.floor(baseCost * (1 + discount)))
end

-- 获取当前寨堡防御减免百分比
function GameScreen.GetFortDefensePercent()
    local s = GameData.state
    return math.min(s.fortCount * 10, 50)  -- 每座10%，上限50%
end

-- ============================================================================
-- 全屏页面 1：宗祠管理
-- ============================================================================

function GameScreen.CreateClanPage()
    local s = GameData.state
    local rankName = GameData.GetClanRankName()
    local nextRankIdx = math.min(s.clanRank + 1, #GameData.CLAN_RANKS)
    local nextRank = GameData.CLAN_RANKS[nextRankIdx]
    local rankReq = GameData.RANK_UP_REQUIREMENTS[nextRankIdx] or { silver = 99999, fame = 99999, grain = 99999, cloth = 99999, population = 99 }
    local upgradeCostSilver = rankReq.silver or 0
    local upgradeCostFame = rankReq.fame or 0
    local upgradeCostGrain = rankReq.grain or 0
    local upgradeCostCloth = rankReq.cloth or 0
    local upgradePopReq = rankReq.population
    local aliveCount = #GameData.GetAliveMembers()

    return UI.ScrollView {
        width = "100%", flexGrow = 1, flexBasis = 0,
        backgroundColor = { 0, 0, 0, 0 },
        children = {
            UI.Panel {
                width = "100%", gap = 10, padding = 12, paddingBottom = 20,
                children = {
                    PageTitle("宗祠管理", s.clanName .. " · " .. rankName),

                    -- 宗族信息卡片
                    UI.Panel {
                        width = "100%", padding = 14, borderRadius = 10,
                        backgroundColor = Theme.BG_WHITE,
                        borderWidth = 1, borderColor = Theme.BORDER_GOLD, gap = 8,
                        children = {
                            UI.Panel {
                                flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                                children = {
                                    UI.Label { text = s.clanName, fontSize = 18, fontColor = Theme.GOLD },
                                    UI.Label { text = GameData.GetRegion().name, fontSize = 12, fontColor = Theme.TEXT_SECONDARY },
                                },
                            },
                            UI.Panel {
                                flexDirection = "row", gap = 16,
                                children = {
                                    UI.Label { text = "族人 " .. #GameData.GetAliveMembers() .. " 人", fontSize = 12, fontColor = Theme.TEXT_SECONDARY },
                                    UI.Label { text = "传承 " .. s.totalMonths .. " 月", fontSize = 12, fontColor = Theme.TEXT_SECONDARY },
                                    UI.Label { text = "寨堡 " .. s.fortCount .. " 座", fontSize = 12, fontColor = Theme.TEXT_SECONDARY },
                                },
                            },
                        },
                    },

                    -- 操作按钮（圆形）
                    UI.Panel {
                        width = "100%", flexDirection = "row", justifyContent = "center", gap = 16,
                        paddingVertical = 8,
                        children = {
                            -- 宗族升级
                            UI.Panel {
                                alignItems = "center", gap = 4,
                                children = {
                                    UI.Panel {
                                        width = 72, height = 72, borderRadius = 36,
                                        opacity = (s.clanRank < #GameData.CLAN_RANKS and GameData.CanAfford(upgradeCostSilver, upgradeCostGrain, upgradeCostCloth, upgradeCostFame) and aliveCount >= upgradePopReq) and 1.0 or 0.5,
                                        onClick = function(self)
                                            if s.clanRank >= #GameData.CLAN_RANKS then return end
                                            if aliveCount < upgradePopReq then
                                                Toast.Warn("需要至少 " .. upgradePopReq .. " 名族人（当前 " .. aliveCount .. " 人）")
                                                return
                                            end
                                            if not GameData.CanAfford(upgradeCostSilver, upgradeCostGrain, upgradeCostCloth, upgradeCostFame) then
                                                Toast.NotEnough("资源不足")
                                                return
                                            end
                                            local reqText = "提升至【" .. nextRank .. "】\n\n需要条件：\n"
                                                .. "  银两 " .. upgradeCostSilver .. "（当前 " .. s.silver .. "）\n"
                                                .. "  声望 " .. upgradeCostFame .. "（当前 " .. s.fame .. "）\n"
                                                .. "  粮食 " .. upgradeCostGrain .. "（当前 " .. s.grain .. "）\n"
                                            if upgradeCostCloth > 0 then
                                                reqText = reqText .. "  布匹 " .. upgradeCostCloth .. "（当前 " .. s.cloth .. "）\n"
                                            end
                                            reqText = reqText .. "  族人 " .. upgradePopReq .. " 人（当前 " .. aliveCount .. " 人）\n\n确认升级？"
                                            GameScreen.ShowConfirm("品级提升", reqText,
                                                "升级", function()
                                                -- 晋升扣除银两/粮食/布匹，声望和人口仅作为门槛判定不扣除
                                                if GameData.SpendResources(upgradeCostSilver, upgradeCostGrain, upgradeCostCloth, 0) then
                                                    local oldRank = s.clanRank
                                                    s.clanRank = s.clanRank + 1
                                                    local newRankName = GameData.GetClanRankName()
                                                    AudioManager.Celebrate()
                                                    GameData.AddLog("宗族品级提升为【" .. newRankName .. "】！")
                                                    Toast.Success("品级提升为【" .. newRankName .. "】")

                                                    -- 收集解锁功能描述
                                                    local descs = GameData.RANK_UNLOCK_DESC[s.clanRank]
                                                    if descs and #descs > 0 then
                                                        local unlockText = "品级提升为【" .. newRankName .. "】！\n\n解锁新功能：\n"
                                                        for _, d in ipairs(descs) do
                                                            unlockText = unlockText .. "  · " .. d .. "\n"
                                                        end
                                                        GameScreen.ShowResultPopup("品级提升", unlockText)
                                                    end

                                                    GameScreen.RefreshAll()
                                                end
                                            end)
                                        end,
                                        backgroundImage = Theme.IMG.BTN_UPGRADE, backgroundFit = "cover", overflow = "hidden",
                                    },
                                    UI.Label { text = "→" .. nextRank, fontSize = 9, fontColor = Theme.TEXT_MUTED, textAlign = "center" },
                                },
                            },
                            -- 祭祀祈福
                            UI.Panel {
                                alignItems = "center", gap = 4,
                                children = {
                                    UI.Panel {
                                        width = 72, height = 72, borderRadius = 36,
                                        opacity = (GameData.state.lastSacrificeYear < GameData.state.year and GameData.CanAfford(0, 10, 5, 0)) and 1.0 or 0.5,
                                        onClick = function(self)
                                            if s.lastSacrificeYear >= s.year then
                                                Toast.Warning("今年已祭祀过，明年再来")
                                                return
                                            end
                                            if not GameData.CanAfford(0, 10, 5, 0) then
                                                Toast.NotEnough("粮食或布匹")
                                                return
                                            end
                                            GameScreen.ShowConfirm("祭祀祈福", "消耗 粮食10、布匹5\n获得 声望+8\n（每年仅可祭祀一次）\n\n确认举行祭祀？", "祭祀", function()
                                                if GameData.SpendResources(0, 10, 5, 0) then
                                                    AudioManager.Celebrate()
                                                    GameData.AddResource("fame", 8)
                                                    s.lastSacrificeYear = s.year
                                                    GameData.AddLog("举行祭祀，宗族声望提升。")
                                                    GameScreen.RefreshAll()
                                                    Toast.Success("祭祀完成，声望+8")
                                                end
                                            end)
                                        end,
                                        backgroundImage = Theme.IMG.BTN_WORSHIP, backgroundFit = "cover", overflow = "hidden",
                                    },
                                    UI.Label { text = "祈福", fontSize = 9, fontColor = Theme.TEXT_MUTED, textAlign = "center" },
                                },
                            },
                            -- 筑寨（望族解锁）
                            UI.Panel {
                                alignItems = "center", gap = 4,
                                children = {
                                    UI.Panel {
                                        width = 72, height = 72, borderRadius = 36,
                                        opacity = GameData.IsIndustryUnlocked("fort") and (GameData.CanAfford(GameScreen.GetFortCost(), 0, 0, 0) and 1.0 or 0.5) or 0.35,
                                        onClick = function(self)
                                            if not GameData.IsIndustryUnlocked("fort") then
                                                Toast.Locked("筑寨", "望族")
                                                return
                                            end
                                            local fortCost = GameScreen.GetFortCost()
                                            local grainCost = 40 + GameData.state.fortCount * 25  -- 额外粮食成本
                                            if not GameData.CanAfford(fortCost, grainCost, 0, 0) then
                                                Toast.NotEnough("银两或粮食")
                                                return
                                            end
                                            local curDef = GameScreen.GetFortDefensePercent()
                                            local newDef = math.min(curDef + 10, 50)
                                            local desc = "消耗 银两" .. fortCost .. " 粮食" .. grainCost
                                            desc = desc .. "\n当前防御：" .. curDef .. "% → " .. newDef .. "%"
                                            if newDef >= 50 then
                                                desc = desc .. "（已达上限）"
                                            end
                                            -- 征伐加成提示
                                            local effForts = math.min(GameData.state.fortCount + 1, 5)
                                            desc = desc .. "\n征伐加成：全军防御+" .. (effForts * 2) .. " 血量+" .. (effForts * 5) .. "%"
                                            if fortCost < 800 + GameData.state.fortCount * 400 then
                                                desc = desc .. "\n族规[全民筑寨]减免30%费用"
                                            end
                                            desc = desc .. "\n\n确认筑寨？"
                                            GameScreen.ShowConfirm("修建寨堡（第" .. (GameData.state.fortCount + 1) .. "座）", desc, "筑寨", function()
                                                if GameData.SpendResources(fortCost, grainCost, 0, 0) then
                                                    AudioManager.Select()
                                                    GameData.AddIndustry("fort")
                                                    GameData.AddLog("修建寨堡一座，防御加强。")
                                                    GameScreen.RefreshAll()
                                                    Toast.Success("寨堡修建完成")
                                                end
                                            end)
                                        end,
                                        backgroundImage = Theme.IMG.BTN_FORT, backgroundFit = "cover", overflow = "hidden",
                                        justifyContent = "center", alignItems = "center",
                                        children = {
                                            (not GameData.IsIndustryUnlocked("fort")) and UI.Label { text = "[锁]", fontSize = 20, fontColor = Theme.TEXT_MUTED } or nil,
                                        },
                                    },
                                    UI.Label {
                                        text = GameData.IsIndustryUnlocked("fort")
                                            and ("防御" .. GameScreen.GetFortDefensePercent() .. "%")
                                            or "防御",
                                        fontSize = 9, fontColor = Theme.TEXT_MUTED, textAlign = "center",
                                    },
                                },
                            },
                        },
                    },

                    -- 宗族大事记
                    UI.Panel {
                        width = "100%", borderRadius = 10,
                        backgroundColor = Theme.BG_WHITE,
                        borderWidth = 1, borderColor = Theme.BORDER,
                        maxHeight = 200, overflow = "hidden",
                        children = {
                            UI.Panel {
                                width = "100%", padding = 12, gap = 6,
                                children = {
                                    UI.Label { text = "宗族大事记", fontSize = 15, fontColor = Theme.GOLD },
                                    UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER },
                                },
                            },
                            UI.ScrollView {
                                width = "100%", flexGrow = 1, flexShrink = 1,
                                paddingHorizontal = 12, paddingBottom = 10,
                                backgroundColor = { 0, 0, 0, 0 },
                                children = {
                                    UI.Panel {
                                        width = "100%", gap = 4,
                                        children = (function()
                                            local logItems = {}
                                            local logs = s.eventLog
                                            local count = math.min(#logs, 20)
                                            if count == 0 then
                                                logItems[#logItems + 1] = UI.Label { text = "暂无记录", fontSize = 11, fontColor = Theme.TEXT_MUTED }
                                            end
                                            for i = 1, count do
                                                local entry = logs[i]
                                                logItems[#logItems + 1] = UI.Label {
                                                    text = "【" .. entry.year .. "年" .. entry.month .. "月】" .. entry.text,
                                                    fontSize = 10, fontColor = Theme.TEXT_SECONDARY, whiteSpace = "normal",
                                                }
                                            end
                                            return logItems
                                        end)(),
                                    },
                                },
                            },
                        },
                    },
                },
            },
        },
    }
end

-- ============================================================================
-- 全屏页面 5：族规设定
-- ============================================================================

function GameScreen.CreateRulesPage()
    local s = GameData.state
    if not s.clanRules then s.clanRules = {} end

    local maxRules = GameData.GetMaxClanRules()

    -- 品级不足无法设族规
    if maxRules <= 0 then
        return UI.ScrollView {
            width = "100%", flexGrow = 1, flexBasis = 0,
            backgroundColor = { 0, 0, 0, 0 },
            children = {
                UI.Panel {
                    width = "100%", gap = 12, padding = 12, paddingBottom = 20, alignItems = "center",
                    children = {
                        PageTitle("族规设定", "需品级【农户】以上方可制定族规"),
                        UI.Panel {
                            width = "100%", padding = 20, borderRadius = 10,
                            backgroundColor = Theme.BG_CARD, borderWidth = 1, borderColor = Theme.BORDER,
                            alignItems = "center", gap = 8,
                            children = {
                                UI.Label { text = "[锁]", fontSize = 32, fontColor = Theme.TEXT_MUTED },
                                UI.Label { text = "族规尚未开放", fontSize = 16, fontColor = Theme.TEXT_MUTED },
                                UI.Label { text = "提升品级至【农户】后可制定族规", fontSize = 12, fontColor = Theme.TEXT_SECONDARY, textAlign = "center" },
                            },
                        },
                    },
                },
            },
        }
    end

    -- 族规效果详情文案（在卡片下方显示实际影响）
    local ruleDetailTexts = {
        store_grain = function(active) return active and "生效中：粮食消耗减少20%，粮食产出降低10%" or "启用后减少粮食消耗，适合备战备荒" end,
        martial_train = function(active) return active and "生效中：武艺成长+50%，流寇抵抗+20%" or "启用后全族习武效率大增，抵御外敌" end,
        frugal = function(active) return active and "生效中：布匹消耗减少40%，声望每月-1" or "启用后大幅节省布匹，但族人不满" end,
        study_first = function(active) return active and "生效中：学识成长+30%，粮食消耗额外增加10%" or "启用后族人读书更快，培养人才" end,
        merchant_focus = function(active) return active and "生效中：经商收益+25%，声望每月-2" or "启用后商路更通，但过于逐利损名声" end,
        fortify = function(active) return active and "生效中：筑寨费用减30%，产业产出-10%" or "启用后集中人力修建防御工事" end,
    }

    local ruleItems = {}
    for _, rule in ipairs(GameData.CLAN_RULES) do
        local isActive = GameData.IsClanRuleActive(rule.id)
        local detailFn = ruleDetailTexts[rule.id]
        local detailText = detailFn and detailFn(isActive) or ""
        ruleItems[#ruleItems + 1] = UI.Panel {
            width = "100%", padding = 10, borderRadius = 8,
            backgroundColor = isActive and { 56, 168, 120, 20 } or Theme.BG_INPUT,
            borderWidth = 1, borderColor = isActive and Theme.GOLD_DARK or Theme.BORDER,
            flexDirection = "row", justifyContent = "space-between", alignItems = "center",
            onClick = function(self)
                AudioManager.Click()
                local result = GameData.ToggleClanRule(rule.id)
                if result == nil then
                    GameScreen.ShowResultPopup("族规已满", "当前品级最多同时启用" .. maxRules .. "条族规，\n请先关闭一条。")
                else
                    GameScreen.RefreshAll()
                end
            end,
            children = {
                UI.Panel {
                    flexShrink = 1, gap = 2,
                    children = {
                        UI.Panel {
                            flexDirection = "row", gap = 4, alignItems = "center",
                            children = {
                                UI.Label { text = rule.icon, fontSize = 16, fontColor = isActive and Theme.GOLD or Theme.TEXT_MUTED },
                                UI.Label { text = rule.name, fontSize = 13, fontColor = isActive and Theme.GOLD or Theme.TEXT_PRIMARY },
                            },
                        },
                        UI.Label { text = rule.desc, fontSize = 10, fontColor = Theme.TEXT_MUTED, whiteSpace = "normal" },
                        UI.Label {
                            text = detailText, fontSize = 9,
                            fontColor = isActive and { 56, 140, 100, 255 } or Theme.TEXT_SECONDARY,
                            whiteSpace = "normal", marginTop = 2,
                        },
                    },
                },
                UI.Panel {
                    width = 38, height = 22, borderRadius = 11,
                    backgroundColor = isActive and Theme.GREEN or Theme.BG_INPUT,
                    borderWidth = 1, borderColor = isActive and Theme.GREEN or Theme.BORDER,
                    justifyContent = "center",
                    alignItems = isActive and "flex-end" or "flex-start",
                    paddingHorizontal = 2,
                    children = {
                        UI.Panel { width = 18, height = 18, borderRadius = 9, backgroundColor = { 255, 255, 255, 255 } },
                    },
                },
            },
        }
    end

    -- 构建当前生效族规总览
    local activeCount = 0
    for _, rid in ipairs(s.clanRules) do activeCount = activeCount + 1 end
    local summaryChildren = {}
    if activeCount > 0 then
        local effects = GameData.GetClanRuleEffects()
        local lines = {}
        if effects.grainConsumeMul and effects.grainConsumeMul ~= 0 then
            lines[#lines + 1] = "粮食消耗 " .. (effects.grainConsumeMul > 0 and "+" or "") .. math.floor(effects.grainConsumeMul * 100) .. "%"
        end
        if effects.grainOutputMul and effects.grainOutputMul ~= 0 then
            lines[#lines + 1] = "粮食产出 " .. math.floor(effects.grainOutputMul * 100) .. "%"
        end
        if effects.clothConsumeMul and effects.clothConsumeMul ~= 0 then
            lines[#lines + 1] = "布匹消耗 " .. math.floor(effects.clothConsumeMul * 100) .. "%"
        end
        if effects.martialGrowthMul and effects.martialGrowthMul ~= 0 then
            lines[#lines + 1] = "武艺成长 +" .. math.floor(effects.martialGrowthMul * 100) .. "%"
        end
        if effects.studyGrowthMul and effects.studyGrowthMul ~= 0 then
            lines[#lines + 1] = "学识成长 +" .. math.floor(effects.studyGrowthMul * 100) .. "%"
        end
        if effects.tradeIncomeMul and effects.tradeIncomeMul ~= 0 then
            lines[#lines + 1] = "经商收益 +" .. math.floor(effects.tradeIncomeMul * 100) .. "%"
        end
        if effects.banditResist and effects.banditResist ~= 0 then
            lines[#lines + 1] = "流寇抵抗 +" .. math.floor(effects.banditResist * 100) .. "%"
        end
        if effects.fameDrain and effects.fameDrain ~= 0 then
            lines[#lines + 1] = "声望变化 " .. effects.fameDrain .. "/月"
        end
        if effects.fortCostMul and effects.fortCostMul ~= 0 then
            lines[#lines + 1] = "筑寨费用 " .. math.floor(effects.fortCostMul * 100) .. "%"
        end
        if effects.allOutputMul and effects.allOutputMul ~= 0 then
            lines[#lines + 1] = "产业产出 " .. math.floor(effects.allOutputMul * 100) .. "%"
        end
        summaryChildren[#summaryChildren + 1] = UI.Panel {
            width = "100%", padding = 10, borderRadius = 8,
            backgroundColor = { 56, 140, 100, 15 },
            borderWidth = 1, borderColor = { 56, 140, 100, 60 },
            gap = 4,
            children = {
                UI.Label { text = "当前族规总效果（" .. activeCount .. "/" .. maxRules .. "条）", fontSize = 12, fontColor = Theme.GOLD },
                UI.Label { text = table.concat(lines, "  |  "), fontSize = 10, fontColor = { 56, 140, 100, 255 }, whiteSpace = "normal" },
            },
        }
    end

    -- 组装页面子元素（避免 table.unpack 不在末尾的陷阱）
    local pageChildren = {}
    pageChildren[#pageChildren + 1] = PageTitle("族规设定", "最多同时启用" .. maxRules .. "条族规")
    for _, item in ipairs(ruleItems) do
        pageChildren[#pageChildren + 1] = item
    end
    for _, item in ipairs(summaryChildren) do
        pageChildren[#pageChildren + 1] = item
    end

    return UI.ScrollView {
        width = "100%", flexGrow = 1, flexBasis = 0,
        backgroundColor = { 0, 0, 0, 0 },
        children = {
            UI.Panel {
                width = "100%", gap = 8, padding = 12, paddingBottom = 20,
                children = pageChildren,
            },
        },
    }
end

-- ============================================================================
-- 时间自动推进
-- ============================================================================

function HandleGameUpdate(eventType, eventData)
    local s = GameData.state
    if not s or not gameRoot_ then return end

    local dt0 = eventData["TimeStep"]:GetFloat()

    -- Toast 倒计时（在所有 early return 之前，确保弹窗始终能自动消失）
    Toast.Update(dt0)

    -- 广告播放期间完全暂停游戏（不推进时间、不弹事件）
    -- 安全超时：如果 isShowingAd 卡住超过 60 秒，强制重置
    if AdSystem.isShowingAd then
        if not AdSystem._adStartTime then
            AdSystem._adStartTime = os.clock()
        elseif os.clock() - AdSystem._adStartTime > 60 then
            log:Write(LOG_WARNING, "[AdSystem] isShowingAd stuck for 60s, force reset!")
            AdSystem.isShowingAd = false
            AdSystem._adStartTime = nil
        end
        return
    end
    AdSystem._adStartTime = nil  -- 不在广告中，清理计时

    if s.gameEnded then
        if gameSpeed_ ~= 0 then
            gameSpeed_ = 0
            RefreshTimeControl()
        end
        return
    end

    if #s.pendingEvents > 0 then
        -- 如果有 Modal/Overlay 打开（设置、成就、结局、族人详情等），
        -- 不弹出事件弹窗，让事件在后台排队，时间继续流逝
        local overlayStack = UI.GetOverlayStack()
        local hasModalOpen = #overlayStack > 0
        if hasModalOpen then
            -- Modal 打开期间：事件静默排队，不暂停、不弹窗
            -- 时间照常流逝（不 return，继续执行后面的时间推进逻辑）
        else
            -- 无 Modal：正常暂停并弹出事件弹窗
            if gameSpeed_ ~= 0 then
                prevGameSpeed_ = gameSpeed_  -- 只在速度非0时保存，避免覆盖有效值
                gameSpeed_ = 0
                RefreshTimeControl()
            end
            -- 安全兜底：确保 prevGameSpeed_ 始终有有效恢复值
            if prevGameSpeed_ <= 0 then prevGameSpeed_ = 1 end
            -- 弹出事件弹窗（不切换页面）
            if not eventPopupShown_ then
                -- pcall 保护：如果 ShowPendingEventPopup 崩溃，跳过当前事件而非卡死
                local ok, err = pcall(GameScreen.ShowPendingEventPopup)
                if not ok then
                    log:Write(LOG_WARNING, "ShowPendingEventPopup error: " .. tostring(err))
                    -- 跳过导致崩溃的事件，防止永久卡死
                    if #s.pendingEvents > 0 then
                        local badEvt = s.pendingEvents[1]
                        log:Write(LOG_WARNING, "Skipping crashed event: " .. tostring(badEvt and badEvt.title))
                        table.remove(s.pendingEvents, 1)
                    end
                    eventPopupShown_ = false
                end
            else
                -- 安全网：如果标记为已显示但overlay实际不存在，重新触发
                local overlay = gameRoot_ and gameRoot_:FindById("eventPopupOverlay")
                if not overlay then
                    eventPopupShown_ = false
                    -- 不在安全网中立即重试，让下一帧的 if not eventPopupShown_ 分支处理
                end
            end
            return
        end
    end

    if gameSpeed_ == 0 then return end

    local dt = eventData["TimeStep"]:GetFloat()
    timeAccum_ = timeAccum_ + dt
    local interval = SPEED_INTERVAL[gameSpeed_] or 3.0
    if timeAccum_ >= interval then
        timeAccum_ = timeAccum_ - interval
        AudioManager.MonthTick()
        local report = MonthlyUpdate.Execute()
        SaveSystem.AutoSave()
        -- 月更新后处理（pcall保护，确保任何崩溃都不会阻止RefreshAll）
        local ok, postErr = pcall(function()
            AudioManager.UpdateGameBGM(s.year)
            -- 加速特权过期检查（基于真实时间24小时）
            if gameSpeed_ >= 2 and not AdSystem.IsSpeedUnlocked() then
                gameSpeed_ = 1
                prevGameSpeed_ = 1
                Toast.Show("加速已到期，已恢复1倍速")
            end
            -- 每月结算钱庄贷款利息
            AdSystem.ProcessMonthlyLoans()
            if s.gameEnded then
                gameSpeed_ = 0
                RefreshTimeControl()
                GameScreen.ShowEndingScreen()
            elseif report.yearEndSummary then
                -- 年终总结时暂停自动推进
                gameSpeed_ = 0
                RefreshTimeControl()
                GameScreen.ShowYearEndSummary(report.yearEndSummary)
            elseif report.rankUpUnlocks then
                -- 品级提升时暂停自动推进，弹出通知
                gameSpeed_ = 0
                RefreshTimeControl()
                AudioManager.Celebrate()
                GameScreen.ShowMonthlyReport(report)
            end
        end)
        if not ok then
            log:Write(LOG_WARNING, "Monthly post-process error: " .. tostring(postErr))
        end
        -- 粮食急救提示：粮食极低时暂停并弹出广告救济选项
        if s.grain <= 5 and not s.gameEnded and AdSystem.IsAvailable("grain_relief") then
            if not grainReliefShownMonth_ or grainReliefShownMonth_ ~= (s.year * 100 + s.month) then
                grainReliefShownMonth_ = s.year * 100 + s.month
                gameSpeed_ = 0
                RefreshTimeControl()
                local aliveCount = #GameData.GetAliveMembers()
                local estRelief = math.max(20, aliveCount * 3 * 3)
                local reliefRemain = AdSystem.GetRemaining("grain_relief")
                local modal = UI.Modal {
                    title = "粮食告急！",
                    size = "sm",
                    showCloseButton = true,
                    closeOnOverlay = true,
                    onClose = function() GameScreen.ResumeSpeed() end,
                }
                modal:SetContent(UI.Panel {
                    width = "100%", padding = 12, gap = 8, alignItems = "center",
                    children = {
                        UI.Label { text = "危", fontSize = 36, fontColor = Theme.RED },
                        UI.Label { text = "粮仓见底，族人即将饿死！", fontSize = 14, fontColor = Theme.RED, fontWeight = "bold", textAlign = "center" },
                        UI.Label { text = "当前粮食：" .. math.floor(s.grain), fontSize = 12, fontColor = Theme.TEXT_SECONDARY, textAlign = "center" },
                        UI.Panel {
                            width = "100%", height = 38, borderRadius = 8, marginTop = 4,
                            backgroundGradient = { direction = "to-right", from = { 200, 160, 40, 255 }, to = { 180, 120, 20, 255 } },
                            flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 4,
                            onPointerDown = function(self)
                                AdSystem.GrainRelief(function(success, result)
                                    modal:Close()
                                    if success then
                                        GameScreen.ShowResultPopup("粮食救济", "获得粮食 " .. result .. "！族人暂时脱离饥荒。")
                                    else
                                        GameScreen.ShowResultPopup("救济失败", result or "")
                                    end
                                    GameScreen.RefreshAll()
                                end)
                            end,
                            children = {
                                UI.Label { text = "▶ 看广告·紧急调粮", fontSize = 13, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                                UI.Label { text = "(" .. reliefRemain .. "次)", fontSize = 10, fontColor = { 255, 255, 255, 160 } },
                            },
                        },
                        UI.Label { text = "可获得约" .. estRelief .. "粮食（够" .. aliveCount .. "人吃3个月）", fontSize = 9, fontColor = Theme.TEXT_MUTED, textAlign = "center" },
                    },
                })
                modal:Open()
            end
        end
        GameScreen.RefreshAll()
    end
end

-- ============================================================================
-- 刷新函数
-- ============================================================================

function GameScreen.RefreshTopBar()
    if not gameRoot_ then return end
    local s = GameData.state
    local function updateLabel(id, text)
        local label = gameRoot_:FindById(id)
        if label then label:SetText(text) end
    end
    updateLabel("clanNameLabel", s.clanName)
    updateLabel("clanRankLabel", GameData.GetClanRankName())
    updateLabel("yearLabel", EraSystem.GetYearLabel(s.year))
    updateLabel("monthLabel", s.month .. "月")
    -- 月份标签闪烁动画（暗示时间在流动）
    if gameSpeed_ > 0 then
        local monthEl = gameRoot_:FindById("monthLabel")
        if monthEl then
            monthEl:Animate({
                keyframes = {
                    { fontColor = Theme.GOLD },
                    { fontColor = Theme.TEXT_MUTED },
                },
                duration = 0.4,
                iterations = 1,
                easing = "easeOut",
            })
        end
    end
    updateLabel("weatherLabel", WeatherSystem.GetDisplayText())
    updateLabel("resSilver", FormatNumber(s.silver))
    updateLabel("resGrain", FormatNumber(s.grain))
    updateLabel("resCloth", FormatNumber(s.cloth))
    updateLabel("resFame", FormatNumber(s.fame))
    updateLabel("resPop", FormatNumber(#GameData.GetAliveMembers()))

    -- 更新钱庄入口条
    local loanBar = gameRoot_:FindById("loanBar")
    if loanBar then
        local topBarEl = gameRoot_:FindById("topBar")
        if topBarEl then
            topBarEl:RemoveChild(loanBar)
            -- 重建钱庄条
            local loans = AdSystem.GetLoans()
            local totalDebt, monthlyInt = AdSystem.GetTotalDebt()
            local showLoanBar = #loans > 0 or s.silver < 30
            if showLoanBar then
                local barText = #loans > 0
                    and ("钱庄 · 在贷" .. #loans .. "笔 · 月息" .. monthlyInt .. "两")
                    or "银两不足？点击钱庄借贷"
                topBarEl:AddChild(UI.Panel {
                    id = "loanBar",
                    width = "100%", height = 22, marginTop = 2,
                    flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 6,
                    borderRadius = 4,
                    backgroundColor = #loans > 0 and { 200, 160, 40, 30 } or { 60, 160, 120, 25 },
                    onClick = function(self)
                        AudioManager.Click()
                        GameScreen.ShowLoanDialog()
                    end,
                    children = {
                        UI.Label { text = "贷", fontSize = 11 },
                        UI.Label { text = barText, fontSize = 10, fontColor = #loans > 0 and Theme.GOLD_DARK or Theme.GREEN },
                        UI.Label { text = "›", fontSize = 12, fontColor = Theme.TEXT_MUTED },
                    },
                })
            end
        end
    end
end

function GameScreen.RefreshAll()
    GameScreen.RefreshTopBar()
    GameScreen.RefreshContent()
    RefreshTimeControl()
    -- 检测新解锁的 tab（播放解锁动画）
    GameScreen.CheckNewUnlocks()

    -- 重建底部导航栏（品级变更后解锁状态会变化）
    if gameRoot_ then
        local oldNav = gameRoot_:FindById("bottomNav")
        if oldNav then
            gameRoot_:RemoveChild(oldNav)
        end
        gameRoot_:AddChild(CreateBottomNav())
    end
end

-- ============================================================================
-- 创建主界面
-- ============================================================================

function GameScreen.Create(callbacks)
    gameSpeed_ = 1   -- 默认1x速度，进入游戏自动推进
    prevGameSpeed_ = 1
    timeAccum_ = 0
    eventPopupShown_ = false
    currentTab_ = "tree"

    -- 中央内容容器（独立不透明背景，不让主背景透出）
    contentArea_ = UI.Panel {
        id = "contentArea",
        width = "100%",
        flexGrow = 1, flexBasis = 0,
        backgroundColor = Theme.BG_LIGHT,
        overflow = "hidden",
    }

    -- 组装布局（主背景提供金边装饰，三区域各自独立背景覆盖）
    gameRoot_ = UI.Panel {
        id = "gameScreen",
        width = "100%", height = "100%",
        flexDirection = "column",
        backgroundImage = Theme.IMG.BG_MAIN_LANDSCAPE, backgroundFit = "cover",
        children = {
            CreateTopBar(),
            UI.Panel {
                id = "middleContainer",
                width = "100%", flexGrow = 1, flexBasis = 0,
                position = "relative",
                overflow = "hidden",
                children = {
                    contentArea_,
                    CreateTimeControl(),
                },
            },
            CreateBottomNav(),
        },
    }

    -- 初始化已知解锁状态（用于检测新解锁动画）
    InitKnownUnlockedTabs()

    -- 加载默认页面（族谱）
    GameScreen.RefreshContent()

    -- 订阅 Update 事件
    if not updateSubscribed_ then
        SubscribeToEvent("Update", "HandleGameUpdate")
        updateSubscribed_ = true
    end

    -- 检查终局
    local s = GameData.state
    if s and s.gameEnded then
        GameScreen.ShowEndingScreen()
    end

    return gameRoot_
end

-- ============================================================================
-- 讨伐系统：进入战斗（Step2 实现3D战斗场景后完善）
-- ============================================================================

--- 进入3D战斗场景
---@param rival table 敌族数据（来自 RivalClans.Generate()）
---@param deployedMemberIds table 出战族人ID列表
function GameScreen.EnterBattle(rival, deployedMemberIds, options)
    log:Write(LOG_INFO, "[Battle] 进入战斗: 讨伐 " .. rival.name ..
        ", 出战将领 " .. #deployedMemberIds .. " 人")

    -- 暂停时间推进
    local savedSpeed = gameSpeed_
    gameSpeed_ = 0

    -- 隐藏主界面 UI
    if gameRoot_ then
        gameRoot_:SetVisible(false)
    end

    -- 获取我方可用兵力（事件战斗可自定义兵力）
    local soldierCount = (options and options.soldierCount) or RivalClans.GetPlayerSoldierCount()

    -- 进入3D战斗场景
    BattleScene.Enter(rival, deployedMemberIds, soldierCount, function()
        -- 战斗结束回调：恢复主界面
        log:Write(LOG_INFO, "[Battle] 返回主界面")

        -- 清除所有可能残留的 overlay（防止遮罩阻断交互）
        local overlays = UI.GetOverlayStack()
        while #overlays > 0 do
            local top = overlays[#overlays]
            if top and top.Close then pcall(function() top:Close() end) end
            UI.PopOverlay(top)
        end

        -- 恢复主界面 UI root（BattleScene.Enter 中 UI.SetRoot 替换过）
        if gameRoot_ then
            gameRoot_:SetVisible(true)
            UI.SetRoot(gameRoot_)
        end

        -- 重新订阅主界面的 Update 事件
        SubscribeToEvent("Update", "HandleGameUpdate")

        -- 重置讨伐页面状态
        BattlePrepPage.Reset()

        -- 恢复游戏速度
        gameSpeed_ = savedSpeed
        if gameSpeed_ == 0 then gameSpeed_ = 1 end

        -- 刷新所有 UI（战斗结算可能改变了资源和族人状态）
        GameScreen.RefreshAll()
        -- 返回族谱页
        GameScreen.SwitchTab("tree")
    end, options)  -- 透传 options（含 onSettle 等）给 BattleScene
end

-- ============================================================================
-- GM 战斗测试面板
-- ============================================================================

function GameScreen.ShowGMBattlePanel()
    -- 清理之前的 GM Modal（防止残留）
    if GameScreen._gmModal then
        pcall(function() GameScreen._gmModal:Close() end)
        GameScreen._gmModal = nil
    end

    local modal = UI.Modal {
        title = "GM 战斗测试",
        size = "md",
        showCloseButton = true,
        closeOnOverlay = true,
    }
    GameScreen._gmModal = modal

    -- 获取可出战的族人（活着的）
    local aliveMembers = GameData.GetAliveMembers()

    -- GM 状态（使用模块级变量保持选择）
    if not GameScreen._gmState then
        GameScreen._gmState = {
            withHorse = false,       -- 是否给族人配马（骑马模型已废弃）
            soldierCount = 500,      -- 兵力数量
            archerPercent = 0,       -- 弓兵比例 0~100
        }
    end
    local gmState = GameScreen._gmState

    -- 构建族人列表（显示是否有马）
    local memberLines = {}
    for i, m in ipairs(aliveMembers) do
        if i > 5 then break end  -- 最多显示5人
        local horseTag = m.hasHorse and " [有马]" or ""
        memberLines[#memberLines + 1] = UI.Label {
            text = m.name .. " 武:" .. m.martial .. " 健:" .. m.health .. horseTag,
            fontSize = 11, fontColor = Theme.TEXT_SECONDARY,
        }
    end

    local function BuildContent()
        -- 计算步兵/弓兵实际数量预览
        local totalUnits = math.floor(gmState.soldierCount / 100)
        totalUnits = math.max(totalUnits, 1)
        totalUnits = math.min(totalUnits, 30)
        local archerUnits = math.floor(totalUnits * gmState.archerPercent / 100 + 0.5)
        local meleeUnits = totalUnits - archerUnits

        return UI.Panel {
            width = "100%", gap = 10, padding = 8,
            children = {
                -- 族人预览
                UI.Label { text = "可出战族人（前5人）", fontSize = 13, fontColor = Theme.GOLD },
                UI.Panel { width = "100%", gap = 2, children = memberLines },

                -- 分割线
                UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginVertical = 4 },

                -- 兵力设置（100~3000，步进100）
                UI.Label { id = "soldierLabel", text = "我方总兵力: " .. gmState.soldierCount, fontSize = 13, fontColor = Theme.GOLD },
                UI.Slider {
                    value = (gmState.soldierCount - 100) / 29,  -- 100~3000 映射到 0~100
                    min = 0, max = 100,
                    step = 1,
                    onChangeEnd = function(self, v)
                        gmState.soldierCount = math.floor(v * 29 / 100) * 100 + 100
                        if gmState.soldierCount > 3000 then gmState.soldierCount = 3000 end
                        if gmState.soldierCount < 100 then gmState.soldierCount = 100 end
                        -- 刷新整个面板以更新所有数字
                        modal:Close()
                        GameScreen.ShowGMBattlePanel()
                    end,
                },

                -- 分割线
                UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginVertical = 4 },

                -- 兵种配比
                UI.Label { text = "兵种配比", fontSize = 13, fontColor = Theme.GOLD },
                UI.Panel {
                    flexDirection = "row", gap = 8, width = "100%", alignItems = "center",
                    children = {
                        -- 刀兵标签
                        UI.Panel {
                            width = 60, height = 28, borderRadius = 6,
                            backgroundColor = { 80, 120, 200, 255 },
                            justifyContent = "center", alignItems = "center",
                            children = { UI.Label { text = "刀兵", fontSize = 11, fontColor = { 255, 255, 255, 255 } } },
                        },
                        -- 滑块
                        UI.Panel {
                            flex = 1,
                            children = {
                                UI.Slider {
                                    value = gmState.archerPercent,
                                    min = 0, max = 100,
                                    step = 10,
                                    onChangeEnd = function(self, v)
                                        gmState.archerPercent = math.floor(v / 10 + 0.5) * 10
                                        if gmState.archerPercent > 100 then gmState.archerPercent = 100 end
                                        if gmState.archerPercent < 0 then gmState.archerPercent = 0 end
                                        modal:Close()
                                        GameScreen.ShowGMBattlePanel()
                                    end,
                                },
                            },
                        },
                        -- 弓兵标签
                        UI.Panel {
                            width = 60, height = 28, borderRadius = 6,
                            backgroundColor = { 200, 120, 60, 255 },
                            justifyContent = "center", alignItems = "center",
                            children = { UI.Label { text = "弓兵", fontSize = 11, fontColor = { 255, 255, 255, 255 } } },
                        },
                    },
                },

                -- 配比预览
                UI.Panel {
                    flexDirection = "row", justifyContent = "center", gap = 12, width = "100%",
                    children = {
                        UI.Label {
                            text = "刀兵 " .. meleeUnits .. " 营",
                            fontSize = 12, fontColor = { 120, 160, 230, 255 },
                        },
                        UI.Label {
                            text = "|", fontSize = 12, fontColor = Theme.TEXT_SECONDARY,
                        },
                        UI.Label {
                            text = "弓兵 " .. archerUnits .. " 营",
                            fontSize = 12, fontColor = { 230, 150, 80, 255 },
                        },
                        UI.Label {
                            text = "（共 " .. totalUnits .. " 营）",
                            fontSize = 11, fontColor = Theme.TEXT_SECONDARY,
                        },
                    },
                },

                -- 分割线
                UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginVertical = 4 },

                -- 开战按钮
                UI.Panel {
                    width = "100%", height = 48, borderRadius = 10,
                    backgroundGradient = { direction = "horizontal", from = { 180, 60, 60, 255 }, to = { 220, 100, 60, 255 } },
                    justifyContent = "center", alignItems = "center",
                    onClick = function(self)
                        modal:Close()
                        GameScreen.GMStartBattle(gmState.withHorse, gmState.soldierCount)
                    end,
                    children = {
                        UI.Label { text = "立即开战", fontSize = 16, fontColor = { 255, 255, 255, 255 }, fontWeight = "bold" },
                    },
                },
            },
        }
    end

    modal:AddContent(BuildContent())
    modal:Open()
end

--- GM 直接进入战斗（绕过 EnterBattle，直接控制兵力）
---@param withHorse boolean 是否给族人配马
---@param soldierCount number 兵力数量
function GameScreen.GMStartBattle(withHorse, soldierCount)
    local s = GameData.state
    if not s then
        log:Write(LOG_WARNING, "[GM] 无存档，无法进入战斗")
        return
    end

    -- 获取可出战族人（取前3个活着的）
    local aliveMembers = GameData.GetAliveMembers()
    local deployIds = {}
    for i, m in ipairs(aliveMembers) do
        if i > 3 then break end
        -- GM 强制设置马匹状态
        m.hasHorse = withHorse
        deployIds[#deployIds + 1] = m.id
    end

    if #deployIds == 0 then
        log:Write(LOG_WARNING, "[GM] 无可出战族人")
        return
    end

    -- 生成一个测试敌族
    local rivals = RivalClans.Generate()
    local rival = rivals[1]  -- 取第一个（最弱的）

    local archerRatio = (GameScreen._gmState and GameScreen._gmState.archerPercent or 0) / 100
    log:Write(LOG_INFO, "[GM] 开战！骑马=" .. tostring(withHorse) ..
        " 兵力=" .. soldierCount .. " 弓兵比例=" .. archerRatio .. " 将领=" .. #deployIds)

    -- 暂停时间推进
    local savedSpeed = gameSpeed_
    gameSpeed_ = 0

    -- 隐藏主界面 UI
    if gameRoot_ then
        gameRoot_:SetVisible(false)
    end

    -- 直接进入战斗（用 GM 指定的兵力和兵种配比）
    BattleScene.Enter(rival, deployIds, soldierCount, function()
        log:Write(LOG_INFO, "[GM] 战斗结束，返回主界面")

        -- 先清理 GM Modal（防止残留遮罩阻断交互）
        if GameScreen._gmModal then
            pcall(function() GameScreen._gmModal:Close() end)
            GameScreen._gmModal = nil
        end
        -- 清除所有可能残留的 overlay（防止 Modal 遮罩阻断交互）
        local overlays = UI.GetOverlayStack()
        while #overlays > 0 do
            local top = overlays[#overlays]
            if top and top.Close then pcall(function() top:Close() end) end
            UI.PopOverlay(top)
        end

        -- 恢复主界面 UI root（BattleScene.Enter 中 UI.SetRoot 替换过）
        if gameRoot_ then
            gameRoot_:SetVisible(true)
            UI.SetRoot(gameRoot_)
        end
        SubscribeToEvent("Update", "HandleGameUpdate")
        BattlePrepPage.Reset()
        gameSpeed_ = savedSpeed
        if gameSpeed_ == 0 then gameSpeed_ = 1 end
        GameScreen.RefreshAll()
        GameScreen.SwitchTab("tree")
    end, { archerRatio = archerRatio })
end

return GameScreen
