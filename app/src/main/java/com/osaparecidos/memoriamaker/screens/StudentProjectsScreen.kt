package com.osaparecidos.memoriamaker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.ui.components.BrandBand
import com.osaparecidos.memoriamaker.ui.components.Chunky3D
import com.osaparecidos.memoriamaker.ui.components.GameControls
import com.osaparecidos.memoriamaker.ui.components.GameIcon
import com.osaparecidos.memoriamaker.ui.components.glossy
import com.osaparecidos.memoriamaker.ui.theme.Cream
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.MedalGold
import com.osaparecidos.memoriamaker.ui.theme.Muted
import com.osaparecidos.memoriamaker.ui.theme.White

private data class Project(val student: String, val title: String, val game: Game)

private val PROJECTS = listOf(
    Project("Saeed", "Geometry Rush", Game.GEORUSH),
    Project("Rawdha", "Block World", Game.BLOCKWORLD),
    Project("Saif", "Camel Race", Game.CAMELRACE),
    Project("Mubarak", "Top Car", Game.TOPCAR),
    Project("Mubarak", "Explore the Desert", Game.DESERT),
    Project("Almaha", "Scary World", Game.SCARY),
    Project("Almaha", "Princess Game", Game.PRINCESS),
)

@Composable
fun StudentProjectsScreen(onBack: () -> Unit, onPlay: (Game) -> Unit) {
    Column(Modifier.fillMaxSize().background(Cream)) {
        BrandBand()
        Column(
            Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))
            Text("🎓 STUDENT PROJECTS", color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 24.sp, textAlign = TextAlign.Center)
            Text("〈 Vibe Coding 〉", color = MedalGold, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Games built by our students", color = Muted, fontFamily = Fredoka, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            PROJECTS.forEach { p ->
                ProjectCard(p, onPlay)
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(8.dp))
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).windowInsetsPadding(WindowInsets.navigationBars)) {
            GameControls(onExit = onBack, exitLabel = "← BACK")
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ProjectCard(p: Project, onPlay: (Game) -> Unit) {
    Chunky3D(
        modifier = Modifier.fillMaxWidth(),
        bg = p.game.palette.bg,
        borderColor = p.game.palette.border,
        shadow = p.game.palette.shadow,
        cornerRadius = 22.dp,
        shadowHeight = 6.dp,
        onClick = { onPlay(p.game) },
    ) {
        Row(Modifier.fillMaxWidth().glossy(22.dp).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            GameIcon(p.game, discSize = 56.dp, emojiSize = 32.sp)
            Column(Modifier.padding(start = 16.dp)) {
                Text("by ${p.student}", color = White.copy(alpha = 0.88f), fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Text(p.title, color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 21.sp)
                Text("▶ PLAY", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
