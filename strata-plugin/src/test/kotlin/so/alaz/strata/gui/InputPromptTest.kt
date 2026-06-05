package so.alaz.strata.gui

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import so.alaz.strata.api.gui.AnvilPrompt
import so.alaz.strata.api.gui.AnvilResponse
import so.alaz.strata.api.gui.ChatPrompt
import so.alaz.strata.api.gui.ChatResponse

class InputPromptTest {

    @Test
    fun anvilResponseFactories() {
        assertThat(AnvilResponse.close().kind).isEqualTo(AnvilResponse.Kind.CLOSE)
        assertThat(AnvilResponse.keepOpen().kind).isEqualTo(AnvilResponse.Kind.KEEP_OPEN)
        val replace = AnvilResponse.replaceText("hi")
        assertThat(replace.kind).isEqualTo(AnvilResponse.Kind.REPLACE_TEXT)
        assertThat(replace.text).isEqualTo("hi")
    }

    @Test
    fun anvilPromptBuilderAndWithText() {
        var seen: String? = null
        val prompt = AnvilPrompt.builder()
            .title("<gray>Enter name")
            .text("start")
            .onComplete { _, input -> seen = input; AnvilResponse.close() }
            .build()
        assertThat(prompt.initialText).isEqualTo("start")
        assertThat(prompt.withText("changed").initialText).isEqualTo("changed")
        // handler is wired and reachable
        prompt.onComplete.complete(io.mockk.mockk(relaxed = true), "typed")
        assertThat(seen).isEqualTo("typed")
    }

    @Test
    fun chatPromptDefaultsAndResponses() {
        val prompt = ChatPrompt.builder().cancelToken("quit").build()
        assertThat(prompt.cancelToken).isEqualTo("quit")
        assertThat(ChatResponse.end().kind).isEqualTo(ChatResponse.Kind.END)
        assertThat(ChatResponse.retry().kind).isEqualTo(ChatResponse.Kind.RETRY)
    }
}
