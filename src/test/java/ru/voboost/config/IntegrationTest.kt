package ru.voboost.config

import io.mockk.every
import org.junit.Test
import org.junit.Assert.*
import ru.voboost.config.models.Config
import ru.voboost.config.models.Settings
import ru.voboost.config.models.Vehicle
import ru.voboost.config.models.Language
import ru.voboost.config.models.Theme
import ru.voboost.config.models.FuelMode
import ru.voboost.config.models.DriveMode
import java.io.File
import java.nio.file.Files

/**
 * Integration tests with real YAML file loading for ConfigManager.
 *
 * Tests cover complete YAML->Config pipeline with real file operations.
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
            settings:
              language: "en"
              theme: "dark"
              interface-shift-x: 0
              interface-shift-y: 0

            vehicle:
              fuel-mode: "intellectual"
              drive-mode: "comfort"
        """.trimIndent()

        configFile.writeText(sampleConfigContent)

        // Mock context to return our temp directory
        every { mockContext.filesDir } returns tempDir

        // Test loading
        val result = configManager.loadConfig(mockContext, "config.yaml")

        assertTrue("Loading should succeed", result.isSuccess)
        val config = result.getOrNull()
        assertNotNull("Config should not be null", config)

        // Verify all fields are loaded correctly
        assertEquals("Language should be EN", Language.en, config?.settings?.language)
        assertEquals("Theme should be DARK", Theme.dark, config?.settings?.theme)
        assertEquals("Interface shift X should be 0", 0, config?.settings?.interfaceShiftX)
        assertEquals("Interface shift Y should be 0", 0, config?.settings?.interfaceShiftY)
        assertEquals("Fuel mode should be INTELLECTUAL", FuelMode.intellectual, config?.vehicle?.fuelMode)
        assertEquals("Drive mode should be COMFORT", DriveMode.comfort, config?.vehicle?.driveMode)

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
            settings:
              language: "ru"
              theme: "auto"
              interface-shift-x: 15
              interface-shift-y: -10

            vehicle:
              fuel-mode: "electric"
              drive-mode: "sport"
        """.trimIndent()

        configFile.writeText(configContent)

        every { mockContext.filesDir } returns tempDir

        val result = configManager.loadConfig(mockContext, "config.yaml")

        assertTrue("Loading should succeed", result.isSuccess)
        val config = result.getOrNull()
        assertNotNull("Config should not be null", config)

        // Verify enum values are correctly parsed
        assertEquals("Language should be RU", Language.ru, config?.settings?.language)
        assertEquals("Theme should be AUTO", Theme.auto, config?.settings?.theme)
        assertEquals("Interface shift X should be 15", 15, config?.settings?.interfaceShiftX)
        assertEquals("Interface shift Y should be -10", -10, config?.settings?.interfaceShiftY)
        assertEquals("Fuel mode should be ELECTRIC", FuelMode.electric, config?.vehicle?.fuelMode)
        assertEquals("Drive mode should be SPORT", DriveMode.sport, config?.vehicle?.driveMode)

        tempDir.deleteRecursively()
    }

    @Test
    fun testLoadConfig_realYamlFile_partialConfig() {
        // Test loading YAML with only some fields set

        val tempDir = File(Files.createTempDirectory("partial_test").toString())
        val configFile = File(tempDir, "config.yaml")

        val configContent = """
            settings:
              language: "en"
              theme: "light"

            vehicle:
              fuel-mode: "fuel"
        """.trimIndent()

        configFile.writeText(configContent)

        every { mockContext.filesDir } returns tempDir

        val result = configManager.loadConfig(mockContext, "config.yaml")

        assertTrue("Loading should succeed", result.isSuccess)
        val config = result.getOrNull()
        assertNotNull("Config should not be null", config)

        // Verify set fields
        assertEquals("Language should be EN", Language.en, config?.settings?.language)
        assertEquals("Theme should be LIGHT", Theme.light, config?.settings?.theme)
        assertEquals("Fuel mode should be FUEL", FuelMode.fuel, config?.vehicle?.fuelMode)

        // Verify unset fields are null
        assertNull("Interface shift X should be null", config?.settings?.interfaceShiftX)
        assertNull("Interface shift Y should be null", config?.settings?.interfaceShiftY)
        assertNull("Drive mode should be null", config?.vehicle?.driveMode)

        tempDir.deleteRecursively()
    }

    @Test
    fun testLoadConfig_realYamlFile_invalidEnumValue() {
        // Test loading YAML with invalid enum value should fail

        val tempDir = File(Files.createTempDirectory("invalid_enum_test").toString())
        val configFile = File(tempDir, "config.yaml")

        val configContent = """
            settings:
              language: "invalid_language"
              theme: "dark"
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
            settings = Settings(
                language = Language.ru,
                theme = Theme.dark,
                interfaceShiftX = 25,
                interfaceShiftY = -5
            ),
            vehicle = Vehicle(
                fuelMode = FuelMode.save,
                driveMode = DriveMode.individual
            )
        )

        // Save config
        val saveResult = configManager.saveConfig(mockContext, "roundtrip.yaml", originalConfig)
        assertTrue("Saving should succeed", saveResult.isSuccess)

        // Load config back
        val loadResult = configManager.loadConfig(mockContext, "roundtrip.yaml")
        assertTrue("Loading should succeed", loadResult.isSuccess)

        val loadedConfig = loadResult.getOrNull()
        assertNotNull("Loaded config should not be null", loadedConfig)

        // Verify all fields match
        assertEquals("Language should match", originalConfig.settings?.language, loadedConfig?.settings?.language)
        assertEquals("Theme should match", originalConfig.settings?.theme, loadedConfig?.settings?.theme)
        assertEquals("Interface shift X should match", originalConfig.settings?.interfaceShiftX, loadedConfig?.settings?.interfaceShiftX)
        assertEquals("Interface shift Y should match", originalConfig.settings?.interfaceShiftY, loadedConfig?.settings?.interfaceShiftY)
        assertEquals("Fuel mode should match", originalConfig.vehicle?.fuelMode, loadedConfig?.vehicle?.fuelMode)
        assertEquals("Drive mode should match", originalConfig.vehicle?.driveMode, loadedConfig?.vehicle?.driveMode)

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
                vehicle:
                  drive-mode: "$yamlValue"
            """.trimIndent()

            configFile.writeText(configContent)

            every { mockContext.filesDir } returns tempDir

            val result = configManager.loadConfig(mockContext, "config.yaml")

            assertTrue("Loading should succeed for $yamlValue", result.isSuccess)
            val config = result.getOrNull()
            assertEquals("Drive mode should be $expectedEnum for YAML value $yamlValue",
                expectedEnum, config?.vehicle?.driveMode)

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
                vehicle:
                  fuel-mode: "$yamlValue"
            """.trimIndent()

            configFile.writeText(configContent)

            every { mockContext.filesDir } returns tempDir

            val result = configManager.loadConfig(mockContext, "config.yaml")

            assertTrue("Loading should succeed for $yamlValue", result.isSuccess)
            val config = result.getOrNull()
            assertEquals("Fuel mode should be $expectedEnum for YAML value $yamlValue",
                expectedEnum, config?.vehicle?.fuelMode)

            tempDir.deleteRecursively()
        }
    }
}