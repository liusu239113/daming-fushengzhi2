-- ============================================================================
-- 大明浮生志2 - 产业管理页面
-- 从 GameScreen.lua 拆分，包含产业卡片和产业管理页面
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")
local AudioManager = require("Systems.AudioManager")
local Toast = require("Systems.Toast")

local IndustryPage = {}

-- ============================================================================
-- 产业图片映射
-- ============================================================================
local INDUSTRY_IMAGES = {
    hemp_field     = "image/industry_hemp_field_20260512185339.png",
    dry_field      = "image/industry_dry_field.png",
    paddy_field    = "image/industry_paddy_field_20260512085628.png",
    fish_pond      = "image/industry_fish_pond_20260512085810.png",
    livestock      = "image/industry_livestock_20260512085647.png",
    handicraft     = "image/industry_handicraft_20260513183510.png",
    shop           = "image/industry_shop_20260512085629.png",
    tea_garden     = "image/industry_tea_garden_20260512085644.png",
    inn            = "image/industry_inn_20260512085621.png",
    herb_shop      = "image/industry_herb_shop_20260512085622.png",
    workshop       = "image/industry_workshop_20260512085622.png",
    fort           = "image/industry_fort_20260512085726.png",
    brewery        = "image/industry_brewery_20260512085619.png",
    smithy         = "image/industry_smithy_20260512085926.png",
    horse_ranch    = "image/industry_horse_ranch_20260512085936.png",
    escort         = "image/industry_escort_20260512090135.png",
    bookshop       = "image/industry_bookshop_20260512085927.png",
    salt_field     = "image/industry_salt_field_20260512085917.png",
    money_house    = "image/industry_money_house_20260512085914.png",
    fleet          = "image/industry_fleet_20260512085933.png",
    estate         = "image/industry_estate_20260512090327.png",
    fertile_field  = "image/industry_fertile_field_20260512085930.png",
    trade_house    = "image/industry_trade_house_20260512090335.png",
    silk_house     = "image/industry_silk_house_20260512090831.png",
    -- 品阶7-9新产业
    pawnshop           = "image/industry_pawnshop_20260515215243.png",
    dye_house          = "image/industry_dye_house_20260515215243.png",
    private_school     = "image/industry_private_school_20260515215244.png",
    canal_wharf        = "image/industry_canal_wharf_20260515215243.png",
    arsenal            = "image/industry_arsenal_20260515215255.png",
    weaving_bureau     = "image/industry_weaving_bureau_20260515215249.png",
    imperial_merchant  = "image/industry_imperial_merchant_20260515215244.png",
    customs_house      = "image/industry_customs_house_20260515215243.png",
    grand_farmland     = "image/industry_grand_farmland_20260515215243.png",
    piaohao            = "image/industry_piaohao_20260515215247.png",
}

-- ============================================================================
-- 产业卡片
-- ============================================================================

local function CreateIndustryCard(industry, PageTitle, screen)
    local indType = GameData.GetIndustryType(industry.typeId)
    if not indType then return UI.Panel {} end
    local assignee = industry.assignedMemberId and GameData.GetMember(industry.assignedMemberId) or nil
    local assigneeName = (assignee and assignee.alive) and assignee.name or "无人管理"
    local isPartTime = false
    local manageMul = 1.0
    local stateLabel = ""
    if assignee and assignee.alive then
        isPartTime = assignee.state ~= "在家" and assignee.state ~= "生病"
        manageMul = isPartTime and 1.15 or 1.3
        if isPartTime then
            stateLabel = "（" .. assignee.state .. "中）"
        end
    end

    -- 资源名映射
    local RES_NAMES = { silver = "银两", grain = "粮食", cloth = "布匹", fame = "声望", none = "防御" }

    -- A4: 对数递减公式（与MonthlyUpdate保持一致）
    local levelMul = 1 + math.log(industry.level) * 1.5

    -- 主资源产出文本
    local output1 = math.floor(indType.baseOutput * levelMul * manageMul)
    local outputParts = {}
    if indType.resource ~= "none" then
        outputParts[#outputParts + 1] = (RES_NAMES[indType.resource] or indType.resource) .. "+" .. output1
    end
    -- 第二资源
    if indType.resource2 and indType.baseOutput2 then
        local o2 = math.floor(indType.baseOutput2 * levelMul * manageMul)
        outputParts[#outputParts + 1] = (RES_NAMES[indType.resource2] or indType.resource2) .. "+" .. o2
    end
    -- 第三资源
    if indType.resource3 and indType.baseOutput3 then
        local o3 = math.floor(indType.baseOutput3 * levelMul * manageMul)
        outputParts[#outputParts + 1] = (RES_NAMES[indType.resource3] or indType.resource3) .. "+" .. o3
    end
    local outputText = #outputParts > 0 and ("月产 " .. table.concat(outputParts, " / ")) or "防御设施"

    -- 特殊效果描述
    local EFFECT_DESCS = {
        weather_immune   = "免疫天灾",
        reduce_sick_15   = "全族生病-15%",
        consume_grain_3  = "月耗粮3",
        martial_grow_10  = "武艺成长+10%",
        military_boost_15 = "从军战力+15%",
        risk_loss_20     = "有折损风险",
        study_grow_10    = "读书成长+10%",
        risk_tax_30      = "有被查抄风险",
        interest_2pct    = "资产生息2%",
        season_amplify   = "受季风影响",
    }
    local effectText = indType.specialEffect and EFFECT_DESCS[indType.specialEffect] or nil

    -- 是否可进化
    local evo = GameData.INDUSTRY_EVOLUTION[industry.typeId]
    local canEvolve = false
    local evoReason = nil
    if evo then
        canEvolve, evoReason = GameData.CanEvolveIndustry(industry.id)
    end

    -- 图片路径
    local imgPath = INDUSTRY_IMAGES[industry.typeId]

    -- 右侧信息子元素列表
    local infoChildren = {
        -- 名称行：名称 + 特效标签
        UI.Panel {
            flexDirection = "row", alignItems = "center", gap = 4, flexShrink = 1,
            children = {
                UI.Label { text = indType.name .. "（Lv." .. industry.level .. "）", fontSize = 14, fontColor = indType.evolved and Theme.GOLD or Theme.TEXT_PRIMARY },
                effectText and UI.Label { text = effectText, fontSize = 9, fontColor = { 255, 170, 50, 200 },
                    paddingHorizontal = 4, paddingVertical = 1, borderRadius = 3,
                    backgroundColor = { 255, 170, 50, 25 } } or nil,
            },
        },
        -- 产出 + 维护费
        UI.Panel {
            flexDirection = "row", justifyContent = "space-between", alignItems = "center",
            children = {
                UI.Label { text = outputText, fontSize = 10, fontColor = Theme.GREEN },
                indType.resource ~= "none" and UI.Label {
                    text = "维护 银-" .. math.floor(indType.cost * 0.018 * industry.level),
                    fontSize = 9, fontColor = Theme.TEXT_MUTED,
                } or nil,
            },
        },
        -- 管理行
        UI.Panel {
            flexDirection = "row", justifyContent = "space-between", alignItems = "center",
            children = {
                UI.Panel {
                    flexDirection = "row", gap = 4, alignItems = "center", flexShrink = 1,
                    children = {
                        UI.Label { text = "管理：" .. assigneeName .. stateLabel, fontSize = 11, fontColor = isPartTime and Theme.GOLD_DARK or Theme.TEXT_SECONDARY },
                        (assignee and assignee.alive) and UI.Label {
                            text = "x" .. string.format("%.2f", manageMul),
                            fontSize = 9, fontColor = isPartTime and Theme.GOLD_DARK or Theme.GREEN,
                            paddingHorizontal = 3, paddingVertical = 1, borderRadius = 3,
                            backgroundColor = isPartTime and { 200, 160, 50, 20 } or { 56, 168, 120, 20 },
                        } or nil,
                    },
                },
                UI.Panel {
                    paddingHorizontal = 10, paddingVertical = 4, borderRadius = 4,
                    backgroundColor = Theme.BG_INPUT, borderWidth = 1, borderColor = Theme.BORDER,
                    onTap = function() AudioManager.Click() screen.ShowAssignMember(industry) end,
                    children = { UI.Label { text = "分配", fontSize = 11, fontColor = Theme.GOLD } },
                },
            },
        },
        -- 操作按钮行
        UI.Panel {
            flexDirection = "row", justifyContent = "flex-end", gap = 8,
            children = {
                -- 进化按钮
                evo and UI.Panel {
                    paddingHorizontal = 10, paddingVertical = 4, borderRadius = 4,
                    backgroundColor = canEvolve and { 180, 100, 30, 40 } or Theme.BG_INPUT,
                    borderWidth = 1, borderColor = canEvolve and Theme.GOLD or Theme.BORDER,
                    opacity = canEvolve and 1.0 or 0.5,
                    onTap = function()
                        if not canEvolve then
                            Toast.Show(evoReason or "条件不满足")
                            return
                        end
                        local evoType = GameData.GetIndustryType(evo.to)
                        local evoName = evoType and evoType.name or evo.to
                        screen.ShowConfirm("产业进化", indType.name .. " → " .. evoName .. "\n" .. evo.desc .. "\n\n消耗银两 " .. evo.cost .. "\n需等级Lv." .. evo.reqLevel .. "，品级【" .. GameData.GetUnlockRankName(evo.reqRank) .. "】\n\n确认进化？", "进化", function()
                            local ok, msg = GameData.EvolveIndustry(industry.id)
                            if ok then
                                AudioManager.Celebrate()
                                Toast.Success(msg)
                                screen.RefreshAll()
                            else
                                Toast.Show(msg or "进化失败")
                            end
                        end)
                    end,
                    children = {
                        UI.Label { text = "进化→" .. (GameData.GetIndustryType(evo.to) and GameData.GetIndustryType(evo.to).name or "?") .. "(" .. evo.cost .. "银)", fontSize = 10, fontColor = Theme.GOLD },
                    },
                } or nil,
                -- 升级按钮
                UI.Panel {
                    paddingHorizontal = 10, paddingVertical = 4, borderRadius = 4,
                    backgroundColor = Theme.BG_INPUT, borderWidth = 1, borderColor = Theme.BORDER,
                    opacity = GameData.CanAfford(indType.cost * industry.level, 0, 0, 0) and 1.0 or 0.5,
                    onTap = function()
                        local cost = indType.cost * industry.level
                        if GameData.SpendResources(cost, 0, 0, 0) then
                            AudioManager.Gain()
                            industry.level = industry.level + 1
                            GameData.AddLog(indType.name .. "升级至Lv." .. industry.level)
                            screen.RefreshAll()
                        end
                    end,
                    children = {
                        UI.Label { text = "升级(-" .. (indType.cost * industry.level) .. "银)", fontSize = 10, fontColor = Theme.GOLD },
                    },
                },
                -- 变卖按钮
                UI.Panel {
                    paddingHorizontal = 10, paddingVertical = 4, borderRadius = 4,
                    backgroundColor = { 120, 40, 40, 30 }, borderWidth = 1, borderColor = { 180, 80, 80, 100 },
                    onTap = function()
                        local refund = math.floor(indType.cost * 0.5)
                        screen.ShowConfirm("变卖产业", "确定要变卖「" .. indType.name .. "（Lv." .. industry.level .. "）」吗？\n\n可回收银两 " .. refund .. "（建造成本的50%）\n\n变卖后不可恢复！", "变卖", function()
                            local ok, msg = GameData.SellIndustry(industry.id)
                            if ok then
                                AudioManager.Click()
                                Toast.Success(msg)
                                screen.RefreshAll()
                            else
                                Toast.Show(msg or "变卖失败")
                            end
                        end)
                    end,
                    children = {
                        UI.Label { text = "变卖(+" .. math.floor(indType.cost * 0.5) .. "银)", fontSize = 10, fontColor = { 200, 80, 80, 255 } },
                    },
                },
            },
        },
    }

    -- 过滤 nil 子元素
    local filteredInfo = {}
    for _, c in ipairs(infoChildren) do
        if c then filteredInfo[#filteredInfo + 1] = c end
    end

    return UI.Panel {
        width = "100%", borderRadius = 8, overflow = "hidden",
        backgroundColor = indType.evolved and { 60, 45, 20, 255 } or Theme.BG_WHITE,
        borderWidth = 1, borderColor = indType.evolved and Theme.GOLD_DARK or Theme.BORDER,
        flexDirection = "row",
        children = {
            -- 左侧：1:1 正方形图片容器
            imgPath and UI.Panel {
                width = 90, height = 90,
                backgroundImage = imgPath,
                backgroundFit = "cover",
            } or UI.Panel {
                width = 90, height = 90,
                backgroundColor = { 80, 60, 40, 100 },
                justifyContent = "center", alignItems = "center",
                children = {
                    UI.Label { text = indType.name, fontSize = 14, fontColor = Theme.TEXT_MUTED },
                },
            },
            -- 右侧：信息列
            UI.Panel {
                flexGrow = 1, flexShrink = 1, padding = 8, gap = 4,
                justifyContent = "center",
                children = filteredInfo,
            },
        },
    }
end

-- ============================================================================
-- 产业管理页面
-- ============================================================================

function IndustryPage.Create(PageTitle, screen)
    local s = GameData.state

    local indCards = {}
    for _, ind in ipairs(s.industries) do
        indCards[#indCards + 1] = CreateIndustryCard(ind, PageTitle, screen)
    end

    local buyButtons = {}
    for _, indType in ipairs(GameData.INDUSTRY_TYPES) do
        if indType.id ~= "fort" and not indType.evolved then
            local unlocked = GameData.IsIndustryUnlocked(indType.id)
            local reqRank = GameData.INDUSTRY_UNLOCK[indType.id] or 1
            local buyImgPath = INDUSTRY_IMAGES[indType.id]
            if unlocked then
                buyButtons[#buyButtons + 1] = UI.Panel {
                    width = "100%", borderRadius = 8, overflow = "hidden",
                    backgroundColor = Theme.BG_INPUT, borderWidth = 1, borderColor = Theme.BORDER,
                    flexDirection = "row", alignItems = "center",
                    opacity = GameData.CanAfford(indType.cost, 0, 0, 0) and 1.0 or 0.5,
                    onTap = function()
                        -- A1: 产业数量上限检查
                        local limit = GameData.INDUSTRY_LIMIT_BY_RANK[GameData.state.clanRank] or 4
                        if #GameData.state.industries >= limit then
                            Toast.Warn("产业已达上限（" .. limit .. "个），需提升品级")
                            return
                        end
                        if not GameData.CanAfford(indType.cost, 0, 0, 0) then
                            Toast.NotEnough("银两")
                            return
                        end
                        screen.ShowConfirm("新建" .. indType.name, "消耗 银两" .. indType.cost .. "\n" .. indType.desc .. "\n\n确认建造？", "建造", function()
                            if GameData.SpendResources(indType.cost, 0, 0, 0) then
                                AudioManager.Gain()
                                GameData.AddIndustry(indType.id)
                                GameData.AddLog("新建" .. indType.name .. "一处。")
                                screen.RefreshAll()
                                Toast.Success("新建" .. indType.name .. "完成")
                            end
                        end)
                    end,
                    children = {
                        buyImgPath and UI.Panel {
                            width = 60, height = 60,
                            backgroundImage = buyImgPath,
                            backgroundFit = "cover",
                        } or UI.Panel {
                            width = 60, height = 60,
                            backgroundColor = { 80, 60, 40, 60 },
                            justifyContent = "center", alignItems = "center",
                            children = { UI.Label { text = indType.name, fontSize = 11, fontColor = Theme.TEXT_MUTED } },
                        },
                        UI.Panel {
                            flexGrow = 1, flexShrink = 1, padding = 8, gap = 2,
                            flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                            children = {
                                UI.Panel {
                                    gap = 2, flexShrink = 1,
                                    children = {
                                        UI.Label { text = "新建" .. indType.name, fontSize = 13, fontColor = Theme.TEXT_PRIMARY },
                                        UI.Label { text = indType.desc, fontSize = 9, fontColor = Theme.TEXT_MUTED },
                                    },
                                },
                                UI.Label { text = indType.cost .. "银", fontSize = 12, fontColor = Theme.GOLD },
                            },
                        },
                    },
                }
            else
                buyButtons[#buyButtons + 1] = UI.Panel {
                    width = "100%", borderRadius = 8, overflow = "hidden",
                    backgroundColor = Theme.BG_INPUT, borderWidth = 1, borderColor = Theme.BORDER,
                    flexDirection = "row", alignItems = "center",
                    opacity = 0.35,
                    onTap = function()
                        AudioManager.Click()
                        Toast.Locked(indType.name, GameData.GetUnlockRankName(reqRank))
                    end,
                    children = {
                        buyImgPath and UI.Panel {
                            width = 60, height = 60,
                            backgroundImage = buyImgPath,
                            backgroundFit = "cover",
                        } or UI.Panel {
                            width = 60, height = 60,
                            backgroundColor = { 80, 60, 40, 60 },
                            justifyContent = "center", alignItems = "center",
                            children = { UI.Label { text = indType.name, fontSize = 11, fontColor = Theme.TEXT_MUTED } },
                        },
                        UI.Panel {
                            flexGrow = 1, flexShrink = 1, padding = 8, gap = 2,
                            children = {
                                UI.Label { text = "[锁] " .. indType.name, fontSize = 13, fontColor = Theme.TEXT_MUTED },
                                UI.Label { text = "品级【" .. GameData.GetUnlockRankName(reqRank) .. "】解锁", fontSize = 9, fontColor = Theme.TEXT_MUTED },
                            },
                        },
                    },
                }
            end
        end
    end

    return UI.ScrollView {
        width = "100%", flexGrow = 1, flexBasis = 0,
        backgroundColor = { 0, 0, 0, 0 },
        children = {
            UI.Panel {
                width = "100%", gap = 10, padding = 12, paddingBottom = 20,
                children = {
                    PageTitle("田庄产业", "管理家族产业"),
                    UI.Panel {
                        flexDirection = "row", justifyContent = "space-between", alignItems = "center",
                        children = {
                            UI.Label { text = "现有产业", fontSize = 15, fontColor = Theme.GOLD },
                            UI.Label {
                                text = #s.industries .. "/" .. (GameData.INDUSTRY_LIMIT_BY_RANK[s.clanRank] or 4),
                                fontSize = 12, fontColor = #s.industries >= (GameData.INDUSTRY_LIMIT_BY_RANK[s.clanRank] or 4) and Theme.RED or Theme.TEXT_SECONDARY,
                            },
                        },
                    },
                    table.unpack(indCards),
                },
            },
            UI.Panel {
                width = "100%", gap = 8, padding = 12, paddingBottom = 20,
                children = {
                    UI.Label { text = "购置产业", fontSize = 15, fontColor = Theme.GOLD },
                    table.unpack(buyButtons),
                },
            },
        },
    }
end

return IndustryPage
