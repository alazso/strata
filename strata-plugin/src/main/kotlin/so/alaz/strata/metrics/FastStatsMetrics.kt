package so.alaz.strata.metrics

import dev.faststats.data.Metric
import so.alaz.strata.api.metrics.MetricChart
import java.util.concurrent.Callable

/** Translates a provider-agnostic [MetricChart] into the matching FastStats [Metric]. */
internal object FastStatsMetrics {

    fun toMetric(chart: MetricChart): Metric<*> = when (chart.type) {
        MetricChart.Type.STRING ->
            Metric.string(chart.id, Callable { chart.stringSupplier!!.get() })
        MetricChart.Type.NUMBER ->
            Metric.number(chart.id, Callable { chart.intSupplier!!.get() })
        MetricChart.Type.BOOL ->
            Metric.bool(chart.id, Callable { chart.boolSupplier!!.get() })
        MetricChart.Type.STRING_LIST ->
            Metric.stringArray(chart.id, Callable { chart.listSupplier!!.get().toTypedArray() })
    }
}
