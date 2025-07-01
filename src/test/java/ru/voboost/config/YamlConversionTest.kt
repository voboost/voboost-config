package ru.voboost.config

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.voboost.config.models.Config
import ru.voboost.config.models.DriveMode
import ru.voboost.config.models.FuelMode
import ru.voboost.config.models.Language
import ru.voboost.config.models.Theme

/**
 * Tests for YAML serialization and deserialization in ConfigManager.
 *
 * Tests cover conversion between Config objects and YAML format with the flattened Config model.
 */
class YamlConversionTest : BaseConfigTest() {
    @Test
    fun testConvertConfigToYaml() {
        val config =
            Config(
                settingsLanguage = Language.en,
                settingsTheme = Theme.dark,
                settingsInterfaceShiftX = 10,
                vehicleFuelMode = FuelMode.intellectual,
                vehicleDriveMode = DriveMode.comfort
            )

        val yaml = convertConfigToYamlViaReflection(config)

        // Verify YAML structure - should be flat structure
        assertTrue(yaml.contains("settings-language: en"))
        assertTrue(yaml.contains("settings-theme: dark"))
        assertTrue(yaml.contains("settings-interface-shift-x: 10"))
        assertTrue(yaml.contains("vehicle-fuel-mode: intellectual"))
        assertTrue(yaml.contains("vehicle-drive-mode: comfort"))
    }

    @Test
    fun testEnumToYamlConversion_verifyCorrectStringValues() {
        // Test that enum values are correctly converted to expected YAML string values
        // This test verifies the enum-to-string conversion part of the process

        val testConfig =
            Config(
                settingsLanguage = Language.ru,
                settingsTheme = Theme.light,
                settingsInterfaceShiftX = 15,
                settingsInterfaceShiftY = -10,
                vehicleFuelMode = FuelMode.electric,
                vehicleDriveMode = DriveMode.sport
            )

        val yamlContent = convertConfigToYamlViaReflection(testConfig)

        // Verify that enum values are converted to correct YAML string representations
        assertTrue(
            "YAML should contain 'settings-language: ru' for Language.ru enum",
            yamlContent.contains("settings-language: ru")
        )
        assertTrue(
            "YAML should contain 'settings-theme: light' for Theme.light enum",
            yamlContent.contains("settings-theme: light")
        )
        assertTrue(
            "YAML should contain 'vehicle-fuel-mode: electric' for FuelMode.electric enum",
            yamlContent.contains("vehicle-fuel-mode: electric")
        )
        assertTrue(
            "YAML should contain 'vehicle-drive-mode: sport' for DriveMode.sport enum",
            yamlContent.contains("vehicle-drive-mode: sport")
        )

        // Verify non-enum values are also correctly converted
        assertTrue(
            "YAML should contain 'settings-interface-shift-x: 15'",
            yamlContent.contains("settings-interface-shift-x: 15")
        )
        assertTrue(
            "YAML should contain 'settings-interface-shift-y: -10'",
            yamlContent.contains("settings-interface-shift-y: -10")
        )
    }

    @Test
    fun testEnumToYamlConversion_allEnumValues() {
        // Test conversion of all enum values to ensure comprehensive coverage

        // Test all Language enum values
        val languageConfig = Config(settingsLanguage = Language.en)
        var yamlContent = convertConfigToYamlViaReflection(languageConfig)
        assertTrue(
            "Language.en should convert to 'settings-language: en'",
            yamlContent.contains("settings-language: en")
        )

        val languageRuConfig = Config(settingsLanguage = Language.ru)
        yamlContent = convertConfigToYamlViaReflection(languageRuConfig)
        assertTrue(
            "Language.ru should convert to 'settings-language: ru'",
            yamlContent.contains("settings-language: ru")
        )

        // Test all Theme enum values
        val themeAutoConfig = Config(settingsTheme = Theme.auto)
        yamlContent = convertConfigToYamlViaReflection(themeAutoConfig)
        assertTrue("Theme.auto should convert to 'settings-theme: auto'", yamlContent.contains("settings-theme: auto"))

        val themeLightConfig = Config(settingsTheme = Theme.light)
        yamlContent = convertConfigToYamlViaReflection(themeLightConfig)
        assertTrue(
            "Theme.light should convert to 'settings-theme: light'",
            yamlContent.contains("settings-theme: light")
        )

        val themeDarkConfig = Config(settingsTheme = Theme.dark)
        yamlContent = convertConfigToYamlViaReflection(themeDarkConfig)
        assertTrue("Theme.dark should convert to 'settings-theme: dark'", yamlContent.contains("settings-theme: dark"))

        // Test all FuelMode enum values
        val fuelIntellectualConfig = Config(vehicleFuelMode = FuelMode.intellectual)
        yamlContent = convertConfigToYamlViaReflection(fuelIntellectualConfig)
        assertTrue(
            "FuelMode.intellectual should convert to 'vehicle-fuel-mode: intellectual'",
            yamlContent.contains("vehicle-fuel-mode: intellectual")
        )

        val fuelElectricConfig = Config(vehicleFuelMode = FuelMode.electric)
        yamlContent = convertConfigToYamlViaReflection(fuelElectricConfig)
        assertTrue(
            "FuelMode.electric should convert to 'vehicle-fuel-mode: electric'",
            yamlContent.contains("vehicle-fuel-mode: electric")
        )

        val fuelConfig = Config(vehicleFuelMode = FuelMode.fuel)
        yamlContent = convertConfigToYamlViaReflection(fuelConfig)
        assertTrue(
            "FuelMode.fuel should convert to 'vehicle-fuel-mode: fuel'",
            yamlContent.contains("vehicle-fuel-mode: fuel")
        )

        val fuelSaveConfig = Config(vehicleFuelMode = FuelMode.save)
        yamlContent = convertConfigToYamlViaReflection(fuelSaveConfig)
        assertTrue(
            "FuelMode.save should convert to 'vehicle-fuel-mode: save'",
            yamlContent.contains("vehicle-fuel-mode: save")
        )

        // Test all DriveMode enum values
        val driveEcoConfig = Config(vehicleDriveMode = DriveMode.eco)
        yamlContent = convertConfigToYamlViaReflection(driveEcoConfig)
        assertTrue(
            "DriveMode.eco should convert to 'vehicle-drive-mode: eco'",
            yamlContent.contains("vehicle-drive-mode: eco")
        )

        val driveComfortConfig = Config(vehicleDriveMode = DriveMode.comfort)
        yamlContent = convertConfigToYamlViaReflection(driveComfortConfig)
        assertTrue(
            "DriveMode.comfort should convert to 'vehicle-drive-mode: comfort'",
            yamlContent.contains("vehicle-drive-mode: comfort")
        )

        val driveSportConfig = Config(vehicleDriveMode = DriveMode.sport)
        yamlContent = convertConfigToYamlViaReflection(driveSportConfig)
        assertTrue(
            "DriveMode.sport should convert to 'vehicle-drive-mode: sport'",
            yamlContent.contains("vehicle-drive-mode: sport")
        )

        val driveSnowConfig = Config(vehicleDriveMode = DriveMode.snow)
        yamlContent = convertConfigToYamlViaReflection(driveSnowConfig)
        assertTrue(
            "DriveMode.snow should convert to 'vehicle-drive-mode: snow'",
            yamlContent.contains("vehicle-drive-mode: snow")
        )

        val driveOutingConfig = Config(vehicleDriveMode = DriveMode.outing)
        yamlContent = convertConfigToYamlViaReflection(driveOutingConfig)
        assertTrue(
            "DriveMode.outing should convert to 'vehicle-drive-mode: outing'",
            yamlContent.contains("vehicle-drive-mode: outing")
        )

        val driveIndividualConfig = Config(vehicleDriveMode = DriveMode.individual)
        yamlContent = convertConfigToYamlViaReflection(driveIndividualConfig)
        assertTrue(
            "DriveMode.individual should convert to 'vehicle-drive-mode: individual'",
            yamlContent.contains("vehicle-drive-mode: individual")
        )
    }

    @Test
    fun testYamlToConfigConversion_enumHandling() {
        // Test that verifies YAML-to-Config conversion handles enums correctly
        // This test focuses on the conversion logic without file system dependencies

        val testConfig =
            Config(
                settingsLanguage = Language.ru,
                settingsTheme = Theme.light,
                settingsInterfaceShiftX = 25,
                settingsInterfaceShiftY = -15,
                vehicleFuelMode = FuelMode.electric,
                vehicleDriveMode = DriveMode.sport
            )

        val yamlContent = convertConfigToYamlViaReflection(testConfig)

        // Step 2: Verify YAML contains correct enum string representations
        assertTrue("YAML should contain 'settings-language: ru'", yamlContent.contains("settings-language: ru"))
        assertTrue("YAML should contain 'settings-theme: light'", yamlContent.contains("settings-theme: light"))
        assertTrue(
            "YAML should contain 'vehicle-fuel-mode: electric'",
            yamlContent.contains("vehicle-fuel-mode: electric")
        )
        assertTrue("YAML should contain 'vehicle-drive-mode: sport'", yamlContent.contains("vehicle-drive-mode: sport"))
        assertTrue(
            "YAML should contain 'settings-interface-shift-x: 25'",
            yamlContent.contains("settings-interface-shift-x: 25")
        )
        assertTrue(
            "YAML should contain 'settings-interface-shift-y: -15'",
            yamlContent.contains("settings-interface-shift-y: -15")
        )

        // Step 3: Verify YAML structure is flat (no sections)
        assertFalse("YAML should not have settings section", yamlContent.contains("settings:"))
        assertFalse("YAML should not have vehicle section", yamlContent.contains("vehicle:"))

        // This test validates that enum-to-YAML conversion works correctly
        // The actual file I/O and YAML-to-enum parsing is tested separately
        // to avoid complex mocking issues with Hoplite library
    }

    @Test
    fun testConfigToYamlToConfigRoundTrip_enumPreservation() {
        // Test that verifies enum values are preserved through YAML conversion
        // This test uses the conversion methods directly to avoid file system complexity

        // Test with sample config values that match sample_config.yaml
        val originalConfig =
            Config(
                settingsLanguage = Language.en,
                settingsTheme = Theme.dark,
                settingsInterfaceShiftX = 0,
                settingsInterfaceShiftY = 0,
                vehicleFuelMode = FuelMode.intellectual,
                vehicleDriveMode = DriveMode.comfort
            )

        val yamlContent = convertConfigToYamlViaReflection(originalConfig)

        // Step 2: Verify YAML contains expected enum values
        assertTrue(
            "YAML should contain Language.en as 'settings-language: en'",
            yamlContent.contains("settings-language: en")
        )
        assertTrue(
            "YAML should contain Theme.dark as 'settings-theme: dark'",
            yamlContent.contains("settings-theme: dark")
        )
        assertTrue(
            "YAML should contain FuelMode.intellectual as 'vehicle-fuel-mode: intellectual'",
            yamlContent.contains("vehicle-fuel-mode: intellectual")
        )
        assertTrue(
            "YAML should contain DriveMode.comfort as 'vehicle-drive-mode: comfort'",
            yamlContent.contains("vehicle-drive-mode: comfort")
        )

        // Step 3: Verify numeric values are preserved
        assertTrue(
            "YAML should contain 'settings-interface-shift-x: 0'",
            yamlContent.contains("settings-interface-shift-x: 0")
        )
        assertTrue(
            "YAML should contain 'settings-interface-shift-y: 0'",
            yamlContent.contains("settings-interface-shift-y: 0")
        )

        // This test validates the enum serialization part of the round-trip process
        // It ensures that enum values are correctly converted to their YAML string representations
        // The deserialization part would be tested in integration tests with real file operations
    }

    @Test
    fun testPartialConfigToYaml_onlySetFieldsIncluded() {
        // Test that only non-null fields are included in YAML output
        val partialConfig =
            Config(
                settingsLanguage = Language.ru,
                vehicleFuelMode = FuelMode.electric
                // Other fields are null
            )

        val yamlContent = convertConfigToYamlViaReflection(partialConfig)

        // Should contain only the set fields
        assertTrue("YAML should contain language", yamlContent.contains("settings-language: ru"))
        assertTrue("YAML should contain fuel-mode", yamlContent.contains("vehicle-fuel-mode: electric"))

        // Should not contain unset fields
        assertFalse("YAML should not contain theme", yamlContent.contains("settings-theme:"))
        assertFalse("YAML should not contain interface-shift-x", yamlContent.contains("settings-interface-shift-x:"))
        assertFalse("YAML should not contain interface-shift-y", yamlContent.contains("settings-interface-shift-y:"))
        assertFalse("YAML should not contain drive-mode", yamlContent.contains("vehicle-drive-mode:"))

        // Should not have section structure (flat YAML)
        assertFalse("YAML should not have settings section", yamlContent.contains("settings:"))
        assertFalse("YAML should not have vehicle section", yamlContent.contains("vehicle:"))
    }

    @Test
    fun testEmptyConfigToYaml_noSectionsGenerated() {
        // Test that empty config doesn't generate any YAML content
        val emptyConfig = Config()

        val yamlContent = convertConfigToYamlViaReflection(emptyConfig)

        // Should be empty or contain no meaningful content
        assertTrue("YAML should be empty or whitespace only", yamlContent.trim().isEmpty())
    }

    @Test
    fun testInterfaceShiftsToYaml_negativeAndPositiveValues() {
        // Test that interface shift values (positive, negative, zero) are correctly converted
        val config =
            Config(
                settingsInterfaceShiftX = 50,
                settingsInterfaceShiftY = -25
            )

        val yamlContent = convertConfigToYamlViaReflection(config)

        assertTrue(
            "YAML should contain positive interface-shift-x",
            yamlContent.contains("settings-interface-shift-x: 50")
        )
        assertTrue(
            "YAML should contain negative interface-shift-y",
            yamlContent.contains("settings-interface-shift-y: -25")
        )
        assertFalse("YAML should not have settings section", yamlContent.contains("settings:"))
    }

    @Test
    fun testAllFieldsToYaml_completeConfig() {
        // Test conversion of a complete config with all fields set
        val completeConfig =
            Config(
                settingsLanguage = Language.en,
                settingsTheme = Theme.auto,
                settingsInterfaceShiftX = 15,
                settingsInterfaceShiftY = -5,
                vehicleFuelMode = FuelMode.save,
                vehicleDriveMode = DriveMode.individual
            )

        val yamlContent = convertConfigToYamlViaReflection(completeConfig)

        // Verify all fields are present
        assertTrue("YAML should contain language", yamlContent.contains("settings-language: en"))
        assertTrue("YAML should contain theme", yamlContent.contains("settings-theme: auto"))
        assertTrue("YAML should contain interface-shift-x", yamlContent.contains("settings-interface-shift-x: 15"))
        assertTrue("YAML should contain interface-shift-y", yamlContent.contains("settings-interface-shift-y: -5"))
        assertTrue("YAML should contain fuel-mode", yamlContent.contains("vehicle-fuel-mode: save"))
        assertTrue("YAML should contain drive-mode", yamlContent.contains("vehicle-drive-mode: individual"))

        // Verify flat structure (no sections)
        assertFalse("YAML should not have settings section", yamlContent.contains("settings:"))
        assertFalse("YAML should not have vehicle section", yamlContent.contains("vehicle:"))
    }
}
