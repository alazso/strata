package so.alaz.strata;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

/**
 * Bootstraps Strata's runtime libraries through Paper's library loader.
 *
 * <p>Written in Java on purpose: Paper instantiates the loader <em>before</em> any library is on
 * the classpath, so it must not reference the Kotlin stdlib. It pulls kotlin-stdlib (and the rest)
 * down from Maven Central, after which the Kotlin main class and implementation load cleanly.
 * The server owner installs nothing extra — Paper caches these in its library store on first start.
 *
 * <p><strong>Coordinates MUST stay in sync with {@code gradle/libs.versions.toml}.</strong>
 * Only top-level artifacts are listed; transitive dependencies resolve automatically.
 */
@SuppressWarnings("UnstableApiUsage")
public class StrataLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpath) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder(
                "central", "default", "https://repo1.maven.org/maven2/").build());

        for (String gav : new String[]{
                "org.jetbrains.kotlin:kotlin-stdlib:2.4.0",
                "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0",
                "org.jetbrains.exposed:exposed-core:1.3.0",
                "org.jetbrains.exposed:exposed-dao:1.3.0",
                "org.jetbrains.exposed:exposed-jdbc:1.3.0",
                "com.zaxxer:HikariCP:7.0.2",
                "org.xerial:sqlite-jdbc:3.53.2.0",
                "org.mariadb.jdbc:mariadb-java-client:3.5.8",
                "org.postgresql:postgresql:42.7.7",
        }) {
            resolver.addDependency(new Dependency(new DefaultArtifact(gav), null));
        }

        classpath.addLibrary(resolver);
    }
}
