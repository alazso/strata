package so.alaz.strata.condition

import so.alaz.strata.api.condition.ConditionContext
import so.alaz.strata.api.condition.ConditionResult
import so.alaz.strata.api.hook.EconomyHook
import so.alaz.strata.api.hook.HookRegistry
import so.alaz.strata.api.hook.PermissionHook
import so.alaz.strata.api.hook.RegionHook
import so.alaz.strata.api.text.TextRenderer

/** Passes if the player can afford [amount]. Fails gracefully if no economy hook is available. */
internal class EconomyCondition(
    private val amount: Double,
    deny: String?,
    text: TextRenderer,
    private val hooks: HookRegistry,
) : BaseCondition(deny, text) {
    override fun test(context: ConditionContext): ConditionResult {
        val economy = hooks.get(EconomyHook::class.java)
            ?: return denied(context, "<red>Economy is unavailable.")
        return if (economy.has(context.player, amount)) pass() else denied(context, "<red>You can't afford this.")
    }
}

/** Passes if the player is in one of the required groups. Needs a group-aware [PermissionHook]. */
internal class RankCondition(
    private val groups: Set<String>,
    deny: String?,
    text: TextRenderer,
    private val hooks: HookRegistry,
) : BaseCondition(deny, text) {
    override fun test(context: ConditionContext): ConditionResult {
        val perms = hooks.get(PermissionHook::class.java)
        val playerGroups = buildSet {
            perms?.primaryGroup(context.player)?.let { add(it.lowercase()) }
            perms?.groups(context.player)?.forEach { add(it.lowercase()) }
        }
        return if (playerGroups.any { it in groups }) pass() else denied(context, "<red>You lack the required rank.")
    }
}

/** Passes if the player's location is in one of the required regions. Needs a [RegionHook]. */
internal class RegionCondition(
    private val regions: Set<String>,
    deny: String?,
    text: TextRenderer,
    private val hooks: HookRegistry,
) : BaseCondition(deny, text) {
    override fun test(context: ConditionContext): ConditionResult {
        val regionHook = hooks.get(RegionHook::class.java)
            ?: return denied(context, "<red>Region support is unavailable.")
        val here = regionHook.regionsAt(context.location)
        return if (here.any { it in regions }) pass() else denied(context, "<red>You're not in the required region.")
    }
}
