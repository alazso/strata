package so.alaz.strata.api.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus
import java.util.Locale

/**
 * A per-plugin message catalog: keyed messages, in one or more locales, rendered through the shared
 * [TextRenderer][so.alaz.strata.api.text.TextRenderer] (so MiniMessage, PlaceholderAPI, and
 * MiniPlaceholders all apply). Obtain one with `StrataApi.messages(plugin)`; the instance is cached
 * per plugin.
 *
 * **Setup is a one-time chain in your `onEnable`:** register code defaults (so the plugin works out
 * of the box and lang files can be generated), then call [load]:
 *
 * ```java
 * StrataApi.messages(this)
 *     .defaultLocale(Locale.ENGLISH)
 *     .defaults(Map.of(
 *         "no-permission", "<red>You lack permission.",
 *         "welcome",       "<green>Welcome, <white><player></white>!"))
 *     .defaults(Locale.GERMAN, Map.of("no-permission", "<red>Keine Berechtigung."))
 *     .load();
 * ```
 *
 * [load] writes a `lang/<locale>.yml` file for each registered locale (creating it from the defaults
 * and adding any newly-introduced keys to existing files), then reads admin edits back. After that,
 * send messages by key; players see their own locale automatically:
 *
 * ```java
 * StrataApi.messages(this).send(player, "welcome", Placeholder.unparsed("player", player.getName()));
 * ```
 */
@ApiStatus.Experimental
public interface MessageService {

    // --- setup (chainable) -----------------------------------------------------------------------

    /** Sets the fallback locale used when a viewer's locale has no catalog. Defaults to English. */
    public fun defaultLocale(locale: Locale): MessageService

    /** Registers one code default for the default locale. */
    public fun default(key: String, value: String): MessageService

    /** Registers code defaults for the default locale. */
    public fun defaults(values: Map<String, String>): MessageService

    /** Registers code defaults for a specific [locale], enabling that locale. */
    public fun defaults(locale: Locale, values: Map<String, String>): MessageService

    /**
     * Loads `lang/<locale>.yml` for every registered locale, creating missing files from the code
     * defaults and appending any default keys not yet present (so updates surface new messages), then
     * reading the files back. Call once after registering defaults. Returns `this`.
     */
    public fun load(): MessageService

    /** Re-reads the lang files from disk without changing registered code defaults. */
    public fun reload(): MessageService

    // --- lookup ----------------------------------------------------------------------------------

    /** The raw template string for [key] in [locale], falling back to the default locale then [key]. */
    public fun raw(locale: Locale, key: String): String

    /** Renders [key] for [viewer]'s locale (or the default locale when null), applying [resolvers]. */
    public fun get(viewer: Player?, key: String, vararg resolvers: TagResolver): Component

    /** Renders [key] for an explicit [locale], applying [resolvers]. */
    public fun get(locale: Locale, key: String, vararg resolvers: TagResolver): Component

    // --- sending ---------------------------------------------------------------------------------

    /** Renders [key] for [sender] (a player's own locale, or the default for console) and sends it. */
    public fun send(sender: CommandSender, key: String, vararg resolvers: TagResolver)

    /** Renders [key] for [player]'s locale and sends it to the action bar. */
    public fun actionBar(player: Player, key: String, vararg resolvers: TagResolver)

    /** Renders [key] per recipient locale and sends it to every online player. */
    public fun broadcast(key: String, vararg resolvers: TagResolver)

    /** The locales this catalog knows (registered defaults and loaded files). */
    public fun locales(): Set<Locale>
}
