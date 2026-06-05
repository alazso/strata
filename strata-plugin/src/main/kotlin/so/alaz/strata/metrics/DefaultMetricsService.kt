package so.alaz.strata.metrics

import dev.faststats.Attributes
import dev.faststats.bukkit.BukkitContext
import org.bukkit.plugin.java.JavaPlugin
import so.alaz.strata.api.metrics.MetricChart
import so.alaz.strata.api.metrics.MetricProvider
import so.alaz.strata.api.metrics.Metrics
import so.alaz.strata.api.metrics.MetricsBuilder
import so.alaz.strata.api.metrics.MetricsService
import java.time.Duration
import java.util.EnumMap

internal class DefaultMetricsService : MetricsService {
    override fun create(plugin: JavaPlugin): MetricsBuilder = DefaultMetricsBuilder(plugin)
}

/**
 * Accumulates provider ids, charts, and feature-flag declarations, then on [start] initializes each
 * enabled backend. Backend init is wrapped so a failure (bad id, network, missing server context)
 * disables that provider without taking down plugin enable.
 */
internal class DefaultMetricsBuilder(private val plugin: JavaPlugin) : MetricsBuilder {

    private val ids = EnumMap<MetricProvider, String>(MetricProvider::class.java)
    private val charts = mutableListOf<MetricChart>()
    private val anonymizers = mutableListOf<Pair<String, String>>()
    private val ignoredErrors = mutableListOf<Class<out Throwable>>()
    private val flagDefaults = LinkedHashMap<String, Any>()
    private val flagAttributes = LinkedHashMap<String, Any>()
    private var flagTtl: Duration? = null
    private var debug = false
    private var errorTracking = false

    override fun enable(provider: MetricProvider, id: String): MetricsBuilder {
        ids[provider] = id
        return this
    }

    override fun addChart(chart: MetricChart): MetricsBuilder {
        charts.add(chart)
        return this
    }

    override fun debug(enabled: Boolean): MetricsBuilder {
        debug = enabled
        return this
    }

    override fun errorTracking(enabled: Boolean): MetricsBuilder {
        errorTracking = enabled
        return this
    }

    override fun addAnonymization(pattern: String, replacement: String): MetricsBuilder {
        anonymizers.add(pattern to replacement)
        errorTracking = true
        return this
    }

    override fun ignoreError(exceptionType: Class<out Throwable>): MetricsBuilder {
        ignoredErrors.add(exceptionType)
        errorTracking = true
        return this
    }

    override fun defineFlag(key: String, default: Boolean): MetricsBuilder {
        flagDefaults[key] = default
        return this
    }

    override fun defineFlag(key: String, default: String): MetricsBuilder {
        flagDefaults[key] = default
        return this
    }

    override fun defineFlag(key: String, default: Double): MetricsBuilder {
        flagDefaults[key] = default
        return this
    }

    override fun flagTtl(ttl: Duration): MetricsBuilder {
        flagTtl = ttl
        return this
    }

    override fun flagAttribute(key: String, value: String): MetricsBuilder {
        flagAttributes[key] = value
        return this
    }

    override fun flagAttribute(key: String, value: Boolean): MetricsBuilder {
        flagAttributes[key] = value
        return this
    }

    override fun flagAttribute(key: String, value: Double): MetricsBuilder {
        flagAttributes[key] = value
        return this
    }

    override fun start(): Metrics {
        val flags = DefaultFeatureFlags(LinkedHashMap(flagDefaults))
        return DefaultMetrics(
            bStats = ids[MetricProvider.BSTATS]?.let(::startBStats),
            fastStats = ids[MetricProvider.FASTSTATS]?.let { startFastStats(it, flags) },
            featureFlags = flags,
        )
    }

    private fun startBStats(id: String): org.bstats.bukkit.Metrics? = runCatching {
        val metrics = org.bstats.bukkit.Metrics(plugin, id.toInt())
        charts.forEach { metrics.addCustomChart(BStatsCharts.toChart(it)) }
        metrics
    }.onFailure { plugin.logger.warning("bStats metrics failed to start: ${it.message}") }.getOrNull()

    // FastStats 0.25.x: BukkitContext composes the metrics, error-tracking, and feature-flag services.
    // The token is the Factory(plugin, token) constructor argument.
    private fun startFastStats(token: String, flags: DefaultFeatureFlags): BukkitContext? = runCatching {
        var factory = BukkitContext.Factory(plugin, token).metrics { metricsFactory ->
            var f = metricsFactory
            for (chart in charts) f = f.addMetric(FastStatsMetrics.toMetric(chart))
            f.create()
        }
        if (errorTracking) {
            factory = factory.errorTrackerService(StrataErrorTracker.build(anonymizers, ignoredErrors))
        }
        if (flagDefaults.isNotEmpty()) {
            factory = factory.featureFlagService { flagFactory ->
                var f = flagFactory
                flagTtl?.let { f = f.ttl(it) }
                if (flagAttributes.isNotEmpty()) f = f.attributes(buildFlagAttributes())
                val service = f.create()
                flags.bind(service)
                service
            }
        }
        factory.create().also { it.ready() }
    }.onFailure { plugin.logger.warning("FastStats metrics failed to start: ${it.message}") }.getOrNull()

    private fun buildFlagAttributes(): Attributes {
        var attributes = Attributes.empty()
        for ((key, value) in flagAttributes) {
            attributes = when (value) {
                is Boolean -> attributes.put(key, value)
                is Number -> attributes.put(key, value)
                else -> attributes.put(key, value.toString())
            }
        }
        return attributes
    }
}
