package com.seansoper.baochuan.indicators

import com.seansoper.baochuan.isMarketOpen
import com.seansoper.baochuan.isMarketOpenHours
import com.seansoper.baochuan.withMarketClose
import net.jacobpeterson.alpaca.AlpacaAPI
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

enum class Period {
//    MINUTE_1,
//    MINUTE_5,
//    MINUTE_15,
//    MINUTE_30,
    HOUR_1,
    HOUR_4,
    DAY,
    WEEK
}

class SimpleMovingAverage(private val client: AlpacaAPI) {

    fun get(ticker: String, period: Period, amount: Int): Float {
        return when (period) {
            Period.WEEK -> getAverage(ticker, getWeeklyDates(amount))
            Period.DAY -> getAverage(ticker, getDailyDates(amount))
            Period.HOUR_4 -> getAverage(ticker, getHourlyDates(amount, 4))
            Period.HOUR_1 -> getAverage(ticker, getHourlyDates(amount, 1))
        }
    }

    fun getAverage(ticker: String, dates: List<ZonedDateTime>): Float {
        val values = dates.mapNotNull { date ->
            client.marketData()
                .getQuotes(ticker, date.minusMinutes(1), date, 1, null)
                .quotes?.first()?.ap
        }

        return (values.sum() / values.size.toDouble()).toFloat()
    }

    fun getHourlyDates(amount: Int, period: Int, afterHours: Boolean = false): List<ZonedDateTime> {
        val dates = mutableListOf<ZonedDateTime>()
        var date = ZonedDateTime.now(ZoneId.of("America/New_York"))
            .withMinute(0)
        var count = amount

        do {
            if ((afterHours && date.isMarketOpen()) ||
                (!afterHours && date.isMarketOpenHours())) {
                dates.add(date)
                date = date.minusHours(period.toLong())
                count--
            } else {
                date = date.minusDays(1).withMarketClose()
            }

        } while (count > 0)

        return dates.toList()
    }

    fun getWeeklyDates(amount: Int): List<ZonedDateTime> {
        val dates = mutableListOf<ZonedDateTime>()
        var date = ZonedDateTime.now(ZoneId.of("America/New_York"))
            .withHour(16)
            .withMinute(0)
            .minusDays(7)
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
        var count = amount

        do {
            if (date.isMarketOpen()) {
                dates.add(date)
                count--
            }

            date = date.minusDays(7)
        } while (count > 0)

        return dates.toList()
    }

    fun getDailyDates(amount: Int): List<ZonedDateTime> {
        val dates = mutableListOf<ZonedDateTime>()
        var date = ZonedDateTime.now(ZoneId.of("America/New_York"))
            .withHour(16)
            .withMinute(0)
            .minusDays(1)
        var count = amount

        do {
            if (date.isMarketOpen()) {
                dates.add(date)
                count--
            }

            date = date.minusDays(1)
        } while (count > 0)

        return dates.toList()
    }

}
