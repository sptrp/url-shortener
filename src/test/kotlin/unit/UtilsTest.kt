package unit

import com.iponomarev.util.BASE62_CHARS
import com.iponomarev.util.formatShortUrl
import com.iponomarev.util.generateShortBase62Code
import com.iponomarev.util.normalizeUrlHost
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

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
        Assertions.assertTrue(code.all { it in allowedChars })
    }

    @Test
    fun `formatShortUrl returns host and short code combined`() {
        val inputUrl = "https://example.com"
        val shortCode = "abc123"

        val result = formatShortUrl(inputUrl, shortCode)

        val expectedHost = "$inputUrl/$shortCode"

        assertEquals(expectedHost, result)
    }

    @Test
    fun `normalizeUrlHost lowercases host but preserves path and query`() {
        val inputUrl = "HTTPS://ExAmPlE.CoM/Path/Page?Query=123#Fragment"
        val expected = "https://example.com/Path/Page?Query=123#Fragment"

        val normalized = normalizeUrlHost(inputUrl)

        assertEquals(expected, normalized)
    }

    @Test
    fun `normalizeUrlHost lowercases host with port and userinfo`() {
        val inputUrl = "http://User:Pass@EXAMPLE.com:8080/path"
        val expected = "http://User:Pass@example.com:8080/path"

        val normalized = normalizeUrlHost(inputUrl)

        assertEquals(expected, normalized)
    }

    @Test
    fun `normalizeUrlHost throws exception for invalid URI`() {
        val invalidUrl = "://missing.scheme.com"

        assertFails {
            normalizeUrlHost(invalidUrl)
        }
    }

    @Test
    fun `normalizeUrlHost works for url with no path`() {
        val inputUrl = "https://EXAMPLE.com"
        val expected = "https://example.com"

        val normalized = normalizeUrlHost(inputUrl)

        assertEquals(expected, normalized)
    }
}