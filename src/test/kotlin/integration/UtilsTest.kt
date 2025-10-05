package com.iponomarev.integration

import com.iponomarev.util.BASE62_CHARS
import com.iponomarev.util.formatShortUrl
import com.iponomarev.util.generateShortBase62Code
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UtilsTest {

    @Test
    fun `generateShortBase62Code returns consistent code for same path`() {
        val path = "/some/path"
        val code1 = generateShortBase62Code(path)
        val code2 = generateShortBase62Code(path)

        assertEquals(code1, code2)
    }

    @Test
    fun `generateShortBase62Code returns code of specified length`() {
        val path = "/another/path"
        val length = 8
        val code = generateShortBase62Code(path, length)

        assertEquals(length, code.length)
    }

    @Test
    fun `generateShortBase62Code returns only base62 characters`() {
        val path = "/test/path"
        val code = generateShortBase62Code(path)

        val allowedChars = BASE62_CHARS.toSet()
        assertTrue(code.all { it in allowedChars })
    }

    @Test
    fun `formatShortUrl returns host and short code combined`() {
        val inputUrl = "https://example.com"
        val shortCode = "abc123"

        val result = formatShortUrl(inputUrl, shortCode)

        val expectedHost = "$inputUrl/$shortCode"

        assertEquals(expectedHost, result)
    }
}