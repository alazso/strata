package so.alaz.strata.api.gui

import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus

/**
 * Opens GUIs and tracks open sessions. Inventories are identified by a Strata `InventoryHolder`
 * (never by title), and all click/drag/close events route through here with the safety guards
 * (clicks cancelled by default, drags into the menu blocked).
 *
 * Must be called from the thread that owns the player (main thread on Paper; the player's region
 * thread on Folia — use `StrataApi.scheduler(plugin)`).
 */
@ApiStatus.Experimental
public interface GuiManager {

    /** Opens [gui] for [player] and returns the new session. */
    public fun open(gui: Gui, player: Player): GuiSession

    /** The player's currently-open Strata GUI session, or `null`. */
    public fun sessionOf(player: Player): GuiSession?

    /** Closes all open Strata GUIs (e.g. on plugin disable). */
    public fun closeAll()

    /** Opens an anvil text-input [prompt] for [player]. */
    public fun openAnvil(prompt: AnvilPrompt, player: Player)

    /** Starts a chat text-input [prompt] for [player] (closes any open inventory first). */
    public fun openChat(prompt: ChatPrompt, player: Player)
}
