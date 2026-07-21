package com.arktools.daming.v3.data

object V3EventContent {
    val siteEvents = listOf(
        V3ActiveEvent("祠堂议谱", "族老请重修谱牒，将庶支与外迁族人重新登记。主房担心权柄被分，二房则认为此举可稳人心。", listOf(
            V3EventChoice("开祠修谱", "凝聚上升，主房略有不满。", silverDelta = -18, cohesionDelta = 6, influenceDelta = 2, siteId = "shrine", siteControlDelta = 6, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", grievanceDelta = 2), V3BranchImpact("second", loyaltyDelta = 2, grievanceDelta = -2))),
            V3EventChoice("只录近支", "维护主房秩序，但庶支不服。", cohesionDelta = -2, influenceDelta = 3, siteId = "shrine", siteControlDelta = 3, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 3), V3BranchImpact("second", grievanceDelta = 3)))
        )),
        V3ActiveEvent("祭田争界", "宗祠祭田与邻村田界不清，乡民聚集争吵，若处置失当会伤及民心。", listOf(
            V3EventChoice("请士绅公断", "士绅关系上升，花费银两。", silverDelta = -20, gentryDelta = 6, villagersDelta = 2, siteId = "shrine", siteRiskDelta = -8, route = V3Route.Scholar),
            V3EventChoice("强行圈界", "粮田保住，但乡民怨声四起。", grainDelta = 35, villagersDelta = -8, cohesionDelta = -3, siteId = "farmland", siteControlDelta = 5, route = V3Route.Warlord)
        )),
        V3ActiveEvent("旱情压田", "南乡田庄连日无雨，佃户望天兴叹。若不修渠开仓，秋粮恐难入仓。", listOf(
            V3EventChoice("修渠引水", "消耗银两，田庄风险下降。", silverDelta = -35, grainDelta = 20, villagersDelta = 4, siteId = "farmland", siteControlDelta = 5, siteRiskDelta = -16, route = V3Route.Hermit),
            V3EventChoice("减租稳佃", "短期少粮，民心与凝聚提升。", grainDelta = -25, cohesionDelta = 5, villagersDelta = 8, siteId = "farmland", siteRiskDelta = -10, route = V3Route.Hermit)
        )),
        V3ActiveEvent("粮仓霉变", "旧仓受潮，族中存粮已有霉味。商支建议卖出坏粮，二房坚决反对。", listOf(
            V3EventChoice("翻修粮仓", "银两下降，粮食损耗减少。", silverDelta = -28, grainDelta = -8, siteId = "farmland", siteControlDelta = 4, siteRiskDelta = -12, route = V3Route.Fortress),
            V3EventChoice("低价卖粮", "保住部分银两，但乡民不满。", silverDelta = 22, grainDelta = -35, villagersDelta = -5, siteId = "market", siteRiskDelta = 5, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", wealthDelta = 3, influenceDelta = 2)))
        )),
        V3ActiveEvent("米价暴涨", "集市米价一日三涨，商帮暗中囤货，乡民已在米铺门前争执。", listOf(
            V3EventChoice("平价放粮", "消耗粮食，民心大涨。", grainDelta = -55, villagersDelta = 12, cohesionDelta = 4, siteId = "market", siteRiskDelta = -12, route = V3Route.Hermit),
            V3EventChoice("顺势加价", "银两大增，乡民关系下降。", silverDelta = 60, villagersDelta = -10, merchantsDelta = 5, siteId = "market", siteControlDelta = 5, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", wealthDelta = 5, grievanceDelta = -2)))
        )),
        V3ActiveEvent("私盐线索", "集市牙人密报，有私盐客借山道入县。此事可牟利，也会招来官府盘查。", listOf(
            V3EventChoice("暗中抽成", "银两增加，官府关系下降。", silverDelta = 45, yamenDelta = -6, merchantsDelta = 5, siteId = "market", siteRiskDelta = 6, route = V3Route.Merchant),
            V3EventChoice("交给县衙", "官府满意，商帮失望。", yamenDelta = 8, merchantsDelta = -5, influenceDelta = 2, siteId = "yamen", siteControlDelta = 3, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("徭役点丁", "县衙差役持文书到祠堂，要求宗族出丁修城。武支想借机练人，主房担心伤农。", listOf(
            V3EventChoice("按丁服役", "官府关系上升，凝聚下降。", yamenDelta = 8, cohesionDelta = -4, siteId = "yamen", siteRiskDelta = -4, route = V3Route.Loyalist),
            V3EventChoice("出银免役", "银两下降，人心稳定。", silverDelta = -50, yamenDelta = 3, cohesionDelta = 3, siteId = "yamen", siteControlDelta = 4, route = V3Route.Hermit)
        )),
        V3ActiveEvent("狱案牵连", "族中远亲被卷入县狱，书吏暗示可以花银周旋。若不救，族人恐寒心。", listOf(
            V3EventChoice("打点书吏", "救出远亲，官府与商支均受影响。", silverDelta = -40, cohesionDelta = 5, yamenDelta = 3, siteId = "yamen", route = V3Route.Loyalist),
            V3EventChoice("守法不救", "维持清名，但族内忠诚受损。", influenceDelta = 3, cohesionDelta = -5, gentryDelta = 3, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("main", loyaltyDelta = -2), V3BranchImpact("second", grievanceDelta = 3)))
        )),
        V3ActiveEvent("书院名师", "东林书院有名师过境，愿短留授课，但束脩不菲。", listOf(
            V3EventChoice("延请授课", "声望与学脉提升。", silverDelta = -45, influenceDelta = 5, gentryDelta = 6, siteId = "academy", siteControlDelta = 8, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("scholar", influenceDelta = 4, grievanceDelta = -3))),
            V3EventChoice("只请题字", "小得声名，不伤财力。", silverDelta = -12, influenceDelta = 2, gentryDelta = 2, siteId = "academy", siteControlDelta = 2, route = V3Route.Scholar)
        )),
        V3ActiveEvent("党争牵连", "书院讲会谈及朝局，县中有心人已将名单送入县衙。", listOf(
            V3EventChoice("约束诸生", "避开党争，书香支不满。", cohesionDelta = 3, gentryDelta = -3, yamenDelta = 4, siteId = "academy", siteRiskDelta = -8, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("scholar", grievanceDelta = 3))),
            V3EventChoice("力挺清议", "士林声望大增，官府猜忌。", influenceDelta = 8, gentryDelta = 9, yamenDelta = -7, siteId = "academy", siteControlDelta = 6, route = V3Route.Scholar)
        )),
        V3ActiveEvent("药材短缺", "医馆药柜见底，瘟病未止。海路支称码头有药材可购，但价高。", listOf(
            V3EventChoice("高价购药", "花银压疫，民心上升。", silverDelta = -55, villagersDelta = 8, cohesionDelta = 4, siteId = "clinic", siteRiskDelta = -16, route = V3Route.Hermit),
            V3EventChoice("采药入山", "省银但有山道风险。", silverDelta = -10, villagersDelta = 4, banditsDelta = 3, siteId = "mountain_pass", siteRiskDelta = 5, route = V3Route.Fortress)
        )),
        V3ActiveEvent("伤兵求医", "军镇伤兵夜至医馆，请求收治。若救治，可结军镇；若拒绝，或免牵连。", listOf(
            V3EventChoice("收治伤兵", "军镇关系提高，医馆压力上升。", silverDelta = -25, garrisonDelta = 9, villagersDelta = 2, siteId = "clinic", siteRiskDelta = 5, route = V3Route.Loyalist),
            V3EventChoice("婉拒离县", "避开军务，宗族更稳。", cohesionDelta = 2, garrisonDelta = -4, siteId = "clinic", siteRiskDelta = -4, route = V3Route.Hermit)
        )),
        V3ActiveEvent("乡勇索饷", "寨堡乡勇称巡夜辛苦，请求增饷。若拖欠，武支必有怨言。", listOf(
            V3EventChoice("增发饷银", "武支安定，银两下降。", silverDelta = -36, militiaDelta = 8, garrisonDelta = 3, siteId = "fort", siteControlDelta = 6, route = V3Route.Fortress, branchImpacts = listOf(V3BranchImpact("martial", loyaltyDelta = 3, grievanceDelta = -5))),
            V3EventChoice("严令节用", "保住银两，武支怨气上升。", silverDelta = 5, cohesionDelta = -2, siteId = "fort", siteRiskDelta = 8, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact("martial", grievanceDelta = 6, loyaltyDelta = -2)))
        )),
        V3ActiveEvent("堡门收留", "流民请求入堡避寇。武支担心混入奸细，二房却主张收留。", listOf(
            V3EventChoice("验籍收留", "民心上升，粮食消耗。", grainDelta = -30, villagersDelta = 8, cohesionDelta = 4, siteId = "fort", siteControlDelta = 4, route = V3Route.Fortress),
            V3EventChoice("闭门自守", "寨堡安全，乡民寒心。", villagersDelta = -7, banditsDelta = -2, siteId = "fort", siteRiskDelta = -8, route = V3Route.Hermit)
        )),
        V3ActiveEvent("船队失期", "三江码头船队迟迟未归，海路支与商支互相推诿。", listOf(
            V3EventChoice("派人沿江查访", "花银寻船，商帮关系稳定。", silverDelta = -25, merchantsDelta = 5, siteId = "dock", siteRiskDelta = -8, route = V3Route.Overseas),
            V3EventChoice("追究海路支", "主房威严上升，海路支不满。", influenceDelta = 3, siteId = "dock", siteControlDelta = 3, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact("sea", grievanceDelta = 5, loyaltyDelta = -2), V3BranchImpact("main", influenceDelta = 2)))
        )),
        V3ActiveEvent("南洋来信", "码头商人带来远海书信，称南洋可置田开铺，愿引李氏族人前往。", listOf(
            V3EventChoice("暗筹船资", "海外路线推进，银两下降。", silverDelta = -60, merchantsDelta = 6, siteId = "dock", siteControlDelta = 8, route = V3Route.Overseas, routeDelta = 10, branchImpacts = listOf(V3BranchImpact("sea", influenceDelta = 5, wealthDelta = 4, grievanceDelta = -4))),
            V3EventChoice("暂存此信", "保守观望，凝聚略升。", cohesionDelta = 2, siteId = "dock", siteRiskDelta = -3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("伏击商队", "黑松山道传来急报，商队被流寇伏击，若不救援，集市商路恐断。", listOf(
            V3EventChoice("武支救援", "乡勇立功，商帮感激。", silverDelta = 20, militiaDelta = -4, merchantsDelta = 8, banditsDelta = -8, siteId = "mountain_pass", siteRiskDelta = -10, route = V3Route.Fortress, branchImpacts = listOf(V3BranchImpact("martial", influenceDelta = 4, loyaltyDelta = 2))),
            V3EventChoice("赎买货物", "保住商路，但流寇气焰上升。", silverDelta = -45, merchantsDelta = 5, banditsDelta = 5, siteId = "mountain_pass", siteRiskDelta = 4, route = V3Route.Merchant)
        ))
    )

    val personEvents = listOf(
        V3ActiveEvent("族长夜议", "开族祖夜召族老，认为当前局势须定一条主线，否则各房各行其是。", listOf(
            V3EventChoice("定下家法", "主房威望上升，凝聚稳定。", cohesionDelta = 5, influenceDelta = 3, personId = 1, personFatigueDelta = 6, personMeritDelta = 2, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 3))),
            V3EventChoice("广听诸房", "房支怨气下降，但决断放缓。", cohesionDelta = 4, personId = 1, personFatigueDelta = 4, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("merchant", grievanceDelta = -2), V3BranchImpact("martial", grievanceDelta = -2), V3BranchImpact("scholar", grievanceDelta = -2)))
        )),
        V3ActiveEvent("承岳请战", "李承岳请率乡勇夜巡山道。他言乱世不能只靠文书，必须让流寇知道李氏有刀。", listOf(
            V3EventChoice("准其夜巡", "山道压力下降，武支得势。", militiaDelta = 5, banditsDelta = -6, personId = 2, personFatigueDelta = 10, personMeritDelta = 4, siteId = "mountain_pass", siteRiskDelta = -8, route = V3Route.Fortress, branchImpacts = listOf(V3BranchImpact("martial", influenceDelta = 4, grievanceDelta = -3))),
            V3EventChoice("令其守寨", "稳健自保，减少冒险。", garrisonDelta = 3, personId = 2, personFatigueDelta = 5, siteId = "fort", siteControlDelta = 5, route = V3Route.Hermit)
        )),
        V3ActiveEvent("若兰清名", "李若兰在书院辩难中声名渐起，有士绅请她代写族中公议文。", listOf(
            V3EventChoice("请她执笔", "士绅关系和声望提升。", influenceDelta = 5, gentryDelta = 7, personId = 3, personFatigueDelta = 8, personMeritDelta = 4, route = V3Route.Scholar),
            V3EventChoice("劝其避名", "避开党争，凝聚稳定。", cohesionDelta = 3, personId = 3, personLoyaltyDelta = 2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("仲财账本", "李仲财呈上新账，账面盈利颇丰，却有几处支出含糊。", listOf(
            V3EventChoice("追查账目", "追回银两，商支怨气上升。", silverDelta = 45, personId = 4, personLoyaltyDelta = -4, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("merchant", grievanceDelta = 5, loyaltyDelta = -2))),
            V3EventChoice("默许分润", "商路更活，主房权威受损。", silverDelta = 25, merchantsDelta = 6, cohesionDelta = -3, personId = 4, personMeritDelta = 3, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", wealthDelta = 5, grievanceDelta = -4), V3BranchImpact("main", grievanceDelta = 2)))
        )),
        V3ActiveEvent("济民义诊", "李济民请开义诊，救治流民与佃户。粮银虽耗，但人心可安。", listOf(
            V3EventChoice("开设义诊", "民心与凝聚提升。", silverDelta = -25, grainDelta = -15, villagersDelta = 9, cohesionDelta = 5, personId = 5, personFatigueDelta = 9, personMeritDelta = 4, siteId = "clinic", siteRiskDelta = -8, route = V3Route.Hermit),
            V3EventChoice("只治族人", "节省资源，乡民失望。", cohesionDelta = 2, villagersDelta = -4, personId = 5, personFatigueDelta = 3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("守砚求仕", "李守砚自言不甘困于县中，请求筹资拜访名士，为日后科举铺路。", listOf(
            V3EventChoice("资助拜师", "学脉推进，银两下降。", silverDelta = -35, influenceDelta = 4, gentryDelta = 5, personId = 6, personFatigueDelta = 6, personMeritDelta = 3, route = V3Route.Scholar),
            V3EventChoice("令其静读", "稳住心性，凝聚上升。", cohesionDelta = 2, personId = 6, personLoyaltyDelta = 2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("阿衡顶撞", "李阿衡巡寨时与官差冲突，称差役勒索粮钱。县衙已派人问责。", listOf(
            V3EventChoice("护下阿衡", "武支归心，官府不满。", yamenDelta = -7, militiaDelta = 4, personId = 7, personLoyaltyDelta = 4, personMeritDelta = 2, route = V3Route.Fortress, branchImpacts = listOf(V3BranchImpact("martial", loyaltyDelta = 4, grievanceDelta = -4))),
            V3EventChoice("赔礼息事", "官府关系恢复，武支有怨。", silverDelta = -22, yamenDelta = 6, personId = 7, personLoyaltyDelta = -3, route = V3Route.Loyalist, branchImpacts = listOf(V3BranchImpact("martial", grievanceDelta = 4)))
        )),
        V3ActiveEvent("环娘议价", "李环娘识破商帮压价，称若让她主谈，可多取三成利。", listOf(
            V3EventChoice("交由环娘", "商贸收益提高。", silverDelta = 55, merchantsDelta = 4, personId = 8, personFatigueDelta = 7, personMeritDelta = 4, route = V3Route.Merchant),
            V3EventChoice("稳价成交", "少赚些银两，关系更稳。", silverDelta = 25, merchantsDelta = 7, personId = 8, personMeritDelta = 2, route = V3Route.Merchant)
        )),
        V3ActiveEvent("观潮听风", "李观潮从码头听来风声，海禁巡查将近，但南洋货路也在此时开价。", listOf(
            V3EventChoice("先撤暗货", "降低海禁风险。", silverDelta = -15, yamenDelta = 3, personId = 9, siteId = "dock", siteRiskDelta = -12, route = V3Route.Hermit),
            V3EventChoice("冒险出货", "大赚一笔，官府猜忌。", silverDelta = 75, yamenDelta = -8, merchantsDelta = 6, personId = 9, personFatigueDelta = 8, personMeritDelta = 4, siteId = "dock", siteRiskDelta = 8, route = V3Route.Overseas)
        )),
        V3ActiveEvent("采薇救童", "李采薇在医馆外救下一名病童，乡民感念，但她已疲惫不堪。", listOf(
            V3EventChoice("让她休养", "保住族人状态。", cohesionDelta = 2, villagersDelta = 3, personId = 10, personFatigueDelta = -12, personLoyaltyDelta = 3, route = V3Route.Hermit),
            V3EventChoice("继续义诊", "民心更高，疲劳更重。", villagersDelta = 8, cohesionDelta = 3, personId = 10, personFatigueDelta = 12, personMeritDelta = 4, route = V3Route.Hermit)
        ))
    )

    val strategyEvents = listOf(
        V3ActiveEvent("辽饷加派", "县中传来新令，辽饷再加。各族皆惶惶，县令暗示李氏应先表态。", listOf(
            V3EventChoice("先缴一半", "官府稍安，资源承压。", silverDelta = -45, grainDelta = -30, yamenDelta = 9, route = V3Route.Loyalist),
            V3EventChoice("联族缓缴", "士绅关系提升，官府不满。", gentryDelta = 8, yamenDelta = -5, influenceDelta = 4, route = V3Route.Scholar)
        )),
        V3ActiveEvent("勤王檄文", "军镇传来勤王檄文，要求地方豪族输粮募勇。族中对是否响应争执不下。", listOf(
            V3EventChoice("输粮募勇", "勤王路线推进。", grainDelta = -60, militiaDelta = 12, garrisonDelta = 10, route = V3Route.Loyalist, routeDelta = 9),
            V3EventChoice("闭门自保", "自保路线推进，军镇不满。", cohesionDelta = 4, garrisonDelta = -5, route = V3Route.Fortress, routeDelta = 7)
        )),
        V3ActiveEvent("清军风闻", "北地商旅称关外兵马日盛，南北商路皆有惊惧。宗族须提前考虑退路。", listOf(
            V3EventChoice("屯粮修堡", "自保路线增强。", silverDelta = -35, grainDelta = -20, militiaDelta = 8, route = V3Route.Fortress, routeDelta = 8),
            V3EventChoice("筹备海路", "海外路线增强。", silverDelta = -50, merchantsDelta = 7, route = V3Route.Overseas, routeDelta = 8)
        )),
        V3ActiveEvent("士绅联姻", "邻县士绅遣媒人问询，若结亲可入士林，也需厚礼。", listOf(
            V3EventChoice("厚礼联姻", "士绅与声望提升。", silverDelta = -70, gentryDelta = 12, influenceDelta = 7, route = V3Route.Scholar, routeDelta = 7),
            V3EventChoice("婉拒媒约", "保住银两，路线更趋隐忍。", cohesionDelta = 2, gentryDelta = -3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("流寇招降", "山外流寇头目遣人来信，愿不扰李氏田庄，只求粮械暗助。", listOf(
            V3EventChoice("虚与委蛇", "暂缓流寇威胁，官府风险增加。", grainDelta = -30, banditsDelta = 8, yamenDelta = -7, route = V3Route.Warlord, routeDelta = 8),
            V3EventChoice("斩使示众", "官府与军镇称快，流寇仇恨加深。", influenceDelta = 5, yamenDelta = 5, garrisonDelta = 5, banditsDelta = -8, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("商帮借贷", "商帮愿借银给李氏渡过难关，但要码头与集市分润。", listOf(
            V3EventChoice("接受借银", "银两充裕，商帮影响扩大。", silverDelta = 120, merchantsDelta = 10, cohesionDelta = -2, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", wealthDelta = 6, influenceDelta = 3))),
            V3EventChoice("拒绝牵制", "保住自主，短期艰难。", cohesionDelta = 4, merchantsDelta = -5, route = V3Route.Hermit)
        )),
        V3ActiveEvent("乡约重修", "士绅与乡民请求李氏牵头重修乡约，共定粮价、夜禁与赈济规条。", listOf(
            V3EventChoice("牵头乡约", "民心士绅双升。", silverDelta = -25, villagersDelta = 8, gentryDelta = 6, influenceDelta = 4, route = V3Route.Scholar),
            V3EventChoice("只管族内", "宗族凝聚上升，地方影响下降。", cohesionDelta = 4, influenceDelta = -2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("县城戒严", "县城忽然戒严，传言有盗匪内应。商路断绝，县衙请各族协查。", listOf(
            V3EventChoice("协查内应", "官府关系提高，山道风险下降。", yamenDelta = 8, banditsDelta = -5, siteId = "mountain_pass", siteRiskDelta = -6, route = V3Route.Loyalist),
            V3EventChoice("护住商货", "商帮关系提高，官府不满。", silverDelta = 25, merchantsDelta = 7, yamenDelta = -4, route = V3Route.Merchant)
        ))
    )

    val routeEvents = listOf(
        V3ActiveEvent("耕读立名", "族中学子文章传入士林，若继续投入，李氏可走耕读门第。", listOf(
            V3EventChoice("刊刻族学文集", "耕读路线大进。", silverDelta = -40, influenceDelta = 8, gentryDelta = 8, route = V3Route.Scholar, routeDelta = 10),
            V3EventChoice("藏名避祸", "避开党争，隐世路线推进。", cohesionDelta = 4, route = V3Route.Hermit, routeDelta = 7)
        )),
        V3ActiveEvent("商号合股", "商支建议合并族中铺面，设一总号，专营米布、药材与海货。", listOf(
            V3EventChoice("设立总号", "富甲路线大进。", silverDelta = -60, merchantsDelta = 10, route = V3Route.Merchant, routeDelta = 10, branchImpacts = listOf(V3BranchImpact("merchant", influenceDelta = 6, wealthDelta = 6))),
            V3EventChoice("分铺经营", "风险分散，凝聚稳定。", cohesionDelta = 3, silverDelta = 20, route = V3Route.Hermit)
        )),
        V3ActiveEvent("堡寨盟约", "邻村请求与李氏共守山道，立堡寨盟约。", listOf(
            V3EventChoice("共守山道", "自保路线大进。", silverDelta = -35, grainDelta = -30, militiaDelta = 15, villagersDelta = 7, route = V3Route.Fortress, routeDelta = 10),
            V3EventChoice("只守本族", "减少消耗，但民心有限。", cohesionDelta = 3, villagersDelta = -3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("地方军议", "军镇把总愿与李氏结盟，共防流寇。若成，宗族将深涉兵事。", listOf(
            V3EventChoice("结军镇盟", "军镇与割据路线推进。", silverDelta = -40, militiaDelta = 18, garrisonDelta = 10, route = V3Route.Warlord, routeDelta = 10),
            V3EventChoice("仅送粮草", "勤王路线推进。", grainDelta = -45, garrisonDelta = 8, yamenDelta = 4, route = V3Route.Loyalist, routeDelta = 8)
        )),
        V3ActiveEvent("迁海密议", "海路支请将一批族人和银两送往南洋，作为乱世退路。", listOf(
            V3EventChoice("分支远渡", "海外路线大进。", silverDelta = -90, cohesionDelta = -3, merchantsDelta = 8, route = V3Route.Overseas, routeDelta = 12, branchImpacts = listOf(V3BranchImpact("sea", influenceDelta = 7, wealthDelta = 5))),
            V3EventChoice("暂缓迁徙", "凝聚稳定，但错失海路。", cohesionDelta = 4, route = V3Route.Hermit)
        )),
        V3ActiveEvent("闭乡避祸", "族老建议减少外争，修祠屯粮，谨慎度过乱世。", listOf(
            V3EventChoice("闭乡修约", "隐世避祸路线推进。", grainDelta = -20, cohesionDelta = 8, villagersDelta = 5, route = V3Route.Hermit, routeDelta = 10),
            V3EventChoice("仍逐外势", "声望上升，风险上升。", influenceDelta = 5, yamenDelta = 3, route = V3Route.Loyalist)
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
        PersonArcSeed(3, "李若兰", "scholar", "academy", V3Route.Scholar, "主持讲会", "清议声名越高，党争牵连越深"),
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
            V3EventChoice("彻查守祠人", "祠堂风险下降，主房威严上升。", silverDelta = -18, influenceDelta = 4, siteId = "shrine", siteControlDelta = 6, siteRiskDelta = -12, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 3), V3BranchImpact("second", grievanceDelta = 2))),
            V3EventChoice("压下不查", "暂保体面，但隐患仍在。", cohesionDelta = -3, siteId = "shrine", siteRiskDelta = 8, route = V3Route.Hermit)
        )),
        V3ActiveEvent("族学缺师", "族中蒙童渐多，旧师年老，请新师需厚礼，若不请则书香支不满。", listOf(
            V3EventChoice("请塾师入祠", "声望与学脉提升。", silverDelta = -32, influenceDelta = 4, gentryDelta = 4, siteId = "shrine", siteControlDelta = 5, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("scholar", grievanceDelta = -3, influenceDelta = 3))),
            V3EventChoice("暂由族老代课", "节省银两，效果有限。", cohesionDelta = 2, siteId = "shrine", route = V3Route.Hermit)
        )),
        V3ActiveEvent("田契水印", "南乡旧田契受潮，几处边界字迹模糊，邻村趁机索地。", listOf(
            V3EventChoice("重摹田契", "花银保住田庄控制。", silverDelta = -24, grainDelta = 18, siteId = "farmland", siteControlDelta = 8, siteRiskDelta = -6, route = V3Route.Hermit),
            V3EventChoice("邀官丈量", "官府关系提升，但田庄收益受损。", grainDelta = -20, yamenDelta = 5, siteId = "farmland", siteRiskDelta = -8, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("蝗影初现", "田边见到蝗群，佃户惶恐。若提前扑杀，可免大灾。", listOf(
            V3EventChoice("组织扑蝗", "粮食小损，风险大降。", grainDelta = -25, villagersDelta = 6, siteId = "farmland", siteRiskDelta = -18, route = V3Route.Hermit),
            V3EventChoice("祈雨观望", "花费少，但灾情可能扩大。", silverDelta = -8, cohesionDelta = 1, siteId = "farmland", siteRiskDelta = 10, route = V3Route.Hermit)
        )),
        V3ActiveEvent("集市斗殴", "米铺与布商因价税斗殴，商帮请李氏出面平事。", listOf(
            V3EventChoice("设行规仲裁", "集市控制提高。", influenceDelta = 3, merchantsDelta = 5, siteId = "market", siteControlDelta = 7, siteRiskDelta = -8, route = V3Route.Merchant),
            V3EventChoice("交县衙处置", "官府满意，商帮失望。", yamenDelta = 5, merchantsDelta = -4, siteId = "market", siteRiskDelta = -5, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("假银流入", "集市出现成色不足的碎银，若不清查，宗族商号信誉受损。", listOf(
            V3EventChoice("清查银色", "花银整顿，商路更稳。", silverDelta = -20, merchantsDelta = 5, siteId = "market", siteControlDelta = 5, siteRiskDelta = -9, route = V3Route.Merchant),
            V3EventChoice("照收快兑", "短期得利，长期风险上升。", silverDelta = 34, influenceDelta = -2, siteId = "market", siteRiskDelta = 8, route = V3Route.Merchant)
        )),
        V3ActiveEvent("县衙换吏", "县衙新到书吏不熟本地旧例，暗示各族重新打点。", listOf(
            V3EventChoice("送礼立案", "官府关系改善。", silverDelta = -38, yamenDelta = 8, siteId = "yamen", siteControlDelta = 5, route = V3Route.Loyalist),
            V3EventChoice("持旧例抗辩", "士绅赞许，县衙不悦。", gentryDelta = 5, yamenDelta = -5, influenceDelta = 3, siteId = "yamen", route = V3Route.Scholar)
        )),
        V3ActiveEvent("差役勒索", "差役借催税之名勒索佃户，乡民怨声传至祠堂。", listOf(
            V3EventChoice("代民呈状", "民心提升，官府紧张。", villagersDelta = 8, yamenDelta = -4, influenceDelta = 4, siteId = "yamen", siteRiskDelta = 5, route = V3Route.Scholar),
            V3EventChoice("私下打点", "压下事端，花费银两。", silverDelta = -30, yamenDelta = 4, villagersDelta = 2, siteId = "yamen", siteRiskDelta = -8, route = V3Route.Hermit)
        )),
        V3ActiveEvent("书院藏书", "旧家藏书愿售于李氏，若购入可兴族学。", listOf(
            V3EventChoice("购入藏书", "书院控制与士绅关系提升。", silverDelta = -42, gentryDelta = 6, influenceDelta = 4, siteId = "academy", siteControlDelta = 8, route = V3Route.Scholar),
            V3EventChoice("借阅抄录", "节省银两，进展较慢。", silverDelta = -12, influenceDelta = 2, siteId = "academy", siteControlDelta = 3, route = V3Route.Scholar)
        )),
        V3ActiveEvent("医馆染疫", "医馆收治病患过多，药童亦有染疫迹象。", listOf(
            V3EventChoice("闭馆消杀", "风险下降，民心短降。", silverDelta = -18, villagersDelta = -3, siteId = "clinic", siteRiskDelta = -18, route = V3Route.Hermit),
            V3EventChoice("扩棚分诊", "耗费更大，民心上升。", silverDelta = -42, grainDelta = -18, villagersDelta = 9, siteId = "clinic", siteControlDelta = 5, siteRiskDelta = -10, route = V3Route.Hermit)
        )),
        V3ActiveEvent("堡墙裂缝", "北山寨堡旧墙雨后开裂，若不修补，夜巡难以安心。", listOf(
            V3EventChoice("修补堡墙", "寨堡控制上升。", silverDelta = -36, grainDelta = -12, militiaDelta = 5, siteId = "fort", siteControlDelta = 9, siteRiskDelta = -12, route = V3Route.Fortress),
            V3EventChoice("只添夜巡", "少花银两，乡勇疲惫。", militiaDelta = 3, siteId = "fort", siteRiskDelta = -4, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact("martial", grievanceDelta = 3)))
        )),
        V3ActiveEvent("码头税卡", "县衙欲在三江码头新设税卡，商支与海路支均忧心生计。", listOf(
            V3EventChoice("协商减税", "花银换取码头稳定。", silverDelta = -35, yamenDelta = 4, merchantsDelta = 5, siteId = "dock", siteRiskDelta = -8, route = V3Route.Merchant),
            V3EventChoice("暗走水路", "海外路线推进，官府风险上升。", silverDelta = 45, yamenDelta = -7, merchantsDelta = 4, siteId = "dock", siteControlDelta = 5, siteRiskDelta = 8, route = V3Route.Overseas)
        )),
        V3ActiveEvent("山道塌方", "黑松山道雨后塌方，商队绕路，流寇也可能借机设伏。", listOf(
            V3EventChoice("雇工修道", "花费银粮，山道风险下降。", silverDelta = -28, grainDelta = -15, merchantsDelta = 3, siteId = "mountain_pass", siteControlDelta = 6, siteRiskDelta = -15, route = V3Route.Merchant),
            V3EventChoice("设哨观望", "乡勇警戒，修复缓慢。", militiaDelta = 4, banditsDelta = -4, siteId = "mountain_pass", siteRiskDelta = -7, route = V3Route.Fortress)
        )),
        V3ActiveEvent("私铸兵器", "山道铁匠暗中为寨丁修刀，若纵容可强武备，若上报可保清白。", listOf(
            V3EventChoice("暗助修刀", "乡勇增强，官府不满。", silverDelta = -22, militiaDelta = 12, yamenDelta = -6, siteId = "mountain_pass", siteControlDelta = 5, route = V3Route.Warlord),
            V3EventChoice("上报县衙", "官府关系提升，武支失望。", yamenDelta = 7, militiaDelta = -3, siteId = "yamen", route = V3Route.Loyalist, branchImpacts = listOf(V3BranchImpact("martial", grievanceDelta = 4)))
        ))
    )

    val advancedPersonEvents = personArcSeeds.flatMap { seed ->
        listOf(
            V3ActiveEvent("${seed.name}请命", "${seed.name}请求亲自${seed.duty}。${seed.pressure}，族中等你定夺。", listOf(
                V3EventChoice("准其主事", "族人得以建功，但更疲惫。", silverDelta = -12, influenceDelta = 2, personId = seed.personId, personFatigueDelta = 8, personMeritDelta = 5, siteId = seed.siteId, siteControlDelta = 5, siteRiskDelta = -5, route = seed.route, branchImpacts = listOf(V3BranchImpact(seed.branchId, influenceDelta = 3, grievanceDelta = -2))),
                V3EventChoice("留中观望", "避免冒进，忠诚略升。", cohesionDelta = 2, personId = seed.personId, personLoyaltyDelta = 2, route = V3Route.Hermit)
            )),
            V3ActiveEvent("${seed.name}受议", "宗祠中有人质疑${seed.name}近来行事过急，若不表态，相关房支会继续争论。", listOf(
                V3EventChoice("公开支持", "相关房支归心，其他人观望。", cohesionDelta = 2, personId = seed.personId, personLoyaltyDelta = 4, route = seed.route, branchImpacts = listOf(V3BranchImpact(seed.branchId, loyaltyDelta = 4, grievanceDelta = -4))),
                V3EventChoice("按族规责备", "宗法威严上升，当事人受挫。", influenceDelta = 3, personId = seed.personId, personLoyaltyDelta = -3, personMeritDelta = -1, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact(seed.branchId, grievanceDelta = 4)))
            )),
            V3ActiveEvent("${seed.name}暗访", "${seed.name}愿夜访${seed.siteId}一带，查明地方传言。此举或有收获，也有风险。", listOf(
                V3EventChoice("拨人随行", "情报可靠，地点风险下降。", silverDelta = -16, personId = seed.personId, personFatigueDelta = 10, personMeritDelta = 4, siteId = seed.siteId, siteRiskDelta = -10, route = seed.route),
                V3EventChoice("独自前往", "省下人手，但疲劳更重。", personId = seed.personId, personFatigueDelta = 15, personMeritDelta = 5, siteId = seed.siteId, siteRiskDelta = -5, route = seed.route)
            )),
            V3ActiveEvent("${seed.name}收徒", "有族中后进愿拜${seed.name}为师。收徒可传本事，也会分散精力。", listOf(
                V3EventChoice("准其收徒", "族望与路线推进。", grainDelta = -8, influenceDelta = 3, personId = seed.personId, personFatigueDelta = 5, route = seed.route, routeDelta = 6, branchImpacts = listOf(V3BranchImpact(seed.branchId, influenceDelta = 2))),
                V3EventChoice("先办正事", "控制疲劳，凝聚稳定。", cohesionDelta = 2, personId = seed.personId, personFatigueDelta = -5, route = V3Route.Hermit)
            )),
            V3ActiveEvent("${seed.name}家书", "${seed.name}呈上一封家书，言及${seed.pressure}。若回应得当，可稳住其心。", listOf(
                V3EventChoice("亲笔回书", "忠诚与凝聚提升。", cohesionDelta = 3, personId = seed.personId, personLoyaltyDelta = 5, route = V3Route.Hermit),
                V3EventChoice("交房支处理", "房支自主上升，但主房控制下降。", personId = seed.personId, personMeritDelta = 2, route = seed.route, branchImpacts = listOf(V3BranchImpact(seed.branchId, influenceDelta = 3, wealthDelta = 1), V3BranchImpact("main", grievanceDelta = 2)))
            ))
        )
    }

    val branchEvents = listOf(
        V3ActiveEvent("主房议嗣", "主房族老要求明确下一任宗祠管事，以免乱世中号令不一。", listOf(
            V3EventChoice("立主房管事", "主房势力上升，诸房略有戒心。", cohesionDelta = 2, influenceDelta = 3, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 5, loyaltyDelta = 3), V3BranchImpact("merchant", grievanceDelta = 2), V3BranchImpact("martial", grievanceDelta = 2))),
            V3EventChoice("设轮值议事", "怨气下降，决策更慢。", cohesionDelta = 5, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", grievanceDelta = 2), V3BranchImpact("second", grievanceDelta = -3), V3BranchImpact("scholar", grievanceDelta = -2)))
        )),
        V3ActiveEvent("二房请赈", "二房认为族中应优先救济佃户和病患，否则民心将散。", listOf(
            V3EventChoice("拨粮赈济", "民心提升，二房归心。", grainDelta = -42, villagersDelta = 9, cohesionDelta = 4, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("second", loyaltyDelta = 5, grievanceDelta = -5))),
            V3EventChoice("只救族内", "资源压力较小，二房不满。", grainDelta = -12, cohesionDelta = 2, villagersDelta = -3, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("second", grievanceDelta = 5)))
        )),
        V3ActiveEvent("商支扩铺", "商支请以族产入股新铺，声称可解银荒。主房担心商支坐大。", listOf(
            V3EventChoice("准其扩铺", "银两增加，商支势力上升。", silverDelta = 85, merchantsDelta = 7, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", wealthDelta = 7, influenceDelta = 5, grievanceDelta = -4))),
            V3EventChoice("限制分润", "保住宗法权威，商支怨气上升。", influenceDelta = 3, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("merchant", grievanceDelta = 7, loyaltyDelta = -3), V3BranchImpact("main", influenceDelta = 2)))
        )),
        V3ActiveEvent("武支请械", "武支要求添置长枪弓弩，称山道不靖，不能空手守族。", listOf(
            V3EventChoice("购置军械", "乡勇增强，武支归心。", silverDelta = -55, militiaDelta = 18, route = V3Route.Fortress, branchImpacts = listOf(V3BranchImpact("martial", loyaltyDelta = 5, influenceDelta = 5, grievanceDelta = -5))),
            V3EventChoice("严禁私械", "官府放心，武支不满。", yamenDelta = 5, militiaDelta = -2, route = V3Route.Loyalist, branchImpacts = listOf(V3BranchImpact("martial", grievanceDelta = 8, loyaltyDelta = -3)))
        )),
        V3ActiveEvent("书香支请学田", "书香支希望划出学田供族学长久运转。", listOf(
            V3EventChoice("划拨学田", "士林路线推进。", grainDelta = -28, influenceDelta = 5, gentryDelta = 6, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("scholar", loyaltyDelta = 4, influenceDelta = 5, grievanceDelta = -5))),
            V3EventChoice("暂缓学田", "保住粮仓，书香支失望。", grainDelta = 12, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("scholar", grievanceDelta = 5)))
        )),
        V3ActiveEvent("海路支求船", "海路支称若置一条江船，便可通商避乱。族中疑其私心。", listOf(
            V3EventChoice("置船试航", "海外路线推进。", silverDelta = -80, merchantsDelta = 8, route = V3Route.Overseas, routeDelta = 10, branchImpacts = listOf(V3BranchImpact("sea", wealthDelta = 6, influenceDelta = 5, grievanceDelta = -4))),
            V3EventChoice("只许租船", "风险较小，推进有限。", silverDelta = -25, merchantsDelta = 3, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("sea", grievanceDelta = 2)))
        )),
        V3ActiveEvent("房支分粮", "秋收后各房争论分粮比例。若偏向一方，必伤另一方。", listOf(
            V3EventChoice("按丁均分", "凝聚提升，富房不满。", cohesionDelta = 5, grainDelta = -10, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("merchant", grievanceDelta = 3), V3BranchImpact("second", loyaltyDelta = 3))),
            V3EventChoice("按产分成", "商支满意，贫房不满。", silverDelta = 20, cohesionDelta = -3, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", loyaltyDelta = 3), V3BranchImpact("second", grievanceDelta = 4)))
        )),
        V3ActiveEvent("祠产审计", "族老提议清查祠产、铺账和田租。有人赞成，有人害怕旧账被翻。", listOf(
            V3EventChoice("公开审计", "追回资源，部分房支怨气上升。", silverDelta = 65, grainDelta = 25, influenceDelta = 3, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 3), V3BranchImpact("merchant", grievanceDelta = 4))),
            V3EventChoice("只查新账", "风波较小，收益有限。", silverDelta = 22, cohesionDelta = 2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("族老病重", "一位德高望重的族老病重，各房都在等他最后一句话。", listOf(
            V3EventChoice("厚礼延医", "凝聚与民心提升。", silverDelta = -40, cohesionDelta = 6, villagersDelta = 3, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", loyaltyDelta = 2), V3BranchImpact("second", influenceDelta = 2))),
            V3EventChoice("简办后事", "节省银两，但人情淡薄。", silverDelta = -8, cohesionDelta = -3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("庶支入谱", "外迁庶支携银归宗，请求重入族谱。主房担心谱系混乱。", listOf(
            V3EventChoice("验明入谱", "银两与凝聚提升。", silverDelta = 55, cohesionDelta = 4, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", grievanceDelta = 2), V3BranchImpact("second", loyaltyDelta = 3))),
            V3EventChoice("拒其入谱", "谱系清晰，但失去助力。", influenceDelta = 2, cohesionDelta = -4, route = V3Route.Hermit)
        )),
        V3ActiveEvent("强房逼议", "强势房支要求在宗祠议事中增加席位，否则拒绝出银出丁。", listOf(
            V3EventChoice("增设席位", "怨气下降，主房权威下降。", cohesionDelta = 4, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = -2, grievanceDelta = 3), V3BranchImpact("merchant", grievanceDelta = -3), V3BranchImpact("martial", grievanceDelta = -3))),
            V3EventChoice("维持旧制", "主房威严上升，强房怨气上升。", influenceDelta = 3, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 4), V3BranchImpact("merchant", grievanceDelta = 4), V3BranchImpact("martial", grievanceDelta = 4)))
        )),
        V3ActiveEvent("孤支求养", "一支孤弱族人无力过冬，请求宗祠拨粮。", listOf(
            V3EventChoice("宗祠赡养", "凝聚提升。", grainDelta = -25, cohesionDelta = 5, villagersDelta = 2, route = V3Route.Hermit),
            V3EventChoice("交邻房照看", "房支责任上升，怨气也增。", grainDelta = -8, cohesionDelta = 2, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("second", grievanceDelta = 2, loyaltyDelta = 2)))
        )),
        V3ActiveEvent("族中私塾", "几房合议开设私塾，但谁出钱、谁掌教又起争执。", listOf(
            V3EventChoice("宗祠出资", "书香路线推进。", silverDelta = -45, influenceDelta = 5, gentryDelta = 5, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("scholar", influenceDelta = 4, grievanceDelta = -4))),
            V3EventChoice("各房自办", "花费较少，凝聚下降。", silverDelta = -12, cohesionDelta = -2, route = V3Route.Scholar)
        )),
        V3ActiveEvent("嫁娶争礼", "族中婚礼礼金规格引发争议，富房要体面，贫房怕负担。", listOf(
            V3EventChoice("降低礼制", "贫房归心，富房不悦。", cohesionDelta = 5, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("second", loyaltyDelta = 3), V3BranchImpact("merchant", grievanceDelta = 3))),
            V3EventChoice("维持体面", "声望上升，贫房怨气上升。", silverDelta = -20, influenceDelta = 4, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("merchant", loyaltyDelta = 2), V3BranchImpact("second", grievanceDelta = 4)))
        )),
        V3ActiveEvent("族规重罚", "有人私卖族田被抓，各房都在看宗祠如何惩戒。", listOf(
            V3EventChoice("重罚立威", "声望上升，凝聚受损。", influenceDelta = 5, cohesionDelta = -3, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 4))),
            V3EventChoice("罚银留人", "追回银两，减少撕裂。", silverDelta = 35, cohesionDelta = 2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("祠堂借贷", "商支愿借银给宗祠应急，但要求来年田租优先偿还。", listOf(
            V3EventChoice("接受借贷", "短期银两充裕，商支坐大。", silverDelta = 100, cohesionDelta = -2, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", wealthDelta = 8, influenceDelta = 4))),
            V3EventChoice("拒绝借贷", "自主性保住，资源吃紧。", cohesionDelta = 3, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("merchant", grievanceDelta = 3)))
        )),
        V3ActiveEvent("武支护院", "武支提出常驻护院，主房担心祠堂变成军营。", listOf(
            V3EventChoice("准许护院", "安全上升，武支影响扩大。", militiaDelta = 10, banditsDelta = -4, route = V3Route.Fortress, branchImpacts = listOf(V3BranchImpact("martial", influenceDelta = 5, grievanceDelta = -3))),
            V3EventChoice("限定轮值", "平衡各房，效果有限。", cohesionDelta = 3, militiaDelta = 3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("海路分宗", "海路支建议在码头另立小祠，便于远行族人祭祖。", listOf(
            V3EventChoice("准立小祠", "海外路线与海路支提升。", silverDelta = -35, cohesionDelta = -1, route = V3Route.Overseas, routeDelta = 8, branchImpacts = listOf(V3BranchImpact("sea", influenceDelta = 6, loyaltyDelta = 4, grievanceDelta = -4))),
            V3EventChoice("祖祠不可分", "主房威严上升，海路支不满。", influenceDelta = 3, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 3), V3BranchImpact("sea", grievanceDelta = 6)))
        )),
        V3ActiveEvent("书香支清议", "书香支要求宗族在县中公开表态反对苛派。", listOf(
            V3EventChoice("附和清议", "士绅声望上升，县衙不悦。", influenceDelta = 6, gentryDelta = 8, yamenDelta = -6, route = V3Route.Scholar, branchImpacts = listOf(V3BranchImpact("scholar", influenceDelta = 5, grievanceDelta = -3))),
            V3EventChoice("不涉公议", "官府关系稳定，书香支不满。", yamenDelta = 4, cohesionDelta = 1, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("scholar", grievanceDelta = 5)))
        )),
        V3ActiveEvent("商支赈粥", "商支愿出钱设粥棚，但要在粥棚挂自家商号名。", listOf(
            V3EventChoice("准挂商号", "民心上升，商支得名。", villagersDelta = 8, cohesionDelta = 3, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", influenceDelta = 5, grievanceDelta = -3))),
            V3EventChoice("只挂宗祠名", "宗族声望上升，商支失望。", silverDelta = -25, influenceDelta = 5, villagersDelta = 5, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("merchant", grievanceDelta = 4)))
        )),
        V3ActiveEvent("二房药田", "二房请求把一块薄田改种药材，为医馆长用。", listOf(
            V3EventChoice("改作药田", "医馆与二房增强。", grainDelta = -18, villagersDelta = 5, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("second", wealthDelta = 3, influenceDelta = 4, grievanceDelta = -4))),
            V3EventChoice("仍种粮食", "保住粮仓，二房失望。", grainDelta = 25, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("second", grievanceDelta = 4)))
        )),
        V3ActiveEvent("主房藏契", "有人传言主房藏有几张旧契，若公开可平息争议，也会削弱主房权柄。", listOf(
            V3EventChoice("公开旧契", "凝聚提升，主房权威下降。", cohesionDelta = 6, route = V3Route.Hermit, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = -3, grievanceDelta = 4), V3BranchImpact("second", grievanceDelta = -3))),
            V3EventChoice("封存旧契", "主房稳住，诸房猜疑。", influenceDelta = 3, cohesionDelta = -3, route = V3Route.Warlord, branchImpacts = listOf(V3BranchImpact("main", influenceDelta = 4), V3BranchImpact("merchant", grievanceDelta = 3), V3BranchImpact("scholar", grievanceDelta = 3)))
        ))
    )

    val advancedStrategyEvents = listOf(
        V3ActiveEvent("巡抚清丈", "上官要清丈田亩，隐田旧账将无处可藏。", listOf(
            V3EventChoice("主动报田", "官府关系提升，粮银承压。", silverDelta = -50, grainDelta = -20, yamenDelta = 10, influenceDelta = 2, route = V3Route.Loyalist),
            V3EventChoice("联绅缓丈", "士绅关系提升，官府不满。", silverDelta = -25, gentryDelta = 9, yamenDelta = -6, route = V3Route.Scholar)
        )),
        V3ActiveEvent("边饷催急", "边地战事吃紧，县中再次催饷。", listOf(
            V3EventChoice("输银买安", "官府暂安。", silverDelta = -75, yamenDelta = 10, garrisonDelta = 4, route = V3Route.Loyalist),
            V3EventChoice("称灾缓缴", "保住资源，但官府记账。", grainDelta = -15, yamenDelta = -8, villagersDelta = 3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("流民成寨", "县外流民聚成小寨，既可招抚为佃，也可能变成匪患。", listOf(
            V3EventChoice("招为佃户", "粮食压力上升，民心提升。", grainDelta = -45, villagersDelta = 10, cohesionDelta = 3, route = V3Route.Hermit),
            V3EventChoice("驱散小寨", "风险下降，民心受损。", militiaDelta = -5, banditsDelta = -6, villagersDelta = -7, route = V3Route.Fortress)
        )),
        V3ActiveEvent("盐课风波", "盐税新规传至县中，商帮请李氏代为说项。", listOf(
            V3EventChoice("替商帮说项", "商帮关系提升。", silverDelta = -20, merchantsDelta = 10, yamenDelta = -3, route = V3Route.Merchant),
            V3EventChoice("避开盐事", "官府关系稳定，商帮失望。", yamenDelta = 3, merchantsDelta = -5, cohesionDelta = 2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("瘟疫封村", "邻村爆发疫病，县衙考虑封村。族中有人亲眷在内。", listOf(
            V3EventChoice("送药入村", "民心大升，资源下降。", silverDelta = -45, grainDelta = -20, villagersDelta = 12, route = V3Route.Hermit),
            V3EventChoice("配合封锁", "官府满意，乡民恐惧。", yamenDelta = 7, villagersDelta = -5, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("乡兵点验", "县衙要求点验各族乡兵，武备过多会被猜忌。", listOf(
            V3EventChoice("如实点验", "官府关系提升，割据路线受限。", yamenDelta = 8, militiaDelta = -6, route = V3Route.Loyalist),
            V3EventChoice("分散藏兵", "保留武备，官府风险上升。", yamenDelta = -8, militiaDelta = 8, route = V3Route.Warlord, routeDelta = 8)
        )),
        V3ActiveEvent("海禁严查", "巡海文书下县，码头商旅人人自危。", listOf(
            V3EventChoice("暂停海货", "风险下降，商路受损。", silverDelta = -35, yamenDelta = 6, merchantsDelta = -4, route = V3Route.Hermit),
            V3EventChoice("转走暗仓", "海外路线推进。", silverDelta = 45, yamenDelta = -8, merchantsDelta = 6, route = V3Route.Overseas, routeDelta = 8)
        )),
        V3ActiveEvent("军镇借粮", "过境军镇请借粮三百石，承诺来日偿还。", listOf(
            V3EventChoice("借粮结军", "军镇关系提升。", grainDelta = -80, garrisonDelta = 12, route = V3Route.Loyalist),
            V3EventChoice("只供一半", "保住粮仓，关系有限。", grainDelta = -35, garrisonDelta = 5, cohesionDelta = 2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("县令更替", "旧县令调任，新县令未明亲疏。各族争先递帖。", listOf(
            V3EventChoice("厚礼递帖", "官府关系重置向好。", silverDelta = -60, yamenDelta = 12, influenceDelta = 2, route = V3Route.Loyalist),
            V3EventChoice("由士绅引荐", "士绅路线推进。", silverDelta = -25, gentryDelta = 8, yamenDelta = 4, route = V3Route.Scholar)
        )),
        V3ActiveEvent("米船被扣", "县外米船被扣，商帮要李氏出面担保。", listOf(
            V3EventChoice("出面担保", "商路稳定，风险自担。", silverDelta = -30, grainDelta = 50, merchantsDelta = 8, route = V3Route.Merchant),
            V3EventChoice("拒绝担保", "避免牵连，商帮不满。", merchantsDelta = -6, cohesionDelta = 2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("清议榜文", "城中贴出清议榜文，称各族应共抗苛派。", listOf(
            V3EventChoice("暗中资助", "士林声望上升。", silverDelta = -25, gentryDelta = 10, yamenDelta = -5, route = V3Route.Scholar),
            V3EventChoice("撕榜避祸", "官府满意，士林失望。", yamenDelta = 6, gentryDelta = -6, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("山外招抚", "县衙考虑招抚一股山贼，想让李氏出面作保。", listOf(
            V3EventChoice("作保招抚", "流寇关系缓和，官府倚重。", yamenDelta = 6, banditsDelta = 9, influenceDelta = 3, route = V3Route.Warlord),
            V3EventChoice("拒作保人", "避免背锅，流寇不满。", cohesionDelta = 3, banditsDelta = -5, route = V3Route.Fortress)
        )),
        V3ActiveEvent("豪族会盟", "邻县豪族请李氏参加会盟，共议守望相助。", listOf(
            V3EventChoice("赴会结盟", "地方声望提升。", silverDelta = -35, influenceDelta = 8, gentryDelta = 5, route = V3Route.Warlord),
            V3EventChoice("婉拒会盟", "少涉纷争，凝聚提升。", cohesionDelta = 4, influenceDelta = -2, route = V3Route.Hermit)
        )),
        V3ActiveEvent("灾后重税", "灾年未过，税册又至。族中有人提议集体抗税。", listOf(
            V3EventChoice("集体缓缴", "民心提升，官府大怒。", villagersDelta = 10, yamenDelta = -12, cohesionDelta = 5, route = V3Route.Hermit),
            V3EventChoice("分户代缴", "官府满意，资源重压。", silverDelta = -80, yamenDelta = 10, cohesionDelta = -2, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("远商献图", "远商献上海外航路图，索价极高。", listOf(
            V3EventChoice("买下航图", "海外路线大进。", silverDelta = -110, merchantsDelta = 8, route = V3Route.Overseas, routeDelta = 12),
            V3EventChoice("临摹一份", "花费较少，但得罪远商。", silverDelta = -35, merchantsDelta = -3, route = V3Route.Overseas, routeDelta = 5)
        )),
        V3ActiveEvent("祠堂盟誓", "乱世渐深，族人请求在宗祠盟誓共守。", listOf(
            V3EventChoice("合族盟誓", "凝聚大增。", grainDelta = -25, cohesionDelta = 10, influenceDelta = 3, route = V3Route.Hermit, routeDelta = 8),
            V3EventChoice("各房自守", "资源不耗，凝聚下降。", cohesionDelta = -5, route = V3Route.Warlord)
        ))
    )

    val crisisRouteEvents = listOf(
        V3ActiveEvent("书院登科", "族中学子榜上有名，李氏可借此正式走向士林。", listOf(
            V3EventChoice("大办鹿鸣宴", "耕读终局基础增强。", silverDelta = -70, influenceDelta = 12, gentryDelta = 12, route = V3Route.Scholar, routeDelta = 14),
            V3EventChoice("低调入仕", "少花银两，官府关系提升。", yamenDelta = 8, influenceDelta = 5, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("商号通省", "李氏商号可并入省城大行，利润丰厚但受人牵制。", listOf(
            V3EventChoice("并入大行", "商业路线大进。", silverDelta = 120, merchantsDelta = 12, route = V3Route.Merchant, routeDelta = 14),
            V3EventChoice("自立字号", "风险更高，自主更强。", silverDelta = -45, influenceDelta = 6, route = V3Route.Merchant, routeDelta = 9)
        )),
        V3ActiveEvent("堡寨成军", "乡勇已成规模，邻村愿奉李氏为盟主。", listOf(
            V3EventChoice("立盟主旗", "堡寨路线大进。", militiaDelta = 25, villagersDelta = 8, influenceDelta = 8, route = V3Route.Fortress, routeDelta = 14),
            V3EventChoice("不称盟主", "民心稳定，避免官府猜忌。", yamenDelta = 4, cohesionDelta = 5, route = V3Route.Hermit)
        )),
        V3ActiveEvent("勤王军书", "朝廷军书至县，若李氏响应，将名列勤王义族。", listOf(
            V3EventChoice("奉书勤王", "勤王路线大进。", grainDelta = -80, militiaDelta = -15, yamenDelta = 14, garrisonDelta = 12, route = V3Route.Loyalist, routeDelta = 14),
            V3EventChoice("称病守县", "保住实力，声名有限。", cohesionDelta = 4, yamenDelta = -4, route = V3Route.Hermit)
        )),
        V3ActiveEvent("据县自保", "县令出逃，城中无主。族中强硬派请李氏接管粮仓与城门。", listOf(
            V3EventChoice("接管城门", "割据路线大进。", militiaDelta = 30, influenceDelta = 10, yamenDelta = -10, route = V3Route.Warlord, routeDelta = 16),
            V3EventChoice("迎回官印", "保留名义秩序。", yamenDelta = 10, influenceDelta = 5, route = V3Route.Loyalist)
        )),
        V3ActiveEvent("海船待发", "南风已起，海船可载一批族人远渡。", listOf(
            V3EventChoice("送族远渡", "海外路线大进。", silverDelta = -140, cohesionDelta = -4, merchantsDelta = 12, route = V3Route.Overseas, routeDelta = 16),
            V3EventChoice("只送货物", "保守获利。", silverDelta = 70, merchantsDelta = 6, route = V3Route.Merchant)
        )),
        V3ActiveEvent("闭乡十约", "族老拟定闭乡十约，要求减少外争、屯粮、禁奢。", listOf(
            V3EventChoice("颁行十约", "隐世路线大进。", grainDelta = 40, cohesionDelta = 12, villagersDelta = 6, route = V3Route.Hermit, routeDelta = 14),
            V3EventChoice("择要施行", "更灵活，推进较慢。", cohesionDelta = 5, route = V3Route.Hermit, routeDelta = 6)
        ))
    )

    val progressEvents = listOf(
        V3ActiveEvent("初立家门", "开局未久，邻里仍把李氏看作一户小家。若此时不立规矩，后续产业、人丁、婚配都会散乱。", listOf(
            V3EventChoice("立家规三条", "凝聚提升，主房立威。", cohesionDelta = 6, influenceDelta = 2, route = V3Route.Hermit),
            V3EventChoice("先求温饱", "获得粮食，暂缓立规。", grainDelta = 45, cohesionDelta = -1, route = V3Route.Hermit)
        )),
        V3ActiveEvent("媒人探门", "媒人到祠前打听李氏家底。若礼数周全，可为成家开路；若敷衍，婚事会迟。", listOf(
            V3EventChoice("备礼迎媒", "声望与婚配机会提升。", silverDelta = -35, grainDelta = -15, influenceDelta = 4, route = V3Route.Hermit),
            V3EventChoice("只问家世", "节省礼金，但显得寒酸。", silverDelta = -8, influenceDelta = -1, route = V3Route.Hermit)
        )),
        V3ActiveEvent("添丁议名", "新生儿或族中幼童渐多，族老请按辈分排字，以免日后谱牒混乱。", listOf(
            V3EventChoice("定下字辈", "族谱秩序提升。", silverDelta = -12, cohesionDelta = 6, influenceDelta = 2, route = V3Route.Hermit),
            V3EventChoice("各房自定", "省事但埋下分房隐患。", cohesionDelta = -3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("童蒙入塾", "族中孩童到了启蒙年纪，书香支建议请塾师，商支认为先学算盘更实用。", listOf(
            V3EventChoice("请塾师启蒙", "耕读路线推进。", silverDelta = -42, gentryDelta = 4, influenceDelta = 3, route = V3Route.Scholar, routeDelta = 8),
            V3EventChoice("先学账房", "商业路线推进。", silverDelta = -24, merchantsDelta = 5, route = V3Route.Merchant, routeDelta = 7)
        )),
        V3ActiveEvent("第一处铺面", "集市有小铺转让，若买下可补银入账；若错过，商路仍弱。", listOf(
            V3EventChoice("买下铺面", "商业根基提升。", silverDelta = -85, merchantsDelta = 8, siteId = "market", siteControlDelta = 9, route = V3Route.Merchant, routeDelta = 8),
            V3EventChoice("租摊试水", "花费较少，推进有限。", silverDelta = -25, merchantsDelta = 3, siteId = "market", siteControlDelta = 4, route = V3Route.Merchant)
        )),
        V3ActiveEvent("粮仓扩建", "田庄已有余粮，族老提议扩建粮仓，以备灾荒和兵乱。", listOf(
            V3EventChoice("扩建粮仓", "粮食安全提升。", silverDelta = -70, grainDelta = -30, cohesionDelta = 4, siteId = "farmland", siteRiskDelta = -10, route = V3Route.Hermit, routeDelta = 7),
            V3EventChoice("卖粮换银", "银两增加，抗灾下降。", silverDelta = 95, grainDelta = -120, merchantsDelta = 4, route = V3Route.Merchant, routeDelta = 6)
        )),
        V3ActiveEvent("族产成局", "田、铺、仓渐成体系，各房开始关心账权归谁。", listOf(
            V3EventChoice("宗祠统账", "凝聚和主房权威上升。", silverDelta = -20, cohesionDelta = 6, influenceDelta = 4, route = V3Route.Hermit),
            V3EventChoice("各房分账", "效率提高，凝聚下降。", silverDelta = 70, cohesionDelta = -4, route = V3Route.Merchant, branchImpacts = listOf(V3BranchImpact("merchant", wealthDelta = 5, influenceDelta = 3)))
        )),
        V3ActiveEvent("产业雇工", "产业渐多，族人不够用，需要雇外姓工匠和佃户。", listOf(
            V3EventChoice("按契雇工", "产业运转更稳。", silverDelta = -45, villagersDelta = 5, siteId = "market", siteControlDelta = 5, route = V3Route.Merchant),
            V3EventChoice("只用族人", "凝聚提升但扩张变慢。", cohesionDelta = 4, silverDelta = -10, route = V3Route.Hermit)
        )),
        V3ActiveEvent("宗祠大修", "族望渐起后，旧祠显得寒酸。修祠可聚族，也会消耗大量银粮。", listOf(
            V3EventChoice("大修宗祠", "凝聚与声望提升。", silverDelta = -120, grainDelta = -40, cohesionDelta = 10, influenceDelta = 8, siteId = "shrine", siteControlDelta = 10, route = V3Route.Hermit, routeDelta = 8),
            V3EventChoice("小修门面", "少花资源。", silverDelta = -35, cohesionDelta = 3, siteId = "shrine", siteControlDelta = 4, route = V3Route.Hermit)
        )),
        V3ActiveEvent("家丁成队", "乡勇已有规模，武支建议编为常备家丁，书香支担心犯忌。", listOf(
            V3EventChoice("编练家丁", "武备提升，官府猜忌。", silverDelta = -75, grainDelta = -45, militiaDelta = 28, yamenDelta = -8, route = V3Route.Warlord, routeDelta = 10),
            V3EventChoice("报备乡勇", "官府关系稳定。", silverDelta = -35, militiaDelta = 10, yamenDelta = 7, route = V3Route.Loyalist, routeDelta = 7)
        )),
        V3ActiveEvent("县中称族", "李氏人口、产业、声望已非小户，县中开始称其为一族。", listOf(
            V3EventChoice("设族长议事", "宗族治理提升。", silverDelta = -45, cohesionDelta = 8, influenceDelta = 6, route = V3Route.Hermit, routeDelta = 8),
            V3EventChoice("广交地方", "对外声望提升。", silverDelta = -65, gentryDelta = 7, merchantsDelta = 5, influenceDelta = 7, route = V3Route.Scholar, routeDelta = 7)
        )),
        V3ActiveEvent("府城来帖", "李氏名声传至府城，有人请你参与府城粮价与治安公议。", listOf(
            V3EventChoice("赴府公议", "府城影响力上升。", silverDelta = -80, influenceDelta = 10, gentryDelta = 8, route = V3Route.Scholar, routeDelta = 9),
            V3EventChoice("遣商支探路", "商路与府城关系推进。", silverDelta = -55, merchantsDelta = 9, influenceDelta = 5, route = V3Route.Merchant, routeDelta = 8)
        )),
        V3ActiveEvent("跨县置产", "邻县有人愿低价出让田铺，但当地宗族未必服李氏。", listOf(
            V3EventChoice("买田入县", "跨县根基提升。", silverDelta = -150, grainDelta = 80, influenceDelta = 8, route = V3Route.Merchant, routeDelta = 8),
            V3EventChoice("先结乡绅", "稳妥扩张。", silverDelta = -70, gentryDelta = 9, influenceDelta = 6, route = V3Route.Scholar, routeDelta = 7)
        )),
        V3ActiveEvent("府县会防", "流寇逼近，府县豪族议定共同守望。李氏若参加，便不再只是本县宗族。", listOf(
            V3EventChoice("出勇会防", "武备和跨县声望提升。", grainDelta = -65, militiaDelta = 22, influenceDelta = 9, route = V3Route.Fortress, routeDelta = 9),
            V3EventChoice("出粮不出人", "保住族人，声望有限。", grainDelta = -90, yamenDelta = 5, route = V3Route.Loyalist, routeDelta = 6)
        )),
        V3ActiveEvent("省城商约", "省城大商号愿与李氏合约，要求稳定供粮和码头份额。", listOf(
            V3EventChoice("签省城商约", "商业路线大进。", silverDelta = 160, grainDelta = -80, merchantsDelta = 12, route = V3Route.Merchant, routeDelta = 12),
            V3EventChoice("保留本县份额", "较稳但错失扩张。", silverDelta = 45, cohesionDelta = 3, route = V3Route.Hermit)
        )),
        V3ActiveEvent("一方豪强", "李氏已能左右府县粮价和治安，小族前来投靠，大族暗中试探。", listOf(
            V3EventChoice("收纳附族", "人口与声望提升，治理压力增加。", grainDelta = -80, cohesionDelta = -2, influenceDelta = 14, route = V3Route.Warlord, routeDelta = 12),
            V3EventChoice("结盟不并族", "风险较低，声望提升。", silverDelta = -60, influenceDelta = 8, gentryDelta = 6, route = V3Route.Scholar, routeDelta = 8)
        )),
        V3ActiveEvent("京畿风声", "京畿动荡传来，士绅问李氏是否仍奉朝廷，武支则言应自立旗号。", listOf(
            V3EventChoice("奉明勤王", "勤王路线大进。", silverDelta = -120, grainDelta = -120, militiaDelta = 35, yamenDelta = 12, garrisonDelta = 14, route = V3Route.Loyalist, routeDelta = 16),
            V3EventChoice("自保待变", "割据路线推进。", silverDelta = -70, grainDelta = -70, militiaDelta = 45, influenceDelta = 12, route = V3Route.Warlord, routeDelta = 16)
        )),
        V3ActiveEvent("天下邀盟", "各地豪族、商帮、军镇都在寻找可依附的新秩序，李氏已被推上牌桌。", listOf(
            V3EventChoice("立天下盟约", "统一路线推进。", silverDelta = -180, grainDelta = -160, militiaDelta = 60, influenceDelta = 18, route = V3Route.Warlord, routeDelta = 18),
            V3EventChoice("以商粮控局", "商业和统一路线并进。", silverDelta = -100, grainDelta = -220, merchantsDelta = 14, influenceDelta = 14, route = V3Route.Merchant, routeDelta = 14)
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
                V3EventChoice("主动操持", "花费资源换取本季主动权。", silverDelta = -22, grainDelta = -12, cohesionDelta = 3, influenceDelta = 2, siteId = seed.siteId, siteControlDelta = 7, siteRiskDelta = -6, route = seed.route, routeDelta = 6),
                V3EventChoice("节用观望", "少花资源，但只求不出乱子。", silverDelta = 10, cohesionDelta = 1, siteId = seed.siteId, siteControlDelta = 2, siteRiskDelta = 2, route = V3Route.Hermit, routeDelta = 3)
            )),
            V3ActiveEvent(seed.titleB, seed.bodyB, listOf(
                V3EventChoice("借机扩名", "用银粮换声望与路线推进。", silverDelta = -30, grainDelta = -10, influenceDelta = 4, siteId = seed.siteId, siteControlDelta = 5, route = seed.route, routeDelta = 7),
                V3EventChoice("只守家计", "保住家底，路线推进较慢。", grainDelta = 15, cohesionDelta = 2, siteId = seed.siteId, siteRiskDelta = -2, route = V3Route.Hermit, routeDelta = 3)
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
        CountyManagementSeed("academy", "东林书院", V3Route.Scholar, "讲会、束脩和党争清议暗流涌动", "士子"),
        CountyManagementSeed("clinic", "仁心医馆", V3Route.Hermit, "药材、病患和义诊开支日日紧逼", "郎中"),
        CountyManagementSeed("fort", "北山寨堡", V3Route.Fortress, "墙垣、哨探和乡勇粮饷都要补足", "武支"),
        CountyManagementSeed("dock", "三江码头", V3Route.Overseas, "船税、海货和巡查风声互相牵扯", "海商"),
        CountyManagementSeed("mountain_pass", "黑松山道", V3Route.Warlord, "塌方、私盐、流寇斥候都从此处冒头", "山民")
    )

    val countyManagementEvents = countyManagementSeeds.flatMap { seed ->
        listOf(
            V3ActiveEvent("${seed.siteName}月课", "${seed.siteName}本月事务繁杂：${seed.pressure}。${seed.ally}请李氏尽快定下月课，否则人心易散。", listOf(
                V3EventChoice("定月课", "地点秩序提升，路线更清楚。", silverDelta = -24, cohesionDelta = 2, siteId = seed.siteId, siteControlDelta = 9, siteRiskDelta = -5, route = seed.route, routeDelta = 6),
                V3EventChoice("缓一月", "暂省开销，但地点风险上升。", silverDelta = 12, siteId = seed.siteId, siteRiskDelta = 6, route = V3Route.Hermit, routeDelta = 2)
            )),
            V3ActiveEvent("${seed.siteName}旧账", "${seed.siteName}翻出一笔旧账，牵涉${seed.ally}与宗祠用度。若公开查账，可能得罪人；若不查，隐患会留到后面。", listOf(
                V3EventChoice("公开清账", "追回银粮，地点控制上升。", silverDelta = 38, grainDelta = 16, influenceDelta = 2, siteId = seed.siteId, siteControlDelta = 7, route = seed.route, routeDelta = 5),
                V3EventChoice("私下抹平", "凝聚暂稳，少得收益。", silverDelta = -16, cohesionDelta = 4, siteId = seed.siteId, siteRiskDelta = -3, route = V3Route.Hermit, routeDelta = 3)
            )),
            V3ActiveEvent("${seed.siteName}添役", "${seed.siteName}要继续运转，必须添人添役。${seed.ally}愿出面帮忙，但要宗祠给出名分和口粮。", listOf(
                V3EventChoice("添役办事", "花粮换效率。", grainDelta = -28, siteId = seed.siteId, siteControlDelta = 10, siteRiskDelta = -7, route = seed.route, routeDelta = 6),
                V3EventChoice("仍用旧人", "省粮但效率有限。", cohesionDelta = 1, siteId = seed.siteId, siteControlDelta = 3, siteRiskDelta = 3, route = V3Route.Hermit, routeDelta = 2)
            )),
            V3ActiveEvent("${seed.siteName}外争", "外族或小吏开始插手${seed.siteName}，试探李氏底线。若退让，日后更难收回；若强硬，局势会紧。", listOf(
                V3EventChoice("强硬收回", "控制大升，但风险也抬头。", influenceDelta = 4, siteId = seed.siteId, siteControlDelta = 14, siteRiskDelta = 5, route = V3Route.Warlord, routeDelta = 6),
                V3EventChoice("请人调停", "稳妥降险。", silverDelta = -26, siteId = seed.siteId, siteControlDelta = 6, siteRiskDelta = -9, route = seed.route, routeDelta = 5)
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
                V3EventChoice("写入议纲", "路线明确推进。", silverDelta = -36, influenceDelta = 5, siteId = seed.costSite, siteControlDelta = 5, route = seed.route, routeDelta = 10),
                V3EventChoice("暂不定纲", "保持灵活，凝聚略升。", cohesionDelta = 3, route = V3Route.Hermit, routeDelta = 3)
            )),
            V3ActiveEvent("${seed.label}荐才", "有人推荐一批适合${seed.label}路线的人手。收下要花银粮，不收则错过扩张时机。", listOf(
                V3EventChoice("收纳荐才", "路线人才入局。", silverDelta = -42, grainDelta = -22, influenceDelta = 4, route = seed.route, routeDelta = 9),
                V3EventChoice("只留名册", "花费较少，推进有限。", silverDelta = -12, route = seed.route, routeDelta = 4)
            )),
            V3ActiveEvent("${seed.label}据点", "${seed.costSite}一带可成为${seed.label}路线据点。若集中投入，后续事件会更偏向此路。", listOf(
                V3EventChoice("集中投入", "据点控制提高。", silverDelta = -58, grainDelta = -28, siteId = seed.costSite, siteControlDelta = 13, siteRiskDelta = -6, route = seed.route, routeDelta = 11),
                V3EventChoice("分散投入", "稳健但不突出。", silverDelta = -24, cohesionDelta = 2, siteId = seed.costSite, siteControlDelta = 5, route = V3Route.Hermit, routeDelta = 4)
            )),
            V3ActiveEvent("${seed.label}声名", "外界已开始用${seed.label}二字评价李氏。此时若顺势造势，可快速抬高名望。", listOf(
                V3EventChoice("顺势造势", "声望和路线大进。", silverDelta = -50, influenceDelta = 9, route = seed.route, routeDelta = 12),
                V3EventChoice("低调蓄势", "少花银两，风险更低。", cohesionDelta = 4, route = V3Route.Hermit, routeDelta = 4)
            )),
            V3ActiveEvent("${seed.label}分歧", "族中有人质疑继续押注${seed.label}路线会拖累家业，要求回到田粮和香火本位。", listOf(
                V3EventChoice("坚持此路", "路线更强，凝聚略损。", cohesionDelta = -2, influenceDelta = 4, route = seed.route, routeDelta = 12),
                V3EventChoice("兼顾保族", "凝聚恢复，路线放缓。", grainDelta = -16, cohesionDelta = 6, route = V3Route.Hermit, routeDelta = 5)
            )),
            V3ActiveEvent("${seed.label}成局", "多年经营后，${seed.symbol}已不是口号，而是李氏真正的活路。下一步要决定是扩张还是守成。", listOf(
                V3EventChoice("继续扩张", "路线终局伏笔增强。", silverDelta = -85, grainDelta = -45, influenceDelta = 10, route = seed.route, routeDelta = 16),
                V3EventChoice("转入守成", "资源压力下降，保族路线增强。", grainDelta = 35, cohesionDelta = 7, route = V3Route.Hermit, routeDelta = 6)
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
                V3EventChoice("顺势应对", "正面处理时代压力。", silverDelta = -48, grainDelta = -28, influenceDelta = 5, siteId = seed.siteId, siteControlDelta = 8, siteRiskDelta = -5, route = seed.routeA, routeDelta = 9),
                V3EventChoice("保族缓冲", "减少冲击，转向守成。", grainDelta = -18, cohesionDelta = 5, siteId = seed.siteId, siteRiskDelta = -3, route = seed.routeB, routeDelta = 6)
            )),
            V3ActiveEvent("${seed.title}余波", "${seed.body} 此事虽暂歇，余波仍在县中发酵，各房都要求宗祠给出后续章程。", listOf(
                V3EventChoice("定后续章程", "长期路线更清楚。", silverDelta = -30, cohesionDelta = 3, influenceDelta = 4, route = seed.routeA, routeDelta = 8),
                V3EventChoice("只补眼前缺口", "资源压力较小。", silverDelta = -8, grainDelta = 18, route = V3Route.Hermit, routeDelta = 4)
            ))
        )
    }

    val allEvents = siteEvents + advancedSiteEvents + seasonalEvents + countyManagementEvents + personEvents + advancedPersonEvents + branchEvents + strategyEvents + advancedStrategyEvents + routeEvents + routeMilestoneEvents + crisisRouteEvents + eraPressureEvents + progressEvents
}
