package com.osaparecidos.memoriamaker.games.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.data.GameResult
import com.osaparecidos.memoriamaker.ui.components.ChunkyButton
import com.osaparecidos.memoriamaker.ui.components.GameControls
import com.osaparecidos.memoriamaker.ui.components.GameHud
import com.osaparecidos.memoriamaker.ui.components.GameScaffold
import com.osaparecidos.memoriamaker.ui.theme.AlertRed
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.Muted
import com.osaparecidos.memoriamaker.ui.theme.PaletteBlue
import com.osaparecidos.memoriamaker.ui.theme.PaletteGreen
import com.osaparecidos.memoriamaker.ui.theme.PaletteOrange
import com.osaparecidos.memoriamaker.ui.theme.PalettePink
import com.osaparecidos.memoriamaker.ui.theme.PalettePurple
import com.osaparecidos.memoriamaker.ui.theme.White
import kotlinx.coroutines.delay

private data class Q(val q: String, val answer: String, val wrong: List<String>)

/** A shuffled question: options in random order + the index of the correct one. */
private data class PreparedQ(val text: String, val options: List<String>, val correct: Int)

private const val TIME_LIMIT = 12          // seconds per question
private const val BASE_POINTS = 60          // points for a correct answer
private const val SPEED_POINTS = 40         // extra points, scaled by how fast you answer

// 4 distinct option colours (by position).
private val OPTION_COLORS = listOf(PaletteBlue.bg, PaletteOrange.bg, PaletteGreen.bg, PalettePink.bg)

@Composable
fun QuizGame(onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var count by remember { mutableStateOf<Int?>(null) }
    val n = count
    if (n == null) ModePicker(onExit) { count = it } else QuizRun(n, onExit, onFinish)
}

@Composable
private fun ModePicker(onExit: () -> Unit, onPick: (Int) -> Unit) {
    GameScaffold(title = "Skills Quiz", onBack = onExit, showBack = false) {
        Spacer(Modifier.weight(1f))
        Text("❓ Skills Quiz", color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 26.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text("Answer fast for more points!", color = Muted, fontFamily = Fredoka, fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))
        ChunkyButton("😊 EASY · 10 questions", onClick = { onPick(10) }, palette = PaletteGreen, modifier = Modifier.fillMaxWidth(), fontSize = 20.sp)
        Spacer(Modifier.height(16.dp))
        ChunkyButton("🔥 HARD · 20 questions", onClick = { onPick(20) }, palette = PalettePurple, modifier = Modifier.fillMaxWidth(), fontSize = 20.sp)
        Spacer(Modifier.weight(1f))
        GameControls(onExit = onExit)
    }
}

@Composable
private fun QuizRun(total: Int, onExit: () -> Unit, onFinish: (GameResult) -> Unit) {
    var seed by remember { mutableIntStateOf(0) }
    // Fresh random questions + fresh answer order every round.
    val quiz = remember(seed) {
        BANK.shuffled().take(total.coerceAtMost(BANK.size)).map { q ->
            val opts = (q.wrong + q.answer).shuffled()
            PreparedQ(q.q, opts, opts.indexOf(q.answer))
        }
    }
    var idx by remember(seed) { mutableIntStateOf(0) }
    var score by remember(seed) { mutableIntStateOf(0) }
    var correctCount by remember(seed) { mutableIntStateOf(0) }
    var selected by remember(seed, idx) { mutableStateOf<Int?>(null) }
    var timeLeft by remember(seed, idx) { mutableIntStateOf(TIME_LIMIT) }
    var finished by remember(seed) { mutableStateOf(false) }

    val current = quiz[idx]

    fun advance() {
        if (idx + 1 >= quiz.size) {
            if (!finished) {
                finished = true
                val pct = correctCount * 100 / quiz.size
                val stars = when { pct >= 80 -> 3; pct >= 50 -> 2; else -> 1 }
                onFinish(
                    GameResult(
                        game = Game.QUIZ, primary = score.toLong(), secondary = 0, stars = stars,
                        statPanels = listOf("SCORE" to "$score", "CORRECT" to "$correctCount/${quiz.size}"),
                    )
                )
            }
        } else {
            idx++
        }
    }

    fun choose(i: Int) {
        if (selected != null) return
        selected = i
        if (i == current.correct) {
            correctCount++
            score += BASE_POINTS + (SPEED_POINTS * timeLeft / TIME_LIMIT)
        }
    }

    // per-question countdown; timing out reveals the answer
    LaunchedEffect(seed, idx) {
        while (timeLeft > 0 && selected == null) {
            delay(1000)
            timeLeft--
        }
        if (selected == null) selected = -1   // timed out
    }
    // after an answer is shown, move on
    LaunchedEffect(selected) {
        if (selected != null) {
            delay(1400)
            advance()
        }
    }

    GameScaffold(title = "Skills Quiz", onBack = onExit, showBack = false) {
        Spacer(Modifier.height(6.dp))
        GameHud(
            primaryLabel = "QUESTION",
            primaryValue = "${idx + 1}/${quiz.size}",
            stats = listOf("TIME" to "${timeLeft}s"),   // score stays hidden until the end
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        // Question card, centered in the screen.
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(InkNavy).padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(current.text, color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(16.dp))
                current.options.forEachIndexed { i, opt ->
                    val revealed = selected != null
                    val isCorrect = i == current.correct
                    val isPicked = selected == i
                    val bg: Color = when {
                        revealed && isCorrect -> PaletteGreen.bg
                        revealed && isPicked && !isCorrect -> AlertRed
                        revealed -> Muted.copy(alpha = 0.4f)
                        else -> OPTION_COLORS[i % OPTION_COLORS.size]
                    }
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp)).background(bg)
                            .clickable(enabled = !revealed) { choose(i) }
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(opt, color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                            if (revealed && isCorrect) Text("✓", color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            if (revealed && isPicked && !isCorrect) Text("✗", color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
                if (selected != null) {
                    Spacer(Modifier.height(10.dp))
                    val ok = selected == current.correct
                    Text(
                        if (ok) "✅ CORRECT!" else "❌ WRONG — the green one was right",
                        color = if (ok) PaletteGreen.bg else AlertRed,
                        fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        GameControls(onExit = onExit, onRestart = { seed++ })
    }
}

// ---- Question bank (53) — kid-friendly, about the skills in the arcade -----

private val BANK = listOf(
    // Coding & computers
    Q("What are the step-by-step instructions we give a computer called?", "Code", listOf("Paint", "Music", "A snack")),
    Q("In coding, what does a LOOP do?", "Repeats steps", listOf("Deletes the app", "Turns off the screen", "Paints a wall")),
    Q("What does an IF block let a program do?", "Make a choice", listOf("Charge the battery", "Print paper", "Play a song")),
    Q("Which part shows the computer's pictures?", "The monitor", listOf("The keyboard", "The mouse", "The speaker")),
    Q("What do we press to type letters?", "The keyboard", listOf("The monitor", "The printer", "The router")),
    Q("A 'bug' in code is a…", "Mistake to fix", listOf("Real insect", "New game", "Fast computer")),
    Q("What does the CPU do in a computer?", "The thinking", listOf("Holds coffee", "Cleans the desk", "Only plays music")),
    Q("Why do we SAVE our work?", "To keep it for later", listOf("To delete it", "To send it to the moon", "To make it louder")),
    // AI & robots
    Q("What does AI stand for?", "Artificial Intelligence", listOf("Apple Ice", "Angry Insect", "Auto Injection")),
    Q("A robot follows…", "A program", listOf("A rainbow", "A smell", "A cloud")),
    Q("How can AI help doctors?", "Spot problems in scans", listOf("Cook dinner", "Drive a bus", "Paint nails")),
    Q("How does an AI learn?", "From many examples", listOf("From one nap", "From a balloon", "From a loud shout")),
    // Design
    Q("A designer plans how things…", "Look and work", listOf("Taste", "Smell", "Sleep")),
    Q("Which is a primary color?", "Red", listOf("Brown", "Pink", "Gray")),
    Q("Mixing blue and yellow paint makes…", "Green", listOf("Purple", "Orange", "Black")),
    Q("A logo is a…", "Symbol for a brand", listOf("Type of food", "Kind of car", "Musical note")),
    // 3D printing & CAD
    Q("A 3D printer builds objects…", "Layer by layer", listOf("All at once", "With paint", "By baking")),
    Q("CAD software is used to…", "Design things in 3D", listOf("Cook meals", "Send letters", "Water plants")),
    Q("What does a 3D printer melt to print?", "Plastic filament", listOf("Water", "Sand", "Milk")),
    Q("Before printing a part, you first…", "Design it on a computer", listOf("Eat it", "Paint the wall", "Wash it")),
    // CNC & making
    Q("A CNC machine can…", "Cut and drill precisely", listOf("Bake cookies", "Sing songs", "Fly a kite")),
    Q("What should a machine do FIRST, safely?", "Go to its start point", listOf("Cut randomly", "Spin forever", "Fall over")),
    Q("Safety goggles protect your…", "Eyes", listOf("Feet", "Ears", "Hair")),
    // Healthcare
    Q("A stethoscope is used to…", "Listen to the heart", listOf("Cut hair", "Measure a room", "Paint")),
    Q("Where do used sharp needles go?", "The sharps bin", listOf("The recycling", "Your pocket", "A backpack")),
    Q("Gloves, mask and goggles are called…", "PPE", listOf("Toys", "Tools", "Snacks")),
    Q("A thermometer measures…", "Temperature", listOf("Weight", "Speed", "Color")),
    Q("A bandage helps a…", "Small cut", listOf("Broken phone", "Flat tire", "Dead battery")),
    // Automotive maintenance
    Q("Engine oil helps the engine…", "Run smoothly", listOf("Change color", "Play music", "Fly")),
    Q("What is filled with air and rolls the car?", "The tire", listOf("The battery", "The mirror", "The seat")),
    Q("A car battery stores…", "Electricity", listOf("Water", "Oil", "Sand")),
    Q("Which fluid keeps an engine cool?", "Coolant", listOf("Soda", "Juice", "Paint")),
    Q("Which tool loosens a bolt?", "A wrench", listOf("A spoon", "A brush", "A pillow")),
    // Home automation & electrical
    Q("A smart light can be turned on by…", "A phone app", listOf("A magic word only", "Throwing it", "Freezing it")),
    Q("IF it gets dark, a smart lamp can…", "Turn itself on", listOf("Explode", "Melt", "Fly away")),
    Q("Electricity travels through…", "Wires", listOf("Ropes", "Straws", "Ribbons")),
    Q("A circuit breaker keeps your home…", "Safe from too much power", listOf("Warm and cozy", "Full of music", "Extra clean")),
    Q("A motion sensor notices…", "Movement", listOf("Smells", "Dreams", "Colors only")),
    Q("A smart thermostat controls the…", "Temperature", listOf("TV channel", "Door color", "Music")),
    Q("You reached home! A smart house can first…", "Turn on the lights", listOf("Cook by itself", "Drive away", "Grow a tree")),
    // Geometry & shapes
    Q("How many sides does a triangle have?", "3", listOf("4", "5", "6")),
    Q("A square has how many equal sides?", "4", listOf("3", "5", "2")),
    Q("A shape that is perfectly round is a…", "Circle", listOf("Square", "Triangle", "Star")),
    Q("How many sides does a hexagon have?", "6", listOf("4", "5", "8")),
    Q("A diamond shape is also called a…", "Rhombus", listOf("Circle", "Cube", "Cone")),
    // Logic, memory & teamwork
    Q("Playing memory games trains your…", "Brain", listOf("Shoes", "Teeth", "Hair")),
    Q("Doing steps in the right order is a…", "Sequence", listOf("Salad", "Song", "Sofa")),
    Q("A maze is solved by finding the right…", "Path", listOf("Color", "Sound", "Smell")),
    Q("Practice makes you…", "Better", listOf("Shorter", "Sleepy", "Invisible")),
    Q("Teamwork means…", "Working together", listOf("Always working alone", "Sleeping", "Eating")),
    // STEM attitude & safety
    Q("If you don't know something, it's smart to…", "Ask and learn", listOf("Give up", "Hide", "Shout")),
    Q("Recycling helps the…", "Planet", listOf("TV", "Clock", "Phone")),
    Q("Engineers love to solve…", "Problems", listOf("Nothing", "Only puzzles", "Dinners")),
)
