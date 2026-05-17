-- ============================================================================
-- 大明浮生志2 - 月度结算系统
-- 每月自动结算：资源产出、口粮消耗、族人成长、随机事件
-- ============================================================================

local GameData = require("Data.GameData")
local EraSystem = require("Data.EraSystem")
local EventSystem = require("Systems.EventSystem")
local GoalSystem = require("Systems.GoalSystem")
local WeatherSystem = require("Systems.WeatherSystem")
local EndingSystem = require("Systems.EndingSystem")
local GrowthSystem = require("Systems.GrowthSystem")
local MarketSystem = require("Systems.MarketSystem")
local SkillSystem = require("Systems.SkillSystem")
local EquipmentSystem = require("Systems.EquipmentSystem")
local MemberData = require("Data.MemberData")

local MonthlyUpdate = {}

-- ============================================================================
-- 主结算函数
-- ============================================================================

function MonthlyUpdate.Execute()
    local s = GameData.state
    if not s then return {} end

    local report = {
        year = s.year,
        month = s.month,
        incomes = { silver = 0, grain = 0, cloth = 0, fame = 0 },
        expenses = { silver = 0, grain = 0, cloth = 0, fame = 0 },
        events = {},
    }

    -- 重置保底婚姻标记
    s._marriageGuaranteedGen = nil

    local ruleEffects = GameData.GetClanRuleEffects()
    -- 合并家训效果
    local mottoEffects = GameData.GetMottoEffects()
    for k, v in pairs(mottoEffects) do
        if k ~= "initSilverBonus" then  -- 初始银两只在开局时生效
            ruleEffects[k] = (ruleEffects[k] or 0) + v
        end
    end

    -- 0. 生成本月天气
    local weather = WeatherSystem.GenerateWeather(s.month, s.year)
    WeatherSystem.ApplyEffects(weather, report)

    -- 1. 产业产出
    MonthlyUpdate.ProcessIndustries(report, ruleEffects)

    -- 2. 经商族人收益
    MonthlyUpdate.ProcessMerchants(report, ruleEffects)

    -- 2.5 打工族人工资
    MonthlyUpdate.ProcessLabor(report)

    -- 3. 人口消耗
    MonthlyUpdate.ProcessConsumption(report, ruleEffects)

    -- 3.5 军队维护
    MonthlyUpdate.ProcessArmyMaintenance(report)

    -- 4. 税赋
    MonthlyUpdate.ProcessTax(report)

    -- 5. 族人成长
    MonthlyUpdate.ProcessGrowth(report, ruleEffects)

    -- 6. 随机事件
    EventSystem.ProcessRandomEvents(report)

    -- 6.3 保底结婚检测：确保每代至少一人结婚，防止家族断绝
    MonthlyUpdate.CheckGuaranteedMarriage(report)

    -- 6.5 族人际遇
    MonthlyUpdate.ProcessEncounters(report, ruleEffects)

    -- 7. 科举/从军进度
    MonthlyUpdate.ProcessCareer(report, ruleEffects)

    -- 7.5 书院/武馆成长
    MonthlyUpdate.ProcessAcademies(report, ruleEffects)

    -- 7.6 探索历练结算
    MonthlyUpdate.ProcessExpeditions(report, ruleEffects)

    -- 7.7 宠物寿命检查
    MonthlyUpdate.ProcessPetAging(report)

    -- 7.8 坐堂郎中自动治疗（每季度检查一次，年上限3人）
    MonthlyUpdate.ProcessClinicDoctor(report)

    -- 8. 族规每月固定效果
    MonthlyUpdate.ProcessClanRulesDrain(report, ruleEffects)

    -- 8.5 集市价格更新（乡绅以上）
    if s.clanRank >= 3 then
        MarketSystem.EnsureState()
        MarketSystem.UpdatePrices()
    end

    -- 8.6 技能专精月度被动效果
    SkillSystem.ProcessMonthly(report)

    -- 9. 结算资源
    local netSilver = report.incomes.silver - report.expenses.silver
    local netGrain = report.incomes.grain - report.expenses.grain
    local netCloth = report.incomes.cloth - report.expenses.cloth
    local netFame = report.incomes.fame - report.expenses.fame

    GameData.AddResource("silver", netSilver)
    GameData.AddResource("grain", netGrain)
    GameData.AddResource("cloth", netCloth)
    GameData.AddResource("fame", netFame)

    -- 9.5 累积年度统计
    if not s.yearStats then
        s.yearStats = {
            incomes = { silver = 0, grain = 0, cloth = 0, fame = 0 },
            expenses = { silver = 0, grain = 0, cloth = 0, fame = 0 },
            births = 0, deaths = 0, examPasses = 0, militaryMerits = 0,
            majorEvents = {},
        }
    end
    if not s.yearStartSnapshot then
        s.yearStartSnapshot = {
            silver = s.silver - netSilver, grain = s.grain - netGrain,
            cloth = s.cloth - netCloth, fame = s.fame - netFame,
            aliveCount = #GameData.GetAliveMembers(),
            clanRank = s.clanRank, industryCount = #s.industries, fortCount = s.fortCount,
            totalBirths = s.totalBirths, totalDeaths = s.totalDeaths,
            totalExamPasses = s.totalExamPasses, totalMilitaryMerits = s.totalMilitaryMerits,
        }
    end
    local ys = s.yearStats
    ys.incomes.silver = ys.incomes.silver + report.incomes.silver
    ys.incomes.grain = ys.incomes.grain + report.incomes.grain
    ys.incomes.cloth = ys.incomes.cloth + report.incomes.cloth
    ys.incomes.fame = ys.incomes.fame + report.incomes.fame
    ys.expenses.silver = ys.expenses.silver + report.expenses.silver
    ys.expenses.grain = ys.expenses.grain + report.expenses.grain
    ys.expenses.cloth = ys.expenses.cloth + report.expenses.cloth
    ys.expenses.fame = ys.expenses.fame + report.expenses.fame

    -- 10. 推进时间
    s.month = s.month + 1
    if s.month > 12 then
        -- 生成年终总结（在年份递增之前，用当前统计数据）
        MonthlyUpdate.BuildYearEndSummary(report, s)

        s.month = 1
        s.year = s.year + 1
        -- 检查年度目标完成情况（先结算旧年目标，再生成新年目标）
        GoalSystem.CheckYearlyGoals(report)
        GoalSystem.GenerateYearlyGoals(report)
        -- 检查历史事件
        MonthlyUpdate.CheckHistoryEvent(report)

        -- 重置年度统计
        s.yearStats = nil
        s.yearStartSnapshot = nil
        s._clinicHealsThisYear = 0  -- 重置坐堂郎中年度治疗计数

        -- 年末自动存档
        local SaveSystem = require("Systems.SaveSystem")
        SaveSystem.AutoSave()
        print("[MonthlyUpdate] 年末自动存档完成 (" .. s.year .. "年)")
    end
    s.totalMonths = s.totalMonths + 1

    -- 10.5 成就检查
    GoalSystem.CheckAchievements(report)

    -- 11. 检查饥荒
    if s.grain <= 0 then
        report.events[#report.events + 1] = "粮食耗尽！族人开始挨饿，健康下降。"
        GameData.AddLog("粮食耗尽，族人饥寒交迫！")
        for _, m in ipairs(GameData.GetAliveMembers()) do
            -- 新手保护：前3年饥荒伤害减半；独苗保护：仅剩1人时伤害极低
            local hpLoss = math.random(5, 15)
            if s.totalMonths < 36 then hpLoss = math.ceil(hpLoss * 0.5) end
            if #GameData.GetAliveMembers() <= 1 then hpLoss = math.max(1, math.ceil(hpLoss * 0.3)) end
            m.health = math.max(0, m.health - hpLoss)
            if m.health <= 0 then
                EquipmentSystem.HandleInheritance(m)
                GameData.CheckPatriarchDeath(m)
                m.alive = false
                m.state = "亡故"
                s.totalDeaths = s.totalDeaths + 1
                report.events[#report.events + 1] = m.name .. "因饥饿病逝。"
                GameData.AddLog(m.name .. "因饥饿病逝，享年" .. m.age .. "岁。")
                GameData.NotifyDeath(m, "因饥饿病逝", "粮尽人亡，惨不忍睹。务必尽快筹粮！")
            end
        end
    end

    -- 12. 检查破产挫折（不终止游戏，降级+惩罚）
    EndingSystem.CheckBankruptcy(report)

    -- 13. 检查隐藏结局（每月检查，优先级高的先触发）
    EndingSystem.Check(report)

    return report
end

-- ============================================================================
-- 产业产出（受族规影响）
-- ============================================================================

-- 季节产出系数（农业受季节影响，商业受节令影响）
local SEASON_FACTORS = {
    grain = { 0.6, 0.8, 1.0, 1.2, 1.5, 1.3, 1.2, 1.0, 1.4, 1.0, 0.7, 0.5 }, -- 春种夏长秋收冬歇
    silver = { 0.9, 1.0, 1.0, 1.1, 1.0, 1.0, 1.0, 1.1, 1.0, 1.2, 1.1, 1.3 }, -- 年底/节令旺季
    cloth  = { 0.8, 0.9, 1.0, 1.0, 1.0, 1.0, 1.0, 1.1, 1.2, 1.1, 1.0, 0.9 }, -- 秋冬略旺
}

function MonthlyUpdate.ProcessIndustries(report, ruleEffects)
    local s = GameData.state

    -- 统计同类产业数量（用于连锁加成）
    local typeCount = {}
    for _, ind in ipairs(s.industries) do
        typeCount[ind.typeId] = (typeCount[ind.typeId] or 0) + 1
    end

    -- 统计特殊效果（部分效果影响全族，需汇总后在本函数末尾处理）
    local hasHerbShop = false      -- 药铺：降低全族生病概率
    local hasHorseRanch = false    -- 马场：从军战力提升
    local hasSmithyCount = 0       -- 铁匠铺数量：武艺成长加成
    local hasBookshopCount = 0     -- 书坊数量：学识成长加成

    for _, ind in ipairs(s.industries) do
        local indType = GameData.GetIndustryType(ind.typeId)
        if not indType then goto continueInd end

        -- 收集全局特殊效果
        if indType.specialEffect == "reduce_sick_15" then hasHerbShop = true end
        if indType.specialEffect == "military_boost_15" then hasHorseRanch = true end
        if indType.specialEffect == "military_boost_25" then hasHorseRanch = true end  -- 军械坊：更强军事加成
        if indType.specialEffect == "martial_grow_10" then hasSmithyCount = hasSmithyCount + 1 end
        if indType.specialEffect == "study_grow_10" then hasBookshopCount = hasBookshopCount + 1 end

        -- 跳过无产出产业（如寨堡）
        if indType.resource == "none" then goto continueInd end

        -- 计算基础产出乘数（管理人、天赋等）
        local manageMul = 1.0
        if ind.assignedMemberId then
            local member = GameData.GetMember(ind.assignedMemberId)
            if member and member.alive then
                -- 兼职管理（读书/从军/经商/为官等非"在家"状态）加成降低
                local isPartTime = member.state ~= "在家" and member.state ~= "生病"
                manageMul = isPartTime and 1.15 or 1.3
                -- 天赋加成
                if member.talent then
                    if member.talent.id == "diligent" then
                        manageMul = manageMul * 1.15
                    elseif member.talent.id == "merchant" and indType.resource == "silver" then
                        manageMul = manageMul * 1.25
                    elseif member.talent.id == "lazy" then
                        manageMul = manageMul * 0.8
                    end
                end
            else
                ind.assignedMemberId = nil
            end
        end

        -- 通用乘数（族规、专精、等级等，所有资源共享）
        local commonMul = 1.0 + (ruleEffects.allOutputMul or 0)

        -- 产业专精加成：同类产业>=3个，每多一个+8%产出
        local sameCount = typeCount[ind.typeId] or 1
        if sameCount >= 3 then
            commonMul = commonMul + (sameCount - 2) * 0.08
        end

        -- 高等级产业额外加成
        if ind.level >= 5 then
            commonMul = commonMul + 0.30
        elseif ind.level >= 3 then
            commonMul = commonMul + 0.15
        end

        -- === 处理主资源 ===
        local function calcResourceOutput(res, baseOut)
            -- A4: 对数递减公式替代线性公式
            local output = baseOut * (1 + math.log(ind.level) * 1.5)
            output = output * manageMul

            local resMul = commonMul
            -- 族规：粮食产出专项
            if res == "grain" then
                resMul = resMul + (ruleEffects.grainOutputMul or 0)
            end
            -- 季节波动
            local sf = SEASON_FACTORS[res]
            if sf then resMul = resMul * sf[s.month] end
            -- 天气影响粮食
            if res == "grain" then
                -- 天气免疫特效
                if indType.specialEffect == "weather_immune" then
                    -- 不受天气影响
                else
                    resMul = resMul * WeatherSystem.GetGrainMultiplier()
                end
            end
            -- 船队季风放大效果
            if indType.specialEffect == "season_amplify" and sf then
                local factor = sf[s.month]
                if factor > 1.0 then
                    resMul = resMul * 1.3  -- 旺季额外放大
                elseif factor < 0.9 then
                    resMul = resMul * 0.7  -- 淡季额外削弱
                end
            end

            output = math.floor(output * resMul)
            return math.max(1, output)
        end

        -- 主资源产出
        local mainOutput = calcResourceOutput(indType.resource, indType.baseOutput)

        -- 镖局风险折损
        if indType.specialEffect == "risk_loss_20" then
            if math.random() < 0.20 then
                local lost = math.floor(mainOutput * 0.5)
                mainOutput = mainOutput - lost
                report.events[#report.events + 1] = indType.name .. "押镖途中遭劫，损失银两" .. lost .. "。"
            end
        end

        -- 盐场官府稽查风险
        if indType.specialEffect == "risk_tax_30" then
            if math.random() < 0.10 then
                local fine = math.floor(mainOutput * 0.6)
                mainOutput = mainOutput - fine
                report.events[#report.events + 1] = indType.name .. "被官府稽查，罚没银两" .. fine .. "。"
            end
        end

        report.incomes[indType.resource] = report.incomes[indType.resource] + mainOutput

        -- === 处理第二资源 ===
        if indType.resource2 and indType.baseOutput2 and indType.resource2 ~= "none" then
            local out2 = calcResourceOutput(indType.resource2, indType.baseOutput2)
            report.incomes[indType.resource2] = (report.incomes[indType.resource2] or 0) + out2
        end

        -- === 处理第三资源 ===
        if indType.resource3 and indType.baseOutput3 and indType.resource3 ~= "none" then
            local out3 = calcResourceOutput(indType.resource3, indType.baseOutput3)
            report.incomes[indType.resource3] = (report.incomes[indType.resource3] or 0) + out3
        end

        -- === 处理即时特殊效果 ===

        -- 酒坊：每月额外消耗粮食
        if indType.specialEffect == "consume_grain_3" then
            report.expenses.grain = report.expenses.grain + 3
        end

        -- 皇商行：每月消耗声望3
        if indType.specialEffect == "consume_fame_3" then
            report.expenses.fame = (report.expenses.fame or 0) + 3
        end

        -- 当铺：按银两存量+1%收益（多个当铺不叠加利率，上限150）
        if indType.specialEffect == "interest_1pct" then
            if not report._pawnshopProcessed then
                report._pawnshopProcessed = true
                local pawnCount = 0
                for _, ind2 in ipairs(s.industries) do
                    local it2 = GameData.GetIndustryType(ind2.typeId)
                    if it2 and it2.specialEffect == "interest_1pct" then
                        pawnCount = pawnCount + 1
                    end
                end
                local cap = 100 + (pawnCount - 1) * 50
                local interest = math.min(cap, math.floor(s.silver * 0.01))
                if interest > 0 then
                    report.incomes.silver = report.incomes.silver + interest
                end
            end
        end

        -- 钱庄：按总银两额外生利2%（多个钱庄不叠加利率，仅提升利息上限）
        -- 基础上限200，每多一个钱庄+100
        if indType.specialEffect == "interest_2pct" then
            if not report._moneyHouseProcessed then
                report._moneyHouseProcessed = true
                -- 统计钱庄数量
                local moneyHouseCount = 0
                for _, ind2 in ipairs(s.industries) do
                    local it2 = GameData.GetIndustryType(ind2.typeId)
                    if it2 and it2.specialEffect == "interest_2pct" then
                        moneyHouseCount = moneyHouseCount + 1
                    end
                end
                local interestCap = 200 + (moneyHouseCount - 1) * 100
                local interest = math.min(interestCap, math.floor(s.silver * 0.02))
                if interest > 0 then
                    report.incomes.silver = report.incomes.silver + interest
                end
            end
        end

        ::continueInd::
    end

    -- === A2: 产业月维护费 ===
    local totalMaintenance = 0
    for _, ind in ipairs(s.industries) do
        local indType = GameData.GetIndustryType(ind.typeId)
        if indType and indType.resource ~= "none" then
            local fee = math.floor(indType.cost * 0.018 * ind.level)
            totalMaintenance = totalMaintenance + fee
        end
    end
    if totalMaintenance > 0 then
        report.expenses.silver = report.expenses.silver + totalMaintenance
        report.maintenanceFee = totalMaintenance
    end

    -- === 全局特殊效果存入report，供其他系统使用 ===
    report.industryEffects = {
        reduceSick = hasHerbShop,         -- 药铺效果
        horseRanch = hasHorseRanch,       -- 马场效果
        smithyBonus = hasSmithyCount,     -- 铁匠铺数量
        bookshopBonus = hasBookshopCount, -- 书坊数量
    }
end

-- ============================================================================
-- 经商族人月收益
-- ============================================================================

function MonthlyUpdate.ProcessMerchants(report, ruleEffects)
    local s = GameData.state
    local merchants = GameData.GetMerchantMembers()

    for _, m in ipairs(merchants) do
        -- 基础收益 5-12 银两
        local income = math.random(5, 12)

        -- 天赋加成
        if m.talent and m.talent.id == "merchant" then
            income = math.floor(income * 1.25)
        end
        if m.talent and m.talent.id == "lazy" then
            income = math.floor(income * 0.8)
        end
        if m.talent and m.talent.id == "diligent" then
            income = math.floor(income * 1.15)
        end

        -- 族规加成
        income = math.floor(income * (1.0 + (ruleEffects.tradeIncomeMul or 0)))

        -- 乱世经商风险：根据年代波动
        local tradeVol = EraSystem.GetTradeVolatility(s.year)
        if tradeVol > 0.05 then
            local roll = math.random() - 0.5
            income = math.floor(income * (1.0 + roll * tradeVol))
            income = math.max(0, income)
        end

        report.incomes.silver = report.incomes.silver + income
    end

    if #merchants > 0 then
        report.events[#report.events + 1] = #merchants .. "名族人经商，本月共获银两" .. report.incomes.silver .. "。"
    end
end

-- ============================================================================
-- 打工收入
-- ============================================================================

function MonthlyUpdate.ProcessLabor(report)
    local laborers = GameData.GetMembersByState("打工")
    for _, m in ipairs(laborers) do
        local job = MemberData.GetLaborJob(m.laborJob)
        local wage = job and job.wage or 3
        report.incomes.silver = report.incomes.silver + wage
    end
end

-- ============================================================================
-- 人口消耗（受族规影响）
-- ============================================================================

function MonthlyUpdate.ProcessConsumption(report, ruleEffects)
    local alive = GameData.GetAliveMembers()
    local grainCost = 0
    local clothCost = 0

    for _, m in ipairs(alive) do
        if m.age <= 5 then
            grainCost = grainCost + 1
        elseif m.age <= 14 then
            grainCost = grainCost + 2
        else
            grainCost = grainCost + 3
        end
    end

    -- 族规：粮食消耗调整
    grainCost = math.floor(grainCost * (1.0 + (ruleEffects.grainConsumeMul or 0)))
    grainCost = math.max(1, grainCost)

    -- 每季度消耗布匹
    local s = GameData.state
    if s.month % 3 == 0 then
        clothCost = math.ceil(#alive * 0.5)
        -- 族规：布匹消耗调整
        clothCost = math.floor(clothCost * (1.0 + (ruleEffects.clothConsumeMul or 0)))
        clothCost = math.max(0, clothCost)
    end

    -- 宠物消耗粮食（存活宠物每月1粮）
    local petGrain = 0
    if s and s.pet and s.pet.alive then
        petGrain = 1
    end

    report.expenses.grain = report.expenses.grain + grainCost + petGrain
    report.expenses.cloth = report.expenses.cloth + clothCost
end

-- ============================================================================
-- 军队维护（养兵月耗）
-- ============================================================================

--- 每月军队维护费用：每1000兵消耗 银两5 + 粮食8
--- 资源不足时士兵逃亡（按比例减少）
function MonthlyUpdate.ProcessArmyMaintenance(report)
    local s = GameData.state
    if not s or not s.army then return end

    local totalSoldiers = (s.army.infantry or 0) + (s.army.archers or 0)
    if totalSoldiers <= 0 then return end

    -- 每1000兵月耗（银1200 粮1500）
    local batches = totalSoldiers / 1000
    local silverCost = math.ceil(batches * 1200)
    local grainCost = math.ceil(batches * 1500)

    -- 检查是否能承受
    local canAffordSilver = s.silver >= silverCost
    local canAffordGrain = s.grain >= grainCost

    if canAffordSilver and canAffordGrain then
        -- 正常扣费
        GameData.AddResource("silver", -silverCost)
        GameData.AddResource("grain", -grainCost)
        report.expenses.silver = report.expenses.silver + silverCost
        report.expenses.grain = report.expenses.grain + grainCost
    else
        -- 资源不足，部分士兵逃亡
        -- 能养活多少兵：取银两和粮食能支撑的较小值
        local canSupportBySilver = (s.silver > 0) and math.floor(s.silver / 1200 * 1000) or 0
        local canSupportByGrain = (s.grain > 0) and math.floor(s.grain / 1500 * 1000) or 0
        local canSupport = math.min(canSupportBySilver, canSupportByGrain)
        canSupport = math.max(0, canSupport)

        -- 逃亡人数
        local deserted = totalSoldiers - canSupport
        if deserted > 0 then
            -- 按比例从步兵和弓兵中扣除
            local ratio = canSupport / totalSoldiers
            s.army.infantry = math.floor((s.army.infantry or 0) * ratio)
            s.army.archers = math.floor((s.army.archers or 0) * ratio)

            -- 扣除实际能支付的费用
            local actualBatches = canSupport / 1000
            local actualSilver = math.min(s.silver, math.ceil(actualBatches * 1200))
            local actualGrain = math.min(s.grain, math.ceil(actualBatches * 1500))
            GameData.AddResource("silver", -actualSilver)
            GameData.AddResource("grain", -actualGrain)
            report.expenses.silver = report.expenses.silver + actualSilver
            report.expenses.grain = report.expenses.grain + actualGrain

            -- 取整到100
            deserted = math.floor(deserted / 100) * 100
            if deserted > 0 then
                report.events[#report.events + 1] = "军粮不足，" .. deserted .. "名士兵逃散"
                GameData.AddLog("军粮银两不足，" .. deserted .. "名士兵逃散。")
            end
        end
    end
end

-- ============================================================================
-- 税赋
-- ============================================================================

function MonthlyUpdate.ProcessTax(report)
    local s = GameData.state
    local region = GameData.GetRegion()
    local difficulty = GameData.GetDifficulty()

    -- 每季度收税
    if s.month % 3 == 0 then
        local taxRate = region.taxRate
        -- 根据年代调整税率
        taxRate = taxRate * EraSystem.GetTaxModifier(s.year)
        -- 难度影响税率
        taxRate = taxRate * difficulty.taxMul

        -- 官员减税：族中最高官职提供税赋减免
        local taxReduceMax = 0
        for _, m in ipairs(GameData.GetAliveMembers()) do
            if m.state == "为官" and m.officialRank then
                for _, rank in ipairs(MemberData.OFFICIAL_RANKS) do
                    if rank.id == m.officialRank and rank.taxReduce > taxReduceMax then
                        taxReduceMax = rank.taxReduce
                    end
                end
            end
        end
        if taxReduceMax > 0 then
            taxRate = taxRate * (1.0 - taxReduceMax)
        end

        local taxSilver = math.ceil(s.silver * taxRate * 0.3)
        local taxGrain = math.ceil(report.incomes.grain * taxRate)
        report.expenses.silver = report.expenses.silver + taxSilver
        report.expenses.grain = report.expenses.grain + taxGrain

        if taxSilver + taxGrain > 0 then
            report.events[#report.events + 1] = "官府征税：银两" .. taxSilver .. "、粮食" .. taxGrain
        end
    end
end

-- ============================================================================
-- 族人成长（委托给 GrowthSystem）
-- ============================================================================

function MonthlyUpdate.ProcessGrowth(report, ruleEffects)
    GrowthSystem.Process(report, ruleEffects)
end

-- ============================================================================
-- 职业进度（科举/从军）
-- ============================================================================

function MonthlyUpdate.ProcessCareer(report, ruleEffects)
    local s = GameData.state

    -- 文官身份月俸禄（声望/银两）
    local CIVIL_INCOME = {
        ["秀才"]  = { fame = 2, silver = 0 },
        ["举人"]  = { fame = 5, silver = 2 },
        ["进士"]  = { fame = 10, silver = 5 },
        ["监生"]  = { fame = 2, silver = 0 },
    }
    for _, m in ipairs(GameData.GetAliveMembers()) do
        local inc = CIVIL_INCOME[m.identity]
        if inc then
            if inc.fame > 0 then
                report.incomes.fame = report.incomes.fame + inc.fame
            end
            if inc.silver > 0 then
                report.incomes.silver = report.incomes.silver + inc.silver
            end
        end
    end

    -- 为官族人月处理（俸禄、声望、任期、贬谪）
    for _, m in ipairs(GameData.GetMembersByState("为官")) do
        if m.officialRank then
            local rankInfo = nil
            for _, r in ipairs(MemberData.OFFICIAL_RANKS) do
                if r.id == m.officialRank then rankInfo = r; break end
            end
            if rankInfo then
                -- 俸禄
                report.incomes.silver = report.incomes.silver + rankInfo.silver
                report.incomes.fame = report.incomes.fame + rankInfo.fame
                -- 布政使月产声望
                if rankInfo.famePerMonth then
                    report.incomes.fame = report.incomes.fame + rankInfo.famePerMonth
                end
                -- 任期累计
                m.officialTenure = (m.officialTenure or 0) + 1
                -- 贬谪风险（每月3%）
                if math.random() < (rankInfo.demotionRate or 0.03) then
                    m.state = "在家"
                    local oldRank = rankInfo.name
                    m.officialRank = nil
                    m.officialTenure = nil
                    report.events[#report.events + 1] = m.name .. "因朝廷变故，被免去" .. oldRank .. "之职。"
                    GameData.AddLog(m.name .. "被免去" .. oldRank .. "之职，返乡赋闲。")
                    report.incomes.fame = math.max(0, report.incomes.fame - 5)
                end
            end
        end
    end

    -- 从军收益
    for _, m in ipairs(GameData.GetMembersByState("从军")) do
        local rank = nil
        for _, r in ipairs(GameData.MILITARY_RANKS) do
            if r.name == (m.militaryRank or "士兵") then rank = r; break end
        end
        if not rank then rank = GameData.MILITARY_RANKS[1] end

        -- 军饷
        report.incomes.silver = report.incomes.silver + rank.silverPay

        -- 武艺增长（降低基础，应用递减）
        local martialGain = math.random(0, 2)  -- 原 1~3，降为 0~2
        martialGain = math.floor(martialGain * (1.0 + (ruleEffects.martialGrowthMul or 0)))
        martialGain = GrowthSystem.DiminishedGain(m.martial, martialGain)
        m.martial = math.min(100, m.martial + martialGain)

        -- 死亡风险
        local deathRate = rank.deathRate
        if m.talent and m.talent.id == "martial" then
            deathRate = deathRate * 0.8
        end
        -- 马场效果：从军族人存活率提升15%
        if report.industryEffects and report.industryEffects.horseRanch then
            deathRate = deathRate * 0.85
        end
        -- 根据年代调整死亡率
        deathRate = deathRate * EraSystem.GetDeathRateModifier(s.year)

        if math.random() < deathRate then
            EquipmentSystem.HandleInheritance(m)
            GameData.CheckPatriarchDeath(m)
            m.alive = false
            m.state = "阵亡"
            s.totalDeaths = s.totalDeaths + 1
            report.events[#report.events + 1] = m.name .. "在战场上英勇牺牲。"
            GameData.AddLog(m.name .. "阵亡沙场，忠魂永存。")
            GameData.NotifyDeath(m, "战死沙场", "马革裹尸，忠魂永存。族人声望因此提升。")
            report.incomes.fame = report.incomes.fame + 5
        elseif math.random() < 0.03 then
            -- 升职
            local nextRank = nil
            for i, r in ipairs(GameData.MILITARY_RANKS) do
                if r.name == rank.name and i < #GameData.MILITARY_RANKS then
                    nextRank = GameData.MILITARY_RANKS[i + 1]
                    break
                end
            end
            if nextRank then
                m.militaryRank = nextRank.name
                m.identity = nextRank.name
                report.events[#report.events + 1] = m.name .. "军功卓著，升任" .. nextRank.name .. "！"
                GameData.AddLog(m.name .. "升任" .. nextRank.name .. "。")
                report.incomes.fame = report.incomes.fame + nextRank.famePlus
            end
        end
    end
end

-- ============================================================================
-- 书院/武馆月度成长
-- ============================================================================

function MonthlyUpdate.ProcessAcademies(report, ruleEffects)
    local s = GameData.state
    if not s or not s.academies then return end

    for _, academy in ipairs(s.academies) do
        local aType = GameData.GetAcademyType(academy.typeId)
        if not aType then goto continueAcademy end

        -- 清理无效成员
        local validIds = {}
        for _, mid in ipairs(academy.memberIds) do
            local m = GameData.GetMember(mid)
            if m and m.alive then validIds[#validIds + 1] = mid end
        end
        academy.memberIds = validIds

        -- 对每个在席位上的族人施加额外成长
        for _, mid in ipairs(academy.memberIds) do
            local m = GameData.GetMember(mid)
            if m and m.alive then
                local attr = aType.attribute  -- "study" or "martial"
                local bonus = aType.baseBonus * academy.level  -- 等级越高加成越大
                local gain = math.random(2, 5)
                gain = math.floor(gain * (1.0 + bonus))
                -- 叠加族规/家训效果
                local mulKey = attr .. "GrowthMul"
                gain = math.floor(gain * (1.0 + (ruleEffects[mulKey] or 0)))
                -- 天赋加成
                if m.talent then
                    if attr == "study" and m.talent.id == "smart" then
                        gain = math.floor(gain * 1.3)
                    elseif attr == "martial" and m.talent.id == "martial" then
                        gain = math.floor(gain * 1.3)
                    end
                end
                gain = math.max(1, gain)
                m[attr] = math.min(100, (m[attr] or 0) + gain)
            end
        end

        ::continueAcademy::
    end
end

-- ============================================================================
-- 探索历练月度结算
-- ============================================================================

function MonthlyUpdate.ProcessExpeditions(report, ruleEffects)
    local s = GameData.state
    if not s or not s.expeditions then return end

    local completed = {}
    for i, exp in ipairs(s.expeditions) do
        exp.monthsLeft = exp.monthsLeft - 1
        if exp.monthsLeft <= 0 then
            completed[#completed + 1] = i
            local member = GameData.GetMember(exp.memberId)
            if not member or not member.alive then goto continueExp end

            local expType = nil
            for _, t in ipairs(GameData.EXPEDITION_TYPES) do
                if t.id == exp.typeId then expType = t; break end
            end
            if not expType then goto continueExp end

            -- 风险判定
            local riskRate = expType.riskRate
            -- 武艺高降低风险
            if member.martial >= 30 then riskRate = riskRate * 0.7 end
            if member.martial >= 60 then riskRate = riskRate * 0.5 end
            -- 家训军事加成
            riskRate = riskRate * (1.0 - (ruleEffects.militarySurvival or 0))

            if math.random() < riskRate then
                -- 遭遇风险（多样化失败文案）
                local damage = math.random(10, 30)
                member.health = math.max(10, member.health - damage)
                if member.state ~= "生病" then member.prevState = member.state end
                member.state = "生病"
                local failStories = {
                    market = { "%s在集市遭遇劫匪，财物尽失，负伤而归。", "%s赶集途中遇到流寇，拼死逃回。" },
                    trade_route = { "%s行商途中遭山贼伏击，险些丧命。", "%s远行路遇土匪截道，货物被劫，伤重归家。" },
                    study_trip = { "%s游学途中水土不服，大病一场。", "%s在外求学时染上风寒，不得不提前归来。" },
                    explore = { "%s在深山遭遇猛兽，九死一生逃出。", "%s探秘时失足坠崖，幸得山民相救。" },
                    recruit = { "%s访才途中遇骗子，银两被骗，还挨了打。", "%s在外招人不慎被盗贼盯上，受伤归来。" },
                }
                local fStories = failStories[exp.typeId]
                local fStory = fStories and fStories[math.random(1, #fStories)] or ("%s历练归来，" .. expType.riskDesc .. "，受伤归家。")
                local failText = string.format(fStory, member.name)
                report.events[#report.events + 1] = failText
                GameData.AddLog(failText)
            else
                -- 成功归来，发放奖励
                member.state = "在家"
                local rewardText = {}

                if expType.rewards.silver then
                    local amt = math.random(expType.rewards.silver[1], expType.rewards.silver[2])
                    amt = math.floor(amt * (1.0 + (ruleEffects.tradeIncomeMul or 0)))
                    GameData.AddResource("silver", amt)
                    rewardText[#rewardText + 1] = "银两+" .. amt
                end
                if expType.rewards.cloth then
                    local amt = math.random(expType.rewards.cloth[1], expType.rewards.cloth[2])
                    GameData.AddResource("cloth", amt)
                    rewardText[#rewardText + 1] = "布匹+" .. amt
                end
                if expType.rewards.fame then
                    local amt = math.random(expType.rewards.fame[1], expType.rewards.fame[2])
                    GameData.AddResource("fame", amt)
                    rewardText[#rewardText + 1] = "声望+" .. amt
                end
                if expType.rewards.study then
                    local amt = math.random(expType.rewards.study[1], expType.rewards.study[2])
                    amt = math.floor(amt * (1.0 + (ruleEffects.studyGrowthMul or 0)))
                    amt = GrowthSystem.DiminishedGain(member.study, amt)
                    member.study = math.min(100, member.study + amt)
                    rewardText[#rewardText + 1] = "学识+" .. amt
                end

                -- 特殊奖励：物品掉落
                if expType.rewards.item_chance and math.random() < expType.rewards.item_chance then
                    local itemPool = GameData.ITEM_TYPES
                    local item = itemPool[math.random(1, #itemPool)]
                    GameData.AddItem(item.id, 1)
                    rewardText[#rewardText + 1] = "获得" .. item.name
                end

                -- 特殊奖励：招募人员
                if expType.rewards.recruit_chance and math.random() < expType.rewards.recruit_chance then
                    local recruit = GameData.CreateMember({
                        gender = math.random() > 0.4 and "male" or "female",
                        age = 16 + math.random(0, 15),
                        generation = 1,
                        state = "在家",
                        health = 60 + math.random(0, 30),
                        study = 8 + math.random(2, 20),
                        martial = 8 + math.random(2, 20),
                    })
                    rewardText[#rewardText + 1] = "招募" .. recruit.name
                end

                local rewardStr = table.concat(rewardText, "、")
                -- 多样化成功结算叙事文案
                local successStories = {
                    market = {
                        "%s赶集归来，满载而归，在集市上左右逢源。",
                        "%s从集市带回了不少好货，邻里纷纷羡慕。",
                        "%s在集市上结识了不少商贾，买卖顺利。",
                    },
                    trade_route = {
                        "%s沿丝路远行归来，历经风霜终得厚报。",
                        "%s跋涉千里经商，途中见多识广，满载而归。",
                        "%s远行贸易归来，沿途与各地商号建立了联系。",
                    },
                    study_trip = {
                        "%s游学归来，拜访了数位名士，学问大进。",
                        "%s在外求学多时，得遇良师指点，受益匪浅。",
                        "%s游历各地书院，与同窗切磋学问，见识大增。",
                    },
                    explore = {
                        "%s深入山林探寻，在古洞中有所发现。",
                        "%s翻山越岭探秘归来，带回了山中奇物。",
                        "%s在深山中偶遇隐士指路，寻得宝物。",
                    },
                    recruit = {
                        "%s四处走访，终于找到了合适的人才。",
                        "%s以诚意打动了能人异士，请回宗族。",
                        "%s在茶馆中偶遇英才，一番交谈后收归门下。",
                    },
                }
                local stories = successStories[exp.typeId]
                local story = stories and stories[math.random(1, #stories)] or "%s历练归来。"
                local logText = string.format(story, member.name) .. "收获：" .. rewardStr .. "。"
                report.events[#report.events + 1] = logText
                GameData.AddLog(logText)
            end

            ::continueExp::
        end
    end

    -- 移除已完成的探索（从后往前删避免索引错乱）
    for i = #completed, 1, -1 do
        table.remove(s.expeditions, completed[i])
    end
end

-- ============================================================================
-- 族规每月固定消耗
-- ============================================================================

function MonthlyUpdate.ProcessClanRulesDrain(report, ruleEffects)
    local fameDrain = ruleEffects.fameDrain or 0
    if fameDrain < 0 then
        report.expenses.fame = report.expenses.fame + math.abs(fameDrain)
    end
end

-- ============================================================================
-- 族人际遇系统（读书/经商/从军族人每月有概率触发特殊际遇）
-- ============================================================================

function MonthlyUpdate.ProcessEncounters(report, ruleEffects)
    local s = GameData.state
    if not s then return end

    -- 读书族人际遇
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.state == "读书" and m.study >= 30 then
            local chance = 0.06
            if m.talent and m.talent.id == "smart" then chance = 0.10 end
            if math.random() < chance then
                local roll = math.random(1, 100)
                if roll <= 35 then
                    -- 悟性开窍：学识增长（降低并应用递减）
                    local gain = math.random(3, 8)  -- 原 8~18
                    gain = GrowthSystem.DiminishedGain(m.study, gain)
                    m.study = math.min(100, m.study + gain)
                    report.events[#report.events + 1] = m.name .. "读书忽有所悟，学识精进（+" .. gain .. "）。"
                    GameData.AddLog(m.name .. "苦读之中灵光一现，学识+" .. gain .. "。")
                elseif roll <= 60 then
                    -- 结识学友：声望+学识少量
                    local studyGain = math.random(2, 4)  -- 原 3~8
                    studyGain = GrowthSystem.DiminishedGain(m.study, studyGain)
                    m.study = math.min(100, m.study + studyGain)
                    GameData.AddResource("fame", 3)
                    report.events[#report.events + 1] = m.name .. "结识志同道合的学友，互相砥砺。"
                    GameData.AddLog(m.name .. "结识良友，学识+" .. studyGain .. "，声望+3。")
                elseif roll <= 80 then
                    -- 获赠典籍
                    GameData.AddItem("book", 1)
                    report.events[#report.events + 1] = m.name .. "获赠一部珍贵典籍。"
                    GameData.AddLog(m.name .. "获赠经史典籍一部。")
                else
                    -- 文名远播
                    local fameGain = math.random(5, 12)
                    GameData.AddResource("fame", fameGain)
                    report.events[#report.events + 1] = m.name .. "文章被传抄，文名远播（声望+" .. fameGain .. "）。"
                    GameData.AddLog(m.name .. "文名远播，声望+" .. fameGain .. "。")
                end
            end
        end
    end

    -- 经商族人际遇
    for _, m in ipairs(GameData.GetMerchantMembers()) do
        local chance = 0.07
        if m.talent and m.talent.id == "merchant" then chance = 0.12 end
        if math.random() < chance then
            local roll = math.random(1, 100)
            if roll <= 30 then
                -- 发现商机：额外银两
                local bonus = math.random(15, 40)
                bonus = math.floor(bonus * (1.0 + (ruleEffects.tradeIncomeMul or 0)))
                GameData.AddResource("silver", bonus)
                report.events[#report.events + 1] = m.name .. "发现一笔好买卖，净赚银两" .. bonus .. "！"
                GameData.AddLog(m.name .. "经商有道，额外获利银两" .. bonus .. "。")
            elseif roll <= 55 then
                -- 结识豪商：下月经商收入翻倍（简化为直接给银两）
                local bonus = math.random(10, 25)
                GameData.AddResource("silver", bonus)
                GameData.AddResource("fame", 2)
                report.events[#report.events + 1] = m.name .. "结识一位大商人，获得商业指点。"
                GameData.AddLog(m.name .. "结识豪商，获银两" .. bonus .. "、声望+2。")
            elseif roll <= 75 then
                -- 带回稀有物品
                local rareItems = { "jade", "seal", "heirloom" }
                local itemId = rareItems[math.random(1, #rareItems)]
                GameData.AddItem(itemId, 1)
                local itemType = GameData.GetItemType(itemId)
                report.events[#report.events + 1] = m.name .. "经商途中觅得" .. (itemType and itemType.name or "奇珍") .. "一件。"
                GameData.AddLog(m.name .. "带回" .. (itemType and itemType.name or "珍品") .. "。")
            else
                -- 开辟客源：声望增加
                local fameGain = math.random(4, 10)
                GameData.AddResource("fame", fameGain)
                report.events[#report.events + 1] = m.name .. "商誉日隆，声望+" .. fameGain .. "。"
                GameData.AddLog(m.name .. "经营有方，声望+" .. fameGain .. "。")
            end
        end
    end

    -- 从军族人际遇
    for _, m in ipairs(GameData.GetMembersByState("从军")) do
        local chance = 0.05
        if m.talent and m.talent.id == "martial" then chance = 0.09 end
        if math.random() < chance then
            local roll = math.random(1, 100)
            if roll <= 30 then
                -- 战场立功：武艺增长+声望（降低并应用递减）
                local martialGain = math.random(3, 8)  -- 原 8~15
                martialGain = GrowthSystem.DiminishedGain(m.martial, martialGain)
                m.martial = math.min(100, m.martial + martialGain)
                local fameGain = math.random(5, 12)
                GameData.AddResource("fame", fameGain)
                report.events[#report.events + 1] = m.name .. "在战场上奋勇杀敌，立下战功！"
                GameData.AddLog(m.name .. "沙场立功，武艺+" .. martialGain .. "，声望+" .. fameGain .. "。")
                s.totalMilitaryMerits = s.totalMilitaryMerits + 1
            elseif roll <= 55 then
                -- 缴获战利品
                local lootItems = { "weapon", "scroll", "herb" }
                local itemId = lootItems[math.random(1, #lootItems)]
                GameData.AddItem(itemId, 1)
                local itemType = GameData.GetItemType(itemId)
                report.events[#report.events + 1] = m.name .. "在战场缴获" .. (itemType and itemType.name or "物资") .. "一件。"
                GameData.AddLog(m.name .. "缴获" .. (itemType and itemType.name or "战利品") .. "。")
            elseif roll <= 75 then
                -- 获得赏银
                local silverBonus = math.random(10, 30)
                GameData.AddResource("silver", silverBonus)
                report.events[#report.events + 1] = m.name .. "获朝廷赏银" .. silverBonus .. "两。"
                GameData.AddLog(m.name .. "获赏银" .. silverBonus .. "两。")
            else
                -- 战友情谊：招募一名流民加入
                if math.random() < 0.5 then
                    local rAge = 18 + math.random(0, 10)
                    local recruit = GameData.CreateMember({
                        gender = "male",
                        age = rAge, generation = 1,
                        state = "在家",
                        health = 50 + math.random(0, 30),
                        study = 3 + math.random(2, math.min(10, math.floor(rAge * 0.4))),
                        martial = 15 + math.random(0, 20),
                    })
                    report.events[#report.events + 1] = m.name .. "带回一名退伍袍泽" .. recruit.name .. "加入宗族。"
                    GameData.AddLog(recruit.name .. "随" .. m.name .. "投奔宗族。")
                else
                    local fameGain = math.random(3, 8)
                    GameData.AddResource("fame", fameGain)
                    report.events[#report.events + 1] = m.name .. "在军中颇有威望，声望+" .. fameGain .. "。"
                    GameData.AddLog(m.name .. "军中威望传回乡里，声望+" .. fameGain .. "。")
                end
            end
        end
    end

    -- 历练中族人际遇（在外历练的族人偶有额外收获）
    for _, exp in ipairs(s.expeditions or {}) do
        local m = GameData.GetMember(exp.memberId)
        if m and m.alive and math.random() < 0.08 then
            local gain = math.random(1, 3)  -- 原 2~6
            local sGain = GrowthSystem.DiminishedGain(m.study, gain)
            local mGain = GrowthSystem.DiminishedGain(m.martial, gain)
            m.study = math.min(100, m.study + sGain)
            m.martial = math.min(100, m.martial + mGain)
            report.events[#report.events + 1] = m.name .. "历练途中增长了见识。"
        end
    end
end

-- ============================================================================
-- 历史事件检查
-- ============================================================================

function MonthlyUpdate.CheckHistoryEvent(report)
    local s = GameData.state
    for _, evt in ipairs(GameData.HISTORY_EVENTS) do
        if evt.year == s.year and not s.triggeredHistoryYears[evt.year] then
            s.triggeredHistoryYears[evt.year] = true
            report.events[#report.events + 1] = "【" .. evt.title .. "】" .. evt.desc
            GameData.AddLog("【历史大事】" .. evt.title .. "——" .. evt.desc)

            -- 应用效果
            local eff = evt.effect
            if eff == "era_start" then
                -- 开朝建业：声望+15，全体士气提升
                GameData.AddResource("fame", 15)
            elseif eff == "fame_boost" then
                -- 盛世文治：声望大幅提升
                GameData.AddResource("fame", math.random(10, 20))
            elseif eff == "tax_reform" then
                -- 税制改革：银两+20，粮食+15
                GameData.AddResource("silver", 20)
                GameData.AddResource("grain", 15)
            elseif eff == "tax_up" then
                -- 税率在税收计算中根据年份自动体现
            elseif eff == "tax_relief" then
                -- 轻徭薄赋：银两+30
                GameData.AddResource("silver", 30)
            elseif eff == "emperor_change" then
                -- 帝位更替：声望小幅波动
                GameData.AddResource("fame", math.random(-5, 5))
            elseif eff == "political_purge" then
                -- 政治清洗：有官员族人可能被牵连
                for _, m in ipairs(GameData.GetAliveMembers()) do
                    if m.identity == "官员" and math.random() < 0.2 then
                        m.state = "在家"; m.identity = "族人"
                        GameData.AddLog(m.name .. "受朝廷大案牵连，被罢官归乡。")
                    end
                end
                GameData.AddResource("fame", -math.random(5, 10))
            elseif eff == "political_chaos" then
                -- 朝政混乱：声望-10，银两-15
                GameData.AddResource("fame", -10)
                GameData.AddResource("silver", -15)
            elseif eff == "trade_boost" then
                -- 贸易繁荣：经商族人收益翻倍（本月银两+40）
                GameData.AddResource("silver", 40)
            elseif eff == "trade_decline" then
                -- 贸易衰退：银两-25
                GameData.AddResource("silver", -25)
            elseif eff == "prosperity" then
                -- 太平盛世：全面受益
                GameData.AddResource("silver", 20)
                GameData.AddResource("grain", 20)
                GameData.AddResource("fame", 8)
            elseif eff == "famine" or eff == "great_famine" then
                local loss = eff == "great_famine" and 0.4 or 0.25
                GameData.AddResource("grain", -math.ceil(s.grain * loss))
            elseif eff == "plague" then
                for _, m in ipairs(GameData.GetAliveMembers()) do
                    if math.random() < 0.15 then
                        m.health = math.max(10, m.health - math.random(20, 40))
                        if m.state ~= "生病" then m.prevState = m.state end
                        m.state = "生病"
                    end
                end
            elseif eff == "war_civil" then
                -- 内战：银两粮食均大量损失
                GameData.AddResource("silver", -math.ceil(s.silver * 0.15))
                GameData.AddResource("grain", -math.ceil(s.grain * 0.15))
            elseif eff == "war_defense" then
                -- 边防战争：从军族人可能伤亡，声望+5
                for _, m in ipairs(GameData.GetAliveMembers()) do
                    if (m.state == "从军" or m.state == "出征") and math.random() < 0.1 then
                        m.health = math.max(5, m.health - math.random(15, 30))
                    end
                end
                GameData.AddResource("fame", 5)
            elseif eff == "war_escalate" then
                -- 战事升级：粮食大量消耗，从军者伤亡加剧
                GameData.AddResource("grain", -math.ceil(s.grain * 0.2))
                for _, m in ipairs(GameData.GetAliveMembers()) do
                    if (m.state == "从军" or m.state == "出征") and math.random() < 0.2 then
                        m.health = math.max(5, m.health - math.random(20, 40))
                    end
                end
            elseif eff == "south_war" then
                -- 南方战事：银两-30，粮食-20
                GameData.AddResource("silver", -30)
                GameData.AddResource("grain", -20)
            elseif eff == "military_draft" then
                -- 征兵令：适龄男子可能被征召
                local drafted = 0
                for _, m in ipairs(GameData.GetAliveMembers()) do
                    if m.gender == "male" and m.age >= 16 and m.age <= 40 and m.state == "在家" and drafted < 2 and math.random() < 0.3 then
                        m.state = "从军"; m.identity = "士兵"; m.militaryRank = "士兵"
                        GameData.AddLog(m.name .. "被朝廷征召入伍。")
                        drafted = drafted + 1
                    end
                end
            elseif eff == "military_drain" then
                -- 军费消耗：银两-20，粮食-15
                GameData.AddResource("silver", -20)
                GameData.AddResource("grain", -15)
            elseif eff == "military_crisis" then
                -- 军事危机：银两-15，声望-5
                GameData.AddResource("silver", -15)
                GameData.AddResource("fame", -5)
            elseif eff == "military_victory" then
                -- 军事大捷：声望+15，从军族人军功+1
                GameData.AddResource("fame", 15)
                for _, m in ipairs(GameData.GetAliveMembers()) do
                    if m.state == "从军" or m.state == "出征" then
                        local mg = GrowthSystem.DiminishedGain(m.martial, math.random(1, 3))  -- 原 2~5
                        m.martial = math.min(100, m.martial + mg)
                    end
                end
            elseif eff == "capital_move" then
                -- 迁都：声望波动，银两-10
                GameData.AddResource("silver", -10)
                GameData.AddResource("fame", math.random(-3, 3))
            elseif eff == "rebellion" then
                -- 叛乱：安全威胁，银两粮食损失
                GameData.AddResource("silver", -math.random(10, 25))
                GameData.AddResource("grain", -math.random(10, 20))
            elseif eff == "refugees" then
                -- 流民涌入：粮食消耗增加，可能新增族人
                GameData.AddResource("grain", -math.random(15, 25))
                if math.random() < 0.3 then
                    local gender = math.random() > 0.5 and "male" or "female"
                    local surnames = GameData.SURNAMES
                    local surname = surnames[math.random(#surnames)]
                    local rAge = math.random(15, 35)
                    GameData.CreateMember({
                        name = surname .. GameData.RandomGivenName(gender),
                        gender = gender, age = rAge, generation = 1,
                        state = "在家", health = 40 + math.random(0, 20),
                        study = 5 + math.random(2, math.min(15, math.floor(rAge * 0.5))),
                        martial = 5 + math.random(2, math.min(12, math.floor(rAge * 0.4))),
                    })
                    GameData.AddLog("收留了一名流民入族。")
                end
            elseif eff == "governance_decline" then
                -- 吏治败坏：银两-10，声望-8
                GameData.AddResource("silver", -10)
                GameData.AddResource("fame", -8)
            elseif eff == "bandit_up" or eff == "bandit_surge" then
                -- 匪患：银两和粮食被劫（寨堡减免 + 族规"习武自卫"减免）
                local mult = eff == "bandit_surge" and 0.12 or 0.08
                local fortReduction = math.min(s.fortCount * 0.1, 0.5)
                local ruleEffects = GameData.GetClanRuleEffects()
                local ruleReduction = ruleEffects.banditResist or 0
                mult = mult * (1 - fortReduction) * (1 - ruleReduction)
                GameData.AddResource("silver", -math.ceil(s.silver * mult))
                GameData.AddResource("grain", -math.ceil(s.grain * mult))
            elseif eff == "border_threat" then
                -- 边疆告急：声望-5，粮食-15
                GameData.AddResource("fame", -5)
                GameData.AddResource("grain", -15)
            elseif eff == "border_fall" then
                -- 边防崩溃：银两-25，粮食-20，声望-10
                GameData.AddResource("silver", -25)
                GameData.AddResource("grain", -20)
                GameData.AddResource("fame", -10)
            elseif eff == "game_end" then
                -- 甲申之变，游戏终局触发
                s.gameEnded = true
                s.pendingEvents[#s.pendingEvents + 1] = {
                    title = "甲申之变",
                    desc = "李自成攻入北京，崇祯帝自缢煤山，大明覆亡！\n你的宗族该何去何从？",
                    choices = {
                        { text = "南渡投奔南明", effect = function() GameData.AddLog("宗族南渡，投奔南明，艰难延续。"); s.endingChoice = "南渡" end },
                        { text = "剃发降清", effect = function() GameData.AddLog("宗族降清，虽保全性命，族望尽失。"); GameData.AddResource("fame", -30); s.endingChoice = "降清" end },
                        { text = "死节殉国", effect = function() GameData.AddLog("宗族死节殉国，忠义千秋！"); GameData.AddResource("fame", 50); s.endingChoice = "殉国" end },
                    },
                    isEnding = true,
                }
            end

            -- 添加到待处理事件展示
            if evt.effect ~= "game_end" then
                s.pendingEvents[#s.pendingEvents + 1] = {
                    title = "【" .. evt.title .. "】",
                    desc = evt.desc,
                    choices = {
                        { text = "朕知道了", effect = function() end },
                    },
                }
            end
            break  -- 每年只触发一个历史事件
        end
    end
end

-- ============================================================================
-- 年终总结（在年份递增之前调用）
-- ============================================================================

function MonthlyUpdate.BuildYearEndSummary(report, s)
    local ys = s.yearStats
    local snap = s.yearStartSnapshot
    if not ys or not snap then return end

    local aliveNow = #GameData.GetAliveMembers()
    local yearLabel = EraSystem.GetYearLabel(s.year)

    -- 通过全局计数器差值得到年度出生/死亡/科举/军功
    local yearBirths = s.totalBirths - (snap.totalBirths or s.totalBirths)
    local yearDeaths = s.totalDeaths - (snap.totalDeaths or s.totalDeaths)
    local yearExams = s.totalExamPasses - (snap.totalExamPasses or s.totalExamPasses)
    local yearMerits = s.totalMilitaryMerits - (snap.totalMilitaryMerits or s.totalMilitaryMerits)
    local popChange = aliveNow - snap.aliveCount
    local industryChange = #s.industries - snap.industryCount
    local fortChange = s.fortCount - snap.fortCount

    -- 年度净收支
    local netSilver = ys.incomes.silver - ys.expenses.silver
    local netGrain = ys.incomes.grain - ys.expenses.grain
    local netCloth = ys.incomes.cloth - ys.expenses.cloth
    local netFame = ys.incomes.fame - ys.expenses.fame

    -- 评语
    local rating, ratingIcon
    local score = 0
    if netSilver > 0 then score = score + 1 end
    if netGrain > 0 then score = score + 1 end
    if yearBirths > yearDeaths then score = score + 1 end
    if yearExams > 0 then score = score + 1 end
    if netFame > 0 then score = score + 1 end
    if industryChange > 0 then score = score + 1 end

    if score >= 5 then
        rating = "鼎盛之年"
        ratingIcon = "优"
    elseif score >= 3 then
        rating = "蒸蒸日上"
        ratingIcon = "升"
    elseif score >= 2 then
        rating = "平稳度日"
        ratingIcon = "平"
    elseif score >= 1 then
        rating = "勉力维持"
        ratingIcon = "降"
    else
        rating = "风雨飘摇"
        ratingIcon = "危"
    end

    -- 组装总结数据
    report.yearEndSummary = {
        yearLabel = yearLabel,
        rating = rating,
        ratingIcon = ratingIcon,
        -- 人口
        aliveCount = aliveNow,
        popChange = popChange,
        births = yearBirths,
        deaths = yearDeaths,
        -- 经济（年度收支）
        incomes = ys.incomes,
        expenses = ys.expenses,
        netSilver = netSilver,
        netGrain = netGrain,
        netCloth = netCloth,
        netFame = netFame,
        -- 经济（当前存量）
        currentSilver = s.silver,
        currentGrain = s.grain,
        currentCloth = s.cloth,
        currentFame = s.fame,
        -- 发展
        industryCount = #s.industries,
        industryChange = industryChange,
        fortCount = s.fortCount,
        fortChange = fortChange,
        -- 功业
        examPasses = yearExams,
        militaryMerits = yearMerits,
        -- 品级
        clanRank = GameData.GetClanRankName(),
        rankChanged = s.clanRank ~= snap.clanRank,
        oldRank = snap.clanRank,
    }
end

-- ============================================================================
-- 保底婚姻提醒：确保每代至少一人结婚，防止家族断绝
-- ============================================================================
-- 逻辑：
--   1. 第一代不触发（玩家刚开局，不催婚）
--   2. 第二代起，如果该代所有人都 ≥ 22 岁且无人结婚 → 弹窗提醒
--   3. 玩家可选择"前去说亲"（打开正常配偶选择界面）或"再等等"
--   4. 拒绝后下个月继续催，直到该代有人结婚

function MonthlyUpdate.CheckGuaranteedMarriage(report)
    local s = GameData.state
    if not s then return end

    local members = GameData.GetAliveMembers()
    if #members == 0 then return end

    -- 找出最高世代
    local maxGen = 0
    for _, m in ipairs(members) do
        if m.generation > maxGen then maxGen = m.generation end
    end

    -- 第一代不触发保底催婚
    if maxGen <= 1 then return end

    -- 本月已弹过催婚窗则跳过（每月初重置）
    if s._marriageGuaranteedGen and s._marriageGuaranteedGen >= maxGen then return end

    -- 收集该代成员信息
    local genMembers = {}
    local hasMarried = false
    local allAbove18 = true
    for _, m in ipairs(members) do
        if m.generation == maxGen then
            genMembers[#genMembers + 1] = m
            if m.spouseId then hasMarried = true end
            if m.age < 18 then allAbove18 = false end
        end
    end

    -- 已有人结婚或还有人不到18岁，不需要催
    if hasMarried or not allAbove18 then return end
    if #genMembers == 0 then return end

    -- 筛选适龄未婚候选人（16-40岁）
    local candidates = {}
    for _, m in ipairs(genMembers) do
        if not m.spouseId and m.age >= 16 and m.age <= 40 then
            candidates[#candidates + 1] = m
        end
    end
    if #candidates == 0 then return end

    local person = candidates[math.random(1, #candidates)]

    if #s.pendingEvents < 5 then
        s.pendingEvents[#s.pendingEvents + 1] = {
            title = "族中长辈催婚",
            desc = person.name .. "已年满" .. person.age .. "岁，至今尚未婚配。族中长辈忧心忡忡，恐香火断绝。\n\n是否为其安排亲事？",
            choices = {
                { text = "前去说亲", effect = function()
                    if person.spouseId then return end
                    -- 打开正常的配偶选择界面，让玩家自己选
                    local GS = require("UI.GameScreen")
                    GS.ShowMarriageTierSelect(person)
                end },
                { text = "再等等", effect = function()
                    GameData.AddLog("族中长辈催促" .. person.name .. "婚事，暂被搁置。")
                end },
            },
        }
    end

    -- 标记本月已弹窗，防止同月重复触发
    s._marriageGuaranteedGen = maxGen

    report.events[#report.events + 1] = "族中长辈催促" .. person.name .. "的婚事。"
end

-- ============================================================================
-- 宠物寿命检查（狗寿命到期则死亡，触发事件弹窗）
-- ============================================================================

function MonthlyUpdate.ProcessPetAging(report)
    local s = GameData.state
    if not s or not s.pet or not s.pet.alive then return end

    local pet = s.pet
    -- 计算养了多少年（按月精确）
    local ageMonths = (s.year - pet.adoptYear) * 12 + (s.month - pet.adoptMonth)
    local ageYears = ageMonths / 12

    if ageYears >= pet.lifespan then
        -- 狗去世
        pet.alive = false
        pet.deathYear = s.year
        pet.deathMonth = s.month
        GameData.AddLog("家犬「" .. pet.name .. "」年老体衰，安详离世。陪伴了家族" .. math.floor(ageYears) .. "年。")
        report.events[#report.events + 1] = "家犬「" .. pet.name .. "」离世"

        -- 弹出事件通知
        s.pendingEvents[#s.pendingEvents + 1] = {
            title = "爱犬离世",
            desc = "家犬「" .. pet.name .. "」已陪伴族人" .. math.floor(ageYears) .. "年，如今年老体衰，安详地趴在院中走完了最后一程。\n\n" ..
                   "全家大小都很伤心，孩子们围在它身旁久久不愿离去。",
            choices = {
                { text = "好好安葬它", effect = function()
                    GameData.AddResource("fame", 1)
                end },
            },
        }
    end
end

-- ============================================================================
-- 坐堂郎中自动治疗（每季度检查，年上限3人）
-- 条件：已分配女性族人（学识>=60）为坐堂郎中
-- 效果：自动治疗健康<30的族人，恢复20~35健康
-- ============================================================================

function MonthlyUpdate.ProcessClinicDoctor(report)
    local s = GameData.state
    if not s then return end

    -- 每季度执行一次（3/6/9/12月）
    if s.month % 3 ~= 0 then return end

    local doctorId = s.clinicDoctorId
    if not doctorId then return end

    local doctor = GameData.GetMember(doctorId)
    -- 验证郎中仍然有效
    if not doctor or not doctor.alive or doctor.gender ~= "female" or (doctor.study or 0) < 60 then
        s.clinicDoctorId = nil
        return
    end

    -- 检查年度上限（看广告可提升至6次）
    local AdSystem = require("Systems.AdSystem")
    local yearlyLimit = AdSystem.GetClinicYearlyLimit()
    local healsThisYear = s._clinicHealsThisYear or 0
    if healsThisYear >= yearlyLimit then return end

    -- 找到健康<30的族人（排除郎中本人）
    local patients = {}
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.id ~= doctorId and m.health < 30 then
            patients[#patients + 1] = m
        end
    end

    if #patients == 0 then return end

    -- 按健康值升序排列，优先治疗最虚弱的
    table.sort(patients, function(a, b) return a.health < b.health end)

    -- 本次最多治疗1人（每季度1人，年上限3人）
    local patient = patients[1]
    local healAmount = math.random(20, 35)
    -- 郎中学识越高治疗效果越好
    if doctor.study >= 80 then healAmount = healAmount + math.random(5, 10) end

    local oldHealth = patient.health
    patient.health = math.min(100, patient.health + healAmount)
    local actualHeal = patient.health - oldHealth

    -- 如果治好了，解除生病状态
    if patient.state == "生病" and patient.health >= 60 then
        patient.state = patient.prevState or "在家"
        patient.prevState = nil
    end

    s._clinicHealsThisYear = healsThisYear + 1

    local logMsg = "坐堂郎中" .. doctor.name .. "为" .. patient.name .. "把脉施药，健康恢复+" .. actualHeal
    GameData.AddLog(logMsg)
    report.events[#report.events + 1] = logMsg
end

return MonthlyUpdate
