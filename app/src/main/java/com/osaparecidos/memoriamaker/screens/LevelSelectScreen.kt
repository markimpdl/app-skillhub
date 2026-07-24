package com.osaparecidos.memoriamaker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.data.LevelInfo
import com.osaparecidos.memoriamaker.data.Levels
import com.osaparecidos.memoriamaker.data.ScoreRepository
import com.osaparecidos.memoriamaker.ui.components.BrandedScaffold
import com.osaparecidos.memoriamaker.ui.components.Chunky3D
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.GoldBorder
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.MedalGold
import com.osaparecidos.memoriamaker.ui.theme.Muted
import com.osaparecidos.memoriamaker.ui.theme.White

@Composable
fun LevelSelectScreen(
    game: Game,
    repo: ScoreRepository,
    onBack: () -> Unit,
    onPlayLevel: (Int) -> Unit,
) {
    val levels = Levels.forGame(game)
    BrandedScaffold(onBack = onBack, title = game.displayName, scroll = false, centerVertically = true) {
        Spacer(Modifier.height(8.dp))
        levels.forEachIndexed { index, info ->
            val stars = repo.getLevelStars(game, index)
            val unlocked = repo.isLevelUnlocked(game, index)
            LevelRow(
                number = index + 1,
                info = info,
                stars = stars,
                unlocked = unlocked,
                onClick = { if (unlocked) onPlayLevel(index) },
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LevelRow(
    number: Int,
    info: LevelInfo,
    stars: Int,
    unlocked: Boolean,
    onClick: () -> Unit,
) {
    val done = stars > 0
    Chunky3D(
        modifier = Modifier.fillMaxWidth().alpha(if (unlocked) 1f else 0.65f),
        bg = White,
        borderColor = if (done) GoldBorder else com.osaparecidos.memoriamaker.ui.theme.BorderCream,
        shadow = InkNavy.copy(alpha = 0.15f),
        cornerRadius = 20.dp,
        enabled = unlocked,
        onClick = onClick,
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(if (done) MedalGold else com.osaparecidos.memoriamaker.ui.theme.OffState),
                contentAlignment = Alignment.Center,
            ) { Text("$number", color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 18.sp) }

            Text(info.emoji, fontSize = 32.sp, modifier = Modifier.padding(horizontal = 12.dp))

            Column(Modifier.weight(1f)) {
                Text(info.name, color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(info.desc, color = Muted, fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 12.sp)
            }

            when {
                done -> Text("⭐".repeat(stars), fontSize = 16.sp)
                unlocked -> Text("▶ PLAY", color = GoldBorder, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                else -> Text("🔒", fontSize = 22.sp)
            }
        }
    }
}
