package com.seansoper.baochuan.indicators

import net.jacobpeterson.alpaca.AlpacaAPI


class ExponentialMovingAverage(private val client: AlpacaAPI) {

    fun get(ticker: String, period: Period, amount: Int): Float? {
        return client.marketData().getLatestTrade(ticker).trade.p?.let {
            val simpleMovingAvg = SimpleMovingAverage(client).get(ticker, period, amount)
            val multiplier = 2f/(1f+amount.toFloat())
            ((it.toFloat() - simpleMovingAvg) * multiplier) + simpleMovingAvg
        }
    }

}