// Shared 2D level data + cave rendering, used by the phase-1 escape (game.js)
// and Mario's fast run-through (mario-dash.js).

export const WORLD_H = 600;       // logical units; canvas is scaled to fit
export const GROUND_Y = 520;      // top of the ground
export const LEVEL_END = 8250;

export const GROUNDS = [ // [x0, x1] ranges that have solid ground
  [-400, 900], [1020, 1800], [1940, 2600], [2760, 3600],
  [3740, 4600], [4780, 5600], [5750, 6800], [6960, 8250],
];
export const PLATFORMS = [ // floating platforms {x, y, w}
  { x: 880, y: 420, w: 180 }, { x: 1360, y: 400, w: 150 },
  { x: 1790, y: 430, w: 190 }, { x: 2260, y: 390, w: 150 },
  { x: 2580, y: 430, w: 220 }, { x: 3100, y: 400, w: 160 },
  { x: 3560, y: 430, w: 220 }, { x: 4000, y: 380, w: 150 },
  { x: 4560, y: 430, w: 260 }, { x: 5000, y: 390, w: 160 },
  { x: 5560, y: 430, w: 230 }, { x: 6200, y: 400, w: 170 },
  { x: 6780, y: 430, w: 220 }, { x: 7300, y: 400, w: 160 },
];
export const SPIKES = [ // {x, w} on the ground
  { x: 1500, w: 90 }, { x: 2300, w: 90 }, { x: 3200, w: 120 },
  { x: 4200, w: 90 }, { x: 5200, w: 120 }, { x: 6400, w: 90 }, { x: 7500, w: 90 },
];
export const CHECKPOINTS = [80, 2050, 4060, 5860];
export const PHONE_X = 8050;

export function groundAt(x) {
  for (const [x0, x1] of GROUNDS) if (x >= x0 && x <= x1) return GROUND_Y;
  return Infinity;
}

export function drawCaveBackground(ctx, camX, viewW, time) {
  const grad = ctx.createLinearGradient(0, 0, 0, WORLD_H);
  grad.addColorStop(0, '#160c22');
  grad.addColorStop(0.6, '#2b1740');
  grad.addColorStop(1, '#3d2158');
  ctx.fillStyle = grad;
  ctx.fillRect(0, 0, viewW, WORLD_H);

  // parallax stalactites
  ctx.save();
  ctx.translate(-camX * 0.4, 0);
  ctx.fillStyle = '#241335';
  for (let i = 0; i < 40; i++) {
    const sx = i * 260 + (i % 3) * 70;
    ctx.beginPath();
    ctx.moveTo(sx, 0); ctx.lineTo(sx + 45, 0); ctx.lineTo(sx + 22, 90 + (i % 4) * 35);
    ctx.closePath(); ctx.fill();
  }
  ctx.restore();
  // glowing crystals
  ctx.save();
  ctx.translate(-camX * 0.7, 0);
  for (let i = 0; i < 50; i++) {
    const cx = i * 210 + (i % 5) * 40;
    const glow = 0.5 + Math.sin(time * 2 + i) * 0.25;
    ctx.fillStyle = i % 2 ? `rgba(120, 220, 255, ${glow})` : `rgba(230, 130, 255, ${glow})`;
    ctx.beginPath();
    ctx.moveTo(cx, GROUND_Y); ctx.lineTo(cx + 9, GROUND_Y - 34); ctx.lineTo(cx + 18, GROUND_Y);
    ctx.closePath(); ctx.fill();
  }
  ctx.restore();
}

export function drawTerrain(ctx) {
  for (const [x0, x1] of GROUNDS) {
    ctx.fillStyle = '#54356b';
    ctx.fillRect(x0, GROUND_Y, x1 - x0, WORLD_H - GROUND_Y + 40);
    ctx.fillStyle = '#7e57a5';
    ctx.fillRect(x0, GROUND_Y, x1 - x0, 14);
  }
  for (const p of PLATFORMS) {
    ctx.fillStyle = '#8d6e63';
    ctx.fillRect(p.x, p.y, p.w, 16);
    ctx.fillStyle = '#a98274';
    ctx.fillRect(p.x, p.y, p.w, 5);
  }
}

export function drawSpikes(ctx, spikes) {
  ctx.fillStyle = '#cfd8dc';
  for (const s of spikes) {
    if (s.dead) continue;
    const n = Math.floor(s.w / 22);
    for (let i = 0; i < n; i++) {
      const sx = s.x + i * 22;
      ctx.beginPath();
      ctx.moveTo(sx, GROUND_Y); ctx.lineTo(sx + 11, GROUND_Y - 24); ctx.lineTo(sx + 22, GROUND_Y);
      ctx.closePath(); ctx.fill();
    }
  }
}
