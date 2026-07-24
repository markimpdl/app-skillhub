package com.osaparecidos.memoriamaker.nav

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.osaparecidos.memoriamaker.data.Game
import com.osaparecidos.memoriamaker.data.GameResult
import com.osaparecidos.memoriamaker.data.ScoreRepository
import com.osaparecidos.memoriamaker.games.blockworld.BlockWorldGame
import com.osaparecidos.memoriamaker.games.camel.CamelRaceGame
import com.osaparecidos.memoriamaker.games.cnc.CncGame
import com.osaparecidos.memoriamaker.games.desert.DesertGame
import com.osaparecidos.memoriamaker.games.fillup.FillUpGame
import com.osaparecidos.memoriamaker.games.scary.ScaryWorldGame
import com.osaparecidos.memoriamaker.games.princess.PrincessGame
import com.osaparecidos.memoriamaker.games.topcar.TopCarGame
import com.osaparecidos.memoriamaker.games.flags.FlagGame
import com.osaparecidos.memoriamaker.games.georush.GeoRushGame
import com.osaparecidos.memoriamaker.games.ifthen.IfThenGame
import com.osaparecidos.memoriamaker.games.jigsaw.JigsawGame
import com.osaparecidos.memoriamaker.games.maze.MazeGame
import com.osaparecidos.memoriamaker.games.quiz.QuizGame
import com.osaparecidos.memoriamaker.games.supplies.SupplyGame
import com.osaparecidos.memoriamaker.games.memory.MemoryGame
import com.osaparecidos.memoriamaker.games.printrush.PrintGame
import com.osaparecidos.memoriamaker.games.simon.SimonGame
import com.osaparecidos.memoriamaker.games.whackabug.WhackGame
import com.osaparecidos.memoriamaker.games.wire.WireGame
import com.osaparecidos.memoriamaker.screens.HomeScreen
import com.osaparecidos.memoriamaker.screens.LevelSelectScreen
import com.osaparecidos.memoriamaker.screens.ModeSelectScreen
import com.osaparecidos.memoriamaker.screens.StudentProjectsScreen
import com.osaparecidos.memoriamaker.screens.VictoryScreen
import com.osaparecidos.memoriamaker.screens.ranking.GameRankingScreen
import com.osaparecidos.memoriamaker.screens.ranking.OverallScreen
import com.osaparecidos.memoriamaker.screens.ranking.RankingHubScreen

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val repo = remember { ScoreRepository(context) }
    val holder: GameResultHolder = viewModel()

    // Where tapping a game goes: duel games → mode picker; level games → level select;
    // plain solo games skip the (duel-less) picker and jump straight into play.
    fun startRoute(game: Game): String = when {
        game.hasDuel -> Routes.mode(game)
        game.hasLevels -> Routes.levels(game)
        else -> Routes.play(game, duel = false)
    }

    NavHost(navController = nav, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            // At the root, back cleanly finishes the app. This is deterministic and prevents a
            // rapid double-back from popping past the start destination into an empty (white) graph.
            BackHandler { activity?.finish() }
            HomeScreen(
                champions = repo.getOverall(),
                onPlay = { game -> nav.navigate(startRoute(game)) },
                onRanking = { nav.navigate(Routes.RANKING_HUB) },
                onOpenProjects = { nav.navigate(Routes.PROJECTS) },
            )
        }

        composable(Routes.PROJECTS) {
            StudentProjectsScreen(
                onBack = { nav.popBackStack() },
                onPlay = { game -> nav.navigate(startRoute(game)) },
            )
        }

        composable(
            Routes.MODE_PATTERN,
            arguments = listOf(navArgument(Routes.ARG_GAME) { type = NavType.StringType }),
        ) { entry ->
            val game = Game.fromId(entry.arguments!!.getString(Routes.ARG_GAME)!!)
            ModeSelectScreen(
                game = game,
                onBack = { nav.popBackStack() },
                onSolo = {
                    if (game.hasLevels) nav.navigate(Routes.levels(game))
                    else nav.navigate(Routes.play(game, duel = false))
                },
                onDuel = { nav.navigate(Routes.play(game, duel = true)) },
            )
        }

        composable(
            Routes.LEVELS_PATTERN,
            arguments = listOf(navArgument(Routes.ARG_GAME) { type = NavType.StringType }),
        ) { entry ->
            val game = Game.fromId(entry.arguments!!.getString(Routes.ARG_GAME)!!)
            LevelSelectScreen(
                game = game,
                repo = repo,
                onBack = { nav.popBackStack() },
                onPlayLevel = { level -> nav.navigate(Routes.play(game, duel = false, level = level)) },
            )
        }

        composable(
            Routes.PLAY_PATTERN,
            arguments = listOf(
                navArgument(Routes.ARG_GAME) { type = NavType.StringType },
                navArgument(Routes.ARG_MODE) { type = NavType.StringType },
                navArgument(Routes.ARG_LEVEL) { type = NavType.IntType },
            ),
        ) { entry ->
            val args = entry.arguments!!
            val game = Game.fromId(args.getString(Routes.ARG_GAME)!!)
            val duel = args.getString(Routes.ARG_MODE) == "duel"
            val level = args.getInt(Routes.ARG_LEVEL)

            holder.lastPlayRoute = Routes.play(game, duel, level)

            val onExit: () -> Unit = { nav.popBackStack() }
            val onFinish: (GameResult) -> Unit = { result ->
                if (game.hasLevels) {
                    repo.setLevelStars(game, level, result.stars)
                    val isLastLevel = level >= game.levelCount - 1
                    if (!isLastLevel) {
                        // Progression: jump straight into the next level, replacing this one.
                        nav.navigate(Routes.play(game, duel = false, level = level + 1)) {
                            popUpTo(Routes.PLAY_PATTERN) { inclusive = true }
                        }
                    } else {
                        holder.result = result
                        nav.navigate(Routes.VICTORY)
                    }
                } else {
                    holder.result = result
                    nav.navigate(Routes.VICTORY)
                }
            }

            when (game) {
                Game.MEMORY -> MemoryGame(duel, onExit, onFinish)
                Game.WHACK -> WhackGame(duel, onExit, onFinish)
                Game.SIMON -> SimonGame(duel, onExit, onFinish)
                Game.PRINT -> PrintGame(duel, onExit, onFinish)
                Game.WIRE -> WireGame(level, onExit, onFinish)
                Game.IFTHEN -> IfThenGame(level, onExit, onFinish)
                Game.FLAG -> FlagGame(onExit, onFinish)
                Game.FILLUP -> FillUpGame(onExit, onFinish)
                Game.CNC -> CncGame(onExit, onFinish)
                Game.SUPPLIES -> SupplyGame(onExit, onFinish)
                Game.JIGSAW -> JigsawGame(onExit, onFinish)
                Game.GEORUSH -> GeoRushGame(onExit, onFinish)
                Game.MAZE -> MazeGame(onExit, onFinish)
                Game.QUIZ -> QuizGame(onExit, onFinish)
                Game.BLOCKWORLD -> BlockWorldGame(onExit, onFinish)
                Game.CAMELRACE -> CamelRaceGame(onExit, onFinish)
                Game.TOPCAR -> TopCarGame(onExit, onFinish)
                Game.DESERT -> DesertGame(onExit, onFinish)
                Game.SCARY -> ScaryWorldGame(onExit, onFinish)
                Game.PRINCESS -> PrincessGame(onExit, onFinish)
            }
        }

        composable(Routes.VICTORY) {
            val result = holder.result
            if (result == null) {
                // No result to show (e.g. the ViewModel was recreated) — bounce home from a side
                // effect, never during composition (which would leave a blank frame on the graph).
                LaunchedEffect(Unit) { nav.popBackStack(Routes.HOME, inclusive = false) }
            } else {
                VictoryScreen(
                    result = result,
                    repo = repo,
                    onGoRanking = {
                        nav.navigate(Routes.ranking(result.game)) {
                            popUpTo(Routes.HOME) { inclusive = false }
                        }
                    },
                    onPlayAgain = {
                        val route = holder.lastPlayRoute ?: Routes.HOME
                        nav.navigate(route) { popUpTo(Routes.VICTORY) { inclusive = true } }
                    },
                    onHome = { nav.popBackStack(Routes.HOME, inclusive = false) },
                )
            }
        }

        composable(Routes.RANKING_HUB) {
            RankingHubScreen(
                repo = repo,
                onBack = { nav.popBackStack() },
                onOverall = { nav.navigate(Routes.OVERALL) },
                onGameRanking = { game -> nav.navigate(Routes.ranking(game)) },
            )
        }

        composable(
            Routes.RANKING_PATTERN,
            arguments = listOf(navArgument(Routes.ARG_GAME) { type = NavType.StringType }),
        ) { entry ->
            val game = Game.fromId(entry.arguments!!.getString(Routes.ARG_GAME)!!)
            GameRankingScreen(
                initialGame = game,
                repo = repo,
                onBack = { nav.popBackStack(Routes.HOME, inclusive = false) },
                onPlay = { g -> nav.navigate(startRoute(g)) },
            )
        }

        composable(Routes.OVERALL) {
            OverallScreen(repo = repo, onBack = { nav.popBackStack() })
        }
    }
}
