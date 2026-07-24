package com.osaparecidos.memoriamaker.games.wire

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.data.GameResult
import com.osaparecidos.memoriamaker.data.Levels
import com.osaparecidos.memoriamaker.data.Stars
import com.osaparecidos.memoriamaker.ui.components.ChunkyButton
import com.osaparecidos.memoriamaker.ui.components.GameControls
import com.osaparecidos.memoriamaker.ui.components.GameScaffold
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkDark
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.MedalGold
import com.osaparecidos.memoriamaker.ui.theme.Muted
import com.osaparecidos.memoriamaker.ui.theme.OffState
import com.osaparecidos.memoriamaker.ui.theme.PaletteBlue
import com.osaparecidos.memoriamaker.ui.theme.PaletteGreen
import com.osaparecidos.memoriamaker.ui.theme.PaletteOrange
import com.osaparecidos.memoriamaker.ui.theme.PalettePink
import com.osaparecidos.memoriamaker.ui.theme.PaletteTeal
import com.osaparecidos.memoriamaker.ui.theme.White
import kotlinx.coroutines.delay
import kotlin.random.Random

private val WIRE_COLORS = listOf(PaletteBlue.bg, PaletteOrange.bg, PaletteGreen.bg, PalettePink.bg, PaletteTeal.bg)

@Composable
fun WireGame(level: Int, onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    val info = Levels.WIRE.getOrElse(level) { Levels.WIRE.first() }
    val title = "${info.emoji} ${info.name}"
    when (level) {
        3 -> BlackoutLevel(level, title, onExit, onFinish)
        else -> {
            val pairs = when (level) { 0 -> 2; 1 -> 4; else -> 3 }
            ConnectLevel(level, title, pairs, onExit, onFinish)
        }
    }
}

// ---- Connect levels (drag a wire from a terminal to its matching room) -----

@Composable
private fun ConnectLevel(
    level: Int,
    title: String,
    pairs: Int,
    onExit: () -> Unit,
    onFinish: (GameResult) -> Unit,
) {
    var containerOrigin by remember { mutableStateOf(Offset.Zero) }
    val sourceRects = remember { mutableStateMapOf<Int, Rect>() }
    val targetRects = remember { mutableStateMapOf<Int, Rect>() }
    val connected = remember { mutableStateMapOf<Int, Int>() } // source -> target
    var dragSource by remember { mutableStateOf<Int?>(null) }
    var dragPos by remember { mutableStateOf(Offset.Zero) }
    var wrong by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    // Shuffle target order so it's not a straight line.
    val targetOrder = remember(level) { (0 until pairs).shuffled() }

    fun rel(r: Rect) = r.translate(-containerOrigin.x, -containerOrigin.y)

    GameScaffold(title = title, onBack = onExit, showBack = false) {
        Text("Route each wire through the house plan to its room.", color = Muted, fontFamily = Fredoka, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        BoxWithConstraints(
            Modifier
                .fillMaxWidth().weight(1f)
                .onGloballyPositioned { containerOrigin = it.boundsInWindow().topLeft }
                .pointerInput(level) {
                    detectDragGestures(
                        onDragStart = { start ->
                            dragSource = sourceRects.entries.firstOrNull { rel(it.value).contains(start) }?.key
                            dragPos = start
                        },
                        onDrag = { change, _ -> dragPos = change.position },
                        onDragEnd = {
                            val src = dragSource
                            if (src != null) {
                                val hit = targetRects.entries.firstOrNull { rel(it.value).contains(dragPos) }?.key
                                if (hit != null && hit == src) {
                                    connected[src] = hit
                                    if (connected.size == pairs && !finished) {
                                        finished = true
                                        val stars = Stars.forMistakes(wrong)
                                        onFinish(makeResult(level, stars))
                                    }
                                } else if (hit != null) {
                                    wrong++
                                }
                            }
                            dragSource = null
                        },
                    )
                },
        ) {
            val W = maxWidth
            val H = maxHeight
            val n = pairs
            fun bandCenter(i: Int) = (i + 0.5f) / n   // fractional y of room / terminal band i

            // House floor-plan: a fuse panel on the left, rooms stacked on the right in their own
            // walled rooms, joined by a corridor. Room boxes are placed INSIDE these plan rooms.
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                val w = size.width; val h = size.height
                val wallC = InkNavy
                val wt = 8f
                val corridorX = w * 0.36f
                val roomLeft = w * 0.50f

                drawRect(White, Offset(0f, 0f), Size(w, h))
                // outer walls
                drawRect(wallC, Offset(0f, 0f), Size(w, wt))
                drawRect(wallC, Offset(0f, h - wt), Size(w, wt))
                drawRect(wallC, Offset(0f, 0f), Size(wt, h))
                drawRect(wallC, Offset(w - wt, 0f), Size(wt, h))
                // fuse panel box (left)
                val pl = w * 0.04f; val pr = w * 0.20f; val pt = h * 0.06f; val pb = h * 0.94f
                drawRect(wallC, Offset(pl, pt), Size(pr - pl, wt))
                drawRect(wallC, Offset(pl, pb - wt), Size(pr - pl, wt))
                drawRect(wallC, Offset(pl, pt), Size(wt, pb - pt))
                drawRect(wallC, Offset(pr - wt, pt), Size(wt, pb - pt))
                // rooms: vertical wall separating corridor from rooms, with a door per room
                drawRect(wallC, Offset(roomLeft, 0f), Size(wt, h))
                for (i in 0 until n) {
                    val doorY = bandCenter(i) * h
                    val gap = h * 0.055f
                    drawRect(White, Offset(roomLeft - 2f, doorY - gap), Size(wt + 4f, gap * 2))
                }
                // horizontal walls dividing the rooms
                for (i in 1 until n) {
                    drawRect(wallC, Offset(roomLeft, i.toFloat() / n * h), Size(w - roomLeft, wt))
                }

                // routed conduit: terminal → corridor → up/down → into the room
                fun routeTo(sr: Rect, end: Offset, col: Color) {
                    val sc = sr.center
                    drawLine(col, sc, Offset(corridorX, sc.y), strokeWidth = 12f)
                    drawLine(col, Offset(corridorX, sc.y), Offset(corridorX, end.y), strokeWidth = 12f)
                    drawLine(col, Offset(corridorX, end.y), end, strokeWidth = 12f)
                }
                connected.forEach { (s, t) ->
                    val sr = sourceRects[s]?.let(::rel); val tr = targetRects[t]?.let(::rel)
                    if (sr != null && tr != null) routeTo(sr, tr.center, WIRE_COLORS[s % WIRE_COLORS.size])
                }
                val src = dragSource
                if (src != null) {
                    val sr = sourceRects[src]?.let(::rel)
                    if (sr != null) routeTo(sr, dragPos, WIRE_COLORS[src % WIRE_COLORS.size])
                }
            }

            // terminals in the fuse panel (fixed order, left)
            for (i in 0 until n) {
                Terminal(
                    WIRE_COLORS[i % WIRE_COLORS.size], connected.containsKey(i),
                    Modifier.offset(x = W * 0.11f - 28.dp, y = H * bandCenter(i) - 28.dp),
                ) { r -> sourceRects[i] = r }
            }
            // rooms placed inside their plan rooms (shuffled order → not a straight line)
            targetOrder.forEachIndexed { band, roomIdx ->
                RoomBox(
                    roomIdx, WIRE_COLORS[roomIdx % WIRE_COLORS.size], connected.containsValue(roomIdx),
                    Modifier.offset(x = W * 0.74f - 82.dp, y = H * bandCenter(band) - 28.dp),
                ) { r -> targetRects[roomIdx] = r }
            }
        }
        Spacer(Modifier.height(12.dp))
        GameControls(onExit = onExit)
    }
}

@Composable
private fun Terminal(color: Color, connected: Boolean, modifier: Modifier = Modifier, onBounds: (Rect) -> Unit) {
    Box(
        modifier
            .size(56.dp)
            .onGloballyPositioned { onBounds(it.boundsInWindow()) }
            .clip(RoundedCornerShape(14.dp))
            .background(color),
        contentAlignment = Alignment.Center,
    ) { Text(if (connected) "✓" else "⚡", color = White, fontWeight = FontWeight.Bold, fontSize = 22.sp) }
}

@Composable
private fun RoomBox(index: Int, color: Color, lit: Boolean, modifier: Modifier = Modifier, onBounds: (Rect) -> Unit) {
    Row(
        modifier
            .size(width = 164.dp, height = 56.dp)
            .onGloballyPositioned { onBounds(it.boundsInWindow()) }
            .clip(RoundedCornerShape(14.dp))
            .background(if (lit) MedalGold else OffState)
            .border(3.dp, color, RoundedCornerShape(14.dp))   // ring in the wire colour this room needs
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (lit) "💡" else "🏠", fontSize = 20.sp)
        Text(
            if (lit) "  ON!" else "  ROOM ${index + 1}",
            color = if (lit) PaletteGreen.shadow else Muted,
            fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            modifier = Modifier.weight(1f),
        )
        // the cable colour this room needs
        Box(Modifier.size(20.dp).clip(androidx.compose.foundation.shape.CircleShape).background(color))
    }
}

// ---- Final level: Blackout (flip switches before time) --------------------

private val RedPalette = com.osaparecidos.memoriamaker.ui.theme.Palette(
    com.osaparecidos.memoriamaker.ui.theme.AlertRed,
    com.osaparecidos.memoriamaker.ui.theme.AlertRedDark,
    com.osaparecidos.memoriamaker.ui.theme.AlertRedDark,
)

@Composable
private fun BlackoutLevel(level: Int, title: String, onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    val total = 4
    val on = remember { mutableStateListOf(false, false, false, false) }
    val rnd = remember { Random(level + 99) }
    val pos = remember { mutableStateListOf<Offset>().apply { repeat(total) { add(Offset(rnd.nextFloat(), rnd.nextFloat())) } } }
    var timeLeft by remember { mutableIntStateOf(22) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0 && !finished) {
            delay(1000); timeLeft--
        }
        if (!finished) {
            finished = true
            onFinish(makeResult(level, 1)) // ran out of time → 1 star
        }
    }

    // Breakers drift slowly around the screen — you have to chase and tap each one.
    LaunchedEffect(Unit) {
        val vx = FloatArray(total) { (if (rnd.nextBoolean()) 1 else -1) * (0.0028f + rnd.nextFloat() * 0.003f) }
        val vy = FloatArray(total) { (if (rnd.nextBoolean()) 1 else -1) * (0.0028f + rnd.nextFloat() * 0.003f) }
        while (!finished) {
            for (i in 0 until total) {
                if (on[i]) continue
                var nx = pos[i].x + vx[i]
                var ny = pos[i].y + vy[i]
                if (nx < 0f) { nx = 0f; vx[i] = -vx[i] }
                if (nx > 1f) { nx = 1f; vx[i] = -vx[i] }
                if (ny < 0f) { ny = 0f; vy[i] = -vy[i] }
                if (ny > 1f) { ny = 1f; vy[i] = -vy[i] }
                pos[i] = Offset(nx, ny)
            }
            delay(16)
        }
    }

    fun turnOn(i: Int) {
        if (finished || on[i]) return
        on[i] = true
        if (on.all { it }) {
            finished = true
            val stars = when { timeLeft >= 14 -> 3; timeLeft >= 7 -> 2; else -> 1 }
            onFinish(makeResult(level, stars))
        }
    }

    GameScaffold(title = title, onBack = onExit, dark = true, showBack = false) {
        Spacer(Modifier.height(8.dp))
        Box(Modifier.clip(RoundedCornerShape(12.dp)).background(com.osaparecidos.memoriamaker.ui.theme.AlertRed).padding(horizontal = 14.dp, vertical = 6.dp)) {
            Text("HURRY! ${timeLeft}s", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text("Catch every breaker and flip it ON!", color = White, fontFamily = Fredoka, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val bw = 230.dp
            val bh = 92.dp
            val rangeX = (maxWidth - bw).coerceAtLeast(0.dp)
            val rangeY = (maxHeight - bh).coerceAtLeast(0.dp)
            for (i in 0 until total) {
                val p = pos[i]
                ChunkyButton(
                    text = if (on[i]) "BREAKER ${i + 1} ✅" else "BREAKER ${i + 1} ❌",
                    onClick = { turnOn(i) },
                    palette = if (on[i]) PaletteGreen else RedPalette,
                    modifier = Modifier.offset(x = rangeX * p.x, y = rangeY * p.y).width(bw).height(bh),
                    fontSize = 20.sp,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        GameControls(onExit = onExit)
    }
}

private fun makeResult(level: Int, stars: Int) = GameResult(
    game = Game.WIRE,
    primary = stars.toLong(),
    secondary = 0,
    stars = stars,
    statPanels = listOf("LEVEL" to "${level + 1}", "STARS" to "⭐$stars"),
)
