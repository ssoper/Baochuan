package com.seansoper.baochuan.watchlist

import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.schema.*
import javax.sql.DataSource

internal lateinit var globalDataSource: DataSource

internal inline fun <E : Any, T : BaseTable<E>> T.getList(predicate: (T) -> ColumnDeclaring<Boolean>): List<E> {
    return Database.connect(globalDataSource).sequenceOf(this).filter(predicate).toList()
}

class Watchlist(dataSource: DataSource) {

    init {
        globalDataSource = dataSource
    }

    fun list(): List<Ticker> {
        return Database.connect(globalDataSource).sequenceOf(Tickers).toList()
    }

}

interface Ticker: Entity<Ticker> {
    val id: Int
    val symbol: String

    val tags get() = TickerTags.getList { it.tickerId eq id }
}

object Tickers: Table<Ticker>("tickers") {
    val id = int("id").primaryKey().bindTo { it.id }
    val symbol = varchar("symbol").bindTo { it.symbol }
}

interface Tag: Entity<Tag> {
    val id: Int
    val name: String
}

object Tags: Table<Tag>("tags") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
}

interface TickerTag: Entity<TickerTag> {
    val ticker: Ticker
    val tag: Tag
}

object TickerTags: Table<TickerTag>("tickers_tags") {
    val tickerId = int("ticker_id").references(Tickers) { it.ticker }
    val tagId = int("tag_id").references(Tags) { it.tag }
}
