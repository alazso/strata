package so.alaz.strata.api.condition

import org.jetbrains.annotations.ApiStatus

/** Helpers for evaluating groups of conditions. */
@ApiStatus.Experimental
public object Conditions {

    /**
     * Tests [conditions] with AND semantics against [context], returning the first failure (so its
     * message can be shown), or [ConditionResult.pass] if all pass (or the list is empty).
     */
    @JvmStatic
    public fun testAll(conditions: List<Condition>, context: ConditionContext): ConditionResult {
        for (condition in conditions) {
            val result = condition.test(context)
            if (!result.passed) return result
        }
        return ConditionResult.pass()
    }
}
