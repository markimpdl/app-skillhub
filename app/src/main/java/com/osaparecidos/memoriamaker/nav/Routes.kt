package com.osaparecidos.memoriamaker.nav

import com.osaparecidos.memoriamaker.data.Game

/** Central place for nav routes. Builders create concrete routes; the *_PATTERN strings register them. */
object Routes {
    const val HOME = "home"
    const val PROJECTS = "projects"
    const val VICTORY = "victory"
    const val RANKING_HUB = "rankingHub"
    const val OVERALL = "overall"

    const val MODE_PATTERN = "mode/{gameId}"
    const val LEVELS_PATTERN = "levels/{gameId}"
    const val PLAY_PATTERN = "play/{gameId}/{mode}/{level}"
    const val RANKING_PATTERN = "ranking/{gameId}"

    const val ARG_GAME = "gameId"
    const val ARG_MODE = "mode"
    const val ARG_LEVEL = "level"

    fun mode(game: Game) = "mode/${game.id}"
    fun levels(game: Game) = "levels/${game.id}"
    fun play(game: Game, duel: Boolean, level: Int = 0) =
        "play/${game.id}/${if (duel) "duel" else "solo"}/$level"
    fun ranking(game: Game) = "ranking/${game.id}"
}
