package com.osaparecidos.memoriamaker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.GameResult
import com.osaparecidos.memoriamaker.data.ScoreRepository
import com.osaparecidos.memoriamaker.data.Stars
import com.osaparecidos.memoriamaker.ui.components.ChunkyButton
import com.osaparecidos.memoriamaker.ui.components.ConfettiOverlay
import com.osaparecidos.memoriamaker.ui.components.NameEntryField
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.Lavender
import com.osaparecidos.memoriamaker.ui.theme.MedalGold
import com.osaparecidos.memoriamaker.ui.theme.InkDark
import com.osaparecidos.memoriamaker.ui.theme.Palette
import com.osaparecidos.memoriamaker.ui.theme.PaletteGreen
import com.osaparecidos.memoriamaker.ui.theme.PalettePurple
import com.osaparecidos.memoriamaker.ui.theme.PanelNavy
import com.osaparecidos.memoriamaker.ui.theme.White

@Composable
fun VictoryScreen(
    result: GameResult,
    repo: ScoreRepository,
    onGoRanking: () -> Unit,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
) {
    val isRecord = remember(result) {
        !result.isDuel && repo.isRecord(result.game, result.primary, result.secondary)
    }
    val qualifies = remember(result) {
        !result.isDuel && repo.qualifies(result.game, result.primary, result.secondary)
    }
    var name by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(InkNavy)) {
        if (!result.gameOver) ConfettiOverlay()
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))
            Text(if (result.gameOver) "💥" else "🏆", fontSize = 76.sp)

            val title = when {
                result.isDuel -> winnerTitle(result.duelWinner)
                result.gameOver -> "GAME OVER"
                else -> "YOU DID IT!"
            }
            Text(title, color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 40.sp, textAlign = TextAlign.Center)

            if (!result.isDuel) {
                Text(
                    "${"⭐".repeat(result.stars.coerceIn(0, 3))}  ${result.game.displayName}",
                    color = Lavender, fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 16.sp,
                )
            }

            if (isRecord) {
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier.clip(RoundedCornerShape(14.dp)).background(MedalGold).padding(horizontal = 16.dp, vertical = 8.dp),
                ) { Text("⭐ NEW RECORD!", color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            }

            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                result.statPanels.take(2).forEach { (label, value) ->
                    StatPanel(label, value, Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(28.dp))

            if (!result.isDuel && qualifies) {
                Text("WRITE YOUR NAME TO SAVE YOUR SCORE", color = Lavender, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(12.dp))
                NameEntryField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                ChunkyButton(
                    text = "SAVE MY SCORE ⭐",
                    onClick = {
                        val trimmed = name.trim()
                        if (trimmed.isNotEmpty()) {
                            repo.addScore(result.game, trimmed, result.primary, result.secondary, result.stars)
                            onGoRanking()
                        }
                    },
                    palette = PaletteGreen,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 20.sp,
                    enabled = name.trim().isNotEmpty(),
                )
            } else {
                ChunkyButton(text = "▶ PLAY AGAIN", onClick = onPlayAgain, palette = PaletteGreen, modifier = Modifier.fillMaxWidth(), fontSize = 20.sp)
                Spacer(Modifier.height(12.dp))
                ChunkyButton(text = "🏅 RANKING", onClick = onGoRanking, palette = PalettePurple, modifier = Modifier.fillMaxWidth(), fontSize = 20.sp)
            }

            Spacer(Modifier.height(12.dp))
            ChunkyButton(
                text = "🏠 HOME",
                onClick = onHome,
                palette = Palette(PanelNavy, InkDark, InkDark),
                textColor = White,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 20.sp,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun winnerTitle(winner: String?): String = when (winner) {
    "TIE" -> "IT'S A TIE!"
    "P1" -> "PLAYER 1 WINS!"
    "P2" -> "PLAYER 2 WINS!"
    else -> "YOU DID IT!"
}

@Composable
private fun StatPanel(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(PanelNavy).padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 30.sp)
        Text(label.uppercase(), color = Lavender, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.sp)
    }
}
