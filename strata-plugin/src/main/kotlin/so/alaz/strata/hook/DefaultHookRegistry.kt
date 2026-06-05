package so.alaz.strata.hook

import so.alaz.strata.api.hook.Hook
import so.alaz.strata.api.hook.HookRegistry
import java.util.concurrent.ConcurrentHashMap

/**
 * Default [HookRegistry]. Registration is synchronized; resolution snapshots the current entries,
 * filters by [Hook.isAvailable], and picks the highest priority — so availability is re-checked on
 * every [get] and a provider that goes away is simply skipped.
 */
internal class DefaultHookRegistry : HookRegistry {

    private class Entry(val hook: Hook, val priority: Int)

    private val byType = ConcurrentHashMap<Class<out Hook>, MutableList<Entry>>()
    private val preferences = ConcurrentHashMap<Class<out Hook>, String>()

    @Synchronized
    override fun <T : Hook> register(type: Class<T>, hook: T, priority: Int) {
        byType.getOrPut(type) { mutableListOf() }.add(Entry(hook, priority))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Hook> get(type: Class<T>): T? {
        val available = entries(type).filter { it.hook.isAvailable() }
        if (available.isEmpty()) return null
        preferences[type]?.let { preferred ->
            available.firstOrNull { it.hook.name().equals(preferred, ignoreCase = true) }
                ?.let { return it.hook as T }
        }
        return available.maxByOrNull { it.priority }?.hook as T?
    }

    override fun <T : Hook> setPreference(type: Class<T>, name: String?) {
        if (name == null) preferences.remove(type) else preferences[type] = name
    }

    override fun <T : Hook> require(type: Class<T>): T =
        get(type) ?: error("No available ${type.simpleName} hook is registered")

    override fun <T : Hook> isAvailable(type: Class<T>): Boolean = get(type) != null

    @Suppress("UNCHECKED_CAST")
    override fun <T : Hook> all(type: Class<T>): List<T> =
        entries(type).sortedByDescending { it.priority }.map { it.hook as T }

    private fun entries(type: Class<out Hook>): List<Entry> =
        synchronized(this) { byType[type]?.toList() } ?: emptyList()
}
