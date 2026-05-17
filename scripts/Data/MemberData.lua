-- ============================================================================
-- 大明浮生志2 - 族人数据模块
-- 管理族人相关配置：天赋、身份、状态、年龄、科举、军职、联姻等
-- 从 GameData.lua 中拆分出来
-- ============================================================================

local MemberData = {}

-- 族人身份
MemberData.IDENTITIES = {
    "白丁", "童生", "秀才", "监生", "举人", "进士", "知县", "知府", "布政使",
    "士兵", "把总", "守备", "商人"
}

-- 族人状态
MemberData.STATES = { "在家", "读书", "赶考", "经商", "从军", "出征", "生病", "筑寨", "历练", "打工", "为官" }

-- 天赋列表
MemberData.TALENTS = {
    { id = "smart", name = "天资聪慧", effect = "学识+30%", studyBonus = 0.3 },
    { id = "merchant", name = "经商能手", effect = "经商收益+25%", tradeBonus = 0.25 },
    { id = "martial", name = "武艺高强", effect = "从军存活率+20%", militaryBonus = 0.2 },
    { id = "weak", name = "体弱多病", effect = "生病概率+15%", sickRate = 0.15 },
    { id = "lazy", name = "好吃懒做", effect = "产出-20%", outputPenalty = -0.2 },
    { id = "diligent", name = "勤劳刻苦", effect = "产出+15%", outputBonus = 0.15 },
    { id = "charisma", name = "容貌出众", effect = "联姻加成+20%", marriageBonus = 0.2 },
    { id = "fertile", name = "多子多福", effect = "生育率+25%", fertilityBonus = 0.25 },
}

-- ============================================================================
-- 资质系统：决定每个属性的终身上限（50-100）
-- 星级：1★50-59(30%) 2★60-69(30%) 3★70-79(25%) 4★80-89(12%) 5★90-100(3%)
-- ============================================================================

--- 资质星级定义（概率权重制）
MemberData.APTITUDE_TIERS = {
    { stars = 1, minCap = 50, maxCap = 59, weight = 30, label = "★" },
    { stars = 2, minCap = 60, maxCap = 69, weight = 30, label = "★★" },
    { stars = 3, minCap = 70, maxCap = 79, weight = 25, label = "★★★" },
    { stars = 4, minCap = 80, maxCap = 89, weight = 12, label = "★★★★" },
    { stars = 5, minCap = 90, maxCap = 100, weight = 3,  label = "★★★★★" },
}

--- 按权重随机抽取一个资质上限值
---@return number cap 属性上限(50-100)
---@return number stars 星级(1-5)
function MemberData.RollAptitude()
    local totalWeight = 0
    for _, t in ipairs(MemberData.APTITUDE_TIERS) do
        totalWeight = totalWeight + t.weight
    end
    local roll = math.random() * totalWeight
    local acc = 0
    for _, t in ipairs(MemberData.APTITUDE_TIERS) do
        acc = acc + t.weight
        if roll <= acc then
            return math.random(t.minCap, t.maxCap), t.stars
        end
    end
    -- fallback
    return math.random(50, 59), 1
end

--- 为一个族人生成完整的三维资质
---@return table aptitude { study={cap,stars}, martial={cap,stars}, health={cap,stars} }
function MemberData.GenerateAptitude()
    local sC, sS = MemberData.RollAptitude()
    local mC, mS = MemberData.RollAptitude()
    local hC, hS = MemberData.RollAptitude()
    return {
        study   = { cap = sC, stars = sS },
        martial = { cap = mC, stars = mS },
        health  = { cap = hC, stars = hS },
    }
end

--- 根据cap值返回星级
---@param cap number
---@return number stars
function MemberData.GetStarsFromCap(cap)
    if cap >= 90 then return 5
    elseif cap >= 80 then return 4
    elseif cap >= 70 then return 3
    elseif cap >= 60 then return 2
    else return 1 end
end

--- 获取星级显示文本（五角星）
---@param stars number 1-5
---@return string
function MemberData.GetStarsLabel(stars)
    local labels = { "★", "★★", "★★★", "★★★★", "★★★★★" }
    return labels[stars] or "★"
end

--- 获取星级颜色
---@param stars number 1-5
---@return table rgba
function MemberData.GetStarsColor(stars)
    local colors = {
        { 160, 160, 160, 255 },  -- 1星 灰色
        { 100, 180, 100, 255 },  -- 2星 绿色
        { 70, 130, 220, 255 },   -- 3星 蓝色
        { 180, 100, 220, 255 },  -- 4星 紫色
        { 220, 180, 40, 255 },   -- 5星 金色
    }
    return colors[stars] or colors[1]
end

-- 年龄阶段
MemberData.AGE_STAGES = {
    { name = "幼童", minAge = 0, maxAge = 5 },
    { name = "少年", minAge = 6, maxAge = 14 },
    { name = "青年", minAge = 15, maxAge = 30 },
    { name = "壮年", minAge = 31, maxAge = 50 },
    { name = "老年", minAge = 51, maxAge = 70 },
    { name = "耄耋", minAge = 71, maxAge = 100 },
}

-- 科举等级
MemberData.EXAM_LEVELS = {
    { id = "tongshi", name = "童试", reqStudy = 30, passRate = 0.4, result = "童生", famePlus = 5 },
    { id = "xiangshi", name = "乡试", reqStudy = 60, passRate = 0.25, result = "秀才", famePlus = 15 },
    { id = "huishi", name = "会试", reqStudy = 85, passRate = 0.12, result = "举人", famePlus = 30 },
    { id = "dianshi", name = "殿试", reqStudy = 95, passRate = 0.05, result = "进士", famePlus = 60 },
}

-- 军职等级（阵亡率已下调，避免从军即送死）
MemberData.MILITARY_RANKS = {
    { id = "soldier", name = "士兵", deathRate = 0.03, silverPay = 3, famePlus = 2 },
    { id = "bazong", name = "把总", deathRate = 0.015, silverPay = 8, famePlus = 8 },
    { id = "shoubei", name = "守备", deathRate = 0.008, silverPay = 15, famePlus = 15 },
}

-- 官职等级（殿试进士可入仕为官）
MemberData.OFFICIAL_RANKS = {
    { id = "zhixian",    name = "知县", reqIdentity = "进士",
      silver = 20, fame = 15, taxReduce = 0.10, demotionRate = 0.03,
      desc = "治理一县，月俸20两，声望+15，税赋-10%" },
    { id = "zhifu",      name = "知府", reqIdentity = "知县",
      silver = 40, fame = 25, taxReduce = 0.20, demotionRate = 0.03,
      desc = "统辖一府，月俸40两，声望+25，税赋-20%" },
    { id = "buzhengshi", name = "布政使", reqIdentity = "知府",
      silver = 80, fame = 40, taxReduce = 0.30, demotionRate = 0.03,
      famePerMonth = 5,
      desc = "掌管一省，月俸80两，声望+40，税赋-30%，月产声望+5" },
}

-- 联姻等级
MemberData.MARRIAGE_TIERS = {
    { id = "common", name = "普通人家", silverCost = 80, grainCost = 0, fameReq = 0, famePlus = 2,
      desc = "聘礼少，无特殊加成", bonusType = nil },
    { id = "scholar", name = "书香门第", silverCost = 200, grainCost = 80, fameReq = 15, famePlus = 8,
      desc = "配偶学识+20，子女聪慧概率+15%", bonusType = "study", bonusValue = 20 },
    { id = "official", name = "官宦世家", silverCost = 450, grainCost = 150, fameReq = 30, famePlus = 15,
      desc = "声望大增，官场人脉+1", bonusType = "fame", bonusValue = 10 },
    { id = "military", name = "军户世家", silverCost = 180, grainCost = 100, fameReq = 10, famePlus = 5,
      desc = "配偶武艺+25，从军存活率+10%", bonusType = "martial", bonusValue = 25 },
    { id = "noble", name = "名门望族", silverCost = 800, grainCost = 300, fameReq = 60, famePlus = 25,
      desc = "配偶学识+15武艺+15，声望大增，子女天赋概率+20%", bonusType = "all", bonusValue = 15 },
    { id = "royal_kin", name = "皇亲国戚", silverCost = 1500, grainCost = 500, fameReq = 120, famePlus = 40,
      desc = "皇室姻亲，声望飞涨；配偶全属性+20，税赋减免10%", bonusType = "all", bonusValue = 20 },
    { id = "warlord", name = "藩镇将门", silverCost = 1200, grainCost = 600, fameReq = 80, famePlus = 30,
      desc = "配偶武艺+35，从军存活率+20%，赠送精兵50", bonusType = "martial", bonusValue = 35 },
    { id = "prime_minister", name = "宰辅门第", silverCost = 2500, grainCost = 800, fameReq = 200, famePlus = 60,
      desc = "宰相之家联姻，全族声望月产+5，配偶全属性+25", bonusType = "all", bonusValue = 25 },
}

-- 联姻门第解锁要求
MemberData.MARRIAGE_UNLOCK = {
    common   = 1,  -- 寒门即可
    scholar  = 3,  -- 乡绅解锁
    official = 4,  -- 望族解锁
    military = 5,  -- 世家解锁
    noble    = 6,  -- 勋贵解锁
    royal_kin = 7, -- 名门解锁
    warlord  = 8,  -- 豪阀解锁
    prime_minister = 9, -- 国柱解锁
}

-- 纳捐监生费用
MemberData.DONATION_COST = { silver = 500, famePrice = -15, resultIdentity = "监生" }

-- 马匹费用
MemberData.HORSE_COST = { silver = 400 }

-- 随机名字用字池
-- 男名用字池（80个）
MemberData.MALE_NAMES = {
    -- 德行类
    "德", "仁", "义", "礼", "智", "信", "忠", "孝", "廉", "恕",
    "恒", "谦", "正", "善", "诚", "敬", "慎", "温", "宽", "俭",
    -- 文才类
    "文", "学", "博", "儒", "翰", "墨", "彦", "哲", "思", "明",
    "达", "通", "睿", "渊", "敏", "聪", "颖", "书", "策", "论",
    -- 武勇类
    "武", "勇", "刚", "毅", "烈", "威", "猛", "壮", "豪", "雄",
    "虎", "龙", "鹏", "骏", "飞", "彪", "锐", "铮", "钧", "镇",
    -- 前途类
    "远", "昌", "盛", "兴", "旺", "隆", "泰", "亨", "运", "吉",
    "安", "康", "宁", "和", "平", "顺", "裕", "丰", "茂", "荣",
}

-- 女名用字池（60个）
MemberData.FEMALE_NAMES = {
    -- 花卉草木
    "兰", "梅", "菊", "莲", "荷", "蕙", "芝", "桂", "杏", "蓉",
    "薇", "萱", "芙", "茉", "蔷", "芷", "蕊", "藤", "葵", "棠",
    -- 美德品性
    "秀", "淑", "婉", "雅", "慧", "贞", "静", "柔", "娴", "端",
    "敏", "巧", "素", "洁", "纯", "温", "惠", "懿", "芳", "馨",
    -- 天象自然
    "月", "云", "霞", "雪", "露", "虹", "晴", "春", "秋", "晓",
    -- 珍宝雅韵
    "珍", "玉", "瑶", "琴", "琳", "珊", "瑾", "璇", "翠", "琪",
}

--- 获取年龄阶段名称
function MemberData.GetAgeStage(age)
    for _, stage in ipairs(MemberData.AGE_STAGES) do
        if age >= stage.minAge and age <= stage.maxAge then
            return stage.name
        end
    end
    return "耄耋"
end

--- 随机生成名字
function MemberData.RandomGivenName(gender)
    local pool = gender == "female" and MemberData.FEMALE_NAMES or MemberData.MALE_NAMES
    local n1 = pool[math.random(1, #pool)]
    if math.random() > 0.5 then
        local n2 = pool[math.random(1, #pool)]
        if n2 ~= n1 then return n1 .. n2 end
    end
    return n1
end

-- ============================================================================
-- 装备系统（新增）
-- ============================================================================

-- 装备槽位定义
MemberData.EQUIPMENT_SLOTS = { "weapon", "armor", "accessory" }
MemberData.SLOT_NAMES = { weapon = "武器", armor = "防具", accessory = "饰品" }

-- 装备品质
MemberData.EQUIPMENT_RARITIES = {
    { id = "white",  name = "普通", color = {200, 200, 200, 255}, multiplier = 1.0 },
    { id = "green",  name = "良品", color = {100, 200, 100, 255}, multiplier = 1.3 },
    { id = "blue",   name = "精良", color = {80, 140, 255, 255},  multiplier = 1.6 },
    { id = "purple", name = "珍品", color = {200, 120, 255, 255}, multiplier = 2.0 },
    { id = "gold",   name = "传世", color = {255, 215, 0, 255},   multiplier = 2.5 },
}

-- 装备列表
MemberData.EQUIPMENT_LIST = {
    -- === 武器 ===
    { id = "hoe",        name = "锄头",   slot = "weapon", rarity = "white",
      martial = 3,  study = 0,  health = 0,  cost = 8,   desc = "农具改造，聊胜于无" },
    { id = "knife",      name = "柴刀",   slot = "weapon", rarity = "white",
      martial = 5,  study = 0,  health = 0,  cost = 15,  desc = "劈柴砍樵，亦可防身" },
    { id = "spear",      name = "长枪",   slot = "weapon", rarity = "green",
      martial = 10, study = 0,  health = 0,  cost = 35,  desc = "百兵之王，攻守兼备" },
    { id = "sword",      name = "宝剑",   slot = "weapon", rarity = "blue",
      martial = 15, study = 2,  health = 0,  cost = 60,  desc = "剑气冲霄，文武双全" },
    { id = "guandao",    name = "偃月刀", slot = "weapon", rarity = "purple",
      martial = 22, study = 0,  health = 0,  cost = 100, desc = "关公遗风，威震八方" },
    { id = "fang_tian",  name = "方天画戟", slot = "weapon", rarity = "gold",
      martial = 30, study = 0,  health = 0,  cost = 200, desc = "传世神兵，万夫莫当", heirloom = true },

    -- === 防具 ===
    { id = "cloth_armor", name = "布衣",   slot = "armor", rarity = "white",
      martial = 0,  study = 0,  health = 5,  cost = 10,  desc = "粗布衣裳，略有防护" },
    { id = "leather",    name = "皮甲",   slot = "armor", rarity = "green",
      martial = 2,  study = 0,  health = 10, cost = 30,  desc = "牛皮硝制，轻便灵活" },
    { id = "iron_armor", name = "铁甲",   slot = "armor", rarity = "blue",
      martial = 3,  study = 0,  health = 18, cost = 65,  desc = "铁片锻打，刀枪不入" },
    { id = "scale_armor", name = "鱼鳞甲", slot = "armor", rarity = "purple",
      martial = 5,  study = 0,  health = 25, cost = 120, desc = "鳞片密布，坚不可摧" },
    { id = "golden_armor", name = "锁子连环甲", slot = "armor", rarity = "gold",
      martial = 5,  study = 0,  health = 35, cost = 250, desc = "传世宝甲，刀枪不入", heirloom = true },

    -- === 饰品 ===
    { id = "jade_pendant", name = "玉佩",   slot = "accessory", rarity = "white",
      martial = 0,  study = 3,  health = 2,  cost = 12,  desc = "温润如玉，养心安神" },
    { id = "abacus",     name = "算盘",   slot = "accessory", rarity = "green",
      martial = 0,  study = 5,  health = 0,  cost = 20,  desc = "珠算如飞，经商利器", tradeBonus = 0.1 },
    { id = "art_of_war", name = "兵书",   slot = "accessory", rarity = "blue",
      martial = 8,  study = 5,  health = 0,  cost = 50,  desc = "孙子兵法，知己知彼" },
    { id = "official_seal", name = "官印", slot = "accessory", rarity = "purple",
      martial = 0,  study = 10, health = 0,  cost = 100, desc = "朝廷印信，权势象征", fameBonus = 3 },
    { id = "dragon_seal", name = "蟠龙玉印", slot = "accessory", rarity = "gold",
      martial = 5,  study = 15, health = 5,  cost = 300, desc = "传世至宝，龙气护身", fameBonus = 5, heirloom = true },
}

--- 获取装备配置
function MemberData.GetEquipment(equipId)
    for _, e in ipairs(MemberData.EQUIPMENT_LIST) do
        if e.id == equipId then return e end
    end
    return nil
end

--- 获取装备品质配置
function MemberData.GetRarityConfig(rarityId)
    for _, r in ipairs(MemberData.EQUIPMENT_RARITIES) do
        if r.id == rarityId then return r end
    end
    return MemberData.EQUIPMENT_RARITIES[1]
end

--- 获取某个槽位的所有可用装备
function MemberData.GetEquipmentBySlot(slotId)
    local result = {}
    for _, e in ipairs(MemberData.EQUIPMENT_LIST) do
        if e.slot == slotId then result[#result + 1] = e end
    end
    return result
end

-- ============================================================================
-- 技能专精路径（新增）
-- ============================================================================

MemberData.SKILL_PATHS = {
    { id = "scholar", name = "文士", icon = "文", reqAttr = "study", reqValue = 50,
      desc = "饱读诗书，科举加成+20%，声望月产+2，教导后代学识+30%",
      effects = { examBonus = 0.2, famePerMonth = 2, childStudyBonus = 0.3 } },
    { id = "warrior", name = "武将", icon = "武", reqAttr = "martial", reqValue = 50,
      desc = "武艺超群，从军存活+25%，战力+30%，训练后代武艺+30%",
      effects = { survivalBonus = 0.25, combatBonus = 0.3, childMartialBonus = 0.3 } },
    { id = "merchant", name = "商人", icon = "商", reqAttr = "study", reqValue = 30,
      desc = "精于经商，产业收益+15%，集市交易免税，人脉拓展",
      reqIndustry = true,  -- 需要管理商铺类产业
      effects = { industryBonus = 0.15, taxFree = true, networkBonus = 0.1 } },
}

--- 检查族人是否满足某条技能路径的解锁条件
function MemberData.CanUnlockSkillPath(member, pathId)
    for _, path in ipairs(MemberData.SKILL_PATHS) do
        if path.id == pathId then
            local attrValue = member[path.reqAttr] or 0
            if attrValue < path.reqValue then
                return false, path.reqAttr .. "需达到" .. path.reqValue
            end
            -- 商人路径需额外检查是否管理产业
            if path.reqIndustry and not member.assignment then
                return false, "需管理一个产业"
            end
            return true, nil
        end
    end
    return false, "路径不存在"
end

--- 获取族人当前激活的技能路径
function MemberData.GetActiveSkillPath(member)
    if not member or not member.skillPath then return nil end
    for _, path in ipairs(MemberData.SKILL_PATHS) do
        if path.id == member.skillPath then return path end
    end
    return nil
end

-- ============================================================================
-- 培养系统（新增）
-- ============================================================================

MemberData.TRAINING_OPTIONS = {
    { id = "study_train",  name = "延师教学", icon = "读",
      desc = "聘请名师，学识成长×1.5",
      cost = { silver = 10, grain = 5 },
      attr = "study", multiplier = 1.5 },
    { id = "martial_train", name = "习武练功", icon = "武",
      desc = "苦练武艺，武艺成长×1.5",
      cost = { silver = 8, grain = 8 },
      attr = "martial", multiplier = 1.5 },
    { id = "health_care",  name = "调养身体", icon = "药",
      desc = "请医调养，健康恢复，延寿",
      cost = { silver = 8, grain = 0 },
      attr = "health", multiplier = 2.0 },
    { id = "elite_study",  name = "名师授业", icon = "儒", rank = 7,
      desc = "延请鸿儒大学士，学识成长×2.5",
      cost = { silver = 30, grain = 10 },
      attr = "study", multiplier = 2.5 },
    { id = "elite_martial", name = "名将指点", icon = "将", rank = 7,
      desc = "边关名将亲授，武艺成长×2.5",
      cost = { silver = 25, grain = 15 },
      attr = "martial", multiplier = 2.5 },
    { id = "grand_nurture", name = "全才培养", icon = "全", rank = 9,
      desc = "文武兼修、强身健体，全属性成长×2.0",
      cost = { silver = 50, grain = 20 },
      attr = "all", multiplier = 2.0 },
}

-- ============================================================================
-- 打工系统（按品级解锁工种，月结工资）
-- ============================================================================

-- 打工工种列表（按品级解锁）
-- 工资设计原则：略低于人均月消耗（成人3粮/月 + 季度布匹0.5 ≈ 月开销折银约4~6两）
-- 让玩家能续命但无法躺赢
MemberData.LABOR_JOBS = {
    { id = "coolie",     name = "帮工",   rank = 1, wage = 5,  desc = "码头搬运，卖力气换铜板" },
    { id = "farmhand",   name = "佃农",   rank = 1, wage = 6,  desc = "替人耕种，辛苦度日" },
    { id = "peddler",    name = "货郎",   rank = 2, wage = 8,  desc = "走街串巷，贩卖杂货" },
    { id = "craftsman",  name = "匠人",   rank = 2, wage = 9,  desc = "手艺谋生，略有余钱" },
    { id = "clerk",      name = "账房",   rank = 3, wage = 11, desc = "商号记账，薪俸稳定" },
    { id = "foreman",    name = "工头",   rank = 3, wage = 12, desc = "管人管事，待遇尚可" },
    { id = "steward",    name = "掌柜",   rank = 4, wage = 14, desc = "店铺坐堂，收入丰厚" },
    { id = "tutor",      name = "西席",   rank = 4, wage = 16, desc = "设帐授徒，束脩不菲" },
    { id = "broker",     name = "牙行经纪", rank = 5, wage = 20, desc = "撮合交易，佣金丰厚" },
    { id = "physician",  name = "坐堂郎中", rank = 5, wage = 18, desc = "悬壶济世，诊金可观" },
    { id = "magistrate_aide", name = "师爷", rank = 6, wage = 24, desc = "幕府参谋，俸禄优厚" },
    { id = "caravan_lead", name = "商队领队", rank = 7, wage = 30, desc = "率领商队远行，收入丰厚但辛苦" },
    { id = "mine_overseer", name = "矿监", rank = 8, wage = 37, desc = "监管矿山开采，收入极高" },
    { id = "tax_collector", name = "税使", rank = 9, wage = 47, desc = "代征赋税，权势滔天" },
}

--- 获取当前品级可用的打工工种
function MemberData.GetAvailableLaborJobs(clanRank)
    local result = {}
    for _, job in ipairs(MemberData.LABOR_JOBS) do
        if clanRank >= job.rank then
            result[#result + 1] = job
        end
    end
    return result
end

--- 根据 id 获取打工工种
function MemberData.GetLaborJob(jobId)
    for _, job in ipairs(MemberData.LABOR_JOBS) do
        if job.id == jobId then return job end
    end
    return nil
end

return MemberData
