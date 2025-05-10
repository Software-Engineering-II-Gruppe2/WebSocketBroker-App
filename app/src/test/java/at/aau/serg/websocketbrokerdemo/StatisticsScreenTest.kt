package at.aau.serg.websocketbrokerdemo

import at.aau.serg.websocketbrokerdemo.data.GameData
import at.aau.serg.websocketbrokerdemo.logic.ChartType
import at.aau.serg.websocketbrokerdemo.logic.Metric
import at.aau.serg.websocketbrokerdemo.logic.calculateChartData
import at.aau.serg.websocketbrokerdemo.logic.cumulativeWins
import at.aau.serg.websocketbrokerdemo.logic.filterByDate
import at.aau.serg.websocketbrokerdemo.logic.groupByTime
import com.google.firebase.Timestamp
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.text.SimpleDateFormat
import java.util.*

class StatisticsScreenTest {
    private val oneDayMillis = 24 * 60 * 60 * 1000L
    private lateinit var currentTime: Date

    @BeforeEach
    fun setup() {
        currentTime = Date()
    }

    private fun createGame(daysAgo: Int, won: Boolean = false, money: Int = 0, level: Int = 0, duration: Int = 0): GameData {
        val timestamp = Timestamp(Date(currentTime.time - daysAgo * oneDayMillis))
        return GameData(
            timestamp = timestamp,
            won = won,
            endMoney = money,
            levelGained = level,
            durationMinutes = duration
        )
    }

    @Test
    fun `filterByDate should return correct results for week filter`() {
        val games = listOf(
            createGame(3),
            createGame(8)
        )

        val result = games.filterByDate("week")

        assertEquals(1, result.size)
    }

    @Test
    fun `cumulativeWins should calculate running total`() {
        val games = listOf(
            createGame(4, won = true),
            createGame(3, won = false),
            createGame(2, won = true)
        ).sortedBy { it.timestamp.toDate().time }

        val result = games.cumulativeWins()

        assertAll(
            { assertEquals(3, result.size) },
            { assertEquals(listOf(1f, 1f, 2f), result.values.toList()) }
        )
    }

    @Test
    fun `groupByTime should aggregate metrics correctly`() {
        val games = listOf(
            createGame(3, money = 100),
            createGame(3, money = 200),
            createGame(2, money = 300)
        )

        val result = games.groupByTime("week", Metric.MONEY)
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())

        assertAll(
            { assertEquals(2, result.size) },
            { assertEquals(300f, result[dateFormat.format(games[0].timestamp.toDate())]) },
            { assertEquals(300f, result[dateFormat.format(games[2].timestamp.toDate())]) }
        )
    }

    @Test
    fun `calculateChartData should handle empty input gracefully`() {
        val (data, max) = calculateChartData(emptyList(), "all", Metric.DURATION)

        assertAll(
            { assertTrue(data.isEmpty()) },
            { assertEquals(1f, max) }
        )
    }


    @Test
    fun `calculateChartData should call cumulativeWins for WINS metric`() {
        val games = listOf(
            createGame(1, won = true),
            createGame(0, won = false)
        )
        val (data, max) = calculateChartData(games, "week", Metric.WINS)

        assertAll(
            { assertEquals(2, data.size) },
            { assertTrue(max >= 1f) }
        )
    }

    @Test
    fun `filterByDate should return all for 'all' filter`() {
        val games = listOf(
            createGame(400),
            createGame(10),
            createGame(0)
        )

        val result = games.filterByDate("all")

        assertEquals(3, result.size)
    }

    @Test
    fun `filterByDate should return correct results for month filter`() {
        val games = listOf(
            createGame(10),
            createGame(20),
            createGame(90)
        )

        val result = games.filterByDate("month")

        assertEquals(2, result.size)
    }

    @Test
    fun `groupByTime should use day format for month filter`() {
        val games = listOf(
            createGame(5, money = 50),
            createGame(5, money = 150),
            createGame(2, money = 100)
        )

        val result = games.groupByTime("month", Metric.MONEY)

        assertEquals(2, result.size)
        assertTrue(result.values.contains(200f))
        assertTrue(result.values.contains(100f))
    }

    @Test
    fun `groupByTime should fallback to 0f for unknown metric`() {
        val games = listOf(
            createGame(1),
            createGame(2)
        )

        val result = games.groupByTime("week", Metric.WINS)

        assertEquals(2, result.size)
        assertTrue(result.values.all { it == 0f })
    }

    @Test
    fun `calculateChartData should default max to 0f when all values are 0`() {
        val games = listOf(
            createGame(1, money = 0),
            createGame(2, money = 0)
        )

        val (data, max) = calculateChartData(games, "week", Metric.MONEY)

        assertEquals(2, data.size)
        assertEquals(0f, max)
    }

    @Test
    fun `ChartType enum values should exist`() {
        val types = ChartType.values()
        assertEquals(2, types.size)
        assertTrue(types.contains(ChartType.BAR))
        assertTrue(types.contains(ChartType.LINE))
    }

}
