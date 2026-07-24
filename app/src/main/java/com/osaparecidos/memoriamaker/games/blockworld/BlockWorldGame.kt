package com.osaparecidos.memoriamaker.games.blockworld

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

// Kiosk touch layout: force on-screen controls, keep movement joystick bottom-LEFT, move the utility
// tools column to the RIGHT (actions are already right), and make a quick tap = dig/attack.
private const val BW_INJECT = """
(function(){
  document.body.classList.add('touch');
  // Clean start screen: remove title/prose/note.
  document.querySelectorAll('#overlay h1, #overlay > p, #overlay .mnote').forEach(function(e){e.remove();});
  // Force Medium + With Robots (click sets the game state), then remove the selectors — Start just plays.
  try{ var rb=document.querySelector('#menu [data-mode=robots]'); if(rb) rb.click();
       var md=document.querySelector('#menu [data-diff=medium]'); if(md) md.click(); }catch(e){}
  document.querySelectorAll('#menu .mrow').forEach(function(e){e.remove();});
  // Remove utility column + mini-map — but keep the real View button (it already carries the
  // game's doAct('view') listener) so we can re-home it on the kiosk bar as a 1st/3rd-person toggle.
  var tb2=document.getElementById('tbtns2');
  var viewBtn = tb2 ? tb2.querySelector('[data-act=view]') : null;
  if(tb2) tb2.remove();
  ['mini','minilbl'].forEach(function(id){var e=document.getElementById(id); if(e) e.remove();});

  // Fire a game action by triggering its (now hidden) original button.
  function fire(act){var b=document.querySelector('#tbtns [data-act='+act+']'); if(b) b.dispatchEvent(new Event('touchstart',{cancelable:true}));}

  // ONE vertical bar on the right: tool hotbar + Jump / Rescue / Carry — all matching squares, no labels.
  var bar=document.createElement('div'); bar.id='kioskbar';
  document.body.appendChild(bar);
  var hb=document.getElementById('hotbar'); if(hb) bar.appendChild(hb);
  [['jump','↑'],['rescue','🔑'],['carry','🧒']].forEach(function(p){
    var b=document.createElement('button'); b.className='kb'; b.textContent=p[1];
    b.addEventListener('touchstart',function(e){e.preventDefault(); fire(p[0]);},{passive:false});
    b.addEventListener('click',function(){ fire(p[0]); });
    bar.appendChild(b);
  });
  // 1st / 3rd-person camera toggle: reuse the real View button (keeps its doAct listener), restyle it.
  if(viewBtn){ viewBtn.className='kb'; viewBtn.textContent='👤'; viewBtn.title='View: 1st / 3rd person';
    viewBtn.addEventListener('click',function(){ viewBtn.dispatchEvent(new Event('touchstart',{cancelable:true})); });
    bar.appendChild(viewBtn); }

  var st=document.createElement('style');
  st.textContent=
    '#touch{display:block!important;}'+
    '#tbtns{display:none!important;}'+
    '.slot .nm{display:none!important;}'+                                                   /* hide tool names */
    '#joy{left:16px!important;bottom:auto!important;top:44%!important;}'+
    '#kioskbar{position:fixed;right:10px;top:18%;display:flex;flex-direction:column;align-items:center;gap:8px;z-index:9;}'+
    'body.touch #hotbar{position:static!important;left:auto!important;right:auto!important;bottom:auto!important;top:auto!important;transform:none!important;flex-direction:column!important;background:none!important;border:none!important;padding:0!important;gap:8px!important;}'+
    'body.touch #kioskbar .slot{width:64px!important;height:64px!important;border-radius:10px!important;}'+
    '#kioskbar .kb{width:64px;height:64px;border-radius:10px;font-size:28px;background:rgba(0,0,0,0.5);color:#fff;border:3px solid #555;}';
  document.head.appendChild(st);

  // Tap the view = dig/attack.
  var lp=document.getElementById('lookpad');
  if(lp){
    var sx=0,sy=0,ts=0;
    lp.addEventListener('touchstart',function(e){var t=e.changedTouches[0];sx=t.clientX;sy=t.clientY;ts=Date.now();},{passive:true});
    lp.addEventListener('touchend',function(e){var t=e.changedTouches[0];
      if(Math.hypot(t.clientX-sx,t.clientY-sy)<14 && (Date.now()-ts)<300) fire('dig');
    },{passive:true});
  }
})();
"""

/**
 * Rawdha's Block World — the student's real 3D (Three.js) game, run in a WebView, played vertically
 * on the kiosk panel. Movement joystick sits bottom-left, tool/option buttons on the right (the
 * active tool is highlighted by the game), and a tap on the view digs/attacks.
 */
@Composable
fun BlockWorldGame(onExit: () -> Unit, @Suppress("UNUSED_PARAMETER") onFinish: (GameResult) -> Unit) {
    GameScaffold(title = "Block World", onBack = onExit, showBack = false) {
        Box(Modifier.fillMaxWidth().weight(1f).background(Color.Black)) {
            WebGameView(
                assetPath = "blockworld/index.html",
                modifier = Modifier.fillMaxSize(),
                injectOnLoad = BW_INJECT,
            )
        }
        Spacer(Modifier.height(10.dp))
        GameControls(onExit = onExit)
    }
}
