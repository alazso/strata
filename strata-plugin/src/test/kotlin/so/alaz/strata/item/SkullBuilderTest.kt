package so.alaz.strata.item

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import so.alaz.strata.api.item.SkullBuilder
import java.util.Base64

/**
 * Only the pure URL-to-texture encoding is unit-tested here; the head-building methods need a running
 * server (Bukkit profiles and item meta) and are compile-verified, as elsewhere in the project.
 */
class SkullBuilderTest {

    @Test
    fun base64FromUrlEncodesTextureJson() {
        val url = "http://textures.minecraft.net/texture/abc123"
        val decoded = String(Base64.getDecoder().decode(SkullBuilder.base64FromUrl(url)), Charsets.UTF_8)
        assertThat(decoded).isEqualTo("""{"textures":{"SKIN":{"url":"$url"}}}""")
    }
}
