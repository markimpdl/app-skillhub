package com.osaparecidos.memoriamaker.games.memory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.R
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.data.GameResult
import com.osaparecidos.memoriamaker.data.ScoreRepository
import com.osaparecidos.memoriamaker.data.Stars
import com.osaparecidos.memoriamaker.ui.components.GameControls
import com.osaparecidos.memoriamaker.ui.components.GameHud
import com.osaparecidos.memoriamaker.ui.components.GameScaffold
import com.osaparecidos.memoriamaker.ui.components.LabelPill
import com.osaparecidos.memoriamaker.ui.components.StartOverlay
import com.osaparecidos.memoriamaker.ui.components.glossy
import com.osaparecidos.memoriamaker.ui.theme.Palette
import com.osaparecidos.memoriamaker.ui.theme.PaletteBlue
import com.osaparecidos.memoriamaker.ui.theme.PaletteGreen
import com.osaparecidos.memoriamaker.ui.theme.PaletteOrange
import com.osaparecidos.memoriamaker.ui.theme.PalettePink
import com.osaparecidos.memoriamaker.ui.theme.PalettePurple
import com.osaparecidos.memoriamaker.ui.theme.PaletteTeal
import com.osaparecidos.memoriamaker.ui.theme.PaletteYellow
import com.osaparecidos.memoriamaker.ui.theme.Purple
import com.osaparecidos.memoriamaker.ui.theme.PurpleDark
import com.osaparecidos.memoriamaker.ui.theme.PurpleMid
import com.osaparecidos.memoriamaker.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** A memory pair: emoji or a custom vector, plus its vivid palette. */
private data class MemoryCategory(val label: String, val emoji: String?, val iconRes: Int?, val palette: Palette)

private val ALL_CATEGORIES = listOf(
    // Skills / tech
    MemoryCategory("CODE", "💻", null, PaletteBlue),
    MemoryCategory("AI", "🤖", null, PaletteOrange),
    MemoryCategory("DESIGN", "🖌️", null, PalettePink),
    MemoryCategory("3D PRINT", null, R.drawable.ic_printer, PaletteTeal),
    MemoryCategory("CAD", null, R.drawable.ic_cad, PaletteYellow),
    // Healthcare
    MemoryCategory("HOSPITAL", "🏥", null, PalettePink),   // was 🩺 STETHOSCOPE (Emoji 12, missing on older device fonts)
    MemoryCategory("PILL", "💊", null, PaletteGreen),
    MemoryCategory("SYRINGE", "💉", null, PaletteBlue),
    MemoryCategory("MASK", "😷", null, PaletteOrange),      // was 🩹 BANDAGE (Emoji 12)
    MemoryCategory("THERMOMETER", "🌡️", null, PalettePink),
    MemoryCategory("HEART", "❤️", null, PalettePink),
    MemoryCategory("AMBULANCE", "🚑", null, PaletteBlue),   // was 🦷 TOOTH (Emoji 11)
    // Automotive maintenance
    MemoryCategory("WRENCH", "🔧", null, PaletteTeal),
    MemoryCategory("BOLT", "🔩", null, PaletteYellow),
    MemoryCategory("OIL", "🛢️", null, PaletteOrange),
    MemoryCategory("CAR", "🚗", null, PaletteBlue),
    MemoryCategory("GEAR", "⚙️", null, PaletteTeal),       // was 🛞 TIRE (Emoji 14, missing on most device fonts)
    MemoryCategory("BATTERY", "🔋", null, PaletteGreen),
    MemoryCategory("FUEL", "⛽", null, PaletteOrange),
    // Home automation
    MemoryCategory("SMART HOME", "🏠", null, PaletteTeal),
    MemoryCategory("LIGHT", "💡", null, PaletteYellow),
    MemoryCategory("FAN", "🌀", null, PaletteBlue),
    MemoryCategory("CAMERA", "📷", null, PalettePurple),
    MemoryCategory("PLUG", "🔌", null, PaletteGreen),
    MemoryCategory("LOCK", "🔒", null, PaletteOrange),
    MemoryCategory("DOORBELL", "🔔", null, PaletteYellow),
)

private class MemoryCard(val categoryIndex: Int) {
    var faceUp by mutableStateOf(false)
    var matched by mutableStateOf(false)
}

/** Nav entry point for Memory. */
@Composable
fun MemoryGame(duel: Boolean, onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    if (duel) MemoryDuel(onExit, onFinish) else MemorySolo(onExit, onFinish)
}

// ---- Solo -----------------------------------------------------------------

// Each level doubles the pairs; more columns → smaller cards.
private val LEVEL_PAIRS = listOf(6, 12, 24)
private val LEVEL_COLS = listOf(3, 4, 6)

@Composable
private fun MemorySolo(onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var seed by remember { mutableIntStateOf(0) }
    var level by remember(seed) { mutableIntStateOf(0) }
    val pairs = LEVEL_PAIRS[level]
    val columns = LEVEL_COLS[level]
    val cards = remember(seed, level) { buildDeck(pairs) }
    val scope = rememberCoroutineScope()

    var first by remember(seed, level) { mutableStateOf<MemoryCard?>(null) }
    var busy by remember(seed, level) { mutableStateOf(false) }
    var matches by remember(seed, level) { mutableIntStateOf(0) }
    var moves by remember(seed) { mutableIntStateOf(0) }
    var running by remember(seed) { mutableStateOf(false) }
    var elapsed by remember(seed) { mutableLongStateOf(0L) }
    var finished by remember(seed) { mutableStateOf(false) }
    var started by remember(seed) { mutableStateOf(false) }
    var levelUp by remember(seed) { mutableStateOf(false) }

    LaunchedEffect(running, seed) {
        if (running) {
            val start = System.currentTimeMillis()
            while (running) {
                elapsed = System.currentTimeMillis() - start
                delay(200)
            }
        }
    }

    fun onClick(card: MemoryCard) {
        if (busy || finished || levelUp || card.matched || card.faceUp) return
        if (!running) running = true
        card.faceUp = true
        val f = first
        if (f == null) {
            first = card
        } else {
            busy = true
            moves++
            if (f.categoryIndex == card.categoryIndex) {
                f.matched = true; card.matched = true
                matches++
                first = null; busy = false
                if (matches == pairs) {
                    if (level >= LEVEL_PAIRS.lastIndex) {
                        finished = true; running = false
                        val stars = Stars.forTime(elapsed)
                        onFinish(
                            GameResult(
                                game = Game.MEMORY,
                                primary = elapsed,
                                secondary = moves.toLong(),
                                stars = stars,
                                statPanels = listOf(
                                    "TIME" to ScoreRepository.formatTime(elapsed),
                                    "MOVES" to moves.toString(),
                                ),
                            )
                        )
                    } else {
                        // Level cleared → next level with double the (smaller) cards.
                        levelUp = true
                        scope.launch {
                            delay(1300)
                            levelUp = false
                            level++
                        }
                    }
                }
            } else {
                scope.launch {
                    delay(800)
                    f.faceUp = false; card.faceUp = false
                    first = null; busy = false
                }
            }
        }
    }

    GameScaffold(title = "Memory", onBack = onExit, showBack = false) {
        Spacer(Modifier.height(12.dp))
        GameHud(
            primaryLabel = "TIME",
            primaryValue = ScoreRepository.formatTime(elapsed),
            stats = listOf("LEVEL" to "${level + 1}/${LEVEL_PAIRS.size}", "PAIRS" to "$matches/$pairs", "MOVES" to "$moves"),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().weight(1f)) {
            MemoryGrid(cards = cards, columns = columns, onClick = ::onClick, modifier = Modifier.fillMaxSize())
            if (!started) StartOverlay { started = true }
            if (levelUp) {
                Box(
                    Modifier.fillMaxSize().background(PurpleDark.copy(alpha = 0.82f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⭐ LEVEL UP! ⭐", color = White, fontFamily = com.osaparecidos.memoriamaker.ui.theme.Fredoka, fontWeight = FontWeight.Bold, fontSize = 30.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Level ${level + 2}: ${LEVEL_PAIRS[level + 1]} pairs!", color = White, fontFamily = com.osaparecidos.memoriamaker.ui.theme.Fredoka, fontSize = 18.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        GameControls(onExit = onExit, onRestart = { seed++ })
    }
}

// ---- Duel -----------------------------------------------------------------

@Composable
private fun MemoryDuel(onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    val pairs = 3
    var seed by remember { mutableIntStateOf(0) }
    val p1 = remember(seed) { buildDeck(pairs) }
    val p2 = remember(seed) { buildDeck(pairs) }
    var p1Matches by remember(seed) { mutableIntStateOf(0) }
    var p2Matches by remember(seed) { mutableIntStateOf(0) }
    var winner by remember(seed) { mutableStateOf<String?>(null) }

    fun finish(w: String) {
        if (winner != null) return
        winner = w
        onFinish(
            GameResult(
                game = Game.MEMORY, primary = 0, secondary = 0, stars = 0,
                statPanels = listOf("P1 PAIRS" to "$p1Matches", "P2 PAIRS" to "$p2Matches"),
                duelWinner = w,
            )
        )
    }

    com.osaparecidos.memoriamaker.ui.components.DuelScaffold(
        p1Score = "P1 $p1Matches",
        p2Score = "P2 $p2Matches",
        centerRight = "",
        onRestart = { seed++ },
        onExit = onExit,
        p2Half = {
            Text("PLAYER 2", color = com.osaparecidos.memoriamaker.ui.theme.P2Orange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            MemoryBoardStateful(p2, pairs, Modifier.fillMaxWidth().weight(1f)) { p2Matches = it; if (it == pairs) finish("P2") }
        },
        p1Half = {
            Text("PLAYER 1", color = com.osaparecidos.memoriamaker.ui.theme.P1Score, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            MemoryBoardStateful(p1, pairs, Modifier.fillMaxWidth().weight(1f)) { p1Matches = it; if (it == pairs) finish("P1") }
        },
    )
}

/** A self-contained mini board that reports its match count; used by both duel halves. */
@Composable
private fun MemoryBoardStateful(
    cards: SnapshotStateList<MemoryCard>,
    pairs: Int,
    modifier: Modifier,
    onMatches: (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var first by remember(cards) { mutableStateOf<MemoryCard?>(null) }
    var busy by remember(cards) { mutableStateOf(false) }

    fun onClick(card: MemoryCard) {
        if (busy || card.matched || card.faceUp) return
        card.faceUp = true
        val f = first
        if (f == null) first = card
        else {
            busy = true
            if (f.categoryIndex == card.categoryIndex) {
                f.matched = true; card.matched = true
                first = null; busy = false
                onMatches(cards.count { it.matched } / 2)
            } else scope.launch {
                delay(800); f.faceUp = false; card.faceUp = false; first = null; busy = false
            }
        }
    }
    MemoryGrid(cards = cards, columns = 3, onClick = ::onClick, modifier = modifier)
}

// ---- Shared grid + card ---------------------------------------------------

private fun buildDeck(pairs: Int): SnapshotStateList<MemoryCard> {
    // Pick a fresh random set of categories each round so levels feel different.
    val chosen = ALL_CATEGORIES.indices.shuffled().take(pairs.coerceAtMost(ALL_CATEGORIES.size))
    return (chosen + chosen).shuffled().map { MemoryCard(it) }.toMutableStateList()
}

@Composable
private fun MemoryGrid(
    cards: SnapshotStateList<MemoryCard>,
    columns: Int,
    onClick: (MemoryCard) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Shrink face content + gaps as the grid gets denser.
    val emojiSize = when { columns <= 2 -> 40.sp; columns == 3 -> 34.sp; columns == 4 -> 26.sp; else -> 18.sp }
    val showLabel = columns <= 3
    val gap = if (columns >= 5) 5.dp else 8.dp
    val radius = if (columns >= 5) 10.dp else 18.dp
    Column(modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
        cards.chunked(columns).forEach { rowCards ->
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
                rowCards.forEach { card ->
                    CardView(card, emojiSize, showLabel, radius, Modifier.weight(1f).fillMaxSize()) { onClick(card) }
                }
            }
        }
    }
}

@Composable
private fun CardView(
    card: MemoryCard,
    emojiSize: androidx.compose.ui.unit.TextUnit,
    showLabel: Boolean,
    radius: androidx.compose.ui.unit.Dp,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val category = ALL_CATEGORIES[card.categoryIndex]
    val faceUp = card.faceUp || card.matched
    val palette = category.palette
    // Face-down cards must not leak their pair colour — keep the border purple until revealed.
    val border = when {
        !faceUp -> PurpleMid
        card.matched -> PaletteGreen.border
        else -> palette.border
    }

    Box(
        modifier
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .graphicsLayer { /* subtle press-free flip look handled by content swap */ }
            .clip(RoundedCornerShape(radius))
            .background(if (faceUp) palette.bg else Purple),
        contentAlignment = Alignment.Center,
    ) {
        if (!faceUp) {
            Image(
                painter = painterResource(R.drawable.logo_actvet),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(0.66f).padding(6.dp),
            )
        } else {
            Column(
                Modifier.fillMaxSize().glossy(radius).padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (category.emoji != null) {
                    Text(category.emoji, fontSize = emojiSize)
                } else {
                    Image(painterResource(category.iconRes!!), contentDescription = category.label, modifier = Modifier.height(emojiSize.value.dp))
                }
                if (showLabel) {
                    Spacer(Modifier.height(6.dp))
                    LabelPill(category.label, fontSize = 10.sp)
                }
            }
            if (card.matched) {
                Box(Modifier.fillMaxSize().padding(4.dp), contentAlignment = Alignment.TopEnd) {
                    Text("✓", color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
        // chunky border overlay
        Box(
            Modifier.fillMaxSize().clip(RoundedCornerShape(radius))
                .border(3.dp, border, RoundedCornerShape(radius)),
        )
    }
}
