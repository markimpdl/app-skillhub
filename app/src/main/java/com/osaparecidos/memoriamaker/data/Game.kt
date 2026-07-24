package com.osaparecidos.memoriamaker.data

import com.osaparecidos.memoriamaker.R
import com.osaparecidos.memoriamaker.ui.theme.Palette
import com.osaparecidos.memoriamaker.ui.theme.PaletteBlue
import com.osaparecidos.memoriamaker.ui.theme.PaletteGreen
import com.osaparecidos.memoriamaker.ui.theme.PaletteOrange
import com.osaparecidos.memoriamaker.ui.theme.PalettePink
import com.osaparecidos.memoriamaker.ui.theme.PalettePurple
import com.osaparecidos.memoriamaker.ui.theme.PaletteTeal
import com.osaparecidos.memoriamaker.ui.theme.PaletteYellow

/** How a game's score is interpreted, sorted and displayed. */
enum class Metric {
    /** Memory: primary = time (ms, lower better), secondary = moves (lower better). */
    TIME_MOVES,

    /** Whack-a-Bug, Print Rush: primary = score (higher better). */
    SCORE,

    /** Simon: primary = longest round reached (higher better). */
    ROUND,

    /** Wire, If...Then: primary = stars (higher better), secondary = time (ms, lower better). */
    STARS_TIME,
}

/**
 * The six mini-games. Metadata drives Home tiles, Mode/Level select, HUDs and rankings.
 * [id] is the stable storage key; changing it orphans saved scores.
 */
enum class Game(
    val id: String,
    val displayName: String,
    val emoji: String,
    val palette: Palette,
    val metric: Metric,
    val hasDuel: Boolean,
    val levelCount: Int = 0,
    val iconRes: Int? = null,
) {
    MEMORY("memory", "MEMORY", "🃏", PaletteBlue, Metric.TIME_MOVES, hasDuel = true),
    WHACK("whack", "WHACK-A-BUG", "🐛", PaletteGreen, Metric.SCORE, hasDuel = true),
    SIMON("simon", "SIMON", "⚡", PaletteYellow, Metric.ROUND, hasDuel = true),
    PRINT("print", "3D PRINT RUSH", "🖨️", PalettePink, Metric.SCORE, hasDuel = true, iconRes = R.drawable.ic_printer),
    WIRE("wire", "WIRE THE HOUSE", "🔌", PaletteTeal, Metric.STARS_TIME, hasDuel = false, levelCount = 4),
    IFTHEN("ifthen", "HOME AUTOMATION", "🤖", PaletteOrange, Metric.STARS_TIME, hasDuel = false, levelCount = 6),
    FLAG("flag", "FLAG GAME", "🌍", PalettePurple, Metric.SCORE, hasDuel = false),
    FILLUP("fillup", "TOP UP FLUIDS", "🚗", PaletteBlue, Metric.SCORE, hasDuel = false),
    CNC("cnc", "CNC PROGRAM", "⚙️", PaletteTeal, Metric.SCORE, hasDuel = false),
    SUPPLIES("supplies", "SUPPLY SORT", "🩺", PalettePink, Metric.SCORE, hasDuel = false),
    JIGSAW("jigsaw", "DESIGN PUZZLE", "🧩", PalettePink, Metric.SCORE, hasDuel = false),
    GEORUSH("georush", "GEOMETRY RUSH", "🔺", PalettePurple, Metric.SCORE, hasDuel = false),
    MAZE("maze", "LOGIC GAME", "🧭", PaletteTeal, Metric.SCORE, hasDuel = false),
    QUIZ("quiz", "SKILLS QUIZ", "❓", PaletteYellow, Metric.SCORE, hasDuel = false),
    BLOCKWORLD("blockworld", "BLOCK WORLD", "🧱", PaletteOrange, Metric.SCORE, hasDuel = false),
    CAMELRACE("camelrace", "CAMEL RACE", "🐪", PaletteYellow, Metric.SCORE, hasDuel = false),
    TOPCAR("topcar", "TOP CAR", "🏎️", PaletteBlue, Metric.SCORE, hasDuel = false),
    DESERT("desert", "EXPLORE THE DESERT", "🌵", PaletteGreen, Metric.SCORE, hasDuel = false),
    SCARY("scary", "SCARY WORLD", "👻", PalettePink, Metric.SCORE, hasDuel = false),
    PRINCESS("princess", "PRINCESS GAME", "👑", PalettePink, Metric.SCORE, hasDuel = false);

    val hasLevels: Boolean get() = levelCount > 0

    companion object {
        fun fromId(id: String): Game = entries.first { it.id == id }

        /** Student "Vibe Coding" games: listed on StudentProjectsScreen, hidden from the home grid. */
        val STUDENT = setOf(GEORUSH, BLOCKWORLD, CAMELRACE, TOPCAR, DESERT, SCARY, PRINCESS)
    }
}
