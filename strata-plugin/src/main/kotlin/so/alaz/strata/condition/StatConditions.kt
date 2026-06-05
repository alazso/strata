package so.alaz.strata.condition

import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import so.alaz.strata.api.condition.ConditionContext
import so.alaz.strata.api.condition.ConditionResult
import so.alaz.strata.api.text.TextRenderer

/** Numeric comparison operators shared by the `papi` and `playerstat` conditions. */
internal object Comparisons {
    fun numeric(left: Double, operator: String, right: Double): Boolean = when (operator.trim().lowercase()) {
        ">", "gt" -> left > right
        ">=", "gte", "at-least", "atleast" -> left >= right
        "<", "lt" -> left < right
        "<=", "lte", "at-most", "atmost" -> left <= right
        "!=", "ne", "not" -> left != right
        else -> left == right // =, ==, equals
    }
}

/**
 * Compares a resolved PlaceholderAPI placeholder against a configured value. Supports string
 * `equals`/`contains`/`!=` and numeric `>`/`>=`/`<`/`<=`. Degrades safely: with PAPI absent the
 * placeholder resolves to its raw text, which simply won't match.
 */
internal class PapiCondition(
    private val placeholder: String,
    private val operator: String,
    private val value: String,
    deny: String?,
    private val text: TextRenderer,
) : BaseCondition(deny, text) {
    override fun test(context: ConditionContext): ConditionResult {
        val resolved = text.resolve(placeholder, context.player)
        val matched = when (operator.trim().lowercase()) {
            "contains" -> resolved.contains(value, ignoreCase = true)
            "!=", "ne", "not" -> !resolved.equals(value, ignoreCase = true)
            ">", ">=", "<", "<=", "gt", "gte", "lt", "lte" -> {
                val left = resolved.toDoubleOrNull()
                val right = value.toDoubleOrNull()
                left != null && right != null && Comparisons.numeric(left, operator, right)
            }
            else -> resolved.equals(value, ignoreCase = true) // =, ==, equals
        }
        return if (matched) pass() else denied(context, "<red>Requirement not met.")
    }
}

/**
 * Compares a player statistic against a threshold. Handles untyped statistics as well as
 * block/item (needs `material`) and entity (needs `entity`) statistics. Invalid names or a missing
 * sub-type deny gracefully.
 */
internal class PlayerstatCondition(
    private val statName: String,
    private val materialName: String?,
    private val entityName: String?,
    private val operator: String,
    private val threshold: Long,
    deny: String?,
    text: TextRenderer,
) : BaseCondition(deny, text) {
    override fun test(context: ConditionContext): ConditionResult {
        val value = runCatching { readStatistic(context.player) }.getOrNull()
            ?: return denied(context, "<red>Could not read that statistic.")
        return if (Comparisons.numeric(value.toDouble(), operator, threshold.toDouble())) pass()
        else denied(context, "<red>Requirement not met.")
    }

    private fun readStatistic(player: Player): Int {
        val statistic = Statistic.valueOf(statName.trim().uppercase())
        return when (statistic.type) {
            Statistic.Type.UNTYPED -> player.getStatistic(statistic)
            Statistic.Type.ITEM, Statistic.Type.BLOCK ->
                player.getStatistic(statistic, Material.valueOf(materialName!!.trim().uppercase()))
            Statistic.Type.ENTITY ->
                player.getStatistic(statistic, EntityType.valueOf(entityName!!.trim().uppercase()))
        }
    }
}
