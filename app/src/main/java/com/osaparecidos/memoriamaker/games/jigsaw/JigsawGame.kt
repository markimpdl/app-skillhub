package com.osaparecidos.memoriamaker.games.jigsaw

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
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
import com.osaparecidos.memoriamaker.ui.theme.Muted
import com.osaparecidos.memoriamaker.ui.theme.PaletteBlue
import com.osaparecidos.memoriamaker.ui.theme.PalettePink
import com.osaparecidos.memoriamaker.ui.theme.PalettePurple
import com.osaparecidos.memoriamaker.ui.theme.White
import kotlin.math.hypot
import kotlin.math.roundToInt

/** Difficulty presets: Easy = 15 pieces (3×5), Hard ≈ 40 pieces (5×8). */
private data class Difficulty(val label: String, val cols: Int, val rows: Int, val hard: Boolean) {
    val count get() = cols * rows
}

private val EASY = Difficulty("EASY", 3, 5, hard = false)
private val HARD = Difficulty("HARD", 5, 8, hard = true)

@Composable
fun JigsawGame(onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var difficulty by remember { mutableStateOf<Difficulty?>(null) }
    val d = difficulty
    if (d == null) {
        DifficultyPicker(onExit) { difficulty = it }
    } else {
        JigsawBoard(d, onExit, onFinish)
    }
}

@Composable
private fun DifficultyPicker(onExit: () -> Unit, onPick: (Difficulty) -> Unit) {
    GameScaffold(title = "Design Puzzle", onBack = onExit, showBack = false) {
        Spacer(Modifier.weight(1f))
        Text("🧩 Build the artwork!", color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 24.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Choose a difficulty", color = Muted, fontFamily = Fredoka, fontSize = 15.sp)
        Spacer(Modifier.height(24.dp))
        ChunkyButton("😊 EASY · 15 pieces", onClick = { onPick(EASY) }, palette = PaletteBlue, modifier = Modifier.fillMaxWidth(), fontSize = 20.sp)
        Spacer(Modifier.height(16.dp))
        ChunkyButton("🔥 HARD · 40 pieces (3D art)", onClick = { onPick(HARD) }, palette = PalettePurple, modifier = Modifier.fillMaxWidth(), fontSize = 20.sp)
        Spacer(Modifier.weight(1f))
        GameControls(onExit = onExit)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JigsawBoard(d: Difficulty, onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var seed by remember { mutableIntStateOf(0) }
    // Scrambled draw-order of the still-loose pieces (each value is its target cell index).
    val tray = remember(seed) { (0 until d.count).shuffled().toMutableStateList() }
    val placed = remember(seed) { mutableStateListOf<Boolean>().apply { repeat(d.count) { add(false) } } }
    var wrong by remember(seed) { mutableIntStateOf(0) }
    var finished by remember(seed) { mutableStateOf(false) }

    var containerOrigin by remember { mutableStateOf(Offset.Zero) }
    val slotRects = remember(seed) { mutableStateMapOf<Int, Rect>() }
    val pieceRects = remember(seed) { mutableStateMapOf<Int, Rect>() }
    var dragCell by remember { mutableStateOf<Int?>(null) }
    var dragPos by remember { mutableStateOf(Offset.Zero) }

    fun rel(r: Rect) = r.translate(-containerOrigin.x, -containerOrigin.y)
    val placedCount = placed.count { it }

    GameScaffold(title = "Design Puzzle · ${d.label}", onBack = onExit, showBack = false) {
        Spacer(Modifier.height(6.dp))
        GameHud(
            primaryLabel = "PIECES",
            primaryValue = "$placedCount/${d.count}",
            stats = listOf("MISSES" to "$wrong"),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        // Small reference thumbnail of the finished picture (NOT drawn on the board itself).
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.height(64.dp).aspectRatio(d.cols.toFloat() / d.rows.toFloat())
                    .clip(RoundedCornerShape(8.dp)).background(InkDark),
            ) { Canvas(Modifier.fillMaxSize()) { drawArt(size, d.hard, alpha = 1f) } }
            Text(
                "  Reference — drag each piece to its matching spot.",
                color = Muted, fontFamily = Fredoka, fontSize = 13.sp,
            )
        }
        Spacer(Modifier.height(8.dp))

        Box(
            Modifier
                .fillMaxWidth().weight(1f)
                .onGloballyPositioned { containerOrigin = it.boundsInWindow().topLeft }
                .pointerInput(seed) {
                    detectDragGestures(
                        onDragStart = { start ->
                            dragCell = pieceRects.entries.firstOrNull { !placed[it.key] && rel(it.value).contains(start) }?.key
                            dragPos = start
                        },
                        onDrag = { change, _ -> dragPos = change.position; change.consume() },
                        onDragEnd = {
                            val cell = dragCell
                            if (cell != null && !placed[cell]) {
                                val slot = slotRects.entries.firstOrNull { rel(it.value).contains(dragPos) }?.key
                                if (slot == cell) {
                                    placed[cell] = true
                                    tray.remove(cell)
                                    if (placed.all { it } && !finished) {
                                        finished = true
                                        val score = maxOf(10, d.count * 10 - wrong * 3)
                                        val stars = when { wrong == 0 -> 3; wrong <= d.count / 3 -> 2; else -> 1 }
                                        onFinish(
                                            GameResult(
                                                game = Game.JIGSAW, primary = score.toLong(), secondary = 0, stars = stars,
                                                statPanels = listOf("SCORE" to "$score", "STARS" to "⭐$stars"),
                                            )
                                        )
                                    }
                                } else if (slot != null) {
                                    wrong++
                                }
                            }
                            dragCell = null
                        },
                        onDragCancel = { dragCell = null },
                    )
                },
        ) {
            Column(Modifier.fillMaxSize()) {
                // The board takes the space left above the tray; sized to fit BOTH dimensions so the
                // loose-pieces tray below always stays on screen.
                BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    val ratio = d.cols.toFloat() / d.rows.toFloat()
                    val boardW = if (maxWidth <= maxHeight * ratio) maxWidth else maxHeight * ratio
                    Box(
                        Modifier.width(boardW).aspectRatio(ratio)
                            .clip(RoundedCornerShape(12.dp)).background(InkDark),
                    ) {
                        // No background image on the board — only placed pieces reveal the picture.
                        Column(Modifier.fillMaxSize()) {
                            for (r in 0 until d.rows) {
                                Row(Modifier.fillMaxWidth().weight(1f)) {
                                    for (c in 0 until d.cols) {
                                        val cell = r * d.cols + c
                                        Box(
                                            Modifier.weight(1f).fillMaxSize()
                                                .onGloballyPositioned { slotRects[cell] = it.boundsInWindow() }
                                                .border(0.5.dp, White.copy(alpha = 0.15f)),
                                        ) {
                                            if (placed[cell]) PieceSlice(cell, d, Modifier.fillMaxSize())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("PIECES", color = Muted, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                // Loose pieces to drag onto the board.
                val pieceSize = if (d.hard) 40.dp else 56.dp
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    tray.forEach { cell ->
                        if (!placed[cell]) {
                            Box(
                                Modifier.size(pieceSize)
                                    .onGloballyPositioned { pieceRects[cell] = it.boundsInWindow() }
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(2.dp, White, RoundedCornerShape(8.dp)),
                            ) { PieceSlice(cell, d, Modifier.fillMaxSize()) }
                        }
                    }
                }
            }
            // floating dragged piece
            val cell = dragCell
            if (cell != null && !placed[cell]) {
                val s = if (d.hard) 64 else 84
                Box(
                    Modifier.offset { IntOffset(dragPos.x.roundToInt() - s / 2, dragPos.y.roundToInt() - s / 2) }
                        .size(s.dp).clip(RoundedCornerShape(8.dp)).border(3.dp, White, RoundedCornerShape(8.dp)),
                ) { PieceSlice(cell, d, Modifier.fillMaxSize()) }
            }
        }
        Spacer(Modifier.height(8.dp))
        GameControls(onExit = onExit, onRestart = { seed++ })
    }
}

/** One slice of the full artwork — draws the whole painting, offset so only this cell shows. */
@Composable
private fun PieceSlice(cell: Int, d: Difficulty, modifier: Modifier) {
    val col = cell % d.cols
    val row = cell / d.cols
    // clipToBounds is essential: the painting is drawn at full board size and translated, so without
    // clipping a single slice would spill across the whole board (Compose Canvas doesn't clip by default).
    Canvas(modifier.clipToBounds()) {
        val cw = size.width; val ch = size.height
        val board = Size(cw * d.cols, ch * d.rows)
        translate(left = -col * cw, top = -row * ch) {
            drawArt(board, d.hard, alpha = 1f)
        }
    }
}

/**
 * The target artwork, drawn to fill [boardSize]. Easy = a bright landscape painting; Hard = a
 * shaded 3D-sphere composition. Same routine drives the guide, the pieces and the finished picture.
 */
private fun DrawScope.drawArt(boardSize: Size, hard: Boolean, alpha: Float) {
    val w = boardSize.width; val h = boardSize.height
    if (!hard) {
        // sky
        drawRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF4FA9E0), Color(0xFFBFE3F5), Color(0xFFFFE0B3)), startY = 0f, endY = h),
            size = boardSize, alpha = alpha,
        )
        // sun
        drawCircle(Color(0xFFFFD23F), radius = w * 0.16f, center = Offset(w * 0.74f, h * 0.24f), alpha = alpha)
        // hills
        drawCircle(Color(0xFF3FBF63), radius = w * 0.55f, center = Offset(w * 0.20f, h * 1.02f), alpha = alpha)
        drawCircle(Color(0xFF2E9E7B), radius = w * 0.55f, center = Offset(w * 0.85f, h * 1.08f), alpha = alpha)
        // river
        drawCircle(Color(0xFF2F7FD6), radius = w * 0.20f, center = Offset(w * 0.5f, h * 0.98f), alpha = alpha)
    } else {
        // deep gradient backdrop
        drawRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF141C3A), Color(0xFF3A2A6B), Color(0xFF6C4DF6))),
            size = boardSize, alpha = alpha,
        )
        // big shaded sphere (3D)
        val c = Offset(w * 0.45f, h * 0.45f)
        val rad = minOf(w, h) * 0.42f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFFF3B0), Color(0xFFFF8A3D), Color(0xFF7A1F5C)),
                center = Offset(c.x - rad * 0.35f, c.y - rad * 0.35f), radius = rad * 1.4f,
            ),
            radius = rad, center = c, alpha = alpha,
        )
        // small orbiting sphere
        val c2 = Offset(w * 0.80f, h * 0.78f)
        val rad2 = minOf(w, h) * 0.16f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFB0F5FF), Color(0xFF2FA5D6), Color(0xFF122A6B)),
                center = Offset(c2.x - rad2 * 0.4f, c2.y - rad2 * 0.4f), radius = rad2 * 1.5f,
            ),
            radius = rad2, center = c2, alpha = alpha,
        )
    }
}
