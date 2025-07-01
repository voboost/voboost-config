package ru.voboost.config

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ru.voboost.config.models.*
import java.io.File

/**
 * Comprehensive test suite for ConfigManager diff utility methods.
 *
 * Tests the functionality of isFieldChanged, getFieldValue, and isValidConfig methods
 * with the flattened Config model structure.
 */
class ConfigDiffUtilsTest {

    private lateinit var configManager: ConfigManager
    private lateinit var mockContext: Context
    private lateinit var mockFilesDir: File

    @Before
    fun setUp() {
        configManager = ConfigManager()
        mockContext = mockk()
        mockFilesDir = mockk()

        every { mockContext.filesDir } returns mockFilesDir
        every { mockFilesDir.absolutePath } returns "/mock/files"
    }

    // ========== isFieldChanged Tests ==========

    @Test
    fun `isFieldChanged returns false for null diff`() {
        val result = configManager.isFieldChanged(null, "settingsLanguage")
        assertFalse("Should return false for null diff", result)
    }

    @Test
    fun `isFieldChanged returns false for empty field name`() {
        val diff = Config()
        val result = configManager.isFieldChanged(diff, "")
        assertFalse("Should return false for empty field name", result)
    }

    @Test
    fun `isFieldChanged returns false for non-existent field`() {
        val diff = Config()
        val result = configManager.isFieldChanged(diff, "nonExistentField")
        assertFalse("Should return false for non-existent field", result)
    }

    @Test
    fun `isFieldChanged returns true for changed language`() {
        val diff = Config(settingsLanguage = Language.ru)
        val result = configManager.isFieldChanged(diff, "settingsLanguage")
        assertTrue("Should return true for changed language", result)
    }

    @Test
    fun `isFieldChanged returns false for unchanged language`() {
        val diff = Config(settingsLanguage = null)
        val result = configManager.isFieldChanged(diff, "settingsLanguage")
        assertFalse("Should return false for unchanged language", result)
    }

    @Test
    fun `isFieldChanged returns true for changed theme`() {
        val diff = Config(settingsTheme = Theme.dark)
        val result = configManager.isFieldChanged(diff, "settingsTheme")
        assertTrue("Should return true for changed theme", result)
    }

    @Test
    fun `isFieldChanged returns true for changed interfaceShiftX`() {
        val diff = Config(settingsInterfaceShiftX = 100)
        val result = configManager.isFieldChanged(diff, "settingsInterfaceShiftX")
        assertTrue("Should return true for changed interfaceShiftX", result)
    }

    @Test
    fun `isFieldChanged returns true for changed interfaceShiftY`() {
        val diff = Config(settingsInterfaceShiftY = -50)
        val result = configManager.isFieldChanged(diff, "settingsInterfaceShiftY")
        assertTrue("Should return true for changed interfaceShiftY", result)
    }

    @Test
    fun `isFieldChanged returns true for changed fuelMode`() {
        val diff = Config(vehicleFuelMode = FuelMode.electric)
        val result = configManager.isFieldChanged(diff, "vehicleFuelMode")
        assertTrue("Should return true for changed fuelMode", result)
    }

    @Test
    fun `isFieldChanged returns true for changed driveMode`() {
        val diff = Config(vehicleDriveMode = DriveMode.sport)
        val result = configManager.isFieldChanged(diff, "vehicleDriveMode")
        assertTrue("Should return true for changed driveMode", result)
    }

    @Test
    fun `isFieldChanged handles multiple changed fields correctly`() {
        val diff = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.light,
            vehicleFuelMode = FuelMode.fuel
        )

        assertTrue("Language should be changed", configManager.isFieldChanged(diff, "settingsLanguage"))
        assertTrue("Theme should be changed", configManager.isFieldChanged(diff, "settingsTheme"))
        assertFalse("InterfaceShiftX should not be changed", configManager.isFieldChanged(diff, "settingsInterfaceShiftX"))
        assertTrue("FuelMode should be changed", configManager.isFieldChanged(diff, "vehicleFuelMode"))
        assertFalse("DriveMode should not be changed", configManager.isFieldChanged(diff, "vehicleDriveMode"))
    }

    // ========== getFieldValue Tests ==========

    @Test
    fun `getFieldValue returns null for null config`() {
        val result = configManager.getFieldValue(null, "settingsLanguage")
        assertNull("Should return null for null config", result)
    }

    @Test
    fun `getFieldValue returns null for invalid field name`() {
        val config = Config()
        val result = configManager.getFieldValue(config, "invalidField")
        assertNull("Should return null for invalid field name", result)
    }

    @Test
    fun `getFieldValue returns correct value for language`() {
        val config = Config(settingsLanguage = Language.ru)
        val result = configManager.getFieldValue(config, "settingsLanguage")
        assertEquals("Should return correct language value", "ru", result)
    }

    @Test
    fun `getFieldValue returns correct value for theme`() {
        val config = Config(settingsTheme = Theme.dark)
        val result = configManager.getFieldValue(config, "settingsTheme")
        assertEquals("Should return correct theme value", "dark", result)
    }

    @Test
    fun `getFieldValue returns correct value for interfaceShiftX`() {
        val config = Config(settingsInterfaceShiftX = 150)
        val result = configManager.getFieldValue(config, "settingsInterfaceShiftX")
        assertEquals("Should return correct interfaceShiftX value", "150", result)
    }

    @Test
    fun `getFieldValue returns correct value for interfaceShiftY`() {
        val config = Config(settingsInterfaceShiftY = -75)
        val result = configManager.getFieldValue(config, "settingsInterfaceShiftY")
        assertEquals("Should return correct interfaceShiftY value", "-75", result)
    }

    @Test
    fun `getFieldValue returns correct value for fuelMode`() {
        val config = Config(vehicleFuelMode = FuelMode.electric)
        val result = configManager.getFieldValue(config, "vehicleFuelMode")
        assertEquals("Should return correct fuelMode value", "electric", result)
    }

    @Test
    fun `getFieldValue returns correct value for driveMode`() {
        val config = Config(vehicleDriveMode = DriveMode.sport)
        val result = configManager.getFieldValue(config, "vehicleDriveMode")
        assertEquals("Should return correct driveMode value", "sport", result)
    }

    @Test
    fun `getFieldValue returns null for null field values`() {
        val config = Config(settingsLanguage = null)
        val result = configManager.getFieldValue(config, "settingsLanguage")
        assertNull("Should return null for null field values", result)
    }

    @Test
    fun `getFieldValue handles all enum values correctly`() {
        // Test all Language enum values
        for (language in Language.values()) {
            val config = Config(settingsLanguage = language)
            val result = configManager.getFieldValue(config, "settingsLanguage")
            assertEquals("Should return correct language enum name", language.name, result)
        }

        // Test all Theme enum values
        for (theme in Theme.values()) {
            val config = Config(settingsTheme = theme)
            val result = configManager.getFieldValue(config, "settingsTheme")
            assertEquals("Should return correct theme enum name", theme.name, result)
        }

        // Test all FuelMode enum values
        for (fuelMode in FuelMode.values()) {
            val config = Config(vehicleFuelMode = fuelMode)
            val result = configManager.getFieldValue(config, "vehicleFuelMode")
            assertEquals("Should return correct fuelMode enum name", fuelMode.name, result)
        }

        // Test all DriveMode enum values
        for (driveMode in DriveMode.values()) {
            val config = Config(vehicleDriveMode = driveMode)
            val result = configManager.getFieldValue(config, "vehicleDriveMode")
            assertEquals("Should return correct driveMode enum name", driveMode.name, result)
        }
    }

    // ========== isValidConfig Tests ==========

    @Test
    fun `isValidConfig returns false for null config`() {
        val result = configManager.isValidConfig(null)
        assertFalse("Should return false for null config", result)
    }

    @Test
    fun `isValidConfig returns false for empty config`() {
        val config = Config()
        val result = configManager.isValidConfig(config)
        assertFalse("Should return false for empty config", result)
    }

    @Test
    fun `isValidConfig returns false when language is null`() {
        val config = Config(
            settingsLanguage = null,
            settingsTheme = Theme.auto,
            vehicleFuelMode = FuelMode.electric,
            vehicleDriveMode = DriveMode.sport
        )
        val result = configManager.isValidConfig(config)
        assertFalse("Should return false when language is null", result)
    }

    @Test
    fun `isValidConfig returns false when theme is null`() {
        val config = Config(
            settingsLanguage = Language.en,
            settingsTheme = null,
            vehicleFuelMode = FuelMode.electric,
            vehicleDriveMode = DriveMode.sport
        )
        val result = configManager.isValidConfig(config)
        assertFalse("Should return false when theme is null", result)
    }

    @Test
    fun `isValidConfig returns false when fuelMode is null`() {
        val config = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.auto,
            vehicleFuelMode = null,
            vehicleDriveMode = DriveMode.sport
        )
        val result = configManager.isValidConfig(config)
        assertFalse("Should return false when fuelMode is null", result)
    }

    @Test
    fun `isValidConfig returns false when driveMode is null`() {
        val config = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.auto,
            vehicleFuelMode = FuelMode.electric,
            vehicleDriveMode = null
        )
        val result = configManager.isValidConfig(config)
        assertFalse("Should return false when driveMode is null", result)
    }

    @Test
    fun `isValidConfig returns true for complete valid config`() {
        val config = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.auto,
            settingsInterfaceShiftX = 0,
            settingsInterfaceShiftY = 0,
            vehicleFuelMode = FuelMode.electric,
            vehicleDriveMode = DriveMode.sport
        )
        val result = configManager.isValidConfig(config)
        assertTrue("Should return true for complete valid config", result)
    }

    @Test
    fun `isValidConfig returns false when interface shift fields are null`() {
        val config = Config(
            settingsLanguage = Language.ru,
            settingsTheme = Theme.dark,
            settingsInterfaceShiftX = null,  // Now required field
            settingsInterfaceShiftY = null,  // Now required field
            vehicleFuelMode = FuelMode.fuel,
            vehicleDriveMode = DriveMode.comfort
        )
        val result = configManager.isValidConfig(config)
        assertFalse("Should return false when interface shift fields are null", result)
    }

    @Test
    fun `isValidConfig validates all enum combinations`() {
        // Test with different valid enum combinations
        val validCombinations = listOf(
            listOf(Language.en, Theme.auto, FuelMode.intellectual, DriveMode.comfort),
            listOf(Language.ru, Theme.light, FuelMode.electric, DriveMode.sport),
            listOf(Language.en, Theme.dark, FuelMode.fuel, DriveMode.eco)
        )

        for (combination in validCombinations) {
            val config = Config(
                settingsLanguage = combination[0] as Language,
                settingsTheme = combination[1] as Theme,
                settingsInterfaceShiftX = 0,
                settingsInterfaceShiftY = 0,
                vehicleFuelMode = combination[2] as FuelMode,
                vehicleDriveMode = combination[3] as DriveMode
            )
            val result = configManager.isValidConfig(config)
            assertTrue("Should return true for valid combination: ${combination.joinToString()}", result)
        }
    }

    @Test
    fun `isValidConfig returns true with interface shifts set`() {
        val config = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.auto,
            settingsInterfaceShiftX = 10,
            settingsInterfaceShiftY = -5,
            vehicleFuelMode = FuelMode.electric,
            vehicleDriveMode = DriveMode.sport
        )
        val result = configManager.isValidConfig(config)
        assertTrue("Should return true with interface shifts set", result)
    }

    // ========== hasDiffAnyChanges Tests ==========

    @Test
    fun `hasDiffAnyChanges returns false for null diff`() {
        val result = configManager.hasDiffAnyChanges(null)
        assertFalse("Should return false for null diff", result)
    }

    @Test
    fun `hasDiffAnyChanges returns false for empty diff`() {
        val diff = Config()
        val result = configManager.hasDiffAnyChanges(diff)
        assertFalse("Should return false for empty diff with all null fields", result)
    }

    @Test
    fun `hasDiffAnyChanges returns true when only language changed`() {
        val diff = Config(settingsLanguage = Language.ru)
        val result = configManager.hasDiffAnyChanges(diff)
        assertTrue("Should return true when language field is non-null", result)
    }

    @Test
    fun `hasDiffAnyChanges returns true when only theme changed`() {
        val diff = Config(settingsTheme = Theme.dark)
        val result = configManager.hasDiffAnyChanges(diff)
        assertTrue("Should return true when theme field is non-null", result)
    }

    @Test
    fun `hasDiffAnyChanges returns true when only interfaceShiftX changed`() {
        val diff = Config(settingsInterfaceShiftX = 100)
        val result = configManager.hasDiffAnyChanges(diff)
        assertTrue("Should return true when interfaceShiftX field is non-null", result)
    }

    @Test
    fun `hasDiffAnyChanges returns true when only interfaceShiftY changed`() {
        val diff = Config(settingsInterfaceShiftY = -50)
        val result = configManager.hasDiffAnyChanges(diff)
        assertTrue("Should return true when interfaceShiftY field is non-null", result)
    }

    @Test
    fun `hasDiffAnyChanges returns true when only fuelMode changed`() {
        val diff = Config(vehicleFuelMode = FuelMode.electric)
        val result = configManager.hasDiffAnyChanges(diff)
        assertTrue("Should return true when fuelMode field is non-null", result)
    }

    @Test
    fun `hasDiffAnyChanges returns true when only driveMode changed`() {
        val diff = Config(vehicleDriveMode = DriveMode.sport)
        val result = configManager.hasDiffAnyChanges(diff)
        assertTrue("Should return true when driveMode field is non-null", result)
    }

    @Test
    fun `hasDiffAnyChanges returns true when multiple fields changed`() {
        val diff = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.light,
            vehicleFuelMode = FuelMode.fuel
        )
        val result = configManager.hasDiffAnyChanges(diff)
        assertTrue("Should return true when multiple fields are non-null", result)
    }

    @Test
    fun `hasDiffAnyChanges returns true when all fields changed`() {
        val diff = Config(
            settingsLanguage = Language.ru,
            settingsTheme = Theme.dark,
            settingsInterfaceShiftX = 25,
            settingsInterfaceShiftY = -10,
            vehicleFuelMode = FuelMode.save,
            vehicleDriveMode = DriveMode.individual
        )
        val result = configManager.hasDiffAnyChanges(diff)
        assertTrue("Should return true when all fields are non-null", result)
    }

    @Test
    fun `hasDiffAnyChanges works with zero values`() {
        val diff = Config(
            settingsInterfaceShiftX = 0,
            settingsInterfaceShiftY = 0
        )
        val result = configManager.hasDiffAnyChanges(diff)
        assertTrue("Should return true even when integer fields are zero (non-null)", result)
    }

    @Test
    fun `hasDiffAnyChanges handles mixed null and non-null fields`() {
        val diff = Config(
            settingsLanguage = null,
            settingsTheme = Theme.auto,
            settingsInterfaceShiftX = null,
            settingsInterfaceShiftY = null,
            vehicleFuelMode = null,
            vehicleDriveMode = null
        )
        val result = configManager.hasDiffAnyChanges(diff)
        assertTrue("Should return true when at least one field is non-null", result)
    }

    @Test
    fun `hasDiffAnyChanges is resilient to reflection errors`() {
        // This test ensures the method doesn't crash on edge cases
        // The method should handle any reflection errors gracefully
        val diff = Config()
        val result = configManager.hasDiffAnyChanges(diff)
        assertFalse("Should handle edge cases gracefully and return false for empty config", result)
    }

    // ========== Integration Tests ==========

    @Test
    fun `diff utility methods work together correctly`() {
        // Create a complete config
        val fullConfig = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.auto,
            settingsInterfaceShiftX = 0,
            settingsInterfaceShiftY = 0,
            vehicleFuelMode = FuelMode.electric,
            vehicleDriveMode = DriveMode.sport
        )

        // Create a diff with only some fields changed
        val diff = Config(
            settingsLanguage = Language.ru,
            settingsTheme = null,
            settingsInterfaceShiftX = null,
            settingsInterfaceShiftY = null,
            vehicleFuelMode = null,
            vehicleDriveMode = DriveMode.comfort
        )

        // Test isFieldChanged
        assertTrue("Language should be changed", configManager.isFieldChanged(diff, "settingsLanguage"))
        assertFalse("Theme should not be changed", configManager.isFieldChanged(diff, "settingsTheme"))
        assertFalse("FuelMode should not be changed", configManager.isFieldChanged(diff, "vehicleFuelMode"))
        assertTrue("DriveMode should be changed", configManager.isFieldChanged(diff, "vehicleDriveMode"))

        // Test getFieldValue on full config
        assertEquals("en", configManager.getFieldValue(fullConfig, "settingsLanguage"))
        assertEquals("auto", configManager.getFieldValue(fullConfig, "settingsTheme"))
        assertEquals("electric", configManager.getFieldValue(fullConfig, "vehicleFuelMode"))
        assertEquals("sport", configManager.getFieldValue(fullConfig, "vehicleDriveMode"))

        // Test getFieldValue on diff (only changed fields have values)
        assertEquals("ru", configManager.getFieldValue(diff, "settingsLanguage"))
        assertNull(configManager.getFieldValue(diff, "settingsTheme"))
        assertNull(configManager.getFieldValue(diff, "vehicleFuelMode"))
        assertEquals("comfort", configManager.getFieldValue(diff, "vehicleDriveMode"))

        // Test validation
        assertTrue("Full config should be valid", configManager.isValidConfig(fullConfig))
        assertFalse("Diff should not be valid (incomplete)", configManager.isValidConfig(diff))

        // Test hasDiffAnyChanges
        assertTrue("Diff should have changes", configManager.hasDiffAnyChanges(diff))
        assertFalse("Empty diff should have no changes", configManager.hasDiffAnyChanges(Config()))
    }

    @Test
    fun `hasDiffAnyChanges integrates correctly with diff workflow`() {
        // Simulate a typical diff workflow scenario
        val emptyDiff = Config()
        val partialDiff = Config(settingsLanguage = Language.ru)
        val fullDiff = Config(
            settingsLanguage = Language.en,
            settingsTheme = Theme.dark,
            settingsInterfaceShiftX = 10,
            settingsInterfaceShiftY = -5,
            vehicleFuelMode = FuelMode.electric,
            vehicleDriveMode = DriveMode.sport
        )

        // Test empty diff
        assertFalse("Empty diff should have no changes", configManager.hasDiffAnyChanges(emptyDiff))

        // Test partial diff
        assertTrue("Partial diff should have changes", configManager.hasDiffAnyChanges(partialDiff))
        assertTrue("Language should be changed in partial diff", configManager.isFieldChanged(partialDiff, "settingsLanguage"))
        assertFalse("Theme should not be changed in partial diff", configManager.isFieldChanged(partialDiff, "settingsTheme"))

        // Test full diff
        assertTrue("Full diff should have changes", configManager.hasDiffAnyChanges(fullDiff))
        assertTrue("All fields should be changed in full diff",
            configManager.isFieldChanged(fullDiff, "settingsLanguage") &&
            configManager.isFieldChanged(fullDiff, "settingsTheme") &&
            configManager.isFieldChanged(fullDiff, "settingsInterfaceShiftX") &&
            configManager.isFieldChanged(fullDiff, "settingsInterfaceShiftY") &&
            configManager.isFieldChanged(fullDiff, "vehicleFuelMode") &&
            configManager.isFieldChanged(fullDiff, "vehicleDriveMode"))
    }

    @Test
    fun `all field names work correctly with utility methods`() {
        val config = Config(
            settingsLanguage = Language.ru,
            settingsTheme = Theme.dark,
            settingsInterfaceShiftX = 25,
            settingsInterfaceShiftY = -10,
            vehicleFuelMode = FuelMode.save,
            vehicleDriveMode = DriveMode.individual
        )

        // Test all field names work with getFieldValue
        assertEquals("ru", configManager.getFieldValue(config, "settingsLanguage"))
        assertEquals("dark", configManager.getFieldValue(config, "settingsTheme"))
        assertEquals("25", configManager.getFieldValue(config, "settingsInterfaceShiftX"))
        assertEquals("-10", configManager.getFieldValue(config, "settingsInterfaceShiftY"))
        assertEquals("save", configManager.getFieldValue(config, "vehicleFuelMode"))
        assertEquals("individual", configManager.getFieldValue(config, "vehicleDriveMode"))

        // Test all field names work with isFieldChanged
        assertTrue("Language should be detected as changed", configManager.isFieldChanged(config, "settingsLanguage"))
        assertTrue("Theme should be detected as changed", configManager.isFieldChanged(config, "settingsTheme"))
        assertTrue("InterfaceShiftX should be detected as changed", configManager.isFieldChanged(config, "settingsInterfaceShiftX"))
        assertTrue("InterfaceShiftY should be detected as changed", configManager.isFieldChanged(config, "settingsInterfaceShiftY"))
        assertTrue("FuelMode should be detected as changed", configManager.isFieldChanged(config, "vehicleFuelMode"))
        assertTrue("DriveMode should be detected as changed", configManager.isFieldChanged(config, "vehicleDriveMode"))
    }
}