package com.osaparecidos.memoriamaker.games.princess

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.GameResult
import com.osaparecidos.memoriamaker.ui.components.GameControls
import com.osaparecidos.memoriamaker.ui.components.GameScaffold
import com.osaparecidos.memoriamaker.ui.components.WebGameView
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy

// Kiosk tweaks: the game's own touch buttons live inside #game-screen so they already show only
// during the playable phases (never over the title/intro/end screens) — keep them, just force the
// `touch` class (WebView detection can miss), make them translucent, and shrink the title/end
// boxes + keyboard hint so they fit the small landscape band.
private const val PRINCESS_INJECT = """
(function(){
  document.body.classList.add('touch');
  var st=document.createElement('style');
  st.textContent=
    '.hint{display:none!important;}'+
    '.title-box{padding:1rem 1.8rem;border-width:4px;}'+
    '.title-box h1{font-size:clamp(1.6rem,6vw,2.6rem);}'+
    '.subtitle{margin:.5rem 0 1rem;font-size:clamp(.85rem,2.6vw,1.1rem);}'+
    '.big-btn{font-size:clamp(1rem,3.5vw,1.4rem);}'+
    '#touch-controls{opacity:.6;padding:0 3vw 3vw;}';
  document.head.appendChild(st);
})();
"""

/**
 * Almaha's Princess Game (Peach's Great Escape) — a 3D/2D story adventure: Three.js cutscenes plus
 * two playable 2D side-scroller phases, run in a WebView inside a centered landscape band (like
 * Scary World). The game's built-in ◀ ▶ ⬆ touch buttons drive both playable phases.
 */
@Composable
fun PrincessGame(onExit: () -> Unit, @Suppress("UNUSED_PARAMETER") onFinish: (GameResult) -> Unit) {
    GameScaffold(title = "Princess Game", onBack = onExit, showBack = false) {
        Spacer(Modifier.height(8.dp))
        Text("Escape the monster · call Mario · beat the boss! 👑", color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).background(Color.Black)) {
            val bandH = (maxWidth * 0.62f).coerceAtMost(maxHeight * 0.62f)
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(Modifier.fillMaxWidth().height(bandH)) {
                    WebGameView(
                        assetPath = "princess/index.html",
                        modifier = Modifier.fillMaxSize(),
                        injectOnLoad = PRINCESS_INJECT,
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        GameControls(onExit = onExit)
    }
}
