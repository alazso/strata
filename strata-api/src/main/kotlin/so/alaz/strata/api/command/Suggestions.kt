package so.alaz.strata.api.command

import org.jetbrains.annotations.ApiStatus
import java.util.function.Supplier

/**
 * Factories for the common [SuggestionProvider] shapes, so callers rarely write one by hand.
 *
 * ```java
 * .suggests(Suggestions.from(registry::voucherIds))   // live, recomputed each keystroke
 * .suggests(Suggestions.integers(1, 16, 32, 64))      // fixed numeric choices
 * ```
 */
@ApiStatus.Experimental
public object Suggestions {

    /** A fixed set of candidates. */
    @JvmStatic
    public fun of(vararg values: String): SuggestionProvider {
        val snapshot = values.toList()
        return SuggestionProvider { snapshot }
    }

    /** A fixed set of candidates. */
    @JvmStatic
    public fun of(values: Collection<String>): SuggestionProvider {
        val snapshot = values.toList()
        return SuggestionProvider { snapshot }
    }

    /** Candidates recomputed on every keystroke from [supplier] (use for live data such as loaded ids). */
    @JvmStatic
    public fun from(supplier: Supplier<out Collection<String>>): SuggestionProvider =
        SuggestionProvider { supplier.get() }

    /** Integer choices rendered as text, e.g. predefined amounts. */
    @JvmStatic
    public fun integers(vararg values: Int): SuggestionProvider {
        val snapshot = values.map(Int::toString)
        return SuggestionProvider { snapshot }
    }
}
