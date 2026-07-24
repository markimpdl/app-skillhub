package com.osaparecidos.memoriamaker.games.flags

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.Countries
import com.osaparecidos.memoriamaker.data.Country
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.data.GameResult
import com.osaparecidos.memoriamaker.ui.components.ChunkyButton
import com.osaparecidos.memoriamaker.ui.components.GameControls
import com.osaparecidos.memoriamaker.ui.components.GameHud
import com.osaparecidos.memoriamaker.ui.components.GameScaffold
import com.osaparecidos.memoriamaker.ui.theme.BorderCream
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.PaletteBlue
import com.osaparecidos.memoriamaker.ui.theme.PaletteGreen
import com.osaparecidos.memoriamaker.ui.theme.Palette
import com.osaparecidos.memoriamaker.ui.theme.White
import kotlinx.coroutines.delay

private const val TOTAL = 10
private val WrongRed = Palette(
    com.osaparecidos.memoriamaker.ui.theme.AlertRed,
    com.osaparecidos.memoriamaker.ui.theme.AlertRedDark,
    com.osaparecidos.memoriamaker.ui.theme.AlertRedDark,
)

/** Guess the country from its flag: one flag + 4 options, [TOTAL] rounds. Offline database. */
@Composable
fun FlagGame(onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var seed by remember { mutableIntStateOf(0) }
    val questions = remember(seed) { Countries.ALL.shuffled().take(TOTAL) }
    var index by remember(seed) { mutableIntStateOf(0) }
    var correct by remember(seed) { mutableIntStateOf(0) }
    var answered by remember(seed) { mutableStateOf(false) }
    var chosen by remember(seed) { mutableStateOf<Country?>(null) }
    var finished by remember(seed) { mutableStateOf(false) }

    val current = questions[index]
    // 3 wrong + the correct answer, shuffled — stable until the question changes.
    val options = remember(seed, index) {
        (Countries.ALL.filter { it.name != current.name }.shuffled().take(3) + current).shuffled()
    }

    // Advance shortly after an answer so the green/red feedback is visible.
    LaunchedEffect(answered, seed) {
        if (answered && !finished) {
            delay(900)
            if (index + 1 >= TOTAL) {
                finished = true
                val score = correct * 10
                val stars = when { correct >= 9 -> 3; correct >= 6 -> 2; else -> 1 }
                onFinish(
                    GameResult(
                        game = Game.FLAG, primary = score.toLong(), secondary = 0, stars = stars,
                        statPanels = listOf("SCORE" to "$score", "CORRECT" to "$correct/$TOTAL"),
                    )
                )
            } else {
                index++
                answered = false
                chosen = null
            }
        }
    }

    GameScaffold(title = "Flag Game", onBack = onExit, showBack = false) {
        Spacer(Modifier.height(8.dp))
        GameHud(
            primaryLabel = "SCORE",
            primaryValue = "${correct * 10}",
            stats = listOf("FLAG" to "${index + 1}/$TOTAL"),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        // The flag (large — fills the width with a generous height)
        Box(
            Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(current.flag),
                contentDescription = "Flag",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 2f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(3.dp, BorderCream, RoundedCornerShape(14.dp)),
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Which country is this?",
            color = com.osaparecidos.memoriamaker.ui.theme.InkNavy,
            fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 18.sp,
        )
        Spacer(Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { opt ->
                val palette = when {
                    !answered -> PaletteBlue
                    opt.name == current.name -> PaletteGreen        // reveal correct
                    opt.name == chosen?.name -> WrongRed             // your wrong pick
                    else -> PaletteBlue
                }
                ChunkyButton(
                    text = opt.name,
                    onClick = {
                        if (answered) return@ChunkyButton
                        chosen = opt
                        if (opt.name == current.name) correct++
                        answered = true
                    },
                    palette = palette,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 16.sp,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        GameControls(onExit = onExit, onRestart = { seed++ })
    }
}
