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
 * settings-theme: light
 * settings-interface-shift-x: 0
 * settings-interface-shift-y: 0
 * settings-active-tab: interface
 * vehicle-fuel-mode: electric
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
 * @since 1.0.0
 * @see ConfigManager
 * @see Tab
 * @see FuelMode
 * @see DriveMode
 */
data class Config(
    /**
     * Application display language setting.
     * Stored as String in config file (e.g., "en", "ru") and converted to Language enum in ConfigViewModel.
     *
     * **YAML Key**: `settings-language`
     */
    var settingsLanguage: String? = null,
    /**
     * Application theme/appearance setting.
     * Stored as String in config file (e.g., "free-dark", "free-light") and converted to Theme enum in ConfigViewModel.
     *
     * **YAML Key**: `settings-theme`
     */
    var settingsTheme: String? = null,
    /**
     * Horizontal interface positioning adjustment in pixels.
     *
     * **YAML Key**: `settings-interface-shift-x`
     */
    var settingsInterfaceShiftX: Int? = null,
    /**
     * Vertical interface positioning adjustment in pixels.
     *
     * **YAML Key**: `settings-interface-shift-y`
     */
    var settingsInterfaceShiftY: Int? = null,
    /**
     * The tab that should be active when the application starts.
     *
     * **YAML Key**: `settings-active-tab`
     */
    var settingsActiveTab: Tab? = null,
    /**
     * Vehicle fuel/energy management mode setting.
     *
     * **YAML Key**: `vehicle-fuel-mode`
     */
    var vehicleFuelMode: FuelMode? = null,
    /**
     * Vehicle driving behavior mode setting.
     *
     * **YAML Key**: `vehicle-drive-mode`
     */
    var vehicleDriveMode: DriveMode? = null,
    /**
     * Russian keyboard feature setting.
     * Stored as String in config file (e.g., "enable-russian").
     *
     * **YAML Key**: `interface-keyboard`
     */
    var interfaceKeyboard: String? = null,
    /**
     * Weather widget feature setting.
     * Stored as String in config file (e.g., "enable-non-chineese-cities").
     *
     * **YAML Key**: `interface-widget-weather`
     */
    var interfaceWidgetWeather: String? = null,
    /**
     * Application startup behavior mode.
     *
     * **YAML Key**: `settings-startup`
     */
    var settingsStartup: StartupMode? = null,
    /**
     * Vehicle model type.
     *
     * **YAML Key**: `settings-car-model`
     */
    var settingsCarModel: CarModel? = null,
    /**
     * Pedestrian warning sound behavior.
     *
     * **YAML Key**: `vehicle-pedestrian-warning`
     */
    var vehiclePedestrianWarning: PedestrianWarning? = null,
)

/**
 * Defines the possible active tabs for the application.
 *
 * @since 1.1.0
 */
enum class Tab {
    store,
    applications,
    `interface`,
    vehicle,
    settings,
}

/**
 * Vehicle fuel and energy management modes.
 *
 * @since 1.0.0
 */
enum class FuelMode {
    /**
     * Prefer electric power when available.
     */
    electric,

    /**
     * Forced electric mode - prevents combustion engine from starting.
     * Only available for Voyah Free model.
     */
    electric_forced,

    /**
     * Hybrid fuel/electric management mode.
     */
    hybrid,

    /**
     * Maximum fuel consumption mode.
     */
    save,
}

/**
 * Vehicle driving behavior and performance modes.
 *
 * @since 1.0.0
 */
enum class DriveMode {
    /**
     * Economy mode optimized for fuel efficiency.
     */
    eco,

    /**
     * Balanced comfort-oriented driving mode.
     */
    comfort,

    /**
     * Performance-oriented driving mode.
     */
    sport,

    /**
     * Winter/snow driving optimization.
     */
    snow,

    /**
     * Off-road and rough terrain mode.
     */
    outing,

    /**
     * User-customizable individual settings.
     */
    individual,
}

/**
 * Application startup behavior mode.
 *
 * @since 1.2.0
 */
enum class StartupMode {
    /**
     * Application does not start automatically.
     */
    off,

    /**
     * Application starts in hidden/background mode.
     */
    hidden,

    /**
     * Application starts with interface visible.
     */
    `interface`,
}

/**
 * Vehicle model type.
 *
 * @since 1.2.0
 */
enum class CarModel {
    /**
     * Voyah Free model.
     */
    free,

    /**
     * Voyah Dreamer model.
     */
    dreamer,
}

/**
 * Pedestrian warning sound behavior.
 *
 * @since 1.2.0
 */
enum class PedestrianWarning {
    /**
     * Original vehicle default behavior.
     */
    original,

    /**
     * Turn off pedestrian warning sound.
     */
    off,
}
