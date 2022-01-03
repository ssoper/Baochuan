package com.seansoper.baochuan.watchlist

import com.seansoper.batil.brokers.etrade.EtradeClient
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.schema.*
import java.lang.Error
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException
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

    fun deleteTicker(id: Int): Boolean {
        return Database.connect(globalDataSource).delete(Tickers) {
            it.id eq id
        } > 0
    }

    fun addTicker(symbol: String): TickerResult? {
        val rows = try {
            Database.connect(globalDataSource).insert(Tickers) {
                set(it.symbol, symbol)
            }
        } catch (_: SQLIntegrityConstraintViolationException) {
            throw TickerExistsError()
        }

        return if (rows == 0) {
             null
        } else {
            Database.connect(globalDataSource).sequenceOf(Tickers).find { it.symbol eq symbol }?.let {
                TickerResult(id = it.id, symbol = it.symbol)
            }
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

    fun addTag(name: String): Int? {
        return try {
            val result = Database.connect(globalDataSource).insert(Tags) { set(it.name, name) }
            if (result > 0) {
                Database.connect(globalDataSource).sequenceOf(Tags)
                    .find { it.name eq name }?.id
            } else {
                null
            }
        } catch (_: SQLException) {
            null
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

    // TODO: Should client argument be moved up like globalDataSource?
    // FIXME: Non-existent results for query "ASBSD" should result in empty array not exception thrown
    fun lookup(query: String, client: EtradeClient): List<SearchTickerResponse> {
        return try {
            client.market.lookup(query)?.mapNotNull {
                it.symbol?.let { symbol ->
                    Regex(query).find(symbol)?.let { match ->
                        SearchTickerResponse(symbol, it.description, it.type, SearchRange(match))
                    }
                }
            } ?: listOf()
        } catch (_: Exception) {
            listOf()
        }
    }

}

@Serializable
data class SearchTickerResponse(
    val symbol: String,
    val description: String? = null,
    val symbolType: String? = null,
    val highlighted: SearchRange)

// Basically an IntRange which isn’t serlializable
@Serializable
data class SearchRange(
    val start: Int,
    val end: Int
) {
    constructor(match: MatchResult): this(
        match.range.first,
        match.range.last
    )
}

@Serializable
data class UpdateSymbolRequest(val symbol: String)

@Serializable
data class AddNewTagRequest(val name: String, val tickerId: Int? = null)

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

class TickerExistsError: Error("Ticker symbol already exists")