package com.osaparecidos.memoriamaker.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Design tokens from the Skills Camp handoff (high fidelity — colors are final).
 * Grouped by role; per-game palettes carry bg / border / shadow for the chunky 3D look.
 */

// Surfaces
val Cream = Color(0xFFFBF6EC)
val InkNavy = Color(0xFF241E4E)
val InkDark = Color(0xFF161232)
val PanelNavy = Color(0xFF332B66)
val Lavender = Color(0xFFB6AEEA)
val ScreenDark = Color(0xFF1B2A4A)

// Primary purple
val Purple = Color(0xFF6C4DF6)
val PurpleMid = Color(0xFF5A3EE0)
val PurpleDark = Color(0xFF4F33D8)

// Alerts / accents
val AlertRed = Color(0xFFFF5A5A)
val AlertRedDark = Color(0xFFD23B3B)
val Cyan = Color(0xFF5EE0FF)

// Neutrals
val BorderCream = Color(0xFFE8E0CF)
val Muted = Color(0xFF8A8272)
val Muted2 = Color(0xFF7A7466)
val Faint = Color(0xFFB9B1A0)
val OffState = Color(0xFFEFEAE0)
val OffState2 = Color(0xFFD9D2C2)
val OffState3 = Color(0xFFC9C2B2)
val White = Color(0xFFFFFFFF)

// Gold row / medals
val GoldBg = Color(0xFFFFF7DF)
val GoldBorder = Color(0xFFF0B429)
val MedalGold = Color(0xFFFFC93D)
val MedalSilver = Color(0xFFD8D2C4)
val MedalBronze = Color(0xFFE8B08A)
val SilverBorder = Color(0xFFC9C2B2)
val BronzeBorder = Color(0xFFE0A177)
val NeutralMedal = Color(0xFFF1EBDD)

// Player accents
val P1Purple = Color(0xFF6C4DF6)
val P2Orange = Color(0xFFFF8A3D)
val P1Score = Color(0xFF9F8BFF)

/** bg / border / shadow triple for a chunky 3D surface. */
data class Palette(val bg: Color, val border: Color, val shadow: Color)

val PaletteBlue = Palette(Color(0xFF3D7BFF), Color(0xFF2E63DB), Color(0xFF2558D0))
val PaletteOrange = Palette(Color(0xFFFF8A3D), Color(0xFFE87526), Color(0xFFD96A20))
val PalettePink = Palette(Color(0xFFFF6B9A), Color(0xFFEE5588), Color(0xFFE04A7C))
val PaletteTeal = Palette(Color(0xFF12B5A5), Color(0xFF0E9E90), Color(0xFF0A8C80))
val PaletteYellow = Palette(Color(0xFFFFC93D), Color(0xFFEEB424), Color(0xFFE0A400))
val PaletteGreen = Palette(Color(0xFF3FBF63), Color(0xFF33A653), Color(0xFF2C9448))
val PalettePurple = Palette(Purple, PurpleMid, PurpleDark)

// Convenience singles used widely
val Green = PaletteGreen.bg
val GreenDark = PaletteGreen.shadow
val Orange = PaletteOrange.bg
val Teal = PaletteTeal.bg
val Yellow = PaletteYellow.bg
val Pink = PalettePink.bg
val Blue = PaletteBlue.bg
