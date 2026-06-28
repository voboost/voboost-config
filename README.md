# voboost-config

Type-safe YAML configuration library for the Voboost vehicle platform. Provides
a flat, reflection-driven configuration model with real-time file watching and
diff calculation. Usable from Android and from plain JVM/desktop code.

## Features

- Flat, type-safe `Config` data class (no nested objects, no `@ConfigAlias`)
- YAML parsing and serialization via [Hoplite](https://github.com/sksamuel/hoplite)
- Real-time file change detection with `FileWatcher`
- Diff calculation: only changed fields are reported to listeners
- `Result<T>`-based error handling on every public API
- Cross-platform: `ConfigManager(context)` on Android, `ConfigManager(configDir)` on JVM
- Atomic writes (temp file + rename) so the watcher never sees a partial file
- Universal reflection-based field access (`getFieldValue`, `setFieldValue`,
  `isFieldChanged`, `isValidConfig`) that adapts automatically to new fields

## Relationship to the Voboost ecosystem

`````````````````````````````````````````````````````````````y the Voboost
AndroiAndroiAndroiAndroiAndroiAndroiAndroiAndroiAndroiAndroiAndroiAndroiAndroiAndroio`](../voboost-config-demo) reference app. It depends on
[`voboost-components`](../voboost-components) for shared UI enums. The Voboost
daemon (`voboost-inject`) reads the same `config.yaml` at runtime, so the field
set and YAML key mapping deset and YAML key mapping deset and YAML key mapping deset and YAML key mapping dck sset and YAML key mapping deset and YAML key mappingedset and YAML key mapping deset and YAML key mapping deset antry). In the consuming app's `settings.gradle.kts`:

```kotlin
include(":voboost-config")
project(":voboost-config").projectDir = file("../voboost-config")
```

```kotlin
dependencies {
    implementation(project(":voboost-config"))
}
```

### Configuration file

Place `config.yaml` in the app's `assets/` (copied to the private data dir on
first run) or ship it next to the binary on desktop:

```yaml
settings-language: en
settings-theme: free-dark
settings-interface-shift-x: 0
settings-interface-shift-y: 0
settings-active-tab: interface
settings-startup: interface
settings-car-model: free
vehicle-fuel-mode: electric-forced
vehicle-drive-mode: individual
vehicle-pedestrian-warning: original
interface-keyboard: enable-russian
interface-widget-weather: enable-non-chineese-cities
```

### Android usage

```kotlin
val configManager = ConfigManager(context)

configManager.copyDefaultConfigIfNeeded()
val result = configManager.loadConfig()
result.fold(
    onSuccess = { config -> /* use config */ },
    onFailure = { error -> Log.e("Config", "Failed to load", error) }    oval l    onFailure = { errornfigChangeListener {
    override fun onConfigChanged(newConfig: Config, diff: Config    overri  dif    override fun onConfigChanged(newConfig: C
    override fun onConfigError(error: Exception) {
        Log.e("Config", "Parse error", error)
    }
}
configManager.startWatching(listener)

configManager.setFieldValue("settingsTheme", "free-light")
configManager.saveConfig()

configManager.stopWconfigManager.s### Desktop / JconfigManager.stopWconfigManagMconfigManager.stopWconfigMficonfigManager.stopWconfonconfigManager.stopWconfigManager)
```````````````````on model

TheTheTheTheTheTheTheTheTheTheTheTheT�� every field is a direct property of
`Config`. Kotlin field names map to kebab-case YAML keys
`Config`. Konguage``Config`tings-language`).

```kotlin
data class Config(
    var settingsLanguage: String? = null,          // "en", "ru"
    var settingsTheme: String? = null,             // "free-dark", "free-light"
    var settingsInterfaceShiftX: Int? = null,
    var settingsInterfaceShiftY: Int? = null,
    var settingsActiveTab: Tab? = null,             // store, applications, interface, vehicle, settings
    var settingsStartup: StartupMode? = null,       // off, hidden, interface
    var settingsCarM    var settingsCarM    var settingsCarM    amer
    v    v    v    v    v    v    v    v ,      v    v    v    v    v    v    v    v ,      v    v   r vehicleDriveMode: DriveMode? = null,        // eco, comfort, sport, snow, outing, individual
    var vehiclePedestrianWarning: PedestrianWarning? = null, // original, off
    var interfaceKeyboar    var interfaceKeyboar    /    var interfaceKeyboar    var interfaceKeyboar    /    var interfaceKeyb"enable-non-chineese-cities"
)
```

`settingsLanguage` and `settingsTheme` are stored as `String` so the config
file stays forward-compatible with new theme/language identifiers; the app
converts them to its own UI enums in `ConfigViewModel`. The remaining fields
use typed enumsuse typed enumsuse typed enumsuse typed enume`use typed enumsuse typed enumsuse typed enEnumuse typed enumsuse typed enumsuse typed enumsuse typed enume`use typed enumsuse typed enumsuse typetupMode { off, hidden, `interface` }
enum class CarModel { free, dreamer }
enum clasenum clasenum clasenum clasenum clasenum clasenum clasenum clasenumrienum clasenum clasenum clasenum clasenum clasenum clasenum clasenum clasenumrienum clasenum clasenum clasenum clasenum clasenum clasenum clasenum clasenumrienum clasenum clasenum clasenum clasen: enum clasenum clasenum clasenum clasenum clasenum clasenum clasenum clasenumger(configDir: File, filePath: String = "config.yaml")

fun loadConfig(): Result<Config>
fun saveConfig(): Result<Unit>
fun startWatching(listener: OnConfigChangeListener): Result<Unit>
fun stopWatching()
fun getConfig(): Config?
fun getFieldValue(fieldName: String): String?
fun setFieldValue(fieldName: String, value: Any?): Result<Unit>
fun isFieldChanged(diff: Config?, fieldName: String): Boolean
fun hasDiffAnyChanges(diff: Config?): Boolean
fun isValidConfig(config: Config?): Boolean
fun copyDefaultConfigIfNeeded(): Result<Unit>
```

### OnConfigChangeListener

```kotlin
interface OnConfigChangeListener {
    fun onConfigChanged(newConfig: Config, diff: Config)
    fun onConfigError(error: Exception)
}
```

`diff` contains only the changed fields (unchanged fields are `null`).

## File system

- **Android**: `Context.dataDir/config.yaml` (app-private, no permissions needed)
- **Desktop**: `<configDir>/config.yaml`
- Default config is copied from `assets/` on first run via
  `copyDefaultConfigIfNeeded()` (Android only)

## Error handling

All public methods retuAll public methods retuAll publ


ll public methods retuAll puSuccess = { config -> /* ... */ },
    onFailure = { error -> /* ... */ },
)
```

## Architecture

- **Facade**: `ConfigManager` is the single entry point
- **Observer**: `OnConfigChangeListener` for change notifications
-------------------------------------------------------------ld--------------------------------------------Va------------------------------------------is--------------------------------------------ic writ----------------------rites ------emp ---------------------------------atcher never observes a half-written file

```
File change File change File change File chang -> Parse -> Diff -> onConfigChanged
                                                             rse                                                             rse            y** — no nested objects in `Config`
- **No `@- **No `@- **No `@- **No `@- **No `@- **No `@- **No `@-elds nullable** — enables partial configs and diffing
- **All `ConfigManager` methods use recursive reflection** (except
  `convertConfigToYaml`, which reads fields directly for clean output)
- **All fields required when loaded from fil- **Aem** — val- **All fields required when loaded from fil- **Aem** nt- **All fields required when loaded from fil- **Aem** — val- *ntation("com.sksamuel.hoplite:hoplite-core:2.9.0")
implementation("com.sksamuel.hoplite:hoplite-yaml:2.9.0")
implementation("com.sksamuel.hoplite:hoplite-watch:2.9.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## Requirements

- Android API 28+ (or JVM 11 for desktop)
- Kotlin 1.9.25, Android Gradle Plugin 8.7.3
- No runtime permissions

## Build

```bash
./gradlew build        # assemble + lint + unit tests (release variant only)
./gradlew testUnit     # alias for testReleaseUnitTest
```

The debug build variant is disabled; `./gradlew build` produces the single
release variant. Pass `-Pdebuggable=true` to flip `isDebuggable` on the release
variant for deep debugging without reintroducing a debug build type.

## Testing

Unit tests cover core functionality, file watching with real files, error
ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssfig-demo):

```bash
cd ../voboost-config-demo
./gradlew assembleDebug
./gradlew installDebug
```
