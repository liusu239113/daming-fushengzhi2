-- ============================================================================
-- 大明浮生志2 - 装备系统
-- 管理装备穿戴、卸下、购买、传承
-- ============================================================================

local GameData = require("Data.GameData")

local EquipmentSystem = {}

-- ============================================================================
-- 装备操作
-- ============================================================================

--- 给族人装备物品
---@param memberId number
---@param equipId string
---@return boolean success
---@return string message
function EquipmentSystem.Equip(memberId, equipId)
    local member = GameData.GetMember(memberId)
    if not member or not member.alive then return false, "族人不存在或已故" end

    local equip = GameData.GetEquipment(equipId)
    if not equip then return false, "装备不存在" end

    -- 检查库存中是否有该装备
    local s = GameData.state
    local found = false
    for i, item in ipairs(s.inventory) do
        if item.itemId == equipId and item.count > 0 then
            item.count = item.count - 1
            if item.count <= 0 then
                table.remove(s.inventory, i)
            end
            found = true
            break
        end
    end
    if not found then return false, "库存中没有此装备" end

    -- 初始化装备槽
    if not member.equipment then member.equipment = {} end

    -- 若该槽位已有装备,先卸下放回库存
    local slot = equip.slot
    if member.equipment[slot] then
        EquipmentSystem.AddToInventory(member.equipment[slot])
    end

    -- 穿上新装备
    member.equipment[slot] = equipId

    -- 应用属性加成
    EquipmentSystem.RecalcStats(member)

    GameData.AddLog(member.name .. "装备了【" .. equip.name .. "】")
    return true, "装备成功"
end

--- 卸下某槽位的装备
---@param memberId number
---@param slot string "weapon"|"armor"|"accessory"
---@return boolean success
---@return string message
function EquipmentSystem.Unequip(memberId, slot)
    local member = GameData.GetMember(memberId)
    if not member or not member.alive then return false, "族人不存在或已故" end
    if not member.equipment or not member.equipment[slot] then
        return false, "该槽位无装备"
    end

    local equipId = member.equipment[slot]
    EquipmentSystem.AddToInventory(equipId)
    member.equipment[slot] = nil

    EquipmentSystem.RecalcStats(member)

    local equip = GameData.GetEquipment(equipId)
    local name = equip and equip.name or equipId
    GameData.AddLog(member.name .. "卸下了【" .. name .. "】")
    return true, "已卸下"
end

--- 从集市购买装备（直接进库房）
---@param equipId string
---@return boolean success
---@return string message
function EquipmentSystem.BuyEquipment(equipId)
    local equip = GameData.GetEquipment(equipId)
    if not equip then return false, "装备不存在" end

    local s = GameData.state
    if s.silver < equip.cost then
        return false, "银两不足（需" .. equip.cost .. "两）"
    end

    s.silver = s.silver - equip.cost
    EquipmentSystem.AddToInventory(equipId)

    GameData.AddLog("购入装备【" .. equip.name .. "】，花费" .. equip.cost .. "两")
    return true, "购入" .. equip.name
end

--- 添加装备到库房
---@param equipId string
function EquipmentSystem.AddToInventory(equipId)
    local s = GameData.state
    for _, item in ipairs(s.inventory) do
        if item.itemId == equipId then
            item.count = item.count + 1
            return
        end
    end
    s.inventory[#s.inventory + 1] = { itemId = equipId, count = 1 }
end

--- 检查库房中某装备的数量
---@param equipId string
---@return number
function EquipmentSystem.GetInventoryCount(equipId)
    local s = GameData.state
    if not s then return 0 end
    for _, item in ipairs(s.inventory) do
        if item.itemId == equipId then return item.count end
    end
    return 0
end

--- 重新计算族人装备加成（存储到 member.equipBonus）
---@param member table
function EquipmentSystem.RecalcStats(member)
    local bonus = { martial = 0, study = 0, health = 0 }
    if member.equipment then
        for _, slot in ipairs(GameData.EQUIPMENT_SLOTS) do
            local eid = member.equipment[slot]
            if eid then
                local equip = GameData.GetEquipment(eid)
                if equip then
                    local rarity = GameData.GetRarityConfig(equip.rarity)
                    local mul = rarity and rarity.multiplier or 1.0
                    bonus.martial = bonus.martial + math.floor((equip.martial or 0) * mul)
                    bonus.study = bonus.study + math.floor((equip.study or 0) * mul)
                    bonus.health = bonus.health + math.floor((equip.health or 0) * mul)
                end
            end
        end
    end
    member.equipBonus = bonus
end

--- 获取族人的装备总加成
---@param member table
---@return table {martial, study, health}
function EquipmentSystem.GetBonus(member)
    if not member.equipBonus then
        EquipmentSystem.RecalcStats(member)
    end
    return member.equipBonus or { martial = 0, study = 0, health = 0 }
end

--- 获取族人某槽位当前装备信息
---@param member table
---@param slot string
---@return table|nil equipConfig
function EquipmentSystem.GetEquipped(member, slot)
    if not member.equipment then return nil end
    local eid = member.equipment[slot]
    if not eid then return nil end
    return GameData.GetEquipment(eid)
end

-- ============================================================================
-- 传世装备继承
-- ============================================================================

--- 处理族人去世时的装备继承
---@param member table 去世的族人
function EquipmentSystem.HandleInheritance(member)
    if not member.equipment then return end
    local s = GameData.state

    for _, slot in ipairs(GameData.EQUIPMENT_SLOTS) do
        local eid = member.equipment[slot]
        if eid then
            local equip = GameData.GetEquipment(eid)
            if equip and equip.heirloom then
                -- 传世装备: 优先传给子嗣，否则传给同辈
                local heir = EquipmentSystem.FindHeir(member)
                if heir then
                    if not heir.equipment then heir.equipment = {} end
                    -- 如果继承人该槽位已有装备，先放回库房
                    if heir.equipment[slot] then
                        EquipmentSystem.AddToInventory(heir.equipment[slot])
                    end
                    heir.equipment[slot] = eid
                    EquipmentSystem.RecalcStats(heir)
                    GameData.AddLog(member.name .. "的传世装备【" .. equip.name .. "】传给了" .. heir.name)
                else
                    -- 无继承人，放回库房
                    EquipmentSystem.AddToInventory(eid)
                    GameData.AddLog(member.name .. "的传世装备【" .. equip.name .. "】收入库房")
                end
            else
                -- 非传世装备: 放回库房
                EquipmentSystem.AddToInventory(eid)
            end
            member.equipment[slot] = nil
        end
    end
end

--- 寻找最佳继承人（优先子嗣，其次同辈年轻人）
---@param member table
---@return table|nil
function EquipmentSystem.FindHeir(member)
    local alive = GameData.GetAliveMembers()
    -- 优先寻找子嗣
    for _, m in ipairs(alive) do
        if m.parentId == member.id and m.age >= 12 then
            return m
        end
    end
    -- 其次找同代最年轻的成年人
    local best = nil
    for _, m in ipairs(alive) do
        if m.id ~= member.id and m.age >= 12 then
            if not best or m.age < best.age then
                best = m
            end
        end
    end
    return best
end

return EquipmentSystem
