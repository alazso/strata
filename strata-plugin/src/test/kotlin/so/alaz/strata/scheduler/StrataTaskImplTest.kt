package so.alaz.strata.scheduler

import io.mockk.mockk
import io.mockk.every
import io.mockk.verify
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StrataTaskImplTest {

    @Test
    fun reportsCancelledFromExecutionState() {
        val task = mockk<ScheduledTask>(relaxed = true)
        every { task.executionState } returns ScheduledTask.ExecutionState.CANCELLED
        assertThat(StrataTaskImpl(task).isCancelled()).isTrue()
    }

    @Test
    fun nullBackingTaskReportsCancelled() {
        assertThat(StrataTaskImpl(null).isCancelled()).isTrue()
    }

    @Test
    fun cancelDelegatesToBackingTask() {
        val task = mockk<ScheduledTask>(relaxed = true)
        StrataTaskImpl(task).cancel()
        verify { task.cancel() }
    }
}
