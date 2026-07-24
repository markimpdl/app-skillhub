package com.osaparecidos.memoriamaker.screens.ranking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.data.ScoreRepository
import com.osaparecidos.memoriamaker.ui.components.BrandedScaffold
import com.osaparecidos.memoriamaker.ui.components.ChunkyButton
import com.osaparecidos.memoriamaker.ui.components.LabelPill
import com.osaparecidos.memoriamaker.ui.components.RankRow
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.Muted
import com.osaparecidos.memoriamaker.ui.theme.OffState
import com.osaparecidos.memoriamaker.ui.theme.PaletteGreen
import com.osaparecidos.memoriamaker.ui.theme.White

@Composable
fun GameRankingScreen(
    initialGame: Game,
    repo: ScoreRepository,
    onBack: () -> Unit,
    onPlay: (Game) -> Unit,
) {
    var selected by remember { mutableStateOf(initialGame) }
    BrandedScaffold(onBack = onBack, title = "Hall of Fame") {
        Spacer(Modifier.height(8.dp))
        // Chip row to switch game.
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Game.entries.forEach { game ->
                val active = game == selected
                LabelPill(
                    text = "${game.emoji} ${game.displayName}",
                    bg = if (active) InkNavy else OffState,
                    textColor = if (active) White else Muted,
                    modifier = Modifier.clickable(
                        interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource(),
                        indication = null,
                    ) { selected = game },
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        val scores = repo.getScores(selected)
        if (scores.isEmpty()) {
            Text("No records yet.\nBe the first in the Hall of Fame!", color = Muted, fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 16.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                scores.forEachIndexed { index, entry ->
                    RankRow(
                        rank = index,
                        name = entry.name,
                        detail = ScoreRepository.formatSecondary(selected, entry),
                        value = ScoreRepository.formatPrimary(selected, entry),
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        ChunkyButton(text = "▶ PLAY AGAIN", onClick = { onPlay(selected) }, palette = PaletteGreen, modifier = Modifier.fillMaxWidth(), fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
    }
}
