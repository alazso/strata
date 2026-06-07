package so.alaz.strata.storage

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import so.alaz.strata.api.storage.StorageConfig
import java.nio.file.Path
import java.util.UUID

class KeyValueStoreTest {

    @Test
    fun keyValueRoundTripsUpdatesAndDeletes(@TempDir dir: Path) {
        val provider = SqlStorageProvider(StorageConfig.sqlite(dir.resolve("kv.db").toString()))
        provider.init().get()
        try {
            val kv = provider.keyValue("settings")
            kv.put("greeting", "hi").get()
            assertThat(kv.get("greeting").get()).isEqualTo("hi")
            assertThat(kv.contains("greeting").get()).isTrue()

            kv.put("greeting", "hello").get() // upsert path: update existing
            assertThat(kv.get("greeting").get()).isEqualTo("hello")

            kv.put("count", 5).get()
            assertThat(kv.getInt("count", 0).get()).isEqualTo(5)
            assertThat(kv.getInt("missing", 42).get()).isEqualTo(42)
            assertThat(kv.keys().get()).containsExactlyInAnyOrder("greeting", "count")

            kv.delete("greeting").get()
            assertThat(kv.get("greeting").get()).isNull()
            assertThat(kv.contains("greeting").get()).isFalse()
        } finally {
            provider.shutdown().get()
        }
    }

    @Test
    fun playerDataIsolatesByUuid(@TempDir dir: Path) {
        val provider = SqlStorageProvider(StorageConfig.sqlite(dir.resolve("pd.db").toString()))
        provider.init().get()
        try {
            val pd = provider.playerData("profile")
            val a = UUID.randomUUID()
            val b = UUID.randomUUID()
            pd.put(a, "kills", 3).get()
            pd.put(a, "name", "Hunter").get()
            pd.put(b, "kills", 99).get()

            assertThat(pd.getInt(a, "kills", 0).get()).isEqualTo(3)
            assertThat(pd.getInt(b, "kills", 0).get()).isEqualTo(99)
            assertThat(pd.getAll(a).get()).containsEntry("kills", "3").containsEntry("name", "Hunter")

            pd.clear(a).get()
            assertThat(pd.getAll(a).get()).isEmpty()
            assertThat(pd.getInt(b, "kills", 0).get()).isEqualTo(99) // other player untouched
        } finally {
            provider.shutdown().get()
        }
    }
}
