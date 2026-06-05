package so.alaz.strata.hook

import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.query.QueryOptions
import org.bukkit.entity.Player
import so.alaz.strata.api.hook.PermissionHook

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

    private fun user(player: Player) =
        runCatching { LuckPermsProvider.get().userManager.getUser(player.uniqueId) }.getOrNull()
}
