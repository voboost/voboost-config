# Voboost Config

Cross-platform configuration library for the Voboost automotive platform. It loads,
saves, and watches a flat YAML configuration file and exposes it as a type-safe
Kotlin data model with real-time change detection.

Part of the Voboost ecosystem; depends on [`voboost-components`](../voboost-components)
for the `Theme` and `Language` enums used by the UI layer.

## Features

- Type-safe YAML loading via [Hoplite](https://github.com/sksamuel/hoplite)
- Real-time file watching with diff notifications
- Atomic writes (temp file + rename) to avoid partial-file reads
- Cross-platform: Android (`Context`) and desktop (`File`) constructors
- Completely flat configuration model (no nested objects)
- Result-based public API that never throws

## The Config model

[`Config`](src/main/java/ru/voboost/config/models/Config.kt) is a flat data class.
Every field is nullable so missing YAML values degrade gracefully and partial
updates can be saved. Kotlin property names map to kebab-case YAML keys
(e.g. `settingsLanguage` -> `settings-language`).

| Field | YAML key | Type | Notes |
|-------|----------|------|-------|
| `settingsLanguage` | `settings-language` | `String?` | e.g. `en`, `ru` |
| `settingsTheme` | `settings-theme` | `String?` | e.g. `free-dark`, `free-light` |
| `settingsInterfaceShiftX` | `settings-interface-shift-x` | `Int?` | pixels |
| `settingsInterfaceShiftY` | `settings-interface-shift-y` | `Int?` | pixels |
| `settingsActiveTab` | `settings-active-tab` | `Tab?` | enum |
| `vehicleFuelMode` | `vehicle-fuel-mode` | `FuelMode?` | enum |
| `vehicleDriveMode` | `vehicle-drive-mode` | `DriveMode?` | enum |
| `interfaceKeyboard` | `interface-keyboard` | `String?` | e.g. `enable-russian` |
| `interfaceWidgetWeather` | `interface-widget-weather` | `String?` | e.g. `enable-non-chineese-cities` |
| `settingsStartup` | `settings-startup` | `StartupMode?` | enum |
| `settingsCarModel` | `settings-car-model` | `CarModel?` | enum |
| `vehiclePedestrianWarning` | `vehicle-pedestrian-warning` | `PedestrianWarning?` | enum |

### Enums

- [`Tab`](src/main/java/ru/voboost/config/models/Config.kt): `store`, `applications`, `interface`, `vehicle`, `settings`
- [`FuelMode`](src/main/java/ru/voboost/config/models/Config.kt): `electric`, `electric_forced`, `hybrid`, `save`
- [`DriveMode`](src/main/java/ru/voboost/config/models/Config.kt): `eco`, `comfort`, `sport`, `snow`, `outing`, `individual`
- [`StartupMode`](src/main/java/ru/voboost/config/models/Config.kt): `off`, `hidden`, `interface`
- [`CarModel`](src/main/java/ru/voboost/config/models/Config.kt): `free`, `dreamer`
- [`PedestrianWarning`](src/main/java/ru/voboost/config/models/Config.kt): `original`, `off`

`settingsLanguage` and `settingsTheme` are intentionally `String` rather than enums;
they are converted to the `Language`/`Theme` enums from `voboost-components` by the
application's `ConfigViewModel`.

## ConfigManager

[`ConfigManager`](src/main/java/ru/voboost/config/ConfigManager.kt) is the entry
point. It is instance-based (not a singleton) for testability and has two public
constructors:

```kotlin
// Android: stores config under Context.dataDir, copies a default from assets
val configManager = ConfigManager(context)
val configManager = ConfigManager(context, "custom-config.yaml")

// Desktop: stores config under the given directory
val configManager = ConfigManager(configDir = File("/path/to/config"))
val configManager = ConfigManager(configDir = File("/path/to/config"), filePath = "custom-config.yaml")
```

### Core API

- `loadConfig(): Result<Config>` - load and parse the YAML file
- `saveConfig(): Result<Unit>` - atomic write of the in-memory config
- `startWatching(listener): Result<Unit>` - watch the file for changes
- `stopWatching()` - close the watcher and cancel coroutines
- `setFieldValue(name, value)` - mutate a field by name (recursive reflection)
- `isFieldChanged(diff, fieldName): Boolean` - check a diff for a changed field

### Atomic writes

`saveConfig()` serializes the config to YAML, writes it to a temp file in the same
directory, then renames it onto the target file. This prevents the file watcher from
observing an empty or partially-written file.

### Change detection

`startWatching()` registers a Hoplite `FileWatcher`. On change, the listener receives
both the new `Config` and a diff `Config` containing only the changed fields (unchanged
fields are `null`).

## Build

- Kotlin `2.0.21`, Android Gradle Plugin `8.7.3`
- `compileSdk` 34, `minSdk` 28, Java/JVM target 11
- Release-only: the debug variant is disabled via `androidComponents.beforeVariants`,
  so `./gradlew build` produces a single release AAR. `-Pdebuggable=true` flips the
  release variant's `isDebuggable` flag for deep debugging without reintroducing a
  debug build type.
- Unit tests run on the release variant; the `testUnit` task aliases
  `testReleaseUnitTest`.
- Code style: ktlint (with `enum-entry-name-case` disabled for YAML-compatible
  lowercase enum names), Checkstyle, and Spotless, applied via
  [`voboost-codestyle`](../voboost-codestyle).

```sh
./gradlew build      # assemble release AAR + run unit tests
./gradlew testUnit   # unit tests only
./gradlew lintFix    # ktlint + spotless formatting
```

## Ecosystem

- [`voboost-components`](../voboost-components) - shared UI components and the `Theme`/`Language` enums
- [`voboost-config-demo`](../voboost-config-demo) - single-activity demo of this library
- [`voboost-codestyle`](../voboost-codestyle) - shared Gradle/code-style configuration
