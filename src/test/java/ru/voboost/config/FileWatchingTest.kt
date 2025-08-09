package ru.voboost.config

import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.voboost.config.models.Config
import ru.voboost.config.models.FuelMode
import ru.voboost.config.models.Language
import ru.voboost.config.models.Theme
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Tests for file watching functionality in ConfigManager.
 *
 * Tests cover file watching operations and listener callbacks.
 */
class FileWatchingTest : BaseConfigTest() {
    @Test
    fun testStartWatching_nonExistentFile_returnsFailure() {
        // Test that startWatching fails when file doesn't exist
        // Create a real temporary directory for testing
        val tempDir = File(Files.createTempDirectory("test").toString())

        // Create ConfigManager with temp directory
        val testConfigManager = ConfigManager(mockContext, "nonexistent.yaml")
        every { mockContext.dataDir } returns tempDir

        val listener =
            object : OnConfigChangeListener {
                override fun onConfigChanged(
                    newConfig: Config,
                    diff: Config
                ) {
                    // Should not be called
                }

                override fun onConfigError(error: Exception) {
                    // Expected to be called
                }
            }

        // Try to start watching with non-existent file - this should return failure
        val result = testConfigManager.startWatching(listener)
        assertTrue("startWatching should fail for non-existent file", result.isFailure)

        // Clean up
        tempDir.deleteRecursively()
    }

    @Test
    fun testStopWatching_safeToCallMultipleTimes() {
        // Test that stopWatching is safe to call multiple times
        val testConfigManager = getConfigManagerInstance()

        // This should not throw any exceptions
        testConfigManager.stopWatching()
        testConfigManager.stopWatching()
        testConfigManager.stopWatching()

        // If we get here without exceptions, the test passes
        assertTrue("stopWatching should be safe to call multiple times", true)
    }

    @Test
    fun testStopWatching_safeToCallWithoutStarting() {
        // Test that stopWatching is safe to call even if watching was never started
        val testConfigManager = getConfigManagerInstance()
        testConfigManager.stopWatching()

        // If we get here without exceptions, the test passes
        assertTrue("stopWatching should be safe to call without starting", true)
    }

    @Test
    fun testConfigChangeListener_interface() {
        // Test that OnConfigChangeListener interface works correctly
        var callbackInvoked = false
        var receivedNewConfig: Config? = null
        var receivedDiff: Config? = null

        val listener =
            object : OnConfigChangeListener {
                override fun onConfigChanged(
                    newConfig: Config,
                    diff: Config
                ) {
                    callbackInvoked = true
                    receivedNewConfig = newConfig
                    receivedDiff = diff
                }
            }

        // Simulate a callback (this tests the interface contract)
        val testConfig = Config(settingsLanguage = Language.en)
        val testDiff = Config(settingsLanguage = Language.ru)

        listener.onConfigChanged(testConfig, testDiff)

        assertTrue("Callback should be invoked", callbackInvoked)
        assertEquals("New config should be passed correctly", testConfig, receivedNewConfig)
        assertEquals("Diff should be passed correctly", testDiff, receivedDiff)
    }

    @Test
    fun testRealFileWatching_detectsFileChanges() {
        // Test real file watching with actual file modifications
        val tempDir = File(Files.createTempDirectory("filewatch_test").toString())
        val configFile = File(tempDir, "config.yaml")

        // Create initial config file
        val initialYaml =
            """
            settings-language: en
            settings-theme: light
            settings-interface-shift-x: 0
            settings-interface-shift-y: 0
            vehicle-fuel-mode: hybrid
            vehicle-drive-mode: comfort
            """.trimIndent()

        FileWriter(configFile).use { it.write(initialYaml) }

        // Create ConfigManager with temp directory
        val testConfigManager = ConfigManager(mockContext, "config.yaml")
        every { mockContext.dataDir } returns tempDir

        // Set up listener with CountDownLatch for synchronization
        val latch = CountDownLatch(1)
        var receivedNewConfig: Config? = null
        var receivedDiff: Config? = null

        val listener =
            object : OnConfigChangeListener {
                override fun onConfigChanged(
                    newConfig: Config,
                    diff: Config
                ) {
                    receivedNewConfig = newConfig
                    receivedDiff = diff
                    latch.countDown()
                }

                override fun onConfigError(error: Exception) {
                    // Handle errors if needed
                }
            }

        // Start watching
        val watchResult = testConfigManager.startWatching(listener)
        assertTrue("startWatching should succeed", watchResult.isSuccess)

        // Wait a bit for the watcher to initialize
        Thread.sleep(500)

        // Modify the file
        val modifiedYaml =
            """
            settings-language: ru
            settings-theme: dark
            settings-interface-shift-x: 10
            settings-interface-shift-y: -5
            vehicle-fuel-mode: electric
            vehicle-drive-mode: sport
            """.trimIndent()

        FileWriter(configFile).use { it.write(modifiedYaml) }

        // Wait for the callback (with timeout)
        val callbackReceived = latch.await(10, TimeUnit.SECONDS)
        assertTrue("File change callback should be received within 10 seconds", callbackReceived)

        // Verify the new config
        assertNotNull("New config should be received", receivedNewConfig)
        assertEquals("Language should be updated", Language.ru, receivedNewConfig?.settingsLanguage)
        assertEquals("Theme should be updated", Theme.dark, receivedNewConfig?.settingsTheme)
        assertEquals("Interface shift X should be updated", 10, receivedNewConfig?.settingsInterfaceShiftX)
        assertEquals("Interface shift Y should be updated", -5, receivedNewConfig?.settingsInterfaceShiftY)
        assertEquals("Fuel mode should be updated", FuelMode.electric, receivedNewConfig?.vehicleFuelMode)

        // Verify the diff contains only changed fields
        assertNotNull("Diff should be received", receivedDiff)
        assertEquals("Diff should contain language change", Language.ru, receivedDiff?.settingsLanguage)
        assertEquals("Diff should contain theme change", Theme.dark, receivedDiff?.settingsTheme)
        assertEquals("Diff should contain interface shift X change", 10, receivedDiff?.settingsInterfaceShiftX)
        assertEquals("Diff should contain interface shift Y change", -5, receivedDiff?.settingsInterfaceShiftY)
        assertEquals("Diff should contain fuel mode change", FuelMode.electric, receivedDiff?.vehicleFuelMode)

        // Stop watching
        testConfigManager.stopWatching()

        // Clean up
        tempDir.deleteRecursively()
    }
}
