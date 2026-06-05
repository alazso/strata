package so.alaz.strata.api.hook

import org.bukkit.entity.Player

/**
 * Permission, group, and meta lookups. The Bukkit-native provider always answers [has]; richer
 * providers (LuckPerms, etc.) additionally resolve groups, prefixes, and meta. Methods that a
 * provider cannot answer return `null`/empty rather than throwing.
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
}
