# Technical Context: voboost-config

## Technology Stack

### Core Technologies
- **Kotlin** - Primary development language
- **Android SDK** - Target platform (API 28+)
- **Hoplite 2.9.0** - YAML parsing and file watching
- **Gradle 8.14.1** - Build system with Kotlin DSL

### Testing Framework
- **JUnit 4** - Unit testing framework
- **MockK** - Kotlin mocking library
- **Android Test** - Android-specific testing utilities

### Development Tools
- **Android Studio** - Primary IDE
- **Git** - Version control
- **Gradle Wrapper** - Build tool consistency

## Project Structure

### Project Structure
```
parent-directory/               # Parent directory
├── voboost-config/             # Main library project
│   ├── src/main/java/          # Library source code
│   ├── src/test/java/          # Unit tests
│   └── build.gradle.kts        # Library build configuration
└── voboost-config-demo/        # Separate demo application project
    ├── src/main/java/          # Demo app source
    ├── src/main/res/           # Android resources
    ├── src/main/assets/        # Configuration files
    └── build.gradle.kts        # Demo app build configuration
```

### Package Organization
```
ru.voboost.config/
├── ConfigManager.kt            # Main facade
├── OnConfigChangeListener.kt   # Change notification interface
└── models/
    ├── Config.kt              # Root configuration model
    ├── Settings.kt            # Settings section model
    └── Vehicle.kt             # Vehicle section model
```

## Dependencies

### Library Dependencies
```kotlin
// YAML Processing and File Watching
implementation("com.sksamuel.hoplite:hoplite-core:2.9.0")
implementation("com.sksamuel.hoplite:hoplite-yaml:2.9.0")
implementation("com.sksamuel.hoplite:hoplite-watch:2.9.0")

// Coroutines for async operations
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

### Test Dependencies
```kotlin
// Unit Testing
testImplementation("junit:junit:4.13.2")
testImplementation("io.mockk:mockk:1.13.8")

// Android Testing
testImplementation("androidx.test:core:1.5.0")
testImplementation("org.robolectric:robolectric:4.11.1")
```

## Build Configuration

### Android Library Setup
```kotlin
android {
    compileSdk = 34

    defaultConfig {
        minSdk = 28
        targetSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}
```

### Demo Application Setup
```kotlin
android {
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.voboost.config.demo"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}
```

## File System Integration

### Android File Access
- **Context.filesDir** - Private app directory for configuration files
- **Assets Integration** - Default configuration in app assets
- **File Copying** - Automatic asset-to-files copying on first run

### File Watching Implementation
```kotlin
// Hoplite ReloadableConfig with FileWatcher
val reloadable = ReloadableConfig(configLoader, Config::class)
    .addWatcher(FileWatcher(directory))
    .withErrorHandler { /* error handling */ }
```

## Configuration Format

### YAML Structure
```yaml
settings:
  language: "ru"              # String mapped to Language enum
  theme: "dark"               # String mapped to Theme enum
  interface-shift-x: 0        # Integer value
  interface-shift-y: 0        # Integer value

vehicle:
  fuel-mode: "electric"       # String mapped to FuelMode enum
  drive-mode: "sport"         # String mapped to DriveMode enum
```

### Enum Mapping Strategy
- **Direct Mapping** - Enum constant names match YAML values exactly
- **Lowercase Convention** - All enum constants use lowercase
- **No Annotations** - Hoplite handles mapping automatically

## Error Handling Strategy

### Result Pattern Implementation
```kotlin
sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val exception: Throwable) : Result<Nothing>()
}
```

### Error Categories
- **File Not Found** - Configuration file doesn't exist
- **Parse Errors** - Invalid YAML syntax or structure
- **Enum Mapping** - Invalid enum values in YAML
- **I/O Errors** - File system access problems

## Testing Strategy

### Test Categories
1. **Unit Tests** - Individual component testing with mocks
2. **Integration Tests** - Real file operations and YAML parsing
3. **File Watching Tests** - Actual file change detection with synchronization
4. **Error Handling Tests** - Comprehensive failure scenario coverage

### Test Utilities
```kotlin
// Base test class with common setup
abstract class BaseConfigTest {
    protected lateinit var mockContext: Context
    protected lateinit var configManager: ConfigManager

    @Before
    fun setUp() {
        mockContext = mockk()
        configManager = ConfigManager()
    }
}

// Real file watching test pattern
@Test
fun testRealFileWatching_detectsFileChanges() {
    val tempDir = File(Files.createTempDirectory("filewatch_test").toString())
    val configFile = File(tempDir, "config.yaml")

    // Create initial config and setup watching
    FileWriter(configFile).use { it.write(initialYaml) }
    every { mockContext.filesDir } returns tempDir

    // Use CountDownLatch for synchronization
    val latch = CountDownLatch(1)
    val listener = object : OnConfigChangeListener {
        override fun onConfigChanged(newConfig: Config, diff: Config) {
            // Verify changes and signal completion
            latch.countDown()
        }
    }

    // Start watching, modify file, wait for callback
    configManager.startWatching(mockContext, "config.yaml", listener)
    FileWriter(configFile).use { it.write(modifiedYaml) }
    val callbackReceived = latch.await(10, TimeUnit.SECONDS)
    assertTrue("File change callback should be received", callbackReceived)
}
```

## Performance Characteristics

### Memory Usage
- **Minimal State** - ConfigManager holds only essential references
- **Efficient Parsing** - Hoplite optimized YAML processing
- **Resource Cleanup** - Proper disposal of file watchers

### File Operations
- **Lazy Loading** - Configuration loaded only when requested
- **Change-based Updates** - File watching triggers only on actual changes
- **Non-blocking I/O** - Coroutine-based async operations

## Development Workflow

### Build Commands
```bash
# Build library (from voboost-config directory)
cd voboost-config
./gradlew build

# Run tests (from voboost-config directory)
cd voboost-config
./gradlew test

# Build demo app (from voboost-config-demo directory)
cd voboost-config-demo
./gradlew assembleDebug

# Install demo app (from voboost-config-demo directory)
cd voboost-config-demo
./gradlew installDebug
```

### Testing Commands
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew testDebugUnitTest --tests FileWatchingTest

# Generate test reports
./gradlew testDebugUnitTest --continue
```

## Deployment Considerations

### Library Distribution
- **AAR Format** - Standard Android library archive
- **Maven Compatible** - Can be published to Maven repositories
- **Dependency Management** - Transitive dependencies handled automatically

### Integration Requirements
- **Minimum API 28** - Android 9.0 and above
- **Kotlin Project** - Target applications must use Kotlin
- **File Permissions** - Uses app private directory (no special permissions)

## Code Quality Standards

### Coding Conventions
- **English Language** - All code, comments, and documentation
- **Blank Line Endings** - All source files end with empty line
- **KDoc Documentation** - Complete API documentation
- **Consistent Naming** - Clear, descriptive names throughout

### Quality Metrics
- **Test Coverage** - 100% of public API covered
- **No Warnings** - Clean compilation without warnings
- **Static Analysis** - Lint checks pass
- **Documentation** - Complete API and usage documentation

This technical foundation provides a robust, maintainable, and scalable configuration management solution for Android applications.