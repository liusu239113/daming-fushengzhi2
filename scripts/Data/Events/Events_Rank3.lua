local GameData = require("Data.GameData")
local events = {}

-- 1. 县令宴请
events[#events + 1] = {
    id = "r3_county_invite",
    title = "县令宴请",
    rankRange = {3, 4},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "县令设宴，遍邀乡中殷实人家，族中亦在受邀之列。"
        GameData.AddLog("县令设宴款待乡绅")
        return {
            title = "县令宴请",
            desc = "县衙传来帖子，知县大人于明日设宴，邀本地乡绅士绅共聚。席间或论时政、或谈风月，实则暗藏结交之意。赴宴须备厚礼，不赴则恐失了颜面。",
            choices = {
                {
                    text = "备厚礼赴宴，结交官府（-8银两，+5声望）",
                    effect = function()
                        GameData.AddResource("silver", -8)
                        GameData.AddResource("fame", 5)
                    end,
                },
                {
                    text = "薄礼应酬，不过分攀附（-3银两，+2声望）",
                    effect = function()
                        GameData.AddResource("silver", -3)
                        GameData.AddResource("fame", 2)
                    end,
                },
                {
                    text = "托病不去，省却银两",
                    effect = function()
                        GameData.AddResource("fame", -2)
                    end,
                },
            },
        }
    end,
}

-- 2. 田产纠纷
events[#events + 1] = {
    id = "r3_land_dispute",
    title = "田产纠纷",
    rankRange = {3, 4},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "邻乡大户侵占田界，与族中起了争执。"
        GameData.AddLog("与邻家发生田产纠纷")
        return {
            title = "田产纠纷",
            desc = "族中东边三十亩良田，与邻乡赵家的地界素有争议。近日赵家趁雨后田垄模糊，擅自移动了界石。管事来报，若不及早处置，恐被蚕食更多。",
            choices = {
                {
                    text = "花银子请讼师打官司（-10银两，+8粮食）",
                    effect = function()
                        GameData.AddResource("silver", -10)
                        GameData.AddResource("grain", 8)
                    end,
                },
                {
                    text = "私下调解，各让一步（-3粮食，+2声望）",
                    effect = function()
                        GameData.AddResource("grain", -3)
                        GameData.AddResource("fame", 2)
                    end,
                },
                {
                    text = "暂且隐忍，日后再计较",
                    effect = function()
                        GameData.AddResource("grain", -5)
                    end,
                },
            },
        }
    end,
}

-- 3. 资助书院
events[#events + 1] = {
    id = "r3_academy_fund",
    title = "资助书院",
    rankRange = {3, 4},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = {"scholar"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local scholars = GameData.GetMembersByState("读书")
        return #scholars > 0
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "县中书院年久失修，山长登门求助。"
        GameData.AddLog("书院请求资助修缮")
        return {
            title = "资助书院",
            desc = "县中明德书院的山长亲自登门拜访，言及书院屋漏墙颓，藏书霉烂，恳请乡中殷实人家慷慨捐资。书院乃本县文脉所系，历年来出过不少举人秀才。",
            choices = {
                {
                    text = "慷慨捐资，刻名立碑（-15银两，+10声望）",
                    effect = function()
                        GameData.AddResource("silver", -15)
                        GameData.AddResource("fame", 10)
                    end,
                },
                {
                    text = "略尽心意，捐些银两（-6银两，+4声望）",
                    effect = function()
                        GameData.AddResource("silver", -6)
                        GameData.AddResource("fame", 4)
                    end,
                },
                {
                    text = "婉言推辞，自家也不宽裕",
                    effect = function()
                        GameData.AddResource("fame", -3)
                    end,
                },
            },
        }
    end,
}

-- 4. 大宗交易
events[#events + 1] = {
    id = "r3_merchant_deal",
    title = "大宗交易",
    rankRange = {3, 4},
    weight = 7,
    cooldownMonths = 6,
    identityMatch = {"merchant"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local merchants = GameData.GetMembersByState("经商")
        return #merchants > 0
    end,
    execute = function(s, report)
        local merchants = GameData.GetMembersByState("经商")
        local m = merchants[1]
        report.events[#report.events + 1] = m.name .. "谈成一桩大宗买卖。"
        GameData.AddLog(m.name .. "接洽大宗商贸")
        return {
            title = "大宗交易",
            desc = "外地客商途经此地，欲大量采购本地土产。族中" .. m.name .. "与之洽谈，对方出价颇丰，但要求先垫付货款，待交货后再行结算。利润可观，风险亦存。",
            choices = {
                {
                    text = "倾力而为，押上全部货物（-12银两，+20粮食）",
                    effect = function()
                        GameData.AddResource("silver", -12)
                        GameData.AddResource("grain", 20)
                    end,
                },
                {
                    text = "谨慎行事，只做一半（-5银两，+10粮食）",
                    effect = function()
                        GameData.AddResource("silver", -5)
                        GameData.AddResource("grain", 10)
                    end,
                },
                {
                    text = "婉拒此单，不冒此险",
                    effect = function() end,
                },
            },
        }
    end,
}

-- 5. 匪患骚扰
events[#events + 1] = {
    id = "r3_bandits_raid",
    title = "匪患骚扰",
    rankRange = {3, 4},
    weight = 6,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "山贼下山劫掠，四邻不安。"
        GameData.AddLog("匪徒侵扰乡里")
        return {
            title = "匪患骚扰",
            desc = "近日山中匪寇频繁出没，已有数家被劫。昨夜更有人看到贼人在族田附近窥探。乡中人心惶惶，须早做打算。",
            choices = {
                {
                    text = "出资雇壮丁巡夜护院（-10银两，-5粮食）",
                    effect = function()
                        GameData.AddResource("silver", -10)
                        GameData.AddResource("grain", -5)
                    end,
                },
                {
                    text = "联合邻家向县衙报案（-5银两，+3声望）",
                    effect = function()
                        GameData.AddResource("silver", -5)
                        GameData.AddResource("fame", 3)
                    end,
                },
                {
                    text = "紧闭门户，听天由命（-8粮食，-3银两）",
                    effect = function()
                        GameData.AddResource("grain", -8)
                        GameData.AddResource("silver", -3)
                    end,
                },
            },
        }
    end,
}

-- 6. 官员巡察
events[#events + 1] = {
    id = "r3_official_visit",
    title = "官员巡察",
    rankRange = {3, 4},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "上官巡视地方，须妥善打点迎迓。"
        GameData.AddLog("府中官员下乡巡察")
        return {
            title = "官员巡察",
            desc = "府中通判奉命巡察各县民情，不日将至。县令传话，令各乡绅大户妥加准备，不可有失体面。此番打点得当，或可借机为族中谋些便利。",
            choices = {
                {
                    text = "精心准备，大加打点（-12银两，-5粮食，+8声望）",
                    effect = function()
                        GameData.AddResource("silver", -12)
                        GameData.AddResource("grain", -5)
                        GameData.AddResource("fame", 8)
                    end,
                },
                {
                    text = "按规矩备些薄礼（-5银两，-3粮食，+3声望）",
                    effect = function()
                        GameData.AddResource("silver", -5)
                        GameData.AddResource("grain", -3)
                        GameData.AddResource("fame", 3)
                    end,
                },
                {
                    text = "只做本分，不刻意逢迎",
                    effect = function()
                        GameData.AddResource("silver", -2)
                    end,
                },
            },
        }
    end,
}

-- 7. 诗文雅集
events[#events + 1] = {
    id = "r3_poetry_contest",
    title = "诗文雅集",
    rankRange = {3, 4},
    weight = 7,
    cooldownMonths = 6,
    identityMatch = {"scholar"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local scholars = GameData.GetMembersByState("读书")
        return #scholars > 0
    end,
    execute = function(s, report)
        local scholars = GameData.GetMembersByState("读书")
        local m = scholars[1]
        report.events[#report.events + 1] = "县中士子雅集，" .. m.name .. "应邀赴会。"
        GameData.AddLog(m.name .. "参加诗文雅集")
        return {
            title = "诗文雅集",
            desc = "时值重阳，县中名士于松风亭设诗文雅集，遍邀有才学之士。族中" .. m.name .. "素来好学，正可借此扬名。然赴会须置办新衣笔墨，亦需备些酒食之资。",
            choices = {
                {
                    text = "隆重赴会，务求出彩（-6银两，+6声望）",
                    effect = function()
                        GameData.AddResource("silver", -6)
                        GameData.AddResource("fame", 6)
                    end,
                },
                {
                    text = "简素赴会，以文采取胜（-2银两，+3声望）",
                    effect = function()
                        GameData.AddResource("silver", -2)
                        GameData.AddResource("fame", 3)
                    end,
                },
                {
                    text = "不去凑热闹，在家读书",
                    effect = function() end,
                },
            },
        }
    end,
}

-- 8. 大水冲田
events[#events + 1] = {
    id = "r3_flood",
    title = "大水冲田",
    rankRange = {3, 4},
    weight = 5,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "连日暴雨，河水漫堤，良田尽没。"
        GameData.AddLog("洪水冲毁大片田地")
        return {
            title = "大水冲田",
            desc = "入夏以来阴雨连绵，河水暴涨，终于溃堤而出。族中二十余亩低洼良田俱被淹没，秧苗尽毁。佃户哭诉无依，急需赈济安抚，更需银两修复堤坝、补种晚稻。",
            choices = {
                {
                    text = "倾力赈灾，修堤补种（-12银两，-15粮食，+8声望）",
                    effect = function()
                        GameData.AddResource("silver", -12)
                        GameData.AddResource("grain", -15)
                        GameData.AddResource("fame", 8)
                    end,
                },
                {
                    text = "量力而行，先安抚佃户（-5银两，-8粮食，+3声望）",
                    effect = function()
                        GameData.AddResource("silver", -5)
                        GameData.AddResource("grain", -8)
                        GameData.AddResource("fame", 3)
                    end,
                },
                {
                    text = "只管自家，佃户自谋出路（-5粮食，-4声望）",
                    effect = function()
                        GameData.AddResource("grain", -5)
                        GameData.AddResource("fame", -4)
                    end,
                },
            },
        }
    end,
}

-- 9. 联姻提议
events[#events + 1] = {
    id = "r3_marriage_alliance",
    title = "联姻提议",
    rankRange = {3, 4},
    weight = 6,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        for _, m in ipairs(adults) do
            if not m.spouseId and m.alive then
                return true
            end
        end
        return false
    end,
    execute = function(s, report)
        -- 找到适龄未婚族人
        local candidate = nil
        local adults = GameData.GetAdultMembers()
        for _, m in ipairs(adults) do
            if not m.spouseId and m.alive then candidate = m; break end
        end
        local cName = candidate and candidate.name or "族人"
        report.events[#report.events + 1] = "邻县大户遣媒人前来为" .. cName .. "提亲。"
        GameData.AddLog("收到大户联姻提议，为" .. cName .. "说亲")
        return {
            title = "联姻提议",
            desc = "邻县周家乃三代簪缨之族，遣媒人为" .. cName .. "议亲。周家家资殷厚，若能结亲，于族中声势大有裨益。是否前去相看？",
            choices = {
                {
                    text = "前去相看",
                    effect = function()
                        if candidate and not candidate.spouseId then
                            local GS = require("UI.GameScreen")
                            GS.ShowMarriageTierSelect(candidate)
                        end
                    end,
                },
                {
                    text = "婉言谢绝，另觅良缘",
                    effect = function() end,
                },
            },
        }
    end,
}

-- 10. 施粥赈灾
events[#events + 1] = {
    id = "r3_charity",
    title = "施粥赈灾",
    rankRange = {3, 4},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "灾年流民涌入，乡中议论施粥赈济。"
        GameData.AddLog("灾年议论施粥赈济")
        return {
            title = "施粥赈灾",
            desc = "邻县遭灾，流民纷纷涌来。饥寒交迫者日渐增多，县衙号召乡绅大户设棚施粥。此乃积德之举，亦可博得善名；然粮食消耗甚巨，须量力而为。",
            choices = {
                {
                    text = "大设粥棚，连施半月（-20粮食，-3银两，+15声望）",
                    effect = function()
                        GameData.AddResource("grain", -20)
                        GameData.AddResource("silver", -3)
                        GameData.AddResource("fame", 15)
                    end,
                },
                {
                    text = "小规模施粥，尽绵薄之力（-10粮食，+7声望）",
                    effect = function()
                        GameData.AddResource("grain", -10)
                        GameData.AddResource("fame", 7)
                    end,
                },
                {
                    text = "自保为上，不参与施粥（-5声望）",
                    effect = function()
                        GameData.AddResource("fame", -5)
                    end,
                },
            },
        }
    end,
}

-- 11. 修路铺桥
events[#events + 1] = {
    id = "r3_road_construction",
    title = "修路铺桥",
    rankRange = {3, 4},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "乡中倡议修路铺桥，便利行旅。"
        GameData.AddLog("乡中倡议修路铺桥")
        return {
            title = "修路铺桥",
            desc = "村东官道年久失修，坑洼难行；溪上木桥亦朽烂欲断。乡老倡议集资修缮，造福一方。此举功德甚大，修成后可镌碑记功。然费银不少，须各家分摊。",
            choices = {
                {
                    text = "慷慨出资，独揽大头（-12银两，+10声望）",
                    effect = function()
                        GameData.AddResource("silver", -12)
                        GameData.AddResource("fame", 10)
                    end,
                },
                {
                    text = "按份分摊，出应有之资（-5银两，+4声望）",
                    effect = function()
                        GameData.AddResource("silver", -5)
                        GameData.AddResource("fame", 4)
                    end,
                },
                {
                    text = "推说手头紧，不出银子（-3声望）",
                    effect = function()
                        GameData.AddResource("fame", -3)
                    end,
                },
            },
        }
    end,
}

-- 12. 加征赋税
events[#events + 1] = {
    id = "r3_tax_increase",
    title = "加征赋税",
    rankRange = {3, 4},
    weight = 6,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "朝廷加征赋税，地方苦不堪言。"
        GameData.AddLog("朝廷加征赋税")
        return {
            title = "加征赋税",
            desc = "布政司下文，因北疆军饷告急，各县须按丁亩加征三成赋税。县令不敢抗命，只得层层摊派。族中田亩颇多，须缴之数远超往年。众佃户闻讯惶恐，恐有逃亡之虞。",
            choices = {
                {
                    text = "如数缴纳，安分守己（-15银两，-10粮食）",
                    effect = function()
                        GameData.AddResource("silver", -15)
                        GameData.AddResource("grain", -10)
                    end,
                },
                {
                    text = "打点胥吏，设法减免（-8银两，-5粮食）",
                    effect = function()
                        GameData.AddResource("silver", -8)
                        GameData.AddResource("grain", -5)
                    end,
                },
                {
                    text = "拖延不缴，等风头过去（-5声望，-8粮食）",
                    effect = function()
                        GameData.AddResource("fame", -5)
                        GameData.AddResource("grain", -8)
                    end,
                },
            },
        }
    end,
}

-- 13. 佃户诉苦
events[#events + 1] = {
    id = "r3_tenant_complaint",
    title = "佃户诉苦",
    rankRange = {3, 4},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "佃户联名诉苦，请求减租宽限。"
        GameData.AddLog("佃户请求减租")
        return {
            title = "佃户诉苦",
            desc = "族中佃户十余家联名跪求减租，言称今岁收成不好，按旧例缴租则全家断粮。领头的老佃户王三忠恳切切，泪流满面。若不减租，恐佃户逃散；若减租太多，族中收入大减。",
            choices = {
                {
                    text = "体恤民苦，减租三成（-10粮食，+6声望）",
                    effect = function()
                        GameData.AddResource("grain", -10)
                        GameData.AddResource("fame", 6)
                    end,
                },
                {
                    text = "略减一成，余者延期缴纳（-5粮食，+2声望）",
                    effect = function()
                        GameData.AddResource("grain", -5)
                        GameData.AddResource("fame", 2)
                    end,
                },
                {
                    text = "规矩不可废，照旧收租（+5粮食，-5声望）",
                    effect = function()
                        GameData.AddResource("grain", 5)
                        GameData.AddResource("fame", -5)
                    end,
                },
            },
        }
    end,
}

-- 14. 丝绸贸易
events[#events + 1] = {
    id = "r3_silk_trade",
    title = "丝绸贸易",
    rankRange = {3, 4},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = {"merchant"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local merchants = GameData.GetMembersByState("经商")
        return #merchants > 0
    end,
    execute = function(s, report)
        local merchants = GameData.GetMembersByState("经商")
        local m = merchants[1]
        report.events[#report.events + 1] = m.name .. "探得丝绸生意的门路。"
        GameData.AddLog(m.name .. "接洽丝绸贸易")
        return {
            title = "丝绸贸易",
            desc = "苏杭丝商经此北上，因盘缠不济，愿以低价出让一批上等丝绸。族中" .. m.name .. "识货之人，估计转手可获厚利。然购货须一次付清，银两不菲。",
            choices = {
                {
                    text = "全数吃下，转手贩卖（-12银两，+15布匹）",
                    effect = function()
                        GameData.AddResource("silver", -12)
                        GameData.AddResource("cloth", 15)
                    end,
                },
                {
                    text = "买下一半，稳妥为上（-6银两，+8布匹）",
                    effect = function()
                        GameData.AddResource("silver", -6)
                        GameData.AddResource("cloth", 8)
                    end,
                },
                {
                    text = "不做此单，怕有猫腻",
                    effect = function() end,
                },
            },
        }
    end,
}

-- 15. 聘请名师
events[#events + 1] = {
    id = "r3_tutor_hire",
    title = "聘请名师",
    rankRange = {3, 4},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local scholars = GameData.GetMembersByState("读书")
        return #scholars > 0
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "听闻有落第举人愿就馆授徒。"
        GameData.AddLog("有名师可聘")
        return {
            title = "聘请名师",
            desc = "本省乡试落第的陈举人流寓至此，学问渊博，愿就馆教书。族中子弟若得名师指点，日后科考大有裨益。然束脩优厚，须年供银两食宿。",
            choices = {
                {
                    text = "重金聘请，好生款待（-10银两，-5粮食，+8声望）",
                    effect = function()
                        GameData.AddResource("silver", -10)
                        GameData.AddResource("grain", -5)
                        GameData.AddResource("fame", 8)
                    end,
                },
                {
                    text = "与邻家合聘，分摊费用（-5银两，-3粮食，+4声望）",
                    effect = function()
                        GameData.AddResource("silver", -5)
                        GameData.AddResource("grain", -3)
                        GameData.AddResource("fame", 4)
                    end,
                },
                {
                    text = "族中自有先生，不必外聘",
                    effect = function() end,
                },
            },
        }
    end,
}

-- 16. 乡绅暗斗
events[#events + 1] = {
    id = "r3_local_rivalry",
    title = "乡绅暗斗",
    rankRange = {3, 4},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "本地另一大户暗中使绊，意图排挤族中势力。"
        GameData.AddLog("遭同乡大户暗中排挤")
        return {
            title = "乡绅暗斗",
            desc = "本乡刘家近来崛起，仗着有人在府中做吏，暗中拉拢佃户、挤兑族中生意。更有流言传出，说族中行事不端。若坐视不理，恐声势渐弱；若针锋相对，又恐两败俱伤。",
            choices = {
                {
                    text = "设宴化解，以德服人（-8银两，+5声望）",
                    effect = function()
                        GameData.AddResource("silver", -8)
                        GameData.AddResource("fame", 5)
                    end,
                },
                {
                    text = "暗中反击，以牙还牙（-5银两，-3声望）",
                    effect = function()
                        GameData.AddResource("silver", -5)
                        GameData.AddResource("fame", -3)
                    end,
                },
                {
                    text = "不理会，做好自家事（-2声望）",
                    effect = function()
                        GameData.AddResource("fame", -2)
                    end,
                },
            },
        }
    end,
}

-- 17. 主持庆典
events[#events + 1] = {
    id = "r3_festival_host",
    title = "主持庆典",
    rankRange = {3, 4},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "乡中推举族中主持今年社日庆典。"
        GameData.AddLog("受推举主持社日庆典")
        return {
            title = "主持庆典",
            desc = "春社将至，乡中推举族中出面主持今年的社日庆典。办得好则扬名四乡，办得差则贻笑大方。请戏班、备酒席、设香案，样样都要银子。",
            choices = {
                {
                    text = "大办特办，请名角唱三天大戏（-15银两，-10粮食，+12声望）",
                    effect = function()
                        GameData.AddResource("silver", -15)
                        GameData.AddResource("grain", -10)
                        GameData.AddResource("fame", 12)
                    end,
                },
                {
                    text = "中规中矩，不失体面（-7银两，-5粮食，+5声望）",
                    effect = function()
                        GameData.AddResource("silver", -7)
                        GameData.AddResource("grain", -5)
                        GameData.AddResource("fame", 5)
                    end,
                },
                {
                    text = "推辞不办，让与他家",
                    effect = function()
                        GameData.AddResource("fame", -4)
                    end,
                },
            },
        }
    end,
}

-- 18. 组建乡勇
events[#events + 1] = {
    id = "r3_militia_form",
    title = "组建乡勇",
    rankRange = {3, 4},
    weight = 6,
    cooldownMonths = 10,
    identityMatch = {"warrior"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local warriors = GameData.GetMembersByState("习武")
        return #warriors > 0
    end,
    execute = function(s, report)
        local warriors = GameData.GetMembersByState("习武")
        local m = warriors[1]
        report.events[#report.events + 1] = "地方不靖，" .. m.name .. "建议组建乡勇。"
        GameData.AddLog(m.name .. "倡议组建乡勇自卫")
        return {
            title = "组建乡勇",
            desc = "近来盗匪猖獗，官兵鞭长莫及。族中" .. m.name .. "有武艺在身，建议招募青壮、置办兵器，组建乡勇以自保。此举需费不少银粮，但若练成可保一方平安。",
            choices = {
                {
                    text = "大力支持，出资招募训练（-10银两，-8粮食，+8声望）",
                    effect = function()
                        GameData.AddResource("silver", -10)
                        GameData.AddResource("grain", -8)
                        GameData.AddResource("fame", 8)
                    end,
                },
                {
                    text = "小规模筹备，先练几个人（-5银两，-3粮食，+3声望）",
                    effect = function()
                        GameData.AddResource("silver", -5)
                        GameData.AddResource("grain", -3)
                        GameData.AddResource("fame", 3)
                    end,
                },
                {
                    text = "此事须慎重，暂且搁置",
                    effect = function() end,
                },
            },
        }
    end,
}

-- 19. 修建粮仓
events[#events + 1] = {
    id = "r3_granary_build",
    title = "修建粮仓",
    rankRange = {3, 4},
    weight = 7,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "旧仓不敷使用，议修新仓。"
        GameData.AddLog("商议修建新粮仓")
        return {
            title = "修建粮仓",
            desc = "族中田亩渐增，旧粮仓已不敷存储，且有鼠患虫蛀之忧。管事建议择高燥之地新建粮仓一座，砖石为基、瓦木为顶，可多存粮食百石。",
            choices = {
                {
                    text = "修建大仓，一劳永逸（-15银两，+30粮食存储）",
                    effect = function()
                        GameData.AddResource("silver", -15)
                        GameData.AddResource("grain", 15)
                    end,
                },
                {
                    text = "修缮旧仓，将就使用（-5银两，+5粮食存储）",
                    effect = function()
                        GameData.AddResource("silver", -5)
                        GameData.AddResource("grain", 5)
                    end,
                },
                {
                    text = "暂且不修，粮食卖了换银子",
                    effect = function()
                        GameData.AddResource("grain", -8)
                        GameData.AddResource("silver", 5)
                    end,
                },
            },
        }
    end,
}

-- 20. 名家字画
events[#events + 1] = {
    id = "r3_calligraphy",
    title = "名家字画",
    rankRange = {3, 4},
    weight = 6,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "有人携名家字画前来兜售。"
        GameData.AddLog("有名家字画可供收藏")
        return {
            title = "名家字画",
            desc = "一落魄书生携祖传字画求售，自称乃前朝大家沈周的山水长卷。细观笔墨确有几分气象，然真伪难辨。若是真迹，价值连城；若是赝品，便白白折了银子。",
            choices = {
                {
                    text = "豪掷买下，挂于厅堂（-12银两，+7声望）",
                    effect = function()
                        GameData.AddResource("silver", -12)
                        GameData.AddResource("fame", 7)
                    end,
                },
                {
                    text = "压价购入，赌上一赌（-5银两，+3声望）",
                    effect = function()
                        GameData.AddResource("silver", -5)
                        GameData.AddResource("fame", 3)
                    end,
                },
                {
                    text = "不识真伪，还是不买",
                    effect = function() end,
                },
            },
        }
    end,
}

-- 21. 人手不足
events[#events + 1] = {
    id = "r3_labor_shortage",
    title = "人手不足",
    rankRange = {3, 4},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "农忙时节人手紧缺，田间活计堆积。"
        GameData.AddLog("农忙人手短缺")
        return {
            title = "人手不足",
            desc = "夏收在即，然今年佃户有数家搬走，余下人手不足以应付田间活计。管事急报，若不及时雇人抢收，只怕粮食烂在地里。然临时雇工费用比往年高出许多。",
            choices = {
                {
                    text = "高价雇工，务必抢收（-8银两，+15粮食）",
                    effect = function()
                        GameData.AddResource("silver", -8)
                        GameData.AddResource("grain", 15)
                    end,
                },
                {
                    text = "合理出价，能收多少算多少（-3银两，+8粮食）",
                    effect = function()
                        GameData.AddResource("silver", -3)
                        GameData.AddResource("grain", 8)
                    end,
                },
                {
                    text = "全靠族中自家人，省了工钱（+3粮食）",
                    effect = function()
                        GameData.AddResource("grain", 3)
                    end,
                },
            },
        }
    end,
}

-- 22. 瘟疫蔓延
events[#events + 1] = {
    id = "r3_plague_outbreak",
    title = "瘟疫蔓延",
    rankRange = {3, 4},
    weight = 4,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "瘟疫从南方传来，乡中已有人染病。"
        GameData.AddLog("瘟疫蔓延至本乡")
        local members = GameData.GetAliveMembers()
        return {
            title = "瘟疫蔓延",
            desc = "南方传来时疫，沿水路蔓延至此。乡中已有数户染病，症见发热咳血、体虚力竭。须速请郎中、购置药材，封闭村口，否则恐全族难保。",
            choices = {
                {
                    text = "重金延医购药，全力防治（-15银两，-5粮食，+10声望）",
                    effect = function()
                        GameData.AddResource("silver", -15)
                        GameData.AddResource("grain", -5)
                        GameData.AddResource("fame", 10)
                    end,
                },
                {
                    text = "备些药材，自行煎服（-6银两，-3粮食）",
                    effect = function()
                        GameData.AddResource("silver", -6)
                        GameData.AddResource("grain", -3)
                    end,
                },
                {
                    text = "闭门不出，隔绝外人（-8粮食，-5声望）",
                    effect = function()
                        GameData.AddResource("grain", -8)
                        GameData.AddResource("fame", -5)
                    end,
                },
            },
        }
    end,
}

-- 23. 酿酒生意
events[#events + 1] = {
    id = "r3_wine_brewing",
    title = "酿酒生意",
    rankRange = {3, 4},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "有人提议用余粮酿酒，另辟财路。"
        GameData.AddLog("商议开办酿酒作坊")
        return {
            title = "酿酒生意",
            desc = "族中管事提议，今年粮食有余，不如置办酒缸、请个酿酒师傅，将余粮酿成好酒。本地酒价不菲，若酿出的酒口味尚可，获利远胜卖粮。然开办作坊须先投入银两粮食。",
            choices = {
                {
                    text = "大干一场，开设酒坊（-8银两，-15粮食，+20银两收益）",
                    effect = function()
                        GameData.AddResource("silver", -8)
                        GameData.AddResource("grain", -15)
                        GameData.AddResource("silver", 20)
                    end,
                },
                {
                    text = "小试牛刀，先酿几缸看看（-3银两，-8粮食，+8银两收益）",
                    effect = function()
                        GameData.AddResource("silver", -3)
                        GameData.AddResource("grain", -8)
                        GameData.AddResource("silver", 8)
                    end,
                },
                {
                    text = "酿酒费粮又费事，还是算了",
                    effect = function() end,
                },
            },
        }
    end,
}

-- 24. 修缮祠堂
events[#events + 1] = {
    id = "r3_ancestral_hall",
    title = "修缮祠堂",
    rankRange = {3, 4},
    weight = 7,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中祠堂年久失修，须出资修缮。"
        GameData.AddLog("商议修缮宗族祠堂")
        return {
            title = "修缮祠堂",
            desc = "族中祠堂建于曾祖之时，历经数十年风雨，屋瓦残破、梁柱虫蛀。族老们商议，祠堂乃宗族体面所系，祖宗牌位供奉之所，不可荒废。须集资修缮，重塑金身，刻碑记事。",
            choices = {
                {
                    text = "独力承担，重修一新（-15银两，-5布匹，+15声望）",
                    effect = function()
                        GameData.AddResource("silver", -15)
                        GameData.AddResource("cloth", -5)
                        GameData.AddResource("fame", 15)
                    end,
                },
                {
                    text = "号召各房分摊，族中共修（-7银两，-3布匹，+7声望）",
                    effect = function()
                        GameData.AddResource("silver", -7)
                        GameData.AddResource("cloth", -3)
                        GameData.AddResource("fame", 7)
                    end,
                },
                {
                    text = "小修小补，先凑合着用（-3银两，+2声望）",
                    effect = function()
                        GameData.AddResource("silver", -3)
                        GameData.AddResource("fame", 2)
                    end,
                },
            },
        }
    end,
}

-- 25. 贪官索贿
events[#events + 1] = {
    id = "r3_corrupt_official",
    title = "贪官索贿",
    rankRange = {3, 4},
    weight = 6,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "县衙胥吏借故上门，明里暗里索要好处。"
        GameData.AddLog("遭贪吏索贿")
        return {
            title = "贪官索贿",
            desc = "县衙税房的张典吏借清查田亩之名登门造访，话里话外暗示若不打点妥当，便要在田册上做文章。此人在县衙盘踞多年，手眼通天，得罪不起；然若任其勒索，日后必变本加厉。",
            choices = {
                {
                    text = "花钱消灾，打发了事（-10银两，-3布匹）",
                    effect = function()
                        GameData.AddResource("silver", -10)
                        GameData.AddResource("cloth", -3)
                    end,
                },
                {
                    text = "略备薄礼，不卑不亢（-5银两）",
                    effect = function()
                        GameData.AddResource("silver", -5)
                    end,
                },
                {
                    text = "硬气拒绝，搜集证据告他（-3银两，+5声望，-3声望风险）",
                    effect = function()
                        GameData.AddResource("silver", -3)
                        GameData.AddResource("fame", 5)
                    end,
                },
            },
        }
    end,
}

return events
