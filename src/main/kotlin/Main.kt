package com.seansoper.baochuan

//import com.seansoper.baochuan.indicators.ExponentialMovingAverage
import com.seansoper.baochuan.indicators.Period
//import com.seansoper.baochuan.indicators.SimpleMovingAverage
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.properties.DataAPIType
import net.jacobpeterson.alpaca.model.properties.EndpointAPIType

fun main(args: Array<String>)  {
    val config = Config.parse()
    val client = AlpacaAPI(config.alpaca.key, config.alpaca.secret, EndpointAPIType.LIVE, DataAPIType.IEX)
    println(client.account().get())
//    val sma = SimpleMovingAverage(polygonClient.stocksClient).get("AAPL", Period.DAY, 4)
//    val ema = ExponentialMovingAverage(polygonClient.stocksClient).get("AAPL", Period.DAY, 4)
//    println(sma)
//    println(ema)
//
//    val ema9 = ExponentialMovingAverage(polygonClient.stocksClient).get("AAPL", Period.DAY, 9)
//    val ema12 = ExponentialMovingAverage(polygonClient.stocksClient).get("AAPL", Period.DAY, 12)
//    val ema26 = ExponentialMovingAverage(polygonClient.stocksClient).get("AAPL", Period.DAY, 26)
//    println(ema9)
//    println(ema12)
//    println(ema26)
//
//    embeddedServer(Netty, port = config.server.port) {
//        routing {
//            get("/") {
//                call.respondText("Hello, world!")
//            }
//        }
//    }.start(wait = true)
}