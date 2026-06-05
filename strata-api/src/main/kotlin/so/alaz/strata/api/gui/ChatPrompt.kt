package so.alaz.strata.api.gui

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus
import java.util.function.Consumer

/**
 * A chat text-input prompt — the fallback when an anvil isn't suitable. The next chat message from
 * the player (which is suppressed from public chat) runs [onInput]; typing [cancelToken] runs
 * [onCancel]. [onInput] returns a [ChatResponse] to end or retry.
 *
 * Open via `StrataApi.gui().openChat(prompt, player)`.
 */
@ApiStatus.Experimental
public class ChatPrompt private constructor(
    public val promptMessage: Component?,
    public val onInput: ChatInput,
    public val onCancel: Consumer<Player>?,
    public val cancelToken: String,
) {

    public class Builder {
        private var promptMessage: Component? = null
        private var onInput: ChatInput = ChatInput { _, _ -> ChatResponse.end() }
        private var onCancel: Consumer<Player>? = null
        private var cancelToken: String = "cancel"

        public fun prompt(message: Component): Builder = apply { this.promptMessage = message }
        public fun onInput(onInput: ChatInput): Builder = apply { this.onInput = onInput }
        public fun onCancel(onCancel: Consumer<Player>): Builder = apply { this.onCancel = onCancel }
        public fun cancelToken(token: String): Builder = apply { this.cancelToken = token }

        public fun build(): ChatPrompt = ChatPrompt(promptMessage, onInput, onCancel, cancelToken)
    }

    public companion object {
        @JvmStatic
        public fun builder(): Builder = Builder()
    }
}

/** Handles a chat line entered for a [ChatPrompt]. Runs on the main/region thread. */
@ApiStatus.Experimental
public fun interface ChatInput {
    public fun handle(player: Player, input: String): ChatResponse
}

/** Whether the chat prompt is satisfied ([end]) or should keep waiting for another line ([retry]). */
@ApiStatus.Experimental
public class ChatResponse private constructor(public val kind: Kind) {
    public enum class Kind { END, RETRY }

    public companion object {
        @JvmStatic public fun end(): ChatResponse = ChatResponse(Kind.END)
        @JvmStatic public fun retry(): ChatResponse = ChatResponse(Kind.RETRY)
    }
}
