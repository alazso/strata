package so.alaz.strata.hook

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import so.alaz.strata.api.hook.PermissionHook
import java.time.Duration

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

    @Test
    fun bukkitHookCannotWriteAndReturnsFalse() {
        val player = mockk<Player>(relaxed = true)
        assertThat(bukkit.addGroup(player, "vip")).isFalse()
        assertThat(bukkit.addTempGroup(player, "vip", Duration.ofDays(7))).isFalse()
        assertThat(bukkit.removeGroup(player, "vip")).isFalse()
        assertThat(bukkit.setPermission(player, "some.node", true)).isFalse()
        assertThat(bukkit.unsetPermission(player, "some.node")).isFalse()
    }

    @Test
    fun luckPermsWritesDegradeToFalseWithoutRunningPlugin() {
        val hook = LuckPermsPermissionHook()
        val player = mockk<Player>(relaxed = true)
        every { player.uniqueId } returns java.util.UUID.randomUUID()
        // No LuckPerms running: every write must degrade to false rather than throwing.
        assertThat(hook.addGroup(player, "vip")).isFalse()
        assertThat(hook.addTempGroup(player, "vip", Duration.ofHours(1))).isFalse()
        assertThat(hook.removeGroup(player, "vip")).isFalse()
        assertThat(hook.setPermission(player, "some.node", true)).isFalse()
        assertThat(hook.unsetPermission(player, "some.node")).isFalse()
    }
}
