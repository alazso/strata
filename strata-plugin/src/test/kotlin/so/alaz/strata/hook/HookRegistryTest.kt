package so.alaz.strata.hook

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import so.alaz.strata.api.hook.Hook

class HookRegistryTest {

    private interface Sample : Hook
    private class SampleHook(private val n: String, private val available: Boolean) : Sample {
        override fun name(): String = n
        override fun isAvailable(): Boolean = available
    }

    @Test
    fun getReturnsNullWhenNothingRegistered() {
        val registry = DefaultHookRegistry()
        assertThat(registry.get(Sample::class.java)).isNull()
        assertThat(registry.isAvailable(Sample::class.java)).isFalse()
    }

    @Test
    fun getPicksHighestPriorityAvailable() {
        val registry = DefaultHookRegistry()
        registry.register(Sample::class.java, SampleHook("low", available = true), 0)
        registry.register(Sample::class.java, SampleHook("high", available = true), 100)
        assertThat(registry.get(Sample::class.java)!!.name()).isEqualTo("high")
    }

    @Test
    fun unavailableHigherPriorityIsSkipped() {
        val registry = DefaultHookRegistry()
        registry.register(Sample::class.java, SampleHook("low", available = true), 0)
        registry.register(Sample::class.java, SampleHook("high", available = false), 100)
        assertThat(registry.get(Sample::class.java)!!.name()).isEqualTo("low")
    }

    @Test
    fun requireThrowsWhenNoneAvailable() {
        val registry = DefaultHookRegistry()
        registry.register(Sample::class.java, SampleHook("x", available = false), 0)
        assertThatThrownBy { registry.require(Sample::class.java) }
            .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun allReturnsEveryProviderHighestFirst() {
        val registry = DefaultHookRegistry()
        registry.register(Sample::class.java, SampleHook("low", available = true), 0)
        registry.register(Sample::class.java, SampleHook("high", available = false), 100)
        assertThat(registry.all(Sample::class.java).map { it.name() }).containsExactly("high", "low")
    }

    @Test
    fun preferenceSelectsNamedAvailableProviderOverPriority() {
        val registry = DefaultHookRegistry()
        registry.register(Sample::class.java, SampleHook("vault", available = true), 100)
        registry.register(Sample::class.java, SampleHook("conduit", available = true), 0)
        registry.setPreference(Sample::class.java, "CONDUIT") // case-insensitive
        assertThat(registry.get(Sample::class.java)!!.name()).isEqualTo("conduit")
    }

    @Test
    fun preferenceFallsBackToPriorityWhenPreferredUnavailable() {
        val registry = DefaultHookRegistry()
        registry.register(Sample::class.java, SampleHook("vault", available = true), 100)
        registry.register(Sample::class.java, SampleHook("conduit", available = false), 0)
        registry.setPreference(Sample::class.java, "conduit")
        assertThat(registry.get(Sample::class.java)!!.name()).isEqualTo("vault")
    }

    @Test
    fun clearingPreferenceRestoresPriorityOrder() {
        val registry = DefaultHookRegistry()
        registry.register(Sample::class.java, SampleHook("vault", available = true), 100)
        registry.register(Sample::class.java, SampleHook("conduit", available = true), 0)
        registry.setPreference(Sample::class.java, "conduit")
        registry.setPreference(Sample::class.java, null)
        assertThat(registry.get(Sample::class.java)!!.name()).isEqualTo("vault")
    }
}
