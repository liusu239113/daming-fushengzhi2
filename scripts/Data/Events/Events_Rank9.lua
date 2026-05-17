-- ============================================================================
-- 大明浮生志2 - 国柱品级（9级）专属事件池
-- 设计理念：国柱之家与国同休，面临最高级别的挑战与机遇
-- ============================================================================

local GameData = require("Data.GameData")
local EventSystem = require("Systems.EventSystem")
local events = {}

-- 1. 社稷之灾 — 终极天灾
events[#events + 1] = {
    id = "r9_national_calamity",
    title = "社稷之灾",
    rankRange = {9, 9},
    weight = 10,
    cooldownMonths = 12,
    isDisaster = true,
    check = function(s) return s.grain > 500 or s.silver > 500 end,
    execute = function(s, report)
        local scale = EventSystem.GetDisasterScale(s.clanRank)
        local grainLoss = math.ceil(s.grain * 0.25)
        local silverLoss = math.ceil(s.silver * 0.15)
        local fameLoss = math.ceil(s.fame * 0.1)

        -- 族人也受波及
        local members = GameData.GetAliveMembers()
        local wounded = 0
        for _, m in ipairs(members) do
            if math.random() < 0.15 then
                m.health = math.max(10, m.health - math.random(10, 25))
                wounded = wounded + 1
            end
        end

        report.events[#report.events + 1] = "社稷动荡！家族首当其冲！"
        GameData.AddLog("天崩地裂，社稷动荡，国柱之家遭受重创。")
        return {
            title = "社稷之灾",
            desc = "连月地震、旱蝗并作、流民四起。\n身为国柱，朝廷与百姓皆仰望你族赈济。\n不出手则声望崩溃，出手则家底大伤。\n族中" .. wounded .. "人受伤。\n预计损失：粮" .. grainLoss .. " 银" .. silverLoss,
            choices = {
                { text = "倾家赈灾保社稷（全额损失+30声望）", effect = function()
                    GameData.AddResource("grain", -grainLoss)
                    GameData.AddResource("silver", -silverLoss)
                    GameData.AddResource("fame", 30)
                    GameData.AddLog("国柱之家倾力赈灾，天下感念。")
                end },
                { text = "出半数赈济尽人事（半额损失）", effect = function()
                    GameData.AddResource("grain", -math.ceil(grainLoss * 0.5))
                    GameData.AddResource("silver", -math.ceil(silverLoss * 0.5))
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("量力赈济，虽非全力但亦尽心。")
                end },
                { text = "明哲保身闭门不出", effect = function()
                    GameData.AddResource("fame", -fameLoss)
                    GameData.AddResource("grain", -math.ceil(grainLoss * 0.2))
                    GameData.AddLog("国柱之家闭门不救，天下人齿冷。")
                end },
            }
        }
    end,
}

-- 2. 权臣构陷
events[#events + 1] = {
    id = "r9_framed_by_minister",
    title = "权臣构陷",
    rankRange = {9, 9},
    weight = 9,
    cooldownMonths = 10,
    isDisaster = true,
    check = function(s) return s.fame > 800 end,
    execute = function(s, report)
        local silverLoss = math.ceil(s.silver * 0.12)
        local fameLoss = math.ceil(s.fame * 0.15)

        report.events[#report.events + 1] = "朝中权臣设计构陷，意图扳倒你族！"
        GameData.AddLog("朝中奸臣密谋构陷，家族危在旦夕。")
        return {
            title = "权臣构陷",
            desc = "朝中权臣嫉你家族势大，伪造罪证上奏弹劾，罗织谋反之罪。\n若处置不当，轻则抄家罚没，重则族灭满门。\n此乃生死存亡之际！",
            choices = {
                { text = "进京面圣自辩（-" .. math.ceil(silverLoss * 0.5) .. "银两路费）", cost = {silver = math.ceil(silverLoss * 0.5)}, effect = function()
                    if math.random() < 0.6 then
                        GameData.AddResource("fame", 20)
                        GameData.AddLog("面圣自辩成功，权臣反被治罪！家族声威更盛。")
                    else
                        GameData.AddResource("fame", -math.ceil(fameLoss * 0.5))
                        GameData.AddResource("silver", -math.ceil(silverLoss * 0.3))
                        GameData.AddLog("面圣未能完全洗清嫌疑，被罚没部分家产。")
                    end
                end },
                { text = "联络同盟反击（-" .. silverLoss .. "银两）", cost = {silver = silverLoss}, effect = function()
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("联合朝中友人反击，权臣阴谋败露。")
                end },
                { text = "忍辱求全献财免灾", effect = function()
                    GameData.AddResource("silver", -silverLoss)
                    GameData.AddResource("fame", -fameLoss)
                    GameData.AddLog("献出大量家财求得平安，但声望一落千丈。")
                end },
            }
        }
    end,
}

-- 3. 万民请愿
events[#events + 1] = {
    id = "r9_peoples_plea",
    title = "万民请愿",
    rankRange = {9, 9},
    weight = 7,
    cooldownMonths = 12,
    isDisaster = false,
    check = function(s) return s.fame > 500 end,
    execute = function(s, report)
        report.events[#report.events + 1] = "百姓联名请愿，恳请家族庇护一方。"
        GameData.AddLog("万民请愿书送达，恳请国柱之家庇佑乡梓。")
        return {
            title = "万民请愿",
            desc = "四方百姓联名上书，恳请你族出面调停地方纷争、赈济灾民。\n国柱之家，已与一方百姓的命运紧紧相连。",
            choices = {
                { text = "全力以赴（-80银两-50粮食+50声望）", cost = {silver = 80, grain = 50}, effect = function()
                    GameData.AddResource("fame", 50)
                    GameData.AddLog("倾力相助，万民称颂，声名如日中天。")
                end },
                { text = "量力而行（-30银两-20粮食+20声望）", cost = {silver = 30, grain = 20}, effect = function()
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("适度出手，百姓感恩。")
                end },
                { text = "婉言推辞", effect = function()
                    GameData.AddResource("fame", -15)
                    GameData.AddLog("推辞不管，百姓失望，声望受损。")
                end },
            }
        }
    end,
}

-- 4. 传世家训
events[#events + 1] = {
    id = "r9_family_legacy",
    title = "传世家训",
    rankRange = {9, 9},
    weight = 6,
    cooldownMonths = 24,
    isDisaster = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族长提议编撰家训，传诸后世。"
        GameData.AddLog("家族已至国柱之尊，当立家训以传千秋。")
        return {
            title = "传世家训",
            desc = "家族已至国柱之巅，族长提议延请大儒编撰《家训》，将家族数代积累的治家之道、为人处世之理汇编成册，以教化后人。",
            choices = {
                { text = "重金延请大儒编撰（-100银两+40声望）", cost = {silver = 100}, effect = function()
                    GameData.AddResource("fame", 40)
                    -- 全族学识微提
                    for _, m in ipairs(GameData.GetAliveMembers()) do
                        m.study = math.min(100, m.study + math.random(2, 5))
                    end
                    GameData.AddLog("家训编成，传诸后世，全族受益。")
                end },
                { text = "族长亲自撰写（+20声望）", effect = function()
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("族长亲撰家训，言简意赅，族人传诵。")
                end },
            }
        }
    end,
}

return events
