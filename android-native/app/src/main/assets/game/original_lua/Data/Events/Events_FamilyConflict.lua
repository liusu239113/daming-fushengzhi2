-- ============================================================================
-- 大明浮生志2 - 家族内斗系统 (C3)
-- 品级≥7触发的家族内部冲突事件池
-- 特点：族人对抗、可触发3D战斗、深层决策、长期后果
-- ============================================================================

local GameData = require("Data.GameData")
local RivalClans = require("Data.RivalClans")

local events = {}

-- ============================================================================
-- 辅助函数
-- ============================================================================

--- 获取所有可用战士（用于内斗战斗）
local function GetAllFighters()
    local candidates = {}
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.gender == "male" and m.age >= 16 and m.age <= 55
           and m.health > 20 and m.state ~= "生病" then
            candidates[#candidates + 1] = m
        end
    end
    table.sort(candidates, function(a, b) return (a.martial or 0) > (b.martial or 0) end)
    return candidates
end

--- 获取前N个战士的名字列表（用于描述文本）
local function GetFighterNames(fighters, count)
    local names = {}
    for i = 1, math.min(count or 4, #fighters) do
        names[#names + 1] = fighters[i].name
    end
    return names
end

--- 根据品级获取内斗强度系数
local function GetConflictScale(rank)
    local scales = { [7] = 1.0, [8] = 1.5, [9] = 2.0 }
    return scales[rank] or 1.0
end

--- 随机选取一个非核心族人作为叛逆者
local function PickRebel(exclude)
    local candidates = {}
    for _, m in ipairs(GameData.GetAliveMembers()) do
        if m.gender == "male" and m.age >= 18 and m.age <= 55
           and m.health > 30 and m.state ~= "生病"
           and not m.isFounder then
            local excluded = false
            if exclude then
                for _, eid in ipairs(exclude) do
                    if m.id == eid then excluded = true; break end
                end
            end
            if not excluded then
                candidates[#candidates + 1] = m
            end
        end
    end
    if #candidates == 0 then return nil end
    return candidates[math.random(1, #candidates)]
end

--- 生成叛逆方的战斗数据（基于叛逆族人属性生成对应rival）
local function GenerateRebelRival(rebelName, rebelMartial, rank)
    local scale = GetConflictScale(rank)
    local baseMartial = rebelMartial or 40
    -- 叛逆者拉拢的外部势力
    local memberCount = math.random(2, math.floor(3 + scale))
    local members = {}
    local givenNames = {"虎", "豹", "彪", "龙", "猛", "刚", "坚", "毅", "烈", "威"}

    -- 首领就是叛逆族人本人
    members[1] = {
        name = rebelName,
        gender = "male",
        age = math.random(25, 45),
        title = "叛首",
        martial = math.min(100, baseMartial + 10),
        health = math.random(70, 95),
        isLeader = true,
        memberIndex = 1,
    }
    -- 跟随者：外部招募的打手
    for j = 2, memberCount do
        local sn = ({"陈", "杨", "周", "吴", "郑"})[math.random(1, 5)]
        members[j] = {
            name = sn .. givenNames[math.random(1, #givenNames)],
            gender = "male",
            age = math.random(18, 40),
            martial = math.random(math.floor(baseMartial * 0.5), math.floor(baseMartial * 0.8)),
            health = math.random(50, 75),
            isLeader = false,
            memberIndex = j,
        }
    end

    local soldiers = math.floor(math.random(100, 300) * scale)
    soldiers = math.floor(soldiers / 100) * 100
    if soldiers < 100 then soldiers = 100 end

    return {
        id = 0,
        name = rebelName .. "叛军",
        surname = string.sub(rebelName, 1, 3), -- utf8 first char
        tierId = "normal",
        tierName = "叛逆",
        tierColor = { 200, 60, 60, 255 },
        tierDesc = "家族内斗",
        members = members,
        soldiers = soldiers,
        rewards = {
            silver = math.floor(10 * scale),
            grain = math.floor(5 * scale),
            fame = math.floor(8 * scale),
        },
    }
end

-- ============================================================================
-- 1. 庶子夺权 —— 族中有野心的庶子/旁支谋反夺权
-- ============================================================================
events[#events + 1] = {
    id = "fc_usurper_revolt",
    title = "庶子夺权",
    rankRange = {7, 9},
    weight = 9,
    cooldownMonths = 12,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        return #adults >= 8
    end,
    execute = function(s, report)
        local scale = GetConflictScale(s.clanRank)
        local rebel = PickRebel(nil)
        if not rebel then
            return nil -- 无合适叛逆者，不触发
        end

        local allFighters = GetAllFighters()
        -- 排除叛逆者后的可战族人（用于人数检查和描述文本）
        local loyalFighters = {}
        for _, f in ipairs(allFighters) do
            if f.id ~= rebel.id then
                loyalFighters[#loyalFighters + 1] = f
            end
        end
        local fighterNames = GetFighterNames(loyalFighters, 3)

        local rebelMartial = rebel.martial or 30
        local silverBribe = math.floor(50 * scale)
        local silverDivide = math.floor(35 * scale)
        local fameLoss = math.floor(15 * scale)
        local fameWin = math.floor(20 * scale)

        report.events[#report.events + 1] = rebel.name .. "暗中勾结外人，图谋夺取族权。"
        GameData.AddLog(rebel.name .. "密谋叛乱，欲夺取族中大权！")

        return {
            title = "庶子夺权",
            desc = "族中" .. rebel.name .. "（武艺" .. rebelMartial .. "）暗中勾结外部势力，"
                .. "招募打手、煽动不满族人，意图强行夺取族权！\n\n"
                .. "此人已控制了部分族产，若不速速处置，恐酿成大祸。\n"
                .. "一旦叛成，族中产业将被瓜分、声望尽失。\n\n"
                .. "可用之将：" .. (table.concat(fighterNames, "、") or "无"),
            choices = {
                -- 选项1：武力镇压（3D战斗）
                { text = "武力镇压叛逆！（战斗）", effect = function()
                    if #loyalFighters == 0 then
                        -- 无人可战，直接惨败
                        local silverLoss = math.floor(80 * scale)
                        local grainLoss = math.floor(40 * scale)
                        GameData.AddResource("silver", -silverLoss)
                        GameData.AddResource("grain", -grainLoss)
                        GameData.AddResource("fame", -fameLoss * 2)
                        GameData.AddLog("族中无人可战，" .. rebel.name .. "叛乱得逞！损失惨重。")
                        -- 叛逆者出走并带走资产
                        rebel.alive = false
                        rebel.deathCause = "出走"
                        return
                    end

                    local rival = GenerateRebelRival(rebel.name, rebelMartial, s.clanRank)
                    local GS = require("UI.GameScreen")
                    GS.EventBattle(allFighters, rival, {
                        soldierCount = 0,
                        onSettle = function(result, rivalData, deployedIds)
                            if result == "victory" then
                                GameData.AddResource("fame", fameWin)
                                GameData.AddLog("镇压叛乱成功！" .. rebel.name .. "被擒获，族权稳固。声望+" .. fameWin)

                                -- 叛逆者被惩罚：禁闭（生病状态模拟）
                                rebel.health = math.max(1, rebel.health - 30)
                                if rebel.state ~= "生病" then rebel.prevState = rebel.state end
                                rebel.state = "生病"
                                -- 胜利后续：可选择如何处置叛逆者
                                s.pendingEvents[#s.pendingEvents + 1] = {
                                    title = "处置叛逆",
                                    desc = rebel.name .. "叛乱已被镇压。此人虽是族中骨肉，但犯下大逆不道之罪。当如何处置？",
                                    choices = {
                                        { text = "逐出族门、永不相认", effect = function()
                                            rebel.alive = false
                                            rebel.deathCause = "出走"
                                            GameData.AddResource("fame", 10)
                                            GameData.AddLog(rebel.name .. "被逐出家族，永不许归。")
                                        end },
                                        { text = "念及骨肉、从轻发落", effect = function()
                                            GameData.AddResource("fame", -5)
                                            GameData.AddLog(rebel.name .. "被软禁在家，罚去一年俸禄。")
                                        end },
                                        { text = "严刑拷打、以儆效尤", effect = function()
                                            rebel.alive = false
                                            rebel.deathCause = "私刑"
                                            rebel.deathAge = rebel.age
                                            rebel.deathYear = s.year
                                            GameData.AddResource("fame", -10)
                                            GameData.AddLog(rebel.name .. "被施以私刑，死于族规惩戒。")
                                        end },
                                    }
                                }
                            else
                                -- 战败：叛逆者控制部分族产
                                local silverLoss = math.floor(60 * scale)
                                local grainLoss = math.floor(30 * scale)
                                GameData.AddResource("silver", -silverLoss)
                                GameData.AddResource("grain", -grainLoss)
                                GameData.AddResource("fame", -fameLoss)
                                GameData.AddLog("镇压失败！" .. rebel.name ..
                                    "夺走部分族产后出走。银-" .. silverLoss .. "、声望-" .. fameLoss)
                                rebel.alive = false
                                rebel.deathCause = "出走"
                            end
                            -- 出战族人伤亡
                            for _, memberId in ipairs(deployedIds) do
                                local member = GameData.GetMember(memberId)
                                if member and math.random(100) <= ((result == "victory") and 15 or 45) then
                                    local loss = math.random(5, 20)
                                    member.health = math.max(1, member.health - loss)
                                    if member.health <= 10 then
                                        if member.state ~= "生病" then member.prevState = member.state end
                                        member.state = "生病"
                                    end
                                end
                            end
                        end,
                    }, { excludeIds = { rebel.id } })
                end },
                -- 选项2：金钱安抚
                { text = "重金安抚、分给" .. rebel.name .. "产业（-" .. silverBribe .. "银两）",
                  effect = function()
                    GameData.AddResource("silver", -silverBribe)
                    GameData.AddResource("fame", -5)
                    -- 60%概率安抚成功
                    if math.random(100) <= 60 then
                        GameData.AddLog("重金安抚" .. rebel.name .. "，暂时平息叛乱。")
                    else
                        GameData.AddResource("silver", -math.floor(silverBribe * 0.5))
                        GameData.AddResource("fame", -fameLoss)
                        GameData.AddLog(rebel.name .. "收钱后仍不满足，变本加厉！额外损失银两。")
                    end
                end },
                -- 选项3：同意分家
                { text = "同意分家、各立门户（-" .. silverDivide .. "银，失去族人）",
                  effect = function()
                    GameData.AddResource("silver", -silverDivide)
                    rebel.alive = false
                    rebel.deathCause = "分家"
                    -- 额外带走1-2个族人
                    local lostCount = 0
                    for _, m in ipairs(GameData.GetAliveMembers()) do
                        if m.id ~= rebel.id and not m.isFounder and m.age >= 16
                           and math.random(100) <= 15 and lostCount < 2 then
                            m.alive = false
                            m.deathCause = "分家"
                            lostCount = lostCount + 1
                            GameData.AddLog(m.name .. "随" .. rebel.name .. "分家而去。")
                        end
                    end
                    GameData.AddResource("fame", -math.floor(fameLoss * 0.5))
                    GameData.AddLog(rebel.name .. "分家另立门户，带走银两" .. silverDivide .. "及族人" .. lostCount .. "口。")
                end },
            }
        }
    end,
}

-- ============================================================================
-- 2. 管事侵吞 —— 产业管事串通侵吞族产
-- ============================================================================
events[#events + 1] = {
    id = "fc_steward_embezzle",
    title = "管事侵吞",
    rankRange = {7, 9},
    weight = 9,
    cooldownMonths = 8,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #s.industries >= 5
    end,
    execute = function(s, report)
        local scale = GetConflictScale(s.clanRank)
        local indCount = #s.industries
        local stolenSilver = math.floor(indCount * 5 * scale)
        local stolenGrain = math.floor(indCount * 3 * scale)
        local investigateCost = math.floor(25 * scale)
        local fameGain = math.floor(12 * scale)

        -- 选一个产业作为主谋据点
        local targetInd = s.industries[math.random(1, #s.industries)]
        local targetType = GameData.GetIndustryType(targetInd.typeId)
        local targetName = targetType and targetType.name or "产业"

        report.events[#report.events + 1] = "发现产业管事暗中侵吞族产。"
        GameData.AddLog("族中" .. targetName .. "管事疑似侵吞族产！")

        return {
            title = "管事侵吞",
            desc = "族中" .. targetName .. "等" .. indCount .. "处产业的管事们暗中串通，"
                .. "多年来以虚报账目、偷工减料等手段侵吞族产。\n\n"
                .. "经初步查实，已被侵吞银两约" .. stolenSilver
                .. "两、粮食" .. stolenGrain .. "石。\n\n"
                .. "管事们根基深厚、势力盘根错节，处置不当恐影响产业运转。",
            choices = {
                -- 选项1：严查到底，杀一儆百
                { text = "严查到底、追缴赃银（-" .. investigateCost .. "银查办费）",
                  effect = function()
                    GameData.AddResource("silver", -investigateCost)
                    -- 追回部分赃款
                    local recovered = math.floor(stolenSilver * 0.6)
                    GameData.AddResource("silver", recovered)
                    GameData.AddResource("fame", fameGain)
                    GameData.AddLog("严查管事贪墨，追回赃银" .. recovered
                        .. "两。声望+" .. fameGain)
                    -- 但产业效率临时下降（通过扣除一次产出模拟）
                    local outputLoss = math.floor(15 * scale)
                    GameData.AddResource("grain", -outputLoss)
                    GameData.AddLog("整顿期间产业运转受阻，粮食-" .. outputLoss)
                end },
                -- 选项2：换人整顿，温和处理
                { text = "逐步换人整顿、避免动荡",
                  effect = function()
                    -- 追回小部分
                    local recovered = math.floor(stolenSilver * 0.25)
                    GameData.AddResource("silver", recovered)
                    GameData.AddResource("fame", math.floor(fameGain * 0.4))
                    GameData.AddLog("逐步替换管事，追回部分赃银" .. recovered .. "两，产业运转如常。")
                end },
                -- 选项3：暗中掌握把柄、以此要挟
                { text = "留中不发、暗握把柄以后利用",
                  effect = function()
                    -- 管事感恩，短期产出增加
                    local bonusGrain = math.floor(stolenGrain * 0.5)
                    GameData.AddResource("grain", bonusGrain)
                    GameData.AddResource("fame", -8)
                    GameData.AddLog("暗握管事把柄不发，管事们投桃报李、产出增加。粮食+" .. bonusGrain)
                    -- 但30%概率日后败露
                    if math.random(100) <= 30 then
                        s.pendingEvents[#s.pendingEvents + 1] = {
                            title = "包庇败露",
                            desc = "管事侵吞之事终究败露于族老面前。"
                                .. "族老震怒，质问族长明知贪墨却包庇纵容，"
                                .. "族中威信大损。",
                            choices = { { text = "无言以对", effect = function()
                                GameData.AddResource("fame", -math.floor(20 * scale))
                                GameData.AddLog("包庇管事之事败露，族中威信大跌。")
                            end } }
                        }
                    end
                end },
            }
        }
    end,
}

-- ============================================================================
-- 3. 分家危机 —— 族人过多时旁支要求分家，处理不当触发战斗
-- ============================================================================
events[#events + 1] = {
    id = "fc_family_split",
    title = "分家危机",
    rankRange = {7, 9},
    weight = 8,
    cooldownMonths = 14,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local alive = GameData.GetAliveMembers()
        return #alive >= 20
    end,
    execute = function(s, report)
        local scale = GetConflictScale(s.clanRank)
        local alive = GameData.GetAliveMembers()
        local memberCount = #alive

        -- 选出分家势力的领头人
        local rebel = PickRebel(nil)
        if not rebel then return nil end

        local allFighters = GetAllFighters()
        local loyalFighters = {}
        for _, f in ipairs(allFighters) do
            if f.id ~= rebel.id then
                loyalFighters[#loyalFighters + 1] = f
            end
        end

        local silverSplit = math.floor(60 * scale)
        local grainSplit = math.floor(40 * scale)
        local fameLoss = math.floor(20 * scale)

        -- 计算可能出走的人数
        local potentialLost = math.floor(memberCount * 0.15)
        if potentialLost < 1 then potentialLost = 1 end

        report.events[#report.events + 1] = rebel.name .. "率旁支要求分家。"
        GameData.AddLog(rebel.name .. "联合旁支" .. potentialLost .. "人要求分家！")

        return {
            title = "分家危机",
            desc = "族中" .. rebel.name .. "联合旁支" .. potentialLost
                .. "余人，上堂请求分家另立门户。\n\n"
                .. "理由：'族大不亲，产业分配不公，旁支子弟无出头之日。'\n\n"
                .. "此事若处理不当，恐引发武斗。当前族人" .. memberCount .. "口。",
            choices = {
                -- 选项1：坚决不允（可能触发战斗）
                { text = "坚决不允！祖宗规矩不可违（可能引发武斗）", effect = function()
                    -- 50%概率叛逆者武力抗争
                    if math.random(100) <= 50 then
                        if #loyalFighters == 0 then
                            -- 无人可战
                            GameData.AddResource("silver", -silverSplit)
                            GameData.AddResource("fame", -fameLoss)
                            rebel.alive = false
                            rebel.deathCause = "出走"
                            GameData.AddLog("族中无人能弹压，" .. rebel.name .. "暴力分家而去！")
                            return
                        end
                        -- 触发战斗
                        local rival = GenerateRebelRival(rebel.name, rebel.martial or 35, s.clanRank)
                        local GS = require("UI.GameScreen")
                        GS.EventBattle(allFighters, rival, {
                            soldierCount = 0,
                            onSettle = function(result, rivalData, deployedIds)
                                if result == "victory" then
                                    GameData.AddResource("fame", math.floor(fameLoss * 0.5))
                                    GameData.AddLog("弹压分家势力成功！族权稳固。")
                                    rebel.health = math.max(1, rebel.health - 25)
                                    if rebel.state ~= "生病" then rebel.prevState = rebel.state end
                                    rebel.state = "生病"
                                else
                                    GameData.AddResource("silver", -silverSplit)
                                    GameData.AddResource("grain", -grainSplit)
                                    GameData.AddResource("fame", -fameLoss)
                                    rebel.alive = false
                                    rebel.deathCause = "出走"
                                    -- 带走部分族人
                                    local lost = 0
                                    for _, m in ipairs(GameData.GetAliveMembers()) do
                                        if m.id ~= rebel.id and not m.isFounder
                                           and m.age >= 16 and math.random(100) <= 12
                                           and lost < potentialLost then
                                            m.alive = false
                                            m.deathCause = "分家"
                                            lost = lost + 1
                                        end
                                    end
                                    GameData.AddLog("弹压失败！" .. rebel.name ..
                                        "率" .. lost .. "人暴力分家而去。")
                                end
                                for _, memberId in ipairs(deployedIds) do
                                    local member = GameData.GetMember(memberId)
                                    if member and math.random(100) <= ((result == "victory") and 15 or 40) then
                                        local loss = math.random(5, 18)
                                        member.health = math.max(1, member.health - loss)
                                        if member.health <= 10 then
                                            if member.state ~= "生病" then member.prevState = member.state end
                                            member.state = "生病"
                                        end
                                    end
                                end
                            end,
                        }, { excludeIds = { rebel.id } })
                    else
                        -- 叛逆者屈服
                        GameData.AddResource("fame", math.floor(fameLoss * 0.3))
                        GameData.AddLog("族长威严镇压分家之议，" .. rebel.name .. "不敢再言。")
                    end
                end },
                -- 选项2：同意和平分家
                { text = "和平分家、各安天命（失去族人和财产）",
                  effect = function()
                    GameData.AddResource("silver", -silverSplit)
                    GameData.AddResource("grain", -grainSplit)
                    rebel.alive = false
                    rebel.deathCause = "分家"
                    local lost = 0
                    for _, m in ipairs(GameData.GetAliveMembers()) do
                        if m.id ~= rebel.id and not m.isFounder
                           and m.age >= 14 and math.random(100) <= 10
                           and lost < potentialLost then
                            m.alive = false
                            m.deathCause = "分家"
                            lost = lost + 1
                        end
                    end
                    GameData.AddLog(rebel.name .. "和平分家而去，带走族人" .. lost .. "口、银" .. silverSplit .. "两。")
                end },
                -- 选项3：增设旁支产业安抚
                { text = "增设旁支产业、满足利益诉求（-" .. math.floor(silverSplit * 1.2) .. "银两）",
                  effect = function()
                    local cost = math.floor(silverSplit * 1.2)
                    GameData.AddResource("silver", -cost)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("增设旁支产业安抚人心，分家之议暂息。银-" .. cost)
                end },
            }
        }
    end,
}

-- ============================================================================
-- 4. 长老逼宫 —— 族中长辈不满当前管理，联合施压
-- ============================================================================
events[#events + 1] = {
    id = "fc_elder_pressure",
    title = "长老逼宫",
    rankRange = {7, 9},
    weight = 8,
    cooldownMonths = 10,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        -- 族中有50岁以上的长辈
        for _, m in ipairs(GameData.GetAliveMembers()) do
            if m.age >= 50 and not m.isFounder then
                return true
            end
        end
        return false
    end,
    execute = function(s, report)
        local scale = GetConflictScale(s.clanRank)

        -- 找到年纪最大的非创始人长辈
        local elder = nil
        for _, m in ipairs(GameData.GetAliveMembers()) do
            if m.age >= 50 and not m.isFounder then
                if not elder or m.age > elder.age then
                    elder = m
                end
            end
        end
        if not elder then return nil end

        local silverCost = math.floor(40 * scale)
        local fameLoss = math.floor(18 * scale)
        local fameGain = math.floor(15 * scale)

        report.events[#report.events + 1] = elder.name .. "率族老逼宫。"
        GameData.AddLog(elder.name .. "联合族老对族长施压，质疑管理能力。")

        return {
            title = "长老逼宫",
            desc = "族中长辈" .. elder.name .. "（" .. elder.age .. "岁）联合数位族老，"
                .. "在祠堂当众质问族长治家不善。\n\n"
                .. "指控包括：'产业经营不力、用人不当、挥霍族财'。\n\n"
                .. "族老话语权极重，若不妥善回应，族长权威将荡然无存。",
            choices = {
                -- 选项1：接受批评、自省改进
                { text = "虚心接受批评、承诺改进",
                  effect = function()
                    GameData.AddResource("fame", fameGain)
                    GameData.AddResource("silver", -math.floor(silverCost * 0.3))
                    GameData.AddLog("虚心接受族老批评，承诺改善治家方略，族人心悦诚服。")
                end },
                -- 选项2：力排众议、展示权威
                { text = "力排众议、当堂展示功绩",
                  effect = function()
                    -- 根据当前资产判断说服力
                    local totalAssets = s.silver + s.grain * 2 + (s.fame or 0)
                    if totalAssets > 5000 * scale then
                        GameData.AddResource("fame", math.floor(fameGain * 1.5))
                        GameData.AddLog("当堂列举治家功绩，数据详实、无可辩驳。族老哑口无言。")
                    else
                        GameData.AddResource("fame", -fameLoss)
                        GameData.AddLog("强词夺理，族老更加不满。声望-" .. fameLoss)
                        -- 可能导致长辈出走
                        if math.random(100) <= 25 then
                            elder.alive = false
                            elder.deathCause = "出走"
                            GameData.AddLog(elder.name .. "愤而离族，投奔他姓。")
                        end
                    end
                end },
                -- 选项3：设宴安抚
                { text = "设宴款待族老、重礼安抚（-" .. silverCost .. "银两）",
                  effect = function()
                    GameData.AddResource("silver", -silverCost)
                    GameData.AddResource("fame", math.floor(fameGain * 0.6))
                    GameData.AddLog("设宴安抚族老，送上重礼，矛盾暂时化解。")
                end },
            }
        }
    end,
}

-- ============================================================================
-- 5. 婢仆叛逃 —— 家中婢仆被外人收买，窃取机密或投毒
-- ============================================================================
events[#events + 1] = {
    id = "fc_servant_betrayal",
    title = "婢仆叛逃",
    rankRange = {7, 9},
    weight = 7,
    cooldownMonths = 8,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return s.fame >= 200
    end,
    execute = function(s, report)
        local scale = GetConflictScale(s.clanRank)
        local silverLoss = math.floor(20 * scale)
        local investigateCost = math.floor(15 * scale)

        -- 随机选一个族人被投毒
        local target = nil
        local adults = GameData.GetAdultMembers()
        if #adults > 0 then
            target = adults[math.random(1, #adults)]
        end
        local targetName = target and target.name or "族长"

        report.events[#report.events + 1] = "家中婢仆被外人收买叛逃。"
        GameData.AddLog("发现婢仆暗通外人，窃取族中机密！")

        return {
            title = "婢仆叛逃",
            desc = "深夜有人发现家中老仆鬼鬼祟祟翻找账册。经盘问，此仆乃受外族重金收买，"
                .. "已暗中窃取族中产业账目、人丁名册。\n\n"
                .. "更可怕的是，此仆在" .. targetName .. "饮食中掺入慢性毒药，"
                .. "若非发现及时，后果不堪设想。",
            choices = {
                -- 选项1：彻查幕后主使
                { text = "严刑拷问、追查幕后主使（-" .. investigateCost .. "银两）",
                  effect = function()
                    GameData.AddResource("silver", -investigateCost)
                    -- 查出幕后黑手获得补偿
                    if math.random(100) <= 60 then
                        local compensation = math.floor(30 * scale)
                        GameData.AddResource("silver", compensation)
                        GameData.AddResource("fame", math.floor(10 * scale))
                        GameData.AddLog("追查出幕后主使乃敌对家族所为！索赔银" .. compensation .. "两。")
                    else
                        GameData.AddLog("仆人畏罪自尽，线索中断。")
                    end
                    -- 解毒
                    if target then
                        target.health = math.max(50, target.health - 10)
                        GameData.AddLog(targetName .. "及时解毒，健康略损。")
                    end
                end },
                -- 选项2：加强防卫
                { text = "清理仆从、加强府中防卫（-" .. math.floor(silverLoss * 1.5) .. "银两）",
                  effect = function()
                    GameData.AddResource("silver", -math.floor(silverLoss * 1.5))
                    if target then
                        target.health = math.max(60, target.health - 5)
                    end
                    GameData.AddLog("大换血清理仆从，府中防卫加强。" .. targetName .. "已解毒安全。")
                end },
                -- 选项3：息事宁人
                { text = "放走仆人、息事宁人",
                  effect = function()
                    GameData.AddResource("fame", -math.floor(8 * scale))
                    -- 族人中毒影响更严重
                    if target then
                        target.health = math.max(20, target.health - 25)
                        if target.health <= 30 then
                            if target.state ~= "生病" then target.prevState = target.state end
                            target.state = "生病"
                        end
                        GameData.AddLog(targetName .. "中毒较深，健康大损。仆人逃脱、机密外泄。")
                    else
                        GameData.AddLog("放走叛仆，族中机密外泄，声名有损。")
                    end
                end },
            }
        }
    end,
}

-- ============================================================================
-- 6. 嫡庶火并 —— 继承争端升级为暴力冲突
-- ============================================================================
events[#events + 1] = {
    id = "fc_succession_battle",
    title = "嫡庶火并",
    rankRange = {8, 9},
    weight = 8,
    cooldownMonths = 14,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        -- 需要足够多的成年男性
        local maleAdults = 0
        for _, m in ipairs(adults) do
            if m.gender == "male" then maleAdults = maleAdults + 1 end
        end
        return maleAdults >= 6
    end,
    execute = function(s, report)
        local scale = GetConflictScale(s.clanRank)

        -- 选两个成年男性作为争夺双方
        local maleAdults = {}
        for _, m in ipairs(GameData.GetAdultMembers()) do
            if m.gender == "male" and m.age >= 18 and m.age <= 50
               and not m.isFounder and m.health > 30 then
                maleAdults[#maleAdults + 1] = m
            end
        end
        if #maleAdults < 2 then return nil end

        -- 按武艺排序，选最强和第二强
        table.sort(maleAdults, function(a, b) return (a.martial or 0) > (b.martial or 0) end)
        local heir1 = maleAdults[1]
        local heir2 = maleAdults[2]

        local silverCost = math.floor(50 * scale)
        local fameWin = math.floor(20 * scale)
        local fameLoss = math.floor(25 * scale)

        report.events[#report.events + 1] = heir1.name .. "与" .. heir2.name .. "嫡庶火并。"
        GameData.AddLog(heir1.name .. "与" .. heir2.name .. "争夺继承权，矛盾激化！")

        -- 玩家需要选择支持哪一方或调解
        return {
            title = "嫡庶火并",
            desc = "族中" .. heir1.name .. "（武艺" .. (heir1.martial or 0) .. "）与"
                .. heir2.name .. "（武艺" .. (heir2.martial or 0) .. "）"
                .. "因继承权之争水火不容，各自拉拢族人、暗中备战。\n\n"
                .. "两方人马在祠堂前对峙，剑拔弩张、一触即发。\n\n"
                .. "族长必须立刻做出决断，否则两败俱伤。",
            choices = {
                -- 选项1：支持heir1（较强者），heir2被驱逐
                { text = "支持" .. heir1.name .. "（武艺更高）",
                  effect = function()
                    GameData.AddResource("fame", fameWin)
                    heir2.alive = false
                    heir2.deathCause = "出走"
                    GameData.AddLog("族长裁定" .. heir1.name .. "为正统继承人。"
                        .. heir2.name .. "不服，率亲信出走。")
                    -- heir2出走时带走部分资产
                    GameData.AddResource("silver", -math.floor(silverCost * 0.3))
                end },
                -- 选项2：支持heir2（较弱者），可能触发战斗
                { text = "支持" .. heir2.name .. "（压制强者以平衡权力）",
                  effect = function()
                    -- heir1不服，60%概率武力抗争
                    if math.random(100) <= 60 then
                        -- heir1发动武力反抗
                        local rival = GenerateRebelRival(heir1.name, heir1.martial or 40, s.clanRank)
                        local GS = require("UI.GameScreen")
                        local allFighters = GetAllFighters()
                        GS.EventBattle(allFighters, rival, {
                            soldierCount = 0,
                            onSettle = function(result, rivalData, deployedIds)
                                if result == "victory" then
                                    heir1.alive = false
                                    heir1.deathCause = "出走"
                                    GameData.AddResource("fame", fameWin)
                                    GameData.AddLog("击败" .. heir1.name .. "的叛乱！"
                                        .. heir2.name .. "继位。")
                                else
                                    heir2.alive = false
                                    heir2.deathCause = "出走"
                                    GameData.AddResource("silver", -silverCost)
                                    GameData.AddResource("fame", -fameLoss)
                                    GameData.AddLog(heir1.name .. "以武力夺权成功！"
                                        .. heir2.name .. "被驱逐。")
                                end
                                for _, memberId in ipairs(deployedIds) do
                                    local member = GameData.GetMember(memberId)
                                    if member and math.random(100) <= 25 then
                                        member.health = math.max(1, member.health - math.random(5, 15))
                                        if member.health <= 10 then
                                            if member.state ~= "生病" then member.prevState = member.state end
                                            member.state = "生病"
                                        end
                                    end
                                end
                            end,
                        }, { excludeIds = { heir1.id }, preSelectedIds = { heir2.id } })
                    else
                        heir1.alive = false
                        heir1.deathCause = "出走"
                        GameData.AddResource("fame", math.floor(fameWin * 0.5))
                        GameData.AddLog(heir1.name .. "不服但无力反抗，黯然离族。")
                        GameData.AddResource("silver", -math.floor(silverCost * 0.2))
                    end
                end },
                -- 选项3：调解双方
                { text = "强行调解、各退一步（-" .. silverCost .. "银两）",
                  effect = function()
                    GameData.AddResource("silver", -silverCost)
                    -- 70%调解成功
                    if math.random(100) <= 70 then
                        GameData.AddResource("fame", math.floor(fameWin * 0.8))
                        GameData.AddLog("族长调解有方，" .. heir1.name .. "与" .. heir2.name
                            .. "握手言和。")
                    else
                        -- 双方都不满
                        GameData.AddResource("fame", -math.floor(fameLoss * 0.5))
                        GameData.AddLog("调解失败，两方都不满意，矛盾仍在暗中发酵。")
                    end
                end },
            }
        }
    end,
}

return events
