package com.example.thetower.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.thetower.theme.RpgBorder
import com.example.thetower.theme.RpgCardSurface
import com.example.thetower.theme.RpgSlotSurface

fun Modifier.techBorder(
    borderColor: Color = RpgBorder,
    fillColor: Color = RpgCardSurface
) = this
    .clip(RoundedCornerShape(24.dp))
    .background(fillColor)

fun Modifier.techCircle(
    borderColor: Color = RpgBorder,
    fillColor: Color = RpgSlotSurface
) = this
    .clip(RoundedCornerShape(50))
    .background(fillColor)
