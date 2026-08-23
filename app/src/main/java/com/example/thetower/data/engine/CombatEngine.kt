package com.example.thetower.data.engine

import com.example.thetower.data.config.GameConfig
import com.example.thetower.data.model.GameState
import com.example.thetower.data.model.MonsterData
import com.example.thetower.data.model.PlayerData
import kotlin.math.max
import kotlin.random.Random

object CombatEngine {

    fun getPlayerAttackBonus(state: GameState): Int {
        val equippedId = state.inventory.equippedWeaponId ?: return 0
        val itemInstance = state.inventory.items.firstOrNull { it.id == equippedId } ?: return 0
        val itemDef = GameConfig.ITEMS[itemInstance.itemDefId] ?: return 0
        return itemDef.attackBonus
    }

    fun getPlayerDefenseBonus(state: GameState): Int {
        val equippedId = state.inventory.equippedShieldId ?: return 0
        val itemInstance = state.inventory.items.firstOrNull { it.id == equippedId } ?: return 0
        val itemDef = GameConfig.ITEMS[itemInstance.itemDefId] ?: return 0
        return itemDef.defenseBonus
    }

    fun getPlayerTotalAttack(state: GameState): Int {
        return state.player.baseAttack + getPlayerAttackBonus(state)
    }

    fun getPlayerTotalDefense(state: GameState): Int {
        return state.player.baseDefense + getPlayerDefenseBonus(state)
    }

    // Damage per completed quest: baseDamage + player.attackBonus
    fun getQuestCompleteDamage(state: GameState): Int {
        val baseDamage = 5 + state.player.level * 2
        return baseDamage + getPlayerAttackBonus(state)
    }

    // Failure damage per quest: max(3, 15 - player.totalDefense)
    fun getQuestFailureDamage(state: GameState): Int {
        val totalDefense = getPlayerTotalDefense(state)
        return max(3, 15 - totalDefense)
    }

    // Temptation damage: 5 + floor * 2
    fun getTemptationDamage(floor: Int): Int {
        return 5 + floor * 2
    }

    // Dehydration damage: 10 + floor
    fun getDehydrationDamage(floor: Int): Int {
        return 10 + floor
    }

    // XP gained: 15 * monster.level
    fun getXpReward(monster: MonsterData): Int {
        return 15 * monster.level
    }

    // Gold gained: 10 * monster.level + random(0..5)
    fun getGoldReward(monster: MonsterData): Int {
        return 10 * monster.level + Random.nextInt(0, 6)
    }

    // Weighted item drop calculation
    fun calculateLootDrop(monster: MonsterData): String? {
        val race = GameConfig.RACES.firstOrNull { it.name == monster.raceName } ?: return null
        val dropTable = race.dropTable
        if (dropTable.isEmpty()) return null

        val dropChance = if (monster.isBoss) 1.0 else 0.35
        if (Random.nextDouble() > dropChance) return null

        val totalWeight = dropTable.sumOf { it.dropWeight }
        if (totalWeight <= 0) return null

        var roll = Random.nextInt(1, totalWeight + 1)
        for (drop in dropTable) {
            roll -= drop.dropWeight
            if (roll <= 0) {
                return drop.itemDefId
            }
        }
        return null
    }
}
