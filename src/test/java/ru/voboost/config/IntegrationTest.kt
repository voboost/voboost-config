package ru.voboost.config

import io.mockk.every
import org.junit.Test
import org.junit.Assert.*
import ru.voboost.config.models.Config
import ru.voboost.config.models.Language
import ru.voboost.config.models.Theme
import ru.voboost.config.models.FuelMode
import ru.voboost.config.models.DriveMode
import java.io.File
import java.nio.file.Files

/**
 * Integration tests with real YAML file loading for ConfigManager.
 *
 * Tests cover complete YAML->Config pipeline with real file operations using the flattened Config model.
 */
class IntegrationTest : BaseConfigTest() {

    @Test
    fun testLoadConfig_realYamlFile_sampleConfig() {
        // Test loading the actual sample_config.yaml file through Hoplite
        // This is an integration test that verifies the complete YAML->Config pipeline

        // Create a real temporary directory and copy sample config
        val tempDir = File(Files.createTempDirectory("integration_test").toString())
        val configFile = File(tempDir, "config.yaml")

        // Copy sample_config.yaml content to temp file
        val sampleConfigContent = """
            settings-language: "en"
            settings-theme: "dark"
            settings-interface-shift-x: 0
            settings-interface-shift-y: 0
            vehicle-fuel-mode: "intellectual"
            vehicle-drive-mode: "comfort"
        """.trimIndent()

        configFile.writeText(sampleConfigContent)

        // Mock context to return our temp directory
        every { mockContext.filesDir } returns tempDir

        // Test loading
        val result = configManager.loadConfig(mockContext, "config.yaml")

        assertTrue("Loading should succeed", result.isSuccess)
        val config = result.getOrNull()
        assertNotNull("Config should not be null", config)

        // Verify all fields are loaded correctly with flattened structure
        assertEquals("Language should be EN", Language.en, config?.settingsLanguage)
        assertEquals("Theme should be DARK", Theme.dark, config?.settingsTheme)
        assertEquals("Interface shift X should be 0", 0, config?.settingsInterfaceShiftX)
        assertEquals("Interface shift Y should be 0", 0, config?.settingsInterfaceShiftY)
        assertEquals("Fuel mode should be INTELLECTUAL", FuelMode.intellectual, config?.vehicleFuelMode)
        assertEquals("Drive mode should be COMFORT", DriveMode.comfort, config?.vehicleDriveMode)

        // Clean up
        tempDir.deleteRecursively()
    }

    @Test
    fun testLoadConfig_realYamlFile_allEnumValues() {
        // Test loading YAML with all possible enum values to ensure @ConfigAlias works

        val tempDir = File(Files.createTempDirectory("enum_test").toString())
        val configFile = File(tempDir, "config.yaml")

        // Test with different enum values
        val configContent = """
            settings-language: "ru"
            settings-theme: "auto"
            settings-interface-shift-x: 15
            settings-interface-shift-y: -10
            vehicle-fuel-mode: "electric"
            vehicle-drive-mode: "sport"
        """.trimIndent()

        configFile.writeText(configContent)

        every { mockContext.filesDir } returns tempDir

        val result = configManager.loadConfig(mockContext, "config.yaml")

        assertTrue("Loading should succeed", result.isSuccess)
        val config = result.getOrNull()
        assertNotNull("Config should not be null", config)

        // Verify enum values are correctly parsed with flattened structure
        assertEquals("Language should be RU", Language.ru, config?.settingsLanguage)
        assertEquals("Theme should be AUTO", Theme.auto, config?.settingsTheme)
        assertEquals("Interface shift X should be 15", 15, config?.settingsInterfaceShiftX)
        assertEquals("Interface shift Y should be -10", -10, config?.settingsInterfaceShiftY)
        assertEquals("Fuel mode should be ELECTRIC", FuelMode.electric, config?.vehicleFuelMode)
        assertEquals("Drive mode should be SPORT", DriveMode.sport, config?.vehicleDriveMode)

        tempDir.deleteRecursively()
    }

    @Test
    fun testLoadConfig_realYamlFile_partialConfig() {
        // Test loading YAML with only some fields set

        val tempDir = File(Files.createTempDirectory("partial_test").toString())
        val configFile = File(tempDir, "config.yaml")

        val configContent = """
            settings-language: "en"
            settings-theme: "light"
            vehicle-fuel-mode: "fuel"
        """.trimIndent()

        configFile.writeText(configContent)

        every { mockContext.filesDir } returns tempDir

        val result = configManager.loadConfig(mockContext, "config.yaml")

        assertTrue("Loading should succeed", result.isSuccess)
        val config = result.getOrNull()
        assertNotNull("Config should not be null", config)

        // Verify set fields with flattened structure
        assertEquals("Language should be EN", Language.en, config?.settingsLanguage)
        assertEquals("Theme should be LIGHT", Theme.light, config?.settingsTheme)
        assertEquals("Fuel mode should be FUEL", FuelMode.fuel, config?.vehicleFuelMode)

        // Verify unset fields are null
        assertNull("Interface shift X should be null", config?.settingsInterfaceShiftX)
        assertNull("Interface shift Y should be null", config?.settingsInterfaceShiftY)
        assertNull("Drive mode should be null", config?.vehicleDriveMode)

        tempDir.deleteRecursively()
    }

    @Test
    fun testLoadConfig_realYamlFile_invalidEnumValue() {
        // Test loading YAML with invalid enum value should fail

        val tempDir = File(Files.createTempDirectory("invalid_enum_test").toString())
        val configFile = File(tempDir, "config.yaml")

        val configContent = """
            settings-language: "invalid_language"
            settings-theme: "dark"
        """.trimIndent()

        configFile.writeText(configContent)

        every { mockContext.filesDir } returns tempDir

        val result = configManager.loadConfig(mockContext, "config.yaml")

        assertTrue("Loading should fail with invalid enum", result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception should be provided", exception)

        tempDir.deleteRecursively()
    }

    @Test
    fun testSaveAndLoadConfig_roundTrip() {
        // Test saving a config and then loading it back

        val tempDir = File(Files.createTempDirectory("roundtrip_test").toString())
        every { mockContext.filesDir } returns tempDir

        val originalConfig = Config(
            settingsLanguage = Language.ru,
            settingsTheme = Theme.dark,
            settingsInterfaceShiftX = 25,
            settingsInterfaceShiftY = -5,
            vehicleFuelMode = FuelMode.save,
            vehicleDriveMode = DriveMode.individual
        )

        // Save config
        val saveResult = configManager.saveConfig(mockContext, "roundtrip.yaml", originalConfig)
        assertTrue("Saving should succeed", saveResult.isSuccess)

        // Load config back
        val loadResult = configManager.loadConfig(mockContext, "roundtrip.yaml")
        assertTrue("Loading should succeed", loadResult.isSuccess)

        val loadedConfig = loadResult.getOrNull()
        assertNotNull("Loaded config should not be null", loadedConfig)

        // Verify all fields match with flattened structure
        assertEquals("Language should match", originalConfig.settingsLanguage, loadedConfig?.settingsLanguage)
        assertEquals("Theme should match", originalConfig.settingsTheme, loadedConfig?.settingsTheme)
        assertEquals("Interface shift X should match", originalConfig.settingsInterfaceShiftX, loadedConfig?.settingsInterfaceShiftX)
        assertEquals("Interface shift Y should match", originalConfig.settingsInterfaceShiftY, loadedConfig?.settingsInterfaceShiftY)
        assertEquals("Fuel mode should match", originalConfig.vehicleFuelMode, loadedConfig?.vehicleFuelMode)
        assertEquals("Drive mode should match", originalConfig.vehicleDriveMode, loadedConfig?.vehicleDriveMode)

        tempDir.deleteRecursively()
    }

    @Test
    fun testLoadConfig_realYamlFile_allDriveModes() {
        // Test all DriveMode enum values to ensure complete coverage

        val driveModes = listOf(
            "eco" to DriveMode.eco,
            "comfort" to DriveMode.comfort,
            "sport" to DriveMode.sport,
            "snow" to DriveMode.snow,
            "outing" to DriveMode.outing,
            "individual" to DriveMode.individual
        )

        driveModes.forEach { (yamlValue, expectedEnum) ->
            val tempDir = File(Files.createTempDirectory("drivemode_test_$yamlValue").toString())
            val configFile = File(tempDir, "config.yaml")

            val configContent = """
                vehicle-drive-mode: "$yamlValue"
            """.trimIndent()

            configFile.writeText(configContent)

            every { mockContext.filesDir } returns tempDir

            val result = configManager.loadConfig(mockContext, "config.yaml")

            assertTrue("Loading should succeed for $yamlValue", result.isSuccess)
            val config = result.getOrNull()
            assertEquals("Drive mode should be $expectedEnum for YAML value $yamlValue",
                expectedEnum, config?.vehicleDriveMode)

            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testLoadConfig_realYamlFile_allFuelModes() {
        // Test all FuelMode enum values to ensure complete coverage

        val fuelModes = listOf(
            "intellectual" to FuelMode.intellectual,
            "electric" to FuelMode.electric,
            "fuel" to FuelMode.fuel,
            "save" to FuelMode.save
        )

        fuelModes.forEach { (yamlValue, expectedEnum) ->
            val tempDir = File(Files.createTempDirectory("fuelmode_test_$yamlValue").toString())
            val configFile = File(tempDir, "config.yaml")

            val configContent = """
                vehicle-fuel-mode: "$yamlValue"
            """.trimIndent()

            configFile.writeText(configContent)

            every { mockContext.filesDir } returns tempDir

            val result = configManager.loadConfig(mockContext, "config.yaml")

            assertTrue("Loading should succeed for $yamlValue", result.isSuccess)
            val config = result.getOrNull()
            assertEquals("Fuel mode should be $expectedEnum for YAML value $yamlValue",
                expectedEnum, config?.vehicleFuelMode)

            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testLoadConfig_realYamlFile_allLanguages() {
        // Test all Language enum values

        val languages = listOf(
            "en" to Language.en,
            "ru" to Language.ru
        )

        languages.forEach { (yamlValue, expectedEnum) ->
            val tempDir = File(Files.createTempDirectory("language_test_$yamlValue").toString())
            val configFile = File(tempDir, "config.yaml")

            val configContent = """
                settings-language: "$yamlValue"
            """.trimIndent()

            configFile.writeText(configContent)

            every { mockContext.filesDir } returns tempDir

            val result = configManager.loadConfig(mockContext, "config.yaml")

            assertTrue("Loading should succeed for $yamlValue", result.isSuccess)
            val config = result.getOrNull()
            assertEquals("Language should be $expectedEnum for YAML value $yamlValue",
                expectedEnum, config?.settingsLanguage)

            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testLoadConfig_realYamlFile_allThemes() {
        // Test all Theme enum values

        val themes = listOf(
            "auto" to Theme.auto,
            "light" to Theme.light,
            "dark" to Theme.dark
        )

        themes.forEach { (yamlValue, expectedEnum) ->
            val tempDir = File(Files.createTempDirectory("theme_test_$yamlValue").toString())
            val configFile = File(tempDir, "config.yaml")

            val configContent = """
                settings-theme: "$yamlValue"
            """.trimIndent()

            configFile.writeText(configContent)

            every { mockContext.filesDir } returns tempDir

            val result = configManager.loadConfig(mockContext, "config.yaml")

            assertTrue("Loading should succeed for $yamlValue", result.isSuccess)
            val config = result.getOrNull()
            assertEquals("Theme should be $expectedEnum for YAML value $yamlValue",
                expectedEnum, config?.settingsTheme)

            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testLoadConfig_realYamlFile_interfaceShifts() {
        // Test interface shift values (positive, negative, zero)

        val tempDir = File(Files.createTempDirectory("interface_shift_test").toString())
        val configFile = File(tempDir, "config.yaml")

        val configContent = """
            settings-interface-shift-x: 100
            settings-interface-shift-y: -50
        """.trimIndent()

        configFile.writeText(configContent)

        every { mockContext.filesDir } returns tempDir

        val result = configManager.loadConfig(mockContext, "config.yaml")

        assertTrue("Loading should succeed", result.isSuccess)
        val config = result.getOrNull()
        assertNotNull("Config should not be null", config)

        assertEquals("Interface shift X should be 100", 100, config?.settingsInterfaceShiftX)
        assertEquals("Interface shift Y should be -50", -50, config?.settingsInterfaceShiftY)

        tempDir.deleteRecursively()
    }
}