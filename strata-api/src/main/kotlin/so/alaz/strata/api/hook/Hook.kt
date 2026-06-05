package so.alaz.strata.api.hook

/**
 * Base type for an integration with a third-party plugin. Implementations detect their backing
 * plugin and report [isAvailable]; consumers never reference the third-party plugin directly —
 * that indirection is what prevents breakage when an integration's internals change.
 */
public interface Hook {

    /** Name of the backing plugin/implementation (e.g. "LuckPerms", "Bukkit"). */
    public fun name(): String

    /** `true` if this hook can be used right now (backing plugin present and ready). */
    public fun isAvailable(): Boolean
}
