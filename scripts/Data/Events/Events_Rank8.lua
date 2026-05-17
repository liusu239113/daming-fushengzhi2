-- ============================================================================
-- 大明浮生志2 - 豪阀品级（8级）专属事件池
-- 设计理念：豪阀势大，招致朝廷猜忌、外敌虎视
-- ============================================================================

local GameData = require("Data.GameData")
local EventSystem = require("Systems.EventSystem")
local events = {}

-- 1. 朝廷猜忌 — 功高震主
events[#events + 1] = {
    id = "r8_imperial_suspicion",
    title = "朝廷猜忌",
    rankRange = {8, 9},
    weight = 10,
    cooldownMonths = 8,
    isDisaster = true,
    check = function(s) return s.fame > 500 end,
    execute = function(s, report)
        local fameLoss = math.ceil(s.fame * 0.1)
        local silverLoss = math.ceil(s.silver * 0.08)
        report.events[#report.events + 1] = "朝廷对家族势力心生猜忌！"
        GameData.AddLog("功高震主，圣上对家族猜忌日深。")
        return {
            title = "朝廷猜忌",
            desc = "家族势力膨胀，引起朝中御史弹劾，言官奏章如雪片飞来，参你族'结党营私，恃势凌人'。\n圣上龙颜不悦，下旨查办。",
            choices = {
                { text = "主动献银表忠心（-" .. silverLoss .. "银两）", cost = {silver = silverLoss}, effect = function()
                    GameData.AddResource("fame", -math.ceil(fameLoss * 0.3))
                    GameData.AddLog("进献银两以释圣疑，暂保平安。")
                end },
                { text = "上书自辩清白", effect = function()
                    if math.random() < 0.5 then
                        GameData.AddResource("fame", 10)
                        GameData.AddLog("辩章入微，圣上释疑，反赐嘉奖。")
                    else
                        GameData.AddResource("fame", -fameLoss)
                        GameData.AddResource("silver", -math.ceil(silverLoss * 0.5))
                        GameData.AddLog("自辩无效，被削去部分产业。")
                    end
                end },
                { text = "低调蛰伏减少活动", effect = function()
                    GameData.AddResource("fame", -fameLoss)
                    GameData.AddLog("暂时收敛锋芒，等待风头过去。")
                end },
            }
        }
    end,
}

-- 2. 天灾人祸 — 复合灾害
events[#events + 1] = {
    id = "r8_compound_disaster",
    title = "天灾人祸",
    rankRange = {8, 9},
    weight = 8,
    cooldownMonths = 10,
    isDisaster = true,
    check = function(s) return s.grain > 200 and s.silver > 100 end,
    execute = function(s, report)
        local scale = EventSystem.GetDisasterScale(s.clanRank)
        local grainLoss = math.ceil(s.grain * math.min(0.35, 0.15 * scale))
        local silverLoss = math.ceil(s.silver * math.min(0.2, 0.08 * scale))
        local clothLoss = math.ceil(s.cloth * math.min(0.25, 0.1 * scale))

        report.events[#report.events + 1] = "地震引发大火，仓库受损严重！"
        GameData.AddLog("地动山摇，引发大火，仓库付之一炬。")
        return {
            title = "天灾人祸",
            desc = "夜半地动山摇，屋宇倾塌，继而引发大火。\n仓库、田庄、作坊均遭重创。\n预计损失：粮食" .. grainLoss .. " 银两" .. silverLoss .. " 布匹" .. clothLoss,
            choices = {
                { text = "全力救灾重建（-" .. math.ceil(50 * scale) .. "银两减半损失）", cost = {silver = math.ceil(50 * scale)}, effect = function()
                    GameData.AddResource("grain", -math.ceil(grainLoss * 0.5))
                    GameData.AddResource("silver", -math.ceil(silverLoss * 0.5))
                    GameData.AddResource("cloth", -math.ceil(clothLoss * 0.5))
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("倾力救灾，虽损失惨重但保住了根基。")
                end },
                { text = "优先保护核心产业", effect = function()
                    GameData.AddResource("grain", -grainLoss)
                    GameData.AddResource("silver", -math.ceil(silverLoss * 0.3))
                    GameData.AddResource("cloth", -clothLoss)
                    GameData.AddLog("集中力量保住了银库，但粮仓和织坊尽毁。")
                end },
                { text = "向朝廷请赈（-20声望）", effect = function()
                    GameData.AddResource("grain", -math.ceil(grainLoss * 0.6))
                    GameData.AddResource("silver", -math.ceil(silverLoss * 0.6))
                    GameData.AddResource("cloth", -math.ceil(clothLoss * 0.6))
                    GameData.AddResource("fame", -20)
                    GameData.AddLog("上书请赈，朝廷拨付部分钱粮，然名望受损。")
                end },
            }
        }
    end,
}

-- 3. 敌族暗算
events[#events + 1] = {
    id = "r8_rival_sabotage",
    title = "敌族暗算",
    rankRange = {8, 9},
    weight = 9,
    cooldownMonths = 6,
    isDisaster = true,
    check = function(s) return #s.industries >= 10 end,
    execute = function(s, report)
        -- 随机损坏1-2个产业
        local damaged = {}
        local industries = {}
        for _, ind in ipairs(s.industries) do
            local indType = GameData.GetIndustryType(ind.typeId)
            if indType and indType.resource ~= "none" then
                industries[#industries + 1] = ind
            end
        end
        local dmgCount = math.min(#industries, math.random(1, 2))
        -- 随机打乱顺序取前dmgCount个
        for i = 1, dmgCount do
            local j = math.random(i, #industries)
            industries[i], industries[j] = industries[j], industries[i]
            damaged[#damaged + 1] = industries[i]
        end

        local damageDesc = {}
        for _, ind in ipairs(damaged) do
            local indType = GameData.GetIndustryType(ind.typeId)
            damageDesc[#damageDesc + 1] = indType.name .. "(Lv." .. ind.level .. ")"
        end

        report.events[#report.events + 1] = "敌族暗中破坏产业！"
        GameData.AddLog("有人暗中破坏家族产业，查明是敌族所为。")
        return {
            title = "敌族暗算",
            desc = "深夜有人潜入破坏，查明是敌对家族派人所为。\n受损产业：" .. table.concat(damageDesc, "、") .. "\n产业等级将被降低！",
            choices = {
                { text = "加强巡逻追查凶手（-30银两）", cost = {silver = 30}, effect = function()
                    -- 50%概率挽回
                    if math.random() < 0.5 then
                        GameData.AddResource("fame", 10)
                        GameData.AddLog("擒获暗探，敌族阴谋败露，产业未受大损。")
                    else
                        for _, ind in ipairs(damaged) do
                            ind.level = math.max(1, ind.level - 1)
                        end
                        GameData.AddResource("fame", 5)
                        GameData.AddLog("虽擒获部分暗探，但产业已遭破坏。")
                    end
                end },
                { text = "忍气吞声修复", effect = function()
                    for _, ind in ipairs(damaged) do
                        ind.level = math.max(1, ind.level - 1)
                    end
                    GameData.AddLog("暗中修复受损产业，但声望无损。")
                end },
                { text = "以牙还牙报复（-50银两）", cost = {silver = 50}, effect = function()
                    for _, ind in ipairs(damaged) do
                        ind.level = math.max(1, ind.level - 1)
                    end
                    GameData.AddResource("fame", 20)
                    GameData.AddResource("silver", math.random(20, 40))
                    GameData.AddLog("派人反击敌族，夺回部分银两，扬我家威。")
                end },
            }
        }
    end,
}

-- 4. 名士来投
events[#events + 1] = {
    id = "r8_scholar_joins",
    title = "名士来投",
    rankRange = {8, 9},
    weight = 7,
    cooldownMonths = 12,
    isDisaster = false,
    check = function(s) return s.fame > 300 end,
    execute = function(s, report)
        report.events[#report.events + 1] = "名士慕名来投，愿为家族效力。"
        GameData.AddLog("有名士闻家族大名，前来投奔。")
        return {
            title = "名士来投",
            desc = "一位饱学名士闻你家族声名远播，特来投奔，愿以一身才学辅佐家族。\n此人精通经史，可大幅提升族中文教。",
            choices = {
                { text = "厚礼相待纳入族中（-40银两）", cost = {silver = 40}, effect = function()
                    -- 提升所有读书族人学识
                    for _, m in ipairs(GameData.GetAliveMembers()) do
                        if m.state == "读书" then
                            m.study = math.min(100, m.study + math.random(5, 10))
                        end
                    end
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("名士入族，教导后辈，学风大振。")
                end },
                { text = "礼貌婉拒", effect = function()
                    GameData.AddLog("婉拒名士好意，各自珍重。")
                end },
            }
        }
    end,
}

return events
