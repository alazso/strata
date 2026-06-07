package so.alaz.strata.player

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import so.alaz.strata.api.player.PlayerLookup
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * Default [PlayerLookup]. Online players and a local cache answer instantly; everything else resolves
 * from Mojang on a daemon pool. The cache is filled on join and whenever a lookup succeeds. All
 * Bukkit access in the synchronous peeks is wrapped so the class is usable (and testable) even before
 * the server is fully up.
 */
internal class DefaultPlayerLookup : PlayerLookup, Listener {

    private val executor = Executors.newCachedThreadPool { runnable ->
        Thread(runnable, "strata-playerlookup").apply { isDaemon = true }
    }

    private val uuidToName = ConcurrentHashMap<UUID, String>()
    private val nameToUuid = ConcurrentHashMap<String, UUID>()

    @EventHandler(priority = EventPriority.MONITOR)
    public fun onJoin(event: PlayerJoinEvent) {
        cache(event.player.uniqueId, event.player.name)
    }

    override fun cachedName(uuid: UUID): String? =
        runCatching { Bukkit.getPlayer(uuid)?.name }.getOrNull() ?: uuidToName[uuid]

    override fun cachedUuid(name: String): UUID? =
        runCatching { Bukkit.getPlayerExact(name)?.uniqueId }.getOrNull()
            ?: nameToUuid[name.lowercase(Locale.ROOT)]

    override fun uuid(name: String): CompletableFuture<UUID?> {
        cachedUuid(name)?.let { return CompletableFuture.completedFuture(it) }
        // Paper's offline cache (no web call) before reaching out to Mojang.
        runCatching { Bukkit.getOfflinePlayerIfCached(name)?.uniqueId }.getOrNull()?.let { id ->
            cache(id, name)
            return CompletableFuture.completedFuture(id)
        }
        return CompletableFuture.supplyAsync({
            runCatching {
                val profile = Bukkit.createProfile(name)
                profile.complete(false)
                profile.id?.also { id -> profile.name?.let { resolved -> cache(id, resolved) } }
            }.getOrNull()
        }, executor)
    }

    override fun name(uuid: UUID): CompletableFuture<String?> {
        cachedName(uuid)?.let { return CompletableFuture.completedFuture(it) }
        return CompletableFuture.supplyAsync({
            runCatching {
                val profile = Bukkit.createProfile(uuid)
                profile.complete(false)
                profile.name?.also { resolved -> cache(uuid, resolved) }
            }.getOrNull()
        }, executor)
    }

    override fun hasPlayedBefore(uuid: UUID): CompletableFuture<Boolean> =
        CompletableFuture.supplyAsync({
            runCatching { Bukkit.getOfflinePlayer(uuid).hasPlayedBefore() }.getOrDefault(false)
        }, executor)

    private fun cache(uuid: UUID, name: String) {
        uuidToName[uuid] = name
        nameToUuid[name.lowercase(Locale.ROOT)] = uuid
    }
}
