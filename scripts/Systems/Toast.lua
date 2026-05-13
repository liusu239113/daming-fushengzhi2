-- ============================================================================
-- 大明浮生志2 - 自定义游戏弹窗通知
-- 居中浮动面板式提示，古风主题，自动消失
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")

local Toast = {}

-- 当前显示的弹窗引用（同一时间只显示一个，新弹窗覆盖旧弹窗）
local currentPopup_ = nil
local popupTimer_ = 0
local popupDuration_ = 0

-- 弹窗类型配置
local POPUP_CONFIG = {
    success = {
        icon = "✓",
        iconColor = { 76, 175, 80, 255 },
        borderColor = { 76, 175, 80, 180 },
        bgColor = { 255, 255, 252, 245 },
        accentColor = { 232, 248, 233, 255 },
    },
    warn = {
        icon = "!",
        iconColor = { 220, 160, 50, 255 },
        borderColor = { 220, 160, 50, 180 },
        bgColor = { 255, 253, 248, 245 },
        accentColor = { 255, 248, 225, 255 },
    },
    error = {
        icon = "✕",
        iconColor = { 220, 80, 60, 255 },
        borderColor = { 220, 80, 60, 180 },
        bgColor = { 255, 252, 251, 245 },
        accentColor = { 255, 235, 232, 255 },
    },
    info = {
        icon = "i",
        iconColor = { 66, 133, 244, 255 },
        borderColor = { 66, 133, 244, 180 },
        bgColor = { 252, 253, 255, 245 },
        accentColor = { 232, 242, 255, 255 },
    },
    locked = {
        icon = "[锁]",
        iconColor = { 165, 160, 148, 255 },
        borderColor = { 200, 155, 50, 180 },
        bgColor = { 255, 253, 248, 245 },
        accentColor = { 255, 248, 225, 255 },
    },
}

-- 倒计时自动关闭（由外部 HandleGameUpdate 调用，不再独立订阅 Update 事件）
function Toast.Update(dt)
    if not currentPopup_ then return end
    popupTimer_ = popupTimer_ + dt
    if popupTimer_ >= popupDuration_ then
        Toast.DismissPopup()
    end
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

--- 显示自定义弹窗
--- @param variant string "success"|"warn"|"error"|"info"|"locked"
--- @param title string 标题文字
--- @param desc string|nil 描述文字（可选）
--- @param duration number|nil 显示时长（秒，默认2.5）
local function ShowPopup(variant, title, desc, duration)
    -- 先关闭旧弹窗
    Toast.DismissPopup()

    local config = POPUP_CONFIG[variant] or POPUP_CONFIG.info
    popupDuration_ = duration or 2.5
    popupTimer_ = 0

    -- 构建内容（仅文字，无图标）
    local contentChildren = {}

    -- 标题
    contentChildren[#contentChildren + 1] = UI.Label {
        text = title,
        fontSize = 15,
        fontColor = Theme.TEXT_PRIMARY,
        fontWeight = "bold",
        textAlign = "center",
    }

    -- 描述（如有）
    if desc and desc ~= "" then
        contentChildren[#contentChildren + 1] = UI.Label {
            text = desc,
            fontSize = 12,
            fontColor = Theme.TEXT_SECONDARY,
            textAlign = "center",
            whiteSpace = "normal",
            marginTop = 2,
        }
    end

    -- 弹窗面板
    local popup = UI.Panel {
        id = "gamePopup",
        width = "100%", height = "100%",
        position = "absolute", left = 0, top = 0, zIndex = 800,
        justifyContent = "center", alignItems = "center",
        backgroundColor = { 0, 0, 0, 50 },
        onPointerDown = function(self)
            Toast.DismissPopup()
        end,
        children = {
            -- 内容卡片
            UI.Panel {
                width = 220, minHeight = 80,
                backgroundColor = config.bgColor,
                borderRadius = 14,
                borderWidth = 2,
                borderColor = config.borderColor,
                padding = { 16, 20, 16, 20 },
                alignItems = "center",
                gap = 2,
                -- 顶部装饰线
                children = (function()
                    local c = {}
                    -- 顶部金色细线装饰
                    c[#c + 1] = UI.Panel {
                        width = 40, height = 2, borderRadius = 1,
                        backgroundColor = Theme.GOLD_LIGHT,
                        marginBottom = 6,
                    }
                    -- 内容（图标、标题、描述）
                    for _, child in ipairs(contentChildren) do
                        c[#c + 1] = child
                    end
                    -- 底部金色细线装饰
                    c[#c + 1] = UI.Panel {
                        width = 40, height = 2, borderRadius = 1,
                        backgroundColor = Theme.GOLD_LIGHT,
                        marginTop = 8,
                    }
                    return c
                end)(),
            },
        },
    }

    -- 挂载到 UI 根节点
    local root = UI.GetRoot()
    if root then
        root:AddChild(popup)
        currentPopup_ = popup

        -- Toast 计时器由 HandleGameUpdate 驱动（Toast.Update），不再独立订阅 Update 事件
    end
end

-- 注意：不再使用全局 SubscribeToEvent("Update")，避免覆盖 HandleGameUpdate
-- Toast.Update(dt) 由 GameScreen.HandleGameUpdate 每帧调用

-- ============================================================================
-- 公开接口（保持与旧 Toast API 兼容）
-- ============================================================================

--- 通用弹窗（兼容 Toast.Show 调用）
function Toast.Show(msg, duration)
    if not msg or msg == "" then return end  -- 空消息不弹窗
    ShowPopup("info", msg, nil, duration or 2.5)
end

--- 显示信息弹窗
function Toast.Info(msg, duration)
    if not msg or msg == "" then return end
    ShowPopup("info", msg, nil, duration or 2.5)
end

--- 显示成功弹窗
function Toast.Success(msg, duration)
    ShowPopup("success", msg, nil, duration or 2.0)
end

--- 显示警告弹窗
function Toast.Warn(msg, duration)
    ShowPopup("warn", msg, nil, duration or 3.0)
end

--- 显示错误弹窗
function Toast.Error(msg, duration)
    ShowPopup("error", msg, nil, duration or 3.0)
end

--- 资源不足提示（专用样式）
function Toast.NotEnough(resourceName)
    ShowPopup("warn", "资源不足", resourceName .. "不够，无法执行此操作。", 2.5)
end

--- 功能未解锁提示（专用样式）
function Toast.Locked(featureName, reqRank)
    ShowPopup("locked", "尚未解锁", featureName .. "需要品级【" .. (reqRank or "?") .. "】才能开启。", 3.0)
end

return Toast
