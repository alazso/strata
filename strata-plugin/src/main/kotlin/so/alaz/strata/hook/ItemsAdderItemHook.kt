package so.alaz.strata.hook

import dev.lone.itemsadder.api.CustomStack
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import so.alaz.strata.api.hook.ItemHook

/**
 * ItemsAdder-backed [ItemHook]. No ItemsAdder-typed fields exist (only a `present` flag); all
 * ItemsAdder references live in wrapped method bodies, so the class loads when ItemsAdder is absent
 * and every lookup degrades to `null`/`false` instead of throwing. Ids are ItemsAdder's
 * `namespace:id` form.
 */
internal class ItemsAdderItemHook : ItemHook {

    private val present: Boolean =
        runCatching { Class.forName("dev.lone.itemsadder.api.CustomStack", false, javaClass.classLoader) }.isSuccess

    override fun name(): String = "ItemsAdder"

    override fun isAvailable(): Boolean =
        present && runCatching { Bukkit.getPluginManager().isPluginEnabled("ItemsAdder") }.getOrDefault(false)

    override fun itemId(item: ItemStack): String? =
        runCatching { CustomStack.byItemStack(item)?.namespacedID }.getOrNull()

    override fun createItem(id: String): ItemStack? =
        runCatching { CustomStack.getInstance(id)?.itemStack }.getOrNull()

    override fun isCustomItem(item: ItemStack): Boolean =
        runCatching { CustomStack.byItemStack(item) != null }.getOrDefault(false)
}
