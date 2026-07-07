-- ============================================================================
-- 大明浮生志2 - 族人列表页面（从 GameScreen.lua 拆分）
-- MemberPage.Create(PageTitle, screen) 创建族人管理页面
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")
local AudioManager = require("Systems.AudioManager")

local MemberPage = {}

-- ============================================================================
-- 族人列表筛选状态（模块局部）
-- ============================================================================

local memberFilter_ = "全部"       -- "全部"|"在家"|"读书"|"经商"|"从军"|"生病"
local memberSort_ = "age"          -- "age"|"health"|"study"|"martial"
local memberSearch_ = ""           -- 搜索关键字

-- ============================================================================
-- 族人卡片
-- ============================================================================

local function CreateMemberCard(member, screen)
    local stateColor = Theme.TEXT_SECONDARY
    if member.state == "生病" then stateColor = Theme.RED
    elseif member.state == "从军" or member.state == "出征" then stateColor = Theme.BLUE
    elseif member.state == "读书" then stateColor = { 100, 180, 120, 255 }
    elseif member.state == "经商" then stateColor = Theme.GOLD_DARK
    end
    local genderIcon = member.gender == "male" and "男" or "女"
    local genderColor = member.gender == "male" and Theme.BLUE or Theme.RED

    return UI.Panel {
        width = "100%", padding = 10, borderRadius = 8,
        backgroundColor = Theme.BG_WHITE,
        borderWidth = 1, borderColor = Theme.BORDER, gap = 4,
        onTap = function() AudioManager.Click() screen.ShowMemberDetail(member) end,
        children = {
            UI.Panel {
                flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                children = {
                    UI.Panel {
                        flexDirection = "row", alignItems = "center", gap = 4,
                        children = {
                            UI.Label { text = member.name, fontSize = 16, fontColor = Theme.TEXT_PRIMARY },
                            UI.Label { text = genderIcon, fontSize = 14, fontColor = genderColor },
                        },
                    },
                    UI.Panel {
                        paddingHorizontal = 6, paddingVertical = 2, borderRadius = 4,
                        backgroundColor = { stateColor[1], stateColor[2], stateColor[3], 30 },
                        children = { UI.Label { text = member.state, fontSize = 12, fontColor = stateColor } },
                    },
                },
            },
            UI.Panel {
                flexDirection = "row", gap = 8,
                children = {
                    UI.Label { text = member.age .. "岁", fontSize = 13, fontColor = Theme.TEXT_SECONDARY },
                    UI.Label { text = member.identity, fontSize = 13, fontColor = Theme.TEXT_SECONDARY },
                    UI.Label { text = member.talent and member.talent.name or "", fontSize = 13, fontColor = Theme.GOLD_DARK },
                },
            },
            UI.Panel {
                flexDirection = "row", gap = 6,
                children = {
                    UI.Label { text = "健" .. member.health, fontSize = 12, fontColor = member.health > 50 and Theme.GREEN or Theme.RED },
                    UI.Label { text = "学" .. member.study, fontSize = 12, fontColor = Theme.BLUE },
                    UI.Label { text = "武" .. member.martial, fontSize = 12, fontColor = Theme.RED },
                },
            },
        },
    }
end

-- ============================================================================
-- 族人列表页面
-- ============================================================================

---@param PageTitle fun(title: string, subtitle?: string): table
---@param screen table GameScreen 引用，用于回调
function MemberPage.Create(PageTitle, screen)
    local allMembers = GameData.GetAliveMembers()

    -- 1. 搜索过滤
    local filtered = {}
    for _, m in ipairs(allMembers) do
        local matchSearch = (memberSearch_ == "" or string.find(m.name, memberSearch_, 1, true))
        local matchFilter = (memberFilter_ == "全部" or m.state == memberFilter_)
        if matchSearch and matchFilter then
            filtered[#filtered + 1] = m
        end
    end

    -- 2. 排序
    local sortFns = {
        age = function(a, b) return a.age > b.age end,
        health = function(a, b) return a.health > b.health end,
        study = function(a, b) return a.study > b.study end,
        martial = function(a, b) return a.martial > b.martial end,
    }
    table.sort(filtered, sortFns[memberSort_] or sortFns.age)

    -- 3. 构建筛选按钮
    local FILTER_OPTIONS = { "全部", "在家", "读书", "经商", "从军", "生病" }
    local filterBtns = {}
    for _, opt in ipairs(FILTER_OPTIONS) do
        local active = (memberFilter_ == opt)
        filterBtns[#filterBtns + 1] = UI.Panel {
            paddingHorizontal = 10, paddingVertical = 4, borderRadius = 12,
            backgroundColor = active and Theme.GOLD_DARK or Theme.BG_INPUT,
            borderWidth = 1, borderColor = active and Theme.GOLD or Theme.BORDER,
            onClick = function(self)
                AudioManager.Click()
                memberFilter_ = opt
                screen.RefreshContent()
            end,
            children = { UI.Label { text = opt, fontSize = 13, fontColor = active and Theme.TEXT_WHITE or Theme.TEXT_SECONDARY } },
        }
    end

    -- 4. 构建排序按钮
    local SORT_OPTIONS = {
        { id = "age", label = "年龄" }, { id = "health", label = "健康" },
        { id = "study", label = "学识" }, { id = "martial", label = "武力" },
    }
    local sortBtns = {}
    for _, opt in ipairs(SORT_OPTIONS) do
        local active = (memberSort_ == opt.id)
        sortBtns[#sortBtns + 1] = UI.Panel {
            paddingHorizontal = 8, paddingVertical = 3, borderRadius = 4,
            backgroundColor = active and { 56, 168, 120, 30 } or { 0, 0, 0, 0 },
            onClick = function(self)
                AudioManager.Click()
                memberSort_ = opt.id
                screen.RefreshContent()
            end,
            children = { UI.Label {
                text = active and ("▼" .. opt.label) or opt.label,
                fontSize = 13, fontColor = active and Theme.GREEN or Theme.TEXT_MUTED,
            }},
        }
    end

    -- 5. 族人卡片
    local memberCards = {}
    for _, m in ipairs(filtered) do
        memberCards[#memberCards + 1] = CreateMemberCard(m, screen)
    end
    if #memberCards == 0 then
        memberCards[#memberCards + 1] = UI.Label {
            text = memberSearch_ ~= "" and "未找到匹配的族人" or "宗族无人存活……",
            fontSize = 16, fontColor = Theme.TEXT_MUTED, textAlign = "center", marginTop = 20,
        }
    end

    return UI.ScrollView {
        width = "100%", flexGrow = 1, flexBasis = 0,
        backgroundColor = { 0, 0, 0, 0 },
        children = {
            UI.Panel {
                width = "100%", gap = 8, padding = 12, paddingBottom = 20,
                children = {
                    PageTitle("族人管理", "共 " .. #allMembers .. " 人" .. (#filtered ~= #allMembers and ("（显示" .. #filtered .. "人）") or "")),

                    -- 搜索框
                    UI.TextField {
                        width = "100%",
                        placeholder = "搜索族人姓名…",
                        value = memberSearch_,
                        fontSize = 15,
                        onChange = function(self, text)
                            memberSearch_ = text
                            screen.RefreshContent()
                        end,
                    },

                    -- 筛选栏
                    UI.ScrollView {
                        width = "100%", height = 32, scrollDirection = "horizontal",
                        backgroundColor = { 0, 0, 0, 0 },
                        children = {
                            UI.Panel { flexDirection = "row", gap = 6, alignItems = "center", children = filterBtns },
                        },
                    },

                    -- 排序+批量操作栏
                    UI.Panel {
                        width = "100%", flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                        children = {
                            UI.Panel { flexDirection = "row", gap = 4, alignItems = "center", children = {
                                UI.Label { text = "排序:", fontSize = 13, fontColor = Theme.TEXT_MUTED },
                                table.unpack(sortBtns),
                            }},
                            UI.Panel {
                                paddingHorizontal = 12, paddingVertical = 6, borderRadius = 6,
                                backgroundColor = Theme.BG_INPUT, borderWidth = 1, borderColor = Theme.GOLD_DARK,
                                onClick = function(self) AudioManager.Click() screen.ShowBatchAssign() end,
                                children = { UI.Label { text = "批量安排", fontSize = 14, fontColor = Theme.GOLD } },
                            },
                        },
                    },

                    table.unpack(memberCards),
                },
            },
        },
    }
end

return MemberPage
