package so.alaz.strata.api.storage

import org.jetbrains.annotations.ApiStatus

/** Supported storage backends. SQLite is the zero-config, file-based default. */
@ApiStatus.Experimental
public enum class Backend {
    SQLITE,
    MYSQL,
    MARIADB,
}
