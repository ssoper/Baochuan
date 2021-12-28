package com.seansoper.baochuan

import com.fasterxml.jackson.core.io.JsonEOFException
import com.seansoper.baochuan.watchlist.*
import com.seansoper.batil.brokers.etrade.EtradeClient
import com.seansoper.batil.brokers.etrade.auth.Authorization
import com.seansoper.batil.config.ClientConfig
import com.seansoper.batil.config.GlobalConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.properties.DataAPIType
import net.jacobpeterson.alpaca.model.properties.EndpointAPIType
import java.io.File
import java.lang.NumberFormatException

@Serializable
data class SimpleResponse(val message: String)

@Serializable
data class AddTagResponse(val message: String, val id: Int)

fun main(args: Array<String>) {
    val config = Config.parse()
    val alpacaClient = with(config.alpaca) {
        AlpacaAPI(key, secret, EndpointAPIType.LIVE, DataAPIType.IEX)
    }

    val etradeClient = with(config.etrade) {
        EtradeClient(key, secret, username, password, EtradeClient.Endpoint.LIVE)
    }

    val dataSource = HikariDataSource()
    dataSource.jdbcUrl = "jdbc:mysql://localhost:3306/${config.database.name}"
    dataSource.username = config.database.username
    dataSource.password = config.database.password

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

    embeddedServer(Netty, port = config.server.port) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true

            })
        }

        install(CORS) {
            method(HttpMethod.Options)
            method(HttpMethod.Get)
            method(HttpMethod.Put)
            method(HttpMethod.Post)
            method(HttpMethod.Delete)
            header(HttpHeaders.AccessControlAllowHeaders)
            header(HttpHeaders.ContentType)
            header(HttpHeaders.AccessControlAllowOrigin)
            anyHost()
        }

        routing {
            // Get all tickers
            get("/tickers") {
                call.respond(Watchlist(dataSource).list())
            }

            // Get the symbol and tags for a ticker
            get("/tickers/{id}") {
                try {
                    call.parameters["id"]?.toInt()?.let {
                        Watchlist(dataSource).find(it)?.let {
                            call.respond(it)
                        } ?: call.respond(HttpStatusCode.NotFound, SimpleResponse("Ticker id not found"))
                    }
                } catch (_: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest, SimpleResponse("Invalid id"))
                }
            }

            // Delete a tag from a ticker
            delete("/tickers/{tickerId}/tags/{tagId}") {
                try {
                    val tickerId = call.parameters["tickerId"]?.toInt() ?: throw NumberFormatException("Invalid tickerId")
                    val tagId = call.parameters["tagId"]?.toInt() ?: throw NumberFormatException("Invalid tagId")

                    if (Watchlist(dataSource).deleteTag(tickerId, tagId)) {
                        call.respond(SimpleResponse("Tag deleted"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, SimpleResponse("Tag for ticker not found"))
                    }
                } catch (_: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest, SimpleResponse("Invalid id"))
                }
            }

            // Add a tag to a ticker
            post("/tickers/{tickerId}/tags/{tagId}") {
                try {
                    val tickerId = call.parameters["tickerId"]?.toInt() ?: throw NumberFormatException("Invalid tickerId")
                    val tagId = call.parameters["tagId"]?.toInt() ?: throw NumberFormatException("Invalid tagId")

                    if (Watchlist(dataSource).tagExists(tickerId, tagId)) {
                        call.respond(HttpStatusCode.Found, SimpleResponse("Tag already exists for ticker"))
                    } else if (Watchlist(dataSource).addTag(tickerId, tagId)) {
                        call.respond(SimpleResponse("Tag added to ticker"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, SimpleResponse("Tag not added to ticker"))
                    }
                } catch (_: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest, SimpleResponse("Invalid id"))
                }
            }

            // Update the symbol for a ticker
            put("/tickers/{id}") {
                try {
                    val tickerId = call.parameters["id"]?.toInt() ?: throw NumberFormatException("Invalid tickerId")
                    val symbol = call.receive<UpdateSymbolRequest>().symbol

                    if (Watchlist(dataSource).updateSymbol(tickerId, symbol)) {
                        call.respond(SimpleResponse("Ticker updated"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, SimpleResponse("Ticker not found"))
                    }
                } catch (_: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest, SimpleResponse("Invalid id"))
                } catch (_: SerializationException) {
                    call.respond(HttpStatusCode.BadRequest, SimpleResponse("Invalid request body"))
                }
            }

            // Get all available tags
            get("/tags") {
                call.request.queryParameters["query"]?.trim()?.let {
                    val minLength = 2
                    if (it.length < minLength) {
                        call.respond(HttpStatusCode.BadRequest, SimpleResponse("Query should be at least $minLength characters"))
                    } else {
                        call.respond(Watchlist(dataSource).searchTags(it))
                    }
                } ?: call.respond(Watchlist(dataSource).allTags())
            }

            // Add new tag with optional tickerId parameter, returns new tag id
            post("/tags") {
                try {
                    val request = call.receive<AddNewTagRequest>()
                    val tagName = request.name

                    Watchlist(dataSource).addTag(tagName)?.let { tagId ->
                        request.tickerId?.let { tickerId ->
                            if (Watchlist(dataSource).addTag(tickerId, tagId)) {
                                call.respond(AddTagResponse("Tag '${tagName}' added to ticker", tagId))
                            } else {
                                call.respond(HttpStatusCode.NotFound, SimpleResponse("Tag '${tagName}' created but not added to ticker"))
                            }
                        } ?: call.respond(AddTagResponse("Tag '${tagName}' added", tagId))
                    } ?: call.respond(HttpStatusCode.BadRequest, SimpleResponse("Could not create tag"))
                } catch (_: SerializationException) {
                    call.respond(HttpStatusCode.BadRequest, SimpleResponse("Invalid request body"))
                }
            }

            get("/lookup") {
                val query = call.request.queryParameters["query"]?.trim()?.uppercase() ?:
                    return@get call.respond(HttpStatusCode.BadRequest, SimpleResponse("No query parameter provided"))

                if (query.length < 2) {
                    return@get call.respond(listOf<SearchTickerResponse>())
                }

                val results = Watchlist(dataSource).lookup(query, etradeClient)
                call.respond(results)
            }
        }
    }.start(wait = true)
}
