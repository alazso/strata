package so.alaz.strata.metrics

import dev.faststats.FeatureFlag
import dev.faststats.FeatureFlagService
import so.alaz.strata.api.metrics.FeatureFlags
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * [FeatureFlags] backed by FastStats `FeatureFlag` handles, with the declared local defaults as the
 * fallback. Holds the defaults regardless of whether FastStats is enabled, so reads always work; the
 * remote handles are bound once the FastStats context is created.
 */
internal class DefaultFeatureFlags(private val defaults: Map<String, Any>) : FeatureFlags {

    private val handles = ConcurrentHashMap<String, FeatureFlag<*>>()

    /** Defines each declared flag on [service] and records its handle. Called from context creation. */
    fun bind(service: FeatureFlagService) {
        for ((key, default) in defaults) {
            val handle: FeatureFlag<*> = when (default) {
                is Boolean -> service.define(key, default)
                is String -> service.define(key, default)
                is Number -> service.define(key, default)
                else -> continue
            }
            handles[key] = handle
        }
    }

    override fun isRemote(): Boolean = handles.isNotEmpty()
    override fun isDefined(key: String): Boolean = defaults.containsKey(key)
    override fun keys(): Set<String> = defaults.keys.toSet()

    override fun getBoolean(key: String): Boolean =
        cached(key) as? Boolean ?: defaults[key] as? Boolean ?: false

    override fun getString(key: String): String =
        cached(key) as? String ?: defaults[key] as? String ?: ""

    override fun getDouble(key: String): Double =
        (cached(key) as? Number)?.toDouble() ?: (defaults[key] as? Number)?.toDouble() ?: 0.0

    override fun getInt(key: String): Int =
        (cached(key) as? Number)?.toInt() ?: (defaults[key] as? Number)?.toInt() ?: 0

    override fun fetchBoolean(key: String): CompletableFuture<Boolean> =
        fetch(key) { it as? Boolean ?: false } ?: CompletableFuture.completedFuture(getBoolean(key))

    override fun fetchString(key: String): CompletableFuture<String> =
        fetch(key) { it as? String ?: "" } ?: CompletableFuture.completedFuture(getString(key))

    override fun fetchDouble(key: String): CompletableFuture<Double> =
        fetch(key) { (it as? Number)?.toDouble() ?: 0.0 } ?: CompletableFuture.completedFuture(getDouble(key))

    /** Cached value with no network, or null if undefined/unavailable. */
    private fun cached(key: String): Any? {
        val handle = handles[key] ?: return null
        return runCatching {
            val value = handle.getCached()
            if (value.isPresent) value.get() else null
        }.getOrNull()
    }

    private fun <T> fetch(key: String, map: (Any?) -> T): CompletableFuture<T>? {
        val handle = handles[key] ?: return null
        return runCatching {
            @Suppress("UNCHECKED_CAST")
            (handle as FeatureFlag<Any?>).fetch().thenApply(map)
        }.getOrNull()
    }
}
