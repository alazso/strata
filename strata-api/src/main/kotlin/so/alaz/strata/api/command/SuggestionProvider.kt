package so.alaz.strata.api.command

import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus
import com.mojang.brigadier.context.CommandContext as BrigadierContext

/**
 * Supplies tab-completion candidates for an argument attached with [StrataCommand.suggests].
 *
 * Return every candidate that makes sense for the [context]; Strata filters them by the partial
 * token the player has typed (case-insensitive prefix) before showing them, so implementations do
 * not need to filter themselves.
 *
 * ```java
 * .then(StrataCommand.argument("id", ArgType.word())
 *     .suggests(Suggestions.from(registry::voucherIds))
 *     .executes(ctx -> give(ctx)))
 * ```
 */
@ApiStatus.Experimental
public fun interface SuggestionProvider {
    public fun suggest(context: SuggestionContext): Collection<String>
}

/** Context for a suggestion request: who is completing, and what they have typed so far. */
@ApiStatus.Experimental
public class SuggestionContext internal constructor(
    private val handle: BrigadierContext<CommandSourceStack>,
    private val builder: SuggestionsBuilder,
) {

    /** Who is completing the command. */
    public fun sender(): CommandSender = handle.source.sender

    /** The completing player, or `null` if not a player. */
    public fun player(): Player? = handle.source.executor as? Player ?: handle.source.sender as? Player

    /** The partial token being completed (the text after the last space); empty when none. */
    public fun remaining(): String = builder.remaining
}
