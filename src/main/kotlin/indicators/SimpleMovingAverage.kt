package com.seansoper.baochuan.indicators

import io.polygon.kotlin.sdk.rest.stocks.PolygonStocksClient
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*

// Limited to daily and weekly quotes without a Stocks Developer sub ($49/month)
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

class SimpleMovingAverage(private val client: PolygonStocksClient) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    fun get(ticker: String, period: Period, amount: Int): Float {
        return when (period) {
            Period.DAY -> getAverage(ticker, getDailyDates(amount))
            Period.WEEK -> getAverage(ticker, getWeeklyDates(amount))
        }
    }

    private fun getAverage(ticker: String, dates: List<String>): Float {
        val values = dates.mapNotNull {
            client.getDailyOpenCloseBlocking(ticker, it, true).close
        }

        return (values.sum() / values.size.toDouble()).toFloat()
    }

    private fun getWeeklyDates(amount: Int): List<String> {
        val dates = mutableListOf<String>()
        var date = ZonedDateTime.now(ZoneId.of("America/New_York"))
            .minusDays(7)
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
        var week = amount

        do {
            if (marketOpen(date)) {
                dates.add(date.format(dateFormatter))
                week--
            }

            date = date.minusDays(7)
        } while (week > 0)

        return dates.toList()
    }

    private fun getDailyDates(amount: Int): List<String> {
        val dates = mutableListOf<String>()
        var date = ZonedDateTime.now(ZoneId.of("America/New_York")).minusDays(1)
        var day = amount

        do {
            if (marketOpen(date)) {
                dates.add(date.format(dateFormatter))
                day--
            }

            date = date.minusDays(1)
        } while (day > 0)

        return dates.toList()
    }

    // Doesn’t account for market holidays
    private fun marketOpen(date: ZonedDateTime): Boolean {
        return (date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY)
    }
}