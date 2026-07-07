-- ============================================================================
-- 大明浮生志2 - 游戏弹窗通知（正式弹窗，需手动确认关闭）
-- 居中面板式提示，古风主题，点击"知道了"按钮关闭
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")

local Toast = {}

-- 当前显示的弹窗引用（同一时间只显示一个，新弹窗覆盖旧弹窗）
local currentPopup_ = nil

-- 弹窗类型配置
local POPUP_CONFIG = {
    success = {
        icon = "OK",
        iconColor = Theme.GREEN,
        borderColor = { 76, 175, 80, 180 },
        titleColor = Theme.GREEN,
    },
    warn = {
        icon = "⚠",
        iconColor = { 220, 160, 50, 255 },
        borderColor = Theme.BORDER_GOLD,
        titleColor = { 185, 140, 30, 255 },
    },
    error = {
        icon = "X",
        iconColor = Theme.RED,
        borderColor = { 220, 80, 60, 180 },
        titleColor = Theme.RED,
    },
    info = {
        icon = "ℹ",
        iconColor = Theme.GOLD,
        borderColor = Theme.BORDER_GOLD,
        titleColor = Theme.GOLD,
    },
    locked = {
        icon = "🔒",
        iconColor = { 165, 160, 148, 255 },
        borderColor = Theme.BORDER_GOLD,
        titleColor = Theme.GOLD_DARK,
    },
}

-- 倒计时自动关闭（保留接口，但不再需要）
function Toast.Update(dt)
    -- 不再自动消失，保留空函数以兼容 GameScreen 的调用
end

--- 关闭当前弹窗
function Toast.DismissPopup()
    if currentPopup_ then
        local root = UI.GetRoot()
        if root then
            root:RemoveChild(currentPopup_)
        end
        currentPopup_ = nil
    end
end

--- 显示自定义弹窗（带确认按钮，需手动关闭）
--- @param variant string "success"|"warn"|"error"|"info"|"locked"
--- @param title string 标题文字
--- @param desc string|nil 描述文字（可选）
--- @param duration number|nil 已废弃，保留参数兼容旧接口
local function ShowPopup(variant, title, desc, duration)
    -- 先关闭旧弹窗
    Toast.DismissPopup()

    local config = POPUP_CONFIG[variant] or POPUP_CONFIG.info

    -- 构建内容子元素
    local cardChildren = {}

    -- 顶部金色装饰线
    cardChildren[#cardChildren + 1] = UI.Panel {
        width = 40, height = 2, borderRadius = 1,
        backgroundColor = Theme.GOLD_LIGHT,
        marginBottom = 4,
    }

    -- 图标
    cardChildren[#cardChildren + 1] = UI.Label {
        text = config.icon,
        fontSize = 22,
        fontColor = config.iconColor,
        textAlign = "center",
    }

    -- 标题
    cardChildren[#cardChildren + 1] = UI.Label {
        text = title,
        fontSize = 15,
        fontColor = config.titleColor,
        fontWeight = "bold",
        textAlign = "center",
        whiteSpace = "normal",
        marginTop = 4,
    }

    -- 描述（如有）
    if desc and desc ~= "" then
        cardChildren[#cardChildren + 1] = UI.Label {
            text = desc,
            fontSize = 12,
            fontColor = Theme.TEXT_SECONDARY,
            textAlign = "center",
            whiteSpace = "normal",
            marginTop = 2,
        }
    end

    -- 底部金色装饰线
    cardChildren[#cardChildren + 1] = UI.Panel {
        width = "60%", height = 1,
        backgroundColor = Theme.BORDER_GOLD,
        marginTop = 8,
    }

    -- "知道了"确认按钮
    cardChildren[#cardChildren + 1] = UI.Panel {
        width = 100, height = 32, borderRadius = 6, marginTop = 8,
        backgroundGradient = Theme.GRADIENT_PRIMARY,
        justifyContent = "center", alignItems = "center",
        onTap = function()
            Toast.DismissPopup()
        end,
        children = {
            UI.Label { text = "知道了", fontSize = 13, fontColor = Theme.TEXT_WHITE },
        },
    }

    -- 弹窗面板（带遮罩层）
    local popup = UI.Panel {
        id = "toastPopup",
        width = "100%", height = "100%",
        position = "absolute", left = 0, top = 0, zIndex = 800,
        justifyContent = "center", alignItems = "center",
        backgroundColor = { 0, 0, 0, 100 },
        children = {
            -- 内容卡片
            UI.Panel {
                width = 240, minHeight = 90,
                backgroundColor = Theme.BG_WHITE,
                borderRadius = 12,
                borderWidth = 2,
                borderColor = config.borderColor,
                padding = { 16, 20, 16, 20 },
                alignItems = "center",
                gap = 2,
                children = cardChildren,
            },
        },
    }

    -- 挂载到 UI 根节点
    local root = UI.GetRoot()
    if root then
        root:AddChild(popup)
        currentPopup_ = popup
    end
end

-- ============================================================================
-- 公开接口（保持与旧 Toast API 兼容）
-- ============================================================================

--- 通用弹窗（兼容 Toast.Show 调用）
function Toast.Show(msg, duration)
    if not msg or msg == "" then return end
    ShowPopup("info", msg, nil, duration)
end

--- 显示信息弹窗
function Toast.Info(msg, duration)
    if not msg or msg == "" then return end
    ShowPopup("info", msg, nil, duration)
end

--- 显示成功弹窗
function Toast.Success(msg, duration)
    ShowPopup("success", msg, nil, duration)
end

--- 显示警告弹窗
function Toast.Warn(msg, duration)
    ShowPopup("warn", msg, nil, duration)
end

--- 显示错误弹窗
function Toast.Error(msg, duration)
    ShowPopup("error", msg, nil, duration)
end

--- 资源不足提示（专用样式）
function Toast.NotEnough(resourceName)
    ShowPopup("warn", "资源不足", resourceName .. "不够，无法执行此操作。")
end

--- 功能未解锁提示（专用样式）
function Toast.Locked(featureName, reqRank)
    ShowPopup("locked", "尚未解锁", featureName .. "需要品级【" .. (reqRank or "?") .. "】才能开启。")
end

return Toast
