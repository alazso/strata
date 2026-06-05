package so.alaz.strata.hook

import org.bukkit.entity.Player
import so.alaz.strata.api.hook.PermissionHook

/**
 * Always-available baseline permission provider using Bukkit's native permission system. Answers
 * [has]; group/prefix/meta are unknown to Bukkit and return `null`/empty so a richer provider
 * (e.g. LuckPerms) is preferred when present.
 */
internal class BukkitPermissionHook : PermissionHook {

    override fun name(): String = "Bukkit"

    override fun isAvailable(): Boolean = true

    override fun has(player: Player, node: String): Boolean = player.hasPermission(node)

    override fun primaryGroup(player: Player): String? = null

    override fun groups(player: Player): List<String> = emptyList()

    override fun prefix(player: Player): String? = null

    override fun meta(player: Player, key: String): String? = null
}
