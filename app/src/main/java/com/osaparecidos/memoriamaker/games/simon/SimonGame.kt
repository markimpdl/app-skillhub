package com.osaparecidos.memoriamaker.games.simon

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.data.GameResult
import com.osaparecidos.memoriamaker.data.Stars
import com.osaparecidos.memoriamaker.ui.components.GameControls
import com.osaparecidos.memoriamaker.ui.components.GameHud
import com.osaparecidos.memoriamaker.ui.components.GameScaffold
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkDark
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.Palette
import com.osaparecidos.memoriamaker.ui.theme.PaletteBlue
import com.osaparecidos.memoriamaker.ui.theme.PaletteGreen
import com.osaparecidos.memoriamaker.ui.theme.PaletteOrange
import com.osaparecidos.memoriamaker.ui.theme.PalettePink
import com.osaparecidos.memoriamaker.ui.theme.PaletteTeal
import com.osaparecidos.memoriamaker.ui.theme.PaletteYellow
import com.osaparecidos.memoriamaker.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private data class Pad(val label: String, val emoji: String, val palette: Palette)

private val PADS = listOf(
    Pad("LED", "💡", PaletteYellow),
    Pad("CHIP", "🖥️", PaletteBlue),
    Pad("MOTOR", "⚙️", PaletteOrange),
    Pad("SENSOR", "📡", PaletteTeal),
    Pad("BATTERY", "🔋", PaletteGreen),
    Pad("MAGNET", "🧲", PalettePink),
)

@Composable
fun SimonGame(duel: Boolean, onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    if (duel) SimonDuel(onExit, onFinish) else SimonSolo(onExit, onFinish)
}

@Composable
private fun SimonSolo(onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var seed by remember { mutableIntStateOf(0) }
    var round by remember(seed) { mutableIntStateOf(0) }
    var watching by remember(seed) { mutableStateOf(true) }
    var finished by remember(seed) { mutableStateOf(false) }

    GameScaffold(title = "Simon · Circuit Sequence", onBack = onExit, showBack = false) {
        Spacer(Modifier.height(12.dp))
        GameHud(
            primaryLabel = "ROUND",
            primaryValue = "$round",
            stats = listOf("STATUS" to if (watching) "WATCH ✨" else "YOUR TURN 👆"),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        SimonBoard(
            active = !finished, seed = seed, mini = false,
            onRound = { round = it }, onWatching = { watching = it },
            onFail = { reached ->
                if (!finished) {
                    finished = true
                    val stars = Stars.forRound(reached.toLong())
                    onFinish(
                        GameResult(
                            game = Game.SIMON, primary = reached.toLong(), secondary = 0, stars = stars,
                            statPanels = listOf("ROUND" to "$reached", "STARS" to "⭐$stars"),
                            gameOver = true,
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
        Spacer(Modifier.height(12.dp))
        GameControls(onExit = onExit, onRestart = { seed++ })
    }
}

@Composable
private fun SimonDuel(onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var seed by remember { mutableIntStateOf(0) }
    var r1 by remember(seed) { mutableIntStateOf(0) }
    var r2 by remember(seed) { mutableIntStateOf(0) }
    var w1 by remember(seed) { mutableStateOf(true) }
    var w2 by remember(seed) { mutableStateOf(true) }
    var finished by remember(seed) { mutableStateOf(false) }

    fun end(loser: String) {
        if (finished) return
        finished = true
        val winner = if (loser == "P1") "P2" else "P1"
        onFinish(
            GameResult(
                game = Game.SIMON, primary = 0, secondary = 0, stars = 0,
                statPanels = listOf("P1 ROUND" to "$r1", "P2 ROUND" to "$r2"),
                duelWinner = winner,
            )
        )
    }

    com.osaparecidos.memoriamaker.ui.components.DuelScaffold(
        p1Score = "P1 R$r1",
        p2Score = "P2 R$r2",
        centerRight = "",
        onRestart = { seed++ },
        onExit = onExit,
        p2Half = {
            Text(if (w2) "WAIT ✋" else "YOUR TURN 👆", color = if (w2) White else PaletteGreen.bg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            SimonBoard(!finished, seed * 2 + 1, mini = true, onRound = { r2 = it }, onWatching = { w2 = it }, onFail = { end("P2") }, modifier = Modifier.fillMaxWidth().weight(1f))
        },
        p1Half = {
            Text(if (w1) "WAIT ✋" else "YOUR TURN 👆", color = if (w1) White else PaletteGreen.bg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            SimonBoard(!finished, seed * 2, mini = true, onRound = { r1 = it }, onWatching = { w1 = it }, onFail = { end("P1") }, modifier = Modifier.fillMaxWidth().weight(1f))
        },
    )
}

/**
 * One player's Simon game: grows the sequence by 1 each round, plays it back (watch), then
 * accepts input. On the first wrong pad it reports the reached round via [onFail].
 */
@Composable
private fun SimonBoard(
    active: Boolean,
    seed: Int,
    mini: Boolean,
    onRound: (Int) -> Unit,
    onWatching: (Boolean) -> Unit,
    onFail: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sequence = remember(seed) { mutableStateListOf<Int>() }
    var lit by remember(seed) { mutableIntStateOf(-1) }
    var accepting by remember(seed) { mutableStateOf(false) }
    var inputIndex by remember(seed) { mutableIntStateOf(0) }
    var playToken by remember(seed) { mutableIntStateOf(0) }
    var countdown by remember(seed) { mutableIntStateOf(0) }
    val rnd = remember(seed) { Random(seed + 3) }
    // Light "you missed" feedback: a quick shake + red border + MISS, then hand off to the result screen.
    val scope = rememberCoroutineScope()
    var missing by remember(seed) { mutableStateOf(false) }
    val shake = remember(seed) { Animatable(0f) }

    // Kick off the first round (a fresh random sequence of length 1).
    LaunchedEffect(seed) {
        delay(600)
        sequence.clear()
        sequence.add(rnd.nextInt(6))
        onRound(1)
        playToken++
    }

    // Play back the current sequence whenever a new round starts.
    LaunchedEffect(playToken) {
        if (playToken == 0) return@LaunchedEffect
        accepting = false
        onWatching(true)
        lit = -1                       // clear any leftover so the FIRST pad always re-flashes
        if (playToken >= 2) {
            // 3·2·1 countdown between rounds (fast)
            for (n in 3 downTo 1) { countdown = n; delay(350) }
            countdown = 0
            delay(350)                 // let the countdown overlay clear before the first flash
        } else {
            delay(500)
        }
        for (pad in sequence) {
            lit = pad
            delay(if (mini) 450 else 620)   // slower flashes, easier to follow
            lit = -1
            delay(260)
        }
        inputIndex = 0
        accepting = true
        onWatching(false)
    }

    fun tap(pad: Int) {
        if (!accepting || !active) return
        lit = pad
        if (pad == sequence[inputIndex]) {
            inputIndex++
            if (inputIndex == sequence.size) {
                accepting = false
                // next round: brand-new random sequence, one longer
                val next = sequence.size + 1
                sequence.clear()
                repeat(next) { sequence.add(rnd.nextInt(6)) }
                onRound(next)
                playToken++
            }
        } else {
            accepting = false
            if (missing) return
            val reached = sequence.size - 1
            missing = true
            scope.launch {
                repeat(4) { shake.animateTo(14f, tween(45)); shake.animateTo(-14f, tween(90)) }
                shake.animateTo(0f, tween(45))
                delay(450)                 // let MISS linger a beat, then go to the result screen
                onFail(reached)
            }
        }
    }

    // clear the tap flash
    LaunchedEffect(lit, accepting) {
        if (accepting && lit >= 0) { delay(220); lit = -1 }
    }

    Box(modifier) {
        Column(Modifier.fillMaxSize().offset(x = shake.value.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PADS.chunked(2).forEach { rowPads ->
                Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowPads.forEach { pad ->
                        val index = PADS.indexOf(pad)
                        PadView(pad, lit == index, dimmed = !accepting, Modifier.weight(1f).fillMaxSize()) { tap(index) }
                    }
                }
            }
        }
        if (countdown > 0) {
            Box(Modifier.matchParentSize().background(InkDark.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                Text("$countdown", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = if (mini) 56.sp else 96.sp)
            }
        }
        if (missing) {
            // shakes with the board, red frame, small MISS pill — light and quick, no full-screen takeover
            Box(
                Modifier.matchParentSize().offset(x = shake.value.dp)
                    .border(6.dp, MissRed, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.clip(RoundedCornerShape(14.dp)).background(MissRed).padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text("MISS", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = if (mini) 26.sp else 44.sp)
                }
            }
        }
    }
}

private val MissRed = Color(0xFFE23B3B)

@Composable
private fun PadView(pad: Pad, lit: Boolean, dimmed: Boolean, modifier: Modifier, onTap: () -> Unit) {
    val scale by animateFloatAsState(if (lit) 1.08f else 1f, label = "scale")
    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            // Dim idle pads only while watching, so the flash pops; full colour on your turn.
            .alpha(if (lit) 1f else if (dimmed) 0.45f else 1f)
            .clip(RoundedCornerShape(20.dp))
            .background(pad.palette.bg)
            // strong black outline on the flashing pad so it clearly stands out
            .then(if (lit) Modifier.border(7.dp, Color.Black, RoundedCornerShape(20.dp)) else Modifier)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        // bright white flash overlay while lit
        if (lit) {
            Box(Modifier.matchParentSize().clip(RoundedCornerShape(20.dp)).background(White.copy(alpha = 0.35f)))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(pad.emoji, fontSize = if (lit) 40.sp else 34.sp)
            Text(pad.label, color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}
