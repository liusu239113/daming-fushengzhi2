package com.daming.fushengzhi3.v3.data

object V3Content {
    val roots = listOf("寒门佃户", "没落士族", "边地军户", "江南商族", "山中堡寨", "海商遗族")
    val counties = listOf("江南水乡", "中原灾地", "西北边堡", "湖广粮仓", "闽粤海路", "辽东边地")
    val creeds = listOf("耕读传家", "重商逐利", "聚族自保", "忠君报国", "明哲保身", "开海远行")
    val crises = listOf("饥荒将至", "流寇逼近", "官府催税", "族产争端", "商路断绝", "瘟疫初起")

    val initialPeople = listOf(
        V3Person(1, "李慎行", 24, "主房", "开族祖", V3Trait.Smooth, study = 26, martial = 22, commerce = 20, diplomacy = 24, loyalty = 90)
    )

    val spouseCandidates = listOf(
        V3SpouseCandidate("farmer", "王春娘", "邻村农户之女，能持家、会管田，适合稳住粮仓。", silverCost = 18, grainCost = 35, influenceReq = 0, studyBonus = 4, commerceBonus = 8, diplomacyBonus = 4, route = V3Route.Hermit),
        V3SpouseCandidate("merchant", "沈玉娘", "小商户之女，带来账本和货路，适合早期积银。", silverCost = 45, grainCost = 20, influenceReq = 8, commerceBonus = 16, diplomacyBonus = 6, route = V3Route.Merchant),
        V3SpouseCandidate("scholar", "陈婉仪", "寒门书香之后，擅识字教子，适合耕读路线。", silverCost = 35, grainCost = 25, influenceReq = 12, studyBonus = 16, diplomacyBonus = 7, route = V3Route.Scholar),
        V3SpouseCandidate("martial", "赵月英", "军户孤女，熟弓马与寨防，适合乱世自保。", silverCost = 30, grainCost = 30, influenceReq = 10, martialBonus = 16, diplomacyBonus = 3, route = V3Route.Fortress)
    )

    val initialBranches = listOf(
        V3Branch("main", "主房", "李慎行", V3Route.Hermit, loyalty = 86, wealth = 18, influence = 20, grievance = 0, desc = "一人开族，尚无旁支。先成家、置产、育子，再谈宗族兴旺。")
    )

    val initialEstateAssets = listOf(
        V3EstateAsset("tenant_land", V3EstateType.TenantLand, level = 1, workers = 1, desc = "祖上传下的几亩佃田，能撑住早期口粮。")
    )

    val initialWorldRegions = listOf(
        V3WorldRegion("home_county", "清河县", 1, V3RegionStatus.Controlled, control = 42, enemyPower = 45, wealth = 40, desc = "家族起家的县域，先从这里控制税粮、宗族和团练。"),
        V3WorldRegion("river_prefecture", "三江府", 2, V3RegionStatus.Unknown, control = 0, enemyPower = 95, wealth = 85, desc = "水路商贸和粮仓所在，控制后商队收益大增。"),
        V3WorldRegion("mountain_prefecture", "黑松府", 2, V3RegionStatus.Unknown, control = 0, enemyPower = 120, wealth = 55, desc = "山寨、流寇和盐道盘踞之地，是军务扩张的试金石。"),
        V3WorldRegion("south_province", "南直隶", 3, V3RegionStatus.Unknown, control = 0, enemyPower = 210, wealth = 160, desc = "州府士绅与商帮交错，控制后家族从县族跃升一方豪强。"),
        V3WorldRegion("north_capital", "京畿", 4, V3RegionStatus.Unknown, control = 0, enemyPower = 360, wealth = 220, desc = "朝廷与禁军所在。若能入主京畿，造反路线进入天下争鼎。"),
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
        V3AnnualGoal("unify_30", "一方豪强", "统一进度达到 30，具备割据一方的基础。", V3GoalMetric.Unification, 30, V3Route.Warlord, rewardSilver = 80)
    )

    fun goalsFor(creed: String, crisis: String): List<V3AnnualGoal> {
        val creedGoal = when (creed) {
            "耕读传家" -> initialAnnualGoals.first { it.id == "influence_48" }
            "重商逐利" -> initialAnnualGoals.first { it.id == "silver_220" }
            "聚族自保" -> initialAnnualGoals.first { it.id == "safe_3" }
            "忠君报国" -> initialAnnualGoals.first { it.id == "control_4" }
            "开海远行" -> initialAnnualGoals.first { it.id == "silver_220" }
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

    fun newGame(root: String, county: String, creed: String, crisis: String, clanNameInput: String = "李氏宗族"): V3GameState {
        val cleanedClanName = clanNameInput.trim().ifBlank { "李氏宗族" }.take(8)
        val clanSurname = cleanedClanName.firstOrNull()?.toString()?.takeIf { it.isNotBlank() } ?: "李"
        val founderName = "${clanSurname}慎行"
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
            clanName = cleanedClanName,
            silver = when (root) {
                "寒门佃户" -> 58
                "没落士族" -> 76
                "边地军户" -> 68
                "江南商族" -> 110
                "山中堡寨" -> 74
                else -> 70
            },
            grain = when (root) {
                "寒门佃户" -> 120
                "边地军户" -> 100
                "江南商族" -> 80
                "山中堡寨" -> 110
                else -> 95
            },
            influence = when (root) {
                "没落士族" -> 14
                "江南商族" -> 10
                "边地军户" -> 8
                else -> 6
            },
            cohesion = 62,
            militia = if (root == "边地军户" || root == "山中堡寨") 12 else 3,
            people = initialPeople.map { if (it.id == 1) it.copy(name = founderName) else it },
            branches = initialBranches.map { if (it.id == "main") it.copy(leaderName = founderName, desc = "一人开族，尚无旁支。先成家、置产、育子，再谈宗族兴旺。") else it },
            sites = initialSites.map { if (it.id == "shrine") it.copy(name = "${clanSurname}氏宗祠") else it },
            annualGoals = goalsFor(creed, crisis),
            routeScores = base.routeScores + (routeBoost to ((base.routeScores[routeBoost] ?: 0) + 12)),
            pendingReports = listOf("${county}局势未稳，${crisis}已成眼前第一患。"),
            eventLog = listOf("${root}立于${county}，奉行【${creed}】，却遭【${crisis}】。")
        )
    }
}
