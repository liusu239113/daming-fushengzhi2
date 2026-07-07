-- ============================================================================
-- 大明浮生志2 - 隐藏结局系统（v3 重构版）
-- 设计原则：
--   1. 只有灭族（extinction）和满门抄斩（executed）是真正的 Game Over
--   2. 所有正面结局都要求国柱（clanRank >= 9），确保玩家体验完整9级内容
--   3. 破产不再是结局，改为挫折事件（降级 + 惩罚，在 MonthlyUpdate 处理）
--   4. 正面结局默认选项为"继续游戏"，不强制终止
--   5. 新增"问鼎天下"结局：征服全部58个战役关卡
-- ============================================================================

local GameData = require("Data.GameData")
local EraSystem = require("Data.EraSystem")
local CampaignRegions = require("Data.CampaignRegions")

local EndingSystem = {}

-- ============================================================================
-- 辅助函数
-- ============================================================================

--- 检查是否已征服全部关卡（58关）
local function AllRegionsConquered()
    local s = GameData.state
    if not s or not s.conqueredStages then return false end
    local total = CampaignRegions.GetTotalStageCount()  -- 58
    return #s.conqueredStages >= total
end

-- ============================================================================
-- 隐藏结局定义
-- ============================================================================

EndingSystem.HIDDEN_ENDINGS = {
    -- ============================
    -- 灭族结局（族人全部死亡）—— 真正的 Game Over
    -- ============================
    {
        id = "extinction",
        title = "血脉断绝",
        priority = 100,
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
    -- 满门抄斩（声望极低）—— 真正的 Game Over
    -- 门槛：声望 <= -100，年份 >= 1450，品级 >= 名门(7)
    -- ============================
    {
        id = "executed",
        title = "满门抄斩",
        priority = 90,
        check = function(s)
            return s.fame <= -100 and s.year >= 1450 and s.clanRank >= 7
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
    -- 问鼎天下（征服全部58个战役关卡）—— 正面结局
    -- 要求：国柱(9) + 全部区域征服
    -- ============================
    {
        id = "conquest_complete",
        title = "问鼎天下",
        priority = 60,
        check = function(s)
            return s.clanRank >= 9 and AllRegionsConquered()
        end,
        trigger = function(s)
            s.gameEnded = true
            s.endingChoice = "conquest"
            s.hiddenEnding = "conquest_complete"
            GameData.AddResource("fame", 500)
            s.pendingEvents[#s.pendingEvents + 1] = {
                title = "问鼎天下 · 隐藏结局",
                desc = s.clanName .. "一族南征北战，历经十二场大战，\n" ..
                       "终于扫平群雄，问鼎天下！\n\n" ..
                       "从寒门崛起到国柱至尊，从清剿匪寨到一统江山，\n" ..
                       "这是一段波澜壮阔的传奇。\n\n" ..
                       "天下大势，分久必合——" .. s.clanName .. "，\n" ..
                       "便是这乱世的终结者。\n\n" ..
                       "（提示：游戏中仍有更多内容等待探索，可选择继续游戏）",
                choices = {
                    { text = "继续游戏（内容尚未体验完毕）", effect = function()
                        s.gameEnded = false
                        s.hiddenEnding = nil
                        GameData.AddLog("虽已问鼎天下，但大明国运未尽……")
                    end },
                    { text = "功成身退", effect = function()
                        GameData.AddLog("宗族一统天下，功盖千秋，青史永载。")
                    end },
                },
                isEnding = true,
            }
        end,
    },

    -- ============================
    -- 科举巅峰（书香门第）—— 正面结局
    -- 要求：国柱(9) + 科举通过 >= 300人
    -- ============================
    {
        id = "scholar_dynasty",
        title = "书香门第",
        priority = 50,
        check = function(s)
            return s.clanRank >= 9 and (s.totalExamPasses or 0) >= 300
        end,
        trigger = function(s)
            s.gameEnded = true
            s.endingChoice = "scholar"
            s.hiddenEnding = "scholar_dynasty"
            GameData.AddResource("fame", 300)
            s.pendingEvents[#s.pendingEvents + 1] = {
                title = "书香门第 · 隐藏结局",
                desc = "三百人登科！" .. s.clanName .. "一门桃李天下，声名远播。\n" ..
                       "朝廷赐匾'书香世家'，御笔亲题'文脉绵长'。\n\n" ..
                       "从寒门崛起到国柱至尊，科举名门冠绝天下，\n" ..
                       "即便大厦将倾，文化传承永不磨灭。\n\n" ..
                       "（提示：游戏中仍有更多内容等待探索，可选择继续游戏）",
                choices = {
                    { text = "继续游戏（内容尚未体验完毕）", effect = function()
                        s.gameEnded = false
                        s.hiddenEnding = nil
                        GameData.AddLog("虽获殊荣，宗族仍需延续……")
                    end },
                    { text = "文脉永续", effect = function()
                        GameData.AddLog("宗族获封'书香世家'，名垂青史。")
                    end },
                },
                isEnding = true,
            }
        end,
    },

    -- ============================
    -- 将门世家（军功卓著）—— 正面结局
    -- 要求：国柱(9) + 军功 >= 500 + 寨堡 >= 150
    -- ============================
    {
        id = "warlord",
        title = "将门世家",
        priority = 50,
        check = function(s)
            return s.clanRank >= 9 and (s.totalMilitaryMerits or 0) >= 500 and s.fortCount >= 150
        end,
        trigger = function(s)
            s.gameEnded = true
            s.endingChoice = "warlord"
            s.hiddenEnding = "warlord"
            GameData.AddResource("fame", 250)
            s.pendingEvents[#s.pendingEvents + 1] = {
                title = "将门世家 · 隐藏结局",
                desc = s.clanName .. "一族战功赫赫，寨堡连营百五十座，麾下精兵如云。\n" ..
                       "朝廷忌惮其势力，却也不得不倚重。\n\n" ..
                       "乱世将至，手握重兵的" .. s.clanName .. "一族，\n" ..
                       "将书写一段属于自己的传奇。\n\n" ..
                       "（提示：游戏中仍有更多内容等待探索，可选择继续游戏）",
                choices = {
                    { text = "继续游戏（内容尚未体验完毕）", effect = function()
                        s.gameEnded = false
                        s.hiddenEnding = nil
                        GameData.AddLog("虽已兵强马壮，大明的命运仍在继续……")
                    end },
                    { text = "虎踞一方", effect = function()
                        GameData.AddLog("宗族雄踞一方，成为乱世中的擎天柱。")
                    end },
                },
                isEnding = true,
            }
        end,
    },

    -- ============================
    -- 商业帝国（巨富）—— 正面结局
    -- 要求：国柱(9) + 银两 >= 500000 + 产业 >= 35
    -- ============================
    {
        id = "merchant_empire",
        title = "富甲天下",
        priority = 50,
        check = function(s)
            return s.clanRank >= 9 and s.silver >= 500000 and #s.industries >= 35
        end,
        trigger = function(s)
            s.gameEnded = true
            s.endingChoice = "merchant"
            s.hiddenEnding = "merchant_empire"
            GameData.AddResource("fame", 200)
            s.pendingEvents[#s.pendingEvents + 1] = {
                title = "富甲天下 · 隐藏结局",
                desc = "银两堆积如山，商铺遍布四方。\n" ..
                       s.clanName .. "一族富可敌国，堪称大明首富。\n\n" ..
                       "民间传言：'天下银子，十之有三流入" .. s.clanName .. "家。'\n" ..
                       "从寒门崛起到国柱至尊，商业帝国冠绝天下，朝廷既羡且惧……\n\n" ..
                       "（提示：游戏中仍有更多内容等待探索，可选择继续游戏）",
                choices = {
                    { text = "继续游戏（内容尚未体验完毕）", effect = function()
                        s.gameEnded = false
                        s.hiddenEnding = nil
                        GameData.AddLog("虽已富可敌国，但大明风云未定……")
                    end },
                    { text = "富贵传家", effect = function()
                        GameData.AddLog("宗族富甲天下，但树大招风……")
                    end },
                },
                isEnding = true,
            }
        end,
    },

    -- ============================
    -- 桃花源（隐居避世）—— 正面结局
    -- 要求：国柱(9) + 300人以上 + 声望>=5000 + 平均健康>=80 + 年份>=1580
    -- ============================
    {
        id = "utopia",
        title = "桃源隐世",
        priority = 40,
        check = function(s)
            if s.clanRank < 9 then return false end
            local alive = GameData.GetAliveMembers()
            if #alive < 300 then return false end
            if s.fame < 5000 then return false end
            local totalHealth = 0
            for _, m in ipairs(alive) do
                totalHealth = totalHealth + m.health
            end
            return (totalHealth / #alive) >= 80 and s.year >= 1580
        end,
        trigger = function(s)
            s.gameEnded = true
            s.endingChoice = "utopia"
            s.hiddenEnding = "utopia"
            GameData.AddResource("fame", 100)
            s.pendingEvents[#s.pendingEvents + 1] = {
                title = "桃源隐世 · 隐藏结局",
                desc = "族人安居乐业，耕读传家，与世无争。\n" ..
                       "山环水抱，鸡犬相闻，俨然一处世外桃源。\n\n" ..
                       "'不知有汉，无论魏晋'——\n" ..
                       "当外面的世界风雨飘摇，" .. s.clanName .. "一族\n" ..
                       "已在青山绿水间找到了真正的归宿。\n\n" ..
                       "（提示：游戏中仍有更多内容等待探索，可选择继续游戏）",
                choices = {
                    { text = "继续游戏（内容尚未体验完毕）", effect = function()
                        s.gameEnded = false
                        s.hiddenEnding = nil
                        GameData.AddLog("世外桃源虽好，族人仍心系天下……")
                    end },
                    { text = "岁月静好", effect = function()
                        GameData.AddLog("宗族隐居桃源，不问世事，安享太平。")
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

-- ============================================================================
-- 破产挫折检查（每月调用，不再是结局，改为降级+惩罚）
-- 条件：银两<=0 且 粮食<=0 且 布匹<=0 且 无产业
-- 效果：品级降一级 + 声望-10 + 提示事件
-- ============================================================================

--- 检查并处理破产挫折（不终止游戏）
--- @param report table 月度报告
--- @return boolean 是否触发了挫折
function EndingSystem.CheckBankruptcy(report)
    local s = GameData.state
    if not s or s.gameEnded then return false end

    -- 条件：四项资源全部耗尽且无产业
    if s.silver > 0 or s.grain > 0 or s.cloth > 0 or #s.industries > 0 then
        return false
    end

    -- 冷却：上次破产挫折后至少6个月才会再次触发
    if s._lastBankruptMonth and (s.totalMonths - s._lastBankruptMonth) < 6 then
        return false
    end
    s._lastBankruptMonth = s.totalMonths

    -- 降级处理（最低降到寒门=1）
    local oldRank = s.clanRank
    if s.clanRank > 1 then
        s.clanRank = s.clanRank - 1
        GameData.AddLog("家道中落，宗族品级从" .. GameData.CLAN_RANKS[oldRank] .. "降为" .. GameData.CLAN_RANKS[s.clanRank] .. "。")
    end

    -- 声望惩罚
    GameData.AddResource("fame", -10)

    -- 给一些救济资源，防止陷入死循环
    GameData.AddResource("grain", 10)
    GameData.AddResource("silver", 5)

    -- 推送提示事件
    s.pendingEvents[#s.pendingEvents + 1] = {
        title = "家道中落",
        desc = "银两粮食俱尽，产业全无，宗族陷入困境。\n" ..
               "族人四散打零工勉强度日，门第声望大跌。\n\n" ..
               (oldRank > 1 and ("品级从「" .. GameData.CLAN_RANKS[oldRank] .. "」降为「" .. GameData.CLAN_RANKS[s.clanRank] .. "」。\n") or "") ..
               "但只要族人尚在，就还有东山再起的机会！\n" ..
               "（获得少量救济粮银，维持基本生存）",
        choices = {
            { text = "卧薪尝胆", effect = function()
                GameData.AddLog("宗族遭遇挫折，族人发誓东山再起。")
            end },
        },
    }

    report.events[#report.events + 1] = "家道中落，品级下降"
    return true
end

-- ============================================================================
-- 工具函数
-- ============================================================================

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
