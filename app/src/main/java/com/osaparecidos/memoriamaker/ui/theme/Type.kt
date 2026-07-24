package com.osaparecidos.memoriamaker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.R

/** Fredoka (Google Fonts, OFL) — bundled under res/font. */
val Fredoka = FontFamily(
    Font(R.font.fredoka_400, FontWeight.Normal),
    Font(R.font.fredoka_500, FontWeight.Medium),
    Font(R.font.fredoka_600, FontWeight.SemiBold),
    Font(R.font.fredoka_700, FontWeight.Bold),
)

/**
 * Base typography — everything Fredoka. Titles 30–58, stat values 24–32 (w700),
 * buttons 15–28 (w700), labels 10–15 (w600 uppercase, letter-spacing ~1px).
 */
val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 46.sp),
    headlineLarge = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 30.sp),
    titleLarge = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    bodyLarge = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
)
