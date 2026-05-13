-- ============================================================================
-- 大明浮生志2 - 3D讨伐战斗场景
-- 自动战斗：双方单位在战场上移动、寻敌、攻击、死亡
-- 战斗结束后结算奖励/伤亡，回写 GameData
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")
local RivalClans = require("Data.RivalClans")
local AudioManager = require("Systems.AudioManager")
local SaveSystem = require("Systems.SaveSystem")
local AdSystem = require("Systems.AdSystem")
local Toast = require("Systems.Toast")

local CampaignRegions = require("Data.CampaignRegions")

local BattleScene = {}

-- ============================================================================
-- 常量配置
-- ============================================================================

local CONFIG = {
    -- 战场尺寸（米）
    FIELD_W = 16,          -- X 方向宽度
    FIELD_D = 24,          -- Z 方向深度
    -- 单位属性
    UNIT_SCALE = 1.0,      -- 统一缩放
    MOVE_SPEED = 2.5,      -- 基础移动速度（米/秒）
    ATTACK_RANGE = 1.8,    -- 攻击范围（米）
    ATTACK_CD = 1.2,       -- 攻击冷却（秒）
    DIE_ANIM_TIME = 2.5,   -- 死亡动画时长（秒）
    -- 相机
    CAM_HEIGHT = 20,       -- 俯视高度
    CAM_PITCH = 70,        -- 俯视角度（正值=朝下看）
    CAM_Z_OFFSET = -5,     -- Z偏移（稍后方便看到全场）
    -- 战斗速度
    BATTLE_SPEED = 1.0,    -- 初始战斗速度倍率
    -- 阅兵入场
    PARADE_SPEED = 3.5,    -- 阅兵行军速度（米/秒）
    PARADE_HOLD_TIME = 1.2, -- 列阵完毕后停留时间（秒）
    -- 弓兵远程攻击
    ARCHER_RANGE = 8.0,        -- 弓兵攻击范围（米）
    ARROW_SPEED = 8.0,         -- 箭矢飞行速度（米/秒）
    ARROW_SCALE = 0.3,         -- 箭矢缩放
    -- 阵型布局
    FORMATION_SPACING_X = 2.5,  -- 列间距（米）
    FORMATION_ROW_GAP = 3.0,    -- 行间距（米）
}

-- ============================================================================
-- 模块状态
-- ============================================================================

---@type Scene
local scene_ = nil
---@type Node
local cameraNode_ = nil
local battleUnits_ = {}       -- 所有战斗单位 { node, data, fsm, state, ... }
local battleState_ = "deploy" -- "deploy"|"fighting"|"victory"|"defeat"
local battleTime_ = 0         -- 战斗已进行时间
local battleSpeed_ = CONFIG.BATTLE_SPEED
local uiRoot_ = nil
local onBattleEnd_ = nil      -- 战斗结束回调
local rivalData_ = nil        -- 敌族数据
local deployedIds_ = nil      -- 出战族人ID
local playerSoldierCount_ = 0 -- 我方士兵数量
local paradeHoldTimer_ = 0    -- 阅兵列阵后停留计时
local paradeAllReached_ = false -- 所有单位是否到达阵位
local archerRatio_ = 0         -- 弓兵比例 (0~1)，0=全步兵，1=全弓兵
local arrows_ = {}             -- 飞行中的箭矢 { node, dir, speed, damage, target, side }
local onSettleOverride_ = nil  -- 自定义结算回调（事件战斗用）function(result, rivalData, deployedIds) -> report
local deployInfantry_ = 0      -- 本次出征步兵数
local deployArchers_ = 0       -- 本次出征弓兵数
local trainingLevel_ = 0       -- 本次出征训练等级
local regionId_ = nil          -- 本次出征区域ID（征伐战役用）

-- ============================================================================
-- 场景创建
-- ============================================================================

local function CreateBattleScene()
    scene_ = Scene()
    scene_:CreateComponent("Octree")
    scene_:CreateComponent("DebugRenderer")

    -- 使用引擎内置白天光照组（包含方向光+环境光+天空盒）
    local lightGroupFile = cache:GetResource("XMLFile", "LightGroup/Daytime.xml")
    local lightGroup = scene_:CreateChild("LightGroup")
    lightGroup:LoadXML(lightGroupFile:GetRoot())
end

local function SetupCamera()
    cameraNode_ = scene_:CreateChild("BattleCamera")
    cameraNode_.position = Vector3(0, CONFIG.CAM_HEIGHT, CONFIG.CAM_Z_OFFSET)
    cameraNode_.rotation = Quaternion(CONFIG.CAM_PITCH, 0, 0)

    local camera = cameraNode_:CreateComponent("Camera")
    camera.nearClip = 0.5
    camera.farClip = 200.0
    camera.fov = 60.0

    local viewport = Viewport:new(scene_, camera)
    renderer:SetViewport(0, viewport)
    renderer.hdrRendering = true

    local cp = cameraNode_.position
    log:Write(LOG_INFO, string.format("[Battle] 相机 pos=(%.1f,%.1f,%.1f) pitch=%d fov=%.0f",
        cp.x, cp.y, cp.z, CONFIG.CAM_PITCH, camera.fov))
end

local function CreateGround()
    -- 战场地面：草地纹理瓦片
    local groundNode = scene_:CreateChild("Ground")
    groundNode.position = Vector3(0, -0.01, 0)
    groundNode.scale = Vector3(CONFIG.FIELD_W + 8, 1, CONFIG.FIELD_D + 8)

    local model = groundNode:CreateComponent("StaticModel")
    model:SetModel(cache:GetResource("Model", "Models/Plane.mdl"))

    local grassTex = cache:GetResource("Texture2D", "Textures/grass_tile.png")
    if grassTex then
        grassTex:SetAddressMode(COORD_U, ADDRESS_WRAP)
        grassTex:SetAddressMode(COORD_V, ADDRESS_WRAP)
        grassTex:SetFilterMode(FILTER_TRILINEAR)
    end

    local mat = Material:new()
    mat:SetTechnique(0, cache:GetResource("Technique", "Techniques/Diff.xml"))
    mat:SetTexture(TU_DIFFUSE, grassTex)
    mat:SetShaderParameter("MatDiffColor", Variant(Color(1.0, 1.0, 1.0, 1.0)))
    -- UV 平铺：地面尺寸约 24x32 米，每 3 米一块瓦片
    local tilesX = (CONFIG.FIELD_W + 8) / 3.0
    local tilesZ = (CONFIG.FIELD_D + 8) / 3.0
    mat:SetUVTransform(Vector2(0, 0), 0, Vector2(tilesX, tilesZ))
    model:SetMaterial(mat)

    -- 云朵装饰（白色扁椭球体漂浮在天空中）
    local cloudMat = Material:new()
    cloudMat:SetTechnique(0, cache:GetResource("Technique", "Techniques/PBR/PBRNoTextureAlpha.xml"))
    cloudMat:SetShaderParameter("MatDiffColor", Variant(Color(1.0, 1.0, 1.0, 0.85)))
    cloudMat:SetShaderParameter("Roughness", Variant(1.0))
    cloudMat:SetShaderParameter("Metallic", Variant(0.0))

    local cloudPositions = {
        { x = -6,  y = 18, z = 12,  sx = 4.0, sy = 1.2, sz = 2.5 },
        { x = 5,   y = 20, z = 8,   sx = 5.0, sy = 1.0, sz = 3.0 },
        { x = -3,  y = 22, z = -5,  sx = 3.5, sy = 0.8, sz = 2.0 },
        { x = 8,   y = 19, z = -8,  sx = 4.5, sy = 1.1, sz = 2.8 },
        { x = -8,  y = 21, z = 3,   sx = 3.0, sy = 0.9, sz = 2.2 },
        { x = 0,   y = 23, z = 15,  sx = 6.0, sy = 1.0, sz = 3.5 },
        { x = 10,  y = 17, z = 5,   sx = 3.5, sy = 1.0, sz = 2.0 },
    }
    for i, cp in ipairs(cloudPositions) do
        local cloudNode = scene_:CreateChild("Cloud_" .. i)
        cloudNode.position = Vector3(cp.x, cp.y, cp.z)
        cloudNode.scale = Vector3(cp.sx, cp.sy, cp.sz)
        local cloudModel = cloudNode:CreateComponent("StaticModel")
        cloudModel:SetModel(cache:GetResource("Model", "Models/Sphere.mdl"))
        cloudModel:SetMaterial(cloudMat)
        cloudModel.castShadows = false
    end

    -- 战场边界标识（左右两侧旗帜竿）
    for _, xSign in ipairs({-1, 1}) do
        for zIdx = 1, 3 do
            local poleNode = scene_:CreateChild("Pole")
            poleNode.position = Vector3(xSign * (CONFIG.FIELD_W / 2 + 1), 1.5, -CONFIG.FIELD_D / 2 + (zIdx - 1) * CONFIG.FIELD_D / 2)
            poleNode.scale = Vector3(0.1, 3, 0.1)
            local poleModel = poleNode:CreateComponent("StaticModel")
            poleModel:SetModel(cache:GetResource("Model", "Models/Cylinder.mdl"))
            local poleMat = Material:new()
            poleMat:SetTechnique(0, cache:GetResource("Technique", "Techniques/PBR/PBRNoTexture.xml"))
            poleMat:SetShaderParameter("MatDiffColor", Variant(Color(0.4, 0.25, 0.1, 1.0)))
            poleMat:SetShaderParameter("Roughness", Variant(0.7))
            poleMat:SetShaderParameter("Metallic", Variant(0.0))
            poleModel:SetMaterial(poleMat)
        end
    end
end

-- ============================================================================
-- 战斗单位创建
-- ============================================================================

--- 创建一个3D战斗单位
---@param battleData table 来自 RivalClans 的战斗单位数据
---@param spawnPos Vector3 出生位置
---@return table unit 单位对象
local function CreateBattleUnit(battleData, spawnPos)
    local unitNode = scene_:CreateChild(battleData.name)
    unitNode.position = spawnPos

    -- 根据类型选择模型（武将统一使用 warrior 模型 + FSM 动画）
    local modelPath
    local isBandit = (battleData.unitStyle == "bandit")
    if isBandit then
        modelPath = "Meshes/bandit.mdl"   -- 山匪/流寇统一使用山匪模型
    elseif battleData.isSoldier then
        modelPath = "Meshes/soldier.mdl"
    else
        modelPath = "Meshes/warrior.mdl"
    end

    -- 根据阵营设定朝向
    if battleData.side == "player" then
        unitNode.rotation = Quaternion(0, Vector3.UP)    -- 面朝+Z（前方）
    else
        unitNode.rotation = Quaternion(180, Vector3.UP)  -- 面朝-Z（前方）
    end

    -- 统一缩放（小兵1.0m、武将1.2m，山匪0.25m，统一到1.0左右）
    local scale = CONFIG.UNIT_SCALE
    if isBandit then
        -- 山匪模型0.25m高，缩放到约1.0m
        scale = CONFIG.UNIT_SCALE * 4.0
    elseif not battleData.isSoldier then
        -- 武将模型1.2m高，缩放到约1.0m以统一
        scale = CONFIG.UNIT_SCALE * 0.85
    end
    unitNode.scale = Vector3(scale, scale, scale)

    ---@type AnimationStateMachine
    local fsm = nil
    ---@type Node
    local weaponNode = nil

    -- === 所有单位统一使用骨骼动画 + FSM ===
    unitNode:GetOrCreateComponent("AnimationController")
    local animModel = unitNode:CreateComponent("AnimatedModel")
    animModel:SetModel(cache:GetResource("Model", modelPath))

    -- 设置材质
    if isBandit then
        local banditMat = cache:GetResource("Material", "Materials/bandit_00_tripo_material_61151d10-8ad9-4ff0-be50-04ceb4dbab33.xml")
        if banditMat then animModel:SetMaterial(0, banditMat) end
    elseif battleData.isSoldier then
        local soldierMat = cache:GetResource("Material", "Materials/soldier_00_tripo_material_df3f1f36-08e6-40ee-a0d2-6479c1d18f53.xml")
        if soldierMat then animModel:SetMaterial(0, soldierMat) end
    else
        local warriorMat = cache:GetResource("Material", "Materials/warrior_00_tripo_material_5200764e-8b99-4fe6-92e8-9871d0eeb408.xml")
        if warriorMat then animModel:SetMaterial(0, warriorMat) end
    end
    animModel.castShadows = true

    -- 武器挂载（山匪模型放大4倍，武器需要补偿缩放）
    local weaponScaleMul = isBandit and 0.25 or 1.0
    local skeleton = animModel:GetSkeleton()
    local isArcher = battleData.isArcher == true

    if isArcher then
        -- 弓兵：弓挂载到左手
        local lHandBone = skeleton:GetBone("L_Hand")
        if lHandBone and lHandBone.node then
            weaponNode = lHandBone.node:CreateChild("Weapon")
        else
            log:Write(LOG_WARNING, "[Battle] L_Hand bone not found for " .. battleData.name)
            weaponNode = unitNode:CreateChild("Weapon")
            weaponNode.position = Vector3(-0.3, 0.8, 0.1)
        end
        local weaponModel = weaponNode:CreateComponent("StaticModel")
        local bowMdl = cache:GetResource("Model", "Meshes/bow_weapon.mdl")
        if bowMdl then
            weaponModel:SetModel(bowMdl)
            -- 尝试加载弓的材质（文件名待模型导入后确定）
            local bowMat = cache:GetResource("Material", "Materials/bow_weapon_00_tripo_material_1e1584d6-15b6-4ad0-95c6-d577aa24176f.xml")
            if bowMat then weaponModel:SetMaterial(0, bowMat) end
        else
            -- 备用：使用 Cylinder 模拟弓形
            weaponModel:SetModel(cache:GetResource("Model", "Models/Cylinder.mdl"))
            local bowMat = Material:new()
            bowMat:SetTechnique(0, cache:GetResource("Technique", "Techniques/PBR/PBRNoTexture.xml"))
            bowMat:SetShaderParameter("MatDiffColor", Variant(Color(0.55, 0.35, 0.15, 1.0)))
            bowMat:SetShaderParameter("Roughness", Variant(0.7))
            bowMat:SetShaderParameter("Metallic", Variant(0.0))
            weaponModel:SetMaterial(bowMat)
        end
        local bs = 2.1 * weaponScaleMul
        weaponNode.scale = Vector3(bs, bs, bs)
        weaponNode.position = weaponNode.position + Vector3(0, 0.02 * weaponScaleMul, 0.05 * weaponScaleMul)
        weaponNode.rotation = Quaternion(90, Vector3.FORWARD)
    else
        -- 步兵：大刀挂载到右手
        local rHandBone = skeleton:GetBone("R_Hand")
        if rHandBone and rHandBone.node then
            weaponNode = rHandBone.node:CreateChild("Weapon")
        else
            log:Write(LOG_WARNING, "[Battle] R_Hand bone not found for " .. battleData.name)
            weaponNode = unitNode:CreateChild("Weapon")
            weaponNode.position = Vector3(0.3, 0.8, 0.1)
        end
        local weaponModel = weaponNode:CreateComponent("StaticModel")
        weaponModel:SetModel(cache:GetResource("Model", "Meshes/dao_weapon.mdl"))
        local daoMat = cache:GetResource("Material", "Materials/dao_weapon_00_tripo_material_e3ada42a-9aa6-4171-bdff-49c2d9dfbf22.xml")
        if daoMat then weaponModel:SetMaterial(0, daoMat) end
        local ds = 1.8 * weaponScaleMul
        weaponNode.scale = Vector3(ds, ds, ds)
        weaponNode.position = weaponNode.position + Vector3(0, 0.02 * weaponScaleMul, 0.05 * weaponScaleMul)
        weaponNode.rotation = Quaternion(90, Vector3.FORWARD)
    end

    -- 加载 FSM
    fsm = unitNode:GetOrCreateComponent("AnimationStateMachine")
    local fsmFile = cache:GetResource("JSONFile", "FSM/BattleUnit.fsm")
    fsm:LoadFromJSONFile(fsmFile)
    fsm:Start()

    local unit = {
        node = unitNode,
        data = battleData,
        fsm = fsm,
        weaponNode = weaponNode,
        isArcher = isArcher,       -- 是否弓兵
        -- AI 状态
        aiState = "idle",          -- "idle"|"move"|"attack"|"die"|"dead"
        target = nil,              -- 当前目标单位
        attackTimer = 0,           -- 攻击冷却计时
        dieTimer = 0,              -- 死亡动画计时
        moveSpeed = 0,             -- 当前移动速度
        hasDamaged = false,        -- 本次攻击是否已造成伤害
    }

    return unit
end

-- ============================================================================
-- 部署战斗单位
-- ============================================================================

local function DeployUnits()
    battleUnits_ = {}

    -- ============================
    -- 1. 收集双方单位数据
    -- ============================
    local playerUnitData = {}
    local enemyUnitData = {}

    -- 筑寨防御加成（提前计算，供将领和士兵共享）
    local fortCount = GameData.state.fortCount or 0
    local effectiveForts = math.min(fortCount, 5)
    local fortDefBonus = effectiveForts * 2
    local fortHpMult = 1.0 + effectiveForts * 0.05  -- 最高25%血量加成
    if effectiveForts > 0 then
        log:Write(LOG_INFO, string.format("[Battle] 筑寨加成: %d座寨子 → 防御+%d, 血量+%.0f%%",
            effectiveForts, fortDefBonus, (fortHpMult - 1) * 100))
    end

    -- 我方将领（家族成员）
    for _, memberId in ipairs(deployedIds_) do
        local member = GameData.GetMember(memberId)
        if member then
            local bData = RivalClans.MemberToBattleUnit(member)
            bData.isLeader = true
            bData.def = (bData.def or 0) + fortDefBonus
            bData.hp = math.floor((bData.hp or 100) * fortHpMult)
            bData.maxHp = bData.hp
            playerUnitData[#playerUnitData + 1] = bData
        end
    end

    -- 我方士兵（每100兵 = 1个单位）
    local pSoldierUnits = math.floor(playerSoldierCount_ / 100)
    pSoldierUnits = math.max(pSoldierUnits, 1)
    pSoldierUnits = math.min(pSoldierUnits, 30)  -- 提高上限到30支持大规模战斗
    -- 根据弓兵比例分配
    local archerCount = math.floor(pSoldierUnits * archerRatio_ + 0.5)
    local meleeCount = pSoldierUnits - archerCount

    -- 训练等级加成
    local tBonus = RivalClans.TRAINING_BONUS[trainingLevel_] or { 0, 0, 0 }
    local bonusAtk, bonusHp, bonusDef = tBonus[1], tBonus[2], tBonus[3]
    -- 合并筑寨防御加成到士兵加成
    bonusDef = bonusDef + fortDefBonus

    -- 先添加步兵（近战型：高攻高血）
    for i = 1, meleeCount do
        local bData = RivalClans.SoldierToBattleUnit(i, "player")
        bData.atk = bData.atk + bonusAtk
        bData.hp = math.floor((bData.hp + bonusHp) * fortHpMult)
        bData.maxHp = bData.hp
        bData.def = bData.def + bonusDef
        playerUnitData[#playerUnitData + 1] = bData
    end
    -- 再添加弓兵（远程型：攻击+50%加成，血量较低-20%）
    for i = 1, archerCount do
        local bData = RivalClans.SoldierToBattleUnit(meleeCount + i, "player")
        bData.isArcher = true
        bData.name = "弓兵第" .. i .. "营"
        bData.atk = math.floor((bData.atk + bonusAtk) * 1.5)
        bData.hp = math.floor((bData.hp + bonusHp) * 0.8 * fortHpMult)
        bData.maxHp = bData.hp
        bData.def = bData.def + bonusDef
        playerUnitData[#playerUnitData + 1] = bData
    end

    -- 敌方将领
    local enemyStyle = rivalData_.unitStyle  -- "bandit" 或 nil
    for _, rivalMember in ipairs(rivalData_.members) do
        local bd = RivalClans.RivalMemberToBattleUnit(rivalMember)
        bd.unitStyle = enemyStyle
        enemyUnitData[#enemyUnitData + 1] = bd
    end

    -- 敌方士兵
    local eSoldierUnits = math.floor(rivalData_.soldiers / 100)
    eSoldierUnits = math.max(eSoldierUnits, 1)
    eSoldierUnits = math.min(eSoldierUnits, 30)
    for i = 1, eSoldierUnits do
        local bd = RivalClans.SoldierToBattleUnit(i, "enemy")
        bd.unitStyle = enemyStyle
        enemyUnitData[#enemyUnitData + 1] = bd
    end

    -- ============================
    -- 2. 阵型布局（整齐排列 + 阅兵起始位置）
    -- ============================
    -- 战场 Z 范围：-FIELD_D/2 到 +FIELD_D/2（即 -12 到 +12）
    -- 我方（南侧）：将领在前（Z 大，靠近中线），士兵在后（Z 小）
    -- 敌方（北侧）：将领在前（Z 小，靠近中线），士兵在后（Z 大）
    -- 相机在南侧偏后方俯视，屏幕上方=北方，下方=南方

    local SX = CONFIG.FORMATION_SPACING_X
    local RG = CONFIG.FORMATION_ROW_GAP

    --- 为一个阵营布阵：分离将领和士兵，分别排成整齐行列
    ---@param unitDataList table 该阵营的所有单位数据
    ---@param side string "player"|"enemy"
    local function layoutFormation(unitDataList, side)
        -- 分离将领和士兵
        local leaders = {}
        local soldiers = {}
        for _, d in ipairs(unitDataList) do
            if d.isSoldier then
                soldiers[#soldiers + 1] = d
            else
                leaders[#leaders + 1] = d
            end
        end

        -- 确定 Z 坐标和方向
        local leaderZ, soldierZDir, startOffFieldZ
        if side == "player" then
            leaderZ = -4              -- 将领阵位：Z = -4（靠近中线 = 屏幕上方）
            soldierZDir = -1          -- 士兵在将领后方（更负的 Z = 屏幕下方）
            startOffFieldZ = -CONFIG.FIELD_D / 2 - 4   -- 入场起始：场外南侧
        else
            leaderZ = 4               -- 敌方将领：Z = +4（靠近中线 = 屏幕中上方）
            soldierZDir = 1           -- 敌方士兵在后方（更正的 Z = 屏幕更上方）
            startOffFieldZ = CONFIG.FIELD_D / 2 + 4    -- 入场起始：场外北侧
        end

        -- 排列将领（一行居中）
        local leaderCount = #leaders
        for i, d in ipairs(leaders) do
            local x = (i - (leaderCount + 1) / 2) * SX
            local targetPos = Vector3(x, 0, leaderZ)
            local startPos = Vector3(x, 0, startOffFieldZ)
            local unit = CreateBattleUnit(d, startPos)
            unit.targetPos = targetPos
            unit.reachedTarget = false
            battleUnits_[#battleUnits_ + 1] = unit
        end

        -- 排列士兵（每行最多5个，整齐居中）
        local maxCols = 5
        local soldierCols = math.min(#soldiers, maxCols)
        for i, d in ipairs(soldiers) do
            local row = math.floor((i - 1) / soldierCols)
            local col = (i - 1) % soldierCols
            -- 该行实际有几个单位（最后一行可能不满）
            local rowSize = math.min(#soldiers - row * soldierCols, soldierCols)
            local x = (col - (rowSize - 1) / 2) * SX
            local z = leaderZ + soldierZDir * (RG + row * RG)
            local targetPos = Vector3(x, 0, z)
            -- 起始位置：在场外，后排稍远（错开入场）
            local rowOffset = row * 1.5
            local sz
            if side == "player" then
                sz = startOffFieldZ - rowOffset
            else
                sz = startOffFieldZ + rowOffset
            end
            local startPos = Vector3(x, 0, sz)
            local unit = CreateBattleUnit(d, startPos)
            unit.targetPos = targetPos
            unit.reachedTarget = false
            battleUnits_[#battleUnits_ + 1] = unit
        end
    end

    layoutFormation(playerUnitData, "player")
    layoutFormation(enemyUnitData, "enemy")

    log:Write(LOG_INFO, "[Battle] 部署完成: 我方 " .. #playerUnitData .. " 单位, 敌方 " .. #enemyUnitData .. " 单位")
end

-- ============================================================================
-- 战斗AI逻辑
-- ============================================================================

--- 查找最近的敌方活着的单位
---@param unit table
---@return table|nil nearestEnemy
local function FindNearestEnemy(unit)
    local myPos = unit.node.position
    local mySide = unit.data.side
    local nearest = nil
    local nearestDist = 99999

    for _, other in ipairs(battleUnits_) do
        if other.data.side ~= mySide and other.aiState ~= "die" and other.aiState ~= "dead" then
            local dist = (other.node.position - myPos):Length()
            if dist < nearestDist then
                nearestDist = dist
                nearest = other
            end
        end
    end

    return nearest, nearestDist
end

--- 更新单个单位的 AI
---@param unit table
---@param dt number
local function UpdateUnitAI(unit, dt)
    if unit.aiState == "dead" then return end

    -- 死亡状态：播放动画后标记为 dead
    if unit.aiState == "die" then
        unit.dieTimer = unit.dieTimer + dt
        if unit.dieTimer >= CONFIG.DIE_ANIM_TIME then
            unit.aiState = "dead"
            -- 淡出并隐藏
            unit.node.enabled = false
        end
        if unit.fsm then unit.fsm:SetFloat("moveSpeed", 0) end
        return
    end

    -- 检查是否死亡
    if (unit.data.hp or 0) <= 0 then
        unit.aiState = "die"
        unit.dieTimer = 0
        if unit.fsm then unit.fsm:SetTrigger("die") end
        unit.moveSpeed = 0
        if unit.fsm then unit.fsm:SetFloat("moveSpeed", 0) end
        return
    end

    -- 查找目标
    local enemy, dist = FindNearestEnemy(unit)

    if not enemy then
        -- 没有敌人了，切到 idle
        unit.aiState = "idle"
        unit.moveSpeed = 0
        if unit.fsm then unit.fsm:SetFloat("moveSpeed", 0) end
        return
    end

    -- 攻击冷却
    unit.attackTimer = math.max(0, unit.attackTimer - dt)

    -- 弓兵和步兵使用不同的攻击范围
    local attackRange = unit.isArcher and CONFIG.ARCHER_RANGE or CONFIG.ATTACK_RANGE

    if dist <= attackRange then
        -- 在攻击范围内
        if unit.aiState ~= "attack" then
            -- 面朝敌人
            local dir = enemy.node.position - unit.node.position
            dir.y = 0
            if dir:Length() > 0.01 then
                local angle = math.deg(math.atan(dir.x, dir.z))
                unit.node.rotation = Quaternion(angle, Vector3.UP)
            end
        end

        if unit.attackTimer <= 0 and unit.aiState ~= "attack" then
            -- 发起攻击
            unit.aiState = "attack"
            unit.moveSpeed = 0
            if unit.fsm then
                unit.fsm:SetFloat("moveSpeed", 0)
                unit.fsm:SetTrigger("attack")
            end
            unit.hasDamaged = false
            unit.attackAnimTime = 0
            unit.target = enemy
            unit.attackTimer = CONFIG.ATTACK_CD
        elseif unit.aiState == "attack" then
            local stateTime
            if unit.fsm then
                stateTime = unit.fsm:GetStateTime(0)
            else
                stateTime = CONFIG.ATTACK_CD - unit.attackTimer
            end
            if stateTime >= 0.4 and not unit.hasDamaged and unit.target then
                if unit.isArcher then
                    -- 弓兵：发射箭矢
                    SpawnArrow(unit, unit.target)
                else
                    -- 步兵：近战直接造成伤害
                    local atk = unit.data.atk or 10
                    local def = unit.target.data.def or 0
                    local dmg = math.max(1, atk - def)
                    dmg = math.floor(dmg * (0.8 + math.random() * 0.4))
                    unit.target.data.hp = (unit.target.data.hp or 0) - dmg
                end
                unit.hasDamaged = true
                -- 攻击音效
                if unit.isArcher then
                    AudioManager.BattleArrow()  -- 弓弦射箭声
                else
                    if math.random() < 0.5 then
                        AudioManager.BattleHit()
                    else
                        AudioManager.BattleSwing()
                    end
                end
            end
            -- 攻击结束判定
            local attackFinished
            if unit.fsm then
                attackFinished = unit.fsm:IsAnimationFinished(0)
            else
                attackFinished = stateTime >= 0.8
            end
            if attackFinished then
                unit.aiState = "idle"
            end
        else
            -- 攻击冷却中，idle
            unit.aiState = "idle"
            unit.moveSpeed = 0
            if unit.fsm then unit.fsm:SetFloat("moveSpeed", 0) end
        end
    else
        -- 不在攻击范围，移动接近
        unit.aiState = "move"
        local dir = enemy.node.position - unit.node.position
        dir.y = 0
        local len = dir:Length()
        if len > 0.01 then
            dir = dir / len
            -- 面朝移动方向
            local angle = math.deg(math.atan(dir.x, dir.z))
            unit.node.rotation = Quaternion(angle, Vector3.UP)

            -- 移动（速度受属性影响）
            local speedMul = unit.data.spd / 20.0
            local moveSpeed = CONFIG.MOVE_SPEED * speedMul
            unit.node:Translate(Vector3(0, 0, 1) * moveSpeed * dt, TS_LOCAL)
            unit.moveSpeed = moveSpeed
        end
        if unit.fsm then unit.fsm:SetFloat("moveSpeed", unit.moveSpeed) end
    end
end

-- ============================================================================
-- 箭矢系统
-- ============================================================================

--- 发射箭矢
---@param shooter table 发射单位
---@param target table 目标单位
function SpawnArrow(shooter, target)
    if not scene_ or not target or not target.node then return end

    local startPos = shooter.node.position + Vector3(0, 1.2, 0)  -- 从胸口高度发射
    local targetPos = target.node.position + Vector3(0, 0.8, 0)  -- 瞄准目标身体
    local dir = targetPos - startPos
    local len = dir:Length()
    if len < 0.1 then return end
    dir = dir / len

    -- 创建箭矢节点（Cylinder 模拟箭杆，放大到清晰可见）
    local arrowNode = scene_:CreateChild("Arrow")
    arrowNode.position = startPos
    -- 箭杆：直径 0.04m，长度 0.6m
    arrowNode.scale = Vector3(0.04, 0.6, 0.04)

    -- 朝向目标：Cylinder 长轴是 Y，需要先绕 X 旋转 90° 放倒成水平，再旋转到飞行方向
    local yaw = math.deg(math.atan(dir.x, dir.z))
    local horizDist = math.sqrt(dir.x * dir.x + dir.z * dir.z)
    local pitch = -math.deg(math.atan(dir.y, horizDist))
    arrowNode.rotation = Quaternion(yaw, Vector3.UP) * Quaternion(pitch, Vector3.RIGHT) * Quaternion(90, Vector3.RIGHT)

    local arrowModel = arrowNode:CreateComponent("StaticModel")
    arrowModel:SetModel(cache:GetResource("Model", "Models/Cylinder.mdl"))
    -- 箭杆用亮色木材，战场上容易分辨
    local arrowMat = Material:new()
    arrowMat:SetTechnique(0, cache:GetResource("Technique", "Techniques/PBR/PBRNoTexture.xml"))
    arrowMat:SetShaderParameter("MatDiffColor", Variant(Color(0.75, 0.55, 0.25, 1.0)))
    arrowMat:SetShaderParameter("Roughness", Variant(0.5))
    arrowMat:SetShaderParameter("Metallic", Variant(0.15))
    arrowModel:SetMaterial(arrowMat)
    arrowModel.castShadows = false

    -- 箭头（小锥形，用深色金属表示）
    local tipNode = arrowNode:CreateChild("ArrowTip")
    tipNode.position = Vector3(0, 0.6, 0)   -- 箭杆顶端
    tipNode.scale = Vector3(2.5, 0.3, 2.5)  -- 相对箭杆加粗，短锥形
    local tipModel = tipNode:CreateComponent("StaticModel")
    tipModel:SetModel(cache:GetResource("Model", "Models/Cone.mdl"))
    local tipMat = Material:new()
    tipMat:SetTechnique(0, cache:GetResource("Technique", "Techniques/PBR/PBRNoTexture.xml"))
    tipMat:SetShaderParameter("MatDiffColor", Variant(Color(0.3, 0.3, 0.35, 1.0)))
    tipMat:SetShaderParameter("Roughness", Variant(0.3))
    tipMat:SetShaderParameter("Metallic", Variant(0.9))
    tipModel:SetMaterial(tipMat)
    tipModel.castShadows = false

    -- 计算伤害
    local atk = shooter.data.atk or 10
    local def = target.data.def or 0
    local dmg = math.max(1, atk - def)
    dmg = math.floor(dmg * (0.8 + math.random() * 0.4))

    arrows_[#arrows_ + 1] = {
        node = arrowNode,
        dir = dir,
        speed = CONFIG.ARROW_SPEED,
        damage = dmg,
        target = target,
        side = shooter.data.side,
        lifetime = 3.0,  -- 最多存活3秒
    }
end

--- 更新所有飞行中的箭矢
---@param dt number
local function UpdateArrows(dt)
    local i = 1
    while i <= #arrows_ do
        local arrow = arrows_[i]
        if not arrow.node or not arrow.node:IsEnabled() then
            -- 箭矢已被清理
            table.remove(arrows_, i)
        else
            -- 移动箭矢
            local move = arrow.dir * arrow.speed * dt
            arrow.node.position = arrow.node.position + move
            arrow.lifetime = arrow.lifetime - dt

            -- 检查是否命中目标
            local hit = false
            if arrow.target and arrow.target.aiState ~= "dead" and arrow.target.node then
                local dist = (arrow.node.position - arrow.target.node.position):Length()
                if dist < 1.2 then
                    -- 命中！造成伤害
                    arrow.target.data.hp = (arrow.target.data.hp or 0) - arrow.damage
                    AudioManager.BattleHit()
                    hit = true
                end
            end

            -- 超时或命中则销毁
            if hit or arrow.lifetime <= 0 then
                arrow.node:Remove()
                table.remove(arrows_, i)
            else
                i = i + 1
            end
        end
    end
end



-- ============================================================================
-- 阅兵入场
-- ============================================================================

local function UpdateParade(dt)
    local allReached = true
    for _, unit in ipairs(battleUnits_) do
        if not unit.reachedTarget then
            local curPos = unit.node.position
            local tx = unit.targetPos.x
            local tz = unit.targetPos.z
            local dx = tx - curPos.x
            local dz = tz - curPos.z
            local dist = math.sqrt(dx * dx + dz * dz)
            if dist > 0.15 then
                allReached = false
                local step = CONFIG.PARADE_SPEED * dt
                if step >= dist then
                    unit.node.position = Vector3(tx, curPos.y, tz)
                    unit.reachedTarget = true
                    unit.moveSpeed = 0
                    if unit.fsm then unit.fsm:SetFloat("moveSpeed", 0) end
                else
                    local ratio = step / dist
                    unit.node.position = Vector3(
                        curPos.x + dx * ratio, curPos.y, curPos.z + dz * ratio)
                    unit.moveSpeed = CONFIG.PARADE_SPEED
                    if unit.fsm then unit.fsm:SetFloat("moveSpeed", CONFIG.PARADE_SPEED) end
                end
            else
                unit.node.position = Vector3(tx, curPos.y, tz)
                unit.reachedTarget = true
                unit.moveSpeed = 0
                if unit.fsm then unit.fsm:SetFloat("moveSpeed", 0) end
            end
        end
    end

    if allReached then
        if not paradeAllReached_ then
            paradeAllReached_ = true
            paradeHoldTimer_ = 0
            log:Write(LOG_INFO, "[Battle] 双方列阵完毕，准备开战！")
        end
        paradeHoldTimer_ = paradeHoldTimer_ + dt
        if paradeHoldTimer_ >= CONFIG.PARADE_HOLD_TIME then
            battleState_ = "fighting"
            log:Write(LOG_INFO, "[Battle] 开战！")
        end
    end
end

-- ============================================================================
-- 胜负判定
-- ============================================================================

local function CountAlive(side)
    local count = 0
    for _, u in ipairs(battleUnits_) do
        if u.data.side == side and u.aiState ~= "die" and u.aiState ~= "dead" then
            count = count + 1
        end
    end
    return count
end

local function CheckBattleResult()
    local playerAlive = CountAlive("player")
    local enemyAlive = CountAlive("enemy")

    if enemyAlive == 0 and battleState_ == "fighting" then
        battleState_ = "victory"
        return "victory"
    elseif playerAlive == 0 and battleState_ == "fighting" then
        battleState_ = "defeat"
        return "defeat"
    end
    return nil
end

-- ============================================================================
-- 战斗结算
-- ============================================================================

local function SettleBattle(result)
    -- 如果有自定义结算回调（事件战斗），使用它代替默认逻辑
    if onSettleOverride_ then
        local report = onSettleOverride_(result, rivalData_, deployedIds_)
        SaveSystem.AutoSave()
        return report
    end

    local s = GameData.state
    local report = {
        result = result,
        rivalName = rivalData_.name,
        rewards = { silver = 0, grain = 0, fame = 0 },
        casualties = {},  -- { memberId, name, injury }
        soldierLosses = { infantry = 0, archers = 0 },  -- 士兵损耗
    }

    if result == "victory" then
        -- 胜利奖励
        report.rewards.silver = rivalData_.rewards.silver
        report.rewards.grain = rivalData_.rewards.grain
        report.rewards.fame = rivalData_.rewards.fame
        GameData.AddResource("silver", report.rewards.silver)
        GameData.AddResource("grain", report.rewards.grain)
        GameData.AddResource("fame", report.rewards.fame)
        GameData.AddLog("讨伐" .. rivalData_.name .. "大获全胜！缴获银两" ..
            report.rewards.silver .. "、粮食" .. report.rewards.grain)

        -- 征伐战役：标记区域征服
        if regionId_ then
            CampaignRegions.MarkConquered(regionId_)
            local region = CampaignRegions.GetById(regionId_)
            if region then
                GameData.AddLog("成功征服【" .. region.name .. "】！")
            end
        end
    else
        -- 战败：声望损失
        local fameLoss = math.floor(rivalData_.rewards.fame * 0.5)
        GameData.AddResource("fame", -fameLoss)
        report.rewards.fame = -fameLoss
        GameData.AddLog("讨伐" .. rivalData_.name .. "失败，声望下降。")
    end

    -- 战后士兵损耗（从 state.army 扣除）
    if s.army and (deployInfantry_ > 0 or deployArchers_ > 0) then
        local lossMin, lossMax
        if result == "victory" then
            lossMin, lossMax = 0.10, 0.30  -- 胜利：损耗10-30%
        else
            lossMin, lossMax = 0.40, 0.70  -- 败退：损耗40-70%
        end
        local lossRate = lossMin + math.random() * (lossMax - lossMin)

        local infantryLoss = math.floor(deployInfantry_ * lossRate)
        local archerLoss = math.floor(deployArchers_ * lossRate)

        s.army.infantry = math.max(0, s.army.infantry - infantryLoss)
        s.army.archers = math.max(0, s.army.archers - archerLoss)

        report.soldierLosses.infantry = infantryLoss
        report.soldierLosses.archers = archerLoss

        local totalLoss = infantryLoss + archerLoss
        if totalLoss > 0 then
            GameData.AddLog(string.format("此战损失步兵%d、弓兵%d（损耗%.0f%%）",
                infantryLoss, archerLoss, lossRate * 100))
        end
        log:Write(LOG_INFO, string.format("[Battle] 士兵损耗: 步兵-%d 弓兵-%d (%.0f%%), 剩余步兵%d 弓兵%d",
            infantryLoss, archerLoss, lossRate * 100, s.army.infantry, s.army.archers))
    end

    -- 出战将领伤亡结算
    for _, memberId in ipairs(deployedIds_) do
        local member = GameData.GetMember(memberId)
        if member then
            -- 根据战况决定伤亡
            local injuryRoll = math.random(100)
            local injuryThreshold = (result == "victory") and 20 or 50

            if injuryRoll < injuryThreshold then
                -- 受伤：扣健康
                local healthLoss = math.random(5, 20)
                if result == "defeat" then healthLoss = healthLoss + 10 end
                member.health = math.max(1, member.health - healthLoss)

                if member.health <= 10 then
                    if member.state ~= "生病" then member.prevState = member.state end
                    member.state = "生病"
                    report.casualties[#report.casualties + 1] = {
                        memberId = memberId,
                        name = member.name,
                        injury = "重伤（健康-" .. healthLoss .. "）",
                    }
                else
                    member.state = "从军"
                    report.casualties[#report.casualties + 1] = {
                        memberId = memberId,
                        name = member.name,
                        injury = "轻伤（健康-" .. healthLoss .. "）",
                    }
                end
            else
                -- 安全归来
                member.state = "从军"
            end
        end
    end

    SaveSystem.AutoSave()
    return report
end

-- ============================================================================
-- UI 系统
-- ============================================================================

local function CreateBattleUI()
    uiRoot_ = UI.Panel {
        id = "battleUI",
        width = "100%", height = "100%",
        pointerEvents = "box-none",
        children = {
            -- 顶部信息栏
            UI.Panel {
                id = "topInfo",
                width = "100%",
                flexDirection = "row", justifyContent = "space-between",
                padding = { 8, 16, 8, 16 },
                backgroundColor = { 0, 0, 0, 120 },
                children = {
                    -- 我方信息
                    UI.Panel {
                        flexDirection = "row", alignItems = "center", gap = 6,
                        children = {
                            UI.Panel { width = 10, height = 10, borderRadius = 5, backgroundColor = { 80, 160, 255, 255 } },
                            UI.Label { id = "playerInfo", text = "我军", fontSize = 14, fontColor = { 200, 220, 255, 255 } },
                        },
                    },
                    -- 战斗时间
                    UI.Label { id = "battleTimer", text = "00:00", fontSize = 16, fontColor = { 255, 220, 150, 255 }, fontWeight = "bold" },
                    -- 敌方信息
                    UI.Panel {
                        flexDirection = "row", alignItems = "center", gap = 6,
                        children = {
                            UI.Label { id = "enemyInfo", text = rivalData_.name, fontSize = 14, fontColor = { 255, 180, 180, 255 } },
                            UI.Panel { width = 10, height = 10, borderRadius = 5, backgroundColor = { 255, 80, 80, 255 } },
                        },
                    },
                },
            },

            -- 中间阅兵/开战提示
            UI.Label {
                id = "paradeLabel",
                text = "列阵进军",
                fontSize = 22, fontWeight = "bold",
                fontColor = { 255, 220, 100, 255 },
                position = "absolute", top = "35%", left = 0, right = 0,
                textAlign = "center",
            },

            -- 底部速度控制
            UI.Panel {
                position = "absolute", bottom = 16, left = 0, right = 0,
                flexDirection = "row", justifyContent = "center", gap = 12,
                children = (function()
                    local speedBtns = {}
                    for _, spd in ipairs({ 1.0, 2.0, 3.0 }) do
                        local needAd = (spd >= 2) and not AdSystem.IsSpeedUnlocked()
                        local label = spd .. "x"
                        speedBtns[#speedBtns + 1] = UI.Panel {
                            width = 48, height = 36, borderRadius = 8,
                            backgroundColor = { 0, 0, 0, 150 }, borderWidth = 1, borderColor = { 255, 255, 255, 80 },
                            justifyContent = "center", alignItems = "center",
                            onClick = function(self)
                                if spd >= 2 and not AdSystem.IsSpeedUnlocked() then
                                    AdSystem.UnlockSpeed(function(success, msg)
                                        if success then
                                            battleSpeed_ = spd
                                            Toast.Show("加速已领取")
                                        else
                                            Toast.Show(msg or "广告播放失败")
                                        end
                                    end)
                                    return
                                end
                                battleSpeed_ = spd
                            end,
                            children = {
                                UI.Label { text = label, fontSize = 14, fontColor = { 255, 255, 255, 200 } },
                                needAd and UI.Panel {
                                    position = "absolute", top = -4, right = -4,
                                    width = 18, height = 14, borderRadius = 4,
                                    backgroundColor = { 255, 180, 0, 230 },
                                    justifyContent = "center", alignItems = "center",
                                    children = {
                                        UI.Label { text = "AD", fontSize = 7, fontColor = { 0, 0, 0, 255 }, fontWeight = "bold" },
                                    },
                                } or nil,
                            },
                        }
                    end
                    return speedBtns
                end)(),
            },
        },
    }

    -- 清空残留的 overlay stack（主界面的弹窗/Modal 可能还在栈里）
    -- overlay stack 在 findWidgetAt 中优先级最高，会拦截所有点击事件
    local stack = UI.GetOverlayStack()
    for i = #stack, 1, -1 do
        UI.PopOverlay(stack[i])
    end

    UI.SetRoot(uiRoot_)
end

local paradeLabelHideTimer_ = 0

local function UpdateBattleUI()
    if not uiRoot_ then return end

    -- 更新时间
    local mins = math.floor(battleTime_ / 60)
    local secs = math.floor(battleTime_ % 60)
    local timerLabel = uiRoot_:FindById("battleTimer")
    if timerLabel then
        timerLabel:SetText(string.format("%02d:%02d", mins, secs))
    end

    -- 更新双方存活数
    local pAlive = CountAlive("player")
    local eAlive = CountAlive("enemy")
    local pLabel = uiRoot_:FindById("playerInfo")
    if pLabel then pLabel:SetText("我军 " .. pAlive .. " 人") end
    local eLabel = uiRoot_:FindById("enemyInfo")
    if eLabel then eLabel:SetText(rivalData_.name .. " " .. eAlive .. " 人") end

    -- 更新阅兵提示标签
    local paradeLabel = uiRoot_:FindById("paradeLabel")
    if paradeLabel then
        if battleState_ == "parade" then
            if paradeAllReached_ then
                paradeLabel:SetText("开战！")
                paradeLabel:SetFontColor({ 255, 80, 60, 255 })
            else
                paradeLabel:SetText("列阵进军")
                paradeLabel:SetFontColor({ 255, 220, 100, 255 })
            end
            paradeLabel:SetVisible(true)
            paradeLabelHideTimer_ = 0
        elseif battleState_ == "fighting" then
            -- 开战后短暂显示"开战！"再隐藏
            paradeLabelHideTimer_ = paradeLabelHideTimer_ + 1
            if paradeLabelHideTimer_ > 60 then -- 约1秒后隐藏
                paradeLabel:SetVisible(false)
            else
                paradeLabel:SetText("开战！")
                paradeLabel:SetFontColor({ 255, 80, 60, 255 })
            end
        else
            paradeLabel:SetVisible(false)
        end
    end
end

-- ============================================================================
-- 血条绘制（NanoVG，通过 UI.QueueOverlay 渲染）
-- ============================================================================

local hpBarDebugLogged_ = false

--- 在 UI 的 NanoVG 渲染管线中绘制血条
--- nvg 参数是 UI 系统的 NanoVG context，坐标系为 UI.GetWidth() x UI.GetHeight()
---@param nvg userdata NanoVG context from UI system
local function DrawHealthBarsOverlay(nvg)
    if not cameraNode_ then return end
    local camera = cameraNode_:GetComponent("Camera")
    if not camera then return end

    local logW = UI.GetWidth()
    local logH = UI.GetHeight()
    local camPos = cameraNode_.position
    local camDir = cameraNode_.direction

    local drawnCount = 0
    for _, unit in ipairs(battleUnits_) do
        if not unit.data or not unit.node then goto continue_hp end
        local hp = unit.data.hp or 0
        local maxHp = unit.data.maxHp or 1
        if unit.aiState ~= "dead" and hp > 0 then
            local headOffset = 1.8
            local worldPos = unit.node.position + Vector3(0, headOffset, 0)

            -- 检查是否在相机前方（WorldToScreenPoint 返回 Vector2，没有 z 分量）
            local toUnit = worldPos - camPos
            if toUnit:DotProduct(camDir) > 0 then
                -- 转换到归一化屏幕坐标 (0~1)
                local screenPos = camera:WorldToScreenPoint(worldPos)
                local sx = screenPos.x * logW
                local sy = screenPos.y * logH

                local barW = 50
                local barH = 6
                local hpRatio = hp / maxHp

                -- 背景条
                nvgBeginPath(nvg)
                nvgRect(nvg, sx - barW / 2, sy, barW, barH)
                nvgFillColor(nvg, nvgRGBA(0, 0, 0, 180))
                nvgFill(nvg)

                -- 血量条（按阵营区分颜色）
                local br, bg, bb
                if unit.data.side == "player" then
                    -- 我方：蓝绿色调（满血亮蓝绿，低血偏暗蓝）
                    br = math.floor(40 + 40 * (1 - hpRatio))
                    bg = math.floor(180 * hpRatio + 80)
                    bb = math.floor(220 * hpRatio + 35)
                else
                    -- 敌方：红橙色调（满血亮红，低血偏暗红）
                    br = math.floor(220 * hpRatio + 35)
                    bg = math.floor(60 * hpRatio + 20)
                    bb = math.floor(30 * hpRatio + 10)
                end
                nvgBeginPath(nvg)
                nvgRect(nvg, sx - barW / 2, sy, barW * hpRatio, barH)
                nvgFillColor(nvg, nvgRGBA(br, bg, bb, 230))
                nvgFill(nvg)

                -- 将领名字（士兵不显示，颜色区分阵营）
                if not unit.data.isSoldier then
                    nvgFontFace(nvg, "sans")
                    nvgFontSize(nvg, 13)
                    nvgTextAlign(nvg, NVG_ALIGN_CENTER + NVG_ALIGN_BOTTOM)
                    if unit.data.side == "player" then
                        nvgFillColor(nvg, nvgRGBA(180, 220, 255, 240))  -- 我方：淡蓝色名字
                    else
                        nvgFillColor(nvg, nvgRGBA(255, 180, 160, 240))  -- 敌方：淡红色名字
                    end
                    nvgText(nvg, sx, sy - 3, unit.data.name)
                end

                drawnCount = drawnCount + 1
            end
        end
        ::continue_hp::
    end

    if not hpBarDebugLogged_ and drawnCount > 0 then
        log:Write(LOG_INFO, "[Battle] 血条绘制成功: " .. drawnCount .. " 个单位")
        hpBarDebugLogged_ = true
    end
end

-- ============================================================================
-- 结算弹窗
-- ============================================================================

local function ShowResultScreen(report)
    -- 清除战斗UI子元素（速度控制等），但保留 root 不变
    -- 重要：不能调用 UI.SetRoot() 替换 root，否则 UI 框架内部的
    -- pressedWidgets_ 映射会失效，导致触屏 PointerUp 时找不到原
    -- widget，onClick 永远不会触发（鼠标偶尔可以是因为 pointerId 固定为 0）
    if uiRoot_ then
        uiRoot_:ClearChildren()
    end

    local isVictory = report.result == "victory"
    local titleText = isVictory and "大获全胜！" or "兵败而归"
    local titleColor = isVictory and { 255, 220, 80, 255 } or { 255, 100, 100, 255 }

    -- 奖惩文本
    local rewardLines = {}
    if isVictory then
        rewardLines[#rewardLines + 1] = UI.Label {
            text = "缴获银两 +" .. report.rewards.silver,
            fontSize = 13, fontColor = { 200, 200, 200, 255 },
        }
        rewardLines[#rewardLines + 1] = UI.Label {
            text = "缴获粮食 +" .. report.rewards.grain,
            fontSize = 13, fontColor = { 200, 200, 200, 255 },
        }
        rewardLines[#rewardLines + 1] = UI.Label {
            text = "声望提升 +" .. report.rewards.fame,
            fontSize = 13, fontColor = { 200, 200, 200, 255 },
        }
    else
        rewardLines[#rewardLines + 1] = UI.Label {
            text = "声望下降 " .. report.rewards.fame,
            fontSize = 13, fontColor = { 255, 100, 100, 255 },
        }
    end

    -- 士兵损耗文本
    if report.soldierLosses and (report.soldierLosses.infantry > 0 or report.soldierLosses.archers > 0) then
        rewardLines[#rewardLines + 1] = UI.Panel { width = "80%", height = 1, backgroundColor = { 255, 255, 255, 30 }, marginVertical = 4 }
        if report.soldierLosses.infantry > 0 then
            rewardLines[#rewardLines + 1] = UI.Label {
                text = "步兵损失 -" .. report.soldierLosses.infantry,
                fontSize = 13, fontColor = { 255, 160, 120, 255 },
            }
        end
        if report.soldierLosses.archers > 0 then
            rewardLines[#rewardLines + 1] = UI.Label {
                text = "弓兵损失 -" .. report.soldierLosses.archers,
                fontSize = 13, fontColor = { 255, 160, 120, 255 },
            }
        end
    end

    -- 伤亡文本
    local casualtyLines = {}
    if #report.casualties > 0 then
        casualtyLines[#casualtyLines + 1] = UI.Panel { width = "100%", height = 1, backgroundColor = { 255, 255, 255, 40 }, marginVertical = 6 }
        casualtyLines[#casualtyLines + 1] = UI.Label { text = "将领伤亡", fontSize = 14, fontColor = { 255, 180, 100, 255 } }
        for _, c in ipairs(report.casualties) do
            casualtyLines[#casualtyLines + 1] = UI.Label {
                text = c.name .. " - " .. c.injury,
                fontSize = 12, fontColor = { 200, 200, 200, 255 },
            }
        end
    else
        casualtyLines[#casualtyLines + 1] = UI.Label {
            text = "全员平安归来",
            fontSize = 13, fontColor = { 150, 255, 150, 255 }, marginTop = 6,
        }
    end

    -- 将结算面板作为子元素添加到现有 root，不替换 root
    -- 不用 position="absolute"，因为 root 已经清空了 children，
    -- 普通全屏子元素即可正确占满
    local resultPanel = UI.Panel {
        width = "100%", height = "100%",
        justifyContent = "center", alignItems = "center",
        pointerEvents = "auto",
        backgroundColor = { 0, 0, 0, 160 },
        children = {
            UI.Panel {
                width = 280, borderRadius = 12,
                backgroundColor = { 30, 30, 40, 240 },
                borderWidth = 2, borderColor = isVictory and { 200, 180, 60, 255 } or { 200, 60, 60, 255 },
                padding = 20, gap = 8, alignItems = "center",
                children = (function()
                    local items = {
                        -- 标题
                        UI.Label { text = "讨伐" .. report.rivalName, fontSize = 12, fontColor = { 180, 180, 180, 255 } },
                        UI.Label { text = titleText, fontSize = 24, fontColor = titleColor, fontWeight = "bold" },
                        UI.Panel { width = "80%", height = 1, backgroundColor = { 255, 255, 255, 40 }, marginVertical = 4 },
                    }
                    -- 奖惩
                    for _, v in ipairs(rewardLines) do items[#items + 1] = v end
                    -- 伤亡
                    for _, v in ipairs(casualtyLines) do items[#items + 1] = v end
                    -- 返回按钮
                    items[#items + 1] = UI.Button {
                        text = "返回宗族",
                        width = 160, height = 44,
                        fontSize = 15,
                        variant = "primary",
                        marginTop = 12,
                        backgroundColor = isVictory and { 56, 168, 120, 255 } or { 120, 80, 80, 255 },
                        onClick = function(self)
                            log:Write(LOG_INFO, "[Battle] 点击返回宗族按钮")
                            BattleScene.Exit()
                        end,
                    }
                    return items
                end)(),
            },
        },
    }

    -- 添加到现有 root 作为 overlay，不调用 UI.SetRoot()
    if uiRoot_ then
        uiRoot_:AddChild(resultPanel)
    end
end

-- ============================================================================
-- 事件处理
-- ============================================================================

local function OnBattleUpdate(eventType, eventData)
    local rawDt = eventData["TimeStep"]:GetFloat()
    local dt = rawDt * battleSpeed_

    if battleState_ == "parade" then
        -- 阅兵入场阶段
        UpdateParade(dt)
        UpdateBattleUI()

    elseif battleState_ == "fighting" then
        battleTime_ = battleTime_ + dt

        -- 更新所有单位 AI
        for _, unit in ipairs(battleUnits_) do
            UpdateUnitAI(unit, dt)
        end

        -- 更新箭矢飞行
        UpdateArrows(dt)

        -- 检查胜负
        local result = CheckBattleResult()
        if result then
            log:Write(LOG_INFO, "[Battle] 战斗结束: " .. result)
            local report = SettleBattle(result)
            battleState_ = result
            ShowResultScreen(report)
        end

        UpdateBattleUI()
    end
end

-- ============================================================================
-- 公开接口
-- ============================================================================

--- 进入战斗场景
---@param rival table 敌族数据
---@param deployedMemberIds table 出战族人ID列表
---@param soldierCount number 我方可用兵力
---@param onEndCallback function 战斗结束回调
---@param options table|nil 可选参数 { archerRatio = 0~1 }
function BattleScene.Enter(rival, deployedMemberIds, soldierCount, onEndCallback, options)
    rivalData_ = rival
    deployedIds_ = deployedMemberIds
    playerSoldierCount_ = soldierCount or 0
    onBattleEnd_ = onEndCallback
    onSettleOverride_ = (options and options.onSettle) or nil  -- 自定义结算（事件战斗用）
    battleState_ = "deploy"
    battleTime_ = 0
    battleSpeed_ = CONFIG.BATTLE_SPEED
    hpBarDebugLogged_ = false
    archerRatio_ = (options and options.archerRatio) or 0
    arrows_ = {}
    -- 征伐系统参数
    deployInfantry_ = (options and options.infantry) or 0
    deployArchers_ = (options and options.archers) or 0
    trainingLevel_ = (options and options.trainingLevel) or 0
    regionId_ = (options and options.regionId) or nil

    log:Write(LOG_INFO, "[Battle] ======= 讨伐战斗开始 =======")
    log:Write(LOG_INFO, "[Battle] 目标: " .. rival.name .. " (" .. rival.tierName .. ")")
    log:Write(LOG_INFO, "[Battle] 我方将领: " .. #deployedMemberIds .. " 人, 士兵: " .. soldierCount)
    if deployInfantry_ > 0 or deployArchers_ > 0 then
        log:Write(LOG_INFO, string.format("[Battle] 出征步兵: %d, 弓兵: %d, 训练等级: %d",
            deployInfantry_, deployArchers_, trainingLevel_))
    end
    if regionId_ then
        log:Write(LOG_INFO, "[Battle] 征伐区域: " .. tostring(regionId_))
    end

    -- 1. 创建3D场景
    CreateBattleScene()
    SetupCamera()
    CreateGround()

    -- 2. 部署单位
    DeployUnits()

    -- 3. 创建 UI
    CreateBattleUI()

    -- 4. 战斗 BGM（激昂战鼓版）
    AudioManager.PlayBGM("BATTLE")

    -- 5. 注册血条绘制为全局 UI 组件（在 UI.Render 的 nvgBeginFrame/nvgEndFrame 之间执行）
    UI.RegisterGlobalComponent("BattleHealthBars", {
        Render = function(self, nvg)
            if battleState_ == "parade" or battleState_ == "fighting" then
                DrawHealthBarsOverlay(nvg)
            end
        end,
    })

    -- 6. 订阅事件
    SubscribeToEvent("Update", "HandleBattleUpdate")

    -- 7. 阅兵入场（双方行军就位后自动开战）
    battleState_ = "parade"
    paradeAllReached_ = false
    paradeHoldTimer_ = 0
    paradeLabelHideTimer_ = 0
    log:Write(LOG_INFO, "[Battle] 阅兵入场开始！")
end

--- 退出战斗场景，返回主界面
function BattleScene.Exit()
    log:Write(LOG_INFO, "[Battle] ======= 退出战斗场景 =======")

    -- 注销血条全局组件
    UI.UnregisterGlobalComponent("BattleHealthBars")

    -- 取消事件订阅（不要置空 HandleBattleUpdate，否则第二次战斗无法订阅）
    UnsubscribeFromEvent("Update")

    -- 清理箭矢
    for _, arrow in ipairs(arrows_) do
        if arrow.node then arrow.node:Remove() end
    end
    arrows_ = {}

    -- 清理战斗单位引用
    battleUnits_ = {}

    -- 清理场景
    if scene_ then
        scene_:Remove()
        scene_ = nil
    end
    cameraNode_ = nil

    -- 恢复主界面 BGM
    AudioManager.StopBGM()

    -- 回调
    if onBattleEnd_ then
        onBattleEnd_()
    end
end

-- 全局事件处理函数（供 SubscribeToEvent 使用）
function HandleBattleUpdate(eventType, eventData)
    OnBattleUpdate(eventType, eventData)
end

return BattleScene
