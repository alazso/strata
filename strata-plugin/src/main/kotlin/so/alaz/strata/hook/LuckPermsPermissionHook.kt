package so.alaz.strata.hook

import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.data.DataMutateResult
import net.luckperms.api.model.user.User
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import net.luckperms.api.node.types.PermissionNode
import net.luckperms.api.query.QueryOptions
import org.bukkit.entity.Player
import so.alaz.strata.api.hook.PermissionHook
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * LuckPerms-backed permission provider. Adds group/prefix/meta resolution on top of Bukkit's native
 * permission checks. All LuckPerms references live inside method bodies (never in fields), so the
 * class loads fine when LuckPerms is absent; [isAvailable] guards usage and every lookup is wrapped
 * so a missing/unloaded LuckPerms degrades to `null`/empty instead of throwing.
 */
internal class LuckPermsPermissionHook : PermissionHook {

    private val present: Boolean =
        runCatching { Class.forName("net.luckperms.api.LuckPerms", false, javaClass.classLoader) }.isSuccess

    override fun name(): String = "LuckPerms"

    override fun isAvailable(): Boolean = present && runCatching { LuckPermsProvider.get() }.isSuccess

    // LuckPerms injects its permissions into Bukkit, so the native check is correct and simplest.
    override fun has(player: Player, node: String): Boolean = player.hasPermission(node)

    override fun primaryGroup(player: Player): String? = user(player)?.primaryGroup

    override fun groups(player: Player): List<String> {
        val user = user(player) ?: return emptyList()
        return runCatching {
            user.getInheritedGroups(QueryOptions.nonContextual()).map { it.name }
        }.getOrDefault(emptyList())
    }

    override fun prefix(player: Player): String? =
        runCatching { user(player)?.cachedData?.metaData?.prefix }.getOrNull()

    override fun meta(player: Player, key: String): String? =
        runCatching { user(player)?.cachedData?.metaData?.getMetaValue(key) }.getOrNull()

    override fun addGroup(player: Player, group: String): Boolean = mutate(player) {
        it.data().add(InheritanceNode.builder(group).build())
    }

    override fun addTempGroup(player: Player, group: String, duration: Duration): Boolean = mutate(player) {
        it.data().add(InheritanceNode.builder(group).expiry(duration.seconds, TimeUnit.SECONDS).build())
    }

    override fun removeGroup(player: Player, group: String): Boolean = mutate(player) {
        it.data().remove(InheritanceNode.builder(group).build())
    }

    override fun setPermission(player: Player, node: String, value: Boolean): Boolean = mutate(player) {
        it.data().add(PermissionNode.builder(node).value(value).build())
    }

    override fun unsetPermission(player: Player, node: String): Boolean = mutateUser(player) { user ->
        // Remove the node regardless of the stored true/false value or contexts.
        val before = user.getNodes(NodeType.PERMISSION).count { it.permission == node }
        if (before == 0) return@mutateUser false
        user.data().clear { it is PermissionNode && it.permission == node }
        true
    }

    private fun user(player: Player) =
        runCatching { LuckPermsProvider.get().userManager.getUser(player.uniqueId) }.getOrNull()

    /** Loads the user (cached or from storage), applies [action], saves only if it reported a change. */
    private fun mutate(player: Player, action: (User) -> DataMutateResult): Boolean = mutateUser(player) {
        action(it).wasSuccessful()
    }

    private fun mutateUser(player: Player, action: (User) -> Boolean): Boolean = runCatching {
        val manager = LuckPermsProvider.get().userManager
        val user = manager.getUser(player.uniqueId) ?: manager.loadUser(player.uniqueId).join()
        val changed = action(user)
        if (changed) manager.saveUser(user).join()
        changed
    }.getOrDefault(false)
}
