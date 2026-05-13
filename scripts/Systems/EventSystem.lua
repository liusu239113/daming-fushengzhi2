-- ============================================================================
-- 大明浮生志2 - 随机事件系统
-- 管理所有随机事件的定义与触发逻辑
-- ============================================================================

local GameData = require("Data.GameData")
local EraSystem = require("Data.EraSystem")
local EventRegistry = require("Systems.EventRegistry")
local RivalClans = require("Data.RivalClans")
local GrowthSystem = require("Systems.GrowthSystem")

local EventSystem = {}

-- ============================================================================
-- 事件战斗辅助函数（调用3D战斗系统）
-- ============================================================================

--- 随机选择N个成年族人作为出战将领
---@param count number 需要的族人数量
---@return table memberIds 出战族人ID列表
---@return table memberNames 族人名字列表
local function PickRandomFighters(count)
    local candidates = {}
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.gender == "male" and m.age >= 16 and m.age <= 55
           and m.health > 20 and m.state ~= "生病" and m.state ~= "从军" then
            candidates[#candidates + 1] = m
        end
    end
    -- 按武艺排序（优先选武艺高的）
    table.sort(candidates, function(a, b) return a.martial > b.martial end)

    local ids = {}
    local names = {}
    for i = 1, math.min(count, #candidates) do
        ids[#ids + 1] = candidates[i].id
        names[#names + 1] = candidates[i].name
    end
    return ids, names
end

--- 根据品级生成事件战斗的敌族数据（兼容 BattleScene 的 rival 结构）
---@param enemyName string 敌军名称
---@param rank number 当前品级
---@param year number 当前年份
---@return table rival 兼容 BattleScene 的敌族数据
local function GenerateEventRival(enemyName, rank, year, unitStyle)
    -- 根据品级选择难度等级
    local tierIndex = 1  -- easy
    if rank >= 5 then tierIndex = 3       -- hard
    elseif rank >= 3 then tierIndex = 2   -- normal
    end
    -- 乱世加一级
    if year >= 1630 and tierIndex < 4 then tierIndex = tierIndex + 1 end

    local tier = RivalClans.DIFFICULTY_TIERS[tierIndex]

    -- 生成敌方成员
    local memberCount = math.random(tier.memberRange[1], tier.memberRange[2])
    local surnames = {"马", "孙", "韩", "徐", "曹", "魏", "吕", "沈", "冯", "郑"}
    local surname = surnames[math.random(1, #surnames)]
    local members = {}
    local givenNames = {"虎", "豹", "彪", "龙", "猛", "刚", "坚", "毅", "烈", "威",
                        "勇", "强", "铁", "钢", "石", "山", "峰", "岩", "崖", "雷"}
    for j = 1, memberCount do
        local isLeader = (j == 1)
        local martial = math.random(tier.martialRange[1], tier.martialRange[2])
        local health = math.random(tier.healthRange[1], tier.healthRange[2])
        if isLeader then
            martial = math.min(100, martial + 15)
            health = math.min(100, health + 10)
        end
        members[j] = {
            name = surname .. givenNames[math.random(1, #givenNames)],
            gender = "male",
            age = isLeader and math.random(30, 50) or math.random(18, 45),
            title = isLeader and "头领" or nil,
            martial = martial,
            health = health,
            isLeader = isLeader,
            memberIndex = j,
        }
    end

    -- 兵力
    local soldiers = math.random(tier.soldierRange[1], tier.soldierRange[2])
    soldiers = math.floor(soldiers / 100) * 100
    if soldiers < 100 then soldiers = 100 end

    -- 胜利奖励（事件战斗：击退匪帮可获少量缴获）
    local rewardMul = tier.rewardMul * 0.6  -- 事件战斗奖励比讨伐低
    local rewards = {
        silver = math.floor(math.random(10, 25) * rewardMul),
        grain  = math.floor(math.random(8, 18) * rewardMul),
        fame   = math.floor(math.random(5, 15) * rewardMul),
    }

    return {
        id = 0,
        name = enemyName,
        surname = surname,
        tierId = tier.id,
        tierName = tier.name,
        tierColor = tier.color,
        tierDesc = "事件战斗",
        members = members,
        soldiers = soldiers,
        rewards = rewards,
        unitStyle = unitStyle or nil,  -- "bandit" 时敌方使用山匪模型
    }
end

--- 创建事件战斗的自定义结算函数
---@param defeatPenalty table { silver, grain, fame } 战败额外惩罚
---@param victoryBonus table|nil { silver, grain, fame } 胜利额外奖励
---@return function onSettle 结算回调
local function CreateEventSettlement(defeatPenalty, victoryBonus)
    return function(result, rivalData, deployedIds)
        local s = GameData.state
        local report = {
            result = result,
            rivalName = rivalData.name,
            rewards = { silver = 0, grain = 0, fame = 0 },
            casualties = {},
        }

        if result == "victory" then
            -- 基础奖励
            report.rewards.silver = rivalData.rewards.silver
            report.rewards.grain = rivalData.rewards.grain
            report.rewards.fame = rivalData.rewards.fame
            GameData.AddResource("silver", report.rewards.silver)
            GameData.AddResource("grain", report.rewards.grain)
            GameData.AddResource("fame", report.rewards.fame)
            -- 额外胜利奖励
            if victoryBonus then
                report.rewards.silver = report.rewards.silver + (victoryBonus.silver or 0)
                report.rewards.grain = report.rewards.grain + (victoryBonus.grain or 0)
                report.rewards.fame = report.rewards.fame + (victoryBonus.fame or 0)
                GameData.AddResource("silver", victoryBonus.silver or 0)
                GameData.AddResource("grain", victoryBonus.grain or 0)
                GameData.AddResource("fame", victoryBonus.fame or 0)
            end
            GameData.AddLog("击退" .. rivalData.name .. "！缴获银两" ..
                report.rewards.silver .. "、粮食" .. report.rewards.grain)
        else
            -- 战败：扣除资源（银、粮、布、声望）
            local sLoss = defeatPenalty.silver or 0
            local gLoss = defeatPenalty.grain or 0
            local cLoss = defeatPenalty.cloth or 0
            local fLoss = defeatPenalty.fame or 0
            report.rewards.silver = -sLoss
            report.rewards.grain = -gLoss
            report.rewards.fame = -fLoss
            GameData.AddResource("silver", -sLoss)
            GameData.AddResource("grain", -gLoss)
            GameData.AddResource("cloth", -cLoss)
            GameData.AddResource("fame", -fLoss)
            GameData.AddLog("抵抗" .. rivalData.name .. "失败！损失银两" ..
                sLoss .. "、粮食" .. gLoss .. "、布匹" .. cLoss .. "、声望" .. fLoss)
        end

        -- 出战族人伤亡
        local deadNames = {}
        local injuredNames = {}
        for _, memberId in ipairs(deployedIds) do
            local member = GameData.GetMember(memberId)
            if member then
                if result == "defeat" then
                    -- 战败：8%概率阵亡
                    local deathRoll = math.random(100)
                    if deathRoll <= 8 then
                        member.state = "已故"
                        member.deathAge = member.age
                        member.deathYear = s.year
                        member.deathCause = "战死"
                        deadNames[#deadNames + 1] = member.name
                        report.casualties[#report.casualties + 1] = {
                            memberId = memberId, name = member.name,
                            injury = "阵亡",
                        }
                        goto continueLoop
                    end
                end
                -- 受伤判定
                local injuryRoll = math.random(100)
                local injuryThreshold = (result == "victory") and 20 or 60
                if injuryRoll < injuryThreshold then
                    local healthLoss = math.random(5, 20)
                    if result == "defeat" then healthLoss = healthLoss + 10 end
                    member.health = math.max(1, member.health - healthLoss)
                    if member.health <= 10 then
                        if member.state ~= "生病" then member.prevState = member.state end
                        member.state = "生病"
                        injuredNames[#injuredNames + 1] = member.name .. "（重伤）"
                        report.casualties[#report.casualties + 1] = {
                            memberId = memberId, name = member.name,
                            injury = "重伤（健康-" .. healthLoss .. "）",
                        }
                    else
                        injuredNames[#injuredNames + 1] = member.name .. "（轻伤）"
                        report.casualties[#report.casualties + 1] = {
                            memberId = memberId, name = member.name,
                            injury = "轻伤（健康-" .. healthLoss .. "）",
                        }
                    end
                end
                ::continueLoop::
            end
        end

        -- 战败后弹出事件通知
        if result == "defeat" then
            local sLoss = defeatPenalty.silver or 0
            local gLoss = defeatPenalty.grain or 0
            local cLoss = defeatPenalty.cloth or 0
            local fLoss = defeatPenalty.fame or 0
            local lossLines = "损失：银两" .. sLoss .. "、粮食" .. gLoss
            if cLoss > 0 then lossLines = lossLines .. "、布匹" .. cLoss end
            lossLines = lossLines .. "、声望" .. fLoss

            local casualtyLines = ""
            if #deadNames > 0 then
                casualtyLines = casualtyLines .. "\n\n战死：" .. table.concat(deadNames, "、")
            end
            if #injuredNames > 0 then
                casualtyLines = casualtyLines .. "\n受伤：" .. table.concat(injuredNames, "、")
            end
            if #deadNames == 0 and #injuredNames == 0 then
                casualtyLines = "\n\n所幸族人虽败犹存，无人阵亡。"
            end

            s.pendingEvents[#s.pendingEvents + 1] = {
                title = "兵败" .. rivalData.name,
                desc = "抵抗" .. rivalData.name .. "失败！敌人冲入村寨大肆劫掠。\n\n" ..
                    lossLines .. casualtyLines,
                choices = { { text = "痛定思痛", effect = function() end } },
            }
        end

        return report
    end
end

-- ============================================================================
-- 随机事件表（36 个 + 连锁事件机制）
-- ============================================================================

EventSystem.RANDOM_EVENTS = {
    -- ===== 家族内部事件 =====
    {
        title = "族人染疫",
        weight = 12,
        check = function(s) return #GameData.GetAliveMembers() > 2 end,
        execute = function(s, report)
            local members = GameData.GetAliveMembers()
            local victim = members[math.random(1, #members)]
            victim.health = math.max(10, victim.health - math.random(15, 30))
            if victim.state ~= "生病" then victim.prevState = victim.state end
            victim.state = "生病"
            report.events[#report.events + 1] = victim.name .. "不幸染疫，卧床不起。"
            GameData.AddLog(victim.name .. "染疫，需要医治。")
            return { title = "族人染疫", desc = victim.name .. "不幸染疫，卧床不起。需花费银两5医治。",
                     choices = {
                         { text = "花银两医治（-5银两）", cost = {silver = 5}, effect = function() victim.health = math.min(100, victim.health + 20) end },
                         { text = "静养恢复", effect = function() end },
                     } }
        end,
    },
    {
        title = "分家争产",
        weight = 6,
        check = function(s) return #GameData.GetAliveMembers() > 6 end,
        execute = function(s, report)
            local silverLoss = math.random(5, 15)
            report.events[#report.events + 1] = "族中长房次房争产，闹得不可开交。"
            GameData.AddLog("族中发生分家争产纠纷。")
            return { title = "分家争产", desc = "族中长房与次房因田产分配争执不休，声望受损。",
                     choices = {
                         { text = "公正裁断（-声望5）", effect = function() GameData.AddResource("fame", -5) end },
                         { text = "花银两安抚（-" .. silverLoss .. "银两）", cost = {silver = silverLoss}, effect = function() end },
                         { text = "任其争斗（-声望10）", effect = function() GameData.AddResource("fame", -10) end },
                     } }
        end,
    },
    {
        title = "族人出走",
        weight = 4,
        check = function(s) return #GameData.GetAliveMembers() > 4 end,
        execute = function(s, report)
            local candidates = {}
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.age >= 16 and m.age <= 30 and not m.spouseId then
                    candidates[#candidates + 1] = m
                end
            end
            if #candidates == 0 then return nil end
            local runaway = candidates[math.random(1, #candidates)]
            report.events[#report.events + 1] = runaway.name .. "与外人私奔离家。"
            return { title = "族人出走", desc = runaway.name .. "（" .. runaway.age .. "岁）与外人相恋，欲私奔离家！",
                     choices = {
                         { text = "放其自由", effect = function() GameData.CheckPatriarchDeath(runaway); runaway.alive = false; runaway.state = "离族" end },
                         { text = "强行挽留（-声望3）", cost = {fame = 3}, effect = function() end },
                     } }
        end,
    },
    {
        title = "族人犯错",
        weight = 5,
        check = function(s) return #GameData.GetAdultMembers() > 3 end,
        execute = function(s, report)
            local adults = GameData.GetAdultMembers()
            local trouble = adults[math.random(1, #adults)]
            report.events[#report.events + 1] = trouble.name .. "在镇上与人斗殴，被官府拘押。"
            GameData.AddLog(trouble.name .. "惹是生非，被官府拘押。")
            return { title = "族人犯错", desc = trouble.name .. "在镇上酒后与人斗殴，被衙役拘押！",
                     choices = {
                         { text = "花银两打点（-15银两）", cost = {silver = 15}, effect = function() trouble.state = "在家" end },
                         { text = "任其受罚", effect = function() trouble.health = math.max(20, trouble.health - 20); GameData.AddResource("fame", -3) end },
                     } }
        end,
    },
    {
        title = "喜添人丁提示",
        weight = 3,
        check = function(s)
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.gender == "female" and m.age >= 18 and m.age <= 35 and m.spouseId and GameData.GetMember(m.spouseId) ~= nil then
                    return true
                end
            end
            return false
        end,
        execute = function(s, report)
            report.incomes.fame = report.incomes.fame + 2
            report.events[#report.events + 1] = "族中人丁兴旺，声望略增。"
            return nil
        end,
    },

    -- ===== 新增家族内部事件 =====
    {
        title = "族中争位",
        weight = 4,
        check = function(s) return #GameData.GetAdultMembers() > 8 end,
        execute = function(s, report)
            report.events[#report.events + 1] = "族中长辈对族长之位有所觊觎。"
            GameData.AddLog("宗族内部出现争权苗头。")
            return { title = "族中争位", desc = "随着宗族壮大，族中几房长辈对大小事务多有不满，暗中谋划夺权。",
                     choices = {
                         { text = "族会公议（-声望8，稳定）", cost = {fame = 8}, effect = function() end },
                         { text = "施恩拉拢（-20银两）", cost = {silver = 20}, effect = function() GameData.AddResource("fame", 3) end },
                         { text = "严厉弹压（-声望15）", cost = {fame = 15}, effect = function()
                             if math.random() < 0.3 then
                                 s.pendingChainEvent = "族人离心"
                             end
                         end },
                     } }
        end,
    },
    {
        title = "族人离心",
        weight = 0,
        isChainEvent = true,
        check = function(s) return false end,
        execute = function(s, report)
            local adults = GameData.GetAdultMembers()
            local leavers = 0
            for _, m in ipairs(adults) do
                if math.random() < 0.15 and m.state == "在家" then
                    GameData.CheckPatriarchDeath(m)
                    m.alive = false; m.state = "离族"; leavers = leavers + 1
                end
            end
            if leavers == 0 then leavers = 1 end
            report.events[#report.events + 1] = leavers .. "名族人心灰意冷，离族而去。"
            GameData.AddLog("严厉弹压之下，" .. leavers .. "名族人离族出走。")
            return { title = "族人离心", desc = "你的铁腕手段引发不满，" .. leavers .. "名族人带着家当离族而去。",
                     choices = { { text = "无可奈何", effect = function() end } } }
        end,
    },
    {
        title = "孝子感天",
        weight = 4,
        check = function(s)
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.age >= 50 and m.state == "生病" then return true end
            end
            return false
        end,
        execute = function(s, report)
            local elder = nil
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.age >= 50 and m.state == "生病" then elder = m; break end
            end
            if not elder then return nil end
            local child = nil
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.parentId == elder.id and m.alive then child = m; break end
            end
            local childName = child and child.name or "族中后辈"
            report.events[#report.events + 1] = childName .. "侍奉病榻，孝行感动乡里。"
            return { title = "孝子感天", desc = elder.name .. "卧病在床，" .. childName .. "衣不解带侍奉汤药，孝行传遍乡里。",
                     choices = {
                         { text = "嘉奖孝行（+8声望）", effect = function() GameData.AddResource("fame", 8); elder.health = math.min(100, elder.health + 15) end },
                     } }
        end,
    },
    {
        title = "族人悟道",
        weight = 3,
        check = function(s)
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.state == "读书" and m.study >= 50 then return true end
            end
            return false
        end,
        execute = function(s, report)
            local candidates = {}
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.state == "读书" and m.study >= 50 then candidates[#candidates + 1] = m end
            end
            if #candidates == 0 then return nil end
            local lucky = candidates[math.random(1, #candidates)]
            local rawGain = math.random(4, 10)  -- 原 12~25
            local gain = GrowthSystem.DiminishedGain(lucky.study, rawGain)
            report.events[#report.events + 1] = lucky.name .. "苦读多年，忽然悟道，学识精进！"
            GameData.AddLog(lucky.name .. "读书悟道，学识大进。")
            return { title = "族人悟道", desc = lucky.name .. "苦读经年，某日于书房中豁然贯通，犹如醍醐灌顶！\n学识+" .. gain,
                     choices = {
                         { text = "好事！", effect = function() lucky.study = math.min(100, lucky.study + gain) end },
                     } }
        end,
    },
    {
        title = "武艺切磋",
        weight = 3,
        check = function(s)
            local fighters = 0
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.martial >= 30 then fighters = fighters + 1 end
            end
            return fighters >= 2
        end,
        execute = function(s, report)
            local fighters = {}
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.martial >= 30 then fighters[#fighters + 1] = m end
            end
            local a = fighters[math.random(1, #fighters)]
            local b = fighters[math.random(1, #fighters)]
            while b.id == a.id and #fighters > 1 do b = fighters[math.random(1, #fighters)] end
            report.events[#report.events + 1] = a.name .. "与" .. b.name .. "在庭院切磋武艺。"
            return { title = "武艺切磋", desc = a.name .. "与" .. b.name .. "在庭院中比试武艺，你来我往好不热闹！",
                     choices = {
                         { text = "鼓励切磋（双方武艺+）", effect = function()
                             local gA = GrowthSystem.DiminishedGain(a.martial, math.random(2, 3))
                             local gB = GrowthSystem.DiminishedGain(b.martial, math.random(2, 3))
                             a.martial = math.min(100, a.martial + gA); b.martial = math.min(100, b.martial + gB)
                         end },
                         { text = "制止（怕受伤）", effect = function() end },
                     } }
        end,
    },
    {
        title = "喜结连理",
        weight = 8,
        check = function(s)
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.age >= 16 and m.age <= 40 and not m.spouseId then return true end
            end
            return false
        end,
        execute = function(s, report)
            local singles = {}
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.age >= 16 and m.age <= 40 and not m.spouseId then singles[#singles + 1] = m end
            end
            if #singles == 0 then return nil end
            local person = singles[math.random(1, #singles)]
            report.events[#report.events + 1] = "有人前来为" .. person.name .. "说媒。"
            return { title = "喜结连理", desc = "邻村媒人登门，为" .. person.name .. "（" .. person.age .. "岁）说亲。是否前去相看？",
                     choices = {
                         { text = "前去相看", effect = function()
                             if person.spouseId then return end
                             -- 打开正常的配偶选择流程（选门第→看属性→看广告换人）
                             local GS = require("UI.GameScreen")
                             GS.ShowMarriageTierSelect(person)
                         end },
                         { text = "婉拒", effect = function() end },
                     } }
        end,
    },
    {
        title = "宗祠修缮",
        weight = 3,
        check = function(s) return s.fame >= 20 and s.silver >= 30 end,
        execute = function(s, report)
            report.events[#report.events + 1] = "宗祠年久失修，族人提议翻修。"
            return { title = "宗祠修缮", desc = "族中宗祠已有些年头，墙壁斑驳，族老提议出资修缮，以彰显家族气派。",
                     choices = {
                         { text = "大修（-40银两，+15声望）", cost = {silver = 40}, effect = function()
                             GameData.AddResource("fame", 15)
                             GameData.AddLog("宗祠大修一新，族望大增。")
                         end },
                         { text = "小修（-15银两，+5声望）", cost = {silver = 15}, effect = function()
                             GameData.AddResource("fame", 5)
                         end },
                         { text = "暂且搁置", effect = function() GameData.AddResource("fame", -2) end },
                     } }
        end,
    },

    -- ===== 明末大势事件 =====
    {
        title = "喜得良田",
        weight = 2,
        check = function(s)
            -- 声望门槛提高，且产业数量不能太多（避免田地过剩）
            if s.fame < 30 then return false end
            local indCount = #(s.industries or {})
            local memberCount = #GameData.GetAliveMembers()
            return indCount < memberCount  -- 产业数不超过族人数才触发
        end,
        execute = function(s, report)
            report.events[#report.events + 1] = "有人投靠，献田一亩。"
            GameData.AddLog("乡人仰慕宗族声望，献田一亩。")
            return { title = "喜得良田", desc = "有乡人仰慕你家声望，愿献旱田一亩投靠。",
                     choices = {
                         { text = "接纳（+旱田）", effect = function() GameData.AddIndustry("dry_field") end },
                         { text = "婉拒（+3声望）", effect = function() GameData.AddResource("fame", 3) end },
                     } }
        end,
    },
    {
        title = "经商暴富",
        weight = 5,
        check = function(s)
            return #GameData.GetMerchantMembers() > 0
        end,
        execute = function(s, report)
            local bonus = math.random(20, 50)
            report.events[#report.events + 1] = "经商族人做成大买卖，额外获得银两" .. bonus .. "。"
            GameData.AddLog("经商族人做成大买卖，获利颇丰。")
            return { title = "经商暴富", desc = "族中经商之人做成一笔大买卖，额外获利银两" .. bonus .. "！",
                     choices = {
                         { text = "好事！（+" .. bonus .. "银两）", effect = function() GameData.AddResource("silver", bonus) end },
                     } }
        end,
    },
    {
        title = "经商亏损",
        weight = 5,
        check = function(s)
            return #GameData.GetMerchantMembers() > 0 and EraSystem.CanTriggerEvent(s.year, "trade_crisis")
        end,
        execute = function(s, report)
            local loss = math.random(10, 30)
            report.events[#report.events + 1] = "经商遭遇变故，亏损银两" .. loss .. "。"
            GameData.AddLog("时局动荡，经商族人遭遇亏损。")
            return { title = "经商亏损", desc = "时局动荡，商路不畅，族中商人遭遇亏损。",
                     choices = {
                         { text = "忍痛承受（-" .. loss .. "银两）", effect = function() GameData.AddResource("silver", -loss) end },
                         { text = "变卖货物止损（-" .. math.ceil(loss * 0.5) .. "银两）", effect = function() GameData.AddResource("silver", -math.ceil(loss * 0.5)); GameData.AddResource("cloth", -5) end },
                     } }
        end,
    },
    {
        title = "流寇过境",
        weight = 12,
        check = function(s) return EraSystem.CanTriggerEvent(s.year, "bandit_raid") end,
        execute = function(s, report)
            local region = GameData.GetRegion()
            local lossRate = region.banditRate
            -- 缴纳保护费金额
            local payoff = math.ceil(s.silver * lossRate * 0.3) + math.random(10, 25)
            local grainLoss = math.ceil(s.grain * lossRate * 0.2) + math.random(5, 12)

            -- 选2个成年族人（用于显示信息）
            local fighterIds, fighterNames = PickRandomFighters(2)
            local fighterInfo = #fighterIds > 0
                and ("可派出" .. table.concat(fighterNames, "、") .. "迎战")
                or "族中无人可战！"

            local enemyNames = {"流寇", "山匪", "劫道悍匪", "散兵游勇"}
            local enemyLabel = enemyNames[math.random(1, #enemyNames)]

            report.events[#report.events + 1] = enemyLabel .. "来袭！"
            GameData.AddLog(enemyLabel .. "逼近村寨。")

            return { title = "流寇过境",
                     desc = "一伙" .. enemyLabel .. "逼近村寨，来势汹汹！\n\n" .. fighterInfo,
                     choices = {
                         { text = "破财消灾（-银两" .. payoff .. "，-粮" .. grainLoss .. "）", cost = {silver = payoff, grain = grainLoss}, effect = function()
                             GameData.AddLog("缴纳保护费，" .. enemyLabel .. "离去。损失银两" .. payoff .. "、粮食" .. grainLoss .. "。")
                         end },
                         { text = "拒绝！奋起抵抗！", effect = function()
                             if #fighterIds == 0 then
                                 -- 无人可战，被迫交钱且加重损失
                                 local extraLoss = math.ceil(payoff * 0.5)
                                 GameData.AddResource("silver", -(payoff + extraLoss))
                                 GameData.AddResource("grain", -grainLoss)
                                 GameData.AddResource("fame", -5)
                                 GameData.AddLog("族中无人能战，被" .. enemyLabel .. "肆意劫掠！")
                                 s.pendingEvents[#s.pendingEvents + 1] = {
                                     title = "无力抵抗",
                                     desc = "族中无人能战，" .. enemyLabel .. "冲入村寨肆意劫掠！\n\n损失：银两" ..
                                         (payoff + extraLoss) .. "、粮食" .. grainLoss .. "、声望-5",
                                     choices = { { text = "痛定思痛", effect = function() end } }
                                 }
                                 return
                             end
                             -- 生成敌族数据，调用3D战斗
                             local rival = GenerateEventRival(enemyLabel, s.clanRank, s.year, "bandit")
                             local defeatPenalty = {
                                 silver = payoff * 2,
                                 grain = grainLoss * 2,
                                 cloth = math.ceil(payoff * 0.3),
                                 fame = 5 + s.clanRank,
                             }
                             local victoryBonus = {
                                 fame = 3 + s.clanRank * 2,
                             }
                             local GS = require("UI.GameScreen")
                             GS.EnterBattle(rival, fighterIds, {
                                 soldierCount = 0,  -- 事件战斗：纯将领对决，不带士兵
                                 onSettle = CreateEventSettlement(defeatPenalty, victoryBonus),
                             })
                         end },
                     } }
        end,
    },
    {
        title = "旱灾降临",
        weight = 10,
        check = function(s) return EraSystem.CanTriggerEvent(s.year, "drought") end,
        execute = function(s, report)
            local grainLoss = math.ceil(s.grain * 0.2)
            report.events[#report.events + 1] = "旱灾降临，庄稼欠收！粮食-" .. grainLoss
            GameData.AddLog("天降大旱，庄稼枯萎。")
            return { title = "旱灾降临", desc = "天旱数月，庄稼干枯，今季粮食大幅减产。",
                     choices = {
                         { text = "节衣缩食度日", effect = function() GameData.AddResource("grain", -grainLoss) end },
                         { text = "花银两购粮（-15银两）", cost = {silver = 15}, effect = function()
                             GameData.AddResource("grain", -math.ceil(grainLoss * 0.3))
                         end },
                     } }
        end,
    },
    {
        title = "水灾泛滥",
        weight = 7,
        check = function(s) return s.regionId == "henan" or s.regionId == "huguang" end,
        execute = function(s, report)
            local grainLoss = math.ceil(s.grain * 0.25)
            local silverLoss = math.random(5, 15)
            report.events[#report.events + 1] = "洪水泛滥！田产受损。"
            GameData.AddLog("大水漫堤，田庄遭灾。")
            return { title = "水灾泛滥", desc = "连日暴雨，河水暴涨，淹没田地。\n损失粮食" .. grainLoss .. "、银两" .. silverLoss,
                     choices = {
                         { text = "组织排涝（-10银两减半损失）", cost = {silver = 10}, effect = function()
                             GameData.AddResource("grain", -math.ceil(grainLoss * 0.5))
                         end },
                         { text = "听天由命", effect = function() GameData.AddResource("grain", -grainLoss); GameData.AddResource("silver", -silverLoss) end },
                     } }
        end,
    },
    {
        title = "蝗灾肆虐",
        weight = 6,
        check = function(s) return EraSystem.CanTriggerEvent(s.year, "locust") end,
        execute = function(s, report)
            local grainLoss = math.ceil(s.grain * 0.3)
            report.events[#report.events + 1] = "蝗虫铺天盖地，庄稼被啃食殆尽！"
            GameData.AddLog("蝗灾肆虐，颗粒无收。")
            return { title = "蝗灾肆虐", desc = "蝗虫遮天蔽日而来，所过之处寸草不生！\n损失粮食" .. grainLoss,
                     choices = {
                         { text = "发动族人灭蝗（减半损失）", effect = function() GameData.AddResource("grain", -math.ceil(grainLoss * 0.5)) end },
                         { text = "束手无策", effect = function() GameData.AddResource("grain", -grainLoss) end },
                     } }
        end,
    },
    {
        title = "瘟疫蔓延",
        weight = 6,
        check = function(s) return EraSystem.CanTriggerEvent(s.year, "plague") end,
        execute = function(s, report)
            local members = GameData.GetAliveMembers()
            local infected = 0
            for _, m in ipairs(members) do
                if math.random() < 0.25 then
                    m.health = math.max(10, m.health - math.random(15, 35))
                    if m.state ~= "生病" then m.prevState = m.state end
                    m.state = "生病"
                    infected = infected + 1
                end
            end
            report.events[#report.events + 1] = "瘟疫来袭，" .. infected .. "人感染！"
            GameData.AddLog("瘟疫蔓延，族中" .. infected .. "人感染。")
            return { title = "瘟疫蔓延", desc = "瘟疫在附近村镇蔓延开来，族中" .. infected .. "人不幸感染！",
                     choices = {
                         { text = "购药救治（-20银两）", cost = {silver = 20}, effect = function()
                             for _, m in ipairs(GameData.GetAliveMembers()) do
                                 if m.state == "生病" then m.health = math.min(100, m.health + 15) end
                             end
                         end },
                         { text = "隔离病人", effect = function() end },
                     } }
        end,
    },
    {
        title = "贵人赏识",
        weight = 5,
        check = function(s)
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.identity == "秀才" or m.identity == "举人" or m.identity == "监生" then return true end
            end
            return false
        end,
        execute = function(s, report)
            report.events[#report.events + 1] = "族中读书人受官员赏识，声望大增。"
            GameData.AddLog("族中才子受贵人赏识。")
            return { title = "贵人赏识", desc = "本地官员赏识你族中读书人的才学，邀请赴宴。",
                     choices = {
                         { text = "欣然赴约（+10声望）", effect = function() GameData.AddResource("fame", 10) end },
                         { text = "谦逊婉拒（+5声望）", effect = function() GameData.AddResource("fame", 5) end },
                     } }
        end,
    },
    {
        title = "偶遇名师",
        weight = 4,
        check = function(s)
            return #GameData.GetStudyingMembers() > 0
        end,
        execute = function(s, report)
            local students = GameData.GetStudyingMembers()
            local lucky = students[math.random(1, #students)]
            report.events[#report.events + 1] = lucky.name .. "偶遇名师指点，学识大增！"
            GameData.AddLog(lucky.name .. "得名师指点，茅塞顿开。")
            local gain1 = GrowthSystem.DiminishedGain(lucky.study, math.random(5, 8))  -- 原+15
            local gain2 = GrowthSystem.DiminishedGain(lucky.study, math.random(8, 12))  -- 原+25
            return { title = "偶遇名师", desc = lucky.name .. "在赶集时偶遇落魄名士，获其指点迷津，学识大增！",
                     choices = {
                         { text = "拜师求学（学识+" .. gain1 .. "）", effect = function() lucky.study = math.min(100, lucky.study + gain1) end },
                         { text = "挽留名师常住（-20银两，学识+" .. gain2 .. "）", cost = {silver = 20}, effect = function()
                             lucky.study = math.min(100, lucky.study + gain2)
                             GameData.AddResource("fame", 5)
                         end },
                     } }
        end,
    },
    {
        title = "官府加税",
        weight = 10,
        check = function(s) return EraSystem.CanTriggerEvent(s.year, "tax_increase") end,
        execute = function(s, report)
            local taxAmount = math.random(10, 25)
            report.events[#report.events + 1] = "朝廷加征三饷，额外征银" .. taxAmount .. "两。"
            GameData.AddLog("朝廷加征辽饷、剿饷、练饷。")
            return { title = "官府加税", desc = "朝廷为应对内忧外患，加征三饷，额外征银" .. taxAmount .. "两！",
                     choices = {
                         { text = "忍痛缴纳", effect = function() GameData.AddResource("silver", -taxAmount) end },
                         { text = "拖延推诿（-声望5）", effect = function() GameData.AddResource("silver", -math.ceil(taxAmount * 0.5)); GameData.AddResource("fame", -5) end },
                     } }
        end,
    },
    {
        title = "边关征兵",
        weight = 6,
        check = function(s) return EraSystem.CanTriggerEvent(s.year, "military_draft") end,
        execute = function(s, report)
            local males = {}
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.gender == "male" and m.age >= 16 and m.age <= 45 and m.state == "在家" then
                    males[#males + 1] = m
                end
            end
            if #males == 0 then return nil end
            local target = males[math.random(1, #males)]
            report.events[#report.events + 1] = "官府征兵，" .. target.name .. "被点名应征。"
            GameData.AddLog("官府征兵，" .. target.name .. "被征召。")
            return { title = "辽东征兵", desc = "官府下达征兵令，" .. target.name .. "（" .. target.age .. "岁）被点名应征！",
                     choices = {
                         { text = "服从征召", effect = function()
                             target.state = "从军"; target.identity = "士兵"; target.militaryRank = "士兵"
                         end },
                         { text = "花银两代役（-30银两）", cost = {silver = 30}, effect = function()
                         end },
                     } }
        end,
    },
    {
        title = "丰收之年",
        weight = 6,
        check = function(s) return EraSystem.CanTriggerEvent(s.year, "good_harvest") end,
        execute = function(s, report)
            local bonus = math.random(15, 35)
            report.events[#report.events + 1] = "风调雨顺，粮食额外丰收！"
            GameData.AddLog("今年风调雨顺，庄稼大丰收。")
            return { title = "丰收之年", desc = "风调雨顺，今年庄稼长势喜人，额外收获粮食" .. bonus .. "！",
                     choices = {
                         { text = "庆祝丰收（+粮食" .. bonus .. "）", effect = function() GameData.AddResource("grain", bonus) end },
                     } }
        end,
    },

    -- ===== 社交与声望事件 =====
    {
        title = "乡邻求助",
        weight = 5,
        check = function(s) return s.fame >= 15 and s.silver >= 10 end,
        execute = function(s, report)
            report.events[#report.events + 1] = "邻村遭灾，前来求助。"
            return { title = "乡邻求助", desc = "邻村遭遇不幸，村长登门恳请你族伸出援手。",
                     choices = {
                         { text = "慷慨相助（-20银两-15粮，+12声望）", cost = {silver = 20, grain = 15}, effect = function()
                             GameData.AddResource("fame", 12)
                             if math.random() < 0.4 then s.pendingChainEvent = "邻村报恩" end
                         end },
                         { text = "少量帮助（-5银两，+3声望）", cost = {silver = 5}, effect = function()
                             GameData.AddResource("fame", 3)
                         end },
                         { text = "推脱婉拒", effect = function() GameData.AddResource("fame", -3) end },
                     } }
        end,
    },
    {
        title = "邻村报恩",
        weight = 0,
        isChainEvent = true,
        check = function(s) return false end,
        execute = function(s, report)
            local rewardGrain = math.random(20, 40)
            report.events[#report.events + 1] = "邻村送来粮食报恩。"
            GameData.AddLog("邻村感恩你族义举，送来粮食" .. rewardGrain .. "。")
            return { title = "邻村报恩", desc = "此前受你族恩惠的邻村渡过难关，特送来粮食" .. rewardGrain .. "以表谢意。\n善有善报！",
                     choices = {
                         { text = "收下谢礼", effect = function() GameData.AddResource("grain", rewardGrain); GameData.AddResource("fame", 5) end },
                     } }
        end,
    },
    {
        title = "世家来访",
        weight = 3,
        check = function(s) return s.fame >= 40 and s.clanRank >= 3 end,
        execute = function(s, report)
            report.events[#report.events + 1] = "远方世家派人来访，欲结盟好。"
            return { title = "世家来访", desc = "远方一门望族闻你族声望日隆，派人前来联络，欲世代交好。",
                     choices = {
                         { text = "设宴款待结盟（-25银两，+20声望）", cost = {silver = 25}, effect = function()
                             GameData.AddResource("fame", 20)
                             GameData.AddLog("与远方世家结盟，声望大增。")
                         end },
                         { text = "礼貌接待（+8声望）", effect = function() GameData.AddResource("fame", 8) end },
                     } }
        end,
    },
    {
        title = "商路发现",
        weight = 4,
        check = function(s) return #GameData.GetMerchantMembers() >= 2 end,
        execute = function(s, report)
            local merchants = GameData.GetMerchantMembers()
            local lucky = merchants[math.random(1, #merchants)]
            report.events[#report.events + 1] = lucky.name .. "发现了一条新的商路。"
            return { title = "商路发现", desc = lucky.name .. "在经商途中发现一条鲜为人知的商路，利润丰厚！",
                     choices = {
                         { text = "投资开辟（-30银两，+商铺）", cost = {silver = 30}, effect = function()
                             GameData.AddIndustry("shop")
                             GameData.AddLog(lucky.name .. "开辟新商路，族中增添商铺一间。")
                         end },
                         { text = "记下位置，日后再说", effect = function() GameData.AddResource("fame", 2) end },
                     } }
        end,
    },

    -- ===== 后期高难事件（1638+） =====
    {
        title = "大军过境征粮",
        weight = 8,
        check = function(s) return EraSystem.CanTriggerEvent(s.year, "army_passage") end,
        execute = function(s, report)
            local grainReq = math.ceil(s.grain * 0.35)
            local silverReq = math.random(15, 40)
            report.events[#report.events + 1] = "官军过境，强行征粮！"
            GameData.AddLog("大军过境，强征军粮。")
            return { title = "大军过境征粮", desc = "一支官军路过，将领下令就地征粮，态度蛮横不可抗拒。\n索要粮食" .. grainReq .. "、银两" .. silverReq,
                     choices = {
                         { text = "含泪交出", effect = function()
                             GameData.AddResource("grain", -grainReq); GameData.AddResource("silver", -silverReq)
                         end },
                         { text = "托关系减免（-声望10）", effect = function()
                             GameData.AddResource("grain", -math.ceil(grainReq * 0.5))
                             GameData.AddResource("silver", -math.ceil(silverReq * 0.5))
                             GameData.AddResource("fame", -10)
                         end },
                     } }
        end,
    },
    {
        title = "匪帮勒索",
        weight = 7,
        check = function(s) return EraSystem.CanTriggerEvent(s.year, "bandit_raid") and s.silver >= 20 end,
        execute = function(s, report)
            local demand = math.random(25, 60)
            report.events[#report.events + 1] = "一伙山匪派人来勒索保护费。"

            -- 选2个成年族人出战
            local fighterIds, fighterNames = PickRandomFighters(2)
            local fighterInfo = #fighterIds > 0
                and ("可派出" .. table.concat(fighterNames, "、") .. "迎战")
                or "族中无人可战！"

            return { title = "匪帮勒索",
                     desc = "附近山寨的匪帮派人传话，要你族缴纳银两" .. demand .. "，否则后果自负！\n\n" .. fighterInfo,
                     choices = {
                         { text = "缴纳保护费（-银两" .. demand .. "）", cost = {silver = demand}, effect = function()
                             GameData.AddLog("缴纳保护费银两" .. demand .. "，匪帮暂时离去。")
                         end },
                         { text = "拒绝！召集族人备战", effect = function()
                             if #fighterIds == 0 then
                                 -- 无人可战，被迫交更多
                                 local extraLoss = math.ceil(demand * 0.5)
                                 GameData.AddResource("silver", -(demand + extraLoss))
                                 GameData.AddResource("fame", -3)
                                 GameData.AddLog("族中无人能战，匪帮肆意劫掠！")
                                 s.pendingEvents[#s.pendingEvents + 1] = {
                                     title = "无力抵抗",
                                     desc = "族中无人能战，匪帮冲入村寨劫掠！\n\n损失：银两" ..
                                         (demand + extraLoss) .. "、声望-3",
                                     choices = { { text = "忍辱负重", effect = function() end } }
                                 }
                                 return
                             end
                             -- 生成敌族数据，调用3D战斗
                             local rival = GenerateEventRival("山寨匪帮", s.clanRank, s.year, "bandit")
                             local defeatPenalty = {
                                 silver = demand * 2,
                                 grain = demand,
                                 cloth = math.ceil(demand * 0.5),
                                 fame = 5 + s.clanRank,
                             }
                             local victoryBonus = {
                                 fame = 5 + s.clanRank * 2,
                             }
                             local GS = require("UI.GameScreen")
                             GS.EnterBattle(rival, fighterIds, {
                                 soldierCount = 0,  -- 事件战斗：纯将领对决，不带士兵
                                 onSettle = CreateEventSettlement(defeatPenalty, victoryBonus),
                             })
                         end },
                     } }
        end,
    },
    {
        title = "难民涌入",
        weight = 6,
        check = function(s) return EraSystem.CanTriggerEvent(s.year, "refugee_influx") end,
        execute = function(s, report)
            local count = math.random(2, 4)
            report.events[#report.events + 1] = "大批难民涌入本地。"
            return { title = "难民涌入", desc = "战乱饥荒之中，" .. count .. "户难民拖家带口来到你族地盘，跪求收留。",
                     choices = {
                         { text = "全部收留（-" .. count * 8 .. "粮，+" .. count .. "人口）", effect = function()
                             GameData.AddResource("grain", -count * 8)
                             for i = 1, count do
                                 GameData.CreateMember({
                                     gender = math.random() > 0.4 and "male" or "female",
                                     age = 18 + math.random(0, 20), generation = 1,
                                     state = "在家", health = 30 + math.random(0, 30),
                                     study = 5 + math.random(3, 15), martial = 5 + math.random(2, 12),
                                 })
                             end
                             GameData.AddResource("fame", count * 2)
                             GameData.AddLog("收留" .. count .. "户难民，族中人口增加。")
                         end },
                         { text = "收留一户", effect = function()
                             GameData.AddResource("grain", -8)
                             local rAge = 20 + math.random(0, 15)
                             GameData.CreateMember({
                                 gender = math.random() > 0.4 and "male" or "female",
                                 age = rAge, generation = 1,
                                 state = "在家", health = 40 + math.random(0, 30),
                                 study = 5 + math.random(3, math.min(15, math.floor(rAge * 0.6))),
                                 martial = 5 + math.random(2, math.min(12, math.floor(rAge * 0.5))),
                             })
                             GameData.AddResource("fame", 2)
                         end },
                         { text = "驱赶（-声望5）", effect = function() GameData.AddResource("fame", -5) end },
                     } }
        end,
    },
    {
        title = "粮价暴涨",
        weight = 5,
        check = function(s) return EraSystem.CanTriggerEvent(s.year, "grain_inflation") and s.grain >= 50 end,
        execute = function(s, report)
            local sellGrain = math.ceil(s.grain * 0.2)
            local silverGain = sellGrain * 3
            report.events[#report.events + 1] = "各地饥荒，粮价暴涨！"
            return { title = "粮价暴涨", desc = "战乱频仍，各地粮价飞涨！你族存粮有余，有商人出高价购粮。\n可卖粮食" .. sellGrain .. "，获银两" .. silverGain,
                     choices = {
                         { text = "趁机卖粮", effect = function()
                             GameData.AddResource("grain", -sellGrain); GameData.AddResource("silver", silverGain)
                         end },
                         { text = "囤粮自保", effect = function() end },
                         { text = "低价济民（-" .. sellGrain .. "粮，+10声望）", effect = function()
                             GameData.AddResource("grain", -sellGrain); GameData.AddResource("fame", 10)
                             GameData.AddLog("族长开仓济民，百姓感恩。")
                         end },
                     } }
        end,
    },
    {
        title = "叛军招揽",
        weight = 5,
        check = function(s) return EraSystem.CanTriggerEvent(s.year, "rebel_recruit") end,
        execute = function(s, report)
            report.events[#report.events + 1] = "有叛军势力派人来招揽。"
            return { title = "叛军招揽", desc = "李自成麾下一名将领派人秘密来访，许诺你族若归顺可封官进爵。这是一步险棋……",
                     choices = {
                         { text = "严辞拒绝（+8声望）", effect = function()
                             GameData.AddResource("fame", 8)
                             GameData.AddLog("族长义正辞严拒绝叛军招揽。")
                             if math.random() < 0.25 then s.pendingChainEvent = "叛军报复" end
                         end },
                         { text = "虚与委蛇", effect = function()
                             GameData.AddResource("silver", 30)
                             GameData.AddResource("fame", -5)
                         end },
                         { text = "暗中归附（+50银两，-20声望）", effect = function()
                             GameData.AddResource("silver", 50); GameData.AddResource("fame", -20)
                             GameData.AddLog("宗族暗中投靠叛军，声望大跌。")
                         end },
                     } }
        end,
    },
    {
        title = "叛军报复",
        weight = 0,
        isChainEvent = true,
        check = function(s) return false end,
        execute = function(s, report)
            local silverLoss = math.ceil(s.silver * 0.25)
            local grainLoss = math.ceil(s.grain * 0.2)
            report.events[#report.events + 1] = "叛军恼羞成怒，派兵前来报复！"
            GameData.AddLog("叛军因被拒而报复，劫掠族中财物。")
            return { title = "叛军报复", desc = "你的拒绝激怒了叛军，他们派了一小队人马前来打砸劫掠！\n损失银两" .. silverLoss .. "、粮食" .. grainLoss,
                     choices = {
                         { text = "咬牙承受", effect = function()
                             GameData.AddResource("silver", -silverLoss); GameData.AddResource("grain", -grainLoss)
                             GameData.AddResource("fame", 5)
                         end },
                     } }
        end,
    },
    {
        title = "义军护乡",
        weight = 4,
        check = function(s)
            local fighters = 0
            for _, m in ipairs(GameData.GetAliveMembers()) do
                if m.martial >= 40 then fighters = fighters + 1 end
            end
            return EraSystem.CanTriggerEvent(s.year, "fortress_battle") and fighters >= 2 and s.fortCount >= 1
        end,
        execute = function(s, report)
            report.events[#report.events + 1] = "附近乡民请求你族组织义军护乡。"
            return { title = "义军护乡", desc = "盗匪横行，附近数村乡民联名请求你族组织团练，保一方平安。",
                     choices = {
                         { text = "慨然应允（-15银两-20粮，+25声望）", cost = {silver = 15, grain = 20}, effect = function()
                             GameData.AddResource("fame", 25)
                             for _, m in ipairs(GameData.GetAliveMembers()) do
                                 if m.martial >= 20 then m.martial = math.min(100, m.martial + 5) end
                             end
                             GameData.AddLog("组织义军护乡，声望大增。")
                         end },
                         { text = "量力而行（-5银两，+10声望）", cost = {silver = 5}, effect = function()
                             GameData.AddResource("fame", 10)
                         end },
                     } }
        end,
    },
}

-- ============================================================================
-- 随机事件处理函数
-- ============================================================================

function EventSystem.ProcessRandomEvents(report)
    local s = GameData.state

    -- 先处理连锁事件（上回合某事件触发的后续）
    if s.pendingChainEvent then
        local chainTitle = s.pendingChainEvent
        s.pendingChainEvent = nil
        local chainHandled = false
        -- 先在原有事件池中查找
        for _, evt in ipairs(EventSystem.RANDOM_EVENTS) do
            if evt.title == chainTitle and evt.isChainEvent then
                local chainResult = evt.execute(s, report)
                if chainResult and #s.pendingEvents < 5 then
                    s.pendingEvents[#s.pendingEvents + 1] = chainResult
                end
                chainHandled = true
                break
            end
        end
        -- 未找到则在扩展事件池中查找（支持扩展池的连锁事件）
        if not chainHandled then
            local chainResult = EventRegistry.ExecuteChainEvent(chainTitle, s, report)
            if chainResult and #s.pendingEvents < 5 then
                s.pendingEvents[#s.pendingEvents + 1] = chainResult
            end
        end
    end

    -- 随机事件触发率：根据年代动态调整
    local triggerRate = EraSystem.GetEventTriggerRate(s.year)
    if math.random() > triggerRate then return end

    local difficulty = GameData.GetDifficulty()
    -- 灾害类事件标题列表（受难度影响权重）
    local disasterEvents = {
        ["旱灾降临"] = true, ["水灾泛滥"] = true, ["蝗灾肆虐"] = true,
        ["瘟疫蔓延"] = true, ["流寇过境"] = true, ["官府加税"] = true,
        ["辽东征兵"] = true, ["大军过境征粮"] = true, ["匪帮勒索"] = true,
        ["叛军招揽"] = true,
    }

    -- 筛选可触发的事件（排除连锁事件，它们只由连锁触发）
    local available = {}
    local totalWeight = 0
    for _, evt in ipairs(EventSystem.RANDOM_EVENTS) do
        if not evt.isChainEvent and evt.check(s) then
            local w = evt.weight
            if disasterEvents[evt.title] then
                w = math.ceil(w * difficulty.disasterMul)
            end
            available[#available + 1] = { evt = evt, weight = w }
            totalWeight = totalWeight + w
        end
    end
    if #available == 0 then return end

    -- 加权随机选择
    local roll = math.random() * totalWeight
    local cumulative = 0
    local chosen = available[1].evt
    for _, entry in ipairs(available) do
        cumulative = cumulative + entry.weight
        if roll <= cumulative then
            chosen = entry.evt
            break
        end
    end

    local eventResult = chosen.execute(s, report)
    if eventResult then
        if #s.pendingEvents < 5 then
            s.pendingEvents[#s.pendingEvents + 1] = eventResult
        end
    end

    -- ========================================================================
    -- 扩展事件池：品阶专属事件（额外触发，不替代原有事件）
    -- 50% 概率从扩展池中选取一个匹配当前品阶的事件
    -- ========================================================================
    if #s.pendingEvents < 5 and math.random() < 0.5 then
        local extResult = EventRegistry.SelectAndExecute(s, report)
        if extResult then
            s.pendingEvents[#s.pendingEvents + 1] = extResult
        end
    end
end

return EventSystem
