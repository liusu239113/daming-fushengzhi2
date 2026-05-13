-- ============================================================================
-- 大明浮生志2 - 音频管理器
-- 管理 BGM 播放（含时代切换）和 SFX 触发
-- ============================================================================

local EraSystem = require("Data.EraSystem")

local AudioManager = {}

-- ============================================================================
-- 音频资源路径
-- ============================================================================

local BGM = {
    MENU     = "audio/music_1778466513767.ogg",      -- 主菜单 BGM（古筝琵琶宫廷雅乐）
    PEACEFUL = "audio/music_1778466591795.ogg",      -- 太平年代 BGM（古筝田园风）
    CRISIS   = "audio/music_1778466712651.ogg",      -- 危机年代 BGM（二胡悲壮战鼓）
    BATTLE   = "audio/music_1778523134060.ogg",      -- 战斗 BGM（激昂战鼓+二胡琵琶史诗战斗）
}

local SFX = {
    UI_CLICK       = "audio/sfx/ui_click.ogg",
    UI_SELECT      = "audio/sfx/ui_select.ogg",
    UI_TAB_SWITCH  = "audio/sfx/ui_tab_switch.ogg",
    UI_BACK        = "audio/sfx/ui_back.ogg",
    EVENT_DISASTER = "audio/sfx/event_disaster.ogg",
    EVENT_CELEBRATE= "audio/sfx/event_celebration.ogg",
    EVENT_BATTLE   = "audio/sfx/event_battle.ogg",
    EVENT_EXAM     = "audio/sfx/event_exam_pass.ogg",
    EVENT_DEATH    = "audio/sfx/event_death.ogg",
    RESOURCE_GAIN  = "audio/sfx/resource_gain.ogg",
    RESOURCE_LOSS  = "audio/sfx/resource_loss.ogg",
    MONTH_ADVANCE  = "audio/sfx/month_advance.ogg",
    BATTLE_HIT     = "audio/sfx/battle_hit.ogg",
    BATTLE_SWING   = "audio/sfx/battle_swing.ogg",
    BATTLE_ARROW   = "audio/sfx/battle_arrow_shoot.ogg",
}

-- ============================================================================
-- 内部状态
-- ============================================================================

---@type Scene
local scene_ = nil
---@type Node
local bgmNode_ = nil
---@type SoundSource
local bgmSource_ = nil
---@type Node
local sfxNode_ = nil
local currentBgmPath_ = ""
local bgmVolume_ = 0.5
local sfxVolume_ = 0.7
local initialized_ = false

-- ============================================================================
-- 初始化
-- ============================================================================

function AudioManager.Init()
    if initialized_ then return end

    -- 创建一个独立的 scene 用于音频（不干扰游戏 scene）
    scene_ = Scene()
    scene_:CreateComponent("Octree")

    -- BGM 节点
    bgmNode_ = scene_:CreateChild("BGM")
    bgmSource_ = bgmNode_:CreateComponent("SoundSource")
    bgmSource_.soundType = "Music"
    bgmSource_.gain = bgmVolume_

    -- SFX 节点
    sfxNode_ = scene_:CreateChild("SFX")

    -- 设置全局音量
    audio:SetMasterGain("Music", bgmVolume_)
    audio:SetMasterGain("Effect", sfxVolume_)

    initialized_ = true
    print("[AudioManager] 音频系统初始化完成")
end

-- ============================================================================
-- BGM 控制
-- ============================================================================

function AudioManager.PlayBGM(bgmKey)
    if not initialized_ then AudioManager.Init() end

    local path = BGM[bgmKey]
    if not path then
        print("[AudioManager] 未知 BGM: " .. tostring(bgmKey))
        return
    end

    -- 避免重复播放同一首
    if currentBgmPath_ == path and bgmSource_:IsPlaying() then
        return
    end

    local sound = cache:GetResource("Sound", path)
    if not sound then
        print("[AudioManager] 无法加载 BGM: " .. path)
        return
    end

    sound.looped = true
    bgmSource_:Play(sound)
    bgmSource_.gain = bgmVolume_
    currentBgmPath_ = path
    print("[AudioManager] 播放 BGM: " .. bgmKey)
end

function AudioManager.StopBGM()
    if bgmSource_ then
        bgmSource_:Stop()
        currentBgmPath_ = ""
    end
end

--- 根据游戏年份自动选择 BGM（根据年代分期切换）
function AudioManager.UpdateGameBGM(year)
    if not initialized_ then return end
    local bgmType = EraSystem.GetBGMType(year)
    if bgmType == "crisis" then
        AudioManager.PlayBGM("CRISIS")
    else
        AudioManager.PlayBGM("PEACEFUL")
    end
end

-- ============================================================================
-- SFX 控制
-- ============================================================================

function AudioManager.PlaySFX(sfxKey)
    if not initialized_ then AudioManager.Init() end

    local path = SFX[sfxKey]
    if not path then
        print("[AudioManager] 未知 SFX: " .. tostring(sfxKey))
        return
    end

    local sound = cache:GetResource("Sound", path)
    if not sound then
        print("[AudioManager] 无法加载 SFX: " .. path)
        return
    end

    -- 每次创建新 SoundSource 播放音效，播放完自动移除
    local source = sfxNode_:CreateComponent("SoundSource")
    source.soundType = "Effect"
    source.gain = sfxVolume_
    source.autoRemoveMode = REMOVE_COMPONENT
    source:Play(sound)
end

-- ============================================================================
-- 快捷方法
-- ============================================================================

function AudioManager.Click()       AudioManager.PlaySFX("UI_CLICK") end
function AudioManager.Select()      AudioManager.PlaySFX("UI_SELECT") end
function AudioManager.TabSwitch()   AudioManager.PlaySFX("UI_TAB_SWITCH") end
function AudioManager.Back()        AudioManager.PlaySFX("UI_BACK") end
function AudioManager.MonthTick()   AudioManager.PlaySFX("MONTH_ADVANCE") end
function AudioManager.Gain()        AudioManager.PlaySFX("RESOURCE_GAIN") end
function AudioManager.Loss()        AudioManager.PlaySFX("RESOURCE_LOSS") end
function AudioManager.Disaster()    AudioManager.PlaySFX("EVENT_DISASTER") end
function AudioManager.Celebrate()   AudioManager.PlaySFX("EVENT_CELEBRATE") end
function AudioManager.Battle()      AudioManager.PlaySFX("EVENT_BATTLE") end
function AudioManager.ExamPass()    AudioManager.PlaySFX("EVENT_EXAM") end
function AudioManager.Death()       AudioManager.PlaySFX("EVENT_DEATH") end
function AudioManager.BattleHit()   AudioManager.PlaySFX("BATTLE_HIT") end
function AudioManager.BattleSwing() AudioManager.PlaySFX("BATTLE_SWING") end
function AudioManager.BattleArrow() AudioManager.PlaySFX("BATTLE_ARROW") end

-- ============================================================================
-- 音量控制
-- ============================================================================

function AudioManager.SetBGMVolume(vol)
    bgmVolume_ = math.max(0, math.min(1, vol))
    audio:SetMasterGain("Music", bgmVolume_)
    if bgmSource_ then
        bgmSource_.gain = bgmVolume_
    end
end

function AudioManager.SetSFXVolume(vol)
    sfxVolume_ = math.max(0, math.min(1, vol))
    audio:SetMasterGain("Effect", sfxVolume_)
end

function AudioManager.GetBGMVolume()
    return bgmVolume_
end

function AudioManager.GetSFXVolume()
    return sfxVolume_
end

return AudioManager
