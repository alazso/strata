package so.alaz.strata.text

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MiniMessageTextRendererTest {

    private val renderer = MiniMessageTextRenderer()
    private val mm = MiniMessage.miniMessage()

    @Test
    fun parsesMiniMessageColor() {
        val out = mm.serialize(renderer.render("<red>Hello"))
        assertThat(out).contains("Hello")
        assertThat(out).contains("red")
    }

    @Test
    fun nullViewerSkipsPlaceholdersButStillParses() {
        val component = renderer.render("<green>Hi", null)
        assertThat(mm.serialize(component)).contains("Hi")
    }

    @Test
    fun customTagResolverApplies() {
        val component = renderer.render("<greeting>", null, Placeholder.parsed("greeting", "Howdy"))
        assertThat(mm.serialize(component)).contains("Howdy")
    }

    @Test
    fun rendersEachLine() {
        val lines = renderer.render(listOf("<red>a", "<blue>b"), null)
        assertThat(lines).hasSize(2)
    }
}
