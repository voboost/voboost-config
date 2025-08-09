# voboost-config

Type-safe YAML configuration library for Android with real-time file watching.

## Features

- Type-safe configuration with Kotlin data classes
- YAML parsing and serialization
- Real-time file change detection
- Diff calculation for changed fields
- Result-based error handling
- Extended API with utilities
- Flat configuration structure
- Universal reflection pattern

## Quick Start

### Add Dependency

```kotlin
dependencies {
    implementation("ru.voboost:voboost-config:1.0.0")
}
```

### Configuration File

Create `config.yaml` in assets:

```yaml
settings-language: en
settings-theme: dark
settings-interface-shift-x: 0
settings-interface-shift-y: 0
settings-active-tab: interface
vehicle-fuel-mode: electric
vehicle-drive-mode: sport
```

### Basic Usage

```kotlin
val configManager = ConfigManager(context)

// Load configuration
val result = configManager.loadConfig()
result.fold(
    onSuccess = { config ->
        val language = config.settingsLanguage
        val theme = config.settingsTheme
    },
    onFailure = { error ->
        Log.e("Config", "Failed to load", error)
    }
)

// Watch for changes
val listener = object : OnConfigChangeListener {
    override fun onConfigChanged(newConfig: Config, diff: Config) {
        diff.settingsTheme?.let { updateTheme(it) }
    }
    override fun onConfigError(error: Exception) {
        Log.e("Config", "Parse error", error)
    }
}
configManager.startWatching(listener)

// Modify and save
configManager.setFieldValue("settingsTheme", Theme.light)
configManager.saveConfig()

// Cleanup
configManager.stopWatching()
```

## Configuration Model

### Data Structure

```kotlin
data class Config(
    val settingsLanguage: Language? = null,           // ru, en
    val settingsTheme: Theme? = null,                 // light, dark
    val settingsInterfaceShiftX: Int? = null,
    val settingsInterfaceShiftY: Int? = null,
    val settingsActiveTab: Tab? = null,               // store, applications, interface, vehicle, settings
    val vehicleFuelMode: FuelMode? = null,            // electric, hybrid, save
    val vehicleDriveMode: DriveMode? = null           // eco, comfort, sport, snow, outing, individual
)
```

### Enums

```kotlin
enum class Language { ru, en }
enum class Theme { light, dark }
enum class Tab { store, applications, `interface`, vehicle, settings }
enum class FuelMode { electric, hybrid, save }
enum class DriveMode { eco, comfort, sport, snow, outing, individual }
```

### YAML Mapping

Kotlin field names map to kebab-case YAML keys:
- `settingsLanguage` → `settings-language`
- `vehicleFuelMode` → `vehicle-fuel-mode`

## API Reference

### ConfigManager

```kotlin
class ConfigManager(context: Context, filePath: String = "config.yaml") {
    // Core operations
    fun loadConfig(): Result<Config>
    fun saveConfig(): Result<Unit>

    // File watching
    fun startWatching(listener: OnConfigChangeListener): Result<Unit>
    fun stopWatching()

    // Current config
    fun getConfig(): Config?

    // Field operations
    fun getFieldValue(fieldName: String): String?
    fun setFieldValue(fieldName: String, value: Any?): Result<Unit>

    // Diff utilities
    fun isFieldChanged(diff: Config?, fieldName: String): Boolean
    fun hasDiffAnyChanges(diff: Config?): Boolean

    // Validation
    fun isValidConfig(config: Config?): Boolean

    // Asset management
    fun copyDefaultConfigIfNeeded(): Result<Unit>
}
```

### OnConfigChangeListener

```kotlin
interface OnConfigChangeListener {
    fun onConfigChanged(newConfig: Config, diff: Config)
    fun onConfigError(error: Exception) // Default empty implementation
}
```

The `diff` parameter contains only changed fields (unchanged fields are `null`).

## File System

- **Storage**: `Context.dataDir/config.yaml`
- **Security**: App private directory, no permissions required
- **Assets**: Copy default config from assets on first run

```kotlin
// Copy from assets if needed
configManager.copyDefaultConfigIfNeeded()
```

## Error Handling

All operations return `Result<T>`:

```kotlin
result.fold(
    onSuccess = { config -> /* use config */ },
    onFailure = { error -> /* handle error */ }
)
```

## File Watching

Uses Hoplite's `FileWatcher` for change detection:

```kotlin
val reloadable = ReloadableConfig(configLoader, Config::class)
    .addWatcher(FileWatcher(directory))
    .withErrorHandler { error ->
        coroutineScope.launch { handleConfigError(error) }
    }
```

## Extended Utilities

```kotlin
// Check specific field changes
if (configManager.isFieldChanged(diff, "settingsTheme")) {
    val newTheme = configManager.getFieldValue("settingsTheme")
    updateTheme(newTheme)
}

// Validate completeness (all fields except settingsActiveTab required)
if (configManager.isValidConfig(config)) {
    applyConfiguration(config)
}

// Check for any changes
if (configManager.hasDiffAnyChanges(diff)) {
    notifyConfigurationChanged()
}
```

## Architecture

### Design Patterns

- **Facade Pattern**: ConfigManager provides unified API
- **Observer Pattern**: OnConfigChangeListener for change notifications
- **Result Pattern**: Consistent error handling
- **Reflection Pattern**: Universal field access

### File Watching Flow

```
File Change → FileWatcher → ReloadableConfig → Parse → Diff → Callback
                                                   ↓
                                            Parse Error → OnConfigError
```

## Implementation Rules

### Configuration Structure

- **FLAT structure only** - no nested objects
- **No @ConfigAlias annotations** - direct kebab-case mapping
- **All fields nullable** for partial configurations
- **Kebab-case YAML keys**: `settingsLanguage` → `settings-language`

### Reflection Rules

- **ALL ConfigManager methods use recursive reflection** (not direct field access)
- **Config type only** - never use `Any` type
- **Exception**: Only `convertConfigToYaml` uses direct field access
- **Universal methods** work with any number of fields
- **Extensible design** handles new fields automatically

### Validation Rules

- **All fields required** when loaded from filesystem
- **Recursive validation** through `validateObjectRecursively`
- **Type-safe validation** using reflection

## Dependencies

```kotlin
// YAML processing and file watching
implementation("com.sksamuel.hoplite:hoplite-core:2.9.0")
implementation("com.sksamuel.hoplite:hoplite-yaml:2.9.0")
implementation("com.sksamuel.hoplite:hoplite-watch:2.9.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## Requirements

- Android API 28+
- Kotlin project
- No special permissions

## Demo Application

Complete demo available in `../voboost-config-demo/`:

```bash
cd ../voboost-config-demo
./gradlew assembleDebug
./gradlew installDebug
```

## Testing

Unit tests covering:
- Core functionality
- File watching with real files
- Error scenarios
- Extended utilities
- Integration tests

## Build

```bash
# Library
./gradlew build
./gradlew test

# Demo app
cd ../voboost-config-demo
./gradlew assembleDebug
