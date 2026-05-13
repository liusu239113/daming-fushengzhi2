local GameData = require("Data.GameData")

local events = {}

-- 1. 族人中举（科举喜事）
events[#events + 1] = {
    id = "r4_imperial_exam",
    title = "族人中举",
    rankRange = {4, 5},
    weight = 8,
    cooldownMonths = 12,
    identityMatch = {"scholar"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local scholars = GameData.GetMembersByState("读书")
        for _, m in ipairs(scholars) do
            if m.alive and m.age >= 18 then return true end
        end
        return false
    end,
    execute = function(s, report)
        local scholars = GameData.GetMembersByState("读书")
        local candidate = nil
        for _, m in ipairs(scholars) do
            if m.alive and m.age >= 18 then candidate = m; break end
        end
        local name = candidate and candidate.name or "族人"
        report.events[#report.events + 1] = name .. "参加乡试，高中举人，阖族欢庆"
        GameData.AddLog(name .. "科场得意，中举归来")
        return {
            title = "族人中举", desc = name .. "在乡试中表现出众，金榜题名高中举人。消息传回族中，亲朋故旧纷纷前来道贺，族中声望大振。",
            choices = {
                { text = "大办宴席，宴请宾客（银两-25，声望+20）", cost = {silver = 25}, result = "庄园张灯结彩，流水席摆了三日三夜，四方宾客纷至沓来。鞭炮声中，族长亲率族人迎候道贺之人，觥筹交错间，阖族荣光尽显。此番盛举传遍十里八乡，望族声望大振。", effect = function() GameData.AddResource("fame", 20) end },
                { text = "低调庆贺，赠予盘缠赴京（银两-10，声望+8）", result = "族中只摆了几桌家宴，不事张扬。族长私下备了盘缠路费，嘱咐举人进京赶考务必专心学业。亲朋虽觉低调了些，但也赞许族长深谋远虑，不忘以学业为重。", effect = function() GameData.AddResource("silver", -10); GameData.AddResource("fame", 8) end },
            }
        }
    end,
}

-- 2. 商队远行（长途贸易）
events[#events + 1] = {
    id = "r4_trade_caravan",
    title = "商队远行",
    rankRange = {4, 5},
    weight = 8,
    cooldownMonths = 8,
    identityMatch = {"merchant"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local merchants = GameData.GetMembersByState("经商")
        for _, m in ipairs(merchants) do
            if m.alive and m.age >= 20 then return true end
        end
        return false
    end,
    execute = function(s, report)
        local merchants = GameData.GetMembersByState("经商")
        local leader = nil
        for _, m in ipairs(merchants) do
            if m.alive and m.age >= 20 then leader = m; break end
        end
        local name = leader and leader.name or "族人"
        report.events[#report.events + 1] = name .. "率商队远赴边疆经商"
        GameData.AddLog(name .. "组织商队远行贸易")
        return {
            title = "商队远行", desc = name .. "提议组建大型商队，携带丝绸、茶叶等货物远赴边疆重镇贸易。路途遥远，利润丰厚但风险亦大。",
            choices = {
                { text = "全力支持，倾力筹备（银两-20，布匹-15，粮食+40，银两+30）", cost = {silver = 20, cloth = 15}, result = "商队浩浩荡荡启程，车马载满丝绸茶叶，一路翻山越岭抵达边疆重镇。异域商贾争相采买，以皮毛良马和白银换取中原货物。数月后满载而归，族中银钱粮食大为充裕。", effect = function() GameData.AddResource("grain", 40); GameData.AddResource("silver", 30) end },
                { text = "小规模试探，派少量人马（银两-8，粮食+15）", result = "只派了十余人携少量货物前往试探。虽未赚得大利，但也平安往返，带回了些许粮食物资。族人得以一窥边贸商机，为日后大举通商积累了经验。", effect = function() GameData.AddResource("silver", -8); GameData.AddResource("grain", 15) end },
            }
        }
    end,
}

-- 3. 官场暗斗（政治纷争）
events[#events + 1] = {
    id = "r4_political_scheme",
    title = "官场暗斗",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "地方官员派系争斗牵连族中"
        GameData.AddLog("官场暗流涌动，族中被卷入纷争")
        return {
            title = "官场暗斗", desc = "府城中两派官员明争暗斗，一方主动拉拢你族站队，另一方则暗中施压。身为望族，难以置身事外。",
            choices = {
                { text = "审时度势，支持势强一方（银两-15，声望+12）", cost = {silver = 15}, result = "族长暗中遣人打探虚实，看准风向后果断押注。奉上银两以为交好之资，得势一方甚为满意，日后在赋税徭役上颇多关照。族中虽有人忧虑站队之险，但眼下确是获益匪浅。", effect = function() GameData.AddResource("fame", 12) end },
                { text = "两不相帮，闭门谢客（声望-5）", result = "族长紧闭大门，对两方使者一概不见。数月后纷争落幕，得胜一方对望族的冷淡颇为不满，日后凡有公事便多加刁难。族中虽保得一时清净，却失了官场人脉。", effect = function() GameData.AddResource("fame", -5) end },
                { text = "暗中斡旋调解两方（银两-10，声望+18）", cost = {silver = 10}, result = "族长以中间人身份暗中往来斡旋，设宴款待两方心腹，晓之以理动之以情。经月余周旋，两派终于握手言和。族长调和之功传开，官场士林皆称赞望族深明大义、善于周全。", effect = function() GameData.AddResource("fame", 18) end },
            }
        }
    end,
}

-- 4. 赈灾义举（组织赈济）
events[#events + 1] = {
    id = "r4_famine_relief",
    title = "赈灾义举",
    rankRange = {4, 5},
    weight = 8,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "邻县遭灾，百姓流离，望族出面赈济"
        GameData.AddLog("邻县灾荒，族中商议赈济事宜")
        return {
            title = "赈灾义举", desc = "邻县遭逢水患，大批灾民涌入本地。官府力有不逮，地方士绅望向你族。身为望族，此乃彰显家风、广积善缘的大好时机。",
            choices = {
                { text = "倾力赈济，设粥棚施衣物（粮食-30，布匹-10，声望+20）", cost = {grain = 30, cloth = 10}, result = "族中在城门外搭起十余座粥棚，日夜不歇地熬粥施饭。又将库中棉衣布匹分发灾民御寒，老弱妇孺无不涕泪交加。数百灾民得保性命，望族仁义之名远播四方，官府亦上报朝廷嘉许。", effect = function() GameData.AddResource("fame", 20) end },
                { text = "量力而行，适度捐助（粮食-12，声望+8）", cost = {grain = 12}, result = "拿出部分存粮设了一处小粥棚，每日施粥百余碗。虽不能尽救所有灾民，但也让不少饥寒交迫之人得以果腹。乡邻感念望族善举，口碑有所增长。", effect = function() GameData.AddResource("fame", 8) end },
                { text = "仅派人协助官府，不出钱粮（声望-3）", result = "只遣了几名族人去官府帮忙登记灾民名册，自家却分文不出。灾民在庄园外嗷嗷待哺，望族大门紧闭。乡里暗中议论纷纷，说望族富而不仁，声名因此受损。", effect = function() GameData.AddResource("fame", -3) end },
            }
        }
    end,
}

-- 5. 扩建庄园（大规模建设）
events[#events + 1] = {
    id = "r4_estate_expansion",
    title = "扩建庄园",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中决议扩建庄园宅院"
        GameData.AddLog("族中商议扩建庄园事宜")
        return {
            title = "扩建庄园", desc = "族中人丁兴旺，现有宅院已显拥挤。族老们提议扩建庄园，修筑新院落、加固围墙、整饬花园，以配望族气派。",
            choices = {
                { text = "大兴土木，修建气派宅院（银两-25，布匹-10，声望+15）", cost = {silver = 25, cloth = 10}, result = "请来数十名能工巧匠，历时数月修筑起一座三进大宅院。雕梁画栋、飞檐翘角，花厅水榭一应俱全。落成之日宾客云集，无不赞叹望族气派。自此族人居住宽敞，接待客人也更有体面。", effect = function() GameData.AddResource("fame", 15) end },
                { text = "修缮旧宅，小规模翻新（银两-10，声望+5）", result = "将漏雨的屋顶修补一新，朽坏的门窗换过，院墙也重新粉刷。虽非大兴土木，但旧宅焕然一新，族人住得也舒适了不少。花费不大，却实实在在改善了居住条件。", effect = function() GameData.AddResource("silver", -10); GameData.AddResource("fame", 5) end },
            }
        }
    end,
}

-- 6. 匪军围城
events[#events + 1] = {
    id = "r4_bandit_army",
    title = "匪军围城",
    rankRange = {4, 5},
    weight = 5,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "大股匪军围困县城，望族首当其冲"
        GameData.AddLog("匪军来犯，围困县城！")
        return {
            title = "匪军围城", desc = "一股流寇聚众数千围困县城，官兵寡不敌众。匪首指名要望族交出钱粮赎城，否则破城之日鸡犬不留。",
            choices = {
                { text = "出资组织乡勇守城（银两-20，粮食-20，声望+18）", result = "族长散尽银两招募青壮，又开仓放粮犒赏守城将士。乡勇们感奋之下拼死守城，连夜加固城墙、备足滚木礌石。匪军数次强攻未克，终于悻悻退去。守城之功传遍州府，望族义勇之名深入人心。", effect = function() GameData.AddResource("silver", -20); GameData.AddResource("grain", -20); GameData.AddResource("fame", 18) end },
                { text = "交出部分钱粮议和（银两-15，粮食-25，布匹-10）", result = "族长忍痛将银两粮食布匹装了十余车，遣管事出城与匪首交涉。匪首见财物丰厚，遂允诺退兵。虽保全了城池与族人性命，但族中积蓄损失大半，数月之内恐怕要紧衣缩食度日。", effect = function() GameData.AddResource("silver", -15); GameData.AddResource("grain", -25); GameData.AddResource("cloth", -10) end },
                { text = "携族人弃城避难（声望-15，粮食-10）", result = "族长连夜率领老幼妇孺从北门出城，仓皇逃往深山中躲避。一路颠沛流离，粮食所剩无几。匪军破城后大肆劫掠，族中宅院被洗劫一空。待匪退后归来，满目疮痍，族人怨声载道。", effect = function() GameData.AddResource("fame", -15); GameData.AddResource("grain", -10) end },
            }
        }
    end,
}

-- 7. 文社雅集（文人聚会）
events[#events + 1] = {
    id = "r4_literary_society",
    title = "文社雅集",
    rankRange = {4, 5},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = {"scholar"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local scholars = GameData.GetMembersByState("读书")
        for _, m in ipairs(scholars) do
            if m.alive and m.age >= 16 then return true end
        end
        return false
    end,
    execute = function(s, report)
        local scholars = GameData.GetMembersByState("读书")
        local attendee = nil
        for _, m in ipairs(scholars) do
            if m.alive and m.age >= 16 then attendee = m; break end
        end
        local name = attendee and attendee.name or "族人"
        report.events[#report.events + 1] = name .. "受邀参加府城文社雅集"
        GameData.AddLog(name .. "参加文社雅集，与名士交游")
        return {
            title = "文社雅集", desc = "府城名流举办文社雅集，邀请各望族子弟赋诗论文。" .. name .. "才学出众，受邀赴会。此乃结交名士、扬名立万的好机会。",
            choices = {
                { text = "备厚礼赴会，力求一鸣惊人（银两-12，声望+15）", cost = {silver = 12}, result = "备了上等文房四宝与名贵茶叶作为赴会之礼，又精心准备了诗赋文章。雅集之上一首七律技惊四座，名士纷纷击节叹赏，争相传抄。此后文坛之中提起望族子弟，无不竖指称赞。", effect = function() GameData.AddResource("fame", 15) end },
                { text = "轻装简从，以才学取胜（银两-3，声望+8）", result = "只带了随身笔墨简装赴会，在席间以一篇策论引经据典、见解独到，虽无厚礼相赠，但才学令人折服。几位名士主动攀谈结交，约定日后书信往来，也算不虚此行。", effect = function() GameData.AddResource("silver", -3); GameData.AddResource("fame", 8) end },
            }
        }
    end,
}

-- 8. 朝廷征召（入京述职）
events[#events + 1] = {
    id = "r4_court_summon",
    title = "朝廷征召",
    rankRange = {4, 5},
    weight = 6,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        return #adults >= 3
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "朝廷下旨征召族中贤能入京"
        GameData.AddLog("圣旨到！朝廷征召族人入京")
        return {
            title = "朝廷征召", desc = "朝廷颁下诏书，征召各地望族贤能入京述职。这既是光耀门楣的机会，也意味着族中要承担不菲的路费与应酬开支。",
            choices = {
                { text = "隆重赴京，广结朝中权贵（银两-25，布匹-10，声望+20）", cost = {silver = 25, cloth = 10}, result = "族长精心备办锦缎绸匹等贡品，率随从浩浩荡荡赴京。入京后四处拜谒权贵要员，广赠礼物结纳人脉。述职之际深得上官赏识，赐宴嘉勉。此行虽费银甚巨，却为族中打通了朝中关节，声望大增。", effect = function() GameData.AddResource("fame", 20) end },
                { text = "简朴赴京，恪尽职责即可（银两-10，声望+10）", cost = {silver = 10}, result = "轻车简从赴京述职，不事奢华，恪尽本分。虽未能广结权贵，但述职之中条理分明、言辞恳切，上官亦觉此人务实可靠。归来后族人皆赞族长稳重持家。", effect = function() GameData.AddResource("fame", 10) end },
                { text = "称病推辞，不愿卷入朝堂（声望-8）", result = "族长以染疾为由上书推辞，闭门不出。朝廷虽未深究，但地方官员对此颇有微词，认为望族托大不恭。日后遇有公事，官府便不再优先关照，族中在地方上的话语权也有所削弱。", effect = function() GameData.AddResource("fame", -8) end },
            }
        }
    end,
}

-- 9. 世家争锋（与对手家族对抗）
events[#events + 1] = {
    id = "r4_rival_clan",
    title = "世家争锋",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "邻县望族挑衅，争夺地方话语权"
        GameData.AddLog("世家争锋，邻县望族前来叫阵")
        return {
            title = "世家争锋", desc = "邻县一望族近年崛起，处处与你族争锋。先是抢夺集市摊位，后又在官府面前诋毁你族。族老们群情激愤，要求给予回击。",
            choices = {
                { text = "以财力压制，展示底蕴（银两-20，声望+12）", cost = {silver = 20}, result = "族长大手笔出资修缮县学、捐建义仓，又在集市上以低价倾销货物，处处彰显财力雄厚。对方家族相形见绌，在地方上的风头被完全压过。虽耗费不少银两，但望族的气势与地位无人敢再质疑。", effect = function() GameData.AddResource("fame", 12) end },
                { text = "以礼相待，化敌为友（银两-8，粮食-5，声望+6）", result = "族长亲自登门拜访对方族长，携厚礼赔罪，言辞诚恳。席间推杯换盏，将前嫌尽释。两族约定日后和睦相处、互通有无。虽有族人觉得示弱了些，但终究免去了一场恶斗。", effect = function() GameData.AddResource("silver", -8); GameData.AddResource("grain", -5); GameData.AddResource("fame", 6) end },
                { text = "联合其他家族孤立对方（银两-15，声望+15）", cost = {silver = 15}, result = "族长暗中联络县中其余数家望族，设宴结盟，共同抵制对方。盟约一成，对方在商贸官场上处处碰壁，再无力与望族争锋。各家对族长的纵横捭阖之术颇为敬佩，族中声望更上层楼。", effect = function() GameData.AddResource("fame", 15) end },
            }
        }
    end,
}

-- 10. 盐业专营（获取盐引）
events[#events + 1] = {
    id = "r4_salt_monopoly",
    title = "盐业专营",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 12,
    identityMatch = {"merchant"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local merchants = GameData.GetMembersByState("经商")
        for _, m in ipairs(merchants) do
            if m.alive then return true end
        end
        return false
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "朝廷开放部分盐引，族中有机会竞购"
        GameData.AddLog("盐引开放，族中商议是否竞购")
        return {
            title = "盐业专营", desc = "朝廷今年额外发放一批盐引，允许望族大户竞购。盐业利润丰厚，但需大量本金，且官场上下打点费用不菲。",
            choices = {
                { text = "全力竞购，志在必得（银两-25，银两+30，粮食+20）", cost = {silver = 25}, result = "族长倾尽全力上下打点，终于竞得大批盐引。盐运一开，白花花的银子如流水般涌入。又以盐换粮，囤积了大量粮食。盐业之利果然丰厚，族中财力更上一层，在商界的地位也水涨船高。", effect = function() GameData.AddResource("silver", 30); GameData.AddResource("grain", 20) end },
                { text = "少量竞购，稳中求进（银两-12，银两+15）", cost = {silver = 12}, result = "只竞得少量盐引，小本经营试探市场。虽利润不算丰厚，但也稳稳当当赚了些银两。族人见盐业确有利可图，日后或可加大投入。", effect = function() GameData.AddResource("silver", 15) end },
                { text = "放弃竞购，另寻商机", result = "族长思虑再三，觉得盐业水深人杂，一个不慎便会惹祸上身，遂放弃竞购。虽未有损失，但眼看其他家族因盐利大发其财，族中不免有人扼腕叹息。", effect = function() end },
            }
        }
    end,
}

-- 11. 军役征调（抽丁从军）
events[#events + 1] = {
    id = "r4_military_levy",
    title = "军役征调",
    rankRange = {4, 5},
    weight = 5,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        local count = 0
        for _, m in ipairs(adults) do
            if m.alive and m.gender == "男" then count = count + 1 end
        end
        return count >= 2
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "朝廷征兵令下达，望族亦难免"
        GameData.AddLog("军役征调令至，族中青壮难逃")
        return {
            title = "军役征调", desc = "边疆战事吃紧，朝廷下达紧急征兵令。望族虽有一定特权，但此次征调力度极大，每户须出丁或交纳代役银。",
            choices = {
                { text = "出丁应征，保全族产（声望+8）", result = "族中挑选了数名健壮青年应征入伍，披甲执戈奔赴边关。出征之日族长亲自送行，乡邻皆赞望族忠义报国。虽失去几名壮丁令人痛惜，但族产得以保全，朝廷亦记下望族之功。", effect = function() GameData.AddResource("fame", 8) end },
                { text = "交纳代役银免征（银两-25）", result = "族长咬牙拿出大笔银两缴纳代役银，将族中青壮免于征发。银两虽去了不少，但族中壮劳力得以保全，田间劳作和商号经营都未受影响。族人暗自庆幸，只是库银空了大半。", effect = function() GameData.AddResource("silver", -25) end },
                { text = "上下打点求减免名额（银两-18，声望-5）", result = "族长暗中向征兵官吏塞了银两，求得减免部分名额。虽省了些代役银，但此事被人传出去后，乡邻颇有议论，说望族以权谋私、不顾大局。族中名声因此有所折损。", effect = function() GameData.AddResource("silver", -18); GameData.AddResource("fame", -5) end },
            }
        }
    end,
}

-- 12. 修建寺庙（宗教捐助）
events[#events + 1] = {
    id = "r4_temple_rebuild",
    title = "修建寺庙",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "县中古刹破败，高僧请求望族出资修缮"
        GameData.AddLog("高僧登门，请求资助修缮古刹")
        return {
            title = "修建寺庙", desc = "县中百年古刹年久失修，住持高僧亲自登门拜访，恳请望族出资修缮。此举可积功德、得民心，但费用不菲。",
            choices = {
                { text = "独力承担，刻碑留名（银两-20，布匹-8，声望+18）", cost = {silver = 20, cloth = 8}, result = "族长独力出资，请来匠人大兴修缮。数月之后古刹焕然一新，金碧辉煌，香火更盛。寺门前竖起石碑，镌刻望族捐资功德。四方信众前来焚香礼佛，无不对望族善行赞不绝口。", effect = function() GameData.AddResource("fame", 18) end },
                { text = "联合数家分担费用（银两-8，声望+8）", cost = {silver = 8}, result = "族长出面召集数家乡绅共同出资，各家分摊修缮费用。工程虽不如独力承担那般气派，但古刹也修葺得像模像样。碑上刻了数家之名，望族列于首位，也算留下了一份功德。", effect = function() GameData.AddResource("fame", 8) end },
                { text = "婉言谢绝，不参与此事", result = "族长以族中事务繁忙为由婉拒了高僧。住持失望而去，转求他族捐助。乡邻得知望族坐拥万贯却不肯为佛门出力，暗中颇有微词，说望族只知敛财，不晓积德。", effect = function() GameData.AddResource("fame", -3) end },
            }
        }
    end,
}

-- 13. 培养继承人（家族传承）
events[#events + 1] = {
    id = "r4_heir_training",
    title = "培养继承人",
    rankRange = {4, 5},
    weight = 8,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local members = GameData.GetAliveMembers()
        for _, m in ipairs(members) do
            if m.alive and m.age >= 10 and m.age <= 20 then return true end
        end
        return false
    end,
    execute = function(s, report)
        local members = GameData.GetAliveMembers()
        local youth = nil
        for _, m in ipairs(members) do
            if m.alive and m.age >= 10 and m.age <= 20 then youth = m; break end
        end
        local name = youth and youth.name or "族中少年"
        report.events[#report.events + 1] = "族老商议为" .. name .. "延请名师"
        GameData.AddLog("族中商议培养继承人事宜")
        return {
            title = "培养继承人", desc = "族老们认为" .. name .. "资质不凡，应当悉心培养，将来继承家业。可延请名师教导，或送往名门大儒处游学。",
            choices = {
                { text = "延请名师，府中教导（银两-18，声望+10）", cost = {silver = 18}, result = "族长不惜重金从府城延请了一位举人出身的名师，在家中设馆授课。名师教授经史子集、诗词文章，又传习礼仪规矩。少年进益神速，数月间谈吐举止已有大家风范，族人皆叹后继有人。", effect = function() GameData.AddResource("fame", 10) end },
                { text = "送往书院游学（银两-12，粮食-5，声望+12）", cost = {silver = 12, grain = 5}, result = "族长备好盘缠口粮，将少年送往府城知名书院就读。书院中名师荟萃、同窗出众，少年得以广交天下英才。数月后传来消息，少年在书院月考中名列前茅，族中上下欣慰不已。", effect = function() GameData.AddResource("fame", 12) end },
                { text = "由族中长辈亲自教导（声望+3）", result = "族中几位饱学长辈轮流教导少年，虽无名师之名，但胜在言传身教、因材施教。长辈们将毕生所学倾囊相授，又以家族兴衰史为教材，少年虽进步不算迅猛，却根基扎实、心性沉稳。", effect = function() GameData.AddResource("fame", 3) end },
            }
        }
    end,
}

-- 14. 书画收藏（文化投资）
events[#events + 1] = {
    id = "r4_art_collection",
    title = "书画收藏",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "有人兜售前朝名家书画真迹"
        GameData.AddLog("古董商携名家书画登门求售")
        return {
            title = "书画收藏", desc = "一位古董商带着数幅前朝名家书画真迹登门求售，言辞恳切。这些书画若是真迹，不仅价值连城，更能彰显望族底蕴与品位。",
            choices = {
                { text = "大量购入，充实家藏（银两-22，声望+15）", cost = {silver = 22}, result = "族长延请鉴赏名家逐一验看，确认皆为真迹后悉数购入。数幅山水花鸟悬于厅堂书房，满室生辉。消息传出后，文人雅士纷纷登门求观，赞叹望族风雅。家藏之丰，在县中已首屈一指。", effect = function() GameData.AddResource("fame", 15) end },
                { text = "精挑细选，只购一二幅（银两-10，声望+6）", cost = {silver = 10}, result = "族长仔细甄别，只挑了两幅笔墨最为精妙的购入收藏。虽不算大手笔，但这两幅佳作悬于书房中颇添雅趣。来访宾客见之，也赞族长眼光独到、品位不俗。", effect = function() GameData.AddResource("fame", 6) end },
                { text = "疑其为赝品，谨慎拒绝", result = "族长对古董商心存疑虑，反复查看后仍不放心，最终婉拒。古董商将书画转售他族，日后据传那几幅确是真迹。族人虽有几分惋惜，但族长的谨慎也免去了上当受骗的风险。", effect = function() end },
            }
        }
    end,
}

-- 15. 大旱之年
events[#events + 1] = {
    id = "r4_drought_severe",
    title = "大旱之年",
    rankRange = {4, 5},
    weight = 5,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "连月不雨，田地龟裂，大旱席卷全县"
        GameData.AddLog("大旱降临，颗粒无收之兆！")
        return {
            title = "大旱之年", desc = "自春入夏滴雨未落，河渠干涸，田地龟裂。族中良田大片绝收，佃户纷纷告急，粮价飞涨，望族亦感粮荒之忧。",
            choices = {
                { text = "开仓放粮救济佃户（粮食-30，声望+15）", result = "族长一声令下打开粮仓，将存粮分给饥饿的佃户和乡邻。虽然自家粮食消耗极大，但佃户们感恩涕零，纷纷表示来年丰收时加倍偿还。望族仁德之名在旱灾中愈发响亮，四方百姓交口称颂。", effect = function() GameData.AddResource("grain", -30); GameData.AddResource("fame", 15) end },
                { text = "高价购粮稳住族中（银两-20，粮食+10）", result = "族长派人四处奔走，不惜高价从外地购入粮食。虽花费银两甚巨，但总算稳住了族中口粮供应。旱灾之中有粮便有底气，族人虽苦犹可支撑，只盼老天早降甘霖。", effect = function() GameData.AddResource("silver", -20); GameData.AddResource("grain", 10) end },
                { text = "紧缩开支，自保为先（粮食-15，声望-8）", result = "族长下令各房紧缩口粮，每日只食两餐稀粥。佃户前来求助也被拒之门外，哀声一片。族中虽勉强熬过旱灾，但佃户多有逃散，邻里也对望族的冷漠颇为不满，声名大损。", effect = function() GameData.AddResource("grain", -15); GameData.AddResource("fame", -8) end },
            }
        }
    end,
}

-- 16. 名门联姻（强强联合）
events[#events + 1] = {
    id = "r4_alliance_marriage",
    title = "名门联姻",
    rankRange = {4, 5},
    weight = 9,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local members = GameData.GetAliveMembers()
        for _, m in ipairs(members) do
            if m.alive and m.age >= 16 and m.age <= 28 and not m.spouseId then return true end
        end
        return false
    end,
    execute = function(s, report)
        local members = GameData.GetAliveMembers()
        local candidate = nil
        for _, m in ipairs(members) do
            if m.alive and m.age >= 16 and m.age <= 28 and not m.spouseId then candidate = m; break end
        end
        local name = candidate and candidate.name or "族人"
        report.events[#report.events + 1] = "外地名门望族遣媒提亲，欲与" .. name .. "联姻"
        GameData.AddLog("名门联姻之议传来，为" .. name .. "说亲")
        return {
            title = "名门联姻", desc = "远方一世家大族遣人提亲，欲与" .. name .. "联姻。对方门第显赫，联姻可结两族之好。是否为" .. name .. "前去相看？",
            choices = {
                { text = "前去相看", result = "族长遣人备了彩礼前往相看，对方家风端正、门第相当，双方长辈一见即合。择定良辰吉日，六礼齐备，喜轿迎亲。联姻之后两族往来密切，互为援引，实乃两全其美之事。", effect = function()
                    if candidate and not candidate.spouseId then
                        local GS = require("UI.GameScreen")
                        GS.ShowMarriageTierSelect(candidate)
                    end
                end },
                { text = "婉拒提亲（声望-3）", result = "族长思虑再三，觉得对方虽门第显赫但路途遥远，日后走动不便，遂婉言谢绝。媒人悻悻而去，对方颇感不快。此事传出后，乡里说望族眼高于顶，连名门都看不上，名声略有折损。", effect = function() GameData.AddResource("fame", -3) end },
            }
        }
    end,
}

-- 17. 矿业投资（开矿冒险）
events[#events + 1] = {
    id = "r4_mining_venture",
    title = "矿业投资",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "有人来报山中发现矿脉，邀族中合股开采"
        GameData.AddLog("山中发现矿脉，族人商议投资开矿")
        return {
            title = "矿业投资", desc = "族中一位远亲在深山勘探到一处矿脉，据称储量丰富。开矿需投入大量人力银两，若成可获厚利，若败则血本无归。",
            choices = {
                { text = "大举投资，独占矿利（银两-25，银两+30，粮食-10）", cost = {silver = 25, grain = 10}, result = "族长拍板独资开矿，调派大批人手入山采掘。果然矿脉丰厚，铜矿源源不断地开采出来。炼铜售铜获利丰厚，大批银两入账。虽耗费了人力粮食，但回报远超预期，族中财力大为充裕。", effect = function() GameData.AddResource("silver", 30) end },
                { text = "小额入股，分散风险（银两-10，银两+12）", cost = {silver = 10}, result = "族长只出了少量银两入股，与几家合伙开矿。矿脉虽有出产，但分到各家利润不算丰厚。好在本金不大，即便有闪失也不至于伤筋动骨，算是稳中有赚。", effect = function() GameData.AddResource("silver", 12) end },
                { text = "不参与此等冒险之事", result = "族长认为开矿之事变数太大，一旦矿脉枯竭便是血本无归，遂断然拒绝。远亲只好另寻合伙之人。族中虽无损失，但也错过了一次可能的获利良机。", effect = function() end },
            }
        }
    end,
}

-- 18. 发现内奸（族中叛徒）
events[#events + 1] = {
    id = "r4_spy_discovered",
    title = "发现内奸",
    rankRange = {4, 5},
    weight = 6,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        return #adults >= 5
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中查出有人暗中向对头家族通风报信"
        GameData.AddLog("内奸暴露！有族人通敌")
        return {
            title = "发现内奸", desc = "族中管事偶然截获一封密信，发现有族人暗中将族中田产、库存、人丁等机密告知对头家族。消息一出，族内人心惶惶。",
            choices = {
                { text = "严惩不贷，以儆效尤（声望+10）", result = "族长在祠堂前当众宣布内奸罪行，按族规施以重罚，逐出宗族永不叙用。众族人见此雷厉风行之举，无不凛然生畏。自此族中上下戒慎恐惧，再无人敢生二心，族规之威信大大增强。", effect = function() GameData.AddResource("fame", 10) end },
                { text = "私下处置，稳定人心（银两-5，声望+3）", cost = {silver = 5}, result = "族长将内奸叫到密室训诫，罚其退回所得赃银，勒令闭门思过。此事不曾声张，只有少数族老知晓。虽未造成族中动荡，但处置过轻，暗中仍有人议论族长心慈手软。", effect = function() GameData.AddResource("fame", 3) end },
                { text = "将计就计，散布假情报（银两-8，声望+12）", cost = {silver = 8}, result = "族长不动声色，暗中授意内奸继续传信，但将族中情报全部替换为虚假消息。对头家族信以为真，依据假情报行事，处处碰壁狼狈不堪。待真相大白后，对头方知中计，对望族又敬又惧。", effect = function() GameData.AddResource("fame", 12) end },
            }
        }
    end,
}

-- 19. 盛大庆典（排场花费）
events[#events + 1] = {
    id = "r4_festival_grand",
    title = "盛大庆典",
    rankRange = {4, 5},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中长辈大寿或节庆将至，需操办盛大庆典"
        GameData.AddLog("庆典将至，族中筹备事宜")
        return {
            title = "盛大庆典", desc = "族中长辈逢整寿大庆，又恰逢佳节，各方亲朋故旧皆要前来贺寿。身为望族，排场不可过于寒酸，否则有失体面。",
            choices = {
                { text = "大操大办，广邀宾客（银两-20，粮食-15，布匹-10，声望+15）", cost = {silver = 20, grain = 15, cloth = 10}, result = "庄园内外张灯结彩，戏台搭起连唱三日大戏。流水席从早摆到晚，珍馐美馔应有尽有，远近宾客逾百人前来贺寿。长辈笑逐颜开，族人与有荣焉。此番排场传遍四乡，皆赞望族气派非凡。", effect = function() GameData.AddResource("fame", 15) end },
                { text = "排场适中，不失体面（银两-10，粮食-8，声望+6）", cost = {silver = 10, grain = 8}, result = "摆了十余桌酒席，请了一班小戏助兴。菜肴虽非山珍海味，但也精心备办、荤素齐全。来贺宾客三四十人，席间欢声笑语不断。排场虽不算盛大，但也中规中矩，不失望族体面。", effect = function() GameData.AddResource("fame", 6) end },
                { text = "从简办理，不铺张浪费（银两-3，粮食-3，声望-3）", result = "只在家中摆了几桌便饭，未请戏班，也未广邀宾客。族中长辈虽口中说简朴为好，但神色间难掩失落。有亲戚来贺见此冷清场面，暗中摇头叹息，说望族大不如前。", effect = function() GameData.AddResource("silver", -3); GameData.AddResource("grain", -3); GameData.AddResource("fame", -3) end },
            }
        }
    end,
}

-- 20. 仓库失火
events[#events + 1] = {
    id = "r4_warehouse_fire",
    title = "仓库失火",
    rankRange = {4, 5},
    weight = 5,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中粮仓布库深夜起火，损失惨重"
        GameData.AddLog("大火！仓库失火，损失惨重！")
        return {
            title = "仓库失火", desc = "深夜仓库突然起火，火势凶猛难以扑救。粮食布匹损失大半，浓烟蔽天。有人怀疑是人为纵火，也可能是疏忽大意所致。",
            choices = {
                { text = "追查纵火嫌犯，重建仓库（银两-15，声望+5）", result = "族长请来官府捕快协助追查，一面清理废墟重建仓库。大火烧毁了大量粮食布匹，损失惨重。经查实乃一名怀恨在心的旧仆所为，已被擒获交官法办。新仓拔地而起，加装了防火水缸，族人稍感安心。", effect = function() GameData.AddResource("grain", -25); GameData.AddResource("cloth", -12); GameData.AddResource("silver", -15); GameData.AddResource("fame", 5) end },
                { text = "认赔止损，加强巡防（粮食-25，布匹-12）", result = "火场残骸清理完毕，粮食布匹损失大半，令人心痛不已。族长不再追究起因，只安排族丁加强夜间巡逻，在各仓库旁备置水缸沙袋以防再患。虽未查出真凶，但亡羊补牢总胜于无。", effect = function() GameData.AddResource("grain", -25); GameData.AddResource("cloth", -12) end },
            }
        }
    end,
}

-- 21. 族人扬名（学术成就）
events[#events + 1] = {
    id = "r4_scholar_fame",
    title = "族人扬名",
    rankRange = {4, 5},
    weight = 8,
    cooldownMonths = 8,
    identityMatch = {"scholar"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local scholars = GameData.GetMembersByState("读书")
        for _, m in ipairs(scholars) do
            if m.alive and m.age >= 20 then return true end
        end
        return false
    end,
    execute = function(s, report)
        local scholars = GameData.GetMembersByState("读书")
        local scholar = nil
        for _, m in ipairs(scholars) do
            if m.alive and m.age >= 20 then scholar = m; break end
        end
        local name = scholar and scholar.name or "族人"
        report.events[#report.events + 1] = name .. "所著文章被传抄天下，名动士林"
        GameData.AddLog(name .. "文章流传，名动四方")
        return {
            title = "族人扬名", desc = name .. "潜心著述的文章被一位过路名士偶然读到，大为赞赏，辗转传抄，声名远播。各地学子慕名来访，族中声望大增。",
            choices = {
                { text = "刊印文集广为流传（银两-15，声望+20）", cost = {silver = 15}, result = "族长出资延请刻工，将文章结集刊印数百册，分赠各地书院与名士。文集一出洛阳纸贵，士林争相传阅品评。望族以文名天下，各方来访求学者络绎不绝，声望大振，一时风头无两。", effect = function() GameData.AddResource("fame", 20) end },
                { text = "设学堂收徒讲学（银两-10，粮食-5，声望+15）", result = "族长在庄园旁辟出一间雅舍作为讲堂，置办桌椅笔墨，延请族中才子开坛授课。远近学子慕名而来拜师求学，每日书声琅琅。学堂虽费银粮，但望族崇文重教之风由此传扬开来。", effect = function() GameData.AddResource("silver", -10); GameData.AddResource("grain", -5); GameData.AddResource("fame", 15) end },
            }
        }
    end,
}

-- 22. 边境贸易（与外族通商）
events[#events + 1] = {
    id = "r4_border_trade",
    title = "边境贸易",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = {"merchant"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local merchants = GameData.GetMembersByState("经商")
        for _, m in ipairs(merchants) do
            if m.alive then return true end
        end
        return false
    end,
    execute = function(s, report)
        local merchants = GameData.GetMembersByState("经商")
        local trader = nil
        for _, m in ipairs(merchants) do
            if m.alive then trader = m; break end
        end
        local name = trader and trader.name or "族人"
        report.events[#report.events + 1] = name .. "提议与边疆外族开展互市贸易"
        GameData.AddLog(name .. "倡议边境互市通商")
        return {
            title = "边境贸易", desc = name .. "从边疆商人口中得知，朝廷有意在边境开设互市。若能抢先布局，以茶叶布匹换取外族皮毛良马，利润极为丰厚。",
            choices = {
                { text = "投入重金抢占先机（银两-20，布匹-15，银两+28，粮食+15）", cost = {silver = 20, cloth = 15}, result = "族长当机立断，筹集大批茶叶布匹率商队赶赴边关互市。抢在他人之前与外族商贾搭上了线，以中原丝绸换得大量皮毛和银两，又购入北地粮食运回。此番互市获利丰厚，族中声名也传至塞外。", effect = function() GameData.AddResource("silver", 28); GameData.AddResource("grain", 15) end },
                { text = "少量试探，稳步推进（银两-8，布匹-5，银两+10）", cost = {silver = 8, cloth = 5}, result = "只派了几辆大车携少量布匹前往边关试探行情。虽未赚得大利，但摸清了互市规矩和路线，带回了些许银两。族人对边贸有了初步认知，日后若要大举通商便有了底气。", effect = function() GameData.AddResource("silver", 10) end },
                { text = "边境不安全，暂不涉足", result = "族长顾虑边关兵荒马乱，商队一旦遇到匪盗便血本无归，遂决定按兵不动。数月后传来消息，去边关互市的几家都赚了不少，族中有人暗自惋惜错失良机。", effect = function() end },
            }
        }
    end,
}

-- 23. 严重瘟疫
events[#events + 1] = {
    id = "r4_plague_severe",
    title = "严重瘟疫",
    rankRange = {4, 5},
    weight = 4,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s)
        local members = GameData.GetAliveMembers()
        return #members >= 5
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "瘟疫蔓延全县，望族亦受波及"
        GameData.AddLog("瘟疫来袭！族中人心惶惶")
        return {
            title = "严重瘟疫", desc = "一场来势凶猛的瘟疫从邻县传入，迅速蔓延。族中已有数人出现症状，百姓惊恐万分，纷纷逃离。望族须即刻决断，否则后果不堪设想。",
            choices = {
                { text = "重金延请名医，购药救治（银两-25，粮食-10，声望+18）", result = "族长不惜重金从府城请来一位杏林圣手，又大量购入药材熬煮汤药。名医妙手回春，族中患者相继痊愈，又将余药分赠邻里。瘟疫终于被遏制住，百姓感恩涕零，皆颂望族活人无数之德。", effect = function() GameData.AddResource("silver", -25); GameData.AddResource("grain", -10); GameData.AddResource("fame", 18) end },
                { text = "封闭庄园，自行隔离（粮食-15，声望-5）", result = "族长下令紧闭庄门，不许任何外人出入。族中染病者隔离在偏院，以草药自行调治。虽保住了大部分族人平安，但庄外百姓求助无门，对望族闭门自保颇有怨言。疫后名声有所折损。", effect = function() GameData.AddResource("grain", -15); GameData.AddResource("fame", -5) end },
                { text = "举族迁避，暂离疫区（银两-15，粮食-10，声望-10）", result = "族长连夜组织族人收拾细软，车马载着老幼一路奔赴山中亲戚处避疫。路上颠簸劳顿，粮食消耗不少。留守庄园的仆役无人看管，田产也荒废数月。待疫退归来，族产损失不轻，更有乡邻暗讽望族只顾自家。", effect = function() GameData.AddResource("silver", -15); GameData.AddResource("grain", -10); GameData.AddResource("fame", -10) end },
            }
        }
    end,
}

-- 24. 开垦荒地（大规模垦荒）
events[#events + 1] = {
    id = "r4_land_reclaim",
    title = "开垦荒地",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local farmers = GameData.GetMembersByState("务农")
        return #farmers >= 1
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中发现大片可垦荒地，商议开垦"
        GameData.AddLog("发现荒地良田之资，族中议垦荒事")
        return {
            title = "开垦荒地", desc = "族中长工在山脚下发现大片荒地，土质肥沃，引水便利，若加以开垦可得良田百亩。但开垦需投入大量人力银钱，且需向官府报备纳税。",
            choices = {
                { text = "全力开垦，广置田产（银两-20，粮食-15，粮食+40，声望+8）", cost = {silver = 20, grain = 15}, result = "族长调集百余人手浩浩荡荡进山开垦。砍树除草、翻土筑埂、引水修渠，数月辛劳终于将荒地变为良田百亩。秋收时稻谷金黄满仓，族中粮食充裕。官府得报亦嘉许望族开荒之功。", effect = function() GameData.AddResource("grain", 40); GameData.AddResource("fame", 8) end },
                { text = "小范围试垦（银两-8，粮食-5，粮食+15）", cost = {silver = 8, grain = 5}, result = "只派了二十余人先垦一小片试种。虽规模不大，但收成尚可，证实了这片荒地确实土质肥沃。族人看到了实实在在的收获，日后或可再行扩展。", effect = function() GameData.AddResource("grain", 15) end },
                { text = "暂不动土，留待日后", result = "族长觉得当下人力财力有限，开垦之事不急于一时，遂将此议搁置。荒地依旧荒芜，只待来日时机成熟再行垦殖。族中虽无损失，但长工们颇觉可惜。", effect = function() end },
            }
        }
    end,
}

-- 25. 天恩浩荡（获得朝廷恩惠）
events[#events + 1] = {
    id = "r4_court_favor",
    title = "天恩浩荡",
    rankRange = {4, 5},
    weight = 5,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "朝廷嘉奖地方望族，御赐匾额赏赐"
        GameData.AddLog("天恩浩荡！圣上御赐匾额嘉奖")
        return {
            title = "天恩浩荡", desc = "朝廷表彰各地积极赈灾、教化乡里的望族，你族名列其中。圣上御笔亲题匾额，赐下金银绸缎以为嘉勉。此乃光宗耀祖之盛事。",
            choices = {
                { text = "叩谢天恩，大肆庆贺（银两+25，布匹+20，声望+20，粮食-10）", cost = {grain = 10}, result = "圣旨到日族中设香案跪迎，御赐匾额高悬厅堂之上，金光灿烂。族长大办庆宴，杀猪宰羊宴请乡邻百姓同沐天恩。圣上赐下的银两绸缎入了库房，族中上下欢天喜地。此等殊荣实乃光宗耀祖、百年难遇之盛事。", effect = function() GameData.AddResource("silver", 25); GameData.AddResource("cloth", 20); GameData.AddResource("fame", 20) end },
                { text = "谦逊领受，低调行事（银两+25，布匹+20，声望+12）", result = "族长恭敬领旨谢恩，将御赐匾额妥善安置，赏赐银绸入库收好。不设大宴，只在族中内部告知此喜讯。乡邻虽未见盛大庆典，但御匾悬挂之处人人可见，望族受朝廷嘉许之事自然传开，声名更著。", effect = function() GameData.AddResource("silver", 25); GameData.AddResource("cloth", 20); GameData.AddResource("fame", 12) end },
            }
        }
    end,
}

-- 26. 修缮族谱（宗族文化建设）
events[#events + 1] = {
    id = "r4_genealogy",
    title = "修缮族谱",
    rankRange = {4, 5},
    weight = 7,
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
        report.events[#report.events + 1] = "族中长辈倡议重修族谱，追溯先祖功德"
        GameData.AddLog("族谱修缮工程正式启动")
        return {
            title = "修缮族谱",
            desc = "族中现有族谱年久失修，许多旁支世系记载模糊，先祖事迹亦多有遗漏。族中几位饱学长辈联名提议，延请善书法者重新编修族谱，刊刻印行，分赐各房留存。此举可凝聚族心，彰显家风，但修谱工程浩大，所费不赀。",
            choices = {
                { text = "精工细作，刊刻传世（银两-20，布匹-5，声望+18）", cost = {silver = 20, cloth = 5}, result = "延请善书法者历时半年精心编修，追溯先祖十余代功德事迹，旁支世系一一厘清。又以上等宣纸刊刻印行百余册，装帧典雅，分赐各房珍藏。阖族上下感念先祖遗泽，凝聚之心更胜从前。", effect = function()
                    GameData.AddResource("fame", 18)
                    GameData.AddLog("族谱修缮告竣，刊刻精美，分赐各房，阖族称颂")
                end },
                { text = "简单整理，手抄存档（银两-6，声望+6）", cost = {silver = 6}, result = "族中几位长辈亲自执笔，将现有族谱中模糊不清之处修订补全，又增添了近年新生子嗣的记录。虽是手抄本不甚华美，但世系清晰、记载翔实，亦可传之后世不致遗忘。", effect = function()
                    GameData.AddResource("fame", 6)
                    GameData.AddLog("族谱简要修订完毕，虽不华美，亦可传世")
                end },
                { text = "暂缓修谱，待时机成熟再议", result = "族长以近来族务繁忙、银钱吃紧为由暂且搁置修谱之议。几位提议的族老颇为不满，认为族谱乃宗族根本，岂可因小失大。各房之间的嫌隙也因此事更添了几分。", effect = function()
                    GameData.AddResource("fame", -2)
                    GameData.AddLog("修谱之议暂且搁置，部分族老颇有微词")
                end },
            }
        }
    end,
}

-- 27. 水利兴修（灌溉工程）
events[#events + 1] = {
    id = "r4_irrigation",
    title = "水利兴修",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local farmers = GameData.GetMembersByState("务农")
        return #farmers >= 1
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "春耕在即，族老提议疏浚河渠、修筑堤坝以利灌溉"
        GameData.AddLog("水利兴修工程提上日程")
        return {
            title = "水利兴修",
            desc = "族中田产日广，但灌溉水源多赖一条年久失修的旧渠。春耕将至，族老提议联合附近数村共同出资疏浚河道、修筑拦水坝、开挖新渠，以保旱涝保收。此举若成，良田可增产三成，但工程浩大须调动大量人力银钱。",
            choices = {
                { text = "牵头主导，出大头银钱（银两-25，粮食-10，粮食+35，声望+15）", cost = {silver = 25, grain = 10}, result = "族长一声号令，调集数百人手大兴水利。疏浚旧渠、修筑拦水坝、开挖新渠引水入田，历时两月竣工。自此良田灌溉无忧，旱涝保收，粮产大增。周边村落亦受其惠，对望族感恩戴德。", effect = function()
                    GameData.AddResource("grain", 35)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("水利工程竣工，良田灌溉无忧，周边村落感恩戴德")
                end },
                { text = "与各村均摊费用（银两-10，粮食+15，声望+8）", cost = {silver = 10}, result = "族长出面召集附近各村里正商议，约定按田亩多少均摊水利费用。工程虽不如独力承担那般宏大，但也将旧渠修缮一新，灌溉之利各村共享。族人出力居中协调，赢得了乡里好评。", effect = function()
                    GameData.AddResource("grain", 15)
                    GameData.AddResource("fame", 8)
                    GameData.AddLog("水渠修缮完毕，各村共享其利")
                end },
                { text = "只修自家田地的引水沟（银两-5，粮食+8）", result = "族长只顾修了一条引水沟将河水引入自家田地，邻村的田却依旧缺水。虽然自家粮产有所增加，但邻村百姓颇有怨言，说望族自私自利，只管自家不顾旁人。", effect = function()
                    GameData.AddResource("silver", -5)
                    GameData.AddResource("grain", 8)
                    GameData.AddLog("自家田地水渠修好，邻村颇有怨言")
                end },
            }
        }
    end,
}

-- 28. 税赋加重（朝廷加派）
events[#events + 1] = {
    id = "r4_heavy_tax",
    title = "税赋加重",
    rankRange = {4, 5},
    weight = 6,
    cooldownMonths = 10,
    identityMatch = nil,
    era = {1580, 1644},
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "朝廷加派辽饷、剿饷，赋税骤增，百姓苦不堪言"
        GameData.AddLog("朝廷加派赋税！族中上下叫苦不迭")
        return {
            title = "税赋加重",
            desc = "边疆战事频仍，朝廷财用匮乏，遂在正税之外加派\"辽饷\"。地方官吏层层摊派，望族首当其冲。族中田产广大，分摊的加派银两数额惊人。若不缴纳恐遭查抄，若如数上缴又伤筋动骨。",
            choices = {
                { text = "如数缴纳，以保平安（银两-30，粮食-15）", result = "族长忍痛将仓中银两和粮食如数上缴，差役清点完毕扬长而去。族中上下一片愁云，几房长辈聚在堂前长吁短叹，只盼朝廷早日平定边患，免去这加派之苦。", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("grain", -15)
                    GameData.AddLog("加派赋税如数上缴，族中财力大损")
                end },
                { text = "上下打点减免部分（银两-20，声望-5）", result = "族长遣人带着银两四处打点，从县衙书吏到粮长里长一一疏通，总算将加派额减去两成。虽省了些银钱，但这般行径传开后，乡邻背地里议论望族只顾自家，有失体面。", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("托人打点求减免，虽省些银钱，名声却受损")
                end },
                { text = "联合乡绅上书请愿减税（银两-10，声望+5）", result = "族长串联县中十余家乡绅联名上书，恳请巡抚大人酌减加派。联名书辗转呈至省城，巡抚念民情艰难，酌减三分。虽然减免不多，但望族牵头请愿之举深得民心。", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("联合各家上书请愿，朝廷酌减三分，族中仍感沉重")
                end },
            }
        }
    end,
}

-- 29. 私塾兴学（创办义学）
events[#events + 1] = {
    id = "r4_charity_school",
    title = "私塾兴学",
    rankRange = {4, 5},
    weight = 8,
    cooldownMonths = 10,
    identityMatch = {"scholar"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local scholars = GameData.GetMembersByState("读书")
        for _, m in ipairs(scholars) do
            if m.alive and m.age >= 25 then return true end
        end
        return false
    end,
    execute = function(s, report)
        local scholars = GameData.GetMembersByState("读书")
        local teacher = nil
        for _, m in ipairs(scholars) do
            if m.alive and m.age >= 25 then teacher = m; break end
        end
        local name = teacher and teacher.name or "族人"
        report.events[#report.events + 1] = name .. "提议在族中创办义学，教导乡里子弟读书识字"
        GameData.AddLog(name .. "倡办义学，惠及乡里")
        return {
            title = "私塾兴学",
            desc = name .. "学问精进，有感于乡里子弟多不识字，提议在族中祠堂旁修建学堂，不仅教导本族子弟，也接纳周边贫家孩童免费入学。此举可广播文教、收揽人心，但需长期投入师资与笔墨纸砚。",
            choices = {
                { text = "全力支持，建学堂聘名师（银两-20，声望+18）", cost = {silver = 20}, result = "新学堂落成之日，族长亲书\"崇文尚学\"匾额高悬堂上。延聘的名师学识渊博，十里八乡的孩童纷纷前来就读。书声琅琅传出学堂，乡邻皆赞望族乃积德行善之家，文教之名远播县中。", effect = function()
                    GameData.AddResource("fame", 18)
                    GameData.AddLog("义学落成，书声琅琅，四方赞颂")
                end },
                { text = "在祠堂偏厅开课，规模从简（银两-8，声望+8）", cost = {silver = 8}, result = "祠堂偏厅收拾整洁，摆上桌案板凳便是学堂。虽无宏大规模，却也窗明几净。族中一位老秀才充任塾师，每日教导十余个孩童诵读识字，乡邻闻之感念不已。", effect = function()
                    GameData.AddResource("fame", 8)
                    GameData.AddLog("义学开办，虽规模不大，亦得乡邻感念")
                end },
                { text = "只教本族子弟，不对外开放（银两-5，声望+3）", result = "族中拨出银两在后院辟出一间书房，只供本族子弟就读。外乡孩童闻讯前来求学却被婉拒，族长说先顾好自家子弟再论其他。虽有人说望族格局不大，但族中子弟确实受益匪浅。", effect = function()
                    GameData.AddResource("silver", -5)
                    GameData.AddResource("fame", 3)
                    GameData.AddLog("族内私塾开课，仅限本族子弟就读")
                end },
            }
        }
    end,
}

-- 30. 贪官索贿（官吏勒索）
events[#events + 1] = {
    id = "r4_corrupt_official",
    title = "贪官索贿",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "新任县丞贪婪成性，借故向望族索要孝敬"
        GameData.AddLog("贪官登门索贿，族中进退两难")
        return {
            title = "贪官索贿",
            desc = "新任县丞到任未几，便四处搜刮民脂民膏。此人盯上族中产业，借查验田契之名行勒索之实，暗示若不\"孝敬\"便在赋税上做文章。此人虽品级低微，却手握实权，不可小觑。",
            choices = {
                { text = "忍气吞声，奉上银两打点（银两-18）", cost = {silver = 18}, result = "族长无奈命人将银两装匣送至县丞私宅，那贪官收下银两满面堆笑，拍着胸脯说日后自会关照。族中长辈虽觉屈辱，但也知小不忍则乱大谋，只盼此人早日调任他处。", effect = function()
                    GameData.AddLog("无奈缴纳孝敬银两，贪官暂时满意离去")
                end },
                { text = "联合士绅向知府告状（银两-12，声望+10）", cost = {silver = 12}, result = "族长暗中串联县中数位德高望重的士绅，联名写了一纸诉状呈递知府衙门。知府查实后将县丞申饬一番，那贪官虽暂时收敛，却暗中怀恨在心。望族仗义执言之举深得乡邻敬重。", effect = function()
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("联名状递上知府衙门，贪官被申饬，暗中怀恨")
                end },
                { text = "硬顶到底，拒不行贿（声望+5，粮食-10）", result = "族长正色回绝了县丞的索贿，声称族中从不行此苟且之事。那贪官恼羞成怒，回去后便在赋税征收上处处刁难，多派了不少粮食。族人虽吃了暗亏，但清白之名却得乡邻称许。", effect = function()
                    GameData.AddResource("fame", 5)
                    GameData.AddResource("grain", -10)
                    GameData.AddLog("拒绝行贿，贪官恼羞成怒，暗中在赋税上使绊子")
                end },
            }
        }
    end,
}

-- 31. 走私风波（私盐/茶引违禁）
events[#events + 1] = {
    id = "r4_smuggling",
    title = "走私风波",
    rankRange = {4, 5},
    weight = 5,
    cooldownMonths = 12,
    identityMatch = {"merchant"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local merchants = GameData.GetMembersByState("经商")
        for _, m in ipairs(merchants) do
            if m.alive then return true end
        end
        return false
    end,
    execute = function(s, report)
        local merchants = GameData.GetMembersByState("经商")
        local suspect = nil
        for _, m in ipairs(merchants) do
            if m.alive then suspect = m; break end
        end
        local name = suspect and suspect.name or "族人"
        report.events[#report.events + 1] = name .. "被人举报走私私盐，官府前来查问"
        GameData.AddLog("走私风波！" .. name .. "被举报贩卖私盐")
        return {
            title = "走私风波",
            desc = "有人向官府举报" .. name .. "在经商途中夹带私盐贩卖。巡检司派人前来查问，虽未搜出实证，但风声已传遍全县。私盐之罪轻则罚没家产，重则发配充军，族中上下惶恐不安。",
            choices = {
                { text = "重金疏通官府，将此事压下（银两-25，声望-5）", cost = {silver = 25}, result = "族长连夜命人带着银两分别送往巡检司和县衙，上下疏通打点。几日后风声渐息，举报之事不了了之。虽破财消灾，但坊间议论纷纷，都说望族花钱买平安，名声多少受了些损。", effect = function()
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("银两上下打点，走私之事被压下，但名声有损")
                end },
                { text = "配合调查以证清白（声望+8）", result = "族长亲自带领族人打开仓库货栈任凭官府搜查，又将历年往来账目一一呈上。巡检司查验数日，未发现任何私盐痕迹，遂认定系他人诬告。清白昭雪之后，望族光明磊落之名反而更加响亮。", effect = function()
                    GameData.AddResource("fame", 8)
                    GameData.AddLog("坦然配合官府调查，证实是他人诬告，清白昭雪")
                end },
                { text = "主动交出部分货物表诚意（银两-12，布匹-8）", cost = {silver = 12, cloth = 8}, result = "族长将商号中部分货物主动缴交官府以表清白诚意，又附上银两作为查验费用。巡检司见望族态度诚恳，草草结案不再深究。虽损失了些货物银钱，但总算平安了事。", effect = function()
                    GameData.AddLog("主动缴出货物以示诚意，官府不再追究")
                end },
            }
        }
    end,
}

-- 32. 族人犯法（宗族纪律）
events[#events + 1] = {
    id = "r4_clan_crime",
    title = "族人犯法",
    rankRange = {4, 5},
    weight = 6,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        return #adults >= 4
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中一名子弟酒后伤人，被捕入狱，牵连全族名声"
        GameData.AddLog("族人犯法！酒后闹事伤人被拿入衙门")
        return {
            title = "族人犯法",
            desc = "族中一名年轻子弟在县城酒肆与人争执，酒后失手将对方打伤。伤者家属告到衙门，县令将该子弟收押候审。身为望族，此事若处置不当，不仅声名受损，还可能被对头家族借题发挥。",
            choices = {
                { text = "出银两赔偿伤者，息事宁人（银两-18，声望-3）", cost = {silver = 18}, result = "族长遣管事携银两前往伤者家中赔礼道歉，又托中人从中调停。伤者家属收了银两撤了状子，犯事子弟被放回。虽然事情平息，但坊间都说望族仗势欺人后又拿钱了事，名声不免有损。", effect = function()
                    GameData.AddResource("fame", -3)
                    GameData.AddLog("赔偿伤者银两，族人释放，但名声略损")
                end },
                { text = "依族规先行惩戒，再向官府求情（银两-10，声望+5）", cost = {silver = 10}, result = "族长在祠堂当着全族之面对犯事子弟施以家法，杖责三十板。随后携银两赴县衙赔情，请县令念在已受家法惩处份上从轻发落。县令见望族家教严明，准予具保释放，众人皆服。", effect = function()
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("先行族规家法惩处，再赴衙门赔情，县令从轻发落")
                end },
                { text = "大义灭亲，任由官府处置（声望+10）", result = "族长铁面无私，传话衙门说此子既犯国法，族中绝不偏袒，任凭官府依律处置。此言一出，县令和乡邻皆惊，纷纷赞叹望族大义灭亲、法度森严。清正之名一时传遍全县。", effect = function()
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("族长大义灭亲，不偏袒犯事子弟，清正之名传遍乡里")
                end },
            }
        }
    end,
}

-- 33. 修桥铺路（公共善举）
events[#events + 1] = {
    id = "r4_build_bridge",
    title = "修桥铺路",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "乡里一座石桥年久坍塌，来往行人叫苦不迭，望族被请出面修缮"
        GameData.AddLog("修桥铺路善举提上日程")
        return {
            title = "修桥铺路",
            desc = "连接县城与乡里的一座石桥在暴雨中坍塌，来往商旅行人苦不堪言。县令无力拨款，百姓推举望族牵头修缮。此乃积德行善、扬名立万之机，但造桥铺路耗费甚巨。",
            choices = {
                { text = "独力出资修建石桥，刻碑纪念（银两-22，声望+20）", cost = {silver = 22}, result = "历时两月，一座坚固的五孔石桥横跨河面，桥头竖起石碑镌刻望族之名。通桥之日商旅行人络绎过桥，无不驻足称赞。县令闻讯亲笔题匾\"义举流芳\"，望族善名远播数十里。", effect = function()
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("新桥落成，碑文镌刻族名，行人交口称赞")
                end },
                { text = "号召各家分摊费用共同修建（银两-10，声望+10）", cost = {silver = 10}, result = "族长出面号召各望族大户共同出资修桥，自家带头捐银十两。数家合力之下新桥很快修成，虽非独力之功，但望族牵头倡议之举亦得乡邻好评，行人过桥时都念着几家大户的好处。", effect = function()
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("新桥合力修成，虽非独功，亦得好评")
                end },
                { text = "仅捐少量银两意思一下（银两-3，声望-2）", result = "族长只象征性地捐了三两银子便不再过问，其余人家见望族如此也都缩手不前。石桥迟迟未能动工，行人仍需绕远涉水。乡邻私下都说堂堂望族竟如此吝啬，名声大不如前。", effect = function()
                    GameData.AddResource("silver", -3)
                    GameData.AddResource("fame", -2)
                    GameData.AddLog("捐银寥寥，乡邻暗中议论望族吝啬")
                end },
            }
        }
    end,
}

-- 34. 商号扩张（开设分号）
events[#events + 1] = {
    id = "r4_shop_expansion",
    title = "商号扩张",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = {"merchant"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local merchants = GameData.GetMembersByState("经商")
        for _, m in ipairs(merchants) do
            if m.alive and m.age >= 20 then return true end
        end
        return false
    end,
    execute = function(s, report)
        local merchants = GameData.GetMembersByState("经商")
        local manager = nil
        for _, m in ipairs(merchants) do
            if m.alive and m.age >= 20 then manager = m; break end
        end
        local name = manager and manager.name or "族人"
        report.events[#report.events + 1] = name .. "提议在府城开设分号，将生意做大"
        GameData.AddLog(name .. "谋划商号扩张，进军府城")
        return {
            title = "商号扩张",
            desc = name .. "在县城经营的商号生意兴隆，他提议趁势在府城开设分号，经营绸缎布匹生意。府城商业繁华，竞争也更为激烈，既有大好机遇也有不小风险。",
            choices = {
                { text = "全力支持，投入重金开设分号（银两-25，布匹-10，银两+20，声望+10）", cost = {silver = 25, cloth = 10}, result = "府城繁华街市上，望族商号的金字招牌高高挂起。开张之日鞭炮齐鸣，四方客商纷纷前来道贺。上等绸缎摆满柜台，买主络绎不绝，头月便获利颇丰。望族商号之名从此在府城站稳了脚跟。", effect = function()
                    GameData.AddResource("silver", 20)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("府城分号开张大吉，生意红火，族中声势大振")
                end },
                { text = "小规模试营，先站稳脚跟（银两-12，银两+8，声望+3）", cost = {silver = 12}, result = "在府城一条偏僻街巷中租了个小铺面，挂起招牌试营。起初门庭冷落，但所售布匹质优价廉，渐渐有了回头客。虽规模不大利润不厚，却也算在府城扎下了根基，日后或可慢慢做大。", effect = function()
                    GameData.AddResource("silver", 8)
                    GameData.AddResource("fame", 3)
                    GameData.AddLog("府城小店开张，虽不起眼，慢慢也有了口碑")
                end },
                { text = "守住县城本业，不冒进", result = "族长思量再三，觉得府城水深龙多，贸然进军恐怕血本无归。于是回绝了分号之议，嘱咐众人守好县城本业，稳扎稳打方为上策。虽错过了机遇，但也避开了风险。", effect = function()
                    GameData.AddLog("族长认为稳守县城为宜，分号之议暂且搁置")
                end },
            }
        }
    end,
}

-- 35. 蝗灾肆虐（天灾）
events[#events + 1] = {
    id = "r4_locust_plague",
    title = "蝗灾肆虐",
    rankRange = {4, 5},
    weight = 4,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "漫天蝗虫铺天盖地而来，禾苗顷刻尽毁！"
        GameData.AddLog("蝗灾降临！庄稼颗粒无收！")
        return {
            title = "蝗灾肆虐",
            desc = "入夏以来天气异常干热，忽一日漫天蝗虫从北方涌来，遮天蔽日。数日之间，田间禾苗被啃食殆尽，眼看秋收无望。佃户哭声震天，族中粮仓储备告急，若不及时应对，恐生大乱。",
            choices = {
                { text = "组织族人灭蝗，开仓济民（粮食-25，银两-10，声望+15）", result = "族长一声令下，全族男女老幼齐上阵扑打蝗虫，又开仓放粮赈济周边饥民。连日辛劳之后蝗势渐退，虽然粮仓大半见底、银钱花费不少，但族人同心协力共渡难关，人心反而更加凝聚。", effect = function()
                    GameData.AddResource("grain", -25)
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("全族动员灭蝗救灾，虽损失惨重，但人心凝聚")
                end },
                { text = "抢购外地粮食囤积自保（银两-20，粮食+5）", result = "族长当机立断派人赶往邻县抢购粮食，虽然价格已被哄抬至平日数倍，但好歹买回了几车米粮。族中粮仓勉强续上，不至于断炊。只是花费银两甚多，元气大伤。", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("grain", 5)
                    GameData.AddLog("高价抢购外地粮食，勉强维持族中口粮")
                end },
                { text = "听天由命，紧缩度日（粮食-20，声望-8）", result = "族长束手无策，只得下令全族紧缩用度、省吃俭用。蝗灾过后田间颗粒无收，族人面有菜色，怨声四起。有人暗中说族长无能，连灭蝗都不组织，眼看着粮食被啃光却无所作为。", effect = function()
                    GameData.AddResource("grain", -20)
                    GameData.AddResource("fame", -8)
                    GameData.AddLog("蝗灾过后满目疮痍，族人怨声四起")
                end },
            }
        }
    end,
}

-- 36. 田地争端（土地纠纷）
events[#events + 1] = {
    id = "r4_land_dispute",
    title = "田地争端",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "邻族突然声称族中一块良田原属其祖产，要求归还"
        GameData.AddLog("田产纠纷！邻族来争良田地界")
        return {
            title = "田地争端",
            desc = "邻近一族突然拿出一份陈旧的地契，声称族中东边那块上等水田原本是他家祖产，当年不过是暂借代管，如今要求归还。族中老人翻检旧档，发现此田确系数十年前低价购入，但原始契约字迹模糊，真伪难辨。",
            choices = {
                { text = "据理力争，请县令裁断（银两-12，声望+8）", cost = {silver = 12}, result = "族长携带地契文书赴县衙对簿公堂，又请来几位年迈乡邻作证。县令审理后裁定此田确系数十年前合法购入，归望族所有。邻族虽悻悻而去，但公堂裁断令人信服，望族依法争理之名传开。", effect = function()
                    GameData.AddResource("fame", 8)
                    GameData.AddLog("对簿公堂，县令裁定田地归我族所有，邻族悻悻而去")
                end },
                { text = "各退一步，将田地一分为二（粮食-8，声望+3）", result = "族长亲赴邻族商议，提出各退一步将那块水田一分为二，各得一半。两族在里长见证下重新丈量立契，虽然望族少了半块良田导致粮产减少，但邻里之间和气犹在，也算是体面收场。", effect = function()
                    GameData.AddResource("grain", -8)
                    GameData.AddResource("fame", 3)
                    GameData.AddLog("两族各退一步，田地各得一半，暂且安宁")
                end },
                { text = "慷慨让出田地，以和为贵（粮食-15，声望+12）", cost = {grain = 15}, result = "族长大度地将那块水田整块让与邻族，只说远亲不如近邻，一块田地不值得伤了两家和气。邻族族长感动不已，当众拜谢。此事传开后，乡里无不称赞望族仁义宽厚、不与人争。", effect = function()
                    GameData.AddResource("fame", 12)
                    GameData.AddLog("主动让田与邻族，仁义之名传遍乡里")
                end },
            }
        }
    end,
}

-- 37. 纺织新法（技术革新）
events[#events + 1] = {
    id = "r4_weaving_innovation",
    title = "纺织新法",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return s.cloth >= 10 end,
    execute = function(s, report)
        report.events[#report.events + 1] = "一位外地织工投奔族中，自称掌握一种提花织造新法"
        GameData.AddLog("外地织工来投，带来织造新技")
        return {
            title = "纺织新法",
            desc = "一位从江南逃难而来的织工投奔族中，自称精通一种新式提花织造之法，所织布匹花色精美、质地紧密，远胜本地粗布。若能引入此法，族中纺织产出可大幅提升，但需置办新式织机、培训织工。",
            choices = {
                { text = "重金留人，购置新织机全面推广（银两-20，布匹+25，声望+8）", cost = {silver = 20}, result = "族长重金礼聘那位织工留下传艺，又斥资购入十架新式提花织机。数月之后族中织坊焕然一新，所产绸缎花色精美、质地上乘，县中商贩争相采购，供不应求。望族纺织之名渐渐在府城也有了口碑。", effect = function()
                    GameData.AddResource("cloth", 25)
                    GameData.AddResource("fame", 8)
                    GameData.AddLog("新织法推广成功，族中布匹质量大增，供不应求")
                end },
                { text = "先小范围试验，验证效果（银两-8，布匹+10）", cost = {silver = 8}, result = "族长谨慎起见，先拨出银两购置两架新织机，让织工在偏院中教授几名族中妇女。试织出的布匹果然比旧法精美许多，族长这才放下心来，打算日后再逐步扩大规模。", effect = function()
                    GameData.AddResource("cloth", 10)
                    GameData.AddLog("新织法初步试验成功，产出的布匹确实精美")
                end },
                { text = "婉拒，怕是骗术", result = "族长生性多疑，担心那织工是江湖骗子，便婉言回绝了此人。那织工见望族无意收留，便转投了邻县另一大户。后来听闻那家布匹生意兴隆，族中不免有人暗自惋惜错失良机。", effect = function()
                    GameData.AddLog("族长疑虑重重，婉拒了织工，此人转投他族")
                end },
            }
        }
    end,
}

-- 38. 丧葬大事（长辈仙逝）
events[#events + 1] = {
    id = "r4_funeral",
    title = "丧葬大事",
    rankRange = {4, 5},
    weight = 6,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local members = GameData.GetAliveMembers()
        for _, m in ipairs(members) do
            if m.alive and m.age >= 55 then return true end
        end
        return false
    end,
    execute = function(s, report)
        local members = GameData.GetAliveMembers()
        local elder = nil
        for _, m in ipairs(members) do
            if m.alive and m.age >= 55 then elder = m; break end
        end
        local name = elder and elder.name or "族中长辈"
        report.events[#report.events + 1] = name .. "年事已高，忽患急症，族中须筹备后事"
        GameData.AddLog(name .. "病重，族中忧心忡忡，暗中筹备丧仪")
        return {
            title = "丧葬大事",
            desc = name .. "德高望重，近日忽染沉疴，卧床不起。族中须提前筹备丧仪事宜——身为望族，丧葬排场不可失礼，但也不宜铺张过甚招人非议。此外还需安排后事分家等敏感事项。",
            choices = {
                { text = "按望族规格隆重办理（银两-22，粮食-10，布匹-8，声望+15）", cost = {silver = 22, grain = 10, cloth = 8}, result = "丧仪按望族最高规格操办，白幡素帐绵延数丈，鼓乐哀鸣震动四野。各方宾客亲朋纷纷前来吊唁，灵前祭品丰盛，出殡之日送葬队伍绵延里许。四方皆赞望族孝道纯厚、礼数周全，尽显大家风范。", effect = function()
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("丧仪隆重庄严，四方宾客来吊，尽显望族风范")
                end },
                { text = "体面但不奢靡，重在心意（银两-10，粮食-5，声望+6）", cost = {silver = 10, grain = 5}, result = "丧仪庄重而不铺张，棺木用料讲究但不奢华，祭品齐备而不靡费。族人亲友前来吊唁时皆感主家用心至诚，既不失望族体面又不落奢靡之讥。长辈含笑九泉，后辈俯仰无愧。", effect = function()
                    GameData.AddResource("fame", 6)
                    GameData.AddLog("丧仪庄重有度，族人亲友皆感其诚")
                end },
                { text = "从简从俭，遵其遗愿（银两-5，声望-3）", result = "族长声称遵从长辈遗愿一切从简，丧仪只摆了三日便草草收场。虽有人称赞不事铺张是难得的节俭之风，但更多族老暗中不满，说堂堂望族办出这等寒酸丧事，叫外人看了笑话。", effect = function()
                    GameData.AddResource("silver", -5)
                    GameData.AddResource("fame", -3)
                    GameData.AddLog("丧仪从简，有人称赞节俭，也有人暗讽寒酸")
                end },
            }
        }
    end,
}

-- 39. 义仓储粮（备荒积谷）
events[#events + 1] = {
    id = "r4_charity_granary",
    title = "义仓储粮",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return s.grain >= 40 end,
    execute = function(s, report)
        report.events[#report.events + 1] = "县令倡议各望族设立义仓，储粮备荒以济饥民"
        GameData.AddLog("县令倡设义仓，族中商议响应与否")
        return {
            title = "义仓储粮",
            desc = "县令有感于近年灾荒频仍，倡议各望族大户设立义仓，丰年储粮、灾年放赈。此举利民利己——义仓可得官府旌表，灾时亦可优先自保，但需拿出大量存粮投入。",
            choices = {
                { text = "慷慨捐粮，设立大型义仓（粮食-25，声望+18）", cost = {grain = 25}, result = "望族独力出资在镇中修建了一座宽敞的义仓，将二十五石存粮悉数搬入。县令亲临巡视，当场亲笔题写\"积善之家\"匾额悬于仓门。百姓奔走相告，都说有望族在此，灾年便有了依靠。", effect = function()
                    GameData.AddResource("fame", 18)
                    GameData.AddLog("义仓拔地而起，粮满仓实，县令亲题匾额嘉许")
                end },
                { text = "适量捐助，量力而行（粮食-12，声望+8）", cost = {grain = 12}, result = "族长量力而行，捐出十二石粮食存入义仓，既响应了县令号召又未伤及族中根本。虽不如其他大户捐得多，但也算尽了心力，县令记录在案以示嘉许。", effect = function()
                    GameData.AddResource("fame", 8)
                    GameData.AddLog("义仓中捐入部分存粮，聊尽绵力")
                end },
                { text = "口头支持，实则观望", result = "族长在县令面前满口答应要捐粮设仓，回去后却迟迟不见行动。日子一久县令明白了望族不过是敷衍了事，面上虽不好发作但心中已有不悦，日后公事上恐怕难得他照拂了。", effect = function()
                    GameData.AddResource("fame", -3)
                    GameData.AddLog("未出实粮，只是口头应承，县令面上不悦")
                end },
            }
        }
    end,
}

-- 40. 族规整顿（内部改革）
events[#events + 1] = {
    id = "r4_clan_reform",
    title = "族规整顿",
    rankRange = {4, 5},
    weight = 6,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local members = GameData.GetAliveMembers()
        return #members >= 6
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中人口日增，旧有族规已不敷用，须重新修订"
        GameData.AddLog("族规整顿之议提上日程")
        return {
            title = "族规整顿",
            desc = "族中人口渐多，各房之间因田产分配、子弟教养、婚丧嫁娶等事屡生龃龉。族老们认为旧有族规条目过于简略，不足以调停日益复杂的族务，提议重修族规，明确各房权责、财产分配与赏罚规则。",
            choices = {
                { text = "全面修订族规，请名儒参酌（银两-15，声望+15）", cost = {silver = 15}, result = "族长延请县中一位饱学名儒参与修订，历时月余终成新族规三十六条，涵盖田产分配、子弟教养、婚丧嫁娶、赏罚条例等方方面面。颁行之日在祠堂当众宣读，各房族老纷纷点头称善，此后族务果然有章可循。", effect = function()
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("新族规修订颁行，条理清晰，各房服膺遵行")
                end },
                { text = "在旧规基础上增补条目（银两-5，声望+6）", cost = {silver = 5}, result = "族长召集各房长辈商议，在旧族规基础上增补了十余条新规，主要针对近年常起纠纷的田产分配和婚丧费用做了明确。虽不尽善尽美，但总比从前模糊的旧规要清楚许多。", effect = function()
                    GameData.AddResource("fame", 6)
                    GameData.AddLog("族规增补完毕，虽不尽善尽美，也比从前明晰")
                end },
                { text = "维持旧制不变，以免各房争执", result = "族长怕修订族规引起各房争执，索性搁置此事维持原样。然而问题并未因回避而消失，反而各房因无明确规矩可循，暗中积怨越来越深。有识之士暗叹族长优柔寡断，恐为后患。", effect = function()
                    GameData.AddResource("fame", -3)
                    GameData.AddLog("族规之事不了了之，各房暗中积怨更深")
                end },
            }
        }
    end,
}

-- 41. 官府差役（征调劳役）
events[#events + 1] = {
    id = "r4_corvee_labor",
    title = "官府差役",
    rankRange = {4, 5},
    weight = 6,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        local count = 0
        for _, m in ipairs(adults) do
            if m.alive and m.gender == "男" then count = count + 1 end
        end
        return count >= 2
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "官府征调劳役修筑城墙，望族须出丁出银"
        GameData.AddLog("官府差役令至！征调壮丁修筑城防")
        return {
            title = "官府差役",
            desc = "知县以防备流贼为由，下令征调各家壮丁修缮城墙。望族虽有一定免役特权，但此次征调事关城防安危，拒绝恐招非议。出丁则误农时，交银则负担沉重。",
            choices = {
                { text = "出丁应役，另贴口粮（粮食-12，声望+8）", result = "族中挑选了十名精壮汉子前往城墙工地应役，又自备口粮供他们食用。壮丁们日夜辛劳搬石砌墙，虽然辛苦但族中出丁应役之举深得县令赞许，乡邻也夸望族有担当。", effect = function()
                    GameData.AddResource("grain", -12)
                    GameData.AddResource("fame", 8)
                    GameData.AddLog("族中壮丁应役修城，虽苦犹荣")
                end },
                { text = "交纳代役银免除劳役（银两-15）", cost = {silver = 15}, result = "族长不愿族中壮丁去受那修城之苦，便按朝廷规矩缴纳了代役银两。银子交到县衙，族人免去了劳役之苦，倒也落得清静。只是白白花去不少银子，族中钱袋又瘪了几分。", effect = function()
                    GameData.AddLog("缴银代役，族人免去修城之苦")
                end },
                { text = "出银又出人，大力支持（银两-10，粮食-8，声望+15）", cost = {silver = 10, grain = 8}, result = "族长慷慨出钱又出人，不仅派了壮丁应役，还额外捐银捐粮资助城防工事。县令大为感念，在城楼竣工之日特意点名表彰望族义举，并上报知府请予旌表，望族声望大振。", effect = function()
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("族中出钱出力，县令大为感念，赞为义举")
                end },
            }
        }
    end,
}

-- 42. 流寇犯境（兵荒马乱）
events[#events + 1] = {
    id = "r4_roaming_bandits",
    title = "流寇犯境",
    rankRange = {4, 5},
    weight = 4,
    cooldownMonths = 12,
    identityMatch = nil,
    era = {1627, 1644},
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "流寇大军过境，烧杀抢掠，乡里遭殃"
        GameData.AddLog("流寇犯境！举族上下戒备森严！")
        return {
            title = "流寇犯境",
            desc = "崇祯年间天灾不断，农民揭竿而起。一股数千人的流寇大军沿途劫掠，正向本县逼近。县令无兵可御，急召各望族大户商议守城之策。生死存亡之际，族中须做出决断。",
            choices = {
                { text = "散尽家财招募乡勇守城（银两-30，粮食-20，声望+20）", result = "族长散尽家财招募了三百乡勇，又将粮食分发守城军民。众人齐心协力加固城防、昼夜巡守。流寇大军试探攻城数次未果，见城防严密遂绕城而去。阖县百姓欢呼雀跃，都说若非望族挺身而出全城必遭洗劫。", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("grain", -20)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("散财聚兵，率乡勇守城，流寇绕城而去，阖县感恩")
                end },
                { text = "献出部分财物求流寇过境不扰（银两-20，粮食-15，布匹-10）", result = "族长忍痛将大批银两、粮食和布匹装车送出城外，遣人与流寇头目交涉，恳求他们收了财物便绕城而过。流寇首领见财物丰厚，果然不再攻城转而他去。虽保全了性命，但半辈子积蓄付之东流。", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("grain", -15)
                    GameData.AddResource("cloth", -10)
                    GameData.AddLog("忍痛割财，换得流寇不入城，虽保全性命却损失惨重")
                end },
                { text = "携带细软弃城逃难（声望-15，银两-10）", result = "族长连夜收拾细软携全家仓皇出逃，躲入深山避祸。流寇入城后大肆劫掠，望族庄院被洗劫一空。待流寇退去归来时只见断壁残垣、满目疮痍。乡邻暗讽望族临难逃跑，往日声望荡然无存。", effect = function()
                    GameData.AddResource("fame", -15)
                    GameData.AddResource("silver", -10)
                    GameData.AddLog("仓皇出逃，流寇洗劫城池后离去，归来时家园残破")
                end },
            }
        }
    end,
}

-- 43. 望族推举（举荐入仕）
events[#events + 1] = {
    id = "r4_recommend_office",
    title = "望族推举",
    rankRange = {4, 5},
    weight = 6,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local members = GameData.GetAliveMembers()
        for _, m in ipairs(members) do
            if m.alive and m.age >= 20 and m.age <= 45 and m.study >= 30 then return true end
        end
        return false
    end,
    execute = function(s, report)
        local members = GameData.GetAliveMembers()
        local candidate = nil
        for _, m in ipairs(members) do
            if m.alive and m.age >= 20 and m.age <= 45 and m.study >= 30 then candidate = m; break end
        end
        local name = candidate and candidate.name or "族人"
        report.events[#report.events + 1] = "知府巡视地方，有意从望族子弟中荐举贤才入仕"
        GameData.AddLog("知府有意荐举族中贤才入仕为官")
        return {
            title = "望族推举",
            desc = "知府巡视各县，对" .. name .. "的才学品行颇为赏识，有意向朝廷举荐为官。这是光耀门楣的大好机会，但需准备丰厚的\"程仪\"和拜谒费用，且入仕后族中需支应各种官场开销。",
            choices = {
                { text = "全力运作，务使推举成功（银两-25，布匹-10，声望+20）", cost = {silver = 25, cloth = 10}, result = "族长倾力运作，备下丰厚程仪和绸缎礼品送往知府衙门。知府果然向朝廷荐举，不久任命文书下达，族中子弟正式踏上仕途赴任。消息传回乡里，乡邻纷纷道贺，望族门楣大放光彩。", effect = function()
                    GameData.AddResource("fame", 20)
                    GameData.AddLog(name .. "得知府举荐，踏上仕途，族中声望大振")
                end },
                { text = "适度准备，听凭天命（银两-12，声望+10）", cost = {silver = 12}, result = "族长备了些程仪礼物适度打点，余下便听天由命。推举之事辗转月余，知府虽上了荐书但朝廷迟迟未有回音。不过此番能得知府青眼相加，已是莫大荣耀，族中声望也有所提升。", effect = function()
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("推举之事已尽人事，能否成功尚待消息")
                end },
                { text = "婉拒推举，不愿卷入官场（声望-5）", result = "族长深思熟虑后婉拒了知府的好意，言道族中子弟尚需历练，且不愿涉足官场是非。知府虽感意外但也不便强求，此事就此作罢。乡邻议论纷纷，有人惋惜错失良机，也有人暗讽不识抬举。", effect = function()
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("族长婉拒知府好意，不欲子弟涉足官场是非")
                end },
            }
        }
    end,
}

-- 44. 洪涝灾害（水患）
events[#events + 1] = {
    id = "r4_flood_disaster",
    title = "洪涝灾害",
    rankRange = {4, 5},
    weight = 4,
    cooldownMonths = 12,
    identityMatch = nil,
    era = nil,
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "连日暴雨，河水暴涨溃堤，庄稼田舍尽遭水淹"
        GameData.AddLog("洪涝大灾！河堤决口，家园被淹！")
        return {
            title = "洪涝灾害",
            desc = "盛夏连降暴雨，河水暴涨冲垮堤坝，洪水倒灌田地，低洼处的庄稼和房舍尽数被淹。族中粮仓也进了水，部分存粮被泡烂。灾后还需安置灾民、修复田舍，族中损失极为惨重。",
            choices = {
                { text = "组织族人修堤排水，开仓救济（银两-20，粮食-20，声望+18）", result = "族长亲自卷起裤脚带领族人冒雨修堤排水，又开仓放粮赈济受灾乡邻。经过数日奋战洪水终于退去，虽然粮仓几乎见底、银钱花费甚多，但望族挺身而出的义举传遍四方，百姓感恩戴德。", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("grain", -20)
                    GameData.AddResource("fame", 18)
                    GameData.AddLog("族长亲率族人修堤排水，赈济灾民，义声远播")
                end },
                { text = "先保护族中产业，再顾及他人（银两-10，粮食-10，声望-5）", result = "族长下令先行加固自家庄院和粮仓，调集族人日夜守护族产。虽然损失减半，但周边邻里在洪水中叫天不应，事后颇有微词，说望族只顾自家不管旁人死活，实在令人寒心。", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("grain", -10)
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("先行自保，族产损失减半，但邻里颇有微词")
                end },
                { text = "向官府求援，等待朝廷赈济（粮食-15）", result = "族长修书急报县衙请求官府赈济，然而公文层层上报、层层拖延，赈粮迟迟不至。族人只得靠着日渐减少的存粮苦熬度日，眼看着泡烂的庄稼心如刀绞，盼星星盼月亮般等着朝廷的救命粮。", effect = function()
                    GameData.AddResource("grain", -15)
                    GameData.AddLog("等官府赈济，迟迟不至，族人苦熬度日")
                end },
            }
        }
    end,
}

-- 45. 佃户闹事（租佃纠纷）
events[#events + 1] = {
    id = "r4_tenant_unrest",
    title = "佃户闹事",
    rankRange = {4, 5},
    weight = 6,
    cooldownMonths = 8,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "佃户因租额过重联合抗租，围堵庄院大门"
        GameData.AddLog("佃户聚众闹事！租佃矛盾激化！")
        return {
            title = "佃户闹事",
            desc = "今年收成不佳，佃户们认为族中定的租额过重，纷纷抱怨交不起租子。数十名佃户联合起来，推举头人前来交涉，要求减租三成，否则便不再耕种族田。若处置失当，恐引发更大骚乱。",
            choices = {
                { text = "体恤民情，减租二成（粮食-15，声望+12）", cost = {grain = 15}, result = "族长体恤佃户困苦，当众宣布今年减租二成，待年景好时再恢复旧例。佃户们闻言喜极而泣，纷纷跪地叩谢。此后佃户们更加用心耕作，田间恢复了往日安宁，望族仁厚之名也在乡里传开。", effect = function()
                    GameData.AddResource("fame", 12)
                    GameData.AddLog("减租安民，佃户感恩戴德，田间恢复太平")
                end },
                { text = "稍作让步，减租一成并施粥慰问（粮食-8，声望+5）", result = "族长在庄院门口设了粥棚，让佃户们先吃一顿饱饭，又宣布减租一成以示体恤。佃户头人虽觉减免不够多，但见望族有所让步也就不再闹腾，双方暂且各退一步维持了和气。", effect = function()
                    GameData.AddResource("grain", -8)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("小幅减租并施粥安抚，佃户虽不全满意但也接受")
                end },
                { text = "寸步不让，请官府弹压（银两-8，声望-8）", cost = {silver = 8}, result = "族长铁了心寸步不让，遣人去县衙请来差役弹压。衙役们棍棒齐下驱散了佃户，为首几人还被锁了枷。佃户们虽然散去但满腹怨恨，暗中议论来年干脆不种族田，民怨愈发深重。", effect = function()
                    GameData.AddResource("fame", -8)
                    GameData.AddLog("官府差役前来弹压，佃户散去，但民怨深重")
                end },
            }
        }
    end,
}

-- 46. 倭寇侵扰（沿海威胁）
events[#events + 1] = {
    id = "r4_wokou_raid",
    title = "倭寇侵扰",
    rankRange = {4, 5},
    weight = 5,
    cooldownMonths = 12,
    identityMatch = nil,
    era = {1520, 1588},
    isDisaster = true,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "倭寇犯境劫掠，沿途村庄遭殃，望族亦受波及"
        GameData.AddLog("倭寇来袭！沿海遭受劫掠！")
        return {
            title = "倭寇侵扰",
            desc = "东南沿海倭寇猖獗，一股倭寇沿江而上深入内地劫掠。附近数村已遭洗劫，难民纷纷涌入。卫所兵力不足，县令紧急征召各望族出丁协防。形势危急，族中须立刻抉择。",
            choices = {
                { text = "出钱出人，协助官兵剿倭（银两-20，粮食-15，声望+18）", result = "族长挺身而出，出银两犒赏卫所官兵，又组织族中壮丁协助巡防。一番激战之后倭寇被击退，仓皇遁去。卫所将领上报战功时特意提及望族出力甚多，保全了一方百姓，功在桑梓。", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("grain", -15)
                    GameData.AddResource("fame", 18)
                    GameData.AddLog("族人奋勇协助官兵击退倭寇，保全乡里，功在桑梓")
                end },
                { text = "加固庄院防守，坚壁清野（银两-12，粮食-8）", result = "族长紧急调集银两购买砖石加固庄院围墙，又将附近田间的粮食全部收入仓中坚壁清野。倭寇抵达后见庄院壁垒森严不易攻取，试探一番便转向他处劫掠。族中虽花费不少但总算安然无恙。", effect = function()
                    GameData.AddResource("silver", -12)
                    GameData.AddResource("grain", -8)
                    GameData.AddLog("庄院加固工事，倭寇试探未攻，转掠他处")
                end },
                { text = "举族避入县城（银两-8，声望-10）", result = "族长闻讯倭寇将至，连忙携带细软率全族老幼逃入县城避难。数日后倭寇果然来袭，将无人看守的庄院洗劫一空，粮仓被搬空、家具被砸毁。待倭寇退去归来时一片狼藉，乡邻也暗讽望族贪生怕死。", effect = function()
                    GameData.AddResource("silver", -8)
                    GameData.AddResource("fame", -10)
                    GameData.AddLog("弃庄入城避难，庄园被倭寇洗劫一空")
                end },
            }
        }
    end,
}

-- 47. 祭祀大典（宗祠祭祖）
events[#events + 1] = {
    id = "r4_ancestor_worship",
    title = "祭祀大典",
    rankRange = {4, 5},
    weight = 8,
    cooldownMonths = 6,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s) return true end,
    execute = function(s, report)
        report.events[#report.events + 1] = "春秋祭祀将至，族中须筹办隆重的宗祠祭祖大典"
        GameData.AddLog("祭祖大典将至，各房积极筹备")
        return {
            title = "祭祀大典",
            desc = "一年一度的春祭大典即将来临。身为望族，宗祠祭祖不可敷衍——三牲供品、鼓乐仪仗、祭文诵读、族人齐聚，这是凝聚人心、教化子弟、彰显家风的重要仪式。各房翘首以待，排场不可失于其他望族。",
            choices = {
                { text = "隆重操办，三牲齐备、鼓乐齐鸣（银两-18，粮食-10，声望+15）", cost = {silver = 18, grain = 10}, result = "祭祀大典那日，宗祠内外张灯结彩，三牲供品摆满祭台，鼓乐班子奏起庄严祭曲。族长率各房老幼依序跪拜，祭文洋洋洒洒诵读完毕，全场肃然。外族闻讯亦赞望族礼仪之盛，不愧为一方名门。", effect = function()
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("祭祀大典庄严肃穆，族人肃然起敬，外族闻之亦赞叹不已")
                end },
                { text = "中规中矩，不失体面即可（银两-8，粮食-5，声望+6）", cost = {silver = 8, grain = 5}, result = "祭祀按往年常例操办，供品齐全、仪式规范，虽无格外出彩之处但也庄重有序。族人按辈分依次上香叩拜，祭文简短精练。一切妥妥帖帖结束，不失望族体面。", effect = function()
                    GameData.AddResource("fame", 6)
                    GameData.AddLog("祭祀按常规操办，庄重有序")
                end },
                { text = "从简祭拜，心诚则灵（银两-3，声望-3）", result = "族长声称心诚则灵不必铺张，只备了些简单供品便草草祭拜了事。几位族老看着简陋的祭台直摇头，私下说如此寒酸的祭祀对不起列祖列宗，传出去叫外人笑话我族不知礼数。", effect = function()
                    GameData.AddResource("silver", -3)
                    GameData.AddResource("fame", -3)
                    GameData.AddLog("祭祀从简，部分族老不满，暗讽族长悭吝")
                end },
            }
        }
    end,
}

-- 48. 药材生意（新产业机遇）
events[#events + 1] = {
    id = "r4_herbal_trade",
    title = "药材生意",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 10,
    identityMatch = {"merchant"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local merchants = GameData.GetMembersByState("经商")
        for _, m in ipairs(merchants) do
            if m.alive then return true end
        end
        return false
    end,
    execute = function(s, report)
        local merchants = GameData.GetMembersByState("经商")
        local trader = nil
        for _, m in ipairs(merchants) do
            if m.alive then trader = m; break end
        end
        local name = trader and trader.name or "族人"
        report.events[#report.events + 1] = name .. "发现山中盛产珍贵药材，提议开辟药材生意"
        GameData.AddLog(name .. "提议开拓药材贸易")
        return {
            title = "药材生意",
            desc = name .. "在外经商时结识一位药材商人，得知附近深山中盛产黄芪、当归等名贵药材，而府城药铺收购价极高。若组织人手上山采药，再贩运至府城出售，利润可观。但山路险阻，且深山多有猛兽毒虫。",
            choices = {
                { text = "投入本金，大规模经营药材（银两-18，银两+25，声望+5）", cost = {silver = 18}, result = "族长投入重金雇了一批精壮山民上山采药，又安排族中商人负责运销。首批黄芪、当归等上等药材运抵府城药铺，因品相极佳被抢购一空，获利远超预期。药材生意就此成为族中又一重要财源。", effect = function()
                    GameData.AddResource("silver", 25)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("药材生意开张，首批药材运抵府城，获利丰厚")
                end },
                { text = "小规模试探，先看看行情（银两-8，银两+10）", cost = {silver = 8}, result = "族长谨慎行事，先雇了几个山民上山采了些药材送往府城试卖。药铺掌柜看过药材后颇为满意，虽然量少但利润尚可。族长决定先看看行情再定是否扩大规模。", effect = function()
                    GameData.AddResource("silver", 10)
                    GameData.AddLog("少量药材送至府城试卖，利润尚可")
                end },
                { text = "山路凶险不值得冒险", result = "族长听闻深山中多有猛兽毒蛇，山路崎岖难行，若有人出了意外反而得不偿失。遂回绝了药材生意的提议，那药材商人见此便另寻合作之家去了。族中安安稳稳，也不知错过的是机遇还是风险。", effect = function()
                    GameData.AddLog("药材之议被否决，族长认为不值得冒险")
                end },
            }
        }
    end,
}

-- 49. 秋闱放榜（科举大比）
events[#events + 1] = {
    id = "r4_autumn_exam",
    title = "秋闱放榜",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 12,
    identityMatch = {"scholar"},
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local scholars = GameData.GetMembersByState("读书")
        for _, m in ipairs(scholars) do
            if m.alive and m.age >= 16 and m.age <= 35 then return true end
        end
        return false
    end,
    execute = function(s, report)
        local scholars = GameData.GetMembersByState("读书")
        local candidate = nil
        for _, m in ipairs(scholars) do
            if m.alive and m.age >= 16 and m.age <= 35 then candidate = m; break end
        end
        local name = candidate and candidate.name or "族人"
        report.events[#report.events + 1] = name .. "赴省城参加乡试，阖族盼望佳音"
        GameData.AddLog(name .. "赴乡试，族人翘首以盼")
        return {
            title = "秋闱放榜",
            desc = "三年一度的秋闱乡试开科，" .. name .. "苦读多年，终于赴省城应考。考后数日，族中上下焦急等待放榜消息。中举则光耀门楣，落第亦需安慰鼓励，无论如何族中都须有所准备。",
            choices = {
                { text = "备厚资打点门路，全力运作（银两-20，声望+15）", cost = {silver = 20}, result = "族长不惜重金上下打点，从考官门生到阅卷书吏一一疏通。放榜之日报喜官快马送达，族中子弟高中举人！鞭炮齐鸣锣鼓喧天，阖族上下欢天喜地，乡邻纷纷登门道贺，望族门楣自此更加光耀。", effect = function()
                    GameData.AddResource("fame", 15)
                    GameData.AddLog(name .. "得中举人！报喜官快马送达，阖族欢天喜地！")
                end },
                { text = "准备庆贺事宜，静候佳音（银两-8，声望+8）", cost = {silver = 8}, result = "族长备好了庆贺酒席和赏银，静候省城消息。数日后传来喜讯，族中子弟虽非名列前茅，却也榜上有名中了举人。族中摆开酒席庆贺一番，虽不如头名那般风光，亦是光宗耀祖之事。", effect = function()
                    GameData.AddResource("fame", 8)
                    GameData.AddLog(name .. "乡试中式，虽名次不高，亦是大喜之事")
                end },
                { text = "不做特别准备，一切随缘（声望+3）", result = "族长说科举之事全凭真才实学，不必刻意准备，一切随缘便好。放榜之后族中子弟名落孙山，虽有些遗憾但也并不意外。族长温言安慰，嘱其勿灰心丧气，三年后再战下科，来日方长。", effect = function()
                    GameData.AddResource("fame", 3)
                    GameData.AddLog(name .. "秋闱名落孙山，族中安慰再战下科")
                end },
            }
        }
    end,
}

-- 50. 暗中较劲（家族内斗）
events[#events + 1] = {
    id = "r4_internal_strife",
    title = "暗中较劲",
    rankRange = {4, 5},
    weight = 6,
    cooldownMonths = 10,
    identityMatch = nil,
    era = nil,
    isDisaster = false,
    isChainEvent = false,
    check = function(s)
        local adults = GameData.GetAdultMembers()
        return #adults >= 5
    end,
    execute = function(s, report)
        report.events[#report.events + 1] = "族中两房因产业分配暗中较劲，矛盾日渐激化"
        GameData.AddLog("族中两房暗生嫌隙，争夺产业主导权")
        return {
            title = "暗中较劲",
            desc = "族中大房与二房因一处新置田产的归属问题产生分歧。大房认为应归族中公产由族长统管，二房则主张谁出银两谁得田。两房各有拥趸，暗中拉帮结派，若不及时化解恐生裂痕。",
            choices = {
                { text = "召集族会公议，按族规裁定（银两-5，声望+12）", cost = {silver = 5}, result = "族长在祠堂召集全族公议，请各房长辈当面陈述理由，最后依据族规条文裁定那处田产归族中公产统管，收益按各房人丁均分。虽有一房略感不满，但众议公断令人信服，族中暗流渐渐平息。", effect = function()
                    GameData.AddResource("fame", 12)
                    GameData.AddLog("族长主持公议，按规裁定田产归属，两房虽有不满但皆服从")
                end },
                { text = "各打五十大板，田产收入两房均分（粮食-5，声望+5）", cost = {grain = 5}, result = "族长不偏不倚各打五十大板，裁定那处新置田产收入由大房二房平分，谁也不许独占。两房虽各觉吃了亏，但见族长如此折中也不好再争，暂且息事宁人。只是暗中较劲的心思并未全然消弭。", effect = function()
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("折中处理，田产收入两房各半，暂且息事宁人")
                end },
                { text = "偏向一方强行裁决（声望-8）", result = "族长偏向大房将田产判归其名下，二房上下愤愤不平却又无可奈何。此事之后二房族人与大房渐行渐远，逢年过节也不再来往。族中人心离散，暗流涌动，有识之士皆忧心这裂痕恐再难弥合。", effect = function()
                    GameData.AddResource("fame", -8)
                    GameData.AddLog("族长偏向一房裁决，另一房心怀怨恨，族中人心离散")
                end },
            }
        }
    end,
}

return events
