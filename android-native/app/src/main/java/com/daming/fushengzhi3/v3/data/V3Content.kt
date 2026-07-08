package com.daming.fushengzhi3.v3.data

object V3Content {
    val roots = listOf("寒门佃户", "没落士族", "边地军户", "江南商族", "山中堡寨", "海商遗族")
    val counties = listOf("江南水乡", "中原灾地", "西北边堡", "湖广粮仓", "闽粤海路", "辽东边地")
    val creeds = listOf("耕读传家", "重商逐利", "聚族自保", "忠君报国", "明哲保身", "开海远行")
    val crises = listOf("饥荒将至", "流寇逼近", "官府催税", "族产争端", "商路断绝", "瘟疫初起")

    val initialPeople = listOf(
        V3Person(1, "李慎行", 42, "主房", "族长", V3Trait.Smooth, study = 62, martial = 35, commerce = 44, diplomacy = 68, loyalty = 82),
        V3Person(2, "李承岳", 24, "武支", "乡勇头目", V3Trait.Martial, study = 28, martial = 74, commerce = 22, diplomacy = 34, loyalty = 71),
        V3Person(3, "李若兰", 21, "书香支", "廪生之女", V3Trait.Studious, study = 77, martial = 18, commerce = 37, diplomacy = 56, loyalty = 76),
        V3Person(4, "李仲财", 36, "商支", "行商", V3Trait.Greedy, study = 31, martial = 26, commerce = 80, diplomacy = 45, loyalty = 58),
        V3Person(5, "李济民", 30, "二房", "郎中", V3Trait.Benevolent, study = 54, martial = 20, commerce = 34, diplomacy = 62, loyalty = 79),
        V3Person(6, "李守砚", 17, "书香支", "童生", V3Trait.Ambitious, study = 69, martial = 22, commerce = 28, diplomacy = 41, loyalty = 65),
        V3Person(7, "李阿衡", 29, "武支", "寨丁", V3Trait.Fierce, study = 20, martial = 81, commerce = 18, diplomacy = 26, loyalty = 73),
        V3Person(8, "李环娘", 27, "商支", "账房", V3Trait.Cunning, study = 43, martial = 16, commerce = 76, diplomacy = 67, loyalty = 61),
        V3Person(9, "李观潮", 33, "海路支", "舵工", V3Trait.Smooth, study = 35, martial = 42, commerce = 71, diplomacy = 52, loyalty = 57),
        V3Person(10, "李采薇", 19, "二房", "药童", V3Trait.Honest, study = 58, martial = 17, commerce = 29, diplomacy = 48, loyalty = 84)
    )

    val initialBranches = listOf(
        V3Branch("main", "主房", "李慎行", V3Route.Hermit, loyalty = 82, wealth = 48, influence = 62, grievance = 8, desc = "掌宗祠谱牒，重秩序与继承。"),
        V3Branch("second", "二房", "李济民", V3Route.Hermit, loyalty = 74, wealth = 35, influence = 41, grievance = 15, desc = "重民心与医药，常为族中缓冲。"),
        V3Branch("merchant", "商支", "李仲财", V3Route.Merchant, loyalty = 58, wealth = 73, influence = 46, grievance = 31, desc = "掌集市商路，求利也最易生私心。"),
        V3Branch("martial", "武支", "李承岳", V3Route.Fortress, loyalty = 69, wealth = 28, influence = 50, grievance = 24, desc = "主张募勇筑寨，乱世先求自保。"),
        V3Branch("scholar", "书香支", "李若兰", V3Route.Scholar, loyalty = 76, wealth = 31, influence = 58, grievance = 12, desc = "主张修书院、走科举、联士绅。"),
        V3Branch("sea", "海路支", "李观潮", V3Route.Overseas, loyalty = 55, wealth = 50, influence = 33, grievance = 20, desc = "熟悉码头与海商，通向远渡海外路线。")
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

    val initialAnnualGoals = listOf(
        V3AnnualGoal("grain_320", "岁内蓄粮", "乱世先看粮仓，年底前把粮食储备推到 320。", V3GoalMetric.GrainStock, 320, V3Route.Hermit, rewardCohesion = 4),
        V3AnnualGoal("safe_4", "靖安四方", "至少让 4 个县域地点风险低于 30。", V3GoalMetric.SafeSites, 4, V3Route.Fortress, rewardInfluence = 4),
        V3AnnualGoal("control_5", "握住县域", "至少让 5 个地点控制达到 50。", V3GoalMetric.ControlledSites, 5, V3Route.Warlord, rewardInfluence = 5),
        V3AnnualGoal("silver_260", "积银备变", "年底前把银两储备推到 260。", V3GoalMetric.SilverStock, 260, V3Route.Merchant, rewardSilver = 20),
        V3AnnualGoal("cohesion_72", "合族同心", "把宗族凝聚提升到 72。", V3GoalMetric.Cohesion, 72, V3Route.Hermit, rewardGrain = 30),
        V3AnnualGoal("influence_48", "族望入县", "把族望声名提升到 48。", V3GoalMetric.Influence, 48, V3Route.Scholar, rewardInfluence = 3)
    )

    fun goalsFor(creed: String, crisis: String): List<V3AnnualGoal> {
        val creedGoal = when (creed) {
            "耕读传家" -> initialAnnualGoals.first { it.id == "influence_48" }
            "重商逐利" -> initialAnnualGoals.first { it.id == "silver_260" }
            "聚族自保" -> initialAnnualGoals.first { it.id == "safe_4" }
            "忠君报国" -> initialAnnualGoals.first { it.id == "control_5" }
            "开海远行" -> initialAnnualGoals.first { it.id == "silver_260" }
            else -> initialAnnualGoals.first { it.id == "cohesion_72" }
        }
        val crisisGoal = when (crisis) {
            "饥荒将至" -> initialAnnualGoals.first { it.id == "grain_320" }
            "流寇逼近" -> initialAnnualGoals.first { it.id == "safe_4" }
            "官府催税" -> initialAnnualGoals.first { it.id == "silver_260" }
            "族产争端" -> initialAnnualGoals.first { it.id == "cohesion_72" }
            "商路断绝" -> initialAnnualGoals.first { it.id == "control_5" }
            else -> initialAnnualGoals.first { it.id == "safe_4" }
        }
        return listOf(creedGoal, crisisGoal, initialAnnualGoals.first { it.id == "control_5" })
            .distinctBy { it.id }
            .take(3)
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
            desc = "族谱、族规、房支议事之地。",
            taskTypes = listOf(V3TaskType.Govern, V3TaskType.Diplomacy)
        ),
        V3CountySite(
            id = "farmland",
            name = "南乡田庄",
            type = V3CountySiteType.Farmland,
            level = 1,
            control = 55,
            risk = 28,
            status = V3SiteStatus.Strained,
            desc = "宗族粮仓根基，受天候和乡民劳力影响。",
            taskTypes = listOf(V3TaskType.Farm, V3TaskType.Relief)
        ),
        V3CountySite(
            id = "market",
            name = "西河集市",
            type = V3CountySiteType.Market,
            level = 1,
            control = 41,
            risk = 34,
            status = V3SiteStatus.Strained,
            desc = "银两、商品、商帮关系汇集之处。",
            taskTypes = listOf(V3TaskType.Trade, V3TaskType.Scout)
        ),
        V3CountySite(
            id = "yamen",
            name = "清河县衙",
            type = V3CountySiteType.Yamen,
            level = 1,
            control = 22,
            risk = 45,
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
            desc = "士林清议之所，推动耕读、士绅与仕途路线。",
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

    fun newGame(root: String, county: String, creed: String, crisis: String): V3GameState {
        val base = V3GameState(root = root, county = county, creed = creed, crisis = crisis)
        val routeBoost = when (creed) {
            "耕读传家" -> V3Route.Scholar
            "重商逐利" -> V3Route.Merchant
            "聚族自保" -> V3Route.Fortress
            "忠君报国" -> V3Route.Loyalist
            "开海远行" -> V3Route.Overseas
            else -> V3Route.Hermit
        }
        return base.copy(
            clanName = when (root) {
                "江南商族" -> "李氏商族"
                "边地军户" -> "李氏军户"
                "山中堡寨" -> "李氏寨族"
                else -> "李氏宗族"
            },
            annualGoals = goalsFor(creed, crisis),
            routeScores = base.routeScores + (routeBoost to ((base.routeScores[routeBoost] ?: 0) + 12)),
            pendingReports = listOf("${county}局势未稳，${crisis}已成眼前第一患。"),
            eventLog = listOf("${root}立于${county}，奉行【${creed}】，却遭【${crisis}】。")
        )
    }
}
