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
                    result = "一番仔细采挖，黄芪甘草装了满满一背篓。赶到集市时，正巧药铺掌柜急需补货，痛快地给了个好价钱。三两白银入手，虽不算多，却也够家中宽裕些时日了。",
                    effect = function()
                        GameData.AddResource("silver", 3)
                        GameData.AddLog(name .. "采得野药，售于集市，得银三两。")
                        report.events[#report.events + 1] = name .. "采药得银三两"
                    end
                },
                {
                    text = "留作自家备用，以防疾病",
                    result = "将采来的药材仔细晾晒收好，挂在灶房梁上。入冬后家中老幼果然有人感了风寒，熬了两副药便好了，省下了一笔请大夫的银钱。",
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
                    result = "挑着断犁赶到镇上铁匠铺，王铁匠看了看断口，说是老铁犁了，接口处锈蚀所致。炉火通红中锤打了半个时辰，犁头焕然一新。赶回家时正好赶上翻地，一点不误农时。",
                    effect = function()
                        GameData.AddLog(name .. "花二两银子修好了犁头，不误农时。")
                        report.events[#report.events + 1] = name .. "花银修犁"
                    end
                },
                {
                    text = "自己凑合用木犁替代",
                    result = "削了根硬木做犁头，凑合着下了地。木头哪比得上铁器，翻地费力不说，深度也不够。忙活了大半月，这一季的收成比往年差了一大截。",
                    effect = function()
                        GameData.AddResource("grain", -8)
                        GameData.AddLog(name .. "以木犁代铁犁，费力倍增，本季收成减损不少。")
                        report.events[#report.events + 1] = name .. "木犁代铁犁，收成减损"
                    end
                },
                {
                    text = "向邻家借用农具",
                    result = "硬着头皮去了邻家门上，人家虽然借了，话里话外却不大痛快。借来的犁也不趁手，勉强耕完了地，又赶紧还回去。这份人情，日后总要还的。",
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
                    result = "连着帮了半个月，把邻翁家的地翻了、苗间了、水也浇足。老翁病愈后拄着拐杖来道谢，非要磕头作揖。村里人见了，都说咱家仁义，这份好名声传了十里八乡。",
                    effect = function()
                        GameData.AddResource("fame", 3)
                        GameData.AddLog(name .. "义助邻翁料理农田，乡邻交口称赞。")
                        report.events[#report.events + 1] = name .. "助邻耕田，声望渐长"
                    end
                },
                {
                    text = "送些粮食过去接济",
                    cost = {grain = 5},
                    result = "装了五斗粮食送到邻翁家中，老翁的儿媳接过粮袋时眼眶都红了。虽说自家也紧巴巴的，但看着人家锅里终于有了米，心里也踏实了些。",
                    effect = function()
                        GameData.AddResource("fame", 2)
                        GameData.AddLog(name .. "赠粮五斗予病邻，虽家中清苦，却得邻里敬重。")
                        report.events[#report.events + 1] = name .. "赠粮济邻"
                    end
                },
                {
                    text = "自家也忙不过来，只能作罢",
                    result = "自家田地也忙不过来，实在抽不出人手。邻翁家的庄稼后来果然荒了大半。乡邻们背后议论，说咱家见死不救，虽然也是无奈之举，但这闲话终究不好听。",
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
                    result = "连夜敲锣打鼓，召集了七八个青壮汉子，举着火把冲进田间。火光映天，野猪被吓得四散奔逃。虽说庄稼已被糟蹋了一些，但好在发现得早，大半田地保住了。",
                    effect = function()
                        GameData.AddResource("grain", -5)
                        GameData.AddLog(name .. "率族人驱赶野猪，庄稼损失了一些，但好在及时止损。")
                        report.events[#report.events + 1] = "野猪侵田，庄稼小损"
                    end
                },
                {
                    text = "设陷阱捕猎野猪",
                    effect = function(self)
                        local luck = math.random(1, 10)
                        if luck <= 6 then
                            GameData.AddResource("grain", -3)
                            GameData.AddResource("silver", 2)
                            GameData.AddLog(name .. "设陷阱捕得一头野猪，卖了二两银子，田地小有损失。")
                            report.events[#report.events + 1] = "捕得野猪换银"
                            self.result = "在田边挖了深坑，上面覆上树枝杂草。第二夜果然听到一声惨叫，赶去一看，一头百来斤的野猪掉进了坑里。拖到集市上卖了二两银子，虽说田地还是被拱坏了一些，但也算因祸得福。"
                        else
                            GameData.AddResource("grain", -8)
                            GameData.AddLog("陷阱未果，野猪又来糟蹋了一夜，损失颇重。")
                            report.events[#report.events + 1] = "捕猪未果，损失加重"
                            self.result = "忙活了一整天挖陷阱，谁知那野猪精得很，绕过了陷阱继续糟蹋庄稼。又白搭了一夜功夫，田地被拱得更惨了，损失比昨夜还重。"
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
                    cost = {silver = 2},
                    effect = function(self)
                        local luck = math.random(1, 10)
                        if luck <= 4 then
                            GameData.AddResource("grain", 10)
                            GameData.AddResource("fame", 2)
                            GameData.AddLog("祈雨之后三日果然天降甘霖，庄稼得救！族人出银有功，乡邻赞许。")
                            report.events[#report.events + 1] = "祈雨应验，旱情解除"
                            self.result = "道士在祭坛上做了三天法事，第三日傍晚果然乌云密布、电闪雷鸣，大雨倾盆而下！干涸的田地喝饱了水，枯黄的庄稼竟慢慢又绿了回来。乡亲们奔走相告，都说咱家出银有功，是积了大德。"
                        else
                            GameData.AddResource("fame", 1)
                            GameData.AddLog("祈雨法事做罢，天空依旧晴朗，银子倒是花了出去。好在出了份心意，邻里记着。")
                            report.events[#report.events + 1] = "祈雨未验，聊表心意"
                            self.result = "道士念了三天经，烧了一堆纸符，天空却依旧万里无云。银子虽然打了水漂，但好歹出了份心意，乡邻们倒也念着这份情面，没人说什么闲话。"
                        end
                    end
                },
                {
                    text = "不出银，自家挑水浇地",
                    result = "没参与祈雨，每日天不亮就挑着水桶去河边，来回几趟浇地。累得腰酸背痛不说，那点水也只够保住最要紧的几亩地，其余的只能眼睁睁看着枯死。这一季的收成，怕是要少上不少。",
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
                    cost = {grain = 3},
                    result = "做了一桌粗茶淡饭招待僧人，老僧吃得很欢喜，饭后盘腿坐在院中，为全家念了一卷《金刚经》。走时双手合十，说施主心善必有福报。左邻右舍听说了，都夸咱家厚道。",
                    effect = function()
                        GameData.AddResource("fame", 2)
                        GameData.AddLog("施舍行脚僧人饭食，僧人合掌道谢，为全家诵经祈福。乡邻皆赞善心。")
                        report.events[#report.events + 1] = "施舍游僧，积德行善"
                    end
                },
                {
                    text = "请教养生调理之法",
                    cost = {grain = 2},
                    result = "僧人听说家中有人体弱多病，仔细号了脉，传授了几味药膳方子——黄芪炖鸡、枸杞莲子粥，都是寻常食材。照着做了半月，体弱的族人气色果然好了许多，夜里也不咳嗽了。",
                    effect = function()
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
                    result = "僧人在村口的老槐树下歇了一会儿，喝了几口井水便继续赶路了。没什么事发生，日子照旧过。只是后来听说他去了隔壁村，治好了王家老太的老寒腿，倒有些可惜。",
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
                    cost = {silver = 3},
                    result = "咬咬牙凑了三两束脩银，把孩子送进了镇上的私塾。先生教了几日便啧啧称奇，说此子过目不忘，是个读书的苗子。虽然家里日子更紧巴了，但看着孩子捧着书本两眼放光的样子，觉得一切都值了。",
                    effect = function()
                        if child.stats then
                            child.stats["文"] = (child.stats["文"] or 0) + 5
                        end
                        GameData.AddLog(name .. "天资聪颖，家中节衣缩食送其入塾读书。")
                        report.events[#report.events + 1] = name .. "入塾启蒙"
                    end
                },
                {
                    text = "让孩子先帮家里干活",
                    result = "孩子听话地收起了树枝，跟着大人下地干活去了。割草、喂鸡、挑水，样样做得利索。只是偶尔发呆时，会用手指在泥地上划拉几个字。先生后来再没提过这事，孩子的天赋也就这么搁下了。",
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
                    cost = {silver = 3},
                    result = "从柜底翻出攒了许久的碎银子，凑够三两如数交上。差役验过银两成色，在册子上画了个勾，翻身上马扬长而去。虽说心疼银钱，但好歹省了一桩麻烦，踏踏实实过日子。",
                    effect = function()
                        GameData.AddLog("如数缴了田赋银三两，差役收银后扬长而去。")
                        report.events[#report.events + 1] = "缴纳田赋银三两"
                    end
                },
                {
                    text = "用粮食折抵税银",
                    cost = {grain = 10},
                    result = "搬出十斗粮食堆在差役面前，差役掂了掂分量，嘟囔着说粮价折低了，不大划算。好说歹说总算收下了。十斗粮食换三两税银，亏是亏了些，但总比被加收三成罚款强。",
                    effect = function()
                        GameData.AddLog("银钱不凑手，只得用十斗粮食折抵税银，差役虽勉强收下，却嫌粮价折低了。")
                        report.events[#report.events + 1] = "以粮折税"
                    end
                },
                {
                    text = "哀求宽限些时日",
                    result = "跪在差役马前苦苦哀求，差役不耐烦地甩了甩马鞭。最后悄悄塞了一两银子过去，差役这才冷哼一声说宽限五日。站起来时膝盖上全是土，乡邻们远远看着，虽没说什么，但那眼神让人难受。",
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
                    cost = {silver = 1, grain = 3},
                    result = "一家人顶着瓢泼大雨，肩扛手提把粮食搬到了村后的高坡上，又用油布盖得严严实实。折腾了一整夜，浑身湿透。后来河水果然涨了，低处好几家都遭了灾，唯独咱家粮食保住了。虽然花了些银钱搬运，但庆幸不已。",
                    effect = function()
                        GameData.AddLog("全家老小冒雨将粮食搬到高处。虽折腾了一番，但若真发大水，损失可就小多了。")
                        report.events[#report.events + 1] = "提前转移粮食以防洪水"
                    end
                },
                {
                    text = "听天由命，不作准备",
                    effect = function(self)
                        local luck = math.random(1, 10)
                        if luck <= 5 then
                            GameData.AddResource("grain", -10)
                            GameData.AddResource("cloth", -3)
                            GameData.AddLog("大水果然漫过堤坝，田地被淹，存粮受损严重，布匹也被泡坏了。早知如此！")
                            report.events[#report.events + 1] = "洪水漫堤，损失惨重"
                            self.result = "第二天夜里，洪水果然漫过了堤坝，浑浊的大水灌进了村子。等水退去，仓房里的粮食泡烂了大半，箱子里的布匹也全毁了。看着满地泥泞和残破的家当，全家人欲哭无泪。早知道就该听上游的预警啊！"
                        else
                            GameData.AddLog("所幸雨势渐小，河水虽涨却未决堤，虚惊一场。")
                            report.events[#report.events + 1] = "洪水虚惊，平安无事"
                            self.result = "提心吊胆地等了两天，雨势居然渐渐小了。河水虽然涨得很高，但终究没有漫过堤坝。虚惊一场，全家人长舒了一口气。这次算是走了运，不过下回还是该早做准备的好。"
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
                    cost = {grain = 2},
                    effect = function(self)
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
                        self.result = "给黄狗洗了澡，喂了几顿饱饭，没几日便养得精神了起来。起名叫「" .. name .. "」，白天在院子里撒欢跑，晚上趴在门口守夜，耳朵一动便竖起来。有了它看家，夜里睡觉也踏实多了。"
                    end
                },
                {
                    text = "给些吃的打发走",
                    result = "舀了一碗剩饭放在门口，黄狗摇着尾巴吃得干干净净。吃完后舔了舔嘴，朝你望了一眼，似乎有些留恋，但终究还是转身跑远了。也许它会找到另一户愿意收留它的人家吧。",
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
                    cost = {silver = 3},
                    result = "全家青壮轮番上阵挖了七八天，挖到三丈深时终于见了水。清冽的井水汩汩涌出，围观的乡亲们欢呼雀跃。咱家出银出力最多，里正特意在井旁立了块石头刻上名字。有了这口井，浇田取水都方便了，粮食产量也跟着提了上来。",
                    effect = function()
                        GameData.AddResource("grain", 8)
                        GameData.AddResource("fame", 3)
                        GameData.AddLog("出银三两、出力数日，水井终于挖成。清冽井水汩汩而出，日后浇田取水大为便利。族人出力有目共睹，乡邻称颂。")
                        report.events[#report.events + 1] = "合力打井成功，获声望"
                    end
                },
                {
                    text = "出少许银两意思一下",
                    cost = {silver = 1},
                    result = "象征性掏了一两银子交上去，打井时也去帮了几回忙。井倒是打成了，水也有得用，但毕竟出力少，排队打水时总得往后站站。不过好歹省了不少挑水的脚力。",
                    effect = function()
                        GameData.AddResource("grain", 3)
                        GameData.AddLog("象征性出了一两银子，井倒是打成了，但出力少，分水时排在后头。")
                        report.events[#report.events + 1] = "少出银打井，受益有限"
                    end
                },
                {
                    text = "不参与，继续挑水",
                    result = "没出银也没出力，照旧每天跑河边挑水。等井打成了，别人家都用新井水浇地，自己却不好意思凑过去。背后有人说闲话，说咱家不合群。这口气堵在心里，但也怪不得旁人。",
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
                    cost = {grain = 8},
                    result = "挑着八斗粮食到了集市，米行掌柜验了成色，说今年新米品相不错，给了个还算公道的价。四两银子沉甸甸地揣在怀里，回去的路上脚步都轻快了些。",
                    effect = function()
                        GameData.AddResource("silver", 4)
                        GameData.AddLog(name .. "赶集卖了八斗粮食，得银四两。")
                        report.events[#report.events + 1] = name .. "赶集卖粮得银"
                    end
                },
                {
                    text = "买些布匹回来",
                    cost = {silver = 2},
                    result = "在布行挑了半天，选了几匹结实的土布，颜色虽素淡，但织得紧密耐穿。掌柜见是老主顾，还多饶了半尺。回家后一家人高高兴兴地比划着裁新衣。",
                    effect = function()
                        GameData.AddResource("cloth", 5)
                        GameData.AddLog(name .. "赶集买了五匹土布，价钱还算公道。")
                        report.events[#report.events + 1] = name .. "赶集买布"
                    end
                },
                {
                    text = "只是逛逛，打听消息",
                    result = "在集市上逛了大半天，没买什么东西，倒是跟几个走南闯北的商客攀谈甚欢。听了不少外头的新鲜事，也认识了几个面孔。这份人脉，说不定日后能派上用场。",
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
                    result = "连忙跑去找里正说了情况，里正当即召集青壮年分班巡夜。入夜后果然有黑影在村口晃悠，见到巡逻的火把便缩了回去。连着巡了三夜，贼人再没敢来。乡亲们都夸咱家警觉，为村子免了一场祸。",
                    effect = function()
                        GameData.AddResource("fame", 3)
                        GameData.AddLog(name .. "警觉发现贼人踩点，及时通知里正。村中连夜巡逻，贼人见有防备便散了。")
                        report.events[#report.events + 1] = name .. "发觉贼踪，组织巡夜"
                    end
                },
                {
                    text = "只管好自家门户",
                    effect = function(self)
                        local luck = math.random(1, 10)
                        if luck <= 3 then
                            GameData.AddResource("silver", -2)
                            GameData.AddLog("夜里贼人果然来了，邻家被盗了不少东西，自家也丢了些银钱。")
                            report.events[#report.events + 1] = "夜盗来袭，自家失银"
                            self.result = "锁好了门闩，在窗口支了根棍子抵着。半夜果然听到隔壁院子有动静，接着是一阵鸡飞狗跳。天亮后才知道，邻家被翻了个底朝天，自家院墙边也有翻动的痕迹，柜子里少了些碎银。光顾自家，到底还是没能幸免。"
                        else
                            GameData.AddLog("锁好了门窗，夜里倒也平安无事。只是邻家似乎丢了些东西。")
                            report.events[#report.events + 1] = "锁门自保，安然无恙"
                            self.result = "把门窗都上了闩，院子里放了几个瓦罐当作警报。提心吊胆地过了一夜，好在贼人没来咱家。第二天听说邻村有人被偷了，自家倒是平安无事。虽说躲过一劫，但想想也有些后怕。"
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
                    result = "跟着老婆婆上了半个月的山，学会了辨认黄连、半夏、柴胡等十余味常用药材，还知道了哪些草根能退烧、哪些树皮能止泻。老婆婆说这些本事虽不起眼，但关键时候能救命。回来后乡邻们有个头疼脑热的，都来问两句。",
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
                    result = "地里的活实在丢不开，只好婉拒了老婆婆的好意。老婆婆倒也不强求，叹了口气说'可惜了'。后来听说她教了隔壁村一个后生，那后生如今成了远近闻名的草药郎中。每每想起，心中不免有些遗憾。",
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
                    cost = {silver = 3},
                    result = "带头掏了三两银子，又带着家中青壮搬石运料。有了带头人，其他各户也纷纷响应。十来天功夫，一座崭新的石板桥便立了起来，比原来的还要宽敞结实。过路的行商每每问起，乡亲们都说是咱家牵头修的，名声传出好远。",
                    effect = function()
                        GameData.AddResource("fame", 5)
                        GameData.AddLog("带头出银三两修桥，乡邻纷纷响应。桥修好后，路人皆赞吾家仁义。")
                        report.events[#report.events + 1] = "带头修桥，声望大增"
                    end
                },
                {
                    text = "出些力气帮忙搬石头",
                    result = "虽没银子可出，但实打实地搬了好几天石头，累得腰都直不起来。桥修好那天，里正当众念了出力的人家名字，咱家也在其中。虽比不上出银的那几户风光，但乡亲们也记着这份情。",
                    effect = function()
                        GameData.AddResource("fame", 2)
                        GameData.AddLog("虽无余银，但出力搬石修桥，乡邻也记着这份情。")
                        report.events[#report.events + 1] = "出力修桥"
                    end
                },
                {
                    text = "不参与，绕路走",
                    result = "没出银也没出力，每天绕远路走小道过河。桥修好后别人高高兴兴地走新桥，自己走上去总觉得不自在。有一回在桥上碰到里正，里正笑了笑没说什么，但那笑容让人浑身不得劲。",
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
                    result = "全家老少齐上阵，拿着扫帚、竹竿、簸箕漫天扑打。蝗虫太多，打死一批又来一批，从清晨打到天黑，累得几乎瘫倒。田地终究还是损失了不少，但好歹保住了一半。看看邻家颗粒无收的惨状，已经算是不幸中的万幸了。",
                    effect = function()
                        GameData.AddResource("grain", -8)
                        GameData.AddLog("全家持扫帚、竹竿奋力扑打蝗虫，虽拼尽全力，田地仍损失过半。但比起邻家颗粒无收，已算幸运。")
                        report.events[#report.events + 1] = "蝗灾来袭，奋力抢救，损失减半"
                    end
                },
                {
                    text = "点火烧田驱蝗",
                    effect = function(self)
                        local luck = math.random(1, 10)
                        if luck <= 5 then
                            GameData.AddResource("grain", -5)
                            GameData.AddLog("火烧蝗虫颇有成效，驱散了大部分蝗群，田地损失不算太大。")
                            report.events[#report.events + 1] = "火攻驱蝗，损失可控"
                            self.result = "在田边点起了火堆，浓烟滚滚中蝗虫纷纷避散。火势控制得当，烧掉了田边一小片荒草，却把大部分蝗群赶跑了。庄稼虽有损失，但比扑打管用得多，算是明智之举。"
                        else
                            GameData.AddResource("grain", -10)
                            GameData.AddLog("火势失控烧了不少庄稼，蝗虫虽散，自家田地也毁了大半。得不偿失。")
                            report.events[#report.events + 1] = "火攻失控，损失惨重"
                            self.result = "本想用火驱赶蝗虫，谁知风向突变，火苗窜进了庄稼地里。蝗虫倒是被烟熏跑了，可自家的田也烧了一大片。等扑灭余火时，田地已经毁了大半。真是赔了夫人又折兵，悔不当初。"
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
                    result = "接过干粮和伤药，千恩万谢地记下了行商的模样。行商笑着摆摆手说'不过举手之劳'，便驾车走了。那包伤药果然好使，敷了两天脚伤便消了。这份恩情，铭记在心，日后若有机会定当报答。",
                    effect = function()
                        GameData.AddResource("grain", 3)
                        GameData.AddResource("fame", 1)
                        GameData.AddLog(name .. "受好心行商相助，得干粮数斤。记下恩情，日后定当报答。")
                        report.events[#report.events + 1] = "得路人相助"
                    end
                },
                {
                    text = "追问行商姓名来路",
                    result = "趁着歇脚的功夫和行商聊了起来，原来他姓刘，是从府城来的布商，每月走这条路贩货。两人越聊越投缘，刘掌柜见咱家诚恳，不仅多给了些干粮，还塞了一两银子说'交个朋友'，约好下次赶集时再叙。",
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
                    result = "把两家人请到自家堂屋，好茶好水伺候着，先听赵家说，再听李家讲。待双方说完了气也消了大半，趁热打铁提议各退半分地做公用小路。两家一琢磨倒也合理，当场握手言和。事后里正逢人便夸咱家公道，村里再有什么纠纷，头一个想到的就是咱。",
                    effect = function()
                        GameData.AddResource("fame", 3)
                        GameData.AddLog(name .. "秉公调解邻里田界纠纷，两家各让半分地，握手言和。乡邻皆赞其公道。")
                        report.events[#report.events + 1] = name .. "调解邻里纷争，声望渐隆"
                    end
                },
                {
                    text = "请两家喝酒化解矛盾",
                    cost = {silver = 1},
                    result = "自掏腰包在家中摆了一桌，把赵家李家的当家人都请了来。起初两人还板着脸不说话，三杯酒下肚便你一言我一语地聊开了。到最后两人勾肩搭背称兄道弟，说什么'都是邻居，为几分地伤了和气不值当'。虽花了些银钱，但两家的情分算是结下了。",
                    effect = function()
                        GameData.AddResource("fame", 2)
                        GameData.AddLog(name .. "自掏腰包请两家吃酒，酒过三巡，恩怨也化了大半。")
                        report.events[#report.events + 1] = name .. "请酒化纷争"
                    end
                },
                {
                    text = "不掺和这种事",
                    result = "推说自家也忙，没空管这闲事。里正碰了个软钉子，只好另寻他人。后来两家闹腾了大半个月才消停，见了面仍是横眉竖眼。有几次路过他们地头，双方还在那嘀嘀咕咕，弄得周围的人都不自在。",
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
                    cost = {silver = 1},
                    result = "随了一两银子的份子，换上干净衣裳，全家去凑了热闹。晒谷场上摆了十几桌，杀了两头猪，炖得满村飘香。老少爷们儿端着碗你敬我我敬你，孩子们在人群里钻来钻去，笑声不断。今年仓里堆得满满当当，那种踏实的感觉，一整年的辛苦都值了。",
                    effect = function()
                        GameData.AddResource("grain", 15)
                        GameData.AddResource("fame", 2)
                        GameData.AddLog("丰年大收，随了份子参与庆典，仓中粮满。与乡邻推杯换盏，其乐融融。")
                        report.events[#report.events + 1] = "丰收庆典，粮仓充盈"
                    end
                },
                {
                    text = "不参加庆典，专心收粮入仓",
                    result = "趁着天好，把田里最后一茬稻子全收了回来。一担一担往仓里挑，看着金黄的谷堆越来越高，心里比吃席还舒坦。远处传来庆典的锣鼓声和欢笑声，虽然没去凑热闹，但看着满仓的粮食，自有一份安心。",
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
                    cost = {cloth = 4},
                    result = "连夜把存着的布匹翻出来，一家人围坐在油灯下赶制棉衣棉裤。婆婆纳鞋底，媳妇缝棉袄，几天功夫做了好几件厚实的冬衣。虽说布匹用了不少，但一家老小穿上新棉衣，暖和得直叹气。这个冬天，总算不用缩在被窝里发抖了。",
                    effect = function()
                        GameData.AddLog("赶制了几件棉衣，一家老小总算熬过了这个寒冬。布匹消耗不少。")
                        report.events[#report.events + 1] = "用布匹赶制棉衣过冬"
                    end
                },
                {
                    text = "花银子买炭火取暖",
                    cost = {silver = 2},
                    result = "咬咬牙花了二两银子，从镇上拉回来好几篓上等木炭。炭火烧得旺旺的，屋子里暖烘烘的，连窗纸上的冰花都化了。虽然心疼银钱，但看着老人孩子红扑扑的脸蛋，觉得这钱花得值。邻家来串门，一进屋就舍不得走了。",
                    effect = function()
                        GameData.AddLog("花了二两银子买了几篓木炭，虽心疼银钱，好歹屋里暖和了些。")
                        report.events[#report.events + 1] = "买炭取暖过严冬"
                    end
                },
                {
                    text = "硬扛过去，省下物资",
                    result = "省下了布匹和银钱，可这个冬天真不好过。一家人挤在一铺炕上，盖着薄被瑟瑟发抖。老人先咳嗽起来，接着孩子也发了烧，一个传一个，全家都病倒了。等开春天暖了，人虽好了些，但个个面黄肌瘦，元气大伤。",
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
                    result = "二话不说把布包原封不动交给了里正。过了三天，失主找上门来——原来是邻村一个卖货郎，那五两银子是他走亲戚时掉的，急得险些上吊。失主千恩万谢，非要留下一两做谢礼，被婉言谢绝了。这事传开后，十里八乡都知道咱家出了个拾金不昧的好人，连县里的教谕都点了头。",
                    effect = function()
                        GameData.AddResource("fame", 5)
                        GameData.AddLog(name .. "拾银交公，失主寻来千恩万谢。此事传开，人人称赞其品行端正。")
                        report.events[#report.events + 1] = name .. "拾金不昧，声望大增"
                    end
                },
                {
                    text = "悄悄收下补贴家用",
                    result = "四下看看没人注意，把布包往怀里一揣，装作若无其事地回了家。五两碎银是解了不少燃眉之急，但心里总不踏实。每次走过捡到银子的那条田埂，都不自觉地低下头快步走过。后来听说有人在村里打听丢银子的事，更是心虚了好一阵。",
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
                    effect = function(self)
                        if hasWarrior then
                            GameData.AddResource("silver", -1)
                            GameData.AddResource("fame", 2)
                            GameData.AddLog(warriors[1].name .. "持棍追赶夜盗，贼人仓皇翻墙而逃，只丢了些散碎银钱。")
                            report.events[#report.events + 1] = warriors[1].name .. "驱赶夜盗"
                            self.result = warriors[1].name .. "抄起门后的木棍冲了出去，一声大喝把贼人吓了一跳。月光下只见两个黑影翻墙就跑，" .. warriors[1].name .. "追出去好一段路才回来。清点之下，只丢了些柜上的散碎银钱，大件东西都没被动过。多亏家里有个习武的，否则今夜不知要损失多少。"
                        else
                            GameData.AddResource("silver", -2)
                            GameData.AddLog("呼救惊动四邻，贼人闻声而逃，但已窃走银两。")
                            report.events[#report.events + 1] = "夜盗入室，失银二两"
                            self.result = "扯着嗓子大喊'抓贼啊'，声音在夜里传出老远。邻家纷纷点起火把出来查看，贼人见势不妙翻墙跑了。可等回屋一看，柜子已被翻得乱七八糟，压箱底的二两银子不见了。好在人没事，虽丢了银钱，好歹把贼人吓跑了。"
                        end
                    end
                },
                {
                    text = "躲在屋里不出声，等贼走了再查看",
                    result = "一家人挤在里屋，连大气都不敢喘一声。只听外面窸窸窣窣的翻找声足足响了半刻钟，才听到翻墙离去的声响。等确认贼人走远了才敢点灯查看，银子被翻走了三两不说，粮仓也被撬开背走了一袋米。看着满地狼藉，全家人又气又怕。",
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
                    cost = {silver = 2},
                    result = "用红纸包了二两银子，又写了副喜联，体体面面地送了过去。张家一看，连声说'太客气了'。喜宴上坐了上席，新郎新娘挨桌敬酒时特意多说了几句感谢的话。席散后张老爹拉着手说'日后有事只管开口'。这份人情，比银子值钱多了。",
                    effect = function()
                        GameData.AddResource("fame", 3)
                        GameData.AddLog(name .. "备了二两银子的贺礼赴宴，张家大为感动。席间觥筹交错，宾主尽欢。")
                        report.events[#report.events + 1] = name .. "赴宴贺喜，与邻交好"
                    end
                },
                {
                    text = "送些自家粮食布匹",
                    cost = {grain = 5, cloth = 2},
                    result = "挑了五斗好米、两匹上等土布送了过去，虽不是金银，但都是过日子的实在东西。张家媳妇一看那布的花色，喜欢得不得了，当场就说要拿来做新床单。张老爹更是高兴，说'这才是会过日子的人家送的礼'。",
                    effect = function()
                        GameData.AddResource("fame", 2)
                        GameData.AddLog(name .. "送了粮食布匹做贺礼，虽不贵重却是实在物件，张家也很欢喜。")
                        report.events[#report.events + 1] = name .. "送粮布贺喜"
                    end
                },
                {
                    text = "随个小份子意思一下",
                    result = "掏了一两碎银包在红纸里，算是随了个份子。到了喜宴坐在末席，菜倒是不少，酒也管够。虽说份子不重，但好歹人到了场面到了，张家也不会说什么。吃完酒席晃晃悠悠地回了家，算是热闹了一回。",
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
                    cost = {silver = 2},
                    result = "花了二两银子从药铺买了艾草、苍术、白芷等药材，按照老郎中教的方子煎水给全家饮服，又在屋里燃烧苍术艾叶熏了个遍。刺鼻的药味弥漫了好几天，但一家人心里踏实多了。果然入冬后村里有几户人染了风寒，咱家却安然无恙，可见这防疫的银子没有白花。",
                    effect = function()
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
                    cost = {grain = 3},
                    result = "全家闭门居家，大门紧锁，连邻居串门都不让进。足足窝了半个多月，每天就是吃了睡、睡了吃，闷得小孩子直哭。存粮眼看着一天天减少，好在后来消息传来说疫病没有扩散到这边，这才松了口气开了门。虽然安全，但这半月的存粮着实消耗不小。",
                    effect = function()
                        GameData.AddLog("闭门居家半月有余，不敢出门赶集。虽安全了些，但存粮消耗不小。")
                        report.events[#report.events + 1] = "闭门避疫"
                    end
                },
                {
                    text = "不予理会，照常生活",
                    effect = function(self)
                        local luck = math.random(1, 10)
                        if luck <= 2 then
                            local members = GameData.GetAliveMembers()
                            local victim = members[math.random(1, #members)]
                            local victimName = victim.name or "族人"
                            if victim.health then
                                victim.health = math.max(20, victim.health - 25)
                            end
                            GameData.AddLog("疫病果然传到了村里，" .. victimName .. "不幸染病，卧床多日。")
                            report.events[#report.events + 1] = victimName .. "染疫卧病"
                            self.result = "满不在乎地照常出门干活、赶集。谁知没过多久，" .. victimName .. "开始发烧咳嗽，浑身无力，请了郎中来看，说是染了疫气。连着卧床了小半个月，汤药吃了不知多少碗，人瘦了一大圈才慢慢好转。早知道花两银子买药防着，何至于遭这份罪。"
                        else
                            GameData.AddLog("所幸疫病并未传来，虚惊一场。")
                            report.events[#report.events + 1] = "疫病传闻，虚惊一场"
                            self.result = "该干啥干啥，完全没当回事。结果等了一个多月，疫病压根没传过来，不过是隔壁县死了几个人，被传得天花乱坠。省下买药闭门的花销，地里的活也没耽搁，算是赚了。只是以后再有这种传闻，心里多少也会嘀咕一下了。"
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
                    cost = {silver = 2},
                    result = "一大早就赶到镇上种子铺，挑了上等的稻种和麦种。回来后全家老少齐上阵，翻地、施肥、播种，从天蒙蒙亮干到月亮上了梢头。虽然累得腰都直不起来，但看着一畦畦整整齐齐的田地，心里满是期待。果然到了秋天，金灿灿的稻穗比邻家的粗壮一截，收成比往年多了好几成。",
                    effect = function()
                        GameData.AddResource("grain", 12)
                        GameData.AddLog("花银二两购得良种，全家老少齐上阵，日出而作日落而息。辛苦一季，秋来必有好收成。")
                        report.events[#report.events + 1] = "精耕细作，购良种播种"
                    end
                },
                {
                    text = "按部就班耕种",
                    result = "按照往年的老法子，该翻地翻地，该播种播种，不多不少不紧不慢。邻家笑说咱家种地像老牛拉车——慢是慢了点，但稳当。到了秋天一收割，不多不少，跟去年差不离。日子嘛，就是这么不温不火地过着。",
                    effect = function()
                        GameData.AddResource("grain", 8)
                        GameData.AddLog("照往年的老法子耕种，不求有功但求无过。收成马马虎虎。")
                        report.events[#report.events + 1] = "春耕如常"
                    end
                },
                {
                    text = "减少耕种面积，抽人去做别的",
                    result = "留了一半田给老人和媳妇种着，抽出两个壮劳力去镇上码头扛活。虽说地里的收成少了些，但码头上按天给工钱，干了一个多月攒下二两银子。两头算下来也不亏，就是人辛苦了些，两头跑。",
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

-- ============================================================================
-- 捡钱事件（寒门专属，低概率意外之财）
-- ============================================================================
events[#events + 1] = {
    id = "find_money",
    title = "路边拾银",
    rankRange = { 1, 2 },
    weight = 1,
    cooldownMonths = 12,
    check = function() return true end,
    execute = function(report)
        return {
            title = "路边拾银",
            desc = "族中小儿在村口老槐树下玩耍，忽然从土里刨出一个油布包袱，打开一看，竟是一包碎银。约莫有二十两上下，不知是哪家埋的私房钱，年深日久早已无人认领。族长思量再三，决定留下这笔意外之财贴补家用。",
            choices = {
                {
                    text = "收下这笔意外之财",
                    result = "虽说来路不明，但眼下家中正缺银子使。族长让人把银子称了称，整整二十两。小儿乐得手舞足蹈，嚷着要吃糖葫芦。族长笑骂一句，转头却也松了口气——这笔钱来得正是时候。",
                    effect = function()
                        GameData.AddResource("silver", 20)
                        GameData.AddLog("族中小儿在村口老槐树下刨出一包碎银，约有二十两，贴补了家用。")
                        report.events[#report.events + 1] = "路边拾银二十两"
                    end
                },
            }
        }
    end,
}

-- ============================================================================
-- 商人路过事件（寒门专属，资源互换）
-- ============================================================================
events[#events + 1] = {
    id = "passing_merchant",
    title = "商人路过",
    rankRange = { 1, 2 },
    weight = 1,
    cooldownMonths = 6,
    check = function() return true end,
    execute = function(report)
        local s = GameData.state
        return {
            title = "商人路过",
            desc = "一个赶着骡车的行商路过村口，车上满载着粮食、布匹和杂货。他说是从南边来的，因为赶路错过了宿头，想在村里歇一晚。见他货物齐全，不妨趁机做些交易。",
            choices = {
                {
                    text = "卖粮换银（10粮→6银）",
                    cost = { grain = 10 },
                    result = "商人验过粮食成色，连连点头：'北边的粮食就是实在，粒粒饱满。'一手交粮一手交银，六两白银入了口袋。商人还多送了一把南方的竹扇，说是交个朋友。",
                    effect = function()
                        GameData.AddResource("silver", 6)
                        GameData.AddLog("与路过的商人做了笔买卖，卖了十斗粮食，得银六两。")
                        report.events[#report.events + 1] = "商人路过，卖粮得银"
                    end
                },
                {
                    text = "用银买粮（5银→10粮）",
                    cost = { silver = 5 },
                    result = "掏出五两银子，商人从车上搬下几袋粮食，整整十斗。'这批是湖广的新米，煮出来又香又糯。'搬回家里堆满了半间屋子，这下仓里可充实多了。",
                    effect = function()
                        GameData.AddResource("grain", 10)
                        GameData.AddLog("向路过的商人买了十斗湖广新米，花去五两银子。")
                        report.events[#report.events + 1] = "商人路过，买粮囤积"
                    end
                },
                {
                    text = "卖布换银（6布→4银）",
                    cost = { cloth = 6 },
                    result = "拿出家中存的六匹土布，商人摸了摸质地，说织工不错，南边正缺这种结实耐穿的布料。爽快地付了四两银子，还说下回路过再来收。",
                    effect = function()
                        GameData.AddResource("silver", 4)
                        GameData.AddLog("将家中六匹土布卖给了路过的商人，得银四两。")
                        report.events[#report.events + 1] = "商人路过，卖布得银"
                    end
                },
                {
                    text = "用银买布（3银→6布）",
                    cost = { silver = 3 },
                    result = "花三两银子从商人手里挑了六匹好布，有素色的棉布也有带花纹的细布。商人笑道：'这可是苏州出的好料子，外头卖还贵些呢。'回去后一家人摸着新布爱不释手。",
                    effect = function()
                        GameData.AddResource("cloth", 6)
                        GameData.AddLog("向路过的商人买了六匹苏州布料，花去三两银子。")
                        report.events[#report.events + 1] = "商人路过，购入布匹"
                    end
                },
                {
                    text = "不做交易，只留他住一晚",
                    result = "没什么需要买卖的，便只管他一顿饭一晚住处。商人感激不尽，临走时讲了许多外头的见闻——南边的物价、北边的战事、哪条路好走哪个渡口安全。这些消息比什么都值钱。",
                    effect = function()
                        GameData.AddResource("fame", 2)
                        GameData.AddLog("留宿了一位路过的商人，虽未交易，但从他口中得知许多外头的消息。")
                        report.events[#report.events + 1] = "留宿商人，打听见闻"
                    end
                },
            }
        }
    end,
}

return events
