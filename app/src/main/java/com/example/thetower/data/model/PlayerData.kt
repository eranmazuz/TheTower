package com.example.thetower.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerData(
    val name: String = "Hero",
    val job: String = "", // "Warrior", "Mage", or "" (unselected)
    val level: Int = 1,
    val xp: Int = 0,
    val gold: Int = 100,
    val currentHp: Int = 10,
    val maxHp: Int = 10,
    val baseAttack: Int = 5,
    val baseDefense: Int = 2
) {
    fun xpToNextLevel(): Int = 100 * level
}
