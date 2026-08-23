package com.example.thetower.data.engine

import com.example.thetower.data.model.GameState
import com.example.thetower.data.model.MonsterData
import com.example.thetower.data.model.PlayerData
import com.example.thetower.data.model.RaceEncounter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

object SlotTransitionEngine {

    fun getActiveSlotAndDate(dateTime: LocalDateTime, alarmTimes: Map<String, String>): Pair<String, String> {
        val morningTime = parseTime(alarmTimes["MORNING"] ?: "09:00")
        val noonTime = parseTime(alarmTimes["NOON"] ?: "12:00")
        val eveningTime = parseTime(alarmTimes["EVENING"] ?: "18:00")
        val nightTime = parseTime(alarmTimes["NIGHT"] ?: "21:00")

        val time = dateTime.toLocalTime()
        val date = dateTime.toLocalDate()

        return when {
            time.isBefore(morningTime) -> {
                // Hours before first morning alarm belong to previous day's NIGHT
                Pair("NIGHT", date.minusDays(1).toString())
            }
            time.isBefore(noonTime) -> {
                Pair("MORNING", date.toString())
            }
            time.isBefore(eveningTime) -> {
                Pair("NOON", date.toString())
            }
            time.isBefore(nightTime) -> {
                Pair("EVENING", date.toString())
            }
            else -> {
                Pair("NIGHT", date.toString())
            }
        }
    }

    private fun parseTime(timeStr: String): LocalTime {
        return try {
            LocalTime.parse(timeStr)
        } catch (e: Exception) {
            when (timeStr) {
                "MORNING" -> LocalTime.of(9, 0)
                "NOON" -> LocalTime.of(12, 0)
                "EVENING" -> LocalTime.of(18, 0)
                "NIGHT" -> LocalTime.of(21, 0)
                else -> LocalTime.of(9, 0)
            }
        }
    }

    fun getNextSlotAndDate(slot: String, dateStr: String): Pair<String, String> {
        val date = LocalDate.parse(dateStr)
        return when (slot) {
            "MORNING" -> Pair("NOON", dateStr)
            "NOON" -> Pair("EVENING", dateStr)
            "EVENING" -> Pair("NIGHT", dateStr)
            "NIGHT" -> Pair("MORNING", date.plusDays(1).toString())
            else -> Pair("MORNING", dateStr)
        }
    }

    fun evaluateTransitions(state: GameState, currentDateTime: LocalDateTime): GameState {
        val (currentActiveSlot, currentActiveDate) = getActiveSlotAndDate(currentDateTime, state.alarmTimes)

        if (state.lastProcessedSlot == null || state.lastProcessedDate == null) {
            // First time running the app or state initialized
            var updatedState = state.copy(
                lastProcessedSlot = currentActiveSlot,
                lastProcessedDate = currentActiveDate
            )
            if (updatedState.activeMonster == null) {
                val monster = MonsterGenerator.generateMonster(updatedState.currentFloor, isBoss = false)
                val bestiary = updatedState.bestiary.toMutableMap()
                val raceEncounter = bestiary[monster.raceName] ?: RaceEncounter(monster.raceName)
                bestiary[monster.raceName] = raceEncounter.copy(encounters = raceEncounter.encounters + 1)
                
                updatedState = updatedState.copy(
                    activeMonster = monster,
                    bestiary = bestiary,
                    battleLog = updatedState.battleLog + "A wild ${monster.name} appeared on Floor ${updatedState.currentFloor}!"
                )
            }
            return updatedState
        }

        var cursorSlot: String = state.lastProcessedSlot ?: return state
        var cursorDate: String = state.lastProcessedDate ?: return state

        if (cursorSlot == currentActiveSlot && cursorDate == currentActiveDate) {
            // No transition occurred
            return state
        }

        var player = state.player
        var floor = state.currentFloor
        var monster = state.activeMonster
        var floorBossAvailable = state.floorBossAvailable
        var hydrationProgress = state.hydrationProgress
        val bestiary = state.bestiary.toMutableMap()
        val logs = state.battleLog.toMutableList()
        val dailyQuestStates = state.dailyQuestStates.toMutableMap()

        // Helper to check if player died and handle reset
        fun checkPlayerDeath() {
            if (player.currentHp <= 0) {
                val goldLost = (player.gold * 0.25).toInt()
                player = player.copy(
                    currentHp = player.maxHp,
                    gold = max(0, player.gold - goldLost)
                )
                floor = 1
                monster = MonsterGenerator.generateMonster(floor = 1, isBoss = false)
                val raceEncounter = bestiary[monster!!.raceName] ?: RaceEncounter(monster!!.raceName)
                bestiary[monster!!.raceName] = raceEncounter.copy(encounters = raceEncounter.encounters + 1)

                floorBossAvailable = false
                logs.add("💀 Hero was defeated! Returned to Floor 1. Lost $goldLost gold.")
            }
        }

        while (cursorSlot != currentActiveSlot || cursorDate != currentActiveDate) {
            // Check if player is already dead, skip further damage if so
            if (player.currentHp <= 0) {
                checkPlayerDeath()
            }

            val localDate = LocalDate.parse(cursorDate)
            val dayOfWeek = localDate.dayOfWeek.value // 1 = Monday, 7 = Sunday

            // 1. Evaluate incomplete quests for the departed slot
            val questsInSlot = state.questDefinitions.filter {
                it.slot == cursorSlot && it.activeDays.contains(dayOfWeek)
            }

            for (quest in questsInSlot) {
                val isCompleted = dailyQuestStates[quest.id] == true
                if (!isCompleted) {
                    val tempState = state.copy(player = player) // temporary evaluation state
                    val dmg = CombatEngine.getQuestFailureDamage(tempState)
                    player = player.copy(currentHp = max(0, player.currentHp - dmg))
                    logs.add("❌ Missed ${cursorSlot.lowercase().capitalize()} quest: '${quest.title}'. Took $dmg damage.")
                }
            }

            // 2. Advance cursor to the next slot
            val next = getNextSlotAndDate(cursorSlot, cursorDate)
            val oldSlot = cursorSlot
            cursorSlot = next.first
            cursorDate = next.second

            // 3. Handle Day Rollover (when entering Morning of the next day)
            if (oldSlot == "NIGHT" && cursorSlot == "MORNING") {
                // A. Check hydration for the old day
                if (hydrationProgress < state.hydrationTarget) {
                    val dmg = CombatEngine.getDehydrationDamage(floor)
                    player = player.copy(currentHp = max(0, player.currentHp - dmg))
                    val progressStr = String.format("%.2f", hydrationProgress)
                    logs.add("💧 Dehydration! Drank ${progressStr}L of ${state.hydrationTarget}L. Took $dmg damage.")
                }
                
                // B. Reset hydration progress
                hydrationProgress = 0.0

                // C. Reset daily quests completion state
                dailyQuestStates.clear()
            }
        }

        // Final check on player death
        checkPlayerDeath()

        // If the active monster was defeated or missing (e.g. on death reset), spawn a new one
        val currentMonster = monster
        if (currentMonster == null || currentMonster.currentHp <= 0) {
            val newMonster = MonsterGenerator.generateMonster(floor, isBoss = false)
            monster = newMonster
            val raceEncounter = bestiary[newMonster.raceName] ?: RaceEncounter(newMonster.raceName)
            bestiary[newMonster.raceName] = raceEncounter.copy(encounters = raceEncounter.encounters + 1)
            logs.add("A wild ${newMonster.name} appeared on Floor $floor!")
            floorBossAvailable = false
        }

        // Cap logs at 100 entries to prevent memory bloat
        val finalLogs = if (logs.size > 100) logs.takeLast(100) else logs

        return state.copy(
            player = player,
            currentFloor = floor,
            activeMonster = monster,
            floorBossAvailable = floorBossAvailable,
            lastProcessedSlot = currentActiveSlot,
            lastProcessedDate = currentActiveDate,
            dailyQuestStates = dailyQuestStates,
            hydrationProgress = hydrationProgress,
            bestiary = bestiary,
            battleLog = finalLogs
        )
    }
}
