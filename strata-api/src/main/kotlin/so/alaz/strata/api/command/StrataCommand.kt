package so.alaz.strata.api.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.Plugin
import org.jetbrains.annotations.ApiStatus

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
    private val children = ArrayList<StrataCommand>()

    /** Requires permission [permission] (and implicit op-or-permission semantics) for this node. */
    public fun permission(permission: String): StrataCommand = apply { this.permission = permission }

    /** Sets the handler run when the command ends at this node. */
    public fun executes(executor: CommandExecutor): StrataCommand = apply { this.executor = executor }

    /** Adds a subcommand or argument child. */
    public fun then(child: StrataCommand): StrataCommand = apply { children.add(child) }

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
        node.children.forEach { builder.then(build(it)) }
        return builder
    }

    public companion object {
        @JvmStatic public fun literal(name: String): StrataCommand = StrataCommand(name, null, null)

        @JvmStatic public fun argument(name: String, type: ArgType): StrataCommand = StrataCommand(null, name, type)
    }
}
