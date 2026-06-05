package so.alaz.strata.api.metrics

import org.jetbrains.annotations.ApiStatus
import java.util.function.Supplier

/**
 * A provider-agnostic custom metric. Build one with the factory methods; Strata routes it to the
 * matching native chart on each enabled backend (bStats chart / FastStats metric), so the same
 * definition reports to both:
 *
 * ```java
 * MetricChart.string("storage_backend", () -> storage.backend().name());
 * MetricChart.number("loaded_regions", () -> regions.size());
 * ```
 */
@ApiStatus.Experimental
public class MetricChart private constructor(
    public val id: String,
    public val type: Type,
    public val stringSupplier: Supplier<String>?,
    public val intSupplier: Supplier<Int>?,
    public val boolSupplier: Supplier<Boolean>?,
    public val listSupplier: Supplier<List<String>>?,
) {

    public enum class Type { STRING, NUMBER, BOOL, STRING_LIST }

    public companion object {

        /** A single string value (bStats SimplePie / FastStats string metric). */
        @JvmStatic
        public fun string(id: String, supplier: Supplier<String>): MetricChart =
            MetricChart(id, Type.STRING, supplier, null, null, null)

        /** A single integer value (bStats SingleLineChart / FastStats number metric). */
        @JvmStatic
        public fun number(id: String, supplier: Supplier<Int>): MetricChart =
            MetricChart(id, Type.NUMBER, null, supplier, null, null)

        /** A boolean value, reported as "true"/"false" (bStats SimplePie / FastStats bool metric). */
        @JvmStatic
        public fun bool(id: String, supplier: Supplier<Boolean>): MetricChart =
            MetricChart(id, Type.BOOL, null, null, supplier, null)

        /** A list of strings (bStats AdvancedPie by frequency / FastStats string-array metric). */
        @JvmStatic
        public fun stringList(id: String, supplier: Supplier<List<String>>): MetricChart =
            MetricChart(id, Type.STRING_LIST, null, null, null, supplier)
    }
}
