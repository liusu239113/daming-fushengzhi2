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
        IndustryType("escort", "镖局", ResourceKind.Silver, 18, 700, "押镖走货，高收益但有折损风险", 5, specialEffect = "risk_loss_20"),
        IndustryType("bookshop", "书坊", ResourceKind.Fame, 6, 650, "刊印经书，声名远扬兼赚银两", 5, resource2 = ResourceKind.Silver, baseOutput2 = 5, specialEffect = "study_grow_10"),
        IndustryType("salt_field", "盐场", ResourceKind.Silver, 25, 1000, "贩盐暴利，但官府稽查严厉", 5, specialEffect = "risk_tax_30"),
        IndustryType("money_house", "钱庄", ResourceKind.Silver, 20, 850, "放贷收息，银两按总资产额外生利", 6, specialEffect = "interest_2pct"),
        IndustryType("fleet", "船队", ResourceKind.Silver, 32, 1400, "海上贸易，利润极高但受季风影响", 6, specialEffect = "season_amplify"),
        IndustryType("estate", "庄园", ResourceKind.Grain, 10, 1200, "粮银名三收", 6, resource2 = ResourceKind.Silver, baseOutput2 = 8, resource3 = ResourceKind.Fame, baseOutput3 = 3),
        IndustryType("pawnshop", "当铺", ResourceKind.Silver, 26, 1600, "典当质押，乱世暴利", 7, specialEffect = "interest_1pct", evolvesTo = "piaohao"),
        IndustryType("dye_house", "染坊", ResourceKind.Cloth, 12, 1300, "染制锦缎彩绸，布匹银两双收", 7, resource2 = ResourceKind.Silver, baseOutput2 = 12),
        IndustryType("private_school", "私塾", ResourceKind.Fame, 10, 1100, "开馆授徒，声名远播", 7, resource2 = ResourceKind.Silver, baseOutput2 = 8, specialEffect = "study_grow_10"),
        IndustryType("canal_wharf", "漕运码头", ResourceKind.Silver, 38, 1800, "扼守漕运要道，南北货物中转抽成", 7, specialEffect = "trade_hub"),
        IndustryType("arsenal", "军械坊", ResourceKind.Silver, 30, 2200, "铸造兵器甲胄，从军族人战力提升", 8, resource2 = ResourceKind.Fame, baseOutput2 = 8, specialEffect = "military_boost_25"),
        IndustryType("weaving_bureau", "织造局", ResourceKind.Cloth, 20, 2000, "承接官府织造，布匹银两声望三收", 8, resource2 = ResourceKind.Silver, baseOutput2 = 22, resource3 = ResourceKind.Fame, baseOutput3 = 6),
        IndustryType("imperial_merchant", "皇商行", ResourceKind.Silver, 60, 4000, "皇家特许经营，暴利但消耗声望", 9, specialEffect = "consume_fame_3"),
        IndustryType("customs_house", "海关行", ResourceKind.Silver, 50, 3500, "把持海关贸易，银两兼得声望", 9, resource2 = ResourceKind.Fame, baseOutput2 = 10, evolved = true),
        IndustryType("grand_farmland", "万亩良田", ResourceKind.Grain, 45, 3000, "终极农业，粮银双收且不受天灾", 9, resource2 = ResourceKind.Silver, baseOutput2 = 20, specialEffect = "weather_immune", evolved = true),
        IndustryType("fertile_field", "良田", ResourceKind.Grain, 22, 500, "精耕良田，产量极高", 4, evolved = true),
        IndustryType("trade_house", "商号", ResourceKind.Silver, 18, 600, "连锁商号，日进斗金", 5, resource2 = ResourceKind.Fame, baseOutput2 = 3, evolved = true),
        IndustryType("silk_house", "绸缎庄", ResourceKind.Cloth, 12, 500, "丝绸锦缎，布匹兼赚银两", 5, resource2 = ResourceKind.Silver, baseOutput2 = 5, evolved = true),
        IndustryType("piaohao", "票号", ResourceKind.Silver, 35, 1200, "汇通天下，跨省银票兑付", 8, specialEffect = "interest_2pct", evolved = true)
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
        ExpeditionType("trade_route", "行商远途", "商", "沿商路远行贸易，收益丰厚但风险也大", 3, 120, 80, 5, rewardSilver = 200..500, rewardFame = 3..8, riskRate = 0.15, riskDesc = "路遇山贼"),
        ExpeditionType("study_trip", "游学求教", "游", "拜访名士，增长学识见闻", 2, 80, 60, 3, rewardStudy = 3..8, rewardFame = 3..8, riskRate = 0.05, riskDesc = "水土不服"),
        ExpeditionType("explore", "深山探秘", "探", "进山探寻宝物，可能有意外收获", 2, 60, 80, 4, rewardSilver = 30..250, itemChance = 0.3, riskRate = 0.20, riskDesc = "遭遇猛兽"),
        ExpeditionType("recruit", "招贤纳士", "招", "四处访求能人异士加入宗族", 2, 150, 80, 4, rewardFame = 3..10, recruitChance = 0.4, riskRate = 0.08, riskDesc = "遇到骗子"),
        ExpeditionType("sea_trade", "海上通商", "帆", "扬帆出海，与番邦贸易", 4, 300, 150, 6, rewardSilver = 500..1200, rewardFame = 8..20, riskRate = 0.25, riskDesc = "遭遇海寇"),
        ExpeditionType("court_visit", "进京面圣", "朝", "上京朝觐，结交朝中权贵", 3, 500, 200, 6, rewardSilver = 100..300, rewardFame = 20..50, riskRate = 0.10, riskDesc = "卷入党争"),
        ExpeditionType("silk_road", "丝路远征", "驼", "远赴西域，带回奇珍异宝", 6, 800, 400, 8, rewardSilver = 1000..3000, rewardFame = 20..50, itemChance = 0.6, riskRate = 0.30, riskDesc = "沙匪劫掠")
    )

    val examLevels = listOf(
        ExamLevel("tongshi", "童试", 30, 0.40, "童生", 5),
        ExamLevel("xiangshi", "乡试", 60, 0.25, "秀才", 15),
        ExamLevel("huishi", "会试", 85, 0.12, "举人", 30),
        ExamLevel("dianshi", "殿试", 95, 0.05, "进士", 60)
    )

    val campaignStages = listOf(
        CampaignStage(101, "乡野流寇", 60, 80, 5),
        CampaignStage(102, "山寨匪首", 120, 150, 10),
        CampaignStage(201, "州府叛军", 260, 350, 25),
        CampaignStage(301, "边关马贼", 520, 800, 60),
        CampaignStage(401, "乱世强藩", 1000, 1500, 120)
    )

    val maleNames = listOf("德", "仁", "义", "礼", "智", "信", "忠", "孝", "文", "学", "博", "儒", "武", "勇", "刚", "毅", "远", "昌", "盛", "安")
    val femaleNames = listOf("兰", "梅", "菊", "莲", "秀", "淑", "婉", "雅", "慧", "贞", "静", "柔", "月", "云", "霞", "雪", "珍", "玉", "瑶", "琴")

    fun origin(id: String) = origins.firstOrNull { it.id == id } ?: origins.first()
    fun region(id: String) = regions.firstOrNull { it.id == id } ?: regions.first()
    fun difficulty(id: String) = difficulties.firstOrNull { it.id == id } ?: difficulties[1]
    fun motto(id: String) = familyMottos.firstOrNull { it.id == id } ?: familyMottos.first()
    fun industry(id: String) = industryTypes.firstOrNull { it.id == id }
    fun item(id: String) = itemTypes.firstOrNull { it.id == id }
    fun academy(id: String) = academyTypes.firstOrNull { it.id == id }
    fun expedition(id: String) = expeditionTypes.firstOrNull { it.id == id }
    fun exam(id: String) = examLevels.firstOrNull { it.id == id }
    fun rankName(rank: Int) = clanRanks.getOrElse(rank - 1) { clanRanks.first() }
}
