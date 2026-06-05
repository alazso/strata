package so.alaz.strata.api.condition

import org.bukkit.Location
import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus

/**
 * What a [Condition] is evaluated against: the [player], a [location] (defaults to the player's),
 * and optional caller-supplied [extras] (e.g. an `"owner"` UUID for the `owner` condition).
 */
@ApiStatus.Experimental
public class ConditionContext private constructor(
    public val player: Player,
    public val location: Location,
    private val extras: Map<String, Any>,
) {

    /** A caller-supplied extra value by [key], or `null` if absent. */
    public fun getExtra(key: String): Any? = extras[key]

    public companion object {
        /** A context for [player] at the player's current location, with no extras. */
        @JvmStatic
        public fun of(player: Player): ConditionContext =
            ConditionContext(player, player.location, emptyMap())

        @JvmStatic
        public fun builder(player: Player): Builder = Builder(player)
    }

    public class Builder(private val player: Player) {
        private var location: Location? = null
        private val extras = HashMap<String, Any>()

        public fun location(location: Location): Builder {
            this.location = location
            return this
        }

        public fun extra(key: String, value: Any): Builder {
            extras[key] = value
            return this
        }

        public fun build(): ConditionContext =
            ConditionContext(player, location ?: player.location, extras.toMap())
    }
}
