package com.osaparecidos.memoriamaker.games.topcar

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

// Kiosk tweaks: force the on-screen touch pad, lock difficulty to EASY (its picker row is hidden),
// drop the top-left mute button (kiosk rule: no interactive controls near the top), and float the
// steer/gas clusters at mid-screen — on the tall totem the canvas letterboxes in the middle, so the
// bottom-anchored buttons would end up far below the action and out of small kids' reach.
private const val TOPCAR_INJECT = """
(function(){
  document.body.classList.add('touch');
  try { selectedDifficulty = 'easy'; } catch(e){}
  var st=document.createElement('style');
  st.textContent=
    '#muteBtn{display:none!important;}'+
    '.section-label{display:none!important;}'+
    '#diffGrid{display:none!important;}'+
    '.hint{display:none!important;}'+
    '.tc-left,.tc-right{bottom:42%!important;}'+
    '#touch button{opacity:.55;}';
  document.head.appendChild(st);
  // The pad now floats mid-screen, so keep it hidden while a menu overlay is up (it would sit on
  // top of the car/track cards). The overlay toggles the .hidden class (and endRace renames its id
  // to #finish, same node), so watch the class attribute on that element.
  var ov=document.getElementById('overlay')||document.getElementById('finish');
  var tc=document.getElementById('touch');
  function sync(){ if(tc&&ov) tc.style.display = ov.classList.contains('hidden') ? 'block' : 'none'; }
  if(ov&&tc){ new MutationObserver(sync).observe(ov,{attributes:true,attributeFilter:['class']}); sync(); }
})();
"""

/**
 * Mubarak's Top Car (TURBO GEAR) — a retro pseudo-3D racer, run in a WebView. The game brings its
 * own big touch pad (steer left/right, GAS, TURBO) pinned to the bottom of the viewport, so the
 * WebView gets the whole play area and the canvas letterboxes in the middle.
 */
@Composable
fun TopCarGame(onExit: () -> Unit, @Suppress("UNUSED_PARAMETER") onFinish: (GameResult) -> Unit) {
    GameScaffold(title = "Top Car", onBack = onExit, showBack = false) {
        Box(Modifier.fillMaxWidth().weight(1f).background(Color.Black)) {
            WebGameView(
                assetPath = "topcar/index.html",
                modifier = Modifier.fillMaxSize(),
                injectOnLoad = TOPCAR_INJECT,
            )
        }
        Spacer(Modifier.height(10.dp))
        GameControls(onExit = onExit)
    }
}
