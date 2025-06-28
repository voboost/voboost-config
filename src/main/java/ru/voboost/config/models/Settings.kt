package ru.voboost.config.models

import com.sksamuel.hoplite.ConfigAlias

/**
 * Settings section of the configuration.
 */
data class Settings(
    /**
     * Application display language setting.
     *
     * **YAML Values**: `ru` (Russian), `en` (English)
     */
    val language: Language? = null,

    /**
     * Application theme/appearance setting.
     *
     * **YAML Values**:
     * - `auto` - Follow system theme (light/dark)
     * - `light` - Force light theme
     * - `dark` - Force dark theme
     */
    val theme: Theme? = null,

    /**
     * Horizontal interface positioning adjustment in pixels.
     *
     * **Value Range**: Typically -100 to +100 pixels, but not enforced
     * **Positive Values**: Shift interface to the right
     * **Negative Values**: Shift interface to the left
     * **Zero**: No horizontal adjustment
     */
    @ConfigAlias("interface-shift-x")
    val interfaceShiftX: Int? = null,

    /**
     * Vertical interface positioning adjustment in pixels.
     *
     * **Value Range**: Typically -100 to +100 pixels, but not enforced
     * **Positive Values**: Shift interface downward
     * **Negative Values**: Shift interface upward
     * **Zero**: No vertical adjustment
     */
    @ConfigAlias("interface-shift-y")
    val interfaceShiftY: Int? = null
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