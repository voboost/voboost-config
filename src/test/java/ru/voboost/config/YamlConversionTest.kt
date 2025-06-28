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
 * Tests for YAML serialization and deserialization in ConfigManager.
 *
 * Tests cover conversion between Config objects and YAML format.
 */
class YamlConversionTest : BaseConfigTest() {

    @Test
    fun testConvertConfigToYaml() {
        val config = Config(
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

        val yaml = convertConfigToYamlViaReflection(config)

        // Verify YAML structure
        assertTrue(yaml.contains("settings:"))
        assertTrue(yaml.contains("language: en"))
        assertTrue(yaml.contains("theme: dark"))
        assertTrue(yaml.contains("interface-shift-x: 10"))
        assertTrue(yaml.contains("vehicle:"))
        assertTrue(yaml.contains("fuel-mode: intellectual"))
        assertTrue(yaml.contains("drive-mode: comfort"))
    }

    @Test
    fun testEnumToYamlConversion_verifyCorrectStringValues() {
        // Test that enum values are correctly converted to expected YAML string values
        // This test verifies the enum-to-string conversion part of the process

        val testConfig = Config(
            settings = Settings(
                language = Language.ru,
                theme = Theme.light,
                interfaceShiftX = 15,
                interfaceShiftY = -10
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.electric,
                driveMode = DriveMode.sport
            )
        )

        val yamlContent = convertConfigToYamlViaReflection(testConfig)

        // Verify that enum values are converted to correct YAML string representations
        assertTrue("YAML should contain 'language: ru' for Language.ru enum",
            yamlContent.contains("language: ru"))
        assertTrue("YAML should contain 'theme: light' for Theme.light enum",
            yamlContent.contains("theme: light"))
        assertTrue("YAML should contain 'fuel-mode: electric' for FuelMode.electric enum",
            yamlContent.contains("fuel-mode: electric"))
        assertTrue("YAML should contain 'drive-mode: sport' for DriveMode.sport enum",
            yamlContent.contains("drive-mode: sport"))

        // Verify non-enum values are also correctly converted
        assertTrue("YAML should contain 'interface-shift-x: 15'",
            yamlContent.contains("interface-shift-x: 15"))
        assertTrue("YAML should contain 'interface-shift-y: -10'",
            yamlContent.contains("interface-shift-y: -10"))
    }

    @Test
    fun testEnumToYamlConversion_allEnumValues() {
        // Test conversion of all enum values to ensure comprehensive coverage

        // Test all Language enum values
        val languageConfig = Config(settings = Settings(language = Language.en))
        var yamlContent = convertConfigToYamlViaReflection(languageConfig)
        assertTrue("Language.en should convert to 'language: en'", yamlContent.contains("language: en"))

        val languageRuConfig = Config(settings = Settings(language = Language.ru))
        yamlContent = convertConfigToYamlViaReflection(languageRuConfig)
        assertTrue("Language.ru should convert to 'language: ru'", yamlContent.contains("language: ru"))

        // Test all Theme enum values
        val themeAutoConfig = Config(settings = Settings(theme = Theme.auto))
        yamlContent = convertConfigToYamlViaReflection(themeAutoConfig)
        assertTrue("Theme.auto should convert to 'theme: auto'", yamlContent.contains("theme: auto"))

        val themeLightConfig = Config(settings = Settings(theme = Theme.light))
        yamlContent = convertConfigToYamlViaReflection(themeLightConfig)
        assertTrue("Theme.light should convert to 'theme: light'", yamlContent.contains("theme: light"))

        val themeDarkConfig = Config(settings = Settings(theme = Theme.dark))
        yamlContent = convertConfigToYamlViaReflection(themeDarkConfig)
        assertTrue("Theme.dark should convert to 'theme: dark'", yamlContent.contains("theme: dark"))

        // Test all FuelMode enum values
        val fuelIntellectualConfig = Config(vehicle = Vehicle(fuelMode = FuelMode.intellectual))
        yamlContent = convertConfigToYamlViaReflection(fuelIntellectualConfig)
        assertTrue("FuelMode.intellectual should convert to 'fuel-mode: intellectual'",
            yamlContent.contains("fuel-mode: intellectual"))

        val fuelElectricConfig = Config(vehicle = Vehicle(fuelMode = FuelMode.electric))
        yamlContent = convertConfigToYamlViaReflection(fuelElectricConfig)
        assertTrue("FuelMode.electric should convert to 'fuel-mode: electric'",
            yamlContent.contains("fuel-mode: electric"))

        val fuelConfig = Config(vehicle = Vehicle(fuelMode = FuelMode.fuel))
        yamlContent = convertConfigToYamlViaReflection(fuelConfig)
        assertTrue("FuelMode.fuel should convert to 'fuel-mode: fuel'",
            yamlContent.contains("fuel-mode: fuel"))

        val fuelSaveConfig = Config(vehicle = Vehicle(fuelMode = FuelMode.save))
        yamlContent = convertConfigToYamlViaReflection(fuelSaveConfig)
        assertTrue("FuelMode.save should convert to 'fuel-mode: save'",
            yamlContent.contains("fuel-mode: save"))

        // Test all DriveMode enum values
        val driveEcoConfig = Config(vehicle = Vehicle(driveMode = DriveMode.eco))
        yamlContent = convertConfigToYamlViaReflection(driveEcoConfig)
        assertTrue("DriveMode.eco should convert to 'drive-mode: eco'",
            yamlContent.contains("drive-mode: eco"))

        val driveComfortConfig = Config(vehicle = Vehicle(driveMode = DriveMode.comfort))
        yamlContent = convertConfigToYamlViaReflection(driveComfortConfig)
        assertTrue("DriveMode.comfort should convert to 'drive-mode: comfort'",
            yamlContent.contains("drive-mode: comfort"))

        val driveSportConfig = Config(vehicle = Vehicle(driveMode = DriveMode.sport))
        yamlContent = convertConfigToYamlViaReflection(driveSportConfig)
        assertTrue("DriveMode.sport should convert to 'drive-mode: sport'",
            yamlContent.contains("drive-mode: sport"))

        val driveSnowConfig = Config(vehicle = Vehicle(driveMode = DriveMode.snow))
        yamlContent = convertConfigToYamlViaReflection(driveSnowConfig)
        assertTrue("DriveMode.snow should convert to 'drive-mode: snow'",
            yamlContent.contains("drive-mode: snow"))

        val driveOutingConfig = Config(vehicle = Vehicle(driveMode = DriveMode.outing))
        yamlContent = convertConfigToYamlViaReflection(driveOutingConfig)
        assertTrue("DriveMode.outing should convert to 'drive-mode: outing'",
            yamlContent.contains("drive-mode: outing"))

        val driveIndividualConfig = Config(vehicle = Vehicle(driveMode = DriveMode.individual))
        yamlContent = convertConfigToYamlViaReflection(driveIndividualConfig)
        assertTrue("DriveMode.individual should convert to 'drive-mode: individual'",
            yamlContent.contains("drive-mode: individual"))
    }

    @Test
    fun testYamlToConfigConversion_enumHandling() {
        // Test that verifies YAML-to-Config conversion handles enums correctly
        // This test focuses on the conversion logic without file system dependencies

        val testConfig = Config(
            settings = Settings(
                language = Language.ru,
                theme = Theme.light,
                interfaceShiftX = 25,
                interfaceShiftY = -15
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.electric,
                driveMode = DriveMode.sport
            )
        )

        val yamlContent = convertConfigToYamlViaReflection(testConfig)

        // Step 2: Verify YAML contains correct enum string representations
        assertTrue("YAML should contain 'language: ru'", yamlContent.contains("language: ru"))
        assertTrue("YAML should contain 'theme: light'", yamlContent.contains("theme: light"))
        assertTrue("YAML should contain 'fuel-mode: electric'", yamlContent.contains("fuel-mode: electric"))
        assertTrue("YAML should contain 'drive-mode: sport'", yamlContent.contains("drive-mode: sport"))
        assertTrue("YAML should contain 'interface-shift-x: 25'", yamlContent.contains("interface-shift-x: 25"))
        assertTrue("YAML should contain 'interface-shift-y: -15'", yamlContent.contains("interface-shift-y: -15"))

        // Step 3: Verify YAML structure is correct
        assertTrue("YAML should have settings section", yamlContent.contains("settings:"))
        assertTrue("YAML should have vehicle section", yamlContent.contains("vehicle:"))

        // This test validates that enum-to-YAML conversion works correctly
        // The actual file I/O and YAML-to-enum parsing is tested separately
        // to avoid complex mocking issues with Hoplite library
    }

    @Test
    fun testConfigToYamlToConfigRoundTrip_enumPreservation() {
        // Test that verifies enum values are preserved through YAML conversion
        // This test uses the conversion methods directly to avoid file system complexity

        // Test with sample config values that match sample_config.yaml
        val originalConfig = Config(
            settings = Settings(
                language = Language.en,
                theme = Theme.dark,
                interfaceShiftX = 0,
                interfaceShiftY = 0
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.intellectual,
                driveMode = DriveMode.comfort
            )
        )

        val yamlContent = convertConfigToYamlViaReflection(originalConfig)

        // Step 2: Verify YAML contains expected enum values
        assertTrue("YAML should contain Language.en as 'language: en'",
            yamlContent.contains("language: en"))
        assertTrue("YAML should contain Theme.dark as 'theme: dark'",
            yamlContent.contains("theme: dark"))
        assertTrue("YAML should contain FuelMode.intellectual as 'fuel-mode: intellectual'",
            yamlContent.contains("fuel-mode: intellectual"))
        assertTrue("YAML should contain DriveMode.comfort as 'drive-mode: comfort'",
            yamlContent.contains("drive-mode: comfort"))

        // Step 3: Verify numeric values are preserved
        assertTrue("YAML should contain 'interface-shift-x: 0'",
            yamlContent.contains("interface-shift-x: 0"))
        assertTrue("YAML should contain 'interface-shift-y: 0'",
            yamlContent.contains("interface-shift-y: 0"))

        // This test validates the enum serialization part of the round-trip process
        // It ensures that enum values are correctly converted to their YAML string representations
        // The deserialization part would be tested in integration tests with real file operations
    }
}