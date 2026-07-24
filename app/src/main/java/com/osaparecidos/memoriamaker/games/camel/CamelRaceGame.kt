package com.osaparecidos.memoriamaker.games.camel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.osaparecidos.memoriamaker.data.GameResult
import com.osaparecidos.memoriamaker.ui.components.GameControls
import com.osaparecidos.memoriamaker.ui.components.GameScaffold
import com.osaparecidos.memoriamaker.ui.components.WebGameView

// Kiosk tweaks: hide the corner mute button (kiosk rule: no interactive controls near the top) and
// float the BOOST button(s) at mid-screen — on the tall totem the game's bottom-anchored HUD row
// would sit too low for small kids to reach comfortably.
private const val CAMEL_INJECT = """
(function(){
  var st=document.createElement('style');
  st.textContent=
    '#mute-btn{display:none!important;}'+
    '.hud-bottom{position:absolute!important;left:0;right:0;bottom:42%!important;justify-content:flex-end!important;padding-right:18px!important;}'+
    /* the BOOST button sits in a full-width .col that centers it — right-align and drop the keyboard hint */
    '.hud-bottom .col{align-items:flex-end!important;}'+
    '.hud-bottom .hint{display:none!important;}'+
    '.boost-btn{opacity:.6;}'+
    '.hud-bottom-mp{position:absolute!important;left:0;right:0;bottom:42%!important;}';
  document.head.appendChild(st);
})();
"""

/**
 * Saif's Camel Race — a portrait touch racing game (Vite build), run full-size in a WebView.
 * The game brings its own touch controls and menus, so no extra Compose controls are needed.
 */
@Composable
fun CamelRaceGame(onExit: () -> Unit, @Suppress("UNUSED_PARAMETER") onFinish: (GameResult) -> Unit) {
    GameScaffold(title = "Camel Race", onBack = onExit, showBack = false) {
        Box(Modifier.fillMaxWidth().weight(1f).background(Color.Black)) {
            WebGameView(
                assetPath = "camel/index.html",
                modifier = Modifier.fillMaxSize(),
                injectOnLoad = CAMEL_INJECT,
            )
        }
        Spacer(Modifier.height(10.dp))
        GameControls(onExit = onExit)
    }
}
