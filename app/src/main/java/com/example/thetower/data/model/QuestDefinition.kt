package com.example.thetower.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestDefinition(
    val id: String,
    val title: String,
    val slot: String, // "MORNING", "NOON", "EVENING", "NIGHT"
    val activeDays: List<Int> // 1 = Monday, 2 = Tuesday, ..., 7 = Sunday
)
