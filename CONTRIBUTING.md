# Contributing

## Prerequisites

- JDK 25 (the Gradle toolchain provisions it if it is not installed)
- The Gradle wrapper (`./gradlew`); there is no need to install Gradle separately

## Build and test

```bash
./gradlew build                              # compile, run tests, build both modules' jars
./gradlew test                               # tests only
./gradlew :strata-plugin:shadowJar           # the server plugin jar
./gradlew :strata-api:publishToMavenLocal    # the API jar, for testing a consumer locally
```

## Project structure

```
strata-api/      Published, Java-friendly public API. Interfaces and pure-API helpers only.
strata-plugin/   The shaded server deliverable: implementations, lifecycle, and the loader.
```

- `strata-api` is what dependent plugins compile against. Keep it thin and
  Java-friendly: no `suspend` on the public surface (async returns
  `CompletableFuture`), `@JvmStatic`/`@JvmOverloads` where they help, and no
  Kotlin-only types in public signatures.
- `strata-plugin` holds the implementations and the runtime services.

## Conventions

- **Runtime libraries are not shaded.** Heavy dependencies (Kotlin stdlib,
  coroutines, Exposed, HikariCP, JDBC drivers) are loaded at runtime by
  `StrataLoader`, which is written in Java because it runs before the Kotlin
  stdlib is on the classpath. They are `compileOnly` in Gradle. The coordinates
  in `StrataLoader.java` must stay in sync with `gradle/libs.versions.toml`.
- **Only `strata-api` is shaded into the plugin jar**, along with bStats and
  FastStats, which are relocated. Keep the jar small and free of the runtime
  libraries.
- **Integrations are soft dependencies.** A hook adapter holds no third-party
  fields (only a presence flag); all third-party references live in method
  bodies and are guarded, so the adapter loads and registers even when the
  backing plugin is absent, and reports unavailable instead of throwing.

## Adding things

- **A hook adapter:** implement the capability interface in `strata-plugin`, add
  the integration API as `compileOnly`, add a soft dependency in
  `paper-plugin.yml`, and register the adapter in `Strata`.
- **A condition type:** register a factory with the condition registry.
- **A metric chart:** use `MetricChart`; it routes to every enabled backend.

## Releasing

1. Bump `version` in `gradle.properties`.
2. Commit and push.
3. Tag `v<version>`. CI publishes the server jar to the GitHub release and the
   `strata-api` jar (with sources and Javadoc) to repo.alaz.so.
