local GameData = require("Data.GameData")
local events = {}

-- 1. 耕牛生病
events[#events + 1] = {
    id = "r2_cattle_sick",
    title = "耕牛生病",
    rankRange = {2, 3},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "家中耕牛突染疫病，卧地不起，口涎不止。"
        GameData.AddLog("耕牛染病，需做决断。")
        return {
            title = "耕牛生病",
            desc = "春耕在即，家中那头老黄牛突然卧地不起，浑身发烫，口中涎水不止。兽医郎中诊过后说尚有救治之望，只是药材费用不菲。若不治，怕是只能趁早卖与屠户，多少换些银钱。",
            choices = {
                { text = "花银两请郎中治牛（-5银两，牛康复可保春耕）", effect = function()
                    GameData.AddResource("silver", -5)
                    GameData.AddResource("grain", 8)
                    GameData.AddLog("花银治好了耕牛，春耕顺利，多收了粮食。")
                end },
                { text = "忍痛卖与屠户（+3银两，来年春耕艰难）", effect = function()
                    GameData.AddResource("silver", 3)
                    GameData.AddResource("grain", -8)
                    GameData.AddLog("卖掉病牛得了些银钱，来年春耕只能靠人力了。")
                end },
                { text = "不管不顾，听天由命", effect = function()
                    GameData.AddResource("grain", -5)
                    GameData.AddLog("耕牛病死，春耕受阻，粮食减产。")
                end },
            }
        }
    end,
}

-- 2. 新作物种子
events[#events + 1] = {
    id = "r2_new_crop",
    title = "新作物种子",
    rankRange = {2, 3},
    weight = 7,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "有外乡客商带来了番邦新作物种子，据说产量甚高。"
        GameData.AddLog("外乡客商推销新作物种子。")
        return {
            title = "新作物种子",
            desc = "一位自称走过南洋的客商路过村口，兜售一种从番邦带来的作物种子，说此物耐旱高产，若试种成功，来年收成可翻一番。只是谁也没见过这东西，能否在本地扎根尚未可知。",
            choices = {
                { text = "花银两买种子试种（-4银两，有机会大丰收）", effect = function()
                    GameData.AddResource("silver", -4)
                    if math.random() > 0.4 then
                        GameData.AddResource("grain", 15)
                        GameData.AddLog("新种子试种成功，产量喜人，粮仓满溢。")
                    else
                        GameData.AddResource("grain", -3)
                        GameData.AddLog("新种子水土不服，枯死大半，白费了功夫。")
                    end
                end },
                { text = "只买少量试试（-2银两，小规模尝试）", effect = function()
                    GameData.AddResource("silver", -2)
                    if math.random() > 0.3 then
                        GameData.AddResource("grain", 6)
                        GameData.AddLog("小量试种新作物，略有收获。")
                    else
                        GameData.AddLog("少量新种子没有发芽，损失不大。")
                    end
                end },
                { text = "不冒这个险，照旧种地", effect = function()
                    GameData.AddLog("婉拒了客商，还是种自家老种子稳妥。")
                end },
            }
        }
    end,
}

-- 3. 水源争端
events[#events + 1] = {
    id = "r2_water_dispute",
    title = "水源争端",
    rankRange = {2, 3},
    weight = 8,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "邻村截断上游水渠，与本村争夺灌溉水源。"
        GameData.AddLog("邻村截水，水源争端骤起。")
        return {
            title = "水源争端",
            desc = "入夏以来雨水稀少，邻村李家庄在上游筑坝截水，本村灌溉用水骤减，眼看秧苗要旱死在地里。村中青壮已聚在祠堂商议对策，有人要去拆坝，有人说不如请里正调解。",
            choices = {
                { text = "出银两请里正调解（-3银两，和平解决）", effect = function()
                    GameData.AddResource("silver", -3)
                    GameData.AddResource("fame", 3)
                    GameData.AddLog("请里正出面调解水源纠纷，两村各退一步，名声有所提升。")
                end },
                { text = "组织族人去理论（可能冲突，但不花钱）", effect = function()
                    if math.random() > 0.5 then
                        GameData.AddResource("fame", -2)
                        GameData.AddLog("与邻村人起了冲突，虽夺回了水源，但结了怨仇。")
                    else
                        GameData.AddResource("grain", 5)
                        GameData.AddLog("族人据理力争，邻村理亏退让，水源恢复。")
                    end
                end },
                { text = "自家挖井取水（-4银两，一劳永逸）", effect = function()
                    GameData.AddResource("silver", -4)
                    GameData.AddResource("grain", 3)
                    GameData.AddLog("花钱雇人打了口深井，从此不必争水。")
                end },
            }
        }
    end,
}

-- 4. 媒婆上门
events[#events + 1] = {
    id = "r2_matchmaker",
    title = "媒婆上门",
    rankRange = {2, 3},
    weight = 7,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        for _, m in ipairs(adults) do
            if m.alive and not m.spouseId and m.age >= 16 then
                return true
            end
        end
        return false
    end,
    execute = function(s, report)
        local candidate = nil
        local adults = GameData.GetAdultMembers()
        for _, m in ipairs(adults) do
            if m.alive and not m.spouseId and m.age >= 16 then
                candidate = m
                break
            end
        end
        local cName = candidate and candidate.name or "族人"
        report.events[#report.events + 1] = "王媒婆上门，为" .. cName .. "说亲。"
        GameData.AddLog("媒婆登门，要为" .. cName .. "保媒。")
        return {
            title = "媒婆上门",
            desc = "村里的王媒婆笑盈盈地登了门，说邻村有户殷实人家，愿与咱家结亲。" .. cName .. "年纪也不小了，这门亲事门当户对。是否前去相看？",
            choices = {
                { text = "前去相看", effect = function()
                    if candidate and not candidate.spouseId then
                        local GS = require("UI.GameScreen")
                        GS.ShowMarriageTierSelect(candidate)
                    end
                end },
                { text = "婉言谢绝，再等等看", effect = function()
                    GameData.AddLog("婉拒了媒婆，" .. cName .. "的亲事暂且搁下。")
                end },
            }
        }
    end,
}

-- 5. 购地机会
events[#events + 1] = {
    id = "r2_land_offer",
    title = "购地机会",
    rankRange = {2, 3},
    weight = 6,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "邻家急需用钱，愿低价出让一亩水田。"
        GameData.AddLog("有田地低价出售的机会。")
        return {
            title = "购地机会",
            desc = "邻家老张因儿子进京赶考急需盘缠，愿将自家一亩上好水田低价出让。这块田紧挨着咱家的地，灌溉方便，实属难得的好买卖。只是手头银两有限，需得掂量掂量。",
            choices = {
                { text = "倾力买下（-8银两，长远收益）", effect = function()
                    GameData.AddResource("silver", -8)
                    GameData.AddResource("grain", 10)
                    GameData.AddLog("买下了一亩良田，来年多了不少收成。")
                end },
                { text = "还价一番再买（-5银两）", effect = function()
                    GameData.AddResource("silver", -5)
                    if math.random() > 0.4 then
                        GameData.AddResource("grain", 6)
                        GameData.AddLog("压了价钱买下田地，虽不算最好的，也还过得去。")
                    else
                        GameData.AddLog("还价太狠，老张不肯卖了，田被别家买走。")
                    end
                end },
                { text = "手头不宽裕，只能作罢", effect = function()
                    GameData.AddLog("无力购田，只好眼看着好田被别家买走。")
                end },
            }
        }
    end,
}

-- 6. 县城庙会
events[#events + 1] = {
    id = "r2_county_fair",
    title = "县城庙会",
    rankRange = {2, 3},
    weight = 9,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "县城逢庙会，热闹非凡，可去赶集。"
        GameData.AddLog("县城庙会将至，考虑赶集。")
        return {
            title = "县城庙会",
            desc = "一年一度的县城庙会开场了，十里八乡的货郎、艺人、商贩齐聚于此。听说今年庙会格外热闹，粮价也比平时公道些。带上家里的土产去卖，再添置些用度，也是美事一桩。",
            choices = {
                { text = "带货物去赶集交易（+5银两，+2布匹）", effect = function()
                    GameData.AddResource("silver", 5)
                    GameData.AddResource("cloth", 2)
                    GameData.AddResource("grain", -5)
                    GameData.AddLog("赶庙会卖了些粮食土产，换回银两和布匹。")
                end },
                { text = "只去看看热闹，少量采买（-2银两）", effect = function()
                    GameData.AddResource("silver", -2)
                    GameData.AddResource("cloth", 1)
                    GameData.AddLog("逛了庙会，买了些针线布匹回来。")
                end },
                { text = "路途遥远，不去了", effect = function()
                    GameData.AddLog("没去赶庙会，安心在家侍弄庄稼。")
                end },
            }
        }
    end,
}

-- 7. 旱灾来临
events[#events + 1] = {
    id = "r2_drought",
    title = "旱灾来临",
    rankRange = {2, 3},
    weight = 5,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "天降大旱，连月不雨，田地龟裂，庄稼枯萎。"
        GameData.AddLog("大旱之年，颗粒无收。")
        return {
            title = "旱灾来临",
            desc = "自入夏以来滴雨未降，烈日当空，田间土地龟裂如瓦，庄稼尽皆枯死。村中水井也将干涸，人畜饮水都成了问题。官府虽说会放赈，但远水难解近渴。",
            choices = {
                { text = "花银两挖渠引远水（-5银两，减轻损失）", effect = function()
                    GameData.AddResource("silver", -5)
                    GameData.AddResource("grain", -8)
                    GameData.AddLog("出钱引水，保住了部分庄稼，损失不算太惨。")
                end },
                { text = "开仓放粮撑过旱期（-15粮食）", effect = function()
                    GameData.AddResource("grain", -15)
                    GameData.AddResource("fame", 2)
                    GameData.AddLog("靠存粮度过了旱灾，粮仓见底，但人都活了下来。")
                end },
                { text = "祈雨求天，听天由命", effect = function()
                    GameData.AddResource("grain", -12)
                    GameData.AddResource("fame", -1)
                    GameData.AddLog("设坛祈雨无果，损失惨重。")
                end },
            }
        }
    end,
}

-- 8. 粮商收购
events[#events + 1] = {
    id = "r2_grain_merchant",
    title = "粮商收购",
    rankRange = {2, 3},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "外地粮商到村中高价收粮，出价颇为诱人。"
        GameData.AddLog("粮商上门收粮，价钱不低。")
        return {
            title = "粮商收购",
            desc = "一个自称从府城来的粮商带着几辆大车到了村口，说今年府城粮价飞涨，愿以高于市价三成收购余粮。不少人家已经开始往外搬粮食了。只是秋收还远，卖了余粮万一遇上荒年可就麻烦了。",
            choices = {
                { text = "趁高价卖掉大半余粮（-12粮食，+10银两）", effect = function()
                    GameData.AddResource("grain", -12)
                    GameData.AddResource("silver", 10)
                    GameData.AddLog("趁着好价钱卖了大半粮食，银子赚了不少。")
                end },
                { text = "少卖一些，留足口粮（-5粮食，+4银两）", effect = function()
                    GameData.AddResource("grain", -5)
                    GameData.AddResource("silver", 4)
                    GameData.AddLog("卖了少量余粮，既赚了银子也留了后路。")
                end },
                { text = "一粒不卖，手中有粮心中不慌", effect = function()
                    GameData.AddLog("没有卖粮，留着以备不时之需。")
                end },
            }
        }
    end,
}

-- 9. 私塾先生
events[#events + 1] = {
    id = "r2_village_teacher",
    title = "私塾先生",
    rankRange = {2, 3},
    weight = 6,
    cooldownMonths = 12,
    identityMatch = {"scholar"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "一位落第秀才流落至此，愿在村中设馆教书。"
        GameData.AddLog("有秀才愿在村中开设私塾。")
        return {
            title = "私塾先生",
            desc = "一位姓陈的落第秀才途经本村，盘缠用尽，愿暂留村中设私塾教书度日。此人虽未中举，但学问扎实，谈吐不俗。若请他教族中子弟读书识字，日后或有出息。只是束脩和笔墨纸砚都需一笔开销。",
            choices = {
                { text = "聘请先生教书（-5银两，族人读书受益）", effect = function()
                    GameData.AddResource("silver", -5)
                    GameData.AddResource("fame", 3)
                    local scholars = GameData.GetMembersByState("读书")
                    if #scholars > 0 then
                        GameData.AddLog("请了陈秀才教书，" .. scholars[1].name .. "等人学业大有长进。")
                    else
                        GameData.AddLog("请了陈秀才在村中设馆，族中子弟开始读书识字。")
                    end
                end },
                { text = "只让一个孩子去旁听（-2银两）", effect = function()
                    GameData.AddResource("silver", -2)
                    GameData.AddResource("fame", 1)
                    GameData.AddLog("安排一个孩子去私塾旁听，多少认些字。")
                end },
                { text = "庄户人家，读书无用", effect = function()
                    GameData.AddLog("没有请先生，孩子们继续在田间帮忙。")
                end },
            }
        }
    end,
}

-- 10. 养蚕季节
events[#events + 1] = {
    id = "r2_silk_worm",
    title = "养蚕季节",
    rankRange = {2, 3},
    weight = 7,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "春蚕时节到了，桑叶正盛，可以养蚕缫丝。"
        GameData.AddLog("养蚕时节，考虑是否投入蚕桑。")
        return {
            title = "养蚕季节",
            desc = "开春以来雨水丰沛，村后山坡上的桑树长势喜人。隔壁赵家娘子养了几筐蚕，听说已经结茧缫丝了，那蚕丝白如雪、细如发，拿到镇上能卖好价钱。只是养蚕费人手，又需添置蚕具。",
            choices = {
                { text = "大力投入养蚕（-3银两，产出布匹）", effect = function()
                    GameData.AddResource("silver", -3)
                    GameData.AddResource("cloth", 8)
                    GameData.AddLog("养蚕缫丝，产出不少好布匹。")
                end },
                { text = "小规模尝试（-1银两）", effect = function()
                    GameData.AddResource("silver", -1)
                    GameData.AddResource("cloth", 3)
                    GameData.AddLog("小养了一些蚕，得了几匹粗布。")
                end },
                { text = "专心种田，不分心思", effect = function()
                    GameData.AddLog("没有养蚕，专心侍弄田地。")
                end },
            }
        }
    end,
}

-- 11. 路遇劫匪
events[#events + 1] = {
    id = "r2_road_bandit",
    title = "路遇劫匪",
    rankRange = {2, 3},
    weight = 6,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族人赶集途中遭遇劫匪拦路。"
        GameData.AddLog("赶集路上遇到了劫道的。")
        return {
            title = "路遇劫匪",
            desc = "族人去镇上卖粮，行至半路的山坳处，突然窜出几个蒙面大汉拦住去路，手持刀棍喝道：'留下钱粮，饶你性命！'车上还载着辛苦攒下的粮食和银两。",
            choices = {
                { text = "破财消灾，交出财物（-3银两，-5粮食）", effect = function()
                    GameData.AddResource("silver", -3)
                    GameData.AddResource("grain", -5)
                    GameData.AddLog("族人被劫，交出了银粮才保住性命。")
                end },
                { text = "奋力反抗（看族中武力）", effect = function()
                    local warriors = GameData.GetMembersByState("习武")
                    if #warriors > 0 then
                        GameData.AddResource("fame", 2)
                        GameData.AddLog(warriors[1].name .. "奋起反击，赶跑了劫匪，分毫未失。")
                    else
                        GameData.AddResource("silver", -2)
                        GameData.AddResource("grain", -3)
                        GameData.AddLog("族人拼命反抗，虽赶走了匪人，但也折损了些财物。")
                    end
                end },
                { text = "大声呼救，等待过路人帮忙", effect = function()
                    if math.random() > 0.5 then
                        GameData.AddResource("silver", -1)
                        GameData.AddLog("呼救声引来了巡路的衙役，匪人逃窜，只丢了少许银两。")
                    else
                        GameData.AddResource("silver", -3)
                        GameData.AddResource("grain", -4)
                        GameData.AddLog("无人应答，最终还是被劫走了大半财物。")
                    end
                end },
            }
        }
    end,
}

-- 12. 修缮庙宇
events[#events + 1] = {
    id = "r2_temple_donation",
    title = "修缮庙宇",
    rankRange = {2, 3},
    weight = 7,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "村中土地庙年久失修，乡绅号召各家捐资修缮。"
        GameData.AddLog("土地庙需要修缮，乡绅募捐。")
        return {
            title = "修缮庙宇",
            desc = "村头的土地庙经年风吹雨打，墙壁倾颓，屋瓦破碎。里正和几位乡绅发起募捐，要修缮庙宇，重塑金身。捐得多的人家名字刻在功德碑上，也算是积德行善、光宗耀祖的事。",
            choices = {
                { text = "慷慨解囊，捐银修庙（-5银两，+5声望）", effect = function()
                    GameData.AddResource("silver", -5)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("慷慨捐银修缮庙宇，名字刻上了功德碑，乡邻敬重。")
                end },
                { text = "量力而行，少捐一些（-2银两，+2声望）", effect = function()
                    GameData.AddResource("silver", -2)
                    GameData.AddResource("fame", 2)
                    GameData.AddLog("捐了些银两修庙，尽了心意。")
                end },
                { text = "手头紧，出力不出钱", effect = function()
                    GameData.AddResource("fame", 1)
                    GameData.AddLog("没钱捐银，派了族人去帮忙搬砖抬瓦。")
                end },
            }
        }
    end,
}

-- 13. 拜师学艺
events[#events + 1] = {
    id = "r2_apprentice",
    title = "拜师学艺",
    rankRange = {2, 3},
    weight = 6,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        for _, m in ipairs(adults) do
            if m.alive and m.age >= 14 and m.age <= 25 then
                return true
            end
        end
        return false
    end,
    execute = function(s, report)
        local candidate = nil
        local adults = GameData.GetAdultMembers()
        for _, m in ipairs(adults) do
            if m.alive and m.age >= 14 and m.age <= 25 then
                candidate = m
                break
            end
        end
        local cName = candidate and candidate.name or "族中后辈"
        report.events[#report.events + 1] = "镇上的木匠师傅愿收" .. cName .. "为徒。"
        GameData.AddLog("有机会送" .. cName .. "去学手艺。")
        return {
            title = "拜师学艺",
            desc = "镇上赫赫有名的王木匠年事已高，想收个关门弟子传承手艺。有人推荐了咱家的" .. cName .. "，说这孩子手脚勤快，脑子也灵光。拜师需备束脩礼金，学成出师后便多了一门营生。",
            choices = {
                { text = "备厚礼送去学艺（-4银两，-2布匹）", effect = function()
                    GameData.AddResource("silver", -4)
                    GameData.AddResource("cloth", -2)
                    GameData.AddResource("fame", 2)
                    GameData.AddLog(cName .. "拜了王木匠为师，开始学手艺。")
                end },
                { text = "简单备礼，试试看（-2银两）", effect = function()
                    GameData.AddResource("silver", -2)
                    if math.random() > 0.3 then
                        GameData.AddLog(cName .. "顺利入了师门，开始学木匠手艺。")
                    else
                        GameData.AddLog("礼薄了些，王木匠没有收下" .. cName .. "。")
                    end
                end },
                { text = "家里人手不够，走不开", effect = function()
                    GameData.AddLog("家中农活离不了人，" .. cName .. "只好留在田间。")
                end },
            }
        }
    end,
}

-- 14. 丰收祭
events[#events + 1] = {
    id = "r2_harvest_festival",
    title = "丰收祭",
    rankRange = {2, 3},
    weight = 8,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "秋收丰稳，村中筹办丰收祭典庆贺。"
        GameData.AddLog("丰年祭典，合村同庆。")
        return {
            title = "丰收祭",
            desc = "今年风调雨顺，五谷丰登，村中各家粮仓满溢。里正提议合村操办一场丰收祭，祭天酬神、摆酒欢庆。各家需出些粮食酒水，热闹热闹也能增进乡邻情谊。",
            choices = {
                { text = "大办祭典，出粮出力（-5粮食，+4声望）", effect = function()
                    GameData.AddResource("grain", -5)
                    GameData.AddResource("fame", 4)
                    GameData.AddLog("出粮办了场风光的丰收祭，合村欢庆，声名远扬。")
                end },
                { text = "随份子参加（-2粮食，+1声望）", effect = function()
                    GameData.AddResource("grain", -2)
                    GameData.AddResource("fame", 1)
                    GameData.AddLog("随了份子参加丰收祭，热闹了一番。")
                end },
                { text = "关起门来自家庆贺", effect = function()
                    GameData.AddResource("fame", -1)
                    GameData.AddLog("没参加村里的庆典，被人说小气。")
                end },
            }
        }
    end,
}

-- 15. 走水隐患
events[#events + 1] = {
    id = "r2_fire_risk",
    title = "走水隐患",
    rankRange = {2, 3},
    weight = 5,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "深夜灶房起火，险些蔓延至粮仓。"
        GameData.AddLog("走水了！灶房失火，紧急扑救。")
        return {
            title = "走水隐患",
            desc = "深夜三更，灶房里突然蹿起火苗，火借风势迅速蔓延，幸而族人发现得早，齐力扑救。虽然没有伤人，但灶房烧毁了大半，存放在旁边的一些粮食和布匹也被烧了不少。",
            choices = {
                { text = "花钱修缮，加固防火（-4银两，防患未然）", effect = function()
                    GameData.AddResource("silver", -4)
                    GameData.AddResource("grain", -5)
                    GameData.AddResource("cloth", -2)
                    GameData.AddLog("修了灶房，加砌了防火墙，粮食布匹损失了一些。")
                end },
                { text = "简单修补，将就着用（-2银两）", effect = function()
                    GameData.AddResource("silver", -2)
                    GameData.AddResource("grain", -8)
                    GameData.AddResource("cloth", -3)
                    GameData.AddLog("简单修了修灶房，但损失的粮食布匹不少。")
                end },
                { text = "先不管灶房，抢救粮仓", effect = function()
                    GameData.AddResource("grain", -3)
                    GameData.AddResource("cloth", -5)
                    GameData.AddLog("保住了大部分粮食，但灶房和布匹损失惨重。")
                end },
            }
        }
    end,
}

-- 16. 行商过境
events[#events + 1] = {
    id = "r2_traveling_merchant",
    title = "行商过境",
    rankRange = {2, 3},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "一队行商驮着异地货物路过村庄。"
        GameData.AddLog("行商队伍过境，带来了外地物产。")
        return {
            title = "行商过境",
            desc = "一队从江南来的行商驮着满满几车货物路过村子，在村口歇脚。车上有丝绸、茶叶、瓷器等物件，还有些稀罕的南方果干蜜饯。他们也想收些本地的粮食和山货带回去卖。",
            choices = {
                { text = "用粮食换丝绸布匹（-8粮食，+6布匹）", effect = function()
                    GameData.AddResource("grain", -8)
                    GameData.AddResource("cloth", 6)
                    GameData.AddLog("拿粮食换了上好的江南丝绸，赚了。")
                end },
                { text = "花银子买些稀罕物件（-3银两，+2布匹）", effect = function()
                    GameData.AddResource("silver", -3)
                    GameData.AddResource("cloth", 2)
                    GameData.AddLog("买了些外地货物，添置了些家用。")
                end },
                { text = "只是看看，不买不换", effect = function()
                    GameData.AddLog("看了看行商的货物，最终没有交易。")
                end },
            }
        }
    end,
}

-- 17. 长辈病重
events[#events + 1] = {
    id = "r2_sick_elder",
    title = "长辈病重",
    rankRange = {2, 3},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local members = GameData.GetAliveMembers()
        for _, m in ipairs(members) do
            if m.alive and m.age >= 50 then
                return true
            end
        end
        return false
    end,
    execute = function(s, report)
        local elder = nil
        local members = GameData.GetAliveMembers()
        for _, m in ipairs(members) do
            if m.alive and m.age >= 50 then
                elder = m
                break
            end
        end
        local eName = elder and elder.name or "族中长辈"
        report.events[#report.events + 1] = eName .. "突然病重，卧床不起。"
        GameData.AddLog(eName .. "病重，急需救治。")
        return {
            title = "长辈病重",
            desc = eName .. "近日咳嗽不止，夜间高热不退，请了村中的赤脚大夫来看，直摇头说病势沉重，需去镇上请名医。镇上的大夫医术高明，但诊金药费加起来可不是小数目。",
            choices = {
                { text = "不惜代价请名医诊治（-5银两）", effect = function()
                    GameData.AddResource("silver", -5)
                    GameData.AddResource("fame", 2)
                    GameData.AddLog("花重金请来名医，" .. eName .. "病情好转，族人称赞孝心。")
                end },
                { text = "先用土方子调理（-1银两）", effect = function()
                    GameData.AddResource("silver", -1)
                    if math.random() > 0.5 then
                        GameData.AddLog("用草药土方调理，" .. eName .. "的病渐渐好了。")
                    else
                        GameData.AddLog("土方子不管用，" .. eName .. "的病越来越重了。")
                    end
                end },
                { text = "求神拜佛，烧香祈福", effect = function()
                    GameData.AddResource("silver", -1)
                    GameData.AddLog("在庙里烧了香祈了福，" .. eName .. "的病只能听天命了。")
                end },
            }
        }
    end,
}

-- 18. 吉兆出现
events[#events + 1] = {
    id = "r2_good_omen",
    title = "吉兆出现",
    rankRange = {2, 3},
    weight = 7,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "屋后老槐树上栖了一对喜鹊，乡邻都说是大吉之兆。"
        GameData.AddLog("喜鹊临门，吉兆显现。")
        return {
            title = "吉兆出现",
            desc = "清晨起来，发现屋后那棵百年老槐树上落了一对喜鹊，叽叽喳喳叫个不停。邻居们纷纷来看，都说喜鹊登枝是大吉之兆，主家中将有喜事临门。一时间族人士气大振。",
            choices = {
                { text = "设香案酬谢天恩（-1银两，+3声望）", effect = function()
                    GameData.AddResource("silver", -1)
                    GameData.AddResource("fame", 3)
                    GameData.AddResource("grain", 3)
                    GameData.AddLog("设香案祭拜，族人士气高涨，劳作更加卖力。")
                end },
                { text = "趁吉兆做些大事（鼓励族人开荒）", effect = function()
                    GameData.AddResource("grain", 6)
                    GameData.AddResource("fame", 1)
                    GameData.AddLog("趁吉兆鼓舞人心，组织族人开垦荒地，多收了粮食。")
                end },
                { text = "心中欢喜，但不做声张", effect = function()
                    GameData.AddResource("fame", 1)
                    GameData.AddLog("暗自高兴，觉得来年定有好运。")
                end },
            }
        }
    end,
}

-- 19. 边关消息
events[#events + 1] = {
    id = "r2_border_news",
    title = "边关消息",
    rankRange = {2, 3},
    weight = 6,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "北方传来边关战事的消息，人心惶惶。"
        GameData.AddLog("边关告急，战事消息传来。")
        return {
            title = "边关消息",
            desc = "从北边逃来的难民带来消息：鞑靼骑兵又犯边关，朝廷正在各地征兵征粮。虽说这里离边关尚远，但征粮的差役说不定哪天就到。村里人心浮动，有人开始囤粮藏银。",
            choices = {
                { text = "主动缴纳军粮以求安稳（-8粮食，+3声望）", effect = function()
                    GameData.AddResource("grain", -8)
                    GameData.AddResource("fame", 3)
                    GameData.AddLog("主动上缴军粮，被里正记了功，免去了后续征调。")
                end },
                { text = "悄悄把粮食藏起来", effect = function()
                    if math.random() > 0.4 then
                        GameData.AddLog("偷偷藏好了粮食，差役来时搪塞了过去。")
                    else
                        GameData.AddResource("grain", -10)
                        GameData.AddResource("fame", -3)
                        GameData.AddLog("藏粮被差役发现，不但粮食被征还挨了罚。")
                    end
                end },
                { text = "送族中青壮去从军（+5声望）", effect = function()
                    GameData.AddResource("fame", 5)
                    GameData.AddResource("grain", -3)
                    GameData.AddLog("送了族中青壮去从军报国，虽少了人手，但博了好名声。")
                end },
            }
        }
    end,
}

-- 20. 养猪收益
events[#events + 1] = {
    id = "r2_pig_breeding",
    title = "养猪收益",
    rankRange = {2, 3},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "邻家养的猪崽子下了一窝，问咱家要不要买几头。"
        GameData.AddLog("有机会买猪崽子养。")
        return {
            title = "养猪收益",
            desc = "邻家老母猪下了一窝崽子，个个圆滚滚的甚是壮实。邻家养不了这许多，问咱家要不要买几头回去养。猪养大了年底杀了腌肉，或是拿到集市上卖，都是一笔不小的进项。就是养猪费粮食。",
            choices = {
                { text = "买三头猪崽好好养（-3银两，-5粮食）", effect = function()
                    GameData.AddResource("silver", -3)
                    GameData.AddResource("grain", -5)
                    GameData.AddResource("silver", 8)
                    GameData.AddLog("买了三头猪崽精心饲养，年底卖了个好价钱。")
                end },
                { text = "买一头试试（-1银两，-2粮食）", effect = function()
                    GameData.AddResource("silver", -1)
                    GameData.AddResource("grain", -2)
                    GameData.AddResource("silver", 3)
                    GameData.AddLog("养了一头猪，年底杀了过年，还剩些腊肉腌肉。")
                end },
                { text = "养猪太费粮食，算了", effect = function()
                    GameData.AddLog("没有买猪崽，省了粮食。")
                end },
            }
        }
    end,
}

-- 21. 修缮房屋
events[#events + 1] = {
    id = "r2_house_repair",
    title = "修缮房屋",
    rankRange = {2, 3},
    weight = 7,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "连日大雨，屋顶漏水，墙壁也裂了缝。"
        GameData.AddLog("房屋破旧需要修缮。")
        return {
            title = "修缮房屋",
            desc = "连下了几日大雨，屋里到处漏水，锅碗瓢盆都用来接水了。东墙也裂了一道长缝，再不修怕是要塌。请泥瓦匠来看过，说小修要二两银子，若要翻新加固则需更多。",
            choices = {
                { text = "翻新加固，一劳永逸（-5银两）", effect = function()
                    GameData.AddResource("silver", -5)
                    GameData.AddResource("fame", 1)
                    GameData.AddLog("花银子把房屋翻新加固了，再大的雨也不怕。")
                end },
                { text = "小修小补，先对付着（-2银两）", effect = function()
                    GameData.AddResource("silver", -2)
                    GameData.AddLog("请人简单修了修屋顶和墙壁，暂时不漏了。")
                end },
                { text = "自己动手糊些泥巴", effect = function()
                    GameData.AddResource("grain", -2)
                    GameData.AddLog("自己用黄泥糊了漏处，凑合着住，但下次大雨还是会漏。")
                end },
            }
        }
    end,
}

-- 22. 地痞欺压
events[#events + 1] = {
    id = "r2_local_bully",
    title = "地痞欺压",
    rankRange = {2, 3},
    weight = 6,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "村中地痞无赖上门滋事，强索'保护费'。"
        GameData.AddLog("地痞无赖上门欺压索财。")
        return {
            title = "地痞欺压",
            desc = "村里的赖三带着几个混混上门来了，嘻皮笑脸地说要收什么'平安钱'，不给就要砸东西。这泼皮在村里横行已久，官府也懒得管，不少人家都吃了他的亏。",
            choices = {
                { text = "花钱打发走（-3银两）", effect = function()
                    GameData.AddResource("silver", -3)
                    GameData.AddLog("忍气吞声交了银子，打发走了地痞。")
                end },
                { text = "联合邻里一起对抗", effect = function()
                    local warriors = GameData.GetMembersByState("习武")
                    if #warriors > 0 then
                        GameData.AddResource("fame", 4)
                        GameData.AddLog("族中有习武之人，联合邻里把地痞赶走了，威望大增。")
                    else
                        GameData.AddResource("fame", 2)
                        GameData.AddResource("silver", -1)
                        GameData.AddLog("邻里合力赶走了泼皮，虽有些损失，但出了口气。")
                    end
                end },
                { text = "去县衙告状（-2银两，走衙门程序）", effect = function()
                    GameData.AddResource("silver", -2)
                    if math.random() > 0.5 then
                        GameData.AddResource("fame", 3)
                        GameData.AddLog("县太爷接了状子，派人拿了赖三，村中太平了。")
                    else
                        GameData.AddResource("fame", -1)
                        GameData.AddLog("衙门敷衍了事，赖三被放了出来，还怀恨在心。")
                    end
                end },
            }
        }
    end,
}

-- 23. 梅雨连绵
events[#events + 1] = {
    id = "r2_rainy_season",
    title = "梅雨连绵",
    rankRange = {2, 3},
    weight = 5,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "梅雨季节连绵不绝，粮仓受潮，庄稼泡烂。"
        GameData.AddLog("梅雨成灾，粮食大量受潮霉变。")
        return {
            title = "梅雨连绵",
            desc = "今年梅雨格外漫长，连下了一个多月的雨还没有停歇的意思。田里的庄稼泡在水里烂了根，粮仓里存放的粮食也受潮发霉。到处湿漉漉的，柴火都烧不着，全家人苦不堪言。",
            choices = {
                { text = "花钱紧急翻晒抢救粮食（-3银两，减少损失）", effect = function()
                    GameData.AddResource("silver", -3)
                    GameData.AddResource("grain", -6)
                    GameData.AddLog("雇了人手抢救粮仓，翻晒受潮粮食，保住了大半。")
                end },
                { text = "挖排水沟保田（-2银两）", effect = function()
                    GameData.AddResource("silver", -2)
                    GameData.AddResource("grain", -10)
                    GameData.AddLog("拼命挖沟排水，田里的庄稼保住了一些，但粮仓损失不小。")
                end },
                { text = "只能熬着等雨停", effect = function()
                    GameData.AddResource("grain", -12)
                    GameData.AddResource("cloth", -3)
                    GameData.AddLog("无力应对连绵梅雨，粮食霉变严重，布匹也发了霉。")
                end },
            }
        }
    end,
}

-- 24. 收留孤儿
events[#events + 1] = {
    id = "r2_orphan_found",
    title = "收留孤儿",
    rankRange = {2, 3},
    weight = 5,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "田间发现一名衣衫褴褛的孤儿，饿得奄奄一息。"
        GameData.AddLog("在田边发现了一个流浪孤儿。")
        return {
            title = "收留孤儿",
            desc = "族人在田间干活时发现一个七八岁的孩子蜷缩在田埂边，衣衫褴褛、面黄肌瘦，饿得说不出话来。问了半天才知道是逃荒来的，父母都饿死在了路上。这孩子可怜得紧，收留下来多张嘴吃饭，送走又于心不忍。",
            choices = {
                { text = "收留下来，当自家孩子养（-3粮食/季，+3声望）", effect = function()
                    GameData.AddResource("grain", -3)
                    GameData.AddResource("fame", 3)
                    GameData.AddLog("收留了孤儿，多了一口人吃饭，但也积了德行。")
                end },
                { text = "先收留，再送给镇上无子人家（+1声望）", effect = function()
                    GameData.AddResource("grain", -1)
                    GameData.AddResource("fame", 1)
                    GameData.AddLog("暂时收留了孤儿，后来送给了镇上一户好人家。")
                end },
                { text = "给些干粮让他继续赶路", effect = function()
                    GameData.AddResource("grain", -1)
                    GameData.AddLog("给了孤儿一些干粮，目送他离去，心中不忍。")
                end },
            }
        }
    end,
}

-- 25. 互助社
events[#events + 1] = {
    id = "r2_mutual_aid",
    title = "互助社",
    rankRange = {2, 3},
    weight = 7,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "村中几户人家提议成立互助社，合力应对灾荒。"
        GameData.AddLog("有人提议成立乡村互助社。")
        return {
            title = "互助社",
            desc = "村里几户殷实人家提议大家凑份子成立一个互助社，平时各家出些粮食银两存着，谁家遇了急事便从社中支取，来年再还上。这法子听着不错，但怕遇上赖账的或管账的不清白。",
            choices = {
                { text = "积极参与，多出份子（-3银两，-3粮食，+4声望）", effect = function()
                    GameData.AddResource("silver", -3)
                    GameData.AddResource("grain", -3)
                    GameData.AddResource("fame", 4)
                    GameData.AddLog("带头参与互助社，出了大份子，被推举为管事。")
                end },
                { text = "参与，但只出基本份子（-1银两，-2粮食，+2声望）", effect = function()
                    GameData.AddResource("silver", -1)
                    GameData.AddResource("grain", -2)
                    GameData.AddResource("fame", 2)
                    GameData.AddLog("加入了互助社，出了基本份子，有了互相照应的保障。")
                end },
                { text = "不参与，自家管自家", effect = function()
                    GameData.AddResource("fame", -2)
                    GameData.AddLog("没有加入互助社，被人说不合群。")
                end },
            }
        }
    end,
}

return events
