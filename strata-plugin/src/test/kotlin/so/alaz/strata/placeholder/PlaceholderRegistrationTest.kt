package so.alaz.strata.placeholder

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThatCode
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test

/**
 * The PAPI and MiniPlaceholders APIs are on the test classpath but neither plugin is running, so
 * register/unregister must wire what they can and swallow the rest rather than throwing.
 */
class PlaceholderRegistrationTest {

    @Test
    fun registerAndUnregisterDoNotThrowWithoutBackends() {
        val plugin = mockk<Plugin>(relaxed = true)
        every { plugin.name } returns "TestPlugin"

        val registration = DefaultPlaceholderRegistration(plugin)
            .add("balance") { _ -> "100" }
            .addGlobal("online") { "5" }

        assertThatCode {
            registration.register()
            registration.unregister()
        }.doesNotThrowAnyException()
    }
}
