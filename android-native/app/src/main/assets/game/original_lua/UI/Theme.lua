-- ============================================================================
-- 大明浮生志2 - 明亮现代主题
-- 浅色暖白底色、干净简洁、现代扁平风格
-- ============================================================================

local Theme = {}

-- 主背景色（淡金/奶白系 — 翡翠绿+金色主题）
Theme.BG_LIGHT = { 250, 245, 232, 255 }       -- 页面底色（淡金米白）
Theme.BG_WHITE = { 255, 253, 247, 255 }        -- 卡片白（暖白微黄）
Theme.BG_CARD = { 255, 253, 247, 235 }         -- 卡片背景（暖白微透）
Theme.BG_PANEL = { 248, 243, 230, 255 }        -- 面板背景（淡暖金）
Theme.BG_INPUT = { 242, 237, 222, 255 }        -- 输入框/禁用态背景
Theme.BG_MAIN = { 250, 245, 232, 255 }         -- 主内容区底色

-- 文字色（深色系，在浅底上清晰可读）
Theme.TEXT_PRIMARY = { 45, 38, 28, 255 }       -- 主文字（深棕黑，加深）
Theme.TEXT_SECONDARY = { 85, 78, 65, 255 }     -- 副文字（加深，提高对比度）
Theme.TEXT_MUTED = { 130, 122, 108, 255 }      -- 灰色辅助（加深）
Theme.TEXT_TITLE = { 60, 42, 18, 255 }         -- 标题深棕（加深）
Theme.TEXT_WHITE = { 255, 255, 255, 255 }       -- 白色文字（用在深色按钮上）

-- 强调色（翡翠绿+金色主题）
Theme.GOLD = { 195, 155, 60, 255 }             -- 暖金色（标题/重点）
Theme.GOLD_DARK = { 170, 130, 40, 255 }        -- 深金
Theme.GOLD_LIGHT = { 225, 195, 100, 255 }      -- 亮金

Theme.PRIMARY = { 45, 140, 100, 255 }          -- 主操作色（翡翠绿）
Theme.PRIMARY_DARK = { 32, 118, 82, 255 }      -- 深翡翠绿
Theme.PRIMARY_LIGHT = { 215, 240, 228, 255 }   -- 浅翡翠绿背景

Theme.RED = { 220, 80, 60, 255 }               -- 红色（危险/警告）
Theme.RED_DARK = { 185, 60, 45, 255 }          -- 深红
Theme.RED_LIGHT = { 255, 235, 232, 255 }       -- 浅红背景
Theme.GREEN = { 76, 175, 80, 255 }             -- 绿色（成功/收入）
Theme.GREEN_LIGHT = { 232, 248, 233, 255 }     -- 浅绿背景
Theme.BLUE = { 66, 133, 244, 255 }             -- 蓝色（信息）
Theme.BLUE_LIGHT = { 232, 242, 255, 255 }      -- 浅蓝背景

-- 边框色（金边主题）
Theme.BORDER = { 218, 210, 190, 255 }          -- 默认边框（暖金调）
Theme.BORDER_LIGHT = { 232, 226, 210, 255 }    -- 极浅边框
Theme.BORDER_GOLD = { 195, 165, 85, 255 }      -- 金色边框

-- 资源图标色（加深饱和，确保在背景图上清晰可读）
Theme.SILVER_COLOR = { 70, 85, 120, 255 }      -- 银两（深蓝灰）
Theme.GRAIN_COLOR = { 160, 125, 20, 255 }      -- 粮食（深金黄）
Theme.CLOTH_COLOR = { 45, 110, 170, 255 }      -- 布匹（深蓝）
Theme.FAME_COLOR = { 185, 120, 15, 255 }       -- 声望（深橙金）

-- 导航栏
Theme.NAV_BG = { 248, 243, 228, 240 }          -- 导航栏背景（淡金半透明）
Theme.NAV_ACTIVE = { 45, 140, 100, 255 }       -- 激活状态（翡翠绿）
Theme.NAV_INACTIVE = { 120, 112, 95, 255 }     -- 未激活（加深灰色）

-- 半透明遮罩
Theme.OVERLAY = { 0, 0, 0, 120 }
Theme.OVERLAY_LIGHT = { 0, 0, 0, 60 }

-- 渐变
Theme.GRADIENT_TOP = {
    type = "linear",
    direction = "to-bottom",
    from = { 252, 249, 242, 255 },
    to = { 247, 242, 230, 255 },
}

Theme.GRADIENT_PRIMARY = {
    type = "linear",
    direction = "to-bottom",
    from = { 55, 155, 110, 255 },
    to = { 32, 118, 82, 255 },
}

Theme.GRADIENT_GOLD = {
    type = "linear",
    direction = "to-bottom",
    from = { 220, 180, 70, 255 },
    to = { 190, 150, 50, 255 },
}

Theme.GRADIENT_RED = {
    type = "linear",
    direction = "to-bottom",
    from = { 230, 90, 70, 255 },
    to = { 195, 65, 50, 255 },
}

-- 时间控制区
Theme.TIME_BG = { 248, 243, 228, 240 }
Theme.SPEED_ACTIVE = { 45, 140, 100, 255 }     -- 速度激活（翡翠绿）
Theme.SPEED_PAUSED = { 220, 80, 60, 255 }      -- 暂停色（红）

-- ============================================================================
-- UI 素材路径
-- ============================================================================

Theme.IMG = {
    MENU_BG       = "image/edited_menu_bg_clean_20260510194159.png",
    BG_TEXTURE    = "image/bg_light_texture_20260510191243.png",
    LOGO          = "image/logo_title_20260510193951.png",
    BTN_START     = "image/btn_start_20260510194730.png",
    BTN_CONTINUE  = "image/btn_load_20260510194725.png",
    BTN_SAVES     = "image/btn_archive_20260510194700.png",
    BTN_SETTINGS  = "image/btn_config_20260510194729.png",

    -- 开局创建 - 姓氏卡片
    SEAL_LI       = "image/seal_li_20260510203822.png",
    SEAL_WANG     = "image/seal_wang_20260510203841.png",
    SEAL_ZHANG    = "image/seal_zhang_20260510203909.png",
    SEAL_LIU      = "image/seal_liu_20260510203934.png",
    SEAL_CHEN     = "image/seal_chen_20260510203955.png",
    SEAL_ZHAO     = "image/seal_zhao_20260510203220.png",
    SEAL_ZHU      = "image/seal_zhu_20260510204016.png",
    SEAL_YANG     = "image/seal_yang_20260510204541.png",
    SEAL_HUANG    = "image/seal_huang_20260510204600.png",
    SEAL_ZHOU     = "image/seal_zhou_20260510204807.png",

    -- 开局创建 - 出身卡片
    ORIGIN_FARMER   = "image/origin_farmer_v3_20260510210645.png",
    ORIGIN_LANDLORD = "image/origin_landlord_20260510211142.png",
    ORIGIN_MILITARY = "image/origin_military_20260510211202.png",

    -- 开局创建 - 地域卡片
    REGION_SHAANBEI = "image/region_shaanbei_20260510212050.png",
    REGION_HENAN    = "image/region_henan_20260510212351.png",
    REGION_JIANGNAN = "image/region_jiangnan_20260510212801.png",
    REGION_HUGUANG  = "image/region_huguang_20260510212445.png",

    -- 开局创建 - 家训卡片
    MOTTO_STUDY   = "image/motto_study_20260510205820.png",
    MOTTO_MARTIAL = "image/edited_motto_martial_20260510205400.png",
    MOTTO_TRADE   = "image/motto_trade_20260510205839.png",
    MOTTO_FARM    = "image/motto_farm_20260510205905.png",
    MOTTO_FORTIFY = "image/motto_fortify_20260510205936.png",

    -- 开局创建 - 难度卡片
    DIFF_EASY   = "image/diff_easy_20260510213852.png",
    DIFF_NORMAL = "image/diff_normal_20260510215034.png",
    DIFF_HARD   = "image/diff_hard_20260510215332.png",

    -- 开局创建 - 开始按钮
    BTN_START_JOURNEY = "image/btn_start_journey_20260510213136.png",

    -- 菜单和时间控制图标
    ICON_MENU    = "image/icon_menu_20260511054951.png",
    ICON_PAUSE   = "image/icon_pause_20260511055015.png",
    ICON_PLAY1X  = "image/icon_play1x_20260511054940.png",
    ICON_PLAY2X  = "image/icon_play2x_20260511055002.png",
    ICON_PLAY3X  = "image/icon_play3x_20260511054939.png",

    -- 顶部资源图标（古风水墨 128x128）
    RES_SILVER   = "image/res_icon_silver_20260511123321.png",
    RES_GRAIN    = "image/res_icon_grain_20260511123117.png",
    RES_CLOTH    = "image/res_icon_cloth_20260511122929.png",
    RES_FAME     = "image/res_icon_fame_20260511123320.png",
    RES_POP      = "image/res_icon_pop_20260511054455.png",

    -- 底部导航按钮（圆形金色徽章 128x128）
    NAV_TREE     = "image/nav_btn_tree_v4_20260511051728.png",
    NAV_CLAN     = "image/nav_btn_clan_v4_20260511051832.png",
    NAV_MEMBERS  = "image/nav_btn_members_v5_20260511052300.png",
    NAV_INDUSTRY = "image/nav_btn_industry_v5_20260511052428.png",
    NAV_CAREER   = "image/nav_btn_career_v5_20260511052458.png",

    -- 功能浮动按钮（圆形金色徽章 128x128）
    NAV_BATTLE    = "image/nav_btn_battle_20260511235439.png",
    NAV_PRAY      = "image/nav_btn_pray_20260511235034.png",
    NAV_COURTESAN = "image/nav_btn_courtesan_20260511235034.png",
    NAV_SEAL      = "image/nav_btn_seal_20260511235035.png",
    NAV_LOAN      = "image/nav_btn_loan_20260512064520.png",
    NAV_CLINIC    = "image/nav_btn_clinic_20260513093122.png",
    NAV_LABOR     = "image/nav_btn_labor_20260513095832.png",

    -- UI 背景素材（翡翠绿+金色主题）
    BG_MAIN_LANDSCAPE = "image/edited_bg_main_landscape_fill_20260511200448.png",
    BG_BOTTOM_NAV     = "image/edited_bg_bottom_nav_fill_20260511200028.png",
    BG_TOP_BAR        = "image/edited_bg_top_bar_fill_20260511200010.png",
    BG_CONTROL_PANEL  = "image/edited_bg_control_panel_fill_20260511195749.png",
    DECO_TITLE_BANNER = "image/deco_title_banner_20260511194040.png",

    -- 游戏内操作按钮（圆形 256x256）
    BTN_UPGRADE  = "image/btn_upgrade_circle_20260511192739.png",
    BTN_WORSHIP  = "image/btn_worship_circle_20260511192919.png",
    BTN_FORT     = "image/btn_fort_circle_20260511192924.png",

    -- 游戏内操作按钮（横条形 256x64）
    BTN_STUDY    = "image/btn_study_bar_20260511031733.png",
    BTN_TRADE    = "image/btn_trade_bar_20260511033400.png",
    BTN_MARRIAGE = "image/btn_marriage_bar_20260511033417.png",
    BTN_RECALL   = "image/btn_recall_bar_20260511033433.png",
    BTN_EXAM     = "image/btn_exam_bar_20260511033455.png",
    BTN_MILITARY = "image/btn_military_bar_20260511033512.png",

    -- 季节图标（古风水墨圆形 256x256）
    SEASON_SPRING = "image/icon_season_spring_20260513103222.png",
    SEASON_SUMMER = "image/icon_season_summer_20260513103227.png",
    SEASON_AUTUMN = "image/icon_season_autumn_20260513103022.png",
    SEASON_WINTER = "image/icon_season_winter_20260513103027.png",

    -- 天气图标（古风水墨圆形 256x256）
    WEATHER_CLEAR    = "image/icon_weather_clear_20260513103014.png",
    WEATHER_CLOUDY   = "image/icon_weather_cloudy_20260513103625.png",
    WEATHER_RAIN     = "image/icon_weather_rain_20260513103627.png",
    WEATHER_STORM    = "image/icon_weather_storm_20260513103026.png",
    WEATHER_DROUGHT  = "image/icon_weather_drought_20260513103629.png",
    WEATHER_FLOOD    = "image/icon_weather_flood_20260513103225.png",
    WEATHER_SNOW     = "image/icon_weather_snow_20260513103223.png",
    WEATHER_FROST    = "image/icon_weather_frost_20260513103624.png",
    WEATHER_FOG      = "image/icon_weather_fog_20260513103629.png",
    WEATHER_WIND     = "image/icon_weather_wind_20260513103626.png",
    WEATHER_PLEASANT = "image/icon_weather_pleasant_20260513103626.png",
    WEATHER_COLD     = "image/icon_weather_cold_20260513103631.png",

    -- 族谱页面背景
    BG_FAMILY_TREE = "image/1000752662.png",
    -- 族人卡片装饰边框（含顶部圆形头像框）
    CARD_FRAME_MEMBER = "image/card_frame_member_20260511204127.png",
}

-- ============================================================================
-- 通用样式（纯色现代风格，不依赖图片）
-- ============================================================================

Theme.PANEL_STYLE = {
    backgroundColor = Theme.BG_WHITE,
    borderRadius = 12,
    borderWidth = 1,
    borderColor = Theme.BORDER,
}

Theme.CARD_STYLE = {
    backgroundColor = Theme.BG_WHITE,
    borderRadius = 12,
    borderWidth = 1,
    borderColor = Theme.BORDER,
    padding = 12,
}

-- 主按钮样式（青绿渐变）
Theme.BTN_PRIMARY_STYLE = {
    backgroundGradient = Theme.GRADIENT_PRIMARY,
    borderRadius = 8,
    justifyContent = "center",
    alignItems = "center",
}

-- 次要按钮样式
Theme.BTN_SECONDARY_STYLE = {
    backgroundColor = Theme.BG_WHITE,
    borderWidth = 1,
    borderColor = Theme.BORDER,
    borderRadius = 8,
    justifyContent = "center",
    alignItems = "center",
}

-- 导航栏样式（白色底 + 底部细线）
Theme.NAV_BAR_STYLE = {
    backgroundColor = Theme.NAV_BG,
}

return Theme
