package so.alaz.strata.api.storage

import org.jetbrains.annotations.ApiStatus
import java.sql.Connection

/**
 * A single forward schema change, identified by an increasing [version]. The migration runner
 * applies pending migrations in version order inside a transaction, tracking the highest applied
 * version in a `strata_schema_version` table.
 *
 * Written against plain JDBC [Connection] so both Java and Kotlin consumers author migrations the
 * same way; do not commit/rollback inside [up] — the runner owns the transaction.
 */
@ApiStatus.Experimental
public interface Migration {

    /** Strictly increasing version number. Must be unique within a runner. */
    public fun version(): Int

    /** Human-readable summary, surfaced in logs and debug output. */
    public fun description(): String

    /** Applies the change. Throwing aborts and rolls back the transaction. */
    @Throws(Exception::class)
    public fun up(connection: Connection)
}
