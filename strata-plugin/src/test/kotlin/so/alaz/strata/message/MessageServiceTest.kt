package so.alaz.strata.message

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import so.alaz.strata.text.MiniMessageTextRenderer
import java.io.File
import java.nio.file.Path
import java.util.Locale
import java.util.logging.Logger

/**
 * The catalog and lang-file behaviour (defaults, file override, write-back, locale fallback, missing
 * keys) is headless-testable. The player-locale rendering and sending paths need a server and are
 * compile-verified.
 */
class MessageServiceTest {

    private fun service(dir: Path): DefaultMessageService {
        val plugin = mockk<Plugin>()
        every { plugin.dataFolder } returns dir.toFile()
        every { plugin.logger } returns Logger.getLogger("MessageServiceTest")
        return DefaultMessageService(plugin, MiniMessageTextRenderer())
    }

    @Test
    fun loadGeneratesLangFileFromDefaults(@TempDir dir: Path) {
        val messages = service(dir).defaults(mapOf("welcome" to "<green>Hi", "bye" to "<red>Bye")).load()

        val file = File(dir.toFile(), "lang/en.yml")
        assertThat(file).exists()
        assertThat(YamlConfiguration.loadConfiguration(file).getString("welcome")).isEqualTo("<green>Hi")
        assertThat(messages.raw(Locale.ENGLISH, "welcome")).isEqualTo("<green>Hi")
    }

    @Test
    fun fileOverridesCodeDefault(@TempDir dir: Path) {
        File(dir.toFile(), "lang").mkdirs()
        File(dir.toFile(), "lang/en.yml").writeText("welcome: \"<gold>Custom\"\n")

        val messages = service(dir).defaults(mapOf("welcome" to "<green>Hi")).load()

        assertThat(messages.raw(Locale.ENGLISH, "welcome")).isEqualTo("<gold>Custom")
    }

    @Test
    fun loadAppendsNewlyIntroducedDefaultKeys(@TempDir dir: Path) {
        File(dir.toFile(), "lang").mkdirs()
        File(dir.toFile(), "lang/en.yml").writeText("welcome: \"<green>Hi\"\n")

        service(dir).defaults(mapOf("welcome" to "<green>Hi", "newkey" to "<aqua>New")).load()

        val yaml = YamlConfiguration.loadConfiguration(File(dir.toFile(), "lang/en.yml"))
        assertThat(yaml.getString("newkey")).isEqualTo("<aqua>New") // appended on update
        assertThat(yaml.getString("welcome")).isEqualTo("<green>Hi") // existing preserved
    }

    @Test
    fun localeFallsBackToLanguageThenDefault(@TempDir dir: Path) {
        val messages = service(dir)
            .defaultLocale(Locale.ENGLISH)
            .defaults(mapOf("k" to "english"))
            .defaults(Locale.GERMAN, mapOf("k" to "deutsch"))
            .load()

        assertThat(messages.raw(Locale.GERMANY, "k")).isEqualTo("deutsch") // de_DE -> de
        assertThat(messages.raw(Locale.FRENCH, "k")).isEqualTo("english") // fr -> fallback en
    }

    @Test
    fun missingKeyReturnsMarker(@TempDir dir: Path) {
        val messages = service(dir).defaults(mapOf("a" to "b")).load()
        assertThat(messages.raw(Locale.ENGLISH, "nope")).isEqualTo("missing message: nope")
    }
}
