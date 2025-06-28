package ru.voboost.config.models

import com.sksamuel.hoplite.ConfigAlias

/**
 * Vehicle section of the configuration.
 */
data class Vehicle(
    /**
     * Vehicle fuel/energy management mode setting.
     *
     * **YAML Values**:
     * - `intellectual` - Smart automatic fuel/energy management
     * - `electric` - Prefer electric power when available
     * - `fuel` - Prefer traditional fuel
     * - `save` - Energy/fuel saving mode
     */
    @ConfigAlias("fuel-mode")
    val fuelMode: FuelMode? = null,

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
     */
    @ConfigAlias("drive-mode")
    val driveMode: DriveMode? = null
)

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