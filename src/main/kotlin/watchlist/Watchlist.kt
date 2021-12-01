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

class Watchlist(val dataSource: DataSource) {

    init {
        globalDataSource = dataSource
    }

    fun list(): List<WatchlistEntity> {
        return Database.connect(globalDataSource).sequenceOf(WatchlistTable).toList()
    }

}

interface WatchlistEntity: Entity<WatchlistEntity> {
    val id: Int
    val ticker: String

    val tags get() = WatchlistTagTable.getList { it.watchlistId eq id }
}

object WatchlistTable: Table<WatchlistEntity>("watchlist") {
    val id = int("id").primaryKey().bindTo { it.id }
    val ticker = varchar("ticker").bindTo { it.ticker }
}

interface TagEntity: Entity<TagEntity> {
    val id: Int
    val name: String
}

object TagTable: Table<TagEntity>("tag") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
}

interface WatchlistTagEntity: Entity<WatchlistTagEntity> {
    val watchlist: WatchlistEntity
    val tag: TagEntity
}

object WatchlistTagTable: Table<WatchlistTagEntity>("watchlist_tag") {
    val watchlistId = int("watchlist_id").references(WatchlistTable) { it.watchlist }
    val tagId = int("tag_id").references(TagTable) { it.tag }
}
