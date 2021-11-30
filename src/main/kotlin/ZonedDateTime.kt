package com.seansoper.baochuan

import java.time.DayOfWeek
import java.time.Month
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters

fun ZonedDateTime.isWeekend(): Boolean {
    return (this.dayOfWeek == DayOfWeek.SATURDAY || this.dayOfWeek == DayOfWeek.SUNDAY)
}

fun ZonedDateTime.isThanksgiving(): Boolean {
    return (this.month == Month.NOVEMBER &&
            this.get(ChronoField.ALIGNED_WEEK_OF_MONTH) == 4 &&
            this.dayOfWeek == DayOfWeek.THURSDAY)
}

fun ZonedDateTime.isLaborDay(): Boolean {
    return (this.month == Month.SEPTEMBER &&
            this.get(ChronoField.ALIGNED_WEEK_OF_MONTH) == 1 &&
            this.dayOfWeek == DayOfWeek.MONDAY)
}

fun ZonedDateTime.isMLKDay(): Boolean {
    return (this.month == Month.JANUARY &&
            this.get(ChronoField.ALIGNED_WEEK_OF_MONTH) == 3 &&
            this.dayOfWeek == DayOfWeek.MONDAY)
}

fun ZonedDateTime.isMemorialDay(): Boolean {
    return (this.month == Month.MAY &&
            this.dayOfMonth == this.with(TemporalAdjusters.lastInMonth(DayOfWeek.MONDAY)).dayOfMonth)
}

fun ZonedDateTime.isMarketHoliday(): Boolean {
    return (this.isThanksgiving() ||
            this.isLaborDay() ||
            this.isMLKDay() ||
            this.isMemorialDay())
}

/*
* Does not account for all observed market holidays
* Official list here https://www.nyse.com/markets/hours-calendars
*/
fun ZonedDateTime.isMarketOpen(): Boolean {
    return (!this.isWeekend() && !this.isMarketHoliday())
}