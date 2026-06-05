package so.alaz.strata.metrics

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Test
import java.time.Duration

class FeatureFlagsTest {

    @Test
    fun returnsLocalDefaultsWithoutRemote() {
        val flags = DefaultFeatureFlags(linkedMapOf("on" to true, "name" to "zstd", "cap" to 10.0))

        assertThat(flags.isRemote()).isFalse()
        assertThat(flags.getBoolean("on")).isTrue()
        assertThat(flags.getString("name")).isEqualTo("zstd")
        assertThat(flags.getDouble("cap")).isEqualTo(10.0)
        assertThat(flags.getInt("cap")).isEqualTo(10)
        assertThat(flags.isDefined("on")).isTrue()
        assertThat(flags.keys()).containsExactlyInAnyOrder("on", "name", "cap")
    }

    @Test
    fun undefinedKeyReturnsHardFallback() {
        val flags = DefaultFeatureFlags(emptyMap())
        assertThat(flags.getBoolean("x")).isFalse()
        assertThat(flags.getString("x")).isEmpty()
        assertThat(flags.getDouble("x")).isZero()
        assertThat(flags.getInt("x")).isZero()
        assertThat(flags.isDefined("x")).isFalse()
    }

    @Test
    fun fetchWithoutRemoteCompletesWithDefault() {
        val flags = DefaultFeatureFlags(linkedMapOf("on" to true))
        assertThat(flags.fetchBoolean("on").get()).isTrue()
    }

    @Test
    fun builderFlagMethodsAreFluent() {
        val builder = DefaultMetricsBuilder(mockk<JavaPlugin>(relaxed = true))
        assertThat(builder.defineFlag("a", true)).isSameAs(builder)
        assertThat(builder.defineFlag("b", "x")).isSameAs(builder)
        assertThat(builder.defineFlag("c", 5.0)).isSameAs(builder)
        assertThat(builder.flagTtl(Duration.ofMinutes(5))).isSameAs(builder)
        assertThat(builder.flagAttribute("version", "1.0")).isSameAs(builder)
        assertThat(builder.flagAttribute("beta", true)).isSameAs(builder)
    }
}
