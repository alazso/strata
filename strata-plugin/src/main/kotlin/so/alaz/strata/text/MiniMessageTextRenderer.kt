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
        mm.deserialize(applyPlaceholders(input, null), *miniPlaceholderResolvers())

    override fun render(input: String, viewer: Player?): Component =
        mm.deserialize(applyPlaceholders(input, viewer), *miniPlaceholderResolvers())

    override fun render(input: String, viewer: Player?, vararg resolvers: TagResolver): Component =
        mm.deserialize(applyPlaceholders(input, viewer), *(miniPlaceholderResolvers() + resolvers))

    override fun render(lines: List<String>, viewer: Player?): List<Component> =
        lines.map { render(it, viewer) }

    override fun resolve(input: String, viewer: Player?): String = applyPlaceholders(input, viewer)

    /**
     * MiniPlaceholders' component-safe resolver covering both global and audience placeholders. The
     * audience for audience-scoped tags comes from the MiniMessage parse context, so global tags
     * resolve unconditionally and audience tags resolve where a target is present. Resolved fresh per
     * render so expansions registered after Strata enables are picked up. Empty (and harmless) when
     * MiniPlaceholders is not installed; wrapped so a misbehaving provider never breaks rendering.
     */
    private fun miniPlaceholderResolvers(): Array<TagResolver> {
        if (!miniPlaceholdersAvailable) return emptyArray()
        return runCatching { arrayOf(MiniPlaceholders.audienceGlobalPlaceholders()) }.getOrDefault(emptyArray())
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
