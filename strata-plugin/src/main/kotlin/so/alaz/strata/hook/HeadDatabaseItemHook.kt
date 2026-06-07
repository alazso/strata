package so.alaz.strata.hook

import me.arcaniax.hdb.api.HeadDatabaseAPI
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import so.alaz.strata.api.hook.ItemHook

/**
 * Head Database (Arcaniax) as an [ItemHook]: its `getItemHead(id)` returns a finished head
 * [ItemStack], which is exactly the [createItem] contract, so HDB heads flow through the same
 * custom-item path as ItemsAdder/Oraxen/Nexo. Ids are HDB's numeric ids, optionally prefixed
 * `hdb:` or `headdatabase:` so a stored id round-trips unambiguously back to this provider.
 *
 * No HDB-typed fields exist (only a `present` flag); all HDB references live in wrapped method
 * bodies, so the class loads when HDB is absent and every lookup degrades to `null`/`false`. Heads
 * resolve once HDB finishes loading its database (shortly after startup, see DatabaseLoadEvent);
 * before then lookups simply return `null`.
 */
internal class HeadDatabaseItemHook : ItemHook {

    private val present: Boolean =
        runCatching { Class.forName("me.arcaniax.hdb.api.HeadDatabaseAPI", false, javaClass.classLoader) }.isSuccess

    override fun name(): String = "HeadDatabase"

    override fun isAvailable(): Boolean =
        present && runCatching { Bukkit.getPluginManager().isPluginEnabled("HeadDatabase") }.getOrDefault(false)

    override fun itemId(item: ItemStack): String? =
        runCatching { HeadDatabaseAPI().getItemID(item)?.let { "hdb:$it" } }.getOrNull()

    override fun createItem(id: String): ItemStack? =
        runCatching { HeadDatabaseAPI().getItemHead(headId(id)) }.getOrNull()

    override fun isCustomItem(item: ItemStack): Boolean = itemId(item) != null

    /** Strips a `hdb:`/`headdatabase:` prefix if present, leaving the bare HDB id. */
    private fun headId(id: String): String =
        PREFIXES.firstOrNull { id.startsWith(it, ignoreCase = true) }?.let { id.substring(it.length) } ?: id

    private companion object {
        val PREFIXES = listOf("hdb:", "headdatabase:")
    }
}
