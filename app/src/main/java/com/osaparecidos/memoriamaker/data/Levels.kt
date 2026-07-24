package com.osaparecidos.memoriamaker.data

/** A level / mission entry for the Wire and If...Then selection screens. */
data class LevelInfo(val emoji: String, val name: String, val desc: String)

/** Static level metadata (5 each). Order defines unlock progression. */
object Levels {
    val WIRE = listOf(
        LevelInfo("🛋️", "Living Room", "Route 2 wires through the plan"),
        LevelInfo("🏠", "Whole House", "Light every room"),
        LevelInfo("🍳", "Kitchen Power", "Plug in the appliances"),
        LevelInfo("🌩️", "Blackout!", "Flip breakers before time runs out"),
    )

    val IFTHEN = listOf(
        LevelInfo("🌙", "Night Light", "IF night time → porch light on"),
        LevelInfo("🌧️", "Rain Alert", "IF rain starts → close windows"),
        LevelInfo("🏡", "You Reached Home", "Open door → lights on → AC on"),
        LevelInfo("🏃", "Nobody Home", "Lock doors + turn AC off"),
        LevelInfo("☀️", "Good Morning", "Open blinds + start coffee"),
        LevelInfo("🔥", "Kitchen Safety", "Smoke → fan on → gas off → alarm"),
    )

    fun forGame(game: Game): List<LevelInfo> = when (game) {
        Game.WIRE -> WIRE
        Game.IFTHEN -> IFTHEN
        else -> emptyList()
    }
}
