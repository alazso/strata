package so.alaz.strata.api.condition

import net.kyori.adventure.text.Component
import org.jetbrains.annotations.ApiStatus

/** The outcome of evaluating a [Condition]: passed, or failed with a [message] to show the player. */
@ApiStatus.Experimental
public class ConditionResult private constructor(
    public val passed: Boolean,
    public val message: Component?,
) {
    public companion object {
        private val PASS: ConditionResult = ConditionResult(true, null)

        /** A passing result. */
        @JvmStatic
        public fun pass(): ConditionResult = PASS

        /** A failing result carrying the [message] to show the player. */
        @JvmStatic
        public fun fail(message: Component): ConditionResult = ConditionResult(false, message)
    }
}
