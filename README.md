# voboost-config

A production-ready, type-safe YAML configuration library for Android with real-time file watching capabilities.

## Features

- **Type-safe Configuration** - Compile-time validation with Kotlin data classes
- **YAML Support** - Human-readable configuration format with full parsing
- **Real-time File Watching** - Automatic detection of configuration changes
- **Precise Diff Calculation** - Know exactly which fields changed
- **Result-based Error Handling** - No crashes, comprehensive error management
- **Android Optimized** - Uses Android file system best practices
- **Production Ready** - Comprehensive testing and robust implementation

## Quick Start

### 1. Add Dependency

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("ru.voboost:voboost-config:1.0.0")
}
```

### 2. Create Configuration File

Create `config.yaml` in your app's assets directory:

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

// Load configuration with Result-based error handling
val result = configManager.loadConfig(context, "config.yaml")
result.fold(
    onSuccess = { config ->
        val language = config.settings?.language  // Language.en
        val theme = config.settings?.theme        // Theme.dark
        val fuelMode = config.vehicle?.fuelMode   // FuelMode.electric
    },
    onFailure = { error ->
        Log.e("Config", "Failed to load configuration", error)
    }
)
```

### 4. Watch for Real-time Changes

```kotlin
val listener = object : OnConfigChangeListener {
    override fun onConfigChanged(newConfig: Config, diff: Config) {
        // React to specific changes - diff contains only changed fields
        diff.settings?.theme?.let { newTheme ->
            updateAppTheme(newTheme)
        }
        diff.settings?.language?.let { newLanguage ->
            updateAppLanguage(newLanguage)
        }
        diff.vehicle?.fuelMode?.let { newFuelMode ->
            updateVehicleSettings(newFuelMode)
        }
    }
}

// Start watching for changes
val result = configManager.startWatching(context, "config.yaml", listener)
result.fold(
    onSuccess = {
        Log.d("Config", "Started watching configuration file")
    },
    onFailure = { error ->
        Log.e("Config", "Failed to start watching", error)
    }
)
```

### 5. Save Configuration

```kotlin
val newConfig = Config(
    settings = Settings(
        language = Language.ru,
        theme = Theme.light,
        interfaceShiftX = 10,
        interfaceShiftY = 5
    ),
    vehicle = Vehicle(
        fuelMode = FuelMode.fuel,
        driveMode = DriveMode.comfort
    )
)

val result = configManager.saveConfig(context, "config.yaml", newConfig)
result.fold(
    onSuccess = {
        Log.d("Config", "Configuration saved successfully")
    },
    onFailure = { error ->
        Log.e("Config", "Failed to save configuration", error)
    }
)
```

### 6. Cleanup Resources

```kotlin
override fun onDestroy() {
    super.onDestroy()
    configManager.stopWatching()
}
```

## Configuration Model

### Complete Data Structure

```kotlin
data class Config(
    val settings: Settings? = null,
    val vehicle: Vehicle? = null
)

data class Settings(
    val language: Language? = null,        // ru, en
    val theme: Theme? = null,              // auto, light, dark
    val interfaceShiftX: Int? = null,      // Interface X positioning
    val interfaceShiftY: Int? = null       // Interface Y positioning
)

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

All enum values map directly to YAML string values (lowercase).

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

Interface for receiving real-time configuration change notifications:

```kotlin
interface OnConfigChangeListener {
    fun onConfigChanged(newConfig: Config, diff: Config)
}
```

**Important**: The `diff` parameter contains only the fields that changed. Unchanged fields are set to `null`, allowing precise reaction to specific changes.

## File System Integration

### File Locations
- **Storage**: App's private files directory (`Context.filesDir`)
- **Path Format**: `Context.filesDir + "/" + filePath`
- **Example**: `/data/data/your.package.name/files/config.yaml`
- **Security**: Private to your app, no special permissions required

### Asset Integration
```kotlin
// Copy default configuration from assets on first run
private fun copyDefaultConfigIfNeeded() {
    val configFile = File(filesDir, "config.yaml")
    if (!configFile.exists()) {
        assets.open("config.yaml").use { input ->
            configFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
```

## Error Handling

All operations return `Result<T>` objects for comprehensive error handling:

```kotlin
val result = configManager.loadConfig(context, "config.yaml")

// Recommended: Use fold for complete error handling
result.fold(
    onSuccess = { config ->
        // Configuration loaded successfully
        processConfiguration(config)
    },
    onFailure = { error ->
        // Handle specific error types
        when (error) {
            is IllegalArgumentException -> {
                // File not found or invalid path
                Log.e("Config", "Configuration file not found", error)
            }
            is Exception -> {
                // Parse error, I/O error, or other issues
                Log.e("Config", "Configuration error", error)
            }
        }
    }
)

// Alternative: Use individual handlers
result.onSuccess { config ->
    // Handle success
}.onFailure { error ->
    // Handle failure
}
```

## Real-time File Watching

### How It Works
- **File System Monitoring**: Uses Hoplite's `FileWatcher` for efficient change detection
- **Automatic Parsing**: Immediately parses changed files
- **Diff Calculation**: Compares old and new configurations to identify changes
- **Thread Safety**: Callbacks are delivered on background threads (use `runOnUiThread` for UI updates)

### Best Practices
```kotlin
class MainActivity : AppCompatActivity(), OnConfigChangeListener {
    private val configManager = ConfigManager()

    override fun onConfigChanged(newConfig: Config, diff: Config) {
        // Always switch to UI thread for UI updates
        runOnUiThread {
            updateUI(newConfig, diff)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Always cleanup to prevent memory leaks
        configManager.stopWatching()
    }
}
```

## Demo Application

A complete demonstration application is available in the **voboost-config-demo** project:

- **Location**: Separate project in sibling directory
- **Features**: Real-time visual feedback with red color highlighting
- **Integration Example**: Production-ready implementation patterns
- **Educational Value**: Comprehensive learning resource

### Running the Demo
```bash
# Navigate to demo project
cd ../voboost-config-demo

# Build and install
./gradlew assembleDebug
./gradlew installDebug

# Launch application
adb shell am start -n ru.voboost.config.demo/.MainActivity
```

## Architecture

### Design Patterns
- **Facade Pattern**: Simple 4-method public API
- **Observer Pattern**: Real-time change notifications
- **Result Pattern**: Consistent error handling
- **Builder Pattern**: Hoplite ConfigLoaderBuilder integration

### Key Components
- **ConfigManager**: Main service facade
- **ReloadableConfig**: File watching orchestrator (Hoplite)
- **FileWatcher**: File system monitoring (Hoplite)
- **Config/Settings/Vehicle**: Type-safe data model

## Requirements

- **Android API 28+** (Android 9.0 or higher)
- **Kotlin** project (library is Kotlin-first)
- **No special permissions** required (uses app private storage)

## Dependencies

The library uses these proven, stable dependencies:

```kotlin
// YAML processing and file watching
implementation("com.sksamuel.hoplite:hoplite-core:2.9.0")
implementation("com.sksamuel.hoplite:hoplite-yaml:2.9.0")
implementation("com.sksamuel.hoplite:hoplite-watch:2.9.0")

// Async operations
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## Testing

The library includes comprehensive testing:
- **33+ Unit Tests**: All public API methods covered
- **Integration Tests**: Real file operations and YAML parsing
- **File Watching Tests**: Actual change detection with timing
- **Error Scenario Tests**: Comprehensive failure case coverage

## Production Readiness

✅ **Complete API**: All planned features implemented
✅ **Comprehensive Testing**: 100% public API test coverage
✅ **Error Resilience**: No uncaught exceptions in public methods
✅ **Memory Efficient**: Proper resource cleanup and minimal state
✅ **Thread Safe**: Coroutine-based async operations
✅ **Documentation**: Complete API documentation and examples
✅ **Demo Application**: Working integration example

## License

[Add your license information here]

## Contributing

[Add contributing guidelines here]

## Support

[Add support information here]
