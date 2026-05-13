-- ============================================================================
-- 大明浮生志2 - 开局创建界面（图片卡片版）
-- 所有选项在一个屏幕内完成，不需要滚动
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")
local AudioManager = require("Systems.AudioManager")

local CreateGame = {}

--- 分区标题（紧凑版）
local function SectionLabel(text)
    return UI.Label {
        text = text,
        fontSize = 12,
        fontColor = Theme.GOLD,
        marginBottom = 2,
    }
end

-- ============================================================================
-- 图片映射表
-- ============================================================================
local SURNAME_IMGS = {
    ["李"] = Theme.IMG.SEAL_LI,
    ["王"] = Theme.IMG.SEAL_WANG,
    ["张"] = Theme.IMG.SEAL_ZHANG,
    ["刘"] = Theme.IMG.SEAL_LIU,
    ["陈"] = Theme.IMG.SEAL_CHEN,
    ["赵"] = Theme.IMG.SEAL_ZHAO,
    ["朱"] = Theme.IMG.SEAL_ZHU,
    ["杨"] = Theme.IMG.SEAL_YANG,
    ["黄"] = Theme.IMG.SEAL_HUANG,
    ["周"] = Theme.IMG.SEAL_ZHOU,
}

local ORIGIN_IMGS = {
    farmer   = Theme.IMG.ORIGIN_FARMER,
    landlord = Theme.IMG.ORIGIN_LANDLORD,
    military = Theme.IMG.ORIGIN_MILITARY,
}

local REGION_IMGS = {
    shaanbei = Theme.IMG.REGION_SHAANBEI,
    henan    = Theme.IMG.REGION_HENAN,
    jiangnan = Theme.IMG.REGION_JIANGNAN,
    huguang  = Theme.IMG.REGION_HUGUANG,
}

local MOTTO_IMGS = {
    study   = Theme.IMG.MOTTO_STUDY,
    martial = Theme.IMG.MOTTO_MARTIAL,
    trade   = Theme.IMG.MOTTO_TRADE,
    farm    = Theme.IMG.MOTTO_FARM,
    fortify = Theme.IMG.MOTTO_FORTIFY,
}

local DIFF_IMGS = {
    easy   = Theme.IMG.DIFF_EASY,
    normal = Theme.IMG.DIFF_NORMAL,
    hard   = Theme.IMG.DIFF_HARD,
}

-- ============================================================================
-- 信息展示区：显示当前各项选中的详情
-- ============================================================================
local function BuildInfoText(surname, originId, regionId, mottoId, diffId)
    local lines = {}
    lines[#lines + 1] = "【" .. surname .. "氏宗族】"
    for _, o in ipairs(GameData.ORIGINS) do
        if o.id == originId then
            lines[#lines + 1] = "出身: " .. o.name .. " — " .. o.desc
            lines[#lines + 1] = "  银" .. o.silver .. "  粮" .. o.grain .. "  布" .. o.cloth
            break
        end
    end
    for _, r in ipairs(GameData.REGIONS) do
        if r.id == regionId then
            lines[#lines + 1] = "地域: " .. r.name .. " — " .. r.desc
            break
        end
    end
    for _, m in ipairs(GameData.FAMILY_MOTTOS) do
        if m.id == mottoId then
            lines[#lines + 1] = "家训: " .. m.name .. " — " .. m.desc
            break
        end
    end
    for _, d in ipairs(GameData.DIFFICULTIES) do
        if d.id == diffId then
            lines[#lines + 1] = "难度: " .. d.name .. " — " .. d.desc
            break
        end
    end
    return table.concat(lines, "\n")
end

-- ============================================================================
-- 选中态样式
-- ============================================================================
local function imgSelectedStyle(color)
    return {
        borderWidth = 3,
        borderColor = color,
        opacity = 1.0,
    }
end

local function imgUnselectedStyle()
    return {
        borderWidth = 1,
        borderColor = { 200, 195, 185, 120 },
        opacity = 0.7,
    }
end

-- ============================================================================
-- 主创建函数
-- ============================================================================
function CreateGame.Create(callbacks)
    local selectedSurname = GameData.SURNAMES[1]
    local selectedOrigin = GameData.ORIGINS[1].id
    local selectedRegion = GameData.REGIONS[1].id
    local selectedMotto = GameData.FAMILY_MOTTOS[1].id
    local selectedDifficulty = "normal"
    local customSurname = ""
    local useCustom = false

    ---@type Panel
    local root = nil

    local diffColors = { easy = Theme.GREEN, normal = Theme.GOLD, hard = Theme.RED }

    -- ====== 更新函数 ======

    local function updateInfoText()
        if not root then return end
        local label = root:FindById("infoText")
        if label then
            local sn = useCustom and customSurname or selectedSurname
            if #sn == 0 then sn = "李" end
            label:SetText(BuildInfoText(sn, selectedOrigin, selectedRegion, selectedMotto, selectedDifficulty))
        end
    end

    local function updateSurnameSelection()
        if not root then return end
        for i, name in ipairs(GameData.SURNAMES) do
            local chip = root:FindById("surname_" .. i)
            if chip then
                local sel = (not useCustom) and (selectedSurname == name)
                chip:SetStyle(sel and imgSelectedStyle(Theme.GOLD) or imgUnselectedStyle())
            end
        end
        updateInfoText()
    end

    local function updateOriginSelection()
        if not root then return end
        for _, origin in ipairs(GameData.ORIGINS) do
            local card = root:FindById("origin_" .. origin.id)
            if card then
                local sel = (selectedOrigin == origin.id)
                card:SetStyle(sel and imgSelectedStyle(Theme.GOLD) or imgUnselectedStyle())
            end
        end
        updateInfoText()
    end

    local function updateRegionSelection()
        if not root then return end
        for _, region in ipairs(GameData.REGIONS) do
            local card = root:FindById("region_" .. region.id)
            if card then
                local sel = (selectedRegion == region.id)
                card:SetStyle(sel and imgSelectedStyle(Theme.PRIMARY) or imgUnselectedStyle())
            end
        end
        updateInfoText()
    end

    local function updateMottoSelection()
        if not root then return end
        for _, motto in ipairs(GameData.FAMILY_MOTTOS) do
            local card = root:FindById("motto_" .. motto.id)
            if card then
                local sel = (selectedMotto == motto.id)
                card:SetStyle(sel and imgSelectedStyle(Theme.GOLD) or imgUnselectedStyle())
            end
        end
        updateInfoText()
    end

    local function updateDiffSelection()
        if not root then return end
        for _, diff in ipairs(GameData.DIFFICULTIES) do
            local card = root:FindById("diff_" .. diff.id)
            if card then
                local sel = (selectedDifficulty == diff.id)
                local dc = diffColors[diff.id] or Theme.GOLD
                card:SetStyle(sel and imgSelectedStyle(dc) or imgUnselectedStyle())
            end
        end
        updateInfoText()
    end

    -- ====== 姓氏图片卡片 ======
    local surnameChips = {}
    for i, name in ipairs(GameData.SURNAMES) do
        local sel = (i == 1)
        local img = SURNAME_IMGS[name]
        surnameChips[#surnameChips + 1] = UI.Panel {
            id = "surname_" .. i,
            width = 40, height = 40,
            borderRadius = 6,
            borderWidth = sel and 3 or 1,
            borderColor = sel and Theme.GOLD or { 200, 195, 185, 120 },
            opacity = sel and 1.0 or 0.7,
            backgroundImage = img,
            backgroundFit = "cover",
            overflow = "hidden",
            transition = "all 0.15s easeOut",
            onClick = function(self)
                AudioManager.Select()
                selectedSurname = name
                useCustom = false
                updateSurnameSelection()
            end,
        }
    end

    -- ====== 出身图片卡片 ======
    local originCards = {}
    for _, origin in ipairs(GameData.ORIGINS) do
        local sel = (selectedOrigin == origin.id)
        local img = ORIGIN_IMGS[origin.id]
        originCards[#originCards + 1] = UI.Panel {
            id = "origin_" .. origin.id,
            height = 50,
            flexGrow = 1, flexBasis = 0,
            borderRadius = 10,
            borderWidth = sel and 3 or 1,
            borderColor = sel and Theme.GOLD or { 200, 195, 185, 120 },
            opacity = sel and 1.0 or 0.7,
            backgroundImage = img,
            backgroundFit = "cover",
            overflow = "hidden",
            transition = "all 0.15s easeOut",
            onClick = function(self)
                AudioManager.Select()
                selectedOrigin = origin.id
                updateOriginSelection()
            end,
        }
    end

    -- ====== 地域图片卡片 ======
    local regionCards = {}
    for _, region in ipairs(GameData.REGIONS) do
        local sel = (selectedRegion == region.id)
        local img = REGION_IMGS[region.id]
        regionCards[#regionCards + 1] = UI.Panel {
            id = "region_" .. region.id,
            height = 42,
            flexGrow = 1, flexBasis = 0,
            borderRadius = 10,
            borderWidth = sel and 3 or 1,
            borderColor = sel and Theme.PRIMARY or { 200, 195, 185, 120 },
            opacity = sel and 1.0 or 0.7,
            backgroundImage = img,
            backgroundFit = "cover",
            overflow = "hidden",
            transition = "all 0.15s easeOut",
            onClick = function(self)
                AudioManager.Select()
                selectedRegion = region.id
                updateRegionSelection()
            end,
        }
    end

    -- ====== 家训图片卡片 ======
    local mottoCards = {}
    for _, motto in ipairs(GameData.FAMILY_MOTTOS) do
        local sel = (selectedMotto == motto.id)
        local img = MOTTO_IMGS[motto.id]
        mottoCards[#mottoCards + 1] = UI.Panel {
            id = "motto_" .. motto.id,
            height = 42,
            flexGrow = 1, flexBasis = 0,
            borderRadius = 10,
            borderWidth = sel and 3 or 1,
            borderColor = sel and Theme.GOLD or { 200, 195, 185, 120 },
            opacity = sel and 1.0 or 0.7,
            backgroundImage = img,
            backgroundFit = "cover",
            overflow = "hidden",
            transition = "all 0.15s easeOut",
            onClick = function(self)
                AudioManager.Select()
                selectedMotto = motto.id
                updateMottoSelection()
            end,
        }
    end

    -- ====== 难度图片卡片 ======
    local diffCards = {}
    for _, diff in ipairs(GameData.DIFFICULTIES) do
        local sel = (selectedDifficulty == diff.id)
        local dc = diffColors[diff.id] or Theme.GOLD
        local img = DIFF_IMGS[diff.id]
        diffCards[#diffCards + 1] = UI.Panel {
            id = "diff_" .. diff.id,
            height = 42,
            flexGrow = 1, flexBasis = 0,
            borderRadius = 10,
            borderWidth = sel and 3 or 1,
            borderColor = sel and dc or { 200, 195, 185, 120 },
            opacity = sel and 1.0 or 0.7,
            backgroundImage = img,
            backgroundFit = "cover",
            overflow = "hidden",
            transition = "all 0.15s easeOut",
            onClick = function(self)
                AudioManager.Select()
                selectedDifficulty = diff.id
                updateDiffSelection()
            end,
        }
    end

    -- ====== 信息区初始文本 ======
    local infoText = BuildInfoText(selectedSurname, selectedOrigin, selectedRegion, selectedMotto, selectedDifficulty)

    -- ====== 整体布局 ======
    root = UI.Panel {
        id = "createGame",
        width = "100%", height = "100%",
        backgroundImage = Theme.IMG.MENU_BG,
        backgroundFit = "cover",
        flexDirection = "column",
        children = {
            -- 半透明遮罩
            UI.Panel {
                position = "absolute",
                top = 0, left = 0, right = 0, bottom = 0,
                backgroundColor = { 255, 252, 245, 140 },
                pointerEvents = "none",
            },

            -- 顶部标题栏
            UI.Panel {
                height = 44,
                flexDirection = "row",
                alignItems = "center",
                paddingHorizontal = 12,
                backgroundColor = { 255, 255, 255, 200 },
                borderBottomWidth = 1,
                borderBottomColor = Theme.BORDER,
                children = {
                    UI.Panel {
                        width = 32, height = 32,
                        borderRadius = 16,
                        justifyContent = "center",
                        alignItems = "center",
                        onClick = function()
                            AudioManager.Click()
                            if callbacks.onBack then callbacks.onBack() end
                        end,
                        children = {
                            UI.Label { text = "<", fontSize = 18, fontColor = Theme.GOLD },
                        },
                    },
                    UI.Label {
                        text = "开局创建",
                        fontSize = 16,
                        fontColor = Theme.TEXT_TITLE,
                        marginLeft = 6,
                    },
                },
            },

            -- 主内容区（ScrollView 防止小屏溢出）
            UI.ScrollView {
                flexGrow = 1,
                flexBasis = 0,
                paddingHorizontal = 12,
                paddingTop = 6,
                paddingBottom = 4,
                justifyContent = "center",
                alignItems = "center",
                children = {
                    -- 所有选择区域
                    UI.Panel {
                        width = "100%",
                        gap = 5,
                        children = {
                            -- 姓氏
                            UI.Panel {
                                width = "100%", gap = 2,
                                children = {
                                    SectionLabel("宗族姓氏"),
                                    UI.Panel {
                                        flexDirection = "row", flexWrap = "wrap", gap = 6,
                                        children = surnameChips,
                                    },
                                    UI.Panel {
                                        flexDirection = "row", alignItems = "center", gap = 6, marginTop = 1,
                                        children = {
                                            UI.Label { text = "自定义:", fontSize = 11, fontColor = Theme.TEXT_SECONDARY },
                                            UI.TextField {
                                                id = "customSurnameField",
                                                placeholder = "输入",
                                                maxLength = 2,
                                                width = 60, height = 28,
                                                fontSize = 12,
                                                onChange = function(self, v)
                                                    if v and #v > 0 then
                                                        customSurname = v
                                                        selectedSurname = v
                                                        useCustom = true
                                                        updateSurnameSelection()
                                                    end
                                                end,
                                            },
                                        },
                                    },
                                },
                            },
                            -- 出身
                            UI.Panel {
                                width = "100%", gap = 2,
                                children = {
                                    SectionLabel("开局出身"),
                                    UI.Panel { flexDirection = "row", gap = 6, width = "100%", children = originCards },
                                },
                            },
                            -- 地域
                            UI.Panel {
                                width = "100%", gap = 2,
                                children = {
                                    SectionLabel("开局地域"),
                                    UI.Panel { flexDirection = "row", gap = 6, width = "100%", children = regionCards },
                                },
                            },
                            -- 家训
                            UI.Panel {
                                width = "100%", gap = 2,
                                children = {
                                    SectionLabel("家训（永久增益）"),
                                    UI.Panel { flexDirection = "row", gap = 5, width = "100%", children = mottoCards },
                                },
                            },
                            -- 难度
                            UI.Panel {
                                width = "100%", gap = 2,
                                children = {
                                    SectionLabel("游戏难度"),
                                    UI.Panel { flexDirection = "row", gap = 6, width = "100%", children = diffCards },
                                },
                            },
                            -- 选中项信息
                            UI.Panel {
                                width = "100%",
                                padding = 8,
                                borderRadius = 8,
                                backgroundColor = { 255, 255, 255, 210 },
                                borderWidth = 1,
                                borderColor = Theme.BORDER,
                                children = {
                                    UI.Label {
                                        id = "infoText",
                                        text = infoText,
                                        fontSize = 10,
                                        fontColor = Theme.TEXT_SECONDARY,
                                        lineHeight = 1.4,
                                        whiteSpace = "normal",
                                    },
                                },
                            },
                            -- 开始征程按钮（与主菜单风格统一）
                            UI.Panel {
                                width = "100%",
                                height = 100,
                                onClick = function(self)
                                    AudioManager.Click()
                                    local finalSurname = useCustom and customSurname or selectedSurname
                                    if #finalSurname == 0 then finalSurname = "李" end
                                    if callbacks.onConfirm then
                                        callbacks.onConfirm(finalSurname, selectedOrigin, selectedRegion, selectedMotto, selectedDifficulty)
                                    end
                                end,
                                children = {
                                    UI.Panel {
                                        width = "100%",
                                        height = "100%",
                                        backgroundImage = Theme.IMG.BTN_START_JOURNEY,
                                        backgroundFit = "contain",
                                    },
                                },
                            },
                        },
                    },
                },
            },
        },
    }

    return root
end

return CreateGame
