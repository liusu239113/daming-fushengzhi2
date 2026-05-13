local GameData = require("Data.GameData")
local events = {}

-- 1. 朝堂博弈
events[#events + 1] = {
    id = "r5_court_politics",
    title = "朝堂博弈",
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
        report.events[#report.events + 1] = "朝堂之上暗流涌动，各方势力争相角逐，我族亦被卷入其中"
        GameData.AddLog("朝堂风云变幻，族中长辈商议应对之策")
        return {
            title = "朝堂博弈", desc = "朝中大臣分为两派，围绕一项重要国策争执不休。以我族如今的地位，难以置身事外，必须表明立场。支持哪一方，将决定家族在朝中的未来走向。",
            choices = {
                { text = "支持改革派，锐意进取", effect = function()
                    GameData.AddResource("silver", -25)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("族中力挺改革派，声望大振，然花费银两打点关系")
                end },
                { text = "支持保守派，稳中求进", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("族中依附保守派，虽无大功，亦保平安")
                end },
                { text = "两不相帮，韬光养晦", effect = function()
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("族中选择中立观望，朝中有人暗讽我族首鼠两端")
                end },
            }
        }
    end,
}

-- 2. 皇恩眷顾
events[#events + 1] = {
    id = "r5_imperial_favor",
    title = "皇恩眷顾",
    rankRange = {5, 6},
    weight = 6,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return true
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "天子下旨嘉奖，御赐匾额与珍宝，举族荣耀"
        GameData.AddLog("圣旨降临，天恩浩荡，族人跪迎御赐")
        return {
            title = "皇恩眷顾", desc = "圣上念及我族世代忠良，特颁旨嘉奖，赐下御笔匾额并金银珠宝。此乃莫大殊荣，然御赐之物如何安置处理，族中意见不一。",
            choices = {
                { text = "高悬匾额于祠堂，珍宝入库收藏", effect = function()
                    GameData.AddResource("silver", 40)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("御赐匾额高悬宗祠，珍宝入库，阖族引以为荣")
                end },
                { text = "将部分赏赐分予族中贫苦子弟", effect = function()
                    GameData.AddResource("silver", 20)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("族长将御赐之物分润族人，人心更加归附")
                end },
            }
        }
    end,
}

-- 3. 创办族学
events[#events + 1] = {
    id = "r5_clan_academy",
    title = "创办族学",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local members = GameData.GetAliveMembers()
        return #members >= 5
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中议定创办族学，延请名师，教导子弟读书习礼"
        GameData.AddLog("族学筹建事宜提上日程，各房踊跃捐资")
        return {
            title = "创办族学", desc = "家族日益兴旺，子弟众多却缺乏统一教导。族中长辈提议创办族学，聘请饱学之士为师，教授经史子集与处世之道。此举利在千秋，然耗资甚巨。",
            choices = {
                { text = "斥巨资兴建，延聘当世大儒", effect = function()
                    GameData.AddResource("silver", -40)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("族学落成，大儒坐镇讲学，远近子弟慕名而来")
                end },
                { text = "量力而行，先设简易学堂", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("学堂初建，虽简陋却也有了读书声")
                end },
                { text = "暂缓此事，待时机成熟再议", effect = function()
                    GameData.AddLog("族学之议暂且搁置，有人叹息子弟教育不可再拖")
                end },
            }
        }
    end,
}

-- 4. 商业帝国
events[#events + 1] = {
    id = "r5_trade_empire",
    title = "商业帝国",
    rankRange = {5, 6},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = {"merchant"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local merchants = GameData.GetMembersByState("经商")
        return #merchants >= 1
    end,
    execute = function(s, report)
        local merchants = GameData.GetMembersByState("经商")
        local m = merchants[1]
        report.events[#report.events + 1] = m.name .. "运筹帷幄，商号遍布数省，隐有商业帝国之势"
        GameData.AddLog(m.name .. "的商业版图持续扩张")
        return {
            title = "商业帝国", desc = m.name .. "经营有道，如今商号已遍布三省六府，丝绸、茶叶、瓷器贸易络绎不绝。有人建议趁势扩张，将生意做到更远之处；也有人担忧树大招风，恐遭朝廷猜忌。",
            choices = {
                { text = "大举扩张，垄断数省商路", effect = function()
                    GameData.AddResource("silver", 50)
                    GameData.AddResource("fame", -10)
                    GameData.AddLog(m.name .. "大举扩张商号，日进斗金，然商名太盛引人侧目")
                end },
                { text = "稳健经营，同时向朝廷纳捐", effect = function()
                    GameData.AddResource("silver", 30)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog(m.name .. "稳健经营并向朝廷纳捐，商誉与官声兼得")
                end },
                { text = "分散经营，化整为零以避嫌", effect = function()
                    GameData.AddResource("silver", 15)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog(m.name .. "化整为零分散商号，虽利润减少但无人侧目")
                end },
            }
        }
    end,
}

-- 5. 军功报国
events[#events + 1] = {
    id = "r5_war_contribution",
    title = "军功报国",
    rankRange = {5, 6},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = {"warrior"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local warriors = GameData.GetMembersByState("从军")
        return #warriors >= 1
    end,
    execute = function(s, report)
        local warriors = GameData.GetMembersByState("从军")
        local m = warriors[1]
        report.events[#report.events + 1] = m.name .. "在边关立下赫赫战功，朝廷论功行赏"
        GameData.AddLog(m.name .. "率部出征，捷报传来")
        return {
            title = "军功报国", desc = "边疆告急，朝廷调兵遣将。" .. m.name .. "奉命率部驰援，经数月鏖战终于击退来犯之敌。朝廷论功行赏，问我族欲求何赏。",
            choices = {
                { text = "请封世袭武职，光耀门楣", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("fame", 30)
                    GameData.AddLog(m.name .. "获封武职，族中又添一柱国之臣")
                end },
                { text = "请赐田产金银，充实家业", effect = function()
                    GameData.AddResource("silver", 40)
                    GameData.AddResource("grain", 30)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog(m.name .. "之功换得大片田产与金银赏赐")
                end },
                { text = "请恩荫子弟入国子监读书", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog(m.name .. "以军功换得子弟入国子监的名额")
                end },
            }
        }
    end,
}

-- 6. 农民起义
events[#events + 1] = {
    id = "r5_peasant_revolt",
    title = "农民起义",
    rankRange = {5, 6},
    weight = 6,
    cooldownMonths = 12,
    identityMatch = nil,
    era = {1620, 1644},
    isDisaster = true,
    isChainEvent = false,
    check = function(s)
        return true
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "天下大乱，流民四起，各地农民揭竿而起，兵锋直指州府"
        GameData.AddLog("烽烟遍地，起义军逼近本族领地")
        return {
            title = "农民起义", desc = "连年灾荒加上苛捐杂税，百姓终于忍无可忍。数万流民聚众起义，席卷数县之地。起义军已逼近我族所在之地，族中必须做出抉择。",
            choices = {
                { text = "组织乡勇坚守，誓与家园共存亡", effect = function()
                    GameData.AddResource("silver", -35)
                    GameData.AddResource("grain", -40)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("族中组织乡勇拼死抵抗，虽损失惨重但保住了根基")
                end },
                { text = "开仓放粮安抚流民，化干戈为玉帛", effect = function()
                    GameData.AddResource("grain", -50)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("族中开仓济民，流民感恩绕道而行，粮仓却近乎见底")
                end },
                { text = "举族暂避他方，保全族人性命", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("grain", -20)
                    GameData.AddResource("fame", -15)
                    GameData.AddLog("族人仓皇出逃，家产损失不少，有人讥讽我族贪生怕死")
                end },
            }
        }
    end,
}

-- 7. 外交使命
events[#events + 1] = {
    id = "r5_diplomatic_mission",
    title = "外交使命",
    rankRange = {5, 6},
    weight = 6,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        return #adults >= 2
    end,
    execute = function(s, report)
        local adults = GameData.GetAdultMembers()
        local m = adults[1]
        report.events[#report.events + 1] = "朝廷委派" .. m.name .. "出使邻邦，代天子行外交之事"
        GameData.AddLog(m.name .. "受命出使，肩负邦交重任")
        return {
            title = "外交使命", desc = "朝廷选中我族" .. m.name .. "出使西域邻邦，商讨通商互市与边界划定事宜。此行路途遥远，凶险难测，然若功成，我族在朝中地位将更上一层。",
            choices = {
                { text = "备厚礼出使，展现大国气度", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("cloth", -15)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog(m.name .. "携重礼出使成功，邦交议成，朝廷大悦")
                end },
                { text = "轻车简从，以才学折服对方", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog(m.name .. "以三寸之舌折冲樽俎，虽无厚礼亦不辱使命")
                end },
            }
        }
    end,
}

-- 8. 家族丑闻
events[#events + 1] = {
    id = "r5_family_scandal",
    title = "家族丑闻",
    rankRange = {5, 6},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local members = GameData.GetAliveMembers()
        return #members >= 4
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中丑事外泄，坊间议论纷纷，家族颜面受损"
        GameData.AddLog("家丑不幸外扬，族中上下忧心忡忡")
        return {
            title = "家族丑闻", desc = "族中一房子弟行为不端，强占他人田产、欺凌乡里之事被人告上衙门。此事传得沸沸扬扬，若处置不当，多年积累的声望将毁于一旦。",
            choices = {
                { text = "大义灭亲，严惩不贷以正家风", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("族长当众惩处不肖子弟，退还田产，世人称赞家风严正")
                end },
                { text = "私下调解，赔偿了事息事宁人", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("族中花重金平息此事，然坊间仍有闲言碎语")
                end },
                { text = "动用关系压下此事，官府不再追究", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("fame", -15)
                    GameData.AddLog("此事虽被压下，但民间怨气难消，族中名声受损")
                end },
            }
        }
    end,
}

-- 9. 大兴土木
events[#events + 1] = {
    id = "r5_grand_construction",
    title = "大兴土木",
    rankRange = {5, 6},
    weight = 8,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return true
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中决议修缮扩建宅邸，以配世家之名"
        GameData.AddLog("大兴土木之议提上案头，各房议论纷纷")
        return {
            title = "大兴土木", desc = "家族地位日隆，然祖宅年久失修，已不堪世家体面。族中提议重修宅邸，扩建园林、祠堂、藏书楼，使之与我族身份相配。此举需耗费大量银钱与人力。",
            choices = {
                { text = "倾力营建，修筑气派庄园", effect = function()
                    GameData.AddResource("silver", -40)
                    GameData.AddResource("cloth", -10)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("庄园落成，亭台楼阁错落有致，远近闻名")
                end },
                { text = "适度修缮，重点修祠堂与书房", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("祠堂与书房焕然一新，族人颇感欣慰")
                end },
                { text = "暂不动工，银钱留作他用", effect = function()
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("修缮之议搁浅，有客来访见宅邸破旧不禁唏嘘")
                end },
            }
        }
    end,
}

-- 10. 敌军来袭
events[#events + 1] = {
    id = "r5_enemy_attack",
    title = "敌军来袭",
    rankRange = {5, 6},
    weight = 5,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s)
        return true
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "外敌入侵，铁骑南下，兵临城下，生灵涂炭"
        GameData.AddLog("敌军铁骑逼近，族中紧急商议对策")
        return {
            title = "敌军来袭", desc = "北方游牧骑兵大举南侵，沿途烧杀抢掠。官军节节败退，敌军前锋距我族不过百里。族中必须立即做出决断，事关全族存亡。",
            choices = {
                { text = "组织族人协助官军抗敌", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("grain", -35)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("族中子弟浴血奋战，与官军并肩击退敌军，伤亡不小")
                end },
                { text = "加固坞堡固守，坚壁清野", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("grain", -25)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("坞堡固若金汤，敌军劫掠一番后退去，田产损失惨重")
                end },
                { text = "携细软南迁避祸", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("grain", -30)
                    GameData.AddResource("fame", -20)
                    GameData.AddLog("举族南逃，祖宅被毁，田产尽失，声望一落千丈")
                end },
            }
        }
    end,
}

-- 11. 文化赞助
events[#events + 1] = {
    id = "r5_cultural_patronage",
    title = "文化赞助",
    rankRange = {5, 6},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = {"scholar"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local scholars = GameData.GetMembersByState("读书")
        return #scholars >= 1
    end,
    execute = function(s, report)
        local scholars = GameData.GetMembersByState("读书")
        local m = scholars[1]
        report.events[#report.events + 1] = m.name .. "倡议资助文人雅士，弘扬诗书礼乐之风"
        GameData.AddLog(m.name .. "提议延揽天下文士，传播家族文名")
        return {
            title = "文化赞助", desc = m.name .. "提议以家族之力资助落魄文人、刊刻经典书籍、举办诗会文会。此举虽不能直接增加财富，却能让家族文名远播，吸引贤才归附。",
            choices = {
                { text = "大力资助，刊印典籍举办文会", effect = function()
                    GameData.AddResource("silver", -35)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("族中刊印典籍数百卷，文会盛况空前，天下士子交口称赞")
                end },
                { text = "适度资助几位有才之士", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("fame", 12)
                    GameData.AddLog("族中资助数位才子，他们感恩戴德，以诗文颂扬家族")
                end },
                { text = "暂不资助，银钱另有要用", effect = function()
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("文人求助被婉拒，有人写诗暗讽我族有钱无文")
                end },
            }
        }
    end,
}

-- 12. 继承危机
events[#events + 1] = {
    id = "r5_succession_crisis",
    title = "继承危机",
    rankRange = {5, 6},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        return #adults >= 3
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中因家主继承之事争执不休，各房暗中较劲"
        GameData.AddLog("继承之争浮出水面，宗族内部暗潮汹涌")
        return {
            title = "继承危机", desc = "老族长年事已高，继承人之争日趋激烈。嫡长房主张立嫡以长，二房之子才干出众亦有人支持，三房则搬出族规要求公选。若处理不当，宗族将面临分裂危局。",
            choices = {
                { text = "尊嫡长之制，立嫡房长子", effect = function()
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("嫡长继位，虽有人不服，但宗法大义无人敢公然违抗")
                end },
                { text = "不拘嫡庶，择贤能者继之", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("贤能者继任族长，旧制虽破，但族务井然有序")
                end },
                { text = "召开族会公议，投票决定", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("族会争吵数日方才定下人选，各房心中仍有芥蒂")
                end },
            }
        }
    end,
}

-- 13. 海外通商
events[#events + 1] = {
    id = "r5_foreign_trade",
    title = "海外通商",
    rankRange = {5, 6},
    weight = 6,
    cooldownMonths = 10,
    identityMatch = {"merchant"},
    era = {1368, 1550},
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local merchants = GameData.GetMembersByState("经商")
        return #merchants >= 1
    end,
    execute = function(s, report)
        local merchants = GameData.GetMembersByState("经商")
        local m = merchants[1]
        report.events[#report.events + 1] = m.name .. "谋划海上通商，遣船出海贸易"
        GameData.AddLog(m.name .. "筹备海上贸易，打造商船准备出海")
        return {
            title = "海外通商", desc = "海禁虽严，然南洋贸易利润丰厚。" .. m.name .. "暗中打造商船，欲走海路将丝绸瓷器贩往南洋诸国，换取香料珍宝。此举利润巨大但风险亦高，若被朝廷查获，恐获重罪。",
            choices = {
                { text = "冒险出海，搏一搏大富贵", effect = function()
                    GameData.AddResource("silver", 50)
                    GameData.AddResource("cloth", -20)
                    GameData.AddResource("fame", -10)
                    GameData.AddLog(m.name .. "商船满载而归，金银香料堆满库房，然走私之名暗中流传")
                end },
                { text = "贿赂官员取得出海许可", effect = function()
                    GameData.AddResource("silver", 25)
                    GameData.AddResource("cloth", -10)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog(m.name .. "以合法名义出海，利润虽薄但无后顾之忧")
                end },
                { text = "放弃海贸，专注内陆生意", effect = function()
                    GameData.AddResource("silver", 10)
                    GameData.AddLog(m.name .. "转而深耕内陆市场，虽无暴利但稳妥安心")
                end },
            }
        }
    end,
}

-- 14. 天灾连绵
events[#events + 1] = {
    id = "r5_natural_disaster",
    title = "天灾连绵",
    rankRange = {5, 6},
    weight = 6,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s)
        return true
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "天降大灾，旱涝交替，蝗虫过境，庄稼颗粒无收"
        GameData.AddLog("天灾降临，田地荒芜，族中粮储告急")
        return {
            title = "天灾连绵", desc = "先是春旱三月不雨，继而夏涝洪水泛滥，入秋又遭蝗灾侵袭。田中庄稼颗粒无收，佃户流离失所，族中粮仓也日渐空虚。百姓嗷嗷待哺，官府救济杯水车薪。",
            choices = {
                { text = "开仓赈灾，收容灾民", effect = function()
                    GameData.AddResource("grain", -50)
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("族中开仓放粮救济灾民数千，声望大振然粮储近空")
                end },
                { text = "仅救济本族佃户", effect = function()
                    GameData.AddResource("grain", -25)
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("本族佃户得以保全，外来灾民则被拒之门外")
                end },
                { text = "趁灾低价收购田产", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("grain", -15)
                    GameData.AddResource("fame", -20)
                    GameData.AddLog("族中趁灾兼并大量田产，然趁火打劫之名不胫而走")
                end },
            }
        }
    end,
}

-- 15. 皇家联姻
events[#events + 1] = {
    id = "r5_royal_wedding",
    title = "皇家联姻",
    rankRange = {5, 6},
    weight = 5,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        for _, m in ipairs(adults) do
            if m.spouseId == nil or m.spouseId == 0 then
                return true
            end
        end
        return #adults >= 2
    end,
    execute = function(s, report)
        -- 找到适龄未婚族人
        local candidate = nil
        local adults = GameData.GetAdultMembers()
        for _, m in ipairs(adults) do
            if not m.spouseId and m.alive then candidate = m; break end
        end
        local name = candidate and candidate.name or "族人"
        report.events[#report.events + 1] = "皇室遣使提亲，欲与" .. name .. "联姻结好"
        GameData.AddLog("天家遣媒来访，为" .. name .. "提亲")
        return {
            title = "皇家联姻", desc = "皇室一位郡主/宗室子弟看中" .. name .. "，遣使前来提亲。与皇家结亲固然是莫大荣耀。是否为" .. name .. "前去相看？",
            choices = {
                { text = "前去相看", effect = function()
                    if candidate and not candidate.spouseId then
                        local GS = require("UI.GameScreen")
                        GS.ShowMarriageTierSelect(candidate)
                    end
                end },
                { text = "婉拒皇恩，不愿受天家约束", effect = function()
                    GameData.AddResource("fame", -10)
                    GameData.AddLog("婉拒皇家联姻，虽失此良机但保住了行事自由")
                end },
            }
        }
    end,
}

-- 16. 乱世抉择
events[#events + 1] = {
    id = "r5_rebellion_choice",
    title = "乱世抉择",
    rankRange = {5, 6},
    weight = 7,
    cooldownMonths = 12,
    identityMatch = nil,
    era = {1630, 1644},
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return true
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "天下大乱，群雄并起，各方势力纷纷拉拢世家大族"
        GameData.AddLog("乱世降临，族中面临艰难的站队抉择")
        return {
            title = "乱世抉择", desc = "朝廷风雨飘摇，各方势力割据一方。有人劝我族效忠朝廷以全忠义之名，有人建议投靠新兴势力以图存续，还有人主张据守一方静观其变。一步走错，便是灭族之祸。",
            choices = {
                { text = "效忠朝廷，与国同休", effect = function()
                    GameData.AddResource("silver", -25)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("族中矢志效忠朝廷，忠义之名传遍天下，然前途未卜")
                end },
                { text = "审时度势，暗中接触新势力", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("族中暗中两面下注，虽非光明磊落却也留了后路")
                end },
                { text = "闭门自守，不介入纷争", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("grain", -20)
                    GameData.AddLog("族中闭门不出，加固防御静待天下大定")
                end },
            }
        }
    end,
}

-- 17. 发现宝藏
events[#events + 1] = {
    id = "r5_treasure_found",
    title = "发现宝藏",
    rankRange = {5, 6},
    weight = 5,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return true
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族人修缮祖宅时意外发现前朝窖藏，金银器物价值连城"
        GameData.AddLog("祖宅地下发现神秘窖藏，族中议论纷纷")
        return {
            title = "发现宝藏", desc = "修缮祖宅时工匠挖出一处前朝窖藏，内有金银器皿、古玩字画数箱，价值不可估量。消息一旦走漏，恐引来觊觎之人。如何处置这批宝藏，族中众说纷纭。",
            choices = {
                { text = "秘密收入族库，充作家底", effect = function()
                    GameData.AddResource("silver", 45)
                    GameData.AddLog("宝藏秘密入库，族中财力大增，此事知者甚少")
                end },
                { text = "上报官府，以示忠厚", effect = function()
                    GameData.AddResource("silver", 15)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("宝藏上交官府，朝廷赏赐一部分并嘉奖我族忠厚")
                end },
                { text = "取一部分自用，余下捐给寺庙", effect = function()
                    GameData.AddResource("silver", 25)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("宝藏分作两份，既充了家底又积了善德")
                end },
            }
        }
    end,
}

-- 18. 赋税改革
events[#events + 1] = {
    id = "r5_tax_reform",
    title = "赋税改革",
    rankRange = {5, 6},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = nil,
    era = {1570, 1600},
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return true
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "朝廷推行赋税新法，一条鞭法将各项赋役合而为一"
        GameData.AddLog("赋税改革波及我族，田产多者首当其冲")
        return {
            title = "赋税改革", desc = "朝廷推行一条鞭法，将田赋、徭役、各项杂税合而为一，统一以银两缴纳。新法之下，田产越多赋税越重。我族田产广袤，首当其冲。族中须决定如何应对这场变革。",
            choices = {
                { text = "积极配合新法，如实申报田产", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("如实纳税虽多费银两，却赢得官府信任，日后多有便利")
                end },
                { text = "将部分田产挂靠在佃户名下避税", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", -10)
                    GameData.AddLog("避税之计虽省了银两，但被人告发的隐患始终存在")
                end },
                { text = "趁机出售多余田产，转投商业", effect = function()
                    GameData.AddResource("silver", 20)
                    GameData.AddResource("grain", -30)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("出售田产转投商业，虽减了粮产但银钱更加灵活")
                end },
            }
        }
    end,
}

-- 19. 族中分裂
events[#events + 1] = {
    id = "r5_clan_split",
    title = "族中分裂",
    rankRange = {5, 6},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local members = GameData.GetAliveMembers()
        return #members >= 5
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中大房与二房积怨已久，矛盾终于公开化"
        GameData.AddLog("宗族内部出现严重分歧，分裂危机一触即发")
        return {
            title = "族中分裂", desc = "大房与二房因田产分配、子弟教育、婚姻结亲等事积怨日深。二房族人扬言要分宗另立，带走应得的田产家业。若当真分裂，百年基业将一分为二，两败俱伤。",
            choices = {
                { text = "召集族老调解，重新分配利益", effect = function()
                    GameData.AddResource("silver", -25)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("族老居中调解，虽割让部分利益但保住了宗族完整")
                end },
                { text = "以宗法族规弹压，不许分宗", effect = function()
                    GameData.AddResource("fame", -10)
                    GameData.AddLog("强行弹压分裂之议，二房虽暂时服从但心中怨恨更深")
                end },
                { text = "同意分宗，和平分家", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("grain", -20)
                    GameData.AddResource("cloth", -10)
                    GameData.AddResource("fame", -15)
                    GameData.AddLog("宗族正式分裂，百年一体之族就此各走各路")
                end },
            }
        }
    end,
}

-- 20. 沙场扬威
events[#events + 1] = {
    id = "r5_military_glory",
    title = "沙场扬威",
    rankRange = {5, 6},
    weight = 6,
    cooldownMonths = 8,
    identityMatch = {"warrior"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local warriors = GameData.GetMembersByState("从军")
        return #warriors >= 1
    end,
    execute = function(s, report)
        local warriors = GameData.GetMembersByState("从军")
        local m = warriors[1]
        report.events[#report.events + 1] = m.name .. "统兵征战，连战连捷，威名远播"
        GameData.AddLog(m.name .. "在战场上屡立奇功，名震天下")
        return {
            title = "沙场扬威", desc = m.name .. "奉命征讨叛乱，凭借出色的军事才能，以少胜多连克数城。捷报频传，朝廷龙颜大悦，满朝文武皆知我族出了一员虎将。",
            choices = {
                { text = "乘胜追击，争取更大战功", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("fame", 30)
                    GameData.AddLog(m.name .. "再立新功，封侯拜将，族中武名冠绝一方")
                end },
                { text = "见好就收，上表请求还乡", effect = function()
                    GameData.AddResource("silver", 20)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog(m.name .. "功成身退携赏银而归，族人夹道欢迎")
                end },
                { text = "趁势举荐族中子弟入伍", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog(m.name .. "举荐族中子弟从军，武将之家的名声渐渐打响")
                end },
            }
        }
    end,
}

-- 21. 大规模瘟疫
events[#events + 1] = {
    id = "r5_epidemic",
    title = "大规模瘟疫",
    rankRange = {5, 6},
    weight = 5,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s)
        return true
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "瘟疫席卷数省之地，死者枕藉，十室九空"
        GameData.AddLog("大疫降临，族中人心惶惶")
        return {
            title = "大规模瘟疫", desc = "一场来势汹汹的瘟疫从南方蔓延而至，沿途村镇十室九空。我族所在之地亦未能幸免，已有族人出现症状。瘟疫面前，人命脆弱如薄纸，须当机立断方能保全更多族人。",
            choices = {
                { text = "重金延请名医，购药救治族人", effect = function()
                    GameData.AddResource("silver", -40)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("名医坐镇施方用药，族中感染者大半得救，然银钱耗费巨大")
                end },
                { text = "隔离病患，封锁村寨防扩散", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("grain", -20)
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("严格隔离虽控制了疫情，但被隔离者中不少人未能熬过")
                end },
                { text = "散尽药材救治周边百姓", effect = function()
                    GameData.AddResource("silver", -35)
                    GameData.AddResource("grain", -25)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("族中散财救济四方，大疫过后百姓感恩，立碑以记")
                end },
            }
        }
    end,
}

-- 22. 藏书阁建设
events[#events + 1] = {
    id = "r5_book_collection",
    title = "藏书阁建设",
    rankRange = {5, 6},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        return true
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中计划兴建藏书阁，收藏天下典籍经卷"
        GameData.AddLog("藏书阁选址动工，族人四处搜罗珍本善册")
        return {
            title = "藏书阁建设", desc = "族中有识之士提议修建藏书阁，广搜天下孤本善册、经史子集，使之成为一方文脉重镇。此举不仅能惠及子孙后代，更能吸引天下读书人慕名而来，提升家族在文坛的地位。",
            choices = {
                { text = "倾力打造，建成一方藏书胜地", effect = function()
                    GameData.AddResource("silver", -35)
                    GameData.AddResource("cloth", -10)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("藏书阁落成，收藏典籍万余卷，文人学子络绎不绝")
                end },
                { text = "适度建设，先收集本地典籍", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("fame", 12)
                    GameData.AddLog("藏书阁初具规模，虽藏书不多但已有文人前来借阅")
                end },
                { text = "暂缓建设，先抄录族中现有藏书", effect = function()
                    GameData.AddResource("silver", -5)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("族中现有藏书整理抄录完毕，待日后再行扩建")
                end },
            }
        }
    end,
}

-- 23. 粮食专营
events[#events + 1] = {
    id = "r5_grain_monopoly",
    title = "粮食专营",
    rankRange = {5, 6},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local farmers = GameData.GetMembersByState("务农")
        local merchants = GameData.GetMembersByState("经商")
        return #farmers >= 1 or #merchants >= 1
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中凭借广袤田产，欲掌控本地粮食贸易"
        GameData.AddLog("控制粮市的计划在族中秘密酝酿")
        return {
            title = "粮食专营", desc = "我族田产遍布数县，每年产粮足以供养数万人。有人建议利用这一优势，收购周边余粮统一售卖，掌控本地粮价。此举利润丰厚，但也会招致小民怨恨和官府关注。",
            choices = {
                { text = "大举收购，垄断本地粮市", effect = function()
                    GameData.AddResource("silver", 40)
                    GameData.AddResource("grain", 30)
                    GameData.AddResource("fame", -15)
                    GameData.AddLog("族中掌控粮市获利颇丰，但民间怨声载道")
                end },
                { text = "适度经营，保持合理粮价", effect = function()
                    GameData.AddResource("silver", 20)
                    GameData.AddResource("grain", 15)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("族中经营粮行童叟无欺，既有利润又不失民心")
                end },
                { text = "平价售粮，荒年减价以惠百姓", effect = function()
                    GameData.AddResource("silver", 5)
                    GameData.AddResource("grain", -10)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("族中平价售粮深得民心，百姓称颂我族仁义")
                end },
            }
        }
    end,
}

-- 24. 宫廷阴谋
events[#events + 1] = {
    id = "r5_palace_intrigue",
    title = "宫廷阴谋",
    rankRange = {5, 6},
    weight = 6,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        return #adults >= 2
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "宫中传来密信，我族被卷入一场宫廷权力争斗"
        GameData.AddLog("宫廷密信至，族中上下如临深渊")
        return {
            title = "宫廷阴谋", desc = "宫中太监携密信到访，称有人欲构陷我族谋反。幕后之人是朝中政敌，意图借此扳倒我族在朝中的势力。此事若不妥善应对，轻则抄家，重则灭族。",
            choices = {
                { text = "进京面圣自辩清白", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("面圣自辩终洗清冤屈，然这番折腾耗费甚巨")
                end },
                { text = "暗中搜集政敌把柄反击", effect = function()
                    GameData.AddResource("silver", -25)
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("搜得政敌贪赃枉法之证据，以其人之道还治其人之身")
                end },
                { text = "主动示弱，辞去朝中官职避祸", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", -15)
                    GameData.AddLog("辞官退隐暂避锋芒，政敌见势已去便不再追究")
                end },
            }
        }
    end,
}

-- 25. 百年基业
events[#events + 1] = {
    id = "r5_legacy_project",
    title = "百年基业",
    rankRange = {5, 6},
    weight = 6,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local members = GameData.GetAliveMembers()
        return #members >= 5
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中长辈提议启动一项传世工程，为子孙后代奠基百年"
        GameData.AddLog("百年基业之议在宗祠中庄严提出")
        return {
            title = "百年基业", desc = "老族长召集各房长辈于宗祠会议，慨然道：'我族历数代经营方有今日，然百年之后何以为继？'遂提议启动一项传世工程——或修建大型水利泽被后世，或编纂族谱家训传承文脉，或广置义田养族中孤寡。",
            choices = {
                { text = "修建水利工程，造福乡里百年", effect = function()
                    GameData.AddResource("silver", -40)
                    GameData.AddResource("grain", 30)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("水利大工落成，灌溉良田万亩，乡人世代受益，立碑颂德")
                end },
                { text = "编纂族谱家训，传承精神文脉", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("cloth", -5)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("族谱家训编纂完毕，洋洋数万言，字字皆先人心血")
                end },
                { text = "广置义田，赡养族中鳏寡孤独", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("grain", -15)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("义田购置完毕，族中困苦之人皆有所养，人心归附")
                end },
            }
        }
    end,
}

return events
