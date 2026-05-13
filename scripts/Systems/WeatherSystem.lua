-- ============================================================================
-- 大明浮生志2 - 天气与季节系统
-- 根据月份生成季节和天气，影响产出、健康、军事
-- ============================================================================

local GameData = require("Data.GameData")
local Theme = require("UI.Theme")

local WeatherSystem = {}

-- ============================================================================
-- 季节定义
-- ============================================================================

WeatherSystem.SEASONS = {
    { id = "spring", name = "春", icon = "春", img = Theme.IMG.SEASON_SPRING, months = { 1, 2, 3 } },
    { id = "summer", name = "夏", icon = "夏", img = Theme.IMG.SEASON_SUMMER, months = { 4, 5, 6 } },
    { id = "autumn", name = "秋", icon = "秋", img = Theme.IMG.SEASON_AUTUMN, months = { 7, 8, 9 } },
    { id = "winter", name = "冬", icon = "冬", img = Theme.IMG.SEASON_WINTER, months = { 10, 11, 12 } },
}

-- ============================================================================
-- 天气类型及其概率权重（按季节分布）
-- ============================================================================

-- 天气定义：name 显示名, icon 图标, effects 影响系数
WeatherSystem.WEATHER_TYPES = {
    clear    = { name = "晴朗", icon = "晴", img = Theme.IMG.WEATHER_CLEAR,    grainMul = 1.0,  healthMod = 0,  moraleMod = 0,  desc = "天朗气清，万物和畅。" },
    cloudy   = { name = "多云", icon = "云", img = Theme.IMG.WEATHER_CLOUDY,   grainMul = 0.95, healthMod = 0,  moraleMod = 0,  desc = "云层叠嶂，不碍农事。" },
    rain     = { name = "细雨", icon = "雨", img = Theme.IMG.WEATHER_RAIN,     grainMul = 1.1,  healthMod = -1, moraleMod = 0,  desc = "春雨润物，利于耕种。" },
    storm    = { name = "暴雨", icon = "暴", img = Theme.IMG.WEATHER_STORM,    grainMul = 0.7,  healthMod = -3, moraleMod = -1, desc = "暴雨倾盆，田地受淹，道路泥泞。" },
    drought  = { name = "干旱", icon = "旱", img = Theme.IMG.WEATHER_DROUGHT,  grainMul = 0.5,  healthMod = -2, moraleMod = -2, desc = "烈日炎炎，禾苗枯焦，旱魃为虐。" },
    flood    = { name = "洪涝", icon = "涝", img = Theme.IMG.WEATHER_FLOOD,    grainMul = 0.4,  healthMod = -5, moraleMod = -3, desc = "洪水漫堤，田宅尽没，流民四起。" },
    snow     = { name = "大雪", icon = "雪", img = Theme.IMG.WEATHER_SNOW,     grainMul = 0.3,  healthMod = -4, moraleMod = -1, desc = "大雪封路，严寒刺骨，柴米告急。" },
    frost    = { name = "霜冻", icon = "霜", img = Theme.IMG.WEATHER_FROST,    grainMul = 0.6,  healthMod = -3, moraleMod = -1, desc = "霜降过早，秋粮减收。" },
    fog      = { name = "大雾", icon = "雾", img = Theme.IMG.WEATHER_FOG,      grainMul = 0.9,  healthMod = -1, moraleMod = 0,  desc = "浓雾弥漫，出行不便。" },
    wind     = { name = "大风", icon = "风", img = Theme.IMG.WEATHER_WIND,     grainMul = 0.85, healthMod = -1, moraleMod = 0,  desc = "狂风大作，屋瓦飞扬。" },
    pleasant = { name = "和煦", icon = "暖", img = Theme.IMG.WEATHER_PLEASANT, grainMul = 1.15, healthMod = 2,  moraleMod = 1,  desc = "风和日丽，适宜耕读。" },
}

-- 各季节天气权重分布
local SEASON_WEATHER_WEIGHTS = {
    spring = { clear = 25, cloudy = 20, rain = 30, storm = 5, fog = 10, pleasant = 10 },
    summer = { clear = 20, cloudy = 10, rain = 15, storm = 15, drought = 15, flood = 10, wind = 5, pleasant = 10 },
    autumn = { clear = 30, cloudy = 15, rain = 10, frost = 10, wind = 10, fog = 5, pleasant = 20 },
    winter = { clear = 15, cloudy = 20, snow = 25, frost = 15, fog = 10, wind = 10, cold = 5 },
}

-- cold 是 winter 专属，补充定义
WeatherSystem.WEATHER_TYPES.cold = { name = "严寒", icon = "寒", img = Theme.IMG.WEATHER_COLD, grainMul = 0.4, healthMod = -5, moraleMod = -2, desc = "天寒地冻，冻死牛马，人心惶惶。" }

-- ============================================================================
-- 核心方法
-- ============================================================================

--- 获取当前季节
--- @param month number 月份(1-12)
--- @return table 季节信息
function WeatherSystem.GetSeason(month)
    for _, season in ipairs(WeatherSystem.SEASONS) do
        for _, m in ipairs(season.months) do
            if m == month then return season end
        end
    end
    return WeatherSystem.SEASONS[1]
end

--- 加权随机选取天气
--- @param season table 季节信息
--- @param year number 年份（晚明灾害加重）
--- @return string 天气类型 id
local function weightedRandomWeather(season, year)
    local weights = SEASON_WEATHER_WEIGHTS[season.id]
    if not weights then weights = SEASON_WEATHER_WEIGHTS.spring end

    -- 晚明（1600年后）灾害权重增加
    local adjusted = {}
    local disasterTypes = { storm = true, drought = true, flood = true, snow = true, frost = true, cold = true }
    for wType, w in pairs(weights) do
        local finalW = w
        if year and year >= 1600 and disasterTypes[wType] then
            -- 1600年后灾害概率逐步升高，崇祯年间（1628-1644）达到峰值
            local factor = 1.0 + math.min((year - 1600) / 44, 1.0) * 0.8
            finalW = math.ceil(w * factor)
        end
        adjusted[wType] = finalW
    end

    -- 加权随机
    local total = 0
    for _, w in pairs(adjusted) do total = total + w end
    local roll = math.random(total)
    local accum = 0
    for wType, w in pairs(adjusted) do
        accum = accum + w
        if roll <= accum then return wType end
    end
    return "clear"
end

--- 生成本月天气（每月调用一次）
--- @param month number 月份
--- @param year number 年份
--- @return table 天气数据 { typeId, type, season }
function WeatherSystem.GenerateWeather(month, year)
    local season = WeatherSystem.GetSeason(month)
    local weatherId = weightedRandomWeather(season, year)
    local weatherType = WeatherSystem.WEATHER_TYPES[weatherId]
    if not weatherType then
        weatherId = "clear"
        weatherType = WeatherSystem.WEATHER_TYPES.clear
    end
    return {
        typeId = weatherId,
        type = weatherType,
        season = season,
    }
end

--- 应用天气效果到月度报告
--- @param weather table GenerateWeather 返回值
--- @param report table 月度报告
function WeatherSystem.ApplyEffects(weather, report)
    local s = GameData.state
    local wt = weather.type

    -- 粮食产出调整（与 SEASON_FACTORS 叠加使用）
    -- 存储到 state 供 ProcessIndustries 读取
    s.currentWeather = weather

    -- 健康影响
    if wt.healthMod ~= 0 then
        for _, m in ipairs(GameData.GetAliveMembers()) do
            -- 年幼和年老者受天气影响更大
            local ageFactor = 1.0
            if m.age < 10 or m.age > 55 then ageFactor = 1.5 end
            local mod = math.floor(wt.healthMod * ageFactor)
            m.health = math.max(5, math.min(100, m.health + mod))
        end
    end

    -- 严重天气记录事件
    local severeWeathers = { storm = true, drought = true, flood = true, snow = true, cold = true }
    if severeWeathers[weather.typeId] then
        report.events[#report.events + 1] = weather.season.icon .. " " .. wt.name .. "——" .. wt.desc
    end
end

--- 获取天气对粮食产出的乘数
--- @return number 乘数(默认 1.0)
function WeatherSystem.GetGrainMultiplier()
    local s = GameData.state
    if s.currentWeather and s.currentWeather.type then
        return s.currentWeather.type.grainMul
    end
    return 1.0
end

--- 获取当前天气显示文本（用于顶栏）
--- @return string 如 "春 · 细雨"
function WeatherSystem.GetDisplayText()
    local s = GameData.state
    if s.currentWeather then
        local w = s.currentWeather
        return w.season.name .. " · " .. w.type.name
    end
    local season = WeatherSystem.GetSeason(s.month or 1)
    return season.name
end

--- 获取当前天气的图标数据（用于顶栏图标显示）
--- @return table { seasonImg, seasonName, weatherImg, weatherName } 或 nil
function WeatherSystem.GetDisplayInfo()
    local s = GameData.state
    if s.currentWeather then
        local w = s.currentWeather
        return {
            seasonImg = w.season.img,
            seasonName = w.season.name,
            weatherImg = w.type.img,
            weatherName = w.type.name,
        }
    end
    local season = WeatherSystem.GetSeason(s.month or 1)
    return {
        seasonImg = season.img,
        seasonName = season.name,
        weatherImg = nil,
        weatherName = nil,
    }
end

return WeatherSystem
