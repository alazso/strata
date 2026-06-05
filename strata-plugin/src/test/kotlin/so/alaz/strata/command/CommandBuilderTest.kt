package so.alaz.strata.command

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import so.alaz.strata.api.command.ArgType
import so.alaz.strata.api.command.StrataCommand

class CommandBuilderTest {

    @Test
    fun buildsBrigadierTreeWithSubcommandsAndArguments() {
        val command = StrataCommand.literal("strata")
            .permission("strata.admin")
            .then(StrataCommand.literal("reload").executes { })
            .then(
                StrataCommand.literal("give")
                    .then(
                        // Pure-Brigadier types in unit tests; ArgType.player() needs a bootstrapped server.
                        StrataCommand.argument("target", ArgType.word())
                            .then(StrataCommand.argument("amount", ArgType.integer(1, 64)).executes { }),
                    ),
            )

        val node = command.toBrigadier()
        assertThat(node.literal).isEqualTo("strata")
        assertThat(node.children.map { it.name }).containsExactlyInAnyOrder("reload", "give")

        val give = node.children.first { it.name == "give" }
        assertThat(give.children.map { it.name }).containsExactly("target")
    }

    @Test
    fun rootMustBeLiteral() {
        org.assertj.core.api.Assertions.assertThatThrownBy {
            StrataCommand.argument("x", ArgType.word()).toBrigadier()
        }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun argTypeFactoriesBuild() {
        // Pure-Brigadier types are headless-safe; ArgType.player() (Paper ArgumentTypes) needs a server.
        assertThat(ArgType.integer(1, 5)).isNotNull()
        assertThat(ArgType.greedyString()).isNotNull()
        assertThat(ArgType.string()).isNotNull()
        assertThat(ArgType.bool()).isNotNull()
        assertThat(ArgType.decimal()).isNotNull()
    }
}
