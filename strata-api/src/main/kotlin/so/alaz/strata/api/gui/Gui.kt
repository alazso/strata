package so.alaz.strata.api.gui

import net.kyori.adventure.text.Component
import org.jetbrains.annotations.ApiStatus

/**
 * A menu blueprint. The framework calls [render] to obtain the slot→[Button] map for a session (so
 * paginated/stateful GUIs can vary by [GuiSession.page] or state), and [title]/[rows] to size the
 * inventory. Implement directly for custom menus, or use [ChestGui]/[PaginatedGui] builders.
 */
@ApiStatus.Experimental
public interface Gui {

    /** The inventory title (may depend on session state, e.g. page x/y). Fixed at open time. */
    public fun title(session: GuiSession): Component

    /** Row count, 1..6. */
    public fun rows(): Int

    /** The buttons to place this render, keyed by raw slot index. */
    public fun render(session: GuiSession): Map<Int, Button>

    public fun onOpen(session: GuiSession) {}

    public fun onClose(session: GuiSession) {}
}
