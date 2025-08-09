package ru.voboost.config

import ru.voboost.config.models.Config

/**
 * Listener interface for receiving real-time configuration change notifications.
 *
 * This interface defines the contract for receiving notifications when a watched
 * configuration file is modified and successfully reloaded. Implementations can
 * react to specific configuration changes using the provided diff object.
 *
 * ## Usage Pattern
 * Typically implemented as an anonymous object or by a class that needs to respond
 * to configuration changes:
 *
 * ```kotlin
 * val listener = object : OnConfigChangeListener {
 *     override fun onConfigChanged(newConfig: Config, diff: Config) {
 *         // React to specific changes
 *         diff.settingsLanguage?.let { newLanguage ->
 *             updateAppLanguage(newLanguage)
 *         }
 *         diff.settingsTheme?.let { newTheme ->
 *             updateAppTheme(newTheme)
 *         }
 *     }
 * }
 *
 * configManager.startWatching(listener)
 * ```
 *
 * ## Threading Considerations
 * The [onConfigChanged] method is called on a background thread (IO dispatcher).
 * If you need to update UI components, ensure you switch to the main thread:
 *
 * ```kotlin
 * override fun onConfigChanged(newConfig: Config, diff: Config) {
 *     runOnUiThread {
 *         // Safe to update UI here
 *         updateUserInterface(newConfig)
 *     }
 * }
 * ```
 *
 * ## Error Handling
 * This method is only called when configuration loading and parsing succeeds.
 * If the configuration file becomes invalid or unreadable, the [onConfigError]
 * method will be called instead.
 *
 * ## Performance Considerations
 * - Keep implementations lightweight to avoid blocking the file watcher
 * - Consider using coroutines for heavy processing
 * - Cache expensive operations based on configuration values
 *
 * @since 1.0.0
 * @see ConfigManager.startWatching
 * @see Config
 */
interface OnConfigChangeListener {
    /**
     * Called when the watched configuration file has been modified and successfully reloaded.
     *
     * This method provides both the complete new configuration and a diff object that
     * contains only the fields that have changed. This allows for efficient reaction
     * to specific configuration changes without having to compare the entire configuration.
     *
     * ## Parameters Explained
     *
     * ### newConfig
     * The complete, updated configuration object with all current values. Use this when you
     * need the full configuration state or when initializing components that depend on
     * multiple configuration values.
     *
     * ### diff
     * A configuration object where:
     * - **Changed fields** contain their new values
     * - **Unchanged fields** are null
     *
     * This enables precise reaction to changes:
     * ```kotlin
     * override fun onConfigChanged(newConfig: Config, diff: Config) {
     *     // Only language changed
     *     diff.settingsLanguage?.let { newLanguage ->
     *         // React to language change
     *         updateLocalization(newLanguage)
     *     }
     *
     *     // Only theme changed
     *     diff.settingsTheme?.let { newTheme ->
     *         // React to theme change
     *         applyTheme(newTheme)
     *     }
     *
     *     // Multiple vehicle settings changed
     *     if (diff.vehicleFuelMode != null || diff.vehicleDriveMode != null) {
     *         updateVehicleSettings(newConfig)
     *     }
     * }
     * ```
     *
     * ## Threading Context
     * This method is invoked on a background thread. For UI updates, use appropriate
     * thread switching mechanisms:
     * - `runOnUiThread { }` in Activities
     * - `lifecycleScope.launch(Dispatchers.Main) { }` in lifecycle-aware components
     * - `withContext(Dispatchers.Main) { }` in coroutines
     *
     * ## Exception Handling
     * Exceptions thrown from this method are caught and logged but do not stop
     * the file watching process. However, it's recommended to handle exceptions
     * gracefully within your implementation.
     *
     * @param newConfig The complete updated configuration object containing all current
     *                  values. Never null, but individual fields may be null if not
     *                  present in the YAML file.
     * @param diff Configuration object containing only the fields that changed.
     *             Unchanged fields are null. Never null as a whole, but will have
     *             all fields null if called incorrectly (shouldn't happen in normal usage).
     * @since 1.0.0
     * @see Config
     * @see ConfigManager.startWatching
     * @see ConfigManager.stopWatching
     */
    fun onConfigChanged(
        newConfig: Config,
        diff: Config
    )

    /**
     * Called when the watched configuration file has been modified but parsing failed.
     *
     * This method is invoked when the file watcher detects a change in the configuration
     * file, but the new content cannot be parsed successfully. This typically happens when:
     * - Invalid YAML syntax is introduced
     * - Invalid enum values are used (e.g., "eco1" instead of "eco")
     * - Required fields are missing or have wrong types
     * - File becomes corrupted or unreadable
     *
     * ## Error Recovery
     * When this method is called, the ConfigManager continues to use the last valid
     * configuration that was successfully loaded. The file watching continues, so if
     * the file is corrected, [onConfigChanged] will be called with the new valid configuration.
     *
     * ## Threading Context
     * Like [onConfigChanged], this method is invoked on a background thread. For UI updates,
     * use appropriate thread switching mechanisms.
     *
     * ## Implementation Example
     * ```kotlin
     * override fun onConfigError(error: Exception) {
     *     runOnUiThread {
     *         showErrorMessage("Configuration error: ${error.message}")
     *         // Keep using the last valid configuration
     *     }
     * }
     * ```
     *
     * ## Default Implementation
     * This method has a default empty implementation to maintain backward compatibility.
     * Existing implementations of OnConfigChangeListener will continue to work without
     * modification, but won't receive error notifications.
     *
     * @param error The exception that occurred during configuration parsing.
     *              Contains details about what went wrong.
     * @since 1.0.0
     * @see onConfigChanged
     * @see ConfigManager.startWatching
     */
    fun onConfigError(error: Exception) {
        // Default empty implementation for backward compatibility
    }
}
