package so.alaz.strata.placeholder

import io.github.miniplaceholders.api.Expansion
import io.github.miniplaceholders.api.resolver.AudienceTagResolver
import io.github.miniplaceholders.api.resolver.GlobalTagResolver
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import so.alaz.strata.api.placeholder.PlaceholderRegistration
import java.util.function.Function
import java.util.function.Supplier

/**
 * Default [PlaceholderRegistration]. Collects per-player and global placeholders, then on [register]
 * wires them into whichever backends are installed. All backend-specific code runs in guarded
 * methods (and the PAPI expansion lives in [StrataPapiExpansion]), so the class loads and is usable
 * even when neither PlaceholderAPI nor MiniPlaceholders is present.
 */
internal class DefaultPlaceholderRegistration(private val plugin: Plugin) : PlaceholderRegistration {

    private val identifier: String =
        plugin.name.lowercase().filter { it.isLetterOrDigit() || it == '_' }.ifEmpty { "strata" }

    private val perPlayer = LinkedHashMap<String, Function<Player, String?>>()
    private val global = LinkedHashMap<String, Supplier<String?>>()

    private val papiPresent = isClassPresent("me.clip.placeholderapi.expansion.PlaceholderExpansion")
    private val miniPresent = isClassPresent("io.github.miniplaceholders.api.Expansion")

    private var papiHandle: Any? = null
    private var miniHandle: Any? = null

    override fun add(key: String, resolver: Function<Player, String?>): PlaceholderRegistration =
        apply { perPlayer[key] = resolver }

    override fun addGlobal(key: String, resolver: Supplier<String?>): PlaceholderRegistration =
        apply { global[key] = resolver }

    override fun register() {
        if (papiPresent) runCatching { registerPapi() }
        if (miniPresent) runCatching { registerMini() }
    }

    override fun unregister() {
        if (papiPresent) (papiHandle as? PlaceholderExpansion)?.let { runCatching { it.unregister() } }
        if (miniPresent) (miniHandle as? Expansion)?.let { runCatching { it.unregister() } }
        papiHandle = null
        miniHandle = null
    }

    private fun registerPapi() {
        val expansion = StrataPapiExpansion(plugin, identifier, perPlayer, global)
        expansion.register()
        papiHandle = expansion
    }

    private fun registerMini() {
        val builder = Expansion.builder(identifier)
            .author(plugin.pluginMeta.authors.joinToString(", ").ifEmpty { "Strata" })
            .version(plugin.pluginMeta.version)
        global.forEach { (key, supplier) ->
            builder.globalPlaceholder(key, GlobalTagResolver { _, _ -> supplier.get()?.let(::textTag) })
        }
        perPlayer.forEach { (key, resolver) ->
            builder.audiencePlaceholder(Player::class.java, key, AudienceTagResolver { player, _, _ ->
                resolver.apply(player)?.let(::textTag)
            })
        }
        val expansion = builder.build()
        expansion.register()
        miniHandle = expansion
    }

    /** Inserts a value as plain text (component-safe: no markup is interpreted from the value). */
    private fun textTag(value: String): Tag = Tag.inserting(Component.text(value))

    private fun isClassPresent(name: String): Boolean =
        runCatching { Class.forName(name, false, javaClass.classLoader) }.isSuccess
}
