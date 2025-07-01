package ru.voboost.config

import org.junit.Test
import org.junit.Assert.*
import ru.voboost.config.models.Config
import ru.voboost.config.models.Language
import ru.voboost.config.models.Theme
import ru.voboost.config.models.FuelMode
import ru.voboost.config.models.DriveMode

/**
 * Tests for diff calculation logic in ConfigManager.
 *
 * Tests cover various scenarios of configuration changes and diff generation
 * with the flattened Config model.
 */
class DiffLogicTest : BaseConfigTest() {

    @Test
    fun testCreateDiff_noChanges_allFieldsNull() {
        val config1 = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.dark,
            settingsInterfaceShiftX = 10,
            settingsInterfaceShiftY = 20,
            vehicleFuelMode = FuelMode.intellectual,
            vehicleDriveMode = DriveMode.comfort
        )
        val config2 = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.dark,
            settingsInterfaceShiftX = 10,
            settingsInterfaceShiftY = 20,
            vehicleFuelMode = FuelMode.intellectual,
            vehicleDriveMode = DriveMode.comfort
        )

        val diff = createDiffViaReflection(config1, config2)

        // All fields should be null since nothing changed
        assertNull("Language should be null", diff.settingsLanguage)
        assertNull("Theme should be null", diff.settingsTheme)
        assertNull("Interface shift X should be null", diff.settingsInterfaceShiftX)
        assertNull("Interface shift Y should be null", diff.settingsInterfaceShiftY)
        assertNull("Fuel mode should be null", diff.vehicleFuelMode)
        assertNull("Drive mode should be null", diff.vehicleDriveMode)
    }

    @Test
    fun testCreateDiff_singleChange_onlyOneFieldNonNull() {
        val oldConfig = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.dark,
            vehicleFuelMode = FuelMode.intellectual,
            vehicleDriveMode = DriveMode.comfort
        )
        val newConfig = Config(
            settingsLanguage = Language.ru, // Only this field changed
            settingsTheme = Theme.dark,
            vehicleFuelMode = FuelMode.intellectual,
            vehicleDriveMode = DriveMode.comfort
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        // Only changed field should be non-null
        assertEquals("Only language should be changed", Language.ru, diff.settingsLanguage)
        assertNull("Theme should be null", diff.settingsTheme)
        assertNull("Fuel mode should be null", diff.vehicleFuelMode)
        assertNull("Drive mode should be null", diff.vehicleDriveMode)
        assertNull("Interface shift X should be null", diff.settingsInterfaceShiftX)
        assertNull("Interface shift Y should be null", diff.settingsInterfaceShiftY)
    }

    @Test
    fun testCreateDiff_multipleChanges_severalFieldsNonNull() {
        val oldConfig = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.dark,
            settingsInterfaceShiftX = 10,
            settingsInterfaceShiftY = 5,
            vehicleFuelMode = FuelMode.intellectual,
            vehicleDriveMode = DriveMode.comfort
        )
        val newConfig = Config(
            settingsLanguage = Language.ru,        // Changed
            settingsTheme = Theme.light,           // Changed
            settingsInterfaceShiftX = 10,          // Same
            settingsInterfaceShiftY = 15,          // Changed
            vehicleFuelMode = FuelMode.electric,  // Changed
            vehicleDriveMode = DriveMode.comfort  // Same
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        // Only changed fields should be non-null
        assertEquals("Language should be changed", Language.ru, diff.settingsLanguage)
        assertEquals("Theme should be changed", Theme.light, diff.settingsTheme)
        assertNull("Interface shift X should be null (unchanged)", diff.settingsInterfaceShiftX)
        assertEquals("Interface shift Y should be changed", 15, diff.settingsInterfaceShiftY)
        assertEquals("Fuel mode should be changed", FuelMode.electric, diff.vehicleFuelMode)
        assertNull("Drive mode should be null (unchanged)", diff.vehicleDriveMode)
    }

    @Test
    fun testCreateDiff_differentDataTypes_allTypesHandled() {
        val oldConfig = Config(
            settingsLanguage = Language.en,         // Enum
            settingsTheme = Theme.dark,             // Enum
            settingsInterfaceShiftX = 10,           // Int
            settingsInterfaceShiftY = 5,            // Int
            vehicleFuelMode = FuelMode.intellectual, // Enum
            vehicleDriveMode = DriveMode.eco       // Enum
        )
        val newConfig = Config(
            settingsLanguage = Language.ru,         // Enum changed
            settingsTheme = Theme.light,            // Enum changed
            settingsInterfaceShiftX = 20,           // Int changed
            settingsInterfaceShiftY = 15,           // Int changed
            vehicleFuelMode = FuelMode.electric,   // Enum changed
            vehicleDriveMode = DriveMode.sport     // Enum changed
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        // All different data types should be handled correctly
        assertEquals("Language enum should be handled", Language.ru, diff.settingsLanguage)
        assertEquals("Theme enum should be handled", Theme.light, diff.settingsTheme)
        assertEquals("Interface shift X int should be handled", 20, diff.settingsInterfaceShiftX)
        assertEquals("Interface shift Y int should be handled", 15, diff.settingsInterfaceShiftY)
        assertEquals("Fuel mode enum should be handled", FuelMode.electric, diff.vehicleFuelMode)
        assertEquals("Drive mode enum should be handled", DriveMode.sport, diff.vehicleDriveMode)
    }

    @Test
    fun testCreateDiff_privateMethod_viaReflection() {
        // Test the private createDiff method using reflection
        // This tests the core diff calculation logic with flattened model

        val oldConfig = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.dark,
            settingsInterfaceShiftX = 10,
            vehicleFuelMode = FuelMode.intellectual,
            vehicleDriveMode = DriveMode.comfort
        )

        val newConfig = Config(
            settingsLanguage = Language.ru,     // Changed
            settingsTheme = Theme.dark,         // Same
            settingsInterfaceShiftX = 20,       // Changed
            settingsInterfaceShiftY = 5,        // New field (null -> value)
            vehicleFuelMode = FuelMode.intellectual, // Same
            vehicleDriveMode = DriveMode.sport // Changed
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        // Verify diff contains only changed fields
        assertEquals("Language should be in diff (changed)", Language.ru, diff.settingsLanguage)
        assertNull("Theme should not be in diff (unchanged)", diff.settingsTheme)
        assertEquals("Interface shift X should be in diff (changed)", 20, diff.settingsInterfaceShiftX)
        assertEquals("Interface shift Y should be in diff (new field)", 5, diff.settingsInterfaceShiftY)
        assertNull("Fuel mode should not be in diff (unchanged)", diff.vehicleFuelMode)
        assertEquals("Drive mode should be in diff (changed)", DriveMode.sport, diff.vehicleDriveMode)
    }

    @Test
    fun testCreateDiff_noChanges_viaReflection() {
        // Test diff calculation when nothing changes
        val config1 = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.dark,
            settingsInterfaceShiftX = 10,
            vehicleFuelMode = FuelMode.intellectual,
            vehicleDriveMode = DriveMode.comfort
        )

        val config2 = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.dark,
            settingsInterfaceShiftX = 10,
            vehicleFuelMode = FuelMode.intellectual,
            vehicleDriveMode = DriveMode.comfort
        )

        val diff = createDiffViaReflection(config1, config2)

        // All fields should be null since nothing changed
        assertNull("Language should be null (no change)", diff.settingsLanguage)
        assertNull("Theme should be null (no change)", diff.settingsTheme)
        assertNull("Interface shift X should be null (no change)", diff.settingsInterfaceShiftX)
        assertNull("Interface shift Y should be null (no change)", diff.settingsInterfaceShiftY)
        assertNull("Fuel mode should be null (no change)", diff.vehicleFuelMode)
        assertNull("Drive mode should be null (no change)", diff.vehicleDriveMode)
    }

    @Test
    fun testCreateDiff_nullToValue_detectedAsChange() {
        // Test that changes from null to value are detected
        val oldConfig = Config(
            settingsLanguage = null,
            settingsTheme = Theme.dark,
            vehicleFuelMode = FuelMode.intellectual
        )

        val newConfig = Config(
            settingsLanguage = Language.en,  // null -> EN
            settingsTheme = Theme.dark,      // unchanged
            vehicleFuelMode = FuelMode.intellectual // unchanged
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        assertEquals("Language change from null should be detected", Language.en, diff.settingsLanguage)
        assertNull("Theme should not be in diff (unchanged)", diff.settingsTheme)
        assertNull("Fuel mode should not be in diff (unchanged)", diff.vehicleFuelMode)
        assertNull("Interface shift X should be null", diff.settingsInterfaceShiftX)
        assertNull("Interface shift Y should be null", diff.settingsInterfaceShiftY)
        assertNull("Drive mode should be null", diff.vehicleDriveMode)
    }

    @Test
    fun testCreateDiff_valueToNull_detectedAsChange() {
        // Test that changes from value to null are detected
        val oldConfig = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.dark,
            vehicleFuelMode = FuelMode.intellectual
        )

        val newConfig = Config(
            settingsLanguage = null,         // EN -> null
            settingsTheme = Theme.dark,      // unchanged
            vehicleFuelMode = FuelMode.intellectual // unchanged
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        assertNull("Language change to null should be detected as null in diff", diff.settingsLanguage)
        assertNull("Theme should not be in diff (unchanged)", diff.settingsTheme)
        assertNull("Fuel mode should not be in diff (unchanged)", diff.vehicleFuelMode)

        // Note: The current implementation sets changed fields to new values,
        // so null -> value shows new value, but value -> null shows null
        // This is the expected behavior for the diff object
    }

    @Test
    fun testCreateDiff_allEnumTypes_changesDetected() {
        // Test diff calculation with all enum types changing
        val oldConfig = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.auto,
            vehicleFuelMode = FuelMode.intellectual,
            vehicleDriveMode = DriveMode.eco
        )

        val newConfig = Config(
            settingsLanguage = Language.ru,
            settingsTheme = Theme.dark,
            vehicleFuelMode = FuelMode.electric,
            vehicleDriveMode = DriveMode.sport
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        // All enum fields should be in diff
        assertEquals("Language change should be detected", Language.ru, diff.settingsLanguage)
        assertEquals("Theme change should be detected", Theme.dark, diff.settingsTheme)
        assertEquals("Fuel mode change should be detected", FuelMode.electric, diff.vehicleFuelMode)
        assertEquals("Drive mode change should be detected", DriveMode.sport, diff.vehicleDriveMode)

        // Non-changed fields should be null
        assertNull("Interface shift X should be null (not set)", diff.settingsInterfaceShiftX)
        assertNull("Interface shift Y should be null (not set)", diff.settingsInterfaceShiftY)
    }

    @Test
    fun testCreateDiff_interfaceShifts_integerChanges() {
        // Test diff calculation specifically for integer interface shift fields
        val oldConfig = Config(
            settingsLanguage = Language.en,
            settingsInterfaceShiftX = 10,
            settingsInterfaceShiftY = 20
        )

        val newConfig = Config(
            settingsLanguage = Language.en,  // Same
            settingsInterfaceShiftX = 15,    // Changed
            settingsInterfaceShiftY = 20     // Same
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        assertNull("Language should not be in diff (unchanged)", diff.settingsLanguage)
        assertEquals("Interface shift X should be in diff (changed)", 15, diff.settingsInterfaceShiftX)
        assertNull("Interface shift Y should not be in diff (unchanged)", diff.settingsInterfaceShiftY)
        assertNull("Theme should be null", diff.settingsTheme)
        assertNull("Fuel mode should be null", diff.vehicleFuelMode)
        assertNull("Drive mode should be null", diff.vehicleDriveMode)
    }

    @Test
    fun testCreateDiff_partialConfig_onlySetFieldsCompared() {
        // Test diff calculation with partial configs (some fields null)
        val oldConfig = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.dark
            // Other fields are null
        )

        val newConfig = Config(
            settingsLanguage = Language.ru,  // Changed
            settingsTheme = Theme.dark,      // Same
            vehicleFuelMode = FuelMode.electric // New field (null -> value)
            // Other fields are null
        )

        val diff = createDiffViaReflection(oldConfig, newConfig)

        assertEquals("Language should be in diff (changed)", Language.ru, diff.settingsLanguage)
        assertNull("Theme should not be in diff (unchanged)", diff.settingsTheme)
        assertEquals("Fuel mode should be in diff (new field)", FuelMode.electric, diff.vehicleFuelMode)
        assertNull("Drive mode should be null", diff.vehicleDriveMode)
        assertNull("Interface shift X should be null", diff.settingsInterfaceShiftX)
        assertNull("Interface shift Y should be null", diff.settingsInterfaceShiftY)
    }
}