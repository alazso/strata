package so.alaz.strata.api.storage

import org.jetbrains.annotations.ApiStatus
import java.util.concurrent.CompletableFuture

/**
 * Registers and applies [Migration]s for a [StorageProvider]. Registration is synchronous and
 * chainable; [migrate] runs off-thread and returns the number of migrations newly applied.
 */
@ApiStatus.Experimental
public interface MigrationRunner {

    /** Registers a migration. Returns `this` for chaining. */
    public fun register(migration: Migration): MigrationRunner

    /** Registers several migrations at once. Returns `this` for chaining. */
    public fun registerAll(migrations: Collection<Migration>): MigrationRunner

    /** Applies all pending migrations in version order. Completes with the count applied. */
    public fun migrate(): CompletableFuture<Int>

    /** The highest applied schema version, or 0 if none. */
    public fun currentVersion(): CompletableFuture<Int>
}
