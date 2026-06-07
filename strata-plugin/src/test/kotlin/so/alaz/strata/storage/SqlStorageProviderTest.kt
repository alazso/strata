package so.alaz.strata.storage

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import so.alaz.strata.api.storage.Migration
import so.alaz.strata.api.storage.StorageConfig
import java.nio.file.Path
import java.sql.Connection

class SqlStorageProviderTest {

    private fun migration(v: Int, sql: String): Migration = object : Migration {
        override fun version(): Int = v
        override fun description(): String = "test $v"
        override fun up(connection: Connection) {
            connection.createStatement().use { it.executeUpdate(sql) }
        }
    }

    @Test
    fun migratesTracksVersionAndPersistsData(@TempDir dir: Path) {
        val provider = SqlStorageProvider(StorageConfig.sqlite(dir.resolve("data.db").toString()))
        provider.init().get()
        try {
            provider.migrations().register(migration(1, "CREATE TABLE widget(id INTEGER PRIMARY KEY)"))

            assertThat(provider.migrations().migrate().get()).isEqualTo(1)
            assertThat(provider.migrations().currentVersion().get()).isEqualTo(1)
            // Re-running applies nothing.
            assertThat(provider.migrations().migrate().get()).isEqualTo(0)

            provider.dataSource().connection.use { conn ->
                conn.createStatement().use { it.executeUpdate("INSERT INTO widget(id) VALUES (42)") }
                conn.createStatement().use { st ->
                    st.executeQuery("SELECT id FROM widget").use { rs ->
                        assertThat(rs.next()).isTrue()
                        assertThat(rs.getInt(1)).isEqualTo(42)
                    }
                }
            }
        } finally {
            provider.shutdown().get()
        }
    }

    @Test
    fun namespaceDeterminesVersionTableName(@TempDir dir: Path) {
        // A namespaced provider tracks its version in <namespace>_schema_version (sanitized), not the
        // shared default, so two plugins on one database never collide.
        val provider = SqlStorageProvider(
            StorageConfig.sqlite(dir.resolve("ns.db").toString()).withNamespace("MyPlugin"),
        )
        provider.init().get()
        try {
            provider.migrations().migrate().get() // creates the version table
            provider.dataSource().connection.use { conn ->
                val tables = buildList {
                    conn.createStatement().use { st ->
                        st.executeQuery("SELECT name FROM sqlite_master WHERE type='table'").use { rs ->
                            while (rs.next()) add(rs.getString(1))
                        }
                    }
                }
                assertThat(tables).contains("myplugin_schema_version")
                assertThat(tables).doesNotContain("strata_schema_version")
            }
        } finally {
            provider.shutdown().get()
        }
    }

    @Test
    fun secondMigrationAppliesOnlyTheNewOne(@TempDir dir: Path) {
        val provider = SqlStorageProvider(StorageConfig.sqlite(dir.resolve("data2.db").toString()))
        provider.init().get()
        try {
            provider.migrations().register(migration(1, "CREATE TABLE a(x INTEGER)"))
            provider.migrations().migrate().get()

            provider.migrations().register(migration(2, "CREATE TABLE b(y INTEGER)"))
            assertThat(provider.migrations().migrate().get()).isEqualTo(1)
            assertThat(provider.migrations().currentVersion().get()).isEqualTo(2)
        } finally {
            provider.shutdown().get()
        }
    }
}
