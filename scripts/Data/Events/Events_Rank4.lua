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
                { text = "大办宴席，宴请宾客（银两-25，声望+20）", effect = function() GameData.AddResource("silver", -25); GameData.AddResource("fame", 20) end },
                { text = "低调庆贺，赠予盘缠赴京（银两-10，声望+8）", effect = function() GameData.AddResource("silver", -10); GameData.AddResource("fame", 8) end },
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
                { text = "全力支持，倾力筹备（银两-20，布匹-15，粮食+40，银两+30）", effect = function() GameData.AddResource("silver", -20); GameData.AddResource("cloth", -15); GameData.AddResource("grain", 40); GameData.AddResource("silver", 30) end },
                { text = "小规模试探，派少量人马（银两-8，粮食+15）", effect = function() GameData.AddResource("silver", -8); GameData.AddResource("grain", 15) end },
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
                { text = "审时度势，支持势强一方（银两-15，声望+12）", effect = function() GameData.AddResource("silver", -15); GameData.AddResource("fame", 12) end },
                { text = "两不相帮，闭门谢客（声望-5）", effect = function() GameData.AddResource("fame", -5) end },
                { text = "暗中斡旋调解两方（银两-10，声望+18）", effect = function() GameData.AddResource("silver", -10); GameData.AddResource("fame", 18) end },
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
                { text = "倾力赈济，设粥棚施衣物（粮食-30，布匹-10，声望+20）", effect = function() GameData.AddResource("grain", -30); GameData.AddResource("cloth", -10); GameData.AddResource("fame", 20) end },
                { text = "量力而行，适度捐助（粮食-12，声望+8）", effect = function() GameData.AddResource("grain", -12); GameData.AddResource("fame", 8) end },
                { text = "仅派人协助官府，不出钱粮（声望-3）", effect = function() GameData.AddResource("fame", -3) end },
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
                { text = "大兴土木，修建气派宅院（银两-25，布匹-10，声望+15）", effect = function() GameData.AddResource("silver", -25); GameData.AddResource("cloth", -10); GameData.AddResource("fame", 15) end },
                { text = "修缮旧宅，小规模翻新（银两-10，声望+5）", effect = function() GameData.AddResource("silver", -10); GameData.AddResource("fame", 5) end },
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
                { text = "出资组织乡勇守城（银两-20，粮食-20，声望+18）", effect = function() GameData.AddResource("silver", -20); GameData.AddResource("grain", -20); GameData.AddResource("fame", 18) end },
                { text = "交出部分钱粮议和（银两-15，粮食-25，布匹-10）", effect = function() GameData.AddResource("silver", -15); GameData.AddResource("grain", -25); GameData.AddResource("cloth", -10) end },
                { text = "携族人弃城避难（声望-15，粮食-10）", effect = function() GameData.AddResource("fame", -15); GameData.AddResource("grain", -10) end },
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
                { text = "备厚礼赴会，力求一鸣惊人（银两-12，声望+15）", effect = function() GameData.AddResource("silver", -12); GameData.AddResource("fame", 15) end },
                { text = "轻装简从，以才学取胜（银两-3，声望+8）", effect = function() GameData.AddResource("silver", -3); GameData.AddResource("fame", 8) end },
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
                { text = "隆重赴京，广结朝中权贵（银两-25，布匹-10，声望+20）", effect = function() GameData.AddResource("silver", -25); GameData.AddResource("cloth", -10); GameData.AddResource("fame", 20) end },
                { text = "简朴赴京，恪尽职责即可（银两-10，声望+10）", effect = function() GameData.AddResource("silver", -10); GameData.AddResource("fame", 10) end },
                { text = "称病推辞，不愿卷入朝堂（声望-8）", effect = function() GameData.AddResource("fame", -8) end },
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
                { text = "以财力压制，展示底蕴（银两-20，声望+12）", effect = function() GameData.AddResource("silver", -20); GameData.AddResource("fame", 12) end },
                { text = "以礼相待，化敌为友（银两-8，粮食-5，声望+6）", effect = function() GameData.AddResource("silver", -8); GameData.AddResource("grain", -5); GameData.AddResource("fame", 6) end },
                { text = "联合其他家族孤立对方（银两-15，声望+15）", effect = function() GameData.AddResource("silver", -15); GameData.AddResource("fame", 15) end },
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
                { text = "全力竞购，志在必得（银两-25，银两+30，粮食+20）", effect = function() GameData.AddResource("silver", -25); GameData.AddResource("silver", 30); GameData.AddResource("grain", 20) end },
                { text = "少量竞购，稳中求进（银两-12，银两+15）", effect = function() GameData.AddResource("silver", -12); GameData.AddResource("silver", 15) end },
                { text = "放弃竞购，另寻商机", effect = function() end },
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
                { text = "出丁应征，保全族产（声望+8）", effect = function() GameData.AddResource("fame", 8) end },
                { text = "交纳代役银免征（银两-25）", effect = function() GameData.AddResource("silver", -25) end },
                { text = "上下打点求减免名额（银两-18，声望-5）", effect = function() GameData.AddResource("silver", -18); GameData.AddResource("fame", -5) end },
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
                { text = "独力承担，刻碑留名（银两-20，布匹-8，声望+18）", effect = function() GameData.AddResource("silver", -20); GameData.AddResource("cloth", -8); GameData.AddResource("fame", 18) end },
                { text = "联合数家分担费用（银两-8，声望+8）", effect = function() GameData.AddResource("silver", -8); GameData.AddResource("fame", 8) end },
                { text = "婉言谢绝，不参与此事", effect = function() GameData.AddResource("fame", -3) end },
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
                { text = "延请名师，府中教导（银两-18，声望+10）", effect = function() GameData.AddResource("silver", -18); GameData.AddResource("fame", 10) end },
                { text = "送往书院游学（银两-12，粮食-5，声望+12）", effect = function() GameData.AddResource("silver", -12); GameData.AddResource("grain", -5); GameData.AddResource("fame", 12) end },
                { text = "由族中长辈亲自教导（声望+3）", effect = function() GameData.AddResource("fame", 3) end },
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
                { text = "大量购入，充实家藏（银两-22，声望+15）", effect = function() GameData.AddResource("silver", -22); GameData.AddResource("fame", 15) end },
                { text = "精挑细选，只购一二幅（银两-10，声望+6）", effect = function() GameData.AddResource("silver", -10); GameData.AddResource("fame", 6) end },
                { text = "疑其为赝品，谨慎拒绝", effect = function() end },
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
                { text = "开仓放粮救济佃户（粮食-30，声望+15）", effect = function() GameData.AddResource("grain", -30); GameData.AddResource("fame", 15) end },
                { text = "高价购粮稳住族中（银两-20，粮食+10）", effect = function() GameData.AddResource("silver", -20); GameData.AddResource("grain", 10) end },
                { text = "紧缩开支，自保为先（粮食-15，声望-8）", effect = function() GameData.AddResource("grain", -15); GameData.AddResource("fame", -8) end },
            }
        }
    end,
}

-- 16. 名门联姻（强强联合）
events[#events + 1] = {
    id = "r4_alliance_marriage",
    title = "名门联姻",
    rankRange = {4, 5},
    weight = 7,
    cooldownMonths = 10,
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
                { text = "前去相看", effect = function()
                    if candidate and not candidate.spouseId then
                        local GS = require("UI.GameScreen")
                        GS.ShowMarriageTierSelect(candidate)
                    end
                end },
                { text = "婉拒提亲（声望-3）", effect = function() GameData.AddResource("fame", -3) end },
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
                { text = "大举投资，独占矿利（银两-25，银两+30，粮食-10）", effect = function() GameData.AddResource("silver", -25); GameData.AddResource("silver", 30); GameData.AddResource("grain", -10) end },
                { text = "小额入股，分散风险（银两-10，银两+12）", effect = function() GameData.AddResource("silver", -10); GameData.AddResource("silver", 12) end },
                { text = "不参与此等冒险之事", effect = function() end },
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
                { text = "严惩不贷，以儆效尤（声望+10）", effect = function() GameData.AddResource("fame", 10) end },
                { text = "私下处置，稳定人心（银两-5，声望+3）", effect = function() GameData.AddResource("silver", -5); GameData.AddResource("fame", 3) end },
                { text = "将计就计，散布假情报（银两-8，声望+12）", effect = function() GameData.AddResource("silver", -8); GameData.AddResource("fame", 12) end },
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
                { text = "大操大办，广邀宾客（银两-20，粮食-15，布匹-10，声望+15）", effect = function() GameData.AddResource("silver", -20); GameData.AddResource("grain", -15); GameData.AddResource("cloth", -10); GameData.AddResource("fame", 15) end },
                { text = "排场适中，不失体面（银两-10，粮食-8，声望+6）", effect = function() GameData.AddResource("silver", -10); GameData.AddResource("grain", -8); GameData.AddResource("fame", 6) end },
                { text = "从简办理，不铺张浪费（银两-3，粮食-3，声望-3）", effect = function() GameData.AddResource("silver", -3); GameData.AddResource("grain", -3); GameData.AddResource("fame", -3) end },
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
                { text = "追查纵火嫌犯，重建仓库（银两-15，声望+5）", effect = function() GameData.AddResource("grain", -25); GameData.AddResource("cloth", -12); GameData.AddResource("silver", -15); GameData.AddResource("fame", 5) end },
                { text = "认赔止损，加强巡防（粮食-25，布匹-12）", effect = function() GameData.AddResource("grain", -25); GameData.AddResource("cloth", -12) end },
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
                { text = "刊印文集广为流传（银两-15，声望+20）", effect = function() GameData.AddResource("silver", -15); GameData.AddResource("fame", 20) end },
                { text = "设学堂收徒讲学（银两-10，粮食-5，声望+15）", effect = function() GameData.AddResource("silver", -10); GameData.AddResource("grain", -5); GameData.AddResource("fame", 15) end },
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
                { text = "投入重金抢占先机（银两-20，布匹-15，银两+28，粮食+15）", effect = function() GameData.AddResource("silver", -20); GameData.AddResource("cloth", -15); GameData.AddResource("silver", 28); GameData.AddResource("grain", 15) end },
                { text = "少量试探，稳步推进（银两-8，布匹-5，银两+10）", effect = function() GameData.AddResource("silver", -8); GameData.AddResource("cloth", -5); GameData.AddResource("silver", 10) end },
                { text = "边境不安全，暂不涉足", effect = function() end },
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
                { text = "重金延请名医，购药救治（银两-25，粮食-10，声望+18）", effect = function() GameData.AddResource("silver", -25); GameData.AddResource("grain", -10); GameData.AddResource("fame", 18) end },
                { text = "封闭庄园，自行隔离（粮食-15，声望-5）", effect = function() GameData.AddResource("grain", -15); GameData.AddResource("fame", -5) end },
                { text = "举族迁避，暂离疫区（银两-15，粮食-10，声望-10）", effect = function() GameData.AddResource("silver", -15); GameData.AddResource("grain", -10); GameData.AddResource("fame", -10) end },
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
                { text = "全力开垦，广置田产（银两-20，粮食-15，粮食+40，声望+8）", effect = function() GameData.AddResource("silver", -20); GameData.AddResource("grain", -15); GameData.AddResource("grain", 40); GameData.AddResource("fame", 8) end },
                { text = "小范围试垦（银两-8，粮食-5，粮食+15）", effect = function() GameData.AddResource("silver", -8); GameData.AddResource("grain", -5); GameData.AddResource("grain", 15) end },
                { text = "暂不动土，留待日后", effect = function() end },
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
                { text = "叩谢天恩，大肆庆贺（银两+25，布匹+20，声望+20，粮食-10）", effect = function() GameData.AddResource("silver", 25); GameData.AddResource("cloth", 20); GameData.AddResource("fame", 20); GameData.AddResource("grain", -10) end },
                { text = "谦逊领受，低调行事（银两+25，布匹+20，声望+12）", effect = function() GameData.AddResource("silver", 25); GameData.AddResource("cloth", 20); GameData.AddResource("fame", 12) end },
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
                { text = "精工细作，刊刻传世（银两-20，布匹-5，声望+18）", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("cloth", -5)
                    GameData.AddResource("fame", 18)
                    GameData.AddLog("族谱修缮告竣，刊刻精美，分赐各房，阖族称颂")
                end },
                { text = "简单整理，手抄存档（银两-6，声望+6）", effect = function()
                    GameData.AddResource("silver", -6)
                    GameData.AddResource("fame", 6)
                    GameData.AddLog("族谱简要修订完毕，虽不华美，亦可传世")
                end },
                { text = "暂缓修谱，待时机成熟再议", effect = function()
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
                { text = "牵头主导，出大头银钱（银两-25，粮食-10，粮食+35，声望+15）", effect = function()
                    GameData.AddResource("silver", -25)
                    GameData.AddResource("grain", -10)
                    GameData.AddResource("grain", 35)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("水利工程竣工，良田灌溉无忧，周边村落感恩戴德")
                end },
                { text = "与各村均摊费用（银两-10，粮食+15，声望+8）", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("grain", 15)
                    GameData.AddResource("fame", 8)
                    GameData.AddLog("水渠修缮完毕，各村共享其利")
                end },
                { text = "只修自家田地的引水沟（银两-5，粮食+8）", effect = function()
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
                { text = "如数缴纳，以保平安（银两-30，粮食-15）", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("grain", -15)
                    GameData.AddLog("加派赋税如数上缴，族中财力大损")
                end },
                { text = "上下打点减免部分（银两-20，声望-5）", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("托人打点求减免，虽省些银钱，名声却受损")
                end },
                { text = "联合乡绅上书请愿减税（银两-10，声望+5）", effect = function()
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
                { text = "全力支持，建学堂聘名师（银两-20，声望+18）", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("fame", 18)
                    GameData.AddLog("义学落成，书声琅琅，四方赞颂")
                end },
                { text = "在祠堂偏厅开课，规模从简（银两-8，声望+8）", effect = function()
                    GameData.AddResource("silver", -8)
                    GameData.AddResource("fame", 8)
                    GameData.AddLog("义学开办，虽规模不大，亦得乡邻感念")
                end },
                { text = "只教本族子弟，不对外开放（银两-5，声望+3）", effect = function()
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
                { text = "忍气吞声，奉上银两打点（银两-18）", effect = function()
                    GameData.AddResource("silver", -18)
                    GameData.AddLog("无奈缴纳孝敬银两，贪官暂时满意离去")
                end },
                { text = "联合士绅向知府告状（银两-12，声望+10）", effect = function()
                    GameData.AddResource("silver", -12)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("联名状递上知府衙门，贪官被申饬，暗中怀恨")
                end },
                { text = "硬顶到底，拒不行贿（声望+5，粮食-10）", effect = function()
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
                { text = "重金疏通官府，将此事压下（银两-25，声望-5）", effect = function()
                    GameData.AddResource("silver", -25)
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("银两上下打点，走私之事被压下，但名声有损")
                end },
                { text = "配合调查以证清白（声望+8）", effect = function()
                    GameData.AddResource("fame", 8)
                    GameData.AddLog("坦然配合官府调查，证实是他人诬告，清白昭雪")
                end },
                { text = "主动交出部分货物表诚意（银两-12，布匹-8）", effect = function()
                    GameData.AddResource("silver", -12)
                    GameData.AddResource("cloth", -8)
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
                { text = "出银两赔偿伤者，息事宁人（银两-18，声望-3）", effect = function()
                    GameData.AddResource("silver", -18)
                    GameData.AddResource("fame", -3)
                    GameData.AddLog("赔偿伤者银两，族人释放，但名声略损")
                end },
                { text = "依族规先行惩戒，再向官府求情（银两-10，声望+5）", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("先行族规家法惩处，再赴衙门赔情，县令从轻发落")
                end },
                { text = "大义灭亲，任由官府处置（声望+10）", effect = function()
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
                { text = "独力出资修建石桥，刻碑纪念（银两-22，声望+20）", effect = function()
                    GameData.AddResource("silver", -22)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("新桥落成，碑文镌刻族名，行人交口称赞")
                end },
                { text = "号召各家分摊费用共同修建（银两-10，声望+10）", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("新桥合力修成，虽非独功，亦得好评")
                end },
                { text = "仅捐少量银两意思一下（银两-3，声望-2）", effect = function()
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
                { text = "全力支持，投入重金开设分号（银两-25，布匹-10，银两+20，声望+10）", effect = function()
                    GameData.AddResource("silver", -25)
                    GameData.AddResource("cloth", -10)
                    GameData.AddResource("silver", 20)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("府城分号开张大吉，生意红火，族中声势大振")
                end },
                { text = "小规模试营，先站稳脚跟（银两-12，银两+8，声望+3）", effect = function()
                    GameData.AddResource("silver", -12)
                    GameData.AddResource("silver", 8)
                    GameData.AddResource("fame", 3)
                    GameData.AddLog("府城小店开张，虽不起眼，慢慢也有了口碑")
                end },
                { text = "守住县城本业，不冒进", effect = function()
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
                { text = "组织族人灭蝗，开仓济民（粮食-25，银两-10，声望+15）", effect = function()
                    GameData.AddResource("grain", -25)
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("全族动员灭蝗救灾，虽损失惨重，但人心凝聚")
                end },
                { text = "抢购外地粮食囤积自保（银两-20，粮食+5）", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("grain", 5)
                    GameData.AddLog("高价抢购外地粮食，勉强维持族中口粮")
                end },
                { text = "听天由命，紧缩度日（粮食-20，声望-8）", effect = function()
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
                { text = "据理力争，请县令裁断（银两-12，声望+8）", effect = function()
                    GameData.AddResource("silver", -12)
                    GameData.AddResource("fame", 8)
                    GameData.AddLog("对簿公堂，县令裁定田地归我族所有，邻族悻悻而去")
                end },
                { text = "各退一步，将田地一分为二（粮食-8，声望+3）", effect = function()
                    GameData.AddResource("grain", -8)
                    GameData.AddResource("fame", 3)
                    GameData.AddLog("两族各退一步，田地各得一半，暂且安宁")
                end },
                { text = "慷慨让出田地，以和为贵（粮食-15，声望+12）", effect = function()
                    GameData.AddResource("grain", -15)
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
                { text = "重金留人，购置新织机全面推广（银两-20，布匹+25，声望+8）", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("cloth", 25)
                    GameData.AddResource("fame", 8)
                    GameData.AddLog("新织法推广成功，族中布匹质量大增，供不应求")
                end },
                { text = "先小范围试验，验证效果（银两-8，布匹+10）", effect = function()
                    GameData.AddResource("silver", -8)
                    GameData.AddResource("cloth", 10)
                    GameData.AddLog("新织法初步试验成功，产出的布匹确实精美")
                end },
                { text = "婉拒，怕是骗术", effect = function()
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
                { text = "按望族规格隆重办理（银两-22，粮食-10，布匹-8，声望+15）", effect = function()
                    GameData.AddResource("silver", -22)
                    GameData.AddResource("grain", -10)
                    GameData.AddResource("cloth", -8)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("丧仪隆重庄严，四方宾客来吊，尽显望族风范")
                end },
                { text = "体面但不奢靡，重在心意（银两-10，粮食-5，声望+6）", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("grain", -5)
                    GameData.AddResource("fame", 6)
                    GameData.AddLog("丧仪庄重有度，族人亲友皆感其诚")
                end },
                { text = "从简从俭，遵其遗愿（银两-5，声望-3）", effect = function()
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
                { text = "慷慨捐粮，设立大型义仓（粮食-25，声望+18）", effect = function()
                    GameData.AddResource("grain", -25)
                    GameData.AddResource("fame", 18)
                    GameData.AddLog("义仓拔地而起，粮满仓实，县令亲题匾额嘉许")
                end },
                { text = "适量捐助，量力而行（粮食-12，声望+8）", effect = function()
                    GameData.AddResource("grain", -12)
                    GameData.AddResource("fame", 8)
                    GameData.AddLog("义仓中捐入部分存粮，聊尽绵力")
                end },
                { text = "口头支持，实则观望", effect = function()
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
                { text = "全面修订族规，请名儒参酌（银两-15，声望+15）", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("新族规修订颁行，条理清晰，各房服膺遵行")
                end },
                { text = "在旧规基础上增补条目（银两-5，声望+6）", effect = function()
                    GameData.AddResource("silver", -5)
                    GameData.AddResource("fame", 6)
                    GameData.AddLog("族规增补完毕，虽不尽善尽美，也比从前明晰")
                end },
                { text = "维持旧制不变，以免各房争执", effect = function()
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
                { text = "出丁应役，另贴口粮（粮食-12，声望+8）", effect = function()
                    GameData.AddResource("grain", -12)
                    GameData.AddResource("fame", 8)
                    GameData.AddLog("族中壮丁应役修城，虽苦犹荣")
                end },
                { text = "交纳代役银免除劳役（银两-15）", effect = function()
                    GameData.AddResource("silver", -15)
                    GameData.AddLog("缴银代役，族人免去修城之苦")
                end },
                { text = "出银又出人，大力支持（银两-10，粮食-8，声望+15）", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("grain", -8)
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
                { text = "散尽家财招募乡勇守城（银两-30，粮食-20，声望+20）", effect = function()
                    GameData.AddResource("silver", -30)
                    GameData.AddResource("grain", -20)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog("散财聚兵，率乡勇守城，流寇绕城而去，阖县感恩")
                end },
                { text = "献出部分财物求流寇过境不扰（银两-20，粮食-15，布匹-10）", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("grain", -15)
                    GameData.AddResource("cloth", -10)
                    GameData.AddLog("忍痛割财，换得流寇不入城，虽保全性命却损失惨重")
                end },
                { text = "携带细软弃城逃难（声望-15，银两-10）", effect = function()
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
                { text = "全力运作，务使推举成功（银两-25，布匹-10，声望+20）", effect = function()
                    GameData.AddResource("silver", -25)
                    GameData.AddResource("cloth", -10)
                    GameData.AddResource("fame", 20)
                    GameData.AddLog(name .. "得知府举荐，踏上仕途，族中声望大振")
                end },
                { text = "适度准备，听凭天命（银两-12，声望+10）", effect = function()
                    GameData.AddResource("silver", -12)
                    GameData.AddResource("fame", 10)
                    GameData.AddLog("推举之事已尽人事，能否成功尚待消息")
                end },
                { text = "婉拒推举，不愿卷入官场（声望-5）", effect = function()
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
                { text = "组织族人修堤排水，开仓救济（银两-20，粮食-20，声望+18）", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("grain", -20)
                    GameData.AddResource("fame", 18)
                    GameData.AddLog("族长亲率族人修堤排水，赈济灾民，义声远播")
                end },
                { text = "先保护族中产业，再顾及他人（银两-10，粮食-10，声望-5）", effect = function()
                    GameData.AddResource("silver", -10)
                    GameData.AddResource("grain", -10)
                    GameData.AddResource("fame", -5)
                    GameData.AddLog("先行自保，族产损失减半，但邻里颇有微词")
                end },
                { text = "向官府求援，等待朝廷赈济（粮食-15）", effect = function()
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
                { text = "体恤民情，减租二成（粮食-15，声望+12）", effect = function()
                    GameData.AddResource("grain", -15)
                    GameData.AddResource("fame", 12)
                    GameData.AddLog("减租安民，佃户感恩戴德，田间恢复太平")
                end },
                { text = "稍作让步，减租一成并施粥慰问（粮食-8，声望+5）", effect = function()
                    GameData.AddResource("grain", -8)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("小幅减租并施粥安抚，佃户虽不全满意但也接受")
                end },
                { text = "寸步不让，请官府弹压（银两-8，声望-8）", effect = function()
                    GameData.AddResource("silver", -8)
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
                { text = "出钱出人，协助官兵剿倭（银两-20，粮食-15，声望+18）", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("grain", -15)
                    GameData.AddResource("fame", 18)
                    GameData.AddLog("族人奋勇协助官兵击退倭寇，保全乡里，功在桑梓")
                end },
                { text = "加固庄院防守，坚壁清野（银两-12，粮食-8）", effect = function()
                    GameData.AddResource("silver", -12)
                    GameData.AddResource("grain", -8)
                    GameData.AddLog("庄院加固工事，倭寇试探未攻，转掠他处")
                end },
                { text = "举族避入县城（银两-8，声望-10）", effect = function()
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
                { text = "隆重操办，三牲齐备、鼓乐齐鸣（银两-18，粮食-10，声望+15）", effect = function()
                    GameData.AddResource("silver", -18)
                    GameData.AddResource("grain", -10)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog("祭祀大典庄严肃穆，族人肃然起敬，外族闻之亦赞叹不已")
                end },
                { text = "中规中矩，不失体面即可（银两-8，粮食-5，声望+6）", effect = function()
                    GameData.AddResource("silver", -8)
                    GameData.AddResource("grain", -5)
                    GameData.AddResource("fame", 6)
                    GameData.AddLog("祭祀按常规操办，庄重有序")
                end },
                { text = "从简祭拜，心诚则灵（银两-3，声望-3）", effect = function()
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
                { text = "投入本金，大规模经营药材（银两-18，银两+25，声望+5）", effect = function()
                    GameData.AddResource("silver", -18)
                    GameData.AddResource("silver", 25)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("药材生意开张，首批药材运抵府城，获利丰厚")
                end },
                { text = "小规模试探，先看看行情（银两-8，银两+10）", effect = function()
                    GameData.AddResource("silver", -8)
                    GameData.AddResource("silver", 10)
                    GameData.AddLog("少量药材送至府城试卖，利润尚可")
                end },
                { text = "山路凶险不值得冒险", effect = function()
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
                { text = "备厚资打点门路，全力运作（银两-20，声望+15）", effect = function()
                    GameData.AddResource("silver", -20)
                    GameData.AddResource("fame", 15)
                    GameData.AddLog(name .. "得中举人！报喜官快马送达，阖族欢天喜地！")
                end },
                { text = "准备庆贺事宜，静候佳音（银两-8，声望+8）", effect = function()
                    GameData.AddResource("silver", -8)
                    GameData.AddResource("fame", 8)
                    GameData.AddLog(name .. "乡试中式，虽名次不高，亦是大喜之事")
                end },
                { text = "不做特别准备，一切随缘（声望+3）", effect = function()
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
                { text = "召集族会公议，按族规裁定（银两-5，声望+12）", effect = function()
                    GameData.AddResource("silver", -5)
                    GameData.AddResource("fame", 12)
                    GameData.AddLog("族长主持公议，按规裁定田产归属，两房虽有不满但皆服从")
                end },
                { text = "各打五十大板，田产收入两房均分（粮食-5，声望+5）", effect = function()
                    GameData.AddResource("grain", -5)
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("折中处理，田产收入两房各半，暂且息事宁人")
                end },
                { text = "偏向一方强行裁决（声望-8）", effect = function()
                    GameData.AddResource("fame", -8)
                    GameData.AddLog("族长偏向一房裁决，另一房心怀怨恨，族中人心离散")
                end },
            }
        }
    end,
}

return events
