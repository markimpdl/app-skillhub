package com.osaparecidos.memoriamaker.games.ifthen

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
import kotlin.math.roundToInt
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.data.GameResult
import com.osaparecidos.memoriamaker.data.Stars
import com.osaparecidos.memoriamaker.ui.components.ChunkyButton
import com.osaparecidos.memoriamaker.ui.components.GameControls
import com.osaparecidos.memoriamaker.ui.components.GameScaffold
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.Lavender
import com.osaparecidos.memoriamaker.ui.theme.MedalGold
import com.osaparecidos.memoriamaker.ui.theme.Muted
import com.osaparecidos.memoriamaker.ui.theme.OffState
import com.osaparecidos.memoriamaker.ui.theme.PaletteGreen
import com.osaparecidos.memoriamaker.ui.theme.PaletteOrange
import com.osaparecidos.memoriamaker.ui.theme.PalettePurple
import com.osaparecidos.memoriamaker.ui.theme.White

private data class Block(val emoji: String, val label: String)
private data class Mission(
    val ifEmoji: String,
    val ifText: String,
    val correct: List<Block>,
    val chips: List<Block>,
)

private val MISSIONS = listOf(
    Mission("🌙", "IF NIGHT TIME", listOf(Block("💡", "PORCH LIGHT ON")),
        listOf(Block("💡", "PORCH LIGHT ON"), Block("🔔", "RING BELL"), Block("❄️", "AC ON"))),
    Mission("🌧️", "IF RAIN STARTS", listOf(Block("🪟", "CLOSE WINDOWS")),
        listOf(Block("💡", "LIGHTS ON"), Block("🪟", "CLOSE WINDOWS"), Block("🔔", "RING BELL"), Block("❄️", "AC ON"))),
    // Longer chain — order matters: door first, then lights, then AC.
    Mission("🏡", "YOU REACHED HOME", listOf(Block("🚪", "OPEN THE DOOR"), Block("💡", "TURN ON THE LIGHTS"), Block("❄️", "TURN ON THE AC")),
        listOf(Block("🚪", "OPEN THE DOOR"), Block("💡", "TURN ON THE LIGHTS"), Block("❄️", "TURN ON THE AC"), Block("🔒", "LOCK DOORS"))),
    Mission("🏃", "IF NOBODY HOME", listOf(Block("🔒", "LOCK DOORS"), Block("❄️", "AC OFF")),
        listOf(Block("🔒", "LOCK DOORS"), Block("❄️", "AC OFF"), Block("💡", "LIGHTS ON"), Block("🔔", "RING BELL"))),
    Mission("☀️", "IF 7:00 AM", listOf(Block("🪟", "OPEN BLINDS"), Block("☕", "START COFFEE")),
        listOf(Block("🪟", "OPEN BLINDS"), Block("☕", "START COFFEE"), Block("🔒", "LOCK DOORS"), Block("❄️", "AC OFF"))),
    // Kitchen safety — 3-step ordered chain.
    Mission("🔥", "IF SMOKE DETECTED", listOf(Block("🌀", "FAN ON"), Block("🚰", "GAS OFF"), Block("🚨", "ALARM ON")),
        listOf(Block("🌀", "FAN ON"), Block("🚰", "GAS OFF"), Block("🚨", "ALARM ON"), Block("☕", "START COFFEE"), Block("💡", "LIGHTS ON"))),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IfThenGame(mission: Int, onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    val m = MISSIONS.getOrElse(mission) { MISSIONS.first() }
    val slotCount = m.correct.size

    var containerOrigin by remember { mutableStateOf(Offset.Zero) }
    val slotRects = remember { mutableStateMapOf<Int, Rect>() }
    val slots = remember(mission) { mutableStateListOf<Block?>().apply { repeat(slotCount) { add(null) } } }
    val wrongSlots = remember(mission) { mutableStateListOf<Boolean>().apply { repeat(slotCount) { add(false) } } }
    val chipRects = remember { mutableStateMapOf<Int, Rect>() }
    var dragChip by remember { mutableStateOf<Int?>(null) }
    var dragPos by remember { mutableStateOf(Offset.Zero) }
    var wrong by remember { mutableIntStateOf(0) }
    var toast by remember { mutableStateOf("") }
    var finished by remember { mutableStateOf(false) }

    fun rel(r: Rect) = r.translate(-containerOrigin.x, -containerOrigin.y)

    GameScaffold(title = "Home Automation", onBack = onExit, showBack = false) {
        // One pointerInput container covering the whole play area (workspace + tray), so a drag
        // can start on a tray chip. rel() maps window coords into this container's space.
        Box(
            Modifier
                .fillMaxWidth().weight(1f)
                .onGloballyPositioned { containerOrigin = it.boundsInWindow().topLeft }
                .pointerInput(mission) {
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
                                if (slot != null) {
                                    val block = m.chips[chip]
                                    // Move (no duplicates): drop the block from any slot it already sits in,
                                    // then place it here — replacing whatever was there.
                                    for (k in slots.indices) if (slots[k]?.label == block.label) slots[k] = null
                                    slots[slot] = block
                                    // Placing a block clears the "wrong" hints and the toast.
                                    for (k in wrongSlots.indices) wrongSlots[k] = false
                                    toast = ""
                                }
                            }
                            dragChip = null
                        },
                        onDragCancel = { dragChip = null },
                    )
                },
        ) {
            Column(Modifier.fillMaxSize()) {
                Spacer(Modifier.height(8.dp))
                // mission card
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(InkNavy).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(m.ifEmoji, fontSize = 34.sp)
                    Text("  ${m.ifText}", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(Modifier.height(12.dp))

                // workspace — vertically centered so the chain sits in the middle of the screen
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    Column(Modifier.align(Alignment.Center).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        IfBlock(m.ifEmoji, m.ifText)
                        for (i in 0 until slotCount) {
                            Text("▽", color = Muted, fontSize = 18.sp)
                            ThenSlot(
                                block = slots[i],
                                wrong = wrongSlots[i],
                                onBounds = { r -> slotRects[i] = r },
                                onRemove = { slots[i] = null },
                            )
                        }
                    }
                    if (toast.isNotEmpty()) {
                        Box(Modifier.align(Alignment.Center).clip(RoundedCornerShape(12.dp)).background(MedalGold).padding(16.dp)) {
                            Text(toast, color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }

                // TEST button
                ChunkyButton(
                    text = "▶ TEST IT!",
                    onClick = {
                        if (finished) return@ChunkyButton
                        // Order matters: each step must match its position in the chain.
                        val allCorrect = (0 until slotCount).all { slots[it]?.label == m.correct[it].label }
                        if (allCorrect) {
                            for (k in wrongSlots.indices) wrongSlots[k] = false
                            val stars = Stars.forMistakes(wrong)
                            toast = "✨ It works! +$stars ⭐"
                            finished = true
                            onFinish(
                                GameResult(
                                    game = Game.IFTHEN, primary = stars.toLong(), secondary = 0, stars = stars,
                                    statPanels = listOf("MISSION" to "${mission + 1}", "STARS" to "⭐$stars"),
                                )
                            )
                        } else {
                            wrong++
                            // Flag exactly which steps are wrong (or missing) so kids see what to fix.
                            for (k in 0 until slotCount) wrongSlots[k] = slots[k]?.label != m.correct[k].label
                            toast = "Fix the red steps! ❌"
                        }
                    },
                    palette = PaletteGreen,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 20.sp,
                )
                Spacer(Modifier.height(10.dp))
                // tray (FlowRow so chips wrap and don't steal the drag gesture)
                Text("DRAG A BLOCK", color = Muted, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    m.chips.forEachIndexed { i, block ->
                        val used = slots.any { it?.label == block.label }
                        ChipView(block, used) { r -> chipRects[i] = r }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // floating ghost of the block being dragged
            val chip = dragChip
            if (chip != null) {
                val ghost = m.chips[chip]
                Box(
                    Modifier
                        .offset { IntOffset(dragPos.x.roundToInt() - 110, dragPos.y.roundToInt() - 44) }
                        .clip(RoundedCornerShape(12.dp))
                        .background(PaletteOrange.border)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Text("${ghost.emoji} ${ghost.label}", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        GameControls(onExit = onExit)
    }
}

@Composable
private fun IfBlock(emoji: String, text: String) {
    Row(
        Modifier.clip(RoundedCornerShape(12.dp)).background(MedalGold).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 24.sp)
        Text("  $text", color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun ThenSlot(block: Block?, wrong: Boolean, onBounds: (Rect) -> Unit, onRemove: () -> Unit) {
    val alertRed = com.osaparecidos.memoriamaker.ui.theme.AlertRed
    Box(
        Modifier
            .fillMaxWidth(0.8f).height(56.dp)
            .onGloballyPositioned { onBounds(it.boundsInWindow()) }
            .clip(RoundedCornerShape(12.dp))
            .background(if (wrong) alertRed.copy(alpha = 0.25f) else if (block != null) PalettePurple.bg else OffState)
            .then(if (wrong) Modifier.border(3.dp, alertRed, RoundedCornerShape(12.dp)) else if (block == null) Modifier.border(2.dp, Muted, RoundedCornerShape(12.dp)) else Modifier)
            .then(if (block != null) Modifier.clickable(onClick = onRemove) else Modifier)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (block == null) {
            Text(if (wrong) "⚠ Needs a block here" else "Drop the THEN block here…", color = if (wrong) alertRed else Muted, fontFamily = Fredoka, fontSize = 14.sp)
        } else {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("THEN ${block.emoji} ${block.label}", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text("✕", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ChipView(block: Block, used: Boolean, onBounds: (Rect) -> Unit) {
    Row(
        Modifier
            .onGloballyPositioned { onBounds(it.boundsInWindow()) }
            .clip(RoundedCornerShape(12.dp))
            .background(if (used) OffState else PaletteOrange.bg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(block.emoji, fontSize = 20.sp)
        Text("  ${block.label}", color = if (used) Muted else White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
