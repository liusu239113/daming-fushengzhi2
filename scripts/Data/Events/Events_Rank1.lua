local GameData = require("Data.GameData")
local events = {}

-- ============================================================
-- 寒门事件池（品阶1-2）
-- 主题：乡野求生、邻里互助、天灾人祸、微末积累
-- ============================================================

-- 1. 采集野药
events[#events + 1] = {
    id = "r1_wild_herb",
    title = "采集野药",
    rankRange = {1, 2},
    weight = 12,
    cooldownMonths = 4,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAliveMembers() > 0
    end,
    execute = function(s, report)
        local members = GameData.GetAdultMembers()
        local finder = members[1]
        local name = finder and finder.name or "族人"
        return {
            title = "采集野药",
            desc = name .. "上山砍柴时，于溪涧旁发现一片野生药材，根茎肥壮、叶色青翠，似是上好的黄芪与甘草。若小心采集，当可换些银钱度日。",
            choices = {
                {
                    text = "仔细采集，拿到集市去卖",
                    effect = function()
                        GameData.AddResource("silver", 3)
                        GameData.AddLog(name .. "采得野药，售于集市，得银三两。")
                        report.events[#report.events + 1] = name .. "采药得银三两"
                    end
                },
                {
                    text = "留作自家备用，以防疾病",
                    effect = function()
                        GameData.AddResource("grain", 5)
                        GameData.AddLog(name .. "采得野药留用，省下延医买药之资，折合粮食五斗。")
                        report.events[#report.events + 1] = name .. "采药留用，省下口粮"
                    end
                },
            }
        }
    end,
}

-- 2. 农具损坏
events[#events + 1] = {
    id = "r1_broken_tool",
    title = "农具损坏",
    rankRange = {1, 2},
    weight = 10,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetMembersByState("务农") > 0
    end,
    execute = function(s, report)
        local farmers = GameData.GetMembersByState("务农")
        local farmer = farmers[1]
        local name = farmer and farmer.name or "族人"
        return {
            title = "农具损坏",
            desc = name .. "耕田时，铁犁忽然断裂，犁头崩飞数尺。眼下正是农忙时节，若不及时修缮，只怕误了农时。镇上铁匠铺可修，只是要花些银钱。",
            choices = {
                {
                    text = "花银两请铁匠修理",
                    cost = {silver = 2},
                    effect = function()
                        GameData.AddLog(name .. "花二两银子修好了犁头，不误农时。")
                        report.events[#report.events + 1] = name .. "花银修犁"
                    end
                },
                {
                    text = "自己凑合用木犁替代",
                    effect = function()
                        GameData.AddResource("grain", -8)
                        GameData.AddLog(name .. "以木犁代铁犁，费力倍增，本季收成减损不少。")
                        report.events[#report.events + 1] = name .. "木犁代铁犁，收成减损"
                    end
                },
                {
                    text = "向邻家借用农具",
                    effect = function()
                        GameData.AddResource("grain", -3)
                        GameData.AddResource("fame", -1)
                        GameData.AddLog(name .. "厚着脸皮向邻家借犁，虽勉强耕完，却欠下人情。")
                        report.events[#report.events + 1] = name .. "借邻家犁具，略损颜面"
                    end
                },
            }
        }
    end,
}

-- 3. 邻里互助
events[#events + 1] = {
    id = "r1_neighbor_help",
    title = "邻里互助",
    rankRange = {1, 2},
    weight = 10,
    cooldownMonths = 3,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAdultMembers() > 0
    end,
    execute = function(s, report)
        local members = GameData.GetAdultMembers()
        local helper = members[1]
        local name = helper and helper.name or "族人"
        return {
            title = "邻里互助",
            desc = "邻家老翁卧病在床，家中无人照料田地，眼看庄稼将要荒废。" .. name .. "见此情形，心中不忍。若伸手相助，虽费些气力，但乡里乡亲的，日后也好相互照应。",
            choices = {
                {
                    text = "帮忙料理邻家田地",
                    effect = function()
                        GameData.AddResource("fame", 3)
                        GameData.AddLog(name .. "义助邻翁料理农田，乡邻交口称赞。")
                        report.events[#report.events + 1] = name .. "助邻耕田，声望渐长"
                    end
                },
                {
                    text = "送些粮食过去接济",
                    effect = function()
                        GameData.AddResource("grain", -5)
                        GameData.AddResource("fame", 2)
                        GameData.AddLog(name .. "赠粮五斗予病邻，虽家中清苦，却得邻里敬重。")
                        report.events[#report.events + 1] = name .. "赠粮济邻"
                    end
                },
                {
                    text = "自家也忙不过来，只能作罢",
                    effect = function()
                        GameData.AddResource("fame", -1)
                        GameData.AddLog("邻翁求助无果，乡里颇有微词。")
                        report.events[#report.events + 1] = "未助病邻，略失人心"
                    end
                },
            }
        }
    end,
}

-- 4. 野兽侵田
events[#events + 1] = {
    id = "r1_wild_animal",
    title = "野兽侵田",
    rankRange = {1, 2},
    weight = 9,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetMembersByState("务农") > 0
    end,
    execute = function(s, report)
        local farmers = GameData.GetMembersByState("务农")
        local farmer = farmers[1]
        local name = farmer and farmer.name or "族人"
        return {
            title = "野兽侵田",
            desc = "夜半时分，一群野猪闯入田间，拱翻了大片庄稼。" .. name .. "闻声赶来时，田地已被糟蹋得不像样子。野猪成群，个头不小，若强行驱赶恐有危险。",
            choices = {
                {
                    text = "召集族人持火把驱赶",
                    effect = function()
                        GameData.AddResource("grain", -5)
                        GameData.AddLog(name .. "率族人驱赶野猪，庄稼损失了一些，但好在及时止损。")
                        report.events[#report.events + 1] = "野猪侵田，庄稼小损"
                    end
                },
                {
                    text = "设陷阱捕猎野猪",
                    effect = function()
                        local luck = math.random(1, 10)
                        if luck <= 6 then
                            GameData.AddResource("grain", -3)
                            GameData.AddResource("silver", 2)
                            GameData.AddLog(name .. "设陷阱捕得一头野猪，卖了二两银子，田地小有损失。")
                            report.events[#report.events + 1] = "捕得野猪换银"
                        else
                            GameData.AddResource("grain", -8)
                            GameData.AddLog("陷阱未果，野猪又来糟蹋了一夜，损失颇重。")
                            report.events[#report.events + 1] = "捕猪未果，损失加重"
                        end
                    end
                },
            }
        }
    end,
}

-- 5. 祈雨仪式
events[#events + 1] = {
    id = "r1_rain_prayer",
    title = "祈雨仪式",
    rankRange = {1, 2},
    weight = 7,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAdultMembers() >= 2
    end,
    execute = function(s, report)
        return {
            title = "祈雨仪式",
            desc = "入夏以来滴雨未下，田地龟裂，庄稼枯黄。里正召集各户商议，拟请道士做法祈雨，每户须摊银若干。乡间传言，隔壁村去年祈雨后果然应验，但也有人说不过是巧合罢了。",
            choices = {
                {
                    text = "出银参与祈雨（-2银两）",
                    effect = function()
                        GameData.AddResource("silver", -2)
                        local luck = math.random(1, 10)
                        if luck <= 4 then
                            GameData.AddResource("grain", 10)
                            GameData.AddResource("fame", 2)
                            GameData.AddLog("祈雨之后三日果然天降甘霖，庄稼得救！族人出银有功，乡邻赞许。")
                            report.events[#report.events + 1] = "祈雨应验，旱情解除"
                        else
                            GameData.AddResource("fame", 1)
                            GameData.AddLog("祈雨法事做罢，天空依旧晴朗，银子倒是花了出去。好在出了份心意，邻里记着。")
                            report.events[#report.events + 1] = "祈雨未验，聊表心意"
                        end
                    end
                },
                {
                    text = "不出银，自家挑水浇地",
                    effect = function()
                        GameData.AddResource("grain", -5)
                        GameData.AddLog("未参与祈雨，自家挑水浇地，费力不小，收成仍减。")
                        report.events[#report.events + 1] = "旱天挑水，收成减损"
                    end
                },
            }
        }
    end,
}

-- 6. 游方僧人
events[#events + 1] = {
    id = "r1_wandering_monk",
    title = "游方僧人",
    rankRange = {1, 2},
    weight = 8,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return true
    end,
    execute = function(s, report)
        return {
            title = "游方僧人",
            desc = "一位行脚僧人路过村口，衣衫虽旧却浆洗得干干净净，面目慈祥。他自称从五台山来，一路化缘南下。听闻此僧颇通医术禅理，不少村民围上前去。",
            choices = {
                {
                    text = "施舍饭食招待",
                    effect = function()
                        GameData.AddResource("grain", -3)
                        GameData.AddResource("fame", 2)
                        GameData.AddLog("施舍行脚僧人饭食，僧人合掌道谢，为全家诵经祈福。乡邻皆赞善心。")
                        report.events[#report.events + 1] = "施舍游僧，积德行善"
                    end
                },
                {
                    text = "请教养生调理之法",
                    effect = function()
                        GameData.AddResource("grain", -2)
                        local members = GameData.GetAliveMembers()
                        for _, m in ipairs(members) do
                            if m.health and m.health < 80 then
                                m.health = math.min(100, m.health + 10)
                            end
                        end
                        GameData.AddLog("僧人传授几味药膳方子，族中体弱者试服之后，颇觉见效。")
                        report.events[#report.events + 1] = "得僧人指点养生之术"
                    end
                },
                {
                    text = "不予理会",
                    effect = function()
                        GameData.AddLog("僧人在村口歇了片刻便走了，未生波澜。")
                        report.events[#report.events + 1] = "游僧过村，未作理会"
                    end
                },
            }
        }
    end,
}

-- 7. 孩童天赋
events[#events + 1] = {
    id = "r1_child_talent",
    title = "孩童天赋",
    rankRange = {1, 2},
    weight = 6,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local members = GameData.GetAliveMembers()
        for _, m in ipairs(members) do
            if m.age and m.age >= 5 and m.age <= 14 then
                return true
            end
        end
        return false
    end,
    execute = function(s, report)
        local children = {}
        local members = GameData.GetAliveMembers()
        for _, m in ipairs(members) do
            if m.age and m.age >= 5 and m.age <= 14 then
                children[#children + 1] = m
            end
        end
        local child = children[math.random(1, #children)]
        local name = child.name or "孩童"
        return {
            title = "孩童天赋",
            desc = "私塾先生路过时见" .. name .. "在地上用树枝写字，竟然笔画工整、有模有样。先生大为惊讶，连道此子天资聪颖，若加以培养，日后必有出息。只是读书花费，对寒门来说着实不小。",
            choices = {
                {
                    text = "省吃俭用送去读书",
                    effect = function()
                        GameData.AddResource("silver", -3)
                        if child.stats then
                            child.stats["文"] = (child.stats["文"] or 0) + 5
                        end
                        GameData.AddLog(name .. "天资聪颖，家中节衣缩食送其入塾读书。")
                        report.events[#report.events + 1] = name .. "入塾启蒙"
                    end
                },
                {
                    text = "让孩子先帮家里干活",
                    effect = function()
                        GameData.AddResource("grain", 3)
                        GameData.AddLog("家贫难以供读，" .. name .. "虽有天赋，只能先帮衬家务。")
                        report.events[#report.events + 1] = name .. "天赋暂搁，帮衬家务"
                    end
                },
            }
        }
    end,
}

-- 8. 催税吏来
events[#events + 1] = {
    id = "r1_tax_collector",
    title = "催税吏来",
    rankRange = {1, 2},
    weight = 11,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return true
    end,
    execute = function(s, report)
        return {
            title = "催税吏来",
            desc = "县衙差役骑马而来，手持催税文书，高声宣读：'奉知县大人之命，限三日内缴清本季田赋，逾期者加收三成！'差役面色不善，村中人人自危。",
            choices = {
                {
                    text = "如数缴纳税银",
                    effect = function()
                        GameData.AddResource("silver", -3)
                        GameData.AddLog("如数缴了田赋银三两，差役收银后扬长而去。")
                        report.events[#report.events + 1] = "缴纳田赋银三两"
                    end
                },
                {
                    text = "用粮食折抵税银",
                    effect = function()
                        GameData.AddResource("grain", -10)
                        GameData.AddLog("银钱不凑手，只得用十斗粮食折抵税银，差役虽勉强收下，却嫌粮价折低了。")
                        report.events[#report.events + 1] = "以粮折税"
                    end
                },
                {
                    text = "哀求宽限些时日",
                    effect = function()
                        GameData.AddResource("fame", -2)
                        GameData.AddResource("silver", -1)
                        GameData.AddLog("苦苦哀求差役宽限，送了一两银子打点，暂得几日缓缓。只是跪地求人，颜面尽失。")
                        report.events[#report.events + 1] = "求缓税期，折了颜面"
                    end
                },
            }
        }
    end,
}

-- 9. 洪水预警
events[#events + 1] = {
    id = "r1_flood_warning",
    title = "洪水预警",
    rankRange = {1, 2},
    weight = 5,
    cooldownMonths = 18,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAdultMembers() >= 2
    end,
    execute = function(s, report)
        return {
            title = "洪水预警",
            desc = "连日暴雨不歇，上游来人传信说河水暴涨，恐有决堤之险。村中老人说，十年前发过一次大水，整村田地都泡了。眼下是提前转移粮食财物，还是赌一把老天开眼？",
            choices = {
                {
                    text = "立刻转移粮食到高处",
                    effect = function()
                        GameData.AddResource("silver", -1)
                        GameData.AddResource("grain", -3)
                        GameData.AddLog("全家老小冒雨将粮食搬到高处。虽折腾了一番，但若真发大水，损失可就小多了。")
                        report.events[#report.events + 1] = "提前转移粮食以防洪水"
                    end
                },
                {
                    text = "听天由命，不作准备",
                    effect = function()
                        local luck = math.random(1, 10)
                        if luck <= 5 then
                            GameData.AddResource("grain", -10)
                            GameData.AddResource("cloth", -3)
                            GameData.AddLog("大水果然漫过堤坝，田地被淹，存粮受损严重，布匹也被泡坏了。早知如此！")
                            report.events[#report.events + 1] = "洪水漫堤，损失惨重"
                        else
                            GameData.AddLog("所幸雨势渐小，河水虽涨却未决堤，虚惊一场。")
                            report.events[#report.events + 1] = "洪水虚惊，平安无事"
                        end
                    end
                },
            }
        }
    end,
}

-- 10. 流浪犬
events[#events + 1] = {
    id = "r1_stray_dog",
    title = "流浪犬",
    rankRange = {1, 6},
    weight = 8,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        -- 只有没有存活宠物时才触发（无宠物 或 宠物已死）
        if s.pet and s.pet.alive then return false end
        return true
    end,
    execute = function(s, report)
        return {
            title = "流浪犬",
            desc = "一条瘦骨嶙峋的黄狗蹲在家门口，见人也不跑，只摇着尾巴可怜巴巴地望着。看样子是哪家走失的看门犬，虽然瘦弱，但目光机警、品相不差。",
            choices = {
                {
                    text = "收养它，养成看门犬",
                    effect = function()
                        GameData.AddResource("grain", -2)
                        GameData.AddResource("fame", 1)
                        local dogNames = {"大黄", "旺财", "阿福", "来福", "黑虎", "小花", "毛毛", "虎子"}
                        local name = dogNames[math.random(1, #dogNames)]
                        -- 狗寿命8~14年
                        local lifespan = math.random(8, 14)
                        s.pet = {
                            type = "dog",
                            name = name,
                            adoptYear = s.year,
                            adoptMonth = s.month,
                            lifespan = lifespan,
                            alive = true,
                            deathYear = nil,
                            deathMonth = nil,
                        }
                        GameData.AddLog("收养了流浪黄犬「" .. name .. "」，日后看家护院、驱赶野兽，倒是颇为得力。")
                        report.events[#report.events + 1] = "收养流浪犬「" .. name .. "」看家护院"
                    end
                },
                {
                    text = "给些吃的打发走",
                    effect = function()
                        GameData.AddResource("grain", -1)
                        GameData.AddLog("给了流浪犬一碗剩饭，它吃完便摇着尾巴走了。")
                        report.events[#report.events + 1] = "施食流浪犬后放走"
                    end
                },
            }
        }
    end,
}

-- 11. 挖井取水
events[#events + 1] = {
    id = "r1_old_well",
    title = "挖井取水",
    rankRange = {1, 2},
    weight = 6,
    cooldownMonths = 24,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAdultMembers() >= 3
    end,
    execute = function(s, report)
        return {
            title = "挖井取水",
            desc = "村中取水一直靠河边挑运，路远费力。里正提议合力在村中打一口水井，各户分摊费用与人力。若成了，日后取水便利，浇地也省事。只是打井费用不小，且不知挖多深才能见水。",
            choices = {
                {
                    text = "出银出力参与打井",
                    effect = function()
                        GameData.AddResource("silver", -3)
                        GameData.AddResource("grain", 8)
                        GameData.AddResource("fame", 3)
                        GameData.AddLog("出银三两、出力数日，水井终于挖成。清冽井水汩汩而出，日后浇田取水大为便利。族人出力有目共睹，乡邻称颂。")
                        report.events[#report.events + 1] = "合力打井成功，获声望"
                    end
                },
                {
                    text = "出少许银两意思一下",
                    effect = function()
                        GameData.AddResource("silver", -1)
                        GameData.AddResource("grain", 3)
                        GameData.AddLog("象征性出了一两银子，井倒是打成了，但出力少，分水时排在后头。")
                        report.events[#report.events + 1] = "少出银打井，受益有限"
                    end
                },
                {
                    text = "不参与，继续挑水",
                    effect = function()
                        GameData.AddResource("fame", -2)
                        GameData.AddLog("未参与打井，井成之后被乡邻说嘴，取水也不好意思去新井。")
                        report.events[#report.events + 1] = "未参与打井，失了人心"
                    end
                },
            }
        }
    end,
}

-- 12. 赶集日
events[#events + 1] = {
    id = "r1_marketplace",
    title = "赶集日",
    rankRange = {1, 2},
    weight = 12,
    cooldownMonths = 3,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAdultMembers() > 0
    end,
    execute = function(s, report)
        local members = GameData.GetAdultMembers()
        local trader = members[math.random(1, #members)]
        local name = trader.name or "族人"
        return {
            title = "赶集日",
            desc = "逢五逢十是镇上的赶集日，" .. name .. "一早便挑着担子出了门。集市上人头攒动，叫卖声此起彼伏。有人卖新织的土布，价钱公道；也可趁机把家里多余的粮食换些银钱。",
            choices = {
                {
                    text = "卖粮换银",
                    effect = function()
                        GameData.AddResource("grain", -8)
                        GameData.AddResource("silver", 4)
                        GameData.AddLog(name .. "赶集卖了八斗粮食，得银四两。")
                        report.events[#report.events + 1] = name .. "赶集卖粮得银"
                    end
                },
                {
                    text = "买些布匹回来",
                    effect = function()
                        GameData.AddResource("silver", -2)
                        GameData.AddResource("cloth", 5)
                        GameData.AddLog(name .. "赶集买了五匹土布，价钱还算公道。")
                        report.events[#report.events + 1] = name .. "赶集买布"
                    end
                },
                {
                    text = "只是逛逛，打听消息",
                    effect = function()
                        GameData.AddResource("fame", 1)
                        GameData.AddLog(name .. "赶集时结识了几位外乡人，谈笑甚欢，消息也灵通了些。")
                        report.events[#report.events + 1] = name .. "赶集交游"
                    end
                },
            }
        }
    end,
}

-- 13. 盗贼探路
events[#events + 1] = {
    id = "r1_bandit_scout",
    title = "盗贼探路",
    rankRange = {1, 2},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAdultMembers() > 0
    end,
    execute = function(s, report)
        local members = GameData.GetAdultMembers()
        local scout = members[1]
        local name = scout.name or "族人"
        return {
            title = "盗贼探路",
            desc = name .. "在村外发现几个形迹可疑之人，鬼鬼祟祟地打量各家院落。细看之下，这些人面目粗犷，腰间似藏有短刀。恐怕是山上的毛贼下来踩点，若不加防范，夜里只怕要出事。",
            choices = {
                {
                    text = "通知里正组织巡夜",
                    effect = function()
                        GameData.AddResource("fame", 3)
                        GameData.AddLog(name .. "警觉发现贼人踩点，及时通知里正。村中连夜巡逻，贼人见有防备便散了。")
                        report.events[#report.events + 1] = name .. "发觉贼踪，组织巡夜"
                    end
                },
                {
                    text = "只管好自家门户",
                    effect = function()
                        local luck = math.random(1, 10)
                        if luck <= 3 then
                            GameData.AddResource("silver", -2)
                            GameData.AddLog("夜里贼人果然来了，邻家被盗了不少东西，自家也丢了些银钱。")
                            report.events[#report.events + 1] = "夜盗来袭，自家失银"
                        else
                            GameData.AddLog("锁好了门窗，夜里倒也平安无事。只是邻家似乎丢了些东西。")
                            report.events[#report.events + 1] = "锁门自保，安然无恙"
                        end
                    end
                },
            }
        }
    end,
}

-- 14. 草药知识
events[#events + 1] = {
    id = "r1_herb_knowledge",
    title = "草药知识",
    rankRange = {1, 2},
    weight = 7,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAdultMembers() > 0
    end,
    execute = function(s, report)
        local members = GameData.GetAdultMembers()
        local learner = members[math.random(1, #members)]
        local name = learner.name or "族人"
        return {
            title = "草药知识",
            desc = "村中有位年迈的采药婆婆，一身辨药识草的本事无人传承。" .. name .. "偶然帮她挑了回柴，老婆婆甚为感动，说愿意教些辨识草药的本事。虽不是什么大学问，但乡间缺医少药，能识得几味常用药草也是好的。",
            choices = {
                {
                    text = "跟着学习辨识草药",
                    effect = function()
                        if learner.stats then
                            learner.stats["文"] = (learner.stats["文"] or 0) + 2
                        end
                        GameData.AddResource("fame", 1)
                        GameData.AddLog(name .. "随采药婆婆学了半月草药知识，识得黄连、半夏等十余味常用药材。")
                        report.events[#report.events + 1] = name .. "习得草药知识"
                    end
                },
                {
                    text = "太忙了，改日再说",
                    effect = function()
                        GameData.AddLog(name .. "无暇学习，采药婆婆也不勉强，只叹后继无人。")
                        report.events[#report.events + 1] = "错过学药机会"
                    end
                },
            }
        }
    end,
}

-- 15. 断桥修缮
events[#events + 1] = {
    id = "r1_broken_bridge",
    title = "断桥修缮",
    rankRange = {1, 2},
    weight = 6,
    cooldownMonths = 18,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAdultMembers() >= 2
    end,
    execute = function(s, report)
        return {
            title = "断桥修缮",
            desc = "村口小石桥年久失修，前日有人过桥时踩断了桥板，险些跌入河中。这桥是通往镇上的必经之路，若不修缮，赶集、送粮都要绕远路。里正号召各户出资出力修桥，但人人都嫌费事。",
            choices = {
                {
                    text = "带头出资修桥（-3银两）",
                    effect = function()
                        GameData.AddResource("silver", -3)
                        GameData.AddResource("fame", 5)
                        GameData.AddLog("带头出银三两修桥，乡邻纷纷响应。桥修好后，路人皆赞吾家仁义。")
                        report.events[#report.events + 1] = "带头修桥，声望大增"
                    end
                },
                {
                    text = "出些力气帮忙搬石头",
                    effect = function()
                        GameData.AddResource("fame", 2)
                        GameData.AddLog("虽无余银，但出力搬石修桥，乡邻也记着这份情。")
                        report.events[#report.events + 1] = "出力修桥"
                    end
                },
                {
                    text = "不参与，绕路走",
                    effect = function()
                        GameData.AddResource("fame", -1)
                        GameData.AddLog("未参与修桥，日后过桥时颇觉不好意思。")
                        report.events[#report.events + 1] = "未参与修桥"
                    end
                },
            }
        }
    end,
}

-- 16. 蝗灾来袭
events[#events + 1] = {
    id = "r1_locust",
    title = "蝗灾来袭",
    rankRange = {1, 2},
    weight = 4,
    cooldownMonths = 24,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetMembersByState("务农") > 0
    end,
    execute = function(s, report)
        return {
            title = "蝗灾来袭",
            desc = "晴空之下忽然暗了下来，抬头望去，铺天盖地的蝗虫从北方涌来，密密麻麻遮天蔽日。蝗虫落处，庄稼转瞬被啃食殆尽，田间一片狼藉。村中老幼惊恐万分，妇人孩童哭声一片。",
            choices = {
                {
                    text = "全家上阵扑打蝗虫",
                    effect = function()
                        GameData.AddResource("grain", -8)
                        GameData.AddLog("全家持扫帚、竹竿奋力扑打蝗虫，虽拼尽全力，田地仍损失过半。但比起邻家颗粒无收，已算幸运。")
                        report.events[#report.events + 1] = "蝗灾来袭，奋力抢救，损失减半"
                    end
                },
                {
                    text = "点火烧田驱蝗",
                    effect = function()
                        local luck = math.random(1, 10)
                        if luck <= 5 then
                            GameData.AddResource("grain", -5)
                            GameData.AddLog("火烧蝗虫颇有成效，驱散了大部分蝗群，田地损失不算太大。")
                            report.events[#report.events + 1] = "火攻驱蝗，损失可控"
                        else
                            GameData.AddResource("grain", -10)
                            GameData.AddLog("火势失控烧了不少庄稼，蝗虫虽散，自家田地也毁了大半。得不偿失。")
                            report.events[#report.events + 1] = "火攻失控，损失惨重"
                        end
                    end
                },
            }
        }
    end,
}

-- 17. 好心路人
events[#events + 1] = {
    id = "r1_kind_stranger",
    title = "好心路人",
    rankRange = {1, 2},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return true
    end,
    execute = function(s, report)
        local members = GameData.GetAdultMembers()
        local receiver = members[1]
        local name = receiver and receiver.name or "族人"
        return {
            title = "好心路人",
            desc = name .. "挑着柴担赶路时扭伤了脚，正苦于无人相助之际，一位过路的行商见状停下车马，不仅帮忙包扎了伤脚，还捎了一程。临别时行商还留下些干粮和一小包伤药。",
            choices = {
                {
                    text = "感恩记下，日后报答",
                    effect = function()
                        GameData.AddResource("grain", 3)
                        GameData.AddResource("fame", 1)
                        GameData.AddLog(name .. "受好心行商相助，得干粮数斤。记下恩情，日后定当报答。")
                        report.events[#report.events + 1] = "得路人相助"
                    end
                },
                {
                    text = "追问行商姓名来路",
                    effect = function()
                        GameData.AddResource("grain", 3)
                        GameData.AddResource("silver", 1)
                        GameData.AddLog(name .. "与行商攀谈甚欢，行商见其诚恳，又赠了一两银子做盘缠，约好下次赶集再叙。")
                        report.events[#report.events + 1] = "结识好心行商"
                    end
                },
            }
        }
    end,
}

-- 18. 邻里纠纷
events[#events + 1] = {
    id = "r1_family_feud",
    title = "邻里纠纷",
    rankRange = {1, 2},
    weight = 9,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAdultMembers() > 0
    end,
    execute = function(s, report)
        local members = GameData.GetAdultMembers()
        local mediator = members[1]
        local name = mediator.name or "族人"
        return {
            title = "邻里纠纷",
            desc = "东邻赵家与西邻李家因田界争执吵得不可开交，双方各执一词，险些动手。里正头疼不已，见" .. name .. "为人厚道，便请其出面调解。这事若处理得好，两家都记情分；若偏帮一方，反倒得罪人。",
            choices = {
                {
                    text = "公正调解，两家各让一步",
                    effect = function()
                        GameData.AddResource("fame", 3)
                        GameData.AddLog(name .. "秉公调解邻里田界纠纷，两家各让半分地，握手言和。乡邻皆赞其公道。")
                        report.events[#report.events + 1] = name .. "调解邻里纷争，声望渐隆"
                    end
                },
                {
                    text = "请两家喝酒化解矛盾",
                    effect = function()
                        GameData.AddResource("silver", -1)
                        GameData.AddResource("fame", 2)
                        GameData.AddLog(name .. "自掏腰包请两家吃酒，酒过三巡，恩怨也化了大半。")
                        report.events[#report.events + 1] = name .. "请酒化纷争"
                    end
                },
                {
                    text = "不掺和这种事",
                    effect = function()
                        GameData.AddLog("推辞了里正的请托，两家闹了许久才不了了之。")
                        report.events[#report.events + 1] = "邻里纷争未予调解"
                    end
                },
            }
        }
    end,
}

-- 19. 丰收庆典
events[#events + 1] = {
    id = "r1_autumn_harvest",
    title = "丰收庆典",
    rankRange = {1, 2},
    weight = 8,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetMembersByState("务农") > 0
    end,
    execute = function(s, report)
        return {
            title = "丰收庆典",
            desc = "今年风调雨顺，秋收时节田间沉甸甸的稻穗压弯了腰。里正张罗着要办一场丰收庆典，杀猪宰羊、鸣锣击鼓，请全村老少吃一顿好的。各家酌情随份子。",
            choices = {
                {
                    text = "慷慨随份子庆贺",
                    effect = function()
                        GameData.AddResource("silver", -1)
                        GameData.AddResource("grain", 15)
                        GameData.AddResource("fame", 2)
                        GameData.AddLog("丰年大收，随了份子参与庆典，仓中粮满。与乡邻推杯换盏，其乐融融。")
                        report.events[#report.events + 1] = "丰收庆典，粮仓充盈"
                    end
                },
                {
                    text = "不参加庆典，专心收粮入仓",
                    effect = function()
                        GameData.AddResource("grain", 12)
                        GameData.AddLog("丰年好收成，忙着收粮入仓，未参加庆典。粮食倒是存够了。")
                        report.events[#report.events + 1] = "丰收入仓"
                    end
                },
            }
        }
    end,
}

-- 20. 寒冬难耐
events[#events + 1] = {
    id = "r1_cold_winter",
    title = "寒冬难耐",
    rankRange = {1, 2},
    weight = 6,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAliveMembers() > 0
    end,
    execute = function(s, report)
        return {
            title = "寒冬难耐",
            desc = "今冬格外寒冷，北风呼啸，滴水成冰。屋内即便生了炭火，仍觉寒气逼人。老人小孩冻得瑟瑟发抖，若不多添衣被御寒，恐怕要冻出病来。家中布匹不多，也可花银子买些棉花炭火。",
            choices = {
                {
                    text = "用布匹赶制棉衣御寒",
                    effect = function()
                        GameData.AddResource("cloth", -4)
                        GameData.AddLog("赶制了几件棉衣，一家老小总算熬过了这个寒冬。布匹消耗不少。")
                        report.events[#report.events + 1] = "用布匹赶制棉衣过冬"
                    end
                },
                {
                    text = "花银子买炭火取暖",
                    effect = function()
                        GameData.AddResource("silver", -2)
                        GameData.AddLog("花了二两银子买了几篓木炭，虽心疼银钱，好歹屋里暖和了些。")
                        report.events[#report.events + 1] = "买炭取暖过严冬"
                    end
                },
                {
                    text = "硬扛过去，省下物资",
                    effect = function()
                        local members = GameData.GetAliveMembers()
                        local weakened = 0
                        for _, m in ipairs(members) do
                            if m.health then
                                m.health = math.max(10, m.health - 15)
                                weakened = weakened + 1
                            end
                        end
                        GameData.AddLog("硬撑着过了寒冬，全家" .. weakened .. "口人都冻病了，身子大不如前。")
                        report.events[#report.events + 1] = "严冬硬扛，全家体弱"
                    end
                },
            }
        }
    end,
}

-- 21. 拾金不昧
events[#events + 1] = {
    id = "r1_found_silver",
    title = "拾金不昧",
    rankRange = {1, 2},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAdultMembers() > 0
    end,
    execute = function(s, report)
        local members = GameData.GetAdultMembers()
        local finder = members[math.random(1, #members)]
        local name = finder.name or "族人"
        return {
            title = "拾金不昧",
            desc = name .. "在田埂上捡到一个布包，打开一看，竟有碎银五两之多。银子上没有记号，四下无人看见。若交还里正寻找失主，自然是积德之举；若悄悄收下，家中也正缺银用。",
            choices = {
                {
                    text = "交给里正寻找失主",
                    effect = function()
                        GameData.AddResource("fame", 5)
                        GameData.AddLog(name .. "拾银交公，失主寻来千恩万谢。此事传开，人人称赞其品行端正。")
                        report.events[#report.events + 1] = name .. "拾金不昧，声望大增"
                    end
                },
                {
                    text = "悄悄收下补贴家用",
                    effect = function()
                        GameData.AddResource("silver", 5)
                        GameData.AddResource("fame", -2)
                        GameData.AddLog(name .. "将捡到的银两悄悄收下。虽解了燃眉之急，心中却总不大安稳。")
                        report.events[#report.events + 1] = name .. "私吞拾银"
                    end
                },
            }
        }
    end,
}

-- 22. 夜盗来袭
events[#events + 1] = {
    id = "r1_night_thief",
    title = "夜盗来袭",
    rankRange = {1, 2},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAliveMembers() > 0
    end,
    execute = function(s, report)
        local warriors = GameData.GetMembersByState("习武")
        local hasWarrior = #warriors > 0
        return {
            title = "夜盗来袭",
            desc = "深夜犬吠不止，有人翻墙而入。等家人惊醒时，院中已有黑影闪动。" ..
                (hasWarrior and ("好在" .. warriors[1].name .. "习过武艺，持棍追了出去。") or "家中无人习武，只能大声呼救。"),
            choices = {
                {
                    text = hasWarrior and "让习武之人追赶盗贼" or "大声呼救惊动邻里",
                    effect = function()
                        if hasWarrior then
                            GameData.AddResource("silver", -1)
                            GameData.AddResource("fame", 2)
                            GameData.AddLog(warriors[1].name .. "持棍追赶夜盗，贼人仓皇翻墙而逃，只丢了些散碎银钱。")
                            report.events[#report.events + 1] = warriors[1].name .. "驱赶夜盗"
                        else
                            GameData.AddResource("silver", -2)
                            GameData.AddLog("呼救惊动四邻，贼人闻声而逃，但已窃走银两。")
                            report.events[#report.events + 1] = "夜盗入室，失银二两"
                        end
                    end
                },
                {
                    text = "躲在屋里不出声，等贼走了再查看",
                    effect = function()
                        GameData.AddResource("silver", -3)
                        GameData.AddResource("grain", -5)
                        GameData.AddLog("一家人躲在屋里大气不敢出，贼人从容搜刮了一番才走。银粮皆有损失。")
                        report.events[#report.events + 1] = "夜盗从容窃财，损失不小"
                    end
                },
            }
        }
    end,
}

-- 23. 喜事邀请
events[#events + 1] = {
    id = "r1_wedding_invite",
    title = "喜事邀请",
    rankRange = {1, 2},
    weight = 9,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAdultMembers() > 0
    end,
    execute = function(s, report)
        local members = GameData.GetAdultMembers()
        local attendee = members[1]
        local name = attendee.name or "族人"
        return {
            title = "喜事邀请",
            desc = "村中张家的儿子要娶亲了，大红喜帖早早送到。张家虽也是寒门，但为人厚道，平日里没少帮衬乡邻。这喜宴自然要去，只是份子钱多少合适，得掂量掂量。",
            choices = {
                {
                    text = "备一份体面的贺礼",
                    effect = function()
                        GameData.AddResource("silver", -2)
                        GameData.AddResource("fame", 3)
                        GameData.AddLog(name .. "备了二两银子的贺礼赴宴，张家大为感动。席间觥筹交错，宾主尽欢。")
                        report.events[#report.events + 1] = name .. "赴宴贺喜，与邻交好"
                    end
                },
                {
                    text = "送些自家粮食布匹",
                    effect = function()
                        GameData.AddResource("grain", -5)
                        GameData.AddResource("cloth", -2)
                        GameData.AddResource("fame", 2)
                        GameData.AddLog(name .. "送了粮食布匹做贺礼，虽不贵重却是实在物件，张家也很欢喜。")
                        report.events[#report.events + 1] = name .. "送粮布贺喜"
                    end
                },
                {
                    text = "随个小份子意思一下",
                    effect = function()
                        GameData.AddResource("silver", -1)
                        GameData.AddResource("fame", 1)
                        GameData.AddLog(name .. "随了一两银子的份子赴宴，不算多，但好歹到了场。")
                        report.events[#report.events + 1] = name .. "小份子赴喜宴"
                    end
                },
            }
        }
    end,
}

-- 24. 瘟疫传闻
events[#events + 1] = {
    id = "r1_plague_rumor",
    title = "瘟疫传闻",
    rankRange = {1, 2},
    weight = 5,
    cooldownMonths = 18,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAliveMembers() >= 3
    end,
    execute = function(s, report)
        return {
            title = "瘟疫传闻",
            desc = "赶集回来的人带回消息，说隔壁县闹起了疫病，已经死了好些人。一时间村中人心惶惶，有人忙着烧醋熏屋，有人去庙里烧香求佛。虽不知传闻真假，但防患于未然总没有错。",
            choices = {
                {
                    text = "买些草药提前预防",
                    effect = function()
                        GameData.AddResource("silver", -2)
                        local members = GameData.GetAliveMembers()
                        for _, m in ipairs(members) do
                            if m.health then
                                m.health = math.min(100, m.health + 5)
                            end
                        end
                        GameData.AddLog("花银二两买了艾草、苍术等药材，煎水饮服、燃烧熏房，全家做足了防疫准备。")
                        report.events[#report.events + 1] = "购药防疫，全家安泰"
                    end
                },
                {
                    text = "减少外出，闭门不出",
                    effect = function()
                        GameData.AddResource("grain", -3)
                        GameData.AddLog("闭门居家半月有余，不敢出门赶集。虽安全了些，但存粮消耗不小。")
                        report.events[#report.events + 1] = "闭门避疫"
                    end
                },
                {
                    text = "不予理会，照常生活",
                    effect = function()
                        local luck = math.random(1, 10)
                        if luck <= 2 then
                            local members = GameData.GetAliveMembers()
                            local victim = members[math.random(1, #members)]
                            if victim.health then
                                victim.health = math.max(20, victim.health - 25)
                            end
                            GameData.AddLog("疫病果然传到了村里，" .. (victim.name or "族人") .. "不幸染病，卧床多日。")
                            report.events[#report.events + 1] = (victim.name or "族人") .. "染疫卧病"
                        else
                            GameData.AddLog("所幸疫病并未传来，虚惊一场。")
                            report.events[#report.events + 1] = "疫病传闻，虚惊一场"
                        end
                    end
                },
            }
        }
    end,
}

-- 25. 春耕动员
events[#events + 1] = {
    id = "r1_spring_plowing",
    title = "春耕动员",
    rankRange = {1, 2},
    weight = 10,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return #GameData.GetAdultMembers() >= 2
    end,
    execute = function(s, report)
        local farmers = GameData.GetMembersByState("务农")
        local adults = GameData.GetAdultMembers()
        local farmerCount = #farmers
        return {
            title = "春耕动员",
            desc = "春回大地，万物复苏。田间的积雪刚化尽，正是翻地播种的好时节。今年若能早些动手、多下些功夫，秋天的收成必然好上几分。只是春耕劳累，全家都得齐上阵。",
            choices = {
                {
                    text = "全力投入春耕，购置良种",
                    effect = function()
                        GameData.AddResource("silver", -2)
                        GameData.AddResource("grain", 12)
                        GameData.AddLog("花银二两购得良种，全家老少齐上阵，日出而作日落而息。辛苦一季，秋来必有好收成。")
                        report.events[#report.events + 1] = "精耕细作，购良种播种"
                    end
                },
                {
                    text = "按部就班耕种",
                    effect = function()
                        GameData.AddResource("grain", 8)
                        GameData.AddLog("照往年的老法子耕种，不求有功但求无过。收成马马虎虎。")
                        report.events[#report.events + 1] = "春耕如常"
                    end
                },
                {
                    text = "减少耕种面积，抽人去做别的",
                    effect = function()
                        GameData.AddResource("grain", 4)
                        GameData.AddResource("silver", 2)
                        GameData.AddLog("减了些田地耕种，抽出人手去镇上做些零工赚钱。粮食少产了，银子倒多了些。")
                        report.events[#report.events + 1] = "减耕打工，银粮两分"
                    end
                },
            }
        }
    end,
}

return events
