package com.seansoper.baochuan

import com.seansoper.baochuan.api.Server
import com.seansoper.baochuan.scanners.OptionScanner
import com.seansoper.batil.brokers.etrade.EtradeClient
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.properties.DataAPIType
import net.jacobpeterson.alpaca.model.properties.EndpointAPIType
import java.time.LocalDateTime
import kotlin.math.pow
import kotlin.math.sqrt

fun standardDeviation(data: FloatArray): Double {
    val mean = data.average()
    return data
        .fold(0.0) { accumulator, next -> accumulator + (next - mean).pow(2.0) }
        .let {
            sqrt(it / data.size) // data.size-1 for the other type of standard dev
        }
}

fun main(args: Array<String>) {
    val config = Config.parse()
    val alpacaClient = with(config.alpaca) {
        AlpacaAPI(key, secret, EndpointAPIType.LIVE, DataAPIType.IEX)
    }

    val etradeClient = with(config.etrade) {
        val endpoint = when (production) {
            true -> EtradeClient.Endpoint.LIVE
            false -> EtradeClient.Endpoint.SANDBOX
        }

        EtradeClient(key, secret, username, password, endpoint)
    }

    val dataSource = HikariDataSource()
    dataSource.jdbcUrl = "jdbc:mysql://${config.database.server}:${config.database.port}/${config.database.name}"
    dataSource.username = config.database.username
    dataSource.password = config.database.password

    val scanner = OptionScanner(etradeClient, dataSource)
    // Uncomment the following to run the blocking 1s scanner
//    runBlocking {
//        try {
//            scanner.scan()
//        } catch (error: Exception) {
//            // This only works on a mac
//            ProcessBuilder("/usr/bin/osascript", "-e", "display notification \"${error.message}\" with title \"Baochuan error\"")
//                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
//            .start()
//        }
//    }

    // Uncomment the following to generate a report given a date (or today if no date provided)
//    runBlocking {
//        val date = LocalDateTime.now().minusDays(3)
//        println("Generating report")
//        async {
//            scanner.generateReport("/Users/ssoper/workspace/Baochuan/options.csv")
//            println("Finished generating report")
//        }
//    }

    val server = Server(etradeClient, dataSource, config.server.port)
    // Uncomment the following to run an API service
//    server.create().start(wait = true)
}
