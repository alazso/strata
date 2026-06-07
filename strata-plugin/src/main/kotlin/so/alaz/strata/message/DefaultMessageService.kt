package so.alaz.strata.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import so.alaz.strata.api.message.MessageService
import so.alaz.strata.api.text.TextRenderer
import java.io.File
import java.util.Collections
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Default [MessageService]. Code defaults seed `lang/<locale>.yml` files under the plugin's data
 * folder; on [load] each file is created if missing, has any newly-introduced default keys appended,
 * and is then read back so admin edits win. Lookups resolve a viewer's locale to the closest catalog
 * (exact, then language-only, then the fallback locale) and render through the shared [TextRenderer].
 */
internal class DefaultMessageService(
    private val plugin: Plugin,
    private val text: TextRenderer,
) : MessageService {

    private var fallbackLocale: Locale = Locale.ENGLISH
    private val codeDefaults = LinkedHashMap<Locale, MutableMap<String, String>>()
    private val catalogs = ConcurrentHashMap<Locale, Map<String, String>>()
    private val warnedMissing = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    private val langDir: File get() = File(plugin.dataFolder, "lang")

    override fun defaultLocale(locale: Locale): MessageService = apply { fallbackLocale = locale }

    override fun default(key: String, value: String): MessageService =
        defaults(fallbackLocale, mapOf(key to value))

    override fun defaults(values: Map<String, String>): MessageService = defaults(fallbackLocale, values)

    override fun defaults(locale: Locale, values: Map<String, String>): MessageService = apply {
        codeDefaults.getOrPut(locale) { LinkedHashMap() }.putAll(values)
    }

    override fun load(): MessageService = apply {
        (codeDefaults.keys + fallbackLocale).toSet().forEach(::loadLocale)
    }

    override fun reload(): MessageService = apply {
        catalogs.clear()
        load()
    }

    private fun loadLocale(locale: Locale) {
        val defaults = codeDefaults[locale] ?: emptyMap()
        val file = File(langDir, "${fileName(locale)}.yml")
        val yaml = YamlConfiguration()
        var loadFailed = false
        if (file.exists()) {
            runCatching { yaml.load(file) }.onFailure {
                loadFailed = true
                plugin.logger.warning(
                    "Could not read lang file ${file.name}: ${it.message}. Using defaults; leaving the file untouched so you can fix it.",
                )
            }
        }
        // Write-back: create the file from defaults, and append any default keys not already present.
        // Never write when an existing file failed to parse, or we would clobber the admin's content.
        var changed = !file.exists()
        for ((key, value) in defaults) {
            if (!yaml.isSet(key)) {
                yaml.set(key, value)
                changed = true
            }
        }
        if (changed && !loadFailed) {
            runCatching {
                langDir.mkdirs()
                yaml.save(file)
            }.onFailure { plugin.logger.warning("Could not write lang file ${file.name}: ${it.message}") }
        }
        // Catalog = every string leaf in the file, with the code defaults as a backstop.
        val map = LinkedHashMap<String, String>(defaults)
        for (key in yaml.getKeys(true)) {
            if (yaml.isString(key)) yaml.getString(key)?.let { map[key] = it }
        }
        catalogs[locale] = map
    }

    override fun raw(locale: Locale, key: String): String =
        catalogFor(locale)[key]
            ?: codeDefaults[fallbackLocale]?.get(key)
            ?: missing(key)

    override fun get(viewer: Player?, key: String, vararg resolvers: TagResolver): Component {
        val locale = viewer?.let(::localeOf) ?: fallbackLocale
        return text.render(raw(locale, key), viewer, *resolvers)
    }

    override fun get(locale: Locale, key: String, vararg resolvers: TagResolver): Component =
        text.render(raw(locale, key), null, *resolvers)

    override fun send(sender: CommandSender, key: String, vararg resolvers: TagResolver) {
        val component =
            if (sender is Player) get(sender, key, *resolvers) else get(fallbackLocale, key, *resolvers)
        sender.sendMessage(component)
    }

    override fun actionBar(player: Player, key: String, vararg resolvers: TagResolver) {
        player.sendActionBar(get(player, key, *resolvers))
    }

    override fun broadcast(key: String, vararg resolvers: TagResolver) {
        Bukkit.getOnlinePlayers().forEach { send(it, key, *resolvers) }
    }

    override fun locales(): Set<Locale> = codeDefaults.keys + catalogs.keys

    /** Closest catalog: exact locale, then language-only, then the fallback locale. */
    private fun catalogFor(locale: Locale): Map<String, String> =
        catalogs[locale]
            ?: catalogs[Locale.of(locale.language)]
            ?: catalogs[fallbackLocale]
            ?: codeDefaults[locale]
            ?: codeDefaults[fallbackLocale]
            ?: emptyMap()

    private fun missing(key: String): String {
        if (warnedMissing.add(key)) plugin.logger.warning("Missing message for key '$key'")
        return "missing message: $key"
    }

    private fun localeOf(player: Player): Locale = runCatching { player.locale() }.getOrDefault(fallbackLocale)

    private fun fileName(locale: Locale): String =
        if (locale.country.isNullOrEmpty()) locale.language.lowercase()
        else "${locale.language.lowercase()}_${locale.country.lowercase()}"
}
