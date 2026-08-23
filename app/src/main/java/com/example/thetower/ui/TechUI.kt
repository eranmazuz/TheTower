package com.example.thetower.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

fun Modifier.techBorder(
    color: Color = Color(0xFF00E5FF),
    strokeWidth: Float = 4f,
    cornerLength: Float = 24f,
    fillColor: Color = Color(0x1500E5FF)
) = this.drawBehind {
    val w = size.width
    val h = size.height

    // 1. Draw semi-transparent background fill
    drawRect(
        color = fillColor
    )

    // 2. Draw thin full outline
    drawRect(
        color = color.copy(alpha = 0.25f),
        style = Stroke(width = strokeWidth / 2)
    )

    // 3. Draw thick corner brackets
    // Top-Left Corner
    drawLine(color, Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth)
    drawLine(color, Offset(0f, 0f), Offset(0f, cornerLength), strokeWidth)

    // Top-Right Corner
    drawLine(color, Offset(w, 0f), Offset(w - cornerLength, 0f), strokeWidth)
    drawLine(color, Offset(w, 0f), Offset(w, cornerLength), strokeWidth)

    // Bottom-Left Corner
    drawLine(color, Offset(0f, h), Offset(cornerLength, h), strokeWidth)
    drawLine(color, Offset(0f, h), Offset(0f, h - cornerLength), strokeWidth)

    // Bottom-Right Corner
    drawLine(color, Offset(w, h), Offset(w - cornerLength, h), strokeWidth)
    drawLine(color, Offset(w, h), Offset(w, h - cornerLength), strokeWidth)
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
