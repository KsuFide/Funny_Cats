package com.example.funny_cats.util

import org.junit.Assert.*
import org.junit.Test

class RussianTranslatorTest {
    @Test
    fun translateBreedName_shouldReturnRussianName() {
        assertEquals("Сиамская", RussianTranslator.translateBreedName("Siamese"))
    }
    @Test
    fun translateOrigin_shouldReturnRussianTranslation() {
        assertEquals("США", RussianTranslator.translateOrigin("United States"))
    }

    @Test
    fun translateTemperament_shouldTranslateMultipleTraits() {
        val result = RussianTranslator.translateTemperament("Active, Playful")
        assertTrue(result.contains("Активный") && result.contains("Игривый"))
    }
}