-- ============================================================================
-- 大明浮生志2 - 年度目标与成就系统
-- 管理年度目标的生成/检查、成就/里程碑的解锁
-- ============================================================================

local GameData = require("Data.GameData")
local EraSystem = require("Data.EraSystem")

local GoalSystem = {}

-- ============================================================================
-- 年度目标系统
-- ============================================================================

--- 每年初生成2-3个可选目标
function GoalSystem.GenerateYearlyGoals(report)
    local s = GameData.state
    if not s then return end
    if s.goalYear == s.year then return end  -- 已为当年生成过

    s.goalYear = s.year

    -- 筛选可用目标（排除已完成的、不满足前置条件的）
    local completedSet = {}
    for _, id in ipairs(s.completedGoalIds) do completedSet[id] = true end

    local available = {}
    for _, goal in ipairs(GameData.YEARLY_GOAL_POOL) do
        if not completedSet[goal.id] and goal.condition(s) then
            available[#available + 1] = goal
        end
    end

    -- 打乱顺序
    for i = #available, 2, -1 do
        local j = math.random(1, i)
        available[i], available[j] = available[j], available[i]
    end

    -- 取2-3个
    local count = math.min(#available, math.random(2, 3))

    -- 创建状态快照（用于年末对比检查净增量）
    local snapshot = {
        silver = s.silver,
        grain = s.grain,
        fame = s.fame,
        clanRank = s.clanRank,
        fortCount = s.fortCount,
        totalBirths = s.totalBirths,
        totalDeaths = s.totalDeaths,
        totalExamPasses = s.totalExamPasses,
        industryCount = #s.industries,
        aliveCount = #GameData.GetAliveMembers(),
        soldierCount = 0,
    }
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.state == "从军" then snapshot.soldierCount = snapshot.soldierCount + 1 end
    end

    s.yearlyGoals = {}
    local goalNames = {}
    for i = 1, count do
        local goal = available[i]
        s.yearlyGoals[i] = {
            goalId = goal.id,
            name = goal.name,
            desc = goal.desc,
            icon = goal.icon,
            reward = goal.reward,
            completed = false,
            snapshot = snapshot,  -- 共享同一快照
        }
        goalNames[#goalNames + 1] = goal.name
    end

    if count > 0 then
        local goalsStr = table.concat(goalNames, "、")
        report.events[#report.events + 1] = "【年度目标】新的一年到来，族中定下目标：" .. goalsStr
        GameData.AddLog(EraSystem.GetYearLabel(s.year) .. "目标：" .. goalsStr .. "。")

        -- 弹窗通知年度目标
        local descLines = {}
        for _, g in ipairs(s.yearlyGoals) do
            local rewardParts = {}
            if g.reward.fame then rewardParts[#rewardParts + 1] = "声望+" .. g.reward.fame end
            if g.reward.silver then rewardParts[#rewardParts + 1] = "银两+" .. g.reward.silver end
            if g.reward.grain then rewardParts[#rewardParts + 1] = "粮食+" .. g.reward.grain end
            descLines[#descLines + 1] = "【" .. g.icon .. "】" .. g.name .. "：" .. g.desc .. "\n  奖励：" .. table.concat(rewardParts, "、")
        end

        if #s.pendingEvents < 5 then
            s.pendingEvents[#s.pendingEvents + 1] = {
                title = EraSystem.GetYearLabel(s.year) .. " 年度目标",
                desc = "新的一年，族中商议定下了以下目标：\n\n" .. table.concat(descLines, "\n\n"),
                choices = {
                    { text = "全力以赴！", effect = function() end },
                },
            }
        end
    end
end

--- 年末检查目标完成情况
function GoalSystem.CheckYearlyGoals(report)
    local s = GameData.state
    if not s then return end
    if not s.yearlyGoals or #s.yearlyGoals == 0 then return end

    local completedNames = {}
    local failedNames = {}

    for _, g in ipairs(s.yearlyGoals) do
        if g.completed then goto continueGoal end

        -- 从目标池中找到对应的check函数
        local goalDef = nil
        for _, def in ipairs(GameData.YEARLY_GOAL_POOL) do
            if def.id == g.goalId then goalDef = def; break end
        end
        if not goalDef then goto continueGoal end

        if goalDef.check(s, g.snapshot) then
            g.completed = true
            s.completedGoalIds[#s.completedGoalIds + 1] = g.goalId

            -- 发放奖励
            local rewardText = {}
            if g.reward.fame then
                GameData.AddResource("fame", g.reward.fame)
                rewardText[#rewardText + 1] = "声望+" .. g.reward.fame
            end
            if g.reward.silver then
                GameData.AddResource("silver", g.reward.silver)
                rewardText[#rewardText + 1] = "银两+" .. g.reward.silver
            end
            if g.reward.grain then
                GameData.AddResource("grain", g.reward.grain)
                rewardText[#rewardText + 1] = "粮食+" .. g.reward.grain
            end
            local rewardStr = table.concat(rewardText, "、")
            completedNames[#completedNames + 1] = g.name
            report.events[#report.events + 1] = "【目标达成】" .. g.name .. "！奖励：" .. rewardStr
            GameData.AddLog("年度目标【" .. g.name .. "】达成！奖励：" .. rewardStr .. "。")
        else
            failedNames[#failedNames + 1] = g.name
        end

        ::continueGoal::
    end

    -- 弹窗汇总
    if #completedNames > 0 or #failedNames > 0 then
        local desc = ""
        if #completedNames > 0 then
            desc = desc .. "达成目标：" .. table.concat(completedNames, "、") .. "\n"
        end
        if #failedNames > 0 then
            desc = desc .. "未完成：" .. table.concat(failedNames, "、") .. "\n"
        end
        desc = desc .. "\n新一年的目标即将揭晓！"

        if #s.pendingEvents < 5 then
            s.pendingEvents[#s.pendingEvents + 1] = {
                title = "年度目标总结",
                desc = desc,
                choices = {
                    { text = "继续前行", effect = function() end },
                },
            }
        end
    end

    -- 清空旧年目标（GenerateYearlyGoals 会写入新目标）
    s.yearlyGoals = {}
end

-- ============================================================================
-- 成就/里程碑检查（每月执行）
-- ============================================================================

function GoalSystem.CheckAchievements(report)
    local s = GameData.state
    if not s then return end

    -- 构建已解锁集合
    local unlocked = {}
    for _, id in ipairs(s.unlockedAchievements) do
        unlocked[id] = true
    end

    for _, ach in ipairs(GameData.ACHIEVEMENTS) do
        if not unlocked[ach.id] and ach.check(s) then
            -- 解锁成就
            s.unlockedAchievements[#s.unlockedAchievements + 1] = ach.id
            unlocked[ach.id] = true

            -- 发放奖励
            local rewardText = {}
            if ach.reward.fame then
                GameData.AddResource("fame", ach.reward.fame)
                rewardText[#rewardText + 1] = "声望+" .. ach.reward.fame
            end
            if ach.reward.silver then
                GameData.AddResource("silver", ach.reward.silver)
                rewardText[#rewardText + 1] = "银两+" .. ach.reward.silver
            end
            if ach.reward.grain then
                GameData.AddResource("grain", ach.reward.grain)
                rewardText[#rewardText + 1] = "粮食+" .. ach.reward.grain
            end
            local rewardStr = table.concat(rewardText, "、")

            report.events[#report.events + 1] = "【成就达成】" .. ach.name .. "！奖励：" .. rewardStr
            GameData.AddLog("达成成就【" .. ach.name .. "】：" .. ach.desc .. "。奖励：" .. rewardStr)

            -- 弹窗通知
            if #s.pendingEvents < 5 then
                s.pendingEvents[#s.pendingEvents + 1] = {
                    title = "成就达成：" .. ach.name,
                    desc = ach.desc .. "\n\n奖励：" .. rewardStr,
                    choices = {
                        { text = "好！", effect = function() end },
                    },
                }
            end
        end
    end
end

return GoalSystem
