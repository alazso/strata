package so.alaz.strata.api.placeholder

import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus
import java.util.function.Function
import java.util.function.Supplier

/**
 * Defines a plugin's own placeholders once and exposes them through **both** PlaceholderAPI and
 * MiniPlaceholders. Obtain it from `StrataApi.placeholders(plugin)`; the placeholder prefix is your
 * plugin's name, so a key `balance` is reachable as `%plugin_balance%` (PlaceholderAPI) and
 * `<plugin_balance>` (MiniPlaceholders).
 *
 * Add placeholders, then call [register] once during `onEnable`. Each backend is wired only when it
 * is installed, so this is safe whether or not PlaceholderAPI or MiniPlaceholders is present.
 *
 * ```java
 * StrataApi.placeholders(this)
 *     .add("balance", player -> economy.format(economy.balance(player)))   // per-player
 *     .addGlobal("online", () -> String.valueOf(Bukkit.getOnlinePlayers().size()))
 *     .register();
 * ```
 *
 * A resolver that returns `null` yields no value for that placeholder. Values are inserted as plain
 * text in the MiniPlaceholders (component) path; style them where you render, not here.
 */
@ApiStatus.Experimental
public interface PlaceholderRegistration {

    /** Adds a per-player placeholder [key] resolved from the viewer. */
    public fun add(key: String, resolver: Function<Player, String?>): PlaceholderRegistration

    /** Adds a global placeholder [key] with no player context. */
    public fun addGlobal(key: String, resolver: Supplier<String?>): PlaceholderRegistration

    /** Registers the placeholders with every installed backend (PlaceholderAPI, MiniPlaceholders). */
    public fun register()

    /** Unregisters the placeholders from every backend they were registered with. */
    public fun unregister()
}
