package so.alaz.strata.api.command

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.jetbrains.annotations.ApiStatus

/**
 * A command argument type. Wraps a Brigadier/Paper [ArgumentType] (so tab-completion and parsing come
 * for free); read the parsed value back from [CommandContext] (`getInt`, `getString`, `getPlayer`, …).
 */
@ApiStatus.Experimental
public class ArgType private constructor(internal val brigadier: ArgumentType<*>) {

    public companion object {
        /** A single unquoted word. */
        @JvmStatic public fun word(): ArgType = ArgType(StringArgumentType.word())

        /** A quoted-or-single-word string. */
        @JvmStatic public fun string(): ArgType = ArgType(StringArgumentType.string())

        /** The rest of the input (must be the last argument). */
        @JvmStatic public fun greedyString(): ArgType = ArgType(StringArgumentType.greedyString())

        @JvmStatic public fun integer(): ArgType = ArgType(IntegerArgumentType.integer())
        @JvmStatic public fun integer(min: Int, max: Int): ArgType = ArgType(IntegerArgumentType.integer(min, max))
        @JvmStatic public fun decimal(): ArgType = ArgType(DoubleArgumentType.doubleArg())
        @JvmStatic public fun decimal(min: Double, max: Double): ArgType = ArgType(DoubleArgumentType.doubleArg(min, max))
        @JvmStatic public fun bool(): ArgType = ArgType(BoolArgumentType.bool())

        /** An online-player selector; read with [CommandContext.getPlayer]/[CommandContext.getPlayers]. */
        @JvmStatic public fun player(): ArgType = ArgType(ArgumentTypes.player())
    }
}
