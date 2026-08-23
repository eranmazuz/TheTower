package com.example.thetower.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thetower.R
import com.example.thetower.ui.techBorder
import com.example.thetower.data.config.GameConfig
import com.example.thetower.data.model.GameState
import com.example.thetower.data.model.ItemInstance

@Composable
fun HeroScreen(
    state: GameState,
    onAddWater: (Double) -> Unit,
    onLogTemptation: () -> Unit,
    onEquip: (String) -> Unit,
    onUnequip: (String) -> Unit,
    onUsePotion: () -> Unit
) {
    var showTemptationDialog by remember { mutableStateOf(false) }

    // Resolve weapon/shield equipment
    val weaponDef = state.inventory.equippedWeaponId?.let { id ->
        state.inventory.items.firstOrNull { it.id == id }
    }?.let { GameConfig.ITEMS[it.itemDefId] }

    val shieldDef = state.inventory.equippedShieldId?.let { id ->
        state.inventory.items.firstOrNull { it.id == id }
    }?.let { GameConfig.ITEMS[it.itemDefId] }

    val totalAtk = state.player.baseAttack + (weaponDef?.attackBonus ?: 0)
    val totalDef = state.player.baseDefense + (shieldDef?.defenseBonus ?: 0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Solo Leveling STATUS Screen Panel
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .techBorder()
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header: STATUS
                    Text(
                        text = "STATUS",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Ornament Separator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            thickness = 2.dp
                        )
                        Text(" ◆ ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            thickness = 2.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grid of Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("NAME: ${state.player.name.uppercase()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("JOB: ${state.player.job.uppercase()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            val weaponText = if (weaponDef != null) "${state.player.baseAttack} (+${weaponDef.attackBonus})" else "${state.player.baseAttack}"
                            Text("ATK: $weaponText", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("LEVEL: ${state.player.level}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            val shieldText = if (shieldDef != null) "${state.player.baseDefense} (+${shieldDef.defenseBonus})" else "${state.player.baseDefense}"
                            Text("DEF: $shieldText", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("GOLD: ${state.player.gold}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // HP & XP bars
                    val hpRatio = if (state.player.maxHp > 0) state.player.currentHp.toFloat() / state.player.maxHp else 0f
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HP: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(40.dp))
                        LinearProgressIndicator(
                            progress = { hpRatio },
                            color = Color(0xFF00E5FF),
                            trackColor = Color(0x3300E5FF),
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${state.player.currentHp}/${state.player.maxHp}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val xpRatio = if (state.player.xpToNextLevel() > 0) state.player.xp.toFloat() / state.player.xpToNextLevel() else 0f
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("XP: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(40.dp))
                        LinearProgressIndicator(
                            progress = { xpRatio },
                            color = Color(0xFF2979FF),
                            trackColor = Color(0x332979FF),
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${state.player.xp}/${state.player.xpToNextLevel()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Separator 2
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            thickness = 2.dp
                        )
                        Text(" ◆ ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            thickness = 2.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Core Stats (attributes distribution)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("STRENGTH: $totalAtk", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("DEFENSE: $totalDef", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("VITALITY: ${state.player.maxHp}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            val speedAttr = if (state.player.job == "Mage") 15 else 10
                            Text("AGILITY: $speedAttr", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Daily Hydration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.water_tracker_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val waterRatio = (state.hydrationProgress / state.hydrationTarget).toFloat().coerceIn(0f, 1f)
                    val progressStr = String.format("%.2f", state.hydrationProgress)
                    val targetStr = String.format("%.1f", state.hydrationTarget)

                    LinearProgressIndicator(
                        progress = { waterRatio },
                        color = Color(0xFF29B6F6),
                        trackColor = Color(0xFFE1F5FE),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$progressStr / ${targetStr}L",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { onAddWater(0.25) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF039BE5))
                        ) {
                            Text(stringResource(R.string.add_250_ml))
                        }
                        Button(
                            onClick = { onAddWater(0.5) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
                        ) {
                            Text(stringResource(R.string.add_500_ml))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Temptations (Bad Habits) logger
        item {
            Button(
                onClick = { showTemptationDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(stringResource(R.string.temptation_button), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Player Inventory & Equipment",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Inventory Items List
        val inventoryItems = state.inventory.items
        if (inventoryItems.isEmpty()) {
            item {
                Text(
                    text = "Inventory is empty. Fight monsters to earn gear!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(inventoryItems) { instance ->
                val itemDef = GameConfig.ITEMS[instance.itemDefId]
                if (itemDef != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(itemDef.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    if (instance.isEquipped) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "EQUIPPED",
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFE8F5E9))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                val statsText = when (itemDef.type) {
                                    "WEAPON" -> "ATK +${itemDef.attackBonus}"
                                    "SHIELD" -> "DEF +${itemDef.defenseBonus}"
                                    "POTION" -> "Heals +${itemDef.healAmount} HP"
                                    else -> ""
                                }
                                Text(statsText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Row {
                                when (itemDef.type) {
                                    "WEAPON", "SHIELD" -> {
                                        if (instance.isEquipped) {
                                            Button(
                                                onClick = { onUnequip(instance.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                Text(stringResource(R.string.unequip_button), fontSize = 11.sp)
                                            }
                                        } else {
                                            Button(
                                                onClick = { onEquip(instance.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                Text(stringResource(R.string.equip_button), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                    "POTION" -> {
                                        Button(
                                            onClick = onUsePotion,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text(stringResource(R.string.use_button), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Temptation Log confirmation dialog
    if (showTemptationDialog) {
        AlertDialog(
            onDismissRequest = { showTemptationDialog = false },
            title = { Text(stringResource(R.string.temptation_dialog_title)) },
            text = { Text(stringResource(R.string.temptation_dialog_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTemptationDialog = false
                        onLogTemptation()
                    }
                ) {
                    Text(
                        stringResource(R.string.temptation_confirm),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showTemptationDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
