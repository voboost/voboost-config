package ru.voboost.config.models

/**
 * Main configuration data model representing the complete application configuration.
 *
 * This data class provides a type-safe representation of the YAML configuration file
 * with a completely flat structure. Field names in Kotlin code map directly to YAML keys
 * using kebab-case conversion (e.g., settingsLanguage -> settings-language).
 *
 * ## YAML Structure Mapping
 * The class maps to this flat YAML structure:
 * ```yaml
 * settings-language: en
 * settings-theme: auto
 * settings-interface-shift-x: 0
 * settings-interface-shift-y: 0
 * vehicle-fuel-mode: intellectual
 * vehicle-drive-mode: comfort
 * ```
 *
 * ## Flat Model Benefits
 * - **Simplified Access**: Direct field access without nested navigation
 * - **Programmatic Friendly**: Easier to work with in loops and reflection
 * - **Diff Calculation**: Simplified comparison logic
 * - **Type Safety**: All fields directly accessible with compile-time checking
 * - **No Annotations**: Clean code without mapping annotations
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
 * val language = config?.settingsLanguage ?: Language.EN // Direct access with default fallback
 * val theme = config?.settingsTheme ?: Theme.AUTO
 * ```
 *
 * ### Creating Partial Configuration for Saving
 * ```kotlin
 * val partialConfig = Config(
 *     settingsLanguage = Language.RU,
 *     settingsTheme = Theme.DARK
 *     // Other fields remain null - won't be written to YAML
 * )
 * configManager.saveConfig(context, "config.yaml", partialConfig)
 * ```
 *
 * ### Processing Diff Objects
 * ```kotlin
 * override fun onConfigChanged(newConfig: Config, diff: Config) {
 *     // Only react to fields that actually changed
 *     diff.language?.let { newLanguage ->
 *         updateAppLanguage(newLanguage)
 *     }
 *     diff.fuelMode?.let { newFuelMode ->
 *         updateVehicleSettings(newFuelMode)
 *     }
 * }
 * ```
 *
 * @since 1.0.0
 * @see ConfigManager
 * @see Language
 * @see Theme
 * @see FuelMode
 * @see DriveMode
 */
data class Config(
    /**
     * Application display language setting.
     *
     * **YAML Values**: `ru` (Russian), `en` (English)
     * **YAML Key**: `settings-language`
     */
    val settingsLanguage: Language? = null,
    /**
     * Application theme/appearance setting.
     *
     * **YAML Values**:
     * - `auto` - Follow system theme (light/dark)
     * - `light` - Force light theme
     * - `dark` - Force dark theme
     * **YAML Key**: `settings-theme`
     */
    val settingsTheme: Theme? = null,
    /**
     * Horizontal interface positioning adjustment in pixels.
     *
     * **Value Range**: Typically -100 to +100 pixels, but not enforced
     * **Positive Values**: Shift interface to the right
     * **Negative Values**: Shift interface to the left
     * **Zero**: No horizontal adjustment
     * **YAML Key**: `settings-interface-shift-x`
     */
    val settingsInterfaceShiftX: Int? = null,
    /**
     * Vertical interface positioning adjustment in pixels.
     *
     * **Value Range**: Typically -100 to +100 pixels, but not enforced
     * **Positive Values**: Shift interface downward
     * **Negative Values**: Shift interface upward
     * **Zero**: No vertical adjustment
     * **YAML Key**: `settings-interface-shift-y`
     */
    val settingsInterfaceShiftY: Int? = null,
    /**
     * Vehicle fuel/energy management mode setting.
     *
     * **YAML Values**:
     * - `intellectual` - Smart automatic fuel/energy management
     * - `electric` - Prefer electric power when available
     * - `fuel` - Prefer traditional fuel
     * - `save` - Energy/fuel saving mode
     * **YAML Key**: `vehicle-fuel-mode`
     */
    val vehicleFuelMode: FuelMode? = null,
    /**
     * Vehicle driving behavior mode setting.
     *
     * **YAML Values**:
     * - `eco` - Economy mode for maximum efficiency
     * - `comfort` - Balanced comfort-oriented driving
     * - `sport` - Performance-oriented driving
     * - `snow` - Optimized for snow/winter conditions
     * - `outing` - Off-road and rough terrain mode
     * - `individual` - Custom user-defined settings
     * **YAML Key**: `vehicle-drive-mode`
     */
    val vehicleDriveMode: DriveMode? = null
)

/**
 * Supported application display languages.
 *
 * Defines the available language options for the application's user interface.
 * Enum constant names directly match YAML string values for Hoplite compatibility.
 *
 * @since 1.0.0
 */
enum class Language {
    /**
     * Russian language.
     *
     * **YAML Value**: `ru`
     * **Usage**: Sets application interface to Russian language
     */
    ru,

    /**
     * English language.
     *
     * **YAML Value**: `en`
     * **Usage**: Sets application interface to English language
     */
    en
}

/**
 * Supported application visual themes.
 *
 * Defines the available theme options that control the application's visual appearance
 * including color schemes, contrast, and overall styling.
 *
 * @since 1.0.0
 */
enum class Theme {
    /**
     * Automatic theme selection based on system settings.
     *
     * **YAML Value**: `auto`
     * **Behavior**: Follows system dark/light mode preference
     * **Recommended**: Default choice for most applications
     */
    auto,

    /**
     * Light theme with bright colors and dark text.
     *
     * **YAML Value**: `light`
     * **Behavior**: Forces light theme regardless of system setting
     * **Use Case**: Better visibility in bright environments
     */
    light,

    /**
     * Dark theme with dark colors and light text.
     *
     * **YAML Value**: `dark`
     * **Behavior**: Forces dark theme regardless of system setting
     * **Use Case**: Reduced eye strain in low-light environments
     */
    dark
}

/**
 * Vehicle fuel and energy management modes.
 *
 * Defines how hybrid and electric vehicles manage fuel consumption and energy usage.
 * These modes affect engine behavior, battery usage, and overall vehicle efficiency.
 * Enum constant names directly match YAML string values for Hoplite compatibility.
 *
 * @since 1.0.0
 */
enum class FuelMode {
    /**
     * Intelligent automatic fuel/energy management.
     *
     * **YAML Value**: `intellectual`
     * **Behavior**: Smart algorithm decides optimal fuel/electric usage
     * **Best For**: Daily driving with mixed conditions
     */
    intellectual,

    /**
     * Prefer electric power when available.
     *
     * **YAML Value**: `electric`
     * **Behavior**: Uses electric motor primarily, engine as backup
     * **Best For**: City driving and short trips
     */
    electric,

    /**
     * Prefer traditional fuel engine.
     *
     * **YAML Value**: `fuel`
     * **Behavior**: Uses combustion engine primarily
     * **Best For**: Highway driving, long trips
     */
    fuel,

    /**
     * Maximum energy and fuel efficiency mode.
     *
     * **YAML Value**: `save`
     * **Behavior**: Optimizes for maximum range and efficiency
     * **Best For**: Long trips, low fuel/battery situations
     * **Trade-off**: Reduced performance for better efficiency
     */
    save
}

/**
 * Vehicle driving behavior and performance modes.
 *
 * Defines different driving characteristics that affect throttle response, steering feel,
 * suspension settings, and overall vehicle dynamics.
 * Enum constant names directly match YAML string values for Hoplite compatibility.
 *
 * @since 1.0.0
 */
enum class DriveMode {
    /**
     * Economy mode optimized for fuel efficiency.
     *
     * **YAML Value**: `eco`
     * **Characteristics**:
     * - Gentle throttle response
     * - Early gear shifts
     * - Reduced air conditioning
     * **Best For**: Maximum fuel economy
     */
    eco,

    /**
     * Balanced comfort-oriented driving mode.
     *
     * **YAML Value**: `comfort`
     * **Characteristics**:
     * - Smooth acceleration and braking
     * - Comfortable suspension settings
     * - Balanced performance and efficiency
     * **Best For**: Daily commuting and city driving
     */
    comfort,

    /**
     * Performance-oriented driving mode.
     *
     * **YAML Value**: `sport`
     * **Characteristics**:
     * - Aggressive throttle response
     * - Firmer suspension
     * - Later gear shifts
     * **Best For**: Spirited driving and highway performance
     */
    sport,

    /**
     * Winter/snow driving optimization.
     *
     * **YAML Value**: `snow`
     * **Characteristics**:
     * - Gentle power delivery
     * - Traction control optimization
     * - Stability-focused settings
     * **Best For**: Slippery conditions and winter driving
     */
    snow,

    /**
     * Off-road and rough terrain mode.
     *
     * **YAML Value**: `outing`
     * **Characteristics**:
     * - Enhanced traction control
     * - Optimized for uneven surfaces
     * - Increased ground clearance settings
     * - Improved stability on rough terrain
     * **Best For**: Off-road driving, unpaved roads, and challenging terrain
     */
    outing,

    /**
     * User-customizable individual settings.
     *
     * **YAML Value**: `individual`
     * **Characteristics**: Custom user-defined parameters
     * **Best For**: Personalized driving preferences
     * **Note**: Actual behavior depends on user configuration
     */
    individual
}
