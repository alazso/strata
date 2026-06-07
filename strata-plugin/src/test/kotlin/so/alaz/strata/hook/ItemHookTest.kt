package so.alaz.strata.hook

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Test
import so.alaz.strata.api.hook.ItemHook

/**
 * The provider APIs (ItemsAdder, Oraxen, Nexo) are on the test classpath, but no Bukkit server and
 * none of the backing plugins are running. Each hook must therefore report unavailable and degrade
 * to `null`/`false` rather than throwing, exactly as it would on a server without that plugin.
 */
class ItemHookTest {

    private val hooks: List<Pair<String, ItemHook>> = listOf(
        "ItemsAdder" to ItemsAdderItemHook(),
        "Oraxen" to OraxenItemHook(),
        "Nexo" to NexoItemHook(),
        "HeadDatabase" to HeadDatabaseItemHook(),
    )

    @Test
    fun reportExpectedNames() {
        hooks.forEach { (name, hook) -> assertThat(hook.name()).isEqualTo(name) }
    }

    @Test
    fun degradeToUnavailableWithoutRunningPlugin() {
        hooks.forEach { (_, hook) -> assertThat(hook.isAvailable()).isFalse() }
    }

    @Test
    fun lookupsDoNotThrowWhenUnavailable() {
        val item = mockk<ItemStack>(relaxed = true)
        hooks.forEach { (_, hook) ->
            assertThat(hook.itemId(item)).isNull()
            assertThat(hook.createItem("namespace:unknown")).isNull()
            assertThat(hook.isCustomItem(item)).isFalse()
        }
    }
}
