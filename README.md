# Strata

A shared library plugin for Paper and Folia. Strata gives the plugins in one
ecosystem a single, tested set of building blocks (scheduling, storage,
integrations, conditions, GUIs, commands, and metrics) instead of each plugin
re-implementing them.

It is installed once on the server, the same way as Vault or LuckPerms, and your
plugins depend on it. Shared, stateful services such as the connection pool and
the integration registry therefore live in one place rather than in a separate
copy inside every plugin.

Full documentation lives at <https://alaz.so/strata>.

## Using Strata

Add the Maven repository and the API artifact (compile-time only):

```kotlin
repositories {
    maven("https://repo.alaz.so/releases") { name = "alazso" }
}

dependencies {
    compileOnly("so.alaz.strata:strata-api:<version>")
}
```

Declare Strata as a required dependency that loads first:

```yaml
# paper-plugin.yml
dependencies:
  server:
    Strata:
      load: BEFORE
      required: true
```

Then reach everything through `StrataApi`:

```java
StrataApi.scheduler(this).async(() -> doWork());            // Folia-safe scheduling
EconomyHook eco = StrataApi.hooks().get(EconomyHook.class); // null if no economy plugin is present
```

Your plugin ships no Kotlin standard library of its own; it is provided at
runtime by Strata.

## What it provides

- Scheduler: a Folia-safe wrapper over the Region, Global, Async, and Entity schedulers
- Text: MiniMessage and PlaceholderAPI rendering in the correct resolution order
- Storage: pooled SQLite/MySQL with a migration runner, plus an Exposed/coroutines layer
- PDC helpers, config-schema validation, and keyed cooldowns
- Hooks: permissions, economy, regions, items, and holograms behind one registry
- Metrics: bStats and FastStats with error tracking and feature flags
- Conditions: config-driven predicates (permission, economy, region, and more)
- GUI: holder-identified chest menus, pagination, and anvil/chat input
- Commands: a fluent Brigadier builder with common and debug subcommands

## Building

Requires JDK 25. Use the Gradle wrapper:

```bash
./gradlew build                       # compile, test, and build both artifacts
./gradlew :strata-plugin:shadowJar    # the server plugin jar only
```

The build produces two artifacts:

- `strata-<version>.jar` (from `:strata-plugin`): the server plugin, installed in `/plugins`
- `strata-api-<version>.jar` (from `:strata-api`): the compile-time API, published to repo.alaz.so

Heavy runtime libraries (Kotlin stdlib, coroutines, Exposed, HikariCP, JDBC
drivers) are downloaded by Paper's library loader on first start, so they are
not bundled in the plugin jar.

See [CONTRIBUTING.md](CONTRIBUTING.md) for project structure and conventions.

## License

MIT. See [LICENSE](LICENSE).
