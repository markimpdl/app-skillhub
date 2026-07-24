package com.osaparecidos.memoriamaker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.Palette
import com.osaparecidos.memoriamaker.ui.theme.PalettePurple
import com.osaparecidos.memoriamaker.ui.theme.White
import androidx.compose.material3.Text

/** Primary chunky action button (3D press + spring). */
@Composable
fun ChunkyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    palette: Palette = PalettePurple,
    textColor: Color = White,
    fontSize: TextUnit = 18.sp,
    cornerRadius: Dp = 18.dp,
    shadowHeight: Dp = 5.dp,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
) {
    Chunky3D(
        modifier = modifier,
        bg = palette.bg,
        borderColor = palette.border,
        shadow = palette.shadow,
        cornerRadius = cornerRadius,
        shadowHeight = shadowHeight,
        onClick = onClick,
        enabled = enabled,
    ) {
        Box(Modifier.padding(contentPadding), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = textColor,
                fontFamily = Fredoka,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
            )
        }
    }
}
