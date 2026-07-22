package com.arktools.daming.v3.logic

import com.arktools.daming.v3.data.V3HexArms
import com.arktools.daming.v3.data.V3HexBattleState
import com.arktools.daming.v3.data.V3MonthlyCard
import com.arktools.daming.v3.data.V3ArmyRoster
import com.arktools.daming.v3.data.V3Content
import com.arktools.daming.v3.data.V3EquipmentQuality
import com.arktools.daming.v3.data.V3EquipmentSlot
import com.arktools.daming.v3.data.V3GameState
import com.arktools.daming.v3.data.V3Gender
import com.arktools.daming.v3.data.V3Person
import com.arktools.daming.v3.data.V3RegionStatus
import com.arktools.daming.v3.data.V3RelationBand
import com.arktools.daming.v3.data.V3Route
import com.arktools.daming.v3.data.V3Trait
import com.arktools.daming.v3.data.V3TroopType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class V3GameEngineTest {
    @Test
    fun completeCardAndVisitorContentIsReachable() {
        assertTrue(V3Content.allMonthlyCards.size >= 60)
        assertTrue(V3Content.visitors.size >= 30)
        assertEquals(5, V3Content.additionalMonthlyCards.count { it.crisisLevel == 1 })
        assertEquals(5, V3Content.additionalMonthlyCards.count { it.crisisLevel == 2 })
        assertEquals(5, V3Content.additionalMonthlyCards.count { it.crisisLevel == 3 })
        assertEquals(6, V3Content.roots.size)
        V3Content.roots.forEach { root ->
            val state = V3Content.newGame(root, "江南水乡", "耕读传家", "饥荒将至")
            assertTrue(state.originTraits.isNotEmpty())
        }
    }

    @Test
    fun visitorIntroCardCanStartEveryVisitorChain() {
        val base = V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税")
            .copy(clanRank = 4, year = 1610)
        val refreshed = V3CardEngine.refreshMonth(base, emptyList())
        val intros = refreshed.activeCards.filter { it.id.startsWith("visitor_intro_") }
        assertTrue(intros.isNotEmpty())
        val intro = intros.first()
        val resolved = V3CardEngine.choose(refreshed, intro.id, "receive")
        assertNotNull(resolved)
        val visitorId = intro.id.removePrefix("visitor_intro_")
        assertEquals(1, resolved?.state?.visitorProgress?.get(visitorId))
    }

    @Test
    fun visitorChoicesRemainPlayableAndWriteAContinuingStory() {
        val base = V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税")
            .copy(year = 1610, month = 1, clanRank = 4)
        val refreshed = V3CardEngine.refreshMonth(base, emptyList())
        val visitor = refreshed.activeCards.first { it.id.startsWith("visitor_intro_") }
        assertEquals(com.arktools.daming.v3.data.V3CardPool.Visitor, visitor.pool)
        val resolution = requireNotNull(V3CardEngine.choose(refreshed, visitor.id, visitor.choices.first().id))
        assertTrue(resolution.message.isNotBlank())
        assertTrue(resolution.state.visitorProgress.values.any { it >= 1 })
        assertTrue(resolution.state.biography.any { it.contains("初次") })
    }

    @Test
    fun tutorialSeparatesExplanationsFromRequiredGameplayActions() {
        val state = V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税")

        assertEquals(32, V3GameController.TUTORIAL_STEP_COUNT)
        assertEquals(12_000L, V3GameController.TUTORIAL_EXPLANATION_AUTO_ADVANCE_MILLIS)
        assertEquals(0, state.tutorialStep)
        assertTrue(V3GameController.tutorialStepRequiresAction(0).not())
        assertTrue(V3GameController.tutorialStepRequiresAction(1).not())
        assertTrue(V3GameController.tutorialStepRequiresAction(5))
        assertTrue(V3GameController.tutorialStepRequiresAction(14))
        assertTrue(V3GameController.tutorialStepRequiresAction(16))
        assertTrue(V3GameController.tutorialStepRequiresAction(17))
        assertTrue(V3GameController.tutorialStepRequiresAction(18))
        assertTrue(V3GameController.tutorialStepRequiresAction(31).not())
        assertEquals(
            setOf(5, 8, 11, 14, 15, 16, 17, 18, 21, 27, 28, 29),
            V3GameController.TUTORIAL_ACTION_STEPS
        )
        assertTrue(
            V3GameController.TUTORIAL_ACTION_STEPS.all {
                it in 0 until V3GameController.TUTORIAL_STEP_COUNT
            }
        )
    }

    @Test
    fun originTraitsChangeCardResolutionByOrigin() {
        val card = V3Content.additionalMonthlyCards.first { it.id == "trade_01" }
        val merchant = V3Content.newGame("江南商族", "江南水乡", "重商逐利", "商路断绝")
        val plain = merchant.copy(originTraits = emptyList())
        val withTrait = V3CardEngine.resolve(merchant, card, card.choices.first(), null).state
        val withoutTrait = V3CardEngine.resolve(plain, card, card.choices.first(), null).state
        assertEquals(withoutTrait.silver + 5, withTrait.silver)
    }

    @Test
    fun extremeRelationsMapToSixBandsAndTriggerAttitudeEvent() {
        assertEquals(V3RelationBand.Hostile, V3EventEngine.relationBand(-70))
        assertEquals(V3RelationBand.Estranged, V3EventEngine.relationBand(-40))
        assertEquals(V3RelationBand.Distant, V3EventEngine.relationBand(0))
        assertEquals(V3RelationBand.Friendly, V3EventEngine.relationBand(20))
        assertEquals(V3RelationBand.Close, V3EventEngine.relationBand(50))
        assertEquals(V3RelationBand.Allied, V3EventEngine.relationBand(80))

        val hostile = V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税")
            .copy(
                year = 1610,
                month = 2,
                relations = V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税").relations.copy(yamen = -70)
            )
        assertTrue(V3EventEngine.generateEvent(hostile)?.title?.contains("敌对") == true)

        val allied = hostile.copy(
            completedStoryFlags = emptyList(),
            relations = hostile.relations.copy(yamen = 80)
        )
        assertTrue(V3EventEngine.generateEvent(allied)?.title?.contains("同盟") == true)
    }
    @Test
    fun finalFailureKindsAndGenealogyPrefaceAreRecorded() {
        val base = V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税")
        assertEquals("从龙失败", V3GameEngine.failureKind(base.copy(
            militia = 0,
            army = V3ArmyRoster(),
            relations = base.relations.copy(garrison = -10),
            routeScores = base.routeScores + (V3Route.Loyalist to 80)
        )))
        assertEquals("抗清殉族", V3GameEngine.failureKind(base.copy(
            militia = 0,
            army = V3ArmyRoster(),
            completedStoryFlags = listOf("抗清守约")
        )))
        assertEquals("出海覆舟", V3GameEngine.failureKind(base.copy(
            routeScores = base.routeScores + (V3Route.Overseas to 80),
            sites = base.sites.map { site -> if (site.id == "dock") site.copy(risk = 90) else site }
        )))
        val preface = V3GameEngine.genealogyPreface(base.copy(plaques = listOf("义门")))
        assertTrue(preface.contains("氏族谱序"))
        assertTrue(preface.contains("义门"))
        assertTrue(V3GameEngine.endingChronicle(base).isNotEmpty())
    }

    @Test
    fun recurringItemsApplyOnlyDuringMonthlySettlement() {
        val base = V3Content.newGame("江南商族", "江南水乡", "重商逐利", "商路断绝")
        val withItem = base.copy(inventory = listOf("new_farming_manual"))
        val once = V3CardEngine.applyInventoryEffects(withItem)
        assertEquals(withItem.grain + 5, once.grain)
        val report = V3GameEngine.advanceMonth(withItem)
        assertTrue(report.nextState.grain >= base.grain - 100)
        assertEquals(1, report.nextState.inventory.count { it == "new_farming_manual" })
    }

    @Test
    fun visitorChainAdvancesAndDeliversSecondChapterGift() {
        val base = V3Content.newGame("江南商族", "江南水乡", "重商逐利", "商路断绝")
            .copy(year = 1610, month = 1, clanRank = 4)
        val first = V3Content.allMonthlyCards.first { it.id == "visitor_xu_xiake" }
        val firstChoice = first.choices.first()
        val afterFirst = V3CardEngine.resolve(base, first, firstChoice, null).state
        assertEquals(1, afterFirst.visitorProgress["xu_xiake"])
        val refreshed = V3CardEngine.refreshMonth(afterFirst, emptyList())
        val chain = refreshed.activeCards.firstOrNull { it.id == "visitor_chain_xu_xiake_2" }
        assertNotNull(chain)
        val afterSecond = V3CardEngine.choose(refreshed, requireNotNull(chain).id, "receive")?.state
        assertNotNull(afterSecond)
        assertEquals(2, afterSecond?.visitorProgress?.get("xu_xiake"))
        assertTrue(afterSecond?.inventory?.contains("mountain_route") == true)
    }

    @Test
    fun oncePerGenerationCardCanReturnAfterSuccessionButNotWithinGeneration() {
        val card = V3MonthlyCard(
            id = "generation_test",
            pool = com.arktools.daming.v3.data.V3CardPool.Annual,
            title = "代际清账",
            body = "账本翻到下一页。",
            oncePerGeneration = true,
            choices = listOf(com.arktools.daming.v3.data.V3CardChoice("ok", "清账"))
        )
        val base = V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税")
        val played = V3CardEngine.resolve(base, card, card.choices.first(), null).state
        assertTrue("generation_test" in played.seenCardGenerations[1].orEmpty())
        val sameGeneration = V3CardEngine.refreshMonth(played, listOf(card))
        assertTrue(sameGeneration.activeCards.none { it.id == card.id })
        val heir = V3Person(2, "李承业", 20, "主房", "族人", V3Trait.Honest, 30, 20, 30, 40, 80, generation = 2, merit = 20, ageMonths = 240, surname = "李")
        val succeeded = V3GameEngine.succeedPatriarch(
            played.copy(pendingSuccession = true, people = played.people + heir),
            heir.id
        )
        val nextGeneration = V3CardEngine.refreshMonth(succeeded, listOf(card))
        assertTrue(nextGeneration.activeCards.any { it.id == card.id })
    }

    @Test
    fun automaticPlaqueAndFailureKindAreRecorded() {
        val base = V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税")
        val state = base.copy(
            patriarch = com.arktools.daming.v3.data.V3Patriarch(conduct = 90, stewardship = 90, prestige = 90, health = 80),
            influence = 90,
            cohesion = 90,
            relations = com.arktools.daming.v3.data.V3Relations(villagers = 50),
            clanRank = 3
        )
        val next = V3GameEngine.advanceMonth(state).nextState
        assertTrue(next.plaques.contains("耕读之家"))
        assertTrue(next.plaques.contains("义门"))
        assertTrue(next.plaques.contains("望族"))

        val failed = base.copy(grain = -300)
        assertEquals("举族逃荒", V3GameEngine.failureKind(failed))
        assertEquals("举族逃荒", V3GameEngine.finalizeEnding(failed).failureKind)
    }
    @Test
    fun allStartCombinationsReachAutomaticEndingWithValidState() {
        var combinations = 0
        for (root in V3Content.roots) {
            for (county in V3Content.counties) {
                for (creed in V3Content.creeds) {
                    for (crisis in V3Content.crises) {
                        var state = V3Content.newGame(root, county, creed, crisis, "沈", "守正")
                        assertStateInvariants(state)
                        var months = 0
                        while (!V3GameEngine.shouldAutoEnd(state) && months < 600) {
                            state = V3GameEngine.advanceMonth(state).nextState
                            assertStateInvariants(state)
                            months += 1
                        }
                        assertTrue("$root/$county/$creed/$crisis 未触发终局", V3GameEngine.shouldAutoEnd(state))
                        assertTrue("终局推进月份异常：$months", months in 1..600)
                        assertNotNull(V3GameEngine.finalizeEnding(state))
                        combinations += 1
                    }
                }
            }
        }
        assertEquals(
            V3Content.roots.size * V3Content.counties.size * V3Content.creeds.size * V3Content.crises.size,
            combinations
        )
    }

    @Test
    fun marriagePregnancyBirthAndAdultGrowthFormAConsistentFamily() {
        var state = V3Content.newGame("江南商族", "江南水乡", "重商逐利", "商路断绝", "沈", "守正")
            .copy(silver = 1_000, grain = 1_000, influence = 100)
        val founder = state.people.single()
        val candidate = V3GameEngine.marriageCandidatesFor(founder, state).first()
        state = V3GameEngine.marry(state, candidate.id, founder.id)
        assertEquals(2, V3GameEngine.alivePeople(state).size)

        repeat(2) { state = V3GameEngine.advanceMonth(state).nextState }
        val wife = state.people.first { it.gender == V3Gender.Female }
        assertNotNull("婚后两个月应确认喜脉", wife.pregnancyDueMonth)

        repeat(10) { state = V3GameEngine.advanceMonth(state).nextState }
        assertEquals(3, V3GameEngine.alivePeople(state).size)
        val child = state.people.first { it.generation == 2 }
        val father = state.people.first { it.id == child.parentId }
        val mother = state.people.first { it.id == child.motherId }
        assertTrue(child.id in father.childrenIds)
        assertTrue(child.id in mother.childrenIds)
        assertEquals(null, mother.pregnancyDueMonth)

        val almostAdult = child.copy(age = 15, ageMonths = 16 * 12 - 1)
        state = state.copy(people = state.people.map { if (it.id == child.id) almostAdult else it })
        state = V3GameEngine.advanceMonth(state).nextState
        assertEquals(16, state.people.first { it.id == child.id }.age)
    }

    @Test
    fun battleAssignmentsNeverCreateTroopsAndEquipmentLosesDurability() {
        val base = V3Content.newGame("边地军户", "西北边堡", "聚族自保", "流寇逼近", "沈", "守正")
        val people = listOf(
            base.people.first().copy(id = 1, age = 30, ageMonths = 360),
            V3Person(2, "沈承安", 28, "主房", "族人", V3Trait.Martial, 15, 55, 20, 25, 90, ageMonths = 336, surname = "沈"),
            V3Person(3, "沈景和", 26, "主房", "族人", V3Trait.Fierce, 18, 50, 18, 22, 88, ageMonths = 312, surname = "沈")
        )
        var state = base.copy(
            clanRank = 2,
            people = people,
            nextPersonId = 4,
            silver = 2_000,
            grain = 2_000,
            militia = 17,
            army = V3ArmyRoster(militia = 17)
        )
        state = V3GameEngine.buyEquipment(state, V3EquipmentSlot.Weapon, V3EquipmentQuality.Common)
        val equipmentId = state.equipment.single().id
        state = V3GameEngine.equipEquipment(state, equipmentId, 1)
        state = V3GameEngine.startBattle(state)
        for (person in people) state = V3GameEngine.selectBattlePerson(state, person.id)
        state = V3GameEngine.confirmBattleLineup(state)

        val battle = requireNotNull(state.battleState)
        assertEquals(17, battle.allies.sumOf { it.troopCount })
        assertTrue(battle.allies.all { it.troopCount > 0 })

        state = V3GameEngine.resolveBattle(state)
        assertEquals(null, state.battleState)
        assertTrue(state.equipment.single().durability < state.equipment.single().maxDurability)
        assertEquals(state.army.total(), state.militia)
    }

    @Test
    fun fullDurabilityRepairDoesNotChargeAndMissingActionsReturnFeedback() {
        var state = V3Content.newGame("江南商族", "江南水乡", "重商逐利", "商路断绝")
            .copy(clanRank = 2, silver = 1_000)
        state = V3GameEngine.buyEquipment(state, V3EquipmentSlot.Armor, V3EquipmentQuality.Common)
        val item = state.equipment.single()
        val silverBefore = state.silver
        state = V3GameEngine.repairEquipment(state, item.id)
        assertEquals(silverBefore, state.silver)
        assertTrue(state.pendingReports.single().contains("无需修复"))

        state = V3GameEngine.equipEquipment(state, "missing", 1)
        assertTrue(state.pendingReports.single().contains("没有找到"))
    }

    @Test
    fun progressionChaptersFollowRealRankAndEndgameState() {
        val base = V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税")
        assertEquals(V3Chapter.Founding, V3ProgressionEngine.currentChapter(base))

        val rooted = base.copy(clanRank = 2, year = 1602)
        assertEquals(V3Chapter.Rooting, V3ProgressionEngine.currentChapter(rooted))

        val expanded = rooted.copy(clanRank = 3, year = 1605)
        assertEquals(V3Chapter.Expansion, V3ProgressionEngine.currentChapter(expanded))

        val countyPower = expanded.copy(clanRank = 4)
        assertEquals(V3Chapter.CountyRivalry, V3ProgressionEngine.currentChapter(countyPower))

        val choosing = countyPower.copy(clanRank = 5, year = 1638)
        assertEquals(V3Chapter.ChoosingPath, V3ProgressionEngine.currentChapter(choosing))

        val final = choosing.copy(year = 1642)
        assertEquals(V3Chapter.FinalLegacy, V3ProgressionEngine.currentChapter(final))
    }

    @Test
    fun foundingQuestUsesExactPromotionThresholds() {
        val base = V3Content.newGame("江南商族", "江南水乡", "重商逐利", "商路断绝")
            .copy(year = 1601, month = 9, silver = 179, grain = 260, influence = 45)
        val child = V3Person(
            id = 2,
            name = "李承业",
            age = 1,
            branch = "主房",
            identity = "嫡长子",
            trait = V3Trait.Honest,
            study = 5,
            martial = 5,
            commerce = 5,
            diplomacy = 5,
            loyalty = 90,
            generation = 2,
            parentId = 1,
            ageMonths = 12,
            surname = base.surname
        )
        val secondChild = child.copy(id = 3, name = "李承平")
        val state = base.copy(
            people = base.people + child + secondChild,
            nextPersonId = 4,
            sites = base.sites.map { site ->
                if (site.id == "market") site.copy(level = 1) else site
            }
        )
        val snapshot = V3ProgressionEngine.snapshot(state)
        assertEquals(V3Chapter.Founding, snapshot.chapter)
        assertTrue(snapshot.mainQuest.conditions.first { it.label == "银两" }.satisfied.not())
        assertEquals("银两还差1", snapshot.mainQuest.blockers.first { it.startsWith("银两") })
        assertTrue(V3GameEngine.canRankUp(state).not())

        val ready = state.copy(silver = 180)
        assertTrue(V3GameEngine.canRankUp(ready))
        assertTrue(V3ProgressionEngine.snapshot(ready).mainQuest.completed)
    }

    @Test
    fun chapterRewardsCanOnlyBeClaimedOnce() {
        val state = V3Content.newGame("江南商族", "江南水乡", "重商逐利", "商路断绝")
            .copy(clanRank = 2, silver = 100, grain = 100, cohesion = 50)
        val reward = requireNotNull(V3ProgressionEngine.claimableReward(state))
        assertEquals(V3Chapter.Founding, reward.chapter)

        val claimed = V3ProgressionEngine.claimChapterReward(state, V3Chapter.Founding)
        assertEquals(140, claimed.silver)
        assertEquals(160, claimed.grain)
        assertEquals(54, claimed.cohesion)
        assertTrue(V3Chapter.Founding.name in claimed.claimedChapterRewards)

        val claimedAgain = V3ProgressionEngine.claimChapterReward(claimed, V3Chapter.Founding)
        assertEquals(claimed.silver, claimedAgain.silver)
        assertEquals(claimed.grain, claimedAgain.grain)
        assertTrue(claimedAgain.pendingReports.single().contains("已经领取"))
    }

    @Test
    fun criticalResourceRiskOverridesMainQuestRecommendation() {
        val state = V3Content.newGame("寒门佃户", "中原灾地", "明哲保身", "饥荒将至")
            .copy(
                grain = 20,
                sites = V3Content.initialSites.map { site ->
                    if (site.id == "farmland") site.copy(risk = 70) else site
                }
            )
        val snapshot = V3ProgressionEngine.snapshot(state)
        assertEquals(V3ActionPriority.Critical, snapshot.primaryAction.priority)
        assertTrue(snapshot.primaryAction.title.contains("粮") || snapshot.primaryAction.title.contains("治理"))
    }

    @Test
    fun controlledHomeCountyDoesNotSatisfyExternalExpansionMilestone() {
        val base = V3Content.newGame("边地军户", "西北边堡", "聚族自保", "流寇逼近")
            .copy(clanRank = 3, year = 1606)
        assertEquals(V3Chapter.Expansion, V3ProgressionEngine.currentChapter(base))
        assertTrue(V3ProgressionEngine.snapshot(base).mainQuest.conditions.first { it.label == "控制县外地域" }.satisfied.not())

        val withExternal = base.copy(
            worldRegions = base.worldRegions.map { region ->
                if (region.id == "neighbor_county") region.copy(status = V3RegionStatus.Controlled, control = 85) else region
            }
        )
        assertTrue(V3ProgressionEngine.snapshot(withExternal).mainQuest.conditions.first { it.label == "控制县外地域" }.satisfied)
    }

    @Test
    fun rankThreeCanStartFirstExternalConquest() {
        val state = V3Content.newGame("边地军户", "西北边堡", "聚族自保", "流寇逼近")
            .copy(
                clanRank = 3,
                militia = 90,
                army = V3ArmyRoster(militia = 90)
            )
        assertTrue(V3GameEngine.isUnlocked(state, "Conquest"))
        assertEquals(0, V3GameEngine.externalControlledRegionCount(state))

        val prepared = V3GameEngine.startConquest(state, "neighbor_county")
        assertNotNull(prepared.conquestState)
        assertEquals("neighbor_county", prepared.conquestState?.regionId)
    }

    @Test
    fun chapterMilestoneIsPersistedAndCannotRepeatAfterLogTruncation() {
        val base = V3Content.newGame("边地军户", "西北边堡", "聚族自保", "流寇逼近")
            .copy(
                clanRank = 2,
                year = 1602,
                month = 5,
                silver = 1_000,
                grain = 1_000,
                cohesion = 80,
                sites = V3Content.initialSites.map { it.copy(risk = 10) }
            )
        val milestone = requireNotNull(V3EventEngine.generateEvent(base))
        assertEquals("小族立约", milestone.title)

        val resolved = V3EventEngine.choose(
            base.copy(activeEvent = milestone),
            milestone.choices.first()
        )
        assertTrue(resolved.seenChapterMilestones.isNotEmpty())

        val afterLongHistory = resolved.copy(
            activeEvent = null,
            eventLog = List(120) { index -> "普通纪要$index" }
        )
        assertTrue(V3EventEngine.generateEvent(afterLongHistory)?.title != "小族立约")
    }

    @Test
    fun monthlyReportKeepsStructuredSummaryAndFullLedger() {
        val state = V3Content.newGame("江南商族", "江南水乡", "重商逐利", "商路断绝")
        val report = V3GameEngine.advanceMonth(state)

        assertTrue(report.conclusion.isNotBlank())
        assertTrue(report.resourceLines.isNotEmpty())
        assertTrue(report.goalLines.any { it.contains("章节") })
        assertTrue(report.lines.any { it.contains("生活消耗") })
        assertTrue(report.lines.any { it.contains("本月账本") })
    }

    @Test
    fun oldStateJsonLoadsWithNewProgressionFieldsDefaulted() {
        val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }
        val state = V3Content.newGame("江南商族", "江南水乡", "重商逐利", "商路断绝")
        val legacyJson = json.encodeToString(state)
            .replace(",\"claimedChapterRewards\":[]", "")
            .replace(",\"seenChapterMilestones\":[]", "")
            .replace(",\"completedStoryFlags\":[]", "")
            .replace(",\"accordRoute\":null", "")

        val restored = json.decodeFromString<V3GameState>(legacyJson)
        assertTrue(restored.claimedChapterRewards.isEmpty())
        assertTrue(restored.seenChapterMilestones.isEmpty())
        assertTrue(restored.completedStoryFlags.isEmpty())
        assertEquals(state.surname, restored.surname)
    }

    @Test
    fun finalActShowsExactCountdownUntilAutomaticEnding() {
        val routeScores = V3Content.initialRouteScores + (V3Route.Scholar to 90)
        val base = V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税")
            .copy(
                clanRank = 5,
                year = 1642,
                month = 1,
                cohesion = 80,
                routeScores = routeScores
            )
        val opening = V3ProgressionEngine.snapshot(base)
        assertEquals(V3Chapter.FinalLegacy, opening.chapter)
        assertTrue(opening.mainQuest.description.contains("还剩28个月"))
        assertEquals(0, opening.mainQuest.conditions.first { it.label == "终章历程（月）" }.current)
        assertTrue(V3GameEngine.shouldAutoEnd(base).not())

        val eve = base.copy(year = 1644, month = 4)
        val finalMonth = V3ProgressionEngine.snapshot(eve)
        assertTrue(finalMonth.mainQuest.description.contains("还剩1个月"))
        assertEquals(27, finalMonth.mainQuest.conditions.first { it.label == "终章历程（月）" }.current)
        assertTrue(V3GameEngine.shouldAutoEnd(eve).not())

        val endingMonth = eve.copy(month = 5)
        assertTrue(V3GameEngine.shouldAutoEnd(endingMonth))
        assertEquals(28, V3ProgressionEngine.snapshot(endingMonth).mainQuest.conditions.first { it.label == "终章历程（月）" }.current)
    }

    @Test
    fun highUnificationOnlyStartsFinalActForWarlordRoute() {
        val base = V3Content.newGame("江南商族", "江南水乡", "重商逐利", "商路断绝")
            .copy(clanRank = 5, year = 1638, unificationProgress = 75)
        assertEquals(V3Chapter.ChoosingPath, V3ProgressionEngine.currentChapter(base))

        val warlord = base.copy(
            militia = 240,
            army = V3ArmyRoster(militia = 240),
            routeScores = V3Content.initialRouteScores + (V3Route.Warlord to 90),
            worldRegions = base.worldRegions.mapIndexed { index, region ->
                if (index < 6) region.copy(status = V3RegionStatus.Controlled, control = 90) else region
            }
        )
        assertTrue(V3ProgressionEngine.snapshot(warlord.copy(unificationProgress = 60)).mainQuest.completed)
        assertEquals(V3Chapter.FinalLegacy, V3ProgressionEngine.currentChapter(warlord))
    }

    @Test
    fun regionAccordRequiresControlAndCreatesPersistentRouteYield() {
        val lowControl = V3Content.newGame("江南商族", "江南水乡", "重商逐利", "商路断绝")
            .copy(
                clanRank = 3,
                year = 1606,
                month = 5,
                seenChapterMilestones = listOf("rooting_covenant", "expansion_alliance"),
                worldRegions = V3Content.initialWorldRegions.map { region ->
                    if (region.id == "neighbor_county") {
                        region.copy(
                            status = V3RegionStatus.Influenced,
                            control = 79
                        )
                    } else {
                        region
                    }
                }
            )
        assertTrue(
            V3EventEngine.generateEvent(lowControl)?.title !=
                "临水县归附条约"
        )

        val ready = lowControl.copy(
            worldRegions = lowControl.worldRegions.map { region ->
                if (region.id == "neighbor_county") {
                    region.copy(control = 80)
                } else {
                    region
                }
            }
        )
        val accord = requireNotNull(V3EventEngine.generateEvent(ready))
        assertEquals("临水县归附条约", accord.title)
        val merchantChoice =
            accord.choices.first { it.route == V3Route.Merchant }
        val resolved = V3EventEngine.choose(
            ready.copy(activeEvent = accord),
            merchantChoice
        )
        val resolvedRegion = resolved.worldRegions.first {
            it.id == "neighbor_county"
        }
        assertTrue(
            "region_accord_neighbor_county" in
                resolved.completedStoryFlags
        )
        assertEquals(V3RegionStatus.Pacified, resolvedRegion.status)
        assertEquals(V3Route.Merchant, resolvedRegion.accordRoute)
        assertTrue(
            V3EventEngine.generateEvent(
                resolved.copy(activeEvent = null)
            )?.title != "临水县归附条约"
        )

        val forecast = V3GameEngine.monthlyForecast(resolved)
        val withoutAccord = V3GameEngine.monthlyForecast(
            resolved.copy(
                worldRegions = resolved.worldRegions.map { region ->
                    if (region.id == "neighbor_county") {
                        region.copy(accordRoute = null)
                    } else {
                        region
                    }
                }
            )
        )
        assertTrue(forecast.silverIncome > withoutAccord.silverIncome)
        assertEquals(
            forecast.silverIncome - withoutAccord.silverIncome,
            V3GameEngine.advanceMonth(resolved).nextState.silver -
                V3GameEngine.advanceMonth(
                    resolved.copy(
                        worldRegions =
                            resolved.worldRegions.map { region ->
                                if (region.id == "neighbor_county") {
                                    region.copy(accordRoute = null)
                                } else {
                                    region
                                }
                            }
                    )
                ).nextState.silver
        )
    }

    @Test
    fun adultHeirCanFoundBranchWithPersistentMonthlyBenefit() {
        val base = V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税")
        val heir = V3Person(
            id = 2,
            name = "李承文",
            age = 20,
            branch = "主房",
            identity = "长子",
            trait = V3Trait.Studious,
            study = 70,
            martial = 20,
            commerce = 25,
            diplomacy = 45,
            loyalty = 88,
            generation = 2,
            merit = 30,
            ageMonths = 240,
            surname = base.surname
        )
        val state = base.copy(
            clanRank = 3,
            year = 1608,
            month = 5,
            people = base.people + heir,
            nextPersonId = 3,
            seenChapterMilestones = listOf("rooting_covenant", "expansion_alliance")
        )
        val event = requireNotNull(V3EventEngine.generateEvent(state))
        assertTrue(event.title.contains("请命立支"))
        val choice = event.choices.first { it.label == "立书香房" }
        val branched = V3EventEngine.choose(state.copy(activeEvent = event), choice)
        assertTrue(branched.branches.any { it.name == "书香房" })
        assertEquals("书香房", branched.people.first { it.id == heir.id }.branch)

        val forecast = V3GameEngine.monthlyForecast(branched)
        val withoutBranch = V3GameEngine.monthlyForecast(
            branched.copy(branches = branched.branches.filterNot { it.name == "书香房" })
        )
        assertEquals(withoutBranch.influenceIncome + 1, forecast.influenceIncome)
    }

    @Test
    fun failureEndingDoesNotRequireFinalDecision() {
        val failed = V3Content.newGame(
            "寒门佃户",
            "中原灾地",
            "明哲保身",
            "饥荒将至"
        ).copy(
            year = 1643,
            month = 8,
            grain = -300,
            seenChapterMilestones = listOf(
                "rooting_covenant",
                "expansion_alliance",
                "county_rivalry",
                "route_council"
            )
        )

        assertTrue(V3GameEngine.isFailureEnding(failed))
        assertTrue(V3GameEngine.isTimelineEnding(failed).not())
        assertTrue(V3GameEngine.shouldAutoEnd(failed))
        assertTrue(
            V3GameEngine.finalizeEnding(failed).body.contains(
                "粮仓长期亏空"
            )
        )
    }

    @Test
    fun timelineEndingCreatesDirectFinalDecisionWithoutOlderMilestones() {
        val timeline = V3Content.newGame(
            "没落士族",
            "江南水乡",
            "耕读传家",
            "官府催税"
        ).copy(
            year = 1644,
            month = 5,
            silver = 1_000,
            grain = 1_000,
            cohesion = 80
        )

        assertTrue(V3GameEngine.isTimelineEnding(timeline))
        assertTrue(V3GameEngine.isFailureEnding(timeline).not())
        val event = requireNotNull(
            V3EventEngine.finalDecisionEvent(timeline)
        )
        assertEquals("甲申前夜", event.title)
        val resolved = V3EventEngine.choose(
            timeline.copy(activeEvent = event),
            event.choices.first()
        )
        assertTrue("final_eve" in resolved.seenChapterMilestones)
    }

    @Test
    fun branchBenefitUsesMainIdInsteadOfListPosition() {
        val base = V3Content.newGame(
            "江南商族",
            "江南水乡",
            "重商逐利",
            "商路断绝"
        )
        val main = base.branches.first { it.id == "main" }
        val merchantBranch = main.copy(
            id = "merchant_2",
            name = "商务房",
            focus = V3Route.Merchant
        )
        val reordered = base.copy(
            branches = listOf(merchantBranch, main)
        )
        val forecast = V3GameEngine.monthlyForecast(reordered)
        val noSupportingBranch = V3GameEngine.monthlyForecast(
            reordered.copy(branches = listOf(main))
        )

        assertEquals(
            noSupportingBranch.silverIncome + 5,
            forecast.silverIncome
        )
    }

    @Test
    fun finalDecisionIsRecordedInEndingChronicle() {
        val base = V3Content.newGame("山中堡寨", "西北边堡", "明哲保身", "流寇逼近")
            .copy(
                clanRank = 5,
                year = 1644,
                month = 5,
                routeScores = V3Content.initialRouteScores + (V3Route.Hermit to 90),
                seenChapterMilestones = listOf("rooting_covenant", "expansion_alliance", "county_rivalry", "route_council")
            )
        val event = requireNotNull(V3EventEngine.generateEvent(base))
        assertEquals("甲申前夜", event.title)
        val resolved = V3EventEngine.choose(base.copy(activeEvent = event), event.choices.first())
        val ending = V3GameEngine.finalizeEnding(resolved)
        assertTrue(resolved.completedStoryFlags.any { it.startsWith("final_decision_") })
        assertTrue(ending.body.contains("宗族最终选择"))
    }

    @Test
    fun monthlyCardsRespectBudgetAndChoiceEffects() {
        val base = V3Content.newGame("寒门佃户", "江南水乡", "耕读传家", "官府催税")
        val state = V3CardEngine.refreshMonth(base)
        assertEquals(3, state.cardBudget)
        assertTrue(state.activeCards.isNotEmpty())
        val card = state.activeCards.first()
        val choice = card.choices.first()
        val result = V3CardEngine.choose(state, card.id, choice.id)
        assertNotNull(result)
        assertEquals(1, result!!.state.playedCardsThisMonth)
        assertTrue(result.state.activeCards.size < state.activeCards.size)
    }

    @Test
    fun diceResolutionIsDeterministicAndClearsPendingRoll() {
        val base = V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税")
        val card = V3Content.monthlyCards.first { it.id == "rumor_old_scholar" }
        val state = base.copy(activeCards = listOf(card), cardBudget = 3)
        val pending = requireNotNull(V3CardEngine.choose(state, card.id, "ask"))
        assertNotNull(pending.state.pendingDice)
        val resolved = requireNotNull(V3CardEngine.resolveDice(pending.state))
        assertEquals(null, resolved.state.pendingDice)
        assertEquals(1, resolved.state.playedCardsThisMonth)
        val second = requireNotNull(V3CardEngine.choose(state, card.id, "ask"))
        assertEquals(pending.dice, second.dice)
    }

    @Test
    fun crisisCascadeMovesFromGrainShortageToUnrestAndMutiny() {
        val base = V3Content.newGame("寒门佃户", "中原灾地", "明哲保身", "饥荒将至").copy(
            grain = -20,
            refugees = 20,
            unrestLevel = 40,
            garrisonMorale = 50,
            militia = 20,
            army = V3ArmyRoster(militia = 20)
        )
        val next = V3CardEngine.applyCrisisCascade(base)
        assertEquals("mutiny", next.currentCrisisStage)
        assertTrue(next.refugees > base.refugees)
        assertTrue(next.garrisonMorale < base.garrisonMorale)
        assertTrue(next.militia < base.militia)
    }

    @Test
    fun hexArmsFollowCounterTriangle() {
        assertTrue(V3HexArms.Spear.counters(V3HexArms.Cavalry))
        assertTrue(V3HexArms.Cavalry.counters(V3HexArms.Archer))
        assertTrue(V3HexArms.Archer.counters(V3HexArms.Spear))
        assertTrue(!V3HexArms.Spear.counters(V3HexArms.Archer))
        assertEquals(6, V3HexBattleState.initial().tiles.size)
    }

    @Test
    fun normalizedSaveRestoresTraitsAndKeepsOnlyOneEncounter() {
        val base = V3Content.newGame("边地军户", "西北边堡", "聚族自保", "流寇逼近")
            .copy(
                clanRank = 3,
                militia = 90,
                army = V3ArmyRoster(militia = 90),
                originTraits = emptyList(),
                currentCrisisStage = "stale_stage"
            )
        val battle = requireNotNull(V3GameEngine.startBattle(base).battleState)
        val conflicted = base.copy(
            battleState = battle,
            hexBattleState = V3HexBattleState.initial(),
            conquestState = com.arktools.daming.v3.data.V3ConquestState(
                regionId = "neighbor_county",
                enemyPower = 60,
                rewardSilver = 10,
                rewardGrain = 10,
                rewardInfluence = 2,
                targetName = "临水县",
                scale = "地域征伐"
            )
        )

        val normalized = V3GameEngine.normalizeState(conflicted)

        assertTrue(normalized.originTraits.any { it.startsWith("边堡军籍") })
        assertEquals(null, normalized.currentCrisisStage)
        assertNotNull(normalized.hexBattleState)
        assertEquals(null, normalized.battleState)
        assertEquals(null, normalized.conquestState)
    }

    @Test
    fun encounterEntriesAreMutuallyExclusive() {
        val base = V3Content.newGame("边地军户", "西北边堡", "聚族自保", "流寇逼近")
            .copy(
                clanRank = 3,
                militia = 90,
                army = V3ArmyRoster(militia = 90)
            )
        val battling = V3GameEngine.startBattle(base)
        assertNotNull(battling.battleState)

        val rejectedConquest = V3GameEngine.startConquest(battling, "neighbor_county")
        assertEquals(null, rejectedConquest.conquestState)
        assertNotNull(rejectedConquest.battleState)
        assertTrue(rejectedConquest.pendingReports.single().contains("不能同时"))
    }

    @Test
    fun cardSelectionUsesLayerQuotasAndAnnualCalendar() {
        val crisisState = V3Content.newGame("寒门佃户", "中原灾地", "明哲保身", "饥荒将至")
            .copy(
                grain = -20,
                refugees = 20,
                unrestLevel = 70,
                garrisonMorale = 30,
                clanRank = 4
            )
        val crisisCards = V3Content.additionalMonthlyCards.filter { it.crisisLevel > 0 }
        val selectedCrisis = V3CardEngine.selectPriorityCards(crisisState, crisisCards, 5)
        assertEquals(1, selectedCrisis.count { it.pool == com.arktools.daming.v3.data.V3CardPool.Crisis })

        val springCard = V3Content.additionalMonthlyCards.first { it.id == "annual_01" }
        val wrongMonth = V3CardEngine.refreshMonth(crisisState.copy(
            grain = 100,
            refugees = 0,
            unrestLevel = 0,
            garrisonMorale = 70,
            month = 3
        ), listOf(springCard))
        assertTrue(wrongMonth.activeCards.none { it.id == springCard.id })

        val spring = V3CardEngine.refreshMonth(crisisState.copy(
            grain = 100,
            refugees = 0,
            unrestLevel = 0,
            garrisonMorale = 70,
            month = 2
        ), listOf(springCard))
        assertTrue(spring.activeCards.any { it.id == springCard.id })

        val resolvedCrisis = V3CardEngine.refreshMonth(crisisState.copy(
            grain = 100,
            refugees = 0,
            unrestLevel = 0,
            garrisonMorale = 70,
            currentCrisisStage = "mutiny"
        ), crisisCards)
        assertTrue(resolvedCrisis.activeCards.none { it.pool == com.arktools.daming.v3.data.V3CardPool.Crisis })
        assertEquals(null, resolvedCrisis.currentCrisisStage)
    }

    @Test
    fun hostileRelationEventsTakePriorityOverAlliedInvitations() {
        val base = V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税")
            .copy(
                year = 1610,
                month = 2,
                relations = V3Content.newGame("没落士族", "江南水乡", "耕读传家", "官府催税")
                    .relations.copy(yamen = 80, garrison = -90)
            )

        val event = requireNotNull(V3EventEngine.generateEvent(base))
        assertEquals("军镇敌对来书", event.title)
    }

    @Test
    fun extendedStateRoundTripsThroughJson() {
        val state = V3Content.newGame("海商遗族", "闽粤海路", "开海远行", "商路断绝")
            .copy(inventory = listOf("western_clock"), biography = listOf("初见海潮"), plaques = listOf("义门"))
        val encoded = Json.encodeToString(state)
        val decoded = Json.decodeFromString<V3GameState>(encoded)
        assertEquals(state.patriarch, decoded.patriarch)
        assertEquals(state.inventory, decoded.inventory)
        assertEquals(state.biography, decoded.biography)
        assertEquals(state.plaques, decoded.plaques)
    }

    private fun assertStateInvariants(state: V3GameState) {
        assertTrue(state.month in 1..12)
        assertTrue(state.clanRank in 1..5)
        assertEquals(state.army.total(), state.militia)
        assertTrue(state.militia in 0..999)
        assertTrue(state.cohesion in 0..100)
        assertTrue(state.influence in 0..100)
        assertEquals(state.people.size, state.people.map { it.id }.distinct().size)
        assertTrue(state.people.all { it.age >= 0 && it.ageMonths >= it.age * 12 })
        assertTrue(state.people.all { person ->
            person.spouseId == null || state.people.any { spouse -> spouse.id == person.spouseId && spouse.spouseId == person.id }
        })
        assertTrue(state.people.all { it.pregnancyDueMonth == null || it.gender == V3Gender.Female })
        assertTrue(state.equipment.all { it.durability in 0..it.maxDurability })
        assertTrue(state.sites.all { it.control in 0..100 && it.risk in 0..100 })
    }
}
