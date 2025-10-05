package integration.routing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.iponomarev.model.RequestDto
import com.iponomarev.model.ResponseDto
import com.iponomarev.repository.table.Urls
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import util.BaseIntegrationTest
import kotlin.test.assertEquals

class RoutingIntegrationTest : BaseIntegrationTest() {

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    companion object {
        private const val API_URL = "/api/v1"
        private const val POST_SHORT_URL = "$API_URL/shortUrl"
    }

    @BeforeEach
    fun cleanUp() {
        transaction {
            Urls.deleteAll()
        }
    }

    @Test
    fun `POST shortUrl returns shortened URL`() = testing {
        val requestDto = RequestDto("https://example.com/test/path")

        val response = client.post(POST_SHORT_URL) {
            contentType(ContentType.Application.Json)
            setBody(objectMapper.writeValueAsString(requestDto))
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val responseDto: ResponseDto = objectMapper.readValue(response.bodyAsText())
        val persisted = urlDatabaseService.findByUrl(requestDto.url)

        assertEquals(true, responseDto.success)
        assert(responseDto.url?.startsWith("http") == true)
        assertEquals(null, responseDto.error)

        assertNotNull(persisted)
        assertEquals(persisted.url, requestDto.url)
        assertEquals(persisted.shortUrlCode, responseDto.url?.substringAfterLast("/"))
    }

    @Test
    fun `POST shortUrl with invalid URL returns bad request`() = testing {
        val requestDto = RequestDto("invalid_url")
        val response = client.post(POST_SHORT_URL) {
            contentType(ContentType.Application.Json)
            setBody(objectMapper.writeValueAsString(requestDto))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)

        val responseDto: ResponseDto = objectMapper.readValue(response.bodyAsText())
        val persisted = urlDatabaseService.findByUrl(requestDto.url)

        assertEquals(false, responseDto.success)
        assertEquals(null, responseDto.url)
        assertEquals("Invalid URL format", responseDto.error)

        assertNull(persisted)
    }

    @Test
    fun `GET original URL returns original URL`() = testing {
        val originalUrl = "https://example.com/test/path"
        val postReq = RequestDto(originalUrl)
        val postResponse = client.post(POST_SHORT_URL) {
            contentType(ContentType.Application.Json)
            setBody(objectMapper.writeValueAsString(postReq))
        }
        val postResDto: ResponseDto = objectMapper.readValue(postResponse.bodyAsText())
        val shortUrl = postResDto.url ?: error("Missing shortened URL")
        val shortCode = shortUrl.substringAfterLast("/")

        val getResponse = client.get("$API_URL/$shortCode")

        assertEquals(HttpStatusCode.OK, getResponse.status)

        val getResDto: ResponseDto = objectMapper.readValue(getResponse.bodyAsText())
        assertEquals(true, getResDto.success)
        assertEquals(originalUrl, getResDto.url)
        assertEquals(null, getResDto.error)
    }

    @Test
    fun `GET non-existing shortUrl returns not found`() = testing {
        val response = client.get("$API_URL/nonexistentCode")

        assertEquals(HttpStatusCode.NotFound, response.status)

        val responseDto: ResponseDto = objectMapper.readValue(response.bodyAsText())
        assertEquals(false, responseDto.success)
        assertEquals(null, responseDto.url)
        assertEquals("Original URL not found", responseDto.error)
    }

    @Test
    fun `shorten URL without path`() = testing {
        val originalUrlNoPath = "https://example.com"

        val shortenResponse = client.post(POST_SHORT_URL) {
            contentType(ContentType.Application.Json)
            setBody("""{"url":"$originalUrlNoPath"}""")
        }
        assertEquals(HttpStatusCode.OK, shortenResponse.status)
        val shortenDto: ResponseDto = objectMapper.readValue(shortenResponse.bodyAsText())

        val shortUrl = shortenDto.url ?: error("No short URL returned")
        val shortCode = shortUrl.substringAfterLast("/")

        val retrieveResponse = client.get("$API_URL/$shortCode")
        assertEquals(HttpStatusCode.OK, retrieveResponse.status)

        val retrieveDto: ResponseDto = objectMapper.readValue(retrieveResponse.bodyAsText())
        val persisted = urlDatabaseService.findByUrl(originalUrlNoPath)

        assertEquals(true, retrieveDto.success)
        assertEquals(originalUrlNoPath, retrieveDto.url)
        assertEquals(null, retrieveDto.error)

        assertNotNull(persisted)
        assertEquals(persisted.url, originalUrlNoPath)
    }

    @Test
    fun `shorten two URLs with same path but different hosts`() = testing {
        val url1 = "https://example1.com/page"
        val url2 = "https://example2.com/page"

        val shortenResponse1 = client.post(POST_SHORT_URL) {
            contentType(ContentType.Application.Json)
            setBody("""{"url":"$url1"}""")
        }
        val shortenResponse2 = client.post(POST_SHORT_URL) {
            contentType(ContentType.Application.Json)
            setBody("""{"url":"$url2"}""")
        }
        val dto1: ResponseDto = objectMapper.readValue(shortenResponse1.bodyAsText())
        val persisted1 = urlDatabaseService.findByUrl(url1)

        val dto2: ResponseDto = objectMapper.readValue(shortenResponse2.bodyAsText())
        val persisted2 = urlDatabaseService.findByUrl(url2)

        assertNotNull(persisted1)
        assertNotNull(persisted2)
        assert(persisted1.url != persisted2.url)
        assert(persisted1.shortUrlCode != persisted2.shortUrlCode)

        assert(dto1.url != dto2.url)

        val code1 = dto1.url!!.substringAfterLast("/")
        val code2 = dto2.url!!.substringAfterLast("/")

        val response1 = client.get("$API_URL/$code1")
        val resDto1: ResponseDto = objectMapper.readValue(response1.bodyAsText())
        assertEquals(url1, resDto1.url)

        val response2 = client.get("$API_URL/$code2")
        val resDto2: ResponseDto = objectMapper.readValue(response2.bodyAsText())
        assertEquals(url2, resDto2.url)
    }

    @Test
    fun `shorten URL with trailing slash`() = testing {
        val url = "https://example.com/path/"

        val response = client.post(POST_SHORT_URL) {
            contentType(ContentType.Application.Json)
            setBody("""{"url":"$url"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val responseDto: ResponseDto = objectMapper.readValue(response.bodyAsText())
        val shortUrl = responseDto.url ?: error("No short URL returned")
        val shortCode = shortUrl.substringAfterLast("/")

        val retrieved = client.get("$API_URL/$shortCode")
        assertEquals(HttpStatusCode.OK, retrieved.status)

        val retrievedDto: ResponseDto = objectMapper.readValue(retrieved.bodyAsText())
        assertEquals(url, retrievedDto.url)
    }

    @Test
    fun `shorten URL with query parameters`() = testing {
        val url = "https://example.com/page?name=alice&ref=123"

        val response = client.post(POST_SHORT_URL) {
            contentType(ContentType.Application.Json)
            setBody("""{"url":"$url"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val responseDto: ResponseDto = objectMapper.readValue(response.bodyAsText())
        val shortUrl = responseDto.url ?: error("No short URL returned")
        val shortCode = shortUrl.substringAfterLast("/")

        val retrieved = client.get("$API_URL/$shortCode")
        assertEquals(HttpStatusCode.OK, retrieved.status)

        val retrievedDto: ResponseDto = objectMapper.readValue(retrieved.bodyAsText())
        assertEquals(url, retrievedDto.url)
    }

    @Test
    fun `shorten URL with uppercase and lowercase`() = testing {
        val url = "https://Example.COM/Path/Page"

        val response = client.post(POST_SHORT_URL) {
            contentType(ContentType.Application.Json)
            setBody("""{"url":"$url"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val responseDto: ResponseDto = objectMapper.readValue(response.bodyAsText())
        val shortUrl = responseDto.url ?: error("No short URL returned")
        val shortCode = shortUrl.substringAfterLast("/")

        val retrieved = client.get("$API_URL/$shortCode")
        assertEquals(HttpStatusCode.OK, retrieved.status)

        val retrievedDto: ResponseDto = objectMapper.readValue(retrieved.bodyAsText())
        val normalizedUrl = "https://example.com/Path/Page"
        assertEquals(normalizedUrl, retrievedDto.url)
    }

    @Test
    fun `shorten URL with unicode characters`() = testing {
        val url = "https://example.com/über/ßpecial"

        val response = client.post(POST_SHORT_URL) {
            contentType(ContentType.Application.Json)
            setBody("""{"url":"$url"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val responseDto: ResponseDto = objectMapper.readValue(response.bodyAsText())
        val shortUrl = responseDto.url ?: error("No short URL returned")
        val shortCode = shortUrl.substringAfterLast("/")

        val retrieved = client.get("$API_URL/$shortCode")
        assertEquals(HttpStatusCode.OK, retrieved.status)

        val retrievedDto: ResponseDto = objectMapper.readValue(retrieved.bodyAsText())
        assertEquals(url, retrievedDto.url)
    }

    @Test
    fun `shorten URL with explicit port`() = testing {
        val url = "https://example.com:8080/path"

        val response = client.post(POST_SHORT_URL) {
            contentType(ContentType.Application.Json)
            setBody("""{"url":"$url"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val responseDto: ResponseDto = objectMapper.readValue(response.bodyAsText())
        val shortUrl = responseDto.url ?: error("No short URL returned")
        val shortCode = shortUrl.substringAfterLast("/")

        val retrieved = client.get("$API_URL/$shortCode")
        assertEquals(HttpStatusCode.OK, retrieved.status)

        val retrievedDto: ResponseDto = objectMapper.readValue(retrieved.bodyAsText())
        assertEquals(url, retrievedDto.url)
    }
}