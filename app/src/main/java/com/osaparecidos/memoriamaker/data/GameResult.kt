package com.osaparecidos.memoriamaker.data

/**
 * The outcome of a single play, handed from a game screen to the Victory screen.
 *
 * Solo: [duelWinner] is null → Victory shows stat panels + name entry (saved via [ScoreRepository]).
 * Duel: [duelWinner] is "P1" / "P2" / "TIE" → Victory shows the winner, no ranking entry.
 *
 * [primary]/[secondary]/[stars] carry the values persisted for solo plays (see [Metric]).
 * [statPanels] are the label→value pairs shown on the Victory panels for this game.
 */
data class GameResult(
    val game: Game,
    val primary: Long,
    val secondary: Long,
    val stars: Int,
    val statPanels: List<Pair<String, String>>,
    val duelWinner: String? = null,
    /** True when the play ended on a mistake (e.g. Simon) — Victory shows GAME OVER, no confetti. */
    val gameOver: Boolean = false,
) {
    val isDuel: Boolean get() = duelWinner != null
}
