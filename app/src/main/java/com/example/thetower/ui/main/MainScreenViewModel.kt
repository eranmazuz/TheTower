package com.example.thetower.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thetower.data.DataRepository
import com.example.thetower.data.config.GameConfig
import com.example.thetower.data.engine.CombatEngine
import com.example.thetower.data.engine.MonsterGenerator
import com.example.thetower.data.engine.SlotTransitionEngine
import com.example.thetower.data.model.GameState
import com.example.thetower.data.model.ItemInstance
import com.example.thetower.data.model.QuestDefinition
import com.example.thetower.data.model.RaceEncounter
import com.example.thetower.receiver.SlotAlarmReceiver
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class MainScreenViewModel(private val repository: DataRepository) : ViewModel() {

    val gameState: StateFlow<GameState> = repository.gameState

    fun resumeAndEvaluate(context: Context) {
        val updatedState = repository.updateState { currentState ->
            SlotTransitionEngine.evaluateTransitions(currentState, LocalDateTime.now())
        }
        SlotAlarmReceiver.scheduleNextAlarm(context, updatedState)
    }

    fun chooseJob(name: String, job: String) {
        repository.updateState { state ->
            val (baseHp, baseAtk, baseDef) = when (job) {
                "Warrior" -> Triple(60, 12, 6)
                "Mage" -> Triple(40, 18, 3)
                else -> Triple(50, 10, 5)
            }
            state.copy(
                player = state.player.copy(
                    name = name.ifBlank { "Hero" },
                    job = job,
                    currentHp = baseHp,
                    maxHp = baseHp,
                    baseAttack = baseAtk,
                    baseDefense = baseDef
                ),
                battleLog = listOf("Welcome, $name the $job! Your journey in The Tower begins.") + state.battleLog.filterNot { it.contains("Welcome") }
            )
        }
    }

    fun completeQuest(questId: String) {
        repository.updateState { state ->
            val quest = state.questDefinitions.firstOrNull { it.id == questId } ?: return@updateState state
            val completed = state.dailyQuestStates[questId] == true
            if (completed) return@updateState state // Already completed

            val updatedDailyStates = state.dailyQuestStates.toMutableMap()
            updatedDailyStates[questId] = true

            var player = state.player
            var monster = state.activeMonster
            var floor = state.currentFloor
            var floorBossAvailable = state.floorBossAvailable
            val bestiary = state.bestiary.toMutableMap()
            val logs = state.battleLog.toMutableList()
            val inventoryItems = state.inventory.items.toMutableList()

            // 1. Calculate Damage to Monster
            if (monster != null) {
                val dmg = CombatEngine.getQuestCompleteDamage(state)
                val newHp = max(0, monster.currentHp - dmg)
                monster = monster.copy(currentHp = newHp)
                logs.add("⚔️ Completed quest: '${quest.title}'! Dealt $dmg damage to ${monster.name}.")

                // 2. Check if Monster Defeated
                if (monster.currentHp <= 0) {
                    val xpGained = CombatEngine.getXpReward(monster)
                    val goldGained = CombatEngine.getGoldReward(monster)
                    val droppedItemDefId = CombatEngine.calculateLootDrop(monster)

                    logs.add("🏆 Defeated ${monster.name}! Gained $xpGained XP and $goldGained gold.")
                    
                    // Award Loot
                    player = player.copy(
                        xp = player.xp + xpGained,
                        gold = player.gold + goldGained
                    )

                    // Increment defeats in bestiary
                    val encounter = bestiary[monster.raceName] ?: RaceEncounter(monster.raceName)
                    bestiary[monster.raceName] = encounter.copy(defeats = encounter.defeats + 1)

                    // Handle Item Drop
                    if (droppedItemDefId != null) {
                        val itemDef = GameConfig.ITEMS[droppedItemDefId]
                        if (itemDef != null) {
                            val instance = ItemInstance(
                                id = UUID.randomUUID().toString(),
                                itemDefId = droppedItemDefId,
                                isEquipped = false
                            )
                            inventoryItems.add(instance)
                            logs.add("🎁 Found loot: ${itemDef.name}!")
                        }
                    }

                    // Handle Level Up
                    while (player.xp >= player.xpToNextLevel()) {
                        val oldXp = player.xp
                        val targetXp = player.xpToNextLevel()
                        val newLevel = player.level + 1
                        
                        // Scale player stats on level up
                        val extraHp = 10
                        val extraAtk = 2
                        val extraDef = 1

                        player = player.copy(
                            level = newLevel,
                            xp = oldXp - targetXp,
                            maxHp = player.maxHp + extraHp,
                            currentHp = player.maxHp + extraHp,
                            baseAttack = player.baseAttack + extraAtk,
                            baseDefense = player.baseDefense + extraDef
                        )
                        logs.add("✨ LEVEL UP! Reached Level $newLevel! Max HP +$extraHp, ATK +$extraAtk, DEF +$extraDef.")
                    }

                    // Check if Floor Boss is available
                    val now = LocalDateTime.now()
                    val (_, currentDateStr) = SlotTransitionEngine.getActiveSlotAndDate(now, state.alarmTimes)
                    val localDate = LocalDate.parse(currentDateStr)
                    val dayOfWeek = localDate.dayOfWeek.value

                    val activeQuestsToday = state.questDefinitions.filter { it.activeDays.contains(dayOfWeek) }
                    // Update daily state map locally to calculate boss availability
                    val allDone = activeQuestsToday.isNotEmpty() && activeQuestsToday.all {
                        it.id == questId || updatedDailyStates[it.id] == true
                    }

                    if (monster.isBoss) {
                        // Boss defeated: Advance floor
                        floor += 1
                        monster = MonsterGenerator.generateMonster(floor, isBoss = false)
                        val newEncounter = bestiary[monster.raceName] ?: RaceEncounter(monster.raceName)
                        bestiary[monster.raceName] = newEncounter.copy(encounters = newEncounter.encounters + 1)
                        logs.add("🏢 Advanced to Floor $floor! A wild ${monster.name} appeared.")
                        floorBossAvailable = false
                    } else if (allDone) {
                        floorBossAvailable = true
                        monster = null // Clear standard monster, boss is ready
                        logs.add("👹 All daily quests completed! The Floor $floor Boss is available to fight in the Tower tab.")
                    } else {
                        // Spawn normal monster
                        monster = MonsterGenerator.generateMonster(floor, isBoss = false)
                        val newEncounter = bestiary[monster.raceName] ?: RaceEncounter(monster.raceName)
                        bestiary[monster.raceName] = newEncounter.copy(encounters = newEncounter.encounters + 1)
                        logs.add("A wild ${monster.name} appeared on Floor $floor.")
                        floorBossAvailable = false
                    }
                }
            }

            state.copy(
                dailyQuestStates = updatedDailyStates,
                player = player,
                activeMonster = monster,
                currentFloor = floor,
                floorBossAvailable = floorBossAvailable,
                bestiary = bestiary,
                battleLog = if (logs.size > 100) logs.takeLast(100) else logs,
                inventory = state.inventory.copy(items = inventoryItems)
            )
        }
    }

    fun logTemptation() {
        repository.updateState { state ->
            var player = state.player
            val dmg = CombatEngine.getTemptationDamage(state.currentFloor)
            player = player.copy(currentHp = max(0, player.currentHp - dmg))

            val logs = state.battleLog.toMutableList()
            logs.add("⚠️ Logged a temptation! Monster counter-attacked and dealt $dmg damage.")

            var floor = state.currentFloor
            var monster = state.activeMonster
            var floorBossAvailable = state.floorBossAvailable
            val bestiary = state.bestiary.toMutableMap()

            // Handle player death
            if (player.currentHp <= 0) {
                val goldLost = (player.gold * 0.25).toInt()
                player = player.copy(
                    currentHp = player.maxHp,
                    gold = max(0, player.gold - goldLost)
                )
                floor = 1
                monster = MonsterGenerator.generateMonster(floor = 1, isBoss = false)
                val raceEncounter = bestiary[monster.raceName] ?: RaceEncounter(monster.raceName)
                bestiary[monster.raceName] = raceEncounter.copy(encounters = raceEncounter.encounters + 1)
                floorBossAvailable = false
                logs.add("💀 Hero was defeated! Floor reset to 1. Lost $goldLost gold.")
            }

            state.copy(
                player = player,
                currentFloor = floor,
                activeMonster = monster,
                floorBossAvailable = floorBossAvailable,
                bestiary = bestiary,
                battleLog = if (logs.size > 100) logs.takeLast(100) else logs
            )
        }
    }

    fun addWater(amount: Double) {
        repository.updateState { state ->
            val currentAmount = state.hydrationProgress
            val newAmount = min(10.0, currentAmount + amount) // Cap at 10L daily maximum safety
            val logs = state.battleLog.toMutableList()
            val progressStr = String.format("%.2f", amount)
            logs.add("💧 Consumed +${progressStr}L of water.")
            state.copy(
                hydrationProgress = newAmount,
                battleLog = if (logs.size > 100) logs.takeLast(100) else logs
            )
        }
    }

    fun useHealthPotion() {
        repository.updateState { state ->
            val items = state.inventory.items.toMutableList()
            val potionIndex = items.indexOfFirst {
                GameConfig.ITEMS[it.itemDefId]?.type == "POTION"
            }
            if (potionIndex == -1) return@updateState state // No potions

            val potionInstance = items[potionIndex]
            val potionDef = GameConfig.ITEMS[potionInstance.itemDefId] ?: return@updateState state

            items.removeAt(potionIndex)
            val healAmount = potionDef.healAmount
            val newHp = min(state.player.maxHp, state.player.currentHp + healAmount)

            val logs = state.battleLog.toMutableList()
            logs.add("❤️ Consumed Health Potion. Restored $healAmount HP.")

            state.copy(
                player = state.player.copy(currentHp = newHp),
                inventory = state.inventory.copy(items = items),
                battleLog = if (logs.size > 100) logs.takeLast(100) else logs
            )
        }
    }

    fun escapeTown() {
        repository.updateState { state ->
            val now = LocalDateTime.now()
            val (currentSlot, currentDateStr) = SlotTransitionEngine.getActiveSlotAndDate(now, state.alarmTimes)
            val localDate = LocalDate.parse(currentDateStr)
            val dayOfWeek = localDate.dayOfWeek.value

            val activeQuestsInSlot = state.questDefinitions.filter {
                it.slot == currentSlot && it.activeDays.contains(dayOfWeek)
            }

            val incompleteQuests = activeQuestsInSlot.filter { state.dailyQuestStates[it.id] != true }
            var player = state.player
            val logs = state.battleLog.toMutableList()

            for (quest in incompleteQuests) {
                val dmg = CombatEngine.getQuestFailureDamage(state)
                player = player.copy(currentHp = max(0, player.currentHp - dmg))
                logs.add("❌ Escaped Tower: Took $dmg damage for incomplete quest '${quest.title}'.")
            }

            var floor = state.currentFloor
            var monster = state.activeMonster
            var floorBossAvailable = state.floorBossAvailable
            val bestiary = state.bestiary.toMutableMap()

            // Handle player death
            if (player.currentHp <= 0) {
                val goldLost = (player.gold * 0.25).toInt()
                player = player.copy(
                    currentHp = player.maxHp,
                    gold = max(0, player.gold - goldLost)
                )
                floor = 1
                monster = MonsterGenerator.generateMonster(floor = 1, isBoss = false)
                val raceEncounter = bestiary[monster.raceName] ?: RaceEncounter(monster.raceName)
                bestiary[monster.raceName] = raceEncounter.copy(encounters = raceEncounter.encounters + 1)
                floorBossAvailable = false
                logs.add("💀 Hero was defeated! Floor reset to 1. Lost $goldLost gold.")
            }

            state.copy(
                player = player,
                currentFloor = floor,
                activeMonster = monster,
                floorBossAvailable = floorBossAvailable,
                bestiary = bestiary,
                battleLog = if (logs.size > 100) logs.takeLast(100) else logs
            )
        }
    }

    fun fightFloorBoss() {
        repository.updateState { state ->
            if (!state.floorBossAvailable) return@updateState state

            val boss = MonsterGenerator.generateMonster(state.currentFloor, isBoss = true)
            val bestiary = state.bestiary.toMutableMap()
            val encounter = bestiary[boss.raceName] ?: RaceEncounter(boss.raceName)
            bestiary[boss.raceName] = encounter.copy(encounters = encounter.encounters + 1)

            val logs = state.battleLog.toMutableList()
            logs.add("😈 Challenged Floor ${state.currentFloor} Boss: ${boss.name}!")

            state.copy(
                activeMonster = boss,
                floorBossAvailable = false,
                bestiary = bestiary,
                battleLog = if (logs.size > 100) logs.takeLast(100) else logs
            )
        }
    }

    fun buyItem(itemDefId: String) {
        repository.updateState { state ->
            val itemDef = GameConfig.ITEMS[itemDefId] ?: return@updateState state
            if (state.player.gold < itemDef.buyPrice) return@updateState state // Not enough gold

            val newInstance = ItemInstance(
                id = UUID.randomUUID().toString(),
                itemDefId = itemDefId,
                isEquipped = false
            )

            val items = state.inventory.items + newInstance
            val logs = state.battleLog.toMutableList()
            logs.add("💰 Bought ${itemDef.name} for ${itemDef.buyPrice} gold.")

            state.copy(
                player = state.player.copy(gold = state.player.gold - itemDef.buyPrice),
                inventory = state.inventory.copy(items = items),
                battleLog = if (logs.size > 100) logs.takeLast(100) else logs
            )
        }
    }

    fun sellItem(instanceId: String) {
        repository.updateState { state ->
            val items = state.inventory.items.toMutableList()
            val index = items.indexOfFirst { it.id == instanceId }
            if (index == -1) return@updateState state

            val instance = items[index]
            val itemDef = GameConfig.ITEMS[instance.itemDefId] ?: return@updateState state

            items.removeAt(index)

            var equippedWeaponId = state.inventory.equippedWeaponId
            var equippedShieldId = state.inventory.equippedShieldId

            if (equippedWeaponId == instanceId) equippedWeaponId = null
            if (equippedShieldId == instanceId) equippedShieldId = null

            val logs = state.battleLog.toMutableList()
            logs.add("💰 Sold ${itemDef.name} for ${itemDef.sellPrice} gold.")

            state.copy(
                player = state.player.copy(gold = state.player.gold + itemDef.sellPrice),
                inventory = state.inventory.copy(
                    items = items,
                    equippedWeaponId = equippedWeaponId,
                    equippedShieldId = equippedShieldId
                ),
                battleLog = if (logs.size > 100) logs.takeLast(100) else logs
            )
        }
    }

    fun equipItem(instanceId: String) {
        repository.updateState { state ->
            val items = state.inventory.items.toMutableList()
            val index = items.indexOfFirst { it.id == instanceId }
            if (index == -1) return@updateState state

            val instance = items[index]
            val itemDef = GameConfig.ITEMS[instance.itemDefId] ?: return@updateState state

            var equippedWeaponId = state.inventory.equippedWeaponId
            var equippedShieldId = state.inventory.equippedShieldId

            // Unequip current item in that slot first
            if (itemDef.type == "WEAPON") {
                // Mark old equipped weapon as unequipped
                if (equippedWeaponId != null) {
                    val oldIndex = items.indexOfFirst { it.id == equippedWeaponId }
                    if (oldIndex != -1) {
                        items[oldIndex] = items[oldIndex].copy(isEquipped = false)
                    }
                }
                equippedWeaponId = instanceId
            } else if (itemDef.type == "SHIELD") {
                // Mark old equipped shield as unequipped
                if (equippedShieldId != null) {
                    val oldIndex = items.indexOfFirst { it.id == equippedShieldId }
                    if (oldIndex != -1) {
                        items[oldIndex] = items[oldIndex].copy(isEquipped = false)
                    }
                }
                equippedShieldId = instanceId
            }

            // Mark new item as equipped
            items[index] = instance.copy(isEquipped = true)

            val logs = state.battleLog.toMutableList()
            logs.add("🛡️ Equipped ${itemDef.name}.")

            state.copy(
                inventory = state.inventory.copy(
                    items = items,
                    equippedWeaponId = equippedWeaponId,
                    equippedShieldId = equippedShieldId
                ),
                battleLog = if (logs.size > 100) logs.takeLast(100) else logs
            )
        }
    }

    fun unequipItem(instanceId: String) {
        repository.updateState { state ->
            val items = state.inventory.items.toMutableList()
            val index = items.indexOfFirst { it.id == instanceId }
            if (index == -1) return@updateState state

            val instance = items[index]
            val itemDef = GameConfig.ITEMS[instance.itemDefId] ?: return@updateState state

            var equippedWeaponId = state.inventory.equippedWeaponId
            var equippedShieldId = state.inventory.equippedShieldId

            if (equippedWeaponId == instanceId) equippedWeaponId = null
            if (equippedShieldId == instanceId) equippedShieldId = null

            items[index] = instance.copy(isEquipped = false)

            val logs = state.battleLog.toMutableList()
            logs.add("🛡️ Unequipped ${itemDef.name}.")

            state.copy(
                inventory = state.inventory.copy(
                    items = items,
                    equippedWeaponId = equippedWeaponId,
                    equippedShieldId = equippedShieldId
                ),
                battleLog = if (logs.size > 100) logs.takeLast(100) else logs
            )
        }
    }

    fun addQuest(title: String, slot: String, activeDays: List<Int>) {
        repository.updateState { state ->
            val newQuest = QuestDefinition(
                id = UUID.randomUUID().toString(),
                title = title,
                slot = slot,
                activeDays = activeDays
            )
            state.copy(questDefinitions = state.questDefinitions + newQuest)
        }
    }

    fun deleteQuest(questId: String) {
        repository.updateState { state ->
            val quests = state.questDefinitions.filter { it.id != questId }
            val dailyStates = state.dailyQuestStates.toMutableMap()
            dailyStates.remove(questId)
            state.copy(
                questDefinitions = quests,
                dailyQuestStates = dailyStates
            )
        }
    }

    fun updateLanguage(lang: String) {
        repository.updateState { state ->
            state.copy(appLanguage = lang)
        }
    }

    fun toggleAlarmMode(active: Boolean) {
        repository.updateState { state ->
            state.copy(alarmModeActive = active)
        }
    }

    fun updateAlarmTimes(times: Map<String, String>) {
        repository.updateState { state ->
            state.copy(alarmTimes = times)
        }
    }

    fun setHydrationTarget(target: Double) {
        repository.updateState { state ->
            state.copy(hydrationTarget = target)
        }
    }

    fun saveCustomRingtonePath() {
        // Just forces state saving and logging that a custom ringtone was updated
        repository.updateState { state ->
            val logs = state.battleLog.toMutableList()
            logs.add("🎵 Uploaded custom ringtone.")
            state.copy(
                battleLog = if (logs.size > 100) logs.takeLast(100) else logs
            )
        }
    }
}
