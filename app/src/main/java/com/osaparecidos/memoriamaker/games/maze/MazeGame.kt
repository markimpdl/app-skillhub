package com.osaparecidos.memoriamaker.games.maze

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.osaparecidos.memoriamaker.ui.theme.OffState
import com.osaparecidos.memoriamaker.ui.theme.PaletteBlue
import com.osaparecidos.memoriamaker.ui.theme.PaletteGreen
import com.osaparecidos.memoriamaker.ui.theme.PaletteOrange
import com.osaparecidos.memoriamaker.ui.theme.PalettePurple
import com.osaparecidos.memoriamaker.ui.theme.PaletteTeal
import com.osaparecidos.memoriamaker.ui.theme.White
import kotlinx.coroutines.delay

// ---- Commands --------------------------------------------------------------

private enum class Cmd(val label: String, val emoji: String) {
    FWD("MOVE", "⬆️"),
    LEFT("TURN LEFT", "↰"),
    RIGHT("TURN RIGHT", "↱"),
    IF_WALL("IF WALL AHEAD →", "❓"),   // runs the NEXT block only if a wall is ahead
    REPEAT2("REPEAT 2×", "🔁"),          // repeats the NEXT block twice
    REPEAT3("REPEAT 3×", "🔁"),          // repeats the NEXT block three times
}

// Which blocks are unlocked at each level: basics only → +IF → +REPEAT → all.
private fun palette(level: Int): List<Cmd> = when {
    level < 3 -> listOf(Cmd.FWD, Cmd.LEFT, Cmd.RIGHT)
    level < 6 -> listOf(Cmd.FWD, Cmd.LEFT, Cmd.RIGHT, Cmd.IF_WALL)
    level < 9 -> listOf(Cmd.FWD, Cmd.LEFT, Cmd.RIGHT, Cmd.REPEAT2, Cmd.REPEAT3)
    else -> Cmd.entries
}

// ---- Mazes (10 levels). '#'=wall '.'=open 'S'=start(faces East) 'G'=goal ----

private val MAZES = listOf(
    listOf("######", "#....#", "#S..G#", "#....#", "######"),
    listOf("######", "#...G#", "#.##.#", "#.##.#", "#S...#", "######"),
    listOf("######", "#S...#", "###.##", "#...##", "#.####", "#..G.#", "######"),
    listOf("######", "#S.#G#", "#.#..#", "#.##.#", "#....#", "######"),
    listOf("#######", "#S....#", "#.###.#", "#.#G#.#", "#.#.#.#", "#...#.#", "#######"),
    listOf("#######", "#S#...#", "#.#.#.#", "#.#.#.#", "#...#G#", "#######"),
    listOf("#######", "#S....#", "#####.#", "#...#.#", "#.#.#.#", "#.#G..#", "#######"),
    listOf("#######", "#S..#G#", "#.#.#.#", "#.#.#.#", "#.#...#", "#.#####", "#######"),
    listOf("#######", "#S....#", "#.###.#", "#.#.#.#", "#.#.#.#", "#G#...#", "#######"),
    listOf("#######", "#S....#", "#.###.#", "#.#.#.#", "#.#.#.#", "#...#G#", "#######"),
)

private data class Maze(val grid: List<String>, val startR: Int, val startC: Int, val goalR: Int, val goalC: Int) {
    val rows get() = grid.size
    val cols get() = grid[0].length
    fun wall(r: Int, c: Int) = r < 0 || c < 0 || r >= rows || c >= cols || grid[r][c] == '#'
}

private fun parseMaze(grid: List<String>): Maze {
    var sr = 0; var sc = 0; var gr = 0; var gc = 0
    grid.forEachIndexed { r, row -> row.forEachIndexed { c, ch -> if (ch == 'S') { sr = r; sc = c }; if (ch == 'G') { gr = r; gc = c } } }
    return Maze(grid, sr, sc, gr, gc)
}

// Facing: 0=N 1=E 2=S 3=W
private val DR = intArrayOf(-1, 0, 1, 0)
private val DC = intArrayOf(0, 1, 0, -1)

@Composable
fun MazeGame(onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var seed by remember { mutableIntStateOf(0) }
    var level by remember(seed) { mutableIntStateOf(0) }
    var score by remember(seed) { mutableIntStateOf(0) }
    var finished by remember(seed) { mutableStateOf(false) }

    val maze = remember(level) { parseMaze(MAZES[level]) }

    // Robot state
    var r by remember(level, seed) { mutableIntStateOf(maze.startR) }
    var c by remember(level, seed) { mutableIntStateOf(maze.startC) }
    var facing by remember(level, seed) { mutableIntStateOf(1) }
    val program = remember(level, seed) { mutableStateListOf<Cmd>() }
    var running by remember(level, seed) { mutableStateOf(false) }
    var runId by remember(level, seed) { mutableIntStateOf(0) }
    var toast by remember(level, seed) { mutableStateOf("") }
    var solved by remember(level, seed) { mutableStateOf(false) }

    fun resetRobot() { r = maze.startR; c = maze.startC; facing = 1 }

    // Execute the program step-by-step with animation whenever RUN bumps runId.
    LaunchedEffect(runId) {
        if (runId == 0) return@LaunchedEffect
        resetRobot()
        running = true; toast = ""
        var rr = maze.startR; var cc = maze.startC; var f = 1
        suspend fun step(cmd: Cmd) {
            when (cmd) {
                Cmd.LEFT -> f = (f + 3) % 4
                Cmd.RIGHT -> f = (f + 1) % 4
                Cmd.FWD -> {
                    val nr = rr + DR[f]; val nc = cc + DC[f]
                    if (!maze.wall(nr, nc)) { rr = nr; cc = nc }
                }
                else -> {}
            }
            r = rr; c = cc; facing = f
            delay(340)
        }
        var i = 0
        var steps = 0
        while (i < program.size && steps < 80 && !solved) {
            val cmd = program[i]
            when (cmd) {
                Cmd.REPEAT2, Cmd.REPEAT3 -> {
                    val n = if (cmd == Cmd.REPEAT2) 2 else 3
                    val next = program.getOrNull(i + 1)
                    if (next != null) repeat(n) { if (!solved) { step(next); steps++; if (rr == maze.goalR && cc == maze.goalC) solved = true } }
                    i += 2
                }
                Cmd.IF_WALL -> {
                    val next = program.getOrNull(i + 1)
                    val wallAhead = maze.wall(rr + DR[f], cc + DC[f])
                    if (next != null && wallAhead) { step(next); steps++ }
                    i += 2
                }
                else -> { step(cmd); steps++; i++ }
            }
            if (rr == maze.goalR && cc == maze.goalC) solved = true
        }
        running = false
        if (solved) {
            val gained = 20
            score += gained
            toast = "👸 Rescued! +$gained"
            delay(1100)
            if (level >= MAZES.size - 1) {
                if (!finished) {
                    finished = true
                    val stars = when { score >= 180 -> 3; score >= 120 -> 2; else -> 1 }
                    onFinish(
                        GameResult(
                            game = Game.MAZE, primary = score.toLong(), secondary = 0, stars = stars,
                            statPanels = listOf("SCORE" to "$score", "STARS" to "⭐$stars"),
                        )
                    )
                }
            } else {
                level++
            }
        } else {
            toast = "Not there yet — try again!"
        }
    }

    GameScaffold(title = "Logic Game", onBack = onExit, showBack = false) {
        Spacer(Modifier.height(6.dp))
        GameHud(
            primaryLabel = "LEVEL",
            primaryValue = "${level + 1}/${MAZES.size}",
            stats = listOf("SCORE" to "$score"),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        Text("Program the hero to reach the princess 👸", color = Muted, fontFamily = Fredoka, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))

        // Maze view (top half of the vertical screen).
        Box(
            Modifier.fillMaxWidth().aspectRatio(maze.cols.toFloat() / maze.rows.toFloat())
                .clip(RoundedCornerShape(12.dp)).background(InkDark),
        ) {
            Column(Modifier.fillMaxSize()) {
                for (rr in 0 until maze.rows) {
                    Row(Modifier.fillMaxWidth().weight(1f)) {
                        for (cc in 0 until maze.cols) {
                            val isWall = maze.grid[rr][cc] == '#'
                            Box(
                                Modifier.weight(1f).fillMaxSize().padding(1.dp)
                                    .background(if (isWall) InkNavy else White.copy(alpha = 0.88f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                when {
                                    rr == r && cc == c -> Canvas(Modifier.fillMaxSize().padding(4.dp)) { drawHero(facing) }
                                    rr == maze.goalR && cc == maze.goalC -> Text("👸", fontSize = 20.sp)
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
            if (toast.isNotEmpty()) {
                Box(Modifier.align(Alignment.Center).clip(RoundedCornerShape(12.dp)).background(MedalGold).padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(toast, color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        // Program strip
        Text("YOUR PROGRAM (tap a block to remove)", color = Muted, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.sp)
        Spacer(Modifier.height(4.dp))
        ProgramStrip(program, running) { idx -> if (!running) program.removeAt(idx) }
        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChunkyButton("▶ RUN", onClick = { if (!running && program.isNotEmpty()) runId++ }, palette = PaletteGreen, modifier = Modifier.weight(2f), fontSize = 18.sp, enabled = !running)
            ChunkyButton("✕ CLEAR", onClick = { if (!running) { program.clear(); resetRobot(); toast = "" } }, palette = PaletteOrange, modifier = Modifier.weight(1f), fontSize = 15.sp, enabled = !running)
        }
        Spacer(Modifier.height(8.dp))
        // Command palette (blocks to connect)
        Text("BLOCKS", color = Muted, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.sp)
        Spacer(Modifier.height(4.dp))
        CmdPalette(palette(level)) { if (!running) program.add(it) }

        Spacer(Modifier.height(8.dp))
        GameControls(onExit = onExit, onRestart = { seed++ })
    }
}

/** A blocky Minecraft-style hero (creeper-green) with a gold arrow showing the way it faces. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHero(facing: Int) {
    val body = Color(0xFF4CAF50)
    val dark = Color(0xFF14331A)
    val w = size.width; val h = size.height
    val pad = minOf(w, h) * 0.14f
    // blocky body
    drawRect(body, Offset(pad, pad), Size(w - 2 * pad, h - 2 * pad))
    // creeper-ish face
    val es = w * 0.15f
    drawRect(dark, Offset(w * 0.32f, h * 0.34f), Size(es, es))
    drawRect(dark, Offset(w * 0.53f, h * 0.34f), Size(es, es))
    drawRect(dark, Offset(w * 0.42f, h * 0.50f), Size(es, es * 1.5f))
    // facing arrow
    val cx = w / 2; val cy = h / 2; val a = minOf(w, h) * 0.15f
    val e = pad * 0.5f
    val tip: Offset; val b1: Offset; val b2: Offset
    when (facing) {
        0 -> { tip = Offset(cx, e); b1 = Offset(cx - a, e + a); b2 = Offset(cx + a, e + a) }
        1 -> { tip = Offset(w - e, cy); b1 = Offset(w - e - a, cy - a); b2 = Offset(w - e - a, cy + a) }
        2 -> { tip = Offset(cx, h - e); b1 = Offset(cx - a, h - e - a); b2 = Offset(cx + a, h - e - a) }
        else -> { tip = Offset(e, cy); b1 = Offset(e + a, cy - a); b2 = Offset(e + a, cy + a) }
    }
    val p = Path().apply { moveTo(tip.x, tip.y); lineTo(b1.x, b1.y); lineTo(b2.x, b2.y); close() }
    drawPath(p, MedalGold)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProgramStrip(program: List<Cmd>, running: Boolean, onRemove: (Int) -> Unit) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(OffState).padding(6.dp)) {
        if (program.isEmpty()) {
            Text("Add blocks below…", color = Muted, fontFamily = Fredoka, fontSize = 13.sp)
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                program.forEachIndexed { i, cmd ->
                    Box(
                        Modifier.clip(RoundedCornerShape(8.dp)).background(cmdColor(cmd))
                            .clickable(enabled = !running) { onRemove(i) }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                    ) { Text("${cmd.emoji} ${cmd.label}", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CmdPalette(cmds: List<Cmd>, onAdd: (Cmd) -> Unit) {
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cmds.forEach { cmd ->
            Box(
                Modifier.clip(RoundedCornerShape(10.dp)).background(cmdColor(cmd))
                    .clickable { onAdd(cmd) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) { Text("${cmd.emoji} ${cmd.label}", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
        }
    }
}

private fun cmdColor(cmd: Cmd) = when (cmd) {
    Cmd.FWD -> PaletteGreen.bg
    Cmd.LEFT, Cmd.RIGHT -> PaletteBlue.bg
    Cmd.IF_WALL -> PaletteOrange.bg
    Cmd.REPEAT2, Cmd.REPEAT3 -> PalettePurple.bg
}
