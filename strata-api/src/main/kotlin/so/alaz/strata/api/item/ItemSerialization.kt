package so.alaz.strata.api.item

import org.bukkit.inventory.ItemStack
import java.util.Base64

/**
 * Version-safe serialization of [ItemStack]s, for persisting reward items, kits, GUI layouts, and
 * `/give from hand` definitions. Built on Paper's native item serialization, which embeds the data
 * version and migrates older data forward on read, so items survive Minecraft upgrades.
 *
 * Use the byte forms for database `BLOB`/`bytea` columns and the Base64 forms for YAML/JSON config.
 * The single-item and multi-item (inventory) forms are distinct, so decode with the matching method.
 * Malformed input throws [IllegalArgumentException].
 */
public object ItemSerialization {

    /** Serializes a single item to bytes. */
    @JvmStatic
    public fun toBytes(item: ItemStack): ByteArray = item.serializeAsBytes()

    /** Reconstructs a single item from [toBytes]. */
    @JvmStatic
    public fun fromBytes(bytes: ByteArray): ItemStack = ItemStack.deserializeBytes(bytes)

    /** Serializes a single item to a Base64 string (for config files). */
    @JvmStatic
    public fun toBase64(item: ItemStack): String = Base64.getEncoder().encodeToString(toBytes(item))

    /** Reconstructs a single item from [toBase64]. */
    @JvmStatic
    public fun fromBase64(base64: String): ItemStack = fromBytes(decode(base64))

    /** Serializes several items (e.g. an inventory) to bytes, preserving order and empty slots. */
    @JvmStatic
    public fun itemsToBytes(items: Array<ItemStack>): ByteArray = ItemStack.serializeItemsAsBytes(items)

    /** Reconstructs an item array from [itemsToBytes]. */
    @JvmStatic
    public fun itemsFromBytes(bytes: ByteArray): Array<ItemStack> = ItemStack.deserializeItemsFromBytes(bytes)

    /** Serializes several items to a Base64 string. */
    @JvmStatic
    public fun itemsToBase64(items: Array<ItemStack>): String =
        Base64.getEncoder().encodeToString(itemsToBytes(items))

    /** Reconstructs an item array from [itemsToBase64]. */
    @JvmStatic
    public fun itemsFromBase64(base64: String): Array<ItemStack> = itemsFromBytes(decode(base64))

    private fun decode(base64: String): ByteArray =
        try {
            Base64.getDecoder().decode(base64)
        } catch (ex: IllegalArgumentException) {
            throw IllegalArgumentException("Not a valid Base64 item string", ex)
        }
}
