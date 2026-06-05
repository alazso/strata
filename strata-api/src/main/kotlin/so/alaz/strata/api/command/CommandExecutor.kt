package so.alaz.strata.api.command

import org.jetbrains.annotations.ApiStatus

/** Runs a command. Throwing `CommandSyntaxException` surfaces a Brigadier usage/error message. */
@ApiStatus.Experimental
public fun interface CommandExecutor {
    public fun execute(context: CommandContext)
}
