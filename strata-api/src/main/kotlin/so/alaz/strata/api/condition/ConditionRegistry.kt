package so.alaz.strata.api.condition

import org.bukkit.configuration.ConfigurationSection
import org.jetbrains.annotations.ApiStatus

/**
 * Maps condition `type` keys to [ConditionFactory]s and builds [Condition]s from config. Strata
 * pre-registers built-ins (permission/world/region/rank/economy/exp/expiry/gamemode/owner); plugins
 * add their own with [register].
 *
 * Config shape: each condition is a section with a `type` key plus type-specific keys and an optional
 * `deny` message, e.g. `{ type: economy, amount: 100, deny: "<red>Need $100" }`.
 */
@ApiStatus.Experimental
public interface ConditionRegistry {

    /** Registers [factory] under [type] (case-insensitive). Replaces any existing factory. */
    public fun register(type: String, factory: ConditionFactory)

    /** Builds a condition from [section]'s `type`, or `null` if the type is missing/unregistered. */
    public fun build(section: ConfigurationSection): Condition?

    /** Builds conditions from a list of sections, skipping unknown types. */
    public fun buildAll(sections: List<ConfigurationSection>): List<Condition>

    /** Builds conditions from a YAML list-of-maps (Bukkit returns these for `getMapList`). */
    public fun buildFromMaps(maps: List<Map<*, *>>): List<Condition>

    /** `true` if a factory is registered for [type] (case-insensitive). */
    public fun isRegistered(type: String): Boolean

    /** All registered type keys. */
    public fun types(): Set<String>
}
