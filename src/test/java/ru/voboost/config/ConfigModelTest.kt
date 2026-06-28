package ru.voboost.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.voboost.components.i18n.Language
import ru.voboost.components.theme.Theme
import ru.voboost.config.models.Config
import ru.voboost.config.models.DriveMode
import ru.voboost.config.models.FuelMode

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
                settingsLanguage = Language.EN.getCode(),
                settingsTheme = Theme.FREE_DARK.getValue(),
                vehicleFuelMode = FuelMode.electric,
                vehicleDriveMode = DriveMode.sport,
            )

        assertEquals("Language should be set", Language.EN.getCode(), config.settingsLanguage)
        assertEquals("Theme should be set", Theme.FREE_DARK.getValue(), config.settingsTheme)
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
                settingsLanguage = Language.RU.getCode(),
                settingsInterfaceShiftX = 10,
            )

        assertEquals("Language should be set", Language.RU.getCode(), config.settingsLanguage)
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
                settingsLanguage = Language.EN.getCode(),
                settingsTheme = Theme.FREE_LIGHT.getValue(),
                vehicleFuelMode = FuelMode.electric,
            )

        val config2 =
            Config(
                settingsLanguage = Language.EN.getCode(),
                settingsTheme = Theme.FREE_LIGHT.getValue(),
                vehicleFuelMode = FuelMode.electric,
            )

        val config3 =
            Config(
                settingsLanguage = Language.RU.getCode(),
                settingsTheme = Theme.FREE_LIGHT.getValue(),
                vehicleFuelMode = FuelMode.electric,
            )

        assertEquals("Configs with same values should be equal", config1, config2)
        assertNotEquals("Configs with different values should not be equal", config1, config3)
    }

    @Test
    fun testConfig_copy() {
        val original =
            Config(
                settingsLanguage = Language.EN.getCode(),
                settingsTheme = Theme.FREE_DARK.getValue(),
                settingsInterfaceShiftX = 5,
                vehicleFuelMode = FuelMode.electric,
            )

        val copy = original.copy(settingsLanguage = Language.RU.getCode())

        assertEquals("Copied field should be changed", Language.RU.getCode(), copy.settingsLanguage)
        assertEquals("Other fields should remain the same", Theme.FREE_DARK.getValue(), copy.settingsTheme)
        assertEquals("Other fields should remain the same", 5, copy.settingsInterfaceShiftX)
        assertEquals("Other fields should remain the same", FuelMode.electric, copy.vehicleFuelMode)
        assertNull("Unset fields should remain null", copy.settingsInterfaceShiftY)
        assertNull("Unset fields should remain null", copy.vehicleDriveMode)
    }

    @Test
    fun testEnums_allValues() {
        // Test Language enum
        assertEquals("Language enum should have correct values", 2, Language.values().size)
        assertTrue("Language should contain ru", Language.values().contains(Language.RU))
        assertTrue("Language should contain en", Language.values().contains(Language.EN))

        // Test Theme enum
        assertEquals("Theme enum should have correct values", 4, Theme.values().size)
        assertTrue("Theme should contain free-light", Theme.values().contains(Theme.FREE_LIGHT))
        assertTrue("Theme should contain free-dark", Theme.values().contains(Theme.FREE_DARK))
        assertTrue("Theme should contain dreamer-light", Theme.values().contains(Theme.DREAMER_LIGHT))
        assertTrue("Theme should contain dreamer-dark", Theme.values().contains(Theme.DREAMER_DARK))

        // Test FuelMode enum
        assertEquals("FuelMode enum should have correct values", 4, FuelMode.values().size)
        assertTrue(
            "FuelMode should contain electric",
            FuelMode.values().contains(FuelMode.electric),
        )
        assertTrue(
            "FuelMode should contain electric_forced",
            FuelMode.values().contains(FuelMode.electric_forced),
        )
        assertTrue("FuelMode should contain hybrid", FuelMode.values().contains(FuelMode.hybrid))
        assertTrue("FuelMode should contain save", FuelMode.values().contains(FuelMode.save))

        // Test DriveMode enum
        assertEquals("DriveMode enum should have correct values", 6, DriveMode.values().size)
        assertTrue("DriveMode should contain eco", DriveMode.values().contains(DriveMode.eco))
        assertTrue(
            "DriveMode should contain comfort",
            DriveMode.values().contains(DriveMode.comfort),
        )
        assertTrue("DriveMode should contain sport", DriveMode.values().contains(DriveMode.sport))
        assertTrue("DriveMode should contain snow", DriveMode.values().contains(DriveMode.snow))
        assertTrue("DriveMode should contain outing", DriveMode.values().contains(DriveMode.outing))
        assertTrue(
            "DriveMode should contain individual",
            DriveMode.values().contains(DriveMode.individual),
        )
    }
}
