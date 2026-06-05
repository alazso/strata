package so.alaz.strata.hook

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VaultEconomyHookTest {

    @Test
    fun degradesGracefullyWithoutAVaultRegistration() {
        // VaultAPI is on the test classpath, but no Bukkit server / economy registration exists,
        // so the hook must report unavailable rather than throwing.
        val hook = VaultEconomyHook()
        assertThat(hook.name()).isEqualTo("Vault")
        assertThat(hook.isAvailable()).isFalse()
    }
}
