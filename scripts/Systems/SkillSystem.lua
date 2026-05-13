-- ============================================================================
-- 大明浮生志2 - 技能专精系统
-- 管理技能路径解锁、效果应用、培养训练
-- ============================================================================

local GameData = require("Data.GameData")

local SkillSystem = {}

-- ============================================================================
-- 技能路径操作
-- ============================================================================

--- 解锁技能路径
---@param memberId number
---@param pathId string "scholar"|"warrior"|"merchant"
---@return boolean success
---@return string message
function SkillSystem.UnlockPath(memberId, pathId)
    local member = GameData.GetMember(memberId)
    if not member or not member.alive then return false, "族人不存在或已故" end

    -- 已有路径不能重复解锁
    if member.skillPath then
        local current = GameData.GetActiveSkillPath(member)
        local name = current and current.name or member.skillPath
        return false, "已激活【" .. name .. "】路径，无法更改"
    end

    -- 年龄限制
    if member.age < 18 then
        return false, "需年满18岁方可专精"
    end

    -- 检查条件
    local canUnlock, reason = GameData.CanUnlockSkillPath(member, pathId)
    if not canUnlock then
        return false, reason
    end

    -- 解锁
    member.skillPath = pathId

    local path = GameData.GetActiveSkillPath(member)
    local pathName = path and path.name or pathId
    GameData.AddLog(member.name .. "专精为【" .. pathName .. "】")

    return true, "已成为" .. pathName
end

--- 获取族人技能路径提供的被动效果表
---@param member table
---@return table effects {key=value, ...}
function SkillSystem.GetEffects(member)
    local path = GameData.GetActiveSkillPath(member)
    if not path then return {} end
    return path.effects or {}
end

--- 检查族人是否有某个技能效果
---@param member table
---@param effectKey string
---@return boolean
function SkillSystem.HasEffect(member, effectKey)
    local effects = SkillSystem.GetEffects(member)
    return effects[effectKey] ~= nil
end

-- ============================================================================
-- 培养训练
-- ============================================================================

--- 执行一次培养训练
---@param memberId number
---@param trainingId string
---@return boolean success
---@return string message
function SkillSystem.Train(memberId, trainingId)
    local member = GameData.GetMember(memberId)
    if not member or not member.alive then return false, "族人不存在或已故" end

    -- 查找训练选项
    local training = nil
    for _, t in ipairs(GameData.TRAINING_OPTIONS) do
        if t.id == trainingId then training = t; break end
    end
    if not training then return false, "训练项目不存在" end

    -- 检查资源
    local cost = training.cost
    if not GameData.CanAfford(cost.silver or 0, cost.grain or 0, 0, 0) then
        return false, "资源不足"
    end

    -- 扣除资源
    GameData.SpendResources(cost.silver or 0, cost.grain or 0, 0, 0)

    -- 计算成长量（基础 + 天赋 + 装备加成）
    local baseGrowth = math.random(3, 8)
    local growth = math.floor(baseGrowth * training.multiplier)

    -- 天赋加成
    if member.talent then
        if training.attr == "study" and member.talent.id == "smart" then
            growth = math.floor(growth * 1.3)
        elseif training.attr == "martial" and member.talent.id == "strong" then
            growth = math.floor(growth * 1.3)
        end
    end

    -- 应用成长
    if training.attr == "health" then
        member.health = math.min(100, member.health + growth)
    elseif member[training.attr] ~= nil then
        member[training.attr] = math.min(100, member[training.attr] + growth)
    end

    GameData.AddLog(member.name .. "进行【" .. training.name .. "】，" .. training.attr .. "+" .. growth)
    return true, training.name .. "完成，" .. training.attr .. "+" .. growth
end

-- ============================================================================
-- 月度被动效果（由 MonthlyUpdate 调用）
-- ============================================================================

--- 处理所有族人的技能被动效果
---@param report table
function SkillSystem.ProcessMonthly(report)
    local s = GameData.state
    if not s then return end

    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.skillPath then
            local effects = SkillSystem.GetEffects(m)
            -- 声望月产
            if effects.famePerMonth then
                report.incomes.fame = report.incomes.fame + effects.famePerMonth
            end
        end
    end
end

-- ============================================================================
-- 后代继承加成（由 GrowthSystem 调用）
-- ============================================================================

--- 计算父母技能对后代的初始属性加成
---@param father table|nil
---@param mother table|nil
---@return table {study, martial}
function SkillSystem.CalcChildBonus(father, mother)
    local bonus = { study = 0, martial = 0 }

    local parents = { father, mother }
    for _, parent in ipairs(parents) do
        if parent and parent.skillPath then
            local effects = SkillSystem.GetEffects(parent)
            if effects.childStudyBonus then
                bonus.study = bonus.study + math.floor(parent.study * effects.childStudyBonus * 0.5)
            end
            if effects.childMartialBonus then
                bonus.martial = bonus.martial + math.floor(parent.martial * effects.childMartialBonus * 0.5)
            end
        end
    end

    -- 家学渊源：双亲同路径额外奖励
    if father and mother and father.skillPath and father.skillPath == mother.skillPath then
        if father.skillPath == "scholar" then
            bonus.study = bonus.study + 10
        elseif father.skillPath == "warrior" then
            bonus.martial = bonus.martial + 10
        end
    end

    return bonus
end

return SkillSystem
