-- ============================================================================
-- 大明浮生志2 - 广告变现系统
-- 激励视频广告触发点与奖励管理
-- 使用 sdk:ShowRewardVideoAd(callback) API
-- 广告次数按游戏年重置，加速特权按真实时间24小时有效
-- ============================================================================

---@diagnostic disable: undefined-global
-- sdk 是引擎在 TapTap 环境注入的全局对象，开发环境可能不存在

local GameData = require("Data.GameData")

local AdSystem = {}

--- 广告正在播放标记（播放期间游戏应暂停）
AdSystem.isShowingAd = false

-- ============================================================================
-- 广告点位配置（次数限制按年重置）
-- ============================================================================

--- 广告触发场景及每年次数限制
AdSystem.AD_SLOTS = {
    double_income   = { id = "double_income",   name = "双倍产出",     yearlyLimit = 6 },
    free_training   = { id = "free_training",    name = "免费培养",     yearlyLimit = 24 },
    market_discount = { id = "market_discount",  name = "集市优惠",     yearlyLimit = 12 },
    exam_boost      = { id = "exam_boost",       name = "科举加持",     yearlyLimit = 4 },
    exam_retry      = { id = "exam_retry",      name = "重考机会",     yearlyLimit = 8 },
    exam_hint       = { id = "exam_hint",       name = "考题提示",     yearlyLimit = 12 },
    battle_boost    = { id = "battle_boost",      name = "战力提升",     yearlyLimit = 6 },
    revive_member   = { id = "revive_member",    name = "族人复活",     yearlyLimit = 3 },
    extra_reward    = { id = "extra_reward",     name = "额外奖励",     yearlyLimit = 12 },
    speed_boost     = { id = "speed_boost",      name = "加速特权",     yearlyLimit = 12 },
    grain_relief    = { id = "grain_relief",     name = "粮食急救",     yearlyLimit = 6 },
    loan            = { id = "loan",             name = "钱庄借贷",     yearlyLimit = 6 },
    ad_grant        = { id = "ad_grant",         name = "看广告领银",   yearlyLimit = 6 },
    year_bonus      = { id = "year_bonus",       name = "年终奖励翻倍", yearlyLimit = 1 },
    health_cure     = { id = "health_cure",      name = "神医诊治",     yearlyLimit = 4 },
    marriage_refresh = { id = "marriage_refresh", name = "换一批佳人",   yearlyLimit = 12 },
    clinic_boost     = { id = "clinic_boost",    name = "郎中加持",     yearlyLimit = 12 },
    clinic_extra     = { id = "clinic_extra",    name = "增额郎中",     yearlyLimit = 99 },
    funeral_grand    = { id = "funeral_grand",   name = "广大祭葬",     yearlyLimit = 3  },
}

-- ============================================================================
-- 状态管理（按年重置）
-- ============================================================================

--- 确保广告状态已初始化
function AdSystem.EnsureState()
    local s = GameData.state
    if not s.adState then
        s.adState = {
            usageYear = {},        -- { [slotId] = count } 本年使用次数
            lastResetYear = 0,     -- 上次重置的游戏年份
            totalWatched = 0,      -- 累计观看总数
            speedUnlockTime = 0,   -- 加速解锁的真实时间戳（秒）
            loans = {},            -- 贷款记录 { { principal, interest, monthsLeft } }
        }
    end
    -- 兼容旧存档：迁移 usageToday → usageYear
    if s.adState.usageToday then
        s.adState.usageYear = s.adState.usageYear or {}
        s.adState.usageToday = nil
        s.adState.lastResetDay = nil
    end
    if not s.adState.usageYear then s.adState.usageYear = {} end
    if not s.adState.loans then s.adState.loans = {} end
    -- 按游戏年重置次数
    local currentYear = s.year or 0
    if s.adState.lastResetYear ~= currentYear then
        s.adState.usageYear = {}
        s.adState.lastResetYear = currentYear
    end
end

--- 检查某个广告点位本年是否可用
---@param slotId string
---@return boolean
function AdSystem.IsAvailable(slotId)
    AdSystem.EnsureState()
    local slot = AdSystem.AD_SLOTS[slotId]
    if not slot then return false end
    local used = GameData.state.adState.usageYear[slotId] or 0
    return used < slot.yearlyLimit
end

--- 获取某广告点位剩余次数
---@param slotId string
---@return number remaining
function AdSystem.GetRemaining(slotId)
    AdSystem.EnsureState()
    local slot = AdSystem.AD_SLOTS[slotId]
    if not slot then return 0 end
    local used = GameData.state.adState.usageYear[slotId] or 0
    return math.max(0, slot.yearlyLimit - used)
end

--- 记录一次广告使用
---@param slotId string
local function RecordUsage(slotId)
    AdSystem.EnsureState()
    local adState = GameData.state.adState
    adState.usageYear[slotId] = (adState.usageYear[slotId] or 0) + 1
    adState.totalWatched = adState.totalWatched + 1
end

-- ============================================================================
-- 核心广告播放接口
-- ============================================================================

--- 播放激励视频广告并在成功后执行回调
---@param slotId string 广告点位 ID
---@param onSuccess function 广告观看成功后的回调
---@param onFail function|nil 广告播放失败后的回调（可选）
function AdSystem.ShowRewardAd(slotId, onSuccess, onFail)
    -- GM 模式：跳过广告直接成功
    if GameData.state and GameData.state.gmMode then
        log:Write(LOG_INFO, "[AdSystem][GM] 跳过广告: " .. slotId)
        if onSuccess then onSuccess() end
        return
    end

    if not AdSystem.IsAvailable(slotId) then
        if onFail then onFail("今年次数已用完，明年重置") end
        return
    end

    -- 检查 sdk 是否可用
    if not sdk then
        -- 开发环境下模拟成功
        log:Write(LOG_WARNING, "[AdSystem] sdk 不可用，模拟广告成功")
        RecordUsage(slotId)
        if onSuccess then onSuccess() end
        return
    end

    -- 标记广告正在播放（游戏循环检测此标记暂停推进）
    AdSystem.isShowingAd = true
    AdSystem._adStartTime = os.clock()
    log:Write(LOG_INFO, "[AdSystem] 开始播放广告: " .. slotId)

    local callOk, callErr = pcall(function()
    sdk:ShowRewardVideoAd(function(result)
        -- 广告结束，取消标记（必须最先执行，防止后续代码异常导致卡死）
        AdSystem.isShowingAd = false
        AdSystem._adStartTime = nil
        log:Write(LOG_INFO, "[AdSystem] 广告播放结束: " .. slotId)

        if result and result.success then
            RecordUsage(slotId)
            local slot = AdSystem.AD_SLOTS[slotId]
            GameData.AddLog("观看广告【" .. (slot and slot.name or slotId) .. "】获得奖励")
            if onSuccess then
                local ok, err = pcall(onSuccess)
                if not ok then
                    log:Write(LOG_WARNING, "[AdSystem] onSuccess callback error: " .. tostring(err))
                end
            end
        else
            local msg = result and result.msg or "广告异常"
            if msg == "embed manual close" then
                msg = "需完整观看广告才能获得奖励"
            end
            log:Write(LOG_WARNING, "[AdSystem] 广告播放失败: " .. tostring(msg))
            if onFail then
                local ok, err = pcall(onFail, msg)
                if not ok then
                    log:Write(LOG_WARNING, "[AdSystem] onFail callback error: " .. tostring(err))
                end
            end
        end
    end)
    end) -- pcall end
    if not callOk then
        -- sdk 调用本身失败，立即重置标记
        AdSystem.isShowingAd = false
        AdSystem._adStartTime = nil
        log:Write(LOG_WARNING, "[AdSystem] sdk:ShowRewardVideoAd call failed: " .. tostring(callErr))
        if onFail then onFail("广告服务异常") end
    end
end

-- ============================================================================
-- 加速特权（看广告解锁 2x/3x，真实时间24小时有效）
-- ============================================================================

--- 获取当前真实时间戳（秒）
---@return number
local function GetRealTime()
    return os.time()
end

--- 检查加速是否在有效期内（30分钟真实时间）
---@return boolean
function AdSystem.IsSpeedUnlocked()
    AdSystem.EnsureState()
    local adState = GameData.state.adState
    local unlockTime = adState.speedUnlockTime or 0
    if unlockTime <= 0 then return false end
    local elapsed = GetRealTime() - unlockTime
    return elapsed < 1800  -- 30 * 60 = 1800秒
end

--- 获取加速剩余时间（秒）
---@return number 剩余秒数，0 表示已过期
function AdSystem.GetSpeedRemainingSeconds()
    AdSystem.EnsureState()
    local adState = GameData.state.adState
    local unlockTime = adState.speedUnlockTime or 0
    if unlockTime <= 0 then return 0 end
    local remaining = 1800 - (GetRealTime() - unlockTime)
    return math.max(0, remaining)
end

--- 看广告解锁加速，成功后30分钟内可用 2x/3x
---@param onDone function|nil callback(success, msg)
function AdSystem.UnlockSpeed(onDone)
    if AdSystem.IsSpeedUnlocked() then
        if onDone then onDone(true, "加速仍在有效期内") end
        return
    end

    AdSystem.ShowRewardAd("speed_boost", function()
        local adState = GameData.state.adState
        adState.speedUnlockTime = GetRealTime()
        GameData.AddLog("观看广告解锁加速特权，30分钟内可使用2倍/3倍速")
        if onDone then onDone(true) end
    end, function(msg)
        if onDone then onDone(false, msg) end
    end)
end

-- ============================================================================
-- 各场景奖励逻辑（供 UI 层调用）
-- ============================================================================

--- 双倍月度产出
---@param report table 月度结算报告
---@param onDone function|nil
function AdSystem.DoubleMonthlyIncome(report, onDone)
    AdSystem.ShowRewardAd("double_income", function()
        local bonusSilver = report.incomes.silver
        local bonusGrain = report.incomes.grain
        local bonusCloth = report.incomes.cloth
        local bonusFame = report.incomes.fame
        GameData.AddResource("silver", bonusSilver)
        GameData.AddResource("grain", bonusGrain)
        GameData.AddResource("cloth", bonusCloth)
        GameData.AddResource("fame", bonusFame)
        GameData.AddLog("观看广告获得双倍产出：银" .. bonusSilver .. " 粮" .. bonusGrain)
        if onDone then onDone(true, bonusSilver, bonusGrain) end
    end, function(msg)
        if onDone then onDone(false, msg) end
    end)
end

--- 免费培养一次（不消耗资源）
---@param memberId number
---@param trainingId string
---@param onDone function|nil
function AdSystem.FreeTraining(memberId, trainingId, onDone)
    AdSystem.ShowRewardAd("free_training", function()
        local member = GameData.GetMember(memberId)
        if not member or not member.alive then
            if onDone then onDone(false, "族人不存在") end
            return
        end
        local training = nil
        for _, t in ipairs(GameData.TRAINING_OPTIONS) do
            if t.id == trainingId then training = t; break end
        end
        if not training then
            if onDone then onDone(false, "训练不存在") end
            return
        end
        local growth = math.random(3, 8)
        growth = math.floor(growth * training.multiplier)
        if member.talent then
            if training.attr == "study" and member.talent.id == "smart" then
                growth = math.floor(growth * 1.3)
            elseif training.attr == "martial" and member.talent.id == "strong" then
                growth = math.floor(growth * 1.3)
            end
        end
        if training.attr == "health" then
            member.health = math.min(100, member.health + growth)
        elseif member[training.attr] ~= nil then
            member[training.attr] = math.min(100, member[training.attr] + growth)
        end
        GameData.AddLog(member.name .. "通过广告免费【" .. training.name .. "】，" .. training.attr .. "+" .. growth)
        if onDone then onDone(true, training.name .. "完成，" .. training.attr .. "+" .. growth) end
    end, function(msg)
        if onDone then onDone(false, msg) end
    end)
end

--- 集市交易优惠（返还 20% 银两）
---@param silverSpent number 本次交易花费的银两
---@param onDone function|nil
function AdSystem.MarketDiscount(silverSpent, onDone)
    AdSystem.ShowRewardAd("market_discount", function()
        local refund = math.max(1, math.floor(silverSpent * 0.2))
        GameData.AddResource("silver", refund)
        GameData.AddLog("集市优惠广告返还银两" .. refund)
        if onDone then onDone(true, refund) end
    end, function(msg)
        if onDone then onDone(false, msg) end
    end)
end

--- 科举加持（额外+15%成功率，存入临时标记）
---@param onDone function|nil
function AdSystem.ExamBoost(onDone)
    AdSystem.ShowRewardAd("exam_boost", function()
        local s = GameData.state
        s.adExamBoost = true
        GameData.AddLog("科举加持：本次考试成功率+15%")
        if onDone then onDone(true) end
    end, function(msg)
        if onDone then onDone(false, msg) end
    end)
end

--- 战力提升（从军族人本月属性临时+10%）
---@param onDone function|nil
function AdSystem.BattleBoost(onDone)
    AdSystem.ShowRewardAd("battle_boost", function()
        local s = GameData.state
        s.adBattleBoost = true
        GameData.AddLog("战力加持：从军族人本月属性+10%")
        if onDone then onDone(true) end
    end, function(msg)
        if onDone then onDone(false, msg) end
    end)
end

--- 族人复活（限已死亡不超过3个月的族人）
---@param memberId number
---@param onDone function|nil
function AdSystem.ReviveMember(memberId, onDone)
    AdSystem.ShowRewardAd("revive_member", function()
        local member = GameData.GetMember(memberId)
        if not member then
            if onDone then onDone(false, "族人不存在") end
            return
        end
        member.alive = true
        member.state = "在家"
        member.health = 30 + math.random(0, 20)
        local s = GameData.state
        s.totalDeaths = math.max(0, s.totalDeaths - 1)
        GameData.AddLog(member.name .. "死而复生！（观看广告）")
        if onDone then onDone(true, member.name .. "奇迹般苏醒！") end
    end, function(msg)
        if onDone then onDone(false, msg) end
    end)
end

--- 额外事件奖励（事件选择后追加 50% 奖励）
---@param rewardTable table {silver=N, grain=N, fame=N, ...}
---@param onDone function|nil
function AdSystem.ExtraEventReward(rewardTable, onDone)
    AdSystem.ShowRewardAd("extra_reward", function()
        local bonusText = {}
        for res, amount in pairs(rewardTable) do
            if type(amount) == "number" and amount > 0 then
                local extra = math.max(1, math.floor(amount * 0.5))
                GameData.AddResource(res, extra)
                bonusText[#bonusText + 1] = res .. "+" .. extra
            end
        end
        local text = table.concat(bonusText, " ")
        GameData.AddLog("广告额外奖励：" .. text)
        if onDone then onDone(true, text) end
    end, function(msg)
        if onDone then onDone(false, msg) end
    end)
end

-- ============================================================================
-- 新增：粮食急救（粮食不足时看广告获得紧急粮食）
-- ============================================================================

--- 粮食急救：根据当前族人数量赠送一定粮食
---@param onDone function|nil
function AdSystem.GrainRelief(onDone)
    AdSystem.ShowRewardAd("grain_relief", function()
        local alive = #GameData.GetAliveMembers()
        -- 赠送够族人消耗3个月的粮食（成人每月消耗3，这里给约3个月量）
        local relief = math.max(20, alive * 3 * 3)
        GameData.AddResource("grain", relief)
        GameData.AddLog("紧急粮食救济：获得粮食" .. relief)
        if onDone then onDone(true, relief) end
    end, function(msg)
        if onDone then onDone(false, msg) end
    end)
end

-- ============================================================================
-- 新增：神医诊治（治疗生病/重伤族人）
-- ============================================================================

--- 神医诊治：恢复族人健康到60-80
---@param memberId number
---@param onDone function|nil
function AdSystem.HealthCure(memberId, onDone)
    AdSystem.ShowRewardAd("health_cure", function()
        local member = GameData.GetMember(memberId)
        if not member or not member.alive then
            if onDone then onDone(false, "族人不存在") end
            return
        end
        local oldHealth = member.health
        member.health = math.min(100, math.max(member.health, 60 + math.random(0, 20)))
        if member.state == "生病" then
            member.state = "在家"
        end
        local healed = member.health - oldHealth
        GameData.AddLog(member.name .. "经神医诊治，健康恢复+" .. healed)
        if onDone then onDone(true, member.name .. "恢复健康（+" .. healed .. "）") end
    end, function(msg)
        if onDone then onDone(false, msg) end
    end)
end

-- ============================================================================
-- 坐堂郎中加持（看广告，24小时内年度治疗上限从3次提升至6次）
-- ============================================================================

--- 检查坐堂郎中加持是否在有效期内（24小时真实时间）
---@return boolean
function AdSystem.IsClinicBoosted()
    AdSystem.EnsureState()
    local adState = GameData.state.adState
    local boostTime = adState.clinicBoostTime or 0
    if boostTime <= 0 then return false end
    return (GetRealTime() - boostTime) < 86400
end

--- 获取坐堂郎中加持剩余时间（秒）
---@return number
function AdSystem.GetClinicBoostRemainingSeconds()
    AdSystem.EnsureState()
    local adState = GameData.state.adState
    local boostTime = adState.clinicBoostTime or 0
    if boostTime <= 0 then return 0 end
    return math.max(0, 86400 - (GetRealTime() - boostTime))
end

--- 获取坐堂郎中当前年度治疗上限
---@return number
function AdSystem.GetClinicYearlyLimit()
    return AdSystem.IsClinicBoosted() and 6 or 3
end

--- 看广告解锁坐堂郎中加持
---@param onDone function|nil callback(success, msg)
function AdSystem.BoostClinicDoctor(onDone)
    if AdSystem.IsClinicBoosted() then
        if onDone then onDone(true, "加持仍在有效期内") end
        return
    end
    AdSystem.ShowRewardAd("clinic_boost", function()
        local adState = GameData.state.adState
        adState.clinicBoostTime = GetRealTime()
        GameData.AddLog("观看广告，坐堂郎中24小时内可诊治6人/年")
        if onDone then onDone(true) end
    end, function(msg)
        if onDone then onDone(false, msg) end
    end)
end

-- ============================================================================
-- 新增：年终奖励翻倍
-- ============================================================================

--- 年终总结时奖励翻倍
---@param rewards table { silver=N, grain=N, fame=N, ... }
---@param onDone function|nil
function AdSystem.DoubleYearBonus(rewards, onDone)
    AdSystem.ShowRewardAd("year_bonus", function()
        local bonusText = {}
        for res, amount in pairs(rewards) do
            if type(amount) == "number" and amount > 0 then
                GameData.AddResource(res, amount) -- 再给一倍
                bonusText[#bonusText + 1] = res .. "+" .. amount
            end
        end
        local text = table.concat(bonusText, " ")
        GameData.AddLog("年终奖励翻倍：" .. text)
        if onDone then onDone(true, text) end
    end, function(msg)
        if onDone then onDone(false, msg) end
    end)
end

-- ============================================================================
-- 新增：钱庄贷款系统（看广告借钱，按月还息，经济平衡）
-- ============================================================================

--- 贷款配置
-- 看广告直接领银两（白送，不用还）
AdSystem.AD_GRANT_OPTIONS = {
    { id = "grant_small",  name = "小额赞助", amount = 30,   desc = "看广告领银30两" },
    { id = "grant_medium", name = "中额赞助", amount = 80,   desc = "看广告领银80两" },
    { id = "grant_large",  name = "大额赞助", amount = 150,  desc = "看广告领银150两" },
}

-- 普通借贷（有利息要还，不看广告）
AdSystem.LOAN_OPTIONS = {
    { id = "small",  name = "小额借贷", amount = 100,  interest = 5,  months = 6,  desc = "借银100两，月息5两，6个月还清" },
    { id = "medium", name = "中额借贷", amount = 300,  interest = 15, months = 12, desc = "借银300两，月息15两，12个月还清" },
    { id = "large",  name = "大额借贷", amount = 600,  interest = 35, months = 12, desc = "借银600两，月息35两，12个月还清" },
}

--- 获取当前贷款列表
---@return table[]
function AdSystem.GetLoans()
    AdSystem.EnsureState()
    return GameData.state.adState.loans
end

--- 获取当前总负债
---@return number 总欠款本金
---@return number 每月总利息
function AdSystem.GetTotalDebt()
    AdSystem.EnsureState()
    local totalPrincipal = 0
    local totalInterest = 0
    for _, loan in ipairs(GameData.state.adState.loans) do
        totalPrincipal = totalPrincipal + loan.principal
        totalInterest = totalInterest + loan.interest
    end
    return totalPrincipal, totalInterest
end

--- 是否还能借更多（最多同时3笔贷款，防止滥借）
---@return boolean
function AdSystem.CanBorrow()
    AdSystem.EnsureState()
    return #GameData.state.adState.loans < 3
end

--- 看广告领银两（白送，不用还）
---@param grantId string 赞助选项ID
---@param onDone function|nil
function AdSystem.TakeAdGrant(grantId, onDone)
    local option = nil
    for _, opt in ipairs(AdSystem.AD_GRANT_OPTIONS) do
        if opt.id == grantId then option = opt; break end
    end
    if not option then
        if onDone then onDone(false, "赞助选项不存在") end
        return
    end

    AdSystem.ShowRewardAd("ad_grant", function()
        GameData.AddResource("silver", option.amount)
        GameData.AddLog("钱庄赞助：看广告领取银两" .. option.amount)
        if onDone then onDone(true, "获得银两" .. option.amount .. "（无需归还）") end
    end, function(msg)
        if onDone then onDone(false, msg) end
    end)
end

--- 普通借贷（不看广告，有利息要还）
---@param loanId string 贷款选项ID
---@param onDone function|nil
function AdSystem.TakeLoan(loanId, onDone)
    if not AdSystem.CanBorrow() then
        if onDone then onDone(false, "最多同时持有3笔贷款") end
        return
    end

    local option = nil
    for _, opt in ipairs(AdSystem.LOAN_OPTIONS) do
        if opt.id == loanId then option = opt; break end
    end
    if not option then
        if onDone then onDone(false, "贷款选项不存在") end
        return
    end

    local loans = GameData.state.adState.loans
    loans[#loans + 1] = {
        principal = option.amount,
        interest = option.interest,
        monthsLeft = option.months,
        name = option.name,
    }
    GameData.AddResource("silver", option.amount)
    GameData.AddLog("钱庄" .. option.name .. "：借入银两" .. option.amount .. "，月息" .. option.interest .. "两")
    if onDone then onDone(true, "借入银两" .. option.amount) end
end

--- 每月结算贷款利息（在 MonthlyUpdate 中调用）
---@return number totalInterestPaid 本月支付的总利息
---@return table expiredLoans 到期但未还清的贷款（触发惩罚）
function AdSystem.ProcessMonthlyLoans()
    AdSystem.EnsureState()
    local loans = GameData.state.adState.loans
    local totalInterest = 0
    local expiredLoans = {}

    local i = 1
    while i <= #loans do
        local loan = loans[i]
        -- 扣利息
        local interest = loan.interest
        GameData.AddResource("silver", -interest)
        totalInterest = totalInterest + interest
        loan.monthsLeft = loan.monthsLeft - 1

        if loan.monthsLeft <= 0 then
            -- 到期：扣除本金
            local s = GameData.state
            if s.silver >= loan.principal then
                GameData.AddResource("silver", -loan.principal)
                GameData.AddLog("钱庄贷款到期，归还本金" .. loan.principal .. "两")
            else
                -- 还不起本金：声望惩罚
                local famePenalty = math.max(3, math.floor(loan.principal / 20))
                GameData.AddResource("fame", -famePenalty)
                GameData.AddLog("钱庄贷款到期无力偿还！声望-" .. famePenalty)
                expiredLoans[#expiredLoans + 1] = loan
            end
            table.remove(loans, i)
        else
            i = i + 1
        end
    end

    if totalInterest > 0 then
        GameData.AddLog("本月钱庄利息支出：银两" .. totalInterest)
    end

    return totalInterest, expiredLoans
end

--- 主动提前还款（不需要看广告）
---@param loanIndex number 贷款在列表中的索引（1开始）
---@param onDone function|nil
function AdSystem.RepayLoan(loanIndex, onDone)
    AdSystem.EnsureState()
    local loans = GameData.state.adState.loans
    if loanIndex < 1 or loanIndex > #loans then
        if onDone then onDone(false, "贷款不存在") end
        return
    end
    local loan = loans[loanIndex]
    local s = GameData.state
    if s.silver < loan.principal then
        if onDone then onDone(false, "银两不足（需" .. loan.principal .. "两）") end
        return
    end
    GameData.AddResource("silver", -loan.principal)
    GameData.AddLog("提前归还钱庄贷款：" .. loan.name .. "，本金" .. loan.principal .. "两")
    table.remove(loans, loanIndex)
    if onDone then onDone(true, "已还清" .. loan.name) end
end

-- ============================================================================
-- 增额郎中（看广告增加临时医生，每次+1人，1小时有效，最多同时3个临时医生）
-- ============================================================================

local CLINIC_EXTRA_DURATION = 3600  -- 1小时（秒）

--- 清理过期的临时医生
local function CleanExpiredExtraDoctors()
    local s = GameData.state
    if not s.clinicExtraDoctors then return end
    local now = GetRealTime()
    local i = 1
    while i <= #s.clinicExtraDoctors do
        if now >= s.clinicExtraDoctors[i].expiresAt then
            table.remove(s.clinicExtraDoctors, i)
        else
            i = i + 1
        end
    end
end

--- 获取当前有效的临时医生列表
---@return table[] { memberId, expiresAt }
function AdSystem.GetExtraDoctors()
    CleanExpiredExtraDoctors()
    return GameData.state.clinicExtraDoctors or {}
end

--- 获取当前有效临时医生数量
---@return number
function AdSystem.GetExtraDoctorCount()
    return #AdSystem.GetExtraDoctors()
end

--- 看广告增加一个临时医生
---@param memberId number 要指派的族人ID
---@param onDone function|nil callback(success, msg)
function AdSystem.AddExtraDoctor(memberId, onDone)
    CleanExpiredExtraDoctors()
    local s = GameData.state
    if not s.clinicExtraDoctors then s.clinicExtraDoctors = {} end

    if #s.clinicExtraDoctors >= 3 then
        if onDone then onDone(false, "临时医生已达上限（3人）") end
        return
    end

    -- 检查族人是否有效
    local member = GameData.GetMember(memberId)
    if not member or not member.alive then
        if onDone then onDone(false, "族人不存在") end
        return
    end
    if member.age < 16 then
        if onDone then onDone(false, "年龄不足16岁") end
        return
    end
    -- 不能是正式郎中
    if member.id == s.clinicDoctorId then
        if onDone then onDone(false, "此人已是坐堂郎中") end
        return
    end
    -- 不能重复指派
    for _, d in ipairs(s.clinicExtraDoctors) do
        if d.memberId == member.id then
            if onDone then onDone(false, "此人已在临时坐诊") end
            return
        end
    end

    AdSystem.ShowRewardAd("clinic_extra", function()
        s.clinicExtraDoctors[#s.clinicExtraDoctors + 1] = {
            memberId = member.id,
            expiresAt = GetRealTime() + CLINIC_EXTRA_DURATION,
        }
        GameData.AddLog(member.name .. "看广告就任临时郎中（1小时）")
        if onDone then onDone(true, member.name .. "已就任临时郎中") end
    end, function(msg)
        if onDone then onDone(false, msg) end
    end)
end

--- 获取所有活跃医生列表（正式 + 临时），用于自动治疗
---@return table[] { memberId, isTemp }
function AdSystem.GetAllActiveDoctors()
    CleanExpiredExtraDoctors()
    local s = GameData.state
    local doctors = {}

    -- 正式郎中
    if s.clinicDoctorId then
        local doc = GameData.GetMember(s.clinicDoctorId)
        if doc and doc.alive and doc.gender == "female" and (doc.study or 0) >= 60 then
            doctors[#doctors + 1] = { memberId = s.clinicDoctorId, isTemp = false }
        end
    end

    -- 临时郎中
    for _, d in ipairs(s.clinicExtraDoctors or {}) do
        local doc = GameData.GetMember(d.memberId)
        if doc and doc.alive then
            doctors[#doctors + 1] = { memberId = d.memberId, isTemp = true }
        end
    end

    return doctors
end

return AdSystem
