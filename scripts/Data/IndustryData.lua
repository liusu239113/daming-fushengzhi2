-- ============================================================================
-- 大明浮生志2 - 产业数据模块
-- 管理产业类型、进化、解锁等产业相关配置
-- 从 GameData.lua 中拆分出来
-- ============================================================================

local IndustryData = {}

-- 产业类型（18种，按品级逐步解锁）
-- resource2/baseOutput2: 可选的第二产出资源
-- specialEffect: 特殊效果标识（需在MonthlyUpdate中处理）
-- evolvesTo: 可进化为的高级产业ID（需满足进化条件）
IndustryData.INDUSTRY_TYPES = {
    -- === 寒门（1级）===
    { id = "dry_field",   name = "旱田", resource = "grain", baseOutput = 8,  cost = 150,
      desc = "产粮食，受天灾影响大", evolvesTo = "paddy_field" },
    { id = "hemp_field",  name = "麻田", resource = "cloth", baseOutput = 3,  cost = 120,
      desc = "种麻织布，前期布匹来源", evolvesTo = "workshop" },

    -- === 农户（2级）===
    { id = "paddy_field", name = "水田", resource = "grain", baseOutput = 14, cost = 300,
      desc = "产量高，需更多人手", evolvesTo = "fertile_field" },
    { id = "fish_pond",   name = "鱼塘", resource = "grain", baseOutput = 10, cost = 240,
      desc = "四季有鱼，产出稳定不受天灾影响", specialEffect = "weather_immune" },
    { id = "livestock",   name = "畜栏", resource = "grain", baseOutput = 6,  cost = 200,
      desc = "养猪牧羊，产粮食兼出少量布匹",
      resource2 = "cloth", baseOutput2 = 3 },
    { id = "handicraft",  name = "手工作坊", resource = "silver", baseOutput = 4, cost = 260,
      desc = "编篮制陶，前期银两来源" },

    -- === 乡绅（3级）===
    { id = "shop",        name = "商铺", resource = "silver", baseOutput = 10, cost = 450,
      desc = "产银两，乱世收益波动", evolvesTo = "trade_house" },
    { id = "tea_garden",  name = "茶园", resource = "silver", baseOutput = 8,  cost = 380,
      desc = "茶叶远销，兼得薄名",
      resource2 = "fame", baseOutput2 = 2 },
    { id = "inn",         name = "客栈", resource = "silver", baseOutput = 7,  cost = 340,
      desc = "南来北往，打探消息兼赚银两" },
    { id = "herb_shop",   name = "药铺", resource = "silver", baseOutput = 5,  cost = 420,
      desc = "济世救人，全族生病概率-15%", specialEffect = "reduce_sick_15" },

    -- === 望族（4级）===
    { id = "workshop",    name = "织坊", resource = "cloth", baseOutput = 6,  cost = 280,
      desc = "产布匹，稳定收益", evolvesTo = "silk_house" },
    { id = "fort",        name = "寨堡", resource = "none",  baseOutput = 0,  cost = 800,
      desc = "降低流寇劫掠损失30%" },
    { id = "brewery",     name = "酒坊", resource = "silver", baseOutput = 12, cost = 500,
      desc = "以粮酿酒，利润丰厚但每月额外消耗粮食3", specialEffect = "consume_grain_3" },
    { id = "smithy",      name = "铁匠铺", resource = "silver", baseOutput = 6, cost = 480,
      desc = "打造兵器农具，兼得名望",
      resource2 = "fame", baseOutput2 = 3, specialEffect = "martial_grow_10" },

    -- === 世家（5级）===
    { id = "horse_ranch", name = "马场", resource = "fame", baseOutput = 4,   cost = 560,
      desc = "饲养良驹，从军族人战力+15%", specialEffect = "military_boost_15" },
    { id = "escort",      name = "镖局", resource = "silver", baseOutput = 18, cost = 700,
      desc = "押镖走货，高收益但有折损风险", specialEffect = "risk_loss_20" },
    { id = "bookshop",    name = "书坊", resource = "fame", baseOutput = 6,   cost = 650,
      desc = "刊印经书，声名远扬兼赚银两",
      resource2 = "silver", baseOutput2 = 5, specialEffect = "study_grow_10" },
    { id = "salt_field",  name = "盐场", resource = "silver", baseOutput = 25, cost = 1000,
      desc = "贩盐暴利，但官府稽查严厉", specialEffect = "risk_tax_30" },

    -- === 勋贵（6级）===
    { id = "money_house", name = "钱庄", resource = "silver", baseOutput = 20, cost = 850,
      desc = "放贷收息，银两按总资产额外生利", specialEffect = "interest_2pct" },
    { id = "fleet",       name = "船队", resource = "silver", baseOutput = 32, cost = 1400,
      desc = "海上贸易，利润极高但受季风影响", specialEffect = "season_amplify" },
    { id = "estate",      name = "庄园", resource = "grain", baseOutput = 10, cost = 1200,
      desc = "良田美宅，粮银名三收",
      resource2 = "silver", baseOutput2 = 8,
      resource3 = "fame", baseOutput3 = 3 },

    -- === 名门（7级）===
    { id = "pawnshop",    name = "当铺", resource = "silver", baseOutput = 26, cost = 1600,
      desc = "典当质押，乱世暴利；每月额外按银两存量+1%收益", specialEffect = "interest_1pct",
      evolvesTo = "piaohao" },
    { id = "dye_house",   name = "染坊", resource = "cloth", baseOutput = 12, cost = 1300,
      desc = "染制锦缎彩绸，布匹兼赚银两",
      resource2 = "silver", baseOutput2 = 12 },
    { id = "private_school", name = "私塾", resource = "fame", baseOutput = 10, cost = 1100,
      desc = "开馆授徒，声名远播；全族学识成长+10%", specialEffect = "study_grow_10",
      resource2 = "silver", baseOutput2 = 8 },
    { id = "canal_wharf", name = "漕运码头", resource = "silver", baseOutput = 38, cost = 1800,
      desc = "扼守漕运要道，南北货物中转抽成", specialEffect = "trade_hub" },

    -- === 豪阀（8级）===
    { id = "arsenal",     name = "军械坊", resource = "silver", baseOutput = 30, cost = 2200,
      desc = "铸造兵器甲胄，供给官军；从军族人战力+25%",
      resource2 = "fame", baseOutput2 = 8, specialEffect = "military_boost_25" },
    { id = "weaving_bureau", name = "织造局", resource = "cloth", baseOutput = 20, cost = 2000,
      desc = "承接官府织造，丝绸贡品；布匹银两声望三收",
      resource2 = "silver", baseOutput2 = 22,
      resource3 = "fame", baseOutput3 = 6 },

    -- === 国柱（9级）===
    { id = "imperial_merchant", name = "皇商行", resource = "silver", baseOutput = 60, cost = 4000,
      desc = "皇家特许经营，垄断盐铁茶叶；暴利但每月消耗声望3", specialEffect = "consume_fame_3" },
    { id = "customs_house", name = "海关行", resource = "silver", baseOutput = 50, cost = 3500,
      desc = "把持海关贸易，坐收厘金；银两兼得声望",
      resource2 = "fame", baseOutput2 = 10 },
    { id = "grand_farmland", name = "万亩良田", resource = "grain", baseOutput = 45, cost = 3000,
      desc = "良田万亩，粮仓丰盈；粮食银两双收，不受天灾影响",
      resource2 = "silver", baseOutput2 = 20, specialEffect = "weather_immune" },
}

-- 产业进化表：从低级进化为高级（需产业等级>=3 + 品级达标 + 银两）
IndustryData.INDUSTRY_EVOLUTION = {
    dry_field    = { to = "paddy_field",   reqLevel = 3, reqRank = 2, cost = 240,
                     desc = "旱田改水利，升级为水田" },
    hemp_field   = { to = "workshop",     reqLevel = 3, reqRank = 4, cost = 320,
                     desc = "添置织机，升级为织坊" },
    paddy_field  = { to = "fertile_field", reqLevel = 3, reqRank = 4, cost = 480,
                     desc = "精耕细作，升级为良田" },
    shop         = { to = "trade_house",   reqLevel = 3, reqRank = 5, cost = 600,
                     desc = "扩大经营，升级为商号" },
    workshop     = { to = "silk_house",    reqLevel = 3, reqRank = 5, cost = 550,
                     desc = "改织丝绸，升级为绸缎庄" },
    money_house  = { to = "piaohao",      reqLevel = 3, reqRank = 8, cost = 1200,
                     desc = "扩张网络，升级为票号" },
    pawnshop     = { to = "piaohao",      reqLevel = 3, reqRank = 8, cost = 1000,
                     desc = "典当升级，升级为票号" },
    escort       = { to = "canal_wharf",  reqLevel = 3, reqRank = 7, cost = 900,
                     desc = "镖路转漕运，升级为漕运码头" },
    salt_field   = { to = "customs_house",reqLevel = 3, reqRank = 9, cost = 2000,
                     desc = "盐政转关务，升级为海关行" },
    estate       = { to = "grand_farmland", reqLevel = 3, reqRank = 9, cost = 1800,
                     desc = "良田扩展，升级为万亩良田" },
}

-- 进化后的高级产业（不在建造菜单中出现，只能通过进化获得）
IndustryData.EVOLVED_INDUSTRY_TYPES = {
    { id = "fertile_field", name = "良田", resource = "grain", baseOutput = 22, cost = 500,
      desc = "精耕良田，产量极高", evolved = true },
    { id = "trade_house",   name = "商号", resource = "silver", baseOutput = 18, cost = 600,
      desc = "连锁商号，日进斗金",
      resource2 = "fame", baseOutput2 = 3, evolved = true },
    { id = "silk_house",    name = "绸缎庄", resource = "cloth", baseOutput = 12, cost = 500,
      desc = "丝绸锦缎，布匹兼赚银两",
      resource2 = "silver", baseOutput2 = 5, evolved = true },
    { id = "piaohao",     name = "票号", resource = "silver", baseOutput = 35, cost = 1200,
      desc = "汇通天下，跨省银票兑付；按总资产额外生利2%",
      specialEffect = "interest_2pct", evolved = true },
}

-- 注册进化产业到查询表中（让 GetIndustryType 能找到）
for _, evo in ipairs(IndustryData.EVOLVED_INDUSTRY_TYPES) do
    IndustryData.INDUSTRY_TYPES[#IndustryData.INDUSTRY_TYPES + 1] = evo
end

--- 产业类型解锁要求
IndustryData.INDUSTRY_UNLOCK = {
    -- 寒门（1级）
    dry_field    = 1,
    hemp_field   = 1,
    -- 农户（2级）
    paddy_field  = 2,
    fish_pond    = 2,
    livestock    = 2,
    handicraft   = 2,
    -- 乡绅（3级）
    shop         = 3,
    tea_garden   = 3,
    inn          = 3,
    herb_shop    = 3,
    -- 望族（4级）
    workshop     = 4,
    fort         = 4,
    brewery      = 4,
    smithy       = 4,
    -- 世家（5级）
    horse_ranch  = 5,
    escort       = 5,
    bookshop     = 5,
    salt_field   = 5,
    -- 勋贵（6级）
    money_house  = 6,
    fleet        = 6,
    estate       = 6,
    -- 名门（7级）
    pawnshop        = 7,
    dye_house       = 7,
    private_school  = 7,
    canal_wharf     = 7,
    -- 豪阀（8级）
    arsenal         = 8,
    weaving_bureau  = 8,
    -- 国柱（9级）
    imperial_merchant = 9,
    customs_house     = 9,
    grand_farmland    = 9,
}

--- 查找产业类型配置
function IndustryData.GetIndustryType(typeId)
    for _, t in ipairs(IndustryData.INDUSTRY_TYPES) do
        if t.id == typeId then return t end
    end
    return nil
end

return IndustryData
