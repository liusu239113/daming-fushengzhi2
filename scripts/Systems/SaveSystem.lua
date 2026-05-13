-- ============================================================================
-- 大明浮生志2 - 存档系统（自动存档 + 手动存档双槽位）
-- ============================================================================

local GameData = require("Data.GameData")

local SaveSystem = {}

-- 当前存档版本（每次添加新字段时递增）
SaveSystem.CURRENT_VERSION = 2

-- 存档槽位定义
SaveSystem.SLOT_AUTO   = "auto"    -- 自动存档
SaveSystem.SLOT_MANUAL = "manual"  -- 手动存档

-- 槽位显示名称
SaveSystem.SLOT_NAMES = {
    auto   = "自动存档",
    manual = "手动存档",
}

--- 获取槽位文件路径
local function GetSlotPath(slotId)
    return "saves/" .. slotId .. ".json"
end

--- 保存到指定槽位
function SaveSystem.Save(slotId)
    slotId = slotId or SaveSystem.SLOT_AUTO
    local s = GameData.state
    if not s then return false end

    fileSystem:CreateDir("saves")

    local saveData = {
        version = SaveSystem.CURRENT_VERSION,
        timestamp = os.time(),
        state = s,
    }

    local path = GetSlotPath(slotId)
    local file = File(path, FILE_WRITE)
    if file:IsOpen() then
        file:WriteString(cjson.encode(saveData))
        file:Close()
        print("[SaveSystem] Saved to " .. slotId .. " (" .. (SaveSystem.SLOT_NAMES[slotId] or slotId) .. ", v" .. SaveSystem.CURRENT_VERSION .. ")")
        return true
    end
    return false
end

--- 自动存档（快捷方法）
function SaveSystem.AutoSave()
    return SaveSystem.Save(SaveSystem.SLOT_AUTO)
end

--- 手动存档（快捷方法）
function SaveSystem.ManualSave()
    return SaveSystem.Save(SaveSystem.SLOT_MANUAL)
end

--- 存档迁移：将旧版存档的 state 补齐缺失字段
function SaveSystem.MigrateState(state, fromVersion)
    local defaults = GameData.GetDefaultState()
    if not defaults then return state end

    for key, defaultVal in pairs(defaults) do
        if state[key] == nil then
            if type(defaultVal) == "table" then
                state[key] = SaveSystem.DeepCopy(defaultVal)
            else
                state[key] = defaultVal
            end
            print("[SaveSystem] Migrated missing field: " .. key)
        end
    end

    return state
end

--- 深拷贝工具
function SaveSystem.DeepCopy(orig)
    if type(orig) ~= "table" then return orig end
    local copy = {}
    for k, v in pairs(orig) do
        copy[k] = SaveSystem.DeepCopy(v)
    end
    return copy
end

--- 加载指定槽位
function SaveSystem.Load(slotId)
    slotId = slotId or SaveSystem.SLOT_AUTO
    local path = GetSlotPath(slotId)

    if not fileSystem:FileExists(path) then
        print("[SaveSystem] No save file found: " .. path)
        return false
    end

    local file = File(path, FILE_READ)
    if file:IsOpen() then
        local content = file:ReadString()
        file:Close()
        local ok, saveData = pcall(cjson.decode, content)
        if ok and saveData and saveData.state then
            local savedVersion = saveData.version or 1

            if savedVersion < SaveSystem.CURRENT_VERSION then
                print("[SaveSystem] Migrating save from v" .. savedVersion .. " to v" .. SaveSystem.CURRENT_VERSION)
                saveData.state = SaveSystem.MigrateState(saveData.state, savedVersion)
            end

            GameData.state = saveData.state
            print("[SaveSystem] Loaded from " .. slotId .. " (" .. (SaveSystem.SLOT_NAMES[slotId] or slotId) .. ", v" .. savedVersion .. ")")
            return true
        else
            print("[SaveSystem] Failed to parse save file")
        end
    end
    return false
end

--- 检查指定槽位是否有存档
function SaveSystem.HasSave(slotId)
    return fileSystem:FileExists(GetSlotPath(slotId))
end

--- 检查是否有任何存档（用于"继续游戏"按钮是否可用）
function SaveSystem.HasAnySave()
    return SaveSystem.HasSave(SaveSystem.SLOT_AUTO) or SaveSystem.HasSave(SaveSystem.SLOT_MANUAL)
end

--- 获取指定槽位的存档信息（含时间戳）
function SaveSystem.GetSaveInfo(slotId)
    local path = GetSlotPath(slotId)
    if not fileSystem:FileExists(path) then return nil end

    local file = File(path, FILE_READ)
    if file:IsOpen() then
        local content = file:ReadString()
        file:Close()
        local ok, saveData = pcall(cjson.decode, content)
        if ok and saveData and saveData.state then
            local s = saveData.state
            return {
                slotId = slotId,
                slotName = SaveSystem.SLOT_NAMES[slotId] or slotId,
                timestamp = saveData.timestamp or 0,
                year = s.year,
                month = s.month,
                clanName = s.clanName,
                silver = s.silver,
            }
        end
    end
    return nil
end

--- 获取最新的存档槽位ID（用于"继续游戏"）
function SaveSystem.GetLatestSlot()
    local autoInfo = SaveSystem.GetSaveInfo(SaveSystem.SLOT_AUTO)
    local manualInfo = SaveSystem.GetSaveInfo(SaveSystem.SLOT_MANUAL)

    if autoInfo and manualInfo then
        -- 两者都有，返回时间戳更新的
        if autoInfo.timestamp >= manualInfo.timestamp then
            return SaveSystem.SLOT_AUTO
        else
            return SaveSystem.SLOT_MANUAL
        end
    elseif autoInfo then
        return SaveSystem.SLOT_AUTO
    elseif manualInfo then
        return SaveSystem.SLOT_MANUAL
    end
    return nil
end

--- 加载最新存档（用于"继续游戏"）
function SaveSystem.LoadLatest()
    local slot = SaveSystem.GetLatestSlot()
    if slot then
        return SaveSystem.Load(slot), slot
    end
    return false, nil
end

--- 删除指定槽位
function SaveSystem.DeleteSave(slotId)
    local path = GetSlotPath(slotId)
    if fileSystem:FileExists(path) then
        fileSystem:Delete(path)
        return true
    end
    return false
end

--- 删除所有存档
function SaveSystem.DeleteAllSaves()
    SaveSystem.DeleteSave(SaveSystem.SLOT_AUTO)
    SaveSystem.DeleteSave(SaveSystem.SLOT_MANUAL)
    -- 兼容：清理旧版 slot1/2/3
    for i = 1, 3 do
        local oldPath = "saves/slot" .. i .. ".json"
        if fileSystem:FileExists(oldPath) then
            fileSystem:Delete(oldPath)
        end
    end
end

--- 迁移旧版存档（slot1→auto，首次运行时调用）
function SaveSystem.MigrateOldSlots()
    local oldPath = "saves/slot1.json"
    local newAutoPath = GetSlotPath(SaveSystem.SLOT_AUTO)
    if fileSystem:FileExists(oldPath) and not fileSystem:FileExists(newAutoPath) then
        -- 读取旧存档，写入新自动存档
        local file = File(oldPath, FILE_READ)
        if file:IsOpen() then
            local content = file:ReadString()
            file:Close()
            local wf = File(newAutoPath, FILE_WRITE)
            if wf:IsOpen() then
                wf:WriteString(content)
                wf:Close()
                print("[SaveSystem] Migrated old slot1 → auto")
            end
        end
    end
end

return SaveSystem
