package so.alaz.strata.api.storage

import org.jetbrains.annotations.ApiStatus
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * A per-player persistent key-value store backed by a [StorageProvider], keyed by player [UUID] and
 * a string sub-key. Obtain one from [StorageProvider.playerData]; the backing table is created on
 * first use and namespaced per provider. Use it for small bits of player state (preferences,
 * counters, flags) without writing a schema.
 *
 * Every operation runs off the main thread. The typed accessors are conveniences over the string
 * [get]/[put]; a value that cannot be parsed falls back to the supplied default.
 */
@ApiStatus.Experimental
public interface PlayerDataStore {

    /** The stored value for [player] under [key], or `null` if absent. */
    public fun get(player: UUID, key: String): CompletableFuture<String?>

    /** Stores [value] for [player] under [key], inserting or replacing. */
    public fun put(player: UUID, key: String, value: String): CompletableFuture<Void>

    /** Removes [key] for [player] if present. */
    public fun delete(player: UUID, key: String): CompletableFuture<Void>

    /** Every key-value pair stored for [player]. */
    public fun getAll(player: UUID): CompletableFuture<Map<String, String>>

    /** Removes all data for [player]. */
    public fun clear(player: UUID): CompletableFuture<Void>

    // --- typed conveniences ----------------------------------------------------------------------

    public fun getInt(player: UUID, key: String, fallback: Int): CompletableFuture<Int> =
        get(player, key).thenApply { it?.toIntOrNull() ?: fallback }

    public fun getLong(player: UUID, key: String, fallback: Long): CompletableFuture<Long> =
        get(player, key).thenApply { it?.toLongOrNull() ?: fallback }

    public fun getDouble(player: UUID, key: String, fallback: Double): CompletableFuture<Double> =
        get(player, key).thenApply { it?.toDoubleOrNull() ?: fallback }

    public fun getBoolean(player: UUID, key: String, fallback: Boolean): CompletableFuture<Boolean> =
        get(player, key).thenApply { it?.toBooleanStrictOrNull() ?: fallback }

    public fun put(player: UUID, key: String, value: Int): CompletableFuture<Void> =
        put(player, key, value.toString())

    public fun put(player: UUID, key: String, value: Long): CompletableFuture<Void> =
        put(player, key, value.toString())

    public fun put(player: UUID, key: String, value: Double): CompletableFuture<Void> =
        put(player, key, value.toString())

    public fun put(player: UUID, key: String, value: Boolean): CompletableFuture<Void> =
        put(player, key, value.toString())
}
