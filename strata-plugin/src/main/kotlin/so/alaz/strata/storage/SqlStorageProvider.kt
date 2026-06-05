package so.alaz.strata.storage

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import so.alaz.strata.api.storage.Backend
import so.alaz.strata.api.storage.MigrationRunner
import so.alaz.strata.api.storage.StorageConfig
import so.alaz.strata.api.storage.StorageProvider
import so.alaz.strata.api.storage.StrataExposed
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.sql.DataSource

/**
 * [StorageProvider] backed by HikariCP over JDBC. Works for SQLite (file-based, default) and
 * MySQL/MariaDB. All blocking work runs on a single dedicated daemon thread, so the public futures
 * never block a server thread and database access is naturally serialized.
 */
internal class SqlStorageProvider(private val config: StorageConfig) : StorageProvider {

    @Volatile
    private var dataSource: HikariDataSource? = null

    private val executor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "strata-storage-${config.poolName}").apply { isDaemon = true }
    }

    private val runner = JdbcMigrationRunner(this, executor)

    override fun backend(): Backend = config.backend

    override fun init(): CompletableFuture<Void> = CompletableFuture.runAsync({
        if (dataSource != null) return@runAsync
        if (config.backend == Backend.SQLITE) {
            val path = config.jdbcUrl.removePrefix("jdbc:sqlite:")
            File(path).absoluteFile.parentFile?.mkdirs()
        }
        val hikari = HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            username = config.username
            password = config.password
            maximumPoolSize = config.maxPoolSize
            poolName = config.poolName
        }
        dataSource = HikariDataSource(hikari)
    }, executor)

    override fun shutdown(): CompletableFuture<Void> = CompletableFuture.runAsync({
        StrataExposed.invalidate(this)
        dataSource?.close()
        dataSource = null
    }, executor)

    override fun dataSource(): DataSource =
        dataSource ?: error("StorageProvider not initialised; call init() first")

    override fun migrations(): MigrationRunner = runner
}
