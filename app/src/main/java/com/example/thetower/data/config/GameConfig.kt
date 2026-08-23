package com.example.thetower.data.config

import com.example.thetower.data.model.ItemDefinition
import com.example.thetower.data.model.LootDrop
import com.example.thetower.data.model.RaceDefinition
import com.example.thetower.data.model.SinModifier

object GameConfig {

    val ITEMS = mapOf(
        "potion_health" to ItemDefinition(
            id = "potion_health",
            name = "Health Potion",
            type = "POTION",
            healAmount = 25,
            buyPrice = 20,
            sellPrice = 10,
            dropWeight = 50
        ),
        "potion_super" to ItemDefinition(
            id = "potion_super",
            name = "Super Potion",
            type = "POTION",
            healAmount = 60,
            buyPrice = 50,
            sellPrice = 25,
            dropWeight = 25
        ),
        "weapon_wooden_sword" to ItemDefinition(
            id = "weapon_wooden_sword",
            name = "Wooden Sword",
            type = "WEAPON",
            attackBonus = 3,
            buyPrice = 20,
            sellPrice = 10,
            dropWeight = 40
        ),
        "weapon_iron_sword" to ItemDefinition(
            id = "weapon_iron_sword",
            name = "Iron Sword",
            type = "WEAPON",
            attackBonus = 7,
            buyPrice = 60,
            sellPrice = 30,
            dropWeight = 25
        ),
        "weapon_steel_blade" to ItemDefinition(
            id = "weapon_steel_blade",
            name = "Steel Blade",
            type = "WEAPON",
            attackBonus = 15,
            buyPrice = 150,
            sellPrice = 75,
            dropWeight = 10
        ),
        "weapon_excalibur" to ItemDefinition(
            id = "weapon_excalibur",
            name = "Excalibur",
            type = "WEAPON",
            attackBonus = 30,
            buyPrice = 400,
            sellPrice = 200,
            dropWeight = 2
        ),
        "shield_wooden" to ItemDefinition(
            id = "shield_wooden",
            name = "Wooden Shield",
            type = "SHIELD",
            defenseBonus = 2,
            buyPrice = 20,
            sellPrice = 10,
            dropWeight = 40
        ),
        "shield_iron" to ItemDefinition(
            id = "shield_iron",
            name = "Iron Shield",
            type = "SHIELD",
            defenseBonus = 5,
            buyPrice = 60,
            sellPrice = 30,
            dropWeight = 25
        ),
        "shield_steel" to ItemDefinition(
            id = "shield_steel",
            name = "Steel Aegis",
            type = "SHIELD",
            defenseBonus = 12,
            buyPrice = 150,
            sellPrice = 75,
            dropWeight = 10
        ),
        "shield_legendary" to ItemDefinition(
            id = "shield_legendary",
            name = "Aegis of Light",
            type = "SHIELD",
            defenseBonus = 25,
            buyPrice = 400,
            sellPrice = 200,
            dropWeight = 2
        )
    )

    val RACES = listOf(
        RaceDefinition(
            name = "Slime",
            description = "Amorphous blobs of congealed temptation that ooze through the cracks of good intentions. Slow but persistent, slimes represent the small procrastinations that build up over time — harmless alone, deadly in numbers.",
            baseHp = 25,
            baseAttack = 4,
            baseDefense = 1,
            avatarEmoji = "🟢",
            minFloor = 1,
            dropTable = listOf(
                LootDrop("potion_health", 60),
                LootDrop("weapon_wooden_sword", 20),
                LootDrop("shield_wooden", 20)
            ),
            possibleSins = listOf(
                SinModifier("Sloth", hpDelta = 10, attackDelta = -2, defenseDelta = 1, descriptionSuffix = "This slime moves even slower than usual, absorbing hits easily."),
                SinModifier("Gluttony", hpDelta = 20, attackDelta = -1, defenseDelta = -1, descriptionSuffix = "Engorged on sugary treats, it is massive but soft."),
                SinModifier("Desperation", hpDelta = -5, attackDelta = 2, defenseDelta = -1, descriptionSuffix = "Panicking and fluid, it flails wildly.")
            )
        ),
        RaceDefinition(
            name = "Imp",
            description = "Mischievous little fiends that thrive on distraction and impulse. Imps are the voice that says 'just one more scroll,' 'check the notifications,' 'you can start tomorrow.' Quick to strike and hard to catch.",
            baseHp = 30,
            baseAttack = 6,
            baseDefense = 2,
            avatarEmoji = "😈",
            minFloor = 1,
            dropTable = listOf(
                LootDrop("potion_health", 40),
                LootDrop("weapon_iron_sword", 30),
                LootDrop("shield_wooden", 30)
            ),
            possibleSins = listOf(
                SinModifier("Distraction", hpDelta = -5, attackDelta = 3, defenseDelta = -2, descriptionSuffix = "Spamming screens and popups, it deals quick strikes but is brittle."),
                SinModifier("Pride", hpDelta = 10, attackDelta = 1, defenseDelta = 1, descriptionSuffix = "Full of smug self-satisfaction, it stands tall."),
                SinModifier("Wrath", hpDelta = 0, attackDelta = 4, defenseDelta = -3, descriptionSuffix = "Furious and red-faced, it attacks relentlessly.")
            )
        ),
        RaceDefinition(
            name = "Skeleton",
            description = "The hollowed remains of forgotten routines and abandoned goals. Skeletons represent habits that once lived but were allowed to die — now they rise again to block your path. Brittle but relentless.",
            baseHp = 40,
            baseAttack = 8,
            baseDefense = 4,
            avatarEmoji = "💀",
            minFloor = 2,
            dropTable = listOf(
                LootDrop("potion_health", 30),
                LootDrop("potion_super", 10),
                LootDrop("weapon_iron_sword", 30),
                LootDrop("shield_iron", 30)
            ),
            possibleSins = listOf(
                SinModifier("Neglect", hpDelta = 15, attackDelta = -1, defenseDelta = 2, descriptionSuffix = "Dusty and calcified, it is extremely rigid."),
                SinModifier("Resignation", hpDelta = -10, attackDelta = 2, defenseDelta = -1, descriptionSuffix = "Having given up entirely, it strikes with hollow despair."),
                SinModifier("Stagnation", hpDelta = 25, attackDelta = -2, defenseDelta = 4, descriptionSuffix = "Frozen in time, its bones are like steel.")
            )
        ),
        RaceDefinition(
            name = "Siren",
            description = "Enchanting voices that lure you from your path with promises of easy pleasure and instant reward. Sirens are the algorithms and feeds designed to capture attention — beautiful, hypnotic, and dangerous.",
            baseHp = 45,
            baseAttack = 10,
            baseDefense = 3,
            avatarEmoji = "🧞",
            minFloor = 3,
            dropTable = listOf(
                LootDrop("potion_super", 30),
                LootDrop("weapon_steel_blade", 35),
                LootDrop("shield_steel", 35)
            ),
            possibleSins = listOf(
                SinModifier("Addiction", hpDelta = 20, attackDelta = 3, defenseDelta = -2, descriptionSuffix = "It traps you in feedback loops, draining your resolve."),
                SinModifier("Lust", hpDelta = -5, attackDelta = 4, defenseDelta = -1, descriptionSuffix = "Its temptation is fierce and hard to resist."),
                SinModifier("Vanity", hpDelta = 10, attackDelta = 2, defenseDelta = 2, descriptionSuffix = "Its mirrored surface deflects attacks.")
            )
        ),
        RaceDefinition(
            name = "Golem",
            description = "Monumental constructs of accumulated obligations — the big tasks you've been putting off for weeks that have hardened into immovable obstacles. Slow to wake, but once they move, they hit hard.",
            baseHp = 70,
            baseAttack = 14,
            baseDefense = 8,
            avatarEmoji = "👹",
            minFloor = 4,
            dropTable = listOf(
                LootDrop("potion_super", 40),
                LootDrop("weapon_steel_blade", 20),
                LootDrop("shield_steel", 20),
                LootDrop("weapon_excalibur", 10),
                LootDrop("shield_legendary", 10)
            ),
            possibleSins = listOf(
                SinModifier("Overwhelm", hpDelta = 40, attackDelta = -2, defenseDelta = 5, descriptionSuffix = "An absolute mountain of clutter, it looks impossible to move."),
                SinModifier("Apathy", hpDelta = 20, attackDelta = 2, defenseDelta = 2, descriptionSuffix = "Cold and unresponsive, it crushes motivation."),
                SinModifier("Burnout", hpDelta = -15, attackDelta = 6, defenseDelta = -3, descriptionSuffix = "Cracking with volcanic stress, it hits with extreme force.")
            )
        ),
        RaceDefinition(
            name = "Wraith",
            description = "Formless spirits of tasks that were forgotten so completely they no longer even have a name. They drain energy just by being near — the vague anxiety of something you know you're supposed to do but can't remember what.",
            baseHp = 50,
            baseAttack = 12,
            baseDefense = 6,
            avatarEmoji = "👻",
            minFloor = 3,
            dropTable = listOf(
                LootDrop("potion_super", 30),
                LootDrop("weapon_steel_blade", 30),
                LootDrop("shield_steel", 30),
                LootDrop("weapon_excalibur", 5),
                LootDrop("shield_legendary", 5)
            ),
            possibleSins = listOf(
                SinModifier("Anxiety", hpDelta = -10, attackDelta = 5, defenseDelta = -2, descriptionSuffix = "It shivers and shifts, striking with sudden panic."),
                SinModifier("Avoidance", hpDelta = 25, attackDelta = -2, defenseDelta = 4, descriptionSuffix = "It slips away from focus, making it hard to damage."),
                SinModifier("Oblivion", hpDelta = 10, attackDelta = 3, defenseDelta = 1, descriptionSuffix = "Fading into forgetfulness, it drains your energy.")
            )
        )
    )
}
