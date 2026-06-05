package so.alaz.strata.gui

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

/**
 * Marks an inventory as a Strata GUI and carries its [session]. Holder-based identity is the
 * Paper-recommended way to recognise your inventories in events — never match on title.
 */
internal class StrataInventoryHolder(val session: DefaultGuiSession) : InventoryHolder {

    lateinit var backing: Inventory

    override fun getInventory(): Inventory = backing
}
