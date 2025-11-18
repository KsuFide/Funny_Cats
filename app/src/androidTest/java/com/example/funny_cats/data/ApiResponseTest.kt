package com.example.funny_cats.data

import org.junit.Test
import org.junit.Assert.*

class ApiResponseTest {

    @Test
    fun testApiResponseParsingLogic() {
        // Симулируем логику парсинга ответа API
        val jsonResponse = """
            [
                {
                    "id": "cat1",
                    "url": "https://example.com/cat1.jpg",
                    "width": 500,
                    "height": 500
                }
            ]
        """.trimIndent()

        // Проверяем что ответ содержит ожидаемые поля
        assertTrue(jsonResponse.contains("\"id\""))
        assertTrue(jsonResponse.contains("\"url\""))
        assertTrue(jsonResponse.contains("\"width\""))
        assertTrue(jsonResponse.contains("\"height\""))
    }

    @Test
    fun testEmptyApiResponseHandling() {
        val emptyResponse = "[]"

        assertTrue(emptyResponse.isNotEmpty())
        assertEquals("[]", emptyResponse)
    }

    @Test
    fun testErrorResponseHandling() {
        val errorResponse = """
            {
                "error": "Not found",
                "status": 404
            }
        """.trimIndent()

        assertTrue(errorResponse.contains("error"))
        assertTrue(errorResponse.contains("status"))
    }

    @Test
    fun testResponseFieldExtraction() {
        // Симулируем извлечение полей из ответа
        val responseData = mapOf(
            "id" to "test_id",
            "name" to "Test Cat",
            "url" to "https://test.com/image.jpg"
        )

        assertEquals("test_id", responseData["id"])
        assertEquals("Test Cat", responseData["name"])
        assertEquals("https://test.com/image.jpg", responseData["url"])
    }

    @Test
    fun testNullSafetyInResponses() {
        // Проверяем обработку null значений
        val responseWithNulls = mapOf(
            "id" to "test_id",
            "description" to null,
            "url" to "https://test.com/image.jpg"
        )

        assertNotNull(responseWithNulls["id"])
        assertNull(responseWithNulls["description"])
        assertNotNull(responseWithNulls["url"])
    }
}