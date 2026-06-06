package so.alaz.strata.condition

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemoryConfiguration
import so.alaz.strata.api.condition.Condition
import so.alaz.strata.api.condition.ConditionFactory
import so.alaz.strata.api.condition.ConditionRegistry
import so.alaz.strata.api.hook.HookRegistry
import so.alaz.strata.api.text.TextRenderer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

/**
 * Default [ConditionRegistry]. Pre-registers the built-in condition types, closing over the
 * [HookRegistry] and [TextRenderer] so hook-backed conditions resolve providers (and degrade
 * gracefully) and all failure messages render through the text renderer.
 */
internal class DefaultConditionRegistry(
    private val hooks: HookRegistry,
    private val text: TextRenderer,
) : ConditionRegistry {

    private val factories = ConcurrentHashMap<String, ConditionFactory>()

    init {
        registerBuiltins()
    }

    override fun register(type: String, factory: ConditionFactory) {
        factories[type.lowercase()] = factory
    }

    override fun build(section: ConfigurationSection): Condition? {
        val type = section.getString("type")?.lowercase() ?: return null
        return factories[type]?.create(section)
    }

    override fun buildAll(sections: List<ConfigurationSection>): List<Condition> = sections.mapNotNull(::build)

    override fun buildFromMaps(maps: List<Map<*, *>>): List<Condition> = maps.mapNotNull { build(toSection(it)) }

    override fun isRegistered(type: String): Boolean = factories.containsKey(type.lowercase())

    override fun types(): Set<String> = factories.keys.toSet()

    private fun toSection(map: Map<*, *>): ConfigurationSection {
        val section = MemoryConfiguration()
        for ((key, value) in map) if (key != null) section.set(key.toString(), value)
        return section
    }

    private fun registerBuiltins() {
        register("permission") { s -> PermissionCondition(s.getString("permission").orEmpty(), s.getString("deny"), text, hooks) }
        register("world") { s -> WorldCondition(s.getStringList("worlds").toSet(), s.getString("deny"), text) }
        register("gamemode") { s -> GamemodeCondition(s.getStringList("gamemodes").map(String::uppercase).toSet(), s.getString("deny"), text) }
        register("exp") { s -> ExpCondition(s.getInt("level"), s.getString("deny"), text) }
        register("expiry") { s -> ExpiryCondition(parseExpiry(s), s.getString("deny"), text) }
        register("owner") { s -> OwnerCondition(s.getString("deny"), text) }
        register("economy") { s -> EconomyCondition(s.getDouble("amount"), s.getString("deny"), text, hooks) }
        register("rank") { s -> RankCondition(s.getStringList("groups").map(String::lowercase).toSet(), s.getString("deny"), text, hooks) }
        register("region") { s -> RegionCondition(s.getStringList("regions").map(String::lowercase).toSet(), s.getString("deny"), text, hooks) }
        register("papi") { s ->
            PapiCondition(s.getString("placeholder").orEmpty(), s.getString("operator") ?: "equals", s.getString("value").orEmpty(), s.getString("deny"), text)
        }
        register("playerstat") { s ->
            PlayerstatCondition(s.getString("statistic").orEmpty(), s.getString("material"), s.getString("entity"), s.getString("operator") ?: ">=", s.getLong("value"), s.getString("deny"), text)
        }
    }

    /** `expires` as epoch millis, an ISO-8601 instant, or a `yyyy-MM-dd` date. Defaults to "never". */
    private fun parseExpiry(section: ConfigurationSection): Long {
        if (section.isLong("expires") || section.isInt("expires")) return section.getLong("expires")
        val raw = section.getString("expires") ?: return Long.MAX_VALUE
        return runCatching { Instant.parse(raw).toEpochMilli() }
            .recoverCatching { LocalDate.parse(raw).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }
            .getOrDefault(Long.MAX_VALUE)
    }
}
