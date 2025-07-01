package ru.voboost.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.voboost.config.models.Config
import ru.voboost.config.models.DriveMode
import ru.voboost.config.models.FuelMode
import ru.voboost.config.models.Language
import ru.voboost.config.models.Theme

/**
 * Tests for Config data model classes.
 *
 * Tests cover the flattened Config data class functionality with direct field access.
 */
class ConfigModelTest : BaseConfigTest() {
    @Test
    fun testConfig_creation() {
        val config =
            Config(
                settingsLanguage = Language.en,
                settingsTheme = Theme.dark,
                vehicleFuelMode = FuelMode.electric,
                vehicleDriveMode = DriveMode.sport
            )

        assertEquals("Language should be set", Language.en, config.settingsLanguage)
        assertEquals("Theme should be set", Theme.dark, config.settingsTheme)
        assertEquals("Fuel mode should be set", FuelMode.electric, config.vehicleFuelMode)
        assertEquals("Drive mode should be set", DriveMode.sport, config.vehicleDriveMode)
        assertNull("Unset fields should be null", config.settingsInterfaceShiftX)
        assertNull("Unset fields should be null", config.settingsInterfaceShiftY)
    }

    @Test
    fun testConfig_allFieldsNull() {
        val config = Config()

        assertNull("All fields should be null by default", config.settingsLanguage)
        assertNull("All fields should be null by default", config.settingsTheme)
        assertNull("All fields should be null by default", config.settingsInterfaceShiftX)
        assertNull("All fields should be null by default", config.settingsInterfaceShiftY)
        assertNull("All fields should be null by default", config.vehicleFuelMode)
        assertNull("All fields should be null by default", config.vehicleDriveMode)
    }

    @Test
    fun testConfig_partialFields() {
        val config =
            Config(
                settingsLanguage = Language.ru,
                settingsInterfaceShiftX = 10
            )

        assertEquals("Language should be set", Language.ru, config.settingsLanguage)
        assertEquals("Interface shift X should be set", 10, config.settingsInterfaceShiftX)
        assertNull("Unset fields should be null", config.settingsTheme)
        assertNull("Unset fields should be null", config.settingsInterfaceShiftY)
        assertNull("Unset fields should be null", config.vehicleFuelMode)
        assertNull("Unset fields should be null", config.vehicleDriveMode)
    }

    @Test
    fun testConfig_equality() {
        val config1 =
            Config(
                settingsLanguage = Language.en,
                settingsTheme = Theme.auto,
                vehicleFuelMode = FuelMode.intellectual
            )

        val config2 =
            Config(
                settingsLanguage = Language.en,
                settingsTheme = Theme.auto,
                vehicleFuelMode = FuelMode.intellectual
            )

        val config3 =
            Config(
                settingsLanguage = Language.ru,
                settingsTheme = Theme.auto,
                vehicleFuelMode = FuelMode.intellectual
            )

        assertEquals("Configs with same values should be equal", config1, config2)
        assertNotEquals("Configs with different values should not be equal", config1, config3)
    }

    @Test
    fun testConfig_copy() {
        val original =
            Config(
                settingsLanguage = Language.en,
                settingsTheme = Theme.dark,
                settingsInterfaceShiftX = 5,
                vehicleFuelMode = FuelMode.electric
            )

        val copy = original.copy(settingsLanguage = Language.ru)

        assertEquals("Copied field should be changed", Language.ru, copy.settingsLanguage)
        assertEquals("Other fields should remain the same", Theme.dark, copy.settingsTheme)
        assertEquals("Other fields should remain the same", 5, copy.settingsInterfaceShiftX)
        assertEquals("Other fields should remain the same", FuelMode.electric, copy.vehicleFuelMode)
        assertNull("Unset fields should remain null", copy.settingsInterfaceShiftY)
        assertNull("Unset fields should remain null", copy.vehicleDriveMode)
    }

    @Test
    fun testEnums_allValues() {
        // Test Language enum
        assertEquals("Language enum should have correct values", 2, Language.values().size)
        assertTrue("Language should contain ru", Language.values().contains(Language.ru))
        assertTrue("Language should contain en", Language.values().contains(Language.en))

        // Test Theme enum
        assertEquals("Theme enum should have correct values", 3, Theme.values().size)
        assertTrue("Theme should contain auto", Theme.values().contains(Theme.auto))
        assertTrue("Theme should contain light", Theme.values().contains(Theme.light))
        assertTrue("Theme should contain dark", Theme.values().contains(Theme.dark))

        // Test FuelMode enum
        assertEquals("FuelMode enum should have correct values", 4, FuelMode.values().size)
        assertTrue("FuelMode should contain intellectual", FuelMode.values().contains(FuelMode.intellectual))
        assertTrue("FuelMode should contain electric", FuelMode.values().contains(FuelMode.electric))
        assertTrue("FuelMode should contain fuel", FuelMode.values().contains(FuelMode.fuel))
        assertTrue("FuelMode should contain save", FuelMode.values().contains(FuelMode.save))

        // Test DriveMode enum
        assertEquals("DriveMode enum should have correct values", 6, DriveMode.values().size)
        assertTrue("DriveMode should contain eco", DriveMode.values().contains(DriveMode.eco))
        assertTrue("DriveMode should contain comfort", DriveMode.values().contains(DriveMode.comfort))
        assertTrue("DriveMode should contain sport", DriveMode.values().contains(DriveMode.sport))
        assertTrue("DriveMode should contain snow", DriveMode.values().contains(DriveMode.snow))
        assertTrue("DriveMode should contain outing", DriveMode.values().contains(DriveMode.outing))
        assertTrue("DriveMode should contain individual", DriveMode.values().contains(DriveMode.individual))
    }
}
