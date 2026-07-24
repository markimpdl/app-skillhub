package com.osaparecidos.memoriamaker.games.cnc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
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
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.MedalGold
import com.osaparecidos.memoriamaker.ui.theme.Muted
import com.osaparecidos.memoriamaker.ui.theme.OffState
import com.osaparecidos.memoriamaker.ui.theme.PaletteGreen
import com.osaparecidos.memoriamaker.ui.theme.PaletteTeal
import com.osaparecidos.memoriamaker.ui.theme.White
import kotlin.math.roundToInt

private data class Cmd(val emoji: String, val label: String)
private data class Program(val goal: String, val correct: List<Cmd>, val chips: List<Cmd>)

private val START = Cmd("📍", "START POINT")
private val SPINON = Cmd("🔄", "SPINDLE ON")
private val MOVE = Cmd("➡️", "MOVE")
private val DRILL = Cmd("🕳️", "DRILL")
private val CUT = Cmd("✂️", "CUT")
private val SPINOFF = Cmd("⏹️", "SPINDLE OFF")

// Short, clear jobs. Slot 1 (START POINT) is pre-placed, so kids only add the rest.
private val PROGRAMS = listOf(
    Program("🕳️ Drill one hole", listOf(START, SPINON, DRILL), listOf(START, SPINON, DRILL, CUT)),
    Program("✂️ Cut a slot", listOf(START, SPINON, MOVE, CUT), listOf(START, SPINON, MOVE, CUT, DRILL)),
    Program("✅ Finish safely", listOf(START, SPINON, DRILL, SPINOFF), listOf(START, SPINON, DRILL, SPINOFF, MOVE)),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CncGame(onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var seed by remember { mutableIntStateOf(0) }
    var index by remember(seed) { mutableIntStateOf(0) }
    var score by remember(seed) { mutableIntStateOf(0) }
    var wrong by remember(seed) { mutableIntStateOf(0) }
    var toast by remember(seed) { mutableStateOf("") }
    var finished by remember(seed) { mutableStateOf(false) }

    val program = PROGRAMS[index]
    // Slot 0 (START POINT) is pre-filled and locked so the machine always begins correctly.
    val slots = remember(seed, index) {
        mutableStateListOf<Cmd?>().apply { repeat(program.correct.size) { i -> add(if (i == 0) program.correct[0] else null) } }
    }

    var containerOrigin by remember { mutableStateOf(Offset.Zero) }
    val slotRects = remember(seed, index) { mutableStateMapOf<Int, Rect>() }
    val chipRects = remember(seed, index) { mutableStateMapOf<Int, Rect>() }
    var dragChip by remember { mutableStateOf<Int?>(null) }
    var dragPos by remember { mutableStateOf(Offset.Zero) }

    fun rel(r: Rect) = r.translate(-containerOrigin.x, -containerOrigin.y)

    fun run() {
        if (finished) return
        val ok = slots.mapIndexed { i, c -> c?.label == program.correct[i].label }.all { it }
        if (ok) {
            score += maxOf(10, 30 - wrong * 5)
            toast = "✅ Program ran!"
            if (index + 1 >= PROGRAMS.size) {
                finished = true
                val stars = when { score >= 80 -> 3; score >= 55 -> 2; else -> 1 }
                onFinish(
                    GameResult(
                        game = Game.CNC, primary = score.toLong(), secondary = 0, stars = stars,
                        statPanels = listOf("SCORE" to "$score", "STARS" to "⭐$stars"),
                    )
                )
            } else {
                index++
                wrong = 0
            }
        } else {
            wrong++
            toast = "⚠️ Wrong order — check the steps"
        }
    }

    GameScaffold(title = "CNC Program", onBack = onExit, showBack = false) {
        Spacer(Modifier.height(8.dp))
        GameHud(
            primaryLabel = "SCORE",
            primaryValue = "$score",
            stats = listOf("JOB" to "${index + 1}/${PROGRAMS.size}"),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(InkNavy).padding(14.dp)) {
            Text("GOAL: ${program.goal}", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text("Drag the steps in order, then press RUN.", color = White.copy(alpha = 0.8f), fontFamily = Fredoka, fontSize = 13.sp)
        }
        Spacer(Modifier.height(10.dp))

        Box(
            Modifier
                .fillMaxWidth().weight(1f)
                .onGloballyPositioned { containerOrigin = it.boundsInWindow().topLeft }
                .pointerInput(seed, index) {
                    detectDragGestures(
                        onDragStart = { start ->
                            dragChip = chipRects.entries.firstOrNull { rel(it.value).contains(start) }?.key
                            dragPos = start
                        },
                        onDrag = { change, _ -> dragPos = change.position; change.consume() },
                        onDragEnd = {
                            val chip = dragChip
                            if (chip != null) {
                                val slot = slotRects.entries.firstOrNull { rel(it.value).contains(dragPos) }?.key
                                // Slot 0 is the locked START POINT — kids fill only slots 1+.
                                if (slot != null && slot != 0) {
                                    val cmd = program.chips[chip]
                                    for (k in slots.indices) if (slots[k]?.label == cmd.label) slots[k] = null
                                    slots[slot] = cmd
                                }
                            }
                            dragChip = null
                        },
                        onDragCancel = { dragChip = null },
                    )
                },
        ) {
            // Steps + controls sit centered in the play area, not pinned to the top.
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                // ordered program slots
                for (i in 0 until program.correct.size) {
                    ProgramSlot(i + 1, slots[i], locked = i == 0, onBounds = { r -> slotRects[i] = r }, onRemove = { slots[i] = null })
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(16.dp))
                if (toast.isNotEmpty()) {
                    Text(toast, color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(6.dp))
                }
                ChunkyButton(text = "▶ RUN PROGRAM", onClick = { run() }, palette = PaletteGreen, modifier = Modifier.fillMaxWidth(), fontSize = 18.sp)
                Spacer(Modifier.height(10.dp))
                Text("DRAG A COMMAND", color = Muted, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    program.chips.forEachIndexed { i, cmd ->
                        val used = slots.any { it?.label == cmd.label }
                        ChipView(cmd, used) { r -> chipRects[i] = r }
                    }
                }
            }
            val chip = dragChip
            if (chip != null) {
                val ghost = program.chips[chip]
                Box(
                    Modifier.offset { IntOffset(dragPos.x.roundToInt() - 90, dragPos.y.roundToInt() - 40) }
                        .clip(RoundedCornerShape(12.dp)).background(PaletteTeal.border).padding(horizontal = 12.dp, vertical = 10.dp),
                ) { Text("${ghost.emoji} ${ghost.label}", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            }
        }

        Spacer(Modifier.height(10.dp))
        GameControls(onExit = onExit, onRestart = { seed++ })
    }
}

@Composable
private fun ProgramSlot(number: Int, cmd: Cmd?, locked: Boolean, onBounds: (Rect) -> Unit, onRemove: () -> Unit) {
    val filled = cmd != null
    Row(
        Modifier.fillMaxWidth().height(56.dp)
            .onGloballyPositioned { onBounds(it.boundsInWindow()) }
            .clip(RoundedCornerShape(12.dp))
            .background(if (locked) PaletteGreen.bg else if (filled) PaletteTeal.bg else OffState)
            .then(if (!filled) Modifier.border(2.dp, Muted, RoundedCornerShape(12.dp)) else Modifier)
            .then(if (filled && !locked) Modifier.clickable(onClick = onRemove) else Modifier)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(if (filled) White else Muted.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
            Text("$number", color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        if (cmd == null) {
            Text("  drag a command here…", color = Muted, fontFamily = Fredoka, fontSize = 15.sp)
        } else {
            Text("  ${cmd.emoji} ${cmd.label}", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Text(if (locked) "🔒" else "✕", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun ChipView(cmd: Cmd, used: Boolean, onBounds: (Rect) -> Unit) {
    Row(
        Modifier.onGloballyPositioned { onBounds(it.boundsInWindow()) }
            .clip(RoundedCornerShape(12.dp))
            .background(if (used) OffState else PaletteTeal.bg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(cmd.emoji, fontSize = 18.sp)
        Text("  ${cmd.label}", color = if (used) Muted else White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
