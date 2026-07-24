package com.osaparecidos.memoriamaker.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.R
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.data.OverallEntry
import com.osaparecidos.memoriamaker.ui.components.Chunky3D
import com.osaparecidos.memoriamaker.ui.components.GameIcon
import com.osaparecidos.memoriamaker.ui.components.LabelPill
import com.osaparecidos.memoriamaker.ui.components.glossy
import com.osaparecidos.memoriamaker.ui.theme.Cream
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkDark
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.Lavender
import com.osaparecidos.memoriamaker.ui.theme.MedalGold
import com.osaparecidos.memoriamaker.ui.theme.Purple
import com.osaparecidos.memoriamaker.ui.theme.White

@Composable
fun HomeScreen(
    champions: List<OverallEntry>,
    onPlay: (Game) -> Unit,
    onRanking: () -> Unit,
    onOpenProjects: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState()),
    ) {
        // Navy logo band (full-bleed, rounded bottom).
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(InkNavy)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.logo_actvet),
                contentDescription = "ACTVET",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(0.65f),
            )
            Spacer(Modifier.height(12.dp))
            Row {
                Text("SKILLS ", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 40.sp)
                Text("CAMP", color = Purple, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 40.sp)
            }
            Text(
                "Pick a game and play! 🎮",
                color = Lavender,
                fontFamily = Fredoka,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
            )
        }

        Column(Modifier.padding(horizontal = 16.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Tappable "Student Projects" banner → opens a dedicated screen with the student games.
            StudentProjectsBanner(onOpenProjects)

            // Games in a 2-column grid. Student-project games live on their own screen, not here.
            // The Skills Quiz spans the full row on its own.
            val cols = 2
            val gridGames = Game.entries.filter { it !in Game.STUDENT && it != Game.QUIZ }
            gridGames.chunked(cols).forEach { rowGames ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowGames.forEach { game -> GameTile(game, Modifier.weight(1f), onPlay) }
                    repeat(cols - rowGames.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            GameTile(Game.QUIZ, Modifier.fillMaxWidth(), onPlay)

            Spacer(Modifier.height(4.dp))
            RankingBar(champions = champions, onClick = onRanking)
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars).height(8.dp))
        }
    }
}

/** Tappable banner → opens the Student Projects (Vibe Coding) screen. */
@Composable
private fun StudentProjectsBanner(onOpen: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(InkNavy)
            .clickable(onClick = onOpen)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🎓", fontSize = 32.sp)
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text("SKILLS CAMP · STUDENT PROJECTS", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("〈 Vibe Coding 〉  games built by students", color = MedalGold, fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        }
        Text("›", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 28.sp)
    }
}

@Composable
private fun GameTile(game: Game, modifier: Modifier, onPlay: (Game) -> Unit) {
    Chunky3D(
        modifier = modifier,
        bg = game.palette.bg,
        borderColor = game.palette.border,
        shadow = game.palette.shadow,
        cornerRadius = 22.dp,
        shadowHeight = 6.dp,
        onClick = { onPlay(game) },
    ) {
        Column(
            Modifier.fillMaxWidth().glossy(22.dp).padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GameIcon(game, discSize = 58.dp, emojiSize = 34.sp)
            Text(
                game.displayName,
                color = InkNavy,
                fontFamily = Fredoka,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                lineHeight = 15.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun RankingBar(champions: List<OverallEntry>, onClick: () -> Unit) {
    val preview = if (champions.isEmpty()) {
        "Be the first champion!"
    } else {
        champions.take(3).mapIndexed { i, c ->
            val crown = if (i == 0) "👑 " else ""
            "$crown${c.name} ⭐${c.stars}"
        }.joinToString(" · ")
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(InkNavy)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(MedalGold),
            contentAlignment = Alignment.Center,
        ) { Text("🏅", fontSize = 22.sp) }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text("RANKING · TOP CHAMPIONS", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(preview, color = Lavender, fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 12.sp, maxLines = 1)
        }
        Text("›", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 24.sp)
    }
}
