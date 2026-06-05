package so.alaz.strata.api.condition

import org.jetbrains.annotations.ApiStatus

/** A predicate over a [ConditionContext] that passes or fails with a message. */
@ApiStatus.Experimental
public fun interface Condition {
    public fun test(context: ConditionContext): ConditionResult
}
