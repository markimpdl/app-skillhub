package com.osaparecidos.memoriamaker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.osaparecidos.memoriamaker.ui.theme.MedalGold
import com.osaparecidos.memoriamaker.ui.theme.Orange
import com.osaparecidos.memoriamaker.ui.theme.Pink
import com.osaparecidos.memoriamaker.ui.theme.Purple
import com.osaparecidos.memoriamaker.ui.theme.Teal
import kotlin.random.Random

private data class Confetto(val xFrac: Float, val phase: Float, val size: Float, val color: Color, val square: Boolean)

/** Cheap celebratory confetti fall for the Victory screen. */
@Composable
fun ConfettiOverlay(modifier: Modifier = Modifier, pieces: Int = 40) {
    val colors = listOf(Purple, Orange, Pink, Teal, MedalGold)
    val confetti = remember {
        val rnd = Random(1234)
        List(pieces) {
            Confetto(
                xFrac = rnd.nextFloat(),
                phase = rnd.nextFloat(),
                size = 8f + rnd.nextFloat() * 10f,
                color = colors[rnd.nextInt(colors.size)],
                square = rnd.nextBoolean(),
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "fall",
    )

    Canvas(modifier.fillMaxSize()) {
        confetti.forEach { c ->
            val y = (((progress + c.phase) % 1f)) * size.height
            val x = c.xFrac * size.width
            val drift = kotlin.math.sin((progress + c.phase) * 6.28f) * 16f
            if (c.square) {
                drawRect(c.color, topLeft = Offset(x + drift, y), size = Size(c.size, c.size))
            } else {
                drawCircle(c.color, radius = c.size / 2f, center = Offset(x + drift, y))
            }
        }
    }
}
