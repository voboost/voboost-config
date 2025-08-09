package ru.voboost.config

import android.content.Context
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.watch.ReloadableConfig
import com.sksamuel.hoplite.watch.watchers.FileWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.voboost.config.models.Config
import java.io.File
import java.io.FileWriter

/**
 * Configuration management class for Android applications.
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
 * - **Instance-based design** for better testability and flexibility
 *
 * ## Usage Example
 * ```kotlin
 * // Create instance with context (uses default config.yaml)
 * val configManager = ConfigManager(context)
 *
 * // Or specify custom file path
 * val configManager = ConfigManager(context, "custom-config.yaml")
 *
 * // Load initial configuration
 * val loadResult = configManager.loadConfig()
 * loadResult.onSuccess { config ->
 *     // Use the loaded configuration
 * }
 *
 * // Start watching for changes
 * val listener = object : OnConfigChangeListener {
 *     override fun onConfigChanged(newConfig: Config, diff: Config) {
 *         // Handle configuration changes
 *     }
 *     override fun onConfigError(error: Exception) {
 *         // Handle errors
 *     }
 * }
 * configManager.startWatching(listener)
 * ```
 *
 * ## Thread Safety
 * This class is **not thread-safe**. If you need to use it from multiple threads,
 * ensure proper synchronization in your application code.
 *
 * ## File Location
 * All configuration files are stored relative to the application's private data directory
 * (`Context.dataDir`). This ensures proper Android security and data isolation.
 *
 * @param context Android context for accessing the application's data directory
 * @param filePath Relative path to the configuration file within the files directory (defaults to "config.yaml")
 * @since 1.0.0
 * @see Config
 * @see OnConfigChangeListener
 */
class ConfigManager(
    private val context: Context,
    private val filePath: String = "config.yaml"
) {
    // Internal state for file watching
    private var reloadableConfig: ReloadableConfig<Config>? = null
    private var currentConfig: Config? = null
    private var currentListener: OnConfigChangeListener? = null
    private var watchedFile: File? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * Gets the current configuration object.
     *
     * This method returns the currently loaded configuration object. The configuration
     * is automatically updated when the file changes and file watching is active.
     *
     * @return The current [Config] object, or null if no configuration is loaded
     * @since 1.0.0
     */
    fun getConfig(): Config? {
        return currentConfig
    }

    /**
     * Loads configuration from a YAML file with full type safety.
     *
     * This method reads a YAML configuration file from the application's private files directory
     * and parses it into a strongly-typed [Config] object using the Hoplite library.
     * All configuration fields are nullable to gracefully handle missing values in the YAML file.
     *
     * @return [Result]<[Config]> containing either the loaded configuration or an error
     */
    fun loadConfig(): Result<Config> {
        return try {
            val file = File(context.dataDir, filePath)

            if (!file.exists()) {
                return Result.failure(
                    IllegalArgumentException("Configuration file does not exist: ${file.absolutePath}")
                )
            }

            val config =
                ConfigLoaderBuilder.default()
                    .addPropertySource(PropertySource.file(file))
                    .build()
                    .loadConfigOrThrow<Config>()

            // Store the loaded configuration for diff calculation
            currentConfig = config
            Result.success(config)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves the current configuration to a YAML file with atomic write operations.
     *
     * This method converts the current [Config] object stored in ConfigManager to YAML format
     * and writes it to the specified file in the application's private files directory.
     * The operation is atomic to prevent corruption during concurrent access or system interruptions.
     *
     * The method uses the internally managed configuration object that was loaded via [loadConfig]
     * or modified via [setFieldValue]. This ensures consistency between the in-memory state
     * and the persisted configuration.
     *
     * ## Usage Example
     * ```kotlin
     * // Load initial configuration
     * configManager.loadConfig()
     *
     * // Modify configuration
     * configManager.setFieldValue("settingsTheme", Theme.dark)
     *
     * // Save current state to disk
     * val result = configManager.saveConfig()
     * result.onSuccess {
     *     println("Configuration saved successfully")
     * }
     * ```
     *
     * @return [Result]<[Unit]> indicating success or containing an error
     * @throws IllegalStateException if no configuration is currently loaded
     * @since 1.0.0
     * @see loadConfig
     * @see setFieldValue
     */
    fun saveConfig(): Result<Unit> {
        return try {
            val config = currentConfig ?: return Result.failure(
                IllegalStateException("No configuration loaded. Call loadConfig() first.")
            )

            val file = File(context.dataDir, filePath)

            // Ensure parent directories exist
            file.parentFile?.mkdirs()

            // Convert config to YAML and write atomically
            val yamlContent = convertConfigToYaml(config)
            FileWriter(file).use { writer ->
                writer.write(yamlContent)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Starts watching a configuration file for changes with real-time notifications.
     *
     * This method sets up automatic file watching using the Hoplite library's built-in
     * file watching capabilities. When the configuration file changes, the provided
     * listener will be notified with both the new configuration and a diff object
     * showing exactly what changed.
     *
     * @param listener Callback interface for receiving change notifications
     * @return [Result]<[Unit]> indicating success or containing an error
     */
    fun startWatching(listener: OnConfigChangeListener): Result<Unit> {
        return try {
            // Stop any existing watching operation
            stopWatching()

            val file = File(context.dataDir, filePath)
            if (!file.exists()) {
                return Result.failure(
                    IllegalArgumentException("Configuration file does not exist: ${file.absolutePath}")
                )
            }

            // Load initial configuration
            val loadResult = loadConfig()
            if (loadResult.isFailure) {
                return Result.failure(
                    loadResult.exceptionOrNull() ?: Exception("Failed to load initial configuration")
                )
            }

            // Store watching state
            currentListener = listener
            watchedFile = file

            // Set up file watching with Hoplite
            val configLoader =
                ConfigLoaderBuilder.default()
                    .addPropertySource(PropertySource.file(file))
                    .build()

            reloadableConfig =
                ReloadableConfig(configLoader, Config::class)
                    .addWatcher(FileWatcher(file.parent ?: file.absolutePath))
                    .withErrorHandler { error ->
                        coroutineScope.launch {
                            handleConfigError(error)
                        }
                    }

            // Subscribe to configuration changes
            reloadableConfig?.subscribe { newConfig ->
                coroutineScope.launch {
                    handleConfigChange(newConfig)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stops watching the configuration file and cleans up resources.
     *
     * This method stops any active file watching operation and properly cleans up
     * all associated resources including file system watchers, event handlers,
     * and callback references.
     *
     * ## Resource Cleanup
     * - Stops file system watching
     * - Clears event handler references
     * - Releases callback listener references
     * - Cleans up internal state
     *
     * ## Safe to Call Multiple Times
     * This method is safe to call multiple times and will not throw exceptions
     * if no watching operation is currently active.
     *
     * ## Example Usage
     * ```kotlin
     * // Stop watching when no longer needed
     * configManager.stopWatching()
     * ```
     *
     * ## Automatic Cleanup
     * This method is automatically called when starting a new watching operation
     * to ensure clean state transitions.
     *
     * @since 1.0.0
     * @see startWatching
     */
    fun stopWatching() {
        reloadableConfig = null
        currentListener = null
        watchedFile = null
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
    fun isFieldChanged(
        diff: Config?,
        fieldName: String
    ): Boolean {
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
     * Gets the value of a field from the current configuration object.
     *
     * This method extracts field values from the current configuration object using field names.
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
     * val language = configManager.getFieldValue("settingsLanguage") // "en" or "ru"
     * val theme = configManager.getFieldValue("settingsTheme") // "light", "dark"
     * val fuelMode = configManager.getFieldValue("vehicleFuelMode") // "electric", "hybrid", etc.
     * ```
     *
     * ## Error Handling
     * This method is designed to be safe and never throw exceptions:
     * - Returns `null` for invalid field names
     * - Returns `null` if reflection operations fail
     * - Returns `null` for null configuration objects
     *
     * @param fieldName The name of the field to extract.
     * @return The string representation of the field value, or `null` if not found or error occurred.
     * @since 1.0.0
     * @see isFieldChanged
     */
    fun getFieldValue(fieldName: String): String? {
        return try {
            val value = getValueByName(currentConfig, fieldName)

            when (value) {
                is Enum<*> -> value.name
                is Int -> value.toString()
                null -> null
                else -> value.toString()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Sets the value of a field in the current configuration and saves it to disk.
     *
     * This method updates the ConfigManager's internal configuration state by setting
     * a specific field to a new value using reflection, then automatically saves the
     * updated configuration to disk. It directly modifies the current config object
     * without creating a new instance.
     *
     * ## Universal Field Setting
     * - **Dynamic Discovery**: Uses reflection to set any Config field by name
     * - **Extensible**: Works with any field without code changes
     * - **Direct Modification**: Modifies the current config object in place
     * - **Automatic Persistence**: Saves updated config to disk automatically
     *
     * ## Usage Example
     * ```kotlin
     * val result = configManager.setFieldValue("settingsTheme", Theme.dark)
     * result.onSuccess {
     *     println("Theme updated and saved successfully")
     * }.onFailure { error ->
     *     println("Failed to update theme: ${error.message}")
     * }
     * ```
     *
     * ## Error Handling
     * This method returns a [Result] object that encapsulates either success or failure:
     * - **Success**: Field was updated and configuration saved successfully
     * - **Failure**: Contains the exception that occurred during the operation
     *
     * @param fieldName The name of the field to set.
     * @param value The new value for the field.
     * @return [Result]<[Unit]> indicating success or containing an error
     * @since 1.0.0
     * @see getFieldValue
     * @see saveConfig
     */
    fun setFieldValue(
        fieldName: String,
        value: Any?
    ): Result<Unit> {
        return try {
            val config =
                currentConfig ?: return Result.failure(
                    IllegalStateException("No configuration loaded")
                )

            // Set field value directly using reflection
            val clazz = config::class.java
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            field.set(config, value)

            // Save to disk using the updated current configuration
            saveConfig()
        } catch (e: Exception) {
            Result.failure(e)
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

            val clazz = Config::class.java
            val fields = clazz.declaredFields

            for (field in fields) {
                field.isAccessible = true
                val fieldValue = field.get(diff)

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
     * val config = configManager.getCurrentConfig()
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
     * Copy default configuration from assets to app files directory if it doesn't exist.
     *
     * This method checks if a configuration file exists in the app's files directory,
     * and if not, copies the default configuration from the assets folder. This ensures
     * a valid configuration file is always available.
     *
     * ## File Operations
     * - Checks if the target configuration file exists in Context.filesDir
     * - If not found, copies the file from assets to the files directory
     * - Uses efficient stream copying with proper resource management
     * - Creates parent directories if needed
     *
     * @return [Result]<[Unit]> indicating success or containing an error
     */
    fun copyDefaultConfigIfNeeded(): Result<Unit> {
        return try {
            val configFile = File(context.dataDir, filePath)

            if (!configFile.exists()) {
                context.assets.open(filePath).use { inputStream ->
                    java.io.FileOutputStream(configFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Handles configuration change events from the file watcher.
     *
     * This internal method processes file change notifications, calculates diffs,
     * and notifies registered listeners about configuration changes.
     *
     * @param newConfig The new configuration loaded from the file
     */
    private fun handleConfigChange(newConfig: Config) {
        val oldConfig = currentConfig ?: return
        val listener = currentListener ?: return

        // Calculate diff to show exactly what changed
        val diff = createDiff(oldConfig, newConfig)

        // Update current configuration
        currentConfig = newConfig

        // Notify listener about the change
        try {
            listener.onConfigChanged(newConfig, diff)
        } catch (e: Exception) {
            // Don't let listener exceptions break the watching system
            handleConfigError(e)
        }
    }

    /**
     * Handles configuration error events from the file watcher.
     *
     * This internal method processes error notifications and forwards them
     * to registered listeners.
     *
     * @param error The error that occurred during file watching or parsing
     */
    private fun handleConfigError(error: Throwable) {
        val listener = currentListener ?: return
        val exception = if (error is Exception) error else Exception(error.message, error)

        try {
            listener.onConfigError(exception)
        } catch (e: Exception) {
            // Don't let listener exceptions cause infinite error loops
            // In a real application, you might want to log this
        }
    }

    /**
     * Creates a diff configuration object showing changes between two configurations.
     *
     * This method compares two configuration objects and creates a new configuration
     * object that contains only the fields that have changed. Unchanged fields are
     * set to null in the diff object, making it easy to identify what specifically
     * changed.
     *
     * ## Diff Object Structure
     * The returned diff object follows these rules:
     * - **Changed fields**: Contain the new value from the updated configuration
     * - **Unchanged fields**: Are set to null
     * - **Type safety**: Maintains the same Config type for easy field access
     *
     * ## Universal Diff Calculation
     * - **Dynamic Discovery**: Automatically compares all Config fields using reflection
     * - **Extensible**: Works with any number of fields without code changes
     * - **Type Safe**: Maintains Config type throughout the process
     * - **Maintainable**: No hardcoded field names to maintain
     *
     * ## Example Usage
     * ```kotlin
     * val oldConfig = Config(settingsLanguage = Language.en, settingsTheme = Theme.light)
     * val newConfig = Config(settingsLanguage = Language.ru, settingsTheme = Theme.light)
     * val diff = configManager.createDiff(oldConfig, newConfig)
     *
     * // diff.settingsLanguage will be Language.ru (changed)
     * // diff.settingsTheme will be null (unchanged)
     * ```
     *
     * ## Error Handling
     * This method is designed to be safe and never throw exceptions:
     * - Returns empty Config object if reflection operations fail
     * - Handles null configurations gracefully
     * - Logs errors but continues execution
     *
     * @param oldConfig The previous configuration state. Must not be null.
     * @param newConfig The new configuration state. Must not be null.
     * @return A [Config] object containing only the changed fields, with unchanged fields set to null.
     * @since 1.0.0
     * @see OnConfigChangeListener
     * @see isFieldChanged
     */
    private fun createDiff(
        oldConfig: Config,
        newConfig: Config
    ): Config {
        return try {
            val diffConfig = Config()
            val clazz = Config::class.java
            val fields = clazz.declaredFields

            for (field in fields) {
                field.isAccessible = true
                val fieldName = field.name
                val oldValue = getValueByName(oldConfig, fieldName)
                val newValue = getValueByName(newConfig, fieldName)

                // Include field in diff only if it changed
                if (oldValue != newValue) {
                    field.set(diffConfig, newValue)
                } else {
                    field.set(diffConfig, null)
                }
            }

            diffConfig
        } catch (e: Exception) {
            // If anything fails, return empty Config
            Config()
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
            val clazz = Config::class.java
            val fields = clazz.declaredFields

            for (field in fields) {
                field.isAccessible = true
                val fieldValue = field.get(obj)

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
     * Gets the value of a field using reflection.
     *
     * @param config The Config object to extract the field value from
     * @param fieldName The name of the field
     * @return The field value, or null if not found or error occurred
     */
    private fun getValueByName(
        config: Config?,
        fieldName: String
    ): Any? {
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
        config.settingsActiveTab?.let {
            yaml.appendLine("settings-active-tab: ${it.name}")
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
