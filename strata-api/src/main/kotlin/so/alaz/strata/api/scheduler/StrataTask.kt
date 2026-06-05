package so.alaz.strata.api.scheduler

/**
 * A handle to a task scheduled through [PlatformScheduler]. Backed by Paper's region-threaded
 * scheduler so it behaves identically on Paper and Folia.
 */
public interface StrataTask {

    /** Cancels the task. Safe to call more than once. */
    public fun cancel()

    /** `true` once the task has been cancelled. */
    public fun isCancelled(): Boolean

    /** `true` if this is a repeating task. */
    public fun isRepeating(): Boolean
}
