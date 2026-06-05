package so.alaz.strata.api.cooldown

import org.jetbrains.annotations.ApiStatus
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.function.LongSupplier

/** Factory for [CooldownManager]s. */
@ApiStatus.Experimental
public object Cooldowns {

    /** A new cooldown manager backed by the system clock. */
    @JvmStatic
    public fun create(): CooldownManager = DefaultCooldownManager { System.currentTimeMillis() }

    /** A new cooldown manager driven by a custom millisecond [clock] (e.g. for testing). */
    @JvmStatic
    public fun create(clock: LongSupplier): CooldownManager = DefaultCooldownManager(clock)
}

private class DefaultCooldownManager(private val clock: LongSupplier) : CooldownManager {

    private val expiries = ConcurrentHashMap<Any, Long>()

    override fun set(key: Any, duration: Duration) {
        setMillis(key, duration.toMillis())
    }

    override fun setMillis(key: Any, millis: Long) {
        if (millis <= 0) expiries.remove(key) else expiries[key] = clock.asLong + millis
    }

    override fun isOnCooldown(key: Any): Boolean = remainingMillis(key) > 0

    override fun remainingMillis(key: Any): Long {
        val expiry = expiries[key] ?: return 0
        val remaining = expiry - clock.asLong
        if (remaining <= 0) {
            expiries.remove(key)
            return 0
        }
        return remaining
    }

    override fun remaining(key: Any): Duration = Duration.ofMillis(remainingMillis(key))

    override fun clear(key: Any) {
        expiries.remove(key)
    }

    override fun clearAll() {
        expiries.clear()
    }
}
