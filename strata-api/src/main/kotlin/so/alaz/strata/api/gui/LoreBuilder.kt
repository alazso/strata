package so.alaz.strata.api.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.jetbrains.annotations.ApiStatus

/** Fluent lore builder. MiniMessage lines are parsed with default italics stripped. */
@ApiStatus.Experimental
public class LoreBuilder {

    private val lines = ArrayList<Component>()

    public fun add(line: Component): LoreBuilder = apply { lines.add(line) }

    public fun add(miniMessage: String): LoreBuilder = apply {
        lines.add(MiniMessage.miniMessage().deserialize(miniMessage).decoration(TextDecoration.ITALIC, false))
    }

    public fun addAll(miniMessageLines: List<String>): LoreBuilder = apply { miniMessageLines.forEach { add(it) } }

    public fun blank(): LoreBuilder = apply { lines.add(Component.empty()) }

    public fun build(): List<Component> = lines.toList()
}
