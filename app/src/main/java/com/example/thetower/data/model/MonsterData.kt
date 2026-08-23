package com.example.thetower.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MonsterData(
    val name: String,
    val raceName: String,
    val raceDescription: String,
    val sinModifier: String? = null,
    val level: Int,
    val currentHp: Int,
    val maxHp: Int,
    val attack: Int,
    val defense: Int,
    val dropTableId: String,
    val avatarEmoji: String,
    val isBoss: Boolean = false
)
