package com.example.thetower.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ItemDefinition(
    val id: String,
    val name: String,
    val type: String, // "WEAPON", "SHIELD", "POTION"
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val healAmount: Int = 0,
    val buyPrice: Int = 0,
    val sellPrice: Int = 0,
    val dropWeight: Int = 0
)
