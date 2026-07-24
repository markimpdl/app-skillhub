// Victory sequence, two scenes:
//  1. playRescue (2D, in the cave): Peach & Toad run to Mario and jump, thankful.
//  2. playCelebration (3D, back at the park): everyone celebrates — THE END!
import * as THREE from 'three';
import { sfx } from './audio.js';
import { WORLD_H, GROUND_Y, drawCaveBackground } from './level.js';
import { drawMario, drawPeach, drawToad } from './sprites2d.js';
import {
  getRenderer, disposeScene, buildKid, buildPeach, buildToad, buildMario,
  buildParkSet, animatePlayground,
} from './park.js';

// ---------- scene 1: the rescue (2D) ----------

export function playRescue(canvas, onDone) {
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

  const LENGTH = 7.5;
  let time = 0;
  const hearts = [];

  function render() {
    ctx.setTransform(scale, 0, 0, scale, 0, 0);
    drawCaveBackground(ctx, 0, viewW, time);
    ctx.fillStyle = '#54356b';
    ctx.fillRect(0, GROUND_Y, viewW, WORLD_H - GROUND_Y + 40);
    ctx.fillStyle = '#7e57a5';
    ctx.fillRect(0, GROUND_Y, viewW, 14);

    const cx = viewW / 2;
    const t = time;

    // Peach & Toad run in from the right, then everyone jumps for joy
    const runK = Math.min(1, t / 2.2);
    const px = cx + 90 + (1 - runK) * 350;
    const tx = cx + 150 + (1 - runK) * 420;
    const jump = t > 2.4 ? Math.abs(Math.sin((t - 2.4) * 5)) * 40 : 0;

    drawMario(ctx, cx - 60, GROUND_Y - jump, 1, t * 4, jump > 2, { big: true, t });
    drawPeach(ctx, px, GROUND_Y - jump * 0.9, -1, runK < 1 ? t * 6 : 0, jump > 2);
    drawToad(ctx, tx, GROUND_Y - jump * 1.1, -1, runK < 1 ? t * 7 : 0, jump > 2);

    // floating hearts
    if (t > 2.4 && Math.random() < 0.25) {
      hearts.push({ x: cx - 120 + Math.random() * 320, y: GROUND_Y - 90, life: 1 });
    }
    ctx.font = '26px "Segoe UI", sans-serif';
    for (const h of hearts) {
      h.y -= 1.6;
      h.life -= 0.012;
      if (h.life <= 0) continue;
      ctx.globalAlpha = Math.max(0, h.life);
      ctx.fillStyle = '#ff4d79';
      ctx.fillText('❤', h.x, h.y);
    }
    ctx.globalAlpha = 1;

    // captions
    const text = t < 2.4 ? '"MARIO! You saved us!"'
               : t < 5 ? '"Thank you, thank you, THANK YOU!" ❤'
               : 'Time to go home…';
    ctx.font = 'bold 26px "Segoe UI", sans-serif';
    ctx.textAlign = 'center';
    const tw = ctx.measureText(text).width + 40;
    ctx.fillStyle = 'rgba(0,0,0,0.65)';
    ctx.beginPath(); ctx.roundRect(cx - tw / 2, 60, tw, 48, 12); ctx.fill();
    ctx.fillStyle = '#fff';
    ctx.fillText(text, cx, 93);
    ctx.textAlign = 'left';
  }

  let last = performance.now();
  let rafId = 0;
  let finished = false;
  let cheered = false;

  function loop(now) {
    if (destroyed || finished) return;
    const dt = Math.min((now - last) / 1000, 0.05);
    last = now;
    time += dt;
    if (!cheered && time > 2.4) { cheered = true; sfx.victory(); }
    render();
    if (time >= LENGTH) { end(); return; }
    rafId = requestAnimationFrame(loop);
  }
  rafId = requestAnimationFrame(loop);

  function end() {
    if (finished) return;
    finished = true;
    cancelAnimationFrame(rafId);
    window.removeEventListener('resize', resize);
    onDone();
  }

  return { destroy: () => { destroyed = true; cancelAnimationFrame(rafId); window.removeEventListener('resize', resize); }, skip: end };
}

// ---------- scene 2: the celebration (3D) ----------

const CAPTIONS = [
  [0.5, 4.2, '🌳 Back at the park — safe at last!'],
  [4.7, 8.2, '🎉 HOORAY FOR MARIO! 🎉'],
  [8.8, 12.5, '⭐ THE END! ⭐'],
];

export function playCelebration(canvas, captionEl, onDone) {
  const LENGTH = 13;
  const renderer = getRenderer(canvas);
  const scene = new THREE.Scene();
  const camera = new THREE.PerspectiveCamera(55, window.innerWidth / window.innerHeight, 0.1, 200);

  const { playground } = buildParkSet(scene);

  // the heroes, front and center
  const mario = buildMario();
  mario.position.set(0, 0, 8);
  const peach = buildPeach();
  peach.position.set(2, 0, 8);
  const toad = buildToad();
  toad.position.set(-1.8, 0, 8.3);
  scene.add(mario, peach, toad);

  // the kids are back!
  const KID_COLORS = [0xe53935, 0x1e88e5, 0xfdd835, 0x8e24aa, 0x00acc1, 0xff7043];
  const kids = KID_COLORS.map((c, i) => {
    const k = buildKid(c);
    const angle = (i / KID_COLORS.length) * Math.PI * 2;
    k.position.set(Math.cos(angle) * 7, 0, Math.sin(angle) * 5 + 6);
    k.userData.phase = i * 1.3;
    scene.add(k);
    return k;
  });

  // confetti
  const confetti = [];
  const confettiColors = [0xff4d79, 0xffd23e, 0x4dd2ff, 0x7bd94b, 0xc77bff];
  for (let i = 0; i < 130; i++) {
    const m = new THREE.Mesh(
      new THREE.PlaneGeometry(0.22, 0.22),
      new THREE.MeshBasicMaterial({ color: confettiColors[i % confettiColors.length], side: THREE.DoubleSide }));
    m.position.set((i % 30) - 15 + (i % 7) * 0.4, 12 + (i % 13), ((i * 7) % 24) - 4);
    scene.add(m);
    confetti.push({ mesh: m, speed: 1.5 + (i % 5) * 0.55, spin: 2 + (i % 4) });
  }

  const soundCues = [
    { t: 0.4, fn: sfx.cheer },
    { t: 3.0, fn: sfx.cheer },
    { t: 5.0, fn: sfx.cheer },
    { t: 8.8, fn: sfx.victory },
  ];
  let cueIdx = 0;
  let capIdx = -1;
  let start = null;
  let rafId = 0;
  let finished = false;

  function setCaption(t) {
    let active = -1;
    for (let i = 0; i < CAPTIONS.length; i++) {
      if (t >= CAPTIONS[i][0] && t <= CAPTIONS[i][1]) { active = i; break; }
    }
    if (active !== capIdx) {
      capIdx = active;
      if (active >= 0) {
        captionEl.textContent = CAPTIONS[active][2];
        captionEl.classList.add('show');
      } else {
        captionEl.classList.remove('show');
      }
    }
  }

  function animate(nowMs) {
    if (finished) return;
    if (start === null) start = nowMs;
    const t = (nowMs - start) / 1000;
    const dt = 1 / 60;

    while (cueIdx < soundCues.length && t >= soundCues[cueIdx].t) {
      soundCues[cueIdx].fn();
      cueIdx++;
    }
    setCaption(t);
    animatePlayground(playground, t);

    // slow orbit camera
    const a = t * 0.12 - 0.4;
    camera.position.set(Math.sin(a) * 21, 6.5, Math.cos(a) * 21 + 8);
    camera.lookAt(0, 2.5, 8);

    // everyone jumps for joy
    mario.position.y = Math.abs(Math.sin(t * 4)) * 0.9;
    peach.position.y = Math.abs(Math.sin(t * 4 + 0.5)) * 0.7;
    toad.position.y = Math.abs(Math.sin(t * 4 + 1)) * 1.0;
    mario.rotation.y = Math.sin(t * 0.7) * 0.4;
    for (const kid of kids) {
      const p = kid.userData.phase;
      kid.position.y = Math.abs(Math.sin(t * 5 + p)) * 0.5;
      kid.rotation.y = t * 1.2 + p;
      const [aL, aR] = kid.userData.arms;
      aL.rotation.z = 2.6 + Math.sin(t * 8 + p) * 0.3;
      aR.rotation.z = -2.6 - Math.sin(t * 8 + p) * 0.3;
    }

    // confetti falls forever
    for (const c of confetti) {
      c.mesh.position.y -= c.speed * dt;
      c.mesh.rotation.x += c.spin * dt;
      c.mesh.rotation.y += c.spin * 0.7 * dt;
      if (c.mesh.position.y < 0) c.mesh.position.y = 14 + Math.random() * 6;
    }

    renderer.render(scene, camera);

    if (t >= LENGTH) { end(); return; }
    rafId = requestAnimationFrame(animate);
  }

  function onResize() {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
  }
  window.addEventListener('resize', onResize);

  function end() {
    if (finished) return;
    finished = true;
    cancelAnimationFrame(rafId);
    window.removeEventListener('resize', onResize);
    captionEl.classList.remove('show');
    disposeScene(scene);
    onDone();
  }

  rafId = requestAnimationFrame(animate);
  return { skip: end };
}
