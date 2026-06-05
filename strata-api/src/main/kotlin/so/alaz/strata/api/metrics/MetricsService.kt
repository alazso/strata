package so.alaz.strata.api.metrics

import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.ApiStatus

/**
 * Entry point for metrics, obtained via `StrataApi.metrics()`. The bStats and FastStats libraries are
 * shaded + relocated inside Strata, so consumers don't shade their own — they just provide their
 * id/token and (optionally) custom charts.
 */
@ApiStatus.Experimental
public interface MetricsService {

    /** Begins configuring metrics for [plugin]. */
    public fun create(plugin: JavaPlugin): MetricsBuilder
}
