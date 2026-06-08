package so.alaz.strata.command

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import so.alaz.strata.api.command.ArgType
import so.alaz.strata.api.command.CommonCommands
import so.alaz.strata.api.command.DebugCommands
import so.alaz.strata.api.command.StrataCommand
import so.alaz.strata.api.command.Suggestions
import so.alaz.strata.api.hook.HookRegistry
import so.alaz.strata.api.storage.StorageProvider
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin

class CommandPatternsTest {

    @Test
    fun reloadSubcommandBuilds() {
        val node = CommonCommands.reload("perm", {}).toBrigadier()
        assertThat(node.literal).isEqualTo("reload")
    }

    @Test
    fun debugDatabaseSubcommandBuilds() {
        // Building the tree doesn't touch the StorageProvider (only on execute), so a mock is fine.
        val node = DebugCommands.database("perm", mockk<StorageProvider>()).toBrigadier()
        assertThat(node.literal).isEqualTo("database")
    }

    @Test
    fun debugIntegrationsAndSchedulerBuild() {
        assertThat(DebugCommands.integrations("perm", mockk<HookRegistry>()).toBrigadier().literal).isEqualTo("integrations")
        assertThat(DebugCommands.scheduler("perm").toBrigadier().literal).isEqualTo("scheduler")
    }

    @Test
    fun debugDumpHasAnonymizeChild() {
        val node = DebugCommands.dump("perm", mockk<Plugin>()).toBrigadier()
        assertThat(node.literal).isEqualTo("dump")
        assertThat(node.children.map { it.name }).containsExactly("anonymize")
    }

    @Test
    fun argumentWithSuggestionsBuilds() {
        val node = StrataCommand.literal("give")
            .then(StrataCommand.argument("id", ArgType.word()).suggests(Suggestions.of("alpha", "beta")))
            .toBrigadier()
        assertThat(node.literal).isEqualTo("give")
        assertThat(node.children.map { it.name }).contains("id")
    }

    @Test
    fun helpEntriesIncludeOnlyPermittedLiterals() {
        val sender = mockk<CommandSender>()
        every { sender.hasPermission("strata.admin") } returns false

        val command = StrataCommand.literal("root")
            .then(StrataCommand.literal("open").description("Open the menu"))
            .then(StrataCommand.literal("admin").permission("strata.admin").description("Admin tools"))
            .then(StrataCommand.argument("x", ArgType.word())) // argument children are not help entries

        val entries = command.helpEntries(sender)
        assertThat(entries.map { it.name }).containsExactly("open") // admin filtered out, argument excluded
        assertThat(entries.single().description).isEqualTo("Open the menu")
    }
}
