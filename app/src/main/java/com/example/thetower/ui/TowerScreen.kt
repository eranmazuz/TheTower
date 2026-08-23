package com.example.thetower.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thetower.R
import com.example.thetower.data.config.GameConfig
import com.example.thetower.data.model.GameState
import com.example.thetower.theme.RpgButtonDark
import com.example.thetower.theme.RpgCardSurface
import com.example.thetower.theme.RpgCyan
import com.example.thetower.theme.RpgEmerald
import com.example.thetower.theme.RpgGold
import com.example.thetower.theme.RpgRuby
import com.example.thetower.theme.RpgSlotSurface
import com.example.thetower.theme.RpgTextPrimary
import com.example.thetower.theme.RpgTextSecondary

@Composable
fun TowerScreen(
    state: GameState,
    onUsePotion: () -> Unit,
    onEscape: () -> Unit,
    onFightBoss: () -> Unit
) {
    val monster = state.activeMonster
    val potionCount = state.inventory.items.count {
        GameConfig.ITEMS[it.itemDefId]?.type == "POTION"
    }

    val weaponDef = state.inventory.equippedWeaponId?.let { id ->
        state.inventory.items.firstOrNull { it.id == id }
    }?.let { GameConfig.ITEMS[it.itemDefId] }

    val shieldDef = state.inventory.equippedShieldId?.let { id ->
        state.inventory.items.firstOrNull { it.id == id }
    }?.let { GameConfig.ITEMS[it.itemDefId] }

    val totalAtk = state.player.baseAttack + (weaponDef?.attackBonus ?: 0)
    val totalDef = state.player.baseDefense + (shieldDef?.defenseBonus ?: 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // --- 1. TOP HERO SUMMARY CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = RpgCardSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Level ${state.player.level} Hero",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val xpRatio = if (state.player.xpToNextLevel() > 0) state.player.xp.toFloat() / state.player.xpToNextLevel() else 0f
                    LinearProgressIndicator(
                        progress = { xpRatio },
                        color = RpgEmerald,
                        trackColor = RpgSlotSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${state.player.xp}/${state.player.xpToNextLevel()} XP",
                        fontSize = 11.sp,
                        color = RpgTextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Gold Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(RpgSlotSurface)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "🪙 ${state.player.gold}g",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RpgGold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. BATTLE CARD ---
        if (monster != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = RpgCardSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Floor Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(RpgSlotSurface)
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Floor ${state.currentFloor}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ENEMY ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = monster.name + if (monster.isBoss) " (BOSS)" else "",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (monster.isBoss) RpgRuby else Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(monster.avatarEmoji, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Level ${monster.level}",
                                fontSize = 12.sp,
                                color = RpgTextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val enemyHpRatio = if (monster.maxHp > 0) monster.currentHp.toFloat() / monster.maxHp else 0f
                            LinearProgressIndicator(
                                progress = { enemyHpRatio },
                                color = RpgRuby,
                                trackColor = RpgSlotSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(7.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${monster.currentHp}/${monster.maxHp} HP",
                                fontSize = 11.sp,
                                color = RpgTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(RpgSlotSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(monster.avatarEmoji, fontSize = 38.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // HERO ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(RpgSlotSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (state.player.job == "Mage") "🧙" else "⚔️",
                                    fontSize = 36.sp,
                                    modifier = Modifier.offset(y = (-7).dp)
                                )
                                Text(
                                    text = "🧑",
                                    fontSize = 30.sp,
                                    modifier = Modifier.offset(y = 10.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.player.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⚔️ $totalAtk Attack",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RpgGold
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "🛡️ $totalDef Defense",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RpgGold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val playerHpRatio = if (state.player.maxHp > 0) state.player.currentHp.toFloat() / state.player.maxHp else 0f
                            LinearProgressIndicator(
                                progress = { playerHpRatio },
                                color = RpgRuby,
                                trackColor = RpgSlotSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(7.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${state.player.currentHp}/${state.player.maxHp} HP",
                                fontSize = 11.sp,
                                color = RpgTextSecondary
                            )
                        }
                    }
                }
            }
        } else if (state.floorBossAvailable) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = RpgCardSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("👹", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Floor Boss Available!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = RpgGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You have completed all scheduled tasks. Challenge the floor boss to ascend!",
                        color = RpgTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onFightBoss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RpgGold)
                    ) {
                        Text(
                            text = stringResource(R.string.floor_boss_button),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF13121D)
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = RpgCardSurface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tower is peaceful. Waiting for the next monster spawn.",
                        color = RpgTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 3. ACTION BUTTONS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onUsePotion,
                enabled = potionCount > 0,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RpgButtonDark,
                    disabledContainerColor = RpgSlotSurface
                )
            ) {
                Text(
                    text = "🧪 " + stringResource(R.string.use_health_potion, potionCount),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = if (potionCount > 0) RpgCyan else RpgTextSecondary
                )
            }

            Button(
                onClick = onEscape,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RpgGold)
            ) {
                Text(
                    text = "💤 " + stringResource(R.string.escape_town_button),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = Color(0xFF13121D)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 4. BATTLE LOG CARD ---
        val listState = rememberLazyListState()
        LaunchedEffect(state.battleLog.size) {
            if (state.battleLog.isNotEmpty()) {
                listState.animateScrollToItem(state.battleLog.size - 1)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RpgCardSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Battle Log",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.battleLog) { log ->
                        Text(
                            text = "• $log",
                            fontSize = 12.sp,
                            color = when {
                                log.startsWith("⚔️") || log.contains("Dealt") -> RpgEmerald
                                log.contains("❌") || log.contains("💧") || log.contains("💀") -> RpgRuby
                                log.startsWith("✨") || log.startsWith("🏆") || log.contains("XP") || log.contains("Gold") -> RpgGold
                                else -> RpgTextPrimary
                            },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
