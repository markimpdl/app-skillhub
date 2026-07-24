package com.osaparecidos.memoriamaker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.OffState2
import com.osaparecidos.memoriamaker.ui.theme.PalettePurple
import com.osaparecidos.memoriamaker.ui.theme.White

/**
 * Bottom action bar for game screens: EXIT (and optionally RESTART), kept near the bottom of
 * the vertical totem so small kids can reach them without stretching to the top.
 */
@Composable
fun GameControls(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    onRestart: (() -> Unit)? = null,
    exitLabel: String = "← EXIT",
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Chunky3D(
            modifier = Modifier.weight(1f),
            bg = White,
            borderColor = OffState2,
            shadow = InkNavy.copy(alpha = 0.2f),
            cornerRadius = 18.dp,
            onClick = onExit,
        ) {
            Box(Modifier.fillMaxWidth().padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                Text(exitLabel, color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
        if (onRestart != null) {
            Chunky3D(
                modifier = Modifier.weight(1f),
                bg = PalettePurple.bg,
                borderColor = PalettePurple.border,
                shadow = PalettePurple.shadow,
                cornerRadius = 18.dp,
                onClick = onRestart,
            ) {
                Box(Modifier.fillMaxWidth().padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text("↻ RESTART", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}
