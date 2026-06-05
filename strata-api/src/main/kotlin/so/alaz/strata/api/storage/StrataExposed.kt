package so.alaz.strata.api.storage

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

/**
 * **Kotlin-only, additive** ergonomics layer over [StorageProvider] using Exposed + coroutines.
 * Java consumers ignore this entirely and use [StorageProvider.dataSource] with plain JDBC.
 *
 * An Exposed [Database] is created lazily per provider over its pooled [DataSource][javax.sql.DataSource]
 * and reused. The Strata plugin invalidates the binding on `shutdown`.
 */
@ApiStatus.Experimental
public object StrataExposed {

    private val databases = ConcurrentHashMap<StorageProvider, Database>()

    /** The Exposed [Database] bound to [storage] (created on first use). */
    @JvmStatic
    public fun database(storage: StorageProvider): Database =
        databases.computeIfAbsent(storage) { Database.connect(it.dataSource()) }

    /** Drops the cached [Database] for [storage]. Called by the plugin on shutdown. */
    public fun invalidate(storage: StorageProvider) {
        databases.remove(storage)
    }

    /** Runs [statement] in a blocking Exposed transaction on [storage]'s database. */
    @JvmStatic
    public fun <T> transaction(storage: StorageProvider, statement: JdbcTransaction.() -> T): T =
        transaction(db = database(storage), statement = statement)

    /**
     * Runs [statement] in a suspended Exposed transaction, dispatching the blocking JDBC work onto
     * [dispatcher] (defaults to [Dispatchers.IO]).
     */
    public suspend fun <T> suspendTransaction(
        storage: StorageProvider,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        statement: suspend JdbcTransaction.() -> T,
    ): T = newSuspendedTransaction(context = dispatcher, db = database(storage), statement = statement)
}

/** Kotlin sugar: `storage.exposed()`. */
public fun StorageProvider.exposed(): Database = StrataExposed.database(this)

/** Kotlin sugar: blocking `storage.transaction { ... }`. */
public fun <T> StorageProvider.transaction(statement: JdbcTransaction.() -> T): T =
    StrataExposed.transaction(this, statement)

/** Kotlin sugar: suspended `storage.suspendTransaction { ... }`. */
public suspend fun <T> StorageProvider.suspendTransaction(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    statement: suspend JdbcTransaction.() -> T,
): T = StrataExposed.suspendTransaction(this, dispatcher, statement)
