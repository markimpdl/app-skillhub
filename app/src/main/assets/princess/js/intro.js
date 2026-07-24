// 3D intro cutscene: a sunny park, kids playing, Peach & Toad — until a giant
// scary Yoshi bursts out from behind the trees, the ground opens, and everyone falls.
import * as THREE from 'three';
import { sfx } from './audio.js';
import {
  getRenderer, disposeScene, buildKid, buildPeach, buildToad, buildScaryYoshi,
  buildHole, buildParkSet, animatePlayground,
} from './park.js';

const INTRO_LENGTH = 34; // seconds

const CAPTIONS = [
  [0.5,  6.5,  '☀️ A beautiful sunny day at the park…'],
  [7,    11.5, 'Princess Peach and Toad were having fun with the kids.'],
  [12.5, 16.5, '😱 Suddenly… a GIANT SCARY YOSHI appeared from behind the trees!'],
  [17,   20.5, 'Everyone ran away screaming!'],
  [21,   25.5, 'A huge hole opened in the ground — Peach and Toad fell in!'],
  [26.2, 29.8, '"HA HA HA HA!!!"'],
  [30.5, 33.5, 'The monster jumped into the hole after them!'],
];

const CAM_KEYS = [
  { t: 0,    pos: [-24, 7, 26], look: [0, 2, 0] },
  { t: 7,    pos: [14, 6, 26],  look: [0, 2, 0] },
  { t: 9,    pos: [5, 3.5, 16], look: [0, 2, 8] },
  { t: 12,   pos: [5, 3.5, 16], look: [0, 2, 8] },
  { t: 14,   pos: [0, 6, 28],   look: [0, 9, -20] },
  { t: 17,   pos: [0, 8, 32],   look: [0, 4, -5] },
  { t: 21,   pos: [7, 6, 19],   look: [0, 0.5, 8] },
  { t: 26,   pos: [0, 7, 12],   look: [0, 8, -18] },
  { t: 30.5, pos: [12, 9, 28],  look: [0, 2, 4] },
  { t: 34,   pos: [12, 9, 28],  look: [0, 2, 4] },
];

function lerp(a, b, k) { return a + (b - a) * k; }
function ease(k) { return k * k * (3 - 2 * k); }

export function playIntro(canvas, captionEl, onDone) {
  const renderer = getRenderer(canvas);
  const scene = new THREE.Scene();
  const camera = new THREE.PerspectiveCamera(55, window.innerWidth / window.innerHeight, 0.1, 200);

  const { playground } = buildParkSet(scene);

  // kids
  const KID_COLORS = [0xe53935, 0x1e88e5, 0xfdd835, 0x8e24aa, 0x00acc1, 0xff7043];
  const kids = KID_COLORS.map((c, i) => {
    const k = buildKid(c);
    const angle = (i / KID_COLORS.length) * Math.PI * 2;
    const r = 5 + (i % 3) * 3;
    k.position.set(Math.cos(angle) * r, 0, Math.sin(angle) * r - 1);
    k.userData.home = k.position.clone();
    k.userData.phase = i * 1.7;
    const flee = k.position.clone().sub(new THREE.Vector3(0, 0, -22)).setY(0).normalize();
    k.userData.flee = flee;
    scene.add(k);
    return k;
  });

  const peach = buildPeach();
  peach.position.set(0.9, 0, 8);
  peach.rotation.y = -0.6;
  scene.add(peach);

  const toad = buildToad();
  toad.position.set(-1.0, 0, 8.2);
  toad.rotation.y = 0.7;
  scene.add(toad);

  const yoshi = buildScaryYoshi();
  yoshi.position.set(0, -11, -22); // hidden below ground, behind the trees
  scene.add(yoshi);

  const hole = buildHole();
  hole.position.set(0, 0, 8);
  hole.scale.setScalar(0.001);
  scene.add(hole);

  // ---------- timeline state ----------
  const soundCues = [
    { t: 12.0, fn: sfx.rumble },
    { t: 13.5, fn: sfx.roar },
    { t: 17.2, fn: sfx.scream },
    { t: 21.2, fn: sfx.rumble },
    { t: 23.0, fn: sfx.fall },
    { t: 26.2, fn: sfx.laugh },
    { t: 28.0, fn: sfx.laugh },
  ];
  let cueIdx = 0;
  let capIdx = -1;
  let start = null;
  let rafId = 0;
  let finished = false;

  const camPos = new THREE.Vector3();
  const camLook = new THREE.Vector3();

  function setCamera(t) {
    let i = 0;
    while (i < CAM_KEYS.length - 2 && CAM_KEYS[i + 1].t <= t) i++;
    const a = CAM_KEYS[i], b = CAM_KEYS[i + 1];
    const k = ease(Math.min(1, Math.max(0, (t - a.t) / (b.t - a.t))));
    camPos.set(lerp(a.pos[0], b.pos[0], k), lerp(a.pos[1], b.pos[1], k), lerp(a.pos[2], b.pos[2], k));
    camLook.set(lerp(a.look[0], b.look[0], k), lerp(a.look[1], b.look[1], k), lerp(a.look[2], b.look[2], k));
    if ((t > 12 && t < 16.5) || (t > 21 && t < 23)) {
      const s = 0.25;
      camPos.x += (Math.random() - 0.5) * s;
      camPos.y += (Math.random() - 0.5) * s;
    }
    camera.position.copy(camPos);
    camera.lookAt(camLook);
  }

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

    while (cueIdx < soundCues.length && t >= soundCues[cueIdx].t) {
      soundCues[cueIdx].fn();
      cueIdx++;
    }
    setCamera(t);
    setCaption(t);
    animatePlayground(playground, t);

    // kids
    for (const kid of kids) {
      const p = kid.userData.phase;
      if (t < 17) {
        kid.position.x = kid.userData.home.x + Math.sin(t * 0.8 + p) * 1.2;
        kid.position.z = kid.userData.home.z + Math.cos(t * 0.6 + p) * 1.2;
        kid.position.y = Math.abs(Math.sin(t * 4 + p)) * 0.35;
        kid.rotation.y = t * 0.5 + p;
      } else {
        const ft = t - 17;
        kid.position.addScaledVector(kid.userData.flee, 0.14);
        kid.position.y = Math.abs(Math.sin(ft * 9 + p)) * 0.3;
        kid.rotation.y = Math.atan2(kid.userData.flee.x, kid.userData.flee.z);
        const [aL, aR] = kid.userData.arms;
        aL.rotation.z = 2.6 + Math.sin(ft * 10) * 0.3;
        aR.rotation.z = -2.6 - Math.sin(ft * 10) * 0.3;
      }
    }

    // Peach & Toad: bob while chatting, tremble at the monster, then fall
    if (t < 12) {
      peach.position.y = Math.sin(t * 2) * 0.05;
      toad.position.y = Math.abs(Math.sin(t * 3)) * 0.15;
    } else if (t < 23) {
      peach.position.y = 0;
      toad.position.y = 0;
      peach.position.x = 0.9 + (Math.random() - 0.5) * 0.06;
      toad.position.x = -1.0 + (Math.random() - 0.5) * 0.06;
      peach.rotation.y = Math.PI;
      toad.rotation.y = Math.PI;
    } else if (t < 25) {
      const k = (t - 23) / 2;
      peach.position.y = -k * k * 14;
      toad.position.y = -k * k * 14;
      peach.rotation.z = k * 5;
      toad.rotation.z = -k * 6;
    } else {
      peach.visible = false;
      toad.visible = false;
    }

    // the hole opens
    if (t >= 21.2 && t < 22.6) {
      const k = ease((t - 21.2) / 1.4);
      hole.scale.setScalar(Math.max(0.001, k));
    } else if (t >= 22.6) {
      hole.scale.setScalar(1);
    }

    // the monster
    if (t >= 12 && t < 16) {
      const k = ease((t - 12) / 4);
      yoshi.position.y = lerp(-11, 0, k);
    } else if (t >= 16 && t < 26) {
      yoshi.position.y = 0;
      yoshi.rotation.z = Math.sin(t * 8) * 0.02;
    } else if (t >= 26 && t < 30.5) {
      yoshi.position.y = Math.abs(Math.sin((t - 26) * 8)) * 0.7;
      const [aL, aR] = yoshi.userData.arms;
      aL.rotation.z = 2.4;
      aR.rotation.z = -2.4;
    } else if (t >= 30.5 && t < 32.3) {
      const k = (t - 30.5) / 1.8;
      yoshi.position.z = lerp(-22, 8, k);
      yoshi.position.y = Math.sin(k * Math.PI) * 16 - (k > 0.5 ? (k - 0.5) * 2 * 10 : 0);
      const shrink = k > 0.6 ? 1 - (k - 0.6) / 0.4 * 0.75 : 1;
      yoshi.scale.setScalar(Math.max(0.25, shrink));
      yoshi.rotation.x = k * 0.9;
    } else if (t >= 32.3) {
      yoshi.visible = false;
    }

    renderer.render(scene, camera);

    if (t >= INTRO_LENGTH) {
      end();
      return;
    }
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
