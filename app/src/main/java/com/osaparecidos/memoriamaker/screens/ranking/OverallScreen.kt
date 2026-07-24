package com.osaparecidos.memoriamaker.screens.ranking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.ScoreRepository
import com.osaparecidos.memoriamaker.ui.components.BrandedScaffold
import com.osaparecidos.memoriamaker.ui.components.RankRow
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.Muted

@Composable
fun OverallScreen(
    repo: ScoreRepository,
    onBack: () -> Unit,
) {
    BrandedScaffold(onBack = onBack, title = "Overall Champions 👑") {
        Spacer(Modifier.height(8.dp))
        val champions = repo.getOverall()
        if (champions.isEmpty()) {
            Text("No champions yet.\nPlay any game to earn stars!", color = Muted, fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 16.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                champions.forEachIndexed { index, c ->
                    RankRow(
                        rank = index,
                        name = c.name,
                        detail = "${c.gamesPlayed} games played",
                        value = "⭐${c.stars}",
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Earn ⭐ by playing any game — 0 to 3 stars per round.",
            color = Muted, fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 13.sp,
        )
    }
}
