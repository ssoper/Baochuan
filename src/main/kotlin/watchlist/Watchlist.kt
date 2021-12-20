package com.seansoper.baochuan.watchlist

import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.schema.*
import java.sql.SQLException
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

    fun addTag(tickerId: Int, tagId: Int): Boolean {
        return try {
            Database.connect(globalDataSource).insert(TickerTags) {
                set(it.tickerId, tickerId)
                set(it.tagId, tagId)
            } > 0
        } catch (_: SQLException) {
            false
        }
    }

    fun tagExists(tickerId: Int, tagId: Int): Boolean {
        return Database.connect(globalDataSource).sequenceOf(TickerTags)
                .find { (it.tickerId eq tickerId).and(it.tagId eq tagId) }?.let { true } ?: false
    }

    fun updateSymbol(tickerId: Int, symbol: String): Boolean {
        return Database.connect(globalDataSource).update(Tickers) {
            set(it.symbol, symbol)
            where {
                it.id eq tickerId
            }
        } > 0
    }

    fun allTags(): List<TagResult> {
        return Database.connect(globalDataSource)
            .sequenceOf(Tags)
            .sortedBy { it.name }.map {
                TagResult(id = it.id, name = it.name)
            }
    }

    fun searchTags(query: String): List<TagResult> {
        return Database.connect(globalDataSource)
            .sequenceOf(Tags)
            .filter { it.name like "$query%" }
            .sortedBy { it.name }.map {
                TagResult(id = it.id, name = it.name)
            }
    }

    // searchSymbol (searches for actual ticker using vendor like alpaca)
    // addTag (for ticker or new)
}

@Serializable
data class UpdateSymbolRequest(val symbol: String)

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
