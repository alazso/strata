package so.alaz.strata.api.item

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.Base64
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * Builds `PLAYER_HEAD` [ItemStack]s from the four stable skull sources, with no third-party plugin.
 *
 * - [fromTexture] and [fromUrl] are synchronous and need no network: the head carries a texture
 *   property and the client fetches the image from Mojang's CDN. These are the reliable choices for
 *   permanent icons because they are immune to a player changing their skin.
 * - [fromName] and [fromUuid] are asynchronous: they resolve the player's current skin from Mojang
 *   (off-thread, cached), so they suit dynamic "this player's head" displays but can change when the
 *   player changes skins. The returned [CompletableFuture] completes off the main thread; schedule
 *   any inventory or entity work back on the right thread yourself.
 *
 * Resolved textures for name/uuid lookups are cached, and failed lookups are not cached so they can
 * be retried.
 */
public object SkullBuilder {

    private val executor = Executors.newCachedThreadPool { runnable ->
        Thread(runnable, "strata-skull").apply { isDaemon = true }
    }

    private val textureCache = ConcurrentHashMap<String, CompletableFuture<String?>>()

    /** A head carrying the given base64 `textures` property value (the `eyJ0ZXh0dXJlcyI6...` form). */
    @JvmStatic
    public fun fromTexture(base64: String): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD)
        val meta = item.itemMeta as SkullMeta
        // Deterministic profile id so identical textures yield identical, stackable heads.
        val profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(base64.toByteArray(Charsets.UTF_8)))
        profile.setProperty(ProfileProperty("textures", base64))
        meta.playerProfile = profile
        item.setItemMeta(meta)
        return item
    }

    /** A head for a Minecraft texture URL (e.g. `http://textures.minecraft.net/texture/...`). */
    @JvmStatic
    public fun fromUrl(url: String): ItemStack = fromTexture(base64FromUrl(url))

    /**
     * The inverse of [fromTexture]: the base64 `textures` value carried by [item], or `null` when
     * [item] is not a player head or has no texture profile. Use this to read a head's texture back
     * out (the fiddly Paper-profile bit) so you can persist or compare it.
     */
    @JvmStatic
    public fun textureOf(item: ItemStack): String? {
        val meta = item.itemMeta as? SkullMeta ?: return null
        val profile = meta.playerProfile ?: return null
        return textureValue(profile)
    }

    /**
     * A head for [name]'s current skin, resolved from Mojang off-thread and cached. Completes with a
     * blank head if the name cannot be resolved.
     */
    @JvmStatic
    public fun fromName(name: String): CompletableFuture<ItemStack> =
        resolveTexture("name:${name.lowercase(Locale.ROOT)}") {
            val profile = Bukkit.createProfile(name)
            profile.complete(true)
            textureValue(profile)
        }.thenApply { texture -> texture?.let(::fromTexture) ?: ItemStack(Material.PLAYER_HEAD) }

    /**
     * A head for [uuid]'s current skin, resolved from Mojang off-thread and cached. Completes with a
     * blank head if the uuid cannot be resolved.
     */
    @JvmStatic
    public fun fromUuid(uuid: UUID): CompletableFuture<ItemStack> =
        resolveTexture("uuid:$uuid") {
            val profile = Bukkit.createProfile(uuid)
            profile.complete(true)
            textureValue(profile)
        }.thenApply { texture -> texture?.let(::fromTexture) ?: ItemStack(Material.PLAYER_HEAD) }

    /** Encodes a texture [url] into the base64 `textures` property value used by [fromTexture]. */
    @JvmStatic
    public fun base64FromUrl(url: String): String {
        val json = """{"textures":{"SKIN":{"url":"$url"}}}"""
        return Base64.getEncoder().encodeToString(json.toByteArray(Charsets.UTF_8))
    }

    private fun resolveTexture(key: String, loader: () -> String?): CompletableFuture<String?> =
        textureCache.computeIfAbsent(key) { cacheKey ->
            CompletableFuture.supplyAsync({ runCatching(loader).getOrNull() }, executor)
                // Do not cache a failed lookup, so a transient Mojang failure can be retried.
                .whenComplete { value, _ -> if (value == null) textureCache.remove(cacheKey) }
        }

    private fun textureValue(profile: PlayerProfile): String? =
        profile.properties.firstOrNull { it.name == "textures" }?.value
}
