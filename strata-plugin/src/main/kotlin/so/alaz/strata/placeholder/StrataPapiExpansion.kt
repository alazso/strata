package so.alaz.strata.placeholder

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.function.Function
import java.util.function.Supplier

/**
 * PlaceholderAPI expansion backing a [DefaultPlaceholderRegistration]. Kept in its own class because
 * it extends [PlaceholderExpansion]: it is only loaded when PlaceholderAPI is present, so the
 * registration class itself stays loadable when PAPI is absent.
 */
internal class StrataPapiExpansion(
    private val plugin: Plugin,
    private val id: String,
    private val perPlayer: Map<String, Function<Player, String?>>,
    private val global: Map<String, Supplier<String?>>,
) : PlaceholderExpansion() {

    override fun getIdentifier(): String = id

    override fun getAuthor(): String = plugin.pluginMeta.authors.joinToString(", ").ifEmpty { "Strata" }

    override fun getVersion(): String = plugin.pluginMeta.version

    override fun persist(): Boolean = true // survive a PlaceholderAPI reload

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        global[params]?.let { return it.get() }
        val resolver = perPlayer[params] ?: return null
        val online = player?.player ?: return null
        return resolver.apply(online)
    }
}
