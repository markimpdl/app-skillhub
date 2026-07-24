// Shared Three.js helpers: the park set and all 3D characters, used by the
// intro cutscene, Mario's arrival cutscene and the celebration finale.
import * as THREE from 'three';

export function mat(color) {
  return new THREE.MeshLambertMaterial({ color });
}

// One WebGL renderer per canvas, reused across cutscenes.
export function getRenderer(canvas) {
  if (!canvas.__renderer) {
    const renderer = new THREE.WebGLRenderer({ canvas, antialias: true });
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    canvas.__renderer = renderer;
  }
  canvas.__renderer.setSize(window.innerWidth, window.innerHeight);
  return canvas.__renderer;
}

export function disposeScene(scene) {
  scene.traverse(obj => {
    if (obj.geometry) obj.geometry.dispose();
    if (obj.material) {
      if (Array.isArray(obj.material)) obj.material.forEach(m => m.dispose());
      else obj.material.dispose();
    }
  });
}

// ---------- characters ----------

export function buildKid(color) {
  const g = new THREE.Group();
  const body = new THREE.Mesh(new THREE.CapsuleGeometry(0.28, 0.5, 4, 8), mat(color));
  body.position.y = 0.65;
  const head = new THREE.Mesh(new THREE.SphereGeometry(0.26, 12, 12), mat(0xffd9b3));
  head.position.y = 1.3;
  const armL = new THREE.Mesh(new THREE.CapsuleGeometry(0.08, 0.35, 4, 6), mat(color));
  armL.position.set(-0.38, 0.85, 0);
  armL.rotation.z = 0.5;
  const armR = armL.clone();
  armR.position.x = 0.38;
  armR.rotation.z = -0.5;
  g.add(body, head, armL, armR);
  g.userData.arms = [armL, armR];
  return g;
}

export function buildPeach() {
  const g = new THREE.Group();
  const dress = new THREE.Mesh(new THREE.ConeGeometry(0.75, 1.7, 20), mat(0xff8fc7));
  dress.position.y = 0.85;
  const torso = new THREE.Mesh(new THREE.CapsuleGeometry(0.26, 0.4, 4, 10), mat(0xffb0d8));
  torso.position.y = 1.85;
  const head = new THREE.Mesh(new THREE.SphereGeometry(0.34, 14, 14), mat(0xffe0c2));
  head.position.y = 2.5;
  const hair = new THREE.Mesh(new THREE.SphereGeometry(0.38, 14, 14), mat(0xffd23e));
  hair.position.set(0, 2.58, -0.12);
  hair.scale.set(1, 1.05, 1);
  const crown = new THREE.Mesh(new THREE.CylinderGeometry(0.18, 0.22, 0.18, 8), mat(0xffe94d));
  crown.position.y = 2.95;
  const jewel = new THREE.Mesh(new THREE.SphereGeometry(0.06, 8, 8), mat(0xe0334f));
  jewel.position.set(0, 2.95, 0.2);
  const armL = new THREE.Mesh(new THREE.CapsuleGeometry(0.09, 0.45, 4, 6), mat(0xffb0d8));
  armL.position.set(-0.42, 1.9, 0);
  armL.rotation.z = 0.45;
  const armR = armL.clone();
  armR.position.x = 0.42;
  armR.rotation.z = -0.45;
  g.add(dress, torso, head, hair, crown, jewel, armL, armR);
  return g;
}

export function buildToad() {
  const g = new THREE.Group();
  const body = new THREE.Mesh(new THREE.CapsuleGeometry(0.3, 0.35, 4, 10), mat(0xffffff));
  body.position.y = 0.55;
  const vest = new THREE.Mesh(new THREE.CapsuleGeometry(0.32, 0.2, 4, 10), mat(0x3b6fd4));
  vest.position.y = 0.6;
  const head = new THREE.Mesh(new THREE.SphereGeometry(0.26, 12, 12), mat(0xffe0c2));
  head.position.y = 1.05;
  const cap = new THREE.Mesh(new THREE.SphereGeometry(0.5, 16, 16), mat(0xfffbf2));
  cap.position.y = 1.28;
  cap.scale.set(1, 0.75, 1);
  g.add(body, vest, head, cap);
  const spotGeo = new THREE.SphereGeometry(0.13, 8, 8);
  const spots = [
    [0, 1.62, 0.05], [0.36, 1.38, 0.28], [-0.36, 1.38, 0.28],
    [0.3, 1.4, -0.32], [-0.3, 1.4, -0.32],
  ];
  for (const [x, y, z] of spots) {
    const s = new THREE.Mesh(spotGeo, mat(0xe0334f));
    s.position.set(x, y, z);
    g.add(s);
  }
  return g;
}

export function buildMario() {
  const g = new THREE.Group();
  const skin = 0xffd9b3, red = 0xe02929, blue = 0x2c56c9, brown = 0x6d3b12, dark = 0x4a2c14;
  // shoes
  const shoeGeo = new THREE.SphereGeometry(0.22, 10, 10);
  const shoeL = new THREE.Mesh(shoeGeo, mat(brown));
  shoeL.position.set(-0.22, 0.14, 0.06);
  shoeL.scale.set(1, 0.7, 1.4);
  const shoeR = shoeL.clone();
  shoeR.position.x = 0.22;
  // legs (blue)
  const legGeo = new THREE.CapsuleGeometry(0.13, 0.3, 4, 8);
  const legL = new THREE.Mesh(legGeo, mat(blue));
  legL.position.set(-0.2, 0.5, 0);
  const legR = legL.clone();
  legR.position.x = 0.2;
  // body: overalls
  const body = new THREE.Mesh(new THREE.CapsuleGeometry(0.4, 0.4, 4, 12), mat(blue));
  body.position.y = 1.0;
  // red chest/shoulders
  const chest = new THREE.Mesh(new THREE.CapsuleGeometry(0.36, 0.2, 4, 12), mat(red));
  chest.position.y = 1.3;
  // arms (red) + gloves
  const armGeo = new THREE.CapsuleGeometry(0.11, 0.4, 4, 8);
  const armL = new THREE.Mesh(armGeo, mat(red));
  armL.position.set(-0.55, 1.2, 0);
  armL.rotation.z = 0.5;
  const armR = armL.clone();
  armR.position.x = 0.55;
  armR.rotation.z = -0.5;
  const gloveGeo = new THREE.SphereGeometry(0.14, 8, 8);
  const gloveL = new THREE.Mesh(gloveGeo, mat(0xffffff));
  gloveL.position.set(-0.72, 0.95, 0);
  const gloveR = gloveL.clone();
  gloveR.position.x = 0.72;
  // head
  const head = new THREE.Mesh(new THREE.SphereGeometry(0.34, 14, 14), mat(skin));
  head.position.y = 1.85;
  // nose
  const nose = new THREE.Mesh(new THREE.SphereGeometry(0.11, 8, 8), mat(0xffc599));
  nose.position.set(0, 1.83, 0.34);
  // mustache
  const stache = new THREE.Mesh(new THREE.BoxGeometry(0.3, 0.09, 0.1), mat(dark));
  stache.position.set(0, 1.72, 0.31);
  // eyes
  const eyeGeo = new THREE.SphereGeometry(0.045, 8, 8);
  const eyeL = new THREE.Mesh(eyeGeo, mat(0x1b3fa0));
  eyeL.position.set(-0.11, 1.93, 0.3);
  const eyeR = eyeL.clone();
  eyeR.position.x = 0.11;
  // cap + brim + emblem
  const cap = new THREE.Mesh(new THREE.SphereGeometry(0.36, 14, 14), mat(red));
  cap.position.set(0, 2.0, -0.03);
  cap.scale.set(1, 0.72, 1);
  const brim = new THREE.Mesh(new THREE.CylinderGeometry(0.3, 0.3, 0.05, 12, 1, false, -0.9, 1.8), mat(red));
  brim.position.set(0, 2.02, 0.14);
  const emblem = new THREE.Mesh(new THREE.CylinderGeometry(0.09, 0.09, 0.02, 10), mat(0xffffff));
  emblem.rotation.x = Math.PI / 2 - 0.4;
  emblem.position.set(0, 2.14, 0.3);
  g.add(shoeL, shoeR, legL, legR, body, chest, armL, armR, gloveL, gloveR,
        head, nose, stache, eyeL, eyeR, cap, brim, emblem);
  g.userData.arms = [armL, armR];
  g.userData.parts = { body, chest, cap, brim, legL, legR, armL, armR };
  return g;
}

export function buildScaryYoshi() {
  const g = new THREE.Group();
  const green = 0x2e7d32;
  const belly = 0xf3f0d8;

  const body = new THREE.Mesh(new THREE.SphereGeometry(2.6, 20, 20), mat(green));
  body.position.y = 3.0;
  body.scale.set(1, 1.15, 0.95);
  const bellyM = new THREE.Mesh(new THREE.SphereGeometry(2.1, 20, 20), mat(belly));
  bellyM.position.set(0, 2.7, 0.85);
  bellyM.scale.set(0.85, 1, 0.7);

  const head = new THREE.Mesh(new THREE.SphereGeometry(1.7, 20, 20), mat(green));
  head.position.set(0, 6.6, 0.4);
  const snout = new THREE.Mesh(new THREE.SphereGeometry(1.25, 18, 18), mat(green));
  snout.position.set(0, 6.1, 1.8);
  snout.scale.set(1, 0.8, 1.15);
  const nostrilGeo = new THREE.SphereGeometry(0.16, 8, 8);
  const nosL = new THREE.Mesh(nostrilGeo, mat(0x14380f));
  nosL.position.set(-0.5, 6.35, 2.9);
  const nosR = nosL.clone();
  nosR.position.x = 0.5;

  const eyeGeo = new THREE.SphereGeometry(0.55, 14, 14);
  const eyeL = new THREE.Mesh(eyeGeo, mat(0xffffff));
  eyeL.position.set(-0.62, 7.6, 1.1);
  const eyeR = eyeL.clone();
  eyeR.position.x = 0.62;
  const pupilGeo = new THREE.SphereGeometry(0.22, 10, 10);
  const pupL = new THREE.Mesh(pupilGeo, mat(0xd50000));
  pupL.position.set(-0.58, 7.55, 1.6);
  const pupR = pupL.clone();
  pupR.position.x = 0.58;
  const browGeo = new THREE.BoxGeometry(0.9, 0.18, 0.25);
  const browL = new THREE.Mesh(browGeo, mat(0x14380f));
  browL.position.set(-0.62, 8.15, 1.35);
  browL.rotation.z = -0.45;
  const browR = new THREE.Mesh(browGeo, mat(0x14380f));
  browR.position.set(0.62, 8.15, 1.35);
  browR.rotation.z = 0.45;

  const mouth = new THREE.Mesh(new THREE.SphereGeometry(0.75, 12, 12), mat(0x5c0a0a));
  mouth.position.set(0, 5.55, 2.45);
  mouth.scale.set(1.15, 0.55, 0.6);
  const toothGeo = new THREE.ConeGeometry(0.14, 0.4, 6);
  for (let i = -2; i <= 2; i++) {
    const tooth = new THREE.Mesh(toothGeo, mat(0xffffff));
    tooth.position.set(i * 0.32, 5.85, 2.85);
    tooth.rotation.x = Math.PI;
    g.add(tooth);
  }

  const shell = new THREE.Mesh(new THREE.SphereGeometry(1.9, 16, 16), mat(0xc62828));
  shell.position.set(0, 3.6, -1.6);
  shell.scale.set(1, 0.9, 0.7);
  const spikeGeo = new THREE.ConeGeometry(0.35, 1.0, 8);
  for (let i = 0; i < 4; i++) {
    const spike = new THREE.Mesh(spikeGeo, mat(0xfff3e0));
    const a = -0.7 + i * 0.5;
    spike.position.set(0, 3.7 + Math.cos(a) * 1.9, -1.6 - Math.sin(a) * 1.4);
    spike.rotation.x = -a - 0.4;
    g.add(spike);
  }

  const armGeo = new THREE.CapsuleGeometry(0.42, 1.6, 4, 10);
  const armL = new THREE.Mesh(armGeo, mat(green));
  armL.position.set(-2.5, 3.9, 0.4);
  armL.rotation.z = 0.8;
  const armR = armL.clone();
  armR.position.x = 2.5;
  armR.rotation.z = -0.8;
  const legGeo = new THREE.CapsuleGeometry(0.6, 1.0, 4, 10);
  const legL = new THREE.Mesh(legGeo, mat(0xef6c00));
  legL.position.set(-1.1, 0.9, 0.2);
  const legR = legL.clone();
  legR.position.x = 1.1;
  const tail = new THREE.Mesh(new THREE.ConeGeometry(0.7, 2.4, 10), mat(green));
  tail.position.set(0, 2.2, -2.6);
  tail.rotation.x = 1.9;

  g.add(body, bellyM, head, snout, nosL, nosR, eyeL, eyeR, pupL, pupR,
        browL, browR, mouth, shell, armL, armR, legL, legR, tail);
  g.userData.arms = [armL, armR];
  return g;
}

// ---------- props ----------

export function buildTree(scale = 1) {
  const g = new THREE.Group();
  const trunk = new THREE.Mesh(new THREE.CylinderGeometry(0.35, 0.5, 3, 8), mat(0x795548));
  trunk.position.y = 1.5;
  const leaves1 = new THREE.Mesh(new THREE.SphereGeometry(1.9, 12, 12), mat(0x43a047));
  leaves1.position.y = 4.2;
  const leaves2 = new THREE.Mesh(new THREE.SphereGeometry(1.4, 12, 12), mat(0x4caf50));
  leaves2.position.set(1.0, 3.4, 0.4);
  const leaves3 = new THREE.Mesh(new THREE.SphereGeometry(1.3, 12, 12), mat(0x388e3c));
  leaves3.position.set(-0.9, 3.5, -0.3);
  g.add(trunk, leaves1, leaves2, leaves3);
  g.scale.setScalar(scale);
  return g;
}

export function buildPlayground() {
  const g = new THREE.Group();

  const slide = new THREE.Group();
  const ramp = new THREE.Mesh(new THREE.BoxGeometry(1.2, 0.15, 4.2), mat(0xffca28));
  ramp.position.set(0, 1.35, 1.2);
  ramp.rotation.x = 0.55;
  const tower = new THREE.Mesh(new THREE.BoxGeometry(1.4, 2.4, 1.4), mat(0xef5350));
  tower.position.set(0, 1.2, -1.2);
  const ladder = new THREE.Mesh(new THREE.BoxGeometry(0.9, 0.1, 2.6), mat(0x8d6e63));
  ladder.position.set(0, 1.2, -2.6);
  ladder.rotation.x = -0.95;
  slide.add(ramp, tower, ladder);
  slide.position.set(-8, 0, 2);
  slide.rotation.y = 0.5;

  const swing = new THREE.Group();
  const barMat = mat(0x1e88e5);
  const top = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.12, 5, 8), barMat);
  top.rotation.z = Math.PI / 2;
  top.position.y = 3;
  swing.add(top);
  for (const sx of [-2.4, 2.4]) {
    for (const sz of [-0.8, 0.8]) {
      const pole = new THREE.Mesh(new THREE.CylinderGeometry(0.1, 0.1, 3.3, 8), barMat);
      pole.position.set(sx, 1.6, sz);
      pole.rotation.x = sz > 0 ? -0.25 : 0.25;
      swing.add(pole);
    }
  }
  const seats = [];
  for (const sx of [-1.1, 1.1]) {
    const pivot = new THREE.Group();
    pivot.position.set(sx, 3, 0);
    const ropeGeo = new THREE.CylinderGeometry(0.03, 0.03, 2.1, 6);
    const ropeL = new THREE.Mesh(ropeGeo, mat(0x9e9e9e));
    ropeL.position.set(-0.3, -1.05, 0);
    const ropeR = ropeL.clone();
    ropeR.position.x = 0.3;
    const seat = new THREE.Mesh(new THREE.BoxGeometry(0.8, 0.1, 0.4), mat(0x6d4c41));
    seat.position.y = -2.1;
    pivot.add(ropeL, ropeR, seat);
    swing.add(pivot);
    seats.push(pivot);
  }
  swing.position.set(9, 0, -1);
  swing.rotation.y = -0.4;
  swing.userData.seats = seats;

  const seesaw = new THREE.Group();
  const base = new THREE.Mesh(new THREE.ConeGeometry(0.5, 0.9, 10), mat(0x7b1fa2));
  base.position.y = 0.45;
  const plank = new THREE.Mesh(new THREE.BoxGeometry(4.4, 0.14, 0.6), mat(0xffa726));
  plank.position.y = 0.9;
  seesaw.add(base, plank);
  seesaw.position.set(3, 0, -4);
  seesaw.userData.plank = plank;

  g.add(slide, swing, seesaw);
  g.userData.swing = swing;
  g.userData.seesaw = seesaw;
  return g;
}

export function buildQuestionBlock() {
  const g = new THREE.Group();
  const box = new THREE.Mesh(new THREE.BoxGeometry(1.6, 1.6, 1.6), mat(0xffb300));
  g.add(box);
  // "?" hint: white dots on each visible face
  const dotGeo = new THREE.SphereGeometry(0.14, 8, 8);
  for (const side of [[0, 0, 0.81], [0.81, 0, 0], [-0.81, 0, 0]]) {
    const dot = new THREE.Mesh(dotGeo, mat(0xffffff));
    dot.position.set(...side);
    g.add(dot);
    const dot2 = new THREE.Mesh(dotGeo, mat(0xffffff));
    dot2.position.set(side[0] * 1, 0.42, side[2] * 1);
    dot2.scale.setScalar(0.8);
    g.add(dot2);
  }
  g.userData.box = box;
  return g;
}

export function buildStar() {
  const shape = new THREE.Shape();
  const R = 0.6, r = 0.26;
  for (let i = 0; i < 10; i++) {
    const rad = i % 2 === 0 ? R : r;
    const a = (i / 10) * Math.PI * 2 - Math.PI / 2;
    const px = Math.cos(a) * rad, py = -Math.sin(a) * rad;
    if (i === 0) shape.moveTo(px, py);
    else shape.lineTo(px, py);
  }
  const geo = new THREE.ExtrudeGeometry(shape, { depth: 0.2, bevelEnabled: false });
  const star = new THREE.Mesh(geo, new THREE.MeshBasicMaterial({ color: 0xffe94d }));
  return star;
}

export function buildHole() {
  const g = new THREE.Group();
  const hole = new THREE.Mesh(new THREE.CircleGeometry(3.4, 32),
    new THREE.MeshBasicMaterial({ color: 0x0a0508 }));
  hole.rotation.x = -Math.PI / 2;
  hole.position.y = 0.03;
  const rim = new THREE.Mesh(new THREE.TorusGeometry(3.4, 0.35, 8, 32), mat(0x5d4037));
  rim.rotation.x = -Math.PI / 2;
  rim.position.y = 0.05;
  g.add(hole, rim);
  return g;
}

// Adds sky, fog, lights, ground, path, trees, playground and clouds to a scene.
// Returns refs used by cutscene animations.
export function buildParkSet(scene) {
  scene.background = new THREE.Color(0x8ec5ff);
  scene.fog = new THREE.Fog(0x8ec5ff, 45, 90);

  scene.add(new THREE.HemisphereLight(0xcfe8ff, 0x76c47a, 0.9));
  const sun = new THREE.DirectionalLight(0xfff3d6, 1.4);
  sun.position.set(20, 30, 15);
  scene.add(sun);

  const ground = new THREE.Mesh(new THREE.CircleGeometry(70, 48), mat(0x66bb6a));
  ground.rotation.x = -Math.PI / 2;
  scene.add(ground);
  const path = new THREE.Mesh(new THREE.RingGeometry(12, 14.5, 40), mat(0xd7c49e));
  path.rotation.x = -Math.PI / 2;
  path.position.y = 0.01;
  scene.add(path);

  const treeSpots = [
    [-22, -20], [-15, -23], [-7, -21], [0, -24], [7, -22], [14, -23], [21, -20],
    [-26, -12], [26, -13], [-20, 12], [23, 10],
  ];
  for (const [x, z] of treeSpots) {
    const tr = buildTree(1 + ((x * 7 + z * 13) % 10) / 16);
    tr.position.set(x, 0, z);
    scene.add(tr);
  }

  const playground = buildPlayground();
  scene.add(playground);

  for (let i = 0; i < 6; i++) {
    const cloud = new THREE.Group();
    for (let j = 0; j < 3; j++) {
      const puff = new THREE.Mesh(new THREE.SphereGeometry(1.6 + (j % 2) * 0.7, 10, 10),
        new THREE.MeshLambertMaterial({ color: 0xffffff }));
      puff.position.set(j * 1.8 - 1.8, (j % 2) * 0.5, 0);
      cloud.add(puff);
    }
    cloud.position.set(-30 + i * 12, 18 + (i % 3) * 2, -30 + (i % 4) * 6);
    scene.add(cloud);
  }

  return { playground };
}

export function animatePlayground(playground, t) {
  const swing = playground.userData.swing;
  for (const seat of swing.userData.seats) seat.rotation.x = Math.sin(t * 2.2) * 0.45;
  playground.userData.seesaw.userData.plank.rotation.z = Math.sin(t * 1.8) * 0.22;
}
