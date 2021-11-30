package com.seansoper.baochuan.indicators

import com.seansoper.baochuan.isMarketOpen
import net.jacobpeterson.alpaca.AlpacaAPI
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*

enum class Period {
//    MINUTE_1,
//    MINUTE_5,
//    MINUTE_15,
//    MINUTE_30,
//    HOUR_1,
//    HOUR_4,
    DAY,
    WEEK
}

class SimpleMovingAverage(private val client: AlpacaAPI) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    fun get(ticker: String, period: Period, amount: Int): Float {
        return when (period) {
            Period.DAY -> getAverage(ticker, getDailyDates(amount))
            Period.WEEK -> getAverage(ticker, getWeeklyDates(amount))
        }
    }

    private fun getAverage(ticker: String, dates: List<ZonedDateTime>): Float {
        val values = dates.mapNotNull { date ->
            client.marketData()
                .getQuotes(ticker, date.minusMinutes(1), date, 1, null)
                .quotes?.first()?.ap
        }

        return (values.sum() / values.size.toDouble()).toFloat()
    }

    private fun getWeeklyDates(amount: Int): List<ZonedDateTime> {
        val dates = mutableListOf<ZonedDateTime>()
        var date = ZonedDateTime.now(ZoneId.of("America/New_York"))
            .withHour(16)
            .withMinute(0)
            .minusDays(7)
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
        var week = amount

        do {
            if (date.isMarketOpen()) {
                dates.add(date)
                week--
            }

            date = date.minusDays(7)
        } while (week > 0)

        return dates.toList()
    }

    private fun getDailyDates(amount: Int): List<ZonedDateTime> {
        val dates = mutableListOf<ZonedDateTime>()
        var date = ZonedDateTime.now(ZoneId.of("America/New_York"))
            .withHour(16)
            .withMinute(0)
            .minusDays(1)
        var day = amount

        do {
            if (date.isMarketOpen()) {
                dates.add(date)
                day--
            }

            date = date.minusDays(1)
        } while (day > 0)

        return dates.toList()
    }

}
