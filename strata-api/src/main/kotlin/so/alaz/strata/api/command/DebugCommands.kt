package so.alaz.strata.api.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.jetbrains.annotations.ApiStatus
import so.alaz.strata.api.hook.EconomyHook
import so.alaz.strata.api.hook.HologramHook
import so.alaz.strata.api.hook.Hook
import so.alaz.strata.api.hook.HookRegistry
import so.alaz.strata.api.hook.ItemHook
import so.alaz.strata.api.hook.PermissionHook
import so.alaz.strata.api.hook.RegionHook
import so.alaz.strata.api.storage.StorageProvider

/**
 * Ready-made `debug` subcommands that introspect Strata subsystems. Attach the ones relevant to your
 * plugin under a `debug` literal:
 *
 * ```java
 * StrataCommand.literal("myplugin").then(StrataCommand.literal("debug")
 *     .then(DebugCommands.database("myplugin.admin", myStorage))
 *     .then(DebugCommands.integrations("myplugin.admin", StrataApi.hooks()))
 *     .then(DebugCommands.scheduler("myplugin.admin"))
 *     .then(DebugCommands.dump("myplugin.admin", this)));
 * ```
 */
@ApiStatus.Experimental
public object DebugCommands {

    private val HOOK_TYPES: List<Pair<String, Class<out Hook>>> = listOf(
        "Permission" to PermissionHook::class.java,
        "Economy" to EconomyHook::class.java,
        "Region" to RegionHook::class.java,
        "Item" to ItemHook::class.java,
        "Hologram" to HologramHook::class.java,
    )

    /** `database` — backend + applied schema version of [storage]. */
    @JvmStatic
    public fun database(permission: String, storage: StorageProvider): StrataCommand =
        StrataCommand.literal("database").permission(permission).executes { ctx ->
            ctx.reply(line("Backend", storage.backend().name))
            storage.migrations().currentVersion().whenComplete { version, error ->
                if (error != null) ctx.reply(line("Schema version", "unavailable (${error.message})"))
                else ctx.reply(line("Schema version", version.toString()))
            }
        }

    /** `scheduler` — platform (Folia/Paper) and thread info. */
    @JvmStatic
    public fun scheduler(permission: String): StrataCommand =
        StrataCommand.literal("scheduler").permission(permission).executes { ctx ->
            ctx.reply(line("Platform", if (isFolia()) "Folia (regionised)" else "Paper"))
            ctx.reply(line("Primary thread", Bukkit.isPrimaryThread().toString()))
            ctx.reply(line("Online players", Bukkit.getOnlinePlayers().size.toString()))
        }

    /** `integrations` — which hook each capability resolves to (or none). */
    @JvmStatic
    public fun integrations(permission: String, hooks: HookRegistry): StrataCommand =
        StrataCommand.literal("integrations").permission(permission).executes { ctx ->
            for ((label, type) in HOOK_TYPES) {
                val provider = hooks.get(type)
                ctx.reply(line(label, provider?.name() ?: "none"))
            }
        }

    /** `dump` (and `dump anonymize`) — a diagnostic snapshot, optionally scrubbed of IPs/paths. */
    @JvmStatic
    public fun dump(permission: String, plugin: Plugin): StrataCommand =
        StrataCommand.literal("dump").permission(permission)
            .executes { ctx -> dump(ctx, plugin, anonymize = false) }
            .then(
                StrataCommand.literal("anonymize")
                    .executes { ctx -> dump(ctx, plugin, anonymize = true) },
            )

    private fun dump(context: CommandContext, plugin: Plugin, anonymize: Boolean) {
        val lines = listOf(
            "Plugin: ${plugin.name} ${plugin.pluginMeta.version}",
            "Server: ${Bukkit.getName()} ${Bukkit.getVersion()}",
            "Bukkit: ${Bukkit.getBukkitVersion()}",
            "Java: ${System.getProperty("java.version")} (${System.getProperty("os.name")})",
            "Players: ${Bukkit.getOnlinePlayers().size}/${Bukkit.getMaxPlayers()}",
            "Plugins: ${Bukkit.getPluginManager().plugins.joinToString { it.name }}",
        )
        context.reply(Component.text("---- ${plugin.name} dump${if (anonymize) " (anonymized)" else ""} ----", NamedTextColor.AQUA))
        lines.forEach { context.reply(Component.text(if (anonymize) anonymize(it) else it, NamedTextColor.GRAY)) }
    }

    private fun anonymize(value: String): String = value
        .replace(Regex("\\d{1,3}(\\.\\d{1,3}){3}"), "[ip]")
        .replace(Regex("[A-Za-z]:\\\\[^\\s]+"), "[path]")
        .replace(Regex("(?<![\\w.])(/[\\w./-]+){2,}"), "[path]")

    private fun line(label: String, value: String): Component =
        Component.text("$label: ", NamedTextColor.GRAY).append(Component.text(value, NamedTextColor.WHITE))

    private fun isFolia(): Boolean =
        runCatching { Class.forName("io.papermc.paper.threadedregions.RegionizedServer") }.isSuccess
}
