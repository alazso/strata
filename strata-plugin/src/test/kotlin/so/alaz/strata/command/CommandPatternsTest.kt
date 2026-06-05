package so.alaz.strata.command

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import so.alaz.strata.api.command.CommonCommands
import so.alaz.strata.api.command.DebugCommands
import so.alaz.strata.api.hook.HookRegistry
import so.alaz.strata.api.storage.StorageProvider
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
}
