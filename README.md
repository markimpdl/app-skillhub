# Skills Camp — SkillHub Arcade

App Android (Kotlin + Jetpack Compose) usado como **totem arcade do Skills Camp (ACTVET)**: um único APK em modo quiosque (vertical, para crianças) reunindo 20 jogos — jogos nativos em Compose e **jogos HTML/JS criados pelos alunos** no vibe coding, rodando em WebView offline.

- APK pronto: [`SkillsCamp.apk`](SkillsCamp.apk)
- Pacote: `com.osaparecidos.memoriamaker`
- Jogos dos alunos ficam em `app/src/main/assets/<jogo>/` e aparecem na tela **Student Projects** (banner "Skills Camp · Student Projects / Vibe Coding" na home)

## Como buildar

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew :app:assembleDebug --no-daemon
# saída: app/build/outputs/apk/debug/app-debug.apk
```

Requisitos: Android SDK platform 36 + build-tools 36.1.0 (versões pinadas em `app/build.gradle.kts`).

## Como incluir novos jogos de alunos do Skills Camp

Para adicionar o jogo de um novo aluno, abra este projeto no **Claude Code** e cole o prompt abaixo, substituindo o link pelo repositório GitHub do jogo do aluno:

```text
Adicione um novo jogo de aluno do Skills Camp a este app.

Repositório do jogo: <LINK DO REPOSITÓRIO GIT DO ALUNO>
Nome do jogo: <NOME DO JOGO>
Nome do aluno: <NOME DO ALUNO>

Siga o padrão dos jogos de alunos já existentes (ex.: assets/princess + PrincessGame.kt):

1. Clone o repositório e copie o jogo (HTML/JS/assets) para app/src/main/assets/<slug-do-jogo>/.
2. Deixe o jogo 100% offline: baixe/vendorize qualquer dependência de CDN (three.js etc.)
   e aponte os imports para o arquivo local; remova crossorigin; DELETE qualquer sw.js e a
   sua registração (service workers quebram atualizações do APK).
3. Crie a tela do jogo em games/ usando o WebGameView existente (WebViewAssetLoader,
   origem https://appassets.androidplatform.net) e um *_INJECT de CSS/JS para adaptar ao
   quiosque: controles de toque flutuantes semi-transparentes (opacity ~0.55) na parte de
   baixo/meio da tela, nunca cobrindo menus; sem nenhum botão interativo no topo.
4. Registre o jogo no enum Game (em data/) incluindo-o no conjunto STUDENT, adicione a rota
   no AppNavHost e o card na StudentProjectsScreen com o nome do jogo e o crédito do aluno.
5. Cuidado com emoji: só use emoji antigos (≤ Emoji 1.0) ou SVG inline — fontes de Android
   antigos não renderizam emoji novos.
6. Builde o APK, rode no emulador (AVD Pixel_9_Pro_XL), tire screenshots do jogo abrindo e
   sendo jogado para confirmar que funciona, e copie o APK final para SkillsCamp.apk na raiz.
```

Depois é só instalar o novo `SkillsCamp.apk` no totem.

## Estrutura

- `app/src/main/java/.../nav/` — navegação (single-activity, Navigation-Compose)
- `app/src/main/java/.../data/` — enum `Game`, placares (`ScoreRepository`), níveis
- `app/src/main/java/.../ui/` — tema + kit compartilhado (`Chunky3D`, `GameScaffold`, `WebGameView`…)
- `app/src/main/java/.../games/` — jogos nativos em Compose
- `app/src/main/java/.../screens/` — Home, Student Projects, Victory, Ranking…
- `app/src/main/assets/` — jogos HTML/JS dos alunos (WebView)
