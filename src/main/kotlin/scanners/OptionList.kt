package com.seansoper.baochuan.scanners

import com.seansoper.batil.brokers.etrade.api.OptionDetails
import org.ktorm.database.Database
import org.ktorm.dsl.greater
import org.ktorm.dsl.insert
import org.ktorm.dsl.less
import org.ktorm.entity.Entity
import org.ktorm.entity.filter
import org.ktorm.entity.groupBy
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.datetime
import org.ktorm.schema.double
import org.ktorm.schema.float
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import java.sql.SQLException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.sql.DataSource

class OptionList(dataSource: DataSource) {

    var dataSource: DataSource

    init {
        this.dataSource = dataSource
    }

    fun add(type: String, underlyingPrice: Float, option: OptionDetails): Boolean {
        return try {
            Database.connect(dataSource).insert(Options) {
                val timestamp = ZonedDateTime.ofInstant(option.timeStamp, ZoneOffset.UTC)
                val localDateTime = timestamp.withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime()
                set(it.timestamp, localDateTime)
                set(it.symbol, option.symbol)
                set(it.expiration, option.expiration?.toLocalDateTime())
                set(it.type, type)
                set(it.strikePrice, option.strikePrice)
                set(it.display, option.displaySymbol)
                set(it.osiKey, option.osiKey)
                set(it.underlyingPrice, underlyingPrice)
                set(it.bid, option.bid)
                set(it.bidSize, option.bidSize)
                set(it.ask, option.ask)
                set(it.askSize, option.askSize)
                set(it.inTheMoney, option.inTheMoney)
                set(it.volume, option.volume)
                set(it.openInterest, option.openInterest)
                set(it.netChange, option.netChange)
                set(it.lastPrice, option.lastPrice)
                set(it.rho, option.greeks.rho?.toDouble())
                set(it.vega, option.greeks.vega?.toDouble())
                set(it.theta, option.greeks.theta?.toDouble())
                set(it.delta, option.greeks.delta?.toDouble())
                set(it.gamma, option.greeks.gamma?.toDouble())
                set(it.iv, option.greeks.iv)
            } > 0
        } catch (_: SQLException) {
            false
        }
    }

    fun getOptionsFor(dateTime: LocalDateTime? = null): List<OptionResult>? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val today = LocalDate.now(ZoneId.of("America/New_York"))
        val todayMidnight = LocalDateTime.of(dateTime?.toLocalDate() ?: today, LocalTime.MIDNIGHT)
        val tomorrow = LocalDate.now(ZoneId.of("America/New_York")).plusDays(1)
        val tomorrowMidnight = LocalDateTime.of(dateTime?.toLocalDate()?.plusDays(1) ?: tomorrow, LocalTime.MIDNIGHT)

        return Database.connect(dataSource)
            .sequenceOf(Options)
            .filter { it.timestamp.greater(todayMidnight) }
            .filter { it.timestamp.less(tomorrowMidnight) }
            .groupBy { "${it.osiKey}-${it.timestamp.format(formatter)}" }
            .map {
                val low = it.value.minOf { it.lastPrice }
                val high = it.value.maxOf { it.lastPrice }

                it.value.sortedBy { it.timestamp.second }.last().let {
                    OptionResult(
                        timestamp = it.timestamp,
                        symbol = it.symbol,
                        type = it.type,
                        strikePrice = it.strikePrice,
                        underlyingPrice = it.underlyingPrice,
                        high = high,
                        low = low,
                        display = it.display,
                        osiKey = it.osiKey,
                        bid = it.bid,
                        bidSize = it.bidSize,
                        ask = it.ask,
                        askSize = it.askSize,
                        inTheMoney = it.inTheMoney,
                        volume = it.volume,
                        openInterest = it.openInterest,
                        netChange = it.netChange,
                        lastPrice = it.lastPrice,
                        rho = it.rho,
                        vega = it.vega,
                        theta = it.theta,
                        delta = it.delta,
                        gamma = it.gamma,
                        iv = it.iv
                    )
                }
            }
    }
}

data class OptionResult(
    val timestamp: LocalDateTime,
    val symbol: String,
    val type: String,
    val strikePrice: Float,
    val underlyingPrice: Float,
    val high: Float,
    val low: Float,
    val display: String,
    val osiKey: String,
    val bid: Float,
    val bidSize: Int,
    val ask: Float,
    val askSize: Int,
    val inTheMoney: Boolean,
    val volume: Int,
    val openInterest: Int,
    val netChange: Float,
    val lastPrice: Float,
    val rho: Double,
    val vega: Double,
    val theta: Double,
    val delta: Double,
    val gamma: Double,
    val iv: Float
)

interface Option : Entity<Option> {
    val id: Int
    val timestamp: LocalDateTime
    val symbol: String
    val expiration: LocalDateTime
    val type: String
    val strikePrice: Float
    val display: String
    val osiKey: String
    val underlyingPrice: Float
    val bid: Float
    val bidSize: Int
    val ask: Float
    val askSize: Int
    val inTheMoney: Boolean
    val volume: Int
    val openInterest: Int
    val netChange: Float
    val lastPrice: Float
    val rho: Double
    val vega: Double
    val theta: Double
    val delta: Double
    val gamma: Double
    val iv: Float
}

object Options : Table<Option>("options") {
    val id = int("id").primaryKey().bindTo { it.id }
    val timestamp = datetime("timestamp").bindTo { it.timestamp }
    val symbol = varchar("symbol").bindTo { it.symbol }
    val expiration = datetime("expiration").bindTo { it.expiration }
    val type = varchar("type").bindTo { it.type }
    val strikePrice = float("strikePrice").bindTo { it.strikePrice }
    val display = varchar("display").bindTo { it.display }
    val osiKey = varchar("osiKey").bindTo { it.osiKey }
    val underlyingPrice = float("underlyingPrice").bindTo { it.underlyingPrice }
    val bid = float("bid").bindTo { it.bid }
    val bidSize = int("bidSize").bindTo { it.bidSize }
    val ask = float("ask").bindTo { it.ask }
    val askSize = int("askSize").bindTo { it.askSize }
    val inTheMoney = boolean("inTheMoney").bindTo { it.inTheMoney }
    val volume = int("volume").bindTo { it.volume }
    val openInterest = int("openInterest").bindTo { it.openInterest }
    val netChange = float("netChange").bindTo { it.netChange }
    val lastPrice = float("lastPrice").bindTo { it.lastPrice }
    val rho = double("rho").bindTo { it.rho }
    val vega = double("vega").bindTo { it.vega }
    val theta = double("theta").bindTo { it.theta }
    val delta = double("delta").bindTo { it.delta }
    val gamma = double("gamma").bindTo { it.gamma }
    val iv = float("iv").bindTo { it.iv }
}
