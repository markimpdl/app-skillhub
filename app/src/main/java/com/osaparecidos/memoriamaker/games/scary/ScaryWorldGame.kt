package com.osaparecidos.memoriamaker.games.scary

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
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
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.PalettePink
import com.osaparecidos.memoriamaker.ui.theme.PaletteYellow
import com.osaparecidos.memoriamaker.ui.components.WebGameView

// Kiosk tweaks: hide the in-page touch controls (we drive the global `keys` object from big Compose
// buttons under the band instead), trim the menu prose/warning so the 5 hero cards fit the band,
// and drop the PWA-install instructions.
private const val SCARY_INJECT = """
(function(){
  var st=document.createElement('style');
  st.textContent=
    '#joy,#joyKnob,#jumpBtn,#knifeBtn{display:none!important;}'+
    '#menu .warn{display:none!important;}'+
    '#menu .story{display:none!important;}'+
    '#menu .ctrl{display:none!important;}'+
    '#menu h1{font-size:1.3rem;margin:2px 0;}'+
    '#menu{padding:6px;justify-content:center;gap:4px;}'+
    '#menu .pick{font-size:.8rem;margin:2px 0;}'+
    /* 5 hero cards in ONE compact row so the whole menu fits the landscape band */
    '#menu .chars{gap:6px;flex-wrap:nowrap;width:98%;}'+
    '#menu .card{width:19%;min-width:0;padding:8px 4px;}'+
    '#menu .card .face{font-size:26px;}'+
    '#menu .card .nm{font-size:12px;letter-spacing:1px;margin:4px 0 2px;}'+
    '#menu .card .st{display:none;}';
  document.head.appendChild(st);
})();
"""

/**
 * Almaha's Scary World (Miessi's Haunted Kingdom) — a spooky pseudo-3D platformer, run in a WebView
 * inside a centered landscape band (like Geometry Rush). Movement/jump/knife are big Compose buttons
 * below the band, wired to the game's global `keys` object and `throwKnife()`.
 */
@Composable
fun ScaryWorldGame(onExit: () -> Unit, @Suppress("UNUSED_PARAMETER") onFinish: (GameResult) -> Unit) {
    var web by remember { mutableStateOf<WebView?>(null) }
    var playing by remember { mutableStateOf(false) }
    fun js(code: String) = web?.evaluateJavascript("try{$code}catch(e){}", null)

    // The floating controls sit over the play band, so only show them during actual gameplay —
    // over the hero menu / level & game-over overlays they'd cover the game's own buttons.
    LaunchedEffect(web) {
        while (true) {
            web?.evaluateJavascript(
                "(function(){try{return document.querySelector('.overlay:not(.hidden)')?'menu':'play'}catch(e){return 'menu'}})()",
            ) { v -> playing = v?.contains("play") == true }
            delay(400)
        }
    }

    GameScaffold(title = "Scary World", onBack = onExit, showBack = false) {
        Spacer(Modifier.height(8.dp))
        Text("Pick a hero · dodge the ghosts · beat the boss! 👻", color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        assetPath = "almaha/index.html",
                        modifier = Modifier.fillMaxSize(),
                        injectOnLoad = SCARY_INJECT,
                        onCreated = { web = it },
                    )
                    // Floating semi-transparent controls over the lower part of the play band
                    // (same pattern as Camel Race / Top Car): D-pad ◀ ▶ runs along the road,
                    // ▲ ▼ walks into / out of the screen depth.
                    if (playing) Row(
                        Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            HoldButton("▲", onDown = { js("keys.up=true") }, onUp = { js("keys.up=false") })
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                HoldButton("◀", onDown = { js("keys.left=true") }, onUp = { js("keys.left=false") })
                                HoldButton("▼", onDown = { js("keys.down=true") }, onUp = { js("keys.down=false") })
                                HoldButton("▶", onDown = { js("keys.right=true") }, onUp = { js("keys.right=false") })
                            }
                        }
                        HoldButton("⬆ JUMP", onDown = { js("keys.jump=true") }, onUp = { js("keys.jump=false") }, wide = true)
                        HoldButton("🔪", onDown = { js("throwKnife()") }, onUp = {})
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        GameControls(onExit = onExit)
    }
}

@Composable
private fun HoldButton(label: String, onDown: () -> Unit, onUp: () -> Unit, wide: Boolean = false) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background((if (wide) PaletteYellow.bg else PalettePink.bg).copy(alpha = 0.55f))
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    onDown()
                    tryAwaitRelease()
                    onUp()
                })
            }
            .padding(horizontal = if (wide) 24.dp else 18.dp, vertical = 14.dp),
    ) { Text(label, color = if (wide) InkNavy else Color.White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
}
