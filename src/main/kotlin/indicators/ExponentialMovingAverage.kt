package com.seansoper.baochuan.indicators

import net.jacobpeterson.alpaca.AlpacaAPI

/**
 * Calculate the exponential moving average (EMA) for a given ticker over a defined period
 *
 * This example will calculate the EMA over 9, 12 and 26 days for AAPL
 *
 *  val config = Config.parse()
 *  val alpacaClient = with(config.alpaca) {
 *    AlpacaAPI(key, secret, EndpointAPIType.LIVE, DataAPIType.IEX)
 *  }
 *
 *  val ema9 = ExponentialMovingAverage(alpacaClient).get("AAPL", Period.DAY, 9)
 *  val ema12 = ExponentialMovingAverage(alpacaClient).get("AAPL", Period.DAY, 12)
 *  val ema26 = ExponentialMovingAverage(alpacaClient).get("AAPL", Period.DAY, 26)
 *
 *  println("EMA 9 days: $ema9 - 12 days: $ema12 - 26 days: $ema26")
 */
class ExponentialMovingAverage(private val client: AlpacaAPI) {

    fun get(ticker: String, period: Period, amount: Int): Float? {
        return client.marketData().getLatestTrade(ticker).trade.p?.let {
            val simpleMovingAvg = SimpleMovingAverage(client).get(ticker, period, amount)
            val multiplier = 2f / (1f + amount.toFloat())
            ((it.toFloat() - simpleMovingAvg) * multiplier) + simpleMovingAvg
        }
    }
}
