package com.seansoper.baochuan.api

import com.seansoper.baochuan.scanners.AddNewTagRequest
import com.seansoper.baochuan.scanners.TickerExistsError
import com.seansoper.baochuan.scanners.UpdateSymbolRequest
import com.seansoper.baochuan.scanners.Watchlist
import com.seansoper.batil.brokers.etrade.EtradeClient
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.sql.DataSource

@Serializable
data class SimpleResponse(val message: String? = "Error")

@Serializable
data class AddTagResponse(val message: String, val id: Int)

class Server(client: EtradeClient, dataSource: DataSource, port: Int = 3000) {

    var client: EtradeClient
    var dataSource: DataSource
    var port: Int

    init {
        this.client = client
        this.dataSource = dataSource
        this.port = port
    }

    fun create(): ApplicationEngine {
        return embeddedServer(Netty, port = port) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                    }
                )
            }

            // TODO: Use settings switch to determine
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

                // Delete a ticker
                delete("/tickers/{id}") {
                    try {
                        val tickerId = call.parameters["id"]?.toInt() ?: throw NumberFormatException()

                        if (Watchlist(dataSource).deleteTicker(tickerId)) {
                            call.respond(SimpleResponse("Ticker deleted"))
                        } else {
                            call.respond(HttpStatusCode.NotFound, SimpleResponse("Ticker not found"))
                        }
                    } catch (_: NumberFormatException) {
                        call.respond(HttpStatusCode.BadRequest, SimpleResponse("Invalid id"))
                    }
                }

                // Add a ticker
                post("/tickers") {
                    try {
                        val symbol = call.receive<UpdateSymbolRequest>().symbol

                        Watchlist(dataSource).addTicker(symbol)?.let {
                            call.respond(AddTagResponse("Ticker added", it.id))
                        } ?: call.respond(HttpStatusCode.NotAcceptable, SimpleResponse("Ticker not created"))
                    } catch (_: SerializationException) {
                        call.respond(HttpStatusCode.BadRequest, SimpleResponse("Invalid request body"))
                    } catch (exception: TickerExistsError) {
                        call.respond(HttpStatusCode.BadRequest, SimpleResponse(exception.message))
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
                                    call.respond(AddTagResponse("Tag '$tagName' added to ticker", tagId))
                                } else {
                                    call.respond(HttpStatusCode.NotFound, SimpleResponse("Tag '$tagName' created but not added to ticker"))
                                }
                            } ?: call.respond(AddTagResponse("Tag '$tagName' added", tagId))
                        } ?: call.respond(HttpStatusCode.BadRequest, SimpleResponse("Could not create tag"))
                    } catch (_: SerializationException) {
                        call.respond(HttpStatusCode.BadRequest, SimpleResponse("Invalid request body"))
                    }
                }

                get("/lookup") {
                    val query = call.request.queryParameters["query"]?.trim()?.uppercase()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, SimpleResponse("No query parameter provided"))

                    val results = Watchlist(dataSource).lookup(query, client)
                    call.respond(results)
                }
            }
        }
    }
}
