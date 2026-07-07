-- ============================================================================
-- 大明浮生志2 - 区域战役数据模块（两级地图系统）
-- 12个大区域，每区域4-5个关卡，共58关
-- Stage ID编码：stageId = areaId * 100 + stageNum
-- ============================================================================

local GameData = require("Data.GameData")

local CampaignRegions = {}

-- 征服状态缓存（避免频繁遍历数组）
local conqueredSet_ = nil

local function InvalidateCache()
    conqueredSet_ = nil
end

local function GetConqueredSet()
    if conqueredSet_ then return conqueredSet_ end
    conqueredSet_ = {}
    local s = GameData.state
    if s and s.conqueredStages then
        for _, id in ipairs(s.conqueredStages) do
            conqueredSet_[id] = true
        end
    end
    return conqueredSet_
end

-- ============================================================================
-- 关卡数据（12区域 x 4~5关 = 58关）
-- ============================================================================

CampaignRegions.AREAS = {
    -- ================================================================
    -- 区域1: 乡里除恶（4关）- 村寨匪患
    -- ================================================================
    {
        id = 1, name = "乡里除恶", desc = "清除盘踞家乡的匪患",
        stages = {
            {
                id = 101, areaId = 1, name = "清剿山匪", difficulty = 1,
                soldierRange = { 300, 500 }, memberRange = { 2, 3 },
                martialRange = { 20, 35 }, healthRange = { 50, 65 },
                rewards = { silver = { 8, 15 }, grain = { 5, 12 }, fame = { 2, 5 } },
                desc = "盘踞附近山林的小股匪贼。",
            },
            {
                id = 102, areaId = 1, name = "铲除路霸", difficulty = 2,
                soldierRange = { 400, 650 }, memberRange = { 2, 3 },
                martialRange = { 22, 36 }, healthRange = { 52, 68 },
                rewards = { silver = { 10, 18 }, grain = { 7, 14 }, fame = { 3, 6 } },
                desc = "拦路劫掠的地痞恶霸。",
            },
            {
                id = 103, areaId = 1, name = "夜袭贼窝", difficulty = 3,
                soldierRange = { 500, 800 }, memberRange = { 2, 3 },
                martialRange = { 24, 38 }, healthRange = { 54, 70 },
                rewards = { silver = { 12, 22 }, grain = { 8, 16 }, fame = { 3, 7 } },
                desc = "趁夜突袭贼匪老巢。",
            },
            {
                id = 104, areaId = 1, name = "匪首伏诛", difficulty = 4, isBoss = true,
                soldierRange = { 600, 1000 }, memberRange = { 3, 3 },
                martialRange = { 26, 40 }, healthRange = { 55, 72 },
                rewards = { silver = { 15, 28 }, grain = { 10, 20 }, fame = { 5, 10 } },
                desc = "匪寨头目，手下数百精兵。",
            },
        },
    },

    -- ================================================================
    -- 区域2: 邻村争锋（4关）- 邻村豪强
    -- ================================================================
    {
        id = 2, name = "邻村争锋", desc = "与邻村豪强争夺地盘",
        stages = {
            {
                id = 201, areaId = 2, name = "田产纷争", difficulty = 5,
                soldierRange = { 700, 1200 }, memberRange = { 2, 3 },
                martialRange = { 25, 38 }, healthRange = { 55, 72 },
                rewards = { silver = { 16, 30 }, grain = { 12, 22 }, fame = { 4, 8 } },
                desc = "邻村豪户侵占我族田产。",
            },
            {
                id = 202, areaId = 2, name = "水源之争", difficulty = 6,
                soldierRange = { 800, 1400 }, memberRange = { 2, 3 },
                martialRange = { 27, 40 }, healthRange = { 56, 74 },
                rewards = { silver = { 18, 34 }, grain = { 13, 25 }, fame = { 5, 9 } },
                desc = "争夺村落水源灌溉权。",
            },
            {
                id = 203, areaId = 2, name = "宗祠保卫", difficulty = 7,
                soldierRange = { 1000, 1600 }, memberRange = { 3, 4 },
                martialRange = { 28, 42 }, healthRange = { 58, 76 },
                rewards = { silver = { 20, 38 }, grain = { 15, 28 }, fame = { 5, 10 } },
                desc = "恶邻纠集帮众冲击我族宗祠。",
            },
            {
                id = 204, areaId = 2, name = "恶霸伏法", difficulty = 8, isBoss = true,
                soldierRange = { 1200, 1800 }, memberRange = { 3, 4 },
                martialRange = { 30, 44 }, healthRange = { 60, 78 },
                rewards = { silver = { 22, 42 }, grain = { 16, 32 }, fame = { 6, 12 } },
                desc = "横行乡里的恶霸头目。",
            },
        },
    },

    -- ================================================================
    -- 区域3: 县城角逐（5关）- 县城地头蛇
    -- ================================================================
    {
        id = 3, name = "县城角逐", desc = "进军县城，挑战地方豪强",
        stages = {
            {
                id = 301, areaId = 3, name = "商路护卫", difficulty = 9,
                soldierRange = { 1300, 2000 }, memberRange = { 3, 4 },
                martialRange = { 30, 43 }, healthRange = { 60, 78 },
                rewards = { silver = { 25, 45 }, grain = { 18, 34 }, fame = { 7, 13 } },
                desc = "打通前往县城的商路。",
            },
            {
                id = 302, areaId = 3, name = "县衙冲突", difficulty = 10,
                soldierRange = { 1500, 2300 }, memberRange = { 3, 4 },
                martialRange = { 32, 45 }, healthRange = { 62, 80 },
                rewards = { silver = { 28, 50 }, grain = { 20, 38 }, fame = { 8, 14 } },
                desc = "与县城权贵的正面交锋。",
            },
            {
                id = 303, areaId = 3, name = "粮仓争夺", difficulty = 11,
                soldierRange = { 1700, 2600 }, memberRange = { 3, 4 },
                martialRange = { 33, 46 }, healthRange = { 63, 80 },
                rewards = { silver = { 30, 52 }, grain = { 22, 40 }, fame = { 8, 15 } },
                desc = "争夺县城粮仓控制权。",
            },
            {
                id = 304, areaId = 3, name = "豪强械斗", difficulty = 12,
                soldierRange = { 1900, 2900 }, memberRange = { 3, 4 },
                martialRange = { 35, 48 }, healthRange = { 64, 82 },
                rewards = { silver = { 32, 55 }, grain = { 24, 42 }, fame = { 9, 16 } },
                desc = "县城最大豪强的武装抵抗。",
            },
            {
                id = 305, areaId = 3, name = "县城之主", difficulty = 13, isBoss = true,
                soldierRange = { 2200, 3200 }, memberRange = { 3, 5 },
                martialRange = { 36, 50 }, healthRange = { 65, 84 },
                rewards = { silver = { 38, 62 }, grain = { 26, 48 }, fame = { 10, 18 } },
                desc = "击败县城霸主，称雄一方。",
            },
        },
    },

    -- ================================================================
    -- 区域4: 邻县攻略（5关）- 扩张邻县
    -- ================================================================
    {
        id = 4, name = "邻县攻略", desc = "扩张势力，攻略邻近州县",
        stages = {
            {
                id = 401, areaId = 4, name = "边界摩擦", difficulty = 14,
                soldierRange = { 2500, 3600 }, memberRange = { 3, 4 },
                martialRange = { 37, 50 }, healthRange = { 65, 84 },
                rewards = { silver = { 38, 65 }, grain = { 28, 48 }, fame = { 10, 18 } },
                desc = "两县边界的小规模冲突。",
            },
            {
                id = 402, areaId = 4, name = "驿站伏击", difficulty = 15,
                soldierRange = { 2800, 4000 }, memberRange = { 3, 5 },
                martialRange = { 38, 52 }, healthRange = { 66, 85 },
                rewards = { silver = { 40, 68 }, grain = { 30, 50 }, fame = { 11, 19 } },
                desc = "在驿站设伏，截断敌方补给。",
            },
            {
                id = 403, areaId = 4, name = "矿山争夺", difficulty = 16,
                soldierRange = { 3100, 4400 }, memberRange = { 3, 5 },
                martialRange = { 40, 53 }, healthRange = { 67, 85 },
                rewards = { silver = { 42, 72 }, grain = { 32, 52 }, fame = { 12, 20 } },
                desc = "争夺富铁矿山的控制权。",
            },
            {
                id = 404, areaId = 4, name = "攻占县城", difficulty = 17,
                soldierRange = { 3400, 4800 }, memberRange = { 4, 5 },
                martialRange = { 41, 55 }, healthRange = { 68, 86 },
                rewards = { silver = { 45, 76 }, grain = { 34, 56 }, fame = { 13, 22 } },
                desc = "攻入邻县县城。",
            },
            {
                id = 405, areaId = 4, name = "双县归一", difficulty = 18, isBoss = true,
                soldierRange = { 3800, 5200 }, memberRange = { 4, 5 },
                martialRange = { 43, 56 }, healthRange = { 68, 86 },
                rewards = { silver = { 50, 82 }, grain = { 36, 60 }, fame = { 14, 24 } },
                desc = "一统两县，势力大增。",
            },
        },
    },

    -- ================================================================
    -- 区域5: 府城争霸（5关）- 望族对决
    -- ================================================================
    {
        id = 5, name = "府城争霸", desc = "挺进府城，与望族一决高下",
        stages = {
            {
                id = 501, areaId = 5, name = "府城前哨", difficulty = 19,
                soldierRange = { 4000, 5600 }, memberRange = { 3, 5 },
                martialRange = { 42, 55 }, healthRange = { 68, 86 },
                rewards = { silver = { 48, 82 }, grain = { 36, 60 }, fame = { 12, 22 } },
                desc = "府城外围的前哨战。",
            },
            {
                id = 502, areaId = 5, name = "望族对决", difficulty = 20,
                soldierRange = { 4400, 6200 }, memberRange = { 4, 5 },
                martialRange = { 44, 57 }, healthRange = { 70, 87 },
                rewards = { silver = { 52, 88 }, grain = { 40, 65 }, fame = { 13, 24 } },
                desc = "与府城望族正面交锋。",
            },
            {
                id = 503, areaId = 5, name = "盐道争锋", difficulty = 21,
                soldierRange = { 4800, 6800 }, memberRange = { 4, 5 },
                martialRange = { 45, 58 }, healthRange = { 70, 88 },
                rewards = { silver = { 55, 92 }, grain = { 42, 68 }, fame = { 14, 25 } },
                desc = "争夺利润丰厚的盐道。",
            },
            {
                id = 504, areaId = 5, name = "府衙攻防", difficulty = 22,
                soldierRange = { 5200, 7400 }, memberRange = { 4, 5 },
                martialRange = { 47, 60 }, healthRange = { 72, 88 },
                rewards = { silver = { 58, 98 }, grain = { 44, 72 }, fame = { 15, 26 } },
                desc = "强攻府衙，夺取权柄。",
            },
            {
                id = 505, areaId = 5, name = "一府之霸", difficulty = 23, isBoss = true,
                soldierRange = { 5800, 8000 }, memberRange = { 4, 6 },
                martialRange = { 48, 62 }, healthRange = { 72, 90 },
                rewards = { silver = { 65, 108 }, grain = { 48, 78 }, fame = { 16, 28 } },
                desc = "击败府城霸主，称雄一府。",
            },
        },
    },

    -- ================================================================
    -- 区域6: 省城攻略（5关）- 省城豪门
    -- ================================================================
    {
        id = 6, name = "省城攻略", desc = "进军省城，攻克根深蒂固的豪门",
        stages = {
            {
                id = 601, areaId = 6, name = "官道封锁", difficulty = 24,
                soldierRange = { 6000, 8500 }, memberRange = { 4, 5 },
                martialRange = { 48, 60 }, healthRange = { 72, 88 },
                rewards = { silver = { 60, 100 }, grain = { 46, 75 }, fame = { 15, 26 } },
                desc = "封锁通往省城的官道。",
            },
            {
                id = 602, areaId = 6, name = "税关冲突", difficulty = 25,
                soldierRange = { 6500, 9200 }, memberRange = { 4, 5 },
                martialRange = { 50, 62 }, healthRange = { 73, 89 },
                rewards = { silver = { 64, 108 }, grain = { 48, 80 }, fame = { 16, 28 } },
                desc = "与省城税关守军冲突。",
            },
            {
                id = 603, areaId = 6, name = "书院之争", difficulty = 26,
                soldierRange = { 7000, 10000 }, memberRange = { 4, 6 },
                martialRange = { 52, 64 }, healthRange = { 74, 90 },
                rewards = { silver = { 68, 115 }, grain = { 52, 85 }, fame = { 18, 30 } },
                desc = "争夺书院学府的控制权。",
            },
            {
                id = 604, areaId = 6, name = "城防突破", difficulty = 27,
                soldierRange = { 7500, 10800 }, memberRange = { 4, 6 },
                martialRange = { 53, 65 }, healthRange = { 75, 90 },
                rewards = { silver = { 72, 120 }, grain = { 54, 88 }, fame = { 19, 32 } },
                desc = "突破省城城防工事。",
            },
            {
                id = 605, areaId = 6, name = "省城易主", difficulty = 28, isBoss = true,
                soldierRange = { 8200, 11500 }, memberRange = { 5, 6 },
                martialRange = { 55, 68 }, healthRange = { 76, 92 },
                rewards = { silver = { 80, 135 }, grain = { 58, 95 }, fame = { 20, 35 } },
                desc = "攻克省城，豪门覆灭。",
            },
        },
    },

    -- ================================================================
    -- 区域7: 中原逐鹿（5关）- 群雄割据
    -- ================================================================
    {
        id = 7, name = "中原逐鹿", desc = "逐鹿中原，群雄割据",
        stages = {
            {
                id = 701, areaId = 7, name = "群雄割据", difficulty = 29,
                soldierRange = { 8500, 12000 }, memberRange = { 4, 6 },
                martialRange = { 55, 66 }, healthRange = { 75, 90 },
                rewards = { silver = { 78, 130 }, grain = { 58, 95 }, fame = { 20, 34 } },
                desc = "中原腹地，各路豪强林立。",
            },
            {
                id = 702, areaId = 7, name = "平原会战", difficulty = 30,
                soldierRange = { 9200, 13000 }, memberRange = { 4, 6 },
                martialRange = { 56, 68 }, healthRange = { 76, 92 },
                rewards = { silver = { 82, 140 }, grain = { 62, 100 }, fame = { 22, 36 } },
                desc = "广袤平原上的大规模会战。",
            },
            {
                id = 703, areaId = 7, name = "河防争夺", difficulty = 31,
                soldierRange = { 10000, 14000 }, memberRange = { 5, 6 },
                martialRange = { 58, 70 }, healthRange = { 78, 92 },
                rewards = { silver = { 88, 148 }, grain = { 65, 105 }, fame = { 23, 38 } },
                desc = "争夺黄河渡口控制权。",
            },
            {
                id = 704, areaId = 7, name = "中原要塞", difficulty = 32,
                soldierRange = { 10800, 15000 }, memberRange = { 5, 6 },
                martialRange = { 60, 72 }, healthRange = { 78, 94 },
                rewards = { silver = { 92, 155 }, grain = { 68, 110 }, fame = { 24, 40 } },
                desc = "攻克中原战略要塞。",
            },
            {
                id = 705, areaId = 7, name = "逐鹿中原", difficulty = 33, isBoss = true,
                soldierRange = { 12000, 16500 }, memberRange = { 5, 6 },
                martialRange = { 62, 74 }, healthRange = { 80, 94 },
                rewards = { silver = { 100, 168 }, grain = { 72, 118 }, fame = { 26, 42 } },
                desc = "击败中原最强势力，问鼎中原。",
            },
        },
    },

    -- ================================================================
    -- 区域8: 江南征伐（5关）- 鱼米之乡
    -- ================================================================
    {
        id = 8, name = "江南征伐", desc = "鱼米之乡，富甲天下",
        stages = {
            {
                id = 801, areaId = 8, name = "渡江先锋", difficulty = 34,
                soldierRange = { 12000, 16000 }, memberRange = { 4, 6 },
                martialRange = { 60, 72 }, healthRange = { 78, 92 },
                rewards = { silver = { 95, 160 }, grain = { 70, 112 }, fame = { 24, 40 } },
                desc = "率先渡江，建立滩头阵地。",
            },
            {
                id = 802, areaId = 8, name = "水乡巷战", difficulty = 35,
                soldierRange = { 13000, 17500 }, memberRange = { 5, 6 },
                martialRange = { 62, 74 }, healthRange = { 80, 93 },
                rewards = { silver = { 100, 170 }, grain = { 74, 118 }, fame = { 26, 42 } },
                desc = "江南水乡中的激烈巷战。",
            },
            {
                id = 803, areaId = 8, name = "运河截断", difficulty = 36,
                soldierRange = { 14000, 18500 }, memberRange = { 5, 6 },
                martialRange = { 64, 75 }, healthRange = { 80, 94 },
                rewards = { silver = { 108, 180 }, grain = { 78, 125 }, fame = { 28, 44 } },
                desc = "截断运河，切断敌方命脉。",
            },
            {
                id = 804, areaId = 8, name = "富商私兵", difficulty = 37,
                soldierRange = { 15000, 20000 }, memberRange = { 5, 6 },
                martialRange = { 65, 77 }, healthRange = { 82, 94 },
                rewards = { silver = { 115, 190 }, grain = { 82, 132 }, fame = { 30, 46 } },
                desc = "江南巨商的精锐私兵。",
            },
            {
                id = 805, areaId = 8, name = "江南平定", difficulty = 38, isBoss = true,
                soldierRange = { 16500, 22000 }, memberRange = { 5, 6 },
                martialRange = { 68, 80 }, healthRange = { 82, 96 },
                rewards = { silver = { 125, 205 }, grain = { 88, 142 }, fame = { 32, 50 } },
                desc = "平定江南，鱼米尽归。",
            },
        },
    },

    -- ================================================================
    -- 区域9: 巴蜀远征（5关）- 蜀道天险
    -- ================================================================
    {
        id = 9, name = "巴蜀远征", desc = "蜀道之难难于上青天",
        stages = {
            {
                id = 901, areaId = 9, name = "蜀道关隘", difficulty = 39,
                soldierRange = { 16000, 21000 }, memberRange = { 5, 6 },
                martialRange = { 66, 78 }, healthRange = { 80, 94 },
                rewards = { silver = { 118, 195 }, grain = { 85, 138 }, fame = { 30, 48 } },
                desc = "蜀道天险，一夫当关。",
            },
            {
                id = 902, areaId = 9, name = "栈道伏击", difficulty = 40,
                soldierRange = { 17500, 23000 }, memberRange = { 5, 6 },
                martialRange = { 68, 80 }, healthRange = { 82, 94 },
                rewards = { silver = { 125, 208 }, grain = { 90, 145 }, fame = { 32, 50 } },
                desc = "栈道之上的伏击战。",
            },
            {
                id = 903, areaId = 9, name = "土司叛军", difficulty = 41,
                soldierRange = { 19000, 25000 }, memberRange = { 5, 6 },
                martialRange = { 70, 82 }, healthRange = { 82, 95 },
                rewards = { silver = { 132, 220 }, grain = { 95, 152 }, fame = { 34, 52 } },
                desc = "川中土司纠集叛军。",
            },
            {
                id = 904, areaId = 9, name = "成都围城", difficulty = 42,
                soldierRange = { 20000, 26500 }, memberRange = { 5, 6 },
                martialRange = { 72, 84 }, healthRange = { 84, 96 },
                rewards = { silver = { 140, 232 }, grain = { 100, 160 }, fame = { 36, 55 } },
                desc = "围攻成都，攻城拔寨。",
            },
            {
                id = 905, areaId = 9, name = "天府归顺", difficulty = 43, isBoss = true,
                soldierRange = { 22000, 28000 }, memberRange = { 6, 6 },
                martialRange = { 74, 86 }, healthRange = { 84, 96 },
                rewards = { silver = { 150, 248 }, grain = { 108, 170 }, fame = { 38, 58 } },
                desc = "天府之国尽归麾下。",
            },
        },
    },

    -- ================================================================
    -- 区域10: 两广统一（5关）- 岭南平定
    -- ================================================================
    {
        id = 10, name = "两广统一", desc = "岭南蛮荒，民风彪悍",
        stages = {
            {
                id = 1001, areaId = 10, name = "岭南蛮族", difficulty = 44,
                soldierRange = { 21000, 27000 }, memberRange = { 5, 6 },
                martialRange = { 72, 84 }, healthRange = { 82, 95 },
                rewards = { silver = { 142, 235 }, grain = { 100, 162 }, fame = { 35, 55 } },
                desc = "岭南山地的蛮族武装。",
            },
            {
                id = 1002, areaId = 10, name = "海盗巢穴", difficulty = 45,
                soldierRange = { 23000, 29000 }, memberRange = { 5, 6 },
                martialRange = { 74, 86 }, healthRange = { 84, 96 },
                rewards = { silver = { 150, 250 }, grain = { 106, 172 }, fame = { 37, 58 } },
                desc = "盘踞沿海的海盗势力。",
            },
            {
                id = 1003, areaId = 10, name = "土司联军", difficulty = 46,
                soldierRange = { 25000, 31000 }, memberRange = { 5, 6 },
                martialRange = { 76, 88 }, healthRange = { 84, 96 },
                rewards = { silver = { 160, 265 }, grain = { 112, 180 }, fame = { 40, 60 } },
                desc = "两广土司联合抵抗。",
            },
            {
                id = 1004, areaId = 10, name = "广州攻防", difficulty = 47,
                soldierRange = { 26000, 32000 }, memberRange = { 6, 6 },
                martialRange = { 78, 90 }, healthRange = { 86, 97 },
                rewards = { silver = { 168, 278 }, grain = { 118, 188 }, fame = { 42, 62 } },
                desc = "攻克广州城防。",
            },
            {
                id = 1005, areaId = 10, name = "两广归一", difficulty = 48, isBoss = true,
                soldierRange = { 28000, 34000 }, memberRange = { 6, 6 },
                martialRange = { 80, 92 }, healthRange = { 86, 98 },
                rewards = { silver = { 180, 295 }, grain = { 125, 198 }, fame = { 45, 65 } },
                desc = "统一两广，威震南疆。",
            },
        },
    },

    -- ================================================================
    -- 区域11: 塞北征讨（5关）- 塞外铁骑
    -- ================================================================
    {
        id = 11, name = "塞北征讨", desc = "塞外铁骑纵横，苦寒劲敌",
        stages = {
            {
                id = 1101, areaId = 11, name = "长城关隘", difficulty = 49,
                soldierRange = { 27000, 33000 }, memberRange = { 5, 6 },
                martialRange = { 78, 90 }, healthRange = { 84, 96 },
                rewards = { silver = { 170, 280 }, grain = { 120, 190 }, fame = { 40, 62 } },
                desc = "夺取长城关隘控制权。",
            },
            {
                id = 1102, areaId = 11, name = "草原铁骑", difficulty = 50,
                soldierRange = { 28000, 34000 }, memberRange = { 5, 6 },
                martialRange = { 80, 92 }, healthRange = { 86, 97 },
                rewards = { silver = { 180, 298 }, grain = { 126, 200 }, fame = { 42, 65 } },
                desc = "与草原铁骑正面交锋。",
            },
            {
                id = 1103, areaId = 11, name = "沙漠行军", difficulty = 51,
                soldierRange = { 30000, 36000 }, memberRange = { 6, 6 },
                martialRange = { 82, 94 }, healthRange = { 86, 98 },
                rewards = { silver = { 190, 315 }, grain = { 132, 210 }, fame = { 45, 68 } },
                desc = "穿越荒漠的远征军。",
            },
            {
                id = 1104, areaId = 11, name = "王庭突袭", difficulty = 52,
                soldierRange = { 32000, 38000 }, memberRange = { 6, 6 },
                martialRange = { 84, 95 }, healthRange = { 88, 98 },
                rewards = { silver = { 200, 330 }, grain = { 138, 220 }, fame = { 48, 72 } },
                desc = "突袭游牧王庭。",
            },
            {
                id = 1105, areaId = 11, name = "塞北臣服", difficulty = 53, isBoss = true,
                soldierRange = { 34000, 40000 }, memberRange = { 6, 6 },
                martialRange = { 86, 96 }, healthRange = { 88, 100 },
                rewards = { silver = { 215, 350 }, grain = { 145, 232 }, fame = { 50, 75 } },
                desc = "塞北诸族俯首称臣。",
            },
        },
    },

    -- ================================================================
    -- 区域12: 问鼎天下（5关）- 一统天下
    -- ================================================================
    {
        id = 12, name = "问鼎天下", desc = "天下大势，分久必合",
        stages = {
            {
                id = 1201, areaId = 12, name = "诸侯会盟", difficulty = 54,
                soldierRange = { 32000, 38000 }, memberRange = { 6, 6 },
                martialRange = { 84, 95 }, healthRange = { 86, 98 },
                rewards = { silver = { 200, 330 }, grain = { 140, 225 }, fame = { 48, 72 } },
                desc = "天下诸侯最后的联盟。",
            },
            {
                id = 1202, areaId = 12, name = "勤王之师", difficulty = 55,
                soldierRange = { 34000, 40000 }, memberRange = { 6, 6 },
                martialRange = { 86, 96 }, healthRange = { 88, 98 },
                rewards = { silver = { 215, 355 }, grain = { 148, 238 }, fame = { 50, 76 } },
                desc = "号称勤王的最强大军。",
            },
            {
                id = 1203, areaId = 12, name = "群雄争霸", difficulty = 56,
                soldierRange = { 36000, 42000 }, memberRange = { 6, 6 },
                martialRange = { 88, 97 }, healthRange = { 88, 100 },
                rewards = { silver = { 230, 375 }, grain = { 155, 250 }, fame = { 55, 80 } },
                desc = "天下群雄的最终决战。",
            },
            {
                id = 1204, areaId = 12, name = "京师之战", difficulty = 57,
                soldierRange = { 38000, 44000 }, memberRange = { 6, 6 },
                martialRange = { 90, 98 }, healthRange = { 90, 100 },
                rewards = { silver = { 250, 400 }, grain = { 165, 268 }, fame = { 60, 85 } },
                desc = "攻入京师，改朝换代。",
            },
            {
                id = 1205, areaId = 12, name = "天命所归", difficulty = 58, isBoss = true,
                soldierRange = { 40000, 48000 }, memberRange = { 6, 6 },
                martialRange = { 92, 100 }, healthRange = { 92, 100 },
                rewards = { silver = { 300, 500 }, grain = { 180, 300 }, fame = { 70, 100 } },
                desc = "天命所归，一统天下！",
            },
        },
    },
}

-- ============================================================================
-- 向后兼容：保留旧 REGIONS 引用（指向 AREAS）
-- ============================================================================
CampaignRegions.REGIONS = CampaignRegions.AREAS

-- ============================================================================
-- 区域级查询
-- ============================================================================

--- 获取所有区域
function CampaignRegions.GetAllAreas()
    return CampaignRegions.AREAS
end

--- 获取所有区域（兼容旧API）
function CampaignRegions.GetAll()
    return CampaignRegions.AREAS
end

--- 获取指定区域
---@param areaId number (1-12)
---@return table|nil
function CampaignRegions.GetArea(areaId)
    for _, area in ipairs(CampaignRegions.AREAS) do
        if area.id == areaId then return area end
    end
    return nil
end

--- 兼容旧API：GetById映射到GetArea
---@param id number
---@return table|nil
function CampaignRegions.GetById(id)
    return CampaignRegions.GetArea(id)
end

--- 区域是否已解锁（第一个关卡已解锁）
---@param areaId number
---@return boolean
function CampaignRegions.IsAreaUnlocked(areaId)
    if areaId == 1 then return true end
    -- 需要上一区域的Boss已征服
    local prevArea = CampaignRegions.GetArea(areaId - 1)
    if not prevArea or not prevArea.stages then return false end
    local bossId = prevArea.stages[#prevArea.stages].id
    return CampaignRegions.IsStageConquered(bossId)
end

--- 兼容旧API
function CampaignRegions.IsUnlocked(areaId)
    return CampaignRegions.IsAreaUnlocked(areaId)
end

--- 区域是否已全部征服
---@param areaId number
---@return boolean
function CampaignRegions.IsAreaConquered(areaId)
    local area = CampaignRegions.GetArea(areaId)
    if not area or not area.stages then return false end
    for _, stage in ipairs(area.stages) do
        if not CampaignRegions.IsStageConquered(stage.id) then
            return false
        end
    end
    return true
end

--- 兼容旧API
function CampaignRegions.IsConquered(areaId)
    return CampaignRegions.IsAreaConquered(areaId)
end

--- 获取区域进度
---@param areaId number
---@return number conquered, number total
function CampaignRegions.GetAreaProgress(areaId)
    local area = CampaignRegions.GetArea(areaId)
    if not area or not area.stages then return 0, 0 end
    local conquered = 0
    for _, stage in ipairs(area.stages) do
        if CampaignRegions.IsStageConquered(stage.id) then
            conquered = conquered + 1
        end
    end
    return conquered, #area.stages
end

-- ============================================================================
-- 关卡级查询
-- ============================================================================

--- 获取指定关卡
---@param stageId number (如 305)
---@return table|nil
function CampaignRegions.GetStage(stageId)
    local areaId = math.floor(stageId / 100)
    local area = CampaignRegions.GetArea(areaId)
    if not area or not area.stages then return nil end
    for _, stage in ipairs(area.stages) do
        if stage.id == stageId then return stage end
    end
    return nil
end

--- 关卡是否已征服
---@param stageId number
---@return boolean
function CampaignRegions.IsStageConquered(stageId)
    return GetConqueredSet()[stageId] == true
end

--- 关卡是否已解锁
---@param stageId number
---@return boolean
function CampaignRegions.IsStageUnlocked(stageId)
    if stageId == 101 then return true end
    local areaId = math.floor(stageId / 100)
    local stageNum = stageId % 100
    if stageNum == 1 then
        -- 区域首关：需上一区域Boss已征服
        local prevArea = CampaignRegions.GetArea(areaId - 1)
        if not prevArea or not prevArea.stages then return false end
        local bossId = prevArea.stages[#prevArea.stages].id
        return CampaignRegions.IsStageConquered(bossId)
    else
        -- 区域内：需上一关已征服
        return CampaignRegions.IsStageConquered(areaId * 100 + stageNum - 1)
    end
end

--- 标记关卡为已征服
---@param stageId number
function CampaignRegions.MarkStageConquered(stageId)
    local s = GameData.state
    if not s then return end
    if not s.conqueredStages then s.conqueredStages = {} end
    -- 避免重复
    for _, id in ipairs(s.conqueredStages) do
        if id == stageId then return end
    end
    s.conqueredStages[#s.conqueredStages + 1] = stageId
    InvalidateCache()
end

--- 兼容旧API：MarkConquered映射到标记区域所有关卡
function CampaignRegions.MarkConquered(regionIdOrStageId)
    if regionIdOrStageId <= 12 then
        -- 旧API：标记整个区域
        local area = CampaignRegions.GetArea(regionIdOrStageId)
        if area and area.stages then
            for _, stage in ipairs(area.stages) do
                CampaignRegions.MarkStageConquered(stage.id)
            end
        end
    else
        -- 新API：直接标记关卡
        CampaignRegions.MarkStageConquered(regionIdOrStageId)
    end
end

--- 获取关卡总数
---@return number
function CampaignRegions.GetTotalStageCount()
    local count = 0
    for _, area in ipairs(CampaignRegions.AREAS) do
        count = count + #area.stages
    end
    return count
end

--- 获取已征服关卡总数
---@return number
function CampaignRegions.GetConqueredStageCount()
    local s = GameData.state
    if not s or not s.conqueredStages then return 0 end
    return #s.conqueredStages
end

-- ============================================================================
-- 敌族姓氏（用于区域战役）
-- ============================================================================

local REGION_SURNAMES = {
    "马", "孙", "韩", "徐", "曹", "魏", "吕", "沈", "冯", "郑",
    "蒋", "唐", "贺", "邓", "萧", "田", "程", "袁", "于", "董",
}

local GIVEN_NAMES = {
    "虎", "豹", "龙", "猛", "刚", "坚", "毅", "烈", "威", "勇",
    "强", "铁", "钢", "峰", "岩", "雷", "霆", "鸿", "熊", "飞",
    "锋", "剑", "骁", "悍", "豪", "壮", "魁", "洪", "凯", "震",
}

local LEADER_TITLES = { "大将军", "都督", "总兵", "统帅", "首领", "寨主", "城主" }

-- 区域对应的难度标签名和颜色
local AREA_TIER_NAMES = {
    "匪寨", "恶霸", "豪强", "世家", "望族", "豪门",
    "群雄", "霸主", "枭雄", "雄主", "铁骑", "天下",
}
local AREA_TIER_COLORS = {
    { 100, 200, 100, 255 },  -- 1 绿
    { 100, 200, 100, 255 },  -- 2 绿
    { 220, 180, 50, 255 },   -- 3 黄
    { 220, 180, 50, 255 },   -- 4 黄
    { 220, 120, 50, 255 },   -- 5 橙
    { 220, 120, 50, 255 },   -- 6 橙
    { 220, 80, 60, 255 },    -- 7 红
    { 220, 80, 60, 255 },    -- 8 红
    { 180, 80, 220, 255 },   -- 9 紫
    { 180, 80, 220, 255 },   -- 10 紫
    { 180, 50, 50, 255 },    -- 11 暗红
    { 200, 180, 50, 255 },   -- 12 金
}

--- 生成关卡的敌族数据（兼容 BattleScene.Enter 所需的 rival 格式）
---@param stageId number
---@return table|nil rival
function CampaignRegions.GenerateStageEnemy(stageId)
    local stage = CampaignRegions.GetStage(stageId)
    if not stage then return nil end

    local playerSurname = GameData.state and GameData.state.surname or ""

    -- 随机姓氏（不与玩家重复）
    local surname
    repeat
        surname = REGION_SURNAMES[math.random(1, #REGION_SURNAMES)]
    until surname ~= playerSurname

    -- 成员数
    local memberCount = math.random(stage.memberRange[1], stage.memberRange[2])
    local members = {}
    local usedNames = {}

    for j = 1, memberCount do
        local isLeader = (j == 1)
        local givenName
        repeat
            givenName = GIVEN_NAMES[math.random(1, #GIVEN_NAMES)]
            if math.random() < 0.4 then
                local n2 = GIVEN_NAMES[math.random(1, #GIVEN_NAMES)]
                if n2 ~= givenName then givenName = givenName .. n2 end
            end
        until not usedNames[givenName]
        usedNames[givenName] = true

        local name = surname .. givenName
        local martial = math.random(stage.martialRange[1], stage.martialRange[2])
        local health = math.random(stage.healthRange[1], stage.healthRange[2])

        if isLeader then
            martial = math.min(100, martial + 15)
            health = math.min(100, health + 10)
        end

        members[j] = {
            name = name,
            gender = "male",
            age = isLeader and math.random(35, 55) or math.random(18, 50),
            title = isLeader and LEADER_TITLES[math.random(1, #LEADER_TITLES)] or nil,
            martial = martial,
            health = health,
            isLeader = isLeader,
            memberIndex = j,
        }
    end

    -- 总兵力
    local soldiers = math.random(stage.soldierRange[1], stage.soldierRange[2])
    soldiers = math.floor(soldiers / 100) * 100
    if soldiers < 100 then soldiers = 100 end

    -- 奖励
    local rewards = {
        silver = math.random(stage.rewards.silver[1], stage.rewards.silver[2]),
        grain = math.random(stage.rewards.grain[1], stage.rewards.grain[2]),
        fame = math.random(stage.rewards.fame[1], stage.rewards.fame[2]),
    }

    local areaId = stage.areaId
    local tierName = AREA_TIER_NAMES[areaId] or "未知"
    if stage.isBoss then tierName = tierName .. "+" end

    local clanName = surname .. "军"

    return {
        id = stageId,
        name = clanName,
        surname = surname,
        tierId = "stage_" .. stageId,
        tierName = tierName,
        tierColor = AREA_TIER_COLORS[areaId] or { 200, 200, 200, 255 },
        tierDesc = stage.desc,
        members = members,
        soldiers = soldiers,
        rewards = rewards,
        stageId = stageId,
        regionId = areaId,  -- 兼容旧字段
    }
end

--- 兼容旧API：GenerateEnemy映射到区域Boss关
---@param regionId number
---@return table|nil
function CampaignRegions.GenerateEnemy(regionId)
    local area = CampaignRegions.GetArea(regionId)
    if not area or not area.stages then return nil end
    -- 找到该区域最后未征服的关卡，或Boss关
    local bossStage = area.stages[#area.stages]
    return CampaignRegions.GenerateStageEnemy(bossStage.id)
end

return CampaignRegions
