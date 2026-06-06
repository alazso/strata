package so.alaz.strata.condition

import io.mockk.every
import io.mockk.mockk
import net.kyori.adventure.text.Component
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.Location
import org.bukkit.Statistic
import org.bukkit.World
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import so.alaz.strata.api.condition.Condition
import so.alaz.strata.api.condition.ConditionContext
import so.alaz.strata.api.condition.ConditionResult
import so.alaz.strata.api.condition.Conditions
import so.alaz.strata.api.hook.RegionHook
import so.alaz.strata.hook.DefaultHookRegistry
import so.alaz.strata.text.MiniMessageTextRenderer

class ConditionFrameworkTest {

    private val registry = DefaultConditionRegistry(DefaultHookRegistry(), MiniMessageTextRenderer())

    private fun section(vararg pairs: Pair<String, Any>) =
        MemoryConfiguration().apply { pairs.forEach { (k, v) -> set(k, v) } }

    @Test
    fun builtinTypesAreRegistered() {
        assertThat(registry.types()).contains(
            "permission", "world", "gamemode", "exp", "expiry", "owner", "economy", "rank", "region",
            "papi", "playerstat",
        )
    }

    @Test
    fun unknownTypeBuildsNull() {
        assertThat(registry.build(section("type" to "does-not-exist"))).isNull()
        assertThat(registry.build(section("permission" to "x"))).isNull() // no type
    }

    @Test
    fun permissionConditionPassesAndFails() {
        val condition = registry.build(section("type" to "permission", "permission" to "a.b", "deny" to "<red>no"))!!
        val player = mockk<Player>(relaxed = true)
        every { player.hasPermission("a.b") } returns true
        assertThat(condition.test(ConditionContext.of(player)).passed).isTrue()

        every { player.hasPermission("a.b") } returns false
        val failed = condition.test(ConditionContext.of(player))
        assertThat(failed.passed).isFalse()
        assertThat(failed.message).isNotNull()
    }

    @Test
    fun worldConditionChecksWorldName() {
        val condition = registry.build(section("type" to "world", "worlds" to listOf("world")))!!
        val world = mockk<World>(); every { world.name } returns "world"
        val player = mockk<Player>(relaxed = true); every { player.world } returns world
        assertThat(condition.test(ConditionContext.of(player)).passed).isTrue()
    }

    @Test
    fun economyConditionFailsGracefullyWithoutHook() {
        val condition = registry.build(section("type" to "economy", "amount" to 100.0))!!
        val result = condition.test(ConditionContext.of(mockk<Player>(relaxed = true)))
        assertThat(result.passed).isFalse() // no economy hook registered → graceful denial
    }

    @Test
    fun testAllReturnsFirstFailureMessage() {
        val context = ConditionContext.of(mockk<Player>(relaxed = true))
        val stop = Component.text("stop")
        val result = Conditions.testAll(
            listOf(
                Condition { ConditionResult.pass() },
                Condition { ConditionResult.fail(stop) },
                Condition { ConditionResult.pass() },
            ),
            context,
        )
        assertThat(result.passed).isFalse()
        assertThat(result.message).isEqualTo(stop)
    }

    @Test
    fun papiConditionComparesResolvedValue() {
        // PAPI isn't running in tests, so resolve() falls back to the raw input — which still
        // exercises the comparison operators.
        val player = mockk<Player>(relaxed = true)
        val equals = registry.build(section("type" to "papi", "placeholder" to "hello", "value" to "hello"))!!
        assertThat(equals.test(ConditionContext.of(player)).passed).isTrue()

        val mismatch = registry.build(section("type" to "papi", "placeholder" to "hello", "value" to "world"))!!
        assertThat(mismatch.test(ConditionContext.of(player)).passed).isFalse()

        val numeric = registry.build(section("type" to "papi", "placeholder" to "10", "operator" to ">=", "value" to "5"))!!
        assertThat(numeric.test(ConditionContext.of(player)).passed).isTrue()
    }

    @Test
    fun playerstatConditionComparesUntypedStatistic() {
        val condition = registry.build(section("type" to "playerstat", "statistic" to "MOB_KILLS", "operator" to ">=", "value" to 100))!!
        val player = mockk<Player>(relaxed = true)
        every { player.getStatistic(Statistic.MOB_KILLS) } returns 150
        assertThat(condition.test(ConditionContext.of(player)).passed).isTrue()

        every { player.getStatistic(Statistic.MOB_KILLS) } returns 50
        assertThat(condition.test(ConditionContext.of(player)).passed).isFalse()
    }

    @Test
    fun regionConditionMatchesCaseInsensitively() {
        // WorldGuard ids are always lowercase; a capitalized region in config must still match.
        val hooks = DefaultHookRegistry()
        hooks.register(RegionHook::class.java, object : RegionHook {
            override fun name() = "Fake"
            override fun isAvailable() = true
            override fun regionsAt(location: Location) = listOf("spawn")
            override fun isInRegion(location: Location, regionId: String) = regionId.equals("spawn", true)
            override fun canBuild(player: Player, location: Location) = true
        }, 0)
        val registry = DefaultConditionRegistry(hooks, MiniMessageTextRenderer())

        val condition = registry.build(section("type" to "region", "regions" to listOf("Spawn")))!!
        assertThat(condition.test(ConditionContext.of(mockk<Player>(relaxed = true))).passed).isTrue()
    }

    @Test
    fun testAllPassesWhenEmptyOrAllPass() {
        val context = ConditionContext.of(mockk<Player>(relaxed = true))
        assertThat(Conditions.testAll(emptyList(), context).passed).isTrue()
        assertThat(Conditions.testAll(listOf(Condition { ConditionResult.pass() }), context).passed).isTrue()
    }
}
