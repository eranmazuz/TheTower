package com.example.thetower.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thetower.data.model.GameState
import com.example.thetower.data.model.QuestDefinition
import com.example.thetower.theme.RpgBorder
import com.example.thetower.theme.RpgButtonDark
import com.example.thetower.theme.RpgCardSurface
import com.example.thetower.theme.RpgCyan
import com.example.thetower.theme.RpgEmerald
import com.example.thetower.theme.RpgGold
import com.example.thetower.theme.RpgRuby
import com.example.thetower.theme.RpgSlotSurface
import com.example.thetower.theme.RpgTextPrimary
import com.example.thetower.theme.RpgTextSecondary
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun QuestsScreen(
    state: GameState,
    onCompleteQuest: (String) -> Unit,
    onNavigateToTown: () -> Unit
) {
    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek.value // 1 (Mon) to 7 (Sun)
    val dayName = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())

    val todayQuests = state.questDefinitions.filter {
        it.activeDays.isEmpty() || it.activeDays.contains(dayOfWeek)
    }

    val completedCount = todayQuests.count { state.dailyQuestStates[it.id] == true }
    val totalCount = todayQuests.size
    val progressRatio = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    val slots = listOf("MORNING", "NOON", "EVENING", "NIGHT")

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
                    text = "DAILY QUESTS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = RpgCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$dayName Missions",
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
                    text = "$completedCount/$totalCount DONE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (completedCount == totalCount && totalCount > 0) RpgEmerald else RpgGold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. DAILY PROGRESS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = RpgCardSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Habit Mastery",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${(progressRatio * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = RpgEmerald
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progressRatio },
                    color = RpgEmerald,
                    trackColor = RpgSlotSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (totalCount == 0) {
                        "No habits scheduled for today."
                    } else if (completedCount == totalCount) {
                        "🎉 All habits completed! Challenge the Floor Boss!"
                    } else {
                        "Complete habits to strike the monster & earn XP/Gold!"
                    },
                    fontSize = 11.sp,
                    color = RpgTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 3. QUEST LIST BY SLOT ---
        if (todayQuests.isEmpty()) {
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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("📜", fontSize = 54.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Quests Scheduled for Today",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add new habits in the Quest Registry in Town to begin your daily quests.",
                        color = RpgTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToTown,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RpgGold)
                    ) {
                        Text(
                            text = "Go to Quest Registry",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF13121D)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                slots.forEach { slot ->
                    val slotQuests = todayQuests.filter { it.slot.equals(slot, ignoreCase = true) }
                    if (slotQuests.isNotEmpty()) {
                        item(key = "header_$slot") {
                            val slotIcon = when (slot) {
                                "MORNING" -> "🌅"
                                "NOON" -> "☀️"
                                "EVENING" -> "🌆"
                                else -> "🌙"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$slotIcon $slot QUESTS",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RpgGold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = RpgBorder,
                                    thickness = 1.dp
                                )
                            }
                        }

                        items(slotQuests, key = { it.id }) { quest ->
                            val isCompleted = state.dailyQuestStates[quest.id] == true
                            QuestCardItem(
                                quest = quest,
                                isCompleted = isCompleted,
                                onComplete = { onCompleteQuest(quest.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestCardItem(
    quest: QuestDefinition,
    isCompleted: Boolean,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isCompleted) { onComplete() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) RpgSlotSurface.copy(alpha = 0.6f) else RpgCardSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox / Circle status indicator
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) RpgEmerald else RpgButtonDark)
                    .border(
                        1.5.dp,
                        if (isCompleted) RpgEmerald else RpgBorder,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Text("✓", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Quest Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) RpgTextSecondary else Color.White,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(RpgButtonDark)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = quest.slot,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RpgGold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (isCompleted) "Completed (+Attack Dealt)" else "Tap to complete & attack ⚔️",
                        fontSize = 11.sp,
                        color = if (isCompleted) RpgEmerald else RpgCyan
                    )
                }
            }

            if (!isCompleted) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(RpgButtonDark)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⚔️ Attack",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RpgGold
                    )
                }
            }
        }
    }
}
