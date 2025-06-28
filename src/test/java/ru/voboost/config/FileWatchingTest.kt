package ru.voboost.config

import io.mockk.every
import org.junit.Test
import org.junit.Assert.*
import ru.voboost.config.models.Config
import ru.voboost.config.models.Settings
import ru.voboost.config.models.Language
import ru.voboost.config.models.Theme
import ru.voboost.config.models.Vehicle
import ru.voboost.config.models.FuelMode
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

        // Mock the context to return our temp directory
        every { mockContext.filesDir } returns tempDir

        val listener = object : OnConfigChangeListener {
            override fun onConfigChanged(newConfig: Config, diff: Config) {
                // Should not be called
            }
        }

        val result = configManager.startWatching(mockContext, "nonexistent.yaml", listener)

        assertTrue("startWatching should fail for non-existent file", result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception should be provided", exception)
        assertTrue("Should be IllegalArgumentException", exception is IllegalArgumentException)
        assertTrue("Error message should mention file doesn't exist",
            exception?.message?.contains("Configuration file does not exist") == true)

        // Clean up
        tempDir.deleteRecursively()
    }

    @Test
    fun testStopWatching_safeToCallMultipleTimes() {
        // Test that stopWatching is safe to call multiple times
        // This should not throw any exceptions
        configManager.stopWatching()
        configManager.stopWatching()
        configManager.stopWatching()

        // If we get here without exceptions, the test passes
        assertTrue("stopWatching should be safe to call multiple times", true)
    }

    @Test
    fun testStopWatching_safeToCallWithoutStarting() {
        // Test that stopWatching is safe to call even if watching was never started
        val newConfigManager = ConfigManager()
        newConfigManager.stopWatching()

        // If we get here without exceptions, the test passes
        assertTrue("stopWatching should be safe to call without starting", true)
    }

    @Test
    fun testConfigChangeListener_interface() {
        // Test that OnConfigChangeListener interface works correctly
        var callbackInvoked = false
        var receivedNewConfig: Config? = null
        var receivedDiff: Config? = null

        val listener = object : OnConfigChangeListener {
            override fun onConfigChanged(newConfig: Config, diff: Config) {
                callbackInvoked = true
                receivedNewConfig = newConfig
                receivedDiff = diff
            }
        }

        // Simulate a callback (this tests the interface contract)
        val testConfig = Config(settings = Settings(language = Language.en))
        val testDiff = Config(settings = Settings(language = Language.ru))

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
        val initialYaml = """
            settings:
              language: en
              theme: light
            vehicle:
              fuel-mode: fuel
        """.trimIndent()

        FileWriter(configFile).use { it.write(initialYaml) }

        // Mock the context to return our temp directory
        every { mockContext.filesDir } returns tempDir

        // Set up listener with CountDownLatch for synchronization
        val latch = CountDownLatch(1)
        var receivedNewConfig: Config? = null
        var receivedDiff: Config? = null

        val listener = object : OnConfigChangeListener {
            override fun onConfigChanged(newConfig: Config, diff: Config) {
                receivedNewConfig = newConfig
                receivedDiff = diff
                latch.countDown()
            }
        }

        // Start watching
        val result = configManager.startWatching(mockContext, "config.yaml", listener)
        assertTrue("startWatching should succeed", result.isSuccess)

        // Wait a bit for the watcher to initialize
        Thread.sleep(500)

        // Modify the file
        val modifiedYaml = """
            settings:
              language: ru
              theme: dark
            vehicle:
              fuel-mode: electric
        """.trimIndent()

        FileWriter(configFile).use { it.write(modifiedYaml) }

        // Wait for the callback (with timeout)
        val callbackReceived = latch.await(10, TimeUnit.SECONDS)
        assertTrue("File change callback should be received within 10 seconds", callbackReceived)

        // Verify the new config
        assertNotNull("New config should be received", receivedNewConfig)
        assertEquals("Language should be updated", Language.ru, receivedNewConfig?.settings?.language)
        assertEquals("Theme should be updated", Theme.dark, receivedNewConfig?.settings?.theme)
        assertEquals("Fuel mode should be updated", FuelMode.electric, receivedNewConfig?.vehicle?.fuelMode)

        // Verify the diff contains only changed fields
        assertNotNull("Diff should be received", receivedDiff)
        assertEquals("Diff should contain language change", Language.ru, receivedDiff?.settings?.language)
        assertEquals("Diff should contain theme change", Theme.dark, receivedDiff?.settings?.theme)
        assertEquals("Diff should contain fuel mode change", FuelMode.electric, receivedDiff?.vehicle?.fuelMode)

        // Stop watching
        configManager.stopWatching()

        // Clean up
        tempDir.deleteRecursively()
    }
}
