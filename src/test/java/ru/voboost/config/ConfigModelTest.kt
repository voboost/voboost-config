package ru.voboost.config

import org.junit.Test
import org.junit.Assert.*
import ru.voboost.config.models.Config
import ru.voboost.config.models.Settings
import ru.voboost.config.models.Language
import ru.voboost.config.models.Theme

/**
 * Tests for Config data model classes.
 *
 * Tests cover Config, Settings, and Vehicle data class functionality.
 */
class ConfigModelTest : BaseConfigTest() {

    @Test
    fun testConfig_creation() {
        val config = Config(
            settings = Settings(
                language = Language.en,
                theme = Theme.dark
            )
        )

        assertEquals("Language should be set", Language.en, config.settings?.language)
        assertEquals("Theme should be set", Theme.dark, config.settings?.theme)
        assertNull("Unset fields should be null", config.vehicle)
    }

    @Test
    fun testConfig_allFieldsNull() {
        val config = Config()

        assertNull("All fields should be null by default", config.settings)
        assertNull("All fields should be null by default", config.vehicle)
    }
}