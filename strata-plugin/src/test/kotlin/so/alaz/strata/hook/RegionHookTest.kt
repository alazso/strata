package so.alaz.strata.hook

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.Location
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test

/**
 * The WorldGuard/WorldEdit APIs are on the test classpath, but no Bukkit server and no WorldGuard
 * are running. The hook must report unavailable and degrade safely: empty/false for reads, and
 * `true` for [WorldGuardRegionHook.canBuild] (an unavailable region provider must not block actions).
 */
class RegionHookTest {

    private val hook = WorldGuardRegionHook()

    @Test
    fun reportsNameAndUnavailability() {
        assertThat(hook.name()).isEqualTo("WorldGuard")
        assertThat(hook.isAvailable()).isFalse()
    }

    @Test
    fun readsDegradeToEmptyWhenUnavailable() {
        val location = mockk<Location>(relaxed = true)
        assertThat(hook.regionsAt(location)).isEmpty()
        assertThat(hook.isInRegion(location, "spawn")).isFalse()
    }

    @Test
    fun canBuildDegradesToAllowedWhenUnavailable() {
        val location = mockk<Location>(relaxed = true)
        val player = mockk<Player>(relaxed = true)
        assertThat(hook.canBuild(player, location)).isTrue()
    }
}
