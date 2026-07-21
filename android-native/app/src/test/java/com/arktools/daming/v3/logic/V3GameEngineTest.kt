package com.arktools.daming.v3.logic

import com.arktools.daming.v3.data.V3ArmyRoster
import com.arktools.daming.v3.data.V3Content
import com.arktools.daming.v3.data.V3EquipmentQuality
import com.arktools.daming.v3.data.V3EquipmentSlot
import com.arktools.daming.v3.data.V3GameState
import com.arktools.daming.v3.data.V3Gender
import com.arktools.daming.v3.data.V3Person
import com.arktools.daming.v3.data.V3Trait
import com.arktools.daming.v3.data.V3TroopType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class V3GameEngineTest {
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
