package com.osaparecidos.memoriamaker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.ui.theme.Cream
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkDark
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.White

/** Round chunky back button (←). */
@Composable
fun BackButton(onBack: () -> Unit, dark: Boolean = false) {
    Chunky3D(
        modifier = Modifier.size(52.dp),
        bg = if (dark) InkNavy else White,
        borderColor = if (dark) InkDark else Color(0xFFE8E0CF),
        shadow = if (dark) InkDark else Color(0x22000000),
        cornerRadius = 16.dp,
        onClick = onBack,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("←", color = if (dark) White else InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
    }
}

/**
 * Standard screen frame: full-bleed background (cream, or dark navy for dark scenes),
 * a header row with a back button + title, then [content] below. Applies safe-drawing insets.
 */
@Composable
fun GameScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    showBack: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier.fillMaxSize().background(if (dark) InkDark else Cream),
    ) {
        // Skills Camp brand band on top of every game screen (compact to spare the board).
        BrandBand(compact = true)
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    color = if (dark) White else InkNavy,
                    fontFamily = Fredoka,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
            }
            content()
        }
    }
}
