// Tiny WebAudio synth — all sounds generated in code, no audio files needed.
let ctx = null;

export function initAudio() {
  if (!ctx) {
    ctx = new (window.AudioContext || window.webkitAudioContext)();
  }
  if (ctx.state === 'suspended') ctx.resume();
}

function now() { return ctx ? ctx.currentTime : 0; }

function tone({ freq = 440, end = freq, dur = 0.2, type = 'square', vol = 0.15, delay = 0 }) {
  if (!ctx) return;
  const t = now() + delay;
  const osc = ctx.createOscillator();
  const gain = ctx.createGain();
  osc.type = type;
  osc.frequency.setValueAtTime(freq, t);
  osc.frequency.exponentialRampToValueAtTime(Math.max(end, 1), t + dur);
  gain.gain.setValueAtTime(vol, t);
  gain.gain.exponentialRampToValueAtTime(0.001, t + dur);
  osc.connect(gain).connect(ctx.destination);
  osc.start(t);
  osc.stop(t + dur + 0.05);
}

function noise({ dur = 0.3, vol = 0.2, delay = 0, low = false }) {
  if (!ctx) return;
  const t = now() + delay;
  const len = Math.floor(ctx.sampleRate * dur);
  const buf = ctx.createBuffer(1, len, ctx.sampleRate);
  const data = buf.getChannelData(0);
  for (let i = 0; i < len; i++) data[i] = Math.random() * 2 - 1;
  const src = ctx.createBufferSource();
  src.buffer = buf;
  const gain = ctx.createGain();
  gain.gain.setValueAtTime(vol, t);
  gain.gain.exponentialRampToValueAtTime(0.001, t + dur);
  let node = src;
  if (low) {
    const filter = ctx.createBiquadFilter();
    filter.type = 'lowpass';
    filter.frequency.value = 300;
    src.connect(filter);
    node = filter;
  }
  node.connect(gain).connect(ctx.destination);
  src.start(t);
}

export const sfx = {
  jump()      { tone({ freq: 300, end: 700, dur: 0.18, type: 'square', vol: 0.12 }); },
  coin()      { tone({ freq: 900, end: 1400, dur: 0.12, type: 'triangle', vol: 0.15 }); },
  hurt()      { tone({ freq: 300, end: 80, dur: 0.35, type: 'sawtooth', vol: 0.18 }); },
  roar() {
    tone({ freq: 90, end: 40, dur: 1.2, type: 'sawtooth', vol: 0.3 });
    tone({ freq: 130, end: 55, dur: 1.2, type: 'square', vol: 0.15 });
    noise({ dur: 1.1, vol: 0.22, low: true });
  },
  laugh() {
    // "HA HA HA" — three descending grunts
    for (let i = 0; i < 3; i++) {
      tone({ freq: 160 - i * 20, end: 70, dur: 0.28, type: 'sawtooth', vol: 0.28, delay: i * 0.38 });
      noise({ dur: 0.15, vol: 0.1, low: true, delay: i * 0.38 });
    }
  },
  rumble()    { noise({ dur: 1.6, vol: 0.28, low: true }); },
  scream() {
    for (let i = 0; i < 4; i++) {
      tone({ freq: 800 + Math.random() * 400, end: 1400, dur: 0.3, type: 'triangle', vol: 0.07, delay: i * 0.25 });
    }
  },
  fall()      { tone({ freq: 800, end: 100, dur: 1.0, type: 'triangle', vol: 0.15 }); },
  ring() {
    for (let i = 0; i < 2; i++) {
      tone({ freq: 1200, end: 1200, dur: 0.08, type: 'sine', vol: 0.15, delay: i * 0.7 });
      tone({ freq: 1500, end: 1500, dur: 0.08, type: 'sine', vol: 0.15, delay: i * 0.7 + 0.12 });
      tone({ freq: 1200, end: 1200, dur: 0.08, type: 'sine', vol: 0.15, delay: i * 0.7 + 0.24 });
    }
  },
  victory() {
    const notes = [523, 659, 784, 1047];
    notes.forEach((f, i) => tone({ freq: f, end: f, dur: 0.22, type: 'triangle', vol: 0.16, delay: i * 0.16 }));
  },
  smash() {
    noise({ dur: 0.25, vol: 0.3 });
    tone({ freq: 200, end: 60, dur: 0.25, type: 'square', vol: 0.2 });
  },
  powerup() {
    const notes = [392, 523, 659, 784, 1047, 1319];
    notes.forEach((f, i) => tone({ freq: f, end: f, dur: 0.12, type: 'square', vol: 0.14, delay: i * 0.09 }));
  },
  yell() {
    tone({ freq: 220, end: 520, dur: 0.5, type: 'sawtooth', vol: 0.22 });
    tone({ freq: 330, end: 660, dur: 0.5, type: 'square', vol: 0.1, delay: 0.05 });
  },
  stomp()  { tone({ freq: 220, end: 90, dur: 0.15, type: 'square', vol: 0.2 }); },
  kick()   { tone({ freq: 500, end: 1100, dur: 0.1, type: 'square', vol: 0.16 }); },
  shrink() { tone({ freq: 700, end: 150, dur: 0.5, type: 'square', vol: 0.15 }); },
  grow()   { tone({ freq: 200, end: 800, dur: 0.4, type: 'square', vol: 0.15 }); },
  oneup() {
    const notes = [659, 784, 1319, 1047, 1175, 1568];
    notes.forEach((f, i) => tone({ freq: f, end: f, dur: 0.13, type: 'triangle', vol: 0.15, delay: i * 0.11 }));
  },
  alarm() {
    for (let i = 0; i < 3; i++) tone({ freq: 700, end: 400, dur: 0.22, type: 'sawtooth', vol: 0.14, delay: i * 0.26 });
  },
  cheer() {
    for (let i = 0; i < 8; i++) {
      tone({ freq: 600 + Math.random() * 700, end: 900 + Math.random() * 600, dur: 0.35,
             type: 'triangle', vol: 0.06, delay: Math.random() * 1.2 });
    }
  },
};
