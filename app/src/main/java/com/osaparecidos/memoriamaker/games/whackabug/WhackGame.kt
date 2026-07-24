package com.osaparecidos.memoriamaker.games.whackabug

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import com.osaparecidos.memoriamaker.ui.components.StartOverlay
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.osaparecidos.memoriamaker.ui.components.GameControls
import com.osaparecidos.memoriamaker.ui.components.GameHud
import com.osaparecidos.memoriamaker.ui.components.GameScaffold
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.MedalGold
import com.osaparecidos.memoriamaker.ui.theme.PanelNavy
import com.osaparecidos.memoriamaker.ui.theme.PaletteGreen
import com.osaparecidos.memoriamaker.ui.theme.White
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val ROUND_MS = 60_000L

/** Assorted critters — a fresh one is picked each time a bug pops up. */
private val BUG_TYPES = listOf("🐛", "🐞", "🦟", "🕷️", "🪲", "🦗", "🐜", "🪳")

@Composable
fun WhackGame(duel: Boolean, onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    if (duel) WhackDuel(onExit, onFinish) else WhackSolo(onExit, onFinish)
}

@Composable
private fun WhackSolo(onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var seed by remember { mutableIntStateOf(0) }
    var score by remember(seed) { mutableIntStateOf(0) }
    var combo by remember(seed) { mutableIntStateOf(1) }
    var timeLeft by remember(seed) { mutableStateOf(ROUND_MS) }
    var active by remember(seed) { mutableStateOf(false) }
    var finished by remember(seed) { mutableStateOf(false) }
    var started by remember(seed) { mutableStateOf(false) }

    LaunchedEffect(active, seed) {
        if (active) {
            val start = System.currentTimeMillis()
            while (active) {
                val e = System.currentTimeMillis() - start
                timeLeft = (ROUND_MS - e).coerceAtLeast(0)
                if (timeLeft == 0L) {
                    active = false; finished = true
                    val stars = Stars.forScore(score.toLong())
                    onFinish(
                        GameResult(
                            game = Game.WHACK, primary = score.toLong(), secondary = 0, stars = stars,
                            statPanels = listOf("SCORE" to "$score", "STARS" to "⭐$stars"),
                        )
                    )
                }
                delay(100)
            }
        }
    }

    GameScaffold(title = "Whack-a-Bug", onBack = onExit, showBack = false) {
        Spacer(Modifier.height(12.dp))
        GameHud(
            primaryLabel = "TIME",
            primaryValue = "${timeLeft / 1000}s",
            stats = listOf("SCORE" to "$score", "COMBO" to "x$combo🔥"),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Text("Squash the bugs before they escape!", color = com.osaparecidos.memoriamaker.ui.theme.Muted, fontFamily = Fredoka, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().weight(1f)) {
            WhackBoard(
                columns = 3, rows = 4, active = active && !finished, seed = seed,
                onFirstTap = {},
                onHit = { score += 10 * combo; combo++ },
                onMiss = { combo = 1 },
                modifier = Modifier.fillMaxSize(),
            )
            if (!started) StartOverlay { started = true; active = true }
        }
        Spacer(Modifier.height(12.dp))
        GameControls(onExit = onExit, onRestart = { seed++ })
    }
}

@Composable
private fun WhackDuel(onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var seed by remember { mutableIntStateOf(0) }
    var s1 by remember(seed) { mutableIntStateOf(0) }
    var c1 by remember(seed) { mutableIntStateOf(1) }
    var s2 by remember(seed) { mutableIntStateOf(0) }
    var c2 by remember(seed) { mutableIntStateOf(1) }
    var timeLeft by remember(seed) { mutableStateOf(ROUND_MS) }
    var finished by remember(seed) { mutableStateOf(false) }

    LaunchedEffect(seed) {
        val start = System.currentTimeMillis()
        while (!finished) {
            val e = System.currentTimeMillis() - start
            timeLeft = (ROUND_MS - e).coerceAtLeast(0)
            if (timeLeft == 0L) {
                finished = true
                val w = if (s1 > s2) "P1" else if (s2 > s1) "P2" else "TIE"
                onFinish(
                    GameResult(
                        game = Game.WHACK, primary = 0, secondary = 0, stars = 0,
                        statPanels = listOf("P1 SCORE" to "$s1", "P2 SCORE" to "$s2"),
                        duelWinner = w,
                    )
                )
            }
            delay(100)
        }
    }

    com.osaparecidos.memoriamaker.ui.components.DuelScaffold(
        p1Score = "P1 $s1",
        p2Score = "P2 $s2",
        centerRight = "${timeLeft / 1000}s",
        onRestart = { seed++ },
        onExit = onExit,
        p2Half = {
            WhackBoard(3, 2, active = !finished, seed = seed * 2 + 1,
                onFirstTap = {}, onHit = { s2 += 10 * c2; c2++ }, onMiss = { c2 = 1 },
                modifier = Modifier.fillMaxWidth().weight(1f))
        },
        p1Half = {
            WhackBoard(3, 2, active = !finished, seed = seed * 2,
                onFirstTap = {}, onHit = { s1 += 10 * c1; c1++ }, onMiss = { c1 = 1 },
                modifier = Modifier.fillMaxWidth().weight(1f))
        },
    )
}

/**
 * A grid of code windows with one bug hopping around while [active]. Reports hits/misses.
 * [onFirstTap] lets the solo screen start its clock on the first interaction.
 */
@Composable
private fun WhackBoard(
    columns: Int,
    rows: Int,
    active: Boolean,
    seed: Int,
    onFirstTap: () -> Unit,
    onHit: () -> Unit,
    onMiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val count = columns * rows
    var bugCell by remember(seed) { mutableIntStateOf(-1) }
    var bugType by remember(seed) { mutableStateOf(BUG_TYPES.first()) }
    var squashCell by remember(seed) { mutableIntStateOf(-1) }
    val rnd = remember(seed) { Random(seed + 7) }

    LaunchedEffect(active, seed) {
        while (active) {
            delay(rnd.nextLong(450, 1000))
            if (!active) break
            val cell = rnd.nextInt(count)
            bugType = BUG_TYPES[rnd.nextInt(BUG_TYPES.size)]
            bugCell = cell
            delay(1000)
            if (bugCell == cell) { // escaped untapped
                bugCell = -1
                onMiss()
            }
        }
        bugCell = -1
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (c in 0 until columns) {
                    val cell = r * columns + c
                    CodeWindow(
                        hasBug = bugCell == cell,
                        bugEmoji = bugType,
                        squashed = squashCell == cell,
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        onTap = {
                            onFirstTap()
                            if (bugCell == cell) {
                                bugCell = -1
                                squashCell = cell
                                onHit()
                            }
                        },
                    )
                }
            }
        }
    }
    LaunchedEffect(squashCell) {
        if (squashCell >= 0) { delay(400); squashCell = -1 }
    }
}

@Composable
private fun CodeWindow(hasBug: Boolean, bugEmoji: String, squashed: Boolean, modifier: Modifier, onTap: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(InkNavy)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onTap),
    ) {
        // title bar with 3 traffic dots
        Row(Modifier.fillMaxWidth().background(PanelNavy).padding(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Dot(0xFFFF5A5A); Dot(0xFFFFC93D); Dot(0xFF3FBF63)
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                squashed -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💥", fontSize = 32.sp)
                    Text("+10", color = PaletteGreen.bg, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                hasBug -> Text(bugEmoji, fontSize = 64.sp)
                else -> Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CodeLine(0.7f, 0xFF3E5A8C); CodeLine(0.5f, 0xFF12B5A5); CodeLine(0.6f, 0xFF6C4DF6)
                }
            }
        }
    }
}

@Composable
private fun Dot(color: Long) {
    Box(Modifier.size(8.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color(color)))
}

@Composable
private fun CodeLine(widthFraction: Float, color: Long) {
    Box(
        Modifier.fillMaxWidth(widthFraction).height(6.dp).clip(RoundedCornerShape(3.dp))
            .background(androidx.compose.ui.graphics.Color(color)),
    )
}
