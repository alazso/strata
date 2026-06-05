package so.alaz.strata.api.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

/**
 * Renders MiniMessage strings (with RGB, gradients, and PlaceholderAPI support) into Adventure
 * [Component]s.
 *
 * **Resolution order is fixed and load-bearing:** PlaceholderAPI placeholders are resolved
 * *first*, then the result is parsed as MiniMessage. Doing it the other way round lets placeholder
 * output be reinterpreted as markup (the CrazyCrates #878 class of bug). Encoding the correct order
 * here once means no Strata plugin can reintroduce it.
 *
 * When PlaceholderAPI is absent, the placeholder pass is skipped gracefully and MiniMessage parsing
 * proceeds unchanged.
 */
public interface TextRenderer {

    /** Parses [input] as MiniMessage with no placeholder resolution. */
    public fun render(input: String): Component

    /**
     * Resolves PlaceholderAPI placeholders against [viewer] (if non-null and PAPI is present),
     * then parses the result as MiniMessage.
     */
    public fun render(input: String, viewer: Player?): Component

    /** As [render], with extra MiniMessage [TagResolver]s (e.g. custom `<arg>` tags). */
    public fun render(input: String, viewer: Player?, vararg resolvers: TagResolver): Component

    /** Renders each line via [render]. */
    public fun render(lines: List<String>, viewer: Player?): List<Component>

    /**
     * Resolves PlaceholderAPI placeholders in [input] against [viewer] **without** MiniMessage
     * parsing, returning the raw resolved string. Returns [input] unchanged when PAPI is absent or
     * [viewer] is `null`. Useful for comparing placeholder output (e.g. the `papi` condition).
     */
    public fun resolve(input: String, viewer: Player?): String
}
