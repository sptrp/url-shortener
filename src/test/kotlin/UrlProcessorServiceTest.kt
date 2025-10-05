package com.iponomarev

import com.iponomarev.database.entity.UrlEntity
import com.iponomarev.service.UrlDatabaseService
import com.iponomarev.service.UrlProcessorService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UrlProcessorServiceTest {

    private val urlDatabaseService = mockk<UrlDatabaseService>()
    private val service = UrlProcessorService(urlDatabaseService)

    companion object {
        private const val URL = "https://example.com"
        private const val SHORT_CODE = "abc123"
    }

    @BeforeEach
    fun setUp() {
        every { urlDatabaseService.findByUrl(URL) } returns mockk<UrlEntity> {
            every { url } returns URL
            every { shortUrlCode } returns SHORT_CODE
        }
    }

    @Test
    fun `getShortURLCodeOrCreateNew returns existing code for known url`() {
        val shortURLCode = service.getShortURLCodeOrCreateNew(URL)

        assertEquals(SHORT_CODE, shortURLCode)
        verify(exactly = 1) { urlDatabaseService.findByUrl(URL) }
    }

    @Test
    fun `getShortURLCodeOrCreateNew inserts and returns new code for unknown url`() {
        val newUrl = "https://newurl.com"
        val newCode = "xyz789"

        every { urlDatabaseService.findByUrl(newUrl) } returns null
        every { urlDatabaseService.insertUrl(newUrl, any()) } returns mockk<UrlEntity> {
            every { url } returns newUrl
            every { shortUrlCode } returns newCode
        }

        val shortURLCode = service.getShortURLCodeOrCreateNew(newUrl)

        assertEquals(newCode, shortURLCode)
        verify { urlDatabaseService.findByUrl(newUrl) }
        verify { urlDatabaseService.insertUrl(newUrl, any()) }
    }
}