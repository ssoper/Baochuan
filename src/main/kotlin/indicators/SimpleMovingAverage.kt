package com.seansoper.baochuan.indicators

import io.polygon.kotlin.sdk.rest.stocks.PolygonStocksClient
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class SimpleMovingAverage(val client: PolygonStocksClient) {

    enum class Period {
        DAY
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    fun get(ticker: String, period: Period, amount: Int): Float {
        return when (period) {
            Period.DAY -> getDaily(ticker, amount)
        }
    }

    private fun getDaily(ticker: String, amount: Int): Float {
        val dates = getDates(amount)
        val values = dates.mapNotNull {
            client.getDailyOpenCloseBlocking(ticker, it, true).close
        }

        return (values.sum() / values.size.toDouble()).toFloat()
    }

    private fun getDates(amount: Int): List<String> {
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