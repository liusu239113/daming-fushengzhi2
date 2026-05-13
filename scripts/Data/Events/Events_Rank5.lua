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
                { text = "支持改革派，锐意进取", cost = {silver = 25}, result = "族长亲赴京师，于朝堂之上慷慨陈词力挺新政。改革派得我族银钱襄助与奔走呼号，声势大振。数月之后新政渐行，朝中大臣皆知我族立场坚定，纷纷刮目相看，族中声望因此大涨。", effect = function()
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("族中力挺改革派，声望大振，然花费银两打点关系")
                end },
                { text = "支持保守派，稳中求进", cost = {silver = 10}, result = "族中遣人暗中拜会保守派重臣，奉上心意以表归附。保守派势力根深蒂固，我族依附其下虽无赫赫之功，却也免去了站错队的风险。朝堂风波过后，族中安然无恙，稳中有进。", effect = function()
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("族中依附保守派，虽无大功，亦保平安")
                end },
                { text = "两不相帮，韬光养晦", result = "族中闭门谢客，对朝中两派使者皆以身体抱恙推辞。然两不相帮便是两面得罪，朝中有人冷笑道我族首鼠两端、不堪大用。一时间门庭冷落，旧日交好之人亦渐渐疏远。", effect = function()
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
                { text = "高悬匾额于祠堂，珍宝入库收藏", result = "御笔亲题之匾额择吉日高悬于宗祠正堂，金光灿灿令人肃然起敬。珍宝尽入族库妥善珍藏，族人每逢祭祖便仰望御匾，满怀荣耀。四邻乡绅闻讯前来拜贺，皆叹我族天恩浩荡、光宗耀祖。", effect = function()
                    GameData.AddResource("silver", 40)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("御赐匾额高悬宗祠，珍宝入库，阖族引以为荣")
                end },
                { text = "将部分赏赐分予族中贫苦子弟", result = "族长召集各房子弟于宗祠前，将御赐珍宝分润族中贫寒之家。老弱妇孺感恩涕零，青壮子弟更添报效之心。虽金银所得不及独占，然族中上下一心归附，凝聚之力远胜金银。", effect = function()
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
                { text = "斥巨资兴建，延聘当世大儒", cost = {silver = 40}, result = "择风水宝地建起三进院落的族学，青砖黛瓦、松柏环绕。重金礼聘当世大儒坐镇讲席，经史子集、六艺之学一应俱全。开学之日，远近子弟慕名而来，书声琅琅响彻乡里，我族文教之名由此远播。", effect = function()
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("族学落成，大儒坐镇讲学，远近子弟慕名而来")
                end },
                { text = "量力而行，先设简易学堂", cost = {silver = 15}, result = "在宗祠偏院辟出数间房舍权作学堂，延请一位老秀才教授蒙童。虽是草创简陋，课桌不过几张、书卷不过百册，然清晨朗朗读书声传出院墙，族中上下皆感欣慰。", effect = function()
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("学堂初建，虽简陋却也有了读书声")
                end },
                { text = "暂缓此事，待时机成熟再议", result = "族中长辈议了数日终未决断，有人叹道子弟荒废学业实在可惜。族学之议就此搁置，几个有志于学的少年只得自行借书苦读，不禁令人唏嘘。", effect = function()
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
                { text = "大举扩张，垄断数省商路", result = "商号如雨后春笋遍布江南数省，丝绸行、茶庄、瓷器铺连成一片。日进斗金之势令同行侧目，商路之上无人不知我族字号。然树大招风，有人暗中向官府参奏其垄断之嫌，隐患渐生。", effect = function()
                    GameData.AddResource("silver", 50)
                    GameData.AddResource("fame", -10)
                    GameData.AddLog(m.name .. "大举扩张商号，日进斗金，然商名太盛引人侧目")
                end },
                { text = "稳健经营，同时向朝廷纳捐", result = "商号稳步扩张之余，特向朝廷纳捐银两以示忠心。银钱入了国库，朝中亦有人为我族美言。如此一来商誉日隆而官声亦佳，进退有据不失分寸，实为经商之上策。", effect = function()
                    GameData.AddResource("silver", 30)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog(m.name .. "稳健经营并向朝廷纳捐，商誉与官声兼得")
                end },
                { text = "分散经营，化整为零以避嫌", result = "将旗下商号拆分为数家，分由族中子弟各自经营，表面上已无垄断之嫌。虽利润因此摊薄不少，但也不再引人注目。族中长辈叹道，做生意如做人，锋芒太露终非善事。", effect = function()
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
                { text = "请封世袭武职，光耀门楣", cost = {silver = 20}, result = "朝廷颁下诰命，赐予世袭武职，族中又添一位柱国之臣。授印之日，全族张灯结彩设宴庆贺。自此我族文武兼备，在朝堂上更有了分量，远近世家纷纷前来结交攀附。", effect = function()
                    GameData.AddResource("fame", 30)
                    GameData.AddLog(m.name .. "获封武职，族中又添一柱国之臣")
                end },
                { text = "请赐田产金银，充实家业", result = "朝廷论功行赏，赐下良田数百亩、黄金白银若干。运回之日车马络绎不绝，族人欢欣鼓舞。田产入册、金银入库，家业因此大为充实，来年佃租收入亦随之水涨船高。", effect = function()
                    GameData.AddResource("silver", 40)
                    GameData.AddResource("grain", 30)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog(m.name .. "之功换得大片田产与金银赏赐")
                end },
                { text = "请恩荫子弟入国子监读书", cost = {silver = 10}, result = "以军功换得恩荫名额，族中数名子弟得以入国子监就读。太学之中名师荟萃、同窗皆贵胄之后，日后科举仕途皆多了一条捷径。族人感慨，一将功成不仅护了家国，更为后辈铺就了青云之路。", effect = function()
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
                { text = "组织乡勇坚守，誓与家园共存亡", result = "族长登高一呼，族中青壮奋勇响应，组成乡勇数百人据坞而守。起义军数度攻来皆被击退，箭矢如雨、刀光剑影间族人伤亡不少。苦战月余终于等来官军增援，家园虽保住了，银粮却耗去大半。", effect = function()
                    GameData.AddResource("silver", -35)
                    GameData.AddResource("grain", -40)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("族中组织乡勇拼死抵抗，虽损失惨重但保住了根基")
                end },
                { text = "开仓放粮安抚流民，化干戈为玉帛", result = "族长下令大开粮仓，于庄前设粥棚施济流民。饥民得食感恩涕零，起义军首领闻我族义举亦深受感动，传令绕道而行不犯此地。然粮仓为之几近空空，来年春耕之种粮尚需另行筹措。", effect = function()
                    GameData.AddResource("grain", -50)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("族中开仓济民，流民感恩绕道而行，粮仓却近乎见底")
                end },
                { text = "举族暂避他方，保全族人性命", result = "族人连夜收拾细软仓皇南逃，车马相接、老幼扶持，狼狈不堪。待风波平息返回时，祖宅已被洗劫一空，田中庄稼也遭践踏殆尽。更有坊间闲言碎语，讥讽我族贪生怕死弃乡而逃。", effect = function()
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
                { text = "备厚礼出使，展现大国气度", cost = {silver = 30, cloth = 15}, result = "车载珍玩丝帛浩浩荡荡出关，沿途旌旗猎猎好不威风。至邻邦王庭献上厚礼，对方君臣叹服天朝气度，通商互市之约一拍即合。捷报传回朝廷，天子龙颜大悦，我族声望更上层楼。", effect = function()
                    GameData.AddResource("fame", 25)
                    GameData.AddLog(m.name .. "携重礼出使成功，邦交议成，朝廷大悦")
                end },
                { text = "轻车简从，以才学折服对方", result = "仅带三五随从便策马西行，至邻邦王庭以三寸之舌折冲樽俎。引经据典、纵横捭阖，对方君臣无不折服。虽无厚礼却以才学赢得敬重，邦交事宜亦圆满议成，不辱使命而归。", effect = function()
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
                { text = "大义灭亲，严惩不贷以正家风", result = "族长面色铁青于宗祠前当众宣布处罚——杖责三十、退还所占田产、禁足三年不得外出。不肖子弟受刑时哭喊求饶，族长不为所动。此事传出后世人皆叹我族家风严正，声誉反因此有所挽回。", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("族长当众惩处不肖子弟，退还田产，世人称赞家风严正")
                end },
                { text = "私下调解，赔偿了事息事宁人", cost = {silver = 30}, result = "族中遣人持银钱登门赔罪，受害之家收了赔偿后撤了状子。衙门不再过问此事，表面上风波已平。然坊间仍有闲言碎语传来，说我族仗势欺人、以钱堵嘴，声誉终究受了些损。", effect = function()
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("族中花重金平息此事，然坊间仍有闲言碎语")
                end },
                { text = "动用关系压下此事，官府不再追究", cost = {silver = 20}, result = "族中动用在官府的关系，知县碍于情面将此案草草了结。状纸虽被压下，但受害之家含冤无处诉说，民间怨气暗中积聚。有正直之士撰文讥讽我族以势压人，名声因此大损。", effect = function()
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
                { text = "倾力营建，修筑气派庄园", cost = {silver = 40, cloth = 10}, result = "请来苏杭名匠精心设计，历时半载终成一座气派庄园。亭台楼阁错落有致，假山流水曲径通幽，花木扶疏四时有景。落成之日远近乡绅皆来道贺，赞叹此园不逊于京城王府之胜。", effect = function()
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("庄园落成，亭台楼阁错落有致，远近闻名")
                end },
                { text = "适度修缮，重点修祠堂与书房", cost = {silver = 20}, result = "将有限银钱用在刀刃上——宗祠翻新粉饰、牌位重新安放、书房扩建添架。修缮过后祠堂庄严肃穆、书房窗明几净，族人祭祖读书皆有体面之所，颇感欣慰。", effect = function()
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("祠堂与书房焕然一新，族人颇感欣慰")
                end },
                { text = "暂不动工，银钱留作他用", result = "修缮之议在长辈们的争论中不了了之，银钱留作他用。然祖宅日渐颓败，墙皮剥落、梁柱生虫，有远客来访见此景象不禁唏嘘叹息，暗道这等世家何以如此破落。", effect = function()
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
                { text = "组织族人协助官军抗敌", result = "族中子弟披甲执锐，与官军并肩迎敌于城下。鏖战数日，血染城墙，敌骑终于退去。战后点检伤亡，族中折损子弟数人，银粮亦耗费甚巨。然朝廷嘉其忠勇，传旨褒奖，我族忠义之名响彻四方。", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("grain", -35)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("族中子弟浴血奋战，与官军并肩击退敌军，伤亡不小")
                end },
                { text = "加固坞堡固守，坚壁清野", result = "族人紧急加固坞堡城垣，将周边粮草牲畜尽数收入堡中坚壁清野。敌骑数度来犯见城高池深便不再强攻，转往他处劫掠。堡内虽保住了性命，然堡外田产庄稼遭铁蹄践踏损失惨重。", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("grain", -25)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("坞堡固若金汤，敌军劫掠一番后退去，田产损失惨重")
                end },
                { text = "携细软南迁避祸", result = "族人连夜车载箱笼仓皇南逃，行至途中又遭散兵劫掠，丢失财物无数。待敌退再归时，祖宅已成断壁残垣，田产尽被焚毁。更令人痛心的是声望一落千丈，世人皆道我族临难而逃不堪倚仗。", effect = function()
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
                { text = "大力资助，刊印典籍举办文会", cost = {silver = 35}, result = "族中出资刊印经史典籍数百卷，广散天下读书人。又于园中设宴举办文会，诗酒唱和三日不绝。天下士子闻风而至，交口称赞我族文脉深厚、礼贤下士，文名由此远播四方。", effect = function()
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("族中刊印典籍数百卷，文会盛况空前，天下士子交口称赞")
                end },
                { text = "适度资助几位有才之士", cost = {silver = 15}, result = "族中择其才学出众者数人予以资助，供其衣食笔墨使其安心治学。这几位才子感恩戴德，以诗文颂扬我族仁德，篇篇传诵于坊间。虽花费不多，却也博得了爱才惜才之美名。", effect = function()
                    GameData.AddResource("fame", 12)
                    GameData.AddLog("族中资助数位才子，他们感恩戴德，以诗文颂扬家族")
                end },
                { text = "暂不资助，银钱另有要用", result = "文人登门求助被族长以银钱紧张为由婉拒。有落魄才子愤而离去，写下数首辛辣诗文暗讽我族坐拥金山却不识风雅，有钱而无文。一时间坊间传唱，颇损文名。", effect = function()
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
                { text = "尊嫡长之制，立嫡房长子", result = "族中遵循宗法古制，于宗祠内焚香告祖，正式册立嫡房长子为新任族长。虽有二房三房暗中不服，然嫡长承继乃天经地义，无人敢公然违抗。新族长入主正堂，族务交接井然有序。", effect = function()
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("嫡长继位，虽有人不服，但宗法大义无人敢公然违抗")
                end },
                { text = "不拘嫡庶，择贤能者继之", cost = {silver = 10}, result = "族长力排众议，不拘嫡庶择贤而立。新族长虽非嫡出却才干过人，上任后整顿族务、清理积弊，族中上下焕然一新。旧制虽破，但族人见新族长治事有方亦渐渐心服。", effect = function()
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("贤能者继任族长，旧制虽破，但族务井然有序")
                end },
                { text = "召开族会公议，投票决定", cost = {silver = 15}, result = "族会于宗祠中召开，各房争相为自家人选造势拉票。连议三日唇枪舌剑互不相让，最终勉强票选出新族长。然落选各房心中不忿，芥蒂暗生，族中和睦之气因此受损不少。", effect = function()
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
                { text = "冒险出海，搏一搏大富贵", cost = {cloth = 20}, result = "商船满载丝绸瓷器趁夜色出港，一路避开巡海官兵。历经风浪颠簸数月后抵达南洋，货物被当地番商抢购一空，换回大量金银、香料、珍珠。然走私之名暗中流传，日后若被追查恐有后患。", effect = function()
                    GameData.AddResource("silver", 50)
                    GameData.AddResource("fame", -10)
                    GameData.AddLog(m.name .. "商船满载而归，金银香料堆满库房，然走私之名暗中流传")
                end },
                { text = "贿赂官员取得出海许可", cost = {cloth = 10}, result = "暗中打点市舶司官员，以丝帛相赠换得一纸出海文牒。商船挂着合法旗号堂而皇之出港，虽因打点花去不少本钱利润摊薄，但一路无虞往返顺遂。合法贸易之名亦为日后的长远经营奠了基。", effect = function()
                    GameData.AddResource("silver", 25)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog(m.name .. "以合法名义出海，利润虽薄但无后顾之忧")
                end },
                { text = "放弃海贸，专注内陆生意", result = "权衡再三终究不敢冒灭族之险，将打造好的商船转卖他人。转而深耕内陆商路，于苏杭一带经营丝绸茶叶生意。虽无海贸之暴利，但胜在稳妥安心，夜夜安枕无忧。", effect = function()
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
                { text = "开仓赈灾，收容灾民", result = "族长下令尽开粮仓，于庄前搭起十余座粥棚日夜施粥。饥民闻讯从四面八方涌来，数千人得以活命。粮仓虽为之几近见底、银钱亦耗费甚多，然我族仁义之名传遍数县，百姓无不感恩戴德。", effect = function()
                    GameData.AddResource("grain", -50)
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("族中开仓放粮救济灾民数千，声望大振然粮储近空")
                end },
                { text = "仅救济本族佃户", result = "族中量力而行，将有限粮食分发给本族佃户及其家眷，保其不至饿死。庄外灾民来求却被拒之门外，有人跪地哀求亦不得入。佃户虽感念恩德，然外人议论我族只顾自家不顾苍生。", effect = function()
                    GameData.AddResource("grain", -25)
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("本族佃户得以保全，外来灾民则被拒之门外")
                end },
                { text = "趁灾低价收购田产", result = "族中遣人四处低价收购灾民急售的田产，趁天灾之际大肆兼并。数月之间田产增了数倍，然百姓怨恨之声不绝于耳。有正直士人撰文痛斥趁火打劫之行径，我族名声因此蒙上一层阴影。", effect = function()
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
    weight = 8,
    cooldownMonths = 8,
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
                { text = "前去相看", result = "族长遣人备下厚礼，携适龄子弟前往京师赴约相看。皇家庭院深深，礼仪繁复，两家子弟隔帘相见，彼此颇为满意。此番若能结成秦晋之好，我族便与天家攀上了姻亲，荣耀非凡。", effect = function()
                    if candidate and not candidate.spouseId then
                        local GS = require("UI.GameScreen")
                        GS.ShowMarriageTierSelect(candidate)
                    end
                end },
                { text = "婉拒皇恩，不愿受天家约束", result = "族长思虑再三，终以族中子弟尚年幼、不堪匹配天家为由婉拒了这桩亲事。皇室使者面露不悦拂袖而去，朝中亦有人暗中非议。然族中长辈松了口气，毕竟与天家结亲便如笼中之鸟，行事处处受制。", effect = function()
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
                { text = "效忠朝廷，与国同休", cost = {silver = 25}, result = "族长率子弟于宗祠前焚香盟誓，矢志效忠大明社稷。倾族中银钱捐作军饷助朝廷平乱，忠义之名传遍天下。然朝廷风雨飘摇、胜负未分，前途吉凶难卜，族人心中忐忑不安。", effect = function()
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("族中矢志效忠朝廷，忠义之名传遍天下，然前途未卜")
                end },
                { text = "审时度势，暗中接触新势力", cost = {silver = 15}, result = "族中暗遣心腹分赴两方，一边维持朝廷关系，一边秘密接触新兴势力探其底细。如此两面下注虽非光明磊落之举，却也为族中留下了退路。不论哪方得势，我族皆有说辞应对。", effect = function()
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("族中暗中两面下注，虽非光明磊落却也留了后路")
                end },
                { text = "闭门自守，不介入纷争", result = "族长下令紧闭庄门，加固坞堡、储备粮草兵器，不与任何一方往来。乱兵过境时见我族庄园守卫森严便绕道而去，虽损失了些田中庄稼与外出的银钱，但族人性命无虞，静待天下尘埃落定。", effect = function()
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
                { text = "秘密收入族库，充作家底", result = "族长连夜召集心腹将窖藏金银器物悉数搬入密室，知情者不过三五人而已。工匠亦被留在庄中看管月余方才放归，嘱其守口如瓶。自此族库充盈、底气倍增，然这批来路不明的财宝始终是个隐患。", effect = function()
                    GameData.AddResource("silver", 45)
                    GameData.AddLog("宝藏秘密入库，族中财力大增，此事知者甚少")
                end },
                { text = "上报官府，以示忠厚", result = "族长将窖藏造册详列后呈报县衙。知县大悦上报朝廷，朝廷嘉奖我族拾遗不昧，赏回部分器物并颁下嘉奖文书。虽失去了大半宝藏，然忠厚之名远近皆知，官府日后亦多有照拂。", effect = function()
                    GameData.AddResource("silver", 15)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("宝藏上交官府，朝廷赏赐一部分并嘉奖我族忠厚")
                end },
                { text = "取一部分自用，余下捐给寺庙", result = "族中取了金银器物中的一半充入库房，余下古玩字画尽数捐给附近宝刹供佛。寺中住持感恩为我族祈福诵经七日，坊间也传出我族乐善好施的美名。既充了家底又积了善德，可谓两全其美。", effect = function()
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
                { text = "积极配合新法，如实申报田产", result = "族长命账房将名下田产逐亩登记造册，如实呈报官府缴纳赋银。一笔银两交出去虽令人肉痛，然知县赞我族深明大义乃地方表率，日后凡有公事皆优先关照，官民之间更添信任。", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("如实纳税虽多费银两，却赢得官府信任，日后多有便利")
                end },
                { text = "将部分田产挂靠在佃户名下避税", result = "族中暗将数百亩田产挂在佃户名下以避重税。表面上族中田产骤减，实则收益分文未少。此计虽省了不少银两，然知情佃户日后若生异心或被人告发，便是欺瞒朝廷的大罪。", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", -10)
                    GameData.AddLog("避税之计虽省了银两，但被人告发的隐患始终存在")
                end },
                { text = "趁机出售多余田产，转投商业", result = "借新法施行之际将偏远贫瘠之田尽数出售，所得银钱转投丝绸与茶叶生意。虽然来年粮产锐减需从外地购粮，但商业收益远超田租。族中经营方略由此一变，从耕读之家渐向商贾转型。", effect = function()
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
                { text = "召集族老调解，重新分配利益", cost = {silver = 25}, result = "族中德高望重的几位老者连日居中斡旋，苦口婆心劝说两房各退一步。最终重新划分了田产收益、子弟名额等利益分配，二房虽仍有不满但总算消了分宗之念。宗族完整得保，然这笔调解银钱着实花了不少。", effect = function()
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("族老居中调解，虽割让部分利益但保住了宗族完整")
                end },
                { text = "以宗法族规弹压，不许分宗", result = "族长搬出祖宗家法，在宗祠前厉声训斥分宗之议乃大逆不道。二房众人在族规压力下不敢再言，暂时偃旗息鼓。然表面上的服从掩盖不住心中怨恨，暗地里的裂痕反而越来越深。", effect = function()
                    GameData.AddResource("fame", -10)
                    GameData.AddLog("强行弹压分裂之议，二房虽暂时服从但心中怨恨更深")
                end },
                { text = "同意分宗，和平分家", cost = {silver = 20, grain = 20, cloth = 10}, result = "族长长叹一声终于点头同意分宗。请来官府作保，将田产银粮布匹一一清点分割。分家之日，两房各立宗祠各奉牌位，百年一体之族就此分道扬镳。族人相对无言，唯有唏嘘。", effect = function()
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
                { text = "乘胜追击，争取更大战功", cost = {silver = 15}, result = "趁敌军溃退之际挥师追击，连克数城直捣贼巢。凯旋之日朝廷颁下封赏——封侯拜将、赐金百两。族中武名一时冠绝四方，世人皆道我族文能安邦、武能定国，实乃当世望族。", effect = function()
                    GameData.AddResource("fame", 30)
                    GameData.AddLog(m.name .. "再立新功，封侯拜将，族中武名冠绝一方")
                end },
                { text = "见好就收，上表请求还乡", result = "功成之后急流勇退，上表天子请求解甲还乡。朝廷感其忠勇赐下丰厚赏银，准予荣归故里。还乡之日族人夹道欢迎锣鼓喧天，老族长含泪相迎叹道知进退方为大丈夫。", effect = function()
                    GameData.AddResource("silver", 20)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog(m.name .. "功成身退携赏银而归，族人夹道欢迎")
                end },
                { text = "趁势举荐族中子弟入伍", cost = {silver = 10}, result = "趁着军中威望正盛，向上峰举荐族中数名勇武子弟入伍从军。这些后生有了前辈照拂在军中如鱼得水，数年间便有人崭露头角。武将之家的名声渐渐在军中传开，我族由此多了一条晋身之路。", effect = function()
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
                { text = "重金延请名医，购药救治族人", result = "遍寻名医终于请得一位杏林圣手入驻庄中。此医辨证施治、对症下药，族中染疫者大半得以救治。然名医诊金与药材费用耗银甚巨，族库为之大空。好在族人性命保住了大半，亦算不幸中之万幸。", effect = function()
                    GameData.AddResource("silver", -40)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("名医坐镇施方用药，族中感染者大半得救，然银钱耗费巨大")
                end },
                { text = "隔离病患，封锁村寨防扩散", result = "族长忍痛下令将染疫者集中隔离于庄外别院，村寨四门紧闭不许进出。疫情因此未能蔓延开来，然被隔离者中不少人因缺医少药未能熬过。族中虽保全了多数人，但那些逝去的族人令人扼腕叹息。", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("grain", -20)
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("严格隔离虽控制了疫情，但被隔离者中不少人未能熬过")
                end },
                { text = "散尽药材救治周边百姓", result = "族长下令将库中药材尽数拿出，于庄前设医棚义诊施药，不论本族外人一视同仁。银钱粮食流水般花去，然救活了四方百姓无数。大疫过后乡民感恩戴德，于道旁立碑铭记我族善举，千古流芳。", effect = function()
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
                { text = "倾力打造，建成一方藏书胜地", cost = {silver = 35, cloth = 10}, result = "于庄园东隅建起三层藏书阁，飞檐翘角、雕梁画栋。遣人四处搜罗孤本善册，半年间竟收藏典籍万余卷。藏书阁落成之日开门迎客，文人学子络绎不绝前来借阅抄录，我族文脉重镇之名不胫而走。", effect = function()
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("藏书阁落成，收藏典籍万余卷，文人学子络绎不绝")
                end },
                { text = "适度建设，先收集本地典籍", cost = {silver = 15}, result = "先建一座小巧藏书楼，派人收集本地乡贤遗著与县志方志。虽规模不大、藏书不过千卷，但已有附近文人闻讯前来借阅研习。藏书阁初具规模，日后再行扩充亦有了根基。", effect = function()
                    GameData.AddResource("fame", 12)
                    GameData.AddLog("藏书阁初具规模，虽藏书不多但已有文人前来借阅")
                end },
                { text = "暂缓建设，先抄录族中现有藏书", result = "藏书阁暂且不建，先令族中识字子弟将现有藏书逐一抄录备份，以防虫蛀水浸之祸。抄录数月得副本数百卷，虽花去些笔墨纸张的银钱，但族中文脉至少不会断绝，日后再建阁亦有底本可用。", effect = function()
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
                { text = "大举收购，垄断本地粮市", result = "族中遣人四处收购余粮，数月间将本地粮市尽握掌中。粮价由我族说了算，获利之丰超乎想象。然小民买粮日贵怨声载道，有人于衙门前鸣冤告状，官府亦对我族侧目而视。", effect = function()
                    GameData.AddResource("silver", 40)
                    GameData.AddResource("grain", 30)
                    GameData.AddResource("fame", -15)
                    GameData.AddLog("族中掌控粮市获利颇丰，但民间怨声载道")
                end },
                { text = "适度经营，保持合理粮价", result = "族中开设粮行明码标价、童叟无欺，既不哄抬亦不贱卖。百姓买粮有了稳定去处不再受奸商盘剥，族中也有了稳定的利润来源。如此双赢之举，既得了实惠又不失民心。", effect = function()
                    GameData.AddResource("silver", 20)
                    GameData.AddResource("grain", 15)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("族中经营粮行童叟无欺，既有利润又不失民心")
                end },
                { text = "平价售粮，荒年减价以惠百姓", result = "族长立下规矩——丰年平价、荒年减价，绝不趁灾涨价盘剥百姓。虽然利润微薄甚至有时贴补粮食，然百姓感恩戴德交口称颂，皆道我族乃仁义之家。这份民心之厚，远非金银可买。", effect = function()
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
                { text = "进京面圣自辩清白", cost = {silver = 30}, result = "族长亲赴京师跪请面圣，呈上数十年来忠心报国之凭证。天子细阅之后龙颜稍霁，着令彻查此案。数月后冤情终得昭雪，然这番奔走打点耗费银钱无数，族中元气大伤。", effect = function()
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("面圣自辩终洗清冤屈，然这番折腾耗费甚巨")
                end },
                { text = "暗中搜集政敌把柄反击", cost = {silver = 25}, result = "族中暗遣心腹四处查探，终于搜得政敌贪赃枉法、结党营私之铁证。将证据密呈御史台后，政敌反被弹劾下狱。以其人之道还治其人之身虽大快人心，然朝中同僚亦暗道我族手段狠辣。", effect = function()
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("搜得政敌贪赃枉法之证据，以其人之道还治其人之身")
                end },
                { text = "主动示弱，辞去朝中官职避祸", result = "族中在朝为官者主动上表请辞，言辞恳切自称才疏学浅不堪重任。政敌见我族已自去爪牙便不再穷追猛打，风波渐渐平息。然辞官之后门庭冷落、旧交疏远，声望亦大不如前。", effect = function()
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
                { text = "修建水利工程，造福乡里百年", cost = {silver = 40}, result = "动员数百民夫历时半年，修筑堤坝、开凿水渠、引河灌田。水利大工落成之日碧水长流灌溉良田万亩，旱时有水涝时有排，乡人世世代代受其恩惠。父老于渠首立碑颂德，铭刻我族功绩于石上永传。", effect = function()
                    GameData.AddResource("grain", 30)
                    GameData.AddResource("fame", 25)
                    GameData.AddLog("水利大工落成，灌溉良田万亩，乡人世代受益，立碑颂德")
                end },
                { text = "编纂族谱家训，传承精神文脉", result = "延请饱学之士执笔，遍访族中老人搜集先祖事迹。历时一年编成族谱家训数卷，洋洋数万言记录了列祖列宗的功业与训诫。每年祭祖之时族长当众宣读家训，子孙后代皆铭记于心，精神文脉由此代代相传。", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("cloth", -5)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("族谱家训编纂完毕，洋洋数万言，字字皆先人心血")
                end },
                { text = "广置义田，赡养族中鳏寡孤独", cost = {silver = 30, grain = 15}, result = "拨出银粮购置良田百余亩专作义田，其租息收入全部用于赡养族中鳏寡孤独之人。老有所养、幼有所教、病有所医，族中困苦之人再无冻馁之虞。人心因此大归，各房子弟皆以族中仁德为荣。", effect = function()
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("义田购置完毕，族中困苦之人皆有所养，人心归附")
                end },
            }
        }
    end,
}

return events
