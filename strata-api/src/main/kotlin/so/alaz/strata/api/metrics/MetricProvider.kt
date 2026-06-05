package so.alaz.strata.api.metrics

import org.jetbrains.annotations.ApiStatus

/** Supported metrics backends. A plugin may enable either or both. */
@ApiStatus.Experimental
public enum class MetricProvider {
    /** bStats — identified by an integer service id (passed as a string). */
    BSTATS,

    /** FastStats — identified by a project token. */
    FASTSTATS,
}
