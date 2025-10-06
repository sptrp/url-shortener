package com.iponomarev

import com.iponomarev.model.ResponseDto
import com.iponomarev.util.AppLogger
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            AppLogger.log.warn("Bad request: ${cause.message}")
            call.respond(
                HttpStatusCode.BadRequest,
                ResponseDto(success = false, url = null, error = cause.message ?: "Bad Request")
            )
        }

        exception<NoSuchElementException> { call, cause ->
            AppLogger.log.warn("Not found: ${cause.message}")
            call.respond(
                HttpStatusCode.NotFound,
                ResponseDto(success = false, url = null, error = cause.message ?: "Not Found")
            )
        }

        exception<Throwable> { call, cause ->
            AppLogger.log.warn("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ResponseDto(success = false, url = null, error = "Internal server error occurred. Please contact support.")
            )
        }
    }
}