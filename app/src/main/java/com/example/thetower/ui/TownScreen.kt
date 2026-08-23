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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import com.example.thetower.data.model.ItemDefinition

@Composable
fun TownScreen(
    state: GameState,
    onBuyItem: (String) -> Unit,
    onSellItem: (String) -> Unit,
    onAddQuest: (String, String, List<Int>) -> Unit,
    onDeleteQuest: (String) -> Unit
) {
    var activeSubTab by remember { mutableStateOf(0) } // 0 = Merchant, 1 = Quest Registry

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = activeSubTab) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text(stringResource(R.string.merchant_title)) }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text(stringResource(R.string.quest_registry_title)) }
            )
        }

        when (activeSubTab) {
            0 -> MerchantShopView(state = state, onBuyItem = onBuyItem, onSellItem = onSellItem)
            1 -> QuestRegistryView(state = state, onAddQuest = onAddQuest, onDeleteQuest = onDeleteQuest)
        }
    }
}

@Composable
fun MerchantShopView(
    state: GameState,
    onBuyItem: (String) -> Unit,
    onSellItem: (String) -> Unit
) {
    var shopTab by remember { mutableStateOf(0) } // 0 = Buy, 1 = Sell

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Gold display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🪙 Gold Balance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "${state.player.gold}g",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Inner Buy/Sell selector
        TabRow(
            selectedTabIndex = shopTab,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Tab(
                selected = shopTab == 0,
                onClick = { shopTab = 0 },
                text = { Text(stringResource(R.string.buy_tab)) }
            )
            Tab(
                selected = shopTab == 1,
                onClick = { shopTab = 1 },
                text = { Text(stringResource(R.string.sell_tab)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (shopTab == 0) {
            // BUY LIST
            val itemsForSale = GameConfig.ITEMS.values.toList()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(itemsForSale) { item ->
                    MerchantItemRow(
                        item = item,
                        playerGold = state.player.gold,
                        isBuy = true,
                        onAction = { onBuyItem(item.id) }
                    )
                }
            }
        } else {
            // SELL LIST (Player Inventory Items)
            val inventoryItems = state.inventory.items
            if (inventoryItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Your inventory is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(inventoryItems) { instance ->
                        val itemDef = GameConfig.ITEMS[instance.itemDefId]
                        if (itemDef != null) {
                            MerchantInventoryRow(
                                item = itemDef,
                                instanceId = instance.id,
                                isEquipped = instance.isEquipped,
                                onAction = { onSellItem(instance.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MerchantItemRow(
    item: ItemDefinition,
    playerGold: Int,
    isBuy: Boolean,
    onAction: () -> Unit
) {
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
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                val statsText = when (item.type) {
                    "WEAPON" -> "ATK +${item.attackBonus}"
                    "SHIELD" -> "DEF +${item.defenseBonus}"
                    "POTION" -> "Heals +${item.healAmount} HP"
                    else -> ""
                }
                Text(statsText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (isBuy) item.buyPrice else item.sellPrice}g",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Button(
                    onClick = onAction,
                    enabled = !isBuy || (playerGold >= item.buyPrice),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(stringResource(if (isBuy) R.string.buy_button else R.string.sell_button), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun MerchantInventoryRow(
    item: ItemDefinition,
    instanceId: String,
    isEquipped: Boolean,
    onAction: () -> Unit
) {
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
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (isEquipped) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "E",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                val statsText = when (item.type) {
                    "WEAPON" -> "ATK +${item.attackBonus}"
                    "SHIELD" -> "DEF +${item.defenseBonus}"
                    "POTION" -> "Heals +${item.healAmount} HP"
                    else -> ""
                }
                Text(statsText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.sellPrice}g",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Button(
                    onClick = onAction,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.sell_button), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun QuestRegistryView(
    state: GameState,
    onAddQuest: (String, String, List<Int>) -> Unit,
    onDeleteQuest: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSlot by remember { mutableStateOf("MORNING") }
    var selectedDays by remember { mutableStateOf(listOf(1, 2, 3, 4, 5, 6, 7)) } // all days selected by default

    val slots = listOf("MORNING", "NOON", "EVENING", "NIGHT")
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Register New Habit (Quest)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Quest Title (e.g., Do 20 pushups)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Time Slot Selector
            Text("Target Time Slot", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                slots.forEach { slot ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedSlot == slot,
                            onClick = { selectedSlot = slot }
                        )
                        Text(slot.lowercase().capitalize(), fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Days Selector
            Text("Active Days of Week", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (d in 1..7) {
                    val label = dayLabels[d - 1]
                    val isChecked = selectedDays.contains(d)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                selectedDays = if (checked) {
                                    (selectedDays + d).sorted()
                                } else {
                                    selectedDays.filter { it != d }
                                }
                            }
                        )
                        Text(label, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && selectedDays.isNotEmpty()) {
                        onAddQuest(title, selectedSlot, selectedDays)
                        title = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && selectedDays.isNotEmpty()
            ) {
                Text(stringResource(R.string.add_quest_button))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))
            Text("Your Quest Registry Definitions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (state.questDefinitions.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.empty_quests_msg),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(state.questDefinitions) { quest ->
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
                            Text(quest.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            val slotStr = quest.slot.lowercase().capitalize()
                            val daysStr = if (quest.activeDays.size == 7) {
                                "Everyday"
                            } else {
                                quest.activeDays.joinToString(", ") { d ->
                                    when (d) {
                                        1 -> "Mon"
                                        2 -> "Tue"
                                        3 -> "Wed"
                                        4 -> "Thu"
                                        5 -> "Fri"
                                        6 -> "Sat"
                                        7 -> "Sun"
                                        else -> ""
                                    }
                                }
                            }
                            Text("$slotStr  |  $daysStr", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        IconButton(onClick = { onDeleteQuest(quest.id) }) {
                            Text("🗑️", fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}
