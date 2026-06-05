package so.alaz.strata.gui

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import so.alaz.strata.api.gui.AnvilPrompt

/** Holder identity for an anvil input prompt. */
internal class AnvilInputHolder(val prompt: AnvilPrompt) : InventoryHolder {

    lateinit var backing: Inventory

    override fun getInventory(): Inventory = backing
}
