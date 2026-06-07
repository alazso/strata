package so.alaz.strata.api.player

import org.jetbrains.annotations.ApiStatus
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Resolves between player names and UUIDs, online or offline, without blocking the server. Obtain it
 * from `StrataApi.players()`.
 *
 * Resolution checks online players and a local cache first (cheap and synchronous via the `cached*`
 * peeks), then falls back to an off-thread Mojang lookup for the async [uuid]/[name] methods. The
 * cache is populated as players join and as lookups resolve. Async methods complete on a pool thread,
 * so schedule any follow-up that touches the server back on the right thread yourself.
 */
@ApiStatus.Experimental
public interface PlayerLookup {

    /** Resolves [name] to a UUID (online, cache, then Mojang). Completes with `null` if unknown. */
    public fun uuid(name: String): CompletableFuture<UUID?>

    /** Resolves [uuid] to a name (online, cache, then Mojang). Completes with `null` if unknown. */
    public fun name(uuid: UUID): CompletableFuture<String?>

    /** Whether [uuid] has joined this server before. Reads player data off-thread. */
    public fun hasPlayedBefore(uuid: UUID): CompletableFuture<Boolean>

    /** Non-blocking: the cached name for [uuid] (online player or a prior lookup), or `null`. */
    public fun cachedName(uuid: UUID): String?

    /** Non-blocking: the cached UUID for [name] (online player or a prior lookup), or `null`. */
    public fun cachedUuid(name: String): UUID?
}
