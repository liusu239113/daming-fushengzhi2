-- ============================================================================
-- 大明浮生志2 - 葬礼系统
-- 死亡触发葬礼弹窗：选择规模后播放仪式
-- 薄葬/厚葬：静态灵堂图 + 哭声 + 5秒
-- 广大祭葬：看广告解锁 → 视频动画 + 收份子钱
-- ============================================================================

---@diagnostic disable: undefined-global

local UI           = require("urhox-libs/UI")
local Video        = require("urhox-libs/Video")
local Theme        = require("UI.Theme")
local AudioManager = require("Systems.AudioManager")
local AdSystem     = require("Systems.AdSystem")
local GameData     = require("Data.GameData")
local Toast        = require("Systems.Toast")
-- GameScreen 用懒加载避免循环 require（GameScreen 顶层已 require FuneralModal）
local function GetGameScreen() return require("UI.GameScreen") end

local FuneralModal = {}

-- ============================================================================
-- 葬礼规模配置
-- ============================================================================

local CLAN_RANKS = { "寒门","农户","乡绅","望族","世家","勋贵","名门","豪阀","国柱" }

-- 三种规模
local SCALES = {
    {
        key         = "simple",
        name        = "薄葬",
        desc        = "简朴仪式，一副棺木，聊表哀思",
        minRank     = 1,
        silverCoef  = 15,   -- 花费银 = silverCoef × clanRank
        clothCoef   = 3,    -- 花费布
        fameReward  = 3,
        color       = { 160, 150, 120, 255 },
        adRequired  = false,
    },
    {
        key         = "grand",
        name        = "厚葬",
        desc        = "隆重仪式，宾客云集，彰显家威",
        minRank     = 3,
        silverCoef  = 50,
        clothCoef   = 12,
        fameCoef    = 4,    -- 额外消耗声望
        fameReward  = 10,
        color       = { 212, 175, 55, 255 },
        adRequired  = false,
    },
    {
        key         = "royal",
        name        = "广大祭葬",
        desc        = "皇家御礼，八方来贺，大操大办",
        minRank     = 1,   -- 所有族人均可举办
        free        = true,   -- 免费（但需看广告）
        fameReward  = 30,
        -- 份子钱：参吊宾客赠银，系数 × clanRank
        fenziCoef   = 25,   -- 赚到银 = fenziCoef × clanRank
        color       = { 180, 60, 60, 255 },
        adRequired  = true,   -- 需看广告
    },
}

-- ============================================================================
-- 工具函数
-- ============================================================================

--- 根据逝者返回哭灵配音路径
---@param member table
---@return string
local function GetVoicePath(member)
    if not member then return "audio/voice/funeral_cry_adult_voice.ogg" end
    if member.age < 15 then
        return "audio/voice/funeral_cry_child_voice.ogg"
    elseif member.age >= 60 then
        return "audio/voice/funeral_cry_elder_voice.ogg"
    elseif member.gender == "female" then
        return "audio/voice/funeral_cry_female_voice.ogg"
    else
        return "audio/voice/funeral_cry_adult_voice.ogg"
    end
end

--- 计算某规模的实际花费 (silver, cloth, fame)
---@param scale table
---@param clanRank number
---@return number, number, number
local function CalcCost(scale, clanRank)
    if scale.free then return 0, 0, 0 end
    local r = math.max(1, clanRank)
    return
        (scale.silverCoef or 0) * r,
        (scale.clothCoef  or 0) * r,
        (scale.fameCoef   or 0) * r
end

-- ============================================================================
-- 普通葬礼仪式（薄葬 / 厚葬）
-- 静态灵堂图 + 哭灵配音 + 葬礼 BGM，5 秒后自动结束
-- ============================================================================

local function PlaySimpleCeremony(gameRoot, member, scaleName, fameReward, onComplete)
    local prevBgmKey = AudioManager.GetCurrentBGMKey()

    -- 播放葬礼 BGM 和哭声
    AudioManager.PlayBGM("FUNERAL")
    AudioManager.PlayFile(GetVoicePath(member))

    local memberName = member and member.name or "族人"
    local memberAge  = member and member.age  or 0
    local ageText = memberAge <= 2
        and ("年仅" .. memberAge .. "岁")
        or  ("享年" .. memberAge .. "岁")

    local DURATION = 5.0
    local elapsed  = 0.0
    local finished = false
    ---@type table
    local overlayRef = nil  -- 闭包引用，直接用于移除，不依赖 FindById

    local function Finish()
        if finished then return end
        finished = true
        log:Write(LOG_INFO, "[FuneralModal][Simple] Finish called, elapsed=" .. elapsed)

        if overlayRef and gameRoot then
            gameRoot:RemoveChild(overlayRef)
            overlayRef = nil
            log:Write(LOG_INFO, "[FuneralModal][Simple] overlay removed OK")
        else
            log:Write(LOG_WARNING, "[FuneralModal][Simple] overlay remove FAILED: overlayRef=" .. tostring(overlayRef) .. " gameRoot=" .. tostring(gameRoot))
        end
        if prevBgmKey ~= "" then
            AudioManager.PlayBGM(prevBgmKey)
        end
        if fameReward and fameReward > 0 and GameData.state then
            GameData.state.fame = (GameData.state.fame or 0) + fameReward
        end
        -- 结算弹窗
        local resultModal
        resultModal = UI.Modal {
            title = scaleName .. " · 礼成",
            size = "sm",
            showCloseButton = false,
            closeOnOverlay = false,
            children = {
                UI.Panel {
                    gap = 12, alignItems = "center", width = "100%",
                    children = {
                        UI.Label {
                            text = memberName .. " 入土为安",
                            fontSize = 15, fontColor = { 220, 200, 160, 255 }, fontWeight = "bold",
                            textAlign = "center",
                        },
                        UI.Panel {
                            width = "100%", padding = 10, borderRadius = 8,
                            backgroundColor = { 30, 30, 40, 200 },
                            borderWidth = 1, borderColor = { 100, 90, 70, 150 },
                            gap = 6,
                            children = {
                                fameReward and fameReward > 0 and UI.Panel {
                                    flexDirection = "row", justifyContent = "space-between",
                                    children = {
                                        UI.Label { text = "声誉积累", fontSize = 13, fontColor = { 200, 190, 160, 255 } },
                                        UI.Label { text = "声望+" .. fameReward, fontSize = 13, fontColor = { 212, 175, 55, 255 }, fontWeight = "bold" },
                                    },
                                } or UI.Label { text = "薄葬从简，无声誉加成", fontSize = 12, fontColor = { 140, 130, 110, 255 } },
                            },
                        },
                        UI.Panel {
                            width = "100%", height = 38, borderRadius = 6,
                            backgroundColor = { 80, 65, 45, 255 },
                            justifyContent = "center", alignItems = "center",
                            onTap = function()
                                AudioManager.Click()
                                log:Write(LOG_INFO, "[FuneralModal][Simple] 礼毕 clicked, calling onComplete")
                                resultModal:Close()
                                if onComplete then onComplete() end
                            end,
                            children = { UI.Label { text = "礼毕", fontSize = 14, fontColor = { 240, 220, 160, 255 }, fontWeight = "bold" } },
                        },
                    },
                },
            },
        }
        resultModal:Open(gameRoot)
    end

    GetGameScreen().RegisterCeremonyUpdate(function(dt)
        if finished then return false end
        elapsed = elapsed + dt
        -- 更新进度条
        if gameRoot then
            local bar = gameRoot:FindById("funeralSimpleProgress")
            if bar then
                local pct = math.min(elapsed / DURATION, 1.0) * 100
                bar:SetStyle({ width = math.floor(pct) .. "%" })
            end
        end
        if elapsed >= DURATION then
            Finish()
            return false
        end
        return true
    end)

    -- 遮罩 UI
    local overlay = UI.Panel {
        id = "funeralSimpleOverlay",
        width = "100%", height = "100%",
        position = "absolute", left = 0, top = 0, zIndex = 960,
        backgroundColor = { 0, 0, 0, 220 },
        justifyContent = "center", alignItems = "center",
        children = {
            UI.Panel {
                width = "85%", maxWidth = 360,
                gap = 0, alignItems = "center",
                children = {
                    -- 灵堂图
                    UI.Panel {
                        width = "100%", height = 260,
                        borderRadius = 8,
                        backgroundImage = "image/funeral_bg_20260523105956.png",
                        backgroundFit = "cover",
                    },
                    -- 逝者信息条
                    UI.Panel {
                        width = "100%", paddingTop = 10, paddingBottom = 4,
                        alignItems = "center", gap = 4,
                        children = {
                            UI.Label {
                                text = memberName,
                                fontSize = 18, fontColor = { 240, 220, 160, 255 }, fontWeight = "bold",
                            },
                            UI.Label {
                                text = ageText .. "   " .. scaleName,
                                fontSize = 13, fontColor = { 180, 160, 120, 255 },
                            },
                        },
                    },
                    -- 进度条底
                    UI.Panel {
                        width = "80%", height = 4, borderRadius = 2,
                        backgroundColor = { 60, 50, 40, 255 },
                        marginTop = 8,
                        children = {
                            UI.Panel {
                                id = "funeralSimpleProgress",
                                width = "0%", height = "100%", borderRadius = 2,
                                backgroundColor = { 180, 150, 80, 255 },
                            },
                        },
                    },
                },
            },
        },
    }
    overlayRef = overlay
    gameRoot:AddChild(overlay)
end

-- ============================================================================
-- 广大祭葬：视频播放 + 份子钱
-- ============================================================================

local function PlayGrandCeremony(gameRoot, member, fameReward, fenziSilver, onComplete)
    local prevBgmKey = AudioManager.GetCurrentBGMKey()
    AudioManager.PlayBGM("FUNERAL")
    -- 0.8秒后播哭灵配音
    local voicePlayed = false
    local voiceTimer  = 0.0

    local finished    = false
    local elapsed     = 0.0   -- 必须在 Finish() 之前定义，Finish() 会引用
    ---@type table
    local overlayRef  = nil   -- 闭包引用，直接用于移除
    ---@type table
    local videoWidget = nil   -- VideoPlayer 引用，Finish 时销毁

    local function Finish()
        if finished then return end
        finished = true
        log:Write(LOG_INFO, "[FuneralModal][Grand] Finish called, elapsed=" .. elapsed)
        -- 销毁 VideoPlayer，释放 GPU 显存
        if videoWidget then
            videoWidget:Destroy()
            videoWidget = nil
        end
        if overlayRef and gameRoot then
            gameRoot:RemoveChild(overlayRef)
            overlayRef = nil
            log:Write(LOG_INFO, "[FuneralModal][Grand] overlay removed OK")
        else
            log:Write(LOG_WARNING, "[FuneralModal][Grand] overlay remove FAILED: overlayRef=" .. tostring(overlayRef) .. " gameRoot=" .. tostring(gameRoot))
        end
        if prevBgmKey ~= "" then
            AudioManager.PlayBGM(prevBgmKey)
        end
        -- 结算奖励
        if GameData.state then
            GameData.state.fame   = (GameData.state.fame   or 0) + fameReward
            GameData.state.silver = (GameData.state.silver or 0) + fenziSilver
            GameData.AddLog("广大祭葬：八方宾客登门，收份子钱银" .. fenziSilver .. "，声望+" .. fameReward)
        end
        -- 结算弹窗
        local resultModal
        resultModal = UI.Modal {
            title = "广大祭葬 · 礼成",
            size = "sm",
            showCloseButton = false,
            closeOnOverlay = false,
            children = {
                UI.Panel {
                    gap = 12, alignItems = "center", width = "100%",
                    children = {
                        UI.Label {
                            text = (member and member.name or "族人") .. " 入土为安",
                            fontSize = 15, fontColor = { 220, 200, 160, 255 }, fontWeight = "bold",
                            textAlign = "center",
                        },
                        UI.Panel {
                            width = "100%", padding = 10, borderRadius = 8,
                            backgroundColor = { 40, 50, 30, 200 },
                            borderWidth = 1, borderColor = { 100, 150, 80, 150 },
                            gap = 6,
                            children = {
                                UI.Label { text = "宾客返礼", fontSize = 13, fontColor = { 170, 200, 120, 255 }, fontWeight = "bold" },
                                UI.Panel {
                                    flexDirection = "row", justifyContent = "space-between",
                                    children = {
                                        UI.Label { text = "份子钱", fontSize = 13, fontColor = { 200, 190, 160, 255 } },
                                        UI.Label { text = "银+" .. fenziSilver, fontSize = 13, fontColor = { 220, 200, 100, 255 }, fontWeight = "bold" },
                                    },
                                },
                                UI.Panel {
                                    flexDirection = "row", justifyContent = "space-between",
                                    children = {
                                        UI.Label { text = "声誉积累", fontSize = 13, fontColor = { 200, 190, 160, 255 } },
                                        UI.Label { text = "声望+" .. fameReward, fontSize = 13, fontColor = { 212, 175, 55, 255 }, fontWeight = "bold" },
                                    },
                                },
                            },
                        },
                        UI.Panel {
                            width = "100%", height = 38, borderRadius = 6,
                            backgroundColor = { 100, 80, 50, 255 },
                            justifyContent = "center", alignItems = "center",
                            onTap = function()
                                AudioManager.Click()
                                log:Write(LOG_INFO, "[FuneralModal][Grand] 礼毕 clicked, calling onComplete")
                                resultModal:Close()
                                if onComplete then onComplete() end
                            end,
                            children = { UI.Label { text = "礼毕", fontSize = 14, fontColor = { 240, 220, 160, 255 }, fontWeight = "bold" } },
                        },
                    },
                },
            },
        }
        resultModal:Open(gameRoot)
    end

    -- 注册仪式计时（通过 GameScreen.RegisterCeremonyUpdate 避免覆盖 HandleGameUpdate 订阅）
    GetGameScreen().RegisterCeremonyUpdate(function(dt)
        if finished then return false end
        elapsed = elapsed + dt
        voiceTimer = voiceTimer + dt
        if not voicePlayed and voiceTimer >= 0.8 then
            voicePlayed = true
            AudioManager.PlayFile(GetVoicePath(member))
        end
        -- 更新跳过提示文字
        if overlayRef then
            local hint = overlayRef:FindById("funeralGrandSkipHint")
            if hint then
                if elapsed < 3.0 then
                    hint:SetText(math.ceil(3.0 - elapsed) .. "秒后可点击跳过")
                else
                    hint:SetText("点击屏幕跳过")
                end
            end
        end
        -- 15秒兜底自动结束（视频 onEnded 通常会先触发）
        if elapsed >= 15.0 then
            Finish()
            return false
        end
        return true
    end)

    local memberName = member and member.name or "族人"
    local memberAge  = member and member.age  or 0
    local ageText = memberAge <= 2
        and ("年仅" .. memberAge .. "岁")
        or  ("享年" .. memberAge .. "岁")

    local overlay = UI.Panel {
        id = "funeralGrandOverlay",
        width = "100%", height = "100%",
        position = "absolute", left = 0, top = 0, zIndex = 960,
        backgroundColor = { 0, 0, 0, 230 },
        justifyContent = "center", alignItems = "center",
        onTap = function()
            if elapsed >= 3.0 then Finish() end
        end,
        children = {
            UI.Panel {
                width = "85%", maxWidth = 360,
                alignItems = "center", gap = 0,
                children = {
                    -- 视频区域（带金色边框）
                    UI.Panel {
                        width = "100%", borderRadius = 8,
                        borderWidth = 2, borderColor = { 180, 150, 60, 200 },
                        overflow = "hidden",
                        children = {
                            -- 视频播放（Video.VideoPlayer，播完自动调 Finish）
                            (function()
                                videoWidget = Video.VideoPlayer {
                                    src           = "video/funeral_grand.mp4",
                                    width         = "100%",
                                    height        = 260,
                                    textureWidth  = 1280,
                                    textureHeight = 720,
                                    autoPlay      = true,
                                    loop          = false,
                                    muted         = false,
                                    objectFit     = "cover",
                                    onEnded       = function() Finish() end,
                                }
                                return videoWidget
                            end)(),
                            -- 金色标题牌（浮在视频上方）
                            UI.Panel {
                                position = "absolute", top = 10, left = 0, right = 0,
                                alignItems = "center",
                                children = {
                                    UI.Panel {
                                        paddingHorizontal = 14, paddingVertical = 4, borderRadius = 4,
                                        backgroundColor = { 100, 80, 20, 200 },
                                        borderWidth = 1, borderColor = { 212, 175, 55, 180 },
                                        children = {
                                            UI.Label { text = "广大祭葬", fontSize = 14, fontColor = { 255, 230, 100, 255 }, fontWeight = "bold" },
                                        },
                                    },
                                },
                            },
                        },
                    },
                    -- 逝者信息
                    UI.Panel {
                        width = "100%", paddingTop = 10, paddingBottom = 4,
                        alignItems = "center", gap = 4,
                        children = {
                            UI.Label {
                                text = memberName,
                                fontSize = 18, fontColor = { 240, 220, 160, 255 }, fontWeight = "bold",
                            },
                            UI.Label {
                                text = ageText .. "   广大祭葬",
                                fontSize = 13, fontColor = { 180, 160, 120, 255 },
                            },
                        },
                    },
                    -- 份子钱预告
                    UI.Panel {
                        flexDirection = "row", gap = 16, justifyContent = "center", marginTop = 6,
                        children = {
                            UI.Label { text = "银+" .. fenziSilver, fontSize = 13, fontColor = { 150, 220, 150, 255 }, fontWeight = "bold" },
                            UI.Label { text = "声望+" .. fameReward, fontSize = 13, fontColor = { 212, 175, 55, 255 }, fontWeight = "bold" },
                        },
                    },
                    -- 跳过提示（id 供 Update 更新文字）
                    UI.Label {
                        id = "funeralGrandSkipHint",
                        text = "3秒后可点击跳过",
                        fontSize = 11, fontColor = { 100, 90, 75, 160 }, marginTop = 8,
                    },
                },
            },
        },
    }
    overlayRef = overlay
    gameRoot:AddChild(overlay)
end

-- ============================================================================
-- 主入口：葬礼规模选择弹窗
-- ============================================================================

---@type table modal 引用，供内部关闭使用
local modal

--- 显示葬礼规模选择弹窗
---@param gameRoot table  UI 根节点
---@param member table    逝者信息
---@param onComplete function  完成后回调
function FuneralModal.ShowDialog(gameRoot, member, onComplete)
    if not member then
        if onComplete then onComplete() end
        return
    end

    local s        = GameData.state
    local clanRank = s and s.clanRank or 1

    local memberName = member.name or "族人"
    local memberAge  = member.age  or 0
    local genderText = member.gender == "female" and "女" or "男"
    local ageText = memberAge <= 2
        and ("年仅" .. memberAge .. "岁")
        or  ("享年" .. memberAge .. "岁")

    -- ── 规模卡片 ──
    local scaleCards = {}

    for _, scale in ipairs(SCALES) do
        local locked  = clanRank < scale.minRank
        local silver, cloth, fame = CalcCost(scale, clanRank)
        local canAfford = scale.free or GameData.CanAfford(silver, 0, cloth, fame)

        -- 费用文字
        local costParts = {}
        if scale.free then
            costParts[#costParts + 1] = "免费"
        else
            if silver > 0 then costParts[#costParts + 1] = "银" .. silver end
            if cloth  > 0 then costParts[#costParts + 1] = "布" .. cloth  end
            if fame   > 0 then costParts[#costParts + 1] = "望-" .. fame  end
        end
        local costText = table.concat(costParts, " ")

        -- 奖励文字
        local rewardParts = { "声望+" .. scale.fameReward }
        if scale.fenziCoef then
            local fenzi = scale.fenziCoef * math.max(1, clanRank)
            rewardParts[#rewardParts + 1] = "份子钱银+" .. fenzi
        end
        local rewardText = table.concat(rewardParts, "  ")

        -- 广告标记
        local adTag = scale.adRequired and "  [看广告]" or ""

        local cardBg = locked
            and { 30, 25, 20, 180 }
            or  { 50, 40, 30, 210 }

        local nameColor = locked
            and { 120, 100, 80, 255 }
            or  scale.color

        -- 点击处理
        local scaleCopy = scale  -- 闭包捕获
        local silverCopy, clothCopy, fameCopy = silver, cloth, fame

        local card = UI.Panel {
            width = "100%", paddingTop = 6, paddingBottom = 6, paddingLeft = 10, paddingRight = 10,
            borderRadius = 6,
            backgroundColor = cardBg,
            borderWidth = 1,
            borderColor = locked and { 70, 60, 50, 150 } or scale.color,
            gap = 2,
            onTap = function(self)
                if locked then
                    AudioManager.Click()
                    return
                end
                if not canAfford then
                    AudioManager.Loss()
                    Toast.Show("资源不足：" .. costText)
                    return
                end
                AudioManager.Select()

                if scaleCopy.adRequired then
                    -- 广大祭葬：先提示看广告，再播放
                    modal:Close()
                    local fenziSilver = (scaleCopy.fenziCoef or 0) * math.max(1, clanRank)

                    -- 提示弹窗
                    local adHintModal
                    adHintModal = UI.Modal {
                        title = "广大祭葬",
                        size = "sm",
                        showCloseButton = false,
                        closeOnOverlay = false,
                        children = {
                            UI.Panel {
                                gap = 10, alignItems = "center", width = "100%",
                                children = {
                                    UI.Label {
                                        text = "观看广告后，将举办隆重的皇家祭葬仪式",
                                        fontSize = 14, fontColor = { 220, 200, 160, 255 },
                                        textAlign = "center", whiteSpace = "normal",
                                    },
                                    UI.Panel {
                                        width = "100%", padding = 10, borderRadius = 8,
                                        backgroundColor = { 40, 60, 40, 200 },
                                        borderWidth = 1, borderColor = { 80, 140, 80, 200 },
                                        gap = 4, alignItems = "center",
                                        children = {
                                            UI.Label {
                                                text = "观看广告 · 效果更佳",
                                                fontSize = 13, fontColor = { 120, 200, 120, 255 }, fontWeight = "bold",
                                            },
                                            UI.Label {
                                                text = "视频仪式 + 份子钱银" .. fenziSilver .. " + 声望+" .. scaleCopy.fameReward,
                                                fontSize = 12, fontColor = { 160, 220, 160, 255 },
                                            },
                                        },
                                    },
                                    UI.Panel {
                                        flexDirection = "row", gap = 8, width = "100%",
                                        children = {
                                            -- 取消
                                            UI.Panel {
                                                flex = 1, height = 38, borderRadius = 6,
                                                backgroundColor = { 50, 40, 30, 200 },
                                                borderWidth = 1, borderColor = Theme.BORDER_LIGHT,
                                                justifyContent = "center", alignItems = "center",
                                                onTap = function()
                                                    AudioManager.Click()
                                                    adHintModal:Close()
                                                    if onComplete then onComplete() end
                                                end,
                                                children = { UI.Label { text = "取消", fontSize = 14, fontColor = Theme.TEXT_MUTED } },
                                            },
                                            -- 看广告
                                            UI.Panel {
                                                flex = 2, height = 38, borderRadius = 6,
                                                backgroundColor = { 60, 140, 60, 255 },
                                                justifyContent = "center", alignItems = "center",
                                                onTap = function()
                                                    AudioManager.Select()
                                                    adHintModal:Close()
                                                    AdSystem.ShowRewardAd("funeral_grand", function()
                                                        -- 广告成功：播放视频仪式
                                                        PlayGrandCeremony(
                                                            gameRoot, member,
                                                            scaleCopy.fameReward, fenziSilver,
                                                            onComplete
                                                        )
                                                    end, function(msg)
                                                        -- 广告失败：提示用户，给基础声望
                                                        Toast.Show("广告暂时无法播放，已给予基础声望")
                                                        GameData.state.fame = (GameData.state.fame or 0) + 5
                                                        if onComplete then onComplete() end
                                                    end)
                                                end,
                                                children = { UI.Label { text = "观看广告", fontSize = 14, fontColor = { 255, 255, 255, 255 }, fontWeight = "bold" } },
                                            },
                                        },
                                    },
                                },
                            },
                        },
                    }
                    adHintModal:Open(gameRoot)
                else
                    -- 普通葬礼：扣费 → 播放简单仪式
                    GameData.SpendResources(silverCopy, 0, clothCopy, fameCopy)
                    modal:Close()
                    PlaySimpleCeremony(
                        gameRoot, member,
                        scaleCopy.name, scaleCopy.fameReward,
                        onComplete
                    )
                end
            end,
            children = {
                -- 第一行：规模名 + 锁定标签
                UI.Panel {
                    flexDirection = "row", justifyContent = "space-between",
                    alignItems = "center", width = "100%",
                    children = {
                        UI.Label {
                            text = scaleCopy.name .. adTag,
                            fontSize = 15, fontColor = nameColor, fontWeight = "bold",
                        },
                        locked and UI.Label {
                            text = "需" .. (CLAN_RANKS[scale.minRank] or "望族") .. "以上",
                            fontSize = 10, fontColor = { 140, 110, 80, 255 },
                            backgroundColor = { 60, 50, 40, 200 },
                            paddingLeft = 4, paddingRight = 4,
                            paddingTop = 2, paddingBottom = 2, borderRadius = 4,
                        } or UI.Panel { width = 1, height = 1 },
                    },
                },
                -- 第二行：费用 + 箭头 + 奖励（合并为一行）
                UI.Panel {
                    flexDirection = "row", gap = 6, alignItems = "center", flexWrap = "wrap",
                    children = {
                        UI.Label {
                            text = costText,
                            fontSize = 12,
                            fontColor = (not scale.free and not canAfford)
                                and { 210, 80, 80, 255 }
                                or  { 130, 200, 130, 255 },
                        },
                        UI.Label { text = "·", fontSize = 12, fontColor = { 100, 90, 70, 200 } },
                        UI.Label {
                            text = rewardText,
                            fontSize = 12, fontColor = { 212, 175, 55, 255 },
                        },
                    },
                },
            },
        }
        scaleCards[#scaleCards + 1] = card
    end

    -- 暂不举办：放入 scaleCards 末尾，确保 table.unpack 在 children 最后
    scaleCards[#scaleCards + 1] = UI.Panel {
        width = "100%", paddingTop = 6, paddingBottom = 6, borderRadius = 6,
        backgroundColor = { 35, 28, 22, 180 },
        borderWidth = 1, borderColor = { 70, 60, 50, 120 },
        justifyContent = "center", alignItems = "center",
        onTap = function()
            AudioManager.Click()
            modal:Close()
            if onComplete then onComplete() end
        end,
        children = {
            UI.Label { text = "暂不举办", fontSize = 12, fontColor = Theme.TEXT_MUTED },
        },
    }

    modal = UI.Modal {
        title = "族人讣告 · 葬礼",
        size  = "sm",
        showCloseButton = false,
        closeOnOverlay  = false,
        children = {
            -- 逝者简介（紧凑单行）
            UI.Panel {
                width = "100%", paddingTop = 8, paddingBottom = 8, paddingLeft = 10, paddingRight = 10,
                borderRadius = 8,
                backgroundColor = { 28, 22, 18, 200 },
                borderWidth = 1, borderColor = { 80, 70, 50, 150 },
                flexDirection = "row", gap = 8, alignItems = "center",
                children = {
                    UI.Label { text = "殁", fontSize = 20, fontColor = { 160, 140, 100, 255 } },
                    UI.Label {
                        text = memberName,
                        fontSize = 15, fontColor = { 230, 210, 165, 255 }, fontWeight = "bold",
                    },
                    UI.Label {
                        text = genderText .. " " .. ageText,
                        fontSize = 12, fontColor = Theme.TEXT_MUTED,
                    },
                },
            },
            UI.Label {
                text = "请选择葬礼规模",
                fontSize = 12, fontColor = Theme.TEXT_MUTED, textAlign = "center",
            },
            -- 规模卡片（3个，紧凑排列无需滚动）
            table.unpack(scaleCards),
        },
    }
    modal:Open(gameRoot)
end

return FuneralModal
