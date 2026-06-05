package so.alaz.strata.api.hook

import org.jetbrains.annotations.ApiStatus

/**
 * Registry of [Hook]s keyed by capability type. Multiple providers may register for the same
 * capability (e.g. several hologram plugins); [get] resolves the highest-priority one that is
 * currently [available][Hook.isAvailable], and returns `null` when none are — callers degrade
 * gracefully rather than crashing.
 */
@ApiStatus.Experimental
public interface HookRegistry {

    /** Registers [hook] for capability [type]. Higher [priority] wins in [get]. */
    public fun <T : Hook> register(type: Class<T>, hook: T, priority: Int)

    /** The best currently-available hook for [type], or `null` if none are available. */
    public fun <T : Hook> get(type: Class<T>): T?

    /** Like [get] but throws [IllegalStateException] when no provider is available. */
    public fun <T : Hook> require(type: Class<T>): T

    /** `true` if at least one provider for [type] is currently available. */
    public fun <T : Hook> isAvailable(type: Class<T>): Boolean

    /** All registered providers for [type], highest priority first (regardless of availability). */
    public fun <T : Hook> all(type: Class<T>): List<T>

    /**
     * Sets the preferred provider [name] for [type] (matched case-insensitively against
     * [Hook.name]). When that provider is available, [get] returns it; otherwise [get] falls back to
     * priority order. Pass `null` to clear. Use this when providers are NOT interchangeable — e.g.
     * distinct economy backends (Vault vs. Conduit) where the admin must choose the authoritative one.
     */
    public fun <T : Hook> setPreference(type: Class<T>, name: String?)

    public companion object {
        /** Default registration priority. */
        public const val DEFAULT_PRIORITY: Int = 0
    }
}
