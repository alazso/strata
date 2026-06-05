package so.alaz.strata.api.pdc

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType

/**
 * Typed read/write wrapper over a single [PersistentDataContainer] key, removing the raw-key
 * boilerplate and unchecked casting that PDC access otherwise requires.
 *
 * `P` is the primitive storage type, `C` the complex (API) type — matching [PersistentDataType].
 * Most callers use the built-in types, e.g.:
 *
 * ```java
 * PdcKey<String, String> ID = new PdcKey<>(new NamespacedKey(plugin, "id"), PersistentDataType.STRING);
 * ID.set(item.getItemMeta(), "abc");   // remember to setItemMeta(...) afterwards
 * String id = ID.get(item.getItemMeta());
 * ```
 *
 * Note: writes mutate the holder's container in place. For [org.bukkit.inventory.meta.ItemMeta] you
 * must still call `ItemStack#setItemMeta`, and for tile entities `TileState#update()`.
 */
public class PdcKey<P : Any, C : Any>(
    public val key: NamespacedKey,
    public val type: PersistentDataType<P, C>,
) {

    /** Reads the value from [container], or `null` if absent. */
    public fun get(container: PersistentDataContainer): C? = container.get(key, type)

    /** Reads the value from [holder]'s container, or `null` if absent. */
    public fun get(holder: PersistentDataHolder): C? = get(holder.persistentDataContainer)

    /** Reads the value, or [fallback] if absent. */
    public fun getOrDefault(container: PersistentDataContainer, fallback: C): C =
        container.getOrDefault(key, type, fallback)

    /** Reads the value from [holder]'s container, or [fallback] if absent. */
    public fun getOrDefault(holder: PersistentDataHolder, fallback: C): C =
        getOrDefault(holder.persistentDataContainer, fallback)

    /** `true` if [container] holds this key with the matching type. */
    public fun has(container: PersistentDataContainer): Boolean = container.has(key, type)

    /** `true` if [holder]'s container holds this key with the matching type. */
    public fun has(holder: PersistentDataHolder): Boolean = has(holder.persistentDataContainer)

    /** Writes [value] into [container]. */
    public fun set(container: PersistentDataContainer, value: C) {
        container.set(key, type, value)
    }

    /** Writes [value] into [holder]'s container. */
    public fun set(holder: PersistentDataHolder, value: C) {
        set(holder.persistentDataContainer, value)
    }

    /** Removes this key from [container]. */
    public fun remove(container: PersistentDataContainer) {
        container.remove(key)
    }

    /** Removes this key from [holder]'s container. */
    public fun remove(holder: PersistentDataHolder) {
        remove(holder.persistentDataContainer)
    }
}
