package com.seansoper.baochuan.scanners

import com.seansoper.batil.brokers.etrade.EtradeClient
import com.seansoper.batil.brokers.etrade.services.EtradeServiceError
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.sql.DataSource

class OptionScanner(client: EtradeClient, dataSource: DataSource) {

    var client: EtradeClient
    var dataSource: DataSource
    var optionDataSource: OptionList

    val columnHeaders = """
    Timestamp,Symbol,Display,Type,Strike,High,Low,Last Price,Net Change,Bid,Ask,Bid Size,Ask Size,In the money,Volume,Open Interest,Underlying Price,Vega,Theta,Delta,Gamma,IV
""".trimIndent()

    init {
        this.client = client
        this.dataSource = dataSource
        this.optionDataSource = OptionList(dataSource)
    }

    suspend fun scan(delay: Long = 1000L) {
        while (true) {
            val today = ZonedDateTime.now(ZoneId.of("America/New_York")).truncatedTo(ChronoUnit.DAYS)
            val lastTrade = client.market.ticker("SPY")?.tickerData?.lastTrade ?: throw Error("No last trade returned")
            val lowPrice = lastTrade*0.95f
            val highPrice = lastTrade*1.05f
            val chains = try {
                client.market.optionChains("SPY")
            } catch (_: EtradeServiceError) {
                continue
            }

            val options = chains?.pairs?.filter {
                it.put.strikePrice != null && it.put.strikePrice!! > lowPrice && it.put.strikePrice!! < highPrice
            }?.filter {
                it.call.strikePrice != null && it.call.strikePrice!! > lowPrice && it.call.strikePrice!! < highPrice
            }?.filter {
                it.put.expiration != null && it.put.expiration!!.truncatedTo(ChronoUnit.DAYS) == today
            }?.filter {
                it.call.expiration != null && it.call.expiration!!.truncatedTo(ChronoUnit.DAYS) == today
            }

            options?.forEach {
                it.call.let { call ->
                    optionDataSource.add("CALL", lastTrade, call)
                }

                it.put.let { put ->
                    optionDataSource.add("PUT", lastTrade, put)
                }
            }

            delay(delay)
        }
    }

    fun generateReport(pathToFile: String, date: LocalDateTime? = null) {
        File(pathToFile).writeText("$columnHeaders\n")
        OptionList(dataSource).getOptionsFor(date)?.forEach {
            File(pathToFile).appendText("${parseOptionResult(it)}\n")
        }
    }

    private fun parseOptionResult(option: OptionResult): String {
        val vega = String.format("%.9f", option.vega)
        val theta = String.format("%.9f", option.theta)
        val delta = String.format("%.9f", option.delta)
        val gamma = String.format("%.9f", option.gamma)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val timestamp = option.timestamp.format(formatter)

        return "${timestamp},${option.symbol},${option.display},${option.type},${option.strikePrice},${option.high},${option.low},${option.lastPrice},${option.netChange},${option.bid},${option.ask},${option.bidSize},${option.askSize},${option.inTheMoney},${option.volume},${option.openInterest},${option.underlyingPrice},$vega,$theta,$delta,$gamma,${option.iv}"
    }
}