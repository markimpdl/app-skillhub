package com.osaparecidos.memoriamaker.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * The glossy treatment from the handoff: a white 25% circle overlapping the top-left and a
 * black 10% circle overlapping the bottom-right, drawn over the content and clipped to the tile.
 * Apply after the fill/clip so the circles sit on top of the tile face.
 */
fun Modifier.glossy(cornerRadius: Dp): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .drawWithContent {
        drawContent()
        val r = size.minDimension * 0.55f
        drawCircle(
            color = Color.White.copy(alpha = 0.25f),
            radius = r,
            center = Offset(size.width * 0.08f, size.height * 0.08f),
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.10f),
            radius = r * 0.8f,
            center = Offset(size.width * 0.95f, size.height * 1.0f),
        )
    }
