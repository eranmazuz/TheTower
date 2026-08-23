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
import com.example.thetower.ui.techBorder
import com.example.thetower.ui.techCircle
import com.example.thetower.data.config.GameConfig
import com.example.thetower.data.model.GameState
import androidx.compose.material3.Divider

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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floor Indicator Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Floor ${state.currentFloor}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Monster Card or Boss Invitation
        if (monster != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f)
                    .techBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- TOP HALF: ENEMY INFO ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Enemy Avatar
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .techCircle(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(monster.avatarEmoji, fontSize = 36.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = monster.name + if (monster.isBoss) " (BOSS)" else "",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (monster.isBoss) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Level ${monster.level}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Monster HP Bar
                            val enemyHpRatio = if (monster.maxHp > 0) monster.currentHp.toFloat() / monster.maxHp else 0f
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { enemyHpRatio },
                                    color = Color(0xFFF43F5E), // Enemy Red
                                    trackColor = Color(0x33F43F5E),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${monster.currentHp}/${monster.maxHp}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // --- DIVISION LINE ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                        Text(" VS ", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                    }

                    // --- BOTTOM HALF: PLAYER INFO ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Player Avatar (Class Emoji + Human below it)
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .techCircle(color = MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (state.player.job == "Mage") "🧙" else "⚔️", fontSize = 24.sp)
                                Text("🧑", fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.player.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.player.job,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Player HP Bar
                            val playerHpRatio = if (state.player.maxHp > 0) state.player.currentHp.toFloat() / state.player.maxHp else 0f
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { playerHpRatio },
                                    color = Color(0xFF00E5FF), // Player Cyan
                                    trackColor = Color(0x3300E5FF),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${state.player.currentHp}/${state.player.maxHp}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else if (state.floorBossAvailable) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3E2723)) // Red-brown warm boss panel
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("👹", fontSize = 72.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Floor Boss Available!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFCC80)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You have completed all scheduled tasks for today. Test your strength against the floor boss to ascend!",
                        color = Color(0xFFD7CCC8),
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onFightBoss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00))
                    ) {
                        Text(stringResource(R.string.floor_boss_button), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Placeholder card if monster is empty and no boss (e.g. at slot transition waiting for next run)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tower is peaceful. No monsters spawned.")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onUsePotion,
                enabled = potionCount > 0,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = stringResource(R.string.use_health_potion, potionCount),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onEscape,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(
                    text = stringResource(R.string.escape_town_button),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Battle Log Header
        Text(
            text = "Combat Battle Log",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp)
        )

        // Battle Log LazyColumn
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
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp)
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
                            log.startsWith("⚔️") -> Color(0xFF388E3C) // green hit
                            log.contains("❌") || log.contains("💧") || log.contains("💀") -> Color(0xFFD32F2F) // red failure
                            log.startsWith("✨") || log.startsWith("🏆") -> Color(0xFFFBC02D) // yellow reward
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
