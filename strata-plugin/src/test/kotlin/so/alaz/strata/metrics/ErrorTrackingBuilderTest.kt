package so.alaz.strata.metrics

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Test

class ErrorTrackingBuilderTest {

    @Test
    fun anonymizationAndIgnoreAreFluentAndReturnSameBuilder() {
        val builder = DefaultMetricsBuilder(mockk<JavaPlugin>(relaxed = true))
        assertThat(builder.addAnonymization("secret-\\d+", "[hidden]")).isSameAs(builder)
        assertThat(builder.ignoreError(IllegalStateException::class.java)).isSameAs(builder)
        assertThat(builder.errorTracking(true)).isSameAs(builder)
    }

    @Test
    fun buildsTrackerWithDefaultsAndCustomsWithoutThrowing() {
        val tracker = StrataErrorTracker.build(
            customAnonymizers = listOf("token=\\w+" to "token=[redacted]"),
            ignoredErrors = listOf(RuntimeException::class.java),
        )
        assertThat(tracker).isNotNull()
    }
}
