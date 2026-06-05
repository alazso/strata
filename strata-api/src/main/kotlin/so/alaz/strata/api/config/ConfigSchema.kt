package so.alaz.strata.api.config

import org.bukkit.configuration.ConfigurationSection

/** The kinds of values a schema rule can require at a config path. */
public enum class ValueType {
    STRING, INT, LONG, DOUBLE, BOOLEAN, LIST, SECTION
}

/**
 * A declarative schema for validating a Bukkit [ConfigurationSection]. Build one with [builder],
 * then call [validate] to get a [ValidationResult] with rich, path-specific messages.
 *
 * ```java
 * ConfigSchema schema = ConfigSchema.builder()
 *     .require("database.type", ValueType.STRING)
 *     .optional("database.pool-size", ValueType.INT)
 *     .intRange("database.pool-size", 1, 100)
 *     .deprecated("mysql-host", "database.host")
 *     .build();
 * schema.validate(config).throwIfInvalid();
 * ```
 */
public class ConfigSchema private constructor(
    private val rules: List<KeyRule>,
    private val intRanges: List<IntRange2>,
    private val doubleRanges: List<DoubleRange2>,
    private val deprecations: List<Deprecation>,
) {

    private class KeyRule(val path: String, val type: ValueType, val required: Boolean)
    private class IntRange2(val path: String, val min: Long, val max: Long)
    private class DoubleRange2(val path: String, val min: Double, val max: Double)
    private class Deprecation(val path: String, val useInstead: String?)

    /** Validates [section] against this schema. Never throws; collects all findings. */
    public fun validate(section: ConfigurationSection): ValidationResult {
        val errors = ArrayList<String>()
        val warnings = ArrayList<String>()

        for (rule in rules) {
            val present = section.isSet(rule.path)
            if (!present) {
                if (rule.required) errors.add("Missing required key '${rule.path}' (expected ${rule.type.name.lowercase()})")
                continue
            }
            if (!matches(section, rule.path, rule.type)) {
                errors.add("Key '${rule.path}' must be a ${rule.type.name.lowercase()}")
            }
        }

        for (r in intRanges) {
            if (!section.isSet(r.path)) continue
            if (!section.isInt(r.path) && !section.isLong(r.path)) continue // type error already reported
            val v = section.getLong(r.path)
            if (v < r.min || v > r.max) {
                errors.add("Key '${r.path}' must be between ${r.min} and ${r.max} (was $v)")
            }
        }

        for (r in doubleRanges) {
            if (!section.isSet(r.path)) continue
            if (!isNumeric(section, r.path)) continue
            val v = section.getDouble(r.path)
            if (v < r.min || v > r.max) {
                errors.add("Key '${r.path}' must be between ${r.min} and ${r.max} (was $v)")
            }
        }

        for (d in deprecations) {
            if (section.isSet(d.path)) {
                warnings.add(
                    if (d.useInstead != null) "Key '${d.path}' is deprecated; use '${d.useInstead}' instead"
                    else "Key '${d.path}' is deprecated and will be removed",
                )
            }
        }

        return ValidationResult(errors, warnings)
    }

    private fun matches(section: ConfigurationSection, path: String, type: ValueType): Boolean = when (type) {
        ValueType.STRING -> section.isString(path)
        ValueType.INT -> section.isInt(path)
        ValueType.LONG -> section.isLong(path) || section.isInt(path)
        ValueType.DOUBLE -> isNumeric(section, path)
        ValueType.BOOLEAN -> section.isBoolean(path)
        ValueType.LIST -> section.isList(path)
        ValueType.SECTION -> section.isConfigurationSection(path)
    }

    private fun isNumeric(section: ConfigurationSection, path: String): Boolean =
        section.isDouble(path) || section.isInt(path) || section.isLong(path)

    /** Fluent builder. */
    public class Builder {
        private val rules = ArrayList<KeyRule>()
        private val intRanges = ArrayList<IntRange2>()
        private val doubleRanges = ArrayList<DoubleRange2>()
        private val deprecations = ArrayList<Deprecation>()

        /** Requires [path] to be present and of [type]. */
        public fun require(path: String, type: ValueType): Builder {
            rules.add(KeyRule(path, type, required = true))
            return this
        }

        /** Allows [path]; if present it must be of [type]. */
        public fun optional(path: String, type: ValueType): Builder {
            rules.add(KeyRule(path, type, required = false))
            return this
        }

        /** Constrains an integer/long [path] to [[min], [max]] when present. */
        public fun intRange(path: String, min: Long, max: Long): Builder {
            intRanges.add(IntRange2(path, min, max))
            return this
        }

        /** Constrains a numeric [path] to [[min], [max]] when present. */
        public fun doubleRange(path: String, min: Double, max: Double): Builder {
            doubleRanges.add(DoubleRange2(path, min, max))
            return this
        }

        /** Flags [path] as deprecated; [useInstead] names the replacement key (may be null). */
        @JvmOverloads
        public fun deprecated(path: String, useInstead: String? = null): Builder {
            deprecations.add(Deprecation(path, useInstead))
            return this
        }

        public fun build(): ConfigSchema = ConfigSchema(rules, intRanges, doubleRanges, deprecations)
    }

    public companion object {
        @JvmStatic
        public fun builder(): Builder = Builder()
    }
}
