// Canvas-2D character sprites, shared by the escape phase, Mario's dash and the boss fight.
// All draw functions take (x, y) = feet center.

export function drawPeach(ctx, x, y, facing, run, airborne) {
  // ~66 tall.
  ctx.save();
  ctx.translate(x, y);
  ctx.scale(facing, 1);
  const legSwing = airborne ? 0.6 : Math.sin(run) * 0.8;
  // legs
  ctx.strokeStyle = '#ffe0c2'; ctx.lineWidth = 6; ctx.lineCap = 'round';
  ctx.beginPath(); ctx.moveTo(-4, -22); ctx.lineTo(-4 + legSwing * 8, 0); ctx.stroke();
  ctx.beginPath(); ctx.moveTo(4, -22); ctx.lineTo(4 - legSwing * 8, 0); ctx.stroke();
  // dress
  ctx.fillStyle = '#ff8fc7';
  ctx.beginPath();
  ctx.moveTo(0, -46); ctx.lineTo(-17, -12); ctx.lineTo(17, -12); ctx.closePath();
  ctx.fill();
  ctx.fillStyle = '#ff6fb5';
  ctx.fillRect(-17, -14, 34, 5);
  // arms
  ctx.strokeStyle = '#ffb0d8'; ctx.lineWidth = 5;
  const armSwing = airborne ? -1.4 : Math.sin(run + Math.PI) * 0.7;
  ctx.beginPath(); ctx.moveTo(-8, -40); ctx.lineTo(-14 + armSwing * 6, -28); ctx.stroke();
  ctx.beginPath(); ctx.moveTo(8, -40); ctx.lineTo(14 - armSwing * 6, -28); ctx.stroke();
  // head
  ctx.fillStyle = '#ffe0c2';
  ctx.beginPath(); ctx.arc(0, -54, 9, 0, Math.PI * 2); ctx.fill();
  // hair
  ctx.fillStyle = '#ffd23e';
  ctx.beginPath(); ctx.arc(-3, -57, 9, 0.4, Math.PI * 1.5); ctx.fill();
  ctx.beginPath(); ctx.ellipse(-8, -46, 5, 10, 0.3, 0, Math.PI * 2); ctx.fill();
  // crown
  ctx.fillStyle = '#ffe94d';
  ctx.beginPath();
  ctx.moveTo(-6, -62); ctx.lineTo(-6, -68); ctx.lineTo(-2, -64); ctx.lineTo(1, -69);
  ctx.lineTo(4, -64); ctx.lineTo(7, -68); ctx.lineTo(7, -62); ctx.closePath();
  ctx.fill();
  // eye
  ctx.fillStyle = '#333';
  ctx.beginPath(); ctx.arc(4, -55, 1.6, 0, Math.PI * 2); ctx.fill();
  ctx.restore();
}

export function drawToad(ctx, x, y, facing, run, airborne) {
  ctx.save();
  ctx.translate(x, y);
  ctx.scale(facing, 1);
  const legSwing = airborne ? 0.5 : Math.sin(run) * 0.9;
  ctx.strokeStyle = '#fff'; ctx.lineWidth = 5; ctx.lineCap = 'round';
  ctx.beginPath(); ctx.moveTo(-3, -12); ctx.lineTo(-3 + legSwing * 6, 0); ctx.stroke();
  ctx.beginPath(); ctx.moveTo(3, -12); ctx.lineTo(3 - legSwing * 6, 0); ctx.stroke();
  // body / vest
  ctx.fillStyle = '#fff';
  ctx.fillRect(-8, -24, 16, 13);
  ctx.fillStyle = '#3b6fd4';
  ctx.fillRect(-8, -24, 16, 8);
  // head
  ctx.fillStyle = '#ffe0c2';
  ctx.beginPath(); ctx.arc(0, -28, 7, 0, Math.PI * 2); ctx.fill();
  // mushroom cap
  ctx.fillStyle = '#fffbf2';
  ctx.beginPath(); ctx.ellipse(0, -33, 13, 9, 0, Math.PI, 0); ctx.fill();
  ctx.fillStyle = '#e0334f';
  ctx.beginPath(); ctx.arc(0, -38, 3.5, 0, Math.PI * 2); ctx.fill();
  ctx.beginPath(); ctx.arc(-8, -34, 3, 0, Math.PI * 2); ctx.fill();
  ctx.beginPath(); ctx.arc(8, -34, 3, 0, Math.PI * 2); ctx.fill();
  // eye
  ctx.fillStyle = '#333';
  ctx.beginPath(); ctx.arc(3.5, -28, 1.4, 0, Math.PI * 2); ctx.fill();
  ctx.restore();
}

export function drawYoshi(ctx, x, y, t, opts = {}) {
  // Giant: ~230 tall at scale 1. Faces +x by default; pass facing: -1 to flip.
  const { facing = 1, scale = 1 } = opts;
  const stomp = Math.abs(Math.sin(t * 9)) * 12;
  ctx.save();
  ctx.translate(x, y - stomp * scale);
  ctx.scale(facing * scale, scale);
  // tail
  ctx.fillStyle = '#2e7d32';
  ctx.beginPath(); ctx.ellipse(-95, -60, 45, 22, -0.3, 0, Math.PI * 2); ctx.fill();
  // legs
  ctx.fillStyle = '#ef6c00';
  ctx.beginPath(); ctx.ellipse(-35, -18 + stomp * 0.6, 26, 20, 0, 0, Math.PI * 2); ctx.fill();
  ctx.beginPath(); ctx.ellipse(35, -18 + stomp * 0.3, 26, 20, 0, 0, Math.PI * 2); ctx.fill();
  // body
  ctx.fillStyle = '#2e7d32';
  ctx.beginPath(); ctx.ellipse(0, -95, 62, 70, 0, 0, Math.PI * 2); ctx.fill();
  // belly
  ctx.fillStyle = '#f3f0d8';
  ctx.beginPath(); ctx.ellipse(18, -85, 38, 48, 0, 0, Math.PI * 2); ctx.fill();
  // shell with spikes
  ctx.fillStyle = '#c62828';
  ctx.beginPath(); ctx.ellipse(-42, -120, 40, 34, 0, 0, Math.PI * 2); ctx.fill();
  ctx.fillStyle = '#fff3e0';
  for (let i = 0; i < 3; i++) {
    const a = Math.PI * 0.7 + i * 0.5;
    const sx = -42 + Math.cos(a) * 38, sy = -120 - Math.sin(a) * 32;
    ctx.beginPath();
    ctx.moveTo(sx - 8, sy + 4); ctx.lineTo(sx + Math.cos(a) * 20, sy - Math.sin(a) * 20); ctx.lineTo(sx + 8, sy + 4);
    ctx.closePath(); ctx.fill();
  }
  // head
  ctx.fillStyle = '#2e7d32';
  ctx.beginPath(); ctx.ellipse(35, -178, 42, 38, 0, 0, Math.PI * 2); ctx.fill();
  // snout
  ctx.beginPath(); ctx.ellipse(78, -168, 34, 24, 0, 0, Math.PI * 2); ctx.fill();
  // mouth + teeth
  ctx.fillStyle = '#5c0a0a';
  ctx.beginPath(); ctx.ellipse(85, -156, 24, 9, 0.1, 0, Math.PI * 2); ctx.fill();
  ctx.fillStyle = '#fff';
  for (let i = 0; i < 4; i++) {
    ctx.beginPath();
    ctx.moveTo(66 + i * 11, -160); ctx.lineTo(71 + i * 11, -149); ctx.lineTo(76 + i * 11, -160);
    ctx.closePath(); ctx.fill();
  }
  // eyes (white, angry red pupils)
  ctx.fillStyle = '#fff';
  ctx.beginPath(); ctx.ellipse(38, -206, 13, 16, 0, 0, Math.PI * 2); ctx.fill();
  ctx.beginPath(); ctx.ellipse(62, -204, 12, 15, 0, 0, Math.PI * 2); ctx.fill();
  ctx.fillStyle = '#d50000';
  ctx.beginPath(); ctx.arc(42, -202, 5.5, 0, Math.PI * 2); ctx.fill();
  ctx.beginPath(); ctx.arc(65, -200, 5, 0, Math.PI * 2); ctx.fill();
  // angry brows
  ctx.strokeStyle = '#14380f'; ctx.lineWidth = 7; ctx.lineCap = 'round';
  ctx.beginPath(); ctx.moveTo(26, -222); ctx.lineTo(48, -214); ctx.stroke();
  ctx.beginPath(); ctx.moveTo(78, -216); ctx.lineTo(56, -212); ctx.stroke();
  // arms reaching forward
  ctx.strokeStyle = '#2e7d32'; ctx.lineWidth = 16;
  const grab = Math.sin(t * 6) * 10;
  ctx.beginPath(); ctx.moveTo(40, -110); ctx.quadraticCurveTo(90, -120, 112, -100 + grab); ctx.stroke();
  ctx.restore();
}

export function drawMario(ctx, x, y, facing, run, airborne, opts = {}) {
  // Big: ~64 tall. Small: scaled to ~40.
  const { big = true, star = false, t = 0 } = opts;
  ctx.save();
  ctx.translate(x, y);
  if (star) {
    // rainbow star aura
    const hue = (t * 400) % 360;
    ctx.fillStyle = `hsla(${hue}, 100%, 60%, 0.35)`;
    ctx.beginPath(); ctx.arc(0, -32, 48, 0, Math.PI * 2); ctx.fill();
    for (let i = 0; i < 5; i++) {
      const a = t * 6 + i * (Math.PI * 2 / 5);
      const sx = Math.cos(a) * 42, sy = -32 + Math.sin(a) * 42;
      ctx.fillStyle = `hsla(${(hue + i * 60) % 360}, 100%, 70%, 0.9)`;
      ctx.beginPath();
      ctx.moveTo(sx, sy - 6); ctx.lineTo(sx + 5, sy + 4); ctx.lineTo(sx - 5, sy + 4);
      ctx.closePath(); ctx.fill();
    }
  }
  ctx.scale(facing * (big ? 1 : 0.62), big ? 1 : 0.62);
  const legSwing = airborne ? 0.7 : Math.sin(run) * 0.9;
  // legs (blue overalls)
  ctx.strokeStyle = '#2c56c9'; ctx.lineWidth = 8; ctx.lineCap = 'round';
  ctx.beginPath(); ctx.moveTo(-5, -20); ctx.lineTo(-5 + legSwing * 8, -3); ctx.stroke();
  ctx.beginPath(); ctx.moveTo(5, -20); ctx.lineTo(5 - legSwing * 8, -3); ctx.stroke();
  // shoes
  ctx.fillStyle = '#6d3b12';
  ctx.beginPath(); ctx.ellipse(-5 + legSwing * 8 + 3, -2, 7, 4.5, 0, 0, Math.PI * 2); ctx.fill();
  ctx.beginPath(); ctx.ellipse(5 - legSwing * 8 + 3, -2, 7, 4.5, 0, 0, Math.PI * 2); ctx.fill();
  // body: overalls
  ctx.fillStyle = '#2c56c9';
  ctx.beginPath(); ctx.roundRect(-12, -38, 24, 20, 6); ctx.fill();
  // red shirt (chest + shoulders)
  ctx.fillStyle = '#e02929';
  ctx.fillRect(-12, -42, 24, 8);
  // overall straps + buttons
  ctx.fillStyle = '#2c56c9';
  ctx.fillRect(-9, -42, 5, 8); ctx.fillRect(4, -42, 5, 8);
  ctx.fillStyle = '#ffd23e';
  ctx.beginPath(); ctx.arc(-6.5, -35, 1.8, 0, Math.PI * 2); ctx.fill();
  ctx.beginPath(); ctx.arc(6.5, -35, 1.8, 0, Math.PI * 2); ctx.fill();
  // arms (red) with white gloves
  const armSwing = airborne ? -1.6 : Math.sin(run + Math.PI) * 0.8;
  ctx.strokeStyle = '#e02929'; ctx.lineWidth = 6;
  ctx.beginPath(); ctx.moveTo(-10, -38); ctx.lineTo(-16 + armSwing * 5, -27); ctx.stroke();
  ctx.beginPath(); ctx.moveTo(10, -38); ctx.lineTo(16 - armSwing * 5, -27); ctx.stroke();
  ctx.fillStyle = '#fff';
  ctx.beginPath(); ctx.arc(-16 + armSwing * 5, -26, 4, 0, Math.PI * 2); ctx.fill();
  ctx.beginPath(); ctx.arc(16 - armSwing * 5, -26, 4, 0, Math.PI * 2); ctx.fill();
  // head
  ctx.fillStyle = '#ffd9b3';
  ctx.beginPath(); ctx.arc(1, -50, 10, 0, Math.PI * 2); ctx.fill();
  // ear + sideburn
  ctx.beginPath(); ctx.arc(-7, -50, 3.5, 0, Math.PI * 2); ctx.fill();
  ctx.fillStyle = '#4a2c14';
  ctx.beginPath(); ctx.ellipse(-6, -52, 3, 5, 0.4, 0, Math.PI * 2); ctx.fill();
  // nose
  ctx.fillStyle = '#ffc599';
  ctx.beginPath(); ctx.ellipse(11, -48, 4.5, 3.5, 0, 0, Math.PI * 2); ctx.fill();
  // mustache
  ctx.fillStyle = '#4a2c14';
  ctx.beginPath(); ctx.ellipse(8, -44.5, 6.5, 2.6, 0.15, 0, Math.PI * 2); ctx.fill();
  // eye
  ctx.fillStyle = '#1b3fa0';
  ctx.beginPath(); ctx.arc(6, -53, 1.9, 0, Math.PI * 2); ctx.fill();
  // red cap + brim + emblem
  ctx.fillStyle = '#e02929';
  ctx.beginPath(); ctx.arc(0, -56, 10.5, Math.PI, 0); ctx.fill();
  ctx.beginPath(); ctx.roundRect(2, -58, 14, 4, 2); ctx.fill();
  ctx.fillStyle = '#fff';
  ctx.beginPath(); ctx.arc(0, -60, 3.6, 0, Math.PI * 2); ctx.fill();
  ctx.fillStyle = '#e02929';
  ctx.font = 'bold 6px sans-serif';
  ctx.fillText('M', -2.4, -57.8);
  ctx.restore();
}

export function drawKoopa(ctx, x, y, facing, t) {
  // Green turtle, ~36 tall.
  ctx.save();
  ctx.translate(x, y);
  ctx.scale(facing, 1);
  const step = Math.sin(t * 8) * 3;
  // feet
  ctx.fillStyle = '#f9a825';
  ctx.beginPath(); ctx.ellipse(-7 + step, -3, 6, 4, 0, 0, Math.PI * 2); ctx.fill();
  ctx.beginPath(); ctx.ellipse(7 - step, -3, 6, 4, 0, 0, Math.PI * 2); ctx.fill();
  // shell
  ctx.fillStyle = '#2f9e44';
  ctx.beginPath(); ctx.ellipse(-2, -18, 15, 13, 0, 0, Math.PI * 2); ctx.fill();
  ctx.fillStyle = '#8ce99a';
  ctx.beginPath(); ctx.ellipse(-2, -13, 14, 5, 0, 0, Math.PI); ctx.fill();
  ctx.strokeStyle = '#1b6e2e'; ctx.lineWidth = 2;
  ctx.beginPath(); ctx.moveTo(-10, -20); ctx.lineTo(6, -20); ctx.stroke();
  ctx.beginPath(); ctx.moveTo(-8, -26); ctx.lineTo(4, -26); ctx.stroke();
  // head
  ctx.fillStyle = '#b2f2bb';
  ctx.beginPath(); ctx.ellipse(13, -28, 7, 8, 0.3, 0, Math.PI * 2); ctx.fill();
  ctx.fillStyle = '#333';
  ctx.beginPath(); ctx.arc(15, -30, 1.5, 0, Math.PI * 2); ctx.fill();
  ctx.restore();
}

export function drawShell(ctx, x, y, spinning, t) {
  // ~24 tall.
  ctx.save();
  ctx.translate(x, y);
  ctx.fillStyle = '#2f9e44';
  ctx.beginPath(); ctx.ellipse(0, -11, 15, 11, 0, 0, Math.PI * 2); ctx.fill();
  ctx.fillStyle = '#e8f5e9';
  ctx.beginPath(); ctx.ellipse(0, -6, 15, 5, 0, 0, Math.PI); ctx.fill();
  ctx.strokeStyle = '#1b6e2e'; ctx.lineWidth = 2;
  if (spinning) {
    const o = (t * 60) % 12 - 6;
    ctx.beginPath(); ctx.moveTo(-12 + o, -14); ctx.lineTo(12 + o, -14); ctx.stroke();
  } else {
    ctx.beginPath(); ctx.moveTo(-10, -14); ctx.lineTo(10, -14); ctx.stroke();
    ctx.beginPath(); ctx.moveTo(-7, -19); ctx.lineTo(7, -19); ctx.stroke();
  }
  ctx.restore();
}

export function drawMushroom(ctx, x, y, t) {
  // Red 1-up/grow mushroom, ~30 tall, wobbles.
  ctx.save();
  ctx.translate(x, y + Math.sin(t * 6) * 2);
  ctx.fillStyle = '#fff3d6';
  ctx.fillRect(-6, -14, 12, 14);
  ctx.fillStyle = '#e02929';
  ctx.beginPath(); ctx.ellipse(0, -17, 15, 11, 0, Math.PI, 0); ctx.fill();
  ctx.fillStyle = '#fff';
  ctx.beginPath(); ctx.arc(0, -24, 4, 0, Math.PI * 2); ctx.fill();
  ctx.beginPath(); ctx.arc(-10, -18, 3, 0, Math.PI * 2); ctx.fill();
  ctx.beginPath(); ctx.arc(10, -18, 3, 0, Math.PI * 2); ctx.fill();
  // eyes
  ctx.fillStyle = '#333';
  ctx.beginPath(); ctx.arc(-3, -8, 1.5, 0, Math.PI * 2); ctx.fill();
  ctx.beginPath(); ctx.arc(3, -8, 1.5, 0, Math.PI * 2); ctx.fill();
  ctx.restore();
}

export function drawPhone(ctx, x, y, t) {
  ctx.save();
  ctx.translate(x, y);
  const glow = 0.35 + Math.sin(t * 4) * 0.15;
  ctx.fillStyle = `rgba(255, 240, 130, ${glow})`;
  ctx.beginPath(); ctx.arc(0, -40, 55, 0, Math.PI * 2); ctx.fill();
  ctx.fillStyle = '#7a6552';
  ctx.fillRect(-16, -28, 32, 28);
  ctx.fillStyle = '#d13b3b';
  ctx.fillRect(-13, -62, 26, 36);
  ctx.fillStyle = '#8c1f1f';
  ctx.fillRect(-9, -56, 18, 12);
  const shake = Math.sin(t * 25) * 2;
  ctx.fillStyle = '#333';
  ctx.fillRect(-16 + shake, -72, 32, 8);
  ctx.restore();
}
