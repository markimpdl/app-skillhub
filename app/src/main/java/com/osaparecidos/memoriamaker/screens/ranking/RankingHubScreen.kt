package com.osaparecidos.memoriamaker.screens.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.data.ScoreRepository
import com.osaparecidos.memoriamaker.ui.components.BrandedScaffold
import com.osaparecidos.memoriamaker.ui.components.Chunky3D
import com.osaparecidos.memoriamaker.ui.components.GameIcon
import com.osaparecidos.memoriamaker.ui.components.LabelPill
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.GoldBg
import com.osaparecidos.memoriamaker.ui.theme.GoldBorder
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.Muted
import com.osaparecidos.memoriamaker.ui.theme.White

@Composable
fun RankingHubScreen(
    repo: ScoreRepository,
    onBack: () -> Unit,
    onOverall: () -> Unit,
    onGameRanking: (Game) -> Unit,
) {
    BrandedScaffold(onBack = onBack, title = "Rankings 🏅") {
        Spacer(Modifier.height(8.dp))
        // OVERALL card
        Chunky3D(
            modifier = Modifier.fillMaxWidth(),
            bg = GoldBg,
            borderColor = GoldBorder,
            shadow = GoldBorder.copy(alpha = 0.4f),
            cornerRadius = 22.dp,
            onClick = onOverall,
        ) {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("👑", fontSize = 44.sp)
                Column(Modifier.padding(start = 16.dp)) {
                    Text("OVERALL", color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                    Text("Stars from every game", color = Muted, fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Game.entries.chunked(2).forEach { rowGames ->
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowGames.forEach { game ->
                    GameRankTile(game, repo, Modifier.weight(1f)) { onGameRanking(game) }
                }
                if (rowGames.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun GameRankTile(game: Game, repo: ScoreRepository, modifier: Modifier, onClick: () -> Unit) {
    val best = repo.best(game)
    Chunky3D(
        modifier = modifier,
        bg = White,
        borderColor = game.palette.border,
        shadow = game.palette.shadow.copy(alpha = 0.35f),
        cornerRadius = 20.dp,
        onClick = onClick,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GameIcon(game, discSize = 52.dp, emojiSize = 30.sp)
            LabelPill(game.displayName, bg = game.palette.bg, textColor = White)
            Text(
                if (best != null) "🏆 ${ScoreRepository.formatPrimary(game, best)}" else "🏆 —",
                color = Muted, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            )
        }
    }
}
