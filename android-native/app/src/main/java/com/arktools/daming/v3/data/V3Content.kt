package com.arktools.daming.v3.data

data class V3StartProfile(
    val silver: Int,
    val grain: Int,
    val influence: Int,
    val cohesion: Int,
    val militia: Int,
    val relations: V3Relations,
    val rebelHeat: Int,
    val routeScores: Map<V3Route, Int>,
    val routeBonuses: List<Pair<V3Route, Int>>,
    val annualGoals: List<V3AnnualGoal>,
    val originTraits: List<String>,
    val countyEffect: String,
    val crisisEffect: String
)

object V3Content {
    val roots = listOf("寒门佃户", "没落士族", "边地军户", "江南商族", "山中堡寨", "海商遗族")
    val counties = listOf("江南水乡", "中原灾地", "西北边堡", "湖广粮仓", "闽粤海路", "辽东边地")
    val creeds = listOf("耕读传家", "重商逐利", "聚族自保", "忠君报国", "明哲保身", "开海远行")
    val crises = listOf("饥荒将至", "流寇逼近", "官府催税", "族产争端", "商路断绝", "瘟疫初起")

    val monthlyCards = listOf(
        V3MonthlyCard(
            id = "clan_ancestral_register",
            pool = V3CardPool.Clan,
            title = "祠堂灯下修谱",
            body = "雨声敲在瓦檐上，族老把一卷旧谱摊在灯下。纸页缺了几行，恰好也缺了几户人的去处。",
            tag = "族务",
            choices = listOf(
                V3CardChoice("repair", "请先生补全", "花银请读书人校谱，先把家世接起来。", effects = V3EffectDelta(silver = -12, influence = 4, cohesion = 5, patriarchPrestige = 2, biographicalNote = "在祠堂灯下补全族谱，族人重新有了共同来处。")),
                V3CardChoice("oral", "召族人共证", "让各房在祖先牌位前说出自己记得的旧事。", effects = V3EffectDelta(cohesion = 8, influence = 2, patriarchConduct = 3, biographicalNote = "召各房共证旧事，族谱添上了纸页之外的人情。"))
            )
        ),
        V3MonthlyCard(
            id = "trade_river_contract",
            pool = V3CardPool.Trade,
            title = "河埠的红契",
            body = "船户带来一纸红契，说上游米船愿把货交给李氏经手。只是河道近来不太平，价钱和风险都写在同一行。",
            tag = "商旅",
            choices = listOf(
                V3CardChoice("sign", "压银接契", "先付定银，把这条粮路握在手里。", require = V3CardRequire(minSilver = 60), effects = V3EffectDelta(silver = -45, grain = 80, merchants = 8, routeDelta = V3RouteDelta(V3Route.Merchant, 5))),
                V3CardChoice("share", "邀商共担", "少赚一些，换一条不至于压垮家底的路。", effects = V3EffectDelta(silver = 15, grain = 35, merchants = 4, cohesion = 2, routeDelta = V3RouteDelta(V3Route.Merchant, 3)))
            )
        ),
        V3MonthlyCard(
            id = "estate_dike_repair",
            pool = V3CardPool.Estate,
            title = "春水漫过田埂",
            body = "一夜暴雨，低田的水已经齐膝。若今日不修堤，秋收便只剩一纸空账。",
            tag = "产业",
            choices = listOf(
                V3CardChoice("hire", "雇短工修堤", "花银买时间，也买一季收成。", require = V3CardRequire(minSilver = 25), effects = V3EffectDelta(silver = -25, grain = 65, patriarchStewardship = 3)),
                V3CardChoice("clan", "让各房出丁", "不花现银，但会考验各房愿不愿意一起扛。", effects = V3EffectDelta(grain = 35, cohesion = -2, militia = 2, patriarchStewardship = 2))
            )
        ),
        V3MonthlyCard(
            id = "field_refugees_arrive",
            pool = V3CardPool.Field,
            title = "篱外来人",
            body = "黄昏时，十几户人站在庄门外。他们没有路引，只有几只空碗和一双不肯倒下的眼睛。",
            tag = "乡野",
            choices = listOf(
                V3CardChoice("settle", "开仓收留", "粮食会少，庄门却会记住今日。", require = V3CardRequire(minGrain = 45), effects = V3EffectDelta(grain = -35, refugees = -12, villagers = 6, influence = 3, cohesion = 3)),
                V3CardChoice("hire", "以工换食", "给他们田埂和水渠的活计，让饭碗凭双手留下。", effects = V3EffectDelta(grain = -18, refugees = -5, patriarchStewardship = 2, villagers = 4)),
                V3CardChoice("refuse", "闭门不纳", "守住现有粮仓，但流言不会因此停下。", effects = V3EffectDelta(unrest = 8, villagers = -8, influence = -2))
            )
        ),
        V3MonthlyCard(
            id = "rumor_old_scholar",
            pool = V3CardPool.Rumor,
            title = "茶棚里的旧话",
            body = "茶棚里有人说，县城新来的幕客正在访查各家宗谱。也有人说，他只是想找一户肯替他担保的人。",
            tag = "风闻",
            choices = listOf(
                V3CardChoice("ask", "顺藤打听", "请熟人把话头引到县城去。", dice = true, successRate = 65, successText = "消息落到了实处。", failureText = "茶棚散了，只留下几句真假难辨的闲话。", successEffects = V3EffectDelta(influence = 5, yamen = 4, biographicalNote = "从茶棚风闻中摸到县城的一条隐线。"), failureEffects = V3EffectDelta(silver = -8, influence = -1)),
                V3CardChoice("ignore", "不沾此事", "族里眼下最要紧的是田庄和粮仓。", effects = V3EffectDelta(patriarchStewardship = 2))
            )
        ),
        V3MonthlyCard(
            id = "crisis_grain_shortage",
            pool = V3CardPool.Crisis,
            title = "粮囤见底",
            body = "账房把最后一袋陈粮倒在案上，米粒滚过木纹。庄门外的脚步声，比往年更密。",
            tag = "危局",
            choices = listOf(
                V3CardChoice("open", "开仓平粜", "先压住人心，再谋下一季。", require = V3CardRequire(minGrain = 1), effects = V3EffectDelta(grain = -20, unrest = -12, refugees = -4, villagers = 8, influence = 3)),
                V3CardChoice("borrow", "向商帮借粮", "以未来的货路换今日的活路。", dice = true, successRate = 58, successText = "商帮愿意押这一注。", failureText = "商帮闭门，借据落了空。", successEffects = V3EffectDelta(grain = 55, merchants = 6, unrest = -5), failureEffects = V3EffectDelta(silver = -12, unrest = 8, merchants = -4)),
                V3CardChoice("guard", "封仓自守", "粮可以少，人心却会先散。", effects = V3EffectDelta(unrest = 10, villagers = -6, garrisonMorale = -4))
            )
        ),
        V3MonthlyCard(
            id = "visitor_xu_guangqi",
            pool = V3CardPool.Visitor,
            title = "西洋钟声入庄",
            body = "一位远道来的学者在驿路歇脚。他谈测量、农具，也谈天下并非只有旧书里写过的模样。",
            tag = "访客",
            choices = listOf(
                V3CardChoice("host", "请入书房", "以一席清谈换一份见识。", require = V3CardRequire(minInfluence = 30), effects = V3EffectDelta(influence = 4, patriarchPrestige = 4, itemId = "western_clock", visitorId = "xu_guangqi", visitorProgress = 1, biographicalNote = "书房接待远客，第一次听见西洋钟声。")),
                V3CardChoice("observe", "请看农具", "不问大道，先问怎样让田里多收一成。", effects = V3EffectDelta(grain = 25, patriarchStewardship = 4, itemId = "new_farming_manual", visitorId = "xu_guangqi", visitorProgress = 1, biographicalNote = "徐光启查看田庄农具，留下改良水利与耕作的办法。"))
            )
        ),
        V3MonthlyCard(
            id = "annual_repair_ledger",
            pool = V3CardPool.Annual,
            title = "岁末清账",
            body = "年关将近，账房、族谱和各房的口供必须对上。数字不会替人遮掩，但能提醒人还剩多少路可走。",
            tag = "年务",
            oncePerGeneration = true,
            choices = listOf(
                V3CardChoice("strict", "逐项核账", "耗些心力，换来明白的家底。", effects = V3EffectDelta(silver = 18, cohesion = 3, patriarchStewardship = 4, biographicalNote = "岁末逐项清账，家业终于不再只凭口说。")),
                V3CardChoice("trust", "交各房自理", "给旁支更多余地，也留下账目模糊处。", effects = V3EffectDelta(cohesion = 5, silver = -8, patriarchConduct = 2))
            )
        )
    )

    val extendedCards = listOf(
        V3MonthlyCard("clan_branch_covenant", V3CardPool.Chain, "旁支归宗", "远房族人带着一纸旧契回到祠堂。他们要的不是银两，而是族谱上一个不再被抹去的位置。", "旧案", weight = 14, choices = listOf(
            V3CardChoice("verify", "验契入谱", "按祖训核验，不让真支流落在外。", dice = true, successRate = 72, successText = "旧契与谱牒相合，远房人跪在祖牌前。", failureText = "契纸破损，争执反而更深。", successEffects = V3EffectDelta(cohesion = 8, influence = 4, visitorId = "branch_covenant", visitorProgress = 1, biographicalNote = "远房归宗，族谱重新接上了一支断线。"), failureEffects = V3EffectDelta(cohesion = -5, unrest = 5)),
            V3CardChoice("buy", "以银息争", "不查旧账，先用银两换眼前安静。", require = V3CardRequire(minSilver = 35), effects = V3EffectDelta(silver = -35, cohesion = 2, visitorId = "branch_covenant", visitorProgress = 1))
        )),
        V3MonthlyCard("relation_yamen_cold", V3CardPool.Chain, "县衙冷帖", "县衙递来一张没有落款的冷帖：往年欠税、今岁估粮，都等着李氏给个说法。", "关系", weight = 15, choices = listOf(
            V3CardChoice("visit", "带账登门", "把账本摊开，先求一个可执行的期限。", require = V3CardRequire(minPatriarchStat = "conduct", minPatriarchStatValue = 35), effects = V3EffectDelta(yamen = 8, silver = -20, patriarchConduct = 4, biographicalNote = "族长带账登门，给家业争来一季喘息。")),
            V3CardChoice("avoid", "托人转圜", "省下登门的体面，也留下官府的不快。", effects = V3EffectDelta(yamen = -5, silver = -8, unrest = 2))
        )),
        V3MonthlyCard("relation_gentry_proud", V3CardPool.Chain, "士绅试席", "县中名门设席，却把李氏安排在屏风之后。席位是一桩小事，也是一家人今后要站在哪里。", "关系", weight = 13, choices = listOf(
            V3CardChoice("accept", "入席听谈", "先听完，再让人知道李氏不是来争一张椅子。", effects = V3EffectDelta(gentry = 8, influence = 4, patriarchConduct = 3)),
            V3CardChoice("leave", "转身离席", "不受轻慢，但从此少一条往来门路。", effects = V3EffectDelta(gentry = -7, cohesion = 3, patriarchPrestige = 3))
        )),
        V3MonthlyCard("relation_villager_anger", V3CardPool.Crisis, "乡民叩门", "佃户把破了口的粮袋带到祠堂。他们说，今秋的租若照旧交，家里就没有明年。", "关系", weight = 18, choices = listOf(
            V3CardChoice("reduce_rent", "减租一成", "少收一季，换来田庄不散。", require = V3CardRequire(minPatriarchStat = "stewardship", minPatriarchStatValue = 35), effects = V3EffectDelta(grain = -15, villagers = 12, cohesion = 5, unrest = -10, patriarchStewardship = 3)),
            V3CardChoice("enforce", "照契收取", "账面不亏，人心却在门外。", effects = V3EffectDelta(silver = 25, villagers = -12, unrest = 12, cohesion = -4))
        )),
        V3MonthlyCard("crisis_refugee_wave", V3CardPool.Crisis, "第二批流民", "渡口传来消息，灾区又有一批人沿河而下。庄门前的空地已经不够搭棚。", "危局", weight = 20, choices = listOf(
            V3CardChoice("open_new_field", "拨荒地安置", "把荒地交给他们，先让人活下来。", require = V3CardRequire(minGrain = 55), effects = V3EffectDelta(grain = -45, refugees = -18, villagers = 10, cohesion = 4, influence = 3, storyFlag = "refugee_settlement", plaqueId = "义门")),
            V3CardChoice("fort_gate", "只收青壮", "让能下地、能守夜的人先入庄。", effects = V3EffectDelta(refugees = -6, militia = 5, villagers = -4, unrest = 7, garrisonMorale = 2))
        )),
        V3MonthlyCard("crisis_mutiny_warning", V3CardPool.Crisis, "乡勇欠饷", "夜巡的火把一支支熄灭。乡勇把旧甲放在案上，说再没有饷银，守庄便只是族老的一句空话。", "危局", weight = 22, choices = listOf(
            V3CardChoice("pay", "先发半饷", "压下怨声，家账再薄一层。", require = V3CardRequire(minSilver = 30), effects = V3EffectDelta(silver = -30, garrisonMorale = 14, unrest = -8, militia = 3, plaqueId = "守望匾")),
            V3CardChoice("council", "让各房共担", "把守庄从主房的事，变成全族的事。", effects = V3EffectDelta(cohesion = 7, garrisonMorale = 5, unrest = -4, silver = -12)),
            V3CardChoice("dismiss", "遣散一半", "眼下省银，庄门从此变薄。", effects = V3EffectDelta(silver = 18, militia = -8, garrisonMorale = -18, unrest = 8))
        )),
        V3MonthlyCard("exam_county_card", V3CardPool.Exam, "县试名额", "县中书吏送来名额，族里读书的孩子终于可以把名字写上考册。", "科举", minChapter = 1, weight = 12, require = V3CardRequire(minChildren = 1), choices = listOf(
            V3CardChoice("prepare", "送子入试", "花束脩，也给族里一个向上攀的念头。", require = V3CardRequire(minSilver = 20, minPatriarchStat = "prestige", minPatriarchStatValue = 25), effects = V3EffectDelta(silver = -20, influence = 3, routeDelta = V3RouteDelta(V3Route.Scholar, 6), storyFlag = "exam_county_ready", biographicalNote = "族中第一次把名字写进县试名册。")),
            V3CardChoice("farm", "先守田庄", "读书不只在考场，家底也不能断。", effects = V3EffectDelta(grain = 20, patriarchStewardship = 2))
        )),
        V3MonthlyCard("exam_provincial_card", V3CardPool.Exam, "乡试秋闱", "族中后辈已过县府两关，乡试的门槛隔着一场秋雨。", "科举", minChapter = 3, weight = 14, require = V3CardRequire(minAliveAdults = 2, flagRequired = "exam_county_ready"), choices = listOf(
            V3CardChoice("sponsor", "倾力供给", "给他盘缠、书卷与一间安静屋子。", require = V3CardRequire(minSilver = 80, minInfluence = 45), dice = true, successRate = 55, successText = "秋闱放榜，族中添了一名举子。", failureText = "名落孙山，盘缠也随秋风去了。", successEffects = V3EffectDelta(silver = -80, influence = 12, gentry = 8, routeDelta = V3RouteDelta(V3Route.Scholar, 12), itemId = "provincial_certificate", plaqueId = "耕读之家", biographicalNote = "族中后辈中举，族谱添上了一行墨色极重的字。"), failureEffects = V3EffectDelta(silver = -80, cohesion = -2, patriarchPrestige = -2)),
            V3CardChoice("share", "各房分担", "不让一房独负，也不把所有希望压在一人身上。", effects = V3EffectDelta(silver = -35, cohesion = 5, routeDelta = V3RouteDelta(V3Route.Scholar, 5)))
        )),
        V3MonthlyCard("visitor_xu_xiake", V3CardPool.Visitor, "徐霞客过庄", "徐霞客自山道来，鞋底沾着远方的泥。他不问族里有多少银，只问哪一条路通向未曾写入舆图的地方。", "访客", minChapter = 2, weight = 16, once = true, choices = listOf(
            V3CardChoice("map", "请他绘路", "用一顿薄酒换一张能留在族谱里的图。", effects = V3EffectDelta(silver = -10, influence = 5, itemId = "travel_map", visitorId = "xu_xiake", visitorProgress = 1, biographicalNote = "徐霞客过庄，留下了一张标着水源与山路的图。")),
            V3CardChoice("guide", "请他入山", "让族人替他引路，也让族人见见山外。", effects = V3EffectDelta(patriarchConduct = 2, bandits = -4, visitorId = "xu_xiake", visitorProgress = 1, routeDelta = V3RouteDelta(V3Route.Hermit, 5)))
        )),
        V3MonthlyCard("visitor_song_yingxing", V3CardPool.Visitor, "宋应星论器", "宋应星带着一册未刊的稿纸，在田埂边谈水车、农具和那些被士人忽略的手艺。", "访客", minChapter = 2, weight = 15, once = true, choices = listOf(
            V3CardChoice("workshop", "请他看作坊", "让书本与工匠在同一张案上说话。", effects = V3EffectDelta(silver = -18, grain = 35, patriarchStewardship = 6, itemId = "heavenly_crafts_draft", visitorId = "song_yingxing", visitorProgress = 1, biographicalNote = "宋应星论器，族中第一次把工匠的名字写进家乘。")),
            V3CardChoice("copy", "请抄一册", "把一份手艺留给后来人。", effects = V3EffectDelta(silver = -12, influence = 4, itemId = "agriculture_compendium", visitorId = "song_yingxing", visitorProgress = 1))
        )),
        V3MonthlyCard("visitor_zhang_dai", V3CardPool.Visitor, "张岱访灯", "张岱从江南来，谈园林、旧梦和一个家族怎样在乱世里保住审美与体面。", "访客", minChapter = 3, weight = 12, once = true, choices = listOf(
            V3CardChoice("record", "请他记族事", "把今日的灯影，留给后世读。", effects = V3EffectDelta(influence = 6, patriarchPrestige = 5, itemId = "dream_record", visitorId = "zhang_dai", visitorProgress = 1, biographicalNote = "张岱访灯，族中第一次认真记录日常的光影。")),
            V3CardChoice("quiet", "只饮一盏", "不求文章，求这一夜不被兵乱打扰。", effects = V3EffectDelta(cohesion = 4, visitorId = "zhang_dai", visitorProgress = 1))
        )),
        V3MonthlyCard("visitor_sun_chuanting", V3CardPool.Visitor, "孙传庭借粮", "孙传庭经过县境，军书上的数字比庄里的粮囤更冷。他没有空话，只问能否借出一批粮。", "访客", minChapter = 4, weight = 17, once = true, choices = listOf(
            V3CardChoice("lend", "借粮济急", "军粮出仓，家族也与一场大势结了缘。", require = V3CardRequire(minGrain = 100), effects = V3EffectDelta(grain = -100, garrison = 12, influence = 8, routeDelta = V3RouteDelta(V3Route.Loyalist, 8), visitorId = "sun_chuanting", visitorProgress = 1, biographicalNote = "孙传庭借粮，族中第一次感到天下兵事压到了自家粮仓。")),
            V3CardChoice("refuse", "留粮守庄", "先守住族人，日后再谈更大的事。", effects = V3EffectDelta(cohesion = 4, garrison = -5, visitorId = "sun_chuanting", visitorProgress = 1))
        )),
        V3MonthlyCard("capstone_family_school", V3CardPool.Clan, "族学成规", "族学终于有了固定的先生、束脩与课表。有人提议把这件事刻在匾上，告诉后人这家人靠什么站稳。", "匾额", minChapter = 2, once = true, require = V3CardRequire(minChildren = 1), choices = listOf(
            V3CardChoice("hang", "悬耕读匾", "从今日起，读书不再只是某一房的私事。", require = V3CardRequire(minInfluence = 50, minCohesion = 55), effects = V3EffectDelta(plaqueId = "耕读之家", influence = 8, cohesion = 6, patriarchPrestige = 5, storyFlag = "plaque_study")),
            V3CardChoice("fund", "先办族学", "匾额可以晚些，孩子不能再晚一年。", effects = V3EffectDelta(silver = -35, influence = 5, routeDelta = V3RouteDelta(V3Route.Scholar, 8)))
        )),
        V3MonthlyCard("capstone_charity_gate", V3CardPool.Clan, "义门初成", "灾年里，祠门没有关。乡民说起李氏，先说那口粥锅，再说家里有几间铺面。", "匾额", minChapter = 3, once = true, choices = listOf(
            V3CardChoice("name", "立义门匾", "把这一季的取舍写进家声。", require = V3CardRequire(minInfluence = 70, minRelationVillagers = 45), effects = V3EffectDelta(plaqueId = "义门", influence = 10, villagers = 8, cohesion = 5, storyFlag = "plaque_charity")),
            V3CardChoice("anonymous", "不留名字", "救人本不该先问谁的名号。", effects = V3EffectDelta(villagers = 12, cohesion = 4))
        ))
    )

    private val coreVisitors = listOf(
        V3Visitor("xu_xiake", "徐霞客", "行脚地理家", chapters = listOf(
            V3VisitorChapter(2, "山路初见", "他从山道来，问水源、问桥，也问族人为何只守着一块熟地。", "travel_map"),
            V3VisitorChapter(4, "再寄山图", "远行书信寄回一张新图，标出乱世中的退路。", "mountain_route")
        )),
        V3Visitor("song_yingxing", "宋应星", "格物学者", chapters = listOf(
            V3VisitorChapter(2, "田边论器", "农具不是粗人的玩意，正是让一家人不饿肚子的学问。", "agriculture_compendium"),
            V3VisitorChapter(5, "工坊留稿", "他把未刊稿纸交给族中工匠，嘱咐不要让手艺只活在纸上。", "heavenly_crafts_draft")
        )),
        V3Visitor("zhang_dai", "张岱", "江南文客", chapters = listOf(
            V3VisitorChapter(3, "一盏灯", "园林会败，文章会散，唯有记下的人知道曾经有过。", "dream_record"),
            V3VisitorChapter(6, "旧梦成谱", "他将一段族中旧事写进自己的游记，族名从此不只存在于本地人的口中。", "family_chronicle")
        )),
        V3Visitor("sun_chuanting", "孙传庭", "关中将领", chapters = listOf(
            V3VisitorChapter(4, "借粮", "军书上的数字落进粮仓，家族第一次被迫回答天下之事。", null),
            V3VisitorChapter(5, "回信", "他在战事间隙回信，谢的不是粮，而是没有趁乱抬价。", "military_letter")
        ))
    )

    private val additionalVisitors = listOf(
        V3Visitor("xu_guangqi", "徐光启", "农政学者", "scholar", listOf(
            V3VisitorChapter(2, "田亩新法", "他不以空谈入庄，先看水渠、种粮与一亩田能养几口人。", "new_farming_manual"),
            V3VisitorChapter(4, "农政留卷", "他把一卷农政旧稿托给族学，嘱咐后人先让乡里吃饱，再谈文章。", "agriculture_compendium")
        )),
        V3Visitor("tang_ruowang", "汤若望", "历法客卿", "scholar", listOf(
            V3VisitorChapter(3, "钟声与历", "远客带来一架小钟，也带来一套与旧历不同的算法。", "western_clock"),
            V3VisitorChapter(5, "推算收成", "他替族里校过节气，提醒庄户不要把一场迟雨当成天意。", "dream_record")
        )),
        V3Visitor("li_zhizao", "李之藻", "舆图学人", "scholar", listOf(
            V3VisitorChapter(2, "舆图摊案", "他把河道、驿路和粮仓画在同一张纸上，家族第一次看见县外的脉络。", "travel_map"),
            V3VisitorChapter(4, "水路校勘", "一张新图标出险滩与渡口，商路因此少走了几段弯路。", "mountain_route")
        )),
        V3Visitor("gu_yanwu", "顾炎武", "经世学者", "scholar", listOf(
            V3VisitorChapter(4, "天下在事", "他不问空名，只问一县的粮、路、户籍是否真的有人料理。", "family_chronicle"),
            V3VisitorChapter(6, "日知旧闻", "临别时，他把地方掌故写入札记，提醒族长家声必须落在实事上。", "dream_record")
        )),
        V3Visitor("wang_fuzhi", "王夫之", "衡岳遗民", "scholar", listOf(
            V3VisitorChapter(4, "衡岳来客", "他在山馆谈兵事与家国，却先问族中是否还有可以安顿老幼的粮仓。", "dream_record"),
            V3VisitorChapter(6, "船山遗稿", "一卷手稿留在族学，后辈从字里行间读到乱世仍须自立的骨气。", "family_chronicle")
        )),
        V3Visitor("huang_zongxi", "黄宗羲", "东浙学者", "scholar", listOf(
            V3VisitorChapter(3, "学校议", "他谈地方学校，不把读书只看成一房一姓的门面。", "family_chronicle"),
            V3VisitorChapter(5, "明夷待访", "他留下几页论学札记，族中从此把族学与乡里公议放在一起。", "dream_record")
        )),
        V3Visitor("fang_yizhi", "方以智", "博物学人", "scholar", listOf(
            V3VisitorChapter(3, "物理初谈", "他在作坊观察火候、木性与水势，工匠第一次被请到同一张席上。", "heavenly_crafts_draft"),
            V3VisitorChapter(5, "药炉问答", "他以杂学相赠，提醒族人灾年最贵的不是金银，是能救人的手艺。", "new_farming_manual")
        )),
        V3Visitor("chen_zilong", "陈子龙", "松江文士", "scholar", listOf(
            V3VisitorChapter(3, "松江过庄", "他携一卷诗文来访，席间谈的是江南水患和百姓的活计。", "dream_record"),
            V3VisitorChapter(5, "危城寄书", "局势渐乱，他寄来一封短札，劝族里莫把文章与粮兵分开。", "military_letter")
        )),
        V3Visitor("zhang_pu", "张溥", "复社文士", "scholar", listOf(
            V3VisitorChapter(2, "七录入门", "他以抄书相勉，族中孩子第一次知道读书也可以成为共同的约定。", "family_chronicle"),
            V3VisitorChapter(4, "乡评成册", "一册地方人物录留在族学，房支争端多了一份可以核对的公议。", "dream_record")
        )),
        V3Visitor("fu_shan", "傅山", "太原遗民", "scholar", listOf(
            V3VisitorChapter(4, "药炉边见", "他以医与字相赠，要求族里先救病人，再论谁的门第更高。", "new_farming_manual"),
            V3VisitorChapter(6, "霜红札记", "临行所留的一纸字帖挂在族学，提醒后人守住自己的笔与骨。", "family_chronicle")
        )),
        V3Visitor("mao_bijiang", "冒辟疆", "水绘文客", "scholar", listOf(
            V3VisitorChapter(3, "水绘来客", "他谈园林与旧游，也认真记下庄中妇人如何分粥理账。", "dream_record"),
            V3VisitorChapter(5, "金陵旧闻", "一段旧都见闻传入族谱，使地方家族也有了天下风云的回声。", "family_chronicle")
        )),
        V3Visitor("liu_rushi", "柳如是", "江南才女", "scholar", listOf(
            V3VisitorChapter(3, "河桥夜话", "她谈诗，也谈一个乱世女子如何替家中守住账本、书信与尊严。", "dream_record"),
            V3VisitorChapter(5, "尺牍留香", "她留下几封整理过的家书，族中内宅第一次有了自己的文书档案。", "family_chronicle")
        )),
        V3Visitor("li_dingguo", "李定国", "西南将领", "martial", listOf(
            V3VisitorChapter(5, "军路借道", "兵马经过庄外，他只求一段不扰民的粮道和一份明确的军约。", "military_letter"),
            V3VisitorChapter(6, "守约回报", "他以一封军书回谢族中不抬粮价，守庄战前多了一条可相信的消息。", "mountain_route")
        )),
        V3Visitor("zheng_chenggong", "郑成功", "海路统领", "martial", listOf(
            V3VisitorChapter(4, "海门问舟", "海商带来风向和船图，问李氏愿不愿为远行留一条退路。", "travel_map"),
            V3VisitorChapter(6, "潮汐军书", "一封海上军书抵达码头，海路与族中船队从此多了一分胆气。", "military_letter")
        )),
        V3Visitor("yuan_chonghuan", "袁崇焕", "边镇将领", "martial", listOf(
            V3VisitorChapter(4, "边堡求粮", "边镇来使只带一张军需清单，族长必须在家底与大势间落笔。", "military_letter"),
            V3VisitorChapter(5, "城守札记", "他把守城与守庄的道理写在一张纸上，乡勇从此有了分班轮值的章法。", "mountain_route")
        )),
        V3Visitor("sun_chuanting_scholar", "孙传庭", "儒将经略", "martial", listOf(
            V3VisitorChapter(3, "边墙论守", "他看过庄墙，指出真正的防线不只是一道土墙，还包括粮路与人心。", "mountain_route"),
            V3VisitorChapter(5, "堡寨成法", "一份边堡布置图留在族中，六门守庄从此有了旧例可依。", "military_letter")
        )),
        V3Visitor("liu_zhiji", "刘之骥", "乡约长者", "elder", listOf(
            V3VisitorChapter(1, "乡约初立", "他劝各房把出丁、分粮和婚丧写清楚，免得好心靠临时争吵。", "family_chronicle"),
            V3VisitorChapter(3, "乡老再议", "灾年重订乡约，乡民关系因一纸可执行的约定而稳住。", "agriculture_compendium")
        )),
        V3Visitor("chen_hongshou", "陈洪绶", "新安画客", "artist", listOf(
            V3VisitorChapter(3, "新安过笔", "他为族中画了一幅庄门图，画里没有华屋，只有各房正在修堤。", "dream_record"),
            V3VisitorChapter(5, "家乘留形", "一卷画稿让后人看见乱世中的普通人，也看见家族为何没有散。", "family_chronicle")
        )),
        V3Visitor("wu_weishan", "吴伟业", "江南诗人", "scholar", listOf(
            V3VisitorChapter(3, "梅村听雨", "他从旧都来，写下驿路风雨，也记下庄门一锅分给众人的粥。", "dream_record"),
            V3VisitorChapter(5, "诗入家乘", "一首长诗被抄入族谱，家族的沉浮第一次有了完整的韵脚。", "family_chronicle")
        )),
        V3Visitor("li_yu", "李渔", "戏曲文客", "artist", listOf(
            V3VisitorChapter(4, "闲情入庄", "他教族中妇人修补戏台，也教族长知道体面不是挥霍。", "dream_record"),
            V3VisitorChapter(6, "一台家戏", "族人在祠堂前演了一出家史，孩子们终于听懂祖辈为何守住这块地。", "family_chronicle")
        )),
        V3Visitor("shuai_fan", "帅范", "河工老吏", "official", listOf(
            V3VisitorChapter(2, "河工查堤", "他沿着水渠敲堤脚，指出一处看不见的渗漏。", "new_farming_manual"),
            V3VisitorChapter(4, "水册留庄", "河工册记下了闸口与粮仓的位置，来年水患少了一场。", "travel_map")
        )),
        V3Visitor("pan_jixun", "潘季驯", "治河名臣", "official", listOf(
            V3VisitorChapter(2, "堤上问粮", "他谈治河不只谈泥沙，还问修堤之后庄里谁来种田。", "new_farming_manual"),
            V3VisitorChapter(4, "束水成约", "一份旧河工法被留下，田庄的经营与水患应对从此连在一起。", "agriculture_compendium")
        )),
        V3Visitor("hai_rui", "海瑞", "清直长者", "official", listOf(
            V3VisitorChapter(2, "过县问租", "他不先看门第，只问佃户的租契是否让人活得下去。", "family_chronicle"),
            V3VisitorChapter(4, "清丈留名", "他留下的丈量规矩让族产清点少了几分房支争执。", "travel_map")
        )),
        V3Visitor("zhang_juzheng", "张居正", "持衡相臣", "official", listOf(
            V3VisitorChapter(1, "考成旧法", "一位旧学官谈起考成法，族长第一次把每一处家产都列入月账。", "agriculture_compendium"),
            V3VisitorChapter(3, "一条鞭影", "赋役与田亩被放在一张表上，家族终于看清银粮如何流失。", "family_chronicle")
        )),
        V3Visitor("shen_shixing", "申时行", "江南名臣", "official", listOf(
            V3VisitorChapter(2, "中和过席", "他谈处世不以逢迎为先，而以让一县的人还能坐下来为先。", "dream_record"),
            V3VisitorChapter(4, "乡绅成约", "一纸士绅乡约让县衙、族里与乡民各退一步。", "family_chronicle")
        )),
        V3Visitor("chen_yuanyuan", "陈圆圆", "秦淮旧人", "artist", listOf(
            V3VisitorChapter(4, "乱世避席", "她从兵乱中来，带来的不是传闻，而是一群需要安置的妇孺。", "family_chronicle"),
            V3VisitorChapter(6, "旧曲新谱", "族中为流离者留下一处屋舍，旧曲被写成提醒后人的家训。", "dream_record")
        )),
        V3Visitor("li_xiangjun", "李香君", "秦淮才女", "artist", listOf(
            V3VisitorChapter(4, "桃花扇影", "她不求族长替谁争名，只求把一封未寄出的家书送到安全处。", "family_chronicle"),
            V3VisitorChapter(6, "扇面留痕", "扇面上的题字被收入家乘，记录乱世中仍有人守信。", "dream_record")
        )),
        V3Visitor("xu_fudong", "徐枋", "吴中遗民", "scholar", listOf(
            V3VisitorChapter(4, "吴中避兵", "他谈避祸不是逃掉责任，而是先保存能继续做事的人。", "mountain_route"),
            V3VisitorChapter(6, "白云旧约", "一纸避居约定让族中老幼有了真正的退路。", "family_chronicle")
        )),
        V3Visitor("qian_qianyi", "钱谦益", "东林名士", "scholar", listOf(
            V3VisitorChapter(3, "虞山议学", "他带来士林消息，也提醒族长声名若没有粮田支撑，终究只是纸上风。", "dream_record"),
            V3VisitorChapter(5, "文契相交", "一纸文契把族学与地方书院联结起来，科举路线多了一层助力。", "family_chronicle")
        )),
        V3Visitor("li_shi", "李时珍", "本草医家", "healer", listOf(
            V3VisitorChapter(2, "药圃问诊", "他教族人把荒地分出一角种药，灾年少一分求医的路。", "new_farming_manual"),
            V3VisitorChapter(4, "本草留方", "一册验方留在医馆，乡民关系与族中身板都因此更稳。", "dream_record")
        ))
    )

    val visitors: List<V3Visitor> = coreVisitors + additionalVisitors

    val items = listOf(
        V3Item("western_clock", "西洋自鸣钟", "relic", "每次接待访客时，额外获得1点族望。", V3EffectDelta(influence = 1)),
        V3Item("new_farming_manual", "新式农具图", "book", "每月田庄结算额外获得5石粮。", V3EffectDelta(grain = 5), recurring = true),
        V3Item("provincial_certificate", "乡试中举文书", "deed", "提升士绅关系与耕读声名。", V3EffectDelta(gentry = 4, influence = 4)),
        V3Item("travel_map", "山河行记图", "book", "山道风险降低，隐世路线更稳。", V3EffectDelta(routeDelta = V3RouteDelta(V3Route.Hermit, 3))),
        V3Item("agriculture_compendium", "农政全书抄本", "book", "经营能力提升，粮食更稳定。", V3EffectDelta(patriarchStewardship = 3)),
        V3Item("heavenly_crafts_draft", "天工开物稿", "book", "作坊与产业收益提升。", V3EffectDelta(patriarchStewardship = 4)),
        V3Item("dream_record", "陶庵梦忆抄", "book", "履历与族望更易被后人记住。", V3EffectDelta(patriarchPrestige = 5)),
        V3Item("family_chronicle", "江南旧梦录", "relic", "解锁一条额外族谱履历。", V3EffectDelta(influence = 3)),
        V3Item("military_letter", "军镇回信", "deed", "守庄战前获得额外守望士气。", V3EffectDelta(garrisonMorale = 8)),
        V3Item("mountain_route", "山中退路图", "map", "危局时减少流民与怨气增长。", V3EffectDelta(unrest = -3), recurring = true)
    )

    val plaques = mapOf(
        "耕读之家" to "族学与科举让一家人的名字从乡里走进书页。",
        "义门" to "灾年不闭祠门，家声先从一锅粥开始。",
        "守望匾" to "六处庄门有人守，夜里的火把没有全部熄灭。",
        "望族" to "各房各业终于被同一卷族谱系在一起。",
        "郡望" to "家名跨过县界，成为地方不能绕开的姓氏。"
    )

    val allMonthlyCards: List<V3MonthlyCard> get() = completeMonthlyCards

    private fun generatedCard(
        id: String,
        pool: V3CardPool,
        title: String,
        body: String,
        tag: String,
        effects: V3EffectDelta,
        counterEffects: V3EffectDelta,
        minChapter: Int = 1,
        crisisLevel: Int = 0,
        require: V3CardRequire? = null,
        actLabel: String = "按议定办理",
        actDesc: String = "不把眼前的难处推给下一个月。",
        waitLabel: String = "暂缓处置",
        waitDesc: String = "省下眼前的力气，但风声会继续积在门外。",
        actSuccessText: String = "事遂人意。",
        waitSuccessText: String = "事遂人意。",
        actNote: String? = null,
        waitNote: String? = null
    ): V3MonthlyCard = V3MonthlyCard(
        id = id,
        pool = pool,
        title = title,
        body = body,
        tag = tag,
        weight = 8,
        minChapter = minChapter,
        crisisLevel = crisisLevel,
        require = require,
        choices = listOf(
            V3CardChoice(
                "act", actLabel, actDesc,
                effects = if (actNote != null) effects.copy(biographicalNote = actNote) else effects,
                successText = actSuccessText
            ),
            V3CardChoice(
                "wait", waitLabel, waitDesc,
                effects = if (waitNote != null) counterEffects.copy(biographicalNote = waitNote) else counterEffects,
                successText = waitSuccessText
            )
        )
    )

    val additionalMonthlyCards = listOf(
        // ── 族务 clan ──
        generatedCard("clan_01", V3CardPool.Clan, "祠门换瓦",
            "春雨将至，祠堂的旧瓦一片片松动。族老说，瓦漏则祖宗牌位受湿，湿则家运不宁。",
            "族务", V3EffectDelta(silver = -12, cohesion = 3, patriarchStewardship = 1), V3EffectDelta(cohesion = -2),
            actLabel = "拨银换瓦",
            actDesc = "趁春雨前修好祠堂瓦面，免得祖宗牌位受潮。",
            waitLabel = "暂用草席遮盖",
            waitDesc = "先挡住这一阵雨，秋后再凑银大修。",
            actSuccessText = "新瓦上梁，族老说这是开族以来最齐整的一次春祭。",
            waitSuccessText = "草席挡了春雨，却挡不住族老的连声叹息。",
            actNote = "拨银修缮祠堂瓦面，族人见族长重祖敬宗，凝聚更坚。",
            waitNote = "祠堂漏雨，族人暗觉族长轻慢祖业。"),
        generatedCard("clan_02", V3CardPool.Clan, "族规重抄",
            "旧族规被油烟熏黑，几处条文已不可辨。族老请人重新誊写，说规矩模糊则人心散漫。",
            "族务", V3EffectDelta(silver = -8, influence = 3, cohesion = 2), V3EffectDelta(influence = -1),
            actLabel = "请先生誊抄",
            actDesc = "请县学的读书人来抄录族规，顺便校订错漏。",
            waitLabel = "照旧例口传",
            waitDesc = "族规由族老口耳相传，不必破费刻写。",
            actSuccessText = "新族规张挂祠壁，各房都来看了，无人敢说不字。",
            waitSuccessText = "口传族规各房理解不一，争执渐起。",
            actNote = "请人誊抄族规并张挂祠堂，县中学人闻之亦称赞李氏治家有方。",
            waitNote = "族规口传各执一词，外间传言李氏家规已废。"),
        generatedCard("clan_03", V3CardPool.Clan, "房支分席",
            "清明祭后，各房为入席先后争了半日。主房说辈分在上，二房说人丁兴旺，三房说田粮最多。",
            "族务", V3EffectDelta(cohesion = 5, influence = 2), V3EffectDelta(cohesion = -4, unrest = 2),
            actLabel = "按辈分定席",
            actDesc = "以族谱辈分为准，祖制不可乱，长幼有序。",
            waitLabel = "暂不分席",
            waitDesc = "撤去席位，族人各自站着饮酒，避开纷争。",
            actSuccessText = "族长在祠堂当众断席，各房虽有微词却不敢再争。",
            waitSuccessText = "清明祭草草收场，各房憋着火气散了。",
            actNote = "清明祭按辈分定席，族人服族长公断，族内秩序分明。",
            waitNote = "席次之争不了了之，各房嫌隙加深。"),
        generatedCard("clan_04", V3CardPool.Clan, "族老做寿",
            "老族老过七旬，几房都来问该备什么礼。七十大寿在乡间是大事，办得好与坏关乎族中脸面。",
            "内宅", V3EffectDelta(silver = -10, cohesion = 4, patriarchPrestige = 2), V3EffectDelta(cohesion = -2),
            actLabel = "合族贺寿",
            actDesc = "各房凑银办寿宴，请戏班来唱三日，给族老做足脸面。",
            waitLabel = "家宴小酌",
            waitDesc = "只在主房设一桌家宴，不事铺张，省下银两。",
            actSuccessText = "寿宴三日，乡邻都来道贺，族老笑得合不拢嘴。",
            waitSuccessText = "族老嘴上说节俭好，眼里却掩不住失落。",
            actNote = "为族老办七十大寿，乡里皆赞李氏孝风醇厚。",
            waitNote = "寿宴草草，族老心中不快，其他房支也觉得族长悭吝。"),
        generatedCard("clan_05", V3CardPool.Clan, "义学借屋",
            "村口孩子想识字，族学却还没有足够的桌案。塾师说，多出十几个孩子，地上坐不下了。",
            "族务", V3EffectDelta(silver = -16, influence = 4, villagers = 4), V3EffectDelta(villagers = -3),
            actLabel = "添桌扩屋",
            actDesc = "腾出祠堂侧屋，添十几套桌案，让村中孩子都能进来。",
            waitLabel = "只收族中子弟",
            waitDesc = "族学本为李氏子弟而设，外姓孩子另寻出路。",
            actSuccessText = "义学扩了屋，村人送子入学时都说李氏积德。",
            waitSuccessText = "外村孩子被拒之门外，村人摇头叹气。",
            actNote = "扩义学收村中子弟，乡民感戴，李氏声望在乡中渐起。",
            waitNote = "拒收外姓子弟入义学，乡人暗怨李氏门户之见过重。"),
        generatedCard("clan_06", V3CardPool.Clan, "收养孤侄",
            "族谱外有个失怙的孩子，抱着旧布包站在门槛前。族老说他身上有李家血脉，只是父辈那一支断了。",
            "内宅", V3EffectDelta(grain = -12, cohesion = 5, villagers = 2), V3EffectDelta(cohesion = -3, unrest = 2),
            actLabel = "收入族谱",
            actDesc = "验过血脉，收入族谱，分一份口粮，养在族学里。",
            waitLabel = "给些米粮送走",
            waitDesc = "给一袋米、几两碎银，让他去别处投亲。",
            actSuccessText = "孩子在祠堂磕了三个头，从此族谱上又多了一个名字。",
            waitSuccessText = "孩子抱着米袋走了，族老看你的眼神冷了几分。",
            actNote = "收养族中遗孤入谱，族人皆服族长仁厚。",
            waitNote = "驱逐族中遗孤，族老心寒，族人议论族长绝情。"),
        generatedCard("clan_07", V3CardPool.Clan, "春祭缺牲",
            "牲口涨价，祭期却不能往后推。周礼春祭必用太牢少牢，简省了便是不敬。",
            "族务", V3EffectDelta(silver = -8, influence = 2), V3EffectDelta(influence = -2, cohesion = -1),
            actLabel = "溢价购牲",
            actDesc = "花高价从外县买来猪羊，春祭不可简省。",
            waitLabel = "减等用牲",
            waitDesc = "改用鸡鸭代猪羊，心意到了祖宗也不会怪罪。",
            actSuccessText = "三牲齐备，春祭如仪，乡绅见了都说李氏知礼。",
            waitSuccessText = "春祭用了鸡鸭，族人议论纷纷，族老脸色铁青。",
            actNote = "溢价购牲行春祭，乡绅称李氏知礼守制。",
            waitNote = "减等用牲，族人暗觉族长怠慢祖宗。"),
        generatedCard("clan_08", V3CardPool.Clan, "旁房借谱",
            "旁房来借旧谱，声称要为远亲补名。他们说有一支迁至邻县，如今后人想认祖归宗。",
            "族务", V3EffectDelta(cohesion = 4, influence = 2), V3EffectDelta(cohesion = -3),
            actLabel = "借谱并派人同往",
            actDesc = "借出族谱，派一位族中长辈同去核验，防止冒认。",
            waitLabel = "婉拒不借",
            waitDesc = "族谱不可轻出祠堂，让他们抄录支系后送来核验即可。",
            actSuccessText = "族老亲往核验，三支远亲确凿归宗，族谱添了新页。",
            waitSuccessText = "旁房悻悻而归，说族长信不过自家人。",
            actNote = "借谱核验远亲归宗，宗支扩大，族中更添人丁。",
            waitNote = "拒借族谱，旁房觉族长疑心太重，心生隔阂。"),
        generatedCard("clan_09", V3CardPool.Clan, "婚书复核",
            "媒人拿来两家婚书，请族长落印。男方是邻县小姓，聘礼丰厚，女儿嫁过去不算委屈。",
            "内宅", V3EffectDelta(cohesion = 3, influence = 2), V3EffectDelta(cohesion = -2),
            actLabel = "查核后落印",
            actDesc = "先派人打听男方家风，再盖族印，不耽误女儿终身。",
            waitLabel = "推脱不决",
            waitDesc = "婚事先压一压，等多打听几家再说。",
            actSuccessText = "男家风评尚好，族长落印，婚事如期定下。",
            waitSuccessText = "媒人等了半月无回音，另找了别家，女儿哭了一夜。",
            actNote = "核查男家风评后落印许婚，婚事妥帖，族人称善。",
            waitNote = "拖延婚事致媒人另择他家，女儿心生怨怼。"),
        generatedCard("clan_10", V3CardPool.Clan, "族产清点",
            "库房钥匙交到案上，账本却少了两页。管仓的老头支支吾吾，说那两页是被虫蛀了。",
            "族务", V3EffectDelta(silver = 20, cohesion = 2, patriarchStewardship = 2), V3EffectDelta(silver = -12, unrest = 4),
            actLabel = "逐项盘库",
            actDesc = "亲自下库盘点，粮银器物逐项核对，查出亏空一律追赔。",
            waitLabel = "交账房自查",
            waitDesc = "让账房重新理账，不必大动干戈伤了和气。",
            actSuccessText = "盘库查出银二十两被挪用，管仓的退了银，族产一清二楚。",
            waitSuccessText = "账房报上来说并无亏空，但你总觉得哪里不对。",
            actNote = "亲自盘库追回亏空银两，族产账目从此清明。",
            waitNote = "盘库不严，族产仍有糊涂账，族中暗生猜疑。"),
        // ── 商旅 trade ──
        generatedCard("trade_01", V3CardPool.Trade, "米价翻红",
            "河埠米价一夜翻涨，船户都在等下一阵风。有人说北边歉收，有人说大户在囤。",
            "商旅", V3EffectDelta(silver = 28, grain = -12, merchants = 4), V3EffectDelta(silver = -8, grain = 8),
            actLabel = "高价出货",
            actDesc = "趁价高抛售存粮，赚一笔现银，秋后再补仓。",
            waitLabel = "持粮观望",
            waitDesc = "等米价回落再售，粮在仓里不亏，银根紧些也无妨。",
            actSuccessText = "存粮高价脱手，赚了银二十八两，船户说你看得准。",
            waitSuccessText = "米价涨了几日便回落，你趁低补进粮十二石，只花了八两银。",
            actNote = "趁米价高涨出货，商帮关系加深，现银入袋。",
            waitNote = "不追涨杀跌，持粮度涨，趁低补入仓粮十二石。"),
        generatedCard("trade_02", V3CardPool.Trade, "盐船靠岸",
            "盐船在雾里靠岸，船主只肯与有担保的人做买卖。盐引是官家的东西，私盐做了要掉脑袋。",
            "商旅", V3EffectDelta(silver = 24, merchants = 6, yamen = -2), V3EffectDelta(merchants = -4),
            actLabel = "出面担保",
            actDesc = "以族名担保接下这批盐货，利润厚，但要担干系。",
            waitLabel = "不沾盐货",
            waitDesc = "盐是官营买卖，沾了容易惹上官府，推脱为上。",
            actSuccessText = "盐货顺利出手，赚了银二十四两，只是县衙那边你得去打点。",
            waitSuccessText = "盐船找了别家担保，你错过了这笔利，商帮脸色也不好看。",
            actNote = "担保盐船贸易获利丰厚，但官府那边暗生芥蒂。",
            waitNote = "拒接盐船买卖，商帮觉你胆子太小，不愿再引大买卖。"),
        generatedCard("trade_03", V3CardPool.Trade, "布行邀约",
            "布行掌柜邀你入股，账面漂亮，风险也漂亮。他说苏松棉布运到闽浙能翻一倍利。",
            "商旅", V3EffectDelta(silver = -35, merchants = 8, routeDelta = V3RouteDelta(V3Route.Merchant, 5)), V3EffectDelta(merchants = -3),
            actLabel = "入股布行",
            actDesc = "投银三十五两入股，苏松棉布这条线值得一试。",
            waitLabel = "只做代销",
            waitDesc = "不入股，只帮布行代销本地，赚几个佣金即可。",
            actSuccessText = "布行首季分红到手，商船往来间你也摸清了棉布的门道。",
            waitSuccessText = "代销赚了几两碎银，但布行掌柜下次不再找你。",
            actNote = "入股布行打开苏松商路，商帮网络进一步扩大。",
            waitNote = "仅做代销错失入股良机，商帮渐生疏离。"),
        generatedCard("trade_04", V3CardPool.Trade, "商队缺脚",
            "往北的商队少了脚夫，货物压在仓里。账房说，再不走就要错过秋市。",
            "商旅", V3EffectDelta(silver = 18, grain = -4, merchants = 3), V3EffectDelta(silver = -6),
            actLabel = "派族中丁壮",
            actDesc = "从族中抽几个年轻丁壮临时充脚夫，粮路上不等人。",
            waitLabel = "高价雇人",
            waitDesc = "花高价从外乡雇脚夫，不动族中人。",
            actSuccessText = "族中丁壮随商队北上，秋市赶上了，商帮连连道谢。",
            waitSuccessText = "雇脚夫花了六两银，利润薄了大半。",
            actNote = "派族中丁壮助商队赶秋市，商帮感戴，商路更加通畅。",
            waitNote = "高价雇脚夫压缩利润，商队运货得不偿失。"),
        generatedCard("trade_05", V3CardPool.Trade, "码头抽税",
            "码头新换了管事，过一船便要多一笔钱。商船怨声载道，说这是明抢。",
            "商旅", V3EffectDelta(silver = -18, yamen = 4, merchants = 3), V3EffectDelta(silver = -10, merchants = -3),
            actLabel = "打点新管事",
            actDesc = "送银子去拜见新管事，规矩是人定的，交个朋友路好走。",
            waitLabel = "联合船户抗税",
            waitDesc = "联合各船户拒交额外税银，逼管事收回成命。",
            actSuccessText = "新管事收了礼，以后李氏商船过码头一律减收。",
            waitSuccessText = "抗税惹恼了管事，你的船被扣了三日，货物折损不少。",
            actNote = "打点码头新管事，李氏商船从此过码头畅通无阻。",
            waitNote = "联合抗税反被码头刁难，商船被扣，商路受损。"),
        generatedCard("trade_06", V3CardPool.Trade, "外地客商",
            "外地客商想看你家的作坊和账本。他说愿出高价订货，却要先看家底虚实。",
            "商旅", V3EffectDelta(silver = 15, patriarchStewardship = 2, merchants = 5), V3EffectDelta(merchants = -2),
            actLabel = "亮账迎客",
            actDesc = "敞开作坊让客商看，有底气才敢让人看账本。",
            waitLabel = "只看样品",
            waitDesc = "作坊和账本不可轻示于人，只给看成品样品。",
            actSuccessText = "客商看了作坊和账本，当场下了大单，还介绍了两家长期客户。",
            waitSuccessText = "客商觉得你不信任他，丢下样品便走了。",
            actNote = "以诚待客亮账迎客，外地客商成了长期主顾。",
            waitNote = "拒客于门外只看样品，客商觉你无诚意，生意未成。"),
        generatedCard("trade_07", V3CardPool.Trade, "货栈失火",
            "夜半货栈起火，火光照见半条河。众人提桶来救，仓里的货却已经烧了大半。",
            "商旅", V3EffectDelta(silver = -28, cohesion = 2, garrisonMorale = 3), V3EffectDelta(silver = -45, unrest = 4),
            actLabel = "全力救火并抚恤",
            actDesc = "花钱雇人全力扑救，再抚恤受灾邻舍，以免结怨。",
            waitLabel = "只管自家货栈",
            waitDesc = "火只救自家的，邻舍烧了与我无关。",
            actSuccessText = "火势被压下，邻舍得了抚恤，乡勇也奋力扑救，损失比预想的小。",
            waitSuccessText = "自家货栈烧了大半，邻舍受灾更重，骂声一片。",
            actNote = "火灾中奋力扑救并抚恤邻舍，乡勇感奋，邻人怀德。",
            waitNote = "火只顾自家不顾邻舍，乡邻怨声载道，庄内怨气升腾。"),
        generatedCard("trade_08", V3CardPool.Trade, "账房收徒",
            "账房先生年老，想把算盘交给族中后辈。他说这一身算账的本事，总得有人接。",
            "商旅", V3EffectDelta(silver = -8, patriarchStewardship = 4, merchants = 2), V3EffectDelta(silver = -5),
            require = V3CardRequire(minAliveAdults = 2),
            actLabel = "选后辈拜师",
            actDesc = "从族中挑个聪明后生跟账房先生学徒，将来也好接管家业。",
            waitLabel = "另请账房",
            waitDesc = "老先生要退休，就从外面请个新账房，干净利落。",
            actSuccessText = "后辈磕了头敬了茶，账房先生把算盘交到他手里。",
            waitSuccessText = "新账房请来了，但老先生的那本旧账法无人再传。",
            actNote = "族中后辈拜师学账，家业管理后继有人，理财能力提升。",
            waitNote = "外请账房虽解燃眉，老账房的经验却随人而去。"),
        // ── 乡野 field ──
        generatedCard("field_01", V3CardPool.Field, "祈雨三日",
            "云层压在田埂上，雨却迟迟不落。佃户跪在田埂上，说再不下雨今年的秧苗就全完了。",
            "乡野", V3EffectDelta(grain = 20, villagers = 4, cohesion = 2), V3EffectDelta(grain = -14, unrest = 3),
            actLabel = "率族中祈雨",
            actDesc = "族长亲自带队在龙王庙祈雨三日，该花的银子花，该守的斋戒守。",
            waitLabel = "等天下雨",
            waitDesc = "祈雨是迷信，雨来不来都是天意，不必折腾。",
            actSuccessText = "第三日午后雷声响彻，大雨倾盆而下，佃户跪在泥里哭。",
            waitSuccessText = "秧苗枯了三成，佃户怨声载道，说族长连雨都不肯祈。",
            actNote = "亲率族人祈雨得甘霖，佃户感戴，乡民凝聚力大增。",
            waitNote = "不祈雨致秧苗枯萎，佃户怨族长无德，庄内不安。"),
        generatedCard("field_02", V3CardPool.Field, "猎户献皮",
            "猎户送来一张虎皮，只求换几石粮。他说这虎伤了山脚下三户人家，如今终于除了。",
            "乡野", V3EffectDelta(grain = -10, garrisonMorale = 5, bandits = -3), V3EffectDelta(villagers = -2),
            actLabel = "收皮赠粮",
            actDesc = "收下虎皮，给猎户十石粮，另赏碎银，鼓励乡勇护庄。",
            waitLabel = "压价收购",
            waitDesc = "虎皮虽好但价不高，给三石粮意思意思。",
            actSuccessText = "猎户扛着粮欢天喜地去了，乡勇听了也觉得跟着李氏有奔头。",
            waitSuccessText = "猎户骂骂咧咧走了，此后山里再有猛兽也无人来报。",
            actNote = "厚赏除虎猎户，乡勇士气大振，山中匪盗也收敛了几分。",
            waitNote = "压价收虎皮冷了猎户心，此后无人再愿为李氏出力。"),
        generatedCard("field_03", V3CardPool.Field, "佃户换契",
            "佃户说旧契太重，愿用劳作换一纸新约。他们说年景不好，旧租实在交不起了。",
            "乡野", V3EffectDelta(villagers = 8, cohesion = 3, silver = -8), V3EffectDelta(villagers = -8, unrest = 5),
            actLabel = "减租换契",
            actDesc = "减一成租重订新契，让佃户有活路，田才有人种。",
            waitLabel = "照旧约收租",
            waitDesc = "契纸写明了的，减了租族中银粮从哪来？",
            actSuccessText = "新契换罢，佃户松了口气，都说族长仁厚，秋收必定出力。",
            waitSuccessText = "几户佃户连夜逃了，地荒了一片，剩下的也整日唉声叹气。",
            actNote = "减租换契安抚佃户，乡民感戴，田地有人耕，庄内安定。",
            waitNote = "拒减租致佃户逃亡，田地抛荒，庄内怨气积聚。"),
        generatedCard("field_04", V3CardPool.Field, "田鼠成灾",
            "田间留下密密的洞，幼苗一夜少了半畦。老农说这是鼠年大灾，不早治粮就没了。",
            "乡野", V3EffectDelta(grain = -18, villagers = -3), V3EffectDelta(grain = -22, villagers = -3),
            actLabel = "购药灭鼠",
            actDesc = "花银从县城买来灭鼠药，发动全庄老小下田灭鼠。",
            waitLabel = "放任不管",
            waitDesc = "田鼠年年有，过段时间自然就少了。",
            actSuccessText = "三日灭鼠数千只，幼苗保住了大半，虽损了些粮但不至于绝收。",
            waitSuccessText = "鼠患蔓延到整片田，秋收粮少了近两成。",
            actNote = "及时购药灭鼠保住幼苗，虽有损但收成尚可。",
            waitNote = "放任鼠患蔓延，秋收大减，粮储告急。"),
        generatedCard("field_05", V3CardPool.Field, "水车停转",
            "河渠淤泥堵住水车，田庄等着一场清淤。老水车已经转了十几年，再不修就要报废。",
            "乡野", V3EffectDelta(silver = -14, grain = 28, patriarchStewardship = 2), V3EffectDelta(grain = -18),
            actLabel = "雇人清淤修水车",
            actDesc = "花银雇人清淤修水车，水利修好了田才能灌。",
            waitLabel = "用人力挑水灌田",
            waitDesc = "暂时用人力挑水浇田，等秋收后再修渠。",
            actSuccessText = "水车重新转起来，清水灌进龟裂的田地，佃户都松了口气。",
            waitSuccessText = "人力挑水只灌了一半田，另一半干裂，秋收减了不少。",
            actNote = "修渠清淤恢复水车灌溉，田地得水，粮产大增。",
            waitNote = "靠人力挑水灌田效率极低，大量田地缺水减产。"),
        generatedCard("field_06", V3CardPool.Field, "夜巡遇险",
            "巡庄人在芦苇里发现新脚印。不是猎户的靴印，是赤脚混着草鞋，数量不少。",
            "乡野", V3EffectDelta(garrisonMorale = 6, militia = 2, bandits = -4), V3EffectDelta(garrisonMorale = -5, bandits = 5),
            actLabel = "加强夜巡并设伏",
            actDesc = "派乡勇加夜巡，在芦苇荡设伏，先下手为强。",
            waitLabel = "紧闭寨门",
            waitDesc = "夜里关紧寨门，白日再派人查看，夜里不要出门。",
            actSuccessText = "伏兵擒住三个探路的流匪，审出了山匪的据点，匪患暂平。",
            waitSuccessText = "流匪在庄外转了几日，虽然没攻进来，却劫走了两头耕牛。",
            actNote = "夜巡设伏擒获流匪探子，乡勇士气大涨，匪盗闻风退却。",
            waitNote = "闭寨自守流匪仍劫走耕牛，乡勇士气受挫，匪患日盛。"),
        // ── 风闻 rumor ──
        generatedCard("rumor_01", V3CardPool.Rumor, "县城新谣",
            "县城茶馆里传出一桩与李氏有关的闲话。有人说你家盐船私通海寇，越传越离谱。",
            "风闻", V3EffectDelta(influence = 3, yamen = 2), V3EffectDelta(influence = -2),
            actLabel = "派人查谣并辟谣",
            actDesc = "派人去县城追查谣言源头，再请乡绅出面澄清。",
            waitLabel = "置之不理",
            waitDesc = "谣言止于智者，越解释越说不清，等它自然消散。",
            actSuccessText = "查出是仇家布的谣，茶馆里的人当面道歉，县衙也知道了真相。",
            waitSuccessText = "谣言越传越广，连县衙都派人来问话了。",
            actNote = "追查谣言并请乡绅辟谣，化险为夷，反在县衙前露了脸。",
            waitNote = "谣言扩散牵连官府，李氏声誉受损。"),
        generatedCard("rumor_02", V3CardPool.Rumor, "北地灾报",
            "北地来的脚夫说，饥民正在沿运河南下。他亲眼看见运河两岸都是拖家带口的人。",
            "风闻", V3EffectDelta(grain = -8, refugees = 4, patriarchConduct = 2), V3EffectDelta(refugees = 8, unrest = 3),
            actLabel = "提前备粮设粥",
            actDesc = "先存粮在庄外设粥棚，流民来了有口饭吃，不至于作乱。",
            waitLabel = "加固寨门",
            waitDesc = "流民来了是祸不是福，先把寨门加固，免得被冲击。",
            actSuccessText = "流民到时庄门外已有热粥，多数喝完便继续南下，无人闹事。",
            waitSuccessText = "流民冲到庄门，寨门虽挡住了人，却挡不住庄外的饥火和怨气。",
            actNote = "提前备粮设粥收容流民，施粥之举传为美谈，族长德行彰显。",
            waitNote = "不做准备致流民冲庄，庄内怨气升腾，人心惶惶。"),
        generatedCard("rumor_03", V3CardPool.Rumor, "旧官来信",
            "一封无署名的旧信，提到县衙即将换人。信里说新知县是个清流，最恨地方大族把持粮价。",
            "风闻", V3EffectDelta(yamen = 5, influence = 2), V3EffectDelta(yamen = -3),
            actLabel = "提前整理田册税契",
            actDesc = "把田亩税契整理清楚，新官来了挑不出毛病。",
            waitLabel = "照旧经营",
            waitDesc = "换个知县而已，铁打的大族流水的官，不必理会。",
            actSuccessText = "新官到任查册，李氏田亩税契一应俱全，反被夸为县中表率。",
            waitSuccessText = "新知县查出李家两笔欠税，罚了银还扣了功名。",
            actNote = "未雨绸缪整理田册税契，新官到任无可挑剔，县衙关系反升。",
            waitNote = "不理消息被新官查出欠税，受罚且失了脸面。"),
        generatedCard("rumor_04", V3CardPool.Rumor, "山中火光",
            "山脊上的火光一晚比一晚近。猎户说山匪在黑松岭聚众，已经不下百人。",
            "风闻", V3EffectDelta(garrisonMorale = 5, bandits = -5), V3EffectDelta(garrisonMorale = -4, bandits = 6),
            actLabel = "派人探查并报官",
            actDesc = "派精干乡勇探查匪情，同时向县衙报备，请兵助剿。",
            waitLabel = "加高寨墙",
            waitDesc = "匪在山上，不下来便不管，先加高寨墙自保。",
            actSuccessText = "探得匪情虚实，县衙派了弓兵来巡，山匪退了三十里。",
            waitSuccessText = "寨墙加高了，但山匪下了山劫了两处佃户村子，没人来救。",
            actNote = "探查匪情报官助剿，山匪退却，乡勇信心大增。",
            waitNote = "消极自保，山匪劫掠佃村，李氏坐视不救，乡人寒心。"),
        generatedCard("rumor_05", V3CardPool.Rumor, "船工密语",
            "船工说海口有一条不在册的货路。走这条线能避开税卡，利润翻倍，但要过海。",
            "风闻", V3EffectDelta(merchants = 6, silver = 12, routeDelta = V3RouteDelta(V3Route.Overseas, 4)), V3EffectDelta(merchants = -2),
            actLabel = "探路试航",
            actDesc = "派一条小船探路，风险虽大但海路利润可观。",
            waitLabel = "不走私路",
            waitDesc = "不在册的货路便是私路，查到了是死罪。",
            actSuccessText = "探路船回来了，海货比内河便宜三成，新的商路打开了。",
            waitSuccessText = "别人的船走了那条路发了财，你还在内河赚辛苦钱。",
            actNote = "探通海路贸易新线，为海贸路线打下根基。",
            waitNote = "不敢冒险试海路，错失海贸先机，商路止步内河。"),
        // ── 产业 estate ──
        generatedCard("estate_01", V3CardPool.Estate, "仓门加固",
            "旧粮仓的木门遇潮，锁眼已经发黑。仓房老头说再换个锁，粮被人摸走了都不知道。",
            "产业", V3EffectDelta(silver = -10, grain = 12, patriarchStewardship = 2), V3EffectDelta(grain = -12),
            actLabel = "换新门加铁锁",
            actDesc = "换上新木门加铁锁，粮仓是庄中命脉，不可大意。",
            waitLabel = "凑合着用",
            waitDesc = "旧门还能用，加把锁便是，不必花大价钱换新。",
            actSuccessText = "新门铁锁装上，仓房里的粮再也没少过，账实相符。",
            waitSuccessText = "果然丢了粮，查了三日也没查出是谁摸走的。",
            actNote = "加固仓门铁锁，粮仓从此账目清明，粮储损耗减少。",
            waitNote = "仓门不严粮被偷走，粮储悄悄流失。"),
        generatedCard("estate_02", V3CardPool.Estate, "作坊招匠",
            "一名逃来的工匠会烧窑，只求一处能遮雨的屋檐。他说原是景德镇窑工，因兵灾逃到这里。",
            "产业", V3EffectDelta(silver = -18, patriarchStewardship = 5, cohesion = 2), V3EffectDelta(silver = -5),
            actLabel = "收留建窑",
            actDesc = "给工匠一处屋、一笔银建窑，将来陶器能卖也能用。",
            waitLabel = "给些米打发",
            waitDesc = "给几斗米让他去别处，庄里不养闲人。",
            actSuccessText = "半年后窑烧出第一批陶器，釉色虽不及景德镇，但本县够用了。",
            waitSuccessText = "工匠去了隔壁庄，三年后那边的陶器卖到了县城。",
            actNote = "收留景德镇窑工建窑，新增窑业作坊，产业经营能力大增。",
            waitNote = "拒留窑工致其投他处，错失建窑良机。"),
        generatedCard("estate_03", V3CardPool.Estate, "集市换契",
            "集市摊主愿以租金换一处固定棚位。他们说风里来雨里去，有个固定摊位生意才好做。",
            "产业", V3EffectDelta(silver = 24, merchants = 4, influence = 2), V3EffectDelta(silver = -8, merchants = -3),
            actLabel = "收租批地",
            actDesc = "划出集市固定棚位收年租，既稳了收入也稳了集市。",
            waitLabel = "维持散摊",
            waitDesc = "不设固定摊位，按日收摊税，谁来谁交，灵活。",
            actSuccessText = "固定棚位租了出去，集市从此天天有市，银钱稳定入账。",
            waitSuccessText = "摊主们嫌摊位不定，渐去了邻县集市，这边冷清了不少。",
            actNote = "设固定棚位收年租，集市稳定繁荣，商路税收双增。",
            waitNote = "维持散摊致摊主流失，集市凋敝，商路税收双降。"),
        generatedCard("estate_04", V3CardPool.Estate, "医馆缺药",
            "医馆说药材涨价，病人却一日多过一日。时气不好，伤寒和痢疾在乡中蔓延。",
            "产业", V3EffectDelta(silver = -16, villagers = 5, unrest = -4), V3EffectDelta(villagers = -5, unrest = 5),
            actLabel = "贴钱补药",
            actDesc = "贴银子从府城购药给医馆，治病救命的事不能省。",
            waitLabel = "抬高诊金",
            waitDesc = "药材贵了诊金自然要涨，医馆也要吃饭。",
            actSuccessText = "药到了，几十条命救了回来，乡民给族长送了面\"德被乡邻\"的匾。",
            waitSuccessText = "病人们看不起病，拖重了几人，庄里怨声四起。",
            actNote = "贴钱补药救治乡民，病患得愈，乡人感德，庄内安定。",
            waitNote = "抬高诊金致病人无医，乡人怨怼，庄内怨气升腾。"),
        generatedCard("estate_05", V3CardPool.Estate, "寨墙补缝",
            "寨墙裂了一道缝，正好能让一个人侧身钻过。乡勇说夜里常有影子在缝外晃。",
            "产业", V3EffectDelta(silver = -20, garrisonMorale = 5, bandits = -4), V3EffectDelta(garrisonMorale = -6, bandits = 5),
            actLabel = "征丁修墙",
            actDesc = "征调族中丁壮修补寨墙，墙不牢庄不安全。",
            waitLabel = "派人守缝",
            waitDesc = "在缝边设个岗哨日夜看守，省了修墙的银。",
            actSuccessText = "寨墙补好又加高了三尺，乡勇巡墙时腰杆都直了。",
            waitSuccessText = "守缝的乡勇夜里打了个盹，第二天少了一头牛。",
            actNote = "征丁修补寨墙加高三尺，庄防牢固，乡勇安心，匪盗却步。",
            waitNote = "只设岗哨不修墙，守兵懈怠，匪盗仍能潜入。"),
        generatedCard("estate_06", V3CardPool.Estate, "桥梁重修",
            "进庄的桥被洪水冲空了桥脚。人走上去桥身晃，马车更是不敢过。",
            "产业", V3EffectDelta(silver = -16, influence = 4, villagers = 4), V3EffectDelta(influence = -3),
            actLabel = "集资修桥",
            actDesc = "花银重修石桥，桥是进出庄的命脉，不可不修。",
            waitLabel = "搭便桥",
            waitDesc = "先用木板搭个便桥走人，等有了钱再修石桥。",
            actSuccessText = "石桥修好那天，四乡八邻都来踩桥，说李氏办了件大好事。",
            waitSuccessText = "便桥摇摇晃晃，商队宁可绕远路也不走了，生意淡了。",
            actNote = "重修石桥连通庄内外，四乡称便，李氏声望大增。",
            waitNote = "搭便桥商队绕行，商旅不便，李氏声望受损。"),
        generatedCard("estate_07", V3CardPool.Estate, "商铺换匾",
            "铺面掌柜想把李氏的名号挂得更醒目。他说匾越大，客人越觉得这家铺子有靠山。",
            "产业", V3EffectDelta(silver = -12, influence = 5, merchants = 4), V3EffectDelta(influence = -2),
            actLabel = "换金匾张扬",
            actDesc = "花银子做块鎏金大匾，李氏的名号要打得响亮。",
            waitLabel = "保持低调",
            waitDesc = "财不外露，木匾即可，免得招人眼红。",
            actSuccessText = "鎏金匾挂上，客人果然多了三成，连邻县商户都慕名而来。",
            waitSuccessText = "铺子还是那块旧匾，生意不温不火。",
            actNote = "商铺挂鎏金大匾张扬族名，商户慕名而来，声望商路双增。",
            waitNote = "商铺低调无特色，生意平淡，声望不进反退。"),
        generatedCard("estate_08", V3CardPool.Estate, "田契归档",
            "几十张新旧田契堆在案头，稍有疏忽便会错一行。账房说有两张田契的四至对不上。",
            "产业", V3EffectDelta(silver = 14, patriarchStewardship = 3, cohesion = 2), V3EffectDelta(silver = -10, cohesion = -2),
            actLabel = "亲核田契",
            actDesc = "把所有田契摊开逐张核验，重新造册归档。",
            waitLabel = "交账房处理",
            waitDesc = "田契的事交给账房去理，族长不必亲躬。",
            actSuccessText = "核验出两张重契和一张虚契，追回十四两银，田册从此清清楚楚。",
            waitSuccessText = "账房理了半日也没理清，田契的事就那样糊涂着。",
            actNote = "亲核田契追回重契虚契银两，田产账目从此清明。",
            waitNote = "田契糊涂不清，族产有暗漏，族人暗生嫌隙，族心渐散。"),
        // ── 讼案/章节 chain ──
        generatedCard("chain_01", V3CardPool.Chain, "旧债追门",
            "十年前的借据被人从箱底翻出，债主已经站在门外。利滚利算下来，已经远超本金。",
            "讼案", V3EffectDelta(silver = -22, yamen = 4, cohesion = 2), V3EffectDelta(silver = -45, yamen = -4, unrest = 4),
            actLabel = "连本带利还清",
            actDesc = "欠债还钱，哪怕多付些利息，也要保住族中信誉。",
            waitLabel = "告官争辩",
            waitDesc = "十年旧债利滚利不合律，告到县衙也未必全还。",
            actSuccessText = "债清了，债主当众撕了借据，说李氏认账是厚道人。",
            waitSuccessText = "官司打了半年，虽减了些利息但银钱花了更多，还结了仇。",
            actNote = "还清旧债保族中信誉，债主感佩，县衙中也留下正直之名。",
            waitNote = "为旧债打官司结仇，银钱花费更多，官府关系受损。"),
        generatedCard("chain_02", V3CardPool.Chain, "田界争讼",
            "两家的田界只隔一块倒下的界碑。你家说是他移了碑，他家说是你犁过了界。",
            "讼案", V3EffectDelta(silver = -12, yamen = 5, influence = 2), V3EffectDelta(yamen = -4, cohesion = -3),
            actLabel = "请乡绅公断",
            actDesc = "请县里乡绅来公断田界，当众立碑，日后不生争议。",
            waitLabel = "强行犁地",
            waitDesc = "田是我家的就是我家的，先犁了再说，有本事告官去。",
            actSuccessText = "乡绅查了鱼鳞图册当众断了界，两家人虽不服却认了。",
            waitSuccessText = "两家打了起来，伤了人，告到县衙，你被杖责还丢了田。",
            actNote = "请乡绅公断田界立碑，县中乡绅觉你处事公道，声望增。",
            waitNote = "强行犁地致伤人命官司，受刑丢田，族誉大损。"),
        generatedCard("chain_03", V3CardPool.Chain, "族学名额",
            "县学教谕说，愿给族中孩子一个旁听名额。能进县学旁听，将来考秀才便有了门路。",
            "章节", V3EffectDelta(silver = -12, influence = 5, routeDelta = V3RouteDelta(V3Route.Scholar, 6)), V3EffectDelta(influence = -2),
            require = V3CardRequire(minChildren = 1),
            actLabel = "送子入县学",
            actDesc = "准备束脩送银，选族中最聪慧的孩子去县学旁听。",
            waitLabel = "辞谢名额",
            waitDesc = "孩子还小，等再大些再说，不急这一时。",
            actSuccessText = "孩子背着书箱去了县学，教谕说这孩子灵性足，将来有指望。",
            waitSuccessText = "名额给了别家，后来听说那孩子中了秀才，你悔了好久。",
            actNote = "送族中子弟入县学旁听，书香之路正式开启。",
            waitNote = "推辞县学名额，错失子弟进学良机。"),
        generatedCard("chain_04", V3CardPool.Chain, "军需借道",
            "军需车队想借庄道过境，留下的却是一张空白欠条。百户说军粮急用，欠条回营再填。",
            "讼案", V3EffectDelta(garrison = 6, yamen = 3, silver = -12), V3EffectDelta(garrison = -5, influence = -2),
            actLabel = "放行并供粮",
            actDesc = "军需过境是公事，送些粮草让军爷们记得李氏的人情。",
            waitLabel = "要求先填欠条",
            waitDesc = "空白欠条不能作数，先填好数额盖了官印再说。",
            actSuccessText = "百户拍着胸脯说他日必有重谢，军爷们吃了饭整队而去。",
            waitSuccessText = "百户冷哼一声说李氏不晓事，绕了远路，日后军里有人来找麻烦。",
            actNote = "供粮助军需过境，军中百户记了人情，军镇关系初建。",
            waitNote = "军需过境不肯通融，军中记恨，日后军中关系紧张。"),
        generatedCard("chain_05", V3CardPool.Chain, "旁支分门",
            "旁支说自己已经足够富裕，想另立祠门。他们说人多了总在一起容易生事，分开反而长远。",
            "族务", V3EffectDelta(cohesion = 6, influence = -2), V3EffectDelta(cohesion = -8, unrest = 4),
            require = V3CardRequire(minBranchCount = 2),
            actLabel = "准其分门立祠",
            actDesc = "准许旁支另立祠门，分谱不分宗，日后还是一家人。",
            waitLabel = "强留不许",
            waitDesc = "一族岂能分祠？强留不许，违者逐出宗族。",
            actSuccessText = "分门之日，两祠同祭祖先，虽分门却不分心，关系反而和睦了。",
            waitSuccessText = "旁支忿忿不平，明里不分暗里疏远，族中气氛压抑。",
            actNote = "准许旁支分门立祠，分谱不分宗，族内和睦依旧。",
            waitNote = "强留旁支不许分门，嫌隙日深，族内裂痕难补。"),
        generatedCard("chain_06", V3CardPool.Chain, "妇人理账",
            "几位主母把散乱的内宅账本带到族长案前。她们说内宅的用度、人情、赏赐，也该有个明白账。",
            "内宅", V3EffectDelta(silver = 18, cohesion = 3, patriarchConduct = 2), V3EffectDelta(silver = -8, cohesion = -2),
            require = V3CardRequire(hasSpouse = true),
            actLabel = "允妇理内账",
            actDesc = "内宅交主母们理，外宅交账房管，内外分清才是治家之道。",
            waitLabel = "统归外账房",
            waitDesc = "账不分内外，统归账房管理，妇人不必插手银钱。",
            actSuccessText = "主母们理出内宅历年积余十八两银，还省下了不少虚耗。",
            waitSuccessText = "内宅用度依然糊涂，主母们觉得不被信任，冷了心。",
            actNote = "允主母理内宅账，内外分明，内宅和谐，银钱节省。",
            waitNote = "不许妇人理账致内宅用度糊涂，主母心冷，内宅不睦。"),
        // ── 年务 annual ──
        generatedCard("annual_01", V3CardPool.Annual, "春耕誓约",
            "春耕前，各房要在祠堂立下今年的田亩约。谁种多少田、交多少租、出多少丁，都要写在约上。",
            "年务", V3EffectDelta(grain = 25, cohesion = 4, patriarchStewardship = 2), V3EffectDelta(grain = -12, cohesion = -3),
            actLabel = "立约督耕",
            actDesc = "在祠堂当众立约，各房画押，族长亲自督耕，不容偷懒。",
            waitLabel = "各房自理",
            waitDesc = "春耕是各房自己的事，不必立约，各自安排即可。",
            actSuccessText = "誓约立下，各房春播争先恐后，秋收粮比去年多了两成。",
            waitSuccessText = "无约无束，有几房偷懒只种了一半田，秋收少了粮。",
            actNote = "春耕立约督耕，各房尽力，秋收大增，族内秩序井然。",
            waitNote = "不立督耕之约，田地有荒，秋收减少，族内松散。"),
        generatedCard("annual_02", V3CardPool.Annual, "秋收分粮",
            "秋收入仓，分粮的秤杆比往年更沉。各房人丁都在仓外等着，这是一年里最要紧的日子。",
            "年务", V3EffectDelta(grain = 40, cohesion = 3, villagers = 3), V3EffectDelta(grain = 12, cohesion = -5),
            actLabel = "按丁按约分粮",
            actDesc = "严格按春誓之约分粮，劳力多的多分，立下的规矩不能破。",
            waitLabel = "平均分配",
            waitDesc = "都是族人，平均分粮省得争执，和和气气最好。",
            actSuccessText = "按约分粮无人有怨言，劳力多的人家拿了粮说春誓值了。",
            waitSuccessText = "平均分了粮，出力多的人家憋了气，明年谁还肯多出力？",
            actNote = "按约按丁分粮，赏罚分明，族人服膺，佃户也感公平。",
            waitNote = "平均主义分粮，出力多者寒心，来年耕作懈怠。"),
        generatedCard("annual_03", V3CardPool.Annual, "岁末祭祖",
            "岁末风紧，祠堂灯火仍得按旧例点满。祭器要擦亮，祭品要齐备，三跪九叩一个不能少。",
            "年务", V3EffectDelta(silver = -12, influence = 4, cohesion = 5), V3EffectDelta(influence = -3, cohesion = -2),
            actLabel = "按旧例大排祭",
            actDesc = "岁末祭祖是一年最重的礼，按祖制大排祭，不可简省。",
            waitLabel = "精简祭祀",
            waitDesc = "年景不好，祭礼从简，心意到了祖宗不怪。",
            actSuccessText = "祭祖如仪，族人在祠堂跪了一地，香烟缭绕中仿佛祖宗在看着。",
            waitSuccessText = "祭礼寒酸，族老摇头叹气，说这是一年不如一年了。",
            actNote = "岁末大排祭祀，乡邻闻之来观，皆赞李氏知礼重祖，声望大增。",
            waitNote = "精简祭礼惹族老不满，乡人觉李氏衰败之兆。"),
        generatedCard("annual_04", V3CardPool.Annual, "族学月考",
            "族学先生把孩子们的卷子摊在案上。有的字写得周正，有的还在描红，先生说优劣要分。",
            "年务", V3EffectDelta(influence = 5, patriarchPrestige = 2, routeDelta = V3RouteDelta(V3Route.Scholar, 5)), V3EffectDelta(influence = -2),
            require = V3CardRequire(minChildren = 1),
            actLabel = "设奖赏优罚劣",
            actDesc = "考得好的赏笔墨纸砚，考得差的罚抄书，激励孩子上进。",
            waitLabel = "不分等第",
            waitDesc = "孩子还小，不分等第免得打击信心，都夸几句便是。",
            actSuccessText = "优等的孩子得了奖，回去更加用功；差的也红了脸说下月赶上来。",
            waitSuccessText = "孩子们觉得考好考坏一个样，渐渐松懈了学业。",
            actNote = "族学月考设赏罚激励子弟，学风日上，书香之气渐成。",
            waitNote = "族学无赏罚，子弟学业荒废，书香之路遥遥。"),
        generatedCard("annual_05", V3CardPool.Annual, "冬藏验仓",
            "第一场霜落下，账房请族长亲自验仓。冬粮够不够吃到明年春收，全在这一次盘点上。",
            "年务", V3EffectDelta(grain = 30, patriarchStewardship = 3), V3EffectDelta(grain = -15, refugees = 4),
            actLabel = "亲自验仓盘粮",
            actDesc = "亲自下仓逐袋验看，粮不够早想办法，不要等到开春慌了手脚。",
            waitLabel = "信账房所报",
            waitDesc = "账房报了数便信他，天寒地冻不必亲自跑仓。",
            actSuccessText = "验仓发现账上多报了三十石粮，虽有亏空但及时发现还来得及补。",
            waitSuccessText = "开春才发现粮少了，急得团团转，只能高价买粮渡荒。",
            actNote = "亲自验仓盘粮，提前发现粮储缺口，冬春之间安排妥当。",
            waitNote = "不验仓致粮储不足，开春粮荒，高价买粮损失惨重。"),
        // ── 内宅 home ──
        generatedCard("home_01", V3CardPool.Clan, "抓周开席",
            "孩子抓住一枚算盘，满堂人都笑了。有人说这是将来经商的料，有人说抓周不作数。",
            "内宅", V3EffectDelta(silver = 8, cohesion = 4, routeDelta = V3RouteDelta(V3Route.Merchant, 3)), V3EffectDelta(cohesion = -1),
            require = V3CardRequire(minChildren = 1),
            actLabel = "大办抓周宴",
            actDesc = "请族中老小来吃酒，孩子抓周是大事，办得热闹些。",
            waitLabel = "家宴小庆",
            waitDesc = "自家人吃碗面便是，不必惊动全族。",
            actSuccessText = "抓周宴上族老给孩子取了字，满堂欢声笑语，都说这孩子有福气。",
            waitSuccessText = "冷冷清清一碗面，孩子娘觉得委屈，背地里抹了泪。",
            actNote = "为孩子办抓周宴，族老赐字，满堂欢喜，孩子有了经商之兆。",
            waitNote = "抓周简办致主母不快，内宅略生芥蒂。"),
        generatedCard("home_02", V3CardPool.Clan, "母亲问安",
            "远房母亲来信，问家中是否还留着旧宅。信里说想回来看看，顺便给亡夫扫个墓。",
            "内宅", V3EffectDelta(cohesion = 4, influence = 2), V3EffectDelta(cohesion = -2),
            actLabel = "接母亲回庄",
            actDesc = "派人去接，旧宅打扫干净，老人回来住些日子也是好的。",
            waitLabel = "寄银不接人",
            waitDesc = "寄些银子回去，说庄里忙，改日再请母亲回来。",
            actSuccessText = "老母亲回来了，拄着拐杖在旧宅转了一圈，眼泪汪汪地说还是家里好。",
            waitSuccessText = "母亲收到银，再没了信，听说半年后就过世了。",
            actNote = "接远房母亲回庄小住，族人见族长重孝，凝聚更坚。",
            waitNote = "未接老人回家，后闻其过世，心有愧疚，族人亦觉族长薄情。"),
        generatedCard("home_03", V3CardPool.Clan, "媒人过门",
            "媒人带来一户相合的人家，请族长先看门第。是邻县书香之家，门第清白，女儿知书。",
            "内宅", V3EffectDelta(silver = -6, cohesion = 4, influence = 2), V3EffectDelta(cohesion = -2),
            actLabel = "先看门第再定",
            actDesc = "派人打听对方家风，门第相当再交换庚帖。",
            waitLabel = "暂不议婚",
            waitDesc = "婚事先不着急，等孩子大些再说。",
            actSuccessText = "对方家风清白、门第端正，婚事初步定下，只待选吉日。",
            waitSuccessText = "媒人看你不甚热心，转头把姑娘说给了别家。",
            actNote = "核查对方门第后定下亲事，门当户对，族人称善。",
            waitNote = "拖延议婚错过好姻缘，良缘另许他家。"),
        generatedCard("home_04", V3CardPool.Clan, "主母设粥",
            "内宅想在庄门设粥，却先要知道粮仓还能撑几日。主母说，她见过饥民的眼神，不想再看。",
            "内宅", V3EffectDelta(grain = -18, refugees = -4, villagers = 6, unrest = -5), V3EffectDelta(unrest = 6, villagers = -4),
            require = V3CardRequire(hasSpouse = true),
            actLabel = "出粮助主母施粥",
            actDesc = "拨粮给主母设粥棚，这是积德的事，不能小气。",
            waitLabel = "劝主母暂停",
            waitDesc = "粮已不多，先顾庄内，外面的饥民管不过来。",
            actSuccessText = "粥棚设了七七四十九日，救活数百人，乡中给主母立了长生牌位。",
            waitSuccessText = "庄外饿殍日多，饥民冲庄的事差一点就发生了。",
            actNote = "支持主母施粥救民，乡人感戴德，庄内安定，主母贤名远播。",
            waitNote = "不许施粥，饥民冲庄险些酿成大祸，庄内怨气弥漫。"),
        generatedCard("home_05", V3CardPool.Clan, "祖母遗匣",
            "祖母留下的木匣里，只有一张褪色的婚书。婚书背面写着一行小字：\"但求后人守得平安。\"",
            "内宅", V3EffectDelta(cohesion = 4, influence = 3, patriarchPrestige = 2), V3EffectDelta(cohesion = -2),
            actLabel = "供奉婚书于祠堂",
            actDesc = "把祖母的婚书供在祠堂，让后人都知道先人的嘱托。",
            waitLabel = "收入匣中",
            waitDesc = "婚书收起来便是，不必大张旗鼓。",
            actSuccessText = "婚书供入祠堂那天，全族老小都来拜了，说祖母是李家的根。",
            waitSuccessText = "婚书锁进了匣中，无人再提起，仿佛祖母从没来过。",
            actNote = "祖母婚书入祠供奉，族人饮水思源，凝聚力大增。",
            waitNote = "祖母遗愿未彰显，族中年轻一代已不知先人的艰难。"),
        generatedCard("home_06", V3CardPool.Clan, "兄弟夜谈",
            "兄弟坐在廊下，第一次谈起各房日后要分什么。长兄说要田，二弟说要铺，三弟说只要族学。",
            "内宅", V3EffectDelta(cohesion = 5, patriarchConduct = 3), V3EffectDelta(cohesion = -7, unrest = 3),
            require = V3CardRequire(minAlivePeople = 3),
            actLabel = "当下说分明",
            actDesc = "趁兄弟都在，把各房日后的分法说清楚，写在纸上，免得到时反目。",
            waitLabel = "等父辈仙去再说",
            waitDesc = "分家是以后的事，现在说徒伤感情。",
            actSuccessText = "兄弟当夜把分法谈妥了，写了分书各执一份，廊下笑声到三更。",
            waitSuccessText = "没谈出个结果，兄弟各怀心事，此后说话都隔着一层。",
            actNote = "兄弟夜谈明析分法，立分书为凭，日后分家不起争端。",
            waitNote = "分家之事悬而未决，兄弟嫌隙渐生，各房暗生芥蒂。"),
        // ── 危机：粮荒 ──
        generatedCard("crisis_grain_01", V3CardPool.Crisis, "粮荒·舍粥",
            "粮仓见底后，庄门外只剩一口大锅能说话。饥民围在锅边，眼睛盯着翻滚的粥。",
            "灾变", V3EffectDelta(grain = -30, refugees = -8, villagers = 8, unrest = -8), V3EffectDelta(unrest = 12, refugees = 8), crisisLevel = 1,
            actLabel = "开大锅舍粥",
            actDesc = "再撑七日粥，让饥民有活路，庄内也不会乱。",
            waitLabel = "保粮封锅",
            waitDesc = "粮已不多，保住庄内人要紧，外人顾不上了。",
            actSuccessText = "七日粥救了几百条命，饥民喝了粥陆续南去，庄内无人闹事。",
            waitSuccessText = "封锅当天夜里就有人冲庄门，庄外哭喊声一片。",
            actNote = "粮荒中开大锅舍粥七日，活人无数，乡人感恩戴德。",
            waitNote = "封锅拒施粥，饥民冲庄，庄内险象环生。"),
        generatedCard("crisis_grain_02", V3CardPool.Crisis, "粮荒·借种",
            "佃户请求留下明年的种粮，不肯再把最后一袋交租。他们说，交了种粮，明年大家一起饿死。",
            "灾变", V3EffectDelta(grain = -20, villagers = 10, cohesion = 3), V3EffectDelta(grain = 10, villagers = -12, unrest = 8), crisisLevel = 1,
            actLabel = "允留种粮免租",
            actDesc = "种粮留给佃户，今年免了这一袋租，明年才有收成的指望。",
            waitLabel = "强征种粮",
            waitDesc = "不交租怎么行？种粮也得交，明年的事明年再说。",
            actSuccessText = "佃户留了种粮，跪在田埂上磕头，说明年秋收加倍偿还。",
            waitSuccessText = "种粮被强征，十几户佃户连夜逃了，地全荒了。",
            actNote = "允佃户留种免租，佃户感泣，来年耕作更勤。",
            waitNote = "强征种粮致佃户逃亡，田地抛荒，来年饥荒更深。"),
        generatedCard("crisis_grain_03", V3CardPool.Crisis, "粮荒·开仓",
            "账房跪在地上说，再开三日仓，冬粮就不够了。庄外饥民的哭声已经传到了内宅。",
            "灾变", V3EffectDelta(grain = -45, refugees = -12, influence = 4), V3EffectDelta(refugees = 12, unrest = 10), crisisLevel = 1,
            actLabel = "开仓放粮三日",
            actDesc = "开仓放粮三日，不管庄内庄外，先保住人命，粮总会有办法。",
            waitLabel = "只供庄内",
            waitDesc = "粮仓只对庄内人开放，外庄饥民不得入内。",
            actSuccessText = "三日放粮，饥民有序领粮而去，无一人闹事，乡中说李氏是救命菩萨。",
            waitSuccessText = "庄外饥民得知消息，冲击寨门，险些酿成民变。",
            actNote = "开仓放粮救万民，虽损粮但声望在乡中大振。",
            waitNote = "只管庄内不顾庄外，饥民冲寨险酿民变。"),
        generatedCard("crisis_grain_04", V3CardPool.Crisis, "粮荒·米船",
            "一条米船停在河心，只要现银就能靠岸。船主说银到粮到，但价是平时的三倍。",
            "灾变", V3EffectDelta(silver = -55, grain = 85, merchants = 5), V3EffectDelta(grain = -22, refugees = 8), crisisLevel = 1,
            actLabel = "付银购粮",
            actDesc = "花高价买米船的粮，救命要紧，不是算细账的时候。",
            waitLabel = "等粮价落",
            waitDesc = "三倍米价是趁火打劫，等几日别处米船来了再说。",
            actSuccessText = "米船靠岸，八十五石粮入仓，庄里人见了粮，悬着的心落了地。",
            waitSuccessText = "等了半月米船不来，粮价又涨了，饥荒更深。",
            actNote = "高价购入米船粮八十五石，粮荒缓解，商帮也在患难中结下关系。",
            waitNote = "等米价回落未果，粮荒加剧，流民涌入庄内。"),
        generatedCard("crisis_grain_05", V3CardPool.Crisis, "粮荒·逃户",
            "最先离开的不是外乡人，而是熟悉田埂的佃户。他们扛着包袱，说再不走全家都要饿死。",
            "灾变", V3EffectDelta(grain = -8, refugees = -6, villagers = 5), V3EffectDelta(refugees = 16, cohesion = -5), crisisLevel = 1,
            actLabel = "给粮留人",
            actDesc = "给每户发两斗粮留下，人在田在，人走了田就荒了。",
            waitLabel = "任其离去",
            waitDesc = "要走的留不住，强留也是白搭，随他们去。",
            actSuccessText = "发了粮，走了的佃户回来了一半，田地没有全荒。",
            waitSuccessText = "佃户走了大半，田荒了，庄里像被抽去了脊梁。",
            actNote = "发粮留住佃户，田地保住大半，灾后恢复有望。",
            waitNote = "佃户大量逃亡致田地抛荒，庄内人心涣散。"),
        // ── 危机：民怨 ──
        generatedCard("crisis_unrest_01", V3CardPool.Crisis, "民怨·抗租",
            "佃户把租契撕成两半，站在祠堂前等你的话。他们说去年的粮连肚子都填不饱，还交什么租。",
            "灾变", V3EffectDelta(silver = -15, villagers = 12, unrest = -12, cohesion = 4), V3EffectDelta(villagers = -15, unrest = 14, cohesion = -5), crisisLevel = 2,
            actLabel = "当众减租",
            actDesc = "在祠堂前宣布减租三成，旧账一笔勾销，换佃户继续种地。",
            waitLabel = "派人押收",
            waitDesc = "佃户抗租是反了，派乡勇去催，不交就拿人。",
            actSuccessText = "减租令下，佃户跪地磕头，当场把撕了的租契拼了回去。",
            waitSuccessText = "乡勇催租激起众怒，佃户聚众围了仓房，差点出了人命。",
            actNote = "当众减租平息抗租风波，佃户感戴，庄内安定。",
            waitNote = "强征租子激成民变，庄内大乱。"),
        generatedCard("crisis_unrest_02", V3CardPool.Crisis, "民怨·堵门",
            "庄门被柴车堵住，乡民说今天必须有个说法。他们不要银，只要一口饭和一条活路。",
            "灾变", V3EffectDelta(grain = -18, villagers = 8, unrest = -10), V3EffectDelta(unrest = 16, garrisonMorale = -8), crisisLevel = 2,
            actLabel = "出面开粮",
            actDesc = "亲自到庄门见乡民，开粮放赈，当面给个说法。",
            waitLabel = "从后门去县衙搬兵",
            waitDesc = "乡民堵门是造反，先去县衙请兵来弹压。",
            actSuccessText = "族长站在庄门上当众承诺开粮，乡民散去，柴车也推走了。",
            waitSuccessText = "乡民等了半日不见人，撞开了庄门，粮仓被抢了一半。",
            actNote = "亲到庄门安抚乡民并开粮放赈，一场民变消于无形。",
            waitNote = "避而不见去搬兵，乡民怒而撞庄门，粮仓被抢。"),
        generatedCard("crisis_unrest_03", V3CardPool.Crisis, "民怨·旧怨",
            "几户人翻出十年前的欠账，怨气终于找到名字。他们说当年修渠占了他家的地，至今没赔。",
            "灾变", V3EffectDelta(silver = -22, cohesion = 5, unrest = -8), V3EffectDelta(cohesion = -8, unrest = 12), crisisLevel = 2,
            actLabel = "查旧账赔偿",
            actDesc = "翻出十年前的旧账查核，该赔的赔，该补的补，旧怨了结。",
            waitLabel = "陈年旧账不理",
            waitDesc = "十年前的账现在才翻出来，分明是借机敲诈，不理。",
            actSuccessText = "旧账查清赔了银，几户人家当场表示再不提此事，怨气消散。",
            waitSuccessText = "旧怨未平，几户人暗中串联，庄里四处都在传李氏的不是。",
            actNote = "核查旧账并赔偿，十年积怨一朝化解，乡中太平。",
            waitNote = "不理旧怨致怨气蔓延，庄内流言四起，人心浮动。"),
        generatedCard("crisis_unrest_04", V3CardPool.Crisis, "民怨·断契",
            "乡民愿意留下，但要把旧租契烧在祠堂门外。他们说，契不烧，心不安。",
            "灾变", V3EffectDelta(silver = -28, villagers = 15, cohesion = 4, unrest = -15), V3EffectDelta(villagers = -12, unrest = 18), crisisLevel = 2,
            actLabel = "当众烧契换新约",
            actDesc = "在祠堂门前烧旧契，当场签新约减租，与乡民重新开始。",
            waitLabel = "契纸岂能说烧就烧",
            waitDesc = "旧契是祖上传下来的，烧了便是败家，绝不答应。",
            actSuccessText = "旧契烧了，新约签了，乡民欢呼声传遍四野，庄门大开。",
            waitSuccessText = "乡民绝望了，连夜投奔了山匪，庄外匪患更重了。",
            actNote = "当众烧旧契签新约减租，乡民归心，庄内大安。",
            waitNote = "拒烧旧契致乡民绝望投匪，匪患日重，庄内危矣。"),
        generatedCard("crisis_unrest_05", V3CardPool.Crisis, "民怨·乡约",
            "乡老提议重订一份乡约，把各户的分担写清楚。税、粮、役、赈，都要有章可循。",
            "灾变", V3EffectDelta(silver = -10, influence = 4, villagers = 10, unrest = -10), V3EffectDelta(influence = -3, unrest = 12), crisisLevel = 2,
            actLabel = "召集乡老定乡约",
            actDesc = "召集各乡乡老合议，把乡约定下来，刻石立碑，人人遵守。",
            waitLabel = "旧约够用不必新订",
            waitDesc = "祖上传下的规矩还在，新订乡约多此一举。",
            actSuccessText = "乡约订立，各户分担清清楚楚，刻碑立在祠堂前，从此再无争执。",
            waitSuccessText = "旧约模糊，各户照旧争吵不休，怨气越积越深。",
            actNote = "召集乡老立新约刻石为凭，乡中秩序井然，声望在乡中大振。",
            waitNote = "不订新乡约致纷争不断，怨气积聚终酿大祸。"),
        // ── 危机：庄乱 ──
        generatedCard("crisis_mutiny_01", V3CardPool.Crisis, "庄乱·欠饷",
            "乡勇把刀放在桌上，说守庄不能只靠家声。三个月没发饷了，家里婆娘都快饿死了。",
            "灾变", V3EffectDelta(silver = -35, garrisonMorale = 18, unrest = -12, cohesion = 3), V3EffectDelta(garrisonMorale = -15, unrest = 18), crisisLevel = 3,
            actLabel = "立即发饷并赏",
            actDesc = "先发三个月欠饷，再每人赏二两，乡勇守庄是命根子，不能亏待。",
            waitLabel = "先打欠条",
            waitDesc = "饷先记着，等庄里缓过来再补发，乡勇要体谅族中难处。",
            actSuccessText = "饷银发下，乡勇们拍着胸脯说愿与庄共存亡，士气大振。",
            waitSuccessText = "乡勇们当晚散了一半，剩下的也刀不出鞘，寨墙形同虚设。",
            actNote = "发饷并赏乡勇，守庄将士士气如虹，庄防稳固。",
            waitNote = "欠饷不发致乡勇离散，庄防空虚，危在旦夕。"),
        generatedCard("crisis_mutiny_02", V3CardPool.Crisis, "庄乱·夺门",
            "有人在夜里摸向西寨门，火把照出熟人的脸。是守寨门的乡勇，他想开门放外面的人进来。",
            "灾变", V3EffectDelta(militia = -2, garrisonMorale = 12, unrest = -10), V3EffectDelta(militia = -8, garrisonMorale = -18, unrest = 22), crisisLevel = 3,
            actLabel = "当场处置并安抚",
            actDesc = "擒住叛勇，当众斩首示众，再安抚其余乡勇，赏银稳军心。",
            waitLabel = "先关起来等天亮",
            waitDesc = "先把人关起来，等天亮了慢慢审，不要夜里杀人。",
            actSuccessText = "叛勇斩首，其余乡勇跪地请罪，赏银发下后无人再有二心。",
            waitSuccessText = "当夜叛勇的同伙劫牢，寨门被打开半边，匪冲入庄内。",
            actNote = "夜诛叛勇并赏安军心，叛乱平息，庄防重新稳固。",
            waitNote = "优柔寡断致叛勇同伙劫牢开寨，匪兵入庄，大祸临头。"),
        generatedCard("crisis_mutiny_03", V3CardPool.Crisis, "庄乱·分甲",
            "甲胄被各房私藏，乡勇不知该听谁的号令。大房说甲在他家库中，二房说甲是他房人捐的。",
            "灾变", V3EffectDelta(cohesion = 8, garrisonMorale = 10, unrest = -8), V3EffectDelta(cohesion = -12, garrisonMorale = -15), crisisLevel = 3,
            actLabel = "族长亲统武库",
            actDesc = "宣布所有甲胄刀枪归武库统管，由族长亲任统领，各房不得私藏。",
            waitLabel = "各房管各房",
            waitDesc = "甲是谁家买的就归谁管，打仗时再凑起来用。",
            actSuccessText = "甲胄收归武库统一调度，乡勇号令统一，操练有素，庄防整肃。",
            waitSuccessText = "各房拥甲自重，打仗时你等我我等你，无人肯先上。",
            actNote = "收甲胄归武库统一号令，乡勇号令严明，战力大增。",
            waitNote = "甲胄分属各房致号令不一，临阵无人效命，庄防崩溃。"),
        generatedCard("crisis_mutiny_04", V3CardPool.Crisis, "庄乱·外援",
            "山外有人愿来助守，但要先把一处田契交出去。他说帮忙不能白帮，田契是信物。",
            "灾变", V3EffectDelta(silver = -20, garrisonMorale = 15, bandits = -8), V3EffectDelta(garrisonMorale = -12, bandits = 12), crisisLevel = 3,
            actLabel = "签契借兵",
            actDesc = "签下田契借兵，庄守住了田将来能再买回来，庄破了什么都没了。",
            waitLabel = "不借外援自守",
            waitDesc = "田契是祖宗基业，岂能给外人？庄人自己守。",
            actSuccessText = "外援到了，合兵击退了山匪，虽出了些田但庄保住了。",
            waitSuccessText = "庄破了，匪杀入内宅，比丢田契惨百倍。",
            actNote = "以田契借外援击退山匪，庄得保全，日后可再赎回田产。",
            waitNote = "拒借外援致庄破，匪入内宅劫掠，损失远超田契。"),
        generatedCard("crisis_mutiny_05", V3CardPool.Crisis, "庄乱·最后一夜",
            "祖祠外的鼓声没有停，六处庄门都在等一个决定。山匪的火把已经在庄外三里了。",
            "灾变", V3EffectDelta(militia = -3, cohesion = 8, garrisonMorale = 15, unrest = -15), V3EffectDelta(militia = -12, cohesion = -15, garrisonMorale = -20), crisisLevel = 3,
            actLabel = "全庄死守祖祠",
            actDesc = "全庄人退守祖祠，族长亲自擂鼓，与庄共存亡。",
            waitLabel = "弃庄突围",
            waitDesc = "守不住了，保着族人趁夜突围，留得青山在。",
            actSuccessText = "鼓声中全庄死战，天快亮时山匪退了，祖祠还在，李氏还在。",
            waitSuccessText = "突围中死伤惨重，祖祠被焚，族谱烧了大半，几代人的基业毁于一旦。",
            actNote = "全庄死守祖祠击退山匪，祖祠保全，李氏根基犹在。",
            waitNote = "弃庄突围损失惨重，祖祠被焚，族谱散佚，数代基业毁于一旦。")
    )

    val completeMonthlyCards = monthlyCards + extendedCards + additionalMonthlyCards

    fun crisisCardsFor(state: V3GameState): List<V3MonthlyCard> {
        val stage = when {
            state.currentCrisisStage == "mutiny" -> 3
            state.currentCrisisStage == "unrest" -> 2
            state.currentCrisisStage == "grain_shortage" -> 1
            else -> 0
        }
        return additionalMonthlyCards.filter { card -> card.crisisLevel in 1..stage }
    }


    val initialPeople = listOf(
        V3Person(1, "李慎行", 24, "主房", "开族祖", V3Trait.Smooth, study = 26, martial = 22, commerce = 20, diplomacy = 24, loyalty = 90)
    )

    val spouseCandidates = listOf(
        V3SpouseCandidate("farmer", "王春娘", "邻村农户之女，能持家、会管田，适合稳住粮仓。", silverCost = 18, grainCost = 35, influenceReq = 0, studyBonus = 4, commerceBonus = 8, diplomacyBonus = 4, route = V3Route.Hermit),
        V3SpouseCandidate("merchant", "沈玉娘", "小商户之女，带来账本和货路，适合早期积银。", silverCost = 45, grainCost = 20, influenceReq = 8, commerceBonus = 16, diplomacyBonus = 6, route = V3Route.Merchant),
        V3SpouseCandidate("scholar", "陈婉仪", "寒门书香之后，擅识字教子，适合耕读路线。", silverCost = 35, grainCost = 25, influenceReq = 12, studyBonus = 16, diplomacyBonus = 7, route = V3Route.Scholar),
        V3SpouseCandidate("martial", "赵月英", "军户孤女，熟弓马与寨防，适合乱世自保。", silverCost = 30, grainCost = 30, influenceReq = 10, martialBonus = 16, diplomacyBonus = 3, route = V3Route.Fortress),
        V3SpouseCandidate("healer", "顾素问", "医家之女，识药理、善抚幼，能在灾疫年景稳住族人和乡里。", silverCost = 38, grainCost = 28, influenceReq = 14, studyBonus = 10, commerceBonus = 3, diplomacyBonus = 10, route = V3Route.Hermit),
        V3SpouseCandidate("gentry", "周明徽", "县中士绅旁支之女，熟礼法与人情，能替家族打开官绅门路。", silverCost = 72, grainCost = 35, influenceReq = 28, studyBonus = 12, commerceBonus = 4, diplomacyBonus = 16, route = V3Route.Loyalist),
        V3SpouseCandidate("sea", "林海棠", "闽商船主之女，懂海货、识风信，婚后可为远海路线积攒人脉。", silverCost = 88, grainCost = 30, influenceReq = 35, studyBonus = 4, commerceBonus = 18, diplomacyBonus = 12, route = V3Route.Overseas),
        V3SpouseCandidate("chieftain", "秦照雪", "山寨盟主遗女，性情果决，能聚拢乡勇，却也容易引来官府猜疑。", silverCost = 65, grainCost = 55, influenceReq = 42, martialBonus = 20, commerceBonus = 3, diplomacyBonus = 9, route = V3Route.Warlord),
        V3SpouseCandidate("scholar_male", "沈砚秋", "寒门书生，熟经义与乡约，婚后可替家族稳住书院与士绅关系。", silverCost = 42, grainCost = 22, influenceReq = 12, studyBonus = 16, diplomacyBonus = 10, route = V3Route.Scholar, gender = V3Gender.Male, avatarKey = "male_youth"),
        V3SpouseCandidate("merchant_male", "顾行舟", "河埠商户之子，善算账、通船路，适合把田庄和码头连成商路。", silverCost = 48, grainCost = 24, influenceReq = 10, commerceBonus = 17, diplomacyBonus = 8, route = V3Route.Merchant, gender = V3Gender.Male, avatarKey = "male_youth"),
        V3SpouseCandidate("martial_male", "赵长戈", "边地军户之后，熟弓马和营伍，可为家族补上军务骨干。", silverCost = 38, grainCost = 32, influenceReq = 14, martialBonus = 18, diplomacyBonus = 4, route = V3Route.Fortress, gender = V3Gender.Male, avatarKey = "male_youth"),
        V3SpouseCandidate("gentry_male", "周怀瑾", "士绅旁支子弟，知礼守约，能为女族人争取体面婚盟与官绅门路。", silverCost = 68, grainCost = 30, influenceReq = 28, studyBonus = 10, diplomacyBonus = 18, route = V3Route.Loyalist, gender = V3Gender.Male, avatarKey = "male_youth")    )

    val initialBranches = listOf(
        V3Branch("main", "主房", "李慎行", V3Route.Hermit, loyalty = 86, wealth = 18, influence = 20, grievance = 0, desc = "一人开族，尚无旁支。先成家、置产、育子，再谈宗族兴旺。")
    )

    val initialEstateAssets = listOf(
        V3EstateAsset("tenant_land", V3EstateType.TenantLand, level = 1, workers = 1, desc = "祖上传下的几亩佃田，能撑住早期口粮。")
    )

    val initialWorldRegions = listOf(
        V3WorldRegion("home_county", "清河县", 1, V3RegionStatus.Controlled, control = 42, enemyPower = 45, wealth = 40, desc = "家族起家的县域，先从这里控制税粮、宗族和团练。"),
        V3WorldRegion("neighbor_county", "临水县", 1, V3RegionStatus.Unknown, control = 0, enemyPower = 68, wealth = 52, desc = "清河邻县，田庄密集但豪强林立，是跨县经营的第一步。"),
        V3WorldRegion("river_prefecture", "三江府", 2, V3RegionStatus.Unknown, control = 0, enemyPower = 95, wealth = 85, desc = "水路商贸和粮仓所在，控制后商队收益大增。"),
        V3WorldRegion("mountain_prefecture", "黑松府", 2, V3RegionStatus.Unknown, control = 0, enemyPower = 120, wealth = 55, desc = "山寨、流寇和盐道盘踞之地，是军务扩张的试金石。"),
        V3WorldRegion("lake_province", "湖广粮区", 3, V3RegionStatus.Unknown, control = 0, enemyPower = 175, wealth = 145, desc = "湖广米粮汇聚之地，地方军头、粮商与流民势力相互角逐。"),
        V3WorldRegion("coast_province", "闽粤海门", 3, V3RegionStatus.Unknown, control = 0, enemyPower = 190, wealth = 175, desc = "海商、卫所与走私船队交错，是南洋路线的重要门户。"),
        V3WorldRegion("south_province", "南直隶", 3, V3RegionStatus.Unknown, control = 0, enemyPower = 210, wealth = 160, desc = "州府士绅与商帮交错，控制后家族从县族跃升一方豪强。"),
        V3WorldRegion("shandong_corridor", "山东运河", 3, V3RegionStatus.Unknown, control = 0, enemyPower = 235, wealth = 150, desc = "漕运与北上咽喉，控制此地可通京畿，也会直面军镇与饥军。"),
        V3WorldRegion("liaodong_front", "辽东军镇", 4, V3RegionStatus.Unknown, control = 0, enemyPower = 330, wealth = 130, desc = "边军、堡垒与难民汇聚，适合勤王立功，也可能陷入无底军需。"),
        V3WorldRegion("north_capital", "京畿", 4, V3RegionStatus.Unknown, control = 0, enemyPower = 360, wealth = 220, desc = "朝廷与禁军所在。若能入主京畿，造反路线进入天下争鼎。"),
        V3WorldRegion("jiangsea_gate", "江海门户", 4, V3RegionStatus.Unknown, control = 0, enemyPower = 295, wealth = 245, desc = "长江入海之门，船队、盐税和南迁族人都要从此经过。"),
        V3WorldRegion("all_realm", "天下", 5, V3RegionStatus.Unknown, control = 0, enemyPower = 520, wealth = 300, desc = "统一终局。需要足够人口、产业、军力、声望和已控制地域。")
    )

    val routePlans = listOf(
        V3RoutePlan(V3Route.Scholar, "以书院和科举重塑门第", listOf("书院等级", "读书人物", "士绅关系", "官府关系"), "至少两名核心族人学识过 80，并维持士绅关系 50+。", "宰辅世家"),
        V3RoutePlan(V3Route.Merchant, "控制集市、码头和商帮网络", listOf("银两", "商帮关系", "集市控制", "码头控制"), "商帮关系 60+ 且码头风险低于 25。", "海贸巨族"),
        V3RoutePlan(V3Route.Fortress, "筑寨屯粮，保境安民", listOf("寨堡等级", "乡勇", "粮食", "乡民关系"), "寨堡控制 70+，乡勇 120+。", "乱世堡主"),
        V3RoutePlan(V3Route.Loyalist, "依附朝廷，勤王立功", listOf("官府关系", "军镇关系", "声望", "战报事件"), "官府关系 70+，军镇关系 50+。", "忠烈勋族"),
        V3RoutePlan(V3Route.Warlord, "掌握乡勇和山道，形成地方武力", listOf("乡勇", "流寇关系", "寨堡", "县衙控制"), "乡勇 200+，县域多地点控制 70+。", "地方诸侯"),
        V3RoutePlan(V3Route.Overseas, "经营码头船队，远渡海外", listOf("码头", "商帮", "银两", "海贸事件"), "码头控制 80+，银两 2000+。", "南洋宗族"),
        V3RoutePlan(V3Route.Hermit, "避开乱世锋芒，保香火延续", listOf("低风险", "族内凝聚", "粮食", "低仇恨"), "县域总风险低于 150，凝聚 80+。", "山林遗族")
    )

    val eventSeeds = listOf(
        V3EventSeed("县令催粮", "官府关系低或粮食充足时出现", listOf("出粮助官", "买通书吏", "联合士绅议价", "拖延不办"), V3Route.Loyalist),
        V3EventSeed("流民入境", "灾荒或乡民关系低时出现", listOf("开仓赈济", "驱逐流民", "招为佃户", "交由县衙"), V3Route.Hermit),
        V3EventSeed("商帮求护", "集市或码头风险高时出现", listOf("派武支护商", "提高抽成", "拒绝牵连", "借机入股"), V3Route.Merchant),
        V3EventSeed("山道匪影", "流寇关系恶化或山道风险高时出现", listOf("刺探虚实", "筑寨防守", "招安头目", "上报官府"), V3Route.Fortress),
        V3EventSeed("房支争产", "商支财富过高或凝聚低时出现", listOf("按族规裁断", "让利安抚", "严惩私藏", "分家另立"), V3Route.Hermit)
    )

    val taskPlans = listOf(
        V3TaskPlan(V3TaskType.Govern, "学识/谋略", "提升地点控制、宗族凝聚和长期稳定", "收益慢，但能压住失控风险", V3Route.Hermit),
        V3TaskPlan(V3TaskType.Farm, "学识/经商", "增加粮食并稳定田庄", "灾荒期收益受影响", V3Route.Hermit),
        V3TaskPlan(V3TaskType.Trade, "经商", "增加银两和商帮关系", "可能抬高商支怨气与官府猜忌", V3Route.Merchant),
        V3TaskPlan(V3TaskType.Study, "学识", "推动科举、书院和士绅路线", "短期资源收益较少", V3Route.Scholar),
        V3TaskPlan(V3TaskType.Diplomacy, "谋略", "改善官府、士绅或地方势力关系", "花费银两，且可能卷入党争", V3Route.Loyalist),
        V3TaskPlan(V3TaskType.Relief, "学识/谋略", "提高民心和凝聚，缓解灾荒瘟疫", "消耗粮银明显", V3Route.Hermit),
        V3TaskPlan(V3TaskType.Fortify, "武艺", "降低寨堡与山道风险，推动自保路线", "会被官府猜忌", V3Route.Fortress),
        V3TaskPlan(V3TaskType.Scout, "武艺/谋略", "侦察山道、流寇、商路暗线", "失败会提高流寇敌意", V3Route.Warlord),
        V3TaskPlan(V3TaskType.Recruit, "武艺", "增加乡勇，强化军备", "持续消耗粮银", V3Route.Fortress)
    )

    val examQuestions = listOf(
        V3ExamQuestion(
            id = "county_1",
            stage = V3ExamStage.County,
            question = "明代地方最低一级正式行政单位通常由谁主政？",
            options = listOf("知县", "巡抚", "尚书", "都督"),
            answerIndex = 0,
            note = "县由知县主政，县衙关系会影响早期仕途。"
        ),
        V3ExamQuestion(
            id = "county_2",
            stage = V3ExamStage.County,
            question = "宗族经营中，最能稳定香火与族内秩序的根基是什么？",
            options = listOf("只扩乡勇", "修谱立规", "拒绝婚配", "弃田逐商"),
            answerIndex = 1,
            note = "宗祠、谱牒和族规是宗族长期稳定的核心。"
        ),
        V3ExamQuestion(
            id = "prefecture_1",
            stage = V3ExamStage.Prefecture,
            question = "明末辽饷、剿饷等加派容易首先压到哪类地方关系？",
            options = listOf("海外商路", "官府与乡民", "宗族祭祀", "儿童启蒙"),
            answerIndex = 1,
            note = "赋役压力会同时影响官府关系与乡民负担。"
        ),
        V3ExamQuestion(
            id = "prefecture_2",
            stage = V3ExamStage.Prefecture,
            question = "若族人走仕途，哪项属性最直接降低考试难度？",
            options = listOf("学识", "乡勇", "粮食", "风险"),
            answerIndex = 0,
            note = "学识越高，科举容错越高；谋略可作为辅助。"
        ),
        V3ExamQuestion(
            id = "provincial_1",
            stage = V3ExamStage.Provincial,
            question = "乱世中宗族由自保转向割据，最关键的硬实力通常是？",
            options = listOf("题库数量", "乡勇与据点", "菜单按钮", "童生头衔"),
            answerIndex = 1,
            note = "乡勇、寨堡、山道控制决定战斗和举旗成功率。"
        ),
        V3ExamQuestion(
            id = "provincial_2",
            stage = V3ExamStage.Provincial,
            question = "明代士人取得举人身份后，家族最可能获得什么提升？",
            options = listOf("士绅声望", "婴儿数量立刻翻倍", "所有风险清零", "无需粮食"),
            answerIndex = 0,
            note = "举人身份会显著提高士绅关系、族望和仕途路线。"
        ),
        V3ExamQuestion(
            id = "county_3",
            stage = V3ExamStage.County,
            question = "宗族遭遇灾荒时，哪种做法最有利于长期稳定？",
            options = listOf("尽数逐民", "核户赈济并留种粮", "立刻卖空粮仓", "停修族谱"),
            answerIndex = 1,
            note = "赈济需要留有余地，兼顾民心与来年生产。"
        ),
        V3ExamQuestion(
            id = "county_4",
            stage = V3ExamStage.County,
            question = "县中田契发生争议，最稳妥的处理方式通常是？",
            options = listOf("焚毁契书", "邀乡约士绅与官府丈量", "直接动兵", "弃田远走"),
            answerIndex = 1,
            note = "契书、乡约与官府丈量共同构成地方产权秩序。"
        ),
        V3ExamQuestion(
            id = "prefecture_3",
            stage = V3ExamStage.Prefecture,
            question = "明末商路受阻时，宗族经营最需要同时控制什么？",
            options = listOf("码头与山道风险", "族谱字体", "婴儿姓名", "按钮颜色"),
            answerIndex = 0,
            note = "码头、集市和山道共同决定货物流通与安全。"
        ),
        V3ExamQuestion(
            id = "prefecture_4",
            stage = V3ExamStage.Prefecture,
            question = "宗族房支怨气过高时，最可能造成什么后果？",
            options = listOf("月粮自动翻倍", "争产与拒绝出丁", "官府关系自动满值", "所有族人免疲劳"),
            answerIndex = 1,
            note = "房支怨气会引发争产、逼议和资源分配冲突。"
        ),
        V3ExamQuestion(
            id = "provincial_3",
            stage = V3ExamStage.Provincial,
            question = "地方豪族要从县域经营迈向跨府势力，首先需要什么？",
            options = listOf("稳定据点、财粮和可用人才", "只靠一次事件", "取消族规", "放弃全部产业"),
            answerIndex = 0,
            note = "跨府扩张需要据点控制、后勤和能够独当一面的族人。"
        ),
        V3ExamQuestion(
            id = "provincial_4",
            stage = V3ExamStage.Provincial,
            question = "面对甲申前后的天下大乱，哪项资源最能决定路线选择余地？",
            options = listOf("财粮、兵力、声望与凝聚", "单一人物年龄", "地图底色", "事件标题长度"),
            answerIndex = 0,
            note = "终局路线由多系统共同决定，不能只堆一种数值。"
        )
    )

    val initialAnnualGoals = listOf(
        V3AnnualGoal("marry", "先成一户", "娶妻成家，家族才有后续人口与传承。", V3GoalMetric.Population, 2, V3Route.Hermit, rewardCohesion = 3),
        V3AnnualGoal("build_2", "置下两产", "至少建成 2 处产业，形成稳定月入。", V3GoalMetric.BuiltSites, 2, V3Route.Merchant, rewardSilver = 18),
        V3AnnualGoal("child_3", "添丁进口", "让家族人口达到 3 人，宗族开始有后继。", V3GoalMetric.Population, 3, V3Route.Hermit, rewardGrain = 25),
        V3AnnualGoal("grain_260", "岁内蓄粮", "乱世先看粮仓，年底前把粮食储备推到 260。", V3GoalMetric.GrainStock, 260, V3Route.Hermit, rewardCohesion = 4),
        V3AnnualGoal("silver_220", "积银备变", "年底前把银两储备推到 220。", V3GoalMetric.SilverStock, 220, V3Route.Merchant, rewardSilver = 20),
        V3AnnualGoal("rank_2", "升为小族", "人口、产业与族望达标后晋升宗族品第。", V3GoalMetric.ClanRank, 2, V3Route.Hermit, rewardInfluence = 4),
        V3AnnualGoal("safe_3", "靖安三处", "至少让 3 个县域地点风险低于 30。", V3GoalMetric.SafeSites, 3, V3Route.Fortress, rewardInfluence = 4),
        V3AnnualGoal("control_4", "握住县域", "至少让 4 个地点控制达到 50。", V3GoalMetric.ControlledSites, 4, V3Route.Warlord, rewardInfluence = 5),
        V3AnnualGoal("cohesion_72", "合族同心", "把宗族凝聚提升到 72。", V3GoalMetric.Cohesion, 72, V3Route.Hermit, rewardGrain = 30),
        V3AnnualGoal("influence_48", "族望入县", "把族望声名提升到 48。", V3GoalMetric.Influence, 48, V3Route.Scholar, rewardInfluence = 3),
        V3AnnualGoal("estate_5", "家产成局", "把家产总等级提升到 5，形成田、铺、仓、团练的基本盘。", V3GoalMetric.EstateLevel, 5, V3Route.Merchant, rewardSilver = 35),
        V3AnnualGoal("region_2", "跨县经营", "控制至少 2 个地域，家族势力从本县走向府县。", V3GoalMetric.ControlledRegions, 2, V3Route.Warlord, rewardInfluence = 8),
        V3AnnualGoal("unify_30", "一方豪强", "统一进度达到 30，具备割据一方的基础。", V3GoalMetric.Unification, 30, V3Route.Warlord, rewardSilver = 80),
        V3AnnualGoal("militia_60", "团练初成", "兵册达到 60 人，能够应对县域流寇和护商。", V3GoalMetric.Militia, 60, V3Route.Fortress, rewardGrain = 45, rewardInfluence = 3),
        V3AnnualGoal("militia_140", "武备成营", "兵册达到 140 人，并为跨府征伐积攒兵力。", V3GoalMetric.Militia, 140, V3Route.Warlord, rewardSilver = 55, rewardInfluence = 5),
        V3AnnualGoal("population_6", "六口成族", "在族谱中维持至少 6 名在世族人。", V3GoalMetric.Population, 6, V3Route.Hermit, rewardGrain = 55, rewardCohesion = 5),
        V3AnnualGoal("rank_3", "望族门第", "晋升到望族品第，解锁高级兵种与跨域征伐。", V3GoalMetric.ClanRank, 3, V3Route.Scholar, rewardSilver = 45, rewardInfluence = 6),
        V3AnnualGoal("relations_180", "六方通达", "地方综合关系达到 180，在官绅民商军之间取得立足点。", V3GoalMetric.RelationTotal, 180, V3Route.Loyalist, rewardInfluence = 7),
        V3AnnualGoal("route_scholar_55", "书香成脉", "耕读路线达到 55，形成稳定士林影响。", V3GoalMetric.RouteScore, 55, V3Route.Scholar, rewardSilver = 30, rewardInfluence = 6),
        V3AnnualGoal("route_merchant_55", "商号成局", "重商路线达到 55，形成跨县货路。", V3GoalMetric.RouteScore, 55, V3Route.Merchant, rewardSilver = 70),
        V3AnnualGoal("route_fortress_55", "堡寨成盟", "自保路线达到 55，建立守望互助体系。", V3GoalMetric.RouteScore, 55, V3Route.Fortress, rewardGrain = 70, rewardCohesion = 4),
        V3AnnualGoal("region_4", "跨府立名", "控制至少 4 个地域，形成跨府影响。", V3GoalMetric.ControlledRegions, 4, V3Route.Warlord, rewardSilver = 100, rewardInfluence = 10),
        V3AnnualGoal("unify_60", "逐鹿天下", "统一进度达到 60，具备问鼎京畿的资格。", V3GoalMetric.Unification, 60, V3Route.Warlord, rewardSilver = 140, rewardInfluence = 12),
        V3AnnualGoal("population_12", "十二口分房", "维持至少12名在世族人，让宗族拥有多名可独当一面的成员。", V3GoalMetric.Population, 12, V3Route.Hermit, rewardGrain = 110, rewardCohesion = 6),
        V3AnnualGoal("estate_10", "六业并举", "把家产总等级提升到10，形成田、铺、仓、作坊、商队和团练体系。", V3GoalMetric.EstateLevel, 10, V3Route.Merchant, rewardSilver = 120, rewardInfluence = 6),
        V3AnnualGoal("relations_260", "县府同盟", "地方综合关系达到260，让官绅民商军都承认家族影响。", V3GoalMetric.RelationTotal, 260, V3Route.Loyalist, rewardSilver = 85, rewardInfluence = 10),
        V3AnnualGoal("safe_6", "六地靖安", "让至少6个县域地点风险低于30，建立稳固大后方。", V3GoalMetric.SafeSites, 6, V3Route.Fortress, rewardGrain = 120, rewardCohesion = 6),
        V3AnnualGoal("route_loyalist_80", "勤王成名", "勤王路线达到80，形成稳定官军网络。", V3GoalMetric.RouteScore, 80, V3Route.Loyalist, rewardInfluence = 12),
        V3AnnualGoal("route_overseas_80", "海路成脉", "海外路线达到80，形成远海退路。", V3GoalMetric.RouteScore, 80, V3Route.Overseas, rewardSilver = 150),
        V3AnnualGoal("route_hermit_80", "保族成约", "避祸路线达到80，以族约和粮仓保存香火。", V3GoalMetric.RouteScore, 80, V3Route.Hermit, rewardGrain = 140, rewardCohesion = 8),
        V3AnnualGoal("region_6", "一方之主", "控制至少6个地域，让县族成为跨省势力。", V3GoalMetric.ControlledRegions, 6, V3Route.Warlord, rewardSilver = 180, rewardInfluence = 14),
        V3AnnualGoal("unify_85", "天下在望", "统一进度达到85，为最终争鼎完成准备。", V3GoalMetric.Unification, 85, V3Route.Warlord, rewardSilver = 220, rewardInfluence = 15)
    )

    fun goalsFor(creed: String, crisis: String): List<V3AnnualGoal> {
        val creedGoal = when (creed) {
            "耕读传家" -> initialAnnualGoals.first { it.id == "influence_48" }
            "重商逐利" -> initialAnnualGoals.first { it.id == "silver_220" }
            "聚族自保" -> initialAnnualGoals.first { it.id == "safe_3" }
            "忠君报国" -> initialAnnualGoals.first { it.id == "control_4" }
            "开海远行" -> initialAnnualGoals.first { it.id == "silver_220" }
            "明哲保身" -> initialAnnualGoals.first { it.id == "cohesion_72" }
            else -> initialAnnualGoals.first { it.id == "cohesion_72" }
        }
        val crisisGoal = when (crisis) {
            "饥荒将至" -> initialAnnualGoals.first { it.id == "grain_260" }
            "流寇逼近" -> initialAnnualGoals.first { it.id == "safe_3" }
            "官府催税" -> initialAnnualGoals.first { it.id == "silver_220" }
            "族产争端" -> initialAnnualGoals.first { it.id == "cohesion_72" }
            "商路断绝" -> initialAnnualGoals.first { it.id == "build_2" }
            else -> initialAnnualGoals.first { it.id == "safe_3" }
        }
        return listOf(
            initialAnnualGoals.first { it.id == "marry" },
            initialAnnualGoals.first { it.id == "build_2" },
            creedGoal,
            crisisGoal
        ).distinctBy { it.id }.take(3)
    }

    val initialSites = listOf(
        V3CountySite(
            id = "shrine",
            name = "李氏宗祠",
            type = V3CountySiteType.Shrine,
            level = 1,
            control = 72,
            risk = 10,
            status = V3SiteStatus.Stable,
            desc = "祖屋旁一间小祠，先记名、立规、守住香火。",
            taskTypes = listOf(V3TaskType.Govern, V3TaskType.Diplomacy)
        ),
        V3CountySite(
            id = "farmland",
            name = "南乡田庄",
            type = V3CountySiteType.Farmland,
            level = 1,
            control = 46,
            risk = 24,
            status = V3SiteStatus.Strained,
            desc = "祖上留下的几亩薄田，早期主要靠它活命。",
            taskTypes = listOf(V3TaskType.Farm, V3TaskType.Relief)
        ),
        V3CountySite(
            id = "market",
            name = "西河集市",
            type = V3CountySiteType.Market,
            level = 0,
            control = 20,
            risk = 22,
            status = V3SiteStatus.Stable,
            desc = "摆摊开铺后的银两来源，需花银营建。",
            taskTypes = listOf(V3TaskType.Trade, V3TaskType.Scout)
        ),
        V3CountySite(
            id = "yamen",
            name = "清河县衙",
            type = V3CountySiteType.Yamen,
            level = 0,
            control = 12,
            risk = 42,
            status = V3SiteStatus.Threatened,
            desc = "税赋、徭役、官府关系和仕途入口。",
            taskTypes = listOf(V3TaskType.Diplomacy, V3TaskType.Govern)
        ),
        V3CountySite(
            id = "fort",
            name = "北山寨堡",
            type = V3CountySiteType.Fort,
            level = 0,
            control = 12,
            risk = 62,
            status = V3SiteStatus.Threatened,
            desc = "修筑后可抵御流寇，也会引发官府猜忌。",
            taskTypes = listOf(V3TaskType.Fortify, V3TaskType.Recruit, V3TaskType.Scout)
        ),
        V3CountySite(
            id = "dock",
            name = "三江码头",
            type = V3CountySiteType.Dock,
            level = 0,
            control = 18,
            risk = 38,
            status = V3SiteStatus.Strained,
            desc = "海贸和远渡海外路线的伏笔。",
            taskTypes = listOf(V3TaskType.Trade, V3TaskType.Scout)
        ),
        V3CountySite(
            id = "academy",
            name = "东林书院",
            type = V3CountySiteType.Academy,
            level = 0,
            control = 26,
            risk = 18,
            status = V3SiteStatus.Stable,
            desc = "士林讲会之所，推动耕读、士绅与仕途路线。",
            taskTypes = listOf(V3TaskType.Study, V3TaskType.Diplomacy)
        ),
        V3CountySite(
            id = "clinic",
            name = "仁心医馆",
            type = V3CountySiteType.Clinic,
            level = 0,
            control = 32,
            risk = 24,
            status = V3SiteStatus.Stable,
            desc = "瘟疫与伤病的缓冲点，也能提升乡民关系。",
            taskTypes = listOf(V3TaskType.Relief, V3TaskType.Govern)
        ),
        V3CountySite(
            id = "mountain_pass",
            name = "黑松山道",
            type = V3CountySiteType.MountainPass,
            level = 0,
            control = 9,
            risk = 71,
            status = V3SiteStatus.Threatened,
            desc = "流寇、私盐、商队和暗线情报都从这里经过。",
            taskTypes = listOf(V3TaskType.Scout, V3TaskType.Recruit, V3TaskType.Fortify)
        )
    )

    val initialRouteScores = mapOf(
        V3Route.Scholar to 15,
        V3Route.Merchant to 10,
        V3Route.Fortress to 8,
        V3Route.Loyalist to 6,
        V3Route.Warlord to 0,
        V3Route.Overseas to 0,
        V3Route.Hermit to 5
    )

    fun startProfile(
        root: String,
        county: String,
        creed: String,
        crisis: String
    ): V3StartProfile {
        var silver = when (root) {
            "寒门佃户" -> 58
            "没落士族" -> 76
            "边地军户" -> 68
            "江南商族" -> 110
            "山中堡寨" -> 74
            else -> 70
        }
        var grain = when (root) {
            "寒门佃户" -> 120
            "边地军户" -> 100
            "江南商族" -> 80
            "山中堡寨" -> 110
            else -> 95
        }
        var influence = when (root) {
            "没落士族" -> 14
            "江南商族" -> 10
            "边地军户" -> 8
            else -> 6
        }
        var cohesion = 62
        var militia =
            if (root == "边地军户" || root == "山中堡寨") 12 else 3
        var relations = V3Relations()
        var rebelHeat = 0
        val originTraits = mutableListOf<String>()
        val routeBonuses = mutableListOf<Pair<V3Route, Int>>()

        when (root) {
            "寒门佃户" -> {
                cohesion += 8
                relations = relations.copy(villagers = 12)
                originTraits += "同乡相护：粮荒时流民转化较慢"
            }
            "没落士族" -> {
                influence += 12
                relations = relations.copy(gentry = 14, yamen = 4)
                originTraits += "旧谱余荫：族望与士绅初始关系更高"
            }
            "边地军户" -> {
                militia += 10
                relations = relations.copy(garrison = 16)
                originTraits += "边堡军籍：守庄战初始驻守更强"
            }
            "江南商族" -> {
                silver += 24
                relations = relations.copy(merchants = 18)
                originTraits += "账房传家：商旅卡更容易带来银两"
            }
            "山中堡寨" -> {
                militia += 8
                rebelHeat += 8
                relations = relations.copy(bandits = 8, garrison = 5)
                originTraits += "山寨旧盟：山贼关系较好，但官府猜忌更深"
            }
            "海商遗族" -> {
                silver += 18
                relations = relations.copy(merchants = 15, yamen = -4)
                routeBonuses += V3Route.Overseas to 8
                originTraits += "海路遗契：海外路线与码头经营起步更快"
            }
        }

        val creedRoute = when (creed) {
            "耕读传家" -> V3Route.Scholar
            "重商逐利" -> V3Route.Merchant
            "聚族自保" -> V3Route.Fortress
            "忠君报国" -> V3Route.Loyalist
            "开海远行" -> V3Route.Overseas
            "明哲保身" -> V3Route.Hermit
            else -> V3Route.Hermit
        }
        routeBonuses += creedRoute to 12

        val countyEffect = when (county) {
            "江南水乡" -> {
                silver += 8
                grain += 15
                relations = relations.copy(merchants = relations.merchants + 8)
                routeBonuses += V3Route.Merchant to 6
                "银两 +8 · 粮食 +15 · 商帮关系 +8 · 富商路线 +6"
            }
            "中原灾地" -> {
                grain -= 20
                influence += 4
                relations = relations.copy(villagers = relations.villagers + 10)
                routeBonuses += V3Route.Hermit to 6
                "粮食 -20 · 族望 +4 · 乡民关系 +10 · 避祸路线 +6"
            }
            "西北边堡" -> {
                militia += 5
                rebelHeat += 5
                relations = relations.copy(garrison = relations.garrison + 8)
                routeBonuses += V3Route.Fortress to 6
                "乡勇 +5 · 军镇关系 +8 · 流寇热度 +5 · 自保路线 +6"
            }
            "湖广粮仓" -> {
                grain += 35
                relations = relations.copy(villagers = relations.villagers + 6)
                routeBonuses += V3Route.Merchant to 6
                "粮食 +35 · 乡民关系 +6 · 富商路线 +6"
            }
            "闽粤海路" -> {
                silver += 20
                relations = relations.copy(merchants = relations.merchants + 10)
                routeBonuses += V3Route.Overseas to 6
                "银两 +20 · 商帮关系 +10 · 海外路线 +6"
            }
            else -> {
                militia += 7
                rebelHeat += 8
                relations = relations.copy(garrison = relations.garrison + 10)
                routeBonuses += V3Route.Loyalist to 6
                "乡勇 +7 · 军镇关系 +10 · 流寇热度 +8 · 勤王路线 +6"
            }
        }

        val crisisEffect = when (crisis) {
            "饥荒将至" -> {
                grain -= 25
                cohesion -= 4
                "粮食 -25 · 凝聚 -4 · 年度目标偏向蓄粮"
            }
            "流寇逼近" -> {
                rebelHeat += 15
                militia += 3
                "流寇热度 +15 · 乡勇 +3 · 年度目标偏向治安"
            }
            "官府催税" -> {
                silver -= 20
                relations = relations.copy(yamen = relations.yamen - 10)
                "银两 -20 · 官府关系 -10 · 年度目标偏向积银"
            }
            "族产争端" -> {
                cohesion -= 12
                influence += 3
                "凝聚 -12 · 族望 +3 · 年度目标偏向和族"
            }
            "商路断绝" -> {
                silver -= 18
                relations = relations.copy(merchants = relations.merchants - 12)
                "银两 -18 · 商帮关系 -12 · 年度目标偏向重开商路"
            }
            else -> {
                grain -= 12
                cohesion -= 6
                relations = relations.copy(villagers = relations.villagers - 6)
                "粮食 -12 · 凝聚 -6 · 乡民关系 -6 · 疫病风险持续"
            }
        }

        val routeScores = routeBonuses.fold(initialRouteScores) {
                scores,
                (route, amount) ->
            scores + (route to ((scores[route] ?: 0) + amount))
        }
        return V3StartProfile(
            silver = silver.coerceAtLeast(0),
            grain = grain.coerceAtLeast(0),
            influence = influence.coerceAtLeast(0),
            cohesion = cohesion.coerceIn(0, 100),
            militia = militia.coerceAtLeast(0),
            relations = relations,
            rebelHeat = rebelHeat,
            routeScores = routeScores,
            routeBonuses = routeBonuses,
            annualGoals = goalsFor(creed, crisis),
            originTraits = originTraits,
            countyEffect = countyEffect,
            crisisEffect = crisisEffect
        )
    }

    fun sanitizeSurname(input: String): String =
        input
            .trim()
            .filter { it in '\u3400'..'\u9FFF' }
            .take(2)
            .ifBlank { "李" }

    private val founderGivenNames = listOf(
        "慎行", "守正", "承安", "景和", "怀义", "允文",
        "敬修", "知远", "伯谦", "维桢", "弘毅", "明德"
    )

    fun founderGivenName(root: String, county: String, creed: String): String {
        val seed = root.sumOf { it.code } + county.sumOf { it.code } + creed.sumOf { it.code }
        return founderGivenNames[seed % founderGivenNames.size]
    }

    private val blockedNameTerms = setOf(
        "毛泽东", "周恩来", "刘少奇", "朱德", "邓小平", "江泽民", "胡锦涛", "习近平",
        "孙中山", "蒋介石", "袁世凯", "李大钊", "陈独秀", "鲁迅", "雷锋",
        "岳飞", "文天祥", "戚继光", "郑成功", "关羽", "诸葛亮"
    )

    fun isBlockedName(input: String): Boolean = blockedNameTerms.any { term -> input.contains(term) }

    fun sanitizeFounderGivenName(input: String): String =
        input.trim().filter { it in '\u3400'..'\u9FFF' }.take(4).ifBlank { "慎行" }

    fun founderName(surnameInput: String, givenNameInput: String): String =
        "${sanitizeSurname(surnameInput)}${sanitizeFounderGivenName(givenNameInput)}"

    fun founderName(surnameInput: String, root: String, county: String, creed: String): String =
        founderName(surnameInput, founderGivenName(root, county, creed))

    fun clanName(surnameInput: String): String = "${sanitizeSurname(surnameInput)}氏宗族"

    fun newGame(root: String, county: String, creed: String, crisis: String, surnameInput: String = "李", givenNameInput: String? = null): V3GameState {
        val surname = sanitizeSurname(surnameInput)
        val cleanedClanName = clanName(surname)
        val founderName = founderName(surname, givenNameInput ?: founderGivenName(root, county, creed))
        val base = V3GameState(root = root, county = county, creed = creed, crisis = crisis)
        val profile = startProfile(root, county, creed, crisis)
        return base.copy(
            surname = surname,
            founderName = founderName,
            clanName = cleanedClanName,
            silver = profile.silver,
            grain = profile.grain,
            influence = profile.influence,
            cohesion = profile.cohesion,
            originTraits = profile.originTraits,
            militia = profile.militia,
            army = V3ArmyRoster(militia = profile.militia),
            relations = profile.relations,
            rebelHeat = profile.rebelHeat,
            people = initialPeople.map { if (it.id == 1) it.copy(name = founderName, ageMonths = it.age * 12, surname = surname) else it },
            branches = initialBranches.map { if (it.id == "main") it.copy(leaderName = founderName, desc = "一人开族，尚无旁支。先成家、置产、育子，再谈宗族兴旺。") else it },
            sites = initialSites.map { if (it.id == "shrine") it.copy(name = "${surname}氏宗祠") else it },
            estateAssets = initialEstateAssets.map { it.copy() },
            worldRegions = initialWorldRegions.map { it.copy() },
            annualGoals = profile.annualGoals,
            routeScores = profile.routeScores,
            patriarch = V3Patriarch(
                personId = 1,
                name = founderName,
                conduct = (28 + if (root == "没落士族" || root == "寒门佃户") 8 else 2).coerceIn(0, 100),
                stewardship = (28 + if (root == "江南商族" || root == "海商遗族") 10 else 3).coerceIn(0, 100),
                prestige = (22 + if (root == "没落士族") 10 else if (creed == "耕读传家") 6 else 2).coerceIn(0, 100),
                health = (55 + if (root == "边地军户" || root == "山中堡寨") 8 else 0).coerceIn(0, 100)
            ),
            cardBudget = if (profile.influence >= 45) 4 else 3,
            tutorialVersion = V3_TUTORIAL_VERSION,
            tutorialStep = 0,
            tutorialCompleted = false,
            pendingReports = listOf("${county}局势未稳，${crisis}已成眼前第一患。"),
            eventLog = listOf("${root}立于${county}，奉行【${creed}】，却遭【${crisis}】。")
        )
    }
}
