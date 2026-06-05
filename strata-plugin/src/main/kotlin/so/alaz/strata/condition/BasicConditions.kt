package so.alaz.strata.condition

import so.alaz.strata.api.condition.Condition
import so.alaz.strata.api.condition.ConditionContext
import so.alaz.strata.api.condition.ConditionResult
import so.alaz.strata.api.hook.HookRegistry
import so.alaz.strata.api.hook.PermissionHook
import so.alaz.strata.api.text.TextRenderer
import java.util.UUID

/**
 * Shared base: renders the configured `deny` message (or a per-condition default) via [TextRenderer]
 * against the failing player, so failure messages support MiniMessage + placeholders.
 */
internal abstract class BaseCondition(
    private val denyMessage: String?,
    private val text: TextRenderer,
) : Condition {

    protected fun pass(): ConditionResult = ConditionResult.pass()

    protected fun denied(context: ConditionContext, default: String): ConditionResult =
        ConditionResult.fail(text.render(denyMessage ?: default, context.player))
}

/** Passes if the player has the permission node. Uses [PermissionHook] when present, else Bukkit. */
internal class PermissionCondition(
    private val node: String,
    deny: String?,
    private val text: TextRenderer,
    private val hooks: HookRegistry,
) : BaseCondition(deny, text) {
    override fun test(context: ConditionContext): ConditionResult {
        val perms = hooks.get(PermissionHook::class.java)
        val has = perms?.has(context.player, node) ?: context.player.hasPermission(node)
        return if (has) pass() else denied(context, "<red>You don't have permission.")
    }
}

/** Passes if the player is in one of the named worlds. */
internal class WorldCondition(
    private val worlds: Set<String>,
    deny: String?,
    text: TextRenderer,
) : BaseCondition(deny, text) {
    override fun test(context: ConditionContext): ConditionResult =
        if (context.player.world.name in worlds) pass() else denied(context, "<red>You're in the wrong world.")
}

/** Passes if the player's game mode is one of the named modes. */
internal class GamemodeCondition(
    private val modes: Set<String>,
    deny: String?,
    text: TextRenderer,
) : BaseCondition(deny, text) {
    override fun test(context: ConditionContext): ConditionResult =
        if (context.player.gameMode.name in modes) pass() else denied(context, "<red>Wrong game mode.")
}

/** Passes if the player's level is at least [minLevel]. */
internal class ExpCondition(
    private val minLevel: Int,
    deny: String?,
    text: TextRenderer,
) : BaseCondition(deny, text) {
    override fun test(context: ConditionContext): ConditionResult =
        if (context.player.level >= minLevel) pass() else denied(context, "<red>You need level $minLevel.")
}

/** Passes only until [expiresAtMillis] (epoch millis). */
internal class ExpiryCondition(
    private val expiresAtMillis: Long,
    deny: String?,
    text: TextRenderer,
) : BaseCondition(deny, text) {
    override fun test(context: ConditionContext): ConditionResult =
        if (System.currentTimeMillis() <= expiresAtMillis) pass() else denied(context, "<red>This has expired.")
}

/** Passes if the player is the `owner` extra supplied in the context. */
internal class OwnerCondition(
    deny: String?,
    text: TextRenderer,
) : BaseCondition(deny, text) {
    override fun test(context: ConditionContext): ConditionResult {
        val owner = context.getExtra("owner") as? UUID
        return if (owner != null && owner == context.player.uniqueId) pass()
        else denied(context, "<red>You are not the owner.")
    }
}
