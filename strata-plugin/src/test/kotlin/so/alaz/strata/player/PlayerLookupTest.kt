package so.alaz.strata.player

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Without a running server, the synchronous cache peeks must degrade to `null` (the Bukkit online
 * lookups are wrapped) rather than throwing. The async Mojang-backed paths need a server and are
 * compile-verified.
 */
class PlayerLookupTest {

    @Test
    fun emptyCachePeeksReturnNullWithoutServer() {
        val lookup = DefaultPlayerLookup()
        assertThat(lookup.cachedName(UUID.randomUUID())).isNull()
        assertThat(lookup.cachedUuid("Nobody")).isNull()
    }
}
