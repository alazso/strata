package so.alaz.strata.api.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.jetbrains.annotations.ApiStatus
import java.util.Locale

/**
 * Fluent builder for a Brigadier command tree, hiding the Brigadier/Mojang generics. Build a literal
 * root, attach subcommands and arguments with [then], set [permission]s and [executes] handlers, then
 * [register] it for your plugin (call during `onEnable`).
 *
 * ```java
 * StrataCommand.literal("shop")
 *     .permission("shop.use")
 *     .executes(ctx -> StrataApi.gui().open(shopGui, ctx.player()))
 *     .then(StrataCommand.literal("give")
 *         .permission("shop.admin")
 *         .then(StrataCommand.argument("target", ArgType.player())
 *             .then(StrataCommand.argument("amount", ArgType.integer(1, 64))
 *                 .executes(ctx -> give(ctx.getPlayer("target"), ctx.getInt("amount"))))))
 *     .register(plugin, "Shop command", List.of("market"));
 * ```
 */
@ApiStatus.Experimental
public class StrataCommand private constructor(
    private val literalName: String?,
    private val argName: String?,
    private val argType: ArgType?,
) {

    private var permission: String? = null
    private var executor: CommandExecutor? = null
    private var suggestionProvider: SuggestionProvider? = null
    private var description: String? = null
    private var usage: String? = null
    private val children = ArrayList<StrataCommand>()

    /** Requires permission [permission] (and implicit op-or-permission semantics) for this node. */
    public fun permission(permission: String): StrataCommand = apply { this.permission = permission }

    /** Sets the handler run when the command ends at this node. */
    public fun executes(executor: CommandExecutor): StrataCommand = apply { this.executor = executor }

    /**
     * Supplies tab-completion candidates for this argument node. Only valid on argument nodes
     * (created with [argument]); has no effect on a literal. Candidates are filtered by the partial
     * token before display, so a provider returns the full candidate set.
     */
    public fun suggests(provider: SuggestionProvider): StrataCommand = apply { this.suggestionProvider = provider }

    /** A one-line description for this node, surfaced by [helpEntries]. */
    public fun description(description: String): StrataCommand = apply { this.description = description }

    /** The human-readable argument syntax for this node (e.g. `give <id> [amount]`), surfaced by [helpEntries]. */
    public fun usage(usage: String): StrataCommand = apply { this.usage = usage }

    /** Adds a subcommand or argument child. */
    public fun then(child: StrataCommand): StrataCommand = apply { children.add(child) }

    /**
     * The immediate subcommands [sender] is allowed to see, for rendering a help menu. A child is
     * included when it is a literal and either has no permission or the sender holds it.
     */
    public fun helpEntries(sender: CommandSender): List<HelpEntry> =
        children
            .filter { child -> child.literalName != null && (child.permission == null || sender.hasPermission(child.permission!!)) }
            .map { child -> HelpEntry(child.literalName!!, child.usage, child.description) }

    /** Builds the Brigadier node (root must be a literal). Power-users can register it manually. */
    public fun toBrigadier(): LiteralCommandNode<CommandSourceStack> {
        check(literalName != null) { "The root command must be a literal" }
        @Suppress("UNCHECKED_CAST")
        return build(this).build() as LiteralCommandNode<CommandSourceStack>
    }

    /** Registers this command for [plugin] through Paper's command lifecycle. Call during `onEnable`. */
    @JvmOverloads
    public fun register(plugin: Plugin, description: String? = null, aliases: List<String> = emptyList()) {
        check(literalName != null) { "The root command must be a literal" }
        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val node = toBrigadier()
            val registrar = event.registrar()
            when {
                description == null && aliases.isEmpty() -> registrar.register(node)
                aliases.isEmpty() -> registrar.register(node, description)
                else -> registrar.register(node, description, aliases)
            }
        }
    }

    private fun build(node: StrataCommand): ArgumentBuilder<CommandSourceStack, *> {
        val builder: ArgumentBuilder<CommandSourceStack, *> =
            if (node.literalName != null) Commands.literal(node.literalName)
            else Commands.argument(node.argName!!, node.argType!!.brigadier)

        node.permission?.let { perm -> builder.requires { source -> source.sender.hasPermission(perm) } }
        node.executor?.let { exec ->
            builder.executes { ctx -> exec.execute(CommandContext(ctx)); Command.SINGLE_SUCCESS }
        }
        if (node.argName != null && node.suggestionProvider != null) {
            @Suppress("UNCHECKED_CAST")
            val argBuilder = builder as RequiredArgumentBuilder<CommandSourceStack, *>
            val provider = node.suggestionProvider!!
            argBuilder.suggests { ctx, suggestions ->
                val context = SuggestionContext(ctx, suggestions)
                val typed = suggestions.remaining.lowercase(Locale.ROOT)
                provider.suggest(context).forEach { candidate ->
                    if (candidate.lowercase(Locale.ROOT).startsWith(typed)) {
                        suggestions.suggest(candidate)
                    }
                }
                suggestions.buildFuture()
            }
        }
        node.children.forEach { builder.then(build(it)) }
        return builder
    }

    /** A single subcommand entry for a help listing: its [name], optional [usage] syntax, and [description]. */
    public class HelpEntry internal constructor(
        public val name: String,
        public val usage: String?,
        public val description: String?,
    )

    public companion object {
        @JvmStatic public fun literal(name: String): StrataCommand = StrataCommand(name, null, null)

        @JvmStatic public fun argument(name: String, type: ArgType): StrataCommand = StrataCommand(null, name, type)
    }
}
