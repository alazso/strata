<div align="center">

# 🧱 Strata

#### Shared library foundation for Paper, Folia, and Purpur plugins

[![Build](https://img.shields.io/github/actions/workflow/status/alazso/strata/ci.yml?branch=main&style=for-the-badge&label=build)](https://github.com/alazso/strata/actions)
[![Downloads](https://img.shields.io/modrinth/dt/strata-api?style=for-the-badge&logo=modrinth&label=downloads)](https://modrinth.com/plugin/strata-api)
[![Minecraft](https://img.shields.io/badge/Paper%20·%20Folia%20·%20Purpur-26.1%2B-2b2d31?style=for-the-badge)](https://papermc.io/)
[![License](https://img.shields.io/github/license/alazso/strata?style=for-the-badge&label=license)](LICENSE)

**[📖 Documentation](https://alaz.so/strata/docs)**  ·  **[💻 Source](https://github.com/alazso/strata)**

</div>

<br>

> ⚠️ **Strata is a shared library, not a standalone plugin.** Install it once on the server (like Vault or LuckPerms) because a plugin depends on it; on its own it does nothing. It is currently under review on Modrinth, so for now grab it from [GitHub releases](https://github.com/alazso/strata/releases).

<br>

## Features

|   |   |
|---|---|
| 🗓️ **Folia-safe scheduler** | One scheduling API over region, global, async, and entity threads. |
| 💬 **Text and messages** | MiniMessage with PlaceholderAPI and MiniPlaceholders, plus a localized message catalog. |
| 🗄️ **Storage** | Pooled SQLite, MySQL, MariaDB, and PostgreSQL with migrations and key-value stores. |
| 🔌 **Integrations** | Permissions, economy, regions, and custom items behind one degrade-gracefully registry. |
| 🎚️ **Conditions** | Config-driven predicates: permission, world, region, economy, rank, and more. |
| 🖼️ **GUIs** | Holder-based chest menus, pagination, and anvil and chat input. |
| 🧰 **Utilities** | Player lookup, item serialization, skull builder, PDC keys, config schema, cooldowns. |
| 🪶 **Folia-ready** | Off-thread storage and region-safe scheduling throughout. |

<br>

## 🚀 Using it

Install `strata-<version>.jar` in `plugins/`, then build against the API:

```kotlin
// build.gradle.kts
repositories { maven("https://repo.alaz.so/releases") }
dependencies { compileOnly("so.alaz.strata:strata-api:0.9.0") }
```

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
StrataApi.scheduler(this).async(() -> doWork());             // Folia-safe scheduling
StrataApi.messages(this).send(player, "welcome");            // localized messages
EconomyHook eco = StrataApi.hooks().get(EconomyHook.class);  // null if none installed
```

Your plugin ships no Kotlin runtime of its own; Strata provides it at runtime. The **[documentation](https://alaz.so/strata/docs)** covers every service with examples.

<br>

## 📦 Requirements

|   |   |
|---|---|
| **Server** | Paper, Folia, or Purpur |
| **Minecraft** | 26.1 or newer |
| **Java** | 25 |

<br>

## 🗺️ Roadmap

* Hologram provider implementations
* Scoreboard and bossbar helpers
* Placeholder expansion registration

<br>

<div align="center">

Released under the [MIT License](LICENSE).  ·  [Contributing](CONTRIBUTING.md)

</div>
