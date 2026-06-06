package so.alaz.strata.hook

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.managers.RegionManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import so.alaz.strata.api.hook.RegionHook

/**
 * WorldGuard-backed [RegionHook]. No WorldGuard-typed fields exist (only a `present` flag); all
 * WorldGuard and WorldEdit references live in wrapped method bodies, so the class loads when
 * WorldGuard is absent and every query degrades safely instead of throwing. [canBuild] degrades to
 * `true` (do not block actions when the region provider is unavailable); the read queries degrade to
 * empty/false.
 */
internal class WorldGuardRegionHook : RegionHook {

    private val present: Boolean =
        runCatching { Class.forName("com.sk89q.worldguard.WorldGuard", false, javaClass.classLoader) }.isSuccess

    override fun name(): String = "WorldGuard"

    override fun isAvailable(): Boolean =
        present && runCatching { Bukkit.getPluginManager().isPluginEnabled("WorldGuard") }.getOrDefault(false)

    override fun regionsAt(location: Location): List<String> = runCatching {
        val manager = regionManager(location.world) ?: return emptyList()
        manager.getApplicableRegions(BukkitAdapter.asBlockVector(location)).regions.map { it.id }
    }.getOrDefault(emptyList())

    override fun isInRegion(location: Location, regionId: String): Boolean =
        regionsAt(location).any { it.equals(regionId, ignoreCase = true) }

    override fun canBuild(player: Player, location: Location): Boolean = runCatching {
        val query = WorldGuard.getInstance().platform.regionContainer.createQuery()
        val subject = WorldGuardPlugin.inst().wrapPlayer(player)
        query.testBuild(BukkitAdapter.adapt(location), subject)
    }.getOrDefault(true)

    private fun regionManager(world: World?): RegionManager? {
        if (world == null) return null
        return WorldGuard.getInstance().platform.regionContainer.get(BukkitAdapter.adapt(world))
    }
}
