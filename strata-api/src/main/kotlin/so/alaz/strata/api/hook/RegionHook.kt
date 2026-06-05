package so.alaz.strata.api.hook

import org.bukkit.Location
import org.bukkit.entity.Player

/** Region/claim queries behind one interface (WorldGuard, claim plugins, etc.). */
public interface RegionHook : Hook {

    /** Ids of all regions covering [location]. */
    public fun regionsAt(location: Location): List<String>

    /** `true` if [location] is inside the region with id [regionId]. */
    public fun isInRegion(location: Location, regionId: String): Boolean

    /** `true` if [player] is allowed to build at [location]. */
    public fun canBuild(player: Player, location: Location): Boolean
}
