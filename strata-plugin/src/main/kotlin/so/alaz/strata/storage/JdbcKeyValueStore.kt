package so.alaz.strata.storage

import so.alaz.strata.api.storage.KeyValueStore
import so.alaz.strata.api.storage.StorageProvider
import java.sql.Connection
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

/**
 * [KeyValueStore] over a JDBC table `[table] (k, v)`, sharing the provider's pool and single-thread
 * executor. The table is created on first use. Writes use a portable update-then-insert upsert
 * (works on SQLite, MySQL/MariaDB, and PostgreSQL); the per-provider executor serializes writes, and
 * a lost insert race against another server falls back to an update.
 */
internal class JdbcKeyValueStore(
    private val storage: StorageProvider,
    private val executor: ExecutorService,
    private val table: String,
) : KeyValueStore {

    @Volatile
    private var ensured = false

    override fun get(key: String): CompletableFuture<String?> = query { conn ->
        conn.prepareStatement("SELECT v FROM $table WHERE k = ?").use { ps ->
            ps.setString(1, key)
            ps.executeQuery().use { rs -> if (rs.next()) rs.getString(1) else null }
        }
    }

    override fun put(key: String, value: String): CompletableFuture<Void> = run { conn ->
        upsert(conn, key, value)
    }

    override fun delete(key: String): CompletableFuture<Void> = run { conn ->
        conn.prepareStatement("DELETE FROM $table WHERE k = ?").use { ps ->
            ps.setString(1, key)
            ps.executeUpdate()
        }
    }

    override fun keys(): CompletableFuture<List<String>> = query { conn ->
        conn.createStatement().use { st ->
            st.executeQuery("SELECT k FROM $table").use { rs ->
                buildList { while (rs.next()) add(rs.getString(1)) }
            }
        }
    }

    private fun upsert(conn: Connection, key: String, value: String) {
        if (update(conn, value, key) > 0) return
        runCatching {
            conn.prepareStatement("INSERT INTO $table (k, v) VALUES (?, ?)").use { ps ->
                ps.setString(1, key)
                ps.setString(2, value)
                ps.executeUpdate()
            }
        }.onFailure { update(conn, value, key) } // another writer inserted first; update instead
    }

    private fun update(conn: Connection, value: String, key: String): Int =
        conn.prepareStatement("UPDATE $table SET v = ? WHERE k = ?").use { ps ->
            ps.setString(1, value)
            ps.setString(2, key)
            ps.executeUpdate()
        }

    private fun <T> query(block: (Connection) -> T): CompletableFuture<T> =
        CompletableFuture.supplyAsync({ withConnection(block) }, executor)

    private fun run(block: (Connection) -> Unit): CompletableFuture<Void> =
        CompletableFuture.runAsync({ withConnection(block) }, executor)

    private fun <T> withConnection(block: (Connection) -> T): T =
        storage.dataSource().connection.use { conn ->
            ensure(conn)
            block(conn)
        }

    private fun ensure(conn: Connection) {
        if (ensured) return
        conn.createStatement().use { st ->
            st.executeUpdate("CREATE TABLE IF NOT EXISTS $table (k VARCHAR(255) NOT NULL PRIMARY KEY, v TEXT NOT NULL)")
        }
        ensured = true
    }
}
