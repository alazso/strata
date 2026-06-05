package so.alaz.strata.api.hook

import org.bukkit.inventory.ItemStack

/** Custom-item lookups behind one interface (Oraxen, ItemAdder, etc.). */
public interface ItemHook : Hook {

    /** The provider's id for [item], or `null` if it is not one of the provider's custom items. */
    public fun itemId(item: ItemStack): String?

    /** Builds the custom item for [id], or `null` if the id is unknown. */
    public fun createItem(id: String): ItemStack?

    /** `true` if [item] is a custom item from this provider. */
    public fun isCustomItem(item: ItemStack): Boolean
}
