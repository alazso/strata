package so.alaz.strata.storage

import so.alaz.strata.api.storage.StorageConfig
import so.alaz.strata.api.storage.StorageFactory
import so.alaz.strata.api.storage.StorageProvider

internal class DefaultStorageFactory : StorageFactory {
    override fun create(config: StorageConfig): StorageProvider = SqlStorageProvider(config)
}
