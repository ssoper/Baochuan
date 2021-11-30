package com.seansoper.baochuan.indicators

/*
class ExponentialMovingAverage(private val client: PolygonStocksClient) {

    fun get(ticker: String, period: Period, amount: Int): Float? {
        return client.getLastTradeBlocking(ticker).lastTrade?.price?.let {
            val simpleMovingAvg = SimpleMovingAverage(client).get(ticker, period, amount)
            val multiplier = 2f/(1f+amount.toFloat())
            ((it.toFloat() - simpleMovingAvg) * multiplier) + simpleMovingAvg
        }
    }

}*/