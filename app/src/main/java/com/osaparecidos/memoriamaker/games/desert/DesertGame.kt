package com.osaparecidos.memoriamaker.games.desert

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.GameResult
import com.osaparecidos.memoriamaker.ui.components.GameControls
import com.osaparecidos.memoriamaker.ui.components.GameScaffold
import com.osaparecidos.memoriamaker.ui.components.WebGameView
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.PaletteGreen

// Kiosk tweaks: hide the keyboard-hint panels (touch drives everything here) and add a helper that
// synthesizes a real keydown→keyup pair — the game's actions (E/F/Space/R) are read edge-triggered
// from its `keys{}` map, so the press must persist for at least one animation frame (120ms is safe).
// HUD panels must only ever be hidden with CSS, never removed: updateHUD() does getElementById on
// its stat nodes every frame and a missing node would freeze the game (same class as the old
// Block World drawMini black-screen bug).
private const val DESERT_INJECT = """
(function(){
  window.kioskKey = function(c){
    window.dispatchEvent(new KeyboardEvent('keydown',{code:c}));
    setTimeout(function(){ window.dispatchEvent(new KeyboardEvent('keyup',{code:c})); }, 120);
  };
  var st=document.createElement('style');
  st.textContent=
    '#help{display:none!important;}'+
    '#lockHint{display:none!important;}'+
    '#overlay .keys{display:none!important;}'+
    /* portrait kiosk: the three top HUD panels collide side-by-side — stack them instead */
    '#stats{top:8px!important;left:8px!important;min-width:210px!important;}'+
    '#clock{top:8px!important;left:auto!important;right:8px!important;transform:none!important;}'+
    '#camelPanel{top:44px!important;right:8px!important;min-width:180px!important;}';
  document.head.appendChild(st);
})();
"""

/**
 * Mubarak's Explore the Desert — a 3D (Three.js) open-world adventure with a boy and his camel,
 * run full-size in a WebView. Walking is tap-to-walk in the game itself; the survival actions
 * (collect / hunt / eat / ride) get big Compose buttons at the bottom that synthesize the game's
 * key shortcuts.
 */
@Composable
fun DesertGame(onExit: () -> Unit, @Suppress("UNUSED_PARAMETER") onFinish: (GameResult) -> Unit) {
    var web by remember { mutableStateOf<WebView?>(null) }
    fun key(code: String) = web?.evaluateJavascript("try{kioskKey('$code')}catch(e){}", null)

    GameScaffold(title = "Explore the Desert", onBack = onExit, showBack = false) {
        Box(Modifier.fillMaxWidth().weight(1f).background(Color.Black)) {
            WebGameView(
                assetPath = "desert/index.html",
                modifier = Modifier.fillMaxSize(),
                injectOnLoad = DESERT_INJECT,
                onCreated = { web = it },
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionButton("🌿", "COLLECT", Modifier.weight(1f)) { key("KeyE") }
            ActionButton("🏹", "HUNT", Modifier.weight(1f)) { key("KeyF") }
            ActionButton("🍖", "EAT·DRINK", Modifier.weight(1f)) { key("Space") }
            ActionButton("🐪", "RIDE", Modifier.weight(1f)) { key("KeyR") }
        }
        Spacer(Modifier.height(10.dp))
        GameControls(onExit = onExit)
    }
}

@Composable
private fun ActionButton(emoji: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PaletteGreen.bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 24.sp)
        Text(label, color = Color.White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}
