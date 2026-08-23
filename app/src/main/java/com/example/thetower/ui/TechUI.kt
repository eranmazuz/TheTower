package com.example.thetower.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.techBorder(
    color: Color = Color(0xFF3B82F6),
    fillColor: Color = Color(0xFF1E293B)
) = this
    .clip(RoundedCornerShape(20.dp))
    .background(fillColor)
    .border(1.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))

fun Modifier.techCircle(
    color: Color = Color(0xFF3B82F6),
    fillColor: Color = Color(0xFF0F172A)
) = this
    .clip(RoundedCornerShape(50))
    .background(fillColor)
    .border(1.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(50))
