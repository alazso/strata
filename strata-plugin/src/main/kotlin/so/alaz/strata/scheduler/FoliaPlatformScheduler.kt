package so.alaz.strata.scheduler

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import so.alaz.strata.api.scheduler.PlatformScheduler
import so.alaz.strata.api.scheduler.StrataTask
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * [PlatformScheduler] backed by Paper's region-threaded schedulers — behaves identically on Paper
 * and Folia. Bound to a single owning [plugin] so its tasks are cancelled when that plugin disables.
 *
 * Tick delays are coerced to a minimum of 1, since Paper rejects delays/periods below 1 tick.
 */
internal class FoliaPlatformScheduler(private val plugin: Plugin) : PlatformScheduler {

    private fun wrap(task: Runnable): Consumer<ScheduledTask> = Consumer { task.run() }

    override fun global(task: Runnable): StrataTask =
        StrataTaskImpl(Bukkit.getGlobalRegionScheduler().run(plugin, wrap(task)))

    override fun globalLater(delayTicks: Long, task: Runnable): StrataTask =
        StrataTaskImpl(Bukkit.getGlobalRegionScheduler().runDelayed(plugin, wrap(task), delayTicks.coerceAtLeast(1)))

    override fun globalTimer(delayTicks: Long, periodTicks: Long, task: Runnable): StrataTask =
        StrataTaskImpl(
            Bukkit.getGlobalRegionScheduler()
                .runAtFixedRate(plugin, wrap(task), delayTicks.coerceAtLeast(1), periodTicks.coerceAtLeast(1)),
        )

    override fun region(location: Location, task: Runnable): StrataTask =
        StrataTaskImpl(Bukkit.getRegionScheduler().run(plugin, location, wrap(task)))

    override fun regionLater(location: Location, delayTicks: Long, task: Runnable): StrataTask =
        StrataTaskImpl(Bukkit.getRegionScheduler().runDelayed(plugin, location, wrap(task), delayTicks.coerceAtLeast(1)))

    override fun regionTimer(location: Location, delayTicks: Long, periodTicks: Long, task: Runnable): StrataTask =
        StrataTaskImpl(
            Bukkit.getRegionScheduler().runAtFixedRate(
                plugin, location, wrap(task), delayTicks.coerceAtLeast(1), periodTicks.coerceAtLeast(1),
            ),
        )

    override fun entity(entity: Entity, task: Runnable): StrataTask = entity(entity, task, null)

    override fun entity(entity: Entity, task: Runnable, retired: Runnable?): StrataTask =
        StrataTaskImpl(entity.scheduler.run(plugin, wrap(task), retired))

    override fun entityLater(entity: Entity, delayTicks: Long, task: Runnable, retired: Runnable?): StrataTask =
        StrataTaskImpl(entity.scheduler.runDelayed(plugin, wrap(task), retired, delayTicks.coerceAtLeast(1)))

    override fun async(task: Runnable): StrataTask =
        StrataTaskImpl(Bukkit.getAsyncScheduler().runNow(plugin, wrap(task)))

    override fun asyncLater(delayMillis: Long, task: Runnable): StrataTask =
        StrataTaskImpl(
            Bukkit.getAsyncScheduler().runDelayed(plugin, wrap(task), delayMillis.coerceAtLeast(1), TimeUnit.MILLISECONDS),
        )

    override fun asyncTimer(delayMillis: Long, periodMillis: Long, task: Runnable): StrataTask =
        StrataTaskImpl(
            Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin, wrap(task), delayMillis.coerceAtLeast(1), periodMillis.coerceAtLeast(1), TimeUnit.MILLISECONDS,
            ),
        )
}
