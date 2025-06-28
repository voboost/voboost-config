# voboost-config

A type-safe YAML configuration library for Android with real-time file watching capabilities.

## Features

- **Type-safe** - Compile-time configuration validation with Kotlin data classes
- **YAML Support** - Load and save configurations in human-readable YAML format
- **Real-time Watching** - Automatic detection of configuration file changes
- **Diff Calculation** - Know exactly which fields changed
- **Error Resilient** - Result-based API prevents crashes
- **Android Optimized** - Uses Android file system best practices

## Quick Start

### 1. Add Dependency

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("ru.voboost:voboost-config:1.0.0")
}
```

### 2. Create Configuration File

Create `config.yaml` in your app's assets or files directory:

```yaml
settings:
  language: "en"
  theme: "dark"
  interface-shift-x: 0
  interface-shift-y: 0

vehicle:
  fuel-mode: "electric"
  drive-mode: "sport"
```

### 3. Load Configuration

```kotlin
val configManager = ConfigManager()

// Load configuration
val result = configManager.loadConfig(context, "config.yaml")
result.onSuccess { config ->
    val language = config.settings?.language  // Language.en
    val theme = config.settings?.theme        // Theme.dark
    val fuelMode = config.vehicle?.fuelMode   // FuelMode.electric
}.onFailure { error ->
    Log.e("Config", "Failed to load configuration", error)
}
```

### 4. Watch for Changes

```kotlin
val listener = object : OnConfigChangeListener {
    override fun onConfigChanged(newConfig: Config, diff: Config) {
        // React to specific changes
        diff.settings?.theme?.let { newTheme ->
            updateAppTheme(newTheme)
        }
        diff.settings?.language?.let { newLanguage ->
            updateAppLanguage(newLanguage)
        }
    }
}

// Start watching for changes
configManager.startWatching(context, "config.yaml", listener)
```

### 5. Save Configuration

```kotlin
val newConfig = Config(
    settings = Settings(
        language = Language.ru,
        theme = Theme.light
    ),
    vehicle = Vehicle(
        fuelMode = FuelMode.fuel,
        driveMode = DriveMode.comfort
    )
)

val result = configManager.saveConfig(context, "config.yaml", newConfig)
result.onSuccess {
    Log.d("Config", "Configuration saved successfully")
}.onFailure { error ->
    Log.e("Config", "Failed to save configuration", error)
}
```

## Configuration Model

### Settings Section

```kotlin
data class Settings(
    val language: Language? = null,        // ru, en
    val theme: Theme? = null,              // auto, light, dark
    val interfaceShiftX: Int? = null,      // Interface X positioning
    val interfaceShiftY: Int? = null       // Interface Y positioning
)
```

### Vehicle Section

```kotlin
data class Vehicle(
    val fuelMode: FuelMode? = null,        // intellectual, electric, fuel, save
    val driveMode: DriveMode? = null       // eco, comfort, sport, snow, outing, individual
)
```

### Supported Enums

```kotlin
enum class Language { ru, en }
enum class Theme { auto, light, dark }
enum class FuelMode { intellectual, electric, fuel, save }
enum class DriveMode { eco, comfort, sport, snow, outing, individual }
```

## API Reference

### ConfigManager

The main facade providing all configuration operations:

```kotlin
class ConfigManager {
    // Load configuration from YAML file
    fun loadConfig(context: Context, filePath: String): Result<Config>

    // Save configuration to YAML file
    fun saveConfig(context: Context, filePath: String, config: Config): Result<Unit>

    // Start watching file for changes
    fun startWatching(context: Context, filePath: String, listener: OnConfigChangeListener): Result<Unit>

    // Stop watching and cleanup resources
    fun stopWatching()
}
```

### OnConfigChangeListener

Interface for receiving configuration change notifications:

```kotlin
interface OnConfigChangeListener {
    fun onConfigChanged(newConfig: Config, diff: Config)
}
```

The `diff` parameter contains only the fields that changed, with unchanged fields set to `null`.

## File Locations

Configuration files are stored in the app's private files directory:
- **Path**: `Context.filesDir + filePath`
- **Example**: `/data/data/your.package.name/files/config.yaml`
- **Security**: Private to your app, no special permissions required

## Error Handling

All operations return `Result<T>` objects for safe error handling:

```kotlin
val result = configManager.loadConfig(context, "config.yaml")

// Handle success and failure
result.fold(
    onSuccess = { config -> /* use config */ },
    onFailure = { error -> /* handle error */ }
)

// Or use individual handlers
result.onSuccess { config ->
    // Configuration loaded successfully
}.onFailure { error ->
    // Handle specific error types
    when (error) {
        is IllegalArgumentException -> // File not found
        is Exception -> // Parse error or I/O error
    }
}
```

## Demo Application

A complete demo application is available in the `voboost-config-demo` project, showing:

- Configuration loading and display
- Real-time change detection
- File system integration
- User interface updates

### Running the Demo

```bash
# Clone the repository
git clone <repository-url>

# Build and install demo app
cd voboost-config-demo
./gradlew installDebug

# Push configuration file and start app
adb push src/main/assets/config.yaml /data/user/0/ru.voboost.config.demo/files/config.yaml
adb shell am start -n ru.voboost.config.demo/.MainActivity

# Test real-time updates by modifying and pushing the config file
# Edit src/main/assets/config.yaml, then:
adb push src/main/assets/config.yaml /data/user/0/ru.voboost.config.demo/files/config.yaml
```

## Requirements

- **Android API 28+** (Android 9.0)
- **Kotlin** project
- **No special permissions** required

## Dependencies

- **Hoplite** - YAML parsing and file watching
- **Kotlin Coroutines** - Async operations

## License

[Add your license information here]

## Contributing

[Add contributing guidelines here]

## Support

[Add support information here]
