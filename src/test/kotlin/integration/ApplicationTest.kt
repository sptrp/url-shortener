package integration

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import util.BaseIntegrationTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest : BaseIntegrationTest() {

    @Test
    fun testHealthcheck() = testing {
        client.get("/healthcheck").apply {
            assertEquals(HttpStatusCode.Companion.OK, status)
        }
    }
}