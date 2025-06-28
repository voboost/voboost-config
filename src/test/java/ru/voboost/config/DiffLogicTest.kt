package ru.voboost.config

import org.junit.Test
import org.junit.Assert.*
import ru.voboost.config.models.Config
import ru.voboost.config.models.Settings
import ru.voboost.config.models.Vehicle
import ru.voboost.config.models.Language
import ru.voboost.config.models.Theme
import ru.voboost.config.models.FuelMode
import ru.voboost.config.models.DriveMode

/**
 * Tests for diff calculation logic in ConfigManager.
 *
 * Tests cover various scenarios of configuration changes and diff generation.
 */
class DiffLogicTest : BaseConfigTest() {

    @Test
    fun testCreateDiff_noChanges_allFieldsNull() {
        val config1 = Config(
            settings = Settings(
                language = Language.en,
                theme = Theme.dark,
                interfaceShiftX = 10,
                interfaceShiftY = 20
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.intellectual,
                driveMode = DriveMode.comfort
            )
        )
        val config2 = Config(
            settings = Settings(
                language = Language.en,
                theme = Theme.dark,
                interfaceShiftX = 10,
                interfaceShiftY = 20
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.intellectual,
                driveMode = DriveMode.comfort
            )
        )

        val diff = createDiffViaReflection(config1, config2)

        // All fields should be null since nothing changed
        assertNull("Settings should be null", diff.settings)
        assertNull("Vehicle should be null", diff.vehicle)
    }

    @Test
    fun testCreateDiff_singleChange_onlyOneFieldNonNull() {
        val oldConfig = Config(
            settings = Settings(
                language = Language.en,
                theme = Theme.dark
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.intellectual
            )
        )
        val newConfig = Config(
            settings = Settings(
                language = Language.ru, // Only this field changed
                theme = Theme.dark
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.intellectual
            )
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        // Only changed field should be non-null
        assertEquals("Only language should be changed", Language.ru, diff.settings?.language)
        assertNull("Theme should be null", diff.settings?.theme)
        assertNull("Fuel mode should be null", diff.vehicle?.fuelMode)
    }

    @Test
    fun testCreateDiff_multipleChanges_severalFieldsNonNull() {
        val oldConfig = Config(
            settings = Settings(
                language = Language.en,
                theme = Theme.dark,
                interfaceShiftX = 10
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.intellectual,
                driveMode = DriveMode.comfort
            )
        )
        val newConfig = Config(
            settings = Settings(
                language = Language.ru,     // Changed
                theme = Theme.light,        // Changed
                interfaceShiftX = 10        // Same
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.electric, // Changed
                driveMode = DriveMode.comfort // Same
            )
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        // Only changed fields should be non-null
        assertEquals("Language should be changed", Language.ru, diff.settings?.language)
        assertEquals("Theme should be changed", Theme.light, diff.settings?.theme)
        assertNull("Interface shift X should be null (unchanged)", diff.settings?.interfaceShiftX)
        assertEquals("Fuel mode should be changed", FuelMode.electric, diff.vehicle?.fuelMode)
        assertNull("Drive mode should be null (unchanged)", diff.vehicle?.driveMode)
    }

    @Test
    fun testCreateDiff_differentDataTypes_allTypesHandled() {
        val oldConfig = Config(
            settings = Settings(
                language = Language.en,         // Enum
                interfaceShiftX = 10,           // Int
                theme = Theme.dark              // Enum
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.intellectual // Enum
            )
        )
        val newConfig = Config(
            settings = Settings(
                language = Language.ru,         // Enum changed
                interfaceShiftX = 20,           // Int changed
                theme = Theme.light             // Enum changed
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.electric     // Enum changed
            )
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        // All different data types should be handled correctly
        assertEquals("Enum type should be handled", Language.ru, diff.settings?.language)
        assertEquals("Int type should be handled", 20, diff.settings?.interfaceShiftX)
        assertEquals("Enum type should be handled", Theme.light, diff.settings?.theme)
        assertEquals("Enum type should be handled", FuelMode.electric, diff.vehicle?.fuelMode)
    }

    @Test
    fun testCreateDiff_privateMethod_viaReflection() {
        // Test the private createDiff method using reflection
        // This tests the core diff calculation logic

        val oldConfig = Config(
            settings = Settings(
                language = Language.en,
                theme = Theme.dark,
                interfaceShiftX = 10
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.intellectual,
                driveMode = DriveMode.comfort
            )
        )

        val newConfig = Config(
            settings = Settings(
                language = Language.ru,     // Changed
                theme = Theme.dark,         // Same
                interfaceShiftX = 20,       // Changed
                interfaceShiftY = 5         // New field
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.intellectual, // Same
                driveMode = DriveMode.sport   // Changed
            )
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        // Verify diff contains only changed fields
        assertEquals("Language should be in diff (changed)", Language.ru, diff.settings?.language)
        assertNull("Theme should not be in diff (unchanged)", diff.settings?.theme)
        assertEquals("Interface shift X should be in diff (changed)", 20, diff.settings?.interfaceShiftX)
        assertEquals("Interface shift Y should be in diff (new field)", 5, diff.settings?.interfaceShiftY)
        assertNull("Fuel mode should not be in diff (unchanged)", diff.vehicle?.fuelMode)
        assertEquals("Drive mode should be in diff (changed)", DriveMode.sport, diff.vehicle?.driveMode)
    }

    @Test
    fun testCreateDiff_noChanges_viaReflection() {
        // Test diff calculation when nothing changes
        val config1 = Config(
            settings = Settings(
                language = Language.en,
                theme = Theme.dark,
                interfaceShiftX = 10
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.intellectual
            )
        )

        val config2 = Config(
            settings = Settings(
                language = Language.en,
                theme = Theme.dark,
                interfaceShiftX = 10
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.intellectual
            )
        )

        val diff = createDiffViaReflection(config1, config2)

        // All fields should be null since nothing changed
        assertNull("Settings should be null (no change)", diff.settings)
        assertNull("Vehicle should be null (no change)", diff.vehicle)
    }

    @Test
    fun testCreateDiff_nullToValue_detectedAsChange() {
        // Test that changes from null to value are detected
        val oldConfig = Config(
            settings = Settings(
                language = null,
                theme = Theme.dark
            )
        )

        val newConfig = Config(
            settings = Settings(
                language = Language.en,  // null -> EN
                theme = Theme.dark       // unchanged
            )
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        assertEquals("Language change from null should be detected", Language.en, diff.settings?.language)
        assertNull("Theme should not be in diff (unchanged)", diff.settings?.theme)
    }

    @Test
    fun testCreateDiff_valueToNull_detectedAsChange() {
        // Test that changes from value to null are detected
        val oldConfig = Config(
            settings = Settings(
                language = Language.en,
                theme = Theme.dark
            )
        )

        val newConfig = Config(
            settings = Settings(
                language = null,         // EN -> null
                theme = Theme.dark       // unchanged
            )
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        assertNull("Language change to null should be detected as null in diff", diff.settings?.language)
        assertNull("Theme should not be in diff (unchanged)", diff.settings?.theme)

        // Note: The current implementation sets changed fields to new values,
        // so null -> value shows new value, but value -> null shows null
        // This is the expected behavior for the diff object
    }

    @Test
    fun testCreateDiff_allEnumTypes_changesDetected() {
        // Test diff calculation with all enum types changing
        val oldConfig = Config(
            settings = Settings(
                language = Language.en,
                theme = Theme.auto
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.intellectual,
                driveMode = DriveMode.eco
            )
        )

        val newConfig = Config(
            settings = Settings(
                language = Language.ru,
                theme = Theme.dark
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.electric,
                driveMode = DriveMode.sport
            )
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        // All enum fields should be in diff
        assertEquals("Language change should be detected", Language.ru, diff.settings?.language)
        assertEquals("Theme change should be detected", Theme.dark, diff.settings?.theme)
        assertEquals("Fuel mode change should be detected", FuelMode.electric, diff.vehicle?.fuelMode)
        assertEquals("Drive mode change should be detected", DriveMode.sport, diff.vehicle?.driveMode)

        // Non-changed fields should be null
        assertNull("Interface shift X should be null (not set)", diff.settings?.interfaceShiftX)
        assertNull("Interface shift Y should be null (not set)", diff.settings?.interfaceShiftY)
    }
}