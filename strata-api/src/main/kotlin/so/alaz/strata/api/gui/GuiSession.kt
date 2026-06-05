package so.alaz.strata.api.gui

import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus

/**
 * A live, open GUI for one viewer. Carries the current page and arbitrary per-open state, and lets
 * handlers [refresh] or [close] the menu. Created by the [GuiManager] when a [Gui] is opened.
 */
@ApiStatus.Experimental
public interface GuiSession {

    public fun id(): GuiSessionId

    public fun viewer(): Player

    public fun gui(): Gui

    /** Current page (0-based); meaningful for paginated GUIs. */
    public fun page(): Int

    public fun setPage(page: Int)

    public fun getState(key: String): Any?

    public fun setState(key: String, value: Any)

    /** Re-renders the current menu's items (does not change the title). */
    public fun refresh()

    /** Closes the menu for the viewer. */
    public fun close()
}
