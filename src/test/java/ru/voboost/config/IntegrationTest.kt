package ru.voboost.config

import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.voboost.config.models.Config
import ru.voboost.config.models.DriveMode
import ru.voboost.config.models.FuelMode
import ru.voboost.config.models.Language
import ru.voboost.config.models.Theme
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
        val sampleConfigContent =
            """
            settings-language: "en"
            settings-theme: "dark"
            settings-interface-shift-x: 0
            settings-interface-shift-y: 0
            vehicle-fuel-mode: "electric"
            vehicle-drive-mode: "comfort"
            """.trimIndent()

        configFile.writeText(sampleConfigContent)

        // Mock context to return our temp directory
        every { mockContext.dataDir } returns tempDir

        // Create ConfigManager instance and load config
        val testConfigManager = ConfigManager(mockContext, "config.yaml")
        val loadResult = testConfigManager.loadConfig()
        assertTrue("Config should load successfully", loadResult.isSuccess)

        // Verify all fields are loaded correctly with flattened structure
        assertEquals("Language should be EN", "en", testConfigManager.getFieldValue("settingsLanguage"))
        assertEquals("Theme should be DARK", "dark", testConfigManager.getFieldValue("settingsTheme"))
        assertEquals("Interface shift X should be 0", "0", testConfigManager.getFieldValue("settingsInterfaceShiftX"))
        assertEquals("Interface shift Y should be 0", "0", testConfigManager.getFieldValue("settingsInterfaceShiftY"))
        assertEquals(
            "Fuel mode should be INTELLECTUAL",
            "electric",
            testConfigManager.getFieldValue("vehicleFuelMode")
        )
        assertEquals("Drive mode should be COMFORT", "comfort", testConfigManager.getFieldValue("vehicleDriveMode"))

        // Clean up
        tempDir.deleteRecursively()
    }

    @Test
    fun testLoadConfig_realYamlFile_allEnumValues() {
        // Test loading YAML with all possible enum values
        val tempDir = File(Files.createTempDirectory("enum_test").toString())
        val configFile = File(tempDir, "config.yaml")

        // Test with different enum values
        val configContent =
            """
            settings-language: "ru"
            settings-theme: "light"
            settings-interface-shift-x: 15
            settings-interface-shift-y: -10
            vehicle-fuel-mode: "electric"
            vehicle-drive-mode: "sport"
            """.trimIndent()

        configFile.writeText(configContent)
        every { mockContext.dataDir } returns tempDir

        // Create ConfigManager instance and load config
        val testConfigManager = ConfigManager(mockContext, "config.yaml")
        val loadResult = testConfigManager.loadConfig()
        assertTrue("Config should load successfully", loadResult.isSuccess)

        // Verify enum values are correctly parsed with flattened structure
        assertEquals("Language should be RU", "ru", testConfigManager.getFieldValue("settingsLanguage"))
        assertEquals("Theme should be LIGHT", "light", testConfigManager.getFieldValue("settingsTheme"))
        assertEquals("Interface shift X should be 15", "15", testConfigManager.getFieldValue("settingsInterfaceShiftX"))
        assertEquals(
            "Interface shift Y should be -10",
            "-10",
            testConfigManager.getFieldValue("settingsInterfaceShiftY")
        )
        assertEquals("Fuel mode should be ELECTRIC", "electric", testConfigManager.getFieldValue("vehicleFuelMode"))
        assertEquals("Drive mode should be SPORT", "sport", testConfigManager.getFieldValue("vehicleDriveMode"))

        tempDir.deleteRecursively()
    }

    @Test
    fun testSaveConfig_realYamlFile_writesCorrectFormat() {
        // Test saving config to YAML file with correct format using new API
        val tempDir = File(Files.createTempDirectory("save_test").toString())
        every { mockContext.dataDir } returns tempDir

        // Create ConfigManager instance
        val testConfigManager = ConfigManager(mockContext, "config.yaml")

        // First create the config file manually to simulate loading
        val configFile = File(tempDir, "config.yaml")
        val initialContent = """
            settings-language: ru
            settings-theme: dark
            settings-interface-shift-x: 25
            settings-interface-shift-y: -15
            vehicle-fuel-mode: electric
            vehicle-drive-mode: sport
        """.trimIndent()
        configFile.writeText(initialContent)

        // Load the config to set currentConfig
        val loadResult = testConfigManager.loadConfig()
        assertTrue("Config should load successfully", loadResult.isSuccess)

        // Save the config using new API (no parameters)
        val saveResult = testConfigManager.saveConfig()
        assertTrue("Config should save successfully", saveResult.isSuccess)

        // Verify the file was created and contains correct content
        assertTrue("Config file should exist", configFile.exists())

        val fileContent = configFile.readText()
        assertTrue("File should contain language setting", fileContent.contains("settings-language: ru"))
        assertTrue("File should contain theme setting", fileContent.contains("settings-theme: dark"))
        assertTrue("File should contain interface shift X", fileContent.contains("settings-interface-shift-x: 25"))
        assertTrue("File should contain interface shift Y", fileContent.contains("settings-interface-shift-y: -15"))
        assertTrue("File should contain fuel mode", fileContent.contains("vehicle-fuel-mode: electric"))
        assertTrue("File should contain drive mode", fileContent.contains("vehicle-drive-mode: sport"))

        tempDir.deleteRecursively()
    }

    @Test
    fun testSaveConfig_noConfigLoaded_returnsFailure() {
        // Test that saveConfig fails when no configuration is loaded
        val tempDir = File(Files.createTempDirectory("save_error_test").toString())
        every { mockContext.dataDir } returns tempDir

        // Create ConfigManager instance without loading any config
        val testConfigManager = ConfigManager(mockContext, "config.yaml")

        // Try to save without loading config first
        val saveResult = testConfigManager.saveConfig()
        assertTrue("Save should fail when no config is loaded", saveResult.isFailure)

        val exception = saveResult.exceptionOrNull()
        assertTrue("Exception should be IllegalStateException", exception is IllegalStateException)
        assertTrue(
            "Exception message should mention no configuration loaded",
            exception?.message?.contains("No configuration loaded") == true
        )

        tempDir.deleteRecursively()
    }
}
