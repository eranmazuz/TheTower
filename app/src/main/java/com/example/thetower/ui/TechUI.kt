package com.example.thetower.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

fun Modifier.techBorder(
    color: Color = Color(0xFF2196F3),
    strokeWidth: Float = 4f,
    cornerLength: Float = 24f,
    fillColor: Color = Color(0x152196F3)
) = this.drawBehind {
    val w = size.width
    val h = size.height
    val outerRadius = androidx.compose.ui.geometry.CornerRadius(32f, 32f)
    val innerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)

    // 1. Draw background
    drawRoundRect(
        color = fillColor,
        cornerRadius = outerRadius
    )

    // 2. Thick Outer Border
    drawRoundRect(
        color = color,
        style = Stroke(width = strokeWidth),
        cornerRadius = outerRadius
    )

    // 3. Thin Inner Border with a small gap (classic retro RPG bubble style)
    val gap = 8f
    drawRoundRect(
        color = color.copy(alpha = 0.6f),
        topLeft = Offset(gap, gap),
        size = androidx.compose.ui.geometry.Size(w - gap * 2, h - gap * 2),
        style = Stroke(width = strokeWidth / 2),
        cornerRadius = innerRadius
    )
}

fun Modifier.techCircle(
    color: Color = Color(0xFF00E5FF),
    strokeWidth: Float = 3f,
    fillColor: Color = Color(0x1500E5FF)
) = this.drawBehind {
    val radius = size.minDimension / 2
    val center = Offset(size.width / 2, size.height / 2)

    // Draw filled circle background
    drawCircle(
        color = fillColor,
        center = center,
        radius = radius
    )

    // Draw outer glowing circle
    drawCircle(
        color = color.copy(alpha = 0.3f),
        center = center,
        radius = radius,
        style = Stroke(width = strokeWidth / 2)
    )

    // Draw inner thicker circle
    drawCircle(
        color = color,
        center = center,
        radius = radius - 6f,
        style = Stroke(width = strokeWidth)
    )
}
