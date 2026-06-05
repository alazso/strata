package so.alaz.strata.api.hook

import org.bukkit.OfflinePlayer

/** Vault-style economy operations behind one interface. */
public interface EconomyHook : Hook {

    /** The player's balance. */
    public fun balance(player: OfflinePlayer): Double

    /** `true` if the player can afford [amount]. */
    public fun has(player: OfflinePlayer, amount: Double): Boolean

    /** Withdraws [amount]; returns `true` on success. */
    public fun withdraw(player: OfflinePlayer, amount: Double): Boolean

    /** Deposits [amount]; returns `true` on success. */
    public fun deposit(player: OfflinePlayer, amount: Double): Boolean

    /** Formats [amount] using the economy provider's conventions. */
    public fun format(amount: Double): String
}
