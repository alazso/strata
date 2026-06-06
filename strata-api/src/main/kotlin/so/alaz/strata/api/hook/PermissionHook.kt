package so.alaz.strata.api.hook

import org.bukkit.entity.Player
import java.time.Duration

/**
 * Permission, group, and meta lookups, plus optional write operations. The Bukkit-native provider
 * always answers [has]; richer providers (LuckPerms, etc.) additionally resolve groups, prefixes,
 * meta, and can mutate them. Read methods a provider cannot answer return `null`/empty; write
 * methods a provider cannot perform return `false`. Both degrade rather than throwing.
 */
public interface PermissionHook : Hook {

    /** `true` if [player] has permission [node]. */
    public fun has(player: Player, node: String): Boolean

    /** The player's primary group, or `null` if the provider does not track groups. */
    public fun primaryGroup(player: Player): String?

    /** All groups the player inherits, or empty if the provider does not track groups. */
    public fun groups(player: Player): List<String>

    /** The player's chat prefix, or `null` if unknown. */
    public fun prefix(player: Player): String?

    /** A meta value by [key], or `null` if unknown. */
    public fun meta(player: Player, key: String): String?

    // --- Write operations -------------------------------------------------------------------------
    // These hit the permission store, so call them off the main thread. Providers that cannot
    // persist permission data (the Bukkit baseline) inherit the `false` defaults below; a richer
    // provider overrides them. Returning `false` means "not changed / not supported".

    /** Adds [player] to [group] permanently. Returns `true` if the data changed and was saved. */
    public fun addGroup(player: Player, group: String): Boolean = false

    /** Adds [player] to [group] for [duration] (an expiring membership). */
    public fun addTempGroup(player: Player, group: String, duration: Duration): Boolean = false

    /** Removes [player] from [group]. */
    public fun removeGroup(player: Player, group: String): Boolean = false

    /** Sets permission [node] on [player] to [value]. */
    public fun setPermission(player: Player, node: String, value: Boolean): Boolean = false

    /** Clears any directly-set [node] permission from [player]. */
    public fun unsetPermission(player: Player, node: String): Boolean = false
}
