package com.osaparecidos.memoriamaker.data

/**
 * Converts a raw performance into 0–3 ⭐ per play. Thresholds are per game.
 * Wire / If...Then compute stars themselves (first-try correctness), so those pass
 * their star count straight through as [primary].
 */
object Stars {

    /** Stars for a completed play, given the game's raw [primary]/[secondary] metric values. */
    fun forResult(game: Game, primary: Long, secondary: Long): Int = when (game.metric) {
        Metric.TIME_MOVES -> forTime(primary)
        Metric.SCORE -> forScore(primary)
        Metric.ROUND -> forRound(primary)
        Metric.STARS_TIME -> primary.toInt().coerceIn(0, 3)
    }

    /** Memory: faster clear = more stars. */
    fun forTime(timeMs: Long): Int = when {
        timeMs <= 25_000 -> 3
        timeMs <= 45_000 -> 2
        else -> 1
    }

    /** Whack-a-Bug / Print Rush: higher score = more stars. */
    fun forScore(score: Long): Int = when {
        score >= 150 -> 3
        score >= 90 -> 2
        else -> 1
    }

    /** Simon: longer sequence = more stars. */
    fun forRound(round: Long): Int = when {
        round >= 8 -> 3
        round >= 5 -> 2
        else -> 1
    }

    /** Wire / If...Then: wrong drops before the correct rule reduce stars. */
    fun forMistakes(wrongTries: Int): Int = when (wrongTries) {
        0 -> 3
        1 -> 2
        else -> 1
    }
}
