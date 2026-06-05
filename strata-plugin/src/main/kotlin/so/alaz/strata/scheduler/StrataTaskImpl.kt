package so.alaz.strata.scheduler

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import so.alaz.strata.api.scheduler.StrataTask

/**
 * Wraps a Paper [ScheduledTask]. The backing task may be `null` when an entity-bound task could not
 * be scheduled because the entity was already retired — such a task reports as cancelled.
 */
internal class StrataTaskImpl(private val task: ScheduledTask?) : StrataTask {

    override fun cancel() {
        task?.cancel()
    }

    override fun isCancelled(): Boolean {
        val t = task ?: return true
        val state = t.executionState
        return state == ScheduledTask.ExecutionState.CANCELLED ||
            state == ScheduledTask.ExecutionState.CANCELLED_RUNNING
    }

    override fun isRepeating(): Boolean = task?.isRepeatingTask ?: false
}
