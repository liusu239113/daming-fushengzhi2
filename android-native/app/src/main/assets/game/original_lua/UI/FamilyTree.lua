-- ============================================================================
-- 大明浮生志2 - 可视化族谱树组件
-- 以树状图形式展示宗族世代关系：夫妻并列、子嗣分支、连线标注
-- 支持双轴滚动和点击族人查看详情
-- ============================================================================

local UI = require("urhox-libs/UI")
local Theme = require("UI.Theme")
local GameData = require("Data.GameData")
local RivalClans = require("Data.RivalClans")
local AvatarSystem = require("UI.AvatarSystem")
local AudioManager = require("Systems.AudioManager")

local FamilyTree = {}

-- ============================================================================
-- 布局常量
-- ============================================================================

local CARD_W = 90          -- 族人卡片宽度（保持原始尺寸）
local CARD_H = 118         -- 族人卡片高度
local COUPLE_GAP = 4       -- 夫妻之间间距
local SIBLING_GAP = 10     -- 兄弟姐妹之间间距
local GEN_GAP_Y = 30       -- 代际垂直间距（连线区域）
local TREE_PAD = 20        -- 树整体内边距
local LINE_COLOR = Theme.GOLD_DARK   -- 连线颜色
local LINE_W = 2           -- 连线宽度
local MARRIAGE_ICON = "婚"  -- 婚配符号

-- ============================================================================
-- 构建族谱数据结构（从扁平 members → 树形 familyUnits）
-- ============================================================================

--- 一个 FamilyUnit = { father, mother, children: FamilyUnit[] }
--- 递归构建：从族长开始，找配偶、找子女，子女各自形成新 FamilyUnit

local function BuildFamilyUnit(memberId)
    local member = GameData.GetMember(memberId)
    if not member then return nil end

    local unit = {
        father = nil,
        mother = nil,
        children = {},  -- FamilyUnit[]
    }

    -- 确定夫妻
    if member.gender == "male" then
        unit.father = member
        if member.spouseId then
            unit.mother = GameData.GetMember(member.spouseId)
        end
    else
        unit.mother = member
        if member.spouseId then
            unit.father = GameData.GetMember(member.spouseId)
        end
    end

    -- 收集子女 ID（从父亲的 childrenIds）
    local childIds = {}
    local primary = unit.father or unit.mother
    if primary and primary.childrenIds then
        for _, cid in ipairs(primary.childrenIds) do
            childIds[#childIds + 1] = cid
        end
    end

    -- 也检查母亲的 childrenIds（如果存在且与父亲不同）
    local secondary = unit.father and unit.mother or nil
    if secondary and secondary.childrenIds then
        local existing = {}
        for _, cid in ipairs(childIds) do existing[cid] = true end
        for _, cid in ipairs(secondary.childrenIds) do
            if not existing[cid] then
                childIds[#childIds + 1] = cid
            end
        end
    end

    -- 子女按年龄从大到小排列
    local childMembers = {}
    for _, cid in ipairs(childIds) do
        local child = GameData.GetMember(cid)
        if child then
            childMembers[#childMembers + 1] = child
        end
    end
    table.sort(childMembers, function(a, b) return a.age > b.age end)

    -- 递归为每个子女构建 FamilyUnit
    for _, child in ipairs(childMembers) do
        -- 男性子女构建自己的家庭单元
        -- 女性子女如果未嫁，也作为叶节点
        -- 女性子女如果已嫁（spouseId存在），她属于丈夫的家庭单元，这里也展示
        local childUnit = BuildFamilyUnit(child.id)
        if childUnit then
            unit.children[#unit.children + 1] = childUnit
        end
    end

    return unit
end

-- ============================================================================
-- 测量 FamilyUnit 所需宽度（递归）
-- ============================================================================

local function MeasureUnitWidth(unit)
    if not unit then return 0 end

    -- 本单元宽度 = 一对夫妻（或单人）的宽度
    local coupleW = CARD_W
    if unit.father and unit.mother then
        coupleW = CARD_W * 2 + COUPLE_GAP
    end

    -- 子女总宽度
    local childrenTotalW = 0
    for i, child in ipairs(unit.children) do
        childrenTotalW = childrenTotalW + MeasureUnitWidth(child)
        if i < #unit.children then
            childrenTotalW = childrenTotalW + SIBLING_GAP
        end
    end

    -- 取两者最大值
    return math.max(coupleW, childrenTotalW)
end

-- ============================================================================
-- 创建族人卡片（紧凑方块）
-- ============================================================================

local function CreateMemberCard(member, onClickMember)
    if not member then return nil end

    local isDead = not member.alive
    local genderColor = member.gender == "male"
        and { 70, 120, 180, 255 }
        or { 180, 70, 90, 255 }

    -- 状态颜色和图标
    local stateColor = Theme.TEXT_MUTED
    local stateIcon = ""
    local stateIcon2 = ""      -- 第二状态图标（生病时显示原状态）
    local stateColor2 = nil
    if member.state == "生病" then
        stateColor = Theme.RED; stateIcon = "病"
        -- 并列显示原状态
        local ps = member.prevState
        if ps == "读书" then stateIcon2 = "书"; stateColor2 = { 100, 180, 120, 255 }
        elseif ps == "从军" or ps == "出征" then stateIcon2 = "军"; stateColor2 = Theme.BLUE
        elseif ps == "经商" then stateIcon2 = "商"; stateColor2 = Theme.GOLD_DARK
        elseif ps == "打工" then stateIcon2 = "工"; stateColor2 = {180, 140, 60, 255}
        end
    elseif member.state == "读书" then stateColor = { 100, 180, 120, 255 }; stateIcon = "书"
    elseif member.state == "从军" or member.state == "出征" then stateColor = Theme.BLUE; stateIcon = "军"
    elseif member.state == "经商" then stateColor = Theme.GOLD_DARK; stateIcon = "商"
    elseif member.state == "打工" then stateColor = {180, 140, 60, 255}; stateIcon = "工"
    elseif member.state == "在家" then stateIcon = ""
    end

    -- 身份标签与颜色
    local identityText = member.identity
    if identityText == "白丁" then identityText = "" end

    -- 身份颜色：文官蓝紫、武将赤红、普通金色
    local identityColor = Theme.GOLD_DARK
    local identityEffect = ""
    local CIVIL_IDS = { ["童生"]=true, ["秀才"]=true, ["举人"]=true, ["进士"]=true, ["监生"]=true }
    local MILITARY_IDS = { ["士兵"]=true, ["把总"]=true, ["守备"]=true }
    if CIVIL_IDS[member.identity] then
        identityColor = { 80, 100, 180, 255 }  -- 蓝紫色（文官）
        if member.identity == "秀才" then identityEffect = "月+2望"
        elseif member.identity == "举人" then identityEffect = "月+5望+2银"
        elseif member.identity == "进士" then identityEffect = "月+10望+5银"
        elseif member.identity == "监生" then identityEffect = "月+2望"
        end
    elseif MILITARY_IDS[member.identity] then
        identityColor = { 180, 60, 60, 255 }  -- 赤红色（武将）
        if member.identity == "把总" then identityEffect = "月+8望+8银"
        elseif member.identity == "守备" then identityEffect = "月+15望+15银"
        end
    end

    local cardBg = isDead and { 230, 225, 218, 200 } or Theme.BG_CARD
    local cardBorder = isDead and { 200, 195, 185, 180 } or Theme.BORDER_GOLD
    local textAlpha = isDead and 120 or 255

    -- 头像圆形区域的尺寸和位置（对齐装饰边框中的金色圆环）
    local avatarSize = 36
    local avatarTop = 12
    local avatarBorderW = 0  -- 装饰边框自带圆环，不需要额外border

    return UI.Panel {
        width = CARD_W,
        height = CARD_H,
        position = "relative",
        overflow = "hidden",
        opacity = isDead and 0.55 or 1.0,
        alignItems = "center",
        onClick = function(self)
            if onClickMember then
                onClickMember(member)
            end
        end,
        children = {
            -- 底层：装饰边框图片（fill 整个卡片）
            UI.Panel {
                position = "absolute",
                left = 0, top = 0,
                width = "100%", height = "100%",
                backgroundImage = Theme.IMG.CARD_FRAME_MEMBER,
                backgroundFit = "fill",
            },
            -- 内容层：头像区 + 文字信息
            UI.Panel {
                width = "100%", height = "100%",
                alignItems = "center",
                overflow = "hidden",
                flexShrink = 1,
                paddingTop = avatarTop,
                paddingBottom = 3,
                paddingHorizontal = 2,
                gap = 0,
                children = (function()
                    local items = {}
                    -- 圆形头像区域（使用纸娃娃头像系统）
                    items[#items + 1] = UI.Panel {
                        width = avatarSize, height = avatarSize,
                        borderRadius = avatarSize / 2,
                        borderWidth = avatarBorderW,
                        borderColor = Theme.GOLD_DARK,
                        overflow = "hidden",
                        backgroundImage = AvatarSystem.GetAvatar(member),
                        backgroundFit = "cover",
                    }
                    -- 姓名（限1行，超出截断）
                    items[#items + 1] = UI.Label {
                        text = member.name,
                        fontSize = 10,
                        marginTop = 1,
                        fontColor = { Theme.TEXT_PRIMARY[1], Theme.TEXT_PRIMARY[2], Theme.TEXT_PRIMARY[3], textAlpha },
                        fontWeight = "bold",
                        textAlign = "center",
                        maxLines = 1,
                        maxWidth = CARD_W - 8,
                        flexShrink = 1,
                    }
                    -- 年龄（限1行）
                    items[#items + 1] = UI.Label {
                        text = isDead and "已故" or (member.age .. "岁"),
                        fontSize = 8,
                        fontColor = isDead and Theme.RED or { Theme.TEXT_SECONDARY[1], Theme.TEXT_SECONDARY[2], Theme.TEXT_SECONDARY[3], textAlpha },
                        textAlign = "center",
                        maxLines = 1,
                        flexShrink = 1,
                    }
                    -- 身份（限1行，文官蓝紫/武将赤红）
                    if identityText ~= "" then
                        items[#items + 1] = UI.Label {
                            text = identityText,
                            fontSize = 8,
                            fontColor = identityColor,
                            fontWeight = "bold",
                            textAlign = "center",
                            maxLines = 1,
                            maxWidth = CARD_W - 8,
                            flexShrink = 1,
                        }
                    end
                    -- 身份效果提示（如 月+5望+2银）
                    if identityEffect ~= "" then
                        items[#items + 1] = UI.Label {
                            text = identityEffect,
                            fontSize = 7,
                            fontColor = { identityColor[1], identityColor[2], identityColor[3], 160 },
                            textAlign = "center",
                            maxLines = 1,
                            maxWidth = CARD_W - 8,
                            flexShrink = 1,
                        }
                    end
                    -- 状态图标
                    if stateIcon ~= "" then
                        if stateIcon2 ~= "" then
                            -- 双状态并列（如 病+书）
                            items[#items + 1] = UI.Panel {
                                flexDirection = "row", justifyContent = "center", alignItems = "center", gap = 2,
                                maxWidth = CARD_W - 8, flexShrink = 1,
                                children = {
                                    UI.Label { text = stateIcon, fontSize = 8, fontColor = stateColor },
                                    UI.Label { text = "+", fontSize = 7, fontColor = Theme.TEXT_MUTED },
                                    UI.Label { text = stateIcon2, fontSize = 8, fontColor = stateColor2 },
                                },
                            }
                        else
                            items[#items + 1] = UI.Label {
                                text = stateIcon,
                                fontSize = 8,
                                fontColor = stateColor,
                                textAlign = "center",
                                maxLines = 1,
                                flexShrink = 1,
                            }
                        end
                    end
                    return items
                end)(),
            },
        },
    }
end

-- ============================================================================
-- 递归渲染 FamilyUnit（自上而下布局）
-- ============================================================================

--- 渲染策略：
--- 1. 每个 FamilyUnit 是一个纵向 Panel
--- 2. 顶部：夫妻行（水平排列）
--- 3. 中间：垂直连线（如果有子女）
--- 4. 底部：子女行（水平排列，每个子女是递归的 FamilyUnit）

local function RenderFamilyUnit(unit, onClickMember)
    if not unit then return UI.Panel {} end

    -- 夫妻行
    local coupleChildren = {}
    if unit.father then
        coupleChildren[#coupleChildren + 1] = CreateMemberCard(unit.father, onClickMember)
    end
    if unit.father and unit.mother then
        -- 婚姻连接符
        coupleChildren[#coupleChildren + 1] = UI.Panel {
            width = COUPLE_GAP,
            height = CARD_H,
            justifyContent = "center",
            alignItems = "center",
        }
    end
    if unit.mother then
        coupleChildren[#coupleChildren + 1] = CreateMemberCard(unit.mother, onClickMember)
    end

    local coupleRow = UI.Panel {
        flexDirection = "row",
        justifyContent = "center",
        alignItems = "flex-start",
        children = coupleChildren,
    }

    -- 如果没有子女，只返回夫妻行
    if #unit.children == 0 then
        return UI.Panel {
            alignItems = "center",
            children = { coupleRow },
        }
    end

    -- 有子女：添加连线和子女行

    -- 从夫妻中心向下的垂直连线
    local verticalLine = UI.Panel {
        width = LINE_W,
        height = GEN_GAP_Y / 2,
        backgroundColor = LINE_COLOR,
        alignSelf = "center",
    }

    -- 子女行：水平排列所有子女 FamilyUnit
    local childrenPanels = {}
    for i, childUnit in ipairs(unit.children) do
        childrenPanels[#childrenPanels + 1] = UI.Panel {
            alignItems = "center",
            children = {
                -- 从水平线到子女的垂直连线
                UI.Panel {
                    width = LINE_W,
                    height = GEN_GAP_Y / 2,
                    backgroundColor = LINE_COLOR,
                    alignSelf = "center",
                },
                -- 子女 FamilyUnit（递归）
                RenderFamilyUnit(childUnit, onClickMember),
            },
        }
        -- 兄弟间距
        if i < #unit.children then
            childrenPanels[#childrenPanels + 1] = UI.Panel {
                width = SIBLING_GAP,
            }
        end
    end

    local childrenRow = UI.Panel {
        flexDirection = "row",
        justifyContent = "center",
        alignItems = "flex-start",
        children = childrenPanels,
    }

    -- 水平连线（连接所有子女顶部）
    -- 宽度 = 从第一个子女中心到最后一个子女中心
    local horizontalLine = nil
    if #unit.children > 1 then
        horizontalLine = UI.Panel {
            alignSelf = "center",
            height = LINE_W,
            -- 宽度需要覆盖子女行，使用 stretch
            width = "100%",
            marginHorizontal = CARD_W / 2,
            backgroundColor = LINE_COLOR,
        }
    end

    return UI.Panel {
        alignItems = "center",
        children = {
            coupleRow,
            verticalLine,
            horizontalLine or UI.Panel { width = 0, height = 0 },
            childrenRow,
        },
    }
end

-- ============================================================================
-- 创建完整族谱树视图
-- ============================================================================

function FamilyTree.Create(onClickMember, onBattleClick, extraCallbacks)
    local s = GameData.state
    if not s then
        return UI.Panel {
            width = "100%", flexGrow = 1,
            justifyContent = "center", alignItems = "center",
            children = {
                UI.Label { text = "暂无族谱数据", fontSize = 14, fontColor = Theme.TEXT_MUTED },
            },
        }
    end

    -- 获取族长（优先使用 patriarchId，兼容旧存档自动查找）
    local patriarch = GameData.GetPatriarch()
    local patriarchId = patriarch and patriarch.id or nil

    if not patriarchId then
        return UI.Panel {
            width = "100%", flexGrow = 1,
            justifyContent = "center", alignItems = "center",
            children = {
                UI.Label { text = "未找到族长", fontSize = 14, fontColor = Theme.TEXT_MUTED },
            },
        }
    end

    -- 构建族谱树
    local rootUnit = BuildFamilyUnit(patriarchId)

    -- 渲染树
    local treeContent = RenderFamilyUnit(rootUnit, onClickMember)

    -- 包裹树形图（精简版，标题在顶栏显示）
    local treeWithHeader = UI.Panel {
        alignItems = "center",
        padding = TREE_PAD,
        gap = 8,
        children = {
            -- 紧凑图例
            UI.Panel {
                flexDirection = "row",
                gap = 10,
                alignItems = "center",
                children = {
                    UI.Panel {
                        flexDirection = "row", gap = 3, alignItems = "center",
                        children = {
                            UI.Panel { width = 10, height = 10, borderRadius = 5, backgroundColor = { 70, 120, 180, 255 } },
                            UI.Label { text = "男", fontSize = 11, fontColor = Theme.TEXT_MUTED },
                        },
                    },
                    UI.Panel {
                        flexDirection = "row", gap = 3, alignItems = "center",
                        children = {
                            UI.Panel { width = 10, height = 10, borderRadius = 5, backgroundColor = { 180, 70, 90, 255 } },
                            UI.Label { text = "女", fontSize = 11, fontColor = Theme.TEXT_MUTED },
                        },
                    },
                    UI.Panel {
                        flexDirection = "row", gap = 3, alignItems = "center",
                        children = {
                            UI.Panel { width = 10, height = 10, borderRadius = 5, backgroundColor = { 200, 195, 185, 200 }, opacity = 0.55 },
                            UI.Label { text = "故", fontSize = 11, fontColor = Theme.TEXT_MUTED },
                        },
                    },
                    UI.Label {
                        text = "第" .. FamilyTree.GetMaxGeneration() .. "代",
                        fontSize = 11,
                        fontColor = Theme.TEXT_MUTED,
                    },
                },
            },
            -- 树形图
            treeContent,
        },
    }

    -- 双轴可滚动容器
    local scrollView = UI.ScrollView {
        width = "100%",
        flexGrow = 1,
        flexBasis = 0,
        scrollX = true,
        scrollY = true,
        bounces = true,
        backgroundColor = { 0, 0, 0, 0 },
        children = {
            treeWithHeader,
        },
    }

    -- 族谱背景图层（cover 填满整个区域）
    local bgLayer = UI.Panel {
        position = "absolute",
        left = 0, top = 0,
        width = "100%", height = "100%",
        backgroundImage = Theme.IMG.BG_FAMILY_TREE,
        backgroundFit = "cover",
    }

    -- 浮动侧边按钮（右侧功能入口，按品级依次解锁）
    local floatingButtons = {}
    local cbs = extraCallbacks or {}
    local btnTopRight = 8  -- 右侧按钮纵向起始位置
    local btnTopLeft = 8   -- 左侧按钮纵向起始位置
    local btnSize = 80  -- 图标尺寸（放大至两倍可读性）
    local btnLabelH = 16 -- 文字标签高度
    local btnTotalH = btnSize + btnLabelH + 2  -- 图标+文字+间距
    local btnGap = btnTotalH + 4  -- 按钮间距
    local btnContainerW = 88  -- 容器宽度（比图标稍大，容纳文字）
    local sideBtnCount = 0  -- 按钮计数，超过3个排左侧
    local maxRightSide = 3  -- 右侧最多放3个按钮

    --- 创建带文字标签的侧边按钮
    local function makeSideBtn(img, label, callback)
        sideBtnCount = sideBtnCount + 1
        local isRight = sideBtnCount <= maxRightSide
        local props = {
            position = "absolute",
            width = btnContainerW,
            alignItems = "center",
            gap = 1,
            onClick = function(self) AudioManager.Click() callback() end,
            children = {
                UI.Panel {
                    width = btnSize, height = btnSize,
                    borderRadius = btnSize / 2,
                    backgroundImage = img, backgroundFit = "contain",
                },
                UI.Label {
                    text = label,
                    fontSize = 13,
                    fontColor = Theme.TEXT_PRIMARY,
                    textAlign = "center",
                },
            },
        }
        if isRight then
            props.right = 4
            props.top = btnTopRight
            btnTopRight = btnTopRight + btnGap
        else
            props.left = 4
            props.top = btnTopLeft
            btnTopLeft = btnTopLeft + btnGap
        end
        local btn = UI.Panel(props)
        floatingButtons[#floatingButtons + 1] = btn
    end

    -- 讨伐入口（世家/品级5解锁）
    if RivalClans.IsUnlocked() and onBattleClick then
        makeSideBtn(Theme.IMG.NAV_BATTLE, "讨伐", onBattleClick)
    end

    -- 钱庄·借贷（开局即可用）
    if cbs.onLoan then
        makeSideBtn(Theme.IMG.NAV_LOAN, "钱庄", cbs.onLoan)
    end

    -- 医馆（开局即可用）
    if cbs.onClinic then
        makeSideBtn(Theme.IMG.NAV_CLINIC, "医馆", cbs.onClinic)
    end

    -- 打工（开局即可用，常驻）
    if cbs.onLabor then
        makeSideBtn(Theme.IMG.NAV_LABOR, "打工", cbs.onLabor)
    end

    -- 祭天祈福（乡绅/品级3解锁）
    if s.clanRank >= 3 and cbs.onPray then
        makeSideBtn(Theme.IMG.NAV_PRAY, "祈福", cbs.onPray)
    end

    -- 教坊司·花魁（世家/品级5解锁）
    if s.clanRank >= 5 and cbs.onCourtesan then
        makeSideBtn(Theme.IMG.NAV_COURTESAN, "花魁", cbs.onCourtesan)
    end

    -- 天子诰封（勋贵/品级6解锁）
    if s.clanRank >= 6 and cbs.onImperialSeal then
        makeSideBtn(Theme.IMG.NAV_SEAL, "诰封", cbs.onImperialSeal)
    end

    -- 左下角宠物面板（绝对定位）
    local petPanel = FamilyTree._CreatePetPanel(s)

    -- 包裹一层容器以支持浮动按钮和背景图
    local allChildren = { bgLayer, scrollView, petPanel }
    for _, btn in ipairs(floatingButtons) do
        allChildren[#allChildren + 1] = btn
    end

    return UI.Panel {
        width = "100%",
        flexGrow = 1,
        flexBasis = 0,
        position = "relative",
        overflow = "hidden",
        children = allChildren,
    }
end

-- ============================================================================
-- 辅助方法
-- ============================================================================

function FamilyTree.GetMaxGeneration()
    local s = GameData.state
    if not s then return 1 end
    local maxGen = 1
    for _, m in ipairs(s.members) do
        if m.generation > maxGen then maxGen = m.generation end
    end
    return maxGen
end

-- ============================================================================
-- 宠物面板（族谱左下角，单只宠物，点击叫声，死亡显示）
-- ============================================================================

local PET_AVATARS = {
    dog = "image/pet_dog_avatar_20260512184033.png",
}

function FamilyTree._CreatePetPanel(s)
    if not s or not s.pet then
        return UI.Panel { width = 0, height = 0 }
    end

    local pet = s.pet
    local isDead = not pet.alive

    -- 计算陪伴时长
    local endYear = isDead and pet.deathYear or s.year
    local endMonth = isDead and pet.deathMonth or s.month
    local ageMonths = (endYear - pet.adoptYear) * 12 + (endMonth - pet.adoptMonth)
    local ageText = ageMonths < 12 and (ageMonths .. "月") or (math.floor(ageMonths / 12) .. "年")

    -- 内容子元素
    local cardChildren = {
        -- 宠物头像（圆形，点击播放叫声）
        UI.Panel {
            width = 36, height = 36,
            borderRadius = 18,
            borderWidth = 2,
            borderColor = isDead and { 160, 155, 145, 150 } or { 180, 140, 80, 200 },
            overflow = "hidden",
            opacity = isDead and 0.45 or 1.0,
            backgroundImage = PET_AVATARS[pet.type] or PET_AVATARS.dog,
            backgroundFit = "cover",
            onClick = function(self)
                if not isDead then
                    -- 播放汪汪叫声
                    local sound = cache:GetResource("Sound", "audio/sfx/dog_bark.ogg")
                    if sound then
                        sound.looped = false
                        local soundNode = s._petSoundNode
                        if not soundNode then
                            soundNode = Scene():CreateChild("PetSound")
                            s._petSoundNode = soundNode
                        end
                        local src = soundNode:GetOrCreateComponent("SoundSource")
                        src:Play(sound)
                        src.gain = 0.6
                    end
                end
            end,
        },
        -- 名字
        UI.Label {
            text = pet.name,
            fontSize = 9,
            fontColor = isDead and Theme.TEXT_MUTED or Theme.TEXT_PRIMARY,
            textAlign = "center",
            maxLines = 1,
        },
    }

    if isDead then
        -- 死亡状态：显示死亡信息
        cardChildren[#cardChildren + 1] = UI.Label {
            text = "已离世",
            fontSize = 7,
            fontColor = Theme.RED,
            textAlign = "center",
            maxLines = 1,
        }
        cardChildren[#cardChildren + 1] = UI.Label {
            text = "陪伴" .. ageText,
            fontSize = 7,
            fontColor = Theme.TEXT_MUTED,
            textAlign = "center",
            maxLines = 1,
        }
    else
        -- 存活状态
        cardChildren[#cardChildren + 1] = UI.Label {
            text = "养了" .. ageText,
            fontSize = 7,
            fontColor = Theme.TEXT_MUTED,
            textAlign = "center",
            maxLines = 1,
        }
        cardChildren[#cardChildren + 1] = UI.Label {
            text = "月耗粮1",
            fontSize = 7,
            fontColor = { 180, 120, 60, 180 },
            textAlign = "center",
            maxLines = 1,
        }
    end

    return UI.Panel {
        position = "absolute",
        left = 12, bottom = 18,
        width = 56,
        alignItems = "center",
        gap = 2,
        paddingVertical = 5,
        paddingHorizontal = 3,
        backgroundColor = { 245, 240, 230, 200 },
        borderRadius = 6,
        borderWidth = 1,
        borderColor = isDead and { 180, 170, 160, 120 } or Theme.BORDER_GOLD,
        children = cardChildren,
    }
end

return FamilyTree
