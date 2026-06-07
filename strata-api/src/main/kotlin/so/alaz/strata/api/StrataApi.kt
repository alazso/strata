package so.alaz.strata.api

import org.bukkit.plugin.Plugin
import org.jetbrains.annotations.ApiStatus
import so.alaz.strata.api.condition.ConditionRegistry
import so.alaz.strata.api.gui.GuiManager
import so.alaz.strata.api.hook.HookRegistry
import so.alaz.strata.api.metrics.MetricsService
import so.alaz.strata.api.player.PlayerLookup
import so.alaz.strata.api.scheduler.PlatformScheduler
import so.alaz.strata.api.storage.StorageFactory
import so.alaz.strata.api.text.TextRenderer

/**
 * Entry point for the Strata public API.
 *
 * Consumers compile against this module (`compileOnly("so.alaz.strata:strata-api:<version>")`) and
 * declare a hard `Strata` dependency (`load: BEFORE`) so the plugin is enabled first. At runtime the
 * accessors below return the live implementations.
 *
 * The surface is consumable cleanly from pure Java as well as Kotlin: no `suspend` on public
 * members, `@JvmStatic`/`@JvmOverloads` where relevant, and no Kotlin-only types in signatures.
 */
@ApiStatus.Experimental
public object StrataApi {

    /** Semantic version of the API surface. */
    public const val VERSION: String = "0.1.0"

    @Volatile
    private var provider: StrataProvider? = null

    /** `true` once the Strata plugin has enabled and registered its services. */
    @JvmStatic
    public fun isAvailable(): Boolean = provider != null

    /** A [PlatformScheduler] bound to [plugin]; tasks are owned by and cancelled with that plugin. */
    @JvmStatic
    public fun scheduler(plugin: Plugin): PlatformScheduler = require().scheduler(plugin)

    /** The shared [TextRenderer]. */
    @JvmStatic
    public fun text(): TextRenderer = require().text()

    /** The [StorageFactory] for building per-plugin storage providers. */
    @JvmStatic
    public fun storage(): StorageFactory = require().storage()

    /** The integration [HookRegistry] (permissions, economy, regions, items, holograms). */
    @JvmStatic
    public fun hooks(): HookRegistry = require().hooks()

    /** The [MetricsService] (bStats + FastStats, shaded and relocated inside Strata). */
    @JvmStatic
    public fun metrics(): MetricsService = require().metrics()

    /** The [ConditionRegistry] (built-in condition types + plugin-registered ones). */
    @JvmStatic
    public fun conditions(): ConditionRegistry = require().conditions()

    /** The [GuiManager] for opening holder-based chest menus. */
    @JvmStatic
    public fun gui(): GuiManager = require().gui()

    /** The shared [PlayerLookup] for resolving player names and UUIDs. */
    @JvmStatic
    public fun players(): PlayerLookup = require().players()

    private fun require(): StrataProvider = provider ?: error(
        "Strata is not enabled yet. Declare a hard 'Strata' dependency (load: BEFORE) in paper-plugin.yml.",
    )

    /** Called by the Strata plugin on enable. Not for consumer use. */
    @ApiStatus.Internal
    @JvmStatic
    public fun register(provider: StrataProvider) {
        this.provider = provider
    }

    /** Called by the Strata plugin on disable. Not for consumer use. */
    @ApiStatus.Internal
    @JvmStatic
    public fun unregister() {
        this.provider = null
    }
}
