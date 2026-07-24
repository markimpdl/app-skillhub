package com.osaparecidos.memoriamaker.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * A single Hall of Fame entry. [primary]/[secondary] meaning depends on the game's [Metric]:
 * Memory time+moves, Bugs/Print score, Simon round, Wire/If...Then stars+time.
 */
data class ScoreEntry(
    val name: String,
    val primary: Long,
    val secondary: Long,
    val stars: Int,
)

/** An Overall champion: total stars earned across all games plus how many distinct games played. */
data class OverallEntry(
    val name: String,
    val stars: Int,
    val gamesPlayed: Int,
)

/**
 * Offline, per-game leaderboards + an Overall star ranking, stored in SharedPreferences as JSON.
 * No network, no database — everything lives on the shared kiosk device. Names are free-typed
 * uppercase. Replaces the old single-game ScoreStore.
 */
class ScoreRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Best-first comparator for a game's metric. */
    private fun comparator(game: Game): Comparator<ScoreEntry> = when (game.metric) {
        Metric.TIME_MOVES -> compareBy({ it.primary }, { it.secondary })          // lower time, then fewer moves
        Metric.SCORE -> compareByDescending<ScoreEntry> { it.primary }             // higher score
        Metric.ROUND -> compareByDescending<ScoreEntry> { it.primary }             // longer sequence
        Metric.STARS_TIME -> compareByDescending<ScoreEntry> { it.primary }        // more stars,
            .thenBy { it.secondary }                                               // then faster
    }

    // ---- Per-game leaderboard ----------------------------------------------

    /** Scores for [game], best-first, capped at [MAX_ENTRIES]. */
    fun getScores(game: Game): List<ScoreEntry> {
        val raw = prefs.getString(keyScores(game), null) ?: return emptyList()
        val list = mutableListOf<ScoreEntry>()
        val array = JSONArray(raw)
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            list.add(
                ScoreEntry(
                    o.optString("n", "PLAYER"),
                    o.optLong("p", 0L),
                    o.optLong("s", 0L),
                    o.optInt("st", 0),
                )
            )
        }
        return list.sortedWith(comparator(game)).take(MAX_ENTRIES)
    }

    fun best(game: Game): ScoreEntry? = getScores(game).firstOrNull()

    /** True when [entry] beats the current record (or there is none yet). */
    fun isRecord(game: Game, primary: Long, secondary: Long): Boolean {
        val best = best(game) ?: return true
        return comparator(game).compare(ScoreEntry("", primary, secondary, 0), best) < 0
    }

    /** True when this score would make the top [MAX_ENTRIES]. */
    fun qualifies(game: Game, primary: Long, secondary: Long): Boolean {
        val scores = getScores(game)
        if (scores.size < MAX_ENTRIES) return true
        return comparator(game).compare(ScoreEntry("", primary, secondary, 0), scores.last()) < 0
    }

    /**
     * Saves a play: appends to the game leaderboard and credits the Overall ranking.
     * Returns the 1-based position in the game leaderboard (or 0 if it didn't qualify).
     */
    fun addScore(game: Game, name: String, primary: Long, secondary: Long, stars: Int): Int {
        val entry = ScoreEntry(name, primary, secondary, stars)
        val updated = (getScores(game) + entry).sortedWith(comparator(game)).take(MAX_ENTRIES)
        val array = JSONArray()
        updated.forEach {
            array.put(
                JSONObject()
                    .put("n", it.name).put("p", it.primary).put("s", it.secondary).put("st", it.stars)
            )
        }
        prefs.edit().putString(keyScores(game), array.toString()).apply()

        creditOverall(name, game, stars)
        return updated.indexOf(entry) + 1
    }

    // ---- Overall (stars across all games) ----------------------------------

    private fun creditOverall(name: String, game: Game, stars: Int) {
        val map = readOverall()
        val cur = map[name] ?: MutableOverall()
        cur.stars += stars
        cur.games.add(game.id)
        map[name] = cur
        writeOverall(map)
    }

    /** Overall champions, most stars first, capped at [MAX_ENTRIES]. */
    fun getOverall(): List<OverallEntry> =
        readOverall().map { (name, o) -> OverallEntry(name, o.stars, o.games.size) }
            .sortedWith(compareByDescending<OverallEntry> { it.stars }.thenByDescending { it.gamesPlayed })
            .take(MAX_ENTRIES)

    private class MutableOverall(var stars: Int = 0, val games: MutableSet<String> = mutableSetOf())

    private fun readOverall(): MutableMap<String, MutableOverall> {
        val raw = prefs.getString(KEY_OVERALL, null) ?: return mutableMapOf()
        val map = mutableMapOf<String, MutableOverall>()
        val array = JSONArray(raw)
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            val games = mutableSetOf<String>()
            val ga = o.optJSONArray("g") ?: JSONArray()
            for (j in 0 until ga.length()) games.add(ga.getString(j))
            map[o.optString("n", "PLAYER")] = MutableOverall(o.optInt("st", 0), games)
        }
        return map
    }

    private fun writeOverall(map: Map<String, MutableOverall>) {
        val array = JSONArray()
        map.forEach { (name, o) ->
            val ga = JSONArray()
            o.games.forEach { ga.put(it) }
            array.put(JSONObject().put("n", name).put("st", o.stars).put("g", ga))
        }
        prefs.edit().putString(KEY_OVERALL, array.toString()).apply()
    }

    // ---- Level progress (Wire / If...Then) ---------------------------------

    /** Best stars earned on [level] (0-based) of [game]; 0 = not completed yet. */
    fun getLevelStars(game: Game, level: Int): Int {
        val raw = prefs.getString(keyLevels(game), null) ?: return 0
        val o = JSONObject(raw)
        return o.optInt(level.toString(), 0)
    }

    /** Records [stars] for [level], keeping the best result. */
    fun setLevelStars(game: Game, level: Int, stars: Int) {
        val raw = prefs.getString(keyLevels(game), null)
        val o = if (raw != null) JSONObject(raw) else JSONObject()
        val best = maxOf(o.optInt(level.toString(), 0), stars)
        o.put(level.toString(), best)
        prefs.edit().putString(keyLevels(game), o.toString()).apply()
    }

    /** A level is unlocked when it's the first one or the previous level is done. */
    fun isLevelUnlocked(game: Game, level: Int): Boolean =
        level == 0 || getLevelStars(game, level - 1) > 0

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS = "skills_camp_scores"
        private const val KEY_OVERALL = "overall"
        const val MAX_ENTRIES = 5

        private fun keyScores(game: Game) = "scores_${game.id}"
        private fun keyLevels(game: Game) = "levels_${game.id}"

        /** Formats milliseconds as mm:ss for display. */
        fun formatTime(ms: Long): String {
            val totalSeconds = ms / 1000
            return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
        }

        /** Human label + value for a game's primary/secondary metric (used by rankings/victory). */
        fun primaryLabel(game: Game): String = when (game.metric) {
            Metric.TIME_MOVES -> "TIME"
            Metric.SCORE -> "SCORE"
            Metric.ROUND -> "ROUND"
            Metric.STARS_TIME -> "STARS"
        }

        fun formatPrimary(game: Game, entry: ScoreEntry): String = when (game.metric) {
            Metric.TIME_MOVES -> formatTime(entry.primary)
            Metric.SCORE -> entry.primary.toString()
            Metric.ROUND -> entry.primary.toString()
            Metric.STARS_TIME -> "⭐${entry.primary}"
        }

        /** Optional secondary detail shown under the name in rankings. */
        fun formatSecondary(game: Game, entry: ScoreEntry): String = when (game.metric) {
            Metric.TIME_MOVES -> "${entry.secondary} moves"
            Metric.SCORE -> "★${entry.stars}"
            Metric.ROUND -> "★${entry.stars}"
            Metric.STARS_TIME -> formatTime(entry.secondary)
        }
    }
}
