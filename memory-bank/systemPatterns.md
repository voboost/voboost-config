# System Architecture: voboost-config

## Overview

voboost-config implements a clean, layered architecture for Android configuration management with real-time file watching capabilities.

## Architectural Patterns

### 1. Facade Pattern
**ConfigManager** serves as the main facade providing a simple 4-method API:
- `loadConfig()` - Load YAML to type-safe objects
- `saveConfig()` - Save objects to YAML
- `startWatching()` - Begin real-time file monitoring
- `stopWatching()` - Stop monitoring and cleanup

### 2. Observer Pattern
**OnConfigChangeListener** enables reactive programming:
- Callbacks when configuration changes
- Provides both new config and diff
- Decoupled notification system

### 3. Result Pattern
**Consistent Error Handling** throughout the API:
- `Result<T>` return types prevent uncaught exceptions
- Detailed error messages for debugging
- Graceful failure handling

### 4. Builder Pattern
**Hoplite ConfigLoaderBuilder** for flexible configuration:
- Property source configuration
- Custom parsing options
- Extensible design

## Core Components

### Data Layer
```
Config (root)
├── Settings
│   ├── Language enum
│   ├── Theme enum
│   ├── interfaceShiftX: Int
│   └── interfaceShiftY: Int
└── Vehicle
    ├── FuelMode enum
    └── DriveMode enum
```

### Service Layer
- **ConfigManager** - Main service facade
- **ReloadableConfig** - File watching orchestrator
- **FileWatcher** - File system monitoring

### Integration Layer
- **Hoplite** - YAML parsing and serialization
- **Android Context** - File system access
- **Coroutines** - Non-blocking operations

## File Watching Architecture

### Components
1. **ReloadableConfig** - Orchestrates watching and reloading
2. **FileWatcher** - Monitors directory for changes
3. **ConfigLoader** - Handles YAML parsing
4. **Subscriber** - Receives change notifications

### Implementation Details
```kotlin
// Correct Hoplite ReloadableConfig pattern
val configLoader = ConfigLoaderBuilder.default()
    .addPropertySource(PropertySource.file(file))
    .build()

val reloadable = ReloadableConfig(configLoader, Config::class)
    .addWatcher(FileWatcher(file.parent ?: file.absolutePath))
    .withErrorHandler { _ -> /* error handling */ }

reloadable.subscribe { newConfig ->
    coroutineScope.launch {
        handleConfigChange(newConfig)
    }
}
```

### Flow
```
File Change → FileWatcher → ReloadableConfig → ConfigLoader → Parse → Diff → Callback
```

### Error Handling
- **Resilient Design** - Continues working despite individual failures
- **Error Handler** - Configurable error processing
- **Graceful Degradation** - Non-critical errors don't stop watching
- **Coroutine Safety** - Non-blocking error handling

## Configuration Model

### Enum Strategy
- **Lowercase Constants** - Match YAML values exactly
- **No Annotations** - Direct string-to-enum mapping
- **Automatic Serialization** - Uses `enum.name`

### Nullable Design
- **Optional Fields** - All config properties nullable
- **Partial Configurations** - Handle missing sections gracefully
- **Default Handling** - Application-level defaults

## Testing Architecture

### Test Organization
- **BaseConfigTest** - Common setup and utilities
- **Functional Tests** - Test specific functionality areas
- **Integration Tests** - Real file operations
- **Real-time Tests** - Actual file watching with synchronization

### Test Patterns
- **Mock Context** - Isolated Android dependencies
- **Temporary Files** - Real file operations in tests
- **CountDownLatch** - Synchronization for async operations
- **Result Validation** - Comprehensive error checking

## Performance Considerations

### Memory Management
- **Stateless Design** - ConfigManager holds minimal state
- **Resource Cleanup** - Proper watcher disposal
- **Efficient Parsing** - Hoplite optimized YAML processing

### File Operations
- **Android Optimized** - Uses Context.filesDir
- **Non-blocking** - Coroutine-based change handling
- **Minimal I/O** - Only reads when changes detected

## Security & Reliability

### File Access
- **Private Directory** - Uses app's private files directory
- **No External Storage** - Avoids permission requirements
- **Secure by Default** - Android app isolation

### Error Resilience
- **No Crashes** - All public methods return Result
- **Detailed Errors** - Comprehensive error messages
- **Graceful Degradation** - Continues working with partial failures

## Extension Points

### Custom Watchers
- **Watchable Interface** - Add custom monitoring sources
- **Multiple Watchers** - Support for various change triggers
- **Configurable Intervals** - Time-based watching options

### Custom Parsing
- **PropertySource** - Support different configuration sources
- **Custom Loaders** - Extend parsing capabilities
- **Validation** - Add configuration validation layers

## Reflection and Field Access Architecture

### Universal Reflection Pattern
All ConfigManager methods use recursive reflection instead of direct field access for maximum extensibility:

- **createDiff()** - Recursive object traversal for universal field comparison
- **isFieldChanged()** - Recursive reflection through getValueByPath
- **isValidConfig()** - Recursive validation through validateObjectRecursively
- **getValueByPath()** - Universal field access with Config type safety

### Type Safety Rules
- **Config Type Only** - Never use Any type, always use Config for type safety
- **Direct Access Exception** - Only convertConfigToYaml uses direct field access for clean YAML output
- **Universal Methods** - All reflection methods work with any number of fields
- **Extensible Design** - Automatically handles new fields without code changes

### Recursive Implementation Benefits
- **Universal** - Works with any configuration structure without hardcoded logic
- **Extensible** - Automatically supports new fields and nested structures
- **Type-safe** - Maintains Config type throughout reflection operations
- **Robust** - Handles complex nested field access dynamically
- **Maintainable** - Single implementation works for any field count

## Design Principles

1. **Simplicity** - Easy to use API with sensible defaults
2. **Type Safety** - Compile-time checks prevent runtime errors
3. **Reliability** - Robust error handling and resource management
4. **Performance** - Efficient file operations and memory usage
5. **Testability** - Comprehensive test coverage and mockable design
6. **Extensibility** - Clear extension points for customization
7. **Universal Reflection** - Recursive field access for maximum maintainability

This architecture provides a solid foundation for configuration management in Android applications while maintaining simplicity, reliability, and universal extensibility through recursive reflection patterns.

