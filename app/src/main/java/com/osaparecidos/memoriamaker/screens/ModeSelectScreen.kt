package com.osaparecidos.memoriamaker.screens

import androidx.compose.foundation.background
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
import com.osaparecidos.memoriamaker.ui.theme.Cream
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.MedalGold
import com.osaparecidos.memoriamaker.ui.theme.Orange
import com.osaparecidos.memoriamaker.ui.theme.PalettePurple
import com.osaparecidos.memoriamaker.ui.theme.PaletteTeal
import com.osaparecidos.memoriamaker.ui.theme.Teal
import com.osaparecidos.memoriamaker.ui.theme.White

@Composable
fun ModeSelectScreen(
    game: Game,
    onBack: () -> Unit,
    onSolo: () -> Unit,
    onDuel: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Cream)) {
        // Keep the ACTVET · Skills Camp branding on top (no tagline here).
        BrandBand()

        Column(
            Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                "${game.displayName} — How do you want to play?",
                color = InkNavy,
                fontFamily = Fredoka,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))

            // SOLO card
            Chunky3D(
                modifier = Modifier.fillMaxWidth(),
                bg = PalettePurple.bg,
                borderColor = PalettePurple.border,
                shadow = PalettePurple.shadow,
                cornerRadius = 24.dp,
                shadowHeight = 6.dp,
                onClick = onSolo,
            ) {
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🧠", fontSize = 48.sp)
                    Column(Modifier.padding(start = 16.dp)) {
                        Text("SOLO", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                        Text("Play alone · beat the clock", color = White.copy(alpha = 0.9f), fontFamily = Fredoka, fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // DUEL card (disabled when the game has no duel mode)
            val duelEnabled = game.hasDuel
            Chunky3D(
                modifier = Modifier.fillMaxWidth(),
                bg = Teal,
                borderColor = PaletteTeal.border,
                shadow = PaletteTeal.shadow,
                cornerRadius = 24.dp,
                shadowHeight = 6.dp,
                enabled = duelEnabled,
                onClick = onDuel,
            ) {
                Box(Modifier.fillMaxWidth()) {
                    Box(Modifier.fillMaxWidth(0.5f).height(112.dp).background(Orange))
                    Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🦊", fontSize = 40.sp)
                        Box(
                            Modifier.padding(horizontal = 12.dp).background(MedalGold, androidx.compose.foundation.shape.RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
                        ) { Text("VS", color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                        Text("🐸", fontSize = 40.sp)
                        Column(Modifier.padding(start = 16.dp)) {
                            Text("DUEL", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                            Text(
                                if (duelEnabled) "2 players · split screen ⚔️" else "Coming soon",
                                color = White.copy(alpha = 0.9f),
                                fontFamily = Fredoka,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).windowInsetsPadding(WindowInsets.navigationBars)) {
            GameControls(onExit = onBack, exitLabel = "← BACK")
            Spacer(Modifier.height(12.dp))
        }
    }
}
