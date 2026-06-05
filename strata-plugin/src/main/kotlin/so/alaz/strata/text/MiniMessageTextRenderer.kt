package so.alaz.strata.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
import so.alaz.strata.api.text.TextRenderer

/**
 * [TextRenderer] over Adventure's [MiniMessage]. PlaceholderAPI is resolved *before* MiniMessage
 * parsing (see [TextRenderer] docs) and is soft — when PAPI is absent the placeholder pass is
 * skipped. The placeholder pass is also skipped when there is no viewer.
 */
internal class MiniMessageTextRenderer : TextRenderer {

    private val mm: MiniMessage = MiniMessage.miniMessage()
    private val papiAvailable: Boolean = isClassPresent("me.clip.placeholderapi.PlaceholderAPI")

    override fun render(input: String): Component = mm.deserialize(applyPlaceholders(input, null))

    override fun render(input: String, viewer: Player?): Component =
        mm.deserialize(applyPlaceholders(input, viewer))

    override fun render(input: String, viewer: Player?, vararg resolvers: TagResolver): Component =
        mm.deserialize(applyPlaceholders(input, viewer), *resolvers)

    override fun render(lines: List<String>, viewer: Player?): List<Component> =
        lines.map { render(it, viewer) }

    override fun resolve(input: String, viewer: Player?): String = applyPlaceholders(input, viewer)

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
