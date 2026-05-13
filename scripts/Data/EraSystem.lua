-- ============================================================================
-- 大明浮生志2 - 年号系统与历史事件时间线
-- 涵盖大明全朝（1368-1644），276年真实历史
-- ============================================================================

local EraSystem = {}

-- ============================================================================
-- 明朝年号表（17个年号）
-- ============================================================================

EraSystem.ERA_NAMES = {
    { name = "洪武", emperor = "太祖朱元璋",  startYear = 1368, endYear = 1398 },
    { name = "建文", emperor = "惠帝朱允炆",  startYear = 1399, endYear = 1402 },
    { name = "永乐", emperor = "成祖朱棣",    startYear = 1403, endYear = 1424 },
    { name = "洪熙", emperor = "仁宗朱高炽",  startYear = 1425, endYear = 1425 },
    { name = "宣德", emperor = "宣宗朱瞻基",  startYear = 1426, endYear = 1435 },
    { name = "正统", emperor = "英宗朱祁镇",  startYear = 1436, endYear = 1449 },
    { name = "景泰", emperor = "代宗朱祁钰",  startYear = 1450, endYear = 1456 },
    { name = "天顺", emperor = "英宗朱祁镇",  startYear = 1457, endYear = 1464 },
    { name = "成化", emperor = "宪宗朱见深",  startYear = 1465, endYear = 1487 },
    { name = "弘治", emperor = "孝宗朱祐樘",  startYear = 1488, endYear = 1505 },
    { name = "正德", emperor = "武宗朱厚照",  startYear = 1506, endYear = 1521 },
    { name = "嘉靖", emperor = "世宗朱厚熜",  startYear = 1522, endYear = 1566 },
    { name = "隆庆", emperor = "穆宗朱载坖",  startYear = 1567, endYear = 1572 },
    { name = "万历", emperor = "神宗朱翊钧",  startYear = 1573, endYear = 1620 },
    { name = "泰昌", emperor = "光宗朱常洛",  startYear = 1620, endYear = 1620 },
    { name = "天启", emperor = "熹宗朱由校",  startYear = 1621, endYear = 1627 },
    { name = "崇祯", emperor = "思宗朱由检",  startYear = 1628, endYear = 1644 },
}

--- 根据绝对年份获取年号信息
--- @param absYear number 绝对年份（如1368）
--- @return string eraName 年号名
--- @return number eraYear 年号第几年
--- @return string emperor 皇帝称号
function EraSystem.GetEraInfo(absYear)
    -- 特殊处理泰昌元年（1620年同时属于万历48年和泰昌元年）
    -- 游戏中按月推进，年份+1时触发，此处泰昌只有1620年
    for i = #EraSystem.ERA_NAMES, 1, -1 do
        local era = EraSystem.ERA_NAMES[i]
        if absYear >= era.startYear then
            local eraYear = absYear - era.startYear + 1
            return era.name, eraYear, era.emperor
        end
    end
    return "洪武", 1, "太祖朱元璋"
end

--- 获取年份显示字符串，如 "洪武元年" "永乐三年"
--- @param absYear number 绝对年份
--- @return string 如 "洪武元年(1368)"
function EraSystem.GetYearLabel(absYear)
    local eraName, eraYear = EraSystem.GetEraInfo(absYear)
    local yearStr
    if eraYear == 1 then
        yearStr = "元"
    else
        yearStr = tostring(eraYear)
    end
    return eraName .. yearStr .. "年"
end

--- 获取带公元年的完整标签
--- @param absYear number
--- @return string 如 "洪武元年(1368)"
function EraSystem.GetFullYearLabel(absYear)
    return EraSystem.GetYearLabel(absYear) .. "(" .. absYear .. ")"
end

-- ============================================================================
-- 明朝历史大事件时间线（1368-1644）
-- 按朝代分期，effect 对应 MonthlyUpdate.CheckHistoryEvent 中的处理逻辑
-- ============================================================================

EraSystem.HISTORY_EVENTS = {
    -- === 洪武朝（1368-1398）：开国定鼎 ===
    { year = 1368, title = "大明开国",
      desc = "朱元璋于应天府称帝，国号大明，年号洪武。徐达北伐，攻克大都，蒙元北遁。",
      effect = "era_start" },
    { year = 1370, title = "大封功臣",
      desc = "太祖大封开国功臣，徐达、常遇春等封公侯。",
      effect = "fame_boost" },
    { year = 1373, title = "编户齐民",
      desc = "朝廷推行黄册制度，清查户口田亩，编户齐民，赋役渐重。",
      effect = "tax_reform" },
    { year = 1380, title = "胡惟庸案",
      desc = "丞相胡惟庸谋反伏诛，太祖废丞相制，株连甚广，朝野震动。",
      effect = "political_purge" },
    { year = 1385, title = "空印案",
      desc = "空印案发，牵连数百官员被诛，天下官吏人心惶惶。",
      effect = "political_purge" },
    { year = 1390, title = "蓝玉案",
      desc = "凉国公蓝玉以谋反罪被诛，牵连万余人，功臣几乎殆尽。",
      effect = "political_purge" },
    { year = 1393, title = "鱼鳞图册",
      desc = "朝廷编制鱼鳞图册，丈量天下田亩，赋税更加严密。",
      effect = "tax_up" },
    { year = 1398, title = "太祖驾崩",
      desc = "洪武帝崩于南京，皇太孙朱允炆即位，年号建文。",
      effect = "emperor_change" },

    -- === 建文-永乐朝（1399-1424）：靖难与盛世 ===
    { year = 1399, title = "靖难之役",
      desc = "燕王朱棣起兵靖难，南北交战四年，天下纷扰，百姓苦于兵火。",
      effect = "war_civil" },
    { year = 1402, title = "燕王入京",
      desc = "朱棣攻入南京，建文帝下落不明。朱棣即位，改元永乐。",
      effect = "emperor_change" },
    { year = 1405, title = "郑和下西洋",
      desc = "三宝太监郑和率船队首次远航西洋，扬国威于海外，通商互利。",
      effect = "trade_boost" },
    { year = 1410, title = "永乐北征",
      desc = "成祖亲征漠北，大败鞑靼，边塞暂安。然军费浩繁，赋役加重。",
      effect = "military_draft" },
    { year = 1421, title = "迁都北京",
      desc = "朝廷正式迁都北京，南京为留都。天子守国门，南方赋税北运。",
      effect = "capital_move" },
    { year = 1424, title = "成祖驾崩",
      desc = "永乐帝崩于北征途中，太子朱高炽即位，年号洪熙，施政宽仁。",
      effect = "emperor_change" },

    -- === 仁宣之治（1425-1435）：太平盛世 ===
    { year = 1425, title = "仁宗即位",
      desc = "仁宗轻徭薄赋，与民休息，虽在位不足一年，却开太平之基。",
      effect = "tax_relief" },
    { year = 1426, title = "宣宗即位",
      desc = "宣宗朱瞻基即位，年号宣德。文治武功，号称仁宣之治。",
      effect = "prosperity" },
    { year = 1430, title = "海禁渐严",
      desc = "朝廷收紧海禁，郑和最后一次下西洋后，海外贸易渐趋萧条。",
      effect = "trade_decline" },

    -- === 正统-景泰-天顺（1436-1464）：土木之变 ===
    { year = 1436, title = "正统即位",
      desc = "英宗年幼即位，太皇太后与三杨辅政，朝局尚稳。",
      effect = "emperor_change" },
    { year = 1449, title = "土木堡之变",
      desc = "英宗亲征瓦剌，于土木堡大败被俘！京师震动，举国惶恐。",
      effect = "military_crisis" },
    { year = 1450, title = "景泰即位·北京保卫战",
      desc = "于谦拥立郕王为帝，率军击退瓦剌，保全社稷。",
      effect = "war_defense" },
    { year = 1457, title = "夺门之变",
      desc = "英宗复辟，于谦被杀，朝局动荡，改元天顺。",
      effect = "political_purge" },

    -- === 成化-弘治（1465-1505）：中兴与承平 ===
    { year = 1465, title = "成化即位",
      desc = "宪宗即位，早年勤政，后渐怠政，西厂横行，朝政渐坏。",
      effect = "emperor_change" },
    { year = 1471, title = "荆襄流民",
      desc = "荆襄一带流民数十万聚集，朝廷招抚设府，流民问题凸显。",
      effect = "refugees" },
    { year = 1488, title = "弘治中兴",
      desc = "孝宗即位，勤勉仁厚，远奸佞、亲贤臣，天下称弘治中兴。",
      effect = "prosperity" },

    -- === 正德朝（1506-1521）：荒唐天子 ===
    { year = 1506, title = "正德即位",
      desc = "武宗即位，宠信刘瑾等八虎宦官，朝政日非。",
      effect = "emperor_change" },
    { year = 1510, title = "安化王叛乱",
      desc = "安化王以诛刘瑾为名叛乱，虽很快平定，但朝野不安。",
      effect = "rebellion" },
    { year = 1519, title = "宁王之乱",
      desc = "宁王朱宸濠起兵谋反，王阳明仅用四十三日平定叛乱。",
      effect = "rebellion" },

    -- === 嘉靖朝（1522-1566）：倭寇与朝争 ===
    { year = 1522, title = "嘉靖即位",
      desc = "世宗以藩王入继大统，大礼议之争起，朝堂分裂。",
      effect = "emperor_change" },
    { year = 1529, title = "大礼议定",
      desc = "嘉靖帝在大礼议中获胜，群臣廷杖，朝局为之一变。",
      effect = "political_purge" },
    { year = 1542, title = "壬寅宫变",
      desc = "宫女谋刺嘉靖帝未遂，帝受惊后移居西苑，不再上朝，沉迷修道。",
      effect = "governance_decline" },
    { year = 1547, title = "倭寇猖獗",
      desc = "东南沿海倭寇大肆侵扰，烧杀抢掠，沿海百姓苦不堪言。",
      effect = "bandit_up" },
    { year = 1555, title = "戚继光抗倭",
      desc = "戚继光练兵浙东，戚家军屡败倭寇，东南渐安。",
      effect = "military_victory" },
    { year = 1566, title = "嘉靖驾崩",
      desc = "世宗崩，穆宗即位，改元隆庆，开放海禁，史称隆庆开关。",
      effect = "emperor_change" },

    -- === 隆庆-万历前期（1567-1600）：张居正改革 ===
    { year = 1567, title = "隆庆开关",
      desc = "穆宗开放海禁，准许民间出海贸易，海外白银大量流入。",
      effect = "trade_boost" },
    { year = 1573, title = "万历即位",
      desc = "神宗年幼即位，张居正辅政，推行改革，吏治清明。",
      effect = "emperor_change" },
    { year = 1578, title = "一条鞭法",
      desc = "张居正推行一条鞭法，统一赋役，清丈田亩，国库充盈。",
      effect = "tax_reform" },
    { year = 1582, title = "张居正病逝",
      desc = "首辅张居正病逝，万历帝清算其党，改革成果渐遭废弃。",
      effect = "governance_decline" },

    -- === 万历中后期（1600-1620）：三大征与党争 ===
    { year = 1592, title = "万历三大征",
      desc = "宁夏、播州、朝鲜三大征接连发起，虽战胜但国力大耗。",
      effect = "military_drain" },
    { year = 1601, title = "万历怠政",
      desc = "万历帝数十年不上朝，朝中缺官过半，政务荒废，党争渐烈。",
      effect = "governance_decline" },
    { year = 1615, title = "东林党争",
      desc = "东林党与阉党之争愈演愈烈，梃击案、红丸案迭起，朝局混乱。",
      effect = "political_chaos" },
    { year = 1616, title = "后金崛起",
      desc = "努尔哈赤建立后金，统一女真各部，辽东边患日益严重。",
      effect = "border_threat" },
    { year = 1619, title = "萨尔浒之战",
      desc = "明军四路出击后金，于萨尔浒惨败，辽东局势急转直下。",
      effect = "military_crisis" },

    -- === 天启-崇祯（1620-1644）：末世乱局 ===
    { year = 1620, title = "泰昌即位",
      desc = "光宗即位仅一月即崩，红丸案震惊朝野，熹宗即位。",
      effect = "emperor_change" },
    { year = 1621, title = "魏忠贤专权",
      desc = "天启帝宠信魏忠贤，阉党势力滔天，残害东林忠良。",
      effect = "political_purge" },
    { year = 1625, title = "辽东失陷",
      desc = "后金连克沈阳、辽阳，辽东大部沦陷，只剩宁远、锦州一线。",
      effect = "border_fall" },
    { year = 1628, title = "崇祯即位",
      desc = "思宗即位，诛魏忠贤，锐意中兴，然积弊已深，回天乏术。加征辽饷，百姓苦不堪言。",
      effect = "tax_up" },
    { year = 1630, title = "流寇壮大",
      desc = "陕西大旱连年，流民揭竿而起，李自成、张献忠等势力日益壮大。",
      effect = "bandit_surge" },
    { year = 1633, title = "后金入关",
      desc = "后金军绕道蒙古入关劫掠，直逼京畿，明军不能制。",
      effect = "military_draft" },
    { year = 1635, title = "流寇会盟荥阳",
      desc = "高迎祥、李自成、张献忠等十三家流寇会盟荥阳，分兵出击。",
      effect = "bandit_surge" },
    { year = 1637, title = "全国大旱",
      desc = "赤地千里，饿殍遍野，粮价飞涨，各地饥民蜂起。",
      effect = "famine" },
    { year = 1639, title = "瘟疫横行",
      desc = "大疫横行华北，死者枕藉，人心惶惶，十室九空。",
      effect = "plague" },
    { year = 1640, title = "河南大饥",
      desc = "河南饥荒至极，人相食之惨状频现，民不聊生。",
      effect = "great_famine" },
    { year = 1641, title = "李自成破洛阳",
      desc = "闯王攻破洛阳，福王朱常洵被杀，天下震动。",
      effect = "war_escalate" },
    { year = 1642, title = "松锦大战",
      desc = "松锦之战明军大败，洪承畴降清，辽东门户洞开。",
      effect = "military_crisis" },
    { year = 1643, title = "张献忠入蜀",
      desc = "张献忠率军入川，湖广大乱，大西政权割据一方。",
      effect = "south_war" },
    { year = 1644, title = "甲申之变",
      desc = "李自成攻入北京，崇祯帝自缢煤山，大明覆亡。",
      effect = "game_end" },
}

-- ============================================================================
-- 年代分期（用于游戏难度曲线和随机事件权重）
-- ============================================================================

--- 获取当前年份所处的时代阶段
--- @param absYear number
--- @return string period 时代分期ID
--- @return string periodName 时代分期名称
--- @return number difficultyMul 难度系数（1.0为基准）
function EraSystem.GetPeriod(absYear)
    if absYear < 1399 then
        return "founding", "开国定鼎", 1.0
    elseif absYear < 1450 then
        return "prosperity", "永宣盛世", 0.8
    elseif absYear < 1465 then
        return "turmoil", "土木之变", 1.2
    elseif absYear < 1522 then
        return "middle", "成弘承平", 0.9
    elseif absYear < 1567 then
        return "jiajing", "嘉靖倭乱", 1.1
    elseif absYear < 1600 then
        return "reform", "隆万改革", 0.85
    elseif absYear < 1620 then
        return "decline", "万历怠政", 1.15
    elseif absYear < 1628 then
        return "late", "天启乱政", 1.3
    else
        return "endgame", "崇祯末世", 1.5
    end
end

--- 获取当前年代的税率修正系数
--- @param absYear number
--- @return number 税率系数（1.0为基准）
function EraSystem.GetTaxModifier(absYear)
    local period = EraSystem.GetPeriod(absYear)
    local modifiers = {
        founding    = 1.0,
        prosperity  = 0.8,
        turmoil     = 1.1,
        middle      = 0.9,
        jiajing     = 1.05,
        reform      = 0.85,
        decline     = 1.1,
        late        = 1.3,
        endgame     = 1.6,
    }
    return modifiers[period] or 1.0
end

--- 获取当前年代的灾害死亡率修正
--- @param absYear number
--- @return number 死亡率系数
function EraSystem.GetDeathRateModifier(absYear)
    local period = EraSystem.GetPeriod(absYear)
    local modifiers = {
        founding    = 1.0,
        prosperity  = 0.7,
        turmoil     = 1.3,
        middle      = 0.8,
        jiajing     = 1.0,
        reform      = 0.8,
        decline     = 1.2,
        late        = 1.5,
        endgame     = 2.0,
    }
    return modifiers[period] or 1.0
end

--- 获取随机事件触发率
--- @param absYear number
--- @return number 触发率(0-1)
function EraSystem.GetEventTriggerRate(absYear)
    local period = EraSystem.GetPeriod(absYear)
    local rates = {
        founding    = 0.25,
        prosperity  = 0.20,
        turmoil     = 0.35,
        middle      = 0.25,
        jiajing     = 0.30,
        reform      = 0.25,
        decline     = 0.35,
        late        = 0.40,
        endgame     = 0.50,
    }
    return rates[period] or 0.30
end

--- 获取经商波动系数（晚期波动更大）
--- @param absYear number
--- @return number 波动系数
function EraSystem.GetTradeVolatility(absYear)
    local period = EraSystem.GetPeriod(absYear)
    local volatilities = {
        founding    = 0.05,
        prosperity  = 0.03,
        turmoil     = 0.10,
        middle      = 0.05,
        jiajing     = 0.08,
        reform      = 0.04,
        decline     = 0.10,
        late        = 0.15,
        endgame     = 0.25,
    }
    return volatilities[period] or 0.05
end

--- 判断某个随机事件在当前年代是否可以触发
--- 用于替代 MonthlyUpdate 中硬编码的年份检查
--- @param absYear number
--- @param eventCategory string 事件分类
--- @return boolean
function EraSystem.CanTriggerEvent(absYear, eventCategory)
    local gameYears = absYear - 1368  -- 游戏已经过的年数

    local requirements = {
        -- 灾害类事件：开局5年后逐步出现
        bandit_raid     = gameYears >= 3,
        drought         = gameYears >= 5,
        locust          = gameYears >= 8,
        plague          = gameYears >= 10,
        famine          = gameYears >= 12,
        -- 军事类事件：需要特定年代
        military_draft  = gameYears >= 8,
        army_passage    = gameYears >= 15,
        rebel_recruit   = gameYears >= 20,
        -- 经济类事件
        trade_crisis    = gameYears >= 5,
        tax_increase    = gameYears >= 5,
        grain_inflation = gameYears >= 10,
        -- 正面事件：早期更容易出现
        good_harvest    = gameYears < 200,
        refugee_influx  = gameYears >= 10,
        -- 战斗事件
        fortress_battle = gameYears >= 15,
    }

    return requirements[eventCategory] ~= false  -- 无条件的默认可触发
end

--- 获取经商风险修正（用于乱世经商）
--- @param absYear number
--- @return number 风险系数
function EraSystem.GetMerchantRiskModifier(absYear)
    local _, _, diffMul = EraSystem.GetPeriod(absYear)
    return diffMul
end

--- 获取丰收事件概率（太平盛世更高）
--- @param absYear number
--- @return boolean 是否有丰收可能
function EraSystem.IsGoodHarvestLikely(absYear)
    local period = EraSystem.GetPeriod(absYear)
    return period == "prosperity" or period == "middle" or period == "reform"
end

--- 获取BGM切换阈值（哪些时期该用危机BGM）
--- @param absYear number
--- @return string bgmType "peaceful" | "crisis"
function EraSystem.GetBGMType(absYear)
    local period = EraSystem.GetPeriod(absYear)
    if period == "endgame" or period == "late" or period == "turmoil" then
        return "crisis"
    end
    return "peaceful"
end

return EraSystem
