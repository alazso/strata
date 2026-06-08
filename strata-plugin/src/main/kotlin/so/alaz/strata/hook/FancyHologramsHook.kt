package so.alaz.strata.hook

import de.oliver.fancyholograms.api.FancyHologramsPlugin
import de.oliver.fancyholograms.api.data.TextHologramData
import org.bukkit.Location
import so.alaz.strata.api.hook.HologramHook

/**
 * FancyHolograms-backed [HologramHook]. FancyHolograms is Adventure/MiniMessage-native, so the
 * MiniMessage line strings pass straight through. No FancyHolograms-typed fields exist (only a
 * `present` flag); all references live in wrapped method bodies, so the class loads when
 * FancyHolograms is absent and every operation degrades to `false`. Created holograms are marked
 * non-persistent so they do not accumulate in FancyHolograms' storage across restarts; the caller
 * owns their lifecycle.
 */
internal class FancyHologramsHook : HologramHook {

    private val present: Boolean =
        runCatching { Class.forName("de.oliver.fancyholograms.api.FancyHologramsPlugin", false, javaClass.classLoader) }.isSuccess

    override fun name(): String = "FancyHolograms"

    override fun isAvailable(): Boolean =
        present && runCatching { FancyHologramsPlugin.isEnabled() }.getOrDefault(false)

    override fun create(id: String, location: Location, lines: List<String>): Boolean = runCatching {
        val manager = FancyHologramsPlugin.get().hologramManager
        if (manager.getHologram(id).isPresent) return@runCatching false
        val data = TextHologramData(id, location).setText(lines).setPersistent(false)
        val hologram = manager.create(data)
        manager.addHologram(hologram)
        true
    }.getOrDefault(false)

    override fun update(id: String, lines: List<String>): Boolean = runCatching {
        val hologram = FancyHologramsPlugin.get().hologramManager.getHologram(id).orElse(null)
            ?: return@runCatching false
        (hologram.data as? TextHologramData)?.setText(lines)
        hologram.queueUpdate()
        true
    }.getOrDefault(false)

    override fun remove(id: String): Boolean = runCatching {
        val manager = FancyHologramsPlugin.get().hologramManager
        val hologram = manager.getHologram(id).orElse(null) ?: return@runCatching false
        manager.removeHologram(hologram)
        true
    }.getOrDefault(false)

    override fun exists(id: String): Boolean =
        runCatching { FancyHologramsPlugin.get().hologramManager.getHologram(id).isPresent }.getOrDefault(false)
}
