package so.alaz.strata.gui

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import so.alaz.strata.api.gui.Gui
import so.alaz.strata.api.gui.GuiSession
import so.alaz.strata.api.gui.GuiSessionId

internal class DefaultGuiSession(
    private val id: GuiSessionId,
    private val viewer: Player,
    private val gui: Gui,
    private val manager: DefaultGuiManager,
) : GuiSession {

    var inventory: Inventory? = null
    private var page: Int = 0
    private val state = HashMap<String, Any>()

    override fun id(): GuiSessionId = id
    override fun viewer(): Player = viewer
    override fun gui(): Gui = gui
    override fun page(): Int = page
    override fun setPage(page: Int) { this.page = page }
    override fun getState(key: String): Any? = state[key]
    override fun setState(key: String, value: Any) { state[key] = value }
    override fun refresh() = manager.render(this)
    override fun close() { viewer.closeInventory() }
}
