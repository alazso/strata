package so.alaz.strata.hook

import com.nexomc.nexo.api.NexoItems
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import so.alaz.strata.api.hook.ItemHook

/**
 * Nexo-backed [ItemHook] (Nexo is the successor to Oraxen). No Nexo-typed fields exist (only a
 * `present` flag); all Nexo references live in wrapped method bodies, so the class loads when Nexo
 * is absent and every lookup degrades to `null`/`false` instead of throwing.
 */
internal class NexoItemHook : ItemHook {

    private val present: Boolean =
        runCatching { Class.forName("com.nexomc.nexo.api.NexoItems", false, javaClass.classLoader) }.isSuccess

    override fun name(): String = "Nexo"

    override fun isAvailable(): Boolean =
        present && runCatching { Bukkit.getPluginManager().isPluginEnabled("Nexo") }.getOrDefault(false)

    override fun itemId(item: ItemStack): String? = runCatching { NexoItems.idFromItem(item) }.getOrNull()

    override fun createItem(id: String): ItemStack? = runCatching {
        if (NexoItems.exists(id)) NexoItems.itemFromId(id)?.build() else null
    }.getOrNull()

    override fun isCustomItem(item: ItemStack): Boolean = itemId(item) != null
}
