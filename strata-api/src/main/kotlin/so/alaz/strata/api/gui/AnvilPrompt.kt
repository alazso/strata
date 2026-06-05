package so.alaz.strata.api.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus
import java.util.function.Consumer

/**
 * An anvil text-input prompt (AnvilGUI-style). The player types in the anvil's rename field; clicking
 * the result slot runs [onComplete] with the typed text, which returns an [AnvilResponse] (close,
 * keep open for an invalid entry, or replace the text and continue).
 *
 * Open via `StrataApi.gui().openAnvil(prompt, player)`.
 */
@ApiStatus.Experimental
public class AnvilPrompt private constructor(
    public val title: Component,
    public val initialText: String,
    public val leftItem: ItemStack?,
    public val onComplete: AnvilCompletion,
    public val onClose: Consumer<Player>?,
) {

    /** A copy with a different starting text (used internally for [AnvilResponse.replaceText]). */
    public fun withText(text: String): AnvilPrompt = AnvilPrompt(title, text, leftItem, onComplete, onClose)

    public class Builder {
        private var title: Component = Component.empty()
        private var initialText: String = ""
        private var leftItem: ItemStack? = null
        private var onComplete: AnvilCompletion = AnvilCompletion { _, _ -> AnvilResponse.close() }
        private var onClose: Consumer<Player>? = null

        public fun title(title: Component): Builder = apply { this.title = title }
        public fun title(miniMessage: String): Builder = apply { this.title = MiniMessage.miniMessage().deserialize(miniMessage) }
        public fun text(initialText: String): Builder = apply { this.initialText = initialText }
        public fun item(leftItem: ItemStack): Builder = apply { this.leftItem = leftItem }
        public fun onComplete(onComplete: AnvilCompletion): Builder = apply { this.onComplete = onComplete }
        public fun onClose(onClose: Consumer<Player>): Builder = apply { this.onClose = onClose }

        public fun build(): AnvilPrompt = AnvilPrompt(title, initialText, leftItem, onComplete, onClose)
    }

    public companion object {
        @JvmStatic
        public fun builder(): Builder = Builder()
    }
}

/** Handles the typed text when the anvil result is clicked. */
@ApiStatus.Experimental
public fun interface AnvilCompletion {
    public fun complete(player: Player, input: String): AnvilResponse
}

/** What to do after an anvil entry: close, keep open (e.g. invalid), or replace the text. */
@ApiStatus.Experimental
public class AnvilResponse private constructor(
    public val kind: Kind,
    public val text: String?,
) {
    public enum class Kind { CLOSE, KEEP_OPEN, REPLACE_TEXT }

    public companion object {
        @JvmStatic public fun close(): AnvilResponse = AnvilResponse(Kind.CLOSE, null)
        @JvmStatic public fun keepOpen(): AnvilResponse = AnvilResponse(Kind.KEEP_OPEN, null)
        @JvmStatic public fun replaceText(text: String): AnvilResponse = AnvilResponse(Kind.REPLACE_TEXT, text)
    }
}
