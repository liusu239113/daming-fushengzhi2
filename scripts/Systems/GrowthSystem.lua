-- ============================================================================
-- 大明浮生志2 - 族人成长系统（从 MonthlyUpdate.lua 拆分）
-- 包含：年龄增长、生老病死、读书武艺成长、生育系统
-- ============================================================================

local GameData = require("Data.GameData")
local EquipmentSystem = require("Systems.EquipmentSystem")
local SkillSystem = require("Systems.SkillSystem")

local GrowthSystem = {}

-- ============================================================================
-- 阶级属性上限：不同阶级能达到的属性天花板
-- 寒门读书人不可能学识100，只有世家勋贵才有顶尖资源和名师
-- ============================================================================
local RANK_ATTR_CAP = {
    [1] = 35,   -- 寒门：乡村私塾水平，最多35
    [2] = 50,   -- 农户：有些积蓄请先生，最多50
    [3] = 65,   -- 乡绅：有书房藏书，最多65
    [4] = 80,   -- 望族：名师教导，最多80
    [5] = 92,   -- 世家：家学渊源，最多92
    [6] = 100,  -- 勋贵：顶级资源，可达满值
}

-- ============================================================================
-- 属性递减增长：属性越高，增长越困难（模拟现实中的瓶颈）
-- 0-20:  全额增长（启蒙阶段）
-- 20-35: 增长×0.6（需要用功）
-- 35-50: 增长×0.35（需要专注训练）
-- 50-65: 增长×0.2（资质瓶颈）
-- 65-80: 增长×0.1（大师级别，极慢）
-- 80+:   增长×0.05（登峰造极，几乎停滞）
-- ============================================================================
function GrowthSystem.DiminishedGain(currentValue, rawGain)
    if rawGain <= 0 then return 0 end
    local factor = 1.0
    if currentValue >= 80 then
        factor = 0.05
    elseif currentValue >= 65 then
        factor = 0.1
    elseif currentValue >= 50 then
        factor = 0.2
    elseif currentValue >= 35 then
        factor = 0.35
    elseif currentValue >= 20 then
        factor = 0.6
    end
    -- 使用概率保底：即使系数很低，也有机会+1
    local scaled = rawGain * factor
    local base = math.floor(scaled)
    local frac = scaled - base
    if frac > 0 and math.random() < frac then
        base = base + 1
    end
    return math.max(0, base)
end

--- 获取当前阶级的属性上限
---@param clanRank number 当前阶级(1-6)
---@return number cap 属性上限
function GrowthSystem.GetRankCap(clanRank)
    return RANK_ATTR_CAP[clanRank] or 35
end

-- ============================================================================
-- 族人成长（增强版：多月生育、婴儿夭折、双胞胎）
-- ============================================================================

---@param report table 月度报告
---@param ruleEffects table 族规效果
function GrowthSystem.Process(report, ruleEffects)
    local s = GameData.state

    for _, m in ipairs(GameData.GetAliveMembers()) do
        -- 每年长一岁（在1月结算）
        if s.month == 1 then
            m.age = m.age + 1

            -- 自然成长：随年龄自动获得少量属性（受阶级上限约束）
            local rankCap = GrowthSystem.GetRankCap(s.clanRank)
            if m.age <= 6 then
                -- 幼儿期：每年 5% 概率+1
                if math.random() < 0.05 then m.study = math.min(rankCap, m.study + 1) end
                if math.random() < 0.05 then m.martial = math.min(rankCap, m.martial + 1) end
            elseif m.age <= 12 then
                -- 少年期：每年 8% 概率+1
                if math.random() < 0.08 then m.study = math.min(rankCap, m.study + 1) end
                if math.random() < 0.08 then m.martial = math.min(rankCap, m.martial + 1) end
            elseif m.age <= 18 then
                -- 青年期：每年 10% 概率+1
                if math.random() < 0.10 then m.study = math.min(rankCap, m.study + 1) end
                if math.random() < 0.10 then m.martial = math.min(rankCap, m.martial + 1) end
            elseif m.age <= 30 then
                -- 壮年期：每年 5% 概率+1
                if math.random() < 0.05 then m.study = math.min(rankCap, m.study + 1) end
                if math.random() < 0.05 then m.martial = math.min(rankCap, m.martial + 1) end
            end
            -- 30岁以上不再自然成长，只能通过读书/习武/培养提升
        end

        -- 老年人寿终概率
        if m.age >= 60 then
            local deathRate = (m.age - 55) * 0.005
            if m.health < 30 then deathRate = deathRate * 2 end
            if math.random() < deathRate then
                EquipmentSystem.HandleInheritance(m)
                GameData.CheckPatriarchDeath(m)
                m.alive = false
                m.state = "亡故"
                s.totalDeaths = s.totalDeaths + 1
                report.events[#report.events + 1] = m.name .. "寿终正寝，享年" .. m.age .. "岁。"
                GameData.AddLog(m.name .. "寿终正寝，享年" .. m.age .. "岁。")
                GameData.NotifyDeath(m, "寿终正寝", "一生操劳，终归尘土，愿其安息。")
                report.incomes.fame = report.incomes.fame + 1
            end
        end

        -- 婴儿夭折（0-2岁）
        if m.alive and m.age <= 2 then
            local infantDeathRate = 0.008
            if m.health < 50 then infantDeathRate = 0.015 end
            if math.random() < infantDeathRate then
                EquipmentSystem.HandleInheritance(m)
                -- 婴儿不可能是族长，无需检查继承
                m.alive = false
                m.state = "夭折"
                s.totalDeaths = s.totalDeaths + 1
                report.events[#report.events + 1] = m.name .. "不幸夭折，年仅" .. m.age .. "岁。"
                GameData.AddLog(m.name .. "不幸夭折，令人痛惜。")
                GameData.NotifyDeath(m, "不幸夭折", "天妒英才，幼子早逝，令人痛惜。")
            end
        end

        -- 生病概率
        if m.alive and m.age > 2 then
            local sickRate = 0.02
            if m.talent and m.talent.id == "weak" then sickRate = 0.05 end
            -- 老年人更容易生病
            if m.age >= 50 then sickRate = sickRate + 0.02 end
            -- 新手保护：前3年（36个月）内生病概率减半
            if s.totalMonths < 36 then sickRate = sickRate * 0.5 end
            -- 独苗保护：仅剩1人时生病概率大幅降低
            if #GameData.GetAliveMembers() <= 1 then sickRate = sickRate * 0.3 end
            -- 药铺效果：全族生病概率-15%
            if report.industryEffects and report.industryEffects.reduceSick then
                sickRate = sickRate * 0.85
            end
            if math.random() < sickRate then
                m.health = math.max(0, m.health - math.random(3, 10))
                if m.state ~= "从军" and m.state ~= "出征" and m.state ~= "赶考" then
                    if m.state ~= "生病" then m.prevState = m.state end
                    m.state = "生病"
                end
                report.events[#report.events + 1] = m.name .. "染病，健康下降。"
            end
        end

        -- 病人恢复
        if m.alive and m.state == "生病" then
            m.health = math.min(100, m.health + math.random(5, 12))
            if m.health >= 60 then
                m.state = m.prevState or "在家"
                m.prevState = nil
            end
            -- 病重者可能病死（独苗保护：仅剩1人时概率大幅降低）
            local deathChance = 0.08
            if #GameData.GetAliveMembers() <= 1 then deathChance = 0.03 end
            if m.health <= 10 and math.random() < deathChance then
                EquipmentSystem.HandleInheritance(m)
                GameData.CheckPatriarchDeath(m)
                m.alive = false
                m.state = "病逝"
                s.totalDeaths = s.totalDeaths + 1
                report.events[#report.events + 1] = m.name .. "久病不愈，抱憾离世。"
                GameData.AddLog(m.name .. "病逝，享年" .. m.age .. "岁。")
                GameData.NotifyDeath(m, "久病不愈，抱憾离世", "药石无灵，终究回天乏术。")
            end
        end

        -- 读书增长学识（受族规影响）— 受阶级上限约束
        if m.alive and m.state == "读书" and m.age >= 6 then
            local rankCap = GrowthSystem.GetRankCap(s.clanRank)
            -- 已到阶级上限则不再增长
            if m.study < rankCap then
                -- 概率制：每月约10%基础概率+1（大幅降速）
                local studyGain = (math.random() < 0.10) and 1 or 0
                if m.talent and m.talent.id == "smart" then
                    -- 聪慧天赋：额外10%概率+1
                    if math.random() < 0.10 then studyGain = studyGain + 1 end
                end
                -- 族规：耕读传家
                studyGain = math.floor(studyGain * (1.0 + (ruleEffects.studyGrowthMul or 0)))
                -- 有族中秀才/举人当先生加成：额外10%概率+1
                local hasTeacher = false
                for _, t in ipairs(GameData.GetAliveMembers()) do
                    if t.id ~= m.id and (t.identity == "秀才" or t.identity == "举人" or t.identity == "进士" or t.identity == "监生") then
                        hasTeacher = true
                        break
                    end
                end
                if hasTeacher and math.random() < 0.10 then studyGain = studyGain + 1 end
                -- 递减增长：属性越高涨得越慢
                studyGain = GrowthSystem.DiminishedGain(m.study, studyGain)
                -- 接近阶级上限时额外衰减（上限前5点内50%概率不涨）
                if m.study + studyGain > rankCap - 5 and math.random() < 0.5 then
                    studyGain = math.max(0, studyGain - 1)
                end
                m.study = math.min(rankCap, m.study + studyGain)
            end
        end

        -- 在家族人武艺自然成长（族规：习武自卫 + 铁匠铺加成）— 受阶级上限约束
        if m.alive and m.age >= 10 and m.gender == "male" then
            local rankCap = GrowthSystem.GetRankCap(s.clanRank)
            if m.martial < rankCap then
                local martialMul = ruleEffects.martialGrowthMul or 0
                -- 铁匠铺：每座+10%武艺成长
                if report.industryEffects and report.industryEffects.smithyBonus > 0 then
                    martialMul = martialMul + report.industryEffects.smithyBonus * 0.10
                end
                if martialMul > 0 then
                    -- 概率制：每月约10%概率+1（大幅降速）
                    local martialGain = (math.random() < 0.10) and 1 or 0
                    martialGain = math.floor(martialGain * (1.0 + martialMul))
                    martialGain = GrowthSystem.DiminishedGain(m.martial, martialGain)
                    m.martial = math.min(rankCap, m.martial + martialGain)
                end
            end
        end

        -- 书坊效果：读书族人额外学识成长 — 受阶级上限约束
        if m.alive and m.state == "读书" and report.industryEffects
            and report.industryEffects.bookshopBonus > 0 then
            local rankCap = GrowthSystem.GetRankCap(s.clanRank)
            if m.study < rankCap then
                local extraStudy = ((math.random() < 0.10) and 1 or 0) * report.industryEffects.bookshopBonus
                if extraStudy > 0 then
                    extraStudy = GrowthSystem.DiminishedGain(m.study, extraStudy)
                    m.study = math.min(rankCap, m.study + extraStudy)
                end
            end
        end

        -- 生育系统（增强版：每对夫妻最多3个后代，5%不育概率）
        if m.alive and m.gender == "female" and m.age >= 18 and m.age <= 40
            and m.spouseId and m.state ~= "生病" then
            local spouse = GameData.GetMember(m.spouseId)
            if spouse and spouse.alive and spouse.state ~= "从军" and spouse.state ~= "出征" then
                -- 硬上限：每对夫妻最多3个孩子
                local childCount = #spouse.childrenIds
                if childCount >= 3 then goto skipBirth end

                -- 不育检测：5%概率这对夫妻终生不育
                -- 用夫妻双方id的组合做种子，确保同一对夫妻每次判定结果一致
                if not spouse._fertilityChecked then
                    spouse._fertilityChecked = true
                    local seed = (spouse.id * 31 + m.id) % 100
                    if seed < 5 then  -- 5%不育
                        spouse._infertile = true
                        GameData.AddLog(spouse.name .. "与" .. m.name .. "婚后未有子嗣之缘。")
                    end
                end
                if spouse._infertile then goto skipBirth end

                -- 基础生育率每月 8%
                local fertilityChance = 0.08

                -- 天赋加成
                if m.talent and m.talent.id == "fertile" then
                    fertilityChance = fertilityChance + 0.15
                end
                if spouse.talent and spouse.talent.id == "fertile" then
                    fertilityChance = fertilityChance + 0.10
                end

                -- 已有子女越多，生育意愿越低
                if childCount >= 1 then fertilityChance = fertilityChance - childCount * 0.03 end
                fertilityChance = math.max(0.02, fertilityChance)

                -- 年龄影响
                if m.age >= 35 then fertilityChance = fertilityChance * 0.6 end

                if math.random() < fertilityChance then
                    -- 双胞胎：只有在已有0-1个孩子时才可能（确保不超过3个）
                    local twinChance = 0.0
                    if childCount <= 1 then
                        twinChance = 0.05
                        if m.talent and m.talent.id == "fertile" then twinChance = 0.10 end
                    end
                    local birthCount = math.random() < twinChance and 2 or 1
                    -- 确保不超过3个
                    if childCount + birthCount > 3 then birthCount = 3 - childCount end

                    -- 计算父母技能对后代的属性加成
                    local childBonus = SkillSystem.CalcChildBonus(spouse, m)

                    for b = 1, birthCount do
                        local child = GameData.CreateMember({
                            gender = math.random() > 0.45 and "male" or "female",
                            age = 0,
                            generation = math.max(m.generation, spouse.generation) + 1,
                            parentId = spouse.id,
                            state = "在家",
                            health = 60 + math.random(0, 40),
                        })
                        -- 应用父母技能传承加成
                        if childBonus.study > 0 then
                            child.study = math.min(100, child.study + childBonus.study)
                        end
                        if childBonus.martial > 0 then
                            child.martial = math.min(100, child.martial + childBonus.martial)
                        end
                        spouse.childrenIds[#spouse.childrenIds + 1] = child.id
                        s.totalBirths = s.totalBirths + 1

                        if birthCount == 2 and b == 1 then
                            report.events[#report.events + 1] = m.name .. "喜得双胎！"
                            GameData.AddLog(m.name .. "诞下双胎，大喜之事！")
                        end
                        if birthCount == 1 or b == birthCount then
                            local genderText = child.gender == "male" and "一子" or "一女"
                            report.events[#report.events + 1] = m.name .. "诞下" .. genderText .. "，取名" .. child.name .. "。"
                            if birthCount == 1 then
                                GameData.AddLog(m.name .. "诞下" .. genderText .. child.name .. "。")
                            end
                        end
                    end
                    report.incomes.fame = report.incomes.fame + birthCount
                end
            end
            ::skipBirth::
        end
    end
end

return GrowthSystem
