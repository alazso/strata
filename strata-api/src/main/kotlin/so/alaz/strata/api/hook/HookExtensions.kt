package so.alaz.strata.api.hook

/** Kotlin sugar: `registry.get<EconomyHook>()`. */
public inline fun <reified T : Hook> HookRegistry.get(): T? = get(T::class.java)

/** Kotlin sugar: `registry.require<EconomyHook>()`. */
public inline fun <reified T : Hook> HookRegistry.require(): T = require(T::class.java)

/** Kotlin sugar: `registry.isAvailable<EconomyHook>()`. */
public inline fun <reified T : Hook> HookRegistry.isAvailable(): Boolean = isAvailable(T::class.java)

/** Kotlin sugar: `registry.register(hook, priority)` with the type inferred. */
public inline fun <reified T : Hook> HookRegistry.register(
    hook: T,
    priority: Int = HookRegistry.DEFAULT_PRIORITY,
): Unit = register(T::class.java, hook, priority)

/** Kotlin sugar: `registry.setPreference<EconomyHook>("conduit")`. */
public inline fun <reified T : Hook> HookRegistry.setPreference(name: String?): Unit =
    setPreference(T::class.java, name)
