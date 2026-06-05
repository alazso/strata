package so.alaz.strata.api.metrics

import org.jetbrains.annotations.ApiStatus

/** A live metrics submission, returned by [MetricsBuilder.start]. Shut it down on plugin disable. */
@ApiStatus.Experimental
public interface Metrics {

    /** `true` if [provider] was enabled and started successfully. */
    public fun isActive(provider: MetricProvider): Boolean

    /**
     * The feature flags declared on the builder. Backed by FastStats when enabled, otherwise a
     * defaults-only view. Always non-null.
     */
    public fun featureFlags(): FeatureFlags

    /** Stops submission for all enabled providers. Idempotent. */
    public fun shutdown()
}
