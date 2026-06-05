package so.alaz.strata.api.config

/**
 * Outcome of validating a config section: hard [errors] (malformed/missing) and soft [warnings]
 * (e.g. deprecated keys). [isValid] is true when there are no errors.
 */
public class ValidationResult(
    public val errors: List<String>,
    public val warnings: List<String>,
) {

    public fun isValid(): Boolean = errors.isEmpty()

    public fun hasWarnings(): Boolean = warnings.isNotEmpty()

    /** Throws [ConfigValidationException] if there are any errors. */
    public fun throwIfInvalid() {
        if (!isValid()) throw ConfigValidationException(errors)
    }
}

/** Thrown by [ValidationResult.throwIfInvalid] to fail loud on malformed config. */
public class ConfigValidationException(
    public val errors: List<String>,
) : RuntimeException("Invalid configuration:\n" + errors.joinToString("\n") { " - $it" })
