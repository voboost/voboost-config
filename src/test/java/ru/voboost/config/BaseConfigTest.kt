package ru.voboost.config

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import ru.voboost.config.models.Config
import java.io.File

/**
 * Base test class with common setup for ConfigManager tests.
 *
 * Provides shared mock objects and utility methods for all test classes.
 * Uses MockK for mocking Android Context.
 * Works with instance-based ConfigManager pattern.
 */
abstract class BaseConfigTest {
    protected lateinit var mockContext: Context
    protected lateinit var mockFilesDir: File
    protected lateinit var configManager: ConfigManager

    @Before
    fun setUp() {
        mockContext = mockk()
        mockFilesDir = mockk()

        every { mockContext.dataDir } returns mockFilesDir
        every { mockFilesDir.toString() } returns "/mock/data"
        every { mockFilesDir.absolutePath } returns "/mock/files"
        every { mockFilesDir.path } returns "/mock/files"

        // Create ConfigManager instance for testing
        configManager = ConfigManager(mockContext, "test-config.yaml")
    }

    @After
    fun tearDown() {
        // Clean up after each test
        configManager.stopWatching()
        unmockkAll()
    }

    /**
     * Helper method to get ConfigManager instance for tests
     */
    protected fun getConfigManagerInstance(): ConfigManager {
        return configManager
    }

    /**
     * Helper method to access private createDiff method via reflection
     */
    protected fun createDiffViaReflection(
        oldConfig: Config,
        newConfig: Config
    ): Config {
        val method = ConfigManager::class.java.getDeclaredMethod("createDiff", Config::class.java, Config::class.java)
        method.isAccessible = true
        return method.invoke(configManager, oldConfig, newConfig) as Config
    }

    /**
     * Helper method to access private convertConfigToYaml method via reflection
     */
    protected fun convertConfigToYamlViaReflection(config: Config): String {
        val method = ConfigManager::class.java.getDeclaredMethod("convertConfigToYaml", Config::class.java)
        method.isAccessible = true
        return method.invoke(configManager, config) as String
    }
}
