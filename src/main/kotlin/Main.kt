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

fun main(args: Array<String>) {
    val config = Config.parse()
    val alpacaClient = with(config.alpaca) {
        AlpacaAPI(key, secret, EndpointAPIType.LIVE, DataAPIType.IEX)
    }

    val etradeClient = with(config.etrade) {
        EtradeClient(key, secret, username, password, EtradeClient.Endpoint.LIVE)
    }

    val dataSource = HikariDataSource()
    dataSource.jdbcUrl = "jdbc:mysql://${config.database.server}:${config.database.port}/${config.database.name}"
    dataSource.username = config.database.username
    dataSource.password = config.database.password

    val scanner = OptionScanner(etradeClient, dataSource)
    // Uncomment the following to run the blocking 1s scanner
//    runBlocking {
//        scanner.scan()
//    }

    // Uncomment the following to generate a report given a date (or today if no date provided)
//    runBlocking {
//        val date = LocalDateTime.now().minusDays(3)
//        println("Generating report for $date")
//        async {
//            scanner.generateReport("/Users/ssoper/workspace/Baochuan/options.csv")
//            println("Finished generating report for $date")
//        }
//    }

    val server = Server(etradeClient, dataSource, config.server.port)
    // Uncomment the following to run an API service
//    server.create().start(wait = true)
}
