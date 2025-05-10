package at.aau.serg.websocketbrokerdemo.logic

import at.aau.serg.websocketbrokerdemo.data.GameData
import java.text.SimpleDateFormat
import java.util.Locale

enum class Metric(val displayName: String) {
    WINS("Wins"),
    LEVEL("Level"),
    MONEY("Money"),
    DURATION("Duration")
}

enum class ChartType {
    BAR, LINE
}

internal fun calculateChartData(
    games: List<GameData>,
    filter: String,
    metric: Metric
): Pair<Map<String, Float>, Float> {
    val filtered = games.filterByDate(filter)
    val processed = when (metric) {
        Metric.WINS -> filtered.cumulativeWins()
        else -> filtered.groupByTime(filter, metric)
    }
    val max = processed.values.maxOrNull() ?: 1f
    return Pair(processed, max)
}

internal fun List<GameData>.filterByDate(filter: String): List<GameData> {
    return this.filter {
        val timestamp = it.timestamp.toDate().time
        when (filter) {
            "week" -> System.currentTimeMillis() - timestamp < 604_800_000
            "month" -> System.currentTimeMillis() - timestamp < 2_592_000_000
            else -> true
        }
    }.sortedBy { it.timestamp.toDate().time }
}

internal fun List<GameData>.cumulativeWins(): Map<String, Float> {
    var cumulative = 0
    return this.associate { game ->
        cumulative += if (game.won) 1 else 0
        Pair(game.getTimeLabel(), cumulative.toFloat())
    }
}

internal fun List<GameData>.groupByTime(filter: String, metric: Metric): Map<String, Float> {
    val format = when (filter) {
        "week" -> SimpleDateFormat("EEE", Locale.getDefault())
        "month" -> SimpleDateFormat("dd", Locale.getDefault())
        else -> SimpleDateFormat("MMM", Locale.getDefault())
    }

    return groupBy {
        format.format(it.timestamp.toDate())
    }.mapValues { entry ->
        when (metric) {
            Metric.MONEY -> entry.value.sumOf { it.endMoney }.toFloat()
            Metric.LEVEL -> entry.value.sumOf { it.levelGained }.toFloat()
            Metric.DURATION -> entry.value.sumOf { it.durationMinutes }.toFloat()
            else -> 0f
        }
    }
}

private fun GameData.getTimeLabel(): String {
    val formatter = SimpleDateFormat("dd.MM", Locale.getDefault())
    return formatter.format(timestamp.toDate())
}
