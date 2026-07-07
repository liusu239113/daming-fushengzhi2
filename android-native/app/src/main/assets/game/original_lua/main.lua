-- ============================================================================
-- 大明浮生志2 — 大明风华
-- 主入口文件
-- ============================================================================

require "LuaScripts/Utilities/Sample"

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")
local EraSystem = require("Data.EraSystem")
local SaveSystem = require("Systems.SaveSystem")
local MainMenu = require("UI.MainMenu")
local CreateGame = require("UI.CreateGame")
local GameScreen = require("UI.GameScreen")
local AudioManager = require("Systems.AudioManager")

-- 当前屏幕标识
local currentScreen_ = "menu"  -- "menu" | "create" | "game"

-- ============================================================================
-- 屏幕切换
-- ============================================================================

function ShowScreen(screenId, ...)
    currentScreen_ = screenId
    local root = nil

    if screenId == "menu" then
        AudioManager.PlayBGM("MENU")
        root = MainMenu.Create({
            onNewGame = function()
                ShowScreen("create")
            end,
            onContinue = function()
                local ok, slotOrErr = SaveSystem.LoadLatest()
                if ok then
                    print("[Main] 继续游戏，加载" .. (SaveSystem.SLOT_NAMES[slotOrErr] or slotOrErr))
                    ShowScreen("game")
                elseif slotOrErr == "version_too_new" then
                    ShowVersionMismatchModal()
                end
            end,
            onSaveManage = function()
                -- 简易存档管理：显示弹窗
                ShowSaveManageModal()
            end,
            onSettings = function()
                ShowSettingsModal()
            end,
        })

    elseif screenId == "create" then
        root = CreateGame.Create({
            onBack = function()
                ShowScreen("menu")
            end,
            onConfirm = function(surname, originId, regionId, mottoId, difficultyId)
                GameData.NewGame(surname, originId, regionId, mottoId, difficultyId)
                SaveSystem.AutoSave()
                ShowScreen("game")
            end,
        })

    elseif screenId == "game" then
        -- 根据当前年份选择 BGM
        if GameData.state then
            AudioManager.UpdateGameBGM(GameData.state.year)
        end
        root = GameScreen.Create({})
    end

    if root then
        UI.SetRoot(root)
    end
end

-- ============================================================================
-- 版本不匹配弹窗（存档版本高于客户端 → 强制更新）
-- ============================================================================

function ShowVersionMismatchModal()
    local modal = UI.Modal {
        title = "版本过旧",
        size = "sm",
        showCloseButton = false,
        closeOnOverlay = false,
    }

    modal:AddContent(UI.Panel {
        width = "100%",
        gap = 12,
        padding = 12,
        alignItems = "center",
        children = {
            -- 警告图标
            UI.Panel {
                width = 56, height = 56, borderRadius = 28,
                backgroundColor = { 220, 80, 60, 40 },
                justifyContent = "center", alignItems = "center",
                children = {
                    UI.Label { text = "!", fontSize = 28, fontColor = { 220, 80, 60, 255 }, fontWeight = "bold" },
                },
            },

            UI.Label {
                text = "检测到存档版本更新",
                fontSize = 16,
                fontColor = Theme.TEXT_PRIMARY,
                fontWeight = "bold",
                textAlign = "center",
            },

            UI.Label {
                text = "您的存档由更新版本的游戏创建。为防止存档数据丢失或损坏，请先更新游戏至最新版本后再登录。",
                fontSize = 12,
                fontColor = Theme.TEXT_SECONDARY,
                textAlign = "center",
                whiteSpace = "normal",
            },

            UI.Label {
                text = "更新方式：点击右上角「...」→ 检查更新，更新完成后请重新启动游戏",
                fontSize = 11,
                fontColor = Theme.GOLD,
                textAlign = "center",
                whiteSpace = "normal",
            },

            -- 知道了按钮
            UI.Panel {
                width = "80%", height = 40, borderRadius = 8,
                backgroundGradient = Theme.GRADIENT_GOLD,
                justifyContent = "center", alignItems = "center",
                marginTop = 4,
                onClick = function(self)
                    AudioManager.Click()
                    modal:Close()
                end,
                children = {
                    UI.Label { text = "知道了", fontSize = 14, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                },
            },
        },
    })

    modal:Open()
end

-- ============================================================================
-- 存档管理弹窗
-- ============================================================================

function ShowSaveManageModal()
    local modal = UI.Modal {
        title = "存档管理",
        size = "md",
        showCloseButton = true,
        closeOnOverlay = true,
    }

    local slots = { SaveSystem.SLOT_AUTO, SaveSystem.SLOT_MANUAL }
    local slotChildren = {}

    for _, slotId in ipairs(slots) do
        local info = SaveSystem.GetSaveInfo(slotId)
        local hasData = info ~= nil
        local slotName = SaveSystem.SLOT_NAMES[slotId] or slotId

        -- 格式化时间戳
        local timeStr = ""
        if hasData and info.timestamp and info.timestamp > 0 then
            timeStr = os.date("%Y-%m-%d %H:%M", info.timestamp)
        end

        slotChildren[#slotChildren + 1] = UI.Panel {
            width = "100%",
            padding = 12,
            borderRadius = 8,
            backgroundColor = Theme.BG_CARD,
            borderWidth = 1,
            borderColor = hasData and Theme.BORDER_GOLD or Theme.BORDER,
            gap = 4,
            children = {
                UI.Panel {
                    flexDirection = "row",
                    justifyContent = "space-between",
                    alignItems = "center",
                    children = {
                        UI.Label {
                            text = slotName,
                            fontSize = 14,
                            fontColor = hasData and Theme.GOLD or Theme.TEXT_MUTED,
                        },
                        hasData and UI.Panel {
                            flexDirection = "row",
                            gap = 8,
                            children = {
                                -- 加载按钮
                                UI.Panel {
                                    paddingHorizontal = 10, paddingVertical = 4,
                                    borderRadius = 4,
                                    backgroundGradient = Theme.GRADIENT_GOLD,
                                    onClick = function(self)
                                        local loadOk, errType = SaveSystem.Load(slotId)
                                        if loadOk then
                                            modal:Close()
                                            ShowScreen("game")
                                        elseif errType == "version_too_new" then
                                            modal:Close()
                                            ShowVersionMismatchModal()
                                        end
                                    end,
                                    children = {
                                        UI.Label { text = "加载", fontSize = 11, fontColor = Theme.TEXT_WHITE },
                                    },
                                },
                                -- 删除按钮
                                UI.Panel {
                                    paddingHorizontal = 10, paddingVertical = 4,
                                    borderRadius = 4,
                                    backgroundColor = Theme.RED_DARK,
                                    onClick = function(self)
                                        SaveSystem.DeleteSave(slotId)
                                        modal:Close()
                                        ShowSaveManageModal()  -- 刷新
                                    end,
                                    children = {
                                        UI.Label { text = "删除", fontSize = 11, fontColor = Theme.TEXT_WHITE },
                                    },
                                },
                            },
                        } or UI.Label { text = "空", fontSize = 11, fontColor = Theme.TEXT_MUTED },
                    },
                },
                hasData and UI.Panel {
                    width = "100%",
                    gap = 2,
                    children = {
                        UI.Label {
                            text = (info.clanName or "未知") .. " · " .. EraSystem.GetYearLabel(info.year or 1368) .. (info.month or 1) .. "月",
                            fontSize = 11,
                            fontColor = Theme.TEXT_SECONDARY,
                        },
                        UI.Label {
                            text = timeStr,
                            fontSize = 10,
                            fontColor = Theme.TEXT_MUTED,
                        },
                    },
                } or nil,
            },
        }
    end

    modal:AddContent(UI.Panel {
        width = "100%",
        gap = 8,
        children = slotChildren,
    })

    modal:Open()
end

-- ============================================================================
-- 设置弹窗（音量控制 + 关于信息）
-- ============================================================================

function ShowSettingsModal()
    local modal = UI.Modal {
        title = "音量设置",
        size = "md",
        showCloseButton = true,
        closeOnOverlay = true,
    }

    -- 创建音量滑块行
    local function VolumeRow(label, initVal, onChange)
        local pct = math.floor(initVal * 100)
        return UI.Panel {
            width = "100%", flexDirection = "row", alignItems = "center",
            justifyContent = "space-between", paddingVertical = 4,
            children = {
                UI.Label { text = label, fontSize = 13, fontColor = Theme.TEXT_PRIMARY, width = 60 },
                UI.Slider {
                    flex = 1, marginHorizontal = 8,
                    value = pct, min = 0, max = 100,
                    onChange = function(self, v)
                        onChange(v / 100)
                        -- 更新百分比文字
                        local pctLabel = modal:FindById("pct_" .. label)
                        if pctLabel then pctLabel:SetText(math.floor(v) .. "%") end
                    end,
                },
                UI.Label {
                    id = "pct_" .. label,
                    text = pct .. "%", fontSize = 11, fontColor = Theme.TEXT_MUTED, width = 36,
                },
            },
        }
    end

    modal:AddContent(UI.Panel {
        width = "100%",
        gap = 10,
        padding = 8,
        children = {
            -- 音量设置区
            VolumeRow("背景音乐", AudioManager.GetBGMVolume(), function(v)
                AudioManager.SetBGMVolume(v)
            end),
            VolumeRow("音效", AudioManager.GetSFXVolume(), function(v)
                AudioManager.SetSFXVolume(v)
            end),

            -- 分割线
            UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginVertical = 6 },

            -- 关于
            UI.Label { text = "关于", fontSize = 14, fontColor = Theme.GOLD, marginBottom = 4 },
            UI.Panel {
                width = "100%", alignItems = "center", gap = 4,
                children = {
                    UI.Label { text = "大明浮生志 · 贰", fontSize = 15, fontColor = Theme.TEXT_PRIMARY },
                    UI.Label { text = "大明风华 · 版本 v2.0.0", fontSize = 11, fontColor = Theme.TEXT_MUTED },
                    UI.Label { text = "一款大明王朝宗族模拟经营游戏", fontSize = 11, fontColor = Theme.TEXT_MUTED },
                },
            },
        },
    })

    modal:Open()
end

-- ============================================================================
-- 成就图鉴弹窗
-- ============================================================================

function ShowAchievementsModal()
    local s = GameData.state
    local unlocked = s.unlockedAchievements or {}
    local unlockedSet = {}
    for _, id in ipairs(unlocked) do unlockedSet[id] = true end

    local modal = UI.Modal { title = "成就图鉴", size = "sm", showCloseButton = true, closeOnOverlay = true }

    local totalCount = #GameData.ACHIEVEMENTS
    local unlockedCount = #unlocked

    local items = {}
    -- 进度总览
    items[#items + 1] = UI.Panel {
        width = "100%", alignItems = "center", gap = 4, marginBottom = 4,
        children = {
            UI.Label { text = "已达成 " .. unlockedCount .. " / " .. totalCount, fontSize = 15, fontColor = Theme.GOLD },
            UI.Panel {
                width = "100%", height = 6, borderRadius = 3, backgroundColor = Theme.BG_SECTION,
                children = {
                    UI.Panel {
                        width = (totalCount > 0 and math.floor(unlockedCount / totalCount * 100) or 0) .. "%",
                        height = "100%", borderRadius = 3,
                        backgroundGradient = Theme.GRADIENT_PRIMARY,
                    },
                },
            },
        },
    }

    -- 成就列表
    for _, ach in ipairs(GameData.ACHIEVEMENTS) do
        local done = unlockedSet[ach.id]
        items[#items + 1] = UI.Panel {
            width = "100%", flexDirection = "row", alignItems = "center",
            paddingVertical = 6, paddingHorizontal = 8, gap = 8,
            borderRadius = 6,
            backgroundColor = done and { 255, 248, 230, 255 } or Theme.BG_CARD,
            borderWidth = 1, borderColor = done and Theme.BORDER_GOLD or Theme.BORDER,
            children = {
                -- 图标
                UI.Panel {
                    width = 32, height = 32, borderRadius = 16,
                    backgroundColor = done and Theme.GOLD or Theme.TEXT_MUTED,
                    justifyContent = "center", alignItems = "center",
                    children = {
                        UI.Label { text = ach.icon, fontSize = 14, fontColor = Theme.TEXT_WHITE },
                    },
                },
                -- 名称+描述
                UI.Panel {
                    flex = 1, gap = 1,
                    children = {
                        UI.Label { text = ach.name, fontSize = 13, fontColor = done and Theme.TEXT_PRIMARY or Theme.TEXT_MUTED, fontWeight = "bold" },
                        UI.Label { text = done and ach.desc or "???", fontSize = 10, fontColor = Theme.TEXT_SECONDARY },
                    },
                },
                -- 状态
                UI.Label {
                    text = done and "已达成" or "未达成",
                    fontSize = 10,
                    fontColor = done and Theme.GREEN or Theme.TEXT_MUTED,
                },
            },
        }
    end

    modal:AddContent(UI.ScrollView {
        width = "100%", maxHeight = 350, gap = 4, padding = 4,
        children = items,
    })
    modal:Open()
end

-- ============================================================================
-- 结局收集弹窗
-- ============================================================================

function ShowEndingsGalleryModal()
    local s = GameData.state
    local triggered = s.triggeredHiddenEndings or {}
    local EndingSys = require("Systems.EndingSystem")

    local modal = UI.Modal { title = "结局收集", size = "sm", showCloseButton = true, closeOnOverlay = true }

    local totalEndings = #EndingSys.HIDDEN_ENDINGS
    local unlockedCount = 0
    for _ in pairs(triggered) do unlockedCount = unlockedCount + 1 end

    local items = {}
    -- 进度总览
    items[#items + 1] = UI.Panel {
        width = "100%", alignItems = "center", gap = 4, marginBottom = 6,
        children = {
            UI.Label { text = "已解锁 " .. unlockedCount .. " / " .. totalEndings, fontSize = 15, fontColor = Theme.GOLD },
        },
    }

    -- 结局列表
    for _, ending in ipairs(EndingSys.HIDDEN_ENDINGS) do
        local done = triggered[ending.id]
        -- 结局描述
        local descMap = {
            extinction = "最后一位族人离世，宗祠香火断绝。",
            bankrupt = "资财散尽，门庭零落，终成过眼云烟。",
            executed = "声名狼藉，终遭朝廷满门抄斩。",
            scholar_dynasty = "十年寒窗，一朝金榜，书香门第传千古。",
            warlord = "铁骑纵横，战功赫赫，将门世家威名远播。",
            merchant_empire = "商贾云集，富可敌国，天下财富尽归一族。",
            utopia = "世外桃源，与世无争，终得太平安乐。",
        }
        items[#items + 1] = UI.Panel {
            width = "100%", padding = 10, borderRadius = 8, gap = 4,
            backgroundColor = done and { 255, 248, 230, 255 } or Theme.BG_CARD,
            borderWidth = 1, borderColor = done and Theme.BORDER_GOLD or Theme.BORDER,
            children = {
                UI.Panel {
                    flexDirection = "row", justifyContent = "space-between", alignItems = "center", width = "100%",
                    children = {
                        UI.Label {
                            text = done and ending.title or "???",
                            fontSize = 14,
                            fontColor = done and Theme.GOLD or Theme.TEXT_MUTED,
                            fontWeight = "bold",
                        },
                        UI.Label {
                            text = done and "已解锁" or "未解锁",
                            fontSize = 10,
                            fontColor = done and Theme.GREEN or Theme.TEXT_MUTED,
                        },
                    },
                },
                UI.Label {
                    text = done and (descMap[ending.id] or "一段传奇故事。") or "达成特定条件后解锁",
                    fontSize = 11,
                    fontColor = done and Theme.TEXT_SECONDARY or Theme.TEXT_MUTED,
                    whiteSpace = "normal",
                },
            },
        }
    end

    modal:AddContent(UI.ScrollView {
        width = "100%", maxHeight = 350, gap = 6, padding = 4,
        bounces = false, showScrollbar = true,
        children = items,
    })
    modal:Open()
end

-- ============================================================================
-- 游戏内暂停菜单（存档 / 设置 / 返回主菜单）
-- ============================================================================

function ShowPauseMenu()
    -- 打开设置时自动暂停游戏，关闭后保持暂停，需玩家手动恢复
    GameScreen.PauseGame()

    local modal = UI.Modal {
        title = "暂停",
        size = "md",
        showCloseButton = true,
        closeOnOverlay = true,
    }

    local function MenuBtn(text, icon, onClick)
        return UI.Panel {
            width = "100%", height = 44, borderRadius = 8,
            backgroundColor = Theme.BG_CARD,
            borderWidth = 1, borderColor = Theme.BORDER,
            flexDirection = "row", alignItems = "center", justifyContent = "center", gap = 8,
            onClick = function(self)
                AudioManager.Click()
                onClick()
            end,
            children = {
                UI.Label { text = icon, fontSize = 16, fontColor = Theme.GOLD },
                UI.Label { text = text, fontSize = 14, fontColor = Theme.TEXT_PRIMARY },
            },
        }
    end

    modal:AddContent(UI.Panel {
        width = "100%",
        gap = 8,
        padding = 8,
        children = {
            -- 手动存档
            MenuBtn("手动存档", "存", function()
                if SaveSystem.ManualSave() then
                    modal:Close()
                    if GameScreen.ShowResultPopup then
                        GameScreen.ShowResultPopup("存档成功", "游戏进度已保存到手动存档。")
                    end
                end
            end),

            -- 存档管理
            MenuBtn("存档管理", "管", function()
                modal:Close()
                ShowSaveManageModal()
            end),

            -- 音量设置
            MenuBtn("音量设置", "设", function()
                modal:Close()
                ShowSettingsModal()
            end),

            -- 分割线
            UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginVertical = 2 },

            -- 成就
            MenuBtn("成就图鉴", "勋", function()
                modal:Close()
                ShowAchievementsModal()
            end),

            -- 结局收集
            MenuBtn("结局收集", "录", function()
                modal:Close()
                ShowEndingsGalleryModal()
            end),

            -- 分割线
            UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginVertical = 2 },

            -- 返回主菜单
            MenuBtn("返回主菜单", "返", function()
                -- 先自动存档
                if GameData.state then
                    SaveSystem.AutoSave()
                end
                modal:Close()
                ShowScreen("menu")
            end),
        },
    })

    modal:Open()
end

-- ============================================================================
-- 引擎入口
-- ============================================================================

function Start()
    -- 设置窗口标题
    graphics.windowTitle = "大明浮生志2 — 大明风华"

    -- 初始化随机种子
    math.randomseed(os.time())

    -- 初始化音频系统
    AudioManager.Init()

    -- 预加载关键资源（字体、草地纹理），确保本地就绪后再初始化 UI
    local criticalPaths = { "Fonts/CustomFont.ttf", "Textures/grass_tile.png" }
    local needDownload = {}
    for _, path in ipairs(criticalPaths) do
        if not cache:Exists(path) then
            needDownload[#needDownload + 1] = path
            log:Write(LOG_INFO, "[Preload] 资源未就绪，等待下载: " .. path)
        end
    end
    if #needDownload > 0 then
        cache:DownloadResources(needDownload, function(success, failedCount)
            if success then
                log:Write(LOG_INFO, "[Preload] 关键资源下载完成")
            else
                log:Write(LOG_WARNING, "[Preload] 部分资源下载失败: " .. tostring(failedCount))
            end
        end, nil)
    end

    -- 初始化 UI 系统
    UI.Init({
        fonts = {
            { family = "sans", weights = { normal = "Fonts/CustomFont.ttf" } },
        },
        scale = UI.Scale.DEFAULT,
    })

    -- 覆盖 UI 库的深色内置主题为明亮现代风格
    local UITheme = require("urhox-libs/UI/Core/Theme")
    local lightTheme = UITheme.ExtendTheme(UITheme.defaultTheme, {
        colors = {
            primary = { 56, 168, 120, 255 },
            primaryHover = { 66, 185, 135, 255 },
            primaryPressed = { 42, 145, 100, 255 },
            secondary = { 165, 160, 148, 255 },
            secondaryHover = { 180, 175, 165, 255 },
            secondaryPressed = { 140, 135, 125, 255 },
            background = { 247, 242, 230, 255 },
            surface = { 255, 255, 255, 250 },
            surfaceHover = { 248, 245, 238, 255 },
            text = { 55, 50, 40, 255 },
            textSecondary = { 110, 105, 95, 255 },
            textDisabled = { 165, 160, 148, 255 },
            border = { 225, 220, 210, 255 },
            borderFocus = { 56, 168, 120, 255 },
            success = { 76, 175, 80, 255 },
            successHover = { 96, 195, 100, 255 },
            warning = { 200, 155, 50, 255 },
            warningHover = { 220, 175, 70, 255 },
            error = { 220, 80, 60, 255 },
            errorHover = { 240, 100, 80, 255 },
            info = { 66, 133, 244, 255 },
            disabled = { 235, 232, 225, 255 },
            disabledText = { 165, 160, 148, 255 },
            overlay = { 0, 0, 0, 100 },
            transparent = { 0, 0, 0, 0 },
            hover = { 0, 0, 0, 15 },
        },
        components = {
            Table = {
                headerBgColor = { 243, 240, 233, 255 },
                rowOddBgColor = { 255, 255, 255, 255 },
                rowEvenBgColor = { 250, 248, 242, 255 },
                rowHoverBgColor = { 56, 168, 120, 20 },
            },
        },
    })
    UITheme.SetTheme(lightTheme)

    -- 显示主菜单
    ShowScreen("menu")

    print("[大明浮生志2] 游戏启动完成")
end

function Stop()
    -- 退出时自动存档
    if currentScreen_ == "game" and GameData.state then
        SaveSystem.AutoSave()
        print("[大明浮生志2] 退出自动存档完成")
    end
end
