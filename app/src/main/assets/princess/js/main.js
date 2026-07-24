import { initAudio } from './audio.js';
import { playIntro } from './intro.js';
import { startGame } from './game.js';
import { playMarioIntro } from './mario-intro.js';
import { playMarioDash } from './mario-dash.js';
import { startBoss } from './boss.js';
import { playRescue, playCelebration } from './finale.js';

const $ = (id) => document.getElementById(id);
const screens = {
  title: $('title-screen'),
  intro: $('intro-screen'),
  game: $('game-screen'),
  end: $('end-screen'),
};
const fader = $('fader');

if ('ontouchstart' in window || navigator.maxTouchPoints > 0) {
  document.body.classList.add('touch');
}

function show(name) {
  for (const key of Object.keys(screens)) {
    screens[key].classList.toggle('hidden', key !== name);
  }
}

function fadeTo(fn) {
  fader.classList.add('on');
  setTimeout(() => {
    fn();
    setTimeout(() => fader.classList.remove('on'), 100);
  }, 650);
}

let cutscene = null; // whatever is currently skippable
let game = null;     // whatever currently owns the game canvas / input

function cleanupGame() {
  if (game) { game.destroy(); game = null; }
}

// ---------- story flow ----------
// title → park attack (3D) → escape (2D) → Mario arrives (3D) → Mario's dash (2D)
// → boss fight (2D) → rescue (2D) → celebration (3D) → THE END

function runIntro() {
  show('intro');
  cutscene = playIntro($('intro-canvas'), $('caption'), () => {
    cutscene = null;
    fadeTo(runGame);
  });
}

function runGame() {
  show('game');
  const onWin = () => fadeTo(() => { cleanupGame(); runMarioIntro(); });
  const onLose = () => fadeTo(() => { cleanupGame(); show('title'); });
  game = startGame($('game-canvas'), $('hud-warning'), onWin, onLose);
}

function runMarioIntro() {
  show('intro');
  cutscene = playMarioIntro($('intro-canvas'), $('caption'), () => {
    cutscene = null;
    fadeTo(runMarioDash);
  });
}

function runMarioDash() {
  show('game');
  game = playMarioDash($('game-canvas'), () => {
    fadeTo(() => { cleanupGame(); runBoss(); });
  });
  cutscene = game;
}

function runBoss() {
  cutscene = null;
  show('game');
  game = startBoss($('game-canvas'), $('hud-warning'), () => {
    fadeTo(() => { cleanupGame(); runRescue(); });
  });
}

function runRescue() {
  show('game');
  game = playRescue($('game-canvas'), () => {
    fadeTo(() => { cleanupGame(); runCelebration(); });
  });
  cutscene = game;
}

function runCelebration() {
  cutscene = null;
  show('intro');
  cutscene = playCelebration($('intro-canvas'), $('caption'), () => {
    cutscene = null;
    fadeTo(() => show('end'));
  });
}

$('start-btn').addEventListener('click', () => {
  initAudio();
  fadeTo(runIntro);
});

$('skip-btn').addEventListener('click', () => {
  if (cutscene && cutscene.skip) cutscene.skip();
});

$('replay-btn').addEventListener('click', () => {
  fadeTo(() => show('title'));
});

if (location.search.includes('debug')) {
  window.__flow = {
    intro: () => fadeTo(() => { cleanupGame(); runIntro(); }),
    game: () => fadeTo(() => { cleanupGame(); runGame(); }),
    marioIntro: () => fadeTo(() => { cleanupGame(); runMarioIntro(); }),
    dash: () => fadeTo(() => { cleanupGame(); runMarioDash(); }),
    boss: () => fadeTo(() => { cleanupGame(); runBoss(); }),
    rescue: () => fadeTo(() => { cleanupGame(); runRescue(); }),
    celebration: () => fadeTo(() => { cleanupGame(); runCelebration(); }),
    skip: () => { if (cutscene && cutscene.skip) cutscene.skip(); },
  };
}
