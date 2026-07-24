// 3D cutscene: Mario arrives at the (now empty) park, finds a Star inside a
// floating ? block, powers up with lightning, and dives into the hole.
import * as THREE from 'three';
import { sfx } from './audio.js';
import {
  getRenderer, disposeScene, buildMario, buildQuestionBlock, buildStar,
  buildHole, buildParkSet, animatePlayground,
} from './park.js';

const LENGTH = 25; // seconds

const CAPTIONS = [
  [0.5,  4.5,  'Meanwhile… MARIO arrived at the park!'],
  [5,    8.3,  '"Mamma mia! Where is everyone?!"'],
  [8.8,  10.8, 'He spotted a strange floating block…'],
  [11.5, 13.8, '⭐ A SUPER STAR!'],
  [14.5, 18.8, '⚡ SUPER STAR POWER! ⚡'],
  [19.2, 21.4, '"PRINCESS!! I\'M-A COMING!!!"'],
  [22,   24.5, 'And he dove into the hole after them!'],
];

const CAM_KEYS = [
  { t: 0,    pos: [-16, 5, 26],  look: [-14, 2, 12] },
  { t: 4,    pos: [3, 4, 20],    look: [-2, 2, 10] },
  { t: 8.5,  pos: [10, 3.5, 12], look: [6, 3, 4] },
  { t: 14,   pos: [10, 3, 10],   look: [7, 2.2, 4] },
  { t: 19,   pos: [2, 4, 15],    look: [5, 2, 5] },
  { t: 21.5, pos: [10, 8, 24],   look: [0, 1, 8] },
  { t: 25,   pos: [10, 8, 24],   look: [0, 1, 8] },
];

function lerp(a, b, k) { return a + (b - a) * k; }
function ease(k) { return k * k * (3 - 2 * k); }

export function playMarioIntro(canvas, captionEl, onDone) {
  const renderer = getRenderer(canvas);
  const scene = new THREE.Scene();
  const camera = new THREE.PerspectiveCamera(55, window.innerWidth / window.innerHeight, 0.1, 200);

  const { playground } = buildParkSet(scene);

  const hole = buildHole();
  hole.position.set(0, 0, 8);
  scene.add(hole);

  const mario = buildMario();
  mario.position.set(-26, 0, 12);
  scene.add(mario);
  const parts = mario.userData.parts;
  const baseColors = {};
  for (const [name, mesh] of Object.entries(parts)) {
    baseColors[name] = mesh.material.color.clone();
    mesh.material = mesh.material.clone(); // own materials so flashing doesn't leak
  }

  const block = buildQuestionBlock();
  block.position.set(6, 4.4, 4);
  scene.add(block);

  const star = buildStar();
  star.position.copy(block.position);
  star.visible = false;
  scene.add(star);

  // block debris
  const debris = [];
  for (let i = 0; i < 10; i++) {
    const piece = new THREE.Mesh(new THREE.BoxGeometry(0.35, 0.35, 0.35),
      new THREE.MeshLambertMaterial({ color: 0xffb300 }));
    piece.visible = false;
    scene.add(piece);
    debris.push({
      mesh: piece,
      vel: new THREE.Vector3((i % 5 - 2) * 1.6, 3 + (i % 3) * 1.5, ((i * 3) % 5 - 2) * 1.2),
      spin: 3 + i,
    });
  }

  // lightning bolts around Mario during power-up
  const bolts = [];
  const boltMat = new THREE.LineBasicMaterial({ color: 0xffee55 });
  for (let i = 0; i < 6; i++) {
    const a = (i / 6) * Math.PI * 2;
    const pts = [];
    let px = 0, py = 3.2, pz = 0;
    for (let s = 0; s < 5; s++) {
      pts.push(new THREE.Vector3(px, py, pz));
      px += Math.cos(a) * 0.5 + (s % 2 ? 0.3 : -0.3);
      pz += Math.sin(a) * 0.5 + (s % 2 ? -0.3 : 0.3);
      py -= 0.7;
    }
    const line = new THREE.Line(new THREE.BufferGeometry().setFromPoints(pts), boltMat);
    line.visible = false;
    bolts.push(line);
    mario.add(line);
  }

  const soundCues = [
    { t: 10.9, fn: sfx.smash },
    { t: 11.5, fn: sfx.coin },
    { t: 14.2, fn: sfx.powerup },
    { t: 15.5, fn: sfx.roar },   // thunder-ish rumble for the power surge
    { t: 19.2, fn: sfx.yell },
    { t: 22.9, fn: sfx.jump },
    { t: 23.4, fn: sfx.fall },
  ];
  let cueIdx = 0;
  let capIdx = -1;
  let start = null;
  let rafId = 0;
  let finished = false;
  let debrisFly = false;

  const camPos = new THREE.Vector3();
  const camLook = new THREE.Vector3();

  function setCamera(t) {
    let i = 0;
    while (i < CAM_KEYS.length - 2 && CAM_KEYS[i + 1].t <= t) i++;
    const a = CAM_KEYS[i], b = CAM_KEYS[i + 1];
    const k = ease(Math.min(1, Math.max(0, (t - a.t) / (b.t - a.t))));
    camPos.set(lerp(a.pos[0], b.pos[0], k), lerp(a.pos[1], b.pos[1], k), lerp(a.pos[2], b.pos[2], k));
    camLook.set(lerp(a.look[0], b.look[0], k), lerp(a.look[1], b.look[1], k), lerp(a.look[2], b.look[2], k));
    if (t > 14.5 && t < 19) { // power surge shake
      camPos.x += (Math.random() - 0.5) * 0.15;
      camPos.y += (Math.random() - 0.5) * 0.15;
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

  function runBob(t, speed = 10) {
    mario.position.y = Math.abs(Math.sin(t * speed)) * 0.18;
    parts.legL.rotation.x = Math.sin(t * speed) * 0.6;
    parts.legR.rotation.x = -Math.sin(t * speed) * 0.6;
    parts.armL.rotation.x = -Math.sin(t * speed) * 0.5;
    parts.armR.rotation.x = Math.sin(t * speed) * 0.5;
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
    setCamera(t);
    setCaption(t);
    animatePlayground(playground, t);

    // block idle: bob + slow spin (until broken)
    if (t < 10.9) {
      block.position.y = 4.4 + Math.sin(t * 2) * 0.15;
      block.rotation.y = t * 0.8;
    }

    // ----- Mario timeline -----
    if (t < 4) {
      // runs in
      const k = ease(t / 4);
      mario.position.x = lerp(-26, -2, k);
      mario.position.z = lerp(12, 10, k);
      mario.rotation.y = Math.PI / 2;
      runBob(t);
    } else if (t < 8.5) {
      // looks around, hand to forehead
      mario.position.set(-2, 0, 10);
      mario.rotation.y = Math.PI / 2 + Math.sin((t - 4) * 1.6) * 1.1;
      parts.legL.rotation.x = 0;
      parts.legR.rotation.x = 0;
      parts.armL.rotation.x = 0;
      parts.armR.rotation.z = -2.6; // hand up, scanning
      parts.armR.rotation.x = -0.6;
    } else if (t < 10.5) {
      // runs under the block
      const k = ease((t - 8.5) / 2);
      mario.position.x = lerp(-2, 6, k);
      mario.position.z = lerp(10, 4, k);
      mario.rotation.y = Math.PI / 2 - k * Math.PI / 2;
      parts.armR.rotation.z = -0.5;
      parts.armR.rotation.x = 0;
      runBob(t);
    } else if (t < 11.4) {
      // jumps and headbutts the block
      const k = (t - 10.5) / 0.9;
      mario.position.x = 6;
      mario.position.z = 4;
      mario.position.y = Math.sin(k * Math.PI) * 2.7;
      parts.armL.rotation.z = 2.8; // fist up
    } else if (t < 14.2) {
      // lands, walks to the star
      const k = ease(Math.min(1, (t - 11.4) / 2));
      mario.position.y = 0;
      mario.position.x = lerp(6, 7.5, k);
      mario.rotation.y = 0.4;
      parts.armL.rotation.z = 0.5;
      runBob(t, 7);
    } else if (t < 19) {
      // POWER UP
      mario.position.set(7.5, Math.abs(Math.sin(t * 12)) * 0.1, 4);
      const s = 1 + ease(Math.min(1, (t - 14.2) / 1.5)) * 0.18;
      mario.scale.setScalar(s);
      const hue = (t * 2.5) % 1;
      for (const mesh of Object.values(parts)) {
        mesh.material.color.setHSL(hue, 0.9, 0.55);
      }
      for (const bolt of bolts) bolt.visible = Math.random() < 0.45;
      parts.armL.rotation.z = 2.6;
      parts.armR.rotation.z = -2.6;
    } else if (t < 21.5) {
      // yells to the sky
      for (const [name, mesh] of Object.entries(parts)) mesh.material.color.copy(baseColors[name]);
      for (const bolt of bolts) bolt.visible = Math.random() < 0.2;
      mario.rotation.y = 0;
      parts.armL.rotation.z = 2.9;
      parts.armR.rotation.z = -2.9;
      mario.position.y = Math.abs(Math.sin(t * 6)) * 0.25;
    } else if (t < 23) {
      // runs to the hole
      const k = ease((t - 21.5) / 1.5);
      mario.position.x = lerp(7.5, 0.5, k);
      mario.position.z = lerp(4, 7.2, k);
      mario.rotation.y = -Math.PI / 2 - 0.4;
      parts.armL.rotation.z = 0.5;
      parts.armR.rotation.z = -0.5;
      for (const bolt of bolts) bolt.visible = Math.random() < 0.15;
      runBob(t, 13);
    } else if (t < 24.3) {
      // leaps into the hole
      const k = (t - 23) / 1.3;
      mario.position.x = lerp(0.5, 0, k);
      mario.position.z = lerp(7.2, 8, k);
      mario.position.y = Math.sin(k * Math.PI * 0.8) * 4 - k * k * 6;
      mario.rotation.x = k * 1.2;
      const shrink = k > 0.5 ? 1.18 * (1 - (k - 0.5) * 1.4) : 1.18;
      mario.scale.setScalar(Math.max(0.2, shrink));
      for (const bolt of bolts) bolt.visible = false;
    } else {
      mario.visible = false;
    }

    // block break + star
    if (t >= 10.9 && !debrisFly) {
      debrisFly = true;
      block.visible = false;
      star.visible = true;
      for (const d of debris) {
        d.mesh.visible = true;
        d.mesh.position.copy(block.position);
      }
    }
    if (debrisFly) {
      for (const d of debris) {
        if (!d.mesh.visible) continue;
        d.vel.y -= 9.8 * dt;
        d.mesh.position.addScaledVector(d.vel, dt);
        d.mesh.rotation.x += d.spin * dt;
        d.mesh.rotation.z += d.spin * dt;
        if (d.mesh.position.y < 0) d.mesh.visible = false;
      }
    }
    if (star.visible) {
      star.rotation.y = t * 4;
      if (t < 12.2) {
        const k = (t - 10.9) / 1.3;
        star.position.y = 4.4 + Math.sin(k * Math.PI) * 1.4 - k * 3.2;
        star.position.x = 6 + k * 1.5;
      } else if (t < 14.2) {
        star.position.x = 7.5;
        star.position.y = 0.9 + Math.abs(Math.sin((t - 12.2) * 5)) * 0.4;
      } else {
        star.visible = false; // absorbed
      }
    }

    renderer.render(scene, camera);

    if (t >= LENGTH) {
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
