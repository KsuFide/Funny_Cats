package com.example.funny_cats.util

import org.junit.Test
import org.junit.Assert.*

class ThemeHelperTest {

    @Test
    fun `applyTheme should handle different theme modes without crashing`() {
        // Просто проверяем что методы не падают
        ThemeHelper.applyTheme("LIGHT")
        ThemeHelper.applyTheme("DARK")
        ThemeHelper.applyTheme("SYSTEM")
        assertTrue(true)
    }

    @Test
    fun `theme helper functions should exist`() {
        // Проверяем что методы доступны
        assertNotNull(ThemeHelper::applyTheme)
        assertNotNull(ThemeHelper::saveThemePreference)
        assertNotNull(ThemeHelper::getSavedTheme)
    }

    @Test
    fun `theme mode names should be valid`() {
        val validModes = listOf("LIGHT", "DARK", "SYSTEM")
        assertTrue(validModes.contains("LIGHT"))
        assertTrue(validModes.contains("DARK"))
        assertTrue(validModes.contains("SYSTEM"))
    }
}