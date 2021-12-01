package com.seansoper.baochuan.watchlist

import org.ktorm.database.Database
import org.ktorm.dsl.Query
import org.ktorm.dsl.from
import org.ktorm.dsl.select
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import java.sql.ResultSet

class Watchlist(val database: Database) {

    fun list(): Query {
        return database
            .from(WatchlistTable)
            .select(WatchlistTable.ticker)
    }

}

object WatchlistTable: Table<Nothing>("watchlist") {
    val id = int("id").primaryKey()
    val ticker = varchar("ticker")
}

object TagTable: Table<Nothing>("tag") {
    val id = int("id").primaryKey()
    val name = varchar("name")
}

object WatchlistTagTable: Table<Nothing>("watchlist_tag") {
    val watchlistId = int("watchlist_id")
    val tagId = varchar("tag_id")
}
