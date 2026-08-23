package com.example.thetower.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val version: Int = 1,
    val lastSavedAt: Long = System.currentTimeMillis(),
    val player: PlayerData = PlayerData(),
    val currentFloor: Int = 1,
    val activeMonster: MonsterData? = null,
    val floorBossAvailable: Boolean = false,
    val lastProcessedSlot: String? = null, // "MORNING", "NOON", "EVENING", "NIGHT"
    val lastProcessedDate: String? = null, // "YYYY-MM-DD"
    val questDefinitions: List<QuestDefinition> = emptyList(),
    val dailyQuestStates: Map<String, Boolean> = emptyMap(), // questId -> completed
    val hydrationTarget: Double = 2.5, // liters
    val hydrationProgress: Double = 0.0, // liters consumed today
    val inventory: InventoryData = InventoryData(),
    val bestiary: Map<String, RaceEncounter> = emptyMap(), // raceName -> RaceEncounter
    val battleLog: List<String> = emptyList(),
    val appLanguage: String = "en", // "en" or "he"
    val alarmModeActive: Boolean = false,
    val alarmTimes: Map<String, String> = mapOf(
        "MORNING" to "09:00",
        "NOON" to "12:00",
        "EVENING" to "18:00",
        "NIGHT" to "21:00"
    )
)
