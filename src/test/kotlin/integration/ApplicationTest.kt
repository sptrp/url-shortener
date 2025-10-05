package com.iponomarev.integration

import com.iponomarev.util.BaseIntegrationTest
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest : BaseIntegrationTest() {

    @Test
    fun testRoot() = testing {
        client.get("/healthcheck").apply {
            assertEquals(HttpStatusCode.Companion.OK, status)
        }
    }
}