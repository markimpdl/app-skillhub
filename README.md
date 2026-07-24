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

Siga o padrão dos jogos de alunos já existentes, salve o APK final como SkillsCamp.apk na raiz.
```

Depois é só instalar o novo `SkillsCamp.apk` no totem.

