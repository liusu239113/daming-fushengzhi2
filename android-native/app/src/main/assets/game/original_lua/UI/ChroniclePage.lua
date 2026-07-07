-- ============================================================================
-- 大明浮生志2 - 家族志（族谱书）
-- 翻页式书本UI，记录家族历代成员信息、生平事迹
-- ============================================================================

local UI = require("urhox-libs/UI")
local GameData = require("Data.GameData")
local Theme = require("UI.Theme")
local EquipmentSystem = require("Systems.EquipmentSystem")
local MemberData = require("Data.MemberData")

local ChroniclePage = {}

-- ============================================================================
-- 辅助函数
-- ============================================================================

-- 获取全部族人（含已故），按世代、年龄排序
local function GetAllMembersSorted()
    local s = GameData.state
    if not s then return {} end
    local list = {}
    for _, m in ipairs(s.members) do
        list[#list + 1] = m
    end
    table.sort(list, function(a, b)
        if a.generation ~= b.generation then return a.generation < b.generation end
        if a.alive ~= b.alive then return a.alive end -- 活人排前面
        return (a.id or 0) < (b.id or 0)
    end)
    return list
end

-- 按世代分组
local function GroupByGeneration(members)
    local groups = {}
    local genOrder = {}
    for _, m in ipairs(members) do
        local gen = m.generation or 1
        if not groups[gen] then
            groups[gen] = {}
            genOrder[#genOrder + 1] = gen
        end
        groups[gen][#groups[gen] + 1] = m
    end
    table.sort(genOrder)
    return groups, genOrder
end

-- 获取成员状态文字
local function GetStatusText(m)
    if not m.alive then
        local cause = m.deathCause or m.state or "亡故"
        local ageText = m.deathAge and ("享年" .. m.deathAge .. "岁") or ""
        local yearText = m.deathYear and (m.deathYear .. "年") or ""
        return yearText .. cause .. " " .. ageText
    end
    return m.state or "在家"
end

-- 获取天赋文字
local function GetTalentText(m)
    if m.talent then return m.talent.name or m.talent.id end
    return nil
end

-- 获取配偶名
local function GetSpouseName(m)
    if m.spouseId then
        local spouse = GameData.GetMember(m.spouseId)
        if spouse then return spouse.name end
    end
    return nil
end

-- 获取子女名列表
local function GetChildrenNames(m)
    if not m.childrenIds or #m.childrenIds == 0 then return nil end
    local names = {}
    for _, cid in ipairs(m.childrenIds) do
        local child = GameData.GetMember(cid)
        if child then names[#names + 1] = child.name end
    end
    if #names == 0 then return nil end
    return table.concat(names, "、")
end

-- 数字转中文
local GEN_NAMES = { "一", "二", "三", "四", "五", "六", "七", "八", "九", "十",
                    "十一", "十二", "十三", "十四", "十五" }
local function GenToChinese(n)
    return GEN_NAMES[n] or tostring(n)
end

-- ============================================================================
-- 页面构建
-- ============================================================================

-- 书籍颜色
local BOOK_BG = { 48, 36, 24, 255 }          -- 书皮深褐
local PAGE_BG = { 255, 250, 235, 255 }        -- 内页泛黄
local PAGE_BORDER = { 200, 180, 140, 120 }    -- 页边线
local INK_PRIMARY = { 40, 30, 20, 255 }       -- 墨色主字
local INK_SECONDARY = { 90, 75, 55, 255 }     -- 墨色副字
local INK_MUTED = { 140, 125, 100, 255 }      -- 浅墨
local INK_RED = { 180, 50, 40, 255 }          -- 朱砂色（重点）
local INK_GOLD = { 170, 140, 50, 255 }        -- 金墨

-- 创建成员传记卡片
local function CreateMemberCard(m)
    local isAlive = m.alive
    local genderIcon = m.gender == "male" and "♂" or "♀"
    local statusColor = isAlive and { 60, 130, 70, 255 } or INK_MUTED

    -- 属性行
    local statsText = "武" .. (m.martial or 0) .. " 学" .. (m.study or 0) .. " 体" .. (m.health or 0)

    -- 装备概要
    local eqText = nil
    if m.equipment then
        local parts = {}
        for _, slot in ipairs(GameData.EQUIPMENT_SLOTS or { "weapon", "armor", "accessory" }) do
            if m.equipment[slot] then
                local eq = GameData.GetEquipment(m.equipment[slot])
                if eq then parts[#parts + 1] = eq.name end
            end
        end
        if #parts > 0 then eqText = table.concat(parts, "、") end
    end

    local talent = GetTalentText(m)
    local spouseName = GetSpouseName(m)
    local childrenText = GetChildrenNames(m)

    local detailRows = {}

    -- 基本信息
    detailRows[#detailRows + 1] = UI.Panel {
        flexDirection = "row", gap = 6, alignItems = "center", flexWrap = "wrap",
        children = {
            UI.Label { text = m.name, fontSize = 15, fontColor = INK_PRIMARY, fontWeight = "bold" },
            UI.Label { text = genderIcon, fontSize = 13, fontColor = m.gender == "male" and { 70, 120, 180, 255 } or { 180, 100, 120, 255 } },
            UI.Label { text = isAlive and (m.age .. "岁") or ("†" .. (m.deathAge or "?")), fontSize = 12, fontColor = statusColor },
            m.identity and m.identity ~= "白丁" and UI.Label {
                text = m.identity, fontSize = 11, fontColor = INK_GOLD,
                paddingHorizontal = 4, paddingVertical = 1, borderRadius = 3,
                backgroundColor = { 170, 140, 50, 20 },
            } or nil,
            talent and UI.Label {
                text = talent, fontSize = 11, fontColor = { 130, 80, 160, 255 },
                paddingHorizontal = 4, paddingVertical = 1, borderRadius = 3,
                backgroundColor = { 130, 80, 160, 15 },
            } or nil,
        },
    }

    -- 状态行
    detailRows[#detailRows + 1] = UI.Label {
        text = isAlive and ("现状：" .. (m.state or "在家")) or ("归宿：" .. GetStatusText(m)),
        fontSize = 12, fontColor = isAlive and INK_SECONDARY or INK_MUTED,
    }

    -- 属性
    detailRows[#detailRows + 1] = UI.Label { text = statsText, fontSize = 12, fontColor = INK_SECONDARY }

    -- 军衔
    if m.militaryRank then
        detailRows[#detailRows + 1] = UI.Label { text = "军衔：" .. m.militaryRank, fontSize = 12, fontColor = { 120, 90, 50, 255 } }
    end

    -- 配偶
    if spouseName then
        detailRows[#detailRows + 1] = UI.Label { text = "配偶：" .. spouseName, fontSize = 12, fontColor = INK_SECONDARY }
    end

    -- 子女
    if childrenText then
        detailRows[#detailRows + 1] = UI.Label { text = "子嗣：" .. childrenText, fontSize = 12, fontColor = INK_SECONDARY }
    end

    -- 装备
    if eqText then
        detailRows[#detailRows + 1] = UI.Label { text = "器物：" .. eqText, fontSize = 11, fontColor = INK_MUTED }
    end

    return UI.Panel {
        width = "100%",
        paddingHorizontal = 12, paddingVertical = 8,
        marginBottom = 6,
        backgroundColor = isAlive and { 255, 252, 240, 255 } or { 245, 240, 228, 255 },
        borderRadius = 4,
        borderWidth = 1,
        borderColor = isAlive and { 210, 195, 160, 100 } or { 190, 180, 165, 80 },
        gap = 3,
        children = detailRows,
    }
end

-- ============================================================================
-- 主入口
-- ============================================================================

function ChroniclePage.Create(PageTitle, screen)
    local s = GameData.state
    if not s then
        return UI.Panel {
            width = "100%", height = "100%", justifyContent = "center", alignItems = "center",
            children = { UI.Label { text = "暂无家族数据", fontSize = 14, fontColor = Theme.TEXT_MUTED } },
        }
    end

    local allMembers = GetAllMembersSorted()
    local groups, genOrder = GroupByGeneration(allMembers)

    -- 统计
    local aliveCount = 0
    local deadCount = 0
    for _, m in ipairs(allMembers) do
        if m.alive then aliveCount = aliveCount + 1 else deadCount = deadCount + 1 end
    end

    -- 当前年份
    local yearText = s.year .. "年"
    local rankName = GameData.CLAN_RANKS[s.clanRank] or "寒门"
    local totalGen = #genOrder

    -- === 构建页面内容 ===
    local pages = {}

    -- -------- 第一页：家族总览 --------
    local overviewChildren = {
        -- 卷首题字
        UI.Panel {
            width = "100%", alignItems = "center", paddingVertical = 16, gap = 6,
            children = {
                UI.Label { text = "◆ " .. s.surname .. "氏家族志 ◆", fontSize = 20, fontColor = INK_PRIMARY, fontWeight = "bold" },
                UI.Panel { width = 60, height = 1, backgroundColor = INK_GOLD },
                UI.Label { text = "「" .. (s.clanName or s.surname .. "氏宗族") .. "」", fontSize = 14, fontColor = INK_GOLD },
            },
        },
        -- 基本信息
        UI.Panel {
            width = "100%", paddingHorizontal = 16, paddingVertical = 10, gap = 6,
            children = {
                UI.Label { text = "时值    " .. yearText, fontSize = 13, fontColor = INK_SECONDARY },
                UI.Label { text = "品级    " .. rankName, fontSize = 13, fontColor = INK_SECONDARY },
                UI.Label { text = "世代    传承" .. GenToChinese(totalGen) .. "代", fontSize = 13, fontColor = INK_SECONDARY },
                UI.Label { text = "在世    " .. aliveCount .. "口", fontSize = 13, fontColor = INK_SECONDARY },
                UI.Label { text = "故去    " .. deadCount .. "人", fontSize = 13, fontColor = INK_MUTED },
                UI.Label { text = "累计    " .. #allMembers .. "人", fontSize = 13, fontColor = INK_SECONDARY },
            },
        },
        -- 分割
        UI.Panel { width = "80%", height = 1, backgroundColor = PAGE_BORDER, alignSelf = "center", marginVertical = 8 },
        -- 家训
        UI.Panel {
            width = "100%", paddingHorizontal = 16, gap = 4,
            children = {
                UI.Label { text = "家训", fontSize = 14, fontColor = INK_PRIMARY, fontWeight = "bold" },
                UI.Label {
                    text = s.familyMottoId and (GameData.GetMottoById(s.familyMottoId) and GameData.GetMottoById(s.familyMottoId).name or "无") or "无",
                    fontSize = 13, fontColor = INK_GOLD,
                },
            },
        },
        -- 地域
        UI.Panel {
            width = "100%", paddingHorizontal = 16, marginTop = 6, gap = 4,
            children = {
                UI.Label { text = "扎根", fontSize = 14, fontColor = INK_PRIMARY, fontWeight = "bold" },
                (function()
                    local regionName = "未知"
                    for _, r in ipairs(GameData.REGIONS) do
                        if r.id == s.regionId then regionName = r.name break end
                    end
                    return UI.Label { text = regionName, fontSize = 13, fontColor = INK_SECONDARY }
                end)(),
            },
        },
    }

    -- 事件摘要（最近 8 条大事记）
    if s.eventLog and #s.eventLog > 0 then
        local logEntries = {}
        logEntries[#logEntries + 1] = UI.Label { text = "近事录", fontSize = 14, fontColor = INK_PRIMARY, fontWeight = "bold", marginTop = 10 }
        local count = math.min(8, #s.eventLog)
        for i = 1, count do
            local log = s.eventLog[i]
            logEntries[#logEntries + 1] = UI.Label {
                text = (log.year or "") .. "年 " .. (log.text or ""),
                fontSize = 11, fontColor = INK_MUTED,
            }
        end
        overviewChildren[#overviewChildren + 1] = UI.Panel {
            width = "100%", paddingHorizontal = 16, gap = 3,
            children = logEntries,
        }
    end

    pages[#pages + 1] = {
        title = "卷首 · 家族总览",
        content = overviewChildren,
    }

    -- -------- 后续页面：每代一页 --------
    for _, gen in ipairs(genOrder) do
        local genMembers = groups[gen]
        local memberCards = {}

        -- 世代标题
        memberCards[#memberCards + 1] = UI.Panel {
            width = "100%", alignItems = "center", paddingVertical = 8, gap = 4,
            children = {
                UI.Label {
                    text = "第" .. GenToChinese(gen) .. "代",
                    fontSize = 16, fontColor = INK_PRIMARY, fontWeight = "bold",
                },
                UI.Panel { width = 40, height = 1, backgroundColor = INK_GOLD },
                UI.Label {
                    text = "共" .. #genMembers .. "人",
                    fontSize = 12, fontColor = INK_MUTED,
                },
            },
        }

        -- 每个成员卡片
        for _, m in ipairs(genMembers) do
            memberCards[#memberCards + 1] = CreateMemberCard(m)
        end

        pages[#pages + 1] = {
            title = "第" .. GenToChinese(gen) .. "代传记",
            content = memberCards,
        }
    end

    -- ============================================================================
    -- 翻页 UI
    -- ============================================================================
    local currentPage = 1
    local totalPages = #pages

    -- 用于刷新的引用
    local pageContainer = nil
    local pageTitle = nil
    local pageCounter = nil

    -- 根据页索引重新构建内容元素（每次都新建UI对象，避免复用已挂载元素导致AddChild静默失败）
    local function BuildPageContent(idx)
        if idx == 1 then
            -- 家族总览页：每次重新构建所有UI对象
            local regionName = "未知"
            for _, r in ipairs(GameData.REGIONS) do
                if r.id == s.regionId then regionName = r.name break end
            end
            local mottoText = s.familyMottoId and (GameData.GetMottoById(s.familyMottoId) and GameData.GetMottoById(s.familyMottoId).name or "无") or "无"
            local items = {
                UI.Panel {
                    width = "100%", alignItems = "center", paddingVertical = 16, gap = 6,
                    children = {
                        UI.Label { text = "◆ " .. s.surname .. "氏家族志 ◆", fontSize = 20, fontColor = INK_PRIMARY, fontWeight = "bold" },
                        UI.Panel { width = 60, height = 1, backgroundColor = INK_GOLD },
                        UI.Label { text = "「" .. (s.clanName or s.surname .. "氏宗族") .. "」", fontSize = 14, fontColor = INK_GOLD },
                    },
                },
                UI.Panel {
                    width = "100%", paddingHorizontal = 16, paddingVertical = 10, gap = 6,
                    children = {
                        UI.Label { text = "时值    " .. yearText, fontSize = 13, fontColor = INK_SECONDARY },
                        UI.Label { text = "品级    " .. rankName, fontSize = 13, fontColor = INK_SECONDARY },
                        UI.Label { text = "世代    传承" .. GenToChinese(totalGen) .. "代", fontSize = 13, fontColor = INK_SECONDARY },
                        UI.Label { text = "在世    " .. aliveCount .. "口", fontSize = 13, fontColor = INK_SECONDARY },
                        UI.Label { text = "故去    " .. deadCount .. "人", fontSize = 13, fontColor = INK_MUTED },
                        UI.Label { text = "累计    " .. #allMembers .. "人", fontSize = 13, fontColor = INK_SECONDARY },
                    },
                },
                UI.Panel { width = "80%", height = 1, backgroundColor = PAGE_BORDER, alignSelf = "center", marginVertical = 8 },
                UI.Panel {
                    width = "100%", paddingHorizontal = 16, gap = 4,
                    children = {
                        UI.Label { text = "家训", fontSize = 14, fontColor = INK_PRIMARY, fontWeight = "bold" },
                        UI.Label { text = mottoText, fontSize = 13, fontColor = INK_GOLD },
                    },
                },
                UI.Panel {
                    width = "100%", paddingHorizontal = 16, marginTop = 6, gap = 4,
                    children = {
                        UI.Label { text = "扎根", fontSize = 14, fontColor = INK_PRIMARY, fontWeight = "bold" },
                        UI.Label { text = regionName, fontSize = 13, fontColor = INK_SECONDARY },
                    },
                },
            }
            -- 大事记
            if s.eventLog and #s.eventLog > 0 then
                local logEntries = {}
                logEntries[#logEntries + 1] = UI.Label { text = "近事录", fontSize = 14, fontColor = INK_PRIMARY, fontWeight = "bold", marginTop = 10 }
                local count = math.min(8, #s.eventLog)
                for i = 1, count do
                    local log = s.eventLog[i]
                    logEntries[#logEntries + 1] = UI.Label {
                        text = (log.year or "") .. "年 " .. (log.text or ""),
                        fontSize = 11, fontColor = INK_MUTED,
                    }
                end
                items[#items + 1] = UI.Panel { width = "100%", paddingHorizontal = 16, gap = 3, children = logEntries }
            end
            return items
        else
            -- 世代传记页：重新生成成员卡片
            local gen = genOrder[idx - 1]
            if not gen then return {} end
            local genMembers = groups[gen]
            local memberCards = {}
            memberCards[#memberCards + 1] = UI.Panel {
                width = "100%", alignItems = "center", paddingVertical = 8, gap = 4,
                children = {
                    UI.Label { text = "第" .. GenToChinese(gen) .. "代", fontSize = 16, fontColor = INK_PRIMARY, fontWeight = "bold" },
                    UI.Panel { width = 40, height = 1, backgroundColor = INK_GOLD },
                    UI.Label { text = "共" .. #genMembers .. "人", fontSize = 12, fontColor = INK_MUTED },
                },
            }
            for _, m in ipairs(genMembers) do
                memberCards[#memberCards + 1] = CreateMemberCard(m)
            end
            return memberCards
        end
    end

    local function RenderPage(idx)
        if idx < 1 then idx = 1 end
        if idx > totalPages then idx = totalPages end
        currentPage = idx

        if pageContainer then
            pageContainer:ClearChildren()
            local newContent = BuildPageContent(currentPage)
            for _, child in ipairs(newContent) do
                pageContainer:AddChild(child)
            end
        end
        if pageTitle then
            pageTitle:SetText(pages[currentPage] and pages[currentPage].title or "")
        end
        if pageCounter then
            pageCounter:SetText(currentPage .. " / " .. totalPages)
        end
    end

    -- 书本外壳
    local bookUI = UI.ScrollView {
        width = "100%", flexGrow = 1,
        children = {
            UI.Panel {
                width = "100%",
                paddingHorizontal = 8, paddingVertical = 10,
                gap = 0,
                children = {
                    -- === 书皮头部 ===
                    UI.Panel {
                        width = "100%",
                        backgroundColor = BOOK_BG,
                        borderTopLeftRadius = 8, borderTopRightRadius = 8,
                        paddingVertical = 10, paddingHorizontal = 16,
                        alignItems = "center",
                        children = {
                            UI.Label {
                                id = "chronicle_title",
                                text = pages[1] and pages[1].title or "",
                                fontSize = 15, fontColor = { 220, 200, 160, 255 }, fontWeight = "bold",
                                ref = function(el) pageTitle = el end,
                            },
                        },
                    },

                    -- === 内页区域 ===
                    UI.Panel {
                        width = "100%",
                        backgroundColor = PAGE_BG,
                        borderWidth = 1, borderColor = PAGE_BORDER,
                        paddingHorizontal = 4, paddingVertical = 10,
                        minHeight = 300,
                        id = "chronicle_page_content",
                        ref = function(el)
                            pageContainer = el
                            -- 首次渲染第一页（避免直接传children导致元素被挂载后无法重新AddChild）
                            RenderPage(1)
                        end,
                        children = {},
                    },

                    -- === 翻页控制栏 ===
                    UI.Panel {
                        width = "100%",
                        backgroundColor = BOOK_BG,
                        borderBottomLeftRadius = 8, borderBottomRightRadius = 8,
                        paddingVertical = 8, paddingHorizontal = 16,
                        flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                        children = {
                            -- 上一页
                            UI.Panel {
                                paddingHorizontal = 16, paddingVertical = 6, borderRadius = 4,
                                backgroundColor = { 80, 60, 40, 255 },
                                onTap = function()
                                    if currentPage > 1 then
                                        RenderPage(currentPage - 1)
                                    end
                                end,
                                children = {
                                    UI.Label { text = "◁ 前页", fontSize = 13, fontColor = { 220, 200, 160, 255 } },
                                },
                            },
                            -- 页码
                            UI.Label {
                                id = "chronicle_counter",
                                text = "1 / " .. totalPages,
                                fontSize = 12, fontColor = { 200, 185, 150, 255 },
                                ref = function(el) pageCounter = el end,
                            },
                            -- 下一页
                            UI.Panel {
                                paddingHorizontal = 16, paddingVertical = 6, borderRadius = 4,
                                backgroundColor = { 80, 60, 40, 255 },
                                onTap = function()
                                    if currentPage < totalPages then
                                        RenderPage(currentPage + 1)
                                    end
                                end,
                                children = {
                                    UI.Label { text = "后页 ▷", fontSize = 13, fontColor = { 220, 200, 160, 255 } },
                                },
                            },
                        },
                    },
                },
            },
        },
    }

    return bookUI
end

return ChroniclePage
