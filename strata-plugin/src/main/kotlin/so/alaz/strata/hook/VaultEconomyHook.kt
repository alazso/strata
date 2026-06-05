package so.alaz.strata.hook

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import so.alaz.strata.api.hook.EconomyHook

/**
 * Vault-backed [EconomyHook]. Resolves the economy provider Vault registered in Bukkit's
 * [org.bukkit.plugin.ServicesManager]. As with the other hooks, no Vault-typed fields exist (only a
 * `present` flag); all Vault references are in method bodies and wrapped so a missing Vault, or Vault
 * present with no economy plugin behind it, degrades to unavailable / no-op rather than throwing.
 *
 * Registered by Strata at priority 0. Your own economy plugin (Conduit) should self-register its own
 * [EconomyHook] at a higher priority, and admins pick the authoritative one via the
 * `economy.provider` config (registry preference).
 */
internal class VaultEconomyHook : EconomyHook {

    private val present: Boolean =
        runCatching { Class.forName("net.milkbowl.vault.economy.Economy", false, javaClass.classLoader) }.isSuccess

    override fun name(): String = "Vault"

    override fun isAvailable(): Boolean = present && economy() != null

    override fun balance(player: OfflinePlayer): Double = economy()?.getBalance(player) ?: 0.0

    override fun has(player: OfflinePlayer, amount: Double): Boolean = economy()?.has(player, amount) ?: false

    override fun withdraw(player: OfflinePlayer, amount: Double): Boolean =
        economy()?.withdrawPlayer(player, amount)?.transactionSuccess() ?: false

    override fun deposit(player: OfflinePlayer, amount: Double): Boolean =
        economy()?.depositPlayer(player, amount)?.transactionSuccess() ?: false

    override fun format(amount: Double): String = economy()?.format(amount) ?: amount.toString()

    private fun economy(): Economy? = runCatching {
        Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider
    }.getOrNull()
}
