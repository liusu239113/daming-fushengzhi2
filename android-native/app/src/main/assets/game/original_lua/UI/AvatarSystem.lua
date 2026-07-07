-- ============================================================================
-- 大明浮生志2 - 头像系统 v2
-- 分配制：每个角色分配唯一头像，同性别同年龄段内不重复
-- 支持玩家家族 + 敌对家族，两套独立分配池互不冲突
-- ============================================================================

local AvatarSystem = {}

-- ============================================================================
-- 头像资源库（所有文件在 image/avatars/ 子目录）
-- ============================================================================

local P = "image/avatars/"  -- 路径前缀

local AVATARS = {
    baby = {
        P .. "pd_baby_1_20260511205339.png",
        P .. "pd_baby_2_20260511205608.png",
        P .. "av_baby_a_20260511210946.png",
        P .. "av_baby_b_20260511210956.png",
        P .. "av_baby_c_20260511210945.png",
        P .. "av_baby_d_20260511211009.png",
    },
    male = {
        child = {
            P .. "pd_boy_1_20260511205352.png",
            P .. "av_boy_a_20260511211044.png",
            P .. "av_boy_b_20260511211028.png",
            P .. "av_boy_c_20260511211027.png",
        },
        youth = {
            P .. "paperdoll_base_v2_20260511205100.png",
            P .. "pd_male_youth_2_20260511205513.png",
            P .. "pd_male_youth_3_20260511205510.png",
            P .. "av_m_youth_a_20260511210138.png",
            P .. "av_m_youth_b_20260511210150.png",
            P .. "av_m_youth_c_20260511210141.png",
            P .. "av_m_youth_d_20260511210142.png",
            P .. "av_m_youth_e_20260511210149.png",
        },
        adult = {
            P .. "av_m_adult_a_20260512082153.png",
            P .. "av_m_adult_b_20260512081743.png",
            P .. "av_m_adult_c_20260512082336.png",
            P .. "av_m_adult_d_20260512081652.png",
            P .. "av_m_adult_e_20260512082337.png",
            P .. "av_m_adult_f_20260512082413.png",
            P .. "av_m_adult_g_20260512141103.png",
            P .. "av_m_adult_h_20260512141024.png",
            P .. "av_m_adult_i_20260512141023.png",
            P .. "av_m_adult_j_20260512141026.png",
            P .. "av_m_adult_k_20260512141046.png",
            P .. "av_m_adult_l_20260512141023.png",
        },
        middle = {
            P .. "pd_male_mid_1_20260511205341.png",
            P .. "pd_male_mid_2_20260511205501.png",
            P .. "av_m_mid_a_20260511210341.png",
            P .. "av_m_mid_b_20260511210335.png",
            P .. "av_m_mid_c_20260511210326.png",
            P .. "av_m_mid_d_20260511210339.png",
            P .. "av_m_mid_e_20260511210333.png",
        },
        elder = {
            P .. "pd_male_elder_1_20260511205340.png",
            P .. "av_m_elder_a_20260511210559.png",
            P .. "av_m_elder_b_20260511210551.png",
            P .. "av_m_elder_c_20260511210536.png",
            P .. "av_m_elder_d_20260511210556.png",
        },
    },
    female = {
        child = {
            P .. "pd_girl_1_20260511205347.png",
            P .. "av_girl_a_20260511211029.png",
            P .. "av_girl_b_20260511211030.png",
            P .. "av_girl_c_20260511211029.png",
        },
        youth = {
            P .. "pd_female_youth_1_20260511205344.png",
            P .. "pd_female_youth_2_20260511205502.png",
            P .. "pd_female_youth_3_20260511205503.png",
            P .. "av_f_youth_a_20260511210215.png",
            P .. "av_f_youth_b_20260511210208.png",
            P .. "av_f_youth_c_20260511210207.png",
            P .. "av_f_youth_d_20260511210212.png",
            P .. "av_f_youth_e_20260511210213.png",
        },
        adult = {
            P .. "av_f_adult_a_20260512081928.png",
            P .. "av_f_adult_b_20260512081939.png",
            P .. "av_f_adult_d_20260512081643.png",
            P .. "av_f_adult_e_20260512081750.png",
            P .. "av_f_adult_g_20260512140907.png",
            P .. "av_f_adult_h_20260512140928.png",
            P .. "av_f_adult_i_20260512140909.png",
            P .. "av_f_adult_j_20260512140918.png",
            P .. "av_f_adult_k_20260512140915.png",
            P .. "av_f_adult_l_20260512140905.png",
            P .. "av_f_adult_m_20260512141240.png",
            P .. "av_f_adult_n_20260512141227.png",
            P .. "av_f_adult_o_20260512141249.png",
            P .. "av_f_adult_p_20260512141234.png",
            P .. "av_f_adult_q_20260512141242.png",
        },
        middle = {
            P .. "pd_female_mid_1_20260511205341.png",
            P .. "pd_female_mid_2_20260511205504.png",
            P .. "av_f_mid_a_20260511210407.png",
            P .. "av_f_mid_b_20260511210401.png",
            P .. "av_f_mid_c_20260511210413.png",
            P .. "av_f_mid_d_20260511210435.png",
            P .. "av_f_mid_e_20260511210400.png",
        },
        elder = {
            P .. "pd_female_elder_1_20260511205338.png",
            P .. "av_f_elder_a_20260511210642.png",
            P .. "av_f_elder_b_20260511210618.png",
            P .. "av_f_elder_c_20260511210617.png",
            P .. "av_f_elder_d_20260511210616.png",
        },
    },
}

-- ============================================================================
-- 年龄段判定
-- ============================================================================

local function GetAgeStage(age)
    if age <= 2 then return "baby"
    elseif age <= 12 then return "child"
    elseif age <= 20 then return "youth"
    elseif age <= 35 then return "adult"
    elseif age <= 55 then return "middle"
    else return "elder" end
end

-- ============================================================================
-- 获取指定性别+年龄段的头像池
-- ============================================================================

local function GetPool(gender, stage)
    if stage == "baby" then
        return AVATARS.baby
    end
    local g = AVATARS[gender] or AVATARS.male
    local pool = g[stage]
    if not pool or #pool == 0 then
        pool = g.youth  -- fallback
    end
    return pool
end

-- ============================================================================
-- 分配器：管理已分配的头像，保证不重复
-- 用 poolKey (如 "player_male_youth") 管理各类别独立计数
-- ============================================================================

-- 已分配记录：{ [memberId] = { [stage] = avatarPath } }
local allocated = {}

-- 各池子已分配索引：{ [poolKey] = nextIndex }
local poolCounters = {}

-- 用确定性方式打散池子顺序（基于poolKey的种子）
local function ShufflePool(pool, seed)
    local copy = {}
    for i = 1, #pool do copy[i] = pool[i] end
    -- Fisher-Yates shuffle with deterministic seed
    local rng = seed
    for i = #copy, 2, -1 do
        rng = (rng * 1103515245 + 12345) % 2147483648
        local j = (rng % i) + 1
        copy[i], copy[j] = copy[j], copy[i]
    end
    return copy
end

local function GetPoolKey(prefix, gender, stage)
    if stage == "baby" then
        return prefix .. "_baby"
    end
    return prefix .. "_" .. gender .. "_" .. stage
end

local function HashString(s)
    local h = 5381
    for i = 1, #s do
        h = ((h * 33) + string.byte(s, i)) % 2147483647
    end
    return h
end

--- 从指定池中分配一个不重复的头像
---@param prefix string "player" 或 "rival_X"
---@param gender string
---@param stage string
---@param memberId any
---@return string avatarPath
local function AllocateAvatar(prefix, gender, stage, memberId)
    local pool = GetPool(gender, stage)
    local poolKey = GetPoolKey(prefix, gender, stage)

    -- 首次使用该池：打散顺序
    if not poolCounters[poolKey] then
        poolCounters[poolKey] = {
            shuffled = ShufflePool(pool, HashString(poolKey)),
            index = 0,
        }
    end

    local counter = poolCounters[poolKey]
    counter.index = counter.index + 1

    -- 如果超出池子大小，循环（但由于打散过，循环后第二轮顺序不同）
    local idx = ((counter.index - 1) % #counter.shuffled) + 1

    -- 如果整轮用完，再次打散（用新种子）
    if counter.index > #counter.shuffled then
        local newSeed = HashString(poolKey .. tostring(counter.index))
        counter.shuffled = ShufflePool(pool, newSeed)
        idx = ((counter.index - 1) % #counter.shuffled) + 1
    end

    return counter.shuffled[idx]
end

-- ============================================================================
-- 核心 API
-- ============================================================================

--- 获取玩家家族成员头像（保证同一人同一年龄段始终返回相同头像）
---@param member table { id, gender, age, ... }
---@return string 头像资源路径
function AvatarSystem.GetAvatar(member)
    if not member then return AVATARS.baby[1] end

    local age = member.age or 0
    local gender = member.gender or "male"
    local stage = GetAgeStage(age)
    local id = tostring(member.id)

    -- 检查是否已分配过该年龄段的头像
    if allocated[id] and allocated[id][stage] then
        return allocated[id][stage]
    end

    -- 分配新头像
    local avatar = AllocateAvatar("player", gender, stage, id)

    -- 记录分配
    if not allocated[id] then allocated[id] = {} end
    allocated[id][stage] = avatar

    return avatar
end

--- 获取敌族成员头像（敌族使用独立分配池，不与玩家家族冲突）
---@param rivalMember table { name, isLeader, ... }
---@param clanId number|string 敌族ID（区分不同敌族）
---@param memberIndex number 成员在该族中的索引
---@return string 头像资源路径
function AvatarSystem.GetRivalAvatar(rivalMember, clanId, memberIndex)
    local prefix = "rival_" .. tostring(clanId)
    local gender = rivalMember.gender or "male"
    -- 有年龄信息则按年龄判断，否则首领老年、其余中年
    local stage
    if rivalMember.age then
        stage = GetAgeStage(rivalMember.age)
    else
        stage = rivalMember.isLeader and "elder" or "middle"
    end

    local key = prefix .. "_" .. tostring(memberIndex)

    if allocated[key] and allocated[key][stage] then
        return allocated[key][stage]
    end

    local avatar = AllocateAvatar(prefix, gender, stage, key)

    if not allocated[key] then allocated[key] = {} end
    allocated[key][stage] = avatar

    return avatar
end

--- 转移头像分配（将临时ID的头像记录转移到正式ID，用于联姻候选人入族）
---@param fromId string|number 临时ID（如 "candidate_xxx"）
---@param toId string|number 正式成员ID
function AvatarSystem.TransferAvatar(fromId, toId)
    local fk = tostring(fromId)
    local tk = tostring(toId)
    if allocated[fk] then
        allocated[tk] = allocated[fk]
        allocated[fk] = nil
    end
end

--- 重置分配器（新游戏时调用）
function AvatarSystem.Reset()
    allocated = {}
    poolCounters = {}
end

--- 获取年龄段名称（用于调试/UI展示）
---@param age number
---@return string
function AvatarSystem.GetAgeStageName(age)
    local stage = GetAgeStage(age)
    local names = {
        baby = "婴儿",
        child = "幼童",
        youth = "少年",
        adult = "成年",
        middle = "中年",
        elder = "老年",
    }
    return names[stage] or "未知"
end

--- 获取头像统计信息（调试用）
function AvatarSystem.GetStats()
    local total = 0
    for _, pool in pairs(AVATARS.baby and {AVATARS.baby} or {}) do
        total = total + #pool
    end
    for _, gender in pairs({"male", "female"}) do
        local g = AVATARS[gender]
        if g then
            for _, pool in pairs(g) do
                if type(pool) == "table" then
                    total = total + #pool
                end
            end
        end
    end
    total = total + #AVATARS.baby

    local allocCount = 0
    for _ in pairs(allocated) do allocCount = allocCount + 1 end

    return {
        totalAvatars = total,
        allocatedMembers = allocCount,
    }
end

return AvatarSystem
