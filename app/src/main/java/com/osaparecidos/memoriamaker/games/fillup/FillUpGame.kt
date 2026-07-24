package com.osaparecidos.memoriamaker.games.fillup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.data.GameResult
import com.osaparecidos.memoriamaker.ui.components.ChunkyButton
import com.osaparecidos.memoriamaker.ui.components.GameControls
import com.osaparecidos.memoriamaker.ui.components.GameHud
import com.osaparecidos.memoriamaker.ui.components.GameScaffold
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkDark
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.MedalGold
import com.osaparecidos.memoriamaker.ui.theme.Muted
import com.osaparecidos.memoriamaker.ui.theme.PaletteBlue
import com.osaparecidos.memoriamaker.ui.theme.PaletteYellow
import com.osaparecidos.memoriamaker.ui.theme.White
import kotlinx.coroutines.delay
import kotlin.math.abs

private const val ROUNDS = 8

private data class Fluid(val name: String, val emoji: String, val color: Color)

private val FLUIDS = listOf(
    Fluid("ENGINE OIL", "🛢️", Color(0xFFB07B2E)),
    Fluid("COOLANT", "❄️", Color(0xFF2FA5D6)),
    Fluid("BRAKE FLUID", "🛑", Color(0xFFE0A400)),
    Fluid("WASHER FLUID", "💧", Color(0xFF3D7BFF)),
)

/** Fill the reservoir and tap STOP right at the target line — closer = more points. */
@Composable
fun FillUpGame(onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var seed by remember { mutableIntStateOf(0) }
    var round by remember(seed) { mutableIntStateOf(0) }
    var level by remember(seed) { mutableFloatStateOf(0f) }
    var target by remember(seed) { mutableStateOf(0.6f) }
    var score by remember(seed) { mutableIntStateOf(0) }
    var running by remember(seed) { mutableStateOf(true) }
    var badge by remember(seed) { mutableStateOf("") }
    var finished by remember(seed) { mutableStateOf(false) }

    val fluid = FLUIDS[round % FLUIDS.size]

    // set the target for the current round (deterministic per seed+round)
    LaunchedEffect(round, seed) {
        val r = ((round * 37 + seed * 13 + 11) % 40) / 100f  // 0.00..0.39
        target = 0.5f + r                                    // 0.50..0.89
        level = 0f
        running = true
    }

    // the fluid rises until STOP (or it overflows)
    LaunchedEffect(round, seed, running) {
        if (!running) return@LaunchedEffect
        val speed = 0.006f + round * 0.0009f
        while (running && level < 1f) {
            level += speed
            delay(16)
        }
        if (running && level >= 1f) {   // overflowed before STOP
            level = 1f
            running = false
            badge = "SPILL! 💦"
        }
    }

    fun stopFill() {
        if (!running || finished) return
        running = false
        val diff = abs(level - target)
        val pts: Int
        when {
            diff < 0.03f -> { pts = 20; badge = "PERFECT! +20" }
            diff < 0.08f -> { pts = 12; badge = "GREAT +12" }
            diff < 0.15f -> { pts = 6; badge = "OK +6" }
            else -> { pts = 0; badge = "TOO ${if (level > target) "MUCH" else "LOW"}!" }
        }
        score += pts
    }

    // advance to next round shortly after a stop/spill so the badge shows
    LaunchedEffect(running, round, seed) {
        if (!running && !finished) {
            delay(950)
            if (round + 1 >= ROUNDS) {
                finished = true
                val stars = when { score >= 120 -> 3; score >= 70 -> 2; else -> 1 }
                onFinish(
                    GameResult(
                        game = Game.FILLUP, primary = score.toLong(), secondary = 0, stars = stars,
                        statPanels = listOf("SCORE" to "$score", "STARS" to "⭐$stars"),
                    )
                )
            } else {
                badge = ""
                round++
            }
        }
    }

    GameScaffold(title = "Top Up Automotive Fluids", onBack = onExit, showBack = false) {
        Spacer(Modifier.height(8.dp))
        GameHud(
            primaryLabel = "SCORE",
            primaryValue = "$score",
            stats = listOf("FILL" to "${round + 1}/$ROUNDS"),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "${fluid.emoji} ${fluid.name} — tap anywhere to stop at the line!",
            color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 16.sp,
        )
        Spacer(Modifier.height(10.dp))

        // the reservoir — tapping ANYWHERE on it stops the fill
        Box(
            Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(18.dp)).background(InkDark)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { stopFill() },
        ) {
            // pouring stream from the container down to the fluid surface
            if (running && level < 1f) {
                Box(
                    Modifier.align(Alignment.TopCenter)
                        .fillMaxHeight((1f - level).coerceIn(0f, 1f))
                        .width(14.dp)
                        .background(fluid.color.copy(alpha = 0.85f)),
                )
            }
            // the container tipping fluid in, sitting at the very top
            Box(
                Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
                    .graphicsLayer { rotationZ = if (running && level < 1f) 28f else 6f }
                    .clip(RoundedCornerShape(10.dp)).background(White)
                    .border(3.dp, fluid.color, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(fluid.emoji, fontSize = 26.sp)
            }
            // liquid
            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(level.coerceIn(0f, 1f))
                    .background(fluid.color),
            )
            // target line at `target` height
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(target.coerceIn(0.05f, 0.95f))) {
                Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(5.dp).background(MedalGold))
                Text(
                    "🎯 FILL TO HERE",
                    color = MedalGold, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(end = 10.dp, top = 2.dp),
                )
            }
            if (badge.isNotEmpty()) {
                Box(Modifier.align(Alignment.Center).clip(RoundedCornerShape(10.dp)).background(White).padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text(badge, color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        ChunkyButton(
            text = "■ STOP",
            onClick = { stopFill() },
            palette = PaletteYellow,
            textColor = InkNavy,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 24.sp,
            enabled = running,
        )
        Spacer(Modifier.height(10.dp))
        GameControls(onExit = onExit, onRestart = { seed++ })
    }
}
