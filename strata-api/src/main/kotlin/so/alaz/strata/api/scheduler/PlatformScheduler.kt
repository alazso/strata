package so.alaz.strata.api.scheduler

import org.bukkit.Location
import org.bukkit.entity.Entity

/**
 * Folia-safe scheduling abstraction over Paper's Region/Global/Async/Entity schedulers.
 *
 * This is the single sanctioned scheduling entry point for Strata plugins — never call
 * `Bukkit.getScheduler()` directly. Each consumer obtains a scheduler bound to its own plugin
 * (so tasks are owned by, and cancelled with, that plugin) via `StrataApi.scheduler(plugin)`.
 *
 * Tick-based delays are in server ticks (20/sec); async delays are in milliseconds.
 */
public interface PlatformScheduler {

    // --- Global region (main-thread-equivalent work that isn't tied to a location) ---

    /** Runs on the global region thread, as soon as possible. */
    public fun global(task: Runnable): StrataTask

    /** Runs on the global region thread after [delayTicks]. */
    public fun globalLater(delayTicks: Long, task: Runnable): StrataTask

    /** Repeats on the global region thread. */
    public fun globalTimer(delayTicks: Long, periodTicks: Long, task: Runnable): StrataTask

    // --- Region (work owning a specific world location) ---

    /** Runs on the region thread owning [location]. */
    public fun region(location: Location, task: Runnable): StrataTask

    /** Runs on the region thread owning [location] after [delayTicks]. */
    public fun regionLater(location: Location, delayTicks: Long, task: Runnable): StrataTask

    /** Repeats on the region thread owning [location]. */
    public fun regionTimer(location: Location, delayTicks: Long, periodTicks: Long, task: Runnable): StrataTask

    // --- Entity (work that follows an entity across regions) ---

    /** Runs on the region thread currently owning [entity]. */
    public fun entity(entity: Entity, task: Runnable): StrataTask

    /**
     * Runs on the region thread currently owning [entity]. If the entity is removed before the
     * task runs, [retired] is invoked instead (may be `null`).
     */
    public fun entity(entity: Entity, task: Runnable, retired: Runnable?): StrataTask

    /** Runs on the region thread owning [entity] after [delayTicks]. */
    public fun entityLater(entity: Entity, delayTicks: Long, task: Runnable, retired: Runnable?): StrataTask

    // --- Async (off the server threads entirely) ---

    /** Runs on a dedicated async thread, as soon as possible. */
    public fun async(task: Runnable): StrataTask

    /** Runs async after [delayMillis] milliseconds. */
    public fun asyncLater(delayMillis: Long, task: Runnable): StrataTask

    /** Repeats async with millisecond timing. */
    public fun asyncTimer(delayMillis: Long, periodMillis: Long, task: Runnable): StrataTask
}
