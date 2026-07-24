// Phase 2 (playable): boss fight against the giant Yoshi.
// Stomp green koopas → kick their shells at Yoshi (3 hits to win).
// After each hit Yoshi rages and charges — escape on the floating blocks!
// Mario: 5 lives, starts big; a hit shrinks him, a hit while small costs a life.
// A red mushroom appears sometimes: small → grow, big → extra life.
import { sfx } from './audio.js';
import { WORLD_H, GROUND_Y, drawCaveBackground } from './level.js';
import { drawMario, drawPeach, drawToad, drawYoshi, drawKoopa, drawShell, drawMushroom } from './sprites2d.js';

const GRAVITY = 0.55;
const JUMP_V = -13.5;
const ACCEL = 0.55;
const MAX_SPEED = 4.6;
const FRICTION = 0.82;

const ARENA_W = 1400;
const YOSHI_SCALE = 0.8;
const PLATFORMS = [
  { x: 120, y: 410, w: 120 }, { x: 640, y: 410, w: 120 }, { x: 1160, y: 410, w: 110 },
  { x: 330, y: 300, w: 140 }, { x: 850, y: 300, w: 140 }, { x: 1255, y: 290, w: 145 },
];
const HIGH_PLATFORMS = [PLATFORMS[3], PLATFORMS[4]]; // mushroom spawn spots
const VICTIM_LEDGE = PLATFORMS[5]; // Peach & Toad wait here, out of reach

export function startBoss(canvas, warnEl, onWin) {
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

  // ----- state -----
  let mario, yoshi, koopas, shells, mushroom, lives, time, flash, state, stateT, spawnT, mushT, banner;

  function reset() {
    mario = { x: 80, y: GROUND_Y, vx: 0, vy: 0, onGround: true, facing: 1, run: 0, big: true, invuln: 0 };
    yoshi = { x: 1130, hp: 3, state: 'idle', t: 0, dir: -1, passes: 0 };
    koopas = [];
    shells = [];
    mushroom = null;
    lives = 5;
    time = 0;
    flash = 0;
    state = 'play'; // 'play' | 'gameover' | 'won' | 'done'
    stateT = 0;
    spawnT = 1.5; // first koopa arrives quickly
    mushT = 9;
    banner = 3;
  }
  reset();

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

  function hurtMario() {
    if (mario.invuln > 0 || state !== 'play') return;
    flash = 1;
    if (mario.big) {
      mario.big = false;
      mario.invuln = 120;
      sfx.shrink();
      return;
    }
    lives--;
    sfx.hurt();
    if (lives <= 0) {
      sfx.laugh();
      state = 'gameover';
      stateT = 3.5;
      return;
    }
    mario.x = 80; mario.y = GROUND_Y; mario.vx = 0; mario.vy = 0;
    mario.big = true;
    mario.invuln = 120;
  }

  function hitYoshi() {
    yoshi.hp--;
    sfx.roar();
    if (yoshi.hp <= 0) {
      yoshi.state = 'dying';
      yoshi.t = 0;
      sfx.victory();
      state = 'won';
      stateT = 3;
      warnEl.classList.remove('show');
      return;
    }
    yoshi.state = 'rage';
    yoshi.t = 0;
    sfx.alarm();
  }

  function update(dt) {
    time += dt / 60;
    if (banner > 0) banner -= dt / 60;
    if (flash > 0) flash -= dt / 20;

    if (state === 'gameover') {
      stateT -= dt / 60;
      if (stateT <= 0) reset(); // the boss fight restarts itself
      return;
    }
    if (state === 'won') {
      yoshi.t += dt / 60;
      stateT -= dt / 60;
      if (stateT <= 0) { state = 'done'; onWin(); }
      return;
    }

    // ----- Mario -----
    if (keys.left)  { mario.vx -= ACCEL * dt; mario.facing = -1; }
    if (keys.right) { mario.vx += ACCEL * dt; mario.facing = 1; }
    if (!keys.left && !keys.right) mario.vx *= Math.pow(FRICTION, dt);
    mario.vx = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, mario.vx));

    if (keys.jump && !jumpHeld && mario.onGround) {
      mario.vy = JUMP_V;
      mario.onGround = false;
      sfx.jump();
    }
    jumpHeld = keys.jump;
    if (!keys.jump && mario.vy < -4) mario.vy = -4;

    mario.vy += GRAVITY * dt;
    mario.x += mario.vx * dt;
    mario.y += mario.vy * dt;
    mario.x = Math.max(30, Math.min(ARENA_W - 30, mario.x));
    if (mario.invuln > 0) mario.invuln -= dt;

    mario.onGround = false;
    if (mario.vy >= 0) {
      if (mario.y >= GROUND_Y && mario.y - mario.vy * dt <= GROUND_Y + 20) {
        mario.y = GROUND_Y; mario.vy = 0; mario.onGround = true;
      }
      for (const p of PLATFORMS) {
        if (p === VICTIM_LEDGE) continue; // Peach's ledge is out of play
        if (mario.x >= p.x && mario.x <= p.x + p.w &&
            mario.y >= p.y && mario.y - mario.vy * dt <= p.y + 14) {
          mario.y = p.y; mario.vy = 0; mario.onGround = true;
        }
      }
    }
    if (mario.onGround && Math.abs(mario.vx) > 0.3) mario.run += dt * 0.35;

    // ----- koopas -----
    spawnT -= dt / 60;
    if (spawnT <= 0 && koopas.length < 2 && yoshi.state !== 'dying') {
      const fromLeft = mario.x > ARENA_W / 2; // enter from the side away from Mario
      koopas.push({ x: fromLeft ? 40 : ARENA_W - 40, dir: fromLeft ? 1 : -1, t: 0 });
      spawnT = 6;
    }
    for (let i = koopas.length - 1; i >= 0; i--) {
      const k = koopas[i];
      k.t += dt / 60;
      k.x += k.dir * 1.3 * dt;
      if (k.x < 40 || k.x > ARENA_W - 40) k.dir *= -1;

      const dx = mario.x - k.x;
      const stomping = mario.vy > 2 && Math.abs(dx) < 26 &&
                       mario.y >= GROUND_Y - 48 && mario.y <= GROUND_Y - 16;
      if (stomping) {
        koopas.splice(i, 1);
        shells.push({ x: k.x, vx: 0 });
        mario.vy = -9;
        sfx.stomp();
      } else if (Math.abs(dx) < 24 && mario.y > GROUND_Y - 34) {
        hurtMario();
      }
    }

    // ----- shells -----
    for (let i = shells.length - 1; i >= 0; i--) {
      const s = shells[i];
      if (s.vx === 0) {
        // kick it
        if (Math.abs(mario.x - s.x) < 28 && mario.y > GROUND_Y - 44) {
          s.vx = 13 * (s.x >= mario.x ? 1 : -1);
          sfx.kick();
        }
      } else {
        s.x += s.vx * dt;
        // shells knock out other koopas on the way
        for (let j = koopas.length - 1; j >= 0; j--) {
          if (Math.abs(koopas[j].x - s.x) < 26) { koopas.splice(j, 1); sfx.stomp(); }
        }
        // hit the boss (only while he stands his ground)
        if (yoshi.state === 'idle' && Math.abs(s.x - yoshi.x) < 70) {
          shells.splice(i, 1);
          hitYoshi();
          continue;
        }
        if (s.x < 10 || s.x > ARENA_W - 10) shells.splice(i, 1);
      }
    }

    // ----- mushroom -----
    mushT -= dt / 60;
    if (mushT <= 0 && !mushroom) {
      const p = HIGH_PLATFORMS[Math.floor(time) % 2];
      mushroom = { x: p.x + p.w / 2, y: p.y, life: 6.5 };
      sfx.coin();
      mushT = 12;
    }
    if (mushroom) {
      mushroom.life -= dt / 60;
      if (mushroom.life <= 0) mushroom = null;
      else if (Math.abs(mario.x - mushroom.x) < 28 && Math.abs(mario.y - mushroom.y) < 54) {
        if (!mario.big) { mario.big = true; sfx.grow(); }
        else if (lives < 9) { lives++; sfx.oneup(); }
        else sfx.coin();
        mushroom = null;
      }
    }

    // ----- Yoshi boss -----
    yoshi.t += dt / 60;
    if (yoshi.state === 'idle') {
      // faces Mario, shuffles a little
      yoshi.dir = mario.x < yoshi.x ? -1 : 1;
      warnEl.classList.remove('show');
    } else if (yoshi.state === 'rage') {
      warnEl.textContent = '⚠ "RRAAAGH!! I\'LL CRUSH YOU!" — GET ON THE HIGH BLOCKS!';
      warnEl.classList.add('show');
      if (yoshi.t > 1.3) {
        yoshi.state = 'charge';
        yoshi.dir = mario.x < yoshi.x ? -1 : 1;
        yoshi.passes = 0;
      }
    } else if (yoshi.state === 'charge') {
      yoshi.x += yoshi.dir * 13 * dt;
      if (yoshi.x < 90 || yoshi.x > ARENA_W - 90) {
        yoshi.x = Math.max(90, Math.min(ARENA_W - 90, yoshi.x));
        yoshi.dir *= -1;
        yoshi.passes++;
        if (yoshi.passes >= 2) {
          yoshi.state = 'idle';
          warnEl.classList.remove('show');
        }
      }
    }
    // boss contact (any state) — high blocks are the safe spot
    if (Math.abs(mario.x - yoshi.x) < 78 && mario.y > 330 && yoshi.state !== 'dying') {
      hurtMario();
    }
  }

  // ----- rendering -----
  function render() {
    ctx.setTransform(scale, 0, 0, scale, 0, 0);
    let camX;
    if (viewW >= ARENA_W) camX = (ARENA_W - viewW) / 2;
    else camX = Math.max(0, Math.min(mario.x - viewW / 2, ARENA_W - viewW));

    drawCaveBackground(ctx, camX, viewW, time);

    ctx.save();
    ctx.translate(-camX, 0);

    // arena floor + rock walls
    ctx.fillStyle = '#54356b';
    ctx.fillRect(-200, GROUND_Y, ARENA_W + 400, WORLD_H - GROUND_Y + 40);
    ctx.fillStyle = '#7e57a5';
    ctx.fillRect(-200, GROUND_Y, ARENA_W + 400, 14);
    ctx.fillStyle = '#3b2350';
    ctx.fillRect(-200, -50, 210, WORLD_H + 100);
    ctx.fillRect(ARENA_W - 10, -50, 210, WORLD_H + 100);

    // floating blocks
    for (const p of PLATFORMS) {
      ctx.fillStyle = p.y < 350 ? '#c98f3d' : '#8d6e63';
      ctx.fillRect(p.x, p.y, p.w, 16);
      ctx.fillStyle = p.y < 350 ? '#e8b465' : '#a98274';
      ctx.fillRect(p.x, p.y, p.w, 5);
    }

    // Peach & Toad on their ledge
    const trem = yoshi.state === 'charge' ? (Math.random() - 0.5) * 3 : 0;
    drawPeach(ctx, VICTIM_LEDGE.x + 95 + trem, VICTIM_LEDGE.y, -1, 0, false);
    drawToad(ctx, VICTIM_LEDGE.x + 40 + trem, VICTIM_LEDGE.y, -1, 0, false);
    if (state === 'play' && Math.floor(time / 4) % 3 === 0) {
      ctx.font = 'bold 16px "Segoe UI", sans-serif';
      ctx.fillStyle = '#fff';
      ctx.strokeStyle = '#333'; ctx.lineWidth = 2;
      const txt = 'Help us, Mario!';
      const tw = ctx.measureText(txt).width + 20;
      ctx.beginPath(); ctx.roundRect(VICTIM_LEDGE.x + 30, VICTIM_LEDGE.y - 105, tw, 30, 9);
      ctx.fill(); ctx.stroke();
      ctx.fillStyle = '#222';
      ctx.fillText(txt, VICTIM_LEDGE.x + 40, VICTIM_LEDGE.y - 84);
    }

    if (mushroom) drawMushroom(ctx, mushroom.x, mushroom.y, time);

    for (const k of koopas) drawKoopa(ctx, k.x, GROUND_Y, k.dir, k.t);
    for (const s of shells) drawShell(ctx, s.x, GROUND_Y, s.vx !== 0, time);

    // boss
    if (yoshi.state === 'dying') {
      const k = Math.min(1, yoshi.t / 2);
      ctx.save();
      ctx.globalAlpha = 1 - k * 0.9;
      ctx.translate(yoshi.x, GROUND_Y);
      ctx.rotate(k * Math.PI / 2);
      drawYoshi(ctx, 0, 0, 0, { facing: yoshi.dir, scale: YOSHI_SCALE });
      ctx.restore();
    } else {
      drawYoshi(ctx, yoshi.x, GROUND_Y, yoshi.state === 'charge' ? time * 2 : time,
        { facing: yoshi.dir, scale: YOSHI_SCALE });
    }

    if (state !== 'gameover' && (mario.invuln <= 0 || Math.floor(time * 12) % 2 === 0)) {
      drawMario(ctx, mario.x, mario.y, mario.facing, mario.run, !mario.onGround,
        { big: mario.big, t: time });
    }

    ctx.restore();

    // ----- HUD -----
    ctx.font = '20px "Segoe UI", sans-serif';
    for (let i = 0; i < Math.max(5, lives); i++) {
      ctx.fillStyle = i < lives ? '#ff4d79' : 'rgba(255,255,255,.25)';
      ctx.fillText('♥', 22 + i * 24, 40);
    }
    ctx.fillStyle = '#fff';
    ctx.font = 'bold 15px "Segoe UI", sans-serif';
    ctx.fillText(mario.big ? 'BIG' : 'SMALL', 24, 64);

    ctx.textAlign = 'right';
    ctx.fillStyle = '#fff';
    ctx.font = 'bold 18px "Segoe UI", sans-serif';
    ctx.fillText('BOSS', viewW - 24, 34);
    ctx.font = '22px "Segoe UI", sans-serif';
    for (let i = 0; i < 3; i++) {
      ctx.fillStyle = i < yoshi.hp ? '#7bd94b' : 'rgba(255,255,255,.25)';
      ctx.fillText('😈', viewW - 24 - i * 30, 62);
    }
    ctx.textAlign = 'left';

    // banner
    if (banner > 0) {
      ctx.textAlign = 'center';
      ctx.fillStyle = `rgba(255, 210, 62, ${Math.min(1, banner)})`;
      ctx.font = 'bold 60px "Segoe UI", sans-serif';
      ctx.fillText('⚔ BOSS FIGHT! ⚔', viewW / 2, 150);
      ctx.font = 'bold 22px "Segoe UI", sans-serif';
      ctx.fillStyle = `rgba(255, 255, 255, ${Math.min(1, banner)})`;
      ctx.fillText('Stomp the turtles — kick their shells at the monster!', viewW / 2, 190);
      ctx.textAlign = 'left';
    }

    if (state === 'won') {
      ctx.textAlign = 'center';
      ctx.fillStyle = '#ffd23e';
      ctx.font = 'bold 64px "Segoe UI", sans-serif';
      ctx.fillText('YOU DID IT!', viewW / 2, WORLD_H / 2 - 40);
      ctx.textAlign = 'left';
    }

    if (state === 'gameover') {
      ctx.fillStyle = 'rgba(10, 0, 10, 0.65)';
      ctx.fillRect(0, 0, viewW, WORLD_H);
      ctx.textAlign = 'center';
      ctx.fillStyle = '#ff4d4d';
      ctx.font = 'bold 72px "Segoe UI", sans-serif';
      ctx.fillText('GAME OVER', viewW / 2, WORLD_H / 2 - 20);
      ctx.fillStyle = '#fff';
      ctx.font = 'bold 26px "Segoe UI", sans-serif';
      ctx.fillText('But Mario never gives up — try again!', viewW / 2, WORLD_H / 2 + 30);
      ctx.textAlign = 'left';
    }

    if (flash > 0) {
      ctx.fillStyle = `rgba(255, 40, 40, ${Math.min(flash, 0.5)})`;
      ctx.fillRect(0, 0, viewW, WORLD_H);
    }
  }

  if (location.search.includes('debug')) {
    window.__debugBoss = {
      mario: () => mario, yoshi: () => yoshi,
      lives: () => lives, state: () => state,
      hitYoshi, hurt: hurtMario,
      spawnShell: (x) => shells.push({ x: x ?? mario.x + 60, vx: 0 }),
    };
  }

  let last = performance.now();
  let rafId = 0;
  function loop(now) {
    if (destroyed) return;
    const dt = Math.min((now - last) / (1000 / 60), 3);
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
