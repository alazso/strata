package so.alaz.strata.api.storage

import org.jetbrains.annotations.ApiStatus
import java.util.concurrent.CompletableFuture
import javax.sql.DataSource

/**
 * Owns the connection lifecycle and pooling for one plugin's database, plus its migration runner.
 *
 * Obtain one from `StrataApi.storage().create(config)`, call [init] once during enable, and
 * [shutdown] during disable. The pooled [DataSource] is the universal access primitive — Java
 * consumers use it directly with JDBC; a future Kotlin layer will add Exposed/coroutine helpers on
 * top of the same provider.
 */
@ApiStatus.Experimental
public interface StorageProvider {

    /** The backend this provider was configured for. */
    public fun backend(): Backend

    /** Opens the connection pool (and creates the SQLite file/dirs if needed). Idempotent. */
    public fun init(): CompletableFuture<Void>

    /** Closes the pool and releases resources. Idempotent. */
    public fun shutdown(): CompletableFuture<Void>

    /** The pooled data source. Only valid between [init] and [shutdown]. */
    public fun dataSource(): DataSource

    /** This provider's migration runner. */
    public fun migrations(): MigrationRunner

    /**
     * A simple string key-value store named [name], backed by this provider. The table is created on
     * first use and namespaced per provider. Repeated calls with the same [name] return the same
     * store. Use it to persist small data without writing a schema.
     */
    public fun keyValue(name: String): KeyValueStore

    /**
     * A per-player key-value store named [name], backed by this provider. The table is created on
     * first use and namespaced per provider. Repeated calls with the same [name] return the same
     * store.
     */
    public fun playerData(name: String): PlayerDataStore
}
