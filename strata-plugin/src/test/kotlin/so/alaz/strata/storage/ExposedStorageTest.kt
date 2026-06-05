package so.alaz.strata.storage

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import so.alaz.strata.api.storage.StorageConfig
import so.alaz.strata.api.storage.StrataExposed
import so.alaz.strata.api.storage.suspendTransaction
import so.alaz.strata.api.storage.transaction
import java.nio.file.Path

class ExposedStorageTest {

    private object Widgets : Table("widgets") {
        val id = integer("id")
    }

    @Test
    fun blockingAndSuspendedTransactionsWork(@TempDir dir: Path) {
        val provider = SqlStorageProvider(StorageConfig.sqlite(dir.resolve("exposed.db").toString()))
        provider.init().get()
        try {
            // Blocking transaction via the Kotlin extension.
            provider.transaction {
                SchemaUtils.create(Widgets)
                Widgets.insert { it[Widgets.id] = 7 }
            }

            // Blocking transaction via the object form.
            val count = StrataExposed.transaction(provider) { Widgets.selectAll().count() }
            assertThat(count).isEqualTo(1L)

            // Suspended transaction (coroutines) via the extension.
            val value = runBlocking {
                provider.suspendTransaction { Widgets.selectAll().first()[Widgets.id] }
            }
            assertThat(value).isEqualTo(7)
        } finally {
            provider.shutdown().get()
        }
    }
}
