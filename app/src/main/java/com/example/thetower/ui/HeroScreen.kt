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
        // Hero Avatar & Level Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(if (state.player.job == "Mage") "🧙" else "⚔️", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(state.player.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.job_label, state.player.job),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // XP Bar
                    val xpRatio = if (state.player.xpToNextLevel() > 0) state.player.xp.toFloat() / state.player.xpToNextLevel() else 0f
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("XP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { xpRatio },
                            color = Color(0xFFFFCA28),
                            trackColor = Color(0xFFFFF9C4),
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${state.player.xp}/${state.player.xpToNextLevel()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // HP Bar
                    val hpRatio = if (state.player.maxHp > 0) state.player.currentHp.toFloat() / state.player.maxHp else 0f
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { hpRatio },
                            color = Color(0xFF66BB6A),
                            trackColor = Color(0xFFC8E6C9),
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${state.player.currentHp}/${state.player.maxHp}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Combat Stats Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Level", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text("${state.player.level}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Attack", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text(
                            text = "$totalAtk",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFFC62828)
                        )
                        if (weaponDef != null) {
                            Text("(${state.player.baseAttack} + ${weaponDef.attackBonus})", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Defense", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text(
                            text = "$totalDef",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1565C0)
                        )
                        if (shieldDef != null) {
                            Text("(${state.player.baseDefense} + ${shieldDef.defenseBonus})", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Gold", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text("${state.player.gold}g", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFF57F17))
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
