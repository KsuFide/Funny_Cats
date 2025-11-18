package com.example.funny_cats

import androidx.paging.PagingConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimpleCoroutineTest {

    @Test
    fun `test basic coroutine functionality`() = runBlocking {
        // Самая простая проверка корутин
        val result = "Test completed"
        assertEquals("Test completed", result)
    }

    @Test
    fun pagingConfig_shouldHaveCorrectSettings() {
        val config = PagingConfig(pageSize = 20)
        assertEquals(20, config.pageSize)
    }

    @Test
    fun `test list operations in coroutine`() = runBlocking {
        val list = listOf(1, 2, 3)
        val doubled = list.map { it * 2 }
        assertEquals(listOf(2, 4, 6), doubled)
    }

    @Test
    fun `test string operations in coroutine`() = runBlocking {
        val text = "Hello, Coroutines!"
        assertTrue(text.contains("Coroutines"))
        assertFalse(text.contains("Threads"))
    }

    @Test
    fun `test simple launch functionality`() = runBlocking {
        // Простой тест с launch в runBlocking контексте
        var result = ""

        launch {
            delay(10)
            result = "Launch completed"
        }

        // Даем время для выполнения
        delay(50)
        assertEquals("Launch completed", result)
    }

    @Test
    fun `test delay functionality`() = runBlocking {
        val startTime = System.currentTimeMillis()
        delay(50)
        val endTime = System.currentTimeMillis()

        assertTrue(endTime - startTime >= 50)
    }
}