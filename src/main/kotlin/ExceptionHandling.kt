package com.iponomarev

import com.iponomarev.model.ResponseDto
import com.iponomarev.util.AppLogger
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import java.sql.SQLException
import java.util.UUID

fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            AppLogger.log.warn("Bad request on ${call.request.uri}: ${cause.message}")
            call.respond(
                HttpStatusCode.BadRequest,
                ResponseDto(success = false, url = null, error = cause.message ?: "Bad Request")
            )
        }

        exception<NoSuchElementException> { call, cause ->
            AppLogger.log.warn("Not found on ${call.request.uri}: ${cause.message}")
            call.respond(
                HttpStatusCode.NotFound,
                ResponseDto(success = false, url = null, error = cause.message ?: "Not Found")
            )
        }

        exception<SQLException> { call, cause ->
            AppLogger.log.error("Database error on ${call.request.uri}", cause)
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                ResponseDto(success = false, url = null, error = "Service temporarily unavailable")
            )
        }

        exception<Throwable> { call, cause ->
            val errorId = UUID.randomUUID().toString()
            AppLogger.log.error("Unhandled exception [ID: $errorId] on ${call.request.uri}", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ResponseDto(
                    success = false,
                    url = null,
                    error = "Internal server error occurred. Please contact support with error ID: $errorId"
                )
            )
        }
    }
}