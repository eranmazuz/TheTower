package com.example.thetower.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RaceEncounter(
    val raceName: String,
    val encounters: Int = 0,
    val defeats: Int = 0
)
