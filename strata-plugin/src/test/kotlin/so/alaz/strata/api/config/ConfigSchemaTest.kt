package so.alaz.strata.api.config

import org.assertj.core.api.Assertions.assertThat
import org.bukkit.configuration.MemoryConfiguration
import org.junit.jupiter.api.Test

class ConfigSchemaTest {

    private fun schema(): ConfigSchema = ConfigSchema.builder()
        .require("database.type", ValueType.STRING)
        .optional("database.pool-size", ValueType.INT)
        .intRange("database.pool-size", 1, 100)
        .deprecated("mysql-host", "database.host")
        .build()

    @Test
    fun validConfigPasses() {
        val cfg = MemoryConfiguration().apply {
            set("database.type", "sqlite")
            set("database.pool-size", 10)
        }
        val result = schema().validate(cfg)
        assertThat(result.isValid()).isTrue()
        assertThat(result.warnings).isEmpty()
    }

    @Test
    fun missingRequiredKeyIsAnError() {
        val cfg = MemoryConfiguration().apply { set("database.pool-size", 10) }
        val result = schema().validate(cfg)
        assertThat(result.isValid()).isFalse()
        assertThat(result.errors).anyMatch { it.contains("database.type") }
    }

    @Test
    fun wrongTypeIsAnError() {
        val cfg = MemoryConfiguration().apply { set("database.type", 123) }
        val result = schema().validate(cfg)
        assertThat(result.errors).anyMatch { it.contains("must be a string") }
    }

    @Test
    fun outOfRangeIsAnError() {
        val cfg = MemoryConfiguration().apply {
            set("database.type", "sqlite")
            set("database.pool-size", 999)
        }
        val result = schema().validate(cfg)
        assertThat(result.errors).anyMatch { it.contains("between 1 and 100") }
    }

    @Test
    fun deprecatedKeyIsAWarningNotAnError() {
        val cfg = MemoryConfiguration().apply {
            set("database.type", "sqlite")
            set("mysql-host", "localhost")
        }
        val result = schema().validate(cfg)
        assertThat(result.isValid()).isTrue()
        assertThat(result.warnings).anyMatch { it.contains("deprecated") && it.contains("database.host") }
    }
}
