package com.seansoper.baochuan

import java.time.DayOfWeek
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters

fun ZonedDateTime.isWeekend(): Boolean {
    return (this.dayOfWeek == DayOfWeek.SATURDAY || this.dayOfWeek == DayOfWeek.SUNDAY)
}

fun ZonedDateTime.isThanksgiving(): Boolean {
    return (
        this.month == Month.NOVEMBER &&
            this.get(ChronoField.ALIGNED_WEEK_OF_MONTH) == 4 &&
            this.dayOfWeek == DayOfWeek.THURSDAY
        )
}

fun ZonedDateTime.isLaborDay(): Boolean {
    return (
        this.month == Month.SEPTEMBER &&
            this.get(ChronoField.ALIGNED_WEEK_OF_MONTH) == 1 &&
            this.dayOfWeek == DayOfWeek.MONDAY
        )
}

fun ZonedDateTime.isMLKDay(): Boolean {
    return (
        this.month == Month.JANUARY &&
            this.get(ChronoField.ALIGNED_WEEK_OF_MONTH) == 3 &&
            this.dayOfWeek == DayOfWeek.MONDAY
        )
}

fun ZonedDateTime.isMemorialDay(): Boolean {
    return (
        this.month == Month.MAY &&
            this.dayOfMonth == this.with(TemporalAdjusters.lastInMonth(DayOfWeek.MONDAY)).dayOfMonth
        )
}

fun ZonedDateTime.isMarketHoliday(): Boolean {
    return (
        this.isThanksgiving() ||
            this.isLaborDay() ||
            this.isMLKDay() ||
            this.isMemorialDay()
        )
}

fun ZonedDateTime.isMarketOpenHours(): Boolean {
    val nyseTime = this.withZoneSameLocal(ZoneId.of("America/New_York"))
    val milli = nyseTime.get(ChronoField.MILLI_OF_DAY)

    return (
        this.isMarketOpen() &&
            (
                milli > nyseTime.withMarketOpen().minusMinutes(1).get(ChronoField.MILLI_OF_DAY) &&
                    milli < nyseTime.withMarketClose().plusMinutes(1).get(ChronoField.MILLI_OF_DAY)
                )
        )
}

fun ZonedDateTime.withMarketOpen(): ZonedDateTime {
    return this
        .withHour(9)
        .withMinute(30)
        .withSecond(0)
}

fun ZonedDateTime.withMarketClose(): ZonedDateTime {
    return this
        .withHour(16)
        .withMinute(0)
        .withSecond(0)
}

/*
* Does not account for all observed market holidays
* Official list – https://www.nyse.com/markets/hours-calendars
* Alpaca API – https://alpaca.markets/deprecated/docs/api-documentation/how-to/market-hours/
*/
fun ZonedDateTime.isMarketOpen(): Boolean {
    return (!this.isWeekend() && !this.isMarketHoliday())
}
