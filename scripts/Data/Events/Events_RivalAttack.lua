-- ============================================================================
-- 大明浮生志2 - C4 敌对势力进攻事件池
-- 高品阶家族面临外部势力主动进攻，必须做出防御决策
-- 包含3D战斗、防御策略、外交博弈、连锁事件
-- ============================================================================

local GameData = require("Data.GameData")
local RivalClans = require("Data.RivalClans")

-- ============================================================================
-- 辅助函数
-- ============================================================================

--- 获取所有符合出战条件的族人（按武艺排序）
local function GetAllFighters()
    local candidates = {}
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.gender == "male" and m.age >= 16 and m.age <= 55
           and m.health > 20 and m.state ~= "生病" and m.state ~= "从军" then
            candidates[#candidates + 1] = m
        end
    end
    table.sort(candidates, function(a, b) return (a.martial or 0) > (b.martial or 0) end)
    return candidates
end

--- 从候选列表取前N个的名字（用于事件描述文本）
local function GetFighterNames(fighters, count)
    local names = {}
    for i = 1, math.min(count or 4, #fighters) do
        names[#names + 1] = fighters[i].name
    end
    return names
end

--- 获取军队总兵力
local function GetArmySize()
    local s = GameData.state
    if not s or not s.army then return 0 end
    return (s.army.infantry or 0) + (s.army.archers or 0)
end

--- 获取防御力评估（基于兵力+武将武艺）
local function GetDefenseRating()
    local armySize = GetArmySize()
    local topMartial = 0
    local martialSum = 0
    local count = 0
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.gender == "male" and m.age >= 16 and m.martial > 10 then
            martialSum = martialSum + m.martial
            count = count + 1
            if m.martial > topMartial then topMartial = m.martial end
        end
    end
    return {
        armySize = armySize,
        topMartial = topMartial,
        avgMartial = count > 0 and math.floor(martialSum / count) or 0,
        fighterCount = count,
    }
end

--- 进攻规模缩放（品级越高进攻越猛）
local function GetAttackScale(rank)
    local scales = {
        [5] = 1.0,
        [6] = 1.3,
        [7] = 1.6,
        [8] = 2.0,
        [9] = 2.5,
    }
    return scales[rank] or 1.0
end

--- 生成进攻敌军数据（兼容 BattleScene）
local function GenerateAttacker(enemyName, rank, year, unitStyle)
    local tierIndex = 2  -- 进攻方至少normal
    if rank >= 7 then tierIndex = 4       -- elite
    elseif rank >= 5 then tierIndex = 3   -- hard
    end
    if year >= 1630 and tierIndex < 4 then tierIndex = tierIndex + 1 end

    local tier = RivalClans.DIFFICULTY_TIERS[math.min(tierIndex, 4)]
    local scale = GetAttackScale(rank)

    local surnames = {"马", "孙", "韩", "徐", "曹", "魏", "吕", "沈", "冯", "郑"}
    local surname = surnames[math.random(1, #surnames)]
    local givenNames = {"虎", "豹", "彪", "龙", "猛", "刚", "坚", "毅", "烈", "威",
                        "勇", "强", "铁", "钢", "石", "山", "峰", "岩", "崖", "雷"}

    local memberCount = math.random(tier.memberRange[1], tier.memberRange[2])
    local members = {}
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

    local soldiers = math.random(tier.soldierRange[1], tier.soldierRange[2])
    soldiers = math.floor(soldiers * scale / 100) * 100
    if soldiers < 100 then soldiers = 100 end

    local rewardMul = tier.rewardMul * 0.7
    local rewards = {
        silver = math.floor(math.random(15, 30) * rewardMul * scale),
        grain  = math.floor(math.random(10, 25) * rewardMul * scale),
        fame   = math.floor(math.random(8, 20) * rewardMul * scale),
    }

    return {
        id = 0,
        name = enemyName,
        surname = surname,
        tierId = tier.id,
        tierName = tier.name,
        tierColor = tier.color,
        tierDesc = "敌军来犯",
        members = members,
        soldiers = soldiers,
        rewards = rewards,
        unitStyle = unitStyle or nil,
    }
end

--- 创建防御战结算
local function CreateDefenseSettlement(defeatPenalty, victoryBonus)
    return function(result, rivalData, deployedIds)
        local s = GameData.state
        if result == "victory" then
            local sr = rivalData.rewards.silver + (victoryBonus and victoryBonus.silver or 0)
            local gr = rivalData.rewards.grain + (victoryBonus and victoryBonus.grain or 0)
            local fr = rivalData.rewards.fame + (victoryBonus and victoryBonus.fame or 0)
            GameData.AddResource("silver", sr)
            GameData.AddResource("grain", gr)
            GameData.AddResource("fame", fr)
            GameData.AddLog("成功击退" .. rivalData.name .. "！缴获银两" .. sr .. "、粮食" .. gr .. "、声望+" .. fr)
        else
            local sL = defeatPenalty.silver or 0
            local gL = defeatPenalty.grain or 0
            local cL = defeatPenalty.cloth or 0
            local fL = defeatPenalty.fame or 0
            GameData.AddResource("silver", -sL)
            GameData.AddResource("grain", -gL)
            GameData.AddResource("cloth", -cL)
            GameData.AddResource("fame", -fL)
            GameData.AddLog("抵御" .. rivalData.name .. "失败！损失银两" .. sL .. "、粮食" .. gL .. "、声望-" .. fL)
        end

        -- 出战族人伤亡处理
        for _, memberId in ipairs(deployedIds) do
            local member = GameData.GetMember(memberId)
            if member then
                if result == "defeat" then
                    if math.random(100) <= 10 then
                        member.alive = false
                        member.deathAge = member.age
                        member.deathYear = s.year
                        member.deathCause = "战死"
                        GameData.AddLog(member.name .. "在防御战中壮烈牺牲。")
                        GameData.CheckPatriarchDeath(member)
                        GameData.NotifyDeath(member, "战死")
                    elseif math.random(100) <= 30 then
                        member.health = math.max(10, member.health - math.random(15, 30))
                        GameData.AddLog(member.name .. "在防御战中负伤。")
                    end
                else
                    if math.random(100) <= 15 then
                        member.health = math.max(20, member.health - math.random(5, 15))
                    end
                end
            end
        end
    end
end

-- ============================================================================
-- 敌对势力进攻事件定义（8个深度事件）
-- ============================================================================

local events = {}

-- ============================================================================
-- 事件1：山匪劫寨（品阶5-7）
-- 山匪趁夜袭击，玩家选择应战/坚守/纳贡
-- ============================================================================
events[#events + 1] = {
    id = "ra_bandit_raid",
    title = "山匪劫寨",
    rankRange = { 5, 7 },
    weight = 7,
    cooldownMonths = 8,
    isDisaster = false,
    check = function(s)
        return #GameData.GetAliveMembers() >= 8
    end,
    execute = function(s, report)
        local scale = GetAttackScale(s.clanRank)
        local silverLoss = math.floor(30 * scale)
        local grainLoss = math.floor(25 * scale)
        local def = GetDefenseRating()
        local allFighters = GetAllFighters()
        local armySize = GetArmySize()
        local armyHint = armySize > 0
            and ("当前兵力：" .. armySize .. "人 | 最强武将：武艺" .. def.topMartial)
            or ("当前兵力：无 | 最强武将：武艺" .. def.topMartial .. "\n(!) 你尚未征兵，仅靠族人武艺迎战，胜算较低！")

        return {
            title = "山匪劫寨",
            desc = "夜半三更，庄外火光冲天！一伙山匪纠集了" ..
                   math.floor(200 * scale) .. "余众，趁夜色偷袭庄园。\n\n" ..
                   "护院急报：'寨主，山匪已破外墙，正向粮仓推进！'\n\n" ..
                   armyHint,
            choices = {
                {
                    text = armySize > 0
                        and "亲率族人迎战！（3D战斗·中等难度）"
                        or "率族人拼死抵抗！（3D战斗·无兵·高风险）",
                    effect = function()
                        if #allFighters < 2 then
                            GameData.AddResource("silver", -silverLoss)
                            GameData.AddResource("grain", -grainLoss)
                            GameData.AddLog("无人可战，被山匪洗劫！损失银两" .. silverLoss .. "、粮食" .. grainLoss)
                            return
                        end
                        local rival = GenerateAttacker("黑风寨匪众", s.clanRank, s.year, "bandit")
                        -- 无兵时敌方规模也适当下调（纯护卫冲突）
                        if armySize == 0 then
                            rival.soldiers = math.max(50, math.floor(rival.soldiers * 0.3))
                        end
                        local GS = require("UI.GameScreen")
                        GS.EventBattle(allFighters, rival, {
                            soldierCount = math.min(armySize, 300),
                            onSettle = CreateDefenseSettlement(
                                { silver = silverLoss, grain = grainLoss, fame = 5 },
                                { silver = 15, grain = 10, fame = 10 }
                            ),
                        })
                    end,
                },
                {
                    text = "紧闭寨门坚守（损失少量物资，安全）",
                    effect = function()
                        local loss = math.floor(15 * scale)
                        GameData.AddResource("grain", -loss)
                        -- 60%概率匪徒抢了外围就走
                        if math.random(100) <= 60 then
                            GameData.AddLog("坚守寨门，山匪抢掠外围粮食" .. loss .. "石后退去。")
                        else
                            -- 40%概率匪徒破墙
                            local extraLoss = math.floor(20 * scale)
                            GameData.AddResource("silver", -extraLoss)
                            GameData.AddLog("寨门被破！山匪额外掠走银两" .. extraLoss .. "、粮食" .. loss .. "石。")
                        end
                    end,
                },
                {
                    text = "纳贡求和（花钱消灾，但损声望）",
                    effect = function()
                        local bribe = math.floor(40 * scale)
                        GameData.AddResource("silver", -bribe)
                        GameData.AddResource("fame", -8)
                        GameData.AddLog("送出银两" .. bribe .. "向山匪纳贡求和，声望-8。虽保全族人，但邻里暗笑。")
                    end,
                },
            },
        }
    end,
}

-- ============================================================================
-- 事件2：倭寇侵袭（品阶5-8，沿海年代限定）
-- 倭寇登陆袭击，需要军事防御
-- ============================================================================
events[#events + 1] = {
    id = "ra_wokou_attack",
    title = "倭寇侵袭",
    rankRange = { 5, 8 },
    weight = 5,
    cooldownMonths = 12,
    isDisaster = false,
    era = { 1368, 1600 },
    check = function(s)
        return GetArmySize() >= 100
    end,
    execute = function(s, report)
        local scale = GetAttackScale(s.clanRank)
        local allFighters = GetAllFighters()
        local armySize = GetArmySize()

        return {
            title = "倭寇侵袭",
            desc = "海疆急报！一支倭寇船队在附近登岸，约" ..
                   math.floor(400 * scale) .. "人，正向内陆劫掠。\n\n" ..
                   "官府征调各方乡勇协防，" .. s.surname .. "家作为当地望族责无旁贷。\n\n" ..
                   "可调用兵力：" .. armySize .. "人 | 将领" .. #allFighters .. "员",
            choices = {
                {
                    text = "主动出击截杀倭寇！（3D战斗·较高难度·高回报）",
                    effect = function()
                        if #allFighters < 2 then
                            GameData.AddResource("fame", -10)
                            GameData.AddLog("无将可用，未能出战抗倭，声望大损。")
                            return
                        end
                        local rival = GenerateAttacker("倭寇先锋", s.clanRank + 1, s.year)
                        local GS = require("UI.GameScreen")
                        GS.EventBattle(allFighters, rival, {
                            soldierCount = math.min(armySize, 500),
                            onSettle = function(result, rivalData, deployedIds)
                                if result == "victory" then
                                    local sr = math.floor(50 * scale)
                                    local fr = math.floor(25 * scale)
                                    GameData.AddResource("silver", sr)
                                    GameData.AddResource("fame", fr)
                                    GameData.AddLog("大破倭寇！缴获银两" .. sr .. "，声望+" .. fr .. "。朝廷嘉奖有功。")
                                    -- 连锁：朝廷嘉奖
                                    s.pendingEvents = s.pendingEvents or {}
                                    s.pendingEvents[#s.pendingEvents + 1] = {
                                        title = "抗倭功勋",
                                        desc = "朝廷闻报" .. s.surname .. "家奋勇抗倭，特遣使者前来嘉奖。\n\n" ..
                                               "钦差道：'圣上龙颜大悦，赐御笔亲书忠义之家匾额，另赏赐良田。'",
                                        choices = {
                                            {
                                                text = "领旨谢恩",
                                                effect = function()
                                                    GameData.AddResource("fame", 15)
                                                    GameData.AddResource("silver", 30)
                                                    GameData.AddLog("获朝廷赐匾'忠义之家'，赏银30两，声望+15。")
                                                end,
                                            },
                                        },
                                    }
                                else
                                    local sL = math.floor(40 * scale)
                                    local gL = math.floor(30 * scale)
                                    GameData.AddResource("silver", -sL)
                                    GameData.AddResource("grain", -gL)
                                    GameData.AddResource("fame", -5)
                                    GameData.AddLog("抗倭失利，倭寇趁势劫掠。损失银两" .. sL .. "、粮食" .. gL .. "。")
                                    -- 族人伤亡
                                    for _, mid in ipairs(deployedIds) do
                                        local m = GameData.GetMember(mid)
                                        if m and math.random(100) <= 12 then
                                            m.alive = false
                                            m.deathAge = m.age
                                            m.deathYear = s.year
                                            m.deathCause = "战死"
                                            GameData.AddLog(m.name .. "在抗倭战斗中壮烈殉国。")
                                            GameData.CheckPatriarchDeath(m)
                                            GameData.NotifyDeath(m, "战死")
                                        end
                                    end
                                end
                            end,
                        })
                    end,
                },
                {
                    text = "协助官军防守（出兵但不主攻，风险低）",
                    effect = function()
                        local grainCost = math.floor(20 * scale)
                        GameData.AddResource("grain", -grainCost)
                        GameData.AddResource("fame", 5)
                        -- 兵力损耗
                        local armyLoss = math.floor(50 * scale)
                        if s.army and s.army.infantry then
                            s.army.infantry = math.max(0, s.army.infantry - armyLoss)
                        end
                        GameData.AddLog("出兵协防抗倭，消耗粮食" .. grainCost .. "，损兵" .. armyLoss .. "人。朝廷记功，声望+5。")
                    end,
                },
                {
                    text = "全族撤入内陆避难（保人但丢资产）",
                    effect = function()
                        local silverLoss = math.floor(60 * scale)
                        local grainLoss = math.floor(40 * scale)
                        GameData.AddResource("silver", -silverLoss)
                        GameData.AddResource("grain", -grainLoss)
                        GameData.AddResource("fame", -12)
                        GameData.AddLog("举族避入内陆，庄园被倭寇洗劫。损失银两" .. silverLoss ..
                                       "、粮食" .. grainLoss .. "，声望-12。但族人无恙。")
                    end,
                },
            },
        }
    end,
}

-- ============================================================================
-- 事件3：流寇围城（品阶6-9，明末限定）
-- 李自成/张献忠式流民军围攻
-- ============================================================================
events[#events + 1] = {
    id = "ra_rebel_siege",
    title = "流寇围城",
    rankRange = { 6, 9 },
    weight = 6,
    cooldownMonths = 14,
    isDisaster = true,
    era = { 1620, 1644 },
    check = function(s)
        return #GameData.GetAliveMembers() >= 10 and GetArmySize() >= 200
    end,
    execute = function(s, report)
        local scale = GetAttackScale(s.clanRank)
        local allFighters = GetAllFighters()
        local armySize = GetArmySize()

        local leaderNames = {"闯王", "八大王", "过天星", "混天王", "扫地王"}
        local leaderName = leaderNames[math.random(1, #leaderNames)]

        return {
            title = "流寇围城",
            desc = "天下大乱！自号'" .. leaderName .. "'的流寇首领率众" ..
                   math.floor(800 * scale) .. "余人围困城池！\n\n" ..
                   "城内粮草仅够支撑数日，必须速做决断。\n\n" ..
                   "城防兵力：" .. armySize .. "人 | 将领" .. #allFighters .. "员\n" ..
                   "银两：" .. (s.silver or 0) .. " | 粮食：" .. (s.grain or 0),
            choices = {
                {
                    text = "死守城池，与城共存亡！（3D战斗·高难度·高回报）",
                    effect = function()
                        if #allFighters < 3 then
                            -- 将领不足直接城破
                            local sL = math.floor(100 * scale)
                            local gL = math.floor(80 * scale)
                            GameData.AddResource("silver", -sL)
                            GameData.AddResource("grain", -gL)
                            GameData.AddResource("fame", -15)
                            GameData.AddLog("将领不足，城池失守！" .. leaderName .. "部下大肆劫掠。")
                            return
                        end
                        local rival = GenerateAttacker(leaderName .. "部", s.clanRank + 1, s.year, "bandit")
                        -- 流寇兵力更多
                        rival.soldiers = math.floor(rival.soldiers * 1.5)
                        local GS = require("UI.GameScreen")
                        GS.EventBattle(allFighters, rival, {
                            soldierCount = math.min(armySize, 800),
                            onSettle = function(result, rivalData, deployedIds)
                                if result == "victory" then
                                    local sr = math.floor(80 * scale)
                                    local fr = math.floor(30 * scale)
                                    GameData.AddResource("silver", sr)
                                    GameData.AddResource("fame", fr)
                                    GameData.AddLog("浴血奋战，击退" .. leaderName .. "部！缴获银两" .. sr .. "，声望+" .. fr)
                                    -- 连锁：英雄事迹
                                    s.pendingEvents = s.pendingEvents or {}
                                    s.pendingEvents[#s.pendingEvents + 1] = {
                                        title = "守城英名",
                                        desc = s.surname .. "家死守城池击退流寇的事迹传遍四方，各地豪族纷纷来信致敬。\n\n" ..
                                               "有三家望族表示愿与" .. s.surname .. "家结盟，共抗乱世。",
                                        choices = {
                                            {
                                                text = "欣然结盟",
                                                effect = function()
                                                    GameData.AddResource("fame", 20)
                                                    GameData.AddResource("silver", 50)
                                                    GameData.AddLog("与三家望族结盟，获赠银两50，声望+20。")
                                                end,
                                            },
                                            {
                                                text = "婉拒结盟，独善其身",
                                                effect = function()
                                                    GameData.AddResource("fame", 10)
                                                    GameData.AddLog("婉拒结盟之请，但英名远播，声望+10。")
                                                end,
                                            },
                                        },
                                    }
                                else
                                    local sL = math.floor(120 * scale)
                                    local gL = math.floor(100 * scale)
                                    GameData.AddResource("silver", -sL)
                                    GameData.AddResource("grain", -gL)
                                    GameData.AddResource("fame", -20)
                                    -- 兵力大损
                                    if s.army then
                                        s.army.infantry = math.max(0, (s.army.infantry or 0) - math.floor(200 * scale))
                                        s.army.archers = math.max(0, (s.army.archers or 0) - math.floor(100 * scale))
                                    end
                                    GameData.AddLog("城池失守！" .. leaderName .. "部攻入，族产被大肆劫掠。")
                                    -- 族人伤亡
                                    for _, mid in ipairs(deployedIds) do
                                        local m = GameData.GetMember(mid)
                                        if m and math.random(100) <= 15 then
                                            m.alive = false
                                            m.deathAge = m.age
                                            m.deathYear = s.year
                                            m.deathCause = "战死"
                                            GameData.AddLog(m.name .. "在守城战中壮烈牺牲。")
                                            GameData.CheckPatriarchDeath(m)
                                            GameData.NotifyDeath(m, "战死")
                                        end
                                    end
                                end
                            end,
                        })
                    end,
                },
                {
                    text = "开城纳降，保全族人（损失惨重但无伤亡）",
                    effect = function()
                        local sL = math.floor(150 * scale)
                        local gL = math.floor(120 * scale)
                        local cL = math.floor(50 * scale)
                        GameData.AddResource("silver", -sL)
                        GameData.AddResource("grain", -gL)
                        GameData.AddResource("cloth", -cL)
                        GameData.AddResource("fame", -25)
                        -- 交出部分兵力
                        if s.army then
                            local armyTake = math.floor(GetArmySize() * 0.4)
                            s.army.infantry = math.max(0, (s.army.infantry or 0) - armyTake)
                        end
                        GameData.AddLog("开城纳降于" .. leaderName .. "。交出银两" .. sL ..
                                       "、粮食" .. gL .. "、布匹" .. cL .. "。声望-25。族人无恙。")
                    end,
                },
                {
                    text = "密道突围，弃城转移（保族人，丢固定资产）",
                    effect = function()
                        local sL = math.floor(80 * scale)
                        local gL = math.floor(60 * scale)
                        GameData.AddResource("silver", -sL)
                        GameData.AddResource("grain", -gL)
                        GameData.AddResource("fame", -10)
                        -- 丢失一个产业
                        if s.industries and #s.industries > 0 then
                            local idx = math.random(1, #s.industries)
                            local lostName = s.industries[idx].name or "产业"
                            table.remove(s.industries, idx)
                            GameData.AddLog("密道突围，弃城而走。失去" .. lostName ..
                                           "，损失银两" .. sL .. "、粮食" .. gL .. "。声望-10。")
                        else
                            GameData.AddLog("密道突围，弃城而走。损失银两" .. sL .. "、粮食" .. gL .. "。声望-10。")
                        end
                    end,
                },
            },
        }
    end,
}

-- ============================================================================
-- 事件4：地方豪强挑衅（品阶5-8）
-- 邻县豪族挑衅，争夺地盘和市场
-- ============================================================================
events[#events + 1] = {
    id = "ra_rival_provocation",
    title = "地方豪强挑衅",
    rankRange = { 5, 8 },
    weight = 8,
    cooldownMonths = 10,
    isDisaster = false,
    check = function(s)
        return s.industries and #s.industries >= 2
    end,
    execute = function(s, report)
        local scale = GetAttackScale(s.clanRank)
        local allFighters = GetAllFighters()
        local armySize = GetArmySize()

        local rivalSurnameList = {"马", "孙", "韩", "徐", "曹", "魏"}
        local rivalSn = rivalSurnameList[math.random(1, #rivalSurnameList)]
        if rivalSn == s.surname then
            rivalSn = "郑"
        end

        -- 选择被威胁的产业
        local targetIndustry = s.industries[math.random(1, #s.industries)]
        local industryName = targetIndustry.name or "产业"

        local armyLine = armySize > 0
            and ("\n\n可调兵力：" .. armySize .. "人 | 将领" .. #allFighters .. "员")
            or ("\n\n可调兵力：无 | 将领" .. #allFighters .. "员\n(!) 无兵可用，对方有护卫家丁，慎重应战！")

        return {
            title = "地方豪强挑衅",
            desc = "邻县" .. rivalSn .. "家派人送来战书：\n\n" ..
                   "'" .. s.surname .. "家据" .. industryName .. "已久，" ..
                   rivalSn .. "家今欲分一杯羹。若不允，便在战场上见真章！'\n\n" ..
                   "此事关乎家族在本地的声望和商路控制权，不可等闲视之。" .. armyLine,
            choices = {
                {
                    text = armySize > 0
                        and "应战！在战场上让他服气（3D战斗·中等难度）"
                        or "率族人应战！（3D战斗·无兵·风险较高）",
                    effect = function()
                        if #allFighters < 2 then
                            GameData.AddResource("fame", -8)
                            GameData.AddLog("无将可战，" .. rivalSn .. "家趁势霸占部分商路，声望-8。")
                            return
                        end
                        local rival = GenerateAttacker(rivalSn .. "家护卫", s.clanRank, s.year)
                        -- 豪强冲突规模较小，无兵时对方也只是护卫级别
                        if armySize == 0 then
                            rival.soldiers = math.max(50, math.floor(rival.soldiers * 0.3))
                        end
                        local GS = require("UI.GameScreen")
                        GS.EventBattle(allFighters, rival, {
                            soldierCount = math.min(armySize, 400),
                            onSettle = function(result, rivalData, deployedIds)
                                if result == "victory" then
                                    GameData.AddResource("fame", math.floor(15 * scale))
                                    GameData.AddResource("silver", math.floor(20 * scale))
                                    GameData.AddLog("击败" .. rivalSn .. "家！对方俯首认输，声望+" ..
                                                   math.floor(15 * scale) .. "。")
                                else
                                    GameData.AddResource("fame", -10)
                                    -- 失去产业的部分产出
                                    local outputLoss = math.floor(20 * scale)
                                    GameData.AddResource("silver", -outputLoss)
                                    GameData.AddLog("不敌" .. rivalSn .. "家，被迫让出部分商路。声望-10，银两-" .. outputLoss .. "。")
                                end
                                -- 双方都有伤亡
                                for _, mid in ipairs(deployedIds) do
                                    local m = GameData.GetMember(mid)
                                    if m and math.random(100) <= 8 then
                                        m.health = math.max(15, m.health - math.random(10, 25))
                                        GameData.AddLog(m.name .. "在械斗中受伤。")
                                    end
                                end
                            end,
                        })
                    end,
                },
                {
                    text = "请官府调停（花银子打官司）",
                    effect = function()
                        local cost = math.floor(35 * scale)
                        GameData.AddResource("silver", -cost)
                        -- 结果取决于声望
                        if (s.fame or 0) >= 100 then
                            GameData.AddResource("fame", 5)
                            GameData.AddLog("官府判" .. s.surname .. "家有理，" .. rivalSn ..
                                           "家退让。花费银两" .. cost .. "，声望+5。")
                        else
                            GameData.AddResource("fame", -3)
                            GameData.AddLog("官府各打五十大板。花费银两" .. cost .. "，" ..
                                           rivalSn .. "家仍时有骚扰。声望-3。")
                        end
                    end,
                },
                {
                    text = "主动示好结交（化敌为友）",
                    effect = function()
                        local giftCost = math.floor(25 * scale)
                        GameData.AddResource("silver", -giftCost)
                        -- 70%概率化敌为友
                        if math.random(100) <= 70 then
                            GameData.AddResource("fame", 3)
                            GameData.AddLog("赠礼示好，" .. rivalSn .. "家被诚意感动，两家化敌为友。银两-" ..
                                           giftCost .. "，声望+3。")
                        else
                            GameData.AddResource("fame", -5)
                            GameData.AddLog(rivalSn .. "家收了礼却依然蛮横，白白浪费银两" .. giftCost .. "。声望-5。")
                        end
                    end,
                },
            },
        }
    end,
}

-- ============================================================================
-- 事件5：土匪绑票（品阶5-7）
-- 族人被劫匪绑架，需要营救
-- ============================================================================
events[#events + 1] = {
    id = "ra_kidnapping",
    title = "土匪绑票",
    rankRange = { 5, 7 },
    weight = 6,
    cooldownMonths = 10,
    isDisaster = false,
    check = function(s)
        -- 至少有一个年轻族人可被绑架
        for _, m in ipairs(GameData.GetAliveMembers()) do
            if m.age >= 10 and m.age <= 30 then
                return true
            end
        end
        return false
    end,
    execute = function(s, report)
        local scale = GetAttackScale(s.clanRank)
        local allFighters = GetAllFighters()
        local armySize = GetArmySize()

        -- 选一个被绑架的族人（年轻人）
        local targets = {}
        for _, m in ipairs(GameData.GetAliveMembers()) do
            if m.age >= 10 and m.age <= 30 then
                targets[#targets + 1] = m
            end
        end
        local victim = targets[math.random(1, #targets)]
        local ransom = math.floor(50 * scale)

        local rescueHint = armySize > 0
            and ("可调兵力：" .. armySize .. "人")
            or "可调兵力：无（纯靠族人武艺营救）"

        return {
            title = "土匪绑票",
            desc = victim.name .. "外出时被一伙土匪劫持！\n\n" ..
                   "匪首传话：'若要人活着回来，速速备银两" .. ransom .. "两，" ..
                   "三日后送到十里亭。否则……'\n\n" ..
                   "族中一片哗然，" .. victim.name .. "性命攸关！\n" .. rescueHint,
            choices = {
                {
                    text = "亲率精锐营救！（3D战斗·中低难度·有风险）",
                    effect = function()
                        if #allFighters < 2 then
                            -- 无人可派，只能交赎金
                            GameData.AddResource("silver", -ransom)
                            GameData.AddLog("无人可派营救，被迫交赎金" .. ransom .. "两赎回" .. victim.name .. "。")
                            return
                        end
                        local rival = GenerateAttacker("绑匪巢穴", s.clanRank - 1, s.year, "bandit")
                        local GS = require("UI.GameScreen")
                        GS.EventBattle(allFighters, rival, {
                            soldierCount = math.min(GetArmySize(), 200),
                            onSettle = function(result, rivalData, deployedIds)
                                if result == "victory" then
                                    local sr = math.floor(20 * scale)
                                    GameData.AddResource("silver", sr)
                                    GameData.AddResource("fame", 10)
                                    GameData.AddLog("攻破匪巢，救出" .. victim.name .. "！缴获银两" .. sr .. "，声望+10。")
                                    victim.health = math.max(30, victim.health - 10)
                                else
                                    -- 营救失败，人质受伤
                                    victim.health = math.max(10, victim.health - math.random(20, 40))
                                    GameData.AddResource("silver", -math.floor(ransom * 0.5))
                                    GameData.AddLog("营救失利！" .. victim.name .. "伤势加重，被迫追加赎金" ..
                                                   math.floor(ransom * 0.5) .. "两。")
                                    -- 出战族人伤亡
                                    for _, mid in ipairs(deployedIds) do
                                        local m = GameData.GetMember(mid)
                                        if m and math.random(100) <= 10 then
                                            m.health = math.max(15, m.health - math.random(10, 20))
                                        end
                                    end
                                end
                            end,
                        })
                    end,
                },
                {
                    text = "交赎金赎人（银两-" .. ransom .. "）",
                    effect = function()
                        GameData.AddResource("silver", -ransom)
                        victim.health = math.max(30, victim.health - 5)
                        GameData.AddLog("交出赎金" .. ransom .. "两，" .. victim.name .. "平安归来。")
                    end,
                },
                {
                    text = "报官追剿（耗时但可能免费）",
                    effect = function()
                        -- 50%官兵成功 30%赎金交涉 20%撕票
                        local roll = math.random(100)
                        if roll <= 50 then
                            GameData.AddResource("fame", 3)
                            GameData.AddLog("官兵出动围剿匪巢，" .. victim.name .. "被成功营救。声望+3。")
                        elseif roll <= 80 then
                            local partialRansom = math.floor(ransom * 0.6)
                            GameData.AddResource("silver", -partialRansom)
                            GameData.AddLog("官兵迟迟不动，最终由中间人交涉赎回" .. victim.name ..
                                           "，花费银两" .. partialRansom .. "。")
                        else
                            -- 撕票
                            victim.health = math.max(5, victim.health - math.random(30, 50))
                            GameData.AddResource("fame", -5)
                            GameData.AddLog("官兵久等不至，" .. victim.name .. "遭匪徒毒打后被弃于路边。伤势严重！声望-5。")
                        end
                    end,
                },
            },
        }
    end,
}

-- ============================================================================
-- 事件6：宗族世仇复仇（品阶6-9）
-- 多年前结下的宗族仇恨，对方集结力量来报仇
-- ============================================================================
events[#events + 1] = {
    id = "ra_blood_feud",
    title = "宗族世仇",
    rankRange = { 6, 9 },
    weight = 5,
    cooldownMonths = 18,
    isDisaster = false,
    check = function(s)
        return #GameData.GetAliveMembers() >= 12 and GetArmySize() >= 300
    end,
    execute = function(s, report)
        local scale = GetAttackScale(s.clanRank)
        local allFighters = GetAllFighters()
        local fighterNames = GetFighterNames(allFighters, 6)
        local armySize = GetArmySize()

        local enemySurnameList = {"马", "孙", "韩", "徐", "曹"}
        local enemySn = enemySurnameList[math.random(1, #enemySurnameList)]
        if enemySn == s.surname then enemySn = "魏" end

        return {
            title = "宗族世仇",
            desc = enemySn .. "家老族长含恨而终前留下遗言，命后辈务必向" ..
                   s.surname .. "家讨回血债。\n\n" ..
                   "如今" .. enemySn .. "家少主继位，集结" ..
                   math.floor(600 * scale) .. "精兵强将誓要报仇雪恨！\n\n" ..
                   "斥候来报：" .. enemySn .. "家先锋已到五十里外，三日内必至！\n\n" ..
                   "己方兵力：" .. armySize .. "人 | 将领：" .. table.concat(fighterNames, "、"),
            choices = {
                {
                    text = "全力迎战！不让世仇踏入家门半步（3D战斗·高难度）",
                    effect = function()
                        if #allFighters < 3 then
                            GameData.AddResource("silver", -math.floor(80 * scale))
                            GameData.AddResource("fame", -15)
                            GameData.AddLog("将少兵弱，被" .. enemySn .. "家长驱直入，遭受重创。")
                            return
                        end
                        local rival = GenerateAttacker(enemySn .. "家复仇军", s.clanRank, s.year)
                        rival.soldiers = math.floor(rival.soldiers * 1.3)
                        local GS = require("UI.GameScreen")
                        GS.EventBattle(allFighters, rival, {
                            soldierCount = math.min(armySize, 600),
                            onSettle = function(result, rivalData, deployedIds)
                                if result == "victory" then
                                    local fr = math.floor(25 * scale)
                                    GameData.AddResource("fame", fr)
                                    GameData.AddResource("silver", math.floor(40 * scale))
                                    GameData.AddLog("大败" .. enemySn .. "家！世仇就此了结，声望大增+" .. fr .. "！")
                                    -- 连锁：和解或赶尽杀绝
                                    s.pendingEvents = s.pendingEvents or {}
                                    s.pendingEvents[#s.pendingEvents + 1] = {
                                        title = "世仇余波",
                                        desc = enemySn .. "家惨败，少主跪地求和。\n\n" ..
                                               "族中长辈道：'斩草除根方能永绝后患。'\n" ..
                                               "也有人说：'得饶人处且饶人，冤冤相报何时了。'",
                                        choices = {
                                            {
                                                text = "饶其性命，化敌为友",
                                                effect = function()
                                                    GameData.AddResource("fame", 10)
                                                    GameData.AddLog("饶恕" .. enemySn .. "家，两族从此化干戈为玉帛。声望+10。")
                                                end,
                                            },
                                            {
                                                text = "趁势追击，彻底瓦解对方",
                                                effect = function()
                                                    local sr = math.floor(60 * scale)
                                                    GameData.AddResource("silver", sr)
                                                    GameData.AddResource("fame", -5)
                                                    GameData.AddLog("趁胜追击，瓦解" .. enemySn .. "家势力。掠得银两" ..
                                                                   sr .. "，但手段酷烈，声望-5。")
                                                end,
                                            },
                                        },
                                    }
                                else
                                    local sL = math.floor(100 * scale)
                                    local gL = math.floor(70 * scale)
                                    GameData.AddResource("silver", -sL)
                                    GameData.AddResource("grain", -gL)
                                    GameData.AddResource("fame", -15)
                                    GameData.AddLog(enemySn .. "家得势，" .. s.surname ..
                                                   "家遭到报复性劫掠。损失惨重。")
                                    -- 大量族人伤亡
                                    for _, mid in ipairs(deployedIds) do
                                        local m = GameData.GetMember(mid)
                                        if m and math.random(100) <= 12 then
                                            m.alive = false
                                            m.deathAge = m.age
                                            m.deathYear = s.year
                                            m.deathCause = "战死"
                                            GameData.AddLog(m.name .. "在宗族仇战中阵亡。")
                                            GameData.CheckPatriarchDeath(m)
                                            GameData.NotifyDeath(m, "战死")
                                        end
                                    end
                                end
                            end,
                        })
                    end,
                },
                {
                    text = "派使者谈判求和（花重金消弭仇恨）",
                    effect = function()
                        local cost = math.floor(80 * scale)
                        GameData.AddResource("silver", -cost)
                        -- 60%成功
                        if math.random(100) <= 60 then
                            GameData.AddResource("fame", 5)
                            GameData.AddLog("赠银两" .. cost .. "求和，" .. enemySn ..
                                           "家接受赔偿，世仇暂息。声望+5。")
                        else
                            GameData.AddResource("fame", -8)
                            GameData.AddLog(enemySn .. "家拒绝求和，银两" .. cost ..
                                           "白白浪费。声望-8。对方扬言下次必来。")
                        end
                    end,
                },
                {
                    text = "联合友族共同抵抗（借兵助战）",
                    effect = function()
                        -- 需声望≥80且花费粮食
                        local grainCost = math.floor(40 * scale)
                        if (s.fame or 0) >= 80 then
                            GameData.AddResource("grain", -grainCost)
                            GameData.AddResource("fame", 8)
                            GameData.AddLog("凭借声望联合友族出兵相助，合力击退" .. enemySn ..
                                           "家。消耗粮食" .. grainCost .. "，声望+8。")
                        else
                            GameData.AddResource("grain", -grainCost)
                            GameData.AddResource("fame", -3)
                            GameData.AddLog("声望不足，友族只派少量援兵。勉强抵挡" .. enemySn ..
                                           "家进攻，但损失不小。消耗粮食" .. grainCost .. "。")
                        end
                    end,
                },
            },
        }
    end,
}

-- ============================================================================
-- 事件7：蒙古骑兵南侵（品阶7-9，早期年代限定）
-- 北方骑兵南下劫掠
-- ============================================================================
events[#events + 1] = {
    id = "ra_mongol_raid",
    title = "蒙古骑兵南侵",
    rankRange = { 7, 9 },
    weight = 4,
    cooldownMonths = 18,
    isDisaster = true,
    era = { 1368, 1500 },
    check = function(s)
        return GetArmySize() >= 500
    end,
    execute = function(s, report)
        local scale = GetAttackScale(s.clanRank)
        local allFighters = GetAllFighters()
        local armySize = GetArmySize()

        return {
            title = "蒙古骑兵南侵",
            desc = "北疆急报！一支蒙古铁骑约" .. math.floor(500 * scale) ..
                   "骑突破边防，长驱直入！\n\n" ..
                   "沿途州县已有数处被劫，骑兵来势汹汹，直奔此地而来。\n\n" ..
                   "朝廷调令：各大家族即刻动员乡勇，据城死守！\n\n" ..
                   "可用兵力：" .. armySize .. "人",
            choices = {
                {
                    text = "率军出城野战！以骑制骑（3D战斗·极高难度·极高回报）",
                    effect = function()
                        if #allFighters < 4 then
                            GameData.AddResource("fame", -10)
                            GameData.AddLog("将领不足，无力出城野战。龟缩城中等骑兵退去。")
                            return
                        end
                        local rival = GenerateAttacker("蒙古骑兵", s.clanRank + 1, s.year)
                        rival.soldiers = math.floor(rival.soldiers * 1.4)
                        local GS = require("UI.GameScreen")
                        GS.EventBattle(allFighters, rival, {
                            soldierCount = math.min(armySize, 800),
                            onSettle = function(result, rivalData, deployedIds)
                                if result == "victory" then
                                    local sr = math.floor(100 * scale)
                                    local fr = math.floor(40 * scale)
                                    GameData.AddResource("silver", sr)
                                    GameData.AddResource("fame", fr)
                                    GameData.AddLog("大破蒙古骑兵！缴获战马财帛无数，银两+" .. sr .. "，声望+" .. fr .. "！")
                                    -- 连锁：朝廷封赏
                                    s.pendingEvents = s.pendingEvents or {}
                                    s.pendingEvents[#s.pendingEvents + 1] = {
                                        title = "北疆封赏",
                                        desc = "朝廷闻" .. s.surname .. "家于野战中大破蒙古骑兵，\n" ..
                                               "兵部尚书亲自上奏请封赏，圣上恩准。",
                                        choices = {
                                            {
                                                text = "领旨谢恩",
                                                effect = function()
                                                    GameData.AddResource("fame", 25)
                                                    GameData.AddResource("silver", 80)
                                                    GameData.AddLog("获朝廷重赏！赐银八十两，声望+25。")
                                                end,
                                            },
                                        },
                                    }
                                else
                                    local sL = math.floor(130 * scale)
                                    local gL = math.floor(100 * scale)
                                    GameData.AddResource("silver", -sL)
                                    GameData.AddResource("grain", -gL)
                                    GameData.AddResource("fame", -15)
                                    if s.army then
                                        s.army.infantry = math.max(0, (s.army.infantry or 0) - math.floor(300 * scale))
                                    end
                                    GameData.AddLog("野战失利，蒙古骑兵大肆劫掠。兵力和物资损失惨重。")
                                    for _, mid in ipairs(deployedIds) do
                                        local m = GameData.GetMember(mid)
                                        if m and math.random(100) <= 15 then
                                            m.alive = false
                                            m.deathAge = m.age
                                            m.deathYear = s.year
                                            m.deathCause = "战死"
                                            GameData.AddLog(m.name .. "在对蒙古骑兵的野战中阵亡。")
                                            GameData.CheckPatriarchDeath(m)
                                            GameData.NotifyDeath(m, "战死")
                                        end
                                    end
                                end
                            end,
                        })
                    end,
                },
                {
                    text = "据城坚守等援军（消耗粮草，较安全）",
                    effect = function()
                        local grainCost = math.floor(60 * scale)
                        GameData.AddResource("grain", -grainCost)
                        -- 骑兵不善攻城，80%概率安全
                        if math.random(100) <= 80 then
                            GameData.AddResource("fame", 5)
                            GameData.AddLog("据城坚守，蒙古骑兵围城数日后退去。消耗粮食" ..
                                           grainCost .. "，声望+5。")
                        else
                            local silverLoss = math.floor(50 * scale)
                            GameData.AddResource("silver", -silverLoss)
                            GameData.AddLog("城外村落被洗劫，损失银两" .. silverLoss ..
                                           "、粮食" .. grainCost .. "。")
                        end
                    end,
                },
                {
                    text = "焦土策略：烧毁城外物资，令骑兵无以为继",
                    effect = function()
                        -- 自毁部分资源，但骑兵必退
                        local silverBurn = math.floor(30 * scale)
                        local grainBurn = math.floor(50 * scale)
                        GameData.AddResource("silver", -silverBurn)
                        GameData.AddResource("grain", -grainBurn)
                        GameData.AddResource("fame", 8)
                        GameData.AddLog("下令焚毁城外粮仓物资，蒙古骑兵无法就地补给，被迫退兵。" ..
                                       "损失银两" .. silverBurn .. "、粮食" .. grainBurn ..
                                       "，但计策奏效，声望+8。")
                    end,
                },
            },
        }
    end,
}

-- ============================================================================
-- 事件8：江湖仇杀（品阶6-8）
-- 江湖势力寻仇，针对高武艺族人
-- ============================================================================
events[#events + 1] = {
    id = "ra_jianghu_vendetta",
    title = "江湖仇杀",
    rankRange = { 6, 8 },
    weight = 6,
    cooldownMonths = 10,
    isDisaster = false,
    check = function(s)
        -- 需有武艺>50的族人
        for _, m in ipairs(GameData.GetAliveMembers()) do
            if m.martial and m.martial > 50 then
                return true
            end
        end
        return false
    end,
    execute = function(s, report)
        local scale = GetAttackScale(s.clanRank)
        local allFighters = GetAllFighters()

        -- 找到武艺最高的族人作为目标
        local targetMember = nil
        local maxMartial = 0
        for _, m in ipairs(GameData.GetAliveMembers()) do
            if m.martial and m.martial > maxMartial and m.gender == "male" then
                maxMartial = m.martial
                targetMember = m
            end
        end

        if not targetMember then
            return nil
        end

        local orgNames = {"血刀门", "毒蛇帮", "黑风堂", "五毒教", "铁掌帮"}
        local orgName = orgNames[math.random(1, #orgNames)]

        return {
            title = "江湖仇杀",
            desc = "一队黑衣蒙面人夜闯庄园，声称是" .. orgName .. "的人！\n\n" ..
                   "为首者道：'" .. targetMember.name .. "曾在江湖上坏了我们的好事，" ..
                   "今日特来讨个公道！要么交出此人，要么全族陪葬！'\n\n" ..
                   targetMember.name .. "（武艺" .. targetMember.martial .. "）镇定自若，" ..
                   "拔刀待战。",
            choices = {
                {
                    text = targetMember.name .. "亲自迎战！（3D战斗·取决于武艺）",
                    effect = function()
                        local rival = GenerateAttacker(orgName .. "杀手", s.clanRank - 1, s.year)
                        -- 江湖人武艺高但兵少
                        rival.soldiers = math.max(100, math.floor(rival.soldiers * 0.4))
                        for _, rm in ipairs(rival.members) do
                            rm.martial = math.min(100, rm.martial + 10)
                        end
                        local GS = require("UI.GameScreen")
                        GS.EventBattle(allFighters, rival, {
                            soldierCount = 0,  -- 纯将领对决
                            onSettle = function(result, rivalData, deployedIds)
                                if result == "victory" then
                                    GameData.AddResource("fame", math.floor(12 * scale))
                                    GameData.AddLog(targetMember.name .. "以一敌众，击退" .. orgName ..
                                                   "杀手！武名远播，声望+" .. math.floor(12 * scale) .. "。")
                                else
                                    targetMember.health = math.max(5, targetMember.health - math.random(25, 45))
                                    GameData.AddResource("fame", -5)
                                    GameData.AddLog(targetMember.name .. "不敌" .. orgName ..
                                                   "高手，身负重伤！声望-5。")
                                    -- 其他出战人伤亡
                                    for _, mid in ipairs(deployedIds) do
                                        if mid ~= targetMember.id then
                                            local m = GameData.GetMember(mid)
                                            if m and math.random(100) <= 20 then
                                                m.health = math.max(10, m.health - math.random(15, 30))
                                            end
                                        end
                                    end
                                end
                            end,
                        }, { preSelectedIds = { targetMember.id }, maxDeploy = 3, title = "选择助战族人" })
                    end,
                },
                {
                    text = "重金请退（花银两买平安）",
                    effect = function()
                        local bribe = math.floor(45 * scale)
                        GameData.AddResource("silver", -bribe)
                        -- 80%离去
                        if math.random(100) <= 80 then
                            GameData.AddLog("拿出银两" .. bribe .. "打点" .. orgName ..
                                           "来人，对方收钱离去，此事暂了。")
                        else
                            GameData.AddResource("fame", -5)
                            GameData.AddLog(orgName .. "收钱后仍出手伤了一名下人，扬长而去。银两-" ..
                                           bribe .. "，声望-5。")
                        end
                    end,
                },
                {
                    text = "报请官府缉拿（官方解决）",
                    effect = function()
                        local cost = math.floor(20 * scale)
                        GameData.AddResource("silver", -cost)
                        -- 江湖人闻风先逃，但之后可能再来
                        if math.random(100) <= 55 then
                            GameData.AddResource("fame", 3)
                            GameData.AddLog("报官后官兵出动，" .. orgName .. "来人仓皇遁走。花费银两" ..
                                           cost .. "，声望+3。但恐其日后再来。")
                        else
                            GameData.AddLog("官兵赶到前" .. orgName .. "已溜走，白花银两" .. cost .. "。")
                        end
                    end,
                },
            },
        }
    end,
}

return events
