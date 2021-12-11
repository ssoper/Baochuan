package com.seansoper.baochuan.watchlist

import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
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

    fun list(): List<TickerResult> {
        return Database.connect(globalDataSource).sequenceOf(Tickers).map {
            TickerResult(id = it.id, symbol = it.symbol)
        }
    }

    fun find(id: Int): TickerResult? {
        return Database.connect(globalDataSource).sequenceOf(Tickers).find { it.id eq id }?.let {
            TickerResult(id = it.id, symbol = it.symbol, tags = it.tags.map {
                TagResult(id = it.tag.id, name = it.tag.name)
            })
        }
    }

    fun deleteTag(tickerId: Int, tagId: Int): Boolean {
        return Database.connect(globalDataSource).delete(TickerTags) {
            (it.tickerId eq tickerId).and(it.tagId eq tagId)
        } > 0
    }

    fun updateName(name: String, tickerId: Int): Boolean {
        return false
    }

    // addTag (for ticker or new)
}

@Serializable
data class TagResult(val id: Int, val name: String)

@Serializable
data class TickerResult(val id: Int, val symbol: String, val tags: List<TagResult>? = null)

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
