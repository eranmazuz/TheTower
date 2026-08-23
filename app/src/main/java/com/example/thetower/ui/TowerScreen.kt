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
import androidx.compose.material3.MaterialTheme
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
import com.example.thetower.theme.SystemBorder
import com.example.thetower.theme.SystemCyan
import com.example.thetower.theme.SystemEmerald
import com.example.thetower.theme.SystemGold
import com.example.thetower.theme.SystemIndigo
import com.example.thetower.theme.SystemRuby
import com.example.thetower.theme.SystemSurface
import com.example.thetower.theme.SystemSurfaceElevated
import com.example.thetower.theme.SystemSurfaceHighlight
import com.example.thetower.theme.SystemTextSecondary

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
            .padding(horizontal = 16.dp, vertical = 12.dp)
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
                    color = SystemCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "System Combat Zone",
                    fontSize = 12.sp,
                    color = SystemTextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SystemSurface)
                    .border(1.dp, SystemGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "FLOOR ${state.currentFloor}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SystemGold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 2. ACTIVE MONSTER / BOSS CARD ---
        if (monster != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .techBorder()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
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
                                color = if (monster.isBoss) SystemRuby else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Level ${monster.level}",
                                fontSize = 12.sp,
                                color = SystemTextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val enemyHpRatio = if (monster.maxHp > 0) monster.currentHp.toFloat() / monster.maxHp else 0f
                            LinearProgressIndicator(
                                progress = { enemyHpRatio },
                                color = SystemRuby,
                                trackColor = Color(0xFF28131E),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(7.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${monster.currentHp}/${monster.maxHp} HP",
                                fontSize = 11.sp,
                                color = SystemTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SystemSurfaceElevated)
                                .border(1.dp, SystemBorder, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(monster.avatarEmoji, fontSize = 36.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // DIVISION LINE
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = SystemBorder,
                            thickness = 1.dp
                        )
                        Text(
                            text = " VS ",
                            color = SystemCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = SystemBorder,
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
                                .size(68.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SystemSurfaceElevated)
                                .border(1.dp, SystemBorder, RoundedCornerShape(16.dp)),
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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.player.job,
                                fontSize = 12.sp,
                                color = SystemTextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val playerHpRatio = if (state.player.maxHp > 0) state.player.currentHp.toFloat() / state.player.maxHp else 0f
                            LinearProgressIndicator(
                                progress = { playerHpRatio },
                                color = SystemRuby,
                                trackColor = Color(0xFF28131E),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(7.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${state.player.currentHp}/${state.player.maxHp} HP",
                                fontSize = 11.sp,
                                color = SystemTextSecondary
                            )
                        }
                    }
                }
            }
        } else if (state.floorBossAvailable) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .techBorder(borderColor = SystemGold.copy(alpha = 0.5f), fillColor = SystemSurface)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("👹", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Floor Boss Available!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SystemGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You have completed all scheduled tasks. Challenge the floor boss to ascend!",
                        color = SystemTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onFightBoss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SystemGold)
                    ) {
                        Text(
                            text = stringResource(R.string.floor_boss_button),
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .techBorder()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tower is peaceful. Waiting for the next monster spawn.",
                    color = SystemTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                    containerColor = SystemIndigo,
                    disabledContainerColor = SystemSurfaceHighlight
                )
            ) {
                Text(
                    text = "🧪 " + stringResource(R.string.use_health_potion, potionCount),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = if (potionCount > 0) Color.White else SystemTextSecondary
                )
            }

            Button(
                onClick = onEscape,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SystemSurfaceHighlight)
            ) {
                Text(
                    text = "🏘️ " + stringResource(R.string.escape_town_button),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 4. COMBAT BATTLE LOG ---
        Text(
            text = "SYSTEM COMBAT LOG",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SystemCyan,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        val listState = rememberLazyListState()
        LaunchedEffect(state.battleLog.size) {
            if (state.battleLog.isNotEmpty()) {
                listState.animateScrollToItem(state.battleLog.size - 1)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .techBorder()
                .padding(10.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.battleLog) { log ->
                    Text(
                        text = log,
                        fontSize = 12.sp,
                        color = when {
                            log.startsWith("⚔️") -> SystemEmerald
                            log.contains("❌") || log.contains("💧") || log.contains("💀") -> SystemRuby
                            log.startsWith("✨") || log.startsWith("🏆") -> SystemGold
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
