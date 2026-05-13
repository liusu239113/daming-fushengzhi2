local GameData = require("Data.GameData")
local events = {}

-- 1. 御前觐见
events[#events + 1] = {
    id = "r6_emperor_audience",
    title = "御前觐见",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "天子传召，族中长者入朝觐见，龙颜大悦，赐以厚赏。"
        GameData.AddLog("族人入宫觐见天子，获赐御酒锦缎。")
        return {
            title = "御前觐见", desc = "圣上传召觐见，金殿之上龙威赫赫。族人伏拜丹墀，天子询及家世功业，颇为嘉许。此乃光耀门楣之千载良机，当如何应对？",
            choices = {
                { text = "进献珍宝以表忠心", effect = function()
                    GameData.AddResource("silver", -50)
                    GameData.AddResource("fame", 35)
                    GameData.AddLog("进献珍宝于御前，圣上龙颜大悦，声望大增。")
                end },
                { text = "恭谨对答不卑不亢", effect = function()
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("觐见应答得体，天子略有嘉许。")
                end },
            }
        }
    end,
}

-- 2. 封爵晋升
events[#events + 1] = {
    id = "r6_noble_title",
    title = "封爵晋升",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "朝廷颁旨，族中因功勋卓著而获爵位晋升之恩典。"
        GameData.AddLog("圣旨降临，族人获封爵位。")
        return {
            title = "封爵晋升", desc = "朝廷论功行赏，族中积年之功终获圣上垂青，特降恩旨晋封爵位。封爵之仪需大排筵宴，昭告四方，然开销甚巨。",
            choices = {
                { text = "大办仪典光耀门楣", effect = function()
                    GameData.AddResource("silver", -60)
                    GameData.AddResource("cloth", -20)
                    GameData.AddResource("fame", 40)
                    GameData.AddLog("封爵大典隆重举行，声名远播。")
                end },
                { text = "简办仪式低调受封", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("简朴受封，虽不张扬亦得体面。")
                end },
            }
        }
    end,
}

-- 3. 朝堂阴谋
events[#events + 1] = {
    id = "r6_court_conspiracy",
    title = "朝堂阴谋",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "朝中风云突变，有人密谋政变，族人被卷入漩涡之中。"
        GameData.AddLog("朝堂暗流涌动，族人被卷入宫廷阴谋。")
        return {
            title = "朝堂阴谋", desc = "夜半有人密送帛书，言及朝中重臣暗结党羽、图谋不轨。此事若成则改天换日，若败则株连九族。族人身处高位，已难置身事外。",
            choices = {
                { text = "密报天子以示忠诚", effect = function()
                    GameData.AddResource("fame", 30)
                    GameData.AddResource("silver", 40)
                    GameData.AddLog("密报阴谋有功，获天子嘉赏。")
                end },
                { text = "韬光养晦静观其变", effect = function()
                    GameData.AddResource("fame", -10)
                    GameData.AddLog("未表明立场，朝中风波渐息，暂且无虞。")
                end },
            }
        }
    end,
}

-- 4. 领军出征
events[#events + 1] = {
    id = "r6_war_command",
    title = "领军出征",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = {"warrior"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local warriors = GameData.GetMembersByState("从军")
        return #warriors > 0
    end,
    execute = function(s, report)
        local warriors = GameData.GetMembersByState("从军")
        local leader = warriors[1]
        report.events[#report.events + 1] = leader.name .. "奉旨统兵出征，三军听令。"
        GameData.AddLog(leader.name .. "被任命为统帅，领军出征。")
        return {
            title = "领军出征", desc = "边关告急，圣上钦点族中" .. leader.name .. "为帅，统兵十万出征讨逆。大军旌旗蔽日、甲光耀天，此一战关乎国运兴亡。",
            choices = {
                { text = "倾族中之力全力支持", effect = function()
                    GameData.AddResource("silver", -50)
                    GameData.AddResource("grain", -60)
                    GameData.AddResource("fame", 35)
                    GameData.AddLog("倾力支持出征，军功赫赫，声望大振。")
                end },
                { text = "按规制供给不多不少", effect = function()
                    GameData.AddResource("grain", -25)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("按制供给军需，出征顺利。")
                end },
            }
        }
    end,
}

-- 5. 贡品使命
events[#events + 1] = {
    id = "r6_tribute_mission",
    title = "贡品使命",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "朝廷委派族人督办岁贡事宜，责任重大。"
        GameData.AddLog("族人受命督办进贡事务。")
        return {
            title = "贡品使命", desc = "朝廷命族中督办今岁贡品，需搜罗奇珍异宝、锦缎丝绸，护送入京。此乃信任之托，亦是敛财之机，当如何行事？",
            choices = {
                { text = "精心置办呈上等贡品", effect = function()
                    GameData.AddResource("silver", -40)
                    GameData.AddResource("cloth", -30)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("贡品精美，天子大悦，族人深受信赖。")
                end },
                { text = "从中周旋留有余利", effect = function()
                    GameData.AddResource("silver", 30)
                    GameData.AddResource("fame", -15)
                    GameData.AddLog("贡品之事暗中取利，虽有所得，名声略损。")
                end },
            }
        }
    end,
}

-- 6. 王朝倾覆
events[#events + 1] = {
    id = "r6_dynasty_fall",
    title = "王朝倾覆",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = {1640, 1644},
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "天下大乱，王朝将倾，族人面临生死抉择。"
        GameData.AddLog("社稷崩摧，王朝覆灭在即。")
        return {
            title = "王朝倾覆", desc = "烽火连天，京师失陷，天子殉国。数百年基业一朝倾覆，山河易主。族中世代忠良，如今何去何从？是殉节以全大义，还是顺时以保宗族？",
            choices = {
                { text = "誓死效忠不事二主", effect = function()
                    GameData.AddResource("silver", -60)
                    GameData.AddResource("grain", -40)
                    GameData.AddResource("fame", 40)
                    GameData.AddLog("族人以死明志，忠义之名传遍天下。")
                end },
                { text = "审时度势保全宗族", effect = function()
                    GameData.AddResource("fame", -30)
                    GameData.AddResource("silver", 20)
                    GameData.AddLog("族人降顺新朝，宗族得保，然忠义之名尽失。")
                end },
            }
        }
    end,
}

-- 7. 御赐庄园
events[#events + 1] = {
    id = "r6_imperial_garden",
    title = "御赐庄园",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "天子恩赐御苑别庄一座，族人喜不自胜。"
        GameData.AddLog("蒙天子恩典，御赐庄园一座。")
        return {
            title = "御赐庄园", desc = "圣上念及族中数代忠勤，特赐京郊御苑别庄一座，良田千亩、亭台楼阁俱全。此等殊荣百年难遇，然经营维护亦需大量银资。",
            choices = {
                { text = "精心经营使之繁盛", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("grain", 60)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("御赐庄园经营得当，年产丰饶。")
                end },
                { text = "将庄园佃租坐收其利", effect = function()
                    GameData.AddResource("silver", 50)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("庄园佃租他人，岁入颇丰。")
                end },
            }
        }
    end,
}

-- 8. 政治清洗
events[#events + 1] = {
    id = "r6_political_purge",
    title = "政治清洗",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "朝中掀起大狱，党争株连甚广，族人被牵涉其中。"
        GameData.AddLog("党争之祸蔓延，族人受牵连。")
        return {
            title = "政治清洗", desc = "朝中奸臣当道，兴起文字大狱，罗织罪名株连甚广。族中因往日交游被列入嫌疑之列，锦衣卫已在城中缉拿。危急存亡之秋，须当机立断。",
            choices = {
                { text = "散尽家财上下打点", effect = function()
                    GameData.AddResource("silver", -60)
                    GameData.AddResource("cloth", -20)
                    GameData.AddResource("fame", -10)
                    GameData.AddLog("倾家荡产疏通关节，勉强脱险。")
                end },
                { text = "坦然面对据理力争", effect = function()
                    GameData.AddResource("fame", 15)
                    GameData.AddResource("silver", -30)
                    GameData.AddLog("据理申辩，虽受波折终获清白。")
                end },
            }
        }
    end,
}

-- 9. 华宴排场
events[#events + 1] = {
    id = "r6_grand_feast",
    title = "华宴排场",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中大排筵宴，广邀朝中显贵，觥筹交错、冠盖如云。"
        GameData.AddLog("族中举办盛大华宴。")
        return {
            title = "华宴排场", desc = "值族中喜庆之日，需设宴款待朝中显贵。席间丝竹悠扬、珍馐罗列，觥筹交错间或可结交权贵、广布人脉。然排场愈大，耗费愈巨。",
            choices = {
                { text = "极尽奢华大宴百席", effect = function()
                    GameData.AddResource("silver", -50)
                    GameData.AddResource("grain", -40)
                    GameData.AddResource("cloth", -15)
                    GameData.AddResource("fame", 30)
                    GameData.AddLog("华宴盛极一时，满朝文武赞不绝口。")
                end },
                { text = "雅致小宴只请至交", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("grain", -10)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("小宴清雅，知交好友尽欢而散。")
                end },
            }
        }
    end,
}

-- 10. 外族入侵
events[#events + 1] = {
    id = "r6_foreign_invasion",
    title = "外族入侵",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = {1616, 1644},
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "北方铁骑南下，烽烟四起，边关告急。"
        GameData.AddLog("外族大举入侵，天下震动。")
        return {
            title = "外族入侵", desc = "北方铁骑破关而入，所过之处生灵涂炭。朝廷急令各地勤王，族中位高权重，避无可避。是倾力御敌以卫社稷，还是固守家业以图自保？",
            choices = {
                { text = "散家纾难倾力勤王", effect = function()
                    GameData.AddResource("silver", -50)
                    GameData.AddResource("grain", -50)
                    GameData.AddResource("fame", 35)
                    GameData.AddLog("倾力勤王，虽损失惨重然忠名远扬。")
                end },
                { text = "固守族地保全实力", effect = function()
                    GameData.AddResource("grain", -20)
                    GameData.AddResource("fame", -20)
                    GameData.AddLog("固守不出，宗族得保然朝中非议不断。")
                end },
            }
        }
    end,
}

-- 11. 庇护文艺
events[#events + 1] = {
    id = "r6_patronage_arts",
    title = "庇护文艺",
    rankRange = {5, 6},
    weight = 8,
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
        report.events[#report.events + 1] = m.name .. "倡议资助天下名儒，弘扬文脉。"
        GameData.AddLog(m.name .. "主持庇护文艺之事。")
        return {
            title = "庇护文艺", desc = "族中" .. m.name .. "素好文章，欲效古之孟尝，广延天下名儒于府中讲学著述。此举可使族中文脉昌盛、声名鹊起，然需长年供养，靡费不赀。",
            choices = {
                { text = "开设书院广延名儒", effect = function()
                    GameData.AddResource("silver", -40)
                    GameData.AddResource("grain", -20)
                    GameData.AddResource("fame", 30)
                    GameData.AddLog("书院落成，四方名儒云集，文风大盛。")
                end },
                { text = "择一二贤者私下资助", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("fame", 12)
                    GameData.AddLog("暗中资助贤儒数人，略有文名。")
                end },
            }
        }
    end,
}

-- 12. 嫡庶之争
events[#events + 1] = {
    id = "r6_succession_war",
    title = "嫡庶之争",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        return #adults >= 3
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中嫡庶争位，内斗日烈，几近分裂。"
        GameData.AddLog("嫡庶之争愈演愈烈。")
        return {
            title = "嫡庶之争", desc = "族中家主年迈体衰，嫡庶子弟各怀心思，暗中结党争夺继承大位。族内人心惶惶，若不及时平息，百年基业恐毁于一旦。",
            choices = {
                { text = "秉公立嫡平息纷争", effect = function()
                    GameData.AddResource("fame", 20)
                    GameData.AddResource("silver", -20)
                    GameData.AddLog("立嫡定分，族中纷争暂息。")
                end },
                { text = "择贤而立不论嫡庶", effect = function()
                    GameData.AddResource("fame", 10)
                    GameData.AddResource("silver", -10)
                    GameData.AddLog("择贤继位，虽有异议然族务渐稳。")
                end },
            }
        }
    end,
}

-- 13. 组建船队
events[#events + 1] = {
    id = "r6_maritime_fleet",
    title = "组建船队",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = {"merchant"},
    era = {1368, 1500},
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local merchants = GameData.GetMembersByState("经商")
        return #merchants > 0
    end,
    execute = function(s, report)
        local merchants = GameData.GetMembersByState("经商")
        local m = merchants[1]
        report.events[#report.events + 1] = m.name .. "筹划组建远洋船队，出海贸易。"
        GameData.AddLog(m.name .. "主持筹建远洋船队。")
        return {
            title = "组建船队", desc = "族中" .. m.name .. "观海外之利甚厚，欲效三保太监旧事，组建船队远赴南洋。所需巨资造船、招募水手、备办货物，然一旦成行获利可达十倍。",
            choices = {
                { text = "倾力建造大型船队", effect = function()
                    GameData.AddResource("silver", -60)
                    GameData.AddResource("cloth", -25)
                    GameData.AddResource("silver", 80)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("船队扬帆出海，满载而归，获利丰厚。")
                end },
                { text = "小规模试探出海", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("silver", 35)
                    GameData.AddResource("fame", 8)
                    GameData.AddLog("小船队试航南洋，略有斩获。")
                end },
            }
        }
    end,
}

-- 14. 天下大饥
events[#events + 1] = {
    id = "r6_great_famine",
    title = "天下大饥",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "天灾连年，赤地千里，饿殍遍野，天下大饥。"
        GameData.AddLog("天下大饥，民不聊生。")
        return {
            title = "天下大饥", desc = "连年旱蝗并至，禾稼尽枯，赤地千里。饥民扶老携幼流离失所，易子而食之惨状触目惊心。族中虽有存粮，然流民日众，若开仓赈济恐自身难保。",
            choices = {
                { text = "开仓放粮赈济灾民", effect = function()
                    GameData.AddResource("grain", -60)
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("fame", 40)
                    GameData.AddLog("大开粮仓救济万民，仁义之名传遍天下。")
                end },
                { text = "紧守粮仓先保宗族", effect = function()
                    GameData.AddResource("grain", -15)
                    GameData.AddResource("fame", -20)
                    GameData.AddLog("闭仓不出，族人虽安然外界怨声载道。")
                end },
            }
        }
    end,
}

-- 15. 皇家赐婚
events[#events + 1] = {
    id = "r6_royal_marriage",
    title = "皇家赐婚",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        for _, m in ipairs(adults) do
            if not m.spouseId and m.age >= 16 and m.age <= 30 then
                return true
            end
        end
        return false
    end,
    execute = function(s, report)
        local adults = GameData.GetAdultMembers()
        local candidate = nil
        for _, m in ipairs(adults) do
            if not m.spouseId and m.age >= 16 and m.age <= 30 then
                candidate = m
                break
            end
        end
        local name = candidate and candidate.name or "族中子弟"
        report.events[#report.events + 1] = "天子赐婚，" .. name .. "将与皇族联姻。"
        GameData.AddLog("皇家赐婚，" .. name .. "蒙此殊荣。")
        return {
            title = "皇家赐婚", desc = "圣上闻族中" .. name .. "才貌出众，特降恩旨赐婚皇族。此乃天恩浩荡，攀龙附凤之良机。是否为" .. name .. "前去相看？",
            choices = {
                { text = "前去相看", effect = function()
                    if candidate and not candidate.spouseId then
                        local GS = require("UI.GameScreen")
                        GS.ShowMarriageTierSelect(candidate)
                    end
                end },
                { text = "婉拒圣恩（声望-15）", effect = function()
                    GameData.AddResource("fame", -15)
                    GameData.AddLog(name .. "婉拒皇家赐婚，圣上略有不悦。")
                end },
            }
        }
    end,
}

-- 16. 心腹背叛
events[#events + 1] = {
    id = "r6_betrayal",
    title = "心腹背叛",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中心腹暗中勾结外人，盗取机密账册。"
        GameData.AddLog("心腹背叛，族中机密外泄。")
        return {
            title = "心腹背叛", desc = "族中倚重多年之心腹管事竟暗通外人，盗走田产契约与机密账册，更将族中隐秘之事泄于政敌。此人知悉太多内情，若不速速处置恐后患无穷。",
            choices = {
                { text = "不惜代价追回叛徒", effect = function()
                    GameData.AddResource("silver", -35)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("派人追缉叛徒，追回部分机密，危机暂解。")
                end },
                { text = "亡羊补牢加固内防", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("fame", -10)
                    GameData.AddLog("整顿内务亡羊补牢，然已失之机密无可挽回。")
                end },
            }
        }
    end,
}

-- 17. 太平盛世
events[#events + 1] = {
    id = "r6_golden_age",
    title = "太平盛世",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = {1400, 1550},
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "四海升平、物阜民丰，族中迎来鼎盛之期。"
        GameData.AddLog("太平盛世，百业兴旺。")
        return {
            title = "太平盛世", desc = "天下承平日久，五谷丰登、百业俱兴。朝廷吏治清明，四方来朝。族中正可乘此盛世之机大展宏图，或广置产业、或投身仕途。",
            choices = {
                { text = "广置良田扩充产业", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("grain", 60)
                    GameData.AddResource("cloth", 30)
                    GameData.AddLog("盛世置业，产业大增，岁入倍于往年。")
                end },
                { text = "投资族学培养人才", effect = function()
                    GameData.AddResource("silver", -25)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("兴办族学，子弟成才者众，声名远播。")
                end },
            }
        }
    end,
}

-- 18. 军制改革
events[#events + 1] = {
    id = "r6_military_reform",
    title = "军制改革",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = {"warrior"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local warriors = GameData.GetMembersByState("从军")
        return #warriors > 0
    end,
    execute = function(s, report)
        local warriors = GameData.GetMembersByState("从军")
        local m = warriors[1]
        report.events[#report.events + 1] = m.name .. "受命参与朝廷军制改革大计。"
        GameData.AddLog(m.name .. "参与军制改革。")
        return {
            title = "军制改革", desc = "朝廷锐意革新军制，" .. m.name .. "因精通兵法被委以参议之任。变法虽利国利民，然触动旧勋贵利益，阻力甚大。是锐意改革还是左右逢源？",
            choices = {
                { text = "全力推行新法", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("fame", 30)
                    GameData.AddLog("军制改革有所成效，" .. m.name .. "声望大增。")
                end },
                { text = "折中调和各方利益", effect = function()
                    GameData.AddResource("silver", 15)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("改革温和推进，各方勉强接受。")
                end },
            }
        }
    end,
}

-- 19. 宗族大会
events[#events + 1] = {
    id = "r6_clan_reunion",
    title = "宗族大会",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local members = GameData.GetAliveMembers()
        return #members >= 5
    end,
    execute = function(s, report)
        local members = GameData.GetAliveMembers()
        report.events[#report.events + 1] = "召集各房各支，举行百年一遇之宗族大会。"
        GameData.AddLog("宗族大会隆重召开，族人" .. #members .. "口齐聚。")
        return {
            title = "宗族大会", desc = "族中决定召集天下各房各支、远近亲疏，齐聚祖祠举行百年大祭。修缮祠堂、编纂族谱、议定族规，此乃凝聚宗族之盛事。现有族人" .. #members .. "口。",
            choices = {
                { text = "大修祠堂重修族谱", effect = function()
                    GameData.AddResource("silver", -40)
                    GameData.AddResource("grain", -30)
                    GameData.AddResource("fame", 30)
                    GameData.AddLog("祠堂焕然一新，族谱续修完成，宗族凝聚力大增。")
                end },
                { text = "简朴聚会重在叙情", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("grain", -15)
                    GameData.AddResource("fame", 12)
                    GameData.AddLog("族人相聚叙旧，虽简朴亦温馨。")
                end },
            }
        }
    end,
}

-- 20. 宝库清点
events[#events + 1] = {
    id = "r6_treasure_vault",
    title = "宝库清点",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中清查宝库，发现历代珍藏与陈年亏空。"
        GameData.AddLog("清点宝库，查验家产。")
        return {
            title = "宝库清点", desc = "族中例行清查宝库，竟发现先祖遗留之珍宝数件，价值连城。然亦查出历年管事暗中挪用之亏空，触目惊心。当如何处置？",
            choices = {
                { text = "变卖珍宝填补亏空", effect = function()
                    GameData.AddResource("silver", 50)
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("珍宝变卖充库，亏空已补，财务清朗。")
                end },
                { text = "珍宝入库严查贪墨", effect = function()
                    GameData.AddResource("silver", 20)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("严查贪墨追回部分款项，族规肃然。")
                end },
            }
        }
    end,
}

-- 21. 边疆危机
events[#events + 1] = {
    id = "r6_border_crisis",
    title = "边疆危机",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "边疆烽火骤起，朝廷急令勋贵出资出力。"
        GameData.AddLog("边疆战事吃紧，朝廷催办军饷。")
        return {
            title = "边疆危机", desc = "西北边关遭敌大举寇犯，守将战死、城池连失。朝廷急诏天下勋贵捐资助饷，更有风声言或将抽丁征发。边关战火若不速扑，恐燃及腹地。",
            choices = {
                { text = "慷慨解囊捐银助饷", effect = function()
                    GameData.AddResource("silver", -50)
                    GameData.AddResource("grain", -40)
                    GameData.AddResource("fame", 30)
                    GameData.AddLog("捐资助饷，边关得以稳固，天子嘉许。")
                end },
                { text = "仅出最低限额敷衍", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("grain", -10)
                    GameData.AddResource("fame", -15)
                    GameData.AddLog("敷衍了事，朝中颇有微词。")
                end },
            }
        }
    end,
}

-- 22. 科举改革
events[#events + 1] = {
    id = "r6_imperial_exam_reform",
    title = "科举改革",
    rankRange = {5, 6},
    weight = 8,
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
        report.events[#report.events + 1] = m.name .. "参与朝廷科举改革之议。"
        GameData.AddLog(m.name .. "上书论科举改革。")
        return {
            title = "科举改革", desc = "朝廷议改科举取士之法，" .. m.name .. "以学识渊博被召入议政。有人主张废八股而用策论，有人力主守旧制。族中在科场素有根基，改革成败关系甚大。",
            choices = {
                { text = "力倡改革推行新学", effect = function()
                    GameData.AddResource("silver", -25)
                    GameData.AddResource("fame", 30)
                    GameData.AddLog("改革奏议获准，" .. m.name .. "声望大振。")
                end },
                { text = "维护旧制保族中优势", effect = function()
                    GameData.AddResource("fame", 5)
                    GameData.AddResource("silver", 15)
                    GameData.AddLog("旧制不改，族中科场根基暂稳。")
                end },
            }
        }
    end,
}

-- 23. 丝路商道
events[#events + 1] = {
    id = "r6_silk_road",
    title = "丝路商道",
    rankRange = {5, 6},
    weight = 8,
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
        report.events[#report.events + 1] = m.name .. "筹划开辟西域丝路商道。"
        GameData.AddLog(m.name .. "着手经营丝路贸易。")
        return {
            title = "丝路商道", desc = "族中" .. m.name .. "闻西域商路复通，欲组织驼队贩运丝绸茶叶至西域，换回宝石香料。此路虽险，然获利极丰。沿途需打点关卡、雇佣护卫。",
            choices = {
                { text = "组建大型商队远赴西域", effect = function()
                    GameData.AddResource("silver", -45)
                    GameData.AddResource("cloth", -30)
                    GameData.AddResource("silver", 75)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("商队平安往返，丝路贸易获利丰厚。")
                end },
                { text = "委托他人代为经营", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("silver", 25)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("委托商贾代营，分润虽薄胜在稳妥。")
                end },
            }
        }
    end,
}

-- 24. 天命之说
events[#events + 1] = {
    id = "r6_heaven_mandate",
    title = "天命之说",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = {1630, 1644},
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "民间流传天命将改之谶语，人心惶惶。"
        GameData.AddLog("天命之说流传坊间，改朝换代征兆频现。")
        return {
            title = "天命之说", desc = "近年天灾不断，民间流传'天命将改'之谶语，更有方士指星象异变、帝星晦暗。朝野上下人心浮动，族中亦议论纷纷。身处高位，当早做打算。",
            choices = {
                { text = "暗中筹备以应变局", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("grain", 40)
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("暗中囤粮备银，以防变局。")
                end },
                { text = "公开辟谣稳定人心", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("出面辟谣安抚人心，朝廷赞许。")
                end },
            }
        }
    end,
}

-- 25. 千秋万代
events[#events + 1] = {
    id = "r6_eternal_legacy",
    title = "千秋万代",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local members = GameData.GetAliveMembers()
        return #members >= 8
    end,
    execute = function(s, report)
        local members = GameData.GetAliveMembers()
        report.events[#report.events + 1] = "族中谋划建立不朽基业，以传千秋万代。"
        GameData.AddLog("族人共议千秋大计，欲立不朽基业。")
        return {
            title = "千秋万代", desc = "族中已历数代兴盛，现有族人" .. #members .. "口。族老们聚议，欲集数代之积蓄建一不朽基业——或筑宏伟宗祠以祀先祖，或设万亩义田以养后昆。此举关乎宗族千秋命脉。",
            choices = {
                { text = "建宏伟宗祠传万世香火", effect = function()
                    GameData.AddResource("silver", -55)
                    GameData.AddResource("cloth", -20)
                    GameData.AddResource("fame", 40)
                    GameData.AddLog("宗祠巍峨落成，雕梁画栋、气象万千，百世瞻仰。")
                end },
                { text = "设万亩义田泽被后世", effect = function()
                    GameData.AddResource("silver", -40)
                    GameData.AddResource("grain", 80)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("义田广设，岁入丰饶，族人世代受益无穷。")
                end },
            }
        }
    end,
}

return events
