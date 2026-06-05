package so.alaz.strata.api.hook

import org.bukkit.Location

/**
 * Hologram management behind one interface (DecentHolograms, FancyHolograms, CMI, etc.).
 * Holograms are addressed by a caller-chosen `id`. Line strings are rendered per the provider's
 * own formatting; pass already-rendered text where the provider expects it.
 */
public interface HologramHook : Hook {

    /** Creates a hologram [id] at [location] with [lines]; returns `true` on success. */
    public fun create(id: String, location: Location, lines: List<String>): Boolean

    /** Replaces the lines of hologram [id]; returns `true` on success. */
    public fun update(id: String, lines: List<String>): Boolean

    /** Removes hologram [id]; returns `true` if it existed. */
    public fun remove(id: String): Boolean

    /** `true` if a hologram with [id] exists. */
    public fun exists(id: String): Boolean
}
