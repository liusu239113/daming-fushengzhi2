-- ============================================================================
-- 大明浮生志2 - 隐藏结局系统
-- 根据玩家行为在特定条件下触发隐藏结局（早于/替代甲申之变）
-- 每月在 MonthlyUpdate 中检查
-- ============================================================================

local GameData = require("Data.GameData")
local EraSystem = require("Data.EraSystem")

local EndingSystem = {}

-- ============================================================================
-- 隐藏结局定义
-- ============================================================================

EndingSystem.HIDDEN_ENDINGS = {
    -- ============================
    -- 灭族结局（族人全部死亡）
    -- ============================
    {
        id = "extinction",
        title = "血脉断绝",
        priority = 100,  -- 优先级最高
        check = function(s)
            return #GameData.GetAliveMembers() == 0
        end,
        trigger = function(s)
            s.gameEnded = true
            s.endingChoice = "extinction"
            s.hiddenEnding = "extinction"
            s.pendingEvents[#s.pendingEvents + 1] = {
                title = "血脉断绝",
                desc = "最后一位族人离世，宗祠香火断绝。\n" ..
                       EraSystem.GetYearLabel(s.year) .. "，" .. s.clanName .. "一族自此湮没于历史长河。\n\n" ..
                       "传承" .. math.floor(s.totalMonths / 12) .. "年，终究未能延续。",
                choices = {
                    { text = "魂归故里", effect = function() end },
                },
                isEnding = true,
            }
        end,
    },

    -- ============================
    -- 破产结局（资源耗尽且负债）
    -- ============================
    {
        id = "bankrupt",
        title = "家道中落",
        priority = 90,
        check = function(s)
            return s.silver <= 0 and s.grain <= 0 and s.cloth <= 0 and #s.industries == 0
        end,
        trigger = function(s)
            s.gameEnded = true
            s.endingChoice = "bankrupt"
            s.hiddenEnding = "bankrupt"
            s.pendingEvents[#s.pendingEvents + 1] = {
                title = "家道中落",
                desc = "银两粮食俱尽，产业全无，宗族已无法维系。\n" ..
                       "族人四散流离，曾经的" .. GameData.GetClanRankName() .. "门第沦为乞丐。\n\n" ..
                       "\"富不过三代\"——这句话成了最残忍的谶语。",
                choices = {
                    { text = "散尽家财", effect = function() end },
                },
                isEnding = true,
            }
        end,
    },

    -- ============================
    -- 科举巅峰（三代进士）
    -- ============================
    {
        id = "scholar_dynasty",
        title = "书香门第",
        priority = 50,
        check = function(s)
            return (s.totalExamPasses or 0) >= 10 and s.clanRank >= 5
        end,
        trigger = function(s)
            s.gameEnded = true
            s.endingChoice = "scholar"
            s.hiddenEnding = "scholar_dynasty"
            GameData.AddResource("fame", 100)
            s.pendingEvents[#s.pendingEvents + 1] = {
                title = "书香门第 · 隐藏结局",
                desc = "十人登科！" .. s.clanName .. "一门桃李天下，声名远播。\n" ..
                       "朝廷赐匾'书香世家'，御笔亲题'文脉绵长'。\n\n" ..
                       "即便大厦将倾，文化传承永不磨灭。\n" ..
                       "族人著书立说，教化一方，青史留名。",
                choices = {
                    { text = "文脉永续", effect = function()
                        GameData.AddLog("宗族获封'书香世家'，名垂青史。")
                    end },
                    { text = "继续游戏", effect = function()
                        s.gameEnded = false
                        s.hiddenEnding = nil
                        GameData.AddLog("虽获殊荣，宗族仍需延续……")
                    end },
                },
                isEnding = true,
            }
        end,
    },

    -- ============================
    -- 军阀崛起（军功卓著）
    -- ============================
    {
        id = "warlord",
        title = "将门世家",
        priority = 50,
        check = function(s)
            return (s.totalMilitaryMerits or 0) >= 15 and s.fortCount >= 5 and s.clanRank >= 5
        end,
        trigger = function(s)
            s.gameEnded = true
            s.endingChoice = "warlord"
            s.hiddenEnding = "warlord"
            GameData.AddResource("fame", 80)
            s.pendingEvents[#s.pendingEvents + 1] = {
                title = "将门世家 · 隐藏结局",
                desc = s.clanName .. "一族战功赫赫，寨堡连营五座，麾下精兵千余。\n" ..
                       "朝廷忌惮其势力，却也不得不倚重。\n\n" ..
                       "乱世将至，手握重兵的" .. s.clanName .. "一族，\n" ..
                       "将书写一段属于自己的传奇。",
                choices = {
                    { text = "虎踞一方", effect = function()
                        GameData.AddLog("宗族雄踞一方，成为乱世中的擎天柱。")
                    end },
                    { text = "继续游戏", effect = function()
                        s.gameEnded = false
                        s.hiddenEnding = nil
                        GameData.AddLog("虽已兵强马壮，大明的命运仍在继续……")
                    end },
                },
                isEnding = true,
            }
        end,
    },

    -- ============================
    -- 商业帝国（巨富）
    -- ============================
    {
        id = "merchant_empire",
        title = "富甲天下",
        priority = 50,
        check = function(s)
            return s.silver >= 2000 and #s.industries >= 10 and s.clanRank >= 4
        end,
        trigger = function(s)
            s.gameEnded = true
            s.endingChoice = "merchant"
            s.hiddenEnding = "merchant_empire"
            GameData.AddResource("fame", 60)
            s.pendingEvents[#s.pendingEvents + 1] = {
                title = "富甲天下 · 隐藏结局",
                desc = "银两堆积如山，商铺遍布四方。\n" ..
                       s.clanName .. "一族富可敌国，堪称大明首富。\n\n" ..
                       "民间传言：'天下银子，十之有三流入" .. s.clanName .. "家。'\n" ..
                       "朝廷既羡且惧，不知是福是祸……",
                choices = {
                    { text = "富贵传家", effect = function()
                        GameData.AddLog("宗族富甲天下，但树大招风……")
                    end },
                    { text = "继续游戏", effect = function()
                        s.gameEnded = false
                        s.hiddenEnding = nil
                        GameData.AddLog("虽已富可敌国，但大明风云未定……")
                    end },
                },
                isEnding = true,
            }
        end,
    },

    -- ============================
    -- 满门抄斩（声望归零且牵连政治）
    -- ============================
    {
        id = "executed",
        title = "满门抄斩",
        priority = 80,
        check = function(s)
            return s.fame <= -20 and s.year >= 1400
        end,
        trigger = function(s)
            s.gameEnded = true
            s.endingChoice = "executed"
            s.hiddenEnding = "executed"
            s.pendingEvents[#s.pendingEvents + 1] = {
                title = "满门抄斩",
                desc = "宗族声名狼藉，积怨已深。\n" ..
                       "朝廷以'谋逆'之罪下旨抄家灭族，\n" ..
                       "锦衣卫破门而入，" .. s.clanName .. "满门遭难。\n\n" ..
                       "覆巢之下，焉有完卵。",
                choices = {
                    { text = "天意如此", effect = function() end },
                },
                isEnding = true,
            }
        end,
    },

    -- ============================
    -- 桃花源（隐居避世）
    -- ============================
    {
        id = "utopia",
        title = "桃源隐世",
        priority = 40,
        check = function(s)
            local alive = GameData.GetAliveMembers()
            if #alive < 20 then return false end
            if s.fame < 50 then return false end
            -- 族人平均健康 >= 80，且无人经商从军
            local totalHealth = 0
            local hasWorker = false
            for _, m in ipairs(alive) do
                totalHealth = totalHealth + m.health
                if m.state == "从军" or m.state == "出征" then hasWorker = true end
            end
            return (totalHealth / #alive) >= 80 and not hasWorker and s.year >= 1500
        end,
        trigger = function(s)
            s.gameEnded = true
            s.endingChoice = "utopia"
            s.hiddenEnding = "utopia"
            GameData.AddResource("fame", 30)
            s.pendingEvents[#s.pendingEvents + 1] = {
                title = "桃源隐世 · 隐藏结局",
                desc = "族人安居乐业，耕读传家，与世无争。\n" ..
                       "山环水抱，鸡犬相闻，俨然一处世外桃源。\n\n" ..
                       "'不知有汉，无论魏晋'——\n" ..
                       "当外面的世界风雨飘摇，" .. s.clanName .. "一族\n" ..
                       "已在青山绿水间找到了真正的归宿。",
                choices = {
                    { text = "岁月静好", effect = function()
                        GameData.AddLog("宗族隐居桃源，不问世事，安享太平。")
                    end },
                    { text = "继续游戏", effect = function()
                        s.gameEnded = false
                        s.hiddenEnding = nil
                        GameData.AddLog("世外桃源虽好，族人仍心系天下……")
                    end },
                },
                isEnding = true,
            }
        end,
    },
}

-- ============================================================================
-- 检查隐藏结局（每月调用）
-- ============================================================================

--- 检查并触发隐藏结局
--- @param report table 月度报告
--- @return boolean 是否触发了结局
function EndingSystem.Check(report)
    local s = GameData.state
    if s.gameEnded then return false end

    -- 已触发过隐藏结局但选择继续的，不再重复触发同一结局
    local triggered = s.triggeredHiddenEndings or {}

    -- 按优先级排序检查
    local sorted = {}
    for _, ending in ipairs(EndingSystem.HIDDEN_ENDINGS) do
        sorted[#sorted + 1] = ending
    end
    table.sort(sorted, function(a, b) return a.priority > b.priority end)

    for _, ending in ipairs(sorted) do
        if not triggered[ending.id] and ending.check(s) then
            triggered[ending.id] = true
            s.triggeredHiddenEndings = triggered
            ending.trigger(s)
            report.events[#report.events + 1] = "【隐藏结局】" .. ending.title
            GameData.AddLog("【隐藏结局】触发：" .. ending.title)
            return true
        end
    end

    return false
end

--- 获取已解锁的隐藏结局列表（用于成就展示）
--- @return table 已触发的结局 id 列表
function EndingSystem.GetUnlockedEndings()
    local s = GameData.state
    local result = {}
    local triggered = s.triggeredHiddenEndings or {}
    for id, _ in pairs(triggered) do
        result[#result + 1] = id
    end
    return result
end

--- 根据 id 获取结局信息
--- @param id string 结局 id
--- @return table|nil 结局定义
function EndingSystem.GetEndingInfo(id)
    for _, ending in ipairs(EndingSystem.HIDDEN_ENDINGS) do
        if ending.id == id then return ending end
    end
    return nil
end

return EndingSystem
