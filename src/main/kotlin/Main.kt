package com.seansoper.baochuan

import com.seansoper.baochuan.indicators.ExponentialMovingAverage
import com.seansoper.baochuan.indicators.Period
import com.seansoper.baochuan.indicators.SimpleMovingAverage
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.polygon.kotlin.sdk.DefaultOkHttpClientProvider
import io.polygon.kotlin.sdk.HttpClientProvider
import io.polygon.kotlin.sdk.rest.PolygonRestClient
import okhttp3.Interceptor
import okhttp3.Response

val okHttpClientProvider: HttpClientProvider
    get() = DefaultOkHttpClientProvider(
        applicationInterceptors = listOf(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                println("Intercepting application level")
                return chain.proceed(chain.request())
            }
        }),
        networkInterceptors = listOf(object : Interceptor{
            override fun intercept(chain: Interceptor.Chain): Response {
                println("Intercepting network level")
                return chain.proceed(chain.request())
            }
        })
    )

fun main(args: Array<String>)  {
    val config = Config.parse()
    val polygonClient = PolygonRestClient(config.polygon.apiKey, httpClientProvider = okHttpClientProvider)
    val sma = SimpleMovingAverage(polygonClient.stocksClient).get("AAPL", Period.DAY, 4)
    val ema = ExponentialMovingAverage(polygonClient.stocksClient).get("AAPL", Period.DAY, 4)
    println(sma)
    println(ema)

    val ema9 = ExponentialMovingAverage(polygonClient.stocksClient).get("AAPL", Period.DAY, 9)
    val ema12 = ExponentialMovingAverage(polygonClient.stocksClient).get("AAPL", Period.DAY, 12)
    val ema26 = ExponentialMovingAverage(polygonClient.stocksClient).get("AAPL", Period.DAY, 26)
    println(ema9)
    println(ema12)
    println(ema26)
//
//    embeddedServer(Netty, port = config.server.port) {
//        routing {
//            get("/") {
//                call.respondText("Hello, world!")
//            }
//        }
//    }.start(wait = true)
}