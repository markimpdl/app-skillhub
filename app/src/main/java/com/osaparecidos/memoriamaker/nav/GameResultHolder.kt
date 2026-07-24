package com.osaparecidos.memoriamaker.nav

import androidx.lifecycle.ViewModel
import com.osaparecidos.memoriamaker.data.GameResult

/**
 * Activity-scoped holder that carries the last [GameResult] from a game screen to the Victory
 * screen without serializing rich objects into nav arguments.
 */
class GameResultHolder : ViewModel() {
    var result: GameResult? = null

    /** The route of the last game played, so Victory's "Play Again" can relaunch it. */
    var lastPlayRoute: String? = null
}
