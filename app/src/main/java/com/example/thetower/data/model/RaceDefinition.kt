package com.example.thetower.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LootDrop(
    val itemDefId: String,
    val dropWeight: Int
)

@Serializable
data class SinModifier(
    val name: String,
    val hpDelta: Int = 0,
    val attackDelta: Int = 0,
    val defenseDelta: Int = 0,
    val descriptionSuffix: String = ""
)

@Serializable
data class RaceDefinition(
    val name: String,
    val description: String,
    val baseHp: Int,
    val baseAttack: Int,
    val baseDefense: Int,
    val avatarEmoji: String,
    val minFloor: Int,
    val maxFloor: Int? = null,
    val dropTable: List<LootDrop>,
    val possibleSins: List<SinModifier>
)
