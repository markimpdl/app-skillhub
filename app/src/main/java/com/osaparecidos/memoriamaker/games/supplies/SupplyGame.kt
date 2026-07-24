package com.osaparecidos.memoriamaker.games.supplies

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.data.GameResult
import com.osaparecidos.memoriamaker.ui.components.GameControls
import com.osaparecidos.memoriamaker.ui.components.GameScaffold
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.Muted
import com.osaparecidos.memoriamaker.ui.theme.OffState
import com.osaparecidos.memoriamaker.ui.theme.PaletteBlue
import com.osaparecidos.memoriamaker.ui.theme.PaletteGreen
import com.osaparecidos.memoriamaker.ui.theme.PalettePink
import com.osaparecidos.memoriamaker.ui.theme.PaletteYellow
import com.osaparecidos.memoriamaker.ui.theme.White
import com.osaparecidos.memoriamaker.ui.theme.Palette
import kotlin.math.roundToInt

private data class Bin(val id: String, val emoji: String, val label: String, val palette: Palette)
private data class Item(val emoji: String, val label: String, val cat: String)

private val BINS = listOf(
    Bin("sharps", "☣️", "SHARPS", PalettePink),
    Bin("ppe", "🧤", "PPE", PaletteBlue),
    Bin("meds", "💊", "MEDS", PaletteGreen),
)

private val ITEMS = listOf(
    Item("💉", "Syringe", "sharps"),
    Item("🔪", "Scalpel", "sharps"),
    Item("📌", "Needle", "sharps"),
    Item("✂️", "Suture", "sharps"),
    Item("🩸", "Lancet", "sharps"),
    Item("🧤", "Gloves", "ppe"),
    Item("😷", "Mask", "ppe"),
    Item("🥽", "Goggles", "ppe"),
    Item("🥼", "Gown", "ppe"),
    Item("🧢", "Cap", "ppe"),
    Item("💊", "Pills", "meds"),
    Item("🧴", "Syrup", "meds"),
    Item("💧", "Drops", "meds"),
    Item("🩹", "Bandage", "meds"),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SupplyGame(onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var seed by remember { mutableIntStateOf(0) }
    val items = remember(seed) { ITEMS.shuffled() }
    val placed = remember(seed) { mutableStateListOf<Boolean>().apply { repeat(items.size) { add(false) } } }
    var wrong by remember(seed) { mutableIntStateOf(0) }
    var finished by remember(seed) { mutableStateOf(false) }

    var containerOrigin by remember { mutableStateOf(Offset.Zero) }
    val binRects = remember(seed) { mutableStateMapOf<String, Rect>() }
    val itemRects = remember(seed) { mutableStateMapOf<Int, Rect>() }
    var dragItem by remember { mutableStateOf<Int?>(null) }
    var dragPos by remember { mutableStateOf(Offset.Zero) }

    fun rel(r: Rect) = r.translate(-containerOrigin.x, -containerOrigin.y)

    fun placedInBin(binId: String) = items.indices.count { placed[it] && items[it].cat == binId }

    GameScaffold(title = "Supply Sort", onBack = onExit, showBack = false) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Drag each supply into the right bin!",
            color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 16.sp,
        )
        Spacer(Modifier.height(10.dp))

        Box(
            Modifier
                .fillMaxWidth().weight(1f)
                .onGloballyPositioned { containerOrigin = it.boundsInWindow().topLeft }
                .pointerInput(seed) {
                    detectDragGestures(
                        onDragStart = { start ->
                            dragItem = itemRects.entries.firstOrNull { !placed[it.key] && rel(it.value).contains(start) }?.key
                            dragPos = start
                        },
                        onDrag = { change, _ -> dragPos = change.position; change.consume() },
                        onDragEnd = {
                            val it = dragItem
                            if (it != null && !placed[it]) {
                                val bin = binRects.entries.firstOrNull { b -> rel(b.value).contains(dragPos) }?.key
                                if (bin != null) {
                                    if (items[it].cat == bin) {
                                        placed[it] = true
                                        if (placed.all { p -> p } && !finished) {
                                            finished = true
                                            val score = maxOf(0, items.size * 10 - wrong * 5)
                                            val stars = when { wrong == 0 -> 3; wrong <= 3 -> 2; else -> 1 }
                                            onFinish(
                                                GameResult(
                                                    game = Game.SUPPLIES, primary = score.toLong(), secondary = 0, stars = stars,
                                                    statPanels = listOf("SCORE" to "$score", "STARS" to "⭐$stars"),
                                                )
                                            )
                                        }
                                    } else {
                                        wrong++
                                    }
                                }
                            }
                            dragItem = null
                        },
                        onDragCancel = { dragItem = null },
                    )
                },
        ) {
            // Content sits centered in the play area — not crammed to the top or bottom.
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                // bins
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BINS.forEach { bin ->
                        BinBox(bin, placedInBin(bin.id), Modifier.weight(1f)) { r -> binRects[bin.id] = r }
                    }
                }
                Spacer(Modifier.height(28.dp))
                // tray of remaining items
                Text("SUPPLIES", color = Muted, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.forEachIndexed { i, item ->
                        if (!placed[i]) ItemChip(item) { r -> itemRects[i] = r }
                    }
                }
            }
            val d = dragItem
            if (d != null && !placed[d]) {
                val g = items[d]
                Box(
                    Modifier.offset { IntOffset(dragPos.x.roundToInt() - 70, dragPos.y.roundToInt() - 40) }
                        .clip(RoundedCornerShape(12.dp)).background(PaletteYellow.border).padding(horizontal = 12.dp, vertical = 10.dp),
                ) { Text("${g.emoji} ${g.label}", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            }
        }

        Spacer(Modifier.height(10.dp))
        GameControls(onExit = onExit, onRestart = { seed++ })
    }
}

@Composable
private fun BinBox(bin: Bin, count: Int, modifier: Modifier, onBounds: (Rect) -> Unit) {
    Column(
        modifier.height(120.dp)
            .onGloballyPositioned { onBounds(it.boundsInWindow()) }
            .clip(RoundedCornerShape(16.dp))
            .background(bin.palette.bg.copy(alpha = 0.20f))
            .border(3.dp, bin.palette.bg, RoundedCornerShape(16.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(bin.emoji, fontSize = 34.sp)
        Text(bin.label, color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
        Text("$count", color = bin.palette.shadow, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun ItemChip(item: Item, onBounds: (Rect) -> Unit) {
    Row(
        Modifier.onGloballyPositioned { onBounds(it.boundsInWindow()) }
            .clip(RoundedCornerShape(12.dp))
            .background(OffState)
            .border(2.dp, Muted.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(item.emoji, fontSize = 22.sp)
        Text("  ${item.label}", color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
