package integration.repository

import com.iponomarev.repository.table.Urls
import com.iponomarev.util.getNowOffSet
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import util.BaseIntegrationTest
import java.time.temporal.ChronoUnit

class UrlDatabaseServiceIntegrationTest : BaseIntegrationTest() {

    @BeforeEach
    fun cleanUp() {
        transaction {
            Urls.deleteAll()
        }
    }

    @Test
    fun `test insertUrl and findByShortUrlCode return correct values`() {
        val originalUrl = "https://example.com"
        val shortCode = "abc123"

        val inserted = urlDatabaseService.insertUrl(originalUrl, shortCode, getNowOffSet().plus(30, ChronoUnit.DAYS))
        Assertions.assertNotNull(inserted)
        Assertions.assertEquals(originalUrl, inserted.url)
        Assertions.assertEquals(shortCode, inserted.shortUrlCode)
    }

    @Test
    fun `test findByUrl returns null when not found`() {
        val found = urlDatabaseService.findByUrl("https://nonexistent.com")
        Assertions.assertNull(found)
    }

    @Test
    fun `test findByUrl returns inserted url`() {
        val originalUrl = "https://testurl.com"
        val shortCode = "xyz789"
        urlDatabaseService.insertUrl(originalUrl, shortCode, getNowOffSet().plus(30, ChronoUnit.DAYS))

        val found = urlDatabaseService.findByUrl(originalUrl)
        Assertions.assertNotNull(found)
        Assertions.assertEquals(shortCode, found?.shortUrlCode)
    }

    @Test
    fun `test findByShortUrlCode returns inserted url`() {
        val originalUrl = "https://testurl.com"
        val shortCode = "xyz789"
        urlDatabaseService.insertUrl(originalUrl, shortCode, getNowOffSet().plus(30, ChronoUnit.DAYS))

        val found = urlDatabaseService.findByShortUrlCode(shortCode)
        Assertions.assertNotNull(found)
        Assertions.assertEquals(originalUrl, found?.url)
    }

    @Test
    fun `test findByShortUrlCode returns null for expired url`() {
        val originalUrl = "https://expired-url.com"
        val shortCode = "expired123"
        val expiresAt = getNowOffSet().minus(1, ChronoUnit.DAYS)

        urlDatabaseService.insertUrl(originalUrl, shortCode, expiresAt)

        val found = urlDatabaseService.findByShortUrlCode(shortCode)
        Assertions.assertNull(found, "Expired URL should not be found")
    }

    @Test
    fun `test findByUrl returns null for expired url`() {
        val originalUrl = "https://expired-url.com"
        val shortCode = "expired456"
        val expiresAt = getNowOffSet().minus(5, ChronoUnit.DAYS)

        urlDatabaseService.insertUrl(originalUrl, shortCode, expiresAt)

        val found = urlDatabaseService.findByUrl(originalUrl)
        Assertions.assertNull(found, "Expired URL should not be found when searching by URL")
    }

    @Test
    fun `test deleteExpiredUrls removes only expired entries`() {
        val expiredUrl = "https://expired.com"
        val expiredCode = "exp123"
        urlDatabaseService.insertUrl(expiredUrl, expiredCode, getNowOffSet().minus(1, ChronoUnit.DAYS))

        val validUrl = "https://valid.com"
        val validCode = "val123"
        urlDatabaseService.insertUrl(validUrl, validCode, getNowOffSet().plus(30, ChronoUnit.DAYS))

        val deletedCount = urlDatabaseService.deleteExpiredUrls()

        Assertions.assertEquals(1, deletedCount, "Should delete 1 expired URL")

        val expiredFound = urlDatabaseService.findByShortUrlCode(expiredCode)
        Assertions.assertNull(expiredFound, "Expired URL should be deleted")

        val validFound = urlDatabaseService.findByShortUrlCode(validCode)
        Assertions.assertNotNull(validFound, "Valid URL should still exist")
    }

    @Test
    fun `test deleteExpiredUrls returns 0 when no expired entries`() {
        val validUrl = "https://valid.com"
        val validCode = "val123"
        urlDatabaseService.insertUrl(validUrl, validCode, getNowOffSet().plus(30, ChronoUnit.DAYS))

        val deletedCount = urlDatabaseService.deleteExpiredUrls()

        Assertions.assertEquals(0, deletedCount, "Should delete 0 URLs when none are expired")
    }

    @Test
    fun `test multiple urls with different expiration times`() {
        val expiresIn1Hour = getNowOffSet().plus(1, ChronoUnit.HOURS)
        val expiresIn30Days = getNowOffSet().plus(30, ChronoUnit.DAYS)
        val expired = getNowOffSet().minus(1, ChronoUnit.HOURS)

        urlDatabaseService.insertUrl("https://short-lived.com", "short1", expiresIn1Hour)
        urlDatabaseService.insertUrl("https://long-lived.com", "long1", expiresIn30Days)
        urlDatabaseService.insertUrl("https://already-expired.com", "exp1", expired)

        Assertions.assertNotNull(urlDatabaseService.findByShortUrlCode("short1"))
        Assertions.assertNotNull(urlDatabaseService.findByShortUrlCode("long1"))
        Assertions.assertNull(urlDatabaseService.findByShortUrlCode("exp1"))
    }
}