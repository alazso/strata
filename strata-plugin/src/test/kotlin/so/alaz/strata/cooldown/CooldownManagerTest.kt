package so.alaz.strata.cooldown

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import so.alaz.strata.api.cooldown.Cooldowns
import java.time.Duration

class CooldownManagerTest {

    @Test
    fun cooldownCountsDownAndExpires() {
        var now = 1_000L
        val cooldowns = Cooldowns.create { now }

        cooldowns.set("a", Duration.ofMillis(500))
        assertThat(cooldowns.isOnCooldown("a")).isTrue()
        assertThat(cooldowns.remainingMillis("a")).isEqualTo(500)

        now += 200
        assertThat(cooldowns.remainingMillis("a")).isEqualTo(300)

        now += 400 // past expiry
        assertThat(cooldowns.isOnCooldown("a")).isFalse()
        assertThat(cooldowns.remainingMillis("a")).isEqualTo(0)
        assertThat(cooldowns.remaining("a")).isEqualTo(Duration.ZERO)
    }

    @Test
    fun unknownKeyIsNotOnCooldown() {
        val cooldowns = Cooldowns.create { 0L }
        assertThat(cooldowns.isOnCooldown("nope")).isFalse()
        assertThat(cooldowns.remainingMillis("nope")).isEqualTo(0)
    }

    @Test
    fun clearRemovesCooldown() {
        var now = 0L
        val cooldowns = Cooldowns.create { now }
        cooldowns.setMillis("k", 1_000)
        assertThat(cooldowns.isOnCooldown("k")).isTrue()
        cooldowns.clear("k")
        assertThat(cooldowns.isOnCooldown("k")).isFalse()
    }

    @Test
    fun nonPositiveDurationClearsInsteadOfSetting() {
        val cooldowns = Cooldowns.create { 0L }
        cooldowns.setMillis("k", 0)
        assertThat(cooldowns.isOnCooldown("k")).isFalse()
    }
}
