-- ============================================================================
-- 大明浮生志2 - 事件/历史大势页面（从 GameScreen.lua 拆分）
-- EventPage.Create(PageTitle, screen) 创建事件页面
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")
local MonthlyUpdate = require("Systems.MonthlyUpdate")
local SaveSystem = require("Systems.SaveSystem")
local AudioManager = require("Systems.AudioManager")
local AdSystem = require("Systems.AdSystem")
local Toast = require("Systems.Toast")

local EventPage = {}

-- ============================================================================
-- 事件/历史大势页面
-- ============================================================================

---@param PageTitle fun(title: string, subtitle?: string): table
---@param screen table GameScreen 引用，用于回调
function EventPage.Create(PageTitle, screen)
    local s = GameData.state
    local children = {
        PageTitle("家族事件", "风云变幻，何去何从"),
    }

    -- 游戏结束提示
    if s.gameEnded then
        children[#children + 1] = UI.Panel {
            width = "100%", height = 56, borderRadius = 8,
            backgroundGradient = Theme.GRADIENT_RED,
            justifyContent = "center", alignItems = "center",
            onClick = function(self) screen.ShowEndingScreen() end,
            children = {
                UI.Label { text = "甲申之变 · 查看终局结算", fontSize = 15, fontColor = Theme.TEXT_WHITE, letterSpacing = 1 },
            },
        }
    end

    -- 手动推进
    if not s.gameEnded then
        children[#children + 1] = UI.Panel {
            width = "100%", height = 48, borderRadius = 8,
            backgroundGradient = Theme.GRADIENT_PRIMARY,
            justifyContent = "center", alignItems = "center",
            onClick = function(self)
                AudioManager.MonthTick()
                local report = MonthlyUpdate.Execute()
                SaveSystem.AutoSave()
                if s.gameEnded then
                    screen.ShowEndingScreen()
                elseif report.yearEndSummary then
                    screen.ShowYearEndSummary(report.yearEndSummary)
                else
                    screen.ShowMonthlyReport(report)
                end
                AudioManager.UpdateGameBGM(s.year)
                screen.RefreshAll()
            end,
            children = {
                UI.Label {
                    text = "手动推进下月（" .. s.year .. "年" .. s.month .. "月→）",
                    fontSize = 14, fontColor = Theme.TEXT_WHITE, letterSpacing = 1,
                },
            },
        }
    end

    -- 待处理事件
    if #s.pendingEvents > 0 then
        children[#children + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 10 }
        children[#children + 1] = UI.Label { text = "待处理事件", fontSize = 15, fontColor = Theme.RED, marginTop = 4 }

        for i, evt in ipairs(s.pendingEvents) do
            children[#children + 1] = UI.Panel {
                width = "100%", padding = 14, borderRadius = 10,
                backgroundColor = Theme.BG_WHITE,
                borderWidth = 1, borderColor = Theme.RED_DARK, gap = 8,
                children = (function()
                    local evtChildren = {
                        UI.Label { text = evt.title, fontSize = 15, fontColor = Theme.GOLD },
                        UI.Label { text = evt.desc, fontSize = 12, fontColor = Theme.TEXT_PRIMARY, whiteSpace = "normal" },
                    }
                    if evt.choices then
                        for _, choice in ipairs(evt.choices) do
                            evtChildren[#evtChildren + 1] = UI.Panel {
                                width = "100%", height = 40, borderRadius = 6,
                                backgroundColor = Theme.BG_INPUT, borderWidth = 1, borderColor = Theme.BORDER_GOLD,
                                justifyContent = "center", alignItems = "center",
                                onClick = function(self)
                                    AudioManager.Select()
                                    local ok2, err2 = pcall(choice.effect)
                                    if not ok2 then
                                        log:Write(LOG_WARNING, "Event effect error: " .. tostring(err2))
                                    end
                                    table.remove(s.pendingEvents, i)
                                    SaveSystem.AutoSave()
                                    screen.RefreshAll()
                                end,
                                children = { UI.Label { text = choice.text, fontSize = 13, fontColor = Theme.GOLD } },
                            }
                        end
                    end
                    -- 额外奖励广告按钮
                    if AdSystem.IsAvailable("extra_reward") then
                        local adRemain = AdSystem.GetRemaining("extra_reward")
                        evtChildren[#evtChildren + 1] = UI.Panel {
                            width = "100%", height = 32, borderRadius = 6, marginTop = 2,
                            backgroundGradient = { direction = "to-right", from = { 180, 130, 50, 255 }, to = { 160, 110, 30, 255 } },
                            flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 4,
                            onClick = function(self)
                                AudioManager.Click()
                                -- 固定额外奖励：银两+粮食
                                local bonusReward = { silver = 10, grain = 8 }
                                AdSystem.ExtraEventReward(bonusReward, function(success)
                                    if success then
                                        Toast.Show("获得额外奖励：银两+5 粮食+4")
                                    end
                                    screen.RefreshAll()
                                end)
                            end,
                            children = {
                                UI.Label { text = "▶ 看广告·额外奖励", fontSize = 10, fontColor = Theme.TEXT_WHITE, fontWeight = "bold" },
                                UI.Label { text = "(" .. adRemain .. "次)", fontSize = 9, fontColor = { 255, 255, 255, 160 } },
                            },
                        }
                    end
                    return evtChildren
                end)(),
            }
        end
    end

    -- 明末大势时间线
    children[#children + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = Theme.BORDER, marginTop = 14 }
    children[#children + 1] = UI.Label { text = "明末大势", fontSize = 15, fontColor = Theme.GOLD, marginTop = 4 }

    for _, evt in ipairs(GameData.HISTORY_EVENTS) do
        local triggered = s.triggeredHistoryYears[evt.year]
        local isFuture = evt.year > s.year
        children[#children + 1] = UI.Panel {
            width = "100%", padding = 10, borderRadius = 8,
            backgroundColor = triggered and { 56, 168, 120, 15 } or Theme.BG_INPUT,
            flexDirection = "row", gap = 10, alignItems = "center",
            opacity = isFuture and 0.4 or 1.0,
            children = {
                UI.Panel {
                    width = 10, height = 10, borderRadius = 5,
                    backgroundColor = triggered and Theme.GOLD or (isFuture and Theme.TEXT_MUTED or Theme.RED),
                },
                UI.Panel {
                    flexShrink = 1, gap = 2,
                    children = {
                        UI.Label {
                            text = evt.year .. "年 " .. evt.title,
                            fontSize = 13,
                            fontColor = triggered and Theme.GOLD or (isFuture and Theme.TEXT_MUTED or Theme.TEXT_PRIMARY),
                        },
                        UI.Label {
                            text = isFuture and "未来之事……" or evt.desc,
                            fontSize = 10, fontColor = Theme.TEXT_MUTED, whiteSpace = "normal",
                        },
                    },
                },
            },
        }
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

return EventPage
