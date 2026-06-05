package so.alaz.strata.metrics

import dev.faststats.bukkit.BukkitContext
import so.alaz.strata.api.metrics.FeatureFlags
import so.alaz.strata.api.metrics.MetricProvider
import so.alaz.strata.api.metrics.Metrics

/** Live [Metrics] handle wrapping the started bStats metrics and/or FastStats [BukkitContext]. */
internal class DefaultMetrics(
    private val bStats: org.bstats.bukkit.Metrics?,
    private val fastStats: BukkitContext?,
    private val featureFlags: FeatureFlags,
) : Metrics {

    override fun isActive(provider: MetricProvider): Boolean = when (provider) {
        MetricProvider.BSTATS -> bStats != null
        MetricProvider.FASTSTATS -> fastStats != null
    }

    override fun featureFlags(): FeatureFlags = featureFlags

    override fun shutdown() {
        runCatching { bStats?.shutdown() }
        runCatching { fastStats?.shutdown() }
    }
}
