package com.osaparecidos.memoriamaker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkDark
import com.osaparecidos.memoriamaker.ui.theme.White

/**
 * A dimming "TAP TO START" scrim shown over a game board until the player begins.
 * Place inside the board's Box (it fills it); tapping anywhere calls [onStart].
 */
@Composable
fun BoxScope.StartOverlay(onStart: () -> Unit) {
    Box(
        Modifier
            .matchParentSize()
            .clip(RoundedCornerShape(18.dp))
            .background(InkDark.copy(alpha = 0.6f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onStart),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("👆", fontSize = 52.sp)
            Text("TAP TO START", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
    }
}
