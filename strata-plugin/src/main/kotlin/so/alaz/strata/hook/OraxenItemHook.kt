package so.alaz.strata.hook

import io.th0rgal.oraxen.api.OraxenItems
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import so.alaz.strata.api.hook.ItemHook

/**
 * Oraxen-backed [ItemHook]. As with every Strata hook, no Oraxen-typed fields exist (only a
 * `present` flag); all Oraxen references live in method bodies and are wrapped, so the class loads
 * when Oraxen is absent and every lookup degrades to `null`/`false` instead of throwing.
 */
internal class OraxenItemHook : ItemHook {

    private val present: Boolean =
        runCatching { Class.forName("io.th0rgal.oraxen.api.OraxenItems", false, javaClass.classLoader) }.isSuccess

    override fun name(): String = "Oraxen"

    override fun isAvailable(): Boolean =
        present && runCatching { Bukkit.getPluginManager().isPluginEnabled("Oraxen") }.getOrDefault(false)

    override fun itemId(item: ItemStack): String? = runCatching { OraxenItems.getIdByItem(item) }.getOrNull()

    override fun createItem(id: String): ItemStack? = runCatching {
        if (OraxenItems.exists(id)) OraxenItems.getItemById(id).build() else null
    }.getOrNull()

    override fun isCustomItem(item: ItemStack): Boolean = itemId(item) != null
}
