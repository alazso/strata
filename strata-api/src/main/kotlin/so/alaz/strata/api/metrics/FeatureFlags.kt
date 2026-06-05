package so.alaz.strata.api.metrics

import org.jetbrains.annotations.ApiStatus
import java.util.concurrent.CompletableFuture

/**
 * Remote feature flags, backed by FastStats. Flags are **declared with a local default** on the
 * metrics builder (`defineFlag`), and that default is the contract: if FastStats is disabled,
 * unreachable, or has no value for a flag, reads return the local default.
 *
 * **Read from cache on hot paths.** The `getX` methods never touch the network (they return the
 * cached value or the local default), so they are safe to call per-tick / per-click / per-event. The
 * `fetchX` methods force a fresh value off-thread and return a future. Background refresh otherwise
 * happens on the TTL configured on the builder.
 */
@ApiStatus.Experimental
public interface FeatureFlags {

    /** `true` if flags are backed by a live FastStats context (remote overrides can apply). */
    public fun isRemote(): Boolean

    /** `true` if [key] was declared via `defineFlag`. */
    public fun isDefined(key: String): Boolean

    /** All declared flag keys. */
    public fun keys(): Set<String>

    /** Cached boolean value, or the local default; `false` if [key] was never declared. */
    public fun getBoolean(key: String): Boolean

    /** Cached string value, or the local default; `""` if [key] was never declared. */
    public fun getString(key: String): String

    /** Cached numeric value as a double, or the local default; `0.0` if [key] was never declared. */
    public fun getDouble(key: String): Double

    /** Cached numeric value as an int, or the local default; `0` if [key] was never declared. */
    public fun getInt(key: String): Int

    /** Forces a fresh boolean value off-thread; falls back to the cached value/default. */
    public fun fetchBoolean(key: String): CompletableFuture<Boolean>

    /** Forces a fresh string value off-thread; falls back to the cached value/default. */
    public fun fetchString(key: String): CompletableFuture<String>

    /** Forces a fresh numeric value off-thread; falls back to the cached value/default. */
    public fun fetchDouble(key: String): CompletableFuture<Double>
}
