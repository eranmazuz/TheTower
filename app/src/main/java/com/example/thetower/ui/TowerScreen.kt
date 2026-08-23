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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thetower.R
import com.example.thetower.data.config.GameConfig
import com.example.thetower.data.model.GameState
import com.example.thetower.theme.RpgBorder
import com.example.thetower.theme.RpgButtonDark
import com.example.thetower.theme.RpgCardSurface
import com.example.thetower.theme.RpgCyan
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // --- 1. SYSTEM HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "THE TOWER",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = RpgCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "System Combat Zone",
                    fontSize = 12.sp,
                    color = RpgTextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(RpgSlotSurface)
                    .border(1.dp, RpgGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "FLOOR ${state.currentFloor}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RpgGold
                )
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
                        .padding(16.dp)
                ) {
                    // TOP HALF: ENEMY INFO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = monster.name + if (monster.isBoss) " (BOSS)" else "",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (monster.isBoss) RpgRuby else Color.White
                            )
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // VS SEPARATOR
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = RpgBorder,
                            thickness = 1.dp
                        )
                        Text(
                            text = " VS ",
                            color = RpgCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = RpgBorder,
                            thickness = 1.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // BOTTOM HALF: PLAYER INFO
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
                            Text(
                                text = state.player.job,
                                fontSize = 12.sp,
                                color = RpgTextSecondary
                            )
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onUsePotion,
                enabled = potionCount > 0,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RpgButtonDark,
                    disabledContainerColor = RpgSlotSurface
                )
            ) {
                Text(
                    text = "🧪 " + stringResource(R.string.use_health_potion, potionCount),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = if (potionCount > 0) Color.White else RpgTextSecondary
                )
            }

            Button(
                onClick = onEscape,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RpgButtonDark)
            ) {
                Text(
                    text = "🏘️ " + stringResource(R.string.escape_town_button),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 4. BATTLE LOG CARD ---
        val listState = rememberLazyListState()
        LaunchedEffect(state.battleLog.size) {
            if (state.battleLog.isNotEmpty()) {
                listState.animateScrollToItem(0)
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
                // Centered Title
                Text(
                    text = "Battle Log",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Separator below title
                HorizontalDivider(
                    color = RpgBorder,
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.battleLog.asReversed()) { log ->
                        val isWaterOrPotion = log.contains("water", ignoreCase = true) ||
                                log.contains("Consumed", ignoreCase = true) ||
                                log.contains("potion", ignoreCase = true) ||
                                log.contains("Healed", ignoreCase = true)

                        val isDamage = !isWaterOrPotion && (
                                log.contains("damage", ignoreCase = true) ||
                                log.contains("failed", ignoreCase = true) ||
                                log.contains("retaliat", ignoreCase = true) ||
                                log.contains("Took", ignoreCase = true) ||
                                log.contains("❌") || log.contains("💀")
                        )

                        val isAttack = log.contains("attack", ignoreCase = true) ||
                                log.contains("Dealt", ignoreCase = true) ||
                                log.contains("Hit", ignoreCase = true) ||
                                log.contains("completed", ignoreCase = true) ||
                                log.contains("Defeated", ignoreCase = true) ||
                                log.startsWith("⚔️") || log.startsWith("✨") || log.startsWith("🏆") ||
                                log.contains("XP") || log.contains("Gold")

                        val itemColor = when {
                            isWaterOrPotion -> RpgCyan // Cyan for water and healing
                            isDamage -> RpgRuby // Red for taking damage / failure
                            isAttack -> RpgGold // Yellow for attacking / rewards
                            else -> RpgTextPrimary
                        }

                        Text(
                            text = "• $log",
                            fontSize = 12.sp,
                            color = itemColor,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
