package so.alaz.strata.api.metrics

import org.jetbrains.annotations.ApiStatus
import java.time.Duration

/**
 * Fluent configuration for a plugin's metrics. Enable one or both providers with their id/token, add
 * any custom charts, then [start].
 *
 * ```java
 * Metrics metrics = StrataApi.metrics().create(this)
 *     .enable(MetricProvider.BSTATS, "12345")
 *     .enable(MetricProvider.FASTSTATS, "project-token")
 *     .addChart(MetricChart.string("storage_backend", () -> backend))
 *     .start();
 * ```
 */
@ApiStatus.Experimental
public interface MetricsBuilder {

    /**
     * Enables [provider] for this plugin. [id] is the bStats service id (numeric, as a string) or the
     * FastStats project token. Returns `this`.
     */
    public fun enable(provider: MetricProvider, id: String): MetricsBuilder

    /** Adds a custom chart, routed to every enabled provider. Returns `this`. */
    public fun addChart(chart: MetricChart): MetricsBuilder

    /** Enables verbose backend logging (development). Returns `this`. */
    public fun debug(enabled: Boolean): MetricsBuilder

    /**
     * Enables FastStats error tracking with Strata's default anonymizers (emails, tokens, UUIDs,
     * JDBC URLs, SQL, filesystem paths). No effect unless FastStats is enabled. Returns `this`.
     */
    public fun errorTracking(enabled: Boolean): MetricsBuilder

    /**
     * Adds a custom anonymizer applied on top of the defaults: occurrences matching the regex
     * [pattern] are replaced with [replacement] before any error trace is uploaded. Implicitly
     * enables error tracking. Returns `this`.
     */
    public fun addAnonymization(pattern: String, replacement: String): MetricsBuilder

    /**
     * Drops errors of [exceptionType] (and its subtypes) so they are never reported. Implicitly
     * enables error tracking. Returns `this`.
     */
    public fun ignoreError(exceptionType: Class<out Throwable>): MetricsBuilder

    /**
     * Declares a remote feature flag [key] with a local [default]. The default is the fallback used
     * whenever FastStats is disabled, unreachable, or has no value. Read flags from the started
     * [Metrics] handle via [Metrics.featureFlags]. Flags require FastStats; without it, only the
     * declared defaults are returned. Returns `this`.
     */
    public fun defineFlag(key: String, default: Boolean): MetricsBuilder
    public fun defineFlag(key: String, default: String): MetricsBuilder
    public fun defineFlag(key: String, default: Double): MetricsBuilder

    /** Cache TTL for flag values (how often a stale flag is refreshed in the background). Returns `this`. */
    public fun flagTtl(ttl: Duration): MetricsBuilder

    /** Adds a global targeting attribute applied to all flag evaluations (e.g. `version`). Returns `this`. */
    public fun flagAttribute(key: String, value: String): MetricsBuilder
    public fun flagAttribute(key: String, value: Boolean): MetricsBuilder
    public fun flagAttribute(key: String, value: Double): MetricsBuilder

    /** Starts submission for all enabled providers and returns the live [Metrics] handle. */
    public fun start(): Metrics
}
