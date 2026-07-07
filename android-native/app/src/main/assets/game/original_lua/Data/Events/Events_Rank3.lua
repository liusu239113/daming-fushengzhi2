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
                    cost = {silver = 8},
                    result = "族人备下八色厚礼，租了一顶好轿赴县衙赴宴。席间觥筹交错，知县大人对族中赞许有加，当众许诺日后有事尽可去衙门说话。散席时更亲笔题了一副对联相赠，回来后高悬厅堂，邻里见了无不艳羡。",
                    effect = function()
                        GameData.AddResource("fame", 5)
                    end,
                },
                {
                    text = "薄礼应酬，不过分攀附（-3银两，+2声望）",
                    cost = {silver = 3},
                    result = "备了些土产干货作为薄礼，不卑不亢地赴了宴。席间不刻意攀附，只与同席乡绅叙些桑麻之事。知县大人虽未特别留意，倒也客客气气，散席时点头致意。不显山不露水，恰到好处。",
                    effect = function()
                        GameData.AddResource("fame", 2)
                    end,
                },
                {
                    text = "托病不去，省却银两",
                    result = "遣人送了一封致歉书信，言称族中有人染恙，不便赴宴。知县大人倒也没说什么，只是席间有人议论起来，说族中架子大，连县太爷的面子也不给。此后数月，但凡衙门里的事都轮不到族中头上了。",
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
                    cost = {silver = 10},
                    result = "请了城里有名的张讼师出面，翻出祖上地契、丈量旧图，在县堂上据理力争。知县审了半日，判定界石原位不变，赵家须退还所侵之地。族人大获全胜，失地悉数收回，当年即种上庄稼，秋后多收粮食八石。",
                    effect = function()
                        GameData.AddResource("grain", 8)
                    end,
                },
                {
                    text = "私下调解，各让一步（-3粮食，+2声望）",
                    cost = {grain = 3},
                    result = "请了乡中德高望重的陈老太爷出面调停，双方各退三尺，重新立了界石。虽损失了几亩田的收成，但两家冰释前嫌，日后相安无事。乡邻都说族中大度，声望反而更高了。",
                    effect = function()
                        GameData.AddResource("fame", 2)
                    end,
                },
                {
                    text = "暂且隐忍，日后再计较",
                    result = "想着多一事不如少一事，忍下了这口气。可赵家见族中软弱可欺，又偷偷移了几块界石，半年下来竟蚕食了不少良田。等到秋收清点，粮食比往年少了一大截，族人这才后悔当初不该心慈手软。",
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
                    cost = {silver = 15},
                    result = "独捐白银十五两，为书院翻新了讲堂、修补了藏书阁，又添置了百余卷经史子集。山长感激涕零，在书院门前立了一通功德碑，将族名刻于首位。此后县中士子提起书院必提族中义举，声望大振。",
                    effect = function()
                        GameData.AddResource("fame", 10)
                    end,
                },
                {
                    text = "略尽心意，捐些银两（-6银两，+4声望）",
                    cost = {silver = 6},
                    result = "捐了六两银子，在功德簿上记了一笔。山长连声道谢，赠了一方端砚作为回礼。银两虽不算多，但也为书院添了几片新瓦、补了几扇窗户，学子们冬日读书不再受风寒之苦。",
                    effect = function()
                        GameData.AddResource("fame", 4)
                    end,
                },
                {
                    text = "婉言推辞，自家也不宽裕",
                    result = "以家中用度紧张为由推辞了山长的请求。山长虽不好说什么，但脸上难掩失望之色。此后县中文人聚会，提起书院修缮之事，总少不了一句'某家一毛不拔'的议论，族中声名多少受了些损。",
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
                    cost = {silver = 12},
                    result = "倾尽族中积蓄，从各处收来大量土产山货，装了满满十几车运往约定之处。客商验货后甚为满意，当即结清货款，又追加了一笔定金。这一单下来利润丰厚，粮仓和银库都充盈了不少。",
                    effect = function()
                        GameData.AddResource("grain", 20)
                    end,
                },
                {
                    text = "谨慎行事，只做一半（-5银两，+10粮食）",
                    cost = {silver = 5},
                    result = "只拿出一半的货物与客商交易，留了一半压着以防万一。虽然利润没有全做的多，但到手的都是实实在在的收益，稳妥可靠。客商临走前说下次路过还来，算是结了善缘。",
                    effect = function()
                        GameData.AddResource("grain", 10)
                    end,
                },
                {
                    text = "婉拒此单，不冒此险",
                    result = "思来想去，觉得先垫货款的买卖风险太大，万一客商拿了货不付钱，找谁说理去？便客客气气地回绝了。客商叹了口气另寻别家去了。族人虽错失了一桩好买卖，但也免了血本无归的风险。",
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
                    cost = {silver = 10, grain = 5},
                    result = "花银子从镇上雇来十几个身强力壮的汉子，每夜轮班巡逻，又在院墙上加装了铁蒺藜。山贼几次来探虚实，见灯火通明、人声鼎沸，便悻悻退去。一连月余不见匪踪，乡邻也渐渐安心下来。",
                    effect = function()
                    end,
                },
                {
                    text = "联合邻家向县衙报案（-5银两，+3声望）",
                    cost = {silver = 5},
                    result = "联合周边数家大户联名向县衙递了呈文，又凑了些银两打点差役。县太爷派了一队官兵上山清剿，虽未尽歼贼寇，却也抓了几个喽啰。匪患暂时平息，族中因牵头报案之功，在乡间声望更高了。",
                    effect = function()
                        GameData.AddResource("fame", 3)
                    end,
                },
                {
                    text = "紧闭门户，听天由命（-8粮食，-3银两）",
                    result = "夜夜紧锁院门，全家人提心吊胆地过日子。某夜山贼终于摸上门来，翻墙入院偷走了好些粮食和银两。族人在黑暗中大气不敢出，直到贼人走远才敢点灯查看，损失惨重。",
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
                    cost = {silver = 12, grain = 5},
                    result = "提前数日便张罗开来，将祖宅打扫得纤尘不染，摆下丰盛酒席。通判大人巡察至此，见族中井井有条、殷勤周到，频频点头。临行前私下嘱咐随从记下族名，日后府中若有善政，优先照拂。族中声势大涨。",
                    effect = function()
                        GameData.AddResource("fame", 8)
                    end,
                },
                {
                    text = "按规矩备些薄礼（-5银两，-3粮食，+3声望）",
                    cost = {silver = 5, grain = 3},
                    result = "按照惯例备了些地方土产和茶酒，不多不少恰到好处。通判大人走马观花看了一圈，也没特别留意，临走时收下礼物点头致谢。虽不算出彩，但也没出什么纰漏，算是过了这一关。",
                    effect = function()
                        GameData.AddResource("fame", 3)
                    end,
                },
                {
                    text = "只做本分，不刻意逢迎",
                    result = "既不张罗酒席也不备礼相迎，只是把门前扫了扫便罢。通判大人路过时瞥了一眼，随从在册子上记了一笔。过后县令传话来说，族中怠慢上差，须补缴些许'修路银'。白白折了二两银子，还落了个不懂规矩的名声。",
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
                    cost = {silver = 6},
                    result = "置办了上等湖笔徽墨、新裁了一身儒衫，精神抖擞地赴了雅集。席间挥毫泼墨，一首七律赢得满座喝彩，被推为当日魁首。主持的名士当场将诗作裱起悬于亭中，从此县中文人圈里都知道了族中出了才子。",
                    effect = function()
                        GameData.AddResource("fame", 6)
                    end,
                },
                {
                    text = "简素赴会，以文采取胜（-2银两，+3声望）",
                    cost = {silver = 2},
                    result = "穿了一身旧儒衫便去了，不与人比排场。席间众人斗诗作赋，族中子弟虽不及名门才子，却也写了几句朴实可读的诗文，被在座先生夸了几句'朴而有味'。虽未拔头筹，倒也交了几位志同道合的书友。",
                    effect = function()
                        GameData.AddResource("fame", 3)
                    end,
                },
                {
                    text = "不去凑热闹，在家读书",
                    result = "觉得这种应酬场面虚浮无趣，不如在家埋头苦读来得实在。于是闭门谢客，安安静静地研读了几日经义。虽然错过了结交名士的机会，倒也在学问上精进了几分。",
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
                    cost = {silver = 12, grain = 15},
                    result = "拿出大量银粮赈济受灾佃户，又雇人修复溃堤、清理淤泥，趁着季节尚来得及抢种了一茬晚稻。佃户们感恩戴德，干活格外卖力。入秋后虽然比不上丰年，但总算没有颗粒无收。族中仁义之名传遍十里八乡。",
                    effect = function()
                        GameData.AddResource("fame", 8)
                    end,
                },
                {
                    text = "量力而行，先安抚佃户（-5银两，-8粮食，+3声望）",
                    cost = {silver = 5, grain = 8},
                    result = "拨了些银粮安抚最困难的几户佃户，帮他们撑过了最艰难的日子。堤坝只做了简单修补，来年若再有大水恐怕难以抵挡。不过佃户们总算没有逃散，秋后勉强有些收成。",
                    effect = function()
                        GameData.AddResource("fame", 3)
                    end,
                },
                {
                    text = "只管自家，佃户自谋出路（-5粮食，-4声望）",
                    result = "只顾着抢救自家宅院和存粮，对佃户的困境视若无睹。几户佃户等不到东家救济，含泪携家带口离去，投奔他乡。留下的佃户也心存怨怼，干活敷衍了事。乡邻议论纷纷，说族中只知敛财不顾人死活。",
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
    weight = 8,
    cooldownMonths = 6,
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
                    result = "择了个黄道吉日，备了见面礼前往周家相看。周家宅院气派非凡，待人接物也颇有规矩。双方长辈寒暄一番，对彼此印象都还不错，便商议起亲事的细节来。",
                    effect = function()
                        if candidate and not candidate.spouseId then
                            local GS = require("UI.GameScreen")
                            GS.ShowMarriageTierSelect(candidate)
                        end
                    end,
                },
                {
                    text = "婉言谢绝，另觅良缘",
                    result = "思量再三，觉得周家虽富，门户却未必般配，况且婚姻大事不可草率。便遣人回了媒人的话，说族中尚需从长计议。媒人虽有些不悦，但也无可奈何，悻悻而去。",
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
                    cost = {grain = 20, silver = 3},
                    result = "在村口搭了三间大粥棚，日日熬制浓粥，连施了整整半月。每日天不亮便排起长队，数百流民赖此活命。县太爷闻讯亲来巡视，当众赞许族中义举，特赐了一块'积善人家'的牌匾。族中善名远播，声望大涨。",
                    effect = function()
                        GameData.AddResource("fame", 15)
                    end,
                },
                {
                    text = "小规模施粥，尽绵薄之力（-10粮食，+7声望）",
                    cost = {grain = 10},
                    result = "在自家门前支了一口大锅，每日施粥两餐，虽不及大户那般排场，却也救了百余口饥民。有老妇人跪地叩谢，说这碗粥救了她孙儿的命。族人心中虽有不舍粮食，但看到这情景也觉得值了。",
                    effect = function()
                        GameData.AddResource("fame", 7)
                    end,
                },
                {
                    text = "自保为上，不参与施粥（-5声望）",
                    result = "紧闭大门，对门外流民的哀求充耳不闻。虽然保住了自家粮食，但乡邻和其他大户都在施粥行善，唯独族中袖手旁观，一时间成了众矢之的。有人编了顺口溜讽刺族中吝啬，传得满县皆知。",
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
                    cost = {silver = 12},
                    result = "一口气拿出十二两银子，请了石匠铺路、木匠架桥。三个月后，一条平整石板路直通县城，溪上新桥宽阔坚固，行人车马无不称便。桥头立了一通石碑，刻着族名居首。过往客商见了无不赞叹，族中声名远播。",
                    effect = function()
                        GameData.AddResource("fame", 10)
                    end,
                },
                {
                    text = "按份分摊，出应有之资（-5银两，+4声望）",
                    cost = {silver = 5},
                    result = "与邻近几家按田亩多少分摊费用，出了应有的一份银子。修路铺桥的事虽非族中独力完成，但也算出了力、尽了份，在功德碑上记了一笔。乡邻都说族中行事公道，不推诿也不逞强。",
                    effect = function()
                        GameData.AddResource("fame", 4)
                    end,
                },
                {
                    text = "推说手头紧，不出银子（-3声望）",
                    result = "推说今年收成不好，手头拮据，实在拿不出银子。乡老们面面相觑，暗自摇头。路桥修好后，碑上列了各家姓名，唯独缺了族名。每次路过那块碑，族人都觉得面上无光。",
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
                    cost = {silver = 15, grain = 10},
                    result = "虽说这笔赋税数目惊人，但族人咬牙凑齐了银粮，早早送往县衙。县令见族中率先完税，在花名册上记了一笔'良善'，日后摊派杂役时也多有照拂。佃户们见东家带头缴纳，也安了心，没有一户逃走的。",
                    effect = function()
                    end,
                },
                {
                    text = "打点胥吏，设法减免（-8银两，-5粮食）",
                    cost = {silver = 8, grain = 5},
                    result = "备了些银两悄悄塞给税房的胥吏，又请里长帮忙说情。胥吏收了好处，在丁亩册上做了些手脚，将族中田亩数报少了几成。如此一来实际缴纳的赋税少了大半，省下不少银粮。只是此事若被追查，恐有后患。",
                    effect = function()
                    end,
                },
                {
                    text = "拖延不缴，等风头过去（-5声望，-8粮食）",
                    result = "一拖再拖，指望着朝廷改了主意或是县令睁只眼闭只眼。谁知差役三番五次登门催逼，最后强行征走了一批粮食。族中名声也因抗税之嫌受损，邻里背后议论纷纷，说族中不识时务。",
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
                    cost = {grain = 10},
                    result = "将减租的告示贴在了村口大槐树下，佃户们奔走相告、喜极而泣。王三更是带着一众佃户到祠堂前磕头谢恩，发誓来年丰收后加倍偿还。消息传开后，附近几个村子的佃户都想来租族中的地，人心所向，声望日隆。",
                    effect = function()
                        GameData.AddResource("fame", 6)
                    end,
                },
                {
                    text = "略减一成，余者延期缴纳（-5粮食，+2声望）",
                    cost = {grain = 5},
                    result = "减了一成租子，余下的准许佃户分三期缴清。虽非大恩大德，但佃户们也算松了口气，不至于卖儿鬻女度日。王三含泪道谢，保证来年不会拖欠。乡邻评说族中行事还算公道，不枉地主之名。",
                    effect = function()
                        GameData.AddResource("fame", 2)
                    end,
                },
                {
                    text = "规矩不可废，照旧收租（+5粮食，-5声望）",
                    result = "板着脸回绝了佃户的请求，一粒租子也不肯少。王三跪在地上哭求无果，只得含恨离去。当年便有三户佃户偷偷卷铺盖逃了，留下的佃户也怨声载道，干活出工不出力。乡邻私下议论，都说族中心太狠。",
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
                    cost = {silver = 12},
                    result = "倾囊将整批丝绸买下，细细验过货色，果然是上等苏绸，光泽柔滑、花色精美。存了半月后逢集市开张，各家绸缎铺争相购买，不出十日便脱手殆尽。算下来获利颇丰，库中布匹充盈，一时风光无二。",
                    effect = function()
                        GameData.AddResource("cloth", 15)
                    end,
                },
                {
                    text = "买下一半，稳妥为上（-6银两，+8布匹）",
                    cost = {silver = 6},
                    result = "只买了一半的货，留了另一半让别家去争。虽说利润不及全吃下来那般丰厚，但到手的丝绸确是好货，转手卖了几匹便回了本。剩下的收在库中，逢年过节裁衣送礼，颇有面子。",
                    effect = function()
                        GameData.AddResource("cloth", 8)
                    end,
                },
                {
                    text = "不做此单，怕有猫腻",
                    result = "总觉得天上不会掉馅饼，低价出货怕是有什么猫腻——要么是偷来的赃物，要么是以次充好。客客气气地回绝了丝商。后来听说那批丝绸被邻村大户全数买去，转手赚了一大笔。族人虽有几分后悔，但想想谨慎也没什么错。",
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
                    cost = {silver = 10, grain = 5},
                    result = "以丰厚束脩聘请陈举人入馆，腾出最好的书房供其居住，三餐另开小灶款待。陈举人感其诚意，倾囊相授，日日督课严谨。不出一年，族中子弟学业突飞猛进，县试中连中数人。十里八乡都知道族中延请了名师，纷纷遣子来附学。",
                    effect = function()
                        GameData.AddResource("fame", 8)
                    end,
                },
                {
                    text = "与邻家合聘，分摊费用（-5银两，-3粮食，+4声望）",
                    cost = {silver = 5, grain = 3},
                    result = "与邻近三家合资聘请陈举人，在村口祠堂旁设了学馆。虽然陈举人精力分散，不能专心教导一家子弟，但胜在花费不大，子弟们也确实学到了不少东西。邻里几家因此走动更勤，关系更加融洽。",
                    effect = function()
                        GameData.AddResource("fame", 4)
                    end,
                },
                {
                    text = "族中自有先生，不必外聘",
                    result = "以族中已有私塾先生为由推辞了。陈举人被邻村大户聘去，此后那家子弟接连考中秀才，风头一时无两。族中先生虽然忠厚，学问到底有限，子弟们的科考之路依旧坎坷，族人暗自叹息错失了良机。",
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
                    cost = {silver = 8},
                    result = "备下酒席，亲自登门邀请刘家家主赴宴。席间推杯换盏，不提旧怨，只叙乡邻情谊。刘家家主本也非蛮横之人，几杯酒下肚便软了态度，当面表示日后井水不犯河水。宴罢两家握手言和，乡邻们都赞族中气度不凡。",
                    effect = function()
                        GameData.AddResource("fame", 5)
                    end,
                },
                {
                    text = "暗中反击，以牙还牙（-5银两，-3声望）",
                    cost = {silver = 5},
                    result = "花银子打探刘家底细，暗中散布些真真假假的传言，又拉拢了刘家的几个佃户。两家你来我往斗了数月，互有损伤。虽然暂时遏制了刘家的势头，但此等手段传出去不甚光彩，乡间口碑也跟着受了损。",
                    effect = function()
                        GameData.AddResource("fame", -3)
                    end,
                },
                {
                    text = "不理会，做好自家事（-2声望）",
                    result = "决定不与刘家计较，埋头做好自家的生意和田产。然而刘家见族中不作回应，愈发得寸进尺，挖走了两个老佃户，抢了一桩原本谈好的买卖。族中虽然根基未动，但在乡间的话语权渐渐弱了几分。",
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
                    cost = {silver = 15, grain = 10},
                    result = "从省城请来了当红戏班，搭了三丈高的戏台，连唱三天三夜。鞭炮齐鸣、锣鼓喧天，十里八乡的百姓都赶来看戏，人山人海好不热闹。酒席流水般摆了上百桌，连过路的行人都能坐下吃一碗。这一场庆典办得空前盛大，族中威望一时无两。",
                    effect = function()
                        GameData.AddResource("fame", 12)
                    end,
                },
                {
                    text = "中规中矩，不失体面（-7银两，-5粮食，+5声望）",
                    cost = {silver = 7, grain = 5},
                    result = "请了本地的小戏班唱了一天，又摆了几十桌酒席招待乡邻。虽说排场不及大户那般阔绰，但该有的仪式一样不缺，香案供品整整齐齐，社戏也唱得有模有样。乡老们点头赞许，说族中办事还算体面。",
                    effect = function()
                        GameData.AddResource("fame", 5)
                    end,
                },
                {
                    text = "推辞不办，让与他家",
                    result = "推说今年手头不便，将主持之责让与了邻家。乡老们面露不悦，背后议论说族中忝居大户之列，连办个社日庆典都推三阻四，实在上不了台面。此后乡中议事也不再来请族中参与，渐渐被边缘化了。",
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
                    cost = {silver = 10, grain = 8},
                    result = "出资购置了刀枪棍棒和弓箭，从附近村子招募了三十名青壮，每日操练不辍。族中武艺出众者担任教头，半年下来这支乡勇已颇有章法。盗匪闻风不敢靠近，县令也专门来看了一回，赞其'功在桑梓'，声望大增。",
                    effect = function()
                        GameData.AddResource("fame", 8)
                    end,
                },
                {
                    text = "小规模筹备，先练几个人（-5银两，-3粮食，+3声望）",
                    cost = {silver = 5, grain = 3},
                    result = "从族中挑了七八个年轻后生，置办了些简陋兵器，每日早晚操练一番。虽然人数不多、装备简陋，但好歹多了一层保障。夜间巡逻时遇到过几个毛贼，被乡勇们吆喝着赶跑了，倒也管些用。",
                    effect = function()
                        GameData.AddResource("fame", 3)
                    end,
                },
                {
                    text = "此事须慎重，暂且搁置",
                    result = "以'私练武装恐触官府忌讳'为由暂且搁置了此议。族中武艺之人虽有些不满，但也只得作罢。数月后邻村遭了匪劫，损失惨重，族人虽庆幸自家无事，心中也暗暗后悔当初没有早做准备。",
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
                    cost = {silver = 15},
                    result = "择了村后高岗之地，请匠人用青砖砌基、杉木立柱、灰瓦盖顶，又在四周开了排水暗沟。新仓宽敞高大，通风干燥，可存粮数百石。落成之日管事笑得合不拢嘴，说这辈子还没见过这么气派的粮仓。从此粮食再无虫蛀鼠患之忧。",
                    effect = function()
                        GameData.AddResource("grain", 15)
                    end,
                },
                {
                    text = "修缮旧仓，将就使用（-5银两，+5粮食存储）",
                    cost = {silver = 5},
                    result = "请人将旧仓的漏洞堵了、朽木换了，又撒了石灰灭了鼠穴。修补后的粮仓虽不及新建的气派，但总算不再漏雨漏风，存粮也安全了许多。管事说至少能再撑个三五年，到时再做打算。",
                    effect = function()
                        GameData.AddResource("grain", 5)
                    end,
                },
                {
                    text = "暂且不修，粮食卖了换银子",
                    result = "不修仓倒省了银子，干脆趁粮价尚可把多余的粮食拿到集市上换了银两。可入冬后鼠患愈发严重，旧仓里剩下的粮食被老鼠啃了不少，霉变虫蛀的更是数不胜数。到了来年青黄不接时，粮食竟有些捉襟见肘。",
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
                    cost = {silver = 12},
                    result = "以十二两银子买下那幅山水长卷，请裱画师精心装裱后悬于正厅。画上云山苍茫、松风萧瑟，气韵生动非凡。县中识货的文人雅士纷纷登门观赏，啧啧赞叹，都说这是沈周晚年的精品力作。族中因此被视为有品位的书香门第，声名大噪。",
                    effect = function()
                        GameData.AddResource("fame", 7)
                    end,
                },
                {
                    text = "压价购入，赌上一赌（-5银两，+3声望）",
                    cost = {silver = 5},
                    result = "与书生磨了半天嘴皮子，以五两银子成交。画虽买到手了，但到底是真是假心里没底。挂在书房里赏了几日，请了一位老画师来看，说笔法虽有几分沈周的影子，但更像是其弟子的仿作。不管真假，画倒是好看，挂着也增些雅气。",
                    effect = function()
                        GameData.AddResource("fame", 3)
                    end,
                },
                {
                    text = "不识真伪，还是不买",
                    result = "想了想自家人不懂字画，万一买了个赝品岂不是花了冤枉银子还被人笑话？便客气地回绝了。书生叹了口气，抱着画卷往邻村去了。后来也不知那画卖给了谁，是真是假更无从得知，只当是与它无缘罢了。",
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
                    cost = {silver = 8},
                    result = "不惜高价从邻村雇来二十多个短工，天不亮就下田抢收。管事盯在地头，督促众人一刻不停。五天之内将所有成熟的庄稼尽数割回，晒谷场上金灿灿铺了一地。虽然工钱花了不少，但颗粒归仓，总算没有白费一年的辛苦。",
                    effect = function()
                        GameData.AddResource("grain", 15)
                    end,
                },
                {
                    text = "合理出价，能收多少算多少（-3银两，+8粮食）",
                    cost = {silver = 3},
                    result = "以正常价格雇了几个帮工，加上族中老少齐上阵，紧赶慢赶收了大半的庄稼。剩下几亩偏远的田因来不及收割，任由稻穗烂在了地里。虽有些可惜，但主要的收成保住了，也算过得去。",
                    effect = function()
                        GameData.AddResource("grain", 8)
                    end,
                },
                {
                    text = "全靠族中自家人，省了工钱（+3粮食）",
                    result = "全族上下老的老、小的小，一齐下田抢收。从早忙到黑，累得腰都直不起来，只收了不到三成的庄稼。眼看着大片稻子熟透了没人割，一阵风雨过后纷纷倒伏霉烂，心疼得族人直掉眼泪。省了工钱却丢了粮食，得不偿失。",
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
                    cost = {silver = 15, grain = 5},
                    result = "从府城请来了一位精通疫症的老郎中，又采办了大量黄连、苍术、石菖蒲等药材。老郎中开了方子，又指导族人在村口燃烧艾草驱邪、撒石灰消毒水井。经过月余苦战，疫情终于得到控制，族中无一人死于此疫。乡间传为美谈，都说族中积德行善感动了上苍。",
                    effect = function()
                        GameData.AddResource("fame", 10)
                    end,
                },
                {
                    text = "备些药材，自行煎服（-6银两，-3粮食）",
                    cost = {silver = 6, grain = 3},
                    result = "到药铺抓了些常用的解毒防疫草药，按老法子煎了大锅汤药让全族人服用。又将染病者隔离在偏房中静养。虽无名医指点，但靠着这些土办法和族人自身体质，总算熬了过来。有几人病了月余方愈，但所幸无人丧命。",
                    effect = function()
                    end,
                },
                {
                    text = "闭门不出，隔绝外人（-8粮食，-5声望）",
                    result = "紧闭院门，不准任何人进出，连邻居求药都拒之门外。一家人蜗居屋中大气不敢出，粮食很快便消耗殆尽。更可悲的是，尽管隔绝了外人，仍有族人染上了疫症。等到疫情过去，族中元气大伤，而乡邻们对族中见死不救的做法更是寒了心。",
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
                    cost = {silver = 8, grain = 15},
                    result = "买了十几口大缸，请了个在绍兴学过酿酒的师傅坐镇，挑上好的糯米浸泡蒸煮、拌曲发酵。两个月后开坛，一股醇香扑鼻而来，尝了一口绵柔甘洌、回味悠长。拿到集市上试卖，被酒楼和商户抢购一空。这门生意比种田划算多了，银两滚滚而来。",
                    effect = function()
                        GameData.AddResource("silver", 20)
                    end,
                },
                {
                    text = "小试牛刀，先酿几缸看看（-3银两，-8粮食，+8银两收益）",
                    cost = {silver = 3, grain = 8},
                    result = "先酿了三缸试试水，请村中几位老酒客品评。众人喝了都说不错，味道虽比不上绍兴名酒，但胜在价廉物美。拿到镇上小卖，很快就脱销了。虽然规模不大利润有限，但算是开了个好头，日后若要扩大也有了经验。",
                    effect = function()
                        GameData.AddResource("silver", 8)
                    end,
                },
                {
                    text = "酿酒费粮又费事，还是算了",
                    result = "想想酿酒又要买缸、又要请师傅、还要费大量粮食，万一酿坏了岂不是血本无归？还是老老实实种地来得稳当。将这个念头打消了，继续守着自家的一亩三分地过日子。虽然日子波澜不惊，倒也安安稳稳。",
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
                    cost = {silver = 15, cloth = 5},
                    result = "请来最好的匠人，将祠堂从里到外翻修一新。换了楠木大梁、重塑了祖宗金身、添了新供桌香炉，又在门前立了两根石柱，气派非凡。落成之日举行了盛大的祭祖仪式，各房族人齐聚一堂，感慨万千。从此祠堂成为全族的骄傲，乡中人人称羡。",
                    effect = function()
                        GameData.AddResource("fame", 15)
                    end,
                },
                {
                    text = "号召各房分摊，族中共修（-7银两，-3布匹，+7声望）",
                    cost = {silver = 7, cloth = 3},
                    result = "号召各房按人丁分摊费用，族中出大头，各房出小头。虽有几房嘀嘀咕咕说分摊不公，但总算将祠堂修缮完毕。新换了屋瓦、补了柱础，祖宗牌位也重新漆了金。修完后大家一起祭了祖，也算凝聚了族心。",
                    effect = function()
                        GameData.AddResource("fame", 7)
                    end,
                },
                {
                    text = "小修小补，先凑合着用（-3银两，+2声望）",
                    result = "只花了三两银子请人补了几片瓦、换了几根腐朽的椽子。祠堂虽不再漏雨，但看上去依旧破旧寒酸。族老们叹息道，堂堂一族的祠堂修成这般模样，说出去实在不好看。好在祖宗牌位总算不用再淋雨了，聊胜于无。",
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
                    cost = {silver = 10, cloth = 3},
                    result = "忍着一肚子火气备下银两绸缎，恭恭敬敬地送了过去。张典吏收了好处，笑眯眯地在田册上画了勾，说'贵族田亩无误'便走了。虽然破了财，但至少保了个太平。只是心中清楚，这不过是喂了一回狼，下次他还会再来。",
                    effect = function()
                    end,
                },
                {
                    text = "略备薄礼，不卑不亢（-5银两）",
                    cost = {silver = 5},
                    result = "备了五两银子和一坛好酒，不多不少地送了去。张典吏掂了掂银子皱了皱眉，嘟囔着说'年头不好大家都不容易'，勉强收下了。田册上的事暂时搁过了，但看他临走时那阴晴不定的脸色，怕是心中还有些不满。",
                    effect = function()
                    end,
                },
                {
                    text = "硬气拒绝，搜集证据告他（-3银两，+5声望，-3声望风险）",
                    result = "当面回绝了张典吏的索贿，还放话说要向上官检举。又花银子请人暗中搜集张典吏贪赃枉法的证据，联合几家苦主一同具状上告。此事轰动一时，知县虽然没有严办张典吏，但也敲打了他一番。族中因敢于仗义执言，在乡间赢得了不少敬重。",
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
