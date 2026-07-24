// Phase 1 (playable): 2D side-scroller. Peach (with Toad following) escapes
// through an underground cave while the giant Yoshi chases. At the end she
// finds a phone and calls Mario.
import { sfx } from './audio.js';
import {
  WORLD_H, GROUND_Y, LEVEL_END, PLATFORMS, SPIKES, CHECKPOINTS, PHONE_X,
  groundAt, drawCaveBackground, drawTerrain, drawSpikes,
} from './level.js';
import { drawPeach, drawToad, drawYoshi, drawPhone } from './sprites2d.js';

const GRAVITY = 0.55;
const JUMP_V = -13.5;
const ACCEL = 0.55;
const MAX_SPEED = 4.6;
const FRICTION = 0.82;

const COINS = [];
for (const p of PLATFORMS) {
  for (let i = 0; i < 3; i++) COINS.push({ x: p.x + 30 + i * 45, y: p.y - 45, got: false });
}

const DIALOG = [
  { who: 'sys',   text: '📞 RING… RING…', dur: 1.6, snd: sfx.ring },
  { who: 'peach', text: 'A phone! Please, please work…', dur: 2.4 },
  { who: 'peach', text: 'MARIO! HELP! A giant monster is chasing us!', dur: 3.0 },
  { who: 'mario', text: "Don't worry, Peach! I'm-a coming!", dur: 2.8 },
];

export function startGame(canvas, warnEl, onWin, onLose) {
  for (const c of COINS) c.got = false; // fresh coins on every run
  const ctx = canvas.getContext('2d');
  let scale = 1, viewW = 800;
  let destroyed = false;

  function resize() {
    canvas.width = window.innerWidth * Math.min(window.devicePixelRatio, 2);
    canvas.height = window.innerHeight * Math.min(window.devicePixelRatio, 2);
    scale = canvas.height / WORLD_H;
    viewW = canvas.width / scale;
  }
  resize();
  window.addEventListener('resize', resize);

  const player = {
    x: CHECKPOINTS[0], y: GROUND_Y, vx: 0, vy: 0,
    onGround: true, facing: 1, run: 0, checkpoint: 0, invuln: 0,
  };
  const monster = { x: -700, t: 0 };
  const trail = []; // recent player states, for Toad to follow
  let coinCount = 0;
  let lives = 10;
  let state = 'play'; // 'play' | 'dialog' | 'gameover' | 'done'
  let dialogIdx = -1, dialogT = 0, gameoverT = 0;
  let time = 0;
  let flash = 0;

  // ----- input -----
  const keys = { left: false, right: false, jump: false };
  let jumpHeld = false;
  function onKey(down) {
    return (e) => {
      const k = e.code;
      if (k === 'ArrowLeft' || k === 'KeyA') keys.left = down;
      else if (k === 'ArrowRight' || k === 'KeyD') keys.right = down;
      else if (k === 'Space' || k === 'ArrowUp' || k === 'KeyW') { keys.jump = down; e.preventDefault(); }
    };
  }
  const kd = onKey(true), ku = onKey(false);
  window.addEventListener('keydown', kd);
  window.addEventListener('keyup', ku);

  const touchBinds = [];
  function bindTouch(id, key) {
    const el = document.getElementById(id);
    if (!el) return;
    const on = (e) => { e.preventDefault(); keys[key] = true; };
    const off = (e) => { e.preventDefault(); keys[key] = false; };
    el.addEventListener('pointerdown', on);
    el.addEventListener('pointerup', off);
    el.addEventListener('pointercancel', off);
    el.addEventListener('pointerleave', off);
    touchBinds.push([el, on, off]);
  }
  bindTouch('tc-left', 'left');
  bindTouch('tc-right', 'right');
  bindTouch('tc-jump', 'jump');

  function respawn() {
    sfx.hurt();
    flash = 1;
    lives--;
    if (lives <= 0) {
      sfx.laugh();
      state = 'gameover';
      gameoverT = 4;
      warnEl.classList.remove('show');
      return;
    }
    const cp = CHECKPOINTS[player.checkpoint];
    player.x = cp; player.y = GROUND_Y; player.vx = 0; player.vy = 0;
    player.invuln = 90;
    monster.x = cp - 750;
    trail.length = 0;
  }

  function update(dt) {
    time += dt / 60;

    if (state === 'gameover') {
      gameoverT -= dt / 60;
      if (gameoverT <= 0) {
        state = 'done';
        onLose();
      }
      return;
    }

    if (state === 'dialog') {
      dialogT -= dt / 60;
      if (dialogT <= 0) {
        dialogIdx++;
        if (dialogIdx >= DIALOG.length) {
          state = 'done';
          onWin();
          return;
        }
        const d = DIALOG[dialogIdx];
        dialogT = d.dur;
        if (d.snd) d.snd();
      }
      return;
    }

    // movement
    if (keys.left)  { player.vx -= ACCEL * dt; player.facing = -1; }
    if (keys.right) { player.vx += ACCEL * dt; player.facing = 1; }
    if (!keys.left && !keys.right) player.vx *= Math.pow(FRICTION, dt);
    player.vx = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, player.vx));

    if (keys.jump && !jumpHeld && player.onGround) {
      player.vy = JUMP_V;
      player.onGround = false;
      sfx.jump();
    }
    jumpHeld = keys.jump;
    if (!keys.jump && player.vy < -4) player.vy = -4; // variable jump height

    player.vy += GRAVITY * dt;
    player.x += player.vx * dt;
    player.y += player.vy * dt;
    player.x = Math.max(monster.x + 60, player.x); // can't run past the monster backwards
    if (player.invuln > 0) player.invuln -= dt;

    // landing: ground + platforms (only when falling)
    player.onGround = false;
    if (player.vy >= 0) {
      const gy = groundAt(player.x);
      if (player.y >= gy && player.y - player.vy * dt <= gy + 20) {
        player.y = gy; player.vy = 0; player.onGround = true;
      }
      for (const p of PLATFORMS) {
        if (player.x >= p.x && player.x <= p.x + p.w &&
            player.y >= p.y && player.y - player.vy * dt <= p.y + 14) {
          player.y = p.y; player.vy = 0; player.onGround = true;
        }
      }
    }
    if (player.onGround && Math.abs(player.vx) > 0.3) player.run += dt * 0.35;

    // fell into a pit
    if (player.y > WORLD_H + 120) { respawn(); return; }

    // spikes
    if (player.invuln <= 0 && player.onGround) {
      for (const s of SPIKES) {
        if (player.x > s.x && player.x < s.x + s.w && Math.abs(player.y - GROUND_Y) < 4) {
          respawn(); return;
        }
      }
    }

    // coins
    for (const c of COINS) {
      if (!c.got && Math.abs(player.x - c.x) < 26 && Math.abs(player.y - 30 - c.y) < 40) {
        c.got = true; coinCount++; sfx.coin();
      }
    }

    // checkpoints
    for (let i = player.checkpoint + 1; i < CHECKPOINTS.length; i++) {
      if (player.x >= CHECKPOINTS[i]) player.checkpoint = i;
    }

    // Toad follows via the position trail
    trail.push({ x: player.x, y: player.y, facing: player.facing, run: player.run, air: !player.onGround });
    if (trail.length > 220) trail.shift();

    // monster chase with rubber-banding
    monster.t += dt / 60;
    const dist = player.x - monster.x;
    let mspeed = 3.4;
    if (dist > 950) mspeed = 5.6;
    else if (dist > 600) mspeed = 4.3;
    else if (dist < 260) mspeed = 3.0;
    monster.x += mspeed * dt;

    warnEl.classList.toggle('show', dist < 420);
    if (dist < 420) warnEl.textContent = '⚠ THE MONSTER IS RIGHT BEHIND YOU! RUN!';

    // caught!
    if (player.invuln <= 0 && dist < 95) { respawn(); return; }

    // reached the phone
    if (player.x >= PHONE_X - 40) {
      state = 'dialog';
      dialogIdx = -1; dialogT = 0;
      warnEl.classList.remove('show');
    }

    if (flash > 0) flash -= dt / 20;
  }

  // ----- rendering -----
  function render() {
    ctx.setTransform(scale, 0, 0, scale, 0, 0);
    const camX = Math.max(0, Math.min(player.x - viewW * 0.35, LEVEL_END - viewW));

    drawCaveBackground(ctx, camX, viewW, time);

    ctx.save();
    ctx.translate(-camX, 0);

    drawTerrain(ctx);
    drawSpikes(ctx, SPIKES);

    // coins
    for (const c of COINS) {
      if (c.got) continue;
      const wob = Math.sin(time * 5 + c.x) * 4;
      ctx.fillStyle = '#ffd23e';
      ctx.beginPath(); ctx.ellipse(c.x, c.y + wob, 9, 12, 0, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#c99b13';
      ctx.beginPath(); ctx.ellipse(c.x, c.y + wob, 4, 7, 0, 0, Math.PI * 2); ctx.fill();
    }
    // checkpoint flags
    for (let i = 1; i < CHECKPOINTS.length; i++) {
      const cx = CHECKPOINTS[i];
      ctx.fillStyle = '#e0e0e0';
      ctx.fillRect(cx - 2, GROUND_Y - 90, 4, 90);
      ctx.fillStyle = i <= player.checkpoint ? '#66bb6a' : '#ef5350';
      ctx.beginPath();
      ctx.moveTo(cx + 2, GROUND_Y - 90); ctx.lineTo(cx + 34, GROUND_Y - 78); ctx.lineTo(cx + 2, GROUND_Y - 66);
      ctx.closePath(); ctx.fill();
    }

    drawPhone(ctx, PHONE_X, GROUND_Y, time);

    // Toad trails behind the player
    const tp = trail.length > 24 ? trail[trail.length - 24] : trail[0];
    if (tp) drawToad(ctx, tp.x, tp.y, tp.facing, tp.run, tp.air);

    if (state !== 'gameover' && (player.invuln <= 0 || Math.floor(time * 12) % 2 === 0)) {
      drawPeach(ctx, player.x, player.y, player.facing, player.run, !player.onGround);
    }

    drawYoshi(ctx, monster.x, GROUND_Y, monster.t);

    // dialog bubbles
    if (state === 'dialog' && dialogIdx >= 0) {
      const d = DIALOG[dialogIdx];
      const bx = d.who === 'mario' ? PHONE_X : player.x;
      const by = d.who === 'mario' ? GROUND_Y - 110 : player.y - 100;
      ctx.font = 'bold 19px "Segoe UI", sans-serif';
      const tw = ctx.measureText(d.text).width + 30;
      ctx.fillStyle = d.who === 'mario' ? '#ffdddd' : '#fff';
      ctx.strokeStyle = '#333'; ctx.lineWidth = 3;
      const rx = Math.min(Math.max(bx - tw / 2, camX + 10), camX + viewW - tw - 10);
      ctx.beginPath();
      ctx.roundRect(rx, by - 34, tw, 44, 12);
      ctx.fill(); ctx.stroke();
      ctx.fillStyle = '#222';
      ctx.fillText(d.text, rx + 15, by - 4);
    }

    ctx.restore();

    // HUD: coins
    ctx.fillStyle = '#ffd23e';
    ctx.beginPath(); ctx.ellipse(30, 34, 9, 12, 0, 0, Math.PI * 2); ctx.fill();
    ctx.fillStyle = '#fff';
    ctx.font = 'bold 22px "Segoe UI", sans-serif';
    ctx.fillText('× ' + coinCount, 48, 42);

    // HUD: lives (10 hearts, lost ones grayed out)
    ctx.font = '20px "Segoe UI", sans-serif';
    for (let i = 0; i < 10; i++) {
      ctx.fillStyle = i < lives ? '#ff4d79' : 'rgba(255,255,255,.25)';
      ctx.fillText('♥', 22 + i * 24, 74);
    }

    // game over overlay
    if (state === 'gameover' || (state === 'done' && lives <= 0)) {
      ctx.fillStyle = 'rgba(10, 0, 10, 0.65)';
      ctx.fillRect(0, 0, viewW, WORLD_H);
      ctx.textAlign = 'center';
      ctx.fillStyle = '#ff4d4d';
      ctx.font = 'bold 72px "Segoe UI", sans-serif';
      ctx.fillText('GAME OVER', viewW / 2, WORLD_H / 2 - 20);
      ctx.fillStyle = '#fff';
      ctx.font = 'bold 26px "Segoe UI", sans-serif';
      ctx.fillText('The monster got Peach and Toad…', viewW / 2, WORLD_H / 2 + 30);
      ctx.textAlign = 'left';
    }

    // hurt flash
    if (flash > 0) {
      ctx.fillStyle = `rgba(255, 40, 40, ${Math.min(flash, 0.5)})`;
      ctx.fillRect(0, 0, viewW, WORLD_H);
    }
  }

  if (location.search.includes('debug')) {
    window.__debug = {
      player, monster,
      state: () => state,
      lives: () => lives,
      hurt: () => respawn(),
      warp: (x) => { player.x = x; player.y = GROUND_Y; player.vy = 0; monster.x = x - 900; trail.length = 0; },
    };
  }

  let last = performance.now();
  let rafId = 0;
  function loop(now) {
    if (destroyed) return;
    const dt = Math.min((now - last) / (1000 / 60), 3); // in 60fps frames, clamped
    last = now;
    if (state !== 'done') update(dt);
    render();
    if (state !== 'done') rafId = requestAnimationFrame(loop);
  }
  rafId = requestAnimationFrame(loop);

  function destroy() {
    destroyed = true;
    cancelAnimationFrame(rafId);
    window.removeEventListener('resize', resize);
    window.removeEventListener('keydown', kd);
    window.removeEventListener('keyup', ku);
    for (const [el, on, off] of touchBinds) {
      el.removeEventListener('pointerdown', on);
      el.removeEventListener('pointerup', off);
      el.removeEventListener('pointercancel', off);
      el.removeEventListener('pointerleave', off);
    }
    warnEl.classList.remove('show');
  }
  return { destroy };
}
