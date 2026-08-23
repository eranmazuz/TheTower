package com.example.thetower.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.thetower.theme.SystemBorder
import com.example.thetower.theme.SystemSurface
import com.example.thetower.theme.SystemSurfaceElevated

fun Modifier.techBorder(
    borderColor: Color = SystemBorder,
    fillColor: Color = SystemSurface
) = this
    .clip(RoundedCornerShape(20.dp))
    .background(fillColor)
    .border(1.dp, borderColor, RoundedCornerShape(20.dp))

fun Modifier.techCircle(
    borderColor: Color = SystemBorder,
    fillColor: Color = SystemSurfaceElevated
) = this
    .clip(RoundedCornerShape(50))
    .background(fillColor)
    .border(1.dp, borderColor, RoundedCornerShape(50))
