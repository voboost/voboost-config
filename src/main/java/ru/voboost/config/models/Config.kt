package ru.voboost.config.models

/**
 * Main configuration data model representing the complete application configuration.
 *
 * This data class provides a type-safe representation of the YAML configuration file
 * with a nested structure that directly maps to the YAML hierarchy. The design prioritizes
 * ease of use and type safety while maintaining compatibility with hierarchical YAML structure.
 *
 * ## YAML Structure Mapping
 * The class maps to this YAML structure:
 * ```yaml
 * settings:
 *   language: en
 *   theme: auto
 *   interface-shift-x: 0
 *   interface-shift-y: 0
 *
 * vehicle:
 *   fuel-mode: intellectual
 *   drive-mode: comfort
 * ```
 *
 * ## Nullable Fields Design
 * All fields are nullable (`?`) to provide several benefits:
 * - **Graceful degradation**: Missing YAML values don't cause parsing failures
 * - **Partial updates**: Only specific fields can be set when saving configuration
 * - **Diff calculation**: Unchanged fields are represented as null in diff objects
 * - **Default value handling**: Applications can provide their own defaults for null values
 *
 * ## Usage Patterns
 *
 * ### Loading Complete Configuration
 * ```kotlin
 * val config = configManager.loadConfig(context, "config.yaml").getOrNull()
 * val language = config?.settings?.language ?: Language.EN // Default fallback
 * ```
 *
 * ### Creating Partial Configuration for Saving
 * ```kotlin
 * val partialConfig = Config(
 *     settings = Settings(language = Language.RU, theme = Theme.DARK)
 *     // Other fields remain null - won't be written to YAML
 * )
 * configManager.saveConfig(context, "config.yaml", partialConfig)
 * ```
 *
 * ### Processing Diff Objects
 * ```kotlin
 * override fun onConfigChanged(newConfig: Config, diff: Config) {
 *     // Only react to fields that actually changed
 *     diff.settings?.language?.let { newLanguage ->
 *         updateAppLanguage(newLanguage)
 *     }
 *     diff.vehicle?.fuelMode?.let { newFuelMode ->
 *         updateVehicleSettings(newFuelMode)
 *     }
 * }
 * ```
 *
 * @since 1.0.0
 * @see ConfigManager
 * @see Settings
 * @see Vehicle
 * @see Language
 * @see Theme
 * @see FuelMode
 * @see DriveMode
 */
data class Config(
    /**
     * Settings section containing user interface and application behavior settings.
     */
    val settings: Settings? = null,

    /**
     * Vehicle section containing vehicle-specific configuration.
     */
    val vehicle: Vehicle? = null
)