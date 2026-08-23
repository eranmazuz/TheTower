package com.example.thetower.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ItemInstance(
    val id: String, // Unique instance ID (UUID)
    val itemDefId: String, // References ItemDefinition.id
    val isEquipped: Boolean = false
)

@Serializable
data class InventoryData(
    val items: List<ItemInstance> = emptyList(),
    val equippedWeaponId: String? = null,
    val equippedShieldId: String? = null
)
