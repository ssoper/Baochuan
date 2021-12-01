package com.seansoper.baochuan

import com.seansoper.baochuan.watchlist.Watchlist
import com.zaxxer.hikari.HikariDataSource
import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.properties.DataAPIType
import net.jacobpeterson.alpaca.model.properties.EndpointAPIType

fun main(args: Array<String>)  {
    val config = Config.parse()
    val client = AlpacaAPI(config.alpaca.key, config.alpaca.secret, EndpointAPIType.LIVE, DataAPIType.IEX)

    val dataSource = HikariDataSource()
    dataSource.jdbcUrl = "jdbc:mysql://localhost:3306/${config.database.name}"
    dataSource.username = config.database.username
    dataSource.password = config.database.password

    for (row in Watchlist(dataSource).list()) {
        println(row.symbol)
        for (tag in row.tags) {
            println(tag.tag.name)
        }

        println("****")
    }

//    val sma = SimpleMovingAverage(client).get("AAPL", Period.MINUTE_15, 10)
//    val sma = SimpleMovingAverage(client).get("AAPL", Period.HOUR_4, 10)
//    val ema = ExponentialMovingAverage(client).get("AAPL", Period.DAY, 4)
//    println(sma)
//    println(ema)

//    val ema9 = ExponentialMovingAverage(client).get("AAPL", Period.DAY, 9)
//    val ema12 = ExponentialMovingAverage(client).get("AAPL", Period.DAY, 12)
//    val ema26 = ExponentialMovingAverage(client).get("AAPL", Period.DAY, 26)
//    println(ema9)
//    println(ema12)
//    println(ema26)

//    println(SimpleMovingAverage(client).getHourlyDates(10, 4, false))

    /*
    embeddedServer(Netty, port = config.server.port) {
        routing {
            get("/") {
                val ema12 = ExponentialMovingAverage(client).get("AAPL", Period.DAY, 12)
                call.respondText("EMA12 $ema12")
            }
        }
    }.start(wait = true)

     */
}