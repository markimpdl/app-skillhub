package com.osaparecidos.memoriamaker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.Muted
import com.osaparecidos.memoriamaker.ui.theme.White

/** A rounded stat pill: small uppercase label above a bold value. Used across game HUDs. */
@Composable
fun StatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    bg: Color = White,
    labelColor: Color = Muted,
    valueColor: Color = InkNavy,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label.uppercase(),
            color = labelColor,
            fontFamily = Fredoka,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
        )
        Text(
            text = value,
            color = valueColor,
            fontFamily = Fredoka,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )
    }
}

/** A small uppercase text pill (label chip) — e.g. white label on a colored tile. */
@Composable
fun LabelPill(
    text: String,
    modifier: Modifier = Modifier,
    bg: Color = White.copy(alpha = 0.92f),
    textColor: Color = InkNavy,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
) {
    Text(
        text = text.uppercase(),
        color = textColor,
        fontFamily = Fredoka,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize,
        letterSpacing = 0.5.sp,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    )
}

/** A horizontal navy info pill (icon/text). */
@Composable
fun NavyPill(
    modifier: Modifier = Modifier,
    bg: Color = InkNavy,
    content: @Composable () -> Unit,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}
