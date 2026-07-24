package com.osaparecidos.memoriamaker.games.printrush

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.data.GameResult
import com.osaparecidos.memoriamaker.data.Stars
import com.osaparecidos.memoriamaker.ui.components.ChunkyButton
import com.osaparecidos.memoriamaker.ui.components.GameControls
import com.osaparecidos.memoriamaker.ui.components.GameHud
import com.osaparecidos.memoriamaker.ui.components.GameScaffold
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkDark
import com.osaparecidos.memoriamaker.ui.theme.MedalGold
import com.osaparecidos.memoriamaker.ui.theme.PaletteGreen
import com.osaparecidos.memoriamaker.ui.theme.PaletteTeal
import com.osaparecidos.memoriamaker.ui.theme.PaletteYellow
import com.osaparecidos.memoriamaker.ui.theme.Purple
import com.osaparecidos.memoriamaker.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val LAYERS = 20

@Composable
fun PrintGame(duel: Boolean, onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    if (duel) PrintDuel(onExit, onFinish) else PrintSolo(onExit, onFinish)
}

@Composable
private fun PrintSolo(onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var seed by remember { mutableIntStateOf(0) }
    var score by remember(seed) { mutableIntStateOf(0) }
    var layer by remember(seed) { mutableIntStateOf(0) }
    var combo by remember(seed) { mutableIntStateOf(0) }
    var finished by remember(seed) { mutableStateOf(false) }

    GameScaffold(title = "3D Print Rush", onBack = onExit, showBack = false) {
        Spacer(Modifier.height(12.dp))
        GameHud(
            primaryLabel = "SCORE",
            primaryValue = "$score",
            stats = listOf("LAYER" to "$layer/$LAYERS", "COMBO" to "x$combo"),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        PrintBoard(
            active = !finished, seed = seed, mini = false,
            onLayer = { layer = it }, onCombo = { combo = it },
            onScore = { score += it },
            onDone = {
                if (!finished) {
                    finished = true
                    val stars = Stars.forScore(score.toLong())
                    onFinish(
                        GameResult(
                            game = Game.PRINT, primary = score.toLong(), secondary = 0, stars = stars,
                            statPanels = listOf("SCORE" to "$score", "STARS" to "⭐$stars"),
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
        Spacer(Modifier.height(10.dp))
        GameControls(onExit = onExit, onRestart = { seed++ })
    }
}

@Composable
private fun PrintDuel(onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var seed by remember { mutableIntStateOf(0) }
    var s1 by remember(seed) { mutableIntStateOf(0) }
    var s2 by remember(seed) { mutableIntStateOf(0) }
    var done1 by remember(seed) { mutableStateOf(false) }
    var done2 by remember(seed) { mutableStateOf(false) }
    var finished by remember(seed) { mutableStateOf(false) }

    LaunchedEffect(done1, done2, seed) {
        if (done1 && done2 && !finished) {
            finished = true
            val w = if (s1 > s2) "P1" else if (s2 > s1) "P2" else "TIE"
            onFinish(
                GameResult(
                    game = Game.PRINT, primary = 0, secondary = 0, stars = 0,
                    statPanels = listOf("P1 SCORE" to "$s1", "P2 SCORE" to "$s2"),
                    duelWinner = w,
                )
            )
        }
    }

    com.osaparecidos.memoriamaker.ui.components.DuelScaffold(
        p1Score = "P1 $s1",
        p2Score = "P2 $s2",
        centerRight = "",
        onRestart = { seed++ },
        onExit = onExit,
        p2Half = {
            PrintBoard(!done2, seed * 2 + 1, mini = true, onLayer = {}, onCombo = {}, onScore = { s2 += it }, onDone = { done2 = true }, modifier = Modifier.fillMaxWidth().weight(1f))
        },
        p1Half = {
            PrintBoard(!done1, seed * 2, mini = true, onLayer = {}, onCombo = {}, onScore = { s1 += it }, onDone = { done1 = true }, modifier = Modifier.fillMaxWidth().weight(1f))
        },
    )
}

/**
 * A print chamber: a yellow layer slides left-and-right (never descends). DROP freezes it onto the
 * stack; the closer to the centred target, the more points (perfect +20 & combo). Each round the
 * layer slides faster. After [LAYERS] drops it reports done.
 */
@Composable
private fun PrintBoard(
    active: Boolean,
    seed: Int,
    mini: Boolean,
    onLayer: (Int) -> Unit,
    onCombo: (Int) -> Unit,
    onScore: (Int) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var x by remember(seed) { mutableFloatStateOf(0.5f) }
    var dir by remember(seed) { mutableStateOf(1f) }
    var layer by remember(seed) { mutableIntStateOf(0) }
    var combo by remember(seed) { mutableIntStateOf(0) }
    var badge by remember(seed) { mutableStateOf("") }
    val placed = remember(seed) { mutableStateListOf<Float>() }   // x of each landed layer, for the stack

    // Drop animation: the yellow piece falls from the rail down onto the stack before it commits.
    var dropping by remember(seed) { mutableStateOf(false) }
    var dropX by remember(seed) { mutableFloatStateOf(0.5f) }
    val fall = remember(seed) { Animatable(0f) }   // 0 = at the rail, 1 = landed on the stack
    val scope = rememberCoroutineScope()

    fun land() {
        if (layer >= LAYERS) return
        val diff = abs(x - 0.5f)
        val points: Int
        when {
            diff < 0.05f -> { points = 20; combo++; badge = "PERFECT! +20" }
            diff < 0.15f -> { points = 10; combo++; badge = "+10" }
            diff < 0.30f -> { points = 5; combo = 0; badge = "+5" }
            else -> { points = 0; combo = 0; badge = "MISS" }
        }
        placed.add(x)
        onScore(points)
        onCombo(combo)
        layer++
        onLayer(layer)
        if (layer >= LAYERS) onDone()
    }

    // Press DROP → freeze the piece, drop it (animate the fall), then commit it to the stack.
    fun drop() {
        if (!active || dropping || layer >= LAYERS) return
        dropX = x
        dropping = true
        scope.launch {
            fall.snapTo(0f)
            fall.animateTo(1f, animationSpec = tween(durationMillis = 240))
            land()
            fall.snapTo(0f)
            dropping = false
        }
    }

    // The piece only slides horizontally; its speed ramps up every round (layer). Paused mid-drop.
    LaunchedEffect(active, seed, layer, dropping) {
        if (dropping || layer >= LAYERS) return@LaunchedEffect
        val hSpeed = 0.008f + layer * 0.0016f
        while (active && !dropping && layer < LAYERS) {
            x += dir * hSpeed
            if (x > 0.95f) { x = 0.95f; dir = -1f }
            if (x < 0.05f) { x = 0.05f; dir = 1f }
            delay(16)
        }
    }

    Column(modifier) {
        BoxWithConstraints(
            Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(18.dp)).background(InkDark),
        ) {
            val w = maxWidth
            val laneTop = 44.dp
            // TARGET LANE (drop here for PERFECT) — bright green centre column, full height
            Box(
                Modifier.align(Alignment.TopCenter).fillMaxHeight().width(66.dp)
                    .background(PaletteGreen.bg.copy(alpha = 0.28f))
                    .border(2.dp, PaletteGreen.bg, RoundedCornerShape(6.dp)),
            )
            // dashed centre guide line
            Box(Modifier.align(Alignment.TopCenter).fillMaxHeight().width(2.dp).background(White.copy(alpha = 0.5f)))
            // target label
            Box(Modifier.align(Alignment.TopCenter).padding(top = 4.dp).clip(RoundedCornerShape(8.dp)).background(PaletteGreen.bg).padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text("🎯 PERFECT", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            // yellow layer: slides left/right on the rail, and when DROP is pressed it falls straight
            // down onto the stack (fall.value 0→1) before committing as a teal layer.
            val pieceX = if (dropping) dropX else x
            val stackSlot = placed.size.coerceAtMost(6)
            val landedY = maxHeight - 22.dp - 14.dp * stackSlot   // top of where this layer will rest
            val pieceY = if (dropping) laneTop + (landedY - laneTop) * fall.value else laneTop
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (w - 60.dp) * pieceX, y = pieceY)
                    .width(60.dp).height(18.dp)
                    .clip(RoundedCornerShape(6.dp)).background(PaletteYellow.bg),
            )
            // stacked layers — each sits where it was dropped, so bad drops overhang visibly
            val recent = placed.takeLast(7)
            recent.forEachIndexed { i, xi ->
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = (w - 80.dp) * (xi - 0.5f), y = -(16.dp + 14.dp * i))
                        .width(80.dp).height(12.dp)
                        .clip(RoundedCornerShape(4.dp)).background(PaletteTeal.bg),
                )
            }
            if (badge.isNotEmpty()) {
                Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp).clip(RoundedCornerShape(10.dp)).background(MedalGold).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(badge, color = InkDark, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            // floating DROP button in the middle of the chamber
            ChunkyButton(
                "▼ DROP",
                onClick = { drop() },
                palette = PaletteYellow,
                textColor = InkDark,
                modifier = Modifier.align(Alignment.Center).width(if (mini) 150.dp else 200.dp),
                fontSize = if (mini) 18.sp else 24.sp,
            )
        }
    }

    LaunchedEffect(badge, layer) {
        if (badge.isNotEmpty()) { delay(600); badge = "" }
    }
}
