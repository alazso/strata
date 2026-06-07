package so.alaz.strata.api.storage

import org.jetbrains.annotations.ApiStatus

/**
 * Connection settings for a [StorageProvider]. Build via the [Companion] factories rather than the
 * constructor, e.g. `StorageConfig.sqlite(dataFolder + "/data.db")` or
 * `StorageConfig.mysql(host, port, db, user, pass)`.
 */
@ApiStatus.Experimental
public class StorageConfig private constructor(
    public val backend: Backend,
    public val jdbcUrl: String,
    public val username: String?,
    public val password: String?,
    public val maxPoolSize: Int,
    public val poolName: String,
    /**
     * Identifier that namespaces this provider's bookkeeping (the schema-version table) so that
     * multiple plugins pointed at the **same** database do not collide. Set it to your plugin id via
     * [withNamespace] whenever you share a database with other Strata plugins. Defaults to `strata`.
     */
    public val namespace: String = "strata",
) {

    /** Returns a copy of this config with the bookkeeping [namespace] set (e.g. your plugin id). */
    public fun withNamespace(namespace: String): StorageConfig =
        StorageConfig(backend, jdbcUrl, username, password, maxPoolSize, poolName, namespace)

    public companion object {

        /** File-based SQLite. Pool size is fixed at 1 (SQLite is single-writer). */
        @JvmStatic
        @JvmOverloads
        public fun sqlite(filePath: String, poolName: String = "strata-sqlite"): StorageConfig =
            StorageConfig(Backend.SQLITE, "jdbc:sqlite:$filePath", null, null, 1, poolName)

        @JvmStatic
        @JvmOverloads
        public fun mysql(
            host: String,
            port: Int,
            database: String,
            username: String,
            password: String,
            maxPoolSize: Int = 10,
            poolName: String = "strata-mysql",
        ): StorageConfig = StorageConfig(
            Backend.MYSQL, "jdbc:mysql://$host:$port/$database", username, password, maxPoolSize, poolName,
        )

        @JvmStatic
        @JvmOverloads
        public fun mariadb(
            host: String,
            port: Int,
            database: String,
            username: String,
            password: String,
            maxPoolSize: Int = 10,
            poolName: String = "strata-mariadb",
        ): StorageConfig = StorageConfig(
            Backend.MARIADB, "jdbc:mariadb://$host:$port/$database", username, password, maxPoolSize, poolName,
        )

        @JvmStatic
        @JvmOverloads
        public fun postgres(
            host: String,
            port: Int,
            database: String,
            username: String,
            password: String,
            maxPoolSize: Int = 10,
            poolName: String = "strata-postgres",
        ): StorageConfig = StorageConfig(
            Backend.POSTGRES, "jdbc:postgresql://$host:$port/$database", username, password, maxPoolSize, poolName,
        )
    }
}
