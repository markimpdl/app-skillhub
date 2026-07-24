package com.osaparecidos.memoriamaker.games.georush

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.GameResult
import com.osaparecidos.memoriamaker.ui.components.GameControls
import com.osaparecidos.memoriamaker.ui.components.GameScaffold
import com.osaparecidos.memoriamaker.ui.components.WebGameView
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.PaletteYellow
import com.osaparecidos.memoriamaker.ui.theme.White

// Kiosk tweaks injected after the page loads: keep only 3 difficulties (Easy/Hard/Insane), drop the
// locked demon / "nothing" / custom-editor cards, and lay the menu out for the wide band.
// Clean, kid-friendly menu: 3 levels only, no shop / character picker / version / editor, and the
// level cards shrunk so all three fit in the vertical panel's band.
private const val GEO_INJECT = """
(function(){
  function rm(s){document.querySelectorAll(s).forEach(function(e){e.remove();});}
  rm('[data-level=demon]'); rm('[data-level=nothing]'); rm('[data-level=custom]');
  rm('#versionTag'); rm('.wallet'); rm('.skins-wrap'); rm('#transferBtn'); rm('#creatorBtn'); rm('.hint');
  var st=document.createElement('style');
  st.textContent=
    '#startScreen h1{font-size:1.4rem;margin:0;}'+
    '#startScreen p{font-size:.78rem;margin:2px 0 6px;}'+
    '#startScreen .levels{display:flex;flex-direction:row;flex-wrap:nowrap;gap:8px;justify-content:center;align-items:stretch;}'+
    '#startScreen .level-card{padding:8px 6px;min-width:0;width:30%;}'+
    '#startScreen .lv-icon{font-size:1.7rem;}'+
    '#startScreen .lv-name{font-size:.7rem;line-height:1.1;}'+
    '#startScreen .lv-diff{font-size:.58rem;padding:2px 5px;}'+
    '#startScreen .lv-best{font-size:.6rem;}'+
    '#startScreen{padding:8px;justify-content:center;gap:4px;}';
  document.head.appendChild(st);
})();
"""

/**
 * Saeed's Geometry Rush — the student's real HTML/JS game, run in a WebView inside a centered
 * horizontal band (the game is landscape; the kiosk panel is vertical). A floating PUSH button
 * jumps the cube (tap) and flies the ship (hold).
 */
@Composable
fun GeoRushGame(onExit: () -> Unit, @Suppress("UNUSED_PARAMETER") onFinish: (GameResult) -> Unit) {
    var web by remember { mutableStateOf<WebView?>(null) }

    GameScaffold(title = "Geometry Rush", onBack = onExit, showBack = false) {
        Spacer(Modifier.height(8.dp))
        Text("Tap a level · PUSH to jump · hold PUSH to fly 🚀", color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        BoxWithConstraints(
            Modifier.fillMaxWidth().weight(1f).background(Color.Black),
        ) {
            // Band + PUSH are centered as a group, so the button sits just below the play area.
            val bandH = (maxWidth * 0.62f).coerceAtMost(maxHeight * 0.6f)
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(Modifier.fillMaxWidth().height(bandH)) {
                    WebGameView(
                        assetPath = "geometry/index.html",
                        modifier = Modifier.fillMaxSize(),
                        injectOnLoad = GEO_INJECT,
                        onCreated = { web = it },
                    )
                }
                Spacer(Modifier.height(14.dp))
                // PUSH button just below the game band (out of the play view). Press = jump, hold = fly.
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(PaletteYellow.bg)
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                web?.evaluateJavascript("try{pressDown()}catch(e){}", null)
                                tryAwaitRelease()
                                web?.evaluateJavascript("try{pressUp()}catch(e){}", null)
                            })
                        }
                        .padding(horizontal = 48.dp, vertical = 18.dp),
                ) { Text("⤒ PUSH", color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 24.sp) }
            }
        }
        Spacer(Modifier.height(10.dp))
        GameControls(onExit = onExit)
    }
}
