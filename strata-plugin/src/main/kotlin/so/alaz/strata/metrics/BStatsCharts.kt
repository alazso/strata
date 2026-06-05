package so.alaz.strata.metrics

import org.bstats.charts.AdvancedPie
import org.bstats.charts.CustomChart
import org.bstats.charts.SimplePie
import org.bstats.charts.SingleLineChart
import so.alaz.strata.api.metrics.MetricChart
import java.util.concurrent.Callable

/** Translates a provider-agnostic [MetricChart] into the matching bStats [CustomChart]. */
internal object BStatsCharts {

    fun toChart(chart: MetricChart): CustomChart = when (chart.type) {
        MetricChart.Type.STRING ->
            SimplePie(chart.id, Callable { chart.stringSupplier!!.get() })
        MetricChart.Type.NUMBER ->
            SingleLineChart(chart.id, Callable { chart.intSupplier!!.get() })
        MetricChart.Type.BOOL ->
            SimplePie(chart.id, Callable { chart.boolSupplier!!.get().toString() })
        MetricChart.Type.STRING_LIST ->
            AdvancedPie(chart.id, Callable { chart.listSupplier!!.get().groupingBy { it }.eachCount() })
    }
}
