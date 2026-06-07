package so.alaz.strata.api

import org.bukkit.plugin.Plugin
import org.jetbrains.annotations.ApiStatus
import so.alaz.strata.api.condition.ConditionRegistry
import so.alaz.strata.api.gui.GuiManager
import so.alaz.strata.api.hook.HookRegistry
import so.alaz.strata.api.message.MessageService
import so.alaz.strata.api.metrics.MetricsService
import so.alaz.strata.api.player.PlayerLookup
import so.alaz.strata.api.scheduler.PlatformScheduler
import so.alaz.strata.api.storage.StorageFactory
import so.alaz.strata.api.text.TextRenderer

/**
 * Backing contract that the Strata plugin registers with [StrataApi] on enable. Consumers go
 * through [StrataApi]; this interface is an internal seam.
 */
@ApiStatus.Internal
public interface StrataProvider {

    /** A scheduler bound to [plugin] (tasks are owned by, and cancelled with, that plugin). */
    public fun scheduler(plugin: Plugin): PlatformScheduler

    /** The shared text renderer. */
    public fun text(): TextRenderer

    /** The storage provider factory. */
    public fun storage(): StorageFactory

    /** The integration hook registry. */
    public fun hooks(): HookRegistry

    /** The metrics service (bStats + FastStats). */
    public fun metrics(): MetricsService

    /** The condition registry (built-in + plugin-registered condition types). */
    public fun conditions(): ConditionRegistry

    /** The GUI manager (holder-based chest menus). */
    public fun gui(): GuiManager

    /** The shared player name/UUID lookup. */
    public fun players(): PlayerLookup

    /** The message catalog bound to [plugin] (cached per plugin). */
    public fun messages(plugin: Plugin): MessageService
}
