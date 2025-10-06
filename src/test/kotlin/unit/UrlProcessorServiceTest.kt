package unit

import com.iponomarev.config.ConfigProvider
import com.iponomarev.repository.UrlRepository
import com.iponomarev.repository.entity.UrlEntity
import com.iponomarev.service.UrlProcessorService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UrlProcessorServiceTest {

    private val urlRepository = mockk<UrlRepository>()
    private val configProvider = mockk<ConfigProvider>(relaxed = true) {
        every { appConfig.expirationTimeDays } returns 30
    }
    private val service = UrlProcessorService(urlRepository, configProvider)

    companion object {
        private const val URL = "https://example.com"
        private const val SHORT_CODE = "abc123"
    }

    @BeforeEach
    fun setUp() {
        every { urlRepository.findByUrl(URL) } returns mockk<UrlEntity> {
            every { url } returns URL
            every { shortUrlCode } returns SHORT_CODE
        }
    }

    @Test
    fun `getShortURLCodeOrCreateNew returns existing code for known url`() {
        val shortURLCode = service.getShortURLCodeOrCreateNew(URL)

        Assertions.assertEquals(SHORT_CODE, shortURLCode)
        verify(exactly = 1) { urlRepository.findByUrl(URL) }
    }

    @Test
    fun `getShortURLCodeOrCreateNew inserts and returns new code for unknown url`() {
        val newUrl = "https://newurl.com"
        val newCode = "xyz789"

        every { urlRepository.findByUrl(newUrl) } returns null
        every { urlRepository.insertUrl(newUrl, any(), any()) } returns mockk<UrlEntity> {
            every { url } returns newUrl
            every { shortUrlCode } returns newCode
        }

        val shortURLCode = service.getShortURLCodeOrCreateNew(newUrl)

        Assertions.assertEquals(newCode, shortURLCode)
        verify { urlRepository.findByUrl(newUrl) }
        verify { urlRepository.insertUrl(newUrl, any(), any()) }
    }
}