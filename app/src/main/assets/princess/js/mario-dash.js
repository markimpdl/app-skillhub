// 2D auto-play cutscene: star-powered Mario races through the same cave level
// at full speed, smashing every obstacle, until he finds the giant Yoshi
// cornering Peach and Toad.
import { sfx } from './audio.js';
import {
  WORLD_H, GROUND_Y, LEVEL_END, SPIKES, PHONE_X,
  groundAt, drawCaveBackground, drawTerrain, drawSpikes,
} from './level.js';
import { drawMario, drawPeach, drawToad, drawYoshi, drawPhone } from './sprites2d.js';

const GRAVITY = 0.55;
const RUN_SPEED = 10;
const STOP_X = LEVEL_END - 700;
const YOSHI_X = LEVEL_END - 380;
const VICTIMS_X = LEVEL_END - 130;

export function playMarioDash(canvas, onDone) {
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

  const spikes = SPIKES.map(s => ({ ...s, dead: false })); // local copy — the real level keeps its spikes
  const mario = { x: 80, y: GROUND_Y, vy: 0, onGround: true, run: 0 };
  const trail = [];
  const particles = [];
  let time = 0;
  let state = 'run'; // 'run' | 'confront' | 'done'
  let confrontT = 0;

  function burst(x, y, color) {
    for (let i = 0; i < 12; i++) {
      particles.push({
        x, y,
        vx: (Math.random() - 0.5) * 14,
        vy: -Math.random() * 12,
        life: 1,
        color,
      });
    }
  }

  function update(dt) {
    time += dt / 60;

    if (state === 'confront') {
      confrontT -= dt / 60;
      if (confrontT <= 0) {
        state = 'done';
        onDone();
      }
      return;
    }

    // sprint
    mario.x += RUN_SPEED * dt;
    mario.run += dt * 0.7;

    // auto-jump over pits
    if (mario.onGround && groundAt(mario.x + 170) === Infinity) {
      mario.vy = -14;
      mario.onGround = false;
      sfx.jump();
    }
    mario.vy += GRAVITY * dt;
    mario.y += mario.vy * dt;
    if (mario.vy >= 0) {
      const gy = groundAt(mario.x);
      if (mario.y >= gy) { mario.y = gy; mario.vy = 0; mario.onGround = true; }
    }

    // smash spikes
    for (const s of spikes) {
      if (!s.dead && mario.x + 45 > s.x && mario.x - 45 < s.x + s.w) {
        s.dead = true;
        sfx.smash();
        burst(s.x + s.w / 2, GROUND_Y - 12, '#cfd8dc');
      }
    }

    // star trail
    trail.push({ x: mario.x, y: mario.y, run: mario.run, air: !mario.onGround });
    if (trail.length > 20) trail.shift();

    // particles
    for (const p of particles) {
      p.vx *= 0.96;
      p.vy += 0.5 * dt;
      p.x += p.vx * dt;
      p.y += p.vy * dt;
      p.life -= dt / 40;
    }
    for (let i = particles.length - 1; i >= 0; i--) {
      if (particles[i].life <= 0) particles.splice(i, 1);
    }

    // arrived at the showdown
    if (mario.x >= STOP_X) {
      mario.x = STOP_X;
      state = 'confront';
      confrontT = 3;
      sfx.yell();
    }
  }

  function caption(text) {
    ctx.font = 'bold 24px "Segoe UI", sans-serif';
    ctx.textAlign = 'center';
    const tw = ctx.measureText(text).width + 40;
    ctx.fillStyle = 'rgba(0,0,0,0.65)';
    ctx.beginPath();
    ctx.roundRect(viewW / 2 - tw / 2, 26, tw, 44, 12);
    ctx.fill();
    ctx.fillStyle = '#fff';
    ctx.fillText(text, viewW / 2, 56);
    ctx.textAlign = 'left';
  }

  function render() {
    ctx.setTransform(scale, 0, 0, scale, 0, 0);
    const camX = Math.max(0, Math.min(mario.x - viewW * 0.4, LEVEL_END - viewW));

    drawCaveBackground(ctx, camX, viewW, time);

    ctx.save();
    ctx.translate(-camX, 0);

    drawTerrain(ctx);
    drawSpikes(ctx, spikes);
    drawPhone(ctx, PHONE_X, GROUND_Y, time);

    // the showdown scene at the end of the cave
    if (camX + viewW > YOSHI_X - 400) {
      const trembleP = (Math.random() - 0.5) * 3;
      const trembleT = (Math.random() - 0.5) * 3;
      drawPeach(ctx, VICTIMS_X + trembleP, GROUND_Y, -1, 0, false);
      drawToad(ctx, VICTIMS_X - 45 + trembleT, GROUND_Y, -1, 0, false);
      drawYoshi(ctx, YOSHI_X, GROUND_Y, time);
    }

    // star afterimages
    for (let i = 0; i < trail.length; i += 4) {
      const tp = trail[i];
      const alpha = (i / trail.length) * 0.3;
      ctx.save();
      ctx.globalAlpha = alpha;
      drawMario(ctx, tp.x, tp.y, 1, tp.run, tp.air, { star: false, t: time });
      ctx.restore();
    }
    drawMario(ctx, mario.x, mario.y, 1, mario.run, !mario.onGround, { star: true, t: time });

    // smash particles
    for (const p of particles) {
      ctx.globalAlpha = Math.max(0, p.life);
      ctx.fillStyle = p.color;
      ctx.fillRect(p.x - 3, p.y - 3, 6, 6);
    }
    ctx.globalAlpha = 1;

    // speech at the showdown
    if (state === 'confront') {
      const text = '"STOP RIGHT THERE, MONSTER!"';
      ctx.font = 'bold 20px "Segoe UI", sans-serif';
      const tw = ctx.measureText(text).width + 30;
      const bx = mario.x - tw / 2, by = mario.y - 110;
      ctx.fillStyle = '#fff';
      ctx.strokeStyle = '#333'; ctx.lineWidth = 3;
      ctx.beginPath(); ctx.roundRect(bx, by - 30, tw, 42, 12); ctx.fill(); ctx.stroke();
      ctx.fillStyle = '#222';
      ctx.fillText(text, bx + 15, by - 2);
    }

    ctx.restore();

    // narration
    if (state === 'run') {
      if (time < 4) caption('⚡ Mario raced through the cave — UNSTOPPABLE! ⚡');
      else if (mario.x > LEVEL_END * 0.55 && mario.x < LEVEL_END * 0.8) {
        caption('Nothing could slow him down!');
      }
    } else if (state === 'confront') {
      caption('He found the monster — about to grab Peach and Toad!');
    }
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
  }
  return { destroy, skip: () => { if (state !== 'done') { state = 'done'; onDone(); } } };
}
