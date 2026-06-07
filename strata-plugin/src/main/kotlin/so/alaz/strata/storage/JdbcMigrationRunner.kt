package so.alaz.strata.storage

import so.alaz.strata.api.storage.Migration
import so.alaz.strata.api.storage.MigrationRunner
import so.alaz.strata.api.storage.StorageProvider
import java.sql.Connection
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

/**
 * Applies [Migration]s in version order, tracking the highest applied version in a single-row
 * version table named [versionTable] (namespaced per consumer so plugins sharing one database do not
 * collide). Each migration runs in its own transaction; a failure rolls that migration back and
 * aborts the run.
 */
internal class JdbcMigrationRunner(
    private val storage: StorageProvider,
    private val executor: ExecutorService,
    private val versionTable: String,
) : MigrationRunner {

    private val migrations = sortedMapOf<Int, Migration>()

    override fun register(migration: Migration): MigrationRunner {
        check(!migrations.containsKey(migration.version())) {
            "Duplicate migration version ${migration.version()}"
        }
        migrations[migration.version()] = migration
        return this
    }

    override fun registerAll(migrations: Collection<Migration>): MigrationRunner {
        migrations.forEach { register(it) }
        return this
    }

    override fun currentVersion(): CompletableFuture<Int> = CompletableFuture.supplyAsync({
        storage.dataSource().connection.use { conn ->
            ensureVersionTable(conn)
            readVersion(conn)
        }
    }, executor)

    override fun migrate(): CompletableFuture<Int> = CompletableFuture.supplyAsync({
        storage.dataSource().connection.use { conn ->
            ensureVersionTable(conn)
            applyPending(conn)
        }
    }, executor)

    private fun applyPending(conn: Connection): Int {
        val current = readVersion(conn)
        var applied = 0
        val previousAutoCommit = conn.autoCommit
        conn.autoCommit = false
        try {
            for ((version, migration) in migrations) {
                if (version <= current) continue
                migration.up(conn)
                writeVersion(conn, version)
                conn.commit()
                applied++
            }
        } catch (ex: Exception) {
            runCatching { conn.rollback() }
            throw RuntimeException("Migration failed at version > $current", ex)
        } finally {
            conn.autoCommit = previousAutoCommit
        }
        return applied
    }

    private fun ensureVersionTable(conn: Connection) {
        conn.createStatement().use { st ->
            st.executeUpdate("CREATE TABLE IF NOT EXISTS $versionTable (version INTEGER NOT NULL)")
        }
        conn.createStatement().use { st ->
            st.executeQuery("SELECT COUNT(*) FROM $versionTable").use { rs ->
                rs.next()
                if (rs.getInt(1) == 0) {
                    st.executeUpdate("INSERT INTO $versionTable (version) VALUES (0)")
                }
            }
        }
    }

    private fun readVersion(conn: Connection): Int {
        conn.createStatement().use { st ->
            st.executeQuery("SELECT version FROM $versionTable LIMIT 1").use { rs ->
                return if (rs.next()) rs.getInt(1) else 0
            }
        }
    }

    private fun writeVersion(conn: Connection, version: Int) {
        conn.prepareStatement("UPDATE $versionTable SET version = ?").use { ps ->
            ps.setInt(1, version)
            ps.executeUpdate()
        }
    }
}
