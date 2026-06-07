package so.alaz.strata.api.storage

import org.jetbrains.annotations.ApiStatus
import java.util.concurrent.CompletableFuture

/**
 * A simple persistent string-to-string store backed by a [StorageProvider], for data that does not
 * justify its own schema and migrations (settings, counters, flags, small JSON blobs). Obtain one
 * from [StorageProvider.keyValue]; the backing table is created on first use and namespaced per
 * provider so plugins sharing a database do not collide.
 *
 * Every operation runs off the main thread and completes on a pool thread, so schedule any follow-up
 * that touches the server back on the right thread yourself. The typed accessors are conveniences
 * over the string [get]/[put]; a value that cannot be parsed falls back to the supplied default.
 */
@ApiStatus.Experimental
public interface KeyValueStore {

    /** The stored value for [key], or `null` if absent. */
    public fun get(key: String): CompletableFuture<String?>

    /** Stores [value] under [key], inserting or replacing. */
    public fun put(key: String, value: String): CompletableFuture<Void>

    /** Removes [key] if present. */
    public fun delete(key: String): CompletableFuture<Void>

    /** `true` if [key] has a stored value. */
    public fun contains(key: String): CompletableFuture<Boolean> = get(key).thenApply { it != null }

    /** All keys currently stored. */
    public fun keys(): CompletableFuture<List<String>>

    // --- typed conveniences ----------------------------------------------------------------------

    public fun getInt(key: String, fallback: Int): CompletableFuture<Int> =
        get(key).thenApply { it?.toIntOrNull() ?: fallback }

    public fun getLong(key: String, fallback: Long): CompletableFuture<Long> =
        get(key).thenApply { it?.toLongOrNull() ?: fallback }

    public fun getDouble(key: String, fallback: Double): CompletableFuture<Double> =
        get(key).thenApply { it?.toDoubleOrNull() ?: fallback }

    public fun getBoolean(key: String, fallback: Boolean): CompletableFuture<Boolean> =
        get(key).thenApply { it?.toBooleanStrictOrNull() ?: fallback }

    public fun put(key: String, value: Int): CompletableFuture<Void> = put(key, value.toString())

    public fun put(key: String, value: Long): CompletableFuture<Void> = put(key, value.toString())

    public fun put(key: String, value: Double): CompletableFuture<Void> = put(key, value.toString())

    public fun put(key: String, value: Boolean): CompletableFuture<Void> = put(key, value.toString())
}
