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
                { text = "花银两请郎中治牛（-5银两，牛康复可保春耕）", cost = {silver = 5}, result = "郎中用了三副汤药，又配以针灸推拿，半月之后老黄牛竟渐渐好了起来。春耕时节，牛儿健步如飞地拉着犁头翻开泥土，族人欢喜不已。这一季庄稼长势极好，粮仓比往年多添了不少新粮。", effect = function()
                    GameData.AddResource("grain", 8)
                    GameData.AddLog("花银治好了耕牛，春耕顺利，多收了粮食。")
                end },
                { text = "忍痛卖与屠户（+3银两，来年春耕艰难）", result = "屠户赶了辆板车来把病牛拉走，留下三两碎银。族人望着空荡荡的牛棚，心中五味杂陈。来年春耕时节，只得全家老少齐上阵，人拉肩扛地翻地播种，累得腰都直不起来，收成自然也大不如前。", effect = function()
                    GameData.AddResource("silver", 3)
                    GameData.AddResource("grain", -8)
                    GameData.AddLog("卖掉病牛得了些银钱，来年春耕只能靠人力了。")
                end },
                { text = "不管不顾，听天由命", result = "老黄牛在牛棚里挣扎了几日，终究没能熬过去，一命呜呼。春耕时节无牛可用，大片田地荒废未耕，秋收时粮食减产甚多。族人望着半空的粮仓，暗自懊悔当初未曾及时救治。", effect = function()
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
                { text = "花银两买种子试种（-4银两，有机会大丰收）", cost = {silver = 4}, effect = function(self)
                    if math.random() > 0.4 then
                        GameData.AddResource("grain", 15)
                        GameData.AddLog("新种子试种成功，产量喜人，粮仓满溢。")
                        self.result = "那番邦种子果然不同凡响，入土之后长势如疯，翠绿的秧苗比寻常庄稼高出一头。秋收时节满田金黄，产量竟是往年两倍有余，粮仓堆得满满当当，族人喜笑颜开，都说这钱花得值。"
                    else
                        GameData.AddResource("grain", -3)
                        GameData.AddLog("新种子水土不服，枯死大半，白费了功夫。")
                        self.result = "番邦种子下地之后起初还冒了几片嫩芽，不料入夏后水土不服，叶子枯黄卷曲，大片大片地萎了下去。白白荒废了几亩好田，连本来能种的粮食也误了时节，族人懊恼不已。"
                    end
                end },
                { text = "只买少量试试（-2银两，小规模尝试）", cost = {silver = 2}, effect = function(self)
                    if math.random() > 0.3 then
                        GameData.AddResource("grain", 6)
                        GameData.AddLog("小量试种新作物，略有收获。")
                        self.result = "只在田角辟了一小块地试种新种子，精心侍弄之下竟然长得不错。虽说产量不算惊人，却也多收了几担粮食。族人暗自庆幸没有冒大险，来年打算再多种些。"
                    else
                        GameData.AddLog("少量新种子没有发芽，损失不大。")
                        self.result = "试种的那一小片新作物始终没有发芽，翻开泥土一看，种子早已腐烂在地里。好在只种了一小块，损失不算太大，族人摇摇头便罢了，还是老种子靠得住。"
                    end
                end },
                { text = "不冒这个险，照旧种地", result = "婉言谢绝了客商的好意，将那番邦种子推了回去。客商叹了口气牵着骡子往别村去了。族人依旧播下自家留的老种子，虽无惊喜，却也踏踏实实，秋收时节中规中矩地入了仓。", effect = function()
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
                { text = "出银两请里正调解（-3银两，和平解决）", cost = {silver = 3}, result = "里正收了银子果然尽心，亲自跑了两趟李家庄，又请了县里的老吏出面说和。最终两村约定轮流引水、各守其分，纠纷就此平息。乡邻们都夸族长处事公道、顾全大局，名声在十里八乡传开了。", effect = function()
                    GameData.AddResource("fame", 3)
                    GameData.AddLog("请里正出面调解水源纠纷，两村各退一步，名声有所提升。")
                end },
                { text = "组织族人去理论（可能冲突，但不花钱）", effect = function(self)
                    if math.random() > 0.5 then
                        GameData.AddResource("fame", -2)
                        GameData.AddLog("与邻村人起了冲突，虽夺回了水源，但结了怨仇。")
                        self.result = "族中青壮浩浩荡荡地开到上游，与李家庄的人在坝前对峙起来。推搡之间拳脚相加，虽然最终拆了那道土坝夺回了水源，却也打伤了几个人。两村从此结了深仇，日后行路相遇都要横眉冷对。"
                    else
                        GameData.AddResource("grain", 5)
                        GameData.AddLog("族人据理力争，邻村理亏退让，水源恢复。")
                        self.result = "族中长辈带着几位老成持重之人前去交涉，搬出祖上的水契和村规，说得邻村老族长面红耳赤、理屈词穷。李家庄自知无理，默默拆了土坝放了水，本村秧苗得以灌溉，秋收时粮食反倒比往年还多。"
                    end
                end },
                { text = "自家挖井取水（-4银两，一劳永逸）", cost = {silver = 4}, result = "请了镇上有名的打井匠来勘察地势，选了块好位置往下挖了三丈有余，终于见到了清冽的地下水。从此自家田地有了独立水源，再不用看邻村脸色。族人挑水浇灌，庄稼也长得齐整了不少。", effect = function()
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
    weight = 9,
    cooldownMonths = 4,
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
                { text = "前去相看", result = "答应了王媒婆的提议，择了个良辰吉日前去相看。一路上心中既有几分期待，又有几分忐忑，不知这门亲事能否称心如意。", effect = function()
                    if candidate and not candidate.spouseId then
                        local GS = require("UI.GameScreen")
                        GS.ShowMarriageTierSelect(candidate)
                    end
                end },
                { text = "婉言谢绝，再等等看", result = "客客气气地谢了王媒婆，说家中尚有诸事未定，亲事不急在这一时。媒婆虽有些不悦，仍笑着说日后若有好人选再来说。族人私下议论纷纷，有人说不该错过好姻缘，有人说缘分自有天定。", effect = function()
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
                { text = "倾力买下（-8银两，长远收益）", cost = {silver = 8}, result = "一口价痛快地买下了那亩水田，请里正做了中人立了契书按了手印。从此自家的田连成了一大片，灌溉耕种都方便了许多。秋收时节新田产出颇丰，族人都说这笔买卖做得划算，长远来看定是稳赚不赔的。", effect = function()
                    GameData.AddResource("grain", 10)
                    GameData.AddLog("买下了一亩良田，来年多了不少收成。")
                end },
                { text = "还价一番再买（-5银两）", cost = {silver = 5}, effect = function(self)
                    if math.random() > 0.4 then
                        GameData.AddResource("grain", 6)
                        GameData.AddLog("压了价钱买下田地，虽不算最好的，也还过得去。")
                        self.result = "磨了半日嘴皮子，终于以低价买下了那块田。虽说老张心有不甘，但急等钱用也只好应了。田虽到手，却因压价过甚得罪了人家，日后邻里之间多少有些嫌隙。好在田是实实在在的，来年收成也还过得去。"
                    else
                        GameData.AddLog("还价太狠，老张不肯卖了，田被别家买走。")
                        self.result = "还来还去把老张惹恼了，一拍桌子说'宁可卖给外人也不卖给你家'。没过两日，那块好田果然被隔壁村的刘财主买走了。族人只得望田兴叹，后悔当初不该太过计较那几两银子。"
                    end
                end },
                { text = "手头不宽裕，只能作罢", result = "掂量了手头的银两，实在拿不出这笔钱来。眼看着那块上好的水田被别家买走，心中虽有不甘，却也无可奈何。族人安慰说来日方长，可这样的好田好价可遇不可求，错过了便是错过了。", effect = function()
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
                { text = "带货物去赶集交易（+5银两，+2布匹）", result = "天没亮就装好了满满一车土产粮食，赶着牛车颠簸了大半日才到县城。庙会上人山人海、热闹非凡，粮食和山货很快便卖了个精光。又顺手买了几匹好布回来，银钱布匹都有了进项，一趟下来收获颇丰，族人个个喜气洋洋。", effect = function()
                    GameData.AddResource("silver", 5)
                    GameData.AddResource("cloth", 2)
                    GameData.AddResource("grain", -5)
                    GameData.AddLog("赶庙会卖了些粮食土产，换回银两和布匹。")
                end },
                { text = "只去看看热闹，少量采买（-2银两）", cost = {silver = 2}, result = "带了几个族人轻装上路，到庙会上只是闲逛了一圈。看了杂耍艺人的把戏、尝了几样小吃，又买了些针头线脑和一匹粗布回来。虽没做什么大买卖，却也开了眼界、添了些家用，算是不虚此行。", effect = function()
                    GameData.AddResource("cloth", 1)
                    GameData.AddLog("逛了庙会，买了些针线布匹回来。")
                end },
                { text = "路途遥远，不去了", result = "思量着县城来回要走一整天，田里的活计又正忙着，便打消了赶庙会的念头。安安心心地在家侍弄庄稼，虽说少了些热闹，却也没耽误农时。邻家赶集回来绘声绘色地说起庙会盛况，听得族人好生羡慕。", effect = function()
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
                { text = "花银两挖渠引远水（-5银两，减轻损失）", result = "出了银子雇了十几个壮劳力，顶着毒日头从三里外的河沟挖了一条引水渠过来。虽说水量不大，却也勉强救活了大半庄稼。旱灾过后清点损失，虽折了不少银两和粮食，但比起别家颗粒无收，已算是万幸。", effect = function()
                    GameData.AddResource("silver", -5)
                    GameData.AddResource("grain", -8)
                    GameData.AddLog("出钱引水，保住了部分庄稼，损失不算太惨。")
                end },
                { text = "开仓放粮撑过旱期（-15粮食）", result = "打开粮仓取出存粮，精打细算地按日发放口粮，勉强维持一家老小不至于饿着。田里的庄稼是救不回来了，但人活着就有希望。邻里见此家有余粮接济度荒，纷纷称赞有远见、积了阴德，名声反倒好了不少。", effect = function()
                    GameData.AddResource("grain", -15)
                    GameData.AddResource("fame", 2)
                    GameData.AddLog("靠存粮度过了旱灾，粮仓见底，但人都活了下来。")
                end },
                { text = "祈雨求天，听天由命", result = "在村头设了祭坛，摆上三牲供品跪地祈雨，连祷了三日三夜仍不见一滴雨水。天不遂人愿，庄稼尽皆枯死，粮仓日渐空虚。邻里暗中嘲笑不知变通、坐等天意，声望也跌落了几分。", effect = function()
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
                { text = "趁高价卖掉大半余粮（-12粮食，+10银两）", result = "粮商笑容满面地过了秤、点了银子，几辆大车装得满满当当地驶向府城。一下子进账十两白银，手头宽裕了不少。只是望着空了大半的粮仓，心底隐隐有些不安，若是来年收成不好，这日子可就难过了。", effect = function()
                    GameData.AddResource("grain", -12)
                    GameData.AddResource("silver", 10)
                    GameData.AddLog("趁着好价钱卖了大半粮食，银子赚了不少。")
                end },
                { text = "少卖一些，留足口粮（-5粮食，+4银两）", result = "只卖了几石余粮，换来四两银子揣进怀里，粮仓里还留着大半年的口粮。族人觉得这样最稳妥，既趁着好行情赚了些银两，又不至于断了粮。粮商虽嫌买得少，也只得作罢。", effect = function()
                    GameData.AddResource("grain", -5)
                    GameData.AddResource("silver", 4)
                    GameData.AddLog("卖了少量余粮，既赚了银子也留了后路。")
                end },
                { text = "一粒不卖，手中有粮心中不慌", result = "任凭粮商说得天花乱坠也不为所动，族长拍板说'手中有粮心中不慌'，一粒也不卖。粮商悻悻地赶着空车走了。虽说没赚到银子，可看着满满当当的粮仓，心里踏踏实实的，不怕年景有什么变故。", effect = function()
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
                { text = "聘请先生教书（-5银两，族人读书受益）", cost = {silver = 5}, result = "腾出一间厢房做了学堂，摆上几张桌椅条凳，又买了笔墨纸砚。陈秀才每日清晨开讲，从《三字经》《百家姓》教起，族中子弟虽是泥腿子出身，却也一个个正襟危坐地用心苦读。不出半年便识得不少字，族中名望也因重学之风而日渐提升。", effect = function()
                    GameData.AddResource("fame", 3)
                    local scholars = GameData.GetMembersByState("读书")
                    if #scholars > 0 then
                        GameData.AddLog("请了陈秀才教书，" .. scholars[1].name .. "等人学业大有长进。")
                    else
                        GameData.AddLog("请了陈秀才在村中设馆，族中子弟开始读书识字。")
                    end
                end },
                { text = "只让一个孩子去旁听（-2银两）", cost = {silver = 2}, result = "花了二两银子的束脩，安排了家中一个机灵的孩子去私塾旁听。那孩子每日背着书袋去学堂，虽只是坐在末排听讲，却也认得了不少字，回来还能教弟妹们写自己的名字。虽说进步不算大，总比目不识丁强。", effect = function()
                    GameData.AddResource("fame", 1)
                    GameData.AddLog("安排一个孩子去私塾旁听，多少认些字。")
                end },
                { text = "庄户人家，读书无用", result = "摆了摆手谢绝了陈秀才，说咱们庄户人家种田为本，读书识字有什么用处。陈秀才叹了口气转去别家了。孩子们照旧在田里拔草割稻，有些聪慧的偷偷跑去学堂外面听先生讲课，被叫回来还挨了一顿骂。", effect = function()
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
                { text = "大力投入养蚕（-3银两，产出布匹）", cost = {silver = 3}, result = "买了十几筐蚕种回来，又添了竹匾蚕架等家什。全家上下齐动手，日夜不停地采桑喂蚕。那蚕儿吃得沙沙作响，一天一个样地长大。待到吐丝结茧时，满屋子雪白的蚕茧堆得如小山一般，缫出的丝又细又亮，织成好几匹上等绢布。", effect = function()
                    GameData.AddResource("cloth", 8)
                    GameData.AddLog("养蚕缫丝，产出不少好布匹。")
                end },
                { text = "小规模尝试（-1银两）", cost = {silver = 1}, result = "只买了两三筐蚕种，让家中妇人闲暇时照看。虽说养得不多，蚕儿倒也争气地吐丝结茧了。缫出的丝不够织上等绢布，便织了几匹粗布自家穿用，也算是多了一份进项，来年打算再多养些。", effect = function()
                    GameData.AddResource("cloth", 3)
                    GameData.AddLog("小养了一些蚕，得了几匹粗布。")
                end },
                { text = "专心种田，不分心思", result = "想了想养蚕又要采桑又要侍弄，实在分不开人手，便打消了念头。安心在田里干活，把心思全放在了庄稼上。虽没有多出布匹的进项，但田里的活计倒也打理得井井有条，没耽误什么农时。", effect = function()
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
                { text = "破财消灾，交出财物（-3银两，-5粮食）", result = "族人不敢抵抗，颤抖着将车上的银两和粮食搬下来交给匪人。那几个蒙面大汉嘿嘿笑着扛起东西消失在山坳之中。族人垂头丧气地赶着空车回了家，虽说丢了钱粮心疼得紧，但好歹人没事，留得青山在不怕没柴烧。", effect = function()
                    GameData.AddResource("silver", -3)
                    GameData.AddResource("grain", -5)
                    GameData.AddLog("族人被劫，交出了银粮才保住性命。")
                end },
                { text = "奋力反抗（看族中武力）", effect = function(self)
                    local warriors = GameData.GetMembersByState("习武")
                    if #warriors > 0 then
                        GameData.AddResource("fame", 2)
                        GameData.AddLog(warriors[1].name .. "奋起反击，赶跑了劫匪，分毫未失。")
                        self.result = "族中习武之人挺身而出，一声大喝震得匪人气势便矮了三分。几番交手之下，那些乌合之众哪里是对手，丢下刀棍狼狈逃窜。车上钱粮分毫未损，此事传开后族人威名大振，再没有毛贼敢来惹是生非。"
                    else
                        GameData.AddResource("silver", -2)
                        GameData.AddResource("grain", -3)
                        GameData.AddLog("族人拼命反抗，虽赶走了匪人，但也折损了些财物。")
                        self.result = "族人虽无武艺在身却也豁出命去抵抗，抄起扁担锄头与匪人缠斗在一起。混乱之中车上散落了不少银粮被匪人抢走，好在最终把人赶跑了，只是身上挂了几处伤，财物也折损了不少。"
                    end
                end },
                { text = "大声呼救，等待过路人帮忙", effect = function(self)
                    if math.random() > 0.5 then
                        GameData.AddResource("silver", -1)
                        GameData.AddLog("呼救声引来了巡路的衙役，匪人逃窜，只丢了少许银两。")
                        self.result = "族人扯开嗓子拼命呼喊，声音在山谷间回荡。恰好有几个巡路的衙役在附近歇脚，闻声赶来。匪人见官差到了，慌忙扔下东西四散奔逃。虽说仓皇中丢了些散碎银两，但大部分钱粮都保住了，算是不幸中的万幸。"
                    else
                        GameData.AddResource("silver", -3)
                        GameData.AddResource("grain", -4)
                        GameData.AddLog("无人应答，最终还是被劫走了大半财物。")
                        self.result = "喊了半天嗓子都哑了也没见一个人影。这荒山野岭的本就人迹罕至，匪人见无人来援更加嚣张，将车上大半钱粮洗劫一空方才扬长而去。族人坐在路边欲哭无泪，只得拖着剩下的一点东西灰溜溜地回了家。"
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
                { text = "慷慨解囊，捐银修庙（-5银两，+5声望）", cost = {silver = 5}, result = "痛快地掏出五两白银交与里正。修庙完工之日，崭新的功德碑上赫然刻着族长的名字排在首位，金漆大字在阳光下熠熠生辉。十里八乡的乡亲前来上香时都要看一看碑上的名字，无不投来敬佩的目光，族中声望一时间大为提升。", effect = function()
                    GameData.AddResource("fame", 5)
                    GameData.AddLog("慷慨捐银修缮庙宇，名字刻上了功德碑，乡邻敬重。")
                end },
                { text = "量力而行，少捐一些（-2银两，+2声望）", cost = {silver = 2}, result = "量力捐了二两银子，虽不算最多，也是一番心意。修庙时族人也去帮了几日工，搬砖递瓦出了不少力。完工后功德碑上虽排在后面几位，但乡邻提起来也都说是个实诚人家，名声稍有提升。", effect = function()
                    GameData.AddResource("fame", 2)
                    GameData.AddLog("捐了些银两修庙，尽了心意。")
                end },
                { text = "手头紧，出力不出钱", result = "实在拿不出银子来，便派了几个壮劳力去工地帮忙。搬砖抬瓦、和泥砌墙，干了好几天的苦力活。虽说功德碑上没有名字，但庙祝记了个人情，乡邻也看在眼里，说这家虽穷却不是不懂事理的人。", effect = function()
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
                { text = "备厚礼送去学艺（-4银两，-2布匹）", cost = {silver = 4, cloth = 2}, result = "备了四两银子并两匹上好的布做束脩之礼，恭恭敬敬地领着孩子去拜师。王木匠见礼数周全，欣然收下，当日便行了拜师礼、叩了头、敬了茶。从此那孩子便在师傅身边学刨学锯，虽说辛苦，却也学到了真本事，日后多了一门安身立命的手艺。", effect = function()
                    GameData.AddResource("fame", 2)
                    GameData.AddLog(cName .. "拜了王木匠为师，开始学手艺。")
                end },
                { text = "简单备礼，试试看（-2银两）", cost = {silver = 2}, effect = function(self)
                    if math.random() > 0.3 then
                        GameData.AddLog(cName .. "顺利入了师门，开始学木匠手艺。")
                        self.result = "只备了二两银子做见面礼，心中忐忑地领着孩子去了王木匠家。王木匠看了看孩子的手掌，又考了几道简单的算术题，见这孩子手脚利索、脑筋灵光，便点了点头收下了。虽说礼薄了些，但师傅看中的是徒弟的资质，倒也不太计较。"
                    else
                        GameData.AddLog("礼薄了些，王木匠没有收下" .. cName .. "。")
                        self.result = "只带了二两银子就上门求师，王木匠看了看那薄礼，脸上露出不悦之色。客客气气地说已经收够了徒弟，将人打发了回来。族人心知是礼数不够被嫌弃了，暗自懊悔不已，可木匠的面子又不能强求，这机会便白白错过了。"
                    end
                end },
                { text = "家里人手不够，走不开", result = "思来想去还是舍不得放人走。田里的活计正忙，少一个壮劳力就少一份收成。那孩子虽有些失望，却也懂事地没多说什么，默默扛起锄头又下了田。只是偶尔路过王木匠的铺子时，会忍不住多看几眼里头刨花飞溅的光景。", effect = function()
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
                { text = "大办祭典，出粮出力（-5粮食，+4声望）", cost = {grain = 5}, result = "族中慷慨出粮，搭台唱戏三日，十里八乡皆来观礼。祭天酬神之际，里正当众赞颂其仁义，名字被书于祠堂功德簿上。此后乡邻有事多来相商，族中威望日隆，俨然成了村中主事之家。", effect = function()
                    GameData.AddResource("fame", 4)
                    GameData.AddLog("出粮办了场风光的丰收祭，合村欢庆，声名远扬。")
                end },
                { text = "随份子参加（-2粮食，+1声望）", cost = {grain = 2}, result = "随了份子，携家小去祭场看了场热闹。族人与邻里推杯换盏、谈笑风生，虽未出大力，却也融入了乡邻之间，得了几分人情。孩子们看了舞龙耍狮，高兴了好些日子。", effect = function()
                    GameData.AddResource("fame", 1)
                    GameData.AddLog("随了份子参加丰收祭，热闹了一番。")
                end },
                { text = "关起门来自家庆贺", result = "合村欢庆之时独自闭门不出，邻里颇有微词，背后议论此家吝啬小气。族中虽自炊了一桌好菜庆祝丰收，却失了与乡邻交好的机缘，日后遇事少了几分助力。", effect = function()
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
                { text = "花钱修缮，加固防火（-4银两，防患未然）", result = "请来泥瓦匠将灶房推倒重砌，又在粮仓与灶房之间加筑了一道防火砖墙。虽花了不少银两，但此后数年再未走过水。乡邻见了纷纷效仿，族中也因此被夸赞有远见。", effect = function()
                    GameData.AddResource("silver", -4)
                    GameData.AddResource("grain", -5)
                    GameData.AddResource("cloth", -2)
                    GameData.AddLog("修了灶房，加砌了防火墙，粮食布匹损失了一些。")
                end },
                { text = "简单修补，将就着用（-2银两）", result = "花了少许银两请人糊了泥墙、换了几片瓦，灶房勉强能用。只是被烧的粮食和布匹已无法挽回，一家老小望着焦黑的墙壁，暗暗叹息。所幸天渐转凉，不至于再生火患。", effect = function()
                    GameData.AddResource("silver", -2)
                    GameData.AddResource("grain", -8)
                    GameData.AddResource("cloth", -3)
                    GameData.AddLog("简单修了修灶房，但损失的粮食布匹不少。")
                end },
                { text = "先不管灶房，抢救粮仓", result = "族人齐心协力，先将粮仓中的粮食搬到安全之处，保住了大半口粮。然而灶房已烧成一片废墟，存放其中的布匹尽数化为灰烬。此后数月只能在露天搭灶做饭，颇为不便。", effect = function()
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
                { text = "用粮食换丝绸布匹（-8粮食，+6布匹）", result = "以自家余粮换得江南上等丝绸六匹，色泽光润、织工精细，在本地实属罕见。族人将丝绸妥善收存，日后逢年过节或婚丧嫁娶皆可裁衣赠礼，又或转手卖出再赚一笔，甚为划算。", effect = function()
                    GameData.AddResource("grain", -8)
                    GameData.AddResource("cloth", 6)
                    GameData.AddLog("拿粮食换了上好的江南丝绸，赚了。")
                end },
                { text = "花银子买些稀罕物件（-3银两，+2布匹）", cost = {silver = 3}, result = "花银子置办了些南方货物，有几匹细布和一套青花瓷碗碟。妇人们爱不释手，孩子们尝了蜜饯果干更是欢天喜地。虽非大宗买卖，却也为寻常日子添了几分新鲜与欢乐。", effect = function()
                    GameData.AddResource("cloth", 2)
                    GameData.AddLog("买了些外地货物，添置了些家用。")
                end },
                { text = "只是看看，不买不换", result = "跟着邻里在行商车队旁看了半日热闹，虽觉那些南方物件精巧可爱，终究舍不得花销。行商次日便启程离去，族人虽有几分遗憾，但想想银粮尚在手中，也就释然了。", effect = function()
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
                { text = "不惜代价请名医诊治（-5银两）", cost = {silver = 5}, result = "族人不惜倾囊，从镇上请来了一位悬壶济世的老大夫。大夫望闻问切后开了几副猛药，又嘱咐了忌口调养之法。半月之后，长辈渐能下床行走，族人无不感念孝心，乡邻也交口称赞。", effect = function()
                    GameData.AddResource("fame", 2)
                    GameData.AddLog("花重金请来名医，" .. eName .. "病情好转，族人称赞孝心。")
                end },
                { text = "先用土方子调理（-1银两）", effect = function(self)
                    GameData.AddResource("silver", -1)
                    if math.random() > 0.5 then
                        self.result = "族人四处搜寻草药偏方，每日煎熬汤药悉心照料。也是天可怜见，那土方子竟渐渐见了效，长辈的咳嗽日轻一日，月余之后已能在院中晒太阳。众人皆道是祖宗保佑，庆幸不已。"
                        GameData.AddLog("用草药土方调理，" .. eName .. "的病渐渐好了。")
                    else
                        self.result = "用了几服草药土方，初时似有好转，不料数日后病势反复，咳血不止，较前更为沉重。族人懊悔不已，只恨当初舍不得银两请名医，如今再请怕是已迟了几分。"
                        GameData.AddLog("土方子不管用，" .. eName .. "的病越来越重了。")
                    end
                end },
                { text = "求神拜佛，烧香祈福", result = "族人往土地庙烧了三炷高香，又请了庙祝念经祈福，盼望神灵庇佑长辈康复。然而药石不进，病体日衰，终究只能听天由命。族中上下忧心忡忡，只盼老天开眼，赐一线转机。", effect = function()
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
                { text = "设香案酬谢天恩（-1银两，+3声望）", cost = {silver = 1}, result = "择了吉日在堂前设下香案，供了三牲果品，焚香祭拜以谢天恩。消息传开，四邻争相前来观瞻，都说此家必有后福。族人受此鼓舞，干劲十足，那一季的庄稼长势格外喜人，果然应了吉兆。", effect = function()
                    GameData.AddResource("fame", 3)
                    GameData.AddResource("grain", 3)
                    GameData.AddLog("设香案祭拜，族人士气高涨，劳作更加卖力。")
                end },
                { text = "趁吉兆做些大事（鼓励族人开荒）", result = "趁着这股子好兆头，族长召集众人，提议将村后那片荒坡开垦出来。族人士气正盛，齐心协力披荆斩棘，不出两月便整出了好几亩新田。秋后一算，多收了不少粮食，人人喜笑颜开。", effect = function()
                    GameData.AddResource("grain", 6)
                    GameData.AddResource("fame", 1)
                    GameData.AddLog("趁吉兆鼓舞人心，组织族人开垦荒地，多收了粮食。")
                end },
                { text = "心中欢喜，但不做声张", result = "虽未大肆张扬，但心中暗喜，做事也多了几分从容与底气。那对喜鹊在槐树上筑了巢，日日啼鸣不绝，族人每每抬头望去，便觉日子有盼头，平添了几分安宁与笃定。", effect = function()
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
                { text = "主动缴纳军粮以求安稳（-8粮食，+3声望）", cost = {grain = 8}, result = "族中主动将军粮装车送至镇上粮库，里正亲自接收并记了功。此后征粮差役到村时，直接跳过了此家。乡邻见状纷纷效仿，族中也因深明大义而在村中威望更高，遇事多有人帮衬。", effect = function()
                    GameData.AddResource("fame", 3)
                    GameData.AddLog("主动上缴军粮，被里正记了功，免去了后续征调。")
                end },
                { text = "悄悄把粮食藏起来", effect = function(self)
                    if math.random() > 0.4 then
                        self.result = "连夜将粮食分装入坛，埋于后院老槐树下，地面铺上落叶杂草，做得天衣无缝。差役来时四处搜了一遍，只找到些糠麸，便骂骂咧咧地走了。一家人悬着的心这才放下，暗自庆幸。"
                        GameData.AddLog("偷偷藏好了粮食，差役来时搪塞了过去。")
                    else
                        self.result = "藏粮之事不知被何人告了密，差役带人直奔后院掘地三尺，将埋藏的粮食尽数起出。不但粮食全被征走，还被罚了银两，里正更是当众斥责，颜面尽失，好一阵子在村中抬不起头来。"
                        GameData.AddResource("grain", -10)
                        GameData.AddResource("fame", -3)
                        GameData.AddLog("藏粮被差役发现，不但粮食被征还挨了罚。")
                    end
                end },
                { text = "送族中青壮去从军（+5声望）", cost = {grain = 3}, result = "族中挑了两名身强力壮的后生，备了干粮盘缠送去从军。临行之日全村相送，里正亲题'忠义之家'匾额悬于门楣。虽家中少了劳力，但此举深得官府看重，日后若有功名传来，更是光耀门楣。", effect = function()
                    GameData.AddResource("fame", 5)
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
                { text = "买三头猪崽好好养（-3银两，-5粮食）", cost = {silver = 3, grain = 5}, result = "买了三头壮实的猪崽回来，在院角搭了猪圈，每日精心喂养。几个月下来，猪崽子长得膘肥体壮，年底拉到镇上集市，被酒楼掌柜一眼相中，高价全收了去。算下来净赚了不少银两，一家人欢喜不已。", effect = function()
                    GameData.AddResource("silver", 8)
                    GameData.AddLog("买了三头猪崽精心饲养，年底卖了个好价钱。")
                end },
                { text = "买一头试试（-1银两，-2粮食）", cost = {silver = 1, grain = 2}, result = "买了一头猪崽养在后院，虽不费太多功夫，日日添些剩饭菜汤倒也养得圆润。腊月里杀了年猪，腌了满满两缸腊肉，又灌了几挂香肠。过年时桌上多了荤腥，孩子们吃得满嘴流油。", effect = function()
                    GameData.AddResource("silver", 3)
                    GameData.AddLog("养了一头猪，年底杀了过年，还剩些腊肉腌肉。")
                end },
                { text = "养猪太费粮食，算了", result = "思来想去，眼下粮食金贵，养猪实在耗费不起。婉言谢了邻家好意，将省下的口粮留作他用。虽少了一桩进项，但也免了日日喂猪的辛劳，专心务农倒也安稳。", effect = function()
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
                { text = "翻新加固，一劳永逸（-5银两）", cost = {silver = 5}, result = "请了镇上最好的泥瓦匠，将屋顶掀了重盖，换上新瓦新梁，东墙也拆了重砌，比原先还结实三分。完工那日，新屋在阳光下焕然一新，族人心中踏实了许多。此后任凭风吹雨打，再不见半点渗漏。", effect = function()
                    GameData.AddResource("fame", 1)
                    GameData.AddLog("花银子把房屋翻新加固了，再大的雨也不怕。")
                end },
                { text = "小修小补，先对付着（-2银两）", cost = {silver = 2}, result = "请了泥瓦匠来修补了屋顶漏处，又在墙缝里灌了石灰浆。虽非长久之计，但一时半会儿总不会再漏了。族人心想，等攒够了银子再做翻新也不迟，暂且先将就度日。", effect = function()
                    GameData.AddLog("请人简单修了修屋顶和墙壁，暂时不漏了。")
                end },
                { text = "自己动手糊些泥巴", result = "族人自己和了黄泥，爬上屋顶将漏处糊了一遍，墙缝也抹了泥巴。看着虽不美观，好歹暂时挡住了雨水。只是心知黄泥不耐久，下次大雨一来怕是又要忙活一番，日子过得捉襟见肘。", effect = function()
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
                { text = "花钱打发走（-3银两）", cost = {silver = 3}, result = "忍着一肚子气掏了银子打发走了那帮混混。赖三拿了银子嘻嘻哈哈地走了，临走还撂下话说下回再来。族人虽免了一顿打砸，心中却窝火得很，暗暗盘算着日后如何应对这无赖。", effect = function()
                    GameData.AddLog("忍气吞声交了银子，打发走了地痞。")
                end },
                { text = "联合邻里一起对抗", effect = function(self)
                    local warriors = GameData.GetMembersByState("习武")
                    if #warriors > 0 then
                        self.result = "族中习武之人挺身而出，一声吆喝间邻里青壮也纷纷赶来助阵。赖三那帮混混见来者不善，吓得抱头鼠窜。自此泼皮再不敢登门，族中威望大涨，被乡邻奉为主心骨。"
                        GameData.AddResource("fame", 4)
                        GameData.AddLog("族中有习武之人，联合邻里把地痞赶走了，威望大增。")
                    else
                        self.result = "虽族中无习武之人，但邻里同仇敌忾，锄头扁担齐上阵，硬是将赖三一伙吓退了去。混战中虽折了些物件，却也出了一口恶气。此后泼皮有所收敛，不敢再轻易上门。"
                        GameData.AddResource("fame", 2)
                        GameData.AddResource("silver", -1)
                        GameData.AddLog("邻里合力赶走了泼皮，虽有些损失，但出了口气。")
                    end
                end },
                { text = "去县衙告状（-2银两，走衙门程序）", cost = {silver = 2}, effect = function(self)
                    if math.random() > 0.5 then
                        self.result = "族人写了状子递到县衙，县太爷恰好正要整治乡间治安，当即派了衙役将赖三拿获，打了二十大板关进了牢房。村中百姓奔走相告，族中也因仗义执言而备受敬重。"
                        GameData.AddResource("fame", 3)
                        GameData.AddLog("县太爷接了状子，派人拿了赖三，村中太平了。")
                    else
                        self.result = "状子虽递到了县衙，奈何赖三在衙门里有些关系，胥吏收了好处便将此事压了下来。赖三被放出后变本加厉，扬言要报复告状之人，族中反而更加不安，悔不该走了这条路。"
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
                { text = "花钱紧急翻晒抢救粮食（-3银两，减少损失）", result = "紧急雇了十几个帮工，趁着雨歇的间隙将粮仓里的粮食搬出来翻晒。虽说霉变了一些，但大部分粮食抢救了下来。又在仓底铺了石灰和干草防潮，总算将损失降到了最低。", effect = function()
                    GameData.AddResource("silver", -3)
                    GameData.AddResource("grain", -6)
                    GameData.AddLog("雇了人手抢救粮仓，翻晒受潮粮食，保住了大半。")
                end },
                { text = "挖排水沟保田（-2银两）", result = "族人冒雨在田间挖出了几条排水沟，将积水引向低洼处。田里的庄稼虽保住了部分，但粮仓因无暇顾及而损失颇重。连日劳作之下，几个族人还受了风寒，好在并无大碍。", effect = function()
                    GameData.AddResource("silver", -2)
                    GameData.AddResource("grain", -10)
                    GameData.AddLog("拼命挖沟排水，田里的庄稼保住了一些，但粮仓损失不小。")
                end },
                { text = "只能熬着等雨停", result = "眼睁睁看着雨水一日日地下，粮仓里的粮食发了霉长了绿毛，布匹也潮得拧出水来。一家人挤在漏雨的屋里，柴火湿透烧不着，只能啃冷饭度日。待到雨停时，损失已是触目惊心，来年的日子更难捱了。", effect = function()
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
                { text = "收留下来，当自家孩子养（-3粮食/季，+3声望）", cost = {grain = 3}, result = "将孤儿带回家中，洗了澡换了干净衣裳，又喂了几碗热粥。孩子渐渐恢复了精神，怯生生地唤了一声'恩人'。族人给他取了名字编入族谱，虽多了一张嘴吃饭，但乡邻无不赞叹此家积德行善，日后必有好报。", effect = function()
                    GameData.AddResource("fame", 3)
                    GameData.AddLog("收留了孤儿，多了一口人吃饭，但也积了德行。")
                end },
                { text = "先收留，再送给镇上无子人家（+1声望）", result = "先将孤儿收留了几日，喂饱养好了些。后来打听到镇上有一户善心人家膝下无子，便托人说合，将孩子送了过去。那户人家甚是欢喜，待孩子如亲生一般。逢年过节还带着孩子来道谢，也算续了一段善缘。", effect = function()
                    GameData.AddResource("grain", -1)
                    GameData.AddResource("fame", 1)
                    GameData.AddLog("暂时收留了孤儿，后来送给了镇上一户好人家。")
                end },
                { text = "给些干粮让他继续赶路", result = "包了几个馍馍、一壶水塞给孩子，又指了去镇上的路。孩子抱着干粮千恩万谢地走了。族人望着那瘦小的背影消失在田埂尽头，心中五味杂陈，总觉得于心有愧，夜里辗转难眠。", effect = function()
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
                { text = "积极参与，多出份子（-3银两，-3粮食，+4声望）", cost = {silver = 3, grain = 3}, result = "族中带头出了大份子，银粮俱丰，众人公推其为互助社管事。此后凡社中大小事务皆由族人主持，账目清明、调度有方，深得各家信任。有了这层关系，日后遇着急难之事，八方都有人伸手相助。", effect = function()
                    GameData.AddResource("fame", 4)
                    GameData.AddLog("带头参与互助社，出了大份子，被推举为管事。")
                end },
                { text = "参与，但只出基本份子（-1银两，-2粮食，+2声望）", cost = {silver = 1, grain = 2}, result = "出了基本份子加入了互助社，虽非大户，但也算有了保障。此后族中偶遇周转不灵之时，便从社中支取了些银粮应急，来年如数归还。有此互助之约，日子过得安稳了许多。", effect = function()
                    GameData.AddResource("fame", 2)
                    GameData.AddLog("加入了互助社，出了基本份子，有了互相照应的保障。")
                end },
                { text = "不参与，自家管自家", result = "婉拒了邻里的邀约，只说自家日子自家过，不愿与人搅在一处。此举被村人议论纷纷，都说此家不合群、不讲情面。此后乡邻有事也不再来商量，族中渐渐被孤立在村中人情之外。", effect = function()
                    GameData.AddResource("fame", -2)
                    GameData.AddLog("没有加入互助社，被人说不合群。")
                end },
            }
        }
    end,
}

return events
