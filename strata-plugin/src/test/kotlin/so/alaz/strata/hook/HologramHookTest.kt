package so.alaz.strata.hook

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.Location
import org.junit.jupiter.api.Test

/**
 * The FancyHolograms API is on the test classpath but the plugin is not running, so the hook must
 * report unavailable and degrade every operation to `false` rather than throwing.
 */
class HologramHookTest {

    private val hook = FancyHologramsHook()

    @Test
    fun reportsNameAndUnavailability() {
        assertThat(hook.name()).isEqualTo("FancyHolograms")
        assertThat(hook.isAvailable()).isFalse()
    }

    @Test
    fun operationsDegradeToFalseWhenUnavailable() {
        val location = mockk<Location>(relaxed = true)
        assertThat(hook.create("hud", location, listOf("<red>Hello"))).isFalse()
        assertThat(hook.update("hud", listOf("<gray>Updated"))).isFalse()
        assertThat(hook.exists("hud")).isFalse()
        assertThat(hook.remove("hud")).isFalse()
    }
}
