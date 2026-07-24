package com.osaparecidos.memoriamaker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.ui.theme.White

/**
 * A game's icon on a soft white disc so it stays legible on any tile colour (e.g. Simon's
 * yellow bolt on a yellow tile). Uses the custom vector when the game has one (3D printer),
 * otherwise the emoji.
 */
@Composable
fun GameIcon(game: Game, discSize: Dp, emojiSize: TextUnit, modifier: Modifier = Modifier) {
    Box(
        modifier.size(discSize).clip(CircleShape).background(White.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center,
    ) {
        val res = game.iconRes
        if (res != null) {
            Image(painterResource(res), contentDescription = game.displayName, modifier = Modifier.size(discSize * 0.66f))
        } else {
            Text(game.emoji, fontSize = emojiSize)
        }
    }
}
