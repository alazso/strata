package so.alaz.strata.api.command

import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus

/**
 * Ready-made subcommands for the patterns every plugin repeats. Attach them under your root command:
 *
 * ```java
 * StrataCommand.literal("myplugin")
 *     .then(CommonCommands.reload("myplugin.reload", this::reloadConfig))
 *     .then(CommonCommands.give("myplugin.give", (ctx, target, amount) -> giveKey(target, amount)))
 *     .register(this);
 * ```
 */
@ApiStatus.Experimental
public object CommonCommands {

    /** `reload` — runs [onReload]; replies success or the failure reason. */
    @JvmStatic
    @JvmOverloads
    public fun reload(
        permission: String,
        onReload: Runnable,
        successMessage: String = "<green>Reloaded.",
    ): StrataCommand = StrataCommand.literal("reload")
        .permission(permission)
        .executes { ctx ->
            runCatching { onReload.run() }
                .onSuccess { ctx.reply(successMessage) }
                .onFailure { ctx.reply("<red>Reload failed: ${it.message}") }
        }

    /** `give <target> <amount>` — resolves the player + amount and hands off to [action]. */
    @JvmStatic
    public fun give(permission: String, action: TargetAmountAction): StrataCommand =
        targetAmount("give", permission, action)

    /** `take <target> <amount>` — resolves the player + amount and hands off to [action]. */
    @JvmStatic
    public fun take(permission: String, action: TargetAmountAction): StrataCommand =
        targetAmount("take", permission, action)

    private fun targetAmount(name: String, permission: String, action: TargetAmountAction): StrataCommand =
        StrataCommand.literal(name)
            .permission(permission)
            .then(
                StrataCommand.argument("target", ArgType.player())
                    .then(
                        StrataCommand.argument("amount", ArgType.integer(1, Int.MAX_VALUE))
                            .executes { ctx -> action.apply(ctx, ctx.getPlayer("target"), ctx.getInt("amount")) },
                    ),
            )
}

/** Callback for [CommonCommands.give]/[CommonCommands.take]: do the actual give/take. */
@ApiStatus.Experimental
public fun interface TargetAmountAction {
    public fun apply(context: CommandContext, target: Player, amount: Int)
}
