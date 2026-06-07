package so.alaz.strata

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import so.alaz.strata.api.StrataApi
import so.alaz.strata.api.StrataProvider
import so.alaz.strata.api.command.DebugCommands
import so.alaz.strata.api.command.StrataCommand
import so.alaz.strata.api.condition.ConditionRegistry
import so.alaz.strata.api.gui.GuiManager
import so.alaz.strata.api.hook.EconomyHook
import so.alaz.strata.api.hook.HookRegistry
import so.alaz.strata.api.hook.ItemHook
import so.alaz.strata.api.hook.PermissionHook
import so.alaz.strata.api.hook.RegionHook
import so.alaz.strata.api.message.MessageService
import so.alaz.strata.api.metrics.MetricsService
import so.alaz.strata.api.player.PlayerLookup
import so.alaz.strata.api.scheduler.PlatformScheduler
import so.alaz.strata.api.storage.StorageFactory
import so.alaz.strata.api.text.TextRenderer
import so.alaz.strata.hook.BukkitPermissionHook
import so.alaz.strata.hook.DefaultHookRegistry
import so.alaz.strata.hook.HeadDatabaseItemHook
import so.alaz.strata.hook.ItemsAdderItemHook
import so.alaz.strata.hook.LuckPermsPermissionHook
import so.alaz.strata.hook.NexoItemHook
import so.alaz.strata.hook.OraxenItemHook
import so.alaz.strata.hook.VaultEconomyHook
import so.alaz.strata.hook.WorldGuardRegionHook
import so.alaz.strata.condition.DefaultConditionRegistry
import so.alaz.strata.gui.DefaultGuiManager
import so.alaz.strata.gui.GuiListener
import so.alaz.strata.message.DefaultMessageService
import so.alaz.strata.metrics.DefaultMetricsService
import so.alaz.strata.player.DefaultPlayerLookup
import so.alaz.strata.scheduler.FoliaPlatformScheduler
import so.alaz.strata.storage.DefaultStorageFactory
import so.alaz.strata.text.MiniMessageTextRenderer
import java.util.concurrent.ConcurrentHashMap

/**
 * Strata plugin entry point. Implements [StrataProvider] and registers itself with [StrataApi] so
 * dependent plugins can reach the shared services. Schedulers are cached per owning plugin.
 */
class Strata : JavaPlugin(), StrataProvider {

    private val schedulers = ConcurrentHashMap<Plugin, PlatformScheduler>()
    private val textRenderer: TextRenderer by lazy { MiniMessageTextRenderer() }
    private val storageFactory: StorageFactory by lazy { DefaultStorageFactory() }
    private val hookRegistry: HookRegistry by lazy {
        DefaultHookRegistry().apply {
            register(PermissionHook::class.java, BukkitPermissionHook(), 0)
            register(PermissionHook::class.java, LuckPermsPermissionHook(), 100)
            register(EconomyHook::class.java, VaultEconomyHook(), 0)
            // Custom-item providers. None shadows another: items are provider-specific, so consumers
            // resolve across all available providers via hooks().all(ItemHook::class.java).
            register(ItemHook::class.java, ItemsAdderItemHook(), 0)
            register(ItemHook::class.java, OraxenItemHook(), 0)
            register(ItemHook::class.java, NexoItemHook(), 0)
            register(ItemHook::class.java, HeadDatabaseItemHook(), 0)
            register(RegionHook::class.java, WorldGuardRegionHook(), 0)
        }
    }
    private val metricsService: MetricsService by lazy { DefaultMetricsService() }
    private val conditionRegistry: ConditionRegistry by lazy {
        DefaultConditionRegistry(hookRegistry, textRenderer)
    }
    private val guiManager: DefaultGuiManager by lazy { DefaultGuiManager(this) }
    private val playerLookup: DefaultPlayerLookup by lazy { DefaultPlayerLookup() }
    private val messageServices = ConcurrentHashMap<Plugin, MessageService>()

    override fun onEnable() {
        instance = this
        saveDefaultConfig()
        StrataApi.register(this)
        applyHookPreferences()
        server.pluginManager.registerEvents(GuiListener(guiManager), this)
        server.pluginManager.registerEvents(playerLookup, this)
        registerStrataCommand()
        logger.info("Strata enabled (API ${StrataApi.VERSION}).")
    }

    /** Strata's own `/strata` admin command (version + debug introspection). */
    private fun registerStrataCommand() {
        StrataCommand.literal("strata")
            .permission("strata.admin")
            .executes { it.reply("<aqua>Strata <gray>v<white>${StrataApi.VERSION}") }
            .then(
                StrataCommand.literal("debug")
                    .permission("strata.admin")
                    .then(DebugCommands.scheduler("strata.admin"))
                    .then(DebugCommands.integrations("strata.admin", hookRegistry))
                    .then(DebugCommands.dump("strata.admin", this)),
            )
            .register(this, "Strata library admin command", listOf("stratalib"))
    }

    /** Applies admin-configured provider preferences (e.g. which economy backend is authoritative). */
    private fun applyHookPreferences() {
        config.getString("economy.provider")?.takeIf { it.isNotBlank() }?.let { preferred ->
            hookRegistry.setPreference(EconomyHook::class.java, preferred)
            logger.info("Economy provider preference set to '$preferred'.")
        }
    }

    override fun onDisable() {
        guiManager.closeAll()
        StrataApi.unregister()
        schedulers.clear()
        logger.info("Strata disabled.")
    }

    override fun scheduler(plugin: Plugin): PlatformScheduler =
        schedulers.computeIfAbsent(plugin) { FoliaPlatformScheduler(it) }

    override fun text(): TextRenderer = textRenderer

    override fun storage(): StorageFactory = storageFactory

    override fun hooks(): HookRegistry = hookRegistry

    override fun metrics(): MetricsService = metricsService

    override fun conditions(): ConditionRegistry = conditionRegistry

    override fun gui(): GuiManager = guiManager

    override fun players(): PlayerLookup = playerLookup

    override fun messages(plugin: Plugin): MessageService =
        messageServices.computeIfAbsent(plugin) { DefaultMessageService(it, textRenderer) }

    companion object {
        @JvmStatic
        lateinit var instance: Strata
            private set
    }
}
