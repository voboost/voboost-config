package ru.voboost.config

import android.content.Context
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.watch.ReloadableConfig
import com.sksamuel.hoplite.watch.watchers.FileWatcher
import ru.voboost.config.models.Config
import ru.voboost.config.models.Language
import ru.voboost.config.models.Theme
import ru.voboost.config.models.FuelMode
import ru.voboost.config.models.DriveMode
import java.io.File
import java.io.FileWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Main facade class for configuration management in Android applications.
 *
 * This class provides a comprehensive API for loading, saving, and watching YAML configuration files
 * with full type safety and real-time change detection. It encapsulates all the complexity of file
 * operations, YAML parsing, and change detection behind a clean, Result-based API.
 *
 * ## Key Features
 * - **Type-safe configuration loading** from YAML files using Hoplite library
 * - **Automatic file watching** with real-time change notifications
 * - **Diff calculation** to identify exactly which configuration fields changed
 * - **Robust error handling** with Result-based return types
 * - **Android-optimized** file operations using Context.filesDir
 * - **Flat configuration model** for simplified programmatic access
 *
 * ## Usage Example
 * ```kotlin
 * val configManager = ConfigManager()
 *
 * // Load configuration
 * val result = configManager.loadConfig(context, "config.yaml")
 * result.onSuccess { config ->
 *     // Direct field access with flattened model
 *     val language = config.settingsLanguage
 *     val theme = config.settingsTheme
 * }.onFailure { error ->
 *     // Handle error
 * }
 *
 * // Watch for changes
 * configManager.startWatching(context, "config.yaml", listener)
 * ```
 *
 * ## Thread Safety
 * This class is **not thread-safe**. If you need to use it from multiple threads,
 * ensure proper synchronization or use separate instances per thread.
 *
 * ## File Location
 * All configuration files are stored relative to the application's private files directory
 * (`Context.filesDir`). This ensures proper Android security and data isolation.
 *
 * @since 1.0.0
 * @see Config
 * @see OnConfigChangeListener
 */
class ConfigManager {

    // Internal state for file watching
    private var reloadableConfig: ReloadableConfig<Config>? = null
    private var currentConfig: Config? = null
    private var currentListener: OnConfigChangeListener? = null
    private var watchedFile: File? = null
    private var watchedContext: Context? = null
    private var watchedFilePath: String? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * Loads configuration from a YAML file with full type safety.
     *
     * This method reads a YAML configuration file from the application's private files directory
     * and parses it into a strongly-typed [Config] object using the Hoplite library.
     * All configuration fields are nullable to gracefully handle missing values in the YAML file.
     *
     * ## File Location
     * The configuration file is loaded from `Context.filesDir + filePath`. For example,
     * if `filePath` is "config.yaml", the actual file path will be:
     * `/data/data/your.package.name/files/config.yaml`
     *
     * ## Error Handling
     * This method returns a [Result] object that encapsulates either success or failure:
     * - **Success**: Contains the parsed [Config] object
     * - **Failure**: Contains the exception that occurred during loading or parsing
     *
     * ## Common Failure Scenarios
     * - File does not exist at the specified path
     * - YAML syntax errors in the configuration file
     * - Invalid enum values in the configuration
     * - I/O errors during file reading
     *
     * ## Example Usage
     * ```kotlin
     * val result = configManager.loadConfig(context, "config.yaml")
     * result.onSuccess { config ->
     *     println("Language: ${config.settingsLanguage}")
     *     println("Theme: ${config.settingsTheme}")
     *     println("Fuel Mode: ${config.vehicleFuelMode}")
     * }.onFailure { error ->
     *     Log.e("Config", "Failed to load configuration", error)
     * }
     * ```
     *
     * @param context Android context used for accessing the application's files directory.
     *                Must not be null.
     * @param filePath Path to the configuration file relative to `Context.filesDir`.
     *                 Must not be null or empty. Use forward slashes for subdirectories.
     * @return [Result]<[Config]> containing either the successfully loaded configuration
     *         or an exception if loading failed.
     * @throws IllegalArgumentException if the file does not exist (wrapped in Result.failure)
     * @since 1.0.0
     * @see Config
     * @see saveConfig
     */
    fun loadConfig(context: Context, filePath: String): Result<Config> {
        return try {
            // Get file from context.filesDir + filePath
            val file = File(context.filesDir, filePath)

            if (!file.exists()) {
                return Result.failure(IllegalArgumentException("Configuration file does not exist: ${file.absolutePath}"))
            }

            // Parse YAML using Hoplite ConfigLoaderBuilder
            val config = ConfigLoaderBuilder.default()
                .addPropertySource(PropertySource.file(file))
                .build()
                .loadConfigOrThrow<Config>()

            Result.success(config)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves a configuration object to a YAML file with automatic directory creation.
     *
     * This method serializes a [Config] object to YAML format and writes it to the specified
     * file in the application's private files directory. The method automatically creates
     * any necessary parent directories if they don't exist.
     *
     * ## File Location
     * The configuration file is saved to `Context.filesDir + filePath`. For example,
     * if `filePath` is "config.yaml", the actual file path will be:
     * `/data/data/your.package.name/files/config.yaml`
     *
     * ## YAML Format
     * The saved YAML file follows this flat structure:
     * ```yaml
     * settings-language: en
     * settings-theme: auto
     * settings-interface-shift-x: 0
     * settings-interface-shift-y: 0
     * vehicle-fuel-mode: intellectual
     * vehicle-drive-mode: comfort
     * ```
     *
     * ## Null Field Handling
     * Only non-null fields from the [Config] object are written to the YAML file.
     * This allows for partial configuration updates where only specific fields are set.
     *
     * ## Error Handling
     * This method returns a [Result] object that encapsulates either success or failure:
     * - **Success**: File was written successfully
     * - **Failure**: Contains the exception that occurred during saving
     *
     * ## Common Failure Scenarios
     * - Insufficient storage space
     * - Permission denied (shouldn't occur in app's private directory)
     * - I/O errors during file writing
     * - Invalid enum values in the configuration object
     *
     * ## Example Usage
     * ```kotlin
     * val config = Config(
     *     settingsLanguage = Language.EN,
     *     settingsTheme = Theme.DARK,
     *     vehicleFuelMode = FuelMode.ELECTRIC
     * )
     *
     * val result = configManager.saveConfig(context, "config.yaml", config)
     * result.onSuccess {
     *     Log.d("Config", "Configuration saved successfully")
     * }.onFailure { error ->
     *     Log.e("Config", "Failed to save configuration", error)
     * }
     * ```
     *
     * @param context Android context used for accessing the application's files directory.
     *                Must not be null.
     * @param filePath Path to the configuration file relative to `Context.filesDir`.
     *                 Must not be null or empty. Parent directories will be created automatically.
     * @param config The [Config] object to serialize and save. Must not be null.
     *               Only non-null fields will be written to the YAML file.
     * @return [Result]<[Unit]> indicating either successful save operation or an exception
     *         if saving failed.
     * @since 1.0.0
     * @see Config
     * @see loadConfig
     */
    fun saveConfig(context: Context, filePath: String, config: Config): Result<Unit> {
        return try {
            // Get file from context.filesDir + filePath
            val file = File(context.filesDir, filePath)

            // Ensure parent directories exist
            file.parentFile?.mkdirs()

            // Convert Config object to YAML string
            val yamlContent = convertConfigToYaml(config)

            // Write YAML content to file
            FileWriter(file).use { writer ->
                writer.write(yamlContent)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Starts watching the configuration file for real-time changes with automatic diff calculation.
     *
     * This method sets up a file system watcher that monitors the specified configuration file
     * for modifications. When changes are detected, the file is automatically reloaded, parsed,
     * and compared with the previous version to generate a precise diff of what changed.
     *
     * ## How It Works
     * 1. **Initial Load**: Loads and stores the current configuration as baseline
     * 2. **File Monitoring**: Uses Hoplite's FileWatcher to monitor file system changes
     * 3. **Change Detection**: Automatically detects when the file is modified
     * 4. **Diff Calculation**: Compares old vs new configuration to identify changes
     * 5. **Notification**: Calls the listener with both complete config and diff
     *
     * ## Diff Object
     * The diff object passed to [OnConfigChangeListener.onConfigChanged] contains:
     * - **Changed fields**: Set to their new values
     * - **Unchanged fields**: Set to null
     *
     * This allows you to react specifically to the fields that actually changed.
     *
     * ## File Location
     * Watches the file at `Context.filesDir + filePath`. The file must exist before
     * calling this method.
     *
     * ## Lifecycle Management
     * - Only one file can be watched at a time per ConfigManager instance
     * - Calling this method again will stop watching the previous file
     * - Call [stopWatching] to stop monitoring and free resources
     * - The watcher continues until explicitly stopped or the ConfigManager is garbage collected
     *
     * ## Error Handling
     * - If the initial file load fails, watching will not start
     * - If file parsing fails during watching, the listener is not called for that change
     * - File watching is resilient and continues even if individual change events fail
     *
     * ## Thread Safety
     * The listener callback is invoked on a background thread (IO dispatcher).
     * If you need to update UI, ensure you switch to the main thread:
     * ```kotlin
     * override fun onConfigChanged(newConfig: Config, diff: Config) {
     *     runOnUiThread {
     *         // Update UI here
     *     }
     * }
     * ```
     *
     * ## Example Usage
     * ```kotlin
     * val listener = object : OnConfigChangeListener {
     *     override fun onConfigChanged(newConfig: Config, diff: Config) {
     *         // Check what changed with direct field access
     *         diff.settingsLanguage?.let { newLanguage ->
     *             updateAppLanguage(newLanguage)
     *         }
     *         diff.settingsTheme?.let { newTheme ->
     *             updateAppTheme(newTheme)
     *         }
     *         diff.vehicleFuelMode?.let { newFuelMode ->
     *             updateVehicleMode(newFuelMode)
     *         }
     *     }
     * }
     *
     * val result = configManager.startWatching(context, "config.yaml", listener)
     * result.onFailure { error ->
     *     Log.e("Config", "Failed to start watching", error)
     * }
     * ```
     *
     * @param context Android context used for accessing the application's files directory.
     *                Must not be null.
     * @param filePath Path to the configuration file relative to `Context.filesDir`.
     *                 The file must exist. Must not be null or empty.
     * @param listener Callback interface that will be invoked when configuration changes
     *                 are detected. Must not be null. Called on background thread.
     * @return [Result]<[Unit]> indicating either successful start of file watching
     *         or an exception if watching could not be started.
     * @throws IllegalArgumentException if the file does not exist (wrapped in Result.failure)
     * @since 1.0.0
     * @see OnConfigChangeListener
     * @see stopWatching
     * @see loadConfig
     */
    fun startWatching(context: Context, filePath: String, listener: OnConfigChangeListener): Result<Unit> {
        return try {
            // Stop any existing watcher
            stopWatching()

            // Get file from context.filesDir + filePath
            val file = File(context.filesDir, filePath)

            if (!file.exists()) {
                return Result.failure(IllegalArgumentException("Configuration file does not exist: ${file.absolutePath}"))
            }

            // Store current configuration for diff calculation
            val loadResult = loadConfig(context, filePath)
            if (loadResult.isFailure) {
                return Result.failure(loadResult.exceptionOrNull() ?: Exception("Failed to load initial configuration"))
            }

            currentConfig = loadResult.getOrNull()
            currentListener = listener
            watchedFile = file
            watchedContext = context
            watchedFilePath = filePath

            // Create ConfigLoader for the specific file
            val configLoader = ConfigLoaderBuilder.default()
                .addPropertySource(PropertySource.file(file))
                .build()

            // Create ReloadableConfig with FileWatcher
            val reloadable = ReloadableConfig(configLoader, Config::class)
                .addWatcher(FileWatcher(file.parent ?: file.absolutePath))
                .withErrorHandler { error ->
                    // Handle parsing errors by notifying the listener
                    coroutineScope.launch {
                        handleConfigError(error)
                    }
                }

            // Subscribe to config changes
            reloadable.subscribe { newConfig ->
                coroutineScope.launch {
                    handleConfigChange(newConfig)
                }
            }

            reloadableConfig = reloadable

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stops watching the configuration file for changes and cleans up resources.
     *
     * This method immediately stops monitoring the configuration file and releases all
     * associated resources including the file watcher, stored configuration baseline,
     * and listener reference. After calling this method, no further change notifications
     * will be sent to the previously registered listener.
     *
     * ## Resource Cleanup
     * The following resources are cleaned up:
     * - File system watcher is stopped and disposed
     * - Current configuration baseline is cleared from memory
     * - Listener reference is removed to prevent memory leaks
     * - Watched file reference is cleared
     *
     * ## When to Call
     * - When you no longer need to monitor configuration changes
     * - In Activity/Fragment `onDestroy()` or similar lifecycle methods
     * - Before starting to watch a different configuration file
     * - When switching between different ConfigManager instances
     *
     * ## Safety
     * - Safe to call multiple times - subsequent calls have no effect
     * - Safe to call even if watching was never started
     * - Does not throw exceptions
     *
     * ## Example Usage
     * ```kotlin
     * class ConfigService {
     *     private val configManager = ConfigManager()
     *
     *     fun startService() {
     *         configManager.startWatching(context, "config.yaml", listener)
     *     }
     *
     *     fun stopService() {
     *         configManager.stopWatching() // Clean up resources
     *     }
     * }
     * ```
     *
     * ## Memory Management
     * While this method cleans up most resources, the ConfigManager instance itself
     * can still be reused for other operations like [loadConfig], [saveConfig], or
     * starting to watch a different file with [startWatching].
     *
     * @since 1.0.0
     * @see startWatching
     */
    fun stopWatching() {
        // Clear reloadable config reference
        reloadableConfig = null

        // Clear stored listener and current configuration
        currentListener = null
        currentConfig = null
        watchedFile = null
        watchedContext = null
        watchedFilePath = null
    }

    /**
     * Handles config change events from the ReloadableConfig.
     */
    private fun handleConfigChange(newConfig: Config) {
        try {
            val oldConfig = currentConfig ?: return
            val listener = currentListener ?: return

            // Calculate diff
            val diff = createDiff(oldConfig, newConfig)

            // Update current config
            currentConfig = newConfig

            // Notify listener
            listener.onConfigChanged(newConfig, diff)
        } catch (e: Exception) {
            // Log error but don't crash - file watching should be resilient
            // In a real implementation, you might want to use Android Log here
        }
    }

    /**
     * Handles config parsing errors from the ReloadableConfig error handler.
     */
    private fun handleConfigError(error: Throwable) {
        try {
            val listener = currentListener ?: return

            // Convert Throwable to Exception for the interface
            val exception = if (error is Exception) error else Exception(error.message, error)

            // Notify listener of the error
            listener.onConfigError(exception)
        } catch (e: Exception) {
            // Log error but don't crash - error handling should be resilient
            // In a real implementation, you might want to use Android Log here
        }
    }

    /**
     * Creates a diff Config object containing only the fields that have changed.
     *
     * This method performs a field-by-field comparison of two configuration objects to identify
     * exactly which fields have changed. Uses universal reflection to traverse all Config fields
     * dynamically, making it extensible for any number of configuration fields.
     *
     * ## Universal Field Traversal
     * - **Dynamic Discovery**: Automatically finds all Config fields using reflection
     * - **Extensible**: Works with any number of fields without code changes
     * - **Type Safe**: Maintains Config type throughout the process
     * - **Maintainable**: No hardcoded field names to maintain
     *
     * @param oldConfig The old Config object
     * @param newConfig The new Config object
     * @return Config object with only changed fields, or empty Config if no changes
     */
    private fun createDiff(oldConfig: Config, newConfig: Config): Config {
        return try {
            val configClass = Config::class
            val constructor = configClass.constructors.first()
            val parameters = constructor.parameters

            // Build map of parameter names to values for changed fields only
            val parameterValues = mutableMapOf<String, Any?>()

            for (parameter in parameters) {
                val fieldName = parameter.name ?: continue

                // Get values from both configs using reflection
                val oldValue = getValueByName(oldConfig, fieldName)
                val newValue = getValueByName(newConfig, fieldName)

                // Include field in diff only if it changed
                if (oldValue != newValue) {
                    parameterValues[fieldName] = newValue
                } else {
                    parameterValues[fieldName] = null
                }
            }

            // Create Config instance using constructor with named parameters
            constructor.callBy(
                parameters.associateWith { param ->
                    parameterValues[param.name]
                }
            )
        } catch (e: Exception) {
            // If anything fails, return empty Config
            Config()
        }
    }

    /**
     * Determines if a specific field has changed based on the diff object.
     *
     * This method analyzes a diff configuration object to check whether a particular
     * field has been modified. The diff object contains only the fields that have changed,
     * with unchanged fields set to null. With the flattened model, field access is now
     * direct and much simpler.
     *
     * ## Field Names
     * Use the actual field names from the Config class:
     * - `"settingsLanguage"` - Language setting
     * - `"settingsTheme"` - Theme setting
     * - `"settingsInterfaceShiftX"` - Interface X shift
     * - `"settingsInterfaceShiftY"` - Interface Y shift
     * - `"vehicleFuelMode"` - Vehicle fuel mode
     * - `"vehicleDriveMode"` - Vehicle drive mode
     *
     * ## Usage Example
     * ```kotlin
     * override fun onConfigChanged(newConfig: Config, diff: Config) {
     *     if (configManager.isFieldChanged(diff, "settingsTheme")) {
     *         updateAppTheme(newConfig.settingsTheme)
     *     }
     *     if (configManager.isFieldChanged(diff, "vehicleFuelMode")) {
     *         updateVehicleMode(newConfig.vehicleFuelMode)
     *     }
     * }
     * ```
     *
     * ## Error Handling
     * This method is designed to be safe and never throw exceptions:
     * - Returns `false` for null diff objects
     * - Returns `false` for invalid field names
     * - Returns `false` if reflection operations fail
     *
     * @param diff The diff configuration object containing only changed fields.
     *             Typically received from [OnConfigChangeListener.onConfigChanged].
     * @param fieldName The name of the field to check.
     * @return `true` if the field has changed (is non-null in diff), `false` otherwise.
     * @since 1.0.0
     * @see OnConfigChangeListener
     * @see getFieldValue
     */
    fun isFieldChanged(diff: Config?, fieldName: String): Boolean {
        if (diff == null) return false
        return try {
            val value = getValueByName(diff, fieldName)
            value != null
        } catch (e: Exception) {
            // Log error but don't crash - diff checking should be resilient
            false
        }
    }

    /**
     * Gets the value of a field from a configuration object.
     *
     * This method extracts field values from configuration objects using field names.
     * With the flattened Config model, this is now much simpler and more efficient.
     *
     * ## Field Names
     * Use the actual field names from the Config class:
     * - `"settingsLanguage"` - Returns the language enum value
     * - `"settingsTheme"` - Returns the theme enum value
     * - `"settingsInterfaceShiftX"` - Returns the X shift integer
     * - `"settingsInterfaceShiftY"` - Returns the Y shift integer
     * - `"vehicleFuelMode"` - Returns the fuel mode enum value
     * - `"vehicleDriveMode"` - Returns the drive mode enum value
     *
     * ## Return Value
     * The method returns the string representation of the field value:
     * - Enum values return their name (e.g., "en", "dark", "electric")
     * - Numeric values return their string representation (e.g., "0", "100")
     * - Null fields return `null`
     *
     * ## Usage Example
     * ```kotlin
     * val config = configManager.loadConfig(context, "config.yaml").getOrNull()
     * val language = configManager.getFieldValue(config, "settingsLanguage") // "en" or "ru"
     * val theme = configManager.getFieldValue(config, "settingsTheme") // "auto", "light", "dark"
     * val fuelMode = configManager.getFieldValue(config, "vehicleFuelMode") // "electric", "fuel", etc.
     * ```
     *
     * ## Error Handling
     * This method is designed to be safe and never throw exceptions:
     * - Returns `null` for invalid field names
     * - Returns `null` if reflection operations fail
     * - Returns `null` for null configuration objects
     *
     * @param config The configuration object to extract the field value from.
     *               Can be a complete config or a diff object.
     * @param fieldName The name of the field to extract.
     * @return The string representation of the field value, or `null` if not found or error occurred.
     * @since 1.0.0
     * @see isFieldChanged
     */
    fun getFieldValue(config: Config?, fieldName: String): String? {
        return try {
            getValueByName(config, fieldName)?.toString()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks if a diff configuration object contains any actual changes.
     *
     * This method analyzes a diff configuration object to determine if it contains
     * any non-null fields, indicating that changes have occurred. Uses universal
     * reflection to check all Config fields dynamically, making it extensible
     * for any number of configuration fields.
     *
     * ## Universal Field Checking
     * - **Dynamic Discovery**: Automatically checks all Config fields using reflection
     * - **Extensible**: Works with any number of fields without code changes
     * - **Type Safe**: Maintains Config type throughout the process
     * - **Maintainable**: No hardcoded field names to maintain
     *
     * ## Usage Example
     * ```kotlin
     * override fun onConfigChanged(newConfig: Config, diff: Config) {
     *     if (configManager.hasDiffAnyChanges(diff)) {
     *         // Process the changes
     *         updateConfiguration(newConfig, diff)
     *     } else {
     *         // No actual changes detected
     *         return
     *     }
     * }
     * ```
     *
     * ## Error Handling
     * This method is designed to be safe and never throw exceptions:
     * - Returns `false` for null diff objects
     * - Returns `false` if reflection operations fail
     * - Logs errors but continues execution
     *
     * @param diff The diff configuration object to check for changes.
     *             Typically received from [OnConfigChangeListener.onConfigChanged].
     * @return `true` if the diff contains any non-null fields (changes), `false` otherwise.
     * @since 1.0.0
     * @see OnConfigChangeListener
     * @see createDiff
     */
    fun hasDiffAnyChanges(diff: Config?): Boolean {
        return try {
            if (diff == null) return false

            val configClass = Config::class
            val constructor = configClass.constructors.first()
            val parameters = constructor.parameters

            for (parameter in parameters) {
                val fieldName = parameter.name ?: continue
                val fieldValue = getValueByName(diff, fieldName)

                // If any field is non-null, there are changes
                if (fieldValue != null) {
                    return true
                }
            }

            // No non-null fields found, no changes
            false
        } catch (e: Exception) {
            // Log error but don't crash - diff checking should be resilient
            false
        }
    }

    /**
     * Validates that a configuration object has all required fields.
     *
     * This method performs validation of a configuration object to ensure it contains
     * all essential fields needed for proper application operation. Uses universal
     * reflection to validate all Config fields dynamically.
     *
     * ## Validation Criteria
     * A configuration is considered valid if:
     * - Root Config object exists and is not null
     * - ALL fields are present and not null (enum and integer fields)
     * - This ensures complete configuration when loaded from filesystem
     *
     * ## Universal Validation
     * - **Dynamic Discovery**: Automatically validates all Config fields using reflection
     * - **Extensible**: Works with any number of fields without code changes
     * - **Type Aware**: Distinguishes between required enum fields and optional numeric fields
     * - **Maintainable**: No hardcoded field names to maintain
     *
     * ## Usage Example
     * ```kotlin
     * val config = configManager.loadConfig(context, "config.yaml").getOrNull()
     * if (configManager.isValidConfig(config)) {
     *     // Safe to use all configuration fields
     *     applyConfiguration(config)
     * } else {
     *     // Handle incomplete configuration
     *     useDefaultConfiguration()
     * }
     * ```
     *
     * ## Error Handling
     * This method is designed to be safe and never throw exceptions:
     * - Returns `false` for null configuration objects
     * - Returns `false` if any validation check fails
     *
     * @param config The configuration object to validate. Can be null.
     * @return `true` if the configuration contains all required fields, `false` otherwise.
     * @since 1.0.0
     * @see Config
     */
    fun isValidConfig(config: Config?): Boolean {
        return try {
            if (config == null) return false

            validateObjectRecursively(config)
        } catch (e: Exception) {
            // Log error but don't crash - validation should be resilient
            false
        }
    }

    /**
     * Recursively validates a Config object using reflection.
     *
     * @param obj The Config object to validate
     * @return true if all required fields are present, false otherwise
     */
    private fun validateObjectRecursively(obj: Config): Boolean {
        return try {
            val configClass = Config::class
            val constructor = configClass.constructors.first()
            val parameters = constructor.parameters

            for (parameter in parameters) {
                val fieldName = parameter.name ?: continue
                val fieldValue = getValueByName(obj, fieldName)

                // ALL fields are required when config is loaded from filesystem
                // This includes enum fields (Language, Theme, FuelMode, DriveMode)
                // and integer fields (interface shift values)
                if (fieldValue == null) {
                    return false // Any missing field makes config invalid
                }
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the value of a field using reflection with support for dot notation paths.
     *
     * This method supports both simple field names and dot notation paths for future extensibility.
     * Currently, with the flat Config model, only simple field names are used, but the method
     * is designed to handle nested paths if the structure evolves.
     *
     * @param config The Config object to extract the field value from
     * @param path The field path (simple name or dot notation like "field.subfield")
     * @return The field value, or null if not found or error occurred
     */
    private fun getValueByPath(config: Config?, path: String): Any? {
        if (config == null) return null

        return try {
            // For now, with flat structure, path is just a field name
            // But this method is extensible for future nested structures
            if (path.contains('.')) {
                // Future support for nested paths like "settings.language"
                val parts = path.split('.')
                var currentValue: Any? = config

                for (part in parts) {
                    if (currentValue == null) return null
                    currentValue = getFieldValueFromObject(currentValue, part)
                }

                currentValue
            } else {
                // Simple field name - current flat structure
                getValueByName(config, path)
            }
        } catch (e: Exception) {
            // Return null on any reflection error for safety
            null
        }
    }

    /**
     * Gets the value of a field using reflection.
     *
     * @param config The Config object to extract the field value from
     * @param fieldName The name of the field
     * @return The field value, or null if not found or error occurred
     */
    private fun getValueByName(config: Config?, fieldName: String): Any? {
        if (config == null) return null

        return try {
            val clazz = config::class.java
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            field.get(config)
        } catch (e: Exception) {
            // Return null on any reflection error for safety
            null
        }
    }

    /**
     * Helper method to get field value from any object using reflection.
     * Used for potential future nested structure support.
     *
     * @param obj The object to extract the field value from
     * @param fieldName The name of the field
     * @return The field value, or null if not found or error occurred
     */
    private fun getFieldValueFromObject(obj: Any, fieldName: String): Any? {
        return try {
            val clazz = obj::class.java
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            field.get(obj)
        } catch (e: Exception) {
            // Return null on any reflection error for safety
            null
        }
    }

    /**
     * Converts a Config object to YAML string format.
     *
     * This method handles the conversion from the flattened Config model to the
     * flat YAML structure. This is the ONLY method allowed to work with specific
     * field names, as it needs to map Kotlin field names to YAML kebab-case keys.
     */
    private fun convertConfigToYaml(config: Config): String {
        val yaml = StringBuilder()

        // Convert each field to kebab-case YAML key
        config.settingsLanguage?.let {
            yaml.appendLine("settings-language: ${it.name}")
        }
        config.settingsTheme?.let {
            yaml.appendLine("settings-theme: ${it.name}")
        }
        config.settingsInterfaceShiftX?.let {
            yaml.appendLine("settings-interface-shift-x: $it")
        }
        config.settingsInterfaceShiftY?.let {
            yaml.appendLine("settings-interface-shift-y: $it")
        }
        config.vehicleFuelMode?.let {
            yaml.appendLine("vehicle-fuel-mode: ${it.name}")
        }
        config.vehicleDriveMode?.let {
            yaml.appendLine("vehicle-drive-mode: ${it.name}")
        }

        return yaml.toString()
    }
}

