package com.example.thetower.data.engine

import com.example.thetower.data.config.GameConfig
import com.example.thetower.data.model.MonsterData
import com.example.thetower.data.model.RaceDefinition
import com.example.thetower.data.model.SinModifier
import kotlin.random.Random

object MonsterGenerator {

    fun generateMonster(floor: Int, isBoss: Boolean = false): MonsterData {
        // Filter races suitable for this floor
        val possibleRaces = GameConfig.RACES.filter { it.minFloor <= floor && (it.maxFloor == null || it.maxFloor >= floor) }
        val race = if (possibleRaces.isNotEmpty()) {
            possibleRaces.random()
        } else {
            GameConfig.RACES.random()
        }

        // Select a random sin modifier
        val sin: SinModifier? = if (race.possibleSins.isNotEmpty()) {
            race.possibleSins.random()
        } else {
            null
        }

        val level = floor

        // Base HP scaling: baseMaxHp + (floor - 1) * 15 + randomVariance(±5)
        val variance = Random.nextInt(-5, 6) // -5 to 5 inclusive
        var rawMaxHp = race.baseHp + (floor - 1) * 15 + variance
        if (rawMaxHp < 10) rawMaxHp = 10

        // Base ATK/DEF scaling: scales with floor
        var rawAttack = race.baseAttack + (floor - 1) * 2
        var rawDefense = race.baseDefense + (floor - 1) * 1

        // Adjust stats for Sin Modifier
        if (sin != null) {
            rawMaxHp += sin.hpDelta
            rawAttack += sin.attackDelta
            rawDefense += sin.defenseDelta
        }

        // If boss, multiply stats
        if (isBoss) {
            rawMaxHp = (rawMaxHp * 2.5).toInt()
            rawAttack = (rawAttack * 1.5).toInt()
            rawDefense = (rawDefense * 1.5).toInt()
        }

        // Clamp values to prevent negative stats
        if (rawMaxHp < 10) rawMaxHp = 10
        if (rawAttack < 1) rawAttack = 1
        if (rawDefense < 0) rawDefense = 0

        val monsterName = if (sin != null) "${sin.name} ${race.name}" else race.name
        val descriptionSuffix = if (sin != null && sin.descriptionSuffix.isNotEmpty()) " ${sin.descriptionSuffix}" else ""
        val fullDescription = "${race.description}$descriptionSuffix"

        return MonsterData(
            name = monsterName,
            raceName = race.name,
            raceDescription = fullDescription,
            sinModifier = sin?.name,
            level = level,
            currentHp = rawMaxHp,
            maxHp = rawMaxHp,
            attack = rawAttack,
            defense = rawDefense,
            dropTableId = race.name,
            avatarEmoji = race.avatarEmoji,
            isBoss = isBoss
        )
    }
}
