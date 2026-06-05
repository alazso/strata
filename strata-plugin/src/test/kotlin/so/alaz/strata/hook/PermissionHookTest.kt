package so.alaz.strata.hook

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import so.alaz.strata.api.hook.PermissionHook

class PermissionHookTest {

    private val bukkit: PermissionHook = BukkitPermissionHook()

    @Test
    fun bukkitHookIsAlwaysAvailable() {
        assertThat(bukkit.isAvailable()).isTrue()
        assertThat(bukkit.name()).isEqualTo("Bukkit")
    }

    @Test
    fun bukkitHasDelegatesToNativeCheck() {
        val player = mockk<Player>()
        every { player.hasPermission("strata.use") } returns true
        every { player.hasPermission("strata.admin") } returns false
        assertThat(bukkit.has(player, "strata.use")).isTrue()
        assertThat(bukkit.has(player, "strata.admin")).isFalse()
    }

    @Test
    fun bukkitDoesNotKnowGroupsOrMeta() {
        val player = mockk<Player>()
        assertThat(bukkit.primaryGroup(player)).isNull()
        assertThat(bukkit.groups(player)).isEmpty()
        assertThat(bukkit.prefix(player)).isNull()
        assertThat(bukkit.meta(player, "weight")).isNull()
    }

    @Test
    fun luckPermsHookDegradesGracefullyWithoutRunningPlugin() {
        // The LuckPerms API is on the test classpath, but no LuckPerms plugin is running,
        // so the hook must report unavailable rather than throwing.
        assertThat(LuckPermsPermissionHook().isAvailable()).isFalse()
    }
}
