package com.osaparecidos.memoriamaker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkDark
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.MedalGold
import com.osaparecidos.memoriamaker.ui.theme.P1Score
import com.osaparecidos.memoriamaker.ui.theme.P2Orange
import com.osaparecidos.memoriamaker.ui.theme.White

/**
 * Two-player split for the vertical totem: both halves upright (no 180° rotation).
 * P2 on top, P1 on bottom, with a central navy VS bar carrying both scores, a gold VS pill,
 * the shared timer/round, and RESTART.
 */
@Composable
fun DuelScaffold(
    p1Score: String,
    p2Score: String,
    centerRight: String,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    p2Half: @Composable ColumnScope.() -> Unit,
    p1Half: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(InkDark)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            Modifier.fillMaxWidth().weight(1f).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { p2Half() }

        VsBar(p1Score, p2Score, centerRight, onRestart, onExit)

        Column(
            Modifier.fillMaxWidth().weight(1f).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { p1Half() }
    }
}

@Composable
private fun VsBar(p1Score: String, p2Score: String, centerRight: String, onRestart: () -> Unit, onExit: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(InkNavy)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Chunky3D(
            modifier = Modifier.size(44.dp),
            bg = com.osaparecidos.memoriamaker.ui.theme.AlertRed,
            borderColor = com.osaparecidos.memoriamaker.ui.theme.AlertRedDark,
            shadow = com.osaparecidos.memoriamaker.ui.theme.AlertRedDark,
            cornerRadius = 12.dp,
            shadowHeight = 3.dp,
            onClick = onExit,
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("✕", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
        ScoreText(p2Score, P2Orange)
        Box(
            Modifier.clip(RoundedCornerShape(10.dp)).background(MedalGold).padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text("VS", color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        ScoreText(p1Score, P1Score)
        Box(Modifier.weight(1f))
        Text(centerRight, color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Chunky3D(
            modifier = Modifier.size(44.dp),
            bg = com.osaparecidos.memoriamaker.ui.theme.Purple,
            borderColor = com.osaparecidos.memoriamaker.ui.theme.PurpleMid,
            shadow = com.osaparecidos.memoriamaker.ui.theme.PurpleDark,
            cornerRadius = 12.dp,
            shadowHeight = 3.dp,
            onClick = onRestart,
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("↻", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun ScoreText(value: String, color: androidx.compose.ui.graphics.Color) {
    Text(value, color = color, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 22.sp)
}
