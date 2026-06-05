package so.alaz.strata.api.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus

/**
 * Fluent [ItemStack] builder with MiniMessage names/lore (Adventure). Display name and lore get the
 * default italic styling stripped (the usual menu-item expectation). [build] requires a running
 * server (item meta).
 */
@ApiStatus.Experimental
public class ItemBuilder(private val material: Material) {

    private var amount: Int = 1
    private var name: Component? = null
    private var lore: List<Component> = emptyList()
    private var glow: Boolean = false

    public fun amount(amount: Int): ItemBuilder = apply { this.amount = amount }
    public fun name(name: Component): ItemBuilder = apply { this.name = name }
    public fun name(miniMessage: String): ItemBuilder = apply { this.name = MiniMessage.miniMessage().deserialize(miniMessage) }
    public fun lore(lore: List<Component>): ItemBuilder = apply { this.lore = lore }
    public fun loreStrings(lines: List<String>): ItemBuilder = apply {
        this.lore = lines.map { MiniMessage.miniMessage().deserialize(it) }
    }
    public fun glow(glow: Boolean): ItemBuilder = apply { this.glow = glow }

    public fun build(): ItemStack {
        val item = ItemStack(material, amount)
        item.editMeta { meta ->
            name?.let { meta.displayName(it.decoration(TextDecoration.ITALIC, false)) }
            if (lore.isNotEmpty()) meta.lore(lore.map { it.decoration(TextDecoration.ITALIC, false) })
            if (glow) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true)
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            }
        }
        return item
    }
}
