package com.daming.fushengzhi2.data

object GameContent {
    val surnames = listOf("李", "王", "张", "刘", "陈", "赵", "朱", "杨", "黄", "周")

    val origins = listOf(
        Origin("farmer", "寒门农户", "资源少、无负债，白手起家", 30, 80, 20, 5),
        Origin("landlord", "小地主", "有田产积蓄，但树大招风", 120, 200, 60, 20),
        Origin("military", "退役军户", "族长有武艺，结寨防御加成", 60, 120, 30, 15)
    )

    val regions = listOf(
        Region("shaanbei", "陕北", "流寇频繁、天灾多、民风彪悍", 0.30, 0.25, 0.10),
        Region("henan", "河南", "四战之地、灾荒严重", 0.25, 0.20, 0.12),
        Region("jiangnan", "江南", "富庶但赋税重、后期清军压力", 0.10, 0.08, 0.20),
        Region("huguang", "湖广", "鱼米之乡、但张献忠之乱将至", 0.12, 0.15, 0.12)
    )

    val difficulties = listOf(
        Difficulty("easy", "太平盛世", "灾害概率-50%，税率-30%", 0.5, 0.7),
        Difficulty("normal", "风雨飘摇", "标准难度", 1.0, 1.0),
        Difficulty("hard", "末世浩劫", "灾害概率+30%，税率+20%", 1.3, 1.2)
    )

    val clanRanks = listOf("寒门", "农户", "乡绅", "望族", "世家", "勋贵", "名门", "豪阀", "国柱")

    val rankRequirements = mapOf(
        2 to RankRequirement(silver = 500, fame = 300, grain = 300, population = 6),
        3 to RankRequirement(silver = 1500, fame = 800, grain = 600, cloth = 100, population = 10),
        4 to RankRequirement(silver = 4000, fame = 2000, grain = 1200, cloth = 300, population = 15),
        5 to RankRequirement(silver = 10000, fame = 5000, grain = 3000, cloth = 800, population = 22),
        6 to RankRequirement(silver = 25000, fame = 12000, grain = 6000, cloth = 2000, population = 30),
        7 to RankRequirement(silver = 60000, fame = 30000, grain = 15000, cloth = 5000, population = 40),
        8 to RankRequirement(silver = 150000, fame = 80000, grain = 40000, cloth = 12000, population = 55),
        9 to RankRequirement(silver = 400000, fame = 200000, grain = 100000, cloth = 30000, population = 75)
    )

    val industryLimitByRank = mapOf(1 to 4, 2 to 6, 3 to 9, 4 to 12, 5 to 16, 6 to 20, 7 to 25, 8 to 30, 9 to 40)
    val maxRulesByRank = mapOf(1 to 0, 2 to 2, 3 to 2, 4 to 3, 5 to 3, 6 to 3, 7 to 4, 8 to 4, 9 to 5)

    val talents = listOf(
        Talent("smart", "天资聪慧", "学识+30%", studyBonus = 0.3),
        Talent("merchant", "经商能手", "经商收益+25%", tradeBonus = 0.25),
        Talent("martial", "武艺高强", "从军存活率+20%", militaryBonus = 0.2),
        Talent("weak", "体弱多病", "生病概率+15%", sickRate = 0.15),
        Talent("lazy", "好吃懒做", "产出-20%", outputBonus = -0.2),
        Talent("diligent", "勤劳刻苦", "产出+15%", outputBonus = 0.15),
        Talent("charisma", "容貌出众", "联姻加成+20%", marriageBonus = 0.2),
        Talent("fertile", "多子多福", "生育率+25%", fertilityBonus = 0.25)
    )

    val clanRules = listOf(
        ClanRule("store_grain", "储粮备荒", "粮食消耗-20%，但产出也-10%", "仓", RuleEffects(grainConsumeMul = -0.2, grainOutputMul = -0.1)),
        ClanRule("martial_train", "习武自卫", "族人武艺成长+50%，流寇抵抗+20%", "武", RuleEffects(martialGrowthMul = 0.5, banditResist = 0.2)),
        ClanRule("frugal", "节衣缩食", "布匹消耗-40%，但声望每月-1", "俭", RuleEffects(clothConsumeMul = -0.4, fameDrain = -1)),
        ClanRule("study_first", "耕读传家", "学识成长+30%，但需额外粮食供给", "读", RuleEffects(studyGrowthMul = 0.3, grainConsumeMul = 0.1)),
        ClanRule("merchant_focus", "重商兴族", "经商收益+25%，但声望-2/月", "商", RuleEffects(tradeIncomeMul = 0.25, fameDrain = -2)),
        ClanRule("fortify", "全民筑寨", "筑寨费用-30%，但产业产出-10%", "堡", RuleEffects(fortCostMul = -0.3, allOutputMul = -0.1))
    )

    val familyMottos = listOf(
        FamilyMotto("study", "诗书传家", "书", "全族学识成长+20%，科举通过率+10%", RuleEffects(studyGrowthMul = 0.2, examPassBonus = 0.1)),
        FamilyMotto("martial", "武德充沛", "武", "全族武艺成长+25%，从军存活率+15%", RuleEffects(martialGrowthMul = 0.25, militarySurvival = 0.15)),
        FamilyMotto("trade", "货殖兴家", "商", "经商收益+20%，初始银两+30", RuleEffects(tradeIncomeMul = 0.2, initSilverBonus = 30)),
        FamilyMotto("farm", "耕读为本", "田", "粮食产出+20%，消耗-10%", RuleEffects(grainOutputMul = 0.2, grainConsumeMul = -0.1)),
        FamilyMotto("fortify", "聚族自保", "堡", "流寇抵抗+30%，筑寨费用-20%", RuleEffects(banditResist = 0.3, fortCostMul = -0.2))
    )

    val industryTypes = listOf(
        IndustryType("dry_field", "旱田", ResourceKind.Grain, 8, 150, "产粮食，受天灾影响大", 1, evolvesTo = "paddy_field"),
        IndustryType("hemp_field", "麻田", ResourceKind.Cloth, 3, 120, "种麻织布，前期布匹来源", 1, evolvesTo = "workshop"),
        IndustryType("paddy_field", "水田", ResourceKind.Grain, 14, 300, "产量高，需更多人手", 2, evolvesTo = "fertile_field"),
        IndustryType("fish_pond", "鱼塘", ResourceKind.Grain, 10, 240, "四季有鱼，产出稳定不受天灾影响", 2, specialEffect = "weather_immune"),
        IndustryType("livestock", "畜栏", ResourceKind.Grain, 6, 200, "养猪牧羊，产粮食兼出少量布匹", 2, resource2 = ResourceKind.Cloth, baseOutput2 = 3),
        IndustryType("handicraft", "手工作坊", ResourceKind.Silver, 4, 260, "编篮制陶，前期银两来源", 2),
        IndustryType("shop", "商铺", ResourceKind.Silver, 10, 450, "产银两，乱世收益波动", 3, evolvesTo = "trade_house"),
        IndustryType("tea_garden", "茶园", ResourceKind.Silver, 8, 380, "茶叶远销，兼得薄名", 3, resource2 = ResourceKind.Fame, baseOutput2 = 2),
        IndustryType("inn", "客栈", ResourceKind.Silver, 7, 340, "南来北往，打探消息兼赚银两", 3),
        IndustryType("herb_shop", "药铺", ResourceKind.Silver, 5, 420, "济世救人，全族生病概率-15%", 3, specialEffect = "reduce_sick_15"),
        IndustryType("workshop", "织坊", ResourceKind.Cloth, 6, 280, "产布匹，稳定收益", 4, evolvesTo = "silk_house"),
        IndustryType("fort", "寨堡", ResourceKind.None, 0, 800, "降低流寇劫掠损失", 4),
        IndustryType("brewery", "酒坊", ResourceKind.Silver, 12, 500, "以粮酿酒，利润丰厚但每月额外消耗粮食3", 4, specialEffect = "consume_grain_3"),
        IndustryType("smithy", "铁匠铺", ResourceKind.Silver, 6, 480, "打造兵器农具，兼得名望", 4, resource2 = ResourceKind.Fame, baseOutput2 = 3, specialEffect = "martial_grow_10"),
        IndustryType("horse_ranch", "马场", ResourceKind.Fame, 4, 560, "饲养良驹，从军族人战力提升", 5, specialEffect = "military_boost_15"),
        IndustryType("escort", "镖局", ResourceKind.Silver, 18, 700, "押镖走货，高收益但有折损风险", 5, specialEffect = "risk_loss_20", evolvesTo = "canal_wharf"),
        IndustryType("bookshop", "书坊", ResourceKind.Fame, 6, 650, "刊印经书，声名远扬兼赚银两", 5, resource2 = ResourceKind.Silver, baseOutput2 = 5, specialEffect = "study_grow_10"),
        IndustryType("salt_field", "盐场", ResourceKind.Silver, 25, 1000, "贩盐暴利，但官府稽查严厉", 5, specialEffect = "risk_tax_30", evolvesTo = "customs_house"),
        IndustryType("money_house", "钱庄", ResourceKind.Silver, 20, 850, "放贷收息，银两按总资产额外生利", 6, specialEffect = "interest_2pct", evolvesTo = "piaohao"),
        IndustryType("fleet", "船队", ResourceKind.Silver, 32, 1400, "海上贸易，利润极高但受季风影响", 6, specialEffect = "season_amplify"),
        IndustryType("estate", "庄园", ResourceKind.Grain, 10, 1200, "粮银名三收", 6, resource2 = ResourceKind.Silver, baseOutput2 = 8, resource3 = ResourceKind.Fame, baseOutput3 = 3, evolvesTo = "grand_farmland"),
        IndustryType("pawnshop", "当铺", ResourceKind.Silver, 26, 1600, "典当质押，乱世暴利", 7, specialEffect = "interest_1pct", evolvesTo = "piaohao"),
        IndustryType("dye_house", "染坊", ResourceKind.Cloth, 12, 1300, "染制锦缎彩绸，布匹银两双收", 7, resource2 = ResourceKind.Silver, baseOutput2 = 12),
        IndustryType("private_school", "私塾", ResourceKind.Fame, 10, 1100, "开馆授徒，声名远播", 7, resource2 = ResourceKind.Silver, baseOutput2 = 8, specialEffect = "study_grow_10"),
        IndustryType("canal_wharf", "漕运码头", ResourceKind.Silver, 38, 1800, "扼守漕运要道，南北货物中转抽成", 7, specialEffect = "trade_hub"),
        IndustryType("arsenal", "军械坊", ResourceKind.Silver, 30, 2200, "铸造兵器甲胄，从军族人战力提升", 8, resource2 = ResourceKind.Fame, baseOutput2 = 8, specialEffect = "military_boost_25"),
        IndustryType("weaving_bureau", "织造局", ResourceKind.Cloth, 20, 2000, "承接官府织造，布匹银两声望三收", 8, resource2 = ResourceKind.Silver, baseOutput2 = 22, resource3 = ResourceKind.Fame, baseOutput3 = 6),
        IndustryType("imperial_merchant", "皇商行", ResourceKind.Silver, 60, 4000, "皇家特许经营，暴利但消耗声望", 9, specialEffect = "consume_fame_3"),
        IndustryType("customs_house", "海关行", ResourceKind.Silver, 50, 3500, "把持海关贸易，银两兼得声望", 9, resource2 = ResourceKind.Fame, baseOutput2 = 10),
        IndustryType("grand_farmland", "万亩良田", ResourceKind.Grain, 45, 3000, "终极农业，粮银双收且不受天灾", 9, resource2 = ResourceKind.Silver, baseOutput2 = 20, specialEffect = "weather_immune"),
        IndustryType("fertile_field", "良田", ResourceKind.Grain, 22, 500, "精耕良田，产量极高", 4, evolved = true),
        IndustryType("trade_house", "商号", ResourceKind.Silver, 18, 600, "连锁商号，日进斗金", 5, resource2 = ResourceKind.Fame, baseOutput2 = 3, evolved = true),
        IndustryType("silk_house", "绸缎庄", ResourceKind.Cloth, 12, 500, "丝绸锦缎，布匹兼赚银两", 5, resource2 = ResourceKind.Silver, baseOutput2 = 5, evolved = true),
        IndustryType("piaohao", "票号", ResourceKind.Silver, 35, 1200, "汇通天下，跨省银票兑付", 8, specialEffect = "interest_2pct", evolved = true)
    )

    val industryEvolutions = listOf(
        IndustryEvolution("dry_field", "paddy_field", 3, 2, 240, "旱田改水利，升级为水田"),
        IndustryEvolution("hemp_field", "workshop", 3, 4, 320, "添置织机，升级为织坊"),
        IndustryEvolution("paddy_field", "fertile_field", 3, 4, 480, "精耕细作，升级为良田"),
        IndustryEvolution("shop", "trade_house", 3, 5, 600, "扩大经营，升级为商号"),
        IndustryEvolution("workshop", "silk_house", 3, 5, 550, "改织丝绸，升级为绸缎庄"),
        IndustryEvolution("money_house", "piaohao", 3, 8, 1200, "扩张网络，升级为票号"),
        IndustryEvolution("pawnshop", "piaohao", 3, 8, 1000, "典当升级，升级为票号"),
        IndustryEvolution("escort", "canal_wharf", 3, 7, 900, "镖路转漕运，升级为漕运码头"),
        IndustryEvolution("salt_field", "customs_house", 3, 9, 2000, "盐政转关务，升级为海关行"),
        IndustryEvolution("estate", "grand_farmland", 3, 9, 1800, "良田扩展，升级为万亩良田")
    )

    val itemTypes = listOf(
        ItemType("herb", "上等药材", "药", "common", "可治疗族人伤病", 80, "heal"),
        ItemType("book", "经史典籍", "典", "uncommon", "使用后学识+3", 120, "study_boost"),
        ItemType("weapon", "精钢兵器", "兵", "uncommon", "使用后武艺+3", 150, "martial_boost"),
        ItemType("jade", "玉石珍玩", "玉", "rare", "可变卖换取银两", 250, "sell"),
        ItemType("scroll", "兵法残卷", "卷", "rare", "使用后武艺+5", 200, "martial_boost_large"),
        ItemType("seal", "官府印信", "印", "epic", "声望+15", 300, "fame_boost"),
        ItemType("heirloom", "传家宝", "宝", "epic", "声望+20", 400, "morale_boost")
    )

    val academyTypes = listOf(
        AcademyType("school", "族学", "学", "study", "提升族人学识成长速度", 2, 250, 0.5, 2),
        AcademyType("wuguan", "武馆", "武", "martial", "提升族人武艺成长速度", 2, 300, 0.6, 3)
    )

    val expeditionTypes = listOf(
        ExpeditionType("market", "赶集贸易", "集", "去集市贸易，获取银两和布匹", 1, 40, 40, 3, rewardSilver = 60..150, rewardCloth = 10..30, riskRate = 0.05, riskDesc = "遭遇劫匪"),
        ExpeditionType("study_trip", "游学求教", "游", "拜访名士，增长学识见闻", 2, 80, 60, 3, rewardStudy = 3..8, rewardFame = 3..8, riskRate = 0.05, riskDesc = "水土不服"),
        ExpeditionType("explore", "深山探秘", "探", "进山探寻宝物，可能有意外收获", 2, 60, 80, 4, rewardSilver = 30..250, itemChance = 0.3, riskRate = 0.20, riskDesc = "遭遇猛兽"),
        ExpeditionType("recruit", "招贤纳士", "招", "四处访求能人异士加入宗族", 2, 150, 80, 4, rewardFame = 3..10, recruitChance = 0.4, riskRate = 0.08, riskDesc = "遇到骗子"),
        ExpeditionType("trade_route", "行商远途", "商", "沿商路远行贸易，收益丰厚但风险也大", 3, 120, 80, 5, rewardSilver = 200..500, rewardFame = 3..8, riskRate = 0.15, riskDesc = "路遇山贼"),
        ExpeditionType("sea_trade", "海上通商", "帆", "扬帆出海，与番邦贸易", 4, 300, 150, 6, rewardSilver = 500..1200, rewardFame = 8..20, riskRate = 0.25, riskDesc = "遭遇海寇"),
        ExpeditionType("court_visit", "进京面圣", "朝", "上京朝觐，结交朝中权贵", 3, 500, 200, 6, rewardSilver = 100..300, rewardFame = 20..50, riskRate = 0.10, riskDesc = "卷入党争"),
        ExpeditionType("border_patrol", "巡边戍守", "戍", "率族中精锐巡视边关，军功换声望", 3, 200, 200, 7, rewardFame = 15..40, rewardMartial = 3..8, riskRate = 0.18, riskDesc = "遭遇敌袭"),
        ExpeditionType("treasure_hunt", "寻访古迹", "古", "探访前朝遗迹，寻觅传世珍宝", 3, 250, 120, 7, rewardSilver = 100..800, rewardFame = 5..15, itemChance = 0.5, riskRate = 0.20, riskDesc = "机关陷阱"),
        ExpeditionType("silk_road", "丝路远征", "驼", "远赴西域，带回奇珍异宝", 6, 800, 400, 8, rewardSilver = 1000..3000, rewardFame = 20..50, itemChance = 0.6, riskRate = 0.30, riskDesc = "沙匪劫掠"),
        ExpeditionType("tributary_mission", "万邦来朝", "使", "代天子出使四方，宣扬国威", 5, 1500, 600, 9, rewardSilver = 800..2000, rewardFame = 50..120, riskRate = 0.15, riskDesc = "外邦刁难")
    )

    val examLevels = listOf(
        ExamLevel("tongshi", "童试", 30, 0.40, "童生", 5),
        ExamLevel("xiangshi", "乡试", 60, 0.25, "秀才", 15),
        ExamLevel("huishi", "会试", 85, 0.12, "举人", 30),
        ExamLevel("dianshi", "殿试", 95, 0.05, "进士", 60)
    )

    val militaryRanks = listOf(
        MilitaryRankDef("soldier", "士兵", 0.03, 3, 2),
        MilitaryRankDef("bazong", "把总", 0.015, 8, 8),
        MilitaryRankDef("shoubei", "守备", 0.008, 15, 15)
    )

    val officialRanks = listOf(
        OfficialRankDef("zhixian", "知县", "进士", 20, 15, 0.10, 0.03, desc = "治理一县，月俸20两，声望+15，税赋-10%"),
        OfficialRankDef("zhifu", "知府", "知县", 40, 25, 0.20, 0.03, desc = "统辖一府，月俸40两，声望+25，税赋-20%"),
        OfficialRankDef("buzhengshi", "布政使", "知府", 80, 40, 0.30, 0.03, famePerMonth = 5, desc = "掌管一省，月俸80两，声望+40，税赋-30%")
    )

    val marriageTiers = listOf(
        MarriageTier("common", "普通人家", 80, 0, 0, 2, "聘礼少，无特殊加成", unlockRank = 1),
        MarriageTier("scholar", "书香门第", 200, 80, 15, 8, "配偶学识+20，子女聪慧概率+15%", "study", 20, 3),
        MarriageTier("official", "官宦世家", 450, 150, 30, 15, "声望大增，官场人脉+1", "fame", 10, 4),
        MarriageTier("military", "军户世家", 180, 100, 10, 5, "配偶武艺+25，从军存活率+10%", "martial", 25, 5),
        MarriageTier("noble", "名门望族", 800, 300, 60, 25, "配偶学识+15武艺+15，声望大增", "all", 15, 6),
        MarriageTier("royal_kin", "皇亲国戚", 1500, 500, 120, 40, "皇室姻亲，声望飞涨；配偶全属性+20", "all", 20, 7),
        MarriageTier("warlord", "藩镇将门", 1200, 600, 80, 30, "配偶武艺+35，赠送精兵50", "martial", 35, 8),
        MarriageTier("prime_minister", "宰辅门第", 2500, 800, 200, 60, "宰相之家联姻，全族声望月产+5", "all", 25, 9)
    )

    val laborJobs = listOf(
        LaborJob("coolie", "帮工", 1, 5, "码头搬运，卖力气换铜板"),
        LaborJob("farmhand", "佃农", 1, 6, "替人耕种，辛苦度日"),
        LaborJob("peddler", "货郎", 2, 8, "走街串巷，贩卖杂货"),
        LaborJob("craftsman", "匠人", 2, 9, "手艺谋生，略有余钱"),
        LaborJob("clerk", "账房", 3, 11, "商号记账，薪俸稳定"),
        LaborJob("foreman", "工头", 3, 12, "管人管事，待遇尚可"),
        LaborJob("steward", "掌柜", 4, 14, "店铺坐堂，收入丰厚"),
        LaborJob("tutor", "西席", 4, 16, "设帐授徒，束脩不菲"),
        LaborJob("broker", "牙行经纪", 5, 20, "撮合交易，佣金丰厚"),
        LaborJob("physician", "坐堂郎中", 5, 18, "悬壶济世，诊金可观"),
        LaborJob("magistrate_aide", "师爷", 6, 24, "幕府参谋，俸禄优厚"),
        LaborJob("caravan_lead", "商队领队", 7, 30, "率领商队远行，收入丰厚"),
        LaborJob("mine_overseer", "矿监", 8, 37, "监管矿山开采，收入极高"),
        LaborJob("tax_collector", "税使", 9, 47, "代征赋税，权势滔天")
    )

    val marketCommodities = listOf(
        MarketCommodity("grain", "粮食", "粮", "石", 8, 10, 0.40, "民以食为天，灾年涨价、丰收跌价", resourceKey = "grain"),
        MarketCommodity("cloth", "布匹", "布", "匹", 16, 5, 0.30, "四季皆需，冬季尤贵", resourceKey = "cloth"),
        MarketCommodity("herb", "药材", "药", "份", 40, 1, 0.20, "济世良药，瘟疫时价格飞涨", itemId = "herb"),
        MarketCommodity("weapon", "铁器", "铁", "件", 64, 1, 0.15, "农具兵器皆赖之，战时涨价", itemId = "weapon"),
        MarketCommodity("book", "书籍", "书", "册", 80, 1, 0.10, "圣贤之书，科举季需求旺盛", itemId = "book"),
        MarketCommodity("horse", "马匹", "马", "匹", 240, 1, 0.25, "行军征战之需，军需时价格高涨")
    )

    val campaignStages = listOf(
        CampaignStage(101, "乡野流寇", 60, 80, 5),
        CampaignStage(102, "山寨匪首", 120, 150, 10),
        CampaignStage(201, "州府叛军", 260, 350, 25),
        CampaignStage(301, "边关马贼", 520, 800, 60),
        CampaignStage(401, "乱世强藩", 1000, 1500, 120)
    )

    val historyEvents = listOf(
        HistoryEvent(1368, "洪武开国", "大明开国，百废待兴。", "era_start"),
        HistoryEvent(1405, "郑和下西洋", "海贸渐兴，布匹药材需求上升。", "trade_boost"),
        HistoryEvent(1449, "土木之变", "边防震动，战事频仍。", "war_defense"),
        HistoryEvent(1522, "嘉靖改元", "朝局更替，地方税赋调整。", "emperor_change"),
        HistoryEvent(1581, "一条鞭法", "税制改革推行，银粮往来更频。", "tax_reform"),
        HistoryEvent(1627, "崇祯登基", "内忧外患，天下渐乱。", "political_chaos"),
        HistoryEvent(1630, "流寇蜂起", "流寇转战各地，乡里难安。", "war_civil"),
        HistoryEvent(1642, "中原大饥", "灾荒蔓延，粮价高涨。", "great_famine"),
        HistoryEvent(1644, "甲申之变", "山河倾覆，旧朝将尽。", "ending_year")
    )

    val achievements = listOf(
        AchievementDef("pop_10", "人丁兴旺", "宗族存活人口达到10人", "丁", rewardFame = 10),
        AchievementDef("pop_20", "枝繁叶茂", "宗族存活人口达到20人", "族", rewardFame = 20),
        AchievementDef("silver_500", "富甲一方", "银两达到500", "富", rewardFame = 15),
        AchievementDef("fame_100", "名震一方", "声望达到100", "望", rewardSilver = 30),
        AchievementDef("first_xiucai", "首中秀才", "族中首次出现秀才", "秀", rewardFame = 10, rewardSilver = 10),
        AchievementDef("first_juren", "金榜题名", "族中首次出现举人", "举", rewardFame = 20, rewardSilver = 30),
        AchievementDef("first_jinshi", "天子门生", "族中首次出现进士", "进", rewardFame = 40, rewardSilver = 60),
        AchievementDef("first_bazong", "从军立功", "族中首次出现把总", "将", rewardFame = 10, rewardSilver = 15),
        AchievementDef("fort_3", "铜墙铁壁", "拥有3座寨堡", "堡", rewardFame = 15),
        AchievementDef("ind_10", "田连阡陌", "拥有10个以上产业", "田", rewardFame = 15, rewardSilver = 25),
        AchievementDef("rank_wangzu", "望族崛起", "宗族品级提升至望族", "族", rewardSilver = 40),
        AchievementDef("survive_10y", "十年磨剑", "宗族延续10年以上", "剑", rewardFame = 15, rewardSilver = 30),
        AchievementDef("births_10", "多子多孙", "累计出生10名族人", "孙", rewardFame = 10),
        AchievementDef("exams_3", "书香世家", "累计3人通过科举", "书", rewardFame = 15, rewardSilver = 20),
        AchievementDef("merits_5", "将门虎子", "累计5次军功", "虎", rewardFame = 20, rewardSilver = 25)
    )

    val yearlyGoalPool = listOf(
        YearlyGoalDef("earn_silver_30", "积银三十", "本年银两净增30以上", "银", rewardFame = 5, rewardGrain = 15),
        YearlyGoalDef("earn_grain_50", "五谷丰登", "本年粮食净增50以上", "谷", rewardSilver = 15, rewardFame = 5),
        YearlyGoalDef("birth_2", "添丁进口", "本年至少新增2名族人", "丁", rewardGrain = 20, rewardFame = 5),
        YearlyGoalDef("martial_40", "武艺精进", "年末至少1名族人武艺达到40", "武", rewardFame = 8, rewardSilver = 8),
        YearlyGoalDef("study_50", "学识渊博", "年末至少1名族人学识达到50", "学", rewardFame = 8, rewardSilver = 10),
        YearlyGoalDef("exam_pass", "金榜题名", "本年至少1名族人通过科举", "榜", rewardFame = 15, rewardSilver = 20, minRank = 3),
        YearlyGoalDef("build_ind_2", "开拓产业", "本年至少新建2个产业", "业", rewardFame = 8, rewardSilver = 10),
        YearlyGoalDef("fame_plus_20", "声名远播", "本年声望净增20以上", "名", rewardSilver = 15, rewardGrain = 10),
        YearlyGoalDef("rank_up", "光宗耀祖", "本年宗族品级提升一次", "族", rewardSilver = 20, rewardGrain = 20, rewardFame = 10),
        YearlyGoalDef("no_death", "全族平安", "本年无一族人死亡", "安", rewardSilver = 10, rewardGrain = 10, rewardFame = 10)
    )

    val maleNames = listOf("德", "仁", "义", "礼", "智", "信", "忠", "孝", "廉", "恕", "恒", "谦", "正", "善", "诚", "敬", "慎", "温", "宽", "俭", "文", "学", "博", "儒", "翰", "墨", "彦", "哲", "思", "明", "达", "通", "睿", "渊", "敏", "聪", "颖", "书", "策", "论", "武", "勇", "刚", "毅", "烈", "威", "猛", "壮", "豪", "雄", "虎", "龙", "鹏", "骏", "飞", "彪", "锐", "铮", "钧", "镇", "远", "昌", "盛", "兴", "旺", "隆", "泰", "亨", "运", "吉", "安", "康", "宁", "和", "平", "顺", "裕", "丰", "茂", "荣")
    val femaleNames = listOf("兰", "梅", "菊", "莲", "荷", "蕙", "芝", "桂", "杏", "蓉", "薇", "萱", "芙", "茉", "蔷", "芷", "蕊", "藤", "葵", "棠", "秀", "淑", "婉", "雅", "慧", "贞", "静", "柔", "娴", "端", "敏", "巧", "素", "洁", "纯", "温", "惠", "懿", "芳", "馨", "月", "云", "霞", "雪", "露", "虹", "晴", "春", "秋", "晓", "珍", "玉", "瑶", "琴", "琳", "珊", "瑾", "璇", "翠", "琪")

    fun defaultMarketPrices() = marketCommodities.associate { it.id to it.basePrice }
    fun origin(id: String) = origins.firstOrNull { it.id == id } ?: origins.first()
    fun region(id: String) = regions.firstOrNull { it.id == id } ?: regions.first()
    fun difficulty(id: String) = difficulties.firstOrNull { it.id == id } ?: difficulties[1]
    fun motto(id: String) = familyMottos.firstOrNull { it.id == id } ?: familyMottos.first()
    fun talent(id: String?) = talents.firstOrNull { it.id == id }
    fun industry(id: String) = industryTypes.firstOrNull { it.id == id }
    fun evolution(from: String) = industryEvolutions.firstOrNull { it.from == from }
    fun item(id: String) = itemTypes.firstOrNull { it.id == id }
    fun academy(id: String) = academyTypes.firstOrNull { it.id == id }
    fun expedition(id: String) = expeditionTypes.firstOrNull { it.id == id }
    fun exam(id: String) = examLevels.firstOrNull { it.id == id }
    fun militaryRank(name: String?) = militaryRanks.firstOrNull { it.name == name || it.id == name } ?: militaryRanks.first()
    fun officialRank(id: String?) = officialRanks.firstOrNull { it.id == id || it.name == id }
    fun marriageTier(id: String) = marriageTiers.firstOrNull { it.id == id }
    fun laborJob(id: String?) = laborJobs.firstOrNull { it.id == id }
    fun commodity(id: String) = marketCommodities.firstOrNull { it.id == id }
    fun achievement(id: String) = achievements.firstOrNull { it.id == id }
    fun yearlyGoal(id: String) = yearlyGoalPool.firstOrNull { it.id == id }
    fun rankName(rank: Int) = clanRanks.getOrElse(rank - 1) { clanRanks.first() }
}
