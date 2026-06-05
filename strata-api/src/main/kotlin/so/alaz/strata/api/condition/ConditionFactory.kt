package so.alaz.strata.api.condition

import org.bukkit.configuration.ConfigurationSection
import org.jetbrains.annotations.ApiStatus

/** Builds a [Condition] from its config `section` (its type-specific keys + an optional `deny`). */
@ApiStatus.Experimental
public fun interface ConditionFactory {
    public fun create(section: ConfigurationSection): Condition
}
