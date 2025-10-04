package com.iponomarev.integrationTests.database

import com.iponomarev.database.table.Urls
import com.iponomarev.service.UrlDatabaseService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UrlDatabaseServiceIntegrationTest {

    private lateinit var urlDatabaseService: UrlDatabaseService

    @BeforeAll
    fun setupDatabase() {
        // Connect to in-memory H2 for testing
        Database.Companion.connect(
            "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver"
        )

        transaction {
            SchemaUtils.create(Urls) // Create the Urls table before tests
        }

        urlDatabaseService = UrlDatabaseService()
    }

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

        val inserted = urlDatabaseService.insertUrl(originalUrl, shortCode)
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
        urlDatabaseService.insertUrl(originalUrl, shortCode)

        val found = urlDatabaseService.findByUrl(originalUrl)
        Assertions.assertNotNull(found)
        Assertions.assertEquals(shortCode, found?.shortUrlCode)
    }

    @Test
    fun `test findByShortUrlCode returns inserted url`() {
        val originalUrl = "https://testurl.com"
        val shortCode = "xyz789"
        urlDatabaseService.insertUrl(originalUrl, shortCode)

        val found = urlDatabaseService.findByShortUrlCode(shortCode)
        Assertions.assertNotNull(found)
        Assertions.assertEquals(originalUrl, found?.url)
    }
}