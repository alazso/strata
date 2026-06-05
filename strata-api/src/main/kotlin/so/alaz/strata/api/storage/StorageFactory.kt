package so.alaz.strata.api.storage

import org.jetbrains.annotations.ApiStatus

/** Creates [StorageProvider]s from [StorageConfig]. Obtained via `StrataApi.storage()`. */
@ApiStatus.Experimental
public interface StorageFactory {

    /** Builds a provider for [config]. The caller owns its [StorageProvider.init]/shutdown lifecycle. */
    public fun create(config: StorageConfig): StorageProvider
}
