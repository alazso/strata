package so.alaz.strata.api.command

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus
import com.mojang.brigadier.context.CommandContext as BrigadierContext

/** Java-friendly view over a Brigadier command invocation: sender/executor, typed args, and replies. */
@ApiStatus.Experimental
public class CommandContext internal constructor(
    private val handle: BrigadierContext<CommandSourceStack>,
) {

    /** The raw Brigadier source (advanced use). */
    public fun source(): CommandSourceStack = handle.source

    /** Who/what ran the command. */
    public fun sender(): CommandSender = handle.source.sender

    /** The entity executing (may differ from sender for `/execute`), or `null`. */
    public fun executor(): Entity? = handle.source.executor

    /** The player who ran it, or `null` if not a player. */
    public fun player(): Player? = handle.source.executor as? Player ?: handle.source.sender as? Player

    public fun getString(name: String): String = StringArgumentType.getString(handle, name)
    public fun getInt(name: String): Int = IntegerArgumentType.getInteger(handle, name)
    public fun getDouble(name: String): Double = DoubleArgumentType.getDouble(handle, name)
    public fun getBoolean(name: String): Boolean = BoolArgumentType.getBool(handle, name)

    /** Players matched by a [ArgType.player] argument (resolved against the sender's context). */
    public fun getPlayers(name: String): List<Player> =
        handle.getArgument(name, PlayerSelectorArgumentResolver::class.java).resolve(handle.source)

    /** The first player matched by a [ArgType.player] argument. */
    public fun getPlayer(name: String): Player = getPlayers(name).first()

    public fun reply(message: Component) {
        handle.source.sender.sendMessage(message)
    }

    public fun reply(miniMessage: String) {
        handle.source.sender.sendMessage(MiniMessage.miniMessage().deserialize(miniMessage))
    }
}
