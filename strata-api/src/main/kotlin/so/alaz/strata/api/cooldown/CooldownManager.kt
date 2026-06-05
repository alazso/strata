package so.alaz.strata.api.cooldown

import org.jetbrains.annotations.ApiStatus
import java.time.Duration

/**
 * Tracks keyed cooldowns. Keys are any object with sensible `equals`/`hashCode` — typically a player
 * UUID, or a composite of UUID + action. Thread-safe. Obtain one with [Cooldowns.create]; each plugin
 * keeps its own instance(s) so cooldowns are isolated.
 */
@ApiStatus.Experimental
public interface CooldownManager {

    /** Starts (or restarts) a cooldown for [key] lasting [duration]. */
    public fun set(key: Any, duration: Duration)

    /** Convenience for [set] in milliseconds. */
    public fun setMillis(key: Any, millis: Long)

    /** `true` if [key] is currently on cooldown. */
    public fun isOnCooldown(key: Any): Boolean

    /** Milliseconds remaining for [key], or 0 if not on cooldown. */
    public fun remainingMillis(key: Any): Long

    /** Time remaining for [key], or [Duration.ZERO] if not on cooldown. */
    public fun remaining(key: Any): Duration

    /** Clears the cooldown for [key]. */
    public fun clear(key: Any)

    /** Clears all cooldowns. */
    public fun clearAll()
}
