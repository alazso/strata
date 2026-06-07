package so.alaz.strata.storage

import so.alaz.strata.api.storage.PlayerDataStore
import so.alaz.strata.api.storage.StorageProvider
import java.sql.Connection
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

/**
 * [PlayerDataStore] over a JDBC table `[table] (uuid, k, v)` with a composite primary key, sharing
 * the provider's pool and single-thread executor. The table is created on first use. Writes use the
 * same portable update-then-insert upsert as [JdbcKeyValueStore].
 */
internal class JdbcPlayerDataStore(
    private val storage: StorageProvider,
    private val executor: ExecutorService,
    private val table: String,
) : PlayerDataStore {

    @Volatile
    private var ensured = false

    override fun get(player: UUID, key: String): CompletableFuture<String?> = query { conn ->
        conn.prepareStatement("SELECT v FROM $table WHERE uuid = ? AND k = ?").use { ps ->
            ps.setString(1, player.toString())
            ps.setString(2, key)
            ps.executeQuery().use { rs -> if (rs.next()) rs.getString(1) else null }
        }
    }

    override fun put(player: UUID, key: String, value: String): CompletableFuture<Void> = run { conn ->
        upsert(conn, player, key, value)
    }

    override fun delete(player: UUID, key: String): CompletableFuture<Void> = run { conn ->
        conn.prepareStatement("DELETE FROM $table WHERE uuid = ? AND k = ?").use { ps ->
            ps.setString(1, player.toString())
            ps.setString(2, key)
            ps.executeUpdate()
        }
    }

    override fun getAll(player: UUID): CompletableFuture<Map<String, String>> = query { conn ->
        conn.prepareStatement("SELECT k, v FROM $table WHERE uuid = ?").use { ps ->
            ps.setString(1, player.toString())
            ps.executeQuery().use { rs ->
                buildMap { while (rs.next()) put(rs.getString(1), rs.getString(2)) }
            }
        }
    }

    override fun clear(player: UUID): CompletableFuture<Void> = run { conn ->
        conn.prepareStatement("DELETE FROM $table WHERE uuid = ?").use { ps ->
            ps.setString(1, player.toString())
            ps.executeUpdate()
        }
    }

    private fun upsert(conn: Connection, player: UUID, key: String, value: String) {
        if (update(conn, value, player, key) > 0) return
        runCatching {
            conn.prepareStatement("INSERT INTO $table (uuid, k, v) VALUES (?, ?, ?)").use { ps ->
                ps.setString(1, player.toString())
                ps.setString(2, key)
                ps.setString(3, value)
                ps.executeUpdate()
            }
        }.onFailure { update(conn, value, player, key) }
    }

    private fun update(conn: Connection, value: String, player: UUID, key: String): Int =
        conn.prepareStatement("UPDATE $table SET v = ? WHERE uuid = ? AND k = ?").use { ps ->
            ps.setString(1, value)
            ps.setString(2, player.toString())
            ps.setString(3, key)
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
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS $table " +
                    "(uuid VARCHAR(36) NOT NULL, k VARCHAR(255) NOT NULL, v TEXT NOT NULL, PRIMARY KEY (uuid, k))",
            )
        }
        ensured = true
    }
}
