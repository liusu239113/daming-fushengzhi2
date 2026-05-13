-- ============================================================================
-- 大明浮生志2 - 主菜单界面（明亮现代风格）
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local SaveSystem = require("Systems.SaveSystem")
local AudioManager = require("Systems.AudioManager")

local MainMenu = {}

function MainMenu.Create(callbacks)
    -- 兼容旧版存档：如果旧 slot1 存在但新槽位不存在，自动迁移
    SaveSystem.MigrateOldSlots()
    local hasSave = SaveSystem.HasAnySave()

    return UI.Panel {
        id = "mainMenu",
        width = "100%",
        height = "100%",
        backgroundColor = Theme.BG_LIGHT,
        backgroundImage = Theme.IMG.MENU_BG,
        backgroundFit = "cover",
        justifyContent = "center",
        alignItems = "center",
        children = {
            -- 柔和遮罩
            UI.Panel {
                position = "absolute",
                top = 0, left = 0, right = 0, bottom = 0,
                backgroundColor = { 255, 252, 245, 80 },
                pointerEvents = "none",
            },

            -- 主内容区
            UI.Panel {
                width = "92%",
                alignItems = "center",
                gap = 24,
                children = {
                    -- 标题区（透明 logo 图片）
                    UI.Panel {
                        width = "100%",
                        alignItems = "center",
                        marginBottom = 4,
                        children = {
                            -- Logo 图片
                            UI.Panel {
                                width = "100%",
                                height = 300,
                                backgroundImage = Theme.IMG.LOGO,
                                backgroundFit = "contain",
                            },

                        },
                    },

                    -- 按钮组（图片式按钮，往下留空间）
                    UI.Panel {
                        width = "100%",
                        gap = 12,
                        marginTop = 20,
                        alignItems = "center",
                        children = {
                            MainMenu.CreateImageButton(Theme.IMG.BTN_START, false, function()
                                if callbacks.onNewGame then callbacks.onNewGame() end
                            end),

                            MainMenu.CreateImageButton(Theme.IMG.BTN_CONTINUE, not hasSave, function()
                                if hasSave and callbacks.onContinue then callbacks.onContinue() end
                            end),

                            MainMenu.CreateImageButton(Theme.IMG.BTN_SAVES, false, function()
                                if callbacks.onSaveManage then callbacks.onSaveManage() end
                            end),

                            MainMenu.CreateImageButton(Theme.IMG.BTN_SETTINGS, false, function()
                                if callbacks.onSettings then callbacks.onSettings() end
                            end),
                        },
                    },


                },
            },
        },
    }
end

--- 创建图片式按钮
function MainMenu.CreateImageButton(imgSrc, disabled, onClick)
    return UI.Panel {
        width = "100%",
        height = 100,
        opacity = disabled and 0.4 or 1.0,
        pointerEvents = disabled and "none" or "auto",
        onClick = function(self)
            if not disabled and onClick then
                AudioManager.Click()
                onClick()
            end
        end,
        children = {
            UI.Panel {
                width = "100%",
                height = "100%",
                backgroundImage = imgSrc,
                backgroundFit = "contain",
                pointerEvents = "none",
            },
        },
    }
end

return MainMenu
