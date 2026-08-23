package com.example.thetower.data.engine

import com.example.thetower.data.model.GameState
import com.example.thetower.data.model.PlayerData
import com.example.thetower.data.model.QuestDefinition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class SlotTransitionEngineTest {

    @Test
    fun testCombatEngineCalculations() {
        val player = PlayerData(
            level = 1,
            baseAttack = 10,
            baseDefense = 5,
            currentHp = 50,
            maxHp = 50
        )
        val state = GameState(player = player)

        // Quest completion damage formula: (5 + level * 2) + weaponAttackBonus
        val questDmg = CombatEngine.getQuestCompleteDamage(state)
        assertEquals(7, questDmg) // base = 5 + 1*2 = 7, weapon bonus = 0

        // Quest failure damage formula: max(3, 15 - player.totalDefense)
        val failureDmg = CombatEngine.getQuestFailureDamage(state)
        assertEquals(10, failureDmg) // 15 - 5 = 10

        // Temptation damage: 5 + floor * 2
        val tempDmgFloor1 = CombatEngine.getTemptationDamage(1)
        val tempDmgFloor5 = CombatEngine.getTemptationDamage(5)
        assertEquals(7, tempDmgFloor1)
        assertEquals(15, tempDmgFloor5)

        // Dehydration damage: 10 + floor
        val dehydrateDmg = CombatEngine.getDehydrationDamage(2)
        assertEquals(12, dehydrateDmg)
    }

    @Test
    fun testSlotTransitionQuestFailure() {
        // Create a game state starting at Sunday, August 23rd, 2026, MORNING slot
        val quest = QuestDefinition(
            id = "quest_1",
            title = "Drink water in morning",
            slot = "MORNING",
            activeDays = listOf(1, 2, 3, 4, 5, 6, 7) // All days active
        )
        val player = PlayerData(
            currentHp = 50,
            maxHp = 50,
            baseDefense = 5 // failure damage: 15 - 5 = 10
        )
        val state = GameState(
            player = player,
            lastProcessedSlot = "MORNING",
            lastProcessedDate = "2026-08-23", // Sunday
            questDefinitions = listOf(quest),
            dailyQuestStates = emptyMap() // quest_1 not completed
        )

        // Current time is now 13:00 (active slot is NOON of August 23rd)
        val evaluationTime = LocalDateTime.of(2026, 8, 23, 13, 0)
        
        val updatedState = SlotTransitionEngine.evaluateTransitions(state, evaluationTime)

        // Since we transitioned from MORNING to NOON, we left the MORNING slot.
        // The morning quest was not completed, so the player should have taken 10 damage.
        assertEquals(40, updatedState.player.currentHp)
        assertEquals("NOON", updatedState.lastProcessedSlot)
        assertEquals("2026-08-23", updatedState.lastProcessedDate)
        
        // Check that a battle log entry was written
        assertTrue(updatedState.battleLog.any { it.contains("Missed Morning quest", ignoreCase = true) })
    }

    @Test
    fun testDayRolloverDehydrationAndQuestReset() {
        val state = GameState(
            player = PlayerData(currentHp = 50, maxHp = 50, baseDefense = 5),
            lastProcessedSlot = "NIGHT",
            lastProcessedDate = "2026-08-23", // Sunday
            hydrationTarget = 3.0,
            hydrationProgress = 1.0, // Failed hydration target
            dailyQuestStates = mapOf("quest_1" to true)
        )

        // Evaluate transitions into Monday, August 24th, 09:30 (active slot is MORNING)
        val evaluationTime = LocalDateTime.of(2026, 8, 24, 9, 30)
        
        val updatedState = SlotTransitionEngine.evaluateTransitions(state, evaluationTime)

        // Day rollover occurred (NIGHT -> MORNING).
        // 1. Dehydration damage applied: 10 + floor = 10 + 1 = 11 damage
        assertEquals(39, updatedState.player.currentHp)

        // 2. Hydration progress reset to 0.0
        assertEquals(0.0, updatedState.hydrationProgress, 0.001)

        // 3. Quest states cleared
        assertTrue(updatedState.dailyQuestStates.isEmpty())

        // 4. Slots updated
        assertEquals("MORNING", updatedState.lastProcessedSlot)
        assertEquals("2026-08-24", updatedState.lastProcessedDate)
    }

    @Test
    fun testQuestInactiveDaySkipsDamage() {
        val quest = QuestDefinition(
            id = "weekend_habit",
            title = "Weekend Workout",
            slot = "MORNING",
            activeDays = listOf(6, 7) // Saturday (6) and Sunday (7) only
        )
        val state = GameState(
            player = PlayerData(currentHp = 50, maxHp = 50, baseDefense = 5),
            lastProcessedSlot = "MORNING",
            lastProcessedDate = "2026-08-24", // August 24th is a Monday (1)
            questDefinitions = listOf(quest),
            dailyQuestStates = emptyMap()
        )

        // Transition from MORNING (Monday) to NOON (Monday)
        val evaluationTime = LocalDateTime.of(2026, 8, 24, 13, 0)
        val updatedState = SlotTransitionEngine.evaluateTransitions(state, evaluationTime)

        // Because the quest is inactive on Monday, it should NOT trigger failure damage even though it was incomplete.
        assertEquals(50, updatedState.player.currentHp)
        assertEquals("NOON", updatedState.lastProcessedSlot)
    }
}
