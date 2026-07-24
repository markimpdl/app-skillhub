package com.osaparecidos.memoriamaker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SkillsColorScheme = lightColorScheme(
    primary = Purple,
    onPrimary = White,
    secondary = Orange,
    background = Cream,
    onBackground = InkNavy,
    surface = Cream,
    onSurface = InkNavy,
)

/** App-wide Compose theme. UI is mostly custom; MaterialTheme carries type + a few defaults. */
@Composable
fun SkillsCampTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SkillsColorScheme,
        typography = AppTypography,
        content = content,
    )
}
