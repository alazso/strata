package so.alaz.strata.metrics

import org.assertj.core.api.Assertions.assertThat
import org.bstats.charts.AdvancedPie
import org.bstats.charts.SimplePie
import org.bstats.charts.SingleLineChart
import org.junit.jupiter.api.Test
import so.alaz.strata.api.metrics.MetricChart

class MetricTranslationTest {

    @Test
    fun chartFactoriesCarryTypeAndValue() {
        assertThat(MetricChart.string("a") { "v" }.type).isEqualTo(MetricChart.Type.STRING)
        assertThat(MetricChart.number("b") { 5 }.intSupplier!!.get()).isEqualTo(5)
        assertThat(MetricChart.bool("c") { true }.boolSupplier!!.get()).isTrue()
        assertThat(MetricChart.stringList("d") { listOf("x", "y") }.listSupplier!!.get()).containsExactly("x", "y")
    }

    @Test
    fun bStatsTranslationPicksTheRightChartType() {
        assertThat(BStatsCharts.toChart(MetricChart.string("s") { "v" })).isInstanceOf(SimplePie::class.java)
        assertThat(BStatsCharts.toChart(MetricChart.bool("b") { true })).isInstanceOf(SimplePie::class.java)
        assertThat(BStatsCharts.toChart(MetricChart.number("n") { 1 })).isInstanceOf(SingleLineChart::class.java)
        assertThat(BStatsCharts.toChart(MetricChart.stringList("l") { listOf("a") })).isInstanceOf(AdvancedPie::class.java)
    }

    @Test
    fun fastStatsTranslationPreservesIdAndComputesValue() {
        val string = FastStatsMetrics.toMetric(MetricChart.string("active") { "sqlite" })
        assertThat(string.id).isEqualTo("active")
        assertThat(string.compute().get()).isEqualTo("sqlite")

        val number = FastStatsMetrics.toMetric(MetricChart.number("count") { 42 })
        assertThat(number.compute().get()).isEqualTo(42)

        val bool = FastStatsMetrics.toMetric(MetricChart.bool("flag") { true })
        assertThat(bool.compute().get()).isEqualTo(true)
    }
}
