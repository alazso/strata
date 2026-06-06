package so.alaz.strata.text

import io.github.miniplaceholders.api.MiniPlaceholders
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
import so.alaz.strata.api.text.TextRenderer

/**
 * [TextRenderer] over Adventure's [MiniMessage]. Two integrations layer on top, both soft:
 *
 * - **PlaceholderAPI** (string placeholders) is resolved *before* MiniMessage parsing (see
 *   [TextRenderer] docs); skipped when PAPI is absent or there is no viewer.
 * - **MiniPlaceholders** (component-safe placeholders) is added as MiniMessage [TagResolver]s, so
 *   `<some_placeholder>` tags resolve during parsing. Global placeholders resolve everywhere;
 *   audience-scoped ones resolve when a target audience is present in the parse context. Skipped
 *   when MiniPlaceholders is absent.
 *
 * Both are free to every consumer and degrade to plain MiniMessage when their plugin is missing.
 */
internal class MiniMessageTextRenderer : TextRenderer {

    private val mm: MiniMessage = MiniMessage.miniMessage()
    private val papiAvailable: Boolean = isClassPresent("me.clip.placeholderapi.PlaceholderAPI")
    private val miniPlaceholdersAvailable: Boolean =
        isClassPresent("io.github.miniplaceholders.api.MiniPlaceholders")

    override fun render(input: String): Component =
        deserialize(applyPlaceholders(input, null))

    override fun render(input: String, viewer: Player?): Component =
        deserialize(applyPlaceholders(input, viewer))

    override fun render(input: String, viewer: Player?, vararg resolvers: TagResolver): Component =
        deserialize(applyPlaceholders(input, viewer), resolvers)

    override fun render(lines: List<String>, viewer: Player?): List<Component> =
        lines.map { render(it, viewer) }

    override fun resolve(input: String, viewer: Player?): String = applyPlaceholders(input, viewer)

    /**
     * Parses [resolved] as MiniMessage with the caller's [callerResolvers] plus MiniPlaceholders'
     * component-safe resolvers (when installed). Caller resolvers come **first** so an explicit
     * per-call tag always wins over a same-named MiniPlaceholders expansion. If a MiniPlaceholders
     * expansion throws during parsing, it falls back to parsing without the MiniPlaceholders
     * resolvers, so a misbehaving provider can never break rendering. When MiniPlaceholders is
     * absent the call is a plain `deserialize` and genuine MiniMessage errors surface unchanged.
     */
    private fun deserialize(resolved: String, callerResolvers: Array<out TagResolver> = emptyArray()): Component {
        val miniPlaceholders = miniPlaceholderResolver() ?: return mm.deserialize(resolved, *callerResolvers)
        return runCatching { mm.deserialize(resolved, *callerResolvers, miniPlaceholders) }
            .getOrElse { mm.deserialize(resolved, *callerResolvers) }
    }

    /**
     * MiniPlaceholders' component-safe resolver covering both global and audience placeholders. The
     * audience for audience-scoped tags comes from the MiniMessage parse context, so global tags
     * resolve unconditionally and audience tags resolve where a target is present. Resolved fresh per
     * render so expansions registered after Strata enables are picked up. `null` (and harmless) when
     * MiniPlaceholders is not installed or the provider misbehaves while building the resolver.
     */
    private fun miniPlaceholderResolver(): TagResolver? {
        if (!miniPlaceholdersAvailable) return null
        return runCatching { MiniPlaceholders.audienceGlobalPlaceholders() }.getOrNull()
    }

    /** PAPI FIRST, then MiniMessage — the order is the whole point (CrazyCrates #878). */
    private fun applyPlaceholders(input: String, viewer: Player?): String {
        if (viewer == null || !papiAvailable) return input
        // Fail-safe: a misbehaving PlaceholderAPI (or expansion) must not break text rendering.
        return runCatching { me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(viewer, input) }
            .getOrDefault(input)
    }

    private fun isClassPresent(name: String): Boolean = try {
        Class.forName(name, false, javaClass.classLoader)
        true
    } catch (_: ClassNotFoundException) {
        false
    }
}
