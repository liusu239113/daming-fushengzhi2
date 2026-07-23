package com.arktools.daming.v3.data

object V3EventContent {
    val siteEvents = listOf(
        V3ActiveEvent("祠堂议谱", "族老请重修谱牒，将庶支与外迁族人重新登记。主房担心权柄被分，二房则认为此举可稳人心。", listOf(
            V3EventChoice("开祠修谱", "大开祠门，召集族众与庶支，命族老重修谱牒，将外迁者一一录名归宗。主房嫌权柄被分，庶支却感归宗之恩。", silverDelta = -18, cohesionDelta = 6, influenceDelta = 2, siteId = "shrine", siteControlDelta = 6, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", grievanceDelta = 2), V3BranchImpact("second", loyaltyDelta = 2, grievanceDelta = -2))),
            V3EventChoice("只录近支", "只录五服内近支，旧谱不动，主房秩序井然，庶支却面有怨色。", cohesionDelta = -2, influenceDelta = 3, siteId = "shrine", siteControlDelta = 3, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 3), V3BranchImpact("second", grievanceDelta = 3)))
        )),
        V3ActiveEvent("祭田争界", "宗祠祭田与邻村田界不清，乡民聚集争吵，若处置失当会伤及民心。", listOf(
            V3EventChoice("请士绅公断", "备礼往请县中士绅公断田界，乡绅到场丈量剖明是非，邻村虽不悦却也服公论。", silverDelta = -20, gentryDelta = 6, villagersDelta = 2, siteId = "shrine", siteRiskDelta = -8, route = V3Route.Scholar),
            V3EventChoice("强行圈界", "命庄头带人立石为界强行圈定祭田，邻村虽怒不敢争，乡民却窃议李氏霸道。", grainDelta = 35, villagersDelta = -8, cohesionDelta = -3, siteId = "farmland", siteControlDelta = 5, route = V3Route.Warlord)
        )),
        V3ActiveEvent("旱情压田", "南乡田庄连日无雨，佃户望天兴叹。若不修渠开仓，秋粮恐难入仓。", listOf(
            V3EventChoice("修渠引水", "拨银请匠人修渠引上游之水入田，又雇佃户日夜轮灌，佃户欢呼。", silverDelta = -35, grainDelta = 20, villagersDelta = 4, siteId = "farmland", siteControlDelta = 5, siteRiskDelta = -16, route = V3Route.Hermit),
            V3EventChoice("减租稳佃", "布告减租三成，命仓中开小粮口借种给佃户，佃户感泣，人心稳住。", grainDelta = -25, cohesionDelta = 5, villagersDelta = 8, siteId = "farmland", siteRiskDelta = -10, route = V3Route.Hermit)
        )),
        V3ActiveEvent("粮仓霉变", "旧仓受潮，族中存粮已有霉味。商支建议卖出坏粮，二房坚决反对。", listOf(
            V3EventChoice("翻修粮仓", "请工匠翻修仓顶夯实板缝，受潮之粮尽数翻晒去霉，仓中存粮得保。", silverDelta = -28, grainDelta = -8, siteId = "farmland", siteControlDelta = 4, siteRiskDelta = -12, route = V3Route.Fortress),
            V3EventChoice("低价卖粮", "命人将霉变粮低价粜给酿坊与养猪户，换得银二十二两，乡民却骂李氏拿坏粮害人。", silverDelta = 22, grainDelta = -35, villagersDelta = -5, siteId = "market", siteRiskDelta = 5, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", wealthDelta = 3, influenceDelta = 2)))
        )),
        V3ActiveEvent("米价暴涨", "集市米价一日三涨，商帮暗中囤货，乡民已在米铺门前争执。", listOf(
            V3EventChoice("平价放粮", "开仓平价放粮，每人限购二斗，米铺前乡民排成长队称谢，集市渐稳。", grainDelta = -55, villagersDelta = 12, cohesionDelta = 4, siteId = "market", siteRiskDelta = -12, route = V3Route.Hermit),
            V3EventChoice("顺势加价", "随行就市将存粮高价出售，赚得银六十两，乡民却骂李氏黑心趁火打劫。", silverDelta = 60, villagersDelta = -10, merchantsDelta = 5, siteId = "market", siteControlDelta = 5, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", wealthDelta = 5, grievanceDelta = -2)))
        )),
        V3ActiveEvent("私盐线索", "集市牙人密报，有私盐客借山道入县。此事可牟利，也会招来官府盘查。", listOf(
            V3EventChoice("暗中抽成", "密令牙人牵线，与私盐客约定山道借道抽成，银钱暗中入账，天知地知。", silverDelta = 45, yamenDelta = -6, merchantsDelta = 5, siteId = "market", siteRiskDelta = 6, route = V3Route.Merchant),
            V3EventChoice("交给县衙", "将私盐线索密报县衙，差役设卡拿获私盐客，县丞夸李氏忠义，商帮却嫌多事。", yamenDelta = 8, merchantsDelta = -5, influenceDelta = 2, siteId = "yamen", siteControlDelta = 3, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("徭役点丁", "县衙差役持文书到祠堂，要求宗族出丁修城。武支想借机练人，主房担心伤农。", listOf(
            V3EventChoice("按丁服役", "按丁册点齐族中青壮赴城修城，武支虽有练人之机，主房却叹农时被误。", yamenDelta = 8, cohesionDelta = -4, siteId = "yamen", siteRiskDelta = -4, route = V3Route.Loyalist),
            V3EventChoice("出银免役", "拨银五十两代全族雇役抵丁，族人免了奔波皆大欢喜，县中也赞李氏慷慨。", silverDelta = -50, yamenDelta = 3, cohesionDelta = 3, siteId = "yamen", siteControlDelta = 4, route = V3Route.Hermit)
        )),
        V3ActiveEvent("狱案牵连", "族中远亲被卷入县狱，书吏暗示可以花银周旋。若不救，族人恐寒心。", listOf(
            V3EventChoice("打点书吏", "备银四十两密托书吏周旋，又送粮入牢打点，远亲得以轻判出狱，族人皆称宗族有人情。", silverDelta = -40, cohesionDelta = 5, yamenDelta = 3, siteId = "yamen", route = V3Route.Loyalist),
            V3EventChoice("守法不救", "称狱案自有法度宗族不当干预，远亲寒心，族中也议论李氏太薄情。", influenceDelta = 3, cohesionDelta = -5, gentryDelta = 3, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("main", loyaltyDelta = -2), V3BranchImpact("second", grievanceDelta = 3)))
        )),
        V3ActiveEvent("书院名师", "东林书院有名师过境，愿短留授课，但束脩不菲。", listOf(
            V3EventChoice("延请授课", "备束脩四十五两亲往书院礼请名师到祠中授馆三月，族中学子闻之雀跃。", silverDelta = -45, influenceDelta = 5, gentryDelta = 6, siteId = "academy", siteControlDelta = 8, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("scholar", influenceDelta = 4, grievanceDelta = -3))),
            V3EventChoice("只请题字", "只备薄礼十二两请名师题了族学匾额与楹联，小得声名，不伤财力。", silverDelta = -12, influenceDelta = 2, gentryDelta = 2, siteId = "academy", siteControlDelta = 2, route = V3Route.Scholar)
        )),
        V3ActiveEvent("党争牵连", "书院讲会谈及朝局，县中有心人已将名单送入县衙。", listOf(
            V3EventChoice("约束诸生", "命书香支约束诸生不得妄议朝局，焚去几份敏感文稿，避过了党争之祸。", cohesionDelta = 3, gentryDelta = -3, yamenDelta = 4, siteId = "academy", siteRiskDelta = -8, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("scholar", grievanceDelta = 3))),
            V3EventChoice("力挺族学公议", "公开力挺族学公议清名，称士大夫当以天下为己任，士林闻之振奋，县衙却开始留意李氏。", influenceDelta = 8, gentryDelta = 9, yamenDelta = -7, siteId = "academy", siteControlDelta = 6, route = V3Route.Scholar)
        )),
        V3ActiveEvent("药材短缺", "医馆药柜见底，瘟病未止。海路支称码头有药材可购，但价高。", listOf(
            V3EventChoice("高价购药", "拨银五十五两托海路支从码头高价购得药材，连夜送医馆，瘟病渐压。", silverDelta = -55, villagersDelta = 8, cohesionDelta = 4, siteId = "clinic", siteRiskDelta = -16, route = V3Route.Hermit),
            V3EventChoice("采药入山", "命几名胆大药农入黑松山采草药，虽遇匪警，所幸采得几味急用之药。", silverDelta = -10, villagersDelta = 4, banditsDelta = 3, siteId = "mountain_pass", siteRiskDelta = 5, route = V3Route.Fortress)
        )),
        V3ActiveEvent("伤兵求医", "军镇伤兵夜至医馆，请求收治。若救治，可结军镇；若拒绝，或免牵连。", listOf(
            V3EventChoice("收治伤兵", "命医馆连夜收治军镇伤兵，又备酒肉款待押送军士，军镇千总修书致谢。", silverDelta = -25, garrisonDelta = 9, villagersDelta = 2, siteId = "clinic", siteRiskDelta = 5, route = V3Route.Loyalist),
            V3EventChoice("婉拒离县", "称医馆狭小药粮不足，赠了些金疮药嘱他们往县城求医，免了军务牵连。", cohesionDelta = 2, garrisonDelta = -4, siteId = "clinic", siteRiskDelta = -4, route = V3Route.Hermit)
        )),
        V3ActiveEvent("乡勇索饷", "寨堡乡勇称巡夜辛苦，请求增饷。若拖欠，武支必有怨言。", listOf(
            V3EventChoice("增发饷银", "准乡勇所请，每人月增饷银二分，又赏酒肉，寨上欢声雷动，武支归心。", silverDelta = -36, militiaDelta = 8, garrisonDelta = 3, siteId = "fort", siteControlDelta = 6, route = V3Route.Fortress, branchImpacts = listOf(V3BranchImpact("martial", loyaltyDelta = 3, grievanceDelta = -5))),
            V3EventChoice("严令节用", "严令近来银根吃紧饷银暂不增，武支怨声四起，巡夜也渐松懈。", silverDelta = 5, cohesionDelta = -2, siteId = "fort", siteRiskDelta = 8, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact("martial", grievanceDelta = 6, loyaltyDelta = -2)))
        )),
        V3ActiveEvent("堡门收留", "流民请求入堡避寇。武支担心混入奸细，二房却主张收留。", listOf(
            V3EventChoice("验籍收留", "命武支于堡门设卡验籍，凡有家业来历者准入避寇，开粥棚赈济。", grainDelta = -30, villagersDelta = 8, cohesionDelta = 4, siteId = "fort", siteControlDelta = 4, route = V3Route.Fortress),
            V3EventChoice("闭门自守", "命堡门紧闭不放流民入内，堡外号哭声传至寨上，乡勇皆不忍闻。", villagersDelta = -7, banditsDelta = -2, siteId = "fort", siteRiskDelta = -8, route = V3Route.Hermit)
        )),
        V3ActiveEvent("船队失期", "三江码头船队迟迟未归，海路支与商支互相推诿。", listOf(
            V3EventChoice("派人沿江查访", "拨银二十五两，派熟悉水路的族人沿江查访船队下落，商帮稍安。", silverDelta = -25, merchantsDelta = 5, siteId = "dock", siteRiskDelta = -8, route = V3Route.Overseas),
            V3EventChoice("追究海路支", "命海路支管事到祠中说明追责，海路支愤愤不平，主房威严倒是涨了。", influenceDelta = 3, siteId = "dock", siteControlDelta = 3, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact("sea", grievanceDelta = 5, loyaltyDelta = -2), V3BranchImpact("main", influenceDelta = 2)))
        )),
        V3ActiveEvent("南洋来信", "码头商人带来远海书信，称南洋可置田开铺，愿引李氏族人前往。", listOf(
            V3EventChoice("暗筹船资", "密召海路支与商支议事，暗筹船资六十两，准备派人下南洋探路。", silverDelta = -60, merchantsDelta = 6, siteId = "dock", siteControlDelta = 8, route = V3Route.Overseas, routeDelta = 10, branchImpacts = listOf(V3BranchImpact("sea", influenceDelta = 5, wealthDelta = 4, grievanceDelta = -4))),
            V3EventChoice("暂存此信", "将南洋书信封存，嘱海路支不可妄动，等时局明朗再议。", cohesionDelta = 2, siteId = "dock", siteRiskDelta = -3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("伏击商队", "黑松山道传来急报，商队被流寇伏击，若不救援，集市商路恐断。", listOf(
            V3EventChoice("武支救援", "李承岳闻报即率乡勇驰援山道，击退流寇救下商队，缴获若干兵器，商帮感激不尽。", silverDelta = 20, militiaDelta = -4, merchantsDelta = 8, banditsDelta = -8, siteId = "mountain_pass", siteRiskDelta = -10, route = V3Route.Fortress, branchImpacts = listOf(V3BranchImpact("martial", influenceDelta = 4, loyaltyDelta = 2))),
            V3EventChoice("赎买货物", "派人与流寇接头出银四十五两赎回被劫货物，商路保住，流寇却更猖狂。", silverDelta = -45, merchantsDelta = 5, banditsDelta = 5, siteId = "mountain_pass", siteRiskDelta = 4, route = V3Route.Merchant)
        ))
    )

    val personEvents = listOf(
        V3ActiveEvent("族长夜议", "开族祖夜召族老，认为当前局势须定一条主线，否则各房各行其是。", listOf(
            V3EventChoice("定下家法", "开族祖当夜定下家法三条：宗祠主祭、田租归公、大事合议。诸房凛然听命。", cohesionDelta = 5, influenceDelta = 3, personId = 1, personFatigueDelta = 6, personMeritDelta = 2, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 3))),
            V3EventChoice("广听诸房", "命族老分别走访各房听取意见，虽决断慢了，房支怨气却消了不少。", cohesionDelta = 4, personId = 1, personFatigueDelta = 4, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("merchant", grievanceDelta = -2), V3BranchImpact("martial", grievanceDelta = -2), V3BranchImpact("scholar", grievanceDelta = -2)))
        )),
        V3ActiveEvent("承岳请战", "李承岳请率乡勇夜巡山道。他言乱世不能只靠文书，必须让流寇知道李氏有刀。", listOf(
            V3EventChoice("准其夜巡", "准李承岳所请，命他率乡勇夜巡山道，他当夜带人出发气势如虹。", militiaDelta = 5, banditsDelta = -6, personId = 2, personFatigueDelta = 10, personMeritDelta = 4, siteId = "mountain_pass", siteRiskDelta = -8, route = V3Route.Fortress, branchImpacts = listOf(V3BranchImpact("martial", influenceDelta = 4, grievanceDelta = -3))),
            V3EventChoice("令其守寨", "令李承岳坚守寨堡不可贸然出击，他虽悻悻，仍遵命守寨。", garrisonDelta = 3, personId = 2, personFatigueDelta = 5, siteId = "fort", siteControlDelta = 5, route = V3Route.Hermit)
        )),
        V3ActiveEvent("若兰清名", "李若兰在书院辩难中声名渐起，有士绅请她代写族中公议文。", listOf(
            V3EventChoice("请她执笔", "请李若兰执笔代写公议文，她下笔千言一挥而就，士绅传阅皆称才女。", influenceDelta = 5, gentryDelta = 7, personId = 3, personFatigueDelta = 8, personMeritDelta = 4, route = V3Route.Scholar),
            V3EventChoice("劝其避名", "劝李若兰女子不宜在士林抛头露面，她虽答应，眉宇间有怅然之色。", cohesionDelta = 3, personId = 3, personLoyaltyDelta = 2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("仲财账本", "李仲财呈上新账，账面盈利颇丰，却有几处支出含糊。", listOf(
            V3EventChoice("追查账目", "命账房当众核对李仲财呈上的账本，几处含糊支出被一一追问清楚。", silverDelta = 45, personId = 4, personLoyaltyDelta = -4, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("merchant", grievanceDelta = 5, loyaltyDelta = -2))),
            V3EventChoice("默许分润", "知李仲财办事能干必有分润，只做不知，商路果然更活，他也更为尽心。", silverDelta = 25, merchantsDelta = 6, cohesionDelta = -3, personId = 4, personMeritDelta = 3, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", wealthDelta = 5, grievanceDelta = -4), V3BranchImpact("main", grievanceDelta = 2)))
        )),
        V3ActiveEvent("济民义诊", "李济民请开义诊，救治流民与佃户。粮银虽耗，但人心可安。", listOf(
            V3EventChoice("开设义诊", "命李济民在医馆外设棚义诊，拨银拨粮供药，流民佃户排队求治。", silverDelta = -25, grainDelta = -15, villagersDelta = 9, cohesionDelta = 5, personId = 5, personFatigueDelta = 9, personMeritDelta = 4, siteId = "clinic", siteRiskDelta = -8, route = V3Route.Hermit),
            V3EventChoice("只治族人", "嘱医馆只治族人，外姓病患须另收费，乡民闻之失望摇头。", cohesionDelta = 2, villagersDelta = -4, personId = 5, personFatigueDelta = 3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("守砚求仕", "李守砚自言不甘困于县中，请求筹资拜访名士，为日后科举铺路。", listOf(
            V3EventChoice("资助拜师", "资助李守砚银三十五两，让他携礼拜访名士，为科举铺路。", silverDelta = -35, influenceDelta = 4, gentryDelta = 5, personId = 6, personFatigueDelta = 6, personMeritDelta = 3, route = V3Route.Scholar),
            V3EventChoice("令其静读", "嘱李守砚功名不可急求，命他在书院静读经义，勿妄生攀附之心。", cohesionDelta = 2, personId = 6, personLoyaltyDelta = 2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("阿衡顶撞", "李阿衡巡寨时与官差冲突，称差役勒索粮钱。县衙已派人问责。", listOf(
            V3EventChoice("护下阿衡", "称李阿衡性情刚直不藏私，是差役勒索在先，反写信向县衙陈情。", yamenDelta = -7, militiaDelta = 4, personId = 7, personLoyaltyDelta = 4, personMeritDelta = 2, route = V3Route.Fortress, branchImpacts = listOf(V3BranchImpact("martial", loyaltyDelta = 4, grievanceDelta = -4))),
            V3EventChoice("赔礼息事", "备银二十二两亲往县衙赔礼，责阿衡当面向官差赔罪，武支人人大为不满。", silverDelta = -22, yamenDelta = 6, personId = 7, personLoyaltyDelta = -3, route = V3Route.Loyalist, branchImpacts = listOf(V3BranchImpact("martial", grievanceDelta = 4)))
        )),
        V3ActiveEvent("环娘议价", "李环娘识破商帮压价，称若让她主谈，可多取三成利。", listOf(
            V3EventChoice("交由环娘", "将商帮谈判交由李环娘主持，她据理力争多取三成利，商帮也服其精明。", silverDelta = 55, merchantsDelta = 4, personId = 8, personFatigueDelta = 7, personMeritDelta = 4, route = V3Route.Merchant),
            V3EventChoice("稳价成交", "嘱李环娘见好就收按原议成交，少赚银三十两却维持了与商帮的关系。", silverDelta = 25, merchantsDelta = 7, personId = 8, personMeritDelta = 2, route = V3Route.Merchant)
        )),
        V3ActiveEvent("观潮听风", "李观潮从码头听来风声，海禁巡查将近，但南洋货路也在此时开价。", listOf(
            V3EventChoice("先撤暗货", "命李观潮立即撤走码头暗仓私货，虽亏了些利，却躲过海禁巡查。", silverDelta = -15, yamenDelta = 3, personId = 9, siteId = "dock", siteRiskDelta = -12, route = V3Route.Hermit),
            V3EventChoice("冒险出货", "李观潮言海禁未至正是出货良机，果然大赚银七十五两，只是风声日紧。", silverDelta = 75, yamenDelta = -8, merchantsDelta = 6, personId = 9, personFatigueDelta = 8, personMeritDelta = 4, siteId = "dock", siteRiskDelta = 8, route = V3Route.Overseas)
        )),
        V3ActiveEvent("采薇救童", "李采薇在医馆外救下一名病童，乡民感念，但她已疲惫不堪。", listOf(
            V3EventChoice("让她休养", "见李采薇已疲惫不堪，命她回去休养，另派人接手病童，她含泪谢恩。", cohesionDelta = 2, villagersDelta = 3, personId = 10, personFatigueDelta = -12, personLoyaltyDelta = 3, route = V3Route.Hermit),
            V3EventChoice("继续义诊", "夸李采薇仁心可嘉，命她继续在医馆义诊，乡民感念李氏恩德。", villagersDelta = 8, cohesionDelta = 3, personId = 10, personFatigueDelta = 12, personMeritDelta = 4, route = V3Route.Hermit)
        ))
    )

    val strategyEvents = listOf(
        V3ActiveEvent("辽饷加派", "县中传来新令，辽饷再加。各族皆惶惶，县令暗示李氏应先表态。", listOf(
            V3EventChoice("先缴一半", "先缴辽饷半数以示恭顺，又修书陈情县中民力已竭请求宽限，县令面色稍缓。", silverDelta = -45, grainDelta = -30, yamenDelta = 9, route = V3Route.Loyalist),
            V3EventChoice("联族缓缴", "暗中联络各族士绅联名请求缓缴，县令面色不善却也不敢犯众怒。", gentryDelta = 8, yamenDelta = -5, influenceDelta = 4, route = V3Route.Scholar)
        )),
        V3ActiveEvent("勤王檄文", "军镇传来勤王檄文，要求地方豪族输粮募勇。族中对是否响应争执不下。", listOf(
            V3EventChoice("输粮募勇", "响应勤王檄文，输粮六十石募勇十二名交军镇调遣，忠义之名渐起。", grainDelta = -60, militiaDelta = 12, garrisonDelta = 10, route = V3Route.Loyalist, routeDelta = 9),
            V3EventChoice("闭门自保", "称族中粮少丁稀难以响应，闭门修堡自保，军镇来人拂袖而去。", cohesionDelta = 4, garrisonDelta = -5, route = V3Route.Fortress, routeDelta = 7)
        )),
        V3ActiveEvent("清军风闻", "北地商旅称关外兵马日盛，南北商路皆有惊惧。宗族须提前考虑退路。", listOf(
            V3EventChoice("屯粮修堡", "命各庄囤粮、武支修堡，以备北地兵马南下时自保。", silverDelta = -35, grainDelta = -20, militiaDelta = 8, route = V3Route.Fortress, routeDelta = 8),
            V3EventChoice("筹备海路", "拨银五十两托海路支筹备南洋退路，多造船位以备不测。", silverDelta = -50, merchantsDelta = 7, route = V3Route.Overseas, routeDelta = 8)
        )),
        V3ActiveEvent("士绅联姻", "邻县士绅遣媒人问询，若结亲可入士林，也需厚礼。", listOf(
            V3EventChoice("厚礼联姻", "备厚礼七十两往邻县士绅家议亲，婚事若成，李氏便正式入了士林。", silverDelta = -70, gentryDelta = 12, influenceDelta = 7, route = V3Route.Scholar, routeDelta = 7),
            V3EventChoice("婉拒媒约", "以族中子弟尚幼为由婉拒联姻，保住银两却也断了入林捷径。", cohesionDelta = 2, gentryDelta = -3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("流寇招降", "山外流寇头目遣人来信，愿不扰李氏田庄，只求粮械暗助。", listOf(
            V3EventChoice("虚与委蛇", "派人与流寇头目虚与委蛇，暗送粮食三十石让他远离李氏田庄。", grainDelta = -30, banditsDelta = 8, yamenDelta = -7, route = V3Route.Warlord, routeDelta = 8),
            V3EventChoice("斩使示众", "将来使斩首号令于寨门外，流寇大恨，军镇与县衙却夸李氏忠义。", influenceDelta = 5, yamenDelta = 5, garrisonDelta = 5, banditsDelta = -8, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("商帮借贷", "商帮愿借银给李氏渡过难关，但要码头与集市分润。", listOf(
            V3EventChoice("接受借银", "接受商帮银一百二十两借贷，以码头集市分润为抵，商帮势力渐入族中。", silverDelta = 120, merchantsDelta = 10, cohesionDelta = -2, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", wealthDelta = 6, influenceDelta = 3))),
            V3EventChoice("拒绝牵制", "称李氏虽难也不受人牵制，商帮悻悻而去，族中却赞主房有骨气。", cohesionDelta = 4, merchantsDelta = -5, route = V3Route.Hermit)
        )),
        V3ActiveEvent("乡约重修", "士绅与乡民请求李氏牵头重修乡约，共定粮价、夜禁与赈济规条。", listOf(
            V3EventChoice("牵头乡约", "牵头召集士绅乡民重修乡约，共定粮价夜禁赈济规条，县中称善。", silverDelta = -25, villagersDelta = 8, gentryDelta = 6, influenceDelta = 4, route = V3Route.Scholar),
            V3EventChoice("只管族内", "称乡约乃县里事李氏只管族内，凝聚虽稳，地方上却少了李氏的声音。", cohesionDelta = 4, influenceDelta = -2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("县城戒严", "县城忽然戒严，传言有盗匪内应。商路断绝，县衙请各族协查。", listOf(
            V3EventChoice("协查内应", "命武支派熟面孔潜入县城协查内应，山道果然查获盗匪细作数名。", yamenDelta = 8, banditsDelta = -5, siteId = "mountain_pass", siteRiskDelta = -6, route = V3Route.Loyalist),
            V3EventChoice("护住商货", "命商支趁乱将县城商货转移至西河集市，赚银二十五两，却得罪了县衙。", silverDelta = 25, merchantsDelta = 7, yamenDelta = -4, route = V3Route.Merchant)
        ))
    )

    val routeEvents = listOf(
        V3ActiveEvent("耕读立名", "族中学子文章传入士林，若继续投入，李氏可走耕读门第。", listOf(
            V3EventChoice("刊刻族学文集", "拨银四十两请书院将族中学子文章刊刻成文集流传士林，李氏耕读之名渐起。", silverDelta = -40, influenceDelta = 8, gentryDelta = 8, route = V3Route.Scholar, routeDelta = 10),
            V3EventChoice("藏名避祸", "命学子收敛锋芒藏名于野，不参与讲会争鸣，反得几分清静。", cohesionDelta = 4, route = V3Route.Hermit, routeDelta = 7)
        )),
        V3ActiveEvent("商号合股", "商支建议合并族中铺面，设一总号，专营米布、药材与海货。", listOf(
            V3EventChoice("设立总号", "合族中铺面为一总号专营米布药材海货，商支大喜，银路大开。", silverDelta = -60, merchantsDelta = 10, route = V3Route.Merchant, routeDelta = 10, branchImpacts = listOf(V3BranchImpact("merchant", influenceDelta = 6, wealthDelta = 6))),
            V3EventChoice("分铺经营", "各铺面仍独立经营风险分散，年终反倒多了二十两盈馀。", cohesionDelta = 3, silverDelta = 20, route = V3Route.Hermit)
        )),
        V3ActiveEvent("堡寨盟约", "邻村请求与李氏共守山道，立堡寨盟约。", listOf(
            V3EventChoice("共守山道", "与邻村立堡寨盟约共守黑松山道，乡勇合练、警讯互通，声势大震。", silverDelta = -35, grainDelta = -30, militiaDelta = 15, villagersDelta = 7, route = V3Route.Fortress, routeDelta = 10),
            V3EventChoice("只守本族", "称各族自保即可不与邻村盟约，邻村失望，李氏也少了外援。", cohesionDelta = 3, villagersDelta = -3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("地方军议", "军镇把总愿与李氏结盟，共防流寇。若成，宗族将深涉兵事。", listOf(
            V3EventChoice("结军镇盟", "与军镇把总歃血为盟共防流寇，李氏从此深涉兵事。", silverDelta = -40, militiaDelta = 18, garrisonDelta = 10, route = V3Route.Warlord, routeDelta = 10),
            V3EventChoice("仅送粮草", "只送粮草四十五石与军镇示好，不深结军盟，勤王路线略有推进。", grainDelta = -45, garrisonDelta = 8, yamenDelta = 4, route = V3Route.Loyalist, routeDelta = 8)
        )),
        V3ActiveEvent("迁海密议", "海路支请将一批族人和银两送往南洋，作为乱世退路。", listOf(
            V3EventChoice("分支远渡", "密令海路支筹备分支远渡南洋，拨银九十两造办船只货物。", silverDelta = -90, cohesionDelta = -3, merchantsDelta = 8, route = V3Route.Overseas, routeDelta = 12, branchImpacts = listOf(V3BranchImpact("sea", influenceDelta = 7, wealthDelta = 5))),
            V3EventChoice("暂缓迁徙", "称迁徙风险太大暂缓，海路支虽不满，族中却松了一口气。", cohesionDelta = 4, route = V3Route.Hermit)
        )),
        V3ActiveEvent("闭乡避祸", "族老建议减少外争，修祠屯粮，谨慎度过乱世。", listOf(
            V3EventChoice("闭乡修约", "颁行闭乡族约：减少外争、屯粮修祠、谨慎度日，族人皆从。", grainDelta = -20, cohesionDelta = 8, villagersDelta = 5, route = V3Route.Hermit, routeDelta = 10),
            V3EventChoice("仍逐外势", "不听族老之言仍逐外势交结县衙，声望虽涨，祸端也埋下了。", influenceDelta = 5, yamenDelta = 3, route = V3Route.Loyalist)
        ))
    )

    private data class PersonArcSeed(
        val personId: Int,
        val name: String,
        val branchId: String,
        val siteId: String,
        val route: V3Route,
        val duty: String,
        val pressure: String
    )

    private val personArcSeeds = listOf(
        PersonArcSeed(1, "开族祖", "main", "shrine", V3Route.Hermit, "整肃族规", "主房权威与诸房利益难以两全"),
        PersonArcSeed(2, "李承岳", "martial", "fort", V3Route.Fortress, "操练乡勇", "武备扩张会招来县衙猜忌"),
        PersonArcSeed(3, "李若兰", "scholar", "academy", V3Route.Scholar, "主持讲会", "士林声名越高，门户争论牵连越深"),
        PersonArcSeed(4, "李仲财", "merchant", "market", V3Route.Merchant, "重整商号", "银钱流转越快，账目越难服众"),
        PersonArcSeed(5, "李济民", "second", "clinic", V3Route.Hermit, "救治乡里", "仁名能安民，也会耗尽药粮"),
        PersonArcSeed(6, "李守砚", "scholar", "academy", V3Route.Scholar, "备考乡试", "少年求名太急，容易被士林利用"),
        PersonArcSeed(7, "李阿衡", "martial", "mountain_pass", V3Route.Warlord, "巡山缉盗", "刚烈可震匪，也可能激化仇杀"),
        PersonArcSeed(8, "李环娘", "merchant", "market", V3Route.Merchant, "议价盘账", "精明能聚财，也会触动旧账"),
        PersonArcSeed(9, "李观潮", "sea", "dock", V3Route.Overseas, "经营码头", "海路越深，海禁风声越近"),
        PersonArcSeed(10, "李采薇", "second", "clinic", V3Route.Hermit, "随师施药", "医者仁心与族中资源常有冲突")
    )

    val advancedSiteEvents = listOf(
        V3ActiveEvent("宗祠夜火", "祠堂偏殿夜里走水，族谱箱差点被焚。族老怀疑有人借乱遮掩旧账。", listOf(
            V3EventChoice("彻查守祠人", "当夜彻查守祠人，封锁偏殿火场，查出有人借乱遮掩旧账的痕迹。", silverDelta = -18, influenceDelta = 4, siteId = "shrine", siteControlDelta = 6, siteRiskDelta = -12, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 3), V3BranchImpact("second", grievanceDelta = 2))),
            V3EventChoice("压下不查", "称只是烛火不慎走水，赏了守祠人几两银子压下此事，旧账隐患却留了下来。", cohesionDelta = -3, siteId = "shrine", siteRiskDelta = 8, route = V3Route.Hermit)
        )),
        V3ActiveEvent("族学缺师", "族中蒙童渐多，旧师年老，请新师需厚礼，若不请则书香支不满。", listOf(
            V3EventChoice("请塾师入祠", "备厚礼三十二两请新塾师入祠授蒙，书香支满意，蒙童也有了先生。", silverDelta = -32, influenceDelta = 4, gentryDelta = 4, siteId = "shrine", siteControlDelta = 5, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("scholar", grievanceDelta = -3, influenceDelta = 3))),
            V3EventChoice("暂由族老代课", "暂请族老中识字者代课，省了银两，只是蒙童学问长进有限。", cohesionDelta = 2, siteId = "shrine", route = V3Route.Hermit)
        )),
        V3ActiveEvent("田契水印", "南乡旧田契受潮，几处边界字迹模糊，邻村趁机索地。", listOf(
            V3EventChoice("重摹田契", "请书手重摹受潮田契，又请里正见证画押，田界得以保全。", silverDelta = -24, grainDelta = 18, siteId = "farmland", siteControlDelta = 8, siteRiskDelta = -6, route = V3Route.Hermit),
            V3EventChoice("邀官丈量", "请县衙书吏到场重新丈量田界，虽损了些田地，却换来了官契保障。", grainDelta = -20, yamenDelta = 5, siteId = "farmland", siteRiskDelta = -8, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("蝗影初现", "田边见到蝗群，佃户惶恐。若提前扑杀，可免大灾。", listOf(
            V3EventChoice("组织扑蝗", "命各庄佃户齐集田边扑杀蝗群，又挖沟拦截，虽损粮二十五石，大灾却免了。", grainDelta = -25, villagersDelta = 6, siteId = "farmland", siteRiskDelta = -18, route = V3Route.Hermit),
            V3EventChoice("祈雨观望", "命人设坛祈雨等蝗群自去，花了八两香火钱，蝗群却越聚越多。", silverDelta = -8, cohesionDelta = 1, siteId = "farmland", siteRiskDelta = 10, route = V3Route.Hermit)
        )),
        V3ActiveEvent("集市斗殴", "米铺与布商因价税斗殴，商帮请李氏出面平事。", listOf(
            V3EventChoice("设行规仲裁", "在集市设行规仲裁，命商帮各铺立契为凭，此后争斗少了许多。", influenceDelta = 3, merchantsDelta = 5, siteId = "market", siteControlDelta = 7, siteRiskDelta = -8, route = V3Route.Merchant),
            V3EventChoice("交县衙处置", "将斗殴双方绑送县衙处置，商帮嫌李氏不肯庇护，县衙倒夸李氏守规矩。", yamenDelta = 5, merchantsDelta = -4, siteId = "market", siteRiskDelta = -5, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("假银流入", "集市出现成色不足的碎银，若不清查，宗族商号信誉受损。", listOf(
            V3EventChoice("清查银色", "命账房与牙人逐笔清查银色成色，又铸了官秤公示，假银渐绝。", silverDelta = -20, merchantsDelta = 5, siteId = "market", siteControlDelta = 5, siteRiskDelta = -9, route = V3Route.Merchant),
            V3EventChoice("照收快兑", "命各铺照收碎银只尽快兑出，短期赚了三十四两，商号信誉却渐渐坏了。", silverDelta = 34, influenceDelta = -2, siteId = "market", siteRiskDelta = 8, route = V3Route.Merchant)
        )),
        V3ActiveEvent("县衙换吏", "县衙新到书吏不熟本地旧例，暗示各族重新打点。", listOf(
            V3EventChoice("送礼立案", "备礼三十八两往县衙拜会新书吏，立案递帖，日后办事不再刁难。", silverDelta = -38, yamenDelta = 8, siteId = "yamen", siteControlDelta = 5, route = V3Route.Loyalist),
            V3EventChoice("持旧例抗辩", "持本县旧例与新书吏抗辩，士绅赞李氏有骨气，新书吏却记了仇。", gentryDelta = 5, yamenDelta = -5, influenceDelta = 3, siteId = "yamen", route = V3Route.Scholar)
        )),
        V3ActiveEvent("差役勒索", "差役借催税之名勒索佃户，乡民怨声传至祠堂。", listOf(
            V3EventChoice("代民呈状", "替佃户代写状纸往县丞处控告差役勒索，乡民欢呼，差役却恨上了李氏。", villagersDelta = 8, yamenDelta = -4, influenceDelta = 4, siteId = "yamen", siteRiskDelta = 5, route = V3Route.Scholar),
            V3EventChoice("私下打点", "备银三十两私下打点差役头目，请他手下留情，勒索稍减。", silverDelta = -30, yamenDelta = 4, villagersDelta = 2, siteId = "yamen", siteRiskDelta = -8, route = V3Route.Hermit)
        )),
        V3ActiveEvent("书院藏书", "旧家藏书愿售于李氏，若购入可兴族学。", listOf(
            V3EventChoice("购入藏书", "拨银四十二两购入旧家藏书数百卷藏于书院，士子纷纷来借阅。", silverDelta = -42, gentryDelta = 6, influenceDelta = 4, siteId = "academy", siteControlDelta = 8, route = V3Route.Scholar),
            V3EventChoice("借阅抄录", "只花十二两借阅费，命族中学子抄录重要书卷，进展虽慢却也有收获。", silverDelta = -12, influenceDelta = 2, siteId = "academy", siteControlDelta = 3, route = V3Route.Scholar)
        )),
        V3ActiveEvent("医馆染疫", "医馆收治病患过多，药童亦有染疫迹象。", listOf(
            V3EventChoice("闭馆消杀", "当机立断命医馆闭馆消杀，病患转移至临时棚，虽有民怨，疫病却止住了。", silverDelta = -18, villagersDelta = -3, siteId = "clinic", siteRiskDelta = -18, route = V3Route.Hermit),
            V3EventChoice("扩棚分诊", "拨银拨粮扩建诊棚分轻重分诊，又多请郎中药童，民心大安。", silverDelta = -42, grainDelta = -18, villagersDelta = 9, siteId = "clinic", siteControlDelta = 5, siteRiskDelta = -10, route = V3Route.Hermit)
        )),
        V3ActiveEvent("堡墙裂缝", "北山寨堡旧墙雨后开裂，若不修补，夜巡难以安心。", listOf(
            V3EventChoice("修补堡墙", "请工匠修补堡墙裂缝，又加高了望楼，寨上守备更稳，乡勇安心。", silverDelta = -36, grainDelta = -12, militiaDelta = 5, siteId = "fort", siteControlDelta = 9, siteRiskDelta = -12, route = V3Route.Fortress),
            V3EventChoice("只添夜巡", "不修道只添夜巡人数，乡勇日夜值守疲惫不堪，武支多有怨言。", militiaDelta = 3, siteId = "fort", siteRiskDelta = -4, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact("martial", grievanceDelta = 3)))
        )),
        V3ActiveEvent("码头税卡", "县衙欲在三江码头新设税卡，商支与海路支均忧心生计。", listOf(
            V3EventChoice("协商减税", "拨银三十五两与县衙协商，税卡税率稍减，码头生意得以维持。", silverDelta = -35, yamenDelta = 4, merchantsDelta = 5, siteId = "dock", siteRiskDelta = -8, route = V3Route.Merchant),
            V3EventChoice("暗走水路", "命海路支暗走偏僻水路避税，赚银四十五两，却也让县衙盯上了李氏。", silverDelta = 45, yamenDelta = -7, merchantsDelta = 4, siteId = "dock", siteControlDelta = 5, siteRiskDelta = 8, route = V3Route.Overseas)
        )),
        V3ActiveEvent("山道塌方", "黑松山道雨后塌方，商队绕路，流寇也可能借机设伏。", listOf(
            V3EventChoice("雇工修道", "请石匠雇工抢修山道，商路渐通，匪寇也少了设伏之机。", silverDelta = -28, grainDelta = -15, merchantsDelta = 3, siteId = "mountain_pass", siteControlDelta = 6, siteRiskDelta = -15, route = V3Route.Merchant),
            V3EventChoice("设哨观望", "命乡勇于山道两端设哨警戒不急于修路，匪情稍知，商路却仍堵塞。", militiaDelta = 4, banditsDelta = -4, siteId = "mountain_pass", siteRiskDelta = -7, route = V3Route.Fortress)
        )),
        V3ActiveEvent("私铸兵器", "山道铁匠暗中为寨丁修刀，若纵容可强武备，若上报可保清白。", listOf(
            V3EventChoice("暗助修刀", "暗中资助山道铁匠修刀，寨丁兵器渐齐，武支士气大振。", silverDelta = -22, militiaDelta = 12, yamenDelta = -6, siteId = "mountain_pass", siteControlDelta = 5, route = V3Route.Warlord),
            V3EventChoice("上报县衙", "将私铸兵器之事上报县衙，差役拿了铁匠问罪，武支恨主房坏了大事。", yamenDelta = 7, militiaDelta = -3, siteId = "yamen", route = V3Route.Loyalist, branchImpacts = listOf(V3BranchImpact("martial", grievanceDelta = 4)))
        ))
    )

    val advancedPersonEvents = personArcSeeds.flatMap { seed ->
        listOf(
            V3ActiveEvent("${seed.name}请命", "${seed.name}请求亲自${seed.duty}。${seed.pressure}，族中等你定夺。", listOf(
                V3EventChoice("准其主事", "许${seed.name}亲自${seed.duty}，他领命而去日夜操劳，功绩渐显。", silverDelta = -12, influenceDelta = 2, personId = seed.personId, personFatigueDelta = 8, personMeritDelta = 5, siteId = seed.siteId, siteControlDelta = 5, siteRiskDelta = -5, route = seed.route, branchImpacts = listOf(V3BranchImpact(seed.branchId, influenceDelta = 3, grievanceDelta = -2))),
                V3EventChoice("留中观望", "嘱${seed.name}再候时机暂不允准，他心下稍安，忠诚更固。", cohesionDelta = 2, personId = seed.personId, personLoyaltyDelta = 2, route = V3Route.Hermit)
            )),
            V3ActiveEvent("${seed.name}受议", "宗祠中有人质疑${seed.name}近来行事过急，若不表态，相关房支会继续争论。", listOf(
                V3EventChoice("公开支持", "当众表态支持${seed.name}，相关房支面露喜色，他也更为尽心。", cohesionDelta = 2, personId = seed.personId, personLoyaltyDelta = 4, route = seed.route, branchImpacts = listOf(V3BranchImpact(seed.branchId, loyaltyDelta = 4, grievanceDelta = -4))),
                V3EventChoice("按族规责备", "依族规责备${seed.name}行事过急，他低头受教，锐气略挫。", influenceDelta = 3, personId = seed.personId, personLoyaltyDelta = -3, personMeritDelta = -1, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact(seed.branchId, grievanceDelta = 4)))
            )),
            V3ActiveEvent("${seed.name}暗访", "${seed.name}愿夜访${seed.siteId}一带，查明地方传言。此举或有收获，也有风险。", listOf(
                V3EventChoice("拨人随行", "拨了几名可靠人手随${seed.name}暗访，所得情报甚详，风险也降了。", silverDelta = -16, personId = seed.personId, personFatigueDelta = 10, personMeritDelta = 4, siteId = seed.siteId, siteRiskDelta = -10, route = seed.route),
                V3EventChoice("独自前往", "让${seed.name}独自前往查访，他孤身夜行，疲惫更甚。", personId = seed.personId, personFatigueDelta = 15, personMeritDelta = 5, siteId = seed.siteId, siteRiskDelta = -5, route = seed.route)
            )),
            V3ActiveEvent("${seed.name}收徒", "有族中后进愿拜${seed.name}为师。收徒可传本事，也会分散精力。", listOf(
                V3EventChoice("准其收徒", "准${seed.name}收族中后进为徒，本事有了传人，路线也多了助力。", grainDelta = -8, influenceDelta = 3, personId = seed.personId, personFatigueDelta = 5, route = seed.route, routeDelta = 6, branchImpacts = listOf(V3BranchImpact(seed.branchId, influenceDelta = 2))),
                V3EventChoice("先办正事", "嘱${seed.name}先办好眼前正事，收徒之事日后再议，他稍感释然。", cohesionDelta = 2, personId = seed.personId, personFatigueDelta = -5, route = V3Route.Hermit)
            )),
            V3ActiveEvent("${seed.name}家书", "${seed.name}呈上一封家书，言及${seed.pressure}。若回应得当，可稳住其心。", listOf(
                V3EventChoice("亲笔回书", "亲笔回书劝慰${seed.name}，他捧书感念，忠心更固，族心也稳。", cohesionDelta = 3, personId = seed.personId, personLoyaltyDelta = 5, route = V3Route.Hermit),
                V3EventChoice("交房支处理", "将${seed.name}所陈之事交房支自行处置，他领命而去，主房权柄略疏。", personId = seed.personId, personMeritDelta = 2, route = seed.route, branchImpacts = listOf(V3BranchImpact(seed.branchId, influenceDelta = 3, wealthDelta = 1), V3BranchImpact("main", grievanceDelta = 2)))
            ))
        )
    }

    val branchEvents = listOf(
        V3ActiveEvent("主房议嗣", "主房族老要求明确下一任宗祠管事，以免乱世中号令不一。", listOf(
            V3EventChoice("立主房管事", "按主房族老所请立主房管事为下一任宗祠管事，诸房虽有戒心，号令却一。", cohesionDelta = 2, influenceDelta = 3, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 5, loyaltyDelta = 3), V3BranchImpact("merchant", grievanceDelta = 2), V3BranchImpact("martial", grievanceDelta = 2))),
            V3EventChoice("设轮值议事", "设各房轮值议事之制，怨气消了，遇事却总议而不决。", cohesionDelta = 5, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", grievanceDelta = 2), V3BranchImpact("second", grievanceDelta = -3), V3BranchImpact("scholar", grievanceDelta = -2)))
        )),
        V3ActiveEvent("二房请赈", "二房认为族中应优先救济佃户和病患，否则民心将散。", listOf(
            V3EventChoice("拨粮赈济", "准二房所请拨粮四十二石赈济佃户病患，二房感激，民心渐归。", grainDelta = -42, villagersDelta = 9, cohesionDelta = 4, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("second", loyaltyDelta = 5, grievanceDelta = -5))),
            V3EventChoice("只救族内", "只拨粮十二石救济族中贫困者，外姓佃户病患不管，二房大为不满。", grainDelta = -12, cohesionDelta = 2, villagersDelta = -3, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("second", grievanceDelta = 5)))
        )),
        V3ActiveEvent("商支扩铺", "商支请以族产入股新铺，声称可解银荒。主房担心商支坐大。", listOf(
            V3EventChoice("准其扩铺", "准商支以族产入股新铺，商路大盛银钱入袋，商支势力也水涨船高。", silverDelta = 85, merchantsDelta = 7, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", wealthDelta = 7, influenceDelta = 5, grievanceDelta = -4))),
            V3EventChoice("限制分润", "限定新铺分润比例族产占大头，商支怨气上涌，办事也不如从前尽心。", influenceDelta = 3, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("merchant", grievanceDelta = 7, loyaltyDelta = -3), V3BranchImpact("main", influenceDelta = 2)))
        )),
        V3ActiveEvent("武支请械", "武支要求添置长枪弓弩，称山道不靖，不能空手守族。", listOf(
            V3EventChoice("购置军械", "拨银五十五两购置长枪弓弩，武支人人大喜，日夜操练。", silverDelta = -55, militiaDelta = 18, route = V3Route.Fortress, branchImpacts = listOf(V3BranchImpact("martial", loyaltyDelta = 5, influenceDelta = 5, grievanceDelta = -5))),
            V3EventChoice("严禁私械", "重申族规严禁私置军械，武支怒不敢言，乡勇士气低落。", yamenDelta = 5, militiaDelta = -2, route = V3Route.Loyalist, branchImpacts = listOf(V3BranchImpact("martial", grievanceDelta = 8, loyaltyDelta = -3)))
        )),
        V3ActiveEvent("书香支请学田", "书香支希望划出学田供族学长久运转。", listOf(
            V3EventChoice("划拨学田", "划出学田二十八石专供族学运转，书香支满意，士林声望也涨了。", grainDelta = -28, influenceDelta = 5, gentryDelta = 6, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("scholar", loyaltyDelta = 4, influenceDelta = 5, grievanceDelta = -5))),
            V3EventChoice("暂缓学田", "称学田之事待丰年再议，仓中余粮十二石入了公账，书香支失望。", grainDelta = 12, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("scholar", grievanceDelta = 5)))
        )),
        V3ActiveEvent("海路支求船", "海路支称若置一条江船，便可通商避乱。族中疑其私心。", listOf(
            V3EventChoice("置船试航", "拨银八十两置江船一条命海路支试航，海路支欢欣鼓舞。", silverDelta = -80, merchantsDelta = 8, route = V3Route.Overseas, routeDelta = 10, branchImpacts = listOf(V3BranchImpact("sea", wealthDelta = 6, influenceDelta = 5, grievanceDelta = -4))),
            V3EventChoice("只许租船", "只许海路支租船运货不许置产，省了银两，海路支却觉得处处掣肘。", silverDelta = -25, merchantsDelta = 3, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("sea", grievanceDelta = 2)))
        )),
        V3ActiveEvent("房支分粮", "秋收后各房争论分粮比例。若偏向一方，必伤另一方。", listOf(
            V3EventChoice("按丁均分", "命按丁口均分秋粮，贫房大喜，富房却觉得吃亏。", cohesionDelta = 5, grainDelta = -10, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("merchant", grievanceDelta = 3), V3BranchImpact("second", loyaltyDelta = 3))),
            V3EventChoice("按产分成", "按各房产业比例分成，商支满意进银二十两，贫房却愤愤不平。", silverDelta = 20, cohesionDelta = -3, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", loyaltyDelta = 3), V3BranchImpact("second", grievanceDelta = 4)))
        )),
        V3ActiveEvent("祠产审计", "族老提议清查祠产、铺账和田租。有人赞成，有人害怕旧账被翻。", listOf(
            V3EventChoice("公开审计", "命账房当众审计祠产铺账田租，追回银粮不少，涉事房支脸上无光。", silverDelta = 65, grainDelta = 25, influenceDelta = 3, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 3), V3BranchImpact("merchant", grievanceDelta = 4))),
            V3EventChoice("只查新账", "只查近一年新账旧账不动，风波不大，追回的银两也有限。", silverDelta = 22, cohesionDelta = 2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("族老病重", "一位德高望重的族老病重，各房都在等他最后一句话。", listOf(
            V3EventChoice("厚礼延医", "不惜银钱厚礼延请名医救治族老，日夜派人服侍，族中皆称李氏仁厚。", silverDelta = -40, cohesionDelta = 6, villagersDelta = 3, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", loyaltyDelta = 2), V3BranchImpact("second", influenceDelta = 2))),
            V3EventChoice("简办后事", "称族老年事已高不宜折腾，只备下后事银八两，族人觉得人情淡薄。", silverDelta = -8, cohesionDelta = -3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("庶支入谱", "外迁庶支携银归宗，请求重入族谱。主房担心谱系混乱。", listOf(
            V3EventChoice("验明入谱", "验明庶支身份后准其重入族谱，又收了他们捐银五十五两，一举两得。", silverDelta = 55, cohesionDelta = 4, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", grievanceDelta = 2), V3BranchImpact("second", loyaltyDelta = 3))),
            V3EventChoice("拒其入谱", "称谱系不可乱拒庶支入谱，主房满意，却断了一支助力。", influenceDelta = 2, cohesionDelta = -4, route = V3Route.Hermit)
        )),
        V3ActiveEvent("强房逼议", "强势房支要求在宗祠议事中增加席位，否则拒绝出银出丁。", listOf(
            V3EventChoice("增设席位", "于议事堂增设强房席位，怨气消了，主房的话却不如从前管用了。", cohesionDelta = 4, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = -2, grievanceDelta = 3), V3BranchImpact("merchant", grievanceDelta = -3), V3BranchImpact("martial", grievanceDelta = -3))),
            V3EventChoice("维持旧制", "严词拒绝维持旧制，主房威严更甚，强房却积怨更深。", influenceDelta = 3, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 4), V3BranchImpact("merchant", grievanceDelta = 4), V3BranchImpact("martial", grievanceDelta = 4)))
        )),
        V3ActiveEvent("孤支求养", "一支孤弱族人无力过冬，请求宗祠拨粮。", listOf(
            V3EventChoice("宗祠赡养", "将孤支族人接入宗祠赡养，拨粮二十五石，孤支感恩涕零。", grainDelta = -25, cohesionDelta = 5, villagersDelta = 2, route = V3Route.Hermit),
            V3EventChoice("交邻房照看", "将孤支交邻房照看只拨粮八石，邻房觉得负担，孤支也未必尽心。", grainDelta = -8, cohesionDelta = 2, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("second", grievanceDelta = 2, loyaltyDelta = 2)))
        )),
        V3ActiveEvent("族中私塾", "几房合议开设私塾，但谁出钱、谁掌教又起争执。", listOf(
            V3EventChoice("宗祠出资", "由宗祠出资四十五两开设私塾请先生授蒙，书香支大为满意。", silverDelta = -45, influenceDelta = 5, gentryDelta = 5, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("scholar", influenceDelta = 4, grievanceDelta = -4))),
            V3EventChoice("各房自办", "各房自行请先生只花了十二两，族中蒙学却散乱无序。", silverDelta = -12, cohesionDelta = -2, route = V3Route.Scholar)
        )),
        V3ActiveEvent("嫁娶争礼", "族中婚礼礼金规格引发争议，富房要体面，贫房怕负担。", listOf(
            V3EventChoice("降低礼制", "布告降低婚礼仪制，贫房不再怕负担，富房觉得丢了体面。", cohesionDelta = 5, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("second", loyaltyDelta = 3), V3BranchImpact("merchant", grievanceDelta = 3))),
            V3EventChoice("维持体面", "维持旧有婚礼仪制花银二十两办得体面，贫房却叫苦不迭。", silverDelta = -20, influenceDelta = 4, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("merchant", loyaltyDelta = 2), V3BranchImpact("second", grievanceDelta = 4)))
        )),
        V3ActiveEvent("族规重罚", "有人私卖族田被抓，各房都在看宗祠如何惩戒。", listOf(
            V3EventChoice("重罚立威", "私卖族田者杖责三十、追回田产、逐出族中三年，诸房凛然。", influenceDelta = 5, cohesionDelta = -3, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 4))),
            V3EventChoice("罚银留人", "罚银三十五两追回损失不逐其人，族中撕裂稍减，威严却差了。", silverDelta = 35, cohesionDelta = 2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("祠堂借贷", "商支愿借银给宗祠应急，但要求来年田租优先偿还。", listOf(
            V3EventChoice("接受借贷", "接受商支银一百两借贷应急，来年田租优先偿还，商支在族中势力大涨。", silverDelta = 100, cohesionDelta = -2, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", wealthDelta = 8, influenceDelta = 4))),
            V3EventChoice("拒绝借贷", "宁肯吃紧也不借商支的钱，商支失望，族中却赞主房有志气。", cohesionDelta = 3, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("merchant", grievanceDelta = 3)))
        )),
        V3ActiveEvent("武支护院", "武支提出常驻护院，主房担心祠堂变成军营。", listOf(
            V3EventChoice("准许护院", "准武支常驻护院配器械轮值祠中，武支势力渐入宗祠，护院确实安心。", militiaDelta = 10, banditsDelta = -4, route = V3Route.Fortress, branchImpacts = listOf(V3BranchImpact("martial", influenceDelta = 5, grievanceDelta = -3))),
            V3EventChoice("限定轮值", "只许武支每月轮值十日人数不逾十，各房平衡，护院效果有限。", cohesionDelta = 3, militiaDelta = 3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("海路分宗", "海路支建议在码头另立小祠，便于远行族人祭祖。", listOf(
            V3EventChoice("准立小祠", "准海路支在码头另立小祠，拨银三十五两，远行族人有了祭祖之所。", silverDelta = -35, cohesionDelta = -1, route = V3Route.Overseas, routeDelta = 8, branchImpacts = listOf(V3BranchImpact("sea", influenceDelta = 6, loyaltyDelta = 4, grievanceDelta = -4))),
            V3EventChoice("祖祠不可分", "称祖祠不可分祠，海路支虽失望，主房威严却稳如磐石。", influenceDelta = 3, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 3), V3BranchImpact("sea", grievanceDelta = 6)))
        )),
        V3ActiveEvent("书香支讲会", "书香支要求宗族在县中公开表态反对苛派。", listOf(
            V3EventChoice("附和族中公议", "附和书香支族中公议联名布告反对苛派，士林拍手称快，县衙却记了账。", influenceDelta = 6, gentryDelta = 8, yamenDelta = -6, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("scholar", influenceDelta = 5, grievanceDelta = -3))),
            V3EventChoice("不涉公议", "嘱书香支讲会不谈县中公事，官府关系稳定，书香支却觉得李氏懦弱。", yamenDelta = 4, cohesionDelta = 1, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("scholar", grievanceDelta = 5)))
        )),
        V3ActiveEvent("商支赈粥", "商支愿出钱设粥棚，但要在粥棚挂自家商号名。", listOf(
            V3EventChoice("准挂商号", "准商支设粥棚挂商号名赈济，乡民领粥皆称商号仁义，商支出了名。", villagersDelta = 8, cohesionDelta = 3, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", influenceDelta = 5, grievanceDelta = -3))),
            V3EventChoice("只挂宗祠名", "粥棚只挂宗祠名，花银二十五两宗族声望涨了，商支却白忙一场。", silverDelta = -25, influenceDelta = 5, villagersDelta = 5, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("merchant", grievanceDelta = 4)))
        )),
        V3ActiveEvent("二房药田", "二房请求把一块薄田改种药材，为医馆长用。", listOf(
            V3EventChoice("改作药田", "准二房所请将薄田改种药材供医馆之用，二房精心打理，药源渐足。", grainDelta = -18, villagersDelta = 5, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("second", wealthDelta = 3, influenceDelta = 4, grievanceDelta = -4))),
            V3EventChoice("仍种粮食", "命薄田仍种粮食不许改种，粮仓多了二十五石，二房怏怏不乐。", grainDelta = 25, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("second", grievanceDelta = 4)))
        )),
        V3ActiveEvent("主房藏契", "有人传言主房藏有几张旧契，若公开可平息争议，也会削弱主房权柄。", listOf(
            V3EventChoice("公开旧契", "命主房将旧契当众公开，几处争议田产由此厘清，主房权柄却被削弱。", cohesionDelta = 6, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = -3, grievanceDelta = 4), V3BranchImpact("second", grievanceDelta = -3))),
            V3EventChoice("封存旧契", "命旧契封存不得公开，主房稳住了权柄，诸房猜疑却日甚一日。", influenceDelta = 3, cohesionDelta = -3, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 4), V3BranchImpact("merchant", grievanceDelta = 3), V3BranchImpact("scholar", grievanceDelta = 3)))
        ))
    )

    val advancedStrategyEvents = listOf(
        V3ActiveEvent("巡抚清丈", "上官要清丈田亩，隐田旧账将无处可藏。", listOf(
            V3EventChoice("主动报田", "主动向巡抚衙门呈报田亩，虽补了银粮，却换来官府嘉奖。", silverDelta = -50, grainDelta = -20, yamenDelta = 10, influenceDelta = 2, route = V3Route.Loyalist),
            V3EventChoice("联绅缓丈", "联络县中士绅联名请求缓丈花银打点，士绅感李氏牵头之德。", silverDelta = -25, gentryDelta = 9, yamenDelta = -6, route = V3Route.Scholar)
        )),
        V3ActiveEvent("边饷催急", "边地战事吃紧，县中再次催饷。", listOf(
            V3EventChoice("输银买安", "输银七十五两助边饷，军镇修书致谢，县令也夸李氏知大礼。", silverDelta = -75, yamenDelta = 10, garrisonDelta = 4, route = V3Route.Loyalist),
            V3EventChoice("称灾缓缴", "以今年灾情为由请求缓缴只缴粮十五石，县衙把李氏记上了黑名单。", grainDelta = -15, yamenDelta = -8, villagersDelta = 3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("流民成寨", "县外流民聚成小寨，既可招抚为佃，也可能变成匪患。", listOf(
            V3EventChoice("招为佃户", "派人前往流民寨招抚，愿为佃户者编入田庄供粮，流民大喜来归。", grainDelta = -45, villagersDelta = 10, cohesionDelta = 3, route = V3Route.Hermit),
            V3EventChoice("驱散小寨", "命武支出勇驱散流民寨，流民四散而去，匪患隐患消了民心却失了。", militiaDelta = -5, banditsDelta = -6, villagersDelta = -7, route = V3Route.Fortress)
        )),
        V3ActiveEvent("盐课风波", "盐税新规传至县中，商帮请李氏代为说项。", listOf(
            V3EventChoice("替商帮说项", "受商帮之托往县衙说项请宽盐税新规，商帮感李氏出面之德。", silverDelta = -20, merchantsDelta = 10, yamenDelta = -3, route = V3Route.Merchant),
            V3EventChoice("避开盐事", "称盐税乃朝廷法度李氏不便置喙，商帮失望，县衙倒觉得李氏识趣。", yamenDelta = 3, merchantsDelta = -5, cohesionDelta = 2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("瘟疫封村", "邻村爆发疫病，县衙考虑封村。族中有人亲眷在内。", listOf(
            V3EventChoice("送药入村", "拨银拨粮命医馆送药入疫村，又派李济民亲自坐镇，村民感泣。", silverDelta = -45, grainDelta = -20, villagersDelta = 12, route = V3Route.Hermit),
            V3EventChoice("配合封锁", "全力配合县衙封村之令不送药不探问，县衙满意村民却寒了心。", yamenDelta = 7, villagersDelta = -5, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("乡兵点验", "县衙要求点验各族乡兵，武备过多会被猜忌。", listOf(
            V3EventChoice("如实点验", "遵命如实点验乡勇造册上报，官府夸奖李氏恭顺，割据路线却受阻。", yamenDelta = 8, militiaDelta = -6, route = V3Route.Loyalist),
            V3EventChoice("分散藏兵", "命武支将多余乡勇分散于各庄隐藏，点验时只报半数，保留了武备。", yamenDelta = -8, militiaDelta = 8, route = V3Route.Warlord, routeDelta = 8)
        )),
        V3ActiveEvent("海禁严查", "巡海文书下县，码头商旅人人自危。", listOf(
            V3EventChoice("暂停海货", "命海路支暂停海货贸易，码头损失惨重，却躲过了巡查。", silverDelta = -35, yamenDelta = 6, merchantsDelta = -4, route = V3Route.Hermit),
            V3EventChoice("转走暗仓", "命海路支将海货转入暗仓绕道出货，大赚银四十五两，海禁风声却日紧。", silverDelta = 45, yamenDelta = -8, merchantsDelta = 6, route = V3Route.Overseas, routeDelta = 8)
        )),
        V3ActiveEvent("军镇借粮", "过境军镇请借粮三百石，承诺来日偿还。", listOf(
            V3EventChoice("借粮结军", "借粮三百石与军镇，军镇千总亲书借据来日偿还，关系大进。", grainDelta = -80, garrisonDelta = 12, route = V3Route.Loyalist),
            V3EventChoice("只供一半", "只供粮一百五十石，既保住了粮仓，也不至于得罪军镇太深。", grainDelta = -35, garrisonDelta = 5, cohesionDelta = 2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("县令更替", "旧县令调任，新县令未明亲疏。各族争先递帖。", listOf(
            V3EventChoice("厚礼递帖", "备厚礼六十两往拜新县令，又托人引荐，新县令对李氏印象颇佳。", silverDelta = -60, yamenDelta = 12, influenceDelta = 2, route = V3Route.Loyalist),
            V3EventChoice("由士绅引荐", "请县中士绅代为引荐新县令花费较少，也借士绅之桥搭上了关系。", silverDelta = -25, gentryDelta = 8, yamenDelta = 4, route = V3Route.Scholar)
        )),
        V3ActiveEvent("米船被扣", "县外米船被扣，商帮要李氏出面担保。", listOf(
            V3EventChoice("出面担保", "李氏出面担保米船无违禁物，米船放行进港粮五十石入仓，商帮大喜。", silverDelta = -30, grainDelta = 50, merchantsDelta = 8, route = V3Route.Merchant),
            V3EventChoice("拒绝担保", "称米船非李氏所有不敢担保，米船被扣日久，商帮恨李氏见死不救。", merchantsDelta = -6, cohesionDelta = 2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("士林榜文", "城中贴出士林榜文，称各族应共抗苛派。", listOf(
            V3EventChoice("暗中资助", "暗中资助士林榜文张贴传抄银二十五两，士林视李氏为同道。", silverDelta = -25, gentryDelta = 10, yamenDelta = -5, route = V3Route.Scholar),
            V3EventChoice("撕榜避祸", "命人撕去县中榜文示好县衙，县衙满意士林却骂李氏趋炎附势。", yamenDelta = 6, gentryDelta = -6, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("山外招抚", "县衙考虑招抚一股山贼，想让李氏出面作保。", listOf(
            V3EventChoice("作保招抚", "李氏出面作保招抚山贼，山贼受招安县中少了匪患，李氏也得了官府倚重。", yamenDelta = 6, banditsDelta = 9, influenceDelta = 3, route = V3Route.Warlord),
            V3EventChoice("拒作保人", "称山贼反复无常不敢作保，山贼恨李氏不肯通融，匪患更甚。", cohesionDelta = 3, banditsDelta = -5, route = V3Route.Fortress)
        )),
        V3ActiveEvent("豪族会盟", "邻县豪族请李氏参加会盟，共议守望相助。", listOf(
            V3EventChoice("赴会结盟", "赴邻县豪族会盟，与各路豪族歃血为盟守望相助，声望大震。", silverDelta = -35, influenceDelta = 8, gentryDelta = 5, route = V3Route.Warlord),
            V3EventChoice("婉拒会盟", "称李氏德薄不敢与盟，少惹了纷争，族中也松了口气。", cohesionDelta = 4, influenceDelta = -2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("灾后重税", "灾年未过，税册又至。族中有人提议集体抗税。", listOf(
            V3EventChoice("集体缓缴", "率各族集体请求缓缴，民心大快，县令却大怒记李氏为首恶。", villagersDelta = 10, yamenDelta = -12, cohesionDelta = 5, route = V3Route.Hermit),
            V3EventChoice("分户代缴", "不与众人起哄分户代缴税银八十两，县衙满意，族中却觉得亏了。", silverDelta = -80, yamenDelta = 10, cohesionDelta = -2, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("远商献图", "远商献上海外航路图，索价极高。", listOf(
            V3EventChoice("买下航图", "拨银一百一十两买下远商海外航图，海路支如获至宝。", silverDelta = -110, merchantsDelta = 8, route = V3Route.Overseas, routeDelta = 12),
            V3EventChoice("临摹一份", "只出银三十五两请人临摹航图一份，远商大怒而去，海路支却也得了图。", silverDelta = -35, merchantsDelta = -3, route = V3Route.Overseas, routeDelta = 5)
        )),
        V3ActiveEvent("祠堂盟誓", "乱世渐深，族人请求在宗祠盟誓共守。", listOf(
            V3EventChoice("合族盟誓", "召集全族于宗祠杀牲盟誓共守，诸房歃血为盟凝聚大增。", grainDelta = -25, cohesionDelta = 10, influenceDelta = 3, route = V3Route.Hermit, routeDelta = 8),
            V3EventChoice("各房自守", "不搞合族盟誓命各房自守，倒是省了粮，凝聚却散了。", cohesionDelta = -5, route = V3Route.Warlord)
        ))
    )

    val crisisRouteEvents = listOf(
        V3ActiveEvent("书院登科", "族中学子榜上有名，李氏可借此正式走向士林。", listOf(
            V3EventChoice("大办鹿鸣宴", "大办鹿鸣宴庆贺登科，遍请县中士绅官员，李氏书香门第之名由此立定。", silverDelta = -70, influenceDelta = 12, gentryDelta = 12, route = V3Route.Scholar, routeDelta = 14),
            V3EventChoice("低调入仕", "不事张扬，只修书往县衙报备入仕低调赴任，得了官府好感。", yamenDelta = 8, influenceDelta = 5, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("商号通省", "李氏商号可并入省城大行，利润丰厚但受人牵制。", listOf(
            V3EventChoice("并入大行", "将李氏商号并入省城大行，利润丰厚银钱滚滚，只是受人牵制。", silverDelta = 120, merchantsDelta = 12, route = V3Route.Merchant, routeDelta = 14),
            V3EventChoice("自立字号", "拨银四十五两自立字号于省城，虽风险高，却是自家招牌。", silverDelta = -45, influenceDelta = 6, route = V3Route.Merchant, routeDelta = 9)
        )),
        V3ActiveEvent("堡寨成军", "乡勇已成规模，邻村愿奉李氏为盟主。", listOf(
            V3EventChoice("立盟主旗", "于寨堡立盟主旗，邻村乡勇皆听李氏调遣，俨然一方诸侯。", militiaDelta = 25, villagersDelta = 8, influenceDelta = 8, route = V3Route.Fortress, routeDelta = 14),
            V3EventChoice("不称盟主", "不称盟主不立旗号，只与邻村守望相助，免了官府猜忌。", yamenDelta = 4, cohesionDelta = 5, route = V3Route.Hermit)
        )),
        V3ActiveEvent("勤王军书", "朝廷军书至县，若李氏响应，将名列勤王义族。", listOf(
            V3EventChoice("奉书勤王", "奉勤王军书输粮募勇交军镇调遣，名列勤王义族，朝野闻名。", grainDelta = -80, militiaDelta = -15, yamenDelta = 14, garrisonDelta = 12, route = V3Route.Loyalist, routeDelta = 14),
            V3EventChoice("称病守县", "称族中多病无力远出只守本县不出，保住了实力声名却有限。", cohesionDelta = 4, yamenDelta = -4, route = V3Route.Hermit)
        )),
        V3ActiveEvent("据县自保", "县令出逃，城中无主。族中强硬派请李氏接管粮仓与城门。", listOf(
            V3EventChoice("接管城门", "趁县令出逃接管城门与粮仓，割据一方待价而沽。", militiaDelta = 30, influenceDelta = 10, yamenDelta = -10, route = V3Route.Warlord, routeDelta = 16),
            V3EventChoice("迎回官印", "派人迎回县令官印维持名义秩序，朝廷念李氏忠义。", yamenDelta = 10, influenceDelta = 5, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("海船待发", "南风已起，海船可载一批族人远渡。", listOf(
            V3EventChoice("送族远渡", "拨银一百四十两送一批族人乘海船远渡南洋开基，海路通了。", silverDelta = -140, cohesionDelta = -4, merchantsDelta = 12, route = V3Route.Overseas, routeDelta = 16),
            V3EventChoice("只送货物", "只送货物不送人，赚银七十两而归，海路终究未成。", silverDelta = 70, merchantsDelta = 6, route = V3Route.Merchant)
        )),
        V3ActiveEvent("闭乡十约", "族老拟定闭乡十约，要求减少外争、屯粮、禁奢。", listOf(
            V3EventChoice("颁行十约", "颁行闭乡十约：减外争、屯粮、禁奢、练勇...合族奉行，隐世之局已成。", grainDelta = 40, cohesionDelta = 12, villagersDelta = 6, route = V3Route.Hermit, routeDelta = 14),
            V3EventChoice("择要施行", "十约只择其中可行者施行，灵活有余，成效却打了折扣。", cohesionDelta = 5, route = V3Route.Hermit, routeDelta = 6)
        ))
    )

    val progressEvents = listOf(
        V3ActiveEvent("初立家门", "开局未久，邻里仍把李氏看作一户小家。若此时不立规矩，后续产业、人丁、婚配都会散乱。", listOf(
            V3EventChoice("立家规三条", "亲手立下家规三条：敬祖宗、守田业、和族人，贴于祠门。", cohesionDelta = 6, influenceDelta = 2, route = V3Route.Hermit),
            V3EventChoice("先求温饱", "称眼下温饱要紧立规矩之事日后再议，先买粮囤入仓中。", grainDelta = 45, cohesionDelta = -1, route = V3Route.Hermit)
        )),
        V3ActiveEvent("媒人探门", "媒人到祠前打听李氏家底。若礼数周全，可为成家开路；若敷衍，婚事会迟。", listOf(
            V3EventChoice("备礼迎媒", "备礼三十五两迎媒人入祠礼数周全，婚事成了一半。", silverDelta = -35, grainDelta = -15, influenceDelta = 4, route = V3Route.Hermit),
            V3EventChoice("只问家世", "只出八两小钱问女方家世，媒人嫌寒酸婚事恐要拖。", silverDelta = -8, influenceDelta = -1, route = V3Route.Hermit)
        )),
        V3ActiveEvent("添丁议名", "新生儿或族中幼童渐多，族老请按辈分排字，以免日后谱牒混乱。", listOf(
            V3EventChoice("定下字辈", "请族老定下字辈排行，以后新生儿按辈取名，谱牒从此有序。", silverDelta = -12, cohesionDelta = 6, influenceDelta = 2, route = V3Route.Hermit),
            V3EventChoice("各房自定", "字辈之事各房自定，省了争执，日后谱牒却必乱。", cohesionDelta = -3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("童蒙入塾", "族中孩童到了启蒙年纪，书香支建议请塾师，商支认为先学算盘更实用。", listOf(
            V3EventChoice("请塾师启蒙", "请塾师入祠启蒙族中孩童，耕读之路从此起步。", silverDelta = -42, gentryDelta = 4, influenceDelta = 3, route = V3Route.Scholar, routeDelta = 8),
            V3EventChoice("先学账房", "命孩童先学算盘记账，商路基础从小打下。", silverDelta = -24, merchantsDelta = 5, route = V3Route.Merchant, routeDelta = 7)
        )),
        V3ActiveEvent("第一处铺面", "集市有小铺转让，若买下可补银入账；若错过，商路仍弱。", listOf(
            V3EventChoice("买下铺面", "花银八十五两买下集市转让铺面，商帮关系打通，李氏有了第一处商铺。", silverDelta = -85, merchantsDelta = 8, siteId = "market", siteControlDelta = 9, route = V3Route.Merchant, routeDelta = 8),
            V3EventChoice("租摊试水", "先租摊位试做买卖，花银较少先看看行情再做打算。", silverDelta = -25, merchantsDelta = 3, siteId = "market", siteControlDelta = 4, route = V3Route.Merchant)
        )),
        V3ActiveEvent("粮仓扩建", "田庄已有余粮，族老提议扩建粮仓，以备灾荒和兵乱。", listOf(
            V3EventChoice("扩建粮仓", "花银七十两扩建粮仓又添粮三十石囤入，田庄从此不怕灾荒。", silverDelta = -70, grainDelta = -30, cohesionDelta = 4, siteId = "farmland", siteRiskDelta = -10, route = V3Route.Hermit, routeDelta = 7),
            V3EventChoice("卖粮换银", "将余粮卖与商帮换银九十五两，仓中却只剩薄粮，抗灾能力大降。", silverDelta = 95, grainDelta = -120, merchantsDelta = 4, route = V3Route.Merchant, routeDelta = 6)
        )),
        V3ActiveEvent("族产成局", "田、铺、仓渐成体系，各房开始关心账权归谁。", listOf(
            V3EventChoice("宗祠统账", "定下族产宗祠统账之制，各房账册归宗祠总管，主房权威更重。", silverDelta = -20, cohesionDelta = 6, influenceDelta = 4, route = V3Route.Hermit),
            V3EventChoice("各房分账", "各房产业各房自管，年终只交定额，效率高了族中却渐散。", silverDelta = 70, cohesionDelta = -4, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", wealthDelta = 5, influenceDelta = 3)))
        )),
        V3ActiveEvent("产业雇工", "产业渐多，族人不够用，需要雇外姓工匠和佃户。", listOf(
            V3EventChoice("按契雇工", "花银四十五两按契雇用外姓工匠佃户，产业运转更稳，外人也感李氏公道。", silverDelta = -45, villagersDelta = 5, siteId = "market", siteControlDelta = 5, route = V3Route.Merchant),
            V3EventChoice("只用族人", "雇工只用族人，虽凝聚稳固，产业扩张却慢了下来。", cohesionDelta = 4, silverDelta = -10, route = V3Route.Hermit)
        )),
        V3ActiveEvent("宗祠大修", "族望渐起后，旧祠显得寒酸。修祠可聚族，也会消耗大量银粮。", listOf(
            V3EventChoice("大修宗祠", "花银一百二十两大修宗祠，翻修大殿增建厢房，族望大增。", silverDelta = -120, grainDelta = -40, cohesionDelta = 10, influenceDelta = 8, siteId = "shrine", siteControlDelta = 10, route = V3Route.Hermit, routeDelta = 8),
            V3EventChoice("小修门面", "只花银三十五两小修祠门门面，不至于太寒酸。", silverDelta = -35, cohesionDelta = 3, siteId = "shrine", siteControlDelta = 4, route = V3Route.Hermit)
        )),
        V3ActiveEvent("家丁成队", "乡勇已有规模，武支建议编为常备家丁，书香支担心犯忌。", listOf(
            V3EventChoice("编练家丁", "将乡勇编为常备家丁日夜操练，武备渐成，官府却起了猜忌之心。", silverDelta = -75, grainDelta = -45, militiaDelta = 28, yamenDelta = -8, route = V3Route.Warlord, routeDelta = 10),
            V3EventChoice("报备乡勇", "将乡勇名册报备县衙合法办团，官府放心，武备却有限。", silverDelta = -35, militiaDelta = 10, yamenDelta = 7, route = V3Route.Loyalist, routeDelta = 7)
        )),
        V3ActiveEvent("县中称族", "李氏人口、产业、声望已非小户，县中开始称其为一族。", listOf(
            V3EventChoice("设族长议事", "正式设立族长议事制，族政有了章法，李氏自此名正言顺为一族。", silverDelta = -45, cohesionDelta = 8, influenceDelta = 6, route = V3Route.Hermit, routeDelta = 8),
            V3EventChoice("广交地方", "花银广交县中士绅商帮，对外声望日隆，族内治理却略疏松。", silverDelta = -65, gentryDelta = 7, merchantsDelta = 5, influenceDelta = 7, route = V3Route.Scholar, routeDelta = 7)
        )),
        V3ActiveEvent("府城来帖", "李氏名声传至府城，有人请你参与府城粮价与治安公议。", listOf(
            V3EventChoice("赴府公议", "赴府城参与粮价治安公议，李氏名声传至府城交游更广。", silverDelta = -80, influenceDelta = 10, gentryDelta = 8, route = V3Route.Scholar, routeDelta = 9),
            V3EventChoice("遣商支探路", "遣商支带银五十五两往府城探路，先结商路再议其他。", silverDelta = -55, merchantsDelta = 9, influenceDelta = 5, route = V3Route.Merchant, routeDelta = 8)
        )),
        V3ActiveEvent("跨县置产", "邻县有人愿低价出让田铺，但当地宗族未必服李氏。", listOf(
            V3EventChoice("买田入县", "花银一百五十两在邻县买田入籍，又赠粮八十石与当地宗族，跨县根基初成。", silverDelta = -150, grainDelta = 80, influenceDelta = 8, route = V3Route.Merchant, routeDelta = 8),
            V3EventChoice("先结乡绅", "先花银七十两结交邻县乡绅站稳脚跟再议置产，稳妥许多。", silverDelta = -70, gentryDelta = 9, influenceDelta = 6, route = V3Route.Scholar, routeDelta = 7)
        )),
        V3ActiveEvent("府县会防", "流寇逼近，府县豪族议定共同守望。李氏若参加，便不再只是本县宗族。", listOf(
            V3EventChoice("出勇会防", "出乡勇赴府县会防，流寇闻风而退，李氏武名传遍府县。", grainDelta = -65, militiaDelta = 22, influenceDelta = 9, route = V3Route.Fortress, routeDelta = 9),
            V3EventChoice("出粮不出人", "只出粮九十石不出人助官府守土，风险小了声望却不如出勇者。", grainDelta = -90, yamenDelta = 5, route = V3Route.Loyalist, routeDelta = 6)
        )),
        V3ActiveEvent("省城商约", "省城大商号愿与李氏合约，要求稳定供粮和码头份额。", listOf(
            V3EventChoice("签省城商约", "与省城大商号签下供粮与码头合约，银钱滚滚商路通到省城。", silverDelta = 160, grainDelta = -80, merchantsDelta = 12, route = V3Route.Merchant, routeDelta = 12),
            V3EventChoice("保留本县份额", "不签省约只守本县商路份额，稳当经营也赚了银子。", silverDelta = 45, cohesionDelta = 3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("一方豪强", "李氏已能左右府县粮价和治安，小族前来投靠，大族暗中试探。", listOf(
            V3EventChoice("收纳附族", "收纳前来投靠的小族为附族，人口声望大增，治理也更吃力。", grainDelta = -80, cohesionDelta = -2, influenceDelta = 14, route = V3Route.Warlord, routeDelta = 12),
            V3EventChoice("结盟不并族", "与小族结盟但不并入李氏，各守本业声望也涨了。", silverDelta = -60, influenceDelta = 8, gentryDelta = 6, route = V3Route.Scholar, routeDelta = 8)
        )),
        V3ActiveEvent("京畿风声", "京畿动荡传来，士绅问李氏是否仍奉朝廷，武支则言应自立旗号。", listOf(
            V3EventChoice("奉明勤王", "闻京畿动荡决意奉明勤王，输银输粮募勇北上，忠义之名传于天下。", silverDelta = -120, grainDelta = -120, militiaDelta = 35, yamenDelta = 12, garrisonDelta = 14, route = V3Route.Loyalist, routeDelta = 16),
            V3EventChoice("自保待变", "不急于勤王先修堡囤粮自保，待天下有变再做打算。", silverDelta = -70, grainDelta = -70, militiaDelta = 45, influenceDelta = 12, route = V3Route.Warlord, routeDelta = 16)
        )),
        V3ActiveEvent("天下邀盟", "各地豪族、商帮、军镇都在寻找可依附的新秩序，李氏已被推上牌桌。", listOf(
            V3EventChoice("立天下盟约", "于李氏宗祠立天下盟约，各路豪族商帮军镇共推李氏为盟主，大业初成。", silverDelta = -180, grainDelta = -160, militiaDelta = 60, influenceDelta = 18, route = V3Route.Warlord, routeDelta = 18),
            V3EventChoice("以商粮控局", "不立盟主虚名，以商路粮道控制各方，银粮虽耗局势却在掌中。", silverDelta = -100, grainDelta = -220, merchantsDelta = 14, influenceDelta = 14, route = V3Route.Merchant, routeDelta = 14)
        ))
    )

    private data class SeasonalEventSeed(
        val titleA: String,
        val bodyA: String,
        val titleB: String,
        val bodyB: String,
        val siteId: String,
        val route: V3Route
    )

    private val seasonalEventSeeds = listOf(
        SeasonalEventSeed("正月开祠", "正月祠门重开，各房上香点名。族老提醒：新岁若没有章程，婚配、田租、用工都会各行其是。", "正月灯市", "县中灯市开张，商帮、士绅与差役都在街上看李氏如何露面。", "shrine", V3Route.Hermit),
        SeasonalEventSeed("二月春耕", "二月水暖，南乡田庄要定佃约、修沟渠。若误了农时，全年粮仓都会受损。", "二月社约", "乡民请李氏出面重申春社旧约，约束盗砍、争水和偷牛。", "farmland", V3Route.Hermit),
        SeasonalEventSeed("三月清明", "清明祭祖，各房都带着账本和怨气回祠。祭礼既是团聚，也是一次无声审计。", "三月讲会", "书院春讲开场，士子谈辽饷、田赋与宗法。李氏若参与，名声会更响。", "academy", V3Route.Scholar),
        SeasonalEventSeed("四月插秧", "插秧时节，佃户缺人缺牛，田庄管事请求宗祠协调劳力。", "四月药市", "药材上市，医馆可趁价低备药，也可把银两留给营建。", "clinic", V3Route.Hermit),
        SeasonalEventSeed("五月端午", "端午龙舟聚众，县衙担心借赛生乱，商帮却想借机招揽客货。", "五月巡堤", "梅雨将至，河堤和码头都要巡检；一旦决口，田庄与集市都会遭殃。", "dock", V3Route.Merchant),
        SeasonalEventSeed("六月暑疫", "暑气蒸腾，医馆外咳喘者增多。若此时不备药，秋前恐成疫势。", "六月晒书", "书院晒书，旧卷中翻出族中先人批注，可借此兴学，也可能牵出旧案。", "academy", V3Route.Scholar),
        SeasonalEventSeed("七月鬼节", "中元施食，流民与乞丐聚在祠外。二房主张施粥，商支担心开了口子。", "七月夜巡", "暑夜盗影频繁，北山寨堡请求加派夜巡和火把。", "fort", V3Route.Fortress),
        SeasonalEventSeed("八月秋税", "秋粮将收，县衙已派书吏下乡估亩。田庄丰歉尚未定，税册先到。", "八月商会", "中秋前后商路最旺，西河集市请李氏定价、护货、平争。", "market", V3Route.Merchant),
        SeasonalEventSeed("九月登高", "重阳登高，族中老人谈及避乱旧事，建议早备山中退路。", "九月练勇", "秋高马肥，武支请趁农闲操练乡勇，以防冬前盗起。", "mountain_pass", V3Route.Fortress),
        SeasonalEventSeed("十月修仓", "霜降后粮仓入满，旧仓板缝、鼠患和账目都要清点。", "十月冬衣", "寒衣未备，佃户与乡勇都在等宗祠发放布棉。", "farmland", V3Route.Hermit),
        SeasonalEventSeed("冬月封河", "河道将封，码头货物若不及时转运，银货都要压到来年。", "冬月县帖", "年关将近，县衙递来催科帖，措辞比往年更急。", "yamen", V3Route.Loyalist),
        SeasonalEventSeed("腊月结账", "腊月封账，各房账册齐聚宗祠。谁多拿一分，谁少出一石，都可能成为来年隐患。", "腊月祭灶", "祭灶之后便是岁末，族人盼赏、佃户盼粮、乡勇盼饷。", "shrine", V3Route.Hermit)
    )

    val seasonalEvents = seasonalEventSeeds.flatMap { seed ->
        listOf(
            V3ActiveEvent(seed.titleA, seed.bodyA, listOf(
                V3EventChoice("主动操持", "依时节亲自调度银粮人手，该花的花该办的办，本季事务有条而不紊。", silverDelta = -22, grainDelta = -12, cohesionDelta = 3, influenceDelta = 2, siteId = seed.siteId, siteControlDelta = 7, siteRiskDelta = -6, route = seed.route, routeDelta = 6),
                V3EventChoice("节用观望", "命管事节用度日常事缓办观望，只求本季不出乱子，家底省了风险却留。", silverDelta = 10, cohesionDelta = 1, siteId = seed.siteId, siteControlDelta = 2, siteRiskDelta = 2, route = V3Route.Hermit, routeDelta = 3)
            )),
            V3ActiveEvent(seed.titleB, seed.bodyB, listOf(
                V3EventChoice("借机扩名", "趁此节令场合露面扬名，银粮花出去，声望与路线也传出去了。", silverDelta = -30, grainDelta = -10, influenceDelta = 4, siteId = seed.siteId, siteControlDelta = 5, route = seed.route, routeDelta = 7),
                V3EventChoice("只守家计", "不事张扬不逐虚名，只守家计囤粮度日，路线推进虽慢家底却稳。", grainDelta = 15, cohesionDelta = 2, siteId = seed.siteId, siteRiskDelta = -2, route = V3Route.Hermit, routeDelta = 3)
            ))
        )
    }

    private data class CountyManagementSeed(
        val siteId: String,
        val siteName: String,
        val route: V3Route,
        val pressure: String,
        val ally: String
    )

    private val countyManagementSeeds = listOf(
        CountyManagementSeed("shrine", "李氏宗祠", V3Route.Hermit, "谱牒、祭田与房支席位交缠", "族老"),
        CountyManagementSeed("farmland", "南乡田庄", V3Route.Hermit, "佃约、水利与秋粮估产难以两全", "佃户"),
        CountyManagementSeed("market", "西河集市", V3Route.Merchant, "货价、牙行与商帮分润不断起争", "商帮"),
        CountyManagementSeed("yamen", "清河县衙", V3Route.Loyalist, "税册、徭役和差役勒索压到乡里", "书吏"),
        CountyManagementSeed("academy", "东林书院", V3Route.Scholar, "讲会、束脩和门户争论暗流涌动", "士子"),
        CountyManagementSeed("clinic", "仁心医馆", V3Route.Hermit, "药材、病患和义诊开支日日紧逼", "郎中"),
        CountyManagementSeed("fort", "北山寨堡", V3Route.Fortress, "墙垣、哨探和乡勇粮饷都要补足", "武支"),
        CountyManagementSeed("dock", "三江码头", V3Route.Overseas, "船税、海货和巡查风声互相牵扯", "海商"),
        CountyManagementSeed("mountain_pass", "黑松山道", V3Route.Warlord, "塌方、私盐、流寇斥候都从此处冒头", "山民")
    )

    val countyManagementEvents = countyManagementSeeds.flatMap { seed ->
        listOf(
            V3ActiveEvent("${seed.siteName}月课", "${seed.siteName}本月事务繁杂：${seed.pressure}。${seed.ally}请李氏尽快定下月课，否则人心易散。", listOf(
                V3EventChoice("定月课", "定下${seed.siteName}月课规矩，${seed.ally}有章可循，事务渐入正轨。", silverDelta = -24, cohesionDelta = 2, siteId = seed.siteId, siteControlDelta = 9, siteRiskDelta = -5, route = seed.route, routeDelta = 6),
                V3EventChoice("缓一月", "告${seed.ally}本月银粮吃紧暂缓一月，只求眼前无事，隐患却留了下来。", silverDelta = 12, siteId = seed.siteId, siteRiskDelta = 6, route = V3Route.Hermit, routeDelta = 2)
            )),
            V3ActiveEvent("${seed.siteName}旧账", "${seed.siteName}翻出一笔旧账，牵涉${seed.ally}与宗祠用度。若公开查账，可能得罪人；若不查，隐患会留到后面。", listOf(
                V3EventChoice("公开清账", "当众翻开旧账一笔笔核对，${seed.ally}在场见证，追回银粮也立了规矩。", silverDelta = 38, grainDelta = 16, influenceDelta = 2, siteId = seed.siteId, siteControlDelta = 7, route = seed.route, routeDelta = 5),
                V3EventChoice("私下抹平", "私下与${seed.ally}抹平旧账不伤脸面，银粮却追不回多少。", silverDelta = -16, cohesionDelta = 4, siteId = seed.siteId, siteRiskDelta = -3, route = V3Route.Hermit, routeDelta = 3)
            )),
            V3ActiveEvent("${seed.siteName}添役", "${seed.siteName}要继续运转，必须添人添役。${seed.ally}愿出面帮忙，但要宗祠给出名分和口粮。", listOf(
                V3EventChoice("添役办事", "给${seed.ally}添了人手名分和口粮，他们办事更勤，${seed.siteName}运转更顺。", grainDelta = -28, siteId = seed.siteId, siteControlDelta = 10, siteRiskDelta = -7, route = seed.route, routeDelta = 6),
                V3EventChoice("仍用旧人", "不添新人仍用旧人撑过这月，${seed.ally}虽无怨言效率却有限。", cohesionDelta = 1, siteId = seed.siteId, siteControlDelta = 3, siteRiskDelta = 3, route = V3Route.Hermit, routeDelta = 2)
            )),
            V3ActiveEvent("${seed.siteName}外争", "外族或小吏开始插手${seed.siteName}，试探李氏底线。若退让，日后更难收回；若强硬，局势会紧。", listOf(
                V3EventChoice("强硬收回", "强硬收回权柄，外族小吏不敢再伸手，${seed.ally}拍手称快，风险也随之抬头。", influenceDelta = 4, siteId = seed.siteId, siteControlDelta = 14, siteRiskDelta = 5, route = V3Route.Warlord, routeDelta = 6),
                V3EventChoice("请人调停", "请${seed.ally}出面调停争端，各让一步险势缓和。", silverDelta = -26, siteId = seed.siteId, siteControlDelta = 6, siteRiskDelta = -9, route = seed.route, routeDelta = 5)
            ))
        )
    }

    private data class RouteMilestoneSeed(
        val route: V3Route,
        val label: String,
        val symbol: String,
        val ally: String,
        val costSite: String
    )

    private val routeMilestoneSeeds = listOf(
        RouteMilestoneSeed(V3Route.Scholar, "耕读", "族学、科举与士林清名", "士绅", "academy"),
        RouteMilestoneSeed(V3Route.Merchant, "商族", "铺面、商号与码头货路", "商帮", "market"),
        RouteMilestoneSeed(V3Route.Fortress, "堡寨", "寨墙、乡勇与守望盟约", "乡民", "fort"),
        RouteMilestoneSeed(V3Route.Loyalist, "勤王", "县衙、军镇与输饷名册", "军镇", "yamen"),
        RouteMilestoneSeed(V3Route.Warlord, "割据", "城防、粮仓与地方兵权", "豪族", "mountain_pass"),
        RouteMilestoneSeed(V3Route.Overseas, "海路", "码头、船位与南洋退路", "海商", "dock"),
        RouteMilestoneSeed(V3Route.Hermit, "保族", "宗祠、粮仓与闭乡族约", "族老", "shrine")
    )

    val routeMilestoneEvents = routeMilestoneSeeds.flatMap { seed ->
        listOf(
            V3ActiveEvent("${seed.label}议纲", "${seed.ally}认为李氏已可把${seed.symbol}写入族中长期议纲。若定纲，路线会更稳；若不定，各房还会摇摆。", listOf(
                V3EventChoice("写入议纲", "将${seed.symbol}正式写入族中议纲，诸房从此认准${seed.label}方向路线坚定。", silverDelta = -36, influenceDelta = 5, siteId = seed.costSite, siteControlDelta = 5, route = seed.route, routeDelta = 10),
                V3EventChoice("暂不定纲", "称时局未定暂不立纲，诸房各展所长保持灵活。", cohesionDelta = 3, route = V3Route.Hermit, routeDelta = 3)
            )),
            V3ActiveEvent("${seed.label}荐才", "有人推荐一批适合${seed.label}路线的人手。收下要花银粮，不收则错过扩张时机。", listOf(
                V3EventChoice("收纳荐才", "收下${seed.ally}荐来的人手，${seed.label}一脉多了得力之人。", silverDelta = -42, grainDelta = -22, influenceDelta = 4, route = seed.route, routeDelta = 9),
                V3EventChoice("只留名册", "只留荐才名册待用暂不收入族中，花费少了推进也慢了。", silverDelta = -12, route = seed.route, routeDelta = 4)
            )),
            V3ActiveEvent("${seed.label}据点", "${seed.costSite}一带可成为${seed.label}路线据点。若集中投入，后续事件会更偏向此路。", listOf(
                V3EventChoice("集中投入", "集中银粮投入${seed.label}据点，${seed.ally}大为振奋，据点日益稳固。", silverDelta = -58, grainDelta = -28, siteId = seed.costSite, siteControlDelta = 13, siteRiskDelta = -6, route = seed.route, routeDelta = 11),
                V3EventChoice("分散投入", "银粮分散投入各处不偏不倚，虽无突出成果倒也稳健。", silverDelta = -24, cohesionDelta = 2, siteId = seed.costSite, siteControlDelta = 5, route = V3Route.Hermit, routeDelta = 4)
            )),
            V3ActiveEvent("${seed.label}声名", "外界已开始用${seed.label}二字评价李氏。此时若顺势造势，可快速抬高名望。", listOf(
                V3EventChoice("顺势造势", "趁外界以${seed.label}评价李氏之时顺势造势，县中皆知李氏走${seed.label}之路。", silverDelta = -50, influenceDelta = 9, route = seed.route, routeDelta = 12),
                V3EventChoice("低调蓄势", "不急于扬名低调蓄势深耕根基，少花银两风险也低。", cohesionDelta = 4, route = V3Route.Hermit, routeDelta = 4)
            )),
            V3ActiveEvent("${seed.label}分歧", "族中有人质疑继续押注${seed.label}路线会拖累家业，要求回到田粮和香火本位。", listOf(
                V3EventChoice("坚持此路", "不为质疑所动坚持押注${seed.label}路线，方向不移怨言也随它去。", cohesionDelta = -2, influenceDelta = 4, route = seed.route, routeDelta = 12),
                V3EventChoice("兼顾保族", "兼顾保族根本，${seed.label}路线推进稍缓族心却稳了。", grainDelta = -16, cohesionDelta = 6, route = V3Route.Hermit, routeDelta = 5)
            )),
            V3ActiveEvent("${seed.label}成局", "多年经营后，${seed.symbol}已不是口号，而是李氏真正的活路。下一步要决定是扩张还是守成。", listOf(
                V3EventChoice("继续扩张", "多年经营根基已稳，继续扩张${seed.label}版图，声势日盛。", silverDelta = -85, grainDelta = -45, influenceDelta = 10, route = seed.route, routeDelta = 16),
                V3EventChoice("转入守成", "不再扩张转入守成囤粮修祠，资源压力大减保族路线增强。", grainDelta = 35, cohesionDelta = 7, route = V3Route.Hermit, routeDelta = 6)
            ))
        )
    }

    private data class EraEventSeed(
        val title: String,
        val body: String,
        val routeA: V3Route,
        val routeB: V3Route,
        val siteId: String
    )

    private val eraEventSeeds = listOf(
        EraEventSeed("万历末税册", "万历末年旧税未清，县衙拿着多年积欠催到宗祠。商路、田庄和族产都被翻上账面。", V3Route.Loyalist, V3Route.Scholar, "yamen"),
        EraEventSeed("万历末矿税", "矿税余波仍在，差役借旧名目盘剥集市。李氏若不出面，商路会被一点点勒住。", V3Route.Merchant, V3Route.Hermit, "market"),
        EraEventSeed("天启党议", "天启年间党争波及书院，讲会名单成了县中暗账。李氏的清名与安全难以两全。", V3Route.Scholar, V3Route.Hermit, "academy"),
        EraEventSeed("天启工役", "魏阉余威下，地方工役名目繁多。县衙要人，军镇要粮，乡民要活路。", V3Route.Loyalist, V3Route.Fortress, "yamen"),
        EraEventSeed("崇祯催科", "崇祯新政清弊，清到县里却变成更急的催科。差役上门，乡民哭诉，士绅观望。", V3Route.Loyalist, V3Route.Hermit, "farmland"),
        EraEventSeed("崇祯灾荒", "连年灾荒让流民渐多，田庄外的求粮声一日比一日近。", V3Route.Hermit, V3Route.Fortress, "clinic"),
        EraEventSeed("关外警讯", "关外消息越来越坏，军镇索饷索勇，县中豪族开始私筑寨堡。", V3Route.Loyalist, V3Route.Warlord, "fort"),
        EraEventSeed("流寇转战", "流寇转战的风声传来，山道商旅骤减，米价却一日数变。", V3Route.Fortress, V3Route.Merchant, "mountain_pass"),
        EraEventSeed("甲申前夜", "京畿震动的消息真假难辨，族人都在问：若朝廷真崩，李氏靠谁活？", V3Route.Warlord, V3Route.Hermit, "shrine"),
        EraEventSeed("南迁风声", "有败兵与商旅南下，说江北不可久留。码头船价暴涨，海路支趁机请命。", V3Route.Overseas, V3Route.Merchant, "dock")
    )

    val eraPressureEvents = eraEventSeeds.flatMap { seed ->
        listOf(
            V3ActiveEvent(seed.title, seed.body, listOf(
                V3EventChoice("顺势应对", "正面应对时局，拨银粮调度人手，该花的花该办的办稳住阵脚。", silverDelta = -48, grainDelta = -28, influenceDelta = 5, siteId = seed.siteId, siteControlDelta = 8, siteRiskDelta = -5, route = seed.routeA, routeDelta = 9),
                V3EventChoice("保族缓冲", "不逞强出头以保族为先缓冲时局冲击，守紧仓廪族心。", grainDelta = -18, cohesionDelta = 5, siteId = seed.siteId, siteRiskDelta = -3, route = seed.routeB, routeDelta = 6)
            )),
            V3ActiveEvent("${seed.title}余波", "${seed.body} 此事虽暂歇，余波仍在县中发酵，各房都要求宗祠给出后续章程。", listOf(
                V3EventChoice("定后续章程", "趁此事余波定下后续章程，诸房有了长久应对之法不再临事慌乱。", silverDelta = -30, cohesionDelta = 3, influenceDelta = 4, route = seed.routeA, routeDelta = 8),
                V3EventChoice("只补眼前缺口", "余波暂且只补眼前缺口不立长远章程，先度过这阵再说。", silverDelta = -8, grainDelta = 18, route = V3Route.Hermit, routeDelta = 4)
            ))
        )
    }

    val allEvents = siteEvents + advancedSiteEvents + seasonalEvents + countyManagementEvents + personEvents + advancedPersonEvents + branchEvents + strategyEvents + advancedStrategyEvents + routeEvents + routeMilestoneEvents + crisisRouteEvents + eraPressureEvents + progressEvents
}
